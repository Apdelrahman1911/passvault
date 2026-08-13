import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.zip.ZipFile
import javax.inject.Inject

abstract class VerifyAndroidPackageContents : DefaultTask() {
    private data class ElfTarget(
        val elfClass: Int,
        val machine: Int,
    )

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val archiveDirectory: DirectoryProperty

    @get:Input
    abstract val archiveExtension: Property<String>

    @get:Input
    abstract val expectedEntry: Property<String>

    @get:Input
    abstract val expectedNativeAbis: ListProperty<String>

    @get:Input
    abstract val expectedNativeLibraries: ListProperty<String>

    @get:Input
    abstract val nativeLibraryPrefix: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val legalDocuments: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val thirdPartyLicenseDirectory: DirectoryProperty

    @get:Input
    abstract val legalEntryPrefix: Property<String>

    @TaskAction
    fun verify() {
        val archiveFile = resolveArchiveFile()
        ZipFile(archiveFile).use { archive ->
            val archiveEntryNames = verifyArchiveStructure(archiveFile, archive)
            verifyNativeContents(archiveFile, archive, archiveEntryNames)
            verifyLegalContents(archiveFile, archive, archiveEntryNames)
        }
    }

    private fun resolveArchiveFile(): File {
        val directory = archiveDirectory.get().asFile
        val extension = archiveExtension.get()
        check(directory.isDirectory && !Files.isSymbolicLink(directory.toPath())) {
            "Android archive directory is missing or unsafe: ${directory.absolutePath}"
        }
        val archives = directory.listFiles()
            .orEmpty()
            .asSequence()
            .filter { candidate -> candidate.isFile && candidate.extension == extension }
            .toList()
        check(archives.size == 1) {
            "Expected exactly one .$extension Android archive in ${directory.absolutePath}, found ${archives.size}"
        }
        val archiveFile = archives.single()
        check(!Files.isSymbolicLink(archiveFile.toPath())) {
            "Android archive must not be a symbolic link: ${archiveFile.absolutePath}"
        }
        return archiveFile
    }

    private fun verifyArchiveStructure(archiveFile: File, archive: ZipFile): Set<String> {
        val archiveEntryNameList = archive.entries().asSequence()
            .filterNot { entry -> entry.isDirectory }
            .map { entry -> entry.name }
            .toList()
        val duplicateArchiveEntries = archiveEntryNameList
            .groupingBy { entry -> entry }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
        check(duplicateArchiveEntries.isEmpty()) {
            "Duplicate entries are present in ${archiveFile.name}: $duplicateArchiveEntries"
        }
        check(archive.getEntry(expectedEntry.get()) != null) {
            "Required Compose resource is missing from ${archiveFile.name}: ${expectedEntry.get()}"
        }
        return archiveEntryNameList.toSet()
    }

    private fun verifyNativeContents(
        archiveFile: File,
        archive: ZipFile,
        archiveEntryNames: Set<String>,
    ) {
        val libraryPrefix = nativeLibraryPrefix.get()
        val packagedAbis = archiveEntryNames.asSequence()
            .filter { entry -> entry.startsWith(libraryPrefix) && '/' in entry.removePrefix(libraryPrefix) }
            .map { entry -> entry.removePrefix(libraryPrefix).substringBefore('/') }
            .toSortedSet()
        val configuredAbis = expectedNativeAbis.get()
        val requiredAbis = configuredAbis.toSortedSet()
        check(
            requiredAbis.isNotEmpty() &&
                requiredAbis.size == configuredAbis.size &&
                requiredAbis.all { abi -> Regex("""[A-Za-z0-9._+-]+""").matches(abi) }
        ) {
            "Expected native ABI configuration is invalid: $configuredAbis"
        }
        check(packagedAbis == requiredAbis) {
            "Unexpected native ABI set in ${archiveFile.name}: $packagedAbis; expected $requiredAbis"
        }

        val configuredNativeLibraries = expectedNativeLibraries.get()
        val nativeLibraries = configuredNativeLibraries.toSortedSet()
        check(
            nativeLibraries.isNotEmpty() &&
                nativeLibraries.size == configuredNativeLibraries.size &&
                nativeLibraries.all { library ->
                    Regex("""lib[A-Za-z0-9._+-]+\.so""").matches(library)
                }
        ) {
            "Expected native library configuration is invalid: $configuredNativeLibraries"
        }
        val expectedNativeLibraryEntries = requiredAbis
            .flatMap { abi ->
                nativeLibraries.map { library -> "$libraryPrefix$abi/$library" }
            }
            .toSortedSet()
        val packagedNativeLibraryEntries = archiveEntryNames
            .filterTo(sortedSetOf()) { entry -> entry.startsWith(libraryPrefix) }
        check(packagedNativeLibraryEntries == expectedNativeLibraryEntries) {
            "Unexpected native library set in ${archiveFile.name}: " +
                "$packagedNativeLibraryEntries; expected $expectedNativeLibraryEntries"
        }
        verifyNativeHeaders(archiveFile, archive, expectedNativeLibraryEntries)
    }

    private fun verifyNativeHeaders(
        archiveFile: File,
        archive: ZipFile,
        expectedEntries: Set<String>,
    ) {
        val libraryPrefix = nativeLibraryPrefix.get()
        expectedEntries.forEach { entryName ->
            val abi = entryName.removePrefix(libraryPrefix).substringBefore('/')
            val expectedTarget = checkNotNull(ELF_TARGETS[abi]) {
                "No ELF target is configured for Android ABI $abi"
            }
            val entry = checkNotNull(archive.getEntry(entryName)) {
                "Native library disappeared from ${archiveFile.name}: $entryName"
            }
            val header = archive.getInputStream(entry).use { input ->
                input.readNBytes(ELF_HEADER_SIZE)
            }
            check(header.size == ELF_HEADER_SIZE && header.hasElfMagic()) {
                "Native library is not a valid ELF file in ${archiveFile.name}: $entryName"
            }
            check(header.unsignedByte(ELF_DATA_INDEX) == ELF_LITTLE_ENDIAN) {
                "Native library has unsupported ELF byte order in ${archiveFile.name}: $entryName"
            }
            val actualClass = header.unsignedByte(ELF_CLASS_INDEX)
            val actualType = header.readLittleEndianUnsignedShort(ELF_TYPE_INDEX)
            val actualMachine = header.readLittleEndianUnsignedShort(ELF_MACHINE_INDEX)
            check(
                actualClass == expectedTarget.elfClass &&
                    actualType == ELF_SHARED_OBJECT &&
                    actualMachine == expectedTarget.machine
            ) {
                "Native library target mismatch in ${archiveFile.name}: $entryName " +
                    "(class=$actualClass, type=$actualType, machine=$actualMachine)"
            }
        }
    }

    private fun ByteArray.hasElfMagic(): Boolean =
        unsignedByte(0) == ELF_MAGIC_PREFIX &&
            this[1] == 'E'.code.toByte() &&
            this[2] == 'L'.code.toByte() &&
            this[3] == 'F'.code.toByte()

    private fun ByteArray.unsignedByte(index: Int): Int =
        this[index].toInt() and UNSIGNED_BYTE_MASK

    private fun ByteArray.readLittleEndianUnsignedShort(index: Int): Int =
        unsignedByte(index) or (unsignedByte(index + 1) shl BITS_PER_BYTE)

    private companion object {
        const val ELF_HEADER_SIZE = 20
        const val ELF_MAGIC_PREFIX = 0x7F
        const val ELF_CLASS_INDEX = 4
        const val ELF_DATA_INDEX = 5
        const val ELF_TYPE_INDEX = 16
        const val ELF_MACHINE_INDEX = 18
        const val ELF_LITTLE_ENDIAN = 1
        const val ELF_32_BIT = 1
        const val ELF_64_BIT = 2
        const val ELF_SHARED_OBJECT = 3
        const val ELF_MACHINE_X86 = 3
        const val ELF_MACHINE_ARM = 40
        const val ELF_MACHINE_X86_64 = 62
        const val ELF_MACHINE_AARCH64 = 183
        const val UNSIGNED_BYTE_MASK = 0xFF
        const val BITS_PER_BYTE = 8

        val ELF_TARGETS = mapOf(
            "arm64-v8a" to ElfTarget(ELF_64_BIT, ELF_MACHINE_AARCH64),
            "armeabi-v7a" to ElfTarget(ELF_32_BIT, ELF_MACHINE_ARM),
            "x86" to ElfTarget(ELF_32_BIT, ELF_MACHINE_X86),
            "x86_64" to ElfTarget(ELF_64_BIT, ELF_MACHINE_X86_64),
        )
    }

    private fun resolveExpectedLegalFiles(): List<Pair<String, File>> {
        val licenseDirectory = thirdPartyLicenseDirectory.get().asFile
        check(licenseDirectory.isDirectory && !Files.isSymbolicLink(licenseDirectory.toPath())) {
            "Canonical third-party license directory is missing or unsafe."
        }
        val licenseFiles = licenseDirectory.listFiles()
            .orEmpty()
            .sortedBy { source -> source.name }
        check(licenseFiles.isNotEmpty() && licenseFiles.all { source ->
            source.isFile &&
                !Files.isSymbolicLink(source.toPath()) &&
                Regex("""[A-Za-z0-9._+-]+""").matches(source.name)
        }) {
            "Canonical third-party license directory must contain only safe regular files."
        }

        val sourceLegalDocuments = legalDocuments.files
            .sortedBy { source -> source.name }
        check(sourceLegalDocuments.size == 3 && sourceLegalDocuments.all { source ->
            source.isFile &&
                !Files.isSymbolicLink(source.toPath()) &&
                Regex("""[A-Za-z0-9._+-]+""").matches(source.name)
        }) {
            "Canonical top-level legal documents are missing or unsafe."
        }
        return buildList {
            sourceLegalDocuments.forEach { source -> add(source.name to source) }
            licenseFiles.forEach { source ->
                add("THIRD_PARTY_LICENSES/${source.name}" to source)
            }
        }
    }

    private fun verifyLegalContents(
        archiveFile: File,
        archive: ZipFile,
        archiveEntryNames: Set<String>,
    ) {
        val legalPrefix = legalEntryPrefix.get()
        val expectedLegalFiles = resolveExpectedLegalFiles()
        expectedLegalFiles.forEach { (relativePath, source) ->
            val entryName = "$legalPrefix$relativePath"
            val entry = checkNotNull(archive.getEntry(entryName)) {
                "Required legal file is missing from ${archiveFile.name}: $entryName"
            }
            val bundledBytes = archive.getInputStream(entry).use { input -> input.readBytes() }
            check(bundledBytes.contentEquals(source.readBytes())) {
                "Bundled legal file is stale in ${archiveFile.name}: $entryName"
            }
        }

        val expectedLegalEntries = expectedLegalFiles
            .map { (relativePath, _) -> "$legalPrefix$relativePath" }
            .toSortedSet()
        val packagedLegalEntries = archiveEntryNames
            .filterTo(sortedSetOf()) { entry -> entry.startsWith(legalPrefix) }
        check(packagedLegalEntries == expectedLegalEntries) {
            "Unexpected legal file set in ${archiveFile.name}: " +
                "$packagedLegalEntries; expected $expectedLegalEntries"
        }
    }
}

@CacheableTask
abstract class PrepareAndroidLegalAssets : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val legalDocuments: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val thirdPartyLicenseDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun prepare() {
        fileSystemOperations.sync {
            from(legalDocuments) {
                into("legal")
            }
            from(thirdPartyLicenseDirectory) {
                into("legal/THIRD_PARTY_LICENSES")
            }
            into(outputDirectory)
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

abstract class VerifyAndroidApplicationIdentity : DefaultTask() {
    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val expectedApplicationId: Property<String>

    @TaskAction
    fun verify() {
        check(applicationId.get() == expectedApplicationId.get()) {
            "Android application ID ${applicationId.get()} does not match " +
                "the approved identity ${expectedApplicationId.get()}."
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
        .toLong()
        .also {
            require(it in 1..2_100_000_000) {
                "passvault.versionCode must be within Android's supported positive range"
            }
        }
        .toInt()

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

val supportedAndroidAbis =
    listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

val expectedAndroidNativeLibraries =
    listOf(
        "libandroidx.graphics.path.so",
        "libimage_processing_util_jni.so",
        "libjnidispatch.so",
        "libsodium.so",
        "libsqliteJni.so",
        "libsurface_util_jni.so",
    )

val prepareAndroidLegalAssets =
    tasks.register<PrepareAndroidLegalAssets>("prepareAndroidLegalAssets") {
        group = "build setup"
        description = "Prepares the canonical legal notice bundle for Android assets."
        legalDocuments.from(
            rootProject.files(
                "LICENSE.txt",
                "NOTICE.txt",
                "THIRD_PARTY_NOTICES.md",
            ),
        )
        thirdPartyLicenseDirectory.set(rootProject.layout.projectDirectory.dir("THIRD_PARTY_LICENSES"))
    }

fun missingReleaseSigningInputs(): List<String> = buildList {
    if (!releaseKeystoreFile.isFile) add("KEYSTORE_PATH")
    if (releaseKeystorePassword.isBlank()) add("KEYSTORE_PASSWORD")
    if (releaseKeyAlias.isBlank()) add("KEY_ALIAS")
    if (releaseKeyPassword.isBlank()) add("KEY_PASSWORD")
}

fun releaseSigningIsValid(): Boolean =
    missingReleaseSigningInputs().isEmpty() && releaseKeyAlias == canonicalReleaseKeyAlias

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
        buildConfigField("boolean", "STORE_SCREENSHOT_MODE", "false")

        // JNA still bundles obsolete armeabi/mips dispatch libraries. Limit
        // packaging to the architectures supported by Android and libsodium.
        // Keep these literals visible to Android Lint; the package verifier
        // enforces exact equality with supportedAndroidAbis after packaging.
        ndk {
            abiFilters += listOf(
                "arm64-v8a",
                "armeabi-v7a",
                "x86",
                "x86_64",
            )
        }
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
            val hasReleaseCredentials = listOf(
                releaseSigningConfig.storePassword,
                releaseSigningConfig.keyAlias,
                releaseSigningConfig.keyPassword,
            ).all { !it.isNullOrBlank() }

            if (releaseSigningConfig.storeFile?.exists() == true && hasReleaseCredentials) {
                signingConfig = releaseSigningConfig
            }
        }

        create("storeScreenshot") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-storescreenshot"
            buildConfigField("boolean", "STORE_SCREENSHOT_MODE", "true")
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
                it.useJUnit()
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

val verifyAndroidApplicationIdentities =
    tasks.register("verifyAndroidApplicationIdentities") {
        group = "verification"
        description = "Verifies that local and Store Android variants use only the approved identities."
    }

androidComponents {
    onVariants(selector().all()) { variant ->
        checkNotNull(variant.sources.assets) {
            "Android assets are unavailable for variant ${variant.name}."
        }.addGeneratedSourceDirectory(
            prepareAndroidLegalAssets,
            PrepareAndroidLegalAssets::outputDirectory,
        )

        val expectedApplicationId = when (variant.name) {
            "debug", "storeScreenshot" -> "com.passvault.android.debug"
            "release" -> "com.passvault.android"
            else -> error("Android variant ${variant.name} has no approved application identity.")
        }
        val capitalizedVariantName = variant.name.replaceFirstChar { character ->
            character.uppercase()
        }
        val verifyVariantIdentity =
            tasks.register<VerifyAndroidApplicationIdentity>(
                "verify${capitalizedVariantName}ApplicationIdentity",
            ) {
                group = "verification"
                description = "Verifies the application ID for Android variant ${variant.name}."
                applicationId.set(variant.applicationId)
                this.expectedApplicationId.set(expectedApplicationId)
            }
        verifyAndroidApplicationIdentities.configure {
            dependsOn(verifyVariantIdentity)
        }
    }
}

tasks.register<VerifyReleaseSigningConfiguration>("verifyReleaseSigningConfiguration") {
    group = "verification"
    description = "Fails unless all Android release signing inputs are valid."
    signingConfigured.set(releaseSigningIsValid())
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

val verifyDebugComposeResources =
    tasks.register<VerifyAndroidPackageContents>("verifyDebugComposeResources") {
        group = "verification"
        description = "Verifies shared resources and supported native ABIs in the debug APK."
        dependsOn("assembleDebug")
        dependsOn(verifyAndroidApplicationIdentities)
        archiveDirectory.set(layout.buildDirectory.dir("outputs/apk/debug"))
        archiveExtension.set("apk")
        expectedEntry.set(
            "assets/composeResources/" +
                "com.passvault.core.designsystem.generated.resources/" +
                "values/strings.commonMain.cvr"
        )
        expectedNativeAbis.set(supportedAndroidAbis)
        expectedNativeLibraries.set(expectedAndroidNativeLibraries)
        nativeLibraryPrefix.set("lib/")
        legalDocuments.from(
            rootProject.files("LICENSE.txt", "NOTICE.txt", "THIRD_PARTY_NOTICES.md"),
        )
        thirdPartyLicenseDirectory.set(rootProject.layout.projectDirectory.dir("THIRD_PARTY_LICENSES"))
        legalEntryPrefix.set("assets/legal/")
    }

val verifyReleaseApkContents =
    tasks.register<VerifyAndroidPackageContents>("verifyReleaseApkContents") {
        group = "verification"
        description = "Verifies shared resources and supported native ABIs in the release APK."
        dependsOn("assembleRelease")
        dependsOn(verifyAndroidApplicationIdentities)
        archiveDirectory.set(layout.buildDirectory.dir("outputs/apk/release"))
        archiveExtension.set("apk")
        expectedEntry.set(
            "assets/composeResources/" +
                "com.passvault.core.designsystem.generated.resources/" +
                "values/strings.commonMain.cvr"
        )
        expectedNativeAbis.set(supportedAndroidAbis)
        expectedNativeLibraries.set(expectedAndroidNativeLibraries)
        nativeLibraryPrefix.set("lib/")
        legalDocuments.from(
            rootProject.files("LICENSE.txt", "NOTICE.txt", "THIRD_PARTY_NOTICES.md"),
        )
        thirdPartyLicenseDirectory.set(rootProject.layout.projectDirectory.dir("THIRD_PARTY_LICENSES"))
        legalEntryPrefix.set("assets/legal/")
    }

val verifyReleaseBundleContents =
    tasks.register<VerifyAndroidPackageContents>("verifyReleaseBundleContents") {
        group = "verification"
        description = "Verifies shared resources and supported native ABIs in the release AAB."
        dependsOn("bundleRelease")
        dependsOn(verifyAndroidApplicationIdentities)
        archiveDirectory.set(layout.buildDirectory.dir("outputs/bundle/release"))
        archiveExtension.set("aab")
        expectedEntry.set(
            "base/assets/composeResources/" +
                "com.passvault.core.designsystem.generated.resources/" +
                "values/strings.commonMain.cvr"
        )
        expectedNativeAbis.set(supportedAndroidAbis)
        expectedNativeLibraries.set(expectedAndroidNativeLibraries)
        nativeLibraryPrefix.set("base/lib/")
        legalDocuments.from(
            rootProject.files("LICENSE.txt", "NOTICE.txt", "THIRD_PARTY_NOTICES.md"),
        )
        thirdPartyLicenseDirectory.set(rootProject.layout.projectDirectory.dir("THIRD_PARTY_LICENSES"))
        legalEntryPrefix.set("base/assets/legal/")
    }

tasks.register("verifyReleasePackageContents") {
    group = "verification"
    description = "Verifies every Android release archive produced for publication."
    dependsOn(verifyReleaseApkContents)
    dependsOn(verifyReleaseBundleContents)
}

tasks.named("check") {
    dependsOn(verifyDebugComposeResources)
    dependsOn(verifyAndroidApplicationIdentities)
}

dependencies {
    implementation(project(":shared"))

    testImplementation(libs.kotlin.test.junit)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)

    // Provides the native Material 3 themes referenced by Android resources.
    implementation(libs.material.components)
}
