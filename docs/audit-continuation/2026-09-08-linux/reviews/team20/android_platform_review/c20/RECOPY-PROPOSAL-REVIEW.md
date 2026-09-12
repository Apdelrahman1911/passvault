# C20 clipboard recopy witness — independent proposal review

2026-09-11; reviewer `/root/android_platform_review`; author
`/root/android_platform_author`. B = `docs/audit-continuation/2026-09-08-linux`.

**ACCEPT exact inert proposal for optional root adoption as one focused host-fake
coverage addition.** It is worthwhile because it distinguishes a previously
uncovered service-owned recopy sequence. This is not a demonstrated production
defect, required framework-fixture correction, executed regression, target
admission or PVA-009/PVA-030 closure. Root owns the adoption/priority decision.

## Exact inputs and passive text checks

All source/proposal text was read without import, compilation or execution.

| Input | SHA-256 |
|---|---|
| W `app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt`, unchanged preimage | `b4a69761ef3f6920ee586f070b5a22a7d552151850bce2718e1a7c06b5c91b64` |
| W `app-android/src/main/kotlin/com/passvault/android/security/AndroidClipboardService.kt` | `db1b9e5387e7059719e0f49b4a8c9340f8abcf210d16a808a5408c17443e00cf` |
| B `reviews/team20/android_platform_author/c20/CLIPBOARD-RECOPY-WITNESS.UNAPPLIED.patch.txt` | `d7caec3abd0c13fd813dd7c8c29e2c7203fa2c52c8c60d64760efcf2f8fed3a3` |
| Same directory `CLIPBOARD-RECOPY-WITNESS.md` | `ab189463dd931bf18639d5cbd1e2dc9dff699f62542bc32afa74a20c91deb4a3` |
| Same directory `FRAMEWORK-MINIMUM-AND-BLOCKER.md` | `46b6e13be096b6e7fd6227f9b1d306e8b4852def51df1ac148a77fb3299d441f` |

The sole hunk matches preimage lines106–111 and inserts **40 LF** before line109.
The existing six methods, helper, fake provider, imports and production are
unchanged. Independent `awk` literal-line extraction/insertion was piped only to
hash/count readers, not applied or interpreted as Kotlin. The prospective
after-image is **190 LF / 6,633 bytes**, SHA-256
`ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332`.
The actual source was rebound afterward and still matched the preimage.
This is a computed after-image, not an adopted/compiled source receipt.

## Independent oracle and surrounding-code challenge

Proposed method:
`newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry`.

The existing six host methods were reread in full. They cover unavailable expiry,
unavailable query, external-label replacement, unlabelled replacement, provider
clear failure and unexpired foreground. None performs a second **owned sensitive
copy** after an unavailable explicit clear has set the first copy's pending flag.
External replacement is not equivalent: recopy authorizes a new token and timer.

| Sequence/control | Independent disposition |
|---|---|
| A has one registered deferred expiry; reads become unavailable; explicit clear produces zero provider clears and retains ownership | Establishes the relevant pending-clear path without firing A's timer. Uses the actual service's clear boundary; no private-state assertion. |
| B receives a different nonnull token and a second registered deferred gate | Reusing the old helper's single completed gate is avoided. No existing helper/method rewrite is necessary. |
| Readability returns; A's gate is signalled; foreground runs; zero clear calls and exact B token/text are checked before `containsSensitive` | A wrong readable-owner query cannot silently retire authority before the preservation oracle. The tuple and clear-count checks discriminate destructive behavior directly. |
| Only B's gate is then signalled; exactly one clear, empty text and no ownership are required; another foreground must not clear | Distinguishes missing B timer or reuse of the predecessor's expiry. This is manually scheduled fake expiry, not elapsed OS-time evidence. |

Source-level negative control, **not executed or patched**: remove only the
`clearRequested = false` reset from `copySensitive`. A's unavailable clear sets
the flag; B inherits it; readable foreground now clears B before its own gate,
violating the zero-clear and B-tuple assertions. None of the six existing methods
has that two-owned-copy setup. Current production already contains the reset;
there is no new supported product defect.

The service's mutex, token replacement, job cancellation and token-equality
guard were inspected together. Removing old-job cancellation alone need not
fail this method: a resumed A callback can still be rejected by its expected-token
guard. Conversely normal cancellation can prevent A's callback from running at
all. Therefore this test does **not** independently prove the cancellation line,
force a stale callback, exercise noncooperative cancellation or establish actual
concurrent contention. The author's explicit narrower wording is required.

The two deferred gates and `Dispatchers.Unconfined` preserve the existing host
test strategy: fake provider operations are synchronous; only gate waits suspend.
The gate-registration assertions expose a changed scheduling assumption instead
of adding sleeps or polling. Do not silently transfer those ordering claims to
another dispatcher. `finally` cancels the test-owned scope, not the Android app,
device or framework; no such external settlement claim is made. Compilation and
actual execution remain unobserved, including any changed Detekt qualification.

No import, dependency, suppression, generic runner, new framework case or
production adjustment is needed. If root adopts this test, its changed file is
new source for affected compile/static/regression qualification; prior unchanged-
source results cannot automatically certify it. No separate execution campaign
or replay of the successful six methods is requested by this review. A future
root-admitted affected host selection may include the one new method.

## Paired framework/target disposition

The author's framework feasibility note agrees with this lane's separate
`FRAMEWORK-SOURCE-REVIEW.md`: retain the accepted two fixed cases and all startup,
null-read/lifecycle, config-vs-device, store-job and aggregate/outer-cleanup limits.
No weaker AX fallback, one-case filtering under a two-case inventory, manual
foreground callback, direct locale publisher or new outer harness is justified.

Independently read only the cited **retained metadata** excerpts, not any SDK,
image, emulator, target or held runtime:

- B `reviews/android32/REPORT.md:135–157,211–229`, SHA-256
  `b800ecf4e48dc51eee34ca5bfb6ede09d55ac2b9fa7d555910fd7d1c3fd660e5`.
- B `reviews/android32/ANDROID32-ADMISSION-PREREQUISITES.md:60–82`, SHA-256
  `5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089`.

The retained API35/x86_64 declaration meets API29+ only as metadata, not evidence
of a presently usable/admitted guest. The historical absent-KVM observation
proves neither current impossibility nor software-emulation viability. Existing
license-marker metadata does not establish the principal's agreement for exact
component/use, but also does not itself prove a mismatch or require accepting
new terms. Root should resolve existing authorized coverage first. No license
acceptance command, download, target launch or account operation follows.

These excerpts are used solely for this lane's API29+ admission feasibility, not
to redo the separate Android32 KDF/ABI work. No new target/rights/isolation facts
were supplied; the genuine block remains. Crypto compile03 success is retained
without repetition and is not app-android fixture compilation or device proof.

## Author activity qualification

The later author disclosure `reviews/team20/android_platform_author/c20/AUTHOR-SCOPE-NOTE.md`
(B-relative), SHA-256 `c804aec0a6e7aa9f1b866e0107cb932f5dfa7afa0bddb007b3042f3d42a4d232`,
records an unnecessary metadata-only skills-directory listing outside W, with
no entries or file-content reads. Root was informed. This reviewer read the
disclosure only and did not inspect that external directory or replay the lookup.
The source/oracle acceptance above is not a claim of perfect author path-scope
adherence, target evidence from that lookup or authority to expand the task.

## Accounting and boundaries

One **inert proposed method**, zero application to W, zero executed methods,
zero production fixes/findings/closures at this review. The existing two framework
cases remain unexecuted/unadmitted; PVA-009/PVA-030 remain target-runtime-blocked.
The current six permanent host methods remain unchanged until root acts.

Only this reviewer-owned report was written. No central/source/runner edit, Git,
build/test, CI/network, SDK/ADB/app/native operation, helper import/execution,
process/held-runtime probe or cleanup was performed. No personal clipboard,
device, account, credential, vault or backup data was accessed. No disposable
runtime/background worker was created; no wrapper stop obligation is added.

Preserve PVU-007 STOP, PVU-011 NO RETRY, PVA-029 49/44/5 FAIL with no automatic
retry, G7/G8 CLOSED, native refusal, all consumed/HOLDs, protected refs,
build1017001, dependency/version/identity restrictions and PVD/hardware/account/
license/Store/publication boundaries. Root remains sole build/adoption/Git/CI/
cleanup owner.
