import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Read backend URL / device key from local.properties (gitignored) so
// nothing environment-specific is hardcoded into source. See
// local.properties.example at the project root.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        FileInputStream(file).use { load(it) }
    }
}

android {
    namespace = "com.magicmirror.vton"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.magicmirror.vton"
        minSdk = 30 // Android 11 — matches the standee's Android box
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-pilot"

        // 10.0.2.2 is the emulator's alias for the host machine's localhost;
        // harmless fallback if local.properties hasn't been set up yet.
        val baseUrl = localProperties.getProperty("magicMirror.baseUrl") ?: "http://10.0.2.2:8000"
        val deviceApiKey = localProperties.getProperty("magicMirror.deviceApiKey") ?: ""
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "DEVICE_API_KEY", "\"$deviceApiKey\"")
    }

    buildTypes {
        release {
            // Pilot-stage default: minification off so there's one fewer
            // unverified moving part on a single-store rollout. Flip this on
            // once there's real hardware to test a minified build against —
            // proguard-rules.pro already has starter keep rules for
            // kotlinx.serialization / Retrofit / libausbc.
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.fragment:fragment-compose:1.8.5")
    implementation("androidx.core:core-ktx:1.15.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Catalog / result images
    implementation("io.coil-kt:coil-compose:2.7.0")

    // QR code generation (take-home share link)
    implementation("com.google.zxing:core:3.5.3")

    // USB UVC webcam — see camera/UvcCameraFragment.kt for integration notes
    implementation("com.github.jiangdongguo.AndroidUSBCamera:libausbc:3.3.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
