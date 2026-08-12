[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$DestinationRoot,

    [Parameter(Mandatory = $true)]
    [string]$CatalogPath,

    [Parameter(Mandatory = $true)]
    [string]$SignedOutputRoot,

    [Parameter(Mandatory = $true)]
    [string]$ExpectedPublisherName,

    [string]$SignToolPath
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "SignPath Windows results must be applied on Windows."
}
if ([string]::IsNullOrWhiteSpace($ExpectedPublisherName) -or
    $ExpectedPublisherName.Length -gt 200 -or
    $ExpectedPublisherName.Contains("`n") -or $ExpectedPublisherName.Contains("`r")) {
    throw "ExpectedPublisherName must be bounded single-line text."
}

$destination = Get-Item -LiteralPath $DestinationRoot -ErrorAction Stop
$signedOutput = Get-Item -LiteralPath $SignedOutputRoot -ErrorAction Stop
foreach ($directory in @($destination, $signedOutput)) {
    if (-not $directory.PSIsContainer -or
        ($directory.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "DestinationRoot and SignedOutputRoot must be real directories."
    }
}
$catalog = Get-Item -LiteralPath $CatalogPath -ErrorAction Stop
if ($catalog.PSIsContainer -or ($catalog.Attributes -band [IO.FileAttributes]::ReparsePoint) -or
    $catalog.Length -le 0 -or $catalog.Length -gt 1024 * 1024) {
    throw "CatalogPath must be a bounded regular file."
}
$signedFilesRoot = Get-Item -LiteralPath (Join-Path $signedOutput.FullName "files") -ErrorAction Stop
if (-not $signedFilesRoot.PSIsContainer -or
    ($signedFilesRoot.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "The signed artifact must contain one real files directory."
}
$unexpectedTopLevel = @(Get-ChildItem -LiteralPath $signedOutput.FullName -Force |
    Where-Object { $_.FullName -ne $signedFilesRoot.FullName })
if ($unexpectedTopLevel.Count -ne 0) {
    throw "The signed artifact contains an unexpected top-level entry."
}
$reparsePoints = @(Get-ChildItem -LiteralPath $signedFilesRoot.FullName -Recurse -Force |
    Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint })
if ($reparsePoints.Count -ne 0) {
    throw "The signed artifact contains a reparse point."
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
    throw "signtool.exe is required for independent SignPath result verification."
}

$catalogPaths = [Collections.Generic.List[string]]::new()
$catalogSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($line in [IO.File]::ReadAllLines($catalog.FullName, [Text.Encoding]::UTF8)) {
    if ([string]::IsNullOrWhiteSpace($line) -or -not $line.StartsWith("./") -or
        $line.Contains("\") -or $line.Contains("`n") -or $line.Contains("`r")) {
        throw "The signing catalog contains an invalid relative path."
    }
    $relative = $line.Substring(2)
    if ([string]::IsNullOrWhiteSpace($relative) -or [IO.Path]::IsPathRooted($relative) -or
        $relative.Split('/') -contains ".." -or -not $catalogSet.Add($relative)) {
        throw "The signing catalog contains an escaping or duplicate path."
    }
    $catalogPaths.Add($relative)
}

$signedByRelativePath = @{}
foreach ($signedFile in @(Get-ChildItem -LiteralPath $signedFilesRoot.FullName -Recurse -File)) {
    $relative = [IO.Path]::GetRelativePath($signedFilesRoot.FullName, $signedFile.FullName).Replace('\', '/')
    if ($signedByRelativePath.ContainsKey($relative)) {
        throw "The signed artifact contains duplicate file paths."
    }
    $signedByRelativePath[$relative] = $signedFile
}
$actualPaths = @($signedByRelativePath.Keys | Sort-Object)
$expectedPaths = @($catalogPaths | Sort-Object)
if (($actualPaths -join "`n") -cne ($expectedPaths -join "`n")) {
    throw "The signed artifact file set differs from the immutable request catalog."
}

$validated = [Collections.Generic.List[object]]::new()
$destinationPrefix = "$($destination.FullName)$([IO.Path]::DirectorySeparatorChar)"
foreach ($relative in $expectedPaths) {
    $signedFile = $signedByRelativePath[$relative]
    & $SignToolPath verify /pa /all /v $signedFile.FullName | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "signtool rejected a SignPath result: $relative"
    }
    $signature = Get-AuthenticodeSignature -LiteralPath $signedFile.FullName
    if ($signature.Status -ne [Management.Automation.SignatureStatus]::Valid -or
        -not $signature.SignerCertificate -or -not $signature.TimeStamperCertificate) {
        throw "A SignPath result is not valid and timestamped: $relative"
    }
    $publisherName = $signature.SignerCertificate.GetNameInfo(
        [Security.Cryptography.X509Certificates.X509NameType]::SimpleName,
        $false
    )
    if ($publisherName -cne $ExpectedPublisherName) {
        throw "A SignPath result does not use the expected publisher: $relative"
    }

    $targetPath = [IO.Path]::GetFullPath((Join-Path $destination.FullName $relative))
    if (-not $targetPath.StartsWith($destinationPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "A signing target escapes DestinationRoot."
    }
    $target = Get-Item -LiteralPath $targetPath -ErrorAction Stop
    if ($target.PSIsContainer -or ($target.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "A signing target is not a regular file: $relative"
    }
    $originalSignature = Get-AuthenticodeSignature -LiteralPath $target.FullName
    if ($originalSignature.Status -ne [Management.Automation.SignatureStatus]::NotSigned) {
        throw "A destination changed after the signing request was created: $relative"
    }
    $validated.Add([pscustomobject]@{
        Relative = $relative
        Signed = $signedFile.FullName
        Target = $target.FullName
    })
}

$backupRoot = Join-Path $env:RUNNER_TEMP "passvault-signpath-rollback-$([Guid]::NewGuid().ToString('N'))"
[IO.Directory]::CreateDirectory($backupRoot) | Out-Null
$replaced = [Collections.Generic.List[object]]::new()
try {
    foreach ($item in $validated) {
        $backup = Join-Path $backupRoot $item.Relative
        [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($backup)) | Out-Null
        [IO.File]::Copy($item.Target, $backup, $false)
        $temporary = "$($item.Target).passvault-signing-$([Guid]::NewGuid().ToString('N')).tmp"
        [IO.File]::Copy($item.Signed, $temporary, $false)
        try {
            [IO.File]::Replace($temporary, $item.Target, $null, $true)
        } finally {
            Remove-Item -LiteralPath $temporary -Force -ErrorAction SilentlyContinue
        }
        $replaced.Add([pscustomobject]@{ Target = $item.Target; Backup = $backup })
    }
    foreach ($item in $validated) {
        $signedHash = (Get-FileHash -LiteralPath $item.Signed -Algorithm SHA256).Hash
        $targetHash = (Get-FileHash -LiteralPath $item.Target -Algorithm SHA256).Hash
        if ($targetHash -ne $signedHash) {
            throw "A replaced signing target differs from the verified SignPath result: $($item.Relative)"
        }
    }
} catch {
    $replacementFailure = $_
    $rollbackFailures = [Collections.Generic.List[string]]::new()
    $rollbackItems = $replaced.ToArray()
    [Array]::Reverse($rollbackItems)
    foreach ($item in $rollbackItems) {
        try {
            [IO.File]::Copy($item.Backup, $item.Target, $true)
        } catch {
            $rollbackFailures.Add($item.Target)
        }
    }
    if ($rollbackFailures.Count -ne 0) {
        throw "SignPath apply failed and rollback also failed for: $($rollbackFailures -join ', ')"
    }
    throw $replacementFailure
} finally {
    Remove-Item -LiteralPath $backupRoot -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Output "Applied and independently verified $($validated.Count) SignPath signature(s)."
