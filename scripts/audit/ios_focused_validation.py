#!/usr/bin/env python3
"""Focused iOS01 NON-PUBLISHING source; not instance/execution admission.

One fresh exact simulator, two fixed module tasks, ten existing declarations.
No old helper is imported. Normal settlement is qualified to the reviewed
synchronous workload plus original wrapper stop/owned-device lifecycle, not
arbitrary descendant containment. Any interruption/uncertainty stays HOLD.
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
import xml.etree.ElementTree as ET


REQUEST = "docs/audit-continuation/2026-09-08-linux/requests/ios-focused-01.json"
SCRIPT = "scripts/audit/ios_focused_validation.py"
WORKFLOW = ".github/workflows/audit-ios-focused-validation.yml"
BRANCH = "refs/heads/codex/audit-continuation-linux-20260908"
DEVELOPER = "/Applications/Xcode_16.4.app/Contents/Developer"
ANDROID_SDK = "/Users/runner/Library/Android/sdk"
PATH = "/usr/bin:/bin:/usr/sbin:/sbin"
DIR_FLAGS = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
MIB, GIB = 1024 ** 2, 1024 ** 3
LOG_CAP, XML_CAP, EVIDENCE_CAP, RAW_CAP = 2 * MIB, 512 * 1024, 16 * MIB, 128 * MIB
WORK_SECONDS, CLEANUP_SECONDS = 24 * 60, 27 * 60
CRYPTO_CLASS = "com.passvault.core.crypto.SecurityTest"
ATTACHMENT_CLASS = "com.passvault.shared.platform.IosAttachmentFileStoreTest"
PROMPT_CLASS = "com.passvault.shared.platform.IosBiometricPromptStringsTest"
CASES = {
    "crypto": [(CRYPTO_CLASS, "VEK wrapping roundtrip"),
               (CRYPTO_CLASS, "wrong KEK fails VEK unwrapping")],
    "shared": [(ATTACHMENT_CLASS, name) for name in (
        "cancelled return after move removes the adopted plaintext directory",
        "cancelled return after copy fallback removes both plaintext locations",
        "successful adoption transfers directory cleanup to source close",
        "destination protection failure removes the moved file and owned directory",
        "cancelled admission removes the original picker copy without adopting it",
        "picker copy is protected before the import path is returned",
        "picker copy is deleted when immediate protection fails",
    )] + [(PROMPT_CLASS, "both native prompt operations receive the selected English or Arabic text")],
}
INPUTS = {
    "shared/src/iosTest/kotlin/com/passvault/shared/platform/IosAttachmentFileStoreTest.kt":
        "406c47fd2ea71afe50a57acfbc5050bcda05a3050e94a16c5555a5e053bdc337",
    "shared/src/iosTest/kotlin/com/passvault/shared/platform/IosBiometricPromptStringsTest.kt":
        "9e18f0bd193ba3308982ec05b45ab6d025e40f9b8b35baf880aa9ff66d640400",
    "core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/SecurityTest.kt":
        "867a98c43713ce879e129953e4fcdd4c314f3e737a85a5b7e7b78ed5360639f6",
}
INIT = r"""import groovy.json.JsonOutput
import org.gradle.api.tasks.testing.AbstractTestTask

def phase = System.getenv('PASSVAULT_IOS_PHASE')
def xmlRoot = System.getenv('PASSVAULT_IOS_XML_ROOT')
if (!(phase in ['crypto', 'shared']) || !xmlRoot) {
    throw new GradleException('Fixed iOS phase/XML root required')
}
def selected = phase == 'crypto' ? ':core:crypto:iosSimulatorArm64Test' : ':shared:iosSimulatorArm64Test'
gradle.beforeProject { p ->
    p.tasks.configureEach { t ->
        if (t.path == selected) {
            if (!(t instanceof AbstractTestTask)) throw new GradleException('Unexpected native test task type')
            t.reports.junitXml.required.set(true)
            t.reports.junitXml.outputLocation.set(new File(xmlRoot, phase + '-xml'))
            t.reports.html.required.set(false)
        }
    }
}
gradle.taskGraph.whenReady { graph ->
    def tests = graph.allTasks.findAll { it instanceof AbstractTestTask }*.path
    if (tests != [selected] || graph.allTasks.any { it.project.path in [':app-android', ':app-desktop'] }) {
        throw new GradleException('Unexpected test or application task in focused native graph')
    }
    def target = new File(xmlRoot, phase + '-graph.json')
    if (!target.createNewFile()) throw new GradleException('Graph receipt already exists')
    target.setText(JsonOutput.toJson([selected: selected, tasks: graph.allTasks*.path]), 'UTF-8')
}
"""


class Hold(Exception):
    pass


def identity(st):
    return st.st_dev, st.st_ino, stat.S_IFMT(st.st_mode), st.st_uid


def file_pin(st):
    return identity(st), st.st_mode, st.st_nlink, st.st_size, st.st_mtime_ns, st.st_ctime_ns


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
            raise Hold("duplicate JSON key")
        result[key] = value
    return result


class Cycle:
    def __init__(self):
        self.started = time.monotonic()
        self.cancelled = self.uncertain = self.bound = self.in_cleanup = False
        self.handles, self.anchors, self.children = [], [], {}
        self.root = self.evidence = self.temp = self.device_data = self.fixture = None
        self.device = None
        self.device_create_attempted = self.device_cleanup_attempted = False
        self.stop_required = self.stop_attempted = False
        self.init_attempted = False
        self.runtime_allocation_attempted = False
        self.current_module = None
        self.retained = 0
        self.env = {"PATH": PATH, "LANG": "C", "LC_ALL": "C", "TZ": "UTC", "DEVELOPER_DIR": DEVELOPER,
                    "GIT_CONFIG_NOSYSTEM": "1", "GIT_CONFIG_GLOBAL": "/dev/null", "GIT_TERMINAL_PROMPT": "0",
                    "GIT_OPTIONAL_LOCKS": "0", "GIT_NO_REPLACE_OBJECTS": "1", "GIT_NO_LAZY_FETCH": "1"}
        self.result = {"status": "HOLD", "declared_cases": CASES,
                       "modules": {name: {"status": "UNSTARTED"} for name in CASES},
                       "commands": [], "resources": [], "acl_checks": [], "errors": [],
                       "cleanup": "NOT_STARTED", "device_cleanup": "NOT_CREATED",
                       "name_mapping": "RAW_XML_UNBOUND_REQUIRES_INDEPENDENT_REVIEW_NO_NORMALIZATION"}

    def interrupted(self, _number, _frame):
        self.cancelled = True
        self.uncertain = True

    def remaining(self):
        return max(0.01, (CLEANUP_SECONDS if self.in_cleanup else WORK_SECONDS)
                   - (time.monotonic() - self.started))

    def check_time(self):
        if (self.cancelled and not self.in_cleanup) or self.remaining() <= 0.01:
            raise Hold("cancellation or fixed work/cleanup deadline")

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
        """Bounded fixed metadata commands, not project/application execution."""
        self.check_time()
        try:
            done = subprocess.run(argv, cwd=getattr(self, "workspace", None), env=self.env,
                                  stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                  timeout=min(10, self.remaining()), check=False, close_fds=True)
        except BaseException:
            self.uncertain = True
            raise
        if done.returncode < 0:
            self.uncertain = True
        if done.returncode != 0 or len(done.stdout) > cap:
            raise Hold("metadata command failed/over-limit: " + argv[0])
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

    def read_leaf(self, parent, relative, cap, *, with_stat=False):
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
                if file_pin(before) != file_pin(named) or before.st_nlink != 1 or before.st_size > cap:
                    raise Hold("unbounded/nonregular input leaf")
                raw = bytearray()
                while len(raw) <= cap:
                    block = os.read(fd, min(65536, cap + 1 - len(raw)))
                    if not block:
                        break
                    raw.extend(block)
                after = os.fstat(fd)
                final_named = os.stat(pieces[-1], dir_fd=parent["fd"], follow_symlinks=False)
                if (len(raw) != before.st_size or len(raw) > cap
                        or file_pin(before) != file_pin(after) or file_pin(before) != file_pin(final_named)):
                    raise Hold("input leaf changed/over-limit")
                return (bytes(raw), before) if with_stat else bytes(raw)
            finally:
                os.close(fd)
        finally:
            for item in reversed(opened):
                self.close(item)

    def save(self, name, raw, final=False):
        limit = EVIDENCE_CAP if final else EVIDENCE_CAP - 524288
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
        disk_floor, percent = (12 * GIB, 25) if launching else (8 * GIB, 20)
        record = {"elapsed_s": round(time.monotonic() - self.started, 3),
                  "phase": "launch" if launching else "running", "total_physical_bytes": self.total_ram,
                  "memory_metric": "kernel_free_equivalent", "page_size_bytes": None,
                  "printed_free_pages": None, "printed_speculative_pages": None,
                  "free_physical_bytes": None, "kernel_free_equivalent_bytes": None,
                  "disk_free_bytes": None, "disk_floor_bytes": disk_floor, "memory_floor_percent": percent}
        self.result["resources"].append(record)  # Preserve available observations even when a guard fails.
        output = self.os_read(["/usr/bin/vm_stat"]).decode("ascii", "strict")
        lines, invalid = output.splitlines(), []
        for name, prefix, pattern in (
            ("page_size_bytes", "Mach Virtual Memory Statistics",
             r"Mach Virtual Memory Statistics: \(page size of ([0-9]+) bytes\)"),
            ("printed_free_pages", "Pages free", r"Pages free:[ \t]+([0-9]+)\.[ \t]*"),
            ("printed_speculative_pages", "Pages speculative", r"Pages speculative:[ \t]+([0-9]+)\.[ \t]*"),
        ):
            candidates = [line for line in lines if line.lstrip().startswith(prefix)]
            parsed = re.fullmatch(pattern, candidates[0]) if len(candidates) == 1 else None
            if parsed is None:
                invalid.append(name + ": " + str(len(candidates)) + " matching rows")
            else:
                record[name] = int(parsed[1])
        if invalid:
            record["invalid_fields"] = invalid
            raise Hold("missing, duplicate or malformed OS free/speculative-page reading")
        page_size = record["page_size_bytes"]
        free_ram = page_size * record["printed_free_pages"]
        # Default vm_stat subtracts speculative from printed free. Reconstruct
        # the kernel free_count, not a sum of potentially reclaimable categories.
        kernel_free_ram = page_size * (record["printed_free_pages"] + record["printed_speculative_pages"])
        record.update({"free_physical_bytes": free_ram, "kernel_free_equivalent_bytes": kernel_free_ram})
        if page_size <= 0:
            raise Hold("invalid OS physical page size")
        disk = min(shutil.disk_usage(self.workspace).free, shutil.disk_usage(self.temp_path).free)
        if self.device_data is not None:
            self.check_dir(self.device_data)
            device_fs = os.fstatvfs(self.device_data["fd"])
            device_free = device_fs.f_bavail * device_fs.f_frsize
            record["device_disk_free_bytes"] = device_free
            record["device_filesystem"] = self.device_data["identity"][0]
            disk = min(disk, device_free)
        record["disk_free_bytes"] = disk
        if kernel_free_ram > self.total_ram or disk < disk_floor or kernel_free_ram * 100 < self.total_ram * percent:
            raise Hold("handoff resource floor not met using kernel-free-equivalent reading")

    def command(self, label, argv, seconds, *, cleanup=False, module=None,
                require_zero=True, stdin_fd=None, stdout_fd=None, stdout_cap=None):
        previous_cleanup = self.in_cleanup
        self.in_cleanup = self.in_cleanup or cleanup
        record = {"label": label, "argv": argv, "exit": None, "settlement": "UNKNOWN", "cleanup": cleanup}
        self.result["commands"].append(record)
        raw, process, pipe, finished = bytearray(), None, None, False
        try:
            if not cleanup:
                self.resources(launching=True)
            self.check_time()
            if module is not None:
                self.stop_required, self.stop_attempted = True, False
                self.result["modules"][module]["status"] = "LAUNCH_ATTEMPT_CASE_EXECUTION_UNPROVED"
            process = subprocess.Popen(
                argv, cwd=self.copy_path if module is not None or cleanup else self.workspace,
                env=self.env, stdin=subprocess.DEVNULL if stdin_fd is None else stdin_fd,
                stdout=subprocess.PIPE if stdout_fd is None else stdout_fd,
                stderr=subprocess.STDOUT if stdout_fd is None else subprocess.PIPE, close_fds=True,
            )
            pipe = process.stdout if stdout_fd is None else process.stderr
            deadline, sample_at, eof = time.monotonic() + min(seconds, self.remaining()), time.monotonic(), False
            while True:
                self.check_time()
                now = time.monotonic()
                if now >= deadline:
                    raise Hold("bounded foreground command timeout")
                if not cleanup and now >= sample_at:
                    self.resources()
                    sample_at = time.monotonic() + 5
                if stdout_fd is not None and os.fstat(stdout_fd).st_size > stdout_cap:
                    raise Hold("raw Git stdout exceeds admitted bound")
                if not eof and select.select([pipe], [], [], 0.2)[0]:
                    block = os.read(pipe.fileno(), 65536)
                    if not block:
                        eof = True
                    else:
                        available = LOG_CAP - len(raw)
                        raw.extend(block[:available])
                        if len(block) > available:
                            raise Hold("foreground command log exceeds two MiB")
                code = process.poll()
                if code is not None and eof:
                    record["exit"] = code
                    if code < 0:
                        raise Hold("foreground tool ended by signal")
                    finished = True
                    if module is not None and code != 0:
                        self.uncertain = True
                        record["settlement"] = "UNPROVEN_NONZERO_GRADLE_OR_NATIVE_CHILD_OUTCOME"
                    else:
                        record["settlement"] = "QUALIFIED_NORMAL_RETURN_EOF_TRUSTED_CLOSED_WORKLOAD"
                    break
                if eof:
                    time.sleep(0.1)
        finally:
            try:
                if not finished and process is not None:
                    self.uncertain = True
                    # Original Popen child only. This never proves all descendants settled.
                    if process.poll() is None:
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
                    if pipe is not None:
                        pipe.close()
                except BaseException as error:
                    self.uncertain = True
                    record["pipe_close_error"] = type(error).__name__ + ": " + str(error)[:256]
                finally:
                    try:
                        record["log_sha256"] = self.save(label + ".log", bytes(raw))
                    finally:
                        self.in_cleanup = previous_cleanup
        if "pipe_close_error" in record or (require_zero and record["exit"] != 0):
            raise Hold("foreground command failed: " + label)
        return bytes(raw)

    def prepare(self):
        if platform.system() != "Darwin" or platform.machine() != "arm64" or len(sys.argv) != 1:
            raise Hold("only the separately admitted Apple-Silicon Actions entry is supported")
        if sys.version_info[:3] != (3, 14, 7):
            raise Hold("installed Python differs from the documented bound; no installer/fallback")
        expected = {"PV_REPOSITORY": "Apdelrahman1911/passvault", "PV_REF": BRANCH,
                    "PV_EVENT": "push", "PV_ATTEMPT": "1", "PV_IMAGE_VERSION": "20260829.0321.1"}
        if any(os.environ.get(k) != v for k, v in expected.items()):
            raise Hold("wrong repository/branch/event/attempt or runner-image drift")
        run_id, activation = os.environ["PV_RUN_ID"], os.environ["PV_SHA"]
        if re.fullmatch(r"[1-9][0-9]{0,19}", run_id) is None or re.fullmatch(r"[0-9a-f]{40}", activation) is None:
            raise Hold("bad run/source identity")
        self.workspace = absolute(os.environ["PV_WORKSPACE"])
        self.temp_path = absolute(os.environ["PV_RUNNER_TEMP"])
        if os.getcwd() != self.workspace:
            raise Hold("unexpected working directory")
        self.workspace_dir, self.temp = self.anchor(self.workspace), self.anchor(self.temp_path)
        self.git = ["/usr/bin/git", "--no-pager"]
        for option in ("core.hooksPath=/dev/null", "core.fsmonitor=false", "core.untrackedCache=false",
                       "gc.auto=0", "maintenance.auto=false", "protocol.allow=never", "commit.gpgsign=false"):
            self.git += ["-c", option]
        request = json.loads(self.read_leaf(self.workspace_dir, REQUEST, 4096), object_pairs_hook=unique_object)
        if set(request) != {"source_commit", "source_tree", "nonce"}:
            raise Hold("wrong one-shot request fields")
        if any(not isinstance(request[k], str) or re.fullmatch(r"[0-9a-f]{40}", request[k]) is None
               for k in ("source_commit", "source_tree")) or re.fullmatch(r"[0-9a-f]{32}", request["nonce"]) is None:
            raise Hold("bad source/nonce binding")
        facts = [self.os_read(self.git + args).decode().strip() for args in (
            ["rev-parse", "HEAD"], ["show", "-s", "--format=%P", "HEAD"], ["rev-parse", "HEAD^{tree}"],
            ["rev-parse", "HEAD^1^{tree}"], ["diff-tree", "--no-commit-id", "--name-status", "-r", "HEAD"],
            ["status", "--porcelain=v1", "--untracked-files=all"],
        )]
        if (facts[0] != activation or facts[1] != request["source_commit"] or facts[3] != request["source_tree"]
                or facts[4] != "A\t" + REQUEST or facts[5]):
            raise Hold("clean single-parent activation must add only the new request to the exact source")
        for name in (SCRIPT, WORKFLOW):
            if self.read_leaf(self.workspace_dir, name, 128 * 1024) != self.os_read(
                    self.git + ["show", request["source_commit"] + ":" + name], 128 * 1024):
                raise Hold("helper/workflow checkout-byte mismatch")
        for name, digest in INPUTS.items():
            if hashlib.sha256(self.read_leaf(self.workspace_dir, name, 256 * 1024)).hexdigest() != digest:
                raise Hold("reviewed selected test/checkout-EOL mismatch: " + name)
        self.request, self.stem = request, "passvault-ios-focused-01-" + run_id + "-1"
        self.evidence_path = self.temp_path + "/" + self.stem + "-evidence"
        os.mkdir(self.stem + "-evidence", 0o700, dir_fd=self.temp["fd"])
        self.evidence = self.open_dir(self.stem + "-evidence", self.temp)
        self.check_dir(self.evidence, private=True)
        self.acl(self.evidence_path)
        self.expose_evidence()
        self.result.update({"source": request, "activation_commit": facts[0], "activation_tree": facts[2],
                            "run_id": run_id, "python": {"version": sys.version, "path": sys.executable},
                            "image_os_observed": os.environ.get("PV_IMAGE_OS"),
                            "image_version": os.environ["PV_IMAGE_VERSION"],
                            "trust": "TRUSTED_SYNCHRONOUS_WORKLOAD_NOT_ARBITRARY_DESCENDANT_CONTAINMENT"})
        self.save("intent.json", json.dumps(self.result, sort_keys=True).encode())
        self.total_ram = int(self.os_read(["/usr/sbin/sysctl", "-n", "hw.memsize"]).strip())
        self.result["total_physical_bytes"] = self.total_ram
        if self.total_ram <= 0:
            raise Hold("invalid installed physical RAM total")
        self.preflight_tools()  # Before simulator/private runtime/source-copy allocation.
        self.allocate_private_runtime()

    def expose_evidence(self):
        output = absolute(os.environ["PV_OUTPUT"])
        prefix = self.temp_path + "/_runner_file_commands/"
        if not output.startswith(prefix) or "/" in output[len(prefix):]:
            raise Hold("unexpected Actions output interface")
        parent = self.open_dir("_runner_file_commands", self.temp)
        self.check_dir(parent)
        self.acl(self.temp_path + "/_runner_file_commands")
        name = output[len(prefix):]
        named = os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)
        if not stat.S_ISREG(named.st_mode) or named.st_uid != os.geteuid() or named.st_nlink != 1 or named.st_size:
            raise Hold("unsafe named Actions output interface")
        fd = os.open(name, os.O_WRONLY | os.O_APPEND | os.O_NOFOLLOW | os.O_CLOEXEC | os.O_NONBLOCK,
                     dir_fd=parent["fd"])
        try:
            if file_pin(os.fstat(fd)) != file_pin(named):
                raise Hold("Actions output interface changed")
            message = ("evidence_path=" + self.evidence_path + "\n").encode()
            if os.write(fd, message) != len(message):
                raise Hold("short Actions output write")
            os.fsync(fd)
        finally:
            os.close(fd)
            self.close(parent)

    def json_command(self, label, argv, *, cleanup=False):
        raw = self.command(label, argv, 20, cleanup=cleanup)
        return json.loads(raw, object_pairs_hook=unique_object)

    def devices(self, label, *, cleanup=False):
        payload = self.json_command(label, ["/usr/bin/xcrun", "simctl", "list", "--json", "devices"],
                                    cleanup=cleanup)
        groups = payload.get("devices")
        if not isinstance(groups, dict):
            raise Hold("missing structured simulator device inventory")
        rows, seen = [], set()
        for runtime, devices in groups.items():
            if not isinstance(runtime, str) or not isinstance(devices, list):
                raise Hold("unrecognized simulator device inventory shape")
            for row in devices:
                if not isinstance(row, dict) or not isinstance(row.get("udid"), str) or not isinstance(row.get("name"), str):
                    raise Hold("unrecognized simulator identity fields")
                udid = row["udid"]
                if re.fullmatch(r"[0-9A-Fa-f]{8}(?:-[0-9A-Fa-f]{4}){3}-[0-9A-Fa-f]{12}", udid) is None:
                    raise Hold("malformed simulator UDID")
                if udid.lower() in seen:
                    raise Hold("duplicate simulator identity")
                seen.add(udid.lower())
                rows.append((runtime, row))
                if len(rows) > 4096:
                    raise Hold("simulator inventory bound")
        return rows

    def preflight_tools(self):
        if self.command("xcode-version", ["/usr/bin/xcodebuild", "-version"], 15).strip() != b"Xcode 16.4\nBuild version 16F6":
            raise Hold("selected Xcode version/build drift")
        xcrun = ["/usr/bin/xcrun", "--sdk", "iphonesimulator18.5"]
        sdk = absolute(self.command("sdk-path", xcrun + ["--show-sdk-path"], 15).decode().strip())
        if not sdk.startswith(DEVELOPER + "/") or self.command(
                "sdk-version", xcrun + ["--show-sdk-version"], 15).strip() != b"18.5":
            raise Hold("selected iOS simulator SDK drift")
        simctl = absolute(self.command("simctl-path", ["/usr/bin/xcrun", "--find", "simctl"], 15).decode().strip())
        if not simctl.startswith(DEVELOPER + "/"):
            raise Hold("simctl outside selected Xcode")
        # These installed help contracts are checked inside this one job, before
        # any device/source-replica allocation. No unsupported-option retry.
        for name, terms in {
            "create": ("<name>", "device type", "runtime"),
            "list": ("--json", "devices", "devicetypes", "runtimes"),
            "spawn": ("--standalone", "SIMCTL_CHILD_", "device", "executable"),
            "shutdown": ("device",),
            "delete": ("device",),
        }.items():
            help_text = self.command("help-" + name, ["/usr/bin/xcrun", "simctl", "help", name], 15).decode()
            if "Usage: simctl " + name + " " not in help_text or any(term not in help_text for term in terms):
                raise Hold("installed simctl command contract unsupported: " + name)
        runtimes = self.json_command("runtime-inventory", ["/usr/bin/xcrun", "simctl", "list", "--json", "runtimes"])
        types = self.json_command("type-inventory", ["/usr/bin/xcrun", "simctl", "list", "--json", "devicetypes"])
        if not isinstance(runtimes.get("runtimes"), list) or not isinstance(types.get("devicetypes"), list):
            raise Hold("missing runtime/device-type metadata")
        selected_runtimes = [r for r in runtimes["runtimes"] if isinstance(r, dict)
                             and r.get("name") == "iOS 18.5" and r.get("version") == "18.5"
                             and r.get("isAvailable") is True]
        selected_types = [t for t in types["devicetypes"] if isinstance(t, dict) and t.get("name") == "iPhone 16"]
        if len(selected_runtimes) != 1 or len(selected_types) != 1:
            raise Hold("required installed iOS18.5/iPhone16 metadata is absent/ambiguous")
        self.runtime, self.device_type = selected_runtimes[0], selected_types[0]
        for row in (self.runtime, self.device_type):
            if not isinstance(row.get("identifier"), str) or re.fullmatch(r"[A-Za-z0-9._-]{1,256}", row["identifier"]) is None:
                raise Hold("invalid observed simulator runtime/type identifier")
        self.device_name = self.stem + "-" + self.request["nonce"]
        baseline = self.devices("device-baseline")
        if any(row["name"] == self.device_name for _runtime, row in baseline):
            raise Hold("new simulator name already occupied; never reuse/delete it")
        self.prior_devices = {row["udid"].lower() for _runtime, row in baseline}
        self.java = absolute(os.path.realpath(absolute(os.environ["PV_JAVA_HOME_17"])))
        java_dir = self.anchor(self.java)
        release = self.read_leaf(java_dir, "release", 32768).decode()
        fields = dict(re.findall(r'^([A-Z_]+)="([^"\n]*)"$', release, re.MULTILINE))
        if fields.get("JAVA_VERSION") != "17.0.20" or fields.get("OS_ARCH") not in ("aarch64", "arm64"):
            raise Hold("installed JDK release is not the documented ARM JDK17")
        if not os.path.isdir(ANDROID_SDK):
            raise Hold("documented preinstalled Android SDK absent; no install/license action")
        self.result["tools"] = {"xcode": "16.4/16F6", "sdk": sdk, "simctl": simctl,
                                "runtime": self.runtime, "device_type": self.device_type, "java_home": self.java,
                                "jdk_release_sha256": hashlib.sha256(release.encode()).hexdigest(),
                                "android_sdk_configuration_only": ANDROID_SDK, "architecture": platform.machine()}

    def new_leaf(self, parent, name, raw, mode=0o600):
        self.check_time()
        self.check_dir(parent)
        fd = os.open(name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                     mode, dir_fd=parent["fd"])
        try:
            view = memoryview(raw)
            while view:
                self.check_time()
                count = os.write(fd, view)
                if count <= 0:
                    raise Hold("short private source/control write")
                view = view[count:]
            os.fsync(fd)
        finally:
            os.close(fd)

    def allocate_private_runtime(self):
        name = self.stem + "-" + self.request["nonce"]
        self.root_path, self.copy_path = self.temp_path + "/" + name, self.temp_path + "/" + name + "/source"
        # JVM option strings and compact guest receipts deliberately allow no whitespace/control text.
        if re.fullmatch(r"/[A-Za-z0-9_./-]+", self.root_path) is None:
            raise Hold("private runtime path incompatible with fixed JVM option contract")
        self.runtime_allocation_attempted = True
        self.result["private_runtime_allocation"] = {"path": self.root_path, "status": "ATTEMPT_UNBOUND"}
        os.mkdir(name, 0o700, dir_fd=self.temp["fd"])
        self.result["private_runtime_allocation"]["status"] = "CREATED_UNBOUND"
        self.root = self.open_dir(name, self.temp)
        self.check_dir(self.root, private=True)
        self.result["private_runtime_allocation"]["status"] = "ORIGINAL_ROOT_BOUND"
        self.acl(self.root_path)
        for child in ("source", "home", "tmp", "gradle-home", "project-cache", "konan", "android-user",
                      "xdg-cache", "xdg-config", "xdg-data", "xdg-state", "logs", "transport"):
            os.mkdir(child, 0o700, dir_fd=self.root["fd"])
            self.children[child] = self.open_dir(child, self.root)
            self.check_dir(self.children[child], private=True)
            self.acl(self.root_path + "/" + child)
        self.bound = True
        home, tmp = self.root_path + "/home", self.root_path + "/tmp"
        self.env.update({"PATH": self.java + "/bin:" + PATH, "JAVA_HOME": self.java, "HOME": home,
                         "TMPDIR": tmp + "/", "TMP": tmp, "TEMP": tmp,
                         "GRADLE_USER_HOME": self.root_path + "/gradle-home", "KONAN_DATA_DIR": self.root_path + "/konan",
                         "ANDROID_USER_HOME": self.root_path + "/android-user",
                         "ANDROID_HOME": ANDROID_SDK, "ANDROID_SDK_ROOT": ANDROID_SDK,
                         "PASSVAULT_IOS_XML_ROOT": self.root_path + "/logs",
                         "JAVA_TOOL_OPTIONS": "-Xmx512m -XX:-UsePerfData -Dfile.encoding=UTF-8 -Duser.home=" + home
                                              + " -Djava.io.tmpdir=" + tmp})
        for kind in ("cache", "config", "data", "state"):
            self.env["XDG_" + kind.upper() + "_HOME"] = self.root_path + "/xdg-" + kind
        self.flags = ["--no-daemon", "--max-workers=1", "--console=plain", "--no-parallel",
                      "--no-configure-on-demand", "--no-configuration-cache", "--no-build-cache",
                      "--dependency-verification=strict", "--stacktrace",
                      "--project-cache-dir", self.root_path + "/project-cache",
                      "-Pkotlin.compiler.execution.strategy=in-process", "-Pandroid.builder.sdkDownload=false",
                      "-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8"
                      + " -Duser.home=" + home + " -Djava.io.tmpdir=" + tmp,
                      "-Dorg.gradle.java.installations.auto-download=false",
                      "-Dorg.gradle.java.installations.auto-detect=false",
                      "-Dorg.gradle.java.installations.paths=" + self.java]
        self.result["private_runtime"] = {"path": self.root_path, "identity": self.root["identity"],
                                          "jvm_heap": "2GiB Gradle /512MiB explicit child default; not total native RAM",
                                          "strict_verification": True}
        version = self.command("java-version", [self.java + "/bin/java", "-version"], 15).decode()
        if re.search(r"17\.0\.20\+101(?:[^0-9]|$)", version) is None:
            raise Hold("documented installed JDK build mismatch")
        self.init_attempted = True
        self.new_leaf(self.root, "validation.init.gradle", INIT.encode())
        self.result["init_sha256"] = hashlib.sha256(INIT.encode()).hexdigest()

    def under(self, root, parts):
        parent, opened = root, []
        try:
            for part in parts:
                parent = self.open_dir(part, parent)
                opened.append(parent)
                self.check_dir(parent)
            return parent, opened
        except BaseException:
            for item in reversed(opened):
                self.close(item)
            raise

    def raw_source_copy(self):
        raw = self.os_read(self.git + ["ls-tree", "-r", "-z", self.request["source_commit"]], MIB)
        expected, folders = {}, set()
        for entry in raw.split(b"\x00")[:-1]:
            fields, name_raw = entry.split(b"\t", 1)
            mode, kind, oid = fields.decode("ascii").split(" ")
            name = name_raw.decode("utf-8", "strict")
            pieces = name.split("/")
            if (mode not in ("100644", "100755") or kind != "blob" or re.fullmatch(r"[0-9a-f]{40}", oid) is None
                    or name in expected or len(name_raw) > 2048 or len(pieces) > 32
                    or any(p in ("", ".", "..", ".git") for p in pieces)
                    or any(c in name for c in "\x00\r\n\t\\")):
                raise Hold("unsupported raw source member/mode/path")
            expected[name] = {"git_mode": mode, "git_blob": oid}
            folders.update("/".join(pieces[:n]) for n in range(1, len(pieces)))
        if not raw.endswith(b"\x00") or not expected or len(expected) > 8192 or set(expected) & folders:
            raise Hold("source tree count/framing/path collision")
        self.source_members = expected
        self.save("source-inventory.json", json.dumps(expected, sort_keys=True).encode())
        transport = self.children["transport"]
        oid_data = "".join(row["git_blob"] + "\n" for row in expected.values()).encode()
        self.new_leaf(transport, "source.oids", oid_data)
        source_in = os.open("source.oids", os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=transport["fd"])
        source_out = None
        try:
            source_in_pin = self.transport_pin("source.oids", source_in)
            source_out = os.open("source.blobs", os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                                 0o600, dir_fd=transport["fd"])
            source_out_pin = self.transport_pin("source.blobs", source_out)
            self.command("raw-source-transport", self.git + ["cat-file", "--batch"], 120,
                         stdin_fd=source_in, stdout_fd=source_out, stdout_cap=RAW_CAP)
            os.fsync(source_out)
            if (self.transport_pin("source.oids", source_in) != source_in_pin
                    or self.read_leaf(transport, "source.oids", MIB) != oid_data):
                raise Hold("raw object request changed")
            produced = self.transport_pin("source.blobs", source_out)
            if produced[:3] != source_out_pin[:3] or produced[3] > RAW_CAP:
                raise Hold("raw object transport size bound")
            with os.fdopen(os.dup(source_out), "rb") as stream:
                first_digest = self.raw_objects(stream, expected, produced, extract=False)
                for name in sorted(folders, key=lambda p: (p.count("/"), p)):
                    parent, opened = self.under(self.children["source"], name.split("/")[:-1])
                    try:
                        self.check_time()
                        os.mkdir(name.split("/")[-1], 0o700, dir_fd=parent["fd"])
                    finally:
                        for item in reversed(opened):
                            self.close(item)
                if self.raw_objects(stream, expected, produced, extract=True) != first_digest:
                    raise Hold("raw transport changed between verification/materialization")
            self.result["raw_source"] = {"representation": "RAW_GIT_BLOBS_NO_ARCHIVE_FILTERS_TEXTCONV",
                                         "members": len(expected), "stream_bytes": produced[3],
                                         "stream_sha256": first_digest, "complete_stream_passes": 2}
        finally:
            try:
                if source_out is not None:
                    os.close(source_out)
            finally:
                os.close(source_in)
        self.source_check("before")
        for name, expected_pin in (("source.oids", source_in_pin), ("source.blobs", produced)):
            self.unlink_owned_leaf(transport, name, expected_pin)

    def transport_pin(self, name, fd):
        if name not in ("source.oids", "source.blobs"):
            raise Hold("unknown raw transport leaf")
        parent = self.children["transport"]
        self.check_dir(parent, private=True)
        opened = os.fstat(fd)
        named = os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)
        if (file_pin(opened) != file_pin(named) or not stat.S_ISREG(opened.st_mode)
                or opened.st_nlink != 1 or opened.st_uid != os.geteuid()
                or opened.st_dev != self.root["identity"][0] or stat.S_IMODE(opened.st_mode) != 0o600):
            raise Hold("raw transport original descriptor/name mismatch")
        return file_pin(opened)

    def raw_objects(self, stream, expected, original, *, extract):
        stream.seek(0)
        digest, total = hashlib.sha256(), 0
        for name, row in expected.items():
            self.check_time()
            if self.transport_pin("source.blobs", stream.fileno()) != original:
                raise Hold("raw stream original identity changed")
            header = stream.readline(64)
            match = re.fullmatch(rb"([0-9a-f]{40}) blob (0|[1-9][0-9]{0,7})\n", header)
            if match is None or match[1].decode() != row["git_blob"]:
                raise Hold("raw blob header/order/OID/type mismatch")
            size = int(match[2])
            if size > 32 * MIB or total + len(header) + size + 1 > original[3]:
                raise Hold("raw blob size/truncation bound")
            digest.update(header)
            total += len(header)
            blob, remaining, out, opened = hashlib.sha1(b"blob " + match[2] + b"\x00"), size, None, []
            try:
                if extract:
                    parent, opened = self.under(self.children["source"], name.split("/")[:-1])
                    out = os.open(name.split("/")[-1], os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                                  0o600, dir_fd=parent["fd"])
                while remaining:
                    self.check_time()
                    block = stream.read(min(65536, remaining))
                    if not block:
                        raise Hold("truncated raw Git blob payload")
                    blob.update(block)
                    digest.update(block)
                    total += len(block)
                    remaining -= len(block)
                    if extract:
                        view = memoryview(block)
                        while view:
                            self.check_time()
                            count = os.write(out, view)
                            if count <= 0:
                                raise Hold("short replica write")
                            view = view[count:]
                if blob.hexdigest() != row["git_blob"] or stream.read(1) != b"\n":
                    raise Hold("raw Git blob/framing mismatch")
                total += 1
                digest.update(b"\n")
                if extract:
                    os.fchmod(out, int(row["git_mode"], 8) & 0o777)
                    os.fsync(out)
            finally:
                if out is not None:
                    os.close(out)
                for item in reversed(opened):
                    self.close(item)
        if stream.read(1) or total != original[3] or self.transport_pin("source.blobs", stream.fileno()) != original:
            raise Hold("raw stream count/trailing bytes/original identity mismatch")
        return digest.hexdigest()

    def source_check(self, phase, *, wrapper_only=False):
        names = ("gradlew", "gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties")
        rows = {n: self.source_members[n] for n in names} if wrapper_only else self.source_members
        changed = []
        for name, row in rows.items():
            self.check_time()
            data, captured = self.read_leaf(self.children["source"], name, 32 * MIB, with_stat=True)
            if (hashlib.sha1(b"blob " + str(len(data)).encode() + b"\x00" + data).hexdigest() != row["git_blob"]
                    or stat.S_IMODE(captured.st_mode) != (int(row["git_mode"], 8) & 0o777)):
                changed.append(name)
        self.result.setdefault("source_checks", []).append({"phase": phase, "members": len(rows), "changed": changed[:64]})
        if changed:
            raise Hold("replica source/wrapper drift; no changed source accepted")

    def unlink_owned_leaf(self, parent, name, expected_pin):
        self.check_time()
        self.check_dir(parent, private=True)
        current = os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)
        if (file_pin(current) != expected_pin or not stat.S_ISREG(current.st_mode) or current.st_nlink != 1
                or current.st_uid != os.geteuid() or current.st_dev != self.root["identity"][0]):
            raise Hold("owned temporary leaf identity changed")
        os.unlink(name, dir_fd=parent["fd"])

    def create_device(self):
        before = self.devices("device-precreate")
        if any(row["name"] == self.device_name for _runtime, row in before):
            raise Hold("new simulator namespace occupied; no creation/reuse")
        prior = self.prior_devices | {row["udid"].lower() for _runtime, row in before}
        self.device_create_attempted = True
        self.result["device_cleanup"] = "CREATE_ATTEMPT_OUTCOME_UNBOUND"
        raw = self.command("device-create", ["/usr/bin/xcrun", "simctl", "create", self.device_name,
                           self.device_type["identifier"], self.runtime["identifier"]], 60)
        udid = raw.decode("ascii", "strict").strip()
        if (re.fullmatch(r"[0-9A-Fa-f]{8}(?:-[0-9A-Fa-f]{4}){3}-[0-9A-Fa-f]{12}", udid) is None
                or udid.lower() in prior):
            raise Hold("creation did not return exactly one fresh simulator UDID")
        self.result["created_udid_unbound_until_post_inventory"] = udid
        matches = [(runtime, row) for runtime, row in self.devices("device-postcreate")
                   if row["udid"].lower() == udid.lower()]
        if len(matches) != 1:
            raise Hold("new simulator not uniquely observed after creation")
        runtime, row = matches[0]
        if (runtime != self.runtime["identifier"] or row["name"] != self.device_name
                or row.get("state") != "Shutdown" or row.get("isAvailable") is not True):
            raise Hold("created simulator identity/runtime/state mismatch")
        data_path = absolute(row.get("dataPath"))
        if (os.path.realpath(data_path) != data_path or data_path.split("/")[-1] != "data"
                or data_path.split("/")[-2].lower() != udid.lower()):
            raise Hold("new simulator dataPath is not the observed canonical UDID/data boundary")
        self.device = {"udid": udid, "name": self.device_name, "runtime": runtime, "data_path": data_path}
        self.result["owned_device"] = dict(self.device)
        self.result["device_cleanup"] = "OWNED_NEW_DEVICE_PENDING_CLOSEOUT"
        # Device anchors are not original-checkout/evidence anchors: simctl may
        # later remove this owned device. Retain their handles until closeout.
        count = len(self.anchors)
        try:
            self.device_data = self.anchor(data_path)
        finally:
            self.device_anchors, self.anchors = self.anchors[count:], self.anchors[:count]
        self.resources(launching=True)  # Include the actual newly owned simulator storage volume.
        name = "passvault-ios-test-parent-" + self.request["nonce"]
        self.fixture_path = data_path + "/" + name
        if re.fullmatch(r"/[A-Za-z0-9_./-]+", self.fixture_path) is None:
            raise Hold("new guest parent cannot satisfy the fixed receipt/path contract")
        os.mkdir(name, 0o700, dir_fd=self.device_data["fd"])
        self.fixture = self.open_dir(name, self.device_data)
        self.check_dir(self.fixture, private=True)
        self.acl(self.fixture_path)
        if os.listdir(self.fixture["fd"]):
            raise Hold("exclusive new guest fixture parent is not empty")
        self.result["guest_parent"] = {"path": self.fixture_path, "identity": self.fixture["identity"],
                                       "created_exclusive_empty": True,
                                       "foundation_status": "REQUIRES_SAME_PROCESS_MANDATORY_GUARD_RECEIPTS"}
        # Installed spawn help must advertise this forwarding before allocation.
        # If forwarding/TMPDIR semantics differ, the mandatory fixture guard
        # fails before fixture writes. No custom --set/default-device fallback.
        self.env["SIMCTL_CHILD_PASSVAULT_IOS_TEST_PARENT"] = self.fixture_path
        self.env["SIMCTL_CHILD_TMPDIR"] = self.fixture_path + "/"

    def stop_wrapper(self, module):
        if not self.stop_required or self.stop_attempted:
            return
        self.stop_attempted = True  # Once only, including refusals and cancellations.
        previous_cleanup, self.in_cleanup = self.in_cleanup, True
        try:
            self.source_check(module + "-before-stop", wrapper_only=True)
            self.command(module + "-gradle-stop", [self.copy_path + "/gradlew", "--stop", *self.flags],
                         60, cleanup=True)
            status = self.command(module + "-gradle-status", [self.copy_path + "/gradlew", "--status", *self.flags],
                                  30, cleanup=True)
            if b"No Gradle daemons are running." not in status:
                raise Hold("private Gradle registry does not confirm no daemons after stop")
            self.stop_required = False
            self.result["modules"][module]["wrapper_stop"] = "NORMAL_STOP_AND_EMPTY_PRIVATE_DAEMON_REGISTRY"
        except BaseException as error:
            self.uncertain = True
            self.result["modules"][module]["wrapper_stop"] = "HOLD_ONCE_ONLY_STOP_OR_REGISTRY_UNCERTAINTY"
            self.result["errors"].append(type(error).__name__ + ": " + str(error)[:512])
        finally:
            self.in_cleanup = previous_cleanup

    def capture_module_xml(self, module, directory):
        self.check_dir(directory, private=True)
        names = os.listdir(directory["fd"])
        if not names or len(names) > 8 or any(re.fullmatch(r"TEST-[A-Za-z0-9_.$-]+\.xml", n) is None for n in names):
            raise Hold("module XML absent/unexpected inventory; no listing/all-tests fallback")
        observed, identities, receipts = [], set(), []
        for index, name in enumerate(sorted(names), 1):
            raw = self.read_leaf(directory, name, XML_CAP)
            if not raw or b"<!DOCTYPE" in raw or b"<!ENTITY" in raw:
                raise Hold("empty/unsupported XML document")
            digest = self.save(module + "-" + str(index).zfill(2) + ".xml", raw)
            document = ET.fromstring(raw)
            if document.tag not in ("testsuite", "testsuites"):
                raise Hold("unrecognized JUnit document root")
            for test in document.iter("testcase"):
                pair = (test.get("classname"), test.get("name"))
                if pair in identities or not all(isinstance(value, str) and 0 < len(value) <= 1024 for value in pair):
                    raise Hold("duplicate/empty/unbounded raw XML case identity")
                identities.add(pair)
                observed.append({"classname": pair[0], "name": pair[1], "xml_sha256": digest,
                                 "failure_or_skip": any(test.find(tag) is not None for tag in ("failure", "error", "skipped"))})
            for output in document.iter("system-out"):
                for line in (output.text or "").splitlines():
                    if line.startswith("PASSVAULT_IOS_FIXTURE_PARENT\t"):
                        receipts.append(line)
        state = self.result["modules"][module]
        state.update({"raw_xml_cases": observed, "fixture_receipts": receipts,
                      "status": "RAW_XML_RETAINED_MAPPING_UNADJUDICATED"})
        expected_classes = {name for name, _method in CASES[module]}
        if (len(observed) != len(CASES[module]) or {row["classname"] for row in observed} != expected_classes
                or any(row["failure_or_skip"] for row in observed)):
            raise Hold("unexpected raw XML count/classes/failure/skip; never normalize into a pass")
        if module == "shared":
            if len(receipts) != 7:
                raise Hold("seven same-process mandatory fixture receipts are not retained in XML")
            for receipt in receipts:
                fields = receipt.split("\t")
                if (len(fields) != 3 or fields[1] != self.fixture_path
                        or not (fields[2] == self.fixture_path or fields[2].startswith(self.fixture_path + "/"))):
                    raise Hold("guest Foundation receipt does not match the original owned parent")
            self.result["guest_parent"]["foundation_status"] = "SEVEN_MANDATORY_SAME_PROCESS_RECEIPTS_UNADJUDICATED"
        # Method display/XML decoration was not established by the retained
        # plugin source. Preserve exact raw names; no stripping/suffix fallback
        # or case PASS occurs here. Independent exact-ten scoring is required.

    def run_module(self, module):
        self.current_module = module
        self.check_time()
        if self.uncertain or self.stop_required:
            raise Hold("no later module after uncertain command/worker settlement")
        self.source_check(module + "-wrapper", wrapper_only=True)
        if self.read_leaf(self.root, "validation.init.gradle", 16384) != INIT.encode():
            raise Hold("fixed native init source changed")
        self.check_dir(self.fixture, private=True)
        if os.listdir(self.fixture["fd"]):
            raise Hold("guest parent residue before module; no test retry/sweep")
        xml_name = module + "-xml"
        os.mkdir(xml_name, 0o700, dir_fd=self.children["logs"]["fd"])
        directory = self.open_dir(xml_name, self.children["logs"])
        self.check_dir(directory, private=True)
        self.env["PASSVAULT_IOS_PHASE"] = module
        task = ":core:crypto:iosSimulatorArm64Test" if module == "crypto" else ":shared:iosSimulatorArm64Test"
        filters = [class_name + "." + method for class_name, method in CASES["crypto"]] if module == "crypto" else [
            ATTACHMENT_CLASS, PROMPT_CLASS]
        argv = [self.copy_path + "/gradlew", "--init-script", self.root_path + "/validation.init.gradle", task]
        for pattern in filters:
            argv += ["--tests", pattern]
        argv += ["--device", self.device["udid"], *self.flags]
        try:
            self.command(module + "-gradle", argv, 720 if module == "crypto" else 1080,
                         module=module, require_zero=False)
            state = self.result["modules"][module]
            state["gradle_exit"] = self.result["commands"][-1]["exit"]
            graph = self.read_leaf(self.children["logs"], module + "-graph.json", 256 * 1024)
            self.save(module + "-graph.json", graph)
            self.capture_module_xml(module, directory)
            if state["gradle_exit"] != 0:
                raise Hold("nonzero native module outcome; no next module")
            self.check_dir(self.fixture, private=True)
            self.acl(self.fixture_path)
            if os.listdir(self.fixture["fd"]):
                raise Hold("fixture finalizer left residue; not repaired into test success")
            state["guest_parent_empty_after"] = True
        finally:
            self.stop_wrapper(module)
            self.close(directory)
        if self.uncertain or self.stop_required:
            raise Hold("private wrapper/worker settlement remains unproved")

    def run(self):
        self.raw_source_copy()
        self.create_device()
        for module in ("crypto", "shared"):
            self.run_module(module)
        self.source_check("after")
        self.result["status"] = "TEN_CASE_EVIDENCE_CAPTURED_INDEPENDENT_NAME_RESULT_REVIEW_REQUIRED"

    def settle_device(self):
        if self.device_cleanup_attempted:
            return
        self.device_cleanup_attempted = True
        if self.device is None:
            self.result["device_cleanup"] = ("HOLD_CREATION_OUTCOME_UNBOUND_NO_GUESSED_DELETE"
                                             if self.device_create_attempted else "NOT_CREATED")
            if self.device_create_attempted:
                self.uncertain = True
            return

        def bound_row(label):
            matches = [(runtime, row) for runtime, row in self.devices(label, cleanup=True)
                       if row["udid"].lower() == self.device["udid"].lower()]
            if len(matches) != 1:
                raise Hold("owned device identity missing/ambiguous; never select another")
            runtime, row = matches[0]
            if (runtime != self.device["runtime"] or row["name"] != self.device["name"]
                    or row.get("dataPath") != self.device["data_path"]):
                raise Hold("owned simulator identity/storage binding changed")
            return row

        row = bound_row("device-preclose")
        if row.get("state") not in ("Shutdown", "Booted", "Booting", "Shutting Down"):
            raise Hold("unrecognized owned simulator state; no guessed lifecycle operation")
        if row["state"] != "Shutdown":
            self.command("device-shutdown", ["/usr/bin/xcrun", "simctl", "shutdown", self.device["udid"]],
                         60, cleanup=True)
        if bound_row("device-shutdown-observed").get("state") != "Shutdown":
            raise Hold("owned simulator shutdown is not observed; no deletion/retry")
        if self.fixture is not None:
            try:
                self.check_dir(self.fixture, private=True)
                names = os.listdir(self.fixture["fd"])
                self.result["guest_parent"]["closeout_entries"] = names[:64]
                self.result["guest_parent"]["closeout_entry_count"] = len(names)
                if names:
                    self.result["errors"].append("guest fixture residue; device disposal is not fixture-finalizer success")
            except BaseException as error:
                self.uncertain = True
                self.result["errors"].append("guest parent closeout unproved: " + str(error)[:512])
        # Independent whole-new-device authority, not a path sweep or inherited
        # fixture deletion authority. Still attempt only this recorded device
        # after cancellation; it does not settle unknown host Gradle descendants.
        self.command("device-delete", ["/usr/bin/xcrun", "simctl", "delete", self.device["udid"]],
                     60, cleanup=True)
        remaining = self.devices("device-deletion-observed", cleanup=True)
        if any(row["udid"].lower() == self.device["udid"].lower() for _runtime, row in remaining):
            raise Hold("owned device still registered after its one deletion attempt")
        if os.path.lexists(self.device["data_path"]):
            raise Hold("owned device dataPath remains after API deletion; never sweep/retry")
        self.result["device_cleanup"] = "QUALIFIED_NEW_DEVICE_API_DELETED_REGISTRY_AND_DATA_PATH_ABSENT"
        self.result["shared_core_simulator_services"] = "NOT_SIGNALED_OR_CLAIMED_SETTLED_SHARED_CACHES_NOT_DELETED"

    def tree_tick(self):
        self.check_time()
        if self.uncertain or self.cancelled:
            raise Hold("uncertain/cancelled settlement forbids private generated-tree deletion")

    def remove_contents(self, parent, depth, budget):
        self.tree_tick()
        if depth > 32:
            raise Hold("private generated-tree depth bound")
        names = os.listdir(parent["fd"])
        budget[0] += len(names)
        if budget[0] > 200000:
            raise Hold("private generated-tree entry bound")
        for name in names:
            self.tree_tick()
            before = os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)
            if before.st_dev != self.root["identity"][0] or before.st_uid != os.geteuid():
                raise Hold("foreign private generated entry")
            if stat.S_ISDIR(before.st_mode):
                child = self.open_dir(name, parent)
                try:
                    if child["identity"] != identity(before):
                        raise Hold("private generated directory replaced")
                    self.remove_contents(child, depth + 1, budget)
                    self.check_dir(child)
                    self.tree_tick()
                    os.rmdir(name, dir_fd=parent["fd"])
                finally:
                    self.close(child)
            elif (stat.S_ISREG(before.st_mode) or stat.S_ISLNK(before.st_mode)) and before.st_nlink == 1:
                if file_pin(os.stat(name, dir_fd=parent["fd"], follow_symlinks=False)) != file_pin(before):
                    raise Hold("private generated leaf replaced")
                self.tree_tick()
                os.unlink(name, dir_fd=parent["fd"])
            else:
                raise Hold("unexpected private generated type/hard-link count")

    def cleanup(self):
        if self.root is None:
            if self.runtime_allocation_attempted:
                self.uncertain = True
                self.result["cleanup"] = "HOLD_PRIVATE_RUNTIME_ALLOCATION_OR_BINDING_UNCERTAIN_NO_DELETION"
            else:
                self.result["cleanup"] = "NO_PRIVATE_RUNTIME_ALLOCATED"
            return
        if self.uncertain or self.cancelled or not self.bound or self.stop_required:
            self.result["cleanup"] = "HOLD_PRIVATE_OUTPUTS_NOT_DELETED_UNCERTAIN_OR_INCOMPLETE_SETTLEMENT"
            return
        if self.device_create_attempted and self.result["device_cleanup"] != (
                "QUALIFIED_NEW_DEVICE_API_DELETED_REGISTRY_AND_DATA_PATH_ABSENT"):
            self.result["cleanup"] = "HOLD_PRIVATE_OUTPUTS_NOT_DELETED_DEVICE_CLOSEOUT_INCOMPLETE"
            return
        for item, path in self.anchors:
            self.check_dir(item)
            self.acl(path, ancestor=True)
        self.check_dir(self.root, private=True)
        self.acl(self.root_path)
        actual = set(os.listdir(self.root["fd"]))
        expected = set(self.children)
        if actual != expected and not (self.init_attempted and actual == expected | {"validation.init.gradle"}):
            raise Hold("private root top-level allowlist changed")
        self.save("cleanup-start.json", json.dumps({"root_identity": self.root["identity"],
                  "authority": "ORIGINAL_EXCLUSIVE_DISPOSABLE_REPLICA_PRIVATE_OUTPUTS_ONLY_NORMAL_SETTLEMENT"}).encode())
        budget = [0]
        self.remove_contents(self.root, 0, budget)
        self.check_dir(self.root, private=True)
        self.tree_tick()
        os.rmdir(self.root["name"], dir_fd=self.temp["fd"])
        try:
            os.stat(self.root["name"], dir_fd=self.temp["fd"], follow_symlinks=False)
        except FileNotFoundError:
            pass
        else:
            raise Hold("private runtime removal not observed; no second attempt")
        self.close(self.root)
        self.result["cleanup"] = "QUALIFIED_DISPOSABLE_REPLICA_PRIVATE_OUTPUTS_REMOVED_ORIGINAL_HANDLES"
        self.result["removed_generated_entries"] = budget[0]

    def finish(self):
        self.in_cleanup = True
        if self.stop_required and not self.stop_attempted and self.current_module is not None:
            self.stop_wrapper(self.current_module)
        try:
            self.settle_device()
        except BaseException as error:
            self.uncertain = True
            self.result["device_cleanup"] = "HOLD_OWNED_DEVICE_CLOSEOUT_ATTEMPT_CONSUMED_NO_RETRY"
            self.result["errors"].append(type(error).__name__ + ": " + str(error)[:512])
        try:
            self.cleanup()
        except BaseException as error:
            self.result["cleanup"] = "HOLD_PRIVATE_CLEANUP_ATTEMPT_CONSUMED_NO_RETRY"
            self.result["errors"].append(type(error).__name__ + ": " + str(error)[:512])
        anchors = {id(item) for item, _path in self.anchors}
        for item in reversed(self.handles):
            if item["fd"] is not None and item is not self.evidence and id(item) not in anchors:
                try:
                    self.close(item)
                except OSError as error:
                    self.result["errors"].append("original descriptor close failed; no retry: " + str(error)[:256])
        complete = (self.result["status"] == "TEN_CASE_EVIDENCE_CAPTURED_INDEPENDENT_NAME_RESULT_REVIEW_REQUIRED"
                    and self.result["cleanup"] == "QUALIFIED_DISPOSABLE_REPLICA_PRIVATE_OUTPUTS_REMOVED_ORIGINAL_HANDLES"
                    and not self.result["errors"] and not self.cancelled and not self.uncertain)
        self.result.update({"cancelled": self.cancelled, "settlement_uncertain": self.uncertain,
                            "all_case_name_scoring": "INDEPENDENT_REVIEW_REQUIRED_NO_AUTOMATIC_CLOSURE"})
        code = 0 if complete else 1
        if not complete:
            self.result["status"] = "FAIL_OR_HOLD_NO_AUTOMATIC_RETRY"
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
                self.result["errors"].append("final original descriptor close failed; no retry: " + str(error)[:256])
        if self.cancelled or self.uncertain:
            code = 1
        if code:
            self.result["status"] = "FAIL_OR_HOLD_NO_AUTOMATIC_RETRY"
        print(json.dumps({"exit": code, "status": self.result["status"], "cleanup": self.result["cleanup"],
                          "device_cleanup": self.result["device_cleanup"], "cancelled": self.cancelled,
                          "settlement_uncertain": self.uncertain, "errors": self.result["errors"]}, sort_keys=True))
        return code


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
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(number, signal.SIG_DFL)
    return 1 if cycle.cancelled or cycle.uncertain else code


if __name__ == "__main__":
    raise SystemExit(main())
