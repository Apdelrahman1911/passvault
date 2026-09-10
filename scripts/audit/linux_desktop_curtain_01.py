#!/usr/bin/python3
"""NEW fixed C5/PVU-005 PID1 source; not execution admission or a general runner.

Copies only reviewed isolated-batch02 source machinery, never imports/runs it.
Root owns fresh source transport, original allocations/lock, outer admission,
pidfd/wait and disposable-copy cleanup. This program never deletes host files
or signals a host PID. All X/session sockets exist only in its private /tmp.
"""
import collections
import hashlib
import json
import os
from pathlib import Path
import re
import resource
import select
import selectors
import signal
import stat
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
R = Path('/root/projects/PassVault/audit-runtime-linux-desktop-curtain01')
E = B / 'runs/linux-desktop-curtain01'
CHECKOUT = R / 'checkout'
SELF, INIT = W / 'scripts/audit/linux_desktop_curtain_01.py', W / 'scripts/audit/desktop_curtain.init.gradle'
SOURCE = B / 'reviews/desktop-curtain/SOURCE.json'
JAVA = Path('/usr/lib/jvm/java-17-openjdk-amd64')
XVFB, XAUTH, WM, DBUS = (Path('/usr/bin/' + n) for n in ('Xvfb', 'xauth', 'xfwm4', 'dbus-daemon'))
MOUNT, IP, XDPYINFO, XPROP = (Path('/usr/bin/' + n) for n in ('mount', 'ip', 'xdpyinfo', 'xprop'))
XFCONF = Path('/usr/lib/x86_64-linux-gnu/xfce4/xfconf/xfconfd')
INPUTS = (SELF, INIT, SOURCE, JAVA / 'bin/java', JAVA / 'release',
          XVFB, XAUTH, WM, DBUS, XFCONF, MOUNT, IP, XDPYINFO, XPROP)
COMMIT, TREE = '96f7758de9984528f4624944c0594d47fc20f14b', '7167789b46cb29b198aed70eb5e21d9eabc4d5ec'
RUN_ID, MEMBERS = 'linux-desktop-curtain01', 1727
FROZEN = {
    SOURCE: '7051258b17dcc93365d1625c8e361612c4ece8bc0b287147e7a83e113451b699',
    INIT: 'dc9d54532ae155a8c5a9ee6d1b54f37b63b4859e34cb4621e9f6eae30a539703',
}
FQCN = 'com.passvault.desktop.security.DesktopCurtainRenderingTest'
METHOD = 'secured locked Compose window shows usable synthetic unlock content after restore'
FIXTURE = 'app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt'
FIXTURE_SHA = '6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57'
XML = CHECKOUT / ('app-desktop/build/test-results/desktopTest/TEST-' + FQCN + '.xml')
PIXELS = ('01-safe-baseline.png', '02-unlocked-marker.png', '03-pre-ack-restore.png',
          '04-post-ack-locked.png', '05-unlocked-removal-control.png', 'observations.txt')
WORKERS = ('desktop-curtain',)
CHILDREN = ('home', 'tmp', 'jna', 'sqlite', 'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
PRIVATE = ('checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state', 'workers')
ORIGINAL_DIRS = (R, E, E / 'logs', E / 'xml', E / 'pixels') + tuple(R / p for p in PRIVATE) + tuple(
    p for w in WORKERS for p in (R / 'workers' / w, *(R / 'workers' / w / c for c in CHILDREN)))
G = Path('/tmp/passvault-desktop-curtain01')
AUTH, BUS = G / 'Xauthority', G / 'runtime/bus'
MIB, GIB = 1024 ** 2, 1024 ** 3
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
COMMANDS = {mode: [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT),
                   '-Ppassvault.audit.desktopCurtainMode=' + mode] + tasks + FLAGS for mode, tasks in (
    ('prepare', [':app-desktop:auditDesktopCurtainPrepare']),
    ('render', [':app-desktop:desktopTest', '--tests', FQCN, '--offline']))}
STOP = [str(CHECKOUT / 'gradlew'), '--stop'] + FLAGS
ENV = {'PATH': str(JAVA / 'bin') + ':/usr/bin:/bin', 'JAVA_HOME': str(JAVA), 'LANG': 'C.UTF-8',
       'LC_ALL': 'C.UTF-8', 'TZ': 'UTC', 'HOME': str(R / 'home'), 'GRADLE_USER_HOME': str(R / 'gradle-home'),
       'KONAN_DATA_DIR': str(R / 'konan'), 'ANDROID_USER_HOME': str(R / 'android-user'),
       'ANDROID_HOME': '/opt/android-sdk', 'ANDROID_SDK_ROOT': '/opt/android-sdk',
       'TMPDIR': str(R / 'tmp'), 'TMP': str(R / 'tmp'), 'TEMP': str(R / 'tmp'), 'SQLITE_TMPDIR': str(R / 'sqlite'),
       **{'XDG_' + k.upper() + '_HOME': str(R / ('xdg-' + k)) for k in ('cache', 'config', 'data', 'state')},
       'JAVA_TOOL_OPTIONS': '-Xmx512m -XX:-UsePerfData -Dfile.encoding=UTF-8 -Duser.home=' + str(R / 'home')
       + ' -Djava.io.tmpdir=' + str(R / 'tmp') + ' -Djna.tmpdir=' + str(R / 'jna')
       + ' -Dorg.sqlite.tmpdir=' + str(R / 'sqlite')}
DISPLAY_ENV = {'DISPLAY': ':88', 'XAUTHORITY': str(AUTH), 'XDG_RUNTIME_DIR': str(G / 'runtime'),
               'DBUS_SESSION_BUS_ADDRESS': 'unix:path=' + str(BUS),
               'DBUS_SYSTEM_BUS_ADDRESS': 'unix:path=' + str(G / 'runtime/no-system-bus')}
GUI_ENV = {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC',
           'HOME': str(G / 'home'), 'TMPDIR': str(G / 'tmp'), 'TMP': str(G / 'tmp'), 'TEMP': str(G / 'tmp'),
           **{'XDG_' + k.upper() + '_HOME': str(G / ('xdg-' + k)) for k in ('cache', 'config', 'data', 'state')},
           **DISPLAY_ENV}
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
        require(len(self.evidence) <= 80 and sum(v['pin']['bytes'] for v in self.evidence.values()) <= 112 * MIB,
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
    initial_net = os.readlink('/proc/self/ns/net')
    require(re.fullmatch(r'net:\[[0-9]+\]', parent['net']) and initial_net == parent['net'],
            'initial online stage must retain the original parent network namespace')
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
    return {'parent': parent, 'self': current, 'initial_net': initial_net, 'nonpropagating_mounts': True, 'mount_rows': len(lines)}


class Curtain:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs, self.helpers = [], [], {}, []
        self.source = self.initial = self.tmpfs_identity = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.last_resource = self.last_inventory = 0.0
        self.settlement_remaining, self.inventories = 120.0, []
        self.gui_attempted = self.render_attempted = self.gui_shutdown_started = False
        self.net_private = self.tmpfs_private = self.gui_settled = False
        self.pixels_preserved = self.pixels_ok = self.xml_preserved = self.xml_ok = False
        self.xml_attempted = False
        self.prepare_ok = self.render_ok = self.gui_readiness = False
        self.orphans = []
        self.private_net = None

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
        value = json.loads(data)
        require(value['format'] == 'passvault-linux-desktop-curtain-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == MEMBERS
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent,
                'fixed original C5 outer intake; not independent execution admission')
        self.f.expected = value['directories']
        for path in ORIGINAL_DIRS:
            require(str(path) in self.f.expected and self.f.expected[str(path)]['uid'] == os.getuid()
                    and self.f.expected[str(path)]['mode'] == stat.S_IFDIR | 0o700, 'original private allocation')
            self.f.directory(path)
        self.inputs[E / 'OUTER-INTENT.json'] = image
        captured = {}
        for path in INPUTS:
            captured[path], actual = self.f.read(path, 32 * MIB)
            require(actual == value['images'][str(path)] and actual['sha256'] == FROZEN.get(path, actual['sha256']),
                    'exact immutable input/tool image')
            self.inputs[path] = actual
        require(b'JAVA_VERSION="17.' in captured[JAVA / 'release']
                and b'OS_ARCH="x86_64"' in captured[JAVA / 'release'], 'JDK17 x86_64')
        self.source = json.loads(captured[SOURCE])
        require(self.source['commit'] == COMMIT and self.source['tree'] == TREE
                and len(self.source['files']) == MEMBERS
                and len({r['path'] for r in self.source['files']}) == MEMBERS
                and len(self.source['checkout_eol_qualifications']) == 2, 'full C5/two EOL qualifications')
        selected = [r for r in self.source['files'] if r['path'] == FIXTURE]
        require(len(selected) == 1 and selected[0]['checkout_sha256'] == FIXTURE_SHA, 'frozen one-case fixture')
        require(not os.path.lexists(CHECKOUT / '.git'), 'disposable raw source only; no borrowed repository')
        require(not os.listdir(self.f.directory(E / 'pixels')), 'original pixels directory must be empty')

    def check_inputs(self, stopping=False):
        for path, image in self.inputs.items():
            if not stopping or path in (JAVA / 'bin/java', JAVA / 'release'):
                require(self.f.read(path, 32 * MIB)[1] == image, 'admitted input drift')

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
                    and bool(image['pin']['mode'] & 0o111) == (row['git_mode'] == '100755'), 'C5 raw blob/mode drift')
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
        self.gui_resources()
        memory = dict(line.split(':', 1) for line in proc_read('/proc/meminfo', 65536).splitlines())
        available, total = (int(memory[k].split()[0]) * 1024 for k in ('MemAvailable', 'MemTotal'))
        disks = [os.fstatvfs(self.f.directory(p)) for p in (R, E)]
        require(min(s.f_bavail * s.f_frsize for s in disks) >= (12 if launch else 8) * GIB
                and available / total >= (0.25 if launch else 0.20), 'disk/RAM floor')
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

    def command(self, label, argv, env, seconds, phase, stopping=False, gradle=True, input_data=None):
        record = {'label': label, 'argv': argv, 'started': False, 'exit': None, 'complete': False, 'errors': []}
        phase['commands'].append(record)
        require(input_data is None or (isinstance(input_data, bytes) and len(input_data) <= 1024), 'bounded command stdin')
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
                'environment_sha256': sha(canonical(env)), 'seconds': seconds, 'stop_required': phase['stop_required'],
                'stdin_sha256': None if input_data is None else sha(input_data)}))
            require(stopping or not self.cancelled(), 'cancel before build launch; original stop retained')
            child = subprocess.Popen(argv, cwd=CHECKOUT, env=env, stdin=subprocess.DEVNULL if input_data is None else subprocess.PIPE, stdout=subprocess.PIPE,
                                     stderr=subprocess.STDOUT, close_fds=True)
            record['started'], record['namespace_pid'] = True, child.pid
            pidfd = os.pidfd_open(child.pid, 0)  # Direct child remains unreaped; never signal a reused numeric PID.
            if input_data is not None:
                require(child.stdin.write(input_data) == len(input_data), 'bounded stdin short write')
                child.stdin.close()
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

    def gui_resources(self):
        # No traversal/adoption of ambient /tmp. This descriptor is opened only
        # after the new mount has been positively established in our namespace.
        if self.tmpfs_identity is not None:
            fd = self.f.directory(Path('/tmp'))
            require(directory_identity(pin(os.fstat(fd), True)) == self.tmpfs_identity, 'private tmpfs drift')
            usage = os.fstatvfs(fd)
            require(usage.f_blocks * usage.f_frsize <= 64 * MIB and usage.f_files <= 4096
                    and usage.f_bavail * usage.f_frsize >= 8 * MIB and usage.f_favail >= 64, 'private GUI tmpfs floor/bound')
        for helper in self.helpers:
            if helper['log_fd'] is not None:
                require(os.fstat(helper['log_fd']).st_size <= 4 * MIB, 'GUI helper log cap')
            if helper['p'] is not None and not self.gui_shutdown_started:
                require(helper['pidfd'] is not None, 'original GUI pidfd missing; no poll before capture')
                if helper['p'].poll() is not None:
                    helper['record']['unexpected_exit'] = True

    def state(self, mode):
        phase = {'phase': mode, 'commands': [], 'stop_required': False, 'stop_attempted': False,
                 'stop_ok': False, 'build_ok': False, 'settled': False}
        self.phases.append(phase)
        return phase

    def stop(self, phase, env):
        if phase['stop_required']:
            try:
                self.check_inputs(stopping=True)
                self.source_check('STOP', True)
                phase['stop_ok'] = self.command(phase['phase'] + '-stop', STOP, env, 600, phase, True)
            except Exception as error:
                self.note(phase['phase'] + ': original wrapper stop incomplete/no retry: ' + str(error))

    def prepare(self):
        phase = self.state('prepare')
        try:
            self.check_inputs()
            require(self.settle() and os.readlink('/proc/self/ns/net') == self.parent['net'],
                    'online preparation needs original net and an empty owned PID namespace')
            self.resources(True)
            phase['build_ok'] = self.command('prepare', COMMANDS['prepare'], ENV, 3600, phase)
        finally:
            self.stop(phase, ENV)
            phase['settled'] = self.settle()
            try:
                if phase['settled'] and phase['build_ok']:
                    data, _ = self.f.read(E / 'logs/prepare.log', 4 * MIB)
                    prefix = 'PVU005_RUNTIME_CLASSPATH='
                    lines = [line[len(prefix):] for line in data.decode('utf-8', errors='replace').splitlines()
                             if line.startswith(prefix)]
                    require(len(lines) == 1 and len(lines[0]) <= 256 * 1024, 'one compact runtime classpath record')
                    value = json.loads(lines[0])
                    require(value['selectedClass'] == FQCN and type(value['entries']) is int
                            and 1 <= value['entries'] <= 512 and len(value['artifacts']) == value['entries'],
                            'runtime classpath identity/count')
                    names = []
                    for row in value['artifacts']:
                        require(isinstance(row['name'], str) and 0 < len(row['name']) <= 512, 'runtime artifact name')
                        if 'sha256' in row:
                            require(re.fullmatch(r'[0-9a-f]{64}', row['sha256']) and type(row['bytes']) is int
                                    and 0 <= row['bytes'] <= 256 * MIB, 'runtime artifact digest/size')
                            names.append(row['name'])
                        else:
                            require(row['directory'] is True and type(row['exists']) is bool, 'NO-SOURCE directory record')
                    require(any(n.startswith('skiko-awt-runtime-linux-x64-') and n.endswith('.jar') for n in names)
                            and any(re.fullmatch(r'jna-[0-9].*\.jar', n) for n in names),
                            'actual Linux Skiko and JNA runtime artifacts must resolve online')
                    phase['runtime_classpath'] = self.f.write(E / 'RUNTIME-CLASSPATH.json', canonical(value))
                    phase['runtime_resolved'] = True
            except Exception as error:
                self.note('prepare runtime record: ' + str(error))
            phase['record_image'] = self.f.write(E / 'PHASE-prepare.json', canonical(phase))
        self.prepare_ok = (phase['build_ok'] and phase['stop_ok'] and phase['settled']
                           and phase.get('runtime_resolved') is True and not self.cancelled())
        require(self.prepare_ok, 'preparation failed; GUI/Test unstarted, no retry')

    def isolation(self):
        phase = self.state('isolation')
        try:
            require(self.prepare_ok and self.settle() and os.readlink('/proc/self/ns/net') == self.parent['net'],
                    'no network transition while prior Gradle workers/channels remain')
            require(hasattr(os, 'unshare') and hasattr(os, 'CLONE_NEWNET'), 'Python network-namespace API missing')
            os.unshare(os.CLONE_NEWNET)
            self.private_net = os.readlink('/proc/self/ns/net')
            require(re.fullmatch(r'net:\[[0-9]+\]', self.private_net) and self.private_net != self.parent['net'],
                    'network transition did not create a distinct namespace')
            require({k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt')} == self.initial['self'],
                    'original PID/mount namespace changed')
            self.net_private = True
            require(self.command('loopback-up', [str(IP), 'link', 'set', 'lo', 'up'], ENV, 20, phase, gradle=False),
                    'private loopback setup failed')
            require(self.command('loopback-readback', [str(IP), 'link', 'show', 'lo'], ENV, 20, phase, gradle=False),
                    'private loopback readback failed')
            data, _ = self.f.read(E / 'logs/loopback-readback.log', 65536)
            flags = re.findall(rb'(?m)^[0-9]+:\s+lo:\s+<([^>]+)>', data)
            require(len(flags) == 1 and b'UP' in flags[0].split(b','), 'private loopback UP flag absent')

            previous_tmp = os.stat('/tmp', follow_symlinks=False)
            require(stat.S_ISDIR(previous_tmp.st_mode), 'original /tmp must be a directory, not an alias')
            argv = [str(MOUNT), '--no-mtab', '--internal-only', '--types', 'tmpfs', '--options',
                    'mode=1777,strictatime,nosuid,nodev,size=67108864,nr_inodes=4096',
                    '--source', 'tmpfs', '--target', '/tmp']
            require(self.command('private-tmpfs', argv, ENV, 20, phase, gradle=False), 'private tmpfs mount failed')
            tmp = os.stat('/tmp', follow_symlinks=False)
            require(tmp.st_dev != previous_tmp.st_dev and tmp.st_uid == 0
                    and tmp.st_mode == stat.S_IFDIR | 0o1777, 'new private tmpfs identity/type/mode')
            rows, device = [], '%s:%s' % (os.major(tmp.st_dev), os.minor(tmp.st_dev))
            for line in proc_read('/proc/self/mountinfo', 131072).splitlines():
                left, separator, right = line.partition(' - ')
                prefix, suffix = left.split(), right.split(' ')
                require(separator and len(prefix) >= 6 and len(suffix) == 3 and suffix[0] and suffix[2],
                        'private tmpfs mountinfo fields')
                require(not any(v.startswith(('shared:', 'master:', 'propagate_from:')) for v in prefix[6:]),
                        'propagating mount after GUI setup')
                if prefix[4] == '/tmp' and prefix[2] == device:
                    require(suffix[0] == 'tmpfs' and {'nosuid', 'nodev'} <= set(prefix[5].split(',')),
                            'private /tmp filesystem/security flags')
                    rows.append(line)
            require(len(rows) == 1, 'one visible new private /tmp mount')
            self.tmpfs_identity = directory_identity(pin(tmp, True))
            fd = self.f.directory(Path('/tmp'))
            require(not os.listdir(fd), 'do not adopt existing private-tmpfs contents')
            self.gui_resources()
            os.mkdir(G.name, 0o700, dir_fd=fd)
            for name in ('home', 'tmp', 'runtime', 'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state'):
                os.mkdir(name, 0o700, dir_fd=self.f.directory(G))
                require(os.fstat(self.f.directory(G / name)).st_mode == stat.S_IFDIR | 0o700, 'private session directory')
            self.tmpfs_private = True
            self.f.write(E / 'GUI-PREFLIGHT.json', canonical({
                'parent': self.parent, 'self': self.initial['self'], 'initial_net': self.parent['net'],
                'private_net': self.private_net, 'loopback_up': True, 'tmpfs_private': True,
                'tmpfs': self.tmpfs_identity, 'mount_row': rows[0], 'tmpfs_bytes': 64 * MIB, 'tmpfs_inodes': 4096,
                'display': ':88', 'screen': '1024x768x24', 'authority_path': str(AUTH),
                'qualification': 'Isolation setup only; no X/WM readiness or test result yet',
            }))
            phase['setup_ok'] = True
        finally:
            phase['settled'] = self.settle()
            phase['record_image'] = self.f.write(E / 'PHASE-isolation.json', canonical(phase))

    @staticmethod
    def helper_limits():
        # Only foreground GUI helpers/descendants inherit this cap, not Gradle
        # or downloaded JARs. Its synthetic file/output failures are setup gaps.
        resource.setrlimit(resource.RLIMIT_FSIZE, (4 * MIB, 4 * MIB))

    def start_helper(self, role, argv):
        require(self.gui_attempted and self.net_private and self.tmpfs_private and not self.cancelled(),
                'GUI helper without admitted private setup')
        path = E / 'logs' / ('helper-' + role + '.log')
        helper = {'p': None, 'pidfd': None, 'log_fd': self.f.new(path), 'path': path,
                  'record': {'role': role, 'argv': argv, 'started': False, 'pidfd_captured': False, 'exit': None,
                             'term_sent': False, 'kill_sent': False, 'unexpected_exit': False}}
        self.helpers.append(helper)
        self.f.write(E / ('helper-' + role + '-INTENT.json'), canonical({
            'argv': argv, 'cwd': str(G), 'environment_sha256': sha(canonical(GUI_ENV)),
            'namespace': {**self.initial['self'], 'net': self.private_net}, 'helper_file_limit': 4 * MIB,
        }))
        helper['p'] = subprocess.Popen(argv, cwd=G, env=GUI_ENV, stdin=subprocess.DEVNULL,
                                       stdout=helper['log_fd'], stderr=subprocess.STDOUT, close_fds=True,
                                       preexec_fn=self.helper_limits)
        helper['record'].update(started=True, namespace_pid=helper['p'].pid)
        helper['pidfd'] = os.pidfd_open(helper['p'].pid, 0)  # Original unreaped direct child, before any poll/wait.
        helper['record']['pidfd_captured'] = True
        return helper

    def live_helpers(self):
        require(1 <= len(self.helpers) <= 3, 'fixed foreground helper set')
        require(all(h['p'] is not None and h['pidfd'] is not None and h['p'].poll() is None for h in self.helpers),
                'original foreground GUI helper exited/daemonized; not readiness')

    def await_socket(self, path):
        deadline = time.monotonic() + 10
        while time.monotonic() < deadline:
            self.live_helpers()
            self.resources()
            require(not self.cancelled(), 'cancelled during GUI startup')
            try:
                value = path.lstat()
                require(stat.S_ISSOCK(value.st_mode) and value.st_uid == 0
                        and value.st_dev == self.tmpfs_identity['dev'], 'unexpected private GUI socket')
                return
            except FileNotFoundError:
                time.sleep(0.1)
        raise RuntimeError('private GUI socket startup deadline')

    def gui_start(self):
        require(self.net_private and self.tmpfs_private and self.prepare_ok, 'GUI isolation prerequisites missing')
        phase = self.state('gui-start')
        self.gui_attempted = True  # Monotone before the first auth/helper command, never on merely attempted isolation.
        try:
            require(not os.path.lexists(AUTH), 'fresh Xauthority path required')
            cookie = os.urandom(16).hex()
            require(self.command('xauthority', [str(XAUTH), 'source', '-'], GUI_ENV, 20, phase, gradle=False,
                                 input_data=('add :88 . ' + cookie + '\n').encode('ascii')), 'fresh Xauthority failed')
            cookie = None  # Synthetic cookie bytes stay out of retained argv/logs/JSON.
            _, auth = self.f.read(AUTH, 4096)
            require(auth['pin']['mode'] == stat.S_IFREG | 0o600, 'private authority permissions')
            self.inputs[AUTH] = auth

            xvfb = self.start_helper('xvfb', [str(XVFB), ':88', '-screen', '0', '1024x768x24',
                                             '-nolisten', 'tcp', '-auth', str(AUTH)])
            self.await_socket(Path('/tmp/.X11-unix/X88'))
            lock, _ = self.f.read(Path('/tmp/.X88-lock'), 64)
            require(lock.strip().isdigit() and int(lock.strip()) == xvfb['p'].pid, 'original Xvfb lock PID mismatch')
            require(self.command('display-readiness', [str(XDPYINFO)], GUI_ENV, 20, phase, gradle=False),
                    'authenticated private display connection failed')
            data, _ = self.f.read(E / 'logs/display-readiness.log', 65536)
            require(re.search(rb'(?m)^\s*number of screens:\s+1\s*$', data)
                    and re.search(rb'(?m)^\s*dimensions:\s+1024x768 pixels\b', data)
                    and re.search(rb'(?m)^\s*depth of root window:\s+24 planes\s*$', data), 'private display geometry/depth')

            self.start_helper('dbus', [str(DBUS), '--session', '--address=unix:path=' + str(BUS),
                                      '--nofork', '--nopidfile'])
            self.await_socket(BUS)
            self.start_helper('wm', [str(WM)])  # Installed Client0_Command; no undocumented foreground/replace flags.
            until = time.monotonic() + 2
            while time.monotonic() < until:
                self.live_helpers()
                self.resources()
                require(not self.cancelled(), 'cancelled while awaiting original WM startup')
                time.sleep(0.1)
            require(self.command('wm-root-readiness', [str(XPROP), '-root', '_NET_SUPPORTING_WM_CHECK'],
                                 GUI_ENV, 20, phase, gradle=False), 'private root WM property query failed')
            data, _ = self.f.read(E / 'logs/wm-root-readiness.log', 65536)
            match = re.fullmatch(rb'_NET_SUPPORTING_WM_CHECK\(WINDOW\): window id # (0x[0-9a-fA-F]+)\s*', data)
            require(match is not None and 0 < int(match[1], 16) <= 0xffffffff, 'supporting-WM root property missing')
            xid = hex(int(match[1], 16))
            require(self.command('wm-self-readiness', [str(XPROP), '-id', xid, '_NET_SUPPORTING_WM_CHECK', '_NET_WM_NAME'],
                                 GUI_ENV, 20, phase, gradle=False), 'supporting-WM self property query failed')
            data, _ = self.f.read(E / 'logs/wm-self-readiness.log', 65536)
            check = re.findall(rb'(?m)^_NET_SUPPORTING_WM_CHECK\(WINDOW\): window id # (0x[0-9a-fA-F]+)\s*$', data)
            require(len(check) == 1 and int(check[0], 16) == int(xid, 16)
                    and re.search(rb'(?mi)^_NET_WM_NAME\((?:UTF8_STRING|STRING)\) = "Xfwm4"\s*$', data),
                    'WM self-check/name mismatch; no PID-only readiness claim')
            self.live_helpers()
            self.f.write(E / 'GUI-READINESS.json', canonical({
                'display': ':88', 'authority': auth, 'supporting_wm_window': xid,
                'original_helper_pids': {h['record']['role']: h['p'].pid for h in self.helpers},
                'display_query_ok': True, 'wm_self_check_ok': True, 'original_foreground_helpers_live': True,
                'qualification': 'Native iconify/restore and rendering still belong to the single actual fixture',
            }))
            self.gui_readiness = True
            phase['setup_ok'] = True
        finally:
            phase['record_image'] = self.f.write(E / 'PHASE-gui-start.json', canonical(phase))

    def namespace_row(self, pid):
        raw = proc_read('/proc/%s/stat' % pid, 32768)
        fields = raw[raw.rfind(')') + 2:].split()
        row = {'pid': pid, 'ppid': int(fields[1]), 'start': int(fields[19])}
        require(pid > 1 and row['ppid'] >= 1 and row['start'] > 0, 'private descendant birth/parent')
        row['namespaces'] = {k: os.readlink('/proc/%s/ns/%s' % (pid, k)) for k in ('pid', 'mnt', 'net')}
        require(row['namespaces'] == {**self.initial['self'], 'net': self.private_net},
                'remaining process is not in the original private GUI domain')
        row['exe'], row['cwd'] = os.readlink('/proc/%s/exe' % pid), os.readlink('/proc/%s/cwd' % pid)
        again = proc_read('/proc/%s/stat' % pid, 32768)
        fields = again[again.rfind(')') + 2:].split()
        require((int(fields[1]), int(fields[19])) == (row['ppid'], row['start']), 'descendant birth drift')
        return row

    def finish_gui(self):
        if self.gui_shutdown_started:
            return self.gui_settled  # Not another signalling/close attempt.
        self.gui_shutdown_started = True
        if not self.namespace_ok:
            return False
        uncertain, empty = False, False
        for helper in self.helpers:
            try:
                if helper['p'] is not None:
                    require(helper['pidfd'] is not None, 'original GUI pidfd missing; no poll before capture')
                    if helper['p'].poll() is not None:
                        helper['record']['unexpected_exit'] = True
            except Exception as error:
                # Even a fast-terminal child cannot repair the missing original
                # pidfd. Namespace settlement below never clears this HOLD.
                uncertain = True
                self.note('direct GUI original wait: ' + str(error))

        # Stop the original WM, bus, then X server. No name/PGID/host-PID kill.
        for sig, field in ((signal.SIGTERM, 'term_sent'), (signal.SIGKILL, 'kill_sent')):
            for helper in reversed(self.helpers):
                try:
                    if helper['p'] is not None:
                        require(helper['pidfd'] is not None, 'original GUI pidfd missing; no numeric fallback')
                        if helper['p'].poll() is None:
                            helper['record'][field] = True  # Monotone before the one signal attempt.
                            self.signal_child(helper['pidfd'], sig)
                except Exception as error:
                    uncertain = True
                    self.note('direct GUI stop: ' + str(error))
            deadline = time.monotonic() + 10
            while time.monotonic() < deadline:
                if all(h['p'] is None or (h['pidfd'] is not None and h['p'].poll() is not None) for h in self.helpers):
                    break
                self.cancelled()  # Cancellation does not skip installed cleanup.
                time.sleep(0.1)

        direct_terminal = all(h['p'] is None or (h['pidfd'] is not None and h['p'].poll() is not None)
                              for h in self.helpers)
        if not direct_terminal:
            uncertain = True
            self.note('original GUI child did not complete its own wait before namespace reaping')

        # Session-bus activation may leave descendants (e.g. xfconfd). Ownership
        # is the positively established, originally empty PID1 domain, not an
        # executable-name match or a host /proc scan. Capture each original
        # pidfd against stable birth/namespace/cwd before signalling once.
        if self.gui_attempted:
            try:
                require({k: os.readlink('/proc/self/ns/' + k) for k in ('pid', 'mnt', 'net')}
                        == {**self.initial['self'], 'net': self.private_net}, 'original GUI namespace changed at cleanup')
                if direct_terminal:
                    try:
                        while os.waitpid(-1, os.WNOHANG)[0]:
                            pass
                    except ChildProcessError:
                        pass
                members = sorted(int(n) for n in os.listdir('/proc') if n.isdigit())
                require(1 in members and len(members) <= 64, 'bounded private GUI descendant set')
                direct = {h['p'].pid for h in self.helpers if h['p'] is not None}
                for pid in members:
                    if pid == 1 or pid in direct:
                        continue
                    fd = None
                    try:
                        original = self.namespace_row(pid)
                        fd = os.pidfd_open(pid, 0)
                        require(self.namespace_row(pid) == original, 'remaining original descendant changed')
                        self.orphans.append({'pidfd': fd, 'record': {**original, 'term_sent': False,
                                                                    'kill_sent': False, 'terminal': False}})
                        fd = None
                    except FileNotFoundError:
                        # A disappearing capture is not signalling/adoption
                        # authority. Final whole-namespace settlement still has
                        # to prove no worker remains.
                        self.orphans.append({'pidfd': None, 'record': {'pid': pid, 'capture_vanished': True}})
                    finally:
                        if fd is not None:
                            os.close(fd)
                for sig, field in ((signal.SIGTERM, 'term_sent'), (signal.SIGKILL, 'kill_sent')):
                    for orphan in self.orphans:
                        fd, row = orphan['pidfd'], orphan['record']
                        if fd is not None and not select.select([fd], [], [], 0)[0]:
                            row[field] = True
                            self.signal_child(fd, sig)
                    deadline = time.monotonic() + 10
                    while time.monotonic() < deadline:
                        if all(o['pidfd'] is None or select.select([o['pidfd']], [], [], 0)[0] for o in self.orphans):
                            break
                        self.cancelled()
                        time.sleep(0.1)
                for orphan in self.orphans:
                    if orphan['pidfd'] is not None:
                        orphan['record']['terminal'] = bool(select.select([orphan['pidfd']], [], [], 0)[0])
            except Exception as error:
                uncertain = True
                self.note('private GUI descendants: ' + str(error))
        try:
            empty = self.settle()
            require(empty, 'owned namespace did not settle after GUI/worker stop')
            for helper in self.helpers:
                row, child = helper['record'], helper['p']
                row['exit'] = None if child is None else child.returncode
                require(child is None or (helper['pidfd'] is not None and row['exit'] is not None),
                        'original helper pidfd/wait incomplete')
                data, _ = self.f.read(helper['path'], 4 * MIB)
                row['log'] = self.f.finish(helper['path'], helper['log_fd'], sha(data), len(data))
                row['settled'] = True
        except Exception as error:
            uncertain = True
            self.note('GUI settlement/log preservation: ' + str(error))
        finally:
            for helper in self.helpers:
                for key in ('pidfd', 'log_fd'):
                    descriptor, helper[key] = helper[key], None
                    if descriptor is not None:
                        try:
                            os.close(descriptor)  # No close retry after uncertainty.
                        except OSError as error:
                            uncertain = True
                            self.note('original helper descriptor close: ' + str(error))
            for orphan in self.orphans:
                descriptor, orphan['pidfd'] = orphan['pidfd'], None
                if descriptor is not None:
                    try:
                        os.close(descriptor)
                    except OSError as error:
                        uncertain = True
                        self.note('original descendant descriptor close: ' + str(error))
        self.gui_settled = not uncertain and empty
        self.f.write(E / 'GUI-CLEANUP.json', canonical({
            'gui_attempted': self.gui_attempted, 'helpers': [h['record'] for h in self.helpers],
            'remaining_original_descendants': [o['record'] for o in self.orphans],
            'namespace_empty': self.gui_settled,
            'qualification': 'Cooperative isolated workload only, not hostile-root/no-escape/hard-interruption proof',
        }))
        return self.gui_settled

    def render(self):
        require(self.gui_readiness, 'no Test before authenticated original GUI readiness')
        phase, env = self.state('render'), dict(ENV, **DISPLAY_ENV)
        try:
            self.check_inputs()
            root = R / 'workers/desktop-curtain'
            require(set(os.listdir(self.f.directory(root))) == set(CHILDREN)
                    and all(not os.listdir(self.f.directory(root / c)) for c in CHILDREN), 'unused synthetic worker root')
            self.live_helpers()
            self.resources(True)
            self.render_attempted = True  # Intent only; XML remains the executed-case authority.
            phase['build_ok'] = self.command('render', COMMANDS['render'], env, 600, phase)
        finally:
            self.stop(phase, env)
            phase['settled'] = self.finish_gui()
            if phase['settled']:
                self.preserve_xml()
            phase['record_image'] = self.f.write(E / 'PHASE-render.json', canonical(phase))
        self.render_ok = phase['build_ok'] and phase['stop_ok'] and phase['settled'] and self.xml_ok

    def preserve_xml(self):
        require(not self.xml_attempted, 'XML preservation already attempted; no retry')
        self.xml_attempted = True
        observed, captures, mapping = set(), [], True
        try:
            names = [n for n in os.listdir(self.f.directory(XML.parent)) if n.startswith('TEST-') and n.endswith('.xml')]
        except FileNotFoundError:
            names = []
        require(len(names) <= 8, 'single-selection XML file count bound')
        for name in sorted(names):
            path = XML.parent / name
            observed.add(path)
            data, original = self.f.read(path, 2 * MIB)
            saved = self.f.write(E / 'xml' / name, data)
            captures.append({'source': str(path), 'original': original, 'saved': saved})
            try:
                require(path == XML and data and b'<!DOCTYPE' not in data and b'<!ENTITY' not in data,
                        'unexpected/empty/declaring XML')
                suite = ET.fromstring(data)
                cases = suite.findall('testcase')
                require(suite.tag == 'testsuite' and suite.get('name') == 'DesktopCurtainRenderingTest[desktop]'
                        and collections.Counter((t.get('classname'), t.get('name')) for t in cases)
                        == collections.Counter({(FQCN, METHOD + '[desktop]'): 1}), 'exact rendering case mapping')
                require(suite.get('tests') == '1' and len(cases) == 1
                        and all(suite.get(k) == '0' for k in ('failures', 'errors', 'skipped'))
                        and all(not list(t.findall('failure') + t.findall('error') + t.findall('skipped'))
                                for t in cases), 'failure/error/skip is not GUI success')
            except (RuntimeError, ET.ParseError) as error:
                mapping = False
                self.note('render XML: ' + str(error))
        self.xml_preserved, self.xml_ok = True, mapping and observed == {XML}
        self.f.write(E / 'XML-RESULT.json', canonical({'captures': captures, 'preserved': True, 'mapping_ok': self.xml_ok}))
        if not self.xml_ok:
            self.note('missing/extra/failed/skipped rendering XML; no success from task counts or compilation')

    def preserve_pixels(self):
        require(self.gui_settled, 'do not adopt/copy pixel evidence while owned workers remain')
        names = set(os.listdir(self.f.directory(E / 'pixels')))
        require(names <= set(PIXELS), 'unexpected pixel-directory member; no traversal/deletion')
        size, images = 0, {}
        for name in sorted(names):
            path = E / 'pixels' / name
            data, image = self.f.read(path, 4 * MIB)
            size += len(data)
            require(size <= 12 * MIB and path not in self.f.evidence, 'pixel aggregate/exclusive evidence bound')
            # Preserve original exact bytes even on test/setup failure. This is
            # neither PNG decoding/visual review nor the fixture's pixel oracle.
            # Child stream close/stable reads alone do not establish durability.
            # Finish the original image, without rewriting/copying its bytes.
            parent = self.f.directory(path.parent)
            fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
            try:
                require(pin(os.fstat(fd)) == image['pin']
                        == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'original pixel reopen drift')
                os.fsync(fd)
                os.fsync(parent)
                require(pin(os.fstat(fd)) == image['pin']
                        == pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'original pixel finalize drift')
            finally:
                try:
                    os.close(fd)  # Exactly once, before evidence adoption/pixels_preserved.
                except OSError as error:
                    raise RuntimeError('original pixel descriptor close uncertain; no retry') from error
            self.f.evidence[path] = image
            images[name] = image
        self.pixels_preserved, self.pixels_ok = True, names == set(PIXELS)
        self.f.write(E / 'PIXEL-RESULT.json', canonical({
            'images': images, 'bytes': size, 'preserved': True, 'all_expected_present': self.pixels_ok,
            'qualification': 'Original synthetic captures only; independent visual review and XML still required',
        }))


def main():
    require(sys.platform == 'linux' and sys.argv[0] == str(SELF) and len(sys.argv) == 4
            and sys.flags.isolated and sys.dont_write_bytecode and sys.flags.no_site
            and os.getuid() == os.geteuid() == 0, 'fixed isolated absolute root entry')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = Curtain(files, dict(zip(('pid', 'mnt', 'net'), sys.argv[1:])))
    settled, code = False, 70
    try:
        batch.intake()
        batch.initial = namespace_preflight(batch.parent)
        batch.namespace_ok = True
        files.write(E / 'INNER-PREFLIGHT.json', canonical(batch.initial))
        require(batch.settle(), 'unexpected initial namespace member')
        batch.source_check('BEFORE')
        batch.before = True
        batch.prepare()
        batch.isolation()
        batch.gui_start()
        batch.render()
    except Exception as error:
        batch.note(type(error).__name__ + ': ' + str(error))
    finally:
        try:
            settled = batch.finish_gui() if batch.namespace_ok else False
            if settled:
                if batch.render_attempted and not batch.xml_attempted:
                    batch.preserve_xml()
                batch.preserve_pixels()
                if batch.before and not batch.cancelled():
                    batch.source_check('AFTER')
                    batch.after = True
            batch.check_inputs()
            files.verify()
            require(not batch.cancelled(), 'cancelled before final evidence acceptance')
            batch.evidence_ok = True
        except Exception as error:
            batch.note('final source/GUI/evidence uncertainty: ' + str(error))
        stops = all(not p['stop_required'] or (p['stop_attempted'] and p['stop_ok']) for p in batch.phases)
        complete = all('record_image' in p and all(c['complete'] and 'log' in c and not c['errors']
                       for c in p['commands']) for p in batch.phases)
        isolated = not batch.gui_attempted or (batch.net_private and batch.tmpfs_private)
        safe = (batch.namespace_ok and batch.before and batch.after and stops and settled and batch.gui_settled
                and batch.evidence_ok and batch.pixels_preserved and complete and isolated and not CANCEL)
        safe = safe and (not batch.render_attempted or batch.xml_preserved)
        passed = (safe and batch.prepare_ok and batch.gui_readiness and batch.render_attempted and batch.render_ok
                  and batch.xml_ok and batch.pixels_ok
                  and all(not h['record']['unexpected_exit'] for h in batch.helpers))
        result = {'format': 'passvault-linux-desktop-curtain-inner-v1', 'run_id': RUN_ID,
            'commit': COMMIT, 'tree': TREE, 'parent_namespaces': batch.parent,
            'source_before': batch.before, 'source_after': batch.after, 'all_required_stops_ok': stops,
            'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'gui_attempted': batch.gui_attempted, 'render_attempted': batch.render_attempted,
            'net_private_before_gui': batch.net_private, 'tmpfs_private_before_gui': batch.tmpfs_private,
            'gui_helpers_settled': batch.gui_settled, 'pixels_preserved': batch.pixels_preserved,
            'gui_helpers': [h['record'] for h in batch.helpers], 'validation_mapping_ok': passed,
            'declared_rendering_cases': 1, 'executed_cases_require_exact_xml': True,
            'xml_preserved': batch.xml_preserved, 'xml_mapping_ok': batch.xml_ok,
            'all_expected_pixels_present': batch.pixels_ok, 'errors': batch.errors, 'phases': batch.phases,
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'fixture_sha256': FIXTURE_SHA,
                                  'init_sha256': FROZEN[INIT], 'representation': 'RAW_GIT_BLOBS'},
            'resource_point_samples_not_ownership_or_cleanup_proof': batch.inventories,
            'independent_semantic_acceptance': False,
            'qualification': 'One Linux synthetic real window only; no full-app/authentication, transient-zero-leak or hardware proof'}
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
