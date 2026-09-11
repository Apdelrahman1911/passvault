#!/usr/bin/python3
"""NEW fixed Detekt03 PID1 source, NOT execution admission.

Text-reuses Linux03 Files/resource/pidfd/stop/settlement and GUI02's distinction
between namespace setup and Gradle commands. No old helper import or replay.
Root owns fresh source/index transport, store freeze, original allocations/lock,
outer admission, pidfd/wait and allowlisted cleanup. No deletion or host signals.
One root :detekt plus original wrapper stop; no tests, GUI, native or packaging.
"""
import hashlib
import json
import os
from pathlib import Path
import re
import resource
import selectors
import signal
import stat
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
R = Path('/root/projects/PassVault/audit-runtime-linux-detekt03')
E = B / 'runs/linux-detekt03'
CHECKOUT = R / 'checkout'
SELF, INIT = W / 'scripts/audit/linux_detekt_03.py', W / 'scripts/audit/detekt_03.init.gradle'
SOURCE = B / 'reviews/checkpoint17/source-prepare01/SOURCE.json'
JAVA, MOUNT = Path('/usr/lib/jvm/java-17-openjdk-amd64'), Path('/usr/bin/mount')
PUBLICATION_GIT = Path('/root/projects/PassVault/passvault-publication-20260910-01/.git')
GIT_DIR, GIT_INDEX = R / 'git-metadata', R / 'git-index'
GIT_CONFIG, GIT_EXCLUDE = GIT_DIR / 'config', GIT_DIR / 'info/exclude'
# Final published snapshot and exact regular tool/alias/optional-exclude identities
# require root's fresh review. None is a hard pre-launch HOLD, not a fallback.
COMMIT = None  # UNBOUND: root must bind the exact published C17 commit.
TREE = None  # UNBOUND: root must bind the exact published C17 tree.
MEMBERS = None  # UNBOUND: exact C17 source/index membership; no inherited admission.
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = None  # UNBOUND: fresh03 store/index/config/exclude review and new packet required.
FROZEN = {SOURCE: None, INIT: None}  # UNBOUND: fresh03 source and init SHA256 bindings.
INPUTS = (SELF, INIT, SOURCE, JAVA / 'bin/java', JAVA / 'release', MOUNT, GIT_INDEX)
GUARD_SHA256 = '7bc2995f85a77cbe45621592a2584908b8802e412a4f90c15130eb65a7960945'
RUN_ID = 'linux-detekt03'
REVIEW_ASSERTIONS = (
    'ordinary_full_stage0_index_matches_source', 'no_split_sparse_unmerged_index',
    'standalone_store_without_redirects', 'local_config_and_excludes_reviewed',
    'no_external_git_config_or_executable_mechanisms', 'publication_store_frozen_for_run',
)
PRIVATE = ('checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
# Outer separately owns the empty metadata mountpoint; its inode is not the bind view's inode.
ORIGINAL_DIRS = (R, E, E / 'logs', E / 'reports') + tuple(R / p for p in PRIVATE)
MODULES = ('', 'app-android', 'app-desktop', 'shared', 'core', 'feature',
           'core/domain', 'core/database', 'core/crypto', 'core/security', 'core/designsystem',
           'core/navigation', 'core/otp', 'core/testing', 'feature/onboarding', 'feature/unlock',
           'feature/vault', 'feature/credential', 'feature/generator', 'feature/health',
           'feature/settings', 'feature/backup')
TASKS = {((':' + m.replace('/', ':') + ':detekt') if m else ':detekt'): m for m in MODULES}
MIB, GIB = 1024 ** 2, 1024 ** 3
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
COMMAND = [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT), ':detekt', '--continue'] + FLAGS
STOP = [str(CHECKOUT / 'gradlew'), '--stop'] + FLAGS
ENV = {'PATH': str(JAVA / 'bin') + ':/usr/bin:/bin', 'JAVA_HOME': str(JAVA), 'LANG': 'C.UTF-8',
       'LC_ALL': 'C.UTF-8', 'TZ': 'UTC', 'HOME': str(R / 'home'), 'GRADLE_USER_HOME': str(R / 'gradle-home'),
       'KONAN_DATA_DIR': str(R / 'konan'), 'ANDROID_USER_HOME': str(R / 'android-user'),
       'ANDROID_HOME': '/opt/android-sdk', 'ANDROID_SDK_ROOT': '/opt/android-sdk',
       'TMPDIR': str(R / 'tmp'), 'TMP': str(R / 'tmp'), 'TEMP': str(R / 'tmp'), 'SQLITE_TMPDIR': str(R / 'sqlite'),
       **{'XDG_' + k.upper() + '_HOME': str(R / ('xdg-' + k)) for k in ('cache', 'config', 'data', 'state')},
       'JAVA_TOOL_OPTIONS': '-Xmx512m -XX:-UsePerfData -Dfile.encoding=UTF-8 -Duser.home=' + str(R / 'home')
       + ' -Djava.io.tmpdir=' + str(R / 'tmp') + ' -Djna.tmpdir=' + str(R / 'jna')
       + ' -Dorg.sqlite.tmpdir=' + str(R / 'sqlite'),
       'GIT_DIR': str(GIT_DIR), 'GIT_WORK_TREE': str(CHECKOUT), 'GIT_INDEX_FILE': str(GIT_INDEX),
       'GIT_OPTIONAL_LOCKS': '0', 'GIT_CONFIG_NOSYSTEM': '1', 'GIT_CONFIG_SYSTEM': '/dev/null',
       'GIT_CONFIG_GLOBAL': '/dev/null', 'GIT_TERMINAL_PROMPT': '0', 'GIT_ASKPASS': '/bin/false',
       'GIT_SSH_COMMAND': '/bin/false', 'GIT_CONFIG_COUNT': '5',
       'GIT_CONFIG_KEY_0': 'core.fsmonitor', 'GIT_CONFIG_VALUE_0': 'false',
       'GIT_CONFIG_KEY_1': 'core.untrackedCache', 'GIT_CONFIG_VALUE_1': 'false',
       'GIT_CONFIG_KEY_2': 'core.hooksPath', 'GIT_CONFIG_VALUE_2': '/dev/null',
       'GIT_CONFIG_KEY_3': 'core.excludesFile', 'GIT_CONFIG_VALUE_3': '/dev/null',
       'GIT_CONFIG_KEY_4': 'credential.helper', 'GIT_CONFIG_VALUE_4': ''}
CANCEL = set()


def require(ok, why):
    if not ok:
        raise RuntimeError(why)


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=True).encode() + b'\n'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def pin(st, directory=False):
    result = {k: getattr(st, 'st_' + k) for k in ('dev', 'ino', 'uid', 'mode', 'nlink')}
    if not directory:
        result.update(bytes=st.st_size, mtime_ns=st.st_mtime_ns, ctime_ns=st.st_ctime_ns)
    return result


def directory_identity(value):
    return {k: value[k] for k in ('dev', 'ino', 'uid', 'mode')}


def note(errors, error):
    value = str(error)[:1024]
    if value not in errors:
        if len(errors) < 127:
            errors.append(value)
        elif len(errors) == 127:
            errors.append('Additional errors omitted at the 128-entry evidence bound')


def parse_task_headers(lines, allowed):
    """Pure bounded log normalization; BARE is evidence, never standalone PASS."""
    allowed = set(allowed)
    if not 1 <= len(allowed) <= 23:
        raise ValueError('fixed static task identity bound')
    events, errors, counts = [], [], {}
    outcomes = {task: 'UNSTARTED' for task in sorted(allowed)}
    header_count = 0

    def reject(reason):
        if reason not in errors and len(errors) < 64:
            errors.append(reason)

    for number, line in enumerate(lines, 1):
        if not line.startswith('> Task '):
            continue
        header_count += 1
        if header_count > 2 * len(allowed):
            reject('Gradle task header count bound')
            continue
        match = re.fullmatch(
            r'> Task (:[A-Za-z0-9:_-]+)(?: (NO-SOURCE|UP-TO-DATE|FROM-CACHE|SKIPPED|FAILED))?', line)
        if match is None:
            reject('Malformed Gradle task header at line ' + str(number))
            continue
        task, suffix = match.groups()
        if task not in allowed:
            reject('Unknown Gradle task identity at line ' + str(number))
            continue
        events.append({'line': number, 'task': task, 'suffix': suffix})
        previous_count = counts.get(task, 0)
        counts[task] = previous_count + 1
        if previous_count == 0 or (previous_count == 1 and outcomes[task] == 'BARE' and suffix is not None):
            outcomes[task] = 'BARE' if suffix is None else suffix
        else:
            reject('Illegal Gradle task header transition at line ' + str(number))
        if suffix == 'FAILED':
            outcomes[task] = 'FAILED'  # A later header can never launder a recorded failure.
    return {'events': events, 'outcomes': outcomes, 'errors': errors,
            'header_count': header_count, 'unique_tasks': len(counts)}


def proc_read(path, cap):
    with open(path, 'rb') as stream:
        data = stream.read(cap + 1)
    require(len(data) <= cap, 'proc metadata bound')
    return data.decode('ascii')


class Files:
    def __init__(self):
        self.dirs, self.expected, self.evidence = {}, {}, {}
        self.pending = set()
        self.write_close_failed = False

    def directory(self, path):
        path = Path(path)
        require(path.is_absolute() and '..' not in path.parts, 'absolute canonical directory')
        parent = None if path == Path('/') else self.directory(path.parent)
        if path not in self.dirs:
            fd = os.open(path.name if parent is not None else '/', os.O_RDONLY | os.O_DIRECTORY
                         | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
            self.dirs[path] = (fd, directory_identity(pin(os.fstat(fd), True)))
        fd, original = self.dirs[path]
        require(directory_identity(pin(os.fstat(fd), True)) == original, 'original directory descriptor drift')
        if parent is not None:
            require(directory_identity(pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False), True))
                    == original, 'original directory path drift')
        if str(path) in self.expected:
            require(original == directory_identity(self.expected[str(path)]), 'outer original directory mismatch')
        return fd

    def read(self, path, cap):
        path = Path(path)
        parent = self.directory(path.parent)
        original = pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
        require(stat.S_ISREG(original['mode']) and original['uid'] == os.getuid() and original['nlink'] == 1
                and not original['mode'] & 0o022 and original['bytes'] <= cap, 'owned single-link bounded file')
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        with os.fdopen(fd, 'rb') as stream:
            require(pin(os.fstat(stream.fileno())) == original, 'file open drift')
            data = stream.read(cap + 1)
            require(len(data) == original['bytes'] and pin(os.fstat(stream.fileno())) == original
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'stable bounded file read')
        return data, {'sha256': sha(data), 'pin': original}

    def new(self, path):
        parent = self.directory(path.parent)
        fd = os.open(path.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                     0o600, dir_fd=parent)
        self.pending.add(path)
        try:
            os.fsync(parent)
        except Exception:
            os.close(fd)
            raise
        return fd

    def finish(self, path, fd, data_hash, size):
        os.fsync(fd)
        value = pin(os.fstat(fd))
        parent = self.directory(path.parent)
        os.fsync(parent)
        require(value == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
                and value['bytes'] == size and value['nlink'] == 1 and value['uid'] == os.getuid()
                and value['mode'] == stat.S_IFREG | 0o600, 'completed original evidence image')
        self.evidence[path] = {'sha256': data_hash, 'pin': value}
        self.pending.remove(path)
        return self.evidence[path]

    def write(self, path, data):
        fd = self.new(path)
        try:
            view = memoryview(data)
            while view:
                count = os.write(fd, view)
                require(count > 0, 'evidence short write')
                view = view[count:]
            return self.finish(path, fd, sha(data), len(data))
        finally:
            try:
                os.close(fd)
            except OSError:
                self.write_close_failed = True
                raise

    def verify(self):
        require(not self.pending and not self.write_close_failed, 'unfinished/uncertain original evidence files')
        for path in tuple(self.dirs):
            self.directory(path)
        require(len(self.evidence) <= 96 and sum(v['pin']['bytes'] for v in self.evidence.values()) <= 64 * MIB,
                'compact evidence aggregate bound')
        for path, expected in self.evidence.items():
            require(self.read(path, 4 * MIB)[1] == expected, 'preserved evidence drift')

    def close(self):
        failed = None
        for fd, _ in reversed(tuple(self.dirs.values())):
            try:
                os.close(fd)
            except OSError as error:
                failed = error
        if failed is not None:
            raise failed


def namespace_preflight(parent):
    require(os.getpid() == 1 and os.getppid() == 0, 'fresh PID1/PPID0 required')
    current = {k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')}
    require(all(re.fullmatch(k + r':\[[0-9]+\]', parent[k]) and current[k] != parent[k] for k in current),
            'distinct original parent namespaces')
    status = dict(line.split(':', 1) for line in proc_read('/proc/self/status', 32768).splitlines())
    require(all(status[k].strip() == v for k, v in (('Pid', '1'), ('Tgid', '1'), ('PPid', '0'), ('NSpid', '1'))),
            'fresh proc self PID mapping')
    lines = proc_read('/proc/self/mountinfo', 131072).splitlines()
    require(0 < len(lines) <= 2048, 'mount metadata count')
    dev = os.stat('/proc').st_dev
    visible, device = 0, '%s:%s' % (os.major(dev), os.minor(dev))
    for line in lines:
        left, separator, right = line.partition(' - ')
        prefix, suffix = left.split(), right.split(' ')
        # Preserve an empty SOURCE slot only. Namespace01 raw failed row is absent;
        # this corrects the independently witnessed valid-format counterexample.
        require(separator and len(prefix) >= 6 and len(suffix) == 3 and suffix[0] and suffix[2],
                'mountinfo positional fields; empty superoptions forbidden')
        require(not any(v.startswith(('shared:', 'master:', 'propagate_from:')) for v in prefix[6:]),
                'propagating mount forbidden')
        if prefix[4] == '/proc' and prefix[2] == device:
            require(suffix[0] == 'proc' and {'nosuid', 'nodev', 'noexec'} <= set(prefix[5].split(',')),
                    'visible proc type/security flags')
            visible += 1
    require(visible, 'visible private proc absent')
    return {'parent': parent, 'self': current, 'nonpropagating_mounts': True, 'mount_rows': len(lines)}


class Detekt03:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs = [], [], {}
        self.source = self.outer = self.initial = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.git_bound = self.git_stable = self.index_unchanged = False
        self.reports_preserved = self.mapping_ok = False
        self.last_resource = self.last_inventory = 0.0
        self.settlement_remaining, self.inventories = 120.0, []

    def note(self, error):
        note(self.errors, error)

    def cancelled(self):
        try:
            os.stat('CANCEL', dir_fd=self.f.directory(E), follow_symlinks=False)
            CANCEL.add('outer-file')
        except FileNotFoundError:
            pass
        return bool(CANCEL)

    def intake(self):
        data, image = self.f.read(E / 'OUTER-INTENT.json', 4 * MIB)
        value = self.outer = json.loads(data)
        require(value['format'] == 'passvault-linux-detekt03-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == MEMBERS
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent,
                'fixed fresh Detekt03 intake; not independent execution admission')
        self.f.expected = dict(value['directories'])
        # Do not compare the future mounted alias against its outer empty-directory inode.
        self.f.expected.pop(str(GIT_DIR), None)
        for path in ORIGINAL_DIRS:
            require(str(path) in self.f.expected and self.f.expected[str(path)]['uid'] == os.getuid()
                    and self.f.expected[str(path)]['mode'] == stat.S_IFDIR | 0o700, 'original private allocation')
            self.f.directory(path)
        self.inputs[E / 'OUTER-INTENT.json'] = image
        captured = {}
        for path in INPUTS + (GIT_IMAGE, RUBY_IMAGE):
            captured[path], actual = self.f.read(path, 4 * MIB if path == GIT_INDEX else 32 * MIB)
            require(actual == value['images'][str(path)] and actual['sha256'] == FROZEN.get(path, actual['sha256']),
                    'exact immutable input/tool image')
            self.inputs[path] = actual
        require(b'JAVA_VERSION="17.' in captured[JAVA / 'release']
                and b'OS_ARCH="x86_64"' in captured[JAVA / 'release'], 'JDK17 x86_64')
        self.source = json.loads(captured[SOURCE])
        require(self.source['format'] == 'passvault-linux-checkout-source-v1'
                and self.source['commit'] == COMMIT and self.source['tree'] == TREE
                and len(self.source['files']) == MEMBERS
                and len({r['path'] for r in self.source['files']}) == MEMBERS
                and len(self.source['checkout_eol_qualifications']) == 2, 'full source/two EOL qualifications')
        binding = value['git_inventory_binding']
        require(binding['publication_store'] == str(PUBLICATION_GIT)
                and binding['source_sha256'] == FROZEN[SOURCE]
                and binding['index_sha256'] == self.inputs[GIT_INDEX]['sha256']
                and set(binding['reviewed_assertions']) == set(REVIEW_ASSERTIONS)
                and all(binding['reviewed_assertions'][k] is True for k in REVIEW_ASSERTIONS)
                and binding['excludes']['state'] == EXCLUDE_STATE, 'externally reviewed exact Git/index binding')
        receipt = value['index_copy']
        require(receipt['copy'] == str(GIT_INDEX) and receipt['copy_image'] == self.inputs[GIT_INDEX]
                and receipt['source_sha256'] == receipt['source_image']['sha256'] == binding['index_sha256']
                and self.inputs[GIT_INDEX]['pin']['mode'] == stat.S_IFREG | 0o400, 'sealed original index copy')
        require(not os.path.lexists(CHECKOUT / '.git'), 'raw snapshot only; never a borrowed/fake local repository')
        require(not os.listdir(self.f.directory(E / 'reports')), 'fresh static-report evidence directory')
        self.check_aliases()

    def check_aliases(self):
        aliases = {Path('/usr/bin/git'): GIT_IMAGE, Path('/usr/bin/ruby'): RUBY_IMAGE}
        require(set(self.outer['tool_aliases']) == {str(p) for p in aliases}, 'exact Git/Ruby alias contract')
        for path, target in aliases.items():
            expected = self.outer['tool_aliases'][str(path)]
            before = pin(os.stat(path.name, dir_fd=self.f.directory(path.parent), follow_symlinks=False))
            require(before == expected['lstat'] and expected['resolved_image'] == str(target), 'tool alias pin drift')
            if stat.S_ISLNK(before['mode']):
                link = os.readlink(path.name, dir_fd=self.f.directory(path.parent))
                destination = Path(link) if Path(link).is_absolute() else path.parent / link
                require(link == expected['readlink'] and '..' not in destination.parts and destination == target,
                        'only the exact reviewed direct tool alias; no chained/guessed fallback')
            else:
                require(stat.S_ISREG(before['mode']) and expected['readlink'] is None and path == target,
                        'reviewed regular tool alias')
            require(before == pin(os.stat(path.name, dir_fd=self.f.directory(path.parent), follow_symlinks=False)),
                    'tool alias changed while read')

    def check_inputs(self, stopping=False):
        for path, image in self.inputs.items():
            if not stopping or path in (JAVA / 'bin/java', JAVA / 'release'):
                require(self.f.read(path, 32 * MIB)[1] == image, 'admitted input drift')
        if not stopping:
            self.check_aliases()

    def source_check(self, label, wrapper_only=False):
        started, count, size = time.monotonic(), 0, 0
        for row in self.source['files']:
            if wrapper_only and row['path'] not in ('gradlew', 'gradle/wrapper/gradle-wrapper.jar',
                                                   'gradle/wrapper/gradle-wrapper.properties'):
                continue
            require(time.monotonic() - started <= 180 and (wrapper_only or not self.cancelled()),
                    'source scan deadline/cancellation')
            relative = Path(row['path'])
            require(not relative.is_absolute() and '..' not in relative.parts, 'source relative path')
            data, image = self.f.read(CHECKOUT / relative, 32 * MIB)
            require(hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == row['git_blob']
                    and row['git_mode'] in ('100644', '100755')
                    and bool(image['pin']['mode'] & 0o111) == (row['git_mode'] == '100755'), 'raw blob/mode drift')
            if row['path'] == 'scripts/verify-static-analysis-coverage.rb':
                require(sha(data) == GUARD_SHA256, 'unchanged Ruby inventory guard required')
            count, size = count + 1, size + len(data)
        require(count == (3 if wrapper_only else MEMBERS), 'source member count')
        if not wrapper_only:
            require(not os.path.lexists(CHECKOUT / '.git'), 'raw source only: Git directory appeared')
            self.f.write(E / ('SOURCE-' + label + '.json'), canonical({'commit': COMMIT, 'tree': TREE,
                'representation': 'RAW_GIT_BLOBS_NOT_CHECKOUT_EOL', 'members': count, 'bytes': size,
                'checkout_eol_qualifications': self.source['checkout_eol_qualifications']}))

    def resources(self, launch=False):
        now = time.monotonic()
        if not launch and now - self.last_resource < 5:
            return
        self.last_resource = now
        memory = dict(line.split(':', 1) for line in proc_read('/proc/meminfo', 65536).splitlines())
        available, total = (int(memory[k].split()[0]) * 1024 for k in ('MemAvailable', 'MemTotal'))
        disks = [os.fstatvfs(self.f.directory(p)) for p in (R, E)]
        free_disk = min(s.f_bavail * s.f_frsize for s in disks)
        require(free_disk >= (12 if launch else 8) * GIB and available / total >= (0.25 if launch else 0.20),
                'disk/RAM floor: launch=%s disk_bytes=%s MemAvailable_bytes=%s MemTotal_bytes=%s'
                % (launch, free_disk, available, total))
        if launch or now - self.last_inventory >= 30:
            self.last_inventory = now
            require(len(self.inventories) < 256, 'resource point-sample count bound')
            sample = {'directories': 0, 'examined_entries': 0, 'files': 0, 'logical_bytes': 0,
                      'vanished_entries': 0, 'vanished_scan_directories': 0, 'traversal_finished': False}
            self.inventories.append(sample)

            def vanished(path, directory=False):
                path = Path(path)
                require(path.is_relative_to(R) and path not in ORIGINAL_DIRS,
                        'root/critical original runtime path disappeared')
                require(time.monotonic() - now <= 30, 'runtime inventory deadline')
                sample['vanished_scan_directories' if directory else 'vanished_entries'] += 1

            def walk_error(error):
                if not isinstance(error, FileNotFoundError):
                    raise error
                vanished(error.filename, True)

            # ENOENT below noncritical fresh R paths is retained sample incompleteness,
            # NOT host provenance, absence of data, or permission to delete anything.
            for parent, children, names in os.walk(R, followlinks=False, onerror=walk_error):
                if Path(parent) == R:
                    # A read-only alias of the publication store is not generated runtime storage.
                    children[:] = [n for n in children if n != 'git-metadata']
                sample['directories'] += 1
                require(time.monotonic() - now <= 30 and sample['directories'] <= 30000,
                        'runtime inventory directory/time cap')
                for name in children + names:
                    sample['examined_entries'] += 1  # Including vanished entries: churn cannot evade this cap.
                    require(sample['examined_entries'] <= 230000 and time.monotonic() - now <= 30,
                            'runtime examined-entry/time cap')
                    path = Path(parent) / name
                    try:
                        value = path.lstat()
                    except FileNotFoundError:
                        vanished(path)
                        continue
                    require(value.st_uid == os.getuid() and (stat.S_ISREG(value.st_mode) or stat.S_ISDIR(value.st_mode)),
                            'runtime inventory ownership/type uncertainty')
                    if stat.S_ISREG(value.st_mode):
                        sample['files'] += 1
                        sample['logical_bytes'] += value.st_size
                    require(sample['files'] <= 200000 and sample['logical_bytes'] <= 6 * GIB, 'runtime inventory cap')
            sample['traversal_finished'] = True

    def settle(self):
        require(self.namespace_ok, 'no membership scan without positive private namespace preflight')
        started, empty = time.monotonic(), 0
        try:
            while time.monotonic() - started < self.settlement_remaining:
                try:
                    while os.waitpid(-1, os.WNOHANG)[0]:
                        pass
                except ChildProcessError:
                    pass
                members = {int(n) for n in os.listdir('/proc') if n.isdigit()}
                require(1 in members and len(members) <= 65536, 'namespace process view/count')
                empty = empty + 1 if members == {1} else 0
                if empty >= 2:
                    return True
                self.resources()
                self.cancelled()
                time.sleep(0.2)
            return False
        finally:
            self.settlement_remaining = max(0, self.settlement_remaining - (time.monotonic() - started))

    def command(self, label, argv, env, seconds, phase, stopping=False, gradle=True):
        record = {'label': label, 'argv': argv, 'started': False, 'exit': None, 'complete': False, 'errors': []}
        phase['commands'].append(record)
        fd = pidfd = child = None
        digest, count, abort, killed = hashlib.sha256(), 0, None, False
        path = E / 'logs' / (label + '.log')
        try:
            fd = self.f.new(path)
            if stopping:
                require(not phase['stop_attempted'], 'stop already attempted; no retry')
                phase['stop_attempted'] = True
            else:
                require(not self.cancelled(), 'cancel before build intent')
                if gradle:
                    phase['stop_required'] = True
            self.f.write(E / (label + '-INTENT.json'), canonical({'argv': argv, 'cwd': str(CHECKOUT),
                'environment_sha256': sha(canonical(env)), 'seconds': seconds, 'stop_required': phase['stop_required']}))
            require(stopping or not self.cancelled(), 'cancel before build launch; original stop retained')
            child = subprocess.Popen(argv, cwd=CHECKOUT, env=env, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                     stderr=subprocess.STDOUT, close_fds=True)
            record['started'], record['namespace_pid'] = True, child.pid
            pidfd = os.pidfd_open(child.pid, 0)  # Direct child remains unreaped; never signal a reused numeric PID.
            os.set_blocking(child.stdout.fileno(), False)
            started, eof, exited = time.monotonic(), False, None
            with selectors.DefaultSelector() as selector:
                selector.register(child.stdout, selectors.EVENT_READ)
                while True:
                    for key, _ in selector.select(0.2):
                        data = os.read(key.fileobj.fileno(), 65536)
                        if not data:
                            selector.unregister(key.fileobj)
                            eof = True
                        else:
                            kept = data[:max(0, 4 * MIB - count)]
                            require(os.write(fd, kept) == len(kept), 'log short write')
                            digest.update(kept)
                            count += len(kept)
                            if len(kept) != len(data):
                                note(record['errors'], 'log cap')
                    try:
                        self.resources()
                    except Exception as error:
                        note(record['errors'], error)
                    cancelled = self.cancelled()
                    now = time.monotonic()  # Resource/syscall time is not subtracted from cooperative deadlines.
                    # The NEW 600s stop envelope includes its 20s TERM/KILL tail.
                    expired = now - started >= seconds - (20 if stopping else 0)
                    expired = expired or (not stopping and now - self.started >= 6000)
                    if expired or record['errors'] or (cancelled and not stopping):
                        if abort is None:
                            abort = now
                            note(record['errors'], 'command timeout/cancel/resource/output failure')
                            self.signal_child(pidfd, signal.SIGTERM)
                        elif now - abort >= 10 and not killed:
                            self.signal_child(pidfd, signal.SIGKILL)
                            killed = True
                        elif now - abort >= 20:
                            break
                    record['exit'] = child.poll()
                    if record['exit'] is not None:
                        exited = now if exited is None else exited
                        if eof:
                            record['complete'] = True
                            break
                        if now - exited >= 10:
                            note(record['errors'], 'pipe remains held after direct exit')
                            break
        except Exception as error:
            note(record['errors'], type(error).__name__ + ': ' + str(error))
        finally:
            try:
                if pidfd is not None and not record['complete'] and not killed:
                    self.signal_child(pidfd, signal.SIGKILL)
                if child is not None:
                    record['exit'] = child.poll()
                    child.stdout.close()
            except Exception as error:
                note(record['errors'], 'direct-child finalization: ' + str(error))
            if fd is not None:
                try:
                    record['log'] = self.f.finish(path, fd, digest.hexdigest(), count)
                except Exception as error:
                    note(record['errors'], 'log preservation: ' + str(error))
            for descriptor in (pidfd, fd):
                if descriptor is not None:
                    try:
                        os.close(descriptor)
                    except OSError as error:
                        note(record['errors'], 'original descriptor close: ' + str(error))
        return record['complete'] and record['exit'] == 0 and not record['errors']

    @staticmethod
    def signal_child(pidfd, sig):
        try:
            signal.pidfd_send_signal(pidfd, sig)
        except ProcessLookupError:
            pass  # Original direct child is already terminal; no numeric-PID fallback or retry.


    def state(self, mode):
        state = {'phase': mode, 'commands': [], 'stop_required': False, 'stop_attempted': False,
                 'stop_ok': False, 'build_ok': False, 'settled': False}
        self.phases.append(state)
        return state

    def binding_check(self):
        require(self.namespace_ok and {k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')}
                == self.initial['self'], 'original private namespace required for Git binding')
        binding = self.outer['git_inventory_binding']
        rows = {str(GIT_DIR): [], str(GIT_INDEX): []}
        for line in proc_read('/proc/self/mountinfo', 131072).splitlines():
            left, separator, right = line.partition(' - ')
            prefix, suffix = left.split(), right.split(' ')
            require(separator and len(prefix) >= 6 and len(suffix) == 3 and suffix[0] and suffix[2],
                    'Git bind mountinfo positional fields')
            require(not any(v.startswith(('shared:', 'master:', 'propagate_from:')) for v in prefix[6:]),
                    'propagating mount after Git setup')
            require(not prefix[4].startswith(str(GIT_DIR) + '/'), 'unexpected nested metadata mount')
            if prefix[4] in rows:
                require({'ro', 'nosuid', 'nodev', 'noexec'} <= set(prefix[5].split(',')), 'read-only Git bind flags')
                rows[prefix[4]].append(line)
        require(all(len(value) == 1 for value in rows.values()), 'exactly one metadata and one index bind mount')
        self.f.expected[str(GIT_DIR)] = binding['metadata_directory']
        require(directory_identity(pin(os.fstat(self.f.directory(GIT_DIR)), True)) == binding['metadata_directory']
                and os.fstatvfs(self.f.directory(GIT_DIR)).f_flag & os.ST_RDONLY, 'read-only exact new metadata view')
        fd = os.open(GIT_INDEX.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=self.f.directory(R))
        try:
            require(pin(os.fstat(fd)) == self.inputs[GIT_INDEX]['pin']
                    and os.fstatvfs(fd).f_flag & os.ST_RDONLY, 'read-only exact sealed index view')
        finally:
            os.close(fd)
        require(self.f.read(GIT_INDEX, 4 * MIB)[1] == self.inputs[GIT_INDEX], 'sealed copied index drift')
        for relative in ('commondir', 'config.worktree', 'objects/info/alternates', 'objects/info/http-alternates'):
            require(not os.path.lexists(GIT_DIR / relative), 'unadmitted Git redirect/worktree input')
        paths = [GIT_CONFIG]
        exclusion = binding['excludes']
        if EXCLUDE_STATE == 'INFO_ABSENT':
            require(exclusion['info_directory'] is None and not os.path.lexists(GIT_DIR / 'info'),
                    'exact reviewed absence of metadata info directory')
        else:
            require(pin(os.fstat(self.f.directory(GIT_DIR / 'info')), True) == exclusion['info_directory'],
                    'original metadata info directory')
            if EXCLUDE_STATE == 'EXCLUDE_ABSENT':
                require(not os.path.lexists(GIT_EXCLUDE), 'exact reviewed exclude-file absence')
            else:
                paths.append(GIT_EXCLUDE)
        for path in paths:
            actual = self.f.read(path, 65536)[1]
            require(actual == self.outer['images'][str(path)], 'reviewed metadata configuration/exclude image')
            if path in self.inputs:
                require(actual == self.inputs[path], 'metadata input drift')
            self.inputs[path] = actual
        return rows

    def isolation(self):
        state = self.state('git-isolation')
        try:
            require(self.namespace_ok and self.settle(), 'positive private PID/mount preflight and empty namespace')
            original = self.outer['directories'][str(GIT_DIR)]
            fd = os.open(GIT_DIR.name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                         dir_fd=self.f.directory(R))
            try:
                require(pin(os.fstat(fd), True) == original and original['mode'] == stat.S_IFDIR | 0o700
                        and original['uid'] == os.getuid() and not os.listdir(fd), 'original empty metadata mountpoint')
            finally:
                os.close(fd)
            origin = self.outer['git_inventory_binding']['metadata_directory']
            self.f.expected[str(PUBLICATION_GIT)] = origin
            require(directory_identity(pin(os.fstat(self.f.directory(PUBLICATION_GIT)), True)) == origin,
                    'exact original newly verified publication store; never retired stores')
            self.resources(True)
            for label, source, target, options in (
                    ('metadata-bind', PUBLICATION_GIT, GIT_DIR, 'bind'),
                    ('metadata-readonly', GIT_DIR, GIT_DIR, 'remount,bind,ro,nosuid,nodev,noexec'),
                    ('index-bind', GIT_INDEX, GIT_INDEX, 'bind'),
                    ('index-readonly', GIT_INDEX, GIT_INDEX, 'remount,bind,ro,nosuid,nodev,noexec')):
                self.check_inputs()
                require({k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')} == self.initial['self'],
                        'namespace changed before fixed bind command')
                argv = [str(MOUNT), '--no-mtab', '--internal-only', '--options', options,
                        '--source', str(source), '--target', str(target)]
                require(self.command(label, argv, ENV, 20, state, gradle=False), 'Git bind setup failed; no retry')
            rows = self.binding_check()
            self.git_bound = True
            self.f.write(E / 'GIT-PREFLIGHT.json', canonical({'mount_rows': rows,
                'publication_store': str(PUBLICATION_GIT), 'git_environment': {k: v for k, v in ENV.items()
                    if k.startswith('GIT_')}, 'index_sha256': self.inputs[GIT_INDEX]['sha256'],
                'qualification': 'Mount/image binding only; full-index/config semantics require root independent review'}))
            state['setup_ok'] = True
        finally:
            state['settled'] = self.settle()
            state['record_image'] = self.f.write(E / 'PHASE-git-isolation.json', canonical(state))
        require(state.get('setup_ok') and state['settled'] and not self.cancelled(), 'Git isolation incomplete')

    def preserve_reports(self, state):
        # Capture fixed allowlisted bytes FIRST. Log/schema/task failures are not
        # permission to discard available reports or invent a successful analysis.
        formats = {'checkstyle': '.xml', 'html': '.html', 'sarif': '.sarif'}
        report_paths, candidates = {}, []
        for task, module in TASKS.items():
            report_paths[task] = {}
            for kind, suffix in formats.items():
                relative = Path(module) / 'build/reports/detekt' / ('detekt' + suffix)
                report_paths[task][kind] = str(relative)
                candidates.append((task, kind, relative, 4 * MIB))
        # Explicit extra diagnostic: 66 Detekt files + one <=1MiB Gradle HTML,
        # sharing the original combined 32MiB capture budget, not extending it.
        candidates.append((None, 'problems_html', Path('build/reports/problems/problems-report.html'), MIB))
        require(len(candidates) == 67, 'fixed 66 static reports plus one bounded diagnostic')
        captures, absent, collection_errors, validation_errors = [], [], [], []
        charged = 0
        for task, kind, relative, cap in candidates:
            try:
                # Only an initial entry lookup may establish absence. ENOENT
                # from directory validation or an observed file read is drift.
                parent, absent_entry = CHECKOUT, None
                for index, part in enumerate(relative.parts):
                    parent_fd = self.f.directory(parent)  # Outside the absence catch.
                    child = parent / part
                    try:
                        observed = pin(os.stat(part, dir_fd=parent_fd, follow_symlinks=False))
                    except FileNotFoundError:
                        require(child not in self.f.dirs, 'previously pinned report parent disappeared')
                        absent_entry = str(child.relative_to(CHECKOUT))
                        break
                    if index + 1 < len(relative.parts):
                        descriptor = self.f.directory(child)
                        require(directory_identity(pin(os.fstat(descriptor), True)) == directory_identity(observed),
                                'report parent changed after initial lookup')
                        parent = child
                if absent_entry is not None:
                    absent.append({'task': task, 'kind': kind, 'source': str(relative), 'absent_entry': absent_entry})
                    continue
                report, original = self.f.read(CHECKOUT / relative, cap)
                require(original['pin'] == observed, 'report changed after initial entry lookup')
            except Exception as error:
                note(collection_errors, 'capture ' + str(relative) + ': ' + str(error))
                continue
            if charged + len(report) > 32 * MIB:
                note(collection_errors, 'combined static/diagnostic capture byte bound; remaining paths uninspected')
                break
            charged += len(report)  # Failed/partial writes still consume this budget.
            name = ('gradle-problems.html' if task is None else
                    (TASKS[task].replace('/', '-') if TASKS[task] else 'root') + '-' + kind + formats[kind])
            destination = 'reports/' + name
            try:
                saved = self.f.write(E / destination, report)
                captures.append({'task': task, 'kind': kind, 'source': str(relative), 'destination': destination,
                                 'original': original, 'saved': saved, 'diagnostic_only': task is None})
            except Exception as error:
                note(collection_errors, 'preserve ' + str(relative) + ': ' + str(error))

        # Semantic failures below cannot bypass the preceding retention pass.
        valid_reports, findings, parsed_checkstyle = set(), 0, 0
        for capture in captures:
            if capture['diagnostic_only']:
                continue  # Opaque retained diagnostic; no analyzer/report-format credit.
            task, kind = capture['task'], capture['kind']
            try:
                report, actual = self.f.read(E / capture['destination'], 4 * MIB)
                require(actual == capture['saved'], 'retained report image drift')
            except Exception as error:
                note(collection_errors, 'retained report ' + capture['destination'] + ': ' + str(error))
                continue
            try:
                require(report, 'nonempty static report required')
                if kind == 'checkstyle':
                    require(b'<!DOCTYPE' not in report and b'<!ENTITY' not in report, 'declaring static XML rejected')
                    document = ET.fromstring(report)
                    require(document.tag == 'checkstyle', 'static Checkstyle XML, never Test XML')
                    findings += len(document.findall('./file/error'))
                    parsed_checkstyle += 1
                elif kind == 'sarif':
                    require(isinstance(json.loads(report), dict), 'SARIF JSON object required')
                valid_reports.add((task, kind))
            except Exception as error:
                note(validation_errors, task + ' ' + kind + ': ' + str(error))

        all_tasks = set(TASKS) | {':verifyStaticAnalysisCoverage'}
        headers = parse_task_headers([], all_tasks)
        lines, source_sets, valid_plans, plan_count = [], {}, set(), 0
        try:
            data, _ = self.f.read(E / 'logs/detekt.log', 4 * MIB)
            lines = data.decode('utf-8', errors='replace').splitlines()
            headers = parse_task_headers(lines, all_tasks)
        except Exception as error:
            note(collection_errors, 'preserved static log uncertainty: ' + str(error))
        try:
            for task, module in TASKS.items():
                excluded = {'build', '.gradle', '.kotlin', 'generated', 'resources'} | ({'.idea'} if not module else set())
                source_sets[task] = sorted(r['path'] for r in self.source['files']
                    if not (set(Path(r['path']).parts) & excluded)
                    and ((not module and r['path'].endswith('.gradle.kts'))
                         or (module and r['path'].startswith(module + '/src/')
                             and r['path'].endswith(('.kt', '.kts')))))
            prefix = 'PASSVAULT_DETEKT03_TASK='
            seen = set()
            for line in lines:
                if not line.startswith(prefix):
                    continue
                plan_count += 1
                if plan_count > len(TASKS):
                    note(validation_errors, 'static task inventory row bound')
                    continue
                try:
                    row = json.loads(line[len(prefix):])
                    require(isinstance(row, dict) and set(row) == {'task', 'sources', 'reports'},
                            'exact generic Detekt inventory fields')
                    task = row['task']
                    require(isinstance(task, str) and task in TASKS and task not in seen,
                            'known unique static task inventory')
                    seen.add(task)
                    require(row['sources'] == source_sets[task], 'exact raw-snapshot planned Detekt source set')
                    require(row['reports'] == report_paths[task], 'exact fixed generic Detekt report paths')
                    valid_plans.add(task)
                except Exception as error:
                    note(validation_errors, 'static task inventory row ' + str(plan_count) + ': ' + str(error))
        except Exception as error:
            note(validation_errors, 'static source/inventory mapping: ' + str(error))
        if plan_count != len(TASKS) or valid_plans != set(TASKS):
            note(validation_errors, 'full exact 22-task static inventory missing or invalid')
        if headers['outcomes'][':verifyStaticAnalysisCoverage'] != 'BARE':
            note(validation_errors, 'Ruby coverage guard lacks expected bare task evidence')
        for task, expected in source_sets.items():
            if headers['outcomes'][task] != ('BARE' if expected else 'NO-SOURCE'):
                note(validation_errors, task + ': outcome ' + headers['outcomes'][task] + '; not qualified success')
            if expected and any((task, kind) not in valid_reports for kind in formats):
                note(validation_errors, task + ': required nonempty valid report triplet incomplete')
        preserved = not collection_errors
        mapping = (preserved and not validation_errors and not headers['errors']
                   and set(source_sets) == set(TASKS) and headers['unique_tasks'] == len(all_tasks))
        state['reports'] = {'captures': captures, 'absent': absent,
            'bytes': sum(c['saved']['pin']['bytes'] for c in captures), 'budget_bytes_charged': charged,
            'capture_file_limit': 67, 'capture_bytes_limit': 32 * MIB, 'diagnostic_file_bytes_limit': MIB,
            'checkstyle_findings': findings, 'parsed_checkstyle_reports': parsed_checkstyle,
            'findings_scope': 'Subtotal in successfully parsed retained Checkstyle reports only; not full coverage',
            'planned_source_sets': source_sets, 'planned_inventory_rows': plan_count,
            'task_events_not_test_cases': headers['events'], 'task_outcomes_not_test_cases': headers['outcomes'],
            'task_header_count': headers['header_count'], 'observed_unique_tasks': headers['unique_tasks'],
            'collection_errors': collection_errors, 'header_errors': headers['errors'],
            'validation_errors': validation_errors, 'mapping_ok': bool(mapping), 'preserved': preserved}
        self.f.write(E / 'STATIC-REPORTS.json', canonical(state['reports']))
        self.reports_preserved, self.mapping_ok = preserved, bool(mapping)
        if not preserved:
            self.note('Static report/log retention uncertain; not safe cleanup evidence')
        if not mapping:
            self.note('Static task/input/report mapping incomplete; no success from task count or compilation')

    def detekt(self):
        state = self.state('detekt')
        try:
            self.check_inputs()
            self.binding_check()
            require(self.git_bound and self.settle() and not self.cancelled(), 'admitted isolated static-only phase')
            self.resources(True)
            state['build_ok'] = self.command('detekt', COMMAND, ENV, 3600, state)
        finally:
            if state['stop_required']:
                try:
                    self.check_inputs(stopping=True)
                    self.source_check('STOP', True)
                    state['stop_ok'] = self.command('detekt-stop', STOP, ENV, 600, state, stopping=True)
                except Exception as error:
                    self.note('original wrapper stop incomplete/no retry: ' + str(error))
            try:
                state['settled'] = self.settle()
                if state['settled'] and state['stop_required']:
                    self.preserve_reports(state)
            except Exception as error:
                self.note('static report/settlement uncertainty: ' + str(error))
            state['record_image'] = self.f.write(E / 'PHASE-detekt.json', canonical(state))
        require(state['build_ok'] and state['stop_ok'] and state['settled'] and self.mapping_ok
                and not self.cancelled(), 'Detekt03 failed; no automatic retry')


def main():
    require(sys.platform == 'linux' and sys.argv[0] == str(SELF) and len(sys.argv) == 3
            and sys.flags.isolated and sys.dont_write_bytecode and sys.flags.no_site
            and os.getuid() == os.geteuid() == 0, 'fixed isolated absolute root entry')
    require(all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{40}', value) for value in (COMMIT, TREE))
            and type(MEMBERS) is int and 1 <= MEMBERS <= 3196
            and all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{64}', value) for value in FROZEN.values())
            and all(isinstance(path, Path) and path.is_absolute() and '..' not in path.parts
                    for path in (GIT_IMAGE, RUBY_IMAGE))
            and EXCLUDE_STATE in ('INFO_ABSENT', 'EXCLUDE_ABSENT', 'FILE'),
            'PENDING final source/tool/exclude identities; no execution admission')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = Detekt03(files, dict(zip(('pid', 'mnt'), sys.argv[1:])))
    settled, code = False, 70
    try:
        batch.intake()
        batch.initial = namespace_preflight(batch.parent)
        batch.namespace_ok = True
        files.write(E / 'INNER-PREFLIGHT.json', canonical(batch.initial))
        require(batch.settle(), 'unexpected initial namespace member')
        batch.source_check('BEFORE')
        batch.before = True
        batch.isolation()
        batch.detekt()
    except Exception as error:
        batch.note(type(error).__name__ + ': ' + str(error))
    finally:
        try:
            settled = batch.settle() if batch.namespace_ok else False
            if settled and batch.before and not batch.cancelled():
                batch.source_check('AFTER')
                batch.after = True
            batch.check_inputs()
            if batch.git_bound:
                batch.binding_check()
                batch.git_stable = batch.index_unchanged = True
            files.verify()
            require(not batch.cancelled(), 'cancelled before final evidence acceptance')
            batch.evidence_ok = True
        except Exception as error:
            batch.note('final source/Git/evidence/namespace uncertainty: ' + str(error))
        stops = all(not p['stop_required'] or (p['stop_attempted'] and p['stop_ok']) for p in batch.phases)
        complete = all('record_image' in p and all(c['complete'] and 'log' in c and not c['errors']
                       for c in p['commands']) for p in batch.phases)
        attempted = any(p['phase'] == 'detekt' and p['stop_required'] for p in batch.phases)
        safe = (batch.namespace_ok and batch.before and batch.after and stops and settled and batch.evidence_ok
                and complete and batch.git_bound and batch.git_stable and batch.index_unchanged and not CANCEL
                and (not attempted or batch.reports_preserved))
        passed = safe and attempted and batch.mapping_ok and all(
            p.get('setup_ok') if p['phase'] == 'git-isolation' else p['build_ok'] for p in batch.phases)
        result = {'format': 'passvault-linux-detekt03-inner-v1', 'run_id': RUN_ID, 'commit': COMMIT, 'tree': TREE,
            'parent_namespaces': batch.parent, 'source_before': batch.before, 'source_after': batch.after,
            'all_required_stops_ok': stops, 'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'git_bindings_established': batch.git_bound, 'git_bindings_stable': batch.git_stable,
            'sealed_index_unchanged': batch.index_unchanged, 'static_reports_preserved': batch.reports_preserved,
            'static_mapping_ok': batch.mapping_ok, 'validation_mapping_ok': bool(passed),
            'gradle_static_selector': ':detekt', 'declared_test_cases': 0, 'errors': batch.errors,
            'phases': batch.phases, 'independent_semantic_acceptance': False,
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'init_sha256': FROZEN[INIT],
                'inventory_guard_sha256': GUARD_SHA256, 'representation': 'RAW_GIT_BLOBS'},
            'resource_point_samples_not_ownership_or_cleanup_proof': batch.inventories,
            'incomplete_resource_samples': sum(not s['traversal_finished'] or bool(s['vanished_entries']
                or s['vanished_scan_directories']) for s in batch.inventories),
            'qualification': 'Static Detekt task/input/report evidence only, no regression cases or closure credit. '
                'Ruby default inventory is retained, not OpenGrep execution. RO metadata is not an immutable '
                'store snapshot against outside mutation. Bind mounts remain until private namespace exit; '
                'outer must prove exit, no runtime mounts, original ownership and allowlisted cleanup.'}
        try:
            files.write(E / 'INNER-RESULT.json', canonical(result))
            files.verify()
            code = (0 if passed and not batch.errors else 1) if safe else 70
        except Exception:
            code = 70
        finally:
            try:
                files.close()
            except Exception:
                code = 70
    os._exit(code)


if __name__ == '__main__':
    main()
