# Release Automation

PassVault promotes one tested mobile build and one tested Git commit through
testing and production. Mobile binaries are never rebuilt after the internal
upload. Desktop testing packages are public and intentionally unsigned;
production Windows and macOS packages cannot publish unless signing and
notarization succeed.

## Branch and approval flow

```text
main
  └─ reviewed PR → testing
       ├─ exact Android/iOS build → internal testing
       ├─ mobile-external-beta approval → closed testing / external TestFlight
       ├─ unsigned Windows/macOS/Linux GitHub prerelease
       └─ Candidate Readiness after Apple Beta Review approval
            └─ fast-forward exact SHA → release
                 └─ mobile-production approval → validation only
                      ├─ signed/timestamped Windows bundle
                      ├─ signed/notarized/stapled macOS bundle
                      └─ frozen attested desktop artifact (no publication)
                           └─ protected production promotion
                                ├─ exact Play build → production (100%)
                                └─ exact TestFlight build → App Review (automatic release)
```

CI runs on `main`, `testing`, and `release`. `main` and `testing` require a PR,
CI, linear history, resolved conversations, and one approval. `release` rejects
force pushes and deletion; the documented automation advances it only through
Candidate Readiness.

GitHub's protected rebase merge can give the reviewed `main` tree a new commit
SHA when it enters `testing`. Candidate preparation therefore accepts either
the current `main` commit as an ancestor or a commit with shared repository
history and the exact same Git tree. A version match, commit message, partial
diff, or unrelated matching tree is never sufficient. Every artifact and
receipt is then bound to the resulting exact `testing` commit and tree.

Run `scripts/configure-release-branches.sh --apply` once to create and protect
the branches. Run `scripts/configure-github-mobile-release.sh --apply` when rotating
mobile credentials; it scopes beta environments to `testing`, production to
`release`, and keyless Play access-check environments to `main`. The production
access check requires the deployment approver because it federates the same
publisher identity as a production promotion. The access-check environments
expose only the keyless Google identity variables;
mobile signing and store-upload secrets remain confined to their release
environments, and stale repository-level copies of those mobile secrets are
removed and verified absent. Google federation is bound to both the repository name and its
immutable GitHub repository ID. The script also records the Android/iOS signing
fingerprints used by candidate provenance.

## Candidate release

Choose the marketing version and exact unused Store build number in
`version.properties` on `main`, then open and merge a reviewed `main` to
`testing` PR. During the Mobile Release Kit pilot, run either the shared caller
or manually dispatch the retained legacy `Testing Candidate`; do not run both
for the same build. Automatic legacy uploads on a `testing` push remain disabled
unless repository variable `LEGACY_TESTING_RELEASE_ON_PUSH` is explicitly set to
`true`. The legacy path:

1. Reads the exact committed `VERSION_CODE`, validates the supported range, and
   rejects a Store collision before upload.
2. Signs and uploads Android and iOS once to internal testing.
3. Attests SHA-256 receipts for the exact signed AAB/APK, IPA/archive, mapping,
   and link-map files; binaries remain private Actions artifacts for 90 days.
4. Waits for the `mobile-external-beta` environment approval.
5. Promotes the exact Android build to the Play `alpha` closed-testing track
   and distributes the exact iOS build to the configured external TestFlight
   group.
6. Builds unsigned Windows x64 EXE/MSI, macOS arm64/x64 DMGs, and Linux x64
   DEB/RPM packages.
7. Publishes `vVERSION-rc.BUILD` as a GitHub prerelease with checksums,
   `candidate-manifest.json`, safe mobile hash receipts, the project license,
   and third-party notices.

The prerelease notes explicitly warn that Windows and macOS test installers
are unsigned. Mobile IPA/AAB files are never attached to a public release.

Apple review is asynchronous and App Store Connect has no GitHub event hook.
After Apple emails that Beta App Review is approved, manually run `Candidate
Readiness` from the `testing` branch for the candidate tag. That workflow
verifies both stores using their APIs, writes `readiness-manifest.json`,
fast-forwards `release` to the exact candidate commit, and starts `Production
Signing Validation`. This is the only unavoidable post-review manual start.

## Production release

Approve the pending `mobile-production` deployment in GitHub. The first
protected run is validation-only: it builds the exact release commit,
Authenticode signs/timestamps Windows native code and installers, signs every
macOS nested native component with Developer ID and Hardened Runtime, requires
`Accepted` notarization, staples/verifies the ticket, checks Gatekeeper, and
freezes an attested Actions artifact. It does not publish a GitHub release or
change either store. A manually started validation defaults to stopping here.

When Candidate Readiness requested automatic continuation, successful signing
validation starts the protected `Production Store Release`. That workflow first
requires the matching unexpired validation artifact, then promotes the same Play
build from `alpha` to `production` with a completed 100% rollout and submits the
same TestFlight build to App Review with automatic release enabled. Reruns target
the same unique version/build and never compile a new mobile binary.

Stores cannot become live atomically: Google may publish before Apple finishes
review. After both consoles show the version live, run `Publish Stable Release`
from `release` and select the same candidate tag. The workflow independently
requires that exact Android build on Play production and that exact iOS
version/build publicly downloadable. It then locates and validates the exact
frozen signing bundle instead of rebuilding:

- Windows x64 EXE and MSI, with mandatory Authenticode signing.
- macOS arm64 and x64 DMGs, with mandatory Developer ID signing and notarization.
- Linux x64 DEB and RPM, covered by the release SHA-256 manifest.
- `LICENSE.txt`, `NOTICE.txt`, and `THIRD_PARTY_NOTICES.md`, copied from the
  tested source tree and covered by the same checksum manifest.

It then creates immutable `vVERSION` release provenance from the exact tested
commit. Missing/partial credentials, an expired validation artifact, a different
SHA, or a failed signature/notarization gate fail closed; production never falls
back to rebuilding or unsigned Windows/macOS output.

## Automated store screenshots

`Generate Store Screenshots` builds the isolated Android `storeScreenshot`
variant, captures actual app onboarding screens in `en-US` and `ar`, builds the
iOS simulator app, captures required 6.9-inch iPhone and 12.9/13-inch iPad
welcome screens in `en-US` and `ar-SA`, validates dimensions, and uploads a
14-day `validated-store-assets` artifact.
Download and inspect that artifact before manually installing approved images
in the tracked store-assets directories; the workflow does not create a branch
or PR and has read-only repository permissions.
Screenshot mode disables Android screen capture protection only in that dedicated
application ID. Static tests verify that normal builds keep
`STORE_SCREENSHOT_MODE=false`.

Review generated images before merging. They must contain only fictional data
and must be checked for localization, cropping, status-bar content, and store
quality. Production is blocked until `validate-mobile-store-assets.rb` passes.

To replace iOS screenshots after a version has been submitted, run `Mobile
Store Release` from `release` with platform `ios`, channel `production`, and
operation `store-assets`. Enter `I_APPROVE_PRODUCTION` and
`I_APPROVE_REVIEW_WITHDRAWAL` in their confirmation fields. The protected job
withdraws the version when Apple permits it, replaces the screenshots, uploads
no binary or metadata, and leaves the version unsubmitted for review.

## Required GitHub configuration

Repository variables used by candidate provenance:

- `ANDROID_UPLOAD_CERTIFICATE_SHA256`
- `IOS_DISTRIBUTION_CERTIFICATE_SHA1`

They are derived and uploaded by `configure-github-mobile-release.sh`.

Mobile signing material is restricted to `mobile-beta`, the only environment
that builds and uploads new Android/iOS binaries. The external and production
environments retain only App Store Connect access, prepared metadata, and the
review/tester values needed to promote the already-tested build; they do not
receive the Android keystore or Apple distribution certificate/profile.

Production desktop variables:

- `PUBLISHER_NAME`, `COPYRIGHT_HOLDER`, `SUPPORT_EMAIL`, `SECURITY_EMAIL`
- `PRIVACY_POLICY_URL`, `SUPPORT_URL`, `PROJECT_URL`
- `WINDOWS_SIGNING_BACKEND`, `WINDOWS_EXPECTED_PUBLISHER_NAME`
- `WINDOWS_TIMESTAMP_URL` for Azure Artifact Signing or local-PFX only
- `WINDOWS_SIGNING_CERTIFICATE_SHA256` for local-PFX only
- `MACOS_SIGNING_IDENTITY` (`Developer ID Application: ...`)
- `MACOS_NOTARIZATION_TEAM_ID`
- `MACOS_DEVELOPER_ID_CERTIFICATE_SHA256`

Production desktop secrets in the release-only `mobile-production` environment:

- `WINDOWS_SIGNPATH_API_TOKEN` for SignPath; or
- `WINDOWS_CERTIFICATE_BASE64`, `WINDOWS_CERTIFICATE_PASSWORD` for local-PFX
- `MACOS_CERTIFICATE_BASE64`, `MACOS_CERTIFICATE_PASSWORD`, `MACOS_PROVISIONING_PROFILE_BASE64`
- `MACOS_NOTARIZATION_APPLE_ID`, `MACOS_NOTARIZATION_PASSWORD`

SignPath production environment variables are
`WINDOWS_SIGNPATH_ORGANIZATION_ID`, `WINDOWS_SIGNPATH_PROJECT_SLUG`,
`WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG`, and
`WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG`. Azure production environment
variables are `WINDOWS_AZURE_CLIENT_ID`, `WINDOWS_AZURE_TENANT_ID`,
`WINDOWS_AZURE_SUBSCRIPTION_ID`, `WINDOWS_ARTIFACT_SIGNING_ENDPOINT`,
`WINDOWS_ARTIFACT_SIGNING_ACCOUNT`, and `WINDOWS_ARTIFACT_SIGNING_PROFILE`.
Only the selected backend's inputs may exist; the configuration script removes
and verifies the absence of inactive and broader-scope copies.

The Windows and macOS signing jobs declare `mobile-production`, so GitHub
releases their inputs only for the protected `release` branch after production
deployment approval. Self-review is disabled. SignPath Foundation may require
one additional manual approval inside SignPath; the workflow waits for the
decision and fails closed on rejection or timeout.

See `docs/RELEASE_SIGNING.md` for certificate requirements and rotation. Use
`docs/PRODUCTION_SIGNING_HANDOFF.md` as the single field-by-field private input
and GitHub-scope handoff; fingerprints and the Developer ID identity are derived
from validated certificates rather than typed manually.

## One-time manual console work

The repository cannot truthfully automate these owner/legal decisions:

- Apple: accept agreements, complete App Privacy, age rating, category,
  pricing/availability, review contact, export-compliance evidence, and create
  a Developer ID Application certificate.
- Google Play: complete App Content/Data Safety, default language, contact
  details, pricing/countries, confirm Play App Signing, satisfy production
  eligibility, retain the real closed-test email list for the legacy path, and
  attach at least one owned Google Group to `alpha` so the shared path can prove
  tester assignment through the Publisher API.
- Signing vendors: obtain an approved HSM-backed Windows Authenticode identity
  (SignPath is the documented starting point; Azure is available only when the
  publisher is eligible), and an Apple Developer ID certificate. Use local PFX
  only when the issuing CA explicitly authorizes that key custody and CI use.
  Keep all private material out of Git.
- Testers: add at least one eligible App Store Connect internal tester and real
  external tester identities/email lists.
- Assets: download and review the generated `validated-store-assets` artifact,
  then install only approved images in the tracked store-assets directories.

Everything after these inputs—building, signing, notarizing, uploading,
promoting exact builds, provenance validation, checksums, and GitHub Release
creation—is performed by GitHub Actions.

Desktop biometric support does not add another private credential. macOS uses the existing Developer ID identity to
sign the Touch ID bridge as nested Hardened Runtime code. Windows signs the repository-built Windows Hello bridge
with the same selected PassVault Authenticode backend as the launcher, refreshes its strict runtime checksum only
after signature verification, and packages installers from that unchanged signed image. See
[`DESKTOP_BIOMETRIC_UNLOCK.md`](DESKTOP_BIOMETRIC_UNLOCK.md).
