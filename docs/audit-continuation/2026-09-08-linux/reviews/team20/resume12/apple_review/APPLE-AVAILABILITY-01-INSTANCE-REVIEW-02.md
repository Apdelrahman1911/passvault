# Apple availability01 — corrected exact-instance acceptance

Reviewer `/root/c20_apple_review`;2026-09-12.
**ACCEPT_EXACT_ROOT_INSTANCE_ONCE_WITH_RETAINED_LIMITS.** This is independent
pre-execution review, not an observed query, available Apple host or test pass.
Root remains the only execution owner and must make its final launch decision.

## Exact binding

All paths are relative to `docs/audit-continuation/2026-09-08-linux/`.

| Input | SHA256 |
|---|---|
| `reviews/team20/apple_platform_author/c20/APPLE-AVAILABILITY-01.ADMISSION.json` | `3481a34b313df911a60e31b6bfd9cbd5363a361a71606bbcbe21e510fd322bb2` |
| Unchanged `reviews/team20/apple_platform_author/c20/APPLE-AVAILABILITY-01.py.txt` | `9311576f3030d859b7875311050d20e33ee796905c95afffb2b2624c70b549e7` |
| Root-copied `reviews/team20/apple_platform_review/c20/APPLE-AVAILABILITY-01-SOURCE-REVIEW-20260912.md` | `f854b9ea1d47344d5f32926da095e6496d792e200d3e6ca7591377a0cf921729` |
| `reviews/checkpoint20/PR-STATE.json` | `bf2148f6a723fd8b12f71bfcb1f45befcf8c1448acfe99c58ba21cac3c4cf417` |
| `reviews/checkpoint20/PR-STATE-EXTERNAL.json` | `81f43bff3f90d19b76efe9c4ef21991eef4896fde99de3516bb3285260e211f0` |
| `reviews/team20/resume12/coverage_review/CONTEXT-PR-POINT-ACTUAL-REVIEW-01.json` | `0641435a5ec548dc0e0b600b2eabd0700b5349d0c181d690cec165bc481dd425` |

The first request remains byte-for-byte in
`reviews/team20/apple_platform_author/c20/APPLE-AVAILABILITY-01.ADMISSION-UNEXECUTED-DRAFT.json`,
SHA256 `393769a4d27a5018d585057d44342f177991298d56849aebd4949ef1dd621dd2`.
Its rejection is retained in `APPLE-AVAILABILITY-01-INSTANCE-REVIEW-01.md`,
SHA256 `43d7408ab9d9825f53b994f6d3fb1ff748a06caad6347d58b2881e3320e34f78`.
Neither draft was executed or consumed a capture slot.

The diff changes exactly the two challenged fields: status is now the required
`ROOT_ADMITTED_ONE_METADATA_CAPTURE`; the deadline is integer1789180144. Root
selected a fresh two-hour latest-start window, not a two-hour command allowance
or an extension of any failed/consumed invocation. The collector still enforces
the once-only directory barrier,30s child lifecycle and95/100s aggregate limits.
An expired/changed instance must refuse; this acceptance grants no automatic
deadline update, replay or alternate namespace.

## Verified request/launch contract

- Schema/key sets, owner `/root`, exclusive-custody=true and the32-hex nonce
  satisfy the reviewed source. Exact scope/reviewer/source hashes are unchanged.
  The fresh review basename resolves to root's explicitly copied exact bytes,
  preserving the true reviewer rather than asserting another review occurred.
- Both gh/curl path/pin/hash objects exactly match the independently accepted
  current point. HOME is the only present credential-path key; neither token
  name is present. Root separately bound the actual non-secret value `/root`.
  The request does not infer this value from key-presence evidence.
- P's original tuple is23/580/0/16877. Current P/T/.git/config equality is checked
  by the collector before queries and before accepting normal completion. Root
  supplies cooperative custody; historical point identities alone do not.
  Exclusive mkdir of the fixed `.apple-availability01-private` child must succeed
  before any request; an existing child refuses without adoption or deletion.
  This reviewer made no independent live directory/absence probe.
- Root supplied this exact outer command, with shell `login=false`, cwd
  `/root/projects/PassVault/passvault-linux`, and no overlapping audit build:

```text
exec /usr/bin/env -i PATH=/usr/bin:/bin HOME=/root LANG=C LC_ALL=C TZ=UTC /usr/bin/python3.12 -I -S -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/apple_platform_author/c20/APPLE-AVAILABILITY-01.py.txt 3481a34b313df911a60e31b6bfd9cbd5363a361a71606bbcbe21e510fd322bb2
```

The explicit clean Python environment excludes inherited loader/debug/proxy/TLS
and Git overrides and tokens. It preserves only the admitted existing HOME
credential context. Child API/documentation environments remain the exact
separate source allowlists. No new interpreter/tool version command, image
update, credential/config read, account probe or executable discovery is needed.
This is a trusted installed-launcher/cooperative-host binding, not a hostile
same-user execution sandbox.

The accepted source review's direct-child-only settlement, gh internal HTTP/
buffering, filesystem/Popen/fsync timing, private evidence and hard-loss limits
remain unchanged. Root must retain the original tool terminal result, actual
per-command argv/start/end/exit/stream/digest/settlement receipts and compact
private outputs, then obtain independent actual-result interpretation. A JSON
status alone cannot prove final descriptor close, actual process exit or a host
entitlement. Failure/ambiguity stops the list; no automatic retry or fallback.

No network/Git/process/runtime/SDK probe, helper import/execution, build/test,
stop or cleanup occurred in this review. There is no new runtime/cache/service
to remove; no Gradle stop obligation. Metadata adds zero testcases or closures.
All physical-platform/Apple-host, IOS01/Mac02, native-refusal, STOP/NO-RETRY/
CLOSED/HOLD/PVD and protected-ref/dependency/identity/version/signing/Store/
build1017001 restrictions from the accepted source review remain binding.
