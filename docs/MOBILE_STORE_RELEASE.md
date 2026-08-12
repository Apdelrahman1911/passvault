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
Google OIDC, scope beta environments to `testing`, scope production to
`release`, delete stale repository-level copies of mobile secrets, verify their
absence, and protect the release branches.

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
9. Create a Developer ID Application certificate for production desktop DMGs.

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
4. Populate a real Play email list and attach it to both `internal` and `beta`.
5. Confirm the account's production eligibility.

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
./gradlew :app-android:assembleStandardDebug
./scripts/test-release-automation.sh
./scripts/verify-ios-release-signing.sh
```

Perform physical-device checks for onboarding, wrong/correct passwords,
biometrics, lock/background behavior, credential/folder/TOTP flows, backup
export/import, RTL, large text, keyboard/safe-area behavior, and offline use.

## Operating the release

1. Merge a reviewed `main` → `testing` PR. `Testing Candidate` performs the
   internal uploads, waits for the external environment approval, promotes the
   exact builds, and publishes the unsigned desktop prerelease.
2. After Apple reports Beta App Review approved, run `Candidate Readiness` from
   the `testing` branch for that candidate tag. It verifies Apple and Google,
   advances `release` to the exact tested SHA, and starts no-publication
   production signing validation.
3. Approve `mobile-production`. The workflow signs/verifies Windows, notarizes
   and verifies macOS, freezes exact desktop artifacts, and only then starts the
   protected mobile-production promotion. Direct promotion is rejected without
   that matching validation result.
4. After both stores show the version live, run `Publish Stable Release` with
   the same tag. It verifies both store builds and publishes the previously
   frozen signed/notarized desktop bundle without rebuilding it.

Never reuse a store build number, move an existing tag, replace signing keys
without a recorded rotation, or manually upload a different binary under the
candidate version.
