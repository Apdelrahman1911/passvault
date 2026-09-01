#!/usr/bin/env ruby
# frozen_string_literal: true

require "optparse"
require "yaml"

repository_root = File.expand_path("..", __dir__)
options = {
  workflow_directory: File.join(repository_root, ".github", "workflows"),
  configuration_path: File.join(repository_root, "scripts", "configure-github-mobile-release.sh"),
}
OptionParser.new do |parser|
  parser.on("--workflow-directory PATH") { |path| options[:workflow_directory] = File.expand_path(path) }
  parser.on("--configuration PATH") { |path| options[:configuration_path] = File.expand_path(path) }
end.parse!
abort("Unexpected positional arguments") unless ARGV.empty?

workflow_paths = Dir[File.join(options.fetch(:workflow_directory), "*.{yml,yaml}")].sort
abort("No workflow files were inspected") if workflow_paths.empty?

workflows = workflow_paths.to_h do |path|
  document = YAML.safe_load(File.read(path, encoding: "UTF-8"), aliases: true)
  abort("Workflow is not a mapping: #{path}") unless document.is_a?(Hash)

  [path, document]
end

candidate_path = File.join(options.fetch(:workflow_directory), "candidate-readiness.yml")
candidate = workflows.fetch(candidate_path) { abort("Candidate Readiness workflow is missing") }
promotion = candidate.fetch("jobs").fetch("promote-release-branch")
abort("Release branch promotion is not protected by release-promotion") unless
  promotion["environment"] == "release-promotion"

promotion_permissions = promotion.fetch("permissions")
abort("Release branch promotion lost its bounded actions permission") unless
  promotion_permissions["actions"] == "write"
abort("Release branch promotion lost its bounded contents permission") unless
  promotion_permissions["contents"] == "write"

promotion_steps = promotion.fetch("steps")
live_policy_step = promotion_steps.find do |step|
  step["name"] == "Require the live release-promotion approval policy"
end
abort("Release branch promotion does not verify its live approval policy") unless live_policy_step
live_policy_command = live_policy_step.fetch("run", "")
required_live_policy_evidence = [
  "environments/release-promotion",
  "deployment-branch-policies",
  ".can_admins_bypass == false",
  ".prevent_self_review == true",
  '.reviewers[0].reviewer.login == $reviewer',
  '.branch_policies[0].name == "testing"',
]
missing_live_policy_evidence = required_live_policy_evidence.reject do |evidence|
  live_policy_command.include?(evidence)
end
abort("Release branch live-policy verification is incomplete: #{missing_live_policy_evidence.join(', ')}") unless
  missing_live_policy_evidence.empty?

promotion_commands = promotion_steps.map { |step| step["run"] }.compact.join("\n")
abort("Release promotion no longer performs the explicit fast-forward push") unless
  promotion_commands.include?('git push origin "$SOURCE_COMMIT:refs/heads/release"')
abort("Release promotion no longer starts signing validation") unless
  promotion_commands.include?("gh workflow run production-signing-validation.yml")
abort("Release promotion still exposes an automatic production continuation") if
  promotion_commands.include?("start_production_after_validation")

signing_path = File.join(options.fetch(:workflow_directory), "production-signing-validation.yml")
signing = workflows.fetch(signing_path) { abort("Production Signing Validation workflow is missing") }
signing_trigger = signing["on"] || signing[true] || abort("Production Signing Validation trigger is missing")
signing_inputs = signing_trigger.fetch("workflow_dispatch").fetch("inputs", {})
abort("Signing validation still accepts an automatic production-continuation input") if
  signing_inputs.key?("start_production_after_validation")
signing_commands = signing.fetch("jobs").values.flat_map do |job|
  job.fetch("steps", []).map { |step| step["run"].to_s }
end.join("\n")
abort("Signing validation still auto-dispatches production") if
  signing_commands.include?("gh workflow run production-release.yml")

release_push_jobs = []
workflow_run_commands = []
workflows.each do |path, workflow|
  workflow.fetch("jobs", {}).each do |job_name, job|
    job.fetch("steps", []).each do |step|
      command = step["run"].to_s
      next if command.empty?

      release_push_jobs << [path, job_name, job["environment"]] if
        command.include?("git push") && command.include?("refs/heads/release")
      workflow_run_commands << [path, job_name, command] if command.include?("gh workflow run")
    end
  end
end
abort("Expected exactly one release-branch mutation job") unless release_push_jobs.length == 1
push_path, push_job, push_environment = release_push_jobs.first
unless push_path == candidate_path && push_job == "promote-release-branch" &&
       push_environment == "release-promotion"
  abort("Release branch mutation exists outside the protected promotion job")
end

static_start = /(?:^|\s)-f\s+(?:start_[A-Za-z0-9_]*|[A-Za-z0-9_]*_after_[A-Za-z0-9_]*)=true\b/
workflow_run_commands.each do |path, job_name, command|
  next unless command.match?(static_start)

  abort("#{path} job #{job_name} statically auto-starts an authority-changing workflow")
end

configuration = File.read(options.fetch(:configuration_path), encoding: "UTF-8")
unless configuration.scan(/^configure_environment release-promotion true testing$/).length == 1
  abort("Release promotion environment is not configured exactly once for protected testing approval")
end
abort("Environment configuration does not disable administrator bypass in both payloads") unless
  configuration.scan(/"?can_admins_bypass"?: false/).length == 2
required_configuration_evidence = [
  'gh secret delete "$secret_name" --env release-promotion',
  'gh variable delete "$variable_name" --env release-promotion',
  "release_promotion_secrets=",
  "release_promotion_variables=",
  "mobile-external-beta release-promotion mobile-production play-access-production",
  'can_admins_bypass="$(gh api',
  "Administrators cannot bypass deployment protection",
]
missing_evidence = required_configuration_evidence.reject { |evidence| configuration.include?(evidence) }
abort("Release promotion configuration verification is incomplete: #{missing_evidence.join(', ')}") unless
  missing_evidence.empty?

puts "Release authority policy keeps branch promotion reviewed and signing validation non-publishing."
