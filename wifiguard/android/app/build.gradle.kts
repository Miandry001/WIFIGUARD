plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.wifiguard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wifiguard"
        minSdk = 29          // WifiNetworkSpecifier / WifiNetworkSuggestion: Android 10+
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // !!! ATAO ETO ny adiresy serveur-nao (HTTPS) !!!
        buildConfigField("String", "SERVER_URL", "\"https://CHANGE-ME.example.com\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
