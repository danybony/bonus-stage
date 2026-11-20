import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Release signing and version come from environment variables (CI, from repository secrets) or
// local.properties (local builds). Never commit a keystore or its passwords.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

fun release(name: String): String? = System.getenv(name) ?: localProperties.getProperty(name)

android {
    namespace = "net.bonysoft.bonusstage"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.bonysoft.bonusstage"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // The release workflow sets both from the tag and the run number.
        versionCode = release("BONUS_STAGE_VERSION_CODE")?.toInt() ?: 1
        versionName = release("BONUS_STAGE_VERSION_NAME") ?: "0.1.0"
    }
    signingConfigs {
        val keystore = release("BONUS_STAGE_KEYSTORE")
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = release("BONUS_STAGE_KEYSTORE_PASSWORD")
                keyAlias = release("BONUS_STAGE_KEY_ALIAS")
                keyPassword = release("BONUS_STAGE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Unsigned when no keystore is configured (for example in pull request CI).
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    ndkVersion = "30.0.16248370"
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.31.6"
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    debugImplementation(libs.androidx.work.runtime)
}
