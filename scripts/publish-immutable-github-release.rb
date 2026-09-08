#!/usr/bin/env ruby
# frozen_string_literal: true

require "pathname"
require_relative "lib/github_api"

abort("Usage: #{$PROGRAM_NAME} <version> <source-commit> <verified-artifact-directory>") unless ARGV.length == 3
version, source_commit, directory = ARGV
abort("Invalid stable marketing version") unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
abort("Expected a full candidate source commit") unless source_commit.match?(/\A[0-9a-f]{40}\z/)
repository = PassVault::GitHubApi.repository!(ENV.fetch("GITHUB_REPOSITORY"))
tag = "v#{version}"
root = Pathname.new(directory).expand_path
abort("Verified artifact directory is unsafe") unless root.directory? && !root.symlink?
files = root.children.sort
abort("Verified artifact bundle is empty or unsafe") unless files.any? && files.all? { |path| path.file? && !path.symlink? }

def tag_commit(repository, tag)
  status, reference = PassVault::GitHubApi.request(
    "GET", "repos/#{repository}/git/ref/tags/#{tag}", acceptable: [200, 404],
  )
  return nil if status == 404

  object = reference.fetch("object")
  seen = []
  8.times do
    unless object.is_a?(Hash) && object["sha"].is_a?(String) && object["sha"].match?(/\A[0-9a-f]{40}\z/)
      abort("The stable tag has an invalid Git object")
    end
    sha = object.fetch("sha")
    abort("The stable tag contains a cycle") if seen.include?(sha)
    seen << sha
    return sha if object["type"] == "commit"
    abort("The stable tag does not resolve to a commit") unless object["type"] == "tag"

    _status, annotated = PassVault::GitHubApi.request("GET", "repos/#{repository}/git/tags/#{sha}")
    object = annotated.fetch("object")
  end
  abort("The stable tag has too many annotated-tag indirections")
end

release_status, = PassVault::GitHubApi.request(
  "GET", "repos/#{repository}/releases/tags/#{tag}", acceptable: [200, 404],
)
abort("A GitHub Release already exists; refusing to overwrite it") if release_status == 200

existing_commit = tag_commit(repository, tag)
if existing_commit.nil?
  # POST creates only an absent ref. A concurrent creator may win (422); it
  # must then pass the same exact peeled-commit check. Never PATCH/force a tag.
  PassVault::GitHubApi.request(
    "POST", "repos/#{repository}/git/refs", acceptable: [201, 422],
    fields: { "ref" => "refs/tags/#{tag}", "sha" => source_commit },
  )
  existing_commit = tag_commit(repository, tag)
end
abort("The stable tag does not identify the verified candidate commit") unless existing_commit == source_commit

notes = "Exact production-validated Desktop packages; SHA256SUMS.txt and release-provenance.json " \
  "bind every asset to the tested candidate, Desktop promotion inputs, and mobile receipts. " \
  "No artifact was rebuilt or re-signed during publication."
unless system(
  "gh", "release", "create", tag, *files.map(&:to_s), "--repo", repository,
  "--verify-tag", "--generate-notes", "--title", "PassVault #{version}", "--notes", notes,
)
  abort("Stable release creation failed; no existing tag or release was overwritten")
end
abort("The stable tag changed during publication") unless tag_commit(repository, tag) == source_commit
puts "Published the frozen bundle for #{tag} at #{source_commit}."
