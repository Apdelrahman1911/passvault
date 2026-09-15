#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "rbconfig"
require "time"

MAX_FILE_BYTES = 16 * 1024 * 1024 * 1024

def required(name)
  value = ENV[name]&.strip
  abort("Missing required environment variable: #{name}") if value.nil? || value.empty?
  value
end

root = Pathname.new(ARGV.fetch(0) do
  abort("Usage: #{$PROGRAM_NAME} <release-assets-directory>")
end).expand_path
abort("Too many arguments") if ARGV.length > 1
abort("Release assets directory is unsafe") unless root.directory? && !root.symlink?

version = required("RELEASE_VERSION")
build_number = Integer(required("RELEASE_BUILD_NUMBER"), 10)
candidate_tag = required("RELEASE_CANDIDATE_TAG")
source_commit = required("RELEASE_SOURCE_COMMIT").downcase
source_tree = required("RELEASE_SOURCE_TREE").downcase

abort("Invalid release version") unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
abort("Invalid release build number") unless build_number.between?(1_000_000, 2_100_000_000)
abort("Invalid candidate tag") unless candidate_tag == "v#{version}-rc.#{build_number}"
abort("Invalid source commit") unless source_commit.match?(/\A[0-9a-f]{40}\z/)
abort("Invalid source tree") unless source_tree.match?(/\A[0-9a-f]{40}\z/)

desktop_receipt_path = root.join("desktop-artifact-receipt.json")
readiness_manifest_path = root.join("readiness-manifest.json")
unless desktop_receipt_path.file? && !desktop_receipt_path.symlink? &&
       readiness_manifest_path.file? && !readiness_manifest_path.symlink?
  abort("Desktop candidate provenance sidecars are missing or unsafe")
end
validator = Pathname.new(__dir__).join("validate-desktop-artifact-receipt.rb")
unless system(
  RbConfig.ruby,
  validator.to_s,
  desktop_receipt_path.to_s,
  version,
  build_number.to_s,
  source_commit,
  source_tree,
  out: File::NULL,
)
  abort("Desktop candidate receipt failed structural validation")
end
candidate_validator = Pathname.new(__dir__).join("validate-candidate-manifest.rb")
unless system(
  RbConfig.ruby,
  candidate_validator.to_s,
  readiness_manifest_path.to_s,
  source_commit,
  source_tree,
  out: File::NULL,
)
  abort("Readiness manifest failed structural validation")
end
desktop_receipt_sha256 = Digest::SHA256.file(desktop_receipt_path).hexdigest
readiness_manifest = JSON.parse(readiness_manifest_path.read(encoding: "UTF-8"))
unless readiness_manifest.fetch("desktop").fetch("artifactReceiptSha256") == desktop_receipt_sha256
  abort("Readiness manifest does not bind the Desktop candidate receipt")
end
desktop_receipt = JSON.parse(desktop_receipt_path.read(encoding: "UTF-8"))

files = root.children.select(&:file?).reject(&:symlink?).reject do |path|
  %w[release-provenance.json SHA256SUMS.txt].include?(path.basename.to_s)
end.sort_by { |path| path.basename.to_s }.map do |path|
  name = path.basename.to_s
  abort("Unsafe release asset name: #{name}") unless name.match?(/\A[A-Za-z0-9._+-]+\z/)
  size = path.size
  abort("Release asset is empty or too large: #{name}") unless size.positive? && size <= MAX_FILE_BYTES
  {
    fileName: name,
    sizeBytes: size,
    sha256: Digest::SHA256.file(path).hexdigest,
  }
end

manifest = {
  schemaVersion: 2,
  candidateTag: candidate_tag,
  marketingVersion: version,
  buildNumber: build_number,
  sourceCommit: source_commit,
  sourceTree: source_tree,
  createdAt: Time.now.utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
  candidateDesktop: {
    artifactReceiptSha256: desktop_receipt_sha256,
    sourceArtifactRun: desktop_receipt.fetch("sourceArtifactRun"),
    promotionInputs: desktop_receipt.fetch("artifacts"),
  },
  files: files,
}

puts JSON.pretty_generate(manifest)
