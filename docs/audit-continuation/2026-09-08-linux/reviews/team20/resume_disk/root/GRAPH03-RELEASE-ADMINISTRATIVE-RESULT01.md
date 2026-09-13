# Administrative result

The root administrative command completed Graph03 actual adoption, archived
its old scheduler, and replaced the live
scheduler with the released state. Printed identities:

- Adoption: 2051 bytes, SHA256
  `fad01732a0224af157a4e742c0d72cf83bf19199350e0ce0dd46972e8f8eb1ef`.
- Released scheduler: 22863 bytes, SHA256
  `2131b256bddfec097c4adb314fceb19d0acf2ad5afe63a1d0356158c5c317382`.
- Prior scheduler: 22260 bytes, SHA256
  `88485c6a65852501fb40eda3bfdaa696bc1993377751fc10fc4662f41aac099d`.

The command then exited1 while checking full `stat_result` equality during an
unrelated named task-file read. The failing metadata field was not retained;
no exact cause is asserted. The task-file copy was not performed. The three
completed operations above are not rerun. A separate task file was subsequently
created directly. No build/test, stop, process signal, or disposable-output
cleanup occurred; no new runtime obligation was created.
