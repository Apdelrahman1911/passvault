#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "lib/github_api"

abort("Usage: #{$PROGRAM_NAME} <repository> <environment> <secret-name>") unless ARGV.length == 3
repository = PassVault::GitHubApi.repository!(ARGV[0])
environment, secret = ARGV[1..2]
abort("Invalid environment name") unless environment.match?(/\A[A-Za-z0-9][A-Za-z0-9_-]{0,99}\z/)
abort("Invalid secret name") unless secret.match?(/\A[A-Z][A-Z0-9_]{0,99}\z/)
path = "repos/#{repository}/environments/#{environment}/secrets/#{secret}"
PassVault::GitHubApi.request("DELETE", path, acceptable: [204, 404])
status, = PassVault::GitHubApi.request("GET", path, acceptable: [200, 404])
abort("The obsolete environment secret is still present after deletion") unless status == 404
puts "Obsolete environment secret is absent."
