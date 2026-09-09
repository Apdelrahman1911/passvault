# macOS native test fixture prerequisite

**These tests require an explicitly admitted private parent. There is no
automatic `/tmp`, `TMPDIR` or `HOME` fallback.**

The caller must supply `PASSVAULT_NATIVE_TEST_PARENT` as an existing absolute
directory path of at most 2048 bytes, without a trailing slash, `/.` or `/..`.
The fixture opens the final directory entry without following a symlink and
checks that it is owned by the effective UID with no group/other mode bits.
The caller is responsible for establishing the private parent's identity,
ancestor/ACL safety, isolated environment, process/resource coordination and
cleanup admission **before** invoking the tests. Setting the variable alone
does not provide that admission. The fixture neither adopts a colliding child
nor creates/removes the supplied parent.

## Caller compatibility

`testDesktopBiometricBridge` invokes CTest, and macOS Desktop JVM `Test` tasks
depend on it. Gradle currently does **not** create or supply this private parent.
Those callers now fail closed unless their environment supplies the admitted
parent; do not silently substitute an ambient temporary directory. This is an
intentional test-infrastructure prerequisite, not an application storage,
biometric-provider, ABI or product-policy change. Windows test branches are
unchanged. This document is not an approved build/test invocation recipe.

## Cases and observations

The original `passvault_biometric_macos_security` case now uses one guarded
synthetic child and a valid synthetic symlink target. Direct positive metadata
reads before and after rejection strengthen the negative oracle. The target is
not `biometric/macos-v1.meta`, so the existing missing-metadata retrieval checks
remain before Keychain/provider access. No real vault or system-file target is
used. The existing bounded-busy-destroy function is unchanged.

Three additional CTest cases select `normal`, `early_return` and `cpp_exception`
fixture paths in the same executable. They check one cleanup attempt, child
absence and closed-descriptor results, then independently observe subject-child
absence and unchanged synthetic sibling metadata **before** the sibling owner's
teardown. The intentional early-return check prints a failure line inside its
probe; the enclosing case only succeeds if the intended injection point and
cleanup observations are reached. Declaration is not execution evidence.

Cleanup is nonrecursive and limited to the exact fixture leaf names and its
exclusively created child directories. A symlink is unlinked, never followed.
Unknown files, hard-linked leaves, changed identities, uncertain acquisition,
failed unlink/rmdir or failed close cause a nonzero result and, after child
creation, a `HOLD` diagnostic. No destructor retry follows an explicit attempt.
In particular, unknown writer `.tmp.*` residue is not wildcard-swept. Preserve
HOLD evidence for separately admitted closeout; do not broaden deletion.

This is a cooperative-host fixture, not a hostile-same-user sandbox: ancestor
or ACL substitution, name-check/unlink races, synchronous syscall stalls,
fatal signals, forced process death, Objective-C abnormal exceptions and the
existing async native lifetime failure schedules are not proven contained.
A runner still needs independent execution, interruption and cleanup review.
Neither these fixtures nor a hosted Mac run proves physical Touch ID/iPhone
security behavior. No macOS execution is admitted by this source correction.
