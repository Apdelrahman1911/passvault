# Dependency Verification Policy

Last reviewed: 2026-09-03

## Enforced policy

PassVault uses Gradle's strict dependency verification with module-metadata verification enabled. The committed
`gradle/verification-metadata.xml` currently records 1,483 components and 2,946 artifacts, with exactly one lowercase
SHA-256 value for every recorded artifact. `./gradlew verifyDependencies` rejects missing hashes, alternate digests,
signature/key-server metadata, and changes to the three narrow trusted-artifact rules.

This is deliberately a **checksum-only integrity policy**. It detects changed cache, mirror, or repository bytes after
the reviewed checksum was committed. It does not prove who originally published those bytes. Generated checksums and
free-text `origin` attributes are not publisher provenance, and bootstrapping from a compromised repository could pin
a compromised artifact.

Of the 2,946 checksum entries, 2,867 currently retain Gradle's generated origin and 79 have human-readable source
annotations. Those annotations are audit notes only; they have no cryptographic effect.

PGP is not enabled because a downloaded signature and key-server result do not establish that a key belongs to the
claimed publisher. Gradle likewise warns that bootstrapping trusts current repository content, that not every
artifact is necessarily signed, and that signature verification does not establish whether the signer is legitimate.
Enabling signatures without independently authenticated publisher fingerprints would replace a visible limitation
with false assurance. SHA-256 fallback must remain unless the complete resolved graph is proven signed.

The build gate intentionally requires `verify-signatures=false`. This does not prevent a reviewed migration: the
migration must change the metadata and the gate together. The gate must never be weakened to accept either mode.

## Updating dependencies

1. Keep versions exact and repositories limited to the declarations in `settings.gradle.kts`.
2. Generate additions with `--write-verification-metadata sha256` in a fresh, temporary `GRADLE_USER_HOME`, exercising
   the same platform/configuration matrix as CI. Do not delete or repurpose the normal global Gradle cache.
3. Review every coordinate and checksum diff. Compare critical direct artifacts with a publisher-controlled release
   source when one exists; record any remaining first-use assumption in the pull request.
4. Never copy a reported checksum merely to make a failed build pass. Investigate unexpected bytes before updating
   the metadata.
5. Run `./gradlew verifyDependencies`, the affected builds/tests, then test at least one clean dependency cache before
   merging. Remove the temporary cache after evidence is collected.

## Requirements for a signature migration

A future hybrid SHA-256/PGP policy requires all of the following in one reviewed change:

- full fingerprints authenticated through publisher-controlled channels, not inferred from `.asc` files or key
  servers;
- keys scoped to the smallest applicable group/module, with no unscoped wildcard trust;
- documented rotation, expiry, and revocation handling plus a reproducible committed keyring;
- SHA-256 retained for integrity and an explicit checksum-only inventory for unsigned artifacts;
- clean-cache verification of every CI/release configuration; and
- a gate changed to require the new mode, trusted-key scopes, and a non-regressing signed-coverage floor.

Until that evidence exists, checksum-only verification is an explicit residual supply-chain limitation, not a claim
of publisher authenticity.
