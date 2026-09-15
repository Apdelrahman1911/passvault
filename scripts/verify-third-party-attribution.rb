#!/usr/bin/env ruby
# frozen_string_literal: true

require "pathname"
require "set"

def abort_with(message)
  warn "Third-party attribution verification failed: #{message}"
  exit 1
end

def read_lines(path, label)
  abort_with("#{label} is missing: #{path}") unless path.file?
  abort_with("#{label} must not be a symlink: #{path}") if path.symlink?

  text = path.binread
  abort_with("#{label} must be UTF-8 text: #{path}") unless text.force_encoding(Encoding::UTF_8).valid_encoding?
  abort_with("#{label} must use LF line endings: #{path}") if text.include?("\r")

  text.lines(chomp: true)
end

def parse_inventory(path, label)
  entries = []
  read_lines(path, label).each_with_index do |line, index|
    line_number = index + 1
    next if line.empty? || line.start_with?("#")

    fields = line.split("\t", -1)
    abort_with("#{label} line #{line_number} must contain scope and coordinate") unless fields.length == 2

    scope, coordinate = fields
    abort_with("#{label} line #{line_number} has unsupported scope #{scope.inspect}") unless %w[android desktop iosArm64].include?(scope)
    unless coordinate.match?(/\A[^:\s]+:[^:\s]+:[^:\s]+\z/)
      abort_with("#{label} line #{line_number} has invalid coordinate #{coordinate.inspect}")
    end

    entries << [scope, coordinate]
  end

  abort_with("#{label} is empty") if entries.empty?
  abort_with("#{label} contains duplicate entries") unless entries.uniq.length == entries.length
  abort_with("#{label} must be sorted by scope and coordinate") unless entries == entries.sort

  entries
end

def split_values(value, label)
  values = value.split(";", -1)
  abort_with("#{label} contains an empty value") if values.empty? || values.any?(&:empty?)

  values
end

def valid_coordinate_pattern?(pattern)
  fields = pattern.split(":", -1)
  fields.length == 3 && fields.none?(&:empty?) && fields.none? { |field| field.match?(/\s/) } &&
    !fields.fetch(2).match?(/[?*\[]/)
end

Rule = Struct.new(
  :kind,
  :id,
  :notice_project,
  :notice_versions,
  :patterns,
  :license_files,
  :reason
)

def parse_rules(path)
  rules = []
  read_lines(path, "attribution map").each_with_index do |line, index|
    line_number = index + 1
    next if line.empty? || line.start_with?("#")

    fields = line.split("\t", -1)
    abort_with("attribution map line #{line_number} must contain seven tab-separated fields") unless fields.length == 7

    kind, id, notice_project, notice_versions, patterns, license_files, reason = fields
    abort_with("attribution map line #{line_number} has invalid kind #{kind.inspect}") unless %w[artifact native exclude].include?(kind)
    abort_with("attribution map line #{line_number} has invalid id #{id.inspect}") unless id.match?(/\A[a-z0-9][a-z0-9-]*\z/)

    parsed_versions = notice_versions == "-" ? [] : split_values(notice_versions, "notice versions for #{id}")
    parsed_patterns = patterns == "-" ? [] : split_values(patterns, "coordinate patterns for #{id}")
    parsed_licenses = license_files == "-" ? [] : split_values(license_files, "license files for #{id}")

    if kind == "artifact"
      abort_with("artifact rule #{id} needs a notice project and version") if notice_project == "-" || parsed_versions.empty?
      abort_with("artifact rule #{id} needs coordinate patterns") if parsed_patterns.empty?
      abort_with("artifact rule #{id} needs a license mapping") if parsed_licenses.empty?
      abort_with("artifact rule #{id} must use '-' as its reason") unless reason == "-"
    elsif kind == "native"
      abort_with("native rule #{id} needs a notice project and version") if notice_project == "-" || parsed_versions.empty?
      abort_with("native rule #{id} needs a carrier pattern") if parsed_patterns.empty?
      abort_with("native rule #{id} needs a license mapping") if parsed_licenses.empty?
      abort_with("native rule #{id} needs a reason") if reason == "-" || reason.empty?
    else
      abort_with("exclusion #{id} needs a notice marker") if notice_project == "-" || notice_project.empty?
      abort_with("exclusion #{id} must not declare a notice version") unless parsed_versions.empty?
      abort_with("exclusion #{id} needs a carrier or operating-system marker") if parsed_patterns.empty?
      abort_with("exclusion #{id} must not map license files") unless parsed_licenses.empty?
      abort_with("exclusion #{id} needs a reason") if reason == "-" || reason.empty?
    end

    parsed_patterns.each do |pattern|
      next if kind == "exclude" && pattern == "operating-system"
      abort_with("rule #{id} has invalid coordinate pattern #{pattern.inspect}") unless valid_coordinate_pattern?(pattern)
    end

    rules << Rule.new(kind, id, notice_project, parsed_versions, parsed_patterns, parsed_licenses, reason)
  end

  abort_with("attribution map is empty") if rules.empty?
  ids = rules.map(&:id)
  abort_with("attribution map contains duplicate ids") unless ids.uniq.length == ids.length
  expected_order = rules.sort_by { |rule| [%w[artifact native exclude].index(rule.kind), rule.id] }
  abort_with("attribution map must sort artifact, native, and exclusion records by id") unless rules == expected_order

  rules
end

def matches?(pattern, coordinate)
  File.fnmatch?(pattern, coordinate)
end

def notice_rows(path)
  rows = []
  read_lines(path, "third-party notices").each do |line|
    next unless line.start_with?("|") && line.end_with?("|")

    cells = line.split("|", -1)[1...-1].map(&:strip)
    next if cells.length < 3 || cells.all? { |cell| cell.match?(/\A:?-+:?\z/) }

    rows << cells
  end
  rows
end

unless ARGV.length == 5
  warn "Usage: #{$PROGRAM_NAME} <resolved-inventory> <expected-inventory> <attribution-map> <notices> <license-directory>"
  exit 2
end

resolved_path, expected_path, rules_path, notices_path, licenses_path = ARGV.map { |argument| Pathname(argument).expand_path }
resolved = parse_inventory(resolved_path, "resolved inventory")
expected = parse_inventory(expected_path, "expected inventory")

unexpected = resolved - expected
missing = expected - resolved
unless unexpected.empty? && missing.empty?
  unexpected.each { |scope, coordinate| warn "Unexpected resolved dependency: #{scope} #{coordinate}" }
  missing.each { |scope, coordinate| warn "Expected dependency is no longer resolved: #{scope} #{coordinate}" }
  abort_with("resolved production dependencies differ from the reviewed inventory")
end

rules = parse_rules(rules_path)
artifact_rules = rules.select { |rule| rule.kind == "artifact" }
native_rules = rules.select { |rule| rule.kind == "native" }
exclusions = rules.select { |rule| rule.kind == "exclude" }
coordinates = resolved.map(&:last).to_set

coordinates.sort.each do |coordinate|
  matching_rules = artifact_rules.select do |rule|
    rule.patterns.any? { |pattern| matches?(pattern, coordinate) }
  end
  if matching_rules.empty?
    abort_with("resolved dependency has no attribution rule: #{coordinate}")
  elsif matching_rules.length > 1
    abort_with("resolved dependency matches multiple attribution rules: #{coordinate} (#{matching_rules.map(&:id).join(', ')})")
  end
end

artifact_rules.each do |rule|
  next if coordinates.any? { |coordinate| rule.patterns.any? { |pattern| matches?(pattern, coordinate) } }

  abort_with("artifact rule #{rule.id} does not match the resolved inventory")
end

native_rules.each do |rule|
  next if coordinates.any? { |coordinate| rule.patterns.any? { |pattern| matches?(pattern, coordinate) } }

  abort_with("native component #{rule.id} has no resolved carrier dependency")
end

exclusions.each do |rule|
  next if rule.patterns.include?("operating-system")
  next if coordinates.any? { |coordinate| rule.patterns.any? { |pattern| matches?(pattern, coordinate) } }

  abort_with("exclusion #{rule.id} has no resolved carrier dependency")
end

rows = notice_rows(notices_path)
(artifact_rules + native_rules).each do |rule|
  matching_rows = rows.select { |row| row.first == rule.notice_project }
  abort_with("notice row is missing for #{rule.notice_project} (rule #{rule.id})") if matching_rows.empty?
  abort_with("notice row is duplicated for #{rule.notice_project}") if matching_rows.length > 1

  version_cell = matching_rows.first.fetch(2)
  rule.notice_versions.each do |version|
    next if version_cell.include?(version)

    abort_with("notice row #{rule.notice_project} does not contain reviewed version #{version}")
  end
end

notice_text = notices_path.binread.force_encoding(Encoding::UTF_8)
exclusions.each do |rule|
  abort_with("notice text does not document exclusion #{rule.notice_project}") unless notice_text.include?(rule.notice_project)
end

abort_with("third-party license directory is missing: #{licenses_path}") unless licenses_path.directory?
abort_with("third-party license directory must not be a symlink: #{licenses_path}") if licenses_path.symlink?

actual_license_files = licenses_path.children.map do |entry|
  abort_with("third-party license entry must be a regular non-symlinked file: #{entry.basename}") unless entry.file? && !entry.symlink?
  abort_with("third-party license filename is unsafe: #{entry.basename}") unless entry.basename.to_s.match?(/\A[A-Za-z0-9._-]+\z/)

  entry.basename.to_s
end.sort
abort_with("third-party license directory is empty") if actual_license_files.empty?

license_references = Hash.new { |hash, key| hash[key] = [] }
(artifact_rules + native_rules).each do |rule|
  rule.license_files.each do |license_file|
    if license_file == "LICENSE.txt"
      project_license = notices_path.dirname.join("LICENSE.txt")
      abort_with("project Apache license is missing: #{project_license}") unless project_license.file? && !project_license.symlink?
      next
    end
    next if %w[PUBLIC_DOMAIN runtime/legal].include?(license_file)

    abort_with("rule #{rule.id} has an unsafe license filename #{license_file.inspect}") unless license_file.match?(/\A[A-Za-z0-9._-]+\z/)
    license_references[license_file] << rule.id
  end
end

missing_license_files = license_references.keys.sort - actual_license_files
orphaned_license_files = actual_license_files - license_references.keys.sort
missing_license_files.each { |name| warn "Mapped third-party license file is missing: #{name}" }
orphaned_license_files.each { |name| warn "Orphaned third-party license file: #{name}" }
unless missing_license_files.empty? && orphaned_license_files.empty?
  abort_with("third-party license files differ from the reviewed attribution map")
end

duplicate_license_mappings = license_references.select { |_name, owners| owners.length != 1 }
unless duplicate_license_mappings.empty?
  details = duplicate_license_mappings.sort.map { |name, owners| "#{name}=#{owners.join(',')}" }.join("; ")
  abort_with("third-party license files must map to exactly one component: #{details}")
end

puts "Third-party attribution verified (#{resolved.length} scoped artifacts, #{coordinates.length} unique coordinates, " \
     "#{native_rules.length} native components, #{actual_license_files.length} reproduced files)."
