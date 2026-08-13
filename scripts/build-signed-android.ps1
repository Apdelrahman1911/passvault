[CmdletBinding()]
param(
    [string]$ValuesFile = "release/private/values.env"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repositoryRoot = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$privateRoot = [IO.Path]::GetFullPath((Join-Path $repositoryRoot "release/private"))
$privatePrefix = $privateRoot.TrimEnd(
    [IO.Path]::DirectorySeparatorChar,
    [IO.Path]::AltDirectorySeparatorChar
) + [IO.Path]::DirectorySeparatorChar
$pathComparison = if ([Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT) {
    [StringComparison]::OrdinalIgnoreCase
} else {
    [StringComparison]::Ordinal
}

function Resolve-PrivateFile {
    param(
        [Parameter(Mandatory)]
        [string]$Path,

        [Parameter(Mandatory)]
        [string]$Description
    )

    if ([IO.Path]::IsPathRooted($Path)) {
        throw "$Description must be configured as a repository-relative path below release/private/."
    }

    $resolved = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $Path))
    if (-not $resolved.StartsWith($privatePrefix, $pathComparison)) {
        throw "$Description must remain below release/private/."
    }
    if (-not (Test-Path -LiteralPath $resolved -PathType Leaf)) {
        throw "$Description was not found: $resolved"
    }

    # Reject reparse points in every path component, not only the final file.
    # A regular file below a symlinked directory could otherwise escape the
    # private release root while still passing the lexical prefix check.
    $relativePath = [IO.Path]::GetRelativePath($privateRoot, $resolved)
    $currentPath = $privateRoot
    $privateRootItem = Get-Item -LiteralPath $currentPath -Force
    if (($privateRootItem.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
        throw "release/private must not be a symlink or reparse point."
    }
    $pathParts = $relativePath.Split(
        [char[]]@([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar),
        [StringSplitOptions]::RemoveEmptyEntries
    )
    foreach ($pathPart in $pathParts) {
        $currentPath = Join-Path $currentPath $pathPart
        $item = Get-Item -LiteralPath $currentPath -Force
        if (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
            throw "$Description must not traverse a symlink or reparse point."
        }
    }

    return $resolved
}

$valuesPath = Resolve-PrivateFile -Path $ValuesFile -Description "Private release values file"
$values = @{}
$lineNumber = 0

foreach ($line in Get-Content -LiteralPath $valuesPath) {
    $lineNumber++
    if ($line -match '^\s*$' -or $line -match '^\s*#') {
        continue
    }
    if ($line -notmatch '^([A-Z][A-Z0-9_]*)=(.*)$') {
        throw "Invalid dotenv entry at line $lineNumber in $valuesPath."
    }

    $name = $matches[1]
    $value = $matches[2]
    if ($value.Length -gt 0 -and ($value[0] -eq "'" -or $value[0] -eq '"')) {
        if ($value.Length -lt 2 -or $value[$value.Length - 1] -ne $value[0]) {
            throw "Unterminated quoted value for $name at line $lineNumber."
        }
        $value = $value.Substring(1, $value.Length - 2)
    }
    if ($values.ContainsKey($name)) {
        throw "Duplicate dotenv value for $name at line $lineNumber."
    }
    $values[$name] = $value
}

$requiredNames = @(
    "ANDROID_UPLOAD_KEYSTORE_FILE",
    "KEYSTORE_PASSWORD",
    "KEY_ALIAS",
    "KEY_PASSWORD"
)
foreach ($name in $requiredNames) {
    if (-not $values.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($values[$name])) {
        throw "Private release values are missing $name."
    }
}

$keystorePath = Resolve-PrivateFile `
    -Path $values["ANDROID_UPLOAD_KEYSTORE_FILE"] `
    -Description "Android upload keystore"
$canonicalAliasPath = Join-Path $repositoryRoot "release/android/passvault-upload-alias.txt"
$canonicalAlias = (Get-Content -LiteralPath $canonicalAliasPath -Raw).Trim()
if ($values["KEY_ALIAS"] -cne $canonicalAlias) {
    throw "KEY_ALIAS must match the registered PassVault upload alias: $canonicalAlias"
}

$environmentNames = @("KEYSTORE_PATH", "KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
$previousValues = @{}
foreach ($name in $environmentNames) {
    $previousValues[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

try {
    $env:KEYSTORE_PATH = $keystorePath
    $env:KEYSTORE_PASSWORD = $values["KEYSTORE_PASSWORD"]
    $env:KEY_ALIAS = $values["KEY_ALIAS"]
    $env:KEY_PASSWORD = $values["KEY_PASSWORD"]

    $gradle = if ([Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT) {
        Join-Path $repositoryRoot "gradlew.bat"
    } else {
        Join-Path $repositoryRoot "gradlew"
    }
    $gradleArguments = @(
        ":app-android:verifyReleaseSigningConfiguration",
        ":app-android:assembleRelease",
        ":app-android:bundleRelease",
        ":app-android:lintRelease",
        ":app-android:verifyReleasePackageContents",
        "--no-configuration-cache",
        "-Ppassvault.requireReleaseSigning=true"
    )

    & $gradle @gradleArguments
    if ($LASTEXITCODE -ne 0) {
        throw "The signed Android release build failed."
    }

    if (-not (Get-Command ruby -ErrorAction SilentlyContinue)) {
        throw "Ruby is required to validate the final Google Play manifest."
    }
    & ruby (Join-Path $PSScriptRoot "validate-google-play-readiness.rb")
    if ($LASTEXITCODE -ne 0) {
        throw "Google Play readiness validation failed."
    }

    & (Join-Path $PSScriptRoot "verify-android-signatures.ps1")
    if ($LASTEXITCODE -ne 0) {
        throw "Android signature verification failed."
    }
} finally {
    foreach ($name in $environmentNames) {
        [Environment]::SetEnvironmentVariable($name, $previousValues[$name], "Process")
    }
    $values.Clear()
    $keystorePath = $null
}
