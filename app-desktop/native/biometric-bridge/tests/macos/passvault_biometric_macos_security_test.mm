// Compile the reviewed implementation into this test-only executable so its
// metadata and error-mapping helpers remain private in the production ABI.
#include "../../src/macos/passvault_biometric_macos.mm"

#include <cstdlib>

#define PV_TEST_CHECK(condition)                                               \
  do {                                                                         \
    if (!(condition))                                                          \
      return __LINE__;                                                         \
  } while (false)

int main() {
  @autoreleasepool {
    char directory_template[] = "/tmp/passvault-biometric-native-test.XXXXXX";
    char *root_value = mkdtemp(directory_template);
    PV_TEST_CHECK(root_value != nullptr);
    const std::string root(root_value);
    const std::string biometric_directory = root + "/biometric";
    const std::string metadata_path = biometric_directory + "/metadata";
    PV_TEST_CHECK(is_owned_directory(root));
    PV_TEST_CHECK(ensure_owned_directory(biometric_directory));

    pv_bio_context *context = nullptr;
    PV_TEST_CHECK(pv_bio_create(root.data(), root.size(), &context) ==
                  PV_BIO_OK);
    PV_TEST_CHECK(context != nullptr);
    std::array<uint8_t, PV_BIO_VAULT_HASH_BYTES> missing_hash{};
    std::array<uint8_t, PV_BIO_VAULT_KEY_BYTES> missing_output{};
    missing_output.fill(0xa5);
    PV_TEST_CHECK(pv_bio_retrieve(context, 1, missing_hash.data(),
                                  missing_hash.size(), missing_output.data(),
                                  missing_output.size()) == PV_BIO_NOT_ENABLED);
    for (uint8_t byte : missing_output) {
      PV_TEST_CHECK(byte == 0);
    }
    pv_bio_destroy(context);
    secure_wipe(missing_hash.data(), missing_hash.size());
    secure_wipe(missing_output.data(), missing_output.size());

    Metadata expected{};
    for (size_t index = 0; index < expected.vault_hash.size(); ++index) {
      expected.vault_hash[index] = static_cast<uint8_t>(index + 1);
    }
    for (size_t index = 0; index < expected.nonce.size(); ++index) {
      expected.nonce[index] = static_cast<uint8_t>(index + 41);
    }
    PV_TEST_CHECK(
        write_metadata_atomic(biometric_directory, metadata_path, expected));

    Metadata decoded{};
    PV_TEST_CHECK(read_metadata(metadata_path, &decoded) ==
                  MetadataReadResult::present);
    PV_TEST_CHECK(std::memcmp(&expected, &decoded, sizeof(expected)) == 0);

    PV_TEST_CHECK(chmod(metadata_path.c_str(), 0644) == 0);
    PV_TEST_CHECK(read_metadata(metadata_path, &decoded) ==
                  MetadataReadResult::invalid);
    PV_TEST_CHECK(chmod(metadata_path.c_str(), 0600) == 0);
    PV_TEST_CHECK(unlink(metadata_path.c_str()) == 0);
    PV_TEST_CHECK(symlink("/etc/passwd", metadata_path.c_str()) == 0);
    PV_TEST_CHECK(read_metadata(metadata_path, &decoded) ==
                  MetadataReadResult::invalid);
    PV_TEST_CHECK(unlink(metadata_path.c_str()) == 0);

    NSError *cancelled = [NSError errorWithDomain:LAErrorDomain
                                             code:LAErrorUserCancel
                                         userInfo:nil];
    NSError *locked = [NSError errorWithDomain:LAErrorDomain
                                          code:LAErrorBiometryLockout
                                      userInfo:nil];
    NSError *unenrolled = [NSError errorWithDomain:LAErrorDomain
                                              code:LAErrorBiometryNotEnrolled
                                          userInfo:nil];
    PV_TEST_CHECK(map_la_error(cancelled) == PV_BIO_CANCELLED);
    PV_TEST_CHECK(map_la_error(locked) == PV_BIO_LOCKED_OUT);
    PV_TEST_CHECK(map_la_error(unenrolled) == PV_BIO_NOT_ENROLLED);

    LAContext *configured_context = [[LAContext alloc] init];
    NSString *reason = @"PassVault native biometric test";
    configure_biometric_context(configured_context, reason);
    PV_TEST_CHECK([configured_context.localizedReason isEqualToString:reason]);
    PV_TEST_CHECK(
        [configured_context.localizedFallbackTitle isEqualToString:@""]);
    PV_TEST_CHECK(
        configured_context.touchIDAuthenticationAllowableReuseDuration == 0);

    secure_wipe(&expected, sizeof(expected));
    secure_wipe(&decoded, sizeof(decoded));
    PV_TEST_CHECK(rmdir(biometric_directory.c_str()) == 0);
    PV_TEST_CHECK(rmdir(root.c_str()) == 0);
    return 0;
  }
}
