# PVA-001 — one isolated API24/x86 software-emulator feasibility proposal

Author: `/root/android32`. **PROPOSAL ONLY; NOT EXECUTED; NOT ADMITTED.**
Root is the sole executor, slot owner and publication owner. This document does
not authorize a GET, license acceptance, SDK/image installation, emulator/ADB,
Gradle, application test, new host privilege or CI run. Existing accepted source,
`REPORT.md` and `SDK-CATALOG*` bytes remain unchanged.

## 1. Smallest useful target, not a matrix

Prepare exactly `system-images;android-24;default;x86`, revision8, then attempt
**one** synthetic cold boot with CPU acceleration explicitly disabled. Determine
whether this installed emulator can reach a usable API24/x86 guest within a
bounded interval and then settle all owned processes/storage cleanly. Do not
install PassVault or run a KDF in this feasibility probe. A successful boot is
zero application test cases and cannot close PVA-001.

This target meets declared minSdk24. `/dev/kvm` was absent at22:11:08Z; the
installed API35/x86_64-only image remains rejected. Neither absence of KVM nor
an i386 backend filename proves software emulation impossible or usable. No
fallback API, ARM image, alternate renderer, larger RAM profile, emulator
downgrade or automatic retry is part of this proposal.

The source-only follow-up observed continuation commit
`f18995e6633904a6845e09eab8a481635290894d`, tree
`8948bb6c92b5baaf406f838b48bf8e31064d1195`. That containing commit is not an
execution freeze; root must bind the actual selected source and new supervisor
after review. The three accepted test/config hashes remain:

| File | SHA-256 |
| --- | --- |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `Android32KdfInstrumentation.kt` in `androidDeviceTest` | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| `androidDeviceTest/AndroidManifest.xml` | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` |

## 2. Exact artifact and checksum/permission gates

The frozen official catalog is `SDK-CATALOG.xml`,183,268bytes,
SHA-256 `ebf2d810d9e0c0b511ae49ee6e8c671a8fa67d210d06f5e3e068f83244ece435`.
Its independently challenged selection is recorded in
`../build-config/ANDROID-CATALOG-INDEPENDENT-REVIEW.json`; that earlier review's
checksum-algorithm uncertainty is preserved, not rewritten.

| Requested artifact | Fixed value |
| --- | --- |
| HTTPS GET, only after separate admission | `https://dl.google.com/android/repository/sys-img/android/x86-24_r08.zip` |
| Exact expected archive bytes | `313489224` |
| Catalog checksum text | `c1cae7634b0216c0b5990f2c144eb8ca948e3511` |
| Package/API/tag/ABI/revision | `system-images;android-24;default;x86` /24/default/x86/8 |
| Catalog license | `android-sdk-license` |

**New local source evidence, independently review before relying on it:** the
captured catalog root is `sys-img2/01`. Installed SDK schema data for exactly
that namespace imports repository common/01, whose archive checksum is
explicitly documented as **SHA1**, with a40-hex restriction. This is not an
inferred algorithm from string length, an XML default attribute, or the newer
common/02 checksum type. Exact source references are in section8. Acceptance of
this schema chain would resolve the algorithm interpretation only. There are
still **no archive bytes, checksum match, publisher signature or installed
image**. SHA-1 is a legacy integrity comparator, not modern collision-resistant
artifact authentication. Record SHA-256 of received bytes for later binding;
that locally calculated hash is not an independent upstream SHA-256 signature.
If root's artifact trust policy requires a stronger upstream digest/signature,
obtain a verified publisher source; do not invent one or weaken the policy.

Before any download/install, root must document authority to use the exact
catalog license. `SDK-CATALOG-RESULT.json` retains normalized license text
SHA-256 `de5fa465e908e7f098e718082f013135b9a36ee706d032cf510f4f1153424b2a`;
the raw parsed text, including its final LF, is
`1f8729233617b193fd619213792ae16a41b95d2bbbf525dfe66998252ba68b16`.
Both differ from raw serialized XML bytes. Do not run `yes`, manufacture an
acceptance file, reuse another user's acceptance, or imply that reading this
license accepts it. License acceptance has not occurred in this lane.

**Do not use this installed `sdkmanager` as a shortcut.** Its23.0 shell source
routes to the Android CLI, documents install/update-to-latest behavior, and
treats `--licenses` as no longer needed. It is not the old JVM launcher assumed
by many examples. A package name alone would not pin revision8 or bound network
access. The proposed alternative is explicit, newly reviewed staging of the
one verified vendor ZIP into a private SDK layout; this is a disclosed proposal,
not a silently substituted missing installer or a claim that staging has run.

## 3. Coordination, private paths and toolchain

Before either phase, root must reserve the **single global audit slot** in
`../../EXECUTION_SLOT.json`, reconcile any local/CI activity and obtain the
current independently admitted Linux lock. The current Linux runner proposes
`/root/projects/PassVault/.audit-coordination-linux-20260908/build.lock`;
its existence/path is not adoption authority. Root must pin its current parent
and file identity under review and hold the same exclusive lock through the
active phase and process settlement. No agent may start a competing build,
inert runner, emulator, server or CI job. The prior Windows FAIL/cleanup HOLD
and all original CLOSED obligations are not resolved by reserving this slot.

Proposed single-use paths, **not created by this proposal**:

```text
I=/root/projects/PassVault/audit-input-android32-api24-r8-01
R=/root/projects/PassVault/audit-runtime-android32-api24-r8-01
E=/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/runs/android32-target-01
```

Reject an occupied path, symlink, unexpected owner/device/parent, stale journal
or namespace from a previous attempt. Do not adopt, rename away or empty it.
Create0700 directories through validated parent descriptors. Bind original
directory identities and source/tool hashes before launching any producer.
`E` is permanent compact evidence and is never a cleanup root.

- `I/download/`: one bounded archive/partial file; no resume or alternate URL.
- `I/sdk/system-images/android-24/default/x86/`: verified extracted image only.
- `I/sdk/emulator/` and `I/sdk/platform-tools/`: private, byte-verified copies
  of the current installed bundles, not newly downloaded versions. No hardlinks
  or symlinks back to `/opt/android-sdk`; this avoids writes through shared
  toolchain aliases. A copy-on-write copy is acceptable only with separately
  reviewed byte/ownership checks and ordinary-copy budget already available.
- `R/avd/`, `R/android-user/`, `R/emulator-home/`, `R/home/`, `R/tmp/`,
  `R/xdg-{cache,config,data,state}/`, `R/runtime/`: private runtime state only.
  No host AVD, account, credential, clipboard, real vault or backup is copied.

Use Linuxx86_64 host executables. The installed emulator, ADB and i386-named
backend are ELF64/EM_X86_64 **host** programs; the backend name is not guest ABI
proof. Pin source and copy bytes for the full two tool bundles and their
licenses before use. Use JDK17 at `/usr/lib/jvm/java-17-openjdk-amd64` if Java
becomes necessary; the proposed probe does not call Java, avdmanager or Gradle.
No application dependency, SDK version, app identity or Store build is changed.

Construct a fresh allowlisted environment, not the inherited shell environment:

```text
PATH=<I>/sdk/platform-tools:<I>/sdk/emulator:/usr/bin:/bin
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
HOME=<R>/home
TMPDIR=<R>/tmp  TMP=<R>/tmp  TEMP=<R>/tmp
ANDROID_HOME=<I>/sdk  ANDROID_SDK_ROOT=<I>/sdk
ANDROID_AVD_HOME=<R>/avd  ANDROID_USER_HOME=<R>/android-user
ANDROID_EMULATOR_HOME=<R>/emulator-home
XDG_CACHE_HOME=<R>/xdg-cache  XDG_CONFIG_HOME=<R>/xdg-config
XDG_DATA_HOME=<R>/xdg-data  XDG_STATE_HOME=<R>/xdg-state
XDG_RUNTIME_DIR=<R>/runtime
ANDROID_ADB_SERVER_PORT=15937
ADB_SERVER_SOCKET=tcp:127.0.0.1:15937
ADB_VENDOR_KEYS=<R>/android-user/adbkey
ADB_MDNS_AUTO_CONNECT=0
LANG=C.UTF-8  LC_ALL=C.UTF-8  TZ=UTC
```

Generate an audit-only ADB key if the reviewed server requires one; never copy
host keys, console tokens or account files. Bound any generated key/token under
`R`, do not retain/upload it, and remove it on closeout. No trace/debug variable,
proxy, `ANDROID_CLI_BIN`, `JAVA_TOOL_OPTIONS`, `LD_PRELOAD`, signing/store secret,
SSH agent, inherited display or desktop-session bus is admitted. These path
settings express intended isolation; they are not proof that every SDK child
honors them or that `/tmp`, USB and networking are kernel-confined.

## 4. Resource ceilings and stop conditions

The23:05:45Z source-only observation was27,058,544KiB free on the worktree
filesystem,22,668,580KiB free on `/tmp`, and44,512/64,311MiB available RAM.
It is stale admission data, not a guarantee. A metadata-only `du` observed
840,284KiB for the installed emulator bundle and21,736KiB for platform-tools;
copying these roughly842MiB is bounded, but fresh logical-byte inventory is
required. No copy has been made.

| Limit | Proposed ceiling/floor |
| --- | --- |
| Launch free disk | At least18GiB on the actual I/R filesystem: original12GiB floor plus6GiB reserved maximum growth |
| Running free disk | At least8GiB on every writable filesystem; abort owned work on crossing |
| Host RAM | Original launch25% / running20% available floors, independently of per-job limits |
| Complete I+R logical/allocated growth | At most6GiB, with separate counters for both; no tmpfs image staging |
| Copied tool bundles | At most1.25GiB /10,000 entries; no symlinks/hardlinks/special files |
| Archive | Exactly313,489,224bytes; reject overrun, short body, redirects, range/resume or mismatch |
| Extracted image | At most2.5GiB total,2GiB per member,4,096 entries; no encrypted ZIP/member links or special files |
| Guest profile | One vCPU,1024MiB guest RAM,320x480 display, no audio/camera/SD card |
| Writable guest data/cache | At most1GiB data,128MiB cache; no snapshots, screen recording or RAM dump |
| Other runtime temporary storage | At most512MiB, inside R only |
| Host job RAM/CPU budget |4GiB aggregate resident memory and at most two host CPU cores' worth; guest vCPU count alone does not bound helper threads |
| Resource observations | At least every5seconds; inventory/allocated-size checks at most every15seconds |
| Phase A wall bound |15minutes total: GET<=540s, copy<=90s, hash/inventory/extract<=150s, cleanup<=120s, within the common deadline |
| Phase B wall bound |15minutes total: cold boot<=600s, evidence<=90s, cleanup<=120s; no clock reset on failure |
| Logs/evidence |4MiB per process stream,16MiB total logs;32MiB maximum retained evidence; no APK/image/large binary upload |

A new independently reviewed supervisor must implement these ceilings before
any invocation. Monitor samples alone are not hard kernel RAM/CPU/disk limits.
Prefer an independently admitted dedicated cgroup/process containment with
4GiB memory and two-core CPU limits; its availability/delegation is **unproved**.
Do not silently label `ulimit`, one vCPU or a process-name scan equivalent to a
cgroup limit. If only cooperative monitoring is available, obtain explicit
independent acceptance of that weaker control and its interruption/overshoot
limits first. Never change host cgroup policy, KVM access, kernel modules,
device permissions or hypervisor configuration under this proposal.

## 5. Phase A — one download and private image preparation

The next minimal executable scope, if root separately admits it, is Phase A
only. It must not auto-chain a boot before the actual archive layout and
after-images have been reviewed.

1. Install signal/deadline handling, durable intent journaling, bounded output
   capture and descriptor-bound cleanup before the first GET/copy. Record
   exact supervisor bytes, all fixed inputs, environment, root/lock pins,
   archive destination FD, resource/capability observations and independent
   admission dispositions. No such supervisor is implemented/admitted here.
2. Perform one TLS-validated HTTPS GET to the fixed URL in section2: identity
   encoding, no redirects/proxy/range/resume/retry,10s connect/20s read inactivity
   limits and540s overall, streamed in<=1MiB chunks through an exclusively
   created owned FD. Reject a non200 status, encoding, size or source mismatch.
   Never stream the archive into tool output or memory as one large buffer.
3. Compare exact byte count and the independently accepted schema-defined
   checksum; compute SHA-256. Retain compact headers, hashes, command outcomes
   and failure evidence, not an invented upstream strong digest.
4. Inspect ZIP metadata **before extracting**: reject absolute/parent/backslash
   traversal, NULs, duplicates/normalization collisions, links, nonregular
   entries, encryption, unsupported compression and all bounds violations.
   Check decompressed sizes/CRC while streaming into descriptor-bound new
   regular files. A central-directory size declaration alone is not the cap.
   No generic `unzip`, `extractall`, chmod of shared SDKs or shell archive helper.
5. Retain actual names and hashes for `source.properties`, image `build.prop`
   if present, kernel/ramdisk and system/userdata images. Require API24/default/
   x86/revision8 metadata agreement. Do not invent filenames, supply a guessed
   package.xml, patch image properties, or boot a mismatching image. Unknown
   layout/guest64/native-bridge support is a stop for source review, not a retry.
6. Copy the pinned emulator/platform-tools bundles only if the image validates
   and Phase B remains the immediate intended use. Verify the copy inventory
   and source preservation. No command-line-tools copy or sdkmanager/avdmanager
   call is needed for this minimal manually specified synthetic AVD.
7. Remove the validated downloaded/partial ZIP after evidence/image validation;
   it is no longer needed. On any preparation failure, delete only validated
   owned files through the already admitted closeout. Retain the compact error
   and never auto-redownload. If image review is pending, retain only the
   sealed private image/tool input for an explicit root-owned review window
   (proposed maximum60minutes), with no live worker. Root must book its imminent
   use or admit immediate input cleanup; do not leave an indefinite large cache.

Phase A is package/source preparation, not a successful Android build/test.
Its output adds exact artifact identities that the current catalog cannot give.

## 6. Phase B — exact single-profile proposal, gated on Phase A review

Require a new request binding the actual image/tool/config bytes and unused R.
The manually generated AVD descriptor must have exactly one owned absolute
`path=<R>/avd/pva001-api24-x86-01.avd`, target`android-24`, and no pointer to a
host AVD or other SDK. Config must bind the actual reviewed image directory,
`abi.type=x86`, `hw.cpu.arch=x86`, `hw.cpu.ncore=1`, `hw.ramSize=1024`,320x480
display, cameras`none`, audio input/output`no`, SD card`no`, explicit owned
userdata/cache paths and cold boot with no saved snapshot. Exact kernel,
ramdisk, seed-image and partition settings cannot be frozen until Phase A
reveals their actual names/sizes; do not fill them by guess or silently grow
an existing data image. This is a generated synthetic AVD, not a product ID.

**Capabilities/isolation prerequisite:** a private loopback-only network scope
and device view without physical USB, KVM, camera, microphone or host clipboard
must be independently established before ADB/emulator. Device enumeration,
default ADB scanning and wireless auto-connect can touch unrelated devices even
with a different server port. An environment variable, `--one-device`, an absent
device at one instant or a free-port sample alone is not a containment proof.
Namespace/filter/cgroup availability and the mechanism to provide this view
remain unproved; obtain a reviewed implementation or a verified isolated target,
not a quiet fallback to unrestricted host ADB. No internet is needed in Phase B.

The **proposed fixed argv arrays** below are not commands to execute now. They
must be launched by the newly reviewed owner/supervisor, with the section3
environment and verified private network/device scope. Their exact CLI/env
semantics, especially server auto-start/discovery and cleanup, still require
independent source/contract challenge. Installed read-only binary help supports
the accel/renderer/port vocabulary; that is not an executed parser test.

```text
[<I>/sdk/platform-tools/adb,
 --one-device, pva001-no-physical-usb,
 -L, tcp:127.0.0.1:15937, server, nodaemon]

[<I>/sdk/emulator/emulator,
 -avd, pva001-api24-x86-01,
 -accel, off, -gpu, swiftshader,
 -cores, 1, -memory, 1024,
 -port, 5566,
 -no-window, -no-audio, -no-boot-anim, -no-snapshot,
 -camera-back, none, -camera-front, none,
 -show-kernel]

[<I>/sdk/platform-tools/adb,
 -H, 127.0.0.1, -P, 15937, -s, emulator-5566,
 shell, /system/bin/getprop, sys.boot_completed]
```

Start the explicitly foreground ADB server once, register its birth/ownership
before permitting emulator launch, and prove the listening socket belongs to
that exact process. Ports15937/5566/5567 must be available in the private scope;
port5566 is within the installed help's stated even5554–5584 range. Do not
fall back to5037, another port or `adb start-server`. Bind every client to this
server and exact emulator serial; refuse missing/extra transports rather than
listing or inspecting unrelated ones. Reconcile any child-server auto-start or
daemonization with ownership tracking; unknown descendants mean HOLD, not an
invitation to kill processes by name.

Poll the one `sys.boot_completed` property at a fixed5-second interval within
the shared600-second boot deadline, with a separately bounded per-client10s
deadline and<=4KiB response. Polling readiness is not rerunning a failed boot.
Any client/transport/ownership error stops the probe; do not auto-reconnect to
a different server or restart the emulator. Once it is`1`, use the **same exact
client prefix** to capture these bounded system properties only:

```text
ro.build.version.sdk
ro.build.fingerprint
ro.product.cpu.abi
ro.product.cpu.abilist
ro.product.cpu.abilist32
ro.product.cpu.abilist64
ro.dalvik.vm.native.bridge
```

Require SDK24, x86/32-bit ABI declarations and no64-bit/native-bridge surprise;
record unexpected or absent fields without inventing a pass. Retain elapsed
boot time, resource high-water samples and bounded startup diagnostics showing
how acceleration/rendering was selected. A command's`-accel off` argument is
intent, not alone proof that the expected backend actually ran. Guest property
configuration/kernel identity is also **not JNA SIZE_T or an installed app's
process-width proof**. The later accepted instrumentation must still fail
closed on Process.is64Bit/POINTER/SIZE_T and reconcile four exact case statuses.

No PassVault APK/install command, app launch, vault/backup/clipboard operation,
benchmark, login/account, synthetic business workflow or native KDF is admitted
by Phase B. A successful profile is only a candidate for a separately reviewed
compile/package/install/native-vector run. ARM32, production profiles/workflows,
minified packaging and physical-device security remain separate.

## 7. Mandatory settlement, cleanup and truthful outcomes

The supervisor and closeout must be **new, exact-source, independently reviewed
and inertly challenged before use**. Do not import the old runner, reuse its
helpers/lock identity, or invoke the fixed Linux database runner for this scope.
Root's own approval is owner/coauthor approval, not an independent vote.

- Before process creation, record a durable intent and establish the child
  birth/UID/process-group/cgroup or pidfd binding through a reviewed launch
  handshake; after-only PID/name snapshots are insufficient. Track ordinary
  children, emulator/QEMU/graphics/netsim/crash/ADB descendants and any detached
  births. No blanket `pkill`, `killall`, host `adb kill-server` or unrelated run
  cancellation. Names are warning signals, never killing authority.
- At success, error, timeout, signal or resource-floor breach: freeze evidence,
  stop only the birth-bound emulator/server via owned process handles, allow
  a bounded15s TERM grace, then one owned KILL escalation if still present.
  Settle/reap the whole owned set within the overall cleanup budget. Do not
  send a shutdown command to a socket unless ownership is still established;
  direct owned process handles avoid confusing a replacement listener.
- Preserve compact command/status/time/resource/ABI logs first. Zero application
  cases executed is explicit. Distinguish `PREPARATION_COMPLETE`,
  `SOFTWARE_GUEST_BOOTED`, `FAIL`, `BLOCKED_BEFORE_LAUNCH` and `CLEANUP_HOLD`;
  only an independently reconciled result plus cleanup can release the slot.
  A terminal process exit does not imply filesystem cleanup.
- With producers settled, validate original parent/directory FDs and each
  allowlisted owned output's type/device/inode/link status before unlinking,
  without following links. Remove I/R tool copies, images, AVD/data/cache,
  private keys/tokens, HOME/TMP/XDG files and empty owned directories. Remove
  only these new generated inputs/runtime paths: never source/tests/reports,
  `.git`, historical evidence, shared caches or `/opt/android-sdk`.
- A failed cleanup step must not hide the result or skip independently safe
  other cleanup. Unknown ownership/linkage/process settlement is a recorded
  HOLD; retain the exact namespace/pins for a separately admitted closeout,
  not a successful deletion claim or automatic recovery/retry.
- `SIGKILL`, controller crash and host loss can interrupt cleanup; no Python
  finally block or shell trap guarantees recovery from those events. Require
  an independent interruption/closeout contract before launch and report any
  residual honestly. The historical G7/G8 recovery scopes remain CLOSED.
- No Gradle invocation is proposed, so wrapper `--stop` is NOT_APPLICABLE to
  this target-only task. It neither settles prior stop obligations nor excuses
  the checked-in wrapper/JDK17/one-worker/strict-verification/serial-Detekt
  rules when a later application build is separately admitted.

No build1017001 replacement, dependency/version/identity change, signing,
publishing, tag/protected-branch action or store secret is part of either phase.
All PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 FAIL/no automatic retry and CLOSED
restrictions remain binding. Defined closure/suspicion denominators are unchanged.

## 8. Newly read local source facts and precise limits

All inspections below were file/ZIP-member/ELF-header/printable-string reads as
**data**. No JAR/class, SDK shell launcher or ELF was imported/executed. A
bounded-output Python data reader briefly returned an exec session, then exited0;
it left no worker, cache, temporary extraction or generated runtime directory.
Some initial schema inventory output was truncated; only the exact selected
ranges below receive interpretive credit, not whole-library review.

| Local input | Exact identity / read scope |
| --- | --- |
| `gradle/libs.versions.toml` | SHA-256 `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a`; minSdk line13 only for this proposal |
| `/opt/android-sdk/cmdline-tools/23.0/bin/sdkmanager` | 8,365bytes; SHA-256 `5705db235fe2b11628e064b825b41afaa8eb42823d276454bff8f4145e8081f5`; full shell source read, not Android CLI implementation |
| `.../bin/avdmanager` | 5,760bytes; SHA-256 `2bf31bb017c0e11413c5c679674b4fe5dcce6c0cd61d9f381fd6f8cb414efa57`; full JVM-launcher source; not invoked or used in the proposed path |
| `.../lib/sdklib/tools.sdklib.jar` | 1,909,475bytes; SHA-256 `d0bbe0e03155a7bdd9bac03567b6b26797f81a778f55d5eadc3e71e7a0a27434`; ZIP metadata and selected XSD data only |
| Its `xsd/sdk-sys-img-01.xsd` | 4,481bytes; SHA-256 `2eb33b82b4db5d24892e8b7c9ebb2eace2d309f6fc64c57103a034b7e2a9eaf5`; namespace/import/root lines35–53 |
| `.../lib/repository/tools.repository.jar` | 272,256bytes; SHA-256 `9fd419f3307be633aaf7b02b52832521c1300d4bf9058cee83bb11139990a7f9`; selected common schema data, no Java execution |
| Its `xsd/repo-common-01.xsd` | 15,469bytes; SHA-256 `91a12ddb132fbf2a56fef48030c0bd21a6032eff1e095d92d605ac8baadb151e`; namespace, archive and checksum fields |
| Its `xsd/repo-common-02.xsd` | 15,510bytes; SHA-256 `5f8b8c8b8bc3d965c4334c20d72cb83ff475012703bd922e6a3387d35efc36e3`; contrary-case checksum type/required attribute only |
| `/opt/android-sdk/emulator/lib/hardware-properties.ini` | 31,318bytes; SHA-256 `13a89cc7cfa84c5fe48cd7eb8d296bca321dcd0d90f7bcb751efd963ae2c653d`; CPU/RAM/display/camera/audio/cache/data/renderer declarations only |
| `/opt/android-sdk/emulator/lib/advancedFeatures.ini` | 28,152bytes; SHA-256 `17d1de26b5b6b71ce88c0c6e2cb5676fbaf0aa6584d71404828c7b217fb3f915`; selected hypervisor/render/snapshot defaults only; not actual Linux feature selection |
| `/opt/android-sdk/emulator/emulator` | 9,429,936bytes; SHA-256 `d430b26ab9806894b06d6a1d3750d8c8a5125300967b2bd87d76245541bc4ce1`; ELF64/EM_X86_64 header and selected embedded help strings, not code execution/whole-source review |
| `/opt/android-sdk/emulator/qemu/linux-x86_64/qemu-system-i386` | 31,971,344bytes; SHA-256 `3556253ace44046f05b9eaa34adbc955e339f7ae760f7d80912a01c3747888b1`; ELF64/EM_X86_64 header/hash only |
| `/opt/android-sdk/platform-tools/adb` | 10,642,368bytes; SHA-256 `a902be8f45c6c62e76c9efaf6947a0fa747c9cabd89a2ac8e0d16ecb30b3ed01`; ELF64/EM_X86_64 header and selected help/environment strings only |

Exact relevant schema excerpts (inert text, not a schema-validation execution):

```xml
<!-- SDK-CATALOG.xml:17 -->
<sys-img:sdk-sys-img xmlns:sys-img="http://schemas.android.com/sdk/android/repo/sys-img2/01" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

<!-- sdk-sys-img-01.xsd:36,39,49,53 -->
targetNamespace="http://schemas.android.com/sdk/android/repo/sys-img2/01"
xmlns:common="http://schemas.android.com/repository/android/common/01"
<xsd:import namespace="http://schemas.android.com/repository/android/common/01"/>
<xsd:element name="sdk-sys-img" type="common:repositoryType"/>

<!-- repo-common-01.xsd:275-285 -->
<xsd:element name="size" type="xsd:long"/>
<!-- The checksum of the archive file. -->
<xsd:element name="checksum">
    <xsd:simpleType>
        <xsd:annotation>
            <xsd:documentation>A SHA1 checksum.</xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:pattern value="([0-9a-fA-F]){40}"/>
        </xsd:restriction>
    </xsd:simpleType>
```

The emulator's embedded `-accel` help at byte399088 says `off` disables CPU
acceleration; x86/x86_64 accelerated Linux mode depends on KVM. Its renderer
help at575999 lists `software` and `swiftshader`, not the older proposed
`swiftshader_indirect` or an assumed `off` renderer. Its port help at467492
states even5554–5584 plus adjacent ADB port. ADB help at444129 describes
`--one-device` for `server nodaemon`, maximum emulator scan port and mDNS
auto-connect. Those strings support a source proposal, not the behavior of all
configuration paths. Exact environment/CLI isolation, extra listener/helper
creation, cold-boot compatibility and cleanup remain to be independently
admitted and observed, not inferred from help text.

## 9. Admission gaps, not manufactured external impossibility

1. Independent challenge of this schema/launcher/one-profile proposal, then
   source implementation and inert validation of the new exact supervisor and
   descriptor-bound closeout. No suitable Android target supervisor is supplied
   by the old runner or this Markdown file.
2. Verified license authority, SHA-1/TLS artifact-trust acceptance and actual
   ZIP identity/layout; a stronger publisher digest requires its verified source
   if mandated. No archive request or installed-image result exists yet.
3. Fresh shared slot/lock, host resource and usable network/device/process
   isolation capabilities; no KVM/privilege escalation or unrelated cleanup.
4. Exact current emulator/ADB option/environment and extra-child/listener
   behavior sufficient for the fixed commands. If installed data plus independent
   source review is insufficient, obtain authoritative documentation/source
   bound to emulator37.1.11/build15917651 and platform-tools37.0.1. No verified
   exact-build source URL was supplied here; do not invent one or silently use a
   different version's behavior as proof.
5. Only then, a newly admitted bounded Phase A followed by separately admitted
   Phase B. Neither a blocked control nor a timeout establishes that Android32
   itself is unsupported, and neither a successful boot nor clean staging
   establishes native KDF/application correctness.

There was **no new network request, archive download, license acceptance, copy,
SDK mutation, AVD creation, emulator/ADB/Java/Gradle invocation or app case**
while preparing this proposal. Root owns the decision and any next execution.
