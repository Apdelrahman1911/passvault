#!/usr/bin/python3
"""One root-admitted capture of three fixed local XSD members as inert data.

NOT execution admission. Root must independently freeze/review this file first.
No arguments, network, Java, SDK commands, subprocesses, XML parsing or imports
of captured data. A consumed output directory is never reused or removed here.
"""

import datetime
import hashlib
import io
import json
import os
from pathlib import Path
import resource
import signal
import stat
import sys
import time
import zipfile


BASE = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/android32')
SELF = BASE / 'CAPTURE_FIXED_XSDS.py'
OUTPUT_NAME = 'schema-capture-01'
MIB = 1024 ** 2
GIB = 1024 ** 3
MAX_CONTAINER_BYTES = 2 * MIB
MAX_MEMBER_BYTES = 32 * 1024
MAX_JSON_BYTES = 64 * 1024
MAX_ARCHIVE_ENTRIES = 10000
MAX_CAPTURE_BYTES = 256 * 1024
DIR_FLAGS = os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | os.O_CLOEXEC
READ_FLAGS = os.O_RDONLY | os.O_NOFOLLOW | os.O_CLOEXEC
INPUTS = (
    {
        'path': '/opt/android-sdk/cmdline-tools/23.0/lib/sdklib/tools.sdklib.jar',
        'bytes': 1909475,
        'sha256': 'd0bbe0e03155a7bdd9bac03567b6b26797f81a778f55d5eadc3e71e7a0a27434',
        'members': (
            {
                'name': 'xsd/sdk-sys-img-01.xsd',
                'output': 'sdk-sys-img-01.xsd',
                'bytes': 4481,
                'sha256': '2eb33b82b4db5d24892e8b7c9ebb2eace2d309f6fc64c57103a034b7e2a9eaf5',
            },
        ),
    },
    {
        'path': '/opt/android-sdk/cmdline-tools/23.0/lib/repository/tools.repository.jar',
        'bytes': 272256,
        'sha256': '9fd419f3307be633aaf7b02b52832521c1300d4bf9058cee83bb11139990a7f9',
        'members': (
            {
                'name': 'xsd/repo-common-01.xsd',
                'output': 'repo-common-01.xsd',
                'bytes': 15469,
                'sha256': '91a12ddb132fbf2a56fef48030c0bd21a6032eff1e095d92d605ac8baadb151e',
            },
            {
                'name': 'xsd/repo-common-02.xsd',
                'output': 'repo-common-02.xsd',
                'bytes': 15510,
                'sha256': '5f8b8c8b8bc3d965c4334c20d72cb83ff475012703bd922e6a3387d35efc36e3',
            },
        ),
    },
)
OUTPUT_FILES = frozenset((
    'INTENT.json', 'RESULT.json',
    'sdk-sys-img-01.xsd', 'repo-common-01.xsd', 'repo-common-02.xsd',
))
OWNED_FDS = {}
CLOSE_EVENTS = []
FIRST_SIGNAL = None


class Refused(Exception):
    pass


def require(condition, message):
    if not condition:
        raise Refused(message)


def utc():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


def checkpoint():
    require(FIRST_SIGNAL is None, 'capture cancellation requested by signal ' + str(FIRST_SIGNAL))


def open_owned(name, flags, label, mode=0o777, dir_fd=None):
    # The installed signal handler records only: it cannot throw between open
    # and registration. An ordinary registration failure still attempts close.
    descriptor = os.open(name, flags, mode, dir_fd=dir_fd)
    try:
        require(descriptor not in OWNED_FDS, 'descriptor registration collision')
        OWNED_FDS[descriptor] = label
    except BaseException as registration_error:
        try:
            os.close(descriptor)
        except BaseException as close_error:
            CLOSE_EVENTS.append({
                'descriptor': descriptor, 'label': label, 'status': 'CLOSE_ERROR_NO_RETRY',
                'phase': 'registration-failure', 'type': type(close_error).__name__,
                'message': str(close_error)[:1000],
            })
            raise registration_error from close_error
        CLOSE_EVENTS.append({
            'descriptor': descriptor, 'label': label, 'status': 'CLOSED',
            'phase': 'registration-failure',
        })
        raise
    return descriptor


def close_owned(descriptor):
    # Remove before the one close attempt. An uncertain close is never retried
    # against a possibly reused integer; final process exit needs reconciliation.
    label = OWNED_FDS[descriptor]
    event = {'descriptor': descriptor, 'label': label, 'status': 'CLOSE_ATTEMPT_UNSETTLED'}
    CLOSE_EVENTS.append(event)
    OWNED_FDS.pop(descriptor)
    try:
        os.close(descriptor)
        event['status'] = 'CLOSED'
        return True
    except BaseException as error:
        event.update({
            'status': 'CLOSE_ERROR_NO_RETRY', 'type': type(error).__name__,
            'message': str(error)[:1000],
        })
        return False


def close_all(result, preserve=()):
    okay = True
    for descriptor in tuple(OWNED_FDS):
        if descriptor in preserve:
            continue
        try:
            closed = close_owned(descriptor)
        except BaseException as error:
            # Do not let one close/accounting exception bypass other originals.
            closed = False
            result['errors'].append({
                'phase': 'close-accounting', 'type': type(error).__name__,
                'message': str(error)[:1000],
            })
        okay = closed and okay
    return okay


def pin(info):
    return {
        'device': info.st_dev, 'inode': info.st_ino, 'uid': info.st_uid,
        'mode': info.st_mode, 'links': info.st_nlink, 'bytes': info.st_size,
        'mtime_ns': info.st_mtime_ns, 'ctime_ns': info.st_ctime_ns,
    }


def open_directory(path):
    """No-follow traversal for fixed absolute paths; no caller-provided paths."""
    require(path.is_absolute() and '..' not in path.parts, 'nonabsolute or parent traversal')
    descriptor = open_owned('/', DIR_FLAGS, 'directory /')
    for part in path.parts[1:]:
        child = open_owned(part, DIR_FLAGS, 'directory component ' + part, dir_fd=descriptor)
        require(close_owned(descriptor), 'directory transition close failed; child remains registered')
        descriptor = child
    return descriptor


def read_regular(parent_fd, name, expected_bytes, expected_sha=None):
    require('/' not in name and name not in ('.', '..'), 'invalid fixed file name')
    checkpoint()
    descriptor = open_owned(name, READ_FLAGS, 'input ' + name, dir_fd=parent_fd)
    try:
        checkpoint()
        before = os.fstat(descriptor)
        require(stat.S_ISREG(before.st_mode), 'input is not a regular file')
        require(before.st_size == expected_bytes, 'input byte count changed')
        pieces = []
        remaining = expected_bytes + 1
        while remaining:
            checkpoint()
            piece = os.read(descriptor, min(65536, remaining))
            if not piece:
                break
            pieces.append(piece)
            remaining -= len(piece)
        payload = b''.join(pieces)
        after = os.fstat(descriptor)
        linked = os.stat(name, dir_fd=parent_fd, follow_symlinks=False)
        require(pin(before) == pin(after) == pin(linked), 'input identity changed while read')
        require(len(payload) == expected_bytes, 'input overrun or short read')
        digest = hashlib.sha256(payload).hexdigest()
        if expected_sha is not None:
            require(digest == expected_sha, 'input SHA-256 mismatch')
        return payload, {'pin': pin(after), 'bytes': len(payload), 'sha256': digest}
    finally:
        require(close_owned(descriptor), 'input close failed')


def resources(parent_fd):
    disk = os.fstatvfs(parent_fd)
    proc_fd = mem_fd = None
    try:
        proc_fd = open_directory(Path('/proc'))
        mem_fd = open_owned('meminfo', READ_FLAGS, 'resource /proc/meminfo', dir_fd=proc_fd)
        data = os.read(mem_fd, 65537)
    finally:
        closed = True
        for descriptor in (mem_fd, proc_fd):
            if descriptor is not None:
                closed = close_owned(descriptor) and closed
        require(closed, 'resource descriptor close failed')
    require(len(data) <= 65536, 'meminfo size cap')
    fields = {}
    for line in data.decode('ascii').splitlines():
        key, _, value = line.partition(':')
        if key in ('MemTotal', 'MemAvailable'):
            number, unit = value.split()
            require(unit == 'kB', 'unexpected meminfo unit')
            fields[key] = int(number) * 1024
    require(fields.get('MemTotal', 0) > 0 and 'MemAvailable' in fields, 'memory data missing')
    return {
        'utc': utc(), 'free_bytes': disk.f_bavail * disk.f_frsize,
        'memory_total_bytes': fields['MemTotal'],
        'memory_available_bytes': fields['MemAvailable'],
    }


def running_floors(sample):
    require(sample['free_bytes'] >= 8 * GIB, 'disk below inherited 8GiB running floor')
    require(sample['memory_available_bytes'] * 5 >= sample['memory_total_bytes'], 'RAM below 20% floor')


def write_new(output_fd, name, payload, total, failure_receipt=False):
    require(name in OUTPUT_FILES, 'output not allowlisted')
    require(not failure_receipt or name == 'RESULT.json', 'only final receipt may ignore cancellation')
    require(len(payload) <= MAX_JSON_BYTES, 'single output cap')
    require(total + len(payload) <= MAX_CAPTURE_BYTES, 'capture total cap')
    if not failure_receipt:
        checkpoint()
    flags = os.O_RDWR | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW | os.O_CLOEXEC
    descriptor = open_owned(name, flags, 'output ' + name, 0o600, dir_fd=output_fd)
    try:
        remaining = memoryview(payload)
        while remaining:
            if not failure_receipt:
                checkpoint()
            count = os.write(descriptor, remaining)
            require(count > 0, 'output write made no progress')
            remaining = remaining[count:]
        os.fsync(descriptor)
        os.lseek(descriptor, 0, os.SEEK_SET)
        actual = bytearray()
        while len(actual) <= len(payload):
            if not failure_receipt:
                checkpoint()
            piece = os.read(descriptor, min(65536, len(payload) + 1 - len(actual)))
            if not piece:
                break
            actual.extend(piece)
        require(bytes(actual) == payload, 'output read-back mismatch')
        info = os.fstat(descriptor)
        linked = os.stat(name, dir_fd=output_fd, follow_symlinks=False)
        require(stat.S_ISREG(info.st_mode) and info.st_nlink == 1, 'output type/link change')
        require(pin(info) == pin(linked), 'output path identity changed')
        return total + len(payload)
    finally:
        require(close_owned(descriptor), 'output close failed')


def json_bytes(value):
    result = (json.dumps(value, indent=2, sort_keys=True) + '\n').encode('utf-8')
    require(len(result) <= MAX_JSON_BYTES, 'JSON cap')
    return result


def interrupted(number, _frame):
    # Never raise asynchronously across acquisition/registration/closeout.
    # Blocking I/O may continue until the root-owned outer timeout kills it.
    global FIRST_SIGNAL
    if FIRST_SIGNAL is None:
        FIRST_SIGNAL = number


def fail(result, phase, error):
    result['status'] = 'FAIL_CAPTURE_NO_RETRY'
    result['errors'].append({
        'phase': phase, 'type': type(error).__name__, 'message': str(error)[:1000],
    })


def record_cancellation(result):
    if FIRST_SIGNAL is not None:
        result['status'] = 'FAIL_CAPTURE_NO_RETRY'
        result['interruption_signal'] = FIRST_SIGNAL


def main():
    require(sys.platform == 'linux', 'Linux only')
    require(len(sys.argv) == 1 and Path(__file__).absolute() == SELF, 'fixed script/no arguments only')
    require(sys.flags.isolated and sys.dont_write_bytecode, 'run with python3 -I -B')
    os.umask(0o077)
    resource.setrlimit(resource.RLIMIT_AS, (128 * MIB, 128 * MIB))
    resource.setrlimit(resource.RLIMIT_CPU, (5, 6))
    resource.setrlimit(resource.RLIMIT_FSIZE, (MAX_CAPTURE_BYTES, MAX_CAPTURE_BYTES))
    resource.setrlimit(resource.RLIMIT_NOFILE, (64, 64))
    for number in (signal.SIGINT, signal.SIGTERM, signal.SIGHUP, signal.SIGALRM, signal.SIGXCPU):
        signal.signal(number, interrupted)
    signal.setitimer(signal.ITIMER_REAL, 15)
    started = time.monotonic()
    parent_fd = output_fd = None
    total = 0
    result = {
        'format': 'passvault-fixed-xsd-capture-v2', 'finding': 'PVA-001',
        'status': 'INCOMPLETE', 'started_utc': utc(),
        'inputs': [], 'outputs': [], 'errors': [],
        'scope': 'Three fixed XSDs as bytes; no XML parse, schema-chain verdict or target admission.',
        'application_test_executions': 0,
        'network_requests': 0, 'subprocesses_started': 0, 'temporary_files_created': 0,
    }
    try:
        checkpoint()
        parent_fd = open_directory(BASE)
        before = resources(parent_fd)
        result['resources_before'] = before
        require(before['free_bytes'] >= 12 * GIB, 'disk below inherited 12GiB launch floor')
        require(before['memory_available_bytes'] * 4 >= before['memory_total_bytes'], 'RAM below 25% floor')
        info = os.stat(SELF.name, dir_fd=parent_fd, follow_symlinks=False)
        require(stat.S_ISREG(info.st_mode) and info.st_size <= MAX_JSON_BYTES, 'reader source size/type')
        _, reader_record = read_regular(parent_fd, SELF.name, info.st_size)
        result['reader'] = {'path': str(SELF), **reader_record}
        checkpoint()
        os.mkdir(OUTPUT_NAME, mode=0o700, dir_fd=parent_fd)
        output_fd = open_owned(OUTPUT_NAME, DIR_FLAGS, 'capture output directory', dir_fd=parent_fd)
        result['output_directory_pin'] = pin(os.fstat(output_fd))
        total = write_new(output_fd, 'INTENT.json', json_bytes({
            'status': 'INTENT_ONLY_NOT_COMPLETION', 'started_utc': result['started_utc'],
            'reader': result['reader'], 'fixed_inputs': INPUTS,
            'expected_outputs': sorted(OUTPUT_FILES),
            'resources_before': before,
            'limits': {
                'address_space_bytes': 128 * MIB, 'cpu_soft_seconds': 5, 'cpu_hard_seconds': 6,
                'wall_cancellation_request_seconds': 15, 'per_container_bytes': MAX_CONTAINER_BYTES,
                'archive_entries': MAX_ARCHIVE_ENTRIES, 'per_member_bytes': MAX_MEMBER_BYTES,
                'total_output_bytes': MAX_CAPTURE_BYTES,
            },
        }), total)
        payloads = []
        for source in INPUTS:
            checkpoint()
            require(source['bytes'] <= MAX_CONTAINER_BYTES, 'fixed container cap')
            source_path = Path(source['path'])
            source_parent = open_directory(source_path.parent)
            try:
                container, descriptor = read_regular(
                    source_parent, source_path.name, source['bytes'], source['sha256'],
                )
            finally:
                require(close_owned(source_parent), 'source parent close failed')
            result['inputs'].append({'path': source['path'], **descriptor})
            # Hash-verified immutable in-memory bytes, not a reopened mutable JAR.
            with zipfile.ZipFile(io.BytesIO(container)) as archive:
                entries = archive.infolist()
                require(len(entries) <= MAX_ARCHIVE_ENTRIES, 'archive entry cap')
                for member in source['members']:
                    checkpoint()
                    matches = [entry for entry in entries if entry.filename == member['name']]
                    require(len(matches) == 1, 'missing or duplicate fixed member')
                    entry = matches[0]
                    require(not entry.is_dir() and not entry.flag_bits & 1, 'directory/encrypted member')
                    require(stat.S_IFMT(entry.external_attr >> 16) in (0, stat.S_IFREG), 'special member')
                    require(entry.compress_type in (zipfile.ZIP_STORED, zipfile.ZIP_DEFLATED), 'compression type')
                    require(entry.file_size == member['bytes'] <= MAX_MEMBER_BYTES, 'member byte cap/identity')
                    require(entry.compress_size <= MAX_MEMBER_BYTES, 'compressed member cap')
                    with archive.open(entry, 'r') as handle:
                        data = handle.read(member['bytes'] + 1)
                    require(len(data) == member['bytes'], 'member short read/overrun')
                    require(hashlib.sha256(data).hexdigest() == member['sha256'], 'member SHA-256 mismatch')
                    payloads.append((member, data))
        require(len(payloads) == 3, 'fixed three-member inventory')
        checkpoint()
        result['resources_before_xsd_outputs'] = resources(parent_fd)
        running_floors(result['resources_before_xsd_outputs'])
        # Validate all three before creating any captured XSD output.
        for member, data in payloads:
            total = write_new(output_fd, member['output'], data, total)
            result['outputs'].append({
                'name': member['output'], 'member': member['name'],
                'bytes': len(data), 'sha256': hashlib.sha256(data).hexdigest(),
                'physical_LF': data.count(b'\n'), 'read_back_verified': True,
            })
        checkpoint()
        result['status'] = 'CAPTURED_THREE_FIXED_XSDS_NO_SEMANTIC_VERDICT'
    except BaseException as error:
        fail(result, 'capture', error)
    finally:
        # Cancellation cannot bypass closeout or its attempted failure receipt.
        record_cancellation(result)
        try:
            if parent_fd is not None:
                result['resources_after'] = resources(parent_fd)
                running_floors(result['resources_after'])
        except BaseException as error:
            fail(result, 'final-resources', error)
        if not close_all(result, preserve=(output_fd, parent_fd)):
            fail(result, 'early-closeout', Refused('one or more original descriptor closes failed'))
        record_cancellation(result)
        result['receipt_utc'] = utc()
        result['elapsed_seconds_at_receipt'] = round(time.monotonic() - started, 6)
        result['cleanup'] = {
            'close_events_before_receipt': list(CLOSE_EVENTS),
            'registered_before_receipt': dict(OWNED_FDS),
            'final_closeout': 'PENDING: external stdout, exit and final close events are mandatory.',
            'retention': 'Keep compact permanent evidence, including partial failure; no temp/cache/SDK writes.',
        }
        try:
            if output_fd is not None:
                # Final receipt is exclusively created, never a replacement of an old result.
                total = write_new(output_fd, 'RESULT.json', json_bytes(result), total, failure_receipt=True)
                os.fsync(output_fd)
                os.fsync(parent_fd)
        except BaseException as error:
            fail(result, 'final-receipt', error)
        finally:
            if not close_all(result):
                fail(result, 'final-closeout', Refused('one or more original descriptor closes failed'))
            if OWNED_FDS or any(event['status'] != 'CLOSED' for event in CLOSE_EVENTS):
                fail(result, 'closeout-ledger', Refused('explicit closeout incomplete or uncertain; no retry'))
            record_cancellation(result)
            signal.setitimer(signal.ITIMER_REAL, 0)
    print(json.dumps({
        'status': result['status'], 'output_directory': str(BASE / OUTPUT_NAME),
        'captured_members': len(result['outputs']),
        'acknowledged_payload_bytes_not_partial_file_total': total,
        'finished_utc': utc(), 'elapsed_seconds': round(time.monotonic() - started, 6),
        'interruption_signal': FIRST_SIGNAL, 'errors': result['errors'],
        'close_events': CLOSE_EVENTS, 'remaining_registered_descriptors': OWNED_FDS,
    }, sort_keys=True))
    return 0 if result['status'] == 'CAPTURED_THREE_FIXED_XSDS_NO_SEMANTIC_VERDICT' and FIRST_SIGNAL is None else 1


if __name__ == '__main__':
    raise SystemExit(main())
