plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.plantas"
    compileSdk = 34

    defaultConfig {

        applicationId = "com.example.plantas"
        minSdk = 23
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
            buildConfigField("String", "PLANTNET_API_KEY", "\"2b10U0eWCgPpoH8ZA2sjEc2\"")
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
    buildFeatures {
        viewBinding = true
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    //firebase
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(platform(libs.google.firebase.bom))
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation (libs.firebase.storage)
    //plantnet
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)
    implementation (platform(libs.firebase.bom.v3200)) // Asegúrate de usar la última versión
    implementation (libs.google.firebase.auth)
    implementation (libs.google.firebase.firestore)
    implementation (libs.google.firebase.storage)




}
