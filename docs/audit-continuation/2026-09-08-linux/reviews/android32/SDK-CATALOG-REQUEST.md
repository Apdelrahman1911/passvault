# PVA-001 — one bounded SDK catalog metadata read

Root renewed this previously unperformed source-data request after the test-only
manifest checkpoint. This is **not Android execution or build admission**.

- Official catalog: `https://dl.google.com/android/repository/sys-img/android/sys-img2-1.xml`.
- One HTTPS GET only; TLS validation, no redirects, retries or alternate catalog.
- Response limit: 2,097,152 bytes; socket timeout10 seconds; overall Python
  deadline30 seconds with an outer35-second process deadline and5-second kill
  grace. Request identity encoding; never decompress an unbounded response.
- Invocation: `timeout -k 5s 35s python3 -I -B -`, with an inline, data-only
  standard-library HTTPS/XML reader. No imported/executed downloaded code,
  old runner, SDKmanager, SDK program, archive request, install, ADB or emulator.
- Inputs: `gradle/libs.versions.toml` SHA-256
  `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a`,
  declaring minSdk24. Select exactly one non-obsolete stable `default;x86`
  package with API>=24: lowest eligible API, newest declared revision at that
  API. This selects a catalog candidate, not an installed/usable target.
- Expected retained evidence: exact catalog bytes/hash, selected package
  identity/revision, declared archive URL/size/checksum, applicable catalog
  license and emulator dependency, response metadata and failure disposition.
  XML containing a DOCTYPE/entity declaration is rejected; no external XML
  resource is fetched. Archive bytes/checksum verification remain unperformed.
- Allowlisted new evidence files in this directory: `SDK-CATALOG.xml` and
  `SDK-CATALOG-RESULT.json`. Exclusive creation only; preserve any partial/failure
  receipt rather than overwriting or automatically retrying it.
- Cleanup: close the response and all file handles, stop the bounded foreground
  reader, retain only compact catalog/receipt evidence. No SDK/cache/build
  output or persistent worker is created; no unrelated cleanup is authorized.
- Existing API35 x86_64-only image is rejected as this target. Absence of
  `/dev/kvm`, software-emulation support/performance, generated APK identity and
  resource/execution/cleanup admission remain separate questions. This request
  cannot establish a running Android32 process or a KDF pass.

Before this request, the official catalog had **not** been fetched in this lane.
The result receipt, not this request record, establishes whether it occurred.
