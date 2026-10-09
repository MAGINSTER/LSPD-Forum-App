plugins {
    id("com.android.application")
}

android {
    namespace = "ru.lspdforum"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = "android"
            keyAlias = "forumapp"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "ru.lspdforum"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
}