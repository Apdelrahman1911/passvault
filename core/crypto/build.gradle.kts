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

    iosArm64 {
        compilations.getByName("main") {
            cinterops.create("rawSodium") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/rawSodium.def"))
                includeDirs(project.file("src/nativeInterop/cinterop"))
            }
        }
    }
    iosSimulatorArm64 {
        compilations.getByName("main") {
            cinterops.create("rawSodium") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/rawSodium.def"))
                includeDirs(project.file("src/nativeInterop/cinterop"))
            }
        }
    }

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

        getByName("androidMain") {
            dependencies {
                // The libsodium Android artifact already supplies JNA as an AAR at runtime.
                // Keep only the compile-time API here so the plain JVM JAR is not packaged too.
                compileOnly(libs.jna)
            }
        }

        getByName("desktopMain") {
            dependencies {
                implementation(libs.jna)
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
