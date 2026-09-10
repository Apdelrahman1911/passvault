# PVA-007 real shared NavHost / Room complement — draft, not admission

Author: `/root/android32`, 2026-09-10. Root requested this bounded next-batch draft after
the current tray/lifecycle/Room three-case batch was frozen. **No module, build file,
runner, central ledger or frozen fixture was changed. No execution, import, build,
dependency inspection from held runtimes, Git, network, SDK or CI probe occurred.**

## Status and exact draft

`CredentialNavHostRoomIntegrationTest.kt` in this directory is **959 LF / 47,819 bytes**,
SHA-256 `e4778ee2415b89688834ecb72865ae7aa94dffff696dc05733284f17288fd2d5`.
It is not in a Gradle source set. Proposed eventual location is
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/`, subject to selection,
independent source challenge and new execution/coordination/cleanup admission.

There is **one prospective `@Test`, zero compilations and zero executed cases**.
Its thirteen ordered trace stages are not thirteen tests. No closure or denominator
change is claimed. The old logical/composition checks and GUI02 evidence remain intact;
this does not replay or recount them. The capacity/page-Save real-Room fixture remains
SHA-256 `a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9`.

## Concrete boundary and why this placement

One captured child JVM starts the real `GlobalContext.startKoin` graph from
`AppModule.getAllModules(desktopModule)`. It renders public `PassVaultApp()` inside
a real Compose `Window` and the production `runDesktopApplicationLoop`. Thus the
shared app owns its actual NavHost, Nav3 decorators, entry VMs, authentication/session
observers, effects and `NavigationBackCoordinator`; none is supplied by the test.

The production Desktop settings, backup and attachment adapters live in app-desktop,
not shared. This placement avoids fake platform ports or a new shared→desktop
dependency. No dependency or product seam was added. Standard lifecycle owner imports
still need resolution against the *admitted actual* compile classpath; do not silently
add dependency edges or consult held runtimes to make this draft compile.

Unlike a `ComposeWindow.setContent`-only fixture, `Window` is expected to supply the
real lifecycle/ViewModel-store environment used by production. The draft observes
those owners without installing replacement providers and requires actual RESUMED,
active native peer and renderer readiness. This is a **prospective API/runtime
contract**, not already-established evidence: missing owners or incompatible APIs
must fail/revise admission, never be replaced by a manually RESUMED lifecycle.

This is **not Main, PassVaultDesktopWindow, instance-lock, tray, curtain, keyboard-menu,
focus-loss auto-lock, production shutdown-coordinator or hardware security evidence**.
The wider outer-window contract is deliberately absent, not merely “hardware blocked.”

## The single distinguishing scenario

Setup uses only the same real Koin-bound bootstrap, libsodium, session and encrypted
credential repository. It seeds one synthetic SecureNote with two stable field tuples,
then performs an ordinary prelaunch manual lock without closing Room. Actual native
password input and Unlock must reach the real Home dock and credential detail/edit.

1. Native row Edit changes a pending field name/value, without row Save or page Save.
   Read-only repository observations must still return the original two tuples.
2. Native toolbar Back opens the real discard dialog. Native **Keep editing** must
   retain both pending input values and the actual editor, with Home still selected.
3. Native Escape exercises the real host/coordinator path and repeats the Stay oracle.
4. Native Settings dock selection while dirty must open confirmation, not select
   Settings. Keep editing again preserves the pending tuple.
5. Native Add credential while dirty must also be guarded. Native **Discard** must
   pop to the actual preceding detail, not silently replay Add into a new editor.
6. Positive controls then require clean Settings→Home to preserve the detail stack,
   and clean native Escape to return to the real credential list without confirmation.
7. After actual composition disposal and terminal cleanup, a distinct real bootstrap,
   Room object and repository reopen the same database inode. Real unlock/read must
   recover the exact original IDs/names/values/secret flags and sole credential ID.

Only native AWT mouse/key input changes UI state. Read-only accessibility supplies
unique target identity, clipping/geometry, selected Tab state, dialog and input-value
oracles. No `onEvent`, navigator, accessibility action, text mutator or captured leave
callback is called. A two-frame observer is only a rendering-progress barrier, never
a fabricated navigation/security acknowledgement. The actual shell is visible on all
MAIN routes, including editor. This complements steady-state full-host reachability;
it does **not** manufacture a before-recomposition race or all-tabs/restoration matrix.

## Storage, ordinary lock, terminal cleanup and resource contract

Before AWT/bootstrap/Preferences, the child requires Linux/JDK17/X11, exact synthetic
DISPLAY, English accessibility, fresh non-overlapping 0700 process-owned canonical
home/tmp/user-prefs/system-prefs directories and no prior `.passvault`. Java Preferences
roots and JVM home are passed at JVM creation, not changed after initialization.
Directory file-key receipts and bounded no-link paths complement—not establish—the
root's allocation authority. The parent supplies sanitized XDG directories and exact
Test runtime classpath; `java.class.path` is not substituted.

The graph necessarily resolves the real ClipboardService shutdown hook and attachment
lifecycle observer. No Copy/Paste, reveal, backup chooser, attachment chooser/viewer,
URL launch or recovery action occurs. With no owned clipboard transfer, production
clear/shutdown guards avoid OS clipboard access. Synthetic `.passvault` database,
attachment directories and possible preview-state paths are part of the wider output
admission; Java prefs/native temporary outputs also need allowlisting.

Ordinary seed lock is followed by actual native unlock. **Terminal** cleanup happens
only after exiting the application composition: release held input/windows; clear
the *observed real* Window store; clear known sensitive owners; cancel/join the five
real singleton VM jobs and app-scope job; separately attempt one manual vault lock,
clipboard clear, real preview purge, unavailable Linux biometric-host close, and one
checkpoint/Room close. Koin stop is limited to the captured graph identity. Reopen
is forbidden after any cleanup error. The second Room owner also has one lock and
checkpoint/close finalizer, not a close retry or preservation/reset path.

Entry decorators remain production-owned; this draft does not enumerate every nested
entry job or claim their individual joins. It observes composition disposal, clears
the actual window store, joins known root/app jobs and additionally requires owned
child process termination. This limitation needs independent challenge, not a claim
that Koin stop alone closes ViewModel scopes. Known unsettled driver or undisposed
composition withholds the database close rather than claiming safe concurrent teardown.

Child heap is 512 MiB / one active processor; parent hard deadline 240 seconds plus
five-second owned-child settlement. Driver deadline is 150 seconds; waits are finite,
cleanup coroutines nominally five seconds. Native/EDT/non-cancellable SQLite stalls
can exceed cooperative deadlines. Only the captured child can be forcibly terminated;
that produces failure, not graceful-cleanup evidence. Root must settle its X server,
wrapper/Test workers, run the correct wrapper `--stop`, and perform validated output
cleanup after preserving ≤256 KiB child log, ≤16 KiB trace, exit receipt and test XML.
The fixture deletes nothing. This source-only lane created no application/build/test
process or runtime artifact; only these two small permanent draft files were added.

## Independent challenge / next admission gates

Review the actual Window lifecycle/store imports, selected Tab accessibility mapping,
unique card/field selectors, root and entry ownership/settlement, ordinary versus
terminal lock, failed-start ownership, and interruption/primary/suppressed failures.
An unavailable native target/owner is a blocker, not permission to inject a callback.
Root must bind the eventual full source commit/tree/payload, exact one-case filter,
new JVM/property/classpath forwarding, sole build slot, fresh home/prefs/X allocation,
bounds and cleanup before any compile or test. No command in this note is admission.

Editor lane's separate PVU-003 observation design may reuse this future scaffold but
needs actual `PassVaultDesktopWindow` for its outer focus guards. This fixture adds no
PVU-003 case and does not claim a modal FileDialog interval permits Home input.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry and G7/G8 CLOSED
remain unchanged. Nothing here authorizes old runners/helpers or publication/recovery.

## Production source bytes inspected (not a whole-tree execution identity)

| Source | SHA-256 |
|---|---|
| `shared/.../PassVaultApp.kt` | `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` |
| `shared/.../navigation/PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| `shared/.../navigation/adapters/VaultRouteAdapters.kt` | `4a155566bd433f2905f6e13137d1b8e7b03da06a54fd9e12b706b4f7d3061b98` |
| `shared/.../di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `app-desktop/.../di/DesktopModule.kt` | `7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253` |
| `app-desktop/.../DesktopApplicationLoop.kt` | `fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac` |
