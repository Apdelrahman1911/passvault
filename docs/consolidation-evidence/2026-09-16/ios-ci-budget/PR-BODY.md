## Target the iOS CI timeout; preserve cleanup HOLD
PR193 run35047359784 hit the generic35-minute batch deadline during unsigned optimized iOS linking. No compiler error/OOM was observed; the reason for slower execution remains unknown. The same tree passed PR192 and mainCI35045205060. Original wrapper stop0 did **not** establish full settlement: private-root process remained outside scope, cleanup **HOLD**, identity/final settlement unknown. No old-run retry or recovery.

- Give only the already admitted macOS/>=12GiB iOS native profile a bounded50-minute batch and60-minute job; ordinary jobs stay35/45.
- Preserve heap, worker/resource floors, strict verification and every fail-closed cleanup condition. No termination/cleanup rewrite or broadened process-kill authority.
- Record chosen timeout in cleanup receipt; extend existing resource-profile regression method. No new platform matrix or production dependency/version/signing changes.
- Fresh CI must prove actual native linking and cleanup; source/mock checks are not native proof. A repeated hang/failure needs investigation, not endless deadline increases.

PR193 remains draft until reviewed correction is merged through main and its exact-main snapshot is refreshed. Store1.0.10/1017004 approval unchanged and unconsumed; no release dispatch here. All historical STOP/NO-RETRY/CLOSED/HOLD and hardware limitations remain.
