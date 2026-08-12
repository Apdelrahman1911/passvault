[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,

    [Parameter(Mandatory = $true)]
    [string]$CatalogPath,

    [Parameter(Mandatory = $true)]
    [string]$ArchivePath
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "SignPath Windows requests must be prepared on Windows."
}

$root = Get-Item -LiteralPath $SourceRoot -ErrorAction Stop
if (-not $root.PSIsContainer -or ($root.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "SourceRoot must be a real directory."
}
$catalog = Get-Item -LiteralPath $CatalogPath -ErrorAction Stop
if ($catalog.PSIsContainer -or ($catalog.Attributes -band [IO.FileAttributes]::ReparsePoint) -or
    $catalog.Length -le 0 -or $catalog.Length -gt 1024 * 1024) {
    throw "CatalogPath must be a bounded regular file."
}

$archiveFullPath = [IO.Path]::GetFullPath($ArchivePath)
if (Test-Path -LiteralPath $archiveFullPath) {
    throw "Refusing to overwrite an existing SignPath request archive."
}
$archiveParent = [IO.Path]::GetDirectoryName($archiveFullPath)
if ([string]::IsNullOrWhiteSpace($archiveParent)) {
    throw "ArchivePath must include a parent directory."
}
[IO.Directory]::CreateDirectory($archiveParent) | Out-Null

$entries = [Collections.Generic.List[object]]::new()
$seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($line in [IO.File]::ReadAllLines($catalog.FullName, [Text.Encoding]::UTF8)) {
    if ([string]::IsNullOrWhiteSpace($line) -or -not $line.StartsWith("./") -or
        $line.Contains("\") -or $line.Contains("`n") -or $line.Contains("`r")) {
        throw "The signing catalog contains an invalid relative path."
    }
    $relative = $line.Substring(2)
    if ([string]::IsNullOrWhiteSpace($relative) -or [IO.Path]::IsPathRooted($relative) -or
        $relative.Split('/') -contains ".." -or -not $seen.Add($relative)) {
        throw "The signing catalog contains an escaping or duplicate path."
    }
    $candidatePath = [IO.Path]::GetFullPath((Join-Path $root.FullName $relative))
    $rootPrefix = "$($root.FullName)$([IO.Path]::DirectorySeparatorChar)"
    if (-not $candidatePath.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "A signing-catalog entry escapes SourceRoot."
    }
    $candidate = Get-Item -LiteralPath $candidatePath -ErrorAction Stop
    if ($candidate.PSIsContainer -or ($candidate.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "A signing-catalog entry is not a regular file: $relative"
    }
    $signature = Get-AuthenticodeSignature -LiteralPath $candidate.FullName
    if ($signature.Status -ne [Management.Automation.SignatureStatus]::NotSigned) {
        throw "SignPath requests may contain only unsigned files: $relative"
    }
    $entries.Add([pscustomobject]@{ Relative = $relative; File = $candidate })
}
if ($entries.Count -eq 0) {
    throw "The SignPath request catalog is empty."
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::Open($archiveFullPath, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($entry in $entries) {
        $entryName = "files/$($entry.Relative)"
        [void][IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $archive,
            $entry.File.FullName,
            $entryName,
            [IO.Compression.CompressionLevel]::Optimal
        )
    }
} catch {
    Remove-Item -LiteralPath $archiveFullPath -Force -ErrorAction SilentlyContinue
    throw
} finally {
    if ($archive) {
        $archive.Dispose()
    }
}

$created = Get-Item -LiteralPath $archiveFullPath -ErrorAction Stop
if ($created.Length -le 0) {
    throw "The SignPath request archive is empty."
}
Write-Output "Prepared a SignPath request containing $($entries.Count) unsigned file(s)."
