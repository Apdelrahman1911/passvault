#!/usr/bin/env ruby

require "base64"
require "json"
require "net/http"
require "openssl"
require "uri"

repository_root = File.expand_path("..", __dir__)
private_root = File.join(repository_root, "release", "private")
values_path = File.join(private_root, "values.env")
values = {}
environment_source = ENV["PASSVAULT_ASC_CONFIGURATION_SOURCE"] == "environment"

if environment_source
  %w[
    ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
    EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
  ].each { |name| values[name] = ENV[name].to_s }
  runtime_root = ENV["PRIVATE_RUNTIME"].to_s
  if runtime_root.empty?
    warn "The private runtime path is missing."
    exit 1
  end
  private_root = File.expand_path(runtime_root)
else
  unless File.file?(values_path) && !File.symlink?(values_path)
    warn "The private values file is missing or unsafe."
    exit 1
  end

  File.foreach(values_path, chomp: true) do |line|
    next if line.empty? || line.lstrip.start_with?("#")
    key, value = line.split("=", 2)
    next unless key&.match?(/\A[A-Z][A-Z0-9_]*\z/) && !value.nil?
    values[key] = value.delete_suffix("\r")
  end
end

required = %w[
  ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_FILE IOS_BUNDLE_ID APP_STORE_APP_ID
  EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
]
if required.any? { |name| values[name].to_s.empty? }
  warn "App Store Connect validation inputs are incomplete."
  exit 1
end

unless %w[EXEMPT_APPROVED NON_EXEMPT_APPROVED].include?(values.fetch("EXPORT_COMPLIANCE_STATUS"))
  warn "An approved export-compliance status is required."
  exit 1
end

unless %w[true false].include?(values.fetch("IOS_FRANCE_AVAILABLE"))
  warn "IOS_FRANCE_AVAILABLE must be exactly true or false."
  exit 1
end

if values.fetch("EXPORT_COMPLIANCE_STATUS") == "EXEMPT_APPROVED" &&
   values.fetch("IOS_FRANCE_AVAILABLE") != "false"
  warn "EXEMPT_APPROVED requires France to remain excluded."
  exit 1
end

private_key_path = File.expand_path(values.fetch("ASC_PRIVATE_KEY_FILE"), repository_root)
unless !private_root.empty? && private_key_path.start_with?(private_root + File::SEPARATOR) &&
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

  tester_candidates = ENV.fetch(
    "PASSVAULT_ASC_INTERNAL_TESTER_CANDIDATES",
    values.fetch("TESTFLIGHT_INTERNAL_EMAILS", ""),
  ).split(",").map(&:strip).reject(&:empty?).uniq
  eligible_internal_roles = %w[ACCOUNT_HOLDER ADMIN APP_MANAGER DEVELOPER MARKETING].freeze

  tester_candidates.each do |email|
    unless email.match?(/\A[^\s@]+@[^\s@]+\.[^\s@]+\z/) && !email.end_with?(".invalid")
      puts "Internal TestFlight candidate #{email}: not eligible (invalid email format)."
      next
    end

    users_uri = URI("https://api.appstoreconnect.apple.com/v1/users")
    users_uri.query = URI.encode_www_form(
      "filter[username]" => email,
      "fields[users]" => "username,roles,allAppsVisible",
      "limit" => "1",
    )
    users_request = Net::HTTP::Get.new(users_uri)
    users_request["Authorization"] = "Bearer #{token}"
    users_response = Net::HTTP.start(
      users_uri.host,
      users_uri.port,
      use_ssl: true,
      open_timeout: 15,
      read_timeout: 30,
    ) { |http| http.request(users_request) }

    unless users_response.is_a?(Net::HTTPSuccess)
      puts "Internal TestFlight candidate #{email}: eligibility could not be verified " \
        "(Users API HTTP #{users_response.code})."
      next
    end

    user = JSON.parse(users_response.body).fetch("data", []).first
    unless user
      puts "Internal TestFlight candidate #{email}: not eligible (not an App Store Connect user)."
      next
    end

    roles = Array(user.dig("attributes", "roles"))
    role_eligible = !(roles & eligible_internal_roles).empty?
    app_visible = user.dig("attributes", "allAppsVisible") == true

    unless app_visible
      apps_uri = URI(
        "https://api.appstoreconnect.apple.com/v1/users/#{user.fetch('id')}/relationships/visibleApps",
      )
      apps_uri.query = URI.encode_www_form("limit" => "200")
      apps_request = Net::HTTP::Get.new(apps_uri)
      apps_request["Authorization"] = "Bearer #{token}"
      apps_response = Net::HTTP.start(
        apps_uri.host,
        apps_uri.port,
        use_ssl: true,
        open_timeout: 15,
        read_timeout: 30,
      ) { |http| http.request(apps_request) }
      if apps_response.is_a?(Net::HTTPSuccess)
        visible_app_ids = JSON.parse(apps_response.body).fetch("data", []).map { |item| item["id"] }
        app_visible = visible_app_ids.include?(expected_id)
      else
        puts "Internal TestFlight candidate #{email}: eligibility could not be verified " \
          "(app-access API HTTP #{apps_response.code})."
        next
      end
    end

    if role_eligible && app_visible
      puts "Internal TestFlight candidate #{email}: eligible App Store Connect user."
    else
      puts "Internal TestFlight candidate #{email}: not eligible " \
        "(required role or PassVault app access is missing)."
    end
  end

  external_group_name = values.fetch("TESTFLIGHT_EXTERNAL_GROUP", "").strip
  unless external_group_name.empty?
    groups_uri = URI("https://api.appstoreconnect.apple.com/v1/betaGroups")
    groups_uri.query = URI.encode_www_form(
      "filter[app]" => expected_id,
      "filter[name]" => external_group_name,
      "fields[betaGroups]" => "name,isInternalGroup,publicLinkEnabled",
      "limit" => "10",
    )
    groups_request = Net::HTTP::Get.new(groups_uri)
    groups_request["Authorization"] = "Bearer #{token}"
    groups_response = Net::HTTP.start(
      groups_uri.host,
      groups_uri.port,
      use_ssl: true,
      open_timeout: 15,
      read_timeout: 30,
    ) { |http| http.request(groups_request) }

    if groups_response.is_a?(Net::HTTPSuccess)
      group = JSON.parse(groups_response.body).fetch("data", []).find do |candidate|
        candidate.dig("attributes", "name") == external_group_name
      end
      if group.nil?
        puts "External TestFlight group: not configured."
      elsif group.dig("attributes", "isInternalGroup") == true
        puts "External TestFlight group: invalid (configured name belongs to an internal group)."
      elsif group.dig("attributes", "publicLinkEnabled") == true
        puts "External TestFlight group: configured, but its public link is enabled."
      else
        puts "External TestFlight group: configured with no public link."
      end
    else
      puts "External TestFlight group: could not be verified (HTTP #{groups_response.code})."
    end
  end

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

  france_available = nil
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
      france_available = france&.dig("attributes", "available") == true
      france_status = france_available ? "enabled" : "disabled"
      puts "App Store France availability: #{france_status}."
      puts "Automatic availability in new territories: " \
        "#{availability.dig('attributes', 'availableInNewTerritories') == true ? 'enabled' : 'disabled'}."
    else
      warn "App Store territory availability could not be read (HTTP #{territories_response.code})."
      exit 1
    end
  elsif availability_response.code == "404"
    france_available = false
    puts "App Store territory availability: not configured."
  else
    warn "App Store availability could not be read (HTTP #{availability_response.code})."
    exit 1
  end

  expected_france_available = values.fetch("IOS_FRANCE_AVAILABLE") == "true"
  if france_available != expected_france_available
    warn "App Store France availability does not match the approved release configuration."
    exit 1
  end
  if values.fetch("EXPORT_COMPLIANCE_STATUS") == "EXEMPT_APPROVED" && france_available
    warn "France is enabled while the no-documentation determination is configured."
    exit 1
  end
  puts "App Store France availability matches the approved release constraint."

  declarations_uri = URI(
    "https://api.appstoreconnect.apple.com/v1/apps/#{expected_id}/appEncryptionDeclarations",
  )
  declarations_uri.query = URI.encode_www_form(
    "fields[appEncryptionDeclarations]" =>
      "usesEncryption,exempt,containsProprietaryCryptography,containsThirdPartyCryptography," \
      "availableOnFrenchStore,appEncryptionDeclarationState",
    "limit" => "200",
  )
  declarations_request = Net::HTTP::Get.new(declarations_uri)
  declarations_request["Authorization"] = "Bearer #{token}"
  declarations_response = Net::HTTP.start(
    declarations_uri.host,
    declarations_uri.port,
    use_ssl: true,
    open_timeout: 15,
    read_timeout: 30,
  ) { |http| http.request(declarations_request) }

  if declarations_response.is_a?(Net::HTTPSuccess)
    declarations = JSON.parse(declarations_response.body).fetch("data", [])
    if declarations.empty?
      puts "App Store encryption declarations: none configured."
    else
      states = declarations.filter_map { |item| item.dig("attributes", "appEncryptionDeclarationState") }.uniq
      puts "App Store encryption declarations: #{declarations.length}; states: #{states.join(', ')}."
    end
  elsif declarations_response.code == "404"
    puts "App Store encryption declarations: none configured."
  else
    warn "App Store encryption declarations could not be read (HTTP #{declarations_response.code})."
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
