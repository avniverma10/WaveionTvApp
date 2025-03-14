plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
    id ("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.tvapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tvapp"
        minSdk = 21
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
//        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.constraintlayout)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")  // If you are using kapt
    implementation("androidx.room:room-ktx:2.6.1")  // Add this line
    implementation ("io.coil-kt:coil-compose:2.4.0")
// Add to your app's build.gradle file under dependencies
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")  // Check for the latest version
    implementation ("com.squareup.okhttp3:okhttp-sse:4.9.0")  // SSE support




//    //for t96 mini
//    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.8")
    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0")

    //Navigation
    implementation ("androidx.navigation:navigation-compose:2.8.6")

    //viewmodel compose
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //coil
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-svg:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    //couroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    //ExoPlayer
    implementation ("androidx.media3:media3-exoplayer:1.5.1")
    implementation ("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")

    //dash
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")

    //message central
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    //pager
    implementation("com.google.accompanist:accompanist-pager:0.32.0")

    //datastore
    implementation ("androidx.datastore:datastore-preferences:1.1.2")


    //Rooms DB
    annotationProcessor("androidx.room:room-compiler:2.6.1")



}