import org.gradle.kotlin.dsl.api

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android.gradle.plugin)
    id ("kotlin-kapt")
    id ("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.android.caastv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.caastv"
        minSdk = 21
        targetSdk = 35
        versionCode = 12
        versionName = "1.0.30"
    }

    buildTypes {

        debug {
            val primaryBaseUrl = System.getenv("PRIMERYY_BASE_URL")
            val secondaryBaseUrl = System.getenv("SECONDARY_BASE_URL")
            buildConfigField("String", "PRIMERY_BASE_URL", "\"$primaryBaseUrl\"")
            buildConfigField("String", "SECONDARY_BASE_URL", "\"$secondaryBaseUrl\"")
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
            buildConfigField("String", "YOUTUBE_API_KEY", "\"${System.getenv("YOUTUBE_API_KEY")}\"")

            isMinifyEnabled = true
            // Enables resource shrinking, which is performed by the
            isShrinkResources =  true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            val primaryBaseUrl = System.getenv("PRIMERYY_BASE_URL")
            val secondaryBaseUrl = System.getenv("SECONDARY_BASE_URL")
            buildConfigField("String", "PRIMERY_BASE_URL", "\"$primaryBaseUrl\"")
            buildConfigField("String", "SECONDARY_BASE_URL", "\"$secondaryBaseUrl\"")
            buildConfigField("String", "BUILD_TYPE", "\"release\"")

            buildConfigField("String", "YOUTUBE_API_KEY", "\"${System.getenv("YOUTUBE_API_KEY")}\"")
            isMinifyEnabled = true
            // Enables resource shrinking, which is performed by the
            isShrinkResources =  true
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.livedata)
//    implementation(libs.androidx.runtime.livedata)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Coroutines
    implementation(libs.bundles.coroutines)
    // Coroutines
    implementation(libs.work.runtime)
    // Coil
    implementation(libs.bundles.coil)
    // Dagger - Hilt
    implementation(libs.bundles.hilt)
    implementation(libs.media3.ui)
    kapt(libs.hilt.compiler.ksp)
    // ExoPlayer
    api(libs.bundles.exoplayer)
    //retrofit
    implementation (libs.bundles.retrofit)
    //room
    //implementation (libs.bundles.room)
    //ksp (libs.room.ksp)
    //paging
    implementation (libs.bundles.pager)
    //permissions
    implementation (libs.bundles.permissions)
    //Navigation
    implementation (libs.navigation.compose)
    //viewmodel compose
    implementation (libs.lifecycle.viewmodel)
    //message central
    implementation (libs.zxing.embedded)

    implementation (libs.jakewharton.threetenab)
    //security
    implementation (libs.bundles.security)
    //datastore
    implementation (libs.datastore.preferences)
    //constraintlayout
    implementation (libs.constraintlayout.compose)
    //spalsh
    implementation(libs.core.splashscreen)
    //dimens
    implementation(libs.bundles.dimens)
   // If you don't use feature license encrypt, please comment line below
   // implementation ("com.sigma.packer:sigma-packer:1.0.1")
    // 1) Basic Lifecycle runtime (gives LifecycleRegistry)
    implementation ("androidx.lifecycle:lifecycle-runtime:2.6.1")
    // 2) Lifecycle KTX (coroutine support, etc.)
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    // 3) Lifecycle‐Compose integration (provides LocalLifecycleOwner)
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")


    // Add this to your app/build.gradle dependencies
    implementation("com.google.errorprone:error_prone_annotations:2.23.0")

    implementation("com.github.techitdevs:android-youtube-lib:1.1")
}