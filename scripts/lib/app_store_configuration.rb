# frozen_string_literal: true

module PassVault
  module AppStoreConfiguration
    EXPECTED_BUNDLE_ID = "com.passvault.ios"
    KEY_ID_PATTERN = /\A[A-Z0-9]{10}\z/
    ISSUER_ID_PATTERN = /\A[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\z/
    APP_ID_PATTERN = /\A[1-9]\d{7,14}\z/
    MAX_GROUP_NAME_LENGTH = 100

    module_function

    def identifiers_valid?(values)
      values.is_a?(Hash) &&
        values["ASC_KEY_ID"].is_a?(String) && values["ASC_KEY_ID"].match?(KEY_ID_PATTERN) &&
        values["ASC_ISSUER_ID"].is_a?(String) && values["ASC_ISSUER_ID"].match?(ISSUER_ID_PATTERN) &&
        values["IOS_BUNDLE_ID"] == EXPECTED_BUNDLE_ID &&
        values["APP_STORE_APP_ID"].is_a?(String) && values["APP_STORE_APP_ID"].match?(APP_ID_PATTERN)
    end

    def external_group_name_valid?(value)
      value.is_a?(String) &&
        !value.empty? &&
        value == value.strip &&
        value.length <= MAX_GROUP_NAME_LENGTH &&
        !value.match?(/[\u0000-\u001f\u007f]/)
    end
  end
end
