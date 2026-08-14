// Intentionally compile the reviewed implementation into this test-only
// executable. This keeps its anonymous-namespace crypto and parser helpers
// directly testable without exporting a production test ABI.
#include "../../src/windows/passvault_biometric_windows.cpp"

#define PV_TEST_CHECK(condition)                                               \
  do {                                                                         \
    if (!(condition))                                                          \
      return __LINE__;                                                         \
  } while (false)

int main() {
  const std::wstring context_suffix = random_suffix();
  PV_TEST_CHECK(!context_suffix.empty());
  const std::filesystem::path context_root =
      std::filesystem::temp_directory_path() /
      (std::wstring(L"passvault-biometric-context-") + context_suffix);
  std::error_code filesystem_error;
  PV_TEST_CHECK(
      std::filesystem::create_directory(context_root, filesystem_error));
  const std::u8string context_root_utf8 = context_root.u8string();
  pv_bio_context *context = nullptr;
  PV_TEST_CHECK(
      pv_bio_create(reinterpret_cast<const char *>(context_root_utf8.data()),
                    context_root_utf8.size(), &context) == PV_BIO_OK);
  PV_TEST_CHECK(context != nullptr);
  pv_bio_destroy(context);
  std::filesystem::remove_all(context_root, filesystem_error);
  PV_TEST_CHECK(!filesystem_error);

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
