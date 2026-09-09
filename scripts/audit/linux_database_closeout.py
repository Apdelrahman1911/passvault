#!/usr/bin/python3
"""Fixed, separately admitted closeout; this source is NOT execution permission.

Never imports the validation runner, launches a command, signals a process,
retries an operation, adopts a replacement or removes the runtime/checkout.
Root alone may invoke the absolute entry with /usr/bin/python3 -I -B, no args.
Cooperative frozen namespace only; not a hostile-UID or atomic-kernel sandbox.
"""

import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import resource
import signal
import stat
import sys
import time


BASE = Path('/root/projects/PassVault')
W = BASE / 'passvault-linux'
R = BASE / 'audit-runtime-linux-db-01'
CHECKOUT = R / 'checkout'
E = W / 'docs/audit-continuation/2026-09-08-linux/runs/linux-database-01'
C = W / 'docs/audit-continuation/2026-09-08-linux/reviews/linux-closeout'
RUN = W / 'docs/audit-continuation/2026-09-08-linux/reviews/linux-runner'
LOCK = BASE / '.audit-coordination-linux-20260908/build.lock'
SELF = W / 'scripts/audit/linux_database_closeout.py'
REQUEST = C / 'REQUEST.json'
ACCEPTANCES = (C / 'ACCEPT-root.json', C / 'ACCEPT-baseline_coverage.json')
JOURNAL_PATH = E / 'CLOSEOUT-JOURNAL.jsonl'
COMMIT = '9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed'
TREE = '05014e9f635131d5db06701e4013b4b5a746465a'
METHODS_SHA = '40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979'
FILES = {
    'helper_sha256': SELF, 'plan_sha256': C / 'PLAN.md',
    'targets_sha256': C / 'TARGETS.json', 'evidence_manifest_sha256': C / 'EVIDENCE.json',
    'runner_sha256': W / 'scripts/audit/linux_database_validation.py',
    'runner_plan_sha256': RUN / 'PLAN.md', 'runner_request_sha256': RUN / 'REQUEST.json',
    'source_sha256': RUN / 'SOURCE.json',
    'methods_sha256': W / 'docs/audit-continuation/2026-09-08-linux/reviews/verification/METHOD-INVENTORY.json',
    'runner_accept_root_sha256': RUN / 'ACCEPT-root.json',
    'runner_accept_verification_sha256': RUN / 'ACCEPT-verification.json',
    'launcher_sha256': RUN / 'LAUNCHER.json', 'run_journal_sha256': E / 'JOURNAL.jsonl',
    'run_result_sha256': E / 'RESULT.json', 'run_originals_sha256': E / 'ORIGINAL-DIRECTORIES.json',
}
LAUNCHER_FILES = tuple(RUN / name for name in (
    'LAUNCHER-stdout.log', 'LAUNCHER-stderr.log', 'LAUNCHER-resources.json', 'LAUNCHER-processes.json',
))
PRIVATE = (
    'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'xdg-cache',
    'xdg-config', 'xdg-data', 'xdg-state', 'android-user',
)
GENERATED = (
    '.gradle', '.kotlin', 'build', 'app-android/build', 'app-desktop/build', 'shared/build',
    'core/domain/build', 'core/database/build', 'core/crypto/build', 'core/security/build',
    'core/designsystem/build', 'core/navigation/build', 'core/otp/build', 'core/testing/build',
    'feature/onboarding/build', 'feature/unlock/build', 'feature/vault/build',
    'feature/credential/build', 'feature/generator/build', 'feature/health/build',
    'feature/settings/build', 'feature/backup/build',
)
TARGET_PATHS = tuple(R / p for p in PRIVATE) + tuple(CHECKOUT / p for p in GENERATED)
PROTECTED_NAMES = {'.git', 'reports', 'test-results'}
LABELS = ('clone', 'checkout', 'identity-before', 'members-before', 'database',
          'stop', 'identity-after', 'members-after')
BUILDLIKE = {
    'java', 'javaw', 'gradle', 'kotlinc', 'kotlin', 'xcodebuild', 'swift', 'swift-frontend',
    'clang', 'clang++', 'gcc', 'g++', 'cc1', 'cc1plus', 'cmake', 'ninja', 'make', 'adb',
    'emulator', 'qemu-system-x86',
}
SIGNALS = {signal.SIGINT, signal.SIGTERM, signal.SIGHUP}
CANCEL = set()
UID = os.getuid()
MIB = 1024 ** 2
GIB = 1024 ** 3
LIMITS = {
    'total_seconds': 900, 'admission_validity_seconds': 3600,
    'files': 200000, 'directories': 30000, 'depth': 64, 'logical_bytes': 6 * GIB,
    'address_space_bytes': 512 * MIB, 'journal_bytes': 8 * MIB,
    'control_bytes': 4 * MIB, 'evidence_bytes': 80 * MIB, 'evidence_files': 64,
    'source_bytes': 256 * MIB, 'source_file_bytes': 32 * MIB,
    'mountinfo_bytes': 2 * MIB, 'processes': 65536, 'poll_seconds': 5,
    'launch_free_bytes': 12 * GIB, 'running_free_bytes': 8 * GIB,
    'launch_memory_fraction': 0.25, 'running_memory_fraction': 0.20,
}
ACKNOWLEDGEMENTS = [
    'COOPERATIVE_NAMESPACE_NOT_HOSTILE_UID_SANDBOX',
    'POINT_SAMPLED_PROCESS_RESOURCE_MOUNT_EVIDENCE',
    'ALL_33_ORIGINAL_TOP_ALLOCATIONS_REQUIRED_NO_ADOPTION',
    'DESCENDANT_SNAPSHOTS_ARE_POST_RUN_OBSERVATIONS_NOT_CREATION_PROOF',
    'RETAIN_RUNTIME_CHECKOUT_SOURCE_SCHEMAS_GIT_REPORTS_AND_TEST_RESULTS',
    'NO_COMMAND_SIGNAL_STOP_RETRY_OR_AUTOMATIC_RECOVERY',
    'PARTIAL_INTERRUPTED_UNCERTAIN_CLOSEOUT_IS_CONSUMED_HOLD',
    'NO_APPLICATION_PASS_CLOSURE_OR_HARDWARE_CREDIT',
]


class Hold(Exception):
    pass


def require(value, reason):
    if not value:
        raise Hold(reason)


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True).encode() + b'\n'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def strict_json(data):
    def pairs(items):
        result = {}
        for key, value in items:
            require(key not in result, 'duplicate JSON key')
            result[key] = value
        return result
    return json.loads(data, object_pairs_hook=pairs,
                      parse_constant=lambda _: (_ for _ in ()).throw(Hold('nonfinite JSON')))


def identity(st, directory=False):
    keys = ('dev', 'ino', 'uid', 'mode') if directory else (
        'dev', 'ino', 'uid', 'mode', 'nlink', 'size', 'mtime_ns', 'ctime_ns')
    return {key: getattr(st, 'st_' + key) for key in keys}


def absolute(path):
    path = Path(path)
    require(path.is_absolute() and '..' not in path.parts, 'noncanonical absolute path')
    return path


def relative(path):
    require(isinstance(path, str) and path and len(path) <= 4096, 'relative path length/type')
    result = Path(path)
    require(not result.is_absolute() and '..' not in result.parts and result.as_posix() == path
            and result.parts and all(ord(c) >= 32 for c in path), 'noncanonical relative path')
    return result


def under(path, root):
    return path == root or path.startswith(root + '/')


def ancestors(paths, tick):
    result = set()
    for path in paths:
        tick()
        current = absolute(path)
        while True:
            tick()
            result.add(str(current))
            if current == Path('/'):
                break
            current = current.parent
    return result


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


def proc_read(path, cap):
    with open(path, 'rb') as stream:
        data = stream.read(cap + 1)
    require(len(data) <= cap, 'proc input cap')
    return data


def process_row(pid):
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
                rows[pid] = process_row(pid)
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
        require(time.monotonic() - self.started <= LIMITS['total_seconds'], 'closeout time cap')
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


def event_rows(events, kind, tick):
    selected = []
    for row in events:
        tick()
        if row['kind'] == kind:
            selected.append(row)
    return selected


def parse_journal(data, tick):
    require(data and data.endswith(b'\n'), 'missing/incomplete run journal')
    events = []
    for line in data.splitlines():
        tick()
        events.append(strict_json(line))
    require(len(events) <= 100000, 'run journal event cap')
    for index, row in enumerate(events):
        tick()
        require(row['sequence'] == index and type(row['time_ns']) is int and row['time_ns'] > 0,
                'run journal sequence/time')
        require(isinstance(row['kind'], str), 'run journal kind')
    require(events[0]['kind'] == 'admission_under_original_lock', 'missing original run admission')
    require(events[-1]['kind'] == 'terminal_commit'
            and len(event_rows(events, 'terminal_commit', tick)) == 1, 'run terminal missing/ambiguous')
    return events


def run_contract(captured, pins, tick):
    def get(key):
        tick()
        value = strict_json(captured[str(FILES[key])])
        tick()
        return value
    request, result = get('runner_request_sha256'), get('run_result_sha256')
    original, launcher = get('run_originals_sha256'), get('launcher_sha256')
    events = parse_journal(captured[str(FILES['run_journal_sha256'])], tick)
    source = get('source_sha256')
    require(request['format'] == 'passvault-linux-database-request-v1' and request['author'] == '/root'
            and request['run_id'] == 'linux-database-01' and request['commit'] == COMMIT
            and request['tree'] == TREE and request['runtime'] == str(R)
            and request['evidence'] == str(E) and request['lock'] == str(LOCK), 'original fixed run request')
    run_bindings = {
        'runner_sha256': sha(captured[str(FILES['runner_sha256'])]),
        'plan_sha256': sha(captured[str(FILES['runner_plan_sha256'])]),
        'request_sha256': sha(captured[str(FILES['runner_request_sha256'])]),
        'source_sha256': sha(captured[str(FILES['source_sha256'])]),
        'methods_sha256': sha(captured[str(FILES['methods_sha256'])]),
        'command_sha256': sha(canonical(request['command'])),
        'stop_command_sha256': sha(canonical(request['stop_command'])),
        'environment_sha256': sha(canonical(request['environment'])),
    }
    require(run_bindings['methods_sha256'] == METHODS_SHA, 'frozen 105-method inventory')
    require(request['bindings'] == {k: v for k, v in run_bindings.items() if k != 'request_sha256'}
            and events[0]['bindings'] == run_bindings, 'original run binding mismatch')
    require(events[0]['lock_pin'] == request['lock_pin']
            and events[0]['directory_pins'] == request['directory_pins'], 'original run lock/parents')
    immutable = ('runner_sha256', 'runner_plan_sha256', 'source_sha256', 'methods_sha256')
    require(request['input_pins'] == {str(FILES[k]): pins[str(FILES[k])] for k in immutable},
            'original run immutable input identities changed')
    for key, reviewer, role in (
        ('runner_accept_root_sha256', '/root', 'OWNER_COAUTHOR_APPROVAL'),
        ('runner_accept_verification_sha256', '/root/verification', 'INDEPENDENT_SOURCE_REVIEW'),
    ):
        tick()
        accepted = get(key)
        require(accepted['format'] == 'passvault-linux-database-acceptance-v1'
                and accepted['reviewer'] == reviewer and accepted['review_role'] == role
                and accepted['runner_authors'] == ['/root/storage', '/root']
                and accepted['purpose'] == 'ONE_SHOT_LINUX_DATABASE_EXECUTION_AND_RETAINED_CLOSEOUT_CONTRACT'
                and accepted['disposition'] == 'ACCEPT' and accepted['bindings'] == run_bindings
                and accepted['commit'] == COMMIT and accepted['tree'] == TREE
                and accepted['runtime'] == str(R) and accepted['evidence'] == str(E)
                and accepted['obligations'] == {f'F{i:02}': 'ACCEPT' for i in range(1, 8)},
                'actual original runner acceptance absent')
    require(result['status'] == 'PRETERMINAL_OBSERVATIONS_NOT_A_PASS' and result['runtime'] == str(R)
            and result['runtime_disposition'] == 'RETAINED_IMMEDIATE_REVIEWED_ROOT_CLOSEOUT_REQUIRED'
            and result['semantic_disposition'] == 'NOT_INDEPENDENTLY_VERIFIED'
            and result['owned_settled'] is True, 'run result/settlement contract')
    terminal = events[-1]
    require(type(terminal['exit']) is int and terminal['exit'] in (0, 1)
            and terminal['semantic_disposition'] == 'NOT_INDEPENDENTLY_VERIFIED'
            and terminal['runtime_disposition'] == result['runtime_disposition'], 'run terminal contract')
    require(terminal['operational_settled'] is (terminal['exit'] == 0), 'run terminal operational consistency')
    require(launcher['format'] == 'passvault-linux-database-launcher-v1' and launcher['author'] == '/root'
            and launcher['run_id'] == 'linux-database-01' and launcher['runtime'] == str(R)
            and launcher['evidence'] == str(E)
            and launcher['argv'] == ['/usr/bin/python3', '-I', '-B', str(FILES['runner_sha256'])]
            and launcher['completed'] is True and launcher['runner_exit'] == terminal['exit']
            and type(launcher['outer_exit']) is int and launcher['outer_exit'] in (0, 1)
            and launcher['no_other_audit_local_or_ci_job'] is True
            and launcher['external_timeout_observed'] is False
            and launcher['external_interruption_observed'] is False, 'external completed launcher missing/ambiguous')
    require(0 < launcher['started_unix_ns'] <= events[0]['time_ns'] <= events[-1]['time_ns']
            <= launcher['finished_unix_ns'] <= time.time_ns(), 'launcher/run time interval')
    require(launcher['bindings'] == {**run_bindings,
            'accept_root_sha256': sha(captured[str(FILES['runner_accept_root_sha256'])]),
            'accept_verification_sha256': sha(captured[str(FILES['runner_accept_verification_sha256'])])},
            'launcher exact run/input/review binding')
    require(isinstance(launcher['limitations'], list) and launcher['limitations'], 'launcher limitations absent')
    evidence_files = launcher['evidence_files']
    require(isinstance(evidence_files, list) and len(evidence_files) == len(LAUNCHER_FILES)
            and all(isinstance(row, dict) and set(row) == {'path', 'sha256', 'identity'} for row in evidence_files)
            and [row['path'] for row in evidence_files] == [str(p) for p in LAUNCHER_FILES],
            'exact bounded launcher evidence set')
    results = [{k: v for k, v in row.items() if k not in ('sequence', 'time_ns', 'kind')}
               for row in event_rows(events, 'command_result', tick)]
    require(results == result['commands'], 'journal/RESULT command disagreement')
    require(len({row['label'] for row in results}) == len(results)
            and all(row['label'] in LABELS for row in results), 'unknown/repeated command result')
    intents = event_rows(events, 'launch_intent', tick)
    observed = event_rows(events, 'launch_observed', tick)
    require(len({row['label'] for row in intents}) == len(intents)
            and len({row['label'] for row in observed}) == len(observed), 'repeated launch intent/observation')
    by_label = {row['label']: row for row in intents}
    for row in results:
        tick()
        label = row['label']
        if label in by_label:
            intent = by_label[label]
            require(intent['argv'] == row['argv'] and intent['cwd'] == row['cwd'] == str(CHECKOUT)
                    and intent['environment_sha256'] == run_bindings['environment_sha256'], 'launch authority drift')
        if row['started']:
            require(label in by_label and sum(o['label'] == label and o['pid'] == row['pid'] for o in observed) == 1,
                    'started command lacks original launch commitment/observation')
        require(type(row['log_bytes']) is int and 0 <= row['log_bytes'] <= 4 * MIB, 'run command log bound')
    stop = [row for row in results if row['label'] == 'stop']
    if result['stop_required'] is True:
        require(result['stop_attempted'] is True and result['stop_ok'] is True and len(stop) == 1,
                'original stop outstanding; retain all original runtime authority')
        row = stop[0]
        require(row['started'] is True and row['complete'] is True and row['exit'] == 0 and row['errors'] == []
                and row['argv'] == request['stop_command'] and 'stop' in by_label
                and by_label['stop']['stop_required'] is True
                and by_label['stop']['budget_seconds'] == request['limits']['stop_seconds'], 'original stop not proved zero')
        require(request['stop_command'][0:2] == [str(CHECKOUT / 'gradlew'), '--stop'], 'original wrapper stop authority')
        require('database' in by_label and by_label['database']['stop_required'] is True
                and by_label['database']['argv'] == request['command']
                and by_label['database']['budget_seconds'] == request['limits']['build_seconds']
                and by_label['database']['sequence'] < by_label['stop']['sequence'],
                'original pre-Gradle stop obligation commitment missing')
    else:
        require(result['stop_required'] is False and result['stop_attempted'] is False
                and result['stop_ok'] is False and not stop
                and all(row['label'] not in ('database', 'stop') for row in intents + observed)
                and not any(row['label'] == 'database' and row['started'] for row in results)
                and all(row['stop_required'] is False for row in intents), 'no-Gradle-launch proof incomplete')
    require(source['format'] == 'passvault-linux-checkout-source-v1' and source['commit'] == COMMIT
            and source['tree'] == TREE and len(source['files']) == 1198
            and len({row['path'] for row in source['files']}) == 1198
            and len(source['checkout_eol_qualifications']) == 2, 'full actual checkout source contract')
    for row in source['files']:
        tick()
        relative(row['path'])
        require(row['git_mode'] in ('100644', '100755'), 'nonregular source manifest')
        require(not any(under(row['path'], p) for p in GENERATED), 'source/disposable prefix collision')
    require(any(row['phase'] == 'before' and row['files'] == 1198 and row['commit'] == COMMIT and row['tree'] == TREE
                for row in event_rows(events, 'source_bound', tick)), 'fresh original checkout not verified')
    process_events = event_rows(events, 'owned_process', tick) + event_rows(events, 'direct_child_already_exited', tick)
    owned = {(row['pid'], row['start']) for row in process_events
             if row['kind'] == 'owned_process' or 'start' in row}
    return request, result, original, launcher, events, source, owned


def original_targets(events, original, tick):
    expected = {str(R), str(CHECKOUT), *(str(p) for p in TARGET_PATHS)}
    originals = event_rows(events, 'allocation_original', tick)
    intents = event_rows(events, 'allocation_intent', tick)
    require(len(originals) == len(intents) == 35
            and {row['path'] for row in originals} == {row['path'] for row in intents} == expected,
            'all 35 original runtime/checkout/target allocations required; partial allocation HOLD')
    by_path = {}
    for row in originals:
        tick()
        intent = events[row['sequence'] - 1]
        path = absolute(row['path'])
        require(intent['kind'] == 'allocation_intent' and intent['path'] == row['path']
                and intent['parent'] == original[str(path.parent)] and row['identity'] == original[str(path)],
                'allocation intent/original/parent mismatch')
        require(row['identity']['uid'] == UID and row['identity']['mode'] == stat.S_IFDIR | 0o700,
                'original allocation ownership/private mode')
        by_path[str(path)] = {'path': str(path), 'identity': row['identity'],
                              'parent_path': str(path.parent), 'parent_pin': intent['parent'],
                              'allocation_intent_sequence': intent['sequence'],
                              'allocation_original_sequence': row['sequence']}
    return [by_path[str(path)] for path in TARGET_PATHS]


def admission(dirs):
    dirs.tick()
    captured, pins, digests = {}, {}, {}
    paths = (REQUEST, *FILES.values(), *ACCEPTANCES)
    for path in paths:
        cap = 8 * MIB if path == FILES['run_journal_sha256'] else LIMITS['control_bytes']
        data, pin, digest = dirs.read(path, cap, bootstrap=True)
        captured[str(path)], pins[str(path)], digests[str(path)] = data, pin, digest
    request = strict_json(captured[str(REQUEST)])
    dirs.tick()
    require(request['format'] == 'passvault-linux-database-closeout-request-v1' and request['author'] == '/root'
            and request['run_id'] == 'linux-database-01' and request['runtime'] == str(R)
            and request['evidence'] == str(E) and request['lock'] == str(LOCK)
            and request['commit'] == COMMIT and request['tree'] == TREE, 'fixed closeout request identity')
    require(request['command'] == ['/usr/bin/python3', '-I', '-B', str(SELF)] and request['limits'] == LIMITS,
            'fixed closeout entry/bounds')
    require(request['no_other_audit_local_or_ci_job'] is True and request['cooperative_producers_frozen'] is True,
            'fresh coordination/producer freeze attestation')
    require(request['created_unix_seconds'] <= time.time() <= request['expires_unix_seconds']
            and 0 < request['expires_unix_seconds'] - request['created_unix_seconds']
            <= LIMITS['admission_validity_seconds'], 'prospective closeout validity interval')
    bindings = {key: digests[str(path)] for key, path in FILES.items()}
    require(request['bindings'] == bindings, 'exact closeout input byte bindings')
    require(request['input_pins'] == {str(path): pins[str(path)] for path in FILES.values()}, 'sealed closeout input originals')
    for path, reviewer, role in zip(ACCEPTANCES, ('/root', '/root/baseline_coverage'),
                                    ('OWNER_EXECUTION_APPROVAL', 'INDEPENDENT_SOURCE_AND_INSTANCE_REVIEW')):
        dirs.tick()
        accepted = strict_json(captured[str(path)])
        require(accepted['format'] == 'passvault-linux-database-closeout-acceptance-v1'
                and accepted['reviewer'] == reviewer and accepted['review_role'] == role
                and accepted['helper_authors'] == ['/root/editor']
                and accepted['design_contributors'] == ['/root', '/root/baseline_coverage']
                and reviewer not in accepted['helper_authors']
                and accepted['purpose'] == 'ONE_SHOT_ORIGINAL_LINUX_DATABASE_GENERATED_OUTPUT_CLOSEOUT'
                and accepted['disposition'] == 'ACCEPT'
                and accepted['bindings'] == {**bindings, 'request_sha256': digests[str(REQUEST)]}
                and accepted['runtime'] == str(R) and accepted['evidence'] == str(E)
                and accepted['commit'] == COMMIT and accepted['tree'] == TREE
                and accepted['obligations'] == {f'C{i:02}': 'ACCEPT' for i in range(1, 8)}
                and accepted['limits_acknowledged'] == ACKNOWLEDGEMENTS, 'actual closeout execution acceptance absent')
    run_request, result, original, launcher, events, source, owned = run_contract(captured, pins, dirs.tick)
    targets = strict_json(captured[str(FILES['targets_sha256'])])
    require(targets['format'] == 'passvault-linux-database-closeout-targets-v1' and targets['author'] == '/root'
            and targets['targets'] == original_targets(events, original, dirs.tick), 'fixed 33 original targets binding')
    evidence = strict_json(captured[str(FILES['evidence_manifest_sha256'])])
    require(evidence['format'] == 'passvault-linux-database-closeout-evidence-v1' and evidence['author'] == '/root'
            and evidence['evidence'] == str(E) and evidence['runtime'] == str(R), 'evidence manifest identity')
    require(request['lock_pin'] == run_request['lock_pin'], 'original run lock must be retained')
    needed_paths = [p.parent for p in paths + LAUNCHER_FILES] + [LOCK.parent, E, *TARGET_PATHS]
    needed_paths += [(CHECKOUT / row['path']).parent for row in source['files']]
    python = run_request['toolchain']['python']
    require(python['path'] == os.path.realpath(sys.executable), 'original interpreter identity')
    needed_paths.append(Path(python['path']).parent)
    # The runner recorded these XML parent observations separately from top allocation originals.
    xml_names = [row['name'] for row in evidence['files'] if row['name'] == 'XML-INVENTORY.json']
    xml_inventory = None
    if xml_names:
        data, pin, digest = dirs.read(E / 'XML-INVENTORY.json', LIMITS['control_bytes'], bootstrap=True)
        captured[str(E / 'XML-INVENTORY.json')], pins[str(E / 'XML-INVENTORY.json')] = data, pin
        digests[str(E / 'XML-INVENTORY.json')] = digest
        xml_inventory = strict_json(data)
        if xml_inventory:
            needed_paths.append(CHECKOUT / 'core/database/build/test-results/desktopTest')
    expected_dirs = request['directory_pins']
    require(set(expected_dirs) == ancestors(needed_paths, dirs.tick), 'missing/extra closeout original directory pins')
    for path, pin in expected_dirs.items():
        dirs.tick()
        if path in original:
            require(pin == original[path], 'original run parent/directory identity substituted')
        elif path in run_request['directory_pins']:
            require(pin == run_request['directory_pins'][path], 'original run admission parent substituted')
        else:
            require(not under(path, str(R)), 'unrecorded runtime parent may not be adopted')
    dirs.bind(expected_dirs)
    require(dirs.pins[str(E)] == run_request['directory_pins'][str(E)], 'original evidence directory identity')
    policy = request['device_policy']
    require(policy == {'directory_dev': original[str(R)]['dev'],
                       'regular_file_dev': pins[str(FILES['run_result_sha256'])]['dev']}, 'separate original device policy')
    require(all(row['identity']['dev'] == policy['directory_dev'] for row in targets['targets']), 'top directory device drift')
    data, pin, digest = dirs.read(python['path'], 32 * MIB)
    require(pin == python['identity'] and digest == python['sha256'], 'original interpreter bytes changed')
    captured[python['path']], pins[python['path']] = data, pin
    for row in launcher['evidence_files']:
        data, pin, digest = dirs.read(row['path'], MIB, durable=True)
        require(pin == row['identity'] and digest == row['sha256'], 'durable launcher evidence mismatch')
        captured[row['path']], pins[row['path']] = data, pin
    return request, captured, pins, bindings, run_request, result, events, source, owned, evidence, xml_inventory


def evidence_check(dirs, evidence, events, result, xml_inventory, journal_exists):
    rows = evidence['files']
    require(len(rows) <= LIMITS['evidence_files'] and len({r['name'] for r in rows}) == len(rows), 'evidence file set cap')
    allowed = {'JOURNAL.jsonl', 'RESULT.json', 'ORIGINAL-DIRECTORIES.json', 'XML-INVENTORY.json'}
    allowed |= {label + '.log' for label in LABELS}
    total = 0
    by_name = {}
    for row in rows:
        name = row['name']
        require(relative(name).name == name, 'evidence leaf only')
        require(name in allowed or (name.startswith('xml-TEST-') and name.endswith('.xml')), 'unknown run evidence file')
        cap = 8 * MIB if name == 'JOURNAL.jsonl' else 4 * MIB
        if name.startswith('xml-TEST-'):
            cap = 2 * MIB
        _, pin, digest = dirs.read(E / name, cap, keep=False, durable=True)
        require(pin == row['identity'] and digest == row['sha256'], 'sealed run evidence changed')
        total += pin['size']
        require(total <= LIMITS['evidence_bytes'], 'aggregate preserved evidence cap')
        by_name[name] = row
    require({'JOURNAL.jsonl', 'RESULT.json', 'ORIGINAL-DIRECTORIES.json'} <= set(by_name), 'essential run evidence missing')
    require(set(os.listdir(dirs.open(E))) == set(by_name) | ({JOURNAL_PATH.name} if journal_exists else set()),
            'evidence namespace unknown/consumed member')
    for command in result['commands']:
        row = by_name.get(command['label'] + '.log')
        require(row is not None and row['sha256'] == command['log_sha256']
                and row['identity']['size'] == command['log_bytes'], 'exact command log preservation missing')
    if xml_inventory is None:
        require(evidence['xml_inventory_disposition'] == 'ABSENT_RECORDED_XML_DIRECTORY_UNAVAILABLE'
                and 'XML-INVENTORY.json' not in by_name
                and not any(name.startswith('xml-TEST-') for name in by_name)
                and any(row['kind'] == 'error' and row['reason'].startswith('XML directory unavailable: ')
                        for row in events), 'unproved XML inventory absence')
    else:
        require(evidence['xml_inventory_disposition'] == 'PRESENT' and len(xml_inventory) <= 32,
                'XML inventory disposition/count')
        require(len({row['name'] for row in xml_inventory}) == len(xml_inventory), 'duplicate XML preservation row')
        require({name for name in by_name if name.startswith('xml-TEST-')}
                == {'xml-' + row['name'] for row in xml_inventory}, 'XML copy/inventory mismatch')
        for row in xml_inventory:
            name = row['name']
            require(relative(name).name == name and name.startswith('TEST-') and name.endswith('.xml'), 'XML leaf name')
            copy = by_name['xml-' + name]
            require(row['preserved'] == {'sha256': copy['sha256'], 'identity': copy['identity']}, 'XML preserved original mismatch')
            _, pin, digest = dirs.read(CHECKOUT / 'core/database/build/test-results/desktopTest' / name, 2 * MIB, keep=False)
            require(pin == row['original'] and digest == copy['sha256'], 'original XML not preserved in place')
    return {'files': len(rows), 'logical_bytes': total, 'xml_inventory_disposition': evidence['xml_inventory_disposition']}


def source_check(dirs, source):
    total = 0
    for row in source['files']:
        _, pin, digest = dirs.read(CHECKOUT / row['path'], LIMITS['source_file_bytes'], keep=False)
        total += pin['size']
        require(total <= LIMITS['source_bytes'], 'source aggregate read cap')
        require(digest == row['checkout_sha256'] and pin['size'] == row['checkout_size']
                and bool(pin['mode'] & 0o111) == (row['git_mode'] == '100755'), 'tracked checkout source drift')
    return {'files': 1198, 'logical_bytes': total, 'commit': COMMIT, 'tree': TREE,
            'checkout_eol_qualifications': source['checkout_eol_qualifications']}


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


def recheck_inputs(dirs, captured, pins):
    for path, data in captured.items():
        _, pin, digest = dirs.read(path, max(len(data), 1), keep=False)
        require(pin == pins[path] and digest == sha(data), 'admitted immutable input changed')


def main():
    require(sys.platform == 'linux' and sys.argv == [str(SELF)] and Path(__file__) == SELF
            and sys.flags.isolated and sys.dont_write_bytecode, 'fixed /usr/bin/python3 -I -B entry only')
    os.umask(0o077)
    for sig in SIGNALS:
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    started = time.monotonic()

    def bootstrap_tick():
        require(not CANCEL and not signal.sigpending() & SIGNALS, 'bootstrap cancellation observed')
        require(time.monotonic() - started <= LIMITS['total_seconds'], 'bootstrap closeout time cap')

    bootstrap_tick()
    resource.setrlimit(resource.RLIMIT_AS, (LIMITS['address_space_bytes'], LIMITS['address_space_bytes']))
    dirs, lock_fd, journal, forest = Directories(bootstrap_tick), None, None, None
    phase, code = 'admission', 1
    try:
        (request, captured, pins, bindings, run_request, result, events, source,
         owned, evidence, xml_inventory) = admission(dirs)
        bootstrap_tick()
        parent = dirs.open(LOCK.parent)
        lock_pin = request['lock_pin']
        require(lock_pin['uid'] == UID and lock_pin['nlink'] == 1 and lock_pin['mode'] == stat.S_IFREG | 0o600,
                'original private regular lock')
        require(identity(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)) == lock_pin, 'lock original changed')
        lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        require(identity(os.fstat(lock_fd)) == lock_pin, 'opened lock original changed')
        fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        bootstrap_tick()
        dirs.verify()
        recheck_inputs(dirs, captured, pins)
        require(time.time() <= request['expires_unix_seconds'] and not CANCEL
                and not signal.sigpending() & SIGNALS, 'expired/cancelled under-lock admission')
        # The exclusive journal is the sole new output and consumes this one-shot attempt.
        bootstrap_tick()
        journal = Journal(dirs)
        journal.event('closeout_admission_under_original_lock', bindings={**bindings, 'request_sha256': sha(captured[str(REQUEST)])},
                      lock_pin=lock_pin, runtime=str(R), evidence=str(E), limits=LIMITS)
        guard = Guard(dirs, started, lock_fd, lock_pin, owned, journal)
        dirs.tick = guard.tick
        guard.tick(force=True, launch=True)
        phase = 'all_tree_preflight'
        preserved = evidence_check(dirs, evidence, events, result, xml_inventory, True)
        journal.event('original_run_evidence_preserved', **preserved)
        journal.event('source_before', **source_check(dirs, source))
        forest = Forest(dirs, guard, request['device_policy'])
        journal.event('all_target_preflight_complete', **forest.preflight())
        for path, node, summary in forest.snapshots:
            guard.tick()
            journal.event('target_deletion_intent', path=str(path), original=node.pin, **summary)
        # All trees, identities, bounded inventories and all intents precede the FIRST unlink.
        dirs.verify()
        recheck_inputs(dirs, captured, pins)
        evidence_check(dirs, evidence, events, result, xml_inventory, True)
        guard.tick(force=True)
        require(time.time() <= request['expires_unix_seconds'], 'admission expired before first deletion')
        phase = 'descriptor_bound_closeout'
        for path, node, summary in forest.snapshots:
            forest.close_target(path, node, summary)
        phase = 'after_proofs'
        dirs.verify()
        for path, node, _ in forest.snapshots:
            if node.retained:
                forest.verify_retained(path, node, dirs.open(path.parent), [])
            else:
                require(path.name not in os.listdir(dirs.open(path.parent)), 'removed target reappeared at final proof')
        recheck_inputs(dirs, captured, pins)
        journal.event('source_after', **source_check(dirs, source))
        journal.event('original_run_evidence_retained_after', **evidence_check(dirs, evidence, events, result, xml_inventory, True))
        guard.tick(force=True)
        # Defined terminal boundary. Signals pending before this sample forbid success.
        # Later arrivals, SIGKILL, host loss and postcommit descriptor-close errors remain external limitations.
        signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        CANCEL.update(signal.sigpending() & SIGNALS)
        guard.cheap()
        journal.event('terminal_commit', exit=0, outcome='BOUNDED_CLOSEOUT_COMPLETED_NOT_APPLICATION_PASS',
                      targets=33, original_runtime_checkout_retained=True,
                      observed_removed=forest.observed_removed,
                      accounting='LOGICAL_AND_STAT_BLOCK_ACCOUNTING_NOT_MEASURED_NET_RECLAIMED_SPACE')
        code = 0
    except BaseException as error:
        if journal is not None and journal.ok:
            try:
                journal.event('terminal_hold', exit=1, phase=phase, error_type=type(error).__name__,
                              reason=str(error) if isinstance(error, Hold) else 'operation failed; original state retained without retry',
                              cancelled_signals=sorted(CANCEL),
                              observed_removed_not_all_durability_proven=forest.observed_removed if forest else {},
                              disposition='CONSUMED_HOLD_NO_AUTOMATIC_RETRY_ADOPTION_OR_RECOVERY')
            except BaseException:
                pass
        os.write(2, ('CLOSEOUT_HOLD_NO_RETRY phase=' + phase + ' type=' + type(error).__name__ + '\n').encode())
    finally:
        # Close only descriptors this process opened. Never issue --stop, signals or path cleanup.
        failures = []
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
            os.write(2, b'CLOSEOUT_DESCRIPTOR_SETTLEMENT_HOLD; terminal record alone is insufficient\n')
    os._exit(code)


if __name__ == '__main__':
    main()
