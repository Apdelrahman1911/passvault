#!/usr/bin/env ruby

require "base64"
require "json"
require "net/http"
require "openssl"
require "uri"

repository_root = File.expand_path("..", __dir__)
private_root = File.join(repository_root, "release", "private")
values_path = File.join(private_root, "values.env")

unless File.file?(values_path) && !File.symlink?(values_path)
  warn "The private values file is missing or unsafe."
  exit 1
end

values = {}
File.foreach(values_path, chomp: true) do |line|
  next if line.empty? || line.lstrip.start_with?("#")
  key, value = line.split("=", 2)
  next unless key&.match?(/\A[A-Z][A-Z0-9_]*\z/) && !value.nil?
  values[key] = value.delete_suffix("\r")
end

required = %w[ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID]
if required.any? { |name| values[name].to_s.empty? }
  warn "App Store Connect validation inputs are incomplete."
  exit 1
end

private_key_path = File.expand_path(values.fetch("ASC_PRIVATE_KEY_FILE"), repository_root)
unless private_key_path.start_with?(private_root + File::SEPARATOR) &&
       File.file?(private_key_path) && !File.symlink?(private_key_path)
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

  uri = URI("https://api.appstoreconnect.apple.com/v1/apps")
  uri.query = URI.encode_www_form("filter[bundleId]" => values.fetch("IOS_BUNDLE_ID"))
  request = Net::HTTP::Get.new(uri)
  request["Authorization"] = "Bearer #{token}"

  response = Net::HTTP.start(uri.host, uri.port, use_ssl: true, open_timeout: 15, read_timeout: 30) do |http|
    http.request(request)
  end

  unless response.is_a?(Net::HTTPSuccess)
    warn "App Store Connect API authentication failed with HTTP #{response.code}."
    exit 1
  end

  apps = JSON.parse(response.body).fetch("data", [])
  expected_id = values.fetch("APP_STORE_APP_ID")
  expected_bundle = values.fetch("IOS_BUNDLE_ID")
  matching_app = apps.find do |app|
    app["id"] == expected_id && app.dig("attributes", "bundleId") == expected_bundle
  end

  unless matching_app
    warn "The App Store app ID and bundle ID do not identify the same accessible app."
    exit 1
  end

  puts "App Store Connect API validation passed for the configured app identifiers."

  availability_uri = URI(
    "https://api.appstoreconnect.apple.com/v1/apps/#{expected_id}/appAvailabilityV2",
  )
  availability_request = Net::HTTP::Get.new(availability_uri)
  availability_request["Authorization"] = "Bearer #{token}"
  availability_response = Net::HTTP.start(
    availability_uri.host,
    availability_uri.port,
    use_ssl: true,
    open_timeout: 15,
    read_timeout: 30,
  ) { |http| http.request(availability_request) }

  if availability_response.is_a?(Net::HTTPSuccess)
    availability = JSON.parse(availability_response.body).fetch("data")
    availability_id = availability.fetch("id")
    territories_uri = URI(
      "https://api.appstoreconnect.apple.com/v2/appAvailabilities/#{availability_id}/territoryAvailabilities",
    )
    territories_uri.query = URI.encode_www_form("limit" => "200")
    territories_request = Net::HTTP::Get.new(territories_uri)
    territories_request["Authorization"] = "Bearer #{token}"
    territories_response = Net::HTTP.start(
      territories_uri.host,
      territories_uri.port,
      use_ssl: true,
      open_timeout: 15,
      read_timeout: 30,
    ) { |http| http.request(territories_request) }

    if territories_response.is_a?(Net::HTTPSuccess)
      territories = JSON.parse(territories_response.body).fetch("data", [])
      france = territories.find do |territory|
        territory.dig("relationships", "territory", "data", "id") == "FRA"
      end
      france_status = france&.dig("attributes", "available") == true ? "enabled" : "disabled"
      puts "App Store France availability: #{france_status}."
      puts "Automatic availability in new territories: " \
        "#{availability.dig('attributes', 'availableInNewTerritories') == true ? 'enabled' : 'disabled'}."
    else
      warn "App Store territory availability could not be read (HTTP #{territories_response.code})."
    end
  elsif availability_response.code == "404"
    puts "App Store territory availability: not configured."
  else
    warn "App Store availability could not be read (HTTP #{availability_response.code})."
  end
rescue OpenSSL::PKey::PKeyError, OpenSSL::ASN1::ASN1Error, JSON::ParserError, KeyError, RuntimeError => error
  warn "App Store Connect validation failed: #{error.class}."
  exit 1
rescue SocketError, SystemCallError, Timeout::Error => error
  warn "App Store Connect validation could not reach Apple: #{error.class}."
  exit 1
ensure
  token = nil
  signature = nil
  signing_input = nil
  private_key = nil
end
