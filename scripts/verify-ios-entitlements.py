#!/usr/bin/env python3
"""Verify that signed iOS entitlements match the reviewed declaration."""

from __future__ import annotations

import json
import plistlib
import re
import sys
from pathlib import Path
from typing import Any


MAX_PLIST_BYTES = 1024 * 1024
TEAM_ID_PATTERN = re.compile(r"[A-Z0-9]{10}\Z")
BUNDLE_ID_PATTERN = re.compile(r"[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+\Z")
PLACEHOLDER_PATTERN = re.compile(r"\$\([^)]+\)")


class EntitlementError(ValueError):
    """Raised when an entitlement input violates the release policy."""


def load_plist(path: Path, label: str) -> dict[str, Any]:
    if path.is_symlink() or not path.is_file():
        raise EntitlementError(f"The {label} entitlements file is missing or unsafe.")
    if path.stat().st_size > MAX_PLIST_BYTES:
        raise EntitlementError(f"The {label} entitlements file is unexpectedly large.")
    try:
        with path.open("rb") as stream:
            value = plistlib.load(stream)
    except (OSError, plistlib.InvalidFileException) as error:
        raise EntitlementError(f"The {label} entitlements file is not a valid plist.") from error
    if not isinstance(value, dict) or not all(isinstance(key, str) for key in value):
        raise EntitlementError(f"The {label} entitlements must be a string-keyed dictionary.")
    return value


def expand_reviewed_value(value: Any, team_id: str, bundle_id: str) -> Any:
    if isinstance(value, str):
        expanded = (
            value.replace("$(AppIdentifierPrefix)", f"{team_id}.")
            .replace("$(TeamIdentifierPrefix)", f"{team_id}.")
            .replace("$(CFBundleIdentifier)", bundle_id)
            .replace("$(PRODUCT_BUNDLE_IDENTIFIER)", bundle_id)
        )
        if PLACEHOLDER_PATTERN.search(expanded):
            raise EntitlementError("The reviewed entitlements contain an unsupported placeholder.")
        return expanded
    if isinstance(value, list):
        return [expand_reviewed_value(item, team_id, bundle_id) for item in value]
    if isinstance(value, dict):
        if not all(isinstance(key, str) for key in value):
            raise EntitlementError("The reviewed entitlements contain a non-string dictionary key.")
        return {
            key: expand_reviewed_value(item, team_id, bundle_id)
            for key, item in value.items()
        }
    if value is None or type(value) in (bool, int, float, bytes):
        return value
    raise EntitlementError("The reviewed entitlements contain an unsupported value type.")


def values_match(actual: Any, expected: Any) -> bool:
    if type(actual) is not type(expected):
        return False
    if isinstance(actual, dict):
        return actual.keys() == expected.keys() and all(
            values_match(actual[key], expected[key]) for key in actual
        )
    if isinstance(actual, list):
        return len(actual) == len(expected) and all(
            values_match(left, right) for left, right in zip(actual, expected)
        )
    return actual == expected


def json_safe(value: Any) -> Any:
    if isinstance(value, bytes):
        return {"$data_hex": value.hex()}
    if isinstance(value, dict):
        return {key: json_safe(item) for key, item in value.items()}
    if isinstance(value, list):
        return [json_safe(item) for item in value]
    return value


def verify(
    reviewed_path: Path,
    signed_path: Path,
    team_id: str,
    bundle_id: str,
) -> dict[str, Any]:
    if not TEAM_ID_PATTERN.fullmatch(team_id):
        raise EntitlementError("The expected Apple Team ID is invalid.")
    if not BUNDLE_ID_PATTERN.fullmatch(bundle_id):
        raise EntitlementError("The expected iOS bundle identifier is invalid.")

    reviewed = load_plist(reviewed_path, "reviewed")
    signed = load_plist(signed_path, "signed")
    expected = expand_reviewed_value(reviewed, team_id, bundle_id)

    generated_required = {
        "application-identifier": f"{team_id}.{bundle_id}",
        "com.apple.developer.team-identifier": team_id,
    }
    for key, value in generated_required.items():
        if key in expected and not values_match(expected[key], value):
            raise EntitlementError(f"Reviewed entitlement '{key}' conflicts with the release identity.")
        expected[key] = value

    # Xcode may materialize these App Store distribution properties in the
    # signature. They are optional because their authority is also checked in
    # the embedded profile, but any materialized value is pinned here.
    generated_optional = {
        "beta-reports-active": True,
        "get-task-allow": False,
    }
    for key, value in generated_optional.items():
        if key in signed:
            expected[key] = value

    unexpected = sorted(signed.keys() - expected.keys())
    missing = sorted(expected.keys() - signed.keys())
    if unexpected:
        raise EntitlementError(
            "Signed artifact contains unreviewed entitlements: " + ", ".join(unexpected)
        )
    if missing:
        raise EntitlementError(
            "Signed artifact is missing required entitlements: " + ", ".join(missing)
        )
    for key in sorted(expected):
        if not values_match(signed[key], expected[key]):
            raise EntitlementError(f"Signed entitlement '{key}' does not match the reviewed value.")
    return signed


def main(argv: list[str]) -> int:
    if len(argv) != 5:
        print(
            "Usage: verify-ios-entitlements.py "
            "<reviewed-entitlements> <signed-entitlements> <team-id> <bundle-id>",
            file=sys.stderr,
        )
        return 2
    try:
        normalized = verify(Path(argv[1]), Path(argv[2]), argv[3], argv[4])
    except EntitlementError as error:
        print(error, file=sys.stderr)
        return 1
    print(json.dumps(json_safe(normalized), sort_keys=True, separators=(",", ":")))
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
