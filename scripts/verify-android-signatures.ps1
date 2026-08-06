[CmdletBinding()]
param(
    [string]$ApkPath,
    [string]$AabPath,
    [string]$ExpectedFingerprintFile = "release/android/passvault-release-cert.sha256"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repositoryRoot = Split-Path -Parent $PSScriptRoot

if ([string]::IsNullOrWhiteSpace($ApkPath)) {
    $ApkPath = Get-ChildItem `
        -LiteralPath (Join-Path $repositoryRoot "app-android/build/outputs/apk/standard/release") `
        -Filter "*.apk" |
        Select-Object -First 1 -ExpandProperty FullName
}

if ([string]::IsNullOrWhiteSpace($AabPath)) {
    $AabPath = Get-ChildItem `
        -LiteralPath (Join-Path $repositoryRoot "app-android/build/outputs/bundle/standardRelease") `
        -Filter "*.aab" |
        Select-Object -First 1 -ExpandProperty FullName
}

$fingerprintPath = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $ExpectedFingerprintFile))
foreach ($path in @($ApkPath, $AabPath, $fingerprintPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required signature-verification input was not found: $path"
    }
}

$sdkRoot = if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { $env:ANDROID_HOME }
if (-not $sdkRoot) {
    $localProperties = Join-Path $repositoryRoot "local.properties"
    if (Test-Path -LiteralPath $localProperties) {
        $sdkLine = Get-Content -LiteralPath $localProperties |
            Where-Object { $_ -match "^sdk\.dir=" } |
            Select-Object -First 1
        if ($sdkLine) {
            $sdkRoot = ($sdkLine -replace "^sdk\.dir=", "")
            $sdkRoot = $sdkRoot.Replace("\:", ":").Replace("\\", "\")
        }
    }
}

if (-not $sdkRoot) {
    throw "ANDROID_SDK_ROOT or ANDROID_HOME is required to locate apksigner."
}

$apksignerName = if ($IsWindows) { "apksigner.bat" } else { "apksigner" }
$apksigner = Get-ChildItem -LiteralPath (Join-Path $sdkRoot "build-tools") -Directory |
    Sort-Object { [version]$_.Name } -Descending |
    ForEach-Object { Join-Path $_.FullName $apksignerName } |
    Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } |
    Select-Object -First 1

if (-not $apksigner) {
    throw "apksigner was not found under $sdkRoot/build-tools."
}

$expectedFingerprint = (Get-Content -LiteralPath $fingerprintPath -Raw) -replace "[^0-9A-Fa-f]", ""
if ($expectedFingerprint.Length -ne 64) {
    throw "The expected Android SHA-256 signer fingerprint is invalid."
}

$apkOutput = & $apksigner verify --verbose --print-certs $ApkPath 2>&1
if ($LASTEXITCODE -ne 0) {
    $apkOutput | Write-Output
    throw "APK cryptographic signature verification failed."
}

$apkDigestLine = $apkOutput |
    Where-Object { $_ -match "certificate SHA-256 digest:" } |
    Select-Object -First 1
$apkFingerprint = ($apkDigestLine -replace ".*digest:\s*", "") -replace "[^0-9A-Fa-f]", ""

if ($apkFingerprint -ne $expectedFingerprint) {
    throw "APK signer fingerprint does not match the pinned PassVault release certificate."
}

& jarsigner -verify $AabPath | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "AAB JAR signature verification failed."
}

$aabCertificate = & keytool -printcert -jarfile $AabPath
if ($LASTEXITCODE -ne 0) {
    throw "Could not read the AAB signing certificate."
}

$aabDigestLine = $aabCertificate |
    Where-Object { $_ -match "SHA256:" } |
    Select-Object -First 1
$aabFingerprint = ($aabDigestLine -replace ".*SHA256:\s*", "") -replace "[^0-9A-Fa-f]", ""

if ($aabFingerprint -ne $expectedFingerprint) {
    throw "AAB signer fingerprint does not match the pinned PassVault release certificate."
}

Write-Host "Android signatures verified against the pinned release certificate."
