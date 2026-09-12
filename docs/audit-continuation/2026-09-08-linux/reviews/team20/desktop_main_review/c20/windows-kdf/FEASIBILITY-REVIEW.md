# Windows KDF01 — independent feasibility review

Reviewer: `/root/desktop_main_review`; author: `/root/desktop_integration_author`.
Date: 2026-09-11 UTC. `B = docs/audit-continuation/2026-09-08-linux`.

## Disposition

**ACCEPT_FEASIBILITY_ONLY.** The proposed branch-only, non-publishing route is
narrowly feasible to prepare. This is neither helper implementation acceptance
nor `ACCEPT_WINDOWS_KDF_KNOWN_ANSWER_01_INSTANCE`, publication authority, a slot
reservation, runtime admission, a Windows result, or permission to replay an old
cohort. Root's RELEASE C20-01 permits source/evidence work only and separately
authorizes the smallest new helper/workflow source task after this seal.

Root explicitly accepted the accompanying declarative workflow text as an inert
feasibility illustration, with no ordering deviation or promotion claim. No
canonical workflow/helper/request was edited or executed by this reviewer.

## Exact reviewed identities

All SHA256 values below were confirmed by bounded byte reads. Author paths are
under `B/reviews/team20/desktop_integration_author/c20/windows-kdf/`.

| Input | Bytes / LF | SHA256 |
|---|---:|---|
| `FEASIBILITY.md` | 11524 / 179 | `7615f1e2e4ddc52842c4c660fb6698ec98729afad8069ea57b8e39817cbaa07d` |
| `workflow.yml.txt` | 2496 / 57 | `6d6261c8a23b69b7ad7494b5a9b5bdde0e2029ac19f79529dac9c8e856ff6918` |
| `.github/workflows/audit-windows-native-validation.yml` (inert precedent) | 2177 / 54 | `d70ba4a276957158d2904a426e421c7303bea9c47de4fc7a96cf060ce2bb1d21` |
| `scripts/audit/windows_native_validation.py` (inert precedent) | 70729 / 1252 | `35c14451f4e08b6c6f222f2b0f1297370b7534422f6b4194616bcecc92fc171a` |

Separate KAT source acceptance, not repeated here:
`B/reviews/team20/desktop_other_review/c20/NATIVE-KDF-EXACT-PATCH-REVIEW.json`,
13800 bytes / 162 LF,
`6de5ea70be36aa4d9416f0de0135276a7a757975c8d1c3bddcdbdf9cf9e62321`.
Its disposition and CI contract were read. Root's exact application receipt,
`B/reviews/team20/root/C20-NATIVE-TEST-INTEGRATION.json`, is 1467 bytes / 26 LF,
`227fae154a727b51e1f03b00a17fe48bf8d380c8d3237b86b944a2e17082dac5`:
applied source delta, not native execution.

Current canonical byte hashes independently match both accepted after-images:

- `app-desktop/native/biometric-bridge/tests/windows/passvault_biometric_windows_security_test.cpp`:
  39578 bytes / 924 LF,
  `b33f12ddf18f519e83dbac57797032a25192bbeb5bf071c9d62697c763576653`.
- `app-desktop/native/biometric-bridge/CMakeLists.txt`: 9534 bytes / 234 LF,
  `e27e27f3c1750c88c308341273410785b72de3c79af9a25f0d3e30c295a555fe`.

## Independent challenges and required implementation boundary

1. **One new case, not old passing-case replay.** Build only the existing normal
   `passvault_biometric_windows_security_test` target with testing ON and
   `PASSVAULT_AUDIT_PVA036_037_CONTROLS=OFF`. Filter both inventory and execution
   with `^passvault_biometric_windows_kdf_known_answer$`; before test launch require
   exactly its generated Release executable plus sole argument `--kdf-known-answer`.
   No ABI/DLL/historical/PRK target, default executable path, unfiltered CTest,
   old 24-case loop, or unrelated binary-retention expectation may survive.
   At most nine controlled parent commands does not mean at most nine processes:
   owned compiler/CTest descendants remain subject to the original Job.

2. **One source-qualified result.** The separate accepted source has two fixed
   vector assertions, success/input guards, and no per-vector terminal markers.
   One selected native test process and one raw CTest XML case can qualify those
   checks under ordinary execution; neither two executions nor four HMAC cases
   may be invented. Require exact nonempty bounded XML, name/classname, counts,
   run status, no skipped/failure/error evidence, and independent source/argv/
   effective-build reconciliation. Empty system-out is valid. A build, process
   launch, exit alone, or source acceptance is not a passing Windows observation.

3. **Branch-only activation is feasible, not already authorized.** Proposed
   canonical identities are the new `.github/workflows/audit-windows-kdf-known-answer.yml`,
   `scripts/audit/windows_kdf_known_answer_01.py`, and
   `B/requests/windows-kdf-known-answer-01.json`, with the distinct future reviewer
   path `B/reviews/team20/desktop_main_review/c20/windows-kdf/INSTANCE-ACCEPT.json`.
   Publish reviewed inert source first only if root admits it; a later one-parent
   direct child must add only the fresh request. Keep exact repository/ref,
   push/not-deleted/attempt1, HEAD/parent/tree/A-only-diff/cleanliness and input-hash
   checks. Install containment before any read-only Git child; complete those
   checks before configure/build. No main merge, PR, dispatch, matrix, release,
   retry, or reuse of the consumed Windows05 request follows from this route.

4. **Scheduling caveat corrected before this seal.** The author's initial claim
   that `cancel-in-progress: false` means no existing-job cancellation was too
   broad. The sealed note and illustration now explicitly preserve the older
   pending-run replacement caveat and require root to exclude queued/pending AND
   running local/CI audit cohorts before either push. Independent other-workflow
   trigger review is required too. The illustration declares exactly one job;
   that is not evidence either push schedules only one job repository-wide, nor
   does a shared concurrency group establish the global execution slot.

5. **Small permission surface.** One Windows-2022 job, ten-minute envelope,
   `contents: read`, existing immutable checkout/upload action commits, exact SHA
   checkout/depth2, no persistent credentials/submodules/LFS, no secret inputs,
   environment attachment, OIDC, signing, cache or publishing step. Platform
   action credentials are not claimed nonexistent; the helper must preserve the
   explicit clean native-child environment and private generated profile roots.

6. **Preserve lifetime primitives literally, narrow only the workload.** Inert
   precedent inspection includes command lines647-795, settlement797-856,
   cleanup858-936 and finalization1099-1248. Preserve creation-time `JOB_LIST`,
   per-command NUL/stdout-only `HANDLE_LIST`, noninherited Job/source/cleanup/log
   handles, original process-HANDLE waits, immediate-parent exit receipt, 16-active-process/
   3GiB Job limits, resource floors and nonrenewable 480/540/600-second envelope.
   Cancellation stays sticky. Exactly one final original-Job termination attempt
   cannot be vetoed by a journal/resource failure; observed Job zero is required.
   No PID/name killing, general runner, old-helper import, `rmtree`, or shell
   recursive cleanup is an acceptable simplification.

7. **Evidence before fresh generated-only cleanup.** Require exclusive fresh G/E
   ownership, final source/authority checks, bounded settled logs/XML/config/PE
   hashes, and original log closure before descriptor-gathered deletion. Gather
   and validate every generated descendant before any deletion; reject reparse,
   multiple-link, identity and original-close uncertainty without following or
   retrying. Delete only through original generated-descendant/root handles and
   verify G absence; E, checkout/source, SDK/cache and old held roots are excluded.
   Test failure alone does not bar independently safe cleanup; evidence/ownership/
   settlement uncertainty does. Preserve HOLD. A KDF-only path does not prove no
   tool/profile reparse entry, repair Windows05, or turn hosted disposal into an
   observed cleanup result. Upload only owned, flat, bounded JSON/JSONL/log/XML;
   no binaries/profiles, and no success invented after hard cancellation.

8. **Exact source packet remains to be reviewed.** The new helper must explicitly
   bind every current configure-input leaf and authority/control file, plus
   `.gitattributes`; prove controls OFF and absence of historical/PRK compilation
   macros/sources in the normal selected target, not merely trust its target name.
   Effective compiler evidence, final source identities, exact helper/workflow/
   review hashes, source commit/tree, request nonce and current root admission are
   not supplied by this feasibility seal. They remain separate gates.

## Method and ceiling

Only bounded inert source/report byte, text and JSON reads/hashes and this own-leaf
report write were used. No subject helper import/AST/eval/execution; no Git,
network/CI, build/test/application/native, process/device/runtime/SDK probe,
canonical edit, cleanup, or held-root traversal. No new native case executed and
no pass/cleanup counter advanced.

Windows05 remains consumed/HOLD and its reported passes do not erase overall
failure. PVU007 STOP, PVU011 NO RETRY, PVA029 failure/no automatic retry, G7/G8
CLOSED, native-refusal, protected refs/dependencies/identity/publication, occupied
1017001 and all other existing STOP/NO-RETRY/HOLD fences remain unchanged.
