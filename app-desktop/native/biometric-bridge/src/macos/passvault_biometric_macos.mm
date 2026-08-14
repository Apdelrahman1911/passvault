#include "passvault_biometric.h"

#import <Foundation/Foundation.h>
#import <LocalAuthentication/LocalAuthentication.h>
#import <Security/Security.h>

#include <array>
#include <atomic>
#include <cerrno>
#include <condition_variable>
#include <cstdint>
#include <cstring>
#include <fcntl.h>
#include <memory>
#include <mutex>
#include <string>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

namespace {

constexpr std::array<uint8_t, 8> kMetadataMagic = {'P', 'V', 'M', 'B',
                                                   'I', 'O', '1', 0};
constexpr size_t kNonceBytes = 16;
constexpr size_t kMetadataBytes =
    kMetadataMagic.size() + PV_BIO_VAULT_HASH_BYTES + kNonceBytes;
constexpr char kService[] = "com.passvault.desktop.biometric-unlock.v1";

struct Metadata {
  std::array<uint8_t, PV_BIO_VAULT_HASH_BYTES> vault_hash{};
  std::array<uint8_t, kNonceBytes> nonce{};
};

void secure_wipe(void *value, size_t length) {
  if (value != nullptr && length > 0) {
    auto *bytes = static_cast<volatile uint8_t *>(value);
    for (size_t index = 0; index < length; ++index) {
      bytes[index] = 0;
    }
    std::atomic_signal_fence(std::memory_order_seq_cst);
  }
}

bool valid_vault_hash(const uint8_t *value, size_t length) {
  return value != nullptr && length == PV_BIO_VAULT_HASH_BYTES;
}

std::string hexadecimal(const uint8_t *value, size_t length) {
  static constexpr char digits[] = "0123456789abcdef";
  std::string result(length * 2, '0');
  for (size_t index = 0; index < length; ++index) {
    const uint8_t byte = value[index];
    result[index * 2] = digits[byte >> 4];
    result[index * 2 + 1] = digits[byte & 0x0f];
  }
  return result;
}

std::string account_for(const Metadata &metadata) {
  return hexadecimal(metadata.vault_hash.data(), metadata.vault_hash.size()) +
         "." + hexadecimal(metadata.nonce.data(), metadata.nonce.size());
}

bool write_all(int descriptor, const uint8_t *bytes, size_t length) {
  size_t offset = 0;
  while (offset < length) {
    const ssize_t written = write(descriptor, bytes + offset, length - offset);
    if (written < 0 && errno == EINTR) {
      continue;
    }
    if (written <= 0) {
      return false;
    }
    offset += static_cast<size_t>(written);
  }
  return true;
}

bool read_all(int descriptor, uint8_t *bytes, size_t length) {
  size_t offset = 0;
  while (offset < length) {
    const ssize_t count = read(descriptor, bytes + offset, length - offset);
    if (count < 0 && errno == EINTR) {
      continue;
    }
    if (count <= 0) {
      return false;
    }
    offset += static_cast<size_t>(count);
  }
  uint8_t extra = 0;
  return read(descriptor, &extra, 1) == 0;
}

bool is_owned_directory(const std::string &path) {
  struct stat info{};
  return lstat(path.c_str(), &info) == 0 && S_ISDIR(info.st_mode) &&
         !S_ISLNK(info.st_mode) && info.st_uid == geteuid();
}

bool ensure_owned_directory(const std::string &path) {
  if (mkdir(path.c_str(), 0700) != 0 && errno != EEXIST) {
    return false;
  }
  if (!is_owned_directory(path)) {
    return false;
  }
  return chmod(path.c_str(), 0700) == 0;
}

void fsync_directory(const std::string &path) {
  const int descriptor = open(path.c_str(), O_RDONLY | O_DIRECTORY | O_CLOEXEC);
  if (descriptor >= 0) {
    static_cast<void>(fsync(descriptor));
    static_cast<void>(close(descriptor));
  }
}

std::array<uint8_t, kMetadataBytes> encode_metadata(const Metadata &metadata) {
  std::array<uint8_t, kMetadataBytes> encoded{};
  size_t offset = 0;
  std::memcpy(encoded.data() + offset, kMetadataMagic.data(),
              kMetadataMagic.size());
  offset += kMetadataMagic.size();
  std::memcpy(encoded.data() + offset, metadata.vault_hash.data(),
              metadata.vault_hash.size());
  offset += metadata.vault_hash.size();
  std::memcpy(encoded.data() + offset, metadata.nonce.data(),
              metadata.nonce.size());
  return encoded;
}

bool decode_metadata(const std::array<uint8_t, kMetadataBytes> &encoded,
                     Metadata *out) {
  if (out == nullptr || std::memcmp(encoded.data(), kMetadataMagic.data(),
                                    kMetadataMagic.size()) != 0) {
    return false;
  }
  size_t offset = kMetadataMagic.size();
  std::memcpy(out->vault_hash.data(), encoded.data() + offset,
              out->vault_hash.size());
  offset += out->vault_hash.size();
  std::memcpy(out->nonce.data(), encoded.data() + offset, out->nonce.size());
  return true;
}

enum class MetadataReadResult {
  present,
  missing,
  invalid,
};

MetadataReadResult read_metadata(const std::string &path, Metadata *out) {
  struct stat info{};
  if (lstat(path.c_str(), &info) != 0) {
    return errno == ENOENT ? MetadataReadResult::missing
                           : MetadataReadResult::invalid;
  }
  if (!S_ISREG(info.st_mode) || S_ISLNK(info.st_mode) ||
      info.st_uid != geteuid() || (info.st_mode & 0077) != 0 ||
      info.st_size != static_cast<off_t>(kMetadataBytes)) {
    return MetadataReadResult::invalid;
  }
  const int descriptor = open(path.c_str(), O_RDONLY | O_NOFOLLOW | O_CLOEXEC);
  if (descriptor < 0) {
    return MetadataReadResult::invalid;
  }
  std::array<uint8_t, kMetadataBytes> encoded{};
  const bool read = read_all(descriptor, encoded.data(), encoded.size());
  const bool closed = close(descriptor) == 0;
  const bool decoded = read && closed && decode_metadata(encoded, out);
  secure_wipe(encoded.data(), encoded.size());
  return decoded ? MetadataReadResult::present : MetadataReadResult::invalid;
}

bool write_metadata_atomic(const std::string &directory,
                           const std::string &destination,
                           const Metadata &metadata) {
  std::array<uint8_t, 8> random_suffix{};
  if (SecRandomCopyBytes(kSecRandomDefault, random_suffix.size(),
                         random_suffix.data()) != errSecSuccess) {
    return false;
  }
  const std::string temporary =
      destination + ".tmp." +
      hexadecimal(random_suffix.data(), random_suffix.size());
  secure_wipe(random_suffix.data(), random_suffix.size());
  const int descriptor =
      open(temporary.c_str(),
           O_WRONLY | O_CREAT | O_EXCL | O_NOFOLLOW | O_CLOEXEC, 0600);
  if (descriptor < 0) {
    return false;
  }
  const auto encoded = encode_metadata(metadata);
  const bool written = write_all(descriptor, encoded.data(), encoded.size());
  const bool synced = written && fsync(descriptor) == 0;
  const bool closed = close(descriptor) == 0;
  if (!synced || !closed ||
      rename(temporary.c_str(), destination.c_str()) != 0) {
    static_cast<void>(unlink(temporary.c_str()));
    return false;
  }
  static_cast<void>(chmod(destination.c_str(), 0600));
  fsync_directory(directory);
  return true;
}

NSMutableDictionary *base_keychain_query(NSString *account) {
  return [@{
    (__bridge id)kSecClass : (__bridge id)kSecClassGenericPassword,
    (__bridge id)kSecAttrService : [NSString stringWithUTF8String:kService],
    (__bridge id)kSecAttrAccount : account,
    (__bridge id)kSecAttrSynchronizable : @NO,
    (__bridge id)kSecUseDataProtectionKeychain : @YES,
  } mutableCopy];
}

pv_bio_status map_la_error(NSError *error) {
  if (error == nil) {
    return PV_BIO_AUTHENTICATION_FAILED;
  }
  switch (error.code) {
  case LAErrorUserCancel:
  case LAErrorUserFallback:
  case LAErrorSystemCancel:
  case LAErrorAppCancel:
    return PV_BIO_CANCELLED;
  case LAErrorBiometryNotEnrolled:
    return PV_BIO_NOT_ENROLLED;
  case LAErrorBiometryLockout:
    return PV_BIO_LOCKED_OUT;
  case LAErrorBiometryNotAvailable:
  case LAErrorPasscodeNotSet:
    return PV_BIO_NOT_AVAILABLE;
  default:
    return PV_BIO_AUTHENTICATION_FAILED;
  }
}

pv_bio_status map_keychain_status(OSStatus status,
                                  bool missing_is_not_enabled) {
  switch (status) {
  case errSecSuccess:
    return PV_BIO_OK;
  case errSecUserCanceled:
    return PV_BIO_CANCELLED;
  case errSecItemNotFound:
  case errSecDecode:
    return missing_is_not_enabled ? PV_BIO_NOT_ENABLED : PV_BIO_INVALIDATED;
  case errSecNotAvailable:
  case errSecInteractionNotAllowed:
    return PV_BIO_NOT_AVAILABLE;
  case errSecAuthFailed:
    return PV_BIO_AUTHENTICATION_FAILED;
  default:
    return PV_BIO_INTERNAL_ERROR;
  }
}

pv_bio_status delete_keychain_account(const std::string &account) {
  NSString *account_string =
      [[NSString alloc] initWithBytes:account.data()
                               length:account.size()
                             encoding:NSUTF8StringEncoding];
  if (account_string == nil) {
    return PV_BIO_INTERNAL_ERROR;
  }
  const OSStatus status = SecItemDelete(
      (__bridge CFDictionaryRef)base_keychain_query(account_string));
  return status == errSecItemNotFound ? PV_BIO_OK
                                      : map_keychain_status(status, true);
}

pv_bio_status delete_keychain_service_items() {
  NSDictionary *query = @{
    (__bridge id)kSecClass : (__bridge id)kSecClassGenericPassword,
    (__bridge id)kSecAttrService : [NSString stringWithUTF8String:kService],
    (__bridge id)kSecAttrSynchronizable : @NO,
    (__bridge id)kSecUseDataProtectionKeychain : @YES,
  };
  const OSStatus status = SecItemDelete((__bridge CFDictionaryRef)query);
  return status == errSecItemNotFound ? PV_BIO_OK
                                      : map_keychain_status(status, true);
}

enum class KeychainPresence {
  present,
  missing,
  invalidated,
  error,
};

KeychainPresence keychain_account_presence(const std::string &account) {
  NSString *account_string =
      [[NSString alloc] initWithBytes:account.data()
                               length:account.size()
                             encoding:NSUTF8StringEncoding];
  if (account_string == nil) {
    return KeychainPresence::error;
  }
  NSMutableDictionary *query = base_keychain_query(account_string);
  query[(__bridge id)kSecReturnAttributes] = @YES;
  query[(__bridge id)kSecMatchLimit] = (__bridge id)kSecMatchLimitOne;
  LAContext *auth_context = [[LAContext alloc] init];
  auth_context.interactionNotAllowed = YES;
  query[(__bridge id)kSecUseAuthenticationContext] = auth_context;
  CFTypeRef result = nullptr;
  const OSStatus status =
      SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
  if (result != nullptr) {
    CFRelease(result);
  }
  switch (status) {
  case errSecSuccess:
  case errSecInteractionNotAllowed:
    return KeychainPresence::present;
  case errSecItemNotFound:
    return KeychainPresence::missing;
  case errSecAuthFailed:
  case errSecDecode:
    return KeychainPresence::invalidated;
  default:
    return KeychainPresence::error;
  }
}

} // namespace

struct pv_bio_context {
  std::string biometric_directory;
  std::string metadata_path;
  std::mutex operation_mutex;
  std::condition_variable operation_finished;
  uint64_t active_operation = 0;
  uint64_t pending_cancellation = 0;
  LAContext *active_context = nil;
  bool cancellation_requested = false;
  bool operation_committed = false;
  bool closing = false;
};

namespace {

bool begin_operation(pv_bio_context *context, uint64_t operation_id,
                     LAContext *auth_context) {
  if (context == nullptr || operation_id == 0 || auth_context == nil) {
    return false;
  }
  std::lock_guard lock(context->operation_mutex);
  if (context->closing || context->active_operation != 0) {
    return false;
  }
  context->active_operation = operation_id;
  context->active_context = auth_context;
  context->cancellation_requested =
      context->pending_cancellation == operation_id;
  context->pending_cancellation = 0;
  context->operation_committed = false;
  return true;
}

bool operation_was_cancelled(pv_bio_context *context, uint64_t operation_id) {
  std::lock_guard lock(context->operation_mutex);
  return context->active_operation != operation_id ||
         context->cancellation_requested || context->closing;
}

bool commit_operation(pv_bio_context *context, uint64_t operation_id) {
  std::lock_guard lock(context->operation_mutex);
  if (context->active_operation != operation_id ||
      context->cancellation_requested || context->closing) {
    return false;
  }
  context->operation_committed = true;
  return true;
}

void finish_operation(pv_bio_context *context, uint64_t operation_id) {
  std::lock_guard lock(context->operation_mutex);
  if (context->active_operation == operation_id) {
    context->active_context = nil;
    context->active_operation = 0;
    context->cancellation_requested = false;
    context->operation_committed = false;
    context->operation_finished.notify_all();
  }
}

class OperationGuard final {
public:
  OperationGuard(pv_bio_context *context, uint64_t operation_id)
      : context_(context), operation_id_(operation_id) {}
  ~OperationGuard() { finish_operation(context_, operation_id_); }
  OperationGuard(const OperationGuard &) = delete;
  OperationGuard &operator=(const OperationGuard &) = delete;

private:
  pv_bio_context *context_;
  uint64_t operation_id_;
};

void configure_biometric_context(LAContext *auth_context, NSString *reason) {
  auth_context.localizedReason = reason;
  // PassVault's fallback is the master-password field in the application. A
  // LocalAuthentication "Use Password" action only reports
  // LAErrorUserFallback to a biometrics-only policy; it cannot release the
  // biometry-current-set Keychain item and previously looked like a dead
  // button. Hide that misleading native fallback while keeping cancellation
  // mapped to the normal password path in the application.
  auth_context.localizedFallbackTitle = @"";
  auth_context.touchIDAuthenticationAllowableReuseDuration = 0;
}

pv_bio_status authenticate_for_enrollment(LAContext *auth_context) {
  configure_biometric_context(
      auth_context, @"Enable Touch ID unlock for this vault");
  dispatch_semaphore_t completion = dispatch_semaphore_create(0);
  __block BOOL authenticated = NO;
  __block NSError *authentication_error = nil;
  [auth_context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
               localizedReason:auth_context.localizedReason
                         reply:^(BOOL success, NSError *error) {
                           authenticated = success;
                           authentication_error = error;
                           dispatch_semaphore_signal(completion);
                         }];
  dispatch_semaphore_wait(completion, DISPATCH_TIME_FOREVER);
  return authenticated ? PV_BIO_OK : map_la_error(authentication_error);
}

pv_bio_status add_keychain_item(const Metadata &metadata,
                                const uint8_t *vault_key,
                                size_t vault_key_length) {
  const std::string account = account_for(metadata);
  NSString *account_string =
      [[NSString alloc] initWithBytes:account.data()
                               length:account.size()
                             encoding:NSUTF8StringEncoding];
  if (account_string == nil) {
    return PV_BIO_INTERNAL_ERROR;
  }
  CFErrorRef access_error = nullptr;
  SecAccessControlRef access_control = SecAccessControlCreateWithFlags(
      kCFAllocatorDefault, kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
      kSecAccessControlBiometryCurrentSet, &access_error);
  if (access_error != nullptr) {
    CFRelease(access_error);
  }
  if (access_control == nullptr) {
    return PV_BIO_NOT_AVAILABLE;
  }
  NSMutableData *key_data = [NSMutableData dataWithLength:vault_key_length];
  if (key_data == nil || key_data.mutableBytes == nullptr) {
    CFRelease(access_control);
    return PV_BIO_INTERNAL_ERROR;
  }
  std::memcpy(key_data.mutableBytes, vault_key, vault_key_length);
  NSMutableDictionary *query = base_keychain_query(account_string);
  query[(__bridge id)kSecAttrAccessControl] = (__bridge id)access_control;
  query[(__bridge id)kSecValueData] = key_data;
  const OSStatus status = SecItemAdd((__bridge CFDictionaryRef)query, nullptr);
  secure_wipe(key_data.mutableBytes, key_data.length);
  CFRelease(access_control);
  return map_keychain_status(status, false);
}

pv_bio_status remove_metadata_and_item(pv_bio_context *context,
                                       const Metadata &metadata) {
  const pv_bio_status deleted = delete_keychain_account(account_for(metadata));
  if (deleted != PV_BIO_OK) {
    return deleted;
  }
  if (unlink(context->metadata_path.c_str()) != 0 && errno != ENOENT) {
    return PV_BIO_INTERNAL_ERROR;
  }
  fsync_directory(context->biometric_directory);
  return PV_BIO_OK;
}

pv_bio_status remove_metadata_and_service_items(pv_bio_context *context) {
  const pv_bio_status deleted = delete_keychain_service_items();
  if (deleted != PV_BIO_OK) {
    return deleted;
  }
  if (unlink(context->metadata_path.c_str()) != 0 && errno != ENOENT) {
    return PV_BIO_INTERNAL_ERROR;
  }
  fsync_directory(context->biometric_directory);
  return PV_BIO_OK;
}

template <typename Operation>
pv_bio_status guarded_status(Operation &&operation) noexcept {
  try {
    return operation();
  } catch (...) {
    return PV_BIO_INTERNAL_ERROR;
  }
}

template <typename Operation>
void guarded_void(Operation &&operation) noexcept {
  try {
    operation();
  } catch (...) {
    // Never allow a C++ exception to cross the reviewed C ABI boundary.
  }
}

} // namespace

extern "C" {

uint32_t PV_BIO_CALL pv_bio_abi_version(void) { return PV_BIO_ABI_VERSION; }

pv_bio_status PV_BIO_CALL pv_bio_create(const char *data_directory_utf8,
                                        size_t data_directory_length,
                                        pv_bio_context **out_context) {
  @autoreleasepool {
    if (out_context != nullptr) {
      *out_context = nullptr;
    }
    return guarded_status([&]() -> pv_bio_status {
      if (data_directory_utf8 == nullptr || data_directory_length == 0 ||
          data_directory_length > 4096 || out_context == nullptr ||
          std::memchr(data_directory_utf8, 0, data_directory_length) !=
              nullptr) {
        return PV_BIO_INTERNAL_ERROR;
      }
      std::string data_directory(data_directory_utf8, data_directory_length);
      if (!is_owned_directory(data_directory)) {
        return PV_BIO_INTERNAL_ERROR;
      }
      const std::string biometric_directory = data_directory + "/biometric";
      if (!ensure_owned_directory(biometric_directory)) {
        return PV_BIO_INTERNAL_ERROR;
      }
      std::unique_ptr<pv_bio_context> context(new (std::nothrow)
                                                  pv_bio_context());
      if (context == nullptr) {
        return PV_BIO_INTERNAL_ERROR;
      }
      context->biometric_directory = biometric_directory;
      context->metadata_path = biometric_directory + "/macos-v1.meta";
      *out_context = context.release();
      return PV_BIO_OK;
    });
  }
}

void PV_BIO_CALL pv_bio_destroy(pv_bio_context *context) {
  @autoreleasepool {
    guarded_void([&] {
      if (context == nullptr) {
        return;
      }
      std::unique_lock lock(context->operation_mutex);
      context->closing = true;
      context->cancellation_requested = true;
      if (context->active_context != nil && !context->operation_committed) {
        [context->active_context invalidate];
      }
      context->operation_finished.wait(
          lock, [context] { return context->active_operation == 0; });
      context->active_context = nil;
      lock.unlock();
      delete context;
    });
  }
}

pv_bio_status PV_BIO_CALL pv_bio_set_parent_window(pv_bio_context *context,
                                                   uintptr_t native_window) {
  static_cast<void>(native_window);
  return context == nullptr ? PV_BIO_INTERNAL_ERROR : PV_BIO_OK;
}

pv_bio_status PV_BIO_CALL pv_bio_get_capability(pv_bio_context *context,
                                                int32_t *out_availability) {
  @autoreleasepool {
    return guarded_status([&]() -> pv_bio_status {
      if (context == nullptr || out_availability == nullptr) {
        return PV_BIO_INTERNAL_ERROR;
      }
      LAContext *auth_context = [[LAContext alloc] init];
      NSError *error = nil;
      const BOOL available = [auth_context
          canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
                      error:&error];
      if (available && auth_context.biometryType == LABiometryTypeTouchID) {
        *out_availability = PV_BIO_AVAILABLE;
      } else if (error.code == LAErrorBiometryNotEnrolled) {
        *out_availability = PV_BIO_AVAILABILITY_NOT_ENROLLED;
      } else if (error.code == LAErrorBiometryLockout) {
        *out_availability = PV_BIO_AVAILABILITY_LOCKED_OUT;
      } else {
        *out_availability = PV_BIO_AVAILABILITY_UNAVAILABLE;
      }
      return PV_BIO_OK;
    });
  }
}

pv_bio_status PV_BIO_CALL pv_bio_contains(pv_bio_context *context,
                                          const uint8_t *vault_hash,
                                          size_t vault_hash_length,
                                          int32_t *out_contains) {
  @autoreleasepool {
    if (out_contains != nullptr) {
      *out_contains = 0;
    }
    return guarded_status([&]() -> pv_bio_status {
      if (context == nullptr ||
          !valid_vault_hash(vault_hash, vault_hash_length) ||
          out_contains == nullptr) {
        return PV_BIO_INTERNAL_ERROR;
      }
      Metadata metadata{};
      const MetadataReadResult result =
          read_metadata(context->metadata_path, &metadata);
      if (result == MetadataReadResult::missing) {
        *out_contains = 0;
        return PV_BIO_OK;
      }
      if (result != MetadataReadResult::present) {
        *out_contains = 0;
        return PV_BIO_INVALIDATED;
      }
      const bool matching = std::memcmp(metadata.vault_hash.data(), vault_hash,
                                        metadata.vault_hash.size()) == 0;
      if (!matching) {
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_INVALIDATED;
      }
      const KeychainPresence presence =
          keychain_account_presence(account_for(metadata));
      if (presence == KeychainPresence::invalidated) {
        static_cast<void>(remove_metadata_and_item(context, metadata));
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_INVALIDATED;
      }
      if (presence == KeychainPresence::error) {
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_INTERNAL_ERROR;
      }
      if (presence == KeychainPresence::missing) {
        static_cast<void>(unlink(context->metadata_path.c_str()));
        fsync_directory(context->biometric_directory);
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_INVALIDATED;
      }
      *out_contains = 1;
      secure_wipe(&metadata, sizeof(metadata));
      return PV_BIO_OK;
    });
  }
}

pv_bio_status PV_BIO_CALL pv_bio_enroll(pv_bio_context *context,
                                        uint64_t operation_id,
                                        const uint8_t *vault_hash,
                                        size_t vault_hash_length,
                                        const uint8_t *vault_key,
                                        size_t vault_key_length) {
  @autoreleasepool {
    return guarded_status([&]() -> pv_bio_status {
      if (context == nullptr ||
          !valid_vault_hash(vault_hash, vault_hash_length) ||
          vault_key == nullptr || vault_key_length != PV_BIO_VAULT_KEY_BYTES) {
        return PV_BIO_INTERNAL_ERROR;
      }
      LAContext *auth_context = [[LAContext alloc] init];
      if (!begin_operation(context, operation_id, auth_context)) {
        return PV_BIO_BUSY;
      }
      OperationGuard operation(context, operation_id);
      if (operation_was_cancelled(context, operation_id)) {
        finish_operation(context, operation_id);
        return PV_BIO_CANCELLED;
      }
      const pv_bio_status authentication =
          authenticate_for_enrollment(auth_context);
      if (authentication != PV_BIO_OK ||
          operation_was_cancelled(context, operation_id)) {
        finish_operation(context, operation_id);
        return authentication == PV_BIO_OK ? PV_BIO_CANCELLED : authentication;
      }

      Metadata previous{};
      MetadataReadResult previous_result =
          read_metadata(context->metadata_path, &previous);
      if (previous_result == MetadataReadResult::invalid) {
        const pv_bio_status cleanup =
            remove_metadata_and_service_items(context);
        if (cleanup != PV_BIO_OK) {
          finish_operation(context, operation_id);
          secure_wipe(&previous, sizeof(previous));
          return cleanup;
        }
        previous_result = MetadataReadResult::missing;
      }
      Metadata replacement{};
      std::memcpy(replacement.vault_hash.data(), vault_hash,
                  replacement.vault_hash.size());
      if (SecRandomCopyBytes(kSecRandomDefault, replacement.nonce.size(),
                             replacement.nonce.data()) != errSecSuccess) {
        finish_operation(context, operation_id);
        secure_wipe(&previous, sizeof(previous));
        secure_wipe(&replacement, sizeof(replacement));
        return PV_BIO_INTERNAL_ERROR;
      }
      pv_bio_status status =
          add_keychain_item(replacement, vault_key, vault_key_length);
      if (status == PV_BIO_OK &&
          operation_was_cancelled(context, operation_id)) {
        static_cast<void>(delete_keychain_account(account_for(replacement)));
        status = PV_BIO_CANCELLED;
      }
      if (status == PV_BIO_OK &&
          !write_metadata_atomic(context->biometric_directory,
                                 context->metadata_path, replacement)) {
        static_cast<void>(delete_keychain_account(account_for(replacement)));
        status = PV_BIO_INTERNAL_ERROR;
      }
      if (status == PV_BIO_OK && !commit_operation(context, operation_id)) {
        bool rolled_back = false;
        if (previous_result == MetadataReadResult::present) {
          rolled_back = write_metadata_atomic(context->biometric_directory,
                                              context->metadata_path, previous);
        } else {
          rolled_back =
              unlink(context->metadata_path.c_str()) == 0 || errno == ENOENT;
          fsync_directory(context->biometric_directory);
        }
        static_cast<void>(delete_keychain_account(account_for(replacement)));
        status = rolled_back ? PV_BIO_CANCELLED : PV_BIO_INTERNAL_ERROR;
      }
      if (status == PV_BIO_OK &&
          previous_result == MetadataReadResult::present) {
        const pv_bio_status previous_deleted =
            delete_keychain_account(account_for(previous));
        if (previous_deleted != PV_BIO_OK) {
          // Preserve the previous authoritative enrollment if cleanup
          // fails instead of reporting success with an orphaned item.
          const bool restored = write_metadata_atomic(
              context->biometric_directory, context->metadata_path, previous);
          if (restored) {
            static_cast<void>(
                delete_keychain_account(account_for(replacement)));
          }
          status = PV_BIO_INTERNAL_ERROR;
        }
      }
      finish_operation(context, operation_id);
      secure_wipe(&previous, sizeof(previous));
      secure_wipe(&replacement, sizeof(replacement));
      return status;
    });
  }
}

pv_bio_status PV_BIO_CALL pv_bio_retrieve(pv_bio_context *context,
                                          uint64_t operation_id,
                                          const uint8_t *vault_hash,
                                          size_t vault_hash_length,
                                          uint8_t *out_vault_key,
                                          size_t out_vault_key_length) {
  @autoreleasepool {
    if (out_vault_key != nullptr &&
        out_vault_key_length == PV_BIO_VAULT_KEY_BYTES) {
      secure_wipe(out_vault_key, out_vault_key_length);
    }
    const pv_bio_status guarded_result = guarded_status([&]() -> pv_bio_status {
      if (context == nullptr ||
          !valid_vault_hash(vault_hash, vault_hash_length) ||
          out_vault_key == nullptr ||
          out_vault_key_length != PV_BIO_VAULT_KEY_BYTES) {
        return out_vault_key_length < PV_BIO_VAULT_KEY_BYTES
                   ? PV_BIO_BUFFER_TOO_SMALL
                   : PV_BIO_INTERNAL_ERROR;
      }
      Metadata metadata{};
      const MetadataReadResult metadata_result =
          read_metadata(context->metadata_path, &metadata);
      if (metadata_result == MetadataReadResult::missing) {
        return PV_BIO_NOT_ENABLED;
      }
      if (metadata_result != MetadataReadResult::present ||
          std::memcmp(metadata.vault_hash.data(), vault_hash,
                      metadata.vault_hash.size()) != 0) {
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_INVALIDATED;
      }
      LAContext *auth_context = [[LAContext alloc] init];
      configure_biometric_context(auth_context,
                                  @"Unlock PassVault with Touch ID");
      if (!begin_operation(context, operation_id, auth_context)) {
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_BUSY;
      }
      OperationGuard operation(context, operation_id);
      if (operation_was_cancelled(context, operation_id)) {
        finish_operation(context, operation_id);
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_CANCELLED;
      }
      const std::string account = account_for(metadata);
      NSString *account_string =
          [[NSString alloc] initWithBytes:account.data()
                                   length:account.size()
                                 encoding:NSUTF8StringEncoding];
      pv_bio_status result_status = PV_BIO_INTERNAL_ERROR;
      if (account_string != nil) {
        NSMutableDictionary *query = base_keychain_query(account_string);
        query[(__bridge id)kSecReturnData] = @YES;
        query[(__bridge id)kSecMatchLimit] = (__bridge id)kSecMatchLimitOne;
        query[(__bridge id)kSecUseAuthenticationContext] = auth_context;
        CFTypeRef result = nullptr;
        const OSStatus status =
            SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
        result_status = map_keychain_status(status, false);
        if (status == errSecSuccess && result != nullptr &&
            CFGetTypeID(result) == CFDataGetTypeID()) {
          const CFDataRef data = static_cast<CFDataRef>(result);
          if (CFDataGetLength(data) == PV_BIO_VAULT_KEY_BYTES &&
              CFDataGetBytePtr(data) != nullptr) {
            std::memcpy(out_vault_key, CFDataGetBytePtr(data),
                        PV_BIO_VAULT_KEY_BYTES);
            result_status = PV_BIO_OK;
          } else {
            result_status = PV_BIO_INVALIDATED;
          }
        }
        if (result != nullptr) {
          CFRelease(result);
        }
      }
      if (result_status == PV_BIO_OK &&
          !commit_operation(context, operation_id)) {
        result_status = PV_BIO_CANCELLED;
      }
      finish_operation(context, operation_id);
      if (result_status != PV_BIO_OK) {
        secure_wipe(out_vault_key, out_vault_key_length);
      }
      if (result_status == PV_BIO_INVALIDATED) {
        static_cast<void>(remove_metadata_and_item(context, metadata));
      }
      secure_wipe(&metadata, sizeof(metadata));
      return result_status;
    });
    if (guarded_result != PV_BIO_OK && out_vault_key != nullptr &&
        out_vault_key_length == PV_BIO_VAULT_KEY_BYTES) {
      secure_wipe(out_vault_key, out_vault_key_length);
    }
    return guarded_result;
  }
}

pv_bio_status PV_BIO_CALL pv_bio_delete(pv_bio_context *context,
                                        const uint8_t *vault_hash,
                                        size_t vault_hash_length) {
  @autoreleasepool {
    return guarded_status([&]() -> pv_bio_status {
      if (context == nullptr ||
          !valid_vault_hash(vault_hash, vault_hash_length)) {
        return PV_BIO_INTERNAL_ERROR;
      }
      Metadata metadata{};
      const MetadataReadResult result =
          read_metadata(context->metadata_path, &metadata);
      if (result == MetadataReadResult::missing) {
        return delete_keychain_service_items();
      }
      if (result != MetadataReadResult::present) {
        return remove_metadata_and_service_items(context);
      }
      if (std::memcmp(metadata.vault_hash.data(), vault_hash,
                      metadata.vault_hash.size()) != 0) {
        secure_wipe(&metadata, sizeof(metadata));
        return PV_BIO_INVALIDATED;
      }
      const pv_bio_status status = remove_metadata_and_service_items(context);
      secure_wipe(&metadata, sizeof(metadata));
      return status;
    });
  }
}

pv_bio_status PV_BIO_CALL pv_bio_cancel(pv_bio_context *context,
                                        uint64_t operation_id) {
  @autoreleasepool {
    return guarded_status([&]() -> pv_bio_status {
      if (context == nullptr || operation_id == 0) {
        return PV_BIO_INTERNAL_ERROR;
      }
      std::lock_guard lock(context->operation_mutex);
      if (context->active_operation == operation_id &&
          !context->operation_committed) {
        context->cancellation_requested = true;
      } else if (context->active_operation == 0 && !context->closing) {
        context->pending_cancellation = operation_id;
      }
      if (context->active_operation == operation_id &&
          context->active_context != nil && !context->operation_committed) {
        [context->active_context invalidate];
      }
      return PV_BIO_OK;
    });
  }
}

} // extern "C"
