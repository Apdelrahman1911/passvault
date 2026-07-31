import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.shared"
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
                // Compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)

                // Navigation 3
                implementation(libs.navigation3.runtime)
                implementation(libs.navigation3.ui)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.viewmodel)
                implementation(libs.koin.navigation)

                // Lifecycle
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.runtime.compose)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Image loading
                implementation(libs.coil.compose)
                implementation(libs.coil.core)

                // Crypto
                implementation(libs.libsodium.bindings)

                // Storage
                implementation(libs.datastore)
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)

                // Core modules
                api(project(":core:domain"))
                api(project(":core:data"))
                api(project(":core:database"))
                api(project(":core:crypto"))
                api(project(":core:security"))
                api(project(":core:designsystem"))
                api(project(":core:navigation"))

                // Feature modules
                api(project(":feature:onboarding"))
                api(project(":feature:unlock"))
                api(project(":feature:vault"))
                api(project(":feature:credential"))
                api(project(":feature:generator"))
                api(project(":feature:health"))
                api(project(":feature:settings"))
                api(project(":feature:backup"))
            }
        }

        val androidMain = getByName("androidMain")
        val desktopMain = getByName("desktopMain")

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
