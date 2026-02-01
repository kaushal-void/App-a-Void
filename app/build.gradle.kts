plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt") // Enable kapt plugin for annotation processing
}

android {
    namespace = "com.example.app_a_void"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app_a_void"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // UPDATED BLOCK: Replaced kotlinOptions with compilerOptions for Kotlin 2.x+

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    buildFeatures {
        compose = true
    }

    // REMOVED: composeOptions { kotlinCompilerExtensionVersion }
    // This is no longer needed because your setup uses Kotlin 2.0+ where the
    // Compose compiler is handled automatically.

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    // Compose BOM for consistent versions
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.coil.compose)

    // Core and Compose dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.compose.material3.material3)

    // Navigation Component for Jetpack Compose
    implementation(libs.androidx.navigation.compose)

    // Extended Material Icons for CRUD icons
    implementation(libs.androidx.material.icons.extended)

    // ConstraintLayout for Jetpack Compose
    implementation(libs.androidx.constraintlayout.compose)

    // AppCompat and Material
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Splash Screen API
    implementation(libs.androidx.core.splashscreen)

    // MPAndroidChart for Pie Chart and Bar Graph
    implementation(libs.mpandroidchart)

    // Compose animation library
    implementation(libs.androidx.animation)

    // Vector Drawable Support
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.vectordrawable.animated)

    // Debugging and testing
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.ui.test.junit4)

    // ROOM with annotation processing
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coroutines for async tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}