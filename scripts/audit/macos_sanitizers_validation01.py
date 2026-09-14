#!/usr/bin/env python3
"""Fresh, nonpublishing macOS ASan/UBSan batch. Requires exact reviewed request.
No Gradle/JNA/provider/signing operation. Five existing CTests, new instrumentation.
"""
import hashlib
import json
import os
from pathlib import Path
import re
import resource
import shutil
import signal
import stat
import subprocess
import time
import xml.etree.ElementTree as ET

REQUEST = 'docs/audit-continuation/2026-09-08-linux/requests/macos-sanitizers-01.json'
CONTROLLER = 'scripts/audit/macos_sanitizers_validation01.py'
WORKFLOW = '.github/workflows/audit-macos-sanitizers01.yml'
PREFIX = 'app-desktop/native/biometric-bridge/'
FILES = ('CMakeLists.txt', 'include/passvault_biometric.h',
         'src/macos/passvault_biometric_macos.mm',
         'tests/macos/passvault_biometric_macos_security_test.mm',
         'tests/passvault_biometric_abi_test.cpp')
CASES = {'passvault_biometric_abi', 'passvault_biometric_macos_security',
         'passvault_biometric_macos_fixture_normal',
         'passvault_biometric_macos_fixture_early_return',
         'passvault_biometric_macos_fixture_cpp_exception'}
FLAGS = '-fsanitize=address,undefined -fno-sanitize-recover=all -fno-omit-frame-pointer -g'
started = time.monotonic()
cancelled = False
active = None
settled = True
allocated = False
evidence_created = False
last_memory = 0.0
memory_total = None
root_identity = None
result = {'status': 'UNSTARTED', 'cases': None, 'commands': [], 'cleanup': 'NOT_ALLOCATED',
          'scope': 'ASan+UBSan existing native paths only; no TSan, leak, provider, physical biometric, JNA or packaged-loader evidence.',
          'gradle_stop': 'NOT_APPLICABLE_NO_GRADLE'}

def require(value, message):
    if not value:
        raise RuntimeError(message)

def sha(data):
    return hashlib.sha256(data).hexdigest()

def ordinary(path, cap=2*1024**2):
    require(path.resolve() == path and not path.is_symlink(), 'Noncanonical input')
    before = path.stat()
    require(stat.S_ISREG(before.st_mode) and before.st_size <= cap, 'Input type/size')
    data = path.read_bytes()
    after = path.stat()
    fields = ('st_dev', 'st_ino', 'st_mode', 'st_uid', 'st_size', 'st_mtime_ns', 'st_ctime_ns')
    require(all(getattr(before, key) == getattr(after, key) for key in fields), 'Input changed during read')
    return data

def interrupt(_sig, _frame):
    global cancelled
    cancelled = True

def group_absent(pid):
    try:
        os.killpg(pid, 0)
        return False
    except ProcessLookupError:
        return True

def parse_available_memory(data, total):
    pages = re.findall(rb'page size of (\d+) bytes', data)
    require(len(pages) == 1 and int(pages[0]) > 0 and total > 0, 'Unique positive page size/total')
    rows = re.findall(rb'^(Pages (?:free|inactive)):\s+(\d+)\.', data, re.M)
    require(len(rows) == 2 and {row[0] for row in rows} == {b'Pages free', b'Pages inactive'},
            'Unique nonnegative free/inactive counts')
    available = int(pages[0])*sum(int(row[1]) for row in rows)
    require(0 <= available <= total, 'Invalid RAM numerator')
    return available


def memory_sample():
    global settled, last_memory
    probe = subprocess.Popen(['/usr/bin/vm_stat'], env=env, stdin=subprocess.DEVNULL,
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                             start_new_session=True, close_fds=True)
    previous_settlement = settled
    settled = False  # Own the probe before any wait/termination/probe can fail.
    try:
        out, err = probe.communicate(timeout=5)
    finally:
        if probe.returncode is None:
            os.killpg(probe.pid, signal.SIGTERM)
            try:
                probe.communicate(timeout=2)
            except subprocess.TimeoutExpired:
                os.killpg(probe.pid, signal.SIGKILL)
                probe.communicate(timeout=2)
        absent = group_absent(probe.pid)
        settled = previous_settlement and absent
    require(probe.returncode == 0 and absent and len(out) <= 65536 and not err, 'RAM probe failure')
    available = parse_available_memory(out, memory_total)
    last_memory = time.monotonic()
    result.setdefault('memory_samples', []).append({'elapsed': last_memory-started, 'available_bytes': available,
                                                  'original_group_absent': absent})
    require(available >= memory_total*.20, 'Running RAM floor')


def resources(entry=False):
    free = shutil.disk_usage(R.parent).free
    require(free >= (12 if entry else 8)*1024**3, 'Disk floor')
    require(not cancelled and time.monotonic()-started < 900, 'Cancelled/deadline')
    if memory_total is not None and time.monotonic()-last_memory >= 10:
        memory_sample()
    return free

def command(label, argv, timeout=120):
    global active, settled
    resources()
    log = E/(label+'.log')
    require(not log.exists(), 'Unique command label')
    rec = {'label': label, 'argv': argv, 'exit': None}
    result['commands'].append(rec)
    with log.open('xb') as output:
        active = subprocess.Popen(argv, cwd=R, env=env, stdin=subprocess.DEVNULL,
                                  stdout=output, stderr=subprocess.STDOUT,
                                  start_new_session=True, close_fds=True)
        pid = active.pid
        deadline = time.monotonic()+timeout
        try:
            while active.poll() is None:
                resources()
                require(log.stat().st_size <= 8*1024**2, 'Command output cap')
                require(time.monotonic() < deadline, 'Command timeout')
                time.sleep(.25)
            rec['exit'] = active.returncode
        finally:
            if active.returncode is None:
                # Original direct child is unreaped: PID cannot have been reused.
                os.killpg(pid, signal.SIGTERM)
                try:
                    active.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    os.killpg(pid, signal.SIGKILL)
                    active.wait(timeout=5)
                rec['exit'] = active.returncode
                rec['terminated'] = True
            absent = group_absent(pid)
            settled = settled and absent
            rec['original_group_absent'] = absent
            active = None
    data = ordinary(log, 8*1024**2)
    rec['log'] = {'bytes': len(data), 'sha256': sha(data)}
    require(rec['exit'] == 0 and rec['original_group_absent'] and not cancelled,
            'Command failure or unsettled original group: '+label)
    require(b'ERROR: AddressSanitizer' not in data and b'runtime error:' not in data,
            'Sanitizer diagnostic')
    return data

def main():
    global allocated, evidence_created, root_identity, env, memory_total, last_memory
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))  # Before any child; no /cores sanitizer dumps.
    require(resource.getrlimit(resource.RLIMIT_CORE) == (0, 0), 'Core dump limit')
    require(os.uname().sysname == 'Darwin' and os.uname().machine == 'x86_64', 'macOS x64 required')
    require(os.environ['PV_REF'] == 'refs/heads/codex/audit-continuation-linux-20260908', 'Branch fence')
    require(os.environ['PV_ATTEMPT'] == '1' and re.fullmatch(r'[0-9]+', os.environ['PV_RUN_ID']), 'Original attempt only')
    require(re.fullmatch(r'[0-9a-f]{40}', os.environ['PV_SHA']), 'Source identity')
    require(W.resolve() == W and R.parent.resolve() == R.parent, 'Canonical runner paths')
    require(not R.exists() and not R.is_symlink() and not E.exists() and not E.is_symlink(), 'Fresh run paths')
    request = json.loads(ordinary(W/REQUEST))
    require(set(request) == {'purpose', 'source_commit', 'controller_sha256', 'workflow_sha256', 'native_inputs'}, 'Request schema')
    require(request['purpose'] == 'ONE_MACOS_X64_ASAN_UBSAN_FIVE_NATIVE_CTESTS', 'Request purpose')
    require(sha(ordinary(W/CONTROLLER)) == request['controller_sha256'] and
            sha(ordinary(W/WORKFLOW)) == request['workflow_sha256'], 'Exact reviewed controls')
    require(set(request['native_inputs']) == set(FILES), 'Exact source set')
    sources = {name: ordinary(W/(PREFIX+name)) for name in FILES}
    require({name: {'bytes': len(data), 'sha256': sha(data)} for name, data in sources.items()} ==
            request['native_inputs'], 'Native source pins')
    resources(entry=True)
    R.mkdir(mode=0o700)
    root_identity = (R.stat().st_dev, R.stat().st_ino)
    allocated = True
    E.mkdir(mode=0o700)
    evidence_created = True
    with Path(os.environ['PV_OUTPUT']).open('a') as output:
        output.write('evidence_path='+str(E)+'\n')
    for name in ('home', 'tmp', 'fixtures', 'source'):
        (R/name).mkdir(mode=0o700)
    env = {'PATH': '/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin', 'HOME': str(R/'home'),
           'TMPDIR': str(R/'tmp')+'/', 'LANG': 'en_US.UTF-8', 'LC_ALL': 'en_US.UTF-8', 'TZ': 'UTC',
           'PASSVAULT_NATIVE_TEST_PARENT': str(R/'fixtures'),
           'ASAN_OPTIONS': 'detect_leaks=0:halt_on_error=1:abort_on_error=1:strict_string_checks=1',
           'UBSAN_OPTIONS': 'halt_on_error=1:print_stacktrace=1',
           'GIT_CONFIG_GLOBAL': '/dev/null', 'GIT_CONFIG_SYSTEM': '/dev/null', 'GIT_CONFIG_NOSYSTEM': '1',
           'GIT_NO_REPLACE_OBJECTS': '1', 'GIT_NO_LAZY_FETCH': '1', 'GIT_OPTIONAL_LOCKS': '0'}
    git = ['/usr/bin/git', '-C', str(W), '-c', 'core.hooksPath=/dev/null', '-c', 'core.fsmonitor=false',
           '-c', 'gc.auto=0', '-c', 'maintenance.auto=false', '-c', 'protocol.allow=never']
    identity = command('source-identity', git+['rev-parse', 'HEAD', 'HEAD^{tree}', 'HEAD^']).decode().splitlines()
    require(len(identity) == 3 and identity[0] == os.environ['PV_SHA'] and
            identity[2] == request['source_commit'], 'Activation and source parent')
    changed = command('activation-delta', git+['diff-tree', '--no-commit-id', '--name-only', '-r', 'HEAD']).decode().splitlines()
    require(changed == [REQUEST], 'Only reviewed request may activate this run')
    total = int(command('memory-total', ['/usr/sbin/sysctl', '-n', 'hw.memsize']).strip())
    require(total >= 12*1024**3, 'RAM total floor')
    memory = command('memory-available', ['/usr/bin/vm_stat'])
    available = parse_available_memory(memory, total)
    require(available >= total*.25, 'RAM availability floor')
    memory_total, last_memory = total, time.monotonic()
    result.update(activation_commit=identity[0], activation_tree=identity[1], source_commit=identity[2],
                  request_sha256=sha(ordinary(W/REQUEST)), native_inputs=request['native_inputs'],
                  run_id=os.environ['PV_RUN_ID'], attempt=1,
                  resources={'entry_disk_bytes': resources(), 'total_ram_bytes': total, 'available_ram_bytes': available,
                             'ram_metric': 'vm_stat free+inactive, cooperative reclaimable estimate, NOT hardfree or prior Mac02 free+speculative metric'})
    for name, data in sources.items():
        path = R/'source'/name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    cmake = shutil.which('cmake', path=env['PATH'])
    ctest = shutil.which('ctest', path=env['PATH'])
    require(cmake and ctest, 'Existing CMake/CTest needed; no toolchain installation')
    command('toolchain', ['/usr/bin/xcrun', 'clang++', '--version'])
    command('cmake-version', [cmake, '--version'])
    command('configure', [cmake, '-S', str(R/'source'), '-B', str(R/'build'),
            '-G', 'Unix Makefiles', '-DBUILD_TESTING=ON', '-DCMAKE_BUILD_TYPE=Debug',
            '-DCMAKE_EXPORT_COMPILE_COMMANDS=ON', '-DCMAKE_OSX_ARCHITECTURES=x86_64',
            '-DCMAKE_CXX_FLAGS='+FLAGS, '-DCMAKE_OBJCXX_FLAGS='+FLAGS,
            '-DCMAKE_EXE_LINKER_FLAGS=-fsanitize=address,undefined',
            '-DCMAKE_SHARED_LINKER_FLAGS=-fsanitize=address,undefined'], 180)
    command('build', [cmake, '--build', str(R/'build'), '--parallel', '1', '--verbose'], 300)
    compile_data = ordinary(R/'build/compile_commands.json')
    compiles = json.loads(compile_data)
    require(len(compiles) == 3 and {Path(row['file']).name for row in compiles} ==
            {'passvault_biometric_macos.mm', 'passvault_biometric_macos_security_test.mm', 'passvault_biometric_abi_test.cpp'},
            'Exact three instrumented translation units')
    for row in compiles:
        require(all(flag in row['command'].split() for flag in FLAGS.split()), 'Missing compile instrumentation')
    (E/'compile_commands.json').write_bytes(compile_data)
    instruments = []
    for target, filename in (('passvault_biometric', 'libpassvault_biometric.dylib'),
                             ('passvault_biometric_abi_test', 'passvault_biometric_abi_test'),
                             ('passvault_biometric_macos_security_test', 'passvault_biometric_macos_security_test')):
        link = ordinary(R/'build/CMakeFiles'/(target+'.dir')/'link.txt')
        require(b'-fsanitize=address,undefined' in link, 'Missing link instrumentation')
        (E/(target+'-link.log')).write_bytes(link)
        binary = R/'build'/filename
        symbols = command(target+'-instrumentation', ['/usr/bin/nm', '-u', str(binary)])
        require(b'___asan_init' in symbols and b'___ubsan_handle_' in symbols, 'Missing emitted sanitizer calls')
        architecture = command(target+'-architecture', ['/usr/bin/lipo', '-archs', str(binary)])
        require(architecture.strip() == b'x86_64', 'Wrong architecture')
        data = ordinary(binary, 32*1024**2)
        instruments.append({'target': target, 'bytes': len(data), 'sha256': sha(data)})
    result['instrumented_binaries'] = instruments
    xml = E/'native-sanitizers.xml'
    command('ctest', [ctest, '--test-dir', str(R/'build'), '--parallel', '1', '--timeout', '30',
                     '-R', '^(passvault_biometric_abi|passvault_biometric_macos_security|passvault_biometric_macos_fixture_(normal|early_return|cpp_exception))$',
                     '--output-on-failure', '--output-junit', str(xml)], 180)
    data = ordinary(xml)
    require(b'<!DOCTYPE' not in data and b'<!ENTITY' not in data, 'Unexpected XML declarations')
    cases = ET.fromstring(data).findall('.//testcase')
    require(len(cases) == 5 and {case.attrib['name'] for case in cases} == CASES, 'Exact five cases')
    require(all(case.find('failure') is None and case.find('error') is None and
                case.find('skipped') is None for case in cases), 'Failed/missing case')
    require(not any((R/'fixtures').iterdir()), 'Native fixture cleanup incomplete')
    require(all(ordinary(W/(PREFIX+name)) == data and ordinary(R/'source'/name) == data
                for name, data in sources.items()), 'Source drift')
    result.update(cases=5, passes=5, failures=0, errors=0, skipped=0, status='FIVE_INSTRUMENTED_CASES_PASSED')

W = Path(os.environ['PV_WORKSPACE'])
base = Path(os.environ['PV_RUNNER_TEMP'])
run = os.environ['PV_RUN_ID']+'-'+os.environ['PV_ATTEMPT']
R, E = base/('passvault-macos-sanitizers01-'+run), base/('passvault-macos-sanitizers01-evidence-'+run)
for sig in (signal.SIGINT, signal.SIGTERM):
    signal.signal(sig, interrupt)
try:
    main()
except BaseException as error:
    result.update(status='FAILED', error=type(error).__name__+': '+str(error))
finally:
    if allocated:
        try:
            require(active is None and settled, 'Original process settlement not established')
            require(R.resolve() == R and not R.is_symlink() and (R.stat().st_dev, R.stat().st_ino) == root_identity,
                    'Original private runtime identity changed')
            require(shutil.rmtree.avoids_symlink_attacks, 'Descriptor-safe cleanup required')
            shutil.rmtree(R)
            require(not R.exists(), 'Private runtime remains')
            result['cleanup'] = 'ORIGINAL_PRIVATE_RUNTIME_REMOVED'
        except BaseException as error:
            result.update(cleanup='HOLD', cleanup_error=type(error).__name__+': '+str(error))
    result['elapsed_seconds'] = time.monotonic()-started
    if evidence_created:
        (E/'RESULT.json').write_text(json.dumps(result, indent=2)+'\n')
    else:
        print(json.dumps({key: result.get(key) for key in ('status', 'error', 'cleanup')}, sort_keys=True))
raise SystemExit(0 if result['status'] == 'FIVE_INSTRUMENTED_CASES_PASSED' and
                 result['cleanup'] == 'ORIGINAL_PRIVATE_RUNTIME_REMOVED' else 1)
