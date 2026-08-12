# PassVault-specific R8 rules.
#
# AndroidX, Compose, Room, DataStore, Kotlin serialization, coroutines, and
# Koin publish their own consumer rules or use generated/direct references.
# Keeping those complete dependency trees defeats shrinking and can hide
# missing-rule regressions, so this file deliberately contains only the native
# crypto boundary that PassVault must preserve.

-keep class com.ionspin.kotlin.crypto.** { *; }
-dontwarn com.ionspin.kotlin.crypto.**
