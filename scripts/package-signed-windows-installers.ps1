[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RuntimePath,

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$")]
    [string]$Version,

    [ValidatePattern("^$|^[0-9A-Fa-f]{64}$")]
    [string]$CertificateSha256 = "",

    [Parameter(Mandatory = $true)]
    [string]$ExpectedPublisherName,

    [Parameter(Mandatory = $true)]
    [string]$Publisher,

    [Parameter(Mandatory = $true)]
    [string]$CopyrightHolder,

    [string]$OutputRoot = "app-desktop/build/compose/binaries/main-release",
    [string]$JPackagePath
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "Signed Windows installer packaging must run on Windows."
}
foreach ($value in @($Publisher, $CopyrightHolder, $ExpectedPublisherName)) {
    if ([string]::IsNullOrWhiteSpace($value) -or $value.Length -gt 200 -or
        $value.Contains("`n") -or $value.Contains("`r")) {
        throw "Publisher and copyright-holder values must be bounded single-line text."
    }
}

$runtimeRoot = Get-Item -LiteralPath $RuntimePath -ErrorAction Stop
if (-not $runtimeRoot.PSIsContainer -or
    ($runtimeRoot.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "RuntimePath must be a real directory."
}
$launchers = @(Get-ChildItem -LiteralPath $runtimeRoot.FullName -Recurse -File -Filter PassVault.exe)
if ($launchers.Count -ne 1) {
    throw "Expected exactly one signed PassVault.exe app-image launcher; found $($launchers.Count)."
}
$appImage = $launchers[0].Directory
if (-not $appImage -or ($appImage.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "The Windows app image is unsafe."
}
$reparsePoints = @(Get-ChildItem -LiteralPath $appImage.FullName -Recurse -Force |
    Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint })
if ($reparsePoints.Count -ne 0) {
    throw "The signed app image contains a reparse point: $($reparsePoints[0].FullName)"
}

$normalizedSha256 = $CertificateSha256.ToUpperInvariant()
$nativeFiles = @(Get-ChildItem -LiteralPath $appImage.FullName -Recurse -File |
    Where-Object { $_.Extension.ToLowerInvariant() -in ".exe", ".dll" } |
    Sort-Object FullName -Unique)
if ($nativeFiles.Count -eq 0) {
    throw "The signed app image contains no native executable files."
}
foreach ($nativeFile in $nativeFiles) {
    $signature = Get-AuthenticodeSignature -LiteralPath $nativeFile.FullName
    if ($signature.Status -ne [Management.Automation.SignatureStatus]::Valid -or
        -not $signature.SignerCertificate -or -not $signature.TimeStamperCertificate) {
        throw "The app image is not fully signed and timestamped: $($nativeFile.FullName)"
    }
    if ($nativeFile.Name -eq "PassVault.exe" -or
        $nativeFile.Name -ceq "passvault_biometric.dll") {
        $publisherName = $signature.SignerCertificate.GetNameInfo(
            [Security.Cryptography.X509Certificates.X509NameType]::SimpleName,
            $false
        )
        if ($publisherName -cne $ExpectedPublisherName) {
            throw "PassVault.exe does not use the expected production publisher identity."
        }
        $fingerprint = $signature.SignerCertificate.GetCertHashString(
            [Security.Cryptography.HashAlgorithmName]::SHA256
        ).ToUpperInvariant()
        if ($normalizedSha256 -and $fingerprint -ne $normalizedSha256) {
            throw "PassVault.exe does not use the pinned production publisher certificate."
        }
    }
}

if ([string]::IsNullOrWhiteSpace($JPackagePath)) {
    if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        throw "JAVA_HOME is required to locate jpackage.exe."
    }
    $JPackagePath = Join-Path $env:JAVA_HOME "bin/jpackage.exe"
}
if (-not (Test-Path -LiteralPath $JPackagePath -PathType Leaf)) {
    throw "jpackage.exe was not found: $JPackagePath"
}

$licensePath = (Get-Item -LiteralPath "LICENSE.txt" -ErrorAction Stop).FullName
$resolvedOutputRoot = [IO.Path]::GetFullPath($OutputRoot)
[IO.Directory]::CreateDirectory($resolvedOutputRoot) | Out-Null

function Get-AppImageSnapshot {
    return @(Get-ChildItem -LiteralPath $appImage.FullName -Recurse -File |
        Sort-Object FullName | ForEach-Object {
            $relativePath = [IO.Path]::GetRelativePath($appImage.FullName, $_.FullName)
            "$relativePath|$($_.Length)|$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash)"
        })
}

$snapshotBefore = Get-AppImageSnapshot
foreach ($format in @("msi", "exe")) {
    $destination = Join-Path $resolvedOutputRoot $format
    if (Test-Path -LiteralPath $destination) {
        $existing = @(Get-ChildItem -LiteralPath $destination -Force)
        if ($existing.Count -ne 0) {
            throw "The $format output directory is not empty."
        }
    } else {
        [IO.Directory]::CreateDirectory($destination) | Out-Null
    }

    $arguments = @(
        "--type", $format,
        "--app-image", $appImage.FullName,
        "--dest", $destination,
        "--name", "PassVault",
        "--app-version", $Version,
        "--description", "A secure password manager with end-to-end encryption",
        "--vendor", $Publisher,
        "--copyright", "© 2026 $CopyrightHolder. All rights reserved.",
        "--license-file", $licensePath,
        "--win-dir-chooser",
        "--win-menu",
        "--win-menu-group", "PassVault",
        "--win-shortcut",
        "--win-per-user-install",
        "--win-upgrade-uuid", "B3B60257-BA42-4233-AF33-5CECFA171EB0",
        "--verbose"
    )
    & $JPackagePath @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed while creating the $format installer."
    }
    $artifacts = @(Get-ChildItem -LiteralPath $destination -File |
        Where-Object { $_.Extension.ToLowerInvariant() -eq ".$format" })
    if ($artifacts.Count -ne 1 -or $artifacts[0].Length -le 0) {
        throw "jpackage did not create exactly one non-empty $format installer."
    }
}

$snapshotAfter = Get-AppImageSnapshot
if (($snapshotBefore -join "`n") -cne ($snapshotAfter -join "`n")) {
    throw "Installer packaging modified the already signed app image."
}

Write-Output "Packaged one MSI and one EXE from the unchanged signed Windows app image."
