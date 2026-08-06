import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.util.zip.ZipFile

abstract class VerifyComposeResourcesInApk : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkFile: RegularFileProperty

    @get:Input
    abstract val expectedEntry: Property<String>

    @TaskAction
    fun verify() {
        val apk = apkFile.get().asFile
        check(apk.isFile) { "Android APK was not produced: ${apk.absolutePath}" }

        ZipFile(apk).use { archive ->
            check(archive.getEntry(expectedEntry.get()) != null) {
                "Required Compose resource is missing from ${apk.name}: ${expectedEntry.get()}"
            }
        }
    }
}

abstract class VerifyReleaseSigningConfiguration : DefaultTask() {
    @get:Input
    abstract val signingConfigured: Property<Boolean>

    @TaskAction
    fun verify() {
        check(signingConfigured.get()) {
            "Android release signing inputs are missing or invalid."
        }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

// Load version properties
val versionProperties = Properties().apply {
    val versionFile = rootProject.file("version.properties")

    if (versionFile.exists()) {
        versionFile.inputStream().use(::load)
    }
}

val configuredVersionName =
    providers.gradleProperty("passvault.versionName").orNull

val configuredVersionCode =
    providers.gradleProperty("passvault.versionCode").orNull

val appVersionCode =
    (configuredVersionCode ?: versionProperties.getProperty("VERSION_CODE", "1"))
        .toInt()
        .also { require(it > 0) { "passvault.versionCode must be positive" } }

val versionMajor =
    versionProperties.getProperty("VERSION_MAJOR", "1")

val versionMinor =
    versionProperties.getProperty("VERSION_MINOR", "0")

val versionPatch =
    versionProperties.getProperty("VERSION_PATCH", "0")

val appVersionName =
    (configuredVersionName ?: "$versionMajor.$versionMinor.$versionPatch")
        .also {
            require(Regex("""\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?""").matches(it)) {
                "passvault.versionName must be a semantic version"
            }
        }

val releaseKeystorePath =
    System.getenv("KEYSTORE_PATH")?.takeUnless(String::isBlank)
        ?: (findProperty("KEYSTORE_PATH") as? String)?.takeUnless(String::isBlank)
        ?: "release.keystore"

val releaseKeystorePassword =
    System.getenv("KEYSTORE_PASSWORD")
        ?: findProperty("KEYSTORE_PASSWORD") as? String
        ?: ""

val releaseKeyAlias =
    System.getenv("KEY_ALIAS")
        ?: findProperty("KEY_ALIAS") as? String
        ?: ""

val releaseKeyPassword =
    System.getenv("KEY_PASSWORD")
        ?: findProperty("KEY_PASSWORD") as? String
        ?: ""

val releaseKeystoreFile = rootProject.file(releaseKeystorePath)
val canonicalReleaseKeyAlias =
    rootProject.file("release/android/passvault-upload-alias.txt")
        .readText()
        .trim()

val requireReleaseSigning =
    providers.gradleProperty("passvault.requireReleaseSigning")
        .map { value -> value.toBooleanStrict() }
        .getOrElse(false)

fun missingReleaseSigningInputs(): List<String> = buildList {
    if (!releaseKeystoreFile.isFile) add("KEYSTORE_PATH")
    if (releaseKeystorePassword.isBlank()) add("KEYSTORE_PASSWORD")
    if (releaseKeyAlias.isBlank()) add("KEY_ALIAS")
    if (releaseKeyPassword.isBlank()) add("KEY_PASSWORD")
}

if (requireReleaseSigning) {
    val missingInputs = missingReleaseSigningInputs()
    require(missingInputs.isEmpty()) {
        "Release signing is required, but these inputs are missing or invalid: " +
            missingInputs.joinToString()
    }
    require(releaseKeyAlias == canonicalReleaseKeyAlias) {
        "KEY_ALIAS must match the canonical Android upload alias: $canonicalReleaseKeyAlias"
    }
}

android {
    namespace = "com.passvault.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.passvault.android"

        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = appVersionCode
        versionName = appVersionName
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.*"
        }

        jniLibs {
            useLegacyPackaging = false
        }
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystoreFile
            storePassword = releaseKeystorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false

            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            val releaseSigningConfig =
                signingConfigs.getByName("release")

            if (
                releaseSigningConfig.storeFile?.exists() == true &&
                !releaseSigningConfig.storePassword.isNullOrBlank() &&
                !releaseSigningConfig.keyAlias.isNullOrBlank() &&
                !releaseSigningConfig.keyPassword.isNullOrBlank()
            ) {
                signingConfig = releaseSigningConfig
            }
        }
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("standard") {
            dimension = "distribution"
        }

        create("fdroid") {
            dimension = "distribution"
            applicationIdSuffix = ".fdroid"
            versionNameSuffix = "-fdroid"
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        disable += "MissingTranslation"
        abortOnError = true
        checkReleaseBuilds = true
        checkDependencies = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            all {
                it.useJUnitPlatform()
                it.jvmArgs("-XX:+IgnoreUnrecognizedVMOptions")
            }
        }
    }

    sourceSets {
        getByName("debug") {
            assets.directories.add("src/debug/assets")
        }

        getByName("release") {
            assets.directories.add("src/release/assets")
        }
    }
}

tasks.register<VerifyReleaseSigningConfiguration>("verifyReleaseSigningConfiguration") {
    group = "verification"
    description = "Fails unless all Android release signing inputs are valid."
    signingConfigured.set(missingReleaseSigningInputs().isEmpty())
}

/*
 * Built-in Kotlin configuration for AGP 9.
 * Keep this outside android {}.
 */
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val verifyStandardDebugComposeResources =
    tasks.register<VerifyComposeResourcesInApk>("verifyStandardDebugComposeResources") {
        group = "verification"
        description = "Verifies that shared Compose resources are packaged in the Standard debug APK."
        dependsOn("assembleStandardDebug")
        apkFile.set(
            layout.buildDirectory.file(
                "outputs/apk/standard/debug/app-android-standard-debug.apk"
            )
        )
        expectedEntry.set(
            "assets/composeResources/" +
                "com.passvault.core.designsystem.generated.resources/" +
                "values/strings.commonMain.cvr"
        )
    }

tasks.named("check") {
    dependsOn(verifyStandardDebugComposeResources)
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Security
    // Material Design 3
    implementation(libs.material.components)
}
