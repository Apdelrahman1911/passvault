import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.feature.backup"
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
                implementation(project(":core:data"))
                implementation(project(":core:database"))
                // implementation(project(":core:crypto"))
                implementation(project(":core:security"))
                implementation(project(":core:designsystem"))
                implementation(project(":core:navigation"))

                // Compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui)

                // Navigation
                implementation(libs.navigation3.runtime)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.viewmodel)
                implementation(libs.koin.navigation)

                // ViewModel
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.viewmodel.compose)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // DataStore
                implementation(libs.datastore)
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
                implementation(libs.koin.test)
            }
        }
    }
}
