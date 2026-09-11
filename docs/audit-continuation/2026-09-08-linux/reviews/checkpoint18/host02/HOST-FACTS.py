#!/usr/bin/python3.12
"""One root-admitted read-only current-host observation, not a build admission.

No subprocess, Git invocation, runtime descent/allocation, deletion, or recovery.
Historical requests are data only. Reuse exact regular images, never old directory
authority. Stdout plus the original terminal result are the evidence; no replay.
120s is a cooperative deadline, not a bound on an uninterruptible kernel call.
"""
import fcntl
import hashlib
import json
import os
import signal
import stat
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
T = W.parent / 'passvault-publication-20260910-01'
G = T / '.git'
LOCK = W.parent / '.audit-coordination-linux-20260908/build.lock'
SDK = Path('/opt/android-sdk')
SELF = B / 'reviews/checkpoint18/host02/HOST-FACTS.py'
REQUESTS = {
    'LINUX-ANDROID-COMPILE-02.json': '317f9ce411b57c1755bbc333911e5081d93d7d788c2ffae4014ab69570d2928e',
    'LINUX-DETEKT-04.json': '6cea7c172cdb7b97d7eec1a954d561baf4d06fa8daf2799a92d271b423728450',
    'LINUX-FOCUSED-REGRESSION-01.json': '88da235c80fb4f6081eab0d13e255096b82769bcb9ad0e40d72dd022db3bb302',
}
ORIGINAL_LOCK = {'dev': 24, 'ino': 14189001, 'uid': 0, 'mode': 33152, 'nlink': 1,
                 'bytes': 0, 'mtime_ns': 1788910891124735946, 'ctime_ns': 1788910891124735946}
DIRECTORIES = (W.parent, LOCK.parent, B / 'runs', T, G, W, SDK)
NEW_RUNS = ('android-compile03', 'detekt04', 'focused-regression01')
ABSENT = tuple(p for name in NEW_RUNS for p in
               (W.parent / ('audit-runtime-linux-' + name), B / ('runs/linux-' + name)))
REDIRECTS = ('commondir', 'config.worktree', 'worktrees', 'info',
             'objects/info/alternates', 'objects/info/http-alternates')
OD = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
OF = os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC
MIB = 1024 ** 2
START = time.monotonic()
SIGNALS = (signal.SIGTERM, signal.SIGINT, signal.SIGALRM)
CANCELLED = False
fds = {}
out = {'format': 'passvault-current-host02-facts-v1', 'owner': '/root',
       'utc': datetime.now(timezone.utc).isoformat(), 'status': 'HOLD',
       'directories': {}, 'images': {}, 'resources': [], 'absences': [],
       'closed_descriptors': False, 'no_children_or_runtime_actions': True,
       'qualification': 'Current read-only point facts; no inferred drift cause, historical directory continuity, '
                        'global process-idleness, build/cleanup admission or product evidence.'}


def need(ok, message):
    if CANCELLED or time.monotonic() - START >= 120:
        raise RuntimeError('observation interrupted or 120s cooperative deadline')
    if not ok:
        raise RuntimeError(message)


def expired(signum, frame):
    # Do not raise between an open() return and its cleanup registration, or
    # interrupt the close sweep. Guarded ordinary control flow observes this.
    global CANCELLED
    CANCELLED = True


def pin(s, directory=False):
    value = {k: getattr(s, 'st_' + k) for k in ('dev', 'ino', 'uid', 'mode', 'nlink')}
    if not directory:
        value.update(bytes=s.st_size, mtime_ns=s.st_mtime_ns, ctime_ns=s.st_ctime_ns)
    return value


def directory(path):
    need(path.is_absolute() and '..' not in path.parts, 'fixed absolute path')
    parent = None if path == Path('/') else directory(path.parent)
    if path not in fds:
        fd = os.open('/' if parent is None else path.name, OD, dir_fd=parent)
        try:
            p = pin(os.fstat(fd), True)
            need(p['uid'] == 0 and stat.S_ISDIR(p['mode']) and not p['mode'] & 0o022,
                 'directory owner/type/mode: ' + str(path))
            fds[path] = (fd, p)
        except BaseException:
            os.close(fd)
            raise
    fd, p = fds[path]
    need(pin(os.fstat(fd), True) == p, 'directory descriptor changed: ' + str(path))
    if parent is not None:
        need(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True) == p,
             'directory name changed: ' + str(path))
    return fd


def read_image(path, cap=32 * MIB, retain=False):
    parent = directory(path.parent)
    before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
    alias = stat.S_ISLNK(before.st_mode)
    target = Path('/usr/bin/python3.12') if alias and path == Path('/usr/bin/python3') else path
    need(not alias or (target != path and os.readlink(path.name, dir_fd=parent) == 'python3.12'),
         'unadmitted file alias: ' + str(path))
    fd = os.open(target.name, OF, dir_fd=parent)
    try:
        first = os.fstat(fd)
        need(stat.S_ISREG(first.st_mode) and first.st_uid == 0 and first.st_nlink == 1
             and not first.st_mode & 0o022 and first.st_size <= cap, 'regular bounded input: ' + str(path))
        digest, data, size = hashlib.sha256(), bytearray(), 0
        while chunk := os.read(fd, 65536):
            size += len(chunk)
            need(size <= cap and time.monotonic() - START < 120, 'read cap/deadline')
            digest.update(chunk)
            if retain:
                data.extend(chunk)
        need(size == first.st_size and pin(first) == pin(os.fstat(fd))
             == pin(os.stat(target.name, dir_fd=parent, follow_symlinks=False)), 'changed image: ' + str(path))
        need(pin(before) == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)),
             'changed image name: ' + str(path))
        result = {'pin': pin(first), 'sha256': digest.hexdigest()}
        if alias:
            result.update(link_pin=pin(before), link_target='python3.12')
        return bytes(data), result
    finally:
        os.close(fd)


def absent(path):
    try:
        parent = directory(path.parent)
        os.stat(path.name, dir_fd=parent, follow_symlinks=False)
    except FileNotFoundError:
        return
    raise RuntimeError('required absent path present: ' + str(path))


def resources():
    fd = os.open('/proc/meminfo', OF)
    try:
        raw = os.read(fd, 32769)
        need(len(raw) <= 32768, 'meminfo cap')
    finally:
        os.close(fd)
    memory = {}
    for line in raw.splitlines():
        fields = line.split()
        if fields and fields[0] in (b'MemTotal:', b'MemAvailable:'):
            need(len(fields) == 3 and fields[2] == b'kB', 'memory units')
            memory[fields[0].decode().rstrip(':')] = int(fields[1]) * 1024
    need(set(memory) == {'MemTotal', 'MemAvailable'} and memory['MemTotal'] > 0,
         'memory availability not established')
    disk = {str(p): os.fstatvfs(directory(p)).f_bavail * os.fstatvfs(directory(p)).f_frsize
            for p in (W.parent, B / 'runs', T)}
    value = {'utc': datetime.now(timezone.utc).isoformat(), 'memory_bytes': memory, 'free_bytes': disk}
    out['resources'].append(value)
    need(min(disk.values()) >= 12 * 1024 ** 3 and memory['MemAvailable'] >= memory['MemTotal'] / 4,
         '12GiB/25percent launch floors unavailable')


lock_fd = None
code = 70
try:
    for s in SIGNALS:
        signal.signal(s, expired)
    signal.alarm(120)
    out['self_image'] = read_image(SELF, 65536)[1]
    lock_parent = directory(LOCK.parent)
    lock_fd = os.open(LOCK.name, OF, dir_fd=lock_parent)
    need(pin(os.fstat(lock_fd)) == pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False))
         == ORIGINAL_LOCK, 'original existing lock mismatch; never recreate')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    out['original_lock'] = ORIGINAL_LOCK
    resources()
    requests, expected = {}, {}
    for name, sha in REQUESTS.items():
        data, image = read_image(B / 'requests' / name, 65536, True)
        need(image['sha256'] == sha, 'historical request changed')
        q = json.loads(data)
        need(q['lock'] == ORIGINAL_LOCK, 'historical lock mismatch')
        requests[name] = q
        for path, image in q['images'].items():
            need(path not in expected or expected[path] == image, 'historical image disagreement')
            expected[path] = image
    out['request_sha256'] = REQUESTS
    for path, wanted in expected.items():
        image = read_image(Path(path))[1]
        need(image == wanted, 'regular image changed; no automatic rebasing: ' + path)
        out['images'][path] = image
    for path in DIRECTORIES:
        out['directories'][str(path)] = pin(os.fstat(directory(path)), True)
    out['parent_namespaces'] = {n: os.readlink('/proc/self/ns/' + n) for n in ('mnt', 'pid')}
    out['prior_parent_namespaces'] = requests['LINUX-ANDROID-COMPILE-02.json']['parent_namespaces']
    # Existing direct aliases are checked, not resolved through arbitrary chains.
    out['tool_aliases'] = {}
    for name, wanted in requests['LINUX-ANDROID-COMPILE-02.json']['tool_aliases'].items():
        path = Path(name)
        parent = directory(path.parent)
        p = pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
        target = os.readlink(path.name, dir_fd=parent) if stat.S_ISLNK(p['mode']) else None
        observed = {'lstat': p, 'readlink': target, 'resolved_image': wanted['resolved_image']}
        need(observed == wanted and p == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)),
             'tool alias changed')
        out['tool_aliases'][name] = observed
    checks = ABSENT + tuple(G / p for p in REDIRECTS)
    for path in checks:
        absent(path)
        out['absences'].append(str(path))
    resources()
    for path in checks:
        absent(path)
    for path in tuple(fds):
        directory(path)
    need(pin(os.fstat(lock_fd)) == pin(os.stat(LOCK.name, dir_fd=lock_parent, follow_symlinks=False))
         == ORIGINAL_LOCK, 'lock changed during observation')
    need(out['parent_namespaces'] == {n: os.readlink('/proc/self/ns/' + n) for n in ('mnt', 'pid')},
         'caller namespace changed')
    out['status'] = 'CURRENT_HOST_FACTS_CAPTURED_NOT_EXECUTION_ADMISSION'
    code = 0
except BaseException as error:
    out['error'] = type(error).__name__ + ':' + str(error)[:1024]
    out['status'], code = 'HOLD', 70
finally:
    # No child or temporary output exists. Close the lock and every read descriptor
    # even after interruption/failure; an error remains HOLD, never a retry permit.
    errors = []
    if lock_fd is not None:
        try:
            os.close(lock_fd)
        except OSError as error:
            errors.append(str(error))
    for fd, unused_pin in reversed(tuple(fds.values())):
        try:
            os.close(fd)
        except OSError as error:
            errors.append(str(error))
    out['closed_descriptors'] = not errors
    if errors:
        out['close_errors'], out['status'], code = errors, 'HOLD', 70
    # Terminal commit boundary, after all close attempts: block handled signals
    # through process exit, incorporating already latched/pending cancellation.
    # Signals arriving after this boundary cannot retroactively change a receipt.
    signal.pthread_sigmask(signal.SIG_BLOCK, SIGNALS)
    signal.alarm(0)
    if CANCELLED or set(signal.sigpending()).intersection(SIGNALS) or time.monotonic() - START >= 120:
        out['error'], out['status'], code = 'cancelled/deadline before terminal commit', 'HOLD', 70
    out['elapsed_seconds'] = round(time.monotonic() - START, 3)
    print(json.dumps(out, sort_keys=True))
sys.exit(code)
