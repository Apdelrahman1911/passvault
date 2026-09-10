# Main/NavHost private-bus environment delta — source only

Author: `/root/android32`; independent challenge requested from `/root/editor_review`.
Root explicitly authorized this permanent-fixture safety correction before C14.
**Zero compilations, product tests, runtime probes or fixture executions.**

## Exact source identities

- Permanent path: `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`.
- Prior adopted/accepted source: SHA-256 `8ffefea4952f393b9bf8ef467b143109d660f9f5ffc58bf05803be30211922a9`, 38,703 bytes / 727 LF.
- Proposed corrected source: SHA-256 `7a0dd50d7caa6cf3cfad729f7393a0e1802fbf65b57d3cceae7c5c64ac94961f`, 40,423 bytes / 753 LF.
- Adjacent exact forward delta: `MAIN-NAVHOST-PRIVATE-BUS-DELTA.patch`, SHA-256 `f2d365596218a3d9667e05019f2c57100ef3943f7503b25a19ab68f343247758`, 3,082 bytes / 49 LF. It records only the 26 added lines; reversing that source delta reproduces the prior accepted bytes. It is evidence, not checkpoint rewrite/execution authority.
- The excluded 727-LF author draft is untouched: `CredentialMainNavHostRoomIntegrationTest.kt`, SHA-256 `7eee7bc045abcb60660203f965a33a71e6e791d9ba2c22509a52ee615040ec9d`. Earlier rejected/accepted snapshots and terminal-correction patch remain untouched.
- Source-intended helper interface read: `scripts/audit/desktop_integration_02.init.gradle`, SHA-256 `3ba456dcc48d956eb2ff266ccfb31138ce3612f2b6aa5674194f09b0d563b8ef`, lines 58–69 and 77–94. This identity is NOT execution admission. `/root/build_config` confirmed the intended private G and G/runtime are both 0700; no actual directory/socket was inspected.

## Bounded correction and source challenge

Storage/build-config independently identified that clearing each child environment discarded the outer worker's private D-Bus and native bridge guards. Private PID/network/tmp scopes do not themselves block filesystem system-bus sockets.

The new validator derives G only from the parent's canonical `G/Xauthority`, using existing `MainNavDirectory` owner/0700/no-symlink/canonical-path checks on G and G/runtime. It requires the actual parent values to equal exactly:

- `XDG_RUNTIME_DIR=G/runtime`
- `DBUS_SESSION_BUS_ADDRESS=unix:path=G/bus`
- `DBUS_SYSTEM_BUS_ADDRESS=unix:path=G/runtime/no-system-bus`
- `NO_AT_BRIDGE=1`

An explicit `Files.notExists(..., NOFOLLOW_LINKS)` rejects an occupied or indeterminate system guard. The original validated strings—not ambient defaults—are forwarded into each cleared seed/main/verify environment. `checkPlatform` repeats these checks in the parent and each child before AWT/product startup. No bus is contacted, selected, launched or deleted by this validator. No address alternatives, lists or autolaunch fallback are accepted by its exact comparison.

## Preserved limits

This is source-only inheritance hardening, **not bus isolation, no-activation, GUI, shutdown or product regression evidence**. The nonexistence observation is not race-proof against same-user changes, and parent/child environment validation does not establish how every native library chooses its bus. A correct address string is not endpoint authentication; the execution owner must separately admit actual private session/socket ownership, route/activation boundaries and cleanup. `NO_AT_BRIDGE` is best-effort native GTK bridge suppression, not Java AccessibleContext suppression or an accessibility proof.

The same one prospective logical test, three serial fresh-JVM roles, 13 ordered events, real Main/Room boundaries and accepted one-EDT-block terminal Ctrl+Q correction are unchanged. No new finding/PVA or closure credit. All prior STOP/NO-RETRY/CLOSED/held execution scopes remain; this delta authorizes no retry, build, device or helper invocation.
