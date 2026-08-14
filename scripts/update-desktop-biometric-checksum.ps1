[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RuntimePath
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "The Windows biometric checksum must be refreshed on Windows."
}

$runtime = Get-Item -LiteralPath $RuntimePath -ErrorAction Stop
if (-not $runtime.PSIsContainer -or
    ($runtime.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "RuntimePath must be a real Windows app-image directory."
}

$libraries = @(Get-ChildItem -LiteralPath $runtime.FullName -Recurse -File |
    Where-Object { $_.Name -ceq "passvault_biometric.dll" })
$manifests = @(Get-ChildItem -LiteralPath $runtime.FullName -Recurse -File |
    Where-Object { $_.Name -ceq "bridge.properties" -and
        $_.Directory.Name -ceq "windows-x64" })
if ($libraries.Count -ne 1 -or $manifests.Count -ne 1) {
    throw "Expected exactly one Windows biometric bridge and one matching manifest."
}
$library = $libraries[0]
$manifest = $manifests[0]
if (($library.Attributes -band [IO.FileAttributes]::ReparsePoint) -or
    ($manifest.Attributes -band [IO.FileAttributes]::ReparsePoint) -or
    $library.Directory.FullName -cne $manifest.Directory.FullName) {
    throw "The Windows biometric bridge resources are unsafe or separated."
}

$signature = Get-AuthenticodeSignature -LiteralPath $library.FullName
if ($signature.Status -ne [Management.Automation.SignatureStatus]::Valid -or
    -not $signature.SignerCertificate -or -not $signature.TimeStamperCertificate) {
    throw "The Windows biometric bridge must be signed and timestamped before refreshing its checksum."
}

$entries = [Collections.Generic.Dictionary[string, string]]::new([StringComparer]::Ordinal)
foreach ($line in [IO.File]::ReadAllLines($manifest.FullName, [Text.Encoding]::UTF8)) {
    if ($line -notmatch '^(?<key>[a-z][a-z0-9_]{0,31})=(?<value>[A-Za-z0-9._-]{1,256})$' -or
        -not $entries.TryAdd($Matches.key, $Matches.value)) {
        throw "The Windows biometric bridge manifest is malformed."
    }
}
$expectedKeys = @("abi", "integrity", "library", "platform", "sha256") | Sort-Object
$actualKeys = (($entries.Keys | Sort-Object) -join "`n")
$expectedKeyText = ($expectedKeys -join "`n")
if ($actualKeys -cne $expectedKeyText -or
    $entries["abi"] -cne "1" -or $entries["integrity"] -cne "sha256" -or
    $entries["library"] -cne "passvault_biometric.dll" -or
    $entries["platform"] -cne "windows-x64" -or
    $entries["sha256"] -notmatch '^[0-9a-f]{64}$') {
    throw "The Windows biometric bridge manifest does not match release policy."
}

$checksum = (Get-FileHash -LiteralPath $library.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
$content = @(
    "abi=1",
    "platform=windows-x64",
    "library=passvault_biometric.dll",
    "integrity=sha256",
    "sha256=$checksum"
)
[IO.File]::WriteAllLines($manifest.FullName, $content, [Text.UTF8Encoding]::new($false))

$verified = (Get-FileHash -LiteralPath $library.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
if ($verified -cne $checksum) {
    throw "The Windows biometric bridge changed while its checksum was refreshed."
}

Write-Output "Refreshed the checksum for the signed Windows biometric bridge."
