# AndroidCompile02 — shared C18 source-reference delta

Author `/root/android_compile_author`, 2026-09-11. **PROPOSED ONLY; ALL BINDINGS UNBOUND.**
B = `docs/audit-continuation/2026-09-08-linux`. Root requested one shared fresh C18
raw-source capture, rather than separate captures for three validation lanes.

`ANDROID-COMPILE02-SHARED-C18-SOURCE.patch.txt` is1625B, SHA256
`83531e0a1c7687d1a4eeadd710af33f78b8247a240ccaac1aa3ad431f7f3e5ea`.
It changes only the `SOURCE` literal in the two already released fresh02 files:

```text
B / 'reviews/android-compile02/source-prepare01/SOURCE.json'
→ B / 'reviews/checkpoint18/source-prepare01/SOURCE.json'
```

| File | Accepted cap-integrated before SHA256 | Proposed after SHA256 |
|---|---|---|
| `scripts/audit/linux_android_compile_02.py` | `7391da07d72ff55ce9cbaa26ba31ca8e435f83a2b3063f962719b8d40519c93b` | `a62983080e04c638754973f4a6dfe16d21ccf6bb8f0cbb2582f334aa9b85f89a` |
| `B/reviews/android-compile02-outer/LAUNCH.py` | `5f00a8109adad00c90546473e8632e25c3233da9369f7b017cd172571fe6a6f3` | `3bfa5fff0a759c2fd11be99595949f1562f99f975ea323be1f25b2bd94eb02b6` |

Inner63320B→63315B, still1038LF; outer63574B→63569B, still1059LF. Each complete
after-image differs by exactly one literal replacement; the UNBOUND comment is
unchanged. The init and every other source file are outside this delta.

This is an intended shared input location, **not capture creation, capture
inspection, source binding or admission**. All COMMIT/TREE/MEMBERS/FROZEN and four
required-source hashes remain `None`/`null`, as do read-root/offline and instance
pins. `FROZEN[SOURCE]` still has no digest. Root must later bind/review the actual
complete shared C18 manifest, exact source/index/control images and genuine
request/approval. A path alone establishes no identity or available resource.

Both helpers agree on the same shared reference. No behavior,26-task/Jar scope,
3500-member/144KiB OID limit, other resource/file cap, reviewer role, cancellation,
stop/settlement/cleanup or receipt requirement changes. The prior cap/source
reviews and qualifications remain; neither consumed01 nor GUI03 is reopened.

Root authorized application of only this exact delta after genuine independent
source acceptance by `/root/android_compile_review`. At proposal time it is
unapplied; a separate integration receipt will retain before/after hashes if
accepted. Original proposals/reports are preserved rather than rewritten.

This lane read only the two new helper sources as data and wrote its own proposal
records. No helper import/execution/AST/syntax check, other source edit, capture,
Git/T/runtime/process/SDK operation, cleanup or binding occurred. GUI03's failed
read-only observer and all R/T freezes/HOLD obligations remain separate; source
work requires no cleanup decision. All STOP/NO-RETRY/CLOSED/native-refusal and
publication restrictions remain. No compiler/test/native/runtime/closure credit.
