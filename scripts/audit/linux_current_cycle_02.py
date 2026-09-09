#!/usr/bin/python3
"""Fixed current-C4 Linux cycle02; this file is NOT execution admission.

Root alone: /usr/bin/python3 -I -B <absolute entry>, no arguments. Five serial
invocations, original per-invocation stop duties, one same-lock final cleanup.
No import/execution of any older helper. No retry/recovery/general-task mode.
Copied NEW reviewed primitives are identified in PLAN.md; changed orchestration
needs its own independent review and fresh exact-instance root admission.
Cooperative namespace/process coordination, not a hostile-UID sandbox.
"""

import collections
import contextlib
import csv
import fcntl
import hashlib
import io
import json
import os
from pathlib import Path
import re
import selectors
import signal
import stat
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

BASE = Path('/root/projects/PassVault')
W = BASE / 'passvault-linux'
REPOSITORY = BASE / 'passvault'
C = W / 'docs/audit-continuation/2026-09-08-linux/reviews/current-cycle02'
B = W / 'docs/audit-continuation/2026-09-08-linux/reviews/build-config/current-cycle01'
E = W / 'docs/audit-continuation/2026-09-08-linux/runs/linux-current-cycle02'
R = BASE / 'audit-runtime-linux-current-cycle02'
CHECKOUT = R / 'checkout'
SOURCE_OBJECTS = REPOSITORY / '.git/objects'
OWN_OBJECTS = CHECKOUT / '.git/objects'
LOCK = BASE / '.audit-coordination-linux-20260908/build.lock'
SELF = W / 'scripts/audit/linux_current_cycle_02.py'
PLAN = C / 'PLAN.md'
REQUEST = C / 'REQUEST.json'
SOURCE = W / 'docs/audit-continuation/2026-09-08-linux/reviews/current-cycle/SOURCE.json'
INIT = W / 'scripts/audit/current_cycle_tests_02.init.gradle'
CLASSES_FILE = B / 'CLASSES.tsv'
METHODS_FILE = B / 'METHODS.tsv'
COMMANDS_FILE = C / 'COMMANDS.md'
JOURNAL_PATH = E / 'JOURNAL.jsonl'
ACCEPTANCES = (C / 'ACCEPT-root.json', C / 'ACCEPT-editor_review.json')
REVIEWERS = ('/root', '/root/editor_review')
REVIEW_ROLES = ('OWNER_COAUTHOR_APPROVAL', 'INDEPENDENT_SOURCE_REVIEW')
RUNNER_AUTHORS = ['/root/storage', '/root']
COMMIT = 'da8ff89b9a8579017d5d524e628dff5251f2bb90'
TREE = 'cf0a8a702e7cd6948236be18b491bb5a21b5886e'
FROZEN = {
    str(SOURCE): '2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003',
    str(INIT): 'b5ddf961be4483d53a6dd3b633f41cbff3d84a708704b55d82eef6c89899e64c',
    str(CLASSES_FILE): '95e6ed4f8e5cbb92e4e6840a08e16613b14674555f7f99cd24e34b3ddcd18bda',
    str(METHODS_FILE): '10cb11cd6392e43ab7ec3e94e2f348ccd2caf6cd2127830fe9e1287ea10210be',
    str(COMMANDS_FILE): '9d8b57d645ea58d2f56e090cc426feacac5940da81fbaf564d238b40f798f858',
}
INPUT_FILES = (SELF, PLAN, SOURCE, INIT, CLASSES_FILE, METHODS_FILE, COMMANDS_FILE)
PHASES = ('ordinary', 'prepare', 'success', 'wrong-key', 'loader-io')
CONSUMERS = PHASES[2:]
FINDINGS = ['PVA-001', 'PVA-007', 'PVA-031', 'PVA-033', 'PVA-034', 'PVA-035', 'PVA-038']
COUNTS = (13, 6, 12, 17, 39, 7, 11, 9, 11, 13, 7, 11, 7, 4)
CLASS_IDS = tuple('D%02d' % i for i in range(1, 9)) + ('C01', 'C02', 'C03', 'S01', 'S02', 'P01')
FIXTURE_RE = r'pva038-v1:[0-9a-f]{104}:[0-9a-f]{48}:[0-9a-f]{104}:[0-9a-f]{48}'
JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
SDK = '/opt/android-sdk'
MIB = 1024 ** 2
GIB = 1024 ** 3
LIMITS = {
    'launch_free_bytes': 12 * GIB, 'running_free_bytes': 8 * GIB,
    'launch_memory_fraction': 0.25, 'running_memory_fraction': 0.20,
    'build_seconds': 3600, 'cold_seconds': 600, 'stop_seconds': 300,
    'git_seconds': 180, 'settlement_seconds': 120, 'total_seconds': 7200,
    'closeout_seconds': 900, 'source_scan_seconds': 180,
    'log_bytes_per_command': 4 * MIB, 'journal_bytes': 8 * MIB,
    'xml_bytes_per_file': 2 * MIB, 'xml_total_bytes': 32 * MIB,
    'evidence_bytes': 112 * MIB, 'evidence_files': 80,
    'runtime_logical_bytes': 6 * GIB, 'runtime_files': 200000,
    'runtime_directories': 30000, 'resource_poll_seconds': 5,
    'inventory_poll_seconds': 30,
    'files': 200000, 'directories': 30000, 'depth': 64, 'logical_bytes': 6 * GIB,
    'mountinfo_bytes': 2 * MIB, 'processes': 65536, 'poll_seconds': 5,
}
PRIVATE_DIRS = (
    'checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan',
    'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state', 'android-user', 'workers',
)
WORKERS = ('database', 'credential', 'shared', 'pva038-prepare', 'pva038-success',
           'pva038-wrong-key', 'pva038-loader-io')
WORKER_CHILDREN = ('home', 'jna', 'tmp', 'sqlite', 'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
GENERATED_ROOTS = (
    '.gradle', '.kotlin', 'build',
    'app-android/build', 'app-desktop/build', 'shared/build',
    'core/domain/build', 'core/database/build', 'core/crypto/build', 'core/security/build',
    'core/designsystem/build', 'core/navigation/build', 'core/otp/build', 'core/testing/build',
    'feature/onboarding/build', 'feature/unlock/build', 'feature/vault/build',
    'feature/credential/build', 'feature/generator/build', 'feature/health/build',
    'feature/settings/build', 'feature/backup/build',
)
TARGET_PATHS = tuple(R / p for p in PRIVATE_DIRS if p != 'checkout') + tuple(CHECKOUT / p for p in GENERATED_ROOTS)
PROTECTED_NAMES = {'.git', 'reports', 'test-results'}
ENV = {
    'PATH': JAVA_HOME + '/bin:/usr/bin:/bin', 'JAVA_HOME': JAVA_HOME,
    'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC',
    'HOME': str(R / 'home'), 'GRADLE_USER_HOME': str(R / 'gradle-home'),
    'KONAN_DATA_DIR': str(R / 'konan'), 'XDG_CACHE_HOME': str(R / 'xdg-cache'),
    'XDG_CONFIG_HOME': str(R / 'xdg-config'), 'XDG_DATA_HOME': str(R / 'xdg-data'),
    'XDG_STATE_HOME': str(R / 'xdg-state'), 'ANDROID_USER_HOME': str(R / 'android-user'),
    'ANDROID_HOME': SDK, 'ANDROID_SDK_ROOT': SDK,
    'TMPDIR': str(R / 'tmp'), 'TMP': str(R / 'tmp'), 'TEMP': str(R / 'tmp'),
    'SQLITE_TMPDIR': str(R / 'sqlite'),
    'JAVA_TOOL_OPTIONS': ' '.join((
        '-Xmx512m', '-XX:-UsePerfData', '-Dfile.encoding=UTF-8',
        '-Duser.home=' + str(R / 'home'), '-Djava.io.tmpdir=' + str(R / 'tmp'),
        '-Djna.tmpdir=' + str(R / 'jna'), '-Dorg.sqlite.tmpdir=' + str(R / 'sqlite'),
    )),
    'GIT_CONFIG_NOSYSTEM': '1', 'GIT_CONFIG_GLOBAL': '/dev/null',
    'GIT_TERMINAL_PROMPT': '0', 'GIT_OPTIONAL_LOCKS': '0',
}
GRADLE_FLAGS = [
    '--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel',
    '--no-configure-on-demand', '--no-configuration-cache', '--no-build-cache',
    '--dependency-verification=strict', '--stacktrace',
    '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
    '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8',
    '-Dorg.gradle.java.installations.auto-download=false',
    '-Dorg.gradle.java.installations.auto-detect=false',
    '-Dorg.gradle.java.installations.paths=' + JAVA_HOME,
]
COMMANDS = {
    mode: [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT), '-Ppassvault.audit.mode=' + mode]
    + ([':core:crypto:tasks', '--all', ':core:database:desktopTest', ':feature:credential:desktopTest',
        ':shared:desktopTest'] if mode == 'ordinary' else [':core:database:desktopTest', '--offline'])
    + GRADLE_FLAGS for mode in PHASES
}
STOP = [str(CHECKOUT / 'gradlew'), '--stop'] + GRADLE_FLAGS
GIT = [
    '/usr/bin/git', '--no-pager', '-c', 'core.hooksPath=/dev/null',
    '-c', 'core.fsmonitor=false', '-c', 'gc.auto=0', '-c', 'maintenance.auto=false',
    '-c', 'core.autocrlf=false', '-c', 'core.eol=lf',
]
SIGNALS = {signal.SIGINT, signal.SIGTERM, signal.SIGHUP}
BUILDLIKE = {
    'java', 'javaw', 'gradle', 'kotlinc', 'kotlin', 'xcodebuild', 'swift',
    'swift-frontend', 'clang', 'clang++', 'gcc', 'g++', 'cc1', 'cc1plus',
    'cmake', 'ninja', 'make', 'adb', 'emulator', 'qemu-system-x86',
}
CANCEL = set()
UID = os.getuid()

class Refused(Exception):
    pass


class ProcessCompleted(Refused):
    """Observed same-birth/UID zombie, not an arbitrary ownership refusal."""


def require(condition, message):
    if not condition:
        raise Refused(message)


def sha(data):
    return hashlib.sha256(data).hexdigest()


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True).encode() + b'\n'


def strict_json(data):
    def pairs(items):
        result = {}
        for key, value in items:
            require(key not in result, 'duplicate JSON key')
            result[key] = value
        return result
    return json.loads(data, object_pairs_hook=pairs,
                      parse_constant=lambda _: (_ for _ in ()).throw(Refused('nonfinite JSON')))


def identity(st, directory=False):
    names = ('st_dev', 'st_ino', 'st_uid', 'st_mode') if directory else (
        'st_dev', 'st_ino', 'st_uid', 'st_mode', 'st_nlink',
        'st_size', 'st_mtime_ns', 'st_ctime_ns')
    return {name[3:]: getattr(st, name) for name in names}


def clean_path(path):
    path = Path(path)
    require(path.is_absolute() and '..' not in path.parts, 'noncanonical absolute path')
    return path


# Closer primitive aliases; same refusal type and canonical absolute-path rule.
Hold = Refused
absolute = clean_path


def under(path, root):
    return path == root or path.startswith(root + '/')


class Directories:
    """Original parent/top FDs persist; removable descendants use stack FDs."""

    def __init__(self, tick):
        self.fds = {}
        self.pins = {}
        self.removed = set()
        self.tick = tick

    def open(self, path, bootstrap=False):
        path = absolute(path)
        key = str(path)
        require(key not in self.removed, 'removed original cannot be reopened/adopted')
        parent = None if path == Path('/') else self.open(path.parent, bootstrap)
        if key not in self.fds:
            require(bootstrap, 'unsealed directory')
            fd = os.open(path.name if parent is not None else '/',
                         os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                         dir_fd=parent)
            try:
                pin = identity(os.fstat(fd), True)
                require(stat.S_ISDIR(pin['mode']), 'directory type')
                self.fds[key], self.pins[key] = fd, pin
            except BaseException:
                # Unpublish only this still-local FD before its sole close attempt.
                if self.fds.get(key) == fd:
                    self.fds.pop(key)
                os.close(fd)
                raise
        fd = self.fds[key]
        require(identity(os.fstat(fd), True) == self.pins[key], 'original directory descriptor drift')
        if parent is not None:
            require(identity(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True)
                    == self.pins[key], 'original directory pathname replacement')
        return fd

    def bind(self, expected):
        for path in sorted(expected, key=lambda p: (len(Path(p).parts), p)):
            self.tick()
            self.open(path, True)
        require(self.pins == expected, 'exact original directory pin set/identity mismatch')

    def verify(self):
        for path in tuple(self.fds):
            self.tick()
            if path not in self.removed:
                self.open(path)

    def read(self, path, cap, keep=True, bootstrap=False, durable=False):
        self.tick()
        path = absolute(path)
        parent = self.open(path.parent, bootstrap)
        before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        require(stat.S_ISREG(before.st_mode) and before.st_nlink == 1 and before.st_uid == UID,
                'single-link owned regular input required')
        require(before.st_size <= cap and not before.st_mode & 0o022, 'input byte/mode bound')
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        try:
            require(identity(os.fstat(fd)) == identity(before), 'pre/open input drift')
            pieces, count, digest = [], 0, hashlib.sha256()
            while True:
                self.tick()
                piece = os.read(fd, min(65536, cap + 1 - count))
                if not piece:
                    break
                count += len(piece)
                require(count <= cap, 'input streaming byte cap')
                digest.update(piece)
                if keep:
                    pieces.append(piece)
            require(count == before.st_size, 'input short read')
            if durable:
                self.tick()
                os.fsync(fd)
                self.tick()
                os.fsync(parent)
                self.tick()
            require(identity(before) == identity(os.fstat(fd))
                    == identity(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'post-read input drift')
            self.open(path.parent)
            return b''.join(pieces) if keep else None, identity(before), digest.hexdigest()
        finally:
            os.close(fd)

    def new_file(self, path):
        path = absolute(path)
        parent = self.open(path.parent)
        fd = os.open(path.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                     0o600, dir_fd=parent)
        try:
            pin = identity(os.fstat(fd))
            require(pin['uid'] == UID and pin['nlink'] == 1 and pin['mode'] == stat.S_IFREG | 0o600,
                    'new owned private regular output')
            os.fsync(parent)
        except BaseException:
            os.close(fd)
            raise
        return fd

    def write_new(self, path, data):
        fd = self.new_file(path)
        try:
            view = memoryview(data)
            while view:
                written = os.write(fd, view)
                require(written > 0, 'short new-output write')
                view = view[written:]
            os.fsync(fd)
            pin = identity(os.fstat(fd))
            parent = self.open(Path(path).parent)
            os.fsync(parent)
            require(pin == identity(os.stat(Path(path).name, dir_fd=parent, follow_symlinks=False))
                    and pin['size'] == len(data), 'new-output completed identity/length')
        finally:
            os.close(fd)
        return {'sha256': sha(data), 'identity': pin}

    def mkdir(self, path, journal):
        path = absolute(path)
        parent = self.open(path.parent)
        journal.event('allocation_intent', path=str(path), parent=self.pins[str(path.parent)])
        os.mkdir(path.name, 0o700, dir_fd=parent)
        os.fsync(parent)
        self.open(path, True)
        require(self.pins[str(path)]['uid'] == UID and self.pins[str(path)]['mode'] == stat.S_IFDIR | 0o700,
                'new private directory ownership/mode')
        journal.event('allocation_original', path=str(path), identity=self.pins[str(path)])

    def close(self):
        failures = []
        for fd in reversed(tuple(self.fds.values())):
            try:
                os.close(fd)
            except OSError:
                failures.append('directory descriptor close failure')
        return failures


class Journal:
    def __init__(self, dirs):
        self.dirs = dirs
        self.fd = None
        self.sequence = self.used = 0
        self.ok = True
        self.errors = []
        parent = dirs.open(E)
        fd = os.open(JOURNAL_PATH.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL
                     | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=parent)
        try:
            self.original = identity(os.fstat(fd))
            require(self.original['uid'] == UID and self.original['nlink'] == 1
                    and self.original['mode'] == stat.S_IFREG | 0o600, 'new private journal identity')
            # Any failure here still leaves the name consumed. Never unlink/retry it.
            os.fsync(fd)
            os.fsync(parent)
            require(identity(os.fstat(fd)) == self.original
                    == identity(os.stat(JOURNAL_PATH.name, dir_fd=parent, follow_symlinks=False))
                    and self.original['size'] == 0, 'initial closeout journal changed')
            self.last = self.original
            self.fd = fd
        except BaseException:
            os.close(fd)
            raise

    def event(self, kind, **values):
        require(self.ok, 'closeout journal already incomplete')
        try:
            parent = self.dirs.open(E)
            current = identity(os.fstat(self.fd))
            require(current == self.last and current['size'] == self.used,
                    'closeout journal prior completed pin/length drift')
            require(identity(os.stat(JOURNAL_PATH.name, dir_fd=parent, follow_symlinks=False)) == current,
                    'closeout journal pathname drift')
            data = canonical({'sequence': self.sequence, 'time_ns': time.time_ns(), 'kind': kind, **values})
            require(self.used + len(data) <= LIMITS['journal_bytes'], 'closeout journal cap')
            view = memoryview(data)
            while view:
                written = os.write(self.fd, view)
                require(written > 0, 'short journal write')
                view = view[written:]
            os.fsync(self.fd)
            after = identity(os.fstat(self.fd))
            require(after == identity(os.stat(JOURNAL_PATH.name, dir_fd=parent, follow_symlinks=False))
                    and after['size'] == self.used + len(data)
                    and all(after[k] == self.original[k] for k in ('dev', 'ino', 'uid', 'mode', 'nlink')),
                    'journal postappend descriptor/name/length drift')
            # Only completed durability plus final name/descriptor proof advances this state.
            self.last = after
            self.used += len(data)
            self.sequence += 1
        except BaseException:
            self.ok = False
            raise


    def error(self, message):
        if message not in self.errors:
            self.errors.append(message)
            if self.ok:
                self.event('error', reason=message)

    def latch_cancel(self):
        if CANCEL:
            self.error('cancellation observed: ' + ','.join(map(str, sorted(CANCEL))))


def resource_sample(launch=False):
    disks = {str(p): os.statvfs(p).f_bavail * os.statvfs(p).f_frsize for p in (BASE, E)}
    memory = {}
    with open('/proc/meminfo', encoding='ascii') as stream:
        for line in stream:
            key, value = line.split(':', 1)
            if key in ('MemTotal', 'MemAvailable'):
                memory[key] = int(value.strip().split()[0]) * 1024
    fraction = memory['MemAvailable'] / memory['MemTotal']
    prefix = 'launch' if launch else 'running'
    require(min(disks.values()) >= LIMITS[prefix + '_free_bytes'], 'disk floor')
    require(fraction >= LIMITS[prefix + '_memory_fraction'], 'memory floor')
    return {'disks_available': disks, 'memory': memory, 'available_fraction': fraction}


def inside_runtime(path):
    return path == str(R) or path.startswith(str(R) + '/')


def process_row(pid):
    root = Path('/proc') / str(pid)
    raw = (root / 'stat').read_text(encoding='utf-8')
    tail = raw[raw.rfind(')') + 2:].split()
    comm = raw[raw.find('(') + 1:raw.rfind(')')]
    first = (int(tail[19]), int(tail[1]), os.stat(root).st_uid)
    cwd = '' if tail[0] == 'Z' else os.readlink(root / 'cwd')
    raw2 = (root / 'stat').read_text(encoding='utf-8')
    tail2 = raw2[raw2.rfind(')') + 2:].split()
    second = (int(tail2[19]), int(tail2[1]), os.stat(root).st_uid)
    cwd2 = '' if tail2[0] == 'Z' else os.readlink(root / 'cwd')
    return {'pid': pid, 'start': first[0], 'ppid': first[1], 'uid': first[2],
            'inside': inside_runtime(cwd), 'buildlike': comm in BUILDLIKE,
            'comm': comm, 'stable': first == second and cwd == cwd2,
            'birth_stable': first[0] == second[0] and first[2] == second[2],
            'zombie': tail2[0] == 'Z'}


class Processes:
    def __init__(self, journal):
        self.j = journal
        self.owned = {}
        self.pidfds = {}
        self.children = {}
        self.unknown = set()

    def admit(self, row, anchor):
        key = (row['pid'], row['start'])
        if key in self.owned:
            return
        require(row['uid'] == UID and row['inside'] and row['stable'], 'owned process original identity/cwd')
        parent_key = None
        fd = None
        try:
            if isinstance(anchor, dict):
                parent_key = (anchor['live_parent_pid'], anchor['start'])
                require(parent_key in self.owned, 'parent birth has no original ownership')
                parent = process_row(parent_key[0])
                require((parent['pid'], parent['start']) == parent_key and parent['uid'] == UID
                        and parent['inside'] and parent['stable'] and not parent['zombie'],
                        'original live parent changed before child capture')
            fresh = process_row(row['pid'])
            require((fresh['pid'], fresh['start']) == key and fresh['uid'] == UID and fresh['birth_stable'],
                    'fresh child birth/uid changed')
            if fresh['zombie']:
                raise ProcessCompleted('same-birth child completed before ownership capture')
            require(fresh['inside'] and fresh['stable'], 'fresh child live cwd changed')
            if parent_key is not None:
                require(fresh['ppid'] == parent_key[0], 'child parent changed before pidfd acquisition')
            if hasattr(os, 'pidfd_open'):
                fd = os.pidfd_open(row['pid'], 0)
            last = process_row(row['pid'])
            require((last['pid'], last['start']) == key and last['uid'] == UID and last['birth_stable'],
                    'pidfd acquisition child identity change')
            if last['zombie']:
                raise ProcessCompleted('same-birth child completed during ownership capture')
            require(last['inside'] and last['stable'], 'pidfd acquisition live cwd change')
            if parent_key is not None:
                require(last['ppid'] == parent_key[0], 'child parent changed during admission')
                parent = process_row(parent_key[0])
                require((parent['pid'], parent['start']) == parent_key and parent['uid'] == UID
                        and parent['inside'] and parent['stable'] and not parent['zombie'],
                        'original live parent changed after child capture')
        except BaseException:
            if fd is not None:
                os.close(fd)
            raise
        if fd is not None:
            self.pidfds[key] = fd
        self.owned[key] = last
        command = (Path('/proc') / str(row['pid']) / 'cmdline').read_bytes()
        require(len(command) <= 65536, 'owned command evidence cap')
        self.j.event('owned_process', pid=row['pid'], start=row['start'], ppid=row['ppid'],
                     anchor=anchor, inside_runtime=True,
                     argv=[v.decode('utf-8', 'replace') for v in command.split(b'\0') if v])

    def direct(self, child):
        self.children[child.pid] = child
        try:
            row = process_row(child.pid)
            if row['zombie']:
                require(row['uid'] == UID and row['birth_stable'], 'direct zombie birth/uid unstable')
                require(child.poll() is not None, 'direct zombie exit not observable')
                self.j.event('direct_child_already_exited', pid=child.pid, start=row['start'], exit=child.returncode)
            else:
                self.admit(row, 'unreaped direct Popen child')
        except (FileNotFoundError, ProcessLookupError, ProcessCompleted):
            require(child.poll() is not None, 'direct child birth not observable')
            self.j.event('direct_child_already_exited', pid=child.pid, exit=child.returncode)

    def scan(self):
        unknown = set()
        completed_direct = set()
        snapshots = []
        for _ in range(2):
            rows = {}
            ids = {int(p) for p in os.listdir('/proc') if p.isdigit()}
            for pid in ids:
                if pid == os.getpid():
                    continue
                try:
                    row = process_row(pid)
                    rows[pid] = row
                except (FileNotFoundError, ProcessLookupError):
                    # A vanished positively owned child is settled only by subsequent sampling/wait.
                    child = self.children.get(pid)
                    if child is None or child.returncode is not None or child.poll() is None:
                        unknown.add(('unclassified_vanished', pid))
                except (PermissionError, ValueError, IndexError):
                    unknown.add(('unclassifiable_process', pid))
            snapshots.append(rows)
        before, rows = snapshots
        for pid in before.keys() | rows.keys():
            old, new = before.get(pid), rows.get(pid)
            relevant = any(row and (row['buildlike'] or row['inside'] or
                                   (row['pid'], row['start']) in self.owned) for row in (old, new))
            if not relevant:
                continue
            if old is not None and new is not None:
                if old['start'] != new['start'] or old['uid'] != new['uid']:
                    unknown.add(('between_sample_birth_identity_change', pid))
                elif not old['zombie'] and not new['zombie'] and (
                        old['inside'] != new['inside'] or old['ppid'] != new['ppid']):
                    unknown.add(('between_sample_relevance_cwd_parent_churn', pid))
            elif old is not None and (old['pid'], old['start']) not in self.owned:
                unknown.add(('unowned_relevant_process_vanished', pid))
        for row in rows.values():
            child = self.children.get(row['pid'])
            if child is not None and child.returncode is None and not row['zombie']:
                try:
                    self.admit(row, 'unreaped direct Popen child')
                except ProcessCompleted:
                    if child.poll() is None:
                        unknown.add(('direct_completion_unobserved', row['pid']))
                    else:
                        self.j.event('direct_child_already_exited', pid=child.pid, exit=child.returncode)
                        completed_direct.add((row['pid'], row['start']))
                except (OSError, Refused):
                    unknown.add(('unsettled_direct_child_birth', row['pid']))
        changed = True
        while changed:
            changed = False
            for row in rows.values():
                key = (row['pid'], row['start'])
                parent = rows.get(row['ppid'])
                if key not in self.owned and parent is not None:
                    parent_key = (parent['pid'], parent['start'])
                    if parent_key in self.owned and row['start'] >= parent['start']:
                        try:
                            self.admit(row, {'live_parent_pid': parent['pid'], 'start': parent['start']})
                            changed = True
                        except (OSError, Refused):
                            unknown.add(('unsettled_descendant_birth', row['pid']))
        live = []
        for row in rows.values():
            key = (row['pid'], row['start'])
            if key in completed_direct:
                continue
            if key in self.owned:
                if not row['birth_stable'] or row['uid'] != UID or (
                        not row['zombie'] and (not row['stable'] or not row['inside'])):
                    unknown.add(('owned_identity_or_cwd_churn', row['pid']))
                if not row['zombie']:
                    live.append(row)
            elif not row['zombie'] and (row['buildlike'] or row['inside']):
                unknown.add(('other_buildlike_or_unanchored_runtime_birth', row['pid']))
        for reason, pid in sorted(unknown - self.unknown):
            self.j.event('process_unknown', pid=pid, reason=reason)
        self.unknown |= unknown
        if unknown:
            self.j.error('process exclusivity/ownership unknown; HOLD')
        return live

    def signal_owned(self, row, sig):
        key = (row['pid'], row['start'])
        require(key in self.owned, 'no signal authority')
        try:
            current = process_row(row['pid'])
        except (FileNotFoundError, ProcessLookupError):
            return
        require((current['pid'], current['start']) == key and current['uid'] == UID and current['birth_stable'],
                'fresh signal birth/uid failed')
        if current['zombie']:
            self.j.event('owned_zombie_no_signal', pid=key[0], start=key[1])
            return
        require(current['inside'] and current['stable'], 'fresh signal live cwd failed')
        self.j.event('owned_signal_intent', pid=key[0], start=key[1], signal=sig)
        if key in self.pidfds and hasattr(signal, 'pidfd_send_signal'):
            signal.pidfd_send_signal(self.pidfds[key], sig)
        else:
            # Narrow fallback: an unreaped direct child cannot have its PID reused.
            child = self.children.get(key[0])
            require(child is not None and child.returncode is None, 'no pidfd/unreaped-direct signal authority')
            child.send_signal(sig)

    def terminate_owned(self, sig):
        for row in self.scan():
            try:
                self.signal_owned(row, sig)
            except (OSError, Refused) as error:
                self.j.error('owned signal incomplete: ' + type(error).__name__)

    def settle(self):
        deadline = time.monotonic() + LIMITS['settlement_seconds']
        sent_term = sent_kill = False
        empty_samples = 0
        while time.monotonic() < deadline:
            for child in self.children.values():
                child.poll()
            live = self.scan()
            if not live:
                empty_samples += 1
                if empty_samples == 3:
                    return not self.unknown
            else:
                empty_samples = 0
                if not sent_term:
                    self.terminate_owned(signal.SIGTERM)
                    sent_term = True
                elif not sent_kill and time.monotonic() > deadline - LIMITS['settlement_seconds'] + 10:
                    self.terminate_owned(signal.SIGKILL)
                    sent_kill = True
            time.sleep(1)
        self.j.error('owned settlement incomplete; original runtime retained')
        return False

    def close(self):
        for fd in self.pidfds.values():
            os.close(fd)


class Run:
    def __init__(self, dirs, journal, processes, environment, started):
        self.d = dirs
        self.j = journal
        self.p = processes
        self.started = started
        self.environment = dict(environment)
        self.stop_required = False
        self.stop_attempted = False
        self.stop_ok = False
        self.build_ok = False
        self.source_ok = False
        self.xml_ok = False
        self.settled = False
        self.last_resource = self.last_inventory = 0
        self.commands = []
        self.retained_evidence = []

    def inventory(self):
        count = directories = logical = 0
        for parent, subdirs, names in os.walk(R, followlinks=False):
            directories += 1
            require(directories <= LIMITS['runtime_directories'], 'runtime directory cap')
            for name in subdirs + names:
                path = Path(parent) / name
                st = path.lstat()
                require(stat.S_ISREG(st.st_mode) or stat.S_ISDIR(st.st_mode), 'runtime special/symlink member')
                require(st.st_uid == UID, 'runtime ownership changed')
                if stat.S_ISREG(st.st_mode):
                    count += 1
                    logical += st.st_size
                    require(count <= LIMITS['runtime_files'] and logical <= LIMITS['runtime_logical_bytes'],
                            'runtime file/byte cap')
        result = {'files': count, 'directories': directories, 'logical_bytes': logical}
        self.j.event('runtime_inventory', **result)
        return result

    def monitor(self, cleanup=False):
        self.j.latch_cancel()
        self.p.scan()
        now = time.monotonic()
        if now - self.last_resource >= LIMITS['resource_poll_seconds']:
            try:
                self.j.event('resources', **resource_sample())
            except (OSError, Refused) as error:
                self.j.error('running resource failure: ' + str(error))
            self.last_resource = now
        if now - self.last_inventory >= LIMITS['inventory_poll_seconds']:
            try:
                self.inventory()
            except (OSError, Refused) as error:
                self.j.error('runtime inventory failure: ' + str(error))
            self.last_inventory = now
        if now - self.started >= LIMITS['total_seconds']:
            self.j.error('total run time bound')
        return bool(self.j.errors) and not cleanup

    def command(self, label, argv, budget, cleanup=False, gradle=False, observational=False):
        record = {'label': label, 'argv': argv, 'cwd': str(R / 'checkout'),
                  'started': False, 'exit': None, 'complete': False, 'errors': []}
        self.commands.append(record)
        output = bytearray()
        try:
            with contextlib.ExitStack() as acquired:
                selector = acquired.enter_context(selectors.DefaultSelector())
                log_fd = self.d.new_file(E / (label + '.log'))
                acquired.callback(os.close, log_fd)
                acquired.callback(os.fsync, log_fd)
                return self.collect_command(label, argv, budget, cleanup, gradle, observational,
                                            record, output, log_fd, selector, acquired)
        except BaseException as error:
            record['errors'].append('startup/log persistence: ' + type(error).__name__)
            self.j.error(label + ' startup/log persistence incomplete: ' + type(error).__name__)
        finally:
            record['log_bytes'] = len(output)
            record['log_sha256'] = sha(output)
            self.j.event('command_result', **record)
        return bytes(output), record

    def collect_command(self, label, argv, budget, cleanup, gradle, observational,
                        record, output, log_fd, selector, acquired):
        child = None
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            if not cleanup and not observational:
                self.j.latch_cancel()
                require(not CANCEL and not signal.sigpending() & SIGNALS and not self.j.errors,
                        'cancel/error before launch intent')
            if gradle:
                self.stop_required = True
            if cleanup:
                require(not self.stop_attempted, 'stop already attempted; no retry')
                self.stop_attempted = True
            self.j.event('launch_intent', **record, stop_required=self.stop_required,
                         environment_sha256=sha(canonical(self.environment)), budget_seconds=budget)
            if not cleanup and not observational and signal.sigpending() & SIGNALS:
                CANCEL.update(signal.sigpending() & SIGNALS)
                self.j.latch_cancel()
                raise Refused('cancel before launch commitment; original stop obligation retained')
            # This is the launch commitment. Later concurrent cancellation aborts owned work;
            # an ambiguous Popen outcome retains the durable original stop obligation.
            child = subprocess.Popen(argv, cwd=R / 'checkout', env=self.environment, stdin=subprocess.DEVNULL,
                                     stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                     start_new_session=True, close_fds=True,
                                     preexec_fn=lambda: signal.pthread_sigmask(signal.SIG_SETMASK, old_mask))
            self.p.children[child.pid] = child
            acquired.callback(child.stdout.close)
            record['started'] = True
            record['pid'] = child.pid
            self.j.event('launch_observed', label=label, pid=child.pid)
        except BaseException as error:
            record['errors'].append('launch: ' + type(error).__name__)
            self.j.error(label + ' launch failed/ambiguous: ' + type(error).__name__)
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)
        start = time.monotonic()
        abort_at = exit_at = None
        kill_sent = False
        eof = False
        try:
            if child is None:
                return bytes(output), record
            self.p.direct(child)
            os.set_blocking(child.stdout.fileno(), False)
            selector.register(child.stdout, selectors.EVENT_READ)
            while True:
                for key, _ in selector.select(0.25):
                    piece = os.read(key.fileobj.fileno(), 65536)
                    if not piece:
                        selector.unregister(key.fileobj)
                        eof = True
                        continue
                    remaining = LIMITS['log_bytes_per_command'] - len(output)
                    retained = piece[:remaining]
                    output.extend(retained)
                    view = memoryview(retained)
                    while view:
                        written = os.write(log_fd, view)
                        require(written > 0, 'short command-log write')
                        view = view[written:]
                    if len(piece) > remaining:
                        record['errors'].append('log cap')
                        self.j.error(label + ' log cap; excess discarded')
                now = time.monotonic()
                aborted = self.monitor(cleanup or observational)
                if now - start >= budget:
                    record['errors'].append('timeout')
                    self.j.error(label + ' time bound')
                if aborted or record['errors']:
                    if abort_at is None:
                        abort_at = now
                        self.p.terminate_owned(signal.SIGTERM)
                    elif now - abort_at > 10 and not kill_sent:
                        self.p.terminate_owned(signal.SIGKILL)
                        kill_sent = True
                code = child.poll()
                if code is not None:
                    record['exit'] = code
                    if eof:
                        record['complete'] = True
                        break
                    if exit_at is None:
                        exit_at = now
                    elif now - exit_at > 10:
                        record['errors'].append('pipe held after direct exit')
                        self.j.error(label + ' output pipe settlement unknown')
                        break
                if abort_at is not None and now - abort_at > 20:
                    record['errors'].append('child abort settlement pending')
                    break
            if record['exit'] != 0:
                self.j.error(label + ' exit nonzero/unobserved')
        except BaseException as error:
            record['errors'].append(type(error).__name__)
            self.j.error(label + ' collection incomplete: ' + type(error).__name__)
        return bytes(output), record

    def git(self, label, extra):
        output, record = self.command(label, GIT + extra, LIMITS['git_seconds'],
                                      observational=label in ('identity-after', 'members-after'))
        require(record['complete'] and record['exit'] == 0 and not record['errors'], 'Git command failed')
        return output

    def check_source(self, source, discover=False):
        checkout = R / 'checkout'
        # Git outputs are bounded and journaled; no filesystem mutation command is used here.
        phase = 'before' if discover else 'after'
        output = self.git('identity-' + phase, ['-C', str(checkout), 'rev-parse', 'HEAD', 'HEAD^{tree}'])
        require(output.decode().splitlines() == [COMMIT, TREE], 'checkout commit/tree')
        output = self.git('members-' + phase, ['-C', str(checkout), 'ls-tree', '-r', '-z', 'HEAD'])
        actual = {}
        for item in output.split(b'\0'):
            if item:
                metadata, path = item.split(b'\t', 1)
                mode, kind, oid = metadata.decode().split()
                require(kind == 'blob' and mode in ('100644', '100755'), 'nonregular tracked Git member')
                actual[path.decode('utf-8')] = {'git_mode': mode, 'git_blob': oid}
        require(set(actual) == {row['path'] for row in source['files']}, 'tracked member set changed')
        scan_started = time.monotonic()
        for row in source['files']:
            require(time.monotonic() - scan_started <= LIMITS['source_scan_seconds'], 'checkout source scan time bound')
            path = Path(row['path'])
            require(not path.is_absolute() and '..' not in path.parts, 'source path escape')
            require(actual[row['path']] == {k: row[k] for k in ('git_mode', 'git_blob')}, 'tracked blob/mode')
            data, meta, _ = self.d.read(checkout / path, 32 * MIB, bootstrap=discover)
            require(sha(data) == row['checkout_sha256'] and len(data) == row['checkout_size'],
                    'actual checkout source hash/size mismatch: ' + row['path'])
            require(bool(meta['mode'] & 0o111) == (row['git_mode'] == '100755'), 'source executable mode')
        self.j.event('source_bound', phase=phase, files=len(actual), commit=COMMIT, tree=TREE,
                     checkout_eol_qualifications=source['checkout_eol_qualifications'])


def proc_read(path, cap):
    with open(path, 'rb') as stream:
        data = stream.read(cap + 1)
    require(len(data) <= cap, 'proc input cap')
    return data


def cleanup_process_row(pid):
    root = Path('/proc') / str(pid)
    first = proc_read(root / 'stat', 4096).decode('utf-8')
    tail = first[first.rfind(')') + 2:].split()
    comm = first[first.find('(') + 1:first.rfind(')')]
    one = (int(tail[19]), int(tail[1]), os.stat(root).st_uid)
    cwd = '' if tail[0] == 'Z' else os.readlink(root / 'cwd')
    second = proc_read(root / 'stat', 4096).decode('utf-8')
    tail2 = second[second.rfind(')') + 2:].split()
    two = (int(tail2[19]), int(tail2[1]), os.stat(root).st_uid)
    cwd2 = '' if tail2[0] == 'Z' else os.readlink(root / 'cwd')
    return {'pid': pid, 'start': one[0], 'ppid': one[1], 'uid': one[2],
            'stable': one == two and cwd == cwd2 and tail[0] == tail2[0],
            'inside': under(cwd, str(R)), 'buildlike': comm in BUILDLIKE, 'zombie': tail2[0] == 'Z'}


def process_sample(owned):
    snapshots, uncertain, live = [], set(), set()
    for _ in range(2):
        ids = {int(name) for name in os.listdir('/proc') if name.isdigit()}
        require(len(ids) <= LIMITS['processes'], 'process count cap')
        rows = {}
        for pid in sorted(ids - {os.getpid()}):
            try:
                rows[pid] = cleanup_process_row(pid)
            except (OSError, ValueError, IndexError, UnicodeError):
                uncertain.add((pid, 'unclassifiable_or_vanished'))
        snapshots.append(rows)
    before, after = snapshots
    owned_pids = {pid for pid, _ in owned}
    for pid in before.keys() | after.keys():
        old, new = before.get(pid), after.get(pid)
        relevant = pid in owned_pids or any(r and (r['inside'] or r['buildlike']) for r in (old, new))
        if not relevant:
            continue
        if old is None or new is None or old != new or not new['stable']:
            uncertain.add((pid, 'relevant_birth_parent_cwd_or_visibility_change'))
        elif pid in owned_pids and (pid, new['start']) not in owned:
            uncertain.add((pid, 'historical_owned_pid_reused_no_adoption'))
        elif not new['zombie']:
            live.add((pid, new['start']))
    require(len(uncertain) + len(live) <= 128, 'process residual evidence cap')
    return {'observed_pid_counts': [len(r) for r in snapshots], 'owned_births_considered': len(owned),
            'relevant_live_births': sorted(live), 'uncertainties': sorted(uncertain)}


def mount_sample():
    raw = proc_read('/proc/self/mountinfo', LIMITS['mountinfo_bytes']).decode('utf-8')
    relevant = []
    for line in raw.splitlines():
        fields = line.split()
        require(len(fields) >= 10 and '-' in fields, 'unparseable mountinfo')
        path = re.sub(r'\\([0-7]{3})', lambda m: chr(int(m.group(1), 8)), fields[4])
        if any(under(path, str(target)) for target in TARGET_PATHS):
            relevant.append({'mount_id': fields[0], 'device': fields[2], 'mountpoint': path})
    require(len(relevant) <= 64, 'mount residual evidence cap')
    return relevant


class Guard:
    def __init__(self, dirs, started, lock_fd, lock_pin, owned, journal):
        self.d, self.started = dirs, started
        self.lock_fd, self.lock_pin, self.owned, self.j = lock_fd, lock_pin, owned, journal
        self.last = 0.0

    def cheap(self):
        require(not CANCEL and not signal.sigpending() & SIGNALS, 'cancellation observed')
        require(time.monotonic() - self.started <= LIMITS['closeout_seconds'], 'closeout time cap')
        parent = self.d.open(LOCK.parent)
        require(identity(os.fstat(self.lock_fd)) == self.lock_pin
                == identity(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)), 'original lock drift')
        require(not CANCEL and not signal.sigpending() & SIGNALS, 'cancellation observed after lock check')

    def resources(self, launch=False):
        disks = {}
        for path in (BASE, E):
            fs = os.fstatvfs(self.d.open(path))
            disks[str(path)] = fs.f_bavail * fs.f_frsize
        memory = {}
        for line in proc_read('/proc/meminfo', 65536).decode('ascii').splitlines():
            key, value = line.split(':', 1)
            if key in ('MemTotal', 'MemAvailable'):
                memory[key] = int(value.strip().split()[0]) * 1024
        fraction = memory['MemAvailable'] / memory['MemTotal']
        result = {'available_disk_bytes': disks, 'memory_bytes': memory, 'available_memory_fraction': fraction}
        self.j.event('resources', **result)
        prefix = 'launch' if launch else 'running'
        require(min(disks.values()) >= LIMITS[prefix + '_free_bytes'], 'closeout disk floor')
        require(fraction >= LIMITS[prefix + '_memory_fraction'], 'closeout memory floor')
        return result

    def tick(self, force=False, launch=False):
        self.cheap()
        if force or time.monotonic() - self.last >= LIMITS['poll_seconds']:
            # Disable recursive polling while reading only proc/mount/resource metadata.
            self.resources(launch)
            mounts = mount_sample()
            self.j.event('filtered_mount_check', mounted_targets=mounts)
            require(not mounts, 'mount at/below disposable target')
            sampled = process_sample(self.owned)
            self.j.event('fresh_process_settlement', **sampled)
            require(not sampled['relevant_live_births'] and not sampled['uncertainties'],
                    'fresh process/coordination settlement unproved')
            self.last = time.monotonic()
            self.cheap()


class Node:
    __slots__ = ('name', 'pin', 'directory', 'protected', 'retained', 'children', 'allocated')

    def __init__(self, name, st, protected):
        self.name, self.directory, self.protected = name, stat.S_ISDIR(st.st_mode), protected
        self.pin = identity(st, self.directory)
        self.retained, self.children = protected, []
        self.allocated = st.st_blocks * 512


class Forest:
    """Snapshots authorize only observed descendants under original allocated tops."""

    def __init__(self, dirs, guard, policy):
        self.d, self.g, self.policy = dirs, guard, policy
        self.files = self.directories = self.logical = 0
        self.snapshots = []
        self.observed_removed = {'files': 0, 'directories': 0, 'logical_bytes': 0, 'allocated_bytes': 0}

    def check_type(self, st):
        directory = stat.S_ISDIR(st.st_mode)
        require(directory or stat.S_ISREG(st.st_mode), 'symlink/special/unknown descendant type')
        require(st.st_uid == UID and not st.st_mode & (stat.S_ISUID | stat.S_ISGID), 'descendant uid/set-id refusal')
        require(st.st_dev == self.policy['directory_dev' if directory else 'regular_file_dev'], 'unadmitted descendant device')
        require(directory or st.st_nlink == 1, 'hardlinked descendant refusal')

    def chain(self, target, stack):
        self.g.tick()
        parent = self.d.open(target.parent)
        for node, fd in stack:
            require(identity(os.fstat(fd), True) == node.pin
                    == identity(os.stat(node.name, dir_fd=parent, follow_symlinks=False), True),
                    'captured directory chain replaced')
            parent = fd

    def scan(self, target, parent, name, protected, depth, stack):
        self.g.tick()
        require(depth <= LIMITS['depth'], 'descendant depth cap')
        st = os.stat(name, dir_fd=parent, follow_symlinks=False)
        self.check_type(st)
        node = Node(name, st, protected or name in PROTECTED_NAMES)
        if not node.directory:
            self.files += 1
            self.logical += st.st_size
            require(self.files <= LIMITS['files'] and self.logical <= LIMITS['logical_bytes'], 'forest file/logical cap')
            # No output contents are read for deletion. This pins descriptor and current name.
            fd = os.open(name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
            try:
                require(identity(os.fstat(fd)) == node.pin
                        == identity(os.stat(name, dir_fd=parent, follow_symlinks=False)), 'leaf changed during preflight')
            finally:
                os.close(fd)
            return node
        self.directories += 1
        require(self.directories <= LIMITS['directories'], 'forest directory cap')
        fd = os.open(name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        try:
            current = stack + [(node, fd)]
            self.chain(target, current)
            with os.scandir(fd) as entries:
                names = []
                for entry in entries:
                    require(len(names) < LIMITS['files'] + LIMITS['directories'], 'directory entry cap')
                    require(entry.name not in ('.', '..') and '/' not in entry.name, 'invalid descendant name')
                    names.append(entry.name)
            require(len(names) == len(set(names)), 'repeated directory entry')
            for child in sorted(names):
                node.children.append(self.scan(target, fd, child, node.protected, depth + 1, current))
            require(set(os.listdir(fd)) == set(names), 'directory membership changed during preflight')
            self.chain(target, current)
            node.retained = node.protected or any(child.retained for child in node.children)
            return node
        finally:
            os.close(fd)

    def summarize(self, node):
        digest = hashlib.sha256()
        counts = {'files': 0, 'directories': 0, 'logical_bytes': 0, 'allocated_bytes': 0,
                  'retained_files': 0, 'retained_directories': 0, 'retained_logical_bytes': 0,
                  'retained_allocated_bytes': 0, 'protected_report_roots': 0}

        def visit(member, rel):
            self.g.tick()
            digest.update(canonical({'path': rel, 'identity': member.pin, 'retained': member.retained,
                                     'protected': member.protected, 'allocated_bytes': member.allocated}))
            kind = 'directories' if member.directory else 'files'
            counts[kind] += 1
            counts['allocated_bytes'] += member.allocated
            if not member.directory:
                counts['logical_bytes'] += member.pin['size']
            if member.retained:
                counts['retained_' + kind] += 1
                counts['retained_allocated_bytes'] += member.allocated
                if not member.directory:
                    counts['retained_logical_bytes'] += member.pin['size']
            if member.directory and member.name in ('reports', 'test-results'):
                counts['protected_report_roots'] += 1
            for child in member.children:
                visit(child, rel + '/' + child.name)

        visit(node, '.')
        return {'snapshot_sha256': digest.hexdigest(), **counts,
                'snapshot_provenance': 'POST_RUN_DESCENDANT_OBSERVATION_UNDER_ORIGINAL_TOP_NOT_CREATION_PROOF'}

    def preflight(self):
        for path in TARGET_PATHS:
            self.d.open(path)
            node = self.scan(path, self.d.open(path.parent), path.name, False, 0, [])
            require(node.pin == self.d.pins[str(path)], 'top differs from original allocation')
            self.snapshots.append((path, node, self.summarize(node)))
        return {'targets': len(self.snapshots), 'files': self.files, 'directories': self.directories,
                'logical_bytes': self.logical}

    def erase(self, target, node, parent, stack):
        self.g.tick()
        if not node.directory:
            self.chain(target, stack)
            fd = os.open(node.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
            try:
                require(identity(os.fstat(fd)) == node.pin
                        == identity(os.stat(node.name, dir_fd=parent, follow_symlinks=False)), 'leaf changed since preflight')
                if node.retained:
                    return
                self.chain(target, stack)
                require(identity(os.stat(node.name, dir_fd=parent, follow_symlinks=False)) == node.pin,
                        'last leaf pathname check failed')
                self.destroy(parent, node, fd)
                self.observed_removed['files'] += 1
                self.observed_removed['logical_bytes'] += node.pin['size']
                self.observed_removed['allocated_bytes'] += node.allocated
            finally:
                os.close(fd)
            return
        fd = os.open(node.name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        try:
            current = stack + [(node, fd)]
            self.chain(target, current)
            require(set(os.listdir(fd)) == {child.name for child in node.children}, 'unknown/missing child after preflight')
            for child in node.children:
                self.erase(target, child, fd, current)
            self.chain(target, current)
            require(set(os.listdir(fd)) == {child.name for child in node.children if child.retained},
                    'unexpected final child set')
            os.fsync(fd)
            if not node.retained:
                self.chain(target, current)
                self.destroy(parent, node, fd)
                self.observed_removed['directories'] += 1
                self.observed_removed['allocated_bytes'] += node.allocated
            os.fsync(parent)
        finally:
            os.close(fd)

    def destroy(self, parent, node, fd):
        # A single destructive syscall has a masked, final pending-signal commitment.
        # A signal arriving after that sample is latched on unmask; no next deletion follows it.
        previous = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            self.g.cheap()
            require(identity(os.fstat(fd), node.directory) == node.pin
                    == identity(os.stat(node.name, dir_fd=parent, follow_symlinks=False), node.directory),
                    'destructive commitment identity drift')
            require(not CANCEL and not signal.sigpending() & SIGNALS, 'cancel before destructive commitment')
            if node.directory:
                os.rmdir(node.name, dir_fd=parent)
            else:
                os.unlink(node.name, dir_fd=parent)
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, previous)

    def verify_retained(self, target, node, parent, stack):
        self.g.tick()
        require(node.retained, 'retained verification requires retained member')
        if not node.directory:
            self.chain(target, stack)
            fd = os.open(node.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
            try:
                require(identity(os.fstat(fd)) == node.pin
                        == identity(os.stat(node.name, dir_fd=parent, follow_symlinks=False)), 'retained leaf drift')
            finally:
                os.close(fd)
            return
        fd = os.open(node.name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        try:
            current = stack + [(node, fd)]
            self.chain(target, current)
            kept = [child for child in node.children if child.retained]
            require(set(os.listdir(fd)) == {child.name for child in kept}, 'retained membership drift')
            for child in kept:
                self.verify_retained(target, child, fd, current)
            self.chain(target, current)
        finally:
            os.close(fd)

    def close_target(self, path, node, summary):
        before = self.observed_removed.copy()
        parent = self.d.open(path.parent)
        self.erase(path, node, parent, [])
        os.fsync(parent)
        if node.retained:
            self.verify_retained(path, node, parent, [])
            outcome = 'CLEARED_ALLOWED_MEMBERS_PROTECTED_REPORTS_RETAINED'
        else:
            # Only this successful rmdir+fsync authorizes REMOVED. Later absence alone never does.
            require(path.name not in os.listdir(parent), 'removed target reappeared')
            require(identity(os.fstat(self.d.fds[str(path)]), True) == node.pin, 'removed original descriptor drift')
            self.d.removed.add(str(path))
            outcome = 'REMOVED'
        delta = {key: value - before[key] for key, value in self.observed_removed.items()}
        self.g.j.event('target_outcome', path=str(path), outcome=outcome,
                       original=node.pin, snapshot_sha256=summary['snapshot_sha256'],
                       removed_syscalls_parent_durability_completed=delta,
                       retained={key: value for key, value in summary.items() if key.startswith('retained_')})


def read_tsv(data, header):
    reader = csv.DictReader(io.StringIO(data.decode('utf-8')), delimiter='\t')
    require(reader.fieldnames == header, 'exact TSV header')
    rows = list(reader)
    require(all(set(row) == set(header) and all(isinstance(v, str) and v for v in row.values())
                for row in rows), 'complete TSV rows')
    return rows


def selection(captured, source):
    classes = read_tsv(captured[str(CLASSES_FILE)], [
        'class_id', 'task', 'fqcn', 'declared_methods', 'source_path', 'source_sha256', 'expected_xml_path',
    ])
    methods = read_tsv(captured[str(METHODS_FILE)], ['invocation', 'class_id', 'source_method'])
    require(tuple(c['class_id'] for c in classes) == CLASS_IDS
            and tuple(int(c['declared_methods']) for c in classes) == COUNTS, 'fixed14 class inventory')
    require(len(methods) == 167 and len({tuple(m.values()) for m in methods}) == 167, '167 unique declarations')
    require(collections.Counter(m['invocation'] for m in methods)
            == {'ordinary': 163, 'prepare': 1, 'success': 1, 'wrong-key': 1, 'loader-io': 1}, 'phase declarations')
    source_map = {r['path']: r for r in source['files']}
    for klass in classes:
        require(source_map[klass['source_path']]['checkout_sha256'] == klass['source_sha256'], 'class C4 source binding')
        members = [m for m in methods if m['class_id'] == klass['class_id']]
        require(len(members) == int(klass['declared_methods']), 'class declaration count')
        require(all(m['invocation'] != 'ordinary' if klass['class_id'] == 'P01'
                    else m['invocation'] == 'ordinary' for m in members), 'ordinary/provider separation')
        module = '/'.join(klass['task'].split(':')[1:-1])
        require(klass['expected_xml_path'] == module + '/build/test-results/desktopTest/TEST-' + klass['fqcn'] + '.xml',
                'default exact XML path')
    return {
        mode: [{**klass, 'methods': [m['source_method'] for m in methods
                                    if m['class_id'] == klass['class_id'] and m['invocation'] == mode]}
               for klass in classes if any(m['class_id'] == klass['class_id'] and m['invocation'] == mode for m in methods)]
        for mode in PHASES
    }


def admission(dirs):
    captured, pins = {}, {}
    for path in INPUT_FILES + (REQUEST,) + ACCEPTANCES:
        data, pin, _ = dirs.read(path, 4 * MIB, bootstrap=True)
        captured[str(path)], pins[str(path)] = data, pin
    for path, digest in FROZEN.items():
        require(sha(captured[path]) == digest, 'frozen current-C4 input changed: ' + path)
    request = strict_json(captured[str(REQUEST)])
    require(request['format'] == 'passvault-linux-current-cycle-request-v1' and request['author'] == '/root',
            'current-cycle root request')
    require(request['run_id'] == 'linux-current-cycle02' and request['commit'] == COMMIT and request['tree'] == TREE,
            'fixed one-shot current-C4 identity')
    require(request['repository'] == str(REPOSITORY) and request['runtime'] == str(R)
            and request['evidence'] == str(E) and request['lock'] == str(LOCK), 'fixed current-cycle paths')
    require(request['object_store'] == str(SOURCE_OBJECTS), 'fixed borrowed source object store')
    require(request['findings'] == FINDINGS and request['phases'] == list(PHASES), 'fixed finding/phase scope')
    require(request['commands'] == COMMANDS and request['stop_command'] == STOP and request['environment'] == ENV,
            'exact command/base-environment contract')
    require(request['limits'] == LIMITS, 'exact bounded resource/time contract')
    require(request['network_policy'] == 'ORDINARY_ONLINE_STRICT_COLD4_OFFLINE_NO_FALLBACK'
            and request['no_other_audit_local_or_ci_job'] is True, 'network and sole-slot authority')
    require(request['git_source_contract'] == 'COOPERATIVELY_FROZEN_BORROWED_C4_OBJECTS_NOT_INDEPENDENT_CLONE'
            and request['runtime_closeout'] == 'SAME_LOCK_ORIGINAL_TARGET_FOREST_ONCE_OR_HOLD'
            and request['cleanup_device_contract'] == 'ORIGINAL_R_DIRECTORY_AND_WORKER_BLOCKER_FILE_DEVICES',
            'source/cleanup original-allocation contracts')
    require(request['created_unix_seconds'] <= time.time() <= request['expires_unix_seconds']
            and 0 < request['expires_unix_seconds'] - request['created_unix_seconds'] <= 3600,
            'fresh initial admission interval')
    bindings = {
        'input_sha256': {str(p): sha(captured[str(p)]) for p in INPUT_FILES},
        'commands_sha256': sha(canonical(COMMANDS)), 'stop_command_sha256': sha(canonical(STOP)),
        'environment_sha256': sha(canonical(ENV)), 'request_sha256': sha(captured[str(REQUEST)]),
    }
    require(request['bindings'] == {k: v for k, v in bindings.items() if k != 'request_sha256'}, 'request hash bindings')
    require(request['input_pins'] == {str(p): pins[str(p)] for p in INPUT_FILES}, 'original input pins')
    dirs.bind(request['directory_pins'])
    require(all(str(p) in dirs.pins for p in (BASE, REPOSITORY, E, LOCK.parent, Path(SDK))), 'original roots absent')
    require(all(str(p) in dirs.pins for p in (
        REPOSITORY / '.git', SOURCE_OBJECTS, SOURCE_OBJECTS / 'info', SOURCE_OBJECTS / 'pack',
    )), 'original borrowed object-store directory pins absent')
    require(dirs.pins[str(E)]['uid'] == UID and dirs.pins[str(E)]['mode'] == stat.S_IFDIR | 0o700,
            'original private evidence directory')
    tool_paths = {
        'java': JAVA_HOME + '/bin/java', 'java_release': JAVA_HOME + '/release',
        'git': '/usr/bin/git', 'python': os.path.realpath(sys.executable),
    }
    require(set(request['toolchain']) == set(tool_paths), 'fixed toolchain bindings')
    for name, path in tool_paths.items():
        bound = request['toolchain'][name]
        data, pin, digest = dirs.read(path, 32 * MIB)
        require(bound == {'path': path, 'sha256': digest, 'identity': pin}, 'original toolchain image')
        require(not pin['mode'] & (stat.S_ISUID | stat.S_ISGID), 'set-id toolchain forbidden')
        captured[path], pins[path] = data, pin
        if name == 'java_release':
            require(b'JAVA_VERSION="17.' in data and b'OS_ARCH="x86_64"' in data, 'JDK17 x86_64 required')
    source = strict_json(captured[str(SOURCE)])
    require(source['format'] == 'passvault-linux-checkout-source-v1' and source['commit'] == COMMIT
            and source['tree'] == TREE and len(source['files']) == 1572, 'full committed current-C4 manifest')
    require(len({r['path'] for r in source['files']}) == 1572
            and len(source['checkout_eol_qualifications']) == 2, 'unique full source/two EOL qualifications')
    selected = selection(captured, source)
    for path, reviewer, role in zip(ACCEPTANCES, REVIEWERS, REVIEW_ROLES):
        accept = strict_json(captured[str(path)])
        require(accept['format'] == 'passvault-linux-current-cycle-acceptance-v1' and accept['reviewer'] == reviewer
                and accept['runner_authors'] == RUNNER_AUTHORS and accept['review_role'] == role,
                'actual source-review identity/authorship/role')
        require(reviewer not in RUNNER_AUTHORS if role == 'INDEPENDENT_SOURCE_REVIEW' else reviewer in RUNNER_AUTHORS,
                'owner coauthor approval is not independent review')
        require(accept['disposition'] == 'ACCEPT' and accept['bindings'] == bindings
                and accept['purpose'] == 'FIVE_CURRENT_C4_INVOCATIONS_AND_SAME_LOCK_FINAL_CLEANUP',
                'normative fixed-cycle acceptance')
        require(accept['commit'] == COMMIT and accept['tree'] == TREE and accept['runtime'] == str(R)
                and accept['evidence'] == str(E), 'acceptance exact instance')
        require(accept['obligations'] == {name: 'ACCEPT' for name in (
            'source_selection', 'per_invocation_stop_and_fixture',
            'same_lock_original_target_cleanup', 'coordination_and_external_exit',
        )}, 'actual obligation dispositions')
    return request, captured, pins, source, selected, bindings


def check_borrowed_store(dirs):
    """Fixed metadata boundary, not a Git transport/configuration implementation."""
    for path in (REPOSITORY, REPOSITORY / '.git', SOURCE_OBJECTS,
                 SOURCE_OBJECTS / 'info', SOURCE_OBJECTS / 'pack'):
        dirs.open(path)
        pin = dirs.pins[str(path)]
        require(pin['uid'] == UID and not pin['mode'] & 0o022, 'borrowed source directory ownership/write mode')
    source_info = set(os.listdir(dirs.open(SOURCE_OBJECTS / 'info')))
    source_pack = os.listdir(dirs.open(SOURCE_OBJECTS / 'pack'))
    require(len(source_info) + len(source_pack) <= LIMITS['runtime_files'], 'source object metadata count bound')
    require(not source_info & {'alternates', 'http-alternates'}, 'recursive/HTTP source alternates forbidden')
    require(not any(name.endswith('.promisor') for name in source_pack), 'source promisor packs forbidden')
    # Only newly initialized own handles can enter this branch. Original own
    # config/alternates bytes are additionally sealed in captured/pins below.
    if str(OWN_OBJECTS / 'info') in dirs.pins:
        own_info = set(os.listdir(dirs.open(OWN_OBJECTS / 'info')))
        own_pack = os.listdir(dirs.open(OWN_OBJECTS / 'pack'))
        require('alternates' in own_info and 'http-alternates' not in own_info, 'own fixed alternate missing/HTTP alternate')
        require(not any(name.endswith('.promisor') for name in own_pack), 'own promisor packs forbidden')
        require('config.worktree' not in os.listdir(dirs.open(CHECKOUT / '.git')), 'own worktree config forbidden')


def fresh_init_config(data):
    """Accept only Git init's small local core section; no includes/remotes."""
    require(data.endswith(b'\n'), 'fresh init config termination')
    lines = data.decode('ascii').splitlines()
    require(len(lines) <= 16 and lines and lines[0] == '[core]', 'fresh init core-only config')
    values = {}
    for line in lines[1:]:
        match = re.fullmatch(r'\s*([a-z]+)\s*=\s*(0|true|false)\s*', line)
        require(match is not None and match[1] not in values, 'unexpected/duplicate init config entry')
        values[match[1]] = match[2]
    mandatory = {'repositoryformatversion', 'filemode', 'bare', 'logallrefupdates'}
    require(mandatory <= set(values) <= mandatory | {'ignorecase', 'symlinks'}, 'unexpected init config keys')
    require(values['repositoryformatversion'] == '0' and values['bare'] == 'false'
            and values['logallrefupdates'] == 'true', 'fresh SHA1 nonbare init config')
    require(all(value in ('true', 'false') for key, value in values.items() if key != 'repositoryformatversion'),
            'init Boolean setting')
    return values


def initialize_borrowed_repository(run, captured, pins):
    check_borrowed_store(run.d)
    require(not os.listdir(run.d.open(CHECKOUT)), 'fresh init requires original empty checkout; no adoption')
    run.git('init', ['init', '--object-format=sha1', '--template=', str(CHECKOUT)])
    require(not run.j.errors and not CANCEL and not signal.sigpending() & SIGNALS, 'init incomplete/cancelled; no alternate')
    for path in (CHECKOUT / '.git', OWN_OBJECTS, OWN_OBJECTS / 'info', OWN_OBJECTS / 'pack'):
        run.d.open(path, True)
        require(run.d.pins[str(path)]['uid'] == UID, 'new own Git directory ownership')
    require(not os.listdir(run.d.open(OWN_OBJECTS / 'info'))
            and not os.listdir(run.d.open(OWN_OBJECTS / 'pack')), 'new own object metadata is not empty')
    require('config.worktree' not in os.listdir(run.d.open(CHECKOUT / '.git')), 'new own worktree config forbidden')
    config = CHECKOUT / '.git/config'
    data, pin, digest = run.d.read(config, 4096)
    settings = fresh_init_config(data)
    captured[str(config)], pins[str(config)] = data, pin
    alternate = OWN_OBJECTS / 'info/alternates'
    encoded = (str(SOURCE_OBJECTS) + '\n').encode('ascii')
    run.j.event('borrowed_object_store_intent', source=str(SOURCE_OBJECTS), alternate=str(alternate),
                source_original=run.d.pins[str(SOURCE_OBJECTS)], own_info_original=run.d.pins[str(OWN_OBJECTS / 'info')])
    require(not run.j.errors and not CANCEL and not signal.sigpending() & SIGNALS, 'cancel/error before exclusive alternate')
    written = run.d.write_new(alternate, encoded)
    captured[str(alternate)], pins[str(alternate)] = encoded, written['identity']
    run.j.event('borrowed_object_store_bound', source=str(SOURCE_OBJECTS), alternate=str(alternate),
                original=written, config_original=pin, config_sha256=digest, init_config=settings,
                independence='BORROWED_ORIGINAL_OBJECTS_NOT_AN_INDEPENDENT_CLONE')
    recheck_inputs(run.d, captured, pins)


def recheck_inputs(dirs, captured, pins):
    check_borrowed_store(dirs)
    for path, data in captured.items():
        _, pin, digest = dirs.read(path, max(len(data), 1), keep=False)
        require(pin == pins[path] and digest == sha(data), 'admitted immutable input drift')


def source_bytes_check(dirs, source):
    """No Git command after cleanup; tracked originals remain outside all targets."""
    start, total = time.monotonic(), 0
    for row in source['files']:
        require(time.monotonic() - start <= LIMITS['source_scan_seconds'], 'physical source scan time bound')
        path = Path(row['path'])
        require(not path.is_absolute() and '..' not in path.parts, 'source path escape')
        _, pin, digest = dirs.read(CHECKOUT / path, 32 * MIB, keep=False)
        require(digest == row['checkout_sha256'] and pin['size'] == row['checkout_size']
                and bool(pin['mode'] & 0o111) == (row['git_mode'] == '100755'), 'tracked checkout bytes/mode drift')
        total += pin['size']
    return {'files': len(source['files']), 'checkout_bytes': total, 'commit': COMMIT, 'tree': TREE}


def original_stop_authority(run, source, captured, pins, lock_fd, lock_pin):
    run.d.verify()
    parent = run.d.open(LOCK.parent)
    require(identity(os.fstat(lock_fd)) == lock_pin
            == identity(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)), 'original stop lock changed')
    for name in ('gradlew', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties'):
        row = next(r for r in source['files'] if r['path'] == name)
        _, pin, digest = run.d.read(CHECKOUT / name, 32 * MIB, keep=False)
        require(digest == row['checkout_sha256'] and pin['size'] == row['checkout_size']
                and bool(pin['mode'] & 0o111) == (row['git_mode'] == '100755'), 'original wrapper stop authority changed')
    for path in (JAVA_HOME + '/bin/java', JAVA_HOME + '/release'):
        _, pin, digest = run.d.read(path, 32 * MIB, keep=False)
        require(pin == pins[path] and digest == sha(captured[path]), 'original stop JDK changed')


def check_empty_worker(dirs, mode, blocker):
    names = WORKERS[:3] if mode == 'ordinary' else ('pva038-' + mode,)
    for name in names:
        root = R / 'workers' / name
        expected = set(WORKER_CHILDREN) | ({'tmp-blocker'} if name == 'pva038-loader-io' else set())
        require(set(os.listdir(dirs.open(root))) == expected, 'unused worker root changed')
        for child in WORKER_CHILDREN:
            require(not os.listdir(dirs.open(root / child)), 'worker child is not original empty storage')
        if name == 'pva038-loader-io':
            data, pin, digest = dirs.read(root / 'tmp-blocker', 1)
            require(data == b'' and pin == blocker['identity'] and digest == blocker['sha256'], 'original zero-byte blocker changed')


def preserve_phase_xml(run, mode, classes, xml_budget):
    expected = {str(CHECKOUT / c['expected_xml_path']): c for c in classes}
    roots = sorted({Path(p).parent for p in expected})
    observed, absent, fixture = [], [], None
    mapping_ok = preservation_ok = True
    file_count = 0
    for root in roots:
        try:
            run.d.open(root, True)
            names = sorted(n for n in os.listdir(run.d.open(root)) if n.startswith('TEST-') and n.endswith('.xml'))
        except FileNotFoundError:
            absent.append(str(root))
            mapping_ok = False
            run.j.error(mode + ' expected XML directory absent')
            continue
        except (OSError, Refused) as error:
            run.j.error(mode + ' XML directory observation incomplete: ' + type(error).__name__)
            mapping_ok = preservation_ok = False
            continue
        if {str(root / n) for n in names} != {p for p in expected if Path(p).parent == root}:
            run.j.error(mode + ' missing/extra XML set')
            mapping_ok = False
        for name in names:
            file_count += 1
            if file_count > 32:
                run.j.error(mode + ' XML file count cap; originals retained')
                mapping_ok = preservation_ok = False
                break
            original = root / name
            row = {'source_path': str(original), 'preserved_path': str(E / ('xml-' + mode + '-' + name))}
            observed.append(row)
            try:
                data, pin, _ = run.d.read(original, LIMITS['xml_bytes_per_file'])
                xml_budget[0] += len(data)
                require(xml_budget[0] <= LIMITS['xml_total_bytes'], 'cycle aggregate XML cap')
                row['original'] = pin
                row['preserved'] = run.d.write_new(row['preserved_path'], data)
                run.retained_evidence.append({'path': row['preserved_path'], **row['preserved']})
            except (OSError, Refused) as error:
                row['preservation_error'] = type(error).__name__
                run.j.error(mode + ' XML preservation incomplete: ' + name)
                mapping_ok = preservation_ok = False
                continue
            try:
                require(data and str(original) in expected, 'empty/unexpected XML')
                require(b'<!DOCTYPE' not in data and b'<!ENTITY' not in data, 'XML declarations forbidden')
                suite = ET.fromstring(data)
                klass = expected[str(original)]
                require(suite.tag == 'testsuite' and suite.get('name') == klass['fqcn'].rsplit('.', 1)[1] + '[desktop]',
                        'exact suite name')
                cases = suite.findall('testcase')
                wanted = collections.Counter((klass['fqcn'], m + '[desktop]') for m in klass['methods'])
                got = collections.Counter((case.get('classname'), case.get('name')) for case in cases)
                require(got == wanted and len(cases) == len(klass['methods']), 'exact selected literal cases once')
                require(all(not case.findall('failure') and not case.findall('error') and not case.findall('skipped')
                            for case in cases), 'XML failure/error/skip')
                require(suite.get('tests') == str(len(cases)) and suite.get('failures') == '0'
                        and suite.get('errors') == '0' and suite.get('skipped') == '0', 'exact zero-failure suite counters')
                if mode == 'prepare':
                    lines = [line for out in suite.findall('system-out') for line in (out.text or '').splitlines()
                             if 'PVA038_FIXTURE=' in line]
                    require(len(lines) == 1 and re.fullmatch('PVA038_FIXTURE=' + FIXTURE_RE, lines[0]) is not None,
                            'exactly one producer XML fixture line')
                    fixture = lines[0][len('PVA038_FIXTURE='):]
                    require(len(fixture) == 317, 'exact fixture length')
                row['mapping'] = 'EXACT_CASE_MAPPING_NOT_INDEPENDENT_SEMANTIC_ACCEPTANCE'
            except (Refused, ET.ParseError) as error:
                row['mapping_error'] = str(error)
                run.j.error(mode + ' XML mapping failed: ' + name)
                mapping_ok = False
    inventory = {'phase': mode, 'observed': observed, 'absent_result_directories': absent,
                 'preservation_complete': preservation_ok, 'mapping_ok': mapping_ok and len(observed) == len(expected)}
    run.d.write_new(E / ('XML-' + mode + '-INVENTORY.json'), canonical(inventory))
    run.xml_ok = inventory['mapping_ok']
    run.xml_preserved = preservation_ok
    if mode == 'prepare' and run.xml_ok:
        require(fixture is not None, 'producer fixture absent')
        retained = run.d.write_new(E / 'PVA038-FIXTURE.txt', fixture.encode('ascii') + b'\n')
        run.retained_evidence.append({'path': str(E / 'PVA038-FIXTURE.txt'), **retained})
        run.j.event('fixture_from_preserved_prepare_xml', producer_xml=observed[0]['preserved_path'],
                    producer_xml_sha256=observed[0]['preserved']['sha256'], fixture_bytes=317,
                    fixture_sha256=sha(fixture.encode('ascii')), retained=retained)
        return fixture
    return None


def phase_summary(mode, run):
    return {'phase': mode, 'commands': run.commands, 'build_ok': run.build_ok,
            'stop_required': run.stop_required, 'stop_attempted': run.stop_attempted, 'stop_ok': run.stop_ok,
            'owned_settled': run.settled, 'xml_mapping_ok': run.xml_ok, 'xml_preservation_complete': run.xml_preserved,
            'environment_sha256': sha(canonical(run.environment))}


def note_error(journal, message):
    # Persistence failure is itself HOLD, but must not skip an original stop or
    # independently owned settlement merely because reporting also failed.
    try:
        journal.error(message)
    except BaseException:
        pass


def execute_phase(run, mode, source, classes, captured, pins, lock_fd, lock_pin, blocker, xml_budget):
    """A fresh Run owns exactly this phase's stop flags; never reset or retry them."""
    fixture = None
    run.xml_preserved = False
    try:
        require(time.monotonic() - run.started < LIMITS['total_seconds'], 'common normal-cycle deadline')
        recheck_inputs(run.d, captured, pins)
        run.d.verify()
        check_empty_worker(run.d, mode, blocker)
        run.j.event('phase_prelaunch_resources', phase=mode, **resource_sample(True))
        require(not run.p.scan() and not run.j.errors and not CANCEL, 'phase exclusive healthy admission')
        _, result = run.command(mode, COMMANDS[mode], LIMITS['build_seconds' if mode == 'ordinary' else 'cold_seconds'],
                                gradle=True)
        run.build_ok = result['started'] and result['complete'] and result['exit'] == 0 and not result['errors']
    except BaseException as error:
        note_error(run.j, mode + ' interrupted/refused: ' + type(error).__name__ + ': ' + str(error))
    finally:
        # Installed before any phase launch. This entire finally is entered once,
        # including launch-intent cancellation and an ambiguous Popen outcome.
        if run.stop_required:
            try:
                original_stop_authority(run, source, captured, pins, lock_fd, lock_pin)
                _, result = run.command(mode + '-stop', STOP, LIMITS['stop_seconds'], cleanup=True)
                run.stop_ok = result['started'] and result['complete'] and result['exit'] == 0 and not result['errors']
            except BaseException as error:
                note_error(run.j, mode + ' original stop outstanding/no retry: ' + type(error).__name__)
        try:
            run.settled = run.p.settle()
        except BaseException as error:
            note_error(run.j, mode + ' owned settlement incomplete: ' + type(error).__name__)
        if run.stop_required and run.settled:
            try:
                fixture = preserve_phase_xml(run, mode, classes, xml_budget)
            except BaseException as error:
                note_error(run.j, mode + ' XML/fixture finalization incomplete: ' + type(error).__name__)
        run.d.write_new(E / ('PHASE-' + mode + '.json'), canonical(phase_summary(mode, run)))
    return fixture


def capture_evidence(dirs):
    rows, total = [], 0
    names = sorted(set(os.listdir(dirs.open(E))) - {JOURNAL_PATH.name})
    require(len(names) <= LIMITS['evidence_files'], 'evidence file bound')
    for name in names:
        path = E / name
        _, pin, digest = dirs.read(path, 4 * MIB, keep=False, durable=True)
        total += pin['size']
        require(total <= LIMITS['evidence_bytes'], 'compact evidence aggregate bound')
        rows.append({'path': str(path), 'identity': pin, 'sha256': digest})
    return rows


def recheck_evidence(dirs, rows):
    for row in rows:
        _, pin, digest = dirs.read(row['path'], 4 * MIB, keep=False, durable=True)
        require(pin == row['identity'] and digest == row['sha256'], 'preserved evidence changed')


def cleanup_cycle(dirs, journal, processes, source, captured, pins, lock_fd, lock_pin, policy, evidence, accounting):
    """New same-process contract; never calls the old closer's admission/main."""
    guard = Guard(dirs, time.monotonic(), lock_fd, lock_pin, set(processes.owned), journal)
    dirs.tick = guard.tick
    forest = Forest(dirs, guard, policy)
    accounting['observed_removed_not_all_durability_proven'] = forest.observed_removed
    accounting['all_target_durability_completed'] = False
    guard.tick(force=True)
    recheck_inputs(dirs, captured, pins)
    recheck_evidence(dirs, evidence)
    journal.event('cleanup_source_before', **source_bytes_check(dirs, source))
    journal.event('all_target_preflight_complete', **forest.preflight())
    for path, node, summary in forest.snapshots:
        guard.tick()
        journal.event('target_deletion_intent', path=str(path), original=node.pin, **summary)
    # Every original target, descendant snapshot and deletion intent precedes unlink.
    dirs.verify()
    recheck_inputs(dirs, captured, pins)
    recheck_evidence(dirs, evidence)
    guard.tick(force=True)
    for path, node, summary in forest.snapshots:
        forest.close_target(path, node, summary)
        if str(path) in dirs.removed:
            # Worker children were also original allocations. Propagate only a
            # proven top rmdir, never infer removal from current absence alone.
            dirs.removed.update(p for p in dirs.pins if under(p, str(path)))
    dirs.verify()
    for path, node, _ in forest.snapshots:
        if node.retained:
            forest.verify_retained(path, node, dirs.open(path.parent), [])
        else:
            require(path.name not in os.listdir(dirs.open(path.parent)), 'removed target reappeared')
    recheck_inputs(dirs, captured, pins)
    recheck_evidence(dirs, evidence)
    journal.event('cleanup_source_after', **source_bytes_check(dirs, source))
    guard.tick(force=True)
    journal.event('cleanup_completed', targets=len(TARGET_PATHS), observed_removed=forest.observed_removed,
                  retained='R_CHECKOUT_GIT_SOURCE_SCHEMAS_PERMANENT_TESTS_REPORTS_TEST_RESULTS_AND_E',
                  accounting='LOGICAL_AND_STAT_BLOCK_ACCOUNTING_NOT_MEASURED_NET_RECLAIMED_SPACE')
    accounting['all_target_durability_completed'] = True


def main():
    require(sys.platform == 'linux' and sys.argv == [str(SELF)] and Path(__file__) == SELF
            and sys.flags.isolated and sys.dont_write_bytecode, 'fixed /usr/bin/python3 -I -B absolute entry only')
    os.umask(0o077)
    for sig in SIGNALS:
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    started = time.monotonic()
    # Stable bounded file reads must remain possible for the original stop and
    # evidence after cancellation; the cleanup Guard later adds stricter ticks.
    dirs = Directories(lambda: None)
    lock_fd = journal = processes = control = None
    request = captured = pins = source = selected = bindings = blocker = policy = None
    phases, xml_budget = {}, [0]
    allocation_complete = source_before = source_after = final_settled = cleanup_ok = False
    removed, code = {}, 1
    try:
        request, captured, pins, source, selected, bindings = admission(dirs)
        parent = dirs.open(LOCK.parent)
        lock_pin = request['lock_pin']
        before = os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)
        require(identity(before) == lock_pin and before.st_uid == UID and before.st_nlink == 1
                and before.st_mode == stat.S_IFREG | 0o600, 'original private regular lock')
        lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        require(identity(os.fstat(lock_fd)) == lock_pin, 'original opened lock')
        fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        dirs.verify()
        recheck_inputs(dirs, captured, pins)
        require(identity(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)) == lock_pin, 'under-lock original changed')
        require(time.time() <= request['expires_unix_seconds'] and not CANCEL and not signal.sigpending() & SIGNALS,
                'expired/cancelled initial admission')
        require(not os.path.lexists(R) and not os.listdir(dirs.open(E)), 'one-shot namespace occupied; no adoption/retry')
        resources = resource_sample(True)
        journal = Journal(dirs)
        journal.event('admission_under_original_lock', bindings=bindings, directory_pins=dirs.pins.copy(),
                      lock_pin=lock_pin, resources=resources, source_contract=request['git_source_contract'])
        processes = Processes(journal)
        require(not processes.scan() and not journal.errors, 'initial exclusive process settlement')
        control = Run(dirs, journal, processes, ENV, started)
        dirs.mkdir(R, journal)
        for name in PRIVATE_DIRS:
            dirs.mkdir(R / name, journal)
        initialize_borrowed_repository(control, captured, pins)
        control.git('checkout', ['-C', str(CHECKOUT), 'checkout', '--detach', COMMIT])
        control.check_source(source, True)
        source_before = True
        for name in GENERATED_ROOTS:
            require(not any(r['path'] == name or r['path'].startswith(name + '/') for r in source['files']),
                    'generated target collides with tracked source')
            require(not os.path.lexists(CHECKOUT / name), 'generated target occupied; no adoption')
        for name in GENERATED_ROOTS:
            dirs.mkdir(CHECKOUT / name, journal)
        for name in WORKERS:
            dirs.mkdir(R / 'workers' / name, journal)
            for child in WORKER_CHILDREN:
                dirs.mkdir(R / 'workers' / name / child, journal)
        blocker = dirs.write_new(R / 'workers/pva038-loader-io/tmp-blocker', b'')
        policy = {'directory_dev': dirs.pins[str(R)]['dev'], 'regular_file_dev': blocker['identity']['dev']}
        require(all(dirs.pins[str(p)]['dev'] == policy['directory_dev'] for p in TARGET_PATHS), 'original top device policy')
        journal.event('complete_original_allocations', targets=[str(p) for p in TARGET_PATHS], blocker=blocker, device_policy=policy)
        allocation_complete = True
        fixture = None
        for mode in PHASES:
            environment = dict(ENV)
            if mode in CONSUMERS:
                require(isinstance(fixture, str) and len(fixture) == 317 and re.fullmatch(FIXTURE_RE, fixture),
                        'consumer lacks original producer fixture')
                environment['PASSVAULT_PVA038_FIXTURE'] = fixture
                journal.event('consumer_fixture_binding', phase=mode, fixture_sha256=sha(fixture.encode('ascii')))
            run = Run(dirs, journal, processes, environment, started)
            phases[mode] = run
            produced = execute_phase(run, mode, source, selected[mode], captured, pins, lock_fd, lock_pin, blocker, xml_budget)
            require(run.build_ok and run.stop_required and run.stop_attempted and run.stop_ok
                    and run.settled and run.xml_ok and run.xml_preserved and not journal.errors and not CANCEL,
                    mode + ' incomplete; remaining fixed calls unstarted, no retry')
            if mode == 'prepare':
                fixture = produced
    except BaseException as error:
        if journal is not None:
            try:
                journal.error('cycle interrupted/refused: ' + type(error).__name__ + ': ' + str(error))
            except BaseException:
                pass
        else:
            os.write(2, ('EARLY_REFUSAL_NO_PROJECT_LAUNCH: ' + type(error).__name__ + ': ' + str(error) + '\n').encode())
    finally:
        # No per-phase stop is retried here, even if the previous finally failed.
        if control is not None:
            try:
                final_settled = processes.settle()
                require(final_settled, 'post-phase settlement required before source observation')
                final_settled = False
                control.check_source(source)
                source_after = True
            except BaseException as error:
                try:
                    journal.error('final source/owned settlement incomplete: ' + type(error).__name__)
                except BaseException:
                    pass
            finally:
                try:
                    final_settled = processes.settle()
                except BaseException as error:
                    final_settled = False
                    note_error(journal, 'final Git child settlement incomplete: ' + type(error).__name__)
            try:
                dirs.verify()
                journal.latch_cancel()
                dirs.write_new(E / 'ORIGINAL-DIRECTORIES.json', canonical({
                    'directory_pins': dirs.pins, 'allocation_complete': allocation_complete,
                    'targets': [str(p) for p in TARGET_PATHS], 'blocker': blocker, 'device_policy': policy,
                }))
                dirs.write_new(E / 'PHASES.json', canonical({
                    'declared_regression_methods': 166, 'declared_fixture_producers': 1,
                    'attempts': [phase_summary(m, r) for m, r in phases.items()],
                    'unstarted_without_launch_intent': [m for m in PHASES if m not in phases or not phases[m].stop_required],
                    'attempted_start_unobserved': [m for m, r in phases.items() if r.stop_required
                                                  and not any(c['label'] == m and c['started'] for c in r.commands)],
                    'control_commands': control.commands,
                }))
                for run in (control, *phases.values()):
                    for command in run.commands:
                        _, pin, digest = dirs.read(E / (command['label'] + '.log'), 4 * MIB, keep=False, durable=True)
                        require(pin['size'] == command['log_bytes'] and digest == command['log_sha256'],
                                'command log differs from originally collected bytes')
                    recheck_evidence(dirs, run.retained_evidence)
                evidence = capture_evidence(dirs)
                evidence_image = dirs.write_new(E / 'EVIDENCE.json', canonical(evidence))
                evidence.append({'path': str(E / 'EVIDENCE.json'), **evidence_image})
                require(allocation_complete and source_before and source_after and final_settled
                        and journal.ok and not processes.unknown and not CANCEL and not signal.sigpending() & SIGNALS,
                        'cleanup original allocation/source/settlement authority incomplete')
                require(all(not r.stop_required or (r.stop_attempted and r.stop_ok and r.settled and r.xml_preserved)
                            for r in phases.values()), 'original per-call stop/evidence still outstanding')
                # Failed validation can still safely clean after complete evidence,
                # original stops and settlement. It never becomes validation success.
                cleanup_cycle(dirs, journal, processes, source, captured, pins,
                              lock_fd, request['lock_pin'], policy, evidence, removed)
                cleanup_ok = True
            except BaseException as error:
                try:
                    journal.error('cleanup consumed HOLD/no automatic retry: ' + type(error).__name__ + ': ' + str(error))
                except BaseException:
                    pass
            finally:
                dirs.tick = lambda: None
            try:
                journal.latch_cancel()
                journal.event('final_resources', **resource_sample())
                validation_ok = len(phases) == len(PHASES) and all(
                    r.build_ok and r.stop_required and r.stop_attempted and r.stop_ok
                    and r.xml_ok and r.settled for r in phases.values())
                dirs.write_new(E / 'RESULT.json', canonical({
                    'status': 'PRETERMINAL_OBSERVATIONS_NOT_AN_INDEPENDENT_PASS',
                    'commit': COMMIT, 'tree': TREE, 'validation_mapping_ok': validation_ok,
                    'source_before_ok': source_before, 'source_after_ok': source_after,
                    'owned_settled': final_settled, 'cleanup_completed': cleanup_ok,
                    'removed_accounting': removed, 'errors': journal.errors,
                    'runtime_disposition': 'GENERATED_CLEANED_SOURCE_REPORTS_RETAINED' if cleanup_ok else 'CONSUMED_HOLD_NO_RETRY',
                    'semantic_disposition': 'NOT_INDEPENDENTLY_VERIFIED',
                }))
                signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
                CANCEL.update(signal.sigpending() & SIGNALS)
                journal.latch_cancel()
                okay = validation_ok and source_before and source_after and final_settled and cleanup_ok
                code = 0 if okay and journal.ok and not journal.errors and not CANCEL else 1
                journal.event('terminal_commit', exit=code, operational_settled=code == 0,
                              cleanup_completed=cleanup_ok, semantic_disposition='NOT_INDEPENDENTLY_VERIFIED')
            except BaseException:
                code = 1
        failures = []
        if processes is not None:
            try:
                processes.close()
            except OSError:
                failures.append('owned process descriptor close failure')
        if journal is not None and journal.fd is not None:
            try:
                os.close(journal.fd)
            except OSError:
                failures.append('journal descriptor close failure')
        if lock_fd is not None:
            try:
                os.close(lock_fd)
            except OSError:
                failures.append('lock descriptor close failure')
        failures.extend(dirs.close())
        if failures:
            code = 1
            os.write(2, b'CURRENT_CYCLE_DESCRIPTOR_SETTLEMENT_HOLD; external exit still required\n')
    os._exit(code)


if __name__ == '__main__':
    main()
