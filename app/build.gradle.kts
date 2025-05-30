plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.travelhelper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.travelhelper"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.work:work-runtime:2.10.1")
    implementation ("androidx.core:core-ktx:1.16.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.car.ui.lib)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}