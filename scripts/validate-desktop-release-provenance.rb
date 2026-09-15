#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "rbconfig"
require "time"

MAX_MANIFEST_BYTES = 128 * 1024
MAX_FILE_BYTES = 16 * 1024 * 1024 * 1024
FIXED_ASSETS = %w[
  LICENSE.txt
  NOTICE.txt
  RELEASE-MANIFEST.txt
  THIRD_PARTY_LICENSES.zip
  THIRD_PARTY_NOTICES.md
  android-artifact-receipt.json
  desktop-artifact-receipt.json
  ios-artifact-receipt.json
  readiness-manifest.json
].freeze

def required_string(document, key)
  value = document.fetch(key)
  abort("#{key} must be a canonical non-empty string") unless value.is_a?(String) && !value.empty? && value == value.strip
  value
end

root = Pathname.new(ARGV.fetch(0) do
  abort("Usage: #{$PROGRAM_NAME} <release-assets-directory> <candidate-tag> <version> <build> <commit> <tree>")
end).expand_path
expected_tag = ARGV.fetch(1)
expected_version = ARGV.fetch(2)
expected_build = Integer(ARGV.fetch(3), 10)
expected_commit = ARGV.fetch(4).downcase
expected_tree = ARGV.fetch(5).downcase
abort("Too many arguments") if ARGV.length > 6
abort("Release assets directory is unsafe") unless root.directory? && !root.symlink?

manifest_path = root.join("release-provenance.json")
unless manifest_path.file? && !manifest_path.symlink? &&
       manifest_path.size.positive? && manifest_path.size <= MAX_MANIFEST_BYTES
  abort("Release provenance manifest is missing, unsafe, empty, or too large")
end
document = JSON.parse(manifest_path.read(encoding: "UTF-8"))
expected_keys = %w[
  schemaVersion candidateTag marketingVersion buildNumber sourceCommit sourceTree createdAt candidateDesktop files
]
abort("Release provenance fields are unexpected or incomplete") unless document.keys.sort == expected_keys.sort
abort("Unsupported release provenance schema") unless document.fetch("schemaVersion") == 2
abort("Candidate tag does not match") unless required_string(document, "candidateTag") == expected_tag
abort("Marketing version does not match") unless required_string(document, "marketingVersion") == expected_version
abort("Build number does not match") unless document.fetch("buildNumber") == expected_build
abort("Source commit does not match") unless required_string(document, "sourceCommit") == expected_commit
abort("Source tree does not match") unless required_string(document, "sourceTree") == expected_tree

created_at = required_string(document, "createdAt")
begin
  parsed_time = Time.iso8601(created_at)
rescue ArgumentError
  abort("Invalid release provenance creation time")
end
unless created_at.match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && parsed_time.utc_offset.zero?
  abort("Release provenance creation time must be canonical UTC")
end

desktop_receipt_path = root.join("desktop-artifact-receipt.json")
readiness_manifest_path = root.join("readiness-manifest.json")
unless desktop_receipt_path.file? && !desktop_receipt_path.symlink? &&
       readiness_manifest_path.file? && !readiness_manifest_path.symlink?
  abort("Desktop candidate provenance sidecars are missing or unsafe")
end
desktop_validator = Pathname.new(__dir__).join("validate-desktop-artifact-receipt.rb")
unless system(
  RbConfig.ruby,
  desktop_validator.to_s,
  desktop_receipt_path.to_s,
  expected_version,
  expected_build.to_s,
  expected_commit,
  expected_tree,
  out: File::NULL,
)
  abort("Desktop artifact receipt failed structural validation")
end
candidate_validator = Pathname.new(__dir__).join("validate-candidate-manifest.rb")
unless system(
  RbConfig.ruby,
  candidate_validator.to_s,
  readiness_manifest_path.to_s,
  expected_commit,
  expected_tree,
  out: File::NULL,
)
  abort("Readiness manifest failed structural validation")
end
desktop_receipt = JSON.parse(desktop_receipt_path.read(encoding: "UTF-8"))
candidate_desktop = document.fetch("candidateDesktop")
expected_candidate_desktop_keys = %w[artifactReceiptSha256 sourceArtifactRun promotionInputs]
unless candidate_desktop.is_a?(Hash) &&
       candidate_desktop.keys.sort == expected_candidate_desktop_keys.sort
  abort("Candidate Desktop provenance is malformed")
end
receipt_digest = Digest::SHA256.file(desktop_receipt_path).hexdigest
unless required_string(candidate_desktop, "artifactReceiptSha256") == receipt_digest &&
       candidate_desktop.fetch("sourceArtifactRun") == desktop_receipt.fetch("sourceArtifactRun") &&
       candidate_desktop.fetch("promotionInputs") == desktop_receipt.fetch("artifacts")
  abort("Release provenance does not preserve the exact candidate Desktop inputs")
end
readiness_manifest = JSON.parse(readiness_manifest_path.read(encoding: "UTF-8"))
unless readiness_manifest.fetch("desktop").fetch("artifactReceiptSha256") == receipt_digest
  abort("Readiness provenance does not bind the Desktop artifact receipt")
end

files = document.fetch("files")
abort("Release provenance files must be a non-empty array") unless files.is_a?(Array) && !files.empty?
actual_root_files = root.children.select(&:file?).reject(&:symlink?).map { |path| path.basename.to_s }.sort
manifest_file_names = files.map { |entry| entry.fetch("fileName") }
unless actual_root_files == (manifest_file_names + %w[release-provenance.json SHA256SUMS.txt]).sort
  abort("Release directory and provenance file list differ")
end
checksum_path = root.join("SHA256SUMS.txt")
abort("SHA256SUMS.txt is missing or unsafe") unless checksum_path.file? && !checksum_path.symlink?
abort("Release provenance file names are not sorted and unique") unless manifest_file_names == manifest_file_names.sort.uniq

files.each do |entry|
  expected_entry_keys = %w[fileName sizeBytes sha256]
  abort("Release file entry is malformed") unless entry.is_a?(Hash) && entry.keys.sort == expected_entry_keys.sort
  name = required_string(entry, "fileName")
  abort("Unsafe release file name") unless name.match?(/\A[A-Za-z0-9._+-]+\z/) && File.basename(name) == name
  size = entry.fetch("sizeBytes")
  abort("Invalid release file size") unless size.is_a?(Integer) && size.positive? && size <= MAX_FILE_BYTES
  sha256 = required_string(entry, "sha256")
  abort("Invalid release file digest") unless sha256.match?(/\A[0-9a-f]{64}\z/)
  path = root.join(name)
  unless path.file? && !path.symlink? && path.dirname.realpath == root.realpath
    abort("Release file is missing or unsafe: #{name}")
  end
  abort("Release file size differs: #{name}") unless path.size == size
  abort("Release file digest differs: #{name}") unless Digest::SHA256.file(path).hexdigest == sha256
end

missing_fixed = FIXED_ASSETS - manifest_file_names
abort("Required release sidecars are missing: #{missing_fixed.join(', ')}") unless missing_fixed.empty?
%w[exe msi deb rpm].each do |extension|
  matches = manifest_file_names.grep(/\.#{extension}\z/i)
  abort("Expected exactly one .#{extension} release asset") unless matches.length == 1
end
%w[arm64 x64].each do |architecture|
  matches = manifest_file_names.grep(/-macos-#{architecture}\.dmg\z/)
  abort("Expected exactly one macOS #{architecture} DMG") unless matches.length == 1
end
all_dmgs = manifest_file_names.grep(/\.dmg\z/i)
abort("Expected exactly two macOS DMG release assets") unless all_dmgs.length == 2

%w[linuxDeb linuxRpm].each do |key|
  promoted = desktop_receipt.fetch("artifacts").fetch(key)
  released = files.find { |entry| entry.fetch("fileName") == promoted.fetch("fileName") }
  unless released && released.fetch("sizeBytes") == promoted.fetch("sizeBytes") &&
         released.fetch("sha256") == promoted.fetch("sha256")
    abort("Released Linux package differs from the tested candidate: #{key}")
  end
end

puts "Validated exact signed desktop release assets for #{expected_tag}."
