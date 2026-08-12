import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.core.crypto"
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
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.libsodium.bindings)
            }
        }

        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(project(":core:testing"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidHostTest = getByName("androidHostTest") {
            dependencies {
                runtimeOnly(
                    "com.ionspin.kotlin:multiplatform-crypto-libsodium-bindings-jvm:" +
                        libs.versions.libsodium.bindings.get(),
                )
            }
        }
    }
}
