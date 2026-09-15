# CI04 Windows launch repair and owner-authorized hosted parallelism

Owner explicitly requested cancellation ofCI03, repair ofWindows failure,
parallel workflow builds and a new run. This supersedes the serial hosted-job
restriction only; local heavy jobs remain excluded during the owned CI run.

CI03 Windows failed2of6synthetic resource-guard cases before Gradle: nativePython
resolved barebash to System32WSL, whose distribution was absent. Do not installWSL.
Preserve actual failure, completed source-bound evidence and cancellation receipts.
The actual caller'sBASH is exported by existing launcher, cygpath-native onWindows,
and required forWindows child/stop dispatch. Job assignment still precedes childgo.
Two focused selection/failclosed cases supplement six existing process/output cases.

Fresh local admission: after previousCIcompleted/locksettled/active0, run eight
synthetic guard cases via the actual launcher, Bash syntax and existing Ruby
workflow-security validator in one bounded120second isolatedHOME/TMP batch.
Record source hashes/explicitPGIDs, stop only owned workers if needed, verify
settlement before deleting new private temp roots. NoGradle/provider/application.
Normal3GiBdisk fallback/25%RAM floor; preserve logs/results and anyHOLD on failure.

New workflow graph: validate -> dependency-verification -> independent test,
Android, Desktop(max3), macOSpackaging(max2), sharedcompile and iOSsimulator.
At most9heavy jobs, on separate hostedVMs; none shares localVPS build resources.
All existing commands, JDK17/worker1, per-job35minute batch/45minute job bounds,
strict dependencyverification, signed-artifact restrictions and cleanup stay.
All-job CIGate remains failclosed; no secrets/signing/Store publishing permissions.
New corrected-source PR synchronize only after seal/review and oldrunsettlement.
Main/testing/release/tags/1017001 and STOP/NO-RETRY/HOLD scopes untouched.

Sealed corrected sourcea518a568f0b17a48144c053a43982d42aec125fc, tree66b346ee40bd62337535969bb969c5a0eb01dbb8; sixfiles bound in SOURCE-IDENTITY.json.
Supersedes all older source bindings AND the prior hosted-job serialization rule
per explicit owner instruction; per-host cleanup/limits/restrictions unchanged.
Syntax-first correctedlocal02 passed8cases (1.639s) andworkflowvalidator; initial
parsefailure/privateHOLD preserved, no Windowsruntime pass inferred.
