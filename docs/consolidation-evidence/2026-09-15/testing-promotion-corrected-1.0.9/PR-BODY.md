## Testing-only promotion: 1.0.9 / 1017003

Promotes the exact main tree `9a9be0fb904fb8a4249fbe46a23060b590357e96` from `da91a20103ed138df5a5d22d71b5467f17c07406`. Includes reviewed PR188 version/iOS memory changes and PR189 buffered Apple signing import + static policy correction. No unrelated source or audit-history integration.

- PR189 CI34995873640: all12jobs passed; main CI34997689283 must pass before this PR is created.
- Native fragmented-key import/negative control/non-extractability verified on Intel+AppleSilicon; seven focused static boundary cases passed.
- New actual iOS4GiB Intel archive/export and Store processing still require release-run evidence.
- Preserve previous failure/HOLD and deferred backlog; no claim hardware gaps closed.

Normal protected approval/merge required. Legacy automatic publishing stays OFF; this PR does not upload. After testing CI, a separately source-bound fresh TestingCandidate dispatch is needed. Build1017002 remains partially occupied,1017001 immutable. Testing destinations only; no production.
