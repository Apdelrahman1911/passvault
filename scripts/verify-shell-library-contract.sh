#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

libraries=(
    scripts/lib/dotenv.sh
    scripts/lib/macos-keychain.sh
    scripts/lib/pkcs12-validation.sh
)
for library in "${libraries[@]}"; do
    grep -Fq 'This file is a sourced library, never a standalone script.' "$library"
    grep -Fq 'must enable strict mode before sourcing this library.' "$library"
    if grep -Eq '^set (-euo pipefail|-e|-u|-o pipefail)' "$library"; then
        echo "Sourced library must not mutate its caller shell options: $library" >&2
        exit 1
    fi
    bash -n "$library"
done

while IFS= read -r caller; do
    grep -Fxq 'set -euo pipefail' "$caller" || {
        echo "Library caller is not strict-mode: $caller" >&2
        exit 1
    }
done < <(
    grep -rlE '(^|[[:space:]])source [^#]*scripts/lib/(dotenv|macos-keychain|pkcs12-validation)\.sh' \
        scripts --include='*.sh' --exclude-dir=lib
)

ruby -ryaml <<'RUBY'
workflow_paths = Dir['.github/workflows/*.{yml,yaml}']
workflow_paths.each do |path|
  document = YAML.safe_load(File.read(path, encoding: 'UTF-8'), aliases: true)
  pending = [document]
  until pending.empty?
    value = pending.pop
    case value
    when Hash
      value.each do |key, child|
        if key == 'run' && child.is_a?(String) &&
            child.match?(%r{source\s+scripts/lib/(dotenv|macos-keychain|pkcs12-validation)\.sh}) &&
            !child.match?(/^\s*set -euo pipefail\s*$/)
          abort("Workflow run block sourcing a shell library is not strict-mode: #{path}")
        end
        pending << child
      end
    when Array
      pending.concat(value)
    end
  end
end
RUBY
