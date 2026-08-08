# Mobile Store Assets

These files are public release inputs and are intentionally tracked. Never use
real vault data, real one-time codes, personal email addresses, or notifications
in screenshots. Populate every locale with the same fictional demo vault.

Required before a production workflow can start:

```text
android/
  en-US/images/icon.png                 # 512 × 512, 32-bit PNG with alpha
  en-US/images/featureGraphic.png       # 1024 × 500, no alpha
  en-US/images/phoneScreenshots/01.png  # four 1080 × 1920 screenshots
  ar/images/featureGraphic.png
  ar/images/icon.png
  ar/images/phoneScreenshots/01.png
ios/
  en-US/01-welcome.png                  # one to ten accepted 6.9-inch screenshots
  en-US/02-ipad-12.9.png                # one to ten 2048 × 2732 iPad screenshots
  ar-SA/01-welcome.png
  ar-SA/02-ipad-12.9.png
```

The Android icons and text-free feature graphics are derived from the approved
in-app PassVault artwork and may be reused for both locales. They pass the PNG,
dimension, and alpha-mode checks. The remaining screenshots must be captured
from the actual application with fictional data; do not replace them with a UI
mockup.

Use 1320 × 2868 portrait screenshots for the current iPhone 6.9-inch capture
target and 2048 × 2732 for the required 12.9-inch iPad capture. The validator
also accepts Apple's other current 6.9-inch iPhone dimensions.
Name screenshots in their intended display order (`01-vault.png`,
`02-account.png`, and so on). Run:

```bash
./scripts/validate-mobile-store-assets.rb
```

The workflow uploads these assets only to the store; it never packages private
release inputs as GitHub artifacts.
