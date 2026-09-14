// Each target includes exactly one implementation; the opt-in historical target
// uses the separately hash-bound full source image, never a reconstructed body.
#if (defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST) + \
     defined(PASSVAULT_BIOMETRIC_PRK_ALLOCATION_TEST) + \
     defined(PASSVAULT_BIOMETRIC_PVA036_AFTER_FLUSH_TEST)) > 1
#error Historical writer, PRK, and after-flush instrumentation are separate targets.
#endif
#if defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST)
#include PASSVAULT_BIOMETRIC_PVA036_SOURCE
#else
#include "../../src/windows/passvault_biometric_windows.cpp"
#endif

#include <cstdio>
#include <cstdlib>
#include <new>
#include <type_traits>

#define PV_TEST_CHECK(condition)                                               \
  do {                                                                         \
    if (!(condition))                                                          \
      return __LINE__;                                                         \
  } while (false)

#if defined(PASSVAULT_BIOMETRIC_PRK_ALLOCATION_TEST)
#if !defined(_MSC_VER) || !defined(_M_X64) || _ITERATOR_DEBUG_LEVEL != 0
#error The allocation-cut evidence requires admitted MSVC x64 with release iterators.
#endif

namespace {
// Trivial TLS has no dynamic initializer. Only this test thread opens the
// allocation window, immediately around the real std::vector construction.
struct PrkAllocationProbe {
  const uint8_t *prk;
  const uint8_t *salt;
  const uint8_t *output;
  size_t expected_bytes;
  size_t intercepted_bytes;
  unsigned registrations;
  unsigned hmac_sites;
  unsigned first_ready;
  unsigned target_sites;
  unsigned target_ready;
  unsigned allocations;
  unsigned before_wipes;
  unsigned after_wipes;
  bool enabled;
  bool inject;
  bool window;
  bool armed;
  bool invalid;
  bool live;
  bool populated_at_target;
  bool before_zero;
  bool after_zero;
  bool injected;
};
static_assert(std::is_trivial_v<PrkAllocationProbe> &&
              std::is_standard_layout_v<PrkAllocationProbe>);
thread_local PrkAllocationProbe prk_probe{};

void disarm_prk_allocation() noexcept {
  prk_probe.armed = false;
  prk_probe.window = false;
}
} // namespace

// This is a replacement allocation function in the dedicated test executable,
// not a throwing callback at an approximate source boundary. Normal allocation
// remains malloc/free-backed; no replacement is linked into the DLL or native14.
void *operator new(size_t length) {
  if (prk_probe.window) {
    ++prk_probe.allocations;
    prk_probe.intercepted_bytes = length;
    if (prk_probe.allocations != 1 || length != prk_probe.expected_bytes ||
        !prk_probe.live) {
      prk_probe.invalid = true;
      disarm_prk_allocation();
      throw std::bad_alloc();
    }
    if (prk_probe.armed) {
      prk_probe.injected = true;
      disarm_prk_allocation(); // Nothing remains armed during C++ unwinding.
      throw std::bad_alloc();
    }
  }
  for (;;) {
    if (void *memory = std::malloc(length == 0 ? 1 : length))
      return memory;
    // Genuine exhaustion is not this test's injected cut or a qualified pass.
    disarm_prk_allocation();
    const std::new_handler handler = std::get_new_handler();
    if (handler == nullptr)
      throw std::bad_alloc();
    handler();
  }
}

void *operator new[](size_t length) {
  if (prk_probe.window) {
    prk_probe.invalid = true; // std::allocator<uint8_t> must use scalar new.
    disarm_prk_allocation();
    throw std::bad_alloc();
  }
  return ::operator new(length);
}
void operator delete(void *memory) noexcept { std::free(memory); }
void operator delete(void *memory, size_t) noexcept { std::free(memory); }
void operator delete[](void *memory) noexcept { std::free(memory); }
void operator delete[](void *memory, size_t) noexcept { std::free(memory); }

namespace {
bool all_zero(const uint8_t *value, size_t length) noexcept {
  uint8_t combined = 0;
  for (size_t index = 0; index < length; ++index)
    combined |= value[index];
  return combined == 0;
}

void pva037_test_register_prk(const uint8_t *value, size_t length) noexcept {
  if (!prk_probe.enabled)
    return;
  ++prk_probe.registrations;
  if (prk_probe.registrations != 1 || value == nullptr || length != kHashBytes) {
    prk_probe.invalid = true;
    return;
  }
  prk_probe.prk = value;
  prk_probe.live = true;
}

void pva037_test_before_hmac_allocation(const uint8_t *key, size_t key_length,
                                       const uint8_t *output,
                                       size_t object_length) noexcept {
  if (!prk_probe.enabled)
    return;
  ++prk_probe.hmac_sites;
  if (prk_probe.hmac_sites == 1) {
    if (!prk_probe.live || key != prk_probe.salt || key_length != kSaltBytes ||
        output != prk_probe.prk || object_length == 0)
      prk_probe.invalid = true;
    return; // Extract must actually succeed before the second HMAC is called.
  }
  ++prk_probe.target_sites;
  if (prk_probe.invalid || prk_probe.hmac_sites != 2 ||
      prk_probe.target_sites != 1 || prk_probe.first_ready != 1 ||
      !prk_probe.live || key != prk_probe.prk || key_length != kHashBytes ||
      output != prk_probe.output || object_length == 0 || prk_probe.window) {
    prk_probe.invalid = true;
    return;
  }
  prk_probe.populated_at_target = !all_zero(key, key_length);
  if (!prk_probe.populated_at_target) {
    prk_probe.invalid = true;
    return;
  }
  prk_probe.expected_bytes = object_length; // Real BCrypt object-size property.
  prk_probe.window = true;
  prk_probe.armed = prk_probe.inject;
}

void pva037_test_after_hmac_allocation(const uint8_t *key) noexcept {
  if (!prk_probe.enabled)
    return;
  if (prk_probe.hmac_sites == 1 && key == prk_probe.salt) {
    ++prk_probe.first_ready;
    return;
  }
  ++prk_probe.target_ready;
  if (key != prk_probe.prk || !prk_probe.window || prk_probe.armed ||
      prk_probe.allocations != 1 || prk_probe.target_ready != 1)
    prk_probe.invalid = true;
  disarm_prk_allocation();
}

void pva037_test_array_wipe(const uint8_t *value, size_t length,
                           bool after) noexcept {
  if (!prk_probe.enabled)
    return;
  if (!prk_probe.live || value != prk_probe.prk || length != kHashBytes) {
    prk_probe.invalid = true;
    disarm_prk_allocation();
    return;
  }
  if (!after) {
    ++prk_probe.before_wipes;
    if (prk_probe.window || prk_probe.armed)
      prk_probe.invalid = true;
    disarm_prk_allocation();
    prk_probe.before_zero = all_zero(value, length);
  } else {
    ++prk_probe.after_wipes;
    prk_probe.after_zero = all_zero(value, length);
    prk_probe.live = false;
    prk_probe.prk = nullptr; // Never inspect a departed stack object in the catch.
  }
}

struct PrkProbeReset final {
  ~PrkProbeReset() noexcept { prk_probe = {}; }
};

int test_prk_allocation(bool inject) {
  std::array<uint8_t, kPrfBytes> prf;
  std::array<uint8_t, kSaltBytes> salt;
  std::array<uint8_t, kHashBytes> vault_hash;
  std::array<uint8_t, kHashBytes> output;
  std::array<uint8_t, kHashBytes> unrelated;
  prf.fill(0xa5);
  salt.fill(0x5a);
  vault_hash.fill(0x3c);
  output.fill(0x91);
  unrelated.fill(0x6d);
  const auto original_prf = prf;
  const auto original_salt = salt;
  const auto original_hash = vault_hash;
  const auto original_output = output;
  const auto original_unrelated = unrelated;
  const PrkProbeReset reset;
  prk_probe = {};
  prk_probe.enabled = true;
  prk_probe.inject = inject;
  prk_probe.salt = salt.data();
  prk_probe.output = output.data();

  bool returned = false;
  bool caught = false;
  bool other_exception = false;
  try {
    returned = derive_wrapping_key(prf, salt, vault_hash, &output);
  } catch (const std::bad_alloc &) {
    caught = true;
  } catch (...) {
    other_exception = true;
  }
  // Saved flags/counters only; the PRK stack array has already ended its lifetime.
  PV_TEST_CHECK(!prk_probe.invalid && !prk_probe.live && !other_exception);
  PV_TEST_CHECK(!prk_probe.window && !prk_probe.armed);
  PV_TEST_CHECK(prk_probe.registrations == 1 && prk_probe.hmac_sites == 2 &&
                prk_probe.first_ready == 1 && prk_probe.target_sites == 1);
  PV_TEST_CHECK(prk_probe.allocations == 1 && prk_probe.expected_bytes > 0 &&
                prk_probe.intercepted_bytes == prk_probe.expected_bytes);
  PV_TEST_CHECK(prk_probe.populated_at_target && prk_probe.before_wipes == 1 &&
                prk_probe.after_wipes == 1 && prk_probe.after_zero);
  PV_TEST_CHECK(prf == original_prf && salt == original_salt &&
                vault_hash == original_hash && unrelated == original_unrelated);
  if (inject) {
    PV_TEST_CHECK(caught && !returned && prk_probe.injected &&
                  prk_probe.target_ready == 0 && !prk_probe.before_zero);
    PV_TEST_CHECK(output == original_output);
  } else {
    // Normal derive tail-wipes PRK before the guard: its before-wipe view is zero.
    PV_TEST_CHECK(returned && !caught && !prk_probe.injected &&
                  prk_probe.target_ready == 1 && prk_probe.before_zero);
    PV_TEST_CHECK(output != original_output && !all_zero(output.data(), output.size()));
  }
  prk_probe.enabled = false;
  PV_TEST_CHECK(std::printf(
                    "PVA037_PRK case=%s allocator=scalar_new events=1 "
                    "object_bytes=%zu populated=YES guard_wiped=YES disarmed=YES\n",
                    inject ? "allocation_cut" : "unarmed", prk_probe.expected_bytes) > 0);
  PV_TEST_CHECK(std::fflush(stdout) == 0);
  return 0;
}
} // namespace

#else // Current native14 or the strictly writer-only historical target.

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

#if defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST)
const char *file_writer_case_name(FileWriterTestCase test_case) {
  switch (test_case) {
  case FileWriterTestCase::success: return "success";
  case FileWriterTestCase::validation_failure: return "validation_failure";
  case FileWriterTestCase::dacl_failure: return "dacl_failure";
  case FileWriterTestCase::collision: return "collision";
  case FileWriterTestCase::empty_payload: return "empty_payload";
  case FileWriterTestCase::oversized_payload: return "oversized_payload";
  case FileWriterTestCase::empty_suffix: return "empty_suffix";
  case FileWriterTestCase::rename_failure: return "rename_failure";
  }
  return "invalid";
}

bool historical_temporary_is_owned_empty(const std::filesystem::path &path) {
  WindowsHandle file(CreateFileW(
      path.c_str(), GENERIC_READ | READ_CONTROL, FILE_SHARE_READ, nullptr,
      OPEN_EXISTING, FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
  if (!file.valid() || !safe_handle(file.get()))
    return false;
  LARGE_INTEGER size{};
  const bool empty = GetFileSizeEx(file.get(), &size) && size.QuadPart == 0;
  const bool closed = file.close();
  return empty && closed;
}
#endif

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
#if defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST)
  const char *historical_oracle = "VALID_CONTROL";
  if (test_case == FileWriterTestCase::validation_failure ||
      test_case == FileWriterTestCase::dacl_failure) {
    // Qualify the particular old red, not just a nonzero exit: the original
    // destination and every callback/handle precondition have already passed.
    PV_TEST_CHECK(std::filesystem::exists(temporary, error) && !error);
    PV_TEST_CHECK(historical_temporary_is_owned_empty(temporary));
    PV_TEST_CHECK(directory_has_only(root, {destination, temporary}));
    historical_oracle = "RED_OWNED_TEMPORARY_RETAINED";
  } else if (test_case == FileWriterTestCase::collision) {
    PV_TEST_CHECK(!file_has_bytes(temporary, sentinel));
    PV_TEST_CHECK(!std::filesystem::exists(temporary, error) && !error);
    PV_TEST_CHECK(directory_has_only(root, {destination}));
    historical_oracle = "RED_UNOWNED_SENTINEL_DELETED";
  } else
#endif
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
#if defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST)
  // No WILL_FAIL/regex inversion. Setup/oracle/cleanup failures above stay FAIL;
  // only an exact observed red (or a valid old control) reaches this terminal record.
  PV_TEST_CHECK(std::printf("PVA036_HISTORICAL case=%s oracle=%s cleanup=PASS\n",
                            file_writer_case_name(test_case), historical_oracle) > 0);
  PV_TEST_CHECK(std::fflush(stdout) == 0);
#endif
  return 0;
}

#if !defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST)
enum class FileWriterIoFault { write_failure, flush_failure, close_failure };

std::optional<FileWriterIoFault> parse_file_writer_io_fault(std::string_view name) {
  if (name == "write_failure")
    return FileWriterIoFault::write_failure;
  if (name == "flush_failure")
    return FileWriterIoFault::flush_failure;
  if (name == "close_failure")
    return FileWriterIoFault::close_failure;
  return std::nullopt;
}

bool same_writer_file(const BY_HANDLE_FILE_INFORMATION &left,
                      const BY_HANDLE_FILE_INFORMATION &right) {
  return left.dwVolumeSerialNumber == right.dwVolumeSerialNumber &&
         left.nFileIndexHigh == right.nFileIndexHigh &&
         left.nFileIndexLow == right.nFileIndexLow &&
         left.ftCreationTime.dwHighDateTime == right.ftCreationTime.dwHighDateTime &&
         left.ftCreationTime.dwLowDateTime == right.ftCreationTime.dwLowDateTime;
}

// One original handle binds identity, exact small synthetic bytes, and close.
// No handle remains open to accidentally block the writer's replace/delete.
bool snapshot_writer_file(const std::filesystem::path &path,
                          const std::vector<uint8_t> &expected,
                          BY_HANDLE_FILE_INFORMATION *identity) {
  std::array<uint8_t, 5> actual{};
  if (identity == nullptr || expected.empty() || expected.size() > actual.size())
    return false;
  WindowsHandle file(CreateFileW(path.c_str(), GENERIC_READ | READ_CONTROL,
                                 FILE_SHARE_READ, nullptr, OPEN_EXISTING,
                                 FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
  DWORD read = 0;
  const bool matches = file.valid() && safe_handle(file.get()) &&
      GetFileInformationByHandle(file.get(), identity) &&
      (identity->dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) == 0 &&
      identity->nNumberOfLinks == 1 && identity->nFileSizeHigh == 0 &&
      identity->nFileSizeLow == expected.size() &&
      ReadFile(file.get(), actual.data(), static_cast<DWORD>(expected.size()),
               &read, nullptr) &&
      read == expected.size() &&
      std::equal(expected.begin(), expected.end(), actual.begin());
  const bool closed = file.close();
  secure_wipe(actual);
  return matches && closed;
}

struct FileWriterIoObservation {
  unsigned int order = 0;
  int writes = 0;
  int flushes = 0;
  int closes = 0;
  DWORD actual_written = 0;
  bool real_write_succeeded = false;
  bool real_flush_succeeded = false;
  bool real_close_succeeded = false;
  bool before_delete_snapshot = false;
  BY_HANDLE_FILE_INFORMATION created{};
  BY_HANDLE_FILE_INFORMATION closed{};
};

struct FaultedFileWriterIo {
  FileWriterIoFault fault;
  const std::filesystem::path &temporary;
  const std::vector<uint8_t> &expected_temporary;
  FileWriterIoObservation &observed;

  bool write(HANDLE file, const uint8_t *bytes, DWORD requested,
             DWORD *written) const {
    observed.order = observed.order * 10 + 3;
    ++observed.writes;
    const DWORD amount = fault == FileWriterIoFault::write_failure
                             ? std::min(requested, DWORD{2}) : requested;
    observed.real_write_succeeded =
        FileWriterWin32Io{}.write(file, bytes, amount, written) && *written == amount;
    observed.actual_written = *written;
    // Deliberate reported failure AFTER a real nonzero prefix write. This is
    // not an assertion that the OS itself returned ERROR_WRITE_FAULT.
    return observed.real_write_succeeded && fault != FileWriterIoFault::write_failure;
  }

  bool flush(HANDLE file) const {
    observed.order = observed.order * 10 + 4;
    ++observed.flushes;
    observed.real_flush_succeeded = FileWriterWin32Io{}.flush(file);
    return observed.real_flush_succeeded && fault != FileWriterIoFault::flush_failure;
  }

  bool close(WindowsHandle &file) const {
    observed.order = observed.order * 10 + 5;
    ++observed.closes;
    observed.real_close_succeeded = FileWriterWin32Io{}.close(file);
    if (observed.real_close_succeeded) {
      observed.before_delete_snapshot =
          snapshot_writer_file(temporary, expected_temporary, &observed.closed) &&
          same_writer_file(observed.created, observed.closed);
    }
    // The real handle is closed even for this conservative false-status cut.
    // It proves the caller's no-delete branch, NOT an actual failed OS close.
    return observed.real_close_succeeded && fault != FileWriterIoFault::close_failure;
  }
};

int test_atomic_writer_io_fault(FileWriterIoFault fault, const char *case_name) {
  const std::wstring suffix = random_suffix();
  PV_TEST_CHECK(!suffix.empty());
  std::error_code error;
  const std::filesystem::path parent = std::filesystem::temp_directory_path(error);
  PV_TEST_CHECK(!error);
  const std::filesystem::path root = parent / (L"passvault-file-io-test-" + suffix);
  TestContextFixture fixture(root);
  fixture.owns_root = std::filesystem::create_directory(root, error);
  PV_TEST_CHECK(fixture.owns_root && !error);
  PV_TEST_CHECK(ensure_safe_directory(root));

  const std::filesystem::path destination = root / kMetadataFileName;
  const std::wstring fixed_suffix = L"0361000000000001";
  const std::filesystem::path temporary = destination.wstring() + L".tmp." + fixed_suffix;
  const std::vector<uint8_t> original{0x01, 0x00, 0x7f, 0xff};
  const std::vector<uint8_t> replacement{0x02, 0xff, 0x00, 0x80, 0x03};
  const std::vector<uint8_t> expected_temporary = fault == FileWriterIoFault::write_failure
      ? std::vector<uint8_t>(replacement.begin(), replacement.begin() + 2) : replacement;
  PV_TEST_CHECK(write_secure_file_atomic(root, destination, original));
  BY_HANDLE_FILE_INFORMATION original_identity{};
  PV_TEST_CHECK(snapshot_writer_file(destination, original, &original_identity));
  PV_TEST_CHECK(directory_has_only(root, {destination}));

  FileWriterIoObservation observed;
  const bool result = write_secure_file_atomic_impl(
      root, destination, replacement, [&]() { return fixed_suffix; },
      [&](HANDLE handle) {
        observed.order = observed.order * 10 + 1;
        return safe_handle(handle) && GetFileInformationByHandle(handle, &observed.created) &&
               observed.created.nNumberOfLinks == 1 && observed.created.nFileSizeHigh == 0 &&
               observed.created.nFileSizeLow == 0;
      },
      [&](HANDLE handle, DWORD inheritance) {
        observed.order = observed.order * 10 + 2;
        return inheritance == NO_INHERITANCE && apply_current_user_only_dacl(handle, inheritance);
      },
      FaultedFileWriterIo{fault, temporary, expected_temporary, observed});
  PV_TEST_CHECK(!result);
  PV_TEST_CHECK(observed.writes == 1 && observed.closes == 1 &&
                observed.real_write_succeeded && observed.real_close_succeeded &&
                observed.actual_written == expected_temporary.size() &&
                observed.before_delete_snapshot);
  const bool write_cut = fault == FileWriterIoFault::write_failure;
  PV_TEST_CHECK(observed.order == (write_cut ? 1235U : 12345U));
  PV_TEST_CHECK(observed.flushes == (write_cut ? 0 : 1));
  PV_TEST_CHECK(observed.real_flush_succeeded == !write_cut);

  // Decisive real-file/identity checks precede all fixture teardown.
  BY_HANDLE_FILE_INFORMATION after_identity{};
  PV_TEST_CHECK(snapshot_writer_file(destination, original, &after_identity));
  PV_TEST_CHECK(same_writer_file(original_identity, after_identity));
  if (fault == FileWriterIoFault::close_failure) {
    BY_HANDLE_FILE_INFORMATION retained_identity{};
    PV_TEST_CHECK(snapshot_writer_file(temporary, replacement, &retained_identity));
    PV_TEST_CHECK(same_writer_file(observed.created, retained_identity));
    PV_TEST_CHECK(directory_has_only(root, {destination, temporary}));
  } else {
    PV_TEST_CHECK(!std::filesystem::exists(temporary, error) && !error);
    PV_TEST_CHECK(directory_has_only(root, {destination}));
  }
  PV_TEST_CHECK(fixture.close());
  PV_TEST_CHECK(!std::filesystem::exists(root, error) && !error);
  PV_TEST_CHECK(std::printf(
      "PVA036_IO case=%s status_injection=YES real_io=PASS pre_teardown=PASS "
      "temp_id=%lu:%lu:%lu destination_id=%lu:%lu:%lu cleanup=PASS\n",
      case_name,
      static_cast<unsigned long>(observed.created.dwVolumeSerialNumber),
      static_cast<unsigned long>(observed.created.nFileIndexHigh),
      static_cast<unsigned long>(observed.created.nFileIndexLow),
      static_cast<unsigned long>(original_identity.dwVolumeSerialNumber),
      static_cast<unsigned long>(original_identity.nFileIndexHigh),
      static_cast<unsigned long>(original_identity.nFileIndexLow)) > 0);
  PV_TEST_CHECK(std::fflush(stdout) == 0);
  return 0;
}

#if defined(PASSVAULT_BIOMETRIC_PVA036_AFTER_FLUSH_TEST)
#if !defined(_MSC_VER) || !defined(_M_X64)
#error The after-flush witness requires admitted MSVC x64.
#endif
// Test-only process-death witness. The existing FileIo seam holds the original
// writer handle after real FlushFileBuffers, before close/rename. No production
// hook, app cancellation, power-loss durability, or automatic orphan cleanup.
constexpr DWORD kAfterFlushWaitMs = 5'000;
constexpr DWORD kAfterFlushGateMs = 15'000;
constexpr DWORD kAfterFlushKilled = 0x036af053;
constexpr DWORD kAfterFlushMagic = 0x036af001;
constexpr wchar_t kAfterFlushSuffix[] = L"0362000000000001";
constexpr wchar_t kAfterFlushSentinel[] = L"unrelated-sentinel.dat";
constexpr LONG kAfterFlushReady = 1;
constexpr LONG kAfterFlushComplete = 2;
constexpr LONG kAfterFlushFailed = 3;

struct AfterFlushShared {
  DWORD magic;
  DWORD expected_process;
  DWORD root_chars;
  wchar_t root[1024];
  BY_HANDLE_FILE_INFORMATION root_identity;
  BY_HANDLE_FILE_INFORMATION created;
  BY_HANDLE_FILE_INFORMATION flushed;
  DWORD order;
  DWORD writes;
  DWORD flushes;
  DWORD closes;
  DWORD written;
  DWORD returned;
  volatile LONG phase;
};
static_assert(std::is_trivial_v<AfterFlushShared> &&
              std::is_standard_layout_v<AfterFlushShared> &&
              sizeof(AfterFlushShared) <= 4096);

LONG after_flush_phase(AfterFlushShared &shared) {
  // The event and this acquire/full-barrier read publish the fixed POD receipt;
  // no std::string, allocator, pointer, or C++ synchronization object is shared.
  return InterlockedCompareExchange(&shared.phase, 0, 0);
}

struct AfterFlushView final {
  AfterFlushShared *value = nullptr;
  ~AfterFlushView() { static_cast<void>(close()); }
  bool open(HANDLE mapping) {
    value = static_cast<AfterFlushShared *>(MapViewOfFile(
        mapping, FILE_MAP_READ | FILE_MAP_WRITE, 0, 0, sizeof(AfterFlushShared)));
    return value != nullptr;
  }
  bool close() {
    if (value == nullptr)
      return true;
    auto *original = value;
    value = nullptr; // No repeat after a reported close failure.
    return UnmapViewOfFile(original) != FALSE;
  }
};

struct AfterFlushChild final {
  PROCESS_INFORMATION info{};
  bool created = false;
  bool settled = false;
  bool termination_requested = false;

  bool wait(DWORD timeout) {
    if (!created || WaitForSingleObject(info.hProcess, timeout) != WAIT_OBJECT_0)
      return false;
    settled = true;
    return true;
  }
  bool terminate() {
    if (!created || settled || termination_requested)
      return false;
    termination_requested = true;
    return TerminateProcess(info.hProcess, kAfterFlushKilled) != FALSE;
  }
  bool close() {
    if (!created || !settled)
      return false;
    const bool thread_closed = info.hThread == nullptr || CloseHandle(info.hThread);
    info.hThread = nullptr;
    const bool process_closed = info.hProcess == nullptr || CloseHandle(info.hProcess);
    info.hProcess = nullptr;
    return thread_closed && process_closed;
  }
  ~AfterFlushChild() {
    if (!created)
      return;
    if (!settled && !wait(0)) {
      if (!termination_requested)
        static_cast<void>(terminate()); // Only the original CreateProcess handle.
      if (!wait(kAfterFlushWaitMs))
        static_cast<void>(std::fputs("PVA036_AFTER_FLUSH child-settlement=HOLD\n", stderr));
    }
    if (settled)
      static_cast<void>(close());
    else {
      // The separate noninherited kill-on-close Job remains the fail-safe.
      if (info.hThread != nullptr)
        static_cast<void>(CloseHandle(info.hThread));
      if (info.hProcess != nullptr)
        static_cast<void>(CloseHandle(info.hProcess));
    }
  }
};

bool after_flush_private_parent(std::filesystem::path *parent) {
  std::array<wchar_t, 1024> selected{}, tmp{}, temp{};
  const DWORD selected_length = GetEnvironmentVariableW(
      L"PASSVAULT_NATIVE_TEST_PARENT", selected.data(), static_cast<DWORD>(selected.size()));
  const DWORD tmp_length = GetEnvironmentVariableW(
      L"TMP", tmp.data(), static_cast<DWORD>(tmp.size()));
  const DWORD temp_length = GetEnvironmentVariableW(
      L"TEMP", temp.data(), static_cast<DWORD>(temp.size()));
  if (selected_length == 0 || selected_length >= selected.size() ||
      tmp_length == 0 || tmp_length >= tmp.size() ||
      temp_length == 0 || temp_length >= temp.size())
    return false;
  *parent = std::filesystem::path(selected.data());
  const auto drive = parent->root_name().native();
  return parent->is_absolute() && drive.size() == 2 && drive[1] == L':' &&
         parent->has_relative_path() && !parent->filename().empty() &&
         *parent == parent->lexically_normal() &&
         *parent == std::filesystem::path(tmp.data()) &&
         *parent == std::filesystem::path(temp.data()) && safe_directory(*parent);
}

bool after_flush_absent(const std::filesystem::path &path) {
  if (GetFileAttributesW(path.c_str()) != INVALID_FILE_ATTRIBUTES)
    return false;
  return GetLastError() == ERROR_FILE_NOT_FOUND;
}

bool after_flush_regular(const BY_HANDLE_FILE_INFORMATION &info, DWORD size) {
  return (info.dwFileAttributes & (FILE_ATTRIBUTE_DIRECTORY | FILE_ATTRIBUTE_REPARSE_POINT)) == 0 &&
         info.nNumberOfLinks == 1 && info.nFileSizeHigh == 0 && info.nFileSizeLow == size;
}

bool after_flush_same_file_id(const BY_HANDLE_FILE_INFORMATION &left,
                              const BY_HANDLE_FILE_INFORMATION &right) {
  // Rename preserves the volume/file ID. NTFS may tunnel a replaced name's
  // creation time; do not mistake that legitimate metadata change for a new ID.
  return left.dwVolumeSerialNumber == right.dwVolumeSerialNumber &&
         left.nFileIndexHigh == right.nFileIndexHigh &&
         left.nFileIndexLow == right.nFileIndexLow;
}

// Cleanup never walks a tree. Mark only the exact observed single-link file for
// deletion through the original no-follow/read+DELETE handle, then close once.
bool after_flush_remove_file(const std::filesystem::path &path,
                             const BY_HANDLE_FILE_INFORMATION &expected,
                             const std::vector<uint8_t> &bytes) {
  if (bytes.empty() || bytes.size() > 5)
    return false;
  WindowsHandle file(CreateFileW(path.c_str(), GENERIC_READ | READ_CONTROL | DELETE,
      FILE_SHARE_READ, nullptr, OPEN_EXISTING, FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
  BY_HANDLE_FILE_INFORMATION actual{};
  std::array<uint8_t, 5> read_bytes{};
  DWORD read = 0;
  FILE_DISPOSITION_INFO disposition{TRUE};
  const bool removed = file.valid() && safe_handle(file.get()) &&
      GetFileInformationByHandle(file.get(), &actual) &&
      same_writer_file(actual, expected) &&
      after_flush_regular(actual, static_cast<DWORD>(bytes.size())) &&
      ReadFile(file.get(), read_bytes.data(), static_cast<DWORD>(bytes.size()), &read, nullptr) &&
      read == bytes.size() && std::equal(bytes.begin(), bytes.end(), read_bytes.begin()) &&
      SetFileInformationByHandle(file.get(), FileDispositionInfo, &disposition,
                                static_cast<DWORD>(sizeof(disposition)));
  const bool closed = file.close();
  return removed && closed && after_flush_absent(path);
}

bool after_flush_handle_arg(const char *argument, HANDLE *handle) {
  if (argument == nullptr || *argument == '\0')
    return false;
  uintptr_t value = 0;
  size_t length = 0;
  for (const char *next = argument; *next != '\0'; ++next) {
    if (++length > 20 || *next < '0' || *next > '9')
      return false;
    const auto digit = static_cast<uintptr_t>(*next - '0');
    if (value > (std::numeric_limits<uintptr_t>::max() - digit) / 10)
      return false;
    value = value * 10 + digit;
  }
  if (value == 0 || value == std::numeric_limits<uintptr_t>::max())
    return false;
  *handle = reinterpret_cast<HANDLE>(value);
  DWORD flags = 0;
  return GetHandleInformation(*handle, &flags) && (flags & HANDLE_FLAG_INHERIT) != 0;
}

struct AfterFlushWriterIo final {
  AfterFlushShared &shared;
  HANDLE ready;
  HANDLE release;

  bool write(HANDLE file, const uint8_t *bytes, DWORD requested, DWORD *written) const {
    shared.order = shared.order * 10 + 3;
    ++shared.writes;
    const bool result = FileWriterWin32Io{}.write(file, bytes, requested, written);
    shared.written = *written;
    return result && shared.writes == 1 && requested == 5 && *written == requested;
  }
  bool flush(HANDLE file) const {
    shared.order = shared.order * 10 + 4;
    ++shared.flushes;
    if (!FileWriterWin32Io{}.flush(file) || shared.flushes != 1 ||
        shared.order != 1234 || shared.closes != 0 || shared.written != 5 ||
        !safe_handle(file) || !GetFileInformationByHandle(file, &shared.flushed) ||
        !after_flush_regular(shared.flushed, 5) ||
        !same_writer_file(shared.created, shared.flushed))
      return false;
    InterlockedExchange(&shared.phase, kAfterFlushReady);
    if (!SetEvent(ready) || WaitForSingleObject(release, kAfterFlushGateMs) != WAIT_OBJECT_0) {
      InterlockedExchange(&shared.phase, kAfterFlushFailed);
      return false;
    }
    return true; // Normal-release control preserves the real successful flush.
  }
  bool close(WindowsHandle &file) const {
    shared.order = shared.order * 10 + 5;
    ++shared.closes;
    return FileWriterWin32Io{}.close(file);
  }
};

int after_flush_child(HANDLE mapping_value, HANDLE ready_value, HANDLE release_value) {
  WindowsHandle mapping(mapping_value), ready(ready_value), release(release_value);
  AfterFlushView view;
  PV_TEST_CHECK(view.open(mapping.get()));
  auto &shared = *view.value;
  PV_TEST_CHECK(after_flush_phase(shared) == 0 && shared.magic == kAfterFlushMagic &&
                shared.expected_process == GetCurrentProcessId() &&
                shared.root_chars > 0 && shared.root_chars < 1024 &&
                shared.root[shared.root_chars] == L'\0');
  const std::filesystem::path root(std::wstring(shared.root, shared.root_chars));
  std::filesystem::path parent;
  PV_TEST_CHECK(after_flush_private_parent(&parent) && root.parent_path() == parent &&
                root == root.lexically_normal());
  WindowsHandle directory(CreateFileW(root.c_str(), FILE_READ_ATTRIBUTES | READ_CONTROL,
      FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE, nullptr, OPEN_EXISTING,
      FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
  BY_HANDLE_FILE_INFORMATION root_identity{};
  PV_TEST_CHECK(directory.valid() && safe_handle(directory.get()) &&
                GetFileInformationByHandle(directory.get(), &root_identity) &&
                (root_identity.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0 &&
                same_writer_file(root_identity, shared.root_identity));
  PV_TEST_CHECK(directory.close());
  const std::vector<uint8_t> replacement{0x02, 0xff, 0x00, 0x80, 0x03};
  const bool result = write_secure_file_atomic_impl(
      root, root / kMetadataFileName, replacement, [] { return std::wstring(kAfterFlushSuffix); },
      [&](HANDLE handle) {
        shared.order = shared.order * 10 + 1;
        return safe_handle(handle) && GetFileInformationByHandle(handle, &shared.created) &&
               after_flush_regular(shared.created, 0);
      },
      [&](HANDLE handle, DWORD inheritance) {
        shared.order = shared.order * 10 + 2;
        return inheritance == NO_INHERITANCE && apply_current_user_only_dacl(handle, inheritance);
      }, AfterFlushWriterIo{shared, ready.get(), release.get()});
  shared.returned = result ? 1 : 2;
  const bool success = result && shared.order == 12345 && shared.closes == 1;
  InterlockedExchange(&shared.phase, success ? kAfterFlushComplete : kAfterFlushFailed);
  const bool unmapped = view.close();
  const bool mapping_closed = mapping.close();
  const bool ready_closed = ready.close();
  const bool release_closed = release.close();
  PV_TEST_CHECK(success && unmapped && mapping_closed && ready_closed && release_closed);
  return 0;
}

int test_after_flush_process(bool kill_child) {
  std::filesystem::path parent;
  PV_TEST_CHECK(after_flush_private_parent(&parent)); // No system-TEMP fallback.
  const std::wstring suffix = random_suffix();
  PV_TEST_CHECK(!suffix.empty());
  const std::filesystem::path root = parent / (L"passvault-after-flush-" + suffix);
  PV_TEST_CHECK(root.native().size() < 1024 && CreateDirectoryW(root.c_str(), nullptr));
  // Failure retains this exclusively created fixture; no destructor/remove_all
  // can erase a failed oracle or a root with uncertain child settlement.
  PV_TEST_CHECK(ensure_safe_directory(root));
  WindowsHandle root_handle(CreateFileW(root.c_str(), FILE_READ_ATTRIBUTES | READ_CONTROL | DELETE,
      FILE_SHARE_READ | FILE_SHARE_WRITE, nullptr, OPEN_EXISTING,
      FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
  BY_HANDLE_FILE_INFORMATION root_identity{};
  PV_TEST_CHECK(root_handle.valid() && safe_handle(root_handle.get()) &&
                GetFileInformationByHandle(root_handle.get(), &root_identity) &&
                (root_identity.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0);
  PV_TEST_CHECK(std::printf(
      "PVA036_AFTER_FLUSH case=%s fixture=%ls root_id=%lu:%lu:%lu setup=OWNED\n",
      kill_child ? "process_death" : "continue", root.filename().c_str(),
      static_cast<unsigned long>(root_identity.dwVolumeSerialNumber),
      static_cast<unsigned long>(root_identity.nFileIndexHigh),
      static_cast<unsigned long>(root_identity.nFileIndexLow)) > 0);
  PV_TEST_CHECK(std::fflush(stdout) == 0);
  const auto destination = root / kMetadataFileName;
  const auto sentinel = root / kAfterFlushSentinel;
  const std::filesystem::path temporary = destination.wstring() + L".tmp." + kAfterFlushSuffix;
  const std::vector<uint8_t> original{0x01, 0x00, 0x7f, 0xff};
  const std::vector<uint8_t> replacement{0x02, 0xff, 0x00, 0x80, 0x03};
  const std::vector<uint8_t> sentinel_bytes{0x53, 0x00, 0x45, 0xff};
  PV_TEST_CHECK(write_secure_file_atomic(root, destination, original));
  PV_TEST_CHECK(write_secure_file_atomic(root, sentinel, sentinel_bytes));
  BY_HANDLE_FILE_INFORMATION original_identity{}, sentinel_identity{};
  PV_TEST_CHECK(snapshot_writer_file(destination, original, &original_identity) &&
                snapshot_writer_file(sentinel, sentinel_bytes, &sentinel_identity) &&
                directory_has_only(root, {destination, sentinel}) && after_flush_absent(temporary));

  WindowsHandle job(CreateJobObjectW(nullptr, nullptr));
  JOBOBJECT_EXTENDED_LIMIT_INFORMATION limits{};
  limits.BasicLimitInformation.LimitFlags =
      JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE | JOB_OBJECT_LIMIT_ACTIVE_PROCESS;
  limits.BasicLimitInformation.ActiveProcessLimit = 1;
  PV_TEST_CHECK(job.valid() && SetInformationJobObject(job.get(), JobObjectExtendedLimitInformation,
      &limits, static_cast<DWORD>(sizeof(limits))));
  SECURITY_ATTRIBUTES inheritable{static_cast<DWORD>(sizeof(SECURITY_ATTRIBUTES)), nullptr, TRUE};
  WindowsHandle mapping(CreateFileMappingW(INVALID_HANDLE_VALUE, &inheritable, PAGE_READWRITE,
      0, static_cast<DWORD>(sizeof(AfterFlushShared)), nullptr));
  WindowsHandle ready(CreateEventW(&inheritable, TRUE, FALSE, nullptr));
  WindowsHandle release(CreateEventW(&inheritable, TRUE, FALSE, nullptr));
  PV_TEST_CHECK(mapping.valid() && ready.valid() && release.valid());
  AfterFlushView view;
  PV_TEST_CHECK(view.open(mapping.get()));
  new (view.value) AfterFlushShared{};
  auto &shared = *view.value;
  shared.magic = kAfterFlushMagic;
  shared.root_chars = static_cast<DWORD>(root.native().size());
  std::wmemcpy(shared.root, root.c_str(), root.native().size() + 1);
  shared.root_identity = root_identity;

  std::array<wchar_t, 2048> image_buffer{};
  const DWORD image_length = GetModuleFileNameW(nullptr, image_buffer.data(),
                                               static_cast<DWORD>(image_buffer.size()));
  PV_TEST_CHECK(image_length > 0 && image_length < image_buffer.size());
  const std::wstring image(image_buffer.data(), image_length);
  PV_TEST_CHECK(image.find(L'"') == std::wstring::npos);
  std::wstring command = L"\"" + image + L"\" --after-flush-child " +
      std::to_wstring(reinterpret_cast<uintptr_t>(mapping.get())) + L" " +
      std::to_wstring(reinterpret_cast<uintptr_t>(ready.get())) + L" " +
      std::to_wstring(reinterpret_cast<uintptr_t>(release.get()));
  std::array<HANDLE, 3> inherited{mapping.get(), ready.get(), release.get()};
  SIZE_T attribute_bytes = 0;
  PV_TEST_CHECK(!InitializeProcThreadAttributeList(nullptr, 1, 0, &attribute_bytes) &&
                GetLastError() == ERROR_INSUFFICIENT_BUFFER);
  alignas(std::max_align_t) std::array<uint8_t, 4096> attribute_buffer{};
  PV_TEST_CHECK(attribute_bytes > 0 && attribute_bytes <= attribute_buffer.size());
  auto *attributes = reinterpret_cast<LPPROC_THREAD_ATTRIBUTE_LIST>(attribute_buffer.data());
  PV_TEST_CHECK(InitializeProcThreadAttributeList(attributes, 1, 0, &attribute_bytes));
  struct AttributeCloser final {
    LPPROC_THREAD_ATTRIBUTE_LIST value;
    ~AttributeCloser() { DeleteProcThreadAttributeList(value); }
  } attribute_closer{attributes};
  PV_TEST_CHECK(UpdateProcThreadAttribute(attributes, 0, PROC_THREAD_ATTRIBUTE_HANDLE_LIST,
      inherited.data(), inherited.size() * sizeof(HANDLE), nullptr, nullptr));
  STARTUPINFOEXW startup{};
  startup.StartupInfo.cb = static_cast<DWORD>(sizeof(startup));
  startup.lpAttributeList = attributes;
  AfterFlushChild child; // Destroyed before the Job, IPC, and directory handles.
  child.created = CreateProcessW(image.c_str(), command.data(), nullptr, nullptr, TRUE,
      CREATE_SUSPENDED | CREATE_NO_WINDOW | EXTENDED_STARTUPINFO_PRESENT, nullptr,
      parent.c_str(), &startup.StartupInfo, &child.info) != FALSE;
  PV_TEST_CHECK(child.created);
  PV_TEST_CHECK(AssignProcessToJobObject(job.get(), child.info.hProcess));
  BOOL in_job = FALSE;
  FILETIME creation{}, exit_time{}, kernel{}, user{};
  PV_TEST_CHECK(IsProcessInJob(child.info.hProcess, job.get(), &in_job) && in_job &&
                GetProcessId(child.info.hProcess) == child.info.dwProcessId &&
                GetProcessIdOfThread(child.info.hThread) == child.info.dwProcessId &&
                GetProcessTimes(child.info.hProcess, &creation, &exit_time, &kernel, &user));
  shared.expected_process = child.info.dwProcessId;
  MemoryBarrier(); // Publish the parent's fixed mapping before the child runs.
  PV_TEST_CHECK(ResumeThread(child.info.hThread) == 1);
  std::array<HANDLE, 2> ready_or_exit{ready.get(), child.info.hProcess};
  PV_TEST_CHECK(WaitForMultipleObjects(static_cast<DWORD>(ready_or_exit.size()),
      ready_or_exit.data(), FALSE, kAfterFlushWaitMs) == WAIT_OBJECT_0);
  PV_TEST_CHECK(WaitForSingleObject(child.info.hProcess, 0) == WAIT_TIMEOUT &&
                after_flush_phase(shared) == kAfterFlushReady && shared.order == 1234 &&
                shared.writes == 1 && shared.flushes == 1 && shared.closes == 0 &&
                shared.returned == 0 && after_flush_regular(shared.created, 0) &&
                after_flush_regular(shared.flushed, 5) &&
                same_writer_file(shared.created, shared.flushed) &&
                !after_flush_same_file_id(shared.created, original_identity) &&
                !after_flush_same_file_id(shared.created, sentinel_identity));
  // The writer still owns its share-zero handle: a pathname reader must fail
  // specifically with sharing violation, not missing/permission/setup failure.
  WindowsHandle blocked(CreateFileW(temporary.c_str(), GENERIC_READ | READ_CONTROL,
      FILE_SHARE_READ, nullptr, OPEN_EXISTING, FILE_FLAG_OPEN_REPARSE_POINT, nullptr));
  const DWORD blocked_error = GetLastError();
  PV_TEST_CHECK(!blocked.valid() && blocked_error == ERROR_SHARING_VIOLATION);
  BY_HANDLE_FILE_INFORMATION before_destination{}, before_sentinel{};
  PV_TEST_CHECK(snapshot_writer_file(destination, original, &before_destination) &&
                same_writer_file(original_identity, before_destination) &&
                snapshot_writer_file(sentinel, sentinel_bytes, &before_sentinel) &&
                same_writer_file(sentinel_identity, before_sentinel) &&
                directory_has_only(root, {destination, sentinel, temporary}));
  if (kill_child)
    PV_TEST_CHECK(child.terminate());
  else
    PV_TEST_CHECK(SetEvent(release.get()));
  PV_TEST_CHECK(child.wait(kAfterFlushWaitMs));
  DWORD exit_code = STILL_ACTIVE;
  FILETIME after_creation{};
  PV_TEST_CHECK(GetExitCodeProcess(child.info.hProcess, &exit_code) &&
                exit_code == (kill_child ? kAfterFlushKilled : DWORD{0}) &&
                GetProcessTimes(child.info.hProcess, &after_creation, &exit_time, &kernel, &user) &&
                CompareFileTime(&creation, &after_creation) == 0);
  JOBOBJECT_BASIC_ACCOUNTING_INFORMATION accounting{};
  const ULONGLONG settle_end = GetTickCount64() + kAfterFlushWaitMs;
  do {
    PV_TEST_CHECK(QueryInformationJobObject(job.get(), JobObjectBasicAccountingInformation,
        &accounting, static_cast<DWORD>(sizeof(accounting)), nullptr));
    if (accounting.ActiveProcesses == 0)
      break;
    PV_TEST_CHECK(GetTickCount64() < settle_end);
    Sleep(1);
  } while (true);
  PV_TEST_CHECK(accounting.TotalProcesses == 1);

  BY_HANDLE_FILE_INFORMATION final_destination{}, final_sentinel{}, retained_temporary{};
  PV_TEST_CHECK(snapshot_writer_file(sentinel, sentinel_bytes, &final_sentinel) &&
                same_writer_file(sentinel_identity, final_sentinel));
  if (kill_child) {
    PV_TEST_CHECK(after_flush_phase(shared) == kAfterFlushReady && shared.order == 1234 &&
                  shared.closes == 0 && shared.returned == 0);
    PV_TEST_CHECK(snapshot_writer_file(destination, original, &final_destination) &&
                  same_writer_file(original_identity, final_destination));
    // Expected crash residual, not a product cleanup success. OS process death
    // releases the handle; the still-owned flushed staging file survives.
    PV_TEST_CHECK(snapshot_writer_file(temporary, replacement, &retained_temporary) &&
                  same_writer_file(shared.created, retained_temporary) &&
                  directory_has_only(root, {destination, sentinel, temporary}));
  } else {
    PV_TEST_CHECK(after_flush_phase(shared) == kAfterFlushComplete && shared.order == 12345 &&
                  shared.closes == 1 && shared.returned == 1 && !child.termination_requested);
    PV_TEST_CHECK(snapshot_writer_file(destination, replacement, &final_destination) &&
                  after_flush_same_file_id(shared.created, final_destination) && after_flush_absent(temporary) &&
                  directory_has_only(root, {destination, sentinel}));
  }
  const auto created_identity = shared.created;
  PV_TEST_CHECK(child.close());
  const bool unmapped = view.close();
  const bool mapping_closed = mapping.close();
  const bool ready_closed = ready.close();
  const bool release_closed = release.close();
  const bool job_closed = job.close();
  PV_TEST_CHECK(unmapped && mapping_closed && ready_closed && release_closed && job_closed);

  // Every decisive filesystem/phase/identity assertion above precedes deletion.
  if (kill_child)
    PV_TEST_CHECK(after_flush_remove_file(temporary, retained_temporary, replacement));
  PV_TEST_CHECK(after_flush_remove_file(destination, final_destination, kill_child ? original : replacement));
  PV_TEST_CHECK(after_flush_remove_file(sentinel, final_sentinel, sentinel_bytes));
  BY_HANDLE_FILE_INFORMATION final_root{};
  FILE_DISPOSITION_INFO delete_root{TRUE};
  PV_TEST_CHECK(directory_has_only(root, {}) && safe_handle(root_handle.get()) &&
                GetFileInformationByHandle(root_handle.get(), &final_root) &&
                same_writer_file(root_identity, final_root) &&
                SetFileInformationByHandle(root_handle.get(), FileDispositionInfo, &delete_root,
                                          static_cast<DWORD>(sizeof(delete_root))));
  PV_TEST_CHECK(root_handle.close() && after_flush_absent(root));
  PV_TEST_CHECK(std::printf(
      "PVA036_AFTER_FLUSH case=%s process=%lu creation=%lu:%lu exit=%lu "
      "stage_id=%lu:%lu:%lu destination_id=%lu:%lu:%lu "
      "real_flush=PASS held_before_close=PASS action=%s residual=%s pre_teardown=PASS cleanup=PASS\n",
      kill_child ? "process_death" : "continue", static_cast<unsigned long>(child.info.dwProcessId),
      static_cast<unsigned long>(creation.dwHighDateTime), static_cast<unsigned long>(creation.dwLowDateTime),
      static_cast<unsigned long>(exit_code), static_cast<unsigned long>(created_identity.dwVolumeSerialNumber),
      static_cast<unsigned long>(created_identity.nFileIndexHigh), static_cast<unsigned long>(created_identity.nFileIndexLow),
      static_cast<unsigned long>(final_destination.dwVolumeSerialNumber),
      static_cast<unsigned long>(final_destination.nFileIndexHigh), static_cast<unsigned long>(final_destination.nFileIndexLow),
      kill_child ? "TERMINATED" : "RELEASED", kill_child ? "OWNED_FLUSHED_STAGING" : "NONE") > 0);
  PV_TEST_CHECK(std::fflush(stdout) == 0);
  return 0;
}
#endif // Dedicated after-flush target only; not the DLL/native14/PRK/history.

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

int test_kdf_known_answer() {
  static_assert(kPrfBytes == 32 && kSaltBytes == 32 && kHashBytes == 32);
  // Independent synthetic oracle: docs/audit-continuation/2026-09-08-linux/
  // reviews/team20/root/C20-KDF-ORACLE-ACTUAL.json, not this CNG helper.
  constexpr std::array<uint8_t, kHashBytes> expected_ascending = {
      0x97, 0x7e, 0xa1, 0xdc, 0xe4, 0x26, 0x67, 0x94,
      0x46, 0x49, 0x32, 0xf3, 0x46, 0x40, 0x33, 0x1e,
      0xe4, 0xd5, 0x8b, 0xfb, 0xd1, 0x25, 0x74, 0x67,
      0xbe, 0x3d, 0x7c, 0xc3, 0x78, 0x71, 0x4c, 0xaf};
  constexpr std::array<uint8_t, kHashBytes> expected_changed_vault = {
      0xb8, 0x3f, 0x58, 0xef, 0x0f, 0x92, 0x97, 0xd7,
      0xff, 0x8a, 0x34, 0x87, 0xe6, 0xf2, 0xd6, 0x57,
      0x2a, 0xed, 0xb7, 0x8e, 0xca, 0x9f, 0x2b, 0xb9,
      0xa3, 0x86, 0x46, 0x2d, 0x32, 0x24, 0xdc, 0x20};
  std::array<uint8_t, kPrfBytes> prf{};
  std::array<uint8_t, kSaltBytes> salt{};
  std::array<uint8_t, kHashBytes> vault_hash{};
  for (size_t index = 0; index < kHashBytes; ++index) {
    prf[index] = static_cast<uint8_t>(0x47 + index);
    salt[index] = static_cast<uint8_t>(0x33 + index);
    vault_hash[index] = static_cast<uint8_t>(0x01 + index);
  }
  const auto original_prf = prf;
  const auto original_salt = salt;
  const auto original_vault_hash = vault_hash;
  auto changed_vault_hash = vault_hash;
  changed_vault_hash[0] = 0x00;
  const auto original_changed_vault_hash = changed_vault_hash;
  std::array<uint8_t, kHashBytes> unrelated;
  unrelated.fill(0x6d);
  const auto original_unrelated = unrelated;
  std::array<uint8_t, kHashBytes> output{};
  const ScopedArrayWipe<kHashBytes> wipe_output(output);

  output.fill(0x91);
  PV_TEST_CHECK(derive_wrapping_key(prf, salt, vault_hash, &output));
  PV_TEST_CHECK(output == expected_ascending);
  PV_TEST_CHECK(prf == original_prf && salt == original_salt &&
                vault_hash == original_vault_hash &&
                changed_vault_hash == original_changed_vault_hash &&
                unrelated == original_unrelated);
  output.fill(0x91);
  PV_TEST_CHECK(derive_wrapping_key(prf, salt, changed_vault_hash, &output));
  PV_TEST_CHECK(output == expected_changed_vault);
  PV_TEST_CHECK(prf == original_prf && salt == original_salt &&
                vault_hash == original_vault_hash &&
                changed_vault_hash == original_changed_vault_hash &&
                unrelated == original_unrelated);
  return 0;
}
#endif // Not the historical source image: it has no ScopedArrayWipe declaration.
#endif // Not the PRK-only target.

int main(int argc, char *argv[]) {
#if defined(PASSVAULT_BIOMETRIC_PRK_ALLOCATION_TEST)
  PV_TEST_CHECK(argc == 3 && std::string_view(argv[1]) == "--prk-case");
  if (std::string_view(argv[2]) == "unarmed")
    return test_prk_allocation(false);
  if (std::string_view(argv[2]) == "allocation_cut")
    return test_prk_allocation(true);
  return __LINE__; // No no-argument, filesystem, guard-unit or provider route.
#elif defined(PASSVAULT_BIOMETRIC_PVA036_AFTER_FLUSH_TEST)
  if (argc == 3 && std::string_view(argv[1]) == "--after-flush-case") {
    const std::string_view selected(argv[2]);
    PV_TEST_CHECK(selected == "continue" || selected == "process_death");
    int result = 0;
    try {
      result = test_after_flush_process(selected == "process_death");
    } catch (...) {
      // Unwind original owners before reporting HOLD; never erase a failed fixture.
      result = __LINE__;
    }
    if (result != 0) {
      static_cast<void>(std::fprintf(stderr,
          "PVA036_AFTER_FLUSH case=%s result=FAIL line=%d cleanup=HOLD retry=NO\n",
          argv[2], result));
      static_cast<void>(std::fflush(stderr));
    }
    return result;
  }
  if (argc == 5 && std::string_view(argv[1]) == "--after-flush-child") {
    HANDLE mapping = nullptr, ready = nullptr, release = nullptr;
    PV_TEST_CHECK(after_flush_handle_arg(argv[2], &mapping) &&
                  after_flush_handle_arg(argv[3], &ready) &&
                  after_flush_handle_arg(argv[4], &release) &&
                  mapping != ready && mapping != release && ready != release);
    try {
      return after_flush_child(mapping, ready, release);
    } catch (...) {
      return __LINE__; // Parent observes premature/nonzero exit, never PASS.
    }
  }
  return __LINE__; // No default/old-case/context/provider or arbitrary PID/path route.
#else
  if (argc == 3 && std::string_view(argv[1]) == "--file-case") {
    const auto test_case = parse_file_writer_case(argv[2]);
    PV_TEST_CHECK(test_case.has_value());
    return test_atomic_writer(*test_case);
  }
#if defined(PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST)
  return __LINE__; // The historical executable accepts ONLY the eight writer cases.
#else
  if (argc == 3 && std::string_view(argv[1]) == "--secret-wipe-case") {
    const auto test_case = parse_secret_wipe_case(argv[2]);
    PV_TEST_CHECK(test_case.has_value());
    return test_scoped_array_wipe(*test_case);
  }
  if (argc == 2 && std::string_view(argv[1]) == "--kdf-known-answer") {
    return test_kdf_known_answer();
  }
  if (argc == 3 && std::string_view(argv[1]) == "--file-io-case") {
    const auto fault = parse_file_writer_io_fault(argv[2]);
    PV_TEST_CHECK(fault.has_value());
    return test_atomic_writer_io_fault(*fault, argv[2]);
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
#endif // Current native14, not historical writer dispatch.
#endif // Current/historical writer target, not PRK-only dispatch.
}
