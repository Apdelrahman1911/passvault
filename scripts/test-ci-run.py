#!/usr/bin/env python3
"""Synthetic process/output safety tests; no application, Gradle or real user data."""
import importlib.util
import os
from pathlib import Path
import subprocess
import shutil
import sys
import tempfile
import time
import unittest
from unittest.mock import Mock, patch

sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location("ci_run", Path(__file__).with_name("ci-run.py"))
runner = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runner)


class ResourceGuardTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="passvault-ci-guard-test-")).resolve()
        self.scopes = []

    def tearDown(self):
        for scope in self.scopes:
            try:
                if not scope.empty():
                    scope.terminate()
                deadline = time.monotonic() + 5
                while not scope.empty() and time.monotonic() < deadline:
                    time.sleep(0.1)
                self.assertTrue(scope.empty(), "Synthetic child not settled; do not remove fixture")
            finally:
                scope.close()
        shutil.rmtree(self.root)

    def launch(self, text):
        script = self.root / "batch.sh"
        script.write_text(text)
        scope = runner.ProcessScope()
        self.scopes.append(scope)
        scope.launch(script, os.environ.copy())
        return scope

    def settled(self, scope):
        deadline = time.monotonic() + 5
        while not scope.empty() and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(scope.empty())

    def test_success_and_failure_status_preserved(self):
        for code in (0, 7):
            scope = self.launch(f"exit {code}\n")
            self.assertEqual(scope.process.wait(timeout=10), code)
            self.settled(scope)

    def test_windows_requires_explicit_shell_without_path_fallback(self):
        with patch.object(runner.sys, "platform", "win32"), \
                patch.object(runner.shutil, "which", side_effect=AssertionError("PATH lookup forbidden")):
            with self.assertRaisesRegex(RuntimeError, "explicit Git Bash"):
                runner.bash_executable({})
            # A spaced native path is passed through as one argv element.
            shell = self.root / "Git Bash" / "bash.exe"
            shell.parent.mkdir()
            shell.touch()
            self.assertEqual(runner.bash_executable({"PASSVAULT_CI_BASH": str(shell)}), str(shell))

    def test_invalid_configured_shell_is_not_replaced_from_path(self):
        for path in ("bash", str(self.root / "missing.exe"), str(self.root)):
            with self.assertRaisesRegex(RuntimeError, "invalid explicit Bash"):
                runner.bash_executable({"PASSVAULT_CI_BASH": path})

    def test_owned_descendant_settles_before_cleanup(self):
        scope = self.launch('sleep 1 &\nwait\n')
        self.assertFalse(scope.empty())
        self.assertEqual(scope.process.wait(timeout=10), 0)
        self.settled(scope)

    def test_termination_is_bounded_to_owned_scope(self):
        scope = self.launch('exec sleep 60\n')
        time.sleep(0.2)
        scope.terminate()
        self.settled(scope)

    @unittest.skipIf(os.name == "nt", "POSIX process-group probe")
    def test_permission_denied_probe_never_proves_settlement(self):
        scope = runner.ProcessScope()
        scope.process = Mock(pid=12345)
        with patch.object(runner.os, "killpg", side_effect=PermissionError):
            self.assertFalse(scope.empty())
        scope.process.poll.assert_called_once_with()

    @unittest.skipIf(os.name == "nt", "POSIX process-group probe")
    def test_termination_waits_through_permission_denied_probe(self):
        scope = runner.ProcessScope()
        scope.process = Mock(pid=12345)
        # TERM succeeds; an uncertain probe must not abort bounded settlement.
        with patch.object(runner.os, "killpg", side_effect=[
            None, PermissionError(), ProcessLookupError(), ProcessLookupError(),
        ]) as killpg, patch.object(runner.time, "sleep") as sleep:
            scope.terminate()
        self.assertEqual(killpg.call_args_list[0].args, (12345, runner.signal.SIGTERM))
        self.assertTrue(all(call.args == (12345, 0) for call in killpg.call_args_list[1:]))
        self.assertEqual(killpg.call_count, 4)
        sleep.assert_called_once_with(0.2)

    def test_completed_windows_scope_cleanup_is_owned_and_fail_closed(self):
        remaining = Mock(job=1)
        remaining.empty.return_value = False
        remaining.process.poll.return_value = 0
        empty = Mock(job=2)
        empty.empty.return_value = True
        posix = Mock(job=None)
        result, attempted = {}, set()
        runner.terminate_completed_windows_scopes([remaining, empty, posix], result, attempted)
        remaining.terminate.assert_called_once_with()
        empty.terminate.assert_not_called()
        posix.terminate.assert_not_called()
        self.assertEqual(attempted, {remaining})
        self.assertEqual(result, {"windows_job_termination_requested": [0]})
        # An interrupted batch may already have dispatched termination once.
        runner.terminate_completed_windows_scopes([remaining], result, attempted)
        remaining.terminate.assert_called_once_with()

        active = Mock(job=3)
        active.empty.return_value = False
        active.process.poll.return_value = None
        with self.assertRaisesRegex(RuntimeError, "active Windows shell"):
            runner.terminate_completed_windows_scopes([active], {}, set())
        active.terminate.assert_not_called()

        failed = Mock(job=4)
        failed.empty.return_value = False
        failed.process.poll.return_value = 0
        failed.terminate.side_effect = OSError("termination failed")
        attempted = set()
        with self.assertRaisesRegex(OSError, "termination failed"):
            runner.terminate_completed_windows_scopes([failed], {}, attempted)
        self.assertEqual(attempted, {failed})  # Caller must not retry failed dispatch.

    def tracked(self, extra=()):
        return ("\0".join(["build.gradle.kts", "core/data/build.gradle.kts", "source.kt", *extra]) + "\0").encode()

    def test_tracked_output_and_symlink_roots_rejected(self):
        with patch.object(runner.subprocess, "check_output", return_value=self.tracked(["build/required.txt"])):
            with self.assertRaisesRegex(RuntimeError, "tracked source"):
                runner.generated_roots(self.root)
        if os.name != "nt":
            (self.root / "build").symlink_to(self.root.parent, target_is_directory=True)
            with patch.object(runner.subprocess, "check_output", return_value=self.tracked()):
                with self.assertRaisesRegex(RuntimeError, "Unsafe"):
                    runner.generated_roots(self.root)

    def test_only_allowed_build_roots_and_compact_reports(self):
        with patch.object(runner.subprocess, "check_output", return_value=self.tracked()):
            roots = runner.generated_roots(self.root)
        self.assertEqual(set(roots), {self.root / p for p in ("build", ".gradle", ".kotlin", "core/data/build")})
        report = self.root / "core/data/build/test-results/test/TEST-synthetic.xml"
        report.parent.mkdir(parents=True)
        report.write_text('<testsuite tests="1" failures="0"/>')
        (report.parent / "binary.bin").write_bytes(b"not an artifact")
        evidence = self.root / "evidence"
        runner.preserve_reports(roots, self.root, evidence)
        self.assertEqual((evidence / report.relative_to(self.root)).read_text(), report.read_text())
        self.assertFalse((evidence / report.relative_to(self.root)).with_name("binary.bin").exists())
        if os.name != "nt":
            (self.root / "build").mkdir()
            (self.root / "build/reports").symlink_to(report.parent, target_is_directory=True)
            with self.assertRaisesRegex(RuntimeError, "directory symlink"):
                runner.preserve_reports(roots, self.root, evidence)

    def test_non_hosted_execution_refused(self):
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "false"}):
            with self.assertRaisesRegex(RuntimeError, "Only fresh GitHub-hosted"):
                runner.main(self.root / "never-run.sh")


if __name__ == "__main__":
    unittest.main()
