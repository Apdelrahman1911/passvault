# PVA007 bounded backend failure/acknowledgement source delta

**SOURCE ONLY — not compiled or run; independent challenge pending.** One existing common-test method strengthened, zero new methods and no product change.

## Exact scope and nonduplication

Only `feature/credential/src/commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldDraftTest.kt`: one `error_credential_save` resource import and the existing `failed page Save keeps adopted values dirty and retry persists them` method. Literal before/after comparison confirms all other test code unchanged; all13 test methods and existing ownership/teardown remain.

The prior method proved immediate fake-backend failure retained the adopted value and a retry persisted it, but never collected effects or observed pending successful persistence. The amendment adds the absent **failure/pending-retry -> completion acknowledgement** oracle rather than another copy of retention. Other existing tests already cover stale captured row Save, all six per-property event orders, direct Add capacity rejection, successful repeat-Save completion and busy input rejection; none is reimplemented. Existing Room blank-name rejection is validation-before-persistence, a different boundary.

## What the amended method asks

1. Real production VM receives Save with a valid inline draft; the existing fake returns `Result.failure` immediately. After the current scheduler drain: no effect, explicit save error, savingfalse/canSavetrue/dirtytrue; adopted name/value/secret remain while fake stored value is still old.
2. The same live editor retries, using the existing fake's1000ms virtual successful-save delay. At that pending boundary: saving/dirty remain true, stored value is old, and no acknowledgement is present.
3. After explicit virtual advance plus drain, assert both stored custom-field IDs/names/values/secret flags (including untouched sibling), no saving/dirty/error, and one `SaveCompleted` with the current credential ID followed by no additional queued effects.

Production `saveCredential`/`performSave` is read-only basis: it synchronously adopts/validates drafts, marks saving, awaits repository save, and publishes completion only in the success branch. The existing fake checks its one-shot failure **before** its success-only delay; the test/comment does not falsely call failure delayed. `stored` returns deep copies and clears them in its unchanged `finally`.

## Limits / review requests

- This is a bounded logical fake-backend check, **not** Room durability, rendered pre-frame input, actual keyboard/IME/a11y/RTL/mobile behavior or PVA031 Add-dialog acceptance/acknowledgement evidence.
- No clear/lock/dispose between Save and the asserted outcome. Existing teardown is unchanged; no universal retention-after-disposal claim and no PVU002/006 route or witness.
- No wrapper backend, instrumentation seam, extra fixture or new task/dependency. No production finding/patch asserted.
- No import/AST/compile/build/test/Gradle execution was performed. Source-only literal scope/LF/added-line length/hash checks passed; that is not Kotlin compilation.
- Root/independent reviewer should challenge effect-subscription timing, immediate-failure versus delayed-success semantics, copied-value ownership and the precise nonduplicate oracle. If admitted, root should select only this changed method in a fresh bounded execution; unchanged successful methods do not need a reassurance rerun.

`BEFORE.kt`, `AFTER.kt` and `DELTA.diff` retain exact original/afterimage. `MANIFEST.json` supplies all pins and limitations. Source review/actual execution may still reject or amend this delta. No family or test-pass count changes now.
