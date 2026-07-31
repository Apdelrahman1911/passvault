[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string[]]$Path,

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[0-9A-Fa-f]{40}$")]
    [string]$CertificateThumbprint,

    [string]$TimestampUrl = "http://timestamp.digicert.com",
    [string]$ProductName = "PassVault",
    [string]$ProductUrl = "https://github.com/Apdelrahman1911/passvault",
    [string]$SignToolPath
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "Authenticode signing must run on Windows."
}

if (-not $TimestampUrl.StartsWith("http://") -and -not $TimestampUrl.StartsWith("https://")) {
    throw "TimestampUrl must use HTTP or HTTPS."
}

if (-not $ProductUrl.StartsWith("https://")) {
    throw "ProductUrl must use HTTPS."
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
    throw "signtool.exe was not found. Install the Windows 10 or 11 SDK signing tools."
}

$normalizedThumbprint = $CertificateThumbprint.ToUpperInvariant()
$certificate = Get-Item -LiteralPath "Cert:/CurrentUser/My/$normalizedThumbprint" -ErrorAction SilentlyContinue
if (-not $certificate) {
    throw "The requested code-signing certificate is not installed in Cert:/CurrentUser/My."
}

if (-not $certificate.HasPrivateKey) {
    throw "The installed code-signing certificate does not contain a private key."
}

$codeSigningOid = "1.3.6.1.5.5.7.3.3"
$hasCodeSigningEku = $certificate.EnhancedKeyUsageList.ObjectId.Value -contains $codeSigningOid
if (-not $hasCodeSigningEku) {
    throw "The certificate is not valid for code signing."
}

if ($certificate.NotAfter.ToUniversalTime() -le [DateTime]::UtcNow) {
    throw "The code-signing certificate has expired."
}

$artifacts = foreach ($candidate in $Path) {
    $resolved = Resolve-Path -LiteralPath $candidate -ErrorAction Stop
    $item = Get-Item -LiteralPath $resolved.Path
    if ($item.PSIsContainer) {
        Get-ChildItem -LiteralPath $item.FullName -Recurse -File |
            Where-Object { $_.Extension -in ".exe", ".msi" }
    } elseif ($item.Extension -in ".exe", ".msi") {
        $item
    } else {
        throw "Only Windows EXE and MSI artifacts can be signed: $candidate"
    }
}

$artifacts = @($artifacts | Sort-Object FullName -Unique)
if ($artifacts.Count -eq 0) {
    throw "No Windows EXE or MSI artifacts were found to sign."
}

foreach ($artifact in $artifacts) {
    & $SignToolPath sign `
        /sha1 $normalizedThumbprint `
        /s My `
        /fd SHA256 `
        /tr $TimestampUrl `
        /td SHA256 `
        /d $ProductName `
        /du $ProductUrl `
        $artifact.FullName

    if ($LASTEXITCODE -ne 0) {
        throw "Authenticode signing failed for $($artifact.Name)."
    }

    & $SignToolPath verify /pa /all /v $artifact.FullName
    if ($LASTEXITCODE -ne 0) {
        throw "Authenticode verification failed for $($artifact.Name)."
    }

    $signature = Get-AuthenticodeSignature -LiteralPath $artifact.FullName
    if ($signature.Status -ne [Management.Automation.SignatureStatus]::Valid) {
        throw "Windows does not trust the signature on $($artifact.Name): $($signature.Status)."
    }

    if ($signature.SignerCertificate.Thumbprint -ne $normalizedThumbprint) {
        throw "The signer certificate does not match for $($artifact.Name)."
    }

    if (-not $signature.TimeStamperCertificate) {
        throw "The Authenticode signature is missing an RFC 3161 timestamp: $($artifact.Name)."
    }
}

Write-Host "Signed and verified $($artifacts.Count) Windows artifact(s)."
