[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ArchivePath,

    [Parameter(Mandatory = $true)]
    [string]$OutputRoot
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "A promoted Windows app image must be restored on Windows."
}

$archive = Get-Item -LiteralPath $ArchivePath -ErrorAction Stop
if (-not $archive.PSIsContainer -and
    -not ($archive.Attributes -band [IO.FileAttributes]::ReparsePoint) -and
    $archive.Length -gt 0) {
    # The validated archive is a regular, non-empty file.
} else {
    throw "ArchivePath must be a real, non-empty file."
}

$output = [IO.Path]::GetFullPath($OutputRoot)
if (Test-Path -LiteralPath $output) {
    $outputItem = Get-Item -LiteralPath $output -ErrorAction Stop
    if (-not $outputItem.PSIsContainer -or
        ($outputItem.Attributes -band [IO.FileAttributes]::ReparsePoint) -or
        @(Get-ChildItem -LiteralPath $output -Force).Count -ne 0) {
        throw "OutputRoot must be a real, empty directory."
    }
} else {
    [IO.Directory]::CreateDirectory($output) | Out-Null
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead($archive.FullName)
try {
    if ($zip.Entries.Count -eq 0 -or $zip.Entries.Count -gt 20000) {
        throw "The promoted Windows app-image archive has an invalid entry count."
    }
    $seen = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase
    )
    [long]$totalBytes = 0
    foreach ($entry in $zip.Entries) {
        $name = $entry.FullName
        if ([string]::IsNullOrEmpty($name) -or $name.Contains("\") -or
            $name.Contains([char]0) -or $name.StartsWith("/")) {
            throw "The promoted Windows app-image archive contains an unsafe path."
        }
        $segments = @($name.TrimEnd("/").Split("/"))
        if ($segments.Count -eq 0 -or $segments[0] -cne "PassVault" -or
            $segments.Where({
                [string]::IsNullOrEmpty($_) -or $_ -in ".", ".." -or
                $_.Contains(":") -or $_.EndsWith(".") -or $_.EndsWith(" ")
            }).Count -ne 0) {
            throw "The promoted Windows app-image archive escapes its PassVault root."
        }
        $normalizedName = $segments -join "/"
        if (-not $seen.Add($normalizedName)) {
            throw "The promoted Windows app-image archive contains a duplicate path."
        }
        $unixType = (($entry.ExternalAttributes -shr 16) -band 0xF000)
        if ($unixType -eq 0xA000) {
            throw "The promoted Windows app-image archive contains a symbolic link."
        }
        $isDirectory = $name.EndsWith("/")
        if (-not $isDirectory) {
            if ($entry.Length -lt 0 -or $entry.Length -gt 2GB) {
                throw "The promoted Windows app-image archive contains an invalid file size."
            }
            $totalBytes += $entry.Length
            if ($totalBytes -gt 4GB) {
                throw "The promoted Windows app-image archive is too large when expanded."
            }
        }

        # PowerShell otherwise coerces the array to one space-delimited string
        # instead of selecting Path.Combine(string[]).
        $relativePath = [IO.Path]::Combine([string[]]$segments)
        $destination = [IO.Path]::GetFullPath([IO.Path]::Combine($output, $relativePath))
        $requiredPrefix = $output.TrimEnd([IO.Path]::DirectorySeparatorChar) +
            [IO.Path]::DirectorySeparatorChar
        if (-not $destination.StartsWith($requiredPrefix, [StringComparison]::OrdinalIgnoreCase)) {
            throw "The promoted Windows app-image archive contains a traversal path."
        }
        if ($isDirectory) {
            [IO.Directory]::CreateDirectory($destination) | Out-Null
            continue
        }
        [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($destination)) | Out-Null
        $sourceStream = $entry.Open()
        try {
            $destinationStream = [IO.File]::Open(
                $destination,
                [IO.FileMode]::CreateNew,
                [IO.FileAccess]::Write,
                [IO.FileShare]::None
            )
            try {
                $sourceStream.CopyTo($destinationStream)
            } finally {
                $destinationStream.Dispose()
            }
        } finally {
            $sourceStream.Dispose()
        }
    }
} finally {
    $zip.Dispose()
}

$topLevel = @(Get-ChildItem -LiteralPath $output -Force)
$appImage = Join-Path $output "PassVault"
$launchers = @(Get-ChildItem -LiteralPath $appImage -Recurse -File -Filter PassVault.exe)
$reparsePoints = @(Get-ChildItem -LiteralPath $appImage -Recurse -Force |
    Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint })
if ($topLevel.Count -ne 1 -or -not $topLevel[0].PSIsContainer -or
    $topLevel[0].Name -cne "PassVault" -or $launchers.Count -ne 1 -or
    $reparsePoints.Count -ne 0) {
    throw "The restored Windows app image has an unexpected shape."
}

Write-Output "Restored the receipt-verified Windows app image without rebuilding it."
