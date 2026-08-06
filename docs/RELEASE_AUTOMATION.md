# Release Automation

Mobile store uploads use the separately protected workflow documented in
`docs/MOBILE_STORE_RELEASE.md`. This document describes the multi-platform
GitHub Release packaging workflow.

The release workflow is intentionally fail-closed. It will not build public
artifacts until version metadata, publisher identity, contact details, and all
platform signing credentials are configured.

## GitHub repository variables

- `PUBLISHER_NAME`
- `COPYRIGHT_HOLDER`
- `SUPPORT_EMAIL`
- `SECURITY_EMAIL`
- `PRIVACY_POLICY_URL`
- `PROJECT_URL`
- `ANDROID_STORE_LISTING_URL` (optional until a listing exists)
- `WINDOWS_TIMESTAMP_URL` (optional; an RFC 3161 service)
- `MACOS_SIGNING_IDENTITY`
- `MACOS_NOTARIZATION_TEAM_ID`

Placeholder domains, local URLs, invalid email addresses, and missing required
values are rejected before any platform build begins.

## GitHub repository secrets

Android:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Windows:

- `WINDOWS_CERTIFICATE_BASE64`
- `WINDOWS_CERTIFICATE_PASSWORD`

macOS:

- `MACOS_CERTIFICATE_BASE64`
- `MACOS_CERTIFICATE_PASSWORD`
- `MACOS_NOTARIZATION_APPLE_ID`
- `MACOS_NOTARIZATION_PASSWORD`

See `docs/RELEASE_SIGNING.md` for certificate requirements and rotation rules.

## Publishing

1. Update every field in `version.properties`. `VERSION_NAME` uses semantic
   versioning; increment `VERSION_CODE` for every Play or App Store upload,
   including rejected and test builds.
2. Merge the release commit into `main` and complete the normal verification
   workflow.
3. Create and push an annotated version tag that exactly matches
   `VERSION_NAME`, for example:

   ```bash
   git tag -a v1.0.1 -m "Release PassVault 1.0.1"
   git push origin v1.0.1
   ```

4. The Release workflow verifies that the tag commit is contained in `main`,
   builds all packages, verifies platform signatures, consolidates the outputs,
   produces `SHA256SUMS.txt`, checks every checksum, and publishes one GitHub
   Release. `ALPHA`, `BETA`, and `RC` channels publish as prereleases;
   `RELEASE` publishes as the latest stable release.

Manual workflow dispatch builds and verifies the requested repository version
but does not create a tag or publish a release.

Consumers can validate downloaded files from the release directory with:

```bash
sha256sum --check SHA256SUMS.txt
```

Do not recreate or move an existing version tag. GitHub Release immutability
should be enabled in repository settings before the first public stable release.
