#!/usr/bin/python3
"""Fresh C18 read-only metadata observer; root promotes/adopts exact reviewed source.
No Git/subprocess/build/test, executable invocation, SDK modification or deletion.
One-shot exclusive evidence; 180s cooperative work deadline, HOLD/no retry.
Two stable observations are not continuous/hostile-host or external-terminal proof.
"""
import fcntl, hashlib, json, os, signal, stat, sys, time
from datetime import datetime, timezone
from pathlib import Path

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
D = B / 'reviews/checkpoint18'
SELF = D / 'C18-READONLY-FACTS-01.py'
INTENT, RESULT = 'C18-READONLY-FACTS-01-INTENT.json', 'C18-READONLY-FACTS-01.json'
T = Path('/root/projects/PassVault/passvault-publication-20260910-01')
G = T / '.git'
LOCK = T.parent / '.audit-coordination-linux-20260908/build.lock'
FACTS = D / 'SOURCE-STORE-ADMISSION.json'
FACTS_SHA = 'cf92d6783a3f8a6d97cda909354b5cfa733fe649c0a65a3cd9bb16378e2abd9a'
SOURCE = D / 'source-prepare01/SOURCE.json'
SOURCE_SHA = 'a34aee1c7ea19fb693adf67437ec67e8b9e0ee34232936cb76127764c7b242d3'
COMMIT, TREE = '6489252e88ad553a867d67578eff45a402e62a48', '57d338a931ab0fb4e072aabcbbfd27bead8ef08a'
REF = 'refs/heads/codex/audit-continuation-linux-20260908'
ABSENCES = ('commondir', 'config.worktree', 'info/attributes',
            'objects/info/alternates', 'objects/info/http-alternates')
JAVA, SDK = Path('/usr/lib/jvm/java-17-openjdk-amd64'), Path('/opt/android-sdk')
TOOLS = tuple(Path('/usr/bin') / name for name in ('git', 'ruby3.2', 'python3.12', 'env', 'mount', 'unshare'))
ALIASES = {Path('/usr/bin/git'): Path('/usr/bin/git'), Path('/usr/bin/ruby'): Path('/usr/bin/ruby3.2')}
PYTHON_ALIAS, PYTHON_IMAGE = Path('/usr/bin/python3'), Path('/usr/bin/python3.12')
SDK_METADATA = tuple(SDK / name for name in ('platforms/android-37.0/source.properties',
    'platforms/android-37.0/package.xml', 'build-tools/36.0.0/source.properties',
    'build-tools/36.0.0/package.xml', 'licenses/android-sdk-license'))
MIB, ODIR = 1024 ** 2, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
START, CANCEL = time.monotonic(), set()
CAPTURED = 'C18_READONLY_FACTS_CAPTURED_NOT_INDEX_OR_BUILD_ADMISSION'


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def tick():
    require(not CANCEL and time.monotonic() - START < 180, 'cancelled/180s observation deadline')


def pin(st, directory=False):
    value = {key: getattr(st, 'st_' + key) for key in ('dev', 'ino', 'uid', 'mode', 'nlink')}
    if not directory:
        value.update(bytes=st.st_size, mtime_ns=st.st_mtime_ns, ctime_ns=st.st_ctime_ns)
    return value


def identity(value):
    return {key: value[key] for key in ('dev', 'ino', 'uid', 'mode')}


def od(path):
    require(path.is_absolute() and '..' not in path.parts, 'fixed absolute directory')
    fd = None
    try:
        fd = os.open('/', ODIR)
        for part in path.parts[1:]:
            child = os.open(part, ODIR, dir_fd=fd)
            previous, fd = fd, child
            os.close(previous)
            current = os.fstat(fd)
            require(current.st_uid == 0 and not current.st_mode & 0o022, 'directory owner/mode')
        answer, fd = fd, None
        return answer
    finally:
        if fd is not None:
            os.close(fd)


def directory_pin(path):
    fd = None
    try:
        fd = od(path)
        return pin(os.fstat(fd), True)
    finally:
        if fd is not None:
            os.close(fd)


def named_pin(path):
    parent = None
    try:
        parent = od(path.parent)
        return pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
    finally:
        if parent is not None:
            os.close(parent)


def absent(path):
    parent = None
    try:
        try:
            parent = od(path.parent)
            os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        except FileNotFoundError:
            return True
        return False
    finally:
        if parent is not None:
            os.close(parent)


def read_image(path, cap, keep=False):
    tick()
    parent = fd = None
    try:
        parent = od(path.parent)
        parent_pin = pin(os.fstat(parent), True)
        before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        require(stat.S_ISREG(before.st_mode) and before.st_uid == 0 and before.st_nlink == 1
                and not before.st_mode & 0o022 and 0 <= before.st_size <= cap, 'bounded regular input: ' + str(path))
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        require(pin(before) == pin(os.fstat(fd)), 'input open drift: ' + str(path))
        digest, size, chunks = hashlib.sha256(), 0, []
        while True:
            tick()
            chunk = os.read(fd, min(65536, cap + 1 - size))
            if not chunk:
                break
            size += len(chunk)
            require(size <= cap, 'input byte cap: ' + str(path))
            digest.update(chunk)
            if keep:
                chunks.append(chunk)
        require(size == before.st_size and pin(before) == pin(os.fstat(fd)) ==
                pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input read drift: ' + str(path))
        require(parent_pin == pin(os.fstat(parent), True) == directory_pin(path.parent), 'input parent drift')
        return {'pin': pin(before), 'sha256': digest.hexdigest()}, b''.join(chunks) if keep else None
    finally:
        try:
            if fd is not None:
                os.close(fd)
        finally:
            if parent is not None:
                os.close(parent)


def alias(path, target):
    tick()
    parent = None
    try:
        parent = od(path.parent)
        parent_pin = pin(os.fstat(parent), True)
        before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        require(before.st_uid == 0 and before.st_nlink == 1, 'alias owner/link count')
        link = None
        if stat.S_ISLNK(before.st_mode):
            require(before.st_size <= 4096, 'alias size')
            link = os.readlink(path.name, dir_fd=parent)
            destination = Path(link) if Path(link).is_absolute() else path.parent / link
            require('..' not in destination.parts and destination == target, 'only fixed direct alias target')
        else:
            require(stat.S_ISREG(before.st_mode) and path == target, 'only fixed regular alias image')
        require(pin(before) == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'alias read drift')
        require(parent_pin == pin(os.fstat(parent), True) == directory_pin(path.parent), 'alias parent drift')
        return {'lstat': pin(before), 'readlink': link, 'resolved_image': str(target)}
    finally:
        if parent is not None:
            os.close(parent)


def resource_point(label, initial, points):
    tick()
    parent = fd = diskfd = None
    try:
        parent = od(Path('/proc'))
        fd = os.open('meminfo', os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        require(stat.S_ISREG(os.fstat(fd).st_mode), 'fixed meminfo type')
        data = bytearray()
        while True:
            tick()
            chunk = os.read(fd, min(65536, 65537 - len(data)))
            if not chunk:
                break
            data.extend(chunk)
            require(len(data) <= 65536, 'meminfo byte cap')
        values = {}
        for line in data.splitlines():
            name, separator, value = line.partition(b':')
            if name in (b'MemAvailable', b'MemTotal'):
                fields = value.split()
                require(separator and name not in values and len(fields) == 2
                        and fields[0].isdigit() and fields[1] == b'kB', 'meminfo point schema')
                values[name] = int(fields[0]) * 1024
        available, total = values[b'MemAvailable'], values[b'MemTotal']
        diskfd = od(D)
        fs = os.fstatvfs(diskfd)
        free, percent, gib = fs.f_bavail * fs.f_frsize, 25 if initial else 20, 12 if initial else 8
        point = {'label': label, 'elapsed_seconds': time.monotonic() - START, 'disk_path': str(D),
                 'disk_available_bytes': free, 'ram_available_bytes': available, 'ram_total_bytes': total,
                 'minimum_disk_GiB': gib, 'minimum_ram_percent': percent, 'qualification': 'POINT_ONLY_NOT_GLOBAL_IDLE',
                 'floors_met': 0 < available <= total and free >= gib * 1024 * MIB and 100 * available >= percent * total}
        points.append(point)
        require(point['floors_met'], 'unchanged point RAM/disk floor')
    finally:
        try:
            if fd is not None:
                os.close(fd)
        finally:
            try:
                if diskfd is not None:
                    os.close(diskfd)
            finally:
                if parent is not None:
                    os.close(parent)


def store_guard(facts, lock):
    tick()
    require(pin(os.fstat(lock)) == named_pin(LOCK) == facts['original_lock'], 'original lock descriptor/path drift')
    directories = {}
    for path, key in ((T, 'checkout_identity'), (G, 'store_identity'),
                      (LOCK.parent, 'coordination_parent_identity'), (T.parent, 'workspace_parent_identity')):
        directories[str(path)] = directory_pin(path)
        require(directories[str(path)] == facts[key], 'original directory drift: ' + str(path))
    absence = {name: absent(G / name) for name in ABSENCES}
    require(absence == facts['absence'] and all(absence.values()), 'original store absence drift')
    return {'directories': directories, 'absence': absence, 'original_lock': pin(os.fstat(lock))}


def excludes():
    if absent(G / 'info'):
        return {'state': 'INFO_ABSENT', 'info_directory': None}
    info = directory_pin(G / 'info')
    return {'state': 'EXCLUDE_ABSENT' if absent(G / 'info/exclude') else 'FILE', 'info_directory': info}


def encoded(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True, allow_nan=False).encode() + b'\n'


def write_record(name, value, evidence_directory, created):
    require(name in (INTENT, RESULT), 'fixed new evidence name')
    data = encoded(value)
    require(len(data) <= 128 * 1024, 'evidence byte cap')
    parent = fd = None
    try:
        parent = od(D)
        require(identity(pin(os.fstat(parent), True)) == evidence_directory, 'original evidence directory')
        fd = os.open(name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=parent)
        created.add(name)
        current = os.fstat(fd)
        require(current.st_uid == 0 and current.st_nlink == 1 and current.st_mode == stat.S_IFREG | 0o600,
                'original exclusive evidence file')
        offset = 0
        while offset < len(data):
            count = os.write(fd, data[offset:])
            require(count > 0, 'short evidence write')
            offset += count
        os.fsync(fd)
        require(os.fstat(fd).st_size == len(data) and pin(os.fstat(fd)) ==
                pin(os.stat(name, dir_fd=parent, follow_symlinks=False)), 'evidence write identity drift')
        require(identity(directory_pin(D)) == evidence_directory, 'evidence directory path drift')
        os.fsync(parent)
        return hashlib.sha256(data).hexdigest()
    finally:
        try:
            if fd is not None:
                os.close(fd)
        finally:
            if parent is not None:
                os.close(parent)


def main():
    report = {'format': 'passvault-c18-readonly-facts-01-v1', 'owner': '/root', 'status': 'HOLD_NO_AUTOMATIC_RETRY',
              'utc': datetime.now(timezone.utc).isoformat(), 'source_commit': COMMIT, 'source_tree': TREE,
              'source_sha256': SOURCE_SHA, 'source_store_admission_sha256': FACTS_SHA,
              'images': {}, 'resource_points': [], 'errors': [], 'additional_tests': 0, 'additional_closures': 0,
              'qualification': 'Metadata/hash observations only; no full-index/source-match/config/exclude semantic '
                  'review assertions, executable availability, global idle, SDK provenance/license acceptance, '
                  'build/cleanup admission or repaired older HOLD/STOP/NO-RETRY/CLOSED/terminal gaps. '
                  'Final file close/stdout/external tool settlement requires separate actual reconciliation.'}
    lock = parent = None
    acquired, created, captures, evidence_directory, result_sha = False, set(), {}, None, None

    def error_note(error):
        report['status'] = 'HOLD_NO_AUTOMATIC_RETRY'
        if len(report['errors']) < 12:
            report['errors'].append(type(error).__name__ + ': ' + str(error)[:1024])

    def take(path, cap, keep=False, bucket=None):
        image, data = read_image(path, cap, keep)
        captures[path] = (cap, image)
        (report['images'] if bucket is None else bucket)[str(path)] = image
        return data

    try:
        require(sys.argv == [str(SELF)] and sys.flags.isolated and sys.flags.no_site
                and sys.dont_write_bytecode and os.getuid() == os.geteuid() == 0, 'fixed isolated root entry')
        facts = json.loads(take(FACTS, 65536, True))
        require(report['images'][str(FACTS)]['sha256'] == FACTS_SHA and facts['owner'] == '/root'
                and (facts['source_commit'], facts['source_tree'], facts['store']) == (COMMIT, TREE, str(G))
                and facts['status'] == 'C18_READ_ONLY_SOURCE_PREPARATION_FACTS_NOT_BUILD_EXECUTION_ADMISSION',
                'accepted C18 source-store admission binding')
        take(SELF, 128 * 1024)
        evidence_directory = identity(directory_pin(D))
        require(absent(D / INTENT) and absent(D / RESULT), 'consumed evidence names; no retry')
        parent = od(LOCK.parent)
        require(pin(os.fstat(parent), True) == facts['coordination_parent_identity'], 'original coordination parent')
        lock = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        require(pin(os.fstat(lock)) == facts['original_lock'], 'original lock; never create or replace')
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        acquired = True
        require(absent(D / INTENT) and absent(D / RESULT), 'consumed evidence names under lock')
        intent = {'format': 'passvault-c18-readonly-facts-01-intent-v1', 'owner': '/root',
                  'status': 'ONE_READONLY_OBSERVATION_INTENT_NOT_DOWNSTREAM_ADMISSION',
                  'source': {'path': str(SELF), **report['images'][str(SELF)]},
                  'source_store_admission_sha256': FACTS_SHA, 'source_sha256': SOURCE_SHA,
                  'result': str(D / RESULT), 'work_deadline_seconds': 180,
                  'operation': 'Fixed metadata/hash reads only; no Git/subprocess/SDK write/deletion or retry'}
        report['intent_serialization_sha256'] = write_record(INTENT, intent, evidence_directory, created)
        resource_point('before metadata observations', True, report['resource_points'])
        before_guard = store_guard(facts, lock)
        report['metadata_directory'] = identity(before_guard['directories'][str(G)])
        for name, key in (('config', 'config'), ('HEAD', 'head'), (REF, 'branch_ref'), ('shallow', 'shallow')):
            data = take(G / name, 65536, key != 'config')
            require(report['images'][str(G / name)] == {'pin': facts[key]['pin'], 'sha256': facts[key]['sha256']},
                    'original bound store image: ' + name)
            if key == 'head':
                require(data == ('ref: ' + REF + '\n').encode(), 'original symbolic HEAD')
            if key == 'branch_ref':
                require(data == (COMMIT + '\n').encode(), 'current C18 branch')
            if key == 'shallow':
                require(data == (facts[key]['boundary_commit'] + '\n').encode(), 'original shallow boundary')
        source = json.loads(take(SOURCE, 2 * MIB, True))
        require(report['images'][str(SOURCE)]['sha256'] == SOURCE_SHA
                and (source['format'], source['commit'], source['tree']) ==
                    ('passvault-linux-checkout-source-v1', COMMIT, TREE), 'accepted shared SOURCE binding')
        report['source_manifest_members'] = len(source['files'])
        del source
        take(G / 'index', 4 * MIB)
        report['excludes'] = excludes()
        if report['excludes']['state'] == 'FILE':
            take(G / 'info/exclude', 65536)
        for path in TOOLS + (JAVA / 'bin/java',):
            take(path, 32 * MIB)
        release = take(JAVA / 'release', 32 * MIB, True)
        metadata = {}
        for line in release.decode('utf-8', 'strict').splitlines():
            key, separator, value = line.partition('=')
            if key in ('JAVA_VERSION', 'OS_ARCH'):
                require(separator and key not in metadata and len(value) >= 2
                        and value[0] == value[-1] == '"' and '"' not in value[1:-1], 'JDK release field')
                metadata[key] = value[1:-1]
        require(set(metadata) == {'JAVA_VERSION', 'OS_ARCH'} and metadata['JAVA_VERSION'].startswith('17.')
                and metadata['OS_ARCH'] == 'x86_64', 'observed JDK17/x86_64 release metadata')
        report['jdk'] = {'home': str(JAVA), 'release_metadata': metadata}
        del release
        report['tool_aliases'] = {str(path): alias(path, target) for path, target in ALIASES.items()}
        report['python3_alias'] = alias(PYTHON_ALIAS, PYTHON_IMAGE)
        require(all(absent(JAVA / 'bin' / name) for name in ('git', 'ruby')), 'JDK-bin Git/Ruby shadow present')
        report['jdk_bin_shadow_absence'] = {'git': True, 'ruby': True}
        sdk_pin = directory_pin(SDK)
        sdk = {'root': str(SDK), 'directory': identity(sdk_pin), 'directory_pin': sdk_pin,
               'metadata_paths': [str(path) for path in SDK_METADATA], 'images': {},
               'operation': 'READ_ONLY_EXISTING_SDK_METADATA_NO_INSTALL_COPY_MODIFICATION_LICENSE_ACCEPTANCE',
               'qualification': 'Existing /opt/android-sdk projection metadata only; not full SDK/compiler '
                   'availability, license assent, a new read-only mount or host-wide immutable snapshot.'}
        report['sdk_metadata_observation'] = sdk
        for path in SDK_METADATA:
            take(path, 128 * 1024, bucket=sdk['images'])
        for path, (cap, expected) in captures.items():
            require(read_image(path, cap)[0] == expected, 'final full pin/hash drift: ' + str(path))
        require(report['tool_aliases'] == {str(path): alias(path, target) for path, target in ALIASES.items()}
                and report['python3_alias'] == alias(PYTHON_ALIAS, PYTHON_IMAGE), 'final alias drift')
        require(all(absent(JAVA / 'bin' / name) for name in ('git', 'ruby')), 'final JDK-bin shadow drift')
        require(directory_pin(SDK) == sdk_pin and excludes() == report['excludes'], 'final SDK/exclude directory drift')
        after_guard = store_guard(facts, lock)
        require(after_guard == before_guard, 'final source-store guard drift')
        report['source_store_rechecks'] = {'before': before_guard, 'after': after_guard,
                                         'captured_file_pins_and_hashes_equal_at_second_read': True}
        resource_point('after metadata observations', False, report['resource_points'])
        tick()
        report['status'] = CAPTURED
    except BaseException as error:
        error_note(error)
    finally:
        # Closing the original lock FD releases this flock; no recreation, deletion or retry.
        report['cleanup'] = {'original_lock_acquired': acquired, 'lock_descriptor_closed': lock is None,
                             'coordination_parent_closed': parent is None, 'destructive_cleanup': 'NONE'}
        if lock is not None:
            try:
                os.close(lock)
                report['cleanup']['lock_descriptor_closed'] = True
            except BaseException as error:
                error_note(error)
        if parent is not None:
            try:
                os.close(parent)
                report['cleanup']['coordination_parent_closed'] = True
            except BaseException as error:
                error_note(error)
        report['elapsed_seconds'] = time.monotonic() - START
        report['evidence_directory'] = evidence_directory
        if INTENT in created:
            try:
                result_sha = write_record(RESULT, report, evidence_directory, created)
            except BaseException as error:
                error_note(error)
    return report, result_sha


if __name__ == '__main__':
    os.umask(0o077)
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(number, lambda signum, _frame: CANCEL.add(signum))
    signal.pthread_sigmask(signal.SIG_SETMASK, set())
    result, result_sha = main()
    print(encoded({'status': result['status'], 'result': str(D / RESULT), 'result_serialization_sha256': result_sha,
                   'errors': result['errors'], 'elapsed_seconds': result['elapsed_seconds']}).decode(), end='', flush=True)
    raise SystemExit(0 if result['status'] == CAPTURED and result_sha is not None else 70)
