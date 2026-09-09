# Current-cycle02 — fixed commands, borrowed C4 objects

Source proposal only. Root executes; `/root/storage` author;
`/root/editor_review` independent successor reviewer. Consumed01 remains FAIL,
cleanup HOLD, all167 declarations unstarted; these are not retry01 commands.

C4 commit `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. Existing01 `SOURCE.json` and
build-config/current-cycle01 `CLASSES.tsv`/`METHODS.tsv` remain immutable inputs.
Selections are unchanged: ordinary163 plus1 producer/3 consumers =166 regression
methods and1 fixture producer. Source bootstrap alone gives zero test credit.

## Exact fixed bootstrap

Working directory is the new original `R/checkout` throughout. `R` is
`/root/projects/PassVault/audit-runtime-linux-current-cycle02`. The driver uses
its unchanged fixed GIT prefix (hooks/fsmonitor/GC/maintenance disabled, LF).
It no longer launches a transport clone, upload-pack, bundle or ref-discovery job.

```text
/usr/bin/git --no-pager -c core.hooksPath=/dev/null -c core.fsmonitor=false
  -c gc.auto=0 -c maintenance.auto=false -c core.autocrlf=false -c core.eol=lf
  init --object-format=sha1 --template= R/checkout
```

This is argv notation, not a multiline shell command. After the original own
`.git/objects/info` handles/config are acquired and validated, an exclusive
`R/checkout/.git/objects/info/alternates` contains exactly:

```text
/root/projects/PassVault/passvault/.git/objects
```

One LF terminates it. The separately pinned/frozen source object store is
**borrowed**, not independently cloned or copied. No source Git write/GC is
allowed. The same GIT prefix then runs `-C R/checkout checkout --detach` with the
exact C4 commit; complete original commit/tree/member/mode/byte checks remain.

## Exact five Gradle calls and original stops

This Bash notation defines argv only; root supplies separate admission/cleanup.

```bash
R=/root/projects/PassVault/audit-runtime-linux-current-cycle02
WRAPPER="$R/checkout/gradlew"
INIT=/root/projects/PassVault/passvault-linux/scripts/audit/current_cycle_tests_02.init.gradle
SAFETY=(
  --no-daemon --max-workers=1 --console=plain --no-parallel
  --no-configure-on-demand --no-configuration-cache --no-build-cache
  --dependency-verification=strict --stacktrace
  -Pkotlin.compiler.execution.strategy=in-process
  -Pandroid.builder.sdkDownload=false
  '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8'
  -Dorg.gradle.java.installations.auto-download=false
  -Dorg.gradle.java.installations.auto-detect=false
  -Dorg.gradle.java.installations.paths=/usr/lib/jvm/java-17-openjdk-amd64
)
"$WRAPPER" --init-script "$INIT" -Ppassvault.audit.mode=ordinary \
  :core:crypto:tasks --all :core:database:desktopTest \
  :feature:credential:desktopTest :shared:desktopTest "${SAFETY[@]}"
"$WRAPPER" --init-script "$INIT" -Ppassvault.audit.mode=prepare \
  :core:database:desktopTest --offline "${SAFETY[@]}"
PASSVAULT_PVA038_FIXTURE="$FIXTURE" "$WRAPPER" --init-script "$INIT" \
  -Ppassvault.audit.mode=success :core:database:desktopTest --offline "${SAFETY[@]}"
PASSVAULT_PVA038_FIXTURE="$FIXTURE" "$WRAPPER" --init-script "$INIT" \
  -Ppassvault.audit.mode=wrong-key :core:database:desktopTest --offline "${SAFETY[@]}"
PASSVAULT_PVA038_FIXTURE="$FIXTURE" "$WRAPPER" --init-script "$INIT" \
  -Ppassvault.audit.mode=loader-io :core:database:desktopTest --offline "${SAFETY[@]}"
# Separately after EACH attempted call, same original per-call environment:
"$WRAPPER" --stop "${SAFETY[@]}"
```

Ordinary is ONLINE/strict verified; cold4 OFFLINE/no fallback. FIXTURE is exactly
prepare's317-character XML-produced value. Init02 changes only cycle-name/path
text; seven worker roots, startup properties, one-worker/fork/512MiB limits,
loader-only tmp-blocker, literal filters and cold Test reexecution are unchanged.
No intermediate clean/recompile. Preserve13 ordinary +4 cold XML captures before
replacement, settle/stop each call, then the unchanged same-lock34-top cleanup.
All167-method/PVD/hardware/STOP/NO-RETRY/CLOSED boundaries remain unchanged.
