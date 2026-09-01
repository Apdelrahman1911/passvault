[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RuntimePath,

    [Parameter(Mandatory = $true)]
    [string[]]$InstallerPath,

    [ValidatePattern("^$|^[0-9A-Fa-f]{64}$")]
    [string]$CertificateSha256 = "",

    [Parameter(Mandatory = $true)]
    [string]$ExpectedPublisherName,

    [string]$SignToolPath
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "Windows release verification must run on Windows."
}

$normalizedSha256 = $CertificateSha256.ToUpperInvariant()
if ([string]::IsNullOrWhiteSpace($ExpectedPublisherName) -or
    $ExpectedPublisherName.Length -gt 200 -or
    $ExpectedPublisherName.Contains("`n") -or $ExpectedPublisherName.Contains("`r")) {
    throw "ExpectedPublisherName must be bounded single-line text."
}
$root = Get-Item -LiteralPath $RuntimePath -ErrorAction Stop
if (-not $root.PSIsContainer -or ($root.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "RuntimePath must be a real directory."
}

if ([string]::IsNullOrWhiteSpace($SignToolPath)) {
    $command = Get-Command signtool.exe -ErrorAction SilentlyContinue
    if ($command) {
        $SignToolPath = $command.Source
    } else {
        $kitsRoot = Join-Path ${env:ProgramFiles(x86)} "Windows Kits/10/bin"
        $SignToolPath = Get-ChildItem -LiteralPath $kitsRoot -Directory -ErrorAction SilentlyContinue |
            Sort-Object { [version]$_.Name } -Descending |
            ForEach-Object { Join-Path $_.FullName "x64/signtool.exe" } |
            Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } |
            Select-Object -First 1
    }
}
if (-not $SignToolPath -or -not (Test-Path -LiteralPath $SignToolPath -PathType Leaf)) {
    throw "signtool.exe is required for independent release verification."
}

$nativeFiles = @(Get-ChildItem -LiteralPath $root.FullName -Recurse -File |
    Where-Object { $_.Extension.ToLowerInvariant() -in ".exe", ".dll" } |
    Sort-Object FullName -Unique)
if ($nativeFiles.Count -eq 0) {
    throw "The packaged runtime contains no Windows native files."
}
$unexpectedPortableExecutables = @(Get-ChildItem -LiteralPath $root.FullName -Recurse -File |
    Where-Object {
        if ($_.Extension.ToLowerInvariant() -in ".exe", ".dll") {
            return $false
        }
        $stream = [IO.File]::Open($_.FullName, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::Read)
        try {
            return $stream.ReadByte() -eq 0x4d -and $stream.ReadByte() -eq 0x5a
        } finally {
            $stream.Dispose()
        }
    })
if ($unexpectedPortableExecutables.Count -ne 0) {
    throw "A PE file has an unreviewed extension: $($unexpectedPortableExecutables[0].FullName)"
}
$launchers = @($nativeFiles | Where-Object { $_.Name -eq "PassVault.exe" })
if ($launchers.Count -ne 1) {
    throw "Expected exactly one PassVault.exe launcher; found $($launchers.Count)."
}

$installers = @($InstallerPath | ForEach-Object {
    Get-Item -LiteralPath $_ -ErrorAction Stop
} | Sort-Object FullName -Unique)
$extensions = @($installers.Extension.ToLowerInvariant() | Sort-Object)
if ($installers.Count -ne 2 -or ($extensions -join ",") -ne ".exe,.msi") {
    throw "Expected exactly one EXE installer and one MSI installer."
}

function Get-Sha256Fingerprint([Security.Cryptography.X509Certificates.X509Certificate2]$Certificate) {
    return $Certificate.GetCertHashString(
        [Security.Cryptography.HashAlgorithmName]::SHA256
    ).ToUpperInvariant()
}

function Assert-ValidTimestampedSignature([IO.FileInfo]$File, [bool]$RequirePublisher) {
    & $SignToolPath verify /pa /all /v $File.FullName | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "signtool rejected an Authenticode signature: $($File.FullName)"
    }
    $signature = Get-AuthenticodeSignature -LiteralPath $File.FullName
    if ($signature.Status -ne [Management.Automation.SignatureStatus]::Valid) {
        throw "Invalid Authenticode signature ($($signature.Status)): $($File.FullName)"
    }
    if (-not $signature.SignerCertificate -or -not $signature.TimeStamperCertificate) {
        throw "A signer or RFC 3161 timestamp is missing: $($File.FullName)"
    }
    if ($RequirePublisher) {
        $publisherName = $signature.SignerCertificate.GetNameInfo(
            [Security.Cryptography.X509Certificates.X509NameType]::SimpleName,
            $false
        )
        if ($publisherName -cne $ExpectedPublisherName) {
            throw "The PassVault publisher identity does not match: $($File.FullName)"
        }
        if ($normalizedSha256 -and
            (Get-Sha256Fingerprint $signature.SignerCertificate) -ne $normalizedSha256) {
            throw "The PassVault publisher fingerprint does not match: $($File.FullName)"
        }
    }
}

function Assert-BiometricBridgeBinding([string]$ImageRoot) {
    $libraries = @(Get-ChildItem -LiteralPath $ImageRoot -Recurse -File |
        Where-Object { $_.Name -ceq "passvault_biometric.dll" })
    $manifests = @(Get-ChildItem -LiteralPath $ImageRoot -Recurse -File |
        Where-Object { $_.Name -ceq "bridge.properties" -and
            $_.Directory.Name -ceq "windows-x64" })
    if ($libraries.Count -ne 1 -or $manifests.Count -ne 1 -or
        $libraries[0].Directory.FullName -cne $manifests[0].Directory.FullName) {
        throw "The app image must contain exactly one co-located Windows biometric bridge and manifest."
    }
    $launcher = Join-Path $ImageRoot "PassVault.exe"
    $bridgeSignature = Get-AuthenticodeSignature -LiteralPath $libraries[0].FullName
    $launcherSignature = Get-AuthenticodeSignature -LiteralPath $launcher
    if ($bridgeSignature.Status -ne [Management.Automation.SignatureStatus]::Valid -or
        $launcherSignature.Status -ne [Management.Automation.SignatureStatus]::Valid -or
        -not $bridgeSignature.SignerCertificate -or -not $launcherSignature.SignerCertificate -or
        -not $bridgeSignature.TimeStamperCertificate -or -not $launcherSignature.TimeStamperCertificate -or
        $bridgeSignature.SignerCertificate.Thumbprint -cne $launcherSignature.SignerCertificate.Thumbprint) {
        throw "The biometric bridge and PassVault launcher must share one valid timestamped Authenticode signer."
    }
    $entries = @{}
    foreach ($line in [IO.File]::ReadAllLines($manifests[0].FullName, [Text.Encoding]::UTF8)) {
        if ($line -notmatch '^(?<key>[a-z][a-z0-9_]{0,31})=(?<value>[A-Za-z0-9._-]{1,256})$' -or
            $entries.ContainsKey($Matches.key)) {
            throw "The packaged biometric bridge manifest is malformed."
        }
        $entries[$Matches.key] = $Matches.value
    }
    $expectedKeys = @("abi", "integrity", "library", "platform", "sha256") | Sort-Object
    if ((($entries.Keys | Sort-Object) -join "`n") -cne ($expectedKeys -join "`n") -or
        $entries["abi"] -cne "1" -or $entries["integrity"] -cne "sha256-and-authenticode" -or
        $entries["library"] -cne "passvault_biometric.dll" -or
        $entries["platform"] -cne "windows-x64" -or
        $entries["sha256"] -notmatch '^[0-9a-f]{64}$') {
        throw "The packaged Windows biometric bridge manifest violates release policy."
    }
    $actual = (Get-FileHash -LiteralPath $libraries[0].FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -cne $entries["sha256"]) {
        throw "The packaged Windows biometric bridge checksum does not match its signed bytes."
    }
}

foreach ($nativeFile in $nativeFiles) {
    $isPassVaultCode = $nativeFile.Name -eq "PassVault.exe" -or
        $nativeFile.Name -ceq "passvault_biometric.dll"
    Assert-ValidTimestampedSignature $nativeFile $isPassVaultCode
}
foreach ($installer in $installers) {
    Assert-ValidTimestampedSignature $installer $true
}

$msi = @($installers | Where-Object { $_.Extension.ToLowerInvariant() -eq ".msi" })[0]
$runnerTemporaryRoot = [IO.Path]::GetFullPath($env:RUNNER_TEMP)
$extractionRoot = Join-Path $runnerTemporaryRoot "passvault-msi-$([Guid]::NewGuid().ToString('N'))"
$extractionLog = "$extractionRoot.log"
try {
    Assert-BiometricBridgeBinding $launchers[0].Directory.FullName
    [IO.Directory]::CreateDirectory($extractionRoot) | Out-Null
    $msiArguments = "/a `"$($msi.FullName)`" /qn TARGETDIR=`"$extractionRoot`" /l*v `"$extractionLog`""
    $process = Start-Process -FilePath msiexec.exe -ArgumentList $msiArguments -Wait -PassThru
    if ($process.ExitCode -notin 0, 3010) {
        if (Test-Path -LiteralPath $extractionLog -PathType Leaf) {
            Get-Content -LiteralPath $extractionLog -Tail 100 | Out-Host
        }
        throw "Administrative MSI extraction failed with exit code $($process.ExitCode)."
    }

    $extractedLaunchers = @(Get-ChildItem -LiteralPath $extractionRoot -Recurse -File -Filter PassVault.exe)
    if ($extractedLaunchers.Count -ne 1) {
        throw "The MSI must contain exactly one PassVault.exe; found $($extractedLaunchers.Count)."
    }
    $sourceImageRoot = $launchers[0].Directory.FullName
    $extractedImageRoot = $extractedLaunchers[0].Directory.FullName
    $sourceNative = @{}
    foreach ($nativeFile in $nativeFiles) {
        if (-not $nativeFile.FullName.StartsWith(
            "$sourceImageRoot$([IO.Path]::DirectorySeparatorChar)",
            [StringComparison]::OrdinalIgnoreCase
        ) -and $nativeFile.FullName -ne $launchers[0].FullName) {
            throw "A native runtime file exists outside the PassVault app image: $($nativeFile.FullName)"
        }
        $relative = [IO.Path]::GetRelativePath($sourceImageRoot, $nativeFile.FullName)
        $sourceNative[$relative] = $nativeFile
    }
    $extractedNativeFiles = @(Get-ChildItem -LiteralPath $extractedImageRoot -Recurse -File |
        Where-Object { $_.Extension.ToLowerInvariant() -in ".exe", ".dll" } |
        Sort-Object FullName -Unique)
    $extractedNative = @{}
    foreach ($nativeFile in $extractedNativeFiles) {
        $relative = [IO.Path]::GetRelativePath($extractedImageRoot, $nativeFile.FullName)
        $extractedNative[$relative] = $nativeFile
    }
    $sourceNames = @($sourceNative.Keys | Sort-Object)
    $extractedNames = @($extractedNative.Keys | Sort-Object)
    if (($sourceNames -join "`n") -cne ($extractedNames -join "`n")) {
        throw "The MSI native-file set differs from the exact signed app image."
    }
    foreach ($relative in $sourceNames) {
        $sourceHash = (Get-FileHash -LiteralPath $sourceNative[$relative].FullName -Algorithm SHA256).Hash
        $extractedHash = (Get-FileHash -LiteralPath $extractedNative[$relative].FullName -Algorithm SHA256).Hash
        if ($sourceHash -ne $extractedHash) {
            throw "The MSI changed signed native bytes: $relative"
        }
        $isPassVaultCode = $relative -eq "PassVault.exe" -or
            $extractedNative[$relative].Name -ceq "passvault_biometric.dll"
        Assert-ValidTimestampedSignature $extractedNative[$relative] $isPassVaultCode
    }
    Assert-BiometricBridgeBinding $extractedImageRoot
} finally {
    if (Test-Path -LiteralPath $extractionRoot) {
        Remove-Item -LiteralPath $extractionRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
    Remove-Item -LiteralPath $extractionLog -Force -ErrorAction SilentlyContinue
}

Write-Output "Verified $($nativeFiles.Count) signed native files, both installers, and the exact MSI payload."
