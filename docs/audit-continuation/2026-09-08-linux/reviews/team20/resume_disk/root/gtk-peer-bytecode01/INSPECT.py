#!/usr/bin/python3.12
"""New DATA-only installed-JDK bytecode inspection; exact independent approval required."""
import fcntl
import hashlib
import json
import os
from pathlib import Path
import shutil
import signal
import stat
import subprocess
import time

P = Path('/root/projects/PassVault')
HERE = P / 'passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/gtk-peer-bytecode01'
ROOT = P / 'audit-gtk-peer-bytecode01'
LOCK = P / '.audit-coordination-linux-20260914-c20/build.lock'
JDK = Path('/usr/lib/jvm/java-17-openjdk-amd64')
child = None
lock_fd = None
allocated = False
cancelled = False
result = {'status': 'UNSTARTED', 'cases': 0, 'cleanup': 'NOT_ALLOCATED'}


def require(value, reason):
    if not value:
        raise RuntimeError(reason)


def sha(data):
    return hashlib.sha256(data).hexdigest()


def file_sha(path):
    before = path.stat()
    require(before.st_size <= 256 * 1024**2, 'JDK input cap')
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        while block := stream.read(1024 * 1024):
            digest.update(block)
    after = path.stat()
    require((before.st_dev, before.st_ino, before.st_size, before.st_mtime_ns, before.st_ctime_ns) ==
            (after.st_dev, after.st_ino, after.st_size, after.st_mtime_ns, after.st_ctime_ns), 'JDK input drift')
    return digest.hexdigest()


def interrupt(_signal, _frame):
    global cancelled
    cancelled = True


for sig in (signal.SIGTERM, signal.SIGINT):
    signal.signal(sig, interrupt)
try:
    raw = (HERE / 'REQUEST.json').read_bytes()
    request = json.loads(raw)
    approval = json.loads((HERE / 'APPROVAL.json').read_bytes())
    require(approval['reviewer'] == '/root/current_ledger' and approval['status'] == 'ACCEPT_EXACT_JAVAP_DATA_ONLY'
            and approval['request_sha256'] == sha(raw), 'Exact independent approval')
    require(request['scope'] == 'GTK_FILE_DIALOG_PEER_BYTECODE01', 'Exact DATA scope')
    require(sha(Path(__file__).read_bytes()) == request['controller_sha256'], 'Bound new controller')
    for name, digest in request['jdk_images'].items():
        path = JDK / name
        require(name in ('bin/javap', 'bin/java', 'release', 'lib/modules') and path.is_file()
                and not path.is_symlink() and file_sha(path) == digest, 'Exact installed JDK bytes')
    require(set(request['jdk_images']) == {'bin/javap', 'bin/java', 'release', 'lib/modules'}, 'Complete JDK inputs')
    lock_fd = os.open(LOCK, os.O_RDONLY | os.O_NOFOLLOW)
    s = os.fstat(lock_fd)
    require([s.st_dev, s.st_ino, s.st_uid, stat.S_IMODE(s.st_mode)] == request['lock_identity'], 'Current lock')
    fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
    require(shutil.disk_usage(P).free >= 3 * 1024**3 and not cancelled, 'DATA resource/cancel admission')
    require(not ROOT.exists() and not ROOT.is_symlink(), 'New disposable namespace')
    ROOT.mkdir(mode=0o700)
    allocated = True
    identity = (ROOT.stat().st_dev, ROOT.stat().st_ino)
    for name in ('home', 'tmp'):
        (ROOT / name).mkdir(mode=0o700)
    env = {'PATH': '/usr/bin:/bin', 'HOME': str(ROOT / 'home'), 'TMPDIR': str(ROOT / 'tmp'),
           'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC', 'JAVA_HOME': str(JDK)}
    argv = [str(JDK / 'bin/javap'), '-J-XX:-UsePerfData', '-J-Duser.home=' + str(ROOT / 'home'),
            '-J-Djava.io.tmpdir=' + str(ROOT / 'tmp'), '-p', '-c', '--module', 'java.desktop',
            'sun.awt.X11.GtkFileDialogPeer']
    result['argv'] = argv
    with (HERE / 'javap.log').open('xb') as output:
        child = subprocess.Popen(argv, cwd=ROOT, env=env, stdin=subprocess.DEVNULL,
                                 stdout=output, stderr=subprocess.STDOUT, start_new_session=True, close_fds=True)
        result['original_pid'] = child.pid
        started = time.monotonic()
        while child.poll() is None:
            require(not cancelled and time.monotonic() - started < 15, 'DATA deadline/cancellation')
            require(os.fstat(output.fileno()).st_size <= 256 * 1024, 'Output cap')
            time.sleep(0.1)
        require(os.fstat(output.fileno()).st_size <= 256 * 1024, 'Final output cap')
        require(child.returncode == 0 and not cancelled, 'Original javap exit')
    result['status'] = 'BYTECODE_CAPTURED_NOT_APPLICATION_VERIFICATION'
except BaseException as error:
    result.update(status='FAILED_NO_AUTOMATIC_RETRY', error=type(error).__name__ + ': ' + str(error))
finally:
    settled = child is None
    if child is not None:
        if child.returncode is None:
            os.killpg(child.pid, signal.SIGTERM)  # Original unreaped child/session only.
            try:
                child.wait(timeout=5)
            except subprocess.TimeoutExpired:
                os.killpg(child.pid, signal.SIGKILL)
                child.wait(timeout=5)
        result['exit'] = child.returncode
        try:
            os.killpg(child.pid, 0)
        except ProcessLookupError:
            settled = True
        result['original_group_settled'] = settled
    if allocated and settled:
        try:
            require(ROOT.resolve() == ROOT and (ROOT.stat().st_dev, ROOT.stat().st_ino) == identity, 'Original root')
            for name in ('home', 'tmp'):
                (ROOT / name).rmdir()
            ROOT.rmdir()
            result['cleanup'] = 'ORIGINAL_EMPTY_PRIVATE_DIRS_REMOVED'
        except BaseException as error:
            result['cleanup'] = 'HOLD_' + type(error).__name__
    elif allocated:
        result['cleanup'] = 'HOLD_UNSETTLED'
    if lock_fd is not None:
        os.close(lock_fd)
    log = HERE / 'javap.log'
    if log.exists():
        result['output'] = {'bytes': log.stat().st_size, 'sha256': sha(log.read_bytes())}
    (HERE / 'RESULT.json').write_text(json.dumps(result, indent=2) + '\n')
raise SystemExit(0 if result['status'] == 'BYTECODE_CAPTURED_NOT_APPLICATION_VERIFICATION' else 1)
