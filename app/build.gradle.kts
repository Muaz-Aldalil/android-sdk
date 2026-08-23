import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.subulalhuda"
    compileSdk = 36

    // Read local.properties (gitignored, never committed)
    val localProps = Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localProps.load(localPropsFile.inputStream())
    }

    // Release signing — all 4 values required, all must be non-blank
    val releaseStoreFile = localProps.getProperty("RELEASE_STORE_FILE")?.takeIf { it.isNotBlank() }
    val releaseStorePassword = localProps.getProperty("RELEASE_STORE_PASSWORD")?.takeIf { it.isNotBlank() }
    val releaseKeyAlias = localProps.getProperty("RELEASE_KEY_ALIAS")?.takeIf { it.isNotBlank() }
    val releaseKeyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
    val hasReleaseSigning = releaseStoreFile != null && releaseStorePassword != null &&
        releaseKeyAlias != null && releaseKeyPassword != null

    defaultConfig {
        applicationId = "com.subulalhuda"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // YouTube API key — read from local.properties
        // Must be a SEPARATE key from the website's VITE_YOUTUBE_API_KEY
        buildConfigField(
            "String",
            "YOUTUBE_API_KEY",
            "\"${localProps.getProperty("YOUTUBE_API_KEY", "")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                // Intentionally left without signingConfig.
                // AssembleRelease will fail with a clear AGP error if no keystore is configured.
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // AGP 9.0+ sets Kotlin JVM target automatically from compileOptions

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Networking
    implementation(libs.okhttp)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // YouTube player
    implementation(libs.youtube.player)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Test
    testImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
