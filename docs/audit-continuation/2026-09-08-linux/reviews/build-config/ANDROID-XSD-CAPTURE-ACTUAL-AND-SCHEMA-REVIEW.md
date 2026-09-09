# Android32 fixed-XSD capture and checksum schema — independent actual review

Reviewer: `/root/build_config`. Capture executor/admission author: `/root`.
Capture utility/proposal author: `/root/android32`.
Review point: `2026-09-09T01:26:54Z`.

## Dispositions — two narrow conclusions

1. **ACCEPT the one completed, bounded local data capture**, with the external
   observation/settlement/timing/diagnostic qualifications below. It supplied
   three exact inert XSDs, not an application test or target qualification.
2. **The previously missing static checksum linkage is now independently
   established.** For the frozen catalog's `sys-img2/01` schema contract, the
   applicable repository-common/01 archive checksum is documented as **SHA1**.
   This interpretation is not inferred from digest length, a missing attribute
   default, the newer common/02 schema, or observed SDK-consumer execution.

This supplement adds later evidence; it does not rewrite the earlier catalog
review's then-unresolved algorithm field, the conditional schema review, the
rejected capture v1, or any frozen report. PVA-001 remains **OPEN**. There are
zero new application cases and no closure/suspicion-denominator credit.

## Exact bound source and evidence

All relative Android paths below are under `../android32/`.

| Input/receipt | SHA-256 independently rechecked |
| --- | --- |
| `CAPTURE_FIXED_XSDS.py` | `44799ffc328d6427482feb31db05adebb5fe31e75f482bbf003ba5b88e6b8fe8` |
| `SCHEMA-CAPTURE-PROPOSAL.md` | `c0ca8616be5bbfad3d099e9a4e1ee7a92b6799c8ace59136550eb89fbefa16c4` |
| `SCHEMA-CAPTURE-ADMISSION-01.json` | `99ceb4adb2021aa678776d386cf97061933c8fb66a70b14fffb8842557819603` |
| `SCHEMA-CAPTURE-EXTERNAL-01.json` | `ef695dabda59ba76aaed2461a8369bff4531892c275e241fce6d24ad8bf533d7` |
| `SDK-CATALOG.xml` | `ebf2d810d9e0c0b511ae49ee6e8c671a8fa67d210d06f5e3e068f83244ece435` |
| `SDK-CATALOG-RESULT.json` | `abe9675dde3224b784c928b2df33103e23c18ed487f51a9f7b8895e9c3e1177b` |
| This lane's `ANDROID-XSD-CAPTURE-V2-SOURCE-REVIEW.md` | `a63e1a07e50d2428f3e8469a27f65daa859b72c34df3e926fa379c86a4ca9021` |

The complete admission, external record, both packet journals and all three XSD
texts were read as inert data. Hashes, physical LF counts and inventory were
checked with ordinary read-only file tools; no XML validator, capture-script
import/execution, JAR decoding, SDK command, Java, HTTP request or recapture was
performed by this reviewer. The relevant catalog root and selected record were
reread from the unchanged, already independently reviewed catalog.

The capture directory has exactly five immediate entries, all regular0600,
root-owned, single-link files; directory mode0700. Its observed inventory is:

| `schema-capture-01/` file | Bytes / physical LF | SHA-256 |
| --- | --- | --- |
| `INTENT.json` |2338 /76| `e0ca0129585244866d75cbfd2f089721d4da5f946a273c14d1c873d0cf2b39d6` |
| `RESULT.json` |8307 /320| `fb507bee59f3ae93da28e9479f7aff8c65cc0db8f77cb5e5a1adde0fcd7f053f` |
| `sdk-sys-img-01.xsd` |4481 /98| `2eb33b82b4db5d24892e8b7c9ebb2eace2d309f6fc64c57103a034b7e2a9eaf5` |
| `repo-common-01.xsd` |15469 /377| `91a12ddb132fbf2a56fef48030c0bd21a6032eff1e095d92d605ac8baadb151e` |
| `repo-common-02.xsd` |15510 /388| `5f8b8c8b8bc3d965c4334c20d72cb83ff475012703bd922e6a3387d35efc36e3` |

Total **46105 bytes /1259 physical LF**, of which the three schemas are
35460 bytes /863 LF. These are evidence-file measurements, not source-review
coverage of the whole SDK or additions to the811-file application denominator.

## Actual capture and closeout reconciliation

- Root's admission was created `01:19:11.913224Z`; the recorded one invocation
  started within its15-minute admission window. Source/proposal/reviewer hashes
  match the independently accepted tuple, and the intent binds the same two
  full JARs and three members.
- External tool chunk `149719` records **exit0**, three captured members,
  `CAPTURED_THREE_FIXED_XSDS_NO_SEMANTIC_VERDICT`, no errors and no interruption.
  The actual46105-byte inventory agrees with acknowledged completed-write bytes.
  The namespace is consumed; no automatic retry/reuse is authorized.
- The packet's reader and two input-JAR full recorded pins agree with the
  corresponding admission fields. Its reader/input hashes agree with the
  expected source identities. Root additionally records unchanged original
  source/input/toolchain pins after the capture; this reviewer did not recreate
  a past filesystem observation from present metadata.
- The present opaque-JAR and tool hashes were independently rechecked unchanged:
  sdklib `d0bbe0e03155a7bdd9bac03567b6b26797f81a778f55d5eadc3e71e7a0a27434`,
  repository `9fd419f3307be633aaf7b02b52832521c1300d4bf9058cee83bb11139990a7f9`,
  `/usr/bin/python3.12`
  `1643dacd9feaedc58f3cc581e4d22577dfe25c09b10282936186ccf0f2e61118`,
  `/usr/bin/timeout`
  `4fccd5b0192653a2446b745d5385ea547b78e466150e07ade9e2caff2b7f4e08`.
  No archive members were decoded again to obtain these containing-file hashes.
- RESULT intentionally has39 CLOSED events and two registered directory FDs
  before final receipt/closeout. The terminal external output retains those
  events and three additional CLOSED events: RESULT FD, parent directory and
  capture directory. All **42 close events** are CLOSED and terminal registry
  is empty. Reused FD integers represent separate serial ownership lifetimes;
 42 is neither a unique-descriptor count, process count nor test-case count.
- All recorded capture resource samples satisfy their floors. Initial disk
 32573751296 bytes and available RAM45062291456/67435888640 satisfy12GiB/25%;
  pre-XSD and final disk32573743104/32573702144 bytes and available RAM
 45060165632/67435888640 satisfy8GiB/20%. They remain point observations.

### Preserve limitations and the separate diagnostic failure

The settlement basis is the **normal foreground timeout exit0**, the inspected
reader's absence of child-launch paths and its terminal FD journal. **No separate
original-controller/reader PID census was captured.** This review does not
manufacture that missing evidence, prove global worker absence, or convert this
small data-read settlement into Android emulator/build/recovery admission.

Output was captured as a combined stream, not separate stdout/stderr files.
Timing scopes are deliberately separate: the tool's reported wall time was
`0.00001326s`, the program's monotonic elapsed time `0.021638s`, and the root
orchestration wall-clock window `01:19:26.138Z`–`01:19:26.300Z`. The anomalously
small tool field is not treated as program duration or a full external monotonic
proof. No timeout/CPU/signal-failure path was tested by this successful capture.

Root's subsequent **metadata-only postread diagnostic failed, exit1**, chunk
`7c67dc`, at an `AssertionError` in a whole-`stat_result` comparison. Its traceback
and disposition remain in the external record. That comparison included access
time, but the exact changing field/value was **not logged**; this review does
not assert a proven atime root cause. Root reports no receipt write/new file,
FD closure in its reader's finally, and a later corrected stable-field data read.
The metadata correction was **not a repeat of the XSD capture**. It neither
erases the exit1 nor turns it into an application/capture failure or test case.

The successful and partial-receipt failure models still assume ordinary trusted
installed tooling and cooperative source stability. Current byte matches are
not a continuous hostile-mutation proof. No deleted-cache accounting or net disk
reclamation claim follows: these five small files are permanent source evidence.
Wrapper `--stop` is NOT_APPLICABLE here; prior stop/cleanup obligations stay intact.

## Complete static checksum chain and counterexamples

### Catalog binding

The unchanged catalog line17 declares the root namespace
`http://schemas.android.com/sdk/android/repo/sys-img2/01`.
Lines2086–2110 select `system-images;android-24;default;x86`, API24/default/x86,
revision8, and the `android-sdk-license` reference. The archive's complete child
declares313489224 bytes, `x86-24_r08.zip`, and checksum
`c1cae7634b0216c0b5990f2c144eb8ca948e3511`, with **no type attribute**.
Its package/archive/complete/checksum ancestors have no overriding `xsi:type`;
the shown `xsi:type` belongs only to the sibling `type-details` element.

### Why repository-common/01 applies to this checksum declaration

| Exact retained source range | Link actually read |
| --- | --- |
| `sdk-sys-img-01.xsd`:35–53 | Matching `sys-img2/01` target namespace; `common` binds **repository/android/common/01**; its import is49; root `sdk-sys-img` has `common:repositoryType` at53 |
| `repo-common-01.xsd`:38–48 | The imported repository-common/01 namespace, with unqualified local elements/attributes; consistent with the catalog's unprefixed child elements |
| same:62–84 | `repositoryType` permits `remotePackage` with `repo:remotePackage` at78; selected record is not the alternative `localPackage` |
| same:115–130 | Remote package includes `archives` of `repo:archivesType` at127, after shared package fields/channel |
| same:153–162 | `archivesType` contains `archive` of `repo:archiveType` at160 |
| same:236–254 | `archiveType` requires one `complete` of `repo:completeType` at251; optional patches are separate |
| same:294–301 | `completeType` uses group `repo:archiveFields` at300 |
| same:267–292 | `archiveFields` contains size, checksum and URL; the checksum's anonymous simple type documents **“A SHA1 checksum.”** at280, with pattern `([0-9a-fA-F]){40}` at283 |

The full relevant linkage is now source evidence rather than an inference from
isolated excerpts or unread member hashes. The annotation establishes the
declared algorithm interpretation; the literal pattern and the selected
40-hex value are additional format evidence, not a computation/authentication
of the remote archive. No default `type` attribute is needed or declared by this
common/01 checksum definition.

### Newer common/02 is a different contract

The complete contrary schema was also read. `repo-common-02.xsd`:27–37 declares
**repository/android/common/02**, not common/01. Its `archiveFields` checksum at289
uses `repo:checksumType`; that type at268–277 extends the hex-value type and
requires a string `type` attribute at274. It declares **no default algorithm**
there and does not enumerate supported algorithm implementations. The checksum
value pattern at264 is not the common/01 definition. Substituting common/02,
guessing an omitted type/default, or calling the frozen catalog invalid on that
wrong-version basis would not establish the actual contract.

There is an additional, distinct **sdk/android/repo/common/01** import at
`sdk-sys-img-01.xsd`:50 and a `type-details` base reference at77. That SDK-common
schema was not one of the three captured members. It is not the repository-
common/01 namespace above and does not replace the checksum's inspected sibling
chain. Consequently this review establishes the **archive checksum source
contract**, not validation of the entire catalog, its complete type-details
ancestry, JAXB generation/import mapping or the installed CLI's actual behavior.
No schema import was resolved over a network and no generator/validator was run.

## Remaining Android32 work and unchanged boundaries

This resolves only the limited algorithm-interpretation/source-linkage gap.
It does **not** accept the Android SDK license, fetch/verify the313489224-byte
archive, establish publisher authenticity or a strong upstream digest, stage an
image, prove API24 software boot feasibility, inspect live guest/process/JNA
width, or execute the four declared native KDF cases. SHA-1 remains a legacy
integrity comparator, not modern collision-resistant artifact authentication.
A locally calculated future SHA-256 would bind bytes, not invent a publisher
signature. Root still needs any necessary license/trust decision, exact new
downloader/stager/cleanup admission and independent network/device/process
containment before target preparation; boot and KDF validation remain separately
admitted steps. ARM32 and physical-device security cannot be inferred from x86.

This reviewer wrote only this permanent compact report. At the review point,
workspace free space was31670792KiB, `/tmp`21209224KiB; MemAvailable43902992KiB
of65855360KiB. No temporary file/cache/build output/worker or SDK write was created
by the review. Existing accepted evidence, original/full confirmed closure
counts19/25 and22/37, conclusive suspicions2/12, eight original design explanations
with owner decisions separate, STOP/NO-RETRY/CLOSED restrictions, Windows
FAIL/cleanup HOLD, protected refs and candidate1017001/publication boundaries
remain unchanged by this work.
