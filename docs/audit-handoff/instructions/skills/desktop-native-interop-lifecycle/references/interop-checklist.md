# Desktop native interop review checklist

## ABI contract

| Question | Required evidence |
|---|---|
| Is the ABI versioned? | Exported version function and mismatch test. |
| Are sizes portable? | Fixed-width integers and explicit maximums. |
| Who owns memory? | Allocation/free pair documented for every pointer. |
| Which encoding is used? | UTF-8/UTF-16 contract plus malformed-input tests. |
| Which thread may call? | Thread-affinity assertion or documented synchronization. |
| How are errors returned? | Stable codes; no exception crossing a C ABI. |

## Operation lifecycle

Use an opaque operation ID and state machine such as:

```text
Idle -> Starting -> Active -> Completing -> Idle
                    |             |
                    +-> Cancelling+
```

Reject a second operation when one is active. Do not queue authentication or secret-bearing native calls. Hold context ownership until the native call and all callbacks finish. A late callback must match the active operation ID before it can mutate state.

## Sensitive buffers

- Bound lengths before allocation.
- Prefer mutable direct/off-heap buffers for native inputs.
- Clear managed, direct, and native copies in independent `finally`/RAII cleanup.
- Avoid immutable `String` for secrets when the surrounding APIs permit.
- Do not log pointers, secrets, decoded credential files, or raw native errors that contain them.

## Loader policy

Require an app-owned regular file under the packaged runtime root, a non-symlink path, expected filename, platform, architecture, ABI, and integrity policy. Reject global library paths and temporary extraction fallback. If production signing changes bytes, validate the trusted signature before recording or accepting the post-signing digest.

## Packaging matrix

Build and launch every claimed architecture on its native runner. Check:

- Gradle configuration succeeds on unrelated operating systems;
- target tasks are registered lazily;
- CMake/toolchain output matches the package architecture;
- the installed path equals the loader's expected path;
- update/install preserves permissions and signing;
- no development fallback path works in a production package.

## Shutdown matrix

Exercise close from window chrome, menu, dock/taskbar, OS shutdown signal, and active native prompt. Cancel work, hide sensitive UI, clear clipboard, close storage, remove tray/menu integrations, and terminate within a documented bound. Capture a JVM thread dump plus native hang sample before changing shutdown behavior.
