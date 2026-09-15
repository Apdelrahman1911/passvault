# frozen_string_literal: true

require "open3"
require_relative "strict_json"

module PassVault
  module GitHubApi
    module_function

    def repository!(value)
      unless value.is_a?(String) && value.match?(%r{\A[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\z}) &&
             value.split("/").none? { |part| %w[. ..].include?(part) }
        raise "Invalid GitHub repository identity"
      end
      value
    end

    # HTTP status, not stderr wording, distinguishes absence from denied or
    # failed requests. Never print response bodies or inherited credentials.
    def request(method, path, acceptable: [200], fields: {})
      arguments = ["gh", "api", "--include", "--method", method, path]
      fields.each { |key, value| arguments.concat(["--raw-field", "#{key}=#{value}"]) }
      stdout, _stderr, result = Open3.capture3(*arguments)
      raise "GitHub response exceeds the size limit" if stdout.bytesize > 1024 * 1024

      headers, body = stdout.split(/\r?\n\r?\n/, 2)
      match = headers.to_s.match(/\AHTTP\/\d+(?:\.\d+)? (\d{3})\b/)
      raise "GitHub returned no valid HTTP response" unless match && body

      status = Integer(match[1], 10)
      unless acceptable.include?(status) && result.success? == status.between?(200, 299)
        raise "GitHub #{method} request failed (HTTP #{status})"
      end
      payload = body.strip.empty? ? nil : PassVault::StrictJson.parse(body)
      [status, payload]
    end
  end
end
