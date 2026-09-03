#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "time"

MAX_FILE_BYTES = 16 * 1024 * 1024 * 1024

ARTIFACTS = {
  "linuxDeb" => {
    env: "DESKTOP_LINUX_DEB_PATH",
    platform: "linux",
    architecture: "x64",
    role: "package",
  },
  "linuxRpm" => {
    env: "DESKTOP_LINUX_RPM_PATH",
    platform: "linux",
    architecture: "x64",
    role: "package",
  },
  "windowsX64AppImage" => {
    env: "DESKTOP_WINDOWS_X64_APP_IMAGE_PATH",
    platform: "windows",
    architecture: "x64",
    role: "app-image",
  },
  "macosArm64AppImage" => {
    env: "DESKTOP_MACOS_ARM64_APP_IMAGE_PATH",
    platform: "macos",
    architecture: "arm64",
    role: "app-image",
  },
  "macosX64AppImage" => {
    env: "DESKTOP_MACOS_X64_APP_IMAGE_PATH",
    platform: "macos",
    architecture: "x64",
    role: "app-image",
  },
}.freeze

def required(name)
  value = ENV[name]&.strip
  abort("Missing required environment variable: #{name}") if value.nil? || value.empty?
  value
end

version = required("RECEIPT_VERSION")
build_number = Integer(required("RECEIPT_BUILD_NUMBER"), 10)
source_commit = required("RECEIPT_SOURCE_COMMIT").downcase
source_tree = required("RECEIPT_SOURCE_TREE").downcase
candidate_tag = required("RECEIPT_CANDIDATE_TAG")
repository = required("RECEIPT_SOURCE_REPOSITORY")
workflow = required("RECEIPT_SOURCE_WORKFLOW")
run_id = Integer(required("RECEIPT_SOURCE_RUN_ID"), 10)
run_attempt = Integer(required("RECEIPT_SOURCE_RUN_ATTEMPT"), 10)

semantic_version = /\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/
abort("Invalid receipt version") unless version.match?(semantic_version)
unless build_number.between?(1_000_000, 2_100_000_000)
  abort("Invalid receipt build number")
end
abort("Invalid receipt source commit") unless source_commit.match?(/\A[0-9a-f]{40}\z/)
abort("Invalid receipt source tree") unless source_tree.match?(/\A[0-9a-f]{40}\z/)
abort("Invalid receipt candidate tag") unless candidate_tag == "v#{version}-rc.#{build_number}"
unless repository.match?(%r{\A[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\z})
  abort("Invalid source repository")
end
unless workflow == ".github/workflows/testing-release.yml"
  abort("Unexpected source workflow")
end
abort("Invalid source workflow run ID") unless run_id.positive?
abort("Invalid source workflow run attempt") unless run_attempt.between?(1, 1_000_000)

artifacts = ARTIFACTS.to_h do |key, policy|
  path = Pathname.new(required(policy.fetch(:env))).expand_path
  unless path.file? && !path.symlink?
    abort("Desktop promotion input is missing or unsafe: #{key}")
  end
  name = path.basename.to_s
  unless name.match?(/\A[A-Za-z0-9._+-]+\z/)
    abort("Desktop promotion input has an unsafe file name: #{key}")
  end
  escaped_version = Regexp.escape(version)
  valid_name = case key
  when "linuxDeb"
    name.end_with?(".deb") &&
      name.delete_suffix(".deb").match?(/(?:\A|[^0-9.])#{escaped_version}(?:[^0-9.]|\z)/)
  when "linuxRpm"
    name.end_with?(".rpm") &&
      name.delete_suffix(".rpm").match?(/(?:\A|[^0-9.])#{escaped_version}(?:[^0-9.]|\z)/)
  when "windowsX64AppImage"
    name == "PassVault-#{version}-windows-x64-app-image.zip"
  when "macosArm64AppImage"
    name == "PassVault-#{version}-macos-arm64-app-image.zip"
  when "macosX64AppImage"
    name == "PassVault-#{version}-macos-x64-app-image.zip"
  else
    false
  end
  abort("Desktop promotion input has an unexpected file name: #{key}") unless valid_name

  size = path.size
  unless size.positive? && size <= MAX_FILE_BYTES
    abort("Desktop promotion input has an invalid size: #{key}")
  end
  [
    key,
    {
      platform: policy.fetch(:platform),
      architecture: policy.fetch(:architecture),
      role: policy.fetch(:role),
      fileName: name,
      sizeBytes: size,
      sha256: Digest::SHA256.file(path).hexdigest,
    },
  ]
end

receipt = {
  schemaVersion: 1,
  marketingVersion: version,
  buildNumber: build_number,
  candidateTag: candidate_tag,
  sourceCommit: source_commit,
  sourceTree: source_tree,
  sourceArtifactRun: {
    repository: repository,
    workflow: workflow,
    runId: run_id,
    runAttempt: run_attempt,
  },
  createdAt: Time.now.utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
  artifacts: artifacts,
}

puts JSON.pretty_generate(receipt)
