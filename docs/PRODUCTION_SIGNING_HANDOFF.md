# Private Production Configuration Handoff

This is the single handoff for every private or publisher-owned input consumed by PassVault release automation.
Do not commit any value or file named here. `release/private/` is ignored, and the configuration scripts refuse to
continue if anything below it is tracked or reached through a symlink.

## Preferred setup (no manual Base64 copying)

Add the missing entries to `release/private/values.env`, place the files at the paths below, then run:

```bash
./scripts/validate-private-release-config.sh
./scripts/configure-mobile-release.sh --apply
```

The first command is local/read-only and validates identities, private-key pairing, expiry, EKUs, profile ownership,
bundle IDs, signing fingerprints, metadata, and tester files. The second command requires authenticated `gh` and
`gcloud`; it streams files directly into protected GitHub environment secrets, derives public fingerprints, configures
Google OIDC, removes broader-scope duplicates, and verifies names/scopes. It never prints private values.

## Values and protected files to supply

Add these exact entries to the ignored `release/private/values.env` file. Values shown literally below are fixed by
the repository; angle-bracket values must be replaced.

```dotenv
GITHUB_REPOSITORY=Apdelrahman1911/passvault
GITHUB_DEPLOYMENT_APPROVER=<exact GitHub username of the independent production reviewer>

PUBLISHER_NAME_EN=<approved English publisher name>
PUBLISHER_NAME_AR=<approved Arabic publisher name>
COPYRIGHT_HOLDER_EN=<approved English copyright holder>
COPYRIGHT_HOLDER_AR=<approved Arabic copyright holder>
COUNTRY_OR_JURISDICTION=<publisher country or jurisdiction>
SUPPORT_EMAIL=<monitored support email>
SECURITY_EMAIL=<monitored security email>
PROJECT_URL=<public HTTPS project URL>
PRIVACY_POLICY_URL=<public HTTPS privacy-policy URL>
SUPPORT_URL=<public HTTPS support URL>

ANDROID_PACKAGE_NAME=com.passvault.android
ANDROID_UPLOAD_KEYSTORE_FILE=release/private/files/android-upload-keystore.jks
KEYSTORE_PASSWORD=<existing Play upload-keystore password>
KEY_ALIAS=passvault-upload
KEY_PASSWORD=<existing Play upload-key password>

IOS_BUNDLE_ID=com.passvault.ios
APPLE_TEAM_ID=<10-character Apple Developer Team ID>
APP_STORE_SKU=<existing App Store Connect SKU>
APP_STORE_APP_ID=<existing numeric App Store Connect app ID>
ASC_ISSUER_ID=<App Store Connect API issuer UUID>
ASC_KEY_ID=<10-character App Store Connect API key ID>
ASC_PRIVATE_KEY_FILE=release/private/files/app-store-connect-key.p8
IOS_DISTRIBUTION_CERTIFICATE_FILE=release/private/files/ios-distribution-certificate.p12
IOS_DISTRIBUTION_CERTIFICATE_PASSWORD=<P12 export password>
IOS_PROVISIONING_PROFILE_FILE=release/private/files/ios-provisioning-profile.mobileprovision
TESTFLIGHT_EXTERNAL_GROUP=<exact existing external TestFlight group name>
APP_REVIEW_CONTACT_NAME=<review contact first and last name>
APP_REVIEW_EMAIL=<review contact email>
APP_REVIEW_PHONE=<review contact phone in international format>
EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED
IOS_FRANCE_AVAILABLE=false

GOOGLE_CLOUD_PROJECT_ID=<Google Cloud project ID used for Play OIDC>
GOOGLE_CLOUD_PROJECT_NUMBER=<numeric Google Cloud project number>

# Select exactly one Windows backend. The SignPath block is the recommended
# starting point for this open-source project; delete all Azure/PFX keys below
# that do not belong to the selected backend.
WINDOWS_SIGNING_BACKEND=signpath
WINDOWS_EXPECTED_PUBLISHER_NAME=<exact certificate Simple Name shown by SignPath>
WINDOWS_SIGNPATH_API_TOKEN=<SignPath submitter API token>
WINDOWS_SIGNPATH_ORGANIZATION_ID=<SignPath organization UUID>
WINDOWS_SIGNPATH_PROJECT_SLUG=<SignPath project slug>
WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG=<SignPath signing-policy slug>
WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG=<slug created from the tracked SignPath XML>

MACOS_CERTIFICATE_FILE=release/private/files/macos-developer-id-application.p12
MACOS_CERTIFICATE_PASSWORD=<P12 export password>
MACOS_NOTARIZATION_APPLE_ID=<Apple ID email used for notarization>
MACOS_NOTARIZATION_PASSWORD=<Apple app-specific password, not the Apple ID password>

RELEASE_NOTES_EN_FILE=release/private/release-notes-en.md
RELEASE_NOTES_AR_FILE=release/private/release-notes-ar.md
PRIVACY_TEXT_EN_FILE=release/private/privacy-en.md
PRIVACY_TEXT_AR_FILE=release/private/privacy-ar.md
STORE_METADATA_EN_FILE=release/private/store-metadata-en.env
STORE_METADATA_AR_FILE=release/private/store-metadata-ar.env
STORE_DESCRIPTION_EN_FILE=release/private/store-description-en.md
STORE_DESCRIPTION_AR_FILE=release/private/store-description-ar.md

TESTFLIGHT_INTERNAL_EMAILS=<optional comma-separated existing App Store Connect user emails>
TESTFLIGHT_EXTERNAL_TESTERS_FILE=release/private/testflight-external-testers.csv
PLAY_CLOSED_TESTERS_FILE=release/private/play-closed-testers.txt
PLAY_USERS_FILE=release/private/play-users.csv
PLAY_SERVICE_ACCOUNT_PERMISSION_MODE=STRICT_LEAST_PRIVILEGE
GOOGLE_CLOSED_TEST_GROUP=
```

Keep `EXEMPT_APPROVED`/`false` only while the existing approved export-compliance decision and France exclusion remain
true. If that decision changes, use `NON_EXEMPT_APPROVED` only after the required legal/export review and set France
availability to the reviewed value.

### Where each credential comes from

| Input | What it contains / required format | Obtain it from | Scope and release stage |
|---|---|---|---|
| Android JKS + three passwords/alias | Existing Play upload private key; JKS/PKCS12 accepted; alias must be `passvault-upload` | The encrypted backup of the key already registered in Google Play; use Play's upload-key reset process if lost | `mobile-beta`; testing upload only |
| Apple Distribution P12/password | Current Apple Distribution certificate plus matching private key, password-protected PKCS#12 | Keychain Access after creating/downloading the distribution certificate in Apple Developer | `mobile-beta`; iOS internal build/upload only |
| App Store provisioning profile | Current App Store distribution `.mobileprovision` for team and `com.passvault.ios`, with the supplied certificate | Apple Developer Certificates, Identifiers & Profiles | `mobile-beta`; iOS internal build/upload only |
| App Store Connect P8/key ID/issuer | One PKCS#8 `AuthKey_*.p8`; 10-character key ID; issuer UUID | App Store Connect → Users and Access → Integrations; App Manager access | P8 secret in all three mobile environments; identifiers are repository variables; testing and production |
| App review phone | Real review-contact telephone number | Publisher/review contact | Secret in `mobile-external-beta` and `mobile-production` |
| TestFlight tester CSV | UTF-8 `first_name,last_name,email` rows with real testers | Approved tester list | `mobile-external-beta`; testing only |
| Play closed-test list | UTF-8 file with one real email per line | Approved tester list and Play Console email list | `mobile-external-beta`; testing only |
| Store metadata source files | Approved bilingual UTF-8 metadata/privacy/release text | Approved store copy | Script creates `STORE_METADATA_ARCHIVE_BASE64` in all mobile environments |
| SignPath submitter token | Bounded single-line API token for a SignPath user that can submit requests but cannot administer the organization | SignPath → User settings → API tokens, after the project/policy is approved | `mobile-production` environment secret; production desktop validation only |
| SignPath organization ID and three slugs | Organization UUID plus exact project, signing-policy, and artifact-configuration slugs | SignPath organization/project URLs and configuration pages | `mobile-production` environment variables; production desktop validation only |
| Windows expected publisher | Exact certificate Simple Name returned by the selected production certificate; case-sensitive, one line, at most 200 characters | The SignPath signing-policy certificate details (or the Azure/PFX certificate subject for another backend) | Repository variable; production verification only |
| Windows PFX/password (local-PFX alternative only) | Existing publicly trusted Authenticode certificate, matching private key, Code Signing EKU `1.3.6.1.5.5.7.3.3`, password-protected PFX whose CA contract explicitly permits export and CI import | Existing trusted code-signing CA/provider; do not manufacture an exportable key for a certificate that must be HSM-backed | `mobile-production` environment secrets; production desktop validation only |
| Developer ID P12/password | `Developer ID Application: … (TEAMID)` certificate plus matching private key, password-protected P12 | Apple Developer certificate portal and Keychain Access | `mobile-production`; production desktop validation only |
| Notarization Apple ID/app-specific password | Apple ID email and an app-specific password | Apple ID account → Sign-In and Security → App-Specific Passwords | `mobile-production`; production desktop validation only |
| Google project ID/number | Existing Google Cloud project with Workload Identity Federation | Google Cloud Console | Used to derive environment-scoped OIDC provider/service-account variables; no JSON key is stored |

The local-PFX Windows and macOS certificate SHA-256 fingerprints, full Developer ID identity, Apple notarization
team, Android upload-certificate SHA-256, and iOS distribution SHA-1 are derived from validated files. Remote Windows
backends deliberately do not pin a leaf fingerprint because their managed production certificates can rotate; they
pin the backend resource/policy and verify the exact expected publisher, Windows trust chain, signature, and timestamp
on every output. Do not type or maintain local certificate fingerprints by hand.

## Windows backend setup

Select exactly one backend. The validator rejects mixed inputs and the configuration script removes stale values and
secrets from every broader or inactive scope.

### SignPath (recommended starting point for this repository)

This route keeps the private key in the signing provider and sends only the two exact unsigned request sets generated
by CI: first the `PassVault.exe` launcher, then the final PassVault EXE/MSI installers. Vendor-signed runtime files are
never re-signed. The returned file set must exactly match the immutable request catalog; CI independently verifies
Windows trust, timestamp, and publisher before atomically replacing the unsigned files.

One-time owner work:

1. Apply at <https://signpath.org/> if the project meets the current SignPath Foundation open-source requirements, or
   create a paid SignPath organization. Provider acceptance is an external decision and cannot be asserted by this
   repository.
2. In SignPath, connect the GitHub trusted build system to `Apdelrahman1911/passvault` and create a PassVault project.
   Restrict it to GitHub-hosted workflow artifacts from this repository.
3. Create an artifact configuration by importing the tracked
   `release/windows/signpath-artifact-configuration.xml`. Do not copy/edit the XML in the SignPath UI. Record the slug
   as `WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG`.
4. Create/select the public-trust Authenticode signing certificate and a production signing policy for that artifact
   configuration. Record its slug as `WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG`. If Foundation terms require every request
   to be approved, assign a separate human the SignPath Approver role; the workflow waits up to two hours and cannot
   bypass that approval.
5. Copy the organization UUID, project slug, and exact signing-policy certificate Simple Name into the corresponding
   values. The Simple Name is `WINDOWS_EXPECTED_PUBLISHER_NAME`; do not infer it from the project or publisher name.
6. Create a token for a SignPath Submitter user and put it only in `WINDOWS_SIGNPATH_API_TOKEN`. Do not give this token
   Approver or organization-administrator authority. Store the recovery/administration credentials outside GitHub.
7. Publish and keep current the repository's [code-signing policy](CODE_SIGNING_POLICY.md). Before submitting a
   Foundation application, add the real human role membership requested by the provider; the repository cannot infer
   the identity of the independent GitHub deployment reviewer.

Use exactly this private configuration and omit every `WINDOWS_AZURE_*`, `WINDOWS_ARTIFACT_SIGNING_*`,
`WINDOWS_CERTIFICATE_*`, `WINDOWS_PFX_POLICY_CONFIRMATION`, and `WINDOWS_TIMESTAMP_URL` entry:

```dotenv
WINDOWS_SIGNING_BACKEND=signpath
WINDOWS_EXPECTED_PUBLISHER_NAME=<exact production certificate Simple Name>
WINDOWS_SIGNPATH_API_TOKEN=<submitter token>
WINDOWS_SIGNPATH_ORGANIZATION_ID=<UUID>
WINDOWS_SIGNPATH_PROJECT_SLUG=<slug>
WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG=<slug>
WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG=<slug>
```

### Azure Artifact Signing (eligible organization alternative)

Microsoft's current Public Trust availability is geographically and entity-type restricted. Confirm eligibility in
the official documentation before creating resources. For the GitHub OIDC federated credential use:

```text
Issuer:   https://token.actions.githubusercontent.com
Audience: api://AzureADTokenExchange
Subject:  repo:Apdelrahman1911/passvault:environment:mobile-production
```

Grant the federated application only the Artifact Signing certificate-profile signer role at the narrowest supported
resource scope. Create a Public Trust account/profile, then use exactly:

```dotenv
WINDOWS_SIGNING_BACKEND=azure-artifact-signing
WINDOWS_EXPECTED_PUBLISHER_NAME=<exact Azure certificate Simple Name>
WINDOWS_TIMESTAMP_URL=http://timestamp.acs.microsoft.com
WINDOWS_AZURE_CLIENT_ID=<federated application/client UUID>
WINDOWS_AZURE_TENANT_ID=<Microsoft Entra tenant UUID>
WINDOWS_AZURE_SUBSCRIPTION_ID=<Azure subscription UUID>
WINDOWS_ARTIFACT_SIGNING_ENDPOINT=<exact https://REGION.codesigning.azure.net endpoint>
WINDOWS_ARTIFACT_SIGNING_ACCOUNT=<account name>
WINDOWS_ARTIFACT_SIGNING_PROFILE=<certificate-profile name>
```

The client ID, tenant ID, subscription ID, endpoint, account, and profile become protected `mobile-production`
environment variables. There is no Azure client secret: GitHub exchanges its environment-bound OIDC token. Omit all
SignPath and local-PFX inputs.

### Local PFX (existing CA-authorized key only)

New ordinary software-exportable OV code-signing keys generally do not satisfy current industry private-key
protection rules. Use this backend only for an existing public-trust certificate whose issuing CA/provider explicitly
authorizes export, ephemeral GitHub-hosted-runner import, and automated signing. The validator requires the exact
policy acknowledgement below.

```dotenv
WINDOWS_SIGNING_BACKEND=local-pfx
WINDOWS_EXPECTED_PUBLISHER_NAME=<exact certificate subject Common Name>
WINDOWS_CERTIFICATE_FILE=release/private/files/windows-code-signing.pfx
WINDOWS_CERTIFICATE_PASSWORD=<PFX export password>
WINDOWS_TIMESTAMP_URL=<CA-approved absolute HTTP(S) RFC-3161 timestamp URL>
WINDOWS_PFX_POLICY_CONFIRMATION=I_CONFIRM_CA_AUTHORIZED_EXPORTABLE_PRODUCTION_KEY
```

Omit all Azure and SignPath inputs. The local validator checks the private-key pair, current validity, Code Signing
EKU, exact publisher, and derives the certificate SHA-256 pin before uploading anything.

## Exact GitHub destinations and consumers

| Name | GitHub type and exact location | Workflow consumer | Scope |
|---|---|---|---|
| `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` | Environment secrets, `mobile-beta` | `mobile-store-release.yml` Android internal upload | testing only |
| `IOS_DISTRIBUTION_CERTIFICATE_BASE64`, `IOS_DISTRIBUTION_CERTIFICATE_PASSWORD`, `IOS_PROVISIONING_PROFILE_BASE64` | Environment secrets, `mobile-beta` | `mobile-store-release.yml` iOS archive/export/internal upload | testing only |
| `ASC_PRIVATE_KEY_BASE64`, `STORE_METADATA_ARCHIVE_BASE64` | Environment secrets in `mobile-beta`, `mobile-external-beta`, and `mobile-production` | Mobile upload/promotion, readiness, live checks | testing and production |
| `TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64`, `PLAY_CLOSED_TESTERS_BASE64` | Environment secrets, `mobile-external-beta` | External TestFlight/Play closed testing | testing only |
| `APP_REVIEW_PHONE` | Environment secrets, `mobile-external-beta` and `mobile-production` | Beta review and App Store production review | testing and production |
| `WINDOWS_SIGNPATH_API_TOKEN` | Environment secret, `mobile-production` (SignPath only) | `release.yml` SignPath request submission | production only |
| `WINDOWS_SIGNPATH_ORGANIZATION_ID`, `WINDOWS_SIGNPATH_PROJECT_SLUG`, `WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG`, `WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG` | Environment variables, `mobile-production` (SignPath only) | `release.yml` SignPath request binding | production only |
| `WINDOWS_AZURE_CLIENT_ID`, `WINDOWS_AZURE_TENANT_ID`, `WINDOWS_AZURE_SUBSCRIPTION_ID`, `WINDOWS_ARTIFACT_SIGNING_ENDPOINT`, `WINDOWS_ARTIFACT_SIGNING_ACCOUNT`, `WINDOWS_ARTIFACT_SIGNING_PROFILE` | Environment variables, `mobile-production` (Azure only) | `release.yml` OIDC/Azure Artifact Signing | production only |
| `WINDOWS_CERTIFICATE_BASE64`, `WINDOWS_CERTIFICATE_PASSWORD` | Environment secrets, `mobile-production` (local-PFX only) | `release.yml` ephemeral PFX import/signing | production only |
| `MACOS_CERTIFICATE_BASE64`, `MACOS_CERTIFICATE_PASSWORD`, `MACOS_NOTARIZATION_APPLE_ID`, `MACOS_NOTARIZATION_PASSWORD` | Environment secrets, `mobile-production` | `release.yml` Developer ID signing/notarization | production only |
| `PUBLISHER_NAME`, `COPYRIGHT_HOLDER` | Repository variables derived from `PUBLISHER_NAME_EN` and `COPYRIGHT_HOLDER_EN` | Candidate, mobile, desktop, Pages, and stable-release workflows | non-secret; testing and production |
| `ANDROID_UPLOAD_CERTIFICATE_SHA256` | Repository variable derived from the validated Android upload certificate in the supplied keystore | Candidate and mobile workflows bind an Android candidate to the registered upload identity | non-secret fingerprint; testing and production provenance |
| `IOS_DISTRIBUTION_CERTIFICATE_SHA1` | Repository variable derived from the validated Apple Distribution P12 | Candidate and mobile workflows bind an iOS candidate to the validated distribution identity | non-secret fingerprint; testing and production provenance |
| `MACOS_DEVELOPER_ID_CERTIFICATE_SHA256` | Repository variable derived from the validated Developer ID Application P12 | `release.yml` verifies the imported macOS signing identity before signing/notarization | non-secret fingerprint; production only |
| `WINDOWS_SIGNING_CERTIFICATE_SHA256` | Repository variable derived from the validated Windows PFX for the `local-pfx` backend; automatically deleted for SignPath/Azure | `release.yml` verifies the imported local Authenticode identity | non-secret fingerprint; production only, local-PFX backend only |
| Publisher/contact/URL/app identifiers, `WINDOWS_SIGNING_BACKEND`, `WINDOWS_EXPECTED_PUBLISHER_NAME`, applicable signing fingerprints, optional `WINDOWS_TIMESTAMP_URL`, `MACOS_SIGNING_IDENTITY`, `MACOS_NOTARIZATION_TEAM_ID` | Repository variables | Candidate, mobile, desktop, and stable-release workflows | non-secret; both |
| `TESTFLIGHT_EXTERNAL_GROUP`, export-compliance variables, review name/email | Environment variables in the mobile environments that consume them | Mobile TestFlight/App Store workflows | least-privilege environment scope |
| `GOOGLE_WORKLOAD_IDENTITY_PROVIDER`, `GOOGLE_SERVICE_ACCOUNT` | Environment variables created in `mobile-beta`, `mobile-external-beta`, `mobile-production`, `play-access-beta`, and `play-access-production` | Google authentication action | keyless testing/production identities |

`mobile-production` must allow deployments only from `release`, require the configured reviewer, and prevent self
review. Do not put any of these secrets at repository scope. The configuration script deletes and verifies the absence
of stale repository-level copies.

Desktop Touch ID and Windows Hello introduce no additional secret, certificate, entitlement, relying-party server,
or cloud credential. Their repository-built native bridges consume the existing `MACOS_*` Developer ID/notarization
inputs and the selected existing `WINDOWS_*` Authenticode backend above. Do not create a biometric secret or place a
VEK/PRF value in GitHub configuration.

## Safe direct-upload commands (fallback only)

The configuration script is preferred. If GitHub UI entry is unavoidable, encode locally as a single Base64 line.
The safer CLI form streams directly to GitHub without printing or creating an encoded file:

```bash
openssl base64 -A -in release/private/files/android-upload-keystore.jks | gh secret set KEYSTORE_BASE64 --env mobile-beta --repo Apdelrahman1911/passvault
openssl base64 -A -in release/private/files/ios-distribution-certificate.p12 | gh secret set IOS_DISTRIBUTION_CERTIFICATE_BASE64 --env mobile-beta --repo Apdelrahman1911/passvault
openssl base64 -A -in release/private/files/ios-provisioning-profile.mobileprovision | gh secret set IOS_PROVISIONING_PROFILE_BASE64 --env mobile-beta --repo Apdelrahman1911/passvault
openssl base64 -A -in release/private/files/app-store-connect-key.p8 | gh secret set ASC_PRIVATE_KEY_BASE64 --env mobile-production --repo Apdelrahman1911/passvault
openssl base64 -A -in release/private/files/macos-developer-id-application.p12 | gh secret set MACOS_CERTIFICATE_BASE64 --env mobile-production --repo Apdelrahman1911/passvault
```

For the local-PFX backend only, the safe direct-upload fallback is:

```bash
openssl base64 -A -in release/private/files/windows-code-signing.pfx | gh secret set WINDOWS_CERTIFICATE_BASE64 --env mobile-production --repo Apdelrahman1911/passvault
```

SignPath tokens are already text; never Base64-encode them. Stream the token only through the configuration script,
or enter it directly as the `WINDOWS_SIGNPATH_API_TOKEN` secret in Settings → Environments → `mobile-production`.
The P8 and generated metadata archive are also needed in the beta environments; use the automated script so all
copies, variables, fingerprints, environment branch policies, and absence checks remain consistent.

## Release sequence after configuration

1. `Testing Candidate` builds each mobile binary once, validates it, records SHA-256 receipts, retains the exact signed
   artifacts privately for 90 days, uploads to internal testing, and promotes the same store build externally.
2. `Candidate Readiness` requires the exact testing commit and approved store state, fast-forwards `release`, and starts
   `Production Signing Validation`—not production publication.
3. Approve the protected `mobile-production` deployment. Validation signs every required Windows native file/package,
   signs all nested macOS code with Hardened Runtime, receives `Accepted` notarization, staples/verifies, checks
   Gatekeeper, freezes the exact bundle, and publishes nothing.
4. Only after validation succeeds may `Production Store Release` promote the exact tested mobile build. Its validation
   job rejects any direct attempt that lacks the matching successful signing-validation artifact.
5. After both stores are verifiably live, `Publish Stable Release` downloads and re-verifies the frozen signed bundle
   and its attestations. It never rebuilds desktop packages.
