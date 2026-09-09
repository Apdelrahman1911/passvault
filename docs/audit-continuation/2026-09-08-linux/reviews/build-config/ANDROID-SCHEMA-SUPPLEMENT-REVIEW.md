# Android32 schema/launcher supplement — independent bounded challenge

Reviewer: `/root/build_config`. Proposal author: `/root/android32`.
Review point: `2026-09-08T23:51:58Z`. **SOURCE/DATA ONLY; NOT EXECUTION ADMISSION.**

## Disposition

**ACCEPT the quoted schema interpretation conditionally; complete schema chain
remains independently UNVERIFIED. ACCEPT the inspected launcher warning.**
The proposed target/preparation/boot is still **NOT ADMITTED**, with zero
application cases. The earlier catalog review's algorithm uncertainty remains
historically accurate; this supplement does not retroactively rewrite it or
declare that uncertainty fully resolved.

## Exact inputs and method

| Input | SHA-256 | Independent observation |
| --- | --- | --- |
| `../android32/TARGET-FEASIBILITY-PROPOSAL.md` | `8609e0f903d28501160f63fe2294879ae1d936aecc74b934dbf305e8c7e0a772` | Complete document read as inert text; selected schema excerpts only |
| `../android32/SDK-CATALOG.xml` | `ebf2d810d9e0c0b511ae49ee6e8c671a8fa67d210d06f5e3e068f83244ece435` | Hash rechecked; actual root line17 and selected package lines2086–2110 read |
| `../android32/SDK-CATALOG-RESULT.json` | `abe9675dde3224b784c928b2df33103e23c18ed487f51a9f7b8895e9c3e1177b` | Hash rechecked; earlier independent selection preserved |
| `ANDROID-CATALOG-INDEPENDENT-REVIEW.json` | `e62700e1ec0ff6c3e5d8dc0775d0dd9bc78624cca3ad2e67f46354abf99a8c3b` | Complete prior reviewer report read; unchanged |
| `/opt/android-sdk/cmdline-tools/23.0/lib/sdklib/tools.sdklib.jar` | `d0bbe0e03155a7bdd9bac03567b6b26797f81a778f55d5eadc3e71e7a0a27434` | 1909475-byte containing archive rehashed, **not decoded** by this reviewer |
| `/opt/android-sdk/cmdline-tools/23.0/lib/repository/tools.repository.jar` | `9fd419f3307be633aaf7b02b52832521c1300d4bf9058cee83bb11139990a7f9` | 272256-byte containing archive rehashed, **not decoded** by this reviewer |
| `/opt/android-sdk/cmdline-tools/23.0/bin/sdkmanager` | `5705db235fe2b11628e064b825b41afaa8eb42823d276454bff8f4145e8081f5` | Full 8365-byte/328-LF shell source read, not executed |

No new archive reader, network request, JAR import, schema-validation engine,
Java/SDK/ADB/emulator/Gradle process or image download was introduced. Shell
source was read with `sed`; archive hashes were computed as opaque file data.
Local file/hash observations are point reads, not hostile-mutation isolation.

## Schema challenge and missing surrounding definitions

1. The **actual retained catalog** root namespace is `sys-img2/01`, not a newer
   schema chosen by convenience. The selected API24/default/x86/revision8
   record still declares a complete archive with 313489224 bytes, checksum
   `c1cae7634b0216c0b5990f2c144eb8ca948e3511`, and `x86-24_r08.zip`. Its checksum
   has no explicit algorithm/type attribute. No selected ancestor shown in
   lines2086–2110 overrides the complete/checksum type with `xsi:type`.
2. The retained **quoted** system-image schema names the matching namespace,
   imports `repository/android/common/01`, and names `common:repositoryType`
   for the root. The quoted common/01 checksum definition documents SHA1 and
   imposes forty hexadecimal characters. **If those declarations are on the
   applicable archive chain**, they support SHA-1 semantics without guessing
   from digest length or assuming a missing default attribute.
3. That qualification is material: the retained excerpt omits the complete
   `repositoryType` → remote package → archives/archive → complete/checksum
   linkage. It also does not retain the common/02 contrary definition itself.
   Containing-JAR hashes cannot prove what an unread member/range says, which
   type is reachable, or that an XML validator/consumer uses that declaration.
   This reviewer has not independently recomputed the three member hashes.
4. On follow-up, the author confirmed that **no complete inert schema packet
   exists**. Earlier ZIP reads hashed full member bytes but showed bounded
   ranges in tool output. The full linkage was not retained/inspected in
   model-visible source; common/02 appeared only in earlier tool stdout, not a
   retained source packet. The author correctly declined to claim that member
   hashes substitute for inspecting the missing chain.
5. Minimal remaining work is a newly accepted, bounded local **data-only** read
   of the three exact fixed XSD members and full relevant linkage, retaining
   their exact text/hashes for independent review. Do not import SDK classes,
   run SDK tools, resolve XSD imports over a network, execute an old helper or
   treat this report as admission for a new reader. No new remote location is
   needed for this local gap if the installed, already-hashed JARs suffice.

Even after that chain is established, SHA-1 is a legacy integrity comparator,
not a modern collision-resistant publisher signature. There remain no fetched
archive bytes, matching archive digest, license acceptance, extracted image,
boot, guest/process ABI proof or four native KDF outcomes. A locally calculated
future SHA-256 is a byte binding, not an independently published upstream hash.

## Launcher challenge

The complete installed `sdkmanager` shell confirms the proposal's warning:

- Help says requested packages are installed or updated to the latest version.
- `run_android_cli()` dispatches to `ANDROID_CLI_BIN` when set, otherwise the
  sibling `android`, with SDK arguments; it is not the old JVM entry point.
- `--licenses` prints that the option is no longer needed. With that option and
  no package/special command, the script exits0 before invoking the CLI. Such
  exit0 is **not a license-acceptance operation or proof** in this wrapper.
- Proxy arguments warn that default system proxy configuration is used. Merely
  passing old proxy flags cannot establish the proposed network restrictions.
- This is shell-source evidence only. The Android CLI implementation, actual
  dependency selection, network effects and SDK-install behavior were not
  executed or independently inspected here. No assertion about their complete
  behavior is being adopted from wrapper help.

These observations support avoiding this wrapper as a shortcut for the exact,
single-revision preparation. They do **not** independently approve a replacement
ZIP installer, authorize license acceptance, or establish emulator/ADB option,
network/device, process/descendant, cgroup or cleanup behavior. Proposed monitor
budgets remain different from hard resource containment. Root retains the
single execution slot and all fresh source/control/cleanup admission duties.

## Preservation, accounting and resources

Only this permanent compact report was added. Accepted catalog/result/reviews
and application/configuration source were not changed. A hardware-property
search using an overly specific spacing pattern returned no match (exit1);
the source was then read as text. That diagnostic is neither an SDK failure
nor a hardware-property verification. No whole hardware-config review is claimed.

At the review point, workspace free space was24712844KiB, `/tmp`22078072KiB;
MemAvailable45977340KiB of65855360KiB. These are stale point observations, not
execution admission. No temporary extraction/cache/runtime namespace, persistent
worker or build output was created; Gradle `--stop` is **NOT_APPLICABLE here**,
not satisfaction of any previous obligation.

PVA-001 remains OPEN; **19/25 original confirmed, 22/37 all confirmed, 2/12
suspicions** remain unchanged. Eight original design explanations remain separate
from owner decisions. PVU-007 STOP, PVU-011 NO RETRY, PVA-029's recorded FAIL/no
automatic retry, G7/G8 CLOSED scopes and candidate1017001 restrictions are intact.
