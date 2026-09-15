# frozen_string_literal: true

require "json"

module PassVault
  module StrictJson
    # Legacy JSON parsers call []= for every object member. Newer parsers first
    # deduplicate into a Hash and only then apply object_class, so this hook alone
    # is insufficient there; their explicit allow_duplicate_key option is needed.
    class Object < Hash
      def []=(key, value)
        raise JSON::ParserError, "Duplicate JSON field: #{key}" if key?(key)

        super
      end
    end

    PARSE_OPTIONS = { object_class: Object, allow_duplicate_key: false, create_additions: false }.freeze
    DUPLICATE_KEY_PROBE = '{"duplicate_key_probe":null,"duplicate_key_probe":false}'.freeze

    def self.parse(text)
      # Some versions silently ignore unknown options. Prove the combined
      # native/legacy contract with one bounded, non-user probe before trusting
      # this parser, rather than accepting duplicate keys on an unknown backend.
      rejected_probe = false
      begin
        JSON.parse(DUPLICATE_KEY_PROBE, **PARSE_OPTIONS)
      rescue JSON::ParserError
        rejected_probe = true
      end
      unless rejected_probe
        raise JSON::ParserError, "JSON parser cannot enforce unique object fields"
      end

      JSON.parse(text, **PARSE_OPTIONS)
    end
  end
end
