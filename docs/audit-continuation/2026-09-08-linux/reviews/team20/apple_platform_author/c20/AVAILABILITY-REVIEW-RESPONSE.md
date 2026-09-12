# C20 availability proposal: response to independent source challenge

Author `/root/apple_platform_author`, 2026-09-11. This records the additive
`/root/apple_platform_review` challenge of the three-command **inert metadata
proposal only**. Original Apple route/test reports, after-image and independent
source review remain frozen and untouched. No metadata command was run by this
author and no collector or execution instance is admitted here.

Prior availability-proposal SHA256:
`c3fee6676309ac3ef2ddfbcd46de5e328b695f57ae3e51f095c922d8d9dc268f`.
The fixed endpoints, command argument lists and their invocation/byte/time
ceilings are unchanged. Accepted clarifications added to that proposal:

1. Reject/scrub inherited debug/HTTP/auth/shell tracing without environment
   disclosure; disable TLS-key logging. Give documentation curl only a minimal
   root-bound PATH/LANG/LC_ALL/TZ environment, never GitHub credential variables/
   configuration, tracing or proxy credentials. No extra discovery/auth command.
2. Validate exact JSON object/field types, integer-not-boolean IDs, all relevant
   booleans, owner.login and fixed public repository identity. Runner/label shape,
   bounded control-free text, positive unique IDs and exact boolean busy are
   explicit. Unknown label/OS/status is not an affirmative candidate.
3. A well-formed matching repository response with admin=false skips command2
   only; public documentation command3 may proceed. An operational child error,
   missing/malformed metadata, identity change, bound violation or ambiguous
   custody aborts the remaining commands. No fallback or pagination/retry.
4. Accept curl status only from its complete final suffix, never an HTML body
   search. The complete untruncated capture and actual exit0 are prerequisites.
5. At most three CLI invocations does not establish exactly three HTTP
   transactions: gh's internal redirect/auth/retry behavior was not inspected.
   No application-level retry or new endpoint is admitted by that qualification.

These are collector requirements for root's separate implementation/instance
review, not observed enforcement. No additional helper, workflow, request,
host/toolchain probe, source edit, runtime pass, defect or closure was created.
The originally accepted tiny PVA014 default-publication test proposal remains
unapplied; Apple hardware/resource/availability and all non-replay fences remain.
