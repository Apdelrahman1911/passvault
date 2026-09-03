#!/usr/bin/env ruby

require "yaml"

project_path = File.expand_path(
  ARGV.fetch(0, "iosApp/iosApp.xcodeproj/project.pbxproj"),
)
workflow_path = File.expand_path(
  ARGV.fetch(1, ".github/workflows/mobile-store-release.yml"),
)

project = File.read(project_path, encoding: "UTF-8")
phase = project.match(
  %r{/\* Compile Kotlin Framework \*/ = \{(?<body>.*?)^\t\t\};}m,
)&.named_captures&.fetch("body", nil)
abort("#{project_path} is missing the Kotlin framework build phase") unless phase

sandbox_values = project.scan(/ENABLE_USER_SCRIPT_SANDBOXING = ([^;]+);/).flatten
unless sandbox_values == %w[NO NO]
  abort("Debug and Release must retain the Kotlin-required script-sandbox setting")
end
unless phase.include?("alwaysOutOfDate = 1;")
  abort("The Kotlin framework phase must run without dependency-analysis skipping")
end

wrapper_check_index = phase.index("./scripts/verify-gradle-wrapper.sh")
gradle_index = phase.index("./gradlew :shared:embedAndSignAppleFrameworkForXcode")
unless wrapper_check_index && gradle_index && wrapper_check_index < gradle_index
  abort("The Xcode phase must verify the Gradle wrapper before invoking Gradle")
end

workflow = YAML.safe_load(File.read(workflow_path, encoding: "UTF-8"), aliases: true)
abort("#{workflow_path} must contain a jobs mapping") unless workflow.is_a?(Hash) && workflow["jobs"].is_a?(Hash)
ios_steps = workflow.fetch("jobs").fetch("ios").fetch("steps")
expected_action = "gradle/actions/wrapper-validation@9c971963bec38e04b3d30dcc455b5382be2fdbfb"
wrapper_steps = ios_steps.each_index.select do |index|
  ios_steps.fetch(index).fetch("uses", "").start_with?("gradle/actions/wrapper-validation@")
end
unless wrapper_steps.length == 1 && ios_steps.fetch(wrapper_steps.first).fetch("uses") == expected_action
  abort("The iOS archive job must use the pinned Gradle wrapper-validation action exactly once")
end

checkout_index = ios_steps.index do |step|
  step.fetch("uses", "").start_with?("actions/checkout@")
end
archive_index = ios_steps.index do |step|
  step.fetch("run", "").match?(/\bxcodebuild\s+archive\b/)
end
wrapper_index = wrapper_steps.first
unless checkout_index && archive_index && checkout_index < wrapper_index && wrapper_index < archive_index
  abort("The iOS wrapper validation must run after checkout and before the archive")
end

puts "The iOS Kotlin build verifies the pinned Gradle wrapper before execution."
