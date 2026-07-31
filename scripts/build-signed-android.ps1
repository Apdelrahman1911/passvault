[CmdletBinding()]
param(
    [string]$SigningDirectory = "release/private/android"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$signingPath = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $SigningDirectory))
$credentialsPath = Join-Path $signingPath "SIGNING-CREDENTIALS.txt"
$keystorePath = Join-Path $signingPath "passvault-android-release.jks"

if (-not (Test-Path -LiteralPath $credentialsPath -PathType Leaf)) {
    throw "Signing credentials were not found: $credentialsPath"
}

if (-not (Test-Path -LiteralPath $keystorePath -PathType Leaf)) {
    throw "Android release keystore was not found: $keystorePath"
}

$values = @{}
foreach ($line in Get-Content -LiteralPath $credentialsPath) {
    if ($line -match "^(Alias|Keystore password|Key password):\s*(.+)$") {
        $values[$matches[1]] = $matches[2]
    }
}

foreach ($required in @("Alias", "Keystore password", "Key password")) {
    if (-not $values.ContainsKey($required)) {
        throw "Signing credentials are missing '$required'."
    }
}

$previousValues = @{}
foreach ($name in @("KEYSTORE_PATH", "KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")) {
    $previousValues[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

try {
    $env:KEYSTORE_PATH = $keystorePath
    $env:KEYSTORE_PASSWORD = $values["Keystore password"]
    $env:KEY_ALIAS = $values["Alias"]
    $env:KEY_PASSWORD = $values["Key password"]

    $gradleArguments = @(
        ":app-android:verifyReleaseSigningConfiguration"
        ":app-android:assembleStandardRelease"
        ":app-android:bundleGoogleRelease"
        "-Ppassvault.requireReleaseSigning=true"
    )

    & (Join-Path $repositoryRoot "gradlew.bat") @gradleArguments

    if ($LASTEXITCODE -ne 0) {
        throw "The signed Android release build failed."
    }

    & (Join-Path $PSScriptRoot "verify-android-signatures.ps1")
    if ($LASTEXITCODE -ne 0) {
        throw "Android signature verification failed."
    }
} finally {
    foreach ($name in $previousValues.Keys) {
        [Environment]::SetEnvironmentVariable($name, $previousValues[$name], "Process")
    }
    $values.Clear()
}
