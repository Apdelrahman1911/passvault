#!/usr/bin/env python3
"""Bound one normal hosted-CI shell step; never use against a developer checkout.

Windows descendants are confined before shell startup; POSIX descendants inherit
an owned session. Forced runner loss cannot be certified: hosted VM disposal is
then the fallback, not a reported cleanup success.
"""
import ctypes
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import tempfile
import time


def bash_executable(env):
    configured = env.get("PASSVAULT_CI_BASH")
    if not configured and sys.platform == "win32":
        raise RuntimeError("Windows requires the caller's explicit Git Bash path")
    executable = configured or shutil.which("bash")
    if not executable or not Path(executable).is_absolute() or not Path(executable).is_file():
        raise RuntimeError("Missing or invalid explicit Bash executable")
    return executable


class ProcessScope:
    def __init__(self):
        self.process = None
        self.job = None
        if os.name == "nt":
            from ctypes import wintypes as w
            self.api = ctypes.WinDLL("kernel32", use_last_error=True)
            self.api.CreateJobObjectW.argtypes = [ctypes.c_void_p, w.LPCWSTR]
            self.api.CreateJobObjectW.restype = w.HANDLE
            self.api.AssignProcessToJobObject.argtypes = [w.HANDLE, w.HANDLE]
            self.api.SetInformationJobObject.argtypes = [w.HANDLE, ctypes.c_int, ctypes.c_void_p, w.DWORD]
            self.api.QueryInformationJobObject.argtypes = [w.HANDLE, ctypes.c_int, ctypes.c_void_p, w.DWORD, ctypes.c_void_p]
            self.api.TerminateJobObject.argtypes = [w.HANDLE, w.UINT]
            self.api.CloseHandle.argtypes = [w.HANDLE]

            class Limits(ctypes.Structure):
                _fields_ = [("process_time", ctypes.c_int64), ("job_time", ctypes.c_int64),
                            ("flags", w.DWORD), ("min_ws", ctypes.c_size_t),
                            ("max_ws", ctypes.c_size_t), ("active_limit", w.DWORD),
                            ("affinity", ctypes.c_size_t), ("priority", w.DWORD), ("scheduling", w.DWORD)]

            class Extended(ctypes.Structure):
                _fields_ = [("basic", Limits), ("io", ctypes.c_uint64 * 6),
                            ("process_memory", ctypes.c_size_t), ("job_memory", ctypes.c_size_t),
                            ("peak_process", ctypes.c_size_t), ("peak_job", ctypes.c_size_t)]

            self.job = self.api.CreateJobObjectW(None, None)
            limits = Extended()
            limits.basic.flags = 0x2000  # JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE; no breakaway.
            if not self.job or not self.api.SetInformationJobObject(self.job, 9, ctypes.byref(limits), ctypes.sizeof(limits)):
                raise ctypes.WinError(ctypes.get_last_error())

    def launch(self, script, env):
        env = dict(env, PASSVAULT_CI_BASH=bash_executable(env))
        # The child cannot launch the shell until assigned to its Windows job.
        self.process = subprocess.Popen([sys.executable, __file__, "--child", str(script)],
                                        env=env, stdin=subprocess.PIPE,
                                        start_new_session=os.name != "nt")
        if self.job and not self.api.AssignProcessToJobObject(self.job, int(self.process._handle)):
            self.process.kill()
            self.process.wait()
            raise ctypes.WinError(ctypes.get_last_error())
        self.process.stdin.write(b"go\n")
        self.process.stdin.close()

    def empty(self):
        if not self.process:
            return True
        self.process.poll()  # reap our direct child before inspecting the scope
        if self.job:
            # JOBOBJECT_BASIC_ACCOUNTING_INFORMATION: four LARGE_INTEGERs,
            # then four DWORDs (ActiveProcesses is the third DWORD).
            info = ctypes.create_string_buffer(48)
            if not self.api.QueryInformationJobObject(self.job, 1, info, 48, None):
                raise ctypes.WinError(ctypes.get_last_error())
            return int.from_bytes(info.raw[40:44], "little") == 0
        try:
            os.killpg(self.process.pid, 0)
            return False
        except ProcessLookupError:
            return True
        except PermissionError:
            # macOS can report EPERM while a terminated group is settling.
            # Lack of permission is never proof of emptiness: keep polling
            # within the caller's deadline, then retain outputs on uncertainty.
            return False

    def terminate(self):
        if self.job:
            if not self.api.TerminateJobObject(self.job, 1):
                raise ctypes.WinError(ctypes.get_last_error())
        elif self.process:
            try:
                os.killpg(self.process.pid, signal.SIGTERM)
            except ProcessLookupError:
                pass
        end = time.monotonic() + 10
        while time.monotonic() < end and not self.empty():
            time.sleep(0.2)
        if not self.empty() and os.name != "nt":
            os.killpg(self.process.pid, signal.SIGKILL)

    def close(self):
        if self.job:
            self.api.CloseHandle(self.job)
            self.job = None


def terminate_completed_windows_scopes(scopes, result, attempted):
    # Build and original-wrapper stop have returned. Their no-breakaway jobs
    # may still contain native build workers; do not wait for worker reuse timers.
    for index, scope in enumerate(scopes):
        if scope in attempted:
            continue
        if scope.job and not scope.empty():
            if scope.process.poll() is None:
                raise RuntimeError("Refuse terminating an active Windows shell")
            attempted.add(scope)
            result.setdefault("windows_job_termination_requested", []).append(index)
            scope.terminate()
    # This dispatch is NOT settlement proof. The caller must still observe
    # empty jobs before final reports or any output/private-root deletion.


def generated_roots(repo):
    paths = subprocess.check_output(["git", "ls-files", "-z"], cwd=repo).decode().split("\0")
    roots = {repo / ".gradle", repo / ".kotlin", repo / "build"}
    roots.update(repo / Path(p).parent / "build" for p in paths if p.endswith("build.gradle.kts"))
    for root in roots:
        if any(p.is_symlink() for p in (root, *root.parents) if p != repo and repo in p.parents) or repo not in root.resolve().parents:
            raise RuntimeError("Unsafe generated-output root")
        if any(p and (repo / p == root or root in (repo / p).parents) for p in paths):
            raise RuntimeError("Refuse cleanup of tracked source")
    return sorted(roots)


def preserve_reports(roots, repo, evidence):
    size = 0
    for root in roots:
        if any(p.is_symlink() for p in (root, *root.parents) if p != repo and repo in p.parents):
            raise RuntimeError("Unexpected output-root symlink while preserving reports")
        for sub in ("test-results", "reports"):
            directory = root / sub
            if directory.is_symlink():
                raise RuntimeError("Unexpected report directory symlink")
            if not directory.exists():
                continue
            for source in sorted(directory.rglob("*")):
                if any(p.is_symlink() for p in (source, *source.parents) if p != root and root in p.parents):
                    raise RuntimeError("Unexpected symlink in reports")
                if not source.is_file() or source.suffix not in (".xml", ".html", ".css", ".js", ".txt", ".json"):
                    continue
                size += source.stat().st_size
                if size > 25 * 1024 * 1024:
                    raise RuntimeError("Reports exceed compact evidence budget; retain outputs")
                destination = evidence / source.relative_to(repo)
                destination.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source, destination)


def memory_fraction():
    if os.name == "nt":
        class Memory(ctypes.Structure):
            _fields_ = [("length", ctypes.c_uint32), ("load", ctypes.c_uint32),
                        *[(name, ctypes.c_uint64) for name in
                          ("total", "available", "page_total", "page_available",
                           "virtual_total", "virtual_available", "extended")]]
        info = Memory()
        info.length = ctypes.sizeof(info)
        if not ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(info)):
            raise ctypes.WinError()
        return info.available / info.total
    if sys.platform == "darwin":
        import re
        stats = subprocess.check_output(["vm_stat"], text=True)
        page_size = int(re.search(r"page size of (\d+) bytes", stats).group(1))
        pages = sum(int(re.search(re.escape(name) + r":\s+(\d+)", stats).group(1))
                    for name in ("Pages free", "Pages inactive", "Pages speculative"))
        total = int(subprocess.check_output(["sysctl", "-n", "hw.memsize"], text=True))
        return pages * page_size / total
    fields = dict(line.split(":", 1) for line in Path("/proc/meminfo").read_text().splitlines())
    return int(fields["MemAvailable"].split()[0]) / int(fields["MemTotal"].split()[0])


def ci_resource_limits(environment):
    """Bound the native CI profile separately; ordinary jobs keep their limits."""
    requested = environment.get("PASSVAULT_CI_IOS_RELEASE_LINK", "false")
    if requested == "false":
        return 2048, 35 * 60
    if requested != "true" or sys.platform != "darwin":
        raise RuntimeError("The iOS release-link heap requires an explicit macOS job")
    total = int(subprocess.check_output(["sysctl", "-n", "hw.memsize"], text=True, timeout=10))
    if total < 12 * 1024 ** 3:
        raise RuntimeError("The iOS release-link heap requires at least 12 GiB host RAM")
    return 4096, 50 * 60


def main(script):
    if os.environ.get("GITHUB_ACTIONS") != "true" or os.environ.get("RUNNER_ENVIRONMENT") != "github-hosted":
        raise RuntimeError("Only fresh GitHub-hosted jobs are admitted")
    repo = Path(os.environ["GITHUB_WORKSPACE"]).resolve()
    if Path.cwd().resolve() != repo:
        raise RuntimeError("Expected checkout working directory")
    if memory_fraction() < 0.25:
        raise RuntimeError("Insufficient hosted RAM headroom before startup")
    if shutil.disk_usage(repo).free < 3 * 1024 ** 3:
        raise RuntimeError("Insufficient hosted disk headroom before startup")
    heap_mib, batch_timeout_seconds = ci_resource_limits(os.environ)
    roots = generated_roots(repo)
    if any(p.exists() for p in roots):
        raise RuntimeError("Unowned pre-existing build output; refusing adoption")
    private = Path(tempfile.mkdtemp(prefix="passvault-ci-", dir=os.environ["RUNNER_TEMP"]))
    evidence = repo / ".ci-evidence" / private.name
    evidence.mkdir(parents=True)
    env = os.environ.copy()  # Preserve RUNNER_TRACKING_ID for runner fallback.
    for name in ("home", "tmp", "gradle", "konan"):
        (private / name).mkdir()
    home = str(private / "home")
    env.update(HOME=home, USERPROFILE=home, TMPDIR=str(private / "tmp"),
               TMP=str(private / "tmp"), TEMP=str(private / "tmp"),
               GRADLE_USER_HOME=str(private / "gradle"), KONAN_DATA_DIR=str(private / "konan"),
               APPDATA=str(private / "home" / "AppData" / "Roaming"),
               LOCALAPPDATA=str(private / "home" / "AppData" / "Local"),
               CMAKE_BUILD_PARALLEL_LEVEL="1")
    env["JAVA_TOOL_OPTIONS"] = f'-Duser.home="{home}" -Djava.io.tmpdir="{private / "tmp"}"'
    env["GRADLE_OPTS"] = f"-Dpassvault.ci.owner={private.name} -Dorg.gradle.daemon=false -Dorg.gradle.workers.max=1 -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false"
    (private / "gradle" / "gradle.properties").write_text(
        f"org.gradle.jvmargs=-Xmx{heap_mib}m -Dfile.encoding=UTF-8 -Dpassvault.ci.owner={private.name}\n"
        "org.gradle.workers.max=1\norg.gradle.parallel=false\norg.gradle.daemon=false\n"
        "org.gradle.configureondemand=false\norg.gradle.configuration-cache=false\n"
        "kotlin.compiler.execution.strategy=in-process\n")
    scopes = []
    windows_termination_attempted = set()
    result = {"source": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(),
              "exit": None, "cleanup": "HOLD", "wrapper_stop": "not-started", "gradle_heap_mib": heap_mib,
              "batch_timeout_seconds": batch_timeout_seconds}
    interrupted = False

    def cancel(_signum, _frame):
        nonlocal interrupted
        interrupted = True
        raise RuntimeError("CI step interrupted")

    signal.signal(signal.SIGTERM, cancel)
    signal.signal(signal.SIGINT, cancel)
    try:
        scope = ProcessScope()
        scopes.append(scope)
        scope.launch(script, env)
        deadline = time.monotonic() + batch_timeout_seconds
        while scope.process.poll() is None:
            free = shutil.disk_usage(repo).free
            result["minimum_free_bytes"] = min(result.get("minimum_free_bytes", free), free)
            available = memory_fraction()
            result["minimum_available_ram_fraction"] = min(result.get("minimum_available_ram_fraction", available), available)
            if available < 0.20:
                raise RuntimeError("Hosted RAM floor reached; stop owned work")
            if free < 3 * 1024 ** 3:
                raise RuntimeError("Hosted disk floor reached; stop owned work")
            if time.monotonic() >= deadline:
                raise RuntimeError("CI batch time budget reached")
            time.sleep(2)
        result["exit"] = scope.process.returncode
    finally:
        # Signal handlers are installed before launch. Repeated/forced termination
        # can still cut cleanup short; never write PASS before actual settlement.
        signal.signal(signal.SIGTERM, signal.SIG_IGN)
        signal.signal(signal.SIGINT, signal.SIG_IGN)
        try:
            # Snapshot even on stop failure; only the post-settlement copy is
            # definitive. Never discard test diagnostics because cleanup failed.
            result["reports"] = "pre-settlement snapshot (may be incomplete)"
            try:
                preserve_reports(roots, repo, evidence)
            except (OSError, RuntimeError) as error:
                result["report_snapshot_error"] = str(error)
            if interrupted or result["exit"] is None:
                for scope in scopes:
                    if scope.job:
                        windows_termination_attempted.add(scope)
                    scope.terminate()
            # Do not bootstrap Gradle merely to stop a non-Gradle shell step.
            if (private / "gradle" / "daemon").exists():
                stop_script = private / "stop.sh"
                stop_script.write_text("set -eu\n" + ("./gradlew.bat" if os.name == "nt" else "./gradlew") + " --stop\n")
                stop = ProcessScope()
                scopes.append(stop)
                result["wrapper_stop"] = "attempted"
                stop.launch(stop_script, env)
                try:
                    result["wrapper_stop"] = stop.process.wait(timeout=60)
                except subprocess.TimeoutExpired:
                    result["wrapper_stop"] = "timeout (no retry)"
                    raise
                if result["wrapper_stop"] != 0:
                    raise RuntimeError("Original wrapper stop failed")
            terminate_completed_windows_scopes(scopes, result, windows_termination_attempted)
            end = time.monotonic() + 15
            while time.monotonic() < end and not all(s.empty() for s in scopes):
                time.sleep(0.2)
            if not all(s.empty() for s in scopes):
                raise RuntimeError("Owned workers did not settle")
            if os.name != "nt":
                # Gradle may detach its single-use JVM from the shell session.
                process_lines = subprocess.check_output(["ps", "-axww", "-o", "pid=", "-o", "command="], text=True)
                if private.name in process_lines:
                    raise RuntimeError("Private-home process remains outside scope")
            preserve_reports(roots, repo, evidence)
            result["reports"] = "post-settlement snapshot"
            if any(private.rglob("CLEANUP_HOLD")):
                raise RuntimeError("Synthetic keychain cleanup HOLD; retain private roots")
            for root in generated_roots(repo):
                if root.exists():
                    shutil.rmtree(root)
            shutil.rmtree(private)
            result["cleanup"] = "PASS"
        except BaseException as error:
            result["cleanup_error"] = str(error)
            for scope in scopes:
                if scope in windows_termination_attempted:
                    continue  # Preserve failure/HOLD; do not retry termination.
                try:
                    if not scope.empty():
                        scope.terminate()
                except BaseException as stop_error:
                    result["termination_error"] = str(stop_error)
            raise
        finally:
            (evidence / "cleanup.json").write_text(json.dumps(result, indent=2) + "\n")
            for scope in scopes:
                scope.close()
    return result["exit"]


if __name__ == "__main__":
    if len(sys.argv) == 3 and sys.argv[1] == "--child":
        if sys.stdin.readline() != "go\n":
            sys.exit(1)
        command = [bash_executable(os.environ), "--noprofile", "--norc", "-eo", "pipefail", sys.argv[2]]
        if os.name != "nt":
            os.execvp(command[0], command)
        sys.exit(subprocess.call(command))
    sys.exit(main(Path(sys.argv[1]).resolve()))
