# PassVault-specific R8 rules.
#
# IonSpin's Android artifact loads libsodium through JNA but publishes no
# consumer rules. JNA resolves JnaLibsodiumInterface methods and Structure
# fields by name; ordinary Kotlin wrappers are direct references. The package-
# wide rule is therefore a conservative release-reliability policy, not proof
# that every retained class needs its name. Narrow it only after a minified
# Release device matrix exercises every production crypto path. Readable names
# are not a security control, and unrelated dependency trees remain shrinkable.
-keep class com.ionspin.kotlin.crypto.** { *; }
