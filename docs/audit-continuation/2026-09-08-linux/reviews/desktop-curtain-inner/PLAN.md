# PVU-005: fixed C5 Desktop-curtain inner source

Author `/root/android32`, 2026-09-09. **SOURCE ONLY. No import, syntax/AST check,
build, process/display probe, tool execution, runtime/cache read or GUI run.**
New paths were checked absent before creation. No fixture/product edits.

## Frozen subject and bounded purpose

- C5 commit `96f7758de9984528f4624944c0594d47fc20f14b`, tree
  `7167789b46cb29b198aed70eb5e21d9eabc4d5ec`.
- Root-created `reviews/desktop-curtain/SOURCE.json` SHA-256
  `7051258b17dcc93365d1625c8e361612c4ece8bc0b287147e7a83e113451b699`:
  1727 raw Git members; both checkout-EOL qualifications remain. Corrected raw
  object transport and its admission belong to root/editor, not this inner.
- `scripts/audit/linux_desktop_curtain_01.py` SHA-256
  `212443a73b052feb00be5154a0414a5063af99cbd7e405cb6aa91df2d6961cf5`.
- `scripts/audit/desktop_curtain.init.gradle` SHA-256
  `dc9d54532ae155a8c5a9ee6d1b54f37b63b4859e34cb4621e9f6eae30a539703`.
  Its independent source review is
  `reviews/editor-independent/DESKTOP-CURTAIN-INIT-SOURCE-REVIEW-01.json`.
- Unchanged accepted `DesktopCurtainRenderingTest.kt` SHA-256
  `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57`.
  Its one declared case is
  `secured locked Compose window shows usable synthetic unlock content after restore`.

The existing real ComposeWindow fixture remains the oracle. No surrogate
test/mock matrix, app entry point, vault, repository, clipboard operation,
renderer override, compatibility redesign or dependency change was added.
Source success alone supplies zero execution/closure credit.

## Reused machinery and deliberate delta

Frozen isolated-batch02 inner SHA
`a1da36534c856ef9a5dddab303c7ad397b3bf2e7ca5d2ab5c07f4685d8255a31`
was read/copied as inert source, never imported or executed. Reuse its reviewed
descriptor-bound file evidence, raw-source checking, original direct-child
pidfd supervision, bounded logs/resources, once-only original-wrapper stop,
PID1 settlement and failure/interrupt qualifications. The new subject is fixed
C5 and one rendering case; old provider/167-selection/fixture-producer plumbing
is absent. No old execution admission, allocation or cleanup authority transfers.

New code is the required two-stage preparation/render flow, private net/tmpfs,
three foreground GUI helpers, authenticated display/WM startup checks, bounded
owned descendant settlement and exact one-case XML/cropped-image collection.
The reused command loop adds bounded stdin for synthetic Xauthority and an
explicit non-Gradle setup mode; these tools do not create spurious wrapper-stop
obligations. GUI resources are separately capped because sockets cannot enter
the regular-file-only R inventory. No generic runner modes or arbitrary argv
entry points are provided.

## Inner/outer interface

`B` = `docs/audit-continuation/2026-09-08-linux` under the continuation worktree.

- R `/root/projects/PassVault/audit-runtime-linux-desktop-curtain01`;
  E `B/runs/linux-desktop-curtain01`; run `linux-desktop-curtain01`;
  purpose `ONE_LINUX_DESKTOP_CURTAIN01`.
- Root/editor outer `B/reviews/desktop-curtain-outer/LAUNCH.py`, request
  `B/requests/LINUX-DESKTOP-CURTAIN-01.json`, approval
  `B/reviews/verification/LINUX-DESKTOP-CURTAIN01-INSTANCE-ACCEPT.json`.
- Outer newly allocates/pins E, **E/logs, E/xml, E/pixels** (pixels empty), R,
  and unchanged regular-only TOP: checkout, home, tmp, jna, sqlite, gradle-home,
  konan, android-user, xdg-cache/config/data/state, workers. Only worker
  `desktop-curtain`, with home/tmp/jna/sqlite/xdg-cache/config/data/state children.
  All these original private directories are0700; no loader blocker.
- Inner absolute argv receives original parent `pid`, `mnt`, `net` identities.
  Initial outer unshare remains private PID/mount + private /proc; network is
  intentionally unchanged for the online preparation.
- OUTER-INTENT format `passvault-linux-desktop-curtain-outer-v1`; required values
  are run/commit/tree, source_members1727, source_representation RAW_GIT_BLOBS,
  parent_namespaces(all3), original directories and exact input images.
- INNER-PREFLIGHT has parent(all3), self(pid/mnt only), initial_net equal parent
  net, nonpropagating_mounts and mount_rows. Outer original process ownership
  stays the proved pid/mnt pair; do not expect its unshare parent to change net.
- GUI-PREFLIGHT is written **after** verified private net/lo/tmpfs, **before**
  gui_attempted/auth/helper launches. It contains parent, original self pid/mnt,
  initial_net/private_net, loopback_up, tmpfs_private, original tmpfs pin/row,
  bounds, display/screen and authority path. GUI-READINESS is separate actual
  startup evidence, not prerequisite for cleaning a failed GUI startup.

Exact14 inner input images: SELF, INIT, SOURCE, JDK17 bin/java and release,
`/usr/bin/{Xvfb,xauth,xfwm4,dbus-daemon,mount,ip,xdpyinfo,xprop}` and
`/usr/lib/x86_64-linux-gnu/xfce4/xfconf/xfconfd`. The latter is an installed
session-service prerequisite, not a guessed direct foreground launch. Outer
additionally binds its own Python/transport/unshare/etc images and authority.

## Fixed lifecycle and commands

1. Prove fresh PID1/private /proc, unchanged initial network and raw C5 source.
   Online `:app-desktop:auditDesktopCurtainPrepare` via the fixed init, mode
   `passvault.audit.desktopCurtainMode=prepare`. Lazy Test FileCollections retain
   compile/resource dependencies without depending on/running Test itself.
   Resolve runtime classpath (including currentOs Linux Skiko/JNA), require the
   compiled selected class and retain artifact hashes. Empty NO-SOURCE resource
   directories remain valid. Original wrapper `--stop`, then full `{PID1}`
   settlement are mandatory before network transition. No held cache borrowing.
2. `os.unshare(os.CLONE_NEWNET)`, then `/usr/bin/ip link set lo up` and
   `ip link show lo` UP readback in that namespace. Only then mount new /tmp:
   `mount --no-mtab --internal-only --types tmpfs --options
   mode=1777,strictatime,nosuid,nodev,size=67108864,nr_inodes=4096
   --source tmpfs --target /tmp`. Check distinct device, tmpfs/nonpropagation,
   bounds and empty contents. **Never open/cache a host /tmp descriptor and
   reuse it after mounting.** No host X socket/lock is adopted or deleted.
3. Under `/tmp/passvault-desktop-curtain01`, fresh0700 session/home/XDG/temp
   directories; `xauth source -` with XAUTHORITY pointing to a fresh0600 file
   and `add :88 . <synthetic32hex>` on bounded stdin. Retain only the authority
   file hash/pin, never cookie argv/content. No -ac/xhost or ambient authority.
4. Owned original-pidfd Xvfb:
   `Xvfb :88 -screen 0 1024x768x24 -nolisten tcp -auth <authority>`.
   Require private socket and lock PID matching this original child; one
   `xdpyinfo` under exact DISPLAY/auth checks connection/screen/depth.
   Owned private foreground bus:
   `dbus-daemon --session --address=unix:path=<private runtime>/bus --nofork --nopidfile`.
   Own its socket, then launch exactly bare `/usr/bin/xfwm4`.
   Reject original helper exit/daemonization; root `_NET_SUPPORTING_WM_CHECK`,
   the returned window's self-check and Xfwm4 name are checked via fixed xprop
   queries. No PID-only WM-readiness claim. Actual iconification/restore belongs
   to the real fixture, not these startup checks.
5. Exactly once, offline:
   `:app-desktop:desktopTest --tests com.passvault.desktop.security.DesktopCurtainRenderingTest --offline`,
   fixed init render mode. Required worker startup properties, non-headless
   JDK17,512m, one fork and2min Test-task timeout are in the init. Private worker
   roots start empty; its XDG runtime/auth/bus point only into private /tmp.
6. Original wrapper `--stop` runs despite validation failure/cancellation.
   Stop original WM/bus/Xvfb via original pidfds; then capture remaining members
   only of this positively established, initially empty private PID/mnt/net
   domain with stable birth/namespace/exe/cwd and original pidfds. Bounded
   TERM/KILL settlement handles bus descendants; no process-name matching,
   process-group kill or host-PID fallback. All original waits, log finalization
   and descriptor closes must succeed. Then require full `{PID1}` settlement.
   Kernel destruction of the owned namespace releases private socket/tmpfs
   resources; outer alone cleans allowlisted R after its actual child wait and
   source/evidence checks. No widening cleanup to arbitrary sockets/symlinks.

All Gradle invocations retain reviewed flags: JDK17, checked-in wrapper,
non-daemon, max-workers1, no parallel/configure-on-demand/configuration-cache/
build-cache, in-process compiler, strict dependency verification and no SDK or
toolchain auto-download. This does not select packaging, signing or publishing.

## Bounds, evidence and honest failure cleanup

Existing overall6000s/cooperative outer envelope remains controlling, not a
promise that every per-command maximum fits additively. Prepare3600s, render
600s (separate from the2min Test task), each original stop600s, setup queries20s,
socket readiness10s, original-helper and descendant TERM/KILL tails10s each;
shared namespace-settlement budget120s. Native/EDT/kernel stalls can defeat
userspace deadlines; original outer cancellation/kill/wait limits still apply.
Existing12/8GiB disk and25/20% available-RAM floors/periodic sampling remain.
Private /tmp64MiB/4096inodes has8MiB/64inode free floors. Only GUI helpers and
their descendants inherit a4MiB per-file cap, not Gradle/downloaded JARs.

Retain five cropped PNGs plus observations.txt, total<=12MiB, one exact
non-skipped JUnit case, compact bounded logs, source/tool/runtime-artifact hashes
and cleanup records. Images are preserved on failure, not decoded or visually
accepted by this script. Runtime JAR hashes are not proof of which native
library was loaded; the observed render API and independent evidence review
remain necessary. Finite settled pixels are not transient-zero-leak/hardware or
full-app authentication proof. An omitted opt-in/skip never earns GUI success.

INNER-RESULT format `passvault-linux-desktop-curtain-inner-v1` retains original
five safety booleans and adds gui_attempted, render_attempted,
net_private_before_gui, tmpfs_private_before_gui, gui_helpers_settled,
pixels_preserved and gui_helpers. **gui_attempted is set only after isolation,
before the first actual auth/helper attempt**; render_attempted is intent, not
executed-case proof. Cleanup exit0/1 needs all original source/stop/settlement/
evidence guards plus preserved partial pixels and helper settlement. If GUI was
attempted both private flags must be true. If GUI was unstarted, helpers are
empty, render_attempted false and exit1; private flags need not be invented.
Thus a safely settled prepare/setup failure can discard its new cache without
claiming GUI success. Pass additionally requires the actual one-case XML and
all six expected capture files. Uncertainty is70/HOLD, never cleanup authority.

## Review and remaining admission facts

Independent outer author `/root/editor` challenged a missing-original-pidfd
cleanup hole in the first inner draft: a fast-terminal helper could be polled
before successful pidfd capture. Accepted and corrected source-only: each
started helper without its original pidfd now latches cleanup uncertainty,
all helper polling/terminal gates require that descriptor, and evidence records
whether capture succeeded. Whole-namespace containment may still settle work,
but cannot clear the HOLD or authorize deletion; no numeric-PID rescue is added.
The stale archive-only diagnostic now says raw source. No subject execution,
syntax/import check, runtime/cache access or test credit accompanies this fix.

Independent reviewer `/root/editor_review` then identified a preservation
contract gap: child-produced pixel files were stable-read/pinned but not
file/parent-fsynced. `/root/editor` independently confirmed no downstream outer
guard; neither review claims observed data loss. Root authorized only the
minimal inline correction. Exact pre-correction source is retained inertly as
`INNER-BEFORE-PIXEL-FSYNC.txt`, SHA-256
`45c0784e27650ea185efb82c84a96bd69a212ca93badcf79d0d618a9565eb756`.
The sole 16-line inner delta reopens each original image descriptor-relatively,
checks its original file/path pin, fsyncs that file and original pixel parent,
rechecks identity and closes exactly once **before** evidence adoption. No bytes
are rewritten/copied. Any error, including close uncertainty, prevents
pixels_preserved and forces final HOLD. Init, fixture, commands, XML contract
and admission are unchanged. This new frozen delta awaits independent fix review.

Independent init source review is already separate; new inner/plan review is
requested from `/root/editor_review`. Source-only readiness qualifications:
installed xvfb-run/xauth, session service and mount/ip declarations ground the
core command choices. Bare xfwm4 is from installed Client0_Command, **not** a
documented foreground guarantee; actual startup must establish that. Installed
xdpyinfo/xprop manuals/completions were absent; their minimal prospective
readiness argv/parser remains for independent source and essential-run review,
not a claimed completed CLI check. No help/probe campaign or alternate WM is
introduced. File presence/API build configuration does not prove capability,
linking, network/mount permission, offline completeness or renderer viability.

Fresh root/independent exact-instance admission, original source/tool/lock pins,
sole-slot coordination, corrected outer source transport and actual worker/
display evidence remain unstarted here. Linux02's release was scheduling only.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 no automatic retry, G7/G8 CLOSED and all
old execution/recovery restrictions remain. No old helper/runtime replay, CI,
license/Store/dependency change, publication authority or closure credit follows.
