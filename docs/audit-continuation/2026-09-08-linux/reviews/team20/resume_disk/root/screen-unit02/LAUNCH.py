"""One new seven-method fake-control batch; no old runner, process scan or outputs."""
import fcntl
import hashlib
import json
import os
import resource
import stat
import sys
import time
import types
import unittest

BASE = '/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk'
PARENT = '/root/projects/PassVault/.audit-coordination-linux-20260914-c20'
FILES = (
    ('screen_replacement_v5', 'screen_replacement_v5.py', 15557,
     '22fe6339eb06a33b6670f719f07c5f87e78f458f69f239299a39d4802531ae25'),
    ('v5_tests', 'test_screen_replacement_v5.py', 17921,
     'f717d76814f01f299b08f872f0653ac54b1c4620163fbf7d4f522d1204d3108d'),
)

def need(ok, message):
    if not ok:
        raise RuntimeError(message)

def identity(s):
    return s.st_dev, s.st_ino, s.st_uid, stat.S_IMODE(s.st_mode)

def image(s):
    return identity(s), s.st_mode, s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns

started = time.monotonic()
parent = lock = None
try:
    need(sys.flags.isolated and sys.flags.no_site and sys.dont_write_bytecode, 'isolated Python required')
    for kind, cap in ((resource.RLIMIT_CORE, 0), (resource.RLIMIT_AS, 512*1024**2),
                      (resource.RLIMIT_CPU, 10), (resource.RLIMIT_FSIZE, 1024**2)):
        soft, hard = resource.getrlimit(kind)
        high = min(cap, hard) if hard != resource.RLIM_INFINITY else cap
        low = min(high, soft) if soft != resource.RLIM_INFINITY else high
        resource.setrlimit(kind, (low, high))
    parent = os.open(PARENT, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC)
    need(identity(os.fstat(parent)) == identity(os.lstat(PARENT)) == (23, 534, 0, 0o700), 'original coordinator parent')
    lock = os.open('build.lock', os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
    s = os.fstat(lock)
    need(stat.S_ISREG(s.st_mode) and s.st_nlink == 1 and s.st_size == 0, 'original empty regular coordinator')
    need(identity(s) == identity(os.stat('build.lock', dir_fd=parent, follow_symlinks=False)) ==
         (24, 13719326, 0, 0o600), 'original coordinator leaf')
    fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    fs = os.fstatvfs(parent)
    need(fs.f_bavail * fs.f_frsize >= 3*1024**3, '3GiB disk floor')
    with open('/proc/meminfo', 'rb') as f:
        raw = f.read(65537)
    need(len(raw) <= 65536, 'memory metadata bound')
    mem = dict(line.split(':', 1) for line in raw.decode('ascii').splitlines())
    available, total = (int(mem[k].split()[0])*1024 for k in ('MemAvailable', 'MemTotal'))
    need(available >= 12*1024**3 and available*4 >= total, 'RAM entry floor')
    for module_name, name, size, digest in FILES:
        path = BASE + '/image_diag_review/esrch_policy02/' + name
        fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC)
        try:
            before = os.fstat(fd)
            need(stat.S_ISREG(before.st_mode) and before.st_uid == 0 and before.st_nlink == 1
                 and not before.st_mode & 0o022 and before.st_size == size, 'fixed source type/size')
            raw = os.read(fd, size+1)
            need(len(raw) == size and hashlib.sha256(raw).hexdigest() == digest
                 and image(before) == image(os.fstat(fd)) == image(os.lstat(path)), 'fixed source drift')
        finally:
            os.close(fd)
        module = types.ModuleType(module_name)
        sys.modules[module_name] = module
        exec(compile(raw, path, 'exec'), module.__dict__)  # Consume the exact reviewed bytes only.
    suite = unittest.defaultTestLoader.loadTestsFromTestCase(sys.modules['v5_tests'].EsrchScreenRegression)
    need(suite.countTestCases() == 7, 'exact seven new methods')
    result = unittest.TextTestRunner(stream=sys.stderr, verbosity=2).run(suite)
    need(result.testsRun == 7 and result.wasSuccessful() and not result.skipped, 'seven complete passing methods required')
    need(time.monotonic()-started <= 30, 'tiny batch exceeded observation bound')
    print(json.dumps({'kind': 'V5_FAKE_CONTROL_METHODS', 'methods': result.testsRun, 'failures': len(result.failures),
                      'errors': len(result.errors), 'skips': len(result.skipped), 'product_cases': 0, 'kernel_cases': 0,
                      'generated_outputs': 0, 'children_started': 0, 'elapsed': round(time.monotonic()-started, 6)}, sort_keys=True))
finally:
    try:
        if lock is not None:
            os.close(lock)
    finally:
        if parent is not None:
            os.close(parent)
