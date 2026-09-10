# xfwm4 documentation capture — source only

Root authorized one bounded HTTPS capture of the installed package's official
Homepage, not a WM/tool probe, build, test, or execution admission.

- Installed metadata: `xfwm4 4.18.0-1build3`; Homepage
  `https://docs.xfce.org/xfce/xfwm4/start`.
- Original and final URL: that exact URL; HTTP **200**, no redirects.
- Captured at `2026-09-09T15:52:21Z`; elapsed **0.450 seconds**.
- Original response: `response.html`, **18,352 bytes**; SHA-256
  `1f2a0877db9a6deb8f77a64d082eb6e8168bd64f343d34c320fe9a1ea56c2287`.
- `CAPTURE.json` records the result and bounds. Python's standard HTTPS client
  used its default certificate/hostname verification, a 20-second total alarm,
  and a 1-MiB body cap. No retry, cookie jar, page-script execution, or link fetch.
  The response was closed; both retrieval and static parsing processes exited 0.
  No temporary files, interpreter caches, or background jobs were created.

## Result and remaining gap

The page is an index advertising **4.20.0**, last modified in January 2025. It
does **not** document installed 4.18 foreground/`--daemon`, exit, or session-bus
semantics. It does not establish readiness or a runtime/toolchain result.

It verifies these possible next targets; **neither was fetched**:

- Source repository: `https://gitlab.xfce.org/xfce/xfwm4`
- Versioned release directory: `https://archive.xfce.org/src/xfce/xfwm4`

The smallest alternative is a separately root-admitted, single installed
`/usr/bin/xfwm4 --help-all` capture, to include any session-library option group
without a second help invocation. Proposed bounds: five seconds, 64 KiB combined
output, isolated HOME/XDG directories, no DISPLAY/WAYLAND/DBUS/SESSION_MANAGER or
injected loader/JVM environment, owned-process cleanup, and no automatic retry.
That proposal is **not permission to execute**, nor proof that help exits before
all initialization. If help lacks the required semantics, report the gap rather
than treating option names as readiness/exit proof.
