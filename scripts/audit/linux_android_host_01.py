#!/usr/bin/python3
"""INERT proposed fixed Host1 PID1 source; NOT execution admission.
Text-reuses accepted Graph04 lifecycle, never imports/runs its consumed helper.
One actual Android host clipboard policy case with fresh compiler/resource/Test
joins, exact retained XML, original same-environment stop and bounded cleanup.
A result is pending independent actual review; no device/native/PVA009 closure.
Root owns fresh request/instance/quiet-host/SDK/Git bindings and original cleanup.
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
R = Path('/root/projects/PassVault/audit-runtime-linux-android-host01')
E = B / 'runs/linux-android-host01'
CHECKOUT = R / 'checkout'
SELF, INIT = W / 'scripts/audit/linux_android_host_01.py', W / 'scripts/audit/android_host_01.init.gradle'
GRAPH_DATA = B / 'runs/linux-android-host-graph04/reports/graph-init-01.json'
SOURCE = B / 'reviews/checkpoint22/source-prepare01/SOURCE.json'  # Proposed current capture bytes/hash; independent actual adoption and instance pending.
JAVA, MOUNT = Path('/usr/lib/jvm/java-17-openjdk-amd64'), Path('/usr/bin/mount')
SDK, SDK_VIEW = Path('/opt/android-sdk'), R / 'sdk-readonly'
SDK_METADATA = tuple(SDK / p for p in ('platforms/android-37.0/source.properties',
    'platforms/android-37.0/package.xml', 'build-tools/36.0.0/source.properties',
    'build-tools/36.0.0/package.xml', 'licenses/android-sdk-license'))
SDK_OPERATION = 'READ_ONLY_EXISTING_COMPILE_SDK_NO_COPY_INSTALL_LICENSE_CHANGES'
HOST_BINDING = {'runtimeRoot': '/root/projects/PassVault/audit-runtime-linux-android-host01', 'evidenceRoot': '/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/runs/linux-android-host01', 'readRoots': ['/root/projects/PassVault/audit-runtime-linux-android-host01/checkout', '/root/projects/PassVault/audit-runtime-linux-android-host01/gradle-home', '/root/projects/PassVault/audit-runtime-linux-android-host01/tmp', '/root/projects/PassVault/audit-runtime-linux-android-host01/konan', '/usr/lib/jvm/java-17-openjdk-amd64', '/root/projects/PassVault/audit-runtime-linux-android-host01/sdk-readonly'], 'offline': False, 'sourceManifestSha256': '5eed5ec803e336c3868d1fe4255f02535fda3672ed5455e7499d7b9ffc881aa6', 'subjectSha256': 'ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332'}  # Proposed current SOURCE literals; independent actual adoption/instance remains root-owned.
READ_ROOT_ENVELOPE = (CHECKOUT, R / 'gradle-home', R / 'tmp', R / 'konan', JAVA, SDK_VIEW)
PUBLICATION_GIT = Path('/root/projects/PassVault/passvault-publication-20260912-02/.git')
GIT_DIR, GIT_INDEX = R / 'git-metadata', R / 'git-index'
GIT_CONFIG, GIT_EXCLUDE = GIT_DIR / 'config', GIT_DIR / 'info/exclude'
# Product P/current SOURCE literals are proposed below; actual adoption and current SDK/read-root/
# tool/alias/exclude facts still require root's final binding, consistency and independent admission.
COMMIT = 'd2d79ad52ce816a36f708bd35bd736a7d8068789'  # Accepted product P supplied by root; not a SOURCE capture observation.
TREE = '2e83afec1894238f59c1e9916cbf548ad54f5da2'  # Accepted product P supplied by root; not a SOURCE capture observation.
MEMBERS = 4548  # Proposed complete checkpoint22 SOURCE count; independent actual adoption pending.
MEMBER_LIMIT = 4548  # Proposed exact source ceiling; no automatic widening.
SOURCE_BYTES = 2519894  # Proposed exact current SOURCE length; independent actual adoption pending.
OID_BYTES = 186468  # Proposed4548*41 at BOTH writer/reader; full ordered OID vector.
SOURCE_CAPACITY = {'members': MEMBERS, 'member_limit': MEMBER_LIMIT, 'manifest_bytes': SOURCE_BYTES,
                   'oid_bytes': OID_BYTES, 'raw_stream_limit_bytes': 160 * 1024 * 1024}
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = 'INFO_ABSENT'  # Retained checkpoint22 candidate; root must admit unchanged current U.
FROZEN = {SOURCE: '5eed5ec803e336c3868d1fe4255f02535fda3672ed5455e7499d7b9ffc881aa6', INIT: '9ebd1a2af73f4a08e3f2193e1e3b2f8944b6149ec9333a4439f44e035b36e287', GRAPH_DATA: '16908a3e0a4120fa6ccc4c8dead1a35d8d39270d05f0f34e1212ea8109f9e459'}  # Proposed source/control hashes; independent actual adoption/instance pending.
INPUTS = (SELF, INIT, SOURCE, GRAPH_DATA, JAVA / 'bin/java', JAVA / 'release', MOUNT, GIT_INDEX) + SDK_METADATA
RUN_ID = 'linux-android-host01'
REVIEW_ASSERTIONS = (
    'ordinary_full_stage0_index_matches_source', 'no_split_sparse_unmerged_index',
    'standalone_store_without_redirects', 'local_config_and_excludes_reviewed',
    'no_external_git_config_or_executable_mechanisms', 'publication_store_frozen_for_run',
)
WORKER_CHILDREN = ('home', 'tmp', 'jna', 'sqlite', 'android-user', 'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
WORKER_DIRS = ('workers', 'workers/clipboard') + tuple('workers/clipboard/' + n for n in WORKER_CHILDREN)
PRIVATE = ('checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
# Outer separately owns empty Git/SDK mountpoints; their inodes are not the bound views.
ORIGINAL_DIRS = (R, E, E / 'logs', E / 'reports', E / 'xml') + tuple(R / p for p in PRIVATE + WORKER_DIRS)
MIB, GIB = 1024 ** 2, 1024 ** 3
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
HOST_COMMAND = [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT),
    ':app-android:testDebugUnitTest', '--info', '-Ppassvault.audit.mode=android-host1'] + FLAGS
if HOST_BINDING['offline']:
    HOST_COMMAND += ['--offline']  # Fixed policy, never a resolution fallback.
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


HOST_TARGET = ':app-android:testDebugUnitTest'
HOST_FILTER = ('com.passvault.android.security.AndroidClipboardServiceTest.'
                'newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry')
HOST_SUBJECT_SHA = 'ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332'
HOST_LEGAL = ':app-android:prepareAndroidLegalAssets'
HOST_PROPERTY_CONTRACT = 'EXACT_CLI_PLUS_FIXED_TOOLCHAIN_6_V1'
HOST_PROPERTY_SOURCE = {
    'path': 'gradle.properties', 'git_mode': '100644',
    'git_blob': 'a0f3d9ab2e45cae4e85feb468ba6d18b2aa9d57b', 'git_size': 1173,
    'raw_size': 1173, 'raw_sha256': '323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235',
    'checkout_size': 1173, 'checkout_sha256': '323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235',
}


def validate_argument_vector(row):
    # Additional receipt condition only. It never replaces action/case callbacks, exit0 or exact XML.
    expected = {'format': 'passvault-android-argument-vector-v3', 'runId': RUN_ID,
        'phase': 'AFTER_ARG12_BEFORE_ARG13', 'sourceManifestSha256': 'UNBOUND',
        'propertySourceSha256': HOST_PROPERTY_SOURCE['raw_sha256'], 'propertyContract': HOST_PROPERTY_CONTRACT,
        'qualification': 'NEW_EXACT_CONTRACT_NOT_REPRODUCED_CAUSE_OR_TASK_AUTHORITY',
        'projectMapKind': 'MAP', 'systemMapKind': 'MAP'}
    require(isinstance(row, dict) and set(row) == set(expected) | {
                'compatibilityShape', 'keys', 'types', 'outcomes', 'diagnosticError'}
            and all(row.get(k) == v for k, v in expected.items())
            and row['diagnosticError'] is False, 'exact new-contract bounded argument-vector schema')
    shape = row['compatibilityShape']
    require(shape == 'EXACT_CLI_PLUS_FIXED_TOOLCHAIN_6', 'exact fixed-toolchain property contract did not accept')
    keys = row['keys']
    require(isinstance(keys, dict) and set(keys) == {'state', 'count', 'expectedMask', 'sourceOnlyMask',
                'sourceStringMask', 'sourceValueMatchMask', 'toolchainOnlyMask', 'toolchainStringMask',
                'toolchainValueMatchMask', 'sizeAndExpectedMembershipAgree'}
            and keys['state'] == 'COUNT_AND_FIXED_KEYS_VALUES_ONLY'
            and type(keys['count']) is int and keys['count'] == 6
            and keys['expectedMask'] == '111'
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
            'property_source_sha256': HOST_PROPERTY_SOURCE['raw_sha256'],
            'diagnostic_not_graph_or_task_authority': True}


# Inert proposed Host1 result mapper. Called only by a future admitted Host1 inner.
HOST_CLASS = 'com.passvault.android.security.AndroidClipboardServiceTest'
HOST_METHOD = 'newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry'
HOST_CLASS_PACKAGE = 'com/passvault/android/security/'
HOST_CONFIG_RESOURCE = 'com/android/tools/test_config.properties'
HOST_MAIN_COMPILER = ':app-android:compileDebugKotlin'
HOST_TEST_COMPILER = ':app-android:compileDebugUnitTestKotlin'
HOST_ROOM = ':core:database:copyRoomSchemas'
HOST_PACKAGE = ':app-android:packageDebugUnitTestForUnitTest'
HOST_CONFIG = ':app-android:generateDebugUnitTestConfig'
HOST_SCHEMAS = {
    'com.passvault.core.database.VaultDatabase/1.json': '76985152ec6593ee7eb13343bf30e86b1fd7beacbb27e89c4a2cfcb286eeb586',
    'com.passvault.core.database.VaultDatabase/2.json': '3c09e59f2ab2b59a2535c82a404b9af27b9e3934f88e4161c1a9478cdef38d94',
    'com.passvault.core.database.VaultDatabase/3.json': '080ac957b9dcdb58bc4950cb3e1f6a8279a0432e2a3ea580ba3aa0c84add9136',
    'com.passvault.core.database.VaultDatabase/4.json': '9dc4f187784ca4a409b258debab1fbf9ad8a700778e060c44b98d9d4d8d14224',
    'com.passvault.core.database.VaultDatabase/5.json': '431ed8a2dbfcde5c132f45a2e4443894d45aa0010bf9b6616780f62be3cd354b',
}


def validate_host_rows(rows, prior):
    common = {'format': 'passvault-android-host1-receipt-v1', 'runId': RUN_ID,
        'sourceManifestSha256': FROZEN[SOURCE], 'subjectSha256': HOST_SUBJECT_SHA,
        'selector': [HOST_TARGET], 'methodFilter': HOST_FILTER, 'graphOnly': False}
    kinds = {'schema-equality', 'action-admission', 'compiler-source-affinity', 'compiler-classes',
        'host-resource-archive', 'compiler-test-join', 'host-config-output', 'case-start', 'case-result',
        'task-state', 'finished'}
    require(isinstance(rows, list) and len(rows) == 348 and all(isinstance(row, dict)
        and all(row.get(k) == v for k, v in common.items()) and row.get('kind') in kinds for row in rows),
        'exact bounded Host1 receipt/common/348-event stream')
    selected = {kind: [row for row in rows if row['kind'] == kind] for kind in kinds}
    require(len(selected['schema-equality']) == 4 and len(selected['task-state']) == 333
        and len(selected['compiler-source-affinity']) == len(selected['compiler-classes']) == 2
        and all(len(selected[k]) == 1 for k in kinds - {'schema-equality', 'task-state',
            'compiler-source-affinity', 'compiler-classes'}), 'exact Host1 event multiplicities')
    graph = selected['action-admission'][0]
    expected = {row['path']: row for row in prior['tasks']}
    require(prior['sourceManifestSha256'] == FROZEN[SOURCE] and prior['taskCount'] == len(expected) == 333
        and prior['edgeReferences'] == 663 and graph['graphDataSha256'] == FROZEN[GRAPH_DATA]
        and graph['taskCount'] == 333 and graph['edgeReferences'] == 663,
        'accepted original graph-data/source tuple')
    output_rows = graph['outputRoots']
    require(isinstance(output_rows, list) and len(output_rows) == 333
        and {r['task'] for r in output_rows} == set(expected), '333 actual task-output bindings')
    outputs = {}
    schema_root = CHECKOUT / 'core/database/schemas'
    for row in output_rows:
        name = row['task']
        require(row['type'] == expected[name]['type'] and isinstance(row['outputs'], list)
            and len(row['outputs']) <= 64 and len(set(row['outputs'])) == len(row['outputs']),
            'exact concrete task type/unique bounded generic output roots')
        module = CHECKOUT.joinpath(*name[1:].split(':')[:-1]) / 'build'
        paths = [Path(p) for p in row['outputs']]
        require(all(p.is_absolute() and '..' not in p.parts and len(str(p)) <= 1024 for p in paths),
            'normalized bounded actual output path')
        if name == HOST_ROOM:
            require(paths == [schema_root] and row['role'] == 'PRIVATE_SOURCE_COPY_SCHEMA_EXCEPTION',
                'only copied canonical Room schema output exception')
        else:
            require(row['role'] == 'PRIVATE_MODULE_BUILD_INTERMEDIATE_OR_REPORT'
                and all(p != module and p.is_relative_to(module)
                    and not p.is_relative_to(module / 'outputs/apk')
                    and not p.is_relative_to(module / 'outputs/bundle') for p in paths),
                'only exact private module build outputs; no final app APK/bundle producer')
        outputs[name] = paths
    critical = {HOST_MAIN_COMPILER, HOST_TEST_COMPILER, HOST_TARGET, HOST_ROOM, HOST_PACKAGE, HOST_CONFIG}
    require(all(outputs[t] for t in critical | {HOST_LEGAL}), 'critical declared producer roots required')
    states = selected['task-state']
    require({row['task'] for row in states} == set(expected), 'one outcome for each admitted task')
    for row in states:
        require(row['type'] == expected[row['task']]['type'] and row['failureType'] is None
            and all(type(row[k]) is bool for k in ('executed', 'skipped', 'upToDate', 'noSource', 'didWork')),
            'typed successful task-state observations')
        if row['task'] in critical:
            require(row['executed'] and not any(row[k] for k in ('skipped', 'upToDate', 'noSource'))
                and row['skipMessage'] is None and (row['task'] == HOST_ROOM or row['didWork']),
                'critical compiler/config/package/finalizer/Test actions must execute freshly')
    schema_rows = selected['schema-equality']
    require([r['at'] for r in schema_rows] == ['before-actions', 'before-schema-finalizer',
        'after-schema-finalizer', 'before-test'], 'complete before/after schema-finalizer ordering')
    for row in schema_rows:
        require(row['equal'] is True and row['root'] == str(schema_root) and len(row['files']) == 5
            and {str(Path(f['path']).relative_to(schema_root)): f['sha256'] for f in row['files']} == HOST_SCHEMAS,
            'all five canonical runtime-copy schemas unchanged')
    affinities = selected['compiler-source-affinity']
    require({r['task'] for r in affinities} == {HOST_MAIN_COMPILER, HOST_TEST_COMPILER}
        and all(r['api'] == 'Task.inputs.files' for r in affinities), 'two generic compiler source affinities')
    sources = {
        HOST_MAIN_COMPILER: ('app-android/src/main/kotlin/com/passvault/android/security/AndroidClipboardService.kt',
            'db1b9e5387e7059719e0f49b4a8c9340f8abcf210d16a808a5408c17443e00cf'),
        HOST_TEST_COMPILER: ('app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt',
            HOST_SUBJECT_SHA),
    }
    for row in affinities:
        relative, digest = sources[row['task']]
        require(row['source'] == str(CHECKOUT / relative) and row['sha256'] == digest,
            'exact selected current source path/SHA, not just compiler-task affinity labels')
    wanted = {}
    producers = selected['compiler-classes']
    require({r['task'] for r in producers} == {HOST_MAIN_COMPILER, HOST_TEST_COMPILER}, 'both fresh compiler cohorts')
    for producer in producers:
        require(isinstance(producer['classes'], list) and 1 <= len(producer['classes']) <= 512
            and producer['testCases'] == 0, 'bounded compiler cohort is not test-case evidence')
        for row in producer['classes']:
            relative = row['relativeClass']
            require(relative.startswith(HOST_CLASS_PACKAGE) and relative.endswith('.class')
                and relative not in wanted and row['header'] == 'cafebabe0000003d'
                and re.fullmatch(r'[0-9a-f]{64}', row['sha256'])
                and any(Path(row['path']).is_relative_to(p) for p in outputs[producer['task']]),
                'unique fresh JVM17 classes in actual declared compiler roots')
            wanted[relative] = row['sha256']
    require(all(HOST_CLASS_PACKAGE + name + '.class' in wanted for name in
        ('AndroidClipboardService', 'AndroidClipboardServiceTest')), 'both critical base classes produced')
    archive = selected['host-resource-archive'][0]['evidence']
    identity = archive['identity']
    require(Path(identity['path']).is_relative_to(CHECKOUT / 'app-android/build/intermediates')
        and any(Path(identity['path']).is_relative_to(p) for p in outputs[HOST_PACKAGE])
        and re.fullmatch(r'[0-9a-f]{64}', identity['sha256'])
        and identity['header'].startswith('504b0304') and 1 <= archive['entries'] <= 32768
        and 0 < archive['expandedBytes'] <= 128 * MIB
        and set(archive['essential']) == {'AndroidManifest.xml', 'resources.arsc'},
        'bounded actual unsigned resource-only host archive evidence')
    config = selected['host-config-output'][0]['evidence']
    require(config['consumerResource'] == HOST_CONFIG_RESOURCE and config['resourceArchive'] == identity
        and any(Path(config['identity']['path']).is_relative_to(p) for p in outputs[HOST_CONFIG]),
        'actual host properties identify exact inspected resource archive')
    wanted[HOST_CONFIG_RESOURCE] = config['identity']['sha256']
    join = selected['compiler-test-join'][0]
    require(join['resourceConfiguration'] == config and set(join['classesAndConfig']) == set(wanted),
        'exact fresh compiler/configuration consumer join keys')
    for relative, digest in wanted.items():
        rows_for_class = join['classesAndConfig'][relative]
        require(isinstance(rows_for_class, list) and len(rows_for_class) == 1
            and rows_for_class[0]['classSha256'] == digest
            and any(Path(rows_for_class[0]['carrier']).is_relative_to(p) for p in READ_ROOT_ENVELOPE),
            'one exact private/read-only effective classpath carrier; no alternate critical winner')
    directories = join['classDirectories']
    require(isinstance(directories, list) and 1 <= len(directories) <= 16
        and len(set(directories)) == len(directories)
        and all(Path(p).is_absolute() and '..' not in Path(p).parts
            and any(Path(p).is_relative_to(root) for root in READ_ROOT_ENVELOPE) for p in directories),
        'finite effective Test class-directory roots')
    test_producer = next(row for row in producers if row['task'] == HOST_TEST_COMPILER)
    for row in test_producer['classes']:
        require(sum(Path(row['path']) == Path(p) / row['relativeClass'] for p in directories) == 1,
            'each fresh test compiler output belongs to exactly one effective Test class directory')
    positions = {id(row): number for number, row in enumerate(rows)}
    state_by_task = {row['task']: row for row in states}
    position = lambda row: positions[id(row)]
    for affinity in affinities:
        produced = next(row for row in producers if row['task'] == affinity['task'])
        require(position(graph) < position(affinity) < position(state_by_task[affinity['task']])
            < position(produced) < position(join), 'source affinity then fresh task outcome/classes then Test join')
    require(position(schema_rows[0]) < position(graph) < position(schema_rows[1])
        < position(state_by_task[HOST_ROOM]) < position(schema_rows[2]) < position(schema_rows[3])
        < position(join), 'canonical schemas before action, around finalizer and before Test join')
    require(position(state_by_task[HOST_PACKAGE]) < position(selected['host-resource-archive'][0])
        < position(join) and position(state_by_task[HOST_CONFIG]) < position(join)
        and position(join) < position(selected['host-config-output'][0]) < position(selected['case-start'][0])
        < position(selected['case-result'][0]) < position(state_by_task[HOST_TARGET])
        < position(selected['finished'][0]), 'actual producer/configuration joins before sole case and Test completion')
    start, result = selected['case-start'][0], selected['case-result'][0]
    require(all(row['className'] == HOST_CLASS and row['method'] == HOST_METHOD for row in (start, result))
        and result['result'] == 'SUCCESS' and result['tests'] == result['successes'] == 1
        and result['failures'] == result['skipped'] == 0, 'one exact real unsuffixed method callback')
    terminal = selected['finished'][0]
    require(rows[-1] is terminal and terminal['localHostPass'] is True and terminal['failureType'] is None
        and terminal['controlFailure'] is False and terminal['taskActionsAdmitted'] is True
        and terminal['graphCount'] == terminal['whenReadyEntries'] == terminal['finishCallbacks'] == 1
        and terminal['beforeTaskEntries'] == terminal['afterTaskEvents'] == 333
        and terminal['schemaFinalizerComplete'] is True and terminal['testComplete'] is True
        and set(terminal['compilerCompletions']) == {HOST_MAIN_COMPILER, HOST_TEST_COMPILER}
        and terminal['caseStarts'] == terminal['caseCompletions'] == terminal['casePasses'] == 1
        and terminal['finalProcessAndXmlReviewRequired'] is True, 'complete callback-local Host1 terminal')
    return {'actual_method_callbacks': 1, 'actual_task_outcomes': 333, 'accepted_graph_sha256': FROZEN[GRAPH_DATA],
        'critical_class_count': len(wanted) - 1, 'resource_archive': identity, 'configuration': config['identity'],
        'qualification': 'Callback/producer-consumer evidence only. Original exit0 and exact XML still required; '
            'injected clipboard policy regression, not actual Android clipboard/native/device/hardware closure.'}


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


class AndroidHost01:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs = [], [], {}
        self.source = self.outer = self.initial = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.git_bound = self.git_stable = self.index_unchanged = False
        self.host_preserved = self.host_mapping_ok = False
        self.xml_preserved = self.xml_mapping_ok = False
        self.expected_graph = None
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
        require(value['format'] == 'passvault-linux-android-host01-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == MEMBERS
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent
                and value['source_capacity'] == SOURCE_CAPACITY,
                'fixed fresh AndroidHost01 intake; not independent execution admission')
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
        require(len(captured[GRAPH_DATA]) == 131015, 'exact independently accepted graph-data length')
        self.expected_graph = json.loads(captured[GRAPH_DATA])
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
        require(not os.listdir(self.f.directory(E / 'reports')) and not os.listdir(self.f.directory(E / 'xml')),
                'fresh Host1 receipt/XML evidence directories')
        sdk = value['sdk_existing_use']
        require(value['host_binding'] == HOST_BINDING and sdk['root'] == str(SDK)
                and sdk['metadata_paths'] == [str(p) for p in SDK_METADATA]
                and sdk['operation'] == SDK_OPERATION, 'exact reviewed Host1/read-only existing SDK scope')
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
                    # Host3600/original-stop600 include their 20s TERM/KILL tail; lifecycle primitive unchanged.
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



    def verify_host_subject(self):
        # Passive source affinity, NOT compiler input visitation or a test outcome.
        relative = 'app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt'
        rows = [r for r in self.source['files'] if r['path'] == relative]
        require(len(rows) == 1, 'exact host subject membership in final source manifest')
        data, image = self.f.read(CHECKOUT / relative, MIB)
        require(sha(data) == HOST_SUBJECT_SHA
                and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == rows[0]['git_blob'],
                'host subject SHA256/final raw Git-blob affinity')
        self.f.write(E / 'HOST-SUBJECT.json', canonical({'relative_source': relative, 'image': image,
                     'sha256': HOST_SUBJECT_SHA, 'source_manifest_sha256': HOST_BINDING['sourceManifestSha256'],
                     'not_compiler_visitation_or_test_evidence': True}))



    def verify_host_properties(self):
        # Exact complete checked-in file affinity, not a claim about Gradle merge behavior.
        relative = HOST_PROPERTY_SOURCE['path']
        rows = [r for r in self.source['files'] if r['path'] == relative]
        # Keep the whole SOURCE row; its observed_checkout_pin is historical, not runtime authority.
        require(len(rows) == 1 and set(rows[0]) == (set(HOST_PROPERTY_SOURCE) | {'observed_checkout_pin'})
                and {key: rows[0][key] for key in HOST_PROPERTY_SOURCE} == HOST_PROPERTY_SOURCE,
                'exact checked-property source schema/raw-affinity projection')
        data, image = self.f.read(CHECKOUT / relative, HOST_PROPERTY_SOURCE['raw_size'])
        require(len(data) == HOST_PROPERTY_SOURCE['raw_size'] and sha(data) == HOST_PROPERTY_SOURCE['raw_sha256']
                and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
                    == HOST_PROPERTY_SOURCE['git_blob'], 'checked-property raw SHA256/Git-blob affinity')
        self.f.write(E / 'PROPERTY-CONTRACT-SOURCE.json', canonical({'relative_source': relative, 'image': image,
            'source_record': rows[0], 'source_manifest_sha256': HOST_BINDING['sourceManifestSha256'],
            'contract': HOST_PROPERTY_CONTRACT,
            'qualification': 'Exact raw-affinity projection; observed_checkout_pin is historical whole-SOURCE-bound metadata, '
                'not current runtime identity. No observed Gradle merge/cause or task authority.'}))


    def preserve_host_xml(self, state):
        root = CHECKOUT / 'app-android/build/test-results/testDebugUnitTest'
        expected = root / ('TEST-' + HOST_CLASS + '.xml')
        captures, errors, observed, total = [], [], set(), 0
        mapping, enumerated = False, False
        try:
            try:
                names = sorted(n for n in os.listdir(self.f.directory(root))
                               if n.startswith('TEST-') and n.endswith('.xml'))
            except FileNotFoundError:
                names = []  # An absent settled output is retained as absent, never a passing test.
            require(len(names) <= 16, 'bounded host XML file count')
            enumerated = True
            for name in names:
                path = root / name
                data, original = self.f.read(path, 2 * MIB)
                total += len(data)
                require(total <= 4 * MIB, 'aggregate host XML byte bound')
                saved = self.f.write(E / 'xml' / name, data)
                captures.append({'source': str(path), 'original': original, 'saved': saved})
                observed.add(path)
                try:
                    raw = data.decode('utf-8')
                    require(path == expected and raw and '\x00' not in raw
                        and '<!DOCTYPE' not in raw and '<!ENTITY' not in raw,
                        'unexpected/empty/declaring/non-UTF8 host XML')
                    suite = ET.fromstring(raw)
                    cases = suite.findall('testcase')
                    require(suite.tag == 'testsuite' and suite.get('name') == HOST_CLASS
                        and len(list(suite.iter('testsuite'))) == 1 and len(cases) == 1
                        and len(list(suite.iter('testcase'))) == 1
                        and cases[0].get('classname') == HOST_CLASS and cases[0].get('name') == HOST_METHOD,
                        'exact one unsuffixed Android host suite/method; no Desktop aliases')
                    require(suite.get('tests') == '1'
                        and all(suite.get(k) == '0' for k in ('failures', 'errors', 'skipped'))
                        and all(not list(suite.iter(k)) for k in ('failure', 'error', 'skipped')),
                        'host suite failure/error/skipped case')
                    mapping = True
                except (RuntimeError, ValueError, UnicodeError, ET.ParseError) as error:
                    note(errors, error)
        except Exception as error:
            enumerated = False
            note(errors, 'host XML retention: ' + str(error))
        preserved = enumerated and len(captures) == len(observed)
        mapping = bool(preserved and mapping and observed == {expected} and not errors)
        state['xml'] = {'root': str(root), 'expected': str(expected), 'captures': captures,
            'observed_file_count': len(observed), 'bytes': total, 'preserved': preserved,
            'mapping_ok': mapping, 'errors': errors, 'actual_successful_cases': 1 if mapping else 0}
        self.xml_preserved, self.xml_mapping_ok = preserved, mapping
        self.f.write(E / 'HOST-XML.json', canonical(state['xml']))
        if not preserved:
            self.note('Host XML preservation incomplete; original cleanup not admitted')
        if not mapping:
            self.note('Host exact XML absent/failed/unmatched; no case pass inferred')

    def preserve_host(self, state):
        # Raw log is already durable; conserve marker suffixes before interpreting them.
        prefix, argument_prefix = b'PASSVAULT_ANDROID_HOST1=', b'PASSVAULT_ANDROID_ARG_VECTOR='
        captures, rows, argument_rows, collection_errors, validation_errors = [], [], [], [], []
        events, arguments, log_image, summary, argument_summary = [], [], None, None, None
        marker_count, argument_count = 0, 0
        signals = {'dry_run_outcomes': 0, 'build_success_lines': 0, 'build_failure_lines': 0,
            'misaligned_markers': 0, 'misaligned_argument_vectors': 0}
        mapped = False

        def pairs(items):
            value = {}
            for key, item in items:
                require(key not in value, 'duplicate Host1 receipt JSON key')
                value[key] = item
            return value

        try:
            raw, log_image = self.f.read(E / 'logs/host1.log', 4 * MIB)
            require(log_image == self.f.evidence[E / 'logs/host1.log'], 'original Host1 log changed')
            for number, line in enumerate(raw.splitlines(keepends=True), 1):
                signals['dry_run_outcomes'] += int(re.fullmatch(rb':[A-Za-z0-9:_-]+ SKIPPED\s*', line) is not None)
                signals['build_success_lines'] += int(line.startswith(b'BUILD SUCCESSFUL'))
                signals['build_failure_lines'] += int(line.startswith(b'BUILD FAILED'))
                signals['misaligned_markers'] += int(prefix in line and not line.startswith(prefix))
                signals['misaligned_argument_vectors'] += int(argument_prefix in line and not line.startswith(argument_prefix))
                is_argument = line.startswith(argument_prefix)
                if not is_argument and not line.startswith(prefix):
                    continue
                if is_argument:
                    argument_count += 1
                    body = line[len(argument_prefix):]
                    ordinal = argument_count
                else:
                    marker_count += 1
                    body = line[len(prefix):]
                    ordinal = marker_count
                try:
                    require((argument_count <= 1 and len(body) <= 4097) if is_argument
                        else (marker_count <= 384 and len(body) <= MIB + 1), 'Host1 marker count/size bound')
                    (arguments if is_argument else events).append(body)
                    captures.append({'line': number, 'kind': 'argument' if is_argument else 'host',
                        'ordinal': ordinal, 'bytes': len(body), 'sha256': sha(body)})
                    require(body.endswith(b'\n') and not body.endswith(b'\r\n'), 'complete original LF-only marker suffix')
                    row = json.loads(body.decode('utf-8'), object_pairs_hook=pairs,
                        parse_constant=lambda _: require(False, 'nonfinite Host1 JSON'))
                    require(isinstance(row, dict), 'Host1 marker JSON object')
                    if is_argument:
                        require(marker_count == 0, 'argument vector precedes Host1 action events')
                        argument_rows.append(row)
                    else:
                        rows.append(row)
                except Exception as error:
                    note(validation_errors, 'Host1 marker: ' + str(error))
            self.f.write(E / 'reports/host-init.ndjson', b''.join(events))
            self.f.write(E / 'reports/host-arguments.ndjson', b''.join(arguments))
        except Exception as error:
            note(collection_errors, 'Host1 log/marker retention: ' + str(error))
        try:
            require(not collection_errors and not validation_errors and marker_count == len(rows) == 348
                and argument_count == len(argument_rows) == 1
                and signals == {'dry_run_outcomes': 0, 'build_success_lines': 1, 'build_failure_lines': 0,
                    'misaligned_markers': 0, 'misaligned_argument_vectors': 0},
                'exact complete actual Host1 stream; never map graph refusal to pass')
            commands = [c for c in state['commands'] if c['label'] == 'host1']
            require(len(commands) == 1 and commands[0]['argv'] == HOST_COMMAND
                and commands[0]['started'] is True and commands[0]['complete'] is True
                and type(commands[0]['exit']) is int and commands[0]['exit'] == 0
                and not commands[0]['errors'] and commands[0].get('log') == log_image
                and state['command_zero_exit'] is True, 'one exact original completed Host1 exit0 command')
            argument_summary = validate_argument_vector(argument_rows[0])
            summary = validate_host_rows(rows, self.expected_graph)
            mapped = True
        except Exception as error:
            note(validation_errors, 'Host1 actual result mapping: ' + str(error))
        preserved = log_image is not None and not collection_errors
        state['host'] = {'log_image': log_image, 'marker_captures': captures, 'marker_count': marker_count,
            'argument_vector_count': argument_count, 'argument_summary': argument_summary,
            'summary': summary, 'log_signals': signals, 'collection_errors': collection_errors,
            'validation_errors': validation_errors, 'evidence_preserved': preserved,
            'actual_action_case_mapping_ok': mapped, 'independent_final_review_required': True,
            'callback_case_count': 1 if mapped else 0,
            'qualification': 'Actual action/case callbacks and producer/configured-consumer joins; '
                'exact retained XML, original stop/settlement and independent actual review still required. '
                'No APK/instrumentation/Android32/native/security-hardware or PVA009 closure.'}
        self.f.write(E / 'HOST-EVIDENCE.json', canonical(state['host']))
        self.host_preserved, self.host_mapping_ok = preserved, mapped
        if not preserved:
            self.note('Host evidence retention uncertain; no automatic cleanup')
        if not mapped:
            self.note('Host action/case mapping failed; retained/no automatic retry')

    def execute_host(self):
        state = self.state('android-host1')
        state['command_zero_exit'], state['host_ok'] = False, False
        state['xml'] = {'preserved': False, 'mapping_ok': False}
        try:
            self.check_inputs()
            self.binding_check()
            require(self.git_bound and self.settle() and not self.cancelled(), 'fresh original Git/SDK-isolated Host1 phase')
            self.verify_host_subject()
            self.verify_host_properties()
            root = R / 'workers/clipboard'
            require(set(os.listdir(self.f.directory(R / 'workers'))) == {'clipboard'}
                and set(os.listdir(self.f.directory(root))) == set(WORKER_CHILDREN)
                and all(not os.listdir(self.f.directory(root / name)) for name in WORKER_CHILDREN),
                'exact original private one-worker membership, all startup storage empty')
            self.resources(True)
            # One real invocation, no --dry-run/-x/rerun and no graph-discovery retry.
            state['command_zero_exit'] = self.command('host1', HOST_COMMAND, ENV, 3600, state)
        finally:
            if state['stop_required']:
                try:
                    self.check_inputs(stopping=True)
                    self.source_check('STOP', True)
                    state['stop_ok'] = self.command('host1-stop', STOP, ENV, 600, state, stopping=True)
                except Exception as error:
                    self.note('original Host1 wrapper stop incomplete/no retry: ' + str(error))
            try:
                state['settled'] = self.settle()
                if state['settled'] and state['stop_required']:
                    self.preserve_host_xml(state)
                    self.preserve_host(state)
            except Exception as error:
                self.note('Host1 evidence/settlement uncertainty: ' + str(error))
            state['host_ok'] = bool(state['command_zero_exit'] and state['stop_ok'] and state['settled']
                and self.host_mapping_ok and self.xml_mapping_ok and not self.cancelled())
            state['record_image'] = self.f.write(E / 'PHASE-android-host1.json', canonical(state))
        require(state['host_ok'], 'AndroidHost1 incomplete; no automatic retry')


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
    require(isinstance(HOST_BINDING, dict) and set(HOST_BINDING) == {
                'runtimeRoot', 'evidenceRoot', 'readRoots', 'offline', 'sourceManifestSha256', 'subjectSha256'}
            and HOST_BINDING['runtimeRoot'] == str(R) and HOST_BINDING['evidenceRoot'] == str(E)
            and HOST_BINDING['readRoots'] == [str(p) for p in READ_ROOT_ENVELOPE]
            and type(HOST_BINDING['offline']) is bool
            and HOST_BINDING['sourceManifestSha256'] == FROZEN[SOURCE]
            and HOST_BINDING['subjectSha256'] == HOST_SUBJECT_SHA,
            'UNBOUND exact new Host1/source/read-root/offline binding')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = AndroidHost01(files, dict(zip(('pid', 'mnt'), sys.argv[1:])))
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
        batch.execute_host()
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
        phase_contract = phase_names in (['git-sdk-isolation'], ['git-sdk-isolation', 'android-host1'])
        attempted = any(p['phase'] == 'android-host1' and p['stop_required'] for p in batch.phases)
        safe = (batch.namespace_ok and batch.before and batch.after and stops and settled and batch.evidence_ok
                and complete and phase_contract and batch.git_bound and batch.git_stable
                and batch.index_unchanged and not CANCEL
                and (not attempted or (batch.host_preserved and batch.xml_preserved)))
        collected = bool(safe and attempted and batch.host_mapping_ok and batch.xml_mapping_ok
            and phase_names == ['git-sdk-isolation', 'android-host1']
            and all(p.get('setup_ok') if p['phase'] == 'git-sdk-isolation' else p.get('host_ok')
                    for p in batch.phases))
        result = {'format': 'passvault-linux-android-host01-inner-v1', 'run_id': RUN_ID,
            'commit': COMMIT, 'tree': TREE, 'parent_namespaces': batch.parent,
            'source_before': batch.before, 'source_after': batch.after, 'source_capacity': SOURCE_CAPACITY,
            'all_required_stops_ok': stops, 'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'git_bindings_established': batch.git_bound, 'git_bindings_stable': batch.git_stable,
            'sealed_index_unchanged': batch.index_unchanged, 'host_evidence_preserved': batch.host_preserved,
            'actual_action_case_mapping_ok': batch.host_mapping_ok, 'xml_preserved': batch.xml_preserved,
            'xml_mapping_ok': batch.xml_mapping_ok, 'host_test_pass_collected': collected,
            'gradle_selector': HOST_TARGET, 'method_filter': HOST_FILTER, 'host_binding': HOST_BINDING,
            'independent_final_review_required': True, 'independent_semantic_acceptance': False,
            'sdk_readonly_view_established': batch.git_bound, 'sdk_readonly_view_stable': batch.git_stable,
            'declared_test_cases': 1, 'actual_pass_collected_cases': 1 if collected else 0,
            'errors': batch.errors, 'phases': batch.phases,
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'init_sha256': FROZEN[INIT],
                'accepted_graph_sha256': FROZEN[GRAPH_DATA], 'representation': 'RAW_GIT_BLOBS'},
            'resource_point_samples_not_ownership_or_cleanup_proof': batch.inventories,
            'incomplete_resource_samples': sum(not s['traversal_finished'] or bool(s['vanished_entries']
                or s['vanished_scan_directories']) for s in batch.inventories),
            'qualification': 'One injected Android host clipboard policy regression only; fresh compiler '
                'outputs and generated resource config joined to the effective Test consumer, exact '
                'unsuffixed XML and original exit0/stop required. No app APK, instrumentation, Android32, '
                'native or physical-device security evidence or PVA009 closure. Configuration/plugins '
                'are not sandboxed against arbitrary code or hostile root writers. Outer must prove '
                'original exit/no mounts/ownership and conserve reports before allowlisted cleanup.'}
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
