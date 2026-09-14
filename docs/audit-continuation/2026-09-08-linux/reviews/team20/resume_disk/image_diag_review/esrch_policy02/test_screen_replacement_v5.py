"""Focused deterministic V5 regression source; V4 tests remain untouched.

Imports no outer/inner runner. All proc, descriptor, clock, and readiness APIs
are fakes; no processes, signals, namespace access, or cleanup is performed.
These checks do NOT prove Linux proc-dirfd or whole-thread-group pidfd contracts.
"""

import errno
import json
import os
import re
import select
import types
import unittest
from unittest.mock import patch

import screen_replacement_v5 as subject


def require(value, message):
    if not value:
        raise RuntimeError(message)


class Entries:
    def __enter__(self):
        return iter([types.SimpleNamespace(name='42')])

    def __exit__(self, *_):
        return False


class Kernel:
    """A single candidate. Numeric replacement and anchored observations differ."""

    candidate_fd = 2048
    owner_fd = 2049
    anchor_fd = 2050
    parent = {'pid': 'pid:[100]', 'mnt': 'mnt:[101]'}
    owned = {'pid': 'pid:[200]', 'mnt': 'mnt:[201]'}

    def __init__(self):
        self.namespace = dict(self.owned)
        self.birth_ppid = 80
        self.terminal = False
        self.owner_terminal = False
        self.values = {}
        self.faults = {}
        self.after = {}
        self.counts = {}
        self.opens = {}
        self.closes = []
        self.close_attempts = []
        self.events = []
        self.poll_counts = {}
        self.poll_hooks = {}
        self.invalid_masks = {}
        self.close_errors = {}
        self.leaf_close_error = None
        self.next_leaf = 3000
        self.anchor_missing = False
        self.numeric_replacement_live = False

    def stat_text(self, start=123):
        fields = ['0'] * 20
        fields[0], fields[1], fields[19] = 'S', str(self.birth_ppid), str(start)
        return '42 (java) ' + ' '.join(fields) + '\n'

    def observation(self, leaf):
        count = self.counts[leaf] = self.counts.get(leaf, 0) + 1
        key = leaf, count
        self.events.append(('observe', leaf, count, self.anchor_fd))
        if key in self.after:
            self.after[key](self)
        if key in self.faults:
            raise self.faults[key]
        if key in self.values:
            return self.values[key]
        if leaf == 'comm':
            return 'java\n'
        if leaf == 'stat':
            return self.stat_text()
        return self.namespace[leaf.split('/')[1]]

    def pidfd_open(self, pid, flags):
        assert (pid, flags) == (42, 0)
        self.events.append(('pidfd-open', pid, flags))
        self.opens[self.candidate_fd] = None
        return self.candidate_fd

    def open(self, path, flags, *, dir_fd=None):
        if path == '/proc/42':
            assert dir_fd is None and self.candidate_fd in self.opens
            assert flags & (os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC) == (
                os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC)
            self.events.append(('anchor-open',))
            if self.anchor_missing:
                raise FileNotFoundError(errno.ENOENT, 'synthetic task directory missing')
            self.opens[self.anchor_fd] = None
            return self.anchor_fd
        # Any later numeric /proc path, sibling anchor, or ordinary symlink
        # lookup is a contract violation rather than another process sample.
        assert dir_fd == self.anchor_fd and path in ('comm', 'stat')
        assert flags & (os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC) == (
            os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC)
        data = self.observation(path).encode('utf-8')
        fd = self.next_leaf
        self.next_leaf += 1
        self.opens[fd] = [data, 0]
        return fd

    def read(self, fd, cap):
        data, position = self.opens[fd]
        part = data[position:position + cap]
        self.opens[fd][1] += len(part)
        return part

    def readlink(self, path, *, dir_fd=None):
        assert dir_fd == self.anchor_fd and path in ('ns/pid', 'ns/mnt')
        return self.observation(path)

    def close(self, fd):
        # Record before assertions: an expected scanner failure must not hide a
        # duplicate close by swallowing the fake's AssertionError.
        self.close_attempts.append(fd)
        assert fd in self.opens and fd not in self.closes
        self.closes.append(fd)
        if fd in self.close_errors:
            raise self.close_errors[fd]
        if fd >= 3000 and self.leaf_close_error is not None:
            error, self.leaf_close_error = self.leaf_close_error, None
            raise error

    def poll(self, fd):
        self.events.append(('poll', fd))
        count = self.poll_counts[fd] = self.poll_counts.get(fd, 0) + 1
        if (fd, count) in self.poll_hooks:
            self.poll_hooks[fd, count](self)
        if (fd, count) in self.invalid_masks:
            return [(fd, self.invalid_masks[fd, count])]
        terminal = self.owner_terminal if fd == self.owner_fd else self.terminal
        return [(fd, select.POLLIN)] if terminal else []

    def select_module(self):
        kernel = self

        class Poll:
            def register(self, fd, mask):
                assert mask == select.POLLIN
                self.fd = fd

            def poll(self, timeout):
                assert timeout == 0
                return kernel.poll(self.fd)

        return types.SimpleNamespace(POLLIN=select.POLLIN, POLLHUP=select.POLLHUP, poll=Poll)

    def os_module(self):
        return types.SimpleNamespace(
            O_RDONLY=os.O_RDONLY, O_DIRECTORY=os.O_DIRECTORY, O_NOFOLLOW=os.O_NOFOLLOW,
            O_NONBLOCK=os.O_NONBLOCK, O_CLOEXEC=os.O_CLOEXEC,
            pidfd_open=self.pidfd_open, open=self.open, read=self.read, readlink=self.readlink,
            close=self.close, scandir=lambda path: Entries() if path == '/proc' else None)


class ReadFailureKernel(Kernel):
    """Inject an errno from read itself, after a successful anchored leaf open."""

    def __init__(self, leaf, number, terminal):
        super().__init__()
        self.fail_read_at = leaf, number
        self.fail_read_fd = None
        self.read_terminal = terminal

    def open(self, path, flags, *, dir_fd=None):
        fd = super().open(path, flags, dir_fd=dir_fd)
        if dir_fd == self.anchor_fd and (path, self.counts[path]) == self.fail_read_at:
            self.fail_read_fd = fd
        return fd

    def read(self, fd, cap):
        if fd == self.fail_read_fd:
            self.terminal = self.read_terminal
            raise ProcessLookupError(errno.ESRCH, 'synthetic anchored read ESRCH')
        return super().read(fd, cap)


class EsrchScreenRegression(unittest.TestCase):
    def invoke(self, kernel, failure=False):
        receipt = {}
        bindings = {
            'churn': 0, 'host_policy_admitted': True, 'require': require,
            'HOST_POLICY_ID': 'GUI7_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_ERRNO_V5',
            'os': kernel.os_module(), 'select': kernel.select_module(),
            'time': types.SimpleNamespace(monotonic=lambda: 10.0), 'END': 100.0,
            'encoded': lambda value: json.dumps(value, sort_keys=True).encode(),
            'decode': json.loads, 'receipt': receipt,
            'BUILDLIKE': re.compile(r'java|gradle.*'), 'parent_ns': kernel.parent,
            'owned_domain': kernel.owned,
            'child': {'fd': kernel.owner_fd, 'isolated': True,
                      'p': types.SimpleNamespace(pid=99, returncode=None)},
        }
        with patch.dict(subject.__dict__, bindings):
            if failure:
                with self.assertRaisesRegex(RuntimeError, 'host-screen candidate uncertainty'):
                    subject.screen()
                self.assertIn('host_screen_first_failure', receipt)
            else:
                subject.screen()
            churn = subject.churn
        # Every opened candidate/anchor/leaf receives one close attempt on every
        # path, including a close failure. The unknown namespace owner is not ours
        # to close/signal here and is never included in this opened set.
        self.assertCountEqual(kernel.close_attempts, list(kernel.opens))
        self.assertEqual(len(kernel.close_attempts), len(set(kernel.close_attempts)))
        self.assertCountEqual(kernel.closes, list(kernel.opens))
        return receipt, churn

    def missing(self, kernel, leaf='ns/mnt', number=1, terminal=True, code=errno.ESRCH):
        kernel.faults[leaf, number] = OSError(code, 'synthetic anchored missing read')
        kernel.after[leaf, number] = lambda k: setattr(k, 'terminal', terminal)

    def test_open_read_and_readlink_esrch_need_terminal_original_group(self):
        for operation in ('open', 'read', 'readlink'):
            for terminal in (False, True):
                with self.subTest(operation=operation, terminal=terminal):
                    kernel = ReadFailureKernel('stat', 1, terminal) if operation == 'read' else Kernel()
                    if operation != 'read':
                        self.missing(kernel, 'stat' if operation == 'open' else 'ns/mnt', terminal=terminal)
                    receipt, churn = self.invoke(kernel, failure=not terminal)
                    diagnostic = receipt['host_screen_first_nonlive_unknown' if terminal else 'host_screen_first_failure']
                    self.assertEqual(diagnostic['stage'], 'proof-namespace:mnt' if operation == 'readlink' else 'proof-birth')
                    self.assertEqual(diagnostic['read_esrch']['errno'], errno.ESRCH)
                    self.assertNotIn('read_enoent', diagnostic)
                    self.assertTrue(diagnostic['proc_anchor_bound'])
                    self.assertFalse(diagnostic['owned_proof'])
                    self.assertEqual(diagnostic['pidfd_observations']['candidate-at-esrch'], [select.POLLIN] if terminal else [])
                    self.assertEqual(churn, 0)
                    if terminal:
                        self.assertEqual(receipt['host_screen_nonlive_unknown_count'], 1)
                        self.assertEqual(diagnostic['classification'], 'NONLIVE_UNKNOWN')
                    else:
                        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                        self.assertIn('without original terminal', diagnostic['error']['message'])

    def test_prepositive_esrch_is_only_churn_and_prebinding_stays_fatal(self):
        for terminal in (False, True):
            with self.subTest(prepositive_terminal=terminal):
                kernel = Kernel()
                self.missing(kernel, 'comm', terminal=terminal)
                receipt, churn = self.invoke(kernel, failure=not terminal)
                self.assertEqual(churn, int(terminal))
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                self.assertNotIn('stat', kernel.counts)
                if not terminal:
                    diagnostic = receipt['host_screen_first_failure']
                    self.assertEqual(diagnostic['stage'], 'comm-initial')
                    self.assertEqual(diagnostic['read_esrch']['errno'], errno.ESRCH)
                    self.assertEqual(diagnostic['pidfd_observations']['prepositive-esrch'], [])

        class MissingAnchor(Kernel):
            def open(self, path, flags, *, dir_fd=None):
                assert path == '/proc/42' and dir_fd is None
                raise ProcessLookupError(errno.ESRCH, 'synthetic prebinding anchor error')

        kernel = MissingAnchor()
        kernel.terminal = True
        receipt, churn = self.invoke(kernel, failure=True)
        diagnostic = receipt['host_screen_first_failure']
        self.assertEqual(diagnostic['stage'], 'proc-anchor-open')
        self.assertFalse(diagnostic['proc_anchor_bound'])
        self.assertEqual(diagnostic['error']['errno'], errno.ESRCH)
        self.assertEqual(kernel.counts, {})
        self.assertEqual(kernel.close_attempts, [kernel.candidate_fd])
        self.assertEqual(churn, 0)
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_foreign_or_birth_mismatch_is_sticky_before_later_esrch(self):
        for mismatch in ('foreign-prefix', 'birth'):
            with self.subTest(mismatch=mismatch):
                kernel = Kernel()
                if mismatch == 'foreign-prefix':
                    kernel.namespace['pid'] = 'pid:[foreign]'
                    self.missing(kernel)
                else:
                    kernel.values['stat', 2] = kernel.stat_text(start=999)
                    kernel.after['stat', 2] = lambda k: setattr(k, 'terminal', True)
                    self.missing(kernel, 'comm', 2)
                receipt, _ = self.invoke(kernel, failure=True)
                diagnostic = receipt['host_screen_first_failure']
                self.assertEqual(diagnostic['stage'], 'proof-namespace:pid' if mismatch == 'foreign-prefix' else 'proof-birth-reread')
                self.assertNotIn('read_esrch', diagnostic)
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                if mismatch == 'foreign-prefix':
                    self.assertNotIn('ns/mnt', kernel.counts)
                else:
                    self.assertEqual(kernel.counts['comm'], 1)

    def test_fully_owned_esrch_requires_original_owner_live(self):
        for owner_terminal in (False, True):
            with self.subTest(owner_terminal=owner_terminal):
                kernel = Kernel()
                self.missing(kernel, number=2)
                kernel.after['ns/mnt', 2] = lambda k: (
                    setattr(k, 'terminal', True), setattr(k, 'owner_terminal', owner_terminal))
                receipt, _ = self.invoke(kernel, failure=owner_terminal)
                diagnostic = receipt['host_screen_first_failure' if owner_terminal else 'host_screen_first_owned_terminal_esrch']
                self.assertTrue(diagnostic['owned_proof'])
                self.assertEqual(diagnostic['stage'], 'reread-namespace:mnt')
                self.assertEqual(diagnostic['read_esrch']['errno'], errno.ESRCH)
                self.assertEqual(diagnostic['reread_esrch'], diagnostic['read_esrch'])
                self.assertNotIn('read_enoent', diagnostic)
                self.assertNotIn('reread_enoent', diagnostic)
                self.assertEqual(diagnostic['pidfd_observations']['owner-at-esrch'], [select.POLLIN] if owner_terminal else [])
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                self.assertNotIn('host_screen_owned_terminal_enoent_count', receipt)
                if owner_terminal:
                    self.assertNotIn('host_screen_owned_terminal_esrch_count', receipt)
                    self.assertIn('owner', diagnostic['error']['message'])
                else:
                    self.assertEqual(receipt['host_screen_owned_terminal_esrch_count'], 1)

    def test_nonmissing_errors_and_esrch_close_error_stay_fatal(self):
        for code in (errno.EACCES, errno.EIO):
            with self.subTest(nonmissing_errno=code):
                kernel = Kernel()
                self.missing(kernel, code=code)
                receipt, _ = self.invoke(kernel, failure=True)
                diagnostic = receipt['host_screen_first_failure']
                self.assertEqual(diagnostic['error']['errno'], code)
                self.assertNotIn('read_esrch', diagnostic)
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
        kernel = Kernel()
        kernel.leaf_close_error = ProcessLookupError(errno.ESRCH, 'synthetic leaf CLOSE error')
        kernel.after['comm', 1] = lambda k: setattr(k, 'terminal', True)
        receipt, _ = self.invoke(kernel, failure=True)
        diagnostic = receipt['host_screen_first_failure']
        self.assertEqual(diagnostic['stage'], 'comm-initial')
        self.assertEqual(diagnostic['leaf_close_error']['errno'], errno.ESRCH)
        self.assertNotIn('read_esrch', diagnostic)
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_enoent_compatibility_keeps_enoent_specific_evidence(self):
        for fully_owned in (False, True):
            with self.subTest(fully_owned=fully_owned):
                kernel = Kernel()
                self.missing(kernel, number=2 if fully_owned else 1, code=errno.ENOENT)
                receipt, _ = self.invoke(kernel)
                diagnostic = receipt['host_screen_first_owned_terminal_enoent' if fully_owned else 'host_screen_first_nonlive_unknown']
                self.assertEqual(diagnostic['read_enoent']['errno'], errno.ENOENT)
                self.assertNotIn('read_esrch', diagnostic)
                self.assertEqual(diagnostic['pidfd_observations']['candidate-at-enoent'], [select.POLLIN])
                self.assertEqual(diagnostic['owned_proof'], fully_owned)
                self.assertEqual(receipt['host_screen_owned_terminal_enoent_count' if fully_owned else 'host_screen_nonlive_unknown_count'], 1)

    def test_invalid_pidfd_mask_and_v4_policy_are_not_admitted(self):
        kernel = Kernel()
        self.missing(kernel)
        kernel.invalid_masks[kernel.candidate_fd, 2] = select.POLLIN | select.POLLERR
        receipt, _ = self.invoke(kernel, failure=True)
        self.assertIn('uncertain pidfd readiness', receipt['host_screen_first_failure']['error']['message'])
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
        kernel = Kernel()
        with patch.dict(subject.__dict__, {'host_policy_admitted': True, 'require': require,
                                         'HOST_POLICY_ID': 'GUI6_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_V4',
                                         'os': kernel.os_module()}):
            with self.assertRaisesRegex(RuntimeError, 'genuine fresh exact-instance intake'):
                subject.screen()
        self.assertEqual(kernel.opens, {})


if __name__ == '__main__':
    unittest.main()
