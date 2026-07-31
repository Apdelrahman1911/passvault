[CmdletBinding()]
param(
    [ValidateRange(5, 120)]
    [int]$TimeoutSeconds = 30,

    [string]$LauncherPath =
        "app-desktop\build\compose\binaries\main-release\app\PassVault\PassVault.exe"
)

$ErrorActionPreference = "Stop"

$launcher = (Resolve-Path -LiteralPath $LauncherPath).Path
$process = Start-Process -FilePath $launcher -WindowStyle Hidden -PassThru

try {
    if ($process.WaitForExit($TimeoutSeconds * 1000)) {
        throw "Desktop release launcher exited early with code $($process.ExitCode)."
    }

    Write-Host (
        "Desktop release launcher remained running for " +
            "$TimeoutSeconds seconds."
    )
} finally {
    if (!$process.HasExited) {
        Stop-Process -Id $process.Id -Force
        Wait-Process -Id $process.Id -ErrorAction SilentlyContinue
    }
}
