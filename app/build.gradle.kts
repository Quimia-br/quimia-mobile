plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

val releaseVersionCode = providers.gradleProperty("versionCode")
    .orElse(providers.environmentVariable("ANDROID_VERSION_CODE"))
    .getOrElse("1")
    .toInt()
val releaseVersionName = providers.gradleProperty("versionName")
    .orElse(providers.environmentVariable("ANDROID_VERSION_NAME"))
    .getOrElse("1.0")

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = java.util.Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use(keystoreProperties::load)
}

// Apply Google Services plugin only when google-services.json is present
// This prevents CI/local builds failing when the file is intentionally omitted (it's gitignored).
if (file("google-services.json").exists() || file("src/debug/google-services.json").exists() || file("src/main/google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

apply {
    from("$rootDir/plugins/android-commons.gradle")
    // Nota: feature-dependencies.gradle só tem efeito quando o módulo :presentation existir
    // e for declarado no settings.gradle.kts
    from("$rootDir/plugins/feature-dependencies.gradle")
}

android {
    namespace = "com.quimia.android"

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "com.quimia.android"
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(project(":designSystem"))
    
    // Retrofit e Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
