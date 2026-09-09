#!/usr/bin/python3
"""PROPOSAL: root alone may run once, after independent review, with -I -B.

Read-only Git/checkout capture, except exclusive SOURCE.json creation. No clone,
filters, network, project execution, old helpers, caches or deletion. Root must
freeze the preserved repository and retain an external command/exit receipt.
Per-file stable observations are not an atomic tree snapshot or build admission.
120s whole-operation alarm; direct-child cleanup has one 5s wait. Resource polls
target 5s between files, not an OS quota or a bound on synchronous filesystem IO.
SIGKILL/host loss cannot be cleaned up here; interrupted/partial output or an
unsettled child is HOLD, no retry. Root must retain the bounded stdout journal.
"""

import contextlib
import hashlib
import json
import os
from pathlib import Path
import signal
import stat
import subprocess
import sys
import time


REPO = Path('/root/projects/PassVault/passvault')
OUT = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/'
           '2026-09-08-linux/reviews/linux-runner/SOURCE.json')
COMMIT = '9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed'
TREE = '05014e9f635131d5db06701e4013b4b5a746465a'
COUNT, FILE_CAP, TOTAL_CAP = 1198, 16 * 1024 ** 2, 256 * 1024 ** 2
ATTRS = {
    '.gitattributes': '8884ed2a100ce791326a3a8d8d4c12a5e0f68d96ecb382fd3a6612ad827604c5',
    'docs/audit-handoff/.gitattributes': 'd46d4df5b35477e25356b95679421f35f1340fa5bcabe95005fc16e2e5f7ade0',
    'docs/audit-publication/.gitattributes': '705fd4d6451a31d36b3df7de96f83f30ac976c9b4a6d1e51671d8e2f33e2d0da',
}
# Exact historical raw/Git/checkout triples; not a generic whitespace allowance.
QUALIFIED = (
    ('test-windows-checksum-boundary.ps1', 11703,
     '17e5d8e8a30ec0ececf31fd20453938a904ab657573b80cccaee72fe35943141', 11703,
     '17e5d8e8a30ec0ececf31fd20453938a904ab657573b80cccaee72fe35943141', 11940,
     '7c9dae59e786612de3852b28ee8bb1ee64ba3b97d31af8d9f3d50f0617c818c7'),
    ('update-desktop-biometric-checksum.ps1', 4213,
     '3d7221596a01c46b23100f7f05b6c0ebd246af141ce943b3f63f4704be19a396', 4132,
     'aa399f28b78df8a81a46e3be198022e8dce34dfd8bd7b57e91b71a3e5a7ed203', 4221,
     '855c8f71a8437d2469917ff75dc6d17b3ac34c041d3cfbc8d8b6e15410541aa1'),
)
ENV = {
    'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC',
    'HOME': '/dev/null', 'XDG_CONFIG_HOME': '/dev/null',
    'GIT_CONFIG_NOSYSTEM': '1', 'GIT_CONFIG_GLOBAL': '/dev/null',
    'GIT_OPTIONAL_LOCKS': '0', 'GIT_TERMINAL_PROMPT': '0',
    'GIT_NO_REPLACE_OBJECTS': '1', 'GIT_NO_LAZY_FETCH': '1',
}
GIT = [
    '/usr/bin/git', '--no-pager', '-c', 'core.hooksPath=/dev/null',
    '-c', 'core.fsmonitor=false', '-c', 'gc.auto=0', '-c', 'maintenance.auto=false',
    '-c', 'protocol.allow=never', '-c', 'core.attributesFile=/dev/null',
    '-c', 'core.autocrlf=false', '-c', 'core.eol=lf', '-C', str(REPO),
]
SIGNALS = {signal.SIGINT, signal.SIGTERM, signal.SIGHUP, signal.SIGALRM}
STDOUT_BYTES = 0
CHILD_NUMBER = 0


def require(ok, reason):
    if not ok:
        raise RuntimeError(reason)


def sha(data):
    return hashlib.sha256(data).hexdigest()


def pin(st):
    return tuple(getattr(st, key) for key in (
        'st_dev', 'st_ino', 'st_mode', 'st_uid', 'st_gid', 'st_nlink',
        'st_size', 'st_mtime_ns', 'st_ctime_ns'))


def emit(event):
    global STDOUT_BYTES
    line = json.dumps(event, sort_keys=True, separators=(',', ':'), ensure_ascii=True) + '\n'
    size = len(line.encode())
    require(size <= 4096 and STDOUT_BYTES + size <= 64 * 1024, 'stdout journal byte bound')
    STDOUT_BYTES += size
    require(sys.stdout.write(line) == len(line), 'short stdout journal write')
    sys.stdout.flush()


@contextlib.contextmanager
def owned_fd(path, flags, mode=0o600, dir_fd=None):
    """The stream may borrow this FD; fdopen failure never transfers ownership."""
    fd = None
    try:
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            fd = os.open(path, flags, mode, dir_fd=dir_fd)
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)
        yield fd
    finally:
        if fd is not None:
            old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
            try:
                os.close(fd)
            finally:
                signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)


@contextlib.contextmanager
def directory(base_fd, parts):
    opened = []
    fd = base_fd
    try:
        for name in parts:
            require(name not in ('', '.', '..'), 'invalid directory component')
            old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
            try:
                child = os.open(name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=fd)
                try:
                    opened.append((fd, name, child, None))  # Register before fstat can fail.
                except BaseException:
                    os.close(child)
                    raise
            finally:
                signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)
            st = os.fstat(child)
            opened[-1] = (fd, name, child, (st.st_dev, st.st_ino, st.st_uid, st.st_mode))
            fd = child

        def verify():
            for parent, name, child, original in opened:
                for st in (os.fstat(child), os.stat(name, dir_fd=parent, follow_symlinks=False)):
                    require((st.st_dev, st.st_ino, st.st_uid, st.st_mode) == original, 'directory replacement')
        verify()
        yield fd, verify
        verify()
    finally:
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            errors = []
            for _, _, child, _ in reversed(opened):
                try:
                    os.close(child)
                except OSError as error:
                    errors.append(type(error).__name__)
            require(not errors, 'directory descriptor cleanup HOLD: ' + ','.join(errors))
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)


def stable_file(root_fd, path):
    parts = path.split('/')
    with directory(root_fd, parts[:-1]) as (parent, _), owned_fd(
            parts[-1], os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent) as fd:
        with os.fdopen(fd, 'rb', closefd=False) as stream:
            before = os.fstat(stream.fileno())
            require(stat.S_ISREG(before.st_mode) and before.st_size <= FILE_CAP, 'file type/size')
            require(not before.st_mode & (stat.S_ISUID | stat.S_ISGID), 'set-id source file')
            data = stream.read(FILE_CAP + 1)
            require(len(data) == before.st_size and len(data) <= FILE_CAP, 'file grew/truncated')
            require(pin(before) == pin(os.fstat(stream.fileno())) ==
                    pin(os.stat(parts[-1], dir_fd=parent, follow_symlinks=False)), 'unstable source file')
            return data, bool(before.st_mode & 0o111)


def resources(repo_fd, out_fd, phase, launch=False):
    disks = []
    floor = (12 if launch else 8) * 1024 ** 3
    for path, fd in ((str(REPO), repo_fd), (str(OUT.parent), out_fd)):
        fs = os.fstatvfs(fd)
        disks.append({'path': path, 'available_bytes': fs.f_bavail * fs.f_frsize})
    with owned_fd('/proc/meminfo', os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC) as fd:
        with os.fdopen(fd, 'rb', closefd=False) as stream:
            data = stream.read(64 * 1024 + 1)
    require(len(data) <= 64 * 1024, 'meminfo byte bound')
    memory = {}
    for line in data.splitlines():
        key, _, value = line.partition(b':')
        if key in (b'MemTotal', b'MemAvailable'):
            parts = value.split()
            require(key not in memory and len(parts) == 2 and parts[0].isdigit() and parts[1] == b'kB',
                    'unexpected meminfo fields')
            memory[key] = int(parts[0]) * 1024
    require(set(memory) == {b'MemTotal', b'MemAvailable'} and
            0 <= memory[b'MemAvailable'] <= memory[b'MemTotal'] and memory[b'MemTotal'] > 0, 'invalid memory sample')
    fraction = 0.25 if launch else 0.20
    okay = all(d['available_bytes'] >= floor for d in disks) and memory[b'MemAvailable'] >= fraction * memory[b'MemTotal']
    row = {'event': 'resources', 'phase': phase, 'unix_seconds': time.time(), 'disks': disks,
           'memory_total_bytes': memory[b'MemTotal'], 'memory_available_bytes': memory[b'MemAvailable'],
           'disk_floor_bytes': floor, 'memory_fraction_floor': fraction, 'within_floors': okay}
    emit(row)
    require(okay, 'resource floor HOLD')
    return row


@contextlib.contextmanager
def git_child(args):
    global CHILD_NUMBER
    CHILD_NUMBER += 1
    number = CHILD_NUMBER
    child = None
    original_pipes = ()
    launch_called = False
    try:
        emit({'event': 'git_child_intent', 'child_number': number, 'argv': GIT + args})
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            require(not signal.sigpending() & SIGNALS, 'pending cancellation before Git')
            launch_called = True
            child = subprocess.Popen(GIT + args, env=ENV, cwd=REPO, stdin=subprocess.PIPE,
                                     stdout=subprocess.PIPE, stderr=subprocess.STDOUT, close_fds=True,
                                     preexec_fn=lambda: signal.pthread_sigmask(signal.SIG_SETMASK, old_mask))
            original_pipes = (('stdin', child.stdin), ('stdout', child.stdout))
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)
        emit({'event': 'git_child_original', 'child_number': number, 'original_pid': child.pid})
        yield child
    finally:
        active_error = sys.exc_info()[1]
        record = {'event': 'git_child_settlement', 'child_number': number,
                  'original_pid': child.pid if child is not None else None, 'launch_called': launch_called,
                  'body_error': type(active_error).__name__ if active_error else None,
                  'actual_exit': None, 'kill_attempted': False, 'pipes': {}, 'cleanup_errors': []}
        old_mask = signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
        try:
            try:
                if child is not None:
                    if child.poll() is None:
                        record['kill_attempted'] = True
                        child.kill()  # Only this unreaped direct Popen child; never a name/group.
                    record['actual_exit'] = child.wait(timeout=5)  # Sole cleanup wait; no retry.
            except BaseException as error:
                record['cleanup_errors'].append(type(error).__name__)
                if child is not None:
                    record['actual_exit'] = child.returncode
            finally:
                # Preserve these original pipe objects, including on timeout/unknown settlement.
                for name, pipe in original_pipes:
                    if pipe is not None:
                        try:
                            pipe.close()
                        except BaseException as error:
                            record['cleanup_errors'].append(name + ':' + type(error).__name__)
                        record['pipes'][name] = bool(pipe.closed)
            record['settled'] = child is not None and record['actual_exit'] is not None
            pipes_closed = child is None or record['pipes'] == {'stdin': True, 'stdout': True}
            hold = (launch_called and not record['settled']) or not pipes_closed or bool(record['cleanup_errors'])
            record['disposition'] = 'HOLD' if hold else ('SETTLED' if child is not None else 'NOT_LAUNCHED')
            emit(record)
            require(not hold, 'Git original-child settlement/pipe cleanup HOLD; see stdout receipt')
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, old_mask)


def git_small(args, cap, receipts):
    with git_child(args) as child:
        child.stdin.close()
        data = child.stdout.read(cap + 1)
        require(len(data) <= cap and child.wait(timeout=5) == 0, 'Git output/exit bound')
    receipts.append({'argv': GIT + args, 'exit': 0, 'stdout_bytes': len(data), 'stdout_sha256': sha(data)})
    return data


def main():
    require(len(sys.argv) == 1 and sys.flags.isolated and sys.dont_write_bytecode, 'requires no arguments and -I -B')
    started = time.time()

    def interrupted(number, _frame):
        raise RuntimeError('capture interrupted by signal ' + str(number))
    for number in SIGNALS:
        signal.signal(number, interrupted)
    signal.setitimer(signal.ITIMER_REAL, 120)
    receipts, files, special, samples = [], [], {}, []
    special_paths = {p for q in QUALIFIED for p in (
        'scripts/' + q[0], 'docs/audit-handoff/raw-source/' + q[0] + '.raw')}
    try:
        with owned_fd('/', os.O_RDONLY | os.O_DIRECTORY | os.O_CLOEXEC) as root_fd, \
                directory(root_fd, REPO.parts[1:]) as (repo_fd, verify_repo), \
                directory(root_fd, OUT.parent.parts[1:]) as (out_fd, verify_out), contextlib.ExitStack() as final_sample:
            def finish_resources(error_type, error, _traceback):
                if error_type is not None:
                    emit({'event': 'capture_source_error_before_final_sample', 'error_type': error_type.__name__,
                          'reason': str(error)[:256]})
                resources(repo_fd, out_fd, 'final')
                return False
            final_sample.push(finish_resources)  # Runs before original directory descriptors close, also on failure.
            try:
                os.stat(OUT.name, dir_fd=out_fd, follow_symlinks=False)
            except FileNotFoundError:
                pass
            else:
                raise RuntimeError('SOURCE.json occupied; no overwrite/retry')
            samples.append(resources(repo_fd, out_fd, 'initial', launch=True))
            last_sample = time.monotonic()
            identity_args = ['rev-parse', 'HEAD', 'HEAD^{tree}']
            expected_identity = (COMMIT + '\n' + TREE + '\n').encode()
            require(git_small(identity_args, 256, receipts) == expected_identity, 'checkpoint HEAD/tree mismatch')
            listing = git_small(['ls-tree', '-r', '-z', '-l', '--full-tree', COMMIT], 512 * 1024, receipts)
            require(listing.endswith(b'\0'), 'incomplete tree listing')
            entries = []
            for raw in listing[:-1].split(b'\0'):
                header, path_bytes = raw.split(b'\t', 1)
                mode, kind, oid, size = header.decode('ascii').split()
                path = path_bytes.decode('utf-8')
                require(mode in ('100644', '100755') and kind == 'blob', 'non-regular Git entry')
                require(len(oid) == 40 and all(c in '0123456789abcdef' for c in oid), 'invalid blob identity')
                require(size.isdecimal() and int(size) <= FILE_CAP, 'Git file size bound')
                require(len(path_bytes) <= 512 and all(p not in ('', '.', '..', '.git') for p in path.split('/'))
                        and not any(c in path for c in '\\\r\n\t'), 'invalid source path')
                entries.append((path, mode, oid, int(size)))
            require(len(entries) == COUNT and len({e[0] for e in entries}) == COUNT, '1198 unique source entries required')
            require(sum(e[3] for e in entries) <= TOTAL_CAP, 'Git total byte bound')
            require({e[0] for e in entries if e[0].split('/')[-1] == '.gitattributes'} == set(ATTRS),
                    'unexpected attribute policy')
            batch_hash, batch_bytes, checkout_bytes = hashlib.sha256(), 0, 0
            with git_child(['cat-file', '--batch']) as child:
                for path, mode, oid, size in sorted(entries, key=lambda e: (e[0] not in ATTRS, e[0])):
                    if time.monotonic() - last_sample >= 5:
                        samples.append(resources(repo_fd, out_fd, 'per_file_loop'))
                        last_sample = time.monotonic()
                    child.stdin.write((oid + '\n').encode())
                    child.stdin.flush()
                    header = child.stdout.readline(128)
                    require(header == f'{oid} blob {size}\n'.encode(), 'unexpected cat-file header')
                    require(batch_bytes + len(header) + size + 1 <= TOTAL_CAP, 'framed Git batch byte bound')
                    blob = child.stdout.read(size)
                    suffix = child.stdout.read(1)
                    require(len(blob) == size and suffix == b'\n', 'incomplete Git blob')
                    require(hashlib.sha1(b'blob ' + str(size).encode() + b'\0' + blob).hexdigest() == oid,
                            'Git blob content identity mismatch')
                    for chunk in (header, blob, suffix):
                        batch_hash.update(chunk)
                        batch_bytes += len(chunk)
                    require(batch_bytes <= TOTAL_CAP, 'actual Git batch byte bound')
                    actual, executable = stable_file(repo_fd, path)
                    require(executable == (mode == '100755'), 'checkout executable-bit mismatch')
                    if path in ATTRS:
                        require(sha(blob) == ATTRS[path], 'fixed attributes changed')
                    windows_text = path.endswith(('.ps1', '.bat', '.cmd')) and not path.startswith(
                        ('docs/audit-handoff/', 'docs/audit-publication/'))
                    if windows_text:
                        require(b'\r' not in blob, 'unexpected CR in Windows text Git blob')
                    expected = blob.replace(b'\n', b'\r\n') if windows_text else blob
                    require(actual == expected, 'checkout bytes differ from exact permitted Git form: ' + path)
                    checkout_bytes += len(actual)
                    require(checkout_bytes <= TOTAL_CAP, 'checkout total byte bound')
                    files.append({'path': path, 'git_mode': mode, 'git_blob': oid,
                                  'checkout_sha256': sha(actual), 'checkout_size': len(actual)})
                    if path in special_paths:
                        special[path] = (blob, actual)
                child.stdin.close()
                require(child.stdout.read(1) == b'' and child.wait(timeout=5) == 0, 'extra batch output/nonzero exit')
            receipts.append({'argv': GIT + ['cat-file', '--batch'], 'exit': 0,
                             'stdout_bytes': batch_bytes, 'stdout_sha256': batch_hash.hexdigest()})
            qualifications = []
            for name, raw_n, raw_sha, git_n, git_sha, checkout_n, checkout_sha in QUALIFIED:
                path, raw_path = 'scripts/' + name, 'docs/audit-handoff/raw-source/' + name + '.raw'
                blob, actual = special[path]
                raw = special[raw_path][1]
                require((len(raw), sha(raw), len(blob), sha(blob), len(actual), sha(actual)) ==
                        (raw_n, raw_sha, git_n, git_sha, checkout_n, checkout_sha), 'qualified EOL identity mismatch')
                require(raw.replace(b'\r\n', b'\n') == blob and blob.replace(b'\n', b'\r\n') == actual,
                        'qualified raw/Git/checkout transformation mismatch')
                qualifications.append({'path': path, 'raw_copy': raw_path, 'raw_bytes': raw_n,
                                       'raw_sha256': raw_sha, 'git_blob_bytes': git_n, 'git_blob_sha256': git_sha,
                                       'checkout_bytes': checkout_n, 'checkout_sha256': checkout_sha,
                                       'qualification': 'Exact raw-to-LF-Git-to-CRLF-checkout only; no inherited raw-byte coverage/runtime pass.'})
            require(git_small(identity_args, 256, receipts) == expected_identity, 'checkpoint changed during capture')
            verify_repo()
            verify_out()
            samples.append(resources(repo_fd, out_fd, 'prewrite'))
            report = {'format': 'passvault-linux-checkout-source-v1', 'author': '/root',
                      'capture_tool_author': '/root/build_config', 'commit': COMMIT, 'tree': TREE,
                      'files': sorted(files, key=lambda f: f['path']), 'checkout_eol_qualifications': qualifications,
                      'capture': {'started_unix_seconds': started, 'finished_unix_seconds': time.time(),
                                  'commands': receipts, 'environment': ENV, 'file_cap': FILE_CAP,
                                  'framed_git_batch_cap': TOTAL_CAP, 'checkout_total_cap': TOTAL_CAP,
                                  'framed_git_batch_bytes': batch_bytes, 'checkout_bytes': checkout_bytes,
                                  'resource_samples_before_output': samples, 'resource_poll_target_seconds': 5,
                                  'final_resource_and_child_settlement_receipt': 'root-retained external stdout',
                                  'stdout_journal_cap': 64 * 1024,
                                  'application_test_executions': 0, 'gradle_stop': 'NOT_APPLICABLE',
                                  'qualification': 'Per-file stable data capture, not atomic tree/clean-worktree/runner admission. Root must rebind under the fresh execution lock.'}}
            data = (json.dumps(report, indent=2, ensure_ascii=True) + '\n').encode()
            require(len(data) <= 1024 ** 2, 'manifest byte bound')
            with owned_fd(OUT.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                          0o600, dir_fd=out_fd) as fd:
                with os.fdopen(fd, 'wb', closefd=False) as stream:
                    require(stream.write(data) == len(data), 'short manifest write')
                    stream.flush()
                    os.fsync(stream.fileno())
                    require(pin(os.fstat(stream.fileno())) ==
                            pin(os.stat(OUT.name, dir_fd=out_fd, follow_symlinks=False)), 'output replaced')
            os.fsync(out_fd)
        emit({'event': 'capture_source_complete', 'output': str(OUT), 'bytes': len(data),
              'sha256': sha(data), 'files': COUNT})
    finally:
        signal.setitimer(signal.ITIMER_REAL, 0)


if __name__ == '__main__':
    try:
        main()
    except BaseException as error:
        print('CAPTURE_SOURCE_REFUSED_HOLD_NO_RETRY: ' + type(error).__name__ + ': ' + str(error)[:512], file=sys.stderr)
        sys.exit(1)
else:
    raise RuntimeError('This fixed capture utility must not be imported.')
