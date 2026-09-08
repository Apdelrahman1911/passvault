# Independent publication-marker disposition

**ACCEPT — these five exact marker contexts in four pinned G12 files are intentional synthetic fixtures or source predicates.** No source replacement is warranted by these concrete flags. This is not general secret clearance, provider authentication, release approval, a source audit or an execution gate.

Reviewer: /root/remed_platform. Only the four expressly named files in the new handoff root were read, plus the pinned G12 source-manifest metadata. File hashing establishes identity; it does not mean the two large scripts were reviewed in full. All token/key bodies were suppressed before display and are absent from these reports.

## Exact dispositions

| File / flagged location | Classification and evidence |
| --- | --- |
| `scripts/testdata/security-analysis/rules/Rules.txt:2` | Deliberate expected-positive provider-token fixture, immediately under its explicit `ruleid` annotation. Its36 distinct body characters are exactly the complete uppercase alphabet followed by the first10 lowercase letters; zero digits. This is a deterministic alphabet-prefix placeholder, not unexplained credential material. |
| Same file, lines5–7 | Deliberate expected-positive private-key-marker fixture. The base64 body decodes to48 identical ASCII-A octets, not a DER SEQUENCE and therefore not a PKCS#8 private key. |
| `scripts/test-release-automation.sh:868` | Deliberately invalid PEM fixture written to `invalid_asc_key`: its7-character body is not valid base64. Lines870–872 explicitly fail if the validator accepts it. The adjacent positive-control key is generated only when that test runs; no positive private key is embedded in this inspected setup. |
| `scripts/validate-app-store-connect-key.rb:17` | An envelope-prefix string used by `start_with?`, paired with an end-marker predicate. It is a parser marker applied to external runtime input, not an embedded private key. |
| `scripts/validate-private-release-config.sh:862` | A `grep` envelope-marker predicate on runtime input, followed by the parser call. The literal match string contains no key body. |

The provider fixture classification is based on exact deterministic construction and expected-positive test context. No provider issuance, ownership, validity, revocation or account access was checked or claimed. Do not turn this disposition into a universal assertion that every provider-shaped token is harmless.

## Exact source identities

- Rules.txt —330 bytes/10LF; SHA256 `0e406b6aaa588e1668a1230b877a1780c8ba1e7eeda31bbaa237cd3df1b77104`.
- test-release-automation.sh —145239 bytes; SHA256 `e2830b2ec2284087b606b1a652fe38b33d75c87a1aa405e4e3661cc89aa4e3f0`; semantic display limited to845–892.
- validate-app-store-connect-key.rb —1089 bytes/40LF; SHA256 `28e8bd28457d44b7eefa63b38f1ba774060090cbd3e2ec43dc78930e2abc6738`; complete parser context read.
- validate-private-release-config.sh —55104 bytes; SHA256 `7172fcaf6c480c9922f1ed83b54a021f8b841a64abe3dca63133601a1210e3da`; semantic display limited to839–885.

All four match pinned G12 manifest `2f029f9cefd3262313e960b1bcc3cabd875d02ebe31a44e6e78f946bb3e98f1f`, canonical raw identity `7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91`. No Git history, index, remote or tracking state was independently queried here.

## Narrow publication use and retained boundaries

These exact hash/location dispositions can explain the specified publication flags without deleting test fixtures or weakening secret detection. They are not blanket path/glob allowlists: changed bytes or a new marker require a fresh bounded assessment. Other scan matches, packed reports, actual staged bytes, the final clone and public-secret clearance remain outside this review.

No inspected shell, Ruby, security-analysis tool, scanner, test or helper was executed/imported. Displayed key generation, OpenSSL, validation and network-related source statements remained inert. The only literal processing was bounded string/byte/base64 inspection; no crypto key parser, authentication or provider API was invoked. No real credentials, key files, signing configuration, vaults or private data paths were opened.

G7/G8 remain CLOSED. PVU007 STOP/no investigation or reformulation; PVU011 NO RETRY/no procedure inquiry or containment relaxation; PVA029 FAIL/no automatic test retry. Source, findings, verification, coverage, admission and release status are unchanged. Existing handoff-only Git authorization is separate and not expanded.

The first local pattern descriptions were intentionally not accepted when their predicates did not match. The final independent result is alphabetic, not the initial digits/alphabet guess. These refinements were inert report-only observations, not scanner retries or application test results. No raw token value, decoded key body or credential-derived hash is included.

This reviewer is independent of root's publication-screening disposition; prior implementation/patch work is not repackaged as a new independent whole-project review. Only this fresh report namespace is written, using exclusive0600 files in0700 with fsync and original-created/written-identity readback.
