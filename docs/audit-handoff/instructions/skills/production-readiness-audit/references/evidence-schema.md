# Production readiness evidence schema

Use JSON so the ledger can be validated without a project-specific parser.

```json
{
  "schema_version": 1,
  "candidate": {
    "commit": "FULL_LOWERCASE_GIT_OBJECT_ID",
    "tree": "FULL_LOWERCASE_GIT_TREE_ID",
    "clean": true
  },
  "verdict": "READY",
  "gates": [
    {
      "id": "static-analysis",
      "status": "PASS",
      "evidence": [
        {"kind": "command", "ref": "./gradlew detekt"},
        {"kind": "path", "ref": "evidence/detekt.txt"}
      ]
    },
    {
      "id": "unsupported-platform",
      "status": "NOT_APPLICABLE",
      "reason": "No target or package is declared for this platform."
    }
  ],
  "artifacts": [
    {
      "name": "desktop-installer",
      "sha256": "64_LOWERCASE_HEX_CHARACTERS",
      "source_commit": "SAME_AS_CANDIDATE",
      "source_tree": "SAME_AS_CANDIDATE"
    }
  ],
  "manual_actions": [
    {
      "id": "physical-rtl-gesture",
      "owner": "release owner",
      "action": "Run the documented physical-device matrix.",
      "blocking": false
    }
  ]
}
```

## Status semantics

- `PASS`: executed successfully and has one or more evidence references.
- `FAIL`: executed and failed; include a specific reason.
- `BLOCKED`: required but cannot execute; include reason and owner.
- `NOT_APPLICABLE`: demonstrably outside project scope; include evidence-backed reason.

Use a blocking manual action when its absence prevents the stated verdict. A repository may be repository-ready while Store publication remains unauthorized, but the report must label that distinction explicitly.

## Evidence handling

Evidence may reference commands, CI runs, artifact receipts, or relative files. Do not store secrets, tester identities, user data, signing material, or private Store responses in a public ledger. Redact identifiers only when they are private; retain enough public identity/digest information to prove the artifact.

Record tool versions and full untruncated output for security-sensitive validators. If a command modifies tracked output, invalidate the candidate freeze and begin the final run again.
