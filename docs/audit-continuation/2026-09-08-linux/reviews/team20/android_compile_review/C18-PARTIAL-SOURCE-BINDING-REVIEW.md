# AndroidCompile02 — C18 partial source/policy binding review

Reviewer `/root/android_compile_review`, 2026-09-11.
**ACCEPT_PARTIAL_SOURCE_POLICY_LITERAL_BINDING_ONLY.** No instance approval or
execution admission. No discrepancy found within the explicitly released delta.

W = `/root/projects/PassVault/passvault-linux`.
B = `W/docs/audit-continuation/2026-09-08-linux`.
Author evidence directory = `B/reviews/team20/android_compile_author/c18-source-binding01`.

## Independent reconciliation

I read the shared `B/reviews/checkpoint18/source-prepare01/SOURCE.json` as data,
the three current W controls, the three preserved `*.before.txt` images, the
author's patch/receipt, and the prior independent application review. I used
standard-library text/JSON/hash comparisons, not project-helper execution,
imports, evaluation, AST parsing, builds or tests.

The independently observed SOURCE is 1,879,327 bytes with SHA256
`a34aee1c7ea19fb693adf67437ec67e8b9e0ee34232936cb76127764c7b242d3`.
Its commit is `6489252e88ad553a867d67578eff45a402e62a48`, tree is
`57d338a931ab0fb4e072aabcbbfd27bead8ef08a`, and its 3,432 rows have 3,432 unique
paths and no duplicate JSON object keys. The capture's member field agrees.
All row OIDs are 40 lowercase hex characters; the ordered, non-deduplicated
OID-plus-LF representation is 140,712 bytes, within the unchanged 147,456-byte
(144 KiB) cap. The 3,500-member ceiling is not substituted for the bound count.
SOURCE's SHA256 was unchanged on readback. These are observations of the
root-supplied evidence file, not a fresh checkout/capture or tool attestation.

The preimages match the previously accepted control hashes in
`SHARED-C18-SOURCE-APPLICATION-REVIEW.md`, independently read at SHA256
`cd09a1b74068df94db1986cb56d4ab541bba10c410b482c2a3fc0d3e3f16af52`.

| Control | Preserved preimage SHA256 | Current external control SHA256 | Bytes / LF count | Literal substitutions |
| --- | --- | --- | --- | --- |
| Init: `W/scripts/audit/android_compile_02.init.gradle` | `fccb4952a892ca5d0a223a6484d63497000b76e4a08eae8dc260d8534729bcd9` | `35beeda0a6766d406e1f18bf6740a07d5f9730658673c67397c5c16d1108b41c` | 13511 / 195 | 5 |
| Inner: `W/scripts/audit/linux_android_compile_02.py` | `a62983080e04c638754973f4a6dfe16d21ccf6bb8f0cbb2582f334aa9b85f89a` | `09137a86e2637907628e5928eef7eeaacd6aa84b47137341946ccaa6afc88580` | 64204 / 1038 | 9 |
| Outer: `B/reviews/android-compile02-outer/LAUNCH.py` | `3bfa5fff0a759c2fd11be99595949f1562f99f975ea323be1f25b2bd94eb02b6` | `153a4c6f19b61c940fc30d8f8819ee65b70af5803a28bf5252ebe9d2672d6c48` | 64272 / 1059 | 5 |

I independently constructed each allowed replacement from the root-selected
policy and SOURCE fields, required a unique literal match, and compared the
entire resulting file to the current bytes. Reversing precisely those
replacements recovers each complete accepted preimage. No other byte changed.
Changed lines are init 11,32–35; inner 37,44–46,50,79,81,83,86; outer 50,52–54,62.

My independently generated three-file unified diff is byte-identical to
`C18-SOURCE-BINDING.patch.txt`: 9,076 bytes, SHA256
`fec92e1dc54069fa739ee06325b4fd2f48ffb3c99cdc6dc8ca6c78b4a9efae88`.
`PARTIAL-SOURCE-BINDING.json` has SHA256
`775d94a0fa9e57ed519cc6f987aaaf4dde9f5edaa14adb643dd92b27b4bba1c3`;
its policy, source pins, counts, byte lengths, hashes and replacement counts
agree with these independent observations.

## Source pins and policy

Both init and inner use the following four SOURCE **raw_sha256** values. They
were derived from the C18 rows, not checkout hashes or inherited C17 pins.

| Relative source path | C18 raw SHA256 | Raw bytes |
| --- | --- | --- |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` | 13666 |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/RawPasswordHash.kt` | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` | 2519 |
| `core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/RawPasswordHash.android.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` | 2837 |
| `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` | `9c4567e4a9cd1f6023534d3b5a40dfb21981cd49cc8eff0b4d1e453429246ca8` | 14600 |

The fixture uses the new C18 `9c4567e4...` raw identity, not the old C17 identity.
All four raw sizes agree with their SOURCE Git-size fields; this review did
not re-observe their contents in T or assert fresh raw transport verification.

Init's ADMITTED and both Python COMPILE_BINDING literals encode the same
root-selected complete envelope, in this exact order:

1. `/root/projects/PassVault/audit-runtime-linux-android-compile02/checkout`
2. `/root/projects/PassVault/audit-runtime-linux-android-compile02/gradle-home`
3. `/root/projects/PassVault/audit-runtime-linux-android-compile02/tmp`
4. `/root/projects/PassVault/audit-runtime-linux-android-compile02/konan`
5. `/usr/lib/jvm/java-17-openjdk-amd64`
6. `/root/projects/PassVault/audit-runtime-linux-android-compile02/sdk-readonly`

All three set **offline=false**. This is policy binding only: no observation
of the SDK/JDK/runtime paths, no SDK download/install/license mutation, and no
claim that current tool/instance facts have been reviewed.

## External controls are not the published helper blobs

All three control paths also occur exactly once in C18 SOURCE. Their
`raw_sha256` and `raw_size` fields match the preserved **UNBOUND preimages**
(init 12826 bytes; inner 63315; outer 63569), not the newly bound W controls.
I independently checked the outer row as well as the init/inner rows listed
in the author's published-helper array. The shorter author array does not
alter the published outer identity.

The external chain is correct: inner FROZEN pins the SOURCE hash above and
the **current bound init** hash; outer FROZEN pins that same SOURCE, the same
bound init, and the **current bound inner** hash from the table. The unchanged
SELF/INNER/INIT paths refer to W external controls. The materialization logic
instead consumes C18's ordered raw Git blob identities. No SOURCE recapture,
self-reference, or replacement of published raw helper identities is called
for. Original UNBOUND comments were deliberately left byte-identical; those
stale annotations do not override the actual bound literals or the unresolved
fields below.

## Preserved guards and remaining holds

Whole-file reversible equality preserves the already-reviewed nonliteral
logic. Focused text checks also confirm matching 26-name init/inner allowlists
(original 25 plus exactly `:core:crypto:androidJar`), not 26 required actions.
The sole selector remains DEVICE. Jar must be enabled, exactly
`org.gradle.jvm.tasks.Jar_Decorated`, with all four outgoing edge lists empty
and the sole incoming relationship DEVICE.dependencies; DEVICE must also
directly depend on MAIN. Both init and collector retain these predicates.
There is no invented Jar-to-MAIN edge, Jar fresh-action/archive-content proof,
test execution or native credit. Only MAIN/DEVICE retain the fresh compiler
action/class requirements and the existing ten compiler receipt roles.

The 3,500-member guard and both outer 144 KiB OID generation/recapture checks
remain, as do the other source/blob/index/log/evidence/resource bounds,
strict-verification and JDK17/wrapper9.7.1 policy, error/STOP/no-retry behavior,
and cleanup qualifications. This is preservation of reviewed source logic,
not evidence of successful enforcement in a new run.

Still explicitly unbound:

- Inner line 49: `EXCLUDE_STATE = None`.
- Outer line 37: `EXCLUDE_STATE = None`.
- Outer line 60: `DEVICE = None`.
- Outer line 61: `EXPECTED_LOCK = None`.

The unchanged entry guards reject these unresolved facts. Root must supply
fresh tool/store/index/exclude/instance/lock facts and the exact request
before a genuine independent instance review. The reserved reviewer remains
`/root/android_compile_review`, with exact approval path
`B/reviews/team20/android_compile_review/INSTANCE-ACCEPT.json`. This report is
**not** that approval; no request or approval was created by this review.

This partial-binding pass used only bounded W source/evidence reads and this
own-directory report write: no Git/T access, SDK/tool/process/runtime probes,
helper execution, cleanup, central-ledger edits or additional agents. Earlier
disclosed filename-discovery limitations are not erased by this statement.
Consumed Compile01, GUI03/C17 controls/evidence, held runtimes, STOP/NO-RETRY/
CLOSED restrictions, native-agent refusal, protected refs/publication and
occupied build1017001 remain untouched. No new cases or closure credit.
