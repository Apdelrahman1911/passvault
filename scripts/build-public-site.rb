#!/usr/bin/env ruby
# frozen_string_literal: true

require "cgi"
require "fileutils"
require "find"
require "optparse"
require "pathname"
require_relative "lib/dotenv"

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
  PassVault::Dotenv.load(options[:values_file]).each do |key, value|
    mapped_key = case key
                 when "PUBLISHER_NAME_EN" then "PUBLISHER_NAME"
                 else key
                 end
    values[mapped_key] = value if required_values.key?(mapped_key)
  end
end

missing = required_values.keys.select { |key| values.fetch(key, "").strip.empty? }
abort "Missing required public site values: #{missing.join(', ')}" unless missing.empty?
required_values.each_key do |key|
  value = values.fetch(key)
  abort "#{key} is not valid UTF-8" unless value.valid_encoding?
  abort "#{key} must be a single printable line" if value.match?(/[\u0000-\u001f\u007f]/)
end

%w[SUPPORT_EMAIL SECURITY_EMAIL].each do |key|
  value = values.fetch(key)
  abort "#{key} is not a valid email address" unless value.match?(/\A[^\s@]+@[^\s@]+\.[^\s@]+\z/)
end

def path_within?(path, root)
  relative = path.relative_path_from(root)
  !relative.absolute? && relative.each_filename.none? { |part| part == ".." }
rescue ArgumentError
  false
end

def reject_symlink_components(root, path, label)
  current = root
  path.relative_path_from(root).each_filename do |part|
    current = current.join(part)
    abort "#{label} contains a symlink component: #{current}" if current.symlink?
    break unless current.exist?
  end
end

repository = Pathname.pwd.realpath
source = Pathname(options.fetch(:source)).expand_path(repository)
output = Pathname(options.fetch(:output)).expand_path(repository)
abort "Site source must be inside the repository" unless path_within?(source, repository)
abort "Site source does not exist: #{source}" unless source.directory?
reject_symlink_components(repository, source, "Site source")
Find.find(source.to_s) do |entry|
  status = File.lstat(entry)
  abort "Site source contains a symlink: #{entry}" if status.symlink?
  abort "Site source contains a special file: #{entry}" unless status.file? || status.directory?
end
if output == source || output.to_s.start_with?("#{source}/")
  abort "Output must not be the source directory or one of its children"
end
abort "Output must be a child of the repository" unless path_within?(output, repository) && output != repository
allowed_output = output == repository.join("_site") ||
                 output.to_s.start_with?("#{repository.join('build')}/")
abort "Output must be _site or a child of build/" unless allowed_output
reject_symlink_components(repository, output, "Site output")

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

FileUtils.rm_rf(output, secure: true)
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
reject_symlink_components(repository, logo_source, "Public logo source")
abort "Missing or unsafe public logo source: #{logo_source}" unless logo_source.file? && !logo_source.symlink?
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
