#!/usr/bin/env ruby
# frozen_string_literal: true

# Static wiring regression only: no Android APIs, device settings, composition,
# native prompts or application storage are accessed. Physical/runtime EN/AR,
# Activity recreation and explicit-to-SYSTEM controls remain separate gates.
require "pathname"

root = Pathname.new(__dir__).parent
path = root.join("shared/src/androidMain/kotlin/com/passvault/shared/platform/AppLanguageProvider.android.kt")
# Comments do not establish executable wiring.
source = path.read.gsub(%r{//[^\n]*}, "")
checks = {
  "observes framework configuration rather than a captured process default" =>
    source.include?("val configurationLocales = LocalConfiguration.current.locales") &&
      !source.include?("LocaleList.getDefault()") && !source.include?("Locale.getDefault()"),
  "configuration invalidates apply and native publication for every app choice" =>
    source.match?(/val\s+locale\s*=\s*remember\(language,\s*configurationLocales\)\s*\{\s*
      AndroidAppLocales\.apply\(language,\s*configurationLocales\)\.also\s*\{\s*
      publishNativeBiometricPromptLanguage\(it\.toLanguageTag\(\)\)/x),
  "SYSTEM consumes current supplied locales with deterministic empty fallback" =>
    source.include?("fun apply(language: SettingsViewModel.AppLanguage, systemLocales: LocaleList)") &&
      source.match?(/AppLanguage\.SYSTEM\s*->\s*systemLocales\.takeUnless\s*\{\s*it\.isEmpty\s*\}\s*
        \?:\s*LocaleList\(Locale\.ENGLISH\)/x),
  "explicit EN and AR selections remain independent of system choice" =>
    source.include?('AppLanguage.ENGLISH -> LocaleList(Locale.forLanguageTag("en"))') &&
      source.include?('AppLanguage.ARABIC -> LocaleList(Locale.forLanguageTag("ar"))'),
  "resource invalidation includes configuration and effective locale" =>
    source.include?("remember(language, configurationLocales, locale, baseDensity.density, baseDensity.fontScale)"),
  "process defaults and direction use the same resolved primary locale" =>
    source.match?(/LocaleList\.setDefault\(locales\)\s*val primary = locales\[0\]\s*Locale\.setDefault\(primary\)/) &&
      source.include?("TextUtils.getLayoutDirectionFromLocale(locale)") &&
      source.include?("LocalLayoutDirection provides direction"),
}

checks.each { |name, passed| puts "#{passed ? 'PASS' : 'FAIL'} PVA-030 source contract: #{name}" }
failed = checks.count { |_name, passed| !passed }
puts "#{checks.length} static source-contract checks; #{failed} failures; zero Android runtime tests"
exit(failed.zero? ? 0 : 1)
