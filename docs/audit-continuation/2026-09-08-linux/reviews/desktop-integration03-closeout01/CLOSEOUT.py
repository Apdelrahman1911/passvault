#!/usr/bin/python3.12
"""NEW current GUI03 disposable-copy closeout01; inert candidate, not admission.

Root must promote these exact bytes to SELF, bind fresh admission and obtain an
independent exact approval. No old helper import/replay, child, Git, build, stop,
process scan/adoption/signal, namespace entry or mount action. Cooperative frozen
instance only; original failure/false flags and W/T/E originals stay unchanged.
"""
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import signal
import stat
import sys
import time

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
D = B / 'reviews/desktop-integration03-closeout01'
SELF, PLAN, REQUEST, ACCEPT = (D / name for name in ('CLOSEOUT.py', 'PLAN.md', 'REQUEST.json', 'ACCEPT.json'))
ADMISSION = D / 'ROOT-ADMISSION.json'
R = Path('/root/projects/PassVault/audit-runtime-linux-desktop-integration03')
E, OUT = B / 'runs/linux-desktop-integration03', B / 'runs/linux-desktop-integration03/closeout01'
LOCK = R.parent / '.audit-coordination-linux-20260908/build.lock'
SLOT = B / 'EXECUTION_SLOT.json'
SOURCE = B / 'reviews/checkpoint17/source-prepare01/SOURCE.json'
OUTER = B / 'reviews/desktop-integration03-outer/LAUNCH.py'
INNER, INIT = W / 'scripts/audit/linux_desktop_integration_03.py', W / 'scripts/audit/desktop_integration_03.init.gradle'
OLD_REQUEST = B / 'requests/LINUX-DESKTOP-INTEGRATION-03.json'
OLD_ACCEPT = B / 'reviews/editor-independent/LINUX-DESKTOP-INTEGRATION03-INSTANCE-ACCEPT.json'
EXTERNAL = B / 'reviews/desktop-integration03/c17-preflight/EXTERNAL-RESULT.json'
ACTUAL = B / 'reviews/team20/detekt_actual/gui03-actual/RECONCILIATION.json'
ACTUAL_REPORT = ACTUAL.parent / 'REPORT.md'
ACTUAL_SHA = 'd94b78c84909e7e2728e4ad4da2eae874814f573a01e005ded088f2d88302830'
REPORT_SHA = 'b57117b895dce042f5be56f8da502cd3817ebe40373211c932778eab3af7c839'
MANIFEST_SHA = 'e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f'
COMMIT, TREE = '0563e31adc9a66aefc3e74b99b9a24d17bdcdd49', 'd1bd6ca5b18d08ff3ff15896d9a78af9016be799'
RAW_IDENTITY = 'ba8d76961cec8b96dcd168880bd2a456175fcbbfbbb50fb217f991c8ff49dd9a'
PURPOSE, REVIEWER = 'ONE_CURRENT_GUI03_DISPOSABLE_CLOSEOUT01', '/root/pva031_review'
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
LOCK_PIN = (24, 14189001, 0, 33152, 1, 0, 1788910891124735946, 1788910891124735946)
OLD_DOMAIN = {'mnt': 'mnt:[4026532115]', 'pid': 'pid:[4026532116]'}
MIB = 1024 ** 2
LIMITS = {'seconds': 300, 'entries': 40000, 'inventory_bytes': 16 * MIB, 'depth': 64,
          'runtime_hash_bytes': 256 * MIB, 'generated_bytes': 2048 * MIB,
          'generated_file_bytes': 512 * MIB, 'receipt_bytes': 128 * 1024,
          'admission_age_seconds': 900, 'launch_disk_GiB': 12, 'running_disk_GiB': 8,
          'launch_mem_percent': 25, 'running_mem_percent': 20}
PRIVATE = 'home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state'.split()
GUI = 'mainnav pvu003 pva027'.split()
WORKERS, WORKER_PRIVATE = ('mainnav', 'editor-room'), 'home tmp jna sqlite xdg-cache xdg-config xdg-data xdg-state'.split()
ALLOCATED = {'', 'checkout', 'workers', *PRIVATE, *GUI}
ALLOCATED.update('workers/' + worker for worker in WORKERS)
ALLOCATED.update('workers/' + worker + '/' + name for worker in WORKERS for name in WORKER_PRIVATE)
EMPTY = set(GUI) | {'workers/' + worker + '/' + name for worker in WORKERS for name in WORKER_PRIVATE}
GENERATED = PRIVATE + '''checkout/.gradle checkout/.kotlin checkout/build
checkout/app-android/build checkout/app-desktop/build checkout/shared/build checkout/core/build checkout/feature/build
checkout/core/domain/build checkout/core/database/build checkout/core/crypto/build checkout/core/security/build
checkout/core/designsystem/build checkout/core/navigation/build checkout/core/otp/build checkout/core/testing/build
checkout/feature/onboarding/build checkout/feature/unlock/build checkout/feature/vault/build
checkout/feature/credential/build checkout/feature/generator/build checkout/feature/health/build
checkout/feature/settings/build checkout/feature/backup/build'''.split()
E_DIRS = {'', 'logs', 'xml', 'mainnav-evidence', 'pvu003-evidence', 'pva027-evidence'}
E_FILES = set('''CANCEL GUI-CLEANUP.json INNER-PREFLIGHT.json INNER-RESULT.json MAINNAV-RESULT.json
OUTER-ALLOCATION.json OUTER-INTENT.json OUTER-RECEIPT.json PHASE-prepare.json PVA027-RESULT.json
PVU003-RESULT.json SOURCE-BEFORE.json prepare-INTENT.json prepare-stop-INTENT.json
logs/outer-git.stderr logs/outer-unshare.stderr logs/outer-unshare.stdout logs/prepare.log logs/prepare-stop.log'''.split())
PINS = {R: (23, 1552647, 0, 16832), R.parent: (23, 498323, 0, 16877),
        R / 'checkout': (23, 1552653, 0, 16832), E: (23, 1552646, 0, 16832),
        E.parent: (23, 642474, 0, 16832), LOCK.parent: (23, 661121, 0, 16832)}
ODIR = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
OREAD = os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC
START, CANCELLED, last_resource = time.monotonic(), False, 0.0
DIRECTORIES, KEPT, FILES, REQUIRED, SNAPSHOT, CHILDREN, E_CHILDREN = {}, {}, {}, {}, {}, {}, {}
SOURCE_DIRS, BASE_DIRS, SEEN_INODES = set(), set(), set()
runtime_bytes, generated_bytes, inventory_bytes = 0, 0, 64
lock_fd, receipt_fd, receipt_identity = None, None, None
out_created, inventory_saved = False, False
record = {'schema': 1, 'purpose': PURPOSE, 'status': 'HOLD_CONSUMED_NO_AUTOMATIC_RETRY',
          'removed_files': 0, 'removed_directories_including_R': 0, 'removed_logical_bytes': 0,
          'removed_R': False, 'builds_tests_additional_stops': 0, 'resources': [],
          'descriptor_close_uncertainty': False, 'original_failed_records': 'UNCHANGED'}


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def encode(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), allow_nan=False).encode() + b'\n'


def decode(raw):
    def unique(pairs):
        result = {}
        for key, value in pairs:
            require(key not in result, 'duplicate JSON key')
            result[key] = value
        return result
    return json.loads(raw, object_pairs_hook=unique, parse_constant=lambda _: require(False, 'nonfinite JSON'))


def dp(s):
    return s.st_dev, s.st_ino, s.st_uid, s.st_mode


def fp(s):
    return dp(s) + (s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns)


def historical(pin, is_dir=False):
    keys = ('dev', 'ino', 'uid', 'mode') if is_dir else ('dev', 'ino', 'uid', 'mode', 'nlink', 'bytes', 'mtime_ns', 'ctime_ns')
    require(all(type(pin[key]) is int for key in keys), 'integer historical pin')
    return tuple(pin[key] for key in keys)


def close_fd(fd):
    try:
        os.close(fd)
    except OSError:
        record['descriptor_close_uncertainty'] = True
        raise


def resources(launch=False):
    global last_resource
    last_resource = time.monotonic()
    fs = os.statvfs(R.parent)
    with open('/proc/meminfo', 'rb') as stream:
        raw = stream.read(65537)
    require(0 < len(raw) <= 65536 and raw.endswith(b'\n'), 'complete bounded meminfo')
    values = {}
    for line in raw.splitlines():
        fields = line.split()
        if fields and fields[0] in (b'MemTotal:', b'MemAvailable:'):
            require(len(fields) == 3 and fields[2] == b'kB' and fields[0] not in values, 'memory row')
            values[fields[0]] = int(fields[1]) * 1024
    available, total = values[b'MemAvailable:'], values[b'MemTotal:']
    disk = fs.f_bavail * fs.f_frsize
    require(0 < total and 0 <= available <= total and len(record['resources']) < 305, 'resource observation cap')
    record['resources'].append({'elapsed_seconds': round(last_resource - START, 3), 'launch': launch,
                                'disk_available_bytes': disk, 'memory_available_bytes': available, 'memory_total_bytes': total})
    require(disk >= LIMITS['launch_disk_GiB' if launch else 'running_disk_GiB'] * 1024 ** 3
            and available * 100 >= total * LIMITS['launch_mem_percent' if launch else 'running_mem_percent'], 'resource floor')


def check():
    require(not CANCELLED and time.monotonic() - START < LIMITS['seconds'], 'cancelled/300s cooperative deadline')
    if time.monotonic() - last_resource >= 1:
        resources()


def directory(path):
    check()
    parent = None if path == Path('/') else directory(path.parent)
    if path not in DIRECTORIES:
        fd = os.open('/' if parent is None else path.name, ODIR, dir_fd=parent)
        try:
            pin = dp(os.fstat(fd))
            require(pin[2] == 0 and not pin[3] & 0o022, 'unsafe directory owner/mode')
            DIRECTORIES[path] = (fd, pin)
        except BaseException:
            close_fd(fd)
            raise
    fd, pin = DIRECTORIES[path]
    require(dp(os.fstat(fd)) == pin and (path not in PINS or pin == PINS[path])
            and (parent is None or dp(os.stat(path.name, dir_fd=parent, follow_symlinks=False)) == pin),
            'original/fresh directory binding changed')
    return fd


def capture(path, cap, digest=None, original=None, remember=True):
    parent = directory(path.parent)
    before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
    require(stat.S_ISREG(before.st_mode) and before.st_dev == 24 and before.st_uid == 0 and before.st_nlink == 1
            and not before.st_mode & 0o7022 and 0 <= before.st_size <= cap, 'bounded safe retained input')
    fd = os.open(path.name, OREAD, dir_fd=parent)
    try:
        require(fp(os.fstat(fd)) == fp(before) and (original is None or fp(before) == tuple(original)), 'input open/original pin')
        data = bytearray()
        while part := os.read(fd, 65536):
            check()
            data.extend(part)
            require(len(data) <= cap, 'retained input cap')
        image = hashlib.sha256(data).hexdigest()
        require(len(data) == before.st_size and (digest is None or image == digest)
                and fp(before) == fp(os.fstat(fd)) == fp(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'retained input drift')
        if remember:
            value = (cap, image, fp(before))
            require(path not in KEPT or KEPT[path] == value, 'intake image changed')
            KEPT[path] = value
        return bytes(data)
    finally:
        close_fd(fd)


def names(fd):
    result = []
    with os.scandir(fd) as entries:
        for entry in entries:
            check()
            require(len(result) < LIMITS['entries'], 'directory entry cap')
            result.append(entry.name)
    return sorted(result)


def safe_relative(name):
    require(isinstance(name, str) and 0 < len(name.encode('utf-8')) <= 8192
            and len(name.split('/')) <= LIMITS['depth'] and not name.startswith('/')
            and not any(ord(c) < 32 or ord(c) == 127 for c in name)
            and all(part not in ('', '.', '..', '.git') for part in name.split('/')), 'unsafe relative member')


def protected():
    for path, (cap, digest, pin) in tuple(KEPT.items()):
        capture(path, cap, digest, pin, remember=False)
    for relative, expected in E_CHILDREN.items():
        allowed = expected + ([OUT.name] if relative == '' and out_created else [])
        require(names(directory(E / relative)) == sorted(allowed), 'original E membership changed')
    if out_created:
        require(names(directory(OUT)) == sorted(['RESULT.json'] + (['INVENTORY.json'] if inventory_saved else [])),
                'new output membership changed')


def mounts():
    check()
    with open('/proc/self/mountinfo', 'rb') as stream:
        raw = stream.read(4 * MIB + 1)
    require(0 < len(raw) <= 4 * MIB and raw.endswith(b'\n'), 'complete bounded mount table')
    lines = raw.splitlines()
    require(len(lines) <= 8192, 'mount row cap')
    for line in lines:
        check()
        halves = line.split(b' - ')
        require(len(halves) == 2 and len(halves[0].split()) >= 6, 'mountinfo prefix')
        suffix = halves[1].split(b' ')  # A legitimate empty SOURCE is not missing grammar.
        require(len(suffix) == 3 and suffix[0] and suffix[2], 'mountinfo suffix')
        point = halves[0].split()[4]
        require(not re.search(rb'\\(?![0-7]{3})', point), 'mountpoint escape')
        point = re.sub(rb'\\([0-7]{3})', lambda m: bytes([int(m[1], 8)]), point)
        require(point.startswith(b'/') and not point.startswith(b'//') and b'\0' not in point
                and not {b'.', b'..'}.intersection(point.split(b'/')), 'mountpoint path')
        require(point != os.fsencode(R) and not point.startswith(os.fsencode(R) + b'/'), 'mount at/below R: HOLD')
    return {'sha256': hashlib.sha256(raw).hexdigest(), 'rows': len(lines), 'runtime_mounts': 0}


def coordination(request, admission):
    check()
    require(fp(os.fstat(lock_fd)) == LOCK_PIN == tuple(request['lock_pin'])
            == fp(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock binding')
    slot = capture(SLOT, MIB, request['slot_sha256'])
    require(decode(slot)['state'] == 'IDLE_NO_AUDIT_BUILD_ADMITTED', 'slot is not bound idle owner state')
    current = {key: os.readlink('/proc/self/ns/' + key) for key in ('mnt', 'pid', 'net')}
    require(current == request['parent_namespaces'] == admission['parent_namespaces'], 'fresh parent namespace binding')
    stamp = admission['observed_monotonic_ns']
    require(type(stamp) is int and 0 <= time.monotonic_ns() - stamp <= LIMITS['admission_age_seconds'] * 10 ** 9,
            'current origin/reference admission expired')


def intake():
    global lock_fd
    request_raw, accept_raw = capture(REQUEST, MIB), capture(ACCEPT, MIB)
    request, approval = decode(request_raw), decode(accept_raw)
    source_hash = hashlib.sha256(capture(SELF, MIB)).hexdigest()
    require(request['purpose'] == PURPOSE and request['author'] == '/root' and request['state'] == 'BOUND_FOR_EXACT_REVIEW'
            and request['source_sha256'] == source_hash and request['limits'] == LIMITS
            and request['actual_review_sha256'] == ACTUAL_SHA, 'unbound/stale request')
    require(all(isinstance(request[key], str) and re.fullmatch(r'[0-9a-f]{64}', request[key]) is not None
                for key in ('plan_sha256', 'root_admission_sha256', 'slot_sha256'))
            and isinstance(request['root_admission_pin'], list) and len(request['root_admission_pin']) == 8
            and all(type(value) is int for value in request['root_admission_pin']), 'unbound hashes/admission pin')
    capture(PLAN, MIB, request['plan_sha256'])
    require(approval == {'purpose': PURPOSE, 'reviewer': REVIEWER, 'disposition': 'ACCEPT_EXACT_ONCE',
                        'request_sha256': hashlib.sha256(request_raw).hexdigest(), 'source_sha256': source_hash,
                        'plan_sha256': request['plan_sha256'], 'root_admission_sha256': request['root_admission_sha256']},
            'missing exact independent approval')
    admission = decode(capture(ADMISSION, MIB, request['root_admission_sha256'], request['root_admission_pin']))
    require(admission['format'] == 'passvault-gui03-closeout-root-admission-v1' and admission['author'] == '/root'
            and admission['state'] == 'BOUND_FOR_EXACT_REVIEW'
            and admission['purpose'] == PURPOSE and admission['actual_review_adopted_sha256'] == ACTUAL_SHA
            and admission['runtime_path'] == str(R) and tuple(admission['original_runtime_pin']) == PINS[R]
            and admission['historical_original_domain'] == OLD_DOMAIN and admission['slot_sha256'] == request['slot_sha256']
            and tuple(admission['original_lock_pin']) == LOCK_PIN, 'current original-instance admission binding')
    for field in ('all_original_commands_terminal', 'original_required_stop_already_settled',
                  'original_runtime_frozen_since_gui03_terminal', 'exclusive_cooperative_writer_freeze_maintained',
                  'current_original_domain_references_absent', 'current_runtime_write_references_and_aliases_absent',
                  'sole_root_closeout_owner', 'no_concurrent_build_test_or_ci'):
        require(admission[field] is True, 'missing fresh root admission: ' + field)
    require(admission['additional_stop_authorized'] is False and admission['unclassified_reference_observations'] == []
            and isinstance(admission['observation_tool_packets'], list) and 0 < len(admission['observation_tool_packets']) <= 32,
            'fresh reference evidence absent/uncertain; no extra stop')
    require(all(isinstance(row, dict) and row.get('exit_code') == 0 and 'session_id' not in row
                and isinstance(row.get('chunk_id'), str) and row['chunk_id']
                and isinstance(row.get('output'), str) and 0 < len(row['output'].encode()) <= 65536
                for row in admission['observation_tool_packets']), 'complete bounded actual observation packets required')
    # The exact reviewer must assess these root packets; booleans/old namespace numbers are not process-reference proof.
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(fp(os.fstat(lock_fd)) == LOCK_PIN == tuple(request['lock_pin'])
            == fp(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock before flock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    coordination(request, admission)
    record.update(source_sha256=source_hash, request_sha256=hashlib.sha256(request_raw).hexdigest(),
                  approval_sha256=hashlib.sha256(accept_raw).hexdigest(), root_admission_sha256=request['root_admission_sha256'])
    return request, admission


def retained_inputs(request, admission):
    actual = decode(capture(ACTUAL, 2 * MIB, ACTUAL_SHA))
    capture(ACTUAL_REPORT, 2 * MIB, REPORT_SHA)
    require(actual['format'] == 'passvault-gui03-independent-retained-reconciliation-v1'
            and actual['reviewer'] == '/root/detekt_actual' and actual['failed_checks'] == []
            and actual['disposition']['actual_record'] == 'ACCEPT_RETAINED_TERMINAL_FAILURE_ONLY'
            and actual['disposition']['current_runtime'] == 'HOLD_NOT_ATTEMPTED_NO_CLEANUP_ADMISSION'
            and actual['commit'] == COMMIT and actual['tree'] == TREE, 'original actual failure acceptance')
    kept = actual['retained_inventory']
    require(set(kept['images']) == E_FILES and set(kept['directory_pins']) == ({'.'} | (E_DIRS - {''}))
            and kept['files'] == 19 and kept['bytes'] == 48396, 'exact retained E inventory')
    for relative, value in kept['directory_pins'].items():
        path = E if relative == '.' else E / relative
        pin = historical(value, True)
        require(pin[0] == 23 and pin[2:] == (0, 16832) and (path not in PINS or pin == PINS[path]), 'original E directory')
        PINS[path] = pin
    E_CHILDREN.update({relative: [] for relative in E_DIRS})
    for relative in E_FILES | (E_DIRS - {''}):
        parent, _, name = relative.rpartition('/')
        E_CHILDREN[parent].append(name)
    saved = {relative: capture(E / relative, 2 * MIB, image['sha256'], historical(image['pin']))
             for relative, image in kept['images'].items()}
    bound = actual['bound_images']
    require(set(bound) == {str(p) for p in (SOURCE, OUTER, INNER, INIT, OLD_ACCEPT, EXTERNAL)}, 'fixed retained control set')
    buffers = {Path(path): capture(Path(path), 2 * MIB, image['sha256'], historical(image['pin']))
               for path, image in bound.items()}
    require(bound[str(SOURCE)]['sha256'] == MANIFEST_SHA, 'C17 raw manifest binding')
    outer, inner, allocation, intent, phase, gui = (decode(saved[name]) for name in
        ('OUTER-RECEIPT.json', 'INNER-RESULT.json', 'OUTER-ALLOCATION.json', 'OUTER-INTENT.json', 'PHASE-prepare.json', 'GUI-CLEANUP.json'))
    capture(OLD_REQUEST, MIB, outer['request']['sha256'], historical(outer['request']['pin']))
    require(outer['allocation'] == allocation and outer['status'] == 'HOLD' and outer['cleanup'] == 'NOT_ATTEMPTED'
            and [child['exit'] for child in outer['children']] == [0, 70]
            and outer['children'][1]['owned_namespaces'] == OLD_DOMAIN
            and outer['parent_namespaces'] == request['parent_namespaces'] == admission['parent_namespaces'], 'original outer settlement')
    require(inner['all_required_stops_ok'] is True and inner['namespace_empty_before_exit'] is True
            and inner['source_before'] is True and inner['source_after'] is False and inner['cleanup_safe'] is False
            and inner['gui_attempted'] is False and inner['render_attempted'] is False and inner['preserved_xml_suites'] == 0
            and phase['settled'] is True and phase['stop_ok'] is True and phase['stop_attempted'] is True
            and [(row['exit'], row['complete']) for row in phase['commands']] == [(143, True), (0, True)]
            and gui['gui_attempted'] is False and gui['namespace_empty'] is True and gui['helpers'] == []
            and gui['original_native_owners'] == [] and gui['original_native_owner_descriptors_settled'] is False
            and gui['remaining_original_descendants'] == [], 'preserve original failed/false GUI facts')
    require(decode(buffers[EXTERNAL])['final']['exit_code'] == 70, 'actual original external exit')
    for path in (R, E):
        require(historical(allocation['allocated_directories'][str(path)], True) == PINS[path], 'original root allocation')
    for path in (R.parent, E.parent, LOCK.parent):
        require(historical(allocation['parents'][str(path)], True) == PINS[path], 'original parent allocation')
    expected = {R / relative for relative in ALLOCATED} | {E / relative for relative in E_DIRS}
    require(set(intent['directories']) == {str(p) for p in expected}, 'all 41 original allocated directories')
    for path in expected:
        pin = historical(intent['directories'][str(path)], True)
        require(pin[0] == 23 and pin[2:] == (0, 16832) and (path not in PINS or PINS[path] == pin), 'original allocated directory pin')
        PINS[path] = pin
    record.update(original_actual_sha256=ACTUAL_SHA, original_external_exit=70, original_prepare_exit=143,
                  original_stop_exit=0, original_source_after=False, original_cleanup_safe=False,
                  original_native_owner_descriptors_settled=False, original_gui_render_attempted=False,
                  application_cases=0, preserved_original_E_files=19, preserved_original_E_bytes=48396)
    return decode(buffers[SOURCE]), outer, intent


def derive(manifest, outer, intent):
    require(manifest['commit'] == COMMIT and manifest['tree'] == TREE and len(manifest['files']) == 3042, 'C17 source count')
    SOURCE_DIRS.update(('', 'checkout'))
    identities, oids = [], bytearray()
    for row in manifest['files']:
        check()
        name, mode = row['path'], row['git_mode']
        safe_relative(name)
        require(mode in ('100644', '100755') and type(row['raw_size']) is int and 0 <= row['raw_size'] <= 32 * MIB
                and re.fullmatch(r'[0-9a-f]{40}', row['git_blob']) is not None
                and re.fullmatch(r'[0-9a-f]{64}', row['raw_sha256']) is not None, 'raw source row')
        relative = 'checkout/' + name
        require(relative not in FILES, 'duplicate raw source member')
        FILES[relative] = {'class': 'source', 'bytes': row['raw_size'], 'mode': int(mode, 8),
                           'sha256': row['raw_sha256'], 'git_blob': row['git_blob']}
        parts = relative.split('/')
        SOURCE_DIRS.update('/'.join(parts[:index]) for index in range(1, len(parts)))
        identities.append([name, mode, row['git_blob'], row['raw_size'], row['raw_sha256']])
        oids.extend(row['git_blob'].encode('ascii') + b'\n')
    require(len(SOURCE_DIRS) == 882 and sum(item['bytes'] for item in FILES.values()) == 103345745
            and hashlib.sha256(encode(sorted(identities))).hexdigest() == RAW_IDENTITY
            == outer['source_before']['raw_identity_sha256'] == intent['raw_source']['raw_identity_sha256'], 'raw source derivation')
    require(len(GENERATED) == len(set(GENERATED)) == 35 and len(ALLOCATED) == 35 and len(EMPTY) == 19, 'fixed allocation/classification count')
    for prefix in GENERATED:
        require(not any(name == prefix or name.startswith(prefix + '/') or prefix.startswith(name + '/') for name in FILES),
                'generated prefix intersects raw source')
        require(not any(prefix != other and prefix.startswith(other + '/') for other in GENERATED), 'overlapping generated roots')
    transports = {'source.oids': outer['raw_transport']['ordered_request'][str(R / 'source.oids')],
                  'source.blobs': outer['raw_transport']['response'][str(R / 'source.blobs')]}
    require(len(oids) == 124722 and hashlib.sha256(oids).hexdigest() == transports['source.oids']['sha256']
            == '6dddf0bc1d59e425fa85fd0eafad02deec739aa741c05b28b0927cb4b737039e'
            and transports['source.blobs']['sha256'] == 'a3780669d4fe01d403b76452bd6b3f81e51b4f46ca91b3322dabcf63db60cc7b',
            'original ordered OID/raw transport images')
    for name, image in transports.items():
        pin = historical(image['pin'])
        require(pin[0] == 24 and pin[2:5] == (0, 33152, 1)
                and pin[5] == (124722 if name == 'source.oids' else 103503964), 'original transport metadata')
        FILES[name] = {'class': 'transport', 'bytes': pin[5], 'mode': pin[3], 'sha256': image['sha256'], 'original_pin': pin}
    BASE_DIRS.update(SOURCE_DIRS | ALLOCATED)
    require(not BASE_DIRS.intersection(FILES) and len(BASE_DIRS) == 915 and len(FILES) == 3044
            and sum(item['bytes'] for item in FILES.values()) == 206974431, 'complete mandatory baseline')
    REQUIRED.update({relative: set() for relative in BASE_DIRS})
    for relative in set(FILES) | (BASE_DIRS - {''}):
        parent, _, name = relative.rpartition('/')
        REQUIRED[parent].add(name)
    record.update(source_commit=COMMIT, source_tree=TREE, raw_identity_sha256=RAW_IDENTITY,
                  mandatory_files=3044, mandatory_directories_including_R=915, mandatory_logical_bytes=206974431,
                  generated_prefixes=GENERATED, original_allocated_runtime_directories=35)


def generated(relative):
    return any(relative == prefix or relative.startswith(prefix + '/') for prefix in GENERATED)


def reject_generated_evidence(relative, is_file):
    parts, name = relative.split('/'), relative.rsplit('/', 1)[-1]
    require(not {'reports', 'test-results', 'evidence', 'screenshots', 'crashes', 'coredumps'}.intersection(parts)
            and not name.startswith(('TEST-', 'hs_err_pid', 'replay_pid'))
            and not (is_file and (name == 'core' or name.startswith('core.')))
            and not name.endswith(('.hprof', '.jfr', '.dmp', '.dump', '.core', '.sarif')),
            'unexpected generated report/test/crash evidence: HOLD before deletion')


def add(relative, value):
    global inventory_bytes
    require(relative not in SNAPSHOT and len(SNAPSHOT) < LIMITS['entries'], 'duplicate/inventory entry cap')
    identity = (value[2][0], value[2][1])
    require(identity not in SEEN_INODES, 'duplicate runtime device/inode: HOLD')
    inventory_bytes += len(encode(relative)) + len(encode(value)) + 2
    require(inventory_bytes <= LIMITS['inventory_bytes'], 'incremental inventory cap')
    SEEN_INODES.add(identity)
    SNAPSHOT[relative] = value


def verify_file(parent, name, relative):
    global runtime_bytes
    expected = FILES[relative]
    before = os.stat(name, dir_fd=parent, follow_symlinks=False)
    require(before.st_dev == 24 and before.st_uid == 0 and before.st_nlink == 1 and before.st_mode == expected['mode']
            and before.st_size == expected['bytes'] and ('original_pin' not in expected or fp(before) == expected['original_pin']),
            'raw source/original transport metadata')
    fd = os.open(name, OREAD, dir_fd=parent)
    try:
        require(fp(os.fstat(fd)) == fp(before), 'raw file changed at open')
        digest, blob, size = hashlib.sha256(), hashlib.sha1(b'blob ' + str(before.st_size).encode() + b'\0'), 0
        while part := os.read(fd, 65536):
            check()
            size += len(part)
            runtime_bytes += len(part)
            require(size <= expected['bytes'] and runtime_bytes <= LIMITS['runtime_hash_bytes'], 'bounded raw stream')
            digest.update(part)
            blob.update(part)
        require(size == expected['bytes'] and digest.hexdigest() == expected['sha256']
                and ('git_blob' not in expected or blob.hexdigest() == expected['git_blob'])
                and fp(before) == fp(os.fstat(fd)) == fp(os.stat(name, dir_fd=parent, follow_symlinks=False)), 'raw file/hash drift')
        add(relative, ['f', expected['class'], fp(before), digest.hexdigest()])
    finally:
        close_fd(fd)


def snapshot(fd, relative, depth=0):
    global generated_bytes, inventory_bytes
    check()
    require(depth <= LIMITS['depth'], 'snapshot depth cap')
    pin, path = dp(os.fstat(fd)), R / relative
    require(pin[0] == 23 and pin[2] == 0 and stat.S_ISDIR(pin[3]) and not pin[3] & 0o7022
            and (relative not in SOURCE_DIRS or pin[3] == 16832)
            and (path not in PINS or pin == PINS[path]), 'runtime directory type/origin/mode')
    add(relative, ['d', 'mandatory' if relative in BASE_DIRS else 'generated', pin])
    observed = names(fd)
    require(REQUIRED.get(relative, set()) <= set(observed) and (relative not in EMPTY or not observed), 'missing source/allocation or populated GUI-only directory')
    inventory_bytes += len(encode(relative)) + len(encode(observed)) + 2
    require(inventory_bytes <= LIMITS['inventory_bytes'], 'incremental child-membership cap')
    CHILDREN[relative] = observed
    for name in observed:
        check()
        child = relative + '/' + name if relative else name
        safe_relative(child)
        if child in FILES:  # Exact raw copies, including tracked old reports/tests, precede generated-name rejection.
            verify_file(fd, name, child)
            continue
        required_dir = child in BASE_DIRS or child in GENERATED
        require(required_dir or generated(child), 'unclassified runtime entry: HOLD')
        before = os.stat(name, dir_fd=fd, follow_symlinks=False)
        if not required_dir:
            reject_generated_evidence(child, stat.S_ISREG(before.st_mode))
        if stat.S_ISDIR(before.st_mode):
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                require(dp(os.fstat(sub)) == dp(before), 'runtime directory open drift')
                snapshot(sub, child, depth + 1)
                require(dp(os.stat(name, dir_fd=fd, follow_symlinks=False)) == dp(before), 'runtime directory name drift')
            finally:
                close_fd(sub)
        else:
            require(not required_dir and stat.S_ISREG(before.st_mode) and before.st_dev == 24 and before.st_uid == 0
                    and before.st_nlink == 1 and not before.st_mode & 0o7022
                    and 0 <= before.st_size <= LIMITS['generated_file_bytes'], 'generated type/link/device/mode/size')
            generated_bytes += before.st_size
            require(generated_bytes <= LIMITS['generated_bytes'], 'generated logical byte cap')
            add(child, ['f', 'generated', fp(before), None])
    require(names(fd) == observed and dp(os.fstat(fd)) == pin, 'snapshot membership/identity drift')


def write_all(fd, raw, cooperative=True):
    at = 0
    while at < len(raw):
        if cooperative:
            check()
        count = os.write(fd, raw[at:at + 65536])
        require(count > 0, 'zero retained write')
        at += count


def flush():
    if receipt_fd is None:
        return
    try:
        record['elapsed_seconds'] = round(time.monotonic() - START, 3)
        raw = encode(record)
        require(len(raw) <= LIMITS['receipt_bytes'], 'receipt byte cap')
        parent = DIRECTORIES[OUT][0]
        require(dp(os.fstat(receipt_fd)) == receipt_identity and os.fstat(receipt_fd).st_nlink == 1
                and dp(os.stat('RESULT.json', dir_fd=parent, follow_symlinks=False)) == receipt_identity, 'receipt identity drift')
        os.lseek(receipt_fd, 0, os.SEEK_SET)
        os.ftruncate(receipt_fd, 0)
        write_all(receipt_fd, raw, cooperative=False)  # Also preserve HOLD after cancellation/deadline.
        os.fsync(receipt_fd)
        pin = fp(os.fstat(receipt_fd))
        os.lseek(receipt_fd, 0, os.SEEK_SET)
        result = bytearray()
        while part := os.read(receipt_fd, 65536):
            result.extend(part)
            require(len(result) <= len(raw), 'receipt readback cap')
        require(bytes(result) == raw and pin == fp(os.fstat(receipt_fd))
                == fp(os.stat('RESULT.json', dir_fd=parent, follow_symlinks=False)), 'receipt readback drift')
    except BaseException as error:
        record['receipt_error'] = type(error).__name__
        raise


def retain_inventory(raw):
    global inventory_saved
    require(len(raw) <= LIMITS['inventory_bytes'], 'final encoded inventory cap')
    parent = directory(OUT)
    fd = os.open('INVENTORY.json', os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=parent)
    try:
        s = os.fstat(fd)
        require(s.st_dev == 24 and s.st_uid == 0 and s.st_mode == 33152 and s.st_nlink == 1, 'new inventory type')
        write_all(fd, raw)
        os.fsync(fd)
        pin = fp(os.fstat(fd))
        os.lseek(fd, 0, os.SEEK_SET)
        at = 0
        while part := os.read(fd, 65536):
            check()
            require(part == raw[at:at + len(part)], 'inventory readback mismatch')
            at += len(part)
            require(at <= len(raw), 'inventory readback cap')
        require(at == len(raw) and pin == fp(os.fstat(fd))
                == fp(os.stat('INVENTORY.json', dir_fd=parent, follow_symlinks=False)), 'inventory identity/size drift')
    finally:
        close_fd(fd)
    os.fsync(parent)
    inventory_saved = True
    image = hashlib.sha256(raw).hexdigest()
    KEPT[OUT / 'INVENTORY.json'] = (LIMITS['inventory_bytes'], image, pin)
    return {'path': 'closeout01/INVENTORY.json', 'bytes': len(raw), 'sha256': image, 'pin': pin}


def remove(fd, relative):
    check()
    require(dp(os.fstat(fd)) == SNAPSHOT[relative][2] and names(fd) == CHILDREN[relative], 'snapshot directory changed before removal')
    for name in CHILDREN[relative]:
        check()
        child = relative + '/' + name if relative else name
        item = SNAPSHOT[child]
        before = os.stat(name, dir_fd=fd, follow_symlinks=False)
        if item[0] == 'd':
            require(dp(before) == item[2], 'snapshot child directory changed')
            sub = os.open(name, ODIR, dir_fd=fd)
            try:
                require(dp(os.fstat(sub)) == item[2], 'removal directory open drift')
                remove(sub, child)
                require(not names(sub) and dp(os.fstat(sub)) == item[2]
                        == dp(os.stat(name, dir_fd=fd, follow_symlinks=False)), 'empty original child binding')
                os.rmdir(name, dir_fd=fd)
                record['removed_directories_including_R'] += 1
                require(os.fstat(sub).st_nlink == 0, 'original child rmdir not established')
            finally:
                close_fd(sub)
        else:
            require(fp(before) == item[2], 'snapshot original file changed')
            os.unlink(name, dir_fd=fd)
            record['removed_files'] += 1
            record['removed_logical_bytes'] += item[2][5]
        if (record['removed_files'] + record['removed_directories_including_R']) % 128 == 0:
            flush()
    require(not names(fd), 'directory not empty after admitted removals')
    os.fsync(fd)


def main():
    global out_created, receipt_fd, receipt_identity
    require(sys.argv == [str(SELF)] and sys.executable == '/usr/bin/python3.12' and sys.flags.isolated
            and sys.flags.no_site and sys.dont_write_bytecode and not sys.flags.optimize
            and os.getuid() == os.geteuid() == 0 and dict(os.environ) == ENV, 'fixed isolated root entry; inert candidate is not SELF')
    resources(True)
    request, admission = intake()
    manifest, outer, intent = retained_inputs(request, admission)
    derive(manifest, outer, intent)
    protected()
    coordination(request, admission)
    os.mkdir(OUT.name, 0o700, dir_fd=directory(E))  # Never adopt or retry an existing closeout directory.
    out_created = True
    out_fd = directory(OUT)
    require(os.fstat(out_fd).st_dev == 23 and os.fstat(out_fd).st_uid == 0 and os.fstat(out_fd).st_mode == 16832,
            'new output directory type')
    receipt_fd = os.open('RESULT.json', os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=out_fd)
    receipt_identity = dp(os.fstat(receipt_fd))
    require(receipt_identity[0] == 24 and receipt_identity[2:] == (0, 33152) and os.fstat(receipt_fd).st_nlink == 1, 'new receipt type')
    record.update(status='VERIFYING_CURRENT_GUI03_COPY', new_descriptor_reacquisition=True, mount_before=mounts())
    flush()
    os.fsync(out_fd)
    os.fsync(directory(E))
    root, parent = directory(R), directory(R.parent)  # First R access is after fresh admission and complete mount guard.
    snapshot(root, '')
    require(set(FILES) <= set(SNAPSHOT) and BASE_DIRS <= set(SNAPSHOT) and runtime_bytes == 206974431, 'full source/transport snapshot incomplete')
    record.update(snapshot_entries=len(SNAPSHOT), snapshot_files=sum(item[0] == 'f' for item in SNAPSHOT.values()),
                  snapshot_directories_including_R=sum(item[0] == 'd' for item in SNAPSHOT.values()),
                  snapshot_logical_bytes=206974431 + generated_bytes, generated_logical_bytes=generated_bytes,
                  fresh_C17_copy_verification={'verified': True, 'raw_members': 3042, 'raw_bytes': 103345745,
                                               'transport_members': 2, 'total_hashed_bytes': runtime_bytes,
                                               'raw_identity_sha256': RAW_IDENTITY, 'original_source_after_remains': False},
                  gui_only_directories_verified_empty=19)
    record['inventory'] = retain_inventory(encode({'entries': SNAPSHOT, 'children': CHILDREN}))
    protected()
    coordination(request, admission)
    directory(R)
    record.update(mount_before_delete=mounts(), status='DELETING_ONLY_INVENTORIED_CURRENT_GUI03_COPY')
    flush()
    check()
    remove(root, '')
    require(not names(root) and dp(os.fstat(root)) == PINS[R]
            == dp(os.stat(R.name, dir_fd=parent, follow_symlinks=False)), 'final empty original R binding')
    check()
    os.rmdir(R.name, dir_fd=parent)
    record['removed_directories_including_R'] += 1
    record['removed_R'] = True
    os.fsync(parent)
    require(os.fstat(root).st_nlink == 0, 'original R rmdir not established')
    try:
        os.stat(R.name, dir_fd=parent, follow_symlinks=False)
    except FileNotFoundError:
        pass
    else:
        require(False, 'R name exists after removal')
    require(record['removed_files'] == record['snapshot_files']
            and record['removed_directories_including_R'] == record['snapshot_directories_including_R']
            and record['removed_logical_bytes'] == record['snapshot_logical_bytes'], 'partial removal counts')
    protected()  # Never reacquire R by its removed pathname.
    coordination(request, admission)
    record.update(mount_after=mounts(), status='CURRENT_GUI03_COPY_REMOVED_PENDING_INDEPENDENT_RECONCILIATION',
                  qualification='Only original current R copy removed; no test/GUI/fix/closure credit. Original false flags unchanged. '
                  'Cooperative freeze, not hostile-root/atomic conditional unlink, hard syscall deadline or physical-space proof.')


if __name__ == '__main__':
    os.umask(0o077)
    watched_signals = {signal.SIGTERM, signal.SIGINT, signal.SIGHUP}
    def cancel(_number, _frame):
        global CANCELLED
        CANCELLED = True
    for signum in watched_signals:
        signal.signal(signum, cancel)
    code = 1
    try:
        main()
        code = 0
    except BaseException as error:
        record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', error=type(error).__name__ + ': ' + str(error)[:1200])
    finally:
        signal.pthread_sigmask(signal.SIG_BLOCK, watched_signals)
        if CANCELLED or signal.sigpending() & watched_signals or time.monotonic() - START >= LIMITS['seconds']:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', terminal_cancelled_or_expired=True)
            code = 1
        try:
            flush()
        except BaseException as error:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', receipt_error=type(error).__name__)
            code = 1
        descriptors = [fd for fd, _ in reversed(tuple(DIRECTORIES.values()))]
        descriptors += ([] if receipt_fd is None else [receipt_fd]) + ([] if lock_fd is None else [lock_fd])
        for fd in descriptors:
            try:
                close_fd(fd)
            except OSError:
                record['status'] = 'HOLD_CONSUMED_NO_AUTOMATIC_RETRY'
                code = 1
        if CANCELLED or signal.sigpending() & watched_signals or time.monotonic() - START >= LIMITS['seconds']:
            record.update(status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', terminal_cancelled_or_expired=True)
            code = 1
        try:
            print(encode({'external_pending_code': code, 'status': record['status'], 'error': record.get('error'),
                          'receipt_error': record.get('receipt_error'), 'descriptor_close_uncertainty': record['descriptor_close_uncertainty'],
                          'removed_R': record['removed_R'], 'removed_files': record['removed_files'],
                          'removed_directories_including_R': record['removed_directories_including_R'],
                          'removed_logical_bytes': record['removed_logical_bytes'],
                          'qualification': 'Receipt precedes final closes/actual exit; root retains full actual output for independent reconciliation. '
                                           'Any failure consumes this attempt; no automatic retry or extra stop.'}).decode(), end='')
            sys.stdout.flush()
        except BaseException:
            code = 1
    sys.exit(code)
