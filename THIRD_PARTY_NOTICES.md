# PassVault third-party notices

This document records the production runtime libraries resolved for PassVault's
Android, iOS, and Desktop applications. Build plugins and test-only libraries
are intentionally excluded. Versions below match the checked-in dependency
configuration, the resolved Android Standard release and Desktop runtime
graphs, and the native archives linked into the iOS and Desktop applications.

The full Apache License 2.0 text is distributed as [`LICENSE.txt`](LICENSE.txt).
Exact license and required notice texts that are not fully represented by that
file are reproduced in [`THIRD_PARTY_LICENSES/`](THIRD_PARTY_LICENSES/). Every
installed application package must contain all three top-level legal documents
and that directory. GitHub Releases keep the three documents as sidecars and
publish the exact directory as `THIRD_PARTY_LICENSES.zip`.

## Direct production dependencies

| Project | Runtime artifact(s) | Version | License | Upstream |
| --- | --- | ---: | --- | --- |
| Kotlin | `kotlin-stdlib`; JDK 7/8 compatibility artifacts | 2.4.10; compatibility artifacts 1.9.23 | Apache-2.0 | <https://kotlinlang.org/> |
| Compose Multiplatform | Compose runtime, UI, Foundation, resources, and Desktop runtime artifacts | 1.11.1 | Apache-2.0 | <https://github.com/JetBrains/compose-multiplatform> |
| Compose Multiplatform Material 3 | `org.jetbrains.compose.material3:material3` | 1.9.0 | Apache-2.0 | <https://github.com/JetBrains/compose-multiplatform> |
| Compose Material Icons | `org.jetbrains.compose.material:material-icons-extended` | 1.7.3 | Apache-2.0 | <https://github.com/JetBrains/compose-multiplatform> |
| AndroidX Navigation 3 | `androidx.navigation3:navigation3-runtime` | 1.1.4 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/navigation3> |
| JetBrains Navigation 3 UI | `org.jetbrains.androidx.navigation3:navigation3-ui` | 1.1.1 | Apache-2.0 | <https://github.com/JetBrains/compose-multiplatform> |
| JetBrains AndroidX Lifecycle | Lifecycle ViewModel and runtime Compose artifacts | 2.11.0 | Apache-2.0 | <https://github.com/JetBrains/compose-multiplatform> |
| Koin | Core, Compose, and Android runtime artifacts | 4.2.2 | Apache-2.0 | <https://insert-koin.io/> |
| Kotlin Coroutines | Core and Swing runtime artifacts | 1.11.0 | Apache-2.0 | <https://github.com/Kotlin/kotlinx.coroutines> |
| Kotlin Serialization | Core and JSON runtime artifacts | 1.11.0 | Apache-2.0 | <https://github.com/Kotlin/kotlinx.serialization> |
| AndroidX Room | `androidx.room:room-runtime` | 2.8.4 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/room> |
| AndroidX SQLite | SQLite runtime and bundled-driver artifacts | 2.6.2 | Apache-2.0; bundled SQLite is public domain | <https://developer.android.com/jetpack/androidx/releases/sqlite> |
| IonSpin Kotlin Multiplatform Libsodium Bindings | `com.ionspin.kotlin:multiplatform-crypto-libsodium-bindings` | 0.9.5 | Apache-2.0 | <https://github.com/ionspin/kotlin-multiplatform-libsodium> |
| libsodium | Native libraries bundled by the IonSpin bindings | 1.0.19 | ISC | <https://github.com/jedisct1/libsodium> |
| Okio | `com.squareup.okio:okio` | 3.17.0 | Apache-2.0 | <https://github.com/square/okio> |
| AndroidX Activity | `androidx.activity:activity-compose` | 1.13.0 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/activity> |
| AndroidX Biometric | `androidx.biometric:biometric` | 1.1.0 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/biometric> |
| AndroidX Core | `androidx.core:core-ktx` | 1.19.0 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/core> |
| AndroidX Core Splashscreen | `androidx.core:core-splashscreen` | 1.2.0 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/core> |
| AndroidX CameraX | Core, Camera2, Lifecycle, and View artifacts | 1.6.1 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/camera> |
| Material Components for Android | `com.google.android.material:material` | 1.14.0 | Apache-2.0 | <https://github.com/material-components/material-components-android> |
| ZXing Core | `com.google.zxing:core` | 3.5.4 | Apache-2.0 | <https://github.com/zxing/zxing> |

## Material transitive runtime dependencies

These projects are not declared directly by PassVault, but their bytecode or
native code is present in a production runtime graph or installed artifact.

| Project | Introduced by | Resolved version | License | Reproduced text |
| --- | --- | ---: | --- | --- |
| Skiko | Compose Multiplatform | 0.144.6 | Apache-2.0 | `skiko-0.144.6-NOTICE.txt`; Apache-2.0 in `LICENSE.txt` |
| JetBrains Runtime API | Compose Foundation Desktop | 1.9.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| AndroidX Compose Runtime | Compose Multiplatform and JetBrains AndroidX adapters | 1.11.2 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| AndroidX Annotation and Collection | Room, Compose, Lifecycle, and Saved State | 1.9.1 and 1.5.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| AndroidX Saved State and JetBrains Saved State adapters | Compose and Lifecycle | 1.4.0 and 1.3.6 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| AndroidX NavigationEvent and JetBrains NavigationEvent Compose | Compose UI and Navigation 3 | 1.1.2 and 1.0.1 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| JetBrains Java Annotations | Kotlin Coroutines | 23.0.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| Kotlin AtomicFU | Compose Multiplatform | 0.28.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| Kotlinx Datetime | Compose Multiplatform Material 3 | 0.7.1 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| JSpecify annotations | AndroidX Lifecycle | 1.0.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| AndroidX Media3 | CameraX Video transitive graph | 1.9.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| Google Guava | AndroidX Media3 and CameraX Video transitive graph | 33.3.1-android | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| Guava support artifacts | Guava (`failureaccess`, the empty `listenablefuture` conflict marker, Error Prone annotations, and J2ObjC annotations) | 1.0.2, 9999.0-empty-to-avoid-conflict-with-guava, 2.28.0, and 3.0.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| Skia | Skiko native runtime | `m144-22f58c9fd4` | BSD-3-Clause | `skia-m144-LICENSE.txt` |
| Google SpiderSymbol font | Skia's macOS and iOS CoreText helpers | embedded source at Skia `m144-22f58c9fd4`; copyright 2015 | SIL Open Font License 1.1 | `spider-symbol-OFL-1.1.txt` |
| ICU and its bundled data | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | Unicode-3.0 and included third-party terms | `icu-LICENSE.txt` |
| libpng | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | libpng | `libpng-LICENSE.txt` |
| libwebp | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | BSD-3-Clause | `libwebp-COPYING.txt` |
| libjpeg-turbo and IJG code | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | IJG, BSD-3-Clause, and Zlib | `libjpeg-turbo-LICENSE.md`; `libjpeg-turbo-README.ijg` |
| Adobe DNG SDK | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | Adobe DNG SDK License | `dng-sdk-LICENSE.txt`; `dng-sdk-NOTICE.txt` |
| Piex | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | Apache-2.0 | `piex-LICENSE.txt`; `piex-NOTICE.txt` |
| HarfBuzz | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | Old MIT | `harfbuzz-COPYING.txt` |
| Expat | Skia native runtime | supplied by Skia `m144-22f58c9fd4` | MIT | `expat-COPYING.txt` |
| Chromium zlib | Skia native runtime | `1.3.0.1-motley` binary at `646b7f569718921d7d4b5b8e22572ff6c76f2596`; its vendored license identifies the 1.2.12 base | Zlib | `chromium-zlib-646b7f56-LICENSE.txt` |
| FreeType | Skia's Linux font scanner, statically present in the shipped Skiko runtime | 2.13.3 (`1518bc83d26b434031bd12c706ac3c7dab3902fd`) | FreeType License; PassVault selects the FTL option | `freetype-2.13.3-LICENSE.txt`; `freetype-2.13.3-FTL.txt` |
| Wuffs | Skia's GIF codec, statically present in the shipped Skiko runtime | `e3f919ccfe3ef542cfc983a82146070258fb57f8` | Apache-2.0 | `wuffs-e3f919cc-LICENSE.txt` |
| Touchlab Stately | Koin | 2.1.0 | Apache-2.0 | Apache-2.0 in `LICENSE.txt` |
| Java Native Access (JNA) | IonSpin JVM bindings | 5.18.1 | Apache-2.0 OR LGPL-2.1-or-later; PassVault selects Apache-2.0 | `jna-5.18.1-LICENSE.txt`; Apache-2.0 in `LICENSE.txt` |
| libffi | Statically linked into JNA's native dispatcher | 3.4.4 vendored by JNA 5.18.1 | MIT | `libffi-3.4.4-LICENSE.txt` |
| resource-loader | IonSpin JVM bindings | 2.0.2 | MIT | `resource-loader-2.0.2-LICENSE.txt` |
| SLF4J API | resource-loader | 2.0.0-alpha1 | MIT | `slf4j-api-2.0.0-alpha1-LICENSE.txt` |
| Checker Framework Qualifiers | resolved Android runtime graph | 3.43.0 | MIT | `checker-qual-3.43.0-LICENSE.txt` |
| Eclipse Temurin OpenJDK runtime image | Compose Desktop/jpackage | JDK 17 selected by the release workflows | GPL-2.0-only WITH Classpath-exception-2.0 plus module-specific third-party terms | Preserved separately inside each installed Desktop image under `runtime/legal` |

All six shipped Skiko Desktop binaries (Linux arm64/x64, macOS arm64/x64, and
Windows arm64/x64) were inspected directly. They contain Skia's Wuffs-backed
GIF decoder. The Linux binaries also contain FreeType and dynamically link the
host's fontconfig library rather than bundling fontconfig code. The macOS
binaries contain Skia's embedded SpiderSymbol font; the same Skia source is
compiled for iOS. Fontconfig is therefore an operating-system dependency and
is not reproduced as a PassVault-distributed third-party component.

Skia's official-build configuration and the shipped native binaries were also
checked for pinned-but-unused optional projects. Perfetto, Brotli, giflib,
libheif, libavif, Highway, Graphite2, Fontations, Vulkan Memory Allocator, and
SPIRV-Tools are not linked into the installed PassVault applications.

The Android Standard and F-Droid release APKs are shrunk with R8. The R8 mapping
and removed-code reports show that Guava is reduced to an obfuscated
`ListenableFuture` interface, while Media3, JSpecify, Checker Framework, Error
Prone, and J2ObjC classes are removed from the current APKs. They remain listed
above because they are present in the production runtime resolution graph and
can reappear if future CameraX or Lifecycle code paths retain them.

SQLite states that its source is in the public domain:
<https://www.sqlite.org/copyright.html>.

The `THIRD_PARTY_LICENSES` directory contains PassVault's app-level reproduced
notices. It does not replace the Desktop runtime's built-in OpenJDK legal tree;
the package verifier requires both sets to be present independently.
