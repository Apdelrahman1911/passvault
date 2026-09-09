# Storage historical-evidence transport follow-up

Author: `/root/storage`. This addendum preserves, rather than rewrites, the
earlier source report and its failed-reader receipt:

- `REPORT.md`: SHA-256
  `fedae4cb95697f26e36d6da26cc3804075644096f32061c7c00c492de7f139a0`.
- `SOURCE_BINDINGS.json`: SHA-256
  `1f638c61d27198c62fa434d76b5133dd1fc9e992de07b6afa96d4c526d63e684`.

The old supplied-reader attempt remains **exit2 / `REFUSED: file type/size/links`**.
It was not retried or weakened. Root subsequently admitted a **separate new,
read-only Git-blob reader**, `scripts/audit/read_handoff_git.py`, exact SHA-256
`f1e0c83a50c1b9c327ff49c5c6444cefa9206fd3c1825ba37f80cc3b7794b304`.

Only after that separate admission this agent ran:

```text
timeout --signal=TERM --kill-after=10s 180s /usr/bin/python3 -I -B \
 scripts/audit/read_handoff_git.py show \
 remediation-reports/20260905T222925Z/reviews/storage-pvu-independent-platform.json
```

The recorded invocation completed **exit0**, approximately **1.21 seconds**.
The displayed historical member was **8,846 bytes**, SHA-256
`1834ada72a62c9e9e76ec7b916910c756c640b426967f1b85026bd26abadef09`.
No invocation was repeated to write this addendum. The transport observation
does not execute the historical report, an old runner, or a recovery helper.

## Reconciliation, not new closure

The historical independent report corroborates the preserved unresolved
outcomes and countercontrols:

- **PVU-001:** a real admitted provider/caller transition-versus-lease timing
  witness remains missing; cancellation and `clearForLock` guards matter.
- **PVU-002:** no freshly post-lock destructive UI admission is demonstrated;
  synchronous busy admission and owner-state clearing remain countercontrols.
- **PVU-003:** the component lock-order inversion is recorded, but two genuinely
  admitted production operations forming the harmful cycle remain unproven;
  navigation, busy-state and single-window/scene qualifications remain.

Those historical source hashes predate G12 and are **not** current-source
identities. That report's checkpoint/source bindings and rejected unwired
`OnCancelOperation` witness remain in the unchanged source report; this addendum
does not rebind any concurrently edited production file. The earlier
transport gap is now resolved **only for this named historical member**; the
three application investigations stay **UNRESOLVED / VERIFICATION BLOCKED**.

No production changes, tests, application processes, caches or generated
artifacts were created by the reader. Its transient process completed; Gradle
stop and build-artifact cleanup were **NOT_APPLICABLE**, not claimed successes.
All STOP/NO-RETRY/CLOSED restrictions and the eight separate PVD choices remain.
Accounting delta: **0 confirmed findings, 0 qualified closures, 0 conclusive
PVU outcomes, 0 application cases**. Root owns any central-ledger reconciliation.
