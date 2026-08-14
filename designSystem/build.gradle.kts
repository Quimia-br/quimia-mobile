plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

apply {
    from("$rootDir/plugins/android-commons.gradle")
}

android {
    compileSdk = 34
    namespace = "com.quimia.designsystem"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)

    api(libs.androidx.compose.ui.tooling)

    // Lucide icons for Jetpack Compose - Android variant
    api("com.composables:icons-lucide-android:1.1.0")
}

