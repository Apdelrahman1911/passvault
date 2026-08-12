[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$BuildToolsRoot,

    [Parameter(Mandatory)]
    [ValidatePattern('^[A-Za-z0-9._-]+$')]
    [string]$ToolName
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not (Test-Path -LiteralPath $BuildToolsRoot -PathType Container)) {
    throw "Android SDK build-tools directory was not found: $BuildToolsRoot"
}

$candidates = foreach ($directory in Get-ChildItem -LiteralPath $BuildToolsRoot -Directory) {
    $versionMatch = [regex]::Match(
        $directory.Name,
        '^(?<major>[0-9]+)\.(?<minor>[0-9]+)\.(?<patch>[0-9]+)(?:-(?<qualifier>[A-Za-z]+)(?<qualifierNumber>[0-9]*))?$'
    )
    if (-not $versionMatch.Success) {
        continue
    }

    $major = 0L
    $minor = 0L
    $patch = 0L
    $qualifierNumber = 0L
    if (
        -not [long]::TryParse($versionMatch.Groups["major"].Value, [ref]$major) -or
        -not [long]::TryParse($versionMatch.Groups["minor"].Value, [ref]$minor) -or
        -not [long]::TryParse($versionMatch.Groups["patch"].Value, [ref]$patch)
    ) {
        continue
    }
    if (
        $versionMatch.Groups["qualifierNumber"].Value.Length -gt 0 -and
        -not [long]::TryParse(
            $versionMatch.Groups["qualifierNumber"].Value,
            [ref]$qualifierNumber
        )
    ) {
        continue
    }

    $toolPath = Join-Path $directory.FullName $ToolName
    if (-not (Test-Path -LiteralPath $toolPath -PathType Leaf)) {
        continue
    }

    [PSCustomObject]@{
        Major = $major
        Minor = $minor
        Patch = $patch
        Stable = if ($versionMatch.Groups["qualifier"].Success) { 0 } else { 1 }
        Qualifier = $versionMatch.Groups["qualifier"].Value.ToLowerInvariant()
        QualifierNumber = $qualifierNumber
        DirectoryName = $directory.Name
        ToolPath = $toolPath
    }
}

$selected = $candidates |
    Sort-Object -Property `
        @{ Expression = "Major"; Descending = $true },
        @{ Expression = "Minor"; Descending = $true },
        @{ Expression = "Patch"; Descending = $true },
        @{ Expression = "Stable"; Descending = $true },
        @{ Expression = "Qualifier"; Descending = $true },
        @{ Expression = "QualifierNumber"; Descending = $true },
        @{ Expression = "DirectoryName"; Descending = $true } |
    Select-Object -First 1

if (-not $selected) {
    throw "No valid $ToolName was found below $BuildToolsRoot."
}

Write-Output $selected.ToolPath
