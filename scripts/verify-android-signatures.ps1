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
    $apkMatches = @(Get-ChildItem `
        -LiteralPath (Join-Path $repositoryRoot "app-android/build/outputs/apk/standard/release") `
        -File -Filter "*.apk")
    if ($apkMatches.Count -ne 1) {
        throw "Expected exactly one Standard release APK; found $($apkMatches.Count)."
    }
    $ApkPath = $apkMatches[0].FullName
}

if ([string]::IsNullOrWhiteSpace($AabPath)) {
    $aabMatches = @(Get-ChildItem `
        -LiteralPath (Join-Path $repositoryRoot "app-android/build/outputs/bundle/standardRelease") `
        -File -Filter "*.aab")
    if ($aabMatches.Count -ne 1) {
        throw "Expected exactly one Standard release AAB; found $($aabMatches.Count)."
    }
    $AabPath = $aabMatches[0].FullName
}

$fingerprintPath = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $ExpectedFingerprintFile))
foreach ($path in @($ApkPath, $AabPath, $fingerprintPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required signature-verification input was not found: $path"
    }
    $item = Get-Item -LiteralPath $path -Force
    if (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
        throw "Signature-verification inputs must not be symlinks or reparse points: $path"
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
$apksigner = & (Join-Path $PSScriptRoot "select-android-build-tool.ps1") `
    -BuildToolsRoot (Join-Path $sdkRoot "build-tools") `
    -ToolName $apksignerName

if (-not $apksigner) {
    throw "apksigner was not found under $sdkRoot/build-tools."
}

$expectedFingerprint = (
    (Get-Content -LiteralPath $fingerprintPath -Raw) -replace "[^0-9A-Fa-f]", ""
).ToUpperInvariant()
if ($expectedFingerprint.Length -ne 64) {
    throw "The expected Android SHA-256 signer fingerprint is invalid."
}

$apkOutput = & $apksigner verify --verbose --print-certs $ApkPath 2>&1
if ($LASTEXITCODE -ne 0) {
    $apkOutput | Write-Output
    throw "APK cryptographic signature verification failed."
}

$apkDigestLines = @($apkOutput |
    Where-Object { $_ -match "certificate SHA-256 digest:" } |
    ForEach-Object {
        (($_ -replace ".*digest:\s*", "") -replace "[^0-9A-Fa-f]", "").ToUpperInvariant()
    } |
    Sort-Object -Unique)

if ($apkDigestLines.Count -ne 1 -or $apkDigestLines[0] -ne $expectedFingerprint) {
    throw "APK must have exactly the pinned PassVault release-certificate signer."
}

& jarsigner -verify -strict $AabPath 2>&1 | Out-Null
$jarsignerExitCode = $LASTEXITCODE
# jarsigner strict status 4 reports the expected untrusted/self-signed chain.
# The Android upload certificate is separately pinned by its exact SHA-256 digest below.
if ($jarsignerExitCode -ne 0 -and $jarsignerExitCode -ne 4) {
    throw "AAB strict JAR signature verification failed."
}

$aabCertificate = & keytool -printcert -jarfile $AabPath
if ($LASTEXITCODE -ne 0) {
    throw "Could not read the AAB signing certificate."
}

$aabDigestLines = @($aabCertificate |
    Where-Object { $_ -match "SHA256:" } |
    ForEach-Object {
        (($_ -replace ".*SHA256:\s*", "") -replace "[^0-9A-Fa-f]", "").ToUpperInvariant()
    } |
    Sort-Object -Unique)

if ($aabDigestLines.Count -ne 1 -or $aabDigestLines[0] -ne $expectedFingerprint) {
    throw "AAB must have exactly the pinned PassVault release-certificate signer."
}

Write-Output "Android signatures verified against the pinned release certificate."
