# RTL and accessibility physical matrix

Run the relevant rows on compact and expanded layouts. Record device, OS, locale, text scale, refresh rate, build, and result.

| Area | LTR | RTL | Runtime direction change |
|---|---|---|---|
| App launch/onboarding/auth | Alignment, focus, transitions | Mirroring, punctuation, mixed text | No restart-only stale direction |
| Top-level navigation | Order and selected state | Intended order/mirroring | Stack/state preserved |
| Detail/editor forms | Cursor, selection, errors | Secrets/URLs/OTP remain readable | Unsaved state preserved |
| Toolbar/system Back | Correct transition | Correct opposite transition | Native and Compose agree |
| Interactive iOS Back | Left-edge, slow/fast/cancel | Right-edge, slow/fast/cancel | Recognizer refreshes safely |
| Lists/swipe actions | Direction and labels | Meaningful mirroring | No stale action side |
| Dialogs/toasts/errors | Reading/focus order | Natural punctuation/order | Active UI updates safely |
| Backup/file UI | Paths and filenames | Bidi isolation | Provider/native UI coherent |
| Accessibility | TalkBack/VoiceOver order | RTL logical order | Labels announced in new locale |

## Directional icon rule

Mirror arrows, chevrons, undo/redo, and navigation affordances when their semantics follow reading/navigation direction. Do not mirror universal/media/brand/security icons merely because the layout is RTL.

## Mixed-direction content

Test email addresses, URLs, OTPs, hashes, file paths, versions, timestamps, and passwords beside translated punctuation. Use isolation APIs or Unicode isolates rather than embedding uncontrolled directional marks in stored user data.

## Text scale and assistive technology

Test at default and large accessibility text, keyboard/focus navigation on Desktop/tablet, VoiceOver on iPhone/iPad, and TalkBack on Android. Verify state descriptions, errors, destructive confirmations, and secure-value redaction.
