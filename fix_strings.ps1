$file = 'E:\passapp\kimi\passvault\core\designsystem\src\commonMain\kotlin\com\passvault\core\designsystem\components\CredentialCard.kt'
$content = Get-Content $file -Raw

# Fix stringResource("string_name") calls
$content = $content -replace 'stringResource\(\"credential_card_unfavorite\"\)', 'stringResource(Res.string.credential_card_unfavorite)'
$content = $content -replace 'stringResource\(\"credential_card_favorite\"\)', 'stringResource(Res.string.credential_card_favorite)'
$content = $content -replace 'stringResource\(\"action_edit\"\)', 'stringResource(Res.string.action_edit)'
$content = $content -replace 'stringResource\(\"action_delete\"\)', 'stringResource(Res.string.action_delete)'
$content = $content -replace 'stringResource\(\"credential_card_copy_username\"\)', 'stringResource(Res.string.credential_card_copy_username)'
$content = $content -replace 'stringResource\(\"credential_card_copy_email\"\)', 'stringResource(Res.string.credential_card_copy_email)'
$content = $content -replace 'stringResource\(\"credential_card_copy_password\"\)', 'stringResource(Res.string.credential_card_copy_password)'
$content = $content -replace 'stringResource\(\"credential_card_open_url\"\)', 'stringResource(Res.string.credential_card_open_url)'
$content = $content -replace 'stringResource\(\"credential_card_last_used\"\)', 'stringResource(Res.string.credential_card_last_used)'

# Fix hardcoded strings
$content = $content -replace '"More options"', 'stringResource(Res.string.action_more)'
$content = $content -replace 'label = "Username"', 'label = stringResource(Res.string.credential_card_copy_username)'
$content = $content -replace 'label = "Email"', 'label = stringResource(Res.string.credential_card_copy_email)'

# Fix relative time strings with proper resource calls
$content = $content -replace 'diff < 1\.days -> "Today"', 'diff < 1.days -> stringResource(Res.string.credential_card_last_used).replace("%1\$s", "Today")'
$content = $content -replace 'diff < 2\.days -> "Yesterday"', 'diff < 2.days -> stringResource(Res.string.credential_card_last_used).replace("%1\$s", "Yesterday")'
$content = $content -replace 'diff < 7\.days -> "\${diff\.inWholeDays} days ago"', 'diff < 7.days -> stringResource(Res.string.credential_card_last_used).replace("%1\$s", "${diff.inWholeDays} days ago")'
$content = $content -replace 'diff < 30\.days -> "\${diff\.inWholeDays / 7} weeks ago"', 'diff < 30.days -> stringResource(Res.string.credential_card_last_used).replace("%1\$s", "${diff.inWholeDays / 7} weeks ago")'
$content = $content -replace 'else -> "\${diff\.inWholeDays / 30} months ago"', 'else -> stringResource(Res.string.credential_card_last_used).replace("%1\$s", "${diff.inWholeDays / 30} months ago")'

$content | Set-Content $file -Encoding utf8
Write-Host "Fix completed"
