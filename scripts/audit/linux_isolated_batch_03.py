#!/usr/bin/python3
"""Fixed C4 PID1 batch, not admission or a general runner. No old helper imports.

Root separately owns the fresh raw object batch, original allocations/lock, host conflict
checks, exact outer admission, pidfd/wait and disposable-copy cleanup. This
program never materializes source, deletes files, or signals a host PID.
"""
import collections
import csv
import hashlib
import io
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
R = Path('/root/projects/PassVault/audit-runtime-linux-isolated-batch03')
E = W / 'docs/audit-continuation/2026-09-08-linux/runs/linux-isolated-batch03'
CHECKOUT = R / 'checkout'
SELF, INIT = W / 'scripts/audit/linux_isolated_batch_03.py', W / 'scripts/audit/isolated_batch_tests_03.init.gradle'
B = W / 'docs/audit-continuation/2026-09-08-linux/reviews/build-config/current-cycle01'
SOURCE = W / 'docs/audit-continuation/2026-09-08-linux/reviews/current-cycle/SOURCE.json'
CLASSES, METHODS = B / 'CLASSES.tsv', B / 'METHODS.tsv'
JAVA = Path('/usr/lib/jvm/java-17-openjdk-amd64')
INPUTS = (SELF, INIT, SOURCE, CLASSES, METHODS, JAVA / 'bin/java', JAVA / 'release')
COMMIT, TREE = 'da8ff89b9a8579017d5d524e628dff5251f2bb90', 'cf0a8a702e7cd6948236be18b491bb5a21b5886e'
RUN_ID = 'linux-isolated-batch03'
FROZEN = {
    SOURCE: '2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003',
    CLASSES: '95e6ed4f8e5cbb92e4e6840a08e16613b14674555f7f99cd24e34b3ddcd18bda',
    METHODS: '10cb11cd6392e43ab7ec3e94e2f348ccd2caf6cd2127830fe9e1287ea10210be',
    INIT: '6e333500d4a9d56599bd22417d89005d26940240228b1f0710773f5f441d0f45',
}
PHASES = ('ordinary', 'prepare', 'success', 'wrong-key', 'loader-io')
WORKERS = ('database', 'credential', 'shared') + tuple('pva038-' + p for p in PHASES[1:])
CHILDREN = ('home', 'tmp', 'jna', 'sqlite', 'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state')
PRIVATE = ('checkout', 'home', 'tmp', 'jna', 'sqlite', 'gradle-home', 'konan', 'android-user',
           'xdg-cache', 'xdg-config', 'xdg-data', 'xdg-state', 'workers')
ORIGINAL_DIRS = (R, E, E / 'logs', E / 'xml') + tuple(R / p for p in PRIVATE) + tuple(
    p for w in WORKERS for p in (R / 'workers' / w, *(R / 'workers' / w / c for c in CHILDREN)))
BLOCKER = R / 'workers/pva038-loader-io/tmp-blocker'
FIXTURE_RE = r'pva038-v1:[0-9a-f]{104}:[0-9a-f]{48}:[0-9a-f]{104}:[0-9a-f]{48}'
MIB, GIB = 1024 ** 2, 1024 ** 3
FLAGS = ['--no-daemon', '--max-workers=1', '--console=plain', '--no-parallel', '--no-configure-on-demand',
         '--no-configuration-cache', '--no-build-cache', '--dependency-verification=strict', '--stacktrace',
         '-Pkotlin.compiler.execution.strategy=in-process', '-Pandroid.builder.sdkDownload=false',
         '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8', '-Dorg.gradle.java.installations.auto-download=false',
         '-Dorg.gradle.java.installations.auto-detect=false', '-Dorg.gradle.java.installations.paths=' + str(JAVA)]
COMMANDS = {p: [str(CHECKOUT / 'gradlew'), '--init-script', str(INIT), '-Ppassvault.audit.mode=' + p]
            + ([':core:crypto:tasks', '--all', ':core:database:desktopTest', ':feature:credential:desktopTest',
                ':shared:desktopTest'] if p == 'ordinary' else [':core:database:desktopTest', '--offline'])
            + FLAGS for p in PHASES}
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


class Batch:
    def __init__(self, files, parent):
        self.f, self.parent, self.started = files, parent, time.monotonic()
        self.errors, self.phases, self.inputs = [], [], {}
        self.source = None
        self.before = self.after = self.namespace_ok = self.evidence_ok = False
        self.xml_bytes, self.last_resource, self.last_inventory = 0, 0.0, 0.0
        self.settlement_remaining = 120.0
        self.inventories = []

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
        require(value['format'] == 'passvault-linux-isolated-outer-v1' and value['run_id'] == RUN_ID
                and value['commit'] == COMMIT and value['tree'] == TREE and value['source_members'] == 1572
                and value['source_representation'] == 'RAW_GIT_BLOBS' and value['parent_namespaces'] == self.parent,
                'fixed original outer intake (not independent admission)')
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
        require(self.source['commit'] == COMMIT and self.source['tree'] == TREE and len(self.source['files']) == 1572
                and len({r['path'] for r in self.source['files']}) == 1572
                and len(self.source['checkout_eol_qualifications']) == 2, 'full C4/two EOL qualifications')
        require(not os.path.lexists(CHECKOUT / '.git'), 'raw object batch only: no fake/borrowed Git repository')
        data, self.blocker = self.f.read(BLOCKER, 1)
        require(data == b'', 'original zero-file loader blocker')
        self.inputs[BLOCKER] = self.blocker
        return captured

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
                    and bool(image['pin']['mode'] & 0o111) == (row['git_mode'] == '100755'), 'C4 raw blob/mode drift')
            count, size = count + 1, size + len(data)
        require(count == (3 if wrapper_only else 1572), 'source member count')
        if not wrapper_only:
            require(not os.path.lexists(CHECKOUT / '.git'), 'raw object batch only: Git directory appeared')
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

    def command(self, label, argv, env, seconds, phase, stopping=False):
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

    def selections(self, captured):
        classes = list(csv.DictReader(io.StringIO(captured[CLASSES].decode()), delimiter='\t'))
        methods = list(csv.DictReader(io.StringIO(captured[METHODS].decode()), delimiter='\t'))
        require(len(classes) == 14 and len(methods) == 167 and len({tuple(r.values()) for r in methods}) == 167
                and collections.Counter(r['invocation'] for r in methods)
                == {'ordinary': 163, 'prepare': 1, 'success': 1, 'wrong-key': 1, 'loader-io': 1}, 'fixed declarations')
        source_map = {r['path']: r for r in self.source['files']}
        for c in classes:
            require(source_map[c['source_path']]['checkout_sha256'] == c['source_sha256']
                    and sum(m['class_id'] == c['class_id'] for m in methods) == int(c['declared_methods']),
                    'reviewed class source/count binding')
        return {p: [{**c, 'methods': [m['source_method'] for m in methods
                                     if m['class_id'] == c['class_id'] and m['invocation'] == p]}
                    for c in classes if any(m['class_id'] == c['class_id'] and m['invocation'] == p for m in methods)]
                for p in PHASES}

    def preserve_xml(self, phase, classes):
        mode, fixture = phase['phase'], None
        expected = {CHECKOUT / c['expected_xml_path']: c for c in classes}
        observed, captures, mapping = set(), [], True
        for root in sorted({p.parent for p in expected}):
            try:
                names = [n for n in os.listdir(self.f.directory(root)) if n.startswith('TEST-') and n.endswith('.xml')]
            except FileNotFoundError:
                mapping = False
                continue
            require(len(names) <= 32, 'XML file count bound')
            for name in sorted(names):
                path = root / name
                observed.add(path)
                data, original = self.f.read(path, 2 * MIB)
                self.xml_bytes += len(data)
                require(self.xml_bytes <= 32 * MIB, 'aggregate XML bound')
                saved = self.f.write(E / 'xml' / (mode + '-' + name), data)
                captures.append({'source': str(path), 'original': original, 'saved': saved})
                try:
                    require(path in expected and data and b'<!DOCTYPE' not in data and b'<!ENTITY' not in data,
                            'unexpected/empty/declaring XML')
                    c, suite = expected[path], ET.fromstring(data)
                    cases = suite.findall('testcase')
                    require(suite.tag == 'testsuite' and suite.get('name') == c['fqcn'].rsplit('.', 1)[1] + '[desktop]'
                            and collections.Counter((t.get('classname'), t.get('name')) for t in cases)
                            == collections.Counter((c['fqcn'], m + '[desktop]') for m in c['methods']), 'exact case mapping')
                    require(suite.get('tests') == str(len(c['methods'])) and len(cases) == len(c['methods'])
                            and all(suite.get(k) == '0' for k in ('failures', 'errors', 'skipped'))
                            and all(not list(t.findall('failure') + t.findall('error') + t.findall('skipped'))
                                    for t in cases), 'failure/error/skip suite')
                    if mode == 'prepare':
                        lines = [line for out in suite.findall('system-out') for line in (out.text or '').splitlines()
                                 if 'PVA038_FIXTURE=' in line]
                        require(len(lines) == 1 and re.fullmatch('PVA038_FIXTURE=' + FIXTURE_RE, lines[0]),
                                'single exact producer XML fixture')
                        fixture = lines[0][len('PVA038_FIXTURE='):]
                except (RuntimeError, ET.ParseError) as error:
                    mapping = False
                    self.note(mode + ': ' + str(error))
        phase['xml'] = {'captures': captures, 'preserved': True, 'mapping_ok': mapping and observed == set(expected)}
        if not phase['xml']['mapping_ok']:
            self.note(mode + ': missing/extra/failed XML; zero success inferred from compilation')
        if fixture is not None and phase['xml']['mapping_ok']:
            require(len(fixture) == 317, 'fixture length')
            self.f.write(E / 'PVA038-FIXTURE.txt', fixture.encode('ascii') + b'\n')
        return fixture

    def phase(self, mode, classes, fixture):
        state = {'phase': mode, 'commands': [], 'stop_required': False, 'stop_attempted': False, 'stop_ok': False,
                 'build_ok': False, 'settled': False, 'xml': {'preserved': False, 'mapping_ok': False}}
        self.phases.append(state)
        env = dict(ENV)
        try:
            if mode in PHASES[2:]:
                require(isinstance(fixture, str) and len(fixture) == 317 and re.fullmatch(FIXTURE_RE, fixture),
                        'original XML producer fixture required')
                env['PASSVAULT_PVA038_FIXTURE'] = fixture
                state['fixture_sha256'] = sha(fixture.encode('ascii'))
            self.check_inputs()
            require(not self.cancelled() and time.monotonic() - self.started < 6000, 'cancelled/expired phase')
            for worker in WORKERS[:3] if mode == 'ordinary' else ('pva038-' + mode,):
                root = R / 'workers' / worker
                require(set(os.listdir(self.f.directory(root))) == set(CHILDREN)
                        | ({'tmp-blocker'} if worker == 'pva038-loader-io' else set()), 'unused worker root membership')
                require(all(not os.listdir(self.f.directory(root / c)) for c in CHILDREN), 'worker storage not empty')
            self.resources(True)
            require(self.settle(), 'namespace not empty before phase')
            state['build_ok'] = self.command(mode, COMMANDS[mode], env, 3600 if mode == 'ordinary' else 600, state)
        except Exception as error:
            self.note(mode + ': ' + str(error))
        finally:
            if state['stop_required']:
                try:
                    self.check_inputs(stopping=True)  # Retain the original wrapper/JDK stop despite other input drift.
                    self.source_check('STOP', True)
                    state['stop_ok'] = self.command(mode + '-stop', STOP, env, 600, state, True)
                except Exception as error:
                    self.note(mode + ': original stop incomplete/no retry: ' + str(error))
            try:
                state['settled'] = self.settle()
                if state['settled'] and state['stop_required']:
                    fixture = self.preserve_xml(state, classes)
            except Exception as error:
                self.note(mode + ': settlement/XML preservation uncertainty: ' + str(error))
            state['record_image'] = self.f.write(E / ('PHASE-' + mode + '.json'), canonical(state))
        require(state['build_ok'] and state['stop_ok'] and state['settled'] and state['xml']['mapping_ok']
                and not self.cancelled(), mode + ' failed; later phases unstarted, no retry')
        return fixture


def main():
    require(sys.platform == 'linux' and sys.argv[0] == str(SELF) and len(sys.argv) == 3
            and sys.flags.isolated and sys.dont_write_bytecode and sys.flags.no_site, 'fixed isolated absolute entry')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    for sig in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(sig, lambda number, _frame: CANCEL.add(number))
    files = Files()
    batch = Batch(files, dict(zip(('pid', 'mnt'), sys.argv[1:])))
    settled, code = False, 70  # 70 is internal finalization uncertainty, never cleanup authority.
    try:
        captured = batch.intake()
        namespace = namespace_preflight(batch.parent)
        batch.namespace_ok = True
        files.write(E / 'INNER-PREFLIGHT.json', canonical(namespace))
        require(batch.settle(), 'unexpected initial namespace member')
        batch.source_check('BEFORE')
        batch.before = True
        selected, fixture = batch.selections(captured), None
        for mode in PHASES:
            produced = batch.phase(mode, selected[mode], fixture)
            if mode == 'prepare':
                fixture = produced  # Preserve the same XML producer value through all three consumers.
    except Exception as error:
        batch.note(type(error).__name__ + ': ' + str(error))
    finally:
        try:
            settled = batch.settle() if batch.namespace_ok else False
            if settled and batch.before and not batch.cancelled():
                batch.source_check('AFTER')
                batch.after = True
            batch.check_inputs()
            files.verify()
            require(not batch.cancelled(), 'cancelled before final evidence acceptance')
            batch.evidence_ok = True
        except Exception as error:
            batch.note('final evidence/source/namespace uncertainty: ' + str(error))
        stops = all(not p['stop_required'] or (p['stop_attempted'] and p['stop_ok']) for p in batch.phases)
        complete = all('record_image' in p and (not p['stop_required'] or p['xml']['preserved'])
                       and all(c['complete'] and 'log' in c and not c['errors'] for c in p['commands'])
                       for p in batch.phases)
        safe = batch.namespace_ok and batch.before and batch.after and stops and settled and batch.evidence_ok and complete
        safe = safe and not CANCEL
        passed = safe and len(batch.phases) == 5 and all(p['build_ok'] and p['xml']['mapping_ok'] for p in batch.phases)
        result = {'format': 'passvault-linux-isolated-inner-v1', 'run_id': RUN_ID, 'commit': COMMIT, 'tree': TREE,
            'parent_namespaces': batch.parent, 'source_before': batch.before, 'source_after': batch.after,
            'all_required_stops_ok': stops, 'namespace_empty_before_exit': settled, 'cleanup_safe': safe,
            'validation_mapping_ok': passed, 'errors': batch.errors, 'phases': batch.phases,
            'unstarted': [p for p in PHASES if not any(s['phase'] == p and s['stop_required'] for s in batch.phases)],
            'declared_regressions': 166, 'declared_producers': 1, 'independent_semantic_acceptance': False,
            'resource_point_samples_not_ownership_or_cleanup_proof': batch.inventories,
            'incomplete_resource_samples': sum(not s['traversal_finished'] or bool(s['vanished_entries']
                or s['vanished_scan_directories']) for s in batch.inventories),
            'source_references': {'manifest_sha256': FROZEN[SOURCE], 'representation': 'RAW_GIT_BLOBS',
                                  'class_ledger_sha256': FROZEN[CLASSES], 'method_ledger_sha256': FROZEN[METHODS]}}
        try:
            files.write(E / 'INNER-RESULT.json', canonical(result))
            files.verify()
            code = 0 if passed and not batch.errors else 1
        except Exception:
            code = 70  # A previously written JSON cannot attest later finalization failure.
        finally:
            try:
                files.close()
            except Exception:
                code = 70
    os._exit(code)


if __name__ == '__main__':
    main()
