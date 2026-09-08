# PVA-007 freshness variant — independent finding review

Reviewer: `/root/editor_review`; originator/patch author: `/root/editor`.
Date: 2026-09-08. **Source-confirmed existing unfinished PVA-007 scope; no new
family, qualified closure, application execution or delivered-input claim.**

## Authority and identities

Read `AUDIT_HANDOFF.md`, START_HERE/PERMISSIONS/ASSEMBLY, published-payload
qualifications, root AGENTS, continuation README/OWNERSHIP, PVA-007's current row
and the supplied cross-platform Back/navigation-verification/Nav3/iOS navigation
skills. Frozen evidence, STOP/NO-RETRY/CLOSED fences and all eight PVD owner
decisions remain unchanged. No archived helper was executed or imported.

Baseline commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`. `BASELINE-INPUTS.json` binds the exact
source bytes and inclusive bounded read ranges. The originator began the
authorized correction while the manifest was being assembled; the two false
current-equals-baseline fields honestly record that fact. These ranges refer to
the handoff before-images read earlier, not to the changing after-images. Hashes
and whole-file lengths do not confer unlisted full-file review credit.

## Attempted disproof and reachable source schedule

1. A loaded, non-busy credential editor with an unchanged inline draft is a
   valid clean state: `hasUnsavedChanges` compares each draft to its field
   (CredentialViewModel 479–487). The custom-field editor is enabled via
   `canSave`; merely beginning an unchanged draft does not mark it dirty
   (CredentialFormSupport 197–215; CustomFieldsEditor 89–98).
2. Credential entries collect the ViewModel state and register the *composed*
   `credentialBackDisposition`. `BackRegistration` stores a value disposition
   and forward-blocking flag; a replacement is published only by `SideEffect`
   (VaultRouteAdapters 99–149/215–238; NavigationBackCoordinator 20–25/132–156).
   A previously registered clean editor therefore has `PopNow`/false.
3. A focused inline text change calls `OnCustomFieldDraftChanged`; the public
   ViewModel forwards synchronously, the busy guard accepts the non-busy edit,
   and `changeDraft` synchronously replaces StateFlow state. A changed name or
   value makes `hasUnsavedChanges` true before the callback returns
   (CustomFieldsEditor 132–138/173–215; CredentialViewModel 204;
   CredentialEventRouter 32–43/152–160; CredentialFormSupport 217–230).
4. A subsequent delivered Escape key-down may reach the already-composed host
   preview handler before the next composition frame. Desktop's outer window
   and content shortcut handlers do not consume Escape (KeyboardShortcuts
   31–89; PassVaultDesktopWindow 130–142/383–395). The host calls the coordinator
   directly, not the credential's ViewModel Back handler
   (PassVaultNavigationHost 229–236/285–286).
5. The old registration is present and still belongs to the current route,
   session, root and tab; the resumed host and token guards pass. The conservative
   missing-registration guard is not involved. The coordinator reads the old
   `PopNow`, invokes the default no-op `beforePop`, and calls `popAfterGuard`.
   The latter validates identity/lifecycle but does not reread credential state
   (NavigationBackCoordinator 34–69; AppNavigator 164–169/332–343). The entry is
   logically removed without a discard confirmation despite the accepted edit.

**Framework counterexample explicitly checked:** It would disprove step 4 if
Desktop key dispatch necessarily completed recomposition first. Official Maven
source for the pinned `org.jetbrains.compose.ui:ui-desktop:1.11.1` does not impose
that boundary. `BaseComposeScene.sendKeyEvent` dispatches input then flushes
scheduled *effects*; `render` separately flushes recomposer work and sends the
frame. `postponeInvalidation` does send snapshot notifications/owner snapshot
commands, but that is not a composition-frame barrier. `RootNodeOwner` delegates
to the focus owner's key dispatch before fallback focus handling. Exact compact
excerpts, URL, member hashes and archive hash are retained in
`FRAMEWORK-SOURCE.json`. The 1,032,410-byte archive was read only in process memory
and not kept as a cache. This is same-coordinate dependency source, **not a
bytecode/source correspondence proof or a runtime test**.

## Counterexamples and bounded variants

- **Toolbar Cancel/Back is not the failing route.** It invokes `OnCancelClick`
  and VM `requestLeave`, which reads current state and opens confirmation
  (CredentialEditScreen 146–158; CredentialEventRouter 184–186/232–247).
- Busy edit rejection, a missing registration, an inactive host, a changed
  route/tab/session, or a completed recomposition with the new registration each
  negates the witness. None is required for the ordinary clean-to-dirty focused
  edit followed by an input before the next frame.
- The same cached pair controls the shell's forward gate. Same-tab reselection
  reaches `popMainToRoot` without another dirty check: the Home dock remains
  selectable even when selected (VaultTab 27–37; VaultScreen 353–445;
  PassVaultNavigationHost 242–246/272–274; AppNavigator 206–218). Switching to a
  different tab ordinarily retains that tab's history; it is **not by itself**
  evidence of discarded data. Existing Add/forward eligibility is likewise
  affected, but no stronger persisted-storage-loss claim is made.
- `completeInteractivePop` also trusts the old disposition. A live completion
  check can protect mutation; it cannot by itself revoke a previous entry already
  supplied to a platform's interactive renderer before composition updates.
  Rendered predictive/interactive eligibility remains separate and unverified.
- No held-swipe PVU-004 procedure, physical iOS/Android input, personal clipboard,
  vault, backup or native application was accessed or exercised.

## Disposition and patch-proposal challenge

The current issue-to-fix ledger already explicitly requires
“edit→Back/tab/forward before SideEffect registration.” This is therefore the
unfinished PVA-007 variant, **not** a new confirmed family. Historical VM/logical
passes and the separate PVA-026 equal-owner correction remain valid within their
stated boundaries; they never claimed this timing path was verified.

The proposed optional live policy pair is appropriately bounded for the action
boundary: resolve current disposition and forward blocking from one VM state
read, only after selecting a valid active registration; retain existing composed
values for invalidation, defaults, token/lifecycle checks and registration disposal.
Credential-only wiring avoids broad product-policy redesign. Read the live pair
once per action. Independently review the actual after-image before acceptance.

Meaningful new regression should use production ViewModel and the real Compose
registration wrapper with composition held after an accepted edit, then exercise
coordinator Back, forward and completion boundaries. Include clean positive,
busy, missing/stale registration and lifecycle controls. A generic stub callback
or hand-constructed fresh policy alone cannot catch an adapter wiring regression.
No test has been executed by this reviewer. Even such a composed-registration
regression is **not** a rendered NavDisplay, delivered OS input/IME, physical
gesture, persistence or full PVA-007 closure.

## Resource and inspection qualifications

All reviewer commands were short source/metadata reads or bounded report writes;
no Gradle, build/test, compiler, app, server, emulator or persistent worker was
launched. No wrapper-stop obligation arose for these reads, and no shared cache
was deleted. Required compact reports are retained; temporary archive data died
with each source-reading process. At 20:46:31 UTC `df -Pk .` observed 15,815,904
KiB available and `free -m` observed 34,892 MiB available; these are point
observations, not build admission or attribution of freed space to this agent.

Two guessed shell-source locations did not exist; source search corrected the
locator to `shared/.../VaultTab.kt` and `feature/vault/.../VaultScreen.kt`.
Some grep searches returned no match/exit 1. These were inspection failures,
not application tests. The first manifest range end for the 101-line
`shared/build.gradle.kts` was corrected from requested display endpoint 120 to
actual EOF 101 before review publication; no source coverage beyond EOF is claimed.
