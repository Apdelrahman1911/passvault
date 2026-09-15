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
from unittest.mock import patch

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
