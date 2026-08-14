#include "passvault_biometric.h"

#include <Windows.h>
#include <aclapi.h>
#include <bcrypt.h>
#include <ncrypt.h>
#include <objbase.h>
#include <webauthn.h>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Security.Credentials.UI.h>
#include <winrt/base.h>

#include <algorithm>
#include <array>
#include <atomic>
#include <condition_variable>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <cwchar>
#include <filesystem>
#include <limits>
#include <memory>
#include <mutex>
#include <optional>
#include <string>
#include <string_view>
#include <vector>

namespace {

constexpr std::array<uint8_t, 8> kEnvelopeMagic = {'P', 'V', 'W', 'H',
                                                   'L', 'O', '1', 0};
constexpr uint32_t kEnvelopeVersion = 1;
constexpr size_t kHashBytes = 32;
constexpr size_t kChallengeBytes = 32;
constexpr size_t kPrfBytes = WEBAUTHN_CTAP_ONE_HMAC_SECRET_LENGTH;
constexpr size_t kSaltBytes = 32;
constexpr size_t kNonceBytes = 12;
constexpr size_t kTagBytes = 16;
constexpr size_t kPublicCoordinateBytes = 32;
constexpr size_t kMaxCredentialIdBytes = 1024;
constexpr size_t kMaxEnvelopeBytes = 16 * 1024;
constexpr DWORD kPromptTimeoutMilliseconds = 120'000;
constexpr uint8_t kAuthenticatorUserPresent = 0x01;
constexpr uint8_t kAuthenticatorUserVerified = 0x04;
constexpr uint8_t kAuthenticatorBackupEligible = 0x08;
constexpr uint8_t kAuthenticatorBackedUp = 0x10;
constexpr uint8_t kAuthenticatorAttestedData = 0x40;
constexpr wchar_t kRelyingPartyId[] = L"passvault.kiramanga.me";
constexpr char kKdfInfo[] = "PassVault Windows Hello vault-key wrapping v1";
constexpr wchar_t kMetadataFileName[] = L"windows-v1.dat";
constexpr wchar_t kBiometricDirectoryName[] = L"biometric";

void secure_wipe(void *value, size_t length) {
  if (value != nullptr && length > 0) {
    auto *bytes = static_cast<volatile uint8_t *>(value);
    for (size_t index = 0; index < length; ++index) {
      bytes[index] = 0;
    }
    std::atomic_signal_fence(std::memory_order_seq_cst);
  }
}

template <typename Container> void secure_wipe(Container &value) {
  secure_wipe(value.data(),
              value.size() * sizeof(typename Container::value_type));
}

bool valid_vault_hash(const uint8_t *value, size_t length) {
  return value != nullptr && length == PV_BIO_VAULT_HASH_BYTES;
}

bool fixed_time_equal(const uint8_t *first, const uint8_t *second,
                      size_t length) {
  if (first == nullptr || second == nullptr) {
    return false;
  }
  uint8_t difference = 0;
  for (size_t index = 0; index < length; ++index) {
    difference |= static_cast<uint8_t>(first[index] ^ second[index]);
  }
  return difference == 0;
}

bool nt_success(NTSTATUS status) { return status >= 0; }

class AlgorithmHandle final {
public:
  AlgorithmHandle() = default;
  ~AlgorithmHandle() {
    if (value_ != nullptr) {
      BCryptCloseAlgorithmProvider(value_, 0);
    }
  }
  AlgorithmHandle(const AlgorithmHandle &) = delete;
  AlgorithmHandle &operator=(const AlgorithmHandle &) = delete;

  BCRYPT_ALG_HANDLE *output() { return &value_; }
  BCRYPT_ALG_HANDLE get() const { return value_; }

private:
  BCRYPT_ALG_HANDLE value_ = nullptr;
};

class HashHandle final {
public:
  HashHandle() = default;
  ~HashHandle() {
    if (value_ != nullptr) {
      BCryptDestroyHash(value_);
    }
  }
  HashHandle(const HashHandle &) = delete;
  HashHandle &operator=(const HashHandle &) = delete;

  BCRYPT_HASH_HANDLE *output() { return &value_; }
  BCRYPT_HASH_HANDLE get() const { return value_; }

private:
  BCRYPT_HASH_HANDLE value_ = nullptr;
};

class KeyHandle final {
public:
  KeyHandle() = default;
  ~KeyHandle() {
    if (value_ != nullptr) {
      BCryptDestroyKey(value_);
    }
  }
  KeyHandle(const KeyHandle &) = delete;
  KeyHandle &operator=(const KeyHandle &) = delete;

  BCRYPT_KEY_HANDLE *output() { return &value_; }
  BCRYPT_KEY_HANDLE get() const { return value_; }

private:
  BCRYPT_KEY_HANDLE value_ = nullptr;
};

bool random_bytes(uint8_t *output, size_t length) {
  if (output == nullptr || length == 0 ||
      length > std::numeric_limits<ULONG>::max()) {
    return false;
  }
  return nt_success(BCryptGenRandom(nullptr, output, static_cast<ULONG>(length),
                                    BCRYPT_USE_SYSTEM_PREFERRED_RNG));
}

bool get_algorithm_object_length(BCRYPT_ALG_HANDLE algorithm, DWORD *output) {
  DWORD written = 0;
  return output != nullptr &&
         nt_success(BCryptGetProperty(algorithm, BCRYPT_OBJECT_LENGTH,
                                      reinterpret_cast<PUCHAR>(output),
                                      sizeof(*output), &written, 0)) &&
         written == sizeof(*output);
}

bool sha256(const uint8_t *data, size_t length,
            std::array<uint8_t, kHashBytes> *output) {
  if (output == nullptr || (data == nullptr && length != 0) ||
      length > std::numeric_limits<ULONG>::max()) {
    return false;
  }
  AlgorithmHandle algorithm;
  if (!nt_success(BCryptOpenAlgorithmProvider(
          algorithm.output(), BCRYPT_SHA256_ALGORITHM, nullptr, 0))) {
    return false;
  }
  DWORD object_length = 0;
  if (!get_algorithm_object_length(algorithm.get(), &object_length) ||
      object_length == 0) {
    return false;
  }
  std::vector<uint8_t> hash_object(object_length);
  HashHandle hash;
  bool success = nt_success(BCryptCreateHash(algorithm.get(), hash.output(),
                                             hash_object.data(), object_length,
                                             nullptr, 0, 0));
  if (success && length > 0) {
    success = nt_success(BCryptHashData(hash.get(), const_cast<PUCHAR>(data),
                                        static_cast<ULONG>(length), 0));
  }
  if (success) {
    success = nt_success(BCryptFinishHash(
        hash.get(), output->data(), static_cast<ULONG>(output->size()), 0));
  }
  secure_wipe(hash_object);
  return success;
}

bool hmac_sha256(const uint8_t *key, size_t key_length, const uint8_t *data,
                 size_t data_length, std::array<uint8_t, kHashBytes> *output) {
  if (key == nullptr || key_length == 0 || output == nullptr ||
      (data == nullptr && data_length != 0) ||
      key_length > std::numeric_limits<ULONG>::max() ||
      data_length > std::numeric_limits<ULONG>::max()) {
    return false;
  }
  AlgorithmHandle algorithm;
  if (!nt_success(BCryptOpenAlgorithmProvider(algorithm.output(),
                                              BCRYPT_SHA256_ALGORITHM, nullptr,
                                              BCRYPT_ALG_HANDLE_HMAC_FLAG))) {
    return false;
  }
  DWORD object_length = 0;
  if (!get_algorithm_object_length(algorithm.get(), &object_length) ||
      object_length == 0) {
    return false;
  }
  std::vector<uint8_t> hash_object(object_length);
  HashHandle hash;
  bool success = nt_success(BCryptCreateHash(
      algorithm.get(), hash.output(), hash_object.data(), object_length,
      const_cast<PUCHAR>(key), static_cast<ULONG>(key_length), 0));
  if (success && data_length > 0) {
    success = nt_success(BCryptHashData(hash.get(), const_cast<PUCHAR>(data),
                                        static_cast<ULONG>(data_length), 0));
  }
  if (success) {
    success = nt_success(BCryptFinishHash(
        hash.get(), output->data(), static_cast<ULONG>(output->size()), 0));
  }
  secure_wipe(hash_object);
  return success;
}

bool derive_wrapping_key(const std::array<uint8_t, kPrfBytes> &prf,
                         const std::array<uint8_t, kSaltBytes> &salt,
                         const std::array<uint8_t, kHashBytes> &vault_hash,
                         std::array<uint8_t, kHashBytes> *output) {
  if (output == nullptr) {
    return false;
  }
  std::array<uint8_t, kHashBytes> pseudorandom_key{};
  std::vector<uint8_t> info;
  info.reserve(sizeof(kKdfInfo) - 1 + vault_hash.size() + 1);
  info.insert(info.end(), kKdfInfo, kKdfInfo + sizeof(kKdfInfo) - 1);
  info.insert(info.end(), vault_hash.begin(), vault_hash.end());
  info.push_back(1);
  const bool success =
      hmac_sha256(salt.data(), salt.size(), prf.data(), prf.size(),
                  &pseudorandom_key) &&
      hmac_sha256(pseudorandom_key.data(), pseudorandom_key.size(), info.data(),
                  info.size(), output);
  secure_wipe(pseudorandom_key);
  secure_wipe(info);
  return success;
}

bool configure_aes_gcm(AlgorithmHandle *algorithm) {
  if (algorithm == nullptr ||
      !nt_success(BCryptOpenAlgorithmProvider(
          algorithm->output(), BCRYPT_AES_ALGORITHM, nullptr, 0))) {
    return false;
  }
  return nt_success(BCryptSetProperty(
      algorithm->get(), BCRYPT_CHAINING_MODE,
      reinterpret_cast<PUCHAR>(const_cast<wchar_t *>(BCRYPT_CHAIN_MODE_GCM)),
      static_cast<ULONG>(sizeof(BCRYPT_CHAIN_MODE_GCM)), 0));
}

bool aes_gcm_encrypt(const std::array<uint8_t, kHashBytes> &key,
                     const std::array<uint8_t, kNonceBytes> &nonce,
                     const std::vector<uint8_t> &aad, const uint8_t *plaintext,
                     size_t plaintext_length, std::vector<uint8_t> *ciphertext,
                     std::array<uint8_t, kTagBytes> *tag) {
  if (plaintext == nullptr || plaintext_length == 0 || ciphertext == nullptr ||
      tag == nullptr || plaintext_length > std::numeric_limits<ULONG>::max() ||
      aad.size() > std::numeric_limits<ULONG>::max()) {
    return false;
  }
  AlgorithmHandle algorithm;
  if (!configure_aes_gcm(&algorithm)) {
    return false;
  }
  DWORD object_length = 0;
  if (!get_algorithm_object_length(algorithm.get(), &object_length) ||
      object_length == 0) {
    return false;
  }
  std::vector<uint8_t> key_object(object_length);
  KeyHandle aes_key;
  bool success = nt_success(BCryptGenerateSymmetricKey(
      algorithm.get(), aes_key.output(), key_object.data(), object_length,
      const_cast<PUCHAR>(key.data()), static_cast<ULONG>(key.size()), 0));
  ciphertext->assign(plaintext_length, 0);
  BCRYPT_AUTHENTICATED_CIPHER_MODE_INFO authentication{};
  BCRYPT_INIT_AUTH_MODE_INFO(authentication);
  authentication.pbNonce = const_cast<PUCHAR>(nonce.data());
  authentication.cbNonce = static_cast<ULONG>(nonce.size());
  authentication.pbAuthData = const_cast<PUCHAR>(aad.data());
  authentication.cbAuthData = static_cast<ULONG>(aad.size());
  authentication.pbTag = tag->data();
  authentication.cbTag = static_cast<ULONG>(tag->size());
  ULONG written = 0;
  if (success) {
    success = nt_success(BCryptEncrypt(
                  aes_key.get(), const_cast<PUCHAR>(plaintext),
                  static_cast<ULONG>(plaintext_length), &authentication,
                  nullptr, 0, ciphertext->data(),
                  static_cast<ULONG>(ciphertext->size()), &written, 0)) &&
              written == static_cast<ULONG>(ciphertext->size());
  }
  secure_wipe(key_object);
  if (!success) {
    secure_wipe(*ciphertext);
    ciphertext->clear();
    secure_wipe(*tag);
  }
  return success;
}

bool aes_gcm_decrypt(const std::array<uint8_t, kHashBytes> &key,
                     const std::array<uint8_t, kNonceBytes> &nonce,
                     const std::vector<uint8_t> &aad,
                     const std::vector<uint8_t> &ciphertext,
                     const std::array<uint8_t, kTagBytes> &tag,
                     uint8_t *plaintext, size_t plaintext_length) {
  if (plaintext == nullptr || plaintext_length != ciphertext.size() ||
      plaintext_length == 0 ||
      plaintext_length > std::numeric_limits<ULONG>::max() ||
      aad.size() > std::numeric_limits<ULONG>::max()) {
    return false;
  }
  AlgorithmHandle algorithm;
  if (!configure_aes_gcm(&algorithm)) {
    return false;
  }
  DWORD object_length = 0;
  if (!get_algorithm_object_length(algorithm.get(), &object_length) ||
      object_length == 0) {
    return false;
  }
  std::vector<uint8_t> key_object(object_length);
  KeyHandle aes_key;
  bool success = nt_success(BCryptGenerateSymmetricKey(
      algorithm.get(), aes_key.output(), key_object.data(), object_length,
      const_cast<PUCHAR>(key.data()), static_cast<ULONG>(key.size()), 0));
  BCRYPT_AUTHENTICATED_CIPHER_MODE_INFO authentication{};
  BCRYPT_INIT_AUTH_MODE_INFO(authentication);
  authentication.pbNonce = const_cast<PUCHAR>(nonce.data());
  authentication.cbNonce = static_cast<ULONG>(nonce.size());
  authentication.pbAuthData = const_cast<PUCHAR>(aad.data());
  authentication.cbAuthData = static_cast<ULONG>(aad.size());
  authentication.pbTag = const_cast<PUCHAR>(tag.data());
  authentication.cbTag = static_cast<ULONG>(tag.size());
  ULONG written = 0;
  if (success) {
    success =
        nt_success(BCryptDecrypt(
            aes_key.get(), const_cast<PUCHAR>(ciphertext.data()),
            static_cast<ULONG>(ciphertext.size()), &authentication, nullptr, 0,
            plaintext, static_cast<ULONG>(plaintext_length), &written, 0)) &&
        written == static_cast<ULONG>(plaintext_length);
  }
  secure_wipe(key_object);
  if (!success) {
    secure_wipe(plaintext, plaintext_length);
  }
  return success;
}

std::optional<std::wstring> utf8_to_utf16(const char *value, size_t length) {
  if (value == nullptr || length == 0 || length > 4096 ||
      length > static_cast<size_t>(std::numeric_limits<int>::max()) ||
      std::memchr(value, 0, length) != nullptr) {
    return std::nullopt;
  }
  const int required =
      MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value,
                          static_cast<int>(length), nullptr, 0);
  if (required <= 0) {
    return std::nullopt;
  }
  std::wstring result(static_cast<size_t>(required), L'\0');
  if (MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value,
                          static_cast<int>(length), result.data(),
                          required) != required) {
    secure_wipe(result);
    return std::nullopt;
  }
  return result;
}

class WindowsHandle final {
public:
  explicit WindowsHandle(HANDLE value = INVALID_HANDLE_VALUE) : value_(value) {}
  ~WindowsHandle() {
    if (valid()) {
      CloseHandle(value_);
    }
  }
  WindowsHandle(const WindowsHandle &) = delete;
  WindowsHandle &operator=(const WindowsHandle &) = delete;

  bool valid() const {
    return value_ != nullptr && value_ != INVALID_HANDLE_VALUE;
  }
  HANDLE get() const { return value_; }
  bool close() {
    if (!valid()) {
      return true;
    }
    const bool closed = CloseHandle(value_) != FALSE;
    value_ = INVALID_HANDLE_VALUE;
    return closed;
  }

private:
  HANDLE value_;
};

bool owned_by_current_user(HANDLE handle) {
  if (handle == nullptr || handle == INVALID_HANDLE_VALUE) {
    return false;
  }
  PSID owner = nullptr;
  PSECURITY_DESCRIPTOR descriptor = nullptr;
  if (GetSecurityInfo(handle, SE_FILE_OBJECT, OWNER_SECURITY_INFORMATION,
                      &owner, nullptr, nullptr, nullptr,
                      &descriptor) != ERROR_SUCCESS ||
      owner == nullptr || descriptor == nullptr) {
    if (descriptor != nullptr) {
      LocalFree(descriptor);
    }
    return false;
  }
  HANDLE raw_token = nullptr;
  if (!OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &raw_token)) {
    LocalFree(descriptor);
    return false;
  }
  WindowsHandle token(raw_token);
  DWORD token_length = 0;
  static_cast<void>(
      GetTokenInformation(token.get(), TokenUser, nullptr, 0, &token_length));
  std::vector<uint8_t> token_data(token_length);
  const bool success =
      token_length >= sizeof(TOKEN_USER) &&
      GetTokenInformation(token.get(), TokenUser, token_data.data(),
                          token_length, &token_length) &&
      EqualSid(owner,
               reinterpret_cast<TOKEN_USER *>(token_data.data())->User.Sid);
  secure_wipe(token_data);
  LocalFree(descriptor);
  return success;
}

bool apply_current_user_only_dacl(HANDLE handle, DWORD inheritance) {
  if (handle == nullptr || handle == INVALID_HANDLE_VALUE) {
    return false;
  }
  HANDLE raw_token = nullptr;
  if (!OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &raw_token)) {
    return false;
  }
  WindowsHandle token(raw_token);
  DWORD token_length = 0;
  static_cast<void>(
      GetTokenInformation(token.get(), TokenUser, nullptr, 0, &token_length));
  std::vector<uint8_t> token_data(token_length);
  if (token_length < sizeof(TOKEN_USER) ||
      !GetTokenInformation(token.get(), TokenUser, token_data.data(),
                           token_length, &token_length)) {
    secure_wipe(token_data);
    return false;
  }
  auto *token_user = reinterpret_cast<TOKEN_USER *>(token_data.data());
  if (!IsValidSid(token_user->User.Sid)) {
    secure_wipe(token_data);
    return false;
  }

  EXPLICIT_ACCESSW access{};
  access.grfAccessPermissions = GENERIC_ALL;
  access.grfAccessMode = SET_ACCESS;
  access.grfInheritance = inheritance;
  access.Trustee.TrusteeForm = TRUSTEE_IS_SID;
  access.Trustee.TrusteeType = TRUSTEE_IS_USER;
  access.Trustee.ptstrName = reinterpret_cast<LPWSTR>(token_user->User.Sid);
  PACL acl = nullptr;
  const DWORD acl_result = SetEntriesInAclW(1, &access, nullptr, &acl);
  bool success = acl_result == ERROR_SUCCESS && acl != nullptr;
  if (success) {
    success = SetSecurityInfo(handle, SE_FILE_OBJECT,
                              DACL_SECURITY_INFORMATION |
                                  PROTECTED_DACL_SECURITY_INFORMATION,
                              nullptr, nullptr, acl, nullptr) == ERROR_SUCCESS;
  }
  if (acl != nullptr) {
    LocalFree(acl);
  }
  secure_wipe(token_data);
  return success;
}

bool safe_handle(HANDLE handle) {
  FILE_ATTRIBUTE_TAG_INFO attributes{};
  return GetFileInformationByHandleEx(handle, FileAttributeTagInfo, &attributes,
                                      static_cast<DWORD>(sizeof(attributes))) &&
         (attributes.FileAttributes & FILE_ATTRIBUTE_REPARSE_POINT) == 0 &&
         owned_by_current_user(handle);
}

bool safe_directory(const std::filesystem::path &path) {
  WindowsHandle directory(CreateFileW(
      path.c_str(), FILE_READ_ATTRIBUTES | READ_CONTROL,
      FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE, nullptr,
      OPEN_EXISTING, FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OPEN_REPARSE_POINT,
      nullptr));
  if (!directory.valid() || !safe_handle(directory.get())) {
    return false;
  }
  BY_HANDLE_FILE_INFORMATION info{};
  return GetFileInformationByHandle(directory.get(), &info) &&
         (info.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0;
}

bool ensure_safe_directory(const std::filesystem::path &path) {
  if (!CreateDirectoryW(path.c_str(), nullptr) &&
      GetLastError() != ERROR_ALREADY_EXISTS) {
    return false;
  }
  WindowsHandle directory(CreateFileW(
      path.c_str(), FILE_READ_ATTRIBUTES | READ_CONTROL | WRITE_DAC,
      FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE, nullptr,
      OPEN_EXISTING, FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OPEN_REPARSE_POINT,
      nullptr));
  if (!directory.valid() || !safe_handle(directory.get())) {
    return false;
  }
  BY_HANDLE_FILE_INFORMATION info{};
  return GetFileInformationByHandle(directory.get(), &info) &&
         (info.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0 &&
         apply_current_user_only_dacl(directory.get(), CONTAINER_INHERIT_ACE |
                                                           OBJECT_INHERIT_ACE);
}

enum class FileReadResult {
  present,
  missing,
  invalid,
};

FileReadResult read_secure_file(const std::filesystem::path &path,
                                std::vector<uint8_t> *output) {
  if (output == nullptr) {
    return FileReadResult::invalid;
  }
  WindowsHandle file(CreateFileW(path.c_str(), GENERIC_READ | READ_CONTROL,
                                 FILE_SHARE_READ, nullptr, OPEN_EXISTING,
                                 FILE_ATTRIBUTE_NORMAL |
                                     FILE_FLAG_OPEN_REPARSE_POINT |
                                     FILE_FLAG_SEQUENTIAL_SCAN,
                                 nullptr));
  if (!file.valid()) {
    return GetLastError() == ERROR_FILE_NOT_FOUND ? FileReadResult::missing
                                                  : FileReadResult::invalid;
  }
  if (!safe_handle(file.get())) {
    return FileReadResult::invalid;
  }
  LARGE_INTEGER size{};
  if (!GetFileSizeEx(file.get(), &size) || size.QuadPart <= 0 ||
      size.QuadPart > static_cast<LONGLONG>(kMaxEnvelopeBytes)) {
    return FileReadResult::invalid;
  }
  output->assign(static_cast<size_t>(size.QuadPart), 0);
  size_t offset = 0;
  while (offset < output->size()) {
    DWORD read = 0;
    const DWORD requested = static_cast<DWORD>(
        std::min(output->size() - offset,
                 static_cast<size_t>(std::numeric_limits<DWORD>::max())));
    if (!ReadFile(file.get(), output->data() + offset, requested, &read,
                  nullptr) ||
        read == 0) {
      secure_wipe(*output);
      output->clear();
      return FileReadResult::invalid;
    }
    offset += read;
  }
  return FileReadResult::present;
}

std::wstring random_suffix() {
  std::array<uint8_t, 8> random{};
  if (!random_bytes(random.data(), random.size())) {
    return {};
  }
  constexpr wchar_t digits[] = L"0123456789abcdef";
  std::wstring result(random.size() * 2, L'0');
  for (size_t index = 0; index < random.size(); ++index) {
    result[index * 2] = digits[random[index] >> 4];
    result[index * 2 + 1] = digits[random[index] & 0x0f];
  }
  secure_wipe(random);
  return result;
}

bool write_secure_file_atomic(const std::filesystem::path &directory,
                              const std::filesystem::path &destination,
                              const std::vector<uint8_t> &bytes) {
  if (bytes.empty() || bytes.size() > kMaxEnvelopeBytes ||
      !safe_directory(directory)) {
    return false;
  }
  const std::wstring suffix = random_suffix();
  if (suffix.empty()) {
    return false;
  }
  const std::filesystem::path temporary =
      destination.wstring() + L".tmp." + suffix;
  WindowsHandle file(
      CreateFileW(temporary.c_str(), GENERIC_WRITE | READ_CONTROL | WRITE_DAC,
                  0, nullptr, CREATE_NEW,
                  FILE_ATTRIBUTE_HIDDEN | FILE_ATTRIBUTE_TEMPORARY |
                      FILE_FLAG_WRITE_THROUGH | FILE_FLAG_OPEN_REPARSE_POINT,
                  nullptr));
  if (!file.valid() || !safe_handle(file.get()) ||
      !apply_current_user_only_dacl(file.get(), NO_INHERITANCE)) {
    DeleteFileW(temporary.c_str());
    return false;
  }
  size_t offset = 0;
  bool success = true;
  while (offset < bytes.size()) {
    DWORD written = 0;
    const DWORD requested = static_cast<DWORD>(
        std::min(bytes.size() - offset,
                 static_cast<size_t>(std::numeric_limits<DWORD>::max())));
    if (!WriteFile(file.get(), bytes.data() + offset, requested, &written,
                   nullptr) ||
        written == 0) {
      success = false;
      break;
    }
    offset += written;
  }
  const bool flushed = success && FlushFileBuffers(file.get());
  const bool closed = file.close();
  success = flushed && closed;
  if (success) {
    success = MoveFileExW(temporary.c_str(), destination.c_str(),
                          MOVEFILE_REPLACE_EXISTING | MOVEFILE_WRITE_THROUGH);
  }
  if (!success) {
    DeleteFileW(temporary.c_str());
  }
  return success;
}

void append_u32(std::vector<uint8_t> *output, uint32_t value) {
  output->push_back(static_cast<uint8_t>(value));
  output->push_back(static_cast<uint8_t>(value >> 8));
  output->push_back(static_cast<uint8_t>(value >> 16));
  output->push_back(static_cast<uint8_t>(value >> 24));
}

std::optional<uint32_t> read_u32(const std::vector<uint8_t> &input,
                                 size_t *offset) {
  if (offset == nullptr || *offset > input.size() ||
      input.size() - *offset < 4) {
    return std::nullopt;
  }
  const uint32_t value = static_cast<uint32_t>(input[*offset]) |
                         (static_cast<uint32_t>(input[*offset + 1]) << 8) |
                         (static_cast<uint32_t>(input[*offset + 2]) << 16) |
                         (static_cast<uint32_t>(input[*offset + 3]) << 24);
  *offset += 4;
  return value;
}

struct Envelope {
  std::array<uint8_t, kHashBytes> vault_hash{};
  std::array<uint8_t, kHashBytes> relying_party_hash{};
  std::array<uint8_t, kPublicCoordinateBytes> public_x{};
  std::array<uint8_t, kPublicCoordinateBytes> public_y{};
  std::array<uint8_t, kPrfBytes> prf_salt{};
  std::array<uint8_t, kSaltBytes> kdf_salt{};
  std::array<uint8_t, kNonceBytes> nonce{};
  std::vector<uint8_t> credential_id;
  std::vector<uint8_t> ciphertext;
  std::array<uint8_t, kTagBytes> tag{};
};

void wipe_envelope(Envelope *envelope) {
  if (envelope == nullptr) {
    return;
  }
  secure_wipe(envelope->vault_hash);
  secure_wipe(envelope->relying_party_hash);
  secure_wipe(envelope->public_x);
  secure_wipe(envelope->public_y);
  secure_wipe(envelope->prf_salt);
  secure_wipe(envelope->kdf_salt);
  secure_wipe(envelope->nonce);
  secure_wipe(envelope->credential_id);
  secure_wipe(envelope->ciphertext);
  secure_wipe(envelope->tag);
}

std::vector<uint8_t> encode_envelope_aad(const Envelope &envelope) {
  std::vector<uint8_t> encoded;
  encoded.reserve(kEnvelopeMagic.size() + 8 + envelope.vault_hash.size() +
                  envelope.relying_party_hash.size() +
                  envelope.public_x.size() + envelope.public_y.size() +
                  envelope.prf_salt.size() + envelope.kdf_salt.size() +
                  envelope.nonce.size() + envelope.credential_id.size());
  encoded.insert(encoded.end(), kEnvelopeMagic.begin(), kEnvelopeMagic.end());
  append_u32(&encoded, kEnvelopeVersion);
  append_u32(&encoded, static_cast<uint32_t>(envelope.credential_id.size()));
  encoded.insert(encoded.end(), envelope.vault_hash.begin(),
                 envelope.vault_hash.end());
  encoded.insert(encoded.end(), envelope.relying_party_hash.begin(),
                 envelope.relying_party_hash.end());
  encoded.insert(encoded.end(), envelope.public_x.begin(),
                 envelope.public_x.end());
  encoded.insert(encoded.end(), envelope.public_y.begin(),
                 envelope.public_y.end());
  encoded.insert(encoded.end(), envelope.prf_salt.begin(),
                 envelope.prf_salt.end());
  encoded.insert(encoded.end(), envelope.kdf_salt.begin(),
                 envelope.kdf_salt.end());
  encoded.insert(encoded.end(), envelope.nonce.begin(), envelope.nonce.end());
  encoded.insert(encoded.end(), envelope.credential_id.begin(),
                 envelope.credential_id.end());
  return encoded;
}

std::vector<uint8_t> encode_envelope(const Envelope &envelope) {
  std::vector<uint8_t> encoded = encode_envelope_aad(envelope);
  encoded.insert(encoded.end(), envelope.ciphertext.begin(),
                 envelope.ciphertext.end());
  encoded.insert(encoded.end(), envelope.tag.begin(), envelope.tag.end());
  return encoded;
}

template <size_t Size>
bool copy_field(const std::vector<uint8_t> &input, size_t *offset,
                std::array<uint8_t, Size> *output) {
  if (offset == nullptr || output == nullptr || *offset > input.size() ||
      input.size() - *offset < Size) {
    return false;
  }
  std::memcpy(output->data(), input.data() + *offset, Size);
  *offset += Size;
  return true;
}

bool decode_envelope(const std::vector<uint8_t> &encoded, Envelope *output) {
  if (output == nullptr || encoded.size() > kMaxEnvelopeBytes ||
      encoded.size() < kEnvelopeMagic.size() + 8 + 6 * kHashBytes +
                           kNonceBytes + kTagBytes + 1 ||
      !fixed_time_equal(encoded.data(), kEnvelopeMagic.data(),
                        kEnvelopeMagic.size())) {
    return false;
  }
  size_t offset = kEnvelopeMagic.size();
  const auto version = read_u32(encoded, &offset);
  const auto credential_length = read_u32(encoded, &offset);
  if (!version || *version != kEnvelopeVersion || !credential_length ||
      *credential_length == 0 || *credential_length > kMaxCredentialIdBytes) {
    return false;
  }
  if (!copy_field(encoded, &offset, &output->vault_hash) ||
      !copy_field(encoded, &offset, &output->relying_party_hash) ||
      !copy_field(encoded, &offset, &output->public_x) ||
      !copy_field(encoded, &offset, &output->public_y) ||
      !copy_field(encoded, &offset, &output->prf_salt) ||
      !copy_field(encoded, &offset, &output->kdf_salt) ||
      !copy_field(encoded, &offset, &output->nonce)) {
    return false;
  }
  constexpr size_t ciphertext_bytes = PV_BIO_VAULT_KEY_BYTES;
  const size_t expected =
      offset + *credential_length + ciphertext_bytes + kTagBytes;
  if (expected != encoded.size()) {
    return false;
  }
  output->credential_id.assign(
      encoded.begin() + static_cast<std::ptrdiff_t>(offset),
      encoded.begin() +
          static_cast<std::ptrdiff_t>(offset + *credential_length));
  offset += *credential_length;
  output->ciphertext.assign(
      encoded.begin() + static_cast<std::ptrdiff_t>(offset),
      encoded.begin() + static_cast<std::ptrdiff_t>(offset + ciphertext_bytes));
  offset += ciphertext_bytes;
  std::memcpy(output->tag.data(), encoded.data() + offset, output->tag.size());
  return true;
}

class CborReader final {
public:
  CborReader(const uint8_t *bytes, size_t length)
      : bytes_(bytes), length_(length) {}

  bool read_map_size(uint64_t *output) { return read_argument(5, output); }

  bool read_integer(int64_t *output) {
    if (output == nullptr || offset_ >= length_) {
      return false;
    }
    const uint8_t major = bytes_[offset_] >> 5;
    uint64_t argument = 0;
    if ((major != 0 && major != 1) || !read_argument(major, &argument) ||
        argument > static_cast<uint64_t>(std::numeric_limits<int64_t>::max())) {
      return false;
    }
    *output = major == 0 ? static_cast<int64_t>(argument)
                         : -1 - static_cast<int64_t>(argument);
    return true;
  }

  bool read_bytes(std::array<uint8_t, kPublicCoordinateBytes> *output) {
    uint64_t length = 0;
    if (output == nullptr || !read_argument(2, &length) ||
        length != output->size() || offset_ > length_ ||
        length_ - offset_ < length) {
      return false;
    }
    std::memcpy(output->data(), bytes_ + offset_, output->size());
    offset_ += output->size();
    return true;
  }

private:
  bool read_argument(uint8_t expected_major, uint64_t *output) {
    if (output == nullptr || offset_ >= length_) {
      return false;
    }
    const uint8_t initial = bytes_[offset_++];
    if ((initial >> 5) != expected_major) {
      return false;
    }
    const uint8_t additional = initial & 0x1f;
    if (additional < 24) {
      *output = additional;
      return true;
    }
    size_t count = 0;
    switch (additional) {
    case 24:
      count = 1;
      break;
    case 25:
      count = 2;
      break;
    case 26:
      count = 4;
      break;
    case 27:
      count = 8;
      break;
    default:
      return false;
    }
    if (offset_ > length_ || length_ - offset_ < count) {
      return false;
    }
    uint64_t value = 0;
    for (size_t index = 0; index < count; ++index) {
      value = (value << 8) | bytes_[offset_++];
    }
    if ((count == 1 && value < 24) || (count == 2 && value <= 0xff) ||
        (count == 4 && value <= 0xffff) ||
        (count == 8 && value <= 0xffffffffULL)) {
      return false;
    }
    *output = value;
    return true;
  }

  const uint8_t *bytes_;
  size_t length_;
  size_t offset_ = 0;
};

struct CredentialPublicKey {
  std::array<uint8_t, kPublicCoordinateBytes> x{};
  std::array<uint8_t, kPublicCoordinateBytes> y{};
};

bool parse_cose_es256_key(const uint8_t *data, size_t length,
                          CredentialPublicKey *output) {
  if (data == nullptr || output == nullptr || length == 0) {
    return false;
  }
  CborReader reader(data, length);
  uint64_t entries = 0;
  if (!reader.read_map_size(&entries) || entries != 5) {
    return false;
  }
  bool key_type = false;
  bool algorithm = false;
  bool curve = false;
  bool x = false;
  bool y = false;
  for (uint64_t index = 0; index < entries; ++index) {
    int64_t label = 0;
    if (!reader.read_integer(&label)) {
      return false;
    }
    switch (label) {
    case 1: {
      int64_t value = 0;
      if (key_type || !reader.read_integer(&value) || value != 2)
        return false;
      key_type = true;
      break;
    }
    case 3: {
      int64_t value = 0;
      if (algorithm || !reader.read_integer(&value) || value != -7)
        return false;
      algorithm = true;
      break;
    }
    case -1: {
      int64_t value = 0;
      if (curve || !reader.read_integer(&value) || value != 1)
        return false;
      curve = true;
      break;
    }
    case -2:
      if (x || !reader.read_bytes(&output->x))
        return false;
      x = true;
      break;
    case -3:
      if (y || !reader.read_bytes(&output->y))
        return false;
      y = true;
      break;
    default:
      return false;
    }
  }
  return key_type && algorithm && curve && x && y;
}

bool parse_attested_credential(
    const uint8_t *authenticator_data, size_t authenticator_data_length,
    const std::array<uint8_t, kHashBytes> &expected_rp_hash,
    std::vector<uint8_t> *credential_id, CredentialPublicKey *public_key) {
  constexpr size_t fixed_header = 32 + 1 + 4;
  constexpr size_t attested_header = 16 + 2;
  if (authenticator_data == nullptr || credential_id == nullptr ||
      public_key == nullptr ||
      authenticator_data_length < fixed_header + attested_header ||
      !fixed_time_equal(authenticator_data, expected_rp_hash.data(),
                        expected_rp_hash.size())) {
    return false;
  }
  const uint8_t flags = authenticator_data[32];
  if ((flags & kAuthenticatorUserPresent) == 0 ||
      (flags & kAuthenticatorUserVerified) == 0 ||
      (flags & kAuthenticatorAttestedData) == 0 ||
      (flags & (kAuthenticatorBackupEligible | kAuthenticatorBackedUp)) != 0) {
    return false;
  }
  size_t offset = fixed_header + 16;
  const size_t credential_length =
      (static_cast<size_t>(authenticator_data[offset]) << 8) |
      static_cast<size_t>(authenticator_data[offset + 1]);
  offset += 2;
  if (credential_length == 0 || credential_length > kMaxCredentialIdBytes ||
      offset > authenticator_data_length ||
      authenticator_data_length - offset < credential_length + 1) {
    return false;
  }
  credential_id->assign(authenticator_data + offset,
                        authenticator_data + offset + credential_length);
  offset += credential_length;
  return parse_cose_es256_key(authenticator_data + offset,
                              authenticator_data_length - offset, public_key);
}

bool valid_assertion_authenticator_data(
    const uint8_t *authenticator_data, size_t length,
    const std::array<uint8_t, kHashBytes> &expected_rp_hash) {
  if (authenticator_data == nullptr || length < 37 ||
      !fixed_time_equal(authenticator_data, expected_rp_hash.data(),
                        expected_rp_hash.size())) {
    return false;
  }
  const uint8_t flags = authenticator_data[32];
  return (flags & kAuthenticatorUserPresent) != 0 &&
         (flags & kAuthenticatorUserVerified) != 0 &&
         (flags & (kAuthenticatorBackupEligible | kAuthenticatorBackedUp)) == 0;
}

bool parse_der_integer(const uint8_t *data, size_t length, size_t *offset,
                       uint8_t *output) {
  if (data == nullptr || offset == nullptr || output == nullptr ||
      *offset + 2 > length || data[(*offset)++] != 0x02) {
    return false;
  }
  const size_t integer_length = data[(*offset)++];
  if (integer_length == 0 || integer_length > 33 || *offset > length ||
      length - *offset < integer_length || (data[*offset] & 0x80) != 0) {
    return false;
  }
  const bool has_padding = integer_length == 33;
  if (has_padding && (data[*offset] != 0 || (data[*offset + 1] & 0x80) == 0)) {
    return false;
  }
  const size_t value_length = integer_length - (has_padding ? 1 : 0);
  if (value_length > kPublicCoordinateBytes ||
      (!has_padding && value_length > 1 && data[*offset] == 0 &&
       (data[*offset + 1] & 0x80) == 0)) {
    return false;
  }
  std::memset(output, 0, kPublicCoordinateBytes);
  std::memcpy(output + kPublicCoordinateBytes - value_length,
              data + *offset + (has_padding ? 1 : 0), value_length);
  *offset += integer_length;
  return true;
}

bool der_es256_to_raw(const uint8_t *data, size_t length,
                      std::array<uint8_t, 64> *output) {
  if (data == nullptr || output == nullptr || length < 8 || length > 72 ||
      data[0] != 0x30 || data[1] != length - 2 || (data[1] & 0x80) != 0) {
    return false;
  }
  size_t offset = 2;
  return parse_der_integer(data, length, &offset, output->data()) &&
         parse_der_integer(data, length, &offset,
                           output->data() + kPublicCoordinateBytes) &&
         offset == length;
}

bool verify_assertion_signature(const CredentialPublicKey &public_key,
                                const uint8_t *authenticator_data,
                                size_t authenticator_data_length,
                                const std::vector<uint8_t> &client_data,
                                const uint8_t *signature,
                                size_t signature_length) {
  if (authenticator_data == nullptr || client_data.empty() ||
      signature == nullptr ||
      authenticator_data_length > std::numeric_limits<ULONG>::max()) {
    return false;
  }
  std::array<uint8_t, kHashBytes> client_hash{};
  std::vector<uint8_t> signed_data;
  std::array<uint8_t, kHashBytes> signed_hash{};
  std::array<uint8_t, 64> raw_signature{};
  bool success = sha256(client_data.data(), client_data.size(), &client_hash);
  if (success) {
    signed_data.reserve(authenticator_data_length + client_hash.size());
    signed_data.insert(signed_data.end(), authenticator_data,
                       authenticator_data + authenticator_data_length);
    signed_data.insert(signed_data.end(), client_hash.begin(),
                       client_hash.end());
    success = sha256(signed_data.data(), signed_data.size(), &signed_hash) &&
              der_es256_to_raw(signature, signature_length, &raw_signature);
  }
  AlgorithmHandle algorithm;
  if (success) {
    success = nt_success(BCryptOpenAlgorithmProvider(
        algorithm.output(), BCRYPT_ECDSA_P256_ALGORITHM, nullptr, 0));
  }
  std::vector<uint8_t> public_blob(sizeof(BCRYPT_ECCKEY_BLOB) +
                                   2 * kPublicCoordinateBytes);
  if (success) {
    auto *header = reinterpret_cast<BCRYPT_ECCKEY_BLOB *>(public_blob.data());
    header->dwMagic = BCRYPT_ECDSA_PUBLIC_P256_MAGIC;
    header->cbKey = static_cast<ULONG>(kPublicCoordinateBytes);
    std::memcpy(public_blob.data() + sizeof(*header), public_key.x.data(),
                public_key.x.size());
    std::memcpy(public_blob.data() + sizeof(*header) + public_key.x.size(),
                public_key.y.data(), public_key.y.size());
  }
  KeyHandle key;
  if (success) {
    success = nt_success(BCryptImportKeyPair(
        algorithm.get(), nullptr, BCRYPT_ECCPUBLIC_BLOB, key.output(),
        public_blob.data(), static_cast<ULONG>(public_blob.size()), 0));
  }
  if (success) {
    success = nt_success(BCryptVerifySignature(
        key.get(), nullptr, signed_hash.data(),
        static_cast<ULONG>(signed_hash.size()), raw_signature.data(),
        static_cast<ULONG>(raw_signature.size()), 0));
  }
  secure_wipe(client_hash);
  secure_wipe(signed_data);
  secure_wipe(signed_hash);
  secure_wipe(raw_signature);
  secure_wipe(public_blob);
  return success;
}

std::string base64url(const uint8_t *data, size_t length) {
  constexpr char alphabet[] =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
  std::string output;
  output.reserve((length * 4 + 2) / 3);
  size_t offset = 0;
  while (offset + 3 <= length) {
    const uint32_t value = (static_cast<uint32_t>(data[offset]) << 16) |
                           (static_cast<uint32_t>(data[offset + 1]) << 8) |
                           data[offset + 2];
    output.push_back(alphabet[(value >> 18) & 0x3f]);
    output.push_back(alphabet[(value >> 12) & 0x3f]);
    output.push_back(alphabet[(value >> 6) & 0x3f]);
    output.push_back(alphabet[value & 0x3f]);
    offset += 3;
  }
  if (length - offset == 1) {
    const uint32_t value = static_cast<uint32_t>(data[offset]) << 16;
    output.push_back(alphabet[(value >> 18) & 0x3f]);
    output.push_back(alphabet[(value >> 12) & 0x3f]);
  } else if (length - offset == 2) {
    const uint32_t value = (static_cast<uint32_t>(data[offset]) << 16) |
                           (static_cast<uint32_t>(data[offset + 1]) << 8);
    output.push_back(alphabet[(value >> 18) & 0x3f]);
    output.push_back(alphabet[(value >> 12) & 0x3f]);
    output.push_back(alphabet[(value >> 6) & 0x3f]);
  }
  return output;
}

std::vector<uint8_t>
make_client_data(const char *type,
                 const std::array<uint8_t, kChallengeBytes> &challenge) {
  const std::string encoded_challenge =
      base64url(challenge.data(), challenge.size());
  const std::string json =
      std::string("{\"type\":\"") + type + "\",\"challenge\":\"" +
      encoded_challenge +
      "\",\"origin\":\"https://passvault.kiramanga.me\",\"crossOrigin\":false}";
  return std::vector<uint8_t>(json.begin(), json.end());
}

} // namespace

struct pv_bio_context {
  std::filesystem::path biometric_directory;
  std::filesystem::path metadata_path;
  std::mutex operation_mutex;
  std::condition_variable operation_finished;
  HWND parent_window = nullptr;
  uint64_t active_operation = 0;
  uint64_t pending_cancellation = 0;
  GUID cancellation_id{};
  bool has_cancellation_id = false;
  bool cancellation_requested = false;
  bool operation_committed = false;
  bool closing = false;
};

namespace {

class WebAuthnApi final {
public:
  using GetApiVersion = decltype(&WebAuthNGetApiVersionNumber);
  using IsPlatformAuthenticatorAvailable =
      decltype(&WebAuthNIsUserVerifyingPlatformAuthenticatorAvailable);
  using MakeCredential = decltype(&WebAuthNAuthenticatorMakeCredential);
  using GetAssertion = decltype(&WebAuthNAuthenticatorGetAssertion);
  using FreeAttestation = decltype(&WebAuthNFreeCredentialAttestation);
  using FreeAssertion = decltype(&WebAuthNFreeAssertion);
  using GetCancellationId = decltype(&WebAuthNGetCancellationId);
  using CancelOperation = decltype(&WebAuthNCancelCurrentOperation);
  using DeleteCredential = decltype(&WebAuthNDeletePlatformCredential);
  using GetCredentialList = decltype(&WebAuthNGetPlatformCredentialList);
  using FreeCredentialList = decltype(&WebAuthNFreePlatformCredentialList);

  WebAuthnApi() {
    module_ =
        LoadLibraryExW(L"webauthn.dll", nullptr, LOAD_LIBRARY_SEARCH_SYSTEM32);
    if (module_ == nullptr ||
        !load(&get_api_version_, "WebAuthNGetApiVersionNumber") ||
        !load(&is_platform_authenticator_available_,
              "WebAuthNIsUserVerifyingPlatformAuthenticatorAvailable") ||
        !load(&make_credential_, "WebAuthNAuthenticatorMakeCredential") ||
        !load(&get_assertion_, "WebAuthNAuthenticatorGetAssertion") ||
        !load(&free_attestation_, "WebAuthNFreeCredentialAttestation") ||
        !load(&free_assertion_, "WebAuthNFreeAssertion") ||
        !load(&get_cancellation_id_, "WebAuthNGetCancellationId") ||
        !load(&cancel_operation_, "WebAuthNCancelCurrentOperation") ||
        !load(&delete_credential_, "WebAuthNDeletePlatformCredential") ||
        !load(&get_credential_list_, "WebAuthNGetPlatformCredentialList") ||
        !load(&free_credential_list_, "WebAuthNFreePlatformCredentialList")) {
      unload();
      return;
    }
    api_version_ = get_api_version_();
    if (api_version_ < WEBAUTHN_API_VERSION_8) {
      unload();
    }
  }

  ~WebAuthnApi() { unload(); }
  WebAuthnApi(const WebAuthnApi &) = delete;
  WebAuthnApi &operator=(const WebAuthnApi &) = delete;

  bool available() const { return module_ != nullptr; }
  DWORD api_version() const { return api_version_; }

  HRESULT is_platform_authenticator_available(BOOL *output) const {
    return is_platform_authenticator_available_(output);
  }
  HRESULT
  make_credential(HWND window, PCWEBAUTHN_RP_ENTITY_INFORMATION rp,
                  PCWEBAUTHN_USER_ENTITY_INFORMATION user,
                  PCWEBAUTHN_COSE_CREDENTIAL_PARAMETERS parameters,
                  PCWEBAUTHN_CLIENT_DATA client_data,
                  PCWEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS options,
                  PWEBAUTHN_CREDENTIAL_ATTESTATION *output) const {
    return make_credential_(window, rp, user, parameters, client_data, options,
                            output);
  }
  HRESULT get_assertion(HWND window, LPCWSTR relying_party,
                        PCWEBAUTHN_CLIENT_DATA client_data,
                        PCWEBAUTHN_AUTHENTICATOR_GET_ASSERTION_OPTIONS options,
                        PWEBAUTHN_ASSERTION *output) const {
    return get_assertion_(window, relying_party, client_data, options, output);
  }
  void free_attestation(PWEBAUTHN_CREDENTIAL_ATTESTATION value) const {
    free_attestation_(value);
  }
  void free_assertion(PWEBAUTHN_ASSERTION value) const {
    free_assertion_(value);
  }
  HRESULT get_cancellation_id(GUID *output) const {
    return get_cancellation_id_(output);
  }
  HRESULT cancel(const GUID *id) const { return cancel_operation_(id); }
  HRESULT delete_credential(const uint8_t *id, size_t length) const {
    if (id == nullptr || length == 0 ||
        length > std::numeric_limits<DWORD>::max()) {
      return E_INVALIDARG;
    }
    return delete_credential_(static_cast<DWORD>(length), id);
  }
  HRESULT get_credential_list(PCWEBAUTHN_GET_CREDENTIALS_OPTIONS options,
                              PWEBAUTHN_CREDENTIAL_DETAILS_LIST *output) const {
    return get_credential_list_(options, output);
  }
  void free_credential_list(PWEBAUTHN_CREDENTIAL_DETAILS_LIST value) const {
    free_credential_list_(value);
  }

private:
  template <typename Function> bool load(Function *output, const char *name) {
    const FARPROC address = GetProcAddress(module_, name);
    if (address == nullptr) {
      return false;
    }
    static_assert(sizeof(address) == sizeof(*output));
    std::memcpy(output, &address, sizeof(address));
    return true;
  }

  void unload() {
    if (module_ != nullptr) {
      FreeLibrary(module_);
      module_ = nullptr;
    }
    api_version_ = 0;
    get_api_version_ = nullptr;
    is_platform_authenticator_available_ = nullptr;
    make_credential_ = nullptr;
    get_assertion_ = nullptr;
    free_attestation_ = nullptr;
    free_assertion_ = nullptr;
    get_cancellation_id_ = nullptr;
    cancel_operation_ = nullptr;
    delete_credential_ = nullptr;
    get_credential_list_ = nullptr;
    free_credential_list_ = nullptr;
  }

  HMODULE module_ = nullptr;
  DWORD api_version_ = 0;
  GetApiVersion get_api_version_ = nullptr;
  IsPlatformAuthenticatorAvailable is_platform_authenticator_available_ =
      nullptr;
  MakeCredential make_credential_ = nullptr;
  GetAssertion get_assertion_ = nullptr;
  FreeAttestation free_attestation_ = nullptr;
  FreeAssertion free_assertion_ = nullptr;
  GetCancellationId get_cancellation_id_ = nullptr;
  CancelOperation cancel_operation_ = nullptr;
  DeleteCredential delete_credential_ = nullptr;
  GetCredentialList get_credential_list_ = nullptr;
  FreeCredentialList free_credential_list_ = nullptr;
};

WebAuthnApi &webauthn() {
  static WebAuthnApi api;
  return api;
}

class AttestationHandle final {
public:
  explicit AttestationHandle(const WebAuthnApi &api) : api_(api) {}
  ~AttestationHandle() {
    if (value_ != nullptr) {
      api_.free_attestation(value_);
    }
  }
  AttestationHandle(const AttestationHandle &) = delete;
  AttestationHandle &operator=(const AttestationHandle &) = delete;

  PWEBAUTHN_CREDENTIAL_ATTESTATION *output() { return &value_; }
  PWEBAUTHN_CREDENTIAL_ATTESTATION get() const { return value_; }

private:
  const WebAuthnApi &api_;
  PWEBAUTHN_CREDENTIAL_ATTESTATION value_ = nullptr;
};

class AssertionHandle final {
public:
  explicit AssertionHandle(const WebAuthnApi &api) : api_(api) {}
  ~AssertionHandle() {
    if (value_ != nullptr) {
      api_.free_assertion(value_);
    }
  }
  AssertionHandle(const AssertionHandle &) = delete;
  AssertionHandle &operator=(const AssertionHandle &) = delete;

  PWEBAUTHN_ASSERTION *output() { return &value_; }
  PWEBAUTHN_ASSERTION get() const { return value_; }

private:
  const WebAuthnApi &api_;
  PWEBAUTHN_ASSERTION value_ = nullptr;
};

class CredentialListHandle final {
public:
  explicit CredentialListHandle(const WebAuthnApi &api) : api_(api) {}
  ~CredentialListHandle() {
    if (value_ != nullptr) {
      api_.free_credential_list(value_);
    }
  }
  CredentialListHandle(const CredentialListHandle &) = delete;
  CredentialListHandle &operator=(const CredentialListHandle &) = delete;

  PWEBAUTHN_CREDENTIAL_DETAILS_LIST *output() { return &value_; }
  PWEBAUTHN_CREDENTIAL_DETAILS_LIST get() const { return value_; }

private:
  const WebAuthnApi &api_;
  PWEBAUTHN_CREDENTIAL_DETAILS_LIST value_ = nullptr;
};

pv_bio_status map_webauthn_error(HRESULT error, bool missing_is_invalidated) {
  if (SUCCEEDED(error)) {
    return PV_BIO_OK;
  }
  if (error == HRESULT_FROM_WIN32(ERROR_CANCELLED) ||
      error == NTE_USER_CANCELLED) {
    return PV_BIO_CANCELLED;
  }
  if (error == HRESULT_FROM_WIN32(ERROR_ACCOUNT_LOCKED_OUT) ||
      error == HRESULT_FROM_WIN32(ERROR_LOCKED)) {
    return PV_BIO_LOCKED_OUT;
  }
  if (error == NTE_NOT_FOUND) {
    return missing_is_invalidated ? PV_BIO_INVALIDATED : PV_BIO_NOT_ENROLLED;
  }
  if (error == NTE_DEVICE_NOT_FOUND) {
    return PV_BIO_NOT_AVAILABLE;
  }
  if (error == HRESULT_FROM_WIN32(ERROR_NOT_SUPPORTED) ||
      error == NTE_NOT_SUPPORTED || error == NTE_INVALID_PARAMETER) {
    return PV_BIO_NOT_AVAILABLE;
  }
  if (error == HRESULT_FROM_WIN32(ERROR_BUSY)) {
    return PV_BIO_BUSY;
  }
  return PV_BIO_AUTHENTICATION_FAILED;
}

class ComApartmentScope final {
public:
  ComApartmentScope()
      : result_(CoInitializeEx(nullptr, COINIT_MULTITHREADED)) {}
  ~ComApartmentScope() {
    if (SUCCEEDED(result_)) {
      CoUninitialize();
    }
  }
  ComApartmentScope(const ComApartmentScope &) = delete;
  ComApartmentScope &operator=(const ComApartmentScope &) = delete;

  bool usable() const {
    return SUCCEEDED(result_) || result_ == RPC_E_CHANGED_MODE;
  }

private:
  HRESULT result_;
};

pv_bio_availability windows_hello_availability() {
  WebAuthnApi &api = webauthn();
  if (!api.available() || api.api_version() < WEBAUTHN_API_VERSION_8) {
    return PV_BIO_AVAILABILITY_UNAVAILABLE;
  }
  ComApartmentScope apartment;
  if (!apartment.usable()) {
    return PV_BIO_AVAILABILITY_UNAVAILABLE;
  }
  try {
    using winrt::Windows::Security::Credentials::UI::UserConsentVerifier;
    using winrt::Windows::Security::Credentials::UI::
        UserConsentVerifierAvailability;
    const auto availability =
        UserConsentVerifier::CheckAvailabilityAsync().get();
    switch (availability) {
    case UserConsentVerifierAvailability::Available:
      break;
    case UserConsentVerifierAvailability::NotConfiguredForUser:
      return PV_BIO_AVAILABILITY_NOT_ENROLLED;
    case UserConsentVerifierAvailability::DeviceBusy:
      return PV_BIO_AVAILABILITY_LOCKED_OUT;
    case UserConsentVerifierAvailability::DeviceNotPresent:
    case UserConsentVerifierAvailability::DisabledByPolicy:
    default:
      return PV_BIO_AVAILABILITY_UNAVAILABLE;
    }
  } catch (const winrt::hresult_error &) {
    return PV_BIO_AVAILABILITY_UNAVAILABLE;
  }
  BOOL available = FALSE;
  return SUCCEEDED(api.is_platform_authenticator_available(&available)) &&
                 available
             ? PV_BIO_AVAILABLE
             : PV_BIO_AVAILABILITY_UNAVAILABLE;
}

pv_bio_status
availability_to_operation_status(pv_bio_availability availability) {
  switch (availability) {
  case PV_BIO_AVAILABLE:
    return PV_BIO_OK;
  case PV_BIO_AVAILABILITY_NOT_ENROLLED:
    return PV_BIO_NOT_ENROLLED;
  case PV_BIO_AVAILABILITY_LOCKED_OUT:
    return PV_BIO_LOCKED_OUT;
  case PV_BIO_AVAILABILITY_UNAVAILABLE:
  default:
    return PV_BIO_NOT_AVAILABLE;
  }
}

bool credential_inventory_is_authoritative(pv_bio_availability availability) {
  // Microsoft documents that a locked authenticator can temporarily omit
  // credentials from WebAuthNGetPlatformCredentialList. Missing entries are
  // therefore evidence of key loss only while the authenticator reports
  // available; temporary lockout/unavailability must preserve enrollment.
  return availability == PV_BIO_AVAILABLE;
}

pv_bio_status begin_operation(pv_bio_context *context, uint64_t operation_id,
                              HWND *parent_window) {
  if (context == nullptr || operation_id == 0 || parent_window == nullptr) {
    return PV_BIO_INTERNAL_ERROR;
  }
  std::lock_guard lock(context->operation_mutex);
  if (context->closing) {
    return PV_BIO_NOT_AVAILABLE;
  }
  if (context->active_operation != 0) {
    return PV_BIO_BUSY;
  }
  if (context->parent_window == nullptr || !IsWindow(context->parent_window)) {
    return PV_BIO_NOT_AVAILABLE;
  }
  context->active_operation = operation_id;
  context->has_cancellation_id = false;
  context->cancellation_requested =
      context->pending_cancellation == operation_id;
  context->pending_cancellation = 0;
  context->operation_committed = false;
  *parent_window = context->parent_window;
  return PV_BIO_OK;
}

pv_bio_status install_cancellation_id(pv_bio_context *context,
                                      uint64_t operation_id,
                                      const GUID &cancellation_id) {
  bool cancel_immediately = false;
  {
    std::lock_guard lock(context->operation_mutex);
    if (context->active_operation != operation_id) {
      return PV_BIO_CANCELLED;
    }
    context->cancellation_id = cancellation_id;
    context->has_cancellation_id = true;
    cancel_immediately = context->cancellation_requested || context->closing;
  }
  if (cancel_immediately) {
    static_cast<void>(webauthn().cancel(&cancellation_id));
  }
  return PV_BIO_OK;
}

void finish_operation(pv_bio_context *context, uint64_t operation_id) {
  std::lock_guard lock(context->operation_mutex);
  if (context->active_operation == operation_id) {
    context->active_operation = 0;
    context->has_cancellation_id = false;
    context->cancellation_requested = false;
    context->operation_committed = false;
    secure_wipe(&context->cancellation_id, sizeof(context->cancellation_id));
    context->operation_finished.notify_all();
  }
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

bool delete_platform_credential(const std::vector<uint8_t> &credential_id) {
  if (credential_id.empty()) {
    return true;
  }
  const HRESULT result =
      webauthn().delete_credential(credential_id.data(), credential_id.size());
  return SUCCEEDED(result) || result == NTE_NOT_FOUND;
}

bool credential_identity_matches(const PWEBAUTHN_CREDENTIAL_DETAILS detail,
                                 const uint8_t *vault_hash) {
  return detail != nullptr && vault_hash != nullptr && detail->dwVersion != 0 &&
         detail->pRpInformation != nullptr &&
         detail->pUserInformation != nullptr &&
         detail->pRpInformation->pwszId != nullptr &&
         detail->pUserInformation->pbId != nullptr &&
         std::wcscmp(detail->pRpInformation->pwszId, kRelyingPartyId) == 0 &&
         detail->pUserInformation->cbId == PV_BIO_VAULT_HASH_BYTES &&
         fixed_time_equal(detail->pUserInformation->pbId, vault_hash,
                          PV_BIO_VAULT_HASH_BYTES);
}

bool credential_is_removable_and_device_bound(
    const PWEBAUTHN_CREDENTIAL_DETAILS detail) {
  return detail != nullptr && detail->bRemovable &&
         (detail->dwVersion < WEBAUTHN_CREDENTIAL_DETAILS_VERSION_2 ||
          !detail->bBackedUp);
}

enum class ManagedCredentialPresence {
  present,
  missing,
  invalid,
  error,
};

ManagedCredentialPresence
managed_credential_presence(const uint8_t *vault_hash,
                            const std::vector<uint8_t> &credential_id) {
  if (vault_hash == nullptr || credential_id.empty() ||
      credential_id.size() > kMaxCredentialIdBytes || !webauthn().available()) {
    return ManagedCredentialPresence::error;
  }
  WEBAUTHN_GET_CREDENTIALS_OPTIONS options{};
  options.dwVersion = WEBAUTHN_GET_CREDENTIALS_OPTIONS_CURRENT_VERSION;
  options.pwszRpId = kRelyingPartyId;
  CredentialListHandle credentials(webauthn());
  const HRESULT list_result =
      webauthn().get_credential_list(&options, credentials.output());
  if (list_result == NTE_NOT_FOUND) {
    return ManagedCredentialPresence::missing;
  }
  if (FAILED(list_result) || credentials.get() == nullptr ||
      credentials.get()->cCredentialDetails > 1024 ||
      (credentials.get()->cCredentialDetails > 0 &&
       credentials.get()->ppCredentialDetails == nullptr)) {
    return ManagedCredentialPresence::error;
  }
  for (DWORD index = 0; index < credentials.get()->cCredentialDetails;
       ++index) {
    const PWEBAUTHN_CREDENTIAL_DETAILS detail =
        credentials.get()->ppCredentialDetails[index];
    if (detail == nullptr || detail->pbCredentialID == nullptr ||
        detail->cbCredentialID == 0 ||
        static_cast<size_t>(detail->cbCredentialID) > kMaxCredentialIdBytes) {
      return ManagedCredentialPresence::error;
    }
    const bool exact_credential =
        credential_id.size() == detail->cbCredentialID &&
        fixed_time_equal(credential_id.data(), detail->pbCredentialID,
                         detail->cbCredentialID);
    if (!exact_credential) {
      continue;
    }
    if (!credential_identity_matches(detail, vault_hash) ||
        !credential_is_removable_and_device_bound(detail)) {
      return ManagedCredentialPresence::invalid;
    }
    return ManagedCredentialPresence::present;
  }
  return ManagedCredentialPresence::missing;
}

bool delete_vault_credentials(
    const uint8_t *vault_hash,
    const std::vector<uint8_t> *except_credential = nullptr) {
  if (vault_hash == nullptr || !webauthn().available()) {
    return false;
  }
  WEBAUTHN_GET_CREDENTIALS_OPTIONS options{};
  options.dwVersion = WEBAUTHN_GET_CREDENTIALS_OPTIONS_CURRENT_VERSION;
  options.pwszRpId = kRelyingPartyId;
  CredentialListHandle credentials(webauthn());
  const HRESULT list_result =
      webauthn().get_credential_list(&options, credentials.output());
  if (list_result == NTE_NOT_FOUND) {
    return true;
  }
  if (FAILED(list_result) || credentials.get() == nullptr ||
      credentials.get()->cCredentialDetails > 1024 ||
      (credentials.get()->cCredentialDetails > 0 &&
       credentials.get()->ppCredentialDetails == nullptr)) {
    return false;
  }
  bool success = true;
  for (DWORD index = 0; index < credentials.get()->cCredentialDetails;
       ++index) {
    const PWEBAUTHN_CREDENTIAL_DETAILS detail =
        credentials.get()->ppCredentialDetails[index];
    if (detail == nullptr || detail->pbCredentialID == nullptr ||
        detail->cbCredentialID == 0 ||
        static_cast<size_t>(detail->cbCredentialID) > kMaxCredentialIdBytes) {
      success = false;
      continue;
    }
    if (!credential_identity_matches(detail, vault_hash)) {
      continue;
    }
    if (!credential_is_removable_and_device_bound(detail)) {
      success = false;
      continue;
    }
    const bool excepted =
        except_credential != nullptr &&
        except_credential->size() == detail->cbCredentialID &&
        fixed_time_equal(except_credential->data(), detail->pbCredentialID,
                         detail->cbCredentialID);
    if (!excepted) {
      const std::vector<uint8_t> credential_id(detail->pbCredentialID,
                                               detail->pbCredentialID +
                                                   detail->cbCredentialID);
      success = delete_platform_credential(credential_id) && success;
    }
  }
  return success;
}

void invalidate_vault_enrollment(pv_bio_context *context,
                                 const uint8_t *vault_hash) {
  // Use the caller's fixed vault identity for credential enumeration. Never
  // delete a platform credential ID taken only from unauthenticated local
  // envelope bytes.
  static_cast<void>(delete_vault_credentials(vault_hash));
  static_cast<void>(DeleteFileW(context->metadata_path.c_str()));
}

std::array<uint8_t, kHashBytes> relying_party_hash() {
  constexpr char relying_party[] = "passvault.kiramanga.me";
  std::array<uint8_t, kHashBytes> result{};
  if (!sha256(reinterpret_cast<const uint8_t *>(relying_party),
              sizeof(relying_party) - 1, &result)) {
    secure_wipe(result);
  }
  return result;
}

pv_bio_status create_windows_hello_credential(pv_bio_context *context,
                                              uint64_t operation_id,
                                              const uint8_t *vault_hash,
                                              const uint8_t *vault_key) {
  const auto availability = windows_hello_availability();
  if (availability != PV_BIO_AVAILABLE) {
    return availability_to_operation_status(availability);
  }
  HWND parent_window = nullptr;
  const pv_bio_status begin_status =
      begin_operation(context, operation_id, &parent_window);
  if (begin_status != PV_BIO_OK) {
    return begin_status;
  }
  OperationGuard operation(context, operation_id);
  if (operation_was_cancelled(context, operation_id)) {
    return PV_BIO_CANCELLED;
  }
  WebAuthnApi &api = webauthn();
  GUID cancellation_id{};
  if (FAILED(api.get_cancellation_id(&cancellation_id)) ||
      install_cancellation_id(context, operation_id, cancellation_id) !=
          PV_BIO_OK) {
    return PV_BIO_CANCELLED;
  }

  std::array<uint8_t, kChallengeBytes> challenge{};
  Envelope envelope;
  std::copy_n(vault_hash, envelope.vault_hash.size(),
              envelope.vault_hash.begin());
  envelope.relying_party_hash = relying_party_hash();
  if (!random_bytes(challenge.data(), challenge.size()) ||
      !random_bytes(envelope.prf_salt.data(), envelope.prf_salt.size()) ||
      !random_bytes(envelope.kdf_salt.data(), envelope.kdf_salt.size()) ||
      !random_bytes(envelope.nonce.data(), envelope.nonce.size()) ||
      std::all_of(envelope.relying_party_hash.begin(),
                  envelope.relying_party_hash.end(),
                  [](uint8_t value) { return value == 0; })) {
    secure_wipe(challenge);
    wipe_envelope(&envelope);
    return PV_BIO_INTERNAL_ERROR;
  }
  std::vector<uint8_t> client_data_json =
      make_client_data("webauthn.create", challenge);
  WEBAUTHN_CLIENT_DATA client_data{};
  client_data.dwVersion = WEBAUTHN_CLIENT_DATA_CURRENT_VERSION;
  client_data.cbClientDataJSON = static_cast<DWORD>(client_data_json.size());
  client_data.pbClientDataJSON = client_data_json.data();
  client_data.pwszHashAlgId = WEBAUTHN_HASH_ALGORITHM_SHA_256;

  WEBAUTHN_RP_ENTITY_INFORMATION relying_party{};
  relying_party.dwVersion = WEBAUTHN_RP_ENTITY_INFORMATION_CURRENT_VERSION;
  relying_party.pwszId = kRelyingPartyId;
  relying_party.pwszName = L"PassVault";

  WEBAUTHN_USER_ENTITY_INFORMATION user{};
  user.dwVersion = WEBAUTHN_USER_ENTITY_INFORMATION_CURRENT_VERSION;
  user.cbId = PV_BIO_VAULT_HASH_BYTES;
  user.pbId = const_cast<PBYTE>(vault_hash);
  user.pwszName = L"PassVault vault";
  user.pwszDisplayName = L"PassVault vault";

  WEBAUTHN_COSE_CREDENTIAL_PARAMETER parameter{};
  parameter.dwVersion = WEBAUTHN_COSE_CREDENTIAL_PARAMETER_CURRENT_VERSION;
  parameter.pwszCredentialType = WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY;
  parameter.lAlg = WEBAUTHN_COSE_ALGORITHM_ECDSA_P256_WITH_SHA256;
  WEBAUTHN_COSE_CREDENTIAL_PARAMETERS parameters{};
  parameters.cCredentialParameters = 1;
  parameters.pCredentialParameters = &parameter;

  WEBAUTHN_CRED_PROTECT_EXTENSION_IN credential_protection{};
  credential_protection.dwCredProtect = WEBAUTHN_USER_VERIFICATION_REQUIRED;
  credential_protection.bRequireCredProtect = TRUE;
  WEBAUTHN_EXTENSION extension{};
  extension.pwszExtensionIdentifier =
      WEBAUTHN_EXTENSIONS_IDENTIFIER_CRED_PROTECT;
  extension.cbExtension = static_cast<DWORD>(sizeof(credential_protection));
  extension.pvExtension = &credential_protection;

  WEBAUTHN_HMAC_SECRET_SALT prf_input{};
  prf_input.cbFirst = static_cast<DWORD>(envelope.prf_salt.size());
  prf_input.pbFirst = envelope.prf_salt.data();

  WEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS options{};
  options.dwVersion = WEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS_VERSION_8;
  options.dwTimeoutMilliseconds = kPromptTimeoutMilliseconds;
  options.Extensions.cExtensions = 1;
  options.Extensions.pExtensions = &extension;
  options.dwAuthenticatorAttachment =
      WEBAUTHN_AUTHENTICATOR_ATTACHMENT_PLATFORM;
  options.bRequireResidentKey = TRUE;
  options.dwUserVerificationRequirement =
      WEBAUTHN_USER_VERIFICATION_REQUIREMENT_REQUIRED;
  options.dwAttestationConveyancePreference =
      WEBAUTHN_ATTESTATION_CONVEYANCE_PREFERENCE_NONE;
  options.pCancellationId = &cancellation_id;
  options.bPreferResidentKey = TRUE;
  options.bEnablePrf = TRUE;
  options.pPRFGlobalEval = &prf_input;

  AttestationHandle attestation(api);
  const HRESULT create_result =
      api.make_credential(parent_window, &relying_party, &user, &parameters,
                          &client_data, &options, attestation.output());
  if (FAILED(create_result) || attestation.get() == nullptr) {
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    wipe_envelope(&envelope);
    return map_webauthn_error(create_result, false);
  }

  if (operation_was_cancelled(context, operation_id)) {
    const PWEBAUTHN_CREDENTIAL_ATTESTATION cancelled_result = attestation.get();
    if (cancelled_result->pbCredentialId != nullptr &&
        cancelled_result->cbCredentialId > 0 &&
        static_cast<size_t>(cancelled_result->cbCredentialId) <=
            kMaxCredentialIdBytes) {
      const std::vector<uint8_t> cancelled_id(
          cancelled_result->pbCredentialId,
          cancelled_result->pbCredentialId + cancelled_result->cbCredentialId);
      static_cast<void>(delete_platform_credential(cancelled_id));
    }
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    wipe_envelope(&envelope);
    return PV_BIO_CANCELLED;
  }

  const PWEBAUTHN_CREDENTIAL_ATTESTATION result = attestation.get();
  CredentialPublicKey public_key;
  std::vector<uint8_t> parsed_credential;
  const bool valid_result =
      result->dwVersion >= WEBAUTHN_CREDENTIAL_ATTESTATION_VERSION_7 &&
      result->bResidentKey && result->bPrfEnabled &&
      result->pHmacSecret != nullptr &&
      static_cast<size_t>(result->pHmacSecret->cbFirst) == kPrfBytes &&
      result->pHmacSecret->pbFirst != nullptr &&
      result->pHmacSecret->cbSecond == 0 && result->cbCredentialId > 0 &&
      static_cast<size_t>(result->cbCredentialId) <= kMaxCredentialIdBytes &&
      result->pbCredentialId != nullptr &&
      result->dwUsedTransport == WEBAUTHN_CTAP_TRANSPORT_INTERNAL &&
      parse_attested_credential(
          result->pbAuthenticatorData, result->cbAuthenticatorData,
          envelope.relying_party_hash, &parsed_credential, &public_key) &&
      parsed_credential.size() == result->cbCredentialId &&
      fixed_time_equal(parsed_credential.data(), result->pbCredentialId,
                       result->cbCredentialId);
  if (!valid_result) {
    if (result->pbCredentialId != nullptr && result->cbCredentialId > 0 &&
        static_cast<size_t>(result->cbCredentialId) <= kMaxCredentialIdBytes) {
      const std::vector<uint8_t> invalid_id(result->pbCredentialId,
                                            result->pbCredentialId +
                                                result->cbCredentialId);
      static_cast<void>(delete_platform_credential(invalid_id));
    }
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    secure_wipe(parsed_credential);
    secure_wipe(public_key.x);
    secure_wipe(public_key.y);
    wipe_envelope(&envelope);
    return PV_BIO_NOT_AVAILABLE;
  }

  const ManagedCredentialPresence managed =
      managed_credential_presence(vault_hash, parsed_credential);
  if (managed != ManagedCredentialPresence::present) {
    // The identifier came directly from the just-completed WebAuthn
    // creation response, so it is safe to retire directly. Never make
    // this call with an identifier sourced only from local envelope data.
    static_cast<void>(delete_platform_credential(parsed_credential));
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    secure_wipe(parsed_credential);
    secure_wipe(public_key.x);
    secure_wipe(public_key.y);
    wipe_envelope(&envelope);
    return managed == ManagedCredentialPresence::error ? PV_BIO_INTERNAL_ERROR
                                                       : PV_BIO_NOT_AVAILABLE;
  }

  envelope.credential_id = parsed_credential;
  envelope.public_x = public_key.x;
  envelope.public_y = public_key.y;
  std::array<uint8_t, kPrfBytes> prf_output{};
  std::copy_n(result->pHmacSecret->pbFirst, prf_output.size(),
              prf_output.begin());
  std::array<uint8_t, kHashBytes> wrapping_key{};
  std::vector<uint8_t> aad = encode_envelope_aad(envelope);
  const bool encrypted =
      derive_wrapping_key(prf_output, envelope.kdf_salt, envelope.vault_hash,
                          &wrapping_key) &&
      aes_gcm_encrypt(wrapping_key, envelope.nonce, aad, vault_key,
                      PV_BIO_VAULT_KEY_BYTES, &envelope.ciphertext,
                      &envelope.tag);

  std::vector<uint8_t> old_bytes;
  Envelope old_envelope;
  const FileReadResult old_read =
      read_secure_file(context->metadata_path, &old_bytes);
  const bool old_valid =
      old_read == FileReadResult::present &&
      decode_envelope(old_bytes, &old_envelope) &&
      fixed_time_equal(old_envelope.vault_hash.data(),
                       envelope.vault_hash.data(), envelope.vault_hash.size());
  std::vector<uint8_t> encoded =
      encrypted ? encode_envelope(envelope) : std::vector<uint8_t>{};
  bool cancelled = operation_was_cancelled(context, operation_id);
  bool stored = encrypted && !cancelled &&
                write_secure_file_atomic(context->biometric_directory,
                                         context->metadata_path, encoded);
  if (stored && !commit_operation(context, operation_id)) {
    const bool rolled_back =
        old_valid ? write_secure_file_atomic(context->biometric_directory,
                                             context->metadata_path, old_bytes)
                  : DeleteFileW(context->metadata_path.c_str()) ||
                        GetLastError() == ERROR_FILE_NOT_FOUND;
    static_cast<void>(delete_platform_credential(envelope.credential_id));
    stored = false;
    cancelled = rolled_back;
  }
  if (!stored) {
    static_cast<void>(delete_platform_credential(envelope.credential_id));
  }
  if (stored) {
    // Once the new authenticated envelope is authoritative, enumerate by
    // the trusted RP + caller vault identity and retire older entries.
    // Never delete an ID copied from the unauthenticated old envelope.
    static_cast<void>(
        delete_vault_credentials(vault_hash, &envelope.credential_id));
  }

  secure_wipe(challenge);
  secure_wipe(client_data_json);
  secure_wipe(parsed_credential);
  secure_wipe(public_key.x);
  secure_wipe(public_key.y);
  secure_wipe(prf_output);
  secure_wipe(wrapping_key);
  secure_wipe(aad);
  secure_wipe(old_bytes);
  wipe_envelope(&old_envelope);
  secure_wipe(encoded);
  wipe_envelope(&envelope);
  return stored ? PV_BIO_OK
                : (cancelled ? PV_BIO_CANCELLED : PV_BIO_INTERNAL_ERROR);
}

pv_bio_status retrieve_windows_hello_credential(pv_bio_context *context,
                                                uint64_t operation_id,
                                                const uint8_t *vault_hash,
                                                uint8_t *out_vault_key) {
  std::vector<uint8_t> encoded;
  const FileReadResult read_result =
      read_secure_file(context->metadata_path, &encoded);
  if (read_result == FileReadResult::missing) {
    return PV_BIO_NOT_ENABLED;
  }
  Envelope envelope;
  const std::array<uint8_t, kHashBytes> expected_rp_hash = relying_party_hash();
  if (read_result != FileReadResult::present ||
      !decode_envelope(encoded, &envelope) ||
      !fixed_time_equal(envelope.vault_hash.data(), vault_hash,
                        envelope.vault_hash.size()) ||
      !fixed_time_equal(envelope.relying_party_hash.data(),
                        expected_rp_hash.data(), expected_rp_hash.size())) {
    invalidate_vault_enrollment(context, vault_hash);
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return PV_BIO_INVALIDATED;
  }
  const auto availability = windows_hello_availability();
  if (!credential_inventory_is_authoritative(availability)) {
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return availability_to_operation_status(availability);
  }
  const ManagedCredentialPresence credential_presence =
      managed_credential_presence(vault_hash, envelope.credential_id);
  if (credential_presence != ManagedCredentialPresence::present) {
    if (credential_presence != ManagedCredentialPresence::error) {
      invalidate_vault_enrollment(context, vault_hash);
    }
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return credential_presence == ManagedCredentialPresence::error
               ? PV_BIO_INTERNAL_ERROR
               : PV_BIO_INVALIDATED;
  }

  HWND parent_window = nullptr;
  const pv_bio_status begin_status =
      begin_operation(context, operation_id, &parent_window);
  if (begin_status != PV_BIO_OK) {
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return begin_status;
  }
  OperationGuard operation(context, operation_id);
  if (operation_was_cancelled(context, operation_id)) {
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return PV_BIO_CANCELLED;
  }
  WebAuthnApi &api = webauthn();
  GUID cancellation_id{};
  if (FAILED(api.get_cancellation_id(&cancellation_id)) ||
      install_cancellation_id(context, operation_id, cancellation_id) !=
          PV_BIO_OK) {
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return PV_BIO_CANCELLED;
  }

  std::array<uint8_t, kChallengeBytes> challenge{};
  if (!random_bytes(challenge.data(), challenge.size())) {
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return PV_BIO_INTERNAL_ERROR;
  }
  std::vector<uint8_t> client_data_json =
      make_client_data("webauthn.get", challenge);
  WEBAUTHN_CLIENT_DATA client_data{};
  client_data.dwVersion = WEBAUTHN_CLIENT_DATA_CURRENT_VERSION;
  client_data.cbClientDataJSON = static_cast<DWORD>(client_data_json.size());
  client_data.pbClientDataJSON = client_data_json.data();
  client_data.pwszHashAlgId = WEBAUTHN_HASH_ALGORITHM_SHA_256;

  WEBAUTHN_CREDENTIAL credential{};
  credential.dwVersion = WEBAUTHN_CREDENTIAL_CURRENT_VERSION;
  credential.cbId = static_cast<DWORD>(envelope.credential_id.size());
  credential.pbId = envelope.credential_id.data();
  credential.pwszCredentialType = WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY;

  WEBAUTHN_HMAC_SECRET_SALT prf_input{};
  prf_input.cbFirst = static_cast<DWORD>(envelope.prf_salt.size());
  prf_input.pbFirst = envelope.prf_salt.data();
  WEBAUTHN_HMAC_SECRET_SALT_VALUES prf_values{};
  prf_values.pGlobalHmacSalt = &prf_input;

  WEBAUTHN_AUTHENTICATOR_GET_ASSERTION_OPTIONS options{};
  options.dwVersion = WEBAUTHN_AUTHENTICATOR_GET_ASSERTION_OPTIONS_VERSION_6;
  options.dwTimeoutMilliseconds = kPromptTimeoutMilliseconds;
  options.CredentialList.cCredentials = 1;
  options.CredentialList.pCredentials = &credential;
  options.dwAuthenticatorAttachment =
      WEBAUTHN_AUTHENTICATOR_ATTACHMENT_PLATFORM;
  options.dwUserVerificationRequirement =
      WEBAUTHN_USER_VERIFICATION_REQUIREMENT_REQUIRED;
  options.pCancellationId = &cancellation_id;
  options.pHmacSecretSaltValues = &prf_values;

  AssertionHandle assertion(api);
  const HRESULT assertion_result =
      api.get_assertion(parent_window, kRelyingPartyId, &client_data, &options,
                        assertion.output());
  if (FAILED(assertion_result) || assertion.get() == nullptr) {
    const pv_bio_status mapped = map_webauthn_error(assertion_result, true);
    if (mapped == PV_BIO_INVALIDATED) {
      DeleteFileW(context->metadata_path.c_str());
    }
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return mapped;
  }

  if (operation_was_cancelled(context, operation_id)) {
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return PV_BIO_CANCELLED;
  }

  const PWEBAUTHN_ASSERTION result = assertion.get();
  CredentialPublicKey public_key{envelope.public_x, envelope.public_y};
  const bool user_handle_matches =
      result->cbUserId == 0 ? result->pbUserId == nullptr
                            : result->cbUserId == PV_BIO_VAULT_HASH_BYTES &&
                                  result->pbUserId != nullptr &&
                                  fixed_time_equal(result->pbUserId, vault_hash,
                                                   PV_BIO_VAULT_HASH_BYTES);
  const bool valid_result =
      result->dwVersion >= WEBAUTHN_ASSERTION_VERSION_4 &&
      user_handle_matches &&
      static_cast<size_t>(result->Credential.cbId) ==
          envelope.credential_id.size() &&
      result->Credential.pbId != nullptr &&
      fixed_time_equal(result->Credential.pbId, envelope.credential_id.data(),
                       envelope.credential_id.size()) &&
      result->Credential.pwszCredentialType != nullptr &&
      std::wcscmp(result->Credential.pwszCredentialType,
                  WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY) == 0 &&
      result->pHmacSecret != nullptr &&
      static_cast<size_t>(result->pHmacSecret->cbFirst) == kPrfBytes &&
      result->pHmacSecret->pbFirst != nullptr &&
      result->pHmacSecret->cbSecond == 0 &&
      result->dwUsedTransport == WEBAUTHN_CTAP_TRANSPORT_INTERNAL &&
      valid_assertion_authenticator_data(result->pbAuthenticatorData,
                                         result->cbAuthenticatorData,
                                         envelope.relying_party_hash) &&
      verify_assertion_signature(public_key, result->pbAuthenticatorData,
                                 result->cbAuthenticatorData, client_data_json,
                                 result->pbSignature, result->cbSignature);
  if (!valid_result) {
    invalidate_vault_enrollment(context, vault_hash);
    secure_wipe(challenge);
    secure_wipe(client_data_json);
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    secure_wipe(public_key.x);
    secure_wipe(public_key.y);
    return PV_BIO_INVALIDATED;
  }

  std::array<uint8_t, kPrfBytes> prf_output{};
  std::copy_n(result->pHmacSecret->pbFirst, prf_output.size(),
              prf_output.begin());
  std::array<uint8_t, kHashBytes> wrapping_key{};
  std::vector<uint8_t> aad = encode_envelope_aad(envelope);
  const bool decrypted =
      derive_wrapping_key(prf_output, envelope.kdf_salt, envelope.vault_hash,
                          &wrapping_key) &&
      aes_gcm_decrypt(wrapping_key, envelope.nonce, aad, envelope.ciphertext,
                      envelope.tag, out_vault_key, PV_BIO_VAULT_KEY_BYTES);
  const bool committed = decrypted && commit_operation(context, operation_id);
  if (decrypted && !committed) {
    secure_wipe(out_vault_key, PV_BIO_VAULT_KEY_BYTES);
  } else if (!decrypted) {
    invalidate_vault_enrollment(context, vault_hash);
  }
  secure_wipe(challenge);
  secure_wipe(client_data_json);
  secure_wipe(encoded);
  secure_wipe(public_key.x);
  secure_wipe(public_key.y);
  secure_wipe(prf_output);
  secure_wipe(wrapping_key);
  secure_wipe(aad);
  wipe_envelope(&envelope);
  if (!decrypted)
    return PV_BIO_INVALIDATED;
  return committed ? PV_BIO_OK : PV_BIO_CANCELLED;
}

} // namespace

extern "C" {

uint32_t PV_BIO_CALL pv_bio_abi_version(void) { return PV_BIO_ABI_VERSION; }

pv_bio_status PV_BIO_CALL pv_bio_create(const char *data_directory_utf8,
                                        size_t data_directory_length,
                                        pv_bio_context **out_context) {
  if (out_context == nullptr) {
    return PV_BIO_INTERNAL_ERROR;
  }
  *out_context = nullptr;
  try {
    const auto data_directory =
        utf8_to_utf16(data_directory_utf8, data_directory_length);
    if (!data_directory) {
      return PV_BIO_INTERNAL_ERROR;
    }
    const std::filesystem::path root(*data_directory);
    if (!root.is_absolute() || !safe_directory(root)) {
      return PV_BIO_NOT_AVAILABLE;
    }
    const std::filesystem::path biometric_directory =
        root / kBiometricDirectoryName;
    if (!ensure_safe_directory(biometric_directory)) {
      return PV_BIO_NOT_AVAILABLE;
    }
    auto context = std::make_unique<pv_bio_context>();
    context->biometric_directory = biometric_directory;
    context->metadata_path = biometric_directory / kMetadataFileName;
    *out_context = context.release();
    return PV_BIO_OK;
  } catch (...) {
    return PV_BIO_INTERNAL_ERROR;
  }
}

void PV_BIO_CALL pv_bio_destroy(pv_bio_context *context) {
  if (context == nullptr) {
    return;
  }
  GUID cancellation_id{};
  bool cancel = false;
  {
    std::unique_lock lock(context->operation_mutex);
    context->closing = true;
    context->parent_window = nullptr;
    if (!context->operation_committed) {
      context->cancellation_requested = true;
    }
    if (context->active_operation != 0 && context->has_cancellation_id &&
        !context->operation_committed) {
      cancellation_id = context->cancellation_id;
      cancel = true;
    }
    lock.unlock();
    if (cancel) {
      static_cast<void>(webauthn().cancel(&cancellation_id));
    }
    lock.lock();
    context->operation_finished.wait(
        lock, [context] { return context->active_operation == 0; });
  }
  secure_wipe(&cancellation_id, sizeof(cancellation_id));
  delete context;
}

pv_bio_status PV_BIO_CALL pv_bio_set_parent_window(pv_bio_context *context,
                                                   uintptr_t native_window) {
  if (context == nullptr) {
    return PV_BIO_INTERNAL_ERROR;
  }
  const HWND window = reinterpret_cast<HWND>(native_window);
  if (window != nullptr && !IsWindow(window)) {
    return PV_BIO_NOT_AVAILABLE;
  }
  std::lock_guard lock(context->operation_mutex);
  if (context->closing) {
    return PV_BIO_NOT_AVAILABLE;
  }
  context->parent_window = window;
  return PV_BIO_OK;
}

pv_bio_status PV_BIO_CALL pv_bio_get_capability(pv_bio_context *context,
                                                int32_t *out_availability) {
  if (context == nullptr || out_availability == nullptr) {
    return PV_BIO_INTERNAL_ERROR;
  }
  {
    std::lock_guard lock(context->operation_mutex);
    if (context->closing) {
      return PV_BIO_NOT_AVAILABLE;
    }
  }
  *out_availability = windows_hello_availability();
  return PV_BIO_OK;
}

pv_bio_status PV_BIO_CALL pv_bio_contains(pv_bio_context *context,
                                          const uint8_t *vault_hash,
                                          size_t vault_hash_length,
                                          int32_t *out_contains) {
  if (context == nullptr || !valid_vault_hash(vault_hash, vault_hash_length) ||
      out_contains == nullptr) {
    return PV_BIO_INTERNAL_ERROR;
  }
  *out_contains = 0;
  try {
    std::vector<uint8_t> encoded;
    Envelope envelope;
    const FileReadResult result =
        read_secure_file(context->metadata_path, &encoded);
    const std::array<uint8_t, kHashBytes> expected_rp_hash =
        relying_party_hash();
    if (result == FileReadResult::missing) {
      secure_wipe(encoded);
      wipe_envelope(&envelope);
      return PV_BIO_OK;
    }
    if (result == FileReadResult::present &&
        decode_envelope(encoded, &envelope) &&
        fixed_time_equal(envelope.vault_hash.data(), vault_hash,
                         envelope.vault_hash.size()) &&
        fixed_time_equal(envelope.relying_party_hash.data(),
                         expected_rp_hash.data(), expected_rp_hash.size())) {
      const auto availability = windows_hello_availability();
      if (!credential_inventory_is_authoritative(availability)) {
        // A well-formed enrollment remains enabled while Windows
        // Hello is temporarily locked or unavailable. Retrieval will
        // return the precise capability failure without prompting.
        *out_contains = 1;
        secure_wipe(encoded);
        wipe_envelope(&envelope);
        return PV_BIO_OK;
      }
      const ManagedCredentialPresence presence =
          managed_credential_presence(vault_hash, envelope.credential_id);
      if (presence == ManagedCredentialPresence::present) {
        *out_contains = 1;
      } else if (presence == ManagedCredentialPresence::error) {
        secure_wipe(encoded);
        wipe_envelope(&envelope);
        return PV_BIO_INTERNAL_ERROR;
      } else {
        invalidate_vault_enrollment(context, vault_hash);
        secure_wipe(encoded);
        wipe_envelope(&envelope);
        return PV_BIO_INVALIDATED;
      }
    } else {
      invalidate_vault_enrollment(context, vault_hash);
      secure_wipe(encoded);
      wipe_envelope(&envelope);
      return PV_BIO_INVALIDATED;
    }
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return PV_BIO_OK;
  } catch (...) {
    return PV_BIO_INTERNAL_ERROR;
  }
}

pv_bio_status PV_BIO_CALL pv_bio_enroll(pv_bio_context *context,
                                        uint64_t operation_id,
                                        const uint8_t *vault_hash,
                                        size_t vault_hash_length,
                                        const uint8_t *vault_key,
                                        size_t vault_key_length) {
  if (context == nullptr || !valid_vault_hash(vault_hash, vault_hash_length) ||
      vault_key == nullptr || vault_key_length != PV_BIO_VAULT_KEY_BYTES) {
    return PV_BIO_INTERNAL_ERROR;
  }
  try {
    return create_windows_hello_credential(context, operation_id, vault_hash,
                                           vault_key);
  } catch (...) {
    return PV_BIO_INTERNAL_ERROR;
  }
}

pv_bio_status PV_BIO_CALL pv_bio_retrieve(pv_bio_context *context,
                                          uint64_t operation_id,
                                          const uint8_t *vault_hash,
                                          size_t vault_hash_length,
                                          uint8_t *out_vault_key,
                                          size_t out_vault_key_length) {
  if (context == nullptr || !valid_vault_hash(vault_hash, vault_hash_length) ||
      out_vault_key == nullptr) {
    return PV_BIO_INTERNAL_ERROR;
  }
  if (out_vault_key_length != PV_BIO_VAULT_KEY_BYTES) {
    return out_vault_key_length < PV_BIO_VAULT_KEY_BYTES
               ? PV_BIO_BUFFER_TOO_SMALL
               : PV_BIO_INTERNAL_ERROR;
  }
  secure_wipe(out_vault_key, out_vault_key_length);
  try {
    const pv_bio_status result = retrieve_windows_hello_credential(
        context, operation_id, vault_hash, out_vault_key);
    if (result != PV_BIO_OK) {
      secure_wipe(out_vault_key, out_vault_key_length);
    }
    return result;
  } catch (...) {
    secure_wipe(out_vault_key, out_vault_key_length);
    return PV_BIO_INTERNAL_ERROR;
  }
}

pv_bio_status PV_BIO_CALL pv_bio_delete(pv_bio_context *context,
                                        const uint8_t *vault_hash,
                                        size_t vault_hash_length) {
  if (context == nullptr || !valid_vault_hash(vault_hash, vault_hash_length)) {
    return PV_BIO_INTERNAL_ERROR;
  }
  try {
    {
      std::lock_guard lock(context->operation_mutex);
      if (context->closing)
        return PV_BIO_NOT_AVAILABLE;
      if (context->active_operation != 0)
        return PV_BIO_BUSY;
    }
    std::vector<uint8_t> encoded;
    Envelope envelope;
    const FileReadResult result =
        read_secure_file(context->metadata_path, &encoded);
    if (result == FileReadResult::missing) {
      // There is no local PRF salt or wrapped VEK to release. Clean up
      // any prior orphan when the credential inventory is authoritative,
      // but do not block vault reset/restore solely on an unavailable
      // optional platform integration.
      const auto availability = windows_hello_availability();
      return !credential_inventory_is_authoritative(availability) ||
                     delete_vault_credentials(vault_hash)
                 ? PV_BIO_OK
                 : PV_BIO_INTERNAL_ERROR;
    }
    const auto availability = windows_hello_availability();
    if (availability == PV_BIO_AVAILABILITY_NOT_ENROLLED) {
      // Resetting Windows Hello makes the old device-bound credential
      // unusable. Retire the local salt/ciphertext relationship so the
      // user can explicitly enroll again after password unlock.
      const bool metadata_deleted =
          DeleteFileW(context->metadata_path.c_str()) ||
          GetLastError() == ERROR_FILE_NOT_FOUND;
      secure_wipe(encoded);
      wipe_envelope(&envelope);
      return metadata_deleted ? PV_BIO_OK : PV_BIO_INTERNAL_ERROR;
    }
    if (!credential_inventory_is_authoritative(availability)) {
      secure_wipe(encoded);
      wipe_envelope(&envelope);
      return availability_to_operation_status(availability);
    }
    if (result != FileReadResult::present ||
        !decode_envelope(encoded, &envelope) ||
        !fixed_time_equal(envelope.vault_hash.data(), vault_hash,
                          envelope.vault_hash.size())) {
      const bool credentials_deleted = delete_vault_credentials(vault_hash);
      const bool metadata_deleted =
          DeleteFileW(context->metadata_path.c_str()) ||
          GetLastError() == ERROR_FILE_NOT_FOUND;
      secure_wipe(encoded);
      wipe_envelope(&envelope);
      return credentials_deleted && metadata_deleted ? PV_BIO_OK
                                                     : PV_BIO_INTERNAL_ERROR;
    }
    const bool credential_deleted = delete_vault_credentials(vault_hash);
    const bool metadata_deleted = DeleteFileW(context->metadata_path.c_str()) ||
                                  GetLastError() == ERROR_FILE_NOT_FOUND;
    secure_wipe(encoded);
    wipe_envelope(&envelope);
    return credential_deleted && metadata_deleted ? PV_BIO_OK
                                                  : PV_BIO_INTERNAL_ERROR;
  } catch (...) {
    return PV_BIO_INTERNAL_ERROR;
  }
}

pv_bio_status PV_BIO_CALL pv_bio_cancel(pv_bio_context *context,
                                        uint64_t operation_id) {
  if (context == nullptr || operation_id == 0) {
    return PV_BIO_INTERNAL_ERROR;
  }
  GUID cancellation_id{};
  bool cancel = false;
  {
    std::lock_guard lock(context->operation_mutex);
    if (context->active_operation != operation_id) {
      if (context->active_operation == 0 && !context->closing) {
        context->pending_cancellation = operation_id;
      }
      return PV_BIO_OK;
    }
    if (!context->operation_committed) {
      context->cancellation_requested = true;
    }
    if (context->has_cancellation_id && !context->operation_committed) {
      cancellation_id = context->cancellation_id;
      cancel = true;
    }
  }
  const HRESULT result = cancel ? webauthn().cancel(&cancellation_id) : S_OK;
  secure_wipe(&cancellation_id, sizeof(cancellation_id));
  return SUCCEEDED(result) ? PV_BIO_OK : PV_BIO_CANCELLED;
}

} // extern "C"
