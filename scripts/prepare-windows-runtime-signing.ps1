[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RuntimePath,

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[0-9A-Fa-f]{40}$")]
    [string]$CertificateThumbprint,

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[0-9A-Fa-f]{64}$")]
    [string]$CertificateSha256,

    [string]$TimestampUrl = "http://timestamp.digicert.com",
    [string]$ProductUrl = "https://github.com/Apdelrahman1911/passvault"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "Windows runtime signing must run on Windows."
}

$root = Get-Item -LiteralPath $RuntimePath -ErrorAction Stop
if (-not $root.PSIsContainer) {
    throw "RuntimePath must be a directory."
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
            $first = $stream.ReadByte()
            $second = $stream.ReadByte()
            return $first -eq 0x4d -and $second -eq 0x5a
        } finally {
            $stream.Dispose()
        }
    })
if ($unexpectedPortableExecutables.Count -ne 0) {
    throw "A PE file has an unreviewed extension: $($unexpectedPortableExecutables[0].FullName)"
}

$unsigned = [Collections.Generic.List[string]]::new()
$preserved = 0
foreach ($nativeFile in $nativeFiles) {
    $signature = Get-AuthenticodeSignature -LiteralPath $nativeFile.FullName
    if ($signature.Status -eq [Management.Automation.SignatureStatus]::Valid) {
        if (-not $signature.TimeStamperCertificate) {
            throw "An existing native signature lacks a timestamp: $($nativeFile.FullName)"
        }
        $preserved++
    } elseif ($signature.Status -eq [Management.Automation.SignatureStatus]::NotSigned) {
        if ($nativeFile.Name -cne "PassVault.exe" -or
            $nativeFile.VersionInfo.ProductName -cne "PassVault") {
            throw "Refusing to claim an unsigned third-party native file as PassVault: $($nativeFile.FullName)"
        }
        $unsigned.Add($nativeFile.FullName)
    } else {
        throw "Refusing to replace an invalid existing native signature ($($signature.Status)): $($nativeFile.FullName)"
    }
}

if ($unsigned.Count -gt 0) {
    & "$PSScriptRoot/sign-windows-artifacts.ps1" `
        -Path $unsigned.ToArray() `
        -CertificateThumbprint $CertificateThumbprint `
        -CertificateSha256 $CertificateSha256 `
        -TimestampUrl $TimestampUrl `
        -ProductUrl $ProductUrl
}

Write-Output "Preserved $preserved valid vendor signature(s) and signed $($unsigned.Count) unsigned native file(s)."
