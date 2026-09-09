#!/usr/bin/python3
"""Root-authored, once-only finite namespace metadata caller; not a build runner."""
import fcntl
import hashlib
import json
import os
from pathlib import Path
import resource
import selectors
import signal
import stat
import subprocess
import sys
import time

B = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux')
E = B / 'runs/linux-namespace-feasibility01'
PROBE = B / 'reviews/storage/LINUX-NAMESPACE-FEASIBILITY-PROBE.py'
LOCK = Path('/root/projects/PassVault/.audit-coordination-linux-20260908/build.lock')
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
IMAGES = {
    '/usr/bin/unshare': '51bcc77ba5db162c80028f861f0a2770d728c1de80773816d863f28d7a817adb',
    '/usr/bin/python3.12': '1643dacd9feaedc58f3cc581e4d22577dfe25c09b10282936186ccf0f2e61118',
    str(PROBE): '96eb1d54c690fd19181a76a5abfeff874cde42ef2e87aa89260a6c29daea1fee',
}
CANCELLED = []


def cancel(number, _frame):
    CANCELLED.append(number)


def namespaces():
    return {k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')}


def pin(s):
    return [s.st_dev, s.st_ino, s.st_uid, s.st_mode, s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns]


def stable_image(path):
    before = os.stat(path, follow_symlinks=False)
    assert stat.S_ISREG(before.st_mode) and before.st_size <= 16 * 1024 * 1024
    fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW)
    try:
        assert pin(os.fstat(fd)) == pin(before)
        h = hashlib.sha256()
        while chunk := os.read(fd, 1024 * 1024):
            h.update(chunk)
        assert pin(os.fstat(fd)) == pin(before) == pin(os.stat(path, follow_symlinks=False))
        return h.hexdigest()
    finally:
        os.close(fd)


def main():
    assert sys.platform == 'linux' and len(sys.argv) == 1 and sys.flags.isolated
    assert sys.dont_write_bytecode and sys.flags.no_site and os.environ == ENV
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, cancel)
    signal.signal(signal.SIGCHLD, signal.SIG_DFL)
    signal.pthread_sigmask(signal.SIG_UNBLOCK, {signal.SIGINT, signal.SIGTERM, signal.SIGHUP, signal.SIGALRM})
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    resource.setrlimit(resource.RLIMIT_AS, (128 * 1024 * 1024, 128 * 1024 * 1024))
    resource.setrlimit(resource.RLIMIT_NOFILE, (64, 64))
    parent = namespaces()
    start = int(Path('/proc/self/stat').read_text().rsplit(') ', 1)[1].split()[19])
    print(json.dumps({'caller_pid': os.getpid(), 'caller_start': start, 'parent_namespaces': parent}), flush=True)
    for path, expected in IMAGES.items():
        assert stable_image(path) == expected
    assert os.readlink('/usr/bin/python3') == 'python3.12'
    memory = {k: int(v.strip().split()[0]) * 1024 for k, v in
              (line.split(':', 1) for line in Path('/proc/meminfo').read_text().splitlines())}
    free = os.statvfs(B).f_bavail * os.statvfs(B).f_frsize
    assert free >= 12 * 1024 ** 3 and memory['MemAvailable'] >= memory['MemTotal'] / 4
    assert B.resolve() == B and stat.S_ISDIR(E.parent.lstat().st_mode)
    assert E.parent.stat().st_uid == 0 and E.parent.stat().st_mode & 0o077 == 0
    lock = os.open(LOCK, os.O_RDONLY | os.O_NOFOLLOW)
    efd = pidfd = None
    child = None
    collector = selectors.DefaultSelector()
    output = {'stdout': bytearray(), 'stderr': bytearray()}
    errors = []
    kill_attempted = False
    code = None
    settled = False
    def record(name, data):
        raw = data if isinstance(data, bytes) else (json.dumps(data, sort_keys=True) + '\n').encode()
        assert len(raw) <= 16384
        fd = os.open(name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600, dir_fd=efd)
        try:
            view = memoryview(raw)
            while view:
                view = view[os.write(fd, view):]
            os.fsync(fd)
        finally:
            os.close(fd)
        os.fsync(efd)
    try:
        ls = os.fstat(lock)
        assert (ls.st_dev, ls.st_ino, ls.st_uid, ls.st_mode, ls.st_nlink, ls.st_size) == (24, 14189001, 0, 0o100600, 1, 0)
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        assert not CANCELLED
        E.mkdir(mode=0o700)
        efd = os.open(E, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        parent_fd = os.open(E.parent, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        try:
            os.fsync(parent_fd)
        finally:
            os.close(parent_fd)
        argv = ['/usr/bin/unshare', '--mount', '--pid', '--fork', '--kill-child=SIGKILL',
                '--propagation=private', '--mount-proc=/proc', '--', '/usr/bin/python3',
                '-I', '-B', '-S', str(PROBE), parent['pid'], parent['mnt']]
        record('INTENT.json', {'argv': argv, 'environment': ENV, 'cwd': '/', 'evidence_pin': pin(os.fstat(efd)),
                              'parent_namespaces': parent, 'disk_free': free,
                              'memory_available': memory['MemAvailable'], 'memory_total': memory['MemTotal'],
                              'deadline_seconds': 10, 'post_kill_wait_seconds': 5,
                              'kind': 'FINITE_NAMESPACE_METADATA_NOT_APPLICATION_TEST'})
        deadline = time.monotonic() + 10
        assert not CANCELLED
        child = subprocess.Popen(argv, cwd='/', env=ENV, stdin=subprocess.DEVNULL,
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                 close_fds=True, start_new_session=True)
        pidfd = os.pidfd_open(child.pid)
        record('CHILD.json', {'pid': child.pid, 'original_unreaped_direct_child_pidfd': True})
        for name, stream in (('stdout', child.stdout), ('stderr', child.stderr)):
            os.set_blocking(stream.fileno(), False)
            collector.register(stream, selectors.EVENT_READ, name)
        while collector.get_map() or child.poll() is None:
            if CANCELLED or time.monotonic() >= deadline or errors:
                errors.append('cancelled_deadline_or_output_failure')
                break
            for key, _events in collector.select(0.1):
                chunk = os.read(key.fd, 4097)
                if not chunk:
                    collector.unregister(key.fileobj)
                    continue
                data = output[key.data]
                remaining = 4096 - len(data)
                data.extend(chunk[:remaining])
                if len(chunk) > remaining:
                    errors.append(key.data + '_truncated_at_4096')
    except BaseException as exc:
        errors.append(type(exc).__name__ + ': ' + str(exc)[:512])
    finally:
        try:
            if child is not None:
                try:
                    if child.poll() is None and pidfd is not None:
                        kill_attempted = True
                        try:
                            signal.pidfd_send_signal(pidfd, signal.SIGKILL)
                        except BaseException as exc:
                            errors.append('owned_signal_failed_no_retry: ' + type(exc).__name__ + ': ' + str(exc)[:512])
                    code = child.wait(timeout=5)
                    settled = True
                except BaseException as exc:
                    errors.append('settlement_HOLD: ' + type(exc).__name__ + ': ' + str(exc)[:512])
                finally:
                    for stream in (child.stdout, child.stderr):
                        if stream is not None:
                            try:
                                stream.close()
                            except BaseException as exc:
                                errors.append('pipe_close: ' + type(exc).__name__)
            if code != 0:
                errors.append('unshare_exit_' + str(code))
            after = namespaces()
            if after != parent:
                errors.append('parent_namespace_changed')
            if CANCELLED:
                errors.append('caller_cancelled_' + str(CANCELLED))
            if not errors:
                try:
                    obj = json.loads(bytes(output['stdout']))
                    assert obj['format'] == 'passvault-linux-namespace-self-v1' and obj['pid'] == 1 and obj['ppid'] == 0
                    assert obj['parent_namespaces'] == parent and obj['proc_nspid'] == [1]
                    assert all(obj['self_namespaces'][k] != parent[k] for k in parent)
                    assert obj['private_mount_propagation'] is True and not output['stderr']
                except BaseException as exc:
                    errors.append('metadata_refused: ' + type(exc).__name__ + ': ' + str(exc)[:512])
            if efd is not None:
                record('stdout.log', bytes(output['stdout']))
                record('stderr.log', bytes(output['stderr']))
                record('RESULT.json', {'exit': code, 'errors': errors, 'owned_pidfd_kill_attempted': kill_attempted,
                                       'direct_wait_completed': settled, 'parent_namespaces_before': parent,
                                       'parent_namespaces_after': after, 'status': 'PRETERMINAL_FINITE_FEASIBILITY_ONLY',
                                       'limits': 'Requires external exit0/reconciliation; not descendant/cancellation proof, build admission or old HOLD release.'})
        finally:
            close_errors = []
            try:
                collector.close()
            except BaseException as exc:
                close_errors.append('collector: ' + type(exc).__name__)
            for fd in (pidfd, efd, lock):
                if fd is not None:
                    try:
                        os.close(fd)
                    except BaseException as exc:
                        close_errors.append('fd: ' + type(exc).__name__)
            if close_errors:
                raise RuntimeError('post-result close HOLD: ' + repr(close_errors))
    return 0 if not errors else 1


if __name__ == '__main__':
    sys.exit(main())
