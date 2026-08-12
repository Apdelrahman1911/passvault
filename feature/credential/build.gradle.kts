import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.feature.credential"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

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
                // Core modules
                implementation(project(":core:domain"))
                implementation(project(":core:crypto"))
                implementation(project(":core:designsystem"))
                implementation(project(":core:otp"))

                // Compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui)

                // ViewModel
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.runtime.compose)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val androidMain = getByName("androidMain") {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.camerax.core)
                implementation(libs.camerax.camera2)
                implementation(libs.camerax.lifecycle)
                implementation(libs.camerax.view)
                implementation(libs.zxing.core)
            }
        }

        val desktopMain = getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.zxing.core)
            }
        }

        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(project(":core:testing"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
    }
}
