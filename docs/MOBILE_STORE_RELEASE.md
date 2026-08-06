# PassVault Mobile Store Release

The repository supports Android internal/closed/production uploads and iOS
internal TestFlight, external TestFlight, and App Store review-candidate uploads.
It never publishes iOS automatically. Android production requires both the
`mobile-production` approval plus the exact dispatch confirmations
`I_APPROVE_PRODUCTION` and `I_CONFIRM_REQUIRED_TESTING_COMPLETED`.

The current Kotlin/Compose iOS framework includes a native dependency built for
iOS 18.5, so the Xcode host intentionally declares iOS 18.5 as its minimum.
Lowering that target requires upgrading or replacing the dependency and
revalidating every linked object before release.

## 1. Complete the local private intake

Read `release/private/README.md`, fill `release/private/values.env`, replace all
metadata placeholders, and add the four named files under
`release/private/files/`. Keep the directory after setup as an encrypted offline
backup. It is ignored by Git and must remain untracked.

```bash
./scripts/validate-private-release-config.sh
```

Validation checks the JKS alias/passwords and pinned SHA-256 certificate, the
P12 private key/certificate/team, the provisioning profile team/bundle, the P8,
IDs, URLs, any supplied tester emails, bilingual copy, and export-compliance decision. The
ignored report contains names and validation results only.

`values.env` is parsed strictly as dotenv data, never sourced as shell code.
Matching single or double quotes are removed without evaluating their contents.
On macOS, legacy Keychain PKCS#12 exports are checked with Homebrew OpenSSL and
its legacy provider when available; failures identify the password, provider,
certificate, private key, key pairing, or provisioning-profile match separately.

Tester fields and files may stay empty during infrastructure and signing
preparation. They are reported as deferred and are never uploaded. External
TestFlight and Play closed-test jobs validate their own real tester file before
any store call. Play open testing has no enabled upload path. Production still
requires completed store testing, protected review, and both confirmation phrases.

## 2. Complete first-time Apple setup

1. Accept current agreements and register `com.passvault.ios` in Certificates,
   Identifiers & Profiles.
2. Create the App Store Connect record manually with SKU `passvault-ios-2026`;
   copy its numeric Apple ID to `APP_STORE_APP_ID`.
3. Create an Apple Distribution certificate and App Store provisioning profile,
   then export/copy them using the exact private filenames.
4. Create a Team App Store Connect API key with the App Manager role and record
   its issuer/key IDs. The `.p8` can only be downloaded once.
5. When tester setup resumes, add the emails in `TESTFLIGHT_INTERNAL_EMAILS` as App Store Connect users and
   internal testers. Create the external group named by
   `TESTFLIGHT_EXTERNAL_GROUP`; the workflow imports the external CSV into it.
   Empty groups can be prepared without assigning users or builds by running
   `ruby scripts/configure-app-store-connect-beta.rb --apply` after validation.
6. Review `docs/EXPORT_COMPLIANCE.md`, then complete App Privacy, age rating,
   category, pricing/availability, encryption, review contact, and legal
   declarations. The completed encryption questionnaire reported that no
   documentation is required while France is excluded. Keep
   `EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED` and
   `IOS_FRANCE_AVAILABLE=false` for this release configuration. Never enable
   France silently: reopen App Encryption Documentation and complete the French
   compliance process before changing either value. Publish the approved
   bilingual privacy text at `PRIVACY_POLICY_URL` before configuration.

## 3. Complete first-time Google Play setup

1. Create the Play app manually using `com.passvault.android`, opt into Play App
   Signing, complete the dashboard declarations, and upload the first signed AAB
   manually if the Publishing API cannot see a new app yet.
2. Complete App content, Data safety, content rating, target audience, app
   access (no remote account is required), ads, category, contact, and privacy
   policy sections.
3. When closed testing resumes, prefer a Play Console email list populated from
   the validated `play-closed-testers.txt` file and attach it to the closed
   `beta` track. `GOOGLE_CLOSED_TEST_GROUP` is optional legacy configuration;
   use it only if the approved test plan specifically chooses a Google Group.
   The Publishing API does not manage email-list or group membership.
4. Your existing account can skip the newer personal-account tester eligibility
   gate per the supplied account status; the internal/closed tracks remain the
   release-quality gates.

Use `docs/GOOGLE_PLAY_READINESS.md` for the source-verified App content answers,
Console paths, and remaining owner confirmations. Validate the tracked
machine-readable record with:

```bash
./scripts/validate-google-play-readiness.rb
```

The no-upload OIDC check inventories tracks, localized listing completeness,
public graphics, app contact-field presence, tester-group counts, and country
availability using an empty disposable edit. It never prints listing text or
tester identities, and it deletes the edit without committing it. Play does not
expose App content questionnaire state, account production eligibility, or
email-list membership through the Publishing API, so those remain explicit
Console checks.

## 4. Configure GitHub and keyless Google access

Authenticate locally, then run the scripts only after private validation passes:

```bash
gh auth status
# If invalid:
gh auth login -h github.com

gcloud auth login
./scripts/configure-mobile-release.sh --apply
```

The wrapper validates everything before its first mutation, then runs the
GitHub and Google setup in order. The two individual configuration scripts
remain available for repair or independent reruns.

The GitHub script displays the authenticated account, repository, and approver,
then requires the exact repository name before mutation. It configures
`mobile-beta`, `mobile-external-beta`, and `mobile-production`, streams secrets
through standard input, and verifies every name/scope through GitHub. External
beta and production require the configured reviewer and only allow `main`.
The local `GITHUB_DEPLOYMENT_APPROVER` input is stored in GitHub as
`DEPLOYMENT_APPROVER` because GitHub reserves variable names beginning with
`GITHUB_`; reviewer protection is configured from the same verified local value.

The Google script creates repository/environment-restricted Workload Identity
providers and separate beta/production service accounts. It never creates a JSON
key. In Play Console, invite the generated beta service account with release and
testing-track permissions only; invite production with only the production and
store-presence permissions it needs.

### PassVault Play app-Admin exception

The account owner explicitly accepts app-level Admin access for both release
service accounts as a PassVault-specific security exception. This includes
financial-data visibility, permission management, reviews, production and test
releases, tester management, listings, drafts, orders, policy content, quality,
and deep-link capabilities. The exception is valid only when all of the
following remain true:

- the exported Play user status is `ACCESS_GRANTED`;
- the only app grant is `com.passvault.android`;
- no account-level or global permission is present;
- `PLAY_SERVICE_ACCOUNT_PERMISSION_MODE=PASSVAULT_APP_ADMIN_ACCEPTED` exists
  only in the ignored local `values.env`;
- Google Cloud service accounts have no project role or user-managed JSON key;
- beta OIDC remains limited to `mobile-beta` and `mobile-external-beta`, while
  production OIDC remains limited to `mobile-production`.

Without that exact opt-in value, validation defaults to role-separated strict
least privilege. Re-export `play-users.csv` and revalidate after every Play
permission change. `Play access check` may create an empty disposable edit to
verify federated package visibility; it reads tracks/listings, deletes the edit,
and has no bundle, edit-commit, metadata-write, release, or publication path.

## 5. Prepare and test the release

Increment `VERSION_CODE` for every store upload and update semantic
`VERSION_NAME`. Use fictional data in all screenshots, then add the bilingual
graphics documented in `release/store-assets/README.md`.

```bash
./gradlew test check detekt verifyDependencies
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :app-android:assembleStandardDebug
./scripts/test-release-automation.sh
./scripts/verify-ios-release-signing.sh  # macOS; builds a temporary signed archive
./scripts/validate-mobile-store-assets.rb   # required for production
```

Perform physical-device checks for onboarding, wrong/correct password, Face ID
or Android biometrics, lock/background behavior, credential/folder/TOTP flows,
backup export/import, RTL Arabic, large text, keyboard/safe-area behavior, and
offline operation.

## 6. Run protected uploads

From GitHub Actions, dispatch **Mobile Store Release** on `main`:

- `internal`: Play internal and/or internal TestFlight.
- `external`: Play closed beta and/or external TestFlight review; protected and
  disabled until the selected platform has a real, validated tester list.
- `production`: requires protected approval and both exact confirmations. Android
  releases to production. iOS uploads metadata, screenshots, and a candidate but
  does not submit for review or release it.

After external acceptance, review store previews, crash/pre-launch reports, and
release notes. Submit the iOS candidate and select manual/phased release in App
Store Connect only after explicit owner approval. Never reuse a store build
number, move a release tag, or replace a signing key without recording rotation.

Official references: [Apple screenshot specifications](https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications/),
[Google Play preview assets](https://support.google.com/googleplay/android-developer/answer/9866151),
and [GitHub deployment environments](https://docs.github.com/en/actions/reference/workflows-and-actions/deployments-and-environments).
