# Database01 completed-launcher receipt — additive schema qualification

Author: `/root/storage`; 2026-09-09 UTC. **SOURCE/DOCUMENTATION ONLY;
independent challenge pending.** Root owns the actual instance and receipt.
This memo creates no request, approval, receipt, exit fact or execution authority.
It does not modify the frozen database outer, its PLAN or the earlier field map.

## Exact inputs and scope

Paths below are relative to W=`/root/projects/PassVault/passvault-linux`.
These are observed file-byte bindings, not a clean-worktree or whole-program
acceptance. The build source remains the handoff commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`, not current continuation changes.

| Input | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| `docs/audit-continuation/2026-09-08-linux/reviews/linux-runner/LAUNCH_DATABASE.py` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37477 /715 |
| `docs/audit-continuation/2026-09-08-linux/reviews/linux-runner/LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` | 11441 /200 |
| `docs/audit-continuation/2026-09-08-linux/reviews/verification/LINUX-DATABASE-INSTANCE-FIELD-MAP.md` | `0edf9d585ee15f62477ededce368407ce74a73712f94d3d36e789e453eacb2e3` | 25416 /400 |
| `scripts/audit/linux_database_closeout.py` | `8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517` | 59609 /1100 |
| `docs/audit-continuation/2026-09-08-linux/reviews/linux-closeout/PLAN.md` | `0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d` | 34814 /561 |

The earlier field map remains historical source/data guidance. In particular,
its section8 helper b16774ff/PLAN cffc6935 identities and associated source
acceptance are not current whole-helper acceptance of8437/0b21. This memo
qualifies only the completed-launcher schema against the exact current pair.
It neither rewrites that history nor supplies missing regression/instance gates.

## Two distinct receipt members, not two meanings of one key

Fixed namespaces:

- RUN=`W/docs/audit-continuation/2026-09-08-linux/reviews/linux-runner`.
- E=`W/docs/audit-continuation/2026-09-08-linux/runs/linux-database-01`.
- R=`/root/projects/PassVault/audit-runtime-linux-db-01`.
- The prospective completed receipt is **RUN/LAUNCHER.json**, outside E.

The actual root-authored receipt must contain both distinct members:

1. **`evidence` is the directory string E**, fully expanded to the absolute
   path above. It is not an array, output-file pathname or RUN directory.
2. **`evidence_files` is an ordered list of exactly four objects**. Each object
   has exactly `path`, `sha256`, `identity`; `path` is the corresponding absolute
   RUN pathname in this order:

   | Order | RUN leaf |
   | ---: | --- |
   | 1 | `LAUNCHER-stdout.log` |
   | 2 | `LAUNCHER-stderr.log` |
   | 3 | `LAUNCHER-resources.json` |
   | 4 | `LAUNCHER-processes.json` |

These four files are under RUN, **not E**. Their hashes and full regular-file
identities must be observed from the actual original durable outputs, never
prefilled: `dev,ino,uid,mode,nlink,size,mtime_ns,ctime_ns`. Each is bounded to1MiB.
Empty stdout/stderr can be valid observed evidence; absent resource/process
evidence is not. The existing `REQUEST.outer_launcher.evidence_paths` list is a
different prospective admission field; this memo does not rename it.

## Both corrected consumers agree

Source proof, not execution:

- Closer `run_contract`, lines514–528, requires the fixed completed-receipt
  identity, including **line516 `launcher['evidence'] == str(E)`**, original
  runtime/runner argv, observed exit/interval relationships and ten bindings.
  Lines530–534 then use **`launcher['evidence_files']`**, checking list/count4,
  each row's exact three-key set and the exact fixed ordered RUN paths.
- Closer `admission` invokes that contract at line658. After the intervening
  original-target/evidence, directory, device and interpreter checks, its
  second consumer at **lines700–703** iterates **`launcher['evidence_files']`**,
  calls `dirs.read(row['path'], MIB, durable=True)`, requires equality to each
  recorded identity and SHA-256, and retains the captured bytes/pins. The
  directory string is not iterated as file-evidence rows.
- Current closer PLAN lines232–267 states the same distinction: directory
  `evidence=E` at239 and ordered four-object `evidence_files` at256–262. It also
  retains independent actual-instance review rather than claiming to interpret
  every external process/resource value or authenticate authorship labels.

There is no old-key alias or fallback. A missing `evidence_files` lookup raises
an ordinary `KeyError`; using an array for `evidence` fails directory equality.
Duplicate JSON keys are rejected by the existing `strict_json` parser, so two
`evidence` keys cannot encode this distinction. Correct spelling alone does not
prove successful full admission, truthful observations, safe cleanup or a pass.

## Why no database-outer code correction follows from this schema memo

The frozen ee46 outer does **not create, write, read or parse receipt contents**.
It does inspect receipt **pathname absence**: lines565–566 require all four
outputs and RUN/LAUNCHER.json absent before output allocation and Popen.
`OUTPUTS` at34–37 contains only the four external files; `Evidence.__init__`
at188–195 admits only those leaves. Thus the memo does not rename an outer
receipt producer/consumer or amend its fixed output allowlist.

Its process summary at670–683 is explicitly
`PRETERMINAL_PARENT_OBSERVATIONS_NOT_ROOT_LAUNCHER_RECEIPT` and records
`outer_exit_intent`, not its own observed exit. Frozen outer PLAN177–185 already
delegates the completed receipt to root and the closer PLAN's schema; it does
not explicitly prescribe the obsolete array key. Root must observe actual tool
exit before authoring the completed receipt. Outer0 can collect runner1; neither
number alone is application PASS. Late close failure, interruption, timeout or
ambiguous settlement must not be normalized into successful completion.

The field map's section2 chronology qualification still applies: preserve the
frozen inner PLAN's historical wording, but do not precreate LAUNCHER.json with
future exit facts. A separate bootstrap/intent outside E is not a completed
receipt. No current absence, original inode or future completion is asserted here.

**This is not blanket clearance of ee46.** The separately raised possible
partial directory-registration variant is outside this schema qualification
and requires its own independent disposition. No unrelated outer correction,
current whole-source viability or regression is inferred from this memo.

## Remaining authority and resource boundaries

The actual database F01–F07 and postrun closeout C01–C07 packets, current source
and producer freeze, original coordination/identities, sole local/CI slot,
external time/cleanup envelope and independent result/settlement review remain
separate. This memo supplies none of them. It grants no import, syntax probe,
control, build, test, deletion, retry or CI authority. PVU-007 STOP, PVU-011 NO
RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED, Windows failure/cleanup HOLD,
PVD decisions, hardware gaps and unchanged mobile1017001/publication limits remain.

Only bounded source/data reads and this permanent documentation were produced;
no target program was imported/executed and no actual receipt was generated.
No build/cache/temp runtime, worker or daemon was created. Wrapper stop is
NOT_APPLICABLE_NO_WRAPPER_LAUNCH; this is not discharge of any prior obligation.
Source-only preparation adds zero executed controls/application cases or closures.
