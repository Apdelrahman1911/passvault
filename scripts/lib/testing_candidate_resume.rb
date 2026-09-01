# frozen_string_literal: true

require "json"

module PassVault
  module TestingCandidateResume
    ANDROID_JOB = "mobile-internal / Android internal"
    IOS_JOB = "mobile-internal / iOS internal"
    WORKFLOW_PATH = ".github/workflows/testing-release.yml"
    ARTIFACT_HASH_KEYS = %w[kind fileName sizeBytes sha256].freeze

    module_function

    def required_confirmation(version, build_number)
      "resume:#{version}:#{build_number}"
    end

    def validate_confirmation!(confirmation, version:, build_number:)
      expected = required_confirmation(version, build_number)
      raise "Resume confirmation must be exactly #{expected}" unless confirmation == expected
    end

    def select_receipt_sources(runs, version:, build_number:, source_commit: nil, source_tree: nil)
      if source_commit
        validate_identity!(version: version, build_number: build_number, source_commit: source_commit)
      else
        unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
          raise "Invalid marketing version"
        end
        unless build_number.to_s.match?(/\A[1-9]\d*\z/) &&
               build_number.to_s.length <= 10 &&
               Integer(build_number, 10) <= 2_100_000_000
          raise "Invalid Store build number"
        end
      end
      source_tree = canonical_sha(source_tree) if source_tree
      android_candidates = []
      ios_candidates = []

      Array(runs).each do |run|
        run_id = canonical_run_id(run)
        next unless accepted_run_head?(run, source_commit: source_commit, source_tree: source_tree)

        jobs = Array(run["jobs"] || run[:jobs])
        artifacts = Array(run["artifacts"] || run[:artifacts])
        if job_succeeded?(jobs, ANDROID_JOB)
          android_candidates << source_record(run_id, artifacts, "android", build_number)
        end
        if job_succeeded?(jobs, IOS_JOB)
          ios_candidates << source_record(run_id, artifacts, "ios", build_number)
        end
      end

      {
        "android" => android_candidates,
        "ios" => ios_candidates,
      }
    end

    def choose_unique_sources(candidates_by_platform, receipts_by_source)
      %w[android ios].to_h do |platform|
        candidates = Array(candidates_by_platform[platform])
        raise "No successful #{platform} Testing Candidate receipt exists for this commit and build" if candidates.empty?

        resolved = candidates.map do |candidate|
          receipt = receipts_by_source.fetch(source_key(candidate)) do
            raise "Missing #{platform} receipt for run #{candidate.fetch('run_id')}"
          end
          [candidate, artifact_identity(receipt, platform)]
        end
        identities = resolved.map { |_candidate, identity| identity }.uniq
        unless identities.one?
          raise "Conflicting successful #{platform} receipts exist for this commit and build"
        end

        [platform, resolved.min_by { |candidate, _identity| Integer(candidate.fetch("run_id")) }.first]
      end
    end

    def bind_receipt!(receipt, platform:, version:, build_number:, source_commit:, source_tree:)
      raise "Resume receipt root must be an object" unless receipt.is_a?(Hash)
      unless receipt["platform"] == platform
        raise "Resume receipt platform #{receipt['platform'].inspect} does not match #{platform}"
      end
      unless receipt["marketingVersion"] == version
        raise "Resume receipt version does not match #{version}"
      end
      unless receipt["buildNumber"] == Integer(build_number, 10)
        raise "Resume receipt build number does not match #{build_number}"
      end
      unless receipt["sourceCommit"] == source_commit
        raise "Resume receipt source commit does not match the original candidate commit"
      end
      unless receipt["sourceTree"] == source_tree
        raise "Resume receipt source tree does not match the original candidate tree"
      end
    end

    def resolve_original_candidate(receipts)
      commits = Array(receipts).map { |receipt| canonical_sha(receipt.fetch("sourceCommit")) }.uniq
      trees = Array(receipts).map { |receipt| canonical_sha(receipt.fetch("sourceTree")) }.uniq
      raise "Resumed receipts do not share one original candidate commit" unless commits.one?
      raise "Resumed receipts do not share one original candidate tree" unless trees.one?

      { "sourceCommit" => commits.first, "sourceTree" => trees.first }
    end

    def accepted_run_head?(run, source_commit:, source_tree:)
      head_sha = canonical_sha(run["head_sha"] || run[:head_sha])
      return true if source_commit.nil? && source_tree.nil?
      return true if source_commit && head_sha == source_commit
      return false if source_tree.nil?

      head_tree = run["head_tree"] || run[:head_tree]
      return false if head_tree.to_s.strip.empty?

      canonical_sha(head_tree) == source_tree
    end

    def parse_github_runs(workflow_runs, jobs_by_run_id, artifacts_by_run_id)
      Array(workflow_runs).filter_map do |run|
        path = run.dig("path") || run.dig("workflow", "path")
        next unless path.nil? || path == WORKFLOW_PATH

        run_id = canonical_run_id(run)
        {
          "id" => run_id,
          "head_sha" => canonical_sha(run.fetch("head_sha")),
          "head_tree" => run["head_tree"] || run[:head_tree],
          "jobs" => Array(jobs_by_run_id[run_id] || jobs_by_run_id[run_id.to_s]),
          "artifacts" => Array(artifacts_by_run_id[run_id] || artifacts_by_run_id[run_id.to_s]),
        }
      end
    end

    def artifact_identity(receipt, platform)
      bind_platform = receipt.fetch("platform")
      raise "Receipt platform #{bind_platform} does not match #{platform}" unless bind_platform == platform

      artifacts = receipt.fetch("artifacts")
      raise "Resume receipt artifacts must be an array" unless artifacts.is_a?(Array)

      artifacts.map do |artifact|
        raise "Resume receipt artifact must be an object" unless artifact.is_a?(Hash)

        ARTIFACT_HASH_KEYS.to_h { |key| [key, artifact.fetch(key)] }
      end
    end

    def source_key(candidate)
      "#{candidate.fetch('platform')}:#{candidate.fetch('run_id')}:#{candidate.fetch('artifact_name')}"
    end

    def source_record(run_id, artifacts, platform, build_number)
      receipt = named_artifact(artifacts, "mobile-receipt-#{platform}-#{build_number}")
      signed = named_artifact(artifacts, "mobile-signed-#{platform}-#{build_number}")
      raise "Successful #{platform} job is missing its receipt artifact" unless receipt
      raise "Successful #{platform} job is missing its signed artifact bundle" unless signed

      {
        "platform" => platform,
        "run_id" => run_id,
        "artifact_id" => receipt["id"] || receipt[:id],
        "artifact_name" => receipt["name"] || receipt[:name],
        "signed_artifact_id" => signed["id"] || signed[:id],
        "signed_artifact_name" => signed["name"] || signed[:name],
      }
    end

    def named_artifact(artifacts, expected)
      matches = artifacts.select { |artifact| (artifact["name"] || artifact[:name]) == expected }
      raise "Multiple #{expected} artifacts exist in one run" if matches.length > 1

      matches.first
    end

    def receipt_and_signed_artifacts(artifacts, platform, build_number)
      receipt = named_artifact(artifacts, "mobile-receipt-#{platform}-#{build_number}")
      signed = named_artifact(artifacts, "mobile-signed-#{platform}-#{build_number}")
      return unless receipt && signed

      { "receipt" => receipt, "signed" => signed }
    end

    def job_succeeded?(jobs, name)
      matches = jobs.select { |job| (job["name"] || job[:name]) == name }
      matches.any? { |job| (job["conclusion"] || job[:conclusion]) == "success" }
    end

    def canonical_run_id(run)
      value = run.is_a?(Hash) ? (run["id"] || run[:id]) : run
      text = value.to_s
      raise "Invalid Actions run id" unless text.match?(/\A[1-9]\d*\z/)

      text
    end

    def canonical_sha(value)
      sha = value.to_s.downcase
      raise "Invalid Git commit SHA" unless sha.match?(/\A[0-9a-f]{40}\z/)

      sha
    end

    def validate_identity!(version:, build_number:, source_commit:)
      unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
        raise "Invalid marketing version"
      end
      unless build_number.to_s.match?(/\A[1-9]\d*\z/) &&
             build_number.to_s.length <= 10 &&
             Integer(build_number, 10) <= 2_100_000_000
        raise "Invalid Store build number"
      end

      canonical_sha(source_commit)
    end
  end
end
