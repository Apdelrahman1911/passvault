# C20 KDF-ORACLE — retained actual-result reconciliation

Reviewer `/root/android_fixture_review`; 2026-09-11. Paths below are relative to
B = `/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux`.

**ACCEPT_RETAINED_SOURCE_ORACLE_ONLY.** The two full32-byte outputs below are
accepted as fixed public-synthetic oracle data from the root's one admitted
calculation. No reviewer recalculation, Windows/CNG/application execution, case
credit, closure, test-source promotion or new execution authority follows.

## Retained evidence independently read and hashed

All five artifacts are under `reviews/team20/root/`.

| Artifact | Bytes / LF | SHA-256 |
| --- | --- | --- |
| `C20-KDF-ORACLE-ADMISSION.json` | 2308 /19 | `c2253ba10e9b8cdb7fc9d17c5b2f6c983fac571971f4731d4ae074a8869cbd92` |
| `C20-KDF-ORACLE-ACTUAL.json` | 1790 /27 | `40752a01b817ff9c4fa79a0f7f0305ed59f84eff453cdf3aa769be9acfde68b3` |
| `C20-KDF-ORACLE-STDIN.py.txt` | 1583 /34 | `4ef1bec5271a9a6da9a40d779084843f07444cc11551ce98b3980e680f8fbcf2` |
| `C20-KDF-ORACLE-STDOUT.json` | 1082 /1 | `adb3f5f07e418f7e717d1fcb030e6cf1ff10fdeca3c84b4e943f7f9e7df56e9e` |
| `C20-KDF-ORACLE-STDERR.txt` | 0 /0 | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |

The retained stdin exactly matches proposal lines17-50, including final LF:
independent inert `sed | cmp` returned0; both SHA-256 identities also agree.
The pre-invocation source review is
`reviews/team20/android_fixture_review/c20/KDF-ORACLE-SOURCE-REVIEW.md`, SHA-256
`58e8b21470906a6cf6c5232b30a81c95656a79407ef51220d94f087b3f83666a`.
Its exact production algorithm/parameter and non-circularity findings are reused,
not repeated by another calculator invocation.

Root's admission binds `timeout --signal=TERM --kill-after=2s 5s /usr/bin/python3
-I -B -S -` with the exact stdin. The actual record and root's message identify
original tools.exec_command chunk `d55fdd`, tool/calculator exit0 and no ongoing
session. The reviewer inspected retained data, not the original live process or
its interpreter; original foreground settlement is the root-recorded fact.
The actual command adds only the named stdout/stderr evidence redirections.
Empty stderr and one compact stdout JSON object are directly verified here.

## Exact accepted literal data

| Vector | Full output32, lowercase hex |
| --- | --- |
| `ascending` | `977ea1dce4266794464932f34640331ee4d58bfbd1257467be3d7cc378714caf` |
| `vault_first_byte_zero` | `b83f58ef0f9297d7ff8a3487e6f2d6572aedb78eca9f2bb9a386462d3224dc20` |

Inert JSON parsing/string comparisons, with no cryptographic calculation, found:

- Schema1, the stated extract/one-block-expand algorithm and output_bytes32 agree.
- Domain hex is exactly the45 ASCII bytes previously reviewed, without NUL;
  public PRF `47..66`, salt `33..52`, vault `01..20` are each32 raw bytes.
- Exactly two named vector records appear. The second changes only vault byte0
  from01 to00. Each info_hex is exactly domain_hex + vault_hash_hex + `01`
  (156 hex characters /78 bytes), not an alternate domain or ASCII counter.
- Both output strings contain exactly64 lowercase hex characters, are distinct,
  and match their correctly named ACTUAL.outputs entries byte-for-byte.
- Receipt exit/session/chunk fields and zero application-case/closure credits,
  together with retry_authority=false, match the root's supplied result.

Given the exact unoptimized stdin and root-recorded exit0, the source's length,
explicit-pad/HMAC agreement and distinctness assertions passed. The independent
source review already established the calculation's non-native provenance.
Both calculation paths still share hashlib SHA256; this is not corroboration
from two providers, a published external vector or Windows compatibility success.

## Remaining boundary

The literal patch did not yet exist for this review. The full two-file delta,
fixed-size assertions, normal-only selector isolation and unchanged old dispatch
belong to desktop_other_review. Any optional subsequent byte-only match here
must name the actual patch/test hashes; this report does not pre-accept them.
No expected values may be regenerated through production code at test runtime.
A future Windows build/test and its independent admission remain separate.

No calculator/helper execution or import, KDF recomputation, AST parse, build,
platform/SDK/provider/process probe, Git, network, held-R access, cleanup, source
edit or central-ledger change occurred. Only bounded inert data reads, hashes,
counts, text/JSON comparisons and this own report were performed. No new runtime
or stop obligation. Native refusal, all consumed failures/HOLDs and existing
STOP/NO-RETRY/CLOSED/occupied1017001 boundaries remain unchanged.
