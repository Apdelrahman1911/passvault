"""UNEXECUTED source candidate. Root must admit exact in-memory SCREEN_SOURCE first."""
import hashlib
import json

SCREEN_SHA256 = 'b7e227cdb7accac627d70971d9295125cbefd689aaf9722f683e127ad115ccba'
assert type(SCREEN_SOURCE) is str and hashlib.sha256(SCREEN_SOURCE.encode()).hexdigest() == SCREEN_SHA256
SCREEN_CODE = compile(SCREEN_SOURCE, '<frozen-prospective-screen>', 'exec')  # No holder-module import.

def require(value, message):
    if not value:
        raise RuntimeError(message)

class Box:
    def __init__(self, **values): self.__dict__.update(values)

class Entries:
    def __enter__(self): return [Box(name='101')]
    def __exit__(self, *_): return False

safe = dict(int=int, str=str, dict=dict, bool=bool, len=len, min=min, enumerate=enumerate,
    type=type, getattr=getattr, Exception=Exception, RuntimeError=RuntimeError, OSError=OSError,
    FileNotFoundError=FileNotFoundError, ProcessLookupError=ProcessLookupError)
owned, results = {'pid': 'pid:[2]', 'mnt': 'mnt:[3]'}, []
cases = ('owned_enoent', 'candidate_live', 'owner_terminal', 'preproof_enoent',
         'foreign_namespace', 'birth_mismatch', 'live_owned', 'close_failure')
for case in cases:
    trace, state = [], {'birth': 0, 'ns': {'pid': 0, 'mnt': 0}, 'missing': False,
                        'gone': False, 'later_reads': 0, 'closes': [], 'polls': []}
    def after_mismatch():
        if state['gone']:
            state['later_reads'] += 1
            state['missing'] = True
            raise FileNotFoundError(2, 'synthetic later disappearance')
    def read_comm(path):
        assert path == '/proc/101/comm'
        trace.append('comm'); after_mismatch()
        return 'java\n'
    def readlink(path):
        namespace = path.rsplit('/', 1)[1]
        assert namespace in owned and path == '/proc/101/ns/' + namespace
        trace.append('ns:' + namespace); after_mismatch()
        state['ns'][namespace] += 1
        if namespace == 'mnt' and ((case == 'preproof_enoent' and state['ns'][namespace] == 1)
                or (case in ('owned_enoent', 'candidate_live', 'owner_terminal', 'close_failure')
                    and state['ns'][namespace] == 2)):
            state['missing'] = True
            raise FileNotFoundError(2, 'synthetic namespace disappearance')
        return 'mnt:[9]' if case == 'foreign_namespace' and namespace == 'mnt' else owned[namespace]
    def birth(pid):
        assert pid == 101
        trace.append('birth'); after_mismatch()
        state['birth'] += 1
        value = {'pid': 101, 'ppid': 77, 'start': 1001}
        if case == 'birth_mismatch' and state['birth'] == 3:
            value['start'] = 1002
            state['gone'] = True  # Any later proc read would now disappear; mismatch must win first.
        return value
    def pidfd_open(pid, flags):
        assert pid == 101 and flags == 0
        trace.append('open'); return 11
    def close(fd):
        state['closes'].append(fd); trace.append('close')
        assert fd == 11  # Never close or signal the original owner descriptor22.
        if case == 'close_failure': raise OSError(5, 'synthetic close uncertainty')
    def scandir(path):
        assert path == '/proc'
        return Entries()
    class Poll:
        def register(self, fd, mask):
            assert fd in (11, 22) and mask == 1
            self.fd = fd
        def poll(self, timeout):
            assert timeout == 0
            trace.append('poll:' + str(self.fd)); state['polls'].append(self.fd)
            ended = state['missing'] and ((self.fd == 11 and case != 'candidate_live')
                                          or (self.fd == 22 and case == 'owner_terminal'))
            return [(self.fd, 1)] if ended else []
    environment = {'__builtins__': safe, 'require': require, 'churn': 0, 'END': 6000,
        'receipt': {}, 'owned_domain': dict(owned), 'child': {'p': Box(pid=77, returncode=None), 'fd': 22},
        'time': Box(monotonic=lambda: 0), 'select': Box(POLLIN=1, POLLHUP=16, poll=Poll),
        'os': Box(scandir=scandir, pidfd_open=pidfd_open, readlink=readlink, close=close),
        'Path': lambda path: Box(read_text=lambda: read_comm(path)), 'birth': birth,
        'BUILDLIKE': Box(fullmatch=lambda comm: comm == 'java'),
        'encoded': lambda value: json.dumps(value, sort_keys=True, separators=(',', ':'), allow_nan=False).encode() + b'\n',
        'decode': json.loads}
    exec(SCREEN_CODE, environment)  # Only the frozen function; every host-operation name is a fixed stub.
    outcome = 'ACCEPT'
    try: environment['screen']()
    except RuntimeError: outcome = 'HOLD'
    assert outcome == ('ACCEPT' if case in ('owned_enoent', 'live_owned') else 'HOLD'), case
    assert trace[:2] == ['open', 'comm'] and state['closes'] == [11] and environment['churn'] == 0, case
    receipt = environment['receipt']
    expected_polls = [] if case == 'preproof_enoent' else ([11] if case == 'foreign_namespace' else
        ([11, 22] if case == 'birth_mismatch' else ([11, 22, 11] if case == 'candidate_live' else [11, 22, 11, 22])))
    assert state['polls'] == expected_polls, case
    assert receipt.get('host_screen_owned_terminal_enoent_count', 0) == int(case in ('owned_enoent', 'close_failure')), case
    failure, accepted = receipt.get('host_screen_first_failure'), receipt.get('host_screen_first_owned_terminal_enoent')
    assert (failure is not None) == (outcome == 'HOLD'), case
    if accepted:
        assert accepted['owned_proof'] and accepted['reread_enoent']['errno'] == 2
        assert accepted['pidfd_observations']['candidate-at-enoent'] == [1]
        assert accepted['pidfd_observations']['owner-at-enoent'] == [] and 'close_error' not in accepted
    if failure:
        stage = {'preproof_enoent': 'proof-namespace:mnt', 'foreign_namespace': 'proof-owned-domain',
                 'birth_mismatch': 'reread-birth-first'}.get(case, 'reread-namespace:mnt')
        assert failure['pid'] == 101 and failure['owner_pid'] == 77 and failure['stage'] == stage
        assert failure['owned_proof'] == (case not in ('preproof_enoent', 'foreign_namespace'))
        if case == 'close_failure':
            assert failure['close_error']['type'] == 'OSError' and failure['close_error']['errno'] == 5 and accepted
        else:
            assert failure['error']['type'] == ('FileNotFoundError' if case == 'preproof_enoent' else 'RuntimeError')
        if case == 'preproof_enoent':
            assert failure['error']['errno'] == 2 and failure['identity']['namespaces'] == {'pid': 'pid:[2]'}
        if case == 'foreign_namespace': assert failure['identity']['namespaces']['mnt'] == 'mnt:[9]'
        if case == 'candidate_live': assert failure['pidfd_observations']['candidate-at-enoent'] == []
        if case == 'owner_terminal': assert failure['pidfd_observations']['owner-at-enoent'] == [1]
        if case == 'birth_mismatch':
            assert failure['identity']['start'] == 1001 and failure['observed_birth']['start'] == 1002
            assert state['gone'] and state['later_reads'] == 0  # A deferred comparison would reach synthetic ENOENT.
    if case == 'live_owned': assert state['birth'] == 4 and state['ns'] == {'pid': 2, 'mnt': 2} and not receipt
    results.append({'scenario': case, 'outcome': outcome, 'trace': trace,
                    'failure_stage': None if failure is None else failure['stage']})
print(json.dumps({'format': 'prospective-screen-memory-controls-v1', 'source_sha256': SCREEN_SHA256,
    'screen_invocations': len(results), 'results': results, 'qualification':
    'Eight synthetic branch controls only; no real proc/pidfd/namespace, application case, holder execution, retry or admission proof.'},
    sort_keys=True, separators=(',', ':')))
