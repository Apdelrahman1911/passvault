#!/usr/bin/python3
"""Root-only, separately admitted launcher for the NEW fixed inert controls.

No child, shell, network, Gradle, old helper or application is launched. The
240-second launcher alarm has 60 seconds of cleanup grace; host loss, SIGKILL
or a blocked syscall may still leave explicit HOLD. This is not a UID sandbox.
"""

import fcntl
import hashlib
import json
import os
from pathlib import Path
import resource
import signal
import stat
import sys
import time
import types


BASE = Path('/root/projects/PassVault')
W = BASE / 'passvault-linux'
C = W / 'docs/audit-continuation/2026-09-08-linux/reviews/verification'
SELF = C / 'RUN_CONTROLS.py'
SCRATCH = BASE / 'audit-runtime-linux-runner-controls-01'
LOCK = BASE / '.audit-coordination-linux-20260908/build.lock'
REQUEST = C / 'CONTROL-REQUEST.json'
APPROVALS = (C / 'CONTROL-ACCEPT-root.json', C / 'CONTROL-ACCEPT-editor_review.json')
REVIEWERS = ('/root', '/root/editor_review')
INPUTS = {
    'launcher': SELF,
    'launch_plan': C / 'CONTROL-LAUNCH-PLAN.md',
    'controls': C / 'linux_runner_controls.py',
    'design': C / 'LINUX-CONTROLS-DESIGN.md',
    'runner': W / 'scripts/audit/linux_database_validation.py',
    'runner_plan': C.parent / 'linux-runner/PLAN.md',
    'controls_review': C.parent / 'storage/LINUX-CONTROLS-SOURCE-REVIEW.json',
}
RUNNER_SHA = '346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279'
PYTHON = Path('/usr/bin/python3.12')
EXPECTED_CASES = (
    'LC01_reader_positive', 'LC02_reader_symlink_refusal', 'LC03_reader_hardlink_refusal',
    'LC04_reader_preopen_mutation', 'LC05_reader_postfd_mutation', 'LC06_reader_postpath_replacement',
    'LC07_reader_parent_replacement', 'LC08_newfile_fsync_failure_closes_fd',
    'LC09_command_positive', 'LC10_command_nonzero', 'LC11_command_prefork_cancel',
    'LC12_command_pending_after_intent', 'LC13_cleanup_stop_once', 'LC14_command_log_cap',
    'LC15_scan_after_only', 'LC16_scan_pid_reuse', 'LC17_direct_already_zombie',
    'LC18_owned_zombie_not_cwd_churn', 'LC19_signal_zombie_and_wrong_birth',
    'LC20_parent_before_child_guard', 'LC21_parent_after_child_guard', 'LC22_direct_death_during_capture',
)
SIGNALS = {signal.SIGHUP, signal.SIGINT, signal.SIGTERM}
MAX_INPUT = 512 * 1024
MAX_JOURNAL = 768 * 1024
GIB = 1024 ** 3
CANCEL = set()
START = time.monotonic()
TIMEOUT = False


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True).encode() + b'\n'


def digest(data):
    return hashlib.sha256(data).hexdigest()


def parse(data):
    def pairs(items):
        result = {}
        for key, value in items:
            require(key not in result, 'duplicate input key')
            result[key] = value
        return result
    return json.loads(data, object_pairs_hook=pairs,
                      parse_constant=lambda _: (_ for _ in ()).throw(RuntimeError('nonfinite JSON')))


def pin(st, directory=False):
    names = ('dev', 'ino', 'uid', 'mode') if directory else (
        'dev', 'ino', 'uid', 'mode', 'nlink', 'size', 'mtime_ns', 'ctime_ns')
    return {name: getattr(st, 'st_' + name) for name in names}


class Originals:
    def __init__(self):
        self.fds, self.pins = {}, {}

    def directory(self, path, discover=False):
        require(path.is_absolute() and '..' not in path.parts, 'absolute original path required')
        key = str(path)
        parent = self.directory(path.parent, discover) if path != Path('/') else None
        if key not in self.fds:
            require(discover, 'unbound directory')
            fd = os.open(path.name if parent is not None else '/',
                         os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
            try:
                original = pin(os.fstat(fd), True)
                require(stat.S_ISDIR(original['mode']), 'directory required')
                self.fds[key], self.pins[key] = fd, original
            except BaseException:
                os.close(fd)
                raise
        fd, original = self.fds[key], self.pins[key]
        require(pin(os.fstat(fd), True) == original, 'original directory fd changed')
        if parent is not None:
            require(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True) == original,
                    'original directory path changed')
        return fd

    def read(self, path, discover=False, cap=MAX_INPUT):
        parent = self.directory(path.parent, discover)
        before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        require(stat.S_ISREG(before.st_mode) and before.st_uid == os.getuid()
                and before.st_nlink == 1 and not before.st_mode & 0o022
                and before.st_size <= cap, 'bounded owned regular input required')
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        try:
            require(pin(os.fstat(fd)) == pin(before), 'input changed before open')
            pieces, count = [], 0
            while True:
                piece = os.read(fd, min(65536, cap + 1 - count))
                if not piece:
                    break
                count += len(piece)
                require(count <= cap, 'input cap')
                pieces.append(piece)
            require(pin(before) == pin(os.fstat(fd))
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input changed after read')
            require(count == before.st_size, 'short input read')
            self.directory(path.parent)
            return b''.join(pieces), pin(before)
        finally:
            os.close(fd)

    def close(self):
        for fd in reversed(tuple(self.fds.values())):
            os.close(fd)
        self.fds.clear()


def write_all(fd, data):
    view = memoryview(data)
    while view:
        written = os.write(fd, view)
        require(written > 0, 'short evidence write')
        view = view[written:]


def sample(launch=False):
    memory = {}
    with open('/proc/meminfo', 'rb') as stream:
        for line in stream:
            fields = line.split()
            if fields[0] in (b'MemTotal:', b'MemAvailable:'):
                memory[fields[0].decode().rstrip(':')] = int(fields[1]) * 1024
    disks = {}
    for path in (BASE, C):
        fs = os.statvfs(path)
        disks[str(path)] = fs.f_bavail * fs.f_frsize
    memory_ok = memory['MemAvailable'] >= memory['MemTotal'] * (0.25 if launch else 0.20)
    disk_ok = min(disks.values()) >= (12 if launch else 8) * GIB
    return {'unix_ns': time.time_ns(), 'disks_free_bytes': disks, 'memory_bytes': memory,
            'launch_floor': launch, 'within_floors': memory_ok and disk_ok,
            'self_maxrss_kib': resource.getrusage(resource.RUSAGE_SELF).ru_maxrss}


class Journal:
    def __init__(self, originals):
        self.d = originals
        self.path = C / 'CONTROL-events.jsonl'
        self.fd = os.open(self.path.name, os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                          0o600, dir_fd=self.d.directory(C))
        try:
            self.last = pin(os.fstat(self.fd))
            self.used, self.sequence, self.last_sample, self.failed = 0, 0, 0.0, False
            os.fsync(self.d.directory(C))
        except BaseException:
            os.close(self.fd)
            raise

    def emit(self, event):
        require(not self.failed, 'journal already failed')
        try:
            parent = self.d.directory(C)
            require(pin(os.fstat(self.fd)) == self.last
                    == pin(os.stat(self.path.name, dir_fd=parent, follow_symlinks=False)), 'journal original drift')
            item = {'sequence': self.sequence, 'unix_ns': time.time_ns(), **event}
            if event.get('kind') != 'launcher_terminal_commit' and time.monotonic() - self.last_sample >= 5:
                item['launcher_resource_observation'] = sample()
                self.last_sample = time.monotonic()
                if not item['launcher_resource_observation']['within_floors']:
                    CANCEL.add('resource_floor')
            data = canonical(item)
            require(len(data) + self.used <= MAX_JOURNAL, 'launcher journal cap')
            write_all(self.fd, data)
            os.fsync(self.fd)
            after = pin(os.fstat(self.fd))
            require(all(after[k] == self.last[k] for k in ('dev', 'ino', 'uid', 'mode', 'nlink'))
                    and after['size'] == self.used + len(data)
                    and after == pin(os.stat(self.path.name, dir_fd=parent, follow_symlinks=False)),
                    'journal append or pathname drift')
            self.last, self.used, self.sequence = after, after['size'], self.sequence + 1
        except BaseException:
            self.failed = True
            raise


def alarm(_number, _frame):
    global TIMEOUT
    CANCEL.add('launcher_timeout')
    if TIMEOUT:
        # No deletion/signal/retry from the emergency path. OS closes this
        # single process's fds; missing terminal receipt remains HOLD.
        os.write(2, b'CONTROL_LAUNCH_TIMEOUT_CLEANUP_INCOMPLETE_HOLD\n')
        os._exit(124)
    TIMEOUT = True
    signal.setitimer(signal.ITIMER_REAL, 60)
    raise TimeoutError('240-second controls launcher bound; 60-second cleanup grace')


def verify_lock(d, lock_fd, request):
    parent = d.directory(LOCK.parent)
    require(pin(os.fstat(lock_fd)) == request['lock_pin']
            == pin(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)), 'original coordination lock drift')


def main():
    require(sys.platform == 'linux' and sys.argv == [str(SELF)] and Path(__file__) == SELF,
            'fixed absolute Linux entry; no overrides')
    require(sys.flags.isolated and sys.dont_write_bytecode and not sys.flags.optimize,
            'require /usr/bin/python3 -I -B without -O')
    require(Path(os.path.realpath(sys.executable)) == PYTHON, 'fixed resolved Python3.12 executable')
    os.umask(0o077)
    for number in SIGNALS:
        signal.signal(number, lambda signum, _frame: CANCEL.add(signum))
    signal.signal(signal.SIGALRM, alarm)
    signal.setitimer(signal.ITIMER_REAL, max(0.001, 240 - (time.monotonic() - START)))
    resource.setrlimit(resource.RLIMIT_AS, (512 * 1024 ** 2, 512 * 1024 ** 2))
    resource.setrlimit(resource.RLIMIT_FSIZE, (1024 ** 2, 1024 ** 2))
    d, lock_fd, scratch_fd, journal = Originals(), None, None, None
    scratch_original, allocation_attempted, report, error = None, False, None, None
    scratch_admitted = False
    cleanup, code = 'NOT_ALLOCATED', 1
    try:
        images, identities = {}, {}
        for path in (REQUEST, *INPUTS.values(), *APPROVALS):
            images[str(path)], identities[str(path)] = d.read(path, True)
        images[str(PYTHON)], identities[str(PYTHON)] = d.read(PYTHON, True, 32 * 1024 ** 2)
        request = parse(images[str(REQUEST)])
        bindings = {key: digest(images[str(path)]) for key, path in INPUTS.items()}
        require(request['format'] == 'passvault-linux-controls-request-v1' and request['author'] == '/root'
                and request['run_id'] == 'linux-runner-controls-01', 'request identity')
        require(request['scratch'] == str(SCRATCH) and request['lock'] == str(LOCK), 'fixed namespace')
        require(request['no_other_audit_local_or_ci_job'] is True, 'sole slot required')
        require(request['bindings'] == bindings and bindings['runner'] == RUNNER_SHA, 'exact source bindings')
        require(request['input_pins'] == {str(p): identities[str(p)] for p in INPUTS.values()}, 'input originals')
        require(request['python'] == {'path': str(PYTHON), 'sha256': digest(images[str(PYTHON)]),
                                      'identity': identities[str(PYTHON)]}
                and not identities[str(PYTHON)]['mode'] & (stat.S_ISUID | stat.S_ISGID),
                'original interpreter identity/bytes')
        require(request['created_unix_seconds'] <= time.time() <= request['expires_unix_seconds']
                and 0 < request['expires_unix_seconds'] - request['created_unix_seconds'] <= 3600, 'fresh request')
        for path in sorted(request['directory_pins'], key=lambda p: (len(Path(p).parts), p)):
            d.directory(Path(path), True)
        require(d.pins == request['directory_pins'], 'complete original directory set')
        for path, reviewer in zip(APPROVALS, REVIEWERS):
            approval = parse(images[str(path)])
            require(approval['format'] == 'passvault-linux-controls-acceptance-v1'
                    and approval['reviewer'] == reviewer and approval['launcher_author'] == '/root'
                    and approval['purpose'] == 'ONE_NEW_EXACT_SOURCE_INERT_CONTROL_EXECUTION'
                    and approval['disposition'] == 'ACCEPT'
                    and approval['request_sha256'] == digest(images[str(REQUEST)])
                    and approval['bindings'] == bindings, 'actual approver-authored acceptance')
        parent = d.directory(LOCK.parent)
        require(pin(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)) == request['lock_pin']
                and request['lock_pin']['mode'] == stat.S_IFREG | 0o600
                and request['lock_pin']['uid'] == os.getuid() and request['lock_pin']['nlink'] == 1,
                'original private coordination lock')
        lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        require(pin(os.fstat(lock_fd)) == request['lock_pin'], 'opened lock original')
        fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        for path, data in images.items():
            observed, identity = d.read(Path(path), cap=32 * 1024 ** 2 if path == str(PYTHON) else MAX_INPUT)
            require(observed == data and identity == identities[path], 'under-lock original input drift')
        require(pin(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)) == request['lock_pin'], 'lock path drift')
        require(time.time() <= request['expires_unix_seconds'] and not CANCEL, 'expired/cancelled before mutation')
        resources = sample(True)
        require(resources['within_floors'], 'launch resource floor')
        require(not os.path.lexists(SCRATCH), 'scratch already consumed; no replay')
        journal = Journal(d)  # Exclusive journal consumes this admission before scratch allocation.
        with open('/proc/self/stat', 'r', encoding='ascii') as stream:
            process_fields = stream.read(8192).rsplit(')', 1)[1].split()
        journal.emit({'kind': 'launcher_admission', 'bindings': bindings,
                      'request_sha256': digest(images[str(REQUEST)]),
                      'approval_sha256': [digest(images[str(p)]) for p in APPROVALS],
                      'directory_pins': d.pins, 'lock_pin': request['lock_pin'], 'resources': resources,
                      'pid': os.getpid(), 'starttime': int(process_fields[19]),
                      'argv': ['/usr/bin/python3', '-I', '-B', str(SELF)],
                      'no_other_audit_local_or_ci_job': True, 'real_children_admitted': 0})
        journal.emit({'kind': 'scratch_allocation_intent', 'path': str(SCRATCH),
                      'parent_pin': d.pins[str(BASE)]})
        require(not CANCEL, 'cancelled before allocation')
        allocation_attempted, cleanup = True, 'HOLD'
        os.mkdir(SCRATCH.name, 0o700, dir_fd=d.directory(BASE))
        scratch_fd = os.open(SCRATCH.name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                             dir_fd=d.directory(BASE))
        scratch_original = pin(os.fstat(scratch_fd), True)
        require(scratch_original['mode'] == stat.S_IFDIR | 0o700
                and scratch_original['uid'] == os.getuid()
                and scratch_original == pin(os.stat(SCRATCH.name, dir_fd=d.directory(BASE), follow_symlinks=False), True)
                and not os.listdir(scratch_fd), 'fresh scratch original')
        os.fsync(d.directory(BASE))
        journal.emit({'kind': 'scratch_allocation_original', 'path': str(SCRATCH), 'identity': scratch_original})
        scratch_admitted = True
        module = types.ModuleType('passvault_new_inert_controls_admitted')
        module.__file__ = str(INPUTS['controls'])
        exec(compile(images[str(INPUTS['controls'])], str(INPUTS['controls']), 'exec'), module.__dict__)
        require(tuple(name for name, _, _ in module.CASES) == EXPECTED_CASES, 'exact ordered control contract')
        report = module.run_controls(scratch_fd=scratch_fd, scratch_path=SCRATCH,
                                     original_scratch=scratch_original, source_bytes=images[str(INPUTS['runner'])],
                                     emit=journal.emit, cancelled=lambda: bool(CANCEL))
    except BaseException as failure:
        error = type(failure).__name__ + ': ' + str(failure)
        os.write(2, ('CONTROL_LAUNCH_FAIL: ' + error[:2000] + '\n').encode())
    finally:
        if journal is not None and scratch_fd is not None and scratch_admitted:
            try:
                parent = d.directory(BASE)
                require(pin(os.fstat(scratch_fd), True) == scratch_original
                        == pin(os.stat(SCRATCH.name, dir_fd=parent, follow_symlinks=False), True), 'scratch original drift')
                require(not os.listdir(scratch_fd), 'scratch not empty; retain HOLD')
                journal.emit({'kind': 'scratch_rmdir_intent', 'path': str(SCRATCH), 'identity': scratch_original})
                verify_lock(d, lock_fd, request)
                parent = d.directory(BASE)
                require(pin(os.fstat(scratch_fd), True) == scratch_original
                        == pin(os.stat(SCRATCH.name, dir_fd=parent, follow_symlinks=False), True)
                        and not os.listdir(scratch_fd), 'last-moment original empty scratch guard')
                os.rmdir(SCRATCH.name, dir_fd=parent)
                os.fsync(parent)
                require(not os.path.lexists(SCRATCH), 'scratch recreated; HOLD')
                journal.emit({'kind': 'scratch_rmdir_observed', 'path': str(SCRATCH)})
                cleanup = 'ORIGINAL_EMPTY_SCRATCH_REMOVED'
            except BaseException as failure:
                error = (error + '; ' if error else '') + 'cleanup: ' + type(failure).__name__ + ': ' + str(failure)
                cleanup = 'HOLD'
        if scratch_fd is not None:
            os.close(scratch_fd)
        if journal is not None:
            try:
                for path, data in images.items():
                    observed, identity = d.read(Path(path), cap=32 * 1024 ** 2 if path == str(PYTHON) else MAX_INPUT)
                    require(observed == data and identity == identities[path], 'final source/admission drift')
                verify_lock(d, lock_fd, request)
                final_resources = sample()
                signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS | {signal.SIGALRM})
                CANCEL.update(signal.sigpending() & (SIGNALS | {signal.SIGALRM}))
                rows = report.get('results', []) if report else []
                okay = (report is not None and tuple(row['name'] for row in rows) == EXPECTED_CASES
                        and all(row['outcome'] == 'PASS' and row['cleanup'] == 'ORIGINAL_SYNTHETIC_MEMBERS_REMOVED'
                                and row['namespace_cleanup'] == 'ORIGINAL_SYNTHETIC_MEMBERS_REMOVED'
                                and row['descriptor_settlement_hold'] is False
                                and row['within_case_soft_bound'] is True
                                for row in rows)
                        and report['runner_sha256'] == RUNNER_SHA
                        and report['expected'] == report['executed'] == report['passed'] == 22
                        and report['not_run'] == [] and report['scratch_empty'] is True
                        and report['within_total_soft_bound'] is True
                        and report['real_child_processes_started'] == report['real_process_signals_sent'] == 0
                        and report['application_tests_executed'] == 0
                        and report['status'] == 'CONTROLS_PASS_NOT_APPLICATION_OR_RUNTIME_ADMISSION'
                        and cleanup == 'ORIGINAL_EMPTY_SCRATCH_REMOVED' and not error and not CANCEL
                        and final_resources['within_floors'] and not journal.failed)
                code = 0 if okay else 1
                journal.emit({'kind': 'launcher_terminal_commit', 'exit': code, 'error': error,
                              'cleanup': cleanup, 'allocation_attempted': allocation_attempted,
                              'cancelled': sorted(map(str, CANCEL)), 'external_timeout_observed': TIMEOUT,
                              'resources': final_resources, 'application_tests_executed': 0,
                              'wrapper_stop': 'NOT_APPLICABLE_NO_WRAPPER_LAUNCH',
                              'status': 'CONTROLS_ONLY_NOT_DATABASE_EXECUTION_ADMISSION',
                              'source_inputs_unchanged': True})
            except BaseException as failure:
                os.write(2, ('CONTROL_TERMINAL_INCOMPLETE_HOLD: ' + str(failure)[:1000] + '\n').encode())
                code = 1
            finally:
                os.close(journal.fd)
        if lock_fd is not None:
            os.close(lock_fd)
        d.close()
    os._exit(code)


if __name__ == '__main__':
    main()
