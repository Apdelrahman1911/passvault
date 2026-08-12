#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "rbconfig"

manifest_path = Pathname.new(ARGV.fetch(0) do
  abort("Usage: #{$PROGRAM_NAME} <candidate-manifest.json> <receipt-directory> <commit> <tree>")
end).expand_path
receipt_root = Pathname.new(ARGV.fetch(1)).expand_path
expected_commit = ARGV.fetch(2).downcase
expected_tree = ARGV.fetch(3).downcase
abort("Too many arguments") if ARGV.length > 4

unless manifest_path.file? && !manifest_path.symlink? &&
       receipt_root.directory? && !receipt_root.symlink?
  abort("Candidate manifest or receipt directory is unsafe")
end

manifest = JSON.parse(manifest_path.read(encoding: "UTF-8"))
version = manifest.fetch("marketingVersion")
build_number = manifest.fetch("buildNumber")

receipts = {
  "android" => receipt_root.join("android-artifact-receipt.json"),
  "ios" => receipt_root.join("ios-artifact-receipt.json"),
}

receipts.each do |platform, path|
  unless path.file? && !path.symlink? && path.dirname.realpath == receipt_root.realpath
    abort("Missing or unsafe #{platform} artifact receipt")
  end
  expected_digest = manifest.fetch(platform).fetch("artifactReceiptSha256")
  actual_digest = Digest::SHA256.file(path).hexdigest
  abort("#{platform} artifact-receipt digest does not match candidate manifest") unless actual_digest == expected_digest

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
  abort("#{platform} artifact receipt failed structural validation") unless validated

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

puts "Candidate mobile artifact provenance is bound to commit #{expected_commit}."
