#!/usr/bin/python3
"""NEW fixed AndroidCompile01 PID1 source, NOT execution admission.

Text-reuses Linux03 Files/resource/pidfd/stop/settlement and GUI02's distinction
between namespace setup and Gradle commands. No old helper import or replay.
Root owns fresh source/index transport, store freeze, original allocations/lock,
outer admission, pidfd/wait and allowlisted cleanup. No deletion or host signals.
One Android device-source compiler plus original wrapper stop; no tests or native runtime.
The finite graph permits only explicit inert compile-JAR/resource intermediates.
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

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
R = Path('/root/projects/PassVault/audit-runtime-linux-android-compile01')
E = B / 'runs/linux-android-compile01'
CHECKOUT = R / 'checkout'
SELF, INIT = W / 'scripts/audit/linux_android_compile_01.py', W / 'scripts/audit/android_compile_01.init.gradle'
SOURCE = B / 'reviews/checkpoint17/source-prepare01/SOURCE.json'
JAVA, MOUNT = Path('/usr/lib/jvm/java-17-openjdk-amd64'), Path('/usr/bin/mount')
SDK, SDK_VIEW = Path('/opt/android-sdk'), R / 'sdk-readonly'
SDK_METADATA = tuple(SDK / p for p in ('platforms/android-37.0/source.properties',
    'platforms/android-37.0/package.xml', 'build-tools/36.0.0/source.properties',
    'build-tools/36.0.0/package.xml', 'licenses/android-sdk-license'))
SDK_OPERATION = 'READ_ONLY_EXISTING_COMPILE_SDK_NO_COPY_INSTALL_LICENSE_CHANGES'
COMPILE_BINDING = {'readRoots': ['/root/projects/PassVault/audit-runtime-linux-android-compile01/checkout', '/root/projects/PassVault/audit-runtime-linux-android-compile01/gradle-home', '/root/projects/PassVault/audit-runtime-linux-android-compile01/tmp', '/root/projects/PassVault/audit-runtime-linux-android-compile01/konan', '/usr/lib/jvm/java-17-openjdk-amd64', '/root/projects/PassVault/audit-runtime-linux-android-compile01/sdk-readonly'], 'offline': False}  # Exact current C17/compile01 binding; no inherited instance authority.
READ_ROOT_ENVELOPE = (CHECKOUT, R / 'gradle-home', R / 'tmp', R / 'konan', JAVA, SDK_VIEW)
PUBLICATION_GIT = Path('/root/projects/PassVault/passvault-publication-20260910-01/.git')
GIT_DIR, GIT_INDEX = R / 'git-metadata', R / 'git-index'
GIT_CONFIG, GIT_EXCLUDE = GIT_DIR / 'config', GIT_DIR / 'info/exclude'
# Final source, SDK metadata, compile read roots and regular tool/alias/exclude identities
# require root's fresh review. Each None is a hard pre-launch HOLD, not a fallback.
COMMIT = '0563e31adc9a66aefc3e74b99b9a24d17bdcdd49'  # Exact current C17/compile01 binding; no inherited instance authority.
TREE = 'd1bd6ca5b18d08ff3ff15896d9a78af9016be799'  # Exact current C17/compile01 binding; no inherited instance authority.
MEMBERS = 3042  # Exact current C17/compile01 binding; no inherited instance authority.
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = 'INFO_ABSENT'  # Exact current C17/compile01 binding; no inherited instance authority.
FROZEN = {SOURCE: 'e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f', INIT: 'c6e7c4f36ce9909414be548e867cb11bf9186f0dc09c5246b44c82cd62a00ffe'}  # Exact current C17 source and external init.
INPUTS = (SELF, INIT, SOURCE, JAVA / 'bin/java', JAVA / 'release', MOUNT, GIT_INDEX) + SDK_METADATA
RUN_ID = 'linux-android-compile01'
REVIEW_ASSERTIONS = (
    'ordinary_full_stage0_index_matches_source', 'no_split_sparse_unmerged_index',
    'standalone_store_without_redirects', 'local_config_and_excludes_reviewed',
    'no_external_git_config_or_executable_mechanisms', 'publication_store_frozen_for_run',
)
PRIVATE = ('checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
# Outer separately owns empty Git/SDK mountpoints; their inodes are not the bound views.
ORIGINAL_DIRS = (R, E, E / 'logs', E / 'reports') + tuple(R / p for p in PRIVATE)
MAIN_TASK = ':core:crypto:compileAndroidMain'
DEVICE_TASK = ':core:crypto:compileAndroidDeviceTest'
ALLOWED_TASKS = {':core:crypto:' + name for name in (
    'compileAndroidMain', 'compileAndroidDeviceTest', 'checkKotlinGradlePluginConfigurationErrors',
    'kmpPartiallyResolvedDependenciesChecker', 'checkAndroidMainAarMetadata', 'checkAndroidDeviceTestAarMetadata',
    'androidPreBuild', 'preAndroidMainBuild', 'preAndroidDeviceTestBuild', 'generateAndroidMainEmptyResourceFiles',
    'generateAndroidDeviceTestResources', 'generateAndroidDeviceTestSources', 'generateAndroidDeviceTestRFile',
    'compileAndroidDeviceTestNavigationResources', 'processAndroidDeviceTestNavigationResources',
    'mapAndroidDeviceTestSourceSetPaths', 'mergeAndroidDeviceTestResources', 'parseAndroidDeviceTestLocalResources',
    'processAndroidDeviceTestResources', 'processAndroidMainManifest', 'mergeAndroidDeviceTestManifest',
    'processAndroidDeviceTestManifest', 'writeAndroidMainAarMetadata',
    'bundleAndroidMainClassesToCompileJar', 'packageAndroidDeviceTestResources')}
REQUIRED_SOURCES = {
    MAIN_TASK: {
        'core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt':
            '2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57',
        'core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/RawPasswordHash.kt':
            '3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb',
        'core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/RawPasswordHash.android.kt':
            'd2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c'},
    DEVICE_TASK: {
        'core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt':
            'd9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310'}}
MIB, GIB = 1024 ** 2, 1024 ** 3
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
COMMAND = [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT), DEVICE_TASK, '--info'] + FLAGS
if isinstance(COMPILE_BINDING, dict) and COMPILE_BINDING.get('offline') is True:
    COMMAND += ['--offline']  # No fallback; original --stop flags below remain unchanged.
STOP = [str(CHECKOUT / 'gradlew'), '--stop'] + FLAGS
ENV = {'PATH': str(JAVA / 'bin') + ':/usr/bin:/bin', 'JAVA_HOME': str(JAVA), 'LANG': 'C.UTF-8',
       'LC_ALL': 'C.UTF-8', 'TZ': 'UTC', 'HOME': str(R / 'home'), 'GRADLE_USER_HOME': str(R / 'gradle-home'),
       'KONAN_DATA_DIR': str(R / 'konan'), 'ANDROID_USER_HOME': str(R / 'android-user'),
       'ANDROID_HOME': str(SDK_VIEW), 'ANDROID_SDK_ROOT': str(SDK_VIEW),
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
    if not 1 <= len(allowed) <= 25:
        raise ValueError('fixed compile task identity bound')
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
        require(len(self.evidence) <= 96 and sum(v['pin']['bytes'] for v in self.evidence.values()) <= 32 * MIB,
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


class AndroidCompile01:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs = [], [], {}
        self.source = self.outer = self.initial = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.git_bound = self.git_stable = self.index_unchanged = False
        self.compilation_preserved = self.compilation_mapping_ok = False
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
        require(value['format'] == 'passvault-linux-android-compile01-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == MEMBERS
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent,
                'fixed fresh AndroidCompile01 intake; not independent execution admission')
        self.f.expected = dict(value['directories'])
        # Do not compare the future mounted alias against its outer empty-directory inode.
        self.f.expected.pop(str(GIT_DIR), None)
        self.f.expected.pop(str(SDK_VIEW), None)
        for path in ORIGINAL_DIRS:
            require(str(path) in self.f.expected and self.f.expected[str(path)]['uid'] == os.getuid()
                    and self.f.expected[str(path)]['mode'] == stat.S_IFDIR | 0o700, 'original private allocation')
            self.f.directory(path)
        self.inputs[E / 'OUTER-INTENT.json'] = image
        captured = {}
        for path in INPUTS + (GIT_IMAGE, RUBY_IMAGE):
            captured[path], actual = self.f.read(path, 4 * MIB if path == GIT_INDEX else
                                                 128 * 1024 if path in SDK_METADATA else 32 * MIB)
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
        require(not os.listdir(self.f.directory(E / 'reports')), 'fresh compiler-receipt evidence directory')
        sdk = value['sdk_existing_use']
        require(value['compile_binding'] == COMPILE_BINDING and sdk['root'] == str(SDK)
                and sdk['metadata_paths'] == [str(p) for p in SDK_METADATA]
                and sdk['operation'] == SDK_OPERATION, 'exact reviewed compile/read-only existing SDK scope')
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
                require(self.f.read(path, 128 * 1024 if path in SDK_METADATA or path.is_relative_to(SDK_VIEW)
                                    else 32 * MIB)[1] == image, 'admitted input drift')
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
                    # Read-only Git/SDK views are not generated runtime storage; never traverse them here.
                    children[:] = [n for n in children if n not in ('git-metadata', 'sdk-readonly')]
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
                    require(sample['files'] <= 200000 and sample['logical_bytes'] <= 4 * GIB, 'runtime inventory cap')
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
                    # Compile900/original-stop600 include their 20s TERM/KILL tail; bind primitive unchanged.
                    expired = now - started >= seconds - (20 if gradle else 0)
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
                == self.initial['self'], 'original private namespace required for Git/SDK binding')
        binding = self.outer['git_inventory_binding']
        rows = {str(GIT_DIR): [], str(GIT_INDEX): [], str(SDK_VIEW): []}
        for line in proc_read('/proc/self/mountinfo', 131072).splitlines():
            left, separator, right = line.partition(' - ')
            prefix, suffix = left.split(), right.split(' ')
            require(separator and len(prefix) >= 6 and len(suffix) == 3 and suffix[0] and suffix[2],
                    'Git bind mountinfo positional fields')
            require(not any(v.startswith(('shared:', 'master:', 'propagate_from:')) for v in prefix[6:]),
                    'propagating mount after Git setup')
            require(not any(prefix[4].startswith(str(p) + '/') for p in (GIT_DIR, SDK, SDK_VIEW)),
                    'unexpected nested Git view or SDK origin/view mount; no shadowed SDK input')
            if prefix[4] in rows:
                flags = {'ro', 'nosuid', 'nodev'} | (set() if prefix[4] == str(SDK_VIEW) else {'noexec'})
                require(flags <= set(prefix[5].split(',')), 'read-only Git/SDK bind flags')
                rows[prefix[4]].append(line)
        require(all(len(value) == 1 for value in rows.values()), 'exactly one metadata, index and SDK bind mount')
        sdk = self.outer['sdk_existing_use']
        self.f.expected[str(SDK_VIEW)] = sdk['directory']
        sdk_fd = self.f.directory(SDK_VIEW)
        require(directory_identity(pin(os.fstat(sdk_fd), True)) == sdk['directory']
                and os.fstatvfs(sdk_fd).f_flag & os.ST_RDONLY, 'read-only exact existing SDK view')
        for source in SDK_METADATA:
            alias = SDK_VIEW / source.relative_to(SDK)
            actual = self.f.read(alias, 128 * 1024)[1]
            require(actual == self.inputs[source], 'bound SDK metadata differs from reviewed existing input')
            self.inputs[alias] = actual
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
        state = self.state('git-sdk-isolation')
        try:
            require(self.namespace_ok and self.settle(), 'positive private PID/mount preflight and empty namespace')
            for target in (GIT_DIR, SDK_VIEW):
                original = self.outer['directories'][str(target)]
                fd = os.open(target.name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC,
                             dir_fd=self.f.directory(R))
                try:
                    require(pin(os.fstat(fd), True) == original and original['mode'] == stat.S_IFDIR | 0o700
                            and original['uid'] == os.getuid() and not os.listdir(fd), 'original empty bind mountpoint')
                finally:
                    os.close(fd)
            for source, origin in ((PUBLICATION_GIT, self.outer['git_inventory_binding']['metadata_directory']),
                                   (SDK, self.outer['sdk_existing_use']['directory'])):
                self.f.expected[str(source)] = origin
                require(directory_identity(pin(os.fstat(self.f.directory(source)), True)) == origin,
                        'exact original reviewed source directory; no retired store or SDK fallback')
            self.resources(True)
            for label, source, target, options in (
                    ('metadata-bind', PUBLICATION_GIT, GIT_DIR, 'bind'),
                    ('metadata-readonly', GIT_DIR, GIT_DIR, 'remount,bind,ro,nosuid,nodev,noexec'),
                    ('index-bind', GIT_INDEX, GIT_INDEX, 'bind'),
                    ('index-readonly', GIT_INDEX, GIT_INDEX, 'remount,bind,ro,nosuid,nodev,noexec'),
                    ('sdk-bind', SDK, SDK_VIEW, 'bind'),
                    ('sdk-readonly', SDK_VIEW, SDK_VIEW, 'remount,bind,ro,nosuid,nodev')):
                self.check_inputs()
                require({k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')} == self.initial['self'],
                        'namespace changed before fixed bind command')
                argv = [str(MOUNT), '--no-mtab', '--internal-only', '--options', options,
                        '--source', str(source), '--target', str(target)]
                require(self.command(label, argv, ENV, 20, state, gradle=False), 'Git bind setup failed; no retry')
            rows = self.binding_check()
            self.git_bound = True
            self.f.write(E / 'GIT-PREFLIGHT.json', canonical({'mount_rows': {str(p): rows[str(p)] for p in (GIT_DIR, GIT_INDEX)},
                'publication_store': str(PUBLICATION_GIT), 'git_environment': {k: v for k, v in ENV.items()
                    if k.startswith('GIT_')}, 'index_sha256': self.inputs[GIT_INDEX]['sha256'],
                'qualification': 'Mount/image binding only; full-index/config semantics require root independent review'}))
            self.f.write(E / 'SDK-PREFLIGHT.json', canonical({'root': str(SDK), 'view': str(SDK_VIEW),
                'mount_rows': rows[str(SDK_VIEW)], 'directory': self.outer['sdk_existing_use']['directory'],
                'metadata': {str(p): self.inputs[p] for p in SDK_METADATA}, 'operation': SDK_OPERATION,
                'qualification': 'Private RO alias, executable compile tools; not full provenance, resolver success, '
                    'new license acceptance, host-wide write isolation or immutable snapshot against outside writers'}))
            state['setup_ok'] = True
        finally:
            state['settled'] = self.settle()
            state['record_image'] = self.f.write(E / 'PHASE-git-sdk-isolation.json', canonical(state))
        require(state.get('setup_ok') and state['settled'] and not self.cancelled(), 'Git/SDK isolation incomplete')

    def preserve_compilation(self, state):
        # Raw bounded log is already durable. Retain marker bytes before interpretation;
        # malformed/partial receipts remain failure evidence, never invented completion.
        captures, rows, collection_errors, validation_errors, readbacks, sources = [], [], [], [], [], []
        headers = parse_task_headers([], ALLOWED_TASKS)
        log_image, marker_count, reachable, mapping = None, 0, False, False
        prefix = b'PASSVAULT_ANDROID_COMPILE01='

        def receipt_pairs(items):
            value = {}
            for key, item in items:
                require(key not in value, 'duplicate init JSON key')
                value[key] = item
            return value

        try:
            raw, log_image = self.f.read(E / 'logs/compile.log', 4 * MIB)
            require(log_image == self.f.evidence[E / 'logs/compile.log'], 'original compiler log changed')
            lines = raw.decode('utf-8', errors='replace').splitlines()
            headers = parse_task_headers(lines, ALLOWED_TASKS)
            for number, line in enumerate(raw.splitlines(), 1):
                if not line.startswith(prefix):
                    continue
                marker_count += 1
                if marker_count > 32 or len(line) > MIB:
                    note(validation_errors, 'marker count/size exceeded; original bounded log retained')
                    continue
                body = line[len(prefix):] + b'\n'
                destination = 'reports/init-%02d.json' % marker_count
                try:
                    saved = self.f.write(E / destination, body)
                    captures.append({'line': number, 'destination': destination, 'saved': saved})
                except Exception as error:
                    note(collection_errors, 'receipt retention: ' + str(error))
                    continue
                try:
                    row = json.loads(body.decode('utf-8'), object_pairs_hook=receipt_pairs,
                                     parse_constant=lambda _: require(False, 'nonfinite init JSON'))
                    require(isinstance(row, dict), 'init JSON object required')
                    rows.append(row)
                except Exception as error:
                    note(validation_errors, 'init JSON row: ' + str(error))
        except Exception as error:
            note(collection_errors, 'compiler log/receipt retention: ' + str(error))

        def one(kind, task=None):
            matches = [r for r in rows if r.get('kind') == kind and r.get('task') == task]
            require(len(matches) == 1, 'unique required receipt: ' + kind + ':' + str(task))
            return matches[0]

        def build_path(value):
            require(isinstance(value, str), 'declared output path text')
            path = Path(value)
            require(path.is_absolute() and str(path) == value and '..' not in path.parts
                    and path.is_relative_to(CHECKOUT / 'core/crypto/build'), 'fixed compiler output boundary')
            return path

        try:
            require(not headers['errors'] and len(rows) == marker_count == len(captures) == 10,
                    'exact successful compiler receipt stream; failures stay retained')
            allowed_kinds = {'graph', 'declared-outputs-before', 'generic-task-inputs', 'task-state', 'emitted-classes', 'finished'}
            require(all(r.get('kind') in allowed_kinds for r in rows), 'unknown init receipt kind')
            graph, terminal = one('graph'), one('finished')
            require(rows[0] is graph and rows[-1] is terminal and graph['selector'] == [DEVICE_TASK], 'receipt order/selector')
            tasks = graph['tasks']
            require(isinstance(tasks, list) and 2 <= len(tasks) <= 25, 'finite observed graph')
            by_task = {r['path']: r for r in tasks}
            require(len(by_task) == len(tasks) and set(by_task) <= ALLOWED_TASKS
                    and {MAIN_TASK, DEVICE_TASK} <= set(by_task), 'unique finite main/device graph membership')
            for row in tasks:
                require(isinstance(row['type'], str) and 0 < len(row['type']) <= 512
                        and type(row['enabled']) is bool, 'actual task type/enabled receipt')
                for key in ('dependencies', 'finalizers', 'mustRunAfter', 'shouldRunAfter'):
                    require(isinstance(row[key], list) and len(row[key]) <= 64
                            and all(isinstance(p, str) and len(p) <= 256 for p in row[key]), 'bounded actual task edges')
                require(set(row['dependencies']) <= set(by_task) and set(row['finalizers']) <= set(by_task),
                        'required/finalizer edge missing from actual graph')
            pending, seen = [DEVICE_TASK], set()
            while pending:
                task = pending.pop()
                if task not in seen:
                    seen.add(task)
                    pending.extend(by_task[task]['dependencies'])
            reachable = MAIN_TASK in seen
            require(reachable, 'MAIN not proven a transitive compiler dependency')
            observed = {e['task'] for e in headers['events']}
            require(observed == set(by_task), 'actual header/graph membership mismatch')
            require(terminal['evidenceComplete'] is True and terminal['failureType'] is None
                    and terminal['completed'] == [MAIN_TASK, DEVICE_TASK]
                    and type(terminal['cases']) is int and terminal['cases'] == 0
                    and terminal['classpathAttribution'] == 'NOT_ESTABLISHED'
                    and terminal['runtimeEvidence'] is False, 'terminal compile-only completion')
            class_bytes = 0
            for task, basename in ((MAIN_TASK, 'LibsodiumCryptoEngine'), (DEVICE_TASK, 'Android32KdfInstrumentation')):
                require(by_task[task]['enabled'] is True and headers['outcomes'][task] == 'BARE', 'compiler must actually run')
                before = one('declared-outputs-before', task)['outputs']
                require(isinstance(before, list) and 1 <= len(before) <= 16
                        and all(r['files'] == [] and type(r['present']) is bool for r in before), 'fresh declared output receipts')
                roots = [build_path(r['root']) for r in before]
                require(len(set(roots)) == len(roots), 'unique declared output roots')
                inputs = one('generic-task-inputs', task)
                require(inputs['api'] == 'Task.inputs.files; NOT compiler classpath or visitation'
                        and isinstance(inputs['inputs'], list) and len(inputs['inputs']) <= 4096, 'generic inputs, not libraries')
                entries = [r for item in inputs['inputs'] for r in item['files']]
                require(len(entries) <= 16384, 'generic declared input-file count')
                for relative, expected in REQUIRED_SOURCES[task].items():
                    path = CHECKOUT / relative
                    require(any(r['path'] == str(path) and r['sha256'] == expected for r in entries), 'required source not an actual declared input')
                    _, image = self.f.read(path, MIB)
                    require(image['sha256'] == expected, 'required source changed after compiler')
                    sources.append({'task': task, 'path': relative, 'image': image})
                status = one('task-state', task)
                require(all(type(status[k]) is bool for k in ('executed', 'skipped', 'upToDate', 'noSource', 'didWork'))
                        and status['executed'] and status['didWork'] and not status['skipped']
                        and not status['upToDate'] and not status['noSource'] and status['skipMessage'] is None
                        and status['failureType'] is None, 'no skipped/cache/no-source or failed compiler credit')
                emitted = one('emitted-classes', task)
                require([str(p) for p in roots] == emitted['declaredRoots']
                        and isinstance(emitted['classes'], list) and 1 <= len(emitted['classes']) <= 128
                        and type(emitted['generatedFileCount']) is int
                        and len(emitted['classes']) <= emitted['generatedFileCount'] <= 16384, 'bounded declared class output')
                base_count, class_paths = 0, set()
                for row in emitted['classes']:
                    path = build_path(row['path'])
                    require(path not in class_paths and any(path == p or path.is_relative_to(p) for p in roots)
                            and path.parent.parts[-4:] == ('com', 'passvault', 'core', 'crypto')
                            and (path.name == basename + '.class' or (path.name.startswith(basename + '$')
                                 and path.name.endswith('.class'))), 'unique same-package declared base/companion')
                    class_paths.add(path)
                    base_count += path.name == basename + '.class'
                    try:
                        remaining = 32 * MIB - class_bytes
                        require(remaining > 0, 'aggregate class readback budget exhausted')
                        data, image = self.f.read(path, min(MIB, remaining))
                        class_bytes += len(data)
                        require(class_bytes <= 32 * MIB and image['sha256'] == row['sha256']
                                and image['pin']['bytes'] == row['bytes'] and data[:8].hex() == row['classHeader']
                                == 'cafebabe0000003d', 'fresh emitted class readback/hash/JVM17 header')
                        readbacks.append({'task': task, 'path': str(path.relative_to(CHECKOUT)), 'image': image,
                                          'class_header': data[:8].hex(), 'binary_retained': False})
                    except Exception as error:
                        note(collection_errors, 'class readback: ' + str(error))
                        raise  # No continued reads after uncertainty; Files.read has one bounded +1 sentinel.
                require(base_count == 1, 'unique emitted base class required')
            mapping = state['build_ok'] and not collection_errors and not validation_errors
        except Exception as error:
            note(validation_errors, 'compile evidence mapping: ' + str(error))
        preserved = not collection_errors
        state['compilation'] = {'log_image': log_image, 'init_receipts': captures, 'marker_count': marker_count,
            'observed_graph': next((r.get('tasks') for r in rows if r.get('kind') == 'graph'), None),
            'task_events_not_test_cases': headers['events'], 'task_outcomes_not_test_cases': headers['outcomes'],
            'main_transitive_dependency_observed': reachable, 'required_source_readbacks': sources,
            'emitted_class_readbacks': readbacks, 'collection_errors': collection_errors,
            'header_errors': headers['errors'], 'validation_errors': validation_errors,
            'preserved': preserved, 'mapping_ok': bool(mapping), 'declared_test_cases': 0,
            'classpath_attribution': 'NOT_ESTABLISHED', 'runtime_kdf_packaging_evidence': False,
            'qualification': 'Original --info actions/input/new-class evidence requires independent reconciliation; '
                'generic declared inputs are not compiler classpaths or visitation. No XML or binary retention.'}
        self.f.write(E / 'COMPILE-EVIDENCE.json', canonical(state['compilation']))
        self.compilation_preserved, self.compilation_mapping_ok = preserved, bool(mapping)
        if not preserved:
            self.note('Compiler log/receipt/class readback retention uncertain; no automatic cleanup')
        if not mapping:
            self.note('Compiler evidence incomplete; no compilation credit from task count or mock assertions')

    def compile(self):
        state = self.state('android-compile')
        try:
            self.check_inputs()
            self.binding_check()
            require(self.git_bound and self.settle() and not self.cancelled(), 'admitted Git/SDK-isolated compile-only phase')
            self.resources(True)
            state['build_ok'] = self.command('compile', COMMAND, ENV, 900, state)
        finally:
            if state['stop_required']:
                try:
                    self.check_inputs(stopping=True)
                    self.source_check('STOP', True)
                    state['stop_ok'] = self.command('compile-stop', STOP, ENV, 600, state, stopping=True)
                except Exception as error:
                    self.note('original wrapper stop incomplete/no retry: ' + str(error))
            try:
                state['settled'] = self.settle()
                if state['settled'] and state['stop_required']:
                    self.preserve_compilation(state)
            except Exception as error:
                self.note('compiler evidence/settlement uncertainty: ' + str(error))
            state['record_image'] = self.f.write(E / 'PHASE-android-compile.json', canonical(state))
        require(state['build_ok'] and state['stop_ok'] and state['settled'] and self.compilation_mapping_ok
                and not self.cancelled(), 'AndroidCompile01 failed; no automatic retry')


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
    require(isinstance(COMPILE_BINDING, dict) and set(COMPILE_BINDING) == {'readRoots', 'offline'}
            and type(COMPILE_BINDING['offline']) is bool and isinstance(COMPILE_BINDING['readRoots'], list)
            and len(set(COMPILE_BINDING['readRoots'])) == len(COMPILE_BINDING['readRoots'])
            and {str(CHECKOUT), str(SDK_VIEW)} <= set(COMPILE_BINDING['readRoots'])
            <= {str(p) for p in READ_ROOT_ENVELOPE}, 'UNBOUND exact init readRoots/offline policy')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = AndroidCompile01(files, dict(zip(('pid', 'mnt'), sys.argv[1:])))
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
        batch.compile()
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
        attempted = any(p['phase'] == 'android-compile' and p['stop_required'] for p in batch.phases)
        safe = (batch.namespace_ok and batch.before and batch.after and stops and settled and batch.evidence_ok
                and complete and batch.git_bound and batch.git_stable and batch.index_unchanged and not CANCEL
                and (not attempted or batch.compilation_preserved))
        passed = safe and attempted and batch.compilation_mapping_ok and all(
            p.get('setup_ok') if p['phase'] == 'git-sdk-isolation' else p['build_ok'] for p in batch.phases)
        result = {'format': 'passvault-linux-android-compile01-inner-v1', 'run_id': RUN_ID, 'commit': COMMIT, 'tree': TREE,
            'parent_namespaces': batch.parent, 'source_before': batch.before, 'source_after': batch.after,
            'all_required_stops_ok': stops, 'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'git_bindings_established': batch.git_bound, 'git_bindings_stable': batch.git_stable,
            'sealed_index_unchanged': batch.index_unchanged, 'compiler_evidence_preserved': batch.compilation_preserved,
            'compiler_mapping_ok': batch.compilation_mapping_ok, 'validation_mapping_ok': bool(passed),
            'gradle_compile_selector': DEVICE_TASK, 'compile_binding': COMPILE_BINDING,
            'sdk_readonly_view_established': batch.git_bound, 'sdk_readonly_view_stable': batch.git_stable, 'declared_test_cases': 0, 'errors': batch.errors,
            'phases': batch.phases, 'independent_semantic_acceptance': False,
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'init_sha256': FROZEN[INIT],
                'representation': 'RAW_GIT_BLOBS'},
            'resource_point_samples_not_ownership_or_cleanup_proof': batch.inventories,
            'incomplete_resource_samples': sum(not s['traversal_finished'] or bool(s['vanished_entries']
                or s['vanished_scan_directories']) for s in batch.inventories),
            'qualification': 'Compile task/input/fresh-class evidence only, no test/XML/KDF/runtime/packaging or closure credit. '
                'Generic inputs are not compiler classpaths. RO Git/SDK aliases are not immutable snapshots against '
                'outside mutation. Bind mounts remain until private namespace exit; '
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
