#!/usr/bin/env ruby

require "base64"
require "json"
require "net/http"
require "openssl"
require "optparse"
require "uri"
require_relative "lib/app_store_configuration"
require_relative "lib/dotenv"
require_relative "lib/private_path"

options = {}
OptionParser.new do |parser|
  parser.banner = "Usage: #{File.basename($PROGRAM_NAME)} [options]"
  parser.on("--status", "Read TestFlight status without changing it") { options[:status] = true }
  parser.on("--version VERSION", "Marketing version") { |value| options[:version] = value }
  parser.on("--build-number NUMBER", "App Store build number") { |value| options[:build] = value }
end.parse!

canonical_version = /\A(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)\z/
valid_build = options[:build]&.match?(/\A[1-9]\d*\z/) &&
  options[:build].length <= 10 && options[:build].to_i <= 2_100_000_000
unless options[:version]&.match?(canonical_version) && valid_build
  warn "A canonical semantic --version and --build-number from 1 through 2100000000 are required."
  exit 2
end
repository_root = File.expand_path("..", __dir__)
private_root = File.join(repository_root, "release", "private")
environment_source = ENV["PASSVAULT_ASC_CONFIGURATION_SOURCE"] == "environment"
values = {}

if environment_source
  %w[
    ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
    TESTFLIGHT_EXTERNAL_GROUP EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
  ].each { |name| values[name] = ENV[name].to_s }
  runtime_root = ENV["PRIVATE_RUNTIME"].to_s
  if runtime_root.empty?
    warn "The private runtime path is missing."
    exit 1
  end
  private_root = File.expand_path(runtime_root)
else
  values_path = File.join(private_root, "values.env")
  unless File.file?(values_path) && !File.symlink?(values_path)
    warn "The ignored private values file is missing or unsafe."
    exit 1
  end
  values = PassVault::Dotenv.load(values_path)
end

required = %w[
  ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
  TESTFLIGHT_EXTERNAL_GROUP EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
]
if required.any? { |name| values[name].to_s.empty? }
  warn "TestFlight status inputs are incomplete."
  exit 1
end
unless PassVault::AppStoreConfiguration.identifiers_valid?(values)
  warn "TestFlight status identifiers are malformed or do not identify PassVault."
  exit 1
end
unless PassVault::AppStoreConfiguration.external_group_name_valid?(
  values.fetch("TESTFLIGHT_EXTERNAL_GROUP"),
)
  warn "The external TestFlight group name must be a trimmed printable line of at most 100 characters."
  exit 1
end
case values.fetch("EXPORT_COMPLIANCE_STATUS")
when "EXEMPT_APPROVED"
  if values.fetch("IOS_FRANCE_AVAILABLE") != "false"
    warn "EXEMPT_APPROVED requires France to remain excluded."
    exit 1
  end
when "NON_EXEMPT_APPROVED"
  unless %w[true false].include?(values.fetch("IOS_FRANCE_AVAILABLE"))
    warn "IOS_FRANCE_AVAILABLE must be exactly true or false."
    exit 1
  end
else
  warn "An approved export-compliance status is required."
  exit 1
end

key_path = File.expand_path(values.fetch("ASC_PRIVATE_KEY_FILE"), repository_root)
unless PassVault::PrivatePath.regular_file_within?(key_path, private_root)
  warn "The App Store Connect private-key path is unsafe or missing."
  exit 1
end

def base64url(value)
  Base64.urlsafe_encode64(value, padding: false)
end

def jose_signature(der_signature)
  integers = OpenSSL::ASN1.decode(der_signature).value.map(&:value)
  raise "Unexpected ECDSA signature" unless integers.length == 2

  integers.map { |integer| [integer.to_s(16).rjust(64, "0")].pack("H*") }.join
end

def token_for(values, key_path)
  private_key = OpenSSL::PKey.read(File.binread(key_path))
  unless private_key.is_a?(OpenSSL::PKey::EC) && private_key.private? &&
         private_key.group.curve_name == "prime256v1" && private_key.check_key
    raise "Invalid App Store Connect key"
  end
  now = Time.now.to_i
  header = { alg: "ES256", kid: values.fetch("ASC_KEY_ID"), typ: "JWT" }
  payload = {
    iss: values.fetch("ASC_ISSUER_ID"),
    iat: now,
    exp: now + 600,
    aud: "appstoreconnect-v1",
  }
  signing_input = [base64url(JSON.generate(header)), base64url(JSON.generate(payload))].join(".")
  signature = jose_signature(private_key.sign("SHA256", signing_input))
  [signing_input, base64url(signature)].join(".")
ensure
  private_key = nil
  signing_input = nil
  signature = nil
end

def request(token, path, query: nil, allow_not_found: false)
  uri = URI("https://api.appstoreconnect.apple.com#{path}")
  uri.query = URI.encode_www_form(query) if query
  http_request = Net::HTTP::Get.new(uri)
  http_request["Authorization"] = "Bearer #{token}"
  response = Net::HTTP.start(
    uri.host,
    uri.port,
    use_ssl: true,
    open_timeout: 15,
    read_timeout: 45,
  ) { |http| http.request(http_request) }
  return nil if allow_not_found && response.code == "404"

  unless response.code == "200"
    error_code = begin
      JSON.parse(response.body).fetch("errors", []).first&.fetch("code", nil)
    rescue JSON::ParserError
      nil
    end
    suffix = error_code ? " (#{error_code})" : ""
    raise "App Store Connect HTTP #{response.code}#{suffix}"
  end
  response.body.to_s.empty? ? {} : JSON.parse(response.body)
end

def presence(value)
  value.is_a?(String) && !value.strip.empty?
end

begin
  token = token_for(values, key_path)
  app_response = request(
    token,
    "/v1/apps",
    query: { "filter[bundleId]" => values.fetch("IOS_BUNDLE_ID"), "limit" => "10" },
  )
  app = app_response.fetch("data", []).find do |candidate|
    candidate["id"] == values.fetch("APP_STORE_APP_ID") &&
      candidate.dig("attributes", "bundleId") == values.fetch("IOS_BUNDLE_ID")
  end
  raise "Configured app identifiers do not match" unless app
  app_id = app.fetch("id")

  groups = request(
    token,
    "/v1/betaGroups",
    query: {
      "filter[app]" => app_id,
      "filter[name]" => values.fetch("TESTFLIGHT_EXTERNAL_GROUP"),
      "fields[betaGroups]" =>
        "name,isInternalGroup,publicLinkEnabled,publicLinkLimitEnabled,publicLinkLimit,publicLink",
      "limit" => "20",
    },
  ).fetch("data", [])
  matching_groups = groups.select do |candidate|
    candidate.dig("attributes", "name") == values.fetch("TESTFLIGHT_EXTERNAL_GROUP")
  end
  raise "The configured external TestFlight group name is ambiguous" if matching_groups.length > 1
  group = matching_groups.first
  raise "The configured external TestFlight group does not exist" unless group
  raise "The configured TestFlight group is internal" if group.dig("attributes", "isInternalGroup") == true
  group_id = group.fetch("id")

  prerelease_versions = request(
    token,
    "/v1/preReleaseVersions",
    query: {
      "filter[app]" => app_id,
      "filter[platform]" => "IOS",
      "filter[version]" => options.fetch(:version),
      "fields[preReleaseVersions]" => "version,platform",
      "limit" => "10",
    },
  ).fetch("data", [])
  matching_prereleases = prerelease_versions.select do |candidate|
    candidate.dig("attributes", "version") == options.fetch(:version) &&
      candidate.dig("attributes", "platform") == "IOS"
  end
  raise "The requested App Store prerelease version is ambiguous" if matching_prereleases.length > 1
  prerelease = matching_prereleases.first
  builds = if prerelease
             request(
               token,
               "/v1/preReleaseVersions/#{prerelease.fetch('id')}/builds",
               query: {
                 "fields[builds]" =>
                   "version,uploadedDate,expirationDate,expired,processingState,usesNonExemptEncryption,buildAudienceType",
                 "limit" => "200",
               },
             ).fetch("data", [])
           else
             []
           end
  matching_builds = builds.select do |candidate|
    candidate.dig("attributes", "version") == options.fetch(:build)
  end
  raise "The requested App Store build number is ambiguous" if matching_builds.length > 1
  build = matching_builds.first
  latest_build = builds.map { |candidate| candidate.dig("attributes", "version") }
    .compact.select { |value| value.match?(/\A\d+\z/) }.max_by(&:to_i)

  review_detail = request(
    token,
    "/v1/apps/#{app_id}/betaAppReviewDetail",
    query: {
      "fields[betaAppReviewDetails]" =>
        "contactFirstName,contactLastName,contactPhone,contactEmail,demoAccountRequired,notes",
    },
    allow_not_found: true,
  )&.fetch("data", nil)
  review_attributes = review_detail&.fetch("attributes", {}) || {}
  review_complete = %w[contactFirstName contactLastName contactPhone contactEmail notes].all? do |name|
    presence(review_attributes[name])
  end && review_attributes["demoAccountRequired"] == false

  app_localizations = request(
    token,
    "/v1/apps/#{app_id}/betaAppLocalizations",
    query: {
      "fields[betaAppLocalizations]" =>
        "feedbackEmail,marketingUrl,privacyPolicyUrl,description,locale",
      "limit" => "50",
    },
  ).fetch("data", [])
  duplicate_app_locales = app_localizations.group_by { |item| item.dig("attributes", "locale") }
    .select { |locale, items| locale && items.length > 1 }.keys
  unless duplicate_app_locales.empty?
    raise "Duplicate beta app localizations: #{duplicate_app_locales.sort.join(', ')}"
  end
  en_localization = app_localizations.find { |item| item.dig("attributes", "locale") == "en-US" }
  en_attributes = en_localization&.fetch("attributes", {}) || {}
  en_complete = %w[feedbackEmail marketingUrl privacyPolicyUrl description].all? do |name|
    presence(en_attributes[name])
  end
  required_locales = %w[en-US ar-SA]
  beta_localizations_complete = required_locales.all? do |locale|
    localization = app_localizations.find { |item| item.dig("attributes", "locale") == locale }
    attributes = localization&.fetch("attributes", {}) || {}
    %w[feedbackEmail marketingUrl privacyPolicyUrl description].all? do |name|
      presence(attributes[name])
    end
  end

  group_has_build = false
  beta_detail_attributes = {}
  beta_review_attributes = {}
  build_localizations = []
  if build
    build_id = build.fetch("id")
    group_builds = request(
      token,
      "/v1/betaGroups/#{group_id}/builds",
      query: { "fields[builds]" => "version", "limit" => "200" },
    ).fetch("data", [])
    group_has_build = group_builds.any? { |candidate| candidate["id"] == build_id }
    beta_detail = request(
      token,
      "/v1/builds/#{build_id}/buildBetaDetail",
      query: { "fields[buildBetaDetails]" => "autoNotifyEnabled,internalBuildState,externalBuildState" },
      allow_not_found: true,
    )
    beta_detail_attributes = beta_detail&.dig("data", "attributes") || {}
    beta_review = request(
      token,
      "/v1/builds/#{build_id}/betaAppReviewSubmission",
      query: { "fields[betaAppReviewSubmissions]" => "betaReviewState,submittedDate" },
      allow_not_found: true,
    )
    beta_review_attributes = beta_review&.dig("data", "attributes") || {}
    build_localizations = request(
      token,
      "/v1/builds/#{build_id}/betaBuildLocalizations",
      query: { "fields[betaBuildLocalizations]" => "whatsNew,locale", "limit" => "50" },
    ).fetch("data", [])
    duplicate_build_locales = build_localizations.group_by { |item| item.dig("attributes", "locale") }
      .select { |locale, items| locale && items.length > 1 }.keys
    unless duplicate_build_locales.empty?
      raise "Duplicate beta build localizations: #{duplicate_build_locales.sort.join(', ')}"
    end
  end

  attributes = group.fetch("attributes")
  puts "APP_STORE_APP_ID=#{app_id}"
  puts "APP_BUNDLE_ID=#{values.fetch('IOS_BUNDLE_ID')}"
  puts "EXTERNAL_GROUP_NAME=#{attributes.fetch('name')}"
  puts "EXTERNAL_GROUP_ID=#{group_id}"
  puts "PUBLIC_LINK_ENABLED=#{attributes['publicLinkEnabled'] == true}"
  puts "PUBLIC_LINK_LIMIT=#{attributes['publicLinkLimitEnabled'] == true ? attributes['publicLinkLimit'] : 'DISABLED'}"
  puts "PUBLIC_LINK_PRESENT=#{presence(attributes['publicLink'])}"
  puts "REVIEW_INFORMATION=#{review_complete ? 'COMPLETE' : 'INCOMPLETE'}"
  puts "ENGLISH_TEST_INFORMATION=#{en_complete ? 'COMPLETE' : 'INCOMPLETE'}"
  puts "REQUIRED_TEST_INFORMATION=#{beta_localizations_complete ? 'COMPLETE' : 'INCOMPLETE'}"
  puts "BETA_APP_LOCALIZATIONS=#{app_localizations.map { |item| item.dig('attributes', 'locale') }.compact.sort.join(',')}"
  puts "LATEST_BUILD_NUMBER=#{latest_build || 'NONE'}"
  if build
    build_attributes = build.fetch("attributes")
    puts "BUILD_ID=#{build.fetch('id')}"
    puts "MARKETING_VERSION=#{options.fetch(:version)}"
    puts "BUILD_NUMBER=#{build_attributes.fetch('version')}"
    puts "PROCESSING_STATE=#{build_attributes.fetch('processingState')}"
    puts "USES_NON_EXEMPT_ENCRYPTION=#{build_attributes['usesNonExemptEncryption']}"
    puts "GROUP_HAS_BUILD=#{group_has_build}"
    puts "AUTO_NOTIFY_ENABLED=#{beta_detail_attributes.fetch('autoNotifyEnabled', 'UNKNOWN')}"
    puts "INTERNAL_BUILD_STATE=#{beta_detail_attributes.fetch('internalBuildState', 'UNKNOWN')}"
    puts "EXTERNAL_BUILD_STATE=#{beta_detail_attributes.fetch('externalBuildState', 'UNKNOWN')}"
    puts "BETA_REVIEW_STATE=#{beta_review_attributes.fetch('betaReviewState', 'NOT_SUBMITTED')}"
    puts "BETA_REVIEW_SUBMITTED_DATE=#{beta_review_attributes.fetch('submittedDate', 'NONE')}"
    required_build_localizations_complete = required_locales.all? do |locale|
      localization = build_localizations.find { |item| item.dig("attributes", "locale") == locale }
      presence(localization&.dig("attributes", "whatsNew"))
    end
    puts "REQUIRED_WHAT_TO_TEST=#{required_build_localizations_complete ? 'COMPLETE' : 'INCOMPLETE'}"
    puts "WHAT_TO_TEST_LOCALIZATIONS=#{build_localizations.map { |item| item.dig('attributes', 'locale') }.compact.sort.join(',')}"
  else
    puts "BUILD_ID=NOT_FOUND"
    puts "MARKETING_VERSION=#{options.fetch(:version)}"
    puts "BUILD_NUMBER=#{options.fetch(:build)}"
    puts "PROCESSING_STATE=NOT_FOUND"
    puts "BETA_REVIEW_STATE=NOT_SUBMITTED"
  end
rescue OpenSSL::PKey::PKeyError, OpenSSL::ASN1::ASN1Error, JSON::ParserError, KeyError,
       RuntimeError, SocketError, SystemCallError, Timeout::Error => error
  warn "TestFlight operation failed: #{error.message}."
  exit 1
ensure
  token = nil
end
