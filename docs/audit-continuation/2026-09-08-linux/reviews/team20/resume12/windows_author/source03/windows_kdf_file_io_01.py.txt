"""One fresh Windows KDF plus three file-I/O-status controls; no automatic retry.

Author: /root/c20_windows_author. Inert source until root separately admits
source03 scope, exact source, request and genuine independent instance review.
The accepted KDF source02 helper is the inert lifetime-code precedent; it and
consumed Windows05 are never imported/executed or given new recovery authority.
One normal target, four fixed selectors; no old cases, Gradle, signing,
installation, credential inventory or Hello call. I/O controls use real owned
files/handles plus reported-status injection, not naturally occurring OS faults.
One Job encloses all commands; a command return is not whole-cohort settlement.
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


BRANCH = "refs/heads/codex/audit-continuation-linux-20260908"
BASE = "docs/audit-continuation/2026-09-08-linux"
REQUEST = f"{BASE}/requests/windows-kdf-file-io-01.json"
HELPER = "scripts/audit/windows_kdf_file_io_01.py"
WORKFLOW = ".github/workflows/audit-windows-kdf-file-io-validation.yml"
SCOPE = f"{BASE}/reviews/team20/resume12/windows_author/source03/SOURCE-SCOPE.md"
REVIEW = f"{BASE}/reviews/team20/resume12/windows_review/IO-BATCH-INSTANCE-ACCEPT.json"
SUITE = "windows-kdf-file-io-01"
SDK = "10.0.26100.0"
TARGET = "passvault_biometric_windows_security_test"
KDF_CASE = "passvault_biometric_windows_kdf_known_answer"
CASE_ARGV = {
    KDF_CASE: ["--kdf-known-answer"],
    "passvault_biometric_windows_file_io_write_failure": ["--file-io-case", "write_failure"],
    "passvault_biometric_windows_file_io_flush_failure": ["--file-io-case", "flush_failure"],
    "passvault_biometric_windows_file_io_close_failure": ["--file-io-case", "close_failure"],
}
CASES = list(CASE_ARGV)
XML_NAMES = {
    KDF_CASE: "kdf.xml",
    "passvault_biometric_windows_file_io_write_failure": "file-io-write.xml",
    "passvault_biometric_windows_file_io_flush_failure": "file-io-flush.xml",
    "passvault_biometric_windows_file_io_close_failure": "file-io-close.xml",
}
PROJECTS = (TARGET,)
INSTRUMENTATION = (
    "PASSVAULT_BIOMETRIC_PVA036_HISTORICAL_TEST",
    "PASSVAULT_BIOMETRIC_PVA036_SOURCE",
    "PASSVAULT_BIOMETRIC_PRK_ALLOCATION_TEST",
    "HISTORICAL-PVA036-WINDOWS.cpp.txt",
)
# These are root attestations, not helper probes or proof of global exclusivity.
# cancel-in-progress:false does not protect older pending workflow runs.
ROOT_ATTESTATIONS = (
    "exclusive_build_slot",
    "source_push_excludes_queued_pending_running_local_ci",
    "activation_push_excludes_queued_pending_running_local_ci",
    "source_push_other_workflow_triggers_reviewed",
    "activation_push_other_workflow_triggers_reviewed",
    "generated_cleanup_admitted",
)
NATIVE = "app-desktop/native/biometric-bridge/"
INPUTS = {
    ".gitattributes": "8884ed2a100ce791326a3a8d8d4c12a5e0f68d96ecb382fd3a6612ad827604c5",
    NATIVE + "CMakeLists.txt": "89fa2928be4da01c17f09cc0813fbf1f284f76ecc6d05a20176389cff9903b10",
    NATIVE + "include/passvault_biometric.h": "dc76bea46e1abc0a2950f55b549fe68cf1399d256756e85ad0a79ef52b228b85",
    NATIVE + "src/windows/passvault_biometric_windows.cpp":
        "93746b0cdb399aa17dd8c3c915e907244d4e86e5b566c51714ccc7f80310bd5d",
    NATIVE + "src/windows/passvault_biometric_windows.rc":
        "13ef2e1a88a6a8d6c7f7e4ce8b679cdf75f32a8ff35e6883875c3bdc6295a39c",
    NATIVE + "tests/passvault_biometric_abi_test.cpp":
        "354d7e1904d46a2155774052ce20c476763c97f9ed293b94c4bce54208fa3785",
    NATIVE + "tests/windows/passvault_biometric_windows_security_test.cpp":
        "7042b2e340640d189ced072c75c17f7a10d0fb287d43d77b3c722279a071bb60",
    BASE + "/reviews/team20/desktop_other_review/c20/NATIVE-KDF-EXACT-PATCH-REVIEW.json":
        "6de5ea70be36aa4d9416f0de0135276a7a757975c8d1c3bddcdbdf9cf9e62321",
    BASE + "/reviews/team20/android_fixture_review/c20/KDF-ORACLE-LITERAL-REVIEW.md":
        "7adafc2fc5985025f5388673d495f10e6f1cf1d48e1d68d17a1d6d9c0d3ce2f2",
    BASE + "/reviews/team20/root/C20-NATIVE-TEST-INTEGRATION.json":
        "227fae154a727b51e1f03b00a17fe48bf8d380c8d3237b86b944a2e17082dac5",
    BASE + "/reviews/team20/resume12/windows_file_review/IO01-SOURCE-REVIEW.json":
        "a13876a95a1c13e51a7ead1328263bc2467a61e4ef51a0c34729db2f10ec08e6",
}
GiB = 1024 ** 3
LOG_LIMIT = 2 * 1024 ** 2
LOG_TOTAL_LIMIT = 8 * 1024 ** 2
XML_LIMIT = 512 * 1024
COMMAND_SECONDS, CLEANUP_SECONDS = 480, 540
CANCELLED = False


def require(condition: bool, explanation: str) -> None:
    if not condition:
        raise RuntimeError(explanation)


def require_absent(path: Path) -> None:
    try:
        path.lstat()  # Never follow a reparse target merely to test absence.
    except FileNotFoundError:
        return
    raise RuntimeError(f"Namespace exists; no adoption/recovery: {path.name}")


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
    encoded = json.dumps(value, sort_keys=True, indent=2) + "\n"
    require(len(encoded.encode("utf-8")) <= 1024 ** 2, "Compact JSON exceeds one MiB")
    with path.open("x", encoding="utf-8", newline="\n") as output:
        output.write(encoded)
        output.flush()
        os.fsync(output.fileno())


def cancel(_number, _frame):
    global CANCELLED
    CANCELLED = True


def plain_path(path: Path) -> None:
    """No reparse components; no assertion of adversarial same-user isolation."""
    for member in reversed((path, *path.parents)):
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


class EntryRefusal(RuntimeError):
    def __init__(self, details):
        super().__init__("No-follow entry ownership/reparse/link refusal")
        self.details = details


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
            "ReadFile": ([wt.HANDLE, wt.LPVOID, wt.DWORD, wt.LPVOID, wt.LPVOID], wt.BOOL),
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
        except BaseException as error:
            handle = self.job
            self.job = None
            if not self.k.CloseHandle(handle):
                raise OSError(ctypes.get_last_error(), "Job construction close failed; no retry") from error
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
            identity = [state.volume, state.index_high, state.index_low, state.attributes]
            if state.attributes & 0x400 or state.links != 1:
                raise EntryRefusal({"identity": identity, "links": state.links,
                                    "reparse": bool(state.attributes & 0x400), "target_read": False})
            return handle, identity
        except BaseException as error:
            closed = bool(self.k.CloseHandle(handle))
            if isinstance(error, EntryRefusal):
                error.details["original_handle_closed"] = closed
                if not closed:
                    error.details["close_error"] = ctypes.get_last_error()
            elif not closed:
                raise OSError(ctypes.get_last_error(), "Rejected original handle close failed; no retry") from error
            raise

    def delete_handle(self, handle):
        mark = ctypes.c_ubyte(1)  # FILE_DISPOSITION_INFO.DeleteFile
        self.check(self.k.SetFileInformationByHandle(handle, 4, ctypes.byref(mark), 1),
                   "HandleBoundDelete")

    def close(self):
        if self.job:
            handle = self.job
            self.job = None
            self.check(self.k.CloseHandle(handle), "CloseJob; no retry")


class Run:
    def __init__(self, win: Windows, workspace: Path, temp: Path, evidence: Path):
        self.win, self.workspace, self.temp, self.evidence = win, workspace, temp, evidence
        self.start = time.monotonic()
        self.sequence = 0
        self.command_results = []
        self.logs, self.xml_paths, self.rejections = [], {}, []
        self.case_results = {name: {"state": "UNSTARTED"} for name in CASES}
        self.compiler_cohort = False
        self.job_settled = False
        self.close_failures = []
        self.cleanup_cancellation_seen = False
        self.sdk_header_observation = None
        self.generated_file_observations = []
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
        entry = json.dumps({"time": time.time(), "event": kind, **value}, sort_keys=True) + "\n"
        path = self.evidence / "journal.jsonl"
        length = len(entry.encode("utf-8"))
        require(length <= 16384 and (path.stat().st_size if path.exists() else 0) + length <= LOG_LIMIT,
                "Compact journal bound exceeded")
        with path.open("a", encoding="utf-8", newline="\n") as output:
            output.write(entry)
            output.flush()
            os.fsync(output.fileno())

    def file_state(self, handle):
        state = FILE_INFO()
        self.win.check(self.win.k.GetFileInformationByHandle(handle, ctypes.byref(state)), "OutputIdentity")
        return {"identity": [state.volume, state.index_high, state.index_low, state.attributes],
                "links": state.links, "size": (state.size_high << 32) | state.size_low,
                "written": [state.written.dwHighDateTime, state.written.dwLowDateTime]}

    def read_sdk_header(self, path):
        """SDK-only stable read; never equate Path.lstat and os.fstat tuples."""
        record = {"path": str(path), "stage": "path_guard", "before": None, "after": None,
                  "read_validated": False, "read_bytes": None, "read_bytes_sha256": None,
                  "original_handle_close": "NOT_OPENED", "sharing": "READ only; deny WRITE/DELETE"}
        self.sdk_header_observation = record
        handle = None

        def snapshot():
            state = FILE_INFO()
            self.win.check(self.win.k.GetFileInformationByHandle(handle, ctypes.byref(state)), "SDKFileIdentity")
            return {"identity": [state.volume, state.index_high, state.index_low, state.attributes],
                    "links": state.links, "size": (state.size_high << 32) | state.size_low,
                    "created": [state.created.dwHighDateTime, state.created.dwLowDateTime],
                    "written": [state.written.dwHighDateTime, state.written.dwLowDateTime]}

        try:
            require(not CANCELLED and time.monotonic() < self.start + COMMAND_SECONDS,
                    "SDK read cancelled or command/global deadline exhausted")
            plain_path(path.parent)
            record["stage"] = "open"
            handle = self.win.k.CreateFileW(str(path), 0x80000000 | 0x80, 0x1, None, 3, 0x00200000, None)
            if handle in (None, ctypes.c_void_p(-1).value):
                error_code, handle = ctypes.get_last_error(), None
                raise OSError(error_code, "OpenSDKHeader; no fallback or retry")
            record["original_handle_close"] = "PENDING"
            record["stage"] = "pre_read_state"
            before = record["before"] = snapshot()
            self.event("sdk_header_observation", **record)
            require(not before["identity"][3] & (0x400 | 0x10) and before["links"] == 1,
                    "SDK header is not a non-reparse regular single-linked file")
            require(before["size"] <= 1024 ** 2, "SDK header exceeds one MiB")
            buffer = ctypes.create_string_buffer(before["size"] + 1)
            read = wt.DWORD()
            record["stage"] = "read"
            ok = self.win.k.ReadFile(handle, buffer, len(buffer), ctypes.byref(read), None)
            error_code = ctypes.get_last_error() if not ok else None
            record.update({"read_returned": bool(ok), "read_bytes": read.value, "read_error": error_code})
            require(read.value <= len(buffer), "SDK ReadFile returned an invalid byte count")
            data = buffer.raw[:read.value]
            record["read_bytes_sha256"] = hashlib.sha256(data).hexdigest()
            record["stage"] = "post_read_state"
            after = record["after"] = snapshot()
            self.event("sdk_header_observation", **record)
            if not ok:
                raise OSError(error_code, "ReadSDKHeader; no fallback or retry")
            require(before == after and len(data) == before["size"], "SDK original-handle state/length changed")
            require(not CANCELLED and time.monotonic() < self.start + COMMAND_SECONDS,
                    "SDK read cancelled or command/global deadline exhausted")
            record.update({"stage": "read_validated", "read_validated": True})
            return data
        except BaseException as error:
            record["failure"] = f"{type(error).__name__}: {error}"
            try:
                self.event("sdk_header_failure", **record)
            except BaseException as journal_error:
                record["failure_journal_error"] = str(journal_error)
            raise
        finally:
            if handle is not None:
                to_close, handle = handle, None
                record["original_handle_close"] = "ATTEMPTED_ONCE"
                try:
                    self.close_original(to_close, "SDK header")
                    record["original_handle_close"] = "SUCCEEDED"
                except BaseException as error:
                    record["original_handle_close"] = "FAILED; no retry"
                    record["close_failure"] = f"{type(error).__name__}: {error}"
                    raise

    def read_generated(self, path, settled=False):
        """CMake text only: read and close one original no-write/delete-share handle."""
        allowed = {"CMakeCache.txt", *(name + ".vcxproj" for name in PROJECTS)}
        require(path.parent == self.temp / "build" and path.name in allowed,
                "Generated reader accepts only this run's selected CMake text")
        record = {"path": str(path), "settled": settled, "stage": "path_guard", "before": None,
                  "after": None, "read_validated": False, "read_bytes": None,
                  "read_bytes_sha256": None, "original_handle_close": "NOT_OPENED",
                  "sharing": "READ only; deny WRITE/DELETE"}
        self.generated_file_observations.append(record)
        handle = None

        def deadline():
            if settled:
                self.cleanup_time()
                require(self.job_settled, "Final generated read requires observed Job zero")
            else:
                require(not CANCELLED and time.monotonic() < self.start + COMMAND_SECONDS,
                        "Generated read cancelled or command/global deadline exhausted")

        def snapshot():
            state = FILE_INFO()
            self.win.check(self.win.k.GetFileInformationByHandle(handle, ctypes.byref(state)),
                           "GeneratedFileIdentity")
            return {"identity": [state.volume, state.index_high, state.index_low, state.attributes],
                    "links": state.links, "size": (state.size_high << 32) | state.size_low,
                    "created": [state.created.dwHighDateTime, state.created.dwLowDateTime],
                    "written": [state.written.dwHighDateTime, state.written.dwLowDateTime]}

        try:
            deadline()
            plain_path(path.parent)
            record["stage"] = "open"
            handle = self.win.k.CreateFileW(str(path), 0x80000000 | 0x80, 0x1, None, 3, 0x00200000, None)
            if handle in (None, ctypes.c_void_p(-1).value):
                error_code, handle = ctypes.get_last_error(), None
                raise OSError(error_code, "OpenGeneratedText; no fallback or retry")
            record["original_handle_close"] = "PENDING"
            record["stage"] = "pre_read_state"
            before = record["before"] = snapshot()
            self.event("generated_file_observation", **record)
            require(not before["identity"][3] & (0x400 | 0x10) and before["links"] == 1,
                    "Generated text is not a non-reparse regular single-linked file")
            require(before["size"] <= 1024 ** 2, "Generated text exceeds one MiB")
            buffer = ctypes.create_string_buffer(before["size"] + 1)
            read = wt.DWORD()
            record["stage"] = "read"
            ok = self.win.k.ReadFile(handle, buffer, len(buffer), ctypes.byref(read), None)
            error_code = ctypes.get_last_error() if not ok else None
            record.update({"read_returned": bool(ok), "read_bytes": read.value, "read_error": error_code})
            require(read.value <= len(buffer), "Generated ReadFile returned an invalid byte count")
            data = buffer.raw[:read.value]
            record["read_bytes_sha256"] = hashlib.sha256(data).hexdigest()
            record["stage"] = "post_read_state"
            after = record["after"] = snapshot()
            self.event("generated_file_observation", **record)
            if not ok:
                raise OSError(error_code, "ReadGeneratedText; no fallback or retry")
            require(before == after and len(data) == before["size"],
                    "Generated original-handle state/length changed")
            deadline()
            record.update({"stage": "read_validated", "read_validated": True})
            return data
        except BaseException as error:
            record["failure"] = f"{type(error).__name__}: {error}"
            try:
                self.event("generated_file_failure", **record)
            except BaseException as journal_error:
                record["failure_journal_error"] = str(journal_error)
            raise
        finally:
            if handle is not None:
                to_close, handle = handle, None
                record["original_handle_close"] = "ATTEMPTED_ONCE"
                try:
                    self.close_original(to_close, "generated CMake text " + path.name)
                    record["original_handle_close"] = "SUCCEEDED"
                except BaseException as error:
                    record["original_handle_close"] = "FAILED; no retry"
                    record["close_failure"] = f"{type(error).__name__}: {error}"
                    raise

    def log_state(self, entry):
        state = self.file_state(entry["handle"])
        require(not state["identity"][3] & (0x400 | 0x10) and state["links"] == 1,
                "Log is not a plain single-linked file")
        if entry["identity"] is None:
            entry["identity"] = state["identity"]
        require(state["identity"][:3] == entry["identity"][:3], "Original log identity changed")
        return state

    def check_outputs(self):
        # Every original stdout handle remains live: old compiler descendants
        # can still write earlier logs while the next phase's parent runs.
        require(len(self.logs) <= 12, "Exact four-case Windows command log count exceeded")
        total = 0
        for entry in self.logs:
            size = self.log_state(entry)["size"]
            require(size <= LOG_LIMIT, f"Compact per-log limit exceeded: {entry['path'].name}")
            total += size
        require(total <= LOG_TOTAL_LIMIT, "Aggregate command log limit exceeded")
        for path in self.xml_paths.values():
            try:
                state = path.lstat()
            except FileNotFoundError:
                continue
            plain_path(path)
            require(stat.S_ISREG(state.st_mode) and state.st_nlink == 1 and state.st_size <= XML_LIMIT,
                    "CTest XML is not a bounded plain single-linked file")

    def close_original(self, handle, label):
        if not self.win.k.CloseHandle(handle):
            self.close_failures.append({"handle_scope": label, "error": ctypes.get_last_error(),
                                        "retry": "FORBIDDEN"})
            raise RuntimeError(f"Original {label} handle close failed; no retry")

    def close_logs(self):
        for entry in self.logs:
            handle, entry["handle"] = entry["handle"], None
            if handle is not None:
                try:
                    self.close_original(handle, entry["path"].name)
                except RuntimeError:
                    pass  # All failures remain in close_failures; close others once.

    def retain_outputs(self):
        errors, remaining = [], LOG_TOTAL_LIMIT
        for entry in self.logs:
            path = entry["path"]
            try:
                self.cleanup_time()
                self.win.check(self.win.k.FlushFileBuffers(entry["handle"]), "FlushRetainedLog")
                before = self.log_state(entry)
                plain_path(path)
                with path.open("rb") as source:
                    prefix = source.read(min(LOG_LIMIT, remaining))
                after = self.log_state(entry)
                remaining -= len(prefix)
                complete = self.job_settled and before == after and len(prefix) == after["size"]
                with (self.evidence / path.name).open("xb") as output:
                    output.write(prefix)
                    output.flush()
                    os.fsync(output.fileno())
                write_json(self.evidence / (path.stem + "-retention.json"), {
                    "log": path.name, "original_identity": entry["identity"],
                    "observed_raw_bytes_before": before["size"], "observed_raw_bytes_after": after["size"],
                    "retained_bytes": len(prefix), "retained_sha256": hashlib.sha256(prefix).hexdigest(),
                    "settled_full_sha256": hashlib.sha256(prefix).hexdigest() if complete else None,
                    "complete": complete, "owned_job_settled": self.job_settled,
                    "qualification": "Settled exact bytes" if complete else "UNADJUDICATED bounded prefix only",
                })
                require(complete, "Log evidence incomplete/unsettled; filesystem HOLD")
            except BaseException as error:
                errors.append(f"{path.name}: {type(error).__name__}: {error}")
        for name, path in self.xml_paths.items():
            handle = None
            try:
                self.cleanup_time()
                require(self.job_settled, "Raw XML retained only after observed Job zero")
                try:
                    path.lstat()
                except FileNotFoundError:
                    self.case_results[name]["raw_xml"] = "MISSING"
                    raise RuntimeError("Missing raw XML; no case outcome inferred")
                handle, _identity = self.open_output(path)
                before = self.file_state(handle)
                require(not before["identity"][3] & 0x10 and 0 < before["size"] <= XML_LIMIT,
                        "Missing, empty or oversized raw XML")
                plain_path(path)
                with path.open("rb") as source:
                    raw = source.read(XML_LIMIT + 1)
                require(before == self.file_state(handle) and len(raw) == before["size"], "Raw XML changed")
                with (self.evidence / path.name).open("xb") as output:
                    output.write(raw)
                    output.flush()
                    os.fsync(output.fileno())
                self.case_results[name].update({"raw_xml": path.name, "xml_bytes": len(raw),
                                                "xml_sha256": hashlib.sha256(raw).hexdigest(),
                                                "xml_scoring": "NOT_PERFORMED; independent actual-case review required"})
            except BaseException as error:
                errors.append(f"{path.name}: {type(error).__name__}: {error}")
            finally:
                if handle is not None:
                    try:
                        self.close_original(handle, path.name)
                    except BaseException as error:
                        errors.append(str(error))
        return errors

    def command(self, arguments, budget, label, case=None):
        require(not CANCELLED, "Cancellation is sticky; no new command")
        require(time.monotonic() < self.start + COMMAND_SECONDS, "Global budget exhausted; no new command")
        if not self.compiler_cohort:
            require(self.win.active() == 0, "Read-only preflight Job not empty; no new command")
        self.check_outputs()
        self.event("resources", observation=self.win.resources([self.workspace, self.temp], launch=True))
        self.sequence += 1
        log = self.temp / "logs" / f"{self.sequence:02d}-{label}.log"
        argv = [str(arg) for arg in arguments]
        record = {"argv": argv, "log": log.name, "state": "INTENT", "exit_code": None}
        self.command_results.append(record)
        if case is not None:
            self.case_results[case]["state"] = "LAUNCH_ATTEMPT; TEST_START_UNKNOWN"
        self.event("command_intent", argv=argv, seconds=budget, log=log.name,
                   stop_obligation="WINDOWS_JOB_SETTLEMENT; no Gradle was launched")
        info = PROCESS_INFORMATION()
        handles = []
        process_attributes = None
        attributes_initialized = False
        try:
            attributes = SECURITY_ATTRIBUTES(ctypes.sizeof(SECURITY_ATTRIBUTES), None, True)
            output = self.win.k.CreateFileW(str(log), 0x40000000 | 0x80, 1,
                                           ctypes.byref(attributes), 1, 0x80, None)
            require(output not in (None, ctypes.c_void_p(-1).value), "Exclusive log creation failed")
            # Retain immediately, including if later launch preparation fails.
            entry = {"path": log, "handle": output, "identity": None}
            self.logs.append(entry)
            self.log_state(entry)
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
            # HANDLE_LIST excludes the Job, source/cleanup handles AND earlier
            # logs. Only this command's NUL/stdout handles are inherited.
            for attribute, values in ((0x0002000D, job_list), (0x00020002, handle_list)):
                self.win.check(self.win.k.UpdateProcThreadAttribute(
                    process_attributes, 0, attribute, values, ctypes.sizeof(values), None, None),
                    "BindProcessAttributes")
            block = ctypes.create_unicode_buffer("\0".join(
                f"{key}={value}" for key, value in sorted(self.environment.items(), key=lambda item: item[0].upper())
            ) + "\0\0")
            command_line = ctypes.create_unicode_buffer(subprocess.list2cmdline(argv))
            require(not CANCELLED, "Cancellation before suspended launch")
            require(time.monotonic() < self.start + COMMAND_SECONDS, "Global budget exhausted before suspended launch")
            self.win.check(self.win.k.CreateProcessW(
                argv[0], command_line, None, None, True,
                0x00000004 | 0x00000400 | 0x08000000 | 0x00080000,
                block, str(self.workspace), ctypes.byref(startup), ctypes.byref(info)), "CreateSuspendedProcess")
            record.update({"state": "PROCESS_CREATED_SUSPENDED", "pid": info.pid, "tid": info.tid})
            if label == "configure":
                self.compiler_cohort = True
            if case is not None:
                self.case_results[case]["state"] = "CTEST_PROCESS_CREATED; TEST_START_UNKNOWN"
            self.event("command_bound", pid=info.pid, tid=info.tid, compiler_cohort=self.compiler_cohort)
            require(not CANCELLED, "Cancellation before resume")
            require(time.monotonic() < self.start + COMMAND_SECONDS, "Global budget exhausted before resume")
            require(self.win.k.ResumeThread(info.thread) != 0xffffffff, "ResumeThread failed")
            record["state"] = "PROCESS_RESUMED"
            deadline = min(time.monotonic() + budget, self.start + COMMAND_SECONDS)
            last_sample = 0.0
            while True:
                waited = self.win.k.WaitForSingleObject(info.process, 500)
                require(waited in (0, 258), "Process wait failed")
                if waited == 0:
                    code = wt.DWORD()
                    self.win.check(self.win.k.GetExitCodeProcess(info.process, ctypes.byref(code)), "ProcessExit")
                    # Persist the actual immediate-parent exit before any Job
                    # query, resource/log check, parsing or final cohort drain.
                    record.update({"state": "PARENT_EXIT_OBSERVED", "exit_code": code.value})
                    if case is not None:
                        self.case_results[case].update({"state": "CTEST_EXIT_OBSERVED; XML_PENDING",
                                                        "ctest_exit_code": code.value})
                    self.event("parent_exit", **record)
                    break
                require(not CANCELLED, "Cancellation requested")
                require(time.monotonic() < deadline, "Command/global time budget exceeded")
                self.check_outputs()
                if time.monotonic() - last_sample >= 5:
                    self.event("resources", observation=self.win.resources([self.workspace, self.temp]))
                    last_sample = time.monotonic()
            require(not CANCELLED, "Cancellation after observed parent exit")
            require(time.monotonic() < deadline, "Command/global budget expired at parent exit")
            self.check_outputs()
            record["job_active_after_parent"] = self.win.active()
            self.event("cohort_after_parent", log=log.name, active=record["job_active_after_parent"],
                       qualification="Point accounting only; not final Job settlement")
            if not self.compiler_cohort:
                active = record["job_active_after_parent"]
                while active:
                    require(not CANCELLED, "Cancellation during read-only Job drain")
                    require(time.monotonic() < deadline, "Read-only Job drain exceeded existing command/global deadline")
                    self.check_outputs()
                    if time.monotonic() - last_sample >= 5:
                        self.event("resources", observation=self.win.resources([self.workspace, self.temp]))
                        last_sample = time.monotonic()
                    time.sleep(min(0.1, max(0.0, deadline - time.monotonic())))
                    active = self.win.active()
                require(not CANCELLED, "Cancellation after read-only Job drain")
                require(time.monotonic() < deadline, "Read-only Job zero observed after existing deadline")
                self.check_outputs()
                record["readonly_job_zero_observed"] = True
                self.event("readonly_job_natural_zero", log=log.name, initial_active=record["job_active_after_parent"],
                           qualification="Point accounting only; no member identity or benignity inference")
            self.win.check(self.win.k.FlushFileBuffers(output), "FlushCommandLog")
            with log.open("rb") as completed_log:
                captured_output = completed_log.read(LOG_LIMIT + 1)
            require(len(captured_output) <= LOG_LIMIT, "Completed log exceeds bounded read")
            record["parent_output_snapshot_sha256"] = hashlib.sha256(captured_output).hexdigest()
            record["snapshot_qualification"] = "Not a settled-log hash; owned descendants may still write"
            require(record["exit_code"] == 0, f"Command failed ({record['exit_code']}); no automatic retry: {label}")
            return captured_output.decode("utf-8", errors="replace")
        except BaseException as error:
            record["failure"] = f"{type(error).__name__}: {error}"
            try:
                self.event("command_failure", log=log.name, error=record["failure"])
            except BaseException as journal_error:
                record["failure_journal_error"] = str(journal_error)
            raise
        finally:
            close_errors = []
            if attributes_initialized:
                self.win.k.DeleteProcThreadAttributeList(process_attributes)
            # A still-running/suspended process remains owned by the Job after
            # its original process/thread handles close. Finalization alone
            # drains/terminates that one cohort; never per-command termination.
            for handle in [info.thread, info.process, *handles]:
                if handle:
                    try:
                        self.close_original(handle, f"command-{self.sequence}")
                    except BaseException as error:
                        close_errors.append(str(error))
            if close_errors:
                record["close_errors"] = close_errors
                raise RuntimeError("Command original-handle close failure; filesystem HOLD")

    def settle(self, normal):
        errors = []
        try:
            if normal and not CANCELLED:
                deadline = min(time.monotonic() + 10, self.start + CLEANUP_SECONDS)
                last_sample = 0.0
                while True:
                    self.check_outputs()
                    if self.win.active() == 0:
                        self.job_settled = True
                        break
                    require(not CANCELLED, "Cancellation during final natural cohort drain")
                    if time.monotonic() >= deadline:
                        break
                    if time.monotonic() - last_sample >= 5:
                        self.event("resources", observation=self.win.resources([self.workspace, self.temp]))
                        last_sample = time.monotonic()
                    time.sleep(0.1)
        except BaseException as error:
            errors.append(f"Natural drain {type(error).__name__}: {error}")
        finally:
            # A failed journal/resource/output check can never veto the one
            # original-Job stop. Query failure also cannot suppress containment.
            if not self.job_settled:
                try:
                    self.job_settled = self.win.active() == 0
                except BaseException as error:
                    errors.append(f"Job query {type(error).__name__}: {error}")
                if not self.job_settled:
                    try:
                        self.event("final_stop_intent", boundary="original non-breakaway Job", attempt=1)
                    except BaseException as error:
                        errors.append(f"Stop-intent journal {type(error).__name__}: {error}")
                    try:
                        self.win.terminate_once()
                    except BaseException as error:
                        errors.append(f"Job termination {type(error).__name__}: {error}")
                    try:
                        deadline = min(time.monotonic() + 10, self.start + CLEANUP_SECONDS)
                        monitor_error_seen = False
                        while self.win.active() != 0 and time.monotonic() < deadline:
                            try:
                                self.check_outputs()
                            except BaseException as error:
                                if not monitor_error_seen:
                                    errors.append(f"Post-stop output monitor {type(error).__name__}: {error}")
                                    monitor_error_seen = True
                            time.sleep(0.1)
                        self.job_settled = self.win.active() == 0
                    except BaseException as error:
                        errors.append(f"Post-stop observation {type(error).__name__}: {error}")
            if not self.job_settled:
                errors.append("Owned Job zero unobserved; kill-on-close is not settlement proof")
            try:
                self.event("final_job_observation", observed_zero=self.job_settled,
                           termination_attempted=self.win.termination_attempted)
            except BaseException as error:
                errors.append(f"Settlement journal {type(error).__name__}: {error}")
        return {"observed_zero": self.job_settled, "termination_attempted": self.win.termination_attempted,
                "errors": errors}

    def cleanup_time(self):
        require(time.monotonic() < self.start + CLEANUP_SECONDS, "540-second cleanup cutoff; filesystem HOLD")
        if CANCELLED and not self.cleanup_cancellation_seen:
            self.cleanup_cancellation_seen = True
            self.event("cancellation_cleanup_only", qualification="No new command; bounded evidence/owned cleanup only")

    def open_output(self, path, delete=False):
        plain_path(path.parent)
        try:
            return self.win.open_owned(path, delete=delete)
        except EntryRefusal as error:
            relative = str(path.relative_to(self.temp))
            detail = {"owned_relative": relative if len(relative) <= 1024 else None,
                      "owned_relative_characters": len(relative),
                      "owned_relative_sha256": hashlib.sha256(relative.encode("utf-8")).hexdigest(),
                      "disposition": "HOLD; no target read/follow/delete", **error.details}
            self.rejections.append(detail)  # Keep before any fallible journaling.
            if not detail["original_handle_closed"]:
                self.close_failures.append({"handle_scope": "rejected entry", "error": detail.get("close_error"),
                                            "retry": "FORBIDDEN"})
            self.event("entry_refused", **detail)
            raise

    def settled_file(self, path, reader):
        self.cleanup_time()
        require(self.job_settled, "Final generated-file read requires observed Job zero")
        handle, _identity = self.open_output(path)
        try:
            before = self.file_state(handle)
            require(not before["identity"][3] & 0x10, "Expected a generated regular file")
            plain_path(path)
            value = reader(path)
            require(before == self.file_state(handle), "Generated file changed during settled read")
            return value
        finally:
            self.close_original(handle, path.name)

    def cleanup(self, root_handle, root_identity):
        require(self.job_settled and self.win.active() == 0, "Cleanup HOLD: owned Job zero not observed")
        require(not self.close_failures, "Cleanup HOLD: original handle close failure")
        self.cleanup_time()
        require(self.file_state(root_handle)["identity"] == root_identity, "Original generated-root identity changed")
        handles = []
        close_attempted = set()
        try:
            # Retain every original descendant handle before any deletion. No
            # path-based recursive delete and no following reparse/hard links.
            stack = [self.temp]
            while stack:
                self.cleanup_time()
                parent = stack.pop()
                with os.scandir(parent) as entries:
                    for entry in entries:
                        self.cleanup_time()
                        path = Path(entry.path)
                        require(len(handles) < 5000, "Cleanup entry cap exceeded")
                        handle, identity = self.open_output(path, delete=True)
                        handles.append((path, handle, identity))
                        if identity[3] & 0x10:
                            stack.append(path)
            for path, handle, identity in sorted(handles, key=lambda item: len(item[0].parts), reverse=True):
                self.cleanup_time()
                self.event("delete_intent", path=str(path.relative_to(self.temp)), identity=identity)
                self.win.delete_handle(handle)
                close_attempted.add(handle)
                self.close_original(handle, "deleted entry")
            self.cleanup_time()
            self.event("delete_root_intent", identity=root_identity)
            self.win.delete_handle(root_handle)
            return len(handles)
        finally:
            for _path, handle, _identity in handles:
                if handle not in close_attempted:
                    close_attempted.add(handle)
                    try:
                        self.close_original(handle, "gathered entry")
                    except RuntimeError:
                        pass
            require(not self.close_failures, "Original cleanup-handle close failure; filesystem HOLD")


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
    require(request.get("owner") == "/root" and all(request.get(key) is True for key in ROOT_ATTESTATIONS),
            "Fresh root queued/pending/running local-CI, both-push trigger and owned-cleanup attestations required")
    require(re.fullmatch(r"[0-9a-f]{32}", request.get("nonce", "")) is not None, "Invalid request nonce")
    require(request.get("case_names") == CASES and request.get("target") == TARGET and
            request.get("case_argv") == CASE_ARGV and request.get("audit_controls") is False,
            "Only the four fixed normal-target KDF/file-I/O selectors with controls OFF are admitted")
    require(request.get("sdk") == SDK and request.get("max_seconds") == COMMAND_SECONDS,
            "Changed target/resource contract")
    require(all(re.fullmatch(r"[0-9a-f]{40}", request.get(key, "")) for key in ("source_commit", "source_tree")),
            "Exact source identity required")
    captures = {}
    for key, path in (("helper_sha256", HELPER), ("workflow_sha256", WORKFLOW), ("scope_sha256", SCOPE)):
        data = frozen_bytes(workspace / path)
        require(request.get(key) == hashlib.sha256(data).hexdigest(), f"Request hash mismatch: {path}")
        captures[path] = data
    review_path = request.get("independent_review_path", "")
    require(review_path == REVIEW, "Wrong new Windows KDF/file-I/O independent-review path")
    review_data = frozen_bytes(workspace / review_path)
    require(request.get("independent_review_sha256") == hashlib.sha256(review_data).hexdigest(), "Review hash mismatch")
    review = json.loads(review_data, object_pairs_hook=no_duplicate_keys)
    require(review.get("schema") == 1 and review.get("reviewer") == "/root/c20_windows_review" and
            review.get("disposition") == "ACCEPT_WINDOWS_KDF_FILE_IO_01_INSTANCE",
            "Missing genuine independent KDF/file-I/O instance acceptance")
    require(review.get("suite") == SUITE and review.get("nonce") == request["nonce"] and
            review.get("case_names") == CASES and review.get("target") == TARGET and
            review.get("case_argv") == CASE_ARGV and review.get("audit_controls") is False and
            review.get("input_sha256") == INPUTS,
            "Independent acceptance does not bind this four-case nonce/source contract")
    for key in ("helper_sha256", "workflow_sha256", "scope_sha256"):
        require(review.get(key) == request.get(key), "Acceptance does not bind the exact candidate")
    # Six native configure inputs, attributes, three retained KDF receipts and
    # the independently accepted IO01 patch review. No historical code is read.
    binding = {"suite": SUITE, "case_names": CASES, "target": TARGET, "case_argv": CASE_ARGV,
               "audit_controls": False,
               "files": [{"path": path, "sha256": expected} for path, expected in INPUTS.items()]}
    for member in binding["files"]:
        data = frozen_bytes(workspace / member["path"])
        require(hashlib.sha256(data).hexdigest() == member["sha256"], "Native source binding changed")
        captures[member["path"]] = data
    captures[REQUEST], captures[review_path] = request_data, review_data
    return request, binding, {path: hashlib.sha256(data).hexdigest() for path, data in captures.items()}


def main():
    require(len(sys.argv) == 1, "Fixed helper only; no command-line overrides")
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
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGBREAK):
        signal.signal(number, cancel)
    request, binding, captures = validated_inputs(workspace)
    require(not CANCELLED, "Cancellation before allocation")
    temp = runner_temp / f"passvault-windows-kdf-file-io-01-{run_id}-1-{request['nonce']}"
    evidence = runner_temp / f"passvault-windows-kdf-file-io-01-{run_id}-1-evidence"
    require_absent(temp)
    require_absent(evidence)
    evidence.mkdir()
    win = None
    run = None
    root_handle = None
    root_identity = None
    parent_handles = []
    configured = False
    build_returned = False
    commands_returned = False
    result = {"suite": SUITE, "case_scoring": "NOT_PERFORMED; raw XML requires independent actual-case review",
              "operational_status": "FAILED_OR_INCOMPLETE",
              "cleanup": "NOT_STARTED", "gradle_stop": "NOT_APPLICABLE: no Gradle invocation",
              "hello_hardware": "BLOCKED: not exercised", "failures": []}
    try:
        # Enable upload only after this run's exclusive evidence mkdir. A
        # refused/preexisting namespace never becomes an upload source.
        with Path(os.environ["GITHUB_OUTPUT"]).open("a", encoding="utf-8", newline="\n") as output:
            output.write("evidence_owned=true\n")
            output.flush()
            os.fsync(output.fileno())
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
        parents = run.command(git_prefix + ["rev-list", "--parents", "-n", "1", "HEAD"],
                              30, "one-parent").split()
        require(parents == [identity[0], identity[2]], "Activation must have exactly one reviewed parent")
        changed = run.command(git_prefix + ["diff", "--name-status", "HEAD^", "HEAD", "--"],
                              30, "request-only-diff").splitlines()
        require(changed == ["A\t" + REQUEST], "Activation must only add the fresh request; modification/reuse forbidden")
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
        header_bytes = run.read_sdk_header(sdk_header)
        header = header_bytes.decode("utf-8", errors="strict")
        for required in ("WEBAUTHN_API_VERSION_8", "WEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS_VERSION_8",
                         "WEBAUTHN_HMAC_SECRET_SALT", "pPRFGlobalEval"):
            require(required in header, "Installed Windows SDK lacks required reviewed WebAuthn declarations")
        write_json(evidence / "toolchain.json", {
            "runner": "windows-2022", "architecture": "x64", "sdk": SDK,
            "image_version": os.environ.get("ImageVersion"), "python": sys.version,
            "tools": [{"path": str(path), "sha256": digest(path)} for path in (Path(sys.executable), git, cmake, ctest)],
            "webauthn_header_sha256": hashlib.sha256(header_bytes).hexdigest(),
            "child_environment_names": sorted(run.environment),
        })
        run.command([cmake, "--version"], 30, "cmake-version")
        build = temp / "build"
        native = workspace / "app-desktop/native/biometric-bridge"
        run.command([cmake, "-S", native, "-B", build, "-G", "Visual Studio 17 2022", "-A", "x64",
                     f"-DCMAKE_SYSTEM_VERSION={SDK}", "-DBUILD_TESTING=ON",
                     "-DPASSVAULT_AUDIT_PVA036_037_CONTROLS=OFF"], 120, "configure")
        configured = True
        # Bounded pre-build snapshots, not settled generated-file identities or
        # evidence of the effective product compilation. Final hashes follow
        # whole-cohort settlement; verbose compiler output remains necessary.
        cache = run.read_generated(build / "CMakeCache.txt").decode("utf-8")
        require("BUILD_TESTING:BOOL=ON" in cache.splitlines() and
                "PASSVAULT_AUDIT_PVA036_037_CONTROLS:BOOL=OFF" in cache.splitlines(),
                "Generated cache must retain testing ON and historical/PRK controls OFF")
        selected_cache = [line for line in cache.splitlines() if re.match(
            r"(BUILD_TESTING|PASSVAULT_AUDIT_PVA036_037_CONTROLS|"
            r"CMAKE_(CXX_FLAGS|CXX_COMPILER|C_COMPILER|GENERATOR|VS_WINDOWS_TARGET_PLATFORM_VERSION|SYSTEM_VERSION))", line)]
        projects = {}
        for name in PROJECTS:
            project = build / f"{name}.vcxproj"
            project_bytes = run.read_generated(project)
            text = project_bytes.decode("utf-8-sig")
            require(not any(token in text for token in INSTRUMENTATION),
                    "Normal KDF/file-I/O target must not compile historical/PRK instrumentation or source")
            for token in ("stdcpp20", "<ExceptionHandling>Sync</ExceptionHandling>",
                          "<WarningLevel>Level4</WarningLevel>", "<TreatWarningAsError>true</TreatWarningAsError>",
                          "<SDLCheck>true</SDLCheck>", "<ControlFlowGuard>Guard</ControlFlowGuard>"):
                require(token in text, f"Required compiler configuration absent: {name}: {token}")
            projects[name] = {"snapshot_sha256": hashlib.sha256(project_bytes).hexdigest(),
                              "required_compile_predicates": "PRESENT",
                              "historical_prk_instrumentation": "ABSENT_IN_SNAPSHOT; effective compile review still required"}
        write_json(evidence / "compile-configuration-snapshot.json", {
            "qualification": "Pre-build bounded snapshots; not settled-file or effective compiler proof",
            "cache": selected_cache, "projects": projects,
        })
        run.command([cmake, "--build", build, "--config", "Release", "--target", TARGET,
                     "--parallel", "1", "--verbose", "--", "/nodeReuse:false"], 240, "build")
        build_returned = True
        exact_filter = "^(" + "|".join(re.escape(name) for name in CASES) + ")$"
        inventory = json.loads(run.command(
            [ctest, "--test-dir", build, "-C", "Release", "--show-only=json-v1", "-R", exact_filter],
            30, "ctest-inventory"), object_pairs_hook=no_duplicate_keys)
        tests = inventory.get("tests")
        require(isinstance(tests, list) and len(tests) == len(CASES),
                "Filtered CTest inventory must contain exactly the four admitted cases")
        by_name = {}
        normal_executable = build / "Release" / (TARGET + ".exe")
        for entry in tests:
            require(isinstance(entry, dict), "Invalid CTest inventory entry")
            name = entry.get("name")
            require(isinstance(name, str) and name in CASE_ARGV and name not in by_name,
                    "Missing, extra or duplicate KDF/file-I/O inventory name")
            selected_argv = entry.get("command")
            require(isinstance(selected_argv, list) and len(selected_argv) == 1 + len(CASE_ARGV[name]) and
                    isinstance(selected_argv[0], str) and Path(selected_argv[0]).is_absolute() and
                    Path(selected_argv[0]) == normal_executable and selected_argv[1:] == CASE_ARGV[name],
                    "Inventory must select this normal Release executable and the exact per-case argv")
            by_name[name] = entry
        require(set(by_name) == set(CASES), "Incomplete exact four-case inventory")
        plain_path(normal_executable)
        write_json(evidence / "ctest-inventory.json", inventory)
        # Fixed four-case list only; each parent/XML remains independently named.
        # A failure aborts the remainder, with later cases still UNSTARTED.
        for index, case in enumerate(CASES):
            xml = temp / "logs" / XML_NAMES[case]
            run.xml_paths[case] = xml
            run.command([ctest, "--test-dir", build, "-C", "Release", "--parallel", "1",
                         "--timeout", "30", "--no-tests=error", "--output-on-failure", "--output-junit", xml,
                         "-R", "^" + re.escape(case) + "$"], 45, f"case-{index + 1}", case=case)
        require(not CANCELLED, "Cancellation before result/cleanup")
        commands_returned = True
    except BaseException as error:
        result["failures"].append(f"{type(error).__name__}: {error}")
    finally:
        if win is not None:
            try:
                if run is None:
                    # No command can have launched before Run exists.
                    result["owned_job_zero_observed"] = win.active() == 0
                    result["cleanup"] = "HOLD_NO_BOUND_RUN; no adoption or automatic retry"
                else:
                    settlement = run.settle(normal=commands_returned and not result["failures"])
                    result["job_settlement"] = settlement
                    result["failures"].extend(settlement["errors"])
                    evidence_ready = run.job_settled
                    if run.job_settled:
                        try:
                            run.cleanup_time()
                            final_inputs = {}
                            for path, expected in captures.items():
                                run.cleanup_time()
                                actual = hashlib.sha256(frozen_bytes(workspace / path)).hexdigest()
                                final_inputs[path] = actual
                                require(actual == expected, f"Source/authority drifted: {path}")
                            write_json(evidence / "final-source-inputs.json", final_inputs)
                            if configured:
                                cache_bytes = run.read_generated(temp / "build/CMakeCache.txt", settled=True)
                                cache_lines = cache_bytes.decode("utf-8").splitlines()
                                require("BUILD_TESTING:BOOL=ON" in cache_lines and
                                        "PASSVAULT_AUDIT_PVA036_037_CONTROLS:BOOL=OFF" in cache_lines,
                                        "Settled cache changed testing/control selection")
                                configuration_hashes = {"CMakeCache.txt": hashlib.sha256(cache_bytes).hexdigest()}
                                for name in PROJECTS:
                                    path = temp / "build" / f"{name}.vcxproj"
                                    project_bytes = run.read_generated(path, settled=True)
                                    require(not any(token in project_bytes.decode("utf-8-sig") for token in INSTRUMENTATION),
                                            "Settled normal target contains historical/PRK instrumentation or source")
                                    configuration_hashes[path.name] = hashlib.sha256(project_bytes).hexdigest()
                                write_json(evidence / "settled-configuration-hashes.json", configuration_hashes)
                            if build_returned:
                                binary = temp / "build/Release" / (TARGET + ".exe")
                                write_json(evidence / "native-artifacts.json", {
                                    "owned_job_settled": True,
                                    "binaries": [run.settled_file(binary, pe_identity)],
                                })
                        except BaseException as error:
                            evidence_ready = False
                            result["failures"].append(f"Settled evidence {type(error).__name__}: {error}")
                    try:
                        retention_errors = run.retain_outputs()
                        result["failures"].extend(retention_errors)
                        evidence_ready = evidence_ready and not retention_errors
                    except BaseException as error:
                        evidence_ready = False
                        result["failures"].append(f"Retention {type(error).__name__}: {error}")
                    finally:
                        # Original stdout handles close only after final raw
                        # evidence capture, including on retention failure.
                        run.close_logs()
                    if evidence_ready and not run.close_failures and root_handle is not None:
                        count = run.cleanup(root_handle, root_identity)
                        to_close, root_handle = root_handle, None
                        run.close_original(to_close, "deleted generated root")
                        require_absent(temp)
                        run.event("cleanup_settled", removed_entries=count, root_removed=True)
                        result["cleanup"] = "SETTLED_ALLOWLISTED_GENERATED_ROOT_REMOVED"
                    else:
                        result["cleanup"] = "HOLD_UNSETTLED_OR_EVIDENCE_OR_HANDLE_FAILURE; no generated-root deletion"
            except BaseException as error:
                result["failures"].append(f"Cleanup {type(error).__name__}: {error}")
                result["cleanup"] = "HOLD; hosted runner disposal is not an observed cleanup pass"
            finally:
                if run is not None:
                    # Slots already cleared by an earlier close are skipped;
                    # this covers exceptional finalization before retention.
                    run.close_logs()
                if root_handle is not None:
                    to_close, root_handle = root_handle, None
                    if not win.k.CloseHandle(to_close):
                        result["failures"].append("Original generated-root handle close failed; no automatic retry")
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
        result["cases"] = run.case_results if run is not None else {name: {"state": "UNSTARTED"} for name in CASES}
        result["entry_refusals"] = run.rejections if run is not None else []
        result["original_handle_close_failures"] = run.close_failures if run is not None else []
        result["sdk_header_observation"] = run.sdk_header_observation if run is not None else None
        result["generated_file_observations"] = run.generated_file_observations if run is not None else []
        result["source_qualification"] = (
            "Four fixed normal-image selectors: KDF has two source-qualified fixed-vector assertions; "
            "three I/O controls combine real owned files/handles with deliberate reported-status failure. "
            "The close control closes the real handle successfully before reporting false. No actual OS "
            "write/flush/close fault, process crash, application cancellation, whole-root recovery, "
            "old-case replay, provider/Hello/hardware, caller-secret cut or security-family closure is proved.")
        if (commands_returned and not result["failures"] and not result["original_handle_close_failures"]
                and result["cleanup"] == "SETTLED_ALLOWLISTED_GENERATED_ROOT_REMOVED"):
            result["operational_status"] = "COMMANDS_RETURNED_WITH_RAW_XML_AWAITING_INDEPENDENT_RESULT_REVIEW"
        write_json(evidence / "result.json", result)
        print(json.dumps(result, sort_keys=True))
    # No custom cancellation handler may swallow a signal after the last
    # sticky-flag check and permit successful process exit. Hard termination
    # can still prevent evidence/upload; hosted disposal is not cleanup proof.
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGBREAK):
        signal.signal(number, signal.SIG_DFL)
    return 0 if not CANCELLED and result["operational_status"] != "FAILED_OR_INCOMPLETE" else 1


if __name__ == "__main__":
    raise SystemExit(main())
