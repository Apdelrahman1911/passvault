"""Inert prospective screen replacement; no imports, launch, or old-run authority.

Root inserts only screen() into a separately reviewed future controller. The
companion deterministic fixture supplies all globals; it never imports a runner.
Linux proc-anchor/flags0 pidfd contracts still require corroboration.
"""


def screen():
    global churn
    require(host_policy_admitted and HOST_POLICY_ID == 'GUI6_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_V4',
            'prospective bound-terminal policy requires genuine fresh exact-instance intake')

    def terminal(fd, diagnostic, label):
        diagnostic['pidfd_observations'][label] = None
        poller = select.poll()
        poller.register(fd, select.POLLIN)
        events = poller.poll(0)
        diagnostic['pidfd_observations'][label] = [mask for _, mask in events]
        require(not events or (len(events) == 1 and events[0][0] == fd and events[0][1] != 0
                and not events[0][1] & ~(select.POLLIN | select.POLLHUP)), 'uncertain pidfd readiness')
        return bool(events)

    def error_record(error):
        return {'type': type(error).__name__, 'errno': getattr(error, 'errno', None),
                'message': str(error)[:256]}

    def retain(key, diagnostic):
        if len(encoded(diagnostic)) > 4096:
            receipt.setdefault('host_screen_first_failure', {'pid': diagnostic['pid'],
                'stage': diagnostic['stage'], 'diagnostic_overflow': True})
            raise RuntimeError('host-screen diagnostic bound')
        receipt.setdefault(key, decode(encoded(diagnostic)))

    class MissingTask(Exception):
        def __init__(self, original):
            self.original = original

    def task_text(anchor, leaf, cap, diagnostic):
        require(leaf in ('comm', 'stat'), 'fixed proc task leaf required')
        leaf_fd = None
        try:
            leaf_fd = os.open(leaf, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK | os.O_CLOEXEC,
                              dir_fd=anchor)
            data = bytearray()
            while True:
                require(time.monotonic() < deadline, 'proc task read deadline')
                part = os.read(leaf_fd, cap + 1 - len(data))
                if not part:
                    return bytes(data).decode('utf-8')
                data.extend(part)
                require(len(data) <= cap, 'proc task text bound')
        except FileNotFoundError as error:  # ENOENT only; ESRCH is not elected here.
            raise MissingTask(error) from error
        finally:
            if leaf_fd is not None:
                try:
                    os.close(leaf_fd)
                except OSError as error:
                    diagnostic['leaf_close_error'] = error_record(error)
                    # A close failure must never masquerade as missing-task departure.
                    raise RuntimeError('proc task leaf close failure') from error

    def task_birth(anchor, pid, diagnostic):
        raw = task_text(anchor, 'stat', 4096, diagnostic)
        require(raw.startswith(str(pid) + ' (') and raw.rfind(')') >= len(str(pid)) + 2,
                'malformed anchored proc stat identity')
        fields = raw[raw.rfind(')') + 2:].split()
        require(len(fields) >= 20, 'short anchored proc stat')
        return {'pid': pid, 'ppid': int(fields[1]), 'start': int(fields[19])}

    def task_namespace(anchor, namespace):
        require(namespace in ('pid', 'mnt'), 'fixed proc namespace required')
        try:
            return os.readlink('ns/' + namespace, dir_fd=anchor)
        except FileNotFoundError as error:  # No broader OSError/dead-process fallback.
            raise MissingTask(error) from error

    def parent_policy(owner, original_birth):
        require(owned_domain is None or all(owned_domain[k] != parent_ns[k] for k in ('pid', 'mnt')),
                'admitted owned namespace pair not distinct from original parent')
        require(owner is None or (owner['fd'] is not None
                and original_birth['pid'] != owner['p'].pid
                and original_birth['ppid'] != owner['p'].pid
                and (not owner['isolated'] or owned_domain is not None)),
                'direct audit child/descendant or unbound isolated audit domain')

    def owned_policy(owner, diagnostic):
        require(owner is not None and owner['p'].returncode is None and owner['fd'] is not None
                and diagnostic['expected_namespaces'] is not None,
                'host buildlike conflict: ' + str(diagnostic['pid']) + ':' + diagnostic['comm'])

    def record_terminal(owner, diagnostic, kind):
        # Caller has just observed understood terminal readiness of the bound
        # original flags0 pidfd. No candidate signal or numeric-PID fallback.
        require(diagnostic['proc_anchor_bound'], 'terminal candidate without original proc binding')
        if diagnostic['owned_proof']:
            require(not terminal(owner['fd'], diagnostic, 'owner-at-' + kind),
                    'original namespace owner ended before departure exemption')
            suffix = 'enoent' if kind == 'enoent' else 'postread'
            retain('host_screen_first_owned_terminal_' + suffix, diagnostic)
            key = 'host_screen_owned_terminal_' + suffix + '_count'
        else:
            diagnostic['classification'] = 'NONLIVE_UNKNOWN'
            diagnostic['qualification'] = 'Original process terminal only; not owned, idle, workload/descendant settlement or cleanup authority.'
            retain('host_screen_first_nonlive_unknown', diagnostic)
            key = 'host_screen_nonlive_unknown_count'
        receipt[key] = receipt.get(key, 0) + 1

    deadline = min(time.monotonic() + 5, END)
    with os.scandir('/proc') as entries:
        for index, entry in enumerate(entries):
            require(index < 8192 and time.monotonic() < deadline, 'host-screen entry/time bound')
            name = entry.name
            if not name.isdecimal():
                continue
            owner, fd, anchor = child, None, None
            diagnostic = {'pid': int(name), 'owner_pid': None if owner is None else owner['p'].pid,
                'comm': None, 'stage': 'pidfd-open', 'owned_proof': False, 'proc_anchor_bound': False,
                'expected_namespaces': None if owned_domain is None else dict(owned_domain),
                'pidfd_observations': {}}
            try:
                try:
                    try:
                        fd = os.pidfd_open(int(name), 0)  # flags0: process/thread-group, not PIDFD_THREAD.
                    except ProcessLookupError:
                        churn += 1  # Pre-positive and unclassified; NOT benign/idle proof.
                        continue
                    diagnostic['stage'] = 'proc-anchor-open'
                    try:
                        anchor = os.open('/proc/' + name, os.O_RDONLY | os.O_DIRECTORY
                                         | os.O_NOFOLLOW | os.O_CLOEXEC)
                    except FileNotFoundError:
                        require(terminal(fd, diagnostic, 'prepositive-anchor-enoent'),
                                'live/unreadable potential host workload')
                        churn += 1
                        continue
                    diagnostic['stage'] = 'proc-anchor-bind'
                    if terminal(fd, diagnostic, 'candidate-binding'):
                        # The numeric name might already be another task: read no
                        # positive comm from it and make no terminal-UNKNOWN claim.
                        churn += 1
                        continue
                    diagnostic['proc_anchor_bound'] = True
                    diagnostic['stage'] = 'comm-initial'
                    try:
                        comm = task_text(anchor, 'comm', 256, diagnostic).strip()
                    except MissingTask:
                        require(terminal(fd, diagnostic, 'prepositive-enoent'),
                                'live/unreadable potential host workload')
                        churn += 1
                        continue
                    diagnostic['comm'] = comm
                    if not BUILDLIKE.fullmatch(comm):
                        continue

                    try:
                        diagnostic['stage'] = 'proof-birth'
                        row = task_birth(anchor, int(name), diagnostic)
                        diagnostic['identity'] = row
                        original_birth = dict(row)
                        row['namespaces'] = {}
                        possible = [('parent', {k: parent_ns[k] for k in ('pid', 'mnt')})]
                        if diagnostic['expected_namespaces'] is not None:
                            possible.append(('owned', diagnostic['expected_namespaces']))
                        for namespace in ('pid', 'mnt'):
                            diagnostic['stage'] = 'proof-namespace:' + namespace
                            observed = task_namespace(anchor, namespace)
                            row['namespaces'][namespace] = observed
                            possible = [(kind, pair) for kind, pair in possible if pair[namespace] == observed]
                            require(possible, 'positive namespace prefix conflicts with admitted domains')
                            # Branch restrictions are checked as soon as the
                            # observed prefix identifies that branch, not after
                            # another read which could fail and hide a mismatch.
                            kinds = {kind for kind, _ in possible}
                            if kinds == {'parent'}:
                                parent_policy(owner, original_birth)
                            elif kinds == {'owned'}:
                                owned_policy(owner, diagnostic)
                        diagnostic['stage'] = 'proof-birth-reread'
                        diagnostic['observed_birth'] = task_birth(anchor, int(name), diagnostic)
                        require(diagnostic['observed_birth'] == original_birth, 'unstable process identity')
                        diagnostic['stage'] = 'proof-candidate-pidfd'
                        if terminal(fd, diagnostic, 'candidate-proof'):
                            record_terminal(owner, diagnostic, 'postread')
                            continue
                        diagnostic['stage'] = 'proof-owned-domain'
                        if row['namespaces'] == {k: parent_ns[k] for k in ('pid', 'mnt')}:
                            diagnostic['stage'] = 'proof-original-parent-policy'
                            parent_policy(owner, original_birth)
                            diagnostic['classification'] = 'UNKNOWN_ORIGINAL_PARENT_PID_MNT'
                        else:
                            owned_policy(owner, diagnostic)
                            require(diagnostic['expected_namespaces'] == row['namespaces'],
                                    'host buildlike conflict: ' + name + ':' + comm)
                            diagnostic['stage'] = 'proof-owner-pidfd'
                            require(not terminal(owner['fd'], diagnostic, 'owner-proof'),
                                    'original namespace owner terminal')
                            diagnostic['owned_proof'] = True

                        diagnostic['stage'] = 'reread-comm'
                        diagnostic['observed_comm'] = task_text(anchor, 'comm', 256, diagnostic).strip()
                        require(diagnostic['observed_comm'] == comm, 'positive comm changed')
                        diagnostic['stage'] = 'reread-birth-first'
                        diagnostic['observed_birth'] = task_birth(anchor, int(name), diagnostic)
                        require(diagnostic['observed_birth'] == original_birth, 'positive birth changed')
                        diagnostic['observed_namespaces'] = {}
                        for namespace in ('pid', 'mnt'):
                            diagnostic['stage'] = 'reread-namespace:' + namespace
                            observed = task_namespace(anchor, namespace)
                            diagnostic['observed_namespaces'][namespace] = observed
                            require(observed == row['namespaces'][namespace], 'positive namespace changed')
                        diagnostic['stage'] = 'reread-birth-last'
                        diagnostic['observed_birth'] = task_birth(anchor, int(name), diagnostic)
                        require(diagnostic['observed_birth'] == original_birth, 'positive birth changed')
                    except MissingTask as error:
                        diagnostic['read_enoent'] = error_record(error.original)
                        if diagnostic['owned_proof']:
                            diagnostic['reread_enoent'] = diagnostic['read_enoent']
                        require(terminal(fd, diagnostic, 'candidate-at-enoent'),
                                'positive ENOENT without original terminal pidfd')
                        record_terminal(owner, diagnostic, 'enoent')
                        continue
                    diagnostic['stage'] = 'reread-candidate-pidfd'
                    if terminal(fd, diagnostic, 'candidate-final'):
                        record_terminal(owner, diagnostic, 'postread')
                        continue
                    if diagnostic['owned_proof']:
                        diagnostic['stage'] = 'reread-owner-pidfd'
                        require(not terminal(owner['fd'], diagnostic, 'owner-final'),
                                'original namespace owner terminal')
                    else:
                        diagnostic['stage'] = 'unknown-original-parent-observed'
                        retain('host_screen_first_unknown_parent_candidate', diagnostic)
                        receipt['host_screen_unknown_parent_observations'] = receipt.get(
                            'host_screen_unknown_parent_observations', 0) + 1
                except Exception as error:
                    diagnostic['error'] = error_record(error)
                    raise
                finally:
                    close_failure = None
                    for descriptor, key in ((anchor, 'proc_close_error'), (fd, 'close_error')):
                        if descriptor is not None:
                            try:
                                os.close(descriptor)  # One attempt each, even if the other close failed.
                            except OSError as error:
                                diagnostic[key] = error_record(error)
                                close_failure = error
                    if close_failure is not None:
                        raise RuntimeError('candidate descriptor close failure') from close_failure
            except Exception as error:
                retain('host_screen_first_failure', diagnostic)
                raise RuntimeError('host-screen candidate uncertainty: ' + name + ':' + diagnostic['stage']) from error
    require(time.monotonic() < deadline, 'host-screen final time bound')
