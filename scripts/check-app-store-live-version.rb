#!/usr/bin/env ruby

require "base64"
require "json"
require "net/http"
require "openssl"
require "uri"
require_relative "verify-app-store-live-response"
require_relative "lib/app_store_configuration"
require_relative "lib/dotenv"
require_relative "lib/private_path"

version, build_number = ARGV
unless ARGV.length == 2 && version&.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/) &&
       build_number&.match?(/\A[1-9]\d*\z/) && build_number.length <= 10 &&
       build_number.to_i <= 2_100_000_000
  warn "Usage: #{File.basename($PROGRAM_NAME)} <semantic-version> <build-number>"
  exit 2
end

repository_root = File.expand_path("..", __dir__)
private_root = File.join(repository_root, "release", "private")
values = {}

if ENV["PASSVAULT_ASC_CONFIGURATION_SOURCE"] == "environment"
  %w[ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID].each do |name|
    values[name] = ENV[name].to_s
  end
  runtime_root = ENV["PRIVATE_RUNTIME"].to_s
  if runtime_root.empty?
    warn "The private runtime path is missing."
    exit 1
  end
  private_root = File.expand_path(runtime_root)
else
  values_path = File.join(private_root, "values.env")
  unless File.file?(values_path) && !File.symlink?(values_path)
    warn "The ignored private App Store Connect values file is missing or unsafe."
    exit 1
  end
  values = PassVault::Dotenv.load(values_path)
end

required = %w[ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID]
if required.any? { |name| values[name].to_s.empty? }
  warn "App Store live-version inputs are incomplete."
  exit 1
end
unless PassVault::AppStoreConfiguration.identifiers_valid?(values)
  warn "App Store live-version identifiers are malformed or do not identify PassVault."
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

def api_get(token, path, query = nil)
  uri = URI("https://api.appstoreconnect.apple.com#{path}")
  uri.query = URI.encode_www_form(query) if query
  request = Net::HTTP::Get.new(uri)
  request["Authorization"] = "Bearer #{token}"
  response = Net::HTTP.start(
    uri.host,
    uri.port,
    use_ssl: true,
    open_timeout: 15,
    read_timeout: 45,
  ) { |http| http.request(request) }

  unless response.code == "200"
    error_code = begin
      JSON.parse(response.body).fetch("errors", []).first&.fetch("code", nil)
    rescue JSON::ParserError
      nil
    end
    suffix = error_code ? " (#{error_code})" : ""
    raise "App Store Connect HTTP #{response.code}#{suffix}"
  end

  JSON.parse(response.body)
end

begin
  token = token_for(values, key_path)
  apps = api_get(
    token,
    "/v1/apps",
    {
      "filter[bundleId]" => values.fetch("IOS_BUNDLE_ID"),
      "fields[apps]" => "bundleId",
      "limit" => "10",
    },
  ).fetch("data", [])
  matching_apps = apps.select do |candidate|
    candidate["id"] == values.fetch("APP_STORE_APP_ID") &&
      candidate.dig("attributes", "bundleId") == values.fetch("IOS_BUNDLE_ID")
  end
  raise "Configured App Store app identifiers are ambiguous" if matching_apps.length > 1
  app = matching_apps.first
  raise "Configured App Store app identifiers do not match" unless app

  app_id = app.fetch("id")
  versions = api_get(
    token,
    "/v1/apps/#{app_id}/appStoreVersions",
    {
      "filter[platform]" => "IOS",
      "filter[versionString]" => version,
      "fields[appStoreVersions]" =>
        "platform,versionString,appVersionState,appStoreState,downloadable",
      "limit" => "10",
    },
  )
  version_id, state = PassVault::AppStoreLiveResponse.live_version(
    versions,
    expected_version: version,
  )
  build = api_get(
    token,
    "/v1/appStoreVersions/#{version_id}/build",
    { "fields[builds]" => "version,processingState" },
  )
  PassVault::AppStoreLiveResponse.verify_build(build, expected_build: build_number)

  puts "App Store version #{version} build #{build_number} is publicly live (#{state})."
rescue OpenSSL::PKey::PKeyError, OpenSSL::ASN1::ASN1Error, JSON::ParserError, KeyError,
       RuntimeError, SocketError, SystemCallError, Timeout::Error => error
  warn "App Store live-version check failed: #{error.message}."
  exit 1
ensure
  token = nil
end
