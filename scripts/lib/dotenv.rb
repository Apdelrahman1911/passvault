# frozen_string_literal: true

module PassVault
  module Dotenv
    KEY_PATTERN = /\A[A-Z][A-Z0-9_]*\z/
    MAX_BYTES = 1024 * 1024

    module_function

    def load(path)
      raise "The dotenv input is missing or unsafe" unless File.file?(path) && !File.symlink?(path)

      contents = File.binread(path, MAX_BYTES + 1)
      raise "The dotenv input exceeds 1 MiB" if contents.bytesize > MAX_BYTES
      contents.force_encoding(Encoding::UTF_8)
      raise "The dotenv input is not valid UTF-8" unless contents.valid_encoding?
      raise "The dotenv input contains a NUL byte" if contents.include?("\0")

      values = {}
      contents.each_line(chomp: true).with_index(1) do |raw_line, line_number|
        line = raw_line.delete_suffix("\r")
        next if line.strip.empty? || line.lstrip.start_with?("#")

        key, value = line.split("=", 2)
        unless key&.match?(KEY_PATTERN) && !value.nil?
          raise "Invalid dotenv entry at line #{line_number}"
        end
        raise "Duplicate dotenv key #{key} at line #{line_number}" if values.key?(key)

        if value.start_with?("\"", "'")
          quote = value[0]
          unless value.length >= 2 && value.end_with?(quote)
            raise "Unterminated quoted value for #{key} at line #{line_number}"
          end
          value = value[1...-1]
        end
        values[key] = value
      end
      values
    end
  end
end
