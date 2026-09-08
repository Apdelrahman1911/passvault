# Windows native-01: author diagnosis and counterchallenge

2026-09-08; author `/root/native`; execution owner `/root`; independent result
reviewer `/root/native_review`. **FAILED_OR_INCOMPLETE; cleanup HOLD; all 14
planned native cases UNSTARTED.** This is a source/evidence-only diagnosis, not
another execution, recovery admission or product-family closure. The accepted
helper, workflow, source bindings, activation request and run01 evidence have
not been changed by this author after execution.

## Identity

- GitHub Actions run **34284083351**, attempt **1**, job **102255455859**;
  terminal failure at **2026-09-08T22:06:09Z**.
- Reviewed source commit `1a57239f74af792b64e631f51a79e1c0bf83816a`, tree
  `7375659b90f1c622b7fa29a19dcc8b268e4ed340`.
- Request-only child commit `a1fbdf0d5f0663225a459838d37b072a65e334f6`, tree
  `0e5144c44fc79f052d0131808b887f8440e0a2cb`.
- Executed helper SHA-256
  `67bcd0aeaa03fa829d55fecfbd640d185163805e4c0a067b281bd807333af243`;
  workflow `643fcba6cb393bd6bbd8497cfd03a52ccc43fa217a9d1d728c453d29ddb87b8c`;
  source bindings `27152712c1ac99ea1fcf739210764b0a5cabf8f45f1edbec2ecb3b143d2ebcb9`.

Evidence below is relative to `../../runs/windows-native-01/`. The author read
the result, complete journal, all command logs and their retention receipts,
allocation/request/toolchain/source captures and root's Actions transport records.
This is not an independent redownload of the artifact. The independent result
reconciliation is `../native-independent/RESULT-REVIEW-01.md` and its JSON.

| Retained evidence | SHA-256 |
| --- | --- |
| `result.json` | `7ac33500c3bd6275e9beb7e04db2caedcf94f73acaef88344e60beb6d2aa7fa5` |
| `journal.jsonl` | `4b59052720fd81afab87d40a2ae3f24a65e1706fca24f1bca77e6f73bbe63e14` |
| `05-configure.log` | `315f0d063a5a1cd1b6ef8d5b7dad2e8a02b16a8f3703376455050edf987f5046` |
| `toolchain.json` | `a4f6607e6b9b1d27656d6ca1723d060b35ea94cfa993c42c94d83e82a719d88b` |
| `allocation-intent.json` | `efd419f3c6fcf348d22b7923de6f8f83ef0e813cec416c245b738d6474a930bf` |

## Observed sequence; no inferred PASS

1. Five command intents were recorded. Four completed exit-0 records cover
   Git identity, request-only diff, clean source and `cmake --version`.
2. Configure launched under the owned Windows Job. Its complete 847-byte log
   reports MSVC **19.44.35228.0**, compiler-identification/ABI probes, configuring
   done, generating done and generated build files. **Compiler probes occurred**;
   saying "no compilation" would be incorrect. There was no product-target
   build or native regression execution.
3. `Run.command` obtained the configure parent exit code (helper 409–410),
   waited up to ten seconds for the owned Job to empty, and failed at 416 with
   `Owned descendants remain after command`. The parent code was only going to
   be recorded after that check (422–425); its value is **not retained**.
   Normal-looking configure stdout does not establish observed configure exit 0.
4. The failure path used its one exact-Job termination call (434–437). The
   `command_aborted` event, settled complete-log receipt and final
   `owned_workers: 0` support settlement of the owned Job. This is not a global
   process census or evidence identifying the lingering descendant.
5. Cleanup rejected a reparse entry in `Windows.open_owned` at 264 during
   complete descendant-handle gathering (449–466). That phase precedes every
   deletion. There are no `delete_intent`, `delete_root_intent` or
   `cleanup_settled` events. No cleanup deletion loop was entered; generated-root
   removal is not proved. The permanent disposition is **HOLD**, not "cleaned
   by hosted disposal."
6. Product-target build, compile-configuration evidence, native artifact hashes,
   CTest inventory and all 14 case invocations were not reached. Actual native
   cases: **0 executed, 0 passed, 0 failed, 0 skipped; 14 unstarted**. The failed
   Actions job is not 14 failed tests. End-of-run source revalidation was also
   not reached; the actual pre-command checkout binding must not be upgraded to
   a post-run drift proof or whole-project EOL revalidation.

CMake was **3.31.6**; Windows image **20260830.290.1**; Python **3.12.10 x64**;
SDK requirement **10.0.26100.0**. Recorded resource samples stayed above the
admitted floors. No resource-floor, log-size or command-time-budget failure was
recorded; sampling is not continuous drain/cleanup resource coverage.

## What the evidence cannot diagnose

The lingering descendant's PID/image/start identity, the configure parent's exit
code, and the rejected reparse entry's path/tag/target were not recorded. The
generated-root size was not retained. In particular, the evidence does **not**
establish MSBuild node reuse, VCTIP, profiler activity, a .NET cache, a junction
or a CMake-created symlink as the cause. Naming one would be speculation.
Neither operational failure is a new confirmed PassVault product defect.

The author agrees with the independent review's counterexamples and bounds:
good-looking configure output is insufficient; a missing exit record is not
proof of a nonzero exit; count-only Job accounting cannot identify a child; the
generic reparse exception cannot identify its target; safe cleanup refusal is
not cleanup success. No worker-name kill, broad process scan, path-following
recovery, reparse deletion or relaxation of containment is proposed.

## Source-supported next proposal, not execution admission

Three observability omissions are reachable on the recorded failure path:

- Record the parent exit immediately after `GetExitCodeProcess`, separately
  from the still-unsettled cohort/overall command status. Do not mark that
  command successful until its existing settlement predicates pass.
- Before exact-Job termination, capture a bounded Job-member snapshot if
  possible: query only this Job's PID list, open query-only handles, verify
  membership with `IsProcessInJob` before querying image/start identity, and
  preserve races/unavailable identities as explicit unknowns. This would not
  authorize name/PID-based termination, global process enumeration or escape.
- On cleanup rejection, record only the allowlisted owned-relative path and
  identity/attributes/reparse tag available from the already-open no-follow
  handle. Do not follow or read the target. Preserve gather-before-delete and
  the existing reparse/hardlink refusal.

These would correct **observability**, not the unproved worker/reparse root
causes. No helper change or new request has been authored here, and no new run
is admitted. An unchanged automatic retry would merely repeat an unresolved
operational failure. A future corrected-source/diagnostic proposal needs a new
bounded rationale, exact source binding, independent admission and root's
separate cross-host slot decision; this report supplies none of that authority.
Run01, its accepted source and the cleanup HOLD must remain preserved even if
some different source is independently admitted later.

## Ledger, safety and local resource disposition

PVA-036/037 remain target-verification-blocked. Historical red controls,
production allocator-cut injection, fixed KDF vectors, PVA-010 native
concurrency/provider/sanitizer/packaged proof, displayed prompts and real Hello
hardware are not supplied by this run. Confirmed closures stay original
**19/25**, all **22/37**; original suspicions stay **2/12**. Eight PVD design
explanations and separate owner decisions remain unchanged.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded FAIL/no automatic retry and
G7/G8 CLOSED remain intact. No archived runner/helper was executed/imported;
no build/test/CI, cleanup recovery, Gradle, signing or publishing operation was
performed by this author. Source reads/hashes and this permanent compact report
created no build artifact/cache/temporary archive or owned background worker.
VPS point sample during this review: about **28 GiB free disk**, **42 GiB
available RAM**. No unrelated cache, SDK, toolchain or process was removed.
