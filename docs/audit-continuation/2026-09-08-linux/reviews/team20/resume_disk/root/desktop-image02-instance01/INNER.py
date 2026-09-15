#!/usr/bin/python3
"""COMPLETE INERT DesktopInstalledImage02 PID1 afterimage; null contracts block use.
Reuses original namespace/resource/command/stop/settlement lifecycle. One image for
three selectors, accepted graph reused without rerun. This explicit successor uses
only a separately elected public synthetic nonpublishing support literal, never API
or private auth. Production validation is unchanged; no real support readiness.
Isolated public JLink routing and the synthetic literal election are explicit.
Opaque actions/store/resources/instance remain UNBOUND; no fallback or retry.
Private-output/settlement/cleanup safeguards remain unchanged.
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
R = Path('/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02')
E = B / 'runs/linux-desktop-installed-image02'
CHECKOUT = R / 'checkout'
SELF, INIT = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/desktop-image02-instance01/INNER.py'), Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/desktop-image02-instance01/INIT.gradle')
SOURCE = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/checkpoint23/source-prepare01/SOURCE.json')  # Retained adopted P23 SOURCE; no new capture.
JAVA, MOUNT = Path('/usr/lib/jvm/java-17-openjdk-amd64'), Path('/usr/bin/mount')
SDK, SDK_VIEW = Path('/opt/android-sdk'), R / 'sdk-readonly'
SDK_METADATA = tuple(SDK / p for p in ('platforms/android-37.0/source.properties',
    'platforms/android-37.0/package.xml', 'build-tools/36.0.0/source.properties',
    'build-tools/36.0.0/package.xml', 'licenses/android-sdk-license'))
SDK_OPERATION = 'READ_ONLY_EXISTING_SDK_CONFIGURATION_CONTEXT_NO_TASKS_COPY_INSTALL_LICENSE_CHANGES'
IMAGE_BINDING = {'runtimeRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02', 'evidenceRoot': '/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/runs/linux-desktop-installed-image02', 'readRoots': ['/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout', '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/gradle-home', '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/tmp', '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/konan', '/usr/lib/jvm/java-17-openjdk-amd64', '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/sdk-readonly'], 'offline': False, 'sourceManifestSha256': '1984fc3221b38fbd4e1c894b0a5b2cc13df8828dc7b5415aa50b0e887e47fccc', 'subjectSha256': '9263cf5e857f55afd4669aa524eaf36f37d3b5a18f4eca293da148bd55b3e709', 'acceptedGraph': {'recordSha256': 'a716a0c832bc5e13951501b26731ec9c5c79fee9e1b9d27b73b0414b29983dc2', 'reviewSha256': 'e7a61d7bab13c1a4692615b47581b7667db31e012acb6268df93cd94b93d7b7f', 'adoptionSha256': '411ef663174b7be3a679e14283452fa05317a4cbccb73bbadb0a2b9d4fd011ed', 'frameSha256': 'ad71dc70f128120451a837de45a78bbcd4b075c43c657d453207eac29de39a77', 'tasks': 292, 'dependencies': 567, 'finalizers': 1}, 'sourceStoreAffinity': {'store': '/root/projects/PassVault/passvault-publication-20260913-03/.git', 'commit': '59bee33096f538b9444f00236448d7fc0ea740b2', 'tree': '76a7dd46c0c1d69bae554c4eb02106da2ac1f6c6', 'sourceSha256': '1984fc3221b38fbd4e1c894b0a5b2cc13df8828dc7b5415aa50b0e887e47fccc', 'indexSha256': '36f9edc3b2f4e6eff79f66d78da1c86a92cb8156edd2b08e879b08f4c124909c', 'reviewSha256': 'ac5c84e320fca4cfba63a18fe7e75d6f7173f99d84d9ec95b2651e5154d2cec0'}, 'taskIo': {'policy': 'PER_TASK_SOURCE_PROJECT_BUILD_ROOTS_V1', 'projectDirectories': {':app-desktop': 'app-desktop', ':core:crypto': 'core/crypto', ':core:database': 'core/database', ':core:designsystem': 'core/designsystem', ':core:domain': 'core/domain', ':core:navigation': 'core/navigation', ':core:otp': 'core/otp', ':core:security': 'core/security', ':feature:backup': 'feature/backup', ':feature:credential': 'feature/credential', ':feature:generator': 'feature/generator', ':feature:health': 'feature/health', ':feature:onboarding': 'feature/onboarding', ':feature:settings': 'feature/settings', ':feature:unlock': 'feature/unlock', ':feature:vault': 'feature/vault', ':shared': 'shared'}}, 'classpathInputs': {'policy': 'PUBLIC_CLASSPATH_READ_ROOTS_NO_PATH_SNAPSHOT_V1', 'accessors': {'org.gradle.api.tasks.compile.JavaCompile_Decorated': 'JAVA_COMPILE_CLASSPATH', 'org.jetbrains.kotlin.gradle.tasks.KotlinCompile_Decorated': 'DECLARED_INPUT_ARCHIVES', 'com.google.devtools.ksp.gradle.KspAATask_Decorated': 'DECLARED_INPUT_ARCHIVES', 'org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask_Decorated': 'DECLARED_INPUT_ARCHIVES', 'org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask_Decorated': 'DECLARED_INPUT_ARCHIVES'}}, 'imageLayout': {'resourceSync': ':app-desktop:prepareDesktopAppResources', 'jlink': ':app-desktop:createRuntimeImage', 'jpackage': ':app-desktop:createReleaseDistributable', 'imageRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/binaries/main-release/app', 'resourceRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/generated/desktopAppResources', 'producerRoots': {':app-desktop:createRuntimeImage': {'outputRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/audit-installed-runtime02', 'workingRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/tmp/createRuntimeImage', 'argumentFile': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/tmp/createRuntimeImage.args.txt'}, ':app-desktop:createReleaseDistributable': {'outputRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/binaries/main-release/app', 'workingRoot': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/tmp/createReleaseDistributable', 'argumentFile': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/app-desktop/build/compose/tmp/createReleaseDistributable.args.txt'}}, 'opaqueActionInputsAndWritesReviewed': True}, 'roomFinalizer': {'producer': ':core:database:kspKotlinDesktop', 'finalizer': ':core:database:copyRoomSchemas', 'schemaDirectory': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/core/database/schemas', 'allowedNewSchemas': [], 'sourceBytesMustRemainExact': True}, 'linkPolicy': {'policy': 'OBSERVED_LEAF_DIRECT_REGULAR_TARGET_SAME_ROOT_V1', 'maximumRoots': 16, 'maximumLinks': 256, 'deviceSource': 'ORIGINAL_RUNTIME_DIRECTORY', 'regularFileDevice': 24}, 'privateInput': {'mode': 'SYNTHETIC_NONPUBLISHING', 'provenance': 'EXACT_REVIEWED_PUBLIC_LITERAL_NO_API_OR_DNS', 'value': 'desktop-contract-only@nonpublishing.passvault.audit', 'publicationAllowed': False, 'productionSupportVerified': False}, 'privateRetention': {'policy': 'PRIVATE_R_ONLY_SAFE_OUTCOMES_NO_OPAQUE_COPY', 'discardKnownDslAndProblemsAfterSafeProjection': True, 'unexpectedDiagnostic': 'HOLD_NO_EXPORT_NO_DELETE'}, 'resourceElection': {'launchGiB': 8, 'runningGiB': 5, 'launchRamFraction': 0.25, 'runningRamFraction': 0.2, 'gradleHeapMiB': 2048, 'workers': 1, 'sampleLogicalGiB': 3, 'imageSeconds': 3600, 'stopSeconds': 600, 'settlementSeconds': 120, 'workSeconds': 5250, 'outerSeconds': 6000}}
READ_ROOT_ENVELOPE = (CHECKOUT, R / 'gradle-home', R / 'tmp', R / 'konan', JAVA, SDK_VIEW)
PUBLICATION_GIT = Path('/root/projects/PassVault/passvault-publication-20260913-03/.git')  # Independently accepted current V/P23 raw-source/retained-index affinity; final instance required.
GIT_DIR, GIT_INDEX = R / 'git-metadata', R / 'git-index'
GIT_CONFIG, GIT_EXCLUDE = GIT_DIR / 'config', GIT_DIR / 'info/exclude'
# All publication/capacity bindings below require fresh root adoption and independent admission.
COMMIT = '59bee33096f538b9444f00236448d7fc0ea740b2'
TREE = '76a7dd46c0c1d69bae554c4eb02106da2ac1f6c6'
MEMBERS = 5121
MEMBER_LIMIT = 5121  # Exact proposed P23 ceiling; root adoption required; no widening.
SOURCE_BYTES = 2846405
OID_BYTES = 209961  # 41 bytes for each of all 5121 ordered rows.
SOURCE_CAPACITY = {'members': MEMBERS, 'member_limit': MEMBER_LIMIT, 'manifest_bytes': SOURCE_BYTES,
                   'oid_bytes': OID_BYTES, 'raw_stream_limit_bytes': 160 * 1024 * 1024}
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = 'FILE'  # Retained P23 expectation; fresh root confirmation required.
FROZEN = {SOURCE: '1984fc3221b38fbd4e1c894b0a5b2cc13df8828dc7b5415aa50b0e887e47fccc', INIT: '77a13e5d8933976315db0bebcd82d237d1c80e80dd3c0fdd1a1e2870184c16d8'}  # Literal cascade only; no instance admission.
INPUTS = (SELF, INIT, SOURCE, JAVA / 'bin/java', JAVA / 'release', MOUNT, GIT_INDEX) + SDK_METADATA
RUN_ID = 'linux-desktop-installed-image02'
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
IMAGE_TARGETS = [
    ':app-desktop:verifyDesktopInstalledLegalNotices',
    ':app-desktop:verifyDesktopInstalledRuntime',
    ':app-desktop:verifyDesktopInstalledBiometricBridge',
]
IMAGE_PRODUCER = ':app-desktop:createReleaseDistributable'
IMAGE_PREREQUISITES = [
    ':app-desktop:validateDesktopPublisherMetadata',
    ':app-desktop:prepareDesktopAppResources',
]
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8 -XX:-HeapDumpOnOutOfMemoryError -XX:-CreateCoredumpOnCrash', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
IMAGE_COMMAND = [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT),
    *IMAGE_TARGETS, '--no-scan', '-Ppassvault.audit.mode=desktop-installed-image-only',
    '-Pkotlin.native.toolchain.enabled=true', '-Pkotlin.native.distribution.downloadFromMaven=true',
    '-Pkonan.data.dir=' + str(R / 'konan')] + FLAGS
if IMAGE_BINDING is not None and IMAGE_BINDING['offline'] is True:
    IMAGE_COMMAND += ['--offline']  # Fixed policy, never a resolution fallback.
STOP = [str(CHECKOUT / 'gradlew'), '--stop'] + FLAGS
ENV = {'PATH': str(JAVA / 'bin') + ':/usr/bin:/bin', 'JAVA_HOME': str(JAVA), 'LANG': 'C.UTF-8',
       'LC_ALL': 'C.UTF-8', 'TZ': 'UTC', 'HOME': str(R / 'home'), 'GRADLE_USER_HOME': str(R / 'gradle-home'),
       'KONAN_DATA_DIR': str(R / 'konan'), 'ANDROID_USER_HOME': str(R / 'android-user'),
       'ANDROID_HOME': str(SDK_VIEW), 'ANDROID_SDK_ROOT': str(SDK_VIEW),
       'TMPDIR': str(R / 'tmp'), 'TMP': str(R / 'tmp'), 'TEMP': str(R / 'tmp'), 'SQLITE_TMPDIR': str(R / 'sqlite'),
       **{'XDG_' + k.upper() + '_HOME': str(R / ('xdg-' + k)) for k in ('cache', 'config', 'data', 'state')},
       'JAVA_TOOL_OPTIONS': '-Xmx512m -XX:-UsePerfData -XX:-HeapDumpOnOutOfMemoryError -XX:-CreateCoredumpOnCrash -Dfile.encoding=UTF-8 -Duser.home=' + str(R / 'home')
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



ACCEPTED_GRAPH = B / 'runs/linux-desktop-installed-graph01/reports/graph-init-01.json'
ACCEPTED_GRAPH_REVIEW = B / 'reviews/team20/resume_disk/platform_review/new36/DESKTOPGRAPH01-ACTUAL-REVIEW.json'
ACCEPTED_GRAPH_ADOPTION = B / 'reviews/team20/resume_disk/root/DESKTOPGRAPH01-ACTUAL-ADOPTION01.json'
ACCEPTED_GRAPH_IMAGES = {
    ACCEPTED_GRAPH: 'a716a0c832bc5e13951501b26731ec9c5c79fee9e1b9d27b73b0414b29983dc2',
    ACCEPTED_GRAPH_REVIEW: 'e7a61d7bab13c1a4692615b47581b7667db31e012acb6268df93cd94b93d7b7f',
    ACCEPTED_GRAPH_ADOPTION: '411ef663174b7be3a679e14283452fa05317a4cbccb73bbadb0a2b9d4fd011ed'}
GRAPH_FRAME_SHA = 'ad71dc70f128120451a837de45a78bbcd4b075c43c657d453207eac29de39a77'
GRAPH_SUBJECT_SHA = '9263cf5e857f55afd4669aa524eaf36f37d3b5a18f4eca293da148bd55b3e709'
IMAGE_SUBJECT_SHA = GRAPH_SUBJECT_SHA
IMAGE_PROPERTY_CONTRACT = 'EXACT_CLI_NATIVE_PLUS_FIXED_TOOLCHAIN_9_V1'
IMAGE_PROPERTY_SOURCE = {
    'path': 'gradle.properties', 'git_mode': '100644',
    'git_blob': 'a0f3d9ab2e45cae4e85feb468ba6d18b2aa9d57b', 'git_size': 1173,
    'raw_size': 1173, 'raw_sha256': '323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235',
    'checkout_size': 1173, 'checkout_sha256': '323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235'}
IMAGE_OUTCOMES = ('UNSTARTED', 'ENTERED_NO_OUTCOME', 'FAIL', 'NO_SOURCE', 'UP_TO_DATE',
                  'SKIPPED', 'TASK_COMPLETED', 'UNKNOWN')
RESOURCE_ELECTION = {'launchGiB': 8, 'runningGiB': 5, 'launchRamFraction': 0.25,
    'runningRamFraction': 0.20, 'gradleHeapMiB': 2048, 'workers': 1, 'sampleLogicalGiB': 3,
    'imageSeconds': 3600, 'stopSeconds': 600, 'settlementSeconds': 120, 'workSeconds': 5250,
    'outerSeconds': 6000}
PRIVATE_RETENTION = {'policy': 'PRIVATE_R_ONLY_SAFE_OUTCOMES_NO_OPAQUE_COPY',
    'discardKnownDslAndProblemsAfterSafeProjection': True,
    'unexpectedDiagnostic': 'HOLD_NO_EXPORT_NO_DELETE'}

TASK_IO_POLICY = {'policy': 'PER_TASK_SOURCE_PROJECT_BUILD_ROOTS_V1', 'projectDirectories': {':app-desktop': 'app-desktop', ':core:crypto': 'core/crypto', ':core:database': 'core/database', ':core:designsystem': 'core/designsystem', ':core:domain': 'core/domain', ':core:navigation': 'core/navigation', ':core:otp': 'core/otp', ':core:security': 'core/security', ':feature:backup': 'feature/backup', ':feature:credential': 'feature/credential', ':feature:generator': 'feature/generator', ':feature:health': 'feature/health', ':feature:onboarding': 'feature/onboarding', ':feature:settings': 'feature/settings', ':feature:unlock': 'feature/unlock', ':feature:vault': 'feature/vault', ':shared': 'shared'}}
CLASSPATH_POLICY = {'policy': 'PUBLIC_CLASSPATH_READ_ROOTS_NO_PATH_SNAPSHOT_V1', 'accessors': {'org.gradle.api.tasks.compile.JavaCompile_Decorated': 'JAVA_COMPILE_CLASSPATH', 'org.jetbrains.kotlin.gradle.tasks.KotlinCompile_Decorated': 'DECLARED_INPUT_ARCHIVES', 'com.google.devtools.ksp.gradle.KspAATask_Decorated': 'DECLARED_INPUT_ARCHIVES', 'org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask_Decorated': 'DECLARED_INPUT_ARCHIVES', 'org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask_Decorated': 'DECLARED_INPUT_ARCHIVES'}}
ROOM_POLICY = {'producer': ':core:database:kspKotlinDesktop', 'finalizer': ':core:database:copyRoomSchemas', 'schemaDirectory': '/root/projects/PassVault/audit-runtime-linux-desktop-installed-image02/checkout/core/database/schemas', 'allowedNewSchemas': [], 'sourceBytesMustRemainExact': True}
LINK_POLICY = {'policy': 'OBSERVED_LEAF_DIRECT_REGULAR_TARGET_SAME_ROOT_V1', 'maximumRoots': 16, 'maximumLinks': 256, 'deviceSource': 'ORIGINAL_RUNTIME_DIRECTORY'}



def graph_frame(rows):
    columns = ('path', 'type', 'enabled', 'isTest', 'isExec', 'isJavaExec', 'isArchive',
               'dependencies', 'finalizers', 'mustRunAfter', 'shouldRunAfter')
    value = 'PASSVAULT_DESKTOP_TASK_GRAPH_FRAME_V1\n'
    for row in rows:
        value += '\t'.join(row[k] if k in columns[:2] else ('1' if row[k] else '0')
            if k in columns[2:7] else ','.join(row[k]) for k in columns) + '\n'
    return hashlib.sha256(value.encode('ascii')).hexdigest()


def image_link_policy():
    policy, layout = IMAGE_BINDING['linkPolicy'], IMAGE_BINDING['imageLayout']
    require(type(policy) is dict and set(policy) == (set(LINK_POLICY) | {'regularFileDevice'})
            and {k: policy[k] for k in LINK_POLICY} == LINK_POLICY
            and type(policy['regularFileDevice']) is int and policy['regularFileDevice'] >= 0
            and type(policy['maximumRoots']) is int and type(policy['maximumLinks']) is int,
            'UNBOUND_IMAGE_LINK_POLICY')
    require(type(layout) is dict and set(layout) == {'resourceSync', 'jlink', 'jpackage', 'imageRoot',
            'resourceRoot', 'producerRoots', 'opaqueActionInputsAndWritesReviewed'}
            and layout['resourceSync'] == ':app-desktop:prepareDesktopAppResources'
            and layout['jlink'] == ':app-desktop:createRuntimeImage'
            and layout['jpackage'] == ':app-desktop:createReleaseDistributable'
            and layout['imageRoot'] == str(R / 'checkout/app-desktop/build/compose/binaries/main-release/app')
            and layout['resourceRoot'] == str(R / 'checkout/app-desktop/build/generated/desktopAppResources')
            and layout['opaqueActionInputsAndWritesReviewed'] is True, 'UNBOUND_SOURCE_IMAGE_LAYOUT')
    producers = layout['producerRoots']
    require(type(producers) is dict and set(producers) == {layout['jlink'], layout['jpackage']},
            'UNBOUND_PRECISE_IMAGE_PRODUCER_ROOTS')
    roots = []
    for task, spec in producers.items():
        require(type(spec) is dict and set(spec) == {'outputRoot', 'workingRoot', 'argumentFile'}, 'IMAGE_PRODUCER_ROOT_SCHEMA')
        working = R / 'checkout/app-desktop/build/compose/tmp' / task.rsplit(':', 1)[1]
        require(spec['workingRoot'] == str(working)
                and spec['argumentFile'] == str(working.with_name(working.name + '.args.txt')),
                'EXACT_COMPOSE_WORKING_ARGUMENT_BINDING')
        for key in ('outputRoot', 'workingRoot'):
            raw = spec[key]
            require(type(raw) is str and 0 < len(raw) <= 1024 and '\0' not in raw
                    and raw == str(Path(raw)) and Path(raw).is_absolute() and '..' not in Path(raw).parts
                    and Path(raw).is_relative_to(R / 'checkout/app-desktop/build')
                    and Path(raw) != R / 'checkout/app-desktop/build', 'IMAGE_PRECISE_ROOT_SCOPE')
            roots.append(tuple(Path(raw).relative_to(R).parts))
    require(producers[layout['jlink']]['outputRoot'] == str(R / 'checkout/app-desktop/build/compose/audit-installed-runtime02')
            and producers[layout['jpackage']]['outputRoot'] == layout['imageRoot']
            and 0 < len(roots) <= 16 and len(set(roots)) == len(roots)
            and not any(a != b and a[:len(b)] == b for a in roots for b in roots), 'IMAGE_ROOT_DOMAINS')
    return tuple(roots)


def check_image_contract(store, index_sha):
    value = IMAGE_BINDING
    require(type(value) is dict and set(value) == {
                'runtimeRoot', 'evidenceRoot', 'readRoots', 'offline', 'sourceManifestSha256',
                'subjectSha256', 'acceptedGraph', 'sourceStoreAffinity', 'taskIo', 'classpathInputs',
                'imageLayout', 'roomFinalizer', 'linkPolicy', 'privateInput', 'privateRetention', 'resourceElection'}
            and all(v is not None for v in value.values())
            and value['runtimeRoot'] == str(R) and value['evidenceRoot'] == str(E)
            and value['readRoots'] == [str(p) for p in READ_ROOT_ENVELOPE]
            and type(value['offline']) is bool and value['sourceManifestSha256'] == FROZEN[SOURCE]
            and value['subjectSha256'] == IMAGE_SUBJECT_SHA, 'UNBOUND_IMAGE_ACTION_CONTRACT')
    require(type(store) is Path or isinstance(store, Path), 'UNBOUND_POST_GRAPH_SOURCE_STORE')
    affinity = value['sourceStoreAffinity']
    require(type(affinity) is dict and set(affinity) == {'store', 'commit', 'tree', 'sourceSha256', 'indexSha256', 'reviewSha256'}
            and affinity['store'] == str(store) and affinity['commit'] == COMMIT and affinity['tree'] == TREE
            and affinity['sourceSha256'] == FROZEN[SOURCE] and affinity['indexSha256'] == index_sha
            and type(affinity['reviewSha256']) is str and re.fullmatch(r'[0-9a-f]{64}', affinity['reviewSha256']),
            'UNBOUND_POST_IOS_STORE_SOURCE_AFFINITY')
    accepted = value['acceptedGraph']
    require(type(accepted) is dict and accepted == {'recordSha256': ACCEPTED_GRAPH_IMAGES[ACCEPTED_GRAPH],
        'reviewSha256': ACCEPTED_GRAPH_IMAGES[ACCEPTED_GRAPH_REVIEW],
        'adoptionSha256': ACCEPTED_GRAPH_IMAGES[ACCEPTED_GRAPH_ADOPTION], 'frameSha256': GRAPH_FRAME_SHA,
        'tasks': 292, 'dependencies': 567, 'finalizers': 1}
            and all(type(accepted[k]) is int for k in ('tasks', 'dependencies', 'finalizers')), 'ACCEPTED_GRAPH_IDENTITY')
    require(type(value['resourceElection']) is dict and value['resourceElection'] == RESOURCE_ELECTION
            and all(type(value['resourceElection'][k]) is type(v) for k, v in RESOURCE_ELECTION.items())
            and type(value['privateRetention']) is dict and value['privateRetention'] == PRIVATE_RETENTION
            and value['privateRetention']['discardKnownDslAndProblemsAfterSafeProjection'] is True,
            'UNBOUND_IMAGE_RESOURCES_PRIVATE_RETENTION')
    require(SUPPORT_API_IMAGE is None and type(value['privateInput']) is dict
            and value['privateInput'] == SYNTHETIC_SUPPORT_INPUT
            and value['privateInput']['publicationAllowed'] is False
            and value['privateInput']['productionSupportVerified'] is False,
            'UNBOUND_EXPLICIT_SYNTHETIC_NONPUBLISHING_INPUT')
    require(type(value['taskIo']) is dict and value['taskIo'] == TASK_IO_POLICY
            and type(value['classpathInputs']) is dict and value['classpathInputs'] == CLASSPATH_POLICY
            and type(value['roomFinalizer']) is dict and value['roomFinalizer'] == ROOM_POLICY
            and value['roomFinalizer']['sourceBytesMustRemainExact'] is True,
            'UNBOUND_SOURCE_SCOPE_CLASSPATH_ROOM_POLICY')
    image_link_policy()  # Precise source-reviewed producer roots, never live arbitrary-root adoption.


def validate_image_outcome(raw):
    try:
        row = json.loads(raw, object_pairs_hook=_support_pairs, parse_constant=_support_nonfinite)
        require(type(row) is dict and set(row) == {'format', 'runId', 'sourceManifestSha256', 'subjectSha256',
                'graphFrameSha256', 'graphAccepted', 'readyCallbacks', 'finishCallbacks', 'enteredTasks',
                'completedTasks', 'buildFailed', 'nativeDenied', 'selectors', 'observedApplicationTestCases'}, 'IMAGE_OUTCOME_SCHEMA')
        require(row['format'] == 'passvault-desktop-installed-image-outcome-v1' and row['runId'] == RUN_ID
                and row['sourceManifestSha256'] == FROZEN[SOURCE] and row['subjectSha256'] == IMAGE_SUBJECT_SHA
                and row['graphFrameSha256'] == GRAPH_FRAME_SHA, 'IMAGE_OUTCOME_IDENTITY')
        require(all(type(row[k]) is bool for k in ('graphAccepted', 'buildFailed', 'nativeDenied'))
                and all(type(row[k]) is int for k in ('readyCallbacks', 'finishCallbacks', 'enteredTasks',
                    'completedTasks', 'observedApplicationTestCases')) and 0 <= row['readyCallbacks'] <= 1
                and row['finishCallbacks'] == 1 and 0 <= row['completedTasks'] <= row['enteredTasks'] <= 292
                and row['observedApplicationTestCases'] == 0 and type(row['selectors']) is list
                and len(row['selectors']) == 3, 'IMAGE_OUTCOME_COUNTS')
        for task, path in zip(row['selectors'], IMAGE_TARGETS):
            require(type(task) is dict and set(task) == {'path', 'outcome'} and task['path'] == path
                    and type(task['outcome']) is str and task['outcome'] in IMAGE_OUTCOMES, 'IMAGE_SELECTOR_OUTCOME')
        return row  # Exact bounded safe schema ONLY; never raw file bytes or their hash.
    except Exception:
        raise RuntimeError('IMAGE_EVIDENCE_UNAVAILABLE') from None


SYNTHETIC_SUPPORT_INPUT = {'mode': 'SYNTHETIC_NONPUBLISHING', 'provenance': 'EXACT_REVIEWED_PUBLIC_LITERAL_NO_API_OR_DNS', 'value': 'desktop-contract-only@nonpublishing.passvault.audit', 'publicationAllowed': False, 'productionSupportVerified': False}
# Legacy private API helpers are unreachable in this explicit synthetic successor.
SUPPORT_ENDPOINT = 'repos/Apdelrahman1911/passvault/actions/variables/SUPPORT_EMAIL'
SUPPORT_API_IMAGE = None       # Exact independently admitted gh image, not PATH search.
SUPPORT_INGRESS_ADMISSION = None  # Set to public admission descriptor only after intake; never token/value.
SUPPORT_RESPONSE_CAP = 65536    # Proposed whole-response cap; requires admission.


def support_api_argv():
    require(SUPPORT_INGRESS_ADMISSION is not None
            and isinstance(SUPPORT_API_IMAGE, Path) and SUPPORT_API_IMAGE.is_absolute(),
            'UNBOUND_SUPPORT_INGRESS')
    return [str(SUPPORT_API_IMAGE), 'api', '--hostname', 'github.com', '--method', 'GET',
            '-H', 'Accept: application/vnd.github+json',
            '-H', 'X-GitHub-Api-Version: 2022-11-28', SUPPORT_ENDPOINT]
    # No shell, --jq/-q, --include, --verbose, pagination, retries, or fallback.


def _support_pairs(pairs):
    row = {}
    for key, value in pairs:
        if key in row:
            raise ValueError('SUPPORT_INPUT_UNAVAILABLE')
        row[key] = value
    return row


def _support_nonfinite(_token):
    raise ValueError('SUPPORT_INPUT_UNAVAILABLE')


def discard_support_response(raw):
    # Best-effort mutable-buffer clearing, NOT secure erasure of Python copies.
    if type(raw) is bytearray:
        raw[:] = b'\0' * len(raw)
        raw.clear()


def support_value_from_response(raw):
    # Call ONLY after the original bounded child completed with exit 0 and EOF.
    # No availability query before/after this one retrieval and no body logging.
    try:
        if type(raw) is not bytearray or not 0 < len(raw) <= SUPPORT_RESPONSE_CAP:
            raise ValueError('SUPPORT_INPUT_UNAVAILABLE')
        row = json.loads(raw.decode('utf-8', errors='strict'),
                         object_pairs_hook=_support_pairs, parse_constant=_support_nonfinite)
        if type(row) is not dict or row.get('name') != 'SUPPORT_EMAIL':
            raise ValueError('SUPPORT_INPUT_UNAVAILABLE')
        value = row.get('value')
        if type(value) is not str or not value or value.isspace() or '\0' in value:
            raise ValueError('SUPPORT_INPUT_UNAVAILABLE')
        value.encode('utf-8', errors='strict')  # Reject unrepresentable surrogates, do not transform.
        return value  # Deliberately no strip(), splitlines(), normalization, or fake default.
    except Exception:
        raise RuntimeError('SUPPORT_INPUT_UNAVAILABLE') from None
    finally:
        discard_support_response(raw)
    # Empty/whitespace is unavailable: product blank-env handling would fall back
    # to the publisher file. Nonblank invalid syntax remains unchanged for the
    # EXISTING product validator; no ownership/deliverability inference is made.


def desktop_image_environment(value):
    require(type(value) is str and value and not value.isspace()
            and '\0' not in value and 'SUPPORT_EMAIL' not in ENV,
            'SUPPORT_INPUT_UNAVAILABLE')
    child_env = dict(ENV)  # Only the fixed donor allowlist, never API env/os.environ.
    child_env['SUPPORT_EMAIL'] = value
    return child_env


class ImageFailureProjection:
    LIMIT = 4096
    MARKERS = {
        b"Could not compile initialization script": "INIT_COMPILATION_REPORTED",
        b"Could not compile build file": "BUILD_SCRIPT_COMPILATION_REPORTED",
        b"Dependency verification failed": "DEPENDENCY_VERIFICATION_REPORTED",
        b"Could not resolve all files": "DEPENDENCY_RESOLUTION_REPORTED",
        b"BUILD FAILED": "BUILD_FAILURE_REPORTED",
        b"OutOfMemoryError": "JVM_OOM_REPORTED",
    }

    def __init__(self, admitted_tasks):
        if (type(admitted_tasks) is not tuple or not 0 < len(admitted_tasks) <= 292
                or len(set(admitted_tasks)) != len(admitted_tasks)
                or any(type(t) is not str or not t.startswith(":")
                       or len(t) > 256 or not t.isascii()
                       or any(c not in ":-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" for c in t)
                       for t in admitted_tasks)):
            raise ValueError("INVALID_PUBLIC_TASK_SET")
        self.tasks = admitted_tasks
        self.failure_lines = {b"> Task " + t.encode("ascii") + b" FAILED": t for t in admitted_tasks}
        self.pending = bytearray()
        self.discard_line = False
        self.flags = set()
        self.failed_tasks = set()
        self.finished = False

    def feed(self, data):
        if self.finished or type(data) is not bytes:
            raise ValueError("INVALID_PROJECTION_FEED")
        # Per-byte bounded state, including lines split across arbitrary pipe reads.
        for byte in data:
            if byte == 10:
                if not self.discard_line:
                    self._line(bytes(self.pending).removesuffix(b"\r"))
                self.pending.clear()
                self.discard_line = False
            elif not self.discard_line:
                if len(self.pending) == self.LIMIT:
                    self.pending.clear()
                    self.discard_line = True
                else:
                    self.pending.append(byte)

    def _line(self, line):
        for marker, flag in self.MARKERS.items():
            if marker in line:
                self.flags.add(flag)
        task = self.failure_lines.get(line)
        if task is not None:
            self.failed_tasks.add(task)

    def finish(self):
        if not self.finished:
            if not self.discard_line:
                self._line(bytes(self.pending).removesuffix(b"\r"))
            self.pending.clear()
            self.discard_line = False
            self.finished = True
        return {"format": "passvault-image-failure-projection-v1",
                "reported_flags": sorted(self.flags),
                "reported_failed_tasks": [t for t in self.tasks if t in self.failed_tasks],
                "classification_only_not_causal_or_task_success_evidence": True}


class DesktopPrivateOutput:
    """Two fixed sinks for the EXISTING command loop, not a subprocess launcher.

    support: stdout bytes in memory; stderr must be DEVNULL, never merged.
    image/stop: merged diagnostics in original private R/tmp, never E.
    Neither mode computes/returns a content hash or exports a content length.
    """
    def __init__(self, label, files):
        require(label in ('desktop-support-read', 'desktop-installed-image',
                          'desktop-installed-image-stop'), 'PRIVATE_OUTPUT_MODE')
        self.label, self.files = label, files
        self.raw, self.fd, self.parent, self.opened, self.count = bytearray(), None, None, None, 0
        if label != 'desktop-support-read':
            self.parent = files.directory(R / 'tmp')
            self.name = label + '.private.log'
            self.fd = os.open(self.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL
                              | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=self.parent)
            try:
                self.opened = os.fstat(self.fd)
                os.fsync(self.parent)
            except Exception:
                fd, self.fd = self.fd, None
                os.close(fd)  # Never strand a constructor-owned descriptor.
                raise RuntimeError('PRIVATE_OUTPUT_OPEN') from None

    def consume(self, data):
        if self.label == 'desktop-support-read':
            require(len(self.raw) + len(data) <= SUPPORT_RESPONSE_CAP, 'PRIVATE_OUTPUT_LIMIT')
            self.raw.extend(data)
        else:
            require(self.count + len(data) <= 4 * MIB, 'PRIVATE_OUTPUT_LIMIT')
            if hasattr(self, 'projection'):
                self.projection.feed(data)
            view = memoryview(data)
            while view:
                count = os.write(self.fd, view)
                require(count > 0, 'PRIVATE_OUTPUT_WRITE')
                self.count += count
                view = view[count:]

    def close(self):
        if self.fd is None:
            return  # support raw is decoded/cleared by phase owner AFTER command outcome.
        fd, self.fd = self.fd, None
        try:
            os.fsync(fd)
            current = os.fstat(fd)
            named = os.stat(self.name, dir_fd=self.parent, follow_symlinks=False)
            require(all(getattr(current, 'st_' + key) == getattr(self.opened, 'st_' + key)
                        for key in ('dev', 'ino', 'uid', 'mode', 'nlink'))
                    and current.st_uid == 0 and current.st_nlink == 1
                    and current.st_mode == stat.S_IFREG | 0o600
                    and current.st_size == self.count and pin(current) == pin(named),
                    'PRIVATE_OUTPUT_IDENTITY')
            os.fsync(self.parent)
        finally:
            os.close(fd)
        # Do NOT call Files.finish/read: their public evidence image has SHA/size.
        # Original outer no-follow inventory/disposal later handles the private file.



def _link_destination(parts, target, root):
    # Live inventories check leaf/lexical scope only. Settled snapshot and the
    # init's pre-consumer guard check EVERY raw intermediate before '..'.
    require(type(target) is str and 0 < len(target) <= 1024 and '\0' not in target
            and not target.startswith('/'), 'IMAGE_LINK_TARGET')
    destination = list(parts[:-1])
    for component in target.split('/'):
        if component in ('', '.'):
            continue
        if component == '..':
            require(len(destination) > len(root), 'IMAGE_LINK_ESCAPE')
            destination.pop()
        else:
            destination.append(component)
        require(tuple(destination[:len(root)]) == root, 'IMAGE_LINK_CROSSES_ROOTS')
    return tuple(destination)


def admitted_image_link(parent_fd, parts, observed, runtime_device):
    roots = image_link_policy()
    require(type(parts) is tuple and 0 < len('/'.join(parts)) <= 1024
            and all(type(c) is str and c not in ('', '.', '..') and '/' not in c and '\0' not in c for c in parts),
            'IMAGE_LINK_PATH_BOUND')
    owned = [root for root in roots if len(parts) > len(root) and parts[:len(root)] == root]
    require(len(owned) == 1 and type(runtime_device) is int and runtime_device >= 0
            and stat.S_ISLNK(observed.st_mode) and observed.st_uid == 0
            and observed.st_nlink == 1 and observed.st_dev == runtime_device, 'IMAGE_LINK_ORIGIN_IDENTITY')
    target = os.readlink(parts[-1], dir_fd=parent_fd)
    _link_destination(parts, target, owned[0])
    current = os.stat(parts[-1], dir_fd=parent_fd, follow_symlinks=False)
    require(pin(current) == pin(observed), 'IMAGE_LINK_CHANGED_DURING_READ')
    return {**pin(current), 'readlink': target}  # Private in-memory leaf pin, NEVER evidence.


FROZEN.update(ACCEPTED_GRAPH_IMAGES)
INPUTS += tuple(ACCEPTED_GRAPH_IMAGES) + (() if SUPPORT_API_IMAGE is None else (SUPPORT_API_IMAGE,))


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

    def read_private(self, path, cap):
        # Same no-follow stable read as read(), but NO content hash/image descriptor.
        path = Path(path)
        parent = self.directory(path.parent)
        original = pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
        require(original['mode'] == stat.S_IFREG | 0o600 and original['uid'] == os.getuid()
                and original['nlink'] == 1 and 0 <= original['bytes'] <= cap, 'PRIVATE_FILE_BOUND')
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        with os.fdopen(fd, 'rb') as stream:
            require(pin(os.fstat(stream.fileno())) == original, 'PRIVATE_FILE_IDENTITY')
            data = stream.read(cap + 1)
            require(len(data) == original['bytes'] and pin(os.fstat(stream.fileno())) == original
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'PRIVATE_FILE_CHANGED')
        return data

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


class DesktopInstalledImage02:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs = [], [], {}
        self.source = self.outer = self.initial = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.git_bound = self.git_stable = self.index_unchanged = False
        self.image_preserved = self.image_succeeded = False
        self.image_callback_validated = False
        self.auth_token, self.private_started = None, False
        self.private_io_ok = True  # Monotone failure latch, not a product-outcome gate.
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
        require(value['format'] == 'passvault-linux-desktop-installed-image02-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == MEMBERS
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent
                and value['source_capacity'] == SOURCE_CAPACITY,
                'fixed fresh DesktopInstalledImage02 intake; not independent execution admission')
        device = value['device_model']
        require(type(device) is dict and type(device.get('regular_file_device')) is int
                and device['regular_file_device'] == IMAGE_BINDING['linkPolicy']['regularFileDevice'],
                'IMAGE_ORIGINAL_REGULAR_FILE_DEVICE')
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
                                                 128 * MIB if path == SUPPORT_API_IMAGE else
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
        require(value['image_binding'] == IMAGE_BINDING and sdk['root'] == str(SDK)
                and sdk['metadata_paths'] == [str(p) for p in SDK_METADATA]
                and sdk['operation'] == SDK_OPERATION, 'exact reviewed graph/read-only existing SDK scope')
        check_image_contract(PUBLICATION_GIT, binding['index_sha256'])
        adopted = json.loads(captured[ACCEPTED_GRAPH])
        require(adopted['taskCount'] == 292 and graph_frame(adopted['tasks']) == GRAPH_FRAME_SHA
                and set(IMAGE_BINDING['taskIo']['projectDirectories'])
                    == {r['path'].rsplit(':', 1)[0] for r in adopted['tasks']}, 'ACCEPTED_GRAPH_SOURCE_PROJECTS')
        self.auth_token = None
        require(not any(k in os.environ for k in ('GH_TOKEN', 'GITHUB_TOKEN', 'SUPPORT_EMAIL', 'MACOS_SIGN')),
                'SYNTHETIC_INPUT_REQUIRES_NO_PRIVATE_ENV')
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
                                    else 128 * MIB if path == SUPPORT_API_IMAGE else 32 * MIB)[1] == image, 'admitted input drift')
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
                      'vanished_entries': 0, 'vanished_scan_directories': 0, 'traversal_finished': False, 'symlinks': 0}
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
                    if stat.S_ISLNK(value.st_mode):
                        admitted_image_link(self.f.directory(path.parent), tuple(path.relative_to(R).parts), value,
                                            self.f.expected[str(R)]['dev'])
                        sample['symlinks'] += 1
                        require(sample['symlinks'] <= 256, 'runtime image-link sample bound')
                        sample['logical_bytes'] += value.st_size  # Own leaf only, never target bytes.
                    else:
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

    def private_output(self, label):
        # Constructor failure may precede a command record, so it must survive to cleanup_safe.
        try:
            return DesktopPrivateOutput(label, self.f)
        except BaseException:
            self.private_io_ok = False
            raise

    def command(self, label, argv, env, seconds, phase, stopping=False, gradle=True, private_output=None):
        private = private_output is not None
        support = private and label == 'desktop-support-read'
        record = {'label': label, 'started': False, 'exit': None, 'complete': False, 'errors': []}
        record.update({'argv_policy': label, 'private_output_closed': False} if private else {'argv': argv})
        phase['commands'].append(record)
        fd = pidfd = child = None
        digest, count, abort, killed = None if private else hashlib.sha256(), 0, None, False
        path = None if private else E / 'logs' / (label + '.log')
        try:
            if private:
                require(type(private_output) is DesktopPrivateOutput and private_output.label == label,
                        'PRIVATE_OUTPUT_MODE')
                expected = support_api_argv() if support else IMAGE_COMMAND if label == 'desktop-installed-image' else STOP
                require(label in ('desktop-support-read', 'desktop-installed-image', 'desktop-installed-image-stop')
                        and argv == expected and (not support or (not gradle and not stopping and seconds == 22)),
                        'PRIVATE_COMMAND_CONTRACT')
                self.private_started = True
            else:
                fd = self.f.new(path)
            if stopping:
                require(not phase['stop_attempted'], 'stop already attempted; no retry')
                phase['stop_attempted'] = True
            else:
                require(not self.cancelled(), 'cancel before build intent')
                if gradle:
                    phase['stop_required'] = True
            intent = {'cwd': str(CHECKOUT), 'seconds': seconds, 'stop_required': phase['stop_required']}
            intent.update({'argv_policy': label, 'environment_policy': 'PRIVATE_EXPLICIT_ALLOWLIST_NO_VALUE_MANIFEST'}
                          if private else {'argv': argv, 'environment_sha256': sha(canonical(env))})
            self.f.write(E / (label + '-INTENT.json'), canonical(intent))
            require(stopping or not self.cancelled(), 'cancel before build launch; original stop retained')
            child = subprocess.Popen(argv, cwd=CHECKOUT, env=env, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                     stderr=subprocess.DEVNULL if support else subprocess.STDOUT, close_fds=True)
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
                        elif private:
                            try:
                                private_output.consume(data)
                            except Exception:
                                self.private_io_ok = False
                                note(record['errors'], 'PRIVATE_OUTPUT_FAILED')
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
                        note(record['errors'], 'PRIVATE_RESOURCE_FAILED' if private else error)
                    cancelled = self.cancelled()
                    now = time.monotonic()  # Resource/syscall time is not subtracted from cooperative deadlines.
                    # Compile900/original-stop600 include their 20s TERM/KILL tail; bind primitive unchanged.
                    expired = now - started >= seconds - (2 if support else 20 if gradle else 0)
                    expired = expired or (not stopping and now - self.started >= 6000)
                    if expired or record['errors'] or (cancelled and not stopping):
                        if abort is None:
                            abort = now
                            note(record['errors'], 'command timeout/cancel/resource/output failure')
                            self.signal_child(pidfd, signal.SIGTERM)
                        elif now - abort >= (1 if support else 10) and not killed:
                            self.signal_child(pidfd, signal.SIGKILL)
                            killed = True
                        elif now - abort >= (2 if support else 20):
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
            note(record['errors'], 'PRIVATE_COMMAND_FAILED' if private else type(error).__name__ + ': ' + str(error))
        finally:
            try:
                if pidfd is not None and not record['complete'] and not killed:
                    self.signal_child(pidfd, signal.SIGKILL)
                if child is not None:
                    record['exit'] = child.poll()
                    child.stdout.close()
            except Exception as error:
                note(record['errors'], 'PRIVATE_CHILD_FINALIZATION_FAILED' if private else 'direct-child finalization: ' + str(error))
            if private:
                try:
                    private_output.close()
                    record['private_output_closed'] = True
                except Exception:
                    self.private_io_ok = False
                    note(record['errors'], 'PRIVATE_OUTPUT_CLOSE_FAILED')
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
                        note(record['errors'], 'PRIVATE_DESCRIPTOR_CLOSE_FAILED' if private else 'original descriptor close: ' + str(error))
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
                'qualification': 'Private RO SDK configuration alias; not SDK/tool/task authority, full provenance, resolver success, '
                    'new license acceptance, host-wide write isolation or immutable snapshot against outside writers'}))
            state['setup_ok'] = True
        finally:
            state['settled'] = self.settle()
            state['record_image'] = self.f.write(E / 'PHASE-git-sdk-isolation.json', canonical(state))
        require(state.get('setup_ok') and state['settled'] and not self.cancelled(), 'Git/SDK isolation incomplete')



    def verify_desktop_installed_graph_subject(self):
        # Passive source affinity, NOT compiler input visitation or a test outcome.
        relative = 'app-desktop/build.gradle.kts'
        rows = [r for r in self.source['files'] if r['path'] == relative]
        require(len(rows) == 1, 'exact Desktop installed-image subject membership in final source manifest')
        data, image = self.f.read(CHECKOUT / relative, MIB)
        require(len(data) == 63918 and sha(data) == IMAGE_SUBJECT_SHA
                and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == rows[0]['git_blob'],
                'Desktop installed-image subject SHA256/final raw Git-blob affinity')
        self.f.write(E / 'DESKTOP-SUBJECT.json', canonical({'relative_source': relative, 'image': image,
                     'sha256': IMAGE_SUBJECT_SHA, 'source_manifest_sha256': IMAGE_BINDING['sourceManifestSha256'],
                     'not_compiler_visitation_or_test_evidence': True}))



    def verify_desktop_installed_graph_properties(self):
        # Exact complete checked-in file affinity, not a claim about Gradle merge behavior.
        relative = IMAGE_PROPERTY_SOURCE['path']
        rows = [r for r in self.source['files'] if r['path'] == relative]
        # Keep the whole SOURCE row; its observed_checkout_pin is historical, not runtime authority.
        require(len(rows) == 1 and set(rows[0]) == (set(IMAGE_PROPERTY_SOURCE) | {'observed_checkout_pin'})
                and {key: rows[0][key] for key in IMAGE_PROPERTY_SOURCE} == IMAGE_PROPERTY_SOURCE,
                'exact checked-property source schema/raw-affinity projection')
        data, image = self.f.read(CHECKOUT / relative, IMAGE_PROPERTY_SOURCE['raw_size'])
        require(len(data) == IMAGE_PROPERTY_SOURCE['raw_size'] and sha(data) == IMAGE_PROPERTY_SOURCE['raw_sha256']
                and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
                    == IMAGE_PROPERTY_SOURCE['git_blob'], 'checked-property raw SHA256/Git-blob affinity')
        self.f.write(E / 'PROPERTY-CONTRACT-SOURCE.json', canonical({'relative_source': relative, 'image': image,
            'source_record': rows[0], 'source_manifest_sha256': IMAGE_BINDING['sourceManifestSha256'],
            'contract': IMAGE_PROPERTY_CONTRACT,
            'qualification': 'Exact raw-affinity projection; observed_checkout_pin is historical whole-SOURCE-bound metadata, '
                'not current runtime identity. No observed Gradle merge/cause or task authority.'}))


    def preserve_desktop_installed_image(self, state):
        attempted = state['stop_required']
        outcome = None
        callback_status = 'NOT_ATTEMPTED'
        if attempted:
            try:
                outcome = validate_image_outcome(self.f.read_private(R / 'tmp/desktop-image-outcome.json', 65536))
                callback_status = 'VALIDATED'
            except Exception:
                # No raw exception/output/path/hash export and no fallback success.
                # This prospective run's failed observation must still be retained.
                callback_status = 'UNAVAILABLE_OR_INVALID'
                outcome = {'format': 'passvault-desktop-image-callback-unavailable-v1',
                           'selectors': [{'path': p, 'outcome': 'UNKNOWN'} for p in IMAGE_TARGETS],
                           'observedApplicationTestCases': 0}
        else:
            outcome = {'format': 'passvault-desktop-installed-image-not-started-v1',
                       'selectors': [{'path': p, 'outcome': 'UNSTARTED'} for p in IMAGE_TARGETS],
                       'observedApplicationTestCases': 0}
        self.image_callback_validated = callback_status == 'VALIDATED'
        self.image_succeeded = bool(attempted and callback_status == 'VALIDATED' and state['image_zero_exit'] and outcome.get('graphAccepted')
            and not outcome.get('buildFailed') and not outcome.get('nativeDenied')
            and outcome.get('readyCallbacks') == 1 and outcome.get('finishCallbacks') == 1
            and outcome.get('enteredTasks') == outcome.get('completedTasks')
            and all(t['outcome'] == 'TASK_COMPLETED' for t in outcome['selectors']))
        safe = {'format': 'passvault-desktop-installed-image-safe-evidence-v1', 'run_id': RUN_ID,
            'support_status': state['support_status'], 'image_attempted': attempted,
            'support_input_mode': 'SYNTHETIC_NONPUBLISHING', 'support_contact_verified': False,
            'artifact_publication_allowed': False,
            'image_succeeded_at_callback_and_process': self.image_succeeded, 'outcome': outcome,
            'callback_status': callback_status,
            'failure_projection': getattr(self, 'image_failure_projection', None),
            'raw_private_output_exported': False, 'private_output_hashes_exported': False,
            'test_cases': 0, 'independent_semantic_acceptance': False,
            'qualification': 'One image/three verification-task outcomes only. No legal clearance, app/JNI/native execution, '
                'package/Store/signing or test credit. Public synthetic metadata only; no API/DNS, actual contact, '
                'ownership/deliverability or production support/legal readiness. No artifact publication; '
                'the original guarded scratch-output disposal policy is retained.'}
        self.f.write(E / 'IMAGE-EVIDENCE.json', canonical(safe))
        self.image_preserved = True

    def run_desktop_installed_image(self):
        state = self.state('desktop-installed-image')
        state.update(support_status='SYNTHETIC_NONPUBLISHING_UNATTEMPTED', image_zero_exit=False, image_ok=False)
        api_env = image_env = value = None
        sink = None
        try:
            self.check_inputs()
            self.binding_check()
            require(self.git_bound and self.settle() and not self.cancelled(), 'IMAGE_ISOLATION_REQUIRED')
            self.verify_desktop_installed_graph_subject()
            self.verify_desktop_installed_graph_properties()
            # This source-level Room affinity does not authorize edits to existing schemas.
            rows = [r for r in self.source['files'] if r['path'] == 'core/database/build.gradle.kts']
            data, _image = self.f.read(CHECKOUT / 'core/database/build.gradle.kts', MIB)
            require(len(rows) == 1 and len(data) == 2079
                    and sha(data) == 'e83ca3a79cafd99f25d9d5a34c4db15f30ba1784a2e7613cfb44d9cbd7ffdbbc',
                    'ROOM_SOURCE_AFFINITY')
            self.resources(True)
            # Exact public synthetic election was checked at intake; no API/auth/value retrieval.
            value = IMAGE_BINDING['privateInput']['value']
            state['support_status'] = 'SYNTHETIC_NONPUBLISHING_UNVALIDATED'
            image_env = desktop_image_environment(value)
            value = None
            self.resources(True)
            sink = self.private_output('desktop-installed-image')
            sink.projection = ImageFailureProjection(tuple(IMAGE_TARGETS))
            state['image_zero_exit'] = self.command('desktop-installed-image', IMAGE_COMMAND, image_env,
                                                   3600, state, private_output=sink)
        except Exception:
            self.note('PRIVATE_IMAGE_PHASE_NONACCEPTING')
        finally:
            # Installed before API/build intent. No stop input, fallback, second image or retry.
            self.auth_token = value = None
            if api_env is not None:
                api_env.pop('GH_TOKEN', None)
            if image_env is not None:
                image_env.pop('SUPPORT_EMAIL', None)
            if sink is not None:
                discard_support_response(sink.raw)
                self.image_failure_projection = sink.projection.finish() if hasattr(sink, 'projection') else None
                try:
                    sink.close()
                except Exception:
                    self.private_io_ok = False
                    self.note('PRIVATE_SINK_CLOSE_HOLD')
            if state['stop_required']:
                try:
                    self.check_inputs(stopping=True)
                    self.source_check('STOP', True)
                    stop_sink = self.private_output('desktop-installed-image-stop')
                    state['stop_ok'] = self.command('desktop-installed-image-stop', STOP, ENV, 600,
                                                   state, stopping=True, private_output=stop_sink)
                except Exception:
                    self.note('ORIGINAL_IMAGE_STOP_INCOMPLETE_NO_RETRY')
            try:
                state['settled'] = self.settle()
                if state['settled']:
                    self.preserve_desktop_installed_image(state)
            except Exception:
                self.note('IMAGE_EVIDENCE_OR_SETTLEMENT_HOLD')
            state['image_ok'] = bool(state['stop_ok'] and state['settled'] and self.image_succeeded
                                     and not self.cancelled())
            state['record_image'] = self.f.write(E / 'PHASE-desktop-installed-image.json', canonical(state))
        require(state['image_ok'], 'DESKTOP_IMAGE_NONACCEPTING_NO_RETRY')


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
    check_image_contract(PUBLICATION_GIT, IMAGE_BINDING.get('sourceStoreAffinity', {}).get('indexSha256')
                         if type(IMAGE_BINDING.get('sourceStoreAffinity')) is dict else None)
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = DesktopInstalledImage02(files, dict(zip(('pid', 'mnt'), sys.argv[1:])))
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
        batch.run_desktop_installed_image()
    except Exception as error:
        batch.note('PRIVATE_CONTROLLER_HOLD:' + type(error).__name__ if batch.private_started
                   else type(error).__name__ + ': ' + str(error))
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
            batch.note('PRIVATE_FINAL_SAFETY_HOLD:' + type(error).__name__ if batch.private_started
                       else 'final source/Git/evidence/namespace uncertainty: ' + str(error))
        stops = all(not p['stop_required'] or (p['stop_attempted'] and p['stop_ok']) for p in batch.phases)
        complete = all('record_image' in p and all(c['complete'] and not c['errors']
            and (c.get('private_output_closed') is True if 'argv_policy' in c else 'log' in c)
            for c in p['commands']) for p in batch.phases)
        phase_names = [p['phase'] for p in batch.phases]
        phase_contract = phase_names in (['git-sdk-isolation'], ['git-sdk-isolation', 'desktop-installed-image'])
        attempted = any(p['phase'] == 'desktop-installed-image' and p['stop_required'] for p in batch.phases)
        private_phase = 'desktop-installed-image' in phase_names
        safe = (batch.namespace_ok and batch.before and batch.after and stops and settled and batch.evidence_ok
                and batch.private_io_ok
                and complete and phase_contract and batch.git_bound and batch.git_stable
                and batch.index_unchanged and not CANCEL and (not private_phase or batch.image_preserved)
                and (not attempted or batch.image_callback_validated))
        collected = bool(safe and attempted and batch.image_succeeded and not batch.errors
                         and phase_names == ['git-sdk-isolation', 'desktop-installed-image'])
        result = {'format': 'passvault-linux-desktop-installed-image02-inner-v1', 'run_id': RUN_ID,
            'commit': COMMIT, 'tree': TREE, 'parent_namespaces': batch.parent,
            'source_before': batch.before, 'source_after': batch.after, 'source_capacity': SOURCE_CAPACITY,
            'all_required_stops_ok': stops, 'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'private_io_lifecycle_ok': batch.private_io_ok,
            'git_bindings_established': batch.git_bound, 'git_bindings_stable': batch.git_stable,
            'sealed_index_unchanged': batch.index_unchanged, 'image_evidence_preserved': batch.image_preserved,
            'image_callback_validated': batch.image_callback_validated,
            'image_succeeded': collected, 'image_attempted': attempted, 'private_phase_attempted': private_phase,
            'gradle_image_selectors': IMAGE_TARGETS, 'image_binding': IMAGE_BINDING,
            'sdk_readonly_view_established': batch.git_bound, 'sdk_readonly_view_stable': batch.git_stable,
            'declared_test_cases': 0, 'errors': batch.errors, 'phases': batch.phases,
            'independent_semantic_acceptance': False, 'raw_private_output_exported': False,
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'init_sha256': FROZEN[INIT],
                                  'accepted_graph_sha256': ACCEPTED_GRAPH_IMAGES[ACCEPTED_GRAPH],
                                  'representation': 'RAW_GIT_BLOBS'},
            'resource_sample_completion_only': [bool(s['traversal_finished'] and not s['vanished_entries']
                and not s['vanished_scan_directories']) for s in batch.inventories],
            'qualification': 'Safe image outcome projection only; diagnostics/history remain private in original R. '
                'Original stop, positive namespace settlement, unchanged complete source, no mounts and bounded '
                'no-follow private disposal are separate gates. Cancel/incomplete proofs stay HOLD, no recovery. '
                'No raw private log/hash/length, contact, API credential or environment manifest exported; no '
                'secure-erasure, ownership/deliverability, application/native/JNI/test or publication credit.'}
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
    batch.auth_token = None
    os.environ.pop('GH_TOKEN', None)
    os._exit(code)


if __name__ == '__main__':
    main()
