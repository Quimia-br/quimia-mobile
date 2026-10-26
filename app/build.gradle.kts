import java.util.Properties

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
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { input ->
        keystoreProperties.load(input)
    }
}

val localConfiguration = Properties().apply {
    val configurationFile = rootProject.file("local.properties")
    if (configurationFile.exists()) configurationFile.inputStream().use(::load)
}
val mapsApiKey = providers.environmentVariable("MAPS_API_KEY").orNull?.trim().takeUnless { it.isNullOrEmpty() }
    ?: localConfiguration.getProperty("MAPS_API_KEY", "").trim()
val mapsConfigured = mapsApiKey.isNotEmpty() && mapsApiKey != "COLE_SUA_CHAVE_AQUI"

if (file("google-services.json").exists() || file("src/debug/google-services.json").exists() || file("src/main/google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

apply {
    from("$rootDir/plugins/android-commons.gradle")
    from("$rootDir/plugins/feature-dependencies.gradle")
}

android {
    namespace = "com.quimia.android"

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.quimia.android"
        versionCode = releaseVersionCode
        versionName = releaseVersionName
        manifestPlaceholders["MAPS_API_KEY"] = if (mapsConfigured) mapsApiKey else ""
        buildConfigField("boolean", "GOOGLE_MAPS_CONFIGURED", mapsConfigured.toString())
        buildConfigField("String", "QUIMIA_API_BASE_URL", "\"https://quimia.discloud.app/\"")
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
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(project(":designSystem"))
    implementation(libs.coil.compose)
    implementation(libs.play.services.maps)
    
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    
    implementation(libs.kotlinx.serialization.json)
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    
    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
