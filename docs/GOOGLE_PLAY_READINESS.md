# Google Play Readiness Record

Last technical review: 8 August 2026

This is the source-backed preparation record for `com.passvault.android`. It is
not a legal certification and does not authorize an upload, review submission,
or publication. Machine-readable answers are in
`release/google-play/app-content.json` and are checked by
`scripts/validate-google-play-readiness.rb`.

## Verified application behavior

PassVault is a local-first password manager with no PassVault account, network
service, cloud sync, ads, analytics, telemetry, automatic crash upload, or Play
Billing. Sensitive vault fields and user-created `.pvault` backups are encrypted
locally. TOTP codes are generated on-device. Optional biometric unlock delegates
authentication to Android and protects unlock material with Android Keystore.

The source manifest requests:

- `CAMERA`, after a user chooses TOTP QR scanning; images are processed locally;
- `USE_BIOMETRIC`, for optional local vault unlock; and
- `HIDE_OVERLAY_WINDOWS`, for supported sensitive-screen protection.

The final merged release also contains `USE_FINGERPRINT` from AndroidX
Biometric for legacy compatibility and AndroidX Core’s app-specific,
signature-protected `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. The release has
no `INTERNET` or `ACCESS_NETWORK_STATE` permission and no application network
client. It does not request Advertising ID, location, contacts, broad storage,
media, microphone, phone, notification, exact-alarm, accessibility, VPN, or
billing permissions.

## Prepared Play Console answers

Enter these answers in **Policy and programs → App content**. The Play Developer
API has no endpoint for completing or submitting these declarations.

| Form | Recommended answer from verified code |
| --- | --- |
| Ads | **No**, the app does not contain ads. |
| App access | **All functionality is available without special access.** No remote credentials exist. Reviewer note: “Create a local vault with a new master password; no server account is required.” |
| Data safety | **No**, the app does not collect or share required user-data types off-device. Use `https://passvault.kiramanga.me/privacy/`. User-selected backup transfer to their own provider is not PassVault collection. |
| Account creation/deletion | **The app does not allow users to create an account.** Records can be deleted in-app; clearing app data or uninstalling removes the local vault, and exported backups are deleted by the user where saved. |
| Target audience | Recommended: **18 and over** and **not designed for children**. This distribution choice requires publisher confirmation before saving. |
| Content rating | Select **Utility, Productivity, Communication or Other**; not a game. Answer **No** to developer-provided violence, sexual content, strong language, controlled substances, gambling, public UGC, online interaction, location sharing, and digital purchases. User-entered private vault text is not published or exchanged. Use the approved support email for IARC contact. |
| Financial features | **My app doesn’t provide any financial features.** Locally storing a payment-card record is not a payment, wallet, loan, trading, or financial-advice service. |
| Health apps | **My app does not provide any health features.** |
| Government apps | **No**, this is not a government app and does not communicate government information. |
| News and magazine | **No**, this is not a news or magazine app. |
| Advertising ID | **No**, the app and its manifest do not use Advertising ID. |
| Social/dating and child safety | **Not in scope**; the app has no social, dating, messaging, public-content, or user-discovery service. |
| Restricted permissions | **None declared.** Camera is requested in context; no all-files, photo/video, location, SMS/call-log, accessibility, VPN, exact-alarm, or full-screen-intent declaration applies. |

Choose **App**, category **Tools**, with English (`en-US`) as the default and
Arabic (`ar`) as the second listing. The implementation has no monetization, so
**Free** is the recommended price; the account owner must confirm this because a
free app cannot later be changed to paid without creating a new package.

## Completed release infrastructure

- The Play package exists and both OIDC service identities can see it through
  separate GitHub environments without JSON keys.
- The ignored Play user export verifies active, PassVault-only app access and no
  account/global grant. App-level Admin is the owner-accepted exception recorded
  in `docs/MOBILE_STORE_RELEASE.md`; the GitHub release gates remain authoritative.
- Android upload alias `passvault-upload`, certificate pin, signed APK/AAB checks,
  bilingual private copy, public privacy/support pages, and internal-track
  automation are prepared.
- Closed testing is enabled and its initial release was submitted manually for
  Google review. Future external workflows fail closed without a validated real
  tester list; open testing has no enabled release path.
- Production still requires the protected `mobile-production` environment,
  reviewer approval, `I_APPROVE_PRODUCTION`, and
  `I_CONFIRM_REQUIRED_TESTING_COMPLETED`.

The internal track contains the signed `1.0.2` build and the initial closed-test
release has been sent for review in 177 countries/regions. The approved `en-US`
and `ar` store assets pass the repository checks. Console declarations, listing
state, contacts, tester groups, and country availability remain Console-owned and
must be verified there before production.

## Remaining Console-only actions

1. In **Policy and programs → App content**, complete and save every declaration
   above. Content rating and target audience require publisher attestations.
2. In **Grow users → Store presence → Main store listing**, change the default
   language from `en-GB` to `en-US`, add the approved `en-US` and `ar` copy, and
   set the approved contact website, support email, and privacy URL.
3. Review the four English and four Arabic phone screenshots listed in
   `release/store-assets/README.md`; they use fictional vault data. The approved
   512 × 512 icons and text-free 1024 × 500 feature graphics are also present and
   pass the local format/dimension checks.
4. In **Test and release → Setup → App integrity → App signing**, confirm Play App
   Signing is active and that the registered upload certificate matches
   `7D:4D:11:20:B1:D1:9F:5B:B9:42:E0:6C:60:0F:2B:64:81:E5:E6:82:47:52:23:FA:4E:7D:C7:B2:7C:DB:10:37`.
5. In **Monetize with Play → Products → App pricing**, confirm **Free** and choose
   distribution countries/regions after legal and tax review.
6. Open **Test and release → Production**. “Create new release” means production
   access is available; “Apply for production” means the Console-displayed tester
   count, duration, and feedback steps remain mandatory. The Publishing API does
   not expose this eligibility state.
7. Accept any outstanding developer agreements, finish account identity/contact
   verification, and resolve dashboard tasks. Do not send declarations or a
   release for review without owner approval.

France remains excluded from the separate iOS App Store configuration under
`docs/EXPORT_COMPLIANCE.md`; this Google Play record does not alter Apple
availability.
