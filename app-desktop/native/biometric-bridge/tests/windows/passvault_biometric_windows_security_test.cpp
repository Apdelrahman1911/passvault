// Intentionally compile the reviewed implementation into this test-only
// executable. This keeps its anonymous-namespace crypto and parser helpers
// directly testable without exporting a production test ABI.
#include "../../src/windows/passvault_biometric_windows.cpp"

#include <new>
#include <type_traits>

#define PV_TEST_CHECK(condition)                                               \
  do {                                                                         \
    if (!(condition))                                                          \
      return __LINE__;                                                         \
  } while (false)

class TestContextFixture final {
public:
  explicit TestContextFixture(const std::filesystem::path &path) : root(path) {}
  ~TestContextFixture() { static_cast<void>(close()); }
  TestContextFixture(const TestContextFixture &) = delete;
  TestContextFixture &operator=(const TestContextFixture &) = delete;

  bool close() {
    pv_bio_destroy(context);
    context = nullptr;
    if (!owns_root) {
      return true;
    }
    std::error_code error;
    std::filesystem::remove_all(root, error);
    if (!error) {
      owns_root = false;
    }
    return !error;
  }

  const std::filesystem::path root;
  pv_bio_context *context = nullptr;
  bool owns_root = false;
};

enum class FileWriterTestCase {
  success,
  validation_failure,
  dacl_failure,
  collision,
  empty_payload,
  oversized_payload,
  empty_suffix,
  rename_failure,
};

std::optional<FileWriterTestCase> parse_file_writer_case(std::string_view name) {
  if (name == "success")
    return FileWriterTestCase::success;
  if (name == "validation_failure")
    return FileWriterTestCase::validation_failure;
  if (name == "dacl_failure")
    return FileWriterTestCase::dacl_failure;
  if (name == "collision")
    return FileWriterTestCase::collision;
  if (name == "empty_payload")
    return FileWriterTestCase::empty_payload;
  if (name == "oversized_payload")
    return FileWriterTestCase::oversized_payload;
  if (name == "empty_suffix")
    return FileWriterTestCase::empty_suffix;
  if (name == "rename_failure")
    return FileWriterTestCase::rename_failure;
  return std::nullopt;
}

bool file_has_bytes(const std::filesystem::path &path,
                    const std::vector<uint8_t> &expected) {
  std::vector<uint8_t> actual;
  const bool matches = read_secure_file(path, &actual) == FileReadResult::present &&
                       actual == expected;
  secure_wipe(actual);
  return matches;
}

bool directory_has_only(const std::filesystem::path &directory,
                         const std::vector<std::filesystem::path> &expected) {
  std::error_code error;
  std::filesystem::directory_iterator entry(directory, error);
  if (error)
    return false;
  size_t count = 0;
  const std::filesystem::directory_iterator end;
  while (entry != end) {
    if (std::find(expected.begin(), expected.end(), entry->path()) == expected.end())
      return false;
    ++count;
    entry.increment(error);
    if (error)
      return false;
  }
  return count == expected.size();
}

int test_atomic_writer(FileWriterTestCase test_case) {
  // Only synthetic data under an exclusively created temporary root. No
  // biometric context/provider, installed application data, or user key is used.
  const std::wstring root_suffix = random_suffix();
  PV_TEST_CHECK(!root_suffix.empty());
  std::error_code error;
  const std::filesystem::path temporary_parent =
      std::filesystem::temp_directory_path(error);
  PV_TEST_CHECK(!error);
  const std::filesystem::path root =
      temporary_parent / (std::wstring(L"passvault-file-test-") + root_suffix);
  TestContextFixture fixture(root);
  fixture.owns_root = std::filesystem::create_directory(root, error);
  PV_TEST_CHECK(fixture.owns_root && !error);
  PV_TEST_CHECK(ensure_safe_directory(root));

  const std::filesystem::path destination = root / kMetadataFileName;
  const std::wstring fixed_suffix = L"0360000000000001";
  const std::filesystem::path temporary =
      destination.wstring() + L".tmp." + fixed_suffix;
  const std::vector<uint8_t> original{0x01, 0x00, 0x7f, 0xff};
  const std::vector<uint8_t> replacement{0x02, 0xff, 0x00, 0x80, 0x03};
  const std::vector<uint8_t> sentinel{0x53, 0x00, 0x45, 0xff};
  PV_TEST_CHECK(write_secure_file_atomic(root, destination, original));
  PV_TEST_CHECK(file_has_bytes(destination, original));
  PV_TEST_CHECK(directory_has_only(root, {destination}));

  if (test_case == FileWriterTestCase::collision) {
    PV_TEST_CHECK(write_secure_file_atomic(root, temporary, sentinel));
    const DWORD attributes = GetFileAttributesW(temporary.c_str());
    PV_TEST_CHECK(attributes != INVALID_FILE_ATTRIBUTES &&
                  (attributes & FILE_ATTRIBUTE_READONLY) == 0);
    // Prove DELETE access is allowed, then CLOSE the probe. A lingering handle
    // denying delete sharing would hide the old writer's unowned deletion.
    WindowsHandle probe(CreateFileW(
        temporary.c_str(), DELETE, FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE,
        nullptr, OPEN_EXISTING, FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
    PV_TEST_CHECK(probe.valid());
    PV_TEST_CHECK(probe.close());
    PV_TEST_CHECK(file_has_bytes(temporary, sentinel));
    PV_TEST_CHECK(directory_has_only(root, {destination, temporary}));
  }

  WindowsHandle blocker(test_case == FileWriterTestCase::rename_failure
                            ? CreateFileW(destination.c_str(), GENERIC_READ,
                                          FILE_SHARE_READ, nullptr, OPEN_EXISTING,
                                          FILE_FLAG_OPEN_REPARSE_POINT, nullptr)
                            : INVALID_HANDLE_VALUE);
  PV_TEST_CHECK(test_case != FileWriterTestCase::rename_failure || blocker.valid());

  std::vector<uint8_t> payload = replacement;
  if (test_case == FileWriterTestCase::empty_payload)
    payload.clear();
  if (test_case == FileWriterTestCase::oversized_payload)
    payload.assign(kMaxEnvelopeBytes + 1, 0x5a);

  int suffix_calls = 0;
  int validation_calls = 0;
  int protection_calls = 0;
  bool saw_valid_empty_file = false;
  bool saw_empty_file_for_dacl = false;
  bool saw_no_inheritance = false;
  const bool result = write_secure_file_atomic_impl(
      root, destination, payload,
      [&]() {
        ++suffix_calls;
        return test_case == FileWriterTestCase::empty_suffix ? std::wstring{}
                                                            : fixed_suffix;
      },
      [&](HANDLE handle) {
        ++validation_calls;
        LARGE_INTEGER size{};
        const bool valid = safe_handle(handle);
        saw_valid_empty_file = valid && GetFileSizeEx(handle, &size) && size.QuadPart == 0;
        return valid && test_case != FileWriterTestCase::validation_failure;
      },
      [&](HANDLE handle, DWORD inheritance) {
        ++protection_calls;
        LARGE_INTEGER size{};
        saw_empty_file_for_dacl = GetFileSizeEx(handle, &size) && size.QuadPart == 0;
        saw_no_inheritance = inheritance == NO_INHERITANCE;
        return test_case != FileWriterTestCase::dacl_failure &&
               apply_current_user_only_dacl(handle, inheritance);
      });
  // This real destination handle (not a fake MoveFileExW) prevents replacement.
  PV_TEST_CHECK(blocker.close());
  PV_TEST_CHECK(result == (test_case == FileWriterTestCase::success));

  const bool invalid_payload = test_case == FileWriterTestCase::empty_payload ||
                               test_case == FileWriterTestCase::oversized_payload;
  const bool created = !invalid_payload && test_case != FileWriterTestCase::empty_suffix &&
                       test_case != FileWriterTestCase::collision;
  const bool protected_file = created && test_case != FileWriterTestCase::validation_failure;
  PV_TEST_CHECK(suffix_calls == (invalid_payload ? 0 : 1));
  PV_TEST_CHECK(validation_calls == (created ? 1 : 0));
  PV_TEST_CHECK(protection_calls == (protected_file ? 1 : 0));
  PV_TEST_CHECK(!created || saw_valid_empty_file);
  PV_TEST_CHECK(!protected_file || (saw_empty_file_for_dacl && saw_no_inheritance));
  PV_TEST_CHECK(file_has_bytes(destination,
                               test_case == FileWriterTestCase::success ? replacement : original));

  // These are the decisive cleanup/ownership assertions. They run BEFORE
  // fixture teardown, which must not erase a regression's evidence first.
  if (test_case == FileWriterTestCase::collision) {
    PV_TEST_CHECK(file_has_bytes(temporary, sentinel));
    PV_TEST_CHECK(directory_has_only(root, {destination, temporary}));
  } else {
    PV_TEST_CHECK(!std::filesystem::exists(temporary, error) && !error);
    PV_TEST_CHECK(directory_has_only(root, {destination}));
  }

  if (test_case == FileWriterTestCase::success) {
    const std::vector<uint8_t> maximum(kMaxEnvelopeBytes, 0x5a);
    PV_TEST_CHECK(write_secure_file_atomic(root, destination, maximum));
    PV_TEST_CHECK(file_has_bytes(destination, maximum));
    PV_TEST_CHECK(directory_has_only(root, {destination}));
  }
  if (test_case == FileWriterTestCase::rename_failure) {
    // Once the blocker is closed, the same real writer can replace the file.
    PV_TEST_CHECK(write_secure_file_atomic(root, destination, replacement));
    PV_TEST_CHECK(file_has_bytes(destination, replacement));
    PV_TEST_CHECK(directory_has_only(root, {destination}));
  }
  PV_TEST_CHECK(fixture.close());
  PV_TEST_CHECK(!std::filesystem::exists(root, error) && !error);
  return 0;
}

// These synthetic cases exercise the production guard with live caller-owned
// arrays. Explicit bad_alloc is an unwind test, not real allocation exhaustion,
// WebAuthn/CNG fault injection, or a probe of freed memory.
static_assert(std::is_nothrow_constructible_v<
              ScopedArrayWipe<kHashBytes>,
              std::array<uint8_t, kHashBytes> &>);
static_assert(std::is_nothrow_destructible_v<ScopedArrayWipe<kHashBytes>>);
static_assert(!std::is_copy_constructible_v<ScopedArrayWipe<kHashBytes>>);
static_assert(!std::is_copy_assignable_v<ScopedArrayWipe<kHashBytes>>);
static_assert(!std::is_move_constructible_v<ScopedArrayWipe<kHashBytes>>);
static_assert(!std::is_move_assignable_v<ScopedArrayWipe<kHashBytes>>);

enum class SecretWipeTestCase {
  normal_scope,
  early_return,
  allocation_exception,
  nested_exception,
};

std::optional<SecretWipeTestCase>
parse_secret_wipe_case(std::string_view value) {
  if (value == "normal_scope")
    return SecretWipeTestCase::normal_scope;
  if (value == "early_return")
    return SecretWipeTestCase::early_return;
  if (value == "allocation_exception")
    return SecretWipeTestCase::allocation_exception;
  if (value == "nested_exception")
    return SecretWipeTestCase::nested_exception;
  return std::nullopt;
}

int test_scoped_array_wipe(SecretWipeTestCase test_case) {
  std::array<uint8_t, kPrfBytes> prf{};
  std::array<uint8_t, kHashBytes> wrapping_key{};
  std::array<uint8_t, kHashBytes> pseudorandom_key{};
  std::array<uint8_t, kHashBytes> unrelated{};
  unrelated.fill(0x91);
  if (test_case == SecretWipeTestCase::normal_scope) {
    {
      const ScopedArrayWipe<kPrfBytes> wipe_prf(prf);
      prf.fill(0xa5);
      PV_TEST_CHECK(prf.front() == 0xa5);
    }
  } else if (test_case == SecretWipeTestCase::early_return) {
    const bool returned = [&prf]() {
      const ScopedArrayWipe<kPrfBytes> wipe_prf(prf);
      prf.fill(0xa5);
      return true;
    }();
    PV_TEST_CHECK(returned);
  } else {
    bool caught = false;
    try {
      const ScopedArrayWipe<kPrfBytes> wipe_prf(prf);
      prf.fill(0xa5);
      const ScopedArrayWipe<kHashBytes> wipe_wrapping_key(wrapping_key);
      if (test_case == SecretWipeTestCase::allocation_exception) {
        // Mirrors the first ownership cut: the PRF is filled, the key is zero.
        throw std::bad_alloc();
      }
      PV_TEST_CHECK(test_case == SecretWipeTestCase::nested_exception);
      wrapping_key.fill(0x5a);
      [&pseudorandom_key]() {
        const ScopedArrayWipe<kHashBytes> wipe_prk(pseudorandom_key);
        pseudorandom_key.fill(0x3c);
        throw std::bad_alloc();
      }();
    } catch (const std::bad_alloc &) {
      caught = true;
    }
    PV_TEST_CHECK(caught);
  }
  PV_TEST_CHECK(std::all_of(prf.begin(), prf.end(),
                            [](uint8_t value) { return value == 0; }));
  PV_TEST_CHECK(std::all_of(wrapping_key.begin(), wrapping_key.end(),
                            [](uint8_t value) { return value == 0; }));
  PV_TEST_CHECK(std::all_of(pseudorandom_key.begin(), pseudorandom_key.end(),
                            [](uint8_t value) { return value == 0; }));
  PV_TEST_CHECK(std::all_of(unrelated.begin(), unrelated.end(),
                            [](uint8_t value) { return value == 0x91; }));
  return 0;
}

int main(int argc, char *argv[]) {
  if (argc == 3 && std::string_view(argv[1]) == "--file-case") {
    const auto test_case = parse_file_writer_case(argv[2]);
    PV_TEST_CHECK(test_case.has_value());
    return test_atomic_writer(*test_case);
  }
  if (argc == 3 && std::string_view(argv[1]) == "--secret-wipe-case") {
    const auto test_case = parse_secret_wipe_case(argv[2]);
    PV_TEST_CHECK(test_case.has_value());
    return test_scoped_array_wipe(*test_case);
  }
  PV_TEST_CHECK(argc == 1);
  const std::wstring context_suffix = random_suffix();
  PV_TEST_CHECK(!context_suffix.empty());
  const std::filesystem::path context_root =
      std::filesystem::temp_directory_path() /
      (std::wstring(L"passvault-biometric-context-") + context_suffix);
  TestContextFixture fixture(context_root);
  std::error_code filesystem_error;
  fixture.owns_root =
      std::filesystem::create_directory(context_root, filesystem_error);
  PV_TEST_CHECK(fixture.owns_root && !filesystem_error);
  const std::u8string context_root_utf8 = context_root.u8string();
  PV_TEST_CHECK(
      pv_bio_create(reinterpret_cast<const char *>(context_root_utf8.data()),
                    context_root_utf8.size(), &fixture.context) == PV_BIO_OK);
  PV_TEST_CHECK(fixture.context != nullptr);
  PV_TEST_CHECK(!std::filesystem::exists(fixture.context->metadata_path,
                                        filesystem_error));
  PV_TEST_CHECK(!filesystem_error);

  // A fresh fixture has no metadata. Retrieval returns NOT_ENABLED before
  // Windows Hello availability, credential inventory, or prompting. All other
  // arguments are valid: bypassing reason validation must change the malformed
  // cases from INTERNAL_ERROR to NOT_ENABLED, not pass via a null-context guard.
  // Do not use enrollment as a control: it consults OS capability first.
  std::array<uint8_t, kHashBytes> fixture_vault_hash{};
  fixture_vault_hash.fill(0x5a);
  std::array<uint8_t, PV_BIO_VAULT_KEY_BYTES> rejected_output;
  rejected_output.fill(0xa5);
  PV_TEST_CHECK(pv_bio_retrieve(
                    fixture.context, 1, fixture_vault_hash.data(),
                    fixture_vault_hash.size(), rejected_output.data(),
                    rejected_output.size()) == PV_BIO_NOT_ENABLED);
  PV_TEST_CHECK(std::all_of(rejected_output.begin(), rejected_output.end(),
                            [](uint8_t byte) { return byte == 0; }));
  const auto check_reason = [&](const char *reason, size_t reason_length,
                                pv_bio_status expected) {
    rejected_output.fill(0xa5);
    const pv_bio_status result = pv_bio_retrieve_localized(
        fixture.context, 1, fixture_vault_hash.data(), fixture_vault_hash.size(),
        rejected_output.data(), rejected_output.size(), reason, reason_length);
    return result == expected &&
           std::all_of(rejected_output.begin(), rejected_output.end(),
                       [](uint8_t byte) { return byte == 0; });
  };
  constexpr char english_reason[] = "Unlock PassVault";
  // UTF-8 Arabic, independent of the compiler's source code page.
  constexpr char arabic_reason[] = "\xd9\x81\xd8\xaa\xd8\xad PassVault";
  const std::string maximum_reason(PV_BIO_MAX_PROMPT_REASON_BYTES, 'r');
  PV_TEST_CHECK(check_reason("r", 1, PV_BIO_NOT_ENABLED));
  PV_TEST_CHECK(check_reason(english_reason, sizeof(english_reason) - 1,
                             PV_BIO_NOT_ENABLED));
  PV_TEST_CHECK(check_reason(arabic_reason, sizeof(arabic_reason) - 1,
                             PV_BIO_NOT_ENABLED));
  PV_TEST_CHECK(check_reason(maximum_reason.data(), maximum_reason.size(),
                             PV_BIO_NOT_ENABLED));

  constexpr char embedded_nul_reason[] = {'a', 0, 'b'};
  constexpr char malformed_reason[] = "a\xc3\x28";
  constexpr char truncated_reason[] = "\xd8";
  constexpr char overlong_reason[] = "\xc0\xaf";
  constexpr char surrogate_reason[] = "\xed\xa0\x80";
  const std::string oversized_reason(PV_BIO_MAX_PROMPT_REASON_BYTES + 1, 'r');
  PV_TEST_CHECK(check_reason(nullptr, 0, PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(nullptr, 1, PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason("", 0, PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(embedded_nul_reason, sizeof(embedded_nul_reason),
                             PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(malformed_reason, sizeof(malformed_reason) - 1,
                             PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(truncated_reason, sizeof(truncated_reason) - 1,
                             PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(overlong_reason, sizeof(overlong_reason) - 1,
                             PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(surrogate_reason, sizeof(surrogate_reason) - 1,
                             PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(check_reason(oversized_reason.data(), oversized_reason.size(),
                             PV_BIO_INTERNAL_ERROR));
  PV_TEST_CHECK(fixture.context->active_operation == 0);
  PV_TEST_CHECK(!std::filesystem::exists(fixture.context->metadata_path,
                                        filesystem_error));
  PV_TEST_CHECK(!filesystem_error);
  PV_TEST_CHECK(fixture.close());

  PV_TEST_CHECK(credential_inventory_is_authoritative(PV_BIO_AVAILABLE));
  PV_TEST_CHECK(
      !credential_inventory_is_authoritative(PV_BIO_AVAILABILITY_NOT_ENROLLED));
  PV_TEST_CHECK(
      !credential_inventory_is_authoritative(PV_BIO_AVAILABILITY_LOCKED_OUT));
  PV_TEST_CHECK(
      !credential_inventory_is_authoritative(PV_BIO_AVAILABILITY_UNAVAILABLE));
  PV_TEST_CHECK(availability_to_operation_status(PV_BIO_AVAILABLE) ==
                PV_BIO_OK);
  PV_TEST_CHECK(availability_to_operation_status(
                    PV_BIO_AVAILABILITY_NOT_ENROLLED) == PV_BIO_NOT_ENROLLED);
  PV_TEST_CHECK(availability_to_operation_status(
                    PV_BIO_AVAILABILITY_LOCKED_OUT) == PV_BIO_LOCKED_OUT);
  PV_TEST_CHECK(availability_to_operation_status(
                    PV_BIO_AVAILABILITY_UNAVAILABLE) == PV_BIO_NOT_AVAILABLE);

  Envelope envelope;
  for (size_t index = 0; index < envelope.vault_hash.size(); ++index) {
    envelope.vault_hash[index] = static_cast<uint8_t>(index + 1);
    envelope.relying_party_hash[index] = static_cast<uint8_t>(index + 11);
    envelope.public_x[index] = static_cast<uint8_t>(index + 21);
    envelope.public_y[index] = static_cast<uint8_t>(index + 31);
    envelope.prf_salt[index] = static_cast<uint8_t>(index + 41);
    envelope.kdf_salt[index] = static_cast<uint8_t>(index + 51);
  }
  for (size_t index = 0; index < envelope.nonce.size(); ++index) {
    envelope.nonce[index] = static_cast<uint8_t>(index + 61);
  }
  envelope.credential_id = {0x10, 0x20, 0x30, 0x40};

  std::array<uint8_t, kPrfBytes> prf{};
  std::array<uint8_t, PV_BIO_VAULT_KEY_BYTES> vault_key{};
  for (size_t index = 0; index < prf.size(); ++index) {
    prf[index] = static_cast<uint8_t>(index + 71);
    vault_key[index] = static_cast<uint8_t>(index + 81);
  }
  std::array<uint8_t, kHashBytes> wrapping_key{};
  PV_TEST_CHECK(derive_wrapping_key(prf, envelope.kdf_salt, envelope.vault_hash,
                                    &wrapping_key));
  std::vector<uint8_t> aad = encode_envelope_aad(envelope);
  PV_TEST_CHECK(aes_gcm_encrypt(wrapping_key, envelope.nonce, aad,
                                vault_key.data(), vault_key.size(),
                                &envelope.ciphertext, &envelope.tag));

  std::vector<uint8_t> encoded = encode_envelope(envelope);
  Envelope decoded;
  PV_TEST_CHECK(decode_envelope(encoded, &decoded));
  std::array<uint8_t, kHashBytes> recovered_wrapping_key{};
  PV_TEST_CHECK(derive_wrapping_key(prf, decoded.kdf_salt, decoded.vault_hash,
                                    &recovered_wrapping_key));
  std::vector<uint8_t> recovered_aad = encode_envelope_aad(decoded);
  std::array<uint8_t, PV_BIO_VAULT_KEY_BYTES> recovered{};
  PV_TEST_CHECK(aes_gcm_decrypt(recovered_wrapping_key, decoded.nonce,
                                recovered_aad, decoded.ciphertext, decoded.tag,
                                recovered.data(), recovered.size()));
  PV_TEST_CHECK(
      fixed_time_equal(recovered.data(), vault_key.data(), vault_key.size()));

  auto tampered_tag = decoded.tag;
  tampered_tag[0] ^= 0x01;
  recovered.fill(0xa5);
  PV_TEST_CHECK(!aes_gcm_decrypt(
      recovered_wrapping_key, decoded.nonce, recovered_aad, decoded.ciphertext,
      tampered_tag, recovered.data(), recovered.size()));
  PV_TEST_CHECK(std::all_of(recovered.begin(), recovered.end(),
                            [](uint8_t value) { return value == 0; }));

  std::vector<uint8_t> malformed = encoded;
  malformed.pop_back();
  Envelope malformed_envelope;
  PV_TEST_CHECK(!decode_envelope(malformed, &malformed_envelope));

  std::vector<uint8_t> swapped = encoded;
  constexpr size_t vault_hash_offset =
      kEnvelopeMagic.size() + 2 * sizeof(uint32_t);
  swapped[vault_hash_offset] ^= 0x01;
  Envelope swapped_envelope;
  PV_TEST_CHECK(decode_envelope(swapped, &swapped_envelope));
  std::array<uint8_t, kHashBytes> swapped_key{};
  PV_TEST_CHECK(derive_wrapping_key(prf, swapped_envelope.kdf_salt,
                                    swapped_envelope.vault_hash, &swapped_key));
  std::vector<uint8_t> swapped_aad = encode_envelope_aad(swapped_envelope);
  recovered.fill(0xa5);
  PV_TEST_CHECK(!aes_gcm_decrypt(swapped_key, swapped_envelope.nonce,
                                 swapped_aad, swapped_envelope.ciphertext,
                                 swapped_envelope.tag, recovered.data(),
                                 recovered.size()));

  WEBAUTHN_RP_ENTITY_INFORMATION relying_party{};
  relying_party.dwVersion = WEBAUTHN_RP_ENTITY_INFORMATION_CURRENT_VERSION;
  relying_party.pwszId = kRelyingPartyId;
  WEBAUTHN_USER_ENTITY_INFORMATION user{};
  user.dwVersion = WEBAUTHN_USER_ENTITY_INFORMATION_CURRENT_VERSION;
  user.cbId = static_cast<DWORD>(envelope.vault_hash.size());
  user.pbId = envelope.vault_hash.data();
  WEBAUTHN_CREDENTIAL_DETAILS detail{};
  detail.dwVersion = WEBAUTHN_CREDENTIAL_DETAILS_VERSION_2;
  detail.cbCredentialID = static_cast<DWORD>(envelope.credential_id.size());
  detail.pbCredentialID = envelope.credential_id.data();
  detail.pRpInformation = &relying_party;
  detail.pUserInformation = &user;
  detail.bRemovable = TRUE;
  detail.bBackedUp = FALSE;
  PV_TEST_CHECK(
      credential_identity_matches(&detail, envelope.vault_hash.data()));
  PV_TEST_CHECK(credential_is_removable_and_device_bound(&detail));
  auto different_vault = envelope.vault_hash;
  different_vault[0] ^= 0x01;
  PV_TEST_CHECK(!credential_identity_matches(&detail, different_vault.data()));
  detail.bBackedUp = TRUE;
  PV_TEST_CHECK(!credential_is_removable_and_device_bound(&detail));

  std::array<uint8_t, 37> authenticator_data{};
  std::copy(envelope.relying_party_hash.begin(),
            envelope.relying_party_hash.end(), authenticator_data.begin());
  authenticator_data[32] =
      kAuthenticatorUserPresent | kAuthenticatorUserVerified;
  PV_TEST_CHECK(valid_assertion_authenticator_data(
      authenticator_data.data(), authenticator_data.size(),
      envelope.relying_party_hash));
  authenticator_data[32] |= kAuthenticatorBackupEligible;
  PV_TEST_CHECK(!valid_assertion_authenticator_data(
      authenticator_data.data(), authenticator_data.size(),
      envelope.relying_party_hash));

  secure_wipe(prf);
  secure_wipe(vault_key);
  secure_wipe(wrapping_key);
  secure_wipe(aad);
  secure_wipe(encoded);
  wipe_envelope(&decoded);
  secure_wipe(recovered_wrapping_key);
  secure_wipe(recovered_aad);
  secure_wipe(recovered);
  secure_wipe(malformed);
  wipe_envelope(&malformed_envelope);
  secure_wipe(swapped);
  wipe_envelope(&swapped_envelope);
  secure_wipe(swapped_key);
  secure_wipe(swapped_aad);
  wipe_envelope(&envelope);
  return 0;
}
