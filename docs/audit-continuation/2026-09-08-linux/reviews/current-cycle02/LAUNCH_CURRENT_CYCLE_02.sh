#!/bin/bash
# Source only until root and the independent reviewer admit the exact instance.
# No old runner, recovery, namespace adoption, process-name/group kill or retry.
set -euo pipefail
umask 077
set -o noclobber

# Root must pin this parent, these previously absent names, all tool images,
# the source/instance inputs and sole slot before invoking this literal script.
C=/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/current-cycle02
exec 3>"$C/EXTERNAL-stdout-02.log"
exec 4>"$C/EXTERNAL-stderr-02.log"
read -r birth < "/proc/$$/stat"
read -r uptime _ < /proc/uptime
printf 'TIMEOUT_PRE_EXEC_PID=%s PPID=%s UPTIME=%s STAT=%s\n' "$$" "$PPID" "$uptime" "$birth" >&3

# exec preserves this original PID/start identity. The inner Bash likewise
# records its own original birth before replacing itself with the fixed Python.
# The driver installs per-call wrapper-stop/settlement/cleanup before Gradle.
# Foreground timeout is NOT a descendant killer. SIGKILL/host loss can prevent
# cleanup: retain HOLD/original evidence, never infer success or retry a stop.
exec /usr/bin/timeout --foreground --signal=TERM --kill-after=1200s 9600s \
    /bin/bash --noprofile --norc -c '
        set -euo pipefail
        read -r birth < "/proc/$$/stat"
        read -r uptime _ < /proc/uptime
        printf "PYTHON_PRE_EXEC_PID=%s PPID=%s UPTIME=%s STAT=%s\n" "$$" "$PPID" "$uptime" "$birth"
        exec /usr/bin/python3 -I -B /root/projects/PassVault/passvault-linux/scripts/audit/linux_current_cycle_02.py
    ' >&3 2>&4
