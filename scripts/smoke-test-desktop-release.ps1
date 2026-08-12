[CmdletBinding()]
param(
    [ValidateRange(5, 120)]
    [int]$TimeoutSeconds = 30,

    [string]$LauncherPath =
        "app-desktop\build\compose\binaries\main-release\app\PassVault\PassVault.exe"
)

$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$configuredLauncher = if ([IO.Path]::IsPathRooted($LauncherPath)) {
    $LauncherPath
} else {
    Join-Path $repositoryRoot $LauncherPath
}
$launcher = (Resolve-Path -LiteralPath $configuredLauncher).Path
$smokeRoot = Join-Path ([IO.Path]::GetTempPath()) ("passvault-smoke-" + [Guid]::NewGuid().ToString("N"))
[IO.Directory]::CreateDirectory($smokeRoot) | Out-Null
$previousJavaToolOptions = [Environment]::GetEnvironmentVariable("JAVA_TOOL_OPTIONS", "Process")
$isolatedHomeOption = '-Duser.home="' + $smokeRoot + '"'
$process = $null

try {
    $env:JAVA_TOOL_OPTIONS = if ([string]::IsNullOrWhiteSpace($previousJavaToolOptions)) {
        $isolatedHomeOption
    } else {
        "$previousJavaToolOptions $isolatedHomeOption"
    }
    $process = Start-Process -FilePath $launcher -WindowStyle Hidden -PassThru

    if ($process.WaitForExit($TimeoutSeconds * 1000)) {
        throw "Desktop release launcher exited early with code $($process.ExitCode)."
    }

    Write-Output (
        "Desktop release launcher remained running for " +
            "$TimeoutSeconds seconds."
    )
} finally {
    [Environment]::SetEnvironmentVariable("JAVA_TOOL_OPTIONS", $previousJavaToolOptions, "Process")
    if ($null -ne $process -and !$process.HasExited) {
        Stop-Process -Id $process.Id -Force
        Wait-Process -Id $process.Id -ErrorAction SilentlyContinue
    }
    if (Test-Path -LiteralPath $smokeRoot -PathType Container) {
        [IO.Directory]::Delete($smokeRoot, $true)
    }
}
