# Standalone iOS signing verifier: temporary profile ownership

The standalone verifier must not leave a newly installed provisioning profile
behind merely because a later permission operation fails or a handled exit occurs
before signing starts. It must also preserve profiles that it did not install.

The verifier stages an input copy under its private temporary verification root
and applies mode 0600 there. It records cleanup responsibility before publishing
the staged inode with an exclusive hard link. Publication never replaces an
existing destination. EXIT cleanup removes the installed path only while it is a
non-symlink naming that exact staged inode. A preexisting equal profile is used
without being claimed for cleanup; a different, symlink, or nonregular destination
stops verification. A replaced profile belongs to the replacing operation and is
not removed.

The temporary verification directory and profile directory must be on the same
filesystem. If an explicitly configured TMPDIR is on another filesystem, exclusive
hard-link publication fails closed. Select a private local TMPDIR on the same
filesystem before running the verifier; there is no overwriting-copy fallback.
This requirement changes the previous standalone verifier's cross-filesystem
behavior; it does not alter Store identities, signing checks, manual approvals,
the release candidate, or mobile/desktop publication policy.

This is bounded cleanup under cooperative ownership. It is not crash recovery:
SIGKILL, process termination before traps are installed, power loss, a failed
filesystem operation, or a hostile writer can prevent cleanup. Cleanup failure
remains a failed verification with a diagnostic. Deleting a profile is not
revocation of a certificate or erasure of copies already held by another process.
The normal platform signing/provisioning and Keychain controls remain separate.

## Non-publishing regression checks

Run "ruby scripts/test-ios-profile-cleanup.rb" for the focused synthetic checks.
The same cases are included in "ruby scripts/test-release-regressions.rb".
They execute the actual installation and EXIT blocks against inert text files in
an isolated HOME/TMPDIR. They do not read release/private, invoke real security or
Xcode tools, import certificates, sign, archive, upload, or launch an application.

The checks cover:
- successful install and a later failure;
- copy failure before/after bytes and chmod failure;
- handled-exit control flow before/after publication and during preparation;
- equal/different/nonregular/symlink preexisting paths;
- exclusive-publication conflict/failure;
- inode and symlink replacement before cleanup;
- explicit profile/root cleanup failures.

Handled exits are injected as exit statuses, not delivered OS signals. A simulated
publication error is not evidence from an actual second filesystem. These fixtures
do not establish Xcode hard-link compatibility or physical-device behavior. Normal
signing/archive validation, when separately authorized, remains required.
