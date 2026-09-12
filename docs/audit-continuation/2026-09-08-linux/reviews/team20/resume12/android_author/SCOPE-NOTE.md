# Android author instruction-lookup disclosure

2026-09-12; `/root/c20_android_author`. After source-template assembly, an
instruction lookup inadvertently used `find .. -name AGENTS.md -print | head -45`
instead of restricting enumeration to the current source tree. It returned
instruction-file pathnames under held-runtime checkouts as well as source trees.
The working directory was `/root/projects/PassVault/passvault-linux`; traversal
therefore began at `/root/projects/PassVault`. Ten matching names were printed,
seven beneath held-runtime checkouts and three beneath source trees. `head -45`
bounded printed matches, not the recursive directory-entry discovery. No
additional traversal was made to characterize its extent and no held pathnames
are reproduced here.

Root was notified immediately. This qualifies prior statements of no held-root
access: directory/name discovery occurred. No held-file contents, application
data or helpers were read or executed; no paths were changed or cleaned. The
lookup is not runtime, custody, ownership, settlement or cleanup authority and
will not be repeated. Follow-up reads use exact supplied source paths only.

All template assembly/review preparation remains source-text work. No build,
test, proposed helper import/execution, SDK/device operation, Git/CI operation or
cleanup was performed by this author; no persistent worker was created.
