# One-shot Detekt02 pure-parser capsule — independent admission review

Reviewer `/root/editor`, 2026-09-10; capsule author `/root`.
**ACCEPT_EXACT_ONCE**, source review only. No capsule, parser, AST/compile,
helper, project command or runtime probe was executed by this reviewer.

## Exact limited admission

- `RUN.py`: SHA256 `a79b3c557013d3acc6da74c146a6a68174c6b3636defbe8e615f0b0b2dbe5824`,
  6,790 bytes /142 LF; all142 lines read as inert source.
- `ACCEPT.json`: SHA256 `a08df58152ef8c2d8b0e96e2c05e2ce0a08d0893a7ddc76489c8dfad352cac70`,
  214 bytes /6 LF; exactly the capsule's four-key acceptance schema.
- Only root may make **one invocation**, after the current closeout and while
  no other audit build/test/CI job is active:
  `/usr/bin/python3.12 -I -S -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/detekt02/parser-controls01/RUN.py`.
  This is not an invitation to invoke/import either holder module or any old
  helper. No additional framework, controls, collector mocks or Gradle run.
- The disposition is consumed by that attempted invocation, including early
  failure before RESULT creation. An occupied RESULT, failed gate, interruption
  or missing evidence is HOLD, not permission for automatic retry.

## Independent challenge and source conclusions

1. Acceptance binds the capsule's own exact hash. Read inputs are owned,
   single-link regular files, <=128KiB, with no-follow leaf opens and stable
   dev/ino/uid/mode/nlink/size/mtime_ns/ctime_ns comparisons excluding atime.
   Parent lstat type checks reject visible symlink/non-directory parents.
   These are finite point-in-time checks, not an atomic descriptor-rooted
   directory snapshot against hostile outside replacement. Only the already
   controlled canonical reviewer-data paths are admitted.
2. The capsule verifies the original Linux coordination directory/lock pins,
   takes a nonblocking exclusive flock, requires the root slot's exact idle
   state, and keeps that lock through computation and receipt settlement.
   These pins are runtime requirements, **not observations made by this
   source-only review**, and not inherited macOS authority. Any mismatch must
   stop this attempt; do not replace a lock or relax a gate to make it pass.
3. Only holder lines124–163 are extracted. Their SHA256 must equal
   `cb30e52bf1c4c1c622a22794158cd6631cbe8542631d46aa748d4f8501294e90`.
   Only that40-LF function is parsed/compiled in memory, constrained to one
   undecorated function with the expected name. The exact bytes were already
   independently reviewed as pure: re.fullmatch and ordinary builtins only.
   No surrounding holder imports, classes, top-level I/O or entry point run.
   Current corrected holder e5b2352… has this same slice; the capsule records
   the actual full-holder digest without pretending to validate its runtime.
4. Control bytes are pinned to `341a26c350d77604a957f433e72fb116fd2532d254dbabf02f5c8c762a1607f2`.
   Exactly P1–P5 are required; all five complete returned objects are compared
   against the independently derived expected objects. There is no empty-list
   all-pass shortcut. P1 is also bound to the three exact retained01 log lines
   71/74/87 and log SHA256
   `13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218`.
   This is data projection, not replay. No failed task becomes analyzer PASS.
5. Input sizes/identities are fixed and small; exact inputs make the five
   evaluations finite. Only one exclusive, <=128KiB RESULT is created; there
   is no subprocess, daemon, server, SDK/cache access, application storage,
   temporary output, Gradle task or background worker. Pre/post capacity
   samples require12/8GiB available disk and25/20 percent available RAM.
   These resource samples are not a worker-ownership or cleanup proof.
6. `finally` retains a compact receipt where possible, fsyncs and closes the
   result and original lock descriptors. Parent fsync/close surrounds initial
   RESULT creation. No report or source deletion is needed or admitted; keep
   this necessary evidence. Process exit releases remaining process-owned
   resources if an ordinary error occurs. Gradle `--stop` is NOT_APPLICABLE
   precisely because no Gradle process or worker can be launched.

## Bounds, failure and evidence qualifications

- The15s check is **cooperative**, not a hard wall-clock preemption guarantee;
  blocking filesystem/kernel I/O may overrun it. No extra timeout/kill/recovery
  helper is admitted by this review.
- SIGTERM/SIGINT/SIGHUP raise cancellation before finalization. Terminal
  finalization blocks those signals to protect its compact receipt/close work;
  only pending signals sampled at its entry are recorded. A later signal may
  remain deferred until process exit. SIGKILL, process/host crash, I/O failure
  or interruption can leave an empty/partial RESULT or no receipt. None is
  evidence of completed controls/cleanup, and none permits automatic retry.
- RESULT is written before descriptor close. A subsequent close/receipt error
  is reported by terminal stdout and nonzero exit and may not appear in the
  already-written RESULT. Therefore root must retain/check **both exit status
  and terminal summary as well as RESULT**, rejecting any mismatch, missing
  receipt, HOLD/FAIL, receipt_error or close_error. A PASS-looking file alone
  is not sufficient admission evidence. Final success requires all five
  exact controls, successful postchecks and successful original completion.
- The receipt is solely five pure-parser control cases; application_cases=0.
  It does not establish collection safety under execution, static analysis,
  Gradle coverage, full-helper success, closure, hardware behavior or release
  readiness. Root owns any later independent interpretation and ledger entry.

No source correction or broader execution is requested for this narrow
capsule. Detekt02 whole-helper source acceptance and execution admission are
separate. All STOP/NO-RETRY/CLOSED/HOLD, consumed01, GUI03, G7/G8,
PVU-007/PVU-011/PVA-029 and PVD restrictions remain intact. This review added
only two small permanent data files; it created no workers, caches, temporary
files or generated build outputs requiring cleanup.
