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
        namespace = "com.passvault.feature.onboarding"
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
                implementation(project(":core:domain"))
                implementation(project(":core:designsystem"))

                implementation(libs.compose.animation)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui)

                implementation(libs.lifecycle.viewmodel)
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
