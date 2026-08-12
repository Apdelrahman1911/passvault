#!/usr/bin/env ruby

require "json"

module PassVault
  module PlayTrackResponse
    VALID_TRACKS = %w[internal beta production].freeze

    module_function

    def verify(document, track:, build_number:)
      raise "Google Play returned an invalid response object" unless document.is_a?(Hash)
      raise "Invalid Google Play track" unless VALID_TRACKS.include?(track)
      unless build_number.match?(/\A[1-9]\d*\z/) && build_number.length <= 10 &&
             build_number.to_i <= 2_100_000_000
        raise "Invalid Google Play build number"
      end
      raise "Google Play returned a different track" unless document["track"] == track

      releases = document["releases"]
      raise "Google Play returned an invalid release list" unless releases.is_a?(Array)
      matching_releases = releases.select do |release|
        raise "Google Play returned an invalid release resource" unless release.is_a?(Hash)

        version_codes = release["versionCodes"]
        valid_version_codes = version_codes.is_a?(Array) && version_codes.all? do |value|
          value.is_a?(String) && value.match?(/\A[1-9]\d*\z/) &&
            value.length <= 10 && value.to_i <= 2_100_000_000
        end
        raise "Google Play returned an invalid version-code list" unless valid_version_codes
        unless version_codes.uniq.length == version_codes.length
          raise "Google Play returned duplicate version codes in one release"
        end

        version_codes.include?(build_number)
      end
      raise "Build #{build_number} is not present on Google Play track #{track}" if matching_releases.empty?
      raise "Build #{build_number} appears in multiple Google Play releases" unless matching_releases.one?
      matching_release = matching_releases.first
      unless matching_release["status"] == "completed"
        raise "Build #{build_number} is not active on Google Play track #{track}"
      end

      matching_release.fetch("status")
    end
  end
end

if $PROGRAM_NAME == __FILE__
  path, track, build_number = ARGV
  unless ARGV.length == 3
    warn "Usage: #{File.basename($PROGRAM_NAME)} <track-response.json> internal|beta|production <build-number>"
    exit 2
  end

  begin
    document = JSON.parse(File.read(path, encoding: "UTF-8"))
    status = PassVault::PlayTrackResponse.verify(document, track: track, build_number: build_number)
    puts "Google Play build #{build_number} is fully active on #{track} (#{status})."
  rescue SystemCallError, JSON::ParserError, KeyError, RuntimeError => error
    warn "Google Play track verification failed: #{error.message}."
    exit 1
  end
end
