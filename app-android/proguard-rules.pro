# ProGuard/R8 Rules for PassVault
# https://developer.android.com/studio/build/shrink-code#keep-code

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#===============================================================================
# PassVault Specific Rules
#===============================================================================

# Keep PassVault Application Class
-keep class com.passvault.android.PassVaultApplication { *; }

# Keep Main Activity
-keep class com.passvault.android.MainActivity { *; }

#===============================================================================
# Kotlin & Kotlinx Serialization
#===============================================================================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep kotlinx.serialization.SerializationStrategy for reflective lookup
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}

# Keep Serializers for Data Classes
-keep,includedescriptorclasses class com.passvault.**$$serializer { *; }
-keepclassmembers class com.passvault.** {
    *** Companion;
}

# Keep serializable classes
-keep class * extends kotlinx.serialization.Serializable
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

#===============================================================================
# Room Database
#===============================================================================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Entity class * {
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
    <init>(...);
}

# Room
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }

# SQLite
-keep class androidx.sqlite.** { *; }
-keep class org.sqlite.** { *; }

#===============================================================================
# Koin Dependency Injection
#===============================================================================

-keep class org.koin.** { *; }
-keepclassmembers class org.koin.** { *; }
-dontwarn org.koin.**

# Keep modules and components
-keep class * extends org.koin.core.module.Module
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# Keep Koin module declarations
-keep class com.passvault.**.*Module { *; }
-keep class com.passvault.**.*KoinModule { *; }

#===============================================================================
# AndroidX & Jetpack
#===============================================================================

# AndroidX
-keep class androidx.** { *; }
-keepclassmembers class androidx.** { *; }
-dontwarn androidx.**

# Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Security Crypto
-keep class androidx.security.** { *; }
-keep class com.google.crypto.** { *; }
-dontwarn androidx.security.**

# DataStore
-keep class androidx.datastore.** { *; }
-keepclassmembers class * {
    @androidx.datastore.* <methods>;
}

# Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

#===============================================================================
# Material Components
#===============================================================================

-keep class com.google.android.material.** { *; }
-keepclassmembers class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

#===============================================================================
# Compose
#===============================================================================

-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Compose Compiler
-keep class androidx.compose.compiler.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

#===============================================================================
# Crypto (Libsodium)
#===============================================================================

-keep class com.ionspin.kotlin.crypto.** { *; }
-keep class com.ionspin.kotlin.crypto.libsodium.** { *; }
-keepclassmembers class com.ionspin.kotlin.crypto.** { *; }
-dontwarn com.ionspin.kotlin.crypto.**

# Keep crypto keys and encryption related classes
-keep class com.passvault.crypto.** { *; }
-keep class com.passvault.security.** { *; }

#===============================================================================
# Networking (Ktor - if used)
#===============================================================================

-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

#===============================================================================
# Image Loading (Coil)
#===============================================================================

-keep class coil.** { *; }
-keepclassmembers class coil.** { *; }
-dontwarn coil.**

#===============================================================================
# Logging
#===============================================================================

# Keep SLF4J
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

#===============================================================================
# Reflection
#===============================================================================

# Keep class members for reflection
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
}

#===============================================================================
# PassVault Domain Models (Keep for serialization)
#===============================================================================

-keep class com.passvault.domain.model.** { *; }
-keepclassmembers class com.passvault.domain.model.** { *; }

# Keep DTOs
-keep class com.passvault.data.dto.** { *; }
-keepclassmembers class com.passvault.data.dto.** { *; }

# Keep Entities
-keep class com.passvault.database.entity.** { *; }
-keepclassmembers class com.passvault.database.entity.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

#===============================================================================
# Third Party Libraries
#===============================================================================

# Keep Kotlin stdlib
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Keep Kotlinx
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**

# Keep atomicfu
-keep class kotlinx.atomicfu.** { *; }
-dontwarn kotlinx.atomicfu.**

# Keep datetime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

#===============================================================================
# Debug/Development
#===============================================================================

# Uncomment for debugging
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile

# Keep BuildConfig
-keep class com.passvault.android.BuildConfig { *; }

# Keep all in debug builds
#-keep class com.passvault.** { *; }
