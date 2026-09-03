# Release Automation

PassVault promotes one tested mobile build and receipt-bound Desktop payloads
through testing and production. Mobile binaries are never rebuilt after the
internal upload. Linux packages are promoted byte-for-byte; Windows and macOS
promote the tested app images into platform signing and packaging. Desktop
signing and publication remain a separate, optional lifecycle, so missing
Desktop credentials never block Android or iOS production.

## Branch and approval flow

```text
main
  └─ reviewed PR → testing
       ├─ exact Android/iOS build → internal testing
       ├─ mobile-external-beta approval → closed testing / external TestFlight
       ├─ smoke-tested Desktop packages and frozen app images
       └─ Candidate Readiness after Apple Beta Review approval
            └─ release-promotion approval → fast-forward exact SHA → release

main
  └─ Request Mobile Production Release (owner + typed confirmation)
       └─ protected bot handoff
            └─ mobile-production approval
                 ├─ exact Play build → production (100%)
                 └─ exact TestFlight build → App Review (automatic release)

release (optional, later)
  └─ Production Signing Validation
       └─ desktop-production approval → frozen signed Desktop bundle
            └─ Publish Stable Release → desktop-production approval
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
mobile credentials or release protections; it scopes beta environments to
`testing`, the secret-free `release-promotion` approval to `testing`, production
mobile promotion to `main`, Desktop production to `release`, and keyless Play
access-check environments to `main`. To apply only these three release
environment policies while Desktop credentials are intentionally deferred, run
`scripts/configure-github-release-environments.sh --apply` instead. Release
promotion requires an explicit approval by the configured reviewer and permits
that reviewer to be the dispatching actor so a single-owner repository can
operate the gate. `mobile-production` still prevents approval by its initiating
actor: the protected request workflow dispatches the child as
`github-actions[bot]`, so the configured owner can approve without a second
human account. Administrator bypass remains disabled.
The production access check requires the deployment approver because it
federates the same publisher identity as a production promotion. The
access-check environments expose only the keyless Google identity variables;
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
6. Builds and smoke-tests unsigned Windows x64 EXE/MSI, macOS arm64/x64 DMGs,
   and Linux x64 DEB/RPM packages. It archives and restores each Windows/macOS
   app image before the smoke test so production consumes the tested archive.
7. Creates an attested `desktop-artifact-receipt.json` with the exact Linux
   package and Windows/macOS app-image hashes, source tree, and workflow run.
   Its digest is bound into schema-3 `candidate-manifest.json`.
8. Publishes `vVERSION-rc.BUILD` with the receipts, promotion inputs, test
   installers, checksums, licenses, and third-party notices.

If internal Android or iOS upload already succeeded for the exact
candidate tree, version, and `VERSION_CODE`, dispatch `Testing
Candidate` from `testing` with `resume_existing_internal_uploads=true`
and confirmation `resume:<version>:<build>`. The live `testing` tree
must still be that original candidate tree. Resume may run from a
later `testing` SHA only to load the workflow; it still binds
`vVERSION-rc.BUILD`, the candidate manifest, and later readiness to
the original receipt commit and tree. It reuses only attested signed
Android/iOS artifacts and their matching receipts from successful
`mobile-internal` jobs, still promotes those exact store builds, and
still creates the candidate tag inside `publish-candidate`. It refuses
another tree, another build, an unattested binary, a receipt whose
hashes do not match the signed files, or two successful receipts whose
artifact hashes differ. It never uploads Android or iOS again.

The prerelease notes explicitly warn that Windows and macOS test installers
and app-image archives are unsigned. The app-image archives are production
signing inputs, not end-user installers. Mobile IPA/AAB files are never attached
to a public release.

Apple review is asynchronous and App Store Connect has no GitHub event hook.
After Apple emails that Beta App Review is approved, manually run `Candidate
Readiness` from the `testing` branch for the candidate tag. That workflow
verifies both stores using their APIs and then waits at the secret-free
`release-promotion` environment. The configured reviewer explicitly authorizes
the immutable `readiness-manifest.json`, fast-forward of `release` to the exact
candidate commit. The dispatcher may approve this gate; the manual approval
itself remains required. Candidate Readiness does not start Desktop signing or
mobile production.

## Production release

Run `Request Mobile Production Release` from protected `main`, enter the exact
candidate tag, choose the platform(s), and type
`I_APPROVE_MOBILE_PRODUCTION`. The request must be made by
`DEPLOYMENT_APPROVER`. It dispatches `Production Store Release` as
`github-actions[bot]`; approve that child's pending `mobile-production`
deployment. The child verifies the successful request run, exact `release`
commit, immutable candidate tag, attested readiness manifest, mobile receipts,
version, build number, and candidate Store assets. It then promotes the
candidate's recorded build from Play `alpha` to production and submits the same
processed TestFlight build to App Review. It does not compile, sign, or upload
a mobile binary.

Desktop is optional. When a Desktop release is wanted later, manually run
`Production Signing Validation` from `release` for the same candidate and
approve `desktop-production`. Before any signing, it downloads the candidate
receipt and promotion inputs, verifies their attestations and hashes, and stages
them as short-lived workflow artifacts. It promotes Linux packages unchanged,
signs the restored Windows app image before creating EXE/MSI installers, and
signs the restored macOS app image before creating and notarizing each DMG. The
production workflow does not invoke Gradle or rebuild application payloads. It
then freezes the attested Desktop bundle without changing either mobile Store.

Stores cannot become live atomically: Google may publish before Apple finishes
review. After both consoles show the version live, run `Publish Stable Release`
from `release` and select the same candidate tag. The workflow independently
requires that exact Android build on Play production and that exact iOS
version/build publicly downloadable. It then locates and validates the exact
frozen signing bundle instead of rebuilding:

- Windows x64 EXE and MSI, packaged from the receipt-verified candidate app
  image with mandatory Authenticode signing.
- macOS arm64 and x64 DMGs, packaged from receipt-verified candidate app images
  with mandatory Developer ID signing and notarization.
- The byte-identical candidate Linux x64 DEB and RPM, covered by the release
  SHA-256 manifest.
- `LICENSE.txt`, `NOTICE.txt`, and `THIRD_PARTY_NOTICES.md`, copied from the
  tested source tree and covered by the same checksum manifest.

Schema-2 `release-provenance.json` records the Desktop receipt digest, candidate
workflow run, and every promotion-input digest alongside the final asset hashes.
Missing/partial credentials, an expired validation artifact, a different SHA,
or a failed signature/notarization gate fail closed; production never falls back
to rebuilding or unsigned Windows/macOS output.

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
Store Release` from `main` with platform `ios`, channel `production`, and
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

Production desktop secrets in the release-only `desktop-production` environment:

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

The Windows and macOS signing jobs declare `desktop-production`, so GitHub
releases their inputs only for the protected `release` branch after production
deployment approval. Self-review is prevented. SignPath Foundation may require
one additional manual approval inside SignPath; the workflow waits for the
decision and fails closed on rejection or timeout.

The `release-promotion` environment contains no secrets or variables. It allows
only `testing` and requires the configured reviewer. Self-review is permitted
there so the repository owner can explicitly approve a run they dispatched.
`mobile-production` allows only `main` and retains self-review prevention
because its normal caller is the bot handoff. `desktop-production` allows only
`release` and retains its independent reviewer policy for any future Desktop
release.
GitHub enables administrator bypass by default and its public environment API
must explicitly receive the non-bypassable setting. The configuration script
disables administrator bypass for every managed environment and verifies
`release-promotion`, `mobile-production`, and `desktop-production`; the
promotion job independently fails closed if `release-promotion` is ever made
bypassable.

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
