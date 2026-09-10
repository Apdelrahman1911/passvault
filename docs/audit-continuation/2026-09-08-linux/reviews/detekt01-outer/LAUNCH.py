#!/usr/bin/python3.12
"""NEW Detekt01 SOURCE ONLY, pending source/index/store/instance admission.

Root externally freezes REQUEST + genuine reviewer approval, including under the
original lock. No old helper is imported. No hostile-root/global-idle/no-escape
claim; blocked syscalls/kernel failure can defeat every userspace time bound.
One serial root :detekt; no tests, GUI, old store, fake index or inventory bypass.
"""
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import signal
import stat
import subprocess
import sys
import time

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
R = Path('/root/projects/PassVault/audit-runtime-linux-detekt01')
E = B / 'runs/linux-detekt01'
SELF = B / 'reviews/detekt01-outer/LAUNCH.py'
REQUEST = B / 'requests/LINUX-DETEKT-01.json'
APPROVAL = B / 'reviews/verification/LINUX-DETEKT01-INSTANCE-ACCEPT.json'
LOCK = Path('/root/projects/PassVault/.audit-coordination-linux-20260908/build.lock')
# Actual source/store/index/tool/exclude identities require root's fresh review.
# Paths or old receipts alone never admit the retired source store or an index.
GITDIR = Path('/root/projects/PassVault/passvault-publication-20260910-01/.git')
INDEX_SOURCE = GITDIR / 'index'
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = 'INFO_ABSENT'
GIT_METADATA, GIT_INDEX = R / 'git-metadata', R / 'git-index'
INNER, INIT = W / 'scripts/audit/linux_detekt_01.py', W / 'scripts/audit/detekt_01.init.gradle'
SOURCE = B / 'reviews/desktop-tray/source-prepare02/SOURCE.json'
JAVA = Path('/usr/lib/jvm/java-17-openjdk-amd64/bin/java')
RELEASE = JAVA.parent.parent / 'release'
PYTHON, INNER_PYTHON, UNSHARE = '/usr/bin/python3.12', '/usr/bin/python3', '/usr/bin/unshare'
RUN, PURPOSE = 'linux-detekt01', 'ONE_LINUX_DETEKT01'
COMMIT = 'd3d46db51d9fa69a4060250e71e0477c6b930d3a'
TREE = '245160648cb79873f41da41969a5b48d4c2057e1'
MEMBERS = 2495  # Exact published C13 source and full stage0 index; not execution admission.
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
BASE_REQUIRED = (INNER, INIT, SOURCE, JAVA, RELEASE, Path('/usr/bin/mount'))
REQUIRED = BASE_REQUIRED + (GIT_IMAGE, RUBY_IMAGE)
IMAGES = ()  # Final fixed set includes the externally reviewed optional exclude only when present.
PARENTS = (R.parent, E.parent, GITDIR, LOCK.parent)
DEVICE = {'directory_device': 23, 'regular_file_device': 24}
EXPECTED_LOCK = {'dev': 24, 'ino': 14189001, 'uid': 0, 'mode': 33152, 'nlink': 1, 'bytes': 0, 'mtime_ns': 1788910891124735946, 'ctime_ns': 1788910891124735946}  # Exact original identity; no execution admission.
FROZEN = {INNER: 'a7895c455f7cf01577a46890d9fc0b72aca94bfc7766c6b9cd9bde299f03140b', INIT: '5beb8165ae491258c0fae5a5a4f119cbe778157d6a577aacff79fe93fc33ae97', SOURCE: 'ee9361f96da7cc7de04b8949453acf971027640144527961af0d38388a561ffc'}
REVIEW_ASSERTIONS = (
    'ordinary_full_stage0_index_matches_source', 'no_split_sparse_unmerged_index',
    'standalone_store_without_redirects', 'local_config_and_excludes_reviewed',
    'no_external_git_config_or_executable_mechanisms', 'publication_store_frozen_for_run',
)
TOP = 'checkout home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state git-metadata'.split()
DIRS = TOP
ODIR, MIB = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, 1024 ** 2
BUILDLIKE = re.compile(r'(java|javac|gradle.*|Gradle.*|kotlinc.*|kotlin.*|Kotlin.*|xcodebuild|clang.*|'
    r'gcc.*|g\+\+.*|cc|c\+\+|cc1.*|cmake|ninja|make|gmake|ctest|mvn.*|msbuild|dotnet|pytest.*|cargo|'
    r'rustc|jpackage|jlink|aapt2?|d8|r8|zipalign|adb|emulator.*|qemu-system.*)')
START = time.monotonic()
END, WORK_END, last_watch = START + 6000, START + 5250, 0.0
reasons, resources, churn, directories, images = [], [], 0, {}, {}
child, lock_fd, owned_domain, cancel_written, index_copy = None, None, None, False, None
receipt = {'format': 'passvault-linux-detekt01-outer-receipt-v1', 'run_id': RUN,
           'status': 'HOLD', 'inner_started': False, 'cleanup': 'NOT_ATTEMPTED', 'children': []}

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
    alias = stat.S_ISLNK(before.st_mode)
    target = Path(PYTHON) if alias and str(path) == INNER_PYTHON else path
    require(not alias or (target != path and os.readlink(path.name, dir_fd=parent) == 'python3.12'), 'input symlink')
    fd = os.open(target.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
    try:
        initial, data, digest, size = os.fstat(fd), bytearray(), hashlib.sha256(), 0
        require(stat.S_ISREG(initial.st_mode) and initial.st_uid == 0 and initial.st_nlink == 1
                and not initial.st_mode & 0o022 and initial.st_size <= cap, 'bounded root single-link regular input')
        while part := os.read(fd, 65536):
            require(not reasons and time.monotonic() < END, 'capture cancelled/expired')
            size += len(part)
            require(size <= cap, 'input growth cap')
            digest.update(part)
            if retain:
                data.extend(part)
        require(size == initial.st_size and pin(initial) == pin(os.fstat(fd))
                == pin(os.stat(target.name, dir_fd=parent, follow_symlinks=False)), 'input changed during capture')
        require(pin(before) == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'input name changed')
        value = {'sha256': digest.hexdigest(), 'pin': pin(initial)}
        if alias:
            value.update(link_pin=pin(before), link_target='python3.12')
        return bytes(data), value
    finally:
        os.close(fd)

def input_cap(path):
    if path in (INDEX_SOURCE, GIT_INDEX):
        return 4 * MIB
    if path in (GITDIR / 'config', GITDIR / 'info/exclude'):
        return 65536
    return 32 * MIB

def inventory_contract():
    binding = request['git_inventory_binding']
    require(isinstance(binding, dict) and set(binding) == {
            'publication_store', 'source_sha256', 'index_sha256', 'metadata_directory',
            'excludes', 'reviewed_assertions'}, 'exact Git inventory binding schema')
    require(binding['publication_store'] == str(GITDIR) and binding['source_sha256'] == FROZEN[SOURCE]
            and isinstance(binding['index_sha256'], str)
            and re.fullmatch(r'[0-9a-f]{64}', binding['index_sha256']), 'source/store/index binding')
    metadata = binding['metadata_directory']
    require(isinstance(metadata, dict) and set(metadata) == {'dev', 'ino', 'uid', 'mode'}
            and all(type(v) is int for v in metadata.values()) and metadata['dev'] == DEVICE['directory_device']
            and stat.S_ISDIR(metadata['mode']) and metadata['uid'] == 0 and not metadata['mode'] & 0o022,
            'original publication-store directory identity')
    exclusion = binding['excludes']
    require(isinstance(exclusion, dict) and set(exclusion) == {'state', 'info_directory'}
            and exclusion['state'] == EXCLUDE_STATE, 'exact optional exclude state')
    if EXCLUDE_STATE == 'INFO_ABSENT':
        require(exclusion['info_directory'] is None, 'absent info must have no invented directory pin')
    else:
        info = exclusion['info_directory']
        require(isinstance(info, dict) and set(info) == {'dev', 'ino', 'uid', 'mode', 'nlink'}
                and all(type(v) is int for v in info.values()) and info['dev'] == DEVICE['directory_device']
                and stat.S_ISDIR(info['mode']) and info['uid'] == 0 and not info['mode'] & 0o022
                and info['nlink'] > 0, 'original optional info directory pin')
    assertions = binding['reviewed_assertions']
    require(isinstance(assertions, dict) and set(assertions) == set(REVIEW_ASSERTIONS)
            and all(assertions[k] is True for k in REVIEW_ASSERTIONS),
            'genuine external index/config/store review required; declarations are not a semantic parser')
    require(isinstance(request['tool_aliases'], dict)
            and set(request['tool_aliases']) == {'/usr/bin/git', '/usr/bin/ruby'}, 'fixed Git/Ruby aliases')

def tool_aliases():
    for path, target in ((Path('/usr/bin/git'), GIT_IMAGE), (Path('/usr/bin/ruby'), RUBY_IMAGE)):
        expected = request['tool_aliases'][str(path)]
        require(isinstance(expected, dict) and set(expected) == {'lstat', 'readlink', 'resolved_image'}
                and expected['resolved_image'] == str(target), 'exact tool alias schema/image')
        parent = directory(path.parent)
        original = pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
        require(original == expected['lstat'] and original['uid'] == 0 and original['nlink'] == 1,
                'original tool alias type/owner/pin')
        if stat.S_ISLNK(original['mode']):
            link = os.readlink(path.name, dir_fd=parent)
            destination = Path(link) if Path(link).is_absolute() else path.parent / link
            require(link == expected['readlink'] and '..' not in destination.parts and destination == target,
                    'only reviewed direct tool alias; no chained/guessed fallback')
        else:
            require(stat.S_ISREG(original['mode']) and expected['readlink'] is None and path == target
                    and original == images[str(target)]['pin'], 'reviewed regular tool alias')
        require(original == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)),
                'tool alias changed during read')
        require(capture(target, 32 * MIB, False)[1] == images[str(target)], 'resolved regular tool image changed')

def store_guard():
    binding = request['git_inventory_binding']
    require(same_dir(pin(os.fstat(directory(GITDIR)), True), binding['metadata_directory']),
            'original new publication store changed')
    # Bounded negative guards only; root's standalone/config/index semantic proof is separate.
    for relative in ('commondir', 'config.worktree', 'objects/info/alternates', 'objects/info/http-alternates'):
        require(not os.path.lexists(GITDIR / relative), 'unadmitted Git redirect/worktree input')
    exclusion = binding['excludes']
    if EXCLUDE_STATE == 'INFO_ABSENT':
        require(not os.path.lexists(GITDIR / 'info'), 'reviewed info absence changed')
    else:
        require(pin(os.fstat(directory(GITDIR / 'info')), True) == exclusion['info_directory'],
                'original reviewed info directory changed')
        if EXCLUDE_STATE == 'EXCLUDE_ABSENT':
            require(not os.path.lexists(GITDIR / 'info/exclude'), 'reviewed exclude absence changed')
    for path in (GITDIR / 'config',) + ((GITDIR / 'info/exclude',) if EXCLUDE_STATE == 'FILE' else ()):
        require(capture(path, 65536, False)[1] == images[str(path)], 'reviewed config/exclude changed')
    require(images[str(INDEX_SOURCE)]['sha256'] == binding['index_sha256'], 'admitted full index hash mismatch')

def copy_index():
    global index_copy
    data, original = capture(INDEX_SOURCE, 4 * MIB)
    require(original == images[str(INDEX_SOURCE)] and 0 < len(data) <= 4 * MIB
            and original['sha256'] == request['git_inventory_binding']['index_sha256'], 'exact bounded input index')
    fd = new(GIT_INDEX)
    try:
        view = memoryview(data)
        while view:
            tick(WORK_END)
            count = os.write(fd, view)
            require(count > 0, 'zero index write')
            view = view[count:]
        os.fchmod(fd, 0o400)
        os.fsync(fd)
        written = pin(os.fstat(fd))
    finally:
        os.close(fd)
    os.fsync(directory(R))
    copied, actual = capture(GIT_INDEX, 4 * MIB)
    require(copied == data and actual['sha256'] == original['sha256'] and actual['pin'] == written
            and written['mode'] == stat.S_IFREG | 0o400, 'sealed index copy readback mismatch')
    images[str(GIT_INDEX)] = actual
    index_copy = {'source': str(INDEX_SOURCE), 'source_image': original,
                  'source_sha256': original['sha256'], 'copy': str(GIT_INDEX), 'copy_image': actual}
    receipt['index_copy'] = index_copy
    output('OUTER-INDEX-COPY.json', encoded(index_copy))

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
    require(E in directories and '/' not in name, 'owned flat evidence required')
    fd = new(E / name)
    try:
        view = memoryview(data)
        while view:
            count = os.write(fd, view)
            require(count > 0, 'zero evidence write')
            view = view[count:]
        os.fsync(fd)
    finally:
        os.close(fd)
    os.fsync(directory(E))

def latch(message):
    global cancel_written
    note(message)
    if E in directories and not cancel_written:
        cancel_written = True  # One exclusive attempt only, including a partial write/finalization failure.
        output('CANCEL', encoded({'run_id': RUN, 'reason': str(message)[:1024]}))

def birth(pid):
    raw = Path(f'/proc/{pid}/stat').read_text()
    fields = raw[raw.rfind(')') + 2:].split()
    return {'pid': pid, 'ppid': int(fields[1]), 'start': int(fields[19])}

def identity(pid, namespaces=('pid', 'mnt')):
    value = birth(pid)
    value['namespaces'] = {k: os.readlink(f'/proc/{pid}/ns/{k}') for k in namespaces}
    require(birth(pid) == {k: value[k] for k in ('pid', 'ppid', 'start')}, 'unstable process identity')
    return value

def screen():
    global churn
    deadline = min(time.monotonic() + 5, END)
    with os.scandir('/proc') as entries:
        for index, entry in enumerate(entries):
            require(index < 8192 and time.monotonic() < deadline, 'host-screen entry/time bound')
            name = entry.name
            if not name.isdecimal():
                continue
            try:
                comm = Path(f'/proc/{name}/comm').read_text().strip()
            except FileNotFoundError:
                churn += 1  # Unclassified disappearance BEFORE any positive indication; NOT benign/idle proof.
                continue
            except OSError:
                raise RuntimeError('live/unreadable potential host workload')
            if BUILDLIKE.fullmatch(comm):
                try:
                    row = identity(int(name))
                    require(Path(f'/proc/{name}/comm').read_text().strip() == comm and identity(int(name)) == row,
                            'positive process birth/namespace/comm changed')
                except (OSError, RuntimeError):
                    raise RuntimeError('positive buildlike process vanished/changed/unreadable')
                require(child is not None and child['p'].returncode is None and owned_domain == row['namespaces'],
                        'host buildlike conflict: ' + name + ':' + comm)
    require(time.monotonic() < deadline, 'host-screen final time bound')

def watch(entry=False):
    global last_watch
    memory = dict(line.split(':', 1) for line in Path('/proc/meminfo').read_text().splitlines())
    available, total = (int(memory[k].split()[0]) * 1024 for k in ('MemAvailable', 'MemTotal'))
    disks = [os.fstatvfs(directory(p)) for p in PARENTS[:2]]
    free, now = min(s.f_bavail * s.f_frsize for s in disks), time.monotonic()
    if not resources or now - START - resources[-1]['elapsed'] >= 30:
        resources.append({'elapsed': round(now - START, 3), 'disk_available': free, 'MemAvailable': available, 'MemTotal': total})
    require(free >= (12 if entry else 8) * 1024 ** 3 and available >= total * (0.25 if entry else 0.20), 'resource floor')
    screen()
    last_watch = now

def tick(deadline):
    require(not reasons and time.monotonic() < deadline, 'cancelled/deadline')
    if time.monotonic() - last_watch >= 5:
        watch()

def bind_domain():
    global owned_domain
    p = child['p']
    try:
        current = identity(p.pid)
        pair = {'pid': os.readlink(f'/proc/{p.pid}/ns/pid_for_children'), 'mnt': current['namespaces']['mnt']}
        require(birth(p.pid) == child['row']['birth'], 'original direct child changed')
        if pair['pid'] != parent_ns['pid'] and pair['mnt'] != parent_ns['mnt']:
            require(owned_domain is None or owned_domain == pair, 'original namespace changed')
            owned_domain = pair
            child['row']['owned_namespaces'] = pair
    except OSError:
        require(p.poll() is not None, 'live original child namespace unreadable')  # Only after original pidfd.

def supervise():
    global child, owned_domain
    c, p = child, child['p']
    require(c['fd'] is not None and not c['final_wait'], 'no pidfd or final wait already consumed; HOLD')
    while p.returncode is None:
        try:
            if c['isolated']:
                bind_domain()
            if p.poll() is not None:
                break
            require(os.fstat(c['out']).st_size <= (MIB // 2 if c['isolated'] else 128 * MIB)
                    and os.fstat(c['err']).st_size <= MIB // 2, 'child output cap')
            require(time.monotonic() < c['deadline'], 'child/work deadline')
            if time.monotonic() - last_watch >= 5:
                watch()
        except BaseException as error:
            latch(type(error).__name__ + ':' + str(error))
        if reasons:
            latch(reasons[0])
            if c['drain'] is None:
                c['drain'] = min(time.monotonic() + (750 if c['isolated'] else 0), END)
            if time.monotonic() >= c['drain']:
                c['row']['pidfd_kill_attempted'] = True
                c['final_wait'] = True  # Monotone before syscall; finally cannot retry kill or 5-second wait.
                try:
                    signal.pidfd_send_signal(c['fd'], signal.SIGKILL)
                except ProcessLookupError:
                    pass  # Original child terminal, not a PID fallback.
                finally:
                    c['row']['exit'] = p.wait(timeout=5)
                break
        time.sleep(0.25)
    c['row']['exit'] = p.returncode
    child, owned_domain = None, None  # Transfer out before the sole descriptor-close attempt.
    os.close(c['fd'])
    require(os.fstat(c['out']).st_size <= (MIB // 2 if c['isolated'] else 128 * MIB)
            and os.fstat(c['err']).st_size <= MIB // 2, 'final child output cap')
    require(reasons or time.monotonic() <= c['deadline'], 'completed beyond child/work deadline')
    return p.returncode

def drive(command, env, out, err, seconds, isolated, stdin=subprocess.DEVNULL):
    global child
    tick(WORK_END)
    require(child is None and seconds > 0, 'unfinished child/expired launch')
    require(not isolated or stdin == subprocess.DEVNULL, 'isolated child requires DEVNULL stdin')
    row = {'command': command, 'environment': env, 'pidfd_kill_attempted': False, 'exit': None}
    receipt['children'].append(row)
    child = {'p': subprocess.Popen(command, cwd=str(W), env=env, stdin=stdin, stdout=out,
                stderr=err, close_fds=True), 'fd': None, 'row': row, 'drain': None, 'final_wait': False,
             'out': out, 'err': err, 'isolated': isolated, 'deadline': min(time.monotonic() + seconds, WORK_END)}
    receipt['inner_started'] = receipt['inner_started'] or isolated
    child['fd'] = os.pidfd_open(child['p'].pid, 0)  # No poll/wait before original unreaped direct-child pidfd.
    row['birth'] = birth(child['p'].pid)  # Does not require live namespaces: fast Git may already be a zombie.
    require(row['birth']['ppid'] == os.getpid(), 'not original direct child')
    return supervise()

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

def oid_requests(expected):
    data = b''.join(row['git_blob'].encode('ascii') + b'\n' for row in expected.values())
    require(len(data) == MEMBERS * 41 and len(data) <= 128 * 1024, 'canonical bounded OID input size')
    fd = new(R / 'source.oids')
    try:
        view = memoryview(data)
        while view:
            tick(WORK_END)
            count = os.write(fd, view)
            require(count > 0, 'zero OID input write')
            view = view[count:]
        os.fsync(fd)
        written = pin(os.fstat(fd))
    finally:
        os.close(fd)
    os.fsync(directory(R))
    actual, original = capture(R / 'source.oids', 128 * 1024)
    require(actual == data and original['pin'] == written, 'original OID input changed before capture')
    return data, original

def check_oid_input(fd, data, original):
    path = R / 'source.oids'
    require(pin(os.fstat(fd)) == original['pin'] == pin(os.stat(path.name, dir_fd=directory(R), follow_symlinks=False)),
            'original OID input descriptor/name changed')
    os.lseek(fd, 0, os.SEEK_SET)
    digest, size = hashlib.sha256(), 0
    while part := os.read(fd, 65536):
        tick(WORK_END)
        require(size + len(part) <= len(data) and part == data[size:size + len(part)], 'OID input bytes changed')
        digest.update(part)
        size += len(part)
    require(size == len(data) and digest.hexdigest() == original['sha256'] and pin(os.fstat(fd)) == original['pin']
            == pin(os.stat(path.name, dir_fd=directory(R), follow_symlinks=False)), 'OID input hash/pin changed')
    os.lseek(fd, 0, os.SEEK_SET)  # The child reads the original regular FD; no interactive pipe or alternate input.

def materialize(expected, folders, original):
    deadline = min(time.monotonic() + 180, WORK_END)
    source_fd = os.open('source.blobs', os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=directory(R))
    with os.fdopen(source_fd, 'rb') as raw:
        def stream_pin():
            require(pin(os.fstat(raw.fileno())) == original['pin'] ==
                    pin(os.stat('source.blobs', dir_fd=directory(R), follow_symlinks=False)), 'original blob stream changed')

        def objects(extract):
            tick(deadline)
            stream_pin()
            raw.seek(0)  # Same original descriptor, fresh framing/digests on both passes; no saved offsets.
            digest, total = hashlib.sha256(), 0
            for name, row in expected.items():
                tick(deadline)
                header = raw.readline(64)
                match = re.fullmatch(rb'([0-9a-f]{40}) blob (0|[1-9][0-9]{0,7})\n', header)
                require(match is not None and match[1].decode('ascii') == row['git_blob'], 'blob response header/OID/order/type')
                size = int(match[2])
                total += len(header)
                require(0 <= size <= 32 * MIB and total + size + 1 <= original['pin']['bytes'] <= 128 * MIB,
                        'blob response size/stream cap')
                digest.update(header)
                blob, remaining, parent, out = hashlib.sha1(b'blob ' + match[2] + b'\0'), size, None, None
                try:
                    if extract:
                        parent = under(directory(R / 'checkout'), name.split('/')[:-1])
                        out = os.open(name.split('/')[-1], os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                                      0o600, dir_fd=parent)
                        s = os.fstat(out)
                        require(s.st_dev == DEVICE['regular_file_device'] and s.st_uid == 0 and s.st_nlink == 1
                                and s.st_mode == stat.S_IFREG | 0o600, 'materialized file device/type/owner')
                    while remaining:
                        tick(deadline)
                        part = raw.read(min(65536, remaining))
                        require(part, 'truncated blob payload')
                        remaining -= len(part)
                        total += len(part)
                        blob.update(part)
                        digest.update(part)
                        if extract:
                            view = memoryview(part)
                            while view:
                                tick(deadline)
                                count = os.write(out, view)
                                require(count > 0, 'zero source write')
                                view = view[count:]
                    require(blob.hexdigest() == row['git_blob'], 'raw Git blob mismatch')
                    framing = raw.read(1)
                    require(framing == b'\n', 'missing blob framing LF')
                    digest.update(framing)
                    total += 1
                    if extract:
                        os.fchmod(out, int(row['git_mode'], 8) & 0o777)
                        os.fsync(out)
                finally:
                    try:
                        if out is not None:
                            os.close(out)
                    finally:
                        if parent is not None:
                            os.close(parent)
            require(raw.read(1) == b'' and total == original['pin']['bytes'] and digest.hexdigest() == original['sha256'],
                    'blob response count/trailing bytes/hash mismatch')
            stream_pin()
            tick(deadline)

        objects(False)  # Every admitted raw blob verified BEFORE writing source files or nested directories.
        for name in sorted(folders, key=lambda p: (p.count('/'), p)):
            tick(deadline)
            fd = under(directory(R / 'checkout'), name.split('/')[:-1])
            try:
                os.mkdir(name.split('/')[-1], 0o700, dir_fd=fd)
            finally:
                os.close(fd)
        objects(True)

def source_check(expected):
    result, different, deadline = [], [], min(time.monotonic() + 180, END)
    for name, row in sorted(expected.items()):
        tick(deadline)
        parent = under(directory(R / 'checkout'), name.split('/')[:-1])
        try:
            fd = os.open(name.split('/')[-1], os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK, dir_fd=parent)
            try:
                s = os.fstat(fd)
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
                result.append([name, row['git_mode'], blob.hexdigest(), size, digest.hexdigest()])
                if digest.hexdigest() != row['checkout_sha256'] or size != row['checkout_size']:
                    different.append(name)
            finally:
                os.close(fd)
        finally:
            os.close(parent)
    return {'members': len(result), 'raw_identity_sha256': hashlib.sha256(encoded(result)).hexdigest(),
            'representation': 'RAW_GIT_BLOBS', 'not_checkout_normalized': different}

def authority():
    require(pin(os.fstat(lock_fd)) == request['lock'] == pin(os.stat(LOCK, follow_symlinks=False)), 'original lock changed')
    require(capture(REQUEST, MIB)[1] == request_image and capture(APPROVAL, 65536)[1] == approval_image, 'packet changed under lock')
    for path in tuple(directories):
        directory(path)
    for path in IMAGES:
        require(capture(path, input_cap(path), False)[1] == images[str(path)], 'admitted image changed')
    tool_aliases()
    store_guard()
    if index_copy is not None:
        require(capture(GIT_INDEX, 4 * MIB, False)[1] == index_copy['copy_image'] == images[str(GIT_INDEX)],
                'original sealed runtime index changed')

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

def remove_runtime():
    no_runtime_mounts()
    mountpoint = directory(GIT_METADATA)
    require(pin(os.fstat(mountpoint), True) == directories[GIT_METADATA][1],
            'original underlying metadata mountpoint changed')
    with os.scandir(mountpoint) as entries:
        require(next(entries, None) is None, 'underlying metadata mountpoint is not empty')
    require(capture(GIT_INDEX, 4 * MIB, False)[1] == index_copy['copy_image'],
            'original underlying sealed index changed')
    receipt['git_underlying_after'] = {'metadata_directory': directories[GIT_METADATA][1],
                                      'empty': True, 'index_image': index_copy['copy_image']}
    root, snapshot = directory(R), {}
    def inventory(fd, prefix=()):
        for name in os.listdir(fd):
            tick(END)
            require(len(snapshot) < 250000 and (prefix or name in TOP + ['git-index', 'source.oids', 'source.blobs']), 'cleanup count/top-level allowlist')
            relative, s = prefix + (name,), os.stat(name, dir_fd=fd, follow_symlinks=False)
            isdir = stat.S_ISDIR(s.st_mode)
            require(s.st_uid == 0 and (isdir or (stat.S_ISREG(s.st_mode) and s.st_nlink == 1))
                    and s.st_dev == DEVICE['directory_device' if isdir else 'regular_file_device'], 'cleanup type/owner/device')
            snapshot[relative] = pin(s, isdir)
            if isdir:
                nested = under(root, relative, snapshot)
                try:
                    inventory(nested, relative)
                finally:
                    os.close(nested)
    inventory(root)  # Complete bounded snapshot BEFORE any deletion; no retry after partial failure.
    no_runtime_mounts()  # Recheck immediately before first destructive operation, including same-device bind mounts.
    receipt['cleanup'] = {'status': 'STARTED', 'snapshot_entries': len(snapshot), 'removed': 0}
    for parts in sorted(snapshot, key=lambda p: (-len(p), p)):
        tick(END)
        fd = under(root, parts[:-1], snapshot)
        try:
            actual, original = os.stat(parts[-1], dir_fd=fd, follow_symlinks=False), snapshot[parts]
            isdir = stat.S_ISDIR(original['mode'])
            require(same_dir(pin(actual, True), original) if isdir else pin(actual) == original, 'cleanup original changed')
            (os.rmdir if isdir else os.unlink)(parts[-1], dir_fd=fd)
            receipt['cleanup']['removed'] += 1
        finally:
            os.close(fd)
    require(not os.listdir(root) and same_dir(pin(os.stat(R.name, dir_fd=directory(R.parent), follow_symlinks=False), True),
            directories[R][1]), 'original runtime not empty/name changed')
    os.rmdir(R.name, dir_fd=directory(R.parent))
    os.fsync(directory(R.parent))
    require(os.fstat(root).st_nlink == 0 and not os.path.lexists(R), 'runtime removal not established')
    receipt['cleanup']['status'] = 'REMOVED_ORIGINAL_RUNTIME'

def main():
    global lock_fd, parent_ns, request, request_image, approval_image, IMAGES
    require(sys.argv == [str(SELF)] and sys.executable == PYTHON and not sys.flags.optimize and sys.flags.isolated and
            sys.flags.no_site and sys.dont_write_bytecode and dict(os.environ) == ENV and os.getuid() == os.geteuid() == 0, 'fixed entry')
    require(all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{40}', value) for value in (COMMIT, TREE))
            and type(MEMBERS) is int and 1 <= MEMBERS <= 3196
            and all(isinstance(path, Path) and path.is_absolute() and '..' not in path.parts
                    for path in (GITDIR, INDEX_SOURCE, GIT_IMAGE, RUBY_IMAGE))
            and GITDIR == Path('/root/projects/PassVault/passvault-publication-20260910-01/.git')
            and all(path != R and R not in path.parents and path != E and E not in path.parents
                    for path in (GITDIR, INDEX_SOURCE, GIT_IMAGE, RUBY_IMAGE))
            and EXCLUDE_STATE in ('INFO_ABSENT', 'EXCLUDE_ABSENT', 'FILE')
            and isinstance(DEVICE, dict) and set(DEVICE) == {'directory_device', 'regular_file_device'}
            and all(type(value) is int and value >= 0 for value in DEVICE.values())
            and isinstance(EXPECTED_LOCK, dict)
            and set(EXPECTED_LOCK) == {'dev', 'ino', 'uid', 'mode', 'nlink', 'bytes', 'mtime_ns', 'ctime_ns'}
            and all(type(value) is int for value in EXPECTED_LOCK.values())
            and EXPECTED_LOCK['dev'] == DEVICE['regular_file_device'] and EXPECTED_LOCK['uid'] == 0
            and EXPECTED_LOCK['mode'] == stat.S_IFREG | 0o600
            and EXPECTED_LOCK['nlink'] == 1 and EXPECTED_LOCK['bytes'] == 0
            and all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{64}', value) for value in FROZEN.values()),
            'PENDING Detekt01 source/index/store/tool/exclude/device/lock binding: source-only, no admission')
    raw, request_image = capture(REQUEST, MIB)
    request = decode(raw)
    raw, approval_image = capture(APPROVAL, 65536)
    approval = decode(raw)
    require(set(request) == {'format', 'run_id', 'purpose', 'commit', 'tree', 'parent_namespaces', 'directories',
            'lock', 'images', 'coordination', 'device_model', 'git_inventory_binding', 'tool_aliases'}
            and request['device_model'] == DEVICE, 'request/device schema')
    inventory_contract()
    IMAGES = tuple(dict.fromkeys(REQUIRED + (SELF, Path(UNSHARE), Path(PYTHON), Path(INNER_PYTHON),
        Path('/usr/bin/env'), INDEX_SOURCE, GITDIR / 'config')
        + ((GITDIR / 'info/exclude',) if EXCLUDE_STATE == 'FILE' else ())))
    require(request['format'] == 'passvault-linux-detekt01-request-v1' and request['run_id'] == RUN and
            request['purpose'] == PURPOSE and request['commit'] == COMMIT and request['tree'] == TREE, 'request purpose/source')
    require(request['coordination'] == {'sole_build_owner': '/root', 'agents_quiescent': True, 'no_ci': True}
            and request['coordination']['agents_quiescent'] is True and request['coordination']['no_ci'] is True,
            'coordination not admitted')
    parent_ns = {k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')}
    entry_parent = identity(os.getppid(), ('pid', 'mnt'))
    require(request['parent_namespaces'] == parent_ns == entry_parent['namespaces'], 'original parent/caller namespace mismatch')
    receipt['parent_at_entry'] = entry_parent  # Provenance only, never signalling/adoption/deletion authority.
    require(set(request['directories']) == {str(p) for p in PARENTS} and set(request['images']) == {str(p) for p in IMAGES}, 'fixed sets')
    require(set(approval) == {'format', 'reviewer', 'disposition', 'purpose', 'run_id', 'request_sha256', 'sources'} and
            approval['format'] == 'passvault-linux-detekt01-approval-v1' and approval['reviewer'] == '/root/verification' and
            approval['disposition'] == 'ACCEPT_EXACT_NEW_INSTANCE' and approval['purpose'] == PURPOSE and approval['run_id'] == RUN and
            approval['request_sha256'] == request_image['sha256'] and approval['sources'] ==
            {str(p): request['images'][str(p)]['sha256'] for p in (SELF, INNER, INIT, SOURCE)}, 'missing/stale/nonaccepting genuine review')
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(pin(os.fstat(lock_fd)) == request['lock'] == EXPECTED_LOCK
            == pin(os.stat(LOCK, follow_symlinks=False)), 'wrong freshly admitted original lock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    receipt['parent_under_lock'] = identity(os.getppid(), ('pid', 'mnt'))
    require(receipt['parent_under_lock'] == entry_parent, 'original parent changed before locked intake')
    source = None
    for path in IMAGES:
        data, actual = capture(path, input_cap(path), path == SOURCE)
        require(actual == request['images'][str(path)] and actual['sha256'] == FROZEN.get(path, actual['sha256']), 'input image/pin mismatch')
        images[str(path)] = actual
        if path == SOURCE:
            source = decode(data)
    for path in PARENTS:
        require(same_dir(pin(os.fstat(directory(path)), True), request['directories'][str(path)]), 'original parent mismatch')
    authority()
    watch(entry=True)
    tick(WORK_END)
    require(source['format'] == 'passvault-linux-checkout-source-v1'
            and source['commit'] == COMMIT and source['tree'] == TREE
            and isinstance(source['checkout_eol_qualifications'], list)
            and len(source['checkout_eol_qualifications']) == 2, 'source manifest identity/EOL qualifications')
    expected, folders = source_inventory(source)
    require(not os.path.lexists(R) and not os.path.lexists(E), 'consumed runtime/evidence name')
    os.umask(0o077)
    directory(E, True)
    directory(R, True)
    receipt.update(request=request_image, approval=approval_image, parent_namespaces=parent_ns, device_model=DEVICE)
    allocation = {'format': 'passvault-linux-detekt01-allocation-v1', 'run_id': RUN, 'purpose': PURPOSE,
        'commit': COMMIT, 'tree': TREE, 'source_members': MEMBERS, 'source_representation': 'RAW_GIT_BLOBS',
        'allocated_directories': {str(p): directories[p][1] for p in (E, R)},
        'parents': {str(p): directories[p][1] for p in PARENTS}, 'parent_namespaces': parent_ns,
        'packet': {'request': request_image, 'approval': approval_image},
        'sources': {str(p): images[str(p)] for p in (SELF, INNER, INIT, SOURCE)}, 'device_model': DEVICE,
        'qualification': 'Original fresh E/R allocation evidence only; NOT materialization, settlement or cleanup admission. No authority for any old or consumed instance.'}
    receipt['allocation'] = allocation  # Retain origin even if the sole permanent allocation-record write fails.
    output('OUTER-ALLOCATION.json', encoded(allocation))
    os.fsync(directory(E.parent))
    os.fsync(directory(R.parent))
    for path in (E / 'logs', E / 'reports', *(R / p for p in DIRS)):
        directory(path, True)
    copy_index()
    authority()
    oid_data, oid_image = oid_requests(expected)
    receipt['raw_transport'] = {'format': 'git-cat-file-batch-raw-blobs', 'members': len(expected),
        'ordered_request': {str(R / 'source.oids'): oid_image}, 'stdin_original_verified_before_after': False}
    git_env = dict(ENV, HOME=str(R / 'home'), XDG_CONFIG_HOME=str(R / 'xdg-config'), GIT_CONFIG_NOSYSTEM='1',
        GIT_CONFIG_SYSTEM='/dev/null', GIT_CONFIG_GLOBAL='/dev/null', GIT_OPTIONAL_LOCKS='0',
        GIT_TERMINAL_PROMPT='0', GIT_ASKPASS='/bin/false', GIT_SSH_COMMAND='/bin/false',
        GIT_NO_REPLACE_OBJECTS='1', GIT_NO_LAZY_FETCH='1')
    command = [str(GIT_IMAGE), '--git-dir=' + str(GITDIR)]
    for option in ('core.hooksPath=/dev/null', 'core.fsmonitor=false', 'gc.auto=0',
                   'maintenance.auto=false', 'protocol.allow=never', 'commit.gpgsign=false'):
        command += ['-c', option]
    command += ['cat-file', '--batch']  # Plain raw objects: no filters/textconv/attributes/path interpretation.
    for isolated, outpath, errpath in ((False, R / 'source.blobs', E / 'logs/outer-git.stderr'),
                                      (True, E / 'logs/outer-unshare.stdout', E / 'logs/outer-unshare.stderr')):
        stdin = subprocess.DEVNULL if isolated else os.open('source.oids', os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC,
                                                           dir_fd=directory(R))
        try:
            out = new(outpath)
            try:
                err = new(errpath)
                try:
                    if not isolated:
                        check_oid_input(stdin, oid_data, oid_image)
                    code = drive(command, ENV if isolated else git_env, out, err,
                                 WORK_END - time.monotonic() if isolated else 120, isolated, stdin)
                    if not isolated:
                        check_oid_input(stdin, oid_data, oid_image)
                        receipt['raw_transport']['stdin_original_verified_before_after'] = True
                    os.fsync(out)
                    os.fsync(err)
                    if not isolated:
                        produced_blob_pin = pin(os.fstat(out))
                finally:
                    os.close(err)
            finally:
                os.close(out)
        finally:
            if not isolated:
                os.close(stdin)
        require(not reasons and (code in (0, 1) if isolated else code == 0), 'child failure/uncertain finalization')
        receipt['children'][-1]['outputs'] = {str(p): capture(p, 128 * MIB if p == R / 'source.blobs' else MIB // 2, False)[1]
                                             for p in (outpath, errpath)}
        if not isolated:
            blob_image = receipt['children'][-1]['outputs'][str(R / 'source.blobs')]
            require(blob_image['pin'] == produced_blob_pin, 'original Git-produced stream changed before capture')
            receipt['raw_transport']['response'] = {str(R / 'source.blobs'): blob_image}
            materialize(expected, folders, blob_image)
            before = source_check(expected)
            require(capture(R / 'source.blobs', 128 * MIB, False)[1] == blob_image, 'blob stream changed during materialization')
            receipt['raw_transport']['complete_stream_passes'] = 2
            receipt['source_before'] = before
            authority()
            inner_images = {str(p): images[str(p)] for p in REQUIRED}
            inner_images[str(GIT_INDEX)] = index_copy['copy_image']
            for relative in ('config',) + (('info/exclude',) if EXCLUDE_STATE == 'FILE' else ()):
                inner_images[str(GIT_METADATA / relative)] = images[str(GITDIR / relative)]
            output('OUTER-INTENT.json', encoded({'format': 'passvault-linux-detekt01-outer-v1', 'run_id': RUN, 'commit': COMMIT, 'tree': TREE,
                'source_representation': 'RAW_GIT_BLOBS', 'source_members': MEMBERS, 'parent_namespaces': parent_ns,
                'directories': {str(p): v[1] for p, v in directories.items() if p == R or R in p.parents or p == E or E in p.parents},
                'images': inner_images, 'outer_images': images,
                'git_inventory_binding': request['git_inventory_binding'], 'index_copy': index_copy,
                'tool_aliases': request['tool_aliases'],
                'packet': {'request': request_image, 'approval': approval_image}, 'device_model': DEVICE, 'raw_source': before}))
            command = [UNSHARE, '--mount', '--pid', '--fork', '--kill-child=SIGKILL', '--propagation=private', '--mount-proc=/proc',
                       '--', INNER_PYTHON, '-I', '-B', '-S', str(INNER), parent_ns['pid'], parent_ns['mnt']]
    raw, result_image = capture(E / 'INNER-RESULT.json', MIB)
    result = decode(raw)
    receipt.update(inner_exit=code, inner_result=result_image)
    require(code in (0, 1) and result['format'] == 'passvault-linux-detekt01-inner-v1' and result['run_id'] == RUN and
            result['commit'] == COMMIT and result['tree'] == TREE and result['parent_namespaces'] == parent_ns and all(result[k] is True for k in
            ('source_before', 'source_after', 'all_required_stops_ok', 'namespace_empty_before_exit',
             'cleanup_safe', 'git_bindings_established', 'git_bindings_stable', 'sealed_index_unchanged')),
            'inner safety/source/Git proofs incomplete')
    raw, preflight = capture(E / 'INNER-PREFLIGHT.json', 65536)
    value = decode(raw)
    require(value['parent'] == parent_ns and value['self'] == receipt['children'][-1].get('owned_namespaces')
            and value['nonpropagating_mounts'] is True, 'original live namespace/preflight binding absent')
    receipt['inner_preflight'] = preflight
    raw, git_preflight = capture(E / 'GIT-PREFLIGHT.json', 65536)
    binding = decode(raw)
    require(binding['publication_store'] == str(GITDIR)
            and binding['index_sha256'] == index_copy['source_sha256']
            and set(binding['mount_rows']) == {str(GIT_METADATA), str(GIT_INDEX)}
            and all(isinstance(rows, list) and len(rows) == 1 for rows in binding['mount_rows'].values()),
            'original Git preflight/index binding absent')
    receipt['git_preflight'] = git_preflight
    require(all(type(result[k]) is bool for k in
                ('static_reports_preserved', 'static_mapping_ok', 'validation_mapping_ok'))
            and result['gradle_static_selector'] == ':detekt' and type(result['declared_test_cases']) is int
            and result['declared_test_cases'] == 0, 'static-only result contract; never application cases')
    phases = result['phases']
    require(isinstance(phases, list) and 1 <= len(phases) <= 2
            and [p['phase'] for p in phases] in (['git-isolation'], ['git-isolation', 'detekt'])
            and all(type(p['stop_required']) is bool for p in phases), 'fixed static phase result contract')
    attempted = any(p['phase'] == 'detekt' and p['stop_required'] for p in phases)
    require(not attempted or result['static_reports_preserved'] is True,
            'attempted Detekt evidence not preserved')
    if code == 0:
        require(attempted and result['static_reports_preserved'] is True and result['static_mapping_ok'] is True
                and result['validation_mapping_ok'] is True,
                'success requires exact static task/source/report mapping; still independently unreconciled')
    # Mapping/validation FAIL is not a cleanup veto when original safety/evidence obligations are met.
    receipt['static_scope'] = {'attempted': attempted, 'selector': ':detekt', 'declared_test_cases': 0,
        **{k: result[k] for k in ('static_reports_preserved', 'static_mapping_ok', 'validation_mapping_ok')}}
    require(source_check(expected) == before, 'outer raw-source after mismatch')
    authority()
    watch()
    remove_runtime()
    receipt['status'] = 'COMPLETED_PENDING_INDEPENDENT_RECONCILIATION' if code == 0 else 'VALIDATION_FAILED_CLEANED'
    return code

if __name__ == '__main__':
    signal.signal(signal.SIGCHLD, signal.SIG_DFL)
    for signum in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
        signal.signal(signum, lambda number, _: note('signal:' + str(number)))
    status = 70
    try:
        signal.pthread_sigmask(signal.SIG_SETMASK, set())  # Handlers are installed before unblocking inherited signals.
        require(not signal.pthread_sigmask(signal.SIG_BLOCK, set()), 'outer signal mask not empty')
        receipt['signal_mask_before_children'] = []
        status = main()
    except BaseException as error:
        try:
            latch(type(error).__name__ + ':' + str(error))
        except BaseException as diagnostic:
            note('CANCEL_WRITE_FAILED:' + type(diagnostic).__name__)
    finally:
        if child is not None:
            try:
                supervise()  # Same original drain/monotone kill/wait state, never another child or retry.
            except BaseException as error:
                note('CHILD_SETTLEMENT_HOLD:' + type(error).__name__)
        receipt.update(reasons=reasons, resource_points=resources, unclassified_host_churn=churn, elapsed=round(time.monotonic() - START, 3),
            preterminal=True, qualification='Receipt precedes final descriptor closes/exit; actual exit authoritative. Nonexhaustive comm screen, generic-interpreter blind spot; no global-idle/no-escape/hard-deadline proof.')
        try:
            output('OUTER-RECEIPT.json', encoded(receipt)) if E in directories else print(encoded(receipt).decode(), end='')
        except BaseException:
            status = 70
        descriptors = [v[0] for v in reversed(tuple(directories.values()))]
        descriptors += ([] if lock_fd is None else [lock_fd]) + ([] if child is None or child['fd'] is None else [child['fd']])
        for fd in descriptors:
            try:
                os.close(fd)  # One attempt per original descriptor; no close retry after uncertainty.
            except OSError:
                status = 70
    sys.exit(status if not reasons else 70)
