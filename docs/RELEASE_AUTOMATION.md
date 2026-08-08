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
                 └─ mobile-production approval
                      ├─ exact Play build → production (100%)
                      └─ exact TestFlight build → App Review (automatic release)
```

CI runs on `main`, `testing`, and `release`. `main` and `testing` require a PR,
CI, linear history, resolved conversations, and one approval. `release` rejects
force pushes and deletion and is advanced only by Candidate Readiness.

Run `scripts/configure-release-branches.sh --apply` once to create and protect
the branches. Run `scripts/configure-github-mobile-release.sh` when rotating
mobile credentials; it scopes beta environments to `testing`, production to
`release`, and records the Android/iOS signing fingerprints used by candidate
provenance.

## Candidate release

Choose the marketing version in `version.properties` on `main`, then open and
merge a reviewed `main` to `testing` PR. `Testing Candidate` automatically:

1. Allocates `1,000,000 + run_number × 10 + run_attempt` as the store build.
2. Signs and uploads Android and iOS once to internal testing.
3. Waits for the `mobile-external-beta` environment approval.
4. Promotes the exact Android build to the Play `beta` track and distributes
   the exact iOS build to the configured external TestFlight group.
5. Builds unsigned Windows x64 EXE/MSI, macOS arm64/x64 DMGs, and Linux x64
   DEB/RPM packages.
6. Publishes `vVERSION-rc.BUILD` as a GitHub prerelease with checksums and
   `candidate-manifest.json`.

The prerelease notes explicitly warn that Windows and macOS test installers
are unsigned. Mobile IPA/AAB files are never attached to a public release.

Apple review is asynchronous and App Store Connect has no GitHub event hook.
After Apple emails that Beta App Review is approved, manually run `Candidate
Readiness` for the candidate tag. That workflow verifies both stores using
their APIs, writes `readiness-manifest.json`, fast-forwards `release` to the
exact candidate commit, and starts `Production Store Release` automatically.
This is the only unavoidable post-review manual start.

## Production release

Approve the pending `mobile-production` deployment in GitHub. It promotes the
same Play build from `beta` to `production` with a completed 100% rollout and
submits the same TestFlight build to App Review with automatic release enabled.
Reruns are designed to target the same version/build and never compile a new
mobile binary.

Stores cannot become live atomically: Google normally publishes before Apple
finishes review. After both consoles show the version live, run `Publish Stable
Release` from `release`, enter `I_CONFIRM_BOTH_STORES_LIVE`, and select the same
candidate tag. The workflow builds:

- Windows x64 EXE and MSI, with mandatory Authenticode signing.
- macOS arm64 and x64 DMGs, with mandatory Developer ID signing and notarization.
- Linux x64 DEB and RPM, covered by the release SHA-256 manifest.

It then creates immutable `vVERSION` release provenance from the exact tested
commit. Missing or partial desktop credentials fail; production never falls
back to unsigned Windows or macOS output.

## Automated store screenshots

`Generate Store Screenshots` builds the isolated Android `storeScreenshot`
variant, captures actual app onboarding screens in `en-US` and `ar`, builds the
iOS simulator app, captures the 6.9-inch welcome screen in `en-US` and `ar-SA`,
validates dimensions, and opens a review PR. Screenshot mode disables Android
screen capture protection only in that dedicated application ID. Static tests
verify that normal builds keep `STORE_SCREENSHOT_MODE=false`.

Review generated images before merging. They must contain only fictional data
and must be checked for localization, cropping, status-bar content, and store
quality. Production is blocked until `validate-mobile-store-assets.rb` passes.

## Required GitHub configuration

Repository variables used by candidate provenance:

- `ANDROID_UPLOAD_CERTIFICATE_SHA256`
- `IOS_DISTRIBUTION_CERTIFICATE_SHA1`

They are derived and uploaded by `configure-github-mobile-release.sh`.

Production desktop variables:

- `PUBLISHER_NAME`, `COPYRIGHT_HOLDER`, `SUPPORT_EMAIL`, `SECURITY_EMAIL`
- `PRIVACY_POLICY_URL`, `SUPPORT_URL`, `PROJECT_URL`
- `WINDOWS_TIMESTAMP_URL`
- `MACOS_SIGNING_IDENTITY` (`Developer ID Application: ...`)
- `MACOS_NOTARIZATION_TEAM_ID`

Production desktop secrets:

- `WINDOWS_CERTIFICATE_BASE64`, `WINDOWS_CERTIFICATE_PASSWORD`
- `MACOS_CERTIFICATE_BASE64`, `MACOS_CERTIFICATE_PASSWORD`
- `MACOS_NOTARIZATION_APPLE_ID`, `MACOS_NOTARIZATION_PASSWORD`

See `docs/RELEASE_SIGNING.md` for certificate requirements and rotation.

## One-time manual console work

The repository cannot truthfully automate these owner/legal decisions:

- Apple: accept agreements, complete App Privacy, age rating, category,
  pricing/availability, review contact, export-compliance evidence, and create
  a Developer ID Application certificate.
- Google Play: complete App Content/Data Safety, default language, contact
  details, pricing/countries, confirm Play App Signing, satisfy production
  eligibility, and attach the real closed-test email list to `internal` and
  `beta`.
- Signing vendors: obtain a trusted Windows Authenticode PFX and Apple
  Developer ID certificate. Keep all private material out of Git.
- Testers: add at least one eligible App Store Connect internal tester and real
  external tester identities/email lists.
- Assets: review and merge the generated screenshot PR.

Everything after these inputs—building, signing, notarizing, uploading,
promoting exact builds, provenance validation, checksums, and GitHub Release
creation—is performed by GitHub Actions.
