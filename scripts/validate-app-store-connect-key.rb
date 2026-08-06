#!/usr/bin/env ruby

require "openssl"

path = ARGV.fetch(0) do
  warn "Usage: #{$PROGRAM_NAME} <app-store-connect-key.p8>"
  exit 2
end

unless File.file?(path) && !File.symlink?(path)
  warn "The App Store Connect private-key file is missing or unsafe."
  exit 1
end

begin
  pem = File.binread(path)
  unless pem.start_with?("-----BEGIN PRIVATE KEY-----\n") &&
         pem.rstrip.end_with?("-----END PRIVATE KEY-----")
    warn "The App Store Connect key must use an unencrypted PKCS#8 PEM envelope."
    exit 1
  end

  key = OpenSSL::PKey.read(pem)
  valid = key.is_a?(OpenSSL::PKey::EC) &&
          key.private? &&
          key.group.curve_name == "prime256v1" &&
          key.check_key
  unless valid
    warn "The App Store Connect key is not a valid private EC P-256 key."
    exit 1
  end

  puts "App Store Connect private-key validation passed."
rescue OpenSSL::PKey::PKeyError, OpenSSL::PKey::ECError, ArgumentError
  warn "The App Store Connect key could not be parsed as a valid private EC P-256 key."
  exit 1
ensure
  pem&.replace("\0" * pem.bytesize)
  key = nil
end
