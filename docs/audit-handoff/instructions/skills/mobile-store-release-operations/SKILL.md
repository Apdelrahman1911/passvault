---
name: mobile-store-release-operations
description: Inspect, prepare, automate, or audit TestFlight, App Store, Google Play internal/closed/open testing, production promotion, store metadata, tester files, screenshots, privacy and content declarations, and review state. Use before mobile store uploads or promotions and whenever manifests, permissions, SDKs, data flows, authentication, metadata, testers, or store assets change.
---

# Mobile Store Release Operations

Automate what APIs can prove and leave owner/legal attestations explicitly manual.

Also load `$mobile-app-identities-and-services` for package/bundle/service identity, `$native-release-signing-and-packaging` for final binary proof, and `$release-provenance-and-promotion` for build-once promotion and receipts.

## Authority boundary

Do not upload, promote, submit for review, withdraw a version, change pricing, or publish merely because this Skill was invoked. Inspect first; request the minimum explicit authorization at the protected action.

Testing tracks and production are distribution stages of one Store application identity. Do not create a testing application ID to model a track.

## Inspection workflow

Derive claims from the final release artifact and source:

- merged Android manifest, iOS entitlements/privacy manifest, permissions, URL schemes, and associated domains;
- network clients, endpoints, SDKs, analytics, ads, crash reporting, billing, authentication, account deletion, backups, exports, and user-selected providers;
- actual collection, sharing, encryption, retention, deletion, and off-device transfer;
- store IDs, languages, descriptions, release notes, contact/privacy/support URLs, categories, ratings, pricing, countries, screenshots, and feature graphics;
- internal/external tester files, groups, track state, review state, and production eligibility;
- CI environments, service-account/API-key permissions, and exact build receipts.

Do not answer a store questionnaire from documentation alone.

## Remediation workflow

1. Create a source-backed machine-readable declaration record with evidence paths and last-reviewed date.
2. Render Store metadata from validated private/publisher inputs; never invent contacts or legal answers.
3. Validate tester files locally without printing private identities. Use `scripts/validate_tester_files.py`.
4. Validate screenshot dimensions, formats, locale coverage, fictional data, and safe content. Require human visual approval.
5. Upload the signed release once to internal testing and record its Store build ID and artifact receipt.
6. Promote that build to external testing and later production through protected environments.
7. Read back Store state after every mutation. Distinguish uploaded, processed, available to testers, submitted, in review, approved, and live.
8. Keep a console-only checklist for agreements, legal declarations, tester membership, ratings, pricing, eligibility, and owner confirmations unavailable through APIs.

## Required verification

- Compare declarations against final permissions, bundled SDKs, dependency graph, and network behavior.
- Validate every metadata locale, placeholder, length, URL, image dimension, and required asset.
- Test malformed/duplicate/empty tester rows without disclosing addresses.
- Run a no-publication workflow mode and verify it cannot mutate Store state.
- Verify internal upload and later promotion reference the same build number/version and receipt.
- Verify API identities are least-privilege and scoped to protected environments.
- Record manual Console evidence separately; do not represent a checkbox as API proof.

## Failure lessons

- Store APIs cannot truthfully complete all legal/content declarations.
- A package name shown as an unreviewed temporary title is not the approved listing.
- Upload completion is not review submission or public release.
- Tester-file syntax validation cannot prove Console group membership.
- Apple and Google review are asynchronous; cross-store publication is not atomic.
- Withdrawing a submitted version is an external destructive action requiring explicit approval.

## Resources

- Read [`references/store-responsibility-matrix.md`](references/store-responsibility-matrix.md) before deciding what can be automated.
- Run [`scripts/validate_tester_files.py`](scripts/validate_tester_files.py) on email-list or CSV tester inputs.
