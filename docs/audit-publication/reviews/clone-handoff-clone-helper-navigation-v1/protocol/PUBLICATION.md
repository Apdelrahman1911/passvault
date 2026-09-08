# Report publication protocol

Only the fresh report namespace is written. All helper sources are re-read with expected pins before allocation. Namespace creation is exclusive at mode 0700; files are exclusive, no-follow, mode 0600, flushed and fsynced. All data members receive stable guarded readback; the manifest indexes exact byte size, LF and SHA-256. Serialized stat identities use decimal strings. Parent/source identities are checked before and after.

PUBLICATION.json deliberately cannot index its own digest. The writer returns that pin externally. A separate read-only Python -I -B invocation then independently rehashes the manifest, all indexed members, and the exact source snapshots, validates the complete report-only namespace and modes, and reports its own capture externally. Neither receipt authorizes execution of archived commands or the helper. Completed packets are not reopened to insert their own final receipt.
