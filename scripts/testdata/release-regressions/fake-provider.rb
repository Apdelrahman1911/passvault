# frozen_string_literal: true

# Loaded only by generated fixture shims. No real provider command is forwarded.
require "json"
require "fileutils"

root = File.realpath(ENV.fetch("PASSVAULT_FIXTURE_ROOT"))
state_path = ENV.fetch("PASSVAULT_FAKE_STATE")
abort("Unsafe fixture state") unless File.dirname(File.realpath(state_path)) == root
state = JSON.parse(File.read(state_path))
actual = [File.basename($PROGRAM_NAME), *ARGV]
request = state.fetch("requests").shift
state.fetch("calls") << actual
File.write(state_path, JSON.generate(state))
unless request
  state["violation"] = "Unexpected provider call"
  File.write(state_path, JSON.generate(state))
  abort("Unexpected provider call: #{actual.inspect}")
end
expected = request.fetch("argv")
matches = expected.length == actual.length && expected.zip(actual).all? do |wanted, received|
  if wanted == "ANY_TEMP"
    received.start_with?(root + "/") && !received.split("/").include?("..")
  else
    wanted == received
  end
end
unless matches
  state["violation"] = "Provider arguments differ"
  File.write(state_path, JSON.generate(state))
  abort("Provider call differs: #{actual.inspect}; expected #{expected.inspect}")
end
if request["files"]
  index = ARGV.index("--dir")
  abort("Fake download requires --dir") unless index
  destination = File.expand_path(ARGV.fetch(index + 1))
  abort("Fake download escaped its fixture") unless destination.start_with?(root + "/")
  FileUtils.mkdir_p(destination)
  request.fetch("files").each do |name, content|
    abort("Unsafe fake download filename") unless name.match?(/\A[A-Za-z0-9._+-]+\z/)
    File.write(File.join(destination, name), content)
  end
end
$stdout.write(request.fetch("stdout", ""))
$stderr.write(request.fetch("stderr", ""))
exit(request.fetch("exit", 0))
