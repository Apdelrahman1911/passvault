"""SOURCE ONLY: GUI07 cleanup component fixture; lifecycle/custody inputs are simulated.

Root must separately review/admit this exact fixture. No controller entry/import,
Gradle, child process, GUI, old runtime, or old lock is used. Stdout is the result.
"""
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import resource
import signal
import stat
import sys
import time

HERE = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/current_ledger')
SUBJECT = HERE / 'gui_successor01/OUTER.py'
SPEC = HERE / 'gui_disposal_contract01/EXTRACTION.json'
ROOT = Path('/root/projects/PassVault/audit-runtime-gui07-disposal-contract01')
KEEP = HERE.parents[3] / 'runs/gui07-disposal-contract01'
NAMES = ('require pin same_dir pairs decode encoded note directory capture new output under '
         'no_runtime_mounts selected_report_images cache_report_root capture_report_source '
         'preserve_generated_reports verify_generated_reports remove_runtime signal_note '
         'only_work_signals disposal_policy begin_cleanup cleanup_tick cleanup_capture').split()
MIB, GIB = 1024 ** 2, 1024 ** 3
ODIR = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC


def need(ok, why):
    if not ok:
        raise RuntimeError(why)


def identity(s):
    return (s.st_dev, s.st_ino, s.st_uid, s.st_mode)


def full(s):
    return identity(s) + (s.st_nlink, s.st_size, s.st_mtime_ns, s.st_ctime_ns)


def sealed(path, size, digest):
    fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC)
    try:
        first = os.fstat(fd)
        need(stat.S_ISREG(first.st_mode) and first.st_uid == 0 and first.st_nlink == 1
             and not first.st_mode & 0o022 and first.st_size == size, 'sealed input type/size')
        raw = b''
        while len(raw) <= size:
            part = os.read(fd, min(65536, size + 1 - len(raw)))
            if not part:
                break
            raw += part
        need(len(raw) == size and hashlib.sha256(raw).hexdigest() == digest
             and full(first) == full(os.fstat(fd)) == full(os.lstat(path)), 'sealed input drift')
        return raw
    finally:
        os.close(fd)


def main():
    started = time.monotonic()
    entries, parents, subjects, errors, rows = [], {}, [], [], []
    active, expected_signal, unexpected, self_fd = None, False, [], None
    result = {'kind': 'GUI07_SYNTHETIC_DISPOSAL_COMPONENT', 'product_cases': 0,
              'lifecycle_custody_and_unlink_eligibility': 'SIMULATED_NOT_GUI07_ADMISSION',
              'real_boundaries': ['original-file disposal', 'evidence retention', 'owner signals'], 'cases': rows}

    def handler(number, frame):
        nonlocal expected_signal
        if expected_signal and number == signal.SIGTERM:
            expected_signal = False
        else:
            unexpected.append(number)
        if active is not None:
            active['signal_note'](number, frame)

    def tick():
        need(not unexpected and time.monotonic() < started + 60, 'fixture interruption/deadline')

    def allocate(path, data=None):
        tick()
        need(path == ROOT or ROOT in path.parents or path == KEEP or KEEP in path.parents, 'fixed fixture roots only')
        parent = parents[path.parent]
        if data is None:
            os.mkdir(path.name, 0o700, dir_fd=parent)
            fd = os.open(path.name, ODIR, dir_fd=parent)
        else:
            fd = os.open(path.name, os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC,
                         0o600, dir_fd=parent)
        row = {'path': path, 'fd': fd, 'parent': parent, 'identity': identity(os.fstat(fd)),
               'directory': data is None, 'complete': False}
        entries.append(row)
        if data is not None:
            need(len(data) < MIB and os.write(fd, data) == len(data), 'fixture write bound/short write')
        os.fsync(fd)
        row.update(image=full(os.fstat(fd)), complete=True)
        if data is None:
            parents[path] = fd
        return fd

    def inject(g):
        nonlocal expected_signal
        tick()
        previous = g['signal_epoch']
        expected_signal = True
        signal.pidfd_send_signal(self_fd, signal.SIGTERM, None, 0)
        need(not expected_signal and g['signal_epoch'] == previous + 1, 'actual self-pidfd signal delivery')

    def refused(call, message):
        try:
            call()
        except RuntimeError as exc:
            need(str(exc) == message, 'different refusal: ' + str(exc))
            return str(exc)
        raise RuntimeError('required refusal missing')

    try:
        need(sys.platform == 'linux' and os.getuid() == os.geteuid() == 0 and len(sys.argv) == 1
             and sys.flags.isolated and sys.dont_write_bytecode and sys.flags.no_site, 'fixed isolated Linux root invocation')
        os.umask(0o077)
        resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
        for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP):
            signal.signal(number, handler)
        need(not signal.pthread_sigmask(signal.SIG_BLOCK, set()), 'fixture inherited signal mask')
        self_fd = os.pidfd_open(os.getpid(), 0)
        raw = sealed(SUBJECT, 100736, '19bccc5f4441cafe771ecac0f94f3ec81a8a8a3688346f9ac0df997e8a3d2f69')
        spec = json.loads(sealed(SPEC, 5759, '90fd9b5bf6c41067ec238c4a88a94718872bcd947475c9c38e632410ffb037a0'))
        need([row['name'] for row in spec['functions']] == NAMES, 'fixed extraction list')
        lines, fragments = raw.splitlines(keepends=True), []
        for row in spec['functions']:
            part = b''.join(lines[row['first_line'] - 1:row['after_last_line'] - 1])
            need(len(part) == row['bytes'] and hashlib.sha256(part).hexdigest() == row['sha256']
                 and part.startswith(('def ' + row['name'] + '(').encode()), 'exact function extraction')
            fragments.append(part)
        with open('/proc/meminfo', 'rb') as stream:
            memory = stream.read(65537)
        need(len(memory) <= 65536, 'memory metadata cap')
        memory = dict(line.split(':', 1) for line in memory.decode('ascii').splitlines())
        total = int(memory['MemTotal'].split()[0]) * 1024
        need(int(memory['MemAvailable'].split()[0]) * 1024 >= total // 4, 'fixture RAM entry reserve')
        for parent in (ROOT.parent, KEEP.parent):
            parents[parent] = os.open(parent, ODIR)
            parent_stat = os.fstat(parents[parent])
            need(parent_stat.st_uid == 0 and not parent_stat.st_mode & 0o022, 'unsafe fixture parent')
            fs = os.fstatvfs(parents[parent])
            need(fs.f_bavail * fs.f_frsize >= 3 * GIB + 32 * MIB, 'fixture disk reserve')
        allocate(ROOT)
        allocate(KEEP)
        lock = ROOT / 'fixture.lock'
        lock_fd = allocate(lock, b'')
        fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        source, report = b'fixture source, not an application\n', b'synthetic generated diagnostic\n'
        for case in ('cancelled', 'unknown', 'repeated'):
            tick()
            case_root, evidence = ROOT / case, KEEP / case
            allocate(case_root)
            allocate(evidence)
            runtime = case_root / 'R'
            case_dirs = (runtime, runtime / 'checkout', runtime / 'checkout/build',
                         runtime / 'checkout/build/reports', runtime / 'checkout/build/reports/problems')
            for path in case_dirs:
                allocate(path)
            source_fd = allocate(runtime / 'checkout/fixture.txt', source)
            report_fd = allocate(runtime / 'checkout/build/reports/problems/problems-report.html', report)
            runtime_images = {path: full(os.lstat(path)) for path in (*case_dirs,
                runtime / 'checkout/fixture.txt', runtime / 'checkout/build/reports/problems/problems-report.html')}
            g = dict(os=os, Path=Path, hashlib=hashlib, json=json, re=re, signal=signal, stat=stat, time=time,
                     ODIR=ODIR, MIB=MIB, ROOT=ROOT, R=runtime, E=evidence, LOCK=lock, lock_fd=lock_fd,
                     RUN='SYNTHETIC_gui07_disposal_' + case, START=started, END=started + 60,
                     PARENTS=(case_root, evidence.parent), PYTHON='', INNER_PYTHON='', IMAGES=(),
                     REQUEST=evidence / 'not-a-request', APPROVAL=evidence / 'not-an-approval',
                     DEVICE={'directory_device': os.fstat(parents[runtime]).st_dev,
                             'regular_file_device': os.fstat(lock_fd).st_dev},
                     DISPOSAL_POLICY_ID='SYNTHETIC_NOT_ADMISSION', CLEANUP_DISK_RESERVE_BYTES=3 * GIB,
                     CLEANUP_RAM_RESERVE_BYTES=total // 5, host_policy_admitted=True, child=None,
                     reasons=[], resources=[], directories={}, receipt={}, signal_epoch=0,
                     cleanup_phase=None, cleanup_deadline=0.0, cleanup_last_resource=0.0,
                     cleanup_signal_epoch=None, cleanup_reason_snapshot=())
            g.update(TOP='checkout home tmp jna sqlite gradle-home konan android-user xdg-cache xdg-config xdg-data xdg-state workers mainnav pvu003 pva027'.split(),
                     CACHE_REPORT_LIMITS={'roots': 128, 'files': 16, 'file_bytes': MIB, 'total_bytes': 4 * MIB, 'descendant_levels': 8, 'relative_json_bytes': 512},
                     AUXILIARY_REPORT_LIMITS={'roots': 4, 'files': 64, 'file_bytes': MIB, 'total_bytes': 4 * MIB, 'descendant_levels': 8, 'relative_json_bytes': 512},
                     PROBLEMS_REPORT=('checkout', 'build', 'reports', 'problems', 'problems-report.html'),
                     AUXILIARY_REPORT_ROOTS=tuple(('checkout', module, 'build', *tail) for tail in
                         (('reports', 'tests', 'desktopTest'), ('test-results',)) for module in ('shared', 'app-desktop')))
            for name, part in zip(NAMES, fragments):
                exec(compile(part, str(SUBJECT) + '#' + name, 'exec'), g)  # Exact selected definitions only.
            subjects.append(g)
            active = g
            g['request'] = {'lock': g['pin'](os.fstat(lock_fd)), 'disposal_policy': g['disposal_policy']()}
            for path in (ROOT, KEEP, case_root, evidence, *case_dirs):
                need(identity(os.fstat(g['directory'](path))) == identity(os.fstat(parents[path])),
                     'subject directory not original fixture allocation')
            g['output']('SOURCE-COPY.txt', source)
            source_image = g['capture'](evidence / 'SOURCE-COPY.txt')[1]
            lifecycle = {'render_attempted': False, 'mainnav_case_results': {}}
            for name in ('mainnav', 'pvu003', 'pva027'):
                filename = name.upper() + '-RESULT.json'
                g['output'](filename, g['encoded']({'synthetic': True, 'crash_diagnostics': []}))
                lifecycle['mainnav_case_results'][name] = {'result_image': g['capture'](evidence / filename)[1]}
            inject(g)
            refusal = refused(lambda: g['capture'](evidence / 'SOURCE-COPY.txt'), 'capture cancelled/expired')
            if case == 'unknown':
                g['note']('SIMULATED_UNKNOWN_HOST_CUSTODY')
                refusal = refused(lambda: g['begin_cleanup']('PRE_GUI_WORK_CANCELLED'),
                                  'unknown work/host/custody reason cannot enter cleanup')
            else:
                g['begin_cleanup']('PRE_GUI_WORK_CANCELLED')
                g['receipt']['cleanup_eligibility']['disposal_authorized'] = True  # SIMULATED upstream lifecycle proof.
                if case == 'repeated':
                    inject(g)
                    refusal = refused(lambda: g['remove_runtime']({'fixture.txt': {}}, set(), lifecycle),
                                      'cleanup interrupted/expired/uncertain')
                else:
                    g['remove_runtime']({'fixture.txt': {}}, set(), lifecycle)
                    need(not os.path.lexists(runtime) and os.fstat(parents[runtime]).st_nlink == 0, 'original R not disposed')
                    need(g['cleanup_capture'](evidence / 'GENERATED-REPORT-0001.bin')[0] == report
                         and g['cleanup_capture'](evidence / 'SOURCE-COPY.txt')[0] == source, 'retained bytes differ')
            if case != 'cancelled':
                need(os.fstat(source_fd).st_nlink == os.fstat(report_fd).st_nlink == 1
                     and os.pread(source_fd, MIB, 0) == source and os.pread(report_fd, MIB, 0) == report
                     and 'cleanup' not in g['receipt']
                     and {path: full(os.lstat(path)) for path in runtime_images} == runtime_images,
                     'refusal changed disposable files/names')
            sealed(evidence / 'SOURCE-COPY.txt', len(source), source_image['sha256'])
            need(g['pin'](os.lstat(evidence / 'SOURCE-COPY.txt')) == source_image['pin'], 'retained source identity changed')
            rows.append({'case': case, 'pass': True, 'expected_refusal': refusal, 'receipt': g['receipt'],
                         'retained_evidence': str(evidence), 'simulated_lifecycle': True})
        tick()
        sealed(SUBJECT, 100736, '19bccc5f4441cafe771ecac0f94f3ec81a8a8a3688346f9ac0df997e8a3d2f69')
    except Exception as exc:
        errors.append(type(exc).__name__ + ': ' + str(exc)[:512])
    finally:
        # Fixture allocation custody is separate from the subject's intentional refusals.
        deadline = min(started + 120, time.monotonic() + 60)
        try:
            for row in reversed(entries):
                path, fd, parent = row['path'], row['fd'], row['parent']
                if path != ROOT and ROOT not in path.parents:
                    continue  # All verification evidence is retained.
                need(time.monotonic() < deadline and row['complete'], 'fixture cleanup deadline/incomplete allocation')
                current = os.fstat(fd)
                need(identity(current) == row['identity'], 'fixture original descriptor changed')
                try:
                    named = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
                except FileNotFoundError:
                    need(current.st_nlink == 0, 'missing name without original deletion')
                    continue
                need(identity(named) == row['identity'], 'fixture original name replaced')
                if row['directory']:
                    need(not os.listdir(fd), 'fixture directory has unowned or retained entries')
                    os.rmdir(path.name, dir_fd=parent)
                else:
                    need(full(current) == full(named) == row['image'], 'fixture file changed')
                    os.unlink(path.name, dir_fd=parent)
                need(os.fstat(fd).st_nlink == 0, 'fixture original unlink not established')
            result['fixture_runtime_disposed'] = not os.path.lexists(ROOT)
        except Exception as exc:
            errors.append('FIXTURE_CLEANUP_HOLD: ' + str(exc)[:512])
        descriptors = set(parents.values()) | {row['fd'] for row in entries}
        descriptors.update(fd for g in subjects for fd, _ in g['directories'].values())
        if self_fd is not None:
            descriptors.add(self_fd)
        for fd in descriptors:
            try:
                os.close(fd)  # One close per original descriptor, including private flock lifetime.
            except OSError as exc:
                errors.append('descriptor close: ' + str(exc.errno))
        result.update(errors=errors, unexpected_signals=unexpected, elapsed=round(time.monotonic() - started, 6))
        print(json.dumps(result, sort_keys=True, allow_nan=False))
    return 0 if len(rows) == 3 and not errors and not unexpected and result.get('fixture_runtime_disposed') else 1


if __name__ == '__main__':
    sys.exit(main())
