# Bounded team ownership

Root alone edits central ledgers and this file. Agents write their named review
subdirectory and only explicitly assigned source paths; report proposed overlaps
before editing. No agent launches builds/tests or commits/pushes.

| Task | Initial scope | Write authority |
| --- | --- | --- |
| android32 | PVA-001 source/ABI/native target and compatibility evidence gap | Review report; propose tests before source edits |
| storage | PVU-001/002/003 remaining admitted source investigation, storage/session guards | Review report; proposed fixes require independent confirmation |
| editor | PVA-007/031, PVU-004/005/006 unfinished UI/lifecycle work | Review report; propose nonoverlapping implementation/tests |
| native | PVA-010/014/036/037 Windows/macOS native test scope and CI evidence | Review report and new dedicated validation workflow/scripts after notifying root |
| build_config | Exact Gradle graph, JDK17, dependency/bootstrap diagnosis, confinement | Review report only; no dependency/version changes |
| verification | Independent 105-method matrix/source binding, runner/admission review | Review report only |
| baseline_coverage | Handoff transport qualifications, independent final coverage/report challenge | Review report only |

Pending results and findings are sent to root. Authorial findings do not enter
the confirmed count until challenged; edited source does not count as closure
until the required evidence is independently adjudicated.
