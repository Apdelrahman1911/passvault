import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
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
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material3.adaptive)
                implementation(libs.compose.material3.adaptive.layout)
                implementation(libs.compose.material3.adaptive.navigation)
                api(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui)

                // Window Size Class
                implementation(libs.androidx.window)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // ViewModel
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.viewmodel.compose)

                // Serialization
                implementation(libs.kotlinx.serialization.json)

                // DateTime
                implementation(libs.kotlinx.datetime)
            }
        }

        val androidMain = getByName("androidMain") {
            dependencies {
                implementation(libs.compose.ui.tooling)
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
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.assertk)
                implementation(libs.turbine)
            }
        }
    }
}
