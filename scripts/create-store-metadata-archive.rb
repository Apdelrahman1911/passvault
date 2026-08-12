#!/usr/bin/env ruby

require_relative "lib/store_metadata_archive"

unless ARGV.length == 2
  warn "Usage: #{File.basename($PROGRAM_NAME)} <metadata-source-directory> <metadata.tar.gz>"
  exit 2
end

PassVault::StoreMetadataArchive.create(ARGV.fetch(0), ARGV.fetch(1))
puts "Created an archive containing only the approved store-metadata files."
