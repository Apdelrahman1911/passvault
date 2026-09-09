#!/usr/bin/python3
"""One separately admitted direct runner; source proposal, not execution authority.

Never import a runner or launch an alternative/retry. Only the original unreaped
Popen child can receive a signal. Its wrapper stop/descendants remain its duties.
Four exclusive evidence files stay; root authors LAUNCHER.json after tool exit.
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
C = W / 'docs/audit-continuation/2026-09-08-linux/reviews/linux-runner'
SELF, PLAN = C / 'LAUNCH_DATABASE.py', C / 'LAUNCH-PLAN.md'
RUNNER, RUNNER_PLAN = W / 'scripts/audit/linux_database_validation.py', C / 'PLAN.md'
REQUEST, SOURCE = C / 'REQUEST.json', C / 'SOURCE.json'
METHODS = W / 'docs/audit-continuation/2026-09-08-linux/reviews/verification/METHOD-INVENTORY.json'
APPROVALS = (C / 'ACCEPT-root.json', C / 'ACCEPT-verification.json')
PYTHON = Path('/usr/bin/python3.12')
LOCK = BASE / '.audit-coordination-linux-20260908/build.lock'
R = BASE / 'audit-runtime-linux-db-01'
E = W / 'docs/audit-continuation/2026-09-08-linux/runs/linux-database-01'
OUTPUTS = tuple(C / name for name in (
    'LAUNCHER-stdout.log', 'LAUNCHER-stderr.log',
    'LAUNCHER-resources.json', 'LAUNCHER-processes.json',
))
COMMIT = '9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed'
TREE = '05014e9f635131d5db06701e4013b4b5a746465a'
RUNNER_SHA = '346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279'
RUNNER_PLAN_SHA = '74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f'
METHODS_SHA = '40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979'
ARGV = ['/usr/bin/python3', '-I', '-B', str(RUNNER)]
ENTRY = ['/usr/bin/python3', '-I', '-B', str(SELF)]
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
SIGNALS = {signal.SIGINT, signal.SIGTERM, signal.SIGHUP}
PURPOSE = 'ONE_ORIGINAL_LINUX_DATABASE_OUTER_SUPERVISOR'
ACK = [
    'ONE_ORIGINAL_DIRECT_CHILD_NO_DESCENDANT_AUTHORITY',
    'RUNNER_OWNS_ORIGINAL_STOP_NO_PARENT_STOP_OR_RETRY',
    'COOPERATIVE_SLOT_AND_SOURCE_FREEZE_NOT_HOST_SANDBOX',
    'POINT_SAMPLED_RESOURCES_AND_SOFT_SYSCALL_DEADLINES',
    'FORCED_INTERRUPTED_OR_UNSETTLED_EXIT_IS_HOLD',
    'ROOT_TOOL_EXIT_AND_SEPARATE_CLOSEOUT_REQUIRED',
]
LIMITS = {
    'bootstrap_seconds': 120, 'outer_soft_seconds': 6000,
    'cleanup_grace_seconds': 900, 'kill_wait_seconds': 60,
    'pipe_after_exit_seconds': 30, 'postcheck_seconds': 120,
    'sample_seconds': 5, 'file_bytes': 1024 * 1024,
    'input_bytes': 4 * 1024 * 1024, 'python_bytes': 32 * 1024 * 1024,
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
    """Only a fixed RUN leaf; failed writes are retained, never repaired/adopted."""
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
        self.append(b'{"format":"passvault-linux-database-outer-evidence-v1","records":[\n')

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
    for path in (REQUEST, SELF, PLAN, RUNNER, RUNNER_PLAN, SOURCE, METHODS) + APPROVALS:
        captures[path] = d.read(path, True)
    request = parse(captures[REQUEST][0])
    require(request['format'] == 'passvault-linux-database-request-v1' and request['author'] == '/root',
            'actual original runner request')
    require(request['run_id'] == 'linux-database-01' and request['commit'] == COMMIT and request['tree'] == TREE,
            'fixed run/source identity')
    require(request['repository'] == str(BASE / 'passvault') and request['runtime'] == str(R)
            and request['evidence'] == str(E) and request['lock'] == str(LOCK), 'fixed original paths')
    require(request['no_other_audit_local_or_ci_job'] is True
            and request['runtime_closeout'] == 'RETAIN_ALL_REVIEW_IMMEDIATE_ROOT_CLOSEOUT', 'root sole-slot attestation')
    require(request['created_unix_seconds'] <= time.time() <= request['expires_unix_seconds']
            and 0 < request['expires_unix_seconds'] - request['created_unix_seconds'] <= 3600, 'fresh original admission')
    hashes = {path: sha(value[0]) for path, value in captures.items()}
    require(hashes[RUNNER] == RUNNER_SHA and hashes[RUNNER_PLAN] == RUNNER_PLAN_SHA
            and hashes[METHODS] == METHODS_SHA, 'frozen runner/plan/methods')
    bindings = {'runner_sha256': hashes[RUNNER], 'plan_sha256': hashes[RUNNER_PLAN],
                'request_sha256': hashes[REQUEST], 'source_sha256': hashes[SOURCE],
                'methods_sha256': hashes[METHODS], 'command_sha256': sha(canonical(request['command'])),
                'stop_command_sha256': sha(canonical(request['stop_command'])),
                'environment_sha256': sha(canonical(request['environment']))}
    require(request['bindings'] == {key: value for key, value in bindings.items() if key != 'request_sha256'},
            'original eight runner binding relationship')
    require(request['input_pins'] == {str(path): captures[path][1] for path in (RUNNER, RUNNER_PLAN, SOURCE, METHODS)},
            'original immutable runner input pins')
    source = parse(captures[SOURCE][0])
    require(source['format'] == 'passvault-linux-checkout-source-v1' and source['commit'] == COMMIT
            and source['tree'] == TREE and len(source['files']) == 1198, 'actual source-manifest identity')
    require(request['outer_launcher'] == {
        'format': 'passvault-linux-database-outer-v1', 'author': '/root', 'purpose': PURPOSE,
        'launcher_sha256': hashes[SELF], 'plan_sha256': hashes[PLAN],
        'input_pins': {str(path): captures[path][1] for path in (SELF, PLAN)},
        'argv': ENTRY, 'runner_argv': ARGV, 'child_environment': ENV, 'limits': LIMITS,
        'evidence_paths': list(map(str, OUTPUTS)), 'slot_owner': '/root',
        'no_other_audit_local_or_ci_job': True, 'source_and_inputs_frozen': True, 'limits_acknowledged': ACK,
    }, 'exact outer-launcher instance approval')
    for path, reviewer, inner_role, outer_role in zip(
            APPROVALS, ('/root', '/root/verification'),
            ('OWNER_COAUTHOR_APPROVAL', 'INDEPENDENT_SOURCE_REVIEW'),
            ('OWNER_EXECUTION_APPROVAL', 'INDEPENDENT_SOURCE_REVIEW')):
        accept = parse(captures[path][0])
        require(accept['format'] == 'passvault-linux-database-acceptance-v1' and accept['reviewer'] == reviewer
                and accept['runner_authors'] == ['/root/storage', '/root'] and accept['review_role'] == inner_role,
                'actual original runner approval authorship/role')
        require(accept['purpose'] == 'ONE_SHOT_LINUX_DATABASE_EXECUTION_AND_RETAINED_CLOSEOUT_CONTRACT'
                and accept['disposition'] == 'ACCEPT' and accept['bindings'] == bindings
                and accept['obligations'] == {f'F{i:02}': 'ACCEPT' for i in range(1, 8)}, 'actual runner acceptance')
        require(accept['commit'] == COMMIT and accept['tree'] == TREE and accept['runtime'] == str(R)
                and accept['evidence'] == str(E), 'accepted original instance')
        require(accept['outer_launcher'] == {
            'purpose': PURPOSE, 'launcher_authors': ['/root/storage'], 'design_contributors': ['/root'],
            'review_role': outer_role, 'disposition': 'ACCEPT', 'launcher_sha256': hashes[SELF],
            'plan_sha256': hashes[PLAN], 'request_sha256': hashes[REQUEST], 'limits_acknowledged': ACK,
        }, 'actual outer source/instance acceptance')
    require(Path(os.path.realpath(sys.executable)) == PYTHON
            and Path(os.path.realpath('/usr/bin/python3')) == PYTHON, 'fixed resolved Python executable')
    captures[PYTHON] = d.read(PYTHON, True, LIMITS['python_bytes'])
    require(request['toolchain']['python'] == {
        'path': str(PYTHON), 'sha256': sha(captures[PYTHON][0]), 'identity': captures[PYTHON][1],
    } and not captures[PYTHON][1]['mode'] & (stat.S_ISUID | stat.S_ISGID), 'original Python bytes/identity')
    for path in (BASE, E, LOCK.parent):
        d.directory(path, True)
    require(all(request['directory_pins'].get(path) == original for path, original in d.pins.items()),
            'root-sealed required original ancestor pins')
    require(d.pins[str(E)]['uid'] == UID and d.pins[str(E)]['mode'] == stat.S_IFDIR | 0o700,
            'original private runner evidence root')
    bindings.update(accept_root_sha256=hashes[APPROVALS[0]], accept_verification_sha256=hashes[APPROVALS[1]])
    return request, captures, bindings


class Supervision:
    def __init__(self, d):
        self.d, self.child, self.original = d, None, None
        self.files, self.errors, self.streams = {}, [], {}
        self.started, self.started_ns = time.monotonic(), time.time_ns()
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
            if now - self.started >= LIMITS['outer_soft_seconds']:
                self.fail('outer 6000-second soft timeout', timeout=True)
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
            if self.kill_at is not None and time.monotonic() - self.kill_at >= LIMITS['kill_wait_seconds']:
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
    request = captures = bindings = None
    lock_fd, lock_held, code = None, False, 1
    try:
        request, captures, bindings = admission(d)
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
            require(d.read(path, cap=LIMITS['python_bytes'] if path == PYTHON else None) == original,
                    'under-probe source/request/acceptance image drift')
        require(pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False)) == expected_lock,
                'under-probe original lock drift')
        require(time.time() <= request['expires_unix_seconds'], 'expired before launcher mutation')
        require(absent(R.name, d.directory(BASE)) and not os.listdir(d.directory(E)),
                'original runtime/evidence consumed; no retry/adoption')
        require(all(absent(path.name, d.directory(C)) for path in OUTPUTS + (C / 'LAUNCHER.json',)),
                'launcher evidence/receipt already occupied; no retry')
        initial = resources(d, True)
        require(initial['floor_ok'], 'launcher admission disk/RAM floor')
        parent_birth = direct_row(os.getpid())
        require(parent_birth['uid'] == UID, 'parent original uid')
        # REQUEST is prospective first-file authority; exclusive creation consumes this attempt.
        state.files['processes'] = Evidence(d, OUTPUTS[3])
        state.files['processes'].begin()
        state.event('outer_admission', bindings=bindings, outer_launcher=request['outer_launcher'],
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
        require(pin(os.fstat(lock_fd)) == expected_lock
                == pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False)), 'prelaunch original lock drift')
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            require(not CANCEL and not signal.sigpending() & SIGNALS, 'cancel before direct launch intent')
            state.event('direct_launch_intent', argv=ARGV, cwd=str(W), environment=ENV,
                        no_other_audit_local_or_ci_job=True, original_lock_not_held=True)
            require(not state.errors and not signal.sigpending() & SIGNALS, 'cancel/error before launch commitment')
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
                    require(d.read(path, cap=LIMITS['python_bytes'] if path == PYTHON else None) == original,
                            'post-run frozen input drift')
                require(pin(os.fstat(lock_fd)) == request['lock_pin']
                        == pin(os.stat(LOCK.name, dir_fd=d.directory(LOCK.parent), follow_symlinks=False)),
                        'post-run original lock drift')
                fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                fcntl.flock(lock_fd, fcntl.LOCK_UN)
                state.event('original_lock_available_after_direct_wait')
            except BaseException as error:
                state.fail('post-run binding/coordination: ' + type(error).__name__ + ': ' + str(error))
        if 'resources' in state.files:
            state.sample()
        for name in ('stdout', 'stderr', 'resources'):
            writer = state.files.get(name)
            if writer is not None:
                try:
                    if name == 'resources' and not writer.failed:
                        writer.finish({'status': 'OBSERVATIONS_NOT_HOST_EXCLUSIVITY_OR_APPLICATION_PASS',
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
            'completed': complete, 'runner_exit': state.exit_observed, 'outer_exit_intent': code,
            'external_timeout_observed': state.timed_out,
            'external_interruption_observed': state.interrupted,
            'term_attempted': state.term_attempted, 'kill_attempted': state.kill_attempted,
            'original_runner': state.original, 'stdout_stderr_bytes_seen': state.seen,
            'discarded_bytes': state.discarded, 'errors': state.errors, 'additional_errors_omitted': state.omitted_errors,
            'runner_stop_and_descendants': 'NOT_PROVEN_BY_PARENT_REQUIRE_ORIGINAL_RUNNER_EVIDENCE_AND_REVIEW',
            'runtime_evidence': 'RETAINED_NO_PARENT_DELETION_OR_RETRY',
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
            except BaseException:
                code = 1
        if d.close():
            code = 1
        if code:
            try:
                os.write(2, ('OUTER_HOLD_RETAIN_ORIGINALS: ' + '; '.join(state.errors)[:4096] + '\n').encode())
            except BaseException:
                pass
    os._exit(code)  # Only root's later tool observation can establish the actual outer exit.


if __name__ == '__main__':
    main()
