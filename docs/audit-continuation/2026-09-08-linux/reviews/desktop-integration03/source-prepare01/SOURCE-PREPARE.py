#!/usr/bin/python3
"""NEW C15 integration03 source-prepare01 shared by focused Desktop and static validation.
Externally reviewed execution required; no predecessor scope is replayed.
Read-only new-T Git only; no cat-file, builds, old stores, R/E or binding edits.
One observed checkout buffer per file; calculated raw candidates need exact Git
OID/size/mode. No atomic-checkout/hostile-root/hard-deadline/descendant claim.
On any failure: HOLD, preserve partial outputs, no automatic retry.
"""
import fcntl, hashlib, json, os, re, selectors, signal, stat, subprocess, sys, time
from pathlib import Path

W = Path('/root/projects/PassVault/passvault-linux')
B = W / 'docs/audit-continuation/2026-09-08-linux'
D = B / 'reviews/desktop-integration03/source-prepare01'
SELF = D / 'SOURCE-PREPARE.py'
T = Path('/root/projects/PassVault/passvault-publication-20260910-01')
G = T / '.git'
LOCK = T.parent / '.audit-coordination-linux-20260908/build.lock'
P, TREE = '8f42274b04e206ff7254ca33d686a9666fce6723', '3a8f53dddd54f5c42c34f772975f02619be118d5'
REF = 'refs/heads/codex/audit-continuation-linux-20260908'
FACTS, FACTS_SHA = B / 'reviews/desktop-integration03/SOURCE-STORE-ADMISSION.json', 'd6019b8fd192d8db6bb472c725fa451b06aec5bb2e7385979fd0fd62e326a3fb'
PUB, PUB_SHA = B / 'publication/CHECKPOINT-15-PUBLISHED.json', 'd596b0a69f4a158943365eaef8a3088e169aec441dbacd7b3d9c13421164b3ca'
OLD = B / 'reviews/desktop-gui02'
OLD_SHA = '6d593b947d76f0a7b3e7929a7ae54e6968cb0a36b69bf78d771b33226bd396ee'
OLD_CAPTURE_SHA = '5b89c5c90f40b34ff92a633fb2e93f3174a65451aaf8642ce8cdc4c9822fe0a9'
MIB, ODIR = 1024 ** 2, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
START, cancelled, pinned = time.monotonic(), [], {}
report = {'format': 'passvault-integration03-source-prepare-v1', 'owner': '/root', 'status': 'HOLD',
          'commit': P, 'tree': TREE, 'store': str(G), 'commands': [], 'resources': [], 'tests': 0}
ENV = {'PATH': '/usr/bin:/bin', 'HOME': '/nonexistent', 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8', 'TZ': 'UTC',
       'GIT_CONFIG_NOSYSTEM': '1', 'GIT_CONFIG_SYSTEM': '/dev/null', 'GIT_CONFIG_GLOBAL': '/dev/null',
       'GIT_OPTIONAL_LOCKS': '0', 'GIT_TERMINAL_PROMPT': '0', 'GIT_NO_REPLACE_OBJECTS': '1',
       'GIT_NO_LAZY_FETCH': '1', 'GIT_ATTR_NOSYSTEM': '1'}
CMD = ['/usr/bin/git', '--no-pager']
for option in ('core.hooksPath=/dev/null', 'core.fsmonitor=false', 'gc.auto=0',
               'maintenance.auto=false', 'protocol.allow=never', 'core.attributesFile=/dev/null'):
    CMD += ['-c', option]
CMD += ['--git-dir=' + str(G)]

def require(ok, why):
    if not ok: raise RuntimeError(why)

def pin(s, directory=False):
    v = {k: getattr(s, 'st_' + k) for k in ('dev', 'ino', 'uid', 'mode', 'nlink')}
    if not directory: v.update(bytes=s.st_size, mtime_ns=s.st_mtime_ns, ctime_ns=s.st_ctime_ns)
    return v

def od(path):
    fd = os.open('/', ODIR)
    try:
        for part in path.parts[1:]:
            require(part not in ('', '.', '..'), 'directory component')
            child = os.open(part, ODIR, dir_fd=fd)
            previous, fd = fd, child
            os.close(previous)
            s = os.fstat(fd)
            require(s.st_uid == 0 and not s.st_mode & 0o022, 'directory owner/mode')
        return fd
    except BaseException:
        os.close(fd); raise

def read(path, cap):
    parent, fd = od(path.parent), None
    try:
        before = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
        require(stat.S_ISREG(before.st_mode) and before.st_uid == 0 and before.st_nlink == 1
                and not before.st_mode & 0o022 and before.st_size <= cap, 'file type/owner/size: ' + str(path))
        fd = os.open(path.name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC, dir_fd=parent)
        require(pin(before) == pin(os.fstat(fd)), 'file open drift')
        data = bytearray()
        while True:
            chunk = os.read(fd, min(65536, cap + 1 - len(data)))
            if not chunk: break
            data.extend(chunk); require(len(data) <= cap, 'file byte cap')
        require(len(data) == before.st_size and pin(before) == pin(os.fstat(fd)) ==
                pin(os.stat(path.name, dir_fd=parent, follow_symlinks=False)), 'file read drift')
        return bytes(data), pin(before)
    finally:
        try:
            if fd is not None: os.close(fd)
        finally: os.close(parent)

def absent(path):
    # Missing ancestor means target absent; all other traversal/close errors refuse.
    try: fd = od(path.parent)
    except FileNotFoundError: return True
    try:
        try: os.stat(path.name, dir_fd=fd, follow_symlinks=False)
        except FileNotFoundError: return True
        return False
    finally: os.close(fd)

def checked(path, digest, cap=2 * MIB):
    data, identity = read(path, cap)
    require(hashlib.sha256(data).hexdigest() == digest, 'pinned data hash: ' + str(path))
    pinned[path] = identity
    return data

def tick():
    require(not cancelled and time.monotonic() - START < 180, 'cancelled/work deadline')

def resources():
    tick()
    with open('/proc/meminfo', 'rb') as stream: data = stream.read(65537)
    require(len(data) <= 65536, 'meminfo cap')
    values = dict(line.split(b':', 1) for line in data.splitlines())
    memory, total = (int(values[k].split()[0]) * 1024 for k in (b'MemAvailable', b'MemTotal'))
    disk = os.statvfs(D); free = disk.f_bavail * disk.f_frsize
    initial = not report['resources']
    require(free >= (12 if initial else 8) * 1024 * MIB
            and 100 * memory >= total * (25 if initial else 20), 'original disk/RAM resource floor')
    report['resources'].append({'free_disk': free, 'available_ram': memory, 'total_ram': total})

def guard(facts, lock):
    tick()
    require(pin(os.fstat(lock)) == facts['original_lock'], 'original lock descriptor')
    for path, key in ((T, 'checkout_identity'), (G, 'store_identity'),
                      (LOCK.parent, 'coordination_parent_identity'), (T.parent, 'workspace_parent_identity')):
        fd = od(path)
        try: require(pin(os.fstat(fd), True) == facts[key], 'original directory: ' + str(path))
        finally: os.close(fd)
    for path, expected in pinned.items():
        fd = od(path.parent)
        try: require(pin(os.stat(path.name, dir_fd=fd, follow_symlinks=False)) == expected, 'pinned input drift')
        finally: os.close(fd)
    require(all(absent(G / name) for name in ('commondir', 'objects/info/alternates',
            'objects/info/http-alternates', 'config.worktree', 'info/attributes')), 'external store/config/attributes')

def git(args, cap, facts, lock):
    guard(facts, lock); resources()
    item = {'argv': CMD + args, 'cwd': str(T), 'complete': False, 'direct_child_reaped': False}
    report['commands'].append(item)
    child, selector, buffers = None, selectors.DefaultSelector(), [bytearray(), bytearray()]
    deadline = time.monotonic() + 30
    try:
        child = subprocess.Popen(item['argv'], cwd=T, env=ENV, stdin=subprocess.DEVNULL,
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE, close_fds=True, start_new_session=True)
        for index, stream in enumerate((child.stdout, child.stderr)):
            os.set_blocking(stream.fileno(), False); selector.register(stream, selectors.EVENT_READ, index)
        while selector.get_map():
            tick(); require(time.monotonic() < deadline, 'Git time cap')
            for key, _ in selector.select(0.1):
                chunk = os.read(key.fileobj.fileno(), 65536)
                if not chunk: selector.unregister(key.fileobj); continue
                buffers[key.data].extend(chunk)
                require(len(buffers[key.data]) <= (cap if key.data == 0 else 65536), 'Git output cap')
        child.wait(timeout=max(0.01, min(deadline, START + 180) - time.monotonic()))
        require(child.returncode == 0 and not buffers[1], 'Git exit/stderr')
        item['complete'] = True
        return bytes(buffers[0])
    except BaseException as error:
        item['error'] = type(error).__name__ + ':' + str(error)[:512]; raise
    finally:
        try:
            if child is not None and child.poll() is None:
                item['abnormal_direct_child_cleanup'] = True
                try: child.terminate(); child.wait(timeout=4)
                except Exception:
                    if child.poll() is None: child.kill(); child.wait(timeout=4)
        finally:
            try:
                if child is not None:
                    item.update(exit=child.poll(), direct_child_reaped=child.returncode is not None)
            finally:
                try:
                    try:
                        if child is not None and child.stdout is not None: child.stdout.close()
                    finally:
                        if child is not None and child.stderr is not None: child.stderr.close()
                finally:
                    selector.close()
                    item['streams'] = [{'bytes': len(b), 'sha256': hashlib.sha256(b).hexdigest()} for b in buffers]

def write(name, data):
    require(name in ('SOURCE.json', 'SOURCE-CAPTURE.json') and len(data) < 2 * MIB, 'output name/size')
    parent, fd = od(D), None
    try:
        fd = os.open(name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC, 0o600, dir_fd=parent)
        s = os.fstat(fd)
        require(s.st_uid == 0 and s.st_nlink == 1 and s.st_mode == stat.S_IFREG | 0o600, 'output original')
        offset = 0
        while offset < len(data):
            count = os.write(fd, data[offset:]); require(count > 0, 'output short write'); offset += count
        os.fsync(fd); os.fsync(parent)
    finally:
        try:
            if fd is not None: os.close(fd)
        finally: os.close(parent)
    observed, _ = read(D / name, 2 * MIB); require(observed == data, 'output readback')

def encoded(value): return json.dumps(value, separators=(',', ':'), ensure_ascii=True, allow_nan=False).encode() + b'\n'

def main():
    require(sys.argv == [str(SELF)] and sys.flags.isolated and sys.flags.no_site
            and sys.dont_write_bytecode and os.getuid() == os.geteuid() == 0, 'fixed isolated root entry')
    facts = json.loads(checked(FACTS, FACTS_SHA, 65536))
    require((facts['source_commit'], facts['source_tree'], facts['store']) == (P, TREE, str(G)), 'facts binding')
    lock = None
    try:
        parent = od(LOCK.parent)
        try: lock = os.open(LOCK.name, os.O_RDWR | os.O_NOFOLLOW | os.O_CLOEXEC, dir_fd=parent)
        finally: os.close(parent)
        require(pin(os.fstat(lock)) == facts['original_lock'], 'original lock'); pinned[LOCK] = facts['original_lock']
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        require(absent(D / 'SOURCE.json') and absent(D / 'SOURCE-CAPTURE.json'), 'consumed output names')
        pub = json.loads(checked(PUB, PUB_SHA, 65536))
        require((pub['commit'], pub['tree'], pub['publication_clone'], pub['status']) ==
                (P, TREE, str(T), 'PUBLISHED_EXACT_C15_SOURCE'), 'publication binding')
        for name, key in (('config', 'config'), ('HEAD', 'head'), (REF, 'branch_ref'), ('shallow', 'shallow')):
            data = checked(G / name, facts[key]['sha256'], 65536)
            require(pinned[G / name] == facts[key]['pin'], 'original store metadata')
            if key == 'branch_ref': require(data == (P + '\n').encode(), 'current C15 branch')
            if key == 'shallow': require(data == (facts[key]['boundary_commit'] + '\n').encode(), 'original shallow boundary')
        prior = json.loads(checked(OLD / 'SOURCE.json', OLD_SHA))
        old_capture = json.loads(checked(OLD / 'SOURCE-CAPTURE.json', OLD_CAPTURE_SHA, 65536))
        allowed = old_capture['calculated_checkout_differences']
        require(len(allowed) == len(set(allowed)) == 15 and len(prior['checkout_eol_qualifications']) == 2, 'pinned EOL data')
        for path in (SELF, Path('/usr/bin/git')):
            data, identity = read(path, 32 * MIB); pinned[path] = identity
            report['recipe' if path == SELF else 'git_image'] = {'sha256': hashlib.sha256(data).hexdigest(), 'pin': identity}
        require(git(['rev-parse', P, P + '^{tree}'], 82, facts, lock) == (P + '\n' + TREE + '\n').encode(), 'actual C15 identity')
        listing = git(['ls-tree', '-r', '-z', '-l', '--full-tree', P], 2 * MIB, facts, lock)
        entries = listing.split(b'\0'); require(entries.pop() == b'' and 0 < len(entries) <= 3000, 'complete NUL inventory/count')
        rows, seen, derived, raw_total, checkout_total = [], set(), [], 0, 0
        for index, entry in enumerate(entries):
            tick()
            if index % 128 == 0: resources()
            header, separator, pathname = entry.partition(b'\t'); fields = header.split()
            require(separator and len(fields) == 4, 'typed tree record')
            mode, kind, oid, size = fields
            require(mode in (b'100644', b'100755') and kind == b'blob' and re.fullmatch(b'[0-9a-f]{40}', oid)
                    and size.isdigit(), 'tree mode/type/OID/size')
            size, name = int(size), pathname.decode('utf-8')
            require(0 <= size <= 32 * MIB and len(pathname) <= 1024 and name and not name.startswith('/')
                    and all(part not in ('', '.', '..', '.git') for part in name.split('/'))
                    and all(ord(c) >= 32 and ord(c) != 127 for c in name) and name not in seen, 'source path/size')
            seen.add(name); raw_total += size; require(raw_total <= 128 * MIB, 'raw source cap')
            data, identity = read(T / name, 32 * MIB)
            require(bool(identity['mode'] & 0o111) == (mode == b'100755'), 'observed Git executable mode')
            raw = data
            blob = hashlib.sha1(); blob.update(b'blob ' + str(len(raw)).encode() + b'\0'); blob.update(raw)
            if len(raw) != size or blob.hexdigest().encode() != oid:
                require(name in allowed, 'unadmitted checkout/raw difference: ' + name)
                raw = data.replace(b'\r\n', b'\n')
                blob = hashlib.sha1(); blob.update(b'blob ' + str(len(raw)).encode() + b'\0'); blob.update(raw)
                require(raw != data and len(raw) == size and blob.hexdigest().encode() == oid, 'EOL candidate not exact Git blob')
                derived.append(name)
            checkout_total += len(data); require(checkout_total <= 128 * MIB, 'observed checkout cap')
            rows.append({'path': name, 'git_mode': mode.decode(), 'git_blob': oid.decode(), 'git_size': size,
                         'raw_size': len(raw), 'raw_sha256': hashlib.sha256(raw).hexdigest(),
                         'checkout_size': len(data), 'checkout_sha256': hashlib.sha256(data).hexdigest(),
                         'observed_checkout_pin': identity})
            del data, raw
        # Retain historical qualifications only with unchanged script AND raw-copy Git tuples.
        # Current checkout fields remain observations, not assertions about old calculated checkout.
        previous_rows = {row['path']: row for row in prior['files']}
        current_rows = {row['path']: row for row in rows}
        for qualification in prior['checkout_eol_qualifications']:
            for name in (qualification['path'], qualification['raw_copy']):
                require(all(current_rows[name][key] == previous_rows[name][key] for key in
                            ('git_mode', 'git_blob', 'git_size', 'raw_size', 'raw_sha256')),
                        'historical EOL raw Git tuple drift: ' + name)
        guard(facts, lock); resources()
        report.update(status='SOURCE_PREPARED_NO_EXECUTION_ADMISSION', publication_receipt_sha256=PUB_SHA,
                      store_facts_sha256=FACTS_SHA, original_shallow=facts['shallow'], members=len(rows),
                      raw_source_bytes=raw_total, observed_checkout_bytes=checkout_total, derived_crlf_to_lf_paths=derived,
                      prior_source_sha256=OLD_SHA, prior_capture_sha256=OLD_CAPTURE_SHA, elapsed=time.monotonic() - START,
                      cleanup='Normal foreground Git children reaped; no runtime/temp/cache/build or Gradle duty. Abnormal direct-child cleanup is not descendant proof; HOLD/no retry.')
        manifest = {'format': 'passvault-linux-checkout-source-v1', 'author': '/root', 'commit': P, 'tree': TREE,
                    'representation': 'RAW_IDENTITIES_FROM_OBSERVED_CHECKOUT_OR_EXACT_OID_VERIFIED_CRLF_TO_LF_CANDIDATE',
                    'files': rows, 'checkout_eol_qualifications': prior['checkout_eol_qualifications'], 'capture': report,
                    'qualification': 'Checkout fields are observed T buffers, not calculated checkout. Raw fields describe the same buffer or exact OID/size/mode-verified candidate, not cat-file transport observation. Two historical qualifications retained as data, not fresh runtime proof. P keeps unbound helpers; this preparation grants no execution admission.'}
        data = encoded(manifest); write('SOURCE.json', data)
        report.update(source_sha256=hashlib.sha256(data).hexdigest(), source_bytes=len(data))
        write('SOURCE-CAPTURE.json', encoded(report))
    finally:
        if lock is not None: os.close(lock)

if __name__ == '__main__':
    signal.signal(signal.SIGCHLD, signal.SIG_DFL)
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
        signal.signal(number, lambda signum, _frame: cancelled.append(signum))
    signal.pthread_sigmask(signal.SIG_SETMASK, set())
    try:
        main(); print(encoded(report).decode(), end='')
    except BaseException as error:
        report.update(status='HOLD_NO_AUTOMATIC_RETRY', error=type(error).__name__ + ':' + str(error)[:1024])
        print(encoded(report).decode(), end=''); sys.exit(70)
