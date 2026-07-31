#!/usr/bin/env pwsh
# Fix Compose Multiplatform resources in designsystem module

$files = @(
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\SecureTextField.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\CopyButton.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\CredentialCard.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\EmptyState.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\ErrorState.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\LoadingState.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\NavigationComponents.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\PasswordStrengthIndicator.kt",
    "E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\tokens\Motion.kt"
)

function Fix-Imports {
    param($file)
    $content = Get-Content $file -Raw
    # Remove the old Res imports
    $content = $content -replace "import com\.passvault\.core\.designsystem\.generated\.resources\.Res\s*\r?\n", ""
    $content = $content -replace "import com\.passvault\.core\.designsystem\.generated\.resources\.[a-z_]+\s*\r?\n", ""
    # Fix stringResource calls to use proper string IDs
    $content = $content -replace 'stringResource\(Res\.string\.', 'stringResource("'
    $content = $content -replace '\)', '")'
    # Fix stateDescription -> stateDescription="..." (Compose 1.11.1 API change)
    $content = $content -replace 'stateDescription = stringResource\("([^"]+)"\)', 'stateDescription = "$1"'
    $content = $content -replace 'stateDescription = stringResource\("([^"]+)"\)', 'stateDescription = "$1"'
    Set-Content $file $content
}

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Fixing: $file"
        Fix-Imports $file
    }
}

Write-Host "Done!"
