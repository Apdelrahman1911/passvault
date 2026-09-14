"""One new tiny fixture launch; preserve current original coordination through exec."""
import fcntl, hashlib, os, stat
P = '/root/projects/PassVault/.audit-coordination-linux-20260914-c20'
F = '/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/current_ledger/gui_disposal_contract01/fixture.py'
def identity(s):
    return (s.st_dev, s.st_ino, s.st_uid, stat.S_IMODE(s.st_mode))
def need(ok):
    if not ok:
        raise RuntimeError('fresh fixture source/coordination mismatch; no launch')
p = os.open(P, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC)
need(identity(os.fstat(p)) == identity(os.lstat(P)) == (23, 534, 0, 0o700))
l = os.open('build.lock', os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=p)
s = os.fstat(l)
need(stat.S_ISREG(s.st_mode) and s.st_nlink == 1 and s.st_size == 0)
need(identity(s) == identity(os.stat('build.lock', dir_fd=p, follow_symlinks=False)) == (24, 13719326, 0, 0o600))
fcntl.flock(l, fcntl.LOCK_EX | fcntl.LOCK_NB)
f = os.open(F, os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC)
raw = os.read(f, 15856)
need(len(raw) == 15855 and hashlib.sha256(raw).hexdigest() == 'b103e0b726b2113caa761eedc7176e4f08c35866f0f8ce6738d6199fd396ed8b')
os.close(f)
os.close(p)
os.set_inheritable(l, True)  # Replaced process owns this original lock until terminal exit.
os.execv('/usr/bin/python3.12', ['/usr/bin/python3.12', '-I', '-B', '-S', '-c', raw.decode('utf-8')])
