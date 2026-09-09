"""New, fixed-source controls; SOURCE ONLY until separately admitted by root.

No CLI, no application/Gradle child, no archived runner, no recovery mode.
The separately reviewed root launcher supplies ORIGINAL fresh scratch authority,
captured runner bytes, durable bounded emit(), and a monotone cancellation latch.
This module does not create/reuse the scratch root or grant its own admission.
Real POSIX file/pipe controls and MOCKED process/command controls are distinguished.
"""

import contextlib
import errno
import hashlib
import json
import os
from pathlib import Path
import signal
import stat
import subprocess
import sys
import time
import types
from unittest import mock


TARGET = Path('/root/projects/PassVault/passvault-linux/scripts/audit/linux_database_validation.py')
TARGET_SHA256 = '346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279'
SCRATCH = Path('/root/projects/PassVault/audit-runtime-linux-runner-controls-01')
MAX_CASE_SECONDS = 15
MAX_TOTAL_SECONDS = 180
MAX_EMITTED_BYTES = 512 * 1024
MAX_CASE_MEMBERS = 24
MAX_FIXTURE_BYTES = 32768
# Single-threaded, one active case; not state in the tested subject module.
_ACTIVE_DESCRIPTOR_ERRORS = None


class ControlFailure(Exception):
    pass


class DescriptorHold(ControlFailure):
    """Original descriptor settlement is uncertain even if namespace cleanup succeeds."""

    def __init__(self, message):
        super().__init__(message)
        # The tested subject may intentionally swallow exceptions into strings.
        # Record uncertainty at creation, before any such conversion can erase it.
        if _ACTIVE_DESCRIPTOR_ERRORS is not None:
            _ACTIVE_DESCRIPTOR_ERRORS.append(type(self).__name__ + ': ' + str(message))


def descriptor_uncertainties(error):
    # A finally/context-manager failure must not erase the original HOLD.
    pending, seen, reasons = [error], set(), []
    while pending:
        if len(seen) >= 64:
            reasons.append('exception chain exceeds 64; descriptor settlement unclassifiable')
            break
        current = pending.pop()
        if current is None or id(current) in seen:
            continue
        seen.add(id(current))
        if isinstance(current, DescriptorHold):
            reasons.append(type(current).__name__ + ': ' + str(current))
        pending.extend((current.__cause__, current.__context__))
    return reasons


def check(condition, message):
    if not condition:
        raise ControlFailure(message)


def raises(expected, operation, contains=None):
    try:
        operation()
    except expected as error:
        if contains is not None:
            check(contains in str(error), 'wrong refusal: ' + str(error))
        return error
    raise ControlFailure('expected refusal was not raised: ' + expected.__name__)


def directory_pin(st):
    return {name: getattr(st, 'st_' + name) for name in ('dev', 'ino', 'uid', 'mode')}


def object_pin(st):
    # Mutated content/timestamps/link count are explicit fixture operations.
    # Original object identity/type/owner is never replaced by a current inode.
    return {'dev': st.st_dev, 'ino': st.st_ino, 'uid': st.st_uid, 'type': stat.S_IFMT(st.st_mode)}


def absent(name, parent_fd):
    try:
        os.stat(name, dir_fd=parent_fd, follow_symlinks=False)
    except FileNotFoundError:
        return True
    return False


def write_all(fd, data):
    check(len(data) <= MAX_FIXTURE_BYTES, 'fixture byte cap')
    view = memoryview(data)
    while view:
        count = os.write(fd, view)
        check(count > 0, 'fixture short write')
        view = view[count:]


def close_original(fd):
    """One close of a retained original; failure is uncertainty, never retry authority."""
    try:
        os.close(fd)
    except BaseException as error:
        raise DescriptorHold('original fd close failed: ' + type(error).__name__ + ': ' + str(error)) from error


def close_acquired_after_failure(fd, cause):
    """Close locally before ownership transfer, retaining both original and close errors."""
    try:
        close_original(fd)
    except DescriptorHold as error:
        raise DescriptorHold('acquisition failed: ' + type(cause).__name__ + ': ' + str(cause)
                             + '; ' + str(error)) from cause


class Area:
    """Only exact newly created synthetic members, through retained original fds.

    No path walk/rmtree/current-inode adoption. An unknown or replaced member
    is HOLD, not deletion authority. Cooperative namespace, not hostile-UID
    atomic inode-conditional unlink protection. Each closeout is attempted once.
    """

    def __init__(self, name, parent_fd, parent_path, emit, parent=None):
        check(name and '/' not in name and name not in ('.', '..'), 'fixture component')
        self.name, self.parent_fd, self.parent_path = name, parent_fd, parent_path
        self.parent, self.emit, self.members = parent, emit, {}
        self.fd, self.pin, self.cleanup_attempted = None, None, False
        self.emit('fixture_allocation_intent', path=str(self.path))
        check(absent(name, parent_fd), 'fixture namespace occupied; no retry')
        os.mkdir(name, 0o700, dir_fd=parent_fd)
        # Failure before original acquisition is partial/HOLD, never re-adopted.
        self.fd = os.open(name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                          dir_fd=parent_fd)
        try:
            self.pin = directory_pin(os.fstat(self.fd))
            check(self.pin['uid'] == os.getuid() and self.pin['mode'] == stat.S_IFDIR | 0o700,
                  'new fixture directory mode/owner')
            os.fsync(parent_fd)
            self.emit('fixture_allocation_original', path=str(self.path), identity=self.pin)
        except BaseException as error:
            # The caller cannot register a constructor that did not return.
            # Close its acquired original fd locally; retain partial namespace.
            fd, self.fd = self.fd, None
            close_acquired_after_failure(fd, error)
            raise

    @property
    def path(self):
        return (self.parent.path if self.parent is not None else self.parent_path) / self.name

    def verify(self):
        if self.parent is not None:
            self.parent.verify()
        check(self.fd is not None and directory_pin(os.fstat(self.fd)) == self.pin,
              'original fixture fd changed')
        check(directory_pin(os.stat(self.name, dir_fd=self.parent_fd, follow_symlinks=False)) == self.pin,
              'original fixture pathname changed')

    def register_file(self, name, fd):
        self.verify()
        check(name not in self.members and '/' not in name, 'fixture member already registered')
        check(len(self.members) < MAX_CASE_MEMBERS, 'fixture member cap')
        st = os.fstat(fd)
        check(stat.S_ISREG(st.st_mode) and st.st_uid == os.getuid(), 'new fixture regular owner')
        pin = object_pin(st)
        check(object_pin(os.stat(name, dir_fd=self.fd, follow_symlinks=False)) == pin,
              'new fixture file path does not name original fd')
        self.members[name] = pin
        self.emit('fixture_file_original', path=str(self.path / name), identity=pin)

    def file(self, name, data=b'original-bytes'):
        self.verify()
        check(name not in self.members and '/' not in name, 'fixture filename')
        fd = os.open(name, os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                     0o600, dir_fd=self.fd)
        try:
            self.register_file(name, fd)
            write_all(fd, data)
            os.fsync(fd)
        finally:
            close_original(fd)
        os.fsync(self.fd)
        return self.path / name

    def open_file(self, name, flags=os.O_RDONLY):
        self.verify()
        pin = self.members[name]
        check(isinstance(pin, dict) and pin['type'] == stat.S_IFREG, 'owned regular fixture required')
        fd = os.open(name, flags | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=self.fd)
        try:
            check(object_pin(os.fstat(fd)) == pin, 'fixture open did not retain original')
        except BaseException as error:
            close_acquired_after_failure(fd, error)
            raise
        return fd

    def bytes(self, name):
        fd = self.open_file(name)
        try:
            data = os.read(fd, MAX_FIXTURE_BYTES + 1)
            check(len(data) <= MAX_FIXTURE_BYTES, 'fixture read cap')
            return data
        finally:
            close_original(fd)

    def mutate(self, name, data):
        self.emit('fixture_content_mutation_intent', path=str(self.path / name))
        fd = self.open_file(name, os.O_RDWR)
        try:
            os.ftruncate(fd, 0)
            write_all(fd, data)
            os.fsync(fd)
        finally:
            close_original(fd)

    def directory(self, name):
        check(name not in self.members, 'fixture directory collision')
        child = Area(name, self.fd, self.path, self.emit, self)
        self.members[name] = child
        return child

    def move(self, old, new):
        self.verify()
        check(old in self.members and new not in self.members and '/' not in new and absent(new, self.fd),
              'fixture rename requires original and absent destination')
        self.emit('fixture_rename_intent', old=str(self.path / old), new=str(self.path / new))
        os.rename(old, new, src_dir_fd=self.fd, dst_dir_fd=self.fd)
        member = self.members.pop(old)
        self.members[new] = member
        if isinstance(member, Area):
            member.name = new
        os.fsync(self.fd)

    def link(self, original, alias, symbolic=False):
        self.verify()
        check(original in self.members and alias not in self.members and '/' not in alias,
              'fixture link scope')
        self.emit('fixture_link_intent', path=str(self.path / alias), symbolic=symbolic)
        if symbolic:
            os.symlink(original, alias, dir_fd=self.fd)
        else:
            os.link(original, alias, src_dir_fd=self.fd, dst_dir_fd=self.fd, follow_symlinks=False)
        pin = object_pin(os.stat(alias, dir_fd=self.fd, follow_symlinks=False))
        check(pin['uid'] == os.getuid() and pin['type'] in (stat.S_IFREG, stat.S_IFLNK), 'fixture link type/owner')
        self.members[alias] = pin
        os.fsync(self.fd)
        self.emit('fixture_link_original', path=str(self.path / alias), identity=pin)

    def preflight(self):
        self.verify()
        check(set(os.listdir(self.fd)) == set(self.members), 'unknown fixture member; retain')
        check(len(self.members) <= MAX_CASE_MEMBERS, 'fixture closeout count cap')
        for name, member in self.members.items():
            if isinstance(member, Area):
                member.preflight()
            else:
                check(object_pin(os.stat(name, dir_fd=self.fd, follow_symlinks=False)) == member,
                      'fixture member original identity changed; retain')

    def cleanup(self):
        check(not self.cleanup_attempted, 'fixture closeout already attempted; no retry')
        self.cleanup_attempted = True
        self.preflight()
        for name, member in tuple(self.members.items()):
            if isinstance(member, Area):
                member.cleanup()
                del self.members[name]
                continue
            self.verify()
            check(object_pin(os.stat(name, dir_fd=self.fd, follow_symlinks=False)) == member,
                  'fixture original changed immediately before unlink')
            self.emit('fixture_unlink_intent', path=str(self.path / name), identity=member)
            os.unlink(name, dir_fd=self.fd)
            os.fsync(self.fd)
            check(absent(name, self.fd), 'fixture member recreated; retain')
            del self.members[name]
            self.emit('fixture_unlink_observed', path=str(self.path / name))
        self.verify()
        check(not os.listdir(self.fd), 'fixture directory not empty')
        self.emit('fixture_rmdir_intent', path=str(self.path), identity=self.pin)
        os.rmdir(self.name, dir_fd=self.parent_fd)
        os.fsync(self.parent_fd)
        check(absent(self.name, self.parent_fd), 'fixture directory recreated; retain')
        self.emit('fixture_rmdir_observed', path=str(self.path))
        fd, self.fd = self.fd, None
        close_original(fd)

    def close_retained_fds(self):
        failures = []
        for member in self.members.values():
            if isinstance(member, Area):
                try:
                    member.close_retained_fds()
                except BaseException as error:
                    failures.append(type(error).__name__ + ': ' + str(error))
        if self.fd is not None:
            fd, self.fd = self.fd, None
            try:
                close_original(fd)
            except BaseException as error:
                failures.append(type(error).__name__ + ': ' + str(error))
        if failures:
            raise DescriptorHold('fixture descriptor settlement HOLD: ' + '; '.join(failures))


def forbidden(*_args, **_kwargs):
    raise ControlFailure('real child/process signal/spawn is not admitted in these controls')


@contextlib.contextmanager
def process_denials():
    with contextlib.ExitStack() as stack:
        stack.enter_context(mock.patch.object(subprocess, 'Popen', forbidden))
        for name in ('system', 'fork', 'forkpty', 'posix_spawn', 'posix_spawnp', 'kill', 'killpg', 'pidfd_open'):
            if hasattr(os, name):
                stack.enter_context(mock.patch.object(os, name, forbidden))
        if hasattr(signal, 'pidfd_send_signal'):
            stack.enter_context(mock.patch.object(signal, 'pidfd_send_signal', forbidden))
        yield


@contextlib.contextmanager
def directories(subject, area, generated=False):
    if generated:
        class OwnedDirectories(subject.Directories):
            def new_file(self, path):
                check(Path(path).parent == area.path, 'generated control file outside original case')
                fd = super().new_file(path)
                try:
                    area.register_file(Path(path).name, fd)
                except BaseException as error:
                    close_acquired_after_failure(fd, error)
                    raise
                return fd
        result = OwnedDirectories()
    else:
        result = subject.Directories()
    try:
        result.open(area.path, True)
        yield result
    finally:
        try:
            result.close()
        except BaseException as error:
            raise DescriptorHold('subject directory descriptor close sequence incomplete: ' + str(error)) from error


def reader_positive(s, a):
    path = a.file('target')
    with directories(s, a) as d:
        data, pin = d.read(path, 64)
        check(data == b'original-bytes' and pin == s.identity(os.stat(path, follow_symlinks=False)),
              'stable read positive bytes/metadata')
    return {'bytes_sha256': hashlib.sha256(data).hexdigest(), 'original_identity': pin}


def reader_link(s, a, symbolic):
    a.file('target')
    a.link('target', 'alias', symbolic)
    with directories(s, a) as d:
        error = raises(s.Refused, lambda: d.read(a.path / 'alias', 64), 'regular single-link')
    return {'refusal': str(error), 'symbolic': symbolic}


def reader_mutation(s, a, phase):
    path = a.file('target')
    original_open, original_read = os.open, os.read
    fired = False
    with directories(s, a) as d:
        def on_open(name, flags, *args, **kwargs):
            nonlocal fired
            if name == 'target' and not fired:
                fired = True
                a.mutate('target', b'mutated-and-longer')
            return original_open(name, flags, *args, **kwargs)

        def on_read(fd, count):
            nonlocal fired
            data = original_read(fd, count)
            if data and not fired:
                fired = True
                if phase == 'post-fd':
                    a.mutate('target', b'mutated-and-longer')
                else:
                    a.move('target', 'original-retained')
                    a.file('target', b'replacement')
            return data

        function, replacement = ('open', on_open) if phase == 'pre-open' else ('read', on_read)
        with mock.patch.object(os, function, replacement):
            error = raises(s.Refused, lambda: d.read(path, 64),
                           'pre/open input change' if phase == 'pre-open' else 'post-read input change')
        check(fired, 'mutation did not run inside reader boundary')
    return {'phase': phase, 'refusal': str(error)}


def reader_parent_replacement(s, a):
    parent = a.directory('parent')
    path = parent.file('target')
    original_read = os.read
    fired = False
    with directories(s, a) as d:
        d.open(parent.path, True)
        def on_read(fd, count):
            nonlocal fired
            data = original_read(fd, count)
            if data and not fired:
                fired = True
                a.move('parent', 'original-parent')
                a.directory('parent').file('target', b'replacement')
            return data
        with mock.patch.object(os, 'read', on_read):
            error = raises(s.Refused, lambda: d.read(path, 64), 'directory pathname replacement')
        check(fired, 'parent mutation schedule not reached')
    return {'refusal': str(error), 'original_parent_preserved_until_fixture_cleanup': True}


def new_file_fsync_failure(s, a):
    original_open, original_fsync = os.open, os.fsync
    acquired = []
    try:
        with directories(s, a) as d:
            target_parent_fd = d.open(a.path)
            def capture_open(name, flags, *args, **kwargs):
                fd = original_open(name, flags, *args, **kwargs)
                try:
                    if name == 'failure-artifact' and flags & os.O_CREAT:
                        pin = object_pin(os.fstat(fd))
                        a.register_file(name, fd)
                        acquired.append((fd, pin))
                except BaseException as error:
                    close_acquired_after_failure(fd, error)
                    raise
                return fd
            def fail_parent(fd):
                if fd == target_parent_fd:
                    raise OSError(errno.EIO, 'synthetic parent fsync failure')
                return original_fsync(fd)
            with mock.patch.object(os, 'open', capture_open), mock.patch.object(os, 'fsync', fail_parent):
                raises(OSError, lambda: d.new_file(a.path / 'failure-artifact'), 'synthetic parent fsync')
            check(len(acquired) == 1, 'expected one original descriptor acquisition')
            error = raises(OSError, lambda: os.fstat(acquired[0][0]))
            check(error.errno == errno.EBADF, 'failed new_file descriptor was not closed')
            acquired.clear()  # Positive closed observation, not another close of an old fd number.
        return {'acquisitions': 1, 'closed_errno': error.errno,
                'partial_file_retained_until_known-original_fixture_cleanup': True}
    finally:
        for fd, pin in acquired:
            try:
                current = object_pin(os.fstat(fd))
            except OSError as error:
                if error.errno == errno.EBADF:
                    continue
                raise DescriptorHold('failed-control descriptor observation unknown: ' + str(error)) from error
            if current != pin:
                raise DescriptorHold('failed-control descriptor identity unknown; no close authority')
            close_original(fd)


class FakeChild:
    def __init__(self, code=0, payload=b'', pid=910001):
        check(len(payload) <= 32, 'fake child payload cap')
        self.pid, self.code, self.returncode, self.polls = pid, code, None, 0
        self.stdout = None
        if payload is not None:
            reader, writer = os.pipe2(os.O_CLOEXEC)
            try:
                try:
                    write_all(writer, payload)
                finally:
                    close_original(writer)
            except BaseException as error:
                close_acquired_after_failure(reader, error)
                raise
            try:
                self.stdout = os.fdopen(reader, 'rb')
            except BaseException as error:
                close_acquired_after_failure(reader, error)
                raise

    def poll(self):
        self.polls += 1
        self.returncode = self.code
        return self.code

    def send_signal(self, _sig):
        forbidden()


class FakeProcesses:
    def __init__(self):
        self.children, self.signals, self.directs = {}, [], []

    def scan(self):
        return []

    def direct(self, child):
        self.children[child.pid] = child
        self.directs.append(child.pid)

    def terminate_owned(self, sig):
        self.signals.append(int(sig))


def command_control(s, a, mode):
    children, launches = [], []
    code, payload = (23, b'nonzero-fixture\n') if mode == 'nonzero' else (0, b'fixture\n')
    processes = FakeProcesses()
    with directories(s, a, True) as d:
        journal = s.Journal(d)
        try:
            run = s.Run(d, journal, processes)
            armed = False
            original_event = journal.event
            def event(kind, **values):
                nonlocal armed
                original_event(kind, **values)
                if kind == 'launch_intent' and mode == 'pending':
                    armed = True
            def pending():
                return {signal.SIGTERM} if armed else set()
            def popen(argv, **options):
                launches.append(list(argv))
                check(options['cwd'] == a.path / 'checkout' and options['env'] == s.ENV,
                      'fixed command cwd/environment passed to fake child')
                check(options['start_new_session'] and options['close_fds'], 'fake child spawn options')
                child = FakeChild(code, payload, 910001 + len(children))
                children.append(child)
                return child
            if mode == 'cancelled':
                s.CANCEL.add(signal.SIGTERM)
            with contextlib.ExitStack() as patches:
                patches.enter_context(mock.patch.object(subprocess, 'Popen', popen))
                patches.enter_context(mock.patch.object(signal, 'pthread_sigmask', lambda *_: set()))
                patches.enter_context(mock.patch.object(signal, 'sigpending', pending))
                patches.enter_context(mock.patch.object(s, 'resource_sample', lambda *_: {'synthetic_resource_sample': True}))
                patches.enter_context(mock.patch.object(journal, 'event', event))
                if mode == 'overflow':
                    patches.enter_context(mock.patch.dict(s.LIMITS, {'log_bytes_per_command': 4}))
                if mode == 'stop-once':
                    run.stop_required = True  # Explicit MOCK of a previously durable launch obligation.
                    output, record = run.command('stop', s.STOP, 5, cleanup=True)
                    _, duplicate = run.command('duplicate-stop', s.STOP, 5, cleanup=True)
                    check(record['started'] and record['complete'] and record['exit'] == 0,
                          'first fake stop must complete')
                    check(run.stop_attempted and not duplicate['started'] and len(launches) == 1,
                          'second stop must refuse without a second Popen')
                    check(journal.errors, 'second stop refusal must remain failure')
                else:
                    output, record = run.command(mode, s.BUILD, 5, gradle=True)
                    if mode in ('cancelled', 'pending'):
                        check(not record['started'] and not launches and journal.errors,
                              'cancellation must prevent fake child launch and latch failure')
                        check(run.stop_required == (mode == 'pending'), 'original pre-fork stop-obligation boundary')
                    elif mode == 'nonzero':
                        check(record['started'] and record['complete'] and record['exit'] == 23,
                              'actual command collector must retain fake child exit23')
                        check(run.stop_required and any('exit nonzero' in value for value in journal.errors),
                              'nonzero exit must be monotonically failed')
                    elif mode == 'overflow':
                        check(output == payload[:4] and record['log_bytes'] == 4 and journal.errors,
                              'hard retained log cap and failure')
                        check('log cap' in record['errors'] and processes.signals,
                              'overflow must request only fake-owned cancellation')
                    else:
                        check(record['started'] and record['complete'] and record['exit'] == 0,
                              'positive command collector')
                        check(output == payload and not journal.errors and run.stop_required,
                              'positive control bytes/no errors/original stop obligation')
            check(all(child.stdout.closed for child in children), 'command ExitStack did not close fake pipes')
            lines = [json.loads(line) for line in a.bytes('JOURNAL.jsonl').splitlines()]
            return {'boundary': 'FAKE_POPEN_AND_PROCESS_ROWS_REAL_PIPE_LOG_JOURNAL_IO',
                    'mode': mode, 'launches': len(launches), 'record': record,
                    'stop_required': run.stop_required, 'stop_attempted': run.stop_attempted,
                    'errors': list(journal.errors), 'journal': lines}
        finally:
            failures = []
            for child in children:
                if child.stdout is not None:
                    try:
                        child.stdout.close()
                    except BaseException as error:
                        failures.append('fake child pipe: ' + type(error).__name__ + ': ' + str(error))
            try:
                close_original(journal.fd)
            except BaseException as error:
                failures.append('subject journal: ' + type(error).__name__ + ': ' + str(error))
            if failures:
                raise DescriptorHold('command fixture descriptor settlement HOLD: ' + '; '.join(failures))


class MemoryJournal:
    def __init__(self):
        self.events, self.errors = [], []

    def event(self, kind, **values):
        self.events.append({'kind': kind, **values})

    def error(self, value):
        if value not in self.errors:
            self.errors.append(value)


def row(s, pid=910101, start=100, **values):
    result = {'pid': pid, 'start': start, 'ppid': 1, 'uid': s.UID,
              'inside': False, 'buildlike': False, 'comm': 'synthetic-row',
              'stable': True, 'birth_stable': True, 'zombie': False}
    result.update(values)
    return result


def scan_control(s, _a, mode):
    journal = MemoryJournal()
    processes = s.Processes(journal)
    old = row(s)
    new = row(s, inside=True, buildlike=True)
    snapshots = [{}, {new['pid']: new}]
    if mode == 'reuse':
        new['start'] += 1
        snapshots = [{old['pid']: old}, {new['pid']: new}]
    elif mode == 'owned-zombie':
        old.update(inside=True, buildlike=True)
        new.update(inside=False, zombie=True)
        processes.owned[(old['pid'], old['start'])] = old  # MOCK birth ledger, not real authority.
        snapshots = [{old['pid']: old}, {new['pid']: new}]
    index = -1
    def listing(path):
        nonlocal index
        check(path == '/proc', 'process mock attempted another directory')
        index += 1
        check(index < 2, 'unexpected extra process pass')
        return [str(pid) for pid in snapshots[index]]
    with mock.patch.object(os, 'getpid', lambda: 1), mock.patch.object(os, 'listdir', listing), \
            mock.patch.object(s, 'process_row', lambda pid: dict(snapshots[index][pid])):
        live = processes.scan()
    check(index == 1 and not live, 'exact two-snapshot/non-live control')
    if mode == 'owned-zombie':
        check(not journal.errors and not processes.unknown, 'owned zombie must not fabricate cwd churn')
    else:
        check(journal.errors and processes.unknown and not processes.owned,
              'unanchored after-only/reused birth must HOLD without ownership')
        if mode == 'reuse':
            check(('between_sample_birth_identity_change', new['pid']) in processes.unknown,
                  'PID reuse reason not preserved')
    return {'boundary': 'MOCK_PROC_ROWS_NOT_OS_PROCESS_PROOF', 'mode': mode,
            'unknown': sorted(processes.unknown), 'errors': journal.errors, 'events': journal.events}


def direct_death(s, _a, during=False):
    journal = MemoryJournal()
    processes = s.Processes(journal)
    child = FakeChild(0, None, 910101)
    live = row(s, inside=True)
    dead = row(s, zombie=True)
    observations = [live, dead] if during else [dead]
    with mock.patch.object(s, 'process_row', side_effect=observations):
        processes.direct(child)
    check(child.polls == 1 and child.returncode == 0 and not processes.owned and not processes.pidfds,
          'same-birth zombie must use observed fake direct exit, not ownership/signals')
    check(journal.events[-1]['kind'] == 'direct_child_already_exited', 'direct exit evidence missing')
    return {'boundary': 'MOCK_DIRECT_CHILD_NOT_WAITPID_PROOF', 'during_admit': during, 'events': journal.events}


def signal_death(s, _a):
    journal = MemoryJournal()
    processes = s.Processes(journal)
    live = row(s, inside=True)
    dead = row(s, zombie=True)
    processes.owned[(live['pid'], live['start'])] = live
    with mock.patch.object(s, 'process_row', return_value=dead):
        processes.signal_owned(live, signal.SIGTERM)
    check(journal.events[-1]['kind'] == 'owned_zombie_no_signal', 'same-birth zombie no-signal path')
    wrong = dict(dead, start=dead['start'] + 1)
    with mock.patch.object(s, 'process_row', return_value=wrong):
        error = raises(s.Refused, lambda: processes.signal_owned(live, signal.SIGTERM), 'fresh signal birth/uid failed')
    return {'boundary': 'MOCK_SIGNAL_GUARD_NO_SIGNAL_SENT', 'events': journal.events, 'wrong_birth_refusal': str(error)}


def parent_guard(s, a, after):
    journal = MemoryJournal()
    processes = s.Processes(journal)
    parent = row(s, pid=910201, start=50, inside=True)
    child = row(s, pid=910202, start=100, ppid=parent['pid'], inside=True)
    changed = dict(parent, start=51)
    processes.owned[(parent['pid'], parent['start'])] = parent
    observations = [parent, child, child, changed] if after else [changed]
    created = []
    a.file('pidfd-token', b'NOT_A_REAL_PIDFD')
    token_fd = a.open_file('pidfd-token')
    try:
        token_pin = object_pin(os.fstat(token_fd))
        def fake_pidfd(pid, flags):
            check(pid == child['pid'] and flags == 0, 'fake pidfd arguments')
            fd = os.dup(token_fd)
            try:
                created.append(fd)
            except BaseException as error:
                close_acquired_after_failure(fd, error)
                raise
            return fd
        with mock.patch.object(s, 'process_row', side_effect=observations), \
                mock.patch.object(os, 'pidfd_open', fake_pidfd, create=True), \
                mock.patch.object(Path, 'read_bytes', forbidden):
            error = raises(s.Refused, lambda: processes.admit(
                child, {'live_parent_pid': parent['pid'], 'start': parent['start']}),
                'original live parent changed ' + ('after' if after else 'before'))
        check((child['pid'], child['start']) not in processes.owned and not processes.pidfds,
              'changed parent must not admit child or retain pidfd authority')
        check(len(created) == (1 if after else 0), 'pidfd acquisition stage mismatch')
        for fd in created:
            closed = raises(OSError, lambda: os.fstat(fd))
            check(closed.errno == errno.EBADF, 'failed parent check leaked acquired descriptor')
        closed_count = len(created)
        created.clear()
        return {'boundary': 'MOCK_PROC_PARENT_REAL_DUP_FD_CLEANUP_NOT_PIDFD_PROOF',
                'after_child_capture': after, 'refusal': str(error), 'acquired_then_closed': closed_count}
    finally:
        failures = []
        try:
            close_original(token_fd)
        except BaseException as error:
            failures.append('token fd: ' + type(error).__name__ + ': ' + str(error))
        for fd in created:
            try:
                try:
                    current = object_pin(os.fstat(fd))
                except OSError as error:
                    if error.errno == errno.EBADF:
                        continue
                    raise
                check(current == token_pin, 'fake pidfd original descriptor unknown; no close authority')
                close_original(fd)
            except BaseException as error:
                failures.append('fake pidfd: ' + type(error).__name__ + ': ' + str(error))
        if failures:
            raise DescriptorHold('parent fixture descriptor settlement HOLD: ' + '; '.join(failures))


CASES = (
    ('LC01_reader_positive', 'REAL_POSIX_FILE', reader_positive),
    ('LC02_reader_symlink_refusal', 'REAL_POSIX_FILE', lambda s, a: reader_link(s, a, True)),
    ('LC03_reader_hardlink_refusal', 'REAL_POSIX_FILE', lambda s, a: reader_link(s, a, False)),
    ('LC04_reader_preopen_mutation', 'REAL_POSIX_FILE_WITH_EXACT_CALL_HOOK', lambda s, a: reader_mutation(s, a, 'pre-open')),
    ('LC05_reader_postfd_mutation', 'REAL_POSIX_FILE_WITH_EXACT_CALL_HOOK', lambda s, a: reader_mutation(s, a, 'post-fd')),
    ('LC06_reader_postpath_replacement', 'REAL_POSIX_FILE_WITH_EXACT_CALL_HOOK', lambda s, a: reader_mutation(s, a, 'post-path')),
    ('LC07_reader_parent_replacement', 'REAL_POSIX_FILE_WITH_EXACT_CALL_HOOK', reader_parent_replacement),
    ('LC08_newfile_fsync_failure_closes_fd', 'REAL_FD_WITH_INJECTED_IO_FAILURE', new_file_fsync_failure),
    ('LC09_command_positive', 'FAKE_CHILD_REAL_PIPE_LOG_IO', lambda s, a: command_control(s, a, 'positive')),
    ('LC10_command_nonzero', 'FAKE_CHILD_REAL_PIPE_LOG_IO', lambda s, a: command_control(s, a, 'nonzero')),
    ('LC11_command_prefork_cancel', 'MOCK_SIGNAL_STATE_NO_CHILD', lambda s, a: command_control(s, a, 'cancelled')),
    ('LC12_command_pending_after_intent', 'MOCK_SIGNAL_STATE_NO_CHILD', lambda s, a: command_control(s, a, 'pending')),
    ('LC13_cleanup_stop_once', 'FAKE_CHILD_REAL_PIPE_LOG_IO', lambda s, a: command_control(s, a, 'stop-once')),
    ('LC14_command_log_cap', 'FAKE_CHILD_REAL_PIPE_LOG_IO', lambda s, a: command_control(s, a, 'overflow')),
    ('LC15_scan_after_only', 'MOCK_PROC_ROWS', lambda s, a: scan_control(s, a, 'after-only')),
    ('LC16_scan_pid_reuse', 'MOCK_PROC_ROWS', lambda s, a: scan_control(s, a, 'reuse')),
    ('LC17_direct_already_zombie', 'MOCK_DIRECT_CHILD', direct_death),
    ('LC18_owned_zombie_not_cwd_churn', 'MOCK_PROC_ROWS', lambda s, a: scan_control(s, a, 'owned-zombie')),
    ('LC19_signal_zombie_and_wrong_birth', 'MOCK_SIGNAL_GUARD', signal_death),
    ('LC20_parent_before_child_guard', 'MOCK_PROC_PARENT', lambda s, a: parent_guard(s, a, False)),
    ('LC21_parent_after_child_guard', 'MOCK_PROC_PARENT_REAL_DUP_FD', lambda s, a: parent_guard(s, a, True)),
    ('LC22_direct_death_during_capture', 'MOCK_DIRECT_CHILD', lambda s, a: direct_death(s, a, True)),
)


def run_controls(*, scratch_fd, scratch_path, original_scratch, source_bytes, emit, cancelled):
    """Only the separately reviewed, source-bound root launcher may call this.

    Caller owns shared-lock admission, parent/root pins, no-CI/local overlap,
    bounded external timeout/resource observations, durable evidence and the
    original scratch-root removal. emit must persist before returning. This
    function never removes scratch root/source/reports/shared caches/toolchains.
    An exception/missing final caller receipt is HOLD, never success or retry.
    """
    global _ACTIVE_DESCRIPTOR_ERRORS
    check(sys.platform == 'linux' and sys.flags.isolated and sys.dont_write_bytecode
          and not sys.flags.optimize, 'require root launcher /usr/bin/python3 -I -B without -O')
    check(Path(scratch_path) == SCRATCH, 'fixed fresh controls scratch path')
    check(len(source_bytes) <= 128 * 1024 and hashlib.sha256(source_bytes).hexdigest() == TARGET_SHA256,
          'exact reviewed successor runner bytes required')
    check(original_scratch == directory_pin(os.fstat(scratch_fd))
          == directory_pin(os.stat(SCRATCH, follow_symlinks=False)), 'original scratch fd/path binding')
    check(original_scratch['uid'] == os.getuid() and original_scratch['mode'] == stat.S_IFDIR | 0o700,
          'fresh scratch mode/owner')
    check(not os.listdir(scratch_fd), 'scratch consumed/occupied; no replay')
    check(not cancelled(), 'cancelled before inert source loading')
    check(_ACTIVE_DESCRIPTOR_ERRORS is None, 'descriptor case already active/ambiguous; no reentry')
    used = 0
    def record(kind, **values):
        nonlocal used
        data = {'kind': kind, **values}
        used += len(json.dumps(data, sort_keys=True, ensure_ascii=True).encode()) + 1
        check(used <= MAX_EMITTED_BYTES, 'control evidence byte cap; caller retains failure')
        emit(data)
    started = time.monotonic()
    results = []
    with process_denials():
        subject = types.ModuleType('passvault_exact_successor_inert_subject')
        subject.__file__ = str(TARGET)
        # This is explicit, separately admitted execution of ONLY the reviewed
        # successor module definitions, never main() or an archived runner.
        exec(compile(source_bytes, str(TARGET), 'exec'), subject.__dict__)
        record('controls_source_loaded', runner_sha256=TARGET_SHA256,
               expected_cases=[name for name, _, _ in CASES], no_real_child_or_signal=True)
        for name, boundary, operation in CASES:
            if cancelled() or time.monotonic() - started > MAX_TOTAL_SECONDS:
                record('controls_interrupted_before_case', next_case=name)
                break
            check(directory_pin(os.fstat(scratch_fd)) == original_scratch
                  == directory_pin(os.stat(SCRATCH, follow_symlinks=False)), 'scratch original drift')
            case_started = time.monotonic()
            area = None
            descriptor_errors = []
            _ACTIVE_DESCRIPTOR_ERRORS = descriptor_errors
            result = {'name': name, 'boundary': boundary, 'outcome': 'FAIL', 'cleanup': 'HOLD'}
            try:
                area = Area(name, scratch_fd, SCRATCH, record)
                with mock.patch.object(subject, 'R', area.path), mock.patch.object(subject, 'E', area.path), \
                        mock.patch.object(subject, 'CANCEL', set()):
                    result['evidence'] = operation(subject, area)
                check(not cancelled(), 'launcher cancellation latched during case')
                result['outcome'] = 'PASS'
            except BaseException as error:
                result['error'] = type(error).__name__ + ': ' + str(error)
                descriptor_errors.extend(descriptor_uncertainties(error))
            finally:
                if area is not None:
                    try:
                        area.cleanup()
                        result['namespace_cleanup'] = 'ORIGINAL_SYNTHETIC_MEMBERS_REMOVED'
                        result['cleanup'] = 'ORIGINAL_SYNTHETIC_MEMBERS_REMOVED'
                    except BaseException as error:
                        result['cleanup_error'] = type(error).__name__ + ': ' + str(error)
                        result['outcome'] = 'FAIL'
                        descriptor_errors.extend(descriptor_uncertainties(error))
                    finally:
                        try:
                            area.close_retained_fds()
                        except BaseException as error:
                            result['descriptor_cleanup_error'] = type(error).__name__ + ': ' + str(error)
                            descriptor_errors.append(result['descriptor_cleanup_error'])
                if descriptor_errors:
                    result['cleanup'], result['outcome'] = 'HOLD', 'FAIL'
                    result['descriptor_cleanup_errors'] = descriptor_errors
                result['descriptor_settlement_hold'] = bool(descriptor_errors)
                result['seconds'] = time.monotonic() - case_started
                result['within_case_soft_bound'] = result['seconds'] <= MAX_CASE_SECONDS
                if not result['within_case_soft_bound']:
                    result['outcome'] = 'FAIL'  # Required cleanup was still attempted first.
                if cancelled():
                    result['outcome'], result['cancelled'] = 'FAIL', True
                results.append(result)
                record('control_case_result', **result)
                # End only after durable case disposition, never clear uncertainty
                # while the subject or required cleanup can still swallow an error.
                _ACTIVE_DESCRIPTOR_ERRORS = None
            if result['cleanup'] == 'HOLD':
                break
    check(directory_pin(os.fstat(scratch_fd)) == original_scratch
          == directory_pin(os.stat(SCRATCH, follow_symlinks=False)), 'final original scratch drift')
    empty = not os.listdir(scratch_fd)
    elapsed = time.monotonic() - started
    successful = (len(results) == len(CASES) and all(row['outcome'] == 'PASS' for row in results)
                  and empty and not cancelled() and elapsed <= MAX_TOTAL_SECONDS)
    report = {'status': 'CONTROLS_PASS_NOT_APPLICATION_OR_RUNTIME_ADMISSION' if successful else 'CONTROLS_FAIL_OR_HOLD',
              'runner_sha256': TARGET_SHA256, 'expected': len(CASES), 'executed': len(results),
              'passed': sum(row['outcome'] == 'PASS' for row in results),
              'seconds': elapsed, 'within_total_soft_bound': elapsed <= MAX_TOTAL_SECONDS,
              'not_run': [name for name, _, _ in CASES[len(results):]], 'scratch_empty': empty,
              'scratch_root_retained_for_original_launcher_closeout': True,
              'real_child_processes_started': 0, 'real_process_signals_sent': 0,
              'application_tests_executed': 0, 'wrapper_stop': 'NOT_APPLICABLE_NO_WRAPPER_LAUNCH',
              'limitations': ['NO_MAIN_OR_WHOLE_ADMISSION_EXECUTION', 'NO_REAL_PROCESS_PIDFD_WAITPID_OR_SIGNAL_RACE_PROOF',
                              'NO_OS_EXCLUSIVITY_OR_HOSTILE_UID_SANDBOX', 'NO_GRADLE_OR_105_APPLICATION_CASE_PASS'],
              'results': results}
    record('controls_preterminal_report', report=report)
    return report


if __name__ == '__main__':
    raise SystemExit('REFUSED: no standalone CLI; separately reviewed root launcher/admission required')
