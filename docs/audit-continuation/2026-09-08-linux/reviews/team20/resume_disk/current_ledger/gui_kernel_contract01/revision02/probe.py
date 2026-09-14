"""One real Linux original-child lifecycle case; never imports an audit runner."""

import errno
import json
import os
import select
import signal
import subprocess
import sys
import time


def main():
    child = None
    pidfd = None
    anchor = None
    reaped = False
    cancelled = 0
    cleanup_errors = []
    result = {
        "schema": 1,
        "kind": "SYNTHETIC_ORIGINAL_CHILD_KERNEL_CONTRACT",
        "case_count": 1,
        "application_test_cases": 0,
        "pidfd_flags": 0,
        "numeric_pid_signal_fallback": False,
        "numeric_proc_reopen": False,
        "forced_pid_reuse": False,
        "assertions_completed": False,
        "markers": [],
        "pidfd_samples": [],
    }
    started = time.monotonic()
    deadline = started + 6.0

    def require(condition, message):
        if not condition:
            raise RuntimeError(message)

    def latch(signum, _frame):
        nonlocal cancelled
        if not cancelled:
            cancelled = signum

    def tick():
        require(not cancelled, "cancellation requested")
        require(time.monotonic() < deadline, "six-second observation deadline")

    def mask(timeout_ms=0):
        events = pidpoll.poll(timeout_ms)
        if not events:
            return 0
        require(len(events) == 1 and events[0][0] == pidfd, "unexpected pidfd event")
        value = events[0][1]
        require(value != 0 and not value & ~(select.POLLIN | select.POLLHUP),
                "invalid pidfd poll mask: " + str(value))
        return value

    def nonterminal(stage):
        value = mask()
        result["pidfd_samples"].append({"at": stage, "mask": value})
        require(value == 0, "original thread group unexpectedly terminal at " + stage)

    def stat():
        # This is always relative to the one original task-directory fd.
        leaf = os.open("stat", os.O_RDONLY | os.O_CLOEXEC | os.O_NOFOLLOW,
                       dir_fd=anchor)
        try:
            data = os.read(leaf, 4097)
            require(len(data) <= 4096 and data.endswith(b"\n"), "invalid stat bound")
            require(not os.read(leaf, 1), "stat exceeds one bounded record")
        finally:
            try:
                os.close(leaf)
            except OSError as exc:
                # A close failure is not a missing-task observation.
                raise RuntimeError("stat leaf close failed: " + str(exc.errno)) from exc
        head, separator, tail = data.decode("utf-8", "strict").rpartition(") ")
        fields = tail.split()
        require(separator and head.startswith(str(child.pid) + " (") and len(fields) >= 20,
                "malformed original-child stat")
        value = {"pid": child.pid, "ppid": int(fields[1]), "starttime": int(fields[19]),
                 "state": fields[0]}
        require(value["ppid"] == os.getpid() and value["starttime"] > 0,
                "original direct-child identity mismatch")
        return value

    def namespace(kind):
        value = os.readlink("ns/" + kind, dir_fd=anchor)
        prefix = kind + ":["
        require(len(value) <= 128 and value.startswith(prefix) and value.endswith("]")
                and value[len(prefix):-1].isdigit(), "malformed namespace link")
        return value

    def send(command):
        tick()
        require(os.write(child.stdin.fileno(), command) == 1, "short protocol write")

    def expect(marker):
        data = bytearray()
        while b"\n" not in data:
            tick()
            events = outpoll.poll(50)
            if not events:
                continue
            require(len(events) == 1 and events[0][0] == child.stdout.fileno(),
                    "unexpected protocol descriptor")
            require(not events[0][1] & ~(select.POLLIN | select.POLLHUP),
                    "invalid protocol poll mask")
            try:
                part = os.read(child.stdout.fileno(), 64)
            except BlockingIOError:
                continue
            require(part, "unexpected child stdout EOF")
            data.extend(part)
            require(len(data) <= 64, "oversized fixed protocol marker")
        require(data == marker, "unexpected fixed protocol marker")
        result["markers"].append(marker.decode("ascii").strip())

    def reap(until, cancellable):
        nonlocal reaped
        while time.monotonic() < until:
            if cancellable:
                tick()
            # Exact original direct child only. ECHILD is an error, not exit 0.
            got, status = os.waitpid(child.pid, os.WNOHANG)
            if got:
                require(got == child.pid and (os.WIFEXITED(status) or os.WIFSIGNALED(status)),
                        "unexpected original-child wait status")
                child.returncode = os.waitstatus_to_exitcode(status)
                reaped = True
                result["child_exit_code"] = child.returncode
                return
            time.sleep(0.01)
        raise RuntimeError("original-child reap deadline")

    def close_stream(name):
        stream = getattr(child, name)
        setattr(child, name, None)
        if stream is not None:
            try:
                stream.close()
            except OSError as exc:
                cleanup_errors.append(name + " close: " + str(exc.errno))

    try:
        # Handlers latch, not throw, including during Popen/pidfd assignment.
        # This standalone process has no other child-reaper or Python thread.
        for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
            signal.signal(sig, latch)
        signal.signal(signal.SIGCHLD, signal.SIG_DFL)  # No SIG_IGN/SA_NOCLDWAIT.
        require(sys.platform == "linux", "Linux-only fixture")
        require(hasattr(os, "pidfd_open") and hasattr(signal, "pidfd_send_signal"),
                "Python native pidfd interfaces required; no ctypes fallback")
        require(len(sys.argv) == 2 and os.path.isabs(sys.argv[1]),
                "one root-admitted absolute helper-binary path required")
        tick()
        child = subprocess.Popen(
            [sys.argv[1]], stdin=subprocess.PIPE, stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL, bufsize=0, close_fds=True,
            start_new_session=True, env={"LANG": "C", "LC_ALL": "C"},
        )
        result["original_child_pid"] = child.pid
        pidfd = os.pidfd_open(child.pid, 0)
        anchor = os.open("/proc/" + str(child.pid),
                         os.O_RDONLY | os.O_DIRECTORY | os.O_CLOEXEC | os.O_NOFOLLOW)
        pidpoll = select.poll()
        pidpoll.register(pidfd, select.POLLIN)
        nonterminal("anchor_bound_before_positive_reads")
        os.set_blocking(child.stdin.fileno(), False)
        os.set_blocking(child.stdout.fileno(), False)
        outpoll = select.poll()
        outpoll.register(child.stdout.fileno(), select.POLLIN)
        expect(b"READY\n")
        initial = {"stat": stat(), "pid_ns": namespace("pid"), "mnt_ns": namespace("mnt")}
        result["initial"] = initial
        nonterminal("initial_live_reads_complete")

        send(b"L")
        expect(b"LEADER_JOINED\n")
        nonterminal("leader_joined")
        namespace_deadline = min(deadline, time.monotonic() + 1.0)
        attempts = 0
        while True:
            tick()
            require(mask() == 0, "thread group died during leader namespace observation")
            attempts += 1
            try:
                still_present = namespace("mnt")
            except OSError as exc:
                require(exc.errno == errno.ENOENT, "unexpected live-leader namespace errno")
                result["leader_mnt_missing"] = {
                    "errno": exc.errno, "name": errno.errorcode[exc.errno], "attempts": attempts,
                }
                break
            require(still_present == initial["mnt_ns"], "anchored namespace changed")
            require(time.monotonic() < namespace_deadline, "leader mount namespace stayed present")
            time.sleep(0.01)
        nonterminal("leader_namespace_missing")
        send(b"P")
        expect(b"WORKER_ALIVE\n")
        # clear_child_tid/join and namespace teardown can precede exit_notify.
        # Wait only for that state transition; identity/read/pidfd errors stay fatal.
        state_deadline = min(deadline, time.monotonic() + 1.0)
        state_attempts = 0
        while True:
            tick()
            require(mask() == 0, "thread group died before leader Z observation")
            state_attempts += 1
            exited_leader = stat()
            require(all(exited_leader[key] == initial["stat"][key]
                        for key in ("pid", "ppid", "starttime")),
                    "leader anchor identity changed")
            require(exited_leader["state"] in
                    ("R", "S", "D", "Z", "T", "t", "W", "X", "x", "K", "P", "I"),
                    "malformed leader state")
            if exited_leader["state"] == "Z":
                break
            require(time.monotonic() < state_deadline, "leader did not reach Z state")
            time.sleep(0.01)
        result["leader_z_attempts"] = state_attempts
        result["exited_leader_stat"] = exited_leader
        nonterminal("live_worker_after_leader_exit")

        send(b"X")
        while True:
            tick()
            value = mask(50)
            if value:
                require(value & select.POLLIN, "terminal pidfd lacks readability before reap")
                result["pidfd_samples"].append({"at": "last_thread_exit", "mask": value})
                break
        reap(deadline, True)
        require(child.returncode == 0, "child did not exit normally")
        result["pidfd_samples"].append({"at": "after_original_reap", "mask": mask()})
        gone = {}
        for name, read in (("stat", stat), ("ns/pid", lambda: namespace("pid")),
                           ("ns/mnt", lambda: namespace("mnt"))):
            tick()
            try:
                read()
            except OSError as exc:
                require(exc.errno in (errno.ENOENT, errno.ESRCH),
                        "unexpected reaped-anchor errno for " + name)
                gone[name] = {"errno": exc.errno, "name": errno.errorcode[exc.errno]}
            else:
                raise RuntimeError("reaped original anchor still readable: " + name)
        result["reaped_anchor_failures"] = gone
        result["assertions_completed"] = True
    except Exception as exc:
        result["error"] = {"type": type(exc).__name__, "message": str(exc)[:512]}
        if isinstance(exc, OSError):
            result["error"]["errno"] = exc.errno
    finally:
        # No numeric-PID signals or scans. EOF plus the child's independent
        # ten-second alarm cover even failure to acquire its original pidfd.
        if child is not None:
            close_stream("stdin")
            if not reaped:
                if pidfd is not None:
                    try:
                        signal.pidfd_send_signal(pidfd, signal.SIGKILL, None, 0)
                        result["cleanup_signal"] = "SIGKILL_VIA_ORIGINAL_PIDFD"
                    except OSError as exc:
                        result["cleanup_signal_errno"] = exc.errno
                        if exc.errno != errno.ESRCH:
                            cleanup_errors.append("pidfd signal: " + str(exc.errno))
                else:
                    result["cleanup_signal"] = "NONE_EOF_AND_CHILD_ALARM_ONLY"
                try:
                    reap(time.monotonic() + 12.0, False)
                except Exception as exc:
                    cleanup_errors.append("reap: " + str(exc)[:256])
            close_stream("stdout")
        # Attempt both independent closes once, even if either fails.
        for name, descriptor in (("proc_anchor", anchor), ("pidfd", pidfd)):
            if descriptor is not None:
                try:
                    os.close(descriptor)
                except OSError as exc:
                    cleanup_errors.append(name + " close: " + str(exc.errno))
        result["original_child_reaped"] = reaped
        result["cancel_signal"] = cancelled
        result["cleanup_errors"] = cleanup_errors
        result["elapsed_seconds"] = round(time.monotonic() - started, 6)

    result["status"] = ("PASS" if result["assertions_completed"] and reaped
                        and not cancelled and not cleanup_errors else "FAIL")
    print(json.dumps(result, sort_keys=True, separators=(",", ":")), flush=True)
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
