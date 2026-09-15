# Release boundary regression fixtures

Run from the repository root with Ruby 3.3 (the release workflow runtime):

```sh
ruby scripts/test-release-regressions.rb
pwsh -NoLogo -NoProfile -NonInteractive -File scripts/test-windows-checksum-boundary.ps1
```

The Ruby suite uses production validator/helper CLIs and selected actual workflow run blocks. It scrubs child environments, allocates disposable HOME/TMPDIR directories, and intercepts every `gh`, `git`, and `security` command with an exact, fail-closed provider queue. Additional network/build/signing command names are intercepted as forbidden calls. A cancellation case sends TERM only to its own fixture shell. No real providers, credentials, user keychains, signing, builds, or publication are used.

The PowerShell suite executes the production checksum helper against inert text files and a fake Authenticode provider. It sets the platform flag only inside its disposable test process and restores it; passing this suite is not Windows build or signature evidence.

## Contracts

- Historical schema 2 is supported only by explicit `--mobile-only` candidate validation. It retains the original mobile manifest/receipt bytes, source commit/tree, Store identities, version/build, signing fingerprints, and receipt digests. Production workflows still require their original readiness, source and attestation gates. No metadata is upgraded/re-attested in place.
- Schema 3 remains the default Desktop contract, including the Desktop receipt. Mobile-only production entrypoints do not download or attest Desktop inputs. Mobile promotion does not authorize Desktop publication or rebuilding occupied build `1017001`.
- New fixtures for schema 3 and artifact bytes are entirely synthetic. Files named as packages are inert text, never installable artifacts.
- Resume must match the current Git tree before exporting reusable receipts. Paginated API collections are explicitly slurped/validated. Attestations and same-history source checks remain mandatory.
- Stable publication verifies the exact peeled remote tag, creates only a missing tag without force, reads it back, and uses `--verify-tag`. A concurrent mismatched tag or existing release is never overwritten. Protected tags/release permissions and protected manual approval remain required; external retargeting cannot be made atomic with publication by this script.
- Optional obsolete tester secrets require checked DELETE and confirmed absence. Private configuration always names the eight canonical metadata payloads; the actual archived snapshot is revalidated/rendered before upload.

## Retained public metadata

`schema2-1017001/` contains byte-identical JSON metadata retained from the public candidate release on 2026-09-05. `public-fixture-provenance.json` records source URL, commit/tree and SHA-256 values. These files contain no signing keys, credentials, testers or binaries. They prove offline compatibility only, not present-day artifact availability, attestation validity, Store state or permission to mutate the historical release.

Authoring fixtures is not verification. Record executed commands, runtime, actual results and remaining native/live gates separately.
