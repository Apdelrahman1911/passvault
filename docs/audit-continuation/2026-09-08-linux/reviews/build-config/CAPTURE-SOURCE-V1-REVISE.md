# Fixed source-capture utility: retained v1 REVISE

Utility author: `/root/build_config`. Independent source reviewer: `/root`.
The reviewer read the complete v1 source, SHA-256
`bf07c34053038d12cd27cd45e6f6e9e1995c029091a3bd8b4b90b3dad1373dc7`
(280 physical LF, 15158 bytes). Exact before-image: `CAPTURE-SOURCE-V1.py.txt`.
It is inert source evidence, **not executable/import admission**.

Disposition: **REVISE, source-only**. Root identified:

1. `directory()` opens a descriptor but calls `fstat` before registering it for
   cleanup; an exceptional `fstat` can leak it.
2. Source/output `fdopen` failures do not close the original descriptor.
3. Git cleanup's `wait(timeout=5)` exception bypasses pipe close and retains no
   original PID/settlement-uncertainty receipt.
4. Add bounded initial/prewrite/final disk/RAM observations and a five-second
   target in the long per-file loop; keep launch12GiB/25%, ongoing8GiB/20% floors.
5. Enforce the Git batch byte cap including headers and delimiters, not merely
   the sum of blob sizes.

The requested successor must always close original pipes even when settlement
is unknown, emit compact child intent/PID/actual-exit/settlement/HOLD records to
root's external stdout receipt, and avoid name/group signaling or retries.
Per-file stable reads remain point observations, not an atomic tree snapshot.

The author has not executed/imported/syntax-tested either utility generation.
`SOURCE.json` was absent at v1 submission and remains unavailable as evidence
until a separately accepted, root-executed capture and independent validation.
This revision is not an application finding, case, closure or skill-validator
substitute. Existing accepted Android/Apple/M03 report bytes are unchanged.
