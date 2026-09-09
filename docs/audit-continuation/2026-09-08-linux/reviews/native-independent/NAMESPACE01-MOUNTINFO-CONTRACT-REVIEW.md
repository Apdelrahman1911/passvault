# Namespace01 mountinfo: bounded independent contract inquiry

Reviewer `/root/native_review`; 2026-09-09.
**A narrow parser correction is justified for an empty mount SOURCE field.
An empty SUPEROPTIONS exception is not established. Namespace01 remains consumed
FAIL; its missing raw child mountinfo prevents attribution to a particular row.**

## Frozen source and actual evidence read as data

Paths are relative to `docs/audit-continuation/2026-09-08-linux/`.

- `reviews/storage/LINUX-NAMESPACE-FEASIBILITY-PROBE.py`:78LF/3717B,
  SHA256 `96eb1d54c690fd19181a76a5abfeff874cde42ef2e87aa89260a6c29daea1fee`.
- `reviews/storage/NAMESPACE01-EXTERNAL-RESULT.json`:55LF/2300B,
  SHA256 `ed2fb7e9fed8df808feb625f804f5c706e5343cd0c308a4151a0c8b01cf22611`.
- All five raw files in `runs/linux-namespace-feasibility01/` were read and
  independently hashed against that receipt, totaling1381B:

| File / bytes | SHA-256 |
| --- | --- |
| `CHILD.json` /61 | `e53bba963eba6d7bbd5e1935cbf8142b29565be5d83aac4050f1c14198b600da` |
| `INTENT.json` /828 | `249b18f0e79f08e5185e88b983a18b4beb713bb2936c144650d788674f427a81` |
| `RESULT.json` /451 | `7ac1eaed030743c11d1847667177e19d09f39338c5576a8e4d25a011bd9a01f2` |
| `stderr.log` /41 | `9a88981efe75b5b34b9b9c18f25973cafefb129247c3b68040c603d52e1852d6` |
| `stdout.log` /0 | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |

Actual stderr is `NAMESPACE_SELF_REFUSED: mountinfo fields\n`, with empty stdout
and outer/direct-child exit1. Source52 collapses whitespace with `line.split()`;
source60 then requires `separator >= 6 and len(row) >= separator + 4` for every
mount, before any final success JSON. Earlier init/namespace/status guards were
source-reached, but this is no final feasibility PASS or cleanup/HOLD release.
There is **no retained failing child row** to identify its precise field defect.

## One separately authorized SELF format observation, not a replay

Root explicitly permitted one bounded current `/proc/self/mountinfo` data read.
Tool `9ceddb` exited0: at most131072B read, fewer than the cap, ten rows, no raw
mount paths output or retained. Five rows fail the old field-count guard:

| Filesystem format token | Whitespace fields | Separator index | Positional suffix fields | Empty suffix index | Superoptions |
| --- | ---: | ---: | ---: | ---: | --- |
| `devpts`, `proc`, `sysfs`, `devpts`, `proc` | 9 each | 6 each | 3 each | 1 each | `rw` each |

Splitting the suffix on **literal spaces** retains `[type, "", "rw"]`.
Generic whitespace splitting loses the middle SOURCE slot and turns the valid
positional suffix into only two tokens. The guard then demands10 fields but sees9.
This is a concrete incompatibility with current SELF input, **not** the missing
child observation, a namespace replay, an empty-superoptions case or a regression
test. No new mount/namespace operation or process census was performed.

## Bounded upstream reference and minimal correction

One anonymous, no-redirect/no-retry data GET retrieved upstream Linux v6.6
[`fs/proc_namespace.c`](https://raw.githubusercontent.com/torvalds/linux/v6.6/fs/proc_namespace.c):
8301B, SHA256 `07a819793943c13b8423e43b33c6a623e64151213320c1ac3971477a5a19e718`.
Tool `fe4834`:curl0/standard-library data-reader0; connect5s/total20s,131072B cap,
no response file/cache/archive. Only inert `show_mountinfo`135-195 was displayed;
this is a versioned reference, **not a claim about the running kernel's identity**.

The renderer emits the literal `" - "`, filesystem type and a space, then a
filesystem device-name callback or `mangle(m, r->mnt_devname ? r->mnt_devname :
"none")` at176-185. The fallback tests a null pointer, not an empty string; the
source emission is not required to add a nonempty token. It then unconditionally
starts superoptions with `" ro"` or `" rw"` at186 before optional extra options.
Thus the empty-source interpretation is consistent with upstream rendering;
this reference does **not** justify accepting absent/empty superoptions.

The smallest justified contract change is positional parsing, not a weaker
minimum token count: partition each raw line **once** at literal `" - "`, retain
the six mandatory prefix fields/optional tags, and split the right suffix on
literal spaces so its three slots remain `[filesystem_type, source, superoptions]`.
Allow only the middle SOURCE slot to be empty; require a nonempty type and
superoptions and reject a missing separator, missing slot or extra malformed
suffix. This is a source-level correction recommendation, not a patched or
executed generic parser. Preserve byte/row caps, every propagation-tag check,
`/proc` device matching, filesystem type and `nosuid,nodev,noexec` requirements.
Simply reducing `separator + 4` would conflate empty source with malformed or
missing fields and is not the proposed fix.

The contract/data distinction was sent to the original author for independent
challenge and new-source planning; the consumed source was not edited. Any new
implementation still needs its own independent source/instance admission. No
retry, old runtime access, generic harness, build/test, helper execution/import,
source patch, cleanup or product/closure credit occurred. Only this compact
permanent report was written; the two bounded data-reader tasks left no files,
caches, namespaces or background workers.
