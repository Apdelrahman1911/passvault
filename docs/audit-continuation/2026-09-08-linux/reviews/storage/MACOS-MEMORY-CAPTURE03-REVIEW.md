# Mac memory capture03 — independent recipe review

Reviewer: `/root/storage`; author: `/root/build_config`.
Reviewed only `reviews/build-config/MACOS-MEMORY-CAPTURE03-PROPOSAL.md`,
SHA-256 `22475238808ffe9827dc6aad9033079e8b4812829c6009d6abd0d99fd4f51ce9`.
The proposal hash was independently checked and its full text read as data.
No network request, downloaded code, helper execution/import, Git, build,
runtime inspection or probe was performed in this review.

## Decision

**Support the specified fresh, one-shot public source-data recipe for root's
explicit admission.** No additional harness or requests are needed. This is not
actual execution admission, a revival of capture01/02, or evidence that any of
the proposed remote identities or source contents have already been observed.
Its data-only work does not claim the build slot or permit a concurrent build.

## Independent checks and implementation qualifications

- Six sequential anonymous HTTPS GETs, with stop on the first failure, give a
  maximum **1,696 KiB / 1,736,704 response-body bytes** and **120 seconds of
  request deadlines**. These are body/request-time bounds, not an assertion
  about all HTTP/TLS overhead or total parsing/receipt time. No seventh request,
  redirects, retries, credentials, cookies, proxy fallback or alternate URLs.
- Install an exception-raising timeout handler and 20-second alarm before each
  request, with socket timeout and response-close/alarm-clear `finally` handling.
  Do not rely on a default fatal alarm to run Python cleanup. Record failures
  honestly if interruption prevents finalization; do not continue to the next
  slot. Reaching the body cap without positive completion is refusal, not EOF.
- XNU requests 2/3 use the same validated commit `C`. Validate the exact requested
  canonical path/ref and reported blob identity, decoded size and Git blob hash.
  This is an API-reported commit/path binding plus independently checked content
  hashes, not a locally reconstructed commit/tree graph.
- `system_cmds` remains an explicitly unverified candidate until request 4
  satisfies the identity rules. Request 5 must be untruncated and yield exactly
  one ordinary `vm_stat.c` blob; encode the validated branch as one URL component.
  Request 6 uses that blob SHA, not a guessed path or branch. Retain `T/path/B`;
  neither a printing-source commit nor a release version is established.
- The proposal's phrase "no release/source download" is understood narrowly as
  **no release artifacts or source archives**. The three specified Contents/Blob
  responses necessarily transfer selected inert source text; only those bounded
  API transfers are supported, never following `download_url` or running content.
- Use fixed output basenames in the fresh, root-created evidence directory and
  exclusive files; accepted remote paths are receipt data, not filesystem or
  shell instructions. Preserve only the three needed inert source texts and
  compact selection/identity receipts. Their Base64 body caps allow at most
  648 KiB decoded source, plus the 64-KiB receipt cap: below 1 MiB retained.
- Discarded complete API envelopes cannot later be independently rehashed from
  their receipt hashes. The proposal explicitly preserves that limitation.
  Incomplete prefixes must remain labelled incomplete and cannot authorize a
  dependent request or a source conclusion. Release response buffers and close
  handles; no temporary helper, background worker, cache or cleanup sweep is
  required. No capture01/02 output, held runtime or old helper is an input.

## Evidence boundaries

Fetched text still needs subsequent semantic interpretation and independent
challenge. Upstream XNU `C` and printing-source `T/path/B` do not establish the
installed macOS/kernel/userland version, actual available memory, non-overlap or
reclaimability of counters, product behavior, or hardware security.

The two proposed future receipt fields (`total_physical_bytes`, `launching`) are
not edited or admitted here. Preserve the existing 25/20-percent RAM and 12/8-GiB
disk predicates and all STOP/NO-RETRY/CLOSED scopes. No metric replacement,
unchanged-job retry, Mac test credit, product closure or denominator change is
authorized by this source-data review.
