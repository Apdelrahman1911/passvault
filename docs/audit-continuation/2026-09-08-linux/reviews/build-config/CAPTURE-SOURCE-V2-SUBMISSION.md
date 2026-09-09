# Fixed source capture: v2 source-only submission

Author: `/root/build_config`. Submitted `2026-09-08T23:46:35Z` for independent
review by `/root`, the sole possible executor. **NOT ADMITTED** by this author.
The complete successor was reread as inert text; no syntax check, import,
execution, build, test or capture has occurred by this author.

## Frozen inputs

- Proposed utility: `../linux-runner/CAPTURE_SOURCE.py`, 22477 bytes, 410 physical
  LF, SHA-256
  `26c939c39864f20b721f1c0caa81d79008d6304bf91fbd4b528329ee3367a149`.
- Rejected v1 retained exactly as `CAPTURE-SOURCE-V1.py.txt`, SHA-256
  `bf07c34053038d12cd27cd45e6f6e9e1995c029091a3bd8b4b90b3dad1373dc7`.
- Independent v1 REVISE disposition: `CAPTURE-SOURCE-V1-REVISE.md`, SHA-256
  `48eb55ff11c6af5023d0f45febd9bb30a7ea37b7bfc97b3397e7eab01fff8810`.
- `../linux-runner/SOURCE.json` was absent at submission. Do not create it by
  another writer or overwrite an occupied/partial output.

## Response to v1 independent challenge

1. `directory()` registers each original descriptor before `fstat`; a failed
   registration explicitly closes the just-opened descriptor. Its finalizer
   attempts all registered closes even after an individual `OSError`.
2. `owned_fd()` retains ownership independently of `fdopen`. Borrowing source,
   memory-sample and output streams use `closefd=False`; stream construction or
   close/flush failure still reaches original-descriptor close. Acquisition and
   descriptor cleanup mask the four handled signals.
3. `git_child()` retains original stdin/stdout objects. The one cleanup wait is
   enclosed by a pipe-closing `finally`, so timeout/other wait failure does not
   bypass original-pipe cleanup. It records actual exit when known, original
   PID, launch-called uncertainty, cleanup errors, settlement and HOLD. Any
   needed signal targets only the direct, unreaped `Popen` child; no name/group
   kill, launch retry or cleanup retry is introduced.
4. Bounded stdout JSONL emits command intent before launch, original PID after
   launch, terminal settlement and resource receipts. Per-event cap is 4096
   bytes, total cap 64 KiB. Root must retain stdout plus its own invocation/exit
   receipt; a summary or `SOURCE.json` alone is insufficient closeout evidence.
5. Disk samples use the original repository/output-directory descriptors;
   bounded `/proc/meminfo` yields MemTotal/MemAvailable. Initial floors are
   12 GiB disk / 25% available memory, ongoing floors 8 GiB / 20%. Samples occur
   initially, at per-file loop boundaries with a five-second target, prewrite,
   and in an `ExitStack` finalizer before directory descriptors close. The
   finalizer also runs on failures **after it is registered**, not failures
   opening those original directories. Final resources and any prior error are
   in external stdout, not retroactively appended to exclusive `SOURCE.json`.
6. Git's framed batch cap includes each header, blob and LF delimiter. Both
   prospective frame size and the actual accumulated byte count are checked
   against 256 MiB; checkout bytes have a separate 256 MiB cap.

## Unchanged qualifications

The fixed target is the preserved 1198-file containing handoff commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, Git tree
`05014e9f635131d5db06701e4013b4b5a746465a`. It is not the 811-file application
coverage denominator. Four serial read-only Git commands are proposed. No old
runner, archive helper, JVM, SDK, emulator, application or private data is read
or executed. The two exact raw/Git/checkout PowerShell qualifications and the
three pinned attribute policies remain explicit; no general whitespace waiver
or inheritance of exact raw-source/runtime coverage is allowed.

Normal coordinated Git-store/installed-tool trust is assumed, not hostile
same-UID hermeticity. Stable file observations and repeated HEAD/tree checks
are not an atomic snapshot, untracked-file inventory or clean-worktree proof.
Root still needs fresh coordination and under-lock rebinding for any build.
The alarm/poll targets do not bound arbitrary synchronous filesystem/stdout
operations or OS scheduling. SIGKILL/host loss can leave partial output or
unknown child settlement; these remain HOLD/no retry, never successful cleanup.
Failure before a journal receipt is emitted requires root's external receipt.

This is only a source-capture proposal, not a new product finding, completed
regression, skill-validator substitute or build admission. Existing accepted
Android, Apple and M03 report bytes and all audit denominators are unchanged.
No author-owned persistent process/cache/build output was created or removed.
