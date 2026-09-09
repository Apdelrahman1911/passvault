// Compile the reviewed implementation into this test-only executable so its
// metadata and error-mapping helpers remain private in the production ABI.
#include "../../src/macos/passvault_biometric_macos.mm"

#include <cstdio>
#include <cstdlib>
#include <future>

#define PV_TEST_CHECK(condition)                                               \
  do {                                                                         \
    if (!(condition)) {                                                        \
      std::fprintf(stderr, "Native test check failed at line %d\n", __LINE__);  \
      return 1;                                                                \
    }                                                                          \
  } while (false)

namespace {

struct FixtureCleanup {
  unsigned attempts = 0;
  bool child_created = false;
  bool child_absent = false;
  bool descriptors_closed = false;
  bool success = false;
};

// Test-only ownership of one exclusively created child. The caller must supply
// an independently admitted private parent; this is not a same-user sandbox or
// a replacement for the runner's process, interruption and cleanup admission.
class NativeFixture final {
public:
  NativeFixture(FixtureCleanup &result, bool &all_cleanup_ok) noexcept
      : result_(result), all_cleanup_ok_(all_cleanup_ok) {}
  ~NativeFixture() { cleanup(); }
  NativeFixture(const NativeFixture &) = delete;
  NativeFixture &operator=(const NativeFixture &) = delete;

  bool create() {
    if (started_ || result_.attempts != 0) {
      return false;
    }
    started_ = true;
    const char *parent = std::getenv("PASSVAULT_NATIVE_TEST_PARENT");
    const char *last_separator = parent == nullptr ? nullptr : std::strrchr(parent, '/');
    // A trailing slash or dot component can make O_NOFOLLOW apply to something
    // other than the final named directory. Ancestors still need runner review.
    if (parent == nullptr || parent[0] != '/' || std::strlen(parent) > 2048 ||
        last_separator == nullptr || last_separator[1] == '\0' ||
        std::strcmp(last_separator + 1, ".") == 0 ||
        std::strcmp(last_separator + 1, "..") == 0) {
      std::fprintf(stderr, "A private PASSVAULT_NATIVE_TEST_PARENT is required\n");
      return false;
    }
    std::array<uint8_t, 8> suffix{};
    if (SecRandomCopyBytes(kSecRandomDefault, suffix.size(), suffix.data()) !=
        errSecSuccess) {
      return false;
    }
    // Complete every throwing path allocation before mkdirat grants ownership.
    name_ = "passvault-biometric-native-test." +
            hexadecimal(suffix.data(), suffix.size());
    root_ = std::string(parent) + "/" + name_;
    biometric_ = root_ + "/biometric";
    metadata_ = biometric_ + "/metadata";
    synthetic_target_ = root_ + "/synthetic-metadata";
    parent_fd_ = open(parent, O_RDONLY | O_DIRECTORY | O_NOFOLLOW | O_CLOEXEC);
    if (!capture_directory(parent_fd_, &parent_identity_)) {
      return false;
    }
    if (mkdirat(parent_fd_, name_.c_str(), 0700) != 0) {
      // On other failures, do not infer absence or adopt an uncertain entry.
      creation_uncertain_ = errno != EEXIST;
      return false; // A collision grants no ownership; no adoption or retry.
    }
    result_.child_created = true;
    root_fd_ = openat(parent_fd_, name_.c_str(),
                      O_RDONLY | O_DIRECTORY | O_NOFOLLOW | O_CLOEXEC);
    root_bound_ = capture_directory(root_fd_, &root_identity_);
    if (!root_bound_ || mkdirat(root_fd_, "biometric", 0700) != 0) {
      return false;
    }
    biometric_created_ = true;
    biometric_fd_ = openat(root_fd_, "biometric",
                           O_RDONLY | O_DIRECTORY | O_NOFOLLOW | O_CLOEXEC);
    biometric_bound_ = capture_directory(biometric_fd_, &biometric_identity_);
    return biometric_bound_;
  }

  bool cleanup() noexcept {
    if (result_.attempts != 0) {
      return result_.success; // A failed explicit attempt is never retried.
    }
    ++result_.attempts;
    bool ok = !creation_uncertain_;
    if (result_.child_created) {
      ok = root_bound_ && descriptor_matches(parent_fd_, parent_identity_) &&
           descriptor_matches(root_fd_, root_identity_) &&
           named_directory_matches(parent_fd_, name_.c_str(), root_identity_);
      if (biometric_created_) {
        ok = ok && biometric_bound_ &&
             descriptor_matches(biometric_fd_, biometric_identity_) &&
             named_directory_matches(root_fd_, "biometric", biometric_identity_);
      }
      if (ok) {
        if (biometric_created_) {
          ok = remove_leaf(biometric_fd_, "metadata") && ok;
          ok = remove_leaf(biometric_fd_, "macos-v1.meta") && ok;
          ok = (unlinkat(root_fd_, "biometric", AT_REMOVEDIR) == 0) && ok;
        }
        ok = remove_leaf(root_fd_, "synthetic-metadata") && ok;
        if (ok && named_directory_matches(parent_fd_, name_.c_str(), root_identity_)) {
          ok = unlinkat(parent_fd_, name_.c_str(), AT_REMOVEDIR) == 0;
        } else {
          ok = false;
        }
        result_.child_absent = sibling_absent(name_);
        ok = result_.child_absent && ok;
      }
    }
    bool closed = close_once(biometric_fd_);
    closed = close_once(root_fd_) && closed;
    closed = close_once(parent_fd_) && closed;
    result_.descriptors_closed = closed;
    result_.success = ok && closed;
    all_cleanup_ok_ = result_.success && all_cleanup_ok_;
    if (result_.child_created || creation_uncertain_) {
      std::fprintf(stderr, "Native fixture %s cleanup: %s\n", name_.c_str(),
                   result_.success ? "settled" : "HOLD");
    }
    return result_.success;
  }

  bool sibling_absent(const std::string &name) const noexcept {
    if (name.empty() || name.find('/') != std::string::npos ||
        name == "." || name == "..") {
      return false;
    }
    struct stat info{};
    return fstatat(parent_fd_, name.c_str(), &info, AT_SYMLINK_NOFOLLOW) != 0 &&
           errno == ENOENT;
  }

  const std::string &name() const noexcept { return name_; }
  const std::string &root() const noexcept { return root_; }
  const std::string &biometric() const noexcept { return biometric_; }
  const std::string &metadata() const noexcept { return metadata_; }
  const std::string &synthetic_target() const noexcept { return synthetic_target_; }

private:
  static bool capture_directory(int descriptor, struct stat *info) noexcept {
    return descriptor >= 0 && fstat(descriptor, info) == 0 &&
           S_ISDIR(info->st_mode) && info->st_uid == geteuid() &&
           (info->st_mode & 0077) == 0;
  }

  static bool same_directory(const struct stat &actual,
                             const struct stat &expected) noexcept {
    return S_ISDIR(actual.st_mode) && actual.st_uid == geteuid() &&
           (actual.st_mode & 0077) == 0 && actual.st_dev == expected.st_dev &&
           actual.st_ino == expected.st_ino;
  }

  static bool descriptor_matches(int descriptor,
                                 const struct stat &expected) noexcept {
    struct stat actual{};
    return descriptor >= 0 && fstat(descriptor, &actual) == 0 &&
           same_directory(actual, expected);
  }

  static bool named_directory_matches(int parent, const char *name,
                                      const struct stat &expected) noexcept {
    struct stat actual{};
    return fstatat(parent, name, &actual, AT_SYMLINK_NOFOLLOW) == 0 &&
           same_directory(actual, expected);
  }

  static bool remove_leaf(int directory, const char *name) noexcept {
    struct stat info{};
    if (fstatat(directory, name, &info, AT_SYMLINK_NOFOLLOW) != 0) {
      return errno == ENOENT;
    }
    if ((!S_ISREG(info.st_mode) && !S_ISLNK(info.st_mode)) ||
        info.st_uid != geteuid() || info.st_nlink != 1) {
      return false;
    }
    return unlinkat(directory, name, 0) == 0; // Removes a link, never its target.
  }

  static bool close_once(int &descriptor) noexcept {
    const int owned = descriptor;
    descriptor = -1;
    return owned < 0 || close(owned) == 0;
  }

  FixtureCleanup &result_;
  bool &all_cleanup_ok_;
  bool started_ = false;
  bool creation_uncertain_ = false;
  bool root_bound_ = false;
  bool biometric_created_ = false;
  bool biometric_bound_ = false;
  int parent_fd_ = -1;
  int root_fd_ = -1;
  int biometric_fd_ = -1;
  struct stat parent_identity_{};
  struct stat root_identity_{};
  struct stat biometric_identity_{};
  std::string name_;
  std::string root_;
  std::string biometric_;
  std::string metadata_;
  std::string synthetic_target_;
};

} // namespace

bool verify_bounded_busy_destroy(bool commit_before_destroy) {
  auto *context = new pv_bio_context();
  LAContext *authentication_context = [[LAContext alloc] init];
  constexpr uint64_t operation_id = 73;
  if (!begin_operation(context, operation_id, authentication_context)) {
    delete context;
    return false;
  }
  if (commit_before_destroy && !commit_operation(context, operation_id)) {
    finish_operation_and_release(context, operation_id);
    delete context;
    return false;
  }

  auto destroy = std::async(std::launch::async,
                            [context] { pv_bio_destroy(context); });
  const bool bounded =
      destroy.wait_for(kDestroyDrainTimeout + std::chrono::seconds(2)) ==
      std::future_status::ready;
  const bool cancelled = operation_was_cancelled(context, operation_id);
  finish_operation_and_release(context, operation_id);
  destroy.get();
  return bounded && cancelled;
}

int run_security_checks(bool &all_cleanup_ok) {
    FixtureCleanup cleanup;
    NativeFixture fixture(cleanup, all_cleanup_ok);
    PV_TEST_CHECK(fixture.create());
    const std::string &root = fixture.root();
    const std::string &biometric_directory = fixture.biometric();
    const std::string &metadata_path = fixture.metadata();
    PV_TEST_CHECK(is_owned_directory(root));
    PV_TEST_CHECK(ensure_owned_directory(biometric_directory));

    pv_bio_context *created_context = nullptr;
    const pv_bio_status created =
        pv_bio_create(root.data(), root.size(), &created_context);
    std::unique_ptr<pv_bio_context, decltype(&pv_bio_destroy)> context(
        created_context, &pv_bio_destroy);
    PV_TEST_CHECK(created == PV_BIO_OK);
    PV_TEST_CHECK(context != nullptr);
    std::array<uint8_t, PV_BIO_VAULT_HASH_BYTES> missing_hash{};
    std::array<uint8_t, PV_BIO_VAULT_KEY_BYTES> missing_output{};
    missing_output.fill(0xa5);
    PV_TEST_CHECK(pv_bio_retrieve(context.get(), 1, missing_hash.data(),
                                  missing_hash.size(), missing_output.data(),
                                  missing_output.size()) == PV_BIO_NOT_ENABLED);
    for (uint8_t byte : missing_output) {
      PV_TEST_CHECK(byte == 0);
    }
    constexpr char arabic_reason[] = "افتح PassVault";
    missing_output.fill(0xa5);
    PV_TEST_CHECK(pv_bio_retrieve_localized(
                      context.get(), 2, missing_hash.data(), missing_hash.size(),
                      missing_output.data(), missing_output.size(),
                      arabic_reason, sizeof(arabic_reason) - 1) ==
                  PV_BIO_NOT_ENABLED);
    for (uint8_t byte : missing_output) {
      PV_TEST_CHECK(byte == 0);
    }
    context.reset();
    secure_wipe(missing_hash.data(), missing_hash.size());
    secure_wipe(missing_output.data(), missing_output.size());

    PV_TEST_CHECK(verify_bounded_busy_destroy(false));
    PV_TEST_CHECK(verify_bounded_busy_destroy(true));

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
    // A direct-positive, valid owned target discriminates symlink rejection
    // from unrelated mode/size/format guards. Never use a system/user file.
    PV_TEST_CHECK(write_metadata_atomic(root, fixture.synthetic_target(), expected));
    PV_TEST_CHECK(read_metadata(fixture.synthetic_target(), &decoded) ==
                  MetadataReadResult::present);
    PV_TEST_CHECK(std::memcmp(&expected, &decoded, sizeof(expected)) == 0);
    PV_TEST_CHECK(symlink("../synthetic-metadata", metadata_path.c_str()) == 0);
    PV_TEST_CHECK(read_metadata(metadata_path, &decoded) ==
                  MetadataReadResult::invalid);
    PV_TEST_CHECK(read_metadata(fixture.synthetic_target(), &decoded) ==
                  MetadataReadResult::present);
    PV_TEST_CHECK(std::memcmp(&expected, &decoded, sizeof(expected)) == 0);
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

    NSString *arabic = localized_prompt_reason(arabic_reason,
                                                sizeof(arabic_reason) - 1);
    PV_TEST_CHECK([arabic isEqualToString:@"افتح PassVault"]);
    configure_biometric_context(configured_context, arabic);
    PV_TEST_CHECK([configured_context.localizedReason isEqualToString:arabic]);
    PV_TEST_CHECK([configured_context.localizedFallbackTitle isEqualToString:@""]);
    PV_TEST_CHECK(configured_context.touchIDAuthenticationAllowableReuseDuration == 0);
    PV_TEST_CHECK(localized_prompt_reason(nullptr, 1) == nil);
    PV_TEST_CHECK(localized_prompt_reason(arabic_reason, 0) == nil);
    constexpr char embedded_nul[] = {'a', 0, 'b'};
    PV_TEST_CHECK(localized_prompt_reason(embedded_nul, sizeof(embedded_nul)) == nil);
    const char invalid_utf8[] = {static_cast<char>(0xc0), static_cast<char>(0xaf)};
    PV_TEST_CHECK(localized_prompt_reason(invalid_utf8, sizeof(invalid_utf8)) == nil);
    const std::string maximum_reason(PV_BIO_MAX_PROMPT_REASON_BYTES, 'a');
    PV_TEST_CHECK(localized_prompt_reason(maximum_reason.data(), maximum_reason.size()) != nil);
    const std::string oversized_reason(PV_BIO_MAX_PROMPT_REASON_BYTES + 1, 'a');
    PV_TEST_CHECK(localized_prompt_reason(oversized_reason.data(), oversized_reason.size()) == nil);

    secure_wipe(&expected, sizeof(expected));
    secure_wipe(&decoded, sizeof(decoded));
    PV_TEST_CHECK(fixture.cleanup());
    PV_TEST_CHECK(cleanup.attempts == 1 && cleanup.child_absent &&
                  cleanup.descriptors_closed);
    return 0;
}

enum class FixtureCase { normal, early_return, cpp_exception };
struct SyntheticFixtureException {};

int run_fixture_probe(FixtureCase selected, const std::string &sibling_target,
                      FixtureCleanup &cleanup, bool &all_cleanup_ok,
                      std::string &created_name, bool &reached_failure_point) {
  NativeFixture fixture(cleanup, all_cleanup_ok);
  PV_TEST_CHECK(fixture.create());
  created_name = fixture.name();
  Metadata synthetic{};
  synthetic.vault_hash.fill(0x31);
  synthetic.nonce.fill(0x72);
  PV_TEST_CHECK(write_metadata_atomic(fixture.root(), fixture.synthetic_target(), synthetic));
  PV_TEST_CHECK(symlink(sibling_target.c_str(), fixture.metadata().c_str()) == 0);
  reached_failure_point = true; // Setup failure must not masquerade as injection.
  if (selected == FixtureCase::early_return) {
    PV_TEST_CHECK(false); // Intentionally exercise the real assertion-return path.
  }
  if (selected == FixtureCase::cpp_exception) {
    throw SyntheticFixtureException{};
  }
  PV_TEST_CHECK(fixture.cleanup()); // Destructor must not repeat this attempt.
  return 0;
}

int run_fixture_case(FixtureCase selected, bool &all_cleanup_ok) {
  FixtureCleanup sibling_cleanup;
  NativeFixture sibling(sibling_cleanup, all_cleanup_ok);
  PV_TEST_CHECK(sibling.create());
  Metadata sentinel{};
  sentinel.vault_hash.fill(0x93);
  sentinel.nonce.fill(0xb4);
  PV_TEST_CHECK(write_metadata_atomic(sibling.root(), sibling.synthetic_target(), sentinel));
  Metadata decoded{};
  PV_TEST_CHECK(read_metadata(sibling.synthetic_target(), &decoded) == MetadataReadResult::present);
  PV_TEST_CHECK(std::memcmp(&sentinel, &decoded, sizeof(sentinel)) == 0);
  struct stat before{};
  PV_TEST_CHECK(lstat(sibling.synthetic_target().c_str(), &before) == 0);

  FixtureCleanup subject_cleanup;
  std::string subject_name;
  bool reached_failure_point = false;
  bool caught_expected = false;
  int probe_result = -1;
  try {
    probe_result = run_fixture_probe(selected, sibling.synthetic_target(), subject_cleanup,
                                     all_cleanup_ok, subject_name, reached_failure_point);
  } catch (const SyntheticFixtureException &) {
    caught_expected = true;
  }
  PV_TEST_CHECK(reached_failure_point);
  PV_TEST_CHECK(caught_expected == (selected == FixtureCase::cpp_exception));
  if (!caught_expected) {
    PV_TEST_CHECK(probe_result == (selected == FixtureCase::early_return ? 1 : 0));
  }
  PV_TEST_CHECK(subject_cleanup.child_created && subject_cleanup.attempts == 1 &&
                subject_cleanup.success && subject_cleanup.child_absent &&
                subject_cleanup.descriptors_closed);
  // These are independent observations BEFORE the sibling owner's teardown.
  PV_TEST_CHECK(sibling.sibling_absent(subject_name));
  struct stat after{};
  PV_TEST_CHECK(lstat(sibling.synthetic_target().c_str(), &after) == 0);
  PV_TEST_CHECK(before.st_dev == after.st_dev && before.st_ino == after.st_ino &&
                before.st_mode == after.st_mode && before.st_size == after.st_size);
  PV_TEST_CHECK(read_metadata(sibling.synthetic_target(), &decoded) == MetadataReadResult::present);
  PV_TEST_CHECK(std::memcmp(&sentinel, &decoded, sizeof(sentinel)) == 0);
  PV_TEST_CHECK(sibling.cleanup());
  return 0;
}

int main(int argc, char **argv) {
  bool all_cleanup_ok = true;
  int result = 1;
  @autoreleasepool {
    try {
      if (argc == 1) {
        result = run_security_checks(all_cleanup_ok);
      } else if (argc == 3 && std::strcmp(argv[1], "--fixture-case") == 0) {
        if (std::strcmp(argv[2], "normal") == 0) {
          result = run_fixture_case(FixtureCase::normal, all_cleanup_ok);
        } else if (std::strcmp(argv[2], "early_return") == 0) {
          result = run_fixture_case(FixtureCase::early_return, all_cleanup_ok);
        } else if (std::strcmp(argv[2], "cpp_exception") == 0) {
          result = run_fixture_case(FixtureCase::cpp_exception, all_cleanup_ok);
        }
      }
    } catch (...) {
      std::fprintf(stderr, "Native test raised an unexpected C++ exception\n");
      result = 1;
    }
  }
  return all_cleanup_ok ? result : 1;
}
