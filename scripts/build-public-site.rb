#!/usr/bin/env ruby
# frozen_string_literal: true

require "cgi"
require "fileutils"
require "optparse"
require "pathname"

options = {
  source: "site",
  output: "_site",
}

OptionParser.new do |parser|
  parser.banner = "Usage: build-public-site.rb [options]"
  parser.on("--source PATH", "Site template directory") { |value| options[:source] = value }
  parser.on("--output PATH", "Generated site directory") { |value| options[:output] = value }
  parser.on("--values-file PATH", "Optional ignored values.env for local builds") do |value|
    options[:values_file] = value
  end
end.parse!

required_values = {
  "PUBLISHER_NAME" => "__PUBLISHER_NAME_EN__",
  "PUBLISHER_NAME_AR" => "__PUBLISHER_NAME_AR__",
  "COUNTRY_OR_JURISDICTION" => "__COUNTRY_OR_JURISDICTION__",
  "SUPPORT_EMAIL" => "__SUPPORT_EMAIL__",
  "SECURITY_EMAIL" => "__SECURITY_EMAIL__",
}.freeze

values = ENV.to_h.slice(*required_values.keys)

if options[:values_file]
  File.foreach(options[:values_file], chomp: true) do |line|
    next if line.empty? || line.start_with?("#") || !line.include?("=")

    key, value = line.split("=", 2)
    mapped_key = case key
                 when "PUBLISHER_NAME_EN" then "PUBLISHER_NAME"
                 else key
                 end
    values[mapped_key] = value if required_values.key?(mapped_key)
  end
end

missing = required_values.keys.select { |key| values.fetch(key, "").strip.empty? }
abort "Missing required public site values: #{missing.join(', ')}" unless missing.empty?

%w[SUPPORT_EMAIL SECURITY_EMAIL].each do |key|
  value = values.fetch(key)
  abort "#{key} is not a valid email address" unless value.match?(/\A[^\s@]+@[^\s@]+\.[^\s@]+\z/)
end

source = Pathname(options.fetch(:source)).expand_path
output = Pathname(options.fetch(:output)).expand_path
repository = Pathname.pwd.expand_path
abort "Site source does not exist: #{source}" unless source.directory?
abort "Output must not be inside the source directory" if output.to_s.start_with?("#{source}/")
abort "Output must be a child of the repository" unless output.to_s.start_with?("#{repository}/")

expected_pages = %w[
  index.html
  privacy/index.html
  support/index.html
  ar/index.html
  ar/privacy/index.html
  ar/support/index.html
].freeze
expected_pages.each do |relative_path|
  abort "Missing site template: #{relative_path}" unless source.join(relative_path).file?
end

FileUtils.rm_rf(output)
FileUtils.mkdir_p(output)
FileUtils.cp_r(Dir.glob(source.join("*").to_s, File::FNM_DOTMATCH).reject { |path| path.end_with?("/.", "/..") }, output)

replacements = required_values.to_h do |key, token|
  [token, CGI.escapeHTML(values.fetch(key).strip)]
end

Dir.glob(output.join("**", "*.html")).each do |path|
  content = File.binread(path).force_encoding(Encoding::UTF_8)
  abort "Site template is not valid UTF-8: #{path}" unless content.valid_encoding?
  replacements.each { |token, value| content = content.gsub(token, value) }
  unresolved = content.scan(/__[A-Z][A-Z0-9_]+__/).uniq
  abort "Unresolved template values in #{path}: #{unresolved.join(', ')}" unless unresolved.empty?
  File.binwrite(path, content)
end

logo_source = Pathname("iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png").expand_path
abort "Missing public logo source: #{logo_source}" unless logo_source.file?
FileUtils.mkdir_p(output.join("assets"))
FileUtils.cp(logo_source, output.join("assets", "passvault-icon.png"))

Dir.glob(output.join("**", "*.html")).each do |path|
  content = File.read(path, encoding: Encoding::UTF_8)
  content.scan(/(?:href|src)="(\/[^"#?]*)"/).flatten.each do |public_path|
    relative_path = public_path.delete_prefix("/")
    destination = output.join(relative_path)
    destination = destination.join("index.html") if relative_path.empty? || destination.directory?
    abort "Broken internal site link in #{path}: #{public_path}" unless destination.file?
  end
end

legacy_redirects = {
  "passvault/index.html" => "/",
  "passvault/privacy/index.html" => "/privacy/",
  "passvault/support/index.html" => "/support/",
  "passvault/ar/index.html" => "/ar/",
  "passvault/ar/privacy/index.html" => "/ar/privacy/",
  "passvault/ar/support/index.html" => "/ar/support/",
}.freeze

legacy_redirects.each do |relative_path, target|
  destination = output.join(relative_path)
  FileUtils.mkdir_p(destination.dirname)
  File.write(
    destination,
    <<~HTML,
      <!doctype html>
      <html lang="en">
      <head>
        <meta charset="utf-8">
        <meta name="robots" content="noindex">
        <meta http-equiv="refresh" content="0; url=#{target}">
        <link rel="canonical" href="https://passvault.kiramanga.me#{target}">
        <title>PassVault</title>
      </head>
      <body><p><a href="#{target}">Continue to PassVault</a></p></body>
      </html>
    HTML
    mode: "w:UTF-8",
  )
end

puts "Public site built successfully (#{expected_pages.length} pages)."
