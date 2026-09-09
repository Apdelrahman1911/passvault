#!/usr/bin/python3
"""Finite namespace-init SELF metadata only; root's separate caller is required."""

import json
import os
import re
import resource
import signal
import sys

SELF = ('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/'
        'reviews/storage/LINUX-NAMESPACE-FEASIBILITY-PROBE.py')


def require(condition, message):
    if not condition:
        raise SystemExit('NAMESPACE_SELF_REFUSED: ' + message)


def read_bounded(path, cap):
    with open(path, 'rb') as stream:
        data = stream.read(cap + 1)
    require(len(data) <= cap, 'self metadata byte cap')
    return data.decode('ascii')


def cancelled(number, _frame):
    os._exit(124 if number == signal.SIGALRM else 128 + number)


def main():
    require(sys.platform == 'linux' and len(sys.argv) == 3 and sys.argv[0] == SELF
            and sys.flags.isolated and sys.dont_write_bytecode and sys.flags.no_site,
            'fixed isolated no-site entry and two original parent namespace IDs')
    require(os.getpid() == 1 and os.getppid() == 0, 'new namespace init required')
    parent = dict(zip(('pid', 'mnt'), sys.argv[1:]))
    require(all(re.fullmatch(kind + r':\[[0-9]+\]', value) for kind, value in parent.items()),
            'literal original parent namespace IDs required')
    for sig in (signal.SIGALRM, signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
        signal.signal(sig, cancelled)
    signal.alarm(3)
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    resource.setrlimit(resource.RLIMIT_CPU, (2, 2))
    resource.setrlimit(resource.RLIMIT_AS, (64 * 1024 * 1024, 64 * 1024 * 1024))
    resource.setrlimit(resource.RLIMIT_NOFILE, (32, 32))
    current = {kind: os.readlink('/proc/self/ns/' + kind) for kind in parent}
    require(all(current[kind] != parent[kind] for kind in parent), 'namespace not distinct from parent')
    status = dict(line.split(':', 1) for line in read_bounded('/proc/self/status', 32768).splitlines())
    require(all(status[name].strip() == value for name, value in (
        ('Tgid', '1'), ('Pid', '1'), ('PPid', '0'), ('NSpid', '1'),
    )), 'proc self status does not describe this namespace init')
    rows = [line.split() for line in read_bounded('/proc/self/mountinfo', 131072).splitlines()]
    require(0 < len(rows) <= 2048, 'self mount count')
    proc_stat = os.stat('/proc')
    proc_dev = '%s:%s' % (os.major(proc_stat.st_dev), os.minor(proc_stat.st_dev))
    visible_proc = []
    for row in rows:
        require('-' in row, 'mountinfo separator')
        separator = row.index('-')
        require(separator >= 6 and len(row) >= separator + 4, 'mountinfo fields')
        require(not any(tag.startswith(('shared:', 'master:', 'propagate_from:'))
                        for tag in row[6:separator]), 'mount propagation is not private')
        if row[4] == '/proc' and row[2] == proc_dev:
            require(row[separator + 1] == 'proc'
                    and {'nosuid', 'nodev', 'noexec'} <= set(row[5].split(',')), 'visible proc mount flags/type')
            visible_proc.append(row[0])
    require(visible_proc, 'visible private proc mount absent')
    output = json.dumps({
        'format': 'passvault-linux-namespace-self-v1', 'pid': 1, 'ppid': 0,
        'parent_namespaces': parent, 'self_namespaces': current, 'proc_nspid': [1],
        'private_mount_propagation': True, 'visible_proc_device': proc_dev,
        'self_mount_count': len(rows), 'scope': 'FINITE_SELF_METADATA_NOT_WORKLOAD_CLEANUP_PROOF',
    }, sort_keys=True, separators=(',', ':')).encode('ascii') + b'\n'
    require(len(output) <= 1024 and os.write(1, output) == len(output), 'bounded complete metadata output')


if __name__ == '__main__':
    main()
