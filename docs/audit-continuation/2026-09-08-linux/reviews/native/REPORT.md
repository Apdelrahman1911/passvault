# Native continuation source review and authored validation

Author/reviewer of historical native fixes: `/root/native`. Author of new
workflow/helper: `/root/native` (therefore **not their independent verifier**).
Date: 2026-09-08. No build, test, CMake configure, native executable, old runner,
signing, publication or process cleanup was invoked by this agent.

## Continued findings, not a restarted audit

- **PVA-036 source correction remains supported.** `CREATE_NEW` failure no
  longer grants temporary-path ownership. An acquired share-zero handle is
  closed before deleting its owned temporary on security-validation/DACL
  rejection. Challenge: a collision is not owned, the temporary contains an
  encrypted envelope rather than plaintext, and real destination rename denial
  must not destroy the old bytes. The eight existing tests have meaningful
  controls: collision sentinel with DELETE probe closed before assertion,
  decisive directory/content checks before fixture teardown, and real sharing
  denial followed by positive replacement. Exact historical red controls are
  still unexecuted; a Windows current-source pass alone will not erase that gap.
- **PVA-037 source correction remains supported in its stated bounds.** The
  allocation-free borrowed-array scope guard precedes secret population in the
  two provider-copy sites and PRK derivation. It does not own caller output.
  Its four existing tests inspect every live-array byte after explicit unwind,
  plus an unrelated nonzero array. Challenge: no actual memory exhaustion,
  freed-memory probe, production provider-cut injection or universal erasure is
  demonstrated. Existing tail wipes are retained; PVD-003 remains separate.
- **PVA-014 target gap can be reduced narrowly.** Actual Windows DLL additive
  symbol resolution and malformed/valid UTF8 reason checks are testable without
  prompting. Missing metadata precedes capability/inventory; null-context ABI
  controls cannot enroll. Windows intentionally retains OS-owned WebAuthn UI;
  this is not a new fourth platform prompt-rendering defect.
- **PVA-010 remains target blocked.** ABI loading plus a no-operation context
  destroy is not a concurrent in-flight native lifetime/cancel test. The
  historical real-JVM-thread/fake-NativeApi evidence remains valid in its scope;
  hosted native14 does not replace provider/sanitizer/packaged lifecycle proof.

The independent reviewer separately challenged these existing source/test paths
in `../native-independent/NATIVE-TEST-SCOPE.md`. Root expressly accepted the
bounded proposal to include the two existing ABI/security siblings with the
12 PVA-036/037 cases; no extra production patch was applied.

## Authored work and independent review boundary

New files:

- `.github/workflows/audit-windows-native-validation.yml` — branch + explicit
  request-path-only trigger, one job, fixed noncancelling audit concurrency
  group, immutable checkout/artifact actions, contents:read, no secrets or
  publishing steps. Ordinary source/report/workflow pushes are inert.
- `scripts/audit/windows_native_validation.py` — new fixed Windows-only
  native14 helper with independent-acceptance/source/request bindings,
  atomically job-bound child creation, bounded resources/output/time, isolated
  fixture/cache directories, exact XML mapping and handle-bound cleanup.
- `WINDOWS_ADMISSION.md` — prerequisites, commands, target requirements,
  source identity, root cross-host build-slot reservation and cleanup/retention
  qualifications. Root alone may create the activation request.
- `SOURCE_BINDINGS.json` — current six native inputs plus checkout attributes,
  exact 14 names and declared source-inspection ranges.

These are authored source, not executed runner controls or accepted admission.
The new helper/workflow require another agent's exact source challenge and
acceptance, then root's separate admission before activation. A successful
transport/commit or syntax check is not native/runtime verification.

## Remaining work and unchanged restrictions

No confirmed-family/closure/suspicion denominator changes are proposed here.
Starting values remain original19/25, all22/37, original-suspicion2/12, eight
documented PVD explanations with separate owner decisions. Windows target14
is useful evidence preparation, not another plan replacing unfinished fixes.

MacOS/iOS selected-language and lifecycle tests remain essential separate Apple
gaps; no speculative Apple matrix or unsafe provider/Keychain run was authored.
Android32 KDF remains first in risk priority under root coordination.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029's recorded FAIL/no automatic retry and
G7/G8 CLOSED scopes are unchanged. No generated skills mirror or unavailable
skill tooling was edited/substituted. The supplied native skills were read as
instructions; absent optional inspector scripts were not needed or fabricated.

Historical source/coverage qualifications remain intact. Native inspections are
bounded source reads with exact hashes, not an increase to the 811-member G12
raw-snapshot denominator or a fresh whole-file/whole-project coverage claim.
