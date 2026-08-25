#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

./scripts/test-private-release-validator.sh >/dev/null
./scripts/validate-workflow-action-pins.sh >/dev/null
./scripts/verify-pentest-scope.sh >/dev/null
./scripts/verify-dependabot-coverage.sh >/dev/null
./scripts/verify-shell-library-contract.sh >/dev/null

export PUBLISHER_NAME="PassVault test publisher"
export COPYRIGHT_HOLDER="PassVault test contributors"
export SUPPORT_EMAIL="support@passvault.test"
export SECURITY_EMAIL="security@passvault.test"
export PRIVACY_POLICY_URL="https://passvault.test/privacy"
export SUPPORT_URL="https://passvault.test/support"
export PROJECT_URL="https://passvault.test/project"

version="$(awk -F= '$1 == "VERSION_NAME" { print $2 }' version.properties)"

test "$(tr -d '\r\n' < release/android/passvault-upload-alias.txt)" = "passvault-upload"
expected_android_fingerprint="$(tr -cd '0-9A-Fa-f' < release/android/passvault-release-cert.sha256 | \
    tr '[:lower:]' '[:upper:]')"
test "$expected_android_fingerprint" = \
    "7D4D1120B1D19F5BB942E06C600F2B6481E5E682475223FA4E7DC7B27CDB1037"
actual_android_fingerprint="$(keytool -printcert -file release/android/passvault-release-cert.pem 2>/dev/null | \
    awk -F'SHA256:' '/SHA256:/ { gsub(/[^0-9A-Fa-f]/, "", $2); print toupper($2) }')"
test "$actual_android_fingerprint" = "$expected_android_fingerprint"
wrong_version="0.0.0"
if [[ "$version" == "$wrong_version" ]]; then
    wrong_version="9.9.9"
fi

./scripts/validate-release-metadata.sh "$version" >/dev/null
grep -Fq "version.properties must define \$key exactly once" scripts/validate-release-metadata.sh
grep -Fq "10#\$version_code > 2100000000" scripts/validate-release-metadata.sh
grep -Fq 'Time.iso8601(value)' scripts/validate-release-metadata.sh

if ./scripts/validate-release-metadata.sh "$wrong_version" >/dev/null 2>&1; then
    echo "Mismatched release version was accepted." >&2
    exit 1
fi

temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-release-test.XXXXXX")"
cleanup() {
    case "$temporary_root" in
        "${TMPDIR:-/tmp}"/passvault-release-test.*)
            rm -rf -- "$temporary_root"
            ;;
        *)
            echo "Refusing to remove unexpected test path: $temporary_root" >&2
            ;;
    esac
}
trap cleanup EXIT

fake_security_bin="$temporary_root/fake-security-bin"
mkdir -p "$fake_security_bin"
cat > "$fake_security_bin/security" <<'FAKE_SECURITY'
#!/usr/bin/env bash
exit 1
FAKE_SECURITY
chmod 700 "$fake_security_bin/security"
failed_keychain_capture="$temporary_root/original-user-keychains.txt"
if PATH="$fake_security_bin:$PATH" bash -euo pipefail -c '
    source scripts/lib/macos-keychain.sh
    passvault_capture_user_keychains "$1"
' -- "$failed_keychain_capture" >/dev/null 2>&1; then
    echo "A failed keychain-list command was accepted." >&2
    exit 1
fi
test ! -s "$failed_keychain_capture"

if find release/store-assets -name .DS_Store -type f -print -quit | grep -q .; then
    echo "Store assets contain forbidden .DS_Store metadata." >&2
    exit 1
fi
git check-ignore -q release/store-assets/.DS_Store
hidden_store_assets="$temporary_root/hidden-store-assets"
mkdir -p "$hidden_store_assets"
: > "$hidden_store_assets/.DS_Store"
if ruby scripts/validate-mobile-store-assets.rb "$hidden_store_assets" >/dev/null 2>&1; then
    echo "The Store asset validator accepted .DS_Store metadata." >&2
    exit 1
fi

candidate_source_repository="$temporary_root/candidate-source"
git init -q "$candidate_source_repository"
git -C "$candidate_source_repository" config user.name "PassVault release test"
git -C "$candidate_source_repository" config user.email "release-test@passvault.test"
printf 'base\n' > "$candidate_source_repository/app.txt"
git -C "$candidate_source_repository" add app.txt
git -C "$candidate_source_repository" commit -qm "Create shared base"
candidate_base="$(git -C "$candidate_source_repository" rev-parse HEAD)"

git -C "$candidate_source_repository" checkout -qb reviewed-main
printf 'reviewed\n' > "$candidate_source_repository/app.txt"
git -C "$candidate_source_repository" commit -qam "Create reviewed main tree"
approved_main_commit="$(git -C "$candidate_source_repository" rev-parse HEAD)"
approved_main_tree="$(git -C "$candidate_source_repository" rev-parse "$approved_main_commit^{tree}")"

git -C "$candidate_source_repository" checkout -qb rebased-testing "$candidate_base"
printf 'reviewed\n' > "$candidate_source_repository/app.txt"
git -C "$candidate_source_repository" commit -qam "Create protected rebase equivalent"
rebased_candidate_commit="$(git -C "$candidate_source_repository" rev-parse HEAD)"
test "$rebased_candidate_commit" != "$approved_main_commit"
test "$(git -C "$candidate_source_repository" rev-parse "$rebased_candidate_commit^{tree}")" = \
    "$approved_main_tree"
if git -C "$candidate_source_repository" merge-base --is-ancestor \
    "$approved_main_commit" "$rebased_candidate_commit"; then
    echo "The protected-rebase fixture unexpectedly contains main." >&2
    exit 1
fi
(
    cd "$candidate_source_repository"
    "$repository_root/scripts/validate-testing-candidate-source.sh" \
        "$rebased_candidate_commit" "$approved_main_commit"
) >/dev/null

git -C "$candidate_source_repository" checkout -qb descendant-candidate "$approved_main_commit"
printf 'reviewed follow-up\n' > "$candidate_source_repository/follow-up.txt"
git -C "$candidate_source_repository" add follow-up.txt
git -C "$candidate_source_repository" commit -qm "Create main descendant"
descendant_candidate_commit="$(git -C "$candidate_source_repository" rev-parse HEAD)"
(
    cd "$candidate_source_repository"
    "$repository_root/scripts/validate-testing-candidate-source.sh" \
        "$descendant_candidate_commit" "$approved_main_commit"
) >/dev/null

git -C "$candidate_source_repository" checkout -qb divergent-candidate "$candidate_base"
printf 'different\n' > "$candidate_source_repository/app.txt"
git -C "$candidate_source_repository" commit -qam "Create divergent candidate"
divergent_candidate_commit="$(git -C "$candidate_source_repository" rev-parse HEAD)"
if (
    cd "$candidate_source_repository"
    "$repository_root/scripts/validate-testing-candidate-source.sh" \
        "$divergent_candidate_commit" "$approved_main_commit"
) >/dev/null 2>&1; then
    echo "A candidate with different content was accepted." >&2
    exit 1
fi

orphan_candidate_commit="$(
    printf 'Create unrelated equivalent tree\n' |
        git -C "$candidate_source_repository" commit-tree "$approved_main_tree"
)"
if (
    cd "$candidate_source_repository"
    "$repository_root/scripts/validate-testing-candidate-source.sh" \
        "$orphan_candidate_commit" "$approved_main_commit"
) >/dev/null 2>&1; then
    echo "An unrelated candidate with a matching tree was accepted." >&2
    exit 1
fi
if (
    cd "$candidate_source_repository"
    "$repository_root/scripts/validate-testing-candidate-source.sh" \
        deadbeef "$approved_main_commit"
) >/dev/null 2>&1; then
    echo "A malformed candidate SHA was accepted." >&2
    exit 1
fi

dotenv_fixture="$temporary_root/values.env"
printf '%s\n' \
    '# parsed as data, never evaluated' \
    'PLAIN=value' \
    'SINGLE='"'"'single quoted'"'"'' \
    'DOUBLE="double quoted"' \
    'WITH_EQUALS=left=right' > "$dotenv_fixture"
ruby -I "$repository_root/scripts" -r lib/dotenv -e '
  values = PassVault::Dotenv.load(ARGV.fetch(0))
  abort unless values == {
    "PLAIN" => "value",
    "SINGLE" => "single quoted",
    "DOUBLE" => "double quoted",
    "WITH_EQUALS" => "left=right",
  }
' "$dotenv_fixture"
printf '%s\n' 'BROKEN="unterminated' > "$temporary_root/invalid-values.env"
if ruby -I "$repository_root/scripts" -r lib/dotenv -e '
    PassVault::Dotenv.load(ARGV.fetch(0))
  ' "$temporary_root/invalid-values.env" >/dev/null 2>&1; then
    echo "An unterminated quoted dotenv value was accepted." >&2
    exit 1
fi
printf '%s\n' 'DUPLICATE=first' 'DUPLICATE=second' > "$temporary_root/duplicate-values.env"
if ruby -I "$repository_root/scripts" -r lib/dotenv -e '
    PassVault::Dotenv.load(ARGV.fetch(0))
  ' "$temporary_root/duplicate-values.env" >/dev/null 2>&1; then
    echo "A duplicate dotenv key was accepted." >&2
    exit 1
fi
ruby -e 'File.binwrite(ARGV.fetch(0), "A" * (1024 * 1024 + 1))' \
    "$temporary_root/oversized-values.env"
if ruby -I "$repository_root/scripts" -r lib/dotenv -e '
    PassVault::Dotenv.load(ARGV.fetch(0))
  ' "$temporary_root/oversized-values.env" >/dev/null 2>&1; then
    echo "An oversized dotenv input was accepted." >&2
    exit 1
fi

ruby -I "$repository_root/scripts" -r lib/app_store_configuration -e '
  valid = {
    "ASC_KEY_ID" => "ABC123DEFG",
    "ASC_ISSUER_ID" => "01234567-89ab-cdef-0123-456789abcdef",
    "IOS_BUNDLE_ID" => "com.passvault.ios",
    "APP_STORE_APP_ID" => "12345678",
  }
  abort unless PassVault::AppStoreConfiguration.identifiers_valid?(valid)
  abort if PassVault::AppStoreConfiguration.identifiers_valid?(valid.merge("IOS_BUNDLE_ID" => "com.other.app"))
  abort if PassVault::AppStoreConfiguration.identifiers_valid?(valid.merge("APP_STORE_APP_ID" => "1234567"))
  abort unless PassVault::AppStoreConfiguration.external_group_name_valid?("PassVault External")
  abort if PassVault::AppStoreConfiguration.external_group_name_valid?(" PassVault External")
  abort if PassVault::AppStoreConfiguration.external_group_name_valid?("PassVault\nExternal")
  abort if PassVault::AppStoreConfiguration.external_group_name_valid?("A" * 101)
'

private_path_root="$temporary_root/private-root"
mkdir -m 700 "$private_path_root" "$private_path_root/nested" "$temporary_root/outside-directory"
printf 'fixture\n' > "$private_path_root/key.p8"
printf 'outside\n' > "$temporary_root/outside.p8"
printf 'outside nested\n' > "$temporary_root/outside-directory/key.p8"
ln -s "$temporary_root/outside.p8" "$private_path_root/symlink.p8"
ln -s "$temporary_root/outside-directory" "$private_path_root/directory-symlink"
ruby -I "$repository_root/scripts" -r lib/private_path -e '
  root, inside, outside, symlink, nested, directory_symlink, escaped_file = ARGV
  abort unless PassVault::PrivatePath.regular_file_within?(inside, root)
  abort if PassVault::PrivatePath.regular_file_within?(outside, root)
  abort if PassVault::PrivatePath.regular_file_within?(symlink, root)
  abort unless PassVault::PrivatePath.directory_within?(nested, root)
  abort if PassVault::PrivatePath.directory_within?(directory_symlink, root)
  abort if PassVault::PrivatePath.regular_file_within?(escaped_file, root)
' "$private_path_root" "$private_path_root/key.p8" \
    "$temporary_root/outside.p8" "$private_path_root/symlink.p8" \
    "$private_path_root/nested" "$private_path_root/directory-symlink" \
    "$private_path_root/directory-symlink/key.p8"

if env -u PRIVATE_RUNTIME PASSVAULT_ASC_CONFIGURATION_SOURCE=environment \
    ruby scripts/check-app-store-live-version.rb 1.0.0 1 >/dev/null 2>&1; then
    echo "The App Store live check accepted a missing private runtime root." >&2
    exit 1
fi
if env -u PRIVATE_RUNTIME PASSVAULT_ASC_CONFIGURATION_SOURCE=environment \
    ruby scripts/manage-testflight-public-link.rb --status --version 1.0.0 \
    --build-number 1 >/dev/null 2>&1; then
    echo "The TestFlight status check accepted a missing private runtime root." >&2
    exit 1
fi

metadata_source="$temporary_root/metadata-source"
metadata_output="$temporary_root/metadata-output"
mkdir -p "$metadata_source" "$metadata_output"
for metadata_name in \
    privacy-ar.md privacy-en.md release-notes-ar.md release-notes-en.md \
    store-description-ar.md store-description-en.md \
    store-metadata-ar.env store-metadata-en.env; do
    cp "scripts/testdata/mobile-store/$metadata_name" "$metadata_source/$metadata_name"
done
ruby scripts/create-store-metadata-archive.rb \
    "$metadata_source" "$temporary_root/store-metadata.tar.gz" >/dev/null
ruby scripts/extract-store-metadata-archive.rb \
    "$temporary_root/store-metadata.tar.gz" "$metadata_output" >/dev/null
test "$(find "$metadata_output" -type f | wc -l | tr -d ' ')" = 8
ruby -I scripts -r lib/store_metadata_archive -rrubygems/package -rzlib \
    - "$temporary_root/store-metadata.tar.gz" <<'RUBY'
archive_path = ARGV.fetch(0)
entries = []
Zlib::GzipReader.open(archive_path) do |gzip|
  Gem::Package::TarReader.new(gzip) do |tar|
    tar.each { |entry| entries << [entry.full_name, entry.header.typeflag, entry.file?] }
  end
end
expected = PassVault::StoreMetadataArchive::EXPECTED_FILES.map { |name| [name, "0", true] }
abort("The store-metadata creator emitted a non-portable tar header") unless entries == expected
RUBY

metadata_symlink_source="$temporary_root/metadata-symlink-source"
cp -R "$metadata_source" "$metadata_symlink_source"
rm "$metadata_symlink_source/privacy-en.md"
ln -s "$metadata_source/privacy-en.md" "$metadata_symlink_source/privacy-en.md"
if ruby scripts/create-store-metadata-archive.rb "$metadata_symlink_source" \
    "$temporary_root/symlink-metadata.tar.gz" >/dev/null 2>&1; then
    echo "A symlinked store-metadata source was archived." >&2
    exit 1
fi
test ! -e "$temporary_root/symlink-metadata.tar.gz"

ruby -rrubygems/package -rzlib -e '
  Zlib::GzipWriter.open(ARGV.fetch(0)) do |gzip|
    Gem::Package::TarWriter.new(gzip) do |tar|
      contents = "unsafe\n"
      tar.add_file_simple("../outside.txt", 0o600, contents.bytesize) { |io| io.write(contents) }
    end
  end
' "$temporary_root/unsafe-metadata.tar.gz"
unsafe_output="$temporary_root/unsafe-output"
mkdir "$unsafe_output"
if ruby scripts/extract-store-metadata-archive.rb \
    "$temporary_root/unsafe-metadata.tar.gz" "$unsafe_output" >/dev/null 2>&1; then
    echo "A path-traversing store-metadata archive was accepted." >&2
    exit 1
fi
test ! -e "$temporary_root/outside.txt"
test -z "$(find "$unsafe_output" -mindepth 1 -print -quit)"

invalid_utf8_source="$temporary_root/invalid-utf8-source"
invalid_utf8_output="$temporary_root/invalid-utf8-output"
cp -R "$metadata_source" "$invalid_utf8_source"
printf '\377\376\n' > "$invalid_utf8_source/privacy-en.md"
tar -czf "$temporary_root/invalid-utf8-metadata.tar.gz" -C "$invalid_utf8_source" \
    privacy-ar.md privacy-en.md release-notes-ar.md release-notes-en.md \
    store-description-ar.md store-description-en.md \
    store-metadata-ar.env store-metadata-en.env
mkdir "$invalid_utf8_output"
if ruby scripts/extract-store-metadata-archive.rb \
    "$temporary_root/invalid-utf8-metadata.tar.gz" "$invalid_utf8_output" >/dev/null 2>&1; then
    echo "A store-metadata archive containing invalid UTF-8 was accepted." >&2
    exit 1
fi
test -z "$(find "$invalid_utf8_output" -mindepth 1 -print -quit)"

synthetic_assets="$temporary_root/synthetic-store-assets"
ruby -rzlib -rfileutils -e '
  root = ARGV.fetch(0)
  def chunk(type, data)
    [data.bytesize].pack("N") + type + data + [Zlib.crc32(type + data)].pack("N")
  end
  def png(width, height, color_type)
    channels = color_type == 6 ? 4 : 3
    row = "\0".b + ("\0".b * (width * channels))
    image = row * height
    "\x89PNG\r\n\x1a\n".b +
      chunk("IHDR", [width, height, 8, color_type, 0, 0, 0].pack("NNCCCCC")) +
      chunk("IDAT", Zlib::Deflate.deflate(image, Zlib::BEST_SPEED)) +
      chunk("IEND", "".b)
  end
  %w[en-US ar].each do |locale|
    image_root = File.join(root, "android", locale, "images")
    screenshots = File.join(image_root, "phoneScreenshots")
    FileUtils.mkdir_p(screenshots)
    File.binwrite(File.join(image_root, "icon.png"), png(512, 512, 6))
    File.binwrite(File.join(image_root, "featureGraphic.png"), png(1024, 500, 2))
    4.times do |index|
      File.binwrite(File.join(screenshots, format("%02d.png", index)), png(1080, 1920, 2))
    end
  end
  %w[en-US ar-SA].each do |locale|
    locale_root = File.join(root, "ios", locale)
    FileUtils.mkdir_p(locale_root)
    File.binwrite(File.join(locale_root, "iphone.png"), png(1260, 2736, 2))
    File.binwrite(File.join(locale_root, "ipad.png"), png(2048, 2732, 2))
  end
' "$synthetic_assets"
ruby scripts/validate-mobile-store-assets.rb "$synthetic_assets" >/dev/null
synthetic_asset_symlink="$synthetic_assets/android/en-US/images/linked-icon.png"
ln -s icon.png "$synthetic_asset_symlink"
if ruby scripts/validate-mobile-store-assets.rb "$synthetic_assets" >/dev/null 2>&1; then
    echo "A symlink inside the public store-assets tree was accepted." >&2
    exit 1
fi
rm -f "$synthetic_asset_symlink"
corrupt_png="$synthetic_assets/android/en-US/images/featureGraphic.png"
cp "$corrupt_png" "$temporary_root/valid-feature-graphic.png"
ruby -rzlib -e '
  path = ARGV.fetch(0)
  contents = File.binread(path)
  offset = 8
  while offset < contents.bytesize
    length = contents.byteslice(offset, 4).unpack1("N")
    type = contents.byteslice(offset + 4, 4)
    if type == "IDAT"
      replacement = "not-a-zlib-stream".b
      chunk = [replacement.bytesize].pack("N") + type + replacement +
        [Zlib.crc32(type + replacement)].pack("N")
      contents[offset, 12 + length] = chunk
      File.binwrite(path, contents)
      exit
    end
    offset += 12 + length
  end
  abort("IDAT not found")
' "$corrupt_png"
if ruby scripts/validate-mobile-store-assets.rb "$synthetic_assets" >/dev/null 2>&1; then
    echo "A store PNG with invalid compressed image data was accepted." >&2
    exit 1
fi
cp "$temporary_root/valid-feature-graphic.png" "$corrupt_png"
ruby -e '
  path = ARGV.fetch(0)
  contents = File.binread(path)
  contents.setbyte(-1, contents.getbyte(-1) ^ 1)
  File.binwrite(path, contents)
' "$corrupt_png"
if ruby scripts/validate-mobile-store-assets.rb "$synthetic_assets" >/dev/null 2>&1; then
    echo "A store PNG with a corrupt chunk CRC was accepted." >&2
    exit 1
fi

play_csv_header='Email,Status,Expiration time,Invitation time,App permissions,Account permissions'
play_beta_email='beta-validator@passvault-test.iam.gserviceaccount.com'
play_production_email='production-validator@passvault-test.iam.gserviceaccount.com'
play_strict_csv="$temporary_root/play-strict.csv"
play_admin_csv="$temporary_root/play-admin.csv"
play_global_csv="$temporary_root/play-global.csv"
play_other_app_csv="$temporary_root/play-other-app.csv"
play_admin_permissions='CAN_VIEW_FINANCIAL_DATA;CAN_MANAGE_PERMISSIONS;CAN_REPLY_TO_REVIEWS;CAN_MANAGE_PUBLIC_APKS;CAN_MANAGE_TRACK_APKS;CAN_MANAGE_TRACK_USERS;CAN_MANAGE_PUBLIC_LISTING;CAN_MANAGE_DRAFT_APPS;CAN_MANAGE_ORDERS;CAN_MANAGE_APP_CONTENT;CAN_VIEW_NON_FINANCIAL_DATA;CAN_VIEW_APP_QUALITY;CAN_MANAGE_DEEPLINKS'
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:CAN_MANAGE_TRACK_APKS;CAN_VIEW_NON_FINANCIAL_DATA},\n' \
        "$play_beta_email"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:CAN_MANAGE_PUBLIC_APKS;CAN_MANAGE_PUBLIC_LISTING;CAN_VIEW_NON_FINANCIAL_DATA},\n' \
        "$play_production_email"
} > "$play_strict_csv"
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_beta_email" "$play_admin_permissions"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_production_email" "$play_admin_permissions"
} > "$play_admin_csv"
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},CAN_MANAGE_PUBLIC_APKS_GLOBAL\n' \
        "$play_beta_email" "$play_admin_permissions"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_production_email" "$play_admin_permissions"
} > "$play_global_csv"
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.other.app:%s},\n' "$play_beta_email" "$play_admin_permissions"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_production_email" "$play_admin_permissions"
} > "$play_other_app_csv"

play_validator_arguments=(
    --package com.passvault.android
    --beta-email "$play_beta_email"
    --production-email "$play_production_email"
)
ruby ./scripts/validate-play-service-accounts.rb --csv "$play_strict_csv" \
    "${play_validator_arguments[@]}" --mode STRICT_LEAST_PRIVILEGE >/dev/null
ruby ./scripts/validate-play-service-accounts.rb --csv "$play_admin_csv" \
    "${play_validator_arguments[@]}" --mode PASSVAULT_APP_ADMIN_ACCEPTED >/dev/null
if ruby ./scripts/validate-play-service-accounts.rb --csv "$play_admin_csv" \
    "${play_validator_arguments[@]}" --mode STRICT_LEAST_PRIVILEGE >/dev/null 2>&1; then
    echo "App-level Admin was accepted without the explicit security-exception mode." >&2
    exit 1
fi
if ruby ./scripts/validate-play-service-accounts.rb --csv "$play_global_csv" \
    "${play_validator_arguments[@]}" --mode PASSVAULT_APP_ADMIN_ACCEPTED >/dev/null 2>&1; then
    echo "Account/global Play permissions were accepted by the app-Admin exception." >&2
    exit 1
fi
if ruby ./scripts/validate-play-service-accounts.rb --csv "$play_other_app_csv" \
    "${play_validator_arguments[@]}" --mode PASSVAULT_APP_ADMIN_ACCEPTED >/dev/null 2>&1; then
    echo "Play access to another app was accepted by the PassVault-only exception." >&2
    exit 1
fi

ruby ./scripts/validate-google-play-readiness.rb >/dev/null
invalid_play_declaration="$temporary_root/invalid-play-app-content.json"
ruby -rjson -e '
  record = JSON.parse(File.read(ARGV.fetch(0), encoding: "UTF-8"))
  record.fetch("dataSafety")["collectsOrSharesRequiredUserData"] = true
  File.write(ARGV.fetch(1), JSON.pretty_generate(record))
' release/google-play/app-content.json "$invalid_play_declaration"
if ruby ./scripts/validate-google-play-readiness.rb "$invalid_play_declaration" >/dev/null 2>&1; then
    echo "A Play declaration contradicting the verified no-network behavior was accepted." >&2
    exit 1
fi

valid_asc_key="$temporary_root/valid-asc-key.p8"
traditional_asc_key="$temporary_root/valid-asc-key-legacy.pem"
invalid_asc_key="$temporary_root/invalid-asc-key.p8"
ruby -ropenssl -e '
  key = OpenSSL::PKey::EC.generate("prime256v1")
  File.binwrite(ARGV.fetch(0), key.to_pem)
' "$traditional_asc_key"
openssl pkcs8 -topk8 -nocrypt -in "$traditional_asc_key" -out "$valid_asc_key"
printf '%s\n' '-----BEGIN PRIVATE KEY-----' 'invalid' '-----END PRIVATE KEY-----' > "$invalid_asc_key"
./scripts/validate-app-store-connect-key.rb "$valid_asc_key" >/dev/null
if ./scripts/validate-app-store-connect-key.rb "$invalid_asc_key" >/dev/null 2>&1; then
    echo "Invalid App Store Connect key was accepted." >&2
    exit 1
fi

input_directory="$temporary_root/input"
output_directory="$temporary_root/output"
mkdir -p "$input_directory"

for extension in exe msi deb rpm; do
    printf 'PassVault %s test artifact\n' "$extension" >"$input_directory/PassVault-$version.$extension"
done
printf 'PassVault arm64 test artifact\n' >"$input_directory/PassVault-$version-macos-arm64.dmg"
printf 'PassVault x64 test artifact\n' >"$input_directory/PassVault-$version-macos-x64.dmg"

GITHUB_SHA=0123456789abcdef0123456789abcdef01234567 \
    ./scripts/prepare-release-assets.sh \
    "$input_directory" \
    "$output_directory" \
    "$version" test >/dev/null

if [[ "$(find "$output_directory" -maxdepth 1 -type f | wc -l | tr -d ' ')" != "12" ]]; then
    echo "Unexpected number of consolidated release files." >&2
    exit 1
fi

for legal_document in LICENSE.txt NOTICE.txt THIRD_PARTY_NOTICES.md; do
    if ! cmp -s "$legal_document" "$output_directory/$legal_document"; then
        echo "Release legal document was not copied exactly: $legal_document" >&2
        exit 1
    fi
done
if [[ "$(find THIRD_PARTY_LICENSES -mindepth 1 -maxdepth 1 -type f | wc -l | tr -d ' ')" != "24" ]]; then
    echo "The canonical third-party license set has an unexpected file count." >&2
    exit 1
fi
./scripts/verify-third-party-license-archive.sh \
    "$output_directory/THIRD_PARTY_LICENSES.zip" >/dev/null
license_archive_entries="$(unzip -Z1 "$output_directory/THIRD_PARTY_LICENSES.zip")"
for required_license in \
    chromium-zlib-646b7f56-LICENSE.txt \
    freetype-2.13.3-FTL.txt \
    freetype-2.13.3-LICENSE.txt \
    libffi-3.4.4-LICENSE.txt \
    spider-symbol-OFL-1.1.txt \
    wuffs-e3f919cc-LICENSE.txt; do
    grep -Fqx "THIRD_PARTY_LICENSES/$required_license" <<< "$license_archive_entries"
done

grep -Fq '0123456789abcdef0123456789abcdef01234567' \
    "$output_directory/RELEASE-MANIFEST.txt"
grep -Fq 'intentionally unsigned testing artifacts' "$output_directory/RELEASE-MANIFEST.txt"
grep -Fq 'Installed Android, iOS, and Desktop application images contain the canonical PassVault legal set.' \
    "$output_directory/RELEASE-MANIFEST.txt"
grep -Fq 'THIRD_PARTY_LICENSES.zip' \
    "$output_directory/SHA256SUMS.txt"
(
    cd "$output_directory"
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum --check SHA256SUMS.txt >/dev/null
    else
        shasum -a 256 --check SHA256SUMS.txt >/dev/null
    fi
)

installed_legal_fixture="$temporary_root/installed-legal"
mkdir "$installed_legal_fixture"
cp LICENSE.txt NOTICE.txt THIRD_PARTY_NOTICES.md "$installed_legal_fixture/"
cp -R THIRD_PARTY_LICENSES "$installed_legal_fixture/"
./scripts/verify-legal-notice-bundle.sh "$installed_legal_fixture" >/dev/null
printf '\nforged notice\n' >>"$installed_legal_fixture/NOTICE.txt"
if ./scripts/verify-legal-notice-bundle.sh "$installed_legal_fixture" >/dev/null 2>&1; then
    echo "A stale installed legal notice bundle was accepted." >&2
    exit 1
fi

tampered_license_archive="$temporary_root/tampered-license-archive.zip"
cp "$output_directory/THIRD_PARTY_LICENSES.zip" "$tampered_license_archive"
zip -q -d "$tampered_license_archive" \
    THIRD_PARTY_LICENSES/skiko-0.144.6-NOTICE.txt
if ./scripts/verify-third-party-license-archive.sh \
    "$tampered_license_archive" >/dev/null 2>&1; then
    echo "A third-party license archive with a missing notice was accepted." >&2
    exit 1
fi

symlink_license_root="$temporary_root/symlink-license-root"
symlink_license_fixture="$symlink_license_root/THIRD_PARTY_LICENSES"
symlink_license_archive="$temporary_root/symlink-license-archive.zip"
mkdir "$symlink_license_root"
cp -R THIRD_PARTY_LICENSES "$symlink_license_root/"
symlink_notice="$symlink_license_fixture/skiko-0.144.6-NOTICE.txt"
symlink_target="$(cat "$symlink_notice"; printf x)"
symlink_target="${symlink_target%x}"
rm "$symlink_notice"
ln -s "$symlink_target" "$symlink_notice"
(
    cd "$symlink_license_root"
    find THIRD_PARTY_LICENSES -mindepth 1 -maxdepth 1 \( -type f -o -type l \) -print |
        LC_ALL=C sort |
        zip -X -y -q "$symlink_license_archive" -@
)
if ./scripts/verify-third-party-license-archive.sh \
    "$symlink_license_archive" >/dev/null 2>&1; then
    echo "A third-party license archive containing a symlink was accepted." >&2
    exit 1
fi

wrong_version_input="$temporary_root/wrong-version-input"
cp -R "$input_directory" "$wrong_version_input"
mv "$wrong_version_input/PassVault-$version.exe" \
    "$wrong_version_input/PassVault-$wrong_version.exe"
if GITHUB_SHA=0123456789abcdef0123456789abcdef01234567 \
    ./scripts/prepare-release-assets.sh \
    "$wrong_version_input" "$temporary_root/wrong-version-output" \
    "$version" test >/dev/null 2>&1; then
    echo "A desktop artifact from another version was accepted." >&2
    exit 1
fi

embedded_version_input="$temporary_root/embedded-version-input"
cp -R "$input_directory" "$embedded_version_input"
mv "$embedded_version_input/PassVault-$version.exe" \
    "$embedded_version_input/PassVault-1${version}0.exe"
if GITHUB_SHA=0123456789abcdef0123456789abcdef01234567 \
    ./scripts/prepare-release-assets.sh \
    "$embedded_version_input" "$temporary_root/embedded-version-output" \
    "$version" test >/dev/null 2>&1; then
    echo "A desktop artifact whose numeric version merely contains the requested version was accepted." >&2
    exit 1
fi

extended_version_input="$temporary_root/extended-version-input"
cp -R "$input_directory" "$extended_version_input"
mv "$extended_version_input/PassVault-$version.exe" \
    "$extended_version_input/PassVault-$version.1.exe"
if GITHUB_SHA=0123456789abcdef0123456789abcdef01234567 \
    ./scripts/prepare-release-assets.sh \
    "$extended_version_input" "$temporary_root/extended-version-output" \
    "$version" test >/dev/null 2>&1; then
    echo "A desktop artifact with a longer dotted version was accepted." >&2
    exit 1
fi

mkdir "$temporary_root/real-output"
ln -s "$temporary_root/real-output" "$temporary_root/symlink-output"
if GITHUB_SHA=0123456789abcdef0123456789abcdef01234567 \
    ./scripts/prepare-release-assets.sh \
    "$input_directory" "$temporary_root/symlink-output" \
    "$version" test >/dev/null 2>&1; then
    echo "A symlinked release output directory was accepted." >&2
    exit 1
fi

if GITHUB_SHA=0123456789abcdef \
    ./scripts/prepare-release-assets.sh \
    "$input_directory" "$temporary_root/short-commit-output" \
    "$version" test >/dev/null 2>&1; then
    echo "A truncated release provenance commit was accepted." >&2
    exit 1
fi

rm -f "$input_directory/PassVault-$version.rpm"
if ./scripts/prepare-release-assets.sh \
    "$input_directory" \
    "$temporary_root/missing-output" \
    "$version" >/dev/null 2>&1; then
    echo "Missing required release artifact was accepted." >&2
    exit 1
fi

mobile_receipt_root="$temporary_root/mobile-receipts"
mkdir "$mobile_receipt_root"
printf 'signed-aab\n' > "$mobile_receipt_root/PassVault-$version-1000123.aab"
printf 'signed-apk\n' > "$mobile_receipt_root/PassVault-$version-1000123.apk"
printf 'mapping\n' > "$mobile_receipt_root/r8-mapping.txt"
RECEIPT_PLATFORM=android \
RECEIPT_VERSION="$version" \
RECEIPT_BUILD_NUMBER=1000123 \
RECEIPT_SOURCE_COMMIT=0123456789abcdef0123456789abcdef01234567 \
RECEIPT_SOURCE_TREE=89abcdef0123456789abcdef0123456789abcdef \
RECEIPT_IDENTIFIER=com.passvault.android \
RECEIPT_SIGNING_FINGERPRINT=7D4D1120B1D19F5BB942E06C600F2B6481E5E682475223FA4E7DC7B27CDB1037 \
RECEIPT_AAB_PATH="$mobile_receipt_root/PassVault-$version-1000123.aab" \
RECEIPT_APK_PATH="$mobile_receipt_root/PassVault-$version-1000123.apk" \
RECEIPT_R8_MAPPING_PATH="$mobile_receipt_root/r8-mapping.txt" \
    ruby scripts/create-mobile-artifact-receipt.rb \
      > "$mobile_receipt_root/android-artifact-receipt.json"
ruby scripts/validate-mobile-artifact-receipt.rb \
    "$mobile_receipt_root/android-artifact-receipt.json" android "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef "$mobile_receipt_root" >/dev/null
debug_android_receipt="$temporary_root/debug-android-artifact-receipt.json"
jq '.identifier = "com.passvault.android.debug"' \
    "$mobile_receipt_root/android-artifact-receipt.json" > "$debug_android_receipt"
if ruby scripts/validate-mobile-artifact-receipt.rb \
    "$debug_android_receipt" android "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null 2>&1; then
    echo "A Development Android identity was accepted in a Store artifact receipt." >&2
    exit 1
fi

printf 'signed-ipa\n' > "$mobile_receipt_root/PassVault-$version-1000123.ipa"
printf 'signed-archive\n' > "$mobile_receipt_root/PassVault-$version-1000123.xcarchive.zip"
printf 'link-map\n' > "$mobile_receipt_root/PassVault-LinkMap.txt"
RECEIPT_PLATFORM=ios \
RECEIPT_VERSION="$version" \
RECEIPT_BUILD_NUMBER=1000123 \
RECEIPT_SOURCE_COMMIT=0123456789abcdef0123456789abcdef01234567 \
RECEIPT_SOURCE_TREE=89abcdef0123456789abcdef0123456789abcdef \
RECEIPT_IDENTIFIER=com.passvault.ios \
RECEIPT_SIGNING_FINGERPRINT=0123456789ABCDEF0123456789ABCDEF01234567 \
RECEIPT_IPA_PATH="$mobile_receipt_root/PassVault-$version-1000123.ipa" \
RECEIPT_XCARCHIVE_PATH="$mobile_receipt_root/PassVault-$version-1000123.xcarchive.zip" \
RECEIPT_LINK_MAP_PATH="$mobile_receipt_root/PassVault-LinkMap.txt" \
    ruby scripts/create-mobile-artifact-receipt.rb \
      > "$mobile_receipt_root/ios-artifact-receipt.json"
ruby scripts/validate-mobile-artifact-receipt.rb \
    "$mobile_receipt_root/ios-artifact-receipt.json" ios "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef "$mobile_receipt_root" >/dev/null
debug_ios_receipt="$temporary_root/debug-ios-artifact-receipt.json"
jq '.identifier = "com.passvault.ios.debug"' \
    "$mobile_receipt_root/ios-artifact-receipt.json" > "$debug_ios_receipt"
if ruby scripts/validate-mobile-artifact-receipt.rb \
    "$debug_ios_receipt" ios "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null 2>&1; then
    echo "A Development iOS identity was accepted in a Store artifact receipt." >&2
    exit 1
fi

candidate_manifest="$temporary_root/candidate-manifest.json"
APP_VERSION="$version" \
BUILD_NUMBER=1000123 \
SOURCE_COMMIT=0123456789abcdef0123456789abcdef01234567 \
SOURCE_TREE=89abcdef0123456789abcdef0123456789abcdef \
CANDIDATE_TAG="v$version-rc.1000123" \
ANDROID_PACKAGE_NAME=com.passvault.android \
ANDROID_SIGNING_SHA256=7D4D1120B1D19F5BB942E06C600F2B6481E5E682475223FA4E7DC7B27CDB1037 \
ANDROID_ARTIFACT_RECEIPT_SHA256="$(sha256sum "$mobile_receipt_root/android-artifact-receipt.json" | awk '{ print $1 }')" \
ANDROID_INTERNAL_STATE=completed \
ANDROID_EXTERNAL_STATE=completed \
IOS_BUNDLE_ID=com.passvault.ios \
APP_STORE_APP_ID=1234567890 \
IOS_SIGNING_SHA1=0123456789ABCDEF0123456789ABCDEF01234567 \
IOS_ARTIFACT_RECEIPT_SHA256="$(sha256sum "$mobile_receipt_root/ios-artifact-receipt.json" | awk '{ print $1 }')" \
IOS_INTERNAL_STATE=completed \
IOS_EXTERNAL_STATE=submitted_for_review \
    ruby scripts/create-candidate-manifest.rb > "$candidate_manifest"
jq -e '.sourceTree == "89abcdef0123456789abcdef0123456789abcdef"' \
    "$candidate_manifest" >/dev/null
ruby scripts/validate-candidate-manifest.rb --allow-pending "$candidate_manifest" \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null
if ruby scripts/validate-candidate-manifest.rb --allow-pending "$candidate_manifest" \
    0123456789abcdef0123456789abcdef01234567 \
    fedcba9876543210fedcba9876543210fedcba98 >/dev/null 2>&1; then
    echo "A candidate manifest with the wrong Git tree was accepted." >&2
    exit 1
fi
if ruby scripts/validate-candidate-manifest.rb "$candidate_manifest" >/dev/null 2>&1; then
    echo "A candidate pending Apple Beta Review was accepted for production." >&2
    exit 1
fi
jq '.ios.external = "approved"' "$candidate_manifest" > "$temporary_root/readiness-manifest.json"
ruby scripts/validate-candidate-manifest.rb "$temporary_root/readiness-manifest.json" >/dev/null
ruby scripts/validate-candidate-artifact-provenance.rb \
    "$temporary_root/readiness-manifest.json" "$mobile_receipt_root" \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null

desktop_provenance_root="$temporary_root/desktop-provenance"
cp -R "$output_directory" "$desktop_provenance_root"
cp "$temporary_root/readiness-manifest.json" \
    "$mobile_receipt_root/android-artifact-receipt.json" \
    "$mobile_receipt_root/ios-artifact-receipt.json" \
    "$desktop_provenance_root/"
RELEASE_VERSION="$version" \
RELEASE_BUILD_NUMBER=1000123 \
RELEASE_CANDIDATE_TAG="v$version-rc.1000123" \
RELEASE_SOURCE_COMMIT=0123456789abcdef0123456789abcdef01234567 \
RELEASE_SOURCE_TREE=89abcdef0123456789abcdef0123456789abcdef \
    ruby scripts/create-desktop-release-provenance.rb "$desktop_provenance_root" \
      > "$desktop_provenance_root/release-provenance.json"
(
    cd "$desktop_provenance_root"
    # SHA256SUMS.txt is explicitly excluded from the input file set.
    # shellcheck disable=SC2094
    find . -maxdepth 1 -type f ! -name SHA256SUMS.txt -print |
        sed 's#^./##' | LC_ALL=C sort | xargs sha256sum > SHA256SUMS.txt
)
ruby scripts/validate-desktop-release-provenance.rb \
    "$desktop_provenance_root" "v$version-rc.1000123" "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null
extra_dmg_root="$temporary_root/desktop-provenance-extra-dmg"
cp -R "$desktop_provenance_root" "$extra_dmg_root"
printf 'unexpected DMG\n' > "$extra_dmg_root/PassVault-$version-macos-debug.dmg"
RELEASE_VERSION="$version" \
RELEASE_BUILD_NUMBER=1000123 \
RELEASE_CANDIDATE_TAG="v$version-rc.1000123" \
RELEASE_SOURCE_COMMIT=0123456789abcdef0123456789abcdef01234567 \
RELEASE_SOURCE_TREE=89abcdef0123456789abcdef0123456789abcdef \
    ruby scripts/create-desktop-release-provenance.rb "$extra_dmg_root" \
      > "$extra_dmg_root/release-provenance.json"
(
    cd "$extra_dmg_root"
    # SHA256SUMS.txt is explicitly excluded from the input file set.
    # shellcheck disable=SC2094
    find . -maxdepth 1 -type f ! -name SHA256SUMS.txt -print |
        sed 's#^./##' | LC_ALL=C sort | xargs sha256sum > SHA256SUMS.txt
)
if ruby scripts/validate-desktop-release-provenance.rb \
    "$extra_dmg_root" "v$version-rc.1000123" "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null 2>&1; then
    echo "An unexpected third DMG was accepted as a production release asset." >&2
    exit 1
fi
printf '\ntampered\n' >> "$desktop_provenance_root/NOTICE.txt"
if ruby scripts/validate-desktop-release-provenance.rb \
    "$desktop_provenance_root" "v$version-rc.1000123" "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef >/dev/null 2>&1; then
    echo "A desktop release file whose bytes differ from provenance was accepted." >&2
    exit 1
fi

cp "$mobile_receipt_root/PassVault-$version-1000123.aab" \
    "$mobile_receipt_root/PassVault-$version-1000123.aab.original"
printf 'tampered\n' >> "$mobile_receipt_root/PassVault-$version-1000123.aab"
if ruby scripts/validate-mobile-artifact-receipt.rb \
    "$mobile_receipt_root/android-artifact-receipt.json" android "$version" 1000123 \
    0123456789abcdef0123456789abcdef01234567 \
    89abcdef0123456789abcdef0123456789abcdef "$mobile_receipt_root" >/dev/null 2>&1; then
    echo "A mobile artifact whose bytes differ from its receipt was accepted." >&2
    exit 1
fi
mv "$mobile_receipt_root/PassVault-$version-1000123.aab.original" \
    "$mobile_receipt_root/PassVault-$version-1000123.aab"

unexpected_candidate_manifest="$temporary_root/unexpected-candidate-manifest.json"
jq '.unexpected = "hidden"' "$temporary_root/readiness-manifest.json" > "$unexpected_candidate_manifest"
if ruby scripts/validate-candidate-manifest.rb "$unexpected_candidate_manifest" >/dev/null 2>&1; then
    echo "A candidate manifest containing an unexpected field was accepted." >&2
    exit 1
fi

invalid_candidate_manifest="$temporary_root/invalid-candidate-manifest.json"
jq '.android.packageName = "com.passvault.android\nforged-output=value"' \
    "$temporary_root/readiness-manifest.json" > "$invalid_candidate_manifest"
if ruby scripts/validate-candidate-manifest.rb "$invalid_candidate_manifest" >/dev/null 2>&1; then
    echo "A candidate manifest with an unsafe package identifier was accepted." >&2
    exit 1
fi
debug_candidate_manifest="$temporary_root/debug-candidate-manifest.json"
jq '.android.packageName = "com.passvault.android.debug" | .ios.bundleId = "com.passvault.ios.debug"' \
    "$temporary_root/readiness-manifest.json" > "$debug_candidate_manifest"
if ruby scripts/validate-candidate-manifest.rb "$debug_candidate_manifest" >/dev/null 2>&1; then
    echo "Development application identities were accepted in candidate provenance." >&2
    exit 1
fi
invalid_app_id_manifest="$temporary_root/invalid-app-id-manifest.json"
jq '.ios.appStoreAppId = "1234567"' \
    "$temporary_root/readiness-manifest.json" > "$invalid_app_id_manifest"
if ruby scripts/validate-candidate-manifest.rb "$invalid_app_id_manifest" >/dev/null 2>&1; then
    echo "A candidate manifest with an invalid App Store app ID was accepted." >&2
    exit 1
fi

play_track_response="$temporary_root/play-track.json"
ruby -rjson -e '
  File.write(
    ARGV.fetch(0),
    JSON.generate(
      track: "production",
      releases: [{ versionCodes: ["1000123"], status: "completed" }],
    ),
  )
' "$play_track_response"
ruby scripts/verify-play-track-response.rb \
    "$play_track_response" production 1000123 >/dev/null
alpha_play_track_response="$temporary_root/play-alpha-track.json"
jq '.track = "alpha"' "$play_track_response" > "$alpha_play_track_response"
ruby scripts/verify-play-track-response.rb \
    "$alpha_play_track_response" alpha 1000123 >/dev/null
jq '.releases += [{ versionCodes: { forged: "1000123" }, status: "completed" }]' \
    "$play_track_response" > "$temporary_root/play-malformed-release.json"
if ruby scripts/verify-play-track-response.rb \
    "$temporary_root/play-malformed-release.json" production 1000123 >/dev/null 2>&1; then
    echo "A malformed Google Play release resource was ignored." >&2
    exit 1
fi
jq '.releases[0].versionCodes += ["1000123"]' \
    "$play_track_response" > "$temporary_root/play-duplicate-version-code.json"
if ruby scripts/verify-play-track-response.rb \
    "$temporary_root/play-duplicate-version-code.json" production 1000123 >/dev/null 2>&1; then
    echo "A duplicated version code inside one Google Play release was accepted." >&2
    exit 1
fi
jq '.releases[0].status = "draft"' "$play_track_response" > "$temporary_root/play-draft.json"
if ruby scripts/verify-play-track-response.rb \
    "$temporary_root/play-draft.json" production 1000123 >/dev/null 2>&1; then
    echo "A draft Google Play release was accepted as publicly active." >&2
    exit 1
fi
duplicate_play_release="$temporary_root/play-duplicate-release.json"
jq '.releases += [.releases[0]]' "$play_track_response" > "$duplicate_play_release"
if ruby scripts/verify-play-track-response.rb \
    "$duplicate_play_release" production 1000123 >/dev/null 2>&1; then
    echo "A Google Play build repeated across releases was accepted as unambiguous." >&2
    exit 1
fi
jq '.releases[0].status = "inProgress" | .releases[0].userFraction = 0' \
    "$play_track_response" > "$temporary_root/play-zero-rollout.json"
if ruby scripts/verify-play-track-response.rb \
    "$temporary_root/play-zero-rollout.json" production 1000123 >/dev/null 2>&1; then
    echo "A zero-user Google Play rollout was accepted as publicly active." >&2
    exit 1
fi
jq '.releases[0].status = "inProgress" | .releases[0].userFraction = 0.5' \
    "$play_track_response" > "$temporary_root/play-partial-rollout.json"
if ruby scripts/verify-play-track-response.rb \
    "$temporary_root/play-partial-rollout.json" production 1000123 >/dev/null 2>&1; then
    echo "A partial Google Play rollout was accepted as fully active." >&2
    exit 1
fi

app_store_versions_response="$temporary_root/app-store-versions.json"
app_store_build_response="$temporary_root/app-store-build.json"
ruby -rjson -e '
  File.write(
    ARGV.fetch(0),
    JSON.generate(
      data: [{
        type: "appStoreVersions",
        id: "version-resource-id",
        attributes: {
          platform: "IOS",
          versionString: "1.0.2",
          appVersionState: "READY_FOR_DISTRIBUTION",
          downloadable: true,
        },
      }],
    ),
  )
  File.write(
    ARGV.fetch(1),
    JSON.generate(
      data: {
        type: "builds",
        id: "build-resource-id",
        attributes: { version: "1000123", processingState: "VALID" },
      },
    ),
  )
' "$app_store_versions_response" "$app_store_build_response"
ruby scripts/verify-app-store-live-response.rb \
    "$app_store_versions_response" "$app_store_build_response" 1.0.2 1000123 >/dev/null
jq '.data += ["malformed"]' \
    "$app_store_versions_response" > "$temporary_root/app-store-malformed-version.json"
if ruby scripts/verify-app-store-live-response.rb \
    "$temporary_root/app-store-malformed-version.json" "$app_store_build_response" \
    1.0.2 1000123 >/dev/null 2>&1; then
    echo "A malformed App Store version resource was ignored." >&2
    exit 1
fi
jq '.data.id = ""' \
    "$app_store_build_response" > "$temporary_root/app-store-missing-build-id.json"
if ruby scripts/verify-app-store-live-response.rb \
    "$app_store_versions_response" "$temporary_root/app-store-missing-build-id.json" \
    1.0.2 1000123 >/dev/null 2>&1; then
    echo "An App Store build without a resource ID was accepted." >&2
    exit 1
fi
jq '.data[0].attributes.appVersionState = "PENDING_DEVELOPER_RELEASE"' \
    "$app_store_versions_response" > "$temporary_root/app-store-pending.json"
if ruby scripts/verify-app-store-live-response.rb \
    "$temporary_root/app-store-pending.json" "$app_store_build_response" \
    1.0.2 1000123 >/dev/null 2>&1; then
    echo "An unreleased App Store version was accepted as publicly live." >&2
    exit 1
fi
jq '.data.attributes.version = "1000999"' \
    "$app_store_build_response" > "$temporary_root/app-store-wrong-build.json"
if ruby scripts/verify-app-store-live-response.rb \
    "$app_store_versions_response" "$temporary_root/app-store-wrong-build.json" \
    1.0.2 1000123 >/dev/null 2>&1; then
    echo "An App Store version attached to the wrong build was accepted." >&2
    exit 1
fi

mobile_fixture="$repository_root/scripts/testdata/mobile-store"
mobile_output="$temporary_root/mobile-store"
./scripts/prepare-mobile-store-metadata.sh \
    "$mobile_fixture" "$mobile_output" "123" >/dev/null
test -s "$mobile_output/android/en-US/changelogs/123.txt"
test -s "$mobile_output/android/ar/full_description.txt"
test -s "$mobile_output/ios/en-US/description.txt"
test -s "$mobile_output/ios/ar-SA/release_notes.txt"
test -s "$mobile_output/ios/en-US/support_url.txt"
test -s "$mobile_output/ios/copyright.txt"
test -s "$mobile_output/testflight/en-US/what_to_test.txt"

for source_locale in en ar; do
    competitor_metadata_fixture="$temporary_root/competitor-mobile-store-$source_locale"
    cp -R "$mobile_fixture" "$competitor_metadata_fixture"
    printf '\nAvailable on Android too.\n' >> \
        "$competitor_metadata_fixture/store-description-$source_locale.md"
    if ./scripts/prepare-mobile-store-metadata.sh \
        "$competitor_metadata_fixture" \
        "$temporary_root/competitor-mobile-output-$source_locale" "123" \
        >/dev/null 2>&1; then
        echo "An $source_locale App Store description that mentions Android was accepted." >&2
        exit 1
    fi
done

legacy_metadata_fixture="$temporary_root/legacy-mobile-store"
cp -R "$mobile_fixture" "$legacy_metadata_fixture"
printf '\nFULL_DESCRIPTION_FILE=release/private/store-description-en.md\n' \
    >> "$legacy_metadata_fixture/store-metadata-en.env"
printf '\nFULL_DESCRIPTION_FILE=release/private/store-description-ar.md\n' \
    >> "$legacy_metadata_fixture/store-metadata-ar.env"
./scripts/prepare-mobile-store-metadata.sh \
    "$legacy_metadata_fixture" "$temporary_root/legacy-mobile-output" "123" >/dev/null
test -s "$temporary_root/legacy-mobile-output/android/en-US/full_description.txt"

invalid_legacy_metadata_fixture="$temporary_root/invalid-legacy-mobile-store"
cp -R "$mobile_fixture" "$invalid_legacy_metadata_fixture"
printf '\nFULL_DESCRIPTION_FILE=../outside.md\n' \
    >> "$invalid_legacy_metadata_fixture/store-metadata-en.env"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$invalid_legacy_metadata_fixture" "$temporary_root/invalid-legacy-mobile-output" "123" \
    >/dev/null 2>&1; then
    echo "An unsafe legacy store-description path was accepted." >&2
    exit 1
fi

if ./scripts/prepare-mobile-store-metadata.sh \
    "$mobile_fixture" "$temporary_root/oversized-version-code" "2100000001" \
    >/dev/null 2>&1; then
    echo "An Android version code above the platform maximum was accepted." >&2
    exit 1
fi

duplicate_metadata_fixture="$temporary_root/duplicate-metadata"
cp -R "$mobile_fixture" "$duplicate_metadata_fixture"
printf '\nSTORE_NAME=Duplicate\n' >> "$duplicate_metadata_fixture/store-metadata-en.env"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$duplicate_metadata_fixture" "$temporary_root/duplicate-metadata-output" "123" \
    >/dev/null 2>&1; then
    echo "Duplicate store metadata keys were accepted." >&2
    exit 1
fi

nonempty_mobile_output="$temporary_root/nonempty-mobile-output"
mkdir "$nonempty_mobile_output"
printf 'stale\n' > "$nonempty_mobile_output/stale.txt"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$mobile_fixture" "$nonempty_mobile_output" "123" >/dev/null 2>&1; then
    echo "A non-empty mobile metadata output directory was accepted." >&2
    exit 1
fi

overlong_description_fixture="$temporary_root/overlong-description"
cp -R "$mobile_fixture" "$overlong_description_fixture"
awk 'BEGIN { print "# Description"; print ""; for (i = 0; i < 4001; i++) printf "x"; print "" }' \
    > "$overlong_description_fixture/store-description-en.md"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$overlong_description_fixture" "$temporary_root/overlong-description-output" "123" \
    >/dev/null 2>&1; then
    echo "An overlong store description was accepted." >&2
    exit 1
fi

overlong_notes_fixture="$temporary_root/overlong-notes"
cp -R "$mobile_fixture" "$overlong_notes_fixture"
awk 'BEGIN { print "# Release notes"; print ""; for (i = 0; i < 501; i++) printf "x"; print "" }' \
    > "$overlong_notes_fixture/release-notes-en.md"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$overlong_notes_fixture" "$temporary_root/overlong-notes-output" "123" \
    >/dev/null 2>&1; then
    echo "Google Play release notes over 500 characters were accepted." >&2
    exit 1
fi

./scripts/validate-mobile-tester-files.sh \
    testflight "$mobile_fixture/testflight-external-testers.csv" >/dev/null
./scripts/validate-mobile-tester-files.sh \
    play "$mobile_fixture/play-closed-testers.txt" >/dev/null

empty_testers="$temporary_root/empty-testers.txt"
placeholder_testers="$temporary_root/placeholder-testers.txt"
empty_testflight="$temporary_root/empty-testflight.csv"
placeholder_testflight="$temporary_root/placeholder-testflight.csv"
missing_header_testflight="$temporary_root/missing-header-testflight.csv"
blank_rows_testflight="$temporary_root/blank-rows-testflight.csv"
duplicate_play_testers="$temporary_root/duplicate-play-testers.txt"
duplicate_testflight="$temporary_root/duplicate-testflight.csv"
: > "$empty_testers"
printf '%s\n' 'tester@example.invalid' > "$placeholder_testers"
printf '%s\n' 'first_name,last_name,email' > "$empty_testflight"
printf '%s\n' 'first_name,last_name,email' 'Test,User,tester@example.invalid' > "$placeholder_testflight"
printf '%s\n' 'Test,User,tester@example.com' > "$missing_header_testflight"
printf '%s\n' '' 'first_name,last_name,email' '' 'Test,User,tester@example.com' '' \
    > "$blank_rows_testflight"
printf '%s\n' 'tester@example.com' 'TESTER@example.com' > "$duplicate_play_testers"
printf '%s\n' 'first_name,last_name,email' \
    'Test,One,tester@example.com' 'Test,Two,TESTER@example.com' > "$duplicate_testflight"
if ./scripts/validate-mobile-tester-files.sh play "$empty_testers" >/dev/null 2>&1; then
    echo "An empty Play tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh play "$placeholder_testers" >/dev/null 2>&1; then
    echo "A placeholder Play tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh testflight "$empty_testflight" >/dev/null 2>&1; then
    echo "A header-only TestFlight tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh testflight "$placeholder_testflight" >/dev/null 2>&1; then
    echo "A placeholder TestFlight tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh testflight "$missing_header_testflight" >/dev/null 2>&1; then
    echo "A TestFlight tester list without the required header was accepted." >&2
    exit 1
fi
./scripts/validate-mobile-tester-files.sh testflight "$blank_rows_testflight" >/dev/null
if ./scripts/validate-mobile-tester-files.sh play "$duplicate_play_testers" >/dev/null 2>&1; then
    echo "A duplicate Play tester email was accepted." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh testflight "$duplicate_testflight" >/dev/null 2>&1; then
    echo "A duplicate TestFlight tester email was accepted." >&2
    exit 1
fi

exempt_plist="$temporary_root/exempt-Info.plist"
non_exempt_plist="$temporary_root/non-exempt-Info.plist"
invented_code_plist="$temporary_root/invented-code-Info.plist"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<plist version="1.0"><dict>' \
    '<key>ITSAppUsesNonExemptEncryption</key><false/>' \
    '</dict></plist>' > "$exempt_plist"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<plist version="1.0"><dict>' \
    '<key>ITSAppUsesNonExemptEncryption</key><true/>' \
    '</dict></plist>' > "$non_exempt_plist"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<plist version="1.0"><dict>' \
    '<key>ITSAppUsesNonExemptEncryption</key><false/>' \
    '<key>ITSEncryptionExportComplianceCode</key><string>invented-code</string>' \
    '</dict></plist>' > "$invented_code_plist"

EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=false \
    ./scripts/validate-ios-export-compliance.sh "$exempt_plist" >/dev/null
EXPORT_COMPLIANCE_STATUS=NON_EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=true \
    ./scripts/validate-ios-export-compliance.sh "$non_exempt_plist" >/dev/null
if EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=true \
    ./scripts/validate-ios-export-compliance.sh "$exempt_plist" >/dev/null 2>&1; then
    echo "France was allowed with the no-documentation export status." >&2
    exit 1
fi
if EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=false \
    ./scripts/validate-ios-export-compliance.sh "$non_exempt_plist" >/dev/null 2>&1; then
    echo "A non-exempt plist was accepted for the approved no-documentation state." >&2
    exit 1
fi
if EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=false \
    ./scripts/validate-ios-export-compliance.sh "$invented_code_plist" >/dev/null 2>&1; then
    echo "An invented Apple export-compliance code was accepted." >&2
    exit 1
fi

grep -Fq 'I_CONFIRM_REQUIRED_TESTING_COMPLETED' .github/workflows/mobile-store-release.yml
grep -Fq 'I_CONFIRM_REQUIRED_TESTING_COMPLETED' fastlane/Fastfile
grep -Fq 'lane :open do' fastlane/Fastfile
grep -Fq 'INFOPLIST_KEY_ITSAppUsesNonExemptEncryption = NO;' \
    iosApp/iosApp.xcodeproj/project.pbxproj
grep -Fq 'Enforce App Store France availability constraint' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'IOS_FRANCE_AVAILABLE' fastlane/Fastfile
bash -n scripts/verify-ios-release-signing.sh
# The pattern intentionally matches a literal variable reference.
# shellcheck disable=SC2016
grep -Fq 'passvault_dotenv_load_file "$values_file"' scripts/verify-ios-release-signing.sh
grep -Fq 'PassVault::PrivatePath.regular_file_within?' scripts/verify-ios-release-signing.sh
grep -Fq 'IOS_BUNDLE_ID" != com.passvault.ios' scripts/verify-ios-release-signing.sh
if grep -Fq './scripts/validate-private-release-config.sh' scripts/verify-ios-release-signing.sh; then
  echo "The iOS-only signing verifier must not require unrelated production Desktop credentials." >&2
  exit 1
fi
bash -n scripts/verify-android-signatures.sh
for shared_android_input in \
    MOBILE_RELEASE_ANDROID_KEYSTORE_PATH \
    MOBILE_RELEASE_ANDROID_KEYSTORE_PASSWORD \
    MOBILE_RELEASE_ANDROID_KEY_ALIAS \
    MOBILE_RELEASE_ANDROID_KEY_PASSWORD \
    MOBILE_RELEASE_REQUIRE_SIGNING; do
    if ! grep -Fq "$shared_android_input" app-android/build.gradle.kts; then
        echo "The Android build does not accept $shared_android_input." >&2
        exit 1
    fi
done
android_build_script_fixture="$temporary_root/android-build-script"
mkdir -p \
    "$android_build_script_fixture/scripts" \
    "$android_build_script_fixture/app-android/build/outputs"
cp scripts/build-android.sh "$android_build_script_fixture/scripts/"
# The fake Gradle wrapper must preserve its argument expressions literally.
# shellcheck disable=SC2016
printf '%s\n' \
    '#!/usr/bin/env bash' \
    'printf '\''%s\n'\'' "$@" > "$PASSVAULT_GRADLE_ARGS_FILE"' \
    > "$android_build_script_fixture/gradlew"
chmod +x "$android_build_script_fixture/gradlew"
android_gradle_arguments="$temporary_root/android-gradle-arguments.txt"
env -u KEYSTORE_PATH -u KEYSTORE_PASSWORD -u KEY_ALIAS -u KEY_PASSWORD \
    PASSVAULT_GRADLE_ARGS_FILE="$android_gradle_arguments" \
    "$android_build_script_fixture/scripts/build-android.sh" --debug >/dev/null
if grep -Fqx -- '--no-configuration-cache' "$android_gradle_arguments"; then
    echo "An ordinary unsigned debug build unnecessarily disabled the configuration cache." >&2
    exit 1
fi
env -u KEYSTORE_PATH -u KEY_ALIAS -u KEY_PASSWORD \
    KEYSTORE_PASSWORD=fixture-secret \
    PASSVAULT_GRADLE_ARGS_FILE="$android_gradle_arguments" \
    "$android_build_script_fixture/scripts/build-android.sh" --debug >/dev/null
if ! grep -Fqx -- '--no-configuration-cache' "$android_gradle_arguments"; then
    echo "A debug build could cache signing credentials from its environment." >&2
    exit 1
fi
if command -v pwsh >/dev/null 2>&1; then
    android_build_tools_fixture="$temporary_root/android-build-tools"
    for build_tools_version in \
        35.0.0 36.0.0-rc9 36.0.0-rc10 999999999999999999999.0.0; do
        mkdir -p "$android_build_tools_fixture/$build_tools_version"
        printf '' > "$android_build_tools_fixture/$build_tools_version/apksigner"
    done
    mkdir -p "$android_build_tools_fixture/37.0.0" "$android_build_tools_fixture/not-a-version"
    selected_build_tool="$(pwsh -NoProfile -File scripts/select-android-build-tool.ps1 \
        -BuildToolsRoot "$android_build_tools_fixture" -ToolName apksigner)"
    selected_build_tool="${selected_build_tool//$'\r'/}"
    if [[ "$selected_build_tool" != */36.0.0-rc10/apksigner &&
          "$selected_build_tool" != *\\36.0.0-rc10\\apksigner ]]; then
        echo "The newest valid preview Android build-tools directory was not selected." >&2
        exit 1
    fi

    mkdir -p "$android_build_tools_fixture/36.0.0"
    printf '' > "$android_build_tools_fixture/36.0.0/apksigner"
    selected_build_tool="$(pwsh -NoProfile -File scripts/select-android-build-tool.ps1 \
        -BuildToolsRoot "$android_build_tools_fixture" -ToolName apksigner)"
    selected_build_tool="${selected_build_tool//$'\r'/}"
    if [[ "$selected_build_tool" != */36.0.0/apksigner &&
          "$selected_build_tool" != *\\36.0.0\\apksigner ]]; then
        echo "A stable Android build-tools directory did not outrank its preview." >&2
        exit 1
    fi
    if pwsh -NoProfile -File scripts/select-android-build-tool.ps1 \
        -BuildToolsRoot "$temporary_root/missing-build-tools" \
        -ToolName apksigner >/dev/null 2>&1; then
        echo "A missing Android build-tools directory was accepted." >&2
        exit 1
    fi

    # The PowerShell platform expression must be passed literally.
    # shellcheck disable=SC2016
    if [[ "$(pwsh -NoProfile -Command 'if ($IsWindows) { "windows" } else { "non-windows" }')" == \
          "non-windows" ]]; then
        private_case_fixture="$temporary_root/private-path-case"
        mkdir -p "$private_case_fixture/scripts" "$private_case_fixture/release/PRIVATE"
        cp scripts/build-signed-android.ps1 "$private_case_fixture/scripts/"
        printf 'UNUSED=true\n' > "$private_case_fixture/release/PRIVATE/values.env"
        if pwsh -NoProfile -File "$private_case_fixture/scripts/build-signed-android.ps1" \
            -ValuesFile release/PRIVATE/values.env \
            > "$temporary_root/private-path-case-output.txt" 2>&1; then
            echo "A differently-cased private directory was accepted on a case-sensitive platform." >&2
            exit 1
        fi
        grep -Fq 'must remain below release/private/' \
            "$temporary_root/private-path-case-output.txt"

        signature_input_fixture="$temporary_root/android-signature-input"
        mkdir -p "$signature_input_fixture"
        printf 'fixture apk\n' > "$signature_input_fixture/target.apk"
        printf 'fixture aab\n' > "$signature_input_fixture/target.aab"
        ln -s target.apk "$signature_input_fixture/link.apk"
        if pwsh -NoProfile -File scripts/verify-android-signatures.ps1 \
            -ApkPath "$signature_input_fixture/link.apk" \
            -AabPath "$signature_input_fixture/target.aab" \
            > "$temporary_root/android-signature-symlink-output.txt" 2>&1; then
            echo "A symlinked Android signature-verification input was accepted." >&2
            exit 1
        fi
        grep -Fq 'must not be symlinks or reparse points' \
            "$temporary_root/android-signature-symlink-output.txt"

        signature_tool_fixture="$temporary_root/android-signature-tools"
        signature_sdk="$signature_tool_fixture/sdk"
        signature_bin="$signature_tool_fixture/bin"
        mkdir -p "$signature_sdk/build-tools/36.0.0" "$signature_bin"
        # Fake native tools must preserve their environment expressions literally.
        # shellcheck disable=SC2016
        printf '%s\n' \
            '#!/usr/bin/env bash' \
            'printf '\''Signer #1 certificate SHA-256 digest: %s\n'\'' "$PASSVAULT_FAKE_FINGERPRINT"' \
            'if [[ -n "${PASSVAULT_FAKE_SECOND_SIGNER:-}" ]]; then' \
            '  printf '\''Signer #2 certificate SHA-256 digest: %064d\n'\'' 0' \
            'fi' \
            > "$signature_sdk/build-tools/36.0.0/apksigner"
        printf '%s\n' '#!/usr/bin/env bash' 'exit 4' > "$signature_bin/jarsigner"
        # shellcheck disable=SC2016
        printf '%s\n' \
            '#!/usr/bin/env bash' \
            'printf '\''SHA256: %s\n'\'' "$PASSVAULT_FAKE_FINGERPRINT"' \
            > "$signature_bin/keytool"
        chmod +x \
            "$signature_sdk/build-tools/36.0.0/apksigner" \
            "$signature_bin/jarsigner" \
            "$signature_bin/keytool"
        lowercase_android_fingerprint="$(
            tr -cd '0-9A-Fa-f' < release/android/passvault-release-cert.sha256 |
                tr '[:upper:]' '[:lower:]'
        )"
        PATH="$signature_bin:$PATH" \
            ANDROID_SDK_ROOT="$signature_sdk" \
            PASSVAULT_FAKE_FINGERPRINT="$lowercase_android_fingerprint" \
            pwsh -NoProfile -File scripts/verify-android-signatures.ps1 \
                -ApkPath "$signature_input_fixture/target.apk" \
                -AabPath "$signature_input_fixture/target.aab" \
                > "$temporary_root/android-signature-lowercase-output.txt"
        grep -Fq 'verified against the pinned release certificate' \
            "$temporary_root/android-signature-lowercase-output.txt"
        if PATH="$signature_bin:$PATH" \
            ANDROID_SDK_ROOT="$signature_sdk" \
            PASSVAULT_FAKE_FINGERPRINT="$lowercase_android_fingerprint" \
            PASSVAULT_FAKE_SECOND_SIGNER=true \
            pwsh -NoProfile -File scripts/verify-android-signatures.ps1 \
                -ApkPath "$signature_input_fixture/target.apk" \
                -AabPath "$signature_input_fixture/target.aab" \
                > "$temporary_root/android-signature-multiple-output.txt" 2>&1; then
            echo "Multiple Android APK signers were accepted." >&2
            exit 1
        fi
        grep -Fq 'exactly the pinned PassVault release-certificate signer' \
            "$temporary_root/android-signature-multiple-output.txt"
    fi
fi
ruby -c scripts/validate-play-service-accounts.rb >/dev/null
ruby -c scripts/validate-google-play-readiness.rb >/dev/null
bash -n scripts/verify-play-oidc-access.sh
grep -Fq 'VERIFY_ONLY_NO_UPLOAD' .github/workflows/play-access-check.yml
grep -Fq 'environment: play-access-beta' .github/workflows/play-access-check.yml
grep -Fq 'environment: play-access-production' .github/workflows/play-access-check.yml
grep -Fq 'configure_environment play-access-beta false main' \
    scripts/configure-github-mobile-release.sh
grep -Fq 'configure_environment play-access-production true main' \
    scripts/configure-github-mobile-release.sh
grep -Fq "assertion.environment=='play-access-beta'" scripts/configure-google-oidc.sh
grep -Fq "assertion.environment=='play-access-production'" scripts/configure-google-oidc.sh
grep -Fq "assertion.repository_id=='\$repository_id'" scripts/configure-google-oidc.sh
grep -Fq "provider_condition_actual\" == \"\$expected_condition" scripts/configure-google-oidc.sh
grep -Fq "provider_mapping_actual\" == \"\$expected_mapping_json" scripts/configure-google-oidc.sh
grep -Fq "gh variable get \"\$variable_name\"" scripts/configure-google-oidc.sh
# The script expression must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'verify_exclusive_workload_bindings "$production_service_account"' \
    scripts/configure-google-oidc.sh
grep -Fq 'PLAY_EDIT_DELETED=PASS' scripts/verify-play-oidc-access.sh
grep -Fq 'PLAY_ALPHA_GOOGLE_GROUP_COUNT=' scripts/verify-play-oidc-access.sh
grep -Fq 'PLAY_PRODUCTION_ACCESS_ELIGIBILITY_API=UNSUPPORTED' scripts/verify-play-oidc-access.sh
grep -Fq 'PLAY_EMAIL_LIST_TESTERS_API=UNSUPPORTED' scripts/verify-play-oidc-access.sh
grep -Fq 'PLAY_EMPTY_EDIT_VALIDATION=' scripts/verify-play-oidc-access.sh
grep -Fq 'PLAY_LISTING_LOCALES=' scripts/verify-play-oidc-access.sh
grep -Fq 'api_request DELETE' scripts/verify-play-oidc-access.sh
if grep -Eiq '(edits/.*/commit|edits/.*:commit|/bundles|upload_to_play_store|fastlane|api_request (PATCH|PUT))' \
    scripts/verify-play-oidc-access.sh .github/workflows/play-access-check.yml; then
    echo "The Play access check contains a store upload or edit-commit path." >&2
    exit 1
fi
ruby -c scripts/configure-app-store-connect-beta.rb >/dev/null
ruby -c scripts/manage-testflight-public-link.rb >/dev/null
ruby -c scripts/strip-opaque-png-alpha.rb >/dev/null
ruby -c scripts/create-store-metadata-archive.rb >/dev/null
ruby -c scripts/extract-store-metadata-archive.rb >/dev/null
ruby -c scripts/lib/store_metadata_archive.rb >/dev/null
ruby -c fastlane/Fastfile >/dev/null
ruby -c scripts/create-candidate-manifest.rb >/dev/null
ruby -c scripts/validate-candidate-manifest.rb >/dev/null
ruby -c scripts/create-mobile-artifact-receipt.rb >/dev/null
ruby -c scripts/validate-mobile-artifact-receipt.rb >/dev/null
ruby -c scripts/validate-candidate-artifact-provenance.rb >/dev/null
ruby -c scripts/create-desktop-release-provenance.rb >/dev/null
ruby -c scripts/validate-desktop-release-provenance.rb >/dev/null
bash -n scripts/check-play-track-build.sh
grep -Fq 'track_promote_to' fastlane/Fastfile
grep -Fq 'play_promote(from: "internal", to: "alpha", release_status: "completed")' \
    fastlane/Fastfile
grep -Fq 'play_promote(from: "alpha", to: "production", release_status: "completed")' \
    fastlane/Fastfile
grep -Fq 'check-play-track-build.sh alpha' .github/workflows/candidate-readiness.yml
grep -Fq 'source_track=alpha' .github/workflows/mobile-store-release.yml
jq -e '.android.externalTrack == {"name": "alpha", "kind": "closed"}' \
    release/mobile-release.json >/dev/null
test "$(grep -Fc 'skip_upload_metadata: !upload_store_assets' fastlane/Fastfile)" -eq 2
test "$(grep -Fc 'skip_upload_changelogs: false' fastlane/Fastfile)" -eq 2
grep -Fq 'mapping = required_env("ANDROID_MAPPING_PATH")' fastlane/Fastfile
grep -Fq 'distribute_only: true' fastlane/Fastfile
grep -Fq 'app_platform: "ios"' fastlane/Fastfile
grep -Fq 'skip_binary_upload: true' fastlane/Fastfile
grep -Fq 'lane :update_store_assets do' fastlane/Fastfile
grep -Fq 'reject_if_possible: true' fastlane/Fastfile
grep -Fq 'overwrite_screenshots: true' fastlane/Fastfile
grep -Fq 'PASSVAULT_REVIEW_WITHDRAWAL_APPROVED' fastlane/Fastfile
grep -Fq 'I_APPROVE_REVIEW_WITHDRAWAL' .github/workflows/mobile-store-release.yml
test "$(grep -Fc 'app_review_information: app_review_information' fastlane/Fastfile)" -eq 1
if grep -Eq 'lane :(closed|external|production_candidate|production) do' fastlane/Fastfile; then
    echo "Fastlane exposes a binary-upload lane outside internal testing." >&2
    exit 1
fi
test "$(grep -Fc 'bundle exec fastlane android internal' \
    .github/workflows/mobile-store-release.yml)" -eq 1
test "$(grep -Fc 'bundle exec fastlane ios internal' \
    .github/workflows/mobile-store-release.yml)" -eq 1
# Workflow expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'APP_REVIEW_PHONE: ${{ secrets.APP_REVIEW_PHONE }}' \
    .github/workflows/mobile-store-release.yml
ruby <<'RUBY'
fastfile = File.read("fastlane/Fastfile", encoding: "UTF-8")
upload_blocks = fastfile.scan(/upload_to_app_store\((.*?)^\s*\)/m).flatten
abort("upload_to_app_store must not receive the unsupported apple_id option") if
  upload_blocks.any? { |block| block.include?("apple_id:") }

asset_lane = fastfile[/lane :update_store_assets do(.*?)^  end/m, 1]
abort("Missing the iOS store asset maintenance lane") unless asset_lane
required_asset_options = [
  "skip_binary_upload: true",
  "skip_metadata: true",
  "overwrite_screenshots: true",
  "reject_if_possible: true",
  "submit_for_review: false",
  "automatic_release: false",
]
missing_options = required_asset_options.reject { |option| asset_lane.include?(option) }
abort("Unsafe iOS store asset lane: missing #{missing_options.join(', ')}") unless missing_options.empty?
abort("iOS store asset maintenance must not submit for review") if asset_lane.include?("submit_for_review: true")
RUBY
grep -Fq 'needs: [ prepare, mobile-internal, desktop-linux, desktop-windows, desktop-macos ]' \
    .github/workflows/testing-release.yml
bash -n scripts/validate-testing-candidate-source.sh
grep -Fq 'git rev-parse FETCH_HEAD' .github/workflows/testing-release.yml
# Workflow variables must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'validate-testing-candidate-source.sh "$GITHUB_SHA" "$MAIN_SHA"' \
    .github/workflows/testing-release.yml
for workflow_path in \
    .github/workflows/testing-release.yml \
    .github/workflows/candidate-readiness.yml \
    .github/workflows/production-release.yml \
    .github/workflows/production-signing-validation.yml \
    .github/workflows/publish-stable-release.yml; do
    grep -Fq '  group: passvault-release-pipeline' "$workflow_path"
done
grep -Fq "'passvault-release-pipeline'" .github/workflows/mobile-store-release.yml
test "$(grep -Fc 'pipeline_lock_held: true' .github/workflows/testing-release.yml)" -eq 2
test "$(grep -Fc 'pipeline_lock_held: true' .github/workflows/production-release.yml)" -eq 1
if grep -E -R -q 'group: (testing-candidate|candidate-readiness|production-store-release|stable-release)' \
    .github/workflows; then
    echo "A release lifecycle still uses a workflow-local concurrency group." >&2
    exit 1
fi
ruby -ryaml <<'RUBY'
workflow = YAML.safe_load(File.read(".github/workflows/testing-release.yml"), aliases: false)
jobs = workflow.fetch("jobs")
expected_permissions = {
  "mobile-internal" => {
    "actions" => "read",
    "artifact-metadata" => "write",
    "attestations" => "write",
    "contents" => "read",
    "id-token" => "write",
  },
  "mobile-external" => {
    "actions" => "read",
    "artifact-metadata" => "write",
    "attestations" => "write",
    "contents" => "read",
    "id-token" => "write",
  },
  "publish-candidate" => {
    "artifact-metadata" => "write",
    "attestations" => "write",
    "contents" => "write",
    "id-token" => "write",
  },
}
actual_permissions = expected_permissions.keys.to_h do |job_name|
  [job_name, jobs.fetch(job_name).fetch("permissions")]
end
abort("Testing workflow job permissions differ from the least-privilege policy") \
  unless actual_permissions == expected_permissions
RUBY
ruby -ryaml <<'RUBY'
Dir[".github/workflows/*.{yml,yaml}"].each do |path|
  workflow = YAML.safe_load(File.read(path, encoding: "UTF-8"), aliases: true)
  workflow_permissions = workflow.fetch("permissions", {})
  workflow.fetch("jobs", {}).each do |job_name, job|
    permissions = job.fetch("permissions", workflow_permissions)
    steps = job.fetch("steps", [])
    if steps.any? { |step| step["run"].to_s.include?("gh attestation verify") }
      unless %w[read write].include?(permissions["attestations"])
        abort("#{path} job #{job_name} verifies attestations without attestations: read")
      end
    end
    if steps.any? { |step| step["uses"].to_s.start_with?("actions/attest@") }
      required = {
        "artifact-metadata" => "write",
        "attestations" => "write",
        "id-token" => "write",
      }
      unless required.all? { |name, access| permissions[name] == access }
        abort("#{path} job #{job_name} creates attestations without all required permissions")
      end
    end
    if steps.any? { |step| step["run"].to_s.include?("gh workflow run") } &&
       permissions["actions"] != "write"
      abort("#{path} job #{job_name} dispatches a workflow without actions: write")
    end
  end
end

reusable_call_permissions = {
  ".github/workflows/mobile-release-candidate.yml" => {
    "candidate" => {
      "actions" => "read",
      "artifact-metadata" => "write",
      "attestations" => "write",
      "contents" => "read",
      "id-token" => "write",
    },
  },
  ".github/workflows/mobile-release-external-testing.yml" => {
    "external-testing" => {
      "actions" => "read",
      "artifact-metadata" => "write",
      "attestations" => "write",
      "contents" => "read",
      "id-token" => "write",
    },
  },
  ".github/workflows/testing-release.yml" => {
    "mobile-internal" => { "artifact-metadata" => "write", "attestations" => "write" },
    "mobile-external" => {
      "actions" => "read",
      "artifact-metadata" => "write",
      "attestations" => "write",
      "contents" => "read",
      "id-token" => "write",
    },
  },
  ".github/workflows/production-release.yml" => {
    "promote-mobile-production" => {
      "actions" => "read",
      "artifact-metadata" => "write",
      "attestations" => "write",
      "contents" => "read",
      "id-token" => "write",
    },
  },
  ".github/workflows/production-signing-validation.yml" => {
    "sign-and-validate" => { "artifact-metadata" => "write", "attestations" => "write" },
  },
}
reusable_call_permissions.each do |path, jobs|
  workflow = YAML.safe_load(File.read(path, encoding: "UTF-8"), aliases: true)
  jobs.each do |job_name, expected|
    actual = workflow.fetch("jobs").fetch(job_name).fetch("permissions")
    unless expected.all? { |name, access| actual[name] == access }
      abort("#{path} reusable caller #{job_name} does not pass required permissions")
    end
  end
end
RUBY
mobile_release_kit_sha="7eebb2656d28df33e4d8e5135f1f8fb64404e2bd"
for workflow in \
    .github/workflows/mobile-release-preflight.yml \
    .github/workflows/mobile-release-candidate.yml \
    .github/workflows/mobile-release-external-testing.yml; do
    test "$(grep -Fc "@${mobile_release_kit_sha}" "$workflow")" -eq 1
    test "$(grep -Fc "tooling_sha: ${mobile_release_kit_sha}" "$workflow")" -eq 1
    if grep -Eq '@(main|master|v[0-9])([[:space:]]|$)' "$workflow"; then
        echo "$workflow uses a mutable mobile-release-kit reference." >&2
        exit 1
    fi
done
grep -Fq \
    "https://raw.githubusercontent.com/Apdelrahman1911/mobile-release-kit/${mobile_release_kit_sha}/schemas/project.schema.json" \
    release/mobile-release.json
if rg -q 'reusable-production-submit|mobile-production' \
    .github/workflows/mobile-release-{preflight,candidate,external-testing}.yml; then
    echo "The PassVault pilot callers must not expose a Production path." >&2
    exit 1
fi
test "$(grep -Fc '      id-token: write' .github/workflows/production-release.yml)" -eq 1
grep -Fq "10#\$VERSION_CODE > 2100000000" .github/workflows/mobile-store-release.yml
grep -Fq 'BUILD_NUMBER="$(awk -F= '\''$1 == "VERSION_CODE" { print $2 }'\'' version.properties)"' \
    .github/workflows/testing-release.yml
grep -Fq "vars.LEGACY_TESTING_RELEASE_ON_PUSH == 'true'" \
    .github/workflows/testing-release.yml
if grep -Fq 'GITHUB_RUN_NUMBER * 1000' .github/workflows/testing-release.yml; then
    echo "The legacy testing workflow still allocates a CI-generated Store build number." >&2
    exit 1
fi
grep -Fq 'COMMITTED_BUILD_NUMBER="$(awk -F= '\''$1 == "VERSION_CODE" { print $2 }'\'' version.properties)"' \
    .github/workflows/mobile-store-release.yml
grep -Fq '[[ "$VERSION_CODE" != "$COMMITTED_BUILD_NUMBER" ]]' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'Ruby is required to select the newest compatible Android build-tools version.' \
    scripts/verify-android-signatures.sh
grep -Fq 'contains an unsafe symlink' scripts/prepare-mobile-store-metadata.sh
test "$(grep -Fc '    environment: mobile-production' .github/workflows/release.yml)" -eq 2
ruby -ryaml <<'RUBY'
release_workflow = YAML.safe_load(File.read(".github/workflows/release.yml", encoding: "UTF-8"), aliases: true)
%w[build-desktop-linux build-desktop-windows build-desktop-macos].each do |job_name|
  support_email = release_workflow.fetch("jobs").fetch(job_name).fetch("env")["SUPPORT_EMAIL"]
  abort("#{job_name} does not receive the validated publisher support address") unless
    support_email == "${{ vars.SUPPORT_EMAIL }}"
end
RUBY
grep -Fq 'STORE_SCREENSHOT_MODE' app-android/build.gradle.kts
grep -Fq 'applicationIdSuffix = ".debug"' app-android/build.gradle.kts
grep -Fq '<string name="app_name">PassVault Dev</string>' \
    app-android/src/debug/res/values/strings.xml
grep -Fq '<string name="app_name">PassVault Dev</string>' \
    app-android/src/debug/res/values-ar/strings.xml
grep -Fq '<string name="app_name">PassVault</string>' \
    app-android/src/main/res/values/strings.xml
grep -Fq '<string name="app_name">PassVault</string>' \
    app-android/src/main/res/values-ar/strings.xml
if grep -Eiq 'productFlavors|fdroid|applicationIdSuffix = "\.storescreenshot"' \
    app-android/build.gradle.kts; then
    echo "Android still defines an obsolete third application identity." >&2
    exit 1
fi
grep -Fq -- '--no-configuration-cache' .github/workflows/mobile-store-release.yml
grep -Fq -- '--no-configuration-cache' scripts/build-android.sh
grep -Fq 'signing_environment_present' scripts/build-android.sh
grep -Fq -- '"--no-configuration-cache"' scripts/build-signed-android.ps1
grep -Fq -- '--no-configuration-cache' scripts/verify-release.sh
grep -Fq ':app-android:lintRelease' scripts/build-android.sh
grep -Fq ':app-android:lintRelease' scripts/verify-release.sh
grep -Fq ':app-android:lintRelease' .github/workflows/ci.yml
grep -Fq ':app-android:verifyReleasePackageContents' scripts/build-android.sh
grep -Fq ':app-android:verifyDebugComposeResources' scripts/build-android.sh
grep -Fq ':app-android:verifyDebugComposeResources' .github/workflows/ci.yml
grep -Fq ':app-android:verifyReleasePackageContents' scripts/verify-release.sh
grep -Fq ':app-android:verifyReleasePackageContents' .github/workflows/ci.yml
grep -Fq 'listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")' \
    app-android/build.gradle.kts
for native_library in \
    libandroidx.graphics.path.so \
    libimage_processing_util_jni.so \
    libjnidispatch.so \
    libsodium.so \
    libsqliteJni.so \
    libsurface_util_jni.so; do
    grep -Fq "\"$native_library\"" app-android/build.gradle.kts
done
test "$(grep -Fc 'expectedNativeLibraries.set(expectedAndroidNativeLibraries)' \
    app-android/build.gradle.kts)" -eq 3
grep -Fq 'packagedNativeLibraryEntries == expectedNativeLibraryEntries' \
    app-android/build.gradle.kts
grep -Fq 'select-android-build-tool.ps1' scripts/verify-android-signatures.ps1
grep -Fq '[IO.FileAttributes]::ReparsePoint' scripts/verify-android-signatures.ps1
grep -Fq ').ToUpperInvariant()' scripts/verify-android-signatures.ps1
grep -Fq 'jarsigner -verify -strict' scripts/verify-android-signatures.sh
grep -Fq 'jarsigner -verify -strict' scripts/verify-android-signatures.ps1
grep -Fq 'jarsigner_status != 0 && jarsigner_status != 4' \
    scripts/verify-android-signatures.sh
# The PowerShell expression must be matched literally.
# shellcheck disable=SC2016
grep -Fq '$jarsignerExitCode -ne 0 -and $jarsignerExitCode -ne 4' \
    scripts/verify-android-signatures.ps1
# The PowerShell expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq '$resolved.StartsWith($privatePrefix, $pathComparison)' \
    scripts/build-signed-android.ps1
# shellcheck disable=SC2016
if grep -Fq '$resolved.StartsWith($privatePrefix, [StringComparison]::OrdinalIgnoreCase)' \
    scripts/build-signed-android.ps1; then
    echo "Private release path containment is unconditionally case-insensitive." >&2
    exit 1
fi
grep -Fq 'prepareAndroidLegalAssets' app-android/build.gradle.kts
grep -Fq '@get:OutputDirectory' app-android/build.gradle.kts
grep -Fq 'addGeneratedSourceDirectory(' app-android/build.gradle.kts
grep -Fq 'PrepareAndroidLegalAssets::outputDirectory' app-android/build.gradle.kts
if grep -Eq 'assets\.(srcDir|directories\.add).*generatedAndroidLegalAssets' \
    app-android/build.gradle.kts; then
    echo "Generated Android legal assets use the legacy SourceSet API." >&2
    exit 1
fi
test "$(grep -Fc 'legalEntryPrefix.set(' app-android/build.gradle.kts)" -eq 3
grep -Fq 'Duplicate entries are present' app-android/build.gradle.kts
grep -Fq 'verifyDesktopInstalledLegalNotices' app-desktop/build.gradle.kts
grep -Fq 'THIRD_PARTY_LICENSES in Resources' iosApp/iosApp.xcodeproj/project.pbxproj
grep -Fq 'verify-legal-notice-bundle.sh' scripts/verify-ios-release-signing.sh
grep -Fq 'verify-legal-notice-bundle.sh' scripts/verify-ios-exported-artifact.sh
test -f iosApp/iosApp/iosApp.entitlements
grep -Fq 'com.apple.developer.default-data-protection' iosApp/iosApp/iosApp.entitlements
grep -Fq 'NSFileProtectionComplete' iosApp/iosApp/iosApp.entitlements
grep -Fq 'keychain-access-groups' iosApp/iosApp/iosApp.entitlements
test "$(grep -Fc 'CODE_SIGN_ENTITLEMENTS = iosApp/iosApp.entitlements;' \
    iosApp/iosApp.xcodeproj/project.pbxproj)" -eq 2
grep -Fq 'attributes = IOS_BACKUP_PROTECTION' \
    shared/src/iosMain/kotlin/com/passvault/shared/platform/IosBackupFileStore.kt
grep -Fq 'protectIosBackupPath(fileManager, path)' \
    shared/src/iosMain/kotlin/com/passvault/shared/platform/IosBackupFileStore.kt
grep -Fq ':app-android:verifyReleasePackageContents' \
    .github/workflows/mobile-store-release.yml
grep -Fq ':app-android:verifyReleasePackageContents' scripts/build-signed-android.ps1
grep -Fq 'Mobile Store Release requires the canonical Store application identities.' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'Unexpected Android Store identity' fastlane/Fastfile
grep -Fq 'Unexpected iOS Store identity' fastlane/Fastfile
bash -n scripts/validate-ios-build-identities.sh
grep -Fq 'com.passvault.ios.debug' scripts/validate-ios-build-identities.sh
grep -Fq 'com.passvault.ios' scripts/validate-ios-build-identities.sh
grep -Fq 'com.apple.developer.default-data-protection' scripts/validate-ios-build-identities.sh
grep -Fq 'CODE_SIGN_ENTITLEMENTS' scripts/validate-ios-build-identities.sh
for shared_ios_input in \
    MOBILE_RELEASE_IOS_CODE_SIGN_IDENTITY \
    MOBILE_RELEASE_IOS_CODE_SIGN_STYLE \
    MOBILE_RELEASE_IOS_DEVELOPMENT_TEAM \
    MOBILE_RELEASE_IOS_PROVISIONING_PROFILE_SPECIFIER; do
    if ! grep -Fq "$shared_ios_input" scripts/validate-ios-build-identities.sh; then
        echo "The iOS identity validator does not cover $shared_ios_input." >&2
        exit 1
    fi
done
grep -Fq 'validate_shared_release_signing_mapping' scripts/validate-ios-build-identities.sh
if grep -Eq '^[[:space:]]*PRODUCT_BUNDLE_IDENTIFIER[[:space:]]*=' \
    iosApp/Configuration/Config.xcconfig; then
    echo "The common Xcode configuration overrides the two application identities." >&2
    exit 1
fi
grep -Fq -- '-configuration Release' .github/workflows/store-screenshots.yml
grep -Fq 'readiness-manifest.json' .github/workflows/production-release.yml
grep -Fq 'Candidate Readiness must run from testing.' .github/workflows/candidate-readiness.yml
grep -Fq 'production-signing-validation.yml' .github/workflows/candidate-readiness.yml
if grep -Fq 'gh workflow run production-release.yml' .github/workflows/candidate-readiness.yml; then
    echo "Candidate Readiness bypasses production signing validation." >&2
    exit 1
fi
grep -Fq "git rev-parse \"\$source_commit^{tree}\"" .github/workflows/candidate-readiness.yml
grep -Fq "git rev-parse \"\$GITHUB_SHA^{tree}\"" .github/workflows/testing-release.yml
if grep -Fq -- '--clobber' .github/workflows/candidate-readiness.yml; then
    echo "Candidate readiness can overwrite an existing release attestation." >&2
    exit 1
fi
grep -Fq 'access_token_scopes: https://www.googleapis.com/auth/androidpublisher' \
    .github/workflows/candidate-readiness.yml
grep -Fq 'access_token_lifetime: 600s' .github/workflows/candidate-readiness.yml
grep -Fq "printf '%s\\n' \"\$status\"" .github/workflows/candidate-readiness.yml
grep -Fq 'refs/heads/release' .github/workflows/production-release.yml
# Workflow expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'candidate_tag: ${{ inputs.candidate_tag }}' \
    .github/workflows/production-release.yml
grep -Fq 'Bind production to the readiness-approved candidate' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'require-production-signing-validation.sh' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'require-production-signing-validation.sh' \
    .github/workflows/production-release.yml
if grep -Fq 'workflow_dispatch:' .github/workflows/release.yml; then
    echo "The low-level desktop publisher is still directly dispatchable around store-live checks." >&2
    exit 1
fi
if grep -Fq 'I_CONFIRM_BOTH_STORES_LIVE' .github/workflows/publish-stable-release.yml; then
    echo "Stable publication still trusts a manual stores-live assertion." >&2
    exit 1
fi
grep -Fq 'environment: mobile-production' .github/workflows/publish-stable-release.yml
# Workflow expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'production-signed-${{ inputs.candidate_tag }}' \
    .github/workflows/production-signing-validation.yml
# shellcheck disable=SC2016
grep -Fq 'build_number: ${{ steps.manifest.outputs.build_number }}' \
    .github/workflows/production-signing-validation.yml
# shellcheck disable=SC2016
grep -Fq '"${{ needs.candidate.outputs.build_number }}" "$GITHUB_SHA"' \
    .github/workflows/production-signing-validation.yml
if grep -Fq 'CANDIDATE_TAG##*.' .github/workflows/production-signing-validation.yml; then
    echo "Production signing derives the build number from an unsafe tag suffix." >&2
    exit 1
fi
grep -Fq 'production-signing-validation.yml' \
    .github/workflows/publish-stable-release.yml
grep -Fq '.conclusion == "success"' .github/workflows/publish-stable-release.yml
grep -Fq '.path == ".github/workflows/production-signing-validation.yml"' \
    .github/workflows/publish-stable-release.yml
if grep -Fq 'uses: ./.github/workflows/release.yml' \
    .github/workflows/publish-stable-release.yml; then
    echo "Stable publication still rebuilds desktop artifacts." >&2
    exit 1
fi
grep -Fq './scripts/check-play-track-build.sh production' \
    .github/workflows/publish-stable-release.yml
grep -Fq 'scripts/check-app-store-live-version.rb' \
    .github/workflows/publish-stable-release.yml
grep -Fq 'needs: [ candidate, stores-live ]' .github/workflows/publish-stable-release.yml
grep -Fq 'CI Gate' .github/workflows/ci.yml
grep -Fq 'contexts: ["CI Gate"]' scripts/configure-release-branches.sh
grep -Fq 'Verify the exact candidate source' .github/workflows/testing-release.yml
bash -n scripts/verify-ios-exported-artifact.sh
grep -Fq 'TESTFLIGHT_DISTRIBUTION_MODE' fastlane/Fastfile
if grep -Eq '(^[[:space:]]*-[[:space:]]+public-link[[:space:]]*$|default:[[:space:]]*public-link|TESTFLIGHT_DISTRIBUTION_MODE:[[:space:]]*public-link)' \
    .github/workflows/mobile-store-release.yml; then
    echo "The automated external-testing workflow still exposes the incomplete public-link mode." >&2
    exit 1
fi
grep -Fq 'runs-on: macos-26' .github/workflows/mobile-store-release.yml
grep -Fq 'Require Xcode 26 or newer' .github/workflows/mobile-store-release.yml
grep -Fq 'gem "multi_json", ">= 1.15", "< 2.0"' Gemfile
test "$(tr -d '\r\n' < .ruby-version)" = "3.3"
grep -Fq '  fastlane (2.235.0)' Gemfile.lock
grep -Fq '  multi_json (1.21.1)' Gemfile.lock
grep -Fq '  arm64-darwin' Gemfile.lock
grep -Fq '  x86_64-linux' Gemfile.lock
grep -Fq 'Validate Fastlane runtime' .github/workflows/mobile-store-release.yml
grep -Fq 'BETA_REVIEW_STATE=' scripts/manage-testflight-public-link.rb
grep -Fq 'configured external TestFlight group name is ambiguous' \
    scripts/manage-testflight-public-link.rb
grep -Fq 'configured external TestFlight group name is ambiguous' \
    scripts/configure-app-store-connect-beta.rb
grep -Fq '2_100_000_000' scripts/manage-testflight-public-link.rb
grep -Fq "REQUIRED_TEST_INFORMATION=#{beta_localizations_complete ? 'COMPLETE' : 'INCOMPLETE'}" \
    scripts/manage-testflight-public-link.rb
grep -Fq "REQUIRED_WHAT_TO_TEST=#{required_build_localizations_complete ? 'COMPLETE' : 'INCOMPLETE'}" \
    scripts/manage-testflight-public-link.rb
grep -Fq 'Verify exact Google Play source build' .github/workflows/mobile-store-release.yml
# Workflow expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'GOOGLE_OAUTH_ACCESS_TOKEN: ${{ steps.google-auth.outputs.access_token }}' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'Enforce email-list TestFlight policy and verify exact processed App Store Connect build' \
    .github/workflows/mobile-store-release.yml
grep -Fq "grep -Fqx 'PROCESSING_STATE=VALID'" .github/workflows/mobile-store-release.yml
grep -Fq 'set_environment_variable mobile-production TESTFLIGHT_EXTERNAL_GROUP' \
    scripts/configure-github-mobile-release.sh
grep -Fq 'gh secret delete TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64' \
    scripts/configure-github-mobile-release.sh
# Configuration-script expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'for secret_name in "${mobile_scoped_secret_names[@]}"; do' \
    scripts/configure-github-mobile-release.sh
# shellcheck disable=SC2016
grep -Fq 'gh secret delete "$secret_name" --repo "$GITHUB_REPOSITORY"' \
    scripts/configure-github-mobile-release.sh
# shellcheck disable=SC2016
grep -Fq 'repository_secrets="$(gh secret list --repo "$GITHUB_REPOSITORY"' \
    scripts/configure-github-mobile-release.sh
grep -Fq 'verify_absent_environment_secrets play-access-production' \
    scripts/configure-github-mobile-release.sh
grep -Fq 'verify_absent_environment_variables play-access-production' \
    scripts/configure-github-mobile-release.sh
grep -Fq "gh variable get \"\$variable_name\"" scripts/configure-github-mobile-release.sh
ruby <<'RUBY'
script = File.read("scripts/configure-github-mobile-release.sh", encoding: "UTF-8")
upload_only = %w[
  KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
  IOS_DISTRIBUTION_CERTIFICATE_BASE64 IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
  IOS_PROVISIONING_PROFILE_BASE64
]
upload_only.each do |secret_name|
  abort("#{secret_name} is not installed in mobile-beta") unless
    script.match?(/set_(?:binary|text)_secret #{secret_name} mobile-beta\b/)
end
external_expected = script[/expected_external_secrets=\((.*?)\)/m, 1]
production_expected = script[/expected_production_secrets=\((.*?)\)/m, 1]
abort("Missing scoped mobile secret declarations") unless external_expected && production_expected
upload_only.each do |secret_name|
  abort("#{secret_name} remains expected outside mobile-beta") if
    external_expected.include?(secret_name) || production_expected.include?(secret_name)
end
abort("Upload-only secret cleanup is missing") unless
  script.include?('for environment_name in mobile-external-beta mobile-production; do') &&
  script.include?('for secret_name in "${upload_only_secret_names[@]}"; do')

fastfile = File.read("fastlane/Fastfile", encoding: "UTF-8")
external_lane = fastfile[/def distribute_existing_testflight_build(.*?)^end/m, 1]
abort("Missing TestFlight external-promotion helper") unless external_lane
abort("External TestFlight promotion omits Beta App Review information") unless
  external_lane.include?("options[:beta_app_review_info]") &&
  external_lane.include?("notes: beta_review_notes")
RUBY
if grep -Eq '(--enable-public-link|publicLinkEnabled:[[:space:]]*true|PUBLIC_LINK=)' \
    scripts/manage-testflight-public-link.rb; then
    echo "The TestFlight utility exposes public-link enablement or disclosure." >&2
    exit 1
fi
grep -Fq 'parser.on("--disable-public-link", "Enforce email-list-only distribution")' \
    scripts/manage-testflight-public-link.rb
grep -Fq 'when :patch then Net::HTTP::Patch.new(uri)' scripts/manage-testflight-public-link.rb
grep -Fq 'attributes: { publicLinkEnabled: false }' scripts/manage-testflight-public-link.rb
grep -Fq 'PUBLIC_LINK_POLICY=#{public_link_policy}' scripts/manage-testflight-public-link.rb
grep -Fq './scripts/manage-testflight-public-link.rb --disable-public-link' \
    .github/workflows/mobile-store-release.yml
grep -Fq "grep -Fqx 'PUBLIC_LINK_POLICY=EMAIL_LIST_ONLY_ENFORCED'" \
    .github/workflows/mobile-store-release.yml
grep -Fq 'PUBLIC_LINK_PRESENT=' scripts/manage-testflight-public-link.rb
public_link_gate_count="$(grep -Fhc \
    "          grep -Fqx 'PUBLIC_LINK_ENABLED=false' <<< \"\$status\"" \
    .github/workflows/candidate-readiness.yml \
    .github/workflows/mobile-store-release.yml | awk '{ total += $1 } END { print total + 0 }')"
test "$public_link_gate_count" -eq 2
grep -Fq 'verify-ios-exported-artifact.sh' .github/workflows/mobile-store-release.yml
bash -n scripts/verify-macos-release-artifact.sh
bash -n scripts/notarize-macos-artifact.sh
bash -n scripts/require-production-signing-validation.sh
bash -n scripts/lib/macos-keychain.sh
if command -v pwsh >/dev/null 2>&1; then
    for powershell_script in \
        scripts/apply-signpath-windows-result.ps1 \
        scripts/create-windows-signing-catalog.ps1 \
        scripts/package-signed-windows-installers.ps1 \
        scripts/prepare-signpath-windows-request.ps1 \
        scripts/prepare-windows-runtime-signing.ps1 \
        scripts/sign-windows-artifacts.ps1 \
        scripts/update-desktop-biometric-checksum.ps1 \
        scripts/verify-windows-release-artifacts.ps1; do
        # PowerShell source is intentionally single-quoted so Bash cannot expand it.
        # shellcheck disable=SC2016
        PASSVAULT_POWERSHELL_SOURCE="$powershell_script" \
            pwsh -NoProfile -NonInteractive -Command '
              $tokens = $null
              $errors = $null
              [void][Management.Automation.Language.Parser]::ParseFile(
                $env:PASSVAULT_POWERSHELL_SOURCE,
                [ref]$tokens,
                [ref]$errors
              )
              if ($errors.Count -ne 0) {
                $errors | ForEach-Object { [Console]::Error.WriteLine($_.Message) }
                exit 1
              }
            ' >/dev/null
    done
fi
grep -Fq 'MACOS_DEVELOPER_ID_CERTIFICATE_SHA256' .github/workflows/release.yml
grep -Fq 'WINDOWS_SIGNING_CERTIFICATE_SHA256' .github/workflows/release.yml
grep -Fq 'azure/login@532459ea530d8321f2fb9bb10d1e0bcf23869a43 # v3.0.0' \
    .github/workflows/release.yml
grep -Fq 'azure/artifact-signing-action@c7ab2a863ab5f9a846ddb8265964877ef296ee82 # v2.0.0' \
    .github/workflows/release.yml
grep -Fq 'signpath/github-action-submit-signing-request@b9d91eadd323de506c0c81cf0c7fe7438f3360fd # v2' \
    .github/workflows/release.yml
grep -Fq 'create-windows-signing-catalog.ps1' .github/workflows/release.yml
grep -Fq 'prepare-windows-runtime-signing.ps1' .github/workflows/release.yml
grep -Fq 'package-signed-windows-installers.ps1' .github/workflows/release.yml
grep -Fq 'verify-windows-release-artifacts.ps1' .github/workflows/release.yml
grep -Fq 'update-desktop-biometric-checksum.ps1' .github/workflows/release.yml
grep -Fq ':app-desktop:desktopTest --stacktrace' .github/workflows/ci.yml
grep -Fq 'package-desktop-macos:' .github/workflows/ci.yml
grep -Fq 'runner: macos-15-intel' .github/workflows/ci.yml
grep -Fq 'verifyDesktopInstalledBiometricBridge' app-desktop/build.gradle.kts
grep -Fq 'adHocSignUnsignedMacOsApp' app-desktop/build.gradle.kts
grep -Fq 'adHocSignUnsignedMacOsApp ?: "createReleaseDistributable"' app-desktop/build.gradle.kts
grep -Fq '"--options",' app-desktop/build.gradle.kts
grep -Fq 'flag.trim() == "runtime"' app-desktop/build.gradle.kts
grep -Fq 'readNBytes(MAX_CODESIGN_OUTPUT_BYTES + 1)' app-desktop/build.gradle.kts
grep -Fq '"--verify", "--deep", "--strict"' app-desktop/build.gradle.kts
grep -Fq 'passvault.requireInstalledMacOsBiometric' app-desktop/build.gradle.kts
grep -Fq 'timestamped Developer ID Application signatures with Hardened Runtime' \
    app-desktop/build.gradle.kts
grep -Fq 'Packaged macOS biometric code lacks the owning app' \
    app-desktop/src/desktopMain/kotlin/com/passvault/desktop/security/biometric/DesktopBiometricNativeLoader.kt
grep -Fq 'testDesktopBiometricBridge' app-desktop/build.gradle.kts
grep -Fq 'passvault_biometric_windows_security_test' \
    app-desktop/native/biometric-bridge/CMakeLists.txt
grep -Fq 'passvault_biometric_macos_security_test' \
    app-desktop/native/biometric-bridge/CMakeLists.txt
grep -Fq 'passvault_biometric\.dll' scripts/prepare-windows-runtime-signing.ps1
grep -Fq 'passvault_biometric.dll' scripts/package-signed-windows-installers.ps1
grep -Fq 'passvault_biometric.dll' scripts/verify-windows-release-artifacts.ps1
grep -Fq 'libpassvault_biometric.dylib' scripts/verify-macos-release-artifact.sh
grep -Fq 'prevent_self_review: true' scripts/configure-github-mobile-release.sh
grep -Fq 'Required reviewer exists and self-review is prevented' \
    scripts/configure-github-mobile-release.sh
# Indirect Bash expansions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'set_environment_variable mobile-production "$variable_name" "${!variable_name}"' \
    scripts/configure-github-mobile-release.sh
grep -Fq 'http://timestamp.acs.microsoft.com' scripts/validate-private-release-config.sh
ruby <<'RUBY'
script = File.read("scripts/configure-github-mobile-release.sh", encoding: "UTF-8")
azure_names = %w[
  WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID
  WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT
  WINDOWS_ARTIFACT_SIGNING_PROFILE
]
repository_block = script[/repo_variable_names=\((.*?)\n\)/m, 1]
abort("Missing repository-variable declaration") unless repository_block
azure_names.each do |name|
  abort("#{name} is incorrectly repository-scoped") if repository_block.include?(name)
end
abort("Azure production-variable cleanup is missing") unless
  script.include?('gh variable delete "$variable_name" --repo "$GITHUB_REPOSITORY"') &&
  script.include?('expected_production_variables+=("${selected_remote_variable_names[@]}")')
abort("Production review configuration does not prevent self-review") unless
  script.scan("prevent_self_review: true").length == 1 &&
  script.scan('"prevent_self_review": false').length == 1
RUBY
# REXML parses the complete document before the structural assertions below,
# so malformed XML fails without relying on runner-specific xmllint packages.
ruby -rrexml/document <<'RUBY'
document = REXML::Document.new(
  File.read("release/windows/signpath-artifact-configuration.xml", encoding: "UTF-8"),
)
namespace = { "s" => "http://signpath.io/artifact-configuration/v1" }
parameters = REXML::XPath.match(document, "/s:artifact-configuration/s:parameters/s:parameter", namespace)
abort("Unexpected SignPath request parameters") unless
  parameters.map { |parameter| parameter.attributes["name"] }.sort == %w[publisher version]
pes = REXML::XPath.match(document, "/s:artifact-configuration/s:zip-file/s:pe-file", namespace)
pe = pes.find { |element| element.attributes["path"] == "files/**/PassVault*.exe" }
bridge = pes.find do |element|
  element.attributes["path"] ==
    "files/**/app/resources/native/windows-x64/passvault_biometric.dll"
end
msi = REXML::XPath.first(document, "/s:artifact-configuration/s:zip-file/s:msi-file", namespace)
abort("Missing bounded SignPath PE/MSI patterns") unless
  pes.length == 2 && pe && %w[path min-matches max-matches].map { |name| pe.attributes[name] } ==
    ["files/**/PassVault*.exe", "1", "1"] &&
  bridge && %w[min-matches max-matches].map { |name| bridge.attributes[name] } == ["0", "1"] &&
  msi && %w[path min-matches max-matches].map { |name| msi.attributes[name] } ==
    ["files/**/PassVault-*.msi", "0", "1"]
[pe, bridge, msi].each do |element|
  directive = REXML::XPath.first(element, "s:authenticode-sign", namespace)
  abort("SignPath must use SHA-256 Authenticode") unless
    directive && directive.attributes["hash-algorithm"] == "sha256"
end
RUBY
# Workflow source expressions must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'Remove-Item -LiteralPath $pfxPath -Force -ErrorAction SilentlyContinue' \
    .github/workflows/release.yml
# shellcheck disable=SC2016
grep -Fq 'rm -f -- "$RUNNER_TEMP/passvault-developer-id.p12"' \
    .github/workflows/release.yml
# shellcheck disable=SC2016
grep -Fq 'rm -f -- "$RUNNER_TEMP/play-closed-testers.txt"' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'PROFILE_GET_TASK_ALLOW' .github/workflows/mobile-store-release.yml
grep -Fq 'PROFILE_BETA_REPORTS' .github/workflows/mobile-store-release.yml
grep -Fq 'Entitlements:get-task-allow' scripts/verify-ios-exported-artifact.sh
grep -Fq 'Entitlements:beta-reports-active' scripts/verify-ios-exported-artifact.sh
grep -Fq 'com.apple.developer.default-data-protection' scripts/verify-ios-exported-artifact.sh
grep -Fq 'keychain-access-groups:0' scripts/verify-ios-exported-artifact.sh
grep -Fq 'INFO_PLIST_NON_EXEMPT_ENCRYPTION=%s' scripts/verify-ios-release-signing.sh
grep -Fq 'NON_EXEMPT_APPROVED) expected_encryption=true' \
    scripts/verify-ios-exported-artifact.sh
# The script expression must be matched literally.
# shellcheck disable=SC2016
grep -Fq 'INFOPLIST_KEY_ITSAppUsesNonExemptEncryption="$encryption_build_setting"' \
    scripts/verify-ios-release-signing.sh
grep -Fq 'EXPORT_COMPLIANCE_CODE=ABSENT' scripts/verify-ios-release-signing.sh

ruby <<'RUBY'
require "yaml"

workflow_paths = Dir[".github/workflows/*.{yml,yaml}"]
unsafe_shell_expressions = %w[inputs. github. secrets. vars.]
workflow_paths.each do |path|
  document = YAML.safe_load(File.read(path, encoding: "UTF-8"), aliases: true)
  pending = [document]
  until pending.empty?
    value = pending.pop
    case value
    when Hash
      value.each do |key, child|
        if key == "run" && child.is_a?(String)
          unsafe_context = unsafe_shell_expressions.find { |context| child.include?("${{ #{context}") }
          if unsafe_context
            abort("Untrusted #{unsafe_context.delete_suffix('.')} context is interpolated into shell source in #{path}")
          end
        end
        pending << child
      end
    when Array
      pending.concat(value)
    end
  end
end
RUBY

echo "Release automation tests passed."
