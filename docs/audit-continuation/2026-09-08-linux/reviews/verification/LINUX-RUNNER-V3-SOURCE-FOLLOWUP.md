# Linux runner v3 independent source follow-up

Reviewer: `/root/verification`. Runner authors: **`/root/storage`, `/root`**.
Disposition: **SOURCE CORRECTIONS REVIEWED; EXECUTION INPUTS/PREREQUISITES PENDING**.
This is not `ACCEPT-verification.json`, an execution admission, a control result
or an application PASS. The reviewer has not edited/imported/compiled/executed
the runner. Root is the sole build/test executor and coauthor approver, **not a
second independent runner reviewer**.

## Exact scope and preserved challenges

| Subject | SHA-256 |
| --- | --- |
| `scripts/audit/linux_database_validation.py` / exact `linux-runner-frozen-v3.py.txt` | `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279` |
| `reviews/linux-runner/PLAN.md` | `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f` |
| Preserved `LINUX-RUNNER-DRAFT-CHALLENGE.md` | `d7bdc6cd38bb9ca20d872770c9145049334ae56dd17717bdba7d2cd37d050d6d` |
| `METHOD-INVENTORY.json` revision2 | `40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979` |
| `METHOD-MAPPING-REVIEW.json` | `0d57268fb29e7c4ce9ade0d11231e321651b2307c2faef6e456af6e50c4587c5` |
| build-config `LINUX-M03-PACKET.json` | `0ce080b824c1ac14684cf6e6fa3529f8c159f8b668a29be70656c308d8df045b` |
| build-config `LINUX-M03-V3-REVIEW.json` | `2754aca02b5223f0ea8cebaabbe0a676b42abd74c1865a38f9c2051f8714d224` |

The runner has55,614 bytes/1,079 LF. The initial918-LF source was read fully;
complete later diffs and changed directory/process/command/admission/terminal
components were inspected. V3 process code and final command/cleanup tail were
read in full, not credited merely from a matching hash. The initial report and
v0/v2/v3 snapshots remain historical evidence; its old singular-author wording
does not override the corrected actual authorship above. Counterexamples are
**source schedules**, not observed incidents or extra PVA families.

| Challenge | V3 source disposition and limits |
| --- | --- |
| D01 stop authority | Cleanup now rehashes `gradlew`, wrapperJAR and wrapper properties against original SOURCE before the sole stop. The earlier full prebuild check alone did not bind these later mutable inputs. Downloaded distribution/dependency verification and actual stop remain execution evidence. |
| D02 acquisition failure | `new_file` closes its acquired fd if parent fsync fails. Command selector/log/pipe resources use ExitStack and direct children are registered immediately after Popen. Exceptional close/acquisition failure is not evidence of successful explicit settlement; actual launcher exit remains necessary. |
| D03 zombie completion | Positively same-birth/UID zombies are non-live/non-signal, not fabricated live-cwd churn. Direct completion requires the actual unreaped child's observed poll result. Generic ownership errors are not silently converted to completion. |
| D04 second process sample | Both passes now carry complete PID/birth rows; relevant PID reuse and cwd/parent churn are explicit HOLD. Unknown activity grants no ownership or signal authority. Point samples cannot prove no later/escaped/short-lived activity. |
| D05 SQLite/Gradle configuration | Exact new official JVM loader source and Gradle/AGP documentation are now bound through the M03 packet; the previous missing-JVM-loader qualification remains. See trust limits below. |
| D06 mandatory controls | Root explicitly retained the F04 prerequisite: new independently reviewed, separately admitted meaningful exact-source controls are required before the database run. They are **not waived** and not yet executed here. |

The additional fresh-parent schedule was independently sustained: a stale
sampled `(PID,start)` parent must not anchor a child of a replacement parent.
V3 brackets refreshed child birth/ppid/pidfd capture with fresh observations of
the original live parent, and closes an acquired pidfd on rejection. The typed
same-birth direct-completion exception does not admit a different birth.

The clone now uses explicit `--local --no-hardlinks`, not an outside-R
upload-pack ownership exception. This reviewer read fixed argv and source guards,
not actual Git subprocess transport behavior. Local-copy concurrent Git-store
mutation remains a coordination limitation; postclone commit/tree/member checks
are mandatory. An installed Git manpage lookup was unavailable; no alternative
clone, network fetch or speculative ownership exception was used.

## F01–F07: source credit is not final acceptance

- **F01:** Sticky failures/cancellation, durable pre-fork original stop
  obligation, one stop attempt, masked launch/terminal commitment and a
  preterminal non-PASS RESULT are present. A terminal row alone does not prove
  external process exit or semantic PASS. Signals after the defined final
  masked observation and hard interruptions remain expressly outside stronger
  guarantees; root must bind adequate outer cleanup grace.
- **F02:** Same-image parse/hash, originally sealed directory/lock/input pins,
  under-lock comparison and bounded original1198-source reads precede first
  mutation. Actual REQUEST/SOURCE/original pins and external launcher have not
  yet been accepted by this reviewer. Historical macOS tuples are not authority.
- **F03:** No recursive deletion is implemented. Retention is not completed
  cleanup. Independently accepted prompt closeout **design before build**, then
  exact postrun instance admission/cleanup results, remain required.
- **F04:** No-follow, single-link, bounded pre/open/post reads and ancestor
  checks have meaningful negative schedules, but are not a hostile-UID atomic
  snapshot. The separately authored new controls still need independent source
  approval, root launcher admission, actual outcomes and external settlement.
- **F05:** Direct/live-parent anchored ownership, pidfds/narrow unreaped-child
  fallback, fresh pre-signal binding and unknown/HOLD policy are source credit.
  No real process signaling, ancestry, waitpid or worker settlement has been
  tested by this reviewer. Unrelated transient process churn may conservatively
  HOLD; it is not automatic retry authority.
- **F06:** Current code verifies actual reviewer roles, runner authors,
  instance/purpose, all eight bindings and F01–F07 dispositions. It correctly
  distinguishes owner/coauthor approval from this independent review. Cooperative
  authorship records are not cryptographic reviewer authentication. No normative
  future-execution acceptance is created by this follow-up.
- **F07:** The seven classes/five source files/105 exact methods and literal
  `[desktop]` display rule stay frozen. Seven nonempty exact XMLs, counters,
  unique exact names/classnames and no failures/errors/skips are required.
  Neither command exit nor mechanical XML mapping is independent semantic PASS.

Current controls source at this checkpoint is verification-authored v3
`320f363e5f9bdce0e806a624f7e9fd9fc943690be57f8c75c9f43f865c04903d`,
with design `1292305d4a0f1729be7016002c9f1bc99f37487077aea4f4f3d1aa1854723921`.
Its22 declarations are **zero executed controls**, not105 application cases or
the nineteen historical inert specifications. Storage's C-FD-01/C-FD-02 source
challenges and v0/v1/v2 afterimages are preserved in that separate design. This
reviewer cannot independently approve its own controls; their pending
other-agent review and root execution remain separate.

## M03 scope and rejected overclaims

The accepted focused packet is source/configuration evidence, not F01–F07 or
execution approval. SQLite2.6.2 JVM loader sourceJAR SHA-256 is
`3749d31ce6ca154e91e842f3b9b8ce39bee897b8d9dc3526ef053b42b23bae8d`;
the reviewed loader source SHA-256 is
`f41b2db9551afa478c2eedd2d7ed49b447a2ffe74f0133275fcb1622197d840a`.
Its search tries System.loadLibrary, optional androidx.sqlite options and
java.home/lib before resource extraction. **Only that extraction fallback**
uses Files.createTempFile/java.io.tmpdir. `org.sqlite.tmpdir` is not used by
this loader. Root explicitly accepts installed JDK/SDK/system-library trust,
not hermetic/native-binary provenance or proof of which branch actually loads.

Official Gradle9.7.1 docs support the two `-D` toolchain auto-detection/download
prohibitions; the provisional claim that they required `-P` was rejected.
`-D...installations.paths` remains a non-authoritative hint. Exact AGP9.4 source
supports `-Pandroid.builder.sdkDownload=false`, not an OS-read-only SDK. Fixed
JAVA_HOME, inherited worker options and exact owned argv must be reconciled
after execution; intended512MiB Test heap/2GiB Gradle heap are not total RSS caps.
The earlier ad-hoc-reader disclosure remains, rather than retroactively treating
all old native-loader data reads as newly admitted ones.

## Bounded runner–closeout integration review

This is only protocol integration, **not** the full independent closeout safety
review assigned to `/root/baseline_coverage` or an execution acceptance.
Inspected closeout v1 source SHA-256:
`a0001a0e5e9e399f7a9f27f627564cdfc99753b9143a35b9dd6bc0d79c7029c4`;
PLAN SHA-256:
`15b2fb04baeba8e849bf50f8c3ff6cfdb67f8c355b89e6e5706df9e1eda2a696`.
The full reviewer reported REVISE for journal integrity and bootstrap
cancellation/time-bound issues; this integration note cannot override them.

- Original allocations line up: **35 = R + checkout +33 targets**. The33
  targets are11 private temporary/cache roots plus22 generated roots. Every
  allocation_original immediately follows its matching durable allocation
  intent; original parent/final pins are reconciled. Partial allocation remains
  HOLD, not a fallback adoption/deletion mode. R, checkout, `.git`, source,
  schemas, permanent tests and all reports/test-results subtrees stay retained.
- Command-result/intent labels, eight run bindings, role-aware original
  acceptances, preterminal RESULT and terminal/launcher schema are consistent.
  A failed build/XML can still allow cleanup only if separate ownership,
  original stop/no-Gradle, preservation, fresh-process and closeout gates pass.
- The no-Gradle branch is source-consistent: no database/stop commitment or
  observation, no started database child and all stop flagsfalse, plus full35
  allocations and `source_bound(before)`. `Run.settled` startsfalse and is set
  only by `Processes.settle()`: three consecutive empty live samples, direct
  child polls and no sticky unknowns; exception leavesfalse. Closeout requires
  `RESULT.owned_settled=true` even in this branch.
- That RESULT is a **source-bound boolean**, not preserved raw three-snapshot
  or waitpid evidence. RESULT/source_bound/completed launcher **alone are not
  fresh deletion authority**. Closeout's new conservative process snapshots,
  original lock and root producer/CI coordination attestations remain mandatory.
  Historical owned births come from owned_process or completed-direct rows
  with a start value, not bare launch_observed PID numbers. No birthless old
  exit grants current ownership; no host-wide/escaped-worker guarantee follows.

## Remaining admission and evidence

Root must supply and this reviewer must separately inspect the exact actual
checkout SOURCE (1198 unique Git mode/blob/checkout hash/size entries, **both
PowerShell CRLF qualifications**, not an811-member raw manifest), prospective
REQUEST and original toolchain/parents/lock/evidence tuples, final exact
launcher/slot/time/resource/cleanup admission and actual other-agent controls
review/outcomes. A new capture tool is separate authoring, not yet reviewed
capture output or a reason to infer SOURCE identity.

PVA033 stale/final-publication semantics, PVA034 accept-only discriminating
budget oracle and PVA035 finite real Room/backup versus helper/provider limits
remain as recorded in METHOD-MAPPING-REVIEW and SOURCE-REVIEW. Physical
authenticator/security behavior, native loader provenance, provider/schema
breadth, race exhaustiveness and Android32 are not proven by this host selection.

Read-only resource sample at2026-09-08T23:33:47Z: worktree available
25,002,516KiB, `/tmp`22,308,972KiB, MemAvailable46,505,672KiB of65,855,360KiB;
no swap. Root was notified of the ambient worktree free-space decline. This
author generated only small permanent source/review evidence, no build outputs,
caches, worker/daemon/server/emulator; no cleanup of unrelated objects occurred.
Gradle stop is NOT_APPLICABLE to this source-review task. One combined display
was truncated; subsequent focused reads covered the affected runner/PLAN and
integration components rather than claiming credit for unseen output.

PVU007 STOP, PVU011 NO RETRY, PVA029 FAIL/no automatic retry, G7/G8 CLOSED and
all real-data/signing/publication/candidate1017001/PVD restrictions persist.
This work adds no application execution/closure and does not change19/25,
22/37 or2/12 denominators. Every actual later result remains independently
adjudicated, including failure, cleanup HOLD and genuinely unstarted cases.
