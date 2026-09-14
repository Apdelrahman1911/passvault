"""Fresh one-compiler/one-kernel-case driver. Root must seal/admit before execution."""
import ctypes, errno, fcntl, hashlib, json, os, resource, signal, stat, subprocess, sys, time

BASE = "/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk"
R = "/root/projects/PassVault/audit-runtime-linux-kernel-contract01"
E = BASE + "/root/kernel-contract01"
LOCK = "/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock"
C = BASE + "/current_ledger/gui_kernel_contract01/leader_exit_child.c"
P = BASE + "/current_ledger/gui_kernel_contract01/revision02/probe.py"
GCC = "/usr/bin/x86_64-linux-gnu-gcc-13"
PY = "/usr/bin/python3.12"
FLOOR = 3221225472
DIR = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC

def identity(s):
    return [s.st_dev, s.st_ino, s.st_uid, stat.S_IMODE(s.st_mode)]

def main():
    held = []; dirs = {}; children = {}; cancelled = 0; finished = False; started = time.monotonic()
    parent = runtime = evidence = None; created = False; error = None; cleanup_errors = []
    result = {"schema": 1, "application_cases": 0, "waits": [], "phases": [], "resources": []}
    def latch(sig, _frame):
        nonlocal cancelled
        if finished: raise SystemExit(128 + sig)
        if not cancelled: cancelled = sig
    def acquire(path, flags, at=None):
        fd = os.open(path, flags, dir_fd=at); held.append(fd); return fd
    def read(path, at=None, limit=1048576):
        fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=at)
        with os.fdopen(fd, "rb") as stream:
            raw = stream.read(limit + 1)
        assert len(raw) <= limit, "oversized fixed input/evidence"
        return raw
    def descriptor(raw):
        return {"bytes": len(raw), "sha256": hashlib.sha256(raw).hexdigest()}
    def sample(stage):
        v = os.statvfs(os.path.dirname(R)); free = v.f_bavail * v.f_frsize
        fields = [line.split() for line in read("/proc/meminfo", limit=16384).decode("ascii").splitlines() if line.startswith(("MemTotal:", "MemAvailable:"))]
        assert len(fields) == 2 and all(len(f) == 3 and f[2] == "kB" for f in fields), "memory resource fields"
        mem = {f[0]: int(f[1]) * 1024 for f in fields}; total = mem["MemTotal:"]; available = mem["MemAvailable:"]
        assert 0 < total and 0 <= available <= total, "invalid memory resource values"
        result["resources"].append({"at": stage, "free_bytes": free, "memory_total_bytes": total, "memory_available_bytes": available})
        entry = stage == "before-runtime" or stage.endswith("-entry")
        assert free >= FLOOR, "3GiB disk floor"
        assert available >= (12 if entry else 8) * 1073741824 and available * (4 if entry else 5) >= total, "host memory absolute/fraction floor"
    def drain():
        while True:
            try: pid, status = os.waitpid(-1, os.WNOHANG)
            except ChildProcessError: return True
            if not pid: return False
            assert os.WIFEXITED(status) or os.WIFSIGNALED(status), "nonterminal wait"
            code = os.waitstatus_to_exitcode(status)
            if pid in children: children[pid].returncode = code
            result["waits"].append({"pid": pid, "exit_code": code, "direct": pid in children})
    def phase(name, argv, payload, seconds):
        child = pidfd = None; streams = []; settled = False; phase_started = time.monotonic()
        row = {"name": name, "argv": argv if payload is not None else [PY, "-I", "-B", "-S", "-c", "<pinned probe bytes>", R + "/leader-exit-child"]}
        result["phases"].append(row)
        try:
            sample(name + "-entry")
            for suffix in ("stdout", "stderr"):
                streams.append(os.open(name + "." + suffix, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=evidence))
            assert not cancelled, "cancelled before " + name + " spawn"
            child = subprocess.Popen(argv, stdin=subprocess.PIPE if payload is not None else subprocess.DEVNULL,
                stdout=streams[0], stderr=streams[1], close_fds=True, start_new_session=True, cwd=R,
                env={"HOME": R + "/home", "TMPDIR": R + "/tmp", "PATH": "/usr/bin:/bin", "LANG": "C", "LC_ALL": "C", "TZ": "UTC"})
            children[child.pid] = child; row["original_pid"] = child.pid
            pidfd = os.pidfd_open(child.pid, 0)
            if payload is not None:
                os.set_blocking(child.stdin.fileno(), False)
                assert os.write(child.stdin.fileno(), payload) == len(payload), "short compiler stdin"
                child.stdin.close(); child.stdin = None
            deadline = time.monotonic() + seconds; next_sample = time.monotonic() + 5
            while not (settled := drain()):
                assert not cancelled, "external cancellation"
                assert time.monotonic() < deadline, name + " cooperative observation deadline"
                if time.monotonic() >= next_sample:
                    sample(name + "-running"); next_sample = time.monotonic() + 5
                time.sleep(0.02)
            row["exit_code"] = child.returncode
            assert child.returncode == 0, name + " nonzero original exit"
        finally:
            try:
                if child is not None and child.stdin is not None:
                    try: child.stdin.close()
                    except OSError as exc: cleanup_errors.append("compiler stdin close: " + str(exc.errno))
                    child.stdin = None
                if not settled:
                    if pidfd is not None and child.returncode is None:
                        try:
                            signal.pidfd_send_signal(pidfd, signal.SIGTERM, None, 0); row["cleanup_signal"] = "ORIGINAL_PIDFD_TERM"
                        except OSError as exc:
                            row["signal_errno"] = exc.errno
                            if exc.errno != errno.ESRCH: cleanup_errors.append("original pidfd TERM: " + str(exc.errno))
                    until = time.monotonic() + 12
                    while not (settled := drain()) and time.monotonic() < until: time.sleep(0.02)
            except Exception as exc: cleanup_errors.append(name + " settlement: " + str(exc)[:256])
            finally:
                row["all_children_settled"] = settled
                row["elapsed_seconds"] = round(time.monotonic() - phase_started, 6)
                for fd in ([pidfd] if pidfd is not None else []) + streams:
                    try: os.close(fd)
                    except OSError as exc: cleanup_errors.append(name + " descriptor close: " + str(exc.errno))
    def binary():
        s = os.stat("leader-exit-child", dir_fd=runtime, follow_symlinks=False)
        assert stat.S_ISREG(s.st_mode) and s.st_uid == os.getuid() and s.st_nlink == 1 and s.st_size <= 1048576, "unexpected binary"
        raw = read("leader-exit-child", runtime)
        return {**descriptor(raw), "identity": identity(s), "mtime_ns": s.st_mtime_ns, "ctime_ns": s.st_ctime_ns}, raw
    try:
        for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP): signal.signal(sig, latch)
        signal.signal(signal.SIGCHLD, signal.SIG_DFL)
        os.umask(0o077)
        result["rlimits"] = {}
        for name, kind, desired in (("core_bytes", resource.RLIMIT_CORE, (0, 0)), ("file_bytes", resource.RLIMIT_FSIZE, (1048576, 1048576)), ("cpu_seconds", resource.RLIMIT_CPU, (20, 25)), ("address_space_bytes", resource.RLIMIT_AS, (536870912, 536870912))):
            current = resource.getrlimit(kind); selected = tuple(w if v == resource.RLIM_INFINITY else min(v, w) for v, w in zip(current, desired))
            resource.setrlimit(kind, selected); result["rlimits"][name] = list(selected)
        assert len(sys.argv) == 3 and sys.argv[1] == E + "/ADMISSION.json", "exact admission path required"
        raw = read(sys.argv[1]); assert hashlib.sha256(raw).hexdigest() == sys.argv[2], "admission hash"
        a = json.loads(raw); result["admission_sha256"] = sys.argv[2]
        assert a["runtime"] == R and a["evidence"] == E and a["coordination"]["path"] == LOCK, "fixed paths"
        c = read(C); p = read(P)
        assert descriptor(c) == {"bytes": 2049, "sha256": "35ea9e52bb561dab693bd610cf22390933d8ac12a310e387200bf94bfa592c32"}, "C source pin"
        assert descriptor(p) == {"bytes": 12585, "sha256": "2814ef187893e20d86c850a80bab47cea25409dc96b757455c67e52ada2464d4"}, "probe source pin"
        result["source"] = {"C": descriptor(c), "probe": descriptor(p)}
        result["tools"] = {name: descriptor(read(path, limit=16777216)) for name, path in (("gcc", GCC), ("python", PY))}
        assert result["tools"] == a["tools"], "root-admitted tool byte pins"
        u = os.uname(); glibc = os.confstr("CS_GNU_LIBC_VERSION")
        assert u.sysname == "Linux" and u.machine == "x86_64" and glibc and glibc.startswith("glibc "), "x86_64 Linux/glibc required"
        result["platform"] = {"system": u.sysname, "release": u.release, "machine": u.machine, "libc": glibc, "python": sys.version.split()[0]}
        lock_parent = acquire(os.path.dirname(LOCK), DIR)
        assert identity(os.fstat(lock_parent)) == a["coordination"]["parent_identity"], "lock parent"
        lock = acquire("build.lock", os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, lock_parent)
        s = os.fstat(lock)
        assert stat.S_ISREG(s.st_mode) and s.st_size == 0 and identity(s) + [s.st_nlink] == a["coordination"]["identity"], "lock leaf"
        assert identity(os.stat(os.path.dirname(LOCK), follow_symlinks=False)) == a["coordination"]["parent_identity"], "lock parent path"
        assert os.stat("build.lock", dir_fd=lock_parent, follow_symlinks=False) == s, "lock leaf path"
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        parent = acquire(os.path.dirname(R), DIR); evidence = acquire(E, DIR)
        assert identity(os.fstat(parent)) == a["runtime_parent_identity"] and identity(os.fstat(evidence)) == a["evidence_identity"], "fresh parent/evidence identity"
        libc = ctypes.CDLL(None, use_errno=True)
        libc.prctl.argtypes = [ctypes.c_int, ctypes.c_ulong, ctypes.c_ulong, ctypes.c_ulong, ctypes.c_ulong]; libc.prctl.restype = ctypes.c_int
        assert libc.prctl(36, 1, 0, 0, 0) == 0, "PR_SET_CHILD_SUBREAPER"
        assert drain(), "fresh driver unexpectedly has children"
        sample("before-runtime"); assert not cancelled, "cancelled before allocation"
        os.mkdir(os.path.basename(R), 0o700, dir_fd=parent); created = True
        runtime = acquire(os.path.basename(R), DIR, parent); dirs[""] = (runtime, identity(os.fstat(runtime)))
        for leaf in ("home", "tmp"):
            os.mkdir(leaf, 0o700, dir_fd=runtime); fd = acquire(leaf, DIR, runtime); dirs[leaf] = (fd, identity(os.fstat(fd)))
        phase("compile", [GCC, "-pipe", "-std=c11", "-O2", "-Wall", "-Wextra", "-Werror", "-pedantic", "-pthread", "-x", "c", "-", "-o", R + "/leader-exit-child"], c, 30)
        assert not cleanup_errors, "compiler cleanup failure; no probe"
        before, elf = binary(); result["binary"] = before
        assert elf.startswith(b"\x7fELF") and before["bytes"] > 0, "native ELF required"
        assert not cancelled, "cancelled before probe"
        phase("probe", [PY, "-I", "-B", "-S", "-c", p.decode("utf-8"), R + "/leader-exit-child"], None, 20)
        probe = json.loads(read("probe.stdout", evidence, 32768)); result["probe"] = probe
        assert probe["status"] == "PASS" and probe["original_child_reaped"] and not probe["cleanup_errors"] and not probe["cancel_signal"], "probe contract/cleanup failed"
        assert binary()[0] == before, "binary changed during probe"
    except Exception as exc:
        error = type(exc).__name__ + ": " + str(exc)[:512]
    finally:
        try:
            settled = drain(); result["all_children_settled"] = settled
            assert settled, "original/adopted child settlement unknown"
            if created:
                assert runtime is not None and set(dirs) == {"", "home", "tmp"}, "partial runtime creation; HOLD"
                assert identity(os.stat(os.path.dirname(R), follow_symlinks=False)) == identity(os.fstat(parent)), "runtime parent rebound"
                for leaf, (fd, pin) in dirs.items():
                    owner_fd = runtime if leaf else parent; name = leaf or os.path.basename(R)
                    assert identity(os.fstat(fd)) == pin == identity(os.stat(name, dir_fd=owner_fd, follow_symlinks=False)), "runtime directory rebound"
                    assert pin[2:] == [os.getuid(), 0o700], "runtime directory owner/mode"
                    if leaf: assert not os.listdir(fd), "nonempty fresh HOME/TMP; HOLD"
                names = set(os.listdir(runtime)); assert names <= {"home", "tmp", "leader-exit-child"}, "unknown runtime output; HOLD"
                if "leader-exit-child" in names:
                    current, _ = binary(); result["cleanup_binary"] = current
                    assert binary()[0] == current, "binary changed before unlink"
                    os.unlink("leader-exit-child", dir_fd=runtime)
                for leaf in ("home", "tmp"): os.rmdir(leaf, dir_fd=runtime)
                os.rmdir(os.path.basename(R), dir_fd=parent); result["runtime_cleanup"] = "REMOVED_VALIDATED_BINARY_AND_ORIGINAL_EMPTY_DIRS"
            else: result["runtime_cleanup"] = "NOT_CREATED"
        except Exception as exc:
            cleanup_errors.append(str(exc)[:512]); result["runtime_cleanup"] = "HOLD_NOT_FULLY_CLEANED"
        try: sample("after-cleanup")
        except Exception as exc: cleanup_errors.append("resource-after: " + str(exc)[:256])
        if evidence is not None:
            for name in ("compile.stdout", "compile.stderr", "probe.stdout", "probe.stderr"):
                try: result.setdefault("outputs", {})[name] = descriptor(read(name, evidence, 1048576))
                except FileNotFoundError: pass
                except Exception as exc: cleanup_errors.append(name + ": " + str(exc)[:256])
        for fd in reversed(held):
            try: os.close(fd)
            except OSError as exc: cleanup_errors.append("outer descriptor close: " + str(exc.errno))
        result.update(error=error, cancel_signal=cancelled, cleanup_errors=cleanup_errors, elapsed_seconds=round(time.monotonic() - started, 6))
    finished = result.get("all_children_settled", False) and not cleanup_errors
    result["cancel_signal"] = cancelled
    result["status"] = "PASS" if not error and not cancelled and not cleanup_errors else "FAIL"
    print(json.dumps(result, sort_keys=True, separators=(",", ":")), flush=True)
    return 0 if result["status"] == "PASS" and not cancelled else 1

if __name__ == "__main__":
    raise SystemExit(main())
