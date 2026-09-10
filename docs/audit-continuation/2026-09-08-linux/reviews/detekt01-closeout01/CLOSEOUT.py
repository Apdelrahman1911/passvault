#!/usr/bin/python3.12
"""Current Detekt01 generated-only closeout; source-only until independently admitted.

No subprocess, Gradle/stop, signals to processes, imports of prior helpers, Git,
mount changes or old-runtime access. Original source/blob/index remainder stays.
Reacquired descriptors are NEW; cooperative freeze, not hostile-root isolation.
"""
import datetime
import fcntl
import hashlib
import json
import os
from pathlib import Path
import signal
import stat
import sys
import time

B = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux')
D = B / 'reviews/detekt01-closeout01'
SELF, REQUEST, APPROVAL = D / 'CLOSEOUT.py', D / 'REQUEST.json', D / 'ACCEPT.json'
R = Path('/root/projects/PassVault/audit-runtime-linux-detekt01')
E = B / 'runs/linux-detekt01'
OUT = E / 'closeout01'
LOCK = Path('/root/projects/PassVault/.audit-coordination-linux-20260908/build.lock')
SLOT = B / 'EXECUTION_SLOT.json'
SOURCE = B / 'reviews/desktop-integration03/source-prepare01/SOURCE.json'
PURPOSE = 'ONE_CURRENT_DETEKT01_GENERATED_CLOSEOUT01'
MIB, MAX_ENTRIES, MAX_INVENTORY = 1024 ** 2, 40000, 16 * 1024 ** 2
ODIR = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
ROOTS = '''home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state
checkout/.gradle checkout/.kotlin checkout/build checkout/app-android/build checkout/app-desktop/build
checkout/shared/build checkout/core/build checkout/feature/build checkout/core/domain/build
checkout/core/database/build checkout/core/crypto/build checkout/core/security/build
checkout/core/designsystem/build checkout/core/navigation/build checkout/core/otp/build checkout/core/testing/build
checkout/feature/onboarding/build checkout/feature/unlock/build checkout/feature/vault/build
checkout/feature/credential/build checkout/feature/generator/build checkout/feature/health/build
checkout/feature/settings/build checkout/feature/backup/build'''.split()
REPORTS = {
    'app-android/build/reports/detekt/detekt.xml': 'app-android-detekt.xml',
    'app-android/build/reports/detekt/detekt.html': 'app-android-detekt.html',
    'app-android/build/reports/detekt/detekt.sarif': 'app-android-detekt.sarif',
    'build/reports/problems/problems-report.html': 'problems-report.html',
}
PINS = {
    R: (23, 1304148, 0, stat.S_IFDIR | 0o700),
    R.parent: (23, 498323, 0, stat.S_IFDIR | 0o755),
    E: (23, 1304147, 0, stat.S_IFDIR | 0o700),
    E.parent: (23, 642474, 0, stat.S_IFDIR | 0o700),
}
INPUTS = {
    E / 'OUTER-ALLOCATION.json': '52216e32746c8614ee15d078274c505d31f3d689bb0974b0b958107fc15892f8',
    E / 'OUTER-RECEIPT.json': '6fa674ea620303612d0f71290a1210368dcc1bae98ac4be0c62e1b88edf3caa6',
    E / 'INNER-RESULT.json': 'aa8813acebfc2d9308926d179845a37303f85ef7652cf0c97aee8849883cfc46',
    E / 'SOURCE-BEFORE.json': 'a71df23efdc330470cf1c71ce34e5344893473da0efa6506d5d4f699352237bc',
    E / 'SOURCE-AFTER.json': 'a71df23efdc330470cf1c71ce34e5344893473da0efa6506d5d4f699352237bc',
    E / 'logs/detekt.log': '13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218',
    SOURCE: '1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23',
    B / 'reviews/detekt01/c15-preflight/EXTERNAL-RESULT.json':
        'e7ffb842fa14d6dfc8ef971a2f647594d563a6f3f1fc6fb776e64e0d5e37beaf',
    B / 'reviews/verification/LINUX-DETEKT01-C15-ACTUAL-RESULT-REVIEW.json':
        '83f5c80bf1934177c7de042b88d327da46929d3b9de21198f918dfb2b813ca05',
    B / 'reviews/detekt01/c15-preflight/SLOT-RELEASE.json':
        'dfb683efea321a591e51aa4aeb5ad391fda47e964a2510a65724bae1a3c0530f',
}
START, CANCELLED = time.monotonic(), False
DIRECTORIES, INVENTORY, CHILDREN = {}, {}, {}
inventory_bytes, last_resource = 3, 0.0
lock_fd, receipt_fd = None, None
record = {'schema': 1, 'purpose': PURPOSE, 'status': 'HOLD', 'reports': [], 'roots': [],
          'removed_files': 0, 'removed_directories': 0, 'removed_logical_bytes': 0,
          'source_remainder': 'UNTOUCHED_HELD', 'builds_tests_stops': 0, 'resources': []}


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def check():
    require(not CANCELLED and time.monotonic() - START < 300, 'cancelled/300s deadline')
    if time.monotonic() - last_resource >= 1:
        resources()


def resources(launch=False):
    global last_resource
    last_resource = time.monotonic()
    fs = os.statvfs(R.parent)
    with open('/proc/meminfo', 'rb') as stream:
        raw = stream.read(65537)
    require(len(raw) <= 65536, 'meminfo bound')
    memory = {line.split(b':', 1)[0]: int(line.split()[1]) * 1024
              for line in raw.splitlines() if line.startswith((b'MemTotal:', b'MemAvailable:'))}
    free, available, total = fs.f_bavail * fs.f_frsize, memory[b'MemAvailable'], memory[b'MemTotal']
    require(len(record['resources']) < 305, 'resource observation bound')
    record['resources'].append({'elapsed_seconds': round(last_resource - START, 3),
                                'disk_available_bytes': free, 'memory_available_bytes': available,
                                'memory_total_bytes': total, 'launch': launch})
    require(free >= (12 if launch else 8) * 1024 ** 3 and
            available * 100 >= total * (25 if launch else 20), 'resource floor')


def dir_pin(s):
    return s.st_dev, s.st_ino, s.st_uid, s.st_mode


def file_pin(s):
    return dir_pin(s) + (s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns)


def encode(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), allow_nan=False).encode() + b'\n'


def directory(path):
    """Hold newly acquired parents; reject named replacement before each use."""
    check()
    parent = None if path == Path('/') else directory(path.parent)
    if path not in DIRECTORIES:
        fd = os.open('/' if parent is None else path.name, ODIR, dir_fd=parent)
        try:
            pin = dir_pin(os.fstat(fd))
            require(pin[2] == 0 and not pin[3] & 0o022, 'unsafe directory owner/mode')
            if path in PINS:
                require(pin == PINS[path], 'original root/parent identity mismatch')
            DIRECTORIES[path] = (fd, pin)
        except BaseException:
            os.close(fd)
            raise
    fd, pin = DIRECTORIES[path]
    require(dir_pin(os.fstat(fd)) == pin and (parent is None or
            dir_pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)) == pin), 'directory replaced')
    return fd


def read(path, cap):
    parent = directory(path.parent)
    before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
    require(stat.S_ISREG(before.st_mode) and before.st_uid == 0 and before.st_nlink == 1
            and not before.st_mode & 0o022 and before.st_size <= cap, 'unsafe bounded regular input')
    require(R not in path.parents or before.st_dev == 24, 'runtime report device drift')
    fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
    try:
        require(file_pin(os.fstat(fd)) == file_pin(before), 'input changed at open')
        data = bytearray()
        while part := os.read(fd, 65536):
            check()
            data.extend(part)
            require(len(data) <= cap, 'read cap')
        require(len(data) == before.st_size and file_pin(os.fstat(fd)) == file_pin(before)
                == file_pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input drift')
        return bytes(data), file_pin(before)
    finally:
        os.close(fd)


def retain(path, raw):
    parent = directory(path.parent)
    fd = os.open(path.name, os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                 0o600, dir_fd=parent)
    try:
        at = 0
        while at < len(raw):
            check()
            written = os.write(fd, raw[at:])
            require(written > 0, 'short retained write')
            at += written
        os.fsync(fd)
        pin = file_pin(os.fstat(fd))
        os.lseek(fd, 0, os.SEEK_SET)
        result = bytearray()
        while part := os.read(fd, 65536):
            check()
            result.extend(part)
            require(len(result) <= len(raw), 'retained readback bound')
        require(bytes(result) == raw and file_pin(os.fstat(fd)) == pin
                == file_pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'retained report drift')
    finally:
        os.close(fd)
    os.fsync(parent)
    return {'path': str(path.relative_to(E)), 'bytes': len(raw), 'sha256': hashlib.sha256(raw).hexdigest(),
            'pin': pin}


def flush():
    if receipt_fd is None:
        return
    record['elapsed_seconds'] = round(time.monotonic() - START, 3)
    raw = encode(record)
    os.lseek(receipt_fd, 0, os.SEEK_SET)
    os.ftruncate(receipt_fd, 0)
    at = 0
    while at < len(raw):
        written = os.write(receipt_fd, raw[at:])
        require(written > 0, 'short receipt write')
        at += written
    os.fsync(receipt_fd)


def mount_guard():
    check()
    with open('/proc/self/mountinfo', 'rb') as stream:
        raw = stream.read(4 * MIB + 1)
    require(len(raw) <= 4 * MIB, 'mount table bound')
    for line in raw.splitlines():
        pieces = line.split(b' - ')
        # SOURCE can legitimately be empty; splitting whitespace would erase it.
        require(len(pieces) == 2 and len(pieces[0].split()) >= 6 and len(pieces[1].split(b' ')) == 3
                and pieces[1].split(b' ')[0] and pieces[1].split(b' ')[2],
                'mountinfo grammar')
        point = pieces[0].split()[4]
        for escaped, byte in ((b'\\040', b' '), (b'\\011', b'\t'), (b'\\012', b'\n'), (b'\\134', b'\\')):
            point = point.replace(escaped, byte)
        require(point != os.fsencode(R) and not point.startswith(os.fsencode(R) + b'/'), 'runtime mount HOLD')
    return {'sha256': hashlib.sha256(raw).hexdigest(), 'mount_rows': len(raw.splitlines()), 'runtime_mounts': 0}


def names(fd):
    result = []
    with os.scandir(fd) as entries:
        for entry in entries:
            check()
            require(len(result) < MAX_ENTRIES, 'directory entry bound')
            result.append(entry.name)
    return sorted(result)


def add_inventory(relative, kind, pin):
    global inventory_bytes
    require(relative not in INVENTORY and len(INVENTORY) < MAX_ENTRIES, 'duplicate/count inventory bound')
    value = (kind, pin)
    inventory_bytes += len(encode(relative)) + len(encode(value))
    require(inventory_bytes <= MAX_INVENTORY, 'incremental inventory byte bound')
    INVENTORY[relative] = value


def snapshot(fd, relative, depth=0):
    check()
    require(depth <= 64 and len(INVENTORY) < MAX_ENTRIES, 'inventory depth/count bound')
    here = os.fstat(fd)
    require(here.st_dev == 23 and here.st_uid == 0 and not here.st_mode & 0o022, 'directory type/device/owner')
    add_inventory(relative, 'd', dir_pin(here))
    CHILDREN[relative] = names(fd)
    for name in CHILDREN[relative]:
        check()
        child = relative + '/' + name
        require(name not in ('.', '..') and '/' not in name and '\n' not in name and len(child) <= 8192,
                'bounded entry name')
        require(len(INVENTORY) < MAX_ENTRIES, 'inventory count bound')
        s = os.stat(name, dir_fd=fd, follow_symlinks=False)
        require(s.st_uid == 0 and not s.st_mode & 0o022, 'entry owner/mode')
        if stat.S_ISDIR(s.st_mode):
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                require(dir_pin(os.fstat(sub)) == dir_pin(s), 'snapshot directory drift')
                snapshot(sub, child, depth + 1)
                require(dir_pin(os.stat(name, dir_fd=fd, follow_symlinks=False)) == dir_pin(s), 'directory name drift')
            finally:
                os.close(sub)
        else:
            require(stat.S_ISREG(s.st_mode) and s.st_dev == 24 and s.st_nlink == 1, 'nonregular/link/type/device HOLD')
            parts = Path(child).parts
            is_report = 'reports' in parts or 'test-results' in parts or name.startswith('TEST-')
            require(not is_report or child in {'checkout/' + p for p in REPORTS}, 'unexpected report/test evidence HOLD')
            if is_report:
                original = next(row['original_pin'] for row in record['reports']
                                if child == 'checkout/' + row['source'])
                require(file_pin(s) == original, 'report changed after retention')
            add_inventory(child, 'f', file_pin(s))
    require(names(fd) == CHILDREN[relative], 'snapshot membership drift')


def remove(fd, relative):
    check()
    require(dir_pin(os.fstat(fd)) == INVENTORY[relative][1], 'original selected directory changed')
    expected = CHILDREN[relative]
    require(names(fd) == expected, 'inventory membership changed')
    for name in expected:
        check()
        child = relative + '/' + name
        kind, pin = INVENTORY[child]
        s = os.stat(name, dir_fd=fd, follow_symlinks=False)
        if kind == 'd':
            require(dir_pin(s) == pin, 'original directory changed')
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                remove(sub, child)
                require(not names(sub) and dir_pin(os.fstat(sub)) == pin
                        == dir_pin(os.stat(name, dir_fd=fd, follow_symlinks=False)), 'directory removal identity')
            finally:
                os.close(sub)
            os.rmdir(name, dir_fd=fd)
            record['removed_directories'] += 1
        else:
            require(file_pin(s) == pin, 'original regular file changed')
            os.unlink(name, dir_fd=fd)
            record['removed_files'] += 1
            record['removed_logical_bytes'] += pin[5]
        if (record['removed_files'] + record['removed_directories']) % 256 == 0:
            flush()
    os.fsync(fd)


def main():
    global lock_fd, receipt_fd
    require(sys.argv == [str(SELF)] and sys.executable == '/usr/bin/python3.12' and sys.flags.isolated
            and sys.flags.no_site and sys.dont_write_bytecode and not sys.flags.optimize
            and os.getuid() == os.geteuid() == 0, 'fixed isolated root entry')
    resources(launch=True)
    request_raw, _ = read(REQUEST, MIB)
    approval_raw, _ = read(APPROVAL, MIB)
    request, approval = json.loads(request_raw), json.loads(approval_raw)
    self_raw, _ = read(SELF, MIB)
    require(request['purpose'] == PURPOSE and request['source_sha256'] == hashlib.sha256(self_raw).hexdigest()
            and approval == {'purpose': PURPOSE, 'reviewer': '/root/storage', 'disposition': 'ACCEPT_EXACT_ONCE',
                             'request_sha256': hashlib.sha256(request_raw).hexdigest(),
                             'source_sha256': request['source_sha256']}, 'missing/stale exact independent approval')
    require(request['original_runtime_frozen'] is True and request['sole_owner'] == '/root'
            and request['no_audit_build_or_ci'] is True, 'coordination not admitted')
    require({k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')} == request['parent_namespaces'],
            'fresh parent namespace mismatch')
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(file_pin(os.fstat(lock_fd)) == tuple(request['lock_pin'])
            == file_pin(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock changed')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    slot_raw, _ = read(SLOT, MIB)
    require(hashlib.sha256(slot_raw).hexdigest() == request['slot_sha256']
            and json.loads(slot_raw)['state'] == 'IDLE_NO_AUDIT_BUILD_ADMITTED', 'slot not original idle owner state')
    for path, digest in INPUTS.items():
        raw, _ = read(path, 2 * MIB)
        require(hashlib.sha256(raw).hexdigest() == digest, 'retained input changed: ' + path.name)
        if path == SOURCE:
            source = json.loads(raw)
    require(len(source['files']) == 2757 and len(ROOTS) == len(set(ROOTS)) == 35, 'fixed source/allowlist count')
    paths = [Path(x['path']) for x in source['files']]
    for root in ROOTS:
        if root.startswith('checkout/'):
            prefix = Path(root).relative_to('checkout')
            require(not any(p == prefix or prefix in p.parents or p in prefix.parents for p in paths),
                    'generated root intersects permanent source manifest')
    for path in PINS:
        directory(path)
    record.update(utc=datetime.datetime.now(datetime.timezone.utc).isoformat(), request_sha256=hashlib.sha256(request_raw).hexdigest(),
                  approval_sha256=hashlib.sha256(approval_raw).hexdigest(), source_sha256=request['source_sha256'],
                  original_root_pins={str(p): v for p, v in PINS.items()}, new_descriptor_reacquisition=True,
                  mount_before=mount_guard(), status='RETAINING_REPORTS')
    os.mkdir(OUT.name, 0o700, dir_fd=directory(E))
    directory(OUT)
    receipt_fd = os.open('RESULT.json', os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                         0o600, dir_fd=directory(OUT))
    flush()
    os.fsync(directory(E))
    os.mkdir('reports', 0o700, dir_fd=directory(OUT))
    directory(OUT / 'reports')
    report_bytes = 0
    for source_path, target in REPORTS.items():
        raw, original = read(R / 'checkout' / source_path, 4 * MIB)
        require(raw, 'required report is empty')
        report_bytes += len(raw)
        require(report_bytes <= 16 * MIB, 'report aggregate cap')
        record['reports'].append({'source': source_path, 'original_pin': original,
                                  'retained': retain(OUT / 'reports' / target, raw)})
        flush()
    record['reports_preserved'] = True
    for root in ROOTS:
        path = R / root
        parent = directory(path.parent)
        try:
            initial = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        except FileNotFoundError:
            record['roots'].append({'relative': root, 'state': 'ABSENT'})
            continue
        require(stat.S_ISDIR(initial.st_mode), 'selected root must be directory')
        fd = os.open(path.name, ODIR, dir_fd=parent)
        try:
            require(dir_pin(os.fstat(fd)) == dir_pin(initial), 'selected root changed at open')
            snapshot(fd, root)
            require(dir_pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)) == dir_pin(initial), 'root name drift')
        finally:
            os.close(fd)
        record['roots'].append({'relative': root, 'state': 'SNAPSHOTTED', 'pin': dir_pin(initial)})
    inventory = encode(INVENTORY)
    require(len(inventory) <= MAX_INVENTORY, 'encoded inventory byte bound')
    record['inventory_retained'] = retain(OUT / 'INVENTORY.json', inventory)
    record.update(inventory_entries=len(INVENTORY), inventory_bytes=len(inventory),
                  inventory_sha256=hashlib.sha256(inventory).hexdigest(), mount_before_delete=mount_guard(), status='DELETING_GENERATED_ONLY')
    flush()
    for row in record['roots']:
        if row['state'] == 'ABSENT':
            continue
        path = R / row['relative']
        parent = directory(path.parent)
        require(dir_pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)) == row['pin'], 'selected root replaced')
        fd = os.open(path.name, ODIR, dir_fd=parent)
        try:
            remove(fd, row['relative'])
            require(not names(fd) and dir_pin(os.fstat(fd)) == row['pin']
                    == dir_pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'selected root final binding')
        finally:
            os.close(fd)
        os.rmdir(path.name, dir_fd=parent)
        os.fsync(parent)
        row['state'] = 'REMOVED'
        record['removed_directories'] += 1
        flush()
    record.update(status='GENERATED_ONLY_CLEANED_PENDING_INDEPENDENT_RECONCILIATION', mount_after=mount_guard(),
                  qualification='No Detekt/application PASS. Original failed records remain; source/blob/index/metadata R remainder held. '
                                'Per-entry rechecks under cooperative freeze are not atomic inode-conditional deletion or hostile-root safety.')


if __name__ == '__main__':
    os.umask(0o077)
    def cancel(_number, _frame):
        global CANCELLED
        CANCELLED = True
    for signum in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
        signal.signal(signum, cancel)
    code = 1
    try:
        main()
        code = 0
    except BaseException as error:
        record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', error=type(error).__name__ + ': ' + str(error)[:1200])
    finally:
        # Terminal receipt/close boundary; later signals stay pending until exit.
        signal.pthread_sigmask(signal.SIG_BLOCK, {signal.SIGTERM, signal.SIGINT, signal.SIGHUP})
        if CANCELLED or signal.sigpending() & {signal.SIGTERM, signal.SIGINT, signal.SIGHUP}:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', terminal_cancelled=True)
            code = 1
        if time.monotonic() - START >= 300:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', terminal_deadline=True)
            code = 1
        try:
            flush()
        except BaseException as error:
            record['receipt_error'] = type(error).__name__
            record['status'] = 'HOLD_CONSUMED_NO_AUTOMATIC_RETRY'
            code = 1
        descriptors = [fd for fd, _ in reversed(tuple(DIRECTORIES.values()))]
        descriptors += ([] if lock_fd is None else [lock_fd]) + ([] if receipt_fd is None else [receipt_fd])
        for fd in descriptors:
            try:
                os.close(fd)
            except OSError:
                record['descriptor_close_uncertainty'] = True
                record['status'] = 'HOLD_CONSUMED_NO_AUTOMATIC_RETRY'
                code = 1
        print(encode({'external_pending_code': code, 'status': record['status'], 'error': record.get('error'),
                      'receipt_error': record.get('receipt_error'),
                      'descriptor_close_uncertainty': record.get('descriptor_close_uncertainty', False),
                      'removed_files': record['removed_files'], 'removed_directories': record['removed_directories'],
                      'qualification': 'Final receipt precedes descriptor close and actual tool exit; no automatic retry.'}).decode(), end='')
    sys.exit(code)
