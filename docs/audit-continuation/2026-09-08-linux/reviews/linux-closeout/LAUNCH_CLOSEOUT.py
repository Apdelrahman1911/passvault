#!/usr/bin/python3
"""One separately admitted direct closer; source proposal, not execution authority.

Never import a helper or launch an alternative/retry. Only the original unreaped
Popen child can receive a signal. This fixed closer launches no descendants/stop.
Four exclusive OUTER evidence files stay; root observes the actual external exit.
Derived by source-only editing of the accepted NEW ee46cfd9 database supervisor,
not imported/replayed and not any old G7/G8 execution or recovery helper.
"""

import fcntl
import hashlib
import json
import os
from pathlib import Path
import select
import signal
import stat
import subprocess
import sys
import time


BASE = Path('/root/projects/PassVault')
W = BASE / 'passvault-linux'
C = W / 'docs/audit-continuation/2026-09-08-linux/reviews/linux-closeout'
RUN = W / 'docs/audit-continuation/2026-09-08-linux/reviews/linux-runner'
SELF, PLAN = C / 'LAUNCH_CLOSEOUT.py', C / 'CLOSEOUT-OUTER-PLAN.md'
CLOSER, CLOSER_PLAN = W / 'scripts/audit/linux_database_closeout.py', C / 'PLAN.md'
REQUEST, INNER_REQUEST = C / 'OUTER-REQUEST.json', C / 'REQUEST.json'
INNER_APPROVALS = (C / 'ACCEPT-root.json', C / 'ACCEPT-baseline_coverage.json')
RUNNER, RUNNER_PLAN = W / 'scripts/audit/linux_database_validation.py', RUN / 'PLAN.md'
RUN_REQUEST, SOURCE = RUN / 'REQUEST.json', RUN / 'SOURCE.json'
RUN_APPROVALS = (RUN / 'ACCEPT-root.json', RUN / 'ACCEPT-verification.json')
RUN_LAUNCHER = RUN / 'LAUNCHER.json'
RUN_EVIDENCE_PATHS = tuple(RUN / name for name in (
    'LAUNCHER-stdout.log', 'LAUNCHER-stderr.log', 'LAUNCHER-resources.json', 'LAUNCHER-processes.json',
))
METHODS = W / 'docs/audit-continuation/2026-09-08-linux/reviews/verification/METHOD-INVENTORY.json'
APPROVALS = (C / 'OUTER-ACCEPT-root.json', C / 'OUTER-ACCEPT-baseline_coverage.json')
PYTHON = Path('/usr/bin/python3.12')
LOCK = BASE / '.audit-coordination-linux-20260908/build.lock'
R = BASE / 'audit-runtime-linux-db-01'
E = W / 'docs/audit-continuation/2026-09-08-linux/runs/linux-database-01'
CHECKOUT = R / 'checkout'
CLOSEOUT_JOURNAL = E / 'CLOSEOUT-JOURNAL.jsonl'
RECEIPT = C / 'OUTER-LAUNCHER.json'
INNER_FILES = {
    'helper_sha256': CLOSER, 'plan_sha256': CLOSER_PLAN,
    'targets_sha256': C / 'TARGETS.json', 'evidence_manifest_sha256': C / 'EVIDENCE.json',
    'runner_sha256': RUNNER, 'runner_plan_sha256': RUNNER_PLAN, 'runner_request_sha256': RUN_REQUEST,
    'source_sha256': SOURCE, 'methods_sha256': METHODS,
    'runner_accept_root_sha256': RUN_APPROVALS[0], 'runner_accept_verification_sha256': RUN_APPROVALS[1],
    'launcher_sha256': RUN_LAUNCHER, 'run_journal_sha256': E / 'JOURNAL.jsonl',
    'run_result_sha256': E / 'RESULT.json', 'run_originals_sha256': E / 'ORIGINAL-DIRECTORIES.json',
}
OUTPUTS = tuple(C / name for name in (
    'OUTER-stdout.log', 'OUTER-stderr.log', 'OUTER-resources.json', 'OUTER-processes.json',
))
COMMIT = '9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed'
TREE = '05014e9f635131d5db06701e4013b4b5a746465a'
RUNNER_SHA = '346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279'
RUNNER_PLAN_SHA = '74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f'
METHODS_SHA = '40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979'
CLOSER_SHA = '8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517'
CLOSER_PLAN_SHA = '0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d'
ARGV = ['/usr/bin/python3', '-I', '-B', str(CLOSER)]
ENTRY = ['/usr/bin/python3', '-I', '-B', str(SELF)]
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
SIGNALS = {signal.SIGINT, signal.SIGTERM, signal.SIGHUP}
PURPOSE = 'ONE_ORIGINAL_LINUX_DATABASE_CLOSEOUT_OUTER_SUPERVISOR'
ACK = [
    'ONE_ORIGINAL_DIRECT_CHILD_NO_DESCENDANT_AUTHORITY',
    'CLOSER_HAS_NO_SUBPROCESS_OR_STOP_NO_PARENT_STOP_RETRY_OR_ADOPTION',
    'ORIGINAL_RUN_AND_SEPARATE_POSTRUN_CLOSEOUT_ACCEPTANCES_UNCHANGED',
    'COOPERATIVE_SLOT_AND_SOURCE_FREEZE_NOT_HOST_SANDBOX',
    'POINT_SAMPLED_RESOURCES_AND_SOFT_SYSCALL_DEADLINES',
    'FORCED_INTERRUPTED_OR_UNSETTLED_EXIT_IS_HOLD',
    'ROOT_ACTUAL_TOOL_EXIT_AND_INDEPENDENT_CLOSEOUT_OUTCOME_REVIEW_REQUIRED',
]
INNER_LIMITS = {
    'total_seconds': 900, 'admission_validity_seconds': 3600,
    'files': 200000, 'directories': 30000, 'depth': 64, 'logical_bytes': 6 * 1024 ** 3,
    'address_space_bytes': 512 * 1024 ** 2, 'journal_bytes': 8 * 1024 ** 2,
    'control_bytes': 4 * 1024 ** 2, 'evidence_bytes': 80 * 1024 ** 2, 'evidence_files': 64,
    'source_bytes': 256 * 1024 ** 2, 'source_file_bytes': 32 * 1024 ** 2,
    'mountinfo_bytes': 2 * 1024 ** 2, 'processes': 65536, 'poll_seconds': 5,
    'launch_free_bytes': 12 * 1024 ** 3, 'running_free_bytes': 8 * 1024 ** 3,
    'launch_memory_fraction': 0.25, 'running_memory_fraction': 0.20,
}
INNER_ACK = [
    'COOPERATIVE_NAMESPACE_NOT_HOSTILE_UID_SANDBOX',
    'POINT_SAMPLED_PROCESS_RESOURCE_MOUNT_EVIDENCE',
    'ALL_33_ORIGINAL_TOP_ALLOCATIONS_REQUIRED_NO_ADOPTION',
    'DESCENDANT_SNAPSHOTS_ARE_POST_RUN_OBSERVATIONS_NOT_CREATION_PROOF',
    'RETAIN_RUNTIME_CHECKOUT_SOURCE_SCHEMAS_GIT_REPORTS_AND_TEST_RESULTS',
    'NO_COMMAND_SIGNAL_STOP_RETRY_OR_AUTOMATIC_RECOVERY',
    'PARTIAL_INTERRUPTED_UNCERTAIN_CLOSEOUT_IS_CONSUMED_HOLD',
    'NO_APPLICATION_PASS_CLOSURE_OR_HARDWARE_CREDIT',
]
LIMITS = {
    'bootstrap_seconds': 120, 'child_main_seconds': 900,
    'child_setup_and_settlement_margin_seconds': 120, 'outer_soft_seconds': 1020,
    'cleanup_grace_seconds': 120, 'kill_wait_seconds': 60,
    'pipe_after_exit_seconds': 30, 'postcheck_seconds': 120,
    'sample_seconds': 5, 'file_bytes': 1024 * 1024,
    'input_bytes': 4 * 1024 * 1024, 'run_journal_bytes': 8 * 1024 * 1024,
    'python_bytes': 32 * 1024 * 1024, 'admission_validity_seconds': 3600,
    'launch_free_bytes': 12 * 1024 ** 3, 'running_free_bytes': 8 * 1024 ** 3,
    'launch_memory_fraction': 0.25, 'running_memory_fraction': 0.20,
}
CANCEL = set()
UID = os.getuid()


class Hold(Exception):
    pass


def require(condition, message):
    if not condition:
        raise Hold(message)


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True).encode() + b'\n'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def parse(data):
    def pairs(items):
        result = {}
        for key, value in items:
            require(key not in result, 'duplicate JSON key')
            result[key] = value
        return result
    return json.loads(data, object_pairs_hook=pairs,
                      parse_constant=lambda _: (_ for _ in ()).throw(Hold('nonfinite JSON')))


def pin(st, directory=False):
    names = ('dev', 'ino', 'uid', 'mode') if directory else (
        'dev', 'ino', 'uid', 'mode', 'nlink', 'size', 'mtime_ns', 'ctime_ns')
    return {name: getattr(st, 'st_' + name) for name in names}


def absent(name, parent):
    try:
        os.stat(name, dir_fd=parent, follow_symlinks=False)
    except FileNotFoundError:
        return True
    return False


def input_cap(path):
    if path == PYTHON:
        return LIMITS['python_bytes']
    if path == INNER_FILES['run_journal_sha256']:
        return LIMITS['run_journal_bytes']
    return LIMITS['input_bytes']


def fresh(request):
    start, end = request['created_unix_seconds'], request['expires_unix_seconds']
    require(type(start) is int and type(end) is int and start <= time.time() <= end
            and 0 < end - start <= LIMITS['admission_validity_seconds'], 'fresh prospective admission interval')


class Originals:
    def __init__(self):
        self.fds, self.pins = {}, {}
        self.deadline = time.monotonic() + LIMITS['bootstrap_seconds']
        self.preadmission = True

    def check_time(self):
        require(time.monotonic() <= self.deadline, 'bounded input/admission read time')
        if self.preadmission:
            require(not CANCEL and not signal.sigpending() & SIGNALS, 'cancelled before launch')

    def directory(self, path, discover=False):
        require(path.is_absolute() and '..' not in path.parts, 'fixed absolute directory')
        key = str(path)
        parent = self.directory(path.parent, discover) if path != Path('/') else None
        if key not in self.fds:
            require(discover, 'missing original directory authority')
            fd = os.open(path.name if parent is not None else '/',
                         os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
            try:
                original = pin(os.fstat(fd), True)
                require(stat.S_ISDIR(original['mode']), 'original directory type')
                self.fds[key], self.pins[key] = fd, original
            except BaseException:
                # Unpublish only this still-local FD before its sole close attempt.
                if self.fds.get(key) == fd:
                    self.fds.pop(key)
                os.close(fd)
                raise
        fd, original = self.fds[key], self.pins[key]
        require(pin(os.fstat(fd), True) == original, 'original directory descriptor drift')
        if parent is not None:
            require(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True) == original,
                    'original directory pathname drift')
        return fd

    def verify(self):
        for path in tuple(self.fds):
            self.directory(Path(path))

    def read(self, path, discover=False, cap=None):
        self.check_time()
        cap = LIMITS['input_bytes'] if cap is None else cap
        parent = self.directory(path.parent, discover)
        before = pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
        require(stat.S_ISREG(before['mode']) and before['uid'] == UID and before['nlink'] == 1
                and not before['mode'] & 0o022 and before['size'] <= cap, 'bounded owned single-link input')
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        try:
            require(pin(os.fstat(fd)) == before, 'input changed before opened image')
            pieces, size = [], 0
            while True:
                self.check_time()
                piece = os.read(fd, min(65536, cap + 1 - size))
                if not piece:
                    break
                pieces.append(piece)
                size += len(piece)
                require(size <= cap, 'input byte cap')
            require(before == pin(os.fstat(fd))
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input changed after read')
            require(size == before['size'], 'short input image')
            self.directory(path.parent)
            return b''.join(pieces), before
        finally:
            os.close(fd)

    def close(self):
        errors = []
        for key in reversed(tuple(self.fds)):
            fd = self.fds.pop(key)
            try:
                os.close(fd)
            except BaseException as error:
                errors.append(type(error).__name__)
        return errors


class Evidence:
    """Only a fixed C/OUTER leaf; failed writes are retained, never repaired/adopted."""
    def __init__(self, originals, path):
        require(path in OUTPUTS, 'fixed evidence allowlist')
        self.d, self.path = originals, path
        self.fd, self.failed, self.records = None, False, 0
        self.used = 0
        parent = originals.directory(C)
        fd = os.open(path.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                     0o600, dir_fd=parent)
        try:
            self.original = pin(os.fstat(fd))
            require(self.original['uid'] == UID and self.original['mode'] == stat.S_IFREG | 0o600
                    and self.original['nlink'] == 1 and self.original['size'] == 0, 'exclusive evidence original')
            require(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)) == self.original,
                    'new evidence path does not name original')
            os.fsync(fd)
            os.fsync(parent)
            self.last, self.fd = self.original, fd
        except BaseException:
            os.close(fd)
            raise

    def append(self, data):
        require(not self.failed and self.fd is not None, 'evidence already incomplete/closed')
        try:
            require(self.used + len(data) <= LIMITS['file_bytes'], 'launcher evidence file cap')
            parent = self.d.directory(C)
            require(pin(os.fstat(self.fd)) == self.last
                    == pin(os.stat(self.path.name, dir_fd=parent, follow_symlinks=False)), 'evidence preappend drift')
            require(self.last['size'] == self.used, 'evidence prior completed length')
            view = memoryview(data)
            while view:
                written = os.write(self.fd, view)
                require(written > 0, 'evidence zero write')
                view = view[written:]
            os.fsync(self.fd)
            after = pin(os.fstat(self.fd))
            require(after == pin(os.stat(self.path.name, dir_fd=parent, follow_symlinks=False)),
                    'evidence postappend path drift')
            require(all(after[key] == self.original[key] for key in ('dev', 'ino', 'uid', 'mode', 'nlink'))
                    and after['size'] == self.used + len(data), 'evidence postappend original/length')
            self.last, self.used = after, self.used + len(data)
        except BaseException:
            self.failed = True
            raise

    def begin(self):
        self.append(b'{"format":"passvault-linux-database-closeout-outer-evidence-v1","records":[\n')

    def event(self, value):
        self.append((b',' if self.records else b'') + canonical(value))
        self.records += 1

    def finish(self, summary):
        self.append(b'],"preterminal":' + canonical(summary).rstrip(b'\n') + b'}\n')

    def close(self):
        if self.fd is not None:
            fd, self.fd = self.fd, None
            os.close(fd)


def resources(d, launch=False):
    with open('/proc/meminfo', 'rb') as stream:
        raw = stream.read(65537)
    require(len(raw) <= 65536, 'meminfo observation cap')
    memory = {}
    for line in raw.splitlines():
        fields = line.split()
        if fields and fields[0] in (b'MemTotal:', b'MemAvailable:'):
            memory[fields[0][:-1].decode()] = int(fields[1]) * 1024
    available = {}
    for name, path in (('base', BASE), ('evidence', E)):
        stats = os.fstatvfs(d.directory(path))
        available[name] = stats.f_bavail * stats.f_frsize
    fraction = memory['MemAvailable'] / memory['MemTotal']
    prefix = 'launch' if launch else 'running'
    return {'unix_ns': time.time_ns(), 'disk_available_bytes': available, 'memory_bytes': memory,
            'available_fraction': fraction,
            'floor_ok': min(available.values()) >= LIMITS[prefix + '_free_bytes']
            and fraction >= LIMITS[prefix + '_memory_fraction']}


def direct_row(pid):
    """Only this parent's known PID or its unreaped direct Popen PID; no scan."""
    path = Path('/proc') / str(pid)
    rows = []
    for _ in range(2):
        with open(path / 'stat', 'rb') as stream:
            raw = stream.read(8193)
        require(len(raw) <= 8192, 'owned proc-stat cap')
        fields = raw[raw.rfind(b')') + 2:].split()
        rows.append({'pid': pid, 'start_ticks': int(fields[19]), 'ppid': int(fields[1]),
                     'uid': os.stat(path).st_uid, 'state': fields[0].decode('ascii')})
    require(all(rows[0][key] == rows[1][key] for key in ('pid', 'start_ticks', 'ppid', 'uid')),
            'direct process birth/parent/uid changed during capture')
    return rows[1]


def admission(d):
    captures = {}
    fixed_inputs = (SELF, PLAN, *INNER_FILES.values(), INNER_REQUEST, *INNER_APPROVALS)
    for path in (REQUEST, *fixed_inputs, *APPROVALS):
        captures[path] = d.read(path, True, input_cap(path))
    request = parse(captures[REQUEST][0])
    inner = parse(captures[INNER_REQUEST][0])
    run = parse(captures[RUN_REQUEST][0])
    require(request['format'] == 'passvault-linux-database-closeout-outer-request-v1'
            and request['author'] == '/root' and request['purpose'] == PURPOSE,
            'separate actual outer closeout request; never alter consumed main requests')
    require(request['run_id'] == 'linux-database-01' and request['commit'] == COMMIT and request['tree'] == TREE,
            'fixed run/source identity')
    require(request['runtime'] == str(R) and request['evidence'] == str(E) and request['lock'] == str(LOCK),
            'fixed original paths')
    require(request['no_other_audit_local_or_ci_job'] is True
            and request['cooperative_producers_frozen'] is True and request['source_and_inputs_frozen'] is True
            and request['slot_owner'] == '/root', 'fresh root sole-slot and frozen-producer attestations')
    fresh(request)
    hashes = {path: sha(value[0]) for path, value in captures.items()}
    require(hashes[CLOSER] == CLOSER_SHA and hashes[CLOSER_PLAN] == CLOSER_PLAN_SHA,
            'only the accepted new fixed closer and its unchanged plan')
    require(hashes[RUNNER] == RUNNER_SHA and hashes[RUNNER_PLAN] == RUNNER_PLAN_SHA
            and hashes[METHODS] == METHODS_SHA, 'frozen runner/plan/methods')
    inner_bindings = {key: hashes[path] for key, path in INNER_FILES.items()}
    require(inner['format'] == 'passvault-linux-database-closeout-request-v1' and inner['author'] == '/root'
            and inner['run_id'] == 'linux-database-01' and inner['runtime'] == str(R)
            and inner['evidence'] == str(E) and inner['lock'] == str(LOCK)
            and inner['commit'] == COMMIT and inner['tree'] == TREE, 'actual original closeout instance')
    require(inner['command'] == ARGV and inner['limits'] == INNER_LIMITS
            and inner['no_other_audit_local_or_ci_job'] is True and inner['cooperative_producers_frozen'] is True,
            'unchanged fixed closeout command/bounds/coordination')
    fresh(inner)
    require(inner['bindings'] == inner_bindings
            and inner['input_pins'] == {str(path): captures[path][1] for path in INNER_FILES.values()},
            'actual original closeout source/input byte and identity bindings')
    for path, reviewer, role in zip(
            INNER_APPROVALS, ('/root', '/root/baseline_coverage'),
            ('OWNER_EXECUTION_APPROVAL', 'INDEPENDENT_SOURCE_AND_INSTANCE_REVIEW')):
        accept = parse(captures[path][0])
        require(accept['format'] == 'passvault-linux-database-closeout-acceptance-v1'
                and accept['reviewer'] == reviewer and accept['review_role'] == role
                and accept['helper_authors'] == ['/root/editor']
                and accept['design_contributors'] == ['/root', '/root/baseline_coverage']
                and accept['purpose'] == 'ONE_SHOT_ORIGINAL_LINUX_DATABASE_GENERATED_OUTPUT_CLOSEOUT'
                and accept['disposition'] == 'ACCEPT'
                and accept['bindings'] == {**inner_bindings, 'request_sha256': hashes[INNER_REQUEST]}
                and accept['obligations'] == {f'C{i:02}': 'ACCEPT' for i in range(1, 8)}
                and accept['limits_acknowledged'] == INNER_ACK, 'actual postrun closeout acceptance required')
        require(accept['runtime'] == str(R) and accept['evidence'] == str(E)
                and accept['commit'] == COMMIT and accept['tree'] == TREE, 'accepted original closeout paths/source')
    require(run['format'] == 'passvault-linux-database-request-v1' and run['author'] == '/root'
            and run['run_id'] == 'linux-database-01' and run['runtime'] == str(R) and run['evidence'] == str(E)
            and run['lock'] == str(LOCK) and run['commit'] == COMMIT and run['tree'] == TREE,
            'actual completed original run request identity')
    run_bindings = {
        'runner_sha256': hashes[RUNNER], 'plan_sha256': hashes[RUNNER_PLAN],
        'request_sha256': hashes[RUN_REQUEST], 'source_sha256': hashes[SOURCE], 'methods_sha256': hashes[METHODS],
        'command_sha256': sha(canonical(run['command'])),
        'stop_command_sha256': sha(canonical(run['stop_command'])),
        'environment_sha256': sha(canonical(run['environment'])),
    }
    require(run['bindings'] == {key: value for key, value in run_bindings.items() if key != 'request_sha256'}
            and run['input_pins'] == {str(path): captures[path][1] for path in (RUNNER, RUNNER_PLAN, SOURCE, METHODS)},
            'completed original run immutable input relationship')
    for path, reviewer, role in zip(
            RUN_APPROVALS, ('/root', '/root/verification'), ('OWNER_COAUTHOR_APPROVAL', 'INDEPENDENT_SOURCE_REVIEW')):
        accept = parse(captures[path][0])
        require(accept['format'] == 'passvault-linux-database-acceptance-v1' and accept['reviewer'] == reviewer
                and accept['review_role'] == role and accept['runner_authors'] == ['/root/storage', '/root']
                and accept['purpose'] == 'ONE_SHOT_LINUX_DATABASE_EXECUTION_AND_RETAINED_CLOSEOUT_CONTRACT'
                and accept['disposition'] == 'ACCEPT' and accept['bindings'] == run_bindings
                and accept['obligations'] == {f'F{i:02}': 'ACCEPT' for i in range(1, 8)},
                'actual original run acceptance')
        require(accept['runtime'] == str(R) and accept['evidence'] == str(E)
                and accept['commit'] == COMMIT and accept['tree'] == TREE, 'original run accepted instance')
    launcher = parse(captures[RUN_LAUNCHER][0])
    require(launcher['format'] == 'passvault-linux-database-launcher-v1' and launcher['author'] == '/root'
            and launcher['run_id'] == 'linux-database-01' and launcher['runtime'] == str(R)
            and launcher['evidence'] == str(E)
            and launcher['argv'] == ['/usr/bin/python3', '-I', '-B', str(RUNNER)]
            and launcher['completed'] is True and type(launcher['runner_exit']) is int
            and launcher['runner_exit'] in (0, 1) and type(launcher['outer_exit']) is int
            and launcher['outer_exit'] in (0, 1) and launcher['no_other_audit_local_or_ci_job'] is True
            and launcher['external_timeout_observed'] is False and launcher['external_interruption_observed'] is False,
            'completed actual original external run receipt')
    require(launcher['bindings'] == {
        **run_bindings, 'accept_root_sha256': hashes[RUN_APPROVALS[0]],
        'accept_verification_sha256': hashes[RUN_APPROVALS[1]],
    } and isinstance(launcher['limitations'], list) and launcher['limitations'], 'original external run binding/limits')
    run_evidence = launcher['evidence_files']
    require(isinstance(run_evidence, list) and len(run_evidence) == len(RUN_EVIDENCE_PATHS)
            and all(isinstance(row, dict) and set(row) == {'path', 'sha256', 'identity'} for row in run_evidence)
            and [row['path'] for row in run_evidence] == [str(path) for path in RUN_EVIDENCE_PATHS],
            'unambiguous original external evidence_files list, distinct from evidence directory')
    # Full journal/stop/worker/evidence semantics are independently admitted and
    # revalidated by the fixed closer before deletion, not inferred here from RESULT.
    source = parse(captures[SOURCE][0])
    require(source['format'] == 'passvault-linux-checkout-source-v1' and source['commit'] == COMMIT
            and source['tree'] == TREE and len(source['files']) == 1198, 'actual source-manifest identity')
    require(Path(os.path.realpath(sys.executable)) == PYTHON
            and Path(os.path.realpath('/usr/bin/python3')) == PYTHON, 'fixed resolved Python executable')
    captures[PYTHON] = d.read(PYTHON, True, LIMITS['python_bytes'])
    require(run['toolchain']['python'] == {
        'path': str(PYTHON), 'sha256': sha(captures[PYTHON][0]), 'identity': captures[PYTHON][1],
    } and not captures[PYTHON][1]['mode'] & (stat.S_ISUID | stat.S_ISGID), 'original Python bytes/identity')
    for path in (BASE, E, R, CHECKOUT, LOCK.parent):
        d.directory(path, True)
    require(all(inner['directory_pins'].get(path) == original for path, original in d.pins.items()),
            'closeout-sealed original ancestors, not later namespace adoption')
    require(request['directory_pins'] == d.pins, 'exact required outer directory subset of original closeout pins')
    original = parse(captures[INNER_FILES['run_originals_sha256']][0])
    require(all(original[str(path)] == d.pins[str(path)] for path in (R, CHECKOUT)),
            'runtime and checkout must be originally allocated retained directories')
    require(inner['lock_pin'] == run['lock_pin'] == request['lock_pin'], 'same original run/closeout lock')
    require(d.pins[str(E)]['uid'] == UID and d.pins[str(E)]['mode'] == stat.S_IFDIR | 0o700,
            'original private runner evidence root')
    bindings = {
        **inner_bindings, 'outer_launcher_sha256': hashes[SELF], 'outer_plan_sha256': hashes[PLAN],
        'closeout_request_sha256': hashes[INNER_REQUEST], 'closeout_accept_root_sha256': hashes[INNER_APPROVALS[0]],
        'closeout_accept_baseline_coverage_sha256': hashes[INNER_APPROVALS[1]],
        'python_sha256': sha(captures[PYTHON][0]),
    }
    require(request['bindings'] == bindings
            and request['input_pins'] == {str(path): captures[path][1] for path in (*fixed_inputs, PYTHON)},
            'separate outer request binds exact original inputs and new supervisor/plan')
    require(request['argv'] == ENTRY and request['closer_argv'] == ARGV and request['child_environment'] == ENV
            and request['limits'] == LIMITS and request['limits_acknowledged'] == ACK
            and request['evidence_paths'] == list(map(str, OUTPUTS)) and request['external_receipt'] == str(RECEIPT)
            and request['closeout_journal'] == str(CLOSEOUT_JOURNAL), 'fixed separate outer launch/evidence contract')
    bindings['outer_request_sha256'] = hashes[REQUEST]
    for path, reviewer, role in zip(
            APPROVALS, ('/root', '/root/baseline_coverage'),
            ('OWNER_EXECUTION_APPROVAL', 'INDEPENDENT_SOURCE_AND_INSTANCE_REVIEW')):
        accept = parse(captures[path][0])
        require(accept['format'] == 'passvault-linux-database-closeout-outer-acceptance-v1'
                and accept['reviewer'] == reviewer and accept['review_role'] == role
                and accept['launcher_authors'] == ['/root/editor'] and accept['design_contributors'] == ['/root']
                and accept['purpose'] == PURPOSE and accept['disposition'] == 'ACCEPT'
                and accept['bindings'] == bindings and accept['obligations'] == {f'O{i:02}': 'ACCEPT' for i in range(1, 8)}
                and accept['limits_acknowledged'] == ACK, 'actual separate outer source/instance acceptance')
        require(accept['runtime'] == str(R) and accept['evidence'] == str(E)
                and accept['commit'] == COMMIT and accept['tree'] == TREE, 'accepted outer original instance')
    bindings.update(outer_accept_root_sha256=hashes[APPROVALS[0]],
                    outer_accept_baseline_coverage_sha256=hashes[APPROVALS[1]])
    return request, inner, captures, bindings


class Supervision:
    def __init__(self, d):
        self.d, self.child, self.original = d, None, None
        self.files, self.errors, self.streams = {}, [], {}
        self.started, self.started_ns = time.monotonic(), time.time_ns()
        self.child_started = None
        self.abort_at = self.kill_at = self.exited_at = None
        self.term_attempted = self.kill_attempted = self.timed_out = False
        self.interrupted, self.omitted_errors = False, 0
        self.exit_observed = None
        self.seen, self.discarded = {'stdout': 0, 'stderr': 0}, {'stdout': 0, 'stderr': 0}

    def fail(self, reason, timeout=False):
        reason = str(reason)[:384]
        if reason not in self.errors:
            if len(self.errors) < 64:
                self.errors.append(reason)
            else:
                self.omitted_errors += 1
        self.timed_out |= timeout
        if self.child is not None and self.child.returncode is None and self.abort_at is None:
            self.abort_at = time.monotonic()
            self.interrupted = True

    def event(self, kind, **values):
        writer = self.files.get('processes')
        if writer is not None and not writer.failed and writer.fd is not None:
            try:
                writer.event({'kind': kind, 'unix_ns': time.time_ns(), **values})
            except BaseException as error:
                self.fail('process evidence incomplete: ' + type(error).__name__)

    def sample(self):
        try:
            value = resources(self.d)
            writer = self.files['resources']
            if not writer.failed:
                writer.event(value)
            if not value['floor_ok']:
                self.fail('running disk/RAM floor')
            self.d.verify()
            if self.child is not None and self.child.returncode is None and self.original is not None:
                current = direct_row(self.child.pid)
                require(all(current[key] == self.original[key] for key in ('pid', 'start_ticks', 'ppid', 'uid')),
                        'original direct child birth/parent/uid drift')
                self.event('direct_sample', observation=current)
        except BaseException as error:
            self.fail('resource/original/process sample: ' + type(error).__name__ + ': ' + str(error))

    def signal_direct(self, number):
        if number == signal.SIGTERM:
            self.term_attempted = True
        else:
            self.kill_attempted, self.kill_at = True, time.monotonic()
        try:
            if self.child.poll() is not None:
                self.event('signal_not_needed_already_waited', signal=number, exit=self.child.returncode)
                return
            require(self.original is not None, 'missing original birth; no later PID adoption')
            current = direct_row(self.child.pid)
            require(all(current[key] == self.original[key] for key in ('pid', 'start_ticks', 'ppid', 'uid'))
                    and current['uid'] == UID and current['ppid'] == os.getpid(), 'fresh original direct signal authority')
            if current['state'] == 'Z':
                self.event('original_direct_zombie_no_signal', observation=current)
                return
            self.event('direct_signal_intent', signal=number, original=self.original)
            # Popen rechecks wait state; this still-unreaped direct PID cannot have been reused.
            self.child.send_signal(number)
            self.event('direct_signal_api_returned_not_delivery_proof', signal=number)
        except BaseException as error:
            self.fail('original direct signal refused/incomplete: ' + type(error).__name__ + ': ' + str(error))

    def monitor(self):
        poller = select.poll()
        for name, stream in (('stdout', self.child.stdout), ('stderr', self.child.stderr)):
            value = {'stream': stream, 'eof': False, 'close_attempted': False, 'polling': False}
            self.streams[name] = value
            try:
                os.set_blocking(stream.fileno(), False)
                poller.register(stream.fileno(), select.POLLIN | select.POLLHUP | select.POLLERR)
                value['polling'] = True
            except BaseException as error:
                self.fail(name + ' pipe setup incomplete: ' + type(error).__name__)
        last_sample = 0.0
        while True:
            now = time.monotonic()
            if CANCEL:
                self.interrupted = True
                self.fail('external signal observed: ' + ','.join(map(str, sorted(CANCEL))))
            if (self.child.returncode is None and self.child_started is not None
                    and now - self.child_started >= LIMITS['outer_soft_seconds']):
                self.fail('outer closeout 1020-second soft timeout: 900 main plus 120 setup/settlement', timeout=True)
            if now - last_sample >= LIMITS['sample_seconds']:
                self.sample()
                last_sample = now
            if self.abort_at is not None and not self.term_attempted:
                self.signal_direct(signal.SIGTERM)
            if self.abort_at is not None and now - self.abort_at >= LIMITS['cleanup_grace_seconds'] and not self.kill_attempted:
                self.signal_direct(signal.SIGKILL)
            try:
                for fd, event in poller.poll(250):
                    name = next(name for name, value in self.streams.items()
                                if value['polling'] and value['stream'].fileno() == fd)
                    value = self.streams[name]
                    require(not event & select.POLLNVAL, 'original pipe descriptor unavailable')
                    try:
                        chunk = os.read(fd, 65536)
                    except BlockingIOError:
                        continue
                    if not chunk:
                        poller.unregister(fd)
                        value['eof'], value['close_attempted'], value['polling'] = True, True, False
                        value['stream'].close()
                        continue
                    self.seen[name] += len(chunk)
                    output = self.files[name]
                    remaining = 0 if output.failed else LIMITS['file_bytes'] - output.used
                    retained = chunk[:remaining]
                    self.discarded[name] += len(chunk) - len(retained)
                    if retained:
                        try:
                            output.append(retained)
                        except BaseException as error:
                            self.fail(name + ' persistence: ' + type(error).__name__)
                    if len(chunk) > remaining:
                        self.fail(name + ' retained-output cap/incomplete persistence; excess discarded')
            except BaseException as error:
                self.fail('pipe collection: ' + type(error).__name__ + ': ' + str(error))
                time.sleep(0.25)
            code = self.child.poll()
            if code is not None:
                if self.exited_at is None:
                    self.exited_at, self.exit_observed = time.monotonic(), code
                    self.event('direct_wait_exit_observed', pid=self.child.pid, original=self.original, exit=code)
                if all(value['eof'] for value in self.streams.values()):
                    break
                if time.monotonic() - self.exited_at >= LIMITS['pipe_after_exit_seconds']:
                    self.fail('pipe EOF unobserved after direct exit; no descendant signal authority')
                    break
            if (code is None and self.kill_at is not None
                    and time.monotonic() - self.kill_at >= LIMITS['kill_wait_seconds']):
                self.fail('direct child not settled after final bounded kill wait; HOLD')
                break

    def emergency_wait(self):
        """No replay of failed pipe setup or launch; same original child/grace only."""
        self.fail('exceptional collection abandonment; original direct cleanup only')
        while self.child.poll() is None:
            now = time.monotonic()
            if not self.term_attempted:
                self.signal_direct(signal.SIGTERM)
            if now - self.abort_at >= LIMITS['cleanup_grace_seconds'] and not self.kill_attempted:
                self.signal_direct(signal.SIGKILL)
            if self.kill_at is not None and now - self.kill_at >= LIMITS['kill_wait_seconds']:
                self.fail('exceptional direct settlement unobserved; HOLD')
                return
            self.sample()
            time.sleep(LIMITS['sample_seconds'])
        self.exit_observed = self.child.returncode
        self.event('exceptional_direct_wait_exit', original=self.original, exit=self.exit_observed)

    def close_pipes(self):
        if self.child is None:
            return
        for name, stream in (('stdout', self.child.stdout), ('stderr', self.child.stderr)):
            value = self.streams.get(name)
            if value is not None and value['close_attempted']:
                continue
            if value is not None:
                value['close_attempted'] = True
            try:
                stream.close()
            except BaseException as error:
                self.fail('original pipe close incomplete: ' + type(error).__name__)


def main():
    require(sys.platform == 'linux' and sys.argv == [str(SELF)] and Path(__file__) == SELF,
            'fixed absolute Linux entry; no arguments or imports')
    require(sys.flags.isolated and sys.dont_write_bytecode and not sys.flags.optimize
            and sys.version_info[:2] == (3, 12), 'pinned Python3.12 -I -B without optimization')
    require(signal.getsignal(signal.SIGCHLD) == signal.SIG_DFL, 'default SIGCHLD required for direct wait ownership')
    require(not signal.pthread_sigmask(signal.SIG_BLOCK, set()) & SIGNALS, 'managed signals initially unblocked')
    os.umask(0o077)
    for number in SIGNALS:
        signal.signal(number, lambda signum, _frame: CANCEL.add(signum))
    d = Originals()
    state = Supervision(d)
    request = inner = captures = bindings = None
    lock_fd, lock_held, code = None, False, 1
    try:
        request, inner, captures, bindings = admission(d)
        lock_parent = d.directory(LOCK.parent)
        expected_lock = request['lock_pin']
        require(pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False)) == expected_lock
                and expected_lock['uid'] == UID and expected_lock['mode'] == stat.S_IFREG | 0o600
                and expected_lock['nlink'] == 1, 'original shared lock identity')
        lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=lock_parent)
        require(pin(os.fstat(lock_fd)) == expected_lock, 'original shared lock open')
        fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        lock_held = True
        d.verify()
        for path, original in captures.items():
            require(d.read(path, cap=input_cap(path)) == original,
                    'under-probe source/request/acceptance image drift')
        require(pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False)) == expected_lock,
                'under-probe original lock drift')
        fresh(request)
        fresh(inner)
        require(absent(CLOSEOUT_JOURNAL.name, d.directory(E)), 'closeout already consumed; no retry/adoption')
        require(all(absent(path.name, d.directory(C)) for path in OUTPUTS + (RECEIPT,)),
                'outer closeout evidence/receipt already occupied; no retry')
        initial = resources(d, True)
        require(initial['floor_ok'], 'launcher admission disk/RAM floor')
        parent_birth = direct_row(os.getpid())
        require(parent_birth['uid'] == UID, 'parent original uid')
        # REQUEST is prospective first-file authority; exclusive creation consumes this attempt.
        state.files['processes'] = Evidence(d, OUTPUTS[3])
        state.files['processes'].begin()
        state.event('outer_closeout_admission', bindings=bindings, outer_request_path=str(REQUEST),
                    closer_argv=ARGV, outer_limits=LIMITS, child_limits=INNER_LIMITS,
                    original_parent=parent_birth, lock_pin=expected_lock,
                    input_pins={str(path): value[1] for path, value in captures.items()},
                    original_process_evidence=state.files['processes'].original)
        for name, path in (('stdout', OUTPUTS[0]), ('stderr', OUTPUTS[1]), ('resources', OUTPUTS[2])):
            state.event('evidence_allocation_intent', path=str(path), parent=d.pins[str(C)])
            require(not state.errors, 'durable evidence allocation intent failed')
            state.files[name] = Evidence(d, path)
            state.event('evidence_allocation_original', path=str(path), identity=state.files[name].original)
            if name == 'resources':
                state.files[name].begin()
                state.files[name].event(initial)
        fcntl.flock(lock_fd, fcntl.LOCK_UN)
        lock_held = False
        state.event('original_lock_probe_released_before_child')
        require(not state.errors, 'launcher evidence incomplete before fork')
        d.check_time()
        d.verify()
        fresh(request)
        fresh(inner)
        require(pin(os.fstat(lock_fd)) == expected_lock
                == pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False)), 'prelaunch original lock drift')
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            require(not CANCEL and not signal.sigpending() & SIGNALS, 'cancel before direct launch intent')
            state.event('direct_launch_intent', argv=ARGV, cwd=str(W), environment=ENV,
                        no_other_audit_local_or_ci_job=True, original_lock_not_held=True,
                        original_run_stop_not_retried=True, no_parent_deletion=True)
            require(not state.errors and not signal.sigpending() & SIGNALS, 'cancel/error before launch commitment')
            state.child_started = time.monotonic()
            state.child = subprocess.Popen(ARGV, cwd=W, env=ENV, stdin=subprocess.DEVNULL,
                                           stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                           close_fds=True, start_new_session=True,
                                           preexec_fn=lambda: signal.pthread_sigmask(signal.SIG_SETMASK, old_mask))
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)
        d.preadmission = False
        try:
            state.original = direct_row(state.child.pid)
            require(state.original['uid'] == UID and state.original['ppid'] == os.getpid(),
                    'original direct Popen child uid/parent')
            state.event('direct_launch_observed', original=state.original)
        except BaseException as error:
            state.original = None
            state.fail('original child birth unavailable; never later adopted: ' + type(error).__name__)
        state.monitor()
    except BaseException as error:
        state.fail('outer failure: ' + type(error).__name__ + ': ' + str(error))
        if state.child is not None and state.child.returncode is None:
            try:
                state.emergency_wait()
            except BaseException as cleanup_error:
                state.fail('original child settlement unavailable; external HOLD: ' + type(cleanup_error).__name__)
    finally:
        state.close_pipes()
        if lock_held:
            try:
                fcntl.flock(lock_fd, fcntl.LOCK_UN)
            except BaseException as error:
                state.fail('original lock release incomplete: ' + type(error).__name__)
        if state.child is not None and state.child.returncode is not None and captures is not None:
            try:
                d.preadmission = False
                d.deadline = time.monotonic() + LIMITS['postcheck_seconds']
                for path, original in captures.items():
                    require(d.read(path, cap=input_cap(path)) == original,
                            'post-closeout frozen input drift')
                require(pin(os.fstat(lock_fd)) == request['lock_pin']
                        == pin(os.stat(LOCK.name, dir_fd=d.directory(LOCK.parent), follow_symlinks=False)),
                        'post-closeout original lock drift')
                fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                fcntl.flock(lock_fd, fcntl.LOCK_UN)
                state.event('original_lock_available_after_direct_wait')
            except BaseException as error:
                state.fail('post-closeout binding/coordination: ' + type(error).__name__ + ': ' + str(error))
        if 'resources' in state.files:
            state.sample()
        for name in ('stdout', 'stderr', 'resources'):
            writer = state.files.get(name)
            if writer is not None:
                try:
                    if name == 'resources' and not writer.failed:
                        writer.finish({'status': 'OBSERVATIONS_NOT_HOST_EXCLUSIVITY_CLOSEOUT_OR_APPLICATION_PASS',
                                       'sample_seconds_target': LIMITS['sample_seconds']})
                except BaseException as error:
                    state.fail(name + ' final persistence: ' + type(error).__name__)
                try:
                    writer.close()
                except BaseException as error:
                    state.fail(name + ' original close: ' + type(error).__name__)
        signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        CANCEL.update(signal.sigpending() & SIGNALS)
        if CANCEL:
            state.interrupted = True
            state.fail('external cancellation at terminal sample')
        complete = (state.child is not None and state.original is not None and state.exit_observed in (0, 1)
                    and state.child.returncode == state.exit_observed and not state.errors
                    and not state.interrupted and not state.timed_out
                    and len(state.streams) == 2 and all(value['eof'] for value in state.streams.values()))
        code = 0 if complete else 1
        summary = {
            'status': 'PRETERMINAL_PARENT_OBSERVATIONS_NOT_ROOT_LAUNCHER_RECEIPT',
            'started_unix_ns': state.started_ns, 'last_observed_unix_ns': time.time_ns(),
            'completed': complete, 'closer_exit': state.exit_observed, 'outer_exit_intent': code,
            'outer_soft_timeout_observed': state.timed_out,
            'managed_signal_or_abort_observed': state.interrupted,
            'term_attempted': state.term_attempted, 'kill_attempted': state.kill_attempted,
            'original_closer': state.original, 'stdout_stderr_bytes_seen': state.seen,
            'discarded_bytes': state.discarded, 'errors': state.errors, 'additional_errors_omitted': state.omitted_errors,
            'original_run_stop_and_workers': 'NO_PARENT_STOP_OR_RETRY_REQUIRE_BOUND_ORIGINAL_RUN_AND_CLOSER_GATES',
            'closeout_outcome': 'UNADJUDICATED_REQUIRE_ACTUAL_CLOSER_ZERO_OUTER_ZERO_AND_INDEPENDENT_JOURNAL_REVIEW',
            'runtime_evidence': 'NO_PARENT_DELETION_OR_RETRY_RETAIN_R_CHECKOUT_E_SOURCE_REPORTS_AND_CONTROLS',
            'limitations': ACK + ['TOOL_EXIT_UNOBSERVED_IN_PROCESS', 'SIGNALS_AFTER_TERMINAL_SAMPLE_OUTSIDE_PROMISE',
                                  'HOST_LOSS_SIGKILL_AND_BLOCKED_SYSCALLS_CAN_PREVENT_SETTLEMENT'],
        }
        writer = state.files.get('processes')
        if writer is not None:
            try:
                if not writer.failed:
                    writer.finish(summary)
                else:
                    code = 1
            except BaseException as error:
                code = 1
                state.fail('process evidence final persistence: ' + type(error).__name__)
            try:
                writer.close()
            except BaseException as error:
                code = 1
                state.fail('process evidence original close: ' + type(error).__name__)
        if lock_fd is not None:
            try:
                os.close(lock_fd)
            except BaseException as error:
                code = 1
                state.fail('original lock descriptor close incomplete: ' + type(error).__name__)
        directory_close_errors = d.close()
        if directory_close_errors:
            code = 1
            state.fail('original directory descriptor closes incomplete: ' + ','.join(directory_close_errors))
        if code:
            try:
                os.write(2, ('CLOSEOUT_OUTER_HOLD_NO_RETRY: ' + '; '.join(state.errors)[:4096] + '\n').encode())
            except BaseException:
                pass
    os._exit(code)  # Only root's later tool observation can establish the actual outer exit.


if __name__ == '__main__':
    main()
