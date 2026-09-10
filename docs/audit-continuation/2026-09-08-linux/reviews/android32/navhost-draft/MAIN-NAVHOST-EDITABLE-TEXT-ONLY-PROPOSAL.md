# Main/NavHost integration02 — editable-text-only fixture proposal

Author: `/root/android32`. Independent challenger: `/root/editor_review`.
**Inert source proposal only.** Root's active-input freeze is intact. No permanent
fixture, build input, runner, dependency, product source or central ledger was edited.
No new build/test/runtime operation, cache/SDK/old-runtime access, import, Git operation
or process probe occurred. The consumed integration02 run is not authorized for retry.

## Exact source and proposal identities

Permanent frozen source:
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`
SHA-256 `7a0dd50d7caa6cf3cfad729f7393a0e1802fbf65b57d3cceae7c5c64ac94961f`,
40,423 bytes / 753 LF. This source still has the accepted private-bus and one-EDT-block
Ctrl+Q corrections; neither is changed by this proposal.

Inert exact patch in this directory: `MAIN-NAVHOST-EDITABLE-TEXT-ONLY.patch`,
SHA-256 `7f883bdbb31478d2b1fa9d5a8dfad7bae148c84c3ec5904fa57e21f87a8ff046`,
1,856 bytes / 24 LF. Prospective transformed source (not written/applied/compiled):
SHA-256 `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`,
40,561 bytes / 755 LF. A bounded two-marker text transformation streamed only into
hash/line-count utilities; the permanent file's frozen hash was rechecked unchanged.

## Retained actual failure and reachability

Evidence base: `../../../runs/linux-desktop-integration02/` relative to this note;
full repository base is `docs/audit-continuation/2026-09-08-linux/runs/linux-desktop-integration02/`.

| Retained input | SHA-256 | Observation |
|---|---|---|
| `mainnav-evidence/main.log` | `5435f264d33ed527a3b5162757a94435f5c1ef6abd48b2ba90d22e13b1a55797` | NPE in Compose accessibility char-count read |
| `mainnav-evidence/main.events` | `754fa55f77c58e383fb54678a2318a181c76767995971876fe67d16e68fe500d` | Only `DRIVER_FAILURE` |
| `mainnav-evidence/main.exit` | `e3b9c2844b5a5c2677b3a2279db2ec8487491dd9a23d6b22fac153391b3bb63c` | `137` |
| `mainnav-evidence/seed.events` | `301dbb801ed4dca09f62a056917b63f5a0184680976de4e362b1eff88798082e` | `SEED_ORIGINAL_TUPLES`, `SEED_ROOM_CLOSED` |
| `mainnav-evidence/seed.exit` | `9a271f2a916b0b6ee6cecb2426f0b3206ef074578be55d9bc94f6f3fe3ab86aa` | `0` |
| `xml/app-desktop--TEST-com.passvault.desktop.CredentialMainNavHostRoomIntegrationTest.xml` | `c3e7b3f0aa2afa4468915381d20972a2e04098db617d8453de253fb4e5a1402d` | One case, one failure, no skips; `MAIN_NAV_DRIVER_FAILED` |

The child stack reaches `NativeMainNav.scenario` line 291 via `onEdt`, then
`snapshot$visit` line 432 and `ComposeAccessible$ComposeAccessibleComponent$ComposeAccessibleText.getCharCount`
at dependency-source line 589. The failing `FutureTask` propagates its NPE through
`ExecutionException`; nothing turns it into a missing-node success. Line 291 is the
first `credentialCard(snapshot())` await after native password-entry and Unlock-button
helpers returned. This proves those driver calls preceded the failure, **not that
unlock succeeded**. It precedes credential-card selection/edit entry and every recorded
dirty Back/Escape/tab/Add assertion. No `MAIN_NATIVE_EDIT` or Main-success receipt
exists. The verify role never started; seed success is not final reopened durability
evidence and the three roles/events are not three executed test cases.

The driver's catch records `DRIVER_FAILURE` while actual Main continues owning its
event loop. XML specifically locates the parent assertion at `runRole` line 139,
inside the still-running-child polling loop. That failure then enters `finally`,
which forcibly destroys a captured live child and waits up to five seconds before
writing its exit receipt. Exit 137 is consistent with this documented force-containment
path; it is **not independent OOM evidence, clean Main shutdown, or proof of the exact
signal issuer without a signal receipt**. No Quit-success event was reached. The
SKIKO GL-fallback warning also does not establish the NPE's cause or a product defect.

No offending accessibility node's identity, role, name, editable interface or current
semantics was logged. A non-text node exposing an unusable text wrapper is a plausible
explanation, **not proven dependency root cause**. A stale/temporarily absent text
layout, including an editable node, remains a counterexample. No dependency source
or cache was accessed to fill that gap by assumption.

## Smallest correction and retained assertions

Capture `context.accessibleEditableText` once; use that `AccessibleText` subtype for
the bounded text-value read and the same captured value for the `editable` flag.
Do not request text payloads from arbitrary non-editable navigation nodes. Keep every
role/name/action/state, owner/window, geometry, identity/depth/node/child bound, EDT
dispatch, exception propagation, ordered event and parent/child cleanup requirement.
The patch adds no catches, defaults, retries, fallback observations or product hooks.

This is justified by the fixture's actual consumers, not a guessed dependency role:
`readText(label)` selects `textField(label)`; readable `replaceText` uses the same
selector. `textField` already requires `editable == true`. All other selection and
navigation checks consume metadata, not `MainNavAx.text`. Non-editable text payloads
were therefore irrelevant to the claimed scenario. Editable values retain the
`0..1024` length assertion, character reads and exact synthetic-value assertions.

Counterexamples the independent reviewer must preserve:

- A non-editable node may have text, null text or a throwing text wrapper. Its payload
  is no longer queried, but its role/name/actions/state and required navigation
  assertions are still observed. This is not a missing-node pass.
- A selected editable field whose reader throws still fails; this candidate does not
  claim to fix a stale/disposed editable layout. An absent editable interface cannot
  satisfy `textField`, so required-field discovery times out/fails rather than passes.
- Empty editable values remain `""`, not null. Oversized, wrong or malformed values
  still fail existing bounds/equality. Duplicate targets still fail uniqueness checks.
- Password entry remains `readable=false`; no newly asserted cleartext observation or
  password-security claim is added. Its editable interface is still required for
  focus/input selection. Disabled or unfocused fields do not become accepted inputs;
  existing click/focus/active-window checks remain.
- If a provider exposes an editable interface incorrectly on an unrelated node, the
  read can still fail. The proposal follows the public interface/consumer contract;
  it is not a claim all possible accessibility supplier failures have been solved.

The source-inspected sibling GUI fixtures use the **same broad old pattern**, not an
existing narrower guard: `CredentialEditorRenderingTest.kt` SHA-256
`6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a`, lines 501–510;
`CredentialEditorRoomIntegrationTest.kt` SHA-256
`a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9`, lines 564–573
(both under `shared/src/desktopTest/kotlin/com/passvault/shared/credential/`). Their
previously successful narrower-screen evidence is preserved, not rerun or generalized
to the full Main/NavHost topology. Neither sibling is edited by this bounded proposal.

## Adoption and evidence boundaries

Independent source acceptance and root's explicit freeze release must precede any
permanent application. Source acceptance would not restore integration02 admission,
authorize an automatic retry, or prove the corrected candidate passes. A fresh exact-
source, single-owner, bounded runtime/cleanup admission remains root's responsibility.
The one case, three serial roles and 13 required events stay unchanged. No closure
denominator, confirmed product family, hardware result, PVD design limitation or
STOP/NO-RETRY/CLOSED/held scope is changed. Only these small permanent review artifacts
were created; no application process, build output, cache or runtime temporary file
was created or removed by this lane.
