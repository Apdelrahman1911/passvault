# Windows PVA-036 after-flush batch — source proposal, not admission

- New mechanism: direct CMake/MSVC x64 dedicated after-flush target; exactly `continue` and `process_death`. No Gradle graph/JNA/recovery or prior-case replay.
- Need Windows-2022, MSVC2022, SDK10.0.26100.0; one build worker, no node reuse. Two actual cases, not a full native suite.
- Configure120s, build240s, cases45s each; existing global480s and cleanup540s; workflow20min. Installed helper requires fresh request/independent review/source commit+tree, quiet local/CI slot and original Job/handle custody.
- Existing C++ synthetic fixture checks real FlushFileBuffers, known held temporary handle, original child identity, controlled death versus normal release, file identities/content and cleanup. No universal power-loss, filesystem durability, Hello or PVA037 allocation-cut proof.
- Only original private runtime and generated allowlist are disposable. Preserve compact log/XML/hash/cleanup evidence3days. Unknown failures retain HOLD; no old WindowsGraph03 cleanup/retry authority.
- This proposal reads source only. Its Windows lifecycle machinery is reused as source from an inert existing controller, not imported/executed. New flags/selection/native inputs and scope require independent review before install/activation.

## Fresh02 distinction (source only)

Original01 run34901080019 failed at the first synthetic seed write before the after-flush boundary. Preserve its one XML FAIL, second UNSTARTED and whole original runtime HOLD. The new test-only seed forwarding trace records reached/success stage bits and qualified observed error codes, without injected faults, extra filesystem probes, retries or production changes. Its independent source review and exact02 instance/execution admission remain required. This proposal does not authorize rerunning01, touching its HOLD runtime, reopening Graph03, modifying PVA037 seams or using real vault/provider state.
