#!/usr/bin/python3.12
"""NEW current Detekt02 whole-R closeout01; SOURCE ONLY until separately admitted.

Fixed-path/once-only, original-cohort cooperative freeze, not hostile-root safety.
No subprocess, build/stop, process census/signals, Git/SDK/network, mount action,
namespace entry, old-helper import/replay, or W/T/permanent-evidence deletion.
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
D = B / 'reviews/detekt02-closeout01'
SELF, REQUEST, ACCEPT = D / 'CLOSEOUT.py', D / 'REQUEST.json', D / 'ACCEPT.json'
R = Path('/root/projects/PassVault/audit-runtime-linux-detekt02')
E, OUT = B / 'runs/linux-detekt02', B / 'runs/linux-detekt02/closeout01'
LOCK = R.parent / '.audit-coordination-linux-20260908/build.lock'
LOCK_PIN = (24, 14189001, 0, 33152, 1, 0, 1788910891124735946, 1788910891124735946)
SLOT = B / 'EXECUTION_SLOT.json'
SOURCE = B / 'reviews/detekt02/source-prepare01/SOURCE.json'
PURPOSE = 'ONE_CURRENT_DETEKT02_WHOLE_R_CLOSEOUT01'
COMMIT, TREE = 'e6738b17a7c783383a4f0ae0f17af726cffb9a40', 'c4009ab5f9ba23bb8097d64cc54328b131baa0c8'
RAW_IDENTITY = '97c8f86c6a25928db7776e16d2b803bfceb1d1b39be9f4064a5b5c152e1f78c8'
MIB = 1024 ** 2
LIMITS = {'seconds': 180, 'entry_count': 40000, 'inventory_bytes': 16 * MIB,
          'logical_bytes': 2 * 1024 ** 3, 'runtime_hash_bytes': 256 * MIB, 'receipt_bytes': 65536,
          'launch_disk_GiB': 12, 'running_disk_GiB': 8, 'launch_mem_percent': 25, 'running_mem_percent': 20}
ODIR = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
OREAD = os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC
PRIVATE = ('home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
MODULES = ('', 'app-android', 'app-desktop', 'shared', 'core', 'feature',
           'core/domain', 'core/database', 'core/crypto', 'core/security', 'core/designsystem',
           'core/navigation', 'core/otp', 'core/testing', 'feature/onboarding', 'feature/unlock',
           'feature/vault', 'feature/credential', 'feature/generator', 'feature/health',
           'feature/settings', 'feature/backup')
GENERATED = tuple('checkout/' + (m + '/' if m else '') + 'build' for m in MODULES) + (
    'checkout/.gradle', 'checkout/.kotlin')
ALLOWROOTS = PRIVATE + GENERATED
TOP = {'checkout': 1372037, 'home': 1372038, 'tmp': 1372039, 'jna': 1372040,
       'sqlite': 1372041, 'gradle-home': 1372042, 'konan': 1372043, 'android-user': 1372044,
       'xdg-cache': 1372045, 'xdg-config': 1372046, 'xdg-data': 1372047,
       'xdg-state': 1372048, 'git-metadata': 1372049}
PINS = {R: (23, 1372034, 0, stat.S_IFDIR | 0o700),
        R.parent: (23, 498323, 0, stat.S_IFDIR | 0o755),
        E: (23, 1372033, 0, stat.S_IFDIR | 0o700),
        E.parent: (23, 642474, 0, stat.S_IFDIR | 0o700),
        E / 'logs': (23, 1372035, 0, stat.S_IFDIR | 0o700),
        E / 'reports': (23, 1372036, 0, stat.S_IFDIR | 0o700),
        LOCK.parent: (23, 661121, 0, stat.S_IFDIR | 0o700)}
PINS.update({R / name: (23, ino, 0, stat.S_IFDIR | 0o700) for name, ino in TOP.items()})
INPUTS = {
    'manifest': (SOURCE, '2af03198511f26cfafb0ddd01470e66585697507e95c654530e1621b5ce0831b'),
    'allocation': (E / 'OUTER-ALLOCATION.json', '6af8a25ec3137458b48ee112fb5c8298fa0f188fdb1302684c7a838929fef833'),
    'intent': (E / 'OUTER-INTENT.json', '280eb0c77b0e39363dae1da3b4dd8800f33faa7762cdc3eaa9160b72a8174368'),
    'outer': (E / 'OUTER-RECEIPT.json', '8099cb62ccbd72928b760281c07e5a3f5926ba3497147ae17144a03d2cac56b0'),
    'inner': (E / 'INNER-RESULT.json', '911e25a273b8ab280a784b130ca9515ab9b13cf94c20ebfa1d3be960c7a68506'),
    'reports': (E / 'STATIC-REPORTS.json', '87a859877d517408d8550b9f1b3cdd2b4f64cd5c873cf7dd8877739b5325c6ab'),
    'terminal': (B / 'reviews/detekt02/c16-preflight/EXTERNAL-RESULT.json',
                 '2cd877888c7b539208b4427c7e4992673c8709845a303aaf6e7e369e4407e4fa'),
}
TEXT_INPUTS = {
    B / 'reviews/verification/LINUX-DETEKT02-ACTUAL-REVIEW.md':
        '722ad5bdf974bbaa06b5b37a597a5e91e4c12de25fa76bf9bbece4e62a541edb',
    B / 'reviews/storage/DETEKT02-CURRENT-WHOLE-R-CLOSEOUT-PLAN-v2.md':
        '104a9e60980a3d5289926b39e52e83e1cc95462510a70ccb6405130dda81ed9c',
    B / 'reviews/verification/DETEKT02-CURRENT-WHOLE-R-CLOSEOUT-PLAN-REVIEW.md':
        'af412d18e7473f9ff5ab7bb694679a530dd628baae984c328f90a6103ca415c7',
    E / 'CANCEL': '47c907d45f33722bf908113afe185dadb46d1136bcd039c0f003c97f2f3d6a6c',
}
PROBLEM = 'checkout/build/reports/problems/problems-report.html'
PROBLEM_SHA = '077cc100c8a55fe02d644fa03f22eebbbaea0ff26a556d3c521ead8c8b1185bc'
FAILURE_FLAGS = {'source_after': False, 'cleanup_safe': False, 'independent_semantic_acceptance': False}
START, CANCELLED, last_resource = time.monotonic(), False, 0.0
DIRECTORIES, FILES, CHILDREN, SNAPSHOT = {}, {}, {}, {}
RAW_DIRS, REQUIRED_DIRS = set(), set()
runtime_bytes, logical_bytes, inventory_bytes = 0, 0, 3
lock_fd, receipt_fd = None, None
record = {'schema': 1, 'purpose': PURPOSE, 'status': 'HOLD', 'removed_files': 0,
          'removed_directories_including_R': 0, 'removed_logical_bytes': 0, 'resources': [],
          'builds_tests_stops': 0, 'original_failure_flags_unchanged': FAILURE_FLAGS,
          'unclassified_host_churn': {'count': 1, 'attribution': 'UNIDENTIFIED_UNWAIVED',
                                    'reason': 'positive buildlike process vanished/changed/unreadable'}}


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
    require(0 < len(raw) <= 65536 and raw.endswith(b'\n'), 'complete bounded meminfo')
    memory = {line.split(b':', 1)[0]: int(line.split()[1]) * 1024
              for line in raw.splitlines() if line.startswith((b'MemAvailable:', b'MemTotal:'))}
    disk, available, total = fs.f_bavail * fs.f_frsize, memory[b'MemAvailable'], memory[b'MemTotal']
    require(total > 0 and 0 <= available <= total and len(record['resources']) < 185, 'resource observation bound')
    record['resources'].append({'elapsed_seconds': round(last_resource - START, 3), 'launch': launch,
                                'disk_available_bytes': disk, 'memory_available_bytes': available, 'memory_total_bytes': total})
    require(disk >= (12 if launch else 8) * 1024 ** 3 and available * 100 >= total * (25 if launch else 20),
            'resource floor')


def check():
    require(not CANCELLED and time.monotonic() - START < LIMITS['seconds'], 'cancelled/180s cooperative deadline')
    if time.monotonic() - last_resource >= 1:
        resources()


def directory(path):
    check()
    parent = None if path == Path('/') else directory(path.parent)
    if path not in DIRECTORIES:
        fd = os.open('/' if parent is None else path.name, ODIR, dir_fd=parent)
        try:
            pin = dp(os.fstat(fd))
            require(pin[2] == 0 and not pin[3] & 0o7022 and (path not in PINS or pin == PINS[path]),
                    'directory origin/owner/mode')
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
            and not before.st_mode & 0o7022 and 0 <= before.st_size <= cap, 'bounded regular retained input')
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
        require(digest is None or hashlib.sha256(data).hexdigest() == digest, 'retained input hash: ' + path.name)
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
            count = os.write(fd, raw[at:at + 65536])
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
    return {'path': 'closeout01/INVENTORY.json', 'bytes': len(raw), 'sha256': hashlib.sha256(raw).hexdigest(), 'pin': pin}


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


def names(fd):
    result = []
    with os.scandir(fd) as entries:
        for entry in entries:
            check()
            require(len(result) < LIMITS['entry_count'], 'directory member cap')
            result.append(entry.name)
    return sorted(result)


def classify(relative, is_directory):
    if relative in FILES:
        require(not is_directory, 'required regular member became directory: ' + relative)
        return
    if relative in REQUIRED_DIRS:
        require(is_directory, 'required directory became file: ' + relative)
        return
    require(any(relative == p or relative.startswith(p + '/') for p in ALLOWROOTS),
            'unadmitted runtime member: ' + relative)
    # Exact evidence locations/patterns anywhere in the admitted trees, not
    # broad test/report substrings or arbitrary XML/HTML/log/cache contents.
    parts = relative.lower().split('/')
    name = parts[-1]
    report = any(p in ('reports', 'test-results', 'surefire-reports', 'failsafe-reports', 'test-output') for p in parts)
    diagnostic = (name.endswith(('.sarif', '.sarif.json', '.xcresult'))
                  or (not is_directory and (name.endswith('.hprof')
                      or (name.startswith('test-') and name.endswith('.xml'))
                      or name.startswith(('hs_err_pid', 'replay_pid', 'worker-error-'))
                      or name == 'core' or (name.startswith('core.') and name[5:].isdigit()))))
    if report or diagnostic:
        record['unexpected_evidence'] = {'relative': relative, 'directory': is_directory,
                                         'reason': 'unpreserved report/test/crash/worker-error evidence'}
        raise RuntimeError('unexpected diagnostic/test evidence: HOLD ' + relative)


def add(relative, value):
    global inventory_bytes, logical_bytes
    require(relative not in SNAPSHOT and len(SNAPSHOT) < LIMITS['entry_count'], 'duplicate/inventory count cap')
    inventory_bytes += len(encode(relative)) + len(encode(value))
    require(inventory_bytes <= LIMITS['inventory_bytes'], 'incremental inventory byte cap')
    if value[0] == 'f':
        logical_bytes += value[1][5]
        require(logical_bytes <= LIMITS['logical_bytes'], 'logical inventory byte cap')
    SNAPSHOT[relative] = value
    record.update(snapshot_observed_entries=len(SNAPSHOT), snapshot_observed_logical_bytes=logical_bytes)


def verify_file(parent, name, relative, before):
    global runtime_bytes
    expected = FILES[relative]
    require(before.st_mode == expected['mode'] and before.st_size == expected['bytes'], 'exact source/transport/report metadata')
    require('original_pin' not in expected or fp(before) == expected['original_pin'], 'original buffer/report pin changed')
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
    require(len(relative.split('/')) <= 64, 'runtime depth cap')
    pin, path = dp(os.fstat(fd)), R if not relative else R / relative
    require(pin[0] == 23 and pin[2] == 0 and not pin[3] & 0o7022
            and (path not in PINS or pin == PINS[path])
            and (relative not in RAW_DIRS or pin[3] == stat.S_IFDIR | 0o700), 'directory origin/type/mode')
    classify(relative, True)
    add(relative, ['d', pin])
    CHILDREN[relative] = names(fd)
    if relative == '':
        require(set(CHILDREN[relative]) == set(TOP) | {'source.oids', 'source.blobs', 'git-index'}, 'exact original top membership')
    if relative == 'git-metadata':
        require(not CHILDREN[relative], 'original underlying metadata must be empty; do not traverse alias')
    for name in CHILDREN[relative]:
        check()
        child = relative + '/' + name if relative else name
        require(name not in ('', '.', '..', '.git') and '/' not in name
                and not any(ord(c) < 32 or ord(c) == 127 for c in name) and len(child) <= 8192, 'bounded safe member name')
        before = os.stat(name, dir_fd=fd, follow_symlinks=False)
        require(before.st_uid == 0 and not before.st_mode & 0o7022, 'entry owner/mode')
        is_directory = stat.S_ISDIR(before.st_mode)
        require(is_directory or (stat.S_ISREG(before.st_mode) and before.st_dev == 24 and before.st_nlink == 1),
                'nonregular/link/device: HOLD')
        classify(child, is_directory)
        if is_directory:
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                require(dp(os.fstat(sub)) == dp(before), 'directory open drift')
                snapshot(sub, child)
                require(dp(os.fstat(sub)) == dp(before) == dp(os.stat(name, dir_fd=fd, follow_symlinks=False)), 'directory name drift')
            finally:
                os.close(sub)
        elif child in FILES:
            verify_file(fd, name, child, before)
        else:
            add(child, ['f', fp(before)])  # Generated/cache metadata only; not a content-diagnostic claim.
    require(names(fd) == CHILDREN[relative] and dp(os.fstat(fd)) == pin, 'snapshot membership/identity drift')


def walk_inventory(fd, relative, *, deleting):
    check()
    require(dp(os.fstat(fd)) == SNAPSHOT[relative][1], 'inventoried directory changed')
    expected = CHILDREN[relative]
    require(names(fd) == expected, 'inventoried membership changed')
    for offset, name in enumerate(expected):
        check()
        if deleting and offset % 128 == 0:
            require(names(fd) == expected[offset:], 'remaining membership changed')
        child = relative + '/' + name if relative else name
        item, before = SNAPSHOT[child], os.stat(name, dir_fd=fd, follow_symlinks=False)
        if item[0] == 'd':
            require(dp(before) == item[1], 'inventoried child directory changed')
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                walk_inventory(sub, child, deleting=deleting)
                require(dp(os.fstat(sub)) == item[1] == dp(os.stat(name, dir_fd=fd, follow_symlinks=False)), 'final child directory binding')
            finally:
                os.close(sub)
            if deleting:
                check()
                os.rmdir(name, dir_fd=fd)
                record['removed_directories_including_R'] += 1
        else:
            require(fp(before) == item[1], 'inventoried file changed')
            if deleting:
                check()
                os.unlink(name, dir_fd=fd)
                record['removed_files'] += 1
                record['removed_logical_bytes'] += item[1][5]
        if deleting and (record['removed_files'] + record['removed_directories_including_R']) % 128 == 0:
            flush()
    require(names(fd) == ([] if deleting else expected), 'final remaining membership changed')
    if deleting:
        os.fsync(fd)


def bind_retained(inputs):
    manifest, allocation, intent, outer, inner = (inputs[k] for k in ('manifest', 'allocation', 'intent', 'outer', 'inner'))
    require(manifest['commit'] == COMMIT and manifest['tree'] == TREE and len(manifest['files']) == 2887
            and len(manifest['checkout_eol_qualifications']) == 2 and allocation == outer['allocation'],
            'original C16 manifest/allocation')
    for path in (R, E):
        require(historical(allocation['allocated_directories'][str(path)], True) == PINS[path], 'original allocation')
    for path in (R.parent, E.parent, LOCK.parent):
        require(historical(allocation['parents'][str(path)], True) == PINS[path], 'original parent allocation')
    for path in (R, E, E / 'logs', E / 'reports', *(R / p for p in TOP)):
        require(historical(intent['directories'][str(path)], True) == PINS[path], 'original top/evidence directory')
    require(inner['commit'] == COMMIT and inner['tree'] == TREE and inner['source_before'] is True
            and all(inner[k] is v for k, v in FAILURE_FLAGS.items())
            and inner['all_required_stops_ok'] is True and inner['namespace_empty_before_exit'] is True
            and inner['git_bindings_stable'] is True and inner['sealed_index_unchanged'] is True
            and inner['declared_test_cases'] == 0, 'original failure/settlement flags; no waiver')
    phases = inner['phases']
    require([p['phase'] for p in phases] == ['git-isolation', 'detekt'] and all(p['settled'] is True for p in phases)
            and phases[1]['stop_required'] is True and phases[1]['stop_attempted'] is True and phases[1]['stop_ok'] is True
            and [(c['label'], c['complete'], c['exit']) for c in phases[1]['commands']] ==
                [('detekt', True, 1), ('detekt-stop', True, 0)], 'original cohort only; no renewed stop')
    require(outer['status'] == 'HOLD' and outer['cleanup'] == 'NOT_ATTEMPTED' and outer['unclassified_host_churn'] == 1
            and 'RuntimeError:positive buildlike process vanished/changed/unreadable' in outer['reasons']
            and [c['exit'] for c in outer['children']] == [0, 70]
            and all(c['pidfd_kill_attempted'] is False for c in outer['children'])
            and inputs['terminal']['terminal_exit'] == 70 and inputs['terminal']['direct_tool_session_complete'] is True,
            'original actual failure/churn/settlement binding')
    for phase in phases:
        image = phase['record_image']
        capture(E / ('PHASE-' + phase['phase'] + '.json'), MIB, image['sha256'], historical(image['pin']))
    for command in phases[1]['commands']:
        image = command['log']
        capture(E / 'logs' / (command['label'] + '.log'), MIB, image['sha256'], historical(image['pin']))
    RAW_DIRS.update({'', *TOP})
    identities, oids = [], bytearray()
    for row in manifest['files']:
        check()
        name, mode = row['path'], row['git_mode']
        require(isinstance(name, str) and 0 < len(name) <= 4096 and len(name.split('/')) <= 32
                and not name.startswith('/') and not any(ord(c) < 32 or ord(c) == 127 for c in name)
                and all(p not in ('', '.', '..', '.git') for p in name.split('/'))
                and mode in ('100644', '100755') and 0 <= row['raw_size'] <= 32 * MIB, 'safe raw source member')
        relative = 'checkout/' + name
        require(relative not in FILES and not any(relative == p or relative.startswith(p + '/') or p.startswith(relative + '/')
                                                for p in GENERATED), 'duplicate source/generated overlap')
        FILES[relative] = {'bytes': row['raw_size'], 'mode': int(mode, 8), 'sha256': row['raw_sha256'], 'git_blob': row['git_blob']}
        parts = relative.split('/')
        RAW_DIRS.update('/'.join(parts[:n]) for n in range(1, len(parts)))
        identities.append([name, mode, row['git_blob'], row['raw_size'], row['raw_sha256']])
        oids.extend(row['git_blob'].encode('ascii') + b'\n')
    require(len(RAW_DIRS) == 868 and len(GENERATED) == 24 and len(set(ALLOWROOTS)) == 35
            and sum(row['raw_size'] for row in manifest['files']) == 97700141
            and hashlib.sha256(encode(sorted(identities))).hexdigest() == RAW_IDENTITY
                == outer['source_before']['raw_identity_sha256'] == intent['raw_source']['raw_identity_sha256'],
            'exact raw source identity/counts')
    extras = {'source.oids': outer['raw_transport']['ordered_request'][str(R / 'source.oids')],
              'source.blobs': outer['raw_transport']['response'][str(R / 'source.blobs')],
              'git-index': outer['index_copy']['copy_image']}
    require(len(oids) == 118367 and hashlib.sha256(oids).hexdigest() == extras['source.oids']['sha256']
            and [extras[p]['pin']['bytes'] for p in extras] == [118367, 97850288, 461842], 'original exact buffers/OID order')
    for name, image in extras.items():
        pin = historical(image['pin'])
        require(pin[0] == 24 and pin[2] == 0 and pin[4] == 1
                and pin[3] == stat.S_IFREG | (0o400 if name == 'git-index' else 0o600), 'original transport/index type')
        FILES[name] = {'bytes': pin[5], 'mode': pin[3], 'sha256': image['sha256'], 'original_pin': pin}
    require(len(FILES) == 2890 and sum(v['bytes'] for v in FILES.values()) == 196130638
            and not RAW_DIRS.intersection(FILES), 'exact source/buffer aggregate')
    reports = inputs['reports']
    absent = {('checkout/' + (m + '/' if m else '') + 'build/reports/detekt/detekt.' + ext)
              for m in MODULES for ext in ('xml', 'html', 'sarif')}
    require(reports == phases[1]['reports'] and reports['preserved'] is True and len(reports['captures']) == 1
            and len(reports['absent']) == len(absent) == 66
            and {'checkout/' + row['source'] for row in reports['absent']} == absent, 'original fixed report inventory')
    kept = reports['captures'][0]
    require(kept['source'] == PROBLEM.removeprefix('checkout/') and kept['destination'] == 'reports/gradle-problems.html'
            and kept['original']['sha256'] == kept['saved']['sha256'] == PROBLEM_SHA
            and kept['original']['pin']['bytes'] == kept['saved']['pin']['bytes'] == 130495, 'sole original/saved report')
    original = historical(kept['original']['pin'])
    FILES[PROBLEM] = {'bytes': 130495, 'mode': original[3], 'sha256': PROBLEM_SHA, 'original_pin': original}
    REQUIRED_DIRS.update(RAW_DIRS | {'checkout/build', 'checkout/build/reports', 'checkout/build/reports/problems'})
    capture(E / kept['destination'], MIB, PROBLEM_SHA, historical(kept['saved']['pin']))
    record.update(original_cohort_settlement_bound=True, original_reports_preserved=True,
                  saved_report=kept, source_commit=COMMIT, source_tree=TREE, raw_source_identity_sha256=RAW_IDENTITY,
                  source_checkout_eol_qualifications='PRESERVED; raw fields only; T checkout pins are not R pins')
    return absent, kept


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
            and request['no_audit_build_or_ci'] is True and request['original_runtime_writer_freeze'] is True
            and request['original_settled_cohort_only'] is True
            and request['unidentified_churn_not_attributed_or_waived'] is True
            and request['acknowledged_original_failure_flags'] == FAILURE_FLAGS, 'fresh root original-only coordination incomplete')
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(fp(os.fstat(lock_fd)) == tuple(request['lock_pin']) == LOCK_PIN
            == fp(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock before flock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    coordination(request)
    # Exclusive permanent evidence directory consumes this admitted attempt
    # BEFORE any R access; it is never removed or reused by this source.
    os.mkdir(OUT.name, 0o700, dir_fd=directory(E))
    directory(OUT)
    receipt_fd = os.open('RESULT.json', os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                         0o600, dir_fd=directory(OUT))
    record.update(utc=datetime.datetime.now(datetime.timezone.utc).isoformat(), status='BINDING_RETAINED_EVIDENCE',
                  source_sha256=source_hash, request_sha256=hashlib.sha256(request_raw).hexdigest(),
                  approval_sha256=hashlib.sha256(approval_raw).hexdigest(),
                  inputs_sha256={key: digest for key, (_, digest) in INPUTS.items()},
                  text_inputs_sha256={str(path): digest for path, digest in TEXT_INPUTS.items()},
                  original_root_pins={str(p): pin for p, pin in PINS.items()})
    flush()
    os.fsync(directory(E))
    inputs = {key: decode(capture(path, 2 * MIB, digest)) for key, (path, digest) in INPUTS.items()}
    for path, digest in TEXT_INPUTS.items():
        capture(path, MIB, digest)
    absent, kept = bind_retained(inputs)
    coordination(request)
    record.update(retained_bindings_verified=True, mount_before=mounts(), status='VERIFYING_WHOLE_CURRENT_R')
    flush()
    root, parent = directory(R), directory(R.parent)
    snapshot(root, '')
    require(set(FILES).issubset(SNAPSHOT) and REQUIRED_DIRS.issubset(SNAPSHOT)
            and absent.isdisjoint(SNAPSHOT) and runtime_bytes == 196261133, 'complete fresh source/report/inventory verification')
    expected_files = sum(item[0] == 'f' for item in SNAPSHOT.values())
    expected_dirs = len(SNAPSHOT) - expected_files
    inventory = encode(SNAPSHOT)
    require(len(inventory) <= LIMITS['inventory_bytes'], 'final inventory cap')
    record.update(inventory_retained=retain_inventory(inventory), inventory_entries=len(SNAPSHOT),
                  expected_files=expected_files, expected_directories_including_R=expected_dirs,
                  expected_logical_bytes=logical_bytes, runtime_bytes_hashed=runtime_bytes,
                  fresh_raw_source_verified=True, fixed_analyzer_reports_absent=66,
                  status='WHOLE_INVENTORY_RETAINED_NO_DELETION_YET')
    flush()
    walk_inventory(root, '', deleting=False)  # Full metadata/membership pass before the first unlink.
    capture(E / kept['destination'], MIB, PROBLEM_SHA, historical(kept['saved']['pin']))
    coordination(request)
    directory(R)
    record.update(mount_before_delete=mounts(), status='DELETING_VERIFIED_WHOLE_CURRENT_R')
    flush()
    walk_inventory(root, '', deleting=True)
    check()
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
    require(record['removed_files'] == expected_files and record['removed_directories_including_R'] == expected_dirs
            and record['removed_logical_bytes'] == logical_bytes, 'final removal accounting')
    capture(E / kept['destination'], MIB, PROBLEM_SHA, historical(kept['saved']['pin']))
    record.update(mount_after=mounts(), status='CURRENT_WHOLE_R_REMOVED_PENDING_INDEPENDENT_RECONCILIATION',
                  qualification='Only original current Detekt02 temporary R removed; W/T/E and original failure/churn preserved. '
                                'Ordinary private Gradle logs/cache metadata are not new diagnostic evidence solely by extension. '
                                'No test/Detekt PASS, global-idle/no-escape, physical-space or hostile-root/atomic-unlink proof.')


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
        if CANCELLED or signal.sigpending() & signals or time.monotonic() - START >= LIMITS['seconds']:
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
