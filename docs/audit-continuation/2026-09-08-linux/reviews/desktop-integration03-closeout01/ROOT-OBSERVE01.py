#!/usr/bin/python3.12
"""Fresh read-only current-GUI03 metadata observation, not cleanup authority.

Inert until exact independent source acceptance and root's quiescent invocation.
Never imports helpers, descends R, reads argv/env/private file contents, creates
processes, sends signals, or mutates the lock/slot/runtime. All observations are
bounded points under a cooperative writer freeze, not a hostile-host guarantee.
"""
import errno
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import select
import stat
import sys
import time

SELF = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/desktop-integration03-closeout01/ROOT-OBSERVE01.py')
B = SELF.parents[2]
R = Path('/root/projects/PassVault/audit-runtime-linux-desktop-integration03')
LOCK = R.parent / '.audit-coordination-linux-20260908/build.lock'
SLOT = B / 'EXECUTION_SLOT.json'
OLD_NS = {'mnt:[4026532115]', 'pid:[4026532116]'}
PARENT_NS = {'mnt': 'mnt:[4026531841]', 'net': 'net:[4026531840]', 'pid': 'pid:[4026531836]'}
LOCK_PIN = (24, 14189001, 0, 33152, 1, 0, 1788910891124735946, 1788910891124735946)
ROOT_PIN = (23, 1552647, 0, 16832)
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
BUILDLIKE = {'java', 'javac', 'gradle', 'kotlinc', 'kotlin', 'xcodebuild', 'cmake', 'ninja',
             'make', 'cc1', 'cc1plus', 'clang', 'clang++', 'gcc', 'g++', 'adb', 'emulator',
             'qemu-system-x86', 'qemu-system-aar', 'xvfb', 'openbox', 'ccache', 'sccache',
             'cargo', 'rustc', 'dotnet', 'msbuild', 'go', 'swiftc', 'swift-frontend', 'ld', 'ld.lld', 'lld'}
START = time.monotonic()
TOTAL = {'processes': 0, 'threads': 0, 'fd_links': 0, 'map_bytes': 0, 'dead_pidfds': 0}
OBSERVED, THREADS = {}, {}
RESULT = {'format': 'passvault-gui03-closeout-observation-v1', 'status': 'HOLD',
          'runtime_path': str(R), 'unclassified': [], 'conflicts': [], 'references': [],
          'scope': 'Bounded current visible PID/task namespace/pid_for_children/cwd/root/exe/fd/maps metadata; separate self context check. Finite build-name screen plus external cooperative no-build/CI attestation, not standalone no-build proof. Current-parent mount alias coverage only. No argv/env/referenced-file contents, signals, R descent, ancestor-capability, hidden-alias, global/continuous or hostile-host exclusion.'}


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def check():
    require(time.monotonic() - START < 90, '90s cooperative observation limit')


def pin(s):
    return (s.st_dev, s.st_ino, s.st_uid, s.st_mode, s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns)


def retain(kind, value):
    require(sum(len(RESULT[key]) for key in ('unclassified', 'conflicts', 'references')) < 64,
            'bounded issue-list overflow: HOLD')
    RESULT[kind].append(value)


def slot_image():
    before = pin(SLOT.lstat())
    require(stat.S_ISREG(before[3]) and before[2] == 0 and before[4] == 1
            and not before[3] & 0o7022, 'safe slot metadata')
    fd = os.open(SLOT, os.O_RDONLY | os.O_CLOEXEC | os.O_NOFOLLOW | os.O_NONBLOCK)
    try:
        require(pin(os.fstat(fd)) == before, 'slot open drift')
        raw = read_fd(fd, 1024 ** 2)
        require(pin(os.fstat(fd)) == pin(SLOT.lstat()) == before and len(raw) == before[5], 'slot read drift')
        return raw, before
    finally:
        os.close(fd)


def decode(raw):
    def unique(pairs):
        value = {}
        for key, item in pairs:
            require(key not in value, 'duplicate JSON key')
            value[key] = item
        return value
    return json.loads(raw, object_pairs_hook=unique, parse_constant=lambda _: require(False, 'nonfinite JSON'))


def read_fd(fd, cap):
    data = bytearray()
    while block := os.read(fd, min(65536, cap + 1 - len(data))):
        check()
        data.extend(block)
        require(len(data) <= cap, 'metadata byte cap')
    return bytes(data)


def read(path, cap):
    check()
    fd = os.open(path, os.O_RDONLY | os.O_CLOEXEC | os.O_NOFOLLOW | os.O_NONBLOCK)
    try:
        return read_fd(fd, cap)
    finally:
        os.close(fd)


def ids(path, cap):
    check()
    found = []
    with os.scandir(path) as entries:
        for entry in entries:
            if entry.name.isdecimal():
                found.append(int(entry.name))
                require(len(found) <= cap, 'metadata member cap')
    return sorted(found)


def birth(path):
    raw = read(path / 'stat', 65536)
    split = raw.rfind(b') ')
    require(split > 0, 'proc stat grammar')
    fields = raw[split + 2:].split()
    require(len(fields) >= 20 and fields[0] in (b'R', b'S', b'D', b'Z', b'T', b't', b'X', b'I', b'P', b'W'), 'proc stat fields')
    return int(raw.split(b' ', 1)[0]), int(fields[1]), int(fields[19]), fields[0].decode('ascii')


def same_birth(first, last):
    return first[:3] == last[:3]


def link(path, pid, tid, kind):
    check()
    value = os.readlink(path)
    require(len(value.encode('utf-8', 'surrogateescape')) <= 16384, 'link metadata cap')
    canonical = value[:-10] if value.endswith(' (deleted)') else value
    if canonical in OLD_NS or canonical == str(R) or canonical.startswith(str(R) + '/'):
        retain('references', {'pid': pid, 'tid': tid, 'kind': kind})
    return value


def departed(fd):
    poll = select.poll()
    poll.register(fd, select.POLLIN | select.POLLHUP | select.POLLERR)
    events = poll.poll(0)
    require(not any(flags & (select.POLLERR | select.POLLNVAL) for _, flags in events), 'pidfd poll uncertainty')
    return bool(events)


def observe(pid):
    check()
    path = Path('/proc') / str(pid)
    pfd = None
    try:
        pfd = os.pidfd_open(pid, 0)
        if departed(pfd):
            TOTAL['dead_pidfds'] += 1
            return
        first = birth(path)
        require(first[0] == pid, 'PID identity')
        comm = read(path / 'comm', 4096).rstrip(b'\n').decode('utf-8', 'strict')
        if comm.casefold() in BUILDLIKE or comm.casefold().startswith(('gradle', 'kotlinc', 'qemu-system', 'clang-', 'gcc-', 'g++-', 'ninja')):
            retain('conflicts', {'pid': pid, 'comm': comm, 'start': first[2], 'ownership': 'NOT_ADOPTED'})
        tids = ids(path / 'task', 2048)
        require(tids and pid in tids, 'complete thread group')
        thread_births = {}
        for tid in tids:
            TOTAL['threads'] += 1
            require(TOTAL['threads'] <= 20000, 'total thread cap')
            thread = path / 'task' / str(tid)
            before = birth(thread)
            require(before[0] == tid, 'TID identity')
            thread_births[tid] = before[:3]
            ns = {kind: link(thread / 'ns' / kind, pid, tid, 'namespace:' + kind) for kind in ('mnt', 'pid', 'pid_for_children', 'net')}
            paths = {kind: link(thread / kind, pid, tid, kind) for kind in ('cwd', 'root', 'exe')}
            fds = ids(thread / 'fd', 16384)
            fd_links = {}
            for fd in fds:
                TOTAL['fd_links'] += 1
                require(TOTAL['fd_links'] <= 200000, 'total descriptor cap')
                fd_links[fd] = link(thread / 'fd' / str(fd), pid, tid, 'fd')
            require(ids(thread / 'fd', 16384) == fds, 'thread descriptor churn')
            require({fd: link(thread / 'fd' / str(fd), pid, tid, 'fd-recheck') for fd in fds} == fd_links
                    and {kind: link(thread / kind, pid, tid, kind + '-recheck') for kind in paths} == paths
                    and {kind: os.readlink(thread / 'ns' / kind) for kind in ns} == ns
                    and same_birth(before, birth(thread)), 'thread namespace/birth churn')
        maps = read(path / 'maps', 8 * 1024 ** 2)
        TOTAL['map_bytes'] += len(maps)
        require(TOTAL['map_bytes'] <= 256 * 1024 ** 2 and (not maps or maps.endswith(b'\n')), 'maps size/completeness')
        for row in maps.splitlines():
            fields = row.split(None, 5)
            require(len(fields) >= 5, 'maps row grammar')
            if len(fields) == 6:
                name = fields[5]
                if name.endswith(b' (deleted)'):
                    name = name[:-10]
                if name == os.fsencode(R) or name.startswith(os.fsencode(R) + b'/'):
                    retain('references', {'pid': pid, 'kind': 'mapped-runtime-file'})
        require(ids(path / 'task', 2048) == tids and same_birth(first, birth(path)), 'process/thread-group churn')
        OBSERVED[pid], THREADS[pid] = first[:3], thread_births
        if departed(pfd):
            TOTAL['dead_pidfds'] += 1
        TOTAL['processes'] += 1
    except (OSError, RuntimeError, ValueError, UnicodeError) as error:
        # Terminal departure can classify only a missing proc entry, never erase
        # malformed/cap/deadline/identity/churn observations.
        if isinstance(error, OSError) and error.errno in (errno.ENOENT, errno.ESRCH) and pfd is not None and departed(pfd):
            TOTAL['dead_pidfds'] += 1
        else:
            retain('unclassified', {'pid': pid, 'error_type': type(error).__name__, 'errno': getattr(error, 'errno', None),
                                    'reason': str(error) if isinstance(error, RuntimeError) else 'metadata access/parse uncertainty'})
    finally:
        if pfd is not None:
            os.close(pfd)


def final_birth_check(pid):
    pfd = None
    try:
        pfd = os.pidfd_open(pid, 0)
        if departed(pfd):
            return
        path = Path('/proc') / str(pid)
        before = birth(path)
        require(before[:3] == OBSERVED.get(pid), 'final after-only/reused/unobserved live PID')
        current = {tid: birth(path / 'task' / str(tid))[:3] for tid in ids(path / 'task', 2048)}
        require(current == THREADS.get(pid) and same_birth(before, birth(path)), 'final thread birth churn')
    except (OSError, RuntimeError, ValueError, UnicodeError) as error:
        if not (isinstance(error, OSError) and error.errno in (errno.ENOENT, errno.ESRCH)
                and pfd is not None and departed(pfd)):
            retain('unclassified', {'pid': pid, 'reason': str(error) if isinstance(error, RuntimeError)
                                    else 'final identity access/parse uncertainty'})
    finally:
        if pfd is not None:
            os.close(pfd)


def mounts():
    raw = read('/proc/self/mountinfo', 4 * 1024 ** 2)
    require(raw and raw.endswith(b'\n'), 'complete mountinfo')
    entries = []
    for row in raw.splitlines():
        halves = row.split(b' - ')
        require(len(halves) == 2 and len(halves[0].split()) >= 6, 'mountinfo prefix')
        suffix = halves[1].split(b' ')
        require(len(suffix) == 3 and suffix[0] and suffix[2], 'mountinfo suffix')
        left, decoded_paths = halves[0].split(), []
        for field in (left[3], left[4]):
            require(not re.search(rb'\\(?![0-7]{3})', field), 'mountinfo escape')
            decoded = re.sub(rb'\\([0-7]{3})', lambda match: bytes([int(match[1], 8)]), field)
            require(decoded.startswith(b'/') and b'\0' not in decoded and not decoded.startswith(b'//')
                    and not {b'.', b'..'}.intersection(decoded.split(b'/')), 'mount path')
            decoded_paths.append(decoded)
        root, point = decoded_paths
        require(point != os.fsencode(R) and not point.startswith(os.fsencode(R) + b'/'), 'mount at/below runtime')
        entries.append((left[0], left[2], suffix[0], root, point))
    runtime = os.fsencode(R)
    containing = [row for row in entries if row[4] == b'/' or runtime.startswith(row[4] + b'/')]
    require(containing, 'containing mount missing')
    longest = max(len(row[4]) for row in containing)
    bases = [row for row in containing if len(row[4]) == longest]
    require(len(bases) == 1, 'ambiguous containing mount')
    base = bases[0]
    internal = base[3].rstrip(b'/') + b'/' + runtime[len(base[4].rstrip(b'/')):].lstrip(b'/')
    for row in entries:
        if row[0] == base[0] or row[1:3] != base[1:3]:
            continue
        root = row[3].rstrip(b'/')
        if internal == root or internal.startswith(root + b'/'):
            alias = row[4].rstrip(b'/') + b'/' + internal[len(root):].lstrip(b'/')
            require(alias == runtime, 'visible containing-mount-relative R alias')
        require(not root.startswith(internal + b'/'), 'visible mounted runtime descendant alias')
    return hashlib.sha256(raw).hexdigest()


def self_context():
    own = os.getpid()
    require(ids('/proc/self/task', 2048) == [own], 'reader unexpectedly multithreaded')
    require(os.readlink('/proc/self/cwd') == str(B.parents[2]), 'observer CWD is not W')
    for kind in ('cwd', 'root', 'exe'):
        link('/proc/self/' + kind, own, own, 'self-' + kind)
    for kind in ('mnt', 'pid', 'pid_for_children', 'net'):
        link('/proc/self/ns/' + kind, own, own, 'self-namespace:' + kind)
    # Inspect inherited fds while scandir's own proc-directory fd is still live.
    count = 0
    with os.scandir('/proc/self/fd') as entries:
        for entry in entries:
            require(entry.name.isdecimal(), 'self descriptor grammar')
            count += 1
            require(count <= 64, 'inherited descriptor cap')
            link('/proc/self/fd/' + entry.name, own, own, 'self-inherited-fd')
    RESULT['self_context'] = {'pid': own, 'single_thread': True, 'cwd_is_W': True, 'descriptor_links_checked': count}


def main():
    require(sys.argv == [str(SELF)] and sys.executable == '/usr/bin/python3.12' and sys.flags.isolated
            and sys.flags.no_site and sys.dont_write_bytecode and not sys.flags.optimize
            and os.getuid() == os.geteuid() == 0 and dict(os.environ) == ENV, 'fixed isolated observation entry')
    expected = {R: ROOT_PIN, R.parent: (23, 498323, 0, 16877), LOCK.parent: (23, 661121, 0, 16832)}
    for path, value in expected.items():
        require(pin(path.lstat())[:4] == value, 'original root/parent identity')
    raw, slot_pin = slot_image()
    require(decode(raw)['state'] == 'IDLE_NO_AUDIT_BUILD_ADMITTED', 'root idle slot not established')
    lock = os.open(LOCK, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC)
    try:
        require(pin(os.fstat(lock)) == pin(LOCK.lstat()) == LOCK_PIN, 'original lock identity')
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        before_mount = mounts()
        before_ns = {key: os.readlink('/proc/self/ns/' + key) for key in ('mnt', 'pid', 'net')}
        require(before_ns == PARENT_NS, 'observer not in original parent namespace tuple')
        self_context()
        own = os.getpid()
        original = set(ids('/proc', 5000)) - {own}
        for pid in sorted(original):
            observe(pid)
        second = set(ids('/proc', 5000)) - {own}
        for pid in sorted(second - original):
            observe(pid)
        final = set(ids('/proc', 5000)) - {own}
        for pid in sorted(final):
            final_birth_check(pid)
        for pid in sorted(set(ids('/proc', 5000)) - final - {own}):
            retain('unclassified', {'pid': pid, 'reason': 'after-only process beyond bounded final identity check'})
        require(mounts() == before_mount and {key: os.readlink('/proc/self/ns/' + key) for key in before_ns} == before_ns,
                'parent namespace/mount changed')
        require(slot_image() == (raw, slot_pin), 'slot changed')
        require(pin(R.lstat())[:4] == ROOT_PIN and pin(os.fstat(lock)) == pin(LOCK.lstat()) == LOCK_PIN, 'original runtime/lock changed')
        rows = re.findall(rb'^(MemTotal|MemAvailable):\s+(\d+) kB$', read('/proc/meminfo', 65536), re.M)
        require(len(rows) == 2 and {row[0] for row in rows} == {b'MemTotal', b'MemAvailable'}, 'unique memory rows')
        mem = dict(rows)
        total, available = int(mem[b'MemTotal']) * 1024, int(mem[b'MemAvailable']) * 1024
        fs = os.statvfs(R.parent); disk = fs.f_bavail * fs.f_frsize
        require(0 < total and 0 <= available <= total and disk >= 12 * 1024 ** 3 and available * 100 >= total * 25, 'resource floor')
        check()
        RESULT.update(observed_monotonic_ns=time.monotonic_ns(), parent_namespaces=before_ns, parent_mountinfo_sha256=before_mount,
                      original_runtime_pin=ROOT_PIN, original_lock_pin=LOCK_PIN, slot_sha256=hashlib.sha256(raw).hexdigest(),
                      resources={'disk_available_bytes': disk, 'memory_available_bytes': available, 'memory_total_bytes': total})
        require(not RESULT['conflicts'] and not RESULT['references'] and not RESULT['unclassified'], 'reference/conflict/uncertainty HOLD')
        RESULT['status'] = 'BOUNDED_CURRENT_REFERENCE_OBSERVATION_CLEAR_NOT_CLEANUP_AUTHORITY'
    finally:
        os.close(lock)


if __name__ == '__main__':
    code = 1
    try:
        main()
        check()
        code = 0
    except BaseException as error:
        RESULT.update(status='HOLD', error_type=type(error).__name__, error=str(error)[:500])
    RESULT.update(totals=TOTAL, elapsed_seconds=round(time.monotonic() - START, 3), additional_stops=0,
                  mutation_or_signal=False, qualification='Actual tool exit and cooperative freeze/CI/agent attestations remain separate; any uncertainty blocks closeout admission.')
    print(json.dumps(RESULT, sort_keys=True, separators=(',', ':'), allow_nan=False))
    sys.exit(code)
