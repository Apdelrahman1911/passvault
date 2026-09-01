# Mobile Release Kit Migration

PassVault is integrated with Mobile Release Kit in parallel-pilot mode. The
Android preflight, internal-candidate, and external-testing callers are pinned
to immutable Mobile Release Kit commit
`7eebb2656d28df33e4d8e5135f1f8fb64404e2bd` (`v0.1.2`). No Production caller
exists. The legacy release paths remain intact until the shared path completes
an internal upload and exact-build external promotion.

## Pilot scope

Android is the first pilot platform. Its Store identity, `release` variant,
Play `alpha` closed-testing track, and public upload-certificate SHA-256 are all
committed and approved. `VERSION_CODE=1017001` and iOS
`CURRENT_PROJECT_VERSION=1017001` are the exact committed candidate build;
both shared and legacy workflows consume that value. CI run numbers are no
longer Store build allocators. The online Store gate must still prove that this
exact build is unused before an upload begins.

The Android Gradle signing configuration accepts the shared names below while
retaining the legacy `KEYSTORE_*` environment variables and
`passvault.requireReleaseSigning` property used by existing automation:

```text
MOBILE_RELEASE_ANDROID_KEYSTORE_PATH
MOBILE_RELEASE_ANDROID_KEYSTORE_PASSWORD
MOBILE_RELEASE_ANDROID_KEY_ALIAS
MOBILE_RELEASE_ANDROID_KEY_PASSWORD
MOBILE_RELEASE_REQUIRE_SIGNING=true
```

Private values remain external to Git. The final AAB must still match the
public fingerprint in `release/android/passvault-release-cert.sha256`.

## iOS gate

iOS stays enabled so diagnosis can validate the public Release bundle ID,
Team ID, Xcode project, shared scheme, Release archive configuration, retained
symbols policy, and reviewed no-demo-account/export setting. Its identity is
deliberately `unverified`: the numeric App Store application ID, external
TestFlight group, and currently approved distribution-certificate SHA-256 are
not established by the shared public configuration. Those fields must be
verified against App Store Connect and the current publisher-owned signing
material before changing `identityStatus` to `approved`.

The existing `scripts/validate-ios-build-identities.sh` remains the application-
owned check for Debug/Release separation. It is attached to the iOS artifact
phase because it requires Xcode and must not make an Android-only Linux
preflight fail.

The Release application target maps four user-defined `MOBILE_RELEASE_IOS_*`
signing values to the standard Xcode keys. Shared signing overrides therefore
stay target-local rather than leaking into dependency targets; the existing
Automatic/team defaults remain unchanged when the shared tool is absent.

## Metadata and private-input gate

`release/store-assets` is the committed source for the current Android and iOS
screenshots and graphics. The configured locale sets match the directories
actually present: Android `ar` and `en-US`; iOS `ar-SA` and `en-US`.

The six ignored `.DS_Store` files found during the audit were removed. The
project validator now rejects Finder metadata, a regression test enforces that
gate, and the global ignore prevents accidental tracking.

The reviewed Arabic and English Play title, short description, full
description, and build `1013002` release notes are committed in the shared
metadata layout. Their character counts match the live Play listing readback.
Screenshots, icons, and feature graphics remain the already approved tracked
assets. Private tester identities and account credentials remain outside Git.

The shared configuration intentionally preserves the repository's existing
closed-testing policy. It does not inspect, copy, or infer the private tester
list, and it must not enable Play open testing without a separately reviewed
product decision. Before activation, the `alpha` track must expose at least one
assigned Google Group through the Android Publisher API. Mobile Release Kit
records only the boolean proof that an assignment exists; it never records the
group name or tester identities. An email-list-only closed track cannot be
proved by the supported API and therefore remains an explicit activation
blocker. Play Console accepted the owned tester Group for the `alpha` track on
22 August 2026 and submitted that tester change for Google review. The shared
pilot stays blocked until the non-publishing API check reads back at least one
Google Group on `alpha`; `beta` is the open-testing track and is deliberately
not used by this pilot.

Owner/legal Console work remains manual, including agreements, pricing and
territories, privacy/content declarations, tester membership, production
eligibility, and visual approval of screenshots. In particular, the existing
France/export-compliance constraint remains authoritative.

## Deliberate exclusions

The shared mobile integration does not own PassVault's Windows, macOS, or Linux
packaging, Authenticode signing, Developer ID signing/notarization, desktop
artifact freezing, or GitHub Release publication. Those existing desktop paths
remain unchanged.

It also does not replace the current mobile workflows, Fastlane lanes, private
configuration scripts, branch protections, candidate manifests, or Store
receipts during the pilot. They are retained as the proven fallback and as
evidence for comparing a shared candidate.

## Safe activation sequence

1. Keep every caller and the schema URL pinned to immutable commit
   `7eebb2656d28df33e4d8e5135f1f8fb64404e2bd`; never consume `main`.
2. Merge the pilot through protected `main`, then promote the identical reviewed
   tree to protected `testing`. Keep the divergent `release` branch dormant
   until a later Production-readiness change; no Production caller is present.
   The retained legacy Testing Candidate remains manually dispatchable, while
   its push trigger stays gated by `LEGACY_TESTING_RELEASE_ON_PUSH`; keep that
   variable unset during the shared pilot so one branch sync cannot upload the
   same build through both systems.
3. Add an owned Google Group to the Play `alpha` track and rerun the read-only
   Store preflight until it records API-visible assignment proof.
4. Run one protected Android testing candidate and verify the Play read-back
   receipt for the exact signed AAB. Do not promote it to production.
5. Promote the exact successful internal build to `alpha` without rebuilding,
   after validating the candidate receipt and required environment review.
6. Validate iOS independently, then run one protected TestFlight candidate and
   verify the exact Store build read-back.
7. Only after successful parallel releases should a separate reviewed change
   remove superseded mobile workflow logic. Desktop automation remains out of
   scope.
