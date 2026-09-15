#!/usr/bin/env python3
"""Synthetic filesystem contracts for release-only staging and ownership checks.

No Gradle, Store credentials, application launch or release execution.
"""
import importlib.util
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location("release_build", Path(__file__).with_name("release-build.py"))
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleaseStagingTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="passvault-release-staging-test-")).resolve()
        self.out = self.root / ".release-artifacts"
        self.out.mkdir()

    def tearDown(self):
        for batch in self.root.glob("actual-*/.release-evidence/batch-*"):
            receipt = batch / "cleanup.json"
            if not receipt.is_file() or json.loads(receipt.read_text()).get("cleanup") != "PASS":
                (self.root / "CLEANUP_HOLD").write_text("Synthetic adapter process settlement not established")
                self.fail(f"Retain failed synthetic adapter fixture: {self.root}")
        shutil.rmtree(self.root)

    def file(self, relative, data=b"synthetic-package"):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
        return path

    def android(self):
        return [self.file(p) for p in (
            "app-android/build/outputs/apk/release/app-android-release.apk",
            "app-android/build/outputs/bundle/release/app-android-release.aab",
            "app-android/build/outputs/mapping/release/mapping.txt",
        )]

    def test_android_selection_preserves_bytes_without_private_inputs(self):
        sources = self.android()
        private = self.file("app-android/build/private/signing-input", b"synthetic-not-a-secret")
        exports = release.stage_outputs(self.root, self.out, "android")
        self.assertEqual(set(exports), {"PASSVAULT_RELEASE_OUTPUT", "ANDROID_APK_PATH", "ANDROID_AAB_PATH", "ANDROID_MAPPING_PATH"})
        self.assertEqual({p.name for p in self.out.iterdir()}, {p.name for p in sources})
        self.assertTrue(all(Path(exports[k]).read_bytes() == b"synthetic-package" for k in exports if k != "PASSVAULT_RELEASE_OUTPUT"))
        self.assertTrue(all(not p.exists() for p in sources))
        self.assertEqual(private.read_bytes(), b"synthetic-not-a-secret")

    def test_missing_android_deliverable_does_not_partially_move_selection(self):
        sources = self.android()
        sources[-1].unlink()
        with self.assertRaisesRegex(RuntimeError, "missing or unsafe"):
            release.stage_outputs(self.root, self.out, "android")
        self.assertTrue(all(p.exists() for p in sources[:-1]))
        self.assertEqual(list(self.out.iterdir()), [])

    def test_occupied_output_is_never_overwritten(self):
        sources = self.android()
        occupied = self.out / sources[0].name
        occupied.write_bytes(b"previous-synthetic-candidate")
        with self.assertRaisesRegex(RuntimeError, "occupied"):
            release.stage_outputs(self.root, self.out, "android")
        self.assertEqual(occupied.read_bytes(), b"previous-synthetic-candidate")
        self.assertTrue(all(p.exists() for p in sources))

    @unittest.skipIf(os.name == "nt", "Symlink creation may require Windows privilege")
    def test_symlinked_artifact_is_rejected_without_touching_target(self):
        sources = self.android()
        target = self.file("outside-candidate", b"synthetic-untouched")
        sources[0].unlink()
        sources[0].symlink_to(target)
        with self.assertRaisesRegex(RuntimeError, "missing or unsafe"):
            release.stage_outputs(self.root, self.out, "android")
        self.assertEqual(target.read_bytes(), b"synthetic-untouched")
        self.assertEqual(list(self.out.iterdir()), [])

    def test_desktop_selection_keeps_app_and_only_exact_package_formats(self):
        base = "app-desktop/build/compose/binaries/main-release/"
        app = self.file(base + "app/PassVault/bin/PassVault")
        self.file(base + "deb/PassVault.deb")
        self.file(base + "rpm/PassVault.rpm")
        excluded = self.file(base + "unneeded/large-output")
        with patch.object(release.sys, "platform", "linux"):
            release.stage_outputs(self.root, self.out, "desktop")
        self.assertEqual({p.name for p in self.out.iterdir()}, {"app", "deb", "rpm"})
        self.assertEqual((self.out / "app/PassVault/bin/PassVault").read_bytes(), b"synthetic-package")
        self.assertFalse(app.exists())
        self.assertTrue(excluded.exists())

    def test_ambiguous_desktop_packages_leave_all_sources_in_place(self):
        base = "app-desktop/build/compose/binaries/main-release/"
        app = self.file(base + "app/PassVault/bin/PassVault")
        self.file(base + "deb/one.deb")
        self.file(base + "deb/two.deb")
        self.file(base + "rpm/PassVault.rpm")
        with patch.object(release.sys, "platform", "linux"), self.assertRaisesRegex(RuntimeError, "exactly one"):
            release.stage_outputs(self.root, self.out, "desktop")
        self.assertTrue(app.exists())
        self.assertEqual(list(self.out.iterdir()), [])

    def test_ios_staging_preserves_archive_and_excludes_signing_inputs(self):
        runtime = self.root / "private-runtime"
        archive_file = self.file("private-runtime/PassVault.xcarchive/Products/Applications/PassVault.app/binary")
        self.file("private-runtime/export/PassVault.ipa")
        self.file("private-runtime/PassVault-LinkMap.txt")
        signing = self.file("private-runtime/synthetic-signing-input", b"synthetic-not-a-key")
        exports = release.stage_outputs(self.root, self.out, "ios", runtime)
        self.assertEqual(set(exports), {"PASSVAULT_RELEASE_OUTPUT", "IOS_IPA_PATH", "IOS_ARCHIVE_PATH", "IOS_LINK_MAP_PATH"})
        self.assertFalse(archive_file.exists())
        self.assertTrue((Path(exports["IOS_ARCHIVE_PATH"]) / "Products/Applications/PassVault.app/binary").is_file())
        self.assertEqual(signing.read_bytes(), b"synthetic-not-a-key")
        self.assertFalse((self.out / signing.name).exists())

    def test_actual_batch_stops_original_synthetic_wrapper_and_stages_after_settlement(self):
        # Two real process/filesystem executions. No Gradle or product process.
        for snapshot_failure in (False, True):
            with self.subTest(initial_snapshot_failure=snapshot_failure):
                repo = self.root / ("actual-" + str(snapshot_failure))
                repo.mkdir()
                temp = repo / "runner-temp"
                temp.mkdir()
                home = repo / "synthetic-home"
                home.mkdir()
                wrapper = repo / "gradlew"
                wrapper.write_text('#!/usr/bin/env bash\nset -eu\n[[ "$1" == --stop ]]\nprintf stopped > stop-marker\n')
                wrapper.chmod(0o700)
                (repo / "gradlew.bat").write_text('@echo off\r\nif not "%1"=="--stop" exit /b 9\r\necho stopped>stop-marker\r\n')
                script = repo / "synthetic-build.sh"
                script.write_text('set -eu\nmkdir -p "$GRADLE_USER_HOME/daemon" app-android/build/outputs/{apk/release,bundle/release,mapping/release}\nprintf fixture-apk > app-android/build/outputs/apk/release/app-android-release.apk\nprintf fixture-aab > app-android/build/outputs/bundle/release/app-android-release.aab\nprintf fixture-map > app-android/build/outputs/mapping/release/mapping.txt\n')
                github_env = repo / "github-env"
                github_env.touch()
                env = {"GITHUB_ACTIONS": "true", "RUNNER_ENVIRONMENT": "github-hosted", "GITHUB_WORKSPACE": str(repo),
                       "GITHUB_RUN_ID": "1", "GITHUB_RUN_ATTEMPT": "1", "GITHUB_JOB": "synthetic", "RUNNER_TEMP": str(temp),
                       "GITHUB_ENV": str(github_env), "HOME": str(home), "USERPROFILE": str(home)}
                real_check_output = subprocess.check_output
                def check_output(args, **kwargs):
                    if args == ["git", "rev-parse", "HEAD"]:
                        return "a" * 40 + "\n"
                    return real_check_output(args, **kwargs)
                snapshots = []
                def preserve(*args):
                    snapshots.append(True)
                    if snapshot_failure and len(snapshots) == 1:
                        raise RuntimeError("synthetic report-budget failure")
                cwd = Path.cwd()
                handlers = {sig: signal.getsignal(sig) for sig in (signal.SIGINT, signal.SIGTERM)}
                try:
                    os.chdir(repo)
                    with patch.dict(os.environ, env), patch.object(release.subprocess, "check_output", side_effect=check_output), \
                            patch.object(release.guard, "generated_roots", return_value=[repo / "app-android/build"]), \
                            patch.object(release.guard, "preserve_reports", side_effect=preserve):
                        self.assertEqual(release.main("android", script), 0)
                finally:
                    os.chdir(cwd)
                    for sig, handler in handlers.items():
                        signal.signal(sig, handler)
                self.assertEqual((repo / "stop-marker").read_text().strip(), "stopped")
                receipts = list((repo / ".release-evidence").glob("batch-*/cleanup.json"))
                self.assertEqual(len(receipts), 1)
                result = json.loads(receipts[0].read_text())
                self.assertEqual((result["cleanup"], result["wrapper_stop"]), ("PASS", 0))
                self.assertEqual("initial_report_snapshot_error" in result, snapshot_failure)
                self.assertEqual((repo / ".release-artifacts/app-android-release.aab").read_bytes(), b"fixture-aab")
                self.assertFalse((repo / "app-android/build").exists())
                self.assertEqual(list(temp.iterdir()), [])
                self.assertIn("ANDROID_AAB_PATH=", github_env.read_text())

    def test_dispose_refuses_missing_or_failed_producer_receipt(self):
        evidence = self.root / ".release-evidence"
        evidence.mkdir()
        batch = evidence / "batch-synthetic"
        batch.mkdir()
        owner = {"source": "a" * 40, "run": "1", "attempt": "1", "job": "synthetic"}
        (evidence / "owner.json").write_text(json.dumps(owner))
        candidate = self.out / "candidate.bin"
        candidate.write_bytes(b"synthetic-keep")
        env = {"GITHUB_ACTIONS": "true", "RUNNER_ENVIRONMENT": "github-hosted", "GITHUB_WORKSPACE": str(self.root),
               "GITHUB_RUN_ID": "1", "GITHUB_RUN_ATTEMPT": "1", "GITHUB_JOB": "synthetic"}
        with patch.dict(os.environ, env), patch.object(release.Path, "cwd", return_value=self.root), \
                patch.object(release.subprocess, "check_output", return_value=owner["source"] + "\n"):
            with self.assertRaisesRegex(RuntimeError, "Unsettled"):
                release.main("dispose")
            (batch / "cleanup.json").write_text(json.dumps({"cleanup": "HOLD"}))
            with self.assertRaisesRegex(RuntimeError, "HOLD"):
                release.main("dispose")
            self.assertEqual(candidate.read_bytes(), b"synthetic-keep")
            (batch / "cleanup.json").write_text(json.dumps({"cleanup": "PASS"}))
            with self.assertRaisesRegex(RuntimeError, "durably retained"):
                release.main("dispose")
            self.assertEqual(candidate.read_bytes(), b"synthetic-keep")
            outside = self.file("unowned-provenance/artifact", b"synthetic-untouched")
            temp = self.root / "runner-temp"
            proven = self.file("runner-temp/passvault-android-provenance/artifact", b"synthetic-retained-remotely")
            with patch.dict(os.environ, {"PASSVAULT_RELEASE_ARTIFACTS_RETAINED": "true", "RUNNER_TEMP": str(temp),
                                         "ANDROID_PROVENANCE_ROOT": str(outside.parent)}):
                with self.assertRaisesRegex(RuntimeError, "Unsafe local provenance"):
                    release.main("dispose")
                self.assertTrue(candidate.is_file())
                self.assertTrue(outside.is_file())
                with patch.dict(os.environ, {"ANDROID_PROVENANCE_ROOT": str(proven.parent)}):
                    self.assertEqual(release.main("dispose"), 0)
                self.assertFalse(proven.parent.exists())
                self.assertEqual(outside.read_bytes(), b"synthetic-untouched")
            self.assertFalse(self.out.exists())
            self.assertTrue((batch / "cleanup.json").is_file())


if __name__ == "__main__":
    unittest.main()
