# TRAY01 outer: tray/lifecycle/Room source delta

2026-09-10 — author `/root/verification`. **Unexecuted source, not admission.**
This supersedes only the tray-only workload description in the preserved outer
`PLAN.md` (`5aff66a93e5700ce0e5b8a7f9d1e3a301f53b808c3ca1a1af4dbbf7a60f8c7c7`).
No historical review, failure, consumed instance or restriction is replaced.

## Frozen source tuple

Paths are relative to the continuation checkout; B is
`docs/audit-continuation/2026-09-08-linux`.

| Source | SHA256 | Bytes / LF |
| --- | --- | --- |
| B/reviews/desktop-tray-outer/LAUNCH.py | `bf109d60c3c1a94071b0cfb21dc6b78db4d6aedc2cea00408756b1416af411dc` | 45542 / 779 |
| scripts/audit/linux_desktop_tray_01.py | `994602c5df621a9f7a775921968338fca9415d895a5cb1fcb50a534edc84fb10` | 87116 / 1438 |
| scripts/audit/desktop_tray_01.init.gradle | `5b875cad3086f7b0f700ab531e02bccf50d0b3d4d401fa9f63e88d65ecb81560` | 10830 / 187 |
| B/reviews/desktop-tray-inner/PLAN.md | `9e3a0d25ff19643e570e0526dedb89a1690aec5c6e31903de9686152fb4fac4f` | 34077 / 533 |

The outer preimage was `f437d04b04a27c4bea0884c3e2fad46422aa2a61b787ccc118a169abdd11f75d`,
44613B/770LF. Inert writer/readback `f4e960` made exactly seven textual replacements
and emitted the complete unified delta. Input/schema reads: `9f385e`, `36b577`.
These receipts are source I/O, not helper execution, imports, compilation or tests.

## Minimal outer changes

- Describe **three prospective JUnit methods / three fixture XML suites / two
  serial Gradle Test tasks**, in one prepare/render cycle. Lifecycle's at-most-two
  serial child-JVM modes are controls within one case, not two additional cases.
- Add only `/usr/bin/xfconf-query` to the existing original-image admission path.
  It is not an outer-launched helper. Live image/instance pins remain pending.
- Add `editor-room` beside `desktop-tray`, with the same eight private worker
  children. Add original empty `R/lifecycle` to TOP and the cleanup allowlist,
  and original empty `E/lifecycle-evidence` to allocated evidence directories.
  This yields 38 original allocated directories, 18 required images and 24 outer
  images; no new generic signalling, deletion or recovery mechanism.
- Require typed true `lifecycle_evidence_preserved` alongside existing safety
  evidence. A safely retained empty/partial lifecycle set may still be code1;
  successful lifecycle mapping is not imposed on an unstarted/failed workload.
- **Code0 only:** require the three tray booleans
  `panel_configuration_verified_before_launch`,
  `original_tray_owners_captured_before_test`, `original_tray_owners_settled`;
  plus existing validation/XML/pixel success and `lifecycle_mapping_ok`.
  Require actual integer values: declared cases3, declared XML suites3,
  preserved XML suites3, declared Test tasks2, expected lifecycle files6,
  planned child modes2 and crash diagnostics0. Integers cannot be booleans.
  The exact captured INNER-RESULT also binds original tray-owner records;
  these result checks grant no extra process ownership or signal authority.

The three cases and exact fixture/production-source hashes are in the frozen
inner selections and final inner PLAN. Earlier tray-only inner acceptance
(`44b3412fa818ff67574e4a218337c4cddbd9530def9dfae62341ccf37404c96b`)
does not automatically accept this batch. `/root/native_review` reviews its
inner/init delta; `/root/editor_review` independently challenges this outer/note.

## Retained evidence accounting — source bounds, not observed output

The final inner source accounts for 87 successful inner-owned E files:
25 command pairs=50; four GUI-helper pairs=8; 16 JSON; three XML;
four tray captures; six lifecycle receipts. The six required lifecycle files
(default/production .events/.log/.exit) total at most 532512B. Only two optional,
bounded textual child-crash copies (at most 1MiB each) may be added after owned
namespace settlement; any retained crash prevents success. Unsafe evidence
path/type/size/identity/count conditions remain HOLD before runtime deletion.

Inner maximum94 = 87 + two optional crash copies + five additional XML
(up to eight XML on failure). Its 112MiB aggregate limit covers **inner
Files.evidence only**, not the whole E directory.

The unchanged outer writes six normal files: OUTER-ALLOCATION.json,
OUTER-INTENT.json, OUTER-RECEIPT.json, logs/outer-git.stderr,
logs/outer-unshare.stdout and logs/outer-unshare.stderr. Its unchanged latch may
also create one exclusive E/CANCEL after inner completion, including on a later
outer source/authority/resource/cleanup failure. Hence **93 total E files on
success; conservative maximum101 = 94 + 6 + 1**, not100. The maximum is a
conservative source bound, not a claim that all failure paths co-occur.
The optional seventh outer file was specifically challenged and independently
corroborated; it does not require changing the inner-owned-files cap.

## Unchanged authority and qualifications

The entire generic region, `def require` through before `def main`, is byte
identical to the preimage: 24995B,
`09fe655928092457a71f948dfeffd167b819fd74ec7dbcd8aa257633c18bff27`.
The complete `__main__` body is also identical: 2059B,
`19916c3030019942a4b01f5ebf1793afb27dd5bd71e6e142b2b77eb59524460a`.
The fixed scrubbed entry, four original-parent bindings, Python symlink exception,
6000s/5250s/750s bounds, original-domain settlement, stop and guarded cleanup
remain unchanged. Code1 cleaned failure remains distinct from code70 HOLD.

GITDIR, COMMIT, TREE, MEMBERS, DEVICE, EXPECTED_LOCK and all FROZEN values remain
None. `/root/storage` remains the mandatory genuine independent instance
approver; this author cannot self-approve. Fresh publication, coordination,
execution and cleanup admission are still required. No old helper is replayed,
imported, tested or granted new authority by this delta.

Zero executed cases or closure credit here. Native text/captures still need
independent actual review; lifecycle is not full Main/provider cleanup proof;
Room is not navigation/race/IME proof. Hardware/platform gaps, PVD boundaries,
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029's failure/no automatic retry, G7/G8 CLOSED,
and all historical HOLD/consumed scopes remain intact.
