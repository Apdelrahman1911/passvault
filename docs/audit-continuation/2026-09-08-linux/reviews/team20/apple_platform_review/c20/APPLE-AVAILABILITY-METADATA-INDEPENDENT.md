# C20 Apple availability metadata — separate independent source review

Reviewer `/root/apple_platform_review`; author `/root/apple_platform_author`;
2026-09-11, following root's explicit request for a bounded availability
assessment. **ACCEPT_FIXED_METADATA_SCOPE_WITH_COLLECTOR_PREREQUISITES.** This
accepts the inert command/response contract, not a collector implementation,
execution instance, available Apple host, test run or closure.

## Exact accepted proposal

Author files under `reviews/team20/apple_platform_author/c20/`:

- `AVAILABILITY-METADATA-PROPOSAL.md`, SHA256
  `fb5b1176cfb593ceeb48ed35dfffcf8b393754099f5a6ec040080c6268a306ba`.
- `AVAILABILITY-REVIEW-RESPONSE.md`, SHA256
  `23c966a3dbe0450c5248c1a0aacb8aaa7faab56b061fd7914c9b78220c8cb845`.

The earlier proposal hash `c3fee6676309ac3ef2ddfbcd46de5e328b695f57ae3e51f095c922d8d9dc268f`
was challenged for inherited debug/credential exposure, typed gating, stop/skip
semantics, status parsing and HTTP-transaction overstatement. The response
retains those challenges; the fixed command argument lists/endpoints did not
change. This is source review, not an observed failed/corrected execution.

`APPLE-C20-INDEPENDENT.md` remains unchanged at SHA256
`9c373cdc77ab31a2b9a170b236bbc92892902e75b6b2e6167e35651a99b9172b`.
No original route/test proposal, after-image, helper, workflow, request or ledger
was edited by this additional review.

## Independently established retained basis

I read the retained JSON as data and checked its raw hashes:

| Input under the continuation directory | SHA256 |
| --- | --- |
| `runs/ios-focused01/RUN.json` | `5b578327aa528eb3bbc8c117c20629ddfc78eb2dda9506e1c5186cedc24c259b` |
| `runs/ios-focused01/JOBS.json` | `7872f40faae42253d6cf644455ceff4eeea8be2c31be3a956547f93bb48d39a3` |
| `runs/ios-focused01/DOWNLOAD-RECEIPT.json` | `c4075b769ee0dfcfef4274cd57a12919373a7da71fce62f9d3c483d04b0d8505` |
| `reviews/android32/runner-facts-01/runner-images-README.md` | `e2282f1ccf4a8c952433ada336e1eeda9eaffe97592c9b9c334fcaac6115f63d` |

RUN names public repository1318415796, `Apdelrahman1911/passvault`, owner104788132,
login `Apdelrahman1911`, type `User`. JOBS contains only completed failed
job102797810556, hosted runner1000005399/default GitHub Actions group0 and label
`macos-15`. It is not an owned/self-hosted candidate to probe or reuse.

README lines34/46 document an ARM64 `macos-15-xlarge` label and the exact official
larger-runners URL, differing from the proposed request only by its omitted
browser fragment. This supports a public documentation read, not label
availability, entitlement, installed tools or free resources. None of these
retained facts establishes current credentials or administrator permission.

## Command scope and guard challenge

Only the exact three serial, once-each command invocations in the accepted
proposal are within scope:

1. Existing authenticated `gh`, explicit github.com and GET, exact repository
   identity/permission projection. Object types, integer-not-boolean numeric IDs,
   exact owner login/type/full name, public/unarchived/enabled flags and boolean
   permission fields must match. No token dump, login/refresh, `/user`, billing or
   alternate owner lookup. A valid matching `admin=false` skips2 only; operational,
   malformed, changed-identity or custody failures abort the remainder.
2. Only with that validated `admin=true`, GET the exact repository self-hosted
   runner endpoint, page1/per_page100. Validate bounded runner/label shapes and
   text, exact boolean busy, and unique positive integer IDs before any complete
   inventory claim. Completeness requires exact count equality; any noncomplete
   inventory remains unestablished, never evidence of absence. The specified
   over100/short-page/error paths stop without pagination, fallback or retry.
3. Unauthenticated `curl -q` to the retained exact official HTTPS URL. No config,
   authentication, redirects, retries, linked assets or JavaScript. A fresh
   four-variable PATH/LANG/LC_ALL/TZ environment excludes GitHub credentials,
   config overrides, proxy credentials, tracing and TLS-key logging. The actual
   executables and environment still need root's separate binding.

Meaningful counterexamples are now handled or explicitly limited:

- `admin="true"`, missing permission fields or a403 cannot become authorization
  or an empty runner inventory. A missing runners array is not an empty list.
- Online/not-busy and an ARM64 label are configurable metadata, not processor,
  compatible Xcode/JDK/runtime, measured free memory, exclusive custody or device
  proof. No returned name/label is interpolated into a new command.
- A redirect can exit curl successfully despite `--fail`; requiring the final
  status200 prevents accepting it. Parse only the complete final exact ASCII
  status suffix, not an apparent status marker inside HTML. Timeout, truncated
  output, excess bytes or missing eligibility prose receives no successful
  documentation interpretation or alternate URL.
- Merely not enabling debugging was insufficient: inherited GH_DEBUG/HTTP/auth/
  shell tracing and TLS-key logging must be rejected or removed without printing
  the environment. Only API children receive the existing necessary credential
  context. Candidate names/labels and unscreened diagnostics stay private; public
  evidence may retain safe counts and response digests, not host names or terminal
  transcripts. No notification may disclose credentials or those private fields.
- At most three CLI invocations is not exactly three HTTP transactions. The
  proposal properly retains uninspected `gh` internal redirect/auth/retry
  behavior and unbounded pre-projection response buffering as limitations; no
  application-level retry, endpoint expansion or raw-memory bound is inferred.

## What root must bind before execution

This packet contains no executable collector or current instance to approve.
Root must separately bind/review its finite current capture mechanism, exact
executables/environment/argv, direct-child custody, serial gate decisions and
private/public retention before invoking anything. Reuse of a consumed collector
or an old source/instance admission is not authorized.

The mechanism must actually enforce30s per child/100s aggregate, API projected
stdout256KiB each, documentation1MiB plus128B trailer, stderr16KiB each and2MiB
aggregate evidence. Retain exact projected bytes/digests, original commands,
start/end, actual exits, original direct-child reaping and document/status
separation; incomplete bytes cannot be labeled a complete response. Bounds are
collector obligations, not a proven hard sandbox or a consequence of `--jq`.
Only positively owned metadata children may be settled after failure; no broad
process signalling, held-runtime access, cleanup or additional probe follows.

## Outcome limits

A complete empty endpoint response would establish only that no configured
repository self-hosted candidate was listed then. A positive response supplies
only candidate metadata, not permission to dispatch a job/SSH/device probe.
Documentation establishes public eligibility rules at capture time, not this
account's purchased entitlement, availability or reservation. No organization,
plan or larger-runner allocation may be guessed or changed to fill a gap.

Until actual authorized evidence establishes otherwise, the Apple route remains
**external accessible compatible Apple-Silicon host required**. Physical
protection/biometrics and PVU004 integration remain open. No new simulator
harness, workflow/request, source edit or operational run is admitted here.

Operations in this review were bounded W retained-text/JSON reads, standard data
projection, SHA256 reads, coordination and this new report only. No network,
Git, SDK/process/runtime probe, project-helper import/execution, build/test/app,
CI, cleanup or credential inspection occurred. No persistent child or new stop
obligation was created. IOS01 FAIL/10UNSTARTED/0XML/unmet original close, Mac02 and
Windows05 qualified results, native refusal, all STOP/NO-RETRY/CLOSED/HOLD/PVD/
hardware restrictions, unchanged resource floors and occupied1017001 survive.
Metadata source acceptance creates no product evidence or denominator change.
