#!/usr/bin/python3.12
"""One finite pure-function check. Never imports/executes either runner module."""
import ast
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import signal
import stat
import sys
import time

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
D = B / 'reviews/detekt02/parser-controls01'
SELF = D / 'RUN.py'
PURPOSE = 'DETEKT02_PURE_PARSER_FIVE_CONTROLS_ONCE'
START = time.monotonic()
record = {'purpose': PURPOSE, 'status': 'HOLD', 'controls': [], 'application_cases': 0,
          'cleanup': 'Memory-only; no subprocess/Gradle/cache/temp/runtime. Stop NOT_APPLICABLE.',
          'resources': []}
lock_fd = result_fd = None


def check():
    if time.monotonic() - START > 15:
        raise RuntimeError('15s cooperative deadline')


def read(path):
    check()
    for parent in path.parents:
        assert stat.S_ISDIR(parent.lstat().st_mode), 'symlink/non-directory parent'
    before = path.lstat()
    assert stat.S_ISREG(before.st_mode) and before.st_uid == 0 and before.st_nlink == 1
    assert before.st_size <= 131072
    def pin(value):
        return (value.st_dev, value.st_ino, value.st_uid, value.st_mode, value.st_nlink,
                value.st_size, value.st_mtime_ns, value.st_ctime_ns)
    with os.fdopen(os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC), 'rb') as stream:
        assert pin(os.fstat(stream.fileno())) == pin(before)
        raw = stream.read(131073)
        assert pin(os.fstat(stream.fileno())) == pin(before) == pin(path.lstat())
    assert len(raw) == before.st_size
    return raw


def resource(launch=False):
    fs = os.statvfs(W)
    memory = {line.split(':')[0]: int(line.split()[1]) * 1024
              for line in Path('/proc/meminfo').read_text().splitlines()}
    row = {'disk_available': fs.f_bavail * fs.f_frsize, 'memory_available': memory['MemAvailable'],
           'memory_total': memory['MemTotal'], 'launch': launch}
    record['resources'].append(row)
    assert row['disk_available'] >= (12 if launch else 8) * 1024 ** 3
    assert row['memory_available'] * 100 >= row['memory_total'] * (25 if launch else 20)


def cancel(_number, _frame):
    raise KeyboardInterrupt('cancelled')


for sig in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
    signal.signal(sig, cancel)
os.umask(0o077)
try:
    assert sys.argv == [str(SELF)] and sys.executable == '/usr/bin/python3.12'
    assert sys.flags.isolated and sys.flags.no_site and sys.dont_write_bytecode and not sys.flags.optimize
    acceptance = json.loads(read(D / 'ACCEPT.json'))
    assert acceptance == {'purpose': PURPOSE, 'reviewer': '/root/editor', 'disposition': 'ACCEPT_EXACT_ONCE',
                          'source_sha256': hashlib.sha256(read(SELF)).hexdigest()}
    lock = W.parent / '.audit-coordination-linux-20260908/build.lock'
    assert (lock.parent.lstat().st_dev, lock.parent.lstat().st_ino) == (23, 661121)
    lock_fd = os.open(lock, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC)
    s = os.fstat(lock_fd)
    assert (s.st_dev, s.st_ino, s.st_uid, s.st_mode, s.st_nlink, s.st_size) == (24, 14189001, 0, 33152, 1, 0)
    assert s == lock.lstat()
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    assert json.loads(read(B / 'EXECUTION_SLOT.json'))['state'] == 'IDLE_NO_AUDIT_BUILD_ADMITTED'
    resource(launch=True)
    result_fd = os.open(D / 'RESULT.json', os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600)
    parent_fd = os.open(D, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC)
    try:
        os.fsync(parent_fd)
    finally:
        os.close(parent_fd)
    controls_raw = read(B / 'reviews/editor/detekt02/PARSER-CONTROLS.json')
    assert hashlib.sha256(controls_raw).hexdigest() == '341a26c350d77604a957f433e72fb116fd2532d254dbabf02f5c8c762a1607f2'
    controls = json.loads(controls_raw)
    holder = read(W / 'scripts/audit/linux_detekt_02.py')
    source = b''.join(holder.splitlines(keepends=True)[123:163])
    assert hashlib.sha256(source).hexdigest() == 'cb30e52bf1c4c1c622a22794158cd6631cbe8542631d46aa748d4f8501294e90'
    tree = ast.parse(source)
    assert len(tree.body) == 1 and isinstance(tree.body[0], ast.FunctionDef)
    assert tree.body[0].name == 'parse_task_headers' and not tree.body[0].decorator_list
    log = read(B / 'runs/linux-detekt01/logs/detekt.log')
    assert hashlib.sha256(log).hexdigest() == '13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218'
    cases = controls['controls']
    assert [x['id'] for x in cases] == ['P1', 'P2', 'P3', 'P4', 'P5']
    assert cases[0]['lines'] == [log.decode().splitlines()[n - 1] for n in (71, 74, 87)]
    namespace = {'re': re}
    exec(compile(tree, '<independently-reviewed-pure-parser>', 'exec'), namespace)
    for case in cases:
        check()
        actual = namespace['parse_task_headers'](case['lines'], controls['allowed'])
        record['controls'].append({'id': case['id'], 'actual': actual,
                                   'status': 'PASS' if actual == case['expected'] else 'FAIL'})
    record.update(holder_sha256=hashlib.sha256(holder).hexdigest(), parser_sha256=hashlib.sha256(source).hexdigest(),
                  controls_sha256=hashlib.sha256(controls_raw).hexdigest(), status='PASS' if
                  all(row['status'] == 'PASS' for row in record['controls']) else 'FAIL')
    resource()
    check()
except BaseException as error:
    record.update(status='HOLD', error=type(error).__name__ + ': ' + str(error)[:500])
finally:
    signal.pthread_sigmask(signal.SIG_BLOCK, {signal.SIGTERM, signal.SIGINT, signal.SIGHUP})
    if signal.sigpending() & {signal.SIGTERM, signal.SIGINT, signal.SIGHUP}:
        record.update(status='HOLD', error='terminal interruption')
    record['elapsed_seconds'] = time.monotonic() - START
    try:
        if result_fd is not None:
            raw = (json.dumps(record, sort_keys=True, indent=2) + '\n').encode()
            assert len(raw) <= 131072
            while raw:
                written = os.write(result_fd, raw)
                assert written > 0
                raw = raw[written:]
            os.fsync(result_fd)
    except BaseException as error:
        record.update(status='HOLD', receipt_error=type(error).__name__)
    for fd in (result_fd, lock_fd):
        if fd is not None:
            try:
                os.close(fd)
            except OSError:
                record.update(status='HOLD', close_error=True)
    print(json.dumps({'status': record['status'], 'controls': len(record['controls']),
                      'error': record.get('error'), 'receipt_error': record.get('receipt_error'),
                      'close_error': record.get('close_error', False)}))
sys.exit(0 if record['status'] == 'PASS' else 1)
