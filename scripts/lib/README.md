# Shell Library Contract

Files ending in `.sh` in this directory are sourced libraries, not standalone
scripts. They intentionally do **not** set `-euo pipefail`: shell options set by
a sourced file alter its caller. Every caller must enable `set -euo pipefail`
before sourcing a library and must handle documented return statuses explicitly.

When adding a library, keep shell-option policy in the caller, document the
contract in the file header, and add the library to
`scripts/verify-shell-library-contract.sh`. When adding a caller, use strict
mode before `source`; workflow `run:` blocks require their own prologue.
