"""Permanent deterministic regression source for the prospective screen only.

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

import screen_replacement as subject


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


class ScreenRegression(unittest.TestCase):
    def invoke(self, kernel, failure=False):
        receipt = {}
        bindings = {
            'churn': 0, 'host_policy_admitted': True, 'require': require,
            'HOST_POLICY_ID': 'GUI6_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_V4',
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

    def missing(self, kernel, leaf='ns/mnt', number=1, terminal=True):
        kernel.faults[leaf, number] = FileNotFoundError(errno.ENOENT, 'synthetic task missing')
        kernel.after[leaf, number] = lambda k: setattr(k, 'terminal', terminal)

    def test_live_anchor_binding_precedes_every_positive_observation(self):
        kernel = Kernel()
        receipt, churn = self.invoke(kernel)
        self.assertEqual(kernel.events[:4], [
            ('pidfd-open', 42, 0), ('anchor-open',), ('poll', kernel.candidate_fd),
            ('observe', 'comm', 1, kernel.anchor_fd)])
        self.assertEqual(churn, 0)
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
        self.assertEqual(kernel.counts['stat'], 4)

    def test_terminal_before_binding_never_samples_a_replacement_numeric_task(self):
        kernel = Kernel()
        kernel.terminal = kernel.numeric_replacement_live = True
        kernel.namespace = {'pid': 'pid:[foreign]', 'mnt': 'mnt:[foreign]'}
        receipt, churn = self.invoke(kernel)
        self.assertEqual(kernel.counts, {})
        self.assertEqual(churn, 1)
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
        for terminal in (False, True):
            with self.subTest(anchor_missing=True, terminal=terminal):
                kernel = Kernel()
                kernel.anchor_missing = True
                kernel.terminal = terminal
                receipt, churn = self.invoke(kernel, failure=not terminal)
                self.assertEqual(kernel.counts, {})
                self.assertEqual(churn, int(terminal))
                self.assertEqual(kernel.close_attempts, [kernel.candidate_fd])
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                if not terminal:
                    self.assertEqual(receipt['host_screen_first_failure']['stage'], 'proc-anchor-open')

    def test_first_proof_missing_terminal_is_not_owned_or_global_idle(self):
        kernel = Kernel()
        self.missing(kernel)
        receipt, churn = self.invoke(kernel)
        diagnostic = receipt['host_screen_first_nonlive_unknown']
        self.assertEqual(receipt['host_screen_nonlive_unknown_count'], 1)
        self.assertEqual(diagnostic['classification'], 'NONLIVE_UNKNOWN')
        self.assertFalse(diagnostic['owned_proof'])
        self.assertTrue(diagnostic['proc_anchor_bound'])
        self.assertEqual(diagnostic['stage'], 'proof-namespace:mnt')
        self.assertEqual(diagnostic['pidfd_observations']['candidate-at-enoent'], [select.POLLIN])
        self.assertEqual(churn, 0)

    def test_missing_leader_namespace_while_group_live_stays_fatal(self):
        # This models, but does not establish on Linux, the distinct thread-group
        # lifetime contract required by the real synthetic lifecycle check.
        kernel = Kernel()
        self.missing(kernel, terminal=False)
        receipt, _ = self.invoke(kernel, failure=True)
        self.assertEqual(receipt['host_screen_first_failure']['stage'], 'proof-namespace:mnt')
        self.assertIn('without original terminal', receipt['host_screen_first_failure']['error']['message'])
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_terminal_at_postread_checks_does_not_move_the_departure_failure(self):
        for stat_number, parent in ((2, False), (4, False), (4, True)):
            with self.subTest(stat_number=stat_number, parent=parent):
                kernel = Kernel()
                if parent:
                    kernel.namespace = dict(kernel.parent)
                kernel.after['stat', stat_number] = lambda k: setattr(k, 'terminal', True)
                receipt, _ = self.invoke(kernel)
                if stat_number == 4 and not parent:
                    self.assertEqual(receipt['host_screen_owned_terminal_postread_count'], 1)
                    self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                else:
                    self.assertEqual(receipt['host_screen_nonlive_unknown_count'], 1)

    def test_foreign_prefix_or_direct_parent_child_cannot_be_hidden_by_later_enoent(self):
        for direct_parent in (False, True):
            with self.subTest(direct_parent=direct_parent):
                kernel = Kernel()
                if direct_parent:
                    kernel.namespace = dict(kernel.parent)
                    kernel.birth_ppid = 99
                else:
                    kernel.namespace['pid'] = 'pid:[foreign]'
                self.missing(kernel)
                receipt, _ = self.invoke(kernel, failure=True)
                self.assertEqual(receipt['host_screen_first_failure']['stage'], 'proof-namespace:pid')
                self.assertNotIn('ns/mnt', kernel.counts)
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_observed_birth_comm_or_namespace_mismatch_is_sticky(self):
        for leaf, number, value in (('stat', 2, 'birth'), ('stat', 3, 'birth'),
                                    ('comm', 2, 'other\n'), ('ns/pid', 2, 'pid:[foreign]')):
            with self.subTest(leaf=leaf, number=number):
                kernel = Kernel()
                kernel.values[leaf, number] = kernel.stat_text(start=999) if value == 'birth' else value
                kernel.after[leaf, number] = lambda k: setattr(k, 'terminal', True)
                receipt, _ = self.invoke(kernel, failure=True)
                expected_stage = {('stat', 2): 'proof-birth-reread', ('stat', 3): 'reread-birth-first',
                                  ('comm', 2): 'reread-comm', ('ns/pid', 2): 'reread-namespace:pid'}[leaf, number]
                self.assertEqual(receipt['host_screen_first_failure']['stage'], expected_stage)
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                self.assertNotIn('host_screen_owned_terminal_postread_count', receipt)

    def test_owner_proof_and_owned_departure_keep_owner_live_requirement(self):
        for at_enoent in (False, True):
            with self.subTest(at_enoent=at_enoent):
                kernel = Kernel()
                if at_enoent:
                    self.missing(kernel, 'ns/mnt', 2)
                    kernel.after['ns/mnt', 2] = lambda k: (
                        setattr(k, 'terminal', True), setattr(k, 'owner_terminal', True))
                else:
                    kernel.poll_hooks[kernel.owner_fd, 1] = lambda k: (
                        setattr(k, 'terminal', True), setattr(k, 'owner_terminal', True))
                receipt, _ = self.invoke(kernel, failure=True)
                self.assertEqual(receipt['host_screen_first_failure']['stage'],
                                 'reread-namespace:mnt' if at_enoent else 'proof-owner-pidfd')
                self.assertIn('owner', receipt['host_screen_first_failure']['error']['message'])
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
                self.assertNotIn('host_screen_owned_terminal_enoent_count', receipt)

    def test_unrecognized_errors_and_malformed_data_are_not_departure(self):
        for error_number in (errno.EACCES, errno.EIO, errno.ESRCH):
            with self.subTest(errno=error_number):
                kernel = Kernel()
                kernel.faults['ns/mnt', 1] = OSError(error_number, 'synthetic non-elected error')
                kernel.after['ns/mnt', 1] = lambda k: setattr(k, 'terminal', True)
                receipt, _ = self.invoke(kernel, failure=True)
                self.assertEqual(receipt['host_screen_first_failure']['stage'], 'proof-namespace:mnt')
                self.assertEqual(receipt['host_screen_first_failure']['error']['errno'], error_number)
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)
        kernel = Kernel()
        kernel.values['stat', 1] = 'malformed\n'
        kernel.after['stat', 1] = lambda k: setattr(k, 'terminal', True)
        receipt, _ = self.invoke(kernel, failure=True)
        self.assertEqual(receipt['host_screen_first_failure']['stage'], 'proof-birth')
        self.assertIn('malformed', receipt['host_screen_first_failure']['error']['message'])
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_invalid_terminal_readiness_is_never_an_exemption(self):
        kernel = Kernel()
        self.missing(kernel)
        kernel.invalid_masks[kernel.candidate_fd, 2] = select.POLLIN | select.POLLERR
        receipt, _ = self.invoke(kernel, failure=True)
        self.assertEqual(receipt['host_screen_first_failure']['stage'], 'proof-namespace:mnt')
        self.assertIn('uncertain pidfd readiness', receipt['host_screen_first_failure']['error']['message'])
        self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_close_failures_remain_fatal_and_each_other_descriptor_still_closes(self):
        for anchor_close in (False, True):
            with self.subTest(anchor_close=anchor_close):
                kernel = Kernel()
                if anchor_close:
                    kernel.close_errors[kernel.anchor_fd] = OSError(errno.EIO, 'synthetic anchor close failure')
                else:
                    # Even an unexpected ENOENT from close is not a missing-task
                    # read and must not be consumed by the departure classifier.
                    kernel.leaf_close_error = FileNotFoundError(errno.ENOENT, 'synthetic leaf close failure')
                    kernel.after['comm', 1] = lambda k: setattr(k, 'terminal', True)
                receipt, _ = self.invoke(kernel, failure=True)
                self.assertIn('proc_close_error' if anchor_close else 'leaf_close_error',
                              receipt['host_screen_first_failure'])
                self.assertNotIn('host_screen_nonlive_unknown_count', receipt)

    def test_old_policy_cannot_implicitly_admit_the_new_terminal_classification(self):
        kernel = Kernel()
        with patch.dict(subject.__dict__, {'host_policy_admitted': True, 'require': require,
                                         'HOST_POLICY_ID': 'GUI6_SCHEDULING_ONLY_ORIGINAL_PARENT_UNKNOWN_V3',
                                         'os': kernel.os_module()}):
            with self.assertRaisesRegex(RuntimeError, 'genuine fresh exact-instance intake'):
                subject.screen()
        self.assertEqual(kernel.opens, {})


if __name__ == '__main__':
    unittest.main()
