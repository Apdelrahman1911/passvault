#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "lib/store_metadata_archive"
require_relative "lib/dotenv"

unless ARGV.length.between?(1, 2)
  abort("Usage: #{$PROGRAM_NAME} <metadata-directory> [configuration-values.env]")
end
configuration = ARGV[1] && PassVault::Dotenv.load(ARGV[1])
PassVault::StoreMetadataArchive.validate_inputs(ARGV[0], configuration)
puts "Canonical bilingual metadata inputs are valid."
