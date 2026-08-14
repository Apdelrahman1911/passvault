#ifndef PASSVAULT_BIOMETRIC_H
#define PASSVAULT_BIOMETRIC_H

#include <stddef.h>
#include <stdint.h>

#if defined(_WIN32)
#if defined(PASSVAULT_BIOMETRIC_BUILD)
#define PV_BIO_API __declspec(dllexport)
#else
#define PV_BIO_API __declspec(dllimport)
#endif
#define PV_BIO_CALL __cdecl
#else
#define PV_BIO_API __attribute__((visibility("default")))
#define PV_BIO_CALL
#endif

#ifdef __cplusplus
extern "C" {
#endif

#define PV_BIO_ABI_VERSION 1u
#define PV_BIO_VAULT_HASH_BYTES 32u
#define PV_BIO_VAULT_KEY_BYTES 32u

typedef struct pv_bio_context pv_bio_context;

typedef enum pv_bio_status {
  PV_BIO_OK = 0,
  PV_BIO_CANCELLED = 1,
  PV_BIO_NOT_AVAILABLE = 2,
  PV_BIO_NOT_ENROLLED = 3,
  PV_BIO_LOCKED_OUT = 4,
  PV_BIO_NOT_ENABLED = 5,
  PV_BIO_INVALIDATED = 6,
  PV_BIO_AUTHENTICATION_FAILED = 7,
  PV_BIO_BUSY = 8,
  PV_BIO_INTERNAL_ERROR = 9,
  PV_BIO_BUFFER_TOO_SMALL = 10
} pv_bio_status;

typedef enum pv_bio_availability {
  PV_BIO_AVAILABLE = 0,
  PV_BIO_AVAILABILITY_NOT_ENROLLED = 1,
  PV_BIO_AVAILABILITY_LOCKED_OUT = 2,
  PV_BIO_AVAILABILITY_UNAVAILABLE = 3
} pv_bio_availability;

PV_BIO_API uint32_t PV_BIO_CALL pv_bio_abi_version(void);

PV_BIO_API pv_bio_status PV_BIO_CALL
pv_bio_create(const char *data_directory_utf8, size_t data_directory_length,
              pv_bio_context **out_context);

PV_BIO_API void PV_BIO_CALL pv_bio_destroy(pv_bio_context *context);

PV_BIO_API pv_bio_status PV_BIO_CALL
pv_bio_set_parent_window(pv_bio_context *context, uintptr_t native_window);

PV_BIO_API pv_bio_status PV_BIO_CALL
pv_bio_get_capability(pv_bio_context *context, int32_t *out_availability);

PV_BIO_API pv_bio_status PV_BIO_CALL pv_bio_contains(pv_bio_context *context,
                                                     const uint8_t *vault_hash,
                                                     size_t vault_hash_length,
                                                     int32_t *out_contains);

PV_BIO_API pv_bio_status PV_BIO_CALL pv_bio_enroll(pv_bio_context *context,
                                                   uint64_t operation_id,
                                                   const uint8_t *vault_hash,
                                                   size_t vault_hash_length,
                                                   const uint8_t *vault_key,
                                                   size_t vault_key_length);

PV_BIO_API pv_bio_status PV_BIO_CALL
pv_bio_retrieve(pv_bio_context *context, uint64_t operation_id,
                const uint8_t *vault_hash, size_t vault_hash_length,
                uint8_t *out_vault_key, size_t out_vault_key_length);

PV_BIO_API pv_bio_status PV_BIO_CALL pv_bio_delete(pv_bio_context *context,
                                                   const uint8_t *vault_hash,
                                                   size_t vault_hash_length);

PV_BIO_API pv_bio_status PV_BIO_CALL pv_bio_cancel(pv_bio_context *context,
                                                   uint64_t operation_id);

#ifdef __cplusplus
}
#endif

#endif
