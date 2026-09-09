# Database outer LCL-OWN-001 successor — author source report

Author: `/root/storage`; 2026-09-09 UTC. Root explicitly authorized this narrow
correction after `/root/verification` independently confirmed the existing
variant. **PROPOSED SOURCE CORRECTION; independent successor review and meaningful
regression remain pending.** This report is not an independent acceptance,
execution approval, runtime result or whole-outer viability claim.

## Exact frozen author tuple

Paths are relative to `docs/audit-continuation/2026-09-08-linux/reviews/`.

| Current subject | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| `linux-runner/LAUNCH_DATABASE.py` | `871aa7ebca0a2c7cf3e73923396a1e918d2186332a7f7c22988d3d8469754889` | 37642 /718 |
| `linux-runner/LAUNCH-PLAN.md` | `965439c1a4ce968aa8c96ec9a379d5b0fdd17e55c33f1cccb6d02ebe793226f2` | 15746 /266 |

The following exact inert rejected before-images were exclusively preserved
before either source or PLAN edit, not reconstructed from the successor:

| Preserved source | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| `storage/linux-database-outer-registration-rejected-ee46.py.txt` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37477 /715 |
| `storage/linux-database-outer-registration-rejected-c5bb-LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` | 11441 /200 |

The independent original defect qualification is
`verification/LINUX-DATABASE-OUTER-REGISTRATION-QUALIFICATION.json`, SHA-256
`326ed288776d5bfcf127d63341aaf2ae28ae21a939146fe75a01544257d82537`,
and MD `4737f349ca9c44314685492f66b4961759d056691ecf5fd97454b61d81a74f77`.
Its prior source-acceptance miss, narrow source inference and counterexamples
remain intact. That report confirms the defect; it does not preaccept this fix.

## Complete source change and author counterchallenge

The exhaustive literal ee46-to871aa7 diff has one hunk: inside the existing
`Originals.directory` exception handler, before its sole local `os.close(fd)`,
add only:

```python
# Unpublish this new key before the possibly completed local close.
self.fds.pop(key, None)
self.pins.pop(key, None)
```

Every other source byte is unchanged. PLAN retains its original200LF exactly
and appends66LF of source/provenance/remaining-gate qualification. There is no
change to commands, caps, paths, outputs, roles, case selection or one-shot
supervision. The source's new line135 is the comment;136–137 remove entries,
138 performs the existing sole local close,139 reraises. This is source text,
not an executed snippet or an invitation to import the helper.

The unchanged outer guard enters only for a key absent from `self.fds`; both
registries remain ordinary dictionaries, and signal handlers do not register
directories. The rollback removes the incomplete new key, not valid parent
entries. A successful paired registration never reaches it. Failure before the
first store has no newly published FD key to remove; successful ordinary pops
are then no-ops for that absent key. After a successful first FD store and failed
second pin store, successful rollback/local close leaves no stale FD entry for
final registry enumeration. No descriptor is adopted or replaced.

The original conditional failure occurs before lock/evidence/child allocation.
No intervening descriptor allocator or FD-number reuse, unrelated-FD injury,
application data loss, false PASS or new operational HOLD was established.
The original failure already causes HOLD. This correction is not a guarantee
for repeated catastrophic allocation failures, ambiguous closes, uncatchable
signals, blocked syscalls or host loss; it fixes the bounded publication order.

## Separate evidence and remaining work

Runner346e,1198-source inventory,105-method inventory, E/bootstrap journal,
old requests/acceptances and all other helpers remain untouched. This is not a
build of continuation application changes. The intended database source remains
commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`. No containing Git commit, publication
inclusion, current original pin, absence, sole-slot state or future exit is
asserted by this author report.

The receipt-schema memo cdc7138ac4057defbfb4d4f3c067d977e5df7f1e3223d253fbc47a76ff7de43d
and its independent schema review6338943509dfb6dbca9d959e3fe5bb22c714e561f3c8f0e8d0bea7967e8a5bda
remain exact **ee46 schema** qualifications, not successor/whole-outer acceptance.
Any later successor schema binding must be additive, not a rewritten old memo.

Independent exact871aa7/965439 patch review is required. Root separately
commissioned source-bound registration02 normal/fault evidence for this sibling;
its source, launcher, instance and execution/result review remain separate.
The existing four controls target closer8437/new closeout outer5047. Neither
those four nor separate17/28 are extended or credited by this edit. All original
database F01–F07 and postrun C01–C07, coordination/resource/external cleanup,
actual exit and independent settlement gates remain in force.

## Operations and resource discipline

The data-only snapshot writer (`7878e7`) checked stable eight-field regular-file
pins, exact hashes/sizes/LFs, then used exclusive0600 no-follow creates, bounded
positive-write loops and file/parent fsync; it exited0. No target source was
parsed as Python or imported. The complete source diff (`eae3ab`) exited1 because
the documented three lines differ; that is not a failed test/control. The source
and PLAN were then rehashed and frozen for review. Only these permanent source,
PLAN, before-images and this report were authored; no actual receipt was made.

No target import, AST/syntax probe, control, build/test, temporary runtime/cache,
worker/daemon, signal, cleanup or retry was performed. Metadata commands exited;
wrapper stop is NOT_APPLICABLE_NO_WRAPPER_LAUNCH, not discharge of prior duties.
Root alone owns execution, monitoring, actual admissions and publication. No
unrelated process, shared cache/toolchain, permanent source/test/report or
protected branch was stopped, deleted or changed.

Zero executed controls/application cases, zero new product families and zero
qualified closures. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic
retry, G7/G8 CLOSED, Windows failure/cleanup HOLD/all14 unstarted, hardware/PVD,
mobile1017001 and all dependency/version/identity/signing/store/publication
boundaries remain unchanged.
