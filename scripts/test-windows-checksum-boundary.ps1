[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

# Provider-boundary fixture only: neither an executable nor a real signing
# provider is invoked. The platform flag is scoped to this disposable pwsh
# process so the same production script can be challenged on non-Windows CI.
$originalIsWindows = $IsWindows
$root = $null
$tempParent = $null
$rootCreated = $false
$signatureDoubleState = [pscustomobject]@{ Fault = ""; Calls = 0 }
$primaryError = $null
$cleanupErrors = [Collections.Generic.List[Management.Automation.ErrorRecord]]::new()

function Assert-NoReparseAncestors {
    param([Parameter(Mandatory = $true)][string]$LiteralPath)
    # GetTempPath/Get-Item may preserve a trailing separator while Parent.FullName
    # does not. Normalize it without trimming the separator from a filesystem root.
    $full = [IO.Path]::TrimEndingDirectorySeparator([IO.Path]::GetFullPath($LiteralPath))
    $current = $full
    while ($null -ne $current) {
        $item = Get-Item -LiteralPath $current -Force -ErrorAction Stop
        if (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
            throw "Synthetic fixture path traverses a reparse point."
        }
        $parent = [IO.Directory]::GetParent($current)
        $current = if ($null -eq $parent) { $null } else { $parent.FullName }
    }
    return $full
}

function Remove-SyntheticFixtureTree {
    param(
        [Parameter(Mandatory = $true)][string]$LiteralPath,
        [Parameter(Mandatory = $true)][string]$ExpectedParent
    )
    $canonicalParent = Assert-NoReparseAncestors -LiteralPath $ExpectedParent
    $canonicalRoot = Assert-NoReparseAncestors -LiteralPath $LiteralPath
    $rootItem = Get-Item -LiteralPath $canonicalRoot -Force -ErrorAction Stop
    if (-not $rootItem.PSIsContainer -or
        $rootItem.Parent.FullName -cne $canonicalParent -or
        $rootItem.Name -cnotmatch '^passvault-checksum-fixture-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$') {
        throw "Synthetic fixture cleanup is outside its exact canonical temp parent."
    }
    # Enumerate one level at a time, never recurse through a reparse directory.
    # Validate the entire tree before deleting anything; then delete explicit
    # leaf files and empty directories only, with no recursive Remove-Item call.
    $pending = [Collections.Generic.Stack[string]]::new()
    $entries = [Collections.Generic.List[IO.FileSystemInfo]]::new()
    $pending.Push($canonicalRoot)
    while ($pending.Count -gt 0) {
        $directory = $pending.Pop()
        $null = Assert-NoReparseAncestors -LiteralPath $directory
        foreach ($item in @(Get-ChildItem -LiteralPath $directory -Force -ErrorAction Stop)) {
            if (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
                throw "Synthetic fixture cleanup refuses nested reparse points."
            }
            $relative = [IO.Path]::GetRelativePath($canonicalRoot, $item.FullName)
            if ([IO.Path]::IsPathRooted($relative) -or $relative -eq ".." -or
                $relative.StartsWith(".." + [IO.Path]::DirectorySeparatorChar)) {
                throw "Synthetic fixture enumeration escaped its canonical root."
            }
            $entries.Add($item)
            if ($item.PSIsContainer) { $pending.Push($item.FullName) }
        }
    }
    foreach ($item in @($entries | Sort-Object { $_.FullName.Length } -Descending)) {
        $null = Assert-NoReparseAncestors -LiteralPath $item.FullName
        if ($item -is [IO.DirectoryInfo]) {
            [IO.Directory]::Delete($item.FullName, $false)
        } else {
            [IO.File]::Delete($item.FullName)
        }
    }
    $null = Assert-NoReparseAncestors -LiteralPath $canonicalRoot
    [IO.Directory]::Delete($canonicalRoot, $false)
}

function Complete-FixtureResult {
    param(
        [AllowNull()][Management.Automation.ErrorRecord]$PrimaryError,
        [Management.Automation.ErrorRecord[]]$CleanupErrors = @()
    )
    # Report every cleanup problem without replacing the original case failure.
    # Explicit Continue prevents the fixture's Stop preference from masking it.
    if ($null -ne $PrimaryError) {
        foreach ($secondary in $CleanupErrors) {
            Write-Error -ErrorRecord $secondary -ErrorAction Continue
        }
        throw $PrimaryError
    }
    if ($CleanupErrors.Count -gt 0) {
        for ($index = 1; $index -lt $CleanupErrors.Count; $index++) {
            Write-Error -ErrorRecord $CleanupErrors[$index] -ErrorAction Continue
        }
        throw $CleanupErrors[0]
    }
}

function Get-AuthenticodeSignature {
    param([string]$LiteralPath)
    # Mutate the enclosing fixture's object, not the caller's script scope: the
    # production helper is a separate script when it invokes this provider double.
    $signatureDoubleState.Calls++
    if (-not (Test-Path -LiteralPath $LiteralPath -PathType Leaf)) {
        throw "Missing synthetic signature target."
    }
    $thumbprint = if ($signatureDoubleState.Fault -eq "different-signer" -and
        [IO.Path]::GetFileName($LiteralPath) -ceq "PassVault.exe") { "OTHER" } else { "FIXTURE" }
    [pscustomobject]@{
        Status = if ($signatureDoubleState.Fault -eq "invalid-signature") {
            [Management.Automation.SignatureStatus]::NotSigned
        } else { [Management.Automation.SignatureStatus]::Valid }
        SignerCertificate = [pscustomobject]@{ Thumbprint = $thumbprint }
        TimeStamperCertificate = if ($signatureDoubleState.Fault -eq "no-timestamp") { $null } else { [pscustomobject]@{} }
    }
}

$passed = 0
$lifecyclePassed = 0
try {
    Set-Variable -Name IsWindows -Value $true -Force
    $tempParent = Assert-NoReparseAncestors -LiteralPath ([IO.Path]::GetTempPath())
    $root = Join-Path $tempParent ("passvault-checksum-fixture-" + [Guid]::NewGuid())
    $null = New-Item -ItemType Directory -Path $root -ErrorAction Stop
    $rootCreated = $true

    # Cleanup must accept the same exact parent with/without the optional final
    # separator, but must still reject a different existing parent before deletion.
    $cleanupProbe = Join-Path $root ("passvault-checksum-fixture-" + [Guid]::NewGuid())
    $null = New-Item -ItemType Directory -Path $cleanupProbe -ErrorAction Stop
    $separator = [IO.Path]::DirectorySeparatorChar
    Remove-SyntheticFixtureTree -LiteralPath ($cleanupProbe + $separator) -ExpectedParent ($root + $separator)
    if (Test-Path -LiteralPath $cleanupProbe) { throw "Canonical cleanup probe was not removed." }
    $lifecyclePassed++
    Write-Output "PASS Windows fixture lifecycle: canonical trailing separators"

    $null = New-Item -ItemType Directory -Path $cleanupProbe -ErrorAction Stop
    $wrongParentRejected = $false
    try {
        Remove-SyntheticFixtureTree -LiteralPath $cleanupProbe -ExpectedParent $tempParent
    } catch {
        if ($_.Exception.Message -cne "Synthetic fixture cleanup is outside its exact canonical temp parent.") { throw }
        $wrongParentRejected = $true
    }
    if (-not $wrongParentRejected -or -not (Test-Path -LiteralPath $cleanupProbe -PathType Container)) {
        throw "Wrong-parent cleanup probe did not fail before deletion."
    }
    Remove-SyntheticFixtureTree -LiteralPath $cleanupProbe -ExpectedParent $root
    $lifecyclePassed++
    Write-Output "PASS Windows fixture lifecycle: wrong parent preserved"

    foreach ($withPrimary in @($true, $false)) {
        $syntheticPrimary = if ($withPrimary) {
            [Management.Automation.ErrorRecord]::new(
                [Exception]::new("synthetic primary failure"), "SyntheticPrimary",
                [Management.Automation.ErrorCategory]::InvalidOperation, $null)
        } else { $null }
        $syntheticCleanup = @(
            [Management.Automation.ErrorRecord]::new(
                [Exception]::new("synthetic first cleanup failure"), "SyntheticFirstCleanup",
                [Management.Automation.ErrorCategory]::InvalidOperation, $null),
            [Management.Automation.ErrorRecord]::new(
                [Exception]::new("synthetic second cleanup failure"), "SyntheticSecondCleanup",
                [Management.Automation.ErrorCategory]::InvalidOperation, $null)
        )
        $expected = if ($withPrimary) { "synthetic primary failure" } else { "synthetic first cleanup failure" }
        $observed = $null
        try {
            Complete-FixtureResult -PrimaryError $syntheticPrimary -CleanupErrors $syntheticCleanup 2>$null
        } catch {
            $observed = $_.Exception.Message
        }
        if ($observed -cne $expected) { throw "Fixture result lost its original failure." }
        $lifecyclePassed++
        Write-Output "PASS Windows fixture lifecycle: error precedence with primary=$withPrimary"
    }

    foreach ($case in @("valid", "parent", "missing-launcher", "duplicate-launcher", "different-signer", "invalid-signature", "no-timestamp")) {
        $caseRoot = Join-Path $root $case
        $image = Join-Path $caseRoot "app/PassVault"
        $bridge = Join-Path $image "app/resources/windows-x64"
        $null = New-Item -ItemType Directory -Path $bridge -Force
        $library = Join-Path $bridge "passvault_biometric.dll"
        $manifest = Join-Path $bridge "bridge.properties"
        [IO.File]::WriteAllText($library, "inert fixture, not executable")
        if ($case -ne "missing-launcher") {
            [IO.File]::WriteAllText((Join-Path $image "PassVault.exe"), "inert launcher, not executable")
        }
        if ($case -eq "duplicate-launcher") {
            [IO.File]::WriteAllText((Join-Path $bridge "PassVault.exe"), "duplicate inert launcher")
        }
        $original = "abi=1`nplatform=windows-x64`nlibrary=passvault_biometric.dll`nintegrity=sha256-and-authenticode`nsha256=" + ("0" * 64) + "`n"
        [IO.File]::WriteAllText($manifest, $original, [Text.UTF8Encoding]::new($false))
        $signatureDoubleState.Fault = $case
        $signatureDoubleState.Calls = 0
        $succeeded = $false
        try {
            $runtime = if ($case -eq "parent") { Join-Path $caseRoot "app" } else { $image }
            & (Join-Path $PSScriptRoot "update-desktop-biometric-checksum.ps1") -RuntimePath $runtime | Out-Null
            $succeeded = $true
        } catch {
            if ($case -eq "valid") { throw }
        }
        if ($succeeded -ne ($case -eq "valid")) { throw "Unexpected checksum result: $case" }
        if ($succeeded) {
            $digest = (Get-FileHash -LiteralPath $library -Algorithm SHA256).Hash.ToLowerInvariant()
            if (-not [IO.File]::ReadAllText($manifest).Contains("sha256=$digest") -or $signatureDoubleState.Calls -ne 2) {
                throw "Valid checksum fixture did not verify both synthetic signers."
            }
        } elseif ([IO.File]::ReadAllText($manifest) -cne $original) {
            throw "Rejected checksum fixture changed its manifest: $case"
        }
        $passed++
        Write-Output "PASS Windows checksum provider boundary: $case"
    }
} catch {
    $primaryError = $_
} finally {
    try {
        Set-Variable -Name IsWindows -Value $originalIsWindows -Force
    } catch {
        $cleanupErrors.Add($_)
    }
    try {
        if ($rootCreated) {
            Remove-SyntheticFixtureTree -LiteralPath $root -ExpectedParent $tempParent
        }
    } catch {
        $cleanupErrors.Add($_)
    }
}
Complete-FixtureResult -PrimaryError $primaryError -CleanupErrors ($cleanupErrors.ToArray())
Write-Output "$passed inert Windows checksum boundary cases and $lifecyclePassed fixture lifecycle controls passed; no real signature or Windows artifact approval."
