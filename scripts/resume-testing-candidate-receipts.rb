#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "pathname"
require "rbconfig"
require "tmpdir"
require_relative "lib/testing_candidate_resume"
require_relative "lib/github_api"

def abort_usage
  abort(
    "Usage: #{$PROGRAM_NAME} --version VERSION --build-number BUILD " \
    "--source-commit SHA --source-tree SHA --confirmation TEXT --output-dir PATH",
  )
end

options = {}
argv = ARGV.dup
until argv.empty?
  switch = argv.shift
  case switch
  when "--version" then options[:version] = argv.shift
  when "--build-number" then options[:build_number] = argv.shift
  when "--source-commit" then options[:source_commit] = argv.shift
  when "--source-tree" then options[:source_tree] = argv.shift
  when "--confirmation" then options[:confirmation] = argv.shift
  when "--output-dir" then options[:output_dir] = argv.shift
  else abort_usage
  end
end
required = %i[version build_number source_commit source_tree confirmation output_dir]
abort_usage if required.any? { |key| options[key].to_s.strip.empty? }

begin
  PassVault::TestingCandidateResume.validate_confirmation!(
    options[:confirmation],
    version: options[:version],
    build_number: options[:build_number],
  )
  PassVault::TestingCandidateResume.validate_identity!(
    version: options[:version], build_number: options[:build_number],
    source_commit: options[:source_commit],
  )
  options[:source_commit] = PassVault::TestingCandidateResume.canonical_sha(options[:source_commit])
  options[:source_tree] = PassVault::TestingCandidateResume.canonical_sha(options[:source_tree])
rescue RuntimeError => error
  abort(error.message)
end

repository = PassVault::GitHubApi.repository!(ENV.fetch("GITHUB_REPOSITORY"))
if ENV["GH_TOKEN"].to_s.strip.empty? && ENV["GITHUB_TOKEN"].to_s.strip.empty?
  abort("GH_TOKEN or GITHUB_TOKEN is required")
end

def gh_collection(path, collection, query = {})
  args = ["gh", "api", "--paginate", "--slurp"]
  unless query.empty?
    encoded = query.map { |key, value| "#{key}=#{value}" }.join("&")
    path = "#{path}?#{encoded}"
  end
  stdout, stderr, status = Open3.capture3(*args, path)
  abort(stderr.empty? ? "gh api #{path} failed" : stderr) unless status.success?

  abort("GitHub pagination response exceeds the size limit") if stdout.bytesize > 16 * 1024 * 1024
  pages = JSON.parse(stdout)
  unless pages.is_a?(Array) && pages.length.between?(1, 100)
    abort("GitHub pagination response must contain 1 through 100 pages")
  end
  entries = pages.flat_map do |page|
    unless page.is_a?(Hash) && page[collection].is_a?(Array) &&
           page[collection].all? { |entry| entry.is_a?(Hash) }
      abort("GitHub pagination page has invalid #{collection}")
    end
    page.fetch(collection)
  end
  abort("GitHub pagination collection exceeds the entry limit") if entries.length > 10_000
  entries
rescue JSON::ParserError
  abort("GitHub pagination response is not valid JSON")
end

def run!(*args)
  stdout, stderr, status = Open3.capture3(*args)
  abort(stderr.empty? ? "#{args.join(' ')} failed" : stderr) unless status.success?

  stdout
end

def git_commit_has_tree?(commit, tree)
  stdout, _stderr, status = Open3.capture3("git", "rev-parse", "#{commit}^{tree}")
  status.success? && stdout.strip == tree
end

def git_is_ancestor?(ancestor, descendant)
  _stdout, _stderr, status = Open3.capture3("git", "merge-base", "--is-ancestor", ancestor, descendant)
  status.success?
end

unless git_commit_has_tree?(options[:source_commit], options[:source_tree])
  abort("Current testing commit does not have the requested source tree")
end

output_dir = Pathname.new(options[:output_dir]).expand_path
abort("Resume output directory is unsafe") if output_dir.symlink?
FileUtils.mkdir_p(output_dir)
abort("Resume output directory must be a real directory") unless output_dir.directory? && !output_dir.symlink?

workflow_runs = gh_collection(
  "repos/#{repository}/actions/workflows/testing-release.yml/runs",
  "workflow_runs",
  "branch" => "testing",
  "per_page" => "100",
)

jobs_by_run_id = {}
artifacts_by_run_id = {}
workflow_runs.each do |run|
  run_id = PassVault::TestingCandidateResume.canonical_run_id(run)
  jobs_by_run_id[run_id] = gh_collection(
    "repos/#{repository}/actions/runs/#{run_id}/jobs", "jobs", "per_page" => "100",
  )
  artifacts_by_run_id[run_id] = gh_collection(
    "repos/#{repository}/actions/runs/#{run_id}/artifacts", "artifacts", "per_page" => "100",
  )
end

runs = PassVault::TestingCandidateResume.parse_github_runs(
  workflow_runs,
  jobs_by_run_id,
  artifacts_by_run_id,
)
candidates = PassVault::TestingCandidateResume.select_receipt_sources(
  runs,
  version: options[:version],
  build_number: options[:build_number],
)

validator = Pathname.new(__dir__).join("validate-mobile-artifact-receipt.rb")
receipts_by_source = {}
Dir.mktmpdir("passvault-testing-resume.") do |temporary_root|
  temporary_root = Pathname.new(temporary_root)
  %w[android ios].each do |platform|
    candidates.fetch(platform).each do |candidate|
      destination = temporary_root.join(PassVault::TestingCandidateResume.source_key(candidate))
      FileUtils.mkdir_p(destination)
      run!(
        "gh", "run", "download", candidate.fetch("run_id"),
        "--repo", repository,
        "--name", candidate.fetch("artifact_name"),
        "--dir", destination.to_s,
      )
      signed_dir = destination.join("signed")
      FileUtils.mkdir_p(signed_dir)
      run!(
        "gh", "run", "download", candidate.fetch("run_id"),
        "--repo", repository,
        "--name", candidate.fetch("signed_artifact_name"),
        "--dir", signed_dir.to_s,
      )
      receipt_path = destination.join("#{platform}-artifact-receipt.json")
      abort("Resumed #{platform} receipt is missing from #{candidate.fetch('artifact_name')}") unless receipt_path.file?
      receipt = JSON.parse(receipt_path.read(encoding: "UTF-8"))
      original = PassVault::TestingCandidateResume.resolve_original_candidate([receipt])
      unless original.fetch("sourceTree") == options[:source_tree]
        abort("Changed-tree resume is forbidden; allocate a new candidate/build instead")
      end
      validated = system(
        RbConfig.ruby,
        validator.to_s,
        receipt_path.to_s,
        platform,
        options[:version],
        options[:build_number].to_s,
        original.fetch("sourceCommit"),
        original.fetch("sourceTree"),
        signed_dir.to_s,
        out: File::NULL,
      )
      abort("Resumed #{platform} receipt failed structural validation") unless validated
      unless git_commit_has_tree?(original.fetch("sourceCommit"), original.fetch("sourceTree"))
        abort("Resumed #{platform} receipt commit does not have its recorded tree")
      end
      unless git_is_ancestor?(original.fetch("sourceCommit"), options[:source_commit])
        abort("Resumed #{platform} receipt commit is not an ancestor of the current testing commit")
      end
      PassVault::TestingCandidateResume.bind_receipt!(
        receipt,
        platform: platform,
        version: options[:version],
        build_number: options[:build_number],
        source_commit: original.fetch("sourceCommit"),
        source_tree: original.fetch("sourceTree"),
      )
      receipt.fetch("artifacts").each do |artifact|
        artifact_path = signed_dir.join(artifact.fetch("fileName"))
        abort("Resumed #{platform} signed artifact is missing: #{artifact.fetch('fileName')}") unless artifact_path.file?
        attestation, attestation_status = Open3.capture2e(
          "gh", "attestation", "verify", artifact_path.to_s,
          "--repo", repository,
          "--signer-workflow", "#{repository}/.github/workflows/testing-release.yml",
          "--source-ref", "refs/heads/testing",
          "--source-digest", original.fetch("sourceCommit"),
          "--deny-self-hosted-runners",
        )
        unless attestation_status.success?
          abort("Resumed #{platform} artifact #{artifact.fetch('fileName')} is not attested for this testing commit: #{attestation}")
        end
      end
      receipts_by_source[PassVault::TestingCandidateResume.source_key(candidate)] = receipt
      candidate["receipt_path"] = receipt_path.to_s
    end
  end

  chosen = PassVault::TestingCandidateResume.choose_unique_sources(candidates, receipts_by_source)
  original = PassVault::TestingCandidateResume.resolve_original_candidate(receipts_by_source.values_at(*chosen.values.map { |candidate| PassVault::TestingCandidateResume.source_key(candidate) }))
  unless original.fetch("sourceTree") == options[:source_tree]
    abort("Resumed receipts do not match the current testing tree")
  end
  unless git_is_ancestor?(original.fetch("sourceCommit"), options[:source_commit])
    abort("Resumed receipts are not from an ancestor of the current testing commit")
  end
  chosen.each do |platform, candidate|
    FileUtils.install(
      candidate.fetch("receipt_path"),
      output_dir.join("#{platform}-artifact-receipt.json"),
      mode: 0o600,
    )
  end
  summary = {
    "version" => options[:version],
    "buildNumber" => Integer(options[:build_number], 10),
    "sourceCommit" => original.fetch("sourceCommit"),
    "sourceTree" => original.fetch("sourceTree"),
    "androidRunId" => chosen.fetch("android").fetch("run_id"),
    "iosRunId" => chosen.fetch("ios").fetch("run_id"),
  }
  File.write(output_dir.join("resume-receipt-sources.json"), JSON.pretty_generate(summary))
  puts "Resumed Android receipt from run #{summary['androidRunId']} and iOS receipt from run #{summary['iosRunId']}."
end
