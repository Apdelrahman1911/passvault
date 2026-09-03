#!/usr/bin/env python3
"""Focused tests for the signed iOS entitlement allowlist."""

from __future__ import annotations

import plistlib
import subprocess
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).with_name("verify-ios-entitlements.py")
TEAM_ID = "ABCDE12345"
BUNDLE_ID = "com.passvault.ios"
APP_IDENTIFIER = f"{TEAM_ID}.{BUNDLE_ID}"


class IosEntitlementVerifierTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary_directory = tempfile.TemporaryDirectory(
            prefix="passvault-ios-entitlement-test."
        )
        self.root = Path(self.temporary_directory.name)
        self.reviewed = {
            "com.apple.developer.default-data-protection": "NSFileProtectionComplete",
            "keychain-access-groups": ["$(AppIdentifierPrefix)$(CFBundleIdentifier)"],
        }
        self.signed = {
            "application-identifier": APP_IDENTIFIER,
            "com.apple.developer.default-data-protection": "NSFileProtectionComplete",
            "com.apple.developer.team-identifier": TEAM_ID,
            "keychain-access-groups": [APP_IDENTIFIER],
        }

    def tearDown(self) -> None:
        self.temporary_directory.cleanup()

    def run_verifier(
        self,
        *,
        reviewed: dict | None = None,
        signed: dict | None = None,
    ) -> subprocess.CompletedProcess[str]:
        reviewed_path = self.root / "reviewed.plist"
        signed_path = self.root / "signed.plist"
        with reviewed_path.open("wb") as stream:
            plistlib.dump(self.reviewed if reviewed is None else reviewed, stream)
        with signed_path.open("wb") as stream:
            plistlib.dump(self.signed if signed is None else signed, stream)
        return subprocess.run(
            [
                "python3",
                str(SCRIPT),
                str(reviewed_path),
                str(signed_path),
                TEAM_ID,
                BUNDLE_ID,
            ],
            check=False,
            capture_output=True,
            text=True,
        )

    def assert_rejected(self, signed: dict, message: str) -> None:
        result = self.run_verifier(signed=signed)
        self.assertEqual(1, result.returncode)
        self.assertIn(message, result.stderr)

    def test_accepts_exact_reviewed_and_generated_entitlements(self) -> None:
        result = self.run_verifier()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn('"application-identifier":"ABCDE12345.com.passvault.ios"', result.stdout)

    def test_accepts_pinned_distribution_entitlements(self) -> None:
        signed = dict(self.signed)
        signed["beta-reports-active"] = True
        signed["get-task-allow"] = False
        result = self.run_verifier(signed=signed)
        self.assertEqual(0, result.returncode, result.stderr)

    def test_rejects_an_unreviewed_entitlement(self) -> None:
        signed = dict(self.signed)
        signed["com.apple.developer.associated-domains"] = ["applinks:attacker.example"]
        self.assert_rejected(signed, "unreviewed entitlements")

    def test_rejects_a_missing_reviewed_entitlement(self) -> None:
        signed = dict(self.signed)
        del signed["com.apple.developer.default-data-protection"]
        self.assert_rejected(signed, "missing required entitlements")

    def test_rejects_a_missing_generated_identity_entitlement(self) -> None:
        signed = dict(self.signed)
        del signed["application-identifier"]
        self.assert_rejected(signed, "missing required entitlements")

    def test_rejects_a_modified_reviewed_entitlement(self) -> None:
        signed = dict(self.signed)
        signed["keychain-access-groups"] = [APP_IDENTIFIER, f"{TEAM_ID}.shared"]
        self.assert_rejected(signed, "does not match the reviewed value")

    def test_rejects_unsafe_generated_distribution_values(self) -> None:
        for key, value in (("get-task-allow", True), ("beta-reports-active", False)):
            with self.subTest(key=key):
                signed = dict(self.signed)
                signed[key] = value
                self.assert_rejected(signed, "does not match the reviewed value")

    def test_rejects_an_unresolved_reviewed_placeholder(self) -> None:
        reviewed = dict(self.reviewed)
        reviewed["example"] = "$(UnsupportedValue)"
        result = self.run_verifier(reviewed=reviewed)
        self.assertEqual(1, result.returncode)
        self.assertIn("unsupported placeholder", result.stderr)


if __name__ == "__main__":
    unittest.main()
