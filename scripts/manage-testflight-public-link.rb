#!/usr/bin/env ruby

require "base64"
require "json"
require "net/http"
require "openssl"
require "optparse"
require "uri"

options = {
  action: "status",
  limit: 250,
}
OptionParser.new do |parser|
  parser.banner = "Usage: #{File.basename($PROGRAM_NAME)} [options]"
  parser.on("--status", "Read TestFlight status without changing it") { options[:action] = "status" }
  parser.on("--enable-public-link", "Enable the public link after beta approval") do
    options[:action] = "enable"
  end
  parser.on("--version VERSION", "Marketing version") { |value| options[:version] = value }
  parser.on("--build-number NUMBER", "App Store build number") { |value| options[:build] = value }
  parser.on("--limit NUMBER", Integer, "Public-link tester limit (1-10,000)") do |value|
    options[:limit] = value
  end
end.parse!

unless options[:version]&.match?(/\A\d+\.\d+\.\d+\z/) && options[:build]&.match?(/\A[1-9]\d*\z/)
  warn "A semantic --version and positive --build-number are required."
  exit 2
end
unless (1..10_000).cover?(options[:limit])
  warn "The public-link tester limit must be between 1 and 10,000."
  exit 2
end

repository_root = File.expand_path("..", __dir__)
private_root = File.join(repository_root, "release", "private")
environment_source = ENV["PASSVAULT_ASC_CONFIGURATION_SOURCE"] == "environment"
values = {}

def parse_dotenv(path)
  values = {}
  File.foreach(path, chomp: true) do |line|
    line = line.delete_suffix("\r")
    next if line.strip.empty? || line.lstrip.start_with?("#")

    key, value = line.split("=", 2)
    raise "Invalid dotenv entry" unless key&.match?(/\A[A-Z][A-Z0-9_]*\z/) && value

    if value.length >= 2 && ((value.start_with?("\"") && value.end_with?("\"")) ||
       (value.start_with?("'") && value.end_with?("'")))
      value = value[1...-1]
    end
    values[key] = value
  end
  values
end

if environment_source
  %w[
    ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
    TESTFLIGHT_EXTERNAL_GROUP EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
  ].each { |name| values[name] = ENV[name].to_s }
  private_root = File.expand_path(ENV.fetch("PRIVATE_RUNTIME", ""))
else
  values_path = File.join(private_root, "values.env")
  unless File.file?(values_path) && !File.symlink?(values_path)
    warn "The ignored private values file is missing or unsafe."
    exit 1
  end
  values = parse_dotenv(values_path)
end

required = %w[
  ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
  TESTFLIGHT_EXTERNAL_GROUP EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
]
if required.any? { |name| values[name].to_s.empty? }
  warn "TestFlight status inputs are incomplete."
  exit 1
end
unless values.fetch("EXPORT_COMPLIANCE_STATUS") == "EXEMPT_APPROVED" &&
       values.fetch("IOS_FRANCE_AVAILABLE") == "false"
  warn "The approved no-documentation, non-France release constraint is required."
  exit 1
end

key_path = File.expand_path(values.fetch("ASC_PRIVATE_KEY_FILE"), repository_root)
unless !private_root.empty? && key_path.start_with?(private_root + File::SEPARATOR) &&
       File.file?(key_path) && !File.symlink?(key_path)
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

def request(token, method, path, query: nil, payload: nil, allow_not_found: false)
  uri = URI("https://api.appstoreconnect.apple.com#{path}")
  uri.query = URI.encode_www_form(query) if query
  request_class = {
    get: Net::HTTP::Get,
    post: Net::HTTP::Post,
    patch: Net::HTTP::Patch,
  }.fetch(method)
  http_request = request_class.new(uri)
  http_request["Authorization"] = "Bearer #{token}"
  if payload
    http_request["Content-Type"] = "application/json"
    http_request.body = JSON.generate(payload)
  end
  response = Net::HTTP.start(
    uri.host,
    uri.port,
    use_ssl: true,
    open_timeout: 15,
    read_timeout: 45,
  ) { |http| http.request(http_request) }
  return nil if allow_not_found && response.code == "404"

  expected = method == :post ? %w[200 201 204] : %w[200 201 204]
  unless expected.include?(response.code)
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
    :get,
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
    :get,
    "/v1/betaGroups",
    query: {
      "filter[app]" => app_id,
      "filter[name]" => values.fetch("TESTFLIGHT_EXTERNAL_GROUP"),
      "fields[betaGroups]" =>
        "name,isInternalGroup,publicLinkEnabled,publicLinkLimitEnabled,publicLinkLimit,publicLink",
      "limit" => "20",
    },
  ).fetch("data", [])
  group = groups.find do |candidate|
    candidate.dig("attributes", "name") == values.fetch("TESTFLIGHT_EXTERNAL_GROUP")
  end
  raise "The configured external TestFlight group does not exist" unless group
  raise "The configured TestFlight group is internal" if group.dig("attributes", "isInternalGroup") == true
  group_id = group.fetch("id")

  prerelease_versions = request(
    token,
    :get,
    "/v1/preReleaseVersions",
    query: {
      "filter[app]" => app_id,
      "filter[platform]" => "IOS",
      "filter[version]" => options.fetch(:version),
      "fields[preReleaseVersions]" => "version,platform",
      "limit" => "10",
    },
  ).fetch("data", [])
  prerelease = prerelease_versions.find do |candidate|
    candidate.dig("attributes", "version") == options.fetch(:version) &&
      candidate.dig("attributes", "platform") == "IOS"
  end
  builds = if prerelease
             request(
               token,
               :get,
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
  build = builds.find { |candidate| candidate.dig("attributes", "version") == options.fetch(:build) }
  latest_build = builds.map { |candidate| candidate.dig("attributes", "version") }
    .compact.select { |value| value.match?(/\A\d+\z/) }.max_by(&:to_i)

  review_detail = request(
    token,
    :get,
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
    :get,
    "/v1/apps/#{app_id}/betaAppLocalizations",
    query: {
      "fields[betaAppLocalizations]" =>
        "feedbackEmail,marketingUrl,privacyPolicyUrl,description,locale",
      "limit" => "50",
    },
  ).fetch("data", [])
  en_localization = app_localizations.find { |item| item.dig("attributes", "locale") == "en-US" }
  en_attributes = en_localization&.fetch("attributes", {}) || {}
  en_complete = %w[feedbackEmail marketingUrl privacyPolicyUrl description].all? do |name|
    presence(en_attributes[name])
  end

  group_has_build = false
  beta_detail_attributes = {}
  beta_review_attributes = {}
  build_localizations = []
  if build
    build_id = build.fetch("id")
    group_builds = request(
      token,
      :get,
      "/v1/betaGroups/#{group_id}/builds",
      query: { "fields[builds]" => "version", "limit" => "200" },
    ).fetch("data", [])
    group_has_build = group_builds.any? { |candidate| candidate["id"] == build_id }
    beta_detail = request(
      token,
      :get,
      "/v1/builds/#{build_id}/buildBetaDetail",
      query: { "fields[buildBetaDetails]" => "autoNotifyEnabled,internalBuildState,externalBuildState" },
      allow_not_found: true,
    )
    beta_detail_attributes = beta_detail&.dig("data", "attributes") || {}
    beta_review = request(
      token,
      :get,
      "/v1/builds/#{build_id}/betaAppReviewSubmission",
      query: { "fields[betaAppReviewSubmissions]" => "betaReviewState,submittedDate" },
      allow_not_found: true,
    )
    beta_review_attributes = beta_review&.dig("data", "attributes") || {}
    build_localizations = request(
      token,
      :get,
      "/v1/builds/#{build_id}/betaBuildLocalizations",
      query: { "fields[betaBuildLocalizations]" => "whatsNew,locale", "limit" => "50" },
    ).fetch("data", [])
  end

  if options.fetch(:action) == "enable"
    raise "The requested build is not available in App Store Connect" unless build
    raise "The build has not completed processing" unless build.dig("attributes", "processingState") == "VALID"
    raise "The build encryption flag is not the approved value" unless build.dig("attributes", "usesNonExemptEncryption") == false
    raise "The build is not assigned to the external group" unless group_has_build
    raise "Beta App Review is not approved" unless beta_review_attributes["betaReviewState"] == "APPROVED"
    unless %w[READY_FOR_BETA_TESTING IN_BETA_TESTING].include?(beta_detail_attributes["externalBuildState"])
      raise "The approved build is not eligible for external testing"
    end

    request(
      token,
      :patch,
      "/v1/betaGroups/#{group_id}",
      payload: {
        data: {
          type: "betaGroups",
          id: group_id,
          attributes: {
            publicLinkEnabled: true,
            publicLinkLimitEnabled: true,
            publicLinkLimit: options.fetch(:limit),
          },
        },
      },
    )
    group = request(
      token,
      :get,
      "/v1/betaGroups/#{group_id}",
      query: {
        "fields[betaGroups]" =>
          "name,isInternalGroup,publicLinkEnabled,publicLinkLimitEnabled,publicLinkLimit,publicLink",
      },
    ).fetch("data")
    public_link = group.dig("attributes", "publicLink")
    unless group.dig("attributes", "publicLinkEnabled") == true &&
           public_link&.match?(%r{\Ahttps://testflight\.apple\.com/join/[A-Za-z0-9]+\z})
      raise "Apple did not return an active TestFlight public link"
    end
  end

  attributes = group.fetch("attributes")
  puts "APP_STORE_APP_ID=#{app_id}"
  puts "APP_BUNDLE_ID=#{values.fetch('IOS_BUNDLE_ID')}"
  puts "EXTERNAL_GROUP_NAME=#{attributes.fetch('name')}"
  puts "EXTERNAL_GROUP_ID=#{group_id}"
  puts "PUBLIC_LINK_ENABLED=#{attributes['publicLinkEnabled'] == true}"
  puts "PUBLIC_LINK_LIMIT=#{attributes['publicLinkLimitEnabled'] == true ? attributes['publicLinkLimit'] : 'DISABLED'}"
  puts "PUBLIC_LINK=#{attributes['publicLink']}" if attributes["publicLinkEnabled"] == true
  puts "REVIEW_INFORMATION=#{review_complete ? 'COMPLETE' : 'INCOMPLETE'}"
  puts "ENGLISH_TEST_INFORMATION=#{en_complete ? 'COMPLETE' : 'INCOMPLETE'}"
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
