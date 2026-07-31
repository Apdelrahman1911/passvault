# Editorial Vault UI Rework

Updated: 2026-07-29

This checklist tracks the application-wide visual rework inspired by `E:\passapp\kimi\app.webp`. The reference is
directional rather than a layout to copy. Functional behavior, security boundaries, real data, navigation, and
platform compatibility must remain intact.

## Visual thesis

PassVault will use an **editorial vault** language:

- warm ivory and charcoal foundations instead of generic cool gray/blue surfaces;
- restrained sage for safe/healthy states, amber for caution, and blush for destructive/error states;
- large, quiet status typography paired with compact uppercase labels;
- thin outlined panels and deliberate negative space in place of stacked elevated cards;
- softly rounded fields and actions, with a dark floating action capsule on compact vault layouts;
- adaptive layouts that become structured workspaces on Desktop rather than stretched phone screens;
- complete light and dark palettes with semantic color-role pairings;
- visible hover, focus, pressed, selected, disabled, error, warning, and success states;
- motion used only for state continuity and feedback, with short, calm transitions.

## Shared system

| ID | Surface | Scope | Verification | State |
|---|---|---|---|---|
| UI-RW-001 | Color system | semantic light/dark palettes and extended security colors | design-system compile, semantic-role and contrast review | VERIFIED |
| UI-RW-002 | Typography | editorial display scale, readable body/label hierarchy, secure monospace | design-system compile and source review | VERIFIED |
| UI-RW-003 | Shape/elevation/spacing | outlined panels, capsule actions, 48 dp targets, responsive padding | design-system compile and component review | VERIFIED |
| UI-RW-004 | Shared page primitives | page header, outlined panel, icon tile, status banner, adaptive content container | common/Desktop/iOS compile | VERIFIED |
| UI-RW-005 | Shared feedback | loading, empty, error, success, validation, snackbar and dialog styling | component review and full tests | VERIFIED |
| UI-RW-006 | Interaction states | bounded indication, hover/focus/pressed/selected/disabled states | source audit and Desktop compact/expanded runtime | VERIFIED |

## Screen inventory

| ID | Screen or surface | Normal/loading/empty/error | Compact/medium/expanded | A11y/keyboard | State |
|---|---|---|---|---|---|
| UI-RW-010 | Welcome | required states reviewed | all widths | semantics and CTA order | VERIFIED |
| UI-RW-011 | Master-password creation | validation/loading/error | all widths | IME/focus/password controls | VERIFIED |
| UI-RW-012 | Master-password confirmation | match/loading/error | all widths | IME/focus/password controls | VERIFIED |
| UI-RW-013 | Security explanation | long content | all widths | headings/scrolling | VERIFIED |
| UI-RW-014 | Unlock and failed attempts | loading/error/biometric states | all widths | IME/focus/announcements | VERIFIED |
| UI-RW-020 | Vault app shell and navigation | loading/error | compact/medium/expanded | mouse/keyboard/touch | VERIFIED |
| UI-RW-021 | Credential list | loading/empty/error/large vault | compact/medium/expanded | list semantics/stable targets | VERIFIED |
| UI-RW-022 | Search, filters and sorting | no-result/error | compact/medium/expanded | focus/traversal | VERIFIED |
| UI-RW-023 | Folder sidebar/dialog | empty/error/long labels | medium/expanded | keyboard/mouse | VERIFIED |
| UI-RW-024 | Tags and favorites | empty/long labels | all widths | selected semantics | VERIFIED |
| UI-RW-025 | Credential list/card rows | long/unicode/RTL content | all widths | actions and labels | VERIFIED |
| UI-RW-030 | Credential details | loading/error/long content | all widths | reveal/copy controls | VERIFIED |
| UI-RW-031 | Credential editor | loading/validation/error/unsaved | all widths | IME/focus/double-submit | VERIFIED |
| UI-RW-032 | Custom fields and URL editor | empty/error/long content | all widths | keyboard/dialog focus | VERIFIED |
| UI-RW-033 | Delete/discard dialogs | destructive/error | resize-safe | focus/semantics | VERIFIED |
| UI-RW-040 | Password generator | error/disabled | all widths | keyboard/actions | VERIFIED |
| UI-RW-041 | Generator options/password display | long generated value | all widths | copy/refresh semantics | VERIFIED |
| UI-RW-050 | Password-health overview | loading/error/empty | all widths | heading/status semantics | VERIFIED |
| UI-RW-051 | Health issue lists/tabs/dialog | empty/large lists | all widths | selected/focus semantics | VERIFIED |
| UI-RW-060 | Settings hub | loading/error | all widths | traversal/toggle roles | VERIFIED |
| UI-RW-061 | Appearance settings | themes/previews | all widths | selected semantics | VERIFIED |
| UI-RW-062 | Security settings/change password | loading/validation/error | all widths | IME/focus | VERIFIED |
| UI-RW-063 | Data settings | loading/error/destructive | all widths | focus/semantics | VERIFIED |
| UI-RW-070 | Backup/export | loading/success/error | all widths | IME/focus | VERIFIED |
| UI-RW-071 | Restore/import/preview | analyzing/error/confirmation | all widths | focus/announcements | VERIFIED |
| UI-RW-080 | Android system bars/IME/screenshots | all sensitive screens | compact/medium | device verification | EXTERNAL |
| UI-RW-081 | Desktop window/menu/tray | resized/minimized/restored | medium/expanded | keyboard/mouse/focus | VERIFIED |

## Verification gates

| ID | Gate | State |
|---|---|---|
| UI-RW-090 | Targeted module compiles after each feature group | VERIFIED |
| UI-RW-091 | Android debug APK builds, installs, starts, and has no fatal Logcat entries | EXTERNAL |
| UI-RW-092 | Desktop development runtime starts and remains alive | VERIFIED |
| UI-RW-093 | Desktop release distribution builds and exact launcher smoke passes | VERIFIED |
| UI-RW-094 | Unit tests and non-Detekt check pass | VERIFIED |
| UI-RW-095 | Android release/R8 and Compose resource verification pass | VERIFIED |
| UI-RW-096 | common design system and shared application compile for iOS simulator | VERIFIED |
| UI-RW-097 | source scan finds no new hardcoded user strings, sensitive logs, TODO/FIXME/HACK, or fake data | VERIFIED |

Detekt is intentionally excluded because the user explicitly asked to skip it. It must not be reported as passing.

## Verification evidence

- All screen groups were compiled for Desktop and the shared/design-system code was compiled for the iOS simulator
  target after the rework.
- `test check -x detekt verifyDependencies`, Android release/R8/lint, the Compose-resource APK assertion, and
  Windows release packaging pass. The suite contains 611 tests with zero failures, errors, or skips.
- The Desktop development runtime was visually inspected at 1536 x 830 and 520 x 820. The window remained
  responsive, content stayed bounded and scrollable, and the process closed cleanly.
- The exact packaged Desktop launcher remained alive and responsive for the 30-second release smoke period.
- The final source scan found no unfinished markers, legacy hardcoded component colors, or production logging.
  Compose animation diagnostic labels and timing-only crypto benchmark output are not user-visible or sensitive.
- Android debug and release APKs build and contain their Compose resources. No device is currently reported by
  `adb devices -l`, so post-rework install, IME, lifecycle, screenshot-protection, and Logcat interaction checks are
  marked `EXTERNAL` rather than represented as passes.
