import io.grpc.internal.SharedResourceHolder.release
import io.netty.util.ReferenceCountUtil.release
import org.gradle.kotlin.dsl.release
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if(localPropertiesFile.exists()) {
 localProperties.load(localPropertiesFile.inputStream())
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}

android {
    namespace = "com.ozonic.offnbuy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ozonic.offnbuy"
        minSdk = 30
        targetSdk = 35
        versionCode = 34
        versionName = "1.0.33"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("KEYSTORE_FILE"))
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = localProperties.getProperty("KEY_ALIAS")
            keyPassword = localProperties.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes{
        release {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "APP_ID", "${localProperties.getProperty("APP_ID")}")
            buildConfigField("String", "API_KEY", "${localProperties.getProperty("API_KEY")}")
            buildConfigField("String", "INDEX_NAME", "${localProperties.getProperty("INDEX_NAME")}")
            buildConfigField("String", "API_URL", "${localProperties.getProperty("API_URL")}")
            buildConfigField("String", "API_TOKEN", "${localProperties.getProperty("API_TOKEN")}")
        }
        debug {
            buildConfigField("String", "APP_ID", "${localProperties.getProperty("APP_ID")}")
            buildConfigField("String", "API_KEY", "${localProperties.getProperty("API_KEY")}")
            buildConfigField("String", "INDEX_NAME", "${localProperties.getProperty("INDEX_NAME")}")
            buildConfigField("String", "API_URL", "${localProperties.getProperty("API_URL")}")
            buildConfigField("String", "API_TOKEN", "${localProperties.getProperty("API_TOKEN")}")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.ui.util)
    implementation(libs.material3)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.androidx.runtime)
    testImplementation(libs.junit)
    implementation (libs.ktor.client.android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.okhttp)
    implementation(libs.coil.network.okhttp) // Only available on Android/JVM.
    implementation(libs.coil.network.ktor3)
    implementation( libs.compose)

    // Import the BoM for the Firebase platform
    // Declare the dependency for the Cloud FireStore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation (libs.firebase.messaging)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

    implementation (libs.androidx.core.splashscreen)

    implementation(libs.androidx.datastore.preferences)

    implementation (libs.algoliasearch.client.kotlin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material.icons.extended.android)

    // Ktor for networking
    implementation (libs.ktor.client.content.negotiation)
    implementation (libs.ktor.serialization.kotlinx.json)

    // Kotlinx Serialization
    implementation (libs.kotlinx.serialization.json)

    //Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.app.update.ktx)
    implementation(libs.google.firebase.appcheck.playintegrity)
    implementation(libs.recaptcha)
}