# Candidate attestation source policy

The candidate commit **C** and tree **T** remain the identity of the tested bytes.
A later workflow invocation **D** does not change the candidate, receipt, tag or
Store build number. GitHub's attestation certificate truthfully records D, not
the `sourceCommit` field inside the attested JSON.

`scripts/verify-candidate-attestation.rb` first requires successful GitHub CLI
verification of the exact subject, repository, approved testing/readiness signer
workflow, `refs/heads/testing`, GitHub OIDC issuer, SLSA-v1 predicate and GitHub-hosted
runner policy. It then accepts a certificate's verified source digest only if:

1. C is an available commit and its actual Git tree equals recorded T; and
2. the certificate digest is C; or it is an available descendant D of C whose
   entire Git tree also equals T.

The digest must come from the **same successful verified result row**, at
`verificationResult.signature.certificate.sourceRepositoryDigest`. Subject JSON,
predicate claims, raw downloaded certificates, missing fields, or successful
process exit alone cannot substitute for this check. At least one complete row
must qualify; unrelated additional attestations cannot override a valid one.
Malformed JSON fails as a document; an unusable extra row cannot qualify or
provide fields to another row, but does not invalidate a complete qualifying row.

Each subprocess capture is bounded to 4 MiB of combined stdout/stderr; the
verified result is limited to 256 rows. The CLI has a 180-second deadline; individual Git queries have
30-second deadlines within a 240-second verification budget. Output overflow,
timeout or cancellation signals the helper-owned child process group and reaps
its direct child. On the supported POSIX workflow hosts, the dedicated CLI sets
SIGCHLD to its default disposition and is the sole reaper. Direct module callers
must preserve those preconditions; importing the module never changes a host's
signal disposition. The helper keeps the leader PID
unreaped through TERM, a one-second grace period, and KILL, so the numeric group
ID cannot be recycled before signalling finishes. It never signals that group
after reaping. This is not a claim to reap grandchildren or revoke bytes a
subprocess has already sent. CLI INT/TERM/HUP handlers only request cancellation;
cleanup happens at explicit safe points. Stderr is drained and counted, not
mixed into verified JSON. These are fail-closed work limits plus termination
cleanup, not a promise to recover from SIGKILL or terminate blocked kernel I/O
instantaneously. No shared or unrelated process is selected by name.

The JSON contract and existing CLI policy were inspected in GitHub CLI v2.81.0
(`52ba8366057b94a77d2e4b2fc99de56e100e45bf`) and its sigstore-go v1.1.0 dependency.
An incompatible CLI output fails closed. This is not a claim that every hosted
runner has that exact CLI version or that a live attestation was verified by the
local regression tests.

Consumers require their **own full-history checkout**, including the testing
history containing D. Missing/unreachable commits still fail closed. Equal trees
without C→D ancestry, changed trees, invalid signatures, wrong identities and
self-hosted runners remain rejected. No commits are fetched from untrusted JSON.

Original mobile-upload receipt discovery remains bound to the exact upload
commit. Final production-signed Desktop assets remain bound to the exact release
commit and production-signing workflow. These boundaries are not widened.

Manual approvals, owner/bot dispatch checks, immutable tags, receipt hashes, schema
validation and actual Store build checks remain separate requirements. Schema-2
mobile candidates are not given invented Desktop receipts. Mobile promotion does
not rebuild or replace binaries; Desktop publication remains separate.

## Non-publishing regression commands

```sh
ruby scripts/test-candidate-attestation.rb
ruby scripts/test-release-regressions.rb
```

The first uses a synthetic local Git graph and a fail-closed `gh` double. It tests
production helper parsing, source relationships and workflow wiring, **not real
certificate issuance, hosted Actions execution or store readiness**. No signing,
network upload, candidate mutation or production publication is performed.
