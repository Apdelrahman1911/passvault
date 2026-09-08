# Coverage protocol

Baseline: 0dbc12c7f1b7770e75963c751c8c67af6e8b057a. Inventory is exhaustive, not proof of review.

Each reviewer writes append-only `events-<reviewer>.jsonl` here during actual review. Each event: `path`, `ranges` as inclusive `[start,end]` pairs, `reviewer`, `status` (`REVIEWED` or `PARTIAL`), `analysis` (actual control-flow/contract conclusions), `relationships` (caller/callee/guards inspected), `tests` (names/evidence, distinguish read vs run), `findings` (suspected IDs only), `evidence` (review note path). Empty/generated/binary files use `method`, no fictitious ranges; disposition `INSPECTED_GENERATED`, `INSPECTED_BINARY`, `INSPECTED_THIRD_PARTY`, or `BLOCKED`. Never mark lines just because displayed/searched/scanned.

Source stays immutable. Reports/fixtures/patches only in external audit area. Build owner /root only: agents must NOT run Gradle/Xcode/native/test commands; request bounded validation and await results. Standalone passive read commands are allowed. Independent verification requires a different agent and its own surrounding-source analysis and disposition.

Final aggregator merges inclusive ranges, validates bounds/path/reviewer attribution, and exposes every uncovered range. App-owned text line denominator = categories application_owned_text + application_owned_documentation. Generated/binary/third-party inspection denominators remain separate. Tests not executed remain verification gaps even if source-reviewed.
