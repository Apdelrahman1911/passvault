#!/usr/bin/python3.12
"""One new finite metadata point; source only until root/independent admission."""
import datetime
import hashlib
import json
import os
from pathlib import Path
import selectors
import signal
import stat
import subprocess
import sys
import time

D = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/checkpoint20')
T = Path('/root/projects/PassVault/passvault-publication-20260910-01')
ARGV = ['/usr/bin/gh', 'api', '--hostname', 'github.com', '--method', 'GET',
        'repos/Apdelrahman1911/passvault/pulls', '-f', 'state=open',
        '-f', 'head=Apdelrahman1911:codex/audit-continuation-linux-20260908',
        '-f', 'per_page=100', '--jq',
        'map({number,head:{ref:.head.ref,sha:.head.sha},base:{ref:.base.ref},state})']
START = time.monotonic()
CANCELLED = False


def need(value, message):
    if not value:
        raise RuntimeError(message)


def pin(s):
    return dict(dev=s.st_dev, ino=s.st_ino, uid=s.st_uid, mode=s.st_mode,
                nlink=s.st_nlink, bytes=s.st_size, mtime_ns=s.st_mtime_ns, ctime_ns=s.st_ctime_ns)


def executable(path):
    fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC)
    try:
        before = os.fstat(fd)
        need(stat.S_ISREG(before.st_mode) and before.st_uid == 0
             and before.st_mode & 0o111 and not before.st_mode & 0o022
             and before.st_nlink == 1 and before.st_size <= 64 * 1024 * 1024, 'executable bounds')
        digest = hashlib.sha256()
        count = 0
        while True:
            data = os.read(fd, 65536)
            if not data:
                break
            count += len(data)
            need(count <= 64 * 1024 * 1024 and time.monotonic() - START < 60 and not CANCELLED,
                 'executable read bound')
            digest.update(data)
        need(pin(before) == pin(os.fstat(fd)) == pin(os.stat(path, follow_symlinks=False))
             and count == before.st_size, 'executable changed')
        return {'path': path, 'pin': pin(before), 'sha256': digest.hexdigest()}
    finally:
        os.close(fd)


def save(name, value):
    data = (json.dumps(value, sort_keys=True, indent=2, allow_nan=False) + '\n').encode()
    need(len(data) <= 65536, 'receipt cap')
    fd = os.open(D / name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600)
    try:
        with os.fdopen(fd, 'wb', closefd=False) as out:
            out.write(data)
            out.flush()
            os.fsync(fd)
    finally:
        os.close(fd)
    need((D / name).read_bytes() == data, 'receipt readback')


def cancel(_number, _frame):
    global CANCELLED
    CANCELLED = True


os.umask(0o077)
for sig in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
    signal.signal(sig, cancel)
need(sys.argv == [str(D / 'C20-CONTEXT-PR-POINT.py')] and sys.flags.isolated
     and sys.flags.no_site and sys.dont_write_bytecode, 'fixed isolated entry')
intent = {'purpose': 'ONE_C20_CONTEXT_AND_BRANCH_PR_POINT', 'owner': '/root',
          'utc': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'argv': ARGV,
          'command_seconds': 45, 'total_seconds': 60, 'pipe_cap_each': 32768,
          'scope': 'Read-only public PR projection, executable bytes, own namespaces and credential environment key PRESENCE only; collector does not inspect credential files/values. The normal gh child may read its existing authorized authentication config. No Git, build, SDK, held runtime or mutations; deadlines are cooperative, not a hard filesystem-IO bound.'}
save('PR-STATE-INTENT.json', intent)
result = dict(intent, status='HOLD_CONSUMED_NO_AUTOMATIC_RETRY', child_started=False,
              child_reaped=False, child_exit=None, signals_attempted=[], error=None)
child = None
selector = None
streams = []
buffers = {'stdout': bytearray(), 'stderr': bytearray()}
code = 1
try:
    result['executables'] = [executable('/usr/bin/gh'), executable('/usr/bin/curl')]
    result['namespaces'] = {key: os.readlink('/proc/self/ns/' + key) for key in ('pid', 'mnt')}
    context_keys = ('HOME', 'GH_CONFIG_DIR', 'XDG_CONFIG_HOME', 'GH_TOKEN', 'GITHUB_TOKEN')
    result['credential_environment_key_presence'] = {key: key in os.environ for key in context_keys}
    env = {key: os.environ[key] for key in context_keys if key in os.environ}
    for key in ('HTTPS_PROXY', 'HTTP_PROXY', 'ALL_PROXY', 'NO_PROXY', 'SSL_CERT_FILE', 'SSL_CERT_DIR'):
        if key in os.environ:
            env[key] = os.environ[key]
    env.update(PATH='/usr/bin:/bin', LANG='C.UTF-8', LC_ALL='C.UTF-8', TZ='UTC',
               GH_HOST='github.com', GH_PROMPT_DISABLED='1', GH_PAGER='cat', PAGER='cat')
    need(not CANCELLED and time.monotonic() - START < 15, 'prelaunch bound')
    selector = selectors.DefaultSelector()
    child = subprocess.Popen(ARGV, cwd=T, env=env, stdin=subprocess.DEVNULL,
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE, close_fds=True)
    result.update(child_started=True, child_pid=child.pid)
    streams = [child.stdout, child.stderr]
    launched = time.monotonic()
    for name, pipe in (('stdout', child.stdout), ('stderr', child.stderr)):
        os.set_blocking(pipe.fileno(), False)
        selector.register(pipe, selectors.EVENT_READ, name)
    while selector.get_map() or child.poll() is None:
        need(not CANCELLED and time.monotonic() - launched < 45 and time.monotonic() - START < 55,
             'command cancelled/deadline')
        for key, _mask in selector.select(0.05):
            data = os.read(key.fd, 4096)
            if data:
                need(len(buffers[key.data]) + len(data) <= 32768, 'pipe cap')
                buffers[key.data].extend(data)
            else:
                selector.unregister(key.fileobj)
    result['child_exit'] = child.wait(timeout=1)
    result['child_reaped'] = True
    need(result['child_exit'] == 0 and not buffers['stderr'], 'API failure or diagnostic')
    pulls = json.loads(bytes(buffers['stdout']))
    need(isinstance(pulls, list) and len(pulls) < 100, 'ambiguous/full-page response')
    for pull in pulls:
        need(set(pull) == {'number', 'head', 'base', 'state'} and isinstance(pull['number'], int)
             and pull['state'] == 'open' and pull['head']['ref'] == 'codex/audit-continuation-linux-20260908'
             and isinstance(pull['base']['ref'], str), 'projection mismatch')
    result['pull_requests'] = pulls
    result['status'] = 'POINT_NO_MATCHING_OPEN_PR' if not pulls else 'POINT_MATCHING_OPEN_PR_REQUIRES_ADMISSION_REASSESSMENT'
    code = 0
except BaseException as error:
    result['error'] = type(error).__name__ + ': ' + str(error)[:400]
finally:
    if child is not None and not result['child_reaped']:
        try:
            if child.poll() is None:
                child.kill()  # Direct original unreaped child only; never a name/group kill.
                result['signals_attempted'].append('KILL_ORIGINAL_DIRECT_CHILD')
            result['child_exit'] = child.wait(timeout=5)
            result['child_reaped'] = True
        except BaseException as error:
            result['settlement_error'] = type(error).__name__
            code = 1
    for pipe in streams:
        try:
            pipe.close()
        except BaseException as error:
            result.setdefault('close_errors', []).append(type(error).__name__)
            code = 1
    if selector is not None:
        try:
            selector.close()
        except BaseException as error:
            result.setdefault('close_errors', []).append(type(error).__name__)
            code = 1
    for name, value in buffers.items():
        result[name] = {'bytes': len(value), 'sha256': hashlib.sha256(value).hexdigest()}
    result['cleanup'] = 'Only original direct child/pipes settled; abnormal descendants not proven absent. No runtime/cache/temp/service or Gradle invocation; no stop or deletion. Exact terminal result remains separately required.'
    result['elapsed_seconds'] = round(time.monotonic() - START, 6)
    if CANCELLED or result['elapsed_seconds'] >= 60:
        code = 1
    if code != 0:
        result['status'] = 'HOLD_CONSUMED_NO_AUTOMATIC_RETRY'
    save('PR-STATE.json', result)
    print(json.dumps({'status': result['status'], 'pending_code': code, 'error': result['error'],
                      'child_reaped': result['child_reaped'], 'child_exit': result['child_exit']}))
sys.exit(code)
