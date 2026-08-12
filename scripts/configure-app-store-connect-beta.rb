#!/usr/bin/env ruby

require "base64"
require "json"
require "net/http"
require "openssl"
require "uri"
require_relative "lib/app_store_configuration"
require_relative "lib/dotenv"
require_relative "lib/private_path"

unless ARGV == ["--apply"]
  warn "Usage: #{File.basename($PROGRAM_NAME)} --apply"
  warn "The apply flag is required because this command creates empty TestFlight groups."
  exit 2
end

repository_root = File.expand_path("..", __dir__)
private_root = File.join(repository_root, "release", "private")
values_path = File.join(private_root, "values.env")
values = {}

unless File.file?(values_path) && !File.symlink?(values_path)
  warn "The private values file is missing or unsafe."
  exit 1
end

values = PassVault::Dotenv.load(values_path)

required = %w[
  ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
  TESTFLIGHT_EXTERNAL_GROUP
]
if required.any? { |name| values[name].to_s.empty? }
  warn "App Store Connect beta-group inputs are incomplete."
  exit 1
end
unless PassVault::AppStoreConfiguration.identifiers_valid?(values)
  warn "App Store Connect beta-group identifiers are malformed or do not identify PassVault."
  exit 1
end
unless PassVault::AppStoreConfiguration.external_group_name_valid?(
  values.fetch("TESTFLIGHT_EXTERNAL_GROUP"),
)
  warn "The external TestFlight group name must be a trimmed printable line of at most 100 characters."
  exit 1
end

private_key_path = File.expand_path(values.fetch("ASC_PRIVATE_KEY_FILE"), repository_root)
unless PassVault::PrivatePath.regular_file_within?(private_key_path, private_root)
  warn "The App Store Connect private-key path is unsafe or missing."
  exit 1
end

def base64url(value)
  Base64.urlsafe_encode64(value, padding: false)
end

def jose_signature(der_signature)
  sequence = OpenSSL::ASN1.decode(der_signature)
  integers = sequence.value.map(&:value)
  raise "Unexpected ECDSA signature format" unless integers.length == 2

  integers.map do |integer|
    [integer.to_s(16).rjust(64, "0")].pack("H*")
  end.join
end

def asc_request(uri, token, method: :get, payload: nil)
  request = method == :post ? Net::HTTP::Post.new(uri) : Net::HTTP::Get.new(uri)
  request["Authorization"] = "Bearer #{token}"
  if payload
    request["Content-Type"] = "application/json"
    request.body = JSON.generate(payload)
  end
  Net::HTTP.start(
    uri.host,
    uri.port,
    use_ssl: true,
    open_timeout: 15,
    read_timeout: 30,
  ) { |http| http.request(request) }
end

def create_group(token, app_id, name, internal:)
  attributes = {
    name: name,
    isInternalGroup: internal,
    hasAccessToAllBuilds: false,
    feedbackEnabled: true,
  }
  attributes[:publicLinkEnabled] = false unless internal
  payload = {
    data: {
      type: "betaGroups",
      attributes: attributes,
      relationships: {
        app: { data: { type: "apps", id: app_id } },
      },
    },
  }
  response = asc_request(
    URI("https://api.appstoreconnect.apple.com/v1/betaGroups"),
    token,
    method: :post,
    payload: payload,
  )
  unless response.code == "201"
    warn "App Store Connect refused beta-group creation with HTTP #{response.code}."
    exit 1
  end
  JSON.parse(response.body).fetch("data")
end

begin
  private_key = OpenSSL::PKey.read(File.binread(private_key_path))
  unless private_key.is_a?(OpenSSL::PKey::EC) && private_key.private? &&
         private_key.group.curve_name == "prime256v1" && private_key.check_key
    warn "The App Store Connect key is not a valid private EC P-256 key."
    exit 1
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
  token = [signing_input, base64url(signature)].join(".")

  app_id = values.fetch("APP_STORE_APP_ID")
  apps_uri = URI("https://api.appstoreconnect.apple.com/v1/apps")
  apps_uri.query = URI.encode_www_form("filter[bundleId]" => values.fetch("IOS_BUNDLE_ID"))
  apps_response = asc_request(apps_uri, token)
  unless apps_response.is_a?(Net::HTTPSuccess)
    warn "App Store Connect app validation failed with HTTP #{apps_response.code}."
    exit 1
  end
  app = JSON.parse(apps_response.body).fetch("data", []).find { |item| item["id"] == app_id }
  unless app&.dig("attributes", "bundleId") == values.fetch("IOS_BUNDLE_ID")
    warn "The configured App Store app ID and bundle ID do not match."
    exit 1
  end

  groups_uri = URI("https://api.appstoreconnect.apple.com/v1/betaGroups")
  groups_uri.query = URI.encode_www_form(
    "filter[app]" => app_id,
    "fields[betaGroups]" => "name,isInternalGroup,publicLinkEnabled",
    "limit" => "200",
  )
  groups_response = asc_request(groups_uri, token)
  unless groups_response.is_a?(Net::HTTPSuccess)
    warn "App Store Connect beta groups could not be listed (HTTP #{groups_response.code})."
    exit 1
  end
  groups = JSON.parse(groups_response.body).fetch("data", [])

  unless groups.any? { |group| group.dig("attributes", "isInternalGroup") == true }
    created = create_group(token, app_id, "PassVault Internal", internal: true)
    groups << created
    puts "Created an empty internal TestFlight group."
  end

  external_name = values.fetch("TESTFLIGHT_EXTERNAL_GROUP")
  matching_external_groups = groups.select do |group|
    group.dig("attributes", "name") == external_name
  end
  if matching_external_groups.length > 1
    warn "The configured external TestFlight group name is ambiguous."
    exit 1
  end
  external_group = matching_external_groups.first
  if external_group&.dig("attributes", "isInternalGroup") == true
    warn "The configured external group name belongs to an internal group."
    exit 1
  end
  unless external_group
    external_group = create_group(token, app_id, external_name, internal: false)
    puts "Created the empty external TestFlight group."
  end
  if external_group.dig("attributes", "publicLinkEnabled") == true
    warn "The external TestFlight group has a public link enabled; disable it before continuing."
    exit 1
  end

  puts "TestFlight groups are prepared without testers, builds, or public-link distribution."
rescue OpenSSL::PKey::PKeyError, OpenSSL::ASN1::ASN1Error, JSON::ParserError, KeyError, RuntimeError => error
  warn "App Store Connect beta-group configuration failed: #{error.class}."
  exit 1
rescue SocketError, SystemCallError, Timeout::Error => error
  warn "App Store Connect beta-group configuration could not reach Apple: #{error.class}."
  exit 1
ensure
  token = nil
  signature = nil
  signing_input = nil
  private_key = nil
end
