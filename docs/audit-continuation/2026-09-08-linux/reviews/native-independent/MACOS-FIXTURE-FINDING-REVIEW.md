# Independent pre-patch challenge: MAC-FIXTURE-001

2026-09-09; reviewer `/root/native_review`; author `/root/native`.
Disposition: **TEST-INFRASTRUCTURE SOURCE GAP SUPPORTED; PATCH REVIEW PENDING;
NO EXECUTION ADMISSION**. This continues the previously recorded Apple fixture
qualification, not a newly counted PVA product family.

Author proposal: `../native/MACOS-FIXTURE-PROPOSAL.md`, SHA-256
`98bca2fa8a6b15d8b347d95d4814d6ee42ef619072a626b37cf4b79b753d5290`.
Original fixture SHA-256:
`0dcfe3a1e07aee49ca36463931b5a23a9d0302d431f9eb9f5e98726245f8797b`.
Production macOS implementation SHA-256:
`ba07db83b6ff8b482f4fd1d40afea2d10419c454d7856a6a1eb4ba92aa5542c9`.
Original CMake SHA-256:
`5db2d9b77a00a8308c00843ec28c5fa727e01fb292c34bf96b3e3b109bee2b23`.

The complete 151-LF fixture and 179-LF CMake file were independently reread.
Production reads were bounded to 1–225, 323–436, 539–635 and 800–907, not a
new whole-file or project review. The author proposal was read in full before
the author was notified that this scope was supported for separate source work.

## Reachability and attempted disproof

- APPLE + BUILD_TESTING registers this real native test executable and its
  CTest entry. No continuation execution is asserted.
- After successful `mkdtemp` at 41–43, `PV_TEST_CHECK` (8–12) can return before
  the only final directory removals at 147–148. Subsequent string allocations
  can also throw without a cleanup owner. The ordinary created native context
  can bypass its explicit destroy if an intervening check fails.
- Counterexamples matter: failed `mkdtemp` owns no child; the success path does
  attempt both removals; `mkdtemp` normally grants a fresh private 0700 child.
  This is not an allegation that every run leaks. Literal `/tmp` ignores a
  caller's supplied temporary parent, but is not itself proof of private-data
  access or an observed escaped execution.
- The original main has no top-level C++ catch. A new caught-unwind boundary is
  therefore needed for the proposed ordinary exception controls; do not infer
  an existing caught-exception test path or guaranteed cleanup on an uncaught
  exception/fatal signal. Separate async-launch/native-lifetime failure schedules
  remain explicitly qualified, not silently closed by filesystem RAII.
- `read_metadata` rejects the `/etc/passwd` symlink through `lstat` before
  `open(O_NOFOLLOW)` (156–176). **No actual system-file read is alleged.** Its
  unrelated permissions, length and content could also reject a regressed read,
  weakening the negative oracle. A directly readable, owned valid metadata
  target followed by symlink rejection is a stronger synthetic-only control.
- Creation only establishes the owned biometric directory/context. Retrieval
  from absent `macos-v1.meta` returns NOT_ENABLED at 834–835 before the Keychain
  access at 867. A new valid fixture target must stay distinct from that context
  metadata path to preserve this no-provider boundary.

## Accepted scope, not a completed fix

The proposal is a suitable bounded implementation scope: explicit admitted
private parent with no fallback; exclusive fresh child with no adoption;
directory owner installed before acquisition; throwing path/name allocation
before creation; retained-descriptor/exact-leaf nonrecursive no-follow cleanup;
one cleanup attempt with observable HOLD rather than destructor retry;
caught ordinary C++ unwind and normal context guard; valid owned symlink target;
focused normal/early-return/exception controls with real child absence and an
unchanged synthetic sibling before any independent teardown.

The later exact patch must still be independently challenged for acquisition
ordering, fd ownership/close outcomes, unexpected leaves, path and identity
checks, cleanup failure propagation, positive/negative oracle discrimination,
provider exclusion and Apple/Windows CMake compatibility. Source-declared cases
are not actual executions. A CMake edit must not silently rewrite the old
Windows01 source binding or treat that consumed request as reusable authority.

Nothing here authorizes a Mac/CI/native/helper run, applies the Windows
observability proposal, retries Windows01 or admits recovery. Windows01 remains
FAIL / filesystem cleanup HOLD / 14 UNSTARTED. No product/PVD/hardware decision,
denominator change, verification credit or closure is established. All
STOP/NO-RETRY/CLOSED and non-publishing restrictions remain intact.

Only compact permanent review files were written; no compiler/test/helper,
temporary executable/archive/cache or owned background worker was created.
No unrelated data, source, test, SDK, toolchain or process was removed.
