#!/usr/bin/env python3
"""Focused NON-PUBLISHING native-five cycle; source is not execution admission.

Only the newly admitted Actions instance may run this file. No old runner,
capture or recovery helper is imported. Normal settlement trusts synchronous
stock tools plus the reviewed closed workload; this is NOT an arbitrary
descendant controller. Timeout/cancellation/uncertainty forbids tree deletion.
"""

import hashlib
import json
import os
import platform
import re
import select
import shutil
import signal
import stat
import subprocess
import sys
import time


REQUEST = "docs/audit-continuation/2026-09-08-linux/requests/macos-focused-01.json"
SCRIPT = "scripts/audit/macos_focused_validation.py"
WORKFLOW = ".github/workflows/audit-macos-focused-validation.yml"
BRANCH = "refs/heads/codex/audit-continuation-linux-20260908"
SOURCE = "app-desktop/native/biometric-bridge"
DEVELOPER = "/Applications/Xcode_16.4.app/Contents/Developer"
PATH = "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
DIR_FLAGS = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
GIB = 1024 ** 3
LOG_CAP, XML_CAP, EVIDENCE_CAP = 2 * 1024 ** 2, 512 * 1024, 16 * 1024 ** 2
CASE_NAMES = (
    "passvault_biometric_macos_fixture_normal",
    "passvault_biometric_macos_fixture_early_return",
    "passvault_biometric_macos_fixture_cpp_exception",
    "passvault_biometric_abi",
    "passvault_biometric_macos_security",
)
NATIVE_INPUTS = {
    "CMakeLists.txt": "f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3",
    "include/passvault_biometric.h": "dc76bea46e1abc0a2950f55b549fe68cf1399d256756e85ad0a79ef52b228b85",
    "src/macos/passvault_biometric_macos.mm": "ba07db83b6ff8b482f4fd1d40afea2d10419c454d7856a6a1eb4ba92aa5542c9",
    "tests/passvault_biometric_abi_test.cpp": "354d7e1904d46a2155774052ce20c476763c97f9ed293b94c4bce54208fa3785",
    "tests/macos/passvault_biometric_macos_security_test.mm": "946bda34cb6b536ab347539637e1f60bc41532d49e1cd08dc655398e9972fab4",
}


class Hold(Exception):
    pass


def identity(st):
    return st.st_dev, st.st_ino, stat.S_IFMT(st.st_mode), st.st_uid


def absolute(value):
    if not isinstance(value, str) or not value.startswith("/") or len(value.encode()) > 2048:
        raise Hold("bounded absolute path required")
    if any(c in value for c in "\x00\r\n\t") or any(p in ("", ".", "..") for p in value.split("/")[1:]):
        raise Hold("noncanonical path")
    return value


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise Hold("duplicate request key")
        result[key] = value
    return result


class Cycle:
    def __init__(self):
        self.started = time.monotonic()
        self.cancelled = False
        self.uncertain = False
        self.handles, self.anchors, self.children = [], [], {}
        self.root = self.evidence = self.temp = None
        self.bound = False
        self.in_cleanup = False
        self.retained = 0
        self.env = {"PATH": PATH, "LANG": "C", "LC_ALL": "C", "TZ": "UTC", "DEVELOPER_DIR": DEVELOPER,
                    "GIT_CONFIG_NOSYSTEM": "1", "GIT_CONFIG_GLOBAL": "/dev/null", "GIT_TERMINAL_PROMPT": "0"}
        self.result = {"status": "HOLD", "cases": {n: "UNSTARTED" for n in CASE_NAMES},
                       "commands": [], "resources": [], "acl_checks": [], "cleanup": "NOT_STARTED", "errors": []}

    def interrupted(self, _number, _frame):
        self.cancelled = True
        self.uncertain = True

    def check_time(self):
        if self.cancelled or time.monotonic() - self.started >= (540 if self.in_cleanup else 480):
            raise Hold("cancellation/global eight-minute command budget")

    def open_dir(self, name, parent=None):
        item = {"fd": None, "name": name, "parent": parent}
        self.handles.append(item)  # Finalizer knows the slot before acquisition.
        item["fd"] = os.open(name, DIR_FLAGS, dir_fd=None if parent is None else parent["fd"])
        item["identity"] = identity(os.fstat(item["fd"]))
        return item

    def close(self, item):
        fd, item["fd"] = item["fd"], None  # A failed close is never retried.
        if fd is not None:
            os.close(fd)

    def check_dir(self, item, private=False):
        current = os.fstat(item["fd"])
        named = os.stat(item["name"], dir_fd=None if item["parent"] is None else item["parent"]["fd"],
                        follow_symlinks=False)
        if identity(current) != item["identity"] or identity(named) != item["identity"]:
            raise Hold("original directory identity changed")
        if not stat.S_ISDIR(current.st_mode) or current.st_uid not in (0, os.geteuid()) or current.st_mode & 0o022:
            raise Hold("unsafe directory owner/write permissions")
        if private and (current.st_uid != os.geteuid() or stat.S_IMODE(current.st_mode) != 0o700):
            raise Hold("private owned directory required")

    def os_read(self, argv, cap=32768):
        """Fixed stock metadata tools only; never application/provider commands."""
        self.check_time()
        try:
            done = subprocess.run(argv, env=self.env, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                  stderr=subprocess.STDOUT, timeout=10, check=False, close_fds=True)
        except BaseException:
            self.uncertain = True
            raise
        if done.returncode < 0:
            self.uncertain = True
        if done.returncode != 0 or len(done.stdout) > cap:
            raise Hold("stock metadata tool failed/over-limit: " + argv[0])
        return done.stdout

    def acl(self, path, ancestor=False):
        # One non-inheriting deny-delete ancestor entry grants no access and we
        # never delete that ancestor. No ACL is admitted on new private roots.
        raw = self.os_read(["/bin/ls", "-lde", path])
        output = raw.decode("utf-8", "strict").splitlines()
        if not output or not output[0].split() or not output[0].split()[0].startswith("d"):
            raise Hold("unrecognized directory ACL header")
        no_acl = len(output) == 1 and "+" not in output[0].split()[0]
        deny_only = (ancestor and len(output) == 2 and "+" in output[0].split()[0]
                     and re.fullmatch(r"\s*0: group:everyone deny delete", output[1]) is not None)
        if not (no_acl or deny_only):
            raise Hold("ACL-bearing/ambiguous directory")
        self.result["acl_checks"].append({"path": path, "stdout_sha256": hashlib.sha256(raw).hexdigest(),
                                          "accepted": "NO_ACL" if no_acl else "ANCESTOR_DENY_DELETE_ONLY"})

    def anchor(self, path):
        parent = self.open_dir("/")
        self.check_dir(parent)
        self.acl("/", ancestor=True)
        self.anchors.append((parent, "/"))
        prefix = ""
        for part in absolute(path).split("/")[1:]:
            prefix += "/" + part
            parent = self.open_dir(part, parent)
            self.check_dir(parent)
            self.acl(prefix, ancestor=True)
            self.anchors.append((parent, prefix))
        return parent

    def read_leaf(self, parent, relative, cap):
        pieces = relative.split("/")
        opened = []
        try:
            for part in pieces[:-1]:
                parent = self.open_dir(part, parent)
                opened.append(parent)
            named = os.stat(pieces[-1], dir_fd=parent["fd"], follow_symlinks=False)
            if not stat.S_ISREG(named.st_mode) or named.st_nlink != 1 or named.st_size > cap:
                raise Hold("unbounded/nonregular named input")
            fd = os.open(pieces[-1], os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC | os.O_NONBLOCK,
                         dir_fd=parent["fd"])
            try:
                before = os.fstat(fd)
                if identity(before) != identity(named) or before.st_nlink != 1 or before.st_size > cap:
                    raise Hold("unbounded/nonregular input leaf")
                raw = bytearray()
                while len(raw) <= cap:
                    block = os.read(fd, min(65536, cap + 1 - len(raw)))
                    if not block:
                        break
                    raw.extend(block)
                after = os.fstat(fd)
                if len(raw) > cap or (identity(before), before.st_size, before.st_mtime_ns, before.st_ctime_ns) != (
                        identity(after), after.st_size, after.st_mtime_ns, after.st_ctime_ns):
                    raise Hold("input leaf changed/over-limit")
                return bytes(raw)
            finally:
                os.close(fd)
        finally:
            for item in reversed(opened):
                self.close(item)

    def save(self, name, raw, final=False):
        limit = EVIDENCE_CAP if final else EVIDENCE_CAP - 65536
        if self.evidence is None or self.retained + len(raw) > limit or "/" in name:
            raise Hold("evidence unavailable/over-limit")
        self.check_dir(self.evidence, private=True)
        fd = os.open(name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                     0o600, dir_fd=self.evidence["fd"])
        self.retained += len(raw)  # Reserve even if a partial write/fsync/close fails.
        try:
            view = memoryview(raw)
            while view:
                count = os.write(fd, view)
                if count <= 0:
                    raise Hold("short evidence write")
                view = view[count:]
            os.fsync(fd)
        finally:
            os.close(fd)
        return hashlib.sha256(raw).hexdigest()

    def resources(self, launching=False):
        output = self.os_read(["/usr/bin/vm_stat"]).decode("ascii", "strict")
        page = re.search(r"page size of ([0-9]+) bytes", output)
        free = re.search(r"^Pages free:\s+([0-9]+)\.\s*$", output, re.MULTILINE)
        if page is None or free is None:
            raise Hold("unrecognized OS free-physical-page reading")
        free_ram = int(page[1]) * int(free[1])  # Conservative free pages only; no reclaimability formula.
        disk = min(shutil.disk_usage(self.workspace).free, shutil.disk_usage(self.temp_path).free)
        self.result["resources"].append({"elapsed_s": round(time.monotonic() - self.started, 3),
                                         "disk_free_bytes": disk, "free_physical_bytes": free_ram})
        disk_floor, percent = (12 * GIB, 25) if launching else (8 * GIB, 20)
        if free_ram > self.total_ram or disk < disk_floor or free_ram * 100 < self.total_ram * percent:
            raise Hold("handoff resource floor not met using conservative free-physical reading")

    def command(self, label, argv, seconds, case=None, require_zero=True):
        self.resources(launching=True)
        self.check_time()
        record = {"label": label, "argv": argv, "exit": None, "settlement": "UNKNOWN"}
        self.result["commands"].append(record)
        raw, process, finished = bytearray(), None, False
        try:
            if case is not None:
                self.result["cases"][case] = "LAUNCH_ATTEMPT_OUTCOME_UNKNOWN"
            process = subprocess.Popen(argv, cwd=self.workspace, env=self.env, stdin=subprocess.DEVNULL,
                                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, close_fds=True)
            if case is not None:
                self.result["cases"][case] = "STARTED_OUTCOME_UNKNOWN"
            deadline, sample_at, eof = time.monotonic() + seconds, time.monotonic(), False
            while True:
                self.check_time()
                now = time.monotonic()
                if now >= deadline:
                    raise Hold("foreground command timeout")
                if now >= sample_at:
                    self.resources()
                    sample_at = time.monotonic() + 5
                if not eof and select.select([process.stdout], [], [], 0.2)[0]:
                    block = os.read(process.stdout.fileno(), 65536)
                    if not block:
                        eof = True
                    else:
                        available = LOG_CAP - len(raw)
                        raw.extend(block[:available])
                        if len(block) > available:
                            raise Hold("command log exceeds two MiB")
                code = process.poll()
                if code is not None and eof:
                    record["exit"] = code
                    if code < 0:
                        raise Hold("foreground tool ended by signal")
                    finished = True
                    if case is not None and code != 0:
                        # CTest may return normally after timing out a child.
                        # Do not parse that failure into descendant settlement.
                        self.uncertain = True
                        record["settlement"] = "UNPROVEN_NONZERO_CTEST_MAY_INCLUDE_CHILD_TIMEOUT"
                    else:
                        record["settlement"] = "QUALIFIED_NORMAL_RETURN_AND_EOF_TRUSTED_SYNCHRONOUS_TOOLS"
                    break
                if eof:
                    time.sleep(0.1)
        finally:
            try:
                if not finished:
                    self.uncertain = True
                    # Only this original Popen child, never a copied PID or group.
                    # Forced stop NEVER licenses descendant settlement/tree cleanup.
                    if process is not None and process.poll() is None:
                        try:
                            process.terminate()
                            process.wait(timeout=5)
                        except subprocess.TimeoutExpired:
                            process.kill()
                            process.wait(timeout=5)
            except BaseException as error:
                self.uncertain = True
                record["original_child_stop_error"] = type(error).__name__ + ": " + str(error)[:256]
            finally:
                try:
                    if process is not None and process.stdout is not None:
                        process.stdout.close()
                except BaseException as error:
                    self.uncertain = True
                    record["stdout_close_error"] = type(error).__name__ + ": " + str(error)[:256]
                finally:
                    record["log_sha256"] = self.save(label + ".log", bytes(raw))
        if "stdout_close_error" in record:
            raise Hold("original child stdout close failed; no later command or tree deletion")
        if require_zero and record["exit"] != 0:
            raise Hold("nonzero command exit: " + label)
        return bytes(raw)

    def prepare(self):
        if platform.system() != "Darwin" or platform.machine() != "x86_64" or len(sys.argv) != 1:
            raise Hold("only the admitted Intel macOS Actions entry is supported")
        expected = {"PV_REPOSITORY": "Apdelrahman1911/passvault", "PV_REF": BRANCH,
                    "PV_EVENT": "push", "PV_ATTEMPT": "1"}
        if any(os.environ.get(k) != v for k, v in expected.items()):
            raise Hold("wrong repository/branch/event/attempt")
        run_id, activation = os.environ["PV_RUN_ID"], os.environ["PV_SHA"]
        if re.fullmatch(r"[1-9][0-9]{0,19}", run_id) is None or re.fullmatch(r"[0-9a-f]{40}", activation) is None:
            raise Hold("bad run/source identity")
        self.workspace = absolute(os.environ["PV_WORKSPACE"])
        self.temp_path = absolute(os.environ["PV_RUNNER_TEMP"])
        if os.getcwd() != self.workspace:
            raise Hold("unexpected working directory")
        workspace_fd = self.anchor(self.workspace)
        self.temp = self.anchor(self.temp_path)
        request = json.loads(self.read_leaf(workspace_fd, REQUEST, 4096), object_pairs_hook=unique_object)
        if set(request) != {"source_commit", "source_tree", "nonce"}:
            raise Hold("wrong focused request fields")
        if any(not isinstance(request[k], str) or re.fullmatch(r"[0-9a-f]{40}", request[k]) is None
               for k in ("source_commit", "source_tree")) or re.fullmatch(r"[0-9a-f]{32}", request["nonce"]) is None:
            raise Hold("bad source/nonce binding")
        git = ["/usr/bin/git", "--no-pager", "-c", "core.fsmonitor=false", "-c", "core.untrackedCache=false"]
        facts = [self.os_read(git + args).decode().strip() for args in (
            ["rev-parse", "HEAD"], ["rev-parse", "HEAD^"], ["rev-parse", "HEAD^{tree}"],
            ["rev-parse", "HEAD^1^{tree}"], ["diff-tree", "--no-commit-id", "--name-status", "-r", "HEAD"],
            ["status", "--porcelain=v1", "--untracked-files=all"],
        )]
        parents = self.os_read(git + ["show", "-s", "--format=%P", "HEAD"]).decode().strip().split()
        if facts[0] != activation or facts[1] != request["source_commit"] or facts[3] != request["source_tree"]:
            raise Hold("source/activation ancestry mismatch")
        if parents != [request["source_commit"]] or facts[4] != "A\t" + REQUEST or facts[5]:
            raise Hold("single-parent activation must add only the new request in a clean checkout")
        for name in (SCRIPT, WORKFLOW):
            if self.read_leaf(workspace_fd, name, 65536) != self.os_read(git + ["show", facts[1] + ":" + name], 65536):
                raise Hold("helper/workflow checkout-byte mismatch")
        for name, digest in NATIVE_INPUTS.items():
            if hashlib.sha256(self.read_leaf(workspace_fd, SOURCE + "/" + name, 262144)).hexdigest() != digest:
                raise Hold("reviewed native input/checkout-EOL mismatch: " + name)
        stem = "passvault-macos-focused-" + run_id + "-1"
        self.evidence_path = self.temp_path + "/" + stem + "-evidence"
        os.mkdir(stem + "-evidence", 0o700, dir_fd=self.temp["fd"])
        self.evidence = self.open_dir(stem + "-evidence", self.temp)
        self.check_dir(self.evidence, private=True)
        self.acl(self.evidence_path)
        # The sole existing host command-file interface, not a cleanup target.
        output = absolute(os.environ["PV_OUTPUT"])
        output_prefix = self.temp_path + "/_runner_file_commands/"
        if not output.startswith(output_prefix) or "/" in output[len(output_prefix):]:
            raise Hold("unexpected Actions output interface")
        output_parent = self.open_dir("_runner_file_commands", self.temp)
        self.check_dir(output_parent)
        self.acl(self.temp_path + "/_runner_file_commands")
        output_leaf = output[len(output_prefix):]
        named = os.stat(output_leaf, dir_fd=output_parent["fd"], follow_symlinks=False)
        if not stat.S_ISREG(named.st_mode) or named.st_uid != os.geteuid() or named.st_nlink != 1 or named.st_size:
            raise Hold("unsafe named Actions output interface")
        fd = os.open(output_leaf, os.O_WRONLY | os.O_APPEND | os.O_NOFOLLOW | os.O_CLOEXEC | os.O_NONBLOCK,
                     dir_fd=output_parent["fd"])
        try:
            st = os.fstat(fd)
            if identity(st) != identity(named) or st.st_nlink != 1 or st.st_size != 0:
                raise Hold("unsafe Actions output interface")
            message = ("evidence_path=" + self.evidence_path + "\n").encode()
            if os.write(fd, message) != len(message):
                raise Hold("short Actions output write")
            os.fsync(fd)
        finally:
            os.close(fd)
            self.close(output_parent)
        self.root_path = self.temp_path + "/" + stem + "-" + request["nonce"]
        self.result.update({"source": request, "activation_commit": facts[0], "activation_tree": facts[2],
                            "run_id": run_id, "synthetic_root": self.root_path,
                            "trust": "QUALIFIED_NORMAL_PATH_STOCK_TOOLS_NOT_ARBITRARY_DESCENDANT_CONTAINMENT"})
        self.save("intent.json", json.dumps(self.result, sort_keys=True).encode())
        os.mkdir(stem + "-" + request["nonce"], 0o700, dir_fd=self.temp["fd"])
        self.root = self.open_dir(stem + "-" + request["nonce"], self.temp)
        self.check_dir(self.root, private=True)
        self.acl(self.root_path)
        for name in ("build", "home", "tmp", "cache", "logs", "native-parent"):
            os.mkdir(name, 0o700, dir_fd=self.root["fd"])
            self.children[name] = self.open_dir(name, self.root)
            self.check_dir(self.children[name], private=True)
            self.acl(self.root_path + "/" + name)
        self.bound = True
        self.env.update({"HOME": self.root_path + "/home", "TMPDIR": self.root_path + "/tmp",
                         "TMP": self.root_path + "/tmp", "TEMP": self.root_path + "/tmp",
                         "XDG_CACHE_HOME": self.root_path + "/cache", "CMAKE_BUILD_PARALLEL_LEVEL": "1",
                         "CTEST_PARALLEL_LEVEL": "1", "PASSVAULT_NATIVE_TEST_PARENT": self.root_path + "/native-parent"})
        self.total_ram = int(self.os_read(["/usr/sbin/sysctl", "-n", "hw.memsize"]).strip())
        if self.total_ram <= 0:
            raise Hold("invalid physical RAM total")

    def run(self):
        cmake = shutil.which("cmake", path=PATH)
        if cmake is None:
            raise Hold("installed CMake unavailable; no install/fallback")
        cmake = os.path.realpath(cmake)
        ctest = os.path.dirname(cmake) + "/ctest"
        versions = {"cmake": self.os_read([cmake, "--version"]).decode(),
                    "ctest": self.os_read([ctest, "--version"]).decode(),
                    "xcode": self.os_read(["/usr/bin/xcodebuild", "-version"]).decode()}
        if not versions["cmake"].startswith("cmake version 4.4.2\n") or not versions["ctest"].startswith("ctest version 4.4.2\n"):
            raise Hold("CMake/CTest drift")
        if versions["xcode"].strip() != "Xcode 16.4\nBuild version 16F6":
            raise Hold("Xcode drift")
        xcrun = ["/usr/bin/xcrun", "--sdk", "macosx15.5"]
        sdk = absolute(self.os_read(xcrun + ["--show-sdk-path"]).decode().strip())
        cc = absolute(self.os_read(xcrun + ["--find", "clang"]).decode().strip())
        cxx = absolute(self.os_read(xcrun + ["--find", "clang++"]).decode().strip())
        if any(not value.startswith(DEVELOPER + "/") for value in (sdk, cc, cxx)):
            raise Hold("toolchain outside selected Xcode")
        if self.os_read(xcrun + ["--show-sdk-version"]).strip() != b"15.5":
            raise Hold("SDK drift")
        self.result["tools"] = {"versions": versions, "cmake": cmake, "ctest": ctest, "sdk": sdk,
                                "cc": cc, "cxx": cxx, "python": sys.version, "architecture": platform.machine()}
        build = self.root_path + "/build"
        self.command("configure", [cmake, "-S", self.workspace + "/" + SOURCE, "-B", build,
                     "-G", "Unix Makefiles", "-DBUILD_TESTING=ON", "-DCMAKE_BUILD_TYPE=Release",
                     "-DCMAKE_OSX_ARCHITECTURES=x86_64", "-DCMAKE_OSX_SYSROOT=" + sdk,
                     "-DCMAKE_MAKE_PROGRAM=/usr/bin/make", "-DCMAKE_C_COMPILER=" + cc,
                     "-DCMAKE_CXX_COMPILER=" + cxx, "-DCMAKE_OBJCXX_COMPILER=" + cxx], 120)
        self.command("build", [cmake, "--build", build, "--config", "Release", "--parallel", "1", "--target",
                     "passvault_biometric_macos_security_test", "passvault_biometric_abi_test"], 240)
        binaries = ("libpassvault_biometric.dylib", "passvault_biometric_macos_security_test", "passvault_biometric_abi_test")
        self.result["binary_hashes_only"] = {name: hashlib.sha256(self.read_leaf(self.children["build"], name,
                                                 16 * 1024 ** 2)).hexdigest() for name in binaries}
        architectures = self.command("architecture", ["/usr/bin/file", *[build + "/" + name for name in binaries]], 10)
        lines = architectures.decode("utf-8", "strict").splitlines()
        if len(lines) != 3 or any("Mach-O 64-bit " not in line or "x86_64" not in line for line in lines):
            raise Hold("unexpected built Mach-O architecture")
        inventory = json.loads(self.command("inventory", [ctest, "--test-dir", build, "--show-only=json-v1"], 30))
        tests = inventory.get("tests", [])
        names = [test.get("name") for test in tests]
        if len(names) != 5 or set(names) != set(CASE_NAMES):
            raise Hold("unexpected CTest inventory")
        for test in tests:
            name = test["name"]
            argv = [build + "/passvault_biometric_macos_security_test"]
            if name == "passvault_biometric_abi":
                argv = [build + "/passvault_biometric_abi_test"]
            elif name.startswith("passvault_biometric_macos_fixture_"):
                argv += ["--fixture-case", name.removeprefix("passvault_biometric_macos_fixture_")]
            if test.get("command") != argv:
                raise Hold("unexpected CTest executable/selectors")
        for index, name in enumerate(CASE_NAMES, 1):
            parent = self.children["native-parent"]
            self.check_dir(parent, private=True)
            self.acl(self.root_path + "/native-parent")
            if os.listdir(parent["fd"]):
                raise Hold("fixture parent not empty; never sweep/retry")
            xml_name = "case-" + str(index).zfill(2) + ".xml"
            self.command("case-" + str(index).zfill(2), [ctest, "--test-dir", build, "-C", "Release",
                         "--parallel", "1", "--timeout", "30", "--no-tests=error", "--output-on-failure",
                         "--output-junit", self.root_path + "/logs/" + xml_name, "-R", "^" + name + "$"],
                         45, case=name, require_zero=False)
            code = self.result["commands"][-1]["exit"]
            try:
                raw = self.read_leaf(self.children["logs"], xml_name, XML_CAP)
            except FileNotFoundError:
                self.result["cases"][name] = {"exit": code, "status": "XML_MISSING_UNADJUDICATED"}
                raise Hold("JUnit XML absent after normal CTest return")
            if not raw:
                raise Hold("empty JUnit XML")
            digest = self.save(xml_name, raw)
            self.result["cases"][name] = {"status": "EXIT_RECORDED_XML_RETAINED_UNADJUDICATED", "exit": code,
                                         "xml_sha256": digest}
            if code != 0:
                raise Hold("case failed; raw XML snapshot retained, no later case or tree deletion")
            self.check_dir(parent, private=True)
            self.acl(self.root_path + "/native-parent")
            if os.listdir(parent["fd"]):
                raise Hold("fixture left residue; no later case or child cleanup")
        self.result["status"] = "EXECUTION_COMPLETE_INDEPENDENT_RESULT_REVIEW_REQUIRED"

    def remove_contents(self, parent, depth, budget):
        self.check_time()
        if depth > 12:
            raise Hold("generated-tree depth bound")
        names = os.listdir(parent["fd"])
        budget[0] += len(names)
        if budget[0] > 4096:
            raise Hold("generated-tree entry bound")
        for name in names:
            self.check_time()
            before = os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)
            if before.st_dev != self.root["identity"][0] or before.st_uid != os.geteuid():
                raise Hold("foreign generated entry")
            if stat.S_ISDIR(before.st_mode):
                child = self.open_dir(name, parent)
                if child["identity"] != identity(before):
                    raise Hold("generated directory replaced")
                self.remove_contents(child, depth + 1, budget)
                self.check_dir(child)
                self.check_time()
                os.rmdir(name, dir_fd=parent["fd"])
                self.close(child)
            elif (stat.S_ISREG(before.st_mode) or stat.S_ISLNK(before.st_mode)) and before.st_nlink == 1:
                if identity(os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)) != identity(before):
                    raise Hold("generated leaf replaced")
                os.unlink(name, dir_fd=parent["fd"])
            else:
                raise Hold("unexpected generated type/link count")

    def cleanup(self):
        if self.uncertain or self.cancelled or not self.bound:
            self.result["cleanup"] = "HOLD_NO_TREE_DELETION_UNCERTAIN_OR_INCOMPLETE_BINDING"
            return
        for item, path in self.anchors:
            self.check_dir(item)
            self.acl(path, ancestor=True)
        self.check_dir(self.root, private=True)
        self.acl(self.root_path)
        self.save("cleanup-start.json", json.dumps({"root_identity": self.root["identity"],
                  "authority": "ORIGINAL_PRIVATE_ROOT_TRUSTED_SYNCHRONOUS_NORMAL_RETURNS_ONCE_ONLY"}).encode())
        budget, residue = [0], False
        for name, child in self.children.items():
            self.check_time()
            self.check_dir(child, private=True)
            self.acl(self.root_path + "/" + name)
            if name == "native-parent" and os.listdir(child["fd"]):
                residue = True  # Never inherit a failed fixture child's deletion authority.
                continue
            if name != "native-parent":
                self.remove_contents(child, 0, budget)
            self.check_dir(child, private=True)
            self.check_time()
            os.rmdir(name, dir_fd=self.root["fd"])
            self.close(child)
        if residue or os.listdir(self.root["fd"]):
            self.result["cleanup"] = "HOLD_RESIDUE_PRESERVED_SAFE_OTHER_GENERATED_OUTPUTS_REMOVED"
            return
        self.check_dir(self.root, private=True)
        self.check_time()
        os.rmdir(self.root["name"], dir_fd=self.temp["fd"])
        try:
            os.stat(self.root["name"], dir_fd=self.temp["fd"], follow_symlinks=False)
        except FileNotFoundError:
            pass
        else:
            raise Hold("root removal not observed; no second attempt")
        self.close(self.root)
        self.result["cleanup"] = "QUALIFIED_GENERATED_ROOT_REMOVED_ORIGINAL_HANDLES"

    def finish(self):
        self.in_cleanup = True
        try:
            self.cleanup()
        except BaseException as error:
            self.result["cleanup"] = "HOLD_CLEANUP_ATTEMPT_CONSUMED_NO_RETRY"
            self.result["errors"].append(type(error).__name__ + ": " + str(error)[:512])
        # Keep E and its original parent/ancestors until the final evidence write.
        anchor_ids = {id(item) for item, _path in self.anchors}
        for item in reversed(self.handles):
            if item["fd"] is not None and item is not self.evidence and id(item) not in anchor_ids:
                try:
                    self.close(item)
                except OSError as error:
                    self.result["errors"].append("descriptor close failed; no retry: " + str(error))
        if (self.result["cleanup"] != "QUALIFIED_GENERATED_ROOT_REMOVED_ORIGINAL_HANDLES"
                or self.result["errors"] or self.cancelled or self.uncertain):
            self.result["status"] = "FAIL_OR_HOLD_NO_AUTOMATIC_RETRY"
        self.result.update({"cancelled": self.cancelled, "settlement_uncertain": self.uncertain})
        code = 0 if self.result["status"] == "EXECUTION_COMPLETE_INDEPENDENT_RESULT_REVIEW_REQUIRED" else 1
        try:
            if self.evidence is not None:
                self.result["evidence_and_ancestor_close"] = "PENDING_REQUIRE_ACTUAL_HELPER_EXIT_ZERO"
                self.save("result.json", json.dumps(self.result, sort_keys=True).encode(), final=True)
        except BaseException as error:
            code = 1
            self.result["errors"].append("final evidence failure: " + str(error)[:512])
        for item in reversed(self.handles):
            try:
                self.close(item)
            except OSError as error:
                code = 1
                self.result["errors"].append("final original descriptor close failed; no retry: " + str(error))
        if self.cancelled or self.uncertain:
            code = 1  # A signal during evidence fsync/close invalidates its earlier snapshot.
        if code != 0:
            self.result["status"] = "FAIL_OR_HOLD_NO_AUTOMATIC_RETRY"
        print(json.dumps({"exit": code, "status": self.result["status"], "cleanup": self.result["cleanup"],
                          "cancelled": self.cancelled, "settlement_uncertain": self.uncertain,
                          "errors": self.result["errors"]}, sort_keys=True))
        return 1 if self.cancelled or self.uncertain else code


def main():
    cycle = Cycle()
    os.umask(0o077)
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(number, cycle.interrupted)
    try:
        cycle.prepare()
        cycle.run()
    except BaseException as error:
        cycle.result["errors"].append(type(error).__name__ + ": " + str(error)[:512])
    finally:
        code = cycle.finish()
    # No further cleanup attempt is permitted. A signal after the last snapshot
    # must become an actual nonzero process outcome, not a swallowed late flag.
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(number, signal.SIG_DFL)
    return 1 if cycle.cancelled or cycle.uncertain else code


if __name__ == "__main__":
    raise SystemExit(main())
