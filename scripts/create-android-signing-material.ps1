[CmdletBinding()]
param(
    [string]$OutputDirectory = "release/private/android",
    [string]$Alias = "passvault_release",
    [string]$DistinguishedName = "CN=PassVault Android Release, OU=Release, O=PassVault"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not (Get-Command keytool -ErrorAction SilentlyContinue)) {
    throw "keytool was not found. Install JDK 17 and ensure keytool is on PATH."
}

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$resolvedOutput = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $OutputDirectory))
$privateRoot = [IO.Path]::GetFullPath((Join-Path $repositoryRoot "release/private"))

if (-not $resolvedOutput.StartsWith($privateRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Signing material must be generated under release/private/."
}

$keystorePath = Join-Path $resolvedOutput "passvault-android-release.jks"
$credentialsPath = Join-Path $resolvedOutput "SIGNING-CREDENTIALS.txt"
$githubSecretsPath = Join-Path $resolvedOutput "GITHUB-SECRETS.txt"
$certificatePath = Join-Path $resolvedOutput "passvault-android-release-cert.pem"

foreach ($path in @($keystorePath, $credentialsPath, $githubSecretsPath, $certificatePath)) {
    if (Test-Path -LiteralPath $path) {
        throw "Refusing to overwrite existing signing material: $path"
    }
}

[IO.Directory]::CreateDirectory($resolvedOutput) | Out-Null

$randomBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($randomBytes)
$password = [Convert]::ToBase64String($randomBytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
[Array]::Clear($randomBytes, 0, $randomBytes.Length)

$passwordEnvironmentName = "PASSVAULT_GENERATED_KEYSTORE_PASSWORD"
[Environment]::SetEnvironmentVariable($passwordEnvironmentName, $password, "Process")

try {
    & keytool -genkeypair `
        -keystore $keystorePath `
        -storetype JKS `
        -storepass:env $passwordEnvironmentName `
        -keypass:env $passwordEnvironmentName `
        -alias $Alias `
        -keyalg RSA `
        -keysize 4096 `
        -sigalg SHA256withRSA `
        -validity 36500 `
        -dname $DistinguishedName `
        -noprompt

    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed to generate the Android release keystore."
    }

    & keytool -exportcert `
        -rfc `
        -keystore $keystorePath `
        -storepass:env $passwordEnvironmentName `
        -alias $Alias `
        -file $certificatePath

    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed to export the public signing certificate."
    }

    $certificateDetails = & keytool -printcert -file $certificatePath
    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed to inspect the public signing certificate."
    }

    $sha256Line = $certificateDetails | Where-Object { $_ -match "SHA256:" } | Select-Object -First 1
    $sha1Line = $certificateDetails | Where-Object { $_ -match "SHA1:" } | Select-Object -First 1
    $sha256 = ($sha256Line -replace ".*SHA256:\s*", "").Trim()
    $sha1 = ($sha1Line -replace ".*SHA1:\s*", "").Trim()

    if ([string]::IsNullOrWhiteSpace($sha256)) {
        throw "Could not determine the signing certificate SHA-256 fingerprint."
    }

    $credentials = @"
PASSVAULT ANDROID RELEASE SIGNING MATERIAL
==========================================

Created (UTC): $([DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ"))
Keystore: passvault-android-release.jks
Keystore type: JKS
Key algorithm: RSA 4096
Signature algorithm: SHA256withRSA
Alias: $Alias
Keystore password: $password
Key password: $password
Certificate subject: $DistinguishedName
Certificate validity: 36,500 days
SHA-256 fingerprint: $sha256
SHA-1 fingerprint: $sha1

KEEP THIS ENTIRE DIRECTORY PRIVATE. Losing this key can prevent future updates
outside Google Play App Signing. Copy it to at least two encrypted, offline
backups and store the password in a trusted password manager.
"@

    $keystoreBase64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keystorePath))
    $githubSecrets = @"
Configure these GitHub Actions repository secrets:

KEYSTORE_BASE64=$keystoreBase64
KEYSTORE_PASSWORD=$password
KEY_ALIAS=$Alias
KEY_PASSWORD=$password

Never commit this file or paste these values into issues, logs, or workflow files.
"@

    [IO.File]::WriteAllText($credentialsPath, $credentials, [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText($githubSecretsPath, $githubSecrets, [Text.UTF8Encoding]::new($false))

    Write-Host "Android signing material created in: $resolvedOutput"
    Write-Host "The directory is excluded from Git. Store an encrypted backup now."
} finally {
    [Environment]::SetEnvironmentVariable($passwordEnvironmentName, $null, "Process")
    $password = $null
}
