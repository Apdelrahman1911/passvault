# Root selection for the one passive Gradle lookup

Source-only clarification before any lookup, not permission to execute a graph.
The five exact top-level basenames in the author's request remain selected.
In that same single bounded central-directory scan, also emit nested class names
whose basename starts exactly `ToolchainBuildOptions$` or
`ToolchainConfigurationBuildOptions$` and ends exactly `.class`. No other prefix,
extra directory, second lookup or source download is authorized by this choice.
Full archive names must be ordinary Java binary-name components; reject malformed
or ambiguous duplicates rather than accept extra candidates. Keep the existing
32 matched-name limit, 256 JAR/8MiB central-directory budget and 3 selected owning
JAR/64MiB total byte-hash limit. Stop on exceeded bounds.

The subsequent one static javap, if separately admitted, may select at most eight
actually observed relevant top-level/nested classes. This is not a wildcard
classpath or class initialization. If evidence lacks the required ingress edge,
retain the missing edge; no blind second scan or speculative graph retry.
Installed implementation explanation is separate from consumed Graph03 byte
identity. Original STOP/NO-RETRY/CLOSED/native/HOLDs remain unchanged.
