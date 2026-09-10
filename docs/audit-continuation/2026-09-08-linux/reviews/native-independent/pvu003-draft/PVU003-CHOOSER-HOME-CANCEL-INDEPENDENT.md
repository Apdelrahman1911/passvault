# PVU-003 chooser / Home / cancel fragment — independent source review

2026-09-10; reviewer `/root/native_review`, author `/root/editor`.
**Accept the narrow inert fragment with the limits below; no required source
correction identified. NOT adopted, compiled, executed or execution-admitted.**
This reviews the concrete implementation, not another full admission design.

## Exact input

All paths prefixed `reviews/` are beneath this continuation directory.

- `reviews/editor/pvu003-draft/PVU003-CHOOSER-HOME-CANCEL.kt.txt`:
  `76d198977e09cfd38e664cd957f5c3aa159c76488843e4b4dad36667b175cbd3` (9951 B/173 LF).
- Its `PVU003-CHOOSER-HOME-CANCEL-SOURCE-NOTE.md`:
  `a9caf50f56f7b239f146cc6ae895a48485f7d7ff6ba5a7e51a9ea2dbb451305f` (9870 B/124 LF).
- Inherited `reviews/android32/navhost-draft/CredentialMainNavHostRoomIntegrationTest.kt`:
  `7eee7bc045abcb60660203f965a33a71e6e791d9ba2c22509a52ee615040ec9d` (38728 B/727 LF).
  Only integration/input/observation/failure-helper excerpts were challenged here;
  this is not a replacement for that whole draft's separate independent review.

## Concrete challenge results

1. **Real route and guards, no injected event.** Settings' Data Management action,
   Data screen's Export Button and the English labels match production source.
   SettingsRouteAdapters carries those real routes to Export. The inherited native
   selectors remain prospective AX mappings, not a demonstrated toolkit contract.
   The snippet reads the actual GlobalContext singleton only after entering Export;
   typed-password equality plus canExport preserve strength/operation guards. It
   neither replaces Koin nor sets ViewModel state. The backup busy claim and real
   FileDialog precede output creation/service entry. Native cancellation's existing
   generic save-error presentation is allowed, not silently patched away.
2. **Modal input is intentional and bounded.** Before the one Home press/release,
   the snippet requires the same directly owned, active/focused, application-modal
   SAVE dialog, expected title, non-excluded frame, exactly two showing owned
   surfaces, unique initially-unselected enabled Home and non-overlapping padded
   geometry. It rechecks identity/geometry after pointer movement. Unlike ordinary
   click, it does not demand/force activation of the blocked frame. Unsupported
   native geometry/focus/AX mapping fails closed; public FileDialog APIs and source
   construction alone do not prove that the actual Linux peer exposes these values.
3. **Oracle stays smaller than PVU-003.** This is explicitly chooser-visible first,
   not the earlier immediate Export/Home race. Twenty during-modal samples and ten
   after-cancel samples OR their respective Home-selected observations. They are
   not continuous monitoring or exact 400/200ms elapsed windows. The four passive
   delivery bits distinguish frame press/release callbacks while showing/hidden;
   they provide neither order/timestamps nor proof of Compose onClick. Zero bits
   are not proof of absent native input or of a specific guard rejection. Both
   selection booleans may be true; after=true alone is not modal admission.
4. **Primary errors and owned cleanup are preserved.** Home input captures pressed
   ownership before its sole press and attempts release in finally. The same exact
   listener object is captured before fallible registration and removal is attempted
   independently after chooser cleanup. MainNavFailures retains first/suppressed
   exceptions and restores interruption; a cleanup error cannot turn an earlier
   failure into a returned DTO. Normal return requires one Escape, dialog hidden,
   null file, export-idle post-cancel samples, no success message and completed
   listener removal. There is no destination selection, Enter/Save, direct Cancel,
   dispose, OnCancelOperation, modality change or competing graph/database close.
5. **Integration remains deliberately incomplete.** The fragment depends on private
   NativeMainNav helpers; it is not a Kotlin compilation unit or @Test. A separately
   named scenario must adopt it and bind a small result receipt before Main's genuine
   terminal path. PVA-007's exact EVENTS/trace allowlist and existing scenario must
   remain unchanged, not be silently reused as a PVU-003 passing oracle. Actual Main
   retains its graph, instance lock, coordinator and terminal ownership. No second
   driver, source-module adoption, extra case or terminal owner was added here.

## Explicit failure / inference limits

**The inherited 150-second driver deadline also caps cleanup await.** If exhausted
before finally, it can prevent any Escape from being issued. A missing/ambiguous,
unfocused or unsupported chooser can likewise prevent native cancellation. An EDT
timeout cannot settle work already begun; listener removal/input release/native
cancellation may fail. The author explicitly agreed these are outer hard-settlement /
HOLD obligations, not guaranteed cleanup or success-by-forced-shutdown. No deadline
expansion, cleanup retry or new authority is supplied by this review. The existing
200-second Main-child cap still requires separately admitted outer containment.

A normal return gives only the sampled chooser/Home/export-state observations.
No transition is bounded counterobservation, not universal disproof. Export-busy
or idle does not establish private Job survival/settlement, lifecycle RESUMED or
mutex ownership. No credential interaction, delete intent/confirmation, A/V cycle,
data-loss or hardware evidence exists. Chooser default-directory/toolkit side
effects still need isolated HOME/XDG/cwd/display authority; this source cannot
establish filesystem isolation. Receipt integration and fresh execution/cleanup
review remain required before any compile or application operation.

## Binding and scope receipt

All seven production pins in the author note independently matched; their relevant
route/button/state/chooser guards were read or reused from the preceding accepted
review. Additional reused source bindings, also rebound here:

- `shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/SettingsRouteAdapters.kt`:
  `6339edcfe9a09916a5485894e287c40697e85909158c0c348e51b486b8073c06`.
- `feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/presentation/BackupFileCleanup.kt`:
  `829962a3eba0fcd198febed205b3b3a22a3aeddf74a4e40aedeae95e39695bb3`.

Twelve input/source files were rebound before this exclusive0600 report write,
file/parent fsync and exact readback/hash. Author artifacts stayed unchanged;
there was no rejected revision or silent correction. Only this permanent report
was created: no source/module/frozen-payload edit, import, syntax probe, build/test,
application/native input, Git/network, held-runtime access, filesystem/process sweep,
cache/temp/runtime output or persistent worker. Wrapper stop is N/A. Root retains
execution/coordination/cleanup/publication authority. All STOP/NO-RETRY/CLOSED/HOLD,
PVA-029 no-automatic-retry, PVD and hardware boundaries are unchanged.
**PVU-003 remains UNRESOLVED; zero executed cases, fixes or closures; no denominator change.**
