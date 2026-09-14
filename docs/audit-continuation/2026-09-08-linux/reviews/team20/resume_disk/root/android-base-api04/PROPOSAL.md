# Narrow AGP superclass DATA capture proposal

Root author; source only, not execution admission. Android03 fails before producer
execution at Class.getMethod("getProgramClasses"). Retained API02 R8Task.class
names BaseR8Task as its superclass but only has call-site method references for
several helpers. No protected/private/public declaration is yet proved.

Propose one HTTPS retrieval of the exact already-known official AGP9.4.0 JAR
(12137130bytes, SHA256606a2136d291e69ba18c9b21672ff62de0af65c1eb3a9f69c0e7da6a490348ca).
No task/build invocation, SDK changes, runtime/HOLD/cache access or JAR execution.
Use bounded standard-library read-only ZIP decoding, not javap/Java processes.
Retain only exact BaseR8Task.class (<=256KiB), its parse of superclass/public and
nonpublic method flags/names/descriptors, and compact provenance. No temp disk,
private cache, server or worker. Short-lived process memory releases at exit;
HTTP context closes on success/failure. No automatic retry. Timeout20seconds per
read and external shell timeout60seconds, memory bounded by13MiB download plus
bounded class/ZIP metadata. HTTPS final URL and fixed JAR hash must agree.

A source DATA capture cannot repair the consumed Android03 run. Any successor
must preserve its failure and HOLD, and independently justify an accessible,
non-invasive API contract before another build. No getDeclaredMethod,
setAccessible, protected helper invocation or guard weakening is authorized.

Resource clarification:13MiB is the bounded download buffer, **not peak RSS**.
BytesIO, ZIP metadata, parsed strings and interpreter overhead add memory.
No hard process RSS cap is claimed; the live host has ample RAM and root's
60second execution bound applies. This does not justify parallel heavy jobs.
