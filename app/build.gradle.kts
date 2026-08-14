plugins {
    alias(libs.plugins.android.application)
}

apply {
    from("$rootDir/plugins/android-commons.gradle")
    from("$rootDir/plugins/feature-dependencies.gradle")
}

android {
    compileSdk = 34
    namespace = "com.quimia"

    defaultConfig {
        applicationId = "com.quimia"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

