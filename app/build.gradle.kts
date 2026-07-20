import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {

    namespace = "com.example.agriguard"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {

        applicationId = "com.example.agriguard"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "com.example.agriguard.runner.AgriGuardTestRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val plantIdApiKey = localProperties.getProperty("PLANT_ID_API_KEY")
            ?: project.findProperty("PLANT_ID_API_KEY")?.toString()
            ?: ""

        buildConfigField(
            "String",
            "PLANT_ID_API_KEY",
            "\"$plantIdApiKey\""
        )
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

    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        abortOnError = false
        htmlReport = true
        warningsAsErrors = false
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.auth)
    implementation(libs.glide)
    
    // Retrofit & Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation(libs.mlkit.image.labeling)

    // Kotlin Coroutines (required by DetectionViewModel + DetectionRepository)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    testImplementation(libs.junit)

    // ── Espresso UI Testing ────────────────────────────────────────────────────
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Espresso Intents — for stubbing camera / gallery intents
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    // Espresso Contrib — for RecyclerViewActions, NavigationViewActions, etc.
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")

    // Espresso Idling Resource
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")

    // Core test rules (ActivityScenarioRule, GrantPermissionRule)
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")

    // Architecture Components testing (optional, for ViewModel testing)
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    // Fragment testing
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
}