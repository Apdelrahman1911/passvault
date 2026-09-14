#!/usr/bin/python3
"""Fresh Android framework/compiler/Debug-package action batch source; NOT execution admission.
Uses accepted original-process/SDK/readonly-source/stop/settlement/allowlisted-cleanup source as DATA.
No previous helper imported/executed/replayed. One finite412-node batch, no runtime test credit.
New default Debug key only in original private HOME; production signing/Release/build1017001 unchanged.
Exact source/instance/resource/custody/cleanup review and root admission required before any run.
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
R = Path('/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01')
E = B / 'runs/linux-android-framework-package-action01'
CHECKOUT = R / 'checkout'
SELF, INIT = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/INNER.py'), Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/INIT.gradle')
EXPECTED_GRAPH = INIT.parent / 'EXPECTED-GRAPH.json'

SOURCE = B / 'reviews/checkpoint22/source-prepare01/SOURCE.json'  # Accepted checkpoint22 SOURCE bytes/hash; fresh cohort physical binding/review required.
JAVA, MOUNT = Path('/usr/lib/jvm/java-17-openjdk-amd64'), Path('/usr/bin/mount')
SDK, SDK_VIEW = Path('/opt/android-sdk'), R / 'sdk-readonly'
SDK_METADATA = tuple(SDK / p for p in ('platforms/android-37.0/source.properties',
    'platforms/android-37.0/package.xml', 'build-tools/36.0.0/source.properties',
    'build-tools/36.0.0/package.xml', 'licenses/android-sdk-license'))
SDK_OPERATION = 'READ_ONLY_EXISTING_COMPILE_SDK_NO_COPY_INSTALL_LICENSE_CHANGES'
ACTION_BINDING = {'runtimeRoot': '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01', 'evidenceRoot': '/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/runs/linux-android-framework-package-action01', 'readRoots': ['/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/checkout', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/gradle-home', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/tmp', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/konan', '/usr/lib/jvm/java-17-openjdk-amd64', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/sdk-readonly'], 'offline': False, 'sourceManifestSha256': '5eed5ec803e336c3868d1fe4255f02535fda3672ed5455e7499d7b9ffc881aa6', 'subjectSha256': '580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d'}  # Accepted checkpoint22 SOURCE literals; fresh cohort physical binding remains root-owned.
READ_ROOT_ENVELOPE = (CHECKOUT, R / 'gradle-home', R / 'tmp', R / 'konan', JAVA, SDK_VIEW)
PUBLICATION_GIT = Path('/root/projects/PassVault/passvault-publication-20260912-02/.git')
GIT_DIR, GIT_INDEX = R / 'git-metadata', R / 'git-index'
GIT_CONFIG, GIT_EXCLUDE = GIT_DIR / 'config', GIT_DIR / 'info/exclude'
# Product P/current SOURCE literals are proposed below; actual adoption and current SDK/read-root/
# tool/alias/exclude facts still require root's final binding, consistency and independent admission.
COMMIT = 'd2d79ad52ce816a36f708bd35bd736a7d8068789'  # Accepted product P supplied by root; not a SOURCE capture observation.
TREE = '2e83afec1894238f59c1e9916cbf548ad54f5da2'  # Accepted product P supplied by root; not a SOURCE capture observation.
MEMBERS = 4548  # Accepted complete checkpoint22 SOURCE count; no recapture.
MEMBER_LIMIT = 4548  # Accepted exact source ceiling; no automatic widening.
SOURCE_BYTES = 2519894  # Accepted exact checkpoint22 SOURCE length; no recapture.
OID_BYTES = 186468  # Accepted4548*41 at BOTH writer/reader; full ordered OID vector.
SOURCE_CAPACITY = {'members': MEMBERS, 'member_limit': MEMBER_LIMIT, 'manifest_bytes': SOURCE_BYTES,
                   'oid_bytes': OID_BYTES, 'raw_stream_limit_bytes': 160 * 1024 * 1024}
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = 'INFO_ABSENT'  # Retained checkpoint22 candidate; root must admit unchanged current U.
FROZEN = {EXPECTED_GRAPH: '90d55008a8e88d83a235eccdb61c59ae3dd4a0b1c62e328dfb907337584223e3', SOURCE: '5eed5ec803e336c3868d1fe4255f02535fda3672ed5455e7499d7b9ffc881aa6', INIT: '27e899ff1b506540286697bad8aa1ee52d2b7b34c3b1b71fd7cc820f8b6a0651'}  # Accepted SOURCE/proposed NEW control hashes; fresh physical admission pending.
INPUTS = (SELF, INIT, SOURCE, EXPECTED_GRAPH, JAVA / 'bin/java', JAVA / 'release', MOUNT, GIT_INDEX) + SDK_METADATA
RUN_ID = 'linux-android-framework-package-action01'
REVIEW_ASSERTIONS = (
    'ordinary_full_stage0_index_matches_source', 'no_split_sparse_unmerged_index',
    'standalone_store_without_redirects', 'local_config_and_excludes_reviewed',
    'no_external_git_config_or_executable_mechanisms', 'publication_store_frozen_for_run',
)
PRIVATE = ('checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
# Outer separately owns empty Git/SDK mountpoints; their inodes are not the bound views.
ORIGINAL_DIRS = (R, E, E / 'logs', E / 'reports') + tuple(R / p for p in PRIVATE)
MIB, GIB = 1024 ** 2, 1024 ** 3
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
ACTION_COMMAND = [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT),
    ':app-android:auditCompileDebugFrameworkInstrumentation', ':app-android:verifyDebugComposeResources', '--info', '-Ppassvault.audit.mode=android-framework-package-actions',
    '-Pkotlin.native.toolchain.enabled=true', '-Pkotlin.native.distribution.downloadFromMaven=true',
    '-Pkonan.data.dir=' + str(R / 'konan')] + FLAGS
if ACTION_BINDING['offline']:
    ACTION_COMMAND += ['--offline']  # Fixed policy, never a resolution fallback.
STOP = [str(CHECKOUT / 'gradlew'), '--stop'] + FLAGS
ENV = {'PATH': str(JAVA / 'bin') + ':/usr/bin:/bin', 'JAVA_HOME': str(JAVA), 'LANG': 'C.UTF-8',
       'LC_ALL': 'C.UTF-8', 'TZ': 'UTC', 'HOME': str(R / 'home'), 'GRADLE_USER_HOME': str(R / 'gradle-home'),
       'KONAN_DATA_DIR': str(R / 'konan'), 'ANDROID_USER_HOME': str(R / 'home/.android'),
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


ACTION_TARGET = ':app-android:auditCompileDebugFrameworkInstrumentation'
ACTION_PACKAGE_TARGET = ':app-android:verifyDebugComposeResources'
ACTION_TARGETS = [ACTION_TARGET, ACTION_PACKAGE_TARGET]
ACTION_FILTER = None  # No Test filter: framework producer graph only.
ACTION_SUBJECT_SHA = '580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d'
ACTION_WRAPPERS = {'org.gradle.internal.exceptions.LocationAwareException',
                  'org.gradle.internal.event.ListenerNotificationException',
                  'org.gradle.execution.MultipleBuildFailures'}
ACTION_OUTPUT_SCOPE = 'EXACT_FINITE_GRAPH_PRIVATE_OUTPUTS_FRESH_COMPILER_DEBUG_PACKAGE_ONLY'


ACTION_PROPERTY_CONTRACT = 'EXACT_CLI_NATIVE_PLUS_FIXED_TOOLCHAIN_9_V1'
ACTION_PROPERTY_SOURCE = {
    'path': 'gradle.properties', 'git_mode': '100644',
    'git_blob': 'a0f3d9ab2e45cae4e85feb468ba6d18b2aa9d57b', 'git_size': 1173,
    'raw_size': 1173, 'raw_sha256': '323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235',
    'checkout_size': 1173, 'checkout_sha256': '323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235',
}


def validate_argument_vector(row):
    # Additional receipt condition only. It never replaces the graph/finished pair.
    expected = {'format': 'passvault-android-argument-vector-v3', 'runId': RUN_ID,
        'phase': 'AFTER_ARG12_BEFORE_ARG13', 'sourceManifestSha256': 'UNBOUND',
        'propertySourceSha256': ACTION_PROPERTY_SOURCE['raw_sha256'], 'propertyContract': ACTION_PROPERTY_CONTRACT,
        'qualification': 'NEW_EXACT_CONTRACT_NOT_REPRODUCED_CAUSE_OR_TASK_AUTHORITY',
        'projectMapKind': 'MAP', 'systemMapKind': 'MAP'}
    require(isinstance(row, dict) and set(row) == set(expected) | {
                'compatibilityShape', 'keys', 'types', 'outcomes', 'diagnosticError'}
            and all(row.get(k) == v for k, v in expected.items())
            and row['diagnosticError'] is False, 'exact new-contract bounded argument-vector schema')
    shape = row['compatibilityShape']
    require(shape == 'EXACT_CLI_NATIVE_PLUS_FIXED_TOOLCHAIN_9', 'exact fixed-toolchain property contract did not accept')
    keys = row['keys']
    require(isinstance(keys, dict) and set(keys) == {'state', 'count', 'expectedMask', 'sourceOnlyMask',
                'sourceStringMask', 'sourceValueMatchMask', 'toolchainOnlyMask', 'toolchainStringMask',
                'toolchainValueMatchMask', 'sizeAndExpectedMembershipAgree'}
            and keys['state'] == 'COUNT_AND_FIXED_KEYS_VALUES_ONLY'
            and type(keys['count']) is int and keys['count'] == 9
            and keys['expectedMask'] == '111111'
            and all(keys[k] == '0' * 16 for k in (
                'sourceOnlyMask', 'sourceStringMask', 'sourceValueMatchMask'))
            and all(keys[k] == '111' for k in (
                'toolchainOnlyMask', 'toolchainStringMask', 'toolchainValueMatchMask'))
            and keys['sizeAndExpectedMembershipAgree'] is True, 'complete exact fixed property shape masks/types')
    require(isinstance(row['types'], dict)
            and row['types'] == {k: 'STRING' for k in ('ARG14', 'ARG15', 'ARG16', 'ARG17', 'ARG18', 'ARG19', 'JAVA_HOME')},
            'fixed literal-key arguments and current JAVA_HOME must be Strings')
    outcomes = row['outcomes']
    require(isinstance(outcomes, dict) and set(outcomes) == {
                'LEGACY_ARG13', 'ARG14', 'ARG15', 'ARG16', 'ARG17', 'ARG18', 'ARG19', 'ARG20'}
            and outcomes['LEGACY_ARG13'] == 'REJECT'
            and all(outcomes[k] == 'ACCEPT' for k in ('ARG14', 'ARG15', 'ARG16', 'ARG17', 'ARG18', 'ARG19', 'ARG20')),
            'complete successful bounded ARG14-20 observations; legacy ARG13 is diagnostic only')
    return {'compatibility_shape': shape, 'legacy_arg13': outcomes['LEGACY_ARG13'],
            'property_source_sha256': ACTION_PROPERTY_SOURCE['raw_sha256'],
            'diagnostic_not_graph_or_task_authority': True}


def validate_framework_action_rows(rows, binding):
    require(rows and len(rows) <= 440 and rows[-1].get('kind') == 'finished', 'bounded terminal action stream')
    kinds = {}
    allowed = {'framework-artifact-binding', 'schema-equality', 'action-admission', 'task-state',
               'framework-compiler-input', 'framework-compiled-class', 'debug-package-evidence', 'finished'}
    for row in rows:
        require(row.get('format') == 'passvault-android-framework-package-action01-v1'
                and row.get('runId') == RUN_ID and row.get('sourceManifestSha256') == binding['sourceManifestSha256']
                and row.get('subjectSha256') == binding['subjectSha256'] and row.get('selector') == ACTION_TARGETS
                and row.get('graphOnly') is False and row.get('observedTestCases') == 0
                and row.get('kind') in allowed, 'source-bound action receipt contract')
        kinds.setdefault(row['kind'], []).append(row)
    for kind in allowed - {'task-state', 'schema-equality'}:
        require(len(kinds.get(kind, [])) == 1, 'one required action receipt: ' + kind)
    graph = kinds['action-admission'][0]
    require(graph['taskCount'] == 412 and len(graph['tasks']) == 412, 'exact action graph size')
    paths = {t['path'] for t in graph['tasks']}
    require(len(paths) == 412 and set(ACTION_TARGETS) <= paths, 'exact unique action graph paths')
    states = kinds.get('task-state', [])
    require(len(states) == 412 and {s['task'] for s in states} == paths
            and all(s.get('failureType') is None for s in states), 'every actual task outcome required')
    compiler = ':app-android:compileDebugAndroidTestKotlin'
    for task in (compiler, ':app-android:validateSigningDebug', ':app-android:packageDebug', ACTION_TARGETS[1]):
        s = next(r for r in states if r['task'] == task)
        require(s['executed'] is True and s['skipped'] is False and s['upToDate'] is False
                and s['noSource'] is False and s['skipMessage'] is None, 'fresh selected task required')
    source = kinds['framework-compiler-input'][0]
    require(source['task'] == compiler and source['fixture'] == str(CHECKOUT / (
                'app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt'))
            and source['image']['sha256'] == binding['subjectSha256'], 'actual compiler fixture affinity')
    emitted = kinds['framework-compiled-class'][0]
    require(emitted['task'] == compiler and emitted['artifact'] == 'PROJECT/CLASSES'
            and len(emitted['files']) == 1, 'one emitted expected class')
    member = emitted['files'][0]
    require(member['entry'] == 'com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.class'
            and member['image']['header'].startswith('cafebabe') and member['image']['bytes'] > 0
            and re.fullmatch(r'[0-9a-f]{64}', member['image']['sha256']), 'fresh class metadata')
    package = kinds['debug-package-evidence'][0]
    require(package['apk']['path'].startswith(str(CHECKOUT / 'app-android/build/outputs/apk/debug') + '/')
            and package['apk']['path'].endswith('.apk') and package['apk']['bytes'] > 0
            and re.fullmatch(r'[0-9a-f]{64}', package['apk']['sha256']), 'original Debug APK metadata')
    members = package['members']
    require(0 < len(members) <= 32768 and len({m['name'] for m in members}) == len(members), 'unique APK members')
    native = [m for m in members if not m['directory'] and m['name'].startswith('lib/')]
    abis = ('arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64')
    libs = ('libandroidx.graphics.path.so', 'libimage_processing_util_jni.so', 'libjnidispatch.so',
            'libsodium.so', 'libsqliteJni.so', 'libsurface_util_jni.so')
    require({m['name'] for m in native} == {f'lib/{abi}/{lib}' for abi in abis for lib in libs}
            and all(m['elfHeader'].startswith('7f454c46') and re.fullmatch(r'[0-9a-f]{64}', m['sha256']) for m in native),
            '24 expected native files; header metadata is not runtime loading')
    require(any(m['name'].startswith('assets/legal/') for m in members), 'legal members absent')
    final = kinds['finished'][0]
    require(final['accepted'] is True and final['beforeCount'] == final['afterCount'] == final['taskCount'] == 412
            and final['schemaCompleted'] is True and final['packageVerified'] is True and final['failureType'] is None,
            'complete successful action result required')
    require({r['at'] for r in kinds.get('schema-equality', [])} ==
            {'before-actions', 'before-schema-finalizer', 'after-schema-finalizer'}
            and all(r['equal'] is True for r in kinds['schema-equality']), 'unchanged copied schemas')
    return {'tasks': 412, 'xml_testcases': 0, 'compiler_class': member, 'debug_apk': package['apk'],
            'native_members': 24, 'runtime_or_hardware_credit': False,
            'qualification': 'Compiler/public-input/fresh-class and existing Debug verifier evidence only; independent actual review required.'}




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
            limit = 16 * MIB if path == E / 'logs/framework-actions.log' else 4 * MIB
            require(self.read(path, limit)[1] == expected, 'preserved evidence drift')

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


class AndroidFrameworkPackageAction01:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs = [], [], {}
        self.source = self.outer = self.initial = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.git_bound = self.git_stable = self.index_unchanged = False
        self.action_preserved = self.action_mapping_ok = False
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
        require(value['format'] == 'passvault-linux-android-framework-package-action01-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == MEMBERS
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent
                and value['source_capacity'] == SOURCE_CAPACITY,
                'fixed fresh AndroidFrameworkPackageAction01 intake; not independent execution admission')
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
            captured[path], actual = self.f.read(path, SOURCE_BYTES if path == SOURCE else
                                                 4 * MIB if path == GIT_INDEX else
                                                 128 * 1024 if path in SDK_METADATA else 32 * MIB)
            require(actual == value['images'][str(path)] and actual['sha256'] == FROZEN.get(path, actual['sha256']),
                    'exact immutable input/tool image')
            self.inputs[path] = actual
        require(self.inputs[SOURCE]['pin']['bytes'] == SOURCE_BYTES, 'exact current source manifest length')
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
        require(not os.listdir(self.f.directory(E / 'reports')), 'fresh graph-receipt evidence directory')
        sdk = value['sdk_existing_use']
        require(value['action_binding'] == ACTION_BINDING and sdk['root'] == str(SDK)
                and sdk['metadata_paths'] == [str(p) for p in SDK_METADATA]
                and sdk['operation'] == SDK_OPERATION, 'exact reviewed graph/read-only existing SDK scope')
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
        require(free_disk >= (8 if launch else 5) * GIB and available / total >= (0.25 if launch else 0.20),
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
                    require(sample['files'] <= 200000 and sample['logical_bytes'] <= 3 * GIB, 'runtime inventory cap')
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
        log_limit = 16 * MIB if label == 'framework-actions' else 4 * MIB
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
                            kept = data[:max(0, log_limit - count)]
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
                 'stop_ok': False, 'settled': False}
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



    def verify_framework_action_subject(self):
        # Passive source affinity, NOT compiler input visitation or a test outcome.
        relative = 'app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt'
        rows = [r for r in self.source['files'] if r['path'] == relative]
        require(len(rows) == 1, 'exact framework fixture graph subject membership in final source manifest')
        data, image = self.f.read(CHECKOUT / relative, MIB)
        require(len(data) == 29184 and sha(data) == ACTION_SUBJECT_SHA
                and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == rows[0]['git_blob'],
                'framework fixture graph subject SHA256/final raw Git-blob affinity')
        self.f.write(E / 'FRAMEWORK-SUBJECT.json', canonical({'relative_source': relative, 'image': image,
                     'sha256': ACTION_SUBJECT_SHA, 'source_manifest_sha256': ACTION_BINDING['sourceManifestSha256'],
                     'not_compiler_visitation_or_test_evidence': True}))



    def verify_framework_action_properties(self):
        # Exact complete checked-in file affinity, not a claim about Gradle merge behavior.
        relative = ACTION_PROPERTY_SOURCE['path']
        rows = [r for r in self.source['files'] if r['path'] == relative]
        # Keep the whole SOURCE row; its observed_checkout_pin is historical, not runtime authority.
        require(len(rows) == 1 and set(rows[0]) == (set(ACTION_PROPERTY_SOURCE) | {'observed_checkout_pin'})
                and {key: rows[0][key] for key in ACTION_PROPERTY_SOURCE} == ACTION_PROPERTY_SOURCE,
                'exact checked-property source schema/raw-affinity projection')
        data, image = self.f.read(CHECKOUT / relative, ACTION_PROPERTY_SOURCE['raw_size'])
        require(len(data) == ACTION_PROPERTY_SOURCE['raw_size'] and sha(data) == ACTION_PROPERTY_SOURCE['raw_sha256']
                and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
                    == ACTION_PROPERTY_SOURCE['git_blob'], 'checked-property raw SHA256/Git-blob affinity')
        self.f.write(E / 'PROPERTY-CONTRACT-SOURCE.json', canonical({'relative_source': relative, 'image': image,
            'source_record': rows[0], 'source_manifest_sha256': ACTION_BINDING['sourceManifestSha256'],
            'contract': ACTION_PROPERTY_CONTRACT,
            'qualification': 'Exact raw-affinity projection; observed_checkout_pin is historical whole-SOURCE-bound metadata, '
                'not current runtime identity. No observed Gradle merge/cause or task authority.'}))


    def preserve_framework_action(self, state):
        # Raw command log is already durable. Index exact marker bodies without duplicating hundreds of files.
        captures, rows, collection_errors, validation_errors = [], [], [], []
        argument_captures, argument_rows = [], []
        prefix, argument_prefix = b'PASSVAULT_ANDROID_FRAMEWORK_PACKAGE_ACTION01=', b'PASSVAULT_ANDROID_ARG_VECTOR='
        log_image, marker_count, summary, mapped = None, 0, None, False
        argument_count, argument_summary = 0, None
        signals = {'task_headers': 0, 'dry_run_outcomes': 0, 'build_success_lines': 0,
                   'misaligned_markers': 0, 'misaligned_argument_vectors': 0}

        def receipt_pairs(items):
            value = {}
            for key, item in items:
                require(key not in value, 'duplicate init JSON key')
                value[key] = item
            return value

        try:
            raw, log_image = self.f.read(E / 'logs/framework-actions.log', 16 * MIB)
            require(log_image == self.f.evidence[E / 'logs/framework-actions.log'], 'original action log changed')
            for number, line in enumerate(raw.splitlines(keepends=True), 1):
                signals['task_headers'] += int(line.lstrip().startswith(b'> Task '))
                signals['dry_run_outcomes'] += int(re.fullmatch(rb':[A-Za-z0-9:_-]+ SKIPPED\s*', line) is not None)
                signals['build_success_lines'] += int(line.startswith(b'BUILD SUCCESSFUL'))
                signals['misaligned_markers'] += int(prefix in line and not line.startswith(prefix))
                signals['misaligned_argument_vectors'] += int(argument_prefix in line and not line.startswith(argument_prefix))
                if line.startswith(argument_prefix):
                    argument_count += 1
                    body = line[len(argument_prefix):]  # Preserve original suffix/terminator; never repair JSON.
                    if argument_count > 1 or len(body) > 4097:
                        note(validation_errors, 'argument-vector count/size exceeded; raw log retained')
                        continue
                    destination = 'reports/graph-arguments-01.json'
                    try:
                        saved = self.f.write(E / destination, body)
                        argument_captures.append({'line': number, 'destination': destination, 'saved': saved})
                    except Exception as error:
                        note(collection_errors, 'argument-vector retention: ' + str(error))
                        continue
                    try:
                        require(marker_count == 0, 'argument vector must precede binding/graph/finished receipts')
                        require(body.endswith(b'\n') and not body.endswith(b'\r\n'),
                                'argument vector is not a complete LF-only record; original suffix retained')
                        row = json.loads(body.decode('utf-8'), object_pairs_hook=receipt_pairs,
                                         parse_constant=lambda _: require(False, 'nonfinite argument JSON'))
                        require(isinstance(row, dict), 'argument-vector JSON object required')
                        argument_rows.append(row)
                    except Exception as error:
                        note(validation_errors, 'argument-vector JSON row: ' + str(error))
                    continue
                if not line.startswith(prefix):
                    continue
                marker_count += 1
                body = line[len(prefix):]  # Exact raw suffix, including its original terminator (if any).
                if marker_count > 440 or len(body) > 2 * MIB + 1:
                    note(validation_errors, 'marker count/size exceeded; raw log retained')
                    continue
                captures.append({'line': number, 'source': 'logs/framework-actions.log',
                                 'prefix_bytes': len(prefix), 'body_bytes': len(body),
                                 'body_sha256': sha(body)})
                try:
                    require(body.endswith(b'\n') and not body.endswith(b'\r\n'),
                            'marker is not a complete LF-only record; original suffix retained without repair')
                    row = json.loads(body.decode('utf-8'), object_pairs_hook=receipt_pairs,
                                     parse_constant=lambda _: require(False, 'nonfinite init JSON'))
                    require(isinstance(row, dict), 'init JSON object required')
                    require(row.get('kind') != 'finished' or len(body) <= 32769, 'terminal receipt size bound')
                    require(row.get('kind') != 'framework-artifact-binding' or len(body) <= 4097,
                            'configuration binding receipt size bound')
                    rows.append(row)
                except Exception as error:
                    note(validation_errors, 'init JSON row: ' + str(error))
        except Exception as error:
            note(collection_errors, 'graph log/receipt retention: ' + str(error))
        try:
            require(not collection_errors and not validation_errors and signals['dry_run_outcomes'] == 0 and signals['build_success_lines'] == 1
                    and signals['misaligned_markers'] == signals['misaligned_argument_vectors'] == 0
                    and 412 < len(rows) == marker_count == len(captures) <= 440
                    and len(argument_rows) == argument_count == len(argument_captures) == 1,
                    'complete action/argument stream with successful terminal')
            commands = [c for c in state['commands'] if c['label'] == 'framework-actions']
            require(len(commands) == 1, 'exact original graph command record')
            command = commands[0]
            require(command['argv'] == ACTION_COMMAND and command['started'] is True and command['complete'] is True
                    and type(command['exit']) is int and command['exit'] == 0 and not command['errors']
                    and command.get('log') == log_image and state['command_zero_exit'] is True,
                    'complete exact action command/exit0/log; failure is not acceptance')
            argument_summary = validate_argument_vector(argument_rows[0])
            summary = validate_framework_action_rows(rows, ACTION_BINDING)
            mapped = True
        except Exception as error:
            note(validation_errors, 'action result mapping: ' + str(error))
        preserved = log_image is not None and not collection_errors
        state['graph'] = {'log_image': log_image, 'init_receipts': captures, 'marker_count': marker_count,
                          'argument_vectors': argument_captures, 'argument_vector_count': argument_count,
                          'argument_summary': argument_summary,
                          'summary': summary, 'task_or_success_log_signals': signals,
                          'collection_errors': collection_errors, 'validation_errors': validation_errors,
                          'evidence_preserved': preserved, 'action_mapping_ok': mapped,
                          'exclusive_global_failure_established': False, 'independent_final_log_review_required': True,
                          'declared_test_cases': 0, 'compiler_evidence_only': True, 'runtime_credit': False,
                          'qualification': 'Original compiler/package action result stream; final raw-log/source/terminal/cleanup '
                              'requires independent review. No runtime cases, production signing or future execution authority.'}
        self.f.write(E / 'GRAPH-EVIDENCE.json', canonical(state['graph']))
        self.action_preserved, self.action_mapping_ok = preserved, mapped
        if not preserved:
            self.note('Graph evidence retention uncertain; no automatic cleanup')
        if not mapped:
            self.note('Action result mapping incomplete; retained/no automatic retry')

    def observe_framework_action(self):
        state = self.state('android-framework-actions')
        state['command_zero_exit'], state['observation_ok'] = False, False
        try:
            require(isinstance(ACTION_BINDING, dict) and isinstance(ACTION_COMMAND, list), 'UNBOUND graph contract')
            self.check_inputs()
            self.binding_check()
            require(self.git_bound and self.settle() and not self.cancelled(), 'admitted Git/SDK-isolated graph phase')
            self.verify_framework_action_subject()
            self.verify_framework_action_properties()
            self.resources(True)
            # Unchanged original process primitive; this action requires exit0, failures remain retained.
            state['command_zero_exit'] = self.command('framework-actions', ACTION_COMMAND, ENV, 2400, state)
        finally:
            if state['stop_required']:
                try:
                    self.check_inputs(stopping=True)
                    self.source_check('STOP', True)
                    state['stop_ok'] = self.command('framework-actions-stop', STOP, ENV, 600, state, stopping=True)
                except Exception as error:
                    self.note('original wrapper stop incomplete/no retry: ' + str(error))
            try:
                state['settled'] = self.settle()
                if state['settled'] and state['stop_required']:
                    self.preserve_framework_action(state)
            except Exception as error:
                self.note('graph evidence/settlement uncertainty: ' + str(error))
            state['observation_ok'] = bool(state['stop_ok'] and state['settled'] and self.action_mapping_ok
                                           and not self.cancelled())
            state['record_image'] = self.f.write(E / 'PHASE-android-framework-actions.json', canonical(state))
        require(state['observation_ok'], 'AndroidFrameworkPackageAction01 incomplete; no automatic retry')


def main():
    require(sys.platform == 'linux' and sys.argv[0] == str(SELF) and len(sys.argv) == 3
            and sys.flags.isolated and sys.dont_write_bytecode and sys.flags.no_site
            and os.getuid() == os.geteuid() == 0, 'fixed isolated absolute root entry')
    require(all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{40}', value) for value in (COMMIT, TREE))
            and type(MEMBERS) is int and type(MEMBER_LIMIT) is int and 1 <= MEMBERS <= MEMBER_LIMIT
            and type(SOURCE_BYTES) is int and 0 < SOURCE_BYTES <= 32 * MIB
            and type(OID_BYTES) is int and OID_BYTES == MEMBERS * 41
            and all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{64}', value) for value in FROZEN.values())
            and all(isinstance(path, Path) and path.is_absolute() and '..' not in path.parts
                    for path in (GIT_IMAGE, RUBY_IMAGE))
            and EXCLUDE_STATE in ('INFO_ABSENT', 'EXCLUDE_ABSENT', 'FILE'),
            'PENDING final source/tool/exclude identities; no execution admission')
    require(isinstance(ACTION_BINDING, dict) and set(ACTION_BINDING) == {
                'runtimeRoot', 'evidenceRoot', 'readRoots', 'offline', 'sourceManifestSha256', 'subjectSha256'}
            and ACTION_BINDING['runtimeRoot'] == str(R) and ACTION_BINDING['evidenceRoot'] == str(E)
            and ACTION_BINDING['readRoots'] == [str(p) for p in READ_ROOT_ENVELOPE]
            and type(ACTION_BINDING['offline']) is bool
            and ACTION_BINDING['sourceManifestSha256'] == FROZEN[SOURCE]
            and ACTION_BINDING['subjectSha256'] == ACTION_SUBJECT_SHA,
            'UNBOUND exact new graph/source/read-root/offline binding')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = AndroidFrameworkPackageAction01(files, dict(zip(('pid', 'mnt'), sys.argv[1:])))
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
        batch.observe_framework_action()
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
        phase_names = [p['phase'] for p in batch.phases]
        phase_contract = phase_names in (['git-sdk-isolation'], ['git-sdk-isolation', 'android-framework-actions'])
        attempted = any(p['phase'] == 'android-framework-actions' and p['stop_required'] for p in batch.phases)
        safe = (batch.namespace_ok and batch.before and batch.after and stops and settled and batch.evidence_ok
                and complete and phase_contract and batch.git_bound and batch.git_stable
                and batch.index_unchanged and not CANCEL and (not attempted or batch.action_preserved))
        collected = bool(safe and attempted and batch.action_mapping_ok
            and phase_names == ['git-sdk-isolation', 'android-framework-actions']
            and all(p.get('setup_ok') if p['phase'] == 'git-sdk-isolation' else p.get('observation_ok')
                    for p in batch.phases))
        result = {'format': 'passvault-linux-android-framework-package-action01-inner-v1', 'run_id': RUN_ID,
            'commit': COMMIT, 'tree': TREE, 'parent_namespaces': batch.parent,
            'source_before': batch.before, 'source_after': batch.after, 'source_capacity': SOURCE_CAPACITY,
            'all_required_stops_ok': stops, 'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'git_bindings_established': batch.git_bound, 'git_bindings_stable': batch.git_stable,
            'sealed_index_unchanged': batch.index_unchanged, 'action_evidence_preserved': batch.action_preserved,
            'action_mapping_ok': batch.action_mapping_ok,
            'action_evidence_collected': collected, 'gradle_action_selectors': ACTION_TARGETS,
            'method_filter': ACTION_FILTER, 'action_binding': ACTION_BINDING,
            'independent_final_log_review_required': True, 'exclusive_global_failure_established': False,
            'sdk_readonly_view_established': batch.git_bound, 'sdk_readonly_view_stable': batch.git_stable,
            'declared_test_cases': 0, 'errors': batch.errors, 'phases': batch.phases,
            'independent_semantic_acceptance': False,
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'init_sha256': FROZEN[INIT],
                                  'representation': 'RAW_GIT_BLOBS'},
            'resource_point_samples_not_ownership_or_cleanup_proof': batch.inventories,
            'incomplete_resource_samples': sum(not s['traversal_finished'] or bool(s['vanished_entries']
                or s['vanished_scan_directories']) for s in batch.inventories),
            'qualification': 'Finite compiler/Debug-package action evidence only; '
                'independent final raw-log/terminal review remains required. No Android runtime, '
                'hardware or Release readiness claim. RO Git/SDK aliases are not immutable snapshots '
                'against outside mutation. Outer must prove exit, no runtime mounts, original '
                'ownership and allowlisted cleanup.'}
        try:
            files.write(E / 'INNER-RESULT.json', canonical(result))
            files.verify()
            code = (0 if collected and not batch.errors else 1) if safe else 70
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
