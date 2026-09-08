#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "rbconfig"

mobile_only = ARGV.delete("--mobile-only")
manifest_path = Pathname.new(ARGV.fetch(0) do
  abort(
    "Usage: #{$PROGRAM_NAME} [--mobile-only] <candidate-manifest.json> <receipt-directory> <commit> <tree> " \
      "[desktop-artifact-directory]",
  )
end).expand_path
receipt_root = Pathname.new(ARGV.fetch(1)).expand_path
expected_commit = ARGV.fetch(2).downcase
expected_tree = ARGV.fetch(3).downcase
desktop_artifact_root = ARGV[4]&.then { |path| Pathname.new(path).expand_path }
abort("Too many arguments") if ARGV.length > 5
abort("Mobile-only validation cannot validate Desktop artifacts") if mobile_only && desktop_artifact_root

unless manifest_path.file? && !manifest_path.symlink? &&
       receipt_root.directory? && !receipt_root.symlink?
  abort("Candidate manifest or receipt directory is unsafe")
end

# This validator proves artifact binding, not Store readiness; callers retain
# their separate non-pending readiness gate and attestation checks.
manifest_arguments = ["--allow-pending"]
manifest_arguments << "--mobile-only" if mobile_only
unless system(
  RbConfig.ruby, File.join(__dir__, "validate-candidate-manifest.rb"),
  *manifest_arguments, manifest_path.to_s, expected_commit, expected_tree,
  out: File::NULL,
)
  abort("Candidate manifest failed structural/source validation")
end
manifest = JSON.parse(manifest_path.read(encoding: "UTF-8"))
version = manifest.fetch("marketingVersion")
build_number = manifest.fetch("buildNumber")

receipts = {
  "android" => receipt_root.join("android-artifact-receipt.json"),
  "ios" => receipt_root.join("ios-artifact-receipt.json"),
}
receipts["desktop"] = receipt_root.join("desktop-artifact-receipt.json") unless mobile_only

receipts.each do |platform, path|
  unless path.file? && !path.symlink? && path.dirname.realpath == receipt_root.realpath
    abort("Missing or unsafe #{platform} artifact receipt")
  end
  expected_digest = manifest.fetch(platform).fetch("artifactReceiptSha256")
  actual_digest = Digest::SHA256.file(path).hexdigest
  abort("#{platform} artifact-receipt digest does not match candidate manifest") unless actual_digest == expected_digest

  if platform == "desktop"
    validator = Pathname.new(__dir__).join("validate-desktop-artifact-receipt.rb")
    validator_arguments = [
      path.to_s,
      version,
      build_number.to_s,
      expected_commit,
      expected_tree,
    ]
    validator_arguments << desktop_artifact_root.to_s if desktop_artifact_root
    validated = system(
      RbConfig.ruby,
      validator.to_s,
      *validator_arguments,
      out: File::NULL,
    )
  else
    validator = Pathname.new(__dir__).join("validate-mobile-artifact-receipt.rb")
    validated = system(
      RbConfig.ruby,
      validator.to_s,
      path.to_s,
      platform,
      version,
      build_number.to_s,
      expected_commit,
      expected_tree,
      out: File::NULL,
    )
  end
  abort("#{platform} artifact receipt failed structural validation") unless validated

  next if platform == "desktop"

  receipt = JSON.parse(path.read(encoding: "UTF-8"))
  expected_identifier = platform == "android" ?
    manifest.fetch("android").fetch("packageName") :
    manifest.fetch("ios").fetch("bundleId")
  expected_fingerprint = platform == "android" ?
    manifest.fetch("android").fetch("signingCertificateSha256") :
    manifest.fetch("ios").fetch("signingIdentitySha1")
  abort("#{platform} receipt identifier does not match candidate manifest") unless receipt.fetch("identifier") == expected_identifier
  unless receipt.fetch("signingFingerprint") == expected_fingerprint
    abort("#{platform} receipt signing fingerprint does not match candidate manifest")
  end
end

puts "Candidate artifact provenance is bound to commit #{expected_commit}."
