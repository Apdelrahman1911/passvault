# Instruction-discovery enumeration qualification

Root's initial resumed `find .. -name AGENTS.md` used output exclusions for
`audit-runtime-*` descendants, not traversal pruning. It therefore enumerated
sibling directory entries, including held-runtime descendants, although those
paths were excluded from its output. No held AGENTS file content was read by
that command. The native author separately reported an unpruned instruction-file
search that returned the held Linux02 `checkout/AGENTS.md` path; it did not open
that file or invoke Git/helpers/process/recovery operations there.

These walks were broader than the intended source-only discovery. Do not infer
unchanged access metadata from them or call the held namespaces wholly unvisited.
No follow-up inspection, repair, deletion, adoption or replay is authorized.
All prior FAIL/HOLD and STOP/NO-RETRY/CLOSED restrictions remain unchanged. The
fresh isolated workload uses neither held checkout nor cache; its independent
admission and original-directory cleanup authority are separate.
