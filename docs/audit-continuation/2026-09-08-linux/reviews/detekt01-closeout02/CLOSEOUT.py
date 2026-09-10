#!/usr/bin/python3.12
"""NEW current Detekt01 temporary-copy closeout02; never import/replay prior helpers.

Root alone may execute once after exact independent approval and fresh binding.
No child, Git, Gradle, stop, signal-to-process, mount action or W/T/E deletion.
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
D = B / 'reviews/detekt01-closeout02'
SELF, REQUEST, ACCEPT = D / 'CLOSEOUT.py', D / 'REQUEST.json', D / 'ACCEPT.json'
R = Path('/root/projects/PassVault/audit-runtime-linux-detekt01')
E, D01 = B / 'runs/linux-detekt01', B / 'reviews/detekt01-closeout01'
OUT = E / 'closeout02'
LOCK = R.parent / '.audit-coordination-linux-20260908/build.lock'
LOCK_PIN = (24, 14189001, 0, 33152, 1, 0, 1788910891124735946, 1788910891124735946)
SLOT = B / 'EXECUTION_SLOT.json'
SOURCE = B / 'reviews/desktop-integration03/source-prepare01/SOURCE.json'
PURPOSE = 'ONE_CURRENT_DETEKT01_TEMPORARY_REMAINDER_CLOSEOUT02'
COMMIT, TREE = '8f42274b04e206ff7254ca33d686a9666fce6723', '3a8f53dddd54f5c42c34f772975f02619be118d5'
RAW_IDENTITY = '91697cd8898b32a6e4dcc8611e7da333a26baf699f6e28a9e9d8072c8f372806'
MIB = 1024 ** 2
LIMITS = {'seconds': 120, 'entry_count': 4096, 'inventory_bytes': 2 * MIB,
          'runtime_hash_bytes': 192 * MIB, 'receipt_bytes': 65536,
          'launch_disk_GiB': 12, 'running_disk_GiB': 8, 'launch_mem_percent': 25, 'running_mem_percent': 20}
ODIR = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
OREAD = os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC
PINS = {R: (23, 1304148, 0, stat.S_IFDIR | 0o700),
        R.parent: (23, 498323, 0, stat.S_IFDIR | 0o755),
        R / 'checkout': (23, 1304151, 0, stat.S_IFDIR | 0o700),
        R / 'git-metadata': (23, 1304163, 0, stat.S_IFDIR | 0o700),
        E: (23, 1304147, 0, stat.S_IFDIR | 0o700),
        E.parent: (23, 642474, 0, stat.S_IFDIR | 0o700),
        LOCK.parent: (23, 661121, 0, stat.S_IFDIR | 0o700)}
INPUTS = {
    'manifest': (SOURCE, '1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23'),
    'allocation': (E / 'OUTER-ALLOCATION.json', '52216e32746c8614ee15d078274c505d31f3d689bb0974b0b958107fc15892f8'),
    'intent': (E / 'OUTER-INTENT.json', '48f262c5dec4237492d231989d5e299dd75a9ffbb329f525c4404879ae934c69'),
    'outer': (E / 'OUTER-RECEIPT.json', '6fa674ea620303612d0f71290a1210368dcc1bae98ac4be0c62e1b88edf3caa6'),
    'inner': (E / 'INNER-RESULT.json', 'aa8813acebfc2d9308926d179845a37303f85ef7652cf0c97aee8849883cfc46'),
    'source_before': (E / 'SOURCE-BEFORE.json', 'a71df23efdc330470cf1c71ce34e5344893473da0efa6506d5d4f699352237bc'),
    'source_after': (E / 'SOURCE-AFTER.json', 'a71df23efdc330470cf1c71ce34e5344893473da0efa6506d5d4f699352237bc'),
    'original_terminal': (B / 'reviews/detekt01/c15-preflight/EXTERNAL-RESULT.json',
                          'e7ffb842fa14d6dfc8ef971a2f647594d563a6f3f1fc6fb776e64e0d5e37beaf'),
    'original_review': (B / 'reviews/verification/LINUX-DETEKT01-C15-ACTUAL-RESULT-REVIEW.json',
                        '83f5c80bf1934177c7de042b88d327da46929d3b9de21198f918dfb2b813ca05'),
    'generated_result': (E / 'closeout01/RESULT.json', 'd90d706927b235572df9ffdbdd46990b63d26e821cc36ca7b9ebce817316a381'),
    'generated_terminal': (D01 / 'EXTERNAL-RESULT.json', '7ffd8f0473f5285710b2d85d89c0fc12d1b13838c138abba02b9a1f92ea37381'),
    'generated_review': (D01 / 'ACTUAL-REVIEW.json', '2c926d2ccaf6c2238e0426dcd0b035509812c4e63c04ee4be241b93888dd1fa4'),
}
REPORTS = ('app-android-detekt.xml', 'app-android-detekt.html', 'app-android-detekt.sarif', 'problems-report.html')
START, CANCELLED, last_resource = time.monotonic(), False, 0.0
DIRECTORIES, FILES, CHILDREN, SNAPSHOT = {}, {}, {}, {}
runtime_bytes, inventory_bytes = 0, 3
lock_fd, receipt_fd = None, None
record = {'schema': 1, 'purpose': PURPOSE, 'status': 'HOLD', 'removed_files': 0,
          'removed_directories_including_R': 0, 'removed_logical_bytes': 0,
          'builds_tests_stops': 0, 'resources': [], 'original_failed_records': 'UNCHANGED'}


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def encode(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), allow_nan=False).encode() + b'\n'


def decode(raw):
    def unique(pairs):
        value = {}
        for key, item in pairs:
            require(key not in value, 'duplicate JSON key')
            value[key] = item
        return value
    return json.loads(raw, object_pairs_hook=unique)


def dp(s):
    return s.st_dev, s.st_ino, s.st_uid, s.st_mode


def fp(s):
    return dp(s) + (s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns)


def historical(pin, directory=False):
    keys = ('dev', 'ino', 'uid', 'mode') if directory else ('dev', 'ino', 'uid', 'mode', 'nlink', 'bytes', 'mtime_ns', 'ctime_ns')
    return tuple(pin[key] for key in keys)


def resources(launch=False):
    global last_resource
    last_resource = time.monotonic()
    fs = os.statvfs(R.parent)
    with open('/proc/meminfo', 'rb') as stream:
        raw = stream.read(65537)
    require(len(raw) <= 65536, 'meminfo cap')
    memory = {line.split(b':', 1)[0]: int(line.split()[1]) * 1024
              for line in raw.splitlines() if line.startswith((b'MemAvailable:', b'MemTotal:'))}
    disk, available, total = fs.f_bavail * fs.f_frsize, memory[b'MemAvailable'], memory[b'MemTotal']
    require(len(record['resources']) < 125, 'resource observation cap')
    record['resources'].append({'elapsed_seconds': round(last_resource - START, 3), 'launch': launch,
                                'disk_available_bytes': disk, 'memory_available_bytes': available, 'memory_total_bytes': total})
    require(disk >= (12 if launch else 8) * 1024 ** 3 and available * 100 >= total * (25 if launch else 20),
            'resource floor')


def check():
    require(not CANCELLED and time.monotonic() - START < 120, 'cancelled/120s cooperative deadline')
    if time.monotonic() - last_resource >= 1:
        resources()


def directory(path):
    check()
    parent = None if path == Path('/') else directory(path.parent)
    if path not in DIRECTORIES:
        fd = os.open('/' if parent is None else path.name, ODIR, dir_fd=parent)
        try:
            pin = dp(os.fstat(fd))
            require(pin[2] == 0 and not pin[3] & 0o022 and (path not in PINS or pin == PINS[path]), 'directory origin/owner/mode')
            DIRECTORIES[path] = (fd, pin)
        except BaseException:
            os.close(fd)
            raise
    fd, pin = DIRECTORIES[path]
    require(dp(os.fstat(fd)) == pin and (parent is None or
            dp(os.stat(path.name, dir_fd=parent, follow_symlinks=False)) == pin), 'directory binding changed')
    return fd


def capture(path, cap, digest=None, original=None):
    parent = directory(path.parent)
    before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
    require(stat.S_ISREG(before.st_mode) and before.st_uid == 0 and before.st_nlink == 1
            and not before.st_mode & 0o022 and before.st_size <= cap, 'bounded regular retained input')
    fd = os.open(path.name, OREAD, dir_fd=parent)
    try:
        require(fp(os.fstat(fd)) == fp(before) and (original is None or fp(before) == tuple(original)), 'input open/original pin')
        data = bytearray()
        while part := os.read(fd, 65536):
            check()
            data.extend(part)
            require(len(data) <= cap, 'input read cap')
        require(len(data) == before.st_size and fp(before) == fp(os.fstat(fd))
                == fp(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input drift')
        require(digest is None or hashlib.sha256(data).hexdigest() == digest, 'retained input hash')
        return bytes(data)
    finally:
        os.close(fd)


def flush():
    if receipt_fd is None:
        return
    record['elapsed_seconds'] = round(time.monotonic() - START, 3)
    raw = encode(record)
    require(len(raw) <= LIMITS['receipt_bytes'], 'receipt byte cap')
    os.lseek(receipt_fd, 0, os.SEEK_SET)
    os.ftruncate(receipt_fd, 0)
    at = 0
    while at < len(raw):
        count = os.write(receipt_fd, raw[at:])
        require(count > 0, 'short receipt write')
        at += count
    os.fsync(receipt_fd)


def retain_inventory(raw):
    parent = directory(OUT)
    fd = os.open('INVENTORY.json', os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=parent)
    try:
        at = 0
        while at < len(raw):
            check()
            count = os.write(fd, raw[at:])
            require(count > 0, 'short inventory write')
            at += count
        os.fsync(fd)
        pin = fp(os.fstat(fd))
        os.lseek(fd, 0, os.SEEK_SET)
        result = bytearray()
        while part := os.read(fd, 65536):
            check()
            result.extend(part)
            require(len(result) <= len(raw), 'inventory readback cap')
        require(bytes(result) == raw and pin == fp(os.fstat(fd))
                == fp(os.stat('INVENTORY.json', dir_fd=parent, follow_symlinks=False)), 'retained inventory drift')
    finally:
        os.close(fd)
    os.fsync(parent)
    return {'path': 'closeout02/INVENTORY.json', 'bytes': len(raw), 'sha256': hashlib.sha256(raw).hexdigest(), 'pin': pin}


def mounts():
    check()
    with open('/proc/self/mountinfo', 'rb') as stream:
        raw = stream.read(4 * MIB + 1)
    require(0 < len(raw) <= 4 * MIB and raw.endswith(b'\n'), 'complete bounded mount table')
    lines = raw.splitlines()
    require(len(lines) <= 8192, 'mount row cap')
    for line in lines:
        halves = line.split(b' - ')
        require(len(halves) == 2 and len(halves[0].split()) >= 6, 'mountinfo prefix')
        suffix = halves[1].split(b' ')  # Preserve a legitimate empty SOURCE field.
        require(len(suffix) == 3 and suffix[0] and suffix[2], 'mountinfo suffix')
        point = halves[0].split()[4]
        for escaped, byte in ((b'\\040', b' '), (b'\\011', b'\t'), (b'\\012', b'\n'), (b'\\134', b'\\')):
            point = point.replace(escaped, byte)
        require(point.startswith(b'/') and point != os.fsencode(R) and not point.startswith(os.fsencode(R) + b'/'),
                'mount at/below R: HOLD')
    return {'sha256': hashlib.sha256(raw).hexdigest(), 'mount_rows': len(lines), 'runtime_mounts': 0}


def coordination(request):
    require(fp(os.fstat(lock_fd)) == tuple(request['lock_pin']) == LOCK_PIN
            == fp(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock changed')
    raw = capture(SLOT, MIB, request['slot_sha256'])
    require(decode(raw)['state'] == 'IDLE_NO_AUDIT_BUILD_ADMITTED'
            and {key: os.readlink('/proc/self/ns/' + key) for key in ('pid', 'mnt')} == request['parent_namespaces'],
            'fresh slot/namespace binding')


def exact_names(fd, relative):
    expected, names = CHILDREN[relative], []
    with os.scandir(fd) as entries:
        for entry in entries:
            check()
            require(len(names) < len(expected), 'extra/unadmitted directory member')
            names.append(entry.name)
    require(sorted(names) == expected, 'missing/unexpected exact member')
    return expected


def add(relative, value):
    global inventory_bytes
    require(relative not in SNAPSHOT and len(SNAPSHOT) < LIMITS['entry_count'], 'duplicate/inventory count cap')
    inventory_bytes += len(encode(relative)) + len(encode(value))
    require(inventory_bytes <= LIMITS['inventory_bytes'], 'incremental inventory byte cap')
    SNAPSHOT[relative] = value


def verify_file(parent, name, relative):
    global runtime_bytes
    expected = FILES[relative]
    before = os.stat(name, dir_fd=parent, follow_symlinks=False)
    require(before.st_dev == 24 and before.st_uid == 0 and before.st_nlink == 1
            and before.st_mode == expected['mode'] and before.st_size == expected['bytes'], 'exact source/transport metadata')
    require('original_pin' not in expected or fp(before) == expected['original_pin'], 'original transport/index pin changed')
    fd = os.open(name, OREAD, dir_fd=parent)
    try:
        require(fp(os.fstat(fd)) == fp(before), 'runtime file changed at open')
        sha, blob, size = hashlib.sha256(), hashlib.sha1(b'blob ' + str(before.st_size).encode() + b'\0'), 0
        while part := os.read(fd, 65536):
            check()
            size += len(part)
            runtime_bytes += len(part)
            require(size <= expected['bytes'] and runtime_bytes <= LIMITS['runtime_hash_bytes'], 'runtime stream cap')
            sha.update(part)
            blob.update(part)
        require(size == expected['bytes'] and sha.hexdigest() == expected['sha256']
                and ('git_blob' not in expected or blob.hexdigest() == expected['git_blob'])
                and fp(before) == fp(os.fstat(fd)) == fp(os.stat(name, dir_fd=parent, follow_symlinks=False)), 'runtime source/hash drift')
        add(relative, ['f', fp(before), sha.hexdigest()])
    finally:
        os.close(fd)


def snapshot(fd, relative):
    check()
    pin = dp(os.fstat(fd))
    path = R if not relative else R / relative
    require(pin[0] == 23 and pin[2] == 0 and pin[3] == stat.S_IFDIR | 0o700
            and (path not in PINS or pin == PINS[path]), 'source directory origin/type')
    add(relative, ['d', pin])
    for name in exact_names(fd, relative):
        child = relative + '/' + name if relative else name
        if child in FILES:
            verify_file(fd, name, child)
        else:
            before = os.stat(name, dir_fd=fd, follow_symlinks=False)
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                require(dp(os.fstat(sub)) == dp(before), 'source directory open drift')
                snapshot(sub, child)
                require(dp(os.stat(name, dir_fd=fd, follow_symlinks=False)) == dp(before), 'source directory name drift')
            finally:
                os.close(sub)
    exact_names(fd, relative)


def remove(fd, relative):
    check()
    require(dp(os.fstat(fd)) == SNAPSHOT[relative][1], 'inventoried directory changed')
    for name in exact_names(fd, relative):
        check()
        child = relative + '/' + name if relative else name
        item = SNAPSHOT[child]
        before = os.stat(name, dir_fd=fd, follow_symlinks=False)
        if item[0] == 'd':
            require(dp(before) == item[1], 'inventoried child directory changed')
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                remove(sub, child)
                require(dp(os.fstat(sub)) == item[1] == dp(os.stat(name, dir_fd=fd, follow_symlinks=False)), 'final child directory binding')
            finally:
                os.close(sub)
            os.rmdir(name, dir_fd=fd)
            record['removed_directories_including_R'] += 1
        else:
            require(fp(before) == item[1], 'inventoried file changed')
            os.unlink(name, dir_fd=fd)
            record['removed_files'] += 1
            record['removed_logical_bytes'] += item[1][5]
        if (record['removed_files'] + record['removed_directories_including_R']) % 128 == 0:
            flush()
    with os.scandir(fd) as entries:
        require(next(entries, None) is None, 'directory not empty after admitted removal')
    os.fsync(fd)


def main():
    global lock_fd, receipt_fd
    require(sys.argv == [str(SELF)] and sys.executable == '/usr/bin/python3.12' and sys.flags.isolated
            and sys.flags.no_site and sys.dont_write_bytecode and not sys.flags.optimize
            and os.getuid() == os.geteuid() == 0, 'fixed isolated root entry')
    resources(True)
    request_raw, approval_raw = capture(REQUEST, MIB), capture(ACCEPT, MIB)
    request, approval = decode(request_raw), decode(approval_raw)
    source_hash = hashlib.sha256(capture(SELF, MIB)).hexdigest()
    require(request['purpose'] == PURPOSE and request['source_sha256'] == source_hash and request['limits'] == LIMITS
            and approval == {'purpose': PURPOSE, 'reviewer': '/root/verification', 'disposition': 'ACCEPT_EXACT_ONCE',
                             'request_sha256': hashlib.sha256(request_raw).hexdigest(), 'source_sha256': source_hash}, 'exact independent admission')
    require(request['binding_complete'] is True and request['sole_owner'] == '/root'
            and request['no_audit_build_or_ci'] is True and request['original_remainder_frozen'] is True,
            'fresh root coordination incomplete')
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(fp(os.fstat(lock_fd)) == tuple(request['lock_pin']) == LOCK_PIN
            == fp(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock before flock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    coordination(request)
    inputs = {key: decode(capture(path, 2 * MIB, digest)) for key, (path, digest) in INPUTS.items()}
    manifest, outer, intent = inputs['manifest'], inputs['outer'], inputs['intent']
    require(manifest['commit'] == COMMIT and manifest['tree'] == TREE and len(manifest['files']) == 2757
            and inputs['source_before'] == inputs['source_after']
            and inputs['source_after']['bytes'] == 87770345 and inputs['source_after']['members'] == 2757,
            'original C15 source binding')
    for path in (R, E):
        require(historical(inputs['allocation']['allocated_directories'][str(path)], True) == PINS[path], 'original allocation')
    for path in (R.parent, E.parent, LOCK.parent):
        require(historical(inputs['allocation']['parents'][str(path)], True) == PINS[path], 'original parent allocation')
    for path in (R / 'checkout', R / 'git-metadata'):
        require(historical(intent['directories'][str(path)], True) == PINS[path], 'original underlying directory, not mounted alias')
    require(inputs['original_terminal']['external_exit'] == 70 and inputs['generated_terminal']['actual_exit'] == 0
            and inputs['generated_review']['disposition'] == 'ACCEPT_COMPLETED_CURRENT_GENERATED_ONLY_CLOSEOUT_PRESERVE_SOURCE_REMAINDER_HOLD'
            and inputs['generated_result']['source_remainder'] == 'UNTOUCHED_HELD', 'separate original/generated settlement')
    generated = inputs['generated_result']
    require(generated['reports_preserved'] is True and len(generated['reports']) == 4, 'four reports previously retained')
    for name, row in zip(REPORTS, generated['reports']):
        kept = row['retained']
        require(kept['path'] == 'closeout01/reports/' + name, 'fixed retained report destination')
        capture(E / kept['path'], 4 * MIB, kept['sha256'], kept['pin'])
    folders, identities, oids = {'', 'checkout', 'git-metadata'}, [], bytearray()
    for row in manifest['files']:
        check()
        name, mode = row['path'], row['git_mode']
        require(isinstance(name, str) and 0 < len(name) <= 4096 and len(name.split('/')) <= 32
                and not name.startswith('/') and '\0' not in name and '\n' not in name
                and all(p not in ('', '.', '..', '.git') for p in name.split('/'))
                and mode in ('100644', '100755') and 0 <= row['raw_size'] <= 32 * MIB, 'safe fixed raw source member')
        relative = 'checkout/' + name
        require(relative not in FILES, 'duplicate source member')
        FILES[relative] = {'bytes': row['raw_size'], 'mode': int(mode, 8), 'sha256': row['raw_sha256'], 'git_blob': row['git_blob']}
        parts = relative.split('/')
        folders.update('/'.join(parts[:n]) for n in range(1, len(parts)))
        identities.append([name, mode, row['git_blob'], row['raw_size'], row['raw_sha256']])
        oids.extend(row['git_blob'].encode('ascii') + b'\n')
    require(hashlib.sha256(encode(sorted(identities))).hexdigest() == RAW_IDENTITY == outer['source_before']['raw_identity_sha256']
            == intent['raw_source']['raw_identity_sha256'] and len(oids) == 113037, 'raw identity and ordered OID derivation')
    extras = {'source.oids': outer['raw_transport']['ordered_request'][str(R / 'source.oids')],
              'source.blobs': outer['raw_transport']['response'][str(R / 'source.blobs')],
              'git-index': outer['index_copy']['copy_image']}
    require(hashlib.sha256(oids).hexdigest() == extras['source.oids']['sha256']
            and [extras[p]['pin']['bytes'] for p in ('source.oids', 'source.blobs', 'git-index')] == [113037, 87913727, 440654],
            'original exact transport/index sizes and OID order')
    for name, image in extras.items():
        pin = historical(image['pin'])
        require(pin[0] == 24 and pin[2] == 0 and pin[4] == 1
                and pin[3] == stat.S_IFREG | (0o400 if name == 'git-index' else 0o600), 'original transport/index type')
        FILES[name] = {'bytes': pin[5], 'mode': pin[3], 'sha256': image['sha256'], 'original_pin': pin}
    require(not folders.intersection(FILES) and len(FILES) == 2760 and len(folders) == 840
            and sum(v['bytes'] for v in FILES.values()) == 176237763, 'exact source-derived remainder size/count')
    CHILDREN.update({p: [] for p in folders})
    for relative in set(FILES) | (folders - {''}):
        parent, _, name = relative.rpartition('/')
        CHILDREN[parent].append(name)
    for names in CHILDREN.values():
        names.sort()
    record.update(utc=datetime.datetime.now(datetime.timezone.utc).isoformat(), source_sha256=source_hash,
                  request_sha256=hashlib.sha256(request_raw).hexdigest(), approval_sha256=hashlib.sha256(approval_raw).hexdigest(),
                  inputs_sha256={key: digest for key, (_, digest) in INPUTS.items()}, original_root_pins={str(p): pin for p, pin in PINS.items()},
                  source_commit=COMMIT, source_tree=TREE, raw_source_identity_sha256=RAW_IDENTITY,
                  expected_files=2760, expected_directories_including_R=840, expected_logical_bytes=176237763,
                  reports_reverified=4, mount_before=mounts())
    root, parent = directory(R), directory(R.parent)
    os.mkdir(OUT.name, 0o700, dir_fd=directory(E))
    directory(OUT)
    receipt_fd = os.open('RESULT.json', os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=directory(OUT))
    record.update(status='VERIFYING_EXACT_TEMPORARY_COPY', new_descriptor_reacquisition=True)
    flush()
    os.fsync(directory(E))
    snapshot(root, '')
    require(len(SNAPSHOT) == 3600 and runtime_bytes == 176237763, 'full exact snapshot/hash completion')
    inventory = encode(SNAPSHOT)
    require(len(inventory) <= LIMITS['inventory_bytes'], 'final inventory cap')
    record['inventory_retained'] = retain_inventory(inventory)
    coordination(request)
    directory(R)
    record.update(inventory_entries=3600, runtime_bytes_hashed=runtime_bytes, mount_before_delete=mounts(), status='DELETING_EXACT_TEMPORARY_COPY')
    flush()
    remove(root, '')
    require(dp(os.fstat(root)) == PINS[R] == dp(os.stat(R.name, dir_fd=parent, follow_symlinks=False)), 'final original R binding')
    os.rmdir(R.name, dir_fd=parent)
    record['removed_directories_including_R'] += 1
    record['removed_R'] = True
    os.fsync(parent)
    require(os.fstat(root).st_nlink == 0, 'removed original R descriptor not unlinked')
    try:
        os.stat(R.name, dir_fd=parent, follow_symlinks=False)
    except FileNotFoundError:
        pass
    else:
        raise RuntimeError('R name reappeared; no further action')
    require(record['removed_files'] == 2760 and record['removed_directories_including_R'] == 840
            and record['removed_logical_bytes'] == 176237763, 'final removal accounting')
    record.update(mount_after=mounts(), status='TEMPORARY_REMAINDER_REMOVED_PENDING_INDEPENDENT_RECONCILIATION',
                  qualification='Only exact original current R temporary C15 copy/transport/index/empty underlying metadata removed. '
                                'W/T/E and original failed records preserved. Cooperative freeze, not hostile-root or atomic unlink proof.')


if __name__ == '__main__':
    os.umask(0o077)
    def cancel(_number, _frame):
        global CANCELLED
        CANCELLED = True
    signals = {signal.SIGTERM, signal.SIGINT, signal.SIGHUP}
    for signum in signals:
        signal.signal(signum, cancel)
    code = 1
    try:
        main()
        code = 0
    except BaseException as error:
        record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', error=type(error).__name__ + ': ' + str(error)[:1200])
    finally:
        signal.pthread_sigmask(signal.SIG_BLOCK, signals)
        if CANCELLED or signal.sigpending() & signals or time.monotonic() - START >= 120:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', terminal_cancel_or_deadline=True)
            code = 1
        try:
            flush()
        except BaseException as error:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', receipt_error=type(error).__name__)
            code = 1
        descriptors = [fd for fd, _ in reversed(tuple(DIRECTORIES.values()))]
        descriptors += ([] if lock_fd is None else [lock_fd]) + ([] if receipt_fd is None else [receipt_fd])
        for fd in descriptors:
            try:
                os.close(fd)
            except OSError:
                record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', descriptor_close_uncertainty=True)
                code = 1
        print(encode({'external_pending_code': code, 'status': record['status'], 'error': record.get('error'),
                      'receipt_error': record.get('receipt_error'), 'descriptor_close_uncertainty': record.get('descriptor_close_uncertainty', False),
                      'removed_files': record['removed_files'], 'removed_directories_including_R': record['removed_directories_including_R'],
                      'removed_logical_bytes': record['removed_logical_bytes'], 'removed_R': record.get('removed_R', False),
                      'qualification': 'Receipt precedes descriptor closure and actual tool exit; root must reconcile both. No automatic retry.'}).decode(), end='')
    sys.exit(code)
