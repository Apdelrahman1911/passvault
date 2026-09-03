#!/usr/bin/env ruby

require "yaml"

path = File.expand_path(ARGV.fetch(0, ".github/workflows/ci.yml"))
workflow = YAML.safe_load(File.read(path, encoding: "UTF-8"), aliases: false)
abort("CI workflow must be a mapping") unless workflow.is_a?(Hash)

read_only_permissions = { "contents" => "read" }
unless workflow.fetch("permissions", {}) == read_only_permissions
  abort("CI workflow permissions must be exactly contents: read")
end

jobs = workflow.fetch("jobs")
abort("CI jobs must be a mapping") unless jobs.is_a?(Hash)

jobs.each do |job_name, job|
  permissions = job.fetch("permissions", workflow.fetch("permissions"))
  unless permissions.is_a?(Hash) && permissions.values.none? { |access| access == "write" }
    abort("CI job #{job_name} requests a write permission")
  end

  job.fetch("steps", []).each do |step|
    action = step["uses"].to_s
    if action.start_with?("actions/checkout@") &&
       step.fetch("with", {})["persist-credentials"] != false
      abort("CI job #{job_name} persists checkout credentials")
    end
    if action.start_with?("dorny/test-reporter@")
      abort("CI must not publish PR-controlled reports with a check-writing action")
    end
  end
end

test_job = jobs.fetch("test")
unless test_job.fetch("permissions", {}) == read_only_permissions
  abort("CI test job permissions must be exactly contents: read")
end

test_steps = test_job.fetch("steps")
security_analysis_index = test_steps.index do |step|
  step["run"].to_s.strip == "./scripts/run-security-analysis.sh"
end
abort("CI test job must run the repository security-analysis gate") unless security_analysis_index
security_analysis_step = test_steps.fetch(security_analysis_index)
if security_analysis_step["continue-on-error"] || security_analysis_step["if"]
  abort("CI security analysis must run unconditionally and fail closed")
end
unit_test_index = test_steps.index { |step| step["name"] == "Run Unit Tests" }
unless unit_test_index && security_analysis_index < unit_test_index
  abort("CI security analysis must run before the Gradle test batch")
end

report_upload = test_steps.find do |step|
  step["uses"].to_s.start_with?("actions/upload-artifact@") &&
    step.dig("with", "name") == "test-reports"
end
abort("CI test reports must remain available as an artifact") unless report_upload
unless report_upload["if"] == "always()"
  abort("CI test reports must be uploaded on success and failure")
end
report_paths = report_upload.dig("with", "path").to_s
unless report_paths.include?("**/build/reports/tests/**") &&
       report_paths.include?("**/build/test-results/**")
  abort("CI test report artifact is missing HTML or JUnit results")
end

ci_gate = jobs.fetch("ci-gate")
unless Array(ci_gate.fetch("needs")).include?("test") && ci_gate["if"] == "always()"
  abort("CI Gate must always incorporate the test job result")
end
gate_script = ci_gate.fetch("steps").map { |step| step["run"].to_s }.join("\n")
unless gate_script.include?("TEST_RESULT") && gate_script.include?("!= success")
  abort("CI Gate does not fail closed on a failed test job")
end

puts "CI pull-request jobs use read-only, non-persisted repository credentials."
