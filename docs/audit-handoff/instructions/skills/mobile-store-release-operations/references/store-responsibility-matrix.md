# Store responsibility matrix

Verify current Store APIs and agreements before relying on this matrix; available automation changes over time.

| Concern | Repository/CI can prove | Store API may automate | Human/owner remains responsible |
|---|---|---|---|
| Binary identity | Package/bundle ID, version, signer, permissions, SDKs | Upload/processing receipt | Existing app ownership and agreements |
| Testing | Tester file syntax, build receipt | Group assignment/track promotion where supported | Tester consent, eligibility, actual membership |
| Privacy/data safety | Code/SDK/network evidence and declaration draft | Metadata upload on supported APIs | Legal truthfulness and Console attestations |
| Content rating/audience | Source-backed questionnaire draft | Limited metadata operations | Final declarations and regional obligations |
| Screenshots/listing | Dimensions, format, locale, placeholder scan | Asset/metadata upload | Visual approval, marketing/legal claims |
| Review | Submitted build/metadata consistency | Submit or read state when supported | Resolve review questions and approve submission |
| Pricing/availability | Desired config validation | Some territory/price updates | Contracts, tax/banking, owner pricing decision |
| Publication | Exact approved build receipt | Phased/manual release controls | Explicit authority and launch decision |

## State vocabulary

Keep these states distinct in automation and reports:

```text
built -> uploaded -> processed -> available to testers
      -> submitted for review -> in review -> approved
      -> pending developer release -> live
```

Do not infer a later state from an earlier one. Read it back from the Store after mutation.

## Declaration evidence

For each answer record:

- exact field/question and selected answer;
- source/artifact evidence path;
- permissions, SDKs, data types, purposes, sharing, retention, deletion, and encryption;
- reviewer and review date;
- uncertainty or owner/legal confirmation required.

Inspect user-controlled export and backup separately from developer collection. A file leaving through a user-selected provider can affect disclosure without being analytics or advertising collection.

## Screenshot safety

Validate format and dimensions mechanically. Require a person to confirm that images contain fictional data, correct locale/theme, no system/account identifiers, no debug UI, and an honest current UI. Never automate removal from review or asset replacement without explicit authorization.
