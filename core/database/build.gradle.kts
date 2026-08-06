import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    android {
        withHostTest { }
        namespace = "com.passvault.core.database"
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
                implementation(project(":core:crypto"))
                implementation(project(":core:security"))

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kermit)

                // Room
                implementation(libs.room.runtime)

                // SQLite
                implementation(libs.sqlite.bundled)

                // DI
                implementation(libs.koin.core)
            }
        }

        val androidMain = getByName("androidMain") {
            dependencies {
                // Android-specific database dependencies
            }
        }

        val desktopMain = getByName("desktopMain") {
            dependencies {
                // Desktop-specific database dependencies
            }
        }

        val desktopTest = getByName("desktopTest") {
            dependencies {
                implementation(project(":core:testing"))
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

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
//    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
}
