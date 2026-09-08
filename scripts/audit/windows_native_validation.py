"""One Windows native audit request; not a general runner or recovery command.

Author: /root/native. Read WINDOWS_ADMISSION.md before admitting any invocation.
Only root may activate this helper with an independently accepted request. No
Gradle, signing, installation, credential inventory, or Hello operation is run.
"""

from __future__ import annotations

import ctypes
from ctypes import wintypes as wt
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import signal
import stat
import struct
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


BRANCH = "refs/heads/codex/audit-continuation-linux-20260908"
BASE = "docs/audit-continuation/2026-09-08-linux"
REQUEST = f"{BASE}/requests/windows-native.json"
BINDINGS = f"{BASE}/reviews/native/SOURCE_BINDINGS.json"
HELPER = "scripts/audit/windows_native_validation.py"
WORKFLOW = ".github/workflows/audit-windows-native-validation.yml"
SUITE = "windows-native-14-v1"
SDK = "10.0.26100.0"
CASES = [
    "passvault_biometric_windows_file_" + name
    for name in (
        "success", "validation_failure", "dacl_failure", "collision",
        "empty_payload", "oversized_payload", "empty_suffix", "rename_failure",
    )
] + [
    "passvault_biometric_windows_secret_" + name
    for name in (
        "normal_scope", "early_return", "allocation_exception", "nested_exception",
    )
] + ["passvault_biometric_abi", "passvault_biometric_windows_security"]
GiB = 1024 ** 3
LOG_LIMIT = 2 * 1024 ** 2
CANCELLED = False


def require(condition: bool, explanation: str) -> None:
    if not condition:
        raise RuntimeError(explanation)


def digest(path: Path) -> str:
    with path.open("rb") as value:
        return hashlib.file_digest(value, "sha256").hexdigest()


def frozen_bytes(path: Path) -> bytes:
    plain_path(path)
    before = path.lstat()
    require(stat.S_ISREG(before.st_mode) and before.st_nlink == 1, "Input must be regular and single-linked")
    require(before.st_size <= 1024 ** 2, "Oversized frozen input")
    identity = lambda state: (state.st_dev, state.st_ino, state.st_size, state.st_mtime_ns, state.st_ctime_ns)
    with path.open("rb") as stream:
        opened = os.fstat(stream.fileno())
        data = stream.read(1024 ** 2 + 1)
        finished = os.fstat(stream.fileno())
    after = path.lstat()
    require(identity(before) == identity(opened) == identity(finished) == identity(after), "Frozen input changed during read")
    require(len(data) == before.st_size, "Frozen input length changed")
    return data


def no_duplicate_keys(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, "Duplicate JSON key")
        result[key] = value
    return result


def read_json(path: Path):
    return json.loads(frozen_bytes(path), object_pairs_hook=no_duplicate_keys)


def write_json(path: Path, value) -> None:
    with path.open("x", encoding="utf-8", newline="\n") as output:
        json.dump(value, output, sort_keys=True, indent=2)
        output.write("\n")
        output.flush()
        os.fsync(output.fileno())


def cancel(_number, _frame):
    global CANCELLED
    CANCELLED = True


def plain_path(path: Path) -> None:
    """No reparse components; no assertion of adversarial same-user isolation."""
    for member in (path, *path.parents):
        state = member.lstat()
        require(not (state.st_file_attributes & 0x400), f"Reparse path: {member}")


class SECURITY_ATTRIBUTES(ctypes.Structure):
    _fields_ = [("length", wt.DWORD), ("descriptor", wt.LPVOID), ("inherit", wt.BOOL)]


class STARTUPINFO(ctypes.Structure):
    _fields_ = [
        ("cb", wt.DWORD), ("reserved", wt.LPWSTR), ("desktop", wt.LPWSTR),
        ("title", wt.LPWSTR), ("x", wt.DWORD), ("y", wt.DWORD),
        ("width", wt.DWORD), ("height", wt.DWORD), ("chars_x", wt.DWORD),
        ("chars_y", wt.DWORD), ("fill", wt.DWORD), ("flags", wt.DWORD),
        ("show", wt.WORD), ("reserved_size", wt.WORD), ("reserved_ptr", wt.LPVOID),
        ("stdin", wt.HANDLE), ("stdout", wt.HANDLE), ("stderr", wt.HANDLE),
    ]


class PROCESS_INFORMATION(ctypes.Structure):
    _fields_ = [("process", wt.HANDLE), ("thread", wt.HANDLE),
                ("pid", wt.DWORD), ("tid", wt.DWORD)]


class STARTUPINFOEX(ctypes.Structure):
    _fields_ = [("startup", STARTUPINFO), ("attributes", wt.LPVOID)]


class BASIC_LIMIT(ctypes.Structure):
    _fields_ = [
        ("process_time", ctypes.c_int64), ("job_time", ctypes.c_int64),
        ("flags", wt.DWORD), ("min_working_set", ctypes.c_size_t),
        ("max_working_set", ctypes.c_size_t), ("active_limit", wt.DWORD),
        ("affinity", ctypes.c_size_t), ("priority", wt.DWORD), ("scheduling", wt.DWORD),
    ]


class IO_COUNTERS(ctypes.Structure):
    _fields_ = [(name, ctypes.c_uint64) for name in (
        "read_ops", "write_ops", "other_ops", "read_bytes", "write_bytes", "other_bytes")]


class EXTENDED_LIMIT(ctypes.Structure):
    _fields_ = [
        ("basic", BASIC_LIMIT), ("io", IO_COUNTERS),
        ("process_memory", ctypes.c_size_t), ("job_memory", ctypes.c_size_t),
        ("peak_process", ctypes.c_size_t), ("peak_job", ctypes.c_size_t),
    ]


class ACCOUNTING(ctypes.Structure):
    _fields_ = [
        ("user_time", ctypes.c_int64), ("kernel_time", ctypes.c_int64),
        ("period_user", ctypes.c_int64), ("period_kernel", ctypes.c_int64),
        ("faults", wt.DWORD), ("total", wt.DWORD),
        ("active", wt.DWORD), ("terminated", wt.DWORD),
    ]


class MEMORY(ctypes.Structure):
    _fields_ = [("length", wt.DWORD), ("load", wt.DWORD)] + [
        (name, ctypes.c_uint64) for name in (
            "total", "available", "total_page", "available_page",
            "total_virtual", "available_virtual", "extended_virtual")]


class FILE_INFO(ctypes.Structure):
    _fields_ = [
        ("attributes", wt.DWORD), ("created", wt.FILETIME),
        ("accessed", wt.FILETIME), ("written", wt.FILETIME), ("volume", wt.DWORD),
        ("size_high", wt.DWORD), ("size_low", wt.DWORD), ("links", wt.DWORD),
        ("index_high", wt.DWORD), ("index_low", wt.DWORD),
    ]


class Windows:
    """Atomic creation-time job assignment; no unowned fork/assignment gap."""

    def __init__(self):
        self.k = ctypes.WinDLL("kernel32", use_last_error=True)
        specs = {
            "CreateJobObjectW": ([wt.LPVOID, wt.LPCWSTR], wt.HANDLE),
            "SetInformationJobObject": ([wt.HANDLE, ctypes.c_int, wt.LPVOID, wt.DWORD], wt.BOOL),
            "QueryInformationJobObject": ([wt.HANDLE, ctypes.c_int, wt.LPVOID, wt.DWORD, wt.LPVOID], wt.BOOL),
            "TerminateJobObject": ([wt.HANDLE, wt.UINT], wt.BOOL),
            "InitializeProcThreadAttributeList": ([wt.LPVOID, wt.DWORD, wt.DWORD, wt.LPVOID], wt.BOOL),
            "UpdateProcThreadAttribute": ([wt.LPVOID, wt.DWORD, ctypes.c_size_t, wt.LPVOID,
                                            ctypes.c_size_t, wt.LPVOID, wt.LPVOID], wt.BOOL),
            "DeleteProcThreadAttributeList": ([wt.LPVOID], None),
            "CreateFileW": ([wt.LPCWSTR, wt.DWORD, wt.DWORD, wt.LPVOID, wt.DWORD, wt.DWORD, wt.HANDLE], wt.HANDLE),
            "CreateProcessW": ([wt.LPCWSTR, wt.LPWSTR, wt.LPVOID, wt.LPVOID, wt.BOOL, wt.DWORD,
                                wt.LPVOID, wt.LPCWSTR, wt.LPVOID, wt.LPVOID], wt.BOOL),
            "ResumeThread": ([wt.HANDLE], wt.DWORD),
            "WaitForSingleObject": ([wt.HANDLE, wt.DWORD], wt.DWORD),
            "GetExitCodeProcess": ([wt.HANDLE, wt.LPVOID], wt.BOOL),
            "CloseHandle": ([wt.HANDLE], wt.BOOL),
            "FlushFileBuffers": ([wt.HANDLE], wt.BOOL),
            "GlobalMemoryStatusEx": ([wt.LPVOID], wt.BOOL),
            "GetFileInformationByHandle": ([wt.HANDLE, wt.LPVOID], wt.BOOL),
            "SetFileInformationByHandle": ([wt.HANDLE, ctypes.c_int, wt.LPVOID, wt.DWORD], wt.BOOL),
        }
        for name, (arguments, result) in specs.items():
            method = getattr(self.k, name)
            method.argtypes, method.restype = arguments, result
        self.job = self.k.CreateJobObjectW(None, None)
        self.termination_attempted = False
        self.check(self.job, "CreateJobObject")
        try:
            limits = EXTENDED_LIMIT()
            # Active-process limit + total committed-memory limit + kill-on-close.
            limits.basic.flags = 0x00000008 | 0x00000200 | 0x00002000
            limits.basic.active_limit = 16
            limits.job_memory = 3 * GiB
            self.check(self.k.SetInformationJobObject(self.job, 9, ctypes.byref(limits),
                                                      ctypes.sizeof(limits)), "SetJobLimits")
        except BaseException:
            self.k.CloseHandle(self.job)
            self.job = None
            raise

    @staticmethod
    def check(ok, operation):
        if not ok:
            raise OSError(ctypes.get_last_error(), operation)

    def active(self) -> int:
        state = ACCOUNTING()
        self.check(self.k.QueryInformationJobObject(self.job, 1, ctypes.byref(state),
                                                    ctypes.sizeof(state), None), "JobAccounting")
        return state.active

    def terminate_once(self):
        require(not self.termination_attempted, "Unsettled prior job termination; no automatic retry")
        self.termination_attempted = True
        self.check(self.k.TerminateJobObject(self.job, 125), "TerminateOwnedJob")

    def resources(self, paths, launch=False):
        memory = MEMORY()
        memory.length = ctypes.sizeof(memory)
        self.check(self.k.GlobalMemoryStatusEx(ctypes.byref(memory)), "MemoryStatus")
        disks = {str(path): shutil.disk_usage(path).free for path in paths}
        observation = {"time": time.time(), "available_ram": memory.available,
                       "total_ram": memory.total, "free_disk": disks}
        require(memory.total > 0 and memory.available / memory.total >= (0.25 if launch else 0.20),
                "RAM admission/running floor crossed: " + json.dumps(observation, sort_keys=True))
        require(min(disks.values()) >= (12 if launch else 8) * GiB,
                "Disk admission/running floor crossed: " + json.dumps(observation, sort_keys=True))
        return observation

    def open_owned(self, path: Path, delete=True):
        # DELETE + READ_ATTRIBUTES; denying delete sharing freezes this entry's
        # name until its retained handle itself requests disposition/close.
        handle = self.k.CreateFileW(str(path), (0x10000 if delete else 0) | 0x80, 0x1 | 0x2, None,
                                    3, 0x02000000 | 0x00200000, None)
        require(handle not in (None, ctypes.c_void_p(-1).value), f"Cannot bind {path}")
        try:
            state = FILE_INFO()
            self.check(self.k.GetFileInformationByHandle(handle, ctypes.byref(state)), "FileIdentity")
            require(not state.attributes & 0x400, "Cleanup refuses reparse point")
            require(state.links == 1, "Cleanup refuses multiple hard links")
            identity = [state.volume, state.index_high, state.index_low, state.attributes]
            return handle, identity
        except BaseException:
            self.k.CloseHandle(handle)
            raise

    def delete_handle(self, handle):
        mark = ctypes.c_ubyte(1)  # FILE_DISPOSITION_INFO.DeleteFile
        self.check(self.k.SetFileInformationByHandle(handle, 4, ctypes.byref(mark), 1),
                   "HandleBoundDelete")

    def close(self):
        if self.job:
            self.check(self.k.CloseHandle(self.job), "CloseJob")
            self.job = None


class Run:
    def __init__(self, win: Windows, workspace: Path, temp: Path, evidence: Path):
        self.win, self.workspace, self.temp, self.evidence = win, workspace, temp, evidence
        self.start = time.monotonic()
        self.sequence = 0
        self.command_results = []
        self.environment = {
            name: os.environ[name] for name in (
                "SystemRoot", "WINDIR", "COMSPEC", "PATH", "PATHEXT",
                "ProgramFiles", "ProgramFiles(x86)", "ProgramW6432",
                "PROCESSOR_ARCHITECTURE", "NUMBER_OF_PROCESSORS", "SystemDrive",
            ) if name in os.environ
        }
        self.environment.update({
            "TEMP": str(temp / "tmp"), "TMP": str(temp / "tmp"),
            "HOME": str(temp / "home"), "USERPROFILE": str(temp / "home"),
            "APPDATA": str(temp / "appdata"), "LOCALAPPDATA": str(temp / "localappdata"),
            "CMAKE_BUILD_PARALLEL_LEVEL": "1", "CTEST_PARALLEL_LEVEL": "1",
            "MSBUILDDISABLENODEREUSE": "1", "VSCMD_SKIP_SENDTELEMETRY": "1",
            "GIT_CONFIG_NOSYSTEM": "1", "GIT_CONFIG_GLOBAL": "NUL",
            "GIT_TERMINAL_PROMPT": "0", "GIT_OPTIONAL_LOCKS": "0",
        })

    def event(self, kind, **value):
        with (self.evidence / "journal.jsonl").open("a", encoding="utf-8", newline="\n") as output:
            output.write(json.dumps({"time": time.time(), "event": kind, **value}, sort_keys=True) + "\n")
            output.flush()
            os.fsync(output.fileno())

    def retain_log(self, path):
        if not path.exists():
            return
        settled = self.win.active() == 0
        size = path.stat().st_size
        with path.open("rb") as source:
            prefix = source.read(LOG_LIMIT)
            full_digest = hashlib.sha256(prefix)
            if settled:
                while block := source.read(1024 ** 2):
                    full_digest.update(block)
        # Raw logs are inside the generated cleanup root, never uploaded.
        # Only this bounded prefix is an evidence *.log file. Hash the complete
        # settled raw file before deletion; an unsettled stream has no full hash.
        target = self.evidence / path.name
        with target.open("xb") as output:
            output.write(prefix)
            output.flush()
            os.fsync(output.fileno())
        write_json(self.evidence / (path.stem + "-retention.json"), {
            "log": path.name, "observed_raw_bytes": size, "retained_bytes": len(prefix),
            "retained_sha256": hashlib.sha256(prefix).hexdigest(),
            "settled_full_sha256": full_digest.hexdigest() if settled else None,
            "truncated": size > len(prefix), "owned_job_settled": settled,
        })

    def command(self, arguments, budget, label):
        require(not CANCELLED, "Cancellation is sticky; no new command")
        require(time.monotonic() < self.start + 900, "Global budget exhausted; no new command")
        require(self.win.active() == 0, "Prior owned workers not settled; no new command")
        self.event("resources", observation=self.win.resources([self.workspace, self.temp], launch=True))
        self.sequence += 1
        log = self.temp / "logs" / f"{self.sequence:02d}-{label}.log"
        argv = [str(arg) for arg in arguments]
        self.event("command_intent", argv=argv, seconds=budget, log=log.name,
                   stop_obligation="WINDOWS_JOB_SETTLEMENT; no Gradle was launched")
        info = PROCESS_INFORMATION()
        handles = []
        process_attributes = None
        attributes_initialized = False
        result = None
        try:
            attributes = SECURITY_ATTRIBUTES(ctypes.sizeof(SECURITY_ATTRIBUTES), None, True)
            output = self.win.k.CreateFileW(str(log), 0x40000000, 1, ctypes.byref(attributes), 1, 0x80, None)
            require(output not in (None, ctypes.c_void_p(-1).value), "Exclusive log creation failed")
            handles.append(output)
            stdin = self.win.k.CreateFileW("NUL", 0x80000000, 3, ctypes.byref(attributes), 3, 0x80, None)
            require(stdin not in (None, ctypes.c_void_p(-1).value), "NUL input creation failed")
            handles.append(stdin)
            startup = STARTUPINFOEX()
            startup.startup.cb, startup.startup.flags = ctypes.sizeof(startup), 0x100
            startup.startup.stdin, startup.startup.stdout, startup.startup.stderr = stdin, output, output
            size = ctypes.c_size_t()
            self.win.k.InitializeProcThreadAttributeList(None, 2, 0, ctypes.byref(size))
            require(0 < size.value < 1024 ** 2, "Invalid process attribute allocation")
            process_attributes = ctypes.create_string_buffer(size.value)
            self.win.check(self.win.k.InitializeProcThreadAttributeList(process_attributes, 2, 0,
                                                                        ctypes.byref(size)), "InitProcessAttributes")
            attributes_initialized = True
            startup.attributes = ctypes.cast(process_attributes, wt.LPVOID)
            job_list = (wt.HANDLE * 1)(self.win.job)
            handle_list = (wt.HANDLE * 2)(stdin, output)
            # PROC_THREAD_ATTRIBUTE_JOB_LIST binds the job atomically with
            # creation, before even a suspended child could escape parent death.
            # HANDLE_LIST excludes the job, source/cleanup handles and all other
            # parent handles from inheritance.
            for attribute, values in ((0x0002000D, job_list), (0x00020002, handle_list)):
                self.win.check(self.win.k.UpdateProcThreadAttribute(
                    process_attributes, 0, attribute, values, ctypes.sizeof(values), None, None),
                    "BindProcessAttributes")
            block = ctypes.create_unicode_buffer("\0".join(
                f"{key}={value}" for key, value in sorted(self.environment.items(), key=lambda item: item[0].upper())
            ) + "\0\0")
            command_line = ctypes.create_unicode_buffer(subprocess.list2cmdline(argv))
            require(not CANCELLED, "Cancellation before suspended launch")
            require(time.monotonic() < self.start + 900, "Global budget exhausted before suspended launch")
            self.win.check(self.win.k.CreateProcessW(
                argv[0], command_line, None, None, True,
                0x00000004 | 0x00000400 | 0x08000000 | 0x00080000,
                block, str(self.workspace), ctypes.byref(startup), ctypes.byref(info)), "CreateSuspendedProcess")
            self.event("command_bound", pid=info.pid, tid=info.tid)
            require(not CANCELLED, "Cancellation before resume")
            require(time.monotonic() < self.start + 900, "Global budget exhausted before resume")
            require(self.win.k.ResumeThread(info.thread) != 0xffffffff, "ResumeThread failed")
            deadline = min(time.monotonic() + budget, self.start + 900)
            last_sample = 0.0
            while True:
                waited = self.win.k.WaitForSingleObject(info.process, 500)
                require(waited in (0, 258), "Process wait failed")
                require(not CANCELLED, "Cancellation requested")
                require(time.monotonic() < deadline, "Command/global time budget exceeded")
                require(log.stat().st_size <= LOG_LIMIT, "Compact log limit exceeded")
                if time.monotonic() - last_sample >= 5:
                    self.event("resources", observation=self.win.resources([self.workspace, self.temp]))
                    last_sample = time.monotonic()
                if waited == 0:
                    break
            code = wt.DWORD()
            self.win.check(self.win.k.GetExitCodeProcess(info.process, ctypes.byref(code)), "ProcessExit")
            # MSBuild node reuse is disabled; unknown/lingering descendants are
            # an operational failure, not assumed harmless by their names.
            settle_deadline = time.monotonic() + 10
            while self.win.active() and time.monotonic() < settle_deadline:
                time.sleep(0.1)
            require(self.win.active() == 0, "Owned descendants remain after command")
            self.win.check(self.win.k.FlushFileBuffers(output), "FlushCommandLog")
            require(log.stat().st_size <= LOG_LIMIT, "Late descendant log burst exceeds compact limit")
            with log.open("rb") as completed_log:
                captured_output = completed_log.read(LOG_LIMIT + 1)
            require(len(captured_output) <= LOG_LIMIT, "Completed log exceeds bounded read")
            result = {"argv": argv, "exit_code": code.value, "log": log.name,
                      "sha256": digest(log), "workers": 0}
            self.command_results.append(result)
            self.event("command_exit", **result)
            require(code.value == 0, f"Command failed ({code.value}); no automatic retry: {label}")
            return captured_output.decode("utf-8", errors="replace")
        except BaseException as error:
            self.event("command_failure", error=f"{type(error).__name__}: {error}")
            raise
        finally:
            close_errors = []
            try:
                if info.process and result is None:
                    self.win.terminate_once()
                    require(self.win.k.WaitForSingleObject(info.process, 10000) == 0, "Child termination unsettled")
                    self.event("command_aborted", pid=info.pid, assigned_to_job=True)
            finally:
                if attributes_initialized:
                    self.win.k.DeleteProcThreadAttributeList(process_attributes)
                for handle in [info.thread, info.process, *handles]:
                    if handle and not self.win.k.CloseHandle(handle):
                        close_errors.append(ctypes.get_last_error())
                self.retain_log(log)
                if close_errors:
                    self.event("handle_close_failed", errors=close_errors)
                    raise RuntimeError("Command handles did not all close; no automatic close retry")

    def cleanup(self, root_handle, root_identity):
        require(self.win.active() == 0, "Cleanup HOLD: owned job still has processes")
        handles = []
        close_attempted = []
        try:
            # Retain every original descendant handle before any deletion. No
            # path-based recursive delete and no following reparse/hard links.
            stack = [self.temp]
            while stack:
                parent = stack.pop()
                with os.scandir(parent) as entries:
                    for entry in entries:
                        path = Path(entry.path)
                        require(len(handles) < 5000, "Cleanup entry cap exceeded")
                        handle, identity = self.win.open_owned(path)
                        handles.append((path, handle, identity))
                        if identity[3] & 0x10:
                            stack.append(path)
            for path, handle, identity in sorted(handles, key=lambda item: len(item[0].parts), reverse=True):
                self.event("delete_intent", path=str(path.relative_to(self.temp)), identity=identity)
                self.win.delete_handle(handle)
                close_attempted.append(handle)
                self.win.check(self.win.k.CloseHandle(handle), "CloseDeletedEntry")
            self.event("delete_root_intent", identity=root_identity)
            self.win.delete_handle(root_handle)
            return len(handles)
        finally:
            for _path, handle, _identity in handles:
                if handle not in close_attempted:
                    self.win.k.CloseHandle(handle)


def executable(name):
    found = shutil.which(name)
    require(found is not None, f"Required preinstalled tool absent: {name}")
    path = Path(found).resolve(strict=True)
    plain_path(path)
    return path


def pe_identity(path):
    require(path.stat().st_size <= 64 * 1024 ** 2, "Unexpectedly large native binary")
    with path.open("rb") as binary:
        header = binary.read(64)
        require(header[:2] == b"MZ", "Missing MZ header")
        binary.seek(struct.unpack_from("<I", header, 60)[0])
        signature = binary.read(6)
    require(signature[:4] == b"PE\0\0" and struct.unpack_from("<H", signature, 4)[0] == 0x8664,
            "Native output is not AMD64 PE")
    return {"name": path.name, "sha256": digest(path), "bytes": path.stat().st_size,
            "machine": "AMD64/0x8664", "retained": "hash only; unsigned non-publishing test output"}


def validated_inputs(workspace):
    """Read/review authority before allocating anything, from the hashed bytes."""
    request_data = frozen_bytes(workspace / REQUEST)
    request = json.loads(request_data, object_pairs_hook=no_duplicate_keys)
    require(request.get("schema") == 1 and request.get("suite") == SUITE, "Wrong request schema/suite")
    require(request.get("owner") == "/root" and request.get("exclusive_build_slot") is True,
            "Root cross-local/CI build-slot attestation required")
    require(re.fullmatch(r"[0-9a-f]{32}", request.get("nonce", "")) is not None, "Invalid request nonce")
    require(request.get("case_names") == CASES, "Exact ordered 14 cases required")
    require(request.get("sdk") == SDK and request.get("max_seconds") == 900, "Changed target/resource contract")
    require(all(re.fullmatch(r"[0-9a-f]{40}", request.get(key, "")) for key in ("source_commit", "source_tree")),
            "Exact source identity required")
    captures = {}
    for key, path in (("helper_sha256", HELPER), ("workflow_sha256", WORKFLOW),
                      ("source_bindings_sha256", BINDINGS)):
        data = frozen_bytes(workspace / path)
        require(request.get(key) == hashlib.sha256(data).hexdigest(), f"Request hash mismatch: {path}")
        captures[path] = data
    review_path = request.get("independent_review_path", "")
    require(re.fullmatch(re.escape(BASE) + r"/reviews/native-independent/[A-Za-z0-9_.-]+\.json", review_path)
            is not None, "Unexpected independent-review path")
    review_data = frozen_bytes(workspace / review_path)
    require(request.get("independent_review_sha256") == hashlib.sha256(review_data).hexdigest(), "Review hash mismatch")
    review = json.loads(review_data, object_pairs_hook=no_duplicate_keys)
    require(review.get("reviewer") == "/root/native_review" and
            review.get("disposition") == "ACCEPT_WINDOWS_NATIVE_14_ADMISSION", "Missing genuine accepting review")
    for key in ("helper_sha256", "workflow_sha256", "source_bindings_sha256"):
        require(review.get(key) == request.get(key), "Acceptance does not bind the exact candidate")
    binding = json.loads(captures[BINDINGS], object_pairs_hook=no_duplicate_keys)
    require(binding["suite"] == SUITE and binding["case_names"] == CASES, "Binding case map changed")
    for member in binding["files"]:
        data = frozen_bytes(workspace / member["path"])
        require(hashlib.sha256(data).hexdigest() == member["sha256"], "Native source binding changed")
        captures[member["path"]] = data
    captures[REQUEST], captures[review_path] = request_data, review_data
    return request, binding, {path: hashlib.sha256(data).hexdigest() for path, data in captures.items()}


def main():
    require(sys.platform == "win32" and struct.calcsize("P") == 8, "Windows x64 Python required")
    require(sys.version_info >= (3, 11), "Preinstalled Python >=3.11 required; no automatic install")
    require(os.environ.get("GITHUB_REPOSITORY") == "Apdelrahman1911/passvault", "Wrong repository")
    require(os.environ.get("GITHUB_REF") == BRANCH, "Wrong branch")
    require(os.environ.get("GITHUB_EVENT_NAME") == "push", "Only a reviewed request push is allowed")
    require(os.environ.get("GITHUB_RUN_ATTEMPT") == "1", "No workflow rerun or automatic retry")
    require(os.environ.get("RUNNER_ENVIRONMENT") == "github-hosted", "Only the isolated hosted runner is admitted")
    require(os.environ.get("RUNNER_OS") == "Windows" and os.environ.get("RUNNER_ARCH") == "X64",
            "Wrong runner architecture")
    run_id = os.environ.get("GITHUB_RUN_ID", "")
    require(re.fullmatch(r"[0-9]{1,20}", run_id) is not None, "Invalid run identity")
    workspace = Path(os.environ["GITHUB_WORKSPACE"]).resolve(strict=True)
    runner_temp = Path(os.environ["RUNNER_TEMP"]).resolve(strict=True)
    plain_path(workspace)
    plain_path(runner_temp)
    require(Path(__file__).resolve() == workspace / HELPER, "Unexpected helper location")
    signal.signal(signal.SIGINT, cancel)
    signal.signal(signal.SIGTERM, cancel)
    request, binding, captures = validated_inputs(workspace)
    require(not CANCELLED, "Cancellation before allocation")
    temp = runner_temp / f"passvault-native-{run_id}-1"
    evidence = runner_temp / f"passvault-native-evidence-{run_id}-1"
    require(not temp.exists() and not evidence.exists(), "Namespace already exists; no adoption/recovery")
    evidence.mkdir()
    win = None
    run = None
    root_handle = None
    root_identity = None
    parent_handles = []
    result = {"suite": SUITE, "passed_cases_machine_scored": 0, "operational_status": "FAILED_OR_INCOMPLETE",
              "cleanup": "NOT_STARTED", "gradle_stop": "NOT_APPLICABLE: no Gradle invocation",
              "hello_hardware": "BLOCKED: not exercised", "failures": []}
    try:
        win = Windows()
        first_resources = win.resources([workspace, runner_temp], launch=True)
        for path in (workspace, runner_temp):
            handle, identity = win.open_owned(path, delete=False)
            parent_handles.append((path, handle, identity))
        _, _, rechecked = validated_inputs(workspace)
        require(rechecked == captures, "Authority/input drift across parent binding")
        write_json(evidence / "allocation-intent.json", {"workspace": str(workspace), "temp": str(temp),
                                                       "resources": first_resources, "run_id": run_id,
                                                       "inputs": captures,
                                                       "parent_identities": [{"path": str(path), "identity": identity}
                                                                             for path, _, identity in parent_handles]})
        require(not CANCELLED, "Cancellation before generated-root allocation")
        temp.mkdir()
        root_handle, root_identity = win.open_owned(temp)
        for name in ("build", "tmp", "home", "appdata", "localappdata", "logs"):
            (temp / name).mkdir()
        run = Run(win, workspace, temp, evidence)
        run.event("ownership_bound", root_identity=root_identity)
        git = executable("git.exe")
        git_prefix = [git, "-c", "core.hooksPath=NUL", "-c", "core.fsmonitor=false", "-c", "gc.auto=0"]
        identity = run.command(git_prefix + ["rev-parse", "HEAD", "HEAD^{tree}", "HEAD^", "HEAD^^{tree}"],
                               30, "git-identity").splitlines()
        require(len(identity) == 4 and identity[0] == os.environ.get("GITHUB_SHA"), "Request HEAD mismatch")
        require(identity[2] == request["source_commit"] and identity[3] == request["source_tree"],
                "Request must directly follow the exact reviewed source commit/tree")
        changed = run.command(git_prefix + ["diff", "--name-only", "HEAD^", "HEAD", "--"],
                              30, "request-only-diff").splitlines()
        require(changed == [REQUEST], "Activation commit may change only the request")
        status = run.command(git_prefix + ["status", "--porcelain=v1", "--untracked-files=all"],
                             30, "clean-source")
        require(not status.strip(), "Source checkout is not clean")
        result.update({"source_commit": identity[2], "source_tree": identity[3],
                       "request_commit": identity[0], "request_tree": identity[1]})
        write_json(evidence / "request.json", request)
        write_json(evidence / "source-bindings.json", binding)
        cmake, ctest = executable("cmake.exe"), executable("ctest.exe")
        require(cmake.parent == ctest.parent, "CMake/CTest must use the same installed toolchain")
        sdk_header = Path(os.environ["ProgramFiles(x86)"]) / "Windows Kits/10/Include" / SDK / "um/webauthn.h"
        plain_path(sdk_header)
        header = sdk_header.read_text(encoding="utf-8", errors="strict")
        for required in ("WEBAUTHN_API_VERSION_8", "WEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS_VERSION_8",
                         "WEBAUTHN_HMAC_SECRET_SALT", "pPRFGlobalEval"):
            require(required in header, "Installed Windows SDK lacks required reviewed WebAuthn declarations")
        write_json(evidence / "toolchain.json", {
            "runner": "windows-2022", "architecture": "x64", "sdk": SDK,
            "image_version": os.environ.get("ImageVersion"), "python": sys.version,
            "tools": [{"path": str(path), "sha256": digest(path)} for path in (Path(sys.executable), git, cmake, ctest)],
            "webauthn_header_sha256": digest(sdk_header),
            "child_environment_names": sorted(run.environment),
        })
        run.command([cmake, "--version"], 30, "cmake-version")
        build = temp / "build"
        native = workspace / "app-desktop/native/biometric-bridge"
        run.command([cmake, "-S", native, "-B", build, "-G", "Visual Studio 17 2022", "-A", "x64",
                     f"-DCMAKE_SYSTEM_VERSION={SDK}", "-DBUILD_TESTING=ON"], 120, "configure")
        # Generated configuration is compact evidence, not a compiler success claim.
        cache = (build / "CMakeCache.txt").read_text(encoding="utf-8")
        selected_cache = [line for line in cache.splitlines() if re.match(
            r"CMAKE_(CXX_FLAGS|CXX_COMPILER|C_COMPILER|GENERATOR|VS_WINDOWS_TARGET_PLATFORM_VERSION|SYSTEM_VERSION)", line)]
        projects = {}
        for name in ("passvault_biometric", "passvault_biometric_windows_security_test"):
            project = build / f"{name}.vcxproj"
            text = project.read_text(encoding="utf-8-sig")
            for token in ("stdcpp20", "<ExceptionHandling>Sync</ExceptionHandling>",
                          "<WarningLevel>Level4</WarningLevel>", "<TreatWarningAsError>true</TreatWarningAsError>",
                          "<SDLCheck>true</SDLCheck>", "<ControlFlowGuard>Guard</ControlFlowGuard>"):
                require(token in text, f"Required compiler configuration absent: {name}: {token}")
            projects[name] = {"sha256": digest(project), "required_compile_predicates": "PRESENT"}
        write_json(evidence / "compile-configuration.json", {"cache": selected_cache, "projects": projects})
        run.command([cmake, "--build", build, "--config", "Release", "--target",
                     "passvault_biometric_windows_security_test", "passvault_biometric_abi_test",
                     "--parallel", "1", "--verbose", "--", "/nodeReuse:false"], 420, "build")
        binaries = [build / "Release" / name for name in (
            "passvault_biometric.dll", "passvault_biometric_windows_security_test.exe", "passvault_biometric_abi_test.exe")]
        write_json(evidence / "native-artifacts.json", {"binaries": [pe_identity(path) for path in binaries]})
        inventory = json.loads(run.command([ctest, "--test-dir", build, "-C", "Release", "--show-only=json-v1"],
                                          30, "ctest-inventory"))
        require(sorted(test["name"] for test in inventory["tests"]) == sorted(CASES), "CTest inventory differs from 14")
        write_json(evidence / "ctest-inventory.json", inventory)
        for index, name in enumerate(CASES, 1):
            xml = evidence / f"case-{index:02d}.xml"
            run.command([ctest, "--test-dir", build, "-C", "Release", "--parallel", "1",
                         "--timeout", "30", "--no-tests=error", "--output-on-failure", "--output-junit", xml,
                         "-R", "^" + re.escape(name) + "$"], 45, f"case-{index:02d}")
            require(xml.is_file() and 0 < xml.stat().st_size <= 1024 ** 2, "Missing or oversized CTest XML")
            tree = ET.parse(xml)
            tests = list(tree.iter("testcase"))
            require(len(tests) == 1 and tests[0].get("name") == name, "CTest XML does not contain the exact single case")
            require(not any(list(tree.iter(tag)) for tag in ("failure", "error", "skipped")), "Case did not pass")
            result["passed_cases_machine_scored"] += 1
        for path, expected in captures.items():
            require(hashlib.sha256(frozen_bytes(workspace / path)).hexdigest() == expected,
                    "Source/authority drifted during execution")
        require(not CANCELLED, "Cancellation before result/cleanup")
        result["operational_status"] = "TESTS_EXECUTED_AWAITING_INDEPENDENT_RESULT_REVIEW"
    except BaseException as error:
        result["failures"].append(f"{type(error).__name__}: {error}")
    finally:
        if win is not None:
            try:
                if win.active():
                    win.terminate_once()
                    deadline = time.monotonic() + 10
                    while win.active() and time.monotonic() < deadline:
                        time.sleep(0.1)
                require(win.active() == 0, "Owned worker settlement remains incomplete")
                result["owned_workers"] = 0
                if run is not None and root_handle is not None:
                    count = run.cleanup(root_handle, root_identity)
                    to_close = root_handle
                    root_handle = None
                    win.check(win.k.CloseHandle(to_close), "CloseDeletedRoot")
                    require(not temp.exists(), "Generated root removal not settled")
                    run.event("cleanup_settled", removed_entries=count, root_removed=True)
                    result["cleanup"] = "SETTLED_ALLOWLISTED_GENERATED_ROOT_REMOVED"
                elif temp.exists():
                    result["cleanup"] = "HOLD_PARTIAL_ALLOCATION; no adoption or automatic retry"
            except BaseException as error:
                result["failures"].append(f"Cleanup {type(error).__name__}: {error}")
                result["cleanup"] = "HOLD; hosted runner disposal is not an observed cleanup pass"
            finally:
                if root_handle is not None:
                    win.k.CloseHandle(root_handle)
                for _path, handle, _identity in parent_handles:
                    if not win.k.CloseHandle(handle):
                        result["failures"].append("Original parent handle close failed; no automatic retry")
                try:
                    win.close()
                except BaseException as error:
                    result["failures"].append(f"Job close {type(error).__name__}: {error}")
        if CANCELLED:
            result["failures"].append("Cancellation observed before terminal evidence commit")
        result["commands"] = run.command_results if run is not None else []
        result["source_qualification"] = "Current 14 cases only; no historical red control, fixed KDF vector, production-cut injection, or Hello hardware proof"
        if result["failures"] or result["cleanup"] != "SETTLED_ALLOWLISTED_GENERATED_ROOT_REMOVED":
            result["operational_status"] = "FAILED_OR_INCOMPLETE"
        write_json(evidence / "result.json", result)
        print(json.dumps(result, sort_keys=True))
    # Signals/hard termination after this terminal boundary can prevent artifact
    # upload; Actions cancellation is never represented as a verification pass.
    return 0 if not CANCELLED and result["operational_status"] != "FAILED_OR_INCOMPLETE" else 1


if __name__ == "__main__":
    raise SystemExit(main())
