#!/usr/bin/python3
"""SOURCE-ONLY PROPOSAL: one new four-registration component invocation.

Author /root/editor; executor /root; independent launcher reviewer /root/editor_review.
Callable author /root/verification; independent callable reviewer /root/build_config.
No actual packet or execution permission is supplied here. No helper main/import,
subprocess/thread, old runner, application, real OOM, FD reuse or original closeout.
A separately admitted GNU timeout parent owns this one Python child. Its finite
TERM/KILL schedule is not a guarantee of kernel/host settlement or cleanup.
"""

import errno
import fcntl
import hashlib
import json
import math
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
REVIEWS = W / 'docs/audit-continuation/2026-09-08-linux/reviews'
C = REVIEWS / 'linux-closeout'
SELF = C / 'LAUNCH_REGISTRATION_CONTROLS_01.py'
SCRATCH = BASE / 'audit-runtime-linux-closeout-registration-controls-01'
LOCK = BASE / '.audit-coordination-linux-20260908/build.lock'
REQUEST = C / 'CLOSEOUT-REGISTRATION-CONTROLS-REQUEST-01.json'
JOURNAL_PATH = C / 'CLOSEOUT-REGISTRATION-CONTROLS-events-01.jsonl'
RECEIPT = C / 'CLOSEOUT-REGISTRATION-CONTROLS-LAUNCH-RECEIPT-01.json'
APPROVALS = (C / 'CLOSEOUT-REGISTRATION-CONTROLS-ACCEPT-root-01.json',
             C / 'CLOSEOUT-REGISTRATION-CONTROLS-ACCEPT-editor_review-01.json')
INPUTS = {
    'launcher': SELF,
    'launch_plan': C / 'CLOSEOUT-REGISTRATION-CONTROLS-LAUNCH-PLAN.md',
    'controls': REVIEWS / 'verification/CLOSEOUT_REGISTRATION_CONTROLS.py',
    'controls_plan': REVIEWS / 'verification/CLOSEOUT-REGISTRATION-CONTROLS-PLAN.md',
    'closer': W / 'scripts/audit/linux_database_closeout.py',
    'closer_plan': C / 'PLAN.md',
    'outer': C / 'LAUNCH_CLOSEOUT.py',
    'outer_plan': C / 'CLOSEOUT-OUTER-PLAN.md',
    'registration_source_review': REVIEWS / 'baseline-coverage/CLOSEOUT-REGISTRATION-SUCCESSOR-REVIEW.json',
    'outer_source_review': REVIEWS / 'baseline-coverage/CLOSEOUT-OUTER-SOURCE-REVIEW.json',
    'controls_source_review': REVIEWS / 'build-config/CLOSEOUT-REGISTRATION-CONTROLS-SOURCE-REVIEW.json',
    'launcher_source_review': REVIEWS / 'editor-independent/CLOSEOUT-REGISTRATION-CONTROLS-LAUNCHER-SOURCE-REVIEW.json',
    'coordination_journal': REVIEWS / 'verification/CONTROL-BOOTSTRAP.jsonl',
    'coordination_receipt': REVIEWS / 'verification/CONTROL-BOOTSTRAP-RECEIPT.json',
}
FIXED = {
    'controls': '0a012823b44c2c2bf2e7e81e185cecc324639d210529d1886ab6651d5efa0802',
    'controls_plan': '27cb2e2da38f6cface649695179ce472b27b8755171393fe3a803703de5f690f',
    'closer': '8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517',
    'closer_plan': '0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d',
    'outer': '50473240372643cc30dc54f12e4f3cf818d6ab68e0f4f27b62929cdae4684a5c',
    'outer_plan': '1fbde7b3ef97a9cbec7a72124cd50c7c85ee807a51a59822af5a9ab80210c44d',
    'registration_source_review': 'c4b7e9d03dc8e5160c0f6cf628cbd4ee97e7dcb27707d73059c61f17576c6370',
    'outer_source_review': '388905a1e4061164803248e2aa11f6b6047aedba1e11ddc91853d03779401b36',
    'controls_source_review': '03f1e898026be305bc76ff64ac1c2fcc3e292359e2b8413fb30319b308c411dd',
    'coordination_journal': '8ae151484b4ddd74a1aea35f5acdc98748c52edf34b5321f170c08d8e67fb613',
    'coordination_receipt': 'cd0ce6a2d06ea3deaf2556736e633c322d41f07919ef511ad0c1ae2571976fc0',
}
PYTHON, TIMEOUT = Path('/usr/bin/python3.12'), Path('/usr/bin/timeout')
ARGV = ['/usr/bin/python3', '-I', '-B', str(SELF)]
EXTERNAL_ARGV = [str(TIMEOUT), '--foreground', '--signal=TERM', '--kill-after=10s', '50s', *ARGV]
EXTERNAL = {'argv': EXTERNAL_ARGV, 'term_after_seconds': 50, 'kill_after_term_seconds': 10,
            'direct_original_child_only': True, 'no_child_descendants': True,
            'unsettled_or_interrupted_is_consumed_hold': True}
PARENTS = (Path('/'), Path('/root'), Path('/root/projects'), BASE)
CASES = (
    ('R01_closer_nominal_registration', 'closer', False, 'closer-normal'),
    ('R02_closer_second_pin_store_memoryerror', 'closer', True, 'closer-fault'),
    ('R03_outer_nominal_registration', 'outer', False, 'outer-normal'),
    ('R04_outer_second_pin_store_memoryerror', 'outer', True, 'outer-fault'),
)
IDS = [case[0] for case in CASES]
MARKER = 'FOUR_SYNTHETIC_REGISTRATION_COMPONENT_CONTROLS_NOT_CLOSEOUT_OR_APPLICATION_ADMISSION'
SELECTED = {'closer': ('Hold', 'require', 'identity', 'absolute', 'Directories'),
            'outer': ('LIMITS', 'Hold', 'require', 'pin', 'Originals')}
LIMITS = {
    'launcher_seconds': 45, 'emergency_grace_seconds': 10,
    'callable_work_seconds': 30, 'callable_total_seconds': 45, 'external_seconds': 60,
    'address_space_bytes': 256 * 1024 ** 2, 'observed_owned_peak_rss_bytes': 256 * 1024 ** 2,
    'input_bytes': 512 * 1024, 'python_bytes': 32 * 1024 ** 2, 'timeout_bytes': 1024 ** 2,
    'journal_bytes': 56 * 1024, 'external_receipt_and_output_bytes': 8 * 1024,
    'journal_record_bytes': 34 * 1024, 'control_event_bytes': 8 * 1024,
    'control_return_bytes': 32 * 1024, 'control_events': 10,
    'scratch_bytes': 64 * 1024, 'directory_originals': 64,
}
OBLIGATIONS = {
    'RC01': 'ACCEPT',  # Exact four controls/source fidelity, never whole-helper or old-runner admission.
    'RC02': 'ACCEPT',  # Original Linux coordination, new absent scratch/evidence and empty-root cleanup.
    'RC03': 'ACCEPT',  # Genuine accepting exact-source reviews, request/input/parent/interpreter pins.
    'RC04': 'ACCEPT',  # Fresh sole local/CI slot and cooperative producer freeze.
    'RC05': 'ACCEPT',  # Literal case/FD/registry/cleanup records and lossless returned-packet reconciliation.
    'RC06': 'ACCEPT',  # Actual GNU timeout bytes/semantics/direct-child envelope and resource/interrupt limits.
    'RC07': 'ACCEPT',  # External original-child/monitor settlement and independent final outcome adoption.
}
ROLES = ('OWNER_EXECUTION_APPROVAL', 'INDEPENDENT_SOURCE_AND_INSTANCE_REVIEW')
SIGNALS = {signal.SIGHUP, signal.SIGINT, signal.SIGTERM}
OBSERVED_SIGNALS = SIGNALS | {signal.SIGALRM}
DIR_FLAGS = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
CANCEL, ALARMED = set(), False
START = time.monotonic()


def require(condition, reason):
    if not condition:
        raise RuntimeError(reason)


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True,
                      allow_nan=False).encode() + b'\n'


def sha(value):
    return hashlib.sha256(value).hexdigest()


def same(left, right):
    return canonical(left) == canonical(right)  # True is never silently equal to integer1.


def parse(data):
    def pairs(items):
        result = {}
        for key, value in items:
            require(key not in result, 'duplicate JSON key')
            result[key] = value
        return result
    def nonfinite(_value):
        raise RuntimeError('nonfinite JSON token')
    return json.loads(data, object_pairs_hook=pairs, parse_constant=nonfinite)


def pin(value, directory=False):
    keys = ('dev', 'ino', 'uid', 'mode') if directory else (
        'dev', 'ino', 'uid', 'mode', 'nlink', 'size', 'mtime_ns', 'ctime_ns')
    return {key: getattr(value, 'st_' + key) for key in keys}


def describe(error):
    return {'type': type(error).__name__, 'errno': getattr(error, 'errno', None),
            'message': str(error)[:160]}


def cancelled():
    CANCEL.update(signal.sigpending() & OBSERVED_SIGNALS)
    if time.monotonic() - START >= LIMITS['launcher_seconds']:
        CANCEL.add('launcher_monotonic_deadline')
    return bool(CANCEL)


def poll():
    require(not cancelled(), 'latched cancellation/deadline; no replay')


def alarm(_number, _frame):
    global ALARMED
    if ALARMED:
        os._exit(124)  # No possibly blocking output before emergency self-exit.
    ALARMED = True
    CANCEL.add('launcher_alarm')
    signal.setitimer(signal.ITIMER_REAL, LIMITS['emergency_grace_seconds'])


class Originals:
    def __init__(self):
        self.fds, self.pins = {}, {}

    def directory(self, path, discover=False):
        require(path.is_absolute() and '..' not in path.parts, 'absolute fixed directory required')
        key = str(path)
        parent = self.directory(path.parent, discover) if path != Path('/') else None
        if key not in self.fds:
            require(discover and len(self.fds) < LIMITS['directory_originals'], 'unbound/excess directory')
            fd = os.open('/' if parent is None else path.name, DIR_FLAGS, dir_fd=parent)
            try:
                original = pin(os.fstat(fd), True)
                require(stat.S_ISDIR(original['mode']), 'original directory type')
                self.fds[key], self.pins[key] = fd, original
            except BaseException:
                if self.fds.get(key) == fd:
                    self.fds.pop(key)
                os.close(fd)
                raise
        fd, original = self.fds[key], self.pins[key]
        require(same(pin(os.fstat(fd), True), original), 'original directory descriptor drift')
        if parent is not None:
            require(same(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True), original),
                    'original directory name drift')
        return fd

    def read(self, path, discover=False, check_cancel=True):
        if check_cancel:
            poll()
        cap = LIMITS['python_bytes'] if path == PYTHON else (
            LIMITS['timeout_bytes'] if path == TIMEOUT else LIMITS['input_bytes'])
        parent = self.directory(path.parent, discover)
        before = pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
        require(stat.S_ISREG(before['mode']) and before['uid'] == os.getuid() and before['nlink'] == 1
                and not before['mode'] & (0o022 | stat.S_ISUID | stat.S_ISGID)
                and 0 <= before['size'] <= cap, 'bounded owned single-link regular input required')
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        try:
            require(pin(os.fstat(fd)) == before, 'input changed before open')
            pieces, used = [], 0
            while True:
                if check_cancel:
                    poll()
                piece = os.read(fd, min(65536, cap + 1 - used))
                if not piece:
                    break
                used += len(piece)
                require(used <= cap, 'input byte cap')
                pieces.append(piece)
            require(pin(os.fstat(fd)) == before
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
                    and used == before['size'], 'input postread pin/length drift')
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
                errors.append({'operation': 'close_original_directory', 'path': key, 'error': describe(error)})
        return errors


def required_directories():
    result = set()
    for path in (REQUEST, *INPUTS.values(), *APPROVALS, PYTHON, TIMEOUT, LOCK, JOURNAL_PATH, RECEIPT):
        parent = path.parent
        while True:
            result.add(str(parent))
            if parent == Path('/'):
                break
            parent = parent.parent
    return result


def absent(parent, name):
    try:
        os.stat(name, dir_fd=parent, follow_symlinks=False)
    except FileNotFoundError:
        return True
    return False


def empty(fd):
    with os.scandir(fd) as entries:
        return next(entries, None) is None  # Do not inventory/traverse unknown contents.


def read_small(path, cap):
    with open(path, 'rb') as stream:
        data = stream.read(cap + 1)
    require(len(data) <= cap, 'bounded owned-process/resource metadata')
    return data


def process_row(pid):
    fields = read_small('/proc/' + str(pid) + '/stat', 8192).rsplit(b')', 1)[1].split()
    return {'pid': pid, 'ppid': int(fields[1]), 'starttime': int(fields[19]),
            'uid': os.stat('/proc/' + str(pid), follow_symlinks=False).st_uid}


def external_parent(timeout_pin, original=None):
    pid = os.getppid()
    parent, child = process_row(pid), process_row(os.getpid())
    require(parent['uid'] == child['uid'] == os.getuid() and child['ppid'] == pid
            and parent['starttime'] <= child['starttime'], 'direct owned timeout parent required')
    require(os.readlink('/proc/' + str(pid) + '/exe') == str(TIMEOUT)
            and same(pin(os.stat('/proc/' + str(pid) + '/exe')), timeout_pin), 'original timeout image required')
    require(read_small('/proc/' + str(pid) + '/cmdline', 8192)
            == b'\0'.join(value.encode() for value in EXTERNAL_ARGV) + b'\0', 'exact timeout foreground argv')
    require(parent == process_row(pid) and child == process_row(os.getpid()) and os.getppid() == pid,
            'parent/child identity changed during observation')
    observed = {'parent': parent, 'child': child}
    require(original is None or same(observed, original), 'external monitor replaced/reparented; no adoption')
    return observed


def sample(timeout_pin, process_original, launch=False):
    external_parent(timeout_pin, process_original)
    memory = {}
    for line in read_small('/proc/meminfo', 65536).splitlines():
        fields = line.split()
        if fields and fields[0] in (b'MemTotal:', b'MemAvailable:'):
            memory[fields[0].decode().rstrip(':')] = int(fields[1]) * 1024
    parent_peak = None
    for line in read_small('/proc/' + str(process_original['parent']['pid']) + '/status', 65536).splitlines():
        fields = line.split()
        if fields and fields[0] == b'VmHWM:':
            parent_peak = int(fields[1]) * 1024
    require(type(parent_peak) is int, 'timeout RSS observation unavailable')
    external_parent(timeout_pin, process_original)
    disks = {}
    for path in (BASE, C):
        value = os.statvfs(path)
        disks[str(path)] = value.f_bavail * value.f_frsize
    own_peak = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss * 1024
    okay = (memory['MemAvailable'] >= memory['MemTotal'] * (0.25 if launch else 0.20)
            and min(disks.values()) >= (12 if launch else 8) * 1024 ** 3 + (LIMITS['scratch_bytes'] if launch else 0)
            and own_peak + parent_peak <= LIMITS['observed_owned_peak_rss_bytes'])
    return {'unix_ns': time.time_ns(), 'launch_floor': launch, 'disks_free_bytes': disks,
            'memory_bytes': memory, 'self_peak_rss_bytes': own_peak,
            'timeout_peak_rss_bytes': parent_peak, 'within_floors': okay}


class Journal:
    def __init__(self, originals, timeout_pin, process_original):
        self.d, self.timeout_pin, self.process_original = originals, timeout_pin, process_original
        self.fd, self.original, self.last = None, None, None
        self.used, self.sequence, self.failed, self.last_sample = 0, 0, False, 0.0

    def create(self):
        parent = self.d.directory(C)
        self.fd = os.open(JOURNAL_PATH.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL
                          | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=parent)
        self.original = pin(os.fstat(self.fd))
        require(self.original['uid'] == os.getuid() and self.original['mode'] == stat.S_IFREG | 0o600
                and self.original['nlink'] == 1 and self.original['size'] == 0, 'exclusive private journal')
        os.fsync(self.fd)
        os.fsync(parent)
        require(self.original == pin(os.fstat(self.fd))
                == pin(os.stat(JOURNAL_PATH.name, dir_fd=parent, follow_symlinks=False)), 'new journal drift')
        self.last = dict(self.original)

    def emit(self, event):
        require(self.fd is not None and self.last is not None and not self.failed, 'journal incomplete; no retry')
        try:
            parent = self.d.directory(C)
            require(pin(os.fstat(self.fd)) == self.last
                    == pin(os.stat(JOURNAL_PATH.name, dir_fd=parent, follow_symlinks=False))
                    and self.last['size'] == self.used, 'prior completed journal pin/length drift')
            require(not {'sequence', 'unix_ns', 'resource_observation'} & set(event), 'reserved outer fields')
            row = {'sequence': self.sequence, 'unix_ns': time.time_ns(), **event}
            if event.get('kind') != 'launcher_preterminal' and time.monotonic() - self.last_sample >= 5:
                row['resource_observation'] = sample(self.timeout_pin, self.process_original)
                self.last_sample = time.monotonic()
                if not row['resource_observation']['within_floors']:
                    CANCEL.add('resource_floor')
            data = canonical(row)
            require(len(data) <= LIMITS['journal_record_bytes']
                    and self.used + len(data) <= LIMITS['journal_bytes'], 'aggregate permanent evidence cap')
            view = memoryview(data)
            while view:
                count = os.write(self.fd, view)
                require(count > 0, 'zero evidence write')
                view = view[count:]
            os.fsync(self.fd)
            after = pin(os.fstat(self.fd))
            require(after == pin(os.stat(JOURNAL_PATH.name, dir_fd=parent, follow_symlinks=False))
                    and all(after[key] == self.original[key] for key in ('dev', 'ino', 'uid', 'mode', 'nlink'))
                    and after['size'] == self.used + len(data), 'journal postwrite original/length drift')
            self.last, self.used, self.sequence = after, after['size'], self.sequence + 1
        except BaseException:
            self.failed = True
            raise


class ControlEvidence:
    def __init__(self, journal, original_directories, borrowed_fds):
        self.journal, self.originals, self.borrowed = journal, original_directories, borrowed_fds
        self.events, self.rows, self.started, self.result_sequences = [], [], [], []
        self.begun, self.pending, self.cleanup, self.cleanup_sequence = False, None, None, None
        self.failed, self.returned, self.packet = False, False, None

    def emit(self, event):
        require(not self.failed and type(event) is dict, 'emitter failed/non-object')
        try:
            data = canonical(event)
            require(len(data) <= LIMITS['control_event_bytes'] and len(self.events) < LIMITS['control_events'],
                    'control event count/byte cap')
            row = parse(data)
            self.journal.emit({'kind': 'control_event', 'record': row})
            self.events.append(row)  # Durable invalid emissions remain separate from validated protocol state.
            self.validate(row)
        except BaseException:
            self.failed = True
            raise

    def validate_pass(self, row, case):
        case_id, kind, inject, name = case
        target = str(SCRATCH / name)
        paths = [str(path) for path in (*PARENTS, SCRATCH)] + [target]
        lifetimes = row['fd_lifetimes']
        require(type(lifetimes) is list and len(lifetimes) == 6
                and [value['path'] for value in lifetimes] == paths, 'exact six source FD lifetimes')
        fds = {value['path']: value['fd'] for value in lifetimes}
        require(all(type(fd) is int and fd >= 0 and fd not in self.borrowed for fd in fds.values())
                and len(set(fds.values())) == 6, 'distinct original subject FDs, never borrowed authority')
        pins = {value['path']: value['pin'] for value in lifetimes}
        require(same({key: pins[key] for key in self.originals}, self.originals)
                and set(pins[target]) == {'dev', 'ino', 'uid', 'mode'}
                and all(type(value) is int for value in pins[target].values())
                and pins[target]['uid'] == os.getuid() and pins[target]['mode'] == stat.S_IFDIR | 0o700,
                'original parent/fixture directory pins')
        for value in lifetimes:
            require(value['open_returned'] is True and type(value['close_calls']) is int
                    and type(value['real_close_attempts']) is int
                    and value['close_calls'] == value['real_close_attempts'] == 1
                    and value['close_returned'] is True and value['closed_ebadf'] is True
                    and value['close_error'] is None
                    and value['close_origin'] == ('subject_open' if inject and value['path'] == target
                                                   else 'subject_final_close'), 'actual once-close and EBADF proof')
        parents = {'fds': {key: fds[key] for key in self.originals}, 'pins': self.originals}
        registered = parents if inject else {'fds': fds, 'pins': pins}
        after_close = {'fds': registered['fds'] if kind == 'closer' else {}, 'pins': registered['pins']}
        require(same(row['parent_registry_before'], parents) and same(row['registry_after_target'], registered)
                and same(row['registry_after_close'], after_close), 'source-specific registry contracts')
        require(row['id'] == case_id and row['kind'] == kind and row['injected'] is inject
                and row['specific_second_store_observed'] is inject
                and row['parent_authority_preserved'] is True and row['registry_contract_verified'] is True
                and row['subject_close_called_once'] is True and row['errors'] == [], 'complete case PASS fields')

    def validate(self, row):
        require(type(row['sequence']) is int and row['sequence'] == len(self.events)
                and row['marker'] == MARKER and self.cleanup is None, 'ordered marked control sequence')
        event = row['event']
        common = {'sequence', 'event', 'marker'}
        if event == 'begin':
            require(set(row) == common | {'cases', 'closer_sha256', 'outer_sha256'}
                    and not self.begun and len(self.events) == 1 and same(row['cases'], IDS)
                    and row['closer_sha256'] == FIXED['closer'] and row['outer_sha256'] == FIXED['outer'],
                    'exact four-control begin')
            self.begun = True
        elif event == 'case_start':
            require(set(row) == common | {'id'} and self.begun and self.pending is None
                    and len(self.started) == len(self.rows) < 4
                    and (not self.rows or self.rows[-1]['status'] == 'PASS')
                    and row['id'] == IDS[len(self.started)], 'ordered once-only case start')
            self.started.append(row['id'])
            self.pending = row['id']
        elif event == 'case_result':
            result = row['result']
            require(set(row) == common | {'result'} and type(result) is dict and self.pending is not None
                    and result['id'] == self.pending and result['status'] in ('PASS', 'FAIL')
                    and type(result['errors']) is list, 'corresponding literal case result')
            if result['status'] == 'PASS':
                self.validate_pass(result, CASES[len(self.rows)])
            self.rows.append(result)
            self.result_sequences.append(row['sequence'])
            self.pending = None
        elif event == 'cleanup':
            require(set(row) == common | {'result'} and type(row['result']) is dict
                    and row['result']['status'] in ('COMPLETE', 'HOLD')
                    and row['result']['borrowed_scratch_closed_or_removed'] is False,
                    'literal cleanup; may follow a failure before begin/result')
            self.cleanup, self.cleanup_sequence = row['result'], row['sequence']
        else:
            raise RuntimeError('unexpected control event')

    def accept_return(self, value):
        data = canonical(value)
        require(len(data) <= LIMITS['control_return_bytes'], 'returned packet cap')
        row = parse(data)
        keys = {'marker', 'expected', 'started', 'results', 'unstarted', 'started_without_result', 'failures',
                'emitter_failure', 'completed_protocol_events', 'cleanup', 'footprint_before_cases',
                'source_sha256', 'elapsed_seconds', 'root_scratch_closeout_required', 'application_cases', 'status'}
        matched = (type(row) is dict and set(row) == keys and same(row['results'], self.rows)
                   and self.cleanup is not None and same(row['cleanup'], self.cleanup))
        if not matched:
            self.failed = True
            self.journal.emit({'kind': 'invalid_control_return', 'packet': row, 'sha256': sha(data), 'bytes': len(data)})
            raise RuntimeError('return shape or emitted-result/cleanup disagreement')
        # Lossless, not summary-only: replace two proven-identical values with explicit
        # event references. Reinsert those literal events to reconstruct these exact bytes.
        remainder = {key: item for key, item in row.items() if key not in ('results', 'cleanup')}
        self.journal.emit({'kind': 'control_return_lossless', 'fields': remainder,
                          'result_control_sequences': self.result_sequences,
                          'cleanup_control_sequence': self.cleanup_sequence,
                          'results_sha256': sha(canonical(row['results'])),
                          'cleanup_sha256': sha(canonical(row['cleanup'])),
                          'canonical_return_sha256': sha(data), 'canonical_return_bytes': len(data)})
        require(not self.failed and row['marker'] == MARKER and same(row['expected'], IDS)
                and same(row['started'], self.started)
                and same(row['unstarted'], [case_id for case_id in IDS if case_id not in self.started])
                and same(row['started_without_result'], [case_id for case_id in self.started
                                                        if case_id not in {result['id'] for result in self.rows}])
                and type(row['completed_protocol_events']) is int
                and row['completed_protocol_events'] == len(self.events)
                and same(row['source_sha256'], {'closer': FIXED['closer'], 'outer': FIXED['outer']})
                and row['root_scratch_closeout_required'] is True and same(row['application_cases'], 0)
                and type(row['failures']) is list and row['status'] in (
                    'HOLD', 'FOUR_COMPONENT_CONTROLS_PASS_PENDING_ROOT_SETTLEMENT')
                and type(row['elapsed_seconds']) in (int, float) and math.isfinite(row['elapsed_seconds'])
                and row['elapsed_seconds'] >= 0, 'literal returned accounting/status')
        require(all(type(error) is dict and set(error) == {'type', 'errno', 'message'}
                    and type(error['type']) is str and type(error['message']) is str
                    and (error['errno'] is None or type(error['errno']) is int)
                    for error in row['failures']), 'returned failure descriptions')
        emitter = row['emitter_failure']
        require(emitter is None or (type(emitter) is dict and set(emitter) == {'type', 'errno', 'message'}
                and type(emitter['type']) is str and type(emitter['message']) is str
                and (emitter['errno'] is None or type(emitter['errno']) is int)), 'returned emitter-failure shape')
        footprint = row['footprint_before_cases']
        require(footprint is None or (type(footprint) is dict and set(footprint) == {'logical_bytes', 'allocated_bytes'}
                and all(type(item) is int and 0 <= item <= LIMITS['scratch_bytes'] for item in footprint.values())),
                'returned optional footprint shape')
        self.returned, self.packet = True, row

    def complete(self):
        if self.failed or not self.returned or self.packet['status'] != 'FOUR_COMPONENT_CONTROLS_PASS_PENDING_ROOT_SETTLEMENT':
            return False
        row, cleanup = self.packet, self.cleanup
        require(len(self.events) == 10 and self.started == IDS and len(self.rows) == 4
                and all(result['status'] == 'PASS' for result in self.rows)
                and not row['unstarted'] and not row['started_without_result'] and not row['failures']
                and row['emitter_failure'] is None and row['elapsed_seconds'] <= LIMITS['callable_total_seconds'],
                'four complete real component cases required')
        footprint = row['footprint_before_cases']
        require(type(footprint) is dict and set(footprint) == {'logical_bytes', 'allocated_bytes'}
                and all(type(value) is int and 0 <= value <= LIMITS['scratch_bytes'] for value in footprint.values()),
                'actual bounded scratch footprint required')
        require(cleanup['status'] == 'COMPLETE' and cleanup['failures'] == [] and cleanup['remaining_names'] == []
                and same(cleanup['observer_close_attempts'], 8)
                and same([item['name'] for item in cleanup['removed']], [case[3] for case in reversed(CASES)]),
                'four removed original empty fixtures/eight observer closes required')
        originals = {case[3]: result['fd_lifetimes'][-1]['pin'] for case, result in zip(CASES, self.rows)}
        require(all(same(item['original'], originals[item['name']]) for item in cleanup['removed']),
                'removed fixture pins must match exercised originals')
        observers = cleanup['observer_lifetimes']
        require(type(observers) is list and len(observers) == 8
                and [item['path'] for item in observers] == [str(path) for path in PARENTS]
                    + [str(SCRATCH / case[3]) for case in CASES]
                and all(type(item['fd']) is int and item['fd'] >= 0 and item['fd'] not in self.borrowed
                        and item['close_attempted'] is True and item['close_returned'] is True
                        and item['closed_ebadf'] is True for item in observers)
                and len({item['fd'] for item in observers}) == 8, 'original observer once-close/EBADF lifetimes')
        require(not {item['fd'] for item in observers} & {
            item['fd'] for result in self.rows for item in result['fd_lifetimes']}, 'subject/held-observer FD separation')
        return True


def verify_lock(d, lock_fd, request):
    require(same(pin(os.fstat(lock_fd)), request['lock_pin'])
            and same(pin(os.stat(LOCK.name, dir_fd=d.directory(LOCK.parent), follow_symlinks=False)),
                     request['lock_pin']), 'original coordination lock drift')


def recheck_inputs(d, images, identities, check_cancel):
    for name, data in images.items():
        observed, original = d.read(Path(name), check_cancel=check_cancel)
        require(observed == data and same(original, identities[name]), 'captured input/interpreter/tool drift')


def verify_coordination(request, images):
    rows = [parse(line) for line in images[str(INPUTS['coordination_journal'])].splitlines()]
    receipt = parse(images[str(INPUTS['coordination_receipt'])])
    require(len(rows) == 6 and same([row['sequence'] for row in rows], list(range(6)))
            and rows[-1]['kind'] == 'bootstrap_complete'
            and receipt['format'] == 'passvault-linux-control-bootstrap-receipt-v1'
            and receipt['author'] == '/root' and same(receipt['observed_exit'], 0)
            and receipt['journal_sha256'] == FIXED['coordination_journal']
            and same(receipt['journal_bytes'], len(images[str(INPUTS['coordination_journal'])])),
            'completed original Linux coordination evidence required')
    require(same(request['lock_pin'], receipt['original_lock'])
            and same(request['lock_pin'], rows[4]['identity'])
            and same(request['lock_pin'], rows[-1]['lock_pin'])
            and same(request['directory_pins'][str(LOCK.parent)], receipt['original_directory'])
            and same(receipt['original_directory'], rows[2]['identity'])
            and all(same(request['directory_pins'][key], value)
                    for key, value in rows[-1]['directory_pins'].items()), 'no lock/parent recreation or adoption')


def main():
    # Ownership registries and finally exist before opening inputs or making effects.
    d, lock_fd, scratch_fd, journal = Originals(), None, None, None
    request, process_original, observed, report, scratch_original = None, None, None, None, None
    images, identities, bindings, errors, close_errors = {}, {}, {}, [], []
    allocation_attempted, scratch_admitted, root_removed, root_fsync, scratch_closed = False, False, False, False, False
    cleanup, code = 'NOT_ALLOCATED', 1
    try:
        require(sys.platform == 'linux' and sys.argv == [str(SELF)] and Path(__file__) == SELF
                and sys.flags.isolated and sys.dont_write_bytecode and not sys.flags.optimize
                and Path(os.path.realpath(sys.executable)) == PYTHON, 'fixed Linux Python3.12 -I -B entry')
        os.umask(0o077)
        require(not signal.pthread_sigmask(signal.SIG_BLOCK, set()) & OBSERVED_SIGNALS,
                'managed signals initially unblocked')
        for number in SIGNALS:
            signal.signal(number, lambda signum, _frame: CANCEL.add(signum))
        signal.signal(signal.SIGALRM, alarm)
        signal.setitimer(signal.ITIMER_REAL, max(0.001, LIMITS['launcher_seconds'] - (time.monotonic() - START)))
        resource.setrlimit(resource.RLIMIT_AS, (LIMITS['address_space_bytes'], LIMITS['address_space_bytes']))
        resource.setrlimit(resource.RLIMIT_FSIZE, (LIMITS['journal_bytes'], LIMITS['journal_bytes']))
        for path in (REQUEST, *INPUTS.values(), *APPROVALS, PYTHON, TIMEOUT):
            images[str(path)], identities[str(path)] = d.read(path, True)
        request = parse(images[str(REQUEST)])
        bindings = {key: sha(images[str(path)]) for key, path in INPUTS.items()}
        require(request['format'] == 'passvault-linux-four-registration-controls-request-v1'
                and request['author'] == '/root' and request['run_id'] == 'linux-closeout-registration-controls-01'
                and request['scratch'] == str(SCRATCH) and request['lock'] == str(LOCK)
                and request['journal'] == str(JOURNAL_PATH) and request['external_receipt'] == str(RECEIPT),
                'exact new request/namespace')
        require(request['no_other_audit_local_or_ci_job'] is True and request['cooperative_producers_frozen'] is True
                and same(request['limits'], LIMITS) and same(request['external_supervision'], EXTERNAL)
                and same(request['bindings'], bindings) and all(bindings[key] == value for key, value in FIXED.items()),
                'fresh sole slot/freeze/exact bounds/source bindings')
        require(same(request['input_pins'], {str(path): identities[str(path)] for path in INPUTS.values()}),
                'exact source/review original pins')
        for key, path in (('python', PYTHON), ('timeout', TIMEOUT)):
            require(same(request[key], {'path': str(path), 'sha256': sha(images[str(path)]),
                                        'identity': identities[str(path)]}), 'fresh actual interpreter/monitor identity')
        created, expires = request['created_unix_seconds'], request['expires_unix_seconds']
        require(type(created) is int and type(expires) is int and 0 < expires - created <= 3600
                and created <= time.time() <= expires, 'fresh integer request interval')
        dirs = required_directories()
        require(type(request['directory_pins']) is dict and set(request['directory_pins']) == dirs,
                'exact required original directory set')
        for path in sorted(dirs, key=lambda value: (len(Path(value).parts), value)):
            poll()
            d.directory(Path(path), True)
        require(same(d.pins, request['directory_pins']), 'original directory pins changed')
        verify_coordination(request, images)
        for path, reviewer, role in zip(APPROVALS, ('/root', '/root/editor_review'), ROLES):
            approval = parse(images[str(path)])
            require(approval['format'] == 'passvault-linux-four-registration-controls-acceptance-v1'
                    and approval['reviewer'] == reviewer and approval['review_role'] == role
                    and approval['launcher_author'] == '/root/editor' and approval['controls_author'] == '/root/verification'
                    and approval['controls_source_reviewer'] == '/root/build_config'
                    and approval['subjects_author'] == '/root/editor'
                    and approval['subjects_source_reviewer'] == '/root/baseline_coverage'
                    and approval['purpose'] == 'ONE_NEW_EXACT_FOUR_REGISTRATION_COMPONENT_CONTROL_EXECUTION'
                    and approval['disposition'] == 'ACCEPT' and same(approval['obligations'], OBLIGATIONS)
                    and approval['request_sha256'] == sha(images[str(REQUEST)])
                    and same(approval['bindings'], bindings), 'genuinely authored source-and-instance acceptance required')
        parent = d.directory(LOCK.parent)
        require(same(pin(os.stat(LOCK.name, dir_fd=parent, follow_symlinks=False)), request['lock_pin'])
                and request['lock_pin']['mode'] == stat.S_IFREG | 0o600
                and request['lock_pin']['uid'] == os.getuid() and request['lock_pin']['nlink'] == 1,
                'original private lock required')
        lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        require(same(pin(os.fstat(lock_fd)), request['lock_pin']), 'opened original lock mismatch')
        fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        recheck_inputs(d, images, identities, True)
        verify_lock(d, lock_fd, request)
        require(created <= time.time() <= expires, 'request expired under lock')
        process_original = external_parent(identities[str(TIMEOUT)])
        resources = sample(identities[str(TIMEOUT)], process_original, True)
        require(resources['within_floors'], 'launch disk/RAM/owned-RSS floors')
        require(absent(d.directory(BASE), SCRATCH.name) and absent(d.directory(C), JOURNAL_PATH.name)
                and absent(d.directory(C), RECEIPT.name), 'occupied attempt; no adoption/replay')
        poll()
        journal = Journal(d, identities[str(TIMEOUT)], process_original)
        journal.create()  # First exclusive evidence creation consumes this attempt.
        journal.emit({'kind': 'launcher_admission', 'bindings': bindings,
                      'request_sha256': sha(images[str(REQUEST)]),
                      'approval_sha256': [sha(images[str(path)]) for path in APPROVALS],
                      'directory_pins': d.pins, 'lock_pin': request['lock_pin'], 'process_original': process_original,
                      'external_supervision': EXTERNAL, 'resources': resources, 'limits': LIMITS,
                      'application_cases': 0, 'python_subprocesses_or_threads_admitted': 0})
        journal.emit({'kind': 'scratch_allocation_intent', 'path': str(SCRATCH), 'parent_pin': d.pins[str(BASE)]})
        poll()
        verify_lock(d, lock_fd, request)
        allocation_attempted, cleanup = True, 'HOLD'
        os.mkdir(SCRATCH.name, 0o700, dir_fd=d.directory(BASE))
        scratch_fd = os.open(SCRATCH.name, DIR_FLAGS, dir_fd=d.directory(BASE))
        scratch_original = pin(os.fstat(scratch_fd), True)
        require(scratch_original['uid'] == os.getuid() and scratch_original['mode'] == stat.S_IFDIR | 0o700
                and scratch_original == pin(os.stat(SCRATCH.name, dir_fd=d.directory(BASE), follow_symlinks=False), True)
                and empty(scratch_fd), 'new empty owned scratch original')
        require(max(os.fstat(scratch_fd).st_size, os.fstat(scratch_fd).st_blocks * 512) <= LIMITS['scratch_bytes'],
                'scratch initial footprint cap')
        os.fsync(d.directory(BASE))
        journal.emit({'kind': 'scratch_allocation_original_and_cleanup_authority', 'path': str(SCRATCH),
                      'identity': scratch_original, 'cleanup': 'ONE_ORIGINAL_EMPTY_ROOT_RMDIR_AFTER_CALL_OR_FAILURE',
                      'unknown_entries_or_drift': 'RETAIN_HOLD', 'recursive_removal': False})
        scratch_admitted = True
        originals = {str(path): dict(d.pins[str(path)]) for path in PARENTS}
        originals[str(SCRATCH)] = dict(scratch_original)
        observed = ControlEvidence(journal, originals, set(d.fds.values()) | {scratch_fd, lock_fd, journal.fd})
        poll()
        module = types.ModuleType('passvault_new_four_registration_controls_admitted')
        module.__file__ = str(INPUTS['controls'])
        exec(compile(images[str(INPUTS['controls'])], str(INPUTS['controls']), 'exec'), module.__dict__)
        require(module.CASES == CASES and module.SELECTED == SELECTED and module.SCRATCH == SCRATCH
                and module.PARENTS == PARENTS and module.MARKER == MARKER and module.CONSUMED is False
                and module.CLOSER == (FIXED['closer'], 59609, 1100)
                and module.OUTER == (FIXED['outer'], 48017, 867)
                and module.WORK_SECONDS == LIMITS['callable_work_seconds']
                and module.TOTAL_SECONDS == LIMITS['callable_total_seconds']
                and module.EVENT_BYTES_CAP == LIMITS['control_event_bytes']
                and module.PACKET_BYTES_CAP == LIMITS['control_return_bytes']
                and module.SCRATCH_BYTES_CAP == LIMITS['scratch_bytes'], 'exact frozen callable interface/bounds')
        poll()
        report = module.run_controls(scratch_fd=scratch_fd, original_directories=originals,
                                     closer_source=images[str(INPUTS['closer'])],
                                     outer_source=images[str(INPUTS['outer'])], emit=observed.emit, cancelled=cancelled)
        observed.accept_return(report)
    except BaseException as error:
        errors.append({'operation': 'main', 'error': describe(error)})
    finally:
        # An ordinary case/emitter failure does not suppress separately safe root
        # cleanup. Durable original/allocation-and-cleanup authority must preexist.
        if scratch_admitted:
            try:
                verify_lock(d, lock_fd, request)
                parent = d.directory(BASE)
                require(pin(os.fstat(scratch_fd), True) == scratch_original
                        == pin(os.stat(SCRATCH.name, dir_fd=parent, follow_symlinks=False), True)
                        and empty(scratch_fd), 'original empty scratch guard; never recurse/adopt')
                os.rmdir(SCRATCH.name, dir_fd=parent)
                root_removed = True
                require(pin(os.fstat(scratch_fd), True) == scratch_original and os.fstat(scratch_fd).st_nlink == 0
                        and absent(parent, SCRATCH.name), 'original root removal not proved')
                os.fsync(parent)
                root_fsync = True
                cleanup = 'ORIGINAL_EMPTY_ROOT_REMOVED'
            except BaseException as error:
                errors.append({'operation': 'root_cleanup', 'error': describe(error)})
                cleanup = 'HOLD'
        if scratch_fd is not None:
            fd, scratch_fd = scratch_fd, None
            try:
                os.close(fd)
                try:
                    os.fstat(fd)
                except OSError as error:
                    require(error.errno == errno.EBADF, 'closed root FD observation failed')
                    scratch_closed = True
                else:
                    raise RuntimeError('root FD remained open after close')
            except BaseException as error:
                close_errors.append({'operation': 'close_original_scratch', 'error': describe(error)})
        if journal is not None and journal.last is not None and not journal.failed:
            try:
                recheck_inputs(d, images, identities, False)
                verify_lock(d, lock_fd, request)
                final_resources = sample(identities[str(TIMEOUT)], process_original)
                signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)  # ALRM remains deliverable through last close.
                cancelled()
                okay = (observed is not None and observed.complete() and not errors and not close_errors
                        and not CANCEL and final_resources['within_floors'] and root_removed and root_fsync
                        and scratch_closed and cleanup == 'ORIGINAL_EMPTY_ROOT_REMOVED')
                code = 0 if okay else 1
                journal.emit({'kind': 'launcher_preterminal', 'intended_exit': code, 'errors': errors,
                              'descriptor_errors_so_far': close_errors, 'cleanup': cleanup,
                              'allocation_attempted': allocation_attempted, 'root_rmdir_returned': root_removed,
                              'root_parent_fsync_completed': root_fsync, 'root_fd_close_and_ebadf': scratch_closed,
                              'cancelled': sorted(map(str, CANCEL)), 'alarm_observed': ALARMED,
                              'durable_control_events': len(observed.events) if observed else 0,
                              'validated_case_results': len(observed.rows) if observed else 0,
                              'returned_packet_reconciled': observed.returned if observed else False,
                              'resources': final_resources, 'source_inputs_unchanged': True,
                              'process_original': process_original, 'application_cases': 0,
                              'wrapper_stop': 'NOT_APPLICABLE_NO_WRAPPER_LAUNCH',
                              'scope': 'PRE_FINAL_DESCRIPTOR_CLOSES_REQUIRE_ACTUAL_PYTHON_AND_TIMEOUT_EXIT_AND_REVIEW'})
            except BaseException as error:
                errors.append({'operation': 'preterminal', 'error': describe(error)})
                code = 1
        if journal is not None and journal.fd is not None:
            fd, journal.fd = journal.fd, None
            try:
                os.close(fd)
            except BaseException as error:
                close_errors.append({'operation': 'close_journal', 'error': describe(error)})
        close_errors.extend(d.close())
        if lock_fd is not None:
            fd, lock_fd = lock_fd, None
            try:
                os.close(fd)
            except BaseException as error:
                close_errors.append({'operation': 'close_original_lock', 'error': describe(error)})
        if errors or close_errors or cancelled():
            code = 1
        if code:
            # No diagnostic precedes cleanup; still no hard I/O deadline. External
            # monitor/emergency self-exit may leave this final message incomplete.
            try:
                os.write(2, b'FOUR_REGISTRATION_CONTROLS_HOLD: inspect retained evidence; no automatic retry\n')
            except BaseException:
                pass
    os._exit(code)


if __name__ == '__main__':
    main()
