# Pending Room fixture text-payload narrowing — independent source assessment

Reviewer `/root/editor_review`, 2026-09-10; hypothesis/prospective source owner `/root`.
**Recommend the small editable-only payload narrowing as risk reduction, not a demonstrated
Room NPE fix. No source edit, exact after-patch approval or execution admission in this note.**

## Inputs

- `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt`:
  SHA256 `a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9`, 36660B/712LF.
  Focused reads238–272/426–458/515–595/639–681; file-local searches for text consumers,
  Ax construction and selectors. Exactly two `.text` consumers occur at258/442.
- `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`:
  SHA256 `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`,
  relevant accepted snapshot421–452 and editable selector543–545.
- Prior independent Main note under this directory's `navhost-draft/`:
  `MAIN-NAVHOST-EDITABLE-TEXT-ONLY-INDEPENDENT.md`, SHA256
  `a0d82c94eedfa710bf8e3ba2c21fee97c0f8c8d5d817485354e008149d6727e5`;
  retained provider/consumer distinctions and sibling-evidence qualification44–76.

Whole-file hashes identify inputs, not whole-file semantic review. No Git/current commit
identity, new runtime result or whole-ledger review was obtained.

## Reachability, guards and preservation

Room find()525–528 builds the complete snapshot before applying its selector. The traversal
visits every node of each showing owned window, including non-editable nodes, with identity,
depth40, node/child2048 and owned-window8 bounds. Its unconditional accessibleText read564–568
calls charCount before the0..256 assertion and calls getAtIndex for each character. Neither
the geometry-only IllegalComponentStateException catch nor tree bounds protect that payload
read from a provider exception. This is the same unnecessary broad-read shape as old Main,
not evidence that a particular Room provider actually throws.

The only text values consumed are the Title assertion258 and replaceText's result assertion442.
Both targets use textField(), whose isNamedField671 requires editable. Other selection and
assertions use names/roles/parents/focus/check/enabled/window/geometry, not non-editable payloads.
Thus labels/dialogs still need their metadata and traversal, but not their AccessibleText
character payload. Snapshot failure before selecting an editable target is source-reachable.

The narrow Main32c pattern is suitable here: capture accessibleEditableText once; use that
same optional provider for the existing bounded payload read and the Ax.editable flag. Keep
all text equality assertions, the0..256 bound/message, getAtIndex/append behavior, metadata,
children/owner traversal and all native input/cleanup controls unchanged. No exception catch,
empty-value fallback, generic retry or selector relaxation is justified. The only intentional
scope reduction is that unused non-editable text no longer gets a character payload/bound check.
AccessibleEditableText supplies the AccessibleText API; this does not prove that arbitrary
providers return identical objects/content from both getters, or make their internals atomic.

## Counterexamples and remaining evidence

- Comparable prior GUI02/sibling success remains successful evidence, not a failure precedent.
  Integration02's Room case was unstarted; **no Room NPE has been observed here**. The hypothesis
  does not identify Main's failing node or imply every broad snapshot must fail.
- A bad/stale/disposed editable provider can still fail charCount/getAtIndex. Remaining name,
  role or other metadata getters can also fail; an editable-only gate is not universal safety.
- Nullable getAtIndex append behavior stays as written. Equality checks are not a general
  malformed-provider validator. No test/platform/library/product defect is newly confirmed.
- Parent Main's own correction still needs its distinct admitted runtime result. This source
  analogy neither supplies that result nor authorizes a Room run or replays GUI02.

Root should bind any eventual one-hunk adoption to its exact afterhash and preserve this
risk-reduction label. Zero new tests, executed tasks, cases, findings, fixes or closures here.
Only static reads and this exclusive0600, fsynced/read-back note; no runtime/generated outputs,
workers, helpers, Git/network, live process/SDK/cache probes or product/fixture edits. Existing
T/Detekt01 freeze, STOP/NO-RETRY/CLOSED/HOLD/consumed scopes including PVU-007/PVU-011/PVA-029
and G7/G8, and sole-root build/cleanup/publication authority remain unchanged.
