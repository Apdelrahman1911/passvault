# Completed database launcher receipt — independent schema qualification review

Reviewer: `/root/verification`; memo author: `/root/storage`; 2026-09-09 UTC.
**ACCEPT_BOUNDED_SCHEMA_QUALIFICATION_SOURCE_ONLY.** This is not whole-launcher
clearance, a filled-instance approval, an actual receipt, execution, regression
or cleanup authority. The ee46 registration defect remains a separate HOLD.

## Subjects, independence and review evidence

Paths below are relative to `docs/audit-continuation/2026-09-08-linux/`, except
`scripts/`, which is relative to `/root/projects/PassVault/passvault-linux`.

| Subject | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| Author memo `reviews/linux-runner/LAUNCHER-RECEIPT-SCHEMA-QUALIFICATION.md` | `cdc7138ac4057defbfb4d4f3c067d977e5df7f1e3223d253fbc47a76ff7de43d` | 7784 /129 |
| Database outer `reviews/linux-runner/LAUNCH_DATABASE.py` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37477 /715 |
| Database outer `reviews/linux-runner/LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` | 11441 /200 |
| Existing `reviews/verification/LINUX-DATABASE-INSTANCE-FIELD-MAP.md` | `0edf9d585ee15f62477ededce368407ce74a73712f94d3d36e789e453eacb2e3` | 25416 /400 |
| `scripts/audit/linux_database_closeout.py` | `8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517` | 59609 /1100 |
| Closer `reviews/linux-closeout/PLAN.md` | `0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d` | 34814 /561 |

This reviewer is not the memo or closer implementation author. This reviewer
**did author** the earlier field map and database-outer source acceptance; their
limitations and this new challenge are disclosed, not counted as additional
independent votes on those own documents. All earlier artifacts remain intact.

The entire129-line memo was read (`de7484`); identities and focused live source,
PLAN and field-map sections were independently reconciled (`0f9400`, `e4df9f`,
`a018e2`, `cfd5f5`, exit0). The truncated portion of the large e4df9f tool display
was reread completely in the smaller a018e2 chunk. Review used text/data reads,
not project imports, compilation, syntax/AST probes or executed parser examples.

## Accepted field distinction and both actual consumers

RUN is the worktree's `docs/audit-continuation/2026-09-08-linux/reviews/linux-runner`;
E is its separate `docs/audit-continuation/2026-09-08-linux/runs/linux-database-01`.
The prospective completed receipt is **RUN/LAUNCHER.json**, not an E member.
These are source constants, not current filesystem observations.

- Receipt **`evidence` is the exact absolute E directory string**. It is not the
  RUN directory, an output filename, or an array.
- Receipt **`evidence_files` is exactly four ordered objects**, each with exactly
  `path`, `sha256`, `identity`. Paths are the absolute RUN leaves, in order:
  `LAUNCHER-stdout.log`, `LAUNCHER-stderr.log`, `LAUNCHER-resources.json`,
  `LAUNCHER-processes.json`. The full regular-file tuple is
  `dev,ino,uid,mode,nlink,size,mtime_ns,ctime_ns`; each file is at most1MiB.
- Request `outer_launcher.evidence_paths` is a **different prospective field**.
  Neither the memo nor closer schema renames it. Receipt `argv` is the literal
  inner-runner argv, not the outer entry substituted under that key.

Closer constants23–52 bind these distinct paths. `run_contract`514–528 checks
completed-receipt identity, including **516 `launcher['evidence'] == str(E)`**,
original runner argv, exits/interval relationships and the ten original
bindings. Its next block530–534 requires `launcher['evidence_files']` with the
exact list/count, row keys and ordered paths. No old-key alias/fallback exists.

`admission` invokes `run_contract` at658. The intervening659–699 blocks check
original targets/evidence, lock, complete directory pins and retained originals,
device policy and interpreter. Its **second consumer700–703 also reads
`launcher['evidence_files']`**, calls `dirs.read(..., MIB, durable=True)`, matches
the captured identity/hash, and retains bytes/pins. It does not iterate the E
string as rows. `Directories.read`215–249 applies owned regular/single-link/
nonwritable/size and original pre/open/post guards and fsyncs file/parent for the
durable read. Closer PLAN232–267 states the same two-key contract.

Source-derived counterexamples, **not executed tests**:

1. An array used as `evidence` fails directory equality, even when it contains
   four plausible rows. Two duplicate `evidence` keys are rejected by
   `strict_json`118–126, not combined into two different meanings.
2. Missing `evidence_files` raises ordinary `KeyError` **if the lookup is
   reached**; earlier admission/contract guards can reject first. Spelling it
   correctly alone proves neither full admission nor truthful evidence.
3. Wrong list order, a fifth object or extra row key fails530–534. Reordered
   hashes do not compensate for wrong paths. Correct row shape with mismatched
   durable identity/hash fails702 if that later check is reached.
4. Empty stdout/stderr can be actual observed files; omitted resource/process
   files cannot satisfy all four reads. A hash/identity check is **not** complete
   semantic validation of every resource/process JSON value. No empty or
   fabricated resource/process content is accepted as completed evidence here.

This does not newly promise globally finite metadata or exact numeric Python
runtime types in every JSON field. Previously documented parser limitations,
actual honest values and independent filled-instance review remain necessary.

## Correct chronology and narrow no-schema-code-change conclusion

Database outeree46 has only the four-file `OUTPUTS` allowlist34–37. Its
`Evidence` constructor189 refuses any other path. At565–566 it inspects absence
of those four leaves **and RUN/LAUNCHER.json** before allocation/child launch.
That pathname check is not a completed-receipt content producer or consumer.
The frozen outer never creates, writes, reads or parses completed-receipt
contents. Its REQUEST parsing is a separate prospective contract.

Its670–683 process summary is explicitly
`PRETERMINAL_PARENT_OBSERVATIONS_NOT_ROOT_LAUNCHER_RECEIPT`, with
`outer_exit_intent` rather than a self-observed exit. PLAN177–185 directs root
to author the external receipt after actual tool completion and references the
closer PLAN's schema without prescribing the obsolete array key. Therefore the
**schema memo alone requires no database-outer code/allowlist change**.

For this outer, outer0 may collect runner1; neither implies application PASS.
The generic closer predicate allows integer outer_exit0 or1, but that does not
make `completed=true` truthful for an ee46 actual outer1/HOLD, late close failure,
timeout/interruption or ambiguous settlement. Actual tool outcome and final
evidence must be reconciled; no preterminal intent supplies a future exit fact.

The field map's section2 chronology qualification102–108 is accurately retained:
its frozen inner PLAN's historical “before invoking” receipt wording is not
permission to precreate a completed LAUNCHER.json. Separate prospective intent
outside E is not that completed receipt. Its section8's b16774ff/cffc6935 and
associated source acceptance are **historical**, not current whole8437/0b21
acceptance. The additive memo qualifies that stale reference without rewriting
history or supplying missing source/regression/instance gates.

## Separate unresolved defect, authority and accounting

The database outer's incomplete-registration variant remains independently
confirmed in `LINUX-DATABASE-OUTER-REGISTRATION-QUALIFICATION.md`
(`4737f349ca9c44314685492f66b4961759d056691ecf5fd97454b61d81a74f77`), JSON
`326ed288776d5bfcf127d63341aaf2ae28ae21a939146fe75a01544257d82537`.
It is conditional stale registration/redundant close, not demonstrated FD reuse,
injury or a new operational HOLD. This schema acceptance neither remedies it nor
reaccepts wholeee46. The earlier own source acceptance miss is preserved there.
No source successor or extension of the four controls is preaccepted here.

Root still needs actual F01–F07, original source/producer/coordination freeze,
sole local/CI slot, resources and external cleanup envelope, then independently
reviewed original results/settlement and actual postrun C01–C07. No packet,
receipt, original pin, current absence or future execution fact was created.
The source handoff remains commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`,
tree `05014e9f635131d5db06701e4013b4b5a746465a`; source declarations are not cases.

Only necessary permanent review MD/JSON were created. No E/runtime/process/
resource probe, helper execution, cache/temp root, build, worker, daemon or
emulator; all text-read commands exited. Wrapper stop is
NOT_APPLICABLE_NO_WRAPPER_LAUNCH, not discharge of another task's obligation.
Root retains monitoring and exclusive execution ownership. Nothing unrelated
or permanent was stopped/deleted. All STOP/NO-RETRY/CLOSED, PVA-029 FAIL,
Windows failure/cleanup HOLD, PVD/hardware/mobile1017001 and protected-ref/
dependency/version/identity/signing/store/publication boundaries remain.

Review counts: zero executed controls/application/native/hardware cases and
zero closure delta.19/25 original confirmed;22/38 all confirmed (16 remain);
2/12 original suspicions (10 remain); eight PVD explanations separate from owner
decisions. No denominator is an overall readiness score.
