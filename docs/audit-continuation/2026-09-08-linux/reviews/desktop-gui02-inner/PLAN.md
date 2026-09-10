# GUI02 inner — fixed curtain1 + rendered-editor3

Author `/root/editor`, 2026-09-10. **SOURCE CANDIDATE ONLY; NOT EXECUTION ADMISSION.**
The workload publication, complete raw-blob manifest, actual object store,
tool/lock/device/instance and cleanup admission are pending. No GUI02 source has
been executed, imported, syntax-probed or compiled. No old helper is replayed.

## Frozen candidate source and predecessor

All paths are relative to `/root/projects/PassVault/passvault-linux`; `B` means
`docs/audit-continuation/2026-09-08-linux`.

| Candidate | SHA256 | Bytes / LF |
| --- | --- | --- |
| `scripts/audit/desktop_gui_02.init.gradle` | `cdc6ffe9c5cef7e4d8cf156fd78fdf653f88f23dd8496f7a7ff616552d372c08` | 8988 / 163 |
| `scripts/audit/linux_desktop_gui_02.py` | `4d7750ad6342ca85c036b81f111022c2fdb8ccaa7b3909500bbb9d668d2790de` | 67912 / 1152 |
| `B/reviews/desktop-gui02/SOURCE.json` **placeholder, not a manifest** | `380921986806346adf572f67568d2bacf1d29bbc7bcf358d601439322a3988d8` | 1371 / 25 |

`COMMIT`, `TREE`, `MEMBERS` and the future manifest SHA are intentionally `None`.
Entry rejects unbound values before intake/allocation/work; an all-null source
placeholder cannot satisfy the required complete `passvault-linux-checkout-source-v1`
manifest. Root must publish the editor fixture, capture the exact source, retain
both checkout-EOL qualifications and obtain a new exact source/instance review.
This candidate does not assert membership in the old C5 or frozen C10 workload.

Unchanged GUI01 remains preserved at its existing paths:

- Init `dc9d54532ae155a8c5a9ee6d1b54f37b63b4859e34cb4621e9f6eae30a539703`;
  independent init review `B/reviews/editor-independent/DESKTOP-CURTAIN-INIT-SOURCE-REVIEW-01.json`,
  `87ab8368e7c0135c50f97934a6e6b027f2f6d43d6e556d2c3b2fc62dfa2a2722`.
- Inner `212443a73b052feb00be5154a0414a5063af99cbd7e405cb6aa91df2d6961cf5`;
  independent inner review `B/reviews/editor-independent/DESKTOP-CURTAIN-INNER-SOURCE-REVIEW-01.json`,
  `31ea6e055a6d8c03c5130a55d7b4019d64a24d1e3b18ee506aaa03c74fadc7b0`.
- GUI02 copies these accepted source primitives, not their execution authority.
  Original PID1 ownership, command supervision, helper pidfds/readiness, cancellation,
  stop/settlement, resource and evidence-adoption bodies remain except the fixed
  two-selection/classpath/XML/pixel loops and explicit pending-binding guard.

## Fixed workload, not a matrix

| Finding | Original Test task / selected class | Worker / evidence | Task budget |
| --- | --- | --- | --- |
| PVU-005 | `:app-desktop:desktopTest` / `com.passvault.desktop.security.DesktopCurtainRenderingTest` | `desktop-curtain` / `E/pixels` | 2 minutes |
| PVA-007, PVA-031 | `:shared:desktopTest` / `com.passvault.shared.credential.CredentialEditorRenderingTest` | `desktop-editor` / `E/editor-pixels` | 8 minutes |

The curtain fixture remains
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt`,
SHA `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57`.
The editor fixture remains
`shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRenderingTest.kt`,
SHA `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a`.
Its independent source acceptance is
`B/reviews/editor-independent/EDITOR-RENDERING-FIXTURE-SOURCE-REVIEW-01.json`,
SHA `b64b160ec2db158c9211d15c94cf2f99fae78a5bc959bd0c922acd0c77f05ffb`.
Neither fixture is edited by this batch wiring.

One online `:auditDesktopGui02Prepare` resolves/compiles both original Tests'
dependency-bearing class roots/runtime classpaths without depending on either
Test task. The graph must contain **zero** Test tasks. Exactly two identified
`DESKTOP_GUI02_RUNTIME_CLASSPATH=` records are required, each at most512 artifacts
and256MiB per regular artifact, with actual Linux-x64 Skiko and JNA in each.
Existing `core:designsystem` Desktop and `core:crypto` dependencies supply those
declarations; no dependency substitution/change or native-loading proof is implied.

After the original wrapper stop and empty owned namespace, the unchanged private
network/tmpfs/Xvfb/WM/D-Bus setup runs. Then exactly this task selection runs once
offline, with task-local filters and `shared.mustRunAfter(app-desktop)`:

```text
:app-desktop:desktopTest --tests com.passvault.desktop.security.DesktopCurtainRenderingTest
:shared:desktopTest --tests com.passvault.shared.credential.CredentialEditorRenderingTest --offline
```

These are two tasks in **one Gradle invocation**, not two concurrent commands.
The pinned inner `COMMANDS`, `FLAGS`, `ENV` and `STOP` are the exact prospective
argv/environment: checked-in wrapper, JDK17, one worker,512MiB test heap per
separate fork,2GiB build heap, no daemon/parallel/configure-on-demand/build cache/
configuration cache, strict dependency verification, in-process compiler,
no toolchain/SDK auto-download and no `--continue`. An empty/unexpected Test graph
fails; neither up-to-date nor cached Test output can stand in for execution.

Each worker has a separately validated fresh HOME/TMP/JNA/SQLite/XDG root.
Only its selected display/evidence properties are installed. The editor worker
starts with `en_US`, empty locale variant and Compose accessibility enabled;
ambient accessibility-disable/JVM/session variables are removed. There is no late
locale mutation or accessible-action substitute. The fixture still must establish
actual bridge, controls, geometry, input and renderer behavior; missing opt-in skips
and opted-in setup failures are **not passing cases**.

**Detekt is not included.** Its required `git ls-files` inventory is not admitted
for this Git-free raw-blob checkout. Preserve that gap; do not fake Git metadata,
use a fallback inventory or relabel compilation as static-analysis evidence.
The already executed49 logical editor cases are not rerun or counted as rendering.

## Exact evidence and failure semantics

Two XML sources, under each module's `build/test-results/desktopTest`, must map
exactly to the pinned class/suite and the four declared method names in `SELECTIONS`.
All cases use the existing `[desktop]` convention; suite totals, duplicate/missing/
extra cases, failures, errors and skips are checked. Retained XML filenames have
fixed `app-desktop--` / `shared--` prefixes to avoid collisions. Readable partial
or unexpected XML is preserved within the existing aggregate8-file/2MiB-per-file
bound, but never accepted as selected success. Raw XML, not task counts or an
attempt flag, remains executed-case authority.

The four meanings are: curtain native lock/restore; native pending row edit then
page Save and fresh form load; row Save/Cancel tuple semantics and fresh load;
capacity50/49 Add plus retained composed dialog input and accepted Save/load.
Editor persistence is a deep-copying in-memory repository with a real new VM and
production form, **not Room/disk or full NavHost navigation**. The capacity
interference is explicitly test-controlled direct VM add/remove, not a second
editor, external writer, backend acknowledgment or deterministic pre-frame race.

- `E/pixels`: unchanged five curtain PNGs plus `observations.txt`;12MiB group,
  4MiB read cap per member. This directory must start completely empty.
- `E/editor-pixels`: exactly `01-page-save-reload.png`,
  `02-row-save-cancel-reload.png`, `03-capacity-retained-input.png`,
  `04-capacity-save-reload.png`;1MiB each,4MiB group. It is a **sibling**, never
  a subdirectory that would violate the curtain's empty-directory assertion.
- Nine PNGs plus one text observation,16MiB aggregate. Existing whole evidence
  limits remain80 files/112MiB. No application binary/archive retention is added.

Original producer channel/parent force does not replace root's original-directory
pins, stable bounded read/hash, reopen/force/close-before-adoption or whole-owned-
namespace settlement. Partial captures are retained on failure. Oversized/unsafe/
unreadable evidence or uncertain close/settlement stays HOLD; nothing is silently
deleted or retried. Passing XML/capture hashes still need independent semantic
review; editor screenshots supplement assertions and are not pixel/OCR oracles.

## Unchanged containment and external limits

`R=/root/projects/PassVault/audit-runtime-linux-desktop-gui02`;
`E=B/runs/linux-desktop-gui02`; private session `/tmp/passvault-desktop-gui02`.
Authenticated Xvfb`:88`,1024x768x24 and64MiB/4096-inode private tmpfs remain fixed.
No ambient display/session, real vault/backup/clipboard/signing material is used.

Prepare ceiling3600s; render ceiling900s; each original wrapper stop ceiling600s.
The editor's120s soft case timer and5s EDT waits are not hard native/process bounds.
Outer6000s total/5250s work cutoff/750s drain remain authoritative even if a slow
preparation leaves insufficient render time. No automatic extension or retry.
Entry disk/RAM floors12GiB/25%; running8GiB/20%; private R inventory6GiB remains.
Resource point samples are not ownership or cleanup proof.

Root alone owns the one local/CI slot and all execution/cleanup. GUI02 retains
`agents_quiescent:true` and `no_ci:true`; no parallel-source interpretation is
inherited. Every phase's original stop, helper/orphan settlement and exact source/
authority checks are still required. The outer may remove only its newly
allocated, completely inventoried R after safe exit0/1; failure/uncertainty70
remains HOLD. E evidence, permanent tests, shared caches/toolchains and all old
scopes are never cleanup targets. Blocking syscalls can defeat userspace deadlines.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
all consumed/HOLD scopes and PVD product boundaries are unchanged. This is
**four prospective cases, zero executions, zero new closures**: no full-app
authentication, universal zero-exposure, mobile/IME/RTL, Apple/Windows or physical
hardware claim. Author operations created only the assigned permanent source and
plan files, with no runtime/cache/temp artifacts or persistent workers.
