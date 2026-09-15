#!/usr/bin/env python3
"""Bound legacy release builds without changing signing HOME or losing deliverables.

Only fresh hosted jobs may run this adapter. Reuse the currently qualified process
scope, not archived audit runners. No signing inputs are copied into evidence.
"""
import importlib.util
import json
import os
from pathlib import Path
import shlex
import shutil
import signal
import subprocess
import sys
import tempfile
import time

sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location("current_ci_guard", Path(__file__).with_name("ci-run.py"))
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)


def require_owned_directory(path, repo):
    if path.is_symlink() or path.parent != repo or not path.is_dir():
        raise RuntimeError("Unsafe release output/evidence directory")


def stage_outputs(repo, destination, kind, private_runtime=None):
    """Move only required deliverables; same checkout filesystem, no broad archives."""
    require_owned_directory(destination, repo)
    if any(destination.iterdir()):
        raise RuntimeError("Release output destination is already occupied")
    exports = {"PASSVAULT_RELEASE_OUTPUT": str(destination)}
    if kind == "android":
        selected = {
            "ANDROID_APK_PATH": "app-android/build/outputs/apk/release/app-android-release.apk",
            "ANDROID_AAB_PATH": "app-android/build/outputs/bundle/release/app-android-release.aab",
            "ANDROID_MAPPING_PATH": "app-android/build/outputs/mapping/release/mapping.txt",
        }
        # Validate the whole selection before moving anything.
        for relative in selected.values():
            source = repo / relative
            if source.is_symlink() or not source.is_file() or source.stat().st_size == 0:
                raise RuntimeError("A required Android deliverable is missing or unsafe")
            if any(p.is_symlink() for p in source.parents if p != repo and repo in p.parents):
                raise RuntimeError("Unsafe Android deliverable parent")
        for key, relative in selected.items():
            source = repo / relative
            target = destination / source.name
            source.rename(target)
            exports[key] = str(target)
    elif kind == "ios":
        runtime = Path(private_runtime)
        if runtime.is_symlink() or not runtime.is_dir():
            raise RuntimeError("Unsafe iOS private runtime")
        ipas = list((runtime / "export").glob("*.ipa"))
        archive, link_map = runtime / "PassVault.xcarchive", runtime / "PassVault-LinkMap.txt"
        if len(ipas) != 1 or not archive.is_dir() or archive.is_symlink():
            raise RuntimeError("Required iOS archive/IPA missing or ambiguous")
        selected = {"IOS_IPA_PATH": ipas[0], "IOS_ARCHIVE_PATH": archive, "IOS_LINK_MAP_PATH": link_map}
        for source in selected.values():
            if source.is_symlink() or not source.exists() or (source.is_file() and source.stat().st_size == 0):
                raise RuntimeError("Required iOS artifact missing or unsafe")
            if any(p.is_symlink() for p in source.parents):
                raise RuntimeError("Unsafe iOS artifact parent")
        for key, source in selected.items():
            target = destination / source.name
            source.rename(target)
            exports[key] = str(target)
    elif kind == "desktop":
        root = repo / "app-desktop/build/compose/binaries/main-release"
        extensions = {"linux": ("deb", "rpm"), "win32": ("msi", "exe"), "darwin": ("dmg",)}[sys.platform]
        app = root / "app" / ("PassVault.app" if sys.platform == "darwin" else "PassVault")
        if not app.is_dir() or app.is_symlink():
            raise RuntimeError("Required Desktop app image is missing or unsafe")
        selected = []
        for extension in extensions:
            matches = list((root / extension).glob("*." + extension))
            if len(matches) != 1 or not matches[0].is_file() or matches[0].is_symlink() or matches[0].stat().st_size == 0:
                raise RuntimeError("Expected exactly one nonempty Desktop package per format")
            selected.append(matches[0])
        for source in [app, *selected]:
            if any(p.is_symlink() for p in source.parents if p != repo and repo in p.parents):
                raise RuntimeError("Unsafe Desktop deliverable parent")
        (destination / "app").mkdir()
        app.rename(destination / "app" / app.name)
        for source in selected:
            (destination / source.parent.name).mkdir(exist_ok=True)
            source.rename(destination / source.parent.name / source.name)
    else:
        raise RuntimeError("Unknown artifact selection")
    return exports


def smoke_script(outputs, private):
    app = outputs / "app"
    if sys.platform == "darwin":
        launcher = app / "PassVault.app/Contents/MacOS/PassVault"
        argv = [str(launcher)]
    elif os.name == "nt":
        launcher = app / "PassVault/PassVault.exe"
        argv = [launcher.as_posix()]
    else:
        launcher = app / "PassVault/bin/PassVault"
        xvfb = shutil.which("xvfb-run")
        if not xvfb:
            raise RuntimeError("xvfb-run is required for the isolated Linux smoke check")
        argv = [xvfb, "-a", str(launcher)]
    if any(p.is_symlink() for p in launcher.parents if p == outputs or outputs in p.parents):
        raise RuntimeError("Unsafe packaged launcher parent")
    if not launcher.is_file() or launcher.is_symlink():
        raise RuntimeError("Packaged launcher is missing or unsafe")
    script = private / "smoke.sh"
    script.write_text("exec " + shlex.join(argv) + "\n")
    return script


def main(mode, script=None):
    if os.environ.get("GITHUB_ACTIONS") != "true" or os.environ.get("RUNNER_ENVIRONMENT") != "github-hosted":
        raise RuntimeError("Release resource guard requires a fresh GitHub-hosted job")
    repo = Path(os.environ["GITHUB_WORKSPACE"]).resolve()
    if Path.cwd().resolve() != repo:
        raise RuntimeError("Unexpected release working directory")
    evidence = repo / ".release-evidence"
    outputs = repo / ".release-artifacts"
    source = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    owner = {"source": source, "run": os.environ["GITHUB_RUN_ID"],
             "attempt": os.environ["GITHUB_RUN_ATTEMPT"], "job": os.environ["GITHUB_JOB"]}
    if not evidence.exists():
        if outputs.exists() or outputs.is_symlink():
            raise RuntimeError("Unowned pre-existing release outputs")
        evidence.mkdir(mode=0o700)
        outputs.mkdir(mode=0o700)
        (evidence / "owner.json").write_text(json.dumps(owner))
    require_owned_directory(evidence, repo)
    require_owned_directory(outputs, repo)
    if (evidence / "owner.json").is_symlink() or json.loads((evidence / "owner.json").read_text()) != owner:
        raise RuntimeError("Release output ownership mismatch")
    batches = list(evidence.glob("batch-*"))
    if any(p.is_symlink() or not p.is_dir() or not (p / "cleanup.json").is_file() for p in batches):
        raise RuntimeError("Unsettled release batch; no automatic recovery")
    receipts = [p / "cleanup.json" for p in batches]
    if any(p.is_symlink() or json.loads(p.read_text()).get("cleanup") != "PASS" for p in receipts):
        raise RuntimeError("Earlier release cleanup HOLD; no automatic recovery")
    if mode == "ready":
        return 0
    if mode == "dispose":
        # No producer is active: every admitted batch must have a settled receipt.
        if any(not (p / "cleanup.json").is_file() for p in evidence.glob("batch-*")):
            raise RuntimeError("Unsettled release batch; retain output HOLD")
        if any(outputs.iterdir()) and os.environ.get("PASSVAULT_RELEASE_ARTIFACTS_RETAINED") != "true":
            raise RuntimeError("HOLD: required release artifacts are not confirmed durably retained")
        require_owned_directory(outputs, repo)
        provenance = []
        for key, name in (("ANDROID_PROVENANCE_ROOT", "passvault-android-provenance"),
                          ("IOS_PROVENANCE_ROOT", "passvault-ios-provenance")):
            if os.environ.get(key):
                path = Path(os.environ[key])
                expected = Path(os.environ["RUNNER_TEMP"]) / name
                if path != expected or path.is_symlink() or not path.is_dir():
                    raise RuntimeError("Unsafe local provenance cleanup target")
                if os.environ.get("PASSVAULT_RELEASE_ARTIFACTS_RETAINED") != "true":
                    raise RuntimeError("HOLD: provenance artifacts not confirmed durably retained")
                provenance.append(path)
        for path in provenance:
            shutil.rmtree(path)
        shutil.rmtree(outputs)
        print("Retained local release outputs removed after producer settlement.")
        return 0
    roots = guard.generated_roots(repo)
    if any(p.exists() for p in roots):
        raise RuntimeError("Unowned pre-existing generated outputs; refuse adoption")
    if guard.memory_fraction() < 0.25 or shutil.disk_usage(repo).free < 3 * 1024 ** 3:
        raise RuntimeError("Insufficient release resource headroom")
    private = Path(tempfile.mkdtemp(prefix="passvault-release-build-", dir=os.environ["RUNNER_TEMP"]))
    receipt_dir = evidence / ("batch-" + private.name)
    receipt_dir.mkdir()
    for name in ("tmp", "gradle", "konan", "home", "derived"):
        (private / name).mkdir()
    env = os.environ.copy()  # Preserve runner tracking and protected signing HOME.
    env.update(GRADLE_USER_HOME=str(private / "gradle"), KONAN_DATA_DIR=str(private / "konan"),
               TMPDIR=str(private / "tmp"), TMP=str(private / "tmp"), TEMP=str(private / "tmp"),
               PASSVAULT_RELEASE_DERIVED_DATA=str(private / "derived"), CMAKE_BUILD_PARALLEL_LEVEL="1")
    env["JAVA_TOOL_OPTIONS"] = (env.get("JAVA_TOOL_OPTIONS", "") +
                                f' -Djava.io.tmpdir="{private / "tmp"}" -Dpassvault.release.owner={private.name}').strip()
    env["GRADLE_OPTS"] = (f"-Dpassvault.release.owner={private.name} -Dorg.gradle.daemon=false "
                          "-Dorg.gradle.workers.max=1 -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false")
    # Kotlin/Native's optimized iOS framework link exceeds the small CI heap.
    # Restore the project's 4 GiB budget only for the archive; other release
    # batches retain their existing 2 GiB limit. The workflow supplies RAM
    # headroom; worker limits and the live resource floor remain unchanged.
    heap_mib = 4096 if mode == "ios" else 2048
    (private / "gradle/gradle.properties").write_text(
        f"org.gradle.jvmargs=-Xmx{heap_mib}m -Dfile.encoding=UTF-8 -Dpassvault.release.owner={private.name}\n"
        "org.gradle.workers.max=1\norg.gradle.parallel=false\norg.gradle.daemon=false\n"
        "org.gradle.configureondemand=false\norg.gradle.configuration-cache=false\n"
        "kotlin.compiler.execution.strategy=in-process\n")
    if mode == "smoke":
        home = private / "home"
        env.update(HOME=str(home), USERPROFILE=str(home),
                   APPDATA=str(home / "AppData/Roaming"), LOCALAPPDATA=str(home / "AppData/Local"),
                   XDG_DATA_HOME=str(home / ".local/share"), XDG_CONFIG_HOME=str(home / ".config"),
                   XDG_CACHE_HOME=str(home / ".cache"))
        env["JAVA_TOOL_OPTIONS"] = f'-Duser.home="{home}" -Djava.io.tmpdir="{private / "tmp"}"'
    scopes, attempted = [], set()
    result = dict(owner, mode=mode, exit=None, cleanup="HOLD", wrapper_stop="not-started", gradle_heap_mib=heap_mib)
    def cancel(_signum, _frame):
        raise RuntimeError("Release batch interrupted; no automatic retry")
    signal.signal(signal.SIGINT, cancel)
    signal.signal(signal.SIGTERM, cancel)
    try:
        if mode == "smoke":
            require_owned_directory(outputs, repo)
            script = smoke_script(outputs, private)
        scope = guard.ProcessScope()
        scopes.append(scope)
        scope.launch(Path(script).resolve(), env)
        deadline = time.monotonic() + (30 if mode == "smoke" else 50 * 60)
        while scope.process.poll() is None:
            free, available = shutil.disk_usage(repo).free, guard.memory_fraction()
            result["minimum_free_bytes"] = min(result.get("minimum_free_bytes", free), free)
            result["minimum_available_ram_fraction"] = min(result.get("minimum_available_ram_fraction", available), available)
            if free < 3 * 1024 ** 3 or available < 0.20:
                raise RuntimeError("Release resource floor reached")
            if time.monotonic() >= deadline:
                if mode != "smoke":
                    raise RuntimeError("Release batch time limit reached")
                if scope.job:
                    attempted.add(scope)
                scope.terminate()
                result["exit"] = 0
                break
            time.sleep(1)
        else:
            result["exit"] = scope.process.returncode
            if mode == "smoke":
                result["launcher_exit"] = result["exit"]
                result["exit"] = 1
                raise RuntimeError("Packaged launcher exited before the smoke deadline")
    finally:
        signal.signal(signal.SIGINT, signal.SIG_IGN)
        signal.signal(signal.SIGTERM, signal.SIG_IGN)
        try:
            try:
                guard.preserve_reports(roots, repo, receipt_dir)
            except (OSError, RuntimeError) as error:
                result["initial_report_snapshot_error"] = str(error)
            if result["exit"] is None:
                for scope in scopes:
                    if scope.job:
                        attempted.add(scope)
                    scope.terminate()
            if (private / "gradle/daemon").exists():
                stop_script = private / "stop.sh"
                stop_script.write_text("set -eu\n" + ("./gradlew.bat" if os.name == "nt" else "./gradlew") + " --stop\n")
                stop = guard.ProcessScope()
                scopes.append(stop)
                result["wrapper_stop"] = "attempted"
                stop.launch(stop_script, env)
                result["wrapper_stop"] = stop.process.wait(timeout=60)
                if result["wrapper_stop"] != 0:
                    raise RuntimeError("Original wrapper stop failed")
            guard.terminate_completed_windows_scopes(scopes, result, attempted)
            deadline = time.monotonic() + 15
            while time.monotonic() < deadline and not all(s.empty() for s in scopes):
                time.sleep(0.2)
            if not all(s.empty() for s in scopes):
                raise RuntimeError("Release workers did not settle")
            if os.name != "nt":
                processes = subprocess.check_output(["ps", "-axww", "-o", "pid=", "-o", "command="], text=True)
                if private.name in processes:
                    raise RuntimeError("Private release worker remains outside scope")
            guard.preserve_reports(roots, repo, receipt_dir)
            if any(private.rglob("CLEANUP_HOLD")):
                raise RuntimeError("Synthetic cleanup HOLD; retain all private/generated roots")
            if mode == "ios" and result["exit"] != 0:
                raise RuntimeError("Xcode failure/interruption: XPC worker settlement unproven; retain HOLD")
            if result["exit"] == 0 and mode in ("android", "desktop", "ios"):
                if mode == "ios" and Path(env.get("PRIVATE_RUNTIME", "")).absolute() != Path(env["RUNNER_TEMP"]).absolute() / "passvault-release":
                    raise RuntimeError("Unexpected iOS private runtime path")
                exports = stage_outputs(repo, outputs, mode, env.get("PRIVATE_RUNTIME"))
                with open(os.environ["GITHUB_ENV"], "a") as stream:
                    for key, value in exports.items():
                        stream.write(f"{key}={value}\n")
                result["retained"] = sorted(exports)
            for root in guard.generated_roots(repo):
                if root.exists():
                    shutil.rmtree(root)
            shutil.rmtree(private)
            result["cleanup"] = "PASS"
        except BaseException as error:
            result["cleanup_error"] = str(error)
            for scope in scopes:
                if scope in attempted:
                    continue
                try:
                    if not scope.empty():
                        attempted.add(scope)
                        scope.terminate()
                except BaseException as stop_error:
                    result["termination_error"] = str(stop_error)
            raise
        finally:
            (receipt_dir / "cleanup.json").write_text(json.dumps(result, indent=2) + "\n")
            for scope in scopes:
                scope.close()
    return result["exit"]


if __name__ == "__main__":
    if len(sys.argv) not in (2, 3) or sys.argv[1] not in ("plain", "android", "desktop", "ios", "smoke", "dispose", "ready"):
        sys.exit("Usage: release-build.py plain|android|desktop|ios <script> | smoke | dispose | ready")
    sys.exit(main(sys.argv[1], sys.argv[2] if len(sys.argv) == 3 else None))
