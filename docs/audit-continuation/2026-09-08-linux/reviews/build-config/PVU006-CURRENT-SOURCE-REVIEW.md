# PVU-006 — independent current Desktop source challenge

Reviewer: `/root/build_config`; report author: `/root/verification`.

**ACCEPT_NARROWED_UNRESOLVED_SOURCE_ONLY.** Retain **UNRESOLVED / VERIFICATION BLOCKED**.
No confirmed defect, production patch, suspicion resolution or closure credit is established here.

## Bound inputs and review method

- Application C4 commit: `da8ff89b9a8579017d5d524e628dff5251f2bb90`;
  tree: `cf0a8a702e7cd6948236be18b491bb5a21b5886e`.
- Reviewed report: `../verification/PVU006-CURRENT-SOURCE.md`, SHA-256
  `5f7652d07deb935cf5378eb896000fc51d374713f731f7488b7a2ffe88f56a7f`, 10,853 bytes / 116 LF.
- Retained handoff outcome: `docs/audit-handoff/current/unresolved-investigations-outcome-only.json`,
  SHA-256 `420b7eb1a419aabec37cbc323bc7d9a9b32ca160395dcc2d5f109488820eed19`, PVU-006 lines 291–347.
  Its embedded independent review identity is
  `b4d97cf14a572dc119968ce6d4576b3b5a0988166ba595b4edb286cc669ccff1`.
- Retained Linux report: `../editor/REPORT.md`, SHA-256
  `24f36c35411e2d2c88679dfd6820822681cecda18f2626f7c3f92cb44d3abd69`.

Independently compared all fifteen production paths in the reviewed report: working-file SHA-256,
C4 Git-blob SHA-256 and reported SHA-256 all matched. The scoped handoff-to-C4 diff changes only
`VaultRepositoryImpl.kt`, in authentication/verification-plaintext error classification, not its
per-instance session flow. These are scoped byte/delta checks, not whole-worktree or full-file coverage.

The review continues prior independent reads of Main, AppModule, the shutdown coordinator, Settings,
Desktop settings/DI and app composition. Focused follow-up reads challenged window disposal, session
cleanup, Linux biometric selection, navigation lock handling and all five singleton lock methods.
No Kotlin, dependency, harness or application code was imported or executed.

## Challenges and findings

1. **Does application-scope cancellation clear the singleton ViewModels?** No such ownership is supplied
   by the inspected bindings. `AppModule.kt:78,190–201` separately creates the injected application scope
   and five ordinary single ViewModels; their work uses AndroidX `viewModelScope`. The entry-scoped
   `lockSensitiveViewModel` registrations/closeables are not terminal ownership for those five singles.
   The Koin `SingleInstanceFactory.drop` contract is retained from the embedded frozen independent review,
   not newly verified against a dependency artifact. Missing cancellation remains an ownership gap,
   not evidence that an otherwise-unrooted VM/repository/StateFlow cycle leaks permanently.
2. **Does the actual Desktop entry point support repeated old/new runtimes in one continuing JVM?**
   `Main.kt:20–69,105–140` instead intends one owned runtime followed by `System.exit(exitCode)` after
   return. That is a useful counterexample to assuming the historical repeated-runtime schedule applies
   unchanged to Desktop. It does not prove successful process death or exclude harmful pre-exit work.
   Early initialization/coordinator exceptions can miss the inner cleanup; an incomplete cleanup report
   skips both injected-scope cancellation and `stopKoin`. The 2,500ms latch wait is not a whole-path bound:
   synchronous prompt/window work, exception propagation and JVM shutdown behavior are not covered by it.
3. **Are real terminal mitigations being ignored?** The coordinator requests once-only cleanup with
   vault-lock then database-close ordering, plus clipboard, preview and biometric-host cleanup. Session
   lock retains bounded retry and `NonCancellable`; window hooks prepare/hide and detach prompt/native,
   tray and protection resources. These are meaningful source guards, not observed successful graphical
   disposal or a singleton VM-clear hook. Linux selects the unavailable biometric implementation, not
   a demonstrated Apple/Windows native callback root.
4. **Would blanket cancellation on ordinary lock be a safe inferred fix?** No. Navigation invokes
   explicit sensitive-state cleanup. Onboarding/Vault cancel active work; Settings retains its collector
   and preference-save job. Unlock cancels an existing lockout timer/status job but can restart status
   work with one confirmation retry; its separate failed-attempt timer is 30 seconds. Backup may queue
   import-selection discard and intentionally preserves validated import work on restore lock. These compatibility
   distinctions must not be silently replaced with terminal cancellation.
5. **Is a late Desktop preference write proved by a generic suspended fake?** No. The real Desktop
   `withContext(Dispatchers.IO)` path is a concrete dispatch boundary, unlike the retained synchronous
   iOS NSUserDefaults counterexample. Settings' mutex/revision/`ensureActive` guards still matter, and
   synchronous Java Preferences calls are not themselves cooperative cancellation points. A pending IO
   operation is only a candidate external scheduling root: no observation establishes its completion
   relative to disposal, database close, conditional Koin stop or process exit, let alone harmful state
   overwrite, data loss or permanent retention.

## Remaining evidence and accounting

Accept the author's stopping point, not an execution admission. A separately admitted synthetic actual
graphical Desktop terminal schedule could correlate instance/operation ownership and completion with
real disposal, database close, Koin stop **if reached**, and externally observed process death. It must
distinguish legitimate preference persistence and bounded late work from harm. One successful close,
headless return, mocked lifecycle, forced in-JVM restart or compilation cannot settle the whole suspicion.
Other-target runtime rebuild, iOS cover/ACK/unmount and native/hardware gaps remain separate and unresolved.

PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry and G7/G8 CLOSED are unchanged. No archived
helper or unadmitted runner was used. New tests, builds, runtime observations and executed cases: **0**.
Only bounded source/Git/hash readers and this permanent compact report were used; no temporary outputs,
application storage, Preferences provider, cache, daemon or worker were created. Wrapper stop is not
applicable. Root remains sole build/execution and shared-resource/cleanup owner; central counts unchanged.
