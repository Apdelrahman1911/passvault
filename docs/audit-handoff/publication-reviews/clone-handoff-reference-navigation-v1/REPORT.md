# Qualified reference-locator review

**Accepted only for pinned v3 source methodology and output metadata consistency—not semantic reference closure.**

Reviewer: `/root/audit_navigation`. Root remained the sole command owner. This reviewer did not import, compile or execute any helper, run Git/builds/tests, inspect excluded procedure bodies or read private/application source data.

## Disposition and history

- **V1:** retained source/log/exit/receipt show the missing quote at line126 and parser failure/exit1 before the helper body. This is a transport-tool failure, not an application test.
- **V2:** independent whole-byte comparison confirms the single-quote correction. It remained unexecuted.
- **V3:** all167LF read; the exact v2-v3 diff independently reproduced. Following challenge, v3 distinguishes digest-bound withholding from path-policy/hash-unresolved association, calls matches candidate locators, records JSONL example line ordinals, and carries script/input/baseline bindings and method limits.
- The retained root receipt/log/exit record one v3 run, exit0, under a cooperative frozen-input envelope. No general runner or application execution admission follows.

V3 source:10225B, SHA
`3bc6a732e30f40ad9dcac19072a1c6dd1c75a34b085cf87c89e0f0c9ac248914`.

## Independent output check

Pinned output:11,439,084B, SHA
`47cebd38e2b5200fe9b709d51ff9aee61000b26c46f2b36b8295729bbe4b1c72`.

A bounded direct read checked all **13,927 unique sorted candidate rows**, **266,962 occurrences**, category totals, **31,876 sampled contexts**, including130 JSONL line-prefixed examples, and exact agreement with the log/receipt bindings. Selection accounting is15,886 parsed files plus4 retained unparsed files out of15,890 selected JSON/JSONL inputs.

| Candidate class | Count |
| --- | ---: |
| Included path/hash in at least one context | 12,134 |
| Included same-hash alias | 1,027 |
| G12 raw-source locator | 536 |
| Baseline Git/computed CRLF locator | 83 |
| Unresolved/external | **109** |
| Withheld-path policy, descriptor hash unresolved | **31** |
| Exact withheld-hash locator | 7 |

The **140 unresolved candidates remain open**. The table is not an evidence-completeness percentage.

## Binding limitations

The bound companion `ASSEMBLY.md` (6374B; SHA
`d8d18db744671ef6ba31ef44444b3a358db7fe94fe3bcda4fbd4b71f24a423b4`) retains the necessary qualifications:

1. Contexts merge; category priority and three examples do not prove every occurrence or exhaust all possible associations. JSONL `/jsonl-line/N` is a synthetic locator prefix using Python line splitting.
2. Size, mode, range and generation semantics are not validated. A generic `lines` key is only heuristic; a fragment hash may locate an entire different blob.
3. Python JSON parsing is not advertised as RFC-strict. Duplicate-key rejection is limited to scanned documents, not startup inputs.
4. Public baseline blobs were read as data; CRLF forms are suffix-based predictions, not observed checkout or checked attribute policy.
5. Startup inputs are parsed before binding rereads. The parent's pre/post hashes and cooperative freeze are not hostile-mutation isolation supplied by this helper.
6. Markdown/free-form references, canonical/range identities, missing history and private runtime evidence remain outside mechanical closure. Missing proof must stay blocked.

This reviewer did **not** rederive the classifications from the included corpus, omissions, source payloads or Git blobs. Other pack/source/platform/screening assertions in the companion are outside this acceptance.

Exact old/new input versions are retained inertly under `inputs/`; `REPORT.json`, `INPUTS.json`, `OUTPUT-CHECK.json` and `DIFF-CHECK.json` preserve detailed scope and bindings. The11MB output is not duplicated here and needs its own final delivery identity.

No finding/fix closure, application test pass, source coverage, archive runtime acceptance, public-secret clearance or release approval follows. STOP/NO-RETRY and CLOSED gates remain unchanged. Later source/output/companion/package mutations require their own attribution.
