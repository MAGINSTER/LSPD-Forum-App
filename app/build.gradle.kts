plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.forumapp"
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
        applicationId = "com.example.forumapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
}