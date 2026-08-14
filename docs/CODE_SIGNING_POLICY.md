# PassVault Code-Signing Policy

PassVault signs public production artifacts only from the repository's protected release automation. A signature
means that the artifact was produced from the exact readiness-approved source commit and passed the repository's
production validation gates; it does not imply an independent security audit.

## Signed artifacts

- Windows: the PassVault launcher, repository-built Windows Hello bridge, and final EXE/MSI installers receive a
  public-trust Authenticode signature and a trusted timestamp. The signed bridge checksum is rebound into the exact
  app image before installer creation. Third-party runtime files retain their valid vendor signatures and are not
  re-signed as PassVault.
- macOS: the application and every nested native component receive the expected Developer ID signature, Hardened
  Runtime, and secure timestamp. Final DMGs must receive an `Accepted` Apple notarization result and a verified staple.
- Android and iOS use their platform-store signing and distribution identities. Linux packages are not code-signed;
  their hashes are covered by the signed GitHub workflow attestation and release checksum manifest.

Test-candidate Windows/macOS packages are intentionally unsigned and are labelled as such. They are never promoted
as production assets.

## Authorization and separation of duties

- Committers prepare changes through reviewed pull requests. Protected `main`, `testing`, and `release` branches and
  required CI checks constrain the source that can enter a release.
- Release automation may advance only the exact candidate commit whose store build receipts and readiness manifest
  validate. The `mobile-production` GitHub environment requires a human reviewer and prevents the workflow initiator
  from approving their own deployment.
- The Windows signing provider token is a submitter credential only. Signing-policy approvers and provider
  administrators must use separate named accounts with MFA. A provider-mandated approval cannot be bypassed by CI.
- GitHub repository/environment role membership and signing-provider role membership are reviewed whenever access
  changes and before each production release. Access is removed when no longer required.

The repository owner is [`@Apdelrahman1911`](https://github.com/Apdelrahman1911). The independent deployment reviewer
and any signing-provider approvers are publisher-selected named people; their current identities must be recorded in
the provider's access-control page and the private release handoff before production signing. The repository does not
publish private contact details or invent identities that have not been supplied.

## Key custody

- No private signing key is committed to Git. HSM-backed Windows signing is preferred. The GitHub secret contains only
  a least-privilege request token; the provider retains the key.
- Apple/Android PKCS#12 or keystore material, when required by their platform tooling, exists only in protected
  environment secrets and encrypted offline backups. CI writes it only to an ephemeral runner location and cleans up
  on success or failure.
- Certificate rotation changes the expected public identity or fingerprint only after explicit publisher review.
  Compromise immediately blocks production environments, revokes the affected credential with its issuer, and
  requires a documented rotation before another release.

## Reproducibility, verification, and publication

Production signing runs in validation-only mode first. It binds candidate tag, version, build number, commit SHA, Git
tree, mobile receipts, and desktop artifact hashes. Windows signatures are independently checked with `signtool`;
macOS signatures, Hardened Runtime, notarization ticket, and Gatekeeper state are independently checked with Apple
tools. The validated bundle is frozen and attested. Stable publication downloads that exact bundle and never rebuilds
it.

Signing requests and validation logs are retained only as long as needed for audit and failure diagnosis. Transient
unsigned SignPath request artifacts are deleted immediately after the result is applied. Production release artifacts,
checksums, public certificates, and provenance are retained with the release.

Security reports must follow [SECURITY.md](../SECURITY.md). Release mechanics and the credential handoff are in
[RELEASE_AUTOMATION.md](RELEASE_AUTOMATION.md) and
[PRODUCTION_SIGNING_HANDOFF.md](PRODUCTION_SIGNING_HANDOFF.md).
