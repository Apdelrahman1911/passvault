# Main/NavHost child private-environment delta — independent source review

Reviewer `/root/editor_review`; author `/root/android32`; 2026-09-10.
**SOURCE ACCEPT for this inheritance correction only; no run/instance admission.**

Permanent source: `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`.

| Input | SHA-256 |
|---|---|
| Corrected source, 40,423 B / 753 LF | `7a0dd50d7caa6cf3cfad729f7393a0e1802fbf65b57d3cceae7c5c64ac94961f` |
| Prior adopted source, 38,703 B / 727 LF | `8ffefea4952f393b9bf8ef467b143109d660f9f5ffc58bf05803be30211922a9` |
| Author `MAIN-NAVHOST-PRIVATE-BUS-DELTA.patch`, 3,082 B / 49 LF | `f2d365596218a3d9667e05019f2c57100ef3943f7503b25a19ab68f343247758` |
| Author companion `.md` | `592a7e77b441d8aa5863baab4affa83f64f2cda0cde800ce7a1996950511f6f9` |

Author evidence lives under `reviews/android32/navhost-draft/` in this continuation.
Independent in-memory inverse of all three hunks recovered the exact prior hash:
**only 26 added lines**, with terminal-input, Room, Main and event/test logic unchanged.
The preceding terminal-race review remains intact at SHA-256
`c0b1e1ea6cc6ad2f9347769c79753ba5c1466d1910c965fb2469ba5a221c219e`.

## Challenge and result

Storage found that child `environment.clear()` discarded the intended private D-Bus,
XDG runtime and GTK bridge settings. A private PID/network namespace and `/tmp` alone
would not exclude the default filesystem system bus. This is a fixture inheritance
defect, not observed bus access or a new product finding.

The correction derives the session root only from the original `G/Xauthority`, then
uses the existing canonical/no-symlink/owner/0700 checks for G and G/runtime. It requires
exact original `XDG_RUNTIME_DIR=G/runtime`, `DBUS_SESSION_BUS_ADDRESS=unix:path=G/bus`,
`DBUS_SYSTEM_BUS_ADDRESS=unix:path=G/runtime/no-system-bus` and `NO_AT_BRIDGE=1` values.
Missing, altered, additional-address/autolaunch or wrong-flag values fail rather than
select defaults. `Files.notExists(..., NOFOLLOW_LINKS)` also rejects an occupied,
symlink or indeterminate system guard. The validated inherited strings are forwarded
into every cleared seed/Main/verify environment; parent and children validate before
AWT/product startup. No new dependency or bus API/connection is introduced.

Interface source inspected: init `3ba456dcc48d956eb2ff266ccfb31138ce3612f2b6aa5674194f09b0d563b8ef`
sets exactly those four worker values; inner `d61b7d0195365a320965e05c59e42856c9028a584df85cc584552b9b82d8bab7`
explicitly creates G and G/runtime as0700. Updated inner selection/source binding still
needs its separate review. These are source snapshots, not inspected live directories.

Correct address strings do not authenticate an endpoint or prove how every native
library selects its bus. Nonexistence is not race-proof against same-user changes.
`NO_AT_BRIDGE` is best-effort native suppression, not Java accessibility suppression
or no-activation proof. Fresh owner/socket/route/resource/cleanup admission remains.
The independent integration02 screen-fit compatibility question is separate and is
not resolved or waived by accepting this bus delta.

One prospective case / three serial roles / thirteen events remain; zero compilation,
execution, import, syntax/AST, network, Git, runtime/process/namespace probe or closure
credit. Only inert source reads, in-memory comparison and this new review were done;
no source amendment, temporary runtime/cache file or background process was created.
All STOP/NO-RETRY/CLOSED/consumed/HOLD scopes and publication/PVD fences remain unchanged.
