#!/usr/bin/python3.12
"""UNADMITTED fresh focused03-only disposal control; never a build/test/stop runner.

Standalone textual donor extraction, not import/replay. Root request, independent
exact-instance acceptance and sole live slot are mandatory. Original source_after
remains false. Fresh source proof grants disposal confidence only, no test credit.
Cooperative-root writer/mount freeze; no census/global-idle/hostile-root guarantee.
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

B = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux')
D = B / 'reviews/team20/resume_disk/capture_author/resumed02/focused03_closeout01'
R = Path('/root/projects/PassVault/audit-runtime-linux-focused-regression03')
ORIGINAL_E = B / 'runs/linux-focused-regression03'
E = ORIGINAL_E / 'closeout01'
SELF, PLAN = D / 'CLOSEOUT01-v2.py', D / 'PLAN-v2.json'
REQUEST = B / 'reviews/team20/resume_disk/root/FOCUSED03-CLOSEOUT01-REQUEST.json'
APPROVAL = B / 'reviews/team20/resume_disk/capture_review/resumed02/focused03_closeout01/FOCUSED03-CLOSEOUT01-ACCEPT.json'
SLOT = B / 'reviews/team20/resume_disk/root/EXECUTION_SLOT.json'
FLOOR = B / 'reviews/team20/resume_disk/root/SFIX-FOCUSED03-EXECUTION-ADMISSION.json'
ADOPTION = B / 'reviews/team20/resume_disk/root/SFIX-FOCUSED03-ACTUAL-ADOPTION01.json'
ACTUAL_REVIEW = B / 'reviews/team20/resume_disk/linux_review/resumed01/focused03_actual01/ACTUAL-REVIEW.json'
EXTERNAL = B / 'reviews/team20/resume_disk/root/SFIX-FOCUSED03-EXTERNAL-TERMINAL.json'
SOURCE = B / 'reviews/checkpoint21/source-prepare01/SOURCE.json'
LOCK = Path('/root/projects/PassVault/.audit-coordination-linux-20260908/build.lock')
PYTHON = '/usr/bin/python3.12'
RUN, ORIGINAL_RUN = 'focused03-closeout01', 'linux-focused-regression03'
PURPOSE = 'ONE_FOCUSED03_CURRENT_RUN_CLEANUP_ONLY'
COMMIT, TREE, MEMBERS = 'e9cca961837789048503aaffc74ab1a1e7e536ec', '7a05dffb2fec8f0dd3b15572558e58f0c0a22f8d', 4113
RAW_ID = 'e399999938d06a2dc755e367e0fde750f415a2a9fb5e76d3b8dab1c43cfb908b'
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
DEVICE = {'directory_device': 23, 'regular_file_device': 24}
EXPECTED_LOCK = {'dev': 24, 'ino': 14189001, 'uid': 0, 'mode': 33152, 'nlink': 1, 'bytes': 0,
                 'mtime_ns': 1788910891124735946, 'ctime_ns': 1788910891124735946}
PARENTS = (R.parent, ORIGINAL_E.parent, LOCK.parent)
TOP = 'checkout home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state workers'.split()
DIRS = TOP + [f'workers/{w}' for w in ('database', 'credential')] + [
    f'workers/{w}/{d}' for w in ('database', 'credential')
    for d in 'home tmp jna sqlite xdg-cache xdg-config xdg-data xdg-state'.split()]
ODIR, MIB = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, 1024 ** 2
CACHE_REPORT_LIMITS = {'roots': 128, 'files': 16, 'file_bytes': MIB, 'total_bytes': 4 * MIB,
                       'descendant_levels': 8, 'relative_json_bytes': 512}
PROBLEMS_REPORT = ('checkout', 'build', 'reports', 'problems', 'problems-report.html')
AUXILIARY_REPORT_LIMITS = {'roots': 4, 'files': 64, 'file_bytes': MIB, 'total_bytes': 4 * MIB,
                          'descendant_levels': 8, 'relative_json_bytes': 512}
AUXILIARY_REPORT_ROOTS = (
    ('checkout', 'core', 'database', 'build', 'reports', 'tests', 'desktopTest'),
    ('checkout', 'feature', 'credential', 'build', 'reports', 'tests', 'desktopTest'),
    ('checkout', 'core', 'database', 'build', 'test-results', 'desktopTest', 'binary'),
    ('checkout', 'feature', 'credential', 'build', 'test-results', 'desktopTest', 'binary'),
)
FROZEN = {
    ORIGINAL_E / 'OUTER-RECEIPT.json': (10390, '259eb638add02615bdb7370cbc375282fb35791b1775cbac3831d05d2b4c3cde'),
    ORIGINAL_E / 'INNER-RESULT.json': (5877, '78d102c931fd292356199fce5f730e98875665188ba73f2b58496c835c658ada'),
    ORIGINAL_E / 'PHASE-focused.json': (2735, '89640a0c2a5675f42e8875364cb467f36bb4f7af21d264fb89ccadb3928410a7'),
    ORIGINAL_E / 'OUTER-ALLOCATION.json': (3863, '0c7dcc0ba55453200262d321b1c69267c91c4d10e98dc04ee9114eca9e3b025f'),
    ORIGINAL_E / 'OUTER-INTENT.json': (12768, '0e4256d9bba0b983f5053f4359bba35f736ef0abc498f21c1b61b0a3079070af'),
    ORIGINAL_E / 'SOURCE-BEFORE.json': (1334, '8f310d124648fb03f9d4ee9a2b08588c8933ba2ad813d4acd5bf0e28efcf50c4'),
    EXTERNAL: (4102, '9ad0db358c2ecd1aa97c499a7de6501f98297a969e60ff51188b62de9bbde577'),
    SOURCE: (2266654, '887d3b93b82ec9cc3b2b3ef33ed67f3ab0f96d2e4aab33e73d0a3ed5e4287d74'),
    ADOPTION: (2210, 'a7127883b9e57f16af9d16ef9de01a3c627594b91790675a254cd5e3d1dc454f'),
    ACTUAL_REVIEW: (9082, '02bfb3dc940a521bf39ddd14b377bd021653fe304a525f6aef6dd0baed61a0d4'),
    FLOOR: (7224, '1b9b15a9f34768429847775ba60c644f98e54f67ed2e261adf0aa390ac11427b'),
}
COORDINATION = {
    'sole_owner': '/root', 'original_tool_session_settled': True,
    'original_stop0_no_repeat': True, 'original_evidence_frozen': True,
    'current_runtime_writer_freeze': True, 'current_runtime_mount_freeze': True,
    'no_parallel_audit_build_test_ci': True, 'no_agent_runtime_access': True,
    'no_automatic_retry_or_followon': True,
}
START = time.monotonic()
END = WORK_END = START + 300
last_watch, output_bytes = 0.0, 0
reasons, resources, directories, images, originals = [], [], {}, {}, {}
lock_fd = None
receipt = {'format': 'passvault-focused03-closeout01-receipt-v1', 'run_id': RUN,
    'original_run_id': ORIGINAL_RUN, 'status': 'HOLD', 'original_source_after': False,
    'fresh_disposal_source_proof': False, 'originals_reverified_after': False,
    'cleanup': {'status': 'NOT_ATTEMPTED', 'removed_files': 0, 'removed_directories': 0,
        'removed_logical_bytes': 0, 'removed_allocated_bytes': 0,
        'original_runtime_removed': False},
    'tests': 0, 'xml_credit': 0, 'compile_credit': False, 'closure_delta': 0,
    'preterminal': True,
    'qualification': 'Fresh disposal only. No build/test/native/stop/Git or process census. '
        'Original stop0 and source_after=false unchanged. Cooperative writer/mount freeze; '
        'no global-idle, hostile-root or hard syscall deadline guarantee. '
        'Removed bytes are successful-unlink stat sums, not guaranteed filesystem reclamation. '
        'SIGKILL/system interruption may prevent final evidence; actual external exit is authoritative.'}

def require(value, message):
    if not value:
        raise RuntimeError(message)

def pin(s, directory=False):
    value = {k: getattr(s, 'st_' + k) for k in ('dev', 'ino', 'uid', 'mode', 'nlink')}
    if not directory:
        value.update(bytes=s.st_size, mtime_ns=s.st_mtime_ns, ctime_ns=s.st_ctime_ns)
    return value

def same_dir(a, b):
    return all(a[k] == b[k] for k in ('dev', 'ino', 'uid', 'mode'))

def pairs(rows):
    value = {}
    for k, v in rows:
        require(k not in value, 'duplicate JSON key')
        value[k] = v
    return value

def decode(raw):
    return json.loads(raw, object_pairs_hook=pairs, parse_constant=lambda _: require(False, 'nonfinite JSON'))

def encoded(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), allow_nan=False).encode() + b'\n'

def note(message):
    message = str(message)[:1024]
    if message not in reasons and len(reasons) < 128:
        reasons.append(message)

def directory(path, create=False):
    parent = None if path == Path('/') else directory(path.parent)
    if create:
        require(path not in directories, 'duplicate new directory')
        os.mkdir(path.name, 0o700, dir_fd=parent)
    if path not in directories:
        fd = os.open('/' if parent is None else path.name, ODIR, dir_fd=parent)
        try:
            value = pin(os.fstat(fd), True)
            require(stat.S_ISDIR(value['mode']) and value['uid'] == 0 and not value['mode'] & 0o022, 'directory type/owner/mode')
            directories[path] = (fd, value)  # One ownership record, one transfer; no partial two-map publish.
        except BaseException:
            os.close(fd)
            raise
    fd, original = directories[path]
    require(same_dir(pin(os.fstat(fd), True), original), 'original directory descriptor changed')
    require(parent is None or same_dir(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True), original),
            'original directory name changed')
    if create or path in PARENTS:
        require(original['dev'] == DEVICE['directory_device'], 'unadmitted directory device')
    return fd


def capture(path, cap=32 * MIB, retain=True):
    parent = directory(path.parent)
    before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
    require(stat.S_ISREG(before.st_mode), 'input is not regular')
    fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
    try:
        initial, data, digest, size = os.fstat(fd), bytearray(), hashlib.sha256(), 0
        require(stat.S_ISREG(initial.st_mode) and initial.st_uid == 0 and initial.st_nlink == 1
                and not initial.st_mode & 0o022 and initial.st_size <= cap, 'bounded root single-link regular input')
        while part := os.read(fd, 65536):
            tick(END)
            size += len(part)
            require(size <= cap, 'input growth cap')
            digest.update(part)
            if retain:
                data.extend(part)
        require(size == initial.st_size and pin(before) == pin(initial) == pin(os.fstat(fd))
                == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input/name changed during capture')
        return bytes(data), {'sha256': digest.hexdigest(), 'pin': pin(initial)}
    finally:
        os.close(fd)

def new(path):
    fd = os.open(path.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                 0o600, dir_fd=directory(path.parent))
    try:
        s = os.fstat(fd)
        require(s.st_dev == DEVICE['regular_file_device'] and s.st_uid == 0 and s.st_nlink == 1
                and s.st_mode == stat.S_IFREG | 0o600, 'unadmitted new file')
        return fd
    except BaseException:
        os.close(fd)
        raise


def output(name, data):
    global output_bytes
    require(E in directories and '/' not in name, 'owned flat closeout evidence required')
    # Keep a final-receipt reserve; source/report data are bounded before writes.
    limit = 80 * MIB if name == 'RECEIPT.json' else 80 * MIB - 128 * 1024
    require(output_bytes + len(data) <= limit, 'closeout evidence byte budget')
    fd = new(E / name)
    try:
        view = memoryview(data)
        while view:
            count = os.write(fd, view)
            require(count > 0, 'zero evidence write')
            output_bytes += count
            view = view[count:]
        os.fsync(fd)
    finally:
        os.close(fd)
    os.fsync(directory(E))


def watch(entry=False):
    global last_watch
    with open('/proc/meminfo', 'rb') as stream:
        raw = stream.read(16385)
    require(len(raw) <= 16384, 'meminfo byte bound')
    memory = dict(line.split(':', 1) for line in raw.decode('ascii').splitlines())
    available, total = (int(memory[k].split()[0]) * 1024 for k in ('MemAvailable', 'MemTotal'))
    disks = [os.fstatvfs(directory(p)) for p in PARENTS[:2]]
    free, now = min(s.f_bavail * s.f_frsize for s in disks), time.monotonic()
    if entry or not resources or now - START - resources[-1]['elapsed'] >= 10:
        resources.append({'elapsed': round(now - START, 3), 'disk_available': free,
                          'MemAvailable': available, 'MemTotal': total})
    require(free >= (12 if entry else 8) * 1024 ** 3 + (96 * MIB if entry else 0)
            and available >= total * (0.25 if entry else 0.20), 'resource floor/evidence allowance')
    last_watch = now


def tick(deadline):
    require(not reasons and time.monotonic() < deadline, 'cancelled/deadline')
    if time.monotonic() - last_watch >= 5:
        watch()


def authority():
    require(pin(os.fstat(lock_fd)) == EXPECTED_LOCK == request['lock']
            == pin(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'original lock changed')
    require(capture(REQUEST, MIB)[1] == request_image and capture(APPROVAL, 65536)[1] == approval_image,
            'fresh cleanup packet changed under original lock')
    for path in tuple(directories):
        if path != R and R not in path.parents:
            directory(path)
    for path, expected_image in {**images, **originals}.items():
        require(capture(path, 32 * MIB, retain=False)[1] == expected_image, 'retained input/evidence image changed')

def under(root, parts, expected=None):
    fd = os.dup(root)
    try:
        for index, name in enumerate(parts, 1):
            nested = os.open(name, ODIR, dir_fd=fd)
            old, fd = fd, nested
            os.close(old)
            actual = pin(os.fstat(fd), True)
            require(actual['dev'] == DEVICE['directory_device'] and actual['uid'] == 0, 'unadmitted nested directory')
            require(expected is None or same_dir(actual, expected[tuple(parts[:index])]), 'snapshot parent changed')
        return fd
    except BaseException:
        os.close(fd)
        raise

def source_inventory(manifest):
    rows, expected, folders = manifest['files'], {}, set()
    require(isinstance(rows, list) and len(rows) == MEMBERS, 'source inventory count')
    for row in rows:
        tick(WORK_END)
        name, oid = row['path'], row['git_blob']
        require(isinstance(name, str) and name and not name.startswith('/') and '\0' not in name
                and all(p not in ('', '.', '..', '.git') for p in name.split('/')) and name not in expected,
                'unsafe/duplicate source path')
        require(row['git_mode'] in ('100644', '100755') and isinstance(oid, str)
                and re.fullmatch(r'[0-9a-f]{40}', oid), 'source Git mode/OID')
        expected[name] = row  # Manifest insertion order; repeated OIDs at distinct paths are NOT deduplicated.
        parts = name.split('/')
        folders.update('/'.join(parts[:n]) for n in range(1, len(parts)))
    require(not folders.intersection(expected) and 'gradlew' in expected
            and expected['gradlew']['git_mode'] == '100755', 'source ancestor collision/wrapper mode')
    return expected, folders


def source_check(expected, snapshot):
    result, different, deadline = [], [], min(time.monotonic() + 180, END)
    for name, row in sorted(expected.items()):
        tick(deadline)
        parts = ('checkout', *name.split('/'))
        parent = under(directory(R), parts[:-1], snapshot)
        try:
            fd = os.open(name.split('/')[-1], os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK, dir_fd=parent)
            try:
                s = os.fstat(fd)
                require(pin(s) == snapshot[parts], 'source differs from original complete snapshot')
                require(s.st_mode == int(row['git_mode'], 8) and s.st_dev == DEVICE['regular_file_device'] and
                        s.st_uid == 0 and s.st_nlink == 1 and s.st_size <= 32 * MIB, 'source metadata changed')
                blob, digest, size = hashlib.sha1(b'blob ' + str(s.st_size).encode() + b'\0'), hashlib.sha256(), 0
                while part := os.read(fd, 65536):
                    tick(deadline)
                    size += len(part)
                    require(size <= 32 * MIB, 'source growth cap')
                    blob.update(part)
                    digest.update(part)
                require(size == s.st_size and pin(s) == pin(os.fstat(fd)) ==
                        pin(os.stat(name.split('/')[-1], dir_fd=parent, follow_symlinks=False)) and blob.hexdigest() == row['git_blob'],
                        'source changed during read/Git blob mismatch')
                require(size == row['raw_size'] == row['git_size'] and digest.hexdigest() == row['raw_sha256'],
                        'raw manifest size/hash mismatch')
                result.append([name, row['git_mode'], blob.hexdigest(), size, digest.hexdigest()])
                if digest.hexdigest() != row['checkout_sha256'] or size != row['checkout_size']:
                    different.append(name)
            finally:
                os.close(fd)
        finally:
            os.close(parent)
    return {'members': len(result), 'raw_identity_sha256': hashlib.sha256(encoded(result)).hexdigest(),
            'representation': 'RAW_GIT_BLOBS', 'not_checkout_normalized': different}


def no_runtime_mounts():
    deadline = min(time.monotonic() + 5, END)
    with open('/proc/self/mountinfo', 'rb') as stream:
        raw = stream.read(128 * 1024 + 1)
    require(len(raw) <= 128 * 1024, 'parent mountinfo byte bound')
    lines = raw.decode('ascii').splitlines()
    require(0 < len(lines) <= 2048, 'parent mountinfo row bound')
    for line in lines:
        require(time.monotonic() < deadline, 'parent mountinfo time bound')
        left, separator, right = line.partition(' - ')
        fields, suffix = left.split(), right.split(' ')  # Preserve a valid empty SOURCE slot.
        require(separator and len(fields) >= 6 and len(suffix) == 3 and suffix[0] and suffix[2], 'parent mountinfo format')
        point = fields[4]
        require(not re.search(r'\\(?![0-7]{3})', point), 'parent mountpoint escape')
        point = re.sub(r'\\([0-7]{3})', lambda m: chr(int(m[1], 8)), point)
        path = Path(point)
        require(path.is_absolute() and not point.startswith('//') and '..' not in path.parts, 'parent mountpoint path')
        require(path != R and R not in path.parents, 'mount at/below disposable runtime: no deletion')
    require(time.monotonic() < deadline, 'parent mountinfo final time bound')
    receipt['parent_mount_guard'] = {'rows': len(lines), 'sha256': hashlib.sha256(raw).hexdigest(),
        'qualification': 'Cooperative quiescent host only; not protection against concurrent hostile-root mount mutation.'}

def selected_report_images(result):
    # These are exactly the four reviewed class XMLs, not arbitrary TEST-* output.
    allowed = {}
    for module, fqcn in (
            ('core/database', 'com.passvault.core.database.CredentialFolderForeignKeyMigrationTest'),
            ('core/database', 'com.passvault.core.database.backup.VaultBackupUnicodePaginationTest'),
            ('feature/credential', 'com.passvault.feature.credential.presentation.CredentialCustomFieldSaveFreshnessTest'),
            ('feature/credential', 'com.passvault.feature.credential.presentation.CredentialCustomFieldDraftTest')):
        name = 'TEST-' + fqcn + '.xml'
        parts = ('checkout', *module.split('/'), 'build', 'test-results', 'desktopTest', name)
        allowed[str(R.joinpath(*parts))] = (parts, 'xml/focused-' + name)
    captures = result['phases'][0]['xml'].get('captures', [])
    require(isinstance(captures, list) and len(captures) <= 4, 'bounded previously retained selected XML')
    selected = {}
    for row in captures:
        require(isinstance(row, dict) and set(row) == {'source', 'original', 'saved'}
                and row['source'] in allowed, 'fixed retained XML source/schema')
        parts, destination = allowed[row['source']]
        require(parts not in selected, 'duplicate retained XML source')
        selected[parts] = {'relative': '/'.join(parts), 'kind': 'ALREADY_RETAINED_SELECTED_XML',
            'destination': destination, 'source_image': row['original'], 'saved_image': row['saved'],
            'file_cap': 2 * MIB, 'already_retained': True}
    paths = [parts for parts, _ in allowed.values()]
    paths += list(AUXILIARY_REPORT_ROOTS)
    return {'files': selected, 'directories': {parts[:count] for parts in paths for count in range(1, len(parts))}}


def cache_report_root(parts, is_directory):
    # Exact reviewed donor DSL-cache shapes only; no basename disposal waiver.
    if len(parts) >= 6 and parts[:4] == ('gradle-home', 'caches', '9.7.1', 'groovy-dsl'):
        count = 6
    elif len(parts) >= 7 and parts[:5] == ('gradle-home', 'caches', '9.7.1', 'kotlin-dsl', 'scripts'):
        count = 7
    else:
        return None
    digest = parts[count - 2]
    if len(digest) != 32 or any(c not in '0123456789abcdef' for c in digest) or parts[count - 1] != 'reports':
        return None
    require(len(parts) - count <= CACHE_REPORT_LIMITS['descendant_levels']
            and len(encoded('/'.join(parts))) <= CACHE_REPORT_LIMITS['relative_json_bytes'], 'cache report depth/path cap')
    root = parts[:count]
    require(parts != root or is_directory, 'cache report root is not a directory')
    return root


def capture_report_source(parts, snapshot, cap=MIB):
    # Bind every containing directory to the original complete snapshot BEFORE
    # reusing capture(); never adopt a new parent merely because the name matches.
    for count in range(1, len(parts)):
        prefix = parts[:count]
        fd = directory(R.joinpath(*prefix))
        require(same_dir(pin(os.fstat(fd), True), snapshot[prefix]), 'report parent differs from original snapshot')
    data, image = capture(R.joinpath(*parts), cap)
    require(image['pin'] == snapshot[parts], 'report file differs from original snapshot')
    return data, image


def preserve_generated_reports(snapshot, expected, folders, selected):
    frozen_files = {('checkout', *name.split('/')) for name in expected}
    frozen_directories = {('checkout', *name.split('/')) for name in folders}
    problem_directories = {PROBLEMS_REPORT[:count] for count in range(3, len(PROBLEMS_REPORT))}
    roots, candidates, cache_files, cache_bytes = {}, [], 0, 0
    auxiliary_roots, auxiliary_files, auxiliary_bytes = {}, 0, 0
    # Classification of the WHOLE snapshot precedes every evidence copy and unlink.
    for parts, original in sorted(snapshot.items(), key=lambda item: (len(item[0]), item[0])):
        tick(END)
        is_directory = stat.S_ISDIR(original['mode'])
        if parts in frozen_files:
            require(not is_directory, 'frozen source file changed type')
            continue  # Authoritative source/evidence remains preserved outside R.
        if parts in frozen_directories:
            require(is_directory, 'frozen source ancestor changed type')
            continue
        if parts in selected['files']:
            row = selected['files'][parts]
            require(not is_directory and row['source_image']['pin'] == original,
                    'previously retained report original differs from complete snapshot')
            continue
        if is_directory and parts in selected['directories']:
            continue  # Exact source/auxiliary ancestors only; siblings gain no waiver.
        root = cache_report_root(parts, is_directory)
        if root is not None:
            if parts == root:
                require(root not in roots and len(roots) < CACHE_REPORT_LIMITS['roots'], 'cache report root cap')
                roots[root] = original
            else:
                require(root in roots, 'cache report root must precede descendants')
            if not is_directory:
                cache_files += 1
                cache_bytes += original['bytes']
                require(cache_files <= CACHE_REPORT_LIMITS['files'] and 0 <= original['bytes'] <= MIB
                        and cache_bytes <= CACHE_REPORT_LIMITS['total_bytes'], 'cache report file/byte caps')
                candidates.append((parts, 'PRIVATE_GRADLE_DSL_REPORT'))
            continue
        if parts == PROBLEMS_REPORT:
            require(not is_directory and 0 <= original['bytes'] <= MIB, 'fixed problems report type/byte cap')
            candidates.append((parts, 'FIXED_GRADLE_PROBLEMS_REPORT'))
            continue
        if is_directory and parts in problem_directories:
            continue  # Only the fixed file is permitted beneath these ancestors.
        auxiliary = next((root for root in AUXILIARY_REPORT_ROOTS if parts[:len(root)] == root), None)
        if auxiliary is not None:
            require(len(parts) - len(auxiliary) <= AUXILIARY_REPORT_LIMITS['descendant_levels']
                    and len(encoded('/'.join(parts))) <= AUXILIARY_REPORT_LIMITS['relative_json_bytes'],
                    'selected test auxiliary report depth/path cap')
            if parts == auxiliary:
                require(is_directory and auxiliary not in auxiliary_roots, 'fixed auxiliary report root type/order')
                auxiliary_roots[auxiliary] = original
            else:
                require(auxiliary in auxiliary_roots, 'fixed auxiliary root must precede descendants')
            if not is_directory:
                auxiliary_files += 1
                auxiliary_bytes += original['bytes']
                require(auxiliary_files <= AUXILIARY_REPORT_LIMITS['files'] and 0 <= original['bytes'] <= MIB
                        and auxiliary_bytes <= AUXILIARY_REPORT_LIMITS['total_bytes'], 'selected test auxiliary file/byte caps')
                candidates.append((parts, 'SELECTED_TEST_AUXILIARY_REPORT'))
            continue
        lower, name = tuple(p.lower() for p in parts), parts[-1].lower()
        report = any(p in ('reports', 'test-results', 'surefire-reports', 'failsafe-reports', 'test-output') for p in lower)
        diagnostic = (name.endswith(('.sarif', '.sarif.json', '.xcresult'))
                      or (not is_directory and (name.endswith('.hprof')
                          or (name.startswith('test-') and name.endswith('.xml'))
                          or name.startswith(('hs_err_pid', 'replay_pid', 'worker-error-'))
                          or name == 'core' or (name.startswith('core.') and name[5:].isdigit()))))
        if report or diagnostic:
            receipt['unexpected_evidence'] = {'relative': '/'.join(parts), 'directory': is_directory,
                                              'reason': 'unpreserved report/test/crash/worker-error evidence'}
            require(False, 'unpreserved generated diagnostic: HOLD before any deletion')
    copies = list(selected['files'].values())
    prior_count = len(copies)
    for parts, kind in candidates:
        tick(END)
        data, original = capture_report_source(parts, snapshot)
        destination = 'GENERATED-REPORT-%04d.bin' % (len(copies) - prior_count + 1)
        output(destination, data)  # Existing exclusive flat-E write + file/parent fsync.
        saved_data, saved = capture(E / destination, MIB)
        require(saved_data == data and saved['sha256'] == original['sha256']
                and saved['pin']['dev'] == DEVICE['regular_file_device']
                and saved['pin']['mode'] == stat.S_IFREG | 0o600, 'exact durable report copy/readback')
        copies.append({'relative': '/'.join(parts), 'kind': kind, 'destination': destination,
                       'source_image': original, 'saved_image': saved, 'file_cap': MIB, 'already_retained': False})
    manifest = {'format': 'passvault-focused03-closeout01-retained-diagnostics-v1', 'run_id': RUN,
        'cache_roots': {'/'.join(parts): value for parts, value in roots.items()}, 'limits': CACHE_REPORT_LIMITS,
        'cache_file_count': cache_files, 'cache_logical_bytes': cache_bytes,
        'auxiliary_roots': {'/'.join(parts): value for parts, value in auxiliary_roots.items()},
        'auxiliary_limits': AUXILIARY_REPORT_LIMITS, 'auxiliary_file_count': auxiliary_files,
        'auxiliary_logical_bytes': auxiliary_bytes,
        'problems_report_limit_bytes': MIB, 'previously_retained_files': prior_count, 'copies': copies,
        'qualification': 'Opaque generated diagnostic bytes conserved outside R, no provenance or test credit.'}
    output('GENERATED-REPORTS.json', encoded(manifest))
    saved_manifest, manifest_image = capture(E / 'GENERATED-REPORTS.json', MIB)
    require(saved_manifest == encoded(manifest), 'exact durable generated-report manifest')
    receipt['generated_report_preservation'] = {'manifest': manifest_image, 'copied_files': len(copies) - prior_count,
        'previously_retained_files': prior_count,
        'copied_logical_bytes': sum(row['source_image']['pin']['bytes'] for row in copies if not row['already_retained']),
        'source_and_saved_reverified_before_unlink': False, 'saved_reverified_after_cleanup': False}
    return {'copies': copies, 'manifest_image': manifest_image}


def verify_generated_reports(conserved, snapshot, *, source):
    require(capture(E / 'GENERATED-REPORTS.json', MIB)[1] == conserved['manifest_image'], 'retained report manifest changed')
    for row in conserved['copies']:
        tick(END)
        if source:
            require(capture_report_source(tuple(row['relative'].split('/')), snapshot, row['file_cap'])[1] == row['source_image'],
                    'original report changed before first unlink')
        saved_data, saved = capture(E / row['destination'], row['file_cap'])
        require(saved == row['saved_image'] and saved['pin']['dev'] == DEVICE['regular_file_device']
                and saved['pin']['mode'] == stat.S_IFREG | 0o600
                and saved['sha256'] == row['source_image']['sha256']
                and len(saved_data) == row['source_image']['pin']['bytes'], 'retained report bytes/pin changed')
    key = 'source_and_saved_reverified_before_unlink' if source else 'saved_reverified_after_cleanup'
    receipt['generated_report_preservation'][key] = True


def remove_runtime(expected, folders, result, intent, source, transport_images):
    root, snapshot, allocations = directory(R), {}, {}
    def inventory(fd, prefix=()):
        for name in os.listdir(fd):
            tick(END)
            require(len(snapshot) < 250000 and len(prefix) < 128 and name != '.git'
                    and (prefix or name in TOP + ['source.oids', 'source.blobs']), 'cleanup count/depth/top/Git boundary')
            relative, s = prefix + (name,), os.stat(name, dir_fd=fd, follow_symlinks=False)
            isdir = stat.S_ISDIR(s.st_mode)
            require(s.st_uid == 0 and (isdir or (stat.S_ISREG(s.st_mode) and s.st_nlink == 1))
                    and s.st_dev == DEVICE['directory_device' if isdir else 'regular_file_device'], 'cleanup type/owner/device')
            snapshot[relative], allocations[relative] = pin(s, isdir), s.st_blocks * 512
            if isdir:
                nested = under(root, relative, snapshot)
                try:
                    inventory(nested, relative)
                finally:
                    os.close(nested)
    inventory(root)  # Complete snapshot, no report copies or deletion until finished.
    for name in DIRS:
        require(same_dir(snapshot[tuple(name.split('/'))], intent['directories'][str(R / name)]), 'original subroot snapshot mismatch')
    for path, image in transport_images.items():
        require(snapshot[(path.name,)] == image['pin'], 'original raw transport differs from deletion snapshot')
    proof = source_check(expected, snapshot)
    require(proof == intent['raw_source'] and proof['members'] == MEMBERS
            and proof['raw_identity_sha256'] == RAW_ID, 'fresh disposal-only raw source identity mismatch')
    proof.update(format='passvault-focused03-closeout01-disposal-source-v1', commit=COMMIT, tree=TREE,
        bytes=sum(row['raw_size'] for row in expected.values()), source_manifest=originals[SOURCE],
        bound_to_complete_deletion_snapshot=True, original_source_after=False,
        checkout_eol_qualifications=source['checkout_eol_qualifications'],
        qualification='Fresh raw duplicate proof for disposal only; original cancelled source-after remains false. '
                      'Fifteen checkout-normalization differences and two historical qualifications remain separate; no test credit.')
    output('SOURCE-DISPOSAL-PROOF.json', encoded(proof))
    proof_image = capture(E / 'SOURCE-DISPOSAL-PROOF.json', MIB)[1]
    receipt.update(fresh_disposal_source_proof=True, disposal_source_proof=proof_image)
    inventory_data = encoded({'format': 'passvault-focused03-closeout01-snapshot-v1', 'run_id': RUN,
        'original_runtime_pin': directories[R][1], 'entries': {'/'.join(p): snapshot[p] for p in sorted(snapshot)},
        'allocated_bytes': {'/'.join(p): allocations[p] for p in sorted(allocations)},
        'count': len(snapshot), 'complete': True})
    require(len(inventory_data) <= 64 * MIB, 'complete snapshot evidence cap')
    output('RUNTIME-SNAPSHOT.json', inventory_data)
    snapshot_image = capture(E / 'RUNTIME-SNAPSHOT.json', 64 * MIB, retain=False)[1]
    receipt['snapshot'] = {'image': snapshot_image, 'entries': len(snapshot),
        'files': sum(not stat.S_ISDIR(s['mode']) for s in snapshot.values()),
        'logical_bytes': sum(s['bytes'] for s in snapshot.values() if not stat.S_ISDIR(s['mode'])),
        'allocated_bytes': sum(allocations.values())}
    conserved = preserve_generated_reports(snapshot, expected, folders, selected_report_images(result))
    verify_generated_reports(conserved, snapshot, source=True)
    authority()
    require(capture(E / 'SOURCE-DISPOSAL-PROOF.json', MIB)[1] == proof_image
            and capture(E / 'RUNTIME-SNAPSHOT.json', 64 * MIB, retain=False)[1] == snapshot_image, 'saved proof/snapshot changed')
    no_runtime_mounts()  # Second mount guard immediately before first unlink.
    receipt['cleanup']['status'] = 'STARTED'
    for parts in sorted(snapshot, key=lambda p: (-len(p), p)):
        tick(END)
        fd = under(root, parts[:-1], snapshot)
        try:
            actual, original = os.stat(parts[-1], dir_fd=fd, follow_symlinks=False), snapshot[parts]
            isdir = stat.S_ISDIR(original['mode'])
            require(same_dir(pin(actual, True), original) if isdir else pin(actual) == original, 'cleanup original changed')
            (os.rmdir if isdir else os.unlink)(parts[-1], dir_fd=fd)
            # Signal handlers only latch; these successful-operation counters update before next tick.
            receipt['cleanup']['removed_directories' if isdir else 'removed_files'] += 1
            receipt['cleanup']['removed_logical_bytes'] += 0 if isdir else actual.st_size
            receipt['cleanup']['removed_allocated_bytes'] += actual.st_blocks * 512
        finally:
            os.close(fd)
    require(not os.listdir(root) and same_dir(pin(os.stat(R.name, dir_fd=directory(R.parent), follow_symlinks=False), True),
            directories[R][1]), 'original runtime not empty/name changed')
    original_root = os.fstat(root)
    os.rmdir(R.name, dir_fd=directory(R.parent))
    receipt['cleanup']['removed_directories'] += 1
    receipt['cleanup']['removed_allocated_bytes'] += original_root.st_blocks * 512
    receipt['cleanup']['original_runtime_removed'] = True
    receipt['cleanup']['status'] = 'REMOVED_POSTCHECKS_PENDING'
    os.fsync(directory(R.parent))
    require(os.fstat(root).st_nlink == 0, 'original runtime descriptor removal not established')
    try:
        os.stat(R.name, dir_fd=directory(R.parent), follow_symlinks=False)
    except FileNotFoundError:
        receipt['cleanup']['original_name_absent'] = True
    else:
        require(False, 'runtime name reappeared after original removal')
    verify_generated_reports(conserved, snapshot, source=False)
    require(capture(E / 'SOURCE-DISPOSAL-PROOF.json', MIB)[1] == proof_image
            and capture(E / 'RUNTIME-SNAPSHOT.json', 64 * MIB, retain=False)[1] == snapshot_image, 'saved proof/snapshot postcheck changed')
    authority()
    receipt['originals_reverified_after'] = True
    receipt['cleanup']['status'] = 'REMOVED_ORIGINAL_RUNTIME_AND_EVIDENCE_REVERIFIED'


def main():
    global lock_fd, request, request_image, approval_image
    require(sys.argv == [str(SELF)] and sys.executable == PYTHON and not sys.flags.optimize
            and sys.flags.isolated and sys.flags.no_site and sys.dont_write_bytecode
            and dict(os.environ) == ENV and os.getuid() == os.geteuid() == 0, 'fixed isolated entry')
    raw, request_image = capture(REQUEST, MIB)
    request = decode(raw)
    raw, approval_image = capture(APPROVAL, 65536)
    approval = decode(raw)
    require(set(request) == {'format', 'owner', 'run_id', 'purpose', 'commit', 'tree', 'lock', 'device_model', 'images', 'coordination'}
            and request['format'] == 'passvault-focused03-closeout01-request-v1' and request['owner'] == '/root'
            and request['run_id'] == RUN and request['purpose'] == PURPOSE and request['commit'] == COMMIT
            and request['tree'] == TREE and request['device_model'] == DEVICE
            and request['coordination'] == COORDINATION, 'fresh fixed cleanup request/source/freeze')
    require(set(request['images']) == {str(p) for p in (SELF, PLAN, SLOT)}, 'exact new packet image set')
    require(set(approval) == {'format', 'reviewer', 'disposition', 'purpose', 'run_id', 'request_sha256', 'sources'}
            and approval['format'] == 'passvault-focused03-closeout01-approval-v1'
            and approval['reviewer'] == '/root/capture_review'
            and approval['disposition'] == 'ACCEPT_EXACT_NEW_INSTANCE' and approval['purpose'] == PURPOSE
            and approval['run_id'] == RUN and approval['request_sha256'] == request_image['sha256']
            and approval['sources'] == {str(p): request['images'][str(p)]['sha256'] for p in (SELF, PLAN)},
            'missing/stale/nonaccepting genuine independent review')
    for path in (SELF, PLAN, SLOT):
        data, actual = capture(path, MIB)
        require(actual == request['images'][str(path)], 'fresh exact image mismatch')
        images[path] = actual
        if path == SLOT:
            slot = decode(data)
    require(slot['owner'] == '/root' and slot['authority'] == 'SOLE_LIVE_POST_FREEZE_SCHEDULING_RECORD_FOR_NEW_OPERATIONS'
            and slot['physical_coordination_lock'] == str(LOCK)
            and slot['root_commitments']['no_audit_build_test_or_ci_running'] is True
            and slot['current_operation']['run_id'] == RUN
            and slot['current_operation']['kind'] == 'CLEANUP_ONLY_NO_BUILD_TEST_OR_STOP'
            and slot['current_operation']['active'] is True, 'fresh cleanup not sole-live reserved operation')
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(pin(os.fstat(lock_fd)) == request['lock'] == EXPECTED_LOCK
            == pin(os.stat(LOCK.name, dir_fd=directory(LOCK.parent), follow_symlinks=False)), 'wrong original physical lock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    records = {}
    for path, (size, digest) in FROZEN.items():
        raw, image = capture(path, 32 * MIB)
        require(len(raw) == size and image['sha256'] == digest, 'immutable original/floor/source/actual-review mismatch')
        originals[path] = image
        if path.suffix == '.json':
            records[path] = decode(raw)
    outer, inner, phase, allocation, intent, source, external, adoption = (records[p] for p in (
        ORIGINAL_E / 'OUTER-RECEIPT.json', ORIGINAL_E / 'INNER-RESULT.json', ORIGINAL_E / 'PHASE-focused.json',
        ORIGINAL_E / 'OUTER-ALLOCATION.json', ORIGINAL_E / 'OUTER-INTENT.json', SOURCE, EXTERNAL, ADOPTION))
    require(outer['allocation'] == allocation and outer['cleanup'] == 'NOT_ATTEMPTED' and outer['status'] == 'HOLD'
            and len(inner['phases']) == 1 and inner['phases'][0] == dict(phase, record_image=originals[ORIGINAL_E / 'PHASE-focused.json'])
            and inner['namespace_empty_before_exit'] is True and inner['all_required_stops_ok'] is True
            and inner['source_before'] is True and inner['source_after'] is False and inner['cleanup_safe'] is False
            and inner['independent_semantic_acceptance'] is False and inner['validation_mapping_ok'] is False
            and phase['settled'] is True and phase['stop_attempted'] is True and phase['stop_ok'] is True
            and phase['xml']['captures'] == [] and [c['exit'] for c in phase['commands']] == [143, 0]
            and all(c['started'] is True and c['complete'] is True for c in phase['commands'])
            and external['tool_terminal']['exit_code'] == 70 and external['tool_terminal']['completion_received'] is True
            and external['observations']['original_stop_exit'] == 0 and external['observations']['stop_complete'] is True
            and external['observations']['source_after'] is False
            and adoption['status'] == 'ACCEPT_CONSUMED_HOLD_ORIGINAL_STOP_COMPLETE_OWNED_EXECUTION_SETTLED_NO_CASES'
            and adoption['independent_review'] == {'path': str(ACTUAL_REVIEW.relative_to(B)),
                'bytes': FROZEN[ACTUAL_REVIEW][0], 'sha256': originals[ACTUAL_REVIEW]['sha256']}
            and adoption['external_terminal'] == {'path': str(EXTERNAL.relative_to(B)),
                'bytes': FROZEN[EXTERNAL][0], 'sha256': originals[EXTERNAL]['sha256']},
            'original cancelled result/settlement/stop0/independent reconciliation changed')
    require(source['commit'] == COMMIT and source['tree'] == TREE and len(source['checkout_eol_qualifications']) == 2
            and originals[SOURCE] == allocation['sources'][str(SOURCE)]
            and intent['raw_source'] == outer['source_before'] and intent['raw_source']['raw_identity_sha256'] == RAW_ID,
            'original 4113 raw source/allocation identity')
    # Prior admission is floor provenance only; this cleanup needs its own exact request/slot.
    require(records[FLOOR]['resources']['launch_floor_bytes'] == 12 * 1024 ** 3
            and records[FLOOR]['resources']['running_floor_bytes'] == 8 * 1024 ** 3, 'normal floor provenance')
    expected_extra = {'TASK-RESULTS.json', 'logs/focused.log', 'logs/focused-stop.log', 'CANCEL'}
    # Actual Focused03 terminal schema; the two log anchors come from its frozen phase.
    evidence_rows = [external[key] for key in ('outer_receipt', 'inner_result', 'phase', 'task_results', 'cancel')]
    evidence_rows += [{'path': str((ORIGINAL_E / name).relative_to(B)),
                      'bytes': command['log']['pin']['bytes'], 'sha256': command['log']['sha256']}
                     for name, command in zip(('logs/focused.log', 'logs/focused-stop.log'), phase['commands'])]
    for row in evidence_rows:
        path = B / row['path']
        require(path.parent == ORIGINAL_E or path.parent == ORIGINAL_E / 'logs', 'fixed original evidence path')
        if path in originals:
            require(originals[path]['sha256'] == row['sha256'] and originals[path]['pin']['bytes'] == row['bytes'], 'external anchor mismatch')
        else:
            relative = str(path.relative_to(ORIGINAL_E))
            require(relative in expected_extra, 'unexpected original evidence member')
            expected_extra.remove(relative)
            raw, image = capture(path, MIB)
            require(image['sha256'] == row['sha256'] and len(raw) == row['bytes'], 'original retained evidence mismatch')
            originals[path] = image
    require(not expected_extra and originals[ORIGINAL_E / 'TASK-RESULTS.json'] == phase['task_evidence']['result_image']
            and originals[ORIGINAL_E / 'logs/focused.log'] == phase['commands'][0]['log']
            and originals[ORIGINAL_E / 'logs/focused-stop.log'] == phase['commands'][1]['log'], 'original retained logs/task pins')
    git_stderr = ORIGINAL_E / 'logs/outer-git.stderr'
    originals[git_stderr] = capture(git_stderr, MIB)[1]
    require(originals[git_stderr] == outer['children'][0]['outputs'][str(git_stderr)], 'original empty transport stderr changed')
    require(capture(Path(PYTHON), 16 * MIB, retain=False)[1] == intent['outer_images'][PYTHON], 'fixed Python tool image changed')
    for path in PARENTS:
        require(same_dir(pin(os.fstat(directory(path)), True), allocation['parents'][str(path)]), 'original parent identity changed')
    require(same_dir(pin(os.fstat(directory(ORIGINAL_E)), True), allocation['allocated_directories'][str(ORIGINAL_E)]),
            'original evidence directory changed')
    authority()
    os.umask(0o077)
    # Exclusive create is the once-only marker. No overwrite, recovery or automatic retry.
    directory(E, True)
    os.fsync(directory(ORIGINAL_E))
    receipt.update(request=request_image, approval=approval_image, original_evidence_pin=directories[ORIGINAL_E][1],
                   closeout_pin=directories[E][1], device_model=DEVICE)
    watch(entry=True)
    output('ORIGINAL-ANCHORS.json', encoded({str(p): image for p, image in originals.items()}))
    no_runtime_mounts()  # Guard before opening any original runtime subtree or transport.
    for path in (R, *(R / name for name in DIRS)):
        actual = pin(os.fstat(directory(path)), True)
        require(same_dir(actual, intent['directories'][str(path)]), 'original focused03 runtime/subroot changed')
    require(same_dir(directories[R][1], allocation['allocated_directories'][str(R)]), 'original allocated runtime changed')
    receipt['original_runtime_pin'] = directories[R][1]
    transport_images = {}
    for path, image in ((R / 'source.oids', outer['raw_transport']['ordered_request'][str(R / 'source.oids')]),
                        (R / 'source.blobs', outer['raw_transport']['response'][str(R / 'source.blobs')])):
        require(capture(path, 256 * MIB, retain=False)[1] == image, 'original raw transport changed')
        transport_images[path] = image
    expected, folders = source_inventory(source)
    remove_runtime(expected, folders, inner, intent, source, transport_images)
    watch()
    require(not reasons, 'late cancellation after disposal')
    receipt['status'] = 'DISPOSAL_COMPLETED_NOT_TEST_SUCCESS'


def on_signal(number, _frame):
    note('signal:' + str(number))  # Latch only; never signal a process or interrupt a completed unlink counter.


if __name__ == '__main__':
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(number, on_signal)
    exit_code = 70
    try:
        main()
        exit_code = 0
    except BaseException as exc:
        note(type(exc).__name__ + ':' + str(exc))
    finally:
        if reasons:
            exit_code = 70
            receipt['status'] = 'HOLD'
        receipt.update(reasons=reasons, resource_points=resources, elapsed=round(time.monotonic() - START, 3),
                       retained_output_bytes_before_receipt=output_bytes)
        if reasons and receipt['cleanup']['status'] == 'STARTED':
            receipt['cleanup']['status'] = 'PARTIAL_REMOVAL_HOLD_NO_RETRY'
        if E in directories:
            try:
                output('RECEIPT.json', encoded(receipt))
            except BaseException as exc:
                note('receipt-write:' + type(exc).__name__ + ':' + str(exc))
                exit_code = 70
        for fd, _ in reversed(tuple(directories.values())):
            try:
                os.close(fd)
            except OSError as exc:
                note('descriptor-close:' + str(exc))
                exit_code = 70
        if lock_fd is not None:
            try:
                os.close(lock_fd)
            except OSError as exc:
                note('lock-close:' + str(exc))
                exit_code = 70
        # A signal may latch while the immutable preterminal receipt is written or descriptors close.
        if reasons:
            exit_code = 70
            receipt['status'] = 'HOLD'
        print(json.dumps({'run_id': RUN, 'preterminal_status': receipt['status'],
                          'cleanup': receipt['cleanup'], 'reasons': reasons, 'exit': exit_code}, sort_keys=True), flush=True)
    sys.exit(exit_code)
