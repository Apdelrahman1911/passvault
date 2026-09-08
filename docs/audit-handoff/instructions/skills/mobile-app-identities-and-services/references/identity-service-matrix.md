# Identity-dependent service matrix

Fill this matrix from the target project before changing any identifier.

| Service/capability | Store identity | Development identity decision | Evidence to verify |
|---|---|---|---|
| Firebase / Google Services | Existing registered app | Register separately or disable locally | Processed config and runtime app ID |
| Crash reporting | Production project/app | Separate app/project or explicit dev collection policy | Uploaded symbols and event destination |
| APNs / push | Production topic/profile | Separate App ID/profile or unavailable | Entitlements, profile, token topic |
| Keychain access groups | Existing group | Separate or intentionally shared entitlement | Compiled entitlements and migration impact |
| App groups/extensions | Existing registrations | Register matching dev members if needed | Extension + container entitlements |
| OAuth callbacks | Registered production scheme/URL | Register explicit dev callback | Provider allowlist and callback test |
| Deep links | Production domains/schemes | Local-only route injection or registered dev contract | Manifest/associated domains and device test |
| Backend allowlist | Production package/cert or bundle/team | Add dev identity only if backend use is needed | Server config plus signed client identity |

## Android checks

Inspect the selected variant's merged manifest and resources, not only Gradle source. Confirm application ID, label, providers/authorities, permissions, exported components, deep links, signing certificate, and service JSON selection. Ensure normal IDE Run selects Debug without committing machine-specific IDE state.

## iOS checks

Query effective settings for both Run/Debug and Archive/Release. Inspect the built product's `Info.plist`, entitlements, embedded profile, extensions, URL types, and KMP framework mode. Scheme Archive must select Release even when Run selects Debug.

## Safe default decisions

- Preserve the canonical Store identifiers and registrations.
- Derive the local identifier with an explicit debug suffix when provider rules permit.
- Disable a Development-only service when the app does not need it locally; do not copy production credentials merely for symmetry.
- Do not add a testing identity for TestFlight or Play testing tracks.
- Add CI assertions that reject a development suffix or name in Store artifacts.

Changing Keychain/app-group identities can strand existing local data. Document whether coexistence, migration, or clean local state is intended before applying the suffix.
