# Lifecycle and native-tray diagnostics — independent patch review

Reviewer `/root/desktop_other_review`, 2026-09-11; author
`/root/desktop_other_author`.

**ACCEPT THE TWO EXACT UNAPPLIED PROPOSALS BELOW FOR SOURCE INTEGRATION ONLY,
SUBJECT TO ROOT'S EXPLICIT FREEZE RELEASE.** Not application, compilation,
Detekt success, runtime execution, source-index rebinding or execution admission.
No product closure or broader correctness claim. Curtain review remains separate.

The full baseline/guard/evidence review is `01-BASELINE-SEMANTICS.md`, SHA-256
`5dedbee39866c571a09a6a68340daeba643c3167feafe581f1fa01aac6b9bad9`.
Read every hunk against the three-fixture/production context already reviewed;
used independent standard-library text reconstruction only, in memory. Exact
hunk context, original/patch/result hashes and lengths agree; original source
files remained unchanged. No author helper/code was executed or imported.

## Accepted exact identities

Patch paths relative to `B/reviews/team20/desktop_other_author/`; source paths
relative to W.

| Proposal | Patch SHA-256 | Original source SHA-256 | In-memory after SHA-256 / bytes |
| --- | --- | --- | --- |
| `01-lifecycle-unapplied.patch.txt` | `25ba7c3d136b1cb93998e4771de2b04a0df94980186c95cb844ffe12e19d5535` | `422cbfd7b85a08585b99519b94f41296ed24ea1dce133f47f4fdab2ddf5a85b9` | `0b40e53ceceaf11e6cbc5baaeb4be5f771bb17d939f2c7c2ae54fbb19ab00275` / 9949 |
| `02-tray-v2-unapplied.patch.txt` | `c4dafe58a516fd1a589eddc777cd32a1dbf8a82d07b4eb0098917377768285e6` | `0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec` | `a0a212aec3cd5a78432feeaf2d8e3cc4b5c14a23dd5776029de15106287fb7ef` / 27327 |

These touch only:

- `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopApplicationLifecycleIntegrationTest.kt`
- `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/DesktopSystemTrayNativeIntegrationTest.kt`

## Lifecycle: accepted rationale

The original LongMethod diagnostic covers `runChild` line72. Its environment
map/XDG-directory setup is moved intact to `childEnvironment`, invoked at the
same point after home/tmp/trace/log creation and before ProcessBuilder creation.
The map is only consumed through `putAll`; returning it as `Map` instead of the
local mutable-map inferred type changes no use. Environment clearing, isolation,
child argv, captured process, bounds, wait, exit evidence and all finally logic
remain unchanged. The entire child probe and default/production event/exit
oracles are byte-identical.

**Precisely accept `ThrowingExceptionFromFinally` suppression on `runChild`**
alongside its existing `TooGenericExceptionCaught` annotation. Original line138
throws only when there is no primary failure; otherwise it adds the cleanup
failure as suppressed. Cleanup-only failure must not turn into success. The
annotation explains that policy and changes no catch/error/interruption behavior.
No file/class suppression or cleanup-lifetime rewrite is introduced.

## Tray: accepted rationale

- Screenshot extraction retains its position after popup selection and before
  Enter. It preserves crop bounds, image ownership, all finally flushes, the
  `CREATE_NEW` evidence path and force-before-image-stream-closes-original-channel
  requirement. Caller still flushes `selected` even if argument/helper work fails.
- `popupBounds` keeps the null/dimension guard first; rejection
  `count < 500 || count < area/3` becomes the equivalent positive conjunction.
  Proximity remains short-circuited behind density. Thus zero/sparse/tooltip/
  far-away deltas still fail, and integer products remain within existing bounds.
- **Precisely accept generic catch suppression on the single test method**:
  it preserves an assertion/error/cancellation as primary before invoking close.
  **Accept both generic-catch and finally-throw annotations on `nativeInput`**:
  release registration precedes press, every partial press attempts release,
  failed release stays registered, a press failure retains priority, and an
  otherwise-successful press cannot hide release-only failure. Its body is
  byte-identical. **Accept generic-catch annotation on local `release` only**:
  it must retain each failure, attempt later independent cleanup and preserve
  interruption, including assertion/errors not covered by `Exception`.
- Native cleanup and trace-writing helpers are cohesive private steps, not new
  owners. The original `close` still owns first/suppressed failure and interrupt
  collection. It passes the same synchronous `release` closure to both helpers;
  native input/observer/original image/tray/removal verification/frame/disposal/
  marker-flush ordering remains exact. ImageIO and every Locale category are
  restored and asserted before trace flags are read. Marker flush still depends
  on verified removal, not merely `tray.cleanup()` returning.
- EDT helper cancellation occurs on every unsuccessful `task.get` exit via a
  completion flag/finally; the flag is set only after successful get. Successful
  get does not cancel. Failure still escapes unchanged, `cancel(false)` still
  avoids interrupting native code, and already-running timed-out work remains
  unsettled until root establishes settlement. No catch narrowing or failure
  masking is introduced by this ordinary control-flow transformation.
- Admission/private-directory/evidence-write functions, nativeInput body,
  production tray, test name, languages, icon/menu identity checks, native input
  ordering, callback/no-extra-action oracles and all evidence destinations remain.

## V1 rejection retained: live cleanup flags are not an early snapshot

Do **not** apply `02-tray-unapplied.patch.txt`, SHA
`0d12ebd67431574e0a65e632e45da6cdae213b11bf81cfdf222659d15637c1ed`
(in-memory after `3a730d5e9ae4feb44ea8b0975de470e4125d2fcc35172fdf5bc408b3800c32b2`).
That first proposal returned `Pair(removed, disposed)` immediately after native
cleanup. Original local captured flags were read later, after Locale/cache
restoration. A timed-out already-running EDT task may finish in between and set
one flag: v1 could freeze an earlier false value. This was a source counterexample
to promised evidence equivalence, **not an executed defect or new PVA family**.

V2 retains a shared `NativeCleanupState` at the original variable-initialization
point. The actual EDT lambdas mutate that same live state; marker flush reads it
at the original point, and observations read live fields after defaults cleanup.
This resolves the reviewed early-snapshot issue. It does not add a memory-visibility
or hard-settlement guarantee for timed-out native work; original qualifications
remain. V1 bytes remain preserved as superseded/unapplied evidence.

## Validation and limits

These proposals address **11 retained diagnostics** (lifecycle2 + tray9), not
11 product findings/test cases. Actual Detekt results for the after-images remain
unrun; text lengths/rule reasoning are not a substituted analyzer pass. Function
signatures/references/annotation scopes were inspected for Kotlin compatibility,
but no compiler was invoked and no new permanent tests were added.

Smallest next validation, only after root's freeze reconciliation and new exact
admission: include `:app-desktop:detekt` with its complete intended Desktop source
set in the focused static candidate, unchanged serial/JDK17/wrapper/dependency
policy. If a concrete compiler/static problem appears, report and scope that
failure; do not use it to replay consumed work automatically.

Reuse the independently supported integration02 lifecycle/tray PASS evidence in
its **original exact-source and behavioral scope**. This review supports a
semantics-preserving source transition; it does not relabel old XML as execution
of these after-images. No successful endpoint GUI rerun or broad all22 rerun is
justified by these edits alone. Full Main/provider/Settings/tooltip/actions and
physical/platform gaps remain separate. Source/helpers/T remain frozen; only
review reports were written, with no Git/runtime/CI/build/test/helper activity.
