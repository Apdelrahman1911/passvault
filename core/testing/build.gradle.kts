import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.core.testing"
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
                api(project(":core:domain"))
                api(project(":core:crypto"))
                api(project(":core:security"))

                implementation(libs.kotlinx.coroutines.core)
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
