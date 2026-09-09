# Prospective C7 loose-object timestamp qualification

Author `/root`; independent review is required before any Git mutation.
This supplements, rather than rewrites, the frozen publication scope and its
original 820-file baseline. No Git mutation has occurred as of this proposal.

Ordinary Git can freshen an already-existing identical loose object's timestamps
when staging duplicate content (including empty logs). Requiring unchanged
timestamps would reject a non-content change without protecting borrowed C4
reads further. Index-only staging would need another command exception and does
not establish that subsequent tree/commit writes cannot freshen existing objects.

Permit **only mtime/ctime differences on existing regular loose-object files**
whose relative names match `[0-9a-f]{2}/[0-9a-f]{38}`. Every original path, byte
hash, size, device/inode, type/mode, owner, link count and ancestor identity/mode
must remain unchanged and readable. No replacement, overwrite, removal, chmod,
ownership/link change, GC, repack, repair or retry is permitted. Pack/index/info,
configuration and other metadata files receive **no timestamp exception**.

Compare against the original baseline before staging, after staging/commit and
after push. Record every permitted timestamp difference with both original and
observed values; stop without repair for any other drift. Those point comparisons
cannot prove who changed timestamps, continuous absence of unknown writers, or
historical process settlement. Do not attribute a difference to Git as a fact
without independent evidence. Borrowed objects must remain available throughout.

All remaining C7-only selected publication rules remain unchanged, including
disabled hooks/signing/maintenance, one dedicated fast-forward commit/push, no
protected refs/tags and no runtime/cache/cleanup/recovery authority. Linux01/02
and every other existing FAIL/HOLD/restriction remain unchanged.
