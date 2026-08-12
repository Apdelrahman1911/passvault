# frozen_string_literal: true

require "fileutils"
require "rubygems/package"
require "tempfile"
require "zlib"

module PassVault
  module StoreMetadataArchive
    EXPECTED_FILES = %w[
      privacy-ar.md
      privacy-en.md
      release-notes-ar.md
      release-notes-en.md
      store-description-ar.md
      store-description-en.md
      store-metadata-ar.env
      store-metadata-en.env
    ].freeze
    MAX_FILE_BYTES = 512 * 1024
    MAX_TOTAL_BYTES = 2 * 1024 * 1024

    module_function

    def create(source_path, archive_path)
      source_path = File.expand_path(source_path)
      archive_path = File.expand_path(archive_path)
      validate_directories(source_path, File.dirname(archive_path))
      raise "The metadata archive output already exists or is a symlink." if File.exist?(archive_path) ||
        File.symlink?(archive_path)

      payloads = read_payloads(source_path)
      write_archive(payloads, archive_path)
    end

    def validate_directories(source_path, output_parent)
      unless File.directory?(source_path) && !File.symlink?(source_path)
        raise "The metadata source directory is missing or unsafe."
      end
      unless File.directory?(output_parent) && !File.symlink?(output_parent)
        raise "The metadata archive output directory is missing or unsafe."
      end
    end
    private_class_method :validate_directories

    def read_payloads(source_path)
      total_bytes = 0
      EXPECTED_FILES.map do |name|
        path = File.join(source_path, name)
        initial_stat = File.lstat(path)
        raise "A metadata source is not a regular file: #{name}" unless initial_stat.file? &&
          !initial_stat.symlink?

        flags = File::RDONLY
        flags |= File::NOFOLLOW if defined?(File::NOFOLLOW)
        contents = File.open(path, flags) do |file|
          opened_stat = file.stat
          unless opened_stat.file? && opened_stat.dev == initial_stat.dev && opened_stat.ino == initial_stat.ino
            raise "A metadata source changed while it was opened: #{name}"
          end
          file.read(MAX_FILE_BYTES + 1)
        end
        unless contents.bytesize.positive? && contents.bytesize <= MAX_FILE_BYTES
          raise "A metadata source is empty or exceeds the size limit: #{name}"
        end
        raise "A metadata source contains a NUL byte: #{name}" if contents.include?("\0")

        contents.force_encoding(Encoding::UTF_8)
        raise "A metadata source is not valid UTF-8: #{name}" unless contents.valid_encoding?

        total_bytes += contents.bytesize
        raise "The metadata sources exceed the total size limit." if total_bytes > MAX_TOTAL_BYTES

        [name, contents.b]
      rescue Errno::ENOENT, Errno::ELOOP
        raise "A metadata source is missing or unsafe: #{name}"
      end
    end
    private_class_method :read_payloads

    def write_archive(payloads, archive_path)
      temporary = Tempfile.new(["passvault-store-metadata", ".tar.gz"], File.dirname(archive_path))
      temporary_path = temporary.path
      temporary.close
      begin
        Zlib::GzipWriter.open(temporary_path) do |gzip|
          gzip.mtime = 0
          Gem::Package::TarWriter.new(gzip) do |tar|
            payloads.each do |name, contents|
              tar.add_file_simple(name, 0o600, contents.bytesize) { |entry| entry.write(contents) }
            end
          end
        end
        File.chmod(0o600, temporary_path)
        File.rename(temporary_path, archive_path)
      ensure
        FileUtils.rm_f(temporary_path)
      end
    end
    private_class_method :write_archive
  end
end
