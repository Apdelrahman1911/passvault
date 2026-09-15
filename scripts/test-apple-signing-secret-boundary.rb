#!/usr/bin/env ruby
# Static boundary regressions only: no key material, signing or native execution.
require "tmpdir"
require "open3"
require "rbconfig"

root = File.expand_path("..", __dir__)
importer = File.read(File.join(__dir__, "import-apple-signing-certificate.sh"))
helper = File.read(File.join(__dir__, "import-apple-key-from-stdin.py"))
cases = [
  ["buffered importer accepted", importer, helper, true],
  ["password fd required", importer.sub("-passin fd:3", "-passin pass:fixture"), helper, false],
  ["buffered helper required", importer.sub("import-apple-key-from-stdin.py", "other-helper.py"), helper, false],
  ["non-extractability required", importer, helper.sub('"-x",', ""), false],
  ["password argv refused", importer, helper.sub('"-x",', '"-x", "-P", "fixture",'), false],
  ["additional trusted tool refused", importer, helper.sub('"-T", "/usr/bin/codesign",', '"-T", "/usr/bin/codesign", "-T", "/bin/sh",'), false],
  ["stdin descriptor required", importer, helper.sub("stdin=reader", "stdin=None"), false],
]
Dir.mktmpdir("passvault-signing-boundary.") do |temporary|
  importer_path = File.join(temporary, "importer.sh")
  helper_path = File.join(temporary, "helper.py")
  cases.each do |name, shell_source, helper_source, expected|
    if !expected && shell_source == importer && helper_source == helper
      abort("Fixture mutation not applied: #{name}")
    end
    File.write(importer_path, shell_source)
    File.write(helper_path, helper_source)
    stdout, stderr, status = Open3.capture3(
      RbConfig.ruby, File.join(__dir__, "validate-apple-signing-secret-boundary.rb"),
      File.join(root, ".github/workflows/release.yml"),
      File.join(root, ".github/workflows/mobile-store-release.yml"),
      importer_path, File.join(__dir__, "verify-ios-release-signing.sh"), helper_path,
    )
    abort("FAIL #{name}: #{stdout}#{stderr}") unless status.success? == expected
    puts "PASS #{name}"
  end
end
puts "#{cases.length} static Apple signing boundary cases passed; temporary fixtures removed."
