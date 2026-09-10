# Mac memory capture03 — proposed source-data recipe only

Author `/root/build_config`; root alone admits execution. **No request has been
made under this proposal.** This is a fresh, bounded documentation/source capture,
not reuse of capture01/02 readers, failed commands, output roots, or permissions.
Those failures/HOLDs remain intact. No runner, build, probe, import, Git or CI work.

Purpose: establish the accounting behind `vm_stat`'s printed free pages and XNU
free/speculative/purgeable fields before considering any metric change. Source
semantics are not observed runner availability or proof of installed-version
equivalence. Preserve 25% launch/20% running RAM and 12/8-GiB disk floors.

## Six requests maximum; stop at the first failure

All URLs use anonymous `https://api.github.com`; no redirects, retry, credentials,
cookies, alternate endpoint, link following, or release/source download. Every
derived URL is recorded with its exact validated SHA/path **before** its request.

| # | Exact URL or metadata-bound derivation | Body cap |
| --- | --- | ---: |
| 1 | `https://api.github.com/repos/apple-oss-distributions/xnu/git/ref/heads/main` | 32 KiB |
| 2 | `https://api.github.com/repos/apple-oss-distributions/xnu/contents/osfmk/mach/vm_statistics.h?ref=C` | 96 KiB |
| 3 | `https://api.github.com/repos/apple-oss-distributions/xnu/contents/osfmk/kern/host.c?ref=C` | 512 KiB |
| 4 | `https://api.github.com/repos/apple-oss-distributions/system_cmds` — **candidate repository verification**, not a previously captured identity | 32 KiB |
| 5 | The verified repository's canonical `/git/trees/ENCODED_DEFAULT_BRANCH?recursive=1` endpoint | 768 KiB |
| 6 | The unique verified `vm_stat.c` entry's canonical `/git/blobs/B` endpoint | 256 KiB |

Sum of response-body caps: **1,696 KiB / 1,736,704 bytes**, below 2 MiB. Each
request has a **20-second total deadline**, at most 120 seconds of request time.
Requests are serial; parse/validate one response before proceeding. HTTP error,
redirect, timeout, rate limit, oversized/incomplete body or ambiguous metadata
ends capture03 with a compact failure receipt; no later slot or automatic retry.

### Metadata rules: do not manufacture a path or release

1. Validate response 1 as `refs/heads/main`, object type `commit`, a full lower-case
   40-hex SHA `C`, and canonical Apple-XNU API URLs. Pin both source requests to
   that **same** C; do not fetch the moving branch again.
2. For each Contents response require the exact repository/path/ref, type `file`,
   supported Base64 encoding, declared size matching decoded bytes, and canonical
   blob URL. Verify Git blob SHA-1 over `blob LENGTH\0` plus decoded bytes and
   retain a SHA-256. Do not follow `download_url` or execute the content.
3. Existing local capture records contain **no verified `vm_stat` printing-source
   repository/path**. Therefore request 4 explicitly tests the `system_cmds`
   candidate. Require `owner.login=apple-oss-distributions`,
   `full_name=apple-oss-distributions/system_cmds`, nonempty `default_branch`, and
   matching canonical repository/tree URL templates. An absent/moved repository
   is a blocker, not permission to search guessed alternatives.
4. Request 5 derives its branch from response 4 (URL-encoded as one path component).
   Require `truncated=false`, a full 40-hex root tree SHA `T`, and exactly one
   ordinary blob whose path basename is `vm_stat.c`. Record its **actual** path,
   mode, size, blob SHA `B` and canonical Git-blob URL. Missing/ambiguous selection
   stops; do not assume `vm_stat.tproj/vm_stat.c` or fetch another filename.
5. Request 6 fetches B, not a mutable branch/path. Require its SHA/declared size/
   Base64 data to agree with the accepted entry and independently compute the Git
   blob hash and SHA-256. The printing-source identity is **T/path/B**; no commit
   or release version was observed for that tree. Do not call it a macOS15.7.9
   source match. XNU C is likewise a captured upstream commit, not a guessed
   mapping to runner kernel `24G830`.

## Direct bounded retrieval, not a new capture runner

Root can issue each admitted literal request directly with a short isolated
Python standard-library cell (`python3 -I -B`), rather than create another helper:

- `urllib.request` opener with `ProxyHandler({})`, an HTTPS handler using
  `ssl.create_default_context()`, and a redirect handler that refuses redirects;
  fixed `Accept: application/vnd.github+json`, `Accept-Encoding: identity`, and
  an ordinary audit User-Agent. No Authorization header, cookie jar, `.netrc`,
  proxy credentials, downloaded module, or environment-provided token.
- Install a 20-second process alarm and response-close `finally` **before** the
  request. Also set the socket timeout. Accept only HTTP200, unchanged exact URL,
  and no content encoding other than identity. Stream at most the slot cap into
  memory; reject a declared larger body before reading. If the cap is reached
  without established completion, stop without an extra read or success claim.
- Validate inert JSON/source as above. Close the response, clear the alarm and
  release its buffer before the next request. No source is evaluated/imported;
  source text is data, never an instruction or shell fragment.
- Write only to a root-created **fresh** `reviews/build-config/macos-memory-capture03/`
  evidence directory, using exclusive files. No temporary file, cache, daemon,
  background job, old held namespace, or old source-capture helper is involved.

Retain three necessary decoded inert source texts (not executable), plus compact
receipts: original/final URL, HTTP status, time/deadline, actual body bytes/full
body SHA-256, completion/failure, accepted repository/ref/commit/tree/path/blob
fields, decoded size/SHA-256/Git hash. Keep metadata needed for the accepted
selection; omit unrelated recursive-tree entries and Base64/API envelopes after
validation. A receipt's original-body hash is a capture assertion, not a claim
that discarded full JSON can later be independently rehashed. Bound total compact
receipts to 64 KiB; decoded source plus receipts stays below 1 MiB at these caps.
Failure prefixes, if necessary, must be marked incomplete and must not authorize
downstream requests or source conclusions. Record ordinary process exit and any
close/receipt failure honestly. No build artifacts or cleanup sweep is needed.

## Smallest evidence-only helper delta — not applied here

Current `scripts/audit/macos_focused_validation.py` SHA-256:
`4448517d4615a34090d2acc61cd8eb872d3e8c1445fd711e8d7aa146f243b69c`.
At the existing `resources()` append, add only:

```python
"total_physical_bytes": self.total_ram,
"launching": launching,
```

These two fields, existing free/disk bytes and the bound helper source suffice
to recompute the exact existing predicate and phase-specific floor. Optional
page-size/free-page fields improve parsing provenance but are not required for
this minimal correction. Keep the observer, `25/20` percentages, `12/8` disk
floors, invalid-relationship check and failure/no-retry behavior unchanged.
No extra OS command is needed. This does not repair missing historical totals or
justify an unchanged-job retry. The helper edit requires separate authorship and
independent review; no metric replacement is allowed before non-overlap and
reclaimability proof and compatibility challenge.

All product counts, Mac5 five UNSTARTED cases, hardware blockers and STOP/
NO-RETRY/CLOSED/publication boundaries remain unchanged by this proposal.
