#!/usr/bin/env ruby

require "fileutils"
require "rubygems/package"
require "zlib"
require_relative "lib/store_metadata_archive"

EXPECTED_FILES = PassVault::StoreMetadataArchive::EXPECTED_FILES
MAX_FILE_BYTES = PassVault::StoreMetadataArchive::MAX_FILE_BYTES
MAX_TOTAL_BYTES = PassVault::StoreMetadataArchive::MAX_TOTAL_BYTES

archive_path, output_path = ARGV
unless ARGV.length == 2
  warn "Usage: #{File.basename($PROGRAM_NAME)} <metadata.tar.gz> <empty-output-directory>"
  exit 2
end

archive_path = File.expand_path(archive_path)
output_path = File.expand_path(output_path)
unless File.file?(archive_path) && !File.symlink?(archive_path)
  abort "The metadata archive is missing or unsafe."
end
unless File.directory?(output_path) && !File.symlink?(output_path) && Dir.empty?(output_path)
  abort "The metadata output directory must exist, be empty, and not be a symlink."
end

seen = []
total_bytes = 0
begin
  Zlib::GzipReader.open(archive_path) do |gzip|
    Gem::Package::TarReader.new(gzip) do |tar|
      tar.each do |entry|
        name = entry.full_name
        unless EXPECTED_FILES.include?(name) && entry.file? && !seen.include?(name)
          raise "The metadata archive contains an unexpected, duplicate, or non-file entry."
        end
        unless entry.size.positive? && entry.size <= MAX_FILE_BYTES
          raise "A metadata file is empty or exceeds the size limit."
        end

        total_bytes += entry.size
        raise "The metadata archive exceeds the total size limit." if total_bytes > MAX_TOTAL_BYTES

        contents = entry.read
        raise "A metadata file was truncated." unless contents.bytesize == entry.size
        raise "A metadata file contains a NUL byte." if contents.include?("\0")
        contents.force_encoding(Encoding::UTF_8)
        raise "A metadata file is not valid UTF-8." unless contents.valid_encoding?

        destination = File.join(output_path, name)
        File.open(destination, File::WRONLY | File::CREAT | File::EXCL, 0o600) do |file|
          file.write(contents)
        end
        seen << name
      end
    end
  end
rescue StandardError => error
  FileUtils.rm_f(Dir.glob(File.join(output_path, "*")))
  warn "Store metadata extraction failed: #{error.message}"
  exit 1
end

missing = EXPECTED_FILES - seen
unless missing.empty?
  FileUtils.rm_f(Dir.glob(File.join(output_path, "*")))
  abort "The metadata archive is incomplete: #{missing.join(', ')}"
end

puts "Extracted the exact approved store-metadata file set."
