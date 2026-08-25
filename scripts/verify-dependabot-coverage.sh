#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

ruby -ryaml <<'RUBY'
config = YAML.safe_load(File.read('.github/dependabot.yml', encoding: 'UTF-8'))
updates = config.fetch('updates')
required = {
  'gradle' => 'gradle/libs.versions.toml',
  'bundler' => 'Gemfile',
}
required.each do |ecosystem, manifest|
  abort "Missing dependency manifest: #{manifest}" unless File.file?(manifest)
  entry = updates.find { |candidate| candidate['package-ecosystem'] == ecosystem }
  abort "Dependabot does not cover #{ecosystem}" unless entry
  abort "Dependabot #{ecosystem} entry must use repository root" unless entry['directory'] == '/'
  abort "Dependabot #{ecosystem} entry must run weekly" unless entry.dig('schedule', 'interval') == 'weekly'
end
RUBY
