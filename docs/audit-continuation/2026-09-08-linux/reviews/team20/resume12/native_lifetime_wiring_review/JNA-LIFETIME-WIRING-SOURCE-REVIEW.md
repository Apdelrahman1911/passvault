# PVA-010 one-case native-host wiring: independent review

Reviewer `/root/c20_remaining_review`; author `/root/c20_remaining_author`.
Disposition: **ACCEPT_BOUNDED_WIRING_SOURCE_ONLY**. No canonical wiring edit or
execution is admitted by this review. No build, test, graph discovery, native
load, SDK/host probe, Git, helper execution/import or cleanup was performed.

## Exact binding

The sole proposed source edit is `app-desktop/build.gradle.kts`:

| Input | SHA-256 | Bytes / LF |
| --- | --- | --- |
| Current source before-image | `381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde` | 59984 / 1472 |
| Author `app-desktop-build.gradle.kts.after.txt` | `0fd4293e5f4741a1a10ef049dc42f24e7f002d2303cb6779087eaa3bacf24ab6` | 63886 / 1535 |
| Author `app-desktop-build.gradle.kts.patch` | `9ccdc510152ee6b9c0dd814a869fea8a8980722c53cdd76fec181d43160eb4f6` | 5364 / 98 |
| Author `WIRING-SCOPE.md` | `b2ef2372e9c83e3239f043acacf5d68b8c50a4996021dbf9cadf36adb46d66a5` | 6351 / 96 |

Author paths are under `reviews/team20/resume12/native_lifetime_wiring_author/`.
Independent complete before/after comparison reproduced the supplied patch hash.
The integrated lifetime test still hashes to
`4a604009911522d2ea952b37c44a3ec19724e146b2eb781016137065bbbdedf6`.
Its existing V1/V2 packet and review `e3dcbe6ac1c07f8aedec3bd0d8f6863113a9be34d29a40722118b50f2018a20d`
were not modified; that test's safety/scope review is reused, not repeated.

## Independent challenge and source judgment

- The exact `auditJnaLifetimeTest` name is exempted from the native-host all-Test
  staging/CTest hook. Other names retain the existing hook. Only that explicit
  task is registered, only on supported native hosts; no source-level edge is
  added from root `test`, `check`, ordinary `desktopTest` or release tasks.
- Direct typed JVM test-compilation FileCollections plus `compileAllTaskName`
  avoid the mapped `desktopTest` TaskProvider producer trap. No late dependency
  clearing, broad task exclusions, generic init runner or graph listener is used.
  Reviewer requested a non-null runtime-dependency guard to avoid nullable KGP
  API ambiguity; V2 adds only that code change. Preserved V1 patch hashes to
  `8a29da5e8e23b5f5b317f5c64235178116b1689d7f0d99414a99d539478555cc`.
- Class-file and method filters jointly narrow selection. JUnit4, nonmatch
  failure, one fork at a time, 512MiB heap, one active processor, 120s task limit
  and XML-only reports are proportionate. Audit runtime results cannot be reused
  from up-to-date/build-cache state; compilation machinery is not redesigned.
- Nine root-supplied properties have no guessed nonempty defaults. Task/model
  realization does not require them. If execution reaches `doFirst`, missing
  admission fails before the Test worker forks, **after** compile prerequisites.
  NO-SOURCE, task/filter success or a skipped case remains insufficient evidence.
- Two narrow inputs bind JVM home/temp/JNA-temp and working directory without
  creating or deleting paths. This removes a separate JVM-options injector;
  it does not establish OS environment, ACLs, ownership, freshness or isolation.
  Those facts, compatible HotSpot JDK17 and actual output paths remain root-bound.

## Cost, remaining risk and handoff

This 63-net-line single-file delta is a proportionate explicit way to remove
the known blanket native-hook obstacle. It is preferable to a new general
validation framework or blanket task surgery. There is no further material
source objection. Root may retain the inert proposal within the fixed current
publication selection rather than expanding canonical changes prematurely.

Exact KGP/Gradle DSL compilation, native-host resolved dependency graph and
aggregate non-inclusion remain **unexecuted**, not proven by source reading.
Any further implicit `builtBy`/plugin edge must be challenged before invocation;
another Test, native aggregate/staging/CTest, packaging or release work is outside
the proposed graph. No host discovery or admission bypass is authorized here.

Future execution still requires an independently bound ordinary library (or
separately admitted one-target `BUILD_TESTING=OFF` native build), exact source,
toolchain/architecture, literal command, resource/time envelope, original private
environment and installed cleanup authority. Root owns one quiet build window,
compact one-case XML/log evidence, original-wrapper stop and owned settlement.
The Test timeout does not bound prerequisite compilation or prove cleanup.

Even a future passing case only adds real-FFI/managed-pre-entry lifetime evidence;
native-active/provider/sanitizer/packaged/signature/hardware obligations remain.
No closure, test/pass count or denominator changes. Original refusal, PVU-007
STOP, PVU-011 NO RETRY, PVA-029 retained 49/44PASS/5FAIL/no automatic retry and
G7/G8 CLOSED remain binding. No refused native procedure is reopened.
