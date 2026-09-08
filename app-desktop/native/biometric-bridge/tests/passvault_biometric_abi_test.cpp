#include "passvault_biometric.h"

int main() {
  if (pv_bio_abi_version() != PV_BIO_ABI_VERSION) {
    return 1;
  }
  // Resolve the additive symbols without opening a context or an OS prompt.
  constexpr char reason[] = "Synthetic ABI prompt";
  if (pv_bio_enroll_localized(nullptr, 1, nullptr, 0, nullptr, 0, reason,
                               sizeof(reason) - 1) != PV_BIO_INTERNAL_ERROR) {
    return 2;
  }
  uint8_t output[PV_BIO_VAULT_KEY_BYTES];
  for (auto &byte : output) {
    byte = 0xa5;
  }
  if (pv_bio_retrieve_localized(nullptr, 1, nullptr, 0, output,
                                 sizeof(output), reason,
                                 sizeof(reason) - 1) != PV_BIO_INTERNAL_ERROR) {
    return 3;
  }
  for (const auto byte : output) {
    if (byte != 0) {
      return 4;
    }
  }
  return 0;
}
