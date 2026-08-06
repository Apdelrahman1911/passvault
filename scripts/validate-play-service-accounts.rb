#!/usr/bin/env ruby

require "csv"
require "optparse"
require "time"

STRICT_MODE = "STRICT_LEAST_PRIVILEGE"
APP_ADMIN_MODE = "PASSVAULT_APP_ADMIN_ACCEPTED"

STRICT_PERMISSIONS = {
  "beta" => %w[
    CAN_MANAGE_TRACK_APKS
    CAN_VIEW_NON_FINANCIAL_DATA
  ],
  "production" => %w[
    CAN_MANAGE_PUBLIC_APKS
    CAN_MANAGE_PUBLIC_LISTING
    CAN_VIEW_NON_FINANCIAL_DATA
  ],
}.transform_values(&:sort).freeze

APP_ADMIN_PERMISSIONS = %w[
  CAN_MANAGE_APP_CONTENT
  CAN_MANAGE_DEEPLINKS
  CAN_MANAGE_DRAFT_APPS
  CAN_MANAGE_ORDERS
  CAN_MANAGE_PERMISSIONS
  CAN_MANAGE_PUBLIC_APKS
  CAN_MANAGE_PUBLIC_LISTING
  CAN_MANAGE_TRACK_APKS
  CAN_MANAGE_TRACK_USERS
  CAN_REPLY_TO_REVIEWS
  CAN_VIEW_APP_QUALITY
  CAN_VIEW_FINANCIAL_DATA
  CAN_VIEW_NON_FINANCIAL_DATA
].sort.freeze

options = {
  mode: STRICT_MODE,
}

OptionParser.new do |parser|
  parser.on("--csv PATH") { |value| options[:csv] = value }
  parser.on("--package PACKAGE") { |value| options[:package] = value }
  parser.on("--beta-email EMAIL") { |value| options[:beta_email] = value }
  parser.on("--production-email EMAIL") { |value| options[:production_email] = value }
  parser.on("--mode MODE") { |value| options[:mode] = value }
end.parse!

required_options = %i[csv package beta_email production_email]
missing_options = required_options.select { |name| options[name].to_s.strip.empty? }
abort "Missing required Play validation options: #{missing_options.join(', ')}" unless missing_options.empty?

unless [STRICT_MODE, APP_ADMIN_MODE].include?(options[:mode])
  abort "Unsupported Play permission mode: #{options[:mode]}"
end

def parse_app_permissions(value)
  text = value.to_s.strip
  return {} if text.empty?

  matches = text.scan(/\{([^{}:]+):([^{}]*)\}/)
  remainder = text.gsub(/\{[^{}:]+:[^{}]*\}/, "").gsub(";", "").strip
  raise "Malformed app-permission field" if matches.empty? || !remainder.empty?

  matches.to_h do |package_name, permission_text|
    permissions = permission_text.split(";").map(&:strip).reject(&:empty?).uniq.sort
    [package_name.strip, permissions]
  end
end

def parse_account_permissions(value)
  value.to_s.split(";").map(&:strip).reject(&:empty?).uniq.sort
end

table = CSV.read(options[:csv], headers: true, encoding: "bom|utf-8")
required_headers = ["Email", "Status", "Expiration time", "App permissions", "Account permissions"]
missing_headers = required_headers - table.headers
abort "Play user export is missing columns: #{missing_headers.join(', ')}" unless missing_headers.empty?

targets = {
  "beta" => options[:beta_email].downcase,
  "production" => options[:production_email].downcase,
}
errors = []
summaries = []

targets.each do |role, email|
  matching_rows = table.select { |row| row["Email"].to_s.strip.downcase == email }
  if matching_rows.length != 1
    errors << "#{role}: expected exactly one target row, found #{matching_rows.length}"
    next
  end

  row = matching_rows.first
  status = row["Status"].to_s.strip
  errors << "#{role}: access status is #{status.empty? ? 'missing' : status}" unless status == "ACCESS_GRANTED"

  expiration = row["Expiration time"].to_s.strip
  unless expiration.empty?
    begin
      errors << "#{role}: access is expired" if Time.parse(expiration) <= Time.now
    rescue ArgumentError
      errors << "#{role}: expiration time is invalid"
    end
  end

  account_permissions = parse_account_permissions(row["Account permissions"])
  unless account_permissions.empty?
    errors << "#{role}: account/global permissions are forbidden: #{account_permissions.join(',')}"
  end

  begin
    app_permissions = parse_app_permissions(row["App permissions"])
  rescue StandardError => error
    errors << "#{role}: #{error.message}"
    next
  end

  packages = app_permissions.keys.sort
  unless packages == [options[:package]]
    errors << "#{role}: app access must be limited to #{options[:package]}; found #{packages.join(',')}"
    next
  end

  expected_permissions = if options[:mode] == APP_ADMIN_MODE
                           APP_ADMIN_PERMISSIONS
                         else
                           STRICT_PERMISSIONS.fetch(role)
                         end
  actual_permissions = app_permissions.fetch(options[:package])
  unless actual_permissions == expected_permissions
    missing = expected_permissions - actual_permissions
    extra = actual_permissions - expected_permissions
    errors << "#{role}: app permissions differ; missing=#{missing.join(',')}; extra=#{extra.join(',')}"
  end

  summaries << "#{role.upcase}_ACCESS=ACCESS_GRANTED"
  summaries << "#{role.upcase}_PACKAGE=#{options[:package]}"
  summaries << "#{role.upcase}_ACCOUNT_PERMISSIONS=NONE"
  summaries << "#{role.upcase}_APP_PERMISSION_COUNT=#{actual_permissions.length}"
end

unless errors.empty?
  warn errors.join("\n")
  exit 1
end

puts "PLAY_SERVICE_ACCOUNT_VALIDATION=PASS"
puts "PLAY_PERMISSION_MODE=#{options[:mode]}"
puts summaries
if options[:mode] == APP_ADMIN_MODE
  puts "PLAY_SECURITY_EXCEPTION=APP_LEVEL_ADMIN_EXPLICITLY_ACCEPTED"
end
