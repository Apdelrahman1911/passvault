# PassVault Mobile Store Release

The canonical branch, approval, promotion, and desktop-publication flow is in
`docs/RELEASE_AUTOMATION.md`. This document covers private mobile setup and the
store-console work that cannot be performed truthfully by CI.

The release pipeline uploads a signed Android AAB and signed iOS IPA exactly
once, to internal testing. Later stages promote those exact build identifiers.
Google production is a completed 100% rollout. Apple production is submitted
for review with automatic release enabled. Production never rebuilds mobile
binaries.

## Private intake

Fill the ignored `release/private/values.env` and the files described in
`docs/PRODUCTION_SIGNING_HANDOFF.md`, then run:

```bash
./scripts/validate-private-release-config.sh
./scripts/configure-mobile-release.sh --apply
./scripts/configure-release-branches.sh --apply
```

The first script validates the JKS alias, passwords, pinned Android certificate,
Apple distribution certificate/profile/team/bundle, App Store Connect P8 key,
metadata, tester files, contact details, and export-compliance constraints. The
configuration scripts upload secrets without printing them, configure keyless
Google OIDC, scope beta environments to `testing`, scope mobile production to
protected `main`, delete stale repository-level copies of mobile secrets,
verify their absence, and protect the release branches. Desktop credentials
are scoped separately to `desktop-production` on `release`. If Desktop inputs
are intentionally unavailable, apply only the release environment policy with
`./scripts/configure-github-release-environments.sh --apply`.

Keep `release/private/` untracked and back it up to encrypted offline storage.
Never place mobile IPA/AAB files, tester identities, private keys, or store
credentials in a GitHub Release.

## Apple one-time setup

Complete these items in Apple Developer and App Store Connect:

1. Accept the current agreements and register `com.passvault.ios`.
2. Create the App Store record and record its numeric app ID.
3. Create the Apple Distribution certificate and App Store provisioning profile.
4. Create an App Manager App Store Connect API key.
5. Add at least one eligible internal App Store Connect user/tester.
6. Create the external email-list TestFlight group and add real testers.
7. Complete App Privacy, age rating, category, pricing/availability, review
   contact, encryption, and all legal declarations.
8. Keep France excluded while `EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED`; do
   not change that constraint without completing the required French process.
9. When Desktop publication is planned, separately create a Developer ID
   Application certificate for production Desktop DMGs.

The iOS store archive runs on `macos-26` and requires Xcode 26 or newer because
of the linked Compose/UIKit SDK symbols.

## Google Play one-time setup

Complete these items in Play Console:

1. Create `com.passvault.android`, opt into Play App Signing, and register the
   pinned upload certificate in `release/android/`.
2. Complete App Content, Data Safety, content rating, target audience, app
   access, ads, category, contacts, privacy policy, pricing, and countries.
3. Invite the OIDC beta and production service accounts with the documented
   PassVault-only permissions.
4. Populate a real Play email list for the legacy path and attach it to the
   required testing tracks.
5. Attach at least one owned Google Group to `alpha`; Mobile Release Kit requires
   the Publisher API to read back that assignment before external promotion.
6. Confirm the account's production eligibility.

The Publishing API cannot configure or prove email-list membership or legal
questionnaire answers. Those remain explicit Console checks. See
`docs/GOOGLE_PLAY_READINESS.md` for the source-verified declarations.

## Store assets and verification

Run `Generate Store Screenshots` to create a short-lived artifact from isolated
builds. Download and review every image for fictional data and quality, then
install only the approved images in the tracked store-assets directories.
Production is blocked until:

```bash
./scripts/validate-mobile-store-assets.rb
```

Before merging `main` to `testing`, also run:

```bash
./gradlew test check detekt verifyDependencies
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :app-android:assembleDebug
./scripts/validate-ios-build-identities.sh
./scripts/test-release-automation.sh
./scripts/verify-ios-release-signing.sh
```

Perform physical-device checks for onboarding, wrong/correct passwords,
biometrics, lock/background behavior, credential/folder/TOTP flows, backup
export/import, RTL, large text, keyboard/safe-area behavior, and offline use.

## Operating the release

1. Commit an exact unused `VERSION_CODE` and merge a reviewed `main` →
   `testing` PR. During the shared-tooling pilot, choose one candidate path:
   dispatch Mobile Release Kit or manually dispatch the retained legacy
   `Testing Candidate`. Never run both for the same build. Legacy push uploads
   require the explicit `LEGACY_TESTING_RELEASE_ON_PUSH=true` repository policy
   opt-in and stay disabled during the pilot.
2. After Apple reports Beta App Review approved, run `Candidate Readiness` from
   the `testing` branch for that candidate tag. It verifies Apple and Google and
   then waits for the configured `release-promotion` reviewer. The dispatcher
   may provide this explicit approval. Approval advances
   `release` to the exact tested SHA. It does not start Desktop signing.
3. Run `Request Mobile Production Release` from `main` for the same candidate,
   choose `both`, and type `I_APPROVE_MOBILE_PRODUCTION`.
4. Open the child `Production Store Release` run and approve its
   `mobile-production` deployment. The owner can approve because the protected
   parent dispatches the child as `github-actions[bot]`; the manual gate and
   self-review prevention remain enabled. The child promotes the exact existing
   Play/TestFlight build and never rebuilds or uploads it.
5. Desktop is optional and independent. Only when it is wanted, manually run
   `Production Signing Validation` from `release`, approve
   `desktop-production`, and later publish the frozen Desktop bundle.

Never reuse a store build number, move an existing tag, replace signing keys
without a recorded rotation, or manually upload a different binary under the
candidate version.

## Application identities

Local developer runs are deliberately separate from Store distribution:

- Android Debug: PassVault Dev / `com.passvault.android.debug`.
- iOS Debug: PassVault Dev / `com.passvault.ios.debug`.
- Android Release and all Play tracks: PassVault / `com.passvault.android`.
- iOS Release, TestFlight, and App Store: PassVault / `com.passvault.ios`.

There is no testing flavor or testing bundle ID. Testing Candidate builds Release once, and later jobs promote the
recorded version code or Apple build number without rebuilding. CI validates both canonical Store identifiers before
creating receipts or performing an upload. F-Droid packaging is retired.
