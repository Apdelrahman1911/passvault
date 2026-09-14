#!/usr/bin/env python3
"""SOURCE DRAFT ONLY. Fresh macOS ordinary-library/JNA batch; independent admission required.
No archived validation helper imported, no production signing or biometric prompt.
"""
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import selectors
import shutil
import signal
import stat
import subprocess
import time
import xml.etree.ElementTree as ET

REQUEST = 'docs/audit-continuation/2026-09-08-linux/requests/macos-jna-lifetime-01.json'
SCRIPT = 'scripts/audit/macos_jna_lifetime_validation01.py'
WORKFLOW = '.github/workflows/audit-macos-jna-lifetime01.yml'
CASE = 'com.passvault.desktop.security.biometric.JnaDesktopBiometricNativeLifetimeIntegrationTest'
METHOD = 'closeReservationProtectsRealContextUntilCancellationReturns'
GIB = 1024 ** 3
MIB = 1024 ** 2
PATH = '/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin'


def require(value, reason):
    if not value:
        raise RuntimeError(reason)


def digest(path):
    require(path.is_file() and not path.is_symlink(), 'Expected ordinary file')
    before = path.stat()
    h = hashlib.sha256()
    with path.open('rb') as source:
        while block := source.read(MIB):
            h.update(block)
    after = path.stat()
    require((before.st_dev, before.st_ino, before.st_size, before.st_mtime_ns) ==
            (after.st_dev, after.st_ino, after.st_size, after.st_mtime_ns), 'File changed during read')
    return {'bytes': after.st_size, 'sha256': h.hexdigest()}


def exact_directory(value):
    p = Path(value)
    require(p.is_absolute() and str(p) == value and p.resolve() == p and p.is_dir(), 'Exact directory required')
    return p


class Batch:
    def __init__(self):
        self.root = self.evidence = self.checkout = None
        self.active = None
        self.groups = []
        self.stop_owed = False
        self.stop_attempted = False
        self.stop_ok = False
        self.cancelled = False
        self.cleanup_started = False
        self.source = {}
        self.result = {'status': 'NOT_STARTED', 'commands': [], 'cases': 0, 'cleanup': 'NOT_ALLOCATED'}
        self.started = time.monotonic()
        self.env = {'PATH': PATH, 'LANG': 'en_US.UTF-8', 'LC_ALL': 'en_US.UTF-8', 'TZ': 'UTC'}

    def interrupt(self, _signum, _frame):
        self.cancelled = True  # Existing command loop owns termination; no asynchronous file deletion.

    def small(self, argv):
        # Public host/Git metadata only. No shell or inherited credential environment.
        return subprocess.check_output(argv, env=self.env, cwd=self.workspace, timeout=20).decode().strip()

    def resources(self, entry=False):
        free = shutil.disk_usage(self.root or self.workspace).free
        require(free >= (12 if entry else 8) * GIB, 'Disk admission/running floor')
        memory = int(self.small(['/usr/sbin/sysctl', '-n', 'hw.memsize']))
        vm = self.small(['/usr/bin/vm_stat'])
        page = int(re.search(r'page size of (\d+) bytes', vm)[1])
        values = dict((name, int(value)) for name, value in re.findall(r'^([^:]+):\s+(\d+)\.', vm, re.M))
        # Conservative explicit free+inactive statistic, not Linux MemAvailable or physical-security proof.
        available = page * (values['Pages free'] + values['Pages inactive'])
        require(available >= memory * (0.25 if entry else 0.20), 'Memory admission/running floor')
        self.result['last_resources'] = {'disk_free': free, 'memory_total': memory, 'free_plus_inactive': available}

    def command(self, label, argv, seconds, stopping=False):
        require(self.active is None and (stopping or not self.cancelled), 'Original child/cancellation guard')
        record = {'label': label, 'argv': argv, 'exit': None}
        self.result['commands'].append(record)
        log = self.evidence / (label + '.log')
        p = subprocess.Popen(argv, cwd=self.checkout or self.workspace, env=self.env,
                             stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                             start_new_session=True, close_fds=True)
        self.active = p
        self.groups.append(p.pid)
        record['pid'] = p.pid
        started = time.monotonic()
        count = 0
        last_resource = 0
        terminated = False
        try:
            os.set_blocking(p.stdout.fileno(), False)
            with log.open('xb') as output, selectors.DefaultSelector() as selector:
                selector.register(p.stdout, selectors.EVENT_READ)
                eof = False
                while not eof:
                    for key, _ in selector.select(0.2):
                        data = os.read(key.fileobj.fileno(), 65536)
                        if not data:
                            eof = True
                            selector.unregister(key.fileobj)
                        else:
                            count += len(data)
                            require(count <= 8 * MIB, 'Command log cap')
                            output.write(data)
                    now = time.monotonic()
                    require(now - started < seconds, 'Command deadline')
                    require(stopping or (not self.cancelled and now - self.started < 2700), 'Batch cancelled/deadline')
                    if not stopping and now - last_resource > 10:
                        self.resources()
                        last_resource = now
            record['exit'] = p.wait(timeout=10)
        except BaseException:
            # Direct child is still unreaped unless normal wait completed. Never signal old/reused groups.
            if p.returncode is None:
                os.killpg(p.pid, signal.SIGTERM)
                try:
                    p.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(p.pid, signal.SIGKILL)
                    p.wait(timeout=10)
                terminated = True
            raise
        finally:
            if p.stdout is not None:
                p.stdout.close()
            self.active = None if p.returncode is not None else p
            record.update(exit=p.returncode, terminated=terminated, elapsed_seconds=time.monotonic() - started)
            if log.exists():
                record['log'] = digest(log)
        require(record['exit'] == 0, 'Nonzero original command: ' + label)

    def start(self):
        self.workspace = exact_directory(os.environ['PV_WORKSPACE'])
        parent = exact_directory(os.environ['PV_RUNNER_TEMP'])
        require(platform.system() == 'Darwin' and platform.machine() == 'x86_64', 'macOS Intel only')
        require(os.environ['PV_REF'] == 'refs/heads/codex/audit-continuation-linux-20260908'
                and os.environ['PV_ATTEMPT'] == '1', 'Dedicated branch, first attempt only')
        require(re.fullmatch(r'[1-9][0-9]+', os.environ['PV_RUN_ID']), 'Run identity')
        request = json.loads((self.workspace / REQUEST).read_text())
        require(request['scope'] == 'MACOS_JNA_LIFETIME01' and request['non_publishing'] is True
                and request['review_status'] == 'INDEPENDENTLY_ACCEPTED', 'Fresh exact request required')
        require(self.small(['/usr/bin/git', 'rev-parse', 'HEAD']) == os.environ['PV_SHA'], 'Exact activation commit')
        require(self.small(['/usr/bin/git', 'rev-parse', 'HEAD^']) == request['source_commit'], 'Exact source parent')
        require(self.small(['/usr/bin/git', 'diff', '--name-only', 'HEAD^', 'HEAD']) == REQUEST,
                'Activation commit may add only this request')
        for name in (SCRIPT, WORKFLOW):
            require(digest(self.workspace / name) == request['controls'][name], 'Reviewed control identity')
        self.resources(entry=True)
        require(re.fullmatch(r'/[A-Za-z0-9_./-]+', str(parent)), 'Unambiguous private Gradle marker path')
        stem = 'passvault-macos-jna01-' + os.environ['PV_RUN_ID']
        self.root = parent / (stem + '-runtime')
        self.evidence = parent / (stem + '-evidence')
        require(not self.root.exists() and not self.evidence.exists(), 'Unconsumed original paths')
        self.evidence.mkdir(mode=0o700)
        self.root.mkdir(mode=0o700)
        self.root_identity = (self.root.stat().st_dev, self.root.stat().st_ino)
        with open(os.environ['PV_OUTPUT'], 'a') as output:
            output.write('evidence_path=' + str(self.evidence) + '\n')
        self.result.update(status='ALLOCATED', activation=os.environ['PV_SHA'], source_commit=request['source_commit'])
        self.checkout = self.root / 'checkout'
        self.checkout.mkdir()
        # Raw committed blobs avoid checkout-EOL transformations (.bat/.cmd/.ps1).
        rows = self.small(['/usr/bin/git', 'ls-tree', '-r', '-l', '-z', 'HEAD']).split('\0')
        entries = []
        total = 0
        for row in rows:
            if not row:
                continue
            metadata, name = row.split('\t', 1)
            mode, kind, blob, size = metadata.split()
            require(kind == 'blob' and mode in ('100644', '100755') and re.fullmatch(r'[0-9a-f]{40}', blob),
                    'Only ordinary committed source blobs')
            size = int(size)
            require(0 <= size <= 32 * MIB, 'Source member bound')
            require(not name.startswith('/') and '..' not in Path(name).parts and '.git' not in Path(name).parts,
                    'Safe source relative path')
            total += size
            require(total <= 256 * MIB and len(entries) < 16000, 'Finite source envelope')
            entries.append((mode, blob, size, name))
        # Known complete ls-tree size envelope bounds this trusted local Git response.
        blobs = subprocess.check_output(['/usr/bin/git', '-c', 'core.hooksPath=/dev/null',
                                         '-c', 'core.fsmonitor=false', 'cat-file', '--batch'],
                                        input=''.join(e[1] + '\n' for e in entries).encode(),
                                        env=self.env, cwd=self.workspace, timeout=60)
        require(len(blobs) <= total + len(entries) * 128, 'Raw source stream bound')
        cursor = 0
        for mode, blob, size, name in entries:
            newline = blobs.index(b'\n', cursor)
            require(blobs[cursor:newline] == (blob + ' blob ' + str(size)).encode(), 'Exact Git response frame')
            cursor = newline + 1
            data = blobs[cursor:cursor + size]
            require(len(data) == size and blobs[cursor + size:cursor + size + 1] == b'\n', 'Complete source body')
            cursor += size + 1
            require(hashlib.sha1(b'blob ' + str(size).encode() + b'\0' + data).hexdigest() == blob,
                    'Source body equals reviewed Git object')
            expected = {'bytes': size, 'sha256': hashlib.sha256(data).hexdigest()}
            target = self.checkout / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            target.chmod(0o700 if mode == '100755' else 0o600)
            require(digest(target) == expected, 'Copied raw source identity')
            self.source[name] = expected
        require(cursor == len(blobs), 'No trailing source bytes')
        del blobs
        self.result['source_inventory_sha256'] = hashlib.sha256(
            json.dumps(self.source, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
        java = exact_directory(os.path.realpath(os.environ['PV_JAVA_HOME_17']))
        require(not any(c.isspace() for c in str(self.root) + str(java)), 'Unambiguous private JVM option paths')
        require(shutil.rmtree.avoids_symlink_attacks, 'Descriptor-safe private tree removal required')
        release = (java / 'release').read_text()
        require(re.search(r'^JAVA_VERSION="17\.', release, re.M) and 'OS_ARCH="x86_64"' in release, 'JDK17x64')
        self.result['jdk_release'] = digest(java / 'release')
        for name in ('home', 'tmp', 'gradle', 'konan', 'worker-home', 'worker-tmp', 'data', 'artifact'):
            (self.root / name).mkdir(mode=0o700)
        self.env.update(PATH=str(java / 'bin') + ':' + PATH, JAVA_HOME=str(java), HOME=str(self.root / 'home'),
                        TMPDIR=str(self.root / 'tmp'), GRADLE_USER_HOME=str(self.root / 'gradle'),
                        KONAN_DATA_DIR=str(self.root / 'konan'), CMAKE_BUILD_PARALLEL_LEVEL='1',
                        JAVA_TOOL_OPTIONS='-XX:-UsePerfData -XX:ActiveProcessorCount=1 -Duser.home=' +
                        str(self.root / 'home') + ' -Djava.io.tmpdir=' + str(self.root / 'tmp') +
                        ' -Djna.tmpdir=' + str(self.root / 'tmp'))
        if os.environ.get('PV_ANDROID_HOME'):
            sdk = exact_directory(os.path.realpath(os.environ['PV_ANDROID_HOME']))
            self.env.update(ANDROID_HOME=str(sdk), ANDROID_SDK_ROOT=str(sdk))
        self.flags = ['--no-daemon', '--max-workers=1', '--no-parallel', '--no-configure-on-demand',
                      '--no-configuration-cache', '--no-build-cache', '--console=plain',
                      '--dependency-verification=strict', '-Pkotlin.compiler.execution.strategy=in-process',
                      '-Pandroid.builder.sdkDownload=false', '-Dorg.gradle.java.installations.auto-download=false',
                      '-Dorg.gradle.jvmargs=-Xmx2g -XX:ActiveProcessorCount=1 -XX:-UsePerfData']

    def run(self):
        native = self.checkout / 'app-desktop/native/biometric-bridge'
        build = self.root / 'native-build'
        cmake = shutil.which('cmake', path=PATH)
        require(cmake, 'Existing CMake required; no tool provisioning')
        self.command('cmake-version', [cmake, '--version'], 20)
        self.command('xcode-version', ['/usr/bin/xcodebuild', '-version'], 20)
        self.command('java-version', [self.env['JAVA_HOME'] + '/bin/java', '-version'], 20)
        self.command('native-configure', [cmake, '-S', str(native), '-B', str(build), '-DBUILD_TESTING=OFF',
                     '-DCMAKE_BUILD_TYPE=Release', '-DCMAKE_OSX_ARCHITECTURES=x86_64'], 120)
        self.command('native-build', [cmake, '--build', str(build), '--target', 'passvault_biometric', '--parallel', '1'], 240)
        library = self.root / 'artifact/libpassvault_biometric.dylib'
        shutil.copyfile(build / library.name, library)
        self.result['native_library'] = digest(library)
        self.command('native-architecture', ['/usr/bin/lipo', '-archs', str(library)], 20)
        require((self.evidence / 'native-architecture.log').read_text().strip() == 'x86_64', 'Exact native architecture')
        self.require_settled()
        require(build.parent == self.root and build.resolve() == build, 'Exact owned native output')
        shutil.rmtree(build)  # Only no-longer-needed CMake objects; copied library remains bound for JNA.
        self.result['native_objects_removed'] = True
        values = {'enabled': '1', 'target': 'macos-x64', 'abi': '1', 'library': str(library),
                  'libraryBytes': str(self.result['native_library']['bytes']),
                  'librarySha256': self.result['native_library']['sha256'], 'dataDirectory': str(self.root / 'data'),
                  'workerHome': str(self.root / 'worker-home'), 'workerTemporaryDirectory': str(self.root / 'worker-tmp')}
        init = self.root / 'scope.gradle'
        init_text = '''import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
gradle.taskGraph.whenReady { graph ->
    def tests = graph.allTasks.findAll { it instanceof Test }
    if (tests.size() != 1 || tests[0].path != ':app-desktop:auditJnaLifetimeTest')
        throw new GradleException('Exact one-case JNA Test graph required')
    if (graph.allTasks.any { it instanceof Exec || it instanceof JavaExec ||
        it.name.toLowerCase().matches('.*(publish|sign|distributable|package|konan|ios|upload).*') })
        throw new GradleException('Unexpected native/provisioning/publication task')
    tests[0].doFirst {
        def marker = new File('__ORIGINAL_ENTERED_MARKER__')
        if (!marker.createNewFile()) throw new GradleException('Original task marker already exists')
        marker.text = 'ORIGINAL_JNA_TASK_ENTERED\\n'
    }
    println('PVA010_JNA_GRAPH_TASKS=' + graph.allTasks.size())
}
'''
        init.write_text(init_text.replace('__ORIGINAL_ENTERED_MARKER__', str(self.root / 'jna-task-entered.txt')))
        self.stop_owed = True  # Before launch; original stop also owed on launch/validation failure.
        self.command('jna-lifetime', [str(self.checkout / 'gradlew'), '--init-script', str(init), ':app-desktop:auditJnaLifetimeTest',
                     *self.flags, *['-Ppassvault.audit.jnaLifetime.' + k + '=' + v for k, v in values.items()]], 2100)
        xml = self.checkout / ('app-desktop/build/test-results/auditJnaLifetimeTest/TEST-' + CASE + '.xml')
        require(xml.stat().st_size <= MIB, 'Compact test XML')
        require('PVA010_REAL_JNA_LIFETIME_PASS target=macos-x64 abi=1 retrieve=5 cancel=0 destroy=1'
                in xml.read_text(), 'Final native/JNA settlement marker required')
        tree = ET.parse(xml)
        cases = tree.findall('.//testcase')
        require(len(cases) == 1 and cases[0].get('classname') == CASE and
                cases[0].get('name') in (METHOD, METHOD + '[desktop]') and
                not any(cases[0].find(k) is not None for k in ('failure', 'error', 'skipped')), 'One actual passing JNA case')
        require(digest(library) == self.result['native_library'], 'Library after-image')
        self.result.update(status='ONE_CASE_PASSED_PENDING_CLEANUP', cases=1)

    def require_settled(self):
        # Hosted runner is cooperative, not an adversarial same-user sandbox. Do not print other commands.
        rows = self.small(['/bin/ps', '-axo', 'pid=,pgid=,command=']).splitlines()
        for row in rows:
            parts = row.split(None, 2)
            require(len(parts) == 3, 'Process observation incomplete')
            require(int(parts[1]) not in self.groups and str(self.root) not in parts[2],
                    'Run-owned or possible escaped worker live; HOLD, no unrelated signal')

    def finish(self):
        self.cleanup_started = True
        if self.root is None or not hasattr(self, 'root_identity'):
            self.result['cleanup'] = 'NOT_ALLOCATED_OR_PARTIAL_ALLOCATION_HOLD'
            return
        retention_error = None
        try:
            results = self.checkout / 'app-desktop/build/test-results/auditJnaLifetimeTest'
            if results.is_dir():
                files = list(results.glob('TEST-*.xml'))
                require(len(files) <= 2, 'Exact compact XML cohort')
                for p in files:
                    require(p.stat().st_size <= MIB, 'XML retention cap')
                    shutil.copyfile(p, self.evidence / p.name)
        except BaseException as failure:
            retention_error = type(failure).__name__ + ': ' + str(failure)
            self.result['retention_error'] = retention_error
        try:
            # Retention failure must never suppress the one original stop attempt.
            if self.stop_owed and not self.stop_attempted:
                self.stop_attempted = True
                self.command('original-gradle-stop', [str(self.checkout / 'gradlew'), '--stop', *self.flags], 180, stopping=True)
                self.stop_ok = True
            require(self.active is None and (not self.stop_owed or self.stop_ok), 'Original settlement/stop uncertain')
            self.require_settled()
            require(retention_error is None, 'Evidence retention failed; preserve private runtime')
            jna_log = self.evidence / 'jna-lifetime.log'
            native_evidence = jna_log.read_text(errors='replace') if jna_log.is_file() else ''
            for xml in self.evidence.glob('TEST-*.xml'):
                native_evidence += xml.read_text(errors='replace')
            require('HOLD:' not in native_evidence, 'Native fixture requested HOLD; retain original runtime')
            entered = self.root / 'jna-task-entered.txt'
            if os.path.lexists(entered):
                require(entered.is_file() and not entered.is_symlink() and
                        entered.read_text() == 'ORIGINAL_JNA_TASK_ENTERED\n', 'Original task marker uncertain; HOLD')
                self.result['native_task_entered'] = True
                require('PVA010_REAL_JNA_LIFETIME_CLEANUP_SETTLED' in native_evidence,
                        'Native test may have started without settlement evidence; retain runtime')
            for name, expected in self.source.items():
                require(digest(self.checkout / name) == expected, 'Source drift; preserve private runtime for review')
            require(self.root.resolve() == self.root and (self.root.stat().st_dev, self.root.stat().st_ino) == self.root_identity,
                    'Original private runtime identity')
            require(sum(p.stat().st_size for p in self.evidence.iterdir() if p.is_file()) <= 32 * MIB, 'Evidence aggregate')
            # Only this newly allocated source copy/cache/output tree; never checkout, SDK, toolchains or shared caches.
            shutil.rmtree(self.root)
            self.result['cleanup'] = 'ORIGINAL_PRIVATE_RUNTIME_REMOVED'
        except BaseException as failure:
            self.result['cleanup'] = 'HOLD_NOT_CLEANED'
            self.result['cleanup_error'] = type(failure).__name__ + ': ' + str(failure)
        finally:
            self.result.update(stop_attempted=self.stop_attempted, stop_ok=self.stop_ok,
                               elapsed_seconds=time.monotonic() - self.started)
            if self.evidence is not None and self.evidence.is_dir():
                (self.evidence / 'RESULT.json').write_text(json.dumps(self.result, indent=2) + '\n')


batch = Batch()
for sig in (signal.SIGINT, signal.SIGTERM):
    signal.signal(sig, batch.interrupt)
try:
    batch.start()
    batch.run()
except BaseException as failure:
    batch.result.update(status='FAILED', error=type(failure).__name__ + ': ' + str(failure))
finally:
    batch.finish()
raise SystemExit(0 if batch.result['status'] == 'ONE_CASE_PASSED_PENDING_CLEANUP'
                 and batch.result['cleanup'] == 'ORIGINAL_PRIVATE_RUNTIME_REMOVED' else 1)
