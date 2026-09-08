#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

root = ARGV.shift || File.expand_path("../.github/workflows", __dir__)
abort("Usage: #{$PROGRAM_NAME} [workflow-directory]") unless ARGV.empty?
abort("Workflow directory does not exist or is a symlink") unless File.directory?(root) && !File.symlink?(root)
paths = Dir.glob(File.join(root, "**", "*.{yml,yaml}")).sort
abort("No workflow YAML files found") if paths.empty?
counts = { remote: 0, local: 0 }

inspect_node = lambda do |node, path|
  if node.is_a?(Psych::Nodes::Alias)
    abort("#{path}:#{node.start_line + 1}: YAML aliases are not supported by action-pin policy")
  end
  if node.is_a?(Psych::Nodes::Mapping)
    seen = {}
    node.children.each_slice(2) do |key, value|
      unless key.is_a?(Psych::Nodes::Scalar) && !seen[key.value]
        abort("#{path}:#{key.start_line + 1}: duplicate or complex YAML key")
      end
      seen[key.value] = true
      next unless key.value == "uses"

      location = "#{path}:#{value.start_line + 1}"
      unless value.is_a?(Psych::Nodes::Scalar) && (value.tag.nil? || value.tag == "tag:yaml.org,2002:str")
        abort("#{location}: uses must be a literal action reference")
      end
      reference = value.value
      if reference.start_with?("./")
        parts = reference.delete_prefix("./").split("/", -1)
        unless parts.all? { |part| part.match?(/\A[A-Za-z0-9_.-]+\z/) && !%w[. ..].include?(part) }
          abort("#{location}: unsafe local action reference")
        end
        counts[:local] += 1
      else
        unless reference.match?(%r{\A[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:/[A-Za-z0-9_.-]+)*@[0-9a-f]{40}\z}) &&
               reference.split("@", 2).first.split("/").none? { |part| %w[. ..].include?(part) }
          abort("#{location}: mutable or invalid external action reference: #{reference}")
        end
        counts[:remote] += 1
      end
    end
  end
  Array(node.children).each { |child| inspect_node.call(child, path) } if node.respond_to?(:children)
end

paths.each do |path|
  abort("Unsafe workflow file: #{path}") if File.symlink?(path) || !File.file?(path)
  stream = YAML.parse_stream(File.read(path, encoding: "UTF-8"), filename: path)
  abort("Each workflow must contain one YAML document: #{path}") unless stream.children.one?
  inspect_node.call(stream, path)
rescue Psych::SyntaxError => error
  abort(error.message)
end
puts "All external GitHub Actions are pinned to immutable commits (#{counts[:remote]} external, #{counts[:local]} local)."
