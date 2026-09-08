---
name: localization-rtl-accessibility
description: Audit and complete localization, plurals, placeholders, accessibility text, bidirectional layout, directional icons, and native platform strings for Android, iOS, Desktop, and Compose Multiplatform. Use when adding a locale, changing user-facing text, shipping Arabic or another RTL language, adding platform-specific UI, or validating that untranslated and direction-sensitive content cannot silently reach release.
---

# Localization, RTL, and Accessibility

Treat localization as a compile-time contract plus a visual and assistive-technology behavior, not as a string-count exercise.

## Invariants

1. Every user-facing base string and plural has a translation or a reviewed explicit allowlist reason.
2. Placeholder names/types, markup, escapes, and plural quantities remain compatible across locales.
3. Layout direction, gesture direction, and transition direction are verified independently.
4. Directional icons mirror only when their meaning is directional.
5. Accessibility labels and native platform UI are part of localization scope.
6. Missing translations fail automation instead of silently falling back at release.

## Audit workflow

### 1. Discover every resource surface

Inspect:

- Compose resources and generated accessors;
- Android `values*`, manifest labels, shortcuts, widgets, and notifications;
- iOS `InfoPlist.strings`, launch/native prompts, privacy usage descriptions, and localized metadata;
- Desktop menus, dialogs, installers, native bridges, and update UI;
- hard-coded UI literals, errors, toasts, dialogs, accessibility semantics, and test fixtures;
- server/store text only when it is versioned in the repository.

Run [`scripts/validate_resource_parity.py`](scripts/validate_resource_parity.py) against each base/locale pair. Use source scanning as a lead generator because legitimate technical literals and dynamic content require review.

### 2. Classify intentional non-translations

Allowlist only stable items such as trademarks, protocol identifiers, example domains, or technical tokens. Store the key and reason in reviewable configuration. Do not allowlist a directory, wildcard, or generic “not needed” reason.

### 3. Translate structurally

- Preserve named/indexed placeholders and formatting types.
- Implement all locale-required plural categories supported by the resource system.
- Keep punctuation natural for the target language; do not mechanically copy English punctuation.
- Isolate mixed-direction secrets, URLs, emails, OTPs, hashes, and file paths with safe bidi handling.
- Never translate data-format identifiers, cryptographic names, or import/export schema tokens.

### 4. Audit RTL behavior

Verify start/end padding and alignment instead of left/right assumptions. Inspect navigation bars, drawers, lists, editors, dialogs, swipe actions, charts, password fields, OTPs, and empty/error states.

For gestures and navigation:

- LTR back generally begins from the left edge; RTL back begins from the right edge when supported.
- Verify the interactive gesture, button/programmatic transition, and fast-flick settlement separately.
- Synchronize Compose layout direction and native host semantic direction after runtime locale changes.

Use [`references/rtl-physical-matrix.md`](references/rtl-physical-matrix.md) for device coverage.

### 5. Audit accessibility

- Give actionable controls localized names, roles, state descriptions, and touch targets.
- Do not duplicate labels when visible text already supplies the accessible name.
- Preserve logical focus order under RTL.
- Announce errors and state changes without exposing secrets.
- Test text scaling, truncation, contrast, keyboard navigation, VoiceOver/TalkBack, and Desktop screen readers where supported.

## Automation requirements

Fail CI for:

- missing or extra locale keys outside the allowlist;
- missing plurals or quantity mismatch;
- incompatible placeholders or malformed resource markup;
- empty translations and accidental base-language copies when configured;
- stale allowlist keys or reasons;
- obvious hard-coded UI literals in designated UI packages.

Keep generated resources deterministic and run checks on every supported resource format.

## Verification

- Compile every localized target and configuration.
- Run unit tests for plural selection, interpolation, locale persistence, and runtime switching.
- Run screenshot/layout tests for representative compact, expanded, tablet, Desktop, large-text, LTR, and RTL states.
- Test navigation directions, directional icons, gestures, dialogs, errors, backup/import/export, security, and new feature UI.
- Complete physical VoiceOver/TalkBack and iPhone/iPad/Android RTL checks for behavior host tests cannot prove.

## Failure patterns

- Reporting string coverage while leaving every plural in the base language.
- Translating visible Compose UI but omitting native prompts, manifests, or installers.
- Assuming correct RTL swipe edge guarantees correct transition animation direction.
- Replacing `start`/`end` with conditional left/right logic throughout shared UI.
- Localizing secrets or paths without bidi isolation, making characters appear reordered.
- Letting fallback resources hide incomplete shipping locales.
