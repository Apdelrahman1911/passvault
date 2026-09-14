#!/usr/bin/python3.12
"""Fresh Android framework/compiler/Debug-package action batch source; NOT execution admission.
Uses accepted original-process/SDK/readonly-source/stop/settlement/allowlisted-cleanup source as DATA.
No previous helper imported/executed/replayed. One finite412-node batch, no runtime test credit.
New default Debug key only in original private HOME; production signing/Release/build1017001 unchanged.
Exact source/instance/resource/custody/cleanup review and root admission required before any run.
"""
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import select
import signal
import stat
import subprocess
import sys
import time

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
R = Path('/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01')
E = B / 'runs/linux-android-framework-package-action01'
SELF = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/OUTER.py')
REQUEST = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/REQUEST.json')
APPROVAL = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/APPROVAL.json')  # Reviewer only.
LOCK = Path('/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock')
# Actual source/store/index/tool/exclude identities require root's fresh review.
# Paths or old receipts alone never admit the retired source store or an index.
GITDIR = Path('/root/projects/PassVault/passvault-publication-20260912-02/.git')
INDEX_SOURCE = GITDIR / 'index'
GIT_IMAGE = Path('/usr/bin/git')
RUBY_IMAGE = Path('/usr/bin/ruby3.2')
EXCLUDE_STATE = 'INFO_ABSENT'  # Retained checkpoint22 candidate; root must admit unchanged current U.
GIT_METADATA, GIT_INDEX = R / 'git-metadata', R / 'git-index'
INNER, INIT = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/INNER.py'), Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-framework-package-action-instance01/INIT.gradle')
EXPECTED_GRAPH = INIT.parent / 'EXPECTED-GRAPH.json'

SOURCE = B / 'reviews/checkpoint22/source-prepare01/SOURCE.json'  # Accepted checkpoint22 SOURCE bytes/hash; fresh cohort physical binding/review required.
JAVA = Path('/usr/lib/jvm/java-17-openjdk-amd64/bin/java')
RELEASE = JAVA.parent.parent / 'release'
PYTHON, INNER_PYTHON, UNSHARE = '/usr/bin/python3.12', '/usr/bin/python3', '/usr/bin/unshare'
RUN, PURPOSE = 'linux-android-framework-package-action01', 'ONE_LINUX_ANDROID_FRAMEWORK_PACKAGE_ACTION01'
HOST_POLICY_ID = 'ANDROIDFRAMEWORKPACKAGEACTION01_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_ERRNO_V5'
HOST_POLICY_ASSERTIONS = (
    'complete_prior_and_current_audit_launch_accounting',
    'every_prior_admitted_audit_operation_has_independently_accepted_scheduling_disposition',
    'historical_unknown_provenance_and_holds_preserved_without_retry_recovery_or_reclassification',
    'no_known_other_audit_execution_command_worker_or_required_original_stop_inflight_at_admission',
    'sole_audit_local_and_ci_scheduler_through_closeout',
    'exclusive_audit_source_control_transport_and_runtime_custody',
    'cooperative_shared_tools_sdk_read_only_use_and_input_guards',
    'only_original_run_private_writable_caches_and_temporary_paths',
    'owned_commands_do_not_escape_to_parent_namespaces_or_mutate_host_mounts',
    'shared_host_capacity_assumptions_and_soft_limits_explicitly_admitted',
    'unconsumed_androidframeworkpackageaction01_only_no_old_hold_or_retry_authority',
    'bound_terminal_unknown_is_not_owned_idle_or_cleanup_authority',
    'proc_anchor_and_flags0_thread_group_contracts_corroborated',
)
HOST_POLICY_EVIDENCE = ('audit_launch_accounting', 'local_and_ci_scheduler',
                        'source_control_tools_cache_custody', 'shared_host_capacity', 'scanner_kernel_contracts')
SDK, SDK_VIEW = Path('/opt/android-sdk'), R / 'sdk-readonly'
SDK_METADATA = tuple(SDK / p for p in ('platforms/android-37.0/source.properties',
    'platforms/android-37.0/package.xml', 'build-tools/36.0.0/source.properties',
    'build-tools/36.0.0/package.xml', 'licenses/android-sdk-license'))
SDK_OPERATION = 'READ_ONLY_EXISTING_COMPILE_SDK_NO_COPY_INSTALL_LICENSE_CHANGES'
ACTION_BINDING = {'runtimeRoot': '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01', 'evidenceRoot': '/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/runs/linux-android-framework-package-action01', 'readRoots': ['/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/checkout', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/gradle-home', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/tmp', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/konan', '/usr/lib/jvm/java-17-openjdk-amd64', '/root/projects/PassVault/audit-runtime-linux-android-framework-package-action01/sdk-readonly'], 'offline': False, 'sourceManifestSha256': '5eed5ec803e336c3868d1fe4255f02535fda3672ed5455e7499d7b9ffc881aa6', 'subjectSha256': '580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d'}  # Accepted checkpoint22 SOURCE literals; fresh cohort physical binding remains root-owned.
READ_ROOT_ENVELOPE = (R / 'checkout', R / 'gradle-home', R / 'tmp', R / 'konan', JAVA.parent.parent, SDK_VIEW)
COMMIT = 'd2d79ad52ce816a36f708bd35bd736a7d8068789'  # Accepted product P supplied by root; not a SOURCE capture observation.
TREE = '2e83afec1894238f59c1e9916cbf548ad54f5da2'  # Accepted product P supplied by root; not a SOURCE capture observation.
MEMBERS = 4548  # Accepted complete checkpoint22 SOURCE count; no recapture.
MEMBER_LIMIT = 4548  # Accepted exact source ceiling; no automatic widening.
SOURCE_BYTES = 2519894  # Accepted exact checkpoint22 SOURCE length; no recapture.
OID_BYTES = 186468  # Accepted4548*41 at BOTH writer/reader; full ordered OID vector.
SOURCE_CAPACITY = {'members': MEMBERS, 'member_limit': MEMBER_LIMIT, 'manifest_bytes': SOURCE_BYTES,
                   'oid_bytes': OID_BYTES, 'raw_stream_limit_bytes': 160 * 1024 * 1024}
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
BASE_REQUIRED = (INNER, INIT, SOURCE, EXPECTED_GRAPH, JAVA, RELEASE, Path('/usr/bin/mount'))
REQUIRED = BASE_REQUIRED + (GIT_IMAGE, RUBY_IMAGE) + SDK_METADATA
IMAGES = ()  # Final fixed set includes the externally reviewed optional exclude only when present.
PARENTS = (R.parent, E.parent, GITDIR, LOCK.parent)
DEVICE = {'directory_device': 23, 'regular_file_device': 24}  # Original root-supplied cohort; final instance review required.
EXPECTED_LOCK = {'bytes': 0, 'ctime_ns': 1789367939912132665, 'dev': 24, 'ino': 13719326, 'mode': 33152, 'mtime_ns': 1789367939912132665, 'nlink': 1, 'uid': 0}  # Prospective current lock; fresh request/late recheck mandatory.
FROZEN = {EXPECTED_GRAPH: '90d55008a8e88d83a235eccdb61c59ae3dd4a0b1c62e328dfb907337584223e3', INNER: 'b50f83101c52fe88ec47944eb70c16b04761253b3a73cd9e8919ddd09291addf', INIT: '27e899ff1b506540286697bad8aa1ee52d2b7b34c3b1b71fd7cc820f8b6a0651', SOURCE: '5eed5ec803e336c3868d1fe4255f02535fda3672ed5455e7499d7b9ffc881aa6'}  # Accepted SOURCE/proposed NEW control hashes; fresh request/pins/review pending.
REVIEW_ASSERTIONS = (
    'ordinary_full_stage0_index_matches_source', 'no_split_sparse_unmerged_index',
    'standalone_store_without_redirects', 'local_config_and_excludes_reviewed',
    'no_external_git_config_or_executable_mechanisms', 'publication_store_frozen_for_run',
)
TOP = 'checkout home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state git-metadata sdk-readonly'.split()
DIRS = TOP
ODIR, MIB = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, 1024 ** 2
CACHE_REPORT_LIMITS = {'roots': 128, 'files': 16, 'file_bytes': MIB, 'total_bytes': 4 * MIB,
                       'descendant_levels': 8, 'relative_json_bytes': 512}
PROBLEMS_REPORT = ('checkout', 'build', 'reports', 'problems', 'problems-report.html')
BUILDLIKE = re.compile(r'(java|javac|gradle.*|Gradle.*|kotlinc.*|kotlin.*|Kotlin.*|xcodebuild|clang.*|'
    r'gcc.*|g\+\+.*|cc|c\+\+|cc1.*|cmake|ninja|make|gmake|ctest|mvn.*|msbuild|dotnet|pytest.*|cargo|'
    r'rustc|jpackage|jlink|aapt2?|d8|r8|zipalign|adb|emulator.*|qemu-system.*)')
START = time.monotonic()
END, WORK_END, last_watch = START + 6000, START + 5250, 0.0
reasons, resources, churn, directories, images = [], [], 0, {}, {}
child, lock_fd, owned_domain, cancel_written, index_copy = None, None, None, False, None
host_policy_admitted = False
receipt = {'format': 'passvault-linux-android-framework-package-action01-outer-receipt-v5', 'run_id': RUN,
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

def check_host_policy(value):
    # Concrete root evidence is checked by the genuine instance reviewer, not
    # inferred from process names or these attestations. The entire object is
    # bound by the original request image, approval hash and authority() checks.
    require(isinstance(value, dict) and set(value) == {'id', 'candidate_classification',
            'root_assertions', 'root_evidence'} and value['id'] == HOST_POLICY_ID
            and value['candidate_classification'] == 'UNKNOWN_ORIGINAL_PARENT_PID_MNT_OR_BOUND_NONLIVE',
            'fresh explicit audit-only host policy required')
    require(isinstance(value['root_assertions'], dict)
            and set(value['root_assertions']) == set(HOST_POLICY_ASSERTIONS)
            and all(value['root_assertions'][key] is True for key in HOST_POLICY_ASSERTIONS),
            'root audit accounting/custody/capacity assertions unbound')
    evidence, observed = value['root_evidence'], {}
    require(isinstance(evidence, dict) and set(evidence) == set(HOST_POLICY_EVIDENCE),
            'concrete root evidence coverage required')
    for key in HOST_POLICY_EVIDENCE:
        group = evidence[key]
        require(isinstance(group, dict) and set(group) == {'coverage', 'records'}
                and isinstance(group['coverage'], str) and 1 <= len(group['coverage']) <= 8192
                and isinstance(group['records'], list) and 1 <= len(group['records']) <= 32,
                'root evidence coverage/records unbound')
        paths = set()
        for record in group['records']:
            require(isinstance(record, dict) and set(record) == {'path', 'bytes', 'sha256'}
                    and isinstance(record['path'], str) and len(record['path']) <= 1024
                    and record['path'] == str(Path(record['path']))
                    and Path(record['path']).is_relative_to(B) and '..' not in Path(record['path']).parts
                    and type(record['bytes']) is int and 0 < record['bytes'] <= 32 * MIB
                    and isinstance(record['sha256'], str)
                    and re.fullmatch(r'[0-9a-f]{64}', record['sha256']),
                    'root evidence descriptor invalid')
            require(record['path'] not in paths, 'duplicate root evidence within coverage')
            paths.add(record['path'])
            require(observed.setdefault(record['path'], record) == record, 'conflicting root evidence descriptors')


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
    if path == SOURCE:
        return SOURCE_BYTES
    if path in SDK_METADATA:
        return 128 * 1024
    if path in (INDEX_SOURCE, GIT_INDEX):
        return 4 * MIB
    if path in (GITDIR / 'config', GITDIR / 'info/exclude'):
        return 65536
    return 32 * MIB

def sdk_contract():
    value = request['sdk_existing_use']
    require(isinstance(value, dict) and set(value) == {'root', 'directory', 'metadata_paths', 'operation'}
            and value['root'] == str(SDK) and value['metadata_paths'] == [str(p) for p in SDK_METADATA]
            and value['operation'] == SDK_OPERATION, 'exact existing SDK read-only use contract')
    original = value['directory']
    require(isinstance(original, dict) and set(original) == {'dev', 'ino', 'uid', 'mode'}
            and all(type(v) is int for v in original.values()) and original['uid'] == 0
            and stat.S_ISDIR(original['mode']) and not original['mode'] & 0o022, 'original SDK directory pin')
    require(request['action_binding'] == ACTION_BINDING, 'exact init/inner/outer compile binding')


def sdk_guard():
    require(same_dir(pin(os.fstat(directory(SDK)), True), request['sdk_existing_use']['directory']),
            'original installed SDK directory changed')
    for path in SDK_METADATA:
        require(capture(path, 128 * 1024, False)[1] == images[str(path)], 'reviewed SDK metadata changed')
    # These bounded metadata hashes are NOT complete SDK provenance, AGP compatibility or a license acceptance.


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
    require(host_policy_admitted and HOST_POLICY_ID == 'ANDROIDFRAMEWORKPACKAGEACTION01_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_ERRNO_V5',
            'prospective bound-terminal policy requires genuine fresh exact-instance intake')
    import errno

    def terminal(fd, diagnostic, label):
        diagnostic['pidfd_observations'][label] = None
        poller = select.poll()
        poller.register(fd, select.POLLIN)
        events = poller.poll(0)
        diagnostic['pidfd_observations'][label] = [mask for _, mask in events]
        require(not events or (len(events) == 1 and events[0][0] == fd and events[0][1] != 0
                and not events[0][1] & ~(select.POLLIN | select.POLLHUP)), 'uncertain pidfd readiness')
        return bool(events)

    def error_record(error):
        return {'type': type(error).__name__, 'errno': getattr(error, 'errno', None),
                'message': str(error)[:256]}

    def retain(key, diagnostic):
        if len(encoded(diagnostic)) > 4096:
            receipt.setdefault('host_screen_first_failure', {'pid': diagnostic['pid'],
                'stage': diagnostic['stage'], 'diagnostic_overflow': True})
            raise RuntimeError('host-screen diagnostic bound')
        receipt.setdefault(key, decode(encoded(diagnostic)))

    class MissingTask(Exception):
        def __init__(self, original):
            self.original = original

    def proc_read(operation, *args, **kwargs):
        # Only anchored open/read/readlink calls below enter this boundary.
        # Close, clock, parsing, policy and readiness errors are never wrapped.
        try:
            return operation(*args, **kwargs)
        except OSError as error:
            if error.errno not in (errno.ENOENT, errno.ESRCH):
                raise
            raise MissingTask(error) from error

    def task_text(anchor, leaf, cap, diagnostic):
        require(leaf in ('comm', 'stat'), 'fixed proc task leaf required')
        leaf_fd = None
        try:
            leaf_fd = proc_read(os.open, leaf, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC,
                                dir_fd=anchor)
            data = bytearray()
            while True:
                require(time.monotonic() < deadline, 'proc task read deadline')
                part = proc_read(os.read, leaf_fd, cap + 1 - len(data))
                if not part:
                    return bytes(data).decode('utf-8')
                data.extend(part)
                require(len(data) <= cap, 'proc task text bound')
        finally:
            if leaf_fd is not None:
                try:
                    os.close(leaf_fd)
                except OSError as error:
                    diagnostic['leaf_close_error'] = error_record(error)
                    # A close failure must never masquerade as missing-task departure.
                    raise RuntimeError('proc task leaf close failure') from error

    def task_birth(anchor, pid, diagnostic):
        raw = task_text(anchor, 'stat', 4096, diagnostic)
        require(raw.startswith(str(pid) + ' (') and raw.rfind(')') >= len(str(pid)) + 2,
                'malformed anchored proc stat identity')
        fields = raw[raw.rfind(')') + 2:].split()
        require(len(fields) >= 20, 'short anchored proc stat')
        return {'pid': pid, 'ppid': int(fields[1]), 'start': int(fields[19])}

    def task_namespace(anchor, namespace):
        require(namespace in ('pid', 'mnt'), 'fixed proc namespace required')
        return proc_read(os.readlink, 'ns/' + namespace, dir_fd=anchor)

    def parent_policy(owner, original_birth):
        require(owned_domain is None or all(owned_domain[k] != parent_ns[k] for k in ('pid', 'mnt')),
                'admitted owned namespace pair not distinct from original parent')
        require(owner is None or (owner['fd'] is not None
                and original_birth['pid'] != owner['p'].pid
                and original_birth['ppid'] != owner['p'].pid
                and (not owner['isolated'] or owned_domain is not None)),
                'direct audit child/descendant or unbound isolated audit domain')

    def owned_policy(owner, diagnostic):
        require(owner is not None and owner['p'].returncode is None and owner['fd'] is not None
                and diagnostic['expected_namespaces'] is not None,
                'host buildlike conflict: ' + str(diagnostic['pid']) + ':' + diagnostic['comm'])

    def record_terminal(owner, diagnostic, kind):
        # Caller has just observed understood terminal readiness of the bound
        # original flags0 pidfd. No candidate signal or numeric-PID fallback.
        require(diagnostic['proc_anchor_bound'], 'terminal candidate without original proc binding')
        if diagnostic['owned_proof']:
            require(not terminal(owner['fd'], diagnostic, 'owner-at-' + kind),
                    'original namespace owner ended before departure exemption')
            suffix = kind if kind in ('enoent', 'esrch') else 'postread'
            retain('host_screen_first_owned_terminal_' + suffix, diagnostic)
            key = 'host_screen_owned_terminal_' + suffix + '_count'
        else:
            diagnostic['classification'] = 'NONLIVE_UNKNOWN'
            diagnostic['qualification'] = 'Original process terminal only; not owned, idle, workload/descendant settlement or cleanup authority.'
            retain('host_screen_first_nonlive_unknown', diagnostic)
            key = 'host_screen_nonlive_unknown_count'
        receipt[key] = receipt.get(key, 0) + 1

    deadline = min(time.monotonic() + 5, END)
    with os.scandir('/proc') as entries:
        for index, entry in enumerate(entries):
            require(index < 8192 and time.monotonic() < deadline, 'host-screen entry/time bound')
            name = entry.name
            if not name.isdecimal():
                continue
            owner, fd, anchor = child, None, None
            diagnostic = {'pid': int(name), 'owner_pid': None if owner is None else owner['p'].pid,
                'comm': None, 'stage': 'pidfd-open', 'owned_proof': False, 'proc_anchor_bound': False,
                'expected_namespaces': None if owned_domain is None else dict(owned_domain),
                'pidfd_observations': {}}
            try:
                try:
                    try:
                        fd = os.pidfd_open(int(name), 0)  # flags0: process/thread-group, not PIDFD_THREAD.
                    except ProcessLookupError:
                        churn += 1  # Pre-positive and unclassified; NOT benign/idle proof.
                        continue
                    diagnostic['stage'] = 'proc-anchor-open'
                    try:
                        anchor = os.open('/proc/' + name, os.O_RDONLY | os.O_DIRECTORY
                                         | os.O_NOFOLLOW | os.O_CLOEXEC)
                    except FileNotFoundError:
                        require(terminal(fd, diagnostic, 'prepositive-anchor-enoent'),
                                'live/unreadable potential host workload')
                        churn += 1
                        continue
                    diagnostic['stage'] = 'proc-anchor-bind'
                    if terminal(fd, diagnostic, 'candidate-binding'):
                        # The numeric name might already be another task: read no
                        # positive comm from it and make no terminal-UNKNOWN claim.
                        churn += 1
                        continue
                    diagnostic['proc_anchor_bound'] = True
                    diagnostic['stage'] = 'comm-initial'
                    try:
                        comm = task_text(anchor, 'comm', 256, diagnostic).strip()
                    except MissingTask as error:
                        kind = errno.errorcode[error.original.errno].lower()
                        diagnostic['read_' + kind] = error_record(error.original)
                        require(terminal(fd, diagnostic, 'prepositive-' + kind),
                                'live/unreadable potential host workload')
                        churn += 1
                        continue
                    diagnostic['comm'] = comm
                    if not BUILDLIKE.fullmatch(comm):
                        continue

                    try:
                        diagnostic['stage'] = 'proof-birth'
                        row = task_birth(anchor, int(name), diagnostic)
                        diagnostic['identity'] = row
                        original_birth = dict(row)
                        row['namespaces'] = {}
                        possible = [('parent', {k: parent_ns[k] for k in ('pid', 'mnt')})]
                        if diagnostic['expected_namespaces'] is not None:
                            possible.append(('owned', diagnostic['expected_namespaces']))
                        for namespace in ('pid', 'mnt'):
                            diagnostic['stage'] = 'proof-namespace:' + namespace
                            observed = task_namespace(anchor, namespace)
                            row['namespaces'][namespace] = observed
                            possible = [(kind, pair) for kind, pair in possible if pair[namespace] == observed]
                            require(possible, 'positive namespace prefix conflicts with admitted domains')
                            # Branch restrictions are checked as soon as the
                            # observed prefix identifies that branch, not after
                            # another read which could fail and hide a mismatch.
                            kinds = {kind for kind, _ in possible}
                            if kinds == {'parent'}:
                                parent_policy(owner, original_birth)
                            elif kinds == {'owned'}:
                                owned_policy(owner, diagnostic)
                        diagnostic['stage'] = 'proof-birth-reread'
                        diagnostic['observed_birth'] = task_birth(anchor, int(name), diagnostic)
                        require(diagnostic['observed_birth'] == original_birth, 'unstable process identity')
                        diagnostic['stage'] = 'proof-candidate-pidfd'
                        if terminal(fd, diagnostic, 'candidate-proof'):
                            record_terminal(owner, diagnostic, 'postread')
                            continue
                        diagnostic['stage'] = 'proof-owned-domain'
                        if row['namespaces'] == {k: parent_ns[k] for k in ('pid', 'mnt')}:
                            diagnostic['stage'] = 'proof-original-parent-policy'
                            parent_policy(owner, original_birth)
                            diagnostic['classification'] = 'UNKNOWN_ORIGINAL_PARENT_PID_MNT'
                        else:
                            owned_policy(owner, diagnostic)
                            require(diagnostic['expected_namespaces'] == row['namespaces'],
                                    'host buildlike conflict: ' + name + ':' + comm)
                            diagnostic['stage'] = 'proof-owner-pidfd'
                            require(not terminal(owner['fd'], diagnostic, 'owner-proof'),
                                    'original namespace owner terminal')
                            diagnostic['owned_proof'] = True

                        diagnostic['stage'] = 'reread-comm'
                        diagnostic['observed_comm'] = task_text(anchor, 'comm', 256, diagnostic).strip()
                        require(diagnostic['observed_comm'] == comm, 'positive comm changed')
                        diagnostic['stage'] = 'reread-birth-first'
                        diagnostic['observed_birth'] = task_birth(anchor, int(name), diagnostic)
                        require(diagnostic['observed_birth'] == original_birth, 'positive birth changed')
                        diagnostic['observed_namespaces'] = {}
                        for namespace in ('pid', 'mnt'):
                            diagnostic['stage'] = 'reread-namespace:' + namespace
                            observed = task_namespace(anchor, namespace)
                            diagnostic['observed_namespaces'][namespace] = observed
                            require(observed == row['namespaces'][namespace], 'positive namespace changed')
                        diagnostic['stage'] = 'reread-birth-last'
                        diagnostic['observed_birth'] = task_birth(anchor, int(name), diagnostic)
                        require(diagnostic['observed_birth'] == original_birth, 'positive birth changed')
                    except MissingTask as error:
                        kind = errno.errorcode[error.original.errno].lower()
                        diagnostic['read_' + kind] = error_record(error.original)
                        if diagnostic['owned_proof']:
                            diagnostic['reread_' + kind] = diagnostic['read_' + kind]
                        require(terminal(fd, diagnostic, 'candidate-at-' + kind),
                                'positive ' + kind.upper() + ' without original terminal pidfd')
                        record_terminal(owner, diagnostic, kind)
                        continue
                    diagnostic['stage'] = 'reread-candidate-pidfd'
                    if terminal(fd, diagnostic, 'candidate-final'):
                        record_terminal(owner, diagnostic, 'postread')
                        continue
                    if diagnostic['owned_proof']:
                        diagnostic['stage'] = 'reread-owner-pidfd'
                        require(not terminal(owner['fd'], diagnostic, 'owner-final'),
                                'original namespace owner terminal')
                    else:
                        diagnostic['stage'] = 'unknown-original-parent-observed'
                        retain('host_screen_first_unknown_parent_candidate', diagnostic)
                        receipt['host_screen_unknown_parent_observations'] = receipt.get(
                            'host_screen_unknown_parent_observations', 0) + 1
                except Exception as error:
                    diagnostic['error'] = error_record(error)
                    raise
                finally:
                    close_failure = None
                    for descriptor, key in ((anchor, 'proc_close_error'), (fd, 'close_error')):
                        if descriptor is not None:
                            try:
                                os.close(descriptor)  # One attempt each, even if the other close failed.
                            except OSError as error:
                                diagnostic[key] = error_record(error)
                                close_failure = error
                    if close_failure is not None:
                        raise RuntimeError('candidate descriptor close failure') from close_failure
            except Exception as error:
                retain('host_screen_first_failure', diagnostic)
                raise RuntimeError('host-screen candidate uncertainty: ' + name + ':' + diagnostic['stage']) from error
    require(time.monotonic() < deadline, 'host-screen final time bound')

def watch(entry=False):
    global last_watch
    memory = dict(line.split(':', 1) for line in Path('/proc/meminfo').read_text().splitlines())
    available, total = (int(memory[k].split()[0]) * 1024 for k in ('MemAvailable', 'MemTotal'))
    disks = [os.fstatvfs(directory(p)) for p in PARENTS[:2]]
    free, now = min(s.f_bavail * s.f_frsize for s in disks), time.monotonic()
    if not resources or now - START - resources[-1]['elapsed'] >= 30:
        resources.append({'elapsed': round(now - START, 3), 'disk_available': free, 'MemAvailable': available, 'MemTotal': total})
    require(free >= (8 if entry else 5) * 1024 ** 3 and available >= total * (0.25 if entry else 0.20), 'resource floor')
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
            require(os.fstat(c['out']).st_size <= (MIB // 2 if c['isolated'] else 160 * MIB)
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
    require(os.fstat(c['out']).st_size <= (MIB // 2 if c['isolated'] else 160 * MIB)
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
    require(len(data) == OID_BYTES == MEMBERS * 41, 'canonical exact current-source OID input size')
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
    actual, original = capture(R / 'source.oids', OID_BYTES)
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
                require(0 <= size <= 32 * MIB and total + size + 1 <= original['pin']['bytes'] <= 160 * MIB,
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
    sdk_guard()
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
        'qualification': 'Explicit cooperative audit-path/mount custody only; not protection against concurrent hostile-root mount mutation.'}

def cache_report_root(parts, is_directory):
    # Exact accepted static02 source shapes only; no basename disposal waiver.
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


def capture_report_source(parts, snapshot):
    # Bind every containing directory to the original complete snapshot BEFORE
    # reusing capture(); never adopt a new parent merely because the name matches.
    for count in range(1, len(parts)):
        prefix = parts[:count]
        fd = directory(R.joinpath(*prefix))
        require(same_dir(pin(os.fstat(fd), True), snapshot[prefix]), 'report parent differs from original snapshot')
    data, image = capture(R.joinpath(*parts), MIB)
    require(image['pin'] == snapshot[parts], 'report file differs from original snapshot')
    return data, image


def preserve_generated_reports(snapshot, expected, folders):
    frozen_files = {('checkout', *name.split('/')) for name in expected}
    frozen_directories = {('checkout', *name.split('/')) for name in folders}
    problem_directories = {PROBLEMS_REPORT[:count] for count in range(3, len(PROBLEMS_REPORT))}
    roots, candidates, cache_files, cache_bytes = {}, [], 0, 0
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
    copies = []
    for parts, kind in candidates:
        tick(END)
        data, original = capture_report_source(parts, snapshot)
        destination = 'GENERATED-REPORT-%04d.bin' % (len(copies) + 1)
        output(destination, data)  # Existing exclusive flat-E write + file/parent fsync.
        saved_data, saved = capture(E / destination, MIB)
        require(saved_data == data and saved['sha256'] == original['sha256']
                and saved['pin']['dev'] == DEVICE['regular_file_device']
                and saved['pin']['mode'] == stat.S_IFREG | 0o600, 'exact durable report copy/readback')
        copies.append({'relative': '/'.join(parts), 'kind': kind, 'destination': destination,
                       'source_image': original, 'saved_image': saved})
    manifest = {'format': 'passvault-linux-android-framework-package-action01-retained-diagnostics-v1', 'run_id': RUN,
        'cache_roots': {'/'.join(parts): value for parts, value in roots.items()}, 'limits': CACHE_REPORT_LIMITS,
        'cache_file_count': cache_files, 'cache_logical_bytes': cache_bytes,
        'problems_report_limit_bytes': MIB, 'copies': copies,
        'qualification': 'Opaque generated diagnostic bytes conserved outside R, no provenance or test credit.'}
    output('GENERATED-REPORTS.json', encoded(manifest))
    saved_manifest, manifest_image = capture(E / 'GENERATED-REPORTS.json', MIB)
    require(saved_manifest == encoded(manifest), 'exact durable generated-report manifest')
    receipt['generated_report_preservation'] = {'manifest': manifest_image, 'copied_files': len(copies),
        'copied_logical_bytes': sum(row['source_image']['pin']['bytes'] for row in copies),
        'source_and_saved_reverified_before_unlink': False, 'saved_reverified_after_cleanup': False}
    return {'copies': copies, 'manifest_image': manifest_image}


def verify_generated_reports(conserved, snapshot, *, source):
    require(capture(E / 'GENERATED-REPORTS.json', MIB)[1] == conserved['manifest_image'], 'retained report manifest changed')
    for row in conserved['copies']:
        tick(END)
        if source:
            require(capture_report_source(tuple(row['relative'].split('/')), snapshot)[1] == row['source_image'],
                    'original report changed before first unlink')
        require(capture(E / row['destination'], MIB)[1] == row['saved_image'], 'retained diagnostic bytes/pin changed')
    key = 'source_and_saved_reverified_before_unlink' if source else 'saved_reverified_after_cleanup'
    receipt['generated_report_preservation'][key] = True


def remove_runtime(expected, folders):
    no_runtime_mounts()
    for target in (GIT_METADATA, SDK_VIEW):
        mountpoint = directory(target)
        require(pin(os.fstat(mountpoint), True) == directories[target][1],
                'original underlying Git/SDK mountpoint changed')
        with os.scandir(mountpoint) as entries:
            require(next(entries, None) is None, 'underlying Git/SDK mountpoint is not empty')
    receipt['sdk_underlying_after'] = {'directory': directories[SDK_VIEW][1], 'empty': True}
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
    conserved = preserve_generated_reports(snapshot, expected, folders)
    verify_generated_reports(conserved, snapshot, source=True)
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
    verify_generated_reports(conserved, snapshot, source=False)

def main():
    global lock_fd, parent_ns, request, request_image, approval_image, IMAGES, host_policy_admitted
    require(sys.argv == [str(SELF)] and sys.executable == PYTHON and not sys.flags.optimize and sys.flags.isolated and
            sys.flags.no_site and sys.dont_write_bytecode and dict(os.environ) == ENV and os.getuid() == os.geteuid() == 0, 'fixed entry')
    require(all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{40}', value) for value in (COMMIT, TREE))
            and type(MEMBERS) is int and type(MEMBER_LIMIT) is int and 1 <= MEMBERS <= MEMBER_LIMIT
            and type(SOURCE_BYTES) is int and 0 < SOURCE_BYTES <= 32 * MIB
            and type(OID_BYTES) is int and OID_BYTES == MEMBERS * 41
            and all(isinstance(path, Path) and path.is_absolute() and '..' not in path.parts
                    for path in (GITDIR, INDEX_SOURCE, GIT_IMAGE, RUBY_IMAGE))
            and GITDIR == Path('/root/projects/PassVault/passvault-publication-20260912-02/.git')
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
            'PENDING AndroidFrameworkPackageAction01 source/index/store/tool/exclude/device/lock binding: source-only, no admission')
    require(isinstance(ACTION_BINDING, dict) and set(ACTION_BINDING) == {
                'runtimeRoot', 'evidenceRoot', 'readRoots', 'offline', 'sourceManifestSha256', 'subjectSha256'}
            and ACTION_BINDING['runtimeRoot'] == str(R) and ACTION_BINDING['evidenceRoot'] == str(E)
            and ACTION_BINDING['readRoots'] == [str(p) for p in READ_ROOT_ENVELOPE]
            and type(ACTION_BINDING['offline']) is bool
            and ACTION_BINDING['sourceManifestSha256'] == FROZEN[SOURCE]
            and ACTION_BINDING['subjectSha256'] == '580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d',
            'UNBOUND exact new graph/source/read-root/offline binding')
    raw, request_image = capture(REQUEST, MIB)
    request = decode(raw)
    raw, approval_image = capture(APPROVAL, 65536)
    approval = decode(raw)
    require(set(request) == {'format', 'run_id', 'purpose', 'commit', 'tree', 'parent_namespaces', 'directories',
            'lock', 'images', 'coordination', 'device_model', 'git_inventory_binding', 'tool_aliases',
            'sdk_existing_use', 'action_binding', 'source_capacity', 'host_policy'}
            and request['device_model'] == DEVICE and request['source_capacity'] == SOURCE_CAPACITY,
            'request/device/current source-capacity schema')
    inventory_contract()
    sdk_contract()
    IMAGES = tuple(dict.fromkeys(REQUIRED + (SELF, Path(UNSHARE), Path(PYTHON), Path(INNER_PYTHON),
        Path('/usr/bin/env'), INDEX_SOURCE, GITDIR / 'config')
        + ((GITDIR / 'info/exclude',) if EXCLUDE_STATE == 'FILE' else ())))
    require(request['format'] == 'passvault-linux-android-framework-package-action01-request-v5' and request['run_id'] == RUN and
            request['purpose'] == PURPOSE and request['commit'] == COMMIT and request['tree'] == TREE, 'request purpose/source')
    require(request['coordination'] == {'sole_build_owner': '/root', 'agents_quiescent': True, 'no_ci': True}
            and request['coordination']['agents_quiescent'] is True and request['coordination']['no_ci'] is True,
            'coordination not admitted')
    check_host_policy(request['host_policy'])
    parent_ns = {k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')}
    entry_parent = identity(os.getppid(), ('pid', 'mnt'))
    require(request['parent_namespaces'] == parent_ns == entry_parent['namespaces'], 'original parent/caller namespace mismatch')
    receipt['parent_at_entry'] = entry_parent  # Provenance only, never signalling/adoption/deletion authority.
    require(set(request['directories']) == {str(p) for p in PARENTS} and set(request['images']) == {str(p) for p in IMAGES}, 'fixed sets')
    require(set(approval) == {'format', 'reviewer', 'disposition', 'purpose', 'run_id', 'request_sha256', 'sources'} and
            approval['format'] == 'passvault-linux-android-framework-package-action01-approval-v5' and
            approval['reviewer'] == '/root/current_ledger' and
            approval['disposition'] == 'ACCEPT_EXACT_NEW_INSTANCE' and approval['purpose'] == PURPOSE and approval['run_id'] == RUN and
            approval['request_sha256'] == request_image['sha256'] and approval['sources'] ==
            {str(p): request['images'][str(p)]['sha256'] for p in (SELF, INNER, INIT, SOURCE, EXPECTED_GRAPH)}, 'missing/stale/nonaccepting genuine review')
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
            require(actual['pin']['bytes'] == SOURCE_BYTES, 'exact current source manifest length')
            source = decode(data)
    for path in PARENTS:
        require(same_dir(pin(os.fstat(directory(path)), True), request['directories'][str(path)]), 'original parent mismatch')
    authority()
    host_policy_admitted = True  # Only after fresh V5 request/review and locked original input intake.
    receipt['host_policy'] = dict(request['host_policy'], qualification=
        'Root-supplied, independently reviewed cooperative assumptions; not runtime ownership or hard-cap proof.')
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
    allocation = {'format': 'passvault-linux-android-framework-package-action01-allocation-v1', 'run_id': RUN, 'purpose': PURPOSE,
        'commit': COMMIT, 'tree': TREE, 'source_members': MEMBERS, 'source_representation': 'RAW_GIT_BLOBS',
        'host_policy_id': HOST_POLICY_ID,
        'allocated_directories': {str(p): directories[p][1] for p in (E, R)},
        'parents': {str(p): directories[p][1] for p in PARENTS}, 'parent_namespaces': parent_ns,
        'packet': {'request': request_image, 'approval': approval_image},
        'sources': {str(p): images[str(p)] for p in (SELF, INNER, INIT, SOURCE, EXPECTED_GRAPH)}, 'device_model': DEVICE,
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
        receipt['children'][-1]['outputs'] = {str(p): capture(p, 160 * MIB if p == R / 'source.blobs' else MIB // 2, False)[1]
                                             for p in (outpath, errpath)}
        if not isolated:
            blob_image = receipt['children'][-1]['outputs'][str(R / 'source.blobs')]
            require(blob_image['pin'] == produced_blob_pin, 'original Git-produced stream changed before capture')
            receipt['raw_transport']['response'] = {str(R / 'source.blobs'): blob_image}
            materialize(expected, folders, blob_image)
            before = source_check(expected)
            require(capture(R / 'source.blobs', 160 * MIB, False)[1] == blob_image, 'blob stream changed during materialization')
            receipt['raw_transport']['complete_stream_passes'] = 2
            receipt['source_before'] = before
            authority()
            inner_images = {str(p): images[str(p)] for p in REQUIRED}
            inner_images[str(GIT_INDEX)] = index_copy['copy_image']
            for relative in ('config',) + (('info/exclude',) if EXCLUDE_STATE == 'FILE' else ()):
                inner_images[str(GIT_METADATA / relative)] = images[str(GITDIR / relative)]
            output('OUTER-INTENT.json', encoded({'format': 'passvault-linux-android-framework-package-action01-outer-v1', 'run_id': RUN, 'commit': COMMIT, 'tree': TREE,
                'source_representation': 'RAW_GIT_BLOBS', 'source_members': MEMBERS, 'source_capacity': SOURCE_CAPACITY,
                'parent_namespaces': parent_ns, 'host_policy_id': HOST_POLICY_ID,
                'directories': {str(p): v[1] for p, v in directories.items() if p == R or R in p.parents or p == E or E in p.parents},
                'images': inner_images, 'outer_images': images,
                'git_inventory_binding': request['git_inventory_binding'], 'index_copy': index_copy,
                'tool_aliases': request['tool_aliases'], 'sdk_existing_use': request['sdk_existing_use'],
                'action_binding': request['action_binding'],
                'packet': {'request': request_image, 'approval': approval_image}, 'device_model': DEVICE, 'raw_source': before}))
            command = [UNSHARE, '--mount', '--pid', '--fork', '--kill-child=SIGKILL', '--propagation=private', '--mount-proc=/proc',
                       '--', INNER_PYTHON, '-I', '-B', '-S', str(INNER), parent_ns['pid'], parent_ns['mnt']]
    raw, result_image = capture(E / 'INNER-RESULT.json', MIB)
    result = decode(raw)
    receipt.update(inner_exit=code, inner_result=result_image)
    require(code in (0, 1) and result['format'] == 'passvault-linux-android-framework-package-action01-inner-v1' and result['run_id'] == RUN and
            result['commit'] == COMMIT and result['tree'] == TREE and result['parent_namespaces'] == parent_ns and all(result[k] is True for k in
            ('source_before', 'source_after', 'all_required_stops_ok', 'namespace_empty_before_exit',
             'cleanup_safe', 'git_bindings_established', 'git_bindings_stable', 'sealed_index_unchanged',
              'sdk_readonly_view_established', 'sdk_readonly_view_stable')),
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
    raw, sdk_preflight = capture(E / 'SDK-PREFLIGHT.json', MIB)
    sdk = decode(raw)
    require(sdk['root'] == str(SDK) and sdk['view'] == str(SDK_VIEW) and sdk['operation'] == SDK_OPERATION
            and sdk['directory'] == request['sdk_existing_use']['directory']
            and isinstance(sdk['mount_rows'], list) and len(sdk['mount_rows']) == 1
            and sdk['metadata'] == {str(p): images[str(p)] for p in SDK_METADATA}, 'original private readonly SDK receipt')
    receipt['sdk_preflight'] = sdk_preflight
    require(all(type(result[k]) is bool for k in
                ('action_evidence_preserved', 'action_mapping_ok', 'action_evidence_collected'))
            and result['gradle_action_selectors'] == [':app-android:auditCompileDebugFrameworkInstrumentation', ':app-android:verifyDebugComposeResources']
            and result['method_filter'] is None
            and result['action_binding'] == ACTION_BINDING and result['source_capacity'] == SOURCE_CAPACITY
            and type(result['declared_test_cases']) is int and result['declared_test_cases'] == 0
            and result['independent_semantic_acceptance'] is False
            and result['independent_final_log_review_required'] is True
            and result['exclusive_global_failure_established'] is False,
            'compile-package zero-case callback-local result contract')
    phases = result['phases']
    require(isinstance(phases, list) and 1 <= len(phases) <= 2
            and [p['phase'] for p in phases] in (['git-sdk-isolation'], ['git-sdk-isolation', 'android-framework-actions'])
            and all(type(p['stop_required']) is bool for p in phases), 'fixed compile-package phase result contract')
    attempted = any(p['phase'] == 'android-framework-actions' and p['stop_required'] for p in phases)
    require(not attempted or result['action_evidence_preserved'] is True, 'attempted graph evidence not preserved')
    if code == 0:
        require(attempted and result['action_evidence_preserved'] is True
                and result['action_mapping_ok'] is True and result['action_evidence_collected'] is True
                and [p['phase'] for p in phases] == ['git-sdk-isolation', 'android-framework-actions']
                and not result['errors'], 'collected protocol requires exact graph evidence; final log review pending')
    # A retained nonaccepting graph can be cleaned only after independent original safety gates.
    receipt['action_scope'] = {'attempted': attempted, 'selectors': [':app-android:auditCompileDebugFrameworkInstrumentation', ':app-android:verifyDebugComposeResources'],
        'declared_test_cases': 0, 'compiler_evidence_only': True, 'runtime_credit': False,
        'independent_final_log_review_required': True, 'exclusive_global_failure_established': False,
        **{k: result[k] for k in ('action_evidence_preserved', 'action_mapping_ok',
                                'action_evidence_collected')}}
    require(source_check(expected) == before, 'outer raw-source after mismatch')
    authority()
    watch()
    remove_runtime(expected, folders)
    receipt['status'] = ('ACTION_OBSERVATION_COLLECTED_PENDING_INDEPENDENT_REVIEW' if code == 0
                         else 'ACTION_OBSERVATION_NONACCEPTING_CLEANED')
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
            preterminal=True, qualification='Receipt precedes final descriptor closes/exit; actual exit authoritative. Explicit audit-only parent-UNKNOWN policy, nonexhaustive comm screen, generic-interpreter blind spot; no global-idle/other-owner/no-escape/hard-deadline proof.')
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
