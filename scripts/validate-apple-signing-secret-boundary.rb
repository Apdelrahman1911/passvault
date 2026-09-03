#!/usr/bin/env ruby

require "yaml"

release_path = File.expand_path(ARGV.fetch(0, ".github/workflows/release.yml"))
mobile_path = File.expand_path(ARGV.fetch(1, ".github/workflows/mobile-store-release.yml"))
importer_path = File.expand_path(
  ARGV.fetch(2, "scripts/import-apple-signing-certificate.sh"),
)
ios_verifier_path = File.expand_path(ARGV.fetch(3, "scripts/verify-ios-release-signing.sh"))

def workflow(path)
  document = YAML.safe_load(File.read(path, encoding: "UTF-8"), aliases: true)
  abort("#{path} must contain a jobs mapping") unless document.is_a?(Hash) && document["jobs"].is_a?(Hash)
  document
end

def shell_commands(document)
  commands = document.fetch("jobs").values.flat_map do |job|
    Array(job["steps"]).each_with_object([]) do |step, result|
      result << step["run"] if step["run"]
    end
  end
  commands.map { |source| source.gsub(/\\\r?\n[ \t]*/, " ") }
end

release = workflow(release_path)
mobile = workflow(mobile_path)
all_commands = shell_commands(release) + shell_commands(mobile)
ios_verifier_source = File.read(ios_verifier_path, encoding: "UTF-8").gsub(/\\\r?\n[ \t]*/, " ")
all_commands << ios_verifier_source

all_commands.each do |source|
  source.each_line do |line|
    if line.match?(/\bsecurity\s+import\b.*(?:^|\s)-P(?:\s|=)/)
      abort("Apple certificate import must not receive a password argument")
    end
    if line.match?(/\bnotarytool\s+store-credentials\b.*(?:^|\s)--password(?:\s|=)/)
      abort("notarytool must not receive a password argument")
    end
  end
end

release_source = File.read(release_path, encoding: "UTF-8")
%w[MACOS_NOTARIZATION_APPLE_ID MACOS_NOTARIZATION_PASSWORD].each do |legacy_name|
  abort("#{release_path} still references retired #{legacy_name}") if release_source.include?(legacy_name)
end

macos_job = release.fetch("jobs").fetch("build-desktop-macos")
macos_steps = macos_job.fetch("steps")
guard_index = macos_steps.index { |step| step["name"] == "Require isolated GitHub-hosted macOS runner" }
import_index = macos_steps.index { |step| step["name"] == "Import macOS signing certificate" }
abort("The macOS signing job is missing its runner guard or import step") unless guard_index && import_index
abort("The runner guard must execute before signing secrets are materialized") unless guard_index < import_index
guard_run = macos_steps.fetch(guard_index).fetch("run", "")
unless guard_run.include?("RUNNER_ENVIRONMENT") && guard_run.include?("github-hosted") &&
       guard_run.include?("exit 1")
  abort("The macOS signing runner guard must fail closed outside GitHub-hosted runners")
end

import_step = macos_steps.fetch(import_index)
expected_secret_env = {
  "MACOS_CERTIFICATE_BASE64" => "${{ secrets.MACOS_CERTIFICATE_BASE64 }}",
  "MACOS_CERTIFICATE_PASSWORD" => "${{ secrets.MACOS_CERTIFICATE_PASSWORD }}",
  "ASC_PRIVATE_KEY_BASE64" => "${{ secrets.ASC_PRIVATE_KEY_BASE64 }}",
}
expected_secret_env.each do |name, expression|
  abort("The macOS import step is missing #{name}") unless import_step.dig("env", name) == expression
end
import_run = import_step.fetch("run", "")
required_fragments = [
  "scripts/import-apple-signing-certificate.sh",
  '--key "$notary_key_path"',
  '--key-id "$ASC_KEY_ID"',
  '--issuer "$ASC_ISSUER_ID"',
  '--keychain "$keychain_path"',
]
required_fragments.each do |fragment|
  abort("The macOS import step is missing #{fragment}") unless import_run.include?(fragment)
end

mobile_source = File.read(mobile_path, encoding: "UTF-8")
unless mobile_source.include?("scripts/import-apple-signing-certificate.sh")
  abort("The iOS signing workflow bypasses the argv-safe certificate importer")
end
unless ios_verifier_source.include?("scripts/import-apple-signing-certificate.sh")
  abort("The local iOS signing verifier bypasses the argv-safe certificate importer")
end

importer = File.read(importer_path, encoding: "UTF-8")
unless importer.include?('-passin fd:3') && importer.include?('/dev/stdin') &&
       importer.include?('-f openssl -x')
  abort("The certificate importer must stream its password and non-extractable private key")
end
if importer.match?(/\bsecurity\s+import\b.*(?:^|\s)-P(?:\s|=)/)
  abort("The certificate importer contains a password argument")
end

puts "Apple signing secrets are absent from security/notarytool arguments."
