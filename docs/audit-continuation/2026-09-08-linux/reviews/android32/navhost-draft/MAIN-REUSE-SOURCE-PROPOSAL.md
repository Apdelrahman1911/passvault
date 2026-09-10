# Smaller real-Main alternative — source proposal only

2026-09-10, `/root/android32`; source-only. Frozen draft/C13 unchanged.

Prefer a captured fresh child invoking actual `com.passvault.desktop.main()`,
with one native AWT driver in that JVM. Seed/close the synthetic Room database
before launch; reopen in a distinct owner only after confirmed child exit.
Reuse locator/input logic, not a second harness. This removes the
fixture's lifecycle/store imports and manual root-VM teardown, not the need to
validate runtime dependencies, real navigation and durable exact tuple contents.

Main owns instance-lock acquisition, global Koin, production application loop,
`PassVaultDesktopWindow`/`PassVaultApp`, and terminal coordinator. No test
GraphOwner may also close Main's database/Koin or emulate owner locals. The
native driver records ordered success before requesting ordinary native close;
parent needs driver success AND settled child exit AND durable reopen. Exit0
alone is insufficient: Main also returns0 on instance contention and logs
cleanup timeout/failures without necessarily changing exit status. Such logs,
missing markers, native failure or forced termination must fail this case.

Scope grows to real tray/window protection/focus and shutdown hook: fresh
independent admission is mandatory. All home/tmp/preferences/XDG/display roots
must be private from JVM start; attachment terminal purge scans its tmp root.
No clipboard, picker, preview, URL or recovery actions. Actual wrapper default
width1200 requires new geometry/occlusion review. Reuse proposed512MiB/one-CPU/
240s child bounds only after review; exit does not prove descendant settlement.

Source SHA-256 (under app-desktop's desktop package):
* Main.kt: `9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93`
* PassVaultDesktopWindow.kt: `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702`
* DesktopShutdownCoordinator.kt: `7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d`
