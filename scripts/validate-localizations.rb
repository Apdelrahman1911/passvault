#!/usr/bin/env ruby
# frozen_string_literal: true

require "rexml/document"
require "set"

ROOT = File.expand_path("..", __dir__)
BASE_PATH = File.join(
  ROOT,
  "core/designsystem/src/commonMain/composeResources/values/strings.xml"
)
ARABIC_PATH = File.join(
  ROOT,
  "core/designsystem/src/commonMain/composeResources/values-ar/strings.xml"
)
ARABIC_QUANTITIES = Set.new(%w[zero one two few many other]).freeze
INTENTIONALLY_LANGUAGE_NEUTRAL = Set.new(
  %w[
    ui_face_id
    ui_bullet_value
    ui_passvault
    ui_percent
    ui_touch_id
    ui_windows_hello
  ]
).freeze
FORMAT_TOKEN = /%(?:\d+\$[a-zA-Z]|%)/
PRODUCTION_SOURCE_ROOTS = %w[app-android app-desktop core feature shared].freeze
FORBIDDEN_UI_LITERAL_PATTERNS = [
  /\bText\(\s*"/,
  /contentDescription\s*=\s*"/,
  /\buiText\(\s*"/,
].freeze
FORBIDDEN_ABSOLUTE_DIRECTION_PATTERNS = [
  /Icons\.(?:Default|Filled|Outlined)\.(?:ArrowBack|ArrowForward|KeyboardArrowLeft|KeyboardArrowRight)/,
  /TextAlign\.(?:Left|Right)/,
  /padding\(\s*(?:left|right)\s*=/,
].freeze

def fail_validation(message)
  warn("Localization validation failed: #{message}")
  exit(1)
end

def document(path)
  fail_validation("missing #{path}") unless File.file?(path)

  REXML::Document.new(File.binread(path))
rescue REXML::ParseException => e
  fail_validation("#{path} is not valid XML: #{e.message}")
end

def resources(path)
  root = document(path).root
  fail_validation("#{path} must have a resources root") unless root&.name == "resources"

  strings = {}
  plurals = {}
  root.elements.each do |element|
    next unless %w[string plurals].include?(element.name)

    name = element.attributes["name"]
    fail_validation("unnamed #{element.name} in #{path}") if name.nil? || name.empty?
    target = element.name == "string" ? strings : plurals
    fail_validation("duplicate resource #{name} in #{path}") if target.key?(name)
    target[name] = if element.name == "string"
                     element.texts.map(&:value).join
                   else
                     element.elements.to_a("item").to_h do |item|
                       [item.attributes["quantity"], item.texts.map(&:value).join]
                     end
                   end
  end
  [strings, plurals]
end

def tokens(value)
  value.scan(FORMAT_TOKEN).sort
end

base_strings, base_plurals = resources(BASE_PATH)
arabic_strings, arabic_plurals = resources(ARABIC_PATH)

missing_strings = base_strings.keys - arabic_strings.keys
extra_strings = arabic_strings.keys - base_strings.keys
missing_plurals = base_plurals.keys - arabic_plurals.keys
extra_plurals = arabic_plurals.keys - base_plurals.keys
fail_validation("missing Arabic strings: #{missing_strings.sort.join(', ')}") unless missing_strings.empty?
fail_validation("unknown Arabic strings: #{extra_strings.sort.join(', ')}") unless extra_strings.empty?
fail_validation("missing Arabic plurals: #{missing_plurals.sort.join(', ')}") unless missing_plurals.empty?
fail_validation("unknown Arabic plurals: #{extra_plurals.sort.join(', ')}") unless extra_plurals.empty?

base_strings.each do |name, english|
  arabic = arabic_strings.fetch(name)
  fail_validation("Arabic string #{name} is blank") if arabic.strip.empty?
  unless tokens(english) == tokens(arabic)
    fail_validation("placeholder mismatch for #{name}: #{tokens(english)} != #{tokens(arabic)}")
  end
  if english == arabic && !INTENTIONALLY_LANGUAGE_NEUTRAL.include?(name)
    fail_validation("#{name} is unchanged but is not explicitly allowlisted")
  end
end

base_plurals.each do |name, english_items|
  arabic_items = arabic_plurals.fetch(name)
  quantities = Set.new(arabic_items.keys)
  unless quantities == ARABIC_QUANTITIES
    fail_validation("#{name} must define Arabic zero/one/two/few/many/other forms")
  end
  expected_tokens = tokens(english_items.fetch("other"))
  arabic_items.each do |quantity, value|
    fail_validation("Arabic plural #{name}/#{quantity} is blank") if value.strip.empty?
    unless tokens(value) == expected_tokens
      fail_validation(
        "placeholder mismatch for #{name}/#{quantity}: #{expected_tokens} != #{tokens(value)}"
      )
    end
  end
end

production_sources = PRODUCTION_SOURCE_ROOTS.flat_map do |source_root|
  Dir.glob(File.join(ROOT, source_root, "**", "*.kt"))
end.reject { |path| path.include?("/build/") || path.end_with?("Test.kt") }

production_sources.each do |path|
  source = File.binread(path)
  FORBIDDEN_UI_LITERAL_PATTERNS.each do |pattern|
    next unless source.match?(pattern)

    fail_validation("unlocalized user-facing literal matching #{pattern.inspect} in #{path.delete_prefix(ROOT + '/')}")
  end
  FORBIDDEN_ABSOLUTE_DIRECTION_PATTERNS.each do |pattern|
    next unless source.match?(pattern)

    fail_validation("non-RTL-aware UI direction matching #{pattern.inspect} in #{path.delete_prefix(ROOT + '/')}")
  end
end

android_manifest = File.binread(File.join(ROOT, "app-android/src/main/AndroidManifest.xml"))
fail_validation("Android manifest must declare supportsRtl=true") unless android_manifest.include?(
  'android:supportsRtl="true"'
)
ios_project = File.binread(File.join(ROOT, "iosApp/iosApp.xcodeproj/project.pbxproj"))
fail_validation("the iOS project must declare Arabic as a known region") unless ios_project.match?(
  /knownRegions\s*=\s*\([^)]*\bar\b/m
)

puts(
  "Validated complete Arabic localization: " \
  "#{arabic_strings.size} strings and #{arabic_plurals.size} plurals."
)
