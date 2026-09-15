#!/usr/bin/python3.12
"""Fresh prospective GUI24 single-chooser-case outer; SOURCE ONLY, NOT admission.

Reuse accepted GUI07 transport/V5/descriptor/cleanup SOURCE, never old runtime,
namespace, store, process, device, lock or approval identity. No old helper is
imported/executed/retried. One remaining chooser case / one XML suite / one
app-desktop Test task; preserve GUI16 Main/Room and GUI07 Room without rerunning either.
P23 raw source plus exact57B test dependency and accepted unlock diagnostic; no
shipped production change, dependency version change or publication; no production overlay.
Root alone binds current original admission and genuine independent approval.
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
R = Path('/root/projects/PassVault/audit-runtime-linux-desktop-integration24')
E = B / 'runs/linux-desktop-integration24'
SELF = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gui24-instance01/OUTER.py')
REQUEST = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gui24-instance01/REQUEST.json')
APPROVAL = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gui24-instance01/APPROVAL.json')
LOCK = Path('/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock')
# Root selected these prospective literals; fresh facts, exact review and late admission remain required.
# No allocation, tool capture, runtime custody or current lock/device identity is claimed here.
# P23 content selection remains exact; root selected the original raw-OID store below.
# Never borrow GUI03 or other consumed-instance authority; old runtime HOLD remains.
GITDIR = Path('/root/projects/PassVault/passvault-publication-20260913-03/.git')
INNER, INIT = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gui24-instance01/INNER.py'), Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gui24-instance01/INIT.gradle')
SOURCE = B / 'reviews/checkpoint23/source-prepare01/SOURCE.json'
DIAGNOSTIC = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gui24-instance01/CredentialMainNavHostRoomIntegrationTest.kt')
JAVA = Path('/usr/lib/jvm/java-17-openjdk-amd64/bin/java')
RELEASE = JAVA.parent.parent / 'release'
PYTHON, INNER_PYTHON, GIT, UNSHARE = '/usr/bin/python3.12', '/usr/bin/python3', '/usr/bin/git', '/usr/bin/unshare'
RUN, PURPOSE = 'linux-desktop-integration24', 'ONE_LINUX_DESKTOP_INTEGRATION24'
PROSPECTIVE_ADMISSION_BOUND = True  # Root-selected prospective literals only; exact request review and late admission still required.
CLEANUP_DISK_RESERVE_BYTES, CLEANUP_RAM_RESERVE_BYTES = 268435456, 536870912  # Root-selected prospective reserves; fresh capacity still required.
HOST_POLICY_ID = 'GUI24_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_ERRNO_V5'
DISPOSAL_POLICY_ID = 'GUI24_ORIGINAL_PRE_GUI_CANCELLED_COPY_ONLY_V1'
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
    'unconsumed_gui24_only_no_old_hold_or_retry_authority',
    'bound_terminal_unknown_is_not_owned_idle_or_cleanup_authority',
    'proc_anchor_and_flags0_thread_group_contracts_corroborated',
    'known_host_custody_identity_and_evidence_contradictions_remain_cleanup_hold',
)
HOST_POLICY_EVIDENCE = ('audit_launch_accounting', 'local_and_ci_scheduler',
                        'source_control_tools_cache_custody', 'shared_host_capacity', 'scanner_kernel_contracts')
COMMIT = '59bee33096f538b9444f00236448d7fc0ea740b2'
TREE = '76a7dd46c0c1d69bae554c4eb02106da2ac1f6c6'
MEMBERS = 5121  # Exact preserved P23 SOURCE ordered member count; no new capture.

# Exact base/effective identities: the overlay does not alter SOURCE/INDEX or claim a new Git tree.
SOURCE_BINDING = {'representation': 'P23_RAW_GIT_BLOBS_PLUS_DESKTOPTEST_DEPENDENCY_AND_CHOOSER_FIXTURE',
 'base_commit': '59bee33096f538b9444f00236448d7fc0ea740b2',
 'base_tree': '76a7dd46c0c1d69bae554c4eb02106da2ac1f6c6',
 'source_manifest_sha256': '1984fc3221b38fbd4e1c894b0a5b2cc13df8828dc7b5415aa50b0e887e47fccc',
 'members': 5121,
 'base_raw_bytes': 153458946,
 'observed_checkout_bytes': 153460893,
 'framed_transport_bytes': 153725453,
 'effective_bytes': 153489436,
 'base_content_identity_sha256': '19211d126337ddf19a37a6b89522642d62dcc247c199a6420cdc1e4b28abd91c',
 'effective_content_identity_sha256': '30ff3211581884489278866578427954669e14e322b47156a609ef222707de85',
 'diagnostic_overlay': {'path': 'app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt',
                        'git_mode': '100644',
                        'before': {'bytes': 88602,
                                   'sha256': '5fe322e48fef5fe7b6d2729817ad6fdbeb6bec182bf1cde5198ffdb1bb8c01a3',
                                   'git_blob': '91858bd15c2ab211bc263b881157cebd9a283154'},
                        'after': {'bytes': 119035,
                                  'sha256': 'cc92a172e2b79ce8122614ffa3f78feb27b8ccc88db0e4680ef245cea8612547',
                                  'git_blob': '2a020c0603cdd5567f4baab44acbe4229dc62d02'},
                        'scope': 'PVU003-only real native chooser ownership/focus via exact inline read-only '
                                 'isolated Python/libX11; successful Main and PVA027 not executed. No '
                                 'production overlay.'}}
DIAGNOSTIC_OVERLAY = SOURCE_BINDING['diagnostic_overlay']
SOURCE_OVERLAY = {'path': 'app-desktop/build.gradle.kts',
 'git_mode': '100644',
 'before': {'bytes': 63918,
            'sha256': '9263cf5e857f55afd4669aa524eaf36f37d3b5a18f4eca293da148bd55b3e709',
            'git_blob': '172fd771919fbb848c7c413af97706b257a7c3d2'},
 'after': {'bytes': 63975,
           'sha256': 'd0ead442c57c9465333d036bcb4d96f1ac5b06fd8d04dfe5b45fc571100265e3',
           'git_blob': 'a8895db34f68cf10b7795079c433c9ef283d4cba'},
 'scope': 'Exactly one pinned libs.lifecycle.viewmodel implementation line in desktopTest only; no version or '
          'production-runtime change'}
RESOURCE_POLICY = 'GUI24_8GiB_ENTRY_3GiB_RUNNING_3GiB_PRIVATE_256MiB_DISK_512MiB_RAM_V1'
ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC'}
GUI_TOOLS = tuple(Path('/usr/bin/' + name) for name in
                  ('Xvfb', 'xauth', 'xfwm4', 'dbus-daemon', 'mount', 'ip', 'xdpyinfo', 'xprop', 'xfce4-panel', 'xfconf-query')) + (
                      Path('/usr/lib/x86_64-linux-gnu/xfce4/xfconf/xfconfd'),
                      Path('/usr/lib/x86_64-linux-gnu/xfce4/panel/plugins/libsystray.so'),
                      Path('/usr/lib/x86_64-linux-gnu/xfce4/panel/wrapper-2.0'),)
REQUIRED = (Path(PYTHON), Path('/usr/lib/x86_64-linux-gnu/libX11.so.6.4.0'), INNER, INIT, SOURCE, DIAGNOSTIC, JAVA, RELEASE) + GUI_TOOLS
IMAGES = REQUIRED + (SELF, Path(GIT), Path(UNSHARE), Path(INNER_PYTHON), Path('/usr/bin/env'))
PARENTS = (R.parent, E.parent, GITDIR, LOCK.parent)
DEVICE = {'directory_device': 23, 'regular_file_device': 24}  # Selected DATA literals only; root must verify fresh facts before approval.
EXPECTED_LOCK = {'bytes': 0, 'ctime_ns': 1789367939912132665, 'dev': 24, 'ino': 13719326, 'mode': 33152, 'mtime_ns': 1789367939912132665, 'nlink': 1, 'uid': 0}  # Selected DATA literals only; no inherited lock custody.
FROZEN = {INNER: '86444dbd81ca6e69ae35729e75cd11b6c983e036a8023151551b21aba2715dff', INIT: 'ef0acc1e64e24092ea8580ef4da5393265c37e8eaa63ce1357559d7cc088b4e4', SOURCE: '1984fc3221b38fbd4e1c894b0a5b2cc13df8828dc7b5415aa50b0e887e47fccc', DIAGNOSTIC: 'cc92a172e2b79ce8122614ffa3f78feb27b8ccc88db0e4680ef245cea8612547'}
TOP = 'checkout home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state workers pvu003'.split()
WORKERS = ['mainnav']
PVA027_CROPS = ('tray-tooltip-en1.png', 'tray-tooltip-ar.png', 'tray-tooltip-en2.png',
                'tray-lock-selection.png', 'tray-exit-selection.png')
CHILDREN = 'home tmp jna sqlite xdg-cache xdg-config xdg-data xdg-state'.split()
DIRS = TOP + [f'workers/{w}' for w in WORKERS] + [f'workers/{w}/{d}' for w in WORKERS for d in CHILDREN]
ODIR, MIB = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC, 1024 ** 2
CACHE_REPORT_LIMITS = {'roots': 128, 'files': 16, 'file_bytes': MIB, 'total_bytes': 4 * MIB,
                       'descendant_levels': 8, 'relative_json_bytes': 512}
PROBLEMS_REPORT = ('checkout', 'build', 'reports', 'problems', 'problems-report.html')
AUXILIARY_REPORT_LIMITS = {'roots': 4, 'files': 64, 'file_bytes': MIB, 'total_bytes': 4 * MIB,
                           'descendant_levels': 8, 'relative_json_bytes': 512}
AUXILIARY_REPORT_ROOTS = (
    ('checkout', 'app-desktop', 'build', 'reports', 'tests', 'desktopTest'),
    ('checkout', 'app-desktop', 'build', 'test-results'),
)
BUILDLIKE = re.compile(r'(java|javac|gradle.*|Gradle.*|kotlinc.*|kotlin.*|Kotlin.*|xcodebuild|clang.*|'
    r'gcc.*|g\+\+.*|cc|c\+\+|cc1.*|cmake|ninja|make|gmake|ctest|mvn.*|msbuild|dotnet|pytest.*|cargo|'
    r'rustc|jpackage|jlink|aapt2?|d8|r8|zipalign|adb|emulator.*|qemu-system.*)')
START = time.monotonic()
END, WORK_END, last_watch = START + 6000, START + 5250, 0.0
reasons, resources, churn, directories, images = [], [], 0, {}, {}
child, lock_fd, owned_domain, cancel_written = None, None, None, False
host_policy_admitted = False
cleanup_phase, cleanup_deadline, cleanup_last_resource = None, 0.0, 0.0
signal_epoch, cleanup_signal_epoch, cleanup_reason_snapshot = 0, None, ()
receipt = {'format': 'passvault-linux-desktop-integration24-outer-receipt-v5', 'run_id': RUN,
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

def capture(path, cap=32 * MIB, retain=True, *, cleanup=False):
    if cleanup:
        cleanup_tick()  # Explicit cleanup-only reader; normal capture still refuses work cancellation.
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
            if cleanup:
                cleanup_tick()
            else:
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
    require(host_policy_admitted and HOST_POLICY_ID == 'GUI24_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_ERRNO_V5',
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
    # Fresh GUI24 proposal: explicit fallback8GiB entry and retained3GiB running/phase floor.
    # Root must independently elect current capacity/reserves; no GUI07 fallback
    # or prior namespace/resource authority is inherited by this source.
    required_free = (8 * 1024 ** 3) if entry else (3 * 1024 ** 3)
    require(free >= required_free and available >= total * (0.25 if entry else 0.20), 'resource floor/reserve')
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
    except OSError as error:
        if p.poll() is None:
            # A previously bound original child may lose proc namespace links just
            # before waitpid reports its exit. Never rebind or overlook a live child:
            # only ENOENT/ESRCH may await this same child for at most 100ms.
            require(owned_domain is not None and error.errno in (2, 3),
                    'live original child namespace unreadable')
            require(not child.get('namespace_terminal_wait_attempted', False),
                    'live original child namespace unreadable')
            child['namespace_terminal_wait_attempted'] = True  # Monotone before the original wait.
            child['row']['namespace_terminal_wait_attempted'] = True
            try:
                p.wait(timeout=0.1)
            except subprocess.TimeoutExpired:
                raise RuntimeError('live original child namespace unreadable') from error
            require(p.returncode is not None, 'original child terminal wait incomplete')

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
    require(sum(row['raw_size'] for row in rows) == SOURCE_BINDING['base_raw_bytes']
            and sum(row['checkout_size'] for row in rows) == SOURCE_BINDING['observed_checkout_bytes']
            and sum(row['raw_size'] + 48 + len(str(row['raw_size'])) for row in rows)
                == SOURCE_BINDING['framed_transport_bytes']
            and max(SOURCE_BINDING['base_raw_bytes'], SOURCE_BINDING['observed_checkout_bytes'],
                    SOURCE_BINDING['framed_transport_bytes']) <= 160 * MIB,
            'exact P23 raw/checkout/repeated-OID framing bounds')
    return expected, folders

def oid_requests(expected):
    data = b''.join(row['git_blob'].encode('ascii') + b'\n' for row in expected.values())
    require(len(data) == MEMBERS * 41 and len(data) <= 209961, 'canonical bounded OID input size')  # Exact P23 N*41 writer cap.
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
    actual, original = capture(R / 'source.oids', 209961)  # Exact same P23 N*41 reader cap.
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
    require(original['pin']['bytes'] == SOURCE_BINDING['framed_transport_bytes'], 'exact P23 raw stream size')
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

def source_check(expected, effective=False):
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
    identity = hashlib.sha256(encoded(result)).hexdigest()
    total = sum(row[3] for row in result)
    require(len(result) == MEMBERS
            and identity == SOURCE_BINDING['effective_content_identity_sha256' if effective else 'base_content_identity_sha256']
            and total == SOURCE_BINDING['effective_bytes' if effective else 'base_raw_bytes'],
            'complete exact base/effective source identity mismatch')
    return {'members': len(result), 'content_identity_sha256': identity, 'bytes': total,
            'representation': SOURCE_BINDING['representation'] if effective else 'RAW_GIT_BLOBS',
            'not_checkout_normalized': different}


def apply_approved_desktop_test_overlay(expected):
    # One fixed 57-byte overlay, only in the newly materialized owned checkout.
    # No live source, SOURCE/INDEX, raw stream or other file is changed; no generic patch input.
    name, old, after = SOURCE_OVERLAY['path'], SOURCE_OVERLAY['before'], SOURCE_OVERLAY['after']
    row = expected[name]
    require(row['git_mode'] == SOURCE_OVERLAY['git_mode'] and row['git_blob'] == old['git_blob']
            and row['raw_size'] == old['bytes'] and row['raw_sha256'] == old['sha256'],
            'approved overlay base row mismatch')
    path = R / 'checkout' / name
    data, before_image = capture(path, old['bytes'])
    require(len(data) == old['bytes'] and before_image['sha256'] == old['sha256']
            and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == old['git_blob'],
            'approved overlay exact beforeimage mismatch')
    anchor = (b'val desktopTest = getByName("desktopTest") {\n'
              b'            dependencies {\n'
              b'                implementation(kotlin("test"))\n')
    addition = b'                implementation(libs.lifecycle.viewmodel)\n'
    require(data.count(anchor) == 1 and addition not in data, 'single exact desktopTest-only insertion required')
    changed = data.replace(anchor, anchor + addition, 1)
    require(len(changed) == after['bytes'] and hashlib.sha256(changed).hexdigest() == after['sha256']
            and hashlib.sha1(b'blob ' + str(len(changed)).encode() + b'\0' + changed).hexdigest() == after['git_blob'],
            'approved overlay complete afterimage mismatch before write')
    deadline = min(time.monotonic() + 180, WORK_END)
    parent = under(directory(R / 'checkout'), name.split('/')[:-1])
    fd = None
    try:
        fd = os.open(path.name, os.O_RDWR | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        require(pin(os.fstat(fd)) == before_image['pin']
                == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'overlay original descriptor/name drift')
        view = memoryview(changed)
        while view:
            tick(deadline)
            count = os.write(fd, view)
            require(count > 0, 'zero overlay write')
            view = view[count:]
        os.ftruncate(fd, len(changed))
        os.fsync(fd)
        os.fsync(parent)
        readback, after_image = capture(path, after['bytes'])
        require(readback == changed and after_image['sha256'] == after['sha256']
                and after_image['pin'] == pin(os.fstat(fd))
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
                and all(after_image['pin'][k] == before_image['pin'][k]
                        for k in ('dev', 'ino', 'uid', 'mode', 'nlink')), 'exact same-file overlay readback drift')
    finally:
        try:
            if fd is not None:
                os.close(fd)  # Original descriptor only, once; failure is HOLD, no retry.
        finally:
            os.close(parent)
    effective = dict(expected)
    effective[name] = dict(row, git_blob=after['git_blob'], git_size=after['bytes'], raw_size=after['bytes'],
                           raw_sha256=after['sha256'], checkout_size=after['bytes'], checkout_sha256=after['sha256'])
    effective[name].pop('observed_checkout_pin', None)  # Derived effective row is NOT an observed P23 checkout row.
    return effective, {'format': 'passvault-gui06-single-desktoptest-overlay-v1',
                       'source_binding': SOURCE_BINDING, 'overlay': SOURCE_OVERLAY,
                       'before_image': before_image, 'after_image': after_image,
                       'qualification': 'Exact owned disposable afterimage; base SOURCE/INDEX unchanged; not a new Git tree.'}

def apply_native_unlock_diagnostic_overlay(expected):
    # One exact accepted unlock diagnostic afterimage, only in the newly owned raw checkout.
    # No live source, SOURCE/INDEX, raw stream or other file is changed; no generic patch input.
    name, old, after = DIAGNOSTIC_OVERLAY['path'], DIAGNOSTIC_OVERLAY['before'], DIAGNOSTIC_OVERLAY['after']
    row = expected[name]
    require(row['git_mode'] == DIAGNOSTIC_OVERLAY['git_mode'] and row['git_blob'] == old['git_blob']
            and row['raw_size'] == old['bytes'] and row['raw_sha256'] == old['sha256'],
            'approved overlay base row mismatch')
    path = R / 'checkout' / name
    data, before_image = capture(path, old['bytes'])
    require(len(data) == old['bytes'] and before_image['sha256'] == old['sha256']
            and hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == old['git_blob'],
            'approved overlay exact beforeimage mismatch')
    changed, supplied = capture(DIAGNOSTIC, after['bytes'])
    require(supplied == images[str(DIAGNOSTIC)] and supplied['sha256'] == after['sha256'],
            'bound installed diagnostic source input drift')
    require(len(changed) == after['bytes'] and hashlib.sha256(changed).hexdigest() == after['sha256']
            and hashlib.sha1(b'blob ' + str(len(changed)).encode() + b'\0' + changed).hexdigest() == after['git_blob'],
            'approved overlay complete afterimage mismatch before write')
    deadline = min(time.monotonic() + 180, WORK_END)
    parent = under(directory(R / 'checkout'), name.split('/')[:-1])
    fd = None
    try:
        fd = os.open(path.name, os.O_RDWR | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        require(pin(os.fstat(fd)) == before_image['pin']
                == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'overlay original descriptor/name drift')
        view = memoryview(changed)
        while view:
            tick(deadline)
            count = os.write(fd, view)
            require(count > 0, 'zero overlay write')
            view = view[count:]
        os.ftruncate(fd, len(changed))
        os.fsync(fd)
        os.fsync(parent)
        readback, after_image = capture(path, after['bytes'])
        require(readback == changed and after_image['sha256'] == after['sha256']
                and after_image['pin'] == pin(os.fstat(fd))
                    == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False))
                and all(after_image['pin'][k] == before_image['pin'][k]
                        for k in ('dev', 'ino', 'uid', 'mode', 'nlink')), 'exact same-file overlay readback drift')
    finally:
        try:
            if fd is not None:
                os.close(fd)  # Original descriptor only, once; failure is HOLD, no retry.
        finally:
            os.close(parent)
    effective = dict(expected)
    effective[name] = dict(row, git_blob=after['git_blob'], git_size=after['bytes'], raw_size=after['bytes'],
                           raw_sha256=after['sha256'], checkout_size=after['bytes'], checkout_sha256=after['sha256'])
    effective[name].pop('observed_checkout_pin', None)  # Derived effective row is NOT an observed P23 checkout row.
    return effective, {'format': 'passvault-gui08-native-unlock-diagnostic-overlay-v1',
                       'source_binding': SOURCE_BINDING, 'overlay': DIAGNOSTIC_OVERLAY,
                       'before_image': before_image, 'after_image': after_image,
                       'qualification': 'Exact owned disposable afterimage; base SOURCE/INDEX unchanged; not a new Git tree.'}

def authority(*, cleanup=False):
    reader = cleanup_capture if cleanup else capture
    require(pin(os.fstat(lock_fd)) == request['lock'] == pin(os.stat(LOCK, follow_symlinks=False)), 'original lock changed')
    require(reader(REQUEST, MIB)[1] == request_image and reader(APPROVAL, 65536)[1] == approval_image, 'packet changed under lock')
    for path in tuple(directories):
        directory(path)
    for path in IMAGES:
        require(reader(path)[1] == images[str(path)], 'admitted image changed')

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

def selected_report_images(result):
    # Reuse original GUI evidence; extra bounded XML remains failure evidence, not new selected cases.
    xml_parents = {module: ('checkout', module, 'build', 'test-results', 'desktopTest')
                   for module in ('app-desktop',)}
    selected = {}
    if result['render_attempted']:
        raw, _ = cleanup_capture(E / 'XML-RESULT.json', MIB)
        xml = decode(raw)
        captures = xml['captures']
        require(xml['preserved'] is True and isinstance(captures, list) and len(captures) <= 6
                and xml['preserved_xml_suites'] == len(captures) == result['preserved_xml_suites']
                and xml['declared_cases'] == xml['declared_xml_suites'] == 1
                and xml['declared_test_tasks'] == 1, 'bounded existing GUI XML preservation result')
        for row in captures:
            require(isinstance(row, dict) and set(row) == {'source', 'selected_task', 'original', 'saved'}
                    and isinstance(row['source'], str), 'existing GUI XML image schema')
            source = Path(row['source'])
            module = next((module for module, parts in xml_parents.items()
                           if source.parent == R.joinpath(*parts)), None)
            name = source.name
            require(module is not None and name.startswith('TEST-') and name.endswith('.xml')
                    and row['source'] == str(R.joinpath(*xml_parents[module], name))
                    and row['selected_task'] == ':' + module + ':desktopTest', 'fixed GUI XML source/task')
            parts = (*xml_parents[module], name)
            require(parts not in selected and len(encoded('/'.join(parts))) <= 512,
                    'duplicate/oversized existing GUI XML path')
            selected[parts] = {'relative': '/'.join(parts), 'kind': 'ALREADY_RETAINED_GUI_XML',
                'destination': 'xml/' + module + '--' + name, 'source_image': row['original'],
                'saved_image': row['saved'], 'file_cap': 2 * MIB, 'already_retained': True}
    for case in ('pvu003',):
        raw, image = cleanup_capture(E / (case.upper() + '-RESULT.json'), MIB)
        require(image == result['mainnav_case_results'][case]['result_image'], 'existing Main result changed')
        case_value, roles = decode(raw), set()
        reports = case_value['crash_diagnostics']
        require(isinstance(reports, list) and len(reports) <= 3, 'bounded existing Main crash images')
        for row in reports:
            require(isinstance(row, dict) and set(row) == {'role', 'source', 'original', 'saved'}
                    and row['role'] in ('seed', 'main', 'verify') and row['role'] not in roles
                    and isinstance(row['source'], str), 'existing Main crash image schema')
            role, name = row['role'], Path(row['source']).name
            parts = (case, role, name)
            require(re.fullmatch(r'hs_err_pid[1-9][0-9]{0,9}\.log', name)
                    and row['source'] == str(R.joinpath(*parts)) and parts not in selected,
                    'exact previously retained Main crash source')
            roles.add(role)
            selected[parts] = {'relative': '/'.join(parts), 'kind': 'ALREADY_RETAINED_MAIN_CRASH',
                'destination': case + '-evidence/' + role + '.hs-error.log',
                'source_image': row['original'], 'saved_image': row['saved'],
                'file_cap': MIB, 'already_retained': True}
    ancestors = {parts[:count] for parts in xml_parents.values() for count in range(1, len(parts) + 1)}
    ancestors.update(parts[:count] for parts in AUXILIARY_REPORT_ROOTS for count in range(1, len(parts)))
    return {'files': selected, 'directories': ancestors}


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


def capture_report_source(parts, snapshot, cap=MIB):
    # Bind every containing directory to the original complete snapshot BEFORE
    # reusing cleanup_capture(); never adopt a new parent merely because the name matches.
    for count in range(1, len(parts)):
        prefix = parts[:count]
        fd = directory(R.joinpath(*prefix))
        require(same_dir(pin(os.fstat(fd), True), snapshot[prefix]), 'report parent differs from original snapshot')
    data, image = cleanup_capture(R.joinpath(*parts), cap)
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
        cleanup_tick()
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
        if is_directory and parts in selected['directories']:
            continue  # Exact source/auxiliary ancestors only; siblings gain no waiver.
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
        cleanup_tick()
        data, original = capture_report_source(parts, snapshot)
        destination = 'GENERATED-REPORT-%04d.bin' % (len(copies) - prior_count + 1)
        output(destination, data)  # Existing exclusive flat-E write + file/parent fsync.
        saved_data, saved = cleanup_capture(E / destination, MIB)
        require(saved_data == data and saved['sha256'] == original['sha256']
                and saved['pin']['dev'] == DEVICE['regular_file_device']
                and saved['pin']['mode'] == stat.S_IFREG | 0o600, 'exact durable report copy/readback')
        copies.append({'relative': '/'.join(parts), 'kind': kind, 'destination': destination,
                       'source_image': original, 'saved_image': saved, 'file_cap': MIB, 'already_retained': False})
    manifest = {'format': 'passvault-linux-desktop-integration24-retained-diagnostics-v1', 'run_id': RUN,
        'cache_roots': {'/'.join(parts): value for parts, value in roots.items()}, 'limits': CACHE_REPORT_LIMITS,
        'cache_file_count': cache_files, 'cache_logical_bytes': cache_bytes,
        'auxiliary_roots': {'/'.join(parts): value for parts, value in auxiliary_roots.items()},
        'auxiliary_limits': AUXILIARY_REPORT_LIMITS, 'auxiliary_file_count': auxiliary_files,
        'auxiliary_logical_bytes': auxiliary_bytes,
        'problems_report_limit_bytes': MIB, 'previously_retained_files': prior_count, 'copies': copies,
        'qualification': 'Opaque generated diagnostic bytes conserved outside R, no provenance or test credit.'}
    output('GENERATED-REPORTS.json', encoded(manifest))
    saved_manifest, manifest_image = cleanup_capture(E / 'GENERATED-REPORTS.json', MIB)
    require(saved_manifest == encoded(manifest), 'exact durable generated-report manifest')
    receipt['generated_report_preservation'] = {'manifest': manifest_image, 'copied_files': len(copies) - prior_count,
        'previously_retained_files': prior_count,
        'copied_logical_bytes': sum(row['source_image']['pin']['bytes'] for row in copies if not row['already_retained']),
        'source_and_saved_reverified_before_unlink': False, 'saved_reverified_after_cleanup': False}
    return {'copies': copies, 'manifest_image': manifest_image}


def verify_generated_reports(conserved, snapshot, *, source):
    require(cleanup_capture(E / 'GENERATED-REPORTS.json', MIB)[1] == conserved['manifest_image'], 'retained report manifest changed')
    for row in conserved['copies']:
        cleanup_tick()
        if source:
            require(capture_report_source(tuple(row['relative'].split('/')), snapshot, row['file_cap'])[1] == row['source_image'],
                    'original report changed before first unlink')
        saved_data, saved = cleanup_capture(E / row['destination'], row['file_cap'])
        require(saved == row['saved_image'] and saved['pin']['dev'] == DEVICE['regular_file_device']
                and saved['pin']['mode'] == stat.S_IFREG | 0o600
                and saved['sha256'] == row['source_image']['sha256']
                and len(saved_data) == row['source_image']['pin']['bytes'], 'retained report bytes/pin changed')
    key = 'source_and_saved_reverified_before_unlink' if source else 'saved_reverified_after_cleanup'
    receipt['generated_report_preservation'][key] = True


def remove_runtime(expected, folders, result):
    require(receipt.get('cleanup_eligibility', {}).get('disposal_authorized') is True,
            'no removal before independently established original disposal eligibility')
    cleanup_tick()
    no_runtime_mounts()
    root, snapshot = directory(R), {}
    def inventory(fd, prefix=()):
        for name in os.listdir(fd):
            cleanup_tick()
            require(len(snapshot) < 250000 and (prefix or name in TOP + ['source.oids', 'source.blobs']), 'cleanup count/top-level allowlist')
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
    conserved = preserve_generated_reports(snapshot, expected, folders, selected_report_images(result))
    verify_generated_reports(conserved, snapshot, source=True)
    no_runtime_mounts()  # Recheck immediately before first destructive operation, including same-device bind mounts.
    receipt['cleanup'] = {'status': 'STARTED', 'snapshot_entries': len(snapshot), 'removed': 0}
    for parts in sorted(snapshot, key=lambda p: (-len(p), p)):
        cleanup_tick()
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

def signal_note(number, _frame):
    global signal_epoch
    signal_epoch += 1  # A repeated signal during cleanup is a NEW interruption, not a duplicate note.
    note('signal:' + str(number))


def only_work_signals():
    return bool(reasons) and all(reason in ('signal:1', 'signal:2', 'signal:15') for reason in reasons)


def disposal_policy():
    return {'id': DISPOSAL_POLICY_ID, 'root_selected': True,
        'runtime': str(R), 'retained_evidence': str(E),
        'scope': 'ORIGINAL_SETTLED_PRE_GUI_PREPARE_CANCELLATION_ONLY',
        'materialized_source_copy_disposable_on_failure': True,
        'exclusive_original_runtime_custody_through_cleanup': True,
        'no_unaccounted_runtime_consumer_or_escape': True,
        'unknown_host_identity_mount_and_evidence_failures_remain_hold': True,
        'cleanup_disk_reserve_bytes': CLEANUP_DISK_RESERVE_BYTES,
        'cleanup_ram_reserve_bytes': CLEANUP_RAM_RESERVE_BYTES,
        'cleanup_max_seconds': 750}


def begin_cleanup(mode):
    global cleanup_phase, cleanup_deadline, cleanup_signal_epoch, cleanup_reason_snapshot
    require(cleanup_phase is None and child is None, 'cleanup already entered or original child unsettled')
    require(mode in ('NORMAL_VALIDATION_SAFE', 'PRE_GUI_WORK_CANCELLED'), 'unknown cleanup mode')
    require(only_work_signals() if mode == 'PRE_GUI_WORK_CANCELLED' else not reasons,
            'unknown work/host/custody reason cannot enter cleanup')
    require(request['disposal_policy'] == disposal_policy() and host_policy_admitted,
            'fresh original disposal/custody scope missing')
    cleanup_phase = mode  # Read-only eligibility intake first; this is NOT unlink permission.
    cleanup_deadline = min(END, time.monotonic() + 750)
    cleanup_signal_epoch, cleanup_reason_snapshot = signal_epoch, tuple(reasons)
    receipt['cleanup_eligibility'] = {'mode': mode, 'disposal_authorized': False,
        'original_work_reasons': list(reasons), 'source_acceptance_separate': True,
        'deadline_monotonic_ns': int(cleanup_deadline * 1_000_000_000)}
    cleanup_tick()


def cleanup_tick():
    global cleanup_last_resource
    require(cleanup_phase is not None and child is None
            and signal_epoch == cleanup_signal_epoch and tuple(reasons) == cleanup_reason_snapshot
            and time.monotonic() < cleanup_deadline <= END, 'cleanup interrupted/expired/uncertain')
    require(lock_fd is not None and pin(os.fstat(lock_fd)) == request['lock']
            == pin(os.stat(LOCK, follow_symlinks=False)), 'original cleanup lock changed')
    now = time.monotonic()
    if not cleanup_last_resource or now - cleanup_last_resource >= 5:
        with open('/proc/meminfo', 'rb') as stream:
            raw = stream.read(65537)
        require(len(raw) <= 65536, 'cleanup memory metadata byte bound')
        memory = dict(line.split(':', 1) for line in raw.decode('ascii').splitlines())
        available = int(memory['MemAvailable'].split()[0]) * 1024
        disks = [os.fstatvfs(directory(path)) for path in PARENTS[:2]]
        free = min(value.f_bavail * value.f_frsize for value in disks)
        require(free >= CLEANUP_DISK_RESERVE_BYTES and available >= CLEANUP_RAM_RESERVE_BYTES,
                'reserved cleanup/evidence budget unavailable')
        resources.append({'phase': 'cleanup_only', 'elapsed': round(now - START, 3),
                          'disk_available': free, 'MemAvailable': available})
        cleanup_last_resource = now
    # No host rescan, build-entry floor, source scan, child launch, or clearing of work reasons.


def cleanup_capture(path, cap=32 * MIB, retain=True):
    require(path in (REQUEST, APPROVAL, *IMAGES) or path.is_relative_to(E) or path.is_relative_to(R),
            'cleanup reader outside original controls/evidence/runtime')
    return capture(path, cap, retain, cleanup=True)


def main():
    global lock_fd, parent_ns, request, request_image, approval_image, host_policy_admitted
    require(PROSPECTIVE_ADMISSION_BOUND and type(CLEANUP_DISK_RESERVE_BYTES) is int
            and CLEANUP_DISK_RESERVE_BYTES > 0 and type(CLEANUP_RAM_RESERVE_BYTES) is int
            and CLEANUP_RAM_RESERVE_BYTES > 0, 'SOURCE ONLY: fresh GUI24 identity/resource/disposal admission unbound')
    require(sys.argv == [str(SELF)] and sys.executable == PYTHON and not sys.flags.optimize and sys.flags.isolated and
            sys.flags.no_site and sys.dont_write_bytecode and dict(os.environ) == ENV and os.getuid() == os.geteuid() == 0, 'fixed entry')
    require(all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{40}', value) for value in (COMMIT, TREE))
            and type(MEMBERS) is int and MEMBERS == 5121  # Exact P23 N, not speculative headroom.
            and isinstance(GITDIR, Path) and GITDIR.is_absolute() and '..' not in GITDIR.parts
            and isinstance(DEVICE, dict) and set(DEVICE) == {'directory_device', 'regular_file_device'}
            and all(type(value) is int and value >= 0 for value in DEVICE.values())
            and isinstance(EXPECTED_LOCK, dict)
            and set(EXPECTED_LOCK) == {'dev', 'ino', 'uid', 'mode', 'nlink', 'bytes', 'mtime_ns', 'ctime_ns'}
            and all(type(value) is int for value in EXPECTED_LOCK.values())
            and EXPECTED_LOCK['dev'] == DEVICE['regular_file_device'] and EXPECTED_LOCK['uid'] == 0
            and EXPECTED_LOCK['mode'] == stat.S_IFREG | 0o600
            and EXPECTED_LOCK['nlink'] == 1 and EXPECTED_LOCK['bytes'] == 0
            and all(isinstance(value, str) and re.fullmatch(r'[0-9a-f]{64}', value) for value in FROZEN.values()),
            'PENDING exact P23/effective INTEGRATION24 source/store/tool/device/lock binding: source-only, no execution admission')
    raw, request_image = capture(REQUEST, MIB)
    request = decode(raw)
    raw, approval_image = capture(APPROVAL, 65536)
    approval = decode(raw)
    require(set(request) == {'format', 'run_id', 'purpose', 'commit', 'tree', 'parent_namespaces', 'directories',
            'lock', 'images', 'coordination', 'device_model', 'host_policy',
            'source_binding', 'source_overlay', 'resource_policy', 'disposal_policy'}
            and request['device_model'] == DEVICE, 'request/device schema')
    require(request['disposal_policy'] == disposal_policy(), 'fresh explicit original-copy disposal policy required')
    require(request['format'] == 'passvault-linux-desktop-integration24-request-v5' and request['run_id'] == RUN and
            request['purpose'] == PURPOSE and request['commit'] == COMMIT and request['tree'] == TREE, 'request purpose/source')
    require(request['source_binding'] == SOURCE_BINDING and request['source_overlay'] == SOURCE_OVERLAY
            and request['resource_policy'] == {'id': RESOURCE_POLICY, 'root_selected': True},
            'explicit base/effective source and fresh root resource selection required')
    require(request['coordination'] == {'sole_build_owner': '/root', 'agents_quiescent': True, 'no_ci': True}, 'coordination not admitted')
    check_host_policy(request['host_policy'])
    parent_ns = {k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt', 'net')}
    entry_parent = identity(os.getppid(), ('pid', 'mnt', 'net'))
    require(request['parent_namespaces'] == parent_ns == entry_parent['namespaces'], 'original parent/caller namespace mismatch')
    receipt['parent_at_entry'] = entry_parent  # Provenance only, never signalling/adoption/deletion authority.
    require(set(request['directories']) == {str(p) for p in PARENTS} and set(request['images']) == {str(p) for p in IMAGES}, 'fixed sets')
    require(set(approval) == {'format', 'reviewer', 'disposition', 'purpose', 'run_id', 'request_sha256', 'sources'} and
            approval['format'] == 'passvault-linux-desktop-integration24-approval-v5' and approval['reviewer'] == '/root/current_ledger' and
            approval['disposition'] == 'ACCEPT_EXACT_NEW_INSTANCE' and approval['purpose'] == PURPOSE and approval['run_id'] == RUN and
            approval['request_sha256'] == request_image['sha256'] and approval['sources'] ==
            {str(p): request['images'][str(p)]['sha256'] for p in (SELF, INNER, INIT, SOURCE, DIAGNOSTIC)}, 'missing/stale/nonaccepting genuine review')
    lock_fd = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=directory(LOCK.parent))
    require(pin(os.fstat(lock_fd)) == request['lock'] == EXPECTED_LOCK
            == pin(os.stat(LOCK, follow_symlinks=False)), 'wrong freshly admitted original lock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    receipt['parent_under_lock'] = identity(os.getppid(), ('pid', 'mnt', 'net'))
    require(receipt['parent_under_lock'] == entry_parent, 'original parent changed before locked intake')
    source = None
    for path in IMAGES:
        data, actual = capture(path)
        require(actual == request['images'][str(path)] and actual['sha256'] == FROZEN.get(path, actual['sha256']), 'input image/pin mismatch')
        images[str(path)] = actual
        if path == SOURCE:
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
    allocation = {'format': 'passvault-linux-desktop-integration24-allocation-v1', 'run_id': RUN, 'purpose': PURPOSE,
        'commit': COMMIT, 'tree': TREE, 'source_members': MEMBERS, 'source_representation': 'RAW_GIT_BLOBS',
        'planned_effective_source': SOURCE_BINDING, 'planned_source_overlay': SOURCE_OVERLAY,
        'resource_policy': RESOURCE_POLICY,
        'host_policy_id': HOST_POLICY_ID,
        'allocated_directories': {str(p): directories[p][1] for p in (E, R)},
        'parents': {str(p): directories[p][1] for p in PARENTS}, 'parent_namespaces': parent_ns,
        'packet': {'request': request_image, 'approval': approval_image},
        'sources': {str(p): images[str(p)] for p in (SELF, INNER, INIT, SOURCE, DIAGNOSTIC)}, 'device_model': DEVICE,
        'qualification': 'Original fresh E/R allocation evidence only; NOT materialization, settlement or cleanup admission. No authority for any old or consumed instance.'}
    receipt['allocation'] = allocation  # Retain origin even if the sole permanent allocation-record write fails.
    output('OUTER-ALLOCATION.json', encoded(allocation))
    os.fsync(directory(E.parent))
    os.fsync(directory(R.parent))
    for path in (E / 'logs', E / 'xml', E / 'pvu003-evidence',
                 *(R / p for p in DIRS)):
        directory(path, True)
    oid_data, oid_image = oid_requests(expected)
    receipt['raw_transport'] = {'format': 'git-cat-file-batch-raw-blobs', 'members': len(expected),
        'ordered_request': {str(R / 'source.oids'): oid_image}, 'stdin_original_verified_before_after': False}
    git_env = dict(ENV, HOME=str(R / 'home'), XDG_CONFIG_HOME=str(R / 'xdg-config'), GIT_CONFIG_NOSYSTEM='1',
        GIT_CONFIG_GLOBAL='/dev/null', GIT_OPTIONAL_LOCKS='0', GIT_TERMINAL_PROMPT='0', GIT_NO_REPLACE_OBJECTS='1', GIT_NO_LAZY_FETCH='1')
    command = [GIT, '--git-dir=' + str(GITDIR)]
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
        if isolated and code == 1 and only_work_signals():
            require(not receipt['children'][-1]['pidfd_kill_attempted'], 'forced outer kill is cleanup HOLD')
            begin_cleanup('PRE_GUI_WORK_CANCELLED')
        else:
            require(not reasons and (code in (0, 1) if isolated else code == 0), 'child failure/uncertain finalization')
        readback = cleanup_capture if cleanup_phase is not None else capture
        receipt['children'][-1]['outputs'] = {str(p): readback(p, 160 * MIB if p == R / 'source.blobs' else MIB // 2, False)[1]
                                             for p in (outpath, errpath)}
        if not isolated:
            blob_image = receipt['children'][-1]['outputs'][str(R / 'source.blobs')]
            require(blob_image['pin'] == produced_blob_pin, 'original Git-produced stream changed before capture')
            receipt['raw_transport']['response'] = {str(R / 'source.blobs'): blob_image}
            materialize(expected, folders, blob_image)
            base_before = source_check(expected)
            require(capture(R / 'source.blobs', 160 * MIB, False)[1] == blob_image, 'blob stream changed during materialization')
            receipt['raw_transport']['complete_stream_passes'] = 2
            receipt['base_raw_source_before'] = base_before
            expected, overlay_record = apply_approved_desktop_test_overlay(expected)
            expected, diagnostic_record = apply_native_unlock_diagnostic_overlay(expected)
            overlay_record['diagnostic_overlay'] = diagnostic_record
            before = source_check(expected, effective=True)
            overlay_record.update(base_raw_source=base_before, effective_source=before)
            output('SOURCE-OVERLAY.json', encoded(overlay_record))
            overlay_data, overlay_image = capture(E / 'SOURCE-OVERLAY.json', 65536)
            require(decode(overlay_data) == overlay_record, 'retained overlay provenance mismatch')
            receipt['source_overlay'] = {'record': overlay_image, 'overlay': SOURCE_OVERLAY,
                                         'diagnostic_overlay': DIAGNOSTIC_OVERLAY}
            receipt['source_before'] = before
            authority()
            output('OUTER-INTENT.json', encoded({'format': 'passvault-linux-desktop-integration24-outer-v1', 'run_id': RUN, 'commit': COMMIT, 'tree': TREE,
                'source_representation': SOURCE_BINDING['representation'], 'source_binding': SOURCE_BINDING,
                'source_overlay': SOURCE_OVERLAY, 'overlay_image': overlay_image, 'resource_policy': RESOURCE_POLICY,
                'source_members': MEMBERS, 'parent_namespaces': parent_ns,
                'host_policy_id': HOST_POLICY_ID, 'disposal_policy': request['disposal_policy'],
                'work_deadline_monotonic_ns': int(WORK_END * 1_000_000_000),
                'directories': {str(p): v[1] for p, v in directories.items() if p == R or R in p.parents or p == E or E in p.parents},
                'images': {str(p): images[str(p)] for p in REQUIRED}, 'outer_images': images,
                'packet': {'request': request_image, 'approval': approval_image}, 'device_model': DEVICE,
                'base_raw_source': base_before, 'effective_source': before}))
            command = [UNSHARE, '--mount', '--pid', '--fork', '--kill-child=SIGKILL', '--propagation=private', '--mount-proc=/proc',
                       '--', INNER_PYTHON, '-I', '-B', '-S', str(INNER), parent_ns['pid'], parent_ns['mnt'], parent_ns['net']]
    raw, result_image = readback(E / 'INNER-RESULT.json', MIB)
    result = decode(raw)
    receipt.update(inner_exit=code, inner_result=result_image)
    require(code in (0, 1) and result['format'] == 'passvault-linux-desktop-integration24-inner-v2' and result['run_id'] == RUN and
            result['commit'] == COMMIT and result['tree'] == TREE and result['parent_namespaces'] == parent_ns
            and result['source_binding'] == SOURCE_BINDING and result['source_overlay'] == SOURCE_OVERLAY
            and result['resource_policy'] == RESOURCE_POLICY
            and result['disposal_policy'] == request['disposal_policy'] and all(result[k] is True for k in
            ('source_before', 'all_required_stops_ok', 'namespace_empty_before_exit', 'cleanup_safe', 'evidence_retained')),
            'inner original disposal/lifecycle/evidence proofs incomplete')
    cancelled_cleanup = result['disposal_scope'] == 'PRE_GUI_WORK_CANCELLED'
    if cancelled_cleanup:
        require(cleanup_phase == 'PRE_GUI_WORK_CANCELLED' and code == 1 and only_work_signals()
                and result['work_cancelled_pre_gui'] is True and result['validation_safe'] is False
                and result['validation_mapping_ok'] is False and result['source_after'] is False
                and result['source_after_status'] == 'NOT_CHECKED_WORK_CANCELLED'
                and result['gui_attempted'] is False and result['render_attempted'] is False
                and result['gui_helpers'] == [] and result['original_tray_owners'] == []
                and len(result['phases']) == 1 and result['phases'][0]['phase'] == 'prepare'
                and result['phases'][0]['settled'] is True
                and result['phases'][0]['stop_required'] is True
                and result['phases'][0]['stop_attempted'] is True
                and result['phases'][0]['stop_ok'] is True
                and [c['label'] for c in result['phases'][0]['commands']] == ['prepare', 'prepare-stop']
                and result['phases'][0]['commands'][0]['cancelled_work_only'] is True
                and all(c.get('disposal_command_complete') is True
                        for c in result['phases'][0]['commands']), 'not exact settled pre-GUI cancellation slice')
    else:
        require(cleanup_phase is None and result['disposal_scope'] == 'NORMAL_VALIDATION_SAFE'
                and result['validation_safe'] is True and result['source_after'] is True
                and result['source_after_status'] == 'CHECKED', 'normal source/validation safety incomplete')
    raw, preflight = readback(E / 'INNER-PREFLIGHT.json', 65536)
    value = decode(raw)
    require(value['parent'] == parent_ns and value['self'] == receipt['children'][-1].get('owned_namespaces')
            and value['initial_net'] == parent_ns['net'] and value['nonpropagating_mounts'] is True,
            'original live namespace/preflight binding absent')
    receipt['inner_preflight'] = preflight
    require(type(result['outer_work_deadline_monotonic_ns']) is int
            and result['outer_work_deadline_monotonic_ns'] == int(WORK_END * 1_000_000_000),
            'inner must echo original outer deadline, not reset its remaining budget')
    case_rows = result['mainnav_case_results']
    require(isinstance(case_rows, dict) and set(case_rows) == {'pvu003'},
            'exact single chooser case result required')
    receipt['mainnav_case_results'] = {}
    for case in ('pvu003',):
        row = case_rows[case]
        require(isinstance(row, dict) and set(row) == {'preserved', 'mapping_ok', 'crash_count', 'result_image'}
                and row['preserved'] is True and type(row['mapping_ok']) is bool
                and type(row['crash_count']) is int and 0 <= row['crash_count'] <= 3,
                'Main case preservation schema')
        case_raw, case_image = readback(E / (case.upper() + '-RESULT.json'), MIB)
        case_value = decode(case_raw)
        require(case_image == row['result_image'] and case_value['case'] == case
                and case_value['runtime_directory'] == str(R / case)
                and case_value['evidence_directory'] == str(E / (case + '-evidence'))
                and case_value['declared_cases'] == 1 and case_value['child_roles_planned'] == 3
                and case_value['preserved'] is True and case_value['mapping_ok'] == row['mapping_ok']
                and len(case_value['crash_diagnostics']) == row['crash_count'], 'Main case saved result binding')
        require(case_value['expected_role_files'] == 9
                and case_value['expected_crop_files'] == (5 if case == 'pva027' else 0)
                and type(case_value['crop_header_mapping_ok']) is bool
                and case_value['independent_visual_review'] ==
                    ('REQUIRED_NOT_PERFORMED' if case == 'pva027' else 'NOT_APPLICABLE'),
                'fixed per-case crop/role counts and independent visual-review qualification')
        if case == 'pva027':
            require(isinstance(case_value['crop_headers'], dict) and set(case_value['crop_headers']) == set(PVA027_CROPS),
                    'exact five PVA027 crop structural records')
        if code == 0:
            require(row['mapping_ok'] is True and row['crash_count'] == 0
                    and case_value['required_present'] is True
                    and len(case_value['required_images']) == (14 if case == 'pva027' else 9),
                    'successful Main case requires exact complete role/crop set')
            if case == 'pva027':
                required = {role + suffix for role in ('seed', 'main', 'verify')
                            for suffix in ('.events', '.log', '.exit')} | set(PVA027_CROPS)
                require(set(case_value['required_images']) == required and case_value['crop_header_mapping_ok'] is True
                        and all(row['present'] is True and row['header_and_terminal_geometry_ok'] is True
                                for row in case_value['crop_headers'].values()),
                        'PVA027 automated crop mapping only; independent visual review remains required')
        receipt['mainnav_case_results'][case] = case_image
    require(all(type(result[k]) is bool for k in ('gui_attempted', 'render_attempted', 'net_private_before_gui',
            'tmpfs_private_before_gui', 'gui_helpers_settled', 'mainnav_evidence_preserved'))
            and result['gui_helpers_settled'] is True and result['mainnav_evidence_preserved'] is True
            and isinstance(result['gui_helpers'], list) and len(result['gui_helpers']) <= 4, 'GUI/MainNav safety/result schema')
    if result['gui_attempted']:
        require(result['net_private_before_gui'] is True and result['tmpfs_private_before_gui'] is True,
                'GUI attempted without isolated network/tmpfs proof')
        raw, gui_preflight = readback(E / 'GUI-PREFLIGHT.json', 65536)
        gui = decode(raw)
        require(gui['parent'] == parent_ns and gui['self'] == value['self'] and gui['initial_net'] == parent_ns['net']
                and re.fullmatch(r'net:\[[0-9]+\]', gui['private_net']) and gui['private_net'] != parent_ns['net']
                and gui['loopback_up'] is True and gui['tmpfs_private'] is True and gui['tmpfs_bytes'] == 64 * MIB
                and gui['tmpfs_inodes'] == 4096 and gui['display'] == ':88' and gui['screen'] == '1280x1024x24'
                and gui['authority_path'] == '/tmp/passvault-desktop-integration24/Xauthority', 'GUI private preflight mismatch')
        receipt['gui_preflight'] = gui_preflight
        budget_raw, budget_image = readback(E / 'GUI-BUDGET.json', 65536)
        budget = decode(budget_raw)
        require(budget['outer_work_deadline_monotonic_ns'] == int(WORK_END * 1_000_000_000)
                and type(budget['checked_monotonic_ns']) is int and type(budget['remaining_ns']) is int
                and budget['remaining_ns'] == budget['outer_work_deadline_monotonic_ns'] - budget['checked_monotonic_ns']
                and budget['required_seconds'] == 3500 and budget['accepted'] is True
                and budget['remaining_ns'] >= 3500 * 1_000_000_000
                and budget['allowances_seconds'] == {'render': 2000, 'render_stop': 600,
                    'isolation_gui': 600, 'source_after': 180, 'owned_settlement': 120},
                'GUI launch requires original cooperative remaining-budget receipt')
        receipt['gui_budget'] = budget_image
    else:
        require(code == 1 and result['render_attempted'] is False and result['gui_helpers'] == [],
                'unstarted GUI cannot claim rendering/success/helpers')
    require(not result['render_attempted'] or result['xml_preserved'] is True, 'attempted render evidence not preserved')
    require(result['pva027_independent_visual_review'] == 'NOT_APPLICABLE',
            'automatic run must not claim independent PVA027 visual acceptance')
    receipt['pva027_independent_visual_review'] = result['pva027_independent_visual_review']
    if code == 0:
        require(all(result[k] is True for k in ('gui_attempted', 'render_attempted', 'validation_mapping_ok',
                'panel_configuration_verified_before_launch', 'original_tray_owners_captured_before_test',
                'original_tray_owners_settled', 'xml_mapping_ok', 'mainnav_mapping_ok'))
                and all(type(result[k]) is int for k in ('declared_rendering_cases', 'declared_xml_suites',
                'declared_test_tasks', 'preserved_xml_suites', 'expected_mainnav_files',
                'expected_mainnav_role_files', 'expected_pva027_crop_files',
                'mainnav_child_roles_planned', 'mainnav_crash_diagnostics'))
                and result['declared_rendering_cases'] == 1
                and result['declared_xml_suites'] == result['preserved_xml_suites'] == 1
                and result['declared_test_tasks'] == 1
                and result['expected_mainnav_files'] == 9 and result['expected_mainnav_role_files'] == 9
                and result['expected_pva027_crop_files'] == 0 and result['mainnav_child_roles_planned'] == 3
                and result['mainnav_crash_diagnostics'] == 0,
                'success requires one exact case/one XML suite/one KMP task and one isolated chooser receipt set; '
                'still pending independent reconciliation')
    receipt['gui_scope'] = {k: result[k] for k in ('gui_attempted', 'render_attempted', 'gui_helpers_settled', 'mainnav_evidence_preserved')}
    if cancelled_cleanup:
        receipt['source_after'] = {'status': 'NOT_CHECKED_WORK_CANCELLED', 'validation_accepted': False}
    else:
        require(source_check(expected, effective=True) == before, 'outer complete effective-source after mismatch')
        receipt['source_after'] = {'status': 'CHECKED', 'validation_accepted': True}
    require(readback(E / 'SOURCE-OVERLAY.json', 65536)[1] == overlay_image, 'retained overlay provenance drift before cleanup')
    authority(cleanup=cancelled_cleanup)
    if not cancelled_cleanup:
        watch()
        begin_cleanup('NORMAL_VALIDATION_SAFE')
    cleanup_tick()
    receipt['cleanup_eligibility']['disposal_authorized'] = True
    remove_runtime(expected, folders, result)
    receipt['status'] = ('VALIDATION_CANCELLED_CLEANED' if cancelled_cleanup else
                         'COMPLETED_PENDING_INDEPENDENT_RECONCILIATION' if code == 0 else 'VALIDATION_FAILED_CLEANED')
    return code

if __name__ == '__main__':
    signal.signal(signal.SIGCHLD, signal.SIG_DFL)
    for signum in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
        signal.signal(signum, signal_note)
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
    cleaned_cancellation = (status == 1 and receipt['status'] == 'VALIDATION_CANCELLED_CLEANED'
                            and signal_epoch == cleanup_signal_epoch
                            and tuple(reasons) == cleanup_reason_snapshot)
    sys.exit(status if not reasons or cleaned_cancellation else 70)
