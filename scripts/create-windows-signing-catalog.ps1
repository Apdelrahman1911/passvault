[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RuntimePath,

    [Parameter(Mandatory = $true)]
    [string]$CatalogPath,

    [string[]]$Extensions = @(".exe", ".dll")
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $IsWindows) {
    throw "The Windows signing catalog must be created on Windows."
}

$allowedExtensionSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($extension in $Extensions) {
    $normalizedExtension = $extension.ToLowerInvariant()
    if ($normalizedExtension -notin ".exe", ".dll", ".msi" -or
        -not $allowedExtensionSet.Add($normalizedExtension)) {
        throw "Extensions must be a unique subset of .exe, .dll, and .msi."
    }
}
if ($allowedExtensionSet.Count -eq 0) {
    throw "At least one signing-catalog extension is required."
}

$root = Get-Item -LiteralPath $RuntimePath -ErrorAction Stop
if (-not $root.PSIsContainer -or ($root.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
    throw "RuntimePath must be a real directory."
}
$catalogFullPath = [IO.Path]::GetFullPath($CatalogPath)
$catalogParent = [IO.Path]::GetDirectoryName($catalogFullPath)
if ($catalogParent -ne $root.FullName) {
    throw "CatalogPath must be a direct child of RuntimePath so catalog entries remain bounded."
}
if (Test-Path -LiteralPath $catalogFullPath) {
    throw "Refusing to overwrite an existing signing catalog."
}

$nativeFiles = @(Get-ChildItem -LiteralPath $root.FullName -Recurse -File |
    Where-Object { $allowedExtensionSet.Contains($_.Extension) } |
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
            return $stream.ReadByte() -eq 0x4d -and $stream.ReadByte() -eq 0x5a
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
        if (-not $signature.SignerCertificate -or -not $signature.TimeStamperCertificate) {
            throw "An existing native signature lacks a signer or timestamp: $($nativeFile.FullName)"
        }
        $preserved++
    } elseif ($signature.Status -eq [Management.Automation.SignatureStatus]::NotSigned) {
        $relative = [IO.Path]::GetRelativePath($root.FullName, $nativeFile.FullName)
        if ($relative.StartsWith("..") -or [IO.Path]::IsPathRooted($relative) -or
            $relative.Contains("`n") -or $relative.Contains("`r")) {
            throw "An unsigned native path escapes the runtime root."
        }
        $unsigned.Add("./$($relative.Replace('\', '/'))")
    } else {
        throw "Refusing to replace an invalid existing signature ($($signature.Status)): $($nativeFile.FullName)"
    }
}
if ($unsigned.Count -eq 0) {
    throw "No unsigned native files were found; refusing an empty remote-signing request."
}

[IO.File]::WriteAllLines($catalogFullPath, $unsigned.ToArray(), [Text.UTF8Encoding]::new($false))
Write-Output "Cataloged $($unsigned.Count) unsigned native file(s); preserved $preserved valid vendor signature(s)."
