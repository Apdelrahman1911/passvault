# Android32 prerequisite assessment — independent source review 01

Reviewer: `/root/verification`; assessment author: `/root/android32`.
Review date: 2026-09-09. Scope: source and retained data only.

**ACCEPT SOURCE ASSESSMENT WITH THE TWO QUALIFICATIONS BELOW; NOT EXECUTION
ADMISSION.** This accepts the classification of remaining work and the useful
narrow next steps, not a target, supervisor, build command, license acceptance,
archive, actual ABI result or PVA-001 closure. No product finding is added.

## Frozen input and method

Reviewed the complete `../android32/ANDROID32-ADMISSION-PREREQUISITES.md`,
**20,564 bytes /292 physical LF**, SHA-256
`5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089`.
The containing commit/tree reported by that assessment are
`bb094f8a39ba43f1ce1f43f394cb662b03febe38` /
`82967f050816454b6905d560dfec15d0e0b091f5`. This review does not infer a current
whole-tree freeze, clean worktree or refreshed remote refs from those identities.

All **13 repository/catalog/schema input hashes** listed in its table were
independently rechecked unchanged. SDK license-marker and NOTICE observations
are retained author evidence, **not fresh reviewer reads of `/opt/android-sdk`**.
Additional bound inputs:

| Input | SHA-256 |
| --- | --- |
| `../android32/TARGET-FEASIBILITY-PROPOSAL.md` | `8609e0f903d28501160f63fe2294879ae1d936aecc74b934dbf305e8c7e0a772` |
| `core/domain/src/commonMain/kotlin/com/passvault/core/domain/model/ValueTypes.kt` | `9935405eef7911b34cd28a5aebd367fd1592927466e6347a4a0344129000edbd` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` |

The harness, manifest, crypto Gradle configuration, raw adapters and C declaration
were read completely. Engine `deriveKey`, fixed Argon2 vectors, application
packaging/signing guards, SensitiveText encoding and three vault caller sites
were focused source reads, not full semantic reviews of their containing files.
The selected catalog record and its retained license text were reread, together
with the already accepted exact-schema review. No catalog recapture or SDK
consumer execution was used. Earlier truncated combined output was supplemented
by bounded reads; it is not counted as unseen full-file semantic review.

Reproducible tool references include `0f7856` (exact assessment), `4b63b9`
(13 hashes, proposal hash and adapters), `88fd21` (complete harness/build and
target-budget excerpts), `379535` (catalog record/complete accepted-schema review),
`02b7eb` (license/skill), and `8ddbea`/`b57fc2` (strict text boundary/callers).
Some guessed caller-path lookups returned missing-file diagnostics; those paths
provided no evidence. Corrected actual paths above were subsequently read.

## Two source/scheduling qualifications

**Q1 — strict text encoding belongs to callers, not the byte API.** Section 3's
phrase “public deriveKey path retains strict caller text encoding” must not be
read as a claim that `deriveKey(ByteArray, ...)` validates UTF-8. Its body accepts
arbitrary nonempty bytes, validates salt/profile bounds and lower-hex encodes the
bytes before the native call. The binary vector deliberately contains `ff`.
`SensitiveText.toUtf8ByteArray()` rejects malformed UTF-16; inspected create,
password-change and unlock callers use it before `deriveKey`. These are separate
boundaries. Preserve both; do not add byte-level UTF-8 validation or alter
lowercase ASCII-hex compatibility to repair this wording. This is not a new
product defect or a reopening of PVD-002.

**Q2 — Android priority does not depend on first executing the database lane.**
Section 4's “After root settles/releases the database slot” is acceptable only
as shorthand for root granting the **idle sole audit slot after any actually
owned job is settled**. It must not create a prerequisite to execute the
currently unadmitted database job, whose closer/control prerequisites remain
unresolved. Android task/source mapping can proceed without a runtime slot;
configuration/package execution still needs its own admission and sole owner.
No competing local/CI execution or bypass of database cleanup is authorized.

Both qualifications were sent to the assessment author for challenge. The author
confirmed them in an additive `/root/android32` message on this review turn:
`deriveKey` neither decodes nor validates UTF-8, the `00ff01` vector is intentional,
and “after database slot” was scheduling shorthand, **not** a prerequisite to
execute/complete the unadmitted database job. Root may prioritize Android in the
idle sole slot while preserving all actually outstanding settlement obligations.
The frozen assessment remains unchanged. The two agreed qualifications do not
prevent using its remaining-work classification with these explicit limits.

## ABI, reachability and contrary cases

- The adapter obtains `crypto_pwhash` from the **already loaded sodium proxy's
  native library**. Pointer arguments are `Memory`; lengths and operations are
  boxed 64-bit `Long`; only `size_t` memory limit selects boxed `Int` for width4
  or `Long` for width8. The C declaration agrees. Source `memLimit` is a checked
  nonnegative `Int`, not an arbitrary unsigned value. `NativeLong` is not a
  portable replacement on Windows LLP64. This is source-contract evidence, not
  ARM calling-convention alignment, symbol/linkage or loader execution proof.
- The four fixed intended cases are `nativeRuntimeIs32BitAndLoadsSodium`,
  `historicalBinaryPasswordVector`, `historicalTextPasswordVector`, and
  `productionProfilesMatchReferenceVectors`. They contain respectively **0, 1,
  1 and 2 intended KDF calls**. The last case covers ops3 and ops4 at64MiB; the
  middle cases use8KiB. Constants match the retained Argon2 test source and its
  independently referenced upstream CLI vectors. No reference CLI or KDF was
  executed for this review.
- Process32 and JNA pointer4/size_t4 checks precede all KDF calls. Runtime JNA
  VERSION is reflected rather than inlined from compileOnly5.19.1. The sodium
  version is queried through the same proxy library. `use32bitAbi`/`multiArch`
  preferences, directory labels and an x86_64 JVM test are contrary cases: none
  establishes a32-bit Android process. A historical Android JNA5.18.1 observation
  is not a newly resolved classpath and does not authorize dependency changes.
- The proposed tested-APK inventory must bind actual `libsodium.so` and
  `libjnidispatch.so` bytes to exact resolved AARs. ELFCLASS32/little-endian/
  ET_DYN with EM_386 or EM_ARM establishes only those header facts; ARM features,
  exports and successful loading remain distinct. The optional own-symbol maps
  row and Java JNA version are not native-dispatch version/ELF-hash proof.
- The harness rejects filters/shards and accepts only inert additional-output
  metadata; it emits fixed start/pass/failure statuses and terminal counts.
  Partial output, zero-case XML, task exit0 or only four case names cannot replace
  reconciliation of all four exact starts/passes and successful terminal result.
  An independently reviewed actual runner/parser is still missing.
- Neither a package build nor the four passing vectors would establish
  create/unlock/wrong-password/password-change, credential round-trip, V2 or
  independently seeded legacy backup compatibility. x86 is not ARM32; ARM32
  evidence need not be physical if an appropriate emulated runtime is admitted.
  Hardware/Keystore behavior remains separately hardware-bound. PVD boundaries
  and minified-application evidence remain separate.

## Trust, license and authority assessment

The selected retained record is exactly API24/default/x86 revision8,
`x86-24_r08.zip`,313,489,224 bytes and SHA-1
`c1cae7634b0216c0b5990f2c144eb8ca948e3511`, under the report's fixed Google HTTPS
URL. The independently accepted sys-img2/01 → repository-common/01 checksum
chain, SHA-256 `a97a44c24580df4e65c1d96ba1dc71666d9d6bf7ed437bdb4550ec384dd94acb`,
supports SHA-1 interpretation. Digest length, an invented attribute default or
substitution of common/02 would not. No repeat capture is needed.

HTTPS origin validation, exact length and this legacy digest can be the
explicitly accepted bounded trust policy; they are not a publisher signature or
modern collision-resistant authentication. Local SHA-256 would bind received
bytes, not create an independent publisher digest. **An unavailable upstream
SHA-256 is not automatically a mandatory blocker.** No archive is downloaded or
integrity-qualified by this review.

The retained41-byte license-marker observation, SHA-256
`c43fa37686457c3f18caa3607945f4ec52a9d1beaaad8117e50dc4e863270c85`, means the report
must not claim no marker exists. It does not prove the accepting principal,
entity authority, exact agreement coverage or SDK canonical text normalization.
Raw/trimmed-text digest differences therefore do not prove a license mismatch.
Root has already asked the owner whether existing acceptance covers the selected
API24/default/x86 agreement/use; this review does not ask again or answer for
them. Do not manufacture metadata or use a no-op `sdkmanager --licenses` as
acceptance evidence. Component NOTICE headings are not a complete private-copy
rights analysis; a reviewed read-only installed-tool strategy is a possible
alternative, not yet admitted or categorically unavailable.

Existing validation authority allows useful internal work to continue. Exact
task mapping, supervisor/control implementation, containment assessment and
closeout design are **unfinished internal work**, not proof that Linux/32-bit
software emulation is impossible. The installed API35/x86_64 image is unsuitable;
historical `/dev/kvm` absence does not disqualify every software backend.
Capabilities have not been tested here. Owner/license clarification and genuine
hardware evidence are narrower external needs, not blanket reasons to stop
source preparation.

## Prospective commands and resource ceilings

The assessment correctly avoids inventing generated Android device task names.
`:core:crypto:tasks --all`, if needed after source/metadata mapping, is a **build
configuration invocation**, not inert data or automatic permission to compile.
It may resolve plugins and needs separately frozen admission. The existing
application `verifyDebugComposeResources`/`assembleDebug` path neither packages
this library instrumentation nor supplies its native inventory. Debug is
unminified; bypassing release-signing guards or rebuilding occupied1017001 is
not an allowed shortcut.

The following are proposals, **not implemented enforcement or admission**:

| Track | Preserved ceilings and distinction |
| --- | --- |
| Task discovery |5min work + separately reserved2min stop/cleanup; must not auto-chain package |
| Harness package |15min work +2min stop/cleanup; exact realized task graph/identities still required |
| Either build |4GiB aggregate owned RSS; one worker;4GiB new disk;16MiB combined logs;32MiB retained evidence; launch12GiB/25% floors **plus** growth reservation, running8GiB/20% |
| Optional preparation-only slice |One fixed GET; archive about299MiB + at most2.5GiB extracted + bounded metadata fits proposed3GiB new storage; common15min preparation deadline; no boot/install/KDF |
| Preserved complete target |6GiB aggregate private input/runtime growth; launch18GiB free; running8GiB/20%;4GiB aggregate RSS/two-core CPU; phase A15min and phase B15min; no deadline reset, fallback or automatic retry |

The smaller preparation slice does not waive full-target bounds or admit an
indefinite staged-image cache. Validate archive/partials for prompt cleanup;
defer bulk tool copies until an admitted boot is imminent. Boot remains exactly
API24/default/x86/r8, acceleration off, one vCPU/1GiB guest RAM and the preserved
private15937/5566/5567 ADB/emulator ownership scheme. Environment variables,
`--one-device`, free ports and monitor samples alone do not establish private
network/device/process containment or hard aggregate resource limits. Source
implementation, inert adverse controls, actual identities, interrupted cleanup
and root's fresh sole-slot approval are required before any invocation.

JDK17/checked-in wrapper, non-daemon/one-worker, configure-on-demand disabled,
serial Detekt and strict dependency verification remain binding. Even task
discovery needs preinstalled cleanup, wrapper `--stop`, owned-worker settlement
and allowlisted output cleanup. These statements authorize none of them now.

## Accounting, fences and reviewer settlement

This review adds **zero executed tests, native calls, build tasks, mock cases,
qualified closures or conclusive suspicions**. Current denominators remain
19/25 original confirmed closures,22/38 all confirmed closures,2/12 original
suspicions; eight PVD explanations/owner decisions remain separate. PVA-001 is
OPEN. No readiness percentage follows. Historical22/37 text in retained earlier
reports remains historical, not the current all-confirmed denominator.

PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 recorded FAIL/no automatic retry and
G7/G8 CLOSED scopes remain intact, as do old-runner/helper restrictions, protected
refs, signing/publication boundaries and occupied mobile build1017001.

Only permanent compact review files under this reviewer's directory are written.
All foreground source/data readers exited. No project/helper import, build/test,
download, SDK/emulator/ADB probe, process probe, private data access, cache,
temporary extraction, runtime directory or background worker was created by
this review. No fresh disk/RAM admission is claimed; root owns resource/slot
checks. Wrapper stop is not applicable to these reads, and this observation
does not settle any other owner's obligation.
