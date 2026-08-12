import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

// Compose Multiplatform resources configuration
compose {
    resources {
        // Fix package name mismatch by explicitly setting package
        packageOfResClass = "com.passvault.core.designsystem.generated.resources"
        // Make Res class public so it can be used from outside the module
        publicResClass = true
    }
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.core.designsystem"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        /*
         * Android resources and assets are disabled by default for AGP's KMP
         * library target. Compose resources are delivered as generated assets,
         * so leaving this disabled produces an APK that compiles but crashes
         * with MissingResourceException on its first stringResource call.
         */
        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                // Compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.animation)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                api(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val androidMain = getByName("androidMain") {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }

        val desktopMain = getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
