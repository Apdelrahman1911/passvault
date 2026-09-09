# Linux02 raw transport: independent source-contract challenge

Reviewer `/root/verification`; 2026-09-09. Source-only follow-up, not execution,
new-instance admission, old-HOLD cleanup authority or a product-family closure.

## Finding and evidence boundary

The outer promises RAW_GIT_BLOBS but produces an attributed `git archive` export.
Its `core.attributesFile=/dev/null` disables an external attributes file, not the
committed tree's `.gitattributes`. Text/EOL conversion in that export is not the
raw-object guarantee required by `materialize` line366, which hashes each
payload as `SHA1("blob <size>\0" + bytes)` against `SOURCE.files[].git_blob`.
The later `source_check` deliberately records, rather than erases, differences
from the manifest's calculated `checkout_sha256`/`checkout_size`.

This is a producer/checker representation incompatibility for converted members.
It challenges the transport sufficiency overlooked in the earlier source
acceptance; preserve that acceptance and the consumed execution unchanged.
Do not normalize an archive back to guessed Git bytes or weaken the digest gate.

Tool `65dd3d` read only the pertinent outer ordering/materialize sections of
`LAUNCH.py`, SHA256
`f65aecef66207da898c7bfda7d966ea235a2fe70f96b523ff977f32f8a80a6af`.
Tool `6c4e53` read the pinned manifest as data and all four attributes files,
independently matching each to both its C4 raw Git OID and checkout SHA256:

| Attributes file | SHA256 |
| --- | --- |
| `.gitattributes` | `8884ed2a100ce791326a3a8d8d4c12a5e0f68d96ecb382fd3a6612ad827604c5` |
| `docs/audit-continuation/.gitattributes` | `ae5e1fbfc6144f03d001303e2eaa93b90805aca8ee3f1b7a31006ad0cac3cec9` |
| `docs/audit-handoff/.gitattributes` | `d46d4df5b35477e25356b95679421f35f1340fa5bcabe95005fc16e2e5f7ade0` |
| `docs/audit-publication/.gitattributes` | `705fd4d6451a31d36b3df7de96f83f30ac976c9b4a6d1e51671d8e2f33e2d0da` |

Root attributes contain `* text=auto eol=lf` and explicit `*.ps1`, `*.bat`,
`*.cmd text eol=crlf`; all three audit subtrees use `* -text`.
SOURCE SHA256 remains
`2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003`.
Its capture explicitly used raw `cat-file --batch`, recorded68,767,537 framed
bytes, and calculated a separate checkout representation. The two documented
PowerShell raw/Git/checkout qualifications are concrete evidence that those
representations must not be conflated, not a list of every converted member.

The actual02 receipt records only `RuntimeError:raw Git blob mismatch`.
No failed path, actual differing bytes, complete source_before, or final archive
stability comparison is retained. This source challenge does NOT identify the
actual first failing path, rule out every other cause, or claim an archive
forensic experiment. No held R/archive access is necessary to select a raw
transport correction. Actual result review remains SHA256
`d34f4e9890a35a72aa54a36c9dcbf09511396d49f2c4b8b95f5e5e4fb9379eca`.

## Narrow prospective correction supported

Use plain `git cat-file --batch` against the exact trusted C4 SOURCE OIDs;
no `--filters`, `--textconv`, follow-symlinks, generic EOL conversion or new
repository/dependency. Existing NO_REPLACE/NO_LAZY/no-network settings remain.
This transports raw payloads independently of committed checkout attributes.
It is a proposed correction, not acceptance of unwritten or unreviewed03 code.

- Validate exactly1572 unique safe relative paths, admitted regular Git modes,
  and exact lowercase40-hex OIDs. Preserve duplicate OIDs for distinct paths;
  do not deduplicate or reorder away the path-to-response association.
- Supply exact ordered `<oid>\n` requests from a fresh private, pinned regular
  read-only stdin FD. Keep the isolated inner's stdin DEVNULL. Avoid an
  interactive pipe pair or buffering the entire response in RAM.
- Strictly frame each response: bounded header, expected OID, literal `blob`,
  canonical decimal size0..32MiB, then exactly that many bytes and one framing
  LF. Raw length is NOT checkout_size. Verify the Git blob SHA1 while streaming,
  exactly1572 responses in request order, then true EOF with no extra data.
- Retain existing120-second/128MiB child limits, bounded parsing/materialization
  deadlines, periodic checks and original pidfd supervision. Pin/hash generated
  request/response files and verify stability before/after their use. A two-pass
  parser must not trust saved offsets across an unverified mutable stream.
- Preserve component-no-follow/O_EXCL path and mode guards, payload digest before
  accepting/restoring source mode, fsync, complete source_check raw SHA1/SHA256,
  and both EOL qualifications. Do not turn differing calculated checkout hashes
  into permission to change raw bytes.
- Change only the fresh generated top-level cleanup allowlist to the exact new
  transport filenames and preserve every existing cleanup/settlement gate.
  Early fresh-origin evidence may retain new R identity before materialization;
  it does not authorize retrospective02 recovery or weaker03 cleanup.

Concrete later verification counterexamples include wrong/missing/out-of-order
OID/type, invalid/oversized/short header or payload, missing framing LF, trailing
response/data, binary NUL/newline and CRLF bytes unchanged, empty blobs, and a
repeated OID serving two distinct paths. These are review obligations, not test
cases executed by this source-only work. Root alone owns any admitted execution.

The author received these requirements; the complete new source delta and any
future exact instance still require independent review. All old FAIL/HOLD,
STOP/NO-RETRY/CLOSED, hardware/platform and publication boundaries remain.
Only this small permanent source note was created; no helper import/execution,
Git/CI/build/test, held-runtime access, probes, temporary files, caches or workers.
