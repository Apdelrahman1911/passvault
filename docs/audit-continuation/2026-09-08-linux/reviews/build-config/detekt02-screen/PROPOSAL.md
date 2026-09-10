# Detekt02 host-screen diagnosis — prospective source only

**Author:** build_config. **Status:** unexecuted proposal awaiting independent challenge; not an admission or retry.
Consumed source: `reviews/detekt02-outer/LAUNCH.py`, SHA256 `f5dc403ea773ae4d844a617a38da2b75a768b93a03d2233c968ddaad824950e1`.
Historical receipt: `runs/linux-detekt02/OUTER-RECEIPT.json`, SHA256 `8099cb62ccbd72928b760281c07e5a3f5926ba3497147ae17144a03d2cac56b0`.

## Established result, not a culprit diagnosis

The **preterminal** receipt records HOLD, cleanup NOT_ATTEMPTED, the generic positive-process disappearance/change/unreadability exception, Git exit0 and isolated-child exit70. It retains **no candidate PID, comm, failed operation, partial identity or errno**. Cause remains **UNKNOWN**. Neither `unclassified_host_churn: 1` nor the two direct-child records identifies the triggering process. The receipt is not final-exit/cleanup proof. Old resource/namespace observations provide no current authority.

## Narrow prospective correction

The consumed screen does two identities and a comm reread **before** testing namespace ownership, then collapses distinct failures into one reason. The inert patch pins each candidate with a pidfd **before its first comm read** (pinning only after a positive read would allow PID-reuse misattribution). A positive candidate must complete birth/namespace/birth reads while its original pidfd remains live, match the already bound owned namespace pair, and have a still-live original direct owner pidfd. Only then is `owned_proof` true.

After that proof, the **only** new departure exemption is a reread ENOENT with the same candidate pidfd terminal and original owner pidfd still live. Each successfully observed comm, birth and namespace is compared **immediately**: an observed mismatch raises before any later ENOENT can mask it. Missing data before proof, foreign namespaces, permission/other errors, unexpected poll masks, owner termination, and positive terminal-without-ENOENT remain HOLD. The exemption does not establish no-escape or global-idle behavior; it cannot explain or guarantee avoidance of the historical failure.

Diagnostics snapshot the first failure and first proven-owned terminal/ENOENT observation (each capped at4KiB), plus a count; stages precede reads; owner PID, partial identity, errno and close failures survive. Snapshots avoid later close-error mutation of retained rows. No new evidence file is required. Each candidate descriptor has one close attempt; the screen never signals, waits for, or numerically kills a candidate. Existing direct-child cancellation, settlement, cleanup and source guards are unchanged. The existing8192-entry/5-second bounds remain; added sequential pidfd opens may themselves exhaust that bound and must fail closed. At most one extra candidate fd is held.

`PROSPECTIVE.patch` SHA256 **`8ae8cb0ceadc5a243507efc3a766f36f2d3ae02a9def47581e1fb3618fccf7ea`**; applying its exact text delta to the bound source would produce SHA256 **`81ae41ebb47117d4f909ab10bb525a7ae8107ea679ae1235e2337ad8a3d8e3f9`**. Diff paths are intentionally non-executable placeholders, not consumed paths. Only the inert patch and this note were written; no target helper was modified/imported/executed, no runtime/Git/process probe or project test occurred. Fresh independent source/execution/coordination/cleanup admission is required for any future scope; consumed02 and all existing STOP/NO-RETRY/CLOSED/HOLD restrictions remain unchanged.
