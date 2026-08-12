#!/usr/bin/env ruby

require "json"

module PassVault
  module AppStoreLiveResponse
    module_function

    def live_version(document, expected_version:)
      raise "App Store Connect returned an invalid response object" unless document.is_a?(Hash)

      data = document["data"]
      raise "App Store Connect returned an invalid version list" unless data.is_a?(Array)
      unless data.all? { |candidate| candidate.is_a?(Hash) }
        raise "App Store Connect returned an invalid version resource"
      end

      candidates = data.select do |candidate|
        attributes = candidate["attributes"]
        candidate["type"] == "appStoreVersions" &&
          attributes.is_a?(Hash) &&
          attributes["platform"] == "IOS" &&
          attributes["versionString"] == expected_version
      end
      raise "The exact iOS App Store version was not found" if candidates.empty?
      raise "App Store Connect returned duplicate exact iOS versions" unless candidates.one?

      version = candidates.first
      attributes = version.fetch("attributes")
      current_state = attributes["appVersionState"]
      legacy_state = attributes["appStoreState"]
      live = current_state == "READY_FOR_DISTRIBUTION" ||
        (current_state.nil? && legacy_state == "READY_FOR_SALE")
      state = current_state || legacy_state || "UNKNOWN"

      raise "The exact iOS version is not publicly live (#{state})" unless live
      raise "The exact iOS version is not downloadable" unless attributes["downloadable"] == true

      id = version["id"]
      raise "The exact iOS version has no resource ID" unless id.is_a?(String) && !id.empty?

      [id, state]
    end

    def verify_build(document, expected_build:)
      raise "App Store Connect returned an invalid build response" unless document.is_a?(Hash)

      build = document["data"]
      raise "The live App Store version has no attached build" unless build.is_a?(Hash)
      raise "The App Store version returned an invalid build resource" unless build["type"] == "builds"

      attributes = build["attributes"]
      raise "The App Store build has no attributes" unless attributes.is_a?(Hash)
      unless attributes["version"] == expected_build
        raise "The live App Store version is attached to build #{attributes['version'] || 'UNKNOWN'}, " \
          "not #{expected_build}"
      end
      unless attributes["processingState"] == "VALID"
        raise "The attached App Store build is not valid (#{attributes['processingState'] || 'UNKNOWN'})"
      end
      id = build["id"]
      raise "The App Store build has no resource ID" unless id.is_a?(String) && !id.empty?

      true
    end
  end
end

if $PROGRAM_NAME == __FILE__
  versions_path, build_path, expected_version, expected_build = ARGV
  unless ARGV.length == 4
    warn "Usage: #{File.basename($PROGRAM_NAME)} <versions.json> <build.json> <version> <build-number>"
    exit 2
  end
  unless expected_version&.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/) &&
         expected_build&.match?(/\A[1-9]\d*\z/) && expected_build.length <= 10 &&
         expected_build.to_i <= 2_100_000_000
    warn "Expected App Store version or build number is invalid."
    exit 2
  end

  begin
    versions = JSON.parse(File.read(versions_path, encoding: "UTF-8"))
    build = JSON.parse(File.read(build_path, encoding: "UTF-8"))
    _, state = PassVault::AppStoreLiveResponse.live_version(
      versions,
      expected_version: expected_version,
    )
    PassVault::AppStoreLiveResponse.verify_build(build, expected_build: expected_build)
    puts "App Store version #{expected_version} build #{expected_build} is publicly live (#{state})."
  rescue SystemCallError, JSON::ParserError, KeyError, RuntimeError => error
    warn "App Store live-version verification failed: #{error.message}."
    exit 1
  end
end
