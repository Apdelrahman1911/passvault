#!/usr/bin/python3
"""Proposed callable source, NOT execution, scratch or closeout admission.

Root's separately reviewed once-only launcher supplies a new original scratch,
exact source/data bytes, durable evidence and latch-only cancellation handling.
Only selected literal components run; no helper import/main/admission/runner,
real runtime, lock, process, build, traversal or deletion helper is executed.
"""

import ast
import hashlib
import json
import os
from pathlib import Path
import stat
import time


SCRATCH = Path('/root/projects/PassVault/audit-runtime-linux-closeout-contract-controls-01')
SOURCE_SHA256 = '8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517'
SOURCE_BYTES = 59609
SOURCE_LF = 1100
METHODS_SHA256 = '40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979'
METHODS_BYTES = 73265
METHODS_LF = 1207
COMMIT = '9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed'
TREE = '05014e9f635131d5db06701e4013b4b5a746465a'
TOTAL_SECONDS = 60
CLEANUP_SECONDS = 15
FILE_CAP = 128 * 1024
EVIDENCE_FILE_CAP = 4096
FIXTURE_BYTES_CAP = 512 * 1024
PACKET_BYTES_CAP = 512 * 1024
TICK_CAP = 200000
MARKER = 'SYNTHETIC_CONTRACT_CONTROL_NOT_ORIGINAL_RUN_OR_ADMISSION_EVIDENCE'
SELECTED = (
    'Hold', 'require', 'canonical', 'sha', 'strict_json', 'identity', 'absolute',
    'relative', 'under', 'Directories', 'event_rows', 'parse_journal',
    'run_contract', 'original_targets',
)
LITERALS = ('COMMIT', 'TREE', 'METHODS_SHA', 'PRIVATE', 'GENERATED', 'LABELS')
INPUT_NAMES = ('runner.input', 'plan.input', 'source.input', 'methods.input')
EVIDENCE_NAMES = (
    'LAUNCHER-stdout.log', 'LAUNCHER-stderr.log',
    'LAUNCHER-resources.json', 'LAUNCHER-processes.json',
)
NAMES = INPUT_NAMES + EVIDENCE_NAMES
CASES = (
    'L01_valid_stop', 'L02_valid_no_gradle', 'L03_legacy_string_missing_rows',
    'L04_legacy_list_directory', 'L05_rows_wrong_type', 'L06_rows_missing',
    'L07_rows_extra', 'L08_rows_reordered', 'L09_rows_duplicate',
    'L10_rows_unknown', 'L11_row_nondict', 'L12_row_missing_key',
    'L13_duplicate_json_key', 'L14_escaped_duplicate_json_key',
    'L15_durable_digest_mismatch', 'L16_durable_pin_mismatch', 'L17_row_extra_key',
)
RETURN_CASES = frozenset((CASES[0], CASES[1], CASES[14], CASES[15]))
_CALLED = False


class ControlFailure(Exception):
    pass


class ControlCancelled(ControlFailure):
    pass


def check(value, reason):
    if not value:
        raise ControlFailure(reason)


def digest(data):
    return hashlib.sha256(data).hexdigest()


def stat_pin(value, directory=False):
    fields = ('dev', 'ino', 'uid', 'mode') if directory else (
        'dev', 'ino', 'uid', 'mode', 'nlink', 'size', 'mtime_ns', 'ctime_ns')
    return {key: getattr(value, 'st_' + key) for key in fields}


def fd_pin(fd, directory=False):
    return stat_pin(os.fstat(fd), directory)


def rejection(operation, exception_type, reason):
    try:
        operation()
    except exception_type as error:
        check(type(error) is exception_type and error.args == (reason,), 'different rejection/arguments')
        return {'type': type(error).__name__, 'reason': reason}
    raise ControlFailure('required rejection was not raised: ' + reason)


class Arena:
    """Eight fixed originals; never owns/closes/removes the borrowed scratch FD."""

    def __init__(self, scratch_fd, original_scratch):
        self.fd, self.original = scratch_fd, dict(original_scratch)
        # Slots and FD fields exist before the corresponding allocation syscall.
        parts = ('/', *SCRATCH.parent.parts[1:])
        self.parents = [{'name': part, 'fd': None, 'pin': None, 'parent': None} for part in parts]
        self.files = [{'name': name, 'fd': None, 'reader': None, 'original': None, 'pin': None,
                       'data': None, 'sha256': None, 'attempted': False, 'created': False,
                       'unlink_attempted': False, 'removed': False} for name in NAMES]
        self.uncertain, self.cleaned = False, False
        self.bytes_written = 0

    def start(self):
        parent = None
        for row in self.parents:
            row['parent'] = parent
            row['fd'] = os.open(row['name'], os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                                dir_fd=parent)
            row['pin'] = fd_pin(row['fd'], True)
            check(stat.S_ISDIR(row['pin']['mode']), 'scratch ancestor type')
            parent = row['fd']
        self.verify_root()
        check(self.members() == set(), 'scratch must be initially empty; no adoption')

    def members(self):
        result = set()
        with os.scandir(self.fd) as entries:
            for entry in entries:
                check(len(result) < len(NAMES), 'scratch member count exceeds fixed allowlist')
                result.add(entry.name)
        return result

    def verify_root(self):
        check(fd_pin(self.fd, True) == self.original, 'borrowed original scratch descriptor drift')
        check(self.original['uid'] == os.getuid()
              and self.original['mode'] == stat.S_IFDIR | 0o700, 'scratch must be owned0700')
        for row in self.parents:
            check(row['fd'] is not None and row['pin'] is not None
                  and fd_pin(row['fd'], True) == row['pin'], 'original scratch ancestor drift')
            if row['parent'] is not None:
                check(stat_pin(os.stat(row['name'], dir_fd=row['parent'], follow_symlinks=False), True)
                      == row['pin'], 'scratch ancestor name replaced')
        check(stat_pin(os.stat(SCRATCH.name, dir_fd=self.parents[-1]['fd'], follow_symlinks=False), True)
              == self.original, 'original scratch pathname replaced')

    def directory_pins(self):
        result, path = {}, Path('/')
        for row in self.parents:
            path = Path('/') if row['name'] == '/' else path / row['name']
            result[str(path)] = dict(row['pin'])
        result[str(SCRATCH)] = dict(self.original)
        return result

    def find(self, name):
        matches = [row for row in self.files if row['name'] == name]
        check(len(matches) == 1, 'nonallowlisted fixture name')
        return matches[0]

    def create(self, name, data):
        row = self.find(name)
        cap = FILE_CAP if name in INPUT_NAMES else EVIDENCE_FILE_CAP
        check(type(data) is bytes and len(data) <= cap and not row['attempted'], 'fixed fixture cap/one-shot')
        self.verify_root()
        try:
            os.stat(name, dir_fd=self.fd, follow_symlinks=False)
        except FileNotFoundError:
            pass
        else:
            raise ControlFailure('occupied fixture name; never adopt/retry')
        row['attempted'] = True
        try:
            row['fd'] = os.open(name, os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                                0o600, dir_fd=self.fd)
            row['created'] = True
            row['original'] = fd_pin(row['fd'])
            row['pin'] = dict(row['original'])
            check(row['original']['uid'] == os.getuid() and row['original']['mode'] == stat.S_IFREG | 0o600
                  and row['original']['nlink'] == 1 and row['original']['size'] == 0,
                  'exclusive private empty fixture original')
            check(stat_pin(os.stat(name, dir_fd=self.fd, follow_symlinks=False)) == row['pin'],
                  'new fixture original name/FD mismatch')
            row['reader'] = os.open(name, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=self.fd)
            check(fd_pin(row['reader']) == row['pin'], 'original fixture reader mismatch')
            # Both originals are retained before the first write. A partial or
            # uncertain write is HOLD, not an automatic completion/retry loop.
            count = os.write(row['fd'], data)
            self.bytes_written += count
            check(count == len(data) and self.bytes_written <= FIXTURE_BYTES_CAP, 'fixture short write/total cap')
            os.fsync(row['fd'])
            os.fsync(self.fd)
            after = fd_pin(row['fd'])
            check(all(after[key] == row['original'][key] for key in ('dev', 'ino', 'uid', 'mode', 'nlink'))
                  and after['size'] == len(data), 'fixture original changed during own write')
            row['pin'], row['data'], row['sha256'] = after, data, digest(data)
            self.verify_file(row, contents=True)
        except BaseException:
            self.uncertain = True
            raise

    def verify_file(self, row, contents=False):
        self.verify_root()
        check(row['created'] and not row['removed'] and row['fd'] is not None and row['reader'] is not None,
              'fixture original registry incomplete')
        check(fd_pin(row['fd']) == fd_pin(row['reader']) == row['pin']
              == stat_pin(os.stat(row['name'], dir_fd=self.fd, follow_symlinks=False)),
              'original fixture full pin/name drift')
        check(row['pin']['nlink'] == 1, 'fixture acquired unknown hardlink')
        if contents:
            data = os.pread(row['reader'], FILE_CAP + 1, 0)
            check(data == row['data'] and digest(data) == row['sha256']
                  and len(data) == row['pin']['size'] <= FILE_CAP, 'actual original fixture bytes changed')
            check(fd_pin(row['fd']) == fd_pin(row['reader']) == row['pin'], 'fixture readback pin drift')

    def cleanup(self, subject_failures):
        failures = list(subject_failures)
        removed, deadline = 0, time.monotonic() + CLEANUP_SECONDS
        try:
            check(not self.cleaned, 'fixture cleanup cannot be replayed')
            self.cleaned = True
            check(not self.uncertain and not failures, 'uncertain fixture/subject settlement; retain')
            self.verify_root()
            owned = [row for row in self.files if row['created']]
            check(self.members() == {row['name'] for row in owned}, 'unknown/missing scratch member; retain')
            # Complete member/original/content preflight before the first unlink.
            for row in owned:
                check(time.monotonic() <= deadline, 'cleanup preflight deadline')
                self.verify_file(row, contents=True)
            for row in owned:
                check(time.monotonic() <= deadline, 'cleanup removal deadline')
                self.verify_file(row)
                row['unlink_attempted'] = True
                os.unlink(row['name'], dir_fd=self.fd)
                after = fd_pin(row['fd'])
                check(after['nlink'] == 0 and all(after[k] == row['pin'][k]
                      for k in ('dev', 'ino', 'uid', 'mode', 'size', 'mtime_ns'))
                      and fd_pin(row['reader']) == after, 'original unlink transition not proved')
                try:
                    os.stat(row['name'], dir_fd=self.fd, follow_symlinks=False)
                except FileNotFoundError:
                    pass
                else:
                    raise ControlFailure('removed original name reoccupied; no adoption')
                os.fsync(self.fd)
                row['removed'], row['pin'] = True, after
                removed += 1  # Completed proof+parent fsync, not merely unlink's return.
            self.verify_root()
            check(self.members() == set(), 'scratch not empty after original-only cleanup')
        except BaseException as error:
            self.uncertain = True
            failures.append(type(error).__name__ + ': ' + str(error)[:200])
        finally:
            failures.extend(self.close_descriptors())
        return {'status': 'COMPLETE' if not failures else 'HOLD', 'failures': failures,
                'created_files': sum(row['created'] for row in self.files),
                'unlink_attempted_names': [row['name'] for row in self.files if row['unlink_attempted']],
                'durably_removed_names': removed, 'bytes_written': self.bytes_written,
                'borrowed_scratch_fd_closed_or_removed': False}

    def close_descriptors(self):
        failures = []
        for row in reversed(self.files):
            for key in ('reader', 'fd'):
                fd, row[key] = row[key], None
                if fd is not None:
                    try:
                        os.close(fd)
                    except OSError as error:
                        failures.append('fixture close uncertainty: ' + str(error)[:100])
        for row in reversed(self.parents):
            fd, row['fd'] = row['fd'], None
            if fd is not None:
                try:
                    os.close(fd)
                except OSError as error:
                    failures.append('ancestor close uncertainty: ' + str(error)[:100])
        return failures


class ObservedOS:
    """Real read/fsync delegate with fixed path/FD bounds; no fake success I/O."""

    O_RDONLY, O_DIRECTORY, O_NOFOLLOW = os.O_RDONLY, os.O_DIRECTORY, os.O_NOFOLLOW
    O_CLOEXEC, O_NONBLOCK = os.O_CLOEXEC, os.O_NONBLOCK

    def __init__(self, arena, poll):
        self.arena, self.poll = arena, poll
        self.slots = [{'fd': None, 'path': None, 'directory': None, 'pin': None} for _ in range(6)]
        self.counts = dict.fromkeys(('directory_opens', 'file_opens', 'file_closes', 'reads',
                                    'eofs', 'read_bytes', 'file_fsyncs', 'directory_fsyncs'), 0)
        self.file_paths, self.failures = [], []

    def slot(self, fd):
        rows = [row for row in self.slots if row['fd'] == fd]
        check(len(rows) == 1, 'source accessed unowned/already-closed descriptor')
        return rows[0]

    def open(self, name, flags, mode=0o777, *, dir_fd=None):
        self.poll()
        parent = self.slot(dir_fd) if dir_fd is not None else None
        check(parent is None or parent['directory'], 'source input parent is not a directory')
        if parent is None:
            check(name == '/', 'only fixed root may be opened without parent')
            path = '/'
        else:
            check(isinstance(name, str) and name not in ('', '.', '..') and '/' not in name,
                  'source open must use a single fixed leaf')
            path = str(Path(parent['path']) / name)
        directory = bool(flags & os.O_DIRECTORY)
        if directory:
            check(path in self.arena.directory_pins()
                  and flags == os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                  'unadmitted source directory open')
        else:
            check(path in {str(SCRATCH / name) for name in EVIDENCE_NAMES}
                  and flags == os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC,
                  'unadmitted source file open')
        free = [row for row in self.slots if row['fd'] is None]
        check(free, 'source descriptor cap')
        row = free[0]
        row['path'], row['directory'], row['pin'] = path, directory, None
        row['fd'] = os.open(name, flags, mode, dir_fd=dir_fd)
        try:
            row['pin'] = fd_pin(row['fd'], directory)
            self.counts['directory_opens' if directory else 'file_opens'] += 1
            if not directory:
                self.file_paths.append(path)
            return row['fd']
        except BaseException:
            self.close(row['fd'])
            raise

    def fstat(self, fd):
        self.slot(fd)
        return os.fstat(fd)

    def stat(self, name, *, dir_fd, follow_symlinks):
        parent = self.slot(dir_fd)
        check(parent['directory'] and follow_symlinks is False
              and isinstance(name, str) and name not in ('', '.', '..') and '/' not in name,
              'source stat outside no-follow parent/leaf contract')
        path = str(Path(parent['path']) / name)
        check(path in self.arena.directory_pins()
              or path in {str(SCRATCH / leaf) for leaf in EVIDENCE_NAMES}, 'unadmitted source stat')
        return os.stat(name, dir_fd=dir_fd, follow_symlinks=False)

    def read(self, fd, amount):
        row = self.slot(fd)
        check(not row['directory'] and type(amount) is int and 0 < amount <= 65536, 'source read cap/type')
        data = os.read(fd, amount)
        self.counts['reads'] += 1
        self.counts['read_bytes'] += len(data)
        self.counts['eofs'] += not data
        return data

    def fsync(self, fd):
        row = self.slot(fd)
        os.fsync(fd)
        self.counts['directory_fsyncs' if row['directory'] else 'file_fsyncs'] += 1

    def close(self, fd):
        row = self.slot(fd)
        directory, row['fd'] = row['directory'], None  # One close attempt, never automatic retry.
        try:
            os.close(fd)
            if not directory:
                self.counts['file_closes'] += 1
        except OSError as error:
            self.failures.append('source close uncertainty: ' + str(error)[:100])
            raise

    def files_closed(self):
        check(not any(row['fd'] is not None and not row['directory'] for row in self.slots),
              'source input FD remains live')
        check(not self.failures, 'source descriptor close was uncertain')


def extract(source_bytes):
    # Execution of this extractor itself requires the separate future admission.
    # Neither the helper's module statements nor its admission/main is compiled.
    text = source_bytes.decode('utf-8')
    tree = ast.parse(text, filename='<exact-closeout-source-data>')
    nodes = []
    for name in LITERALS:
        matches = [n for n in tree.body if isinstance(n, ast.Assign) and len(n.targets) == 1
                   and isinstance(n.targets[0], ast.Name) and n.targets[0].id == name]
        check(len(matches) == 1, 'selected literal missing/ambiguous')
        value = ast.literal_eval(matches[0].value)
        check(isinstance(value, (str, tuple)), 'nonliteral subject constant')
        nodes.append(matches[0])
    for name in SELECTED:
        matches = [n for n in tree.body if isinstance(n, (ast.FunctionDef, ast.ClassDef)) and n.name == name]
        check(len(matches) == 1 and not matches[0].decorator_list, 'selected definition missing/decorated')
        nodes.append(matches[0])
    matches = [n for n in tree.body if isinstance(n, ast.FunctionDef) and n.name == 'admission']
    check(len(matches) == 1 and isinstance(matches[0].body[-1], ast.Return)
          and isinstance(matches[0].body[-2], ast.For), 'final admission loop location changed')
    loop = matches[0].body[-2]
    check(isinstance(loop.iter, ast.Subscript) and isinstance(loop.iter.value, ast.Name)
          and loop.iter.value.id == 'launcher' and isinstance(loop.iter.slice, ast.Constant)
          and loop.iter.slice.value == 'evidence_files', 'wrong admission loop selected')
    wrapper = ast.parse('def consume_evidence(dirs, launcher, captured, pins):\n    pass\n').body[0]
    wrapper.body = [loop]  # Literal loop node/body; no predicate or statement substitution.
    nodes.append(wrapper)
    literal = b''.join(source_bytes.splitlines(keepends=True)[loop.lineno - 1:loop.end_lineno])
    metadata = {'definitions': list(SELECTED), 'literal_assignments': list(LITERALS),
                'consumer_lines': [loop.lineno, loop.end_lineno], 'consumer_bytes': len(literal),
                'consumer_sha256': digest(literal), 'consumer_physical_lf': literal.count(b'\n')}
    return compile(ast.fix_missing_locations(ast.Module(body=nodes, type_ignores=[])),
                   '<exact-selected-closeout-components>', 'exec'), metadata


class Subject:
    def __init__(self, arena, poll):
        self.arena, self.poll = arena, poll
        self.os = ObservedOS(arena, poll)
        self.dirs, self.ns, self.ticks = None, None, 0

    def tick(self):
        self.ticks += 1
        check(self.ticks <= TICK_CAP, 'subject tick cap')
        self.poll()

    def start(self, code):
        runtime = SCRATCH / 'SYNTHETIC-RUNTIME-NOT-CREATED'
        files = {
            'runner_sha256': SCRATCH / INPUT_NAMES[0], 'runner_plan_sha256': SCRATCH / INPUT_NAMES[1],
            'source_sha256': SCRATCH / INPUT_NAMES[2], 'methods_sha256': SCRATCH / INPUT_NAMES[3],
            'runner_request_sha256': SCRATCH / 'MEMORY-REQUEST-NOT-CREATED.json',
            'run_result_sha256': SCRATCH / 'MEMORY-RESULT-NOT-CREATED.json',
            'run_originals_sha256': SCRATCH / 'MEMORY-ORIGINALS-NOT-CREATED.json',
            'launcher_sha256': SCRATCH / 'MEMORY-LAUNCHER-NOT-CREATED.json',
            'run_journal_sha256': SCRATCH / 'MEMORY-JOURNAL-NOT-CREATED.jsonl',
            'runner_accept_root_sha256': SCRATCH / 'MEMORY-ROOT-ACCEPTANCE-NOT-CREATED.json',
            'runner_accept_verification_sha256': SCRATCH / 'MEMORY-REVIEW-NOT-CREATED.json',
        }
        self.ns = {'os': self.os, 'hashlib': hashlib, 'json': json, 'Path': Path, 'stat': stat, 'time': time,
                   'UID': os.getuid(), 'MIB': 1024 ** 2, 'R': runtime, 'CHECKOUT': runtime / 'checkout',
                   'E': SCRATCH, 'LOCK': SCRATCH / 'SYNTHETIC-LOCK-NOT-CREATED', 'FILES': files,
                   'LAUNCHER_FILES': tuple(SCRATCH / name for name in EVIDENCE_NAMES)}
        exec(code, self.ns)
        check(self.ns['COMMIT'] == COMMIT and self.ns['TREE'] == TREE
              and self.ns['METHODS_SHA'] == METHODS_SHA256, 'literal subject constants changed')
        self.ns['TARGET_PATHS'] = tuple(runtime / p for p in self.ns['PRIVATE']) + tuple(
            self.ns['CHECKOUT'] / p for p in self.ns['GENERATED'])
        check(len(self.ns['TARGET_PATHS']) == 33, 'literal target projection count')
        self.dirs = self.ns['Directories'](self.tick)
        self.dirs.open(SCRATCH, True)
        check(self.dirs.pins == self.arena.directory_pins(), 'real subject bootstrap original directory map')

    def close(self):
        failures = []
        try:
            if self.dirs is not None:
                failures.extend(self.dirs.close())
        except BaseException as error:
            failures.append('source directory settlement exception: ' + type(error).__name__)
        finally:
            for row in self.os.slots:
                if row['fd'] is not None:
                    failures.append('source left a tracked FD; explicit owner fallback close required')
                    try:
                        self.os.close(row['fd'])
                    except OSError:
                        pass
            failures.extend(self.os.failures)
        return failures


def fixture_files(subject, methods_bytes):
    ns, canonical = subject.ns, subject.ns['canonical']
    paths = ['gradlew', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties',
             'synthetic/eol-a.ps1', 'synthetic/eol-b.ps1']
    paths += [f'synthetic/s{i:04}.txt' for i in range(1193)]
    source = {'format': 'passvault-linux-checkout-source-v1', 'commit': COMMIT, 'tree': TREE,
              'synthetic_fixture': MARKER,
              'row_model': 'Only path/mode schema; no actual source file/content/provenance assertion.',
              'files': [{'path': path, 'git_mode': '100755' if path == 'gradlew' else '100644'} for path in paths],
              'checkout_eol_qualifications': [
                  {'path': path, 'qualification': 'SYNTHETIC LF-to-CRLF model only; no inherited source coverage',
                   'git_blob_bytes': 2, 'git_blob_sha256': digest(b'x\n'),
                   'checkout_bytes': 3, 'checkout_sha256': digest(b'x\r\n')}
                  for path in paths[3:5]]}
    data = dict(zip(INPUT_NAMES, (b'INERT SYNTHETIC RUNNER DATA; NEVER EXECUTE\n',
                                 b'INERT SYNTHETIC PLAN DATA; NOT ADMISSION\n', canonical(source), methods_bytes)))
    data.update({EVIDENCE_NAMES[0]: b'SYNTHETIC stdout fixture; no command or child ran.\n',
                 EVIDENCE_NAMES[1]: b'SYNTHETIC stderr fixture; not actual launcher output.\n',
                 EVIDENCE_NAMES[2]: canonical({'synthetic_fixture': MARKER, 'actual_resource_observations': 0}),
                 EVIDENCE_NAMES[3]: canonical({'synthetic_fixture': MARKER, 'actual_process_observations': 0})})
    check(set(data) == set(NAMES) and sum(map(len, data.values())) <= FIXTURE_BYTES_CAP, 'fixture aggregate cap')
    for name in NAMES:
        check(len(data[name]) <= (FILE_CAP if name in INPUT_NAMES else EVIDENCE_FILE_CAP), 'fixture individual cap')
        subject.poll()
        subject.arena.create(name, data[name])
    check(ns['sha'](data['methods.input']) == ns['METHODS_SHA'], 'real frozen methods bytes required')
    return source


def packet(subject, source, stop_required):
    """Construct serialized test inputs, not fake filesystem/process observations."""
    ns, arena = subject.ns, subject.arena
    canonical, sha = ns['canonical'], ns['sha']
    runtime, checkout, files = ns['R'], ns['CHECKOUT'], ns['FILES']
    captured = {str(files[key]): arena.find(name)['data'] for key, name in zip(
        ('runner_sha256', 'runner_plan_sha256', 'source_sha256', 'methods_sha256'), INPUT_NAMES)}
    pins = {str(files[key]): dict(arena.find(name)['pin']) for key, name in zip(
        ('runner_sha256', 'runner_plan_sha256', 'source_sha256', 'methods_sha256'), INPUT_NAMES)}
    flags = ['--no-daemon', '--max-workers=1', '--no-parallel', '--no-configure-on-demand',
             '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict',
             '-Pkotlin.compiler.execution.strategy=in-process']
    command = [str(checkout / 'gradlew'), ':core:database:desktopTest', *flags]
    stop_command = [str(checkout / 'gradlew'), '--stop', *flags]
    environment = {'PATH': str(runtime / 'SYNTHETIC-JDK17-NOT-CREATED/bin'),
                   'JAVA_HOME': str(runtime / 'SYNTHETIC-JDK17-NOT-CREATED'),
                   'HOME': str(runtime / 'home'), 'TMPDIR': str(runtime / 'tmp'),
                   'GRADLE_USER_HOME': str(runtime / 'gradle-home'), 'LANG': 'C.UTF-8', 'TZ': 'UTC'}
    bindings = {'runner_sha256': sha(captured[str(files['runner_sha256'])]),
                'plan_sha256': sha(captured[str(files['runner_plan_sha256'])]),
                'source_sha256': sha(captured[str(files['source_sha256'])]),
                'methods_sha256': sha(captured[str(files['methods_sha256'])]),
                'command_sha256': sha(canonical(command)), 'stop_command_sha256': sha(canonical(stop_command)),
                'environment_sha256': sha(canonical(environment))}
    directory_pins = arena.directory_pins()
    lock_pin = {'dev': arena.original['dev'], 'ino': 9000000000000, 'uid': os.getuid(),
                'mode': stat.S_IFREG | 0o600, 'nlink': 1, 'size': 0, 'mtime_ns': 1, 'ctime_ns': 1}
    request = {'format': 'passvault-linux-database-request-v1', 'author': '/root',
               'run_id': 'linux-database-01', 'commit': COMMIT, 'tree': TREE,
               'runtime': str(runtime), 'evidence': str(ns['E']), 'lock': str(ns['LOCK']),
               'command': command, 'stop_command': stop_command, 'environment': environment,
               'limits': {'build_seconds': 3600, 'stop_seconds': 600, 'git_seconds': 180},
               'bindings': bindings, 'input_pins': pins, 'directory_pins': directory_pins,
               'lock_pin': lock_pin, 'synthetic_fixture': MARKER}
    captured[str(files['runner_request_sha256'])] = canonical(request)
    bindings = {**bindings, 'request_sha256': sha(captured[str(files['runner_request_sha256'])])}
    events, commands, owned = [], [], set()

    def event(kind, **values):
        # Deliberate synthetic interval at Unix epoch+1s. The subject's REAL
        # time.time_ns() comparison still runs; none of these is an observation.
        events.append({'sequence': len(events), 'time_ns': 1000000001 + len(events), 'kind': kind, **values})

    event('admission_under_original_lock', bindings=bindings, lock_pin=lock_pin,
          directory_pins=directory_pins, synthetic_fixture=MARKER)
    original = {key: dict(value) for key, value in directory_pins.items()}
    allocation_paths = (runtime, checkout, *ns['TARGET_PATHS'])
    parents = set(allocation_paths)
    for path in allocation_paths:
        while path != SCRATCH:
            check(SCRATCH in path.parents, 'synthetic allocation escaped scratch')
            path = path.parent
            parents.add(path)
    for index, path in enumerate(sorted(parents - {SCRATCH}, key=lambda p: (len(p.parts), str(p)))):
        fake = {'dev': arena.original['dev'], 'ino': 8000000000000 + index,
                'uid': os.getuid(), 'mode': stat.S_IFDIR | 0o700}
        check(all(fake != pin for pin in original.values()) and fake['ino'] != lock_pin['ino'],
              'synthetic original identities must be distinct')
        original[str(path)] = fake

    def allocate(path):
        event('allocation_intent', path=str(path), parent=original[str(path.parent)])
        event('allocation_original', path=str(path), identity=original[str(path)])

    def command_events(label, argv, obligation, budget):
        pid, birth = 80000001 + len(commands), 90000001 + len(commands)
        initial = {'label': label, 'argv': argv, 'cwd': str(checkout),
                   'started': False, 'exit': None, 'complete': False, 'errors': []}
        event('launch_intent', **initial, stop_required=obligation,
              environment_sha256=bindings['environment_sha256'], budget_seconds=budget)
        event('launch_observed', label=label, pid=pid)
        event('owned_process', pid=pid, start=birth)
        owned.add((pid, birth))
        record = {**initial, 'started': True, 'exit': 0, 'complete': True,
                  'pid': pid, 'log_bytes': 0, 'log_sha256': sha(b'')}
        commands.append(record)
        event('command_result', **record)

    allocate(runtime)
    allocate(checkout)
    for path in ns['TARGET_PATHS'][:len(ns['PRIVATE'])]:
        allocate(path)
    git = str(runtime / 'SYNTHETIC-GIT-NOT-CREATED')
    command_events('clone', [git, 'clone', '--no-checkout', str(SCRATCH / 'SYNTHETIC-REPOSITORY'),
                             str(checkout)], False, 180)
    command_events('checkout', [git, '-C', str(checkout), 'checkout', '--detach', COMMIT], False, 180)
    command_events('identity-before', [git, 'rev-parse', 'HEAD', 'HEAD^{tree}'], False, 180)
    command_events('members-before', [git, 'ls-files', '-z'], False, 180)
    event('source_bound', phase='before', files=1198, commit=COMMIT, tree=TREE)
    for path in ns['TARGET_PATHS'][len(ns['PRIVATE']):]:
        allocate(path)
    if stop_required:
        command_events('database', command, True, 3600)
        command_events('stop', stop_command, True, 600)
    else:
        event('synthetic_failure_before_database', reason='after all allocations/source binding; no Gradle intent')
    command_events('identity-after', [git, 'rev-parse', 'HEAD', 'HEAD^{tree}'], stop_required, 180)
    command_events('members-after', [git, 'ls-files', '-z'], stop_required, 180)
    event('source_bound', phase='after', files=1198, commit=COMMIT, tree=TREE)
    runtime_disposition = 'RETAINED_IMMEDIATE_REVIEWED_ROOT_CLOSEOUT_REQUIRED'
    result = {'status': 'PRETERMINAL_OBSERVATIONS_NOT_A_PASS', 'runtime': str(runtime),
              'runtime_disposition': runtime_disposition, 'semantic_disposition': 'NOT_INDEPENDENTLY_VERIFIED',
              'owned_settled': True, 'commands': commands, 'stop_required': stop_required,
              'stop_attempted': stop_required, 'stop_ok': stop_required,
              'build_ok': stop_required, 'source_ok': True, 'xml_mapping_ok': stop_required,
              'errors': [] if stop_required else ['SYNTHETIC pre-database failure'], 'synthetic_fixture': MARKER}
    event('terminal_commit', exit=0 if stop_required else 1, operational_settled=stop_required,
          semantic_disposition='NOT_INDEPENDENTLY_VERIFIED', runtime_disposition=runtime_disposition)
    for key, reviewer, role in (
        ('runner_accept_root_sha256', '/root', 'OWNER_COAUTHOR_APPROVAL'),
        ('runner_accept_verification_sha256', '/root/verification', 'INDEPENDENT_SOURCE_REVIEW'),
    ):
        accepted = {'format': 'passvault-linux-database-acceptance-v1', 'reviewer': reviewer, 'review_role': role,
                    'runner_authors': ['/root/storage', '/root'],
                    'purpose': 'ONE_SHOT_LINUX_DATABASE_EXECUTION_AND_RETAINED_CLOSEOUT_CONTRACT',
                    'disposition': 'ACCEPT', 'bindings': bindings, 'commit': COMMIT, 'tree': TREE,
                    'runtime': str(runtime), 'evidence': str(ns['E']),
                    'obligations': {f'F{i:02}': 'ACCEPT' for i in range(1, 8)}, 'synthetic_fixture': MARKER}
        captured[str(files[key])] = canonical(accepted)
    launcher = {'format': 'passvault-linux-database-launcher-v1', 'author': '/root', 'run_id': 'linux-database-01',
                'runtime': str(runtime), 'evidence': str(ns['E']),
                'argv': ['/usr/bin/python3', '-I', '-B', str(files['runner_sha256'])],
                'completed': True, 'runner_exit': 0 if stop_required else 1, 'outer_exit': 0 if stop_required else 1,
                'no_other_audit_local_or_ci_job': True, 'external_timeout_observed': False,
                'external_interruption_observed': False, 'started_unix_ns': 1000000000,
                'finished_unix_ns': events[-1]['time_ns'] + 1,
                'bindings': {**bindings,
                    'accept_root_sha256': sha(captured[str(files['runner_accept_root_sha256'])]),
                    'accept_verification_sha256': sha(captured[str(files['runner_accept_verification_sha256'])])},
                'limitations': [MARKER, 'No original runtime/command/process/acceptance evidence.'],
                'evidence_files': [{'path': str(SCRATCH / name), 'sha256': arena.find(name)['sha256'],
                                    'identity': dict(arena.find(name)['pin'])} for name in EVIDENCE_NAMES],
                'synthetic_fixture': MARKER}
    captured.update({str(files['run_result_sha256']): canonical(result),
                     str(files['run_originals_sha256']): canonical(original),
                     str(files['run_journal_sha256']): b''.join(canonical(row) for row in events),
                     str(files['launcher_sha256']): canonical(launcher)})
    check(sum(map(len, captured.values())) + sum(len(arena.find(n)['data']) for n in EVIDENCE_NAMES)
          <= PACKET_BYTES_CAP, 'serialized synthetic packet aggregate cap')
    return {'captured': captured, 'pins': dict(pins), 'launcher': launcher,
            'expected': (request, result, original, launcher, events, source, owned)}


def exercise(subject, source, case):
    ns, arena = subject.ns, subject.arena
    data = packet(subject, source, case != 'L02_valid_no_gradle')
    captured, pins, launcher = data['captured'], data['pins'], data['launcher']
    launcher_key = str(ns['FILES']['launcher_sha256'])
    before_counts, before_paths = dict(subject.os.counts), len(subject.os.file_paths)
    rows, reason, error_type = launcher['evidence_files'], 'exact bounded launcher evidence set', ns['Hold']
    if case == 'L03_legacy_string_missing_rows':
        del launcher['evidence_files']
        error_type, reason = KeyError, 'evidence_files'
    elif case == 'L04_legacy_list_directory':
        launcher['evidence'] = rows  # evidence_files remains present; no missing-key masking.
        reason = 'external completed launcher missing/ambiguous'
    elif case == 'L05_rows_wrong_type':
        launcher['evidence_files'] = str(ns['E'])
    elif case == 'L06_rows_missing':
        launcher['evidence_files'] = rows[:-1]
    elif case == 'L07_rows_extra':
        launcher['evidence_files'] = rows + [dict(rows[0])]
    elif case == 'L08_rows_reordered':
        launcher['evidence_files'] = [rows[1], rows[0], rows[2], rows[3]]
    elif case == 'L09_rows_duplicate':
        launcher['evidence_files'] = [rows[0], dict(rows[0]), rows[2], rows[3]]
    elif case == 'L10_rows_unknown':
        rows[0]['path'] = str(SCRATCH / 'UNKNOWN-NEVER-OPEN')
    elif case == 'L11_row_nondict':
        rows[0] = 17
    elif case == 'L12_row_missing_key':
        del rows[0]['path']
    elif case == 'L15_durable_digest_mismatch':
        actual = arena.find(EVIDENCE_NAMES[0])
        rows[0]['sha256'] = ('0' if actual['sha256'][0] != '0' else '1') + actual['sha256'][1:]
        check(rows[0]['identity'] == actual['pin'] and rows[0]['sha256'] != actual['sha256'],
              'digest negative must preserve the exact actual full pin')
    elif case == 'L16_durable_pin_mismatch':
        actual = arena.find(EVIDENCE_NAMES[0])
        rows[0]['identity']['mtime_ns'] += 1
        check(rows[0]['sha256'] == actual['sha256'] and rows[0]['identity'] != actual['pin'],
              'pin negative must preserve the exact actual digest')
    elif case == 'L17_row_extra_key':
        rows[0]['extra'] = 'must not be accepted'
    captured[launcher_key] = ns['canonical'](launcher)
    if case in ('L13_duplicate_json_key', 'L14_escaped_duplicate_json_key'):
        key = b'"evidence"' if case == 'L13_duplicate_json_key' else b'"\\u0065vidence"'
        captured[launcher_key] = captured[launcher_key][:-2] + b',' + key + b':' + ns['canonical'](str(ns['E']))[:-1] + b'}\n'
        reason = 'duplicate JSON key'
    check(sum(map(len, captured.values())) + sum(len(arena.find(n)['data']) for n in EVIDENCE_NAMES)
          <= PACKET_BYTES_CAP, 'mutated serialized packet cap')
    packet_hashes = {key: digest(value) for key, value in captured.items()}
    expected_reads, outcome = [], None
    returned = False
    if case in RETURN_CASES:
        result = ns['run_contract'](captured, pins, subject.tick)
        check(type(result) is tuple and len(result) == 7 and result == data['expected'],
              'complete real run_contract normal seven-value return required')
        returned = True
        projected = ns['original_targets'](result[4], result[2], subject.tick)
        check(len(projected) == 33 and [r['path'] for r in projected] == [str(p) for p in ns['TARGET_PATHS']],
              'full pure synthetic35-original/33-target projection required')
        before_captured, before_pins = dict(captured), dict(pins)
        if case in ('L15_durable_digest_mismatch', 'L16_durable_pin_mismatch'):
            outcome = rejection(lambda: ns['consume_evidence'](subject.dirs, result[3], captured, pins),
                                ns['Hold'], 'durable launcher evidence mismatch')
            expected_reads = [str(SCRATCH / EVIDENCE_NAMES[0])]
            check(captured == before_captured and pins == before_pins,
                  'mismatched first durable row must not be captured or followed by later I/O')
        else:
            check(ns['consume_evidence'](subject.dirs, result[3], captured, pins) is None,
                  'literal evidence loop unexpected return')
            expected_reads = [str(SCRATCH / name) for name in EVIDENCE_NAMES]
            expected_captured, expected_pins = dict(before_captured), dict(before_pins)
            for name in EVIDENCE_NAMES:
                original = arena.find(name)
                expected_captured[str(SCRATCH / name)] = original['data']
                expected_pins[str(SCRATCH / name)] = original['pin']
            check(captured == expected_captured and pins == expected_pins,
                  'literal loop did not capture all and only exact durable original evidence')
            outcome = {'normal_return': True, 'durable_evidence_files': 4}
    else:
        outcome = rejection(lambda: ns['run_contract'](captured, pins, subject.tick), error_type, reason)
    subject.os.files_closed()
    observed_paths = subject.os.file_paths[before_paths:]
    counts = {key: value - before_counts[key] for key, value in subject.os.counts.items()}
    expected_bytes = sum(len(arena.find(Path(path).name)['data']) for path in expected_reads)
    check(observed_paths == expected_reads and counts == {
        'directory_opens': 0, 'file_opens': len(expected_reads), 'file_closes': len(expected_reads),
        'reads': 2 * len(expected_reads), 'eofs': len(expected_reads), 'read_bytes': expected_bytes,
        'file_fsyncs': len(expected_reads), 'directory_fsyncs': len(expected_reads)},
        'real consumer path/read/EOF/fsync/closed-FD evidence differs')
    arena.verify_root()
    return {'run_contract_returned': returned, 'stop_branch': case != 'L02_valid_no_gradle',
            'outcome': outcome, 'packet_sha256s': packet_hashes,
            'consumer_paths': observed_paths, 'consumer_io': counts,
            'synthetic_source_rows': 1198, 'synthetic_allocation_records': 35,
            'original_targets_pure_projection': 33 if returned else 'not reached',
            'actual_runtime_allocations': 0, 'actual_source_files': 0,
            'actual_commands_processes_locks': 0, 'actual_application_test_cases': 0}


def run_contract_controls(*, scratch_fd, scratch_path, original_scratch,
                          source_bytes, methods_bytes, emit, cancelled):
    """Only root's separately admitted new original instance may call this.

    emit(record) must durably retain bounded evidence or raise; cancelled() is
    the outer monotone latch. All17 individual receipts plus final cleanup and
    independent outer settlement are required. Failure/cancel is consumed HOLD.
    """
    global _CALLED
    check(not _CALLED, 'callable already consumed in this namespace; no reentry/retry')
    _CALLED = True
    check(Path(scratch_path) == SCRATCH and str(scratch_path) == str(SCRATCH), 'fixed new scratch required')
    check(type(scratch_fd) is int and isinstance(original_scratch, dict)
          and set(original_scratch) == {'dev', 'ino', 'uid', 'mode'}, 'borrowed original scratch FD/pin required')
    check(type(source_bytes) is bytes and len(source_bytes) == SOURCE_BYTES
          and source_bytes.count(b'\n') == SOURCE_LF and digest(source_bytes) == SOURCE_SHA256,
          'exact current closer source required; no adaptive/old-source extraction')
    check(type(methods_bytes) is bytes and len(methods_bytes) == METHODS_BYTES
          and methods_bytes.count(b'\n') == METHODS_LF and digest(methods_bytes) == METHODS_SHA256,
          'exact actual frozen inventory bytes required; no invented matching digest')
    check(callable(emit) and callable(cancelled), 'durable evidence/monotone cancellation callbacks required')
    started, cancellation_seen = time.monotonic(), False

    def poll():
        nonlocal cancellation_seen
        cancellation_seen = cancellation_seen or bool(cancelled())
        if cancellation_seen:
            raise ControlCancelled('outer cancellation; consumed HOLD, no retry')
        check(time.monotonic() - started <= TOTAL_SECONDS, 'contract-control time cap; consumed HOLD')

    rows, arena, subject, failure = [], None, None, None
    cleanup = {'status': 'HOLD', 'failures': ['fixture ownership not established']}
    try:
        poll()
        code, selected = extract(source_bytes)
        emit({'kind': 'closeout_contract_controls_begin', 'source_sha256': SOURCE_SHA256,
              'methods_sha256': METHODS_SHA256, 'planned_cases': list(CASES), 'selected': selected,
              'scratch': str(SCRATCH), 'original_scratch': original_scratch,
              'synthetic_fixture': MARKER, 'actual_application_test_cases': 0})
        arena = Arena(scratch_fd, original_scratch)
        arena.start()
        subject = Subject(arena, poll)
        subject.start(code)
        source = fixture_files(subject, methods_bytes)
        emit({'kind': 'closeout_contract_fixture_originals', 'fixture_files': [
            {'path': str(SCRATCH / row['name']), 'original_empty_pin': row['original'],
             'immutable_pin': row['pin'], 'sha256': row['sha256']} for row in arena.files],
             'fixture_bytes': arena.bytes_written, 'synthetic_fixture': MARKER,
             'actual_created_files': 8, 'actual_created_runtime_directories': 0})
        for case in CASES:
            row = {'kind': 'closeout_contract_control_result', 'case': case, 'status': 'HOLD',
                   'case_body_started': False, 'effect': None, 'failure': None,
                   'shared_fixture_settlement': 'PENDING_SUITE_FINALLY_NOT_A_COMPLETE_SUITE_PASS'}
            rows.append(row)
            try:
                emit({'kind': 'closeout_contract_control_begin', 'case': case})
                poll()
                row['case_body_started'] = True
                row['effect'] = exercise(subject, source, case)
                poll()
                row['status'] = 'PASS'
            except BaseException as error:
                row['failure'] = type(error).__name__ + ': ' + str(error)[:240]
            emit(row)
            if row['status'] != 'PASS':
                break
        poll()
    except BaseException as error:
        failure = type(error).__name__ + ': ' + str(error)[:240]
    finally:
        try:
            failures = subject.close() if subject is not None else []
        except BaseException as error:
            failures = ['subject settlement exception: ' + type(error).__name__ + ': ' + str(error)[:160]]
        if arena is not None:
            cleanup = arena.cleanup(failures)
    if failure is None:
        try:
            poll()
        except BaseException as error:
            failure = type(error).__name__ + ': ' + str(error)[:240]
    complete = (failure is None and cleanup['status'] == 'COMPLETE'
                and len(rows) == len(CASES) and all(row['status'] == 'PASS' for row in rows))
    terminal = {'kind': 'closeout_contract_controls_terminal', 'status': 'PASS' if complete else 'HOLD',
                'planned_cases': len(CASES), 'attempted_cases': len(rows),
                'case_bodies_started': sum(row['case_body_started'] for row in rows),
                'passed_cases': sum(row['status'] == 'PASS' for row in rows),
                'unstarted_cases': [row['case'] for row in rows if not row['case_body_started']] + list(CASES[len(rows):]),
                'failure': failure, 'cleanup': cleanup, 'source_sha256': SOURCE_SHA256,
                'methods_sha256': METHODS_SHA256, 'actual_application_test_cases': 0,
                'source_io': dict(subject.os.counts) if subject is not None else None,
                'source_ticks': subject.ticks if subject is not None else 0,
                'borrowed_scratch_fd_closed_or_removed': False,
                'outer_settlement_and_original_scratch_removal': 'REQUIRED_SEPARATELY_NOT_PERFORMED_HERE',
                'elapsed_seconds': time.monotonic() - started}
    emit(terminal)
    return terminal


if __name__ == '__main__':
    raise SystemExit('NOT A RUNNER: separate exact-source/instance/launcher admission required; no retry')
