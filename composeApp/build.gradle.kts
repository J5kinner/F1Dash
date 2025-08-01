import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
//        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
        }


        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        commonMain.dependencies {
            implementation(project(":shared-models"))
            
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Ktor - Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Koin - Dependency Injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            // OrbitMVI - State Management
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
            
            // Navigation
            implementation(libs.navigation.compose)
            
            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            
            // Material Icons (using compose BOM)
            implementation(compose.materialIconsExtended)
            
            // Coroutines
            implementation(libs.coroutines.core)
            
            // Serialization
            implementation(libs.serialization.json)
            
            // DateTime
            implementation(libs.datetime)

            // DataStore
            implementation(libs.datastore.core)
            implementation(libs.datastore.preferences)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.orbit.test)
        }
        
        androidInstrumentedTest.dependencies {
            implementation(libs.compose.ui.test.junit4)
        }
    }
}

android {
    namespace = "com.jskinner.f1dash"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        create("release") {
            keyAlias = "f1dash"
            keyPassword = "f1dash123"
            storeFile = file("../f1dash-release-key.keystore")
            storePassword = "f1dash123"
        }
    }

    defaultConfig {
        applicationId = "com.jskinner.f1dash"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/v1\"")
            buildConfigField("boolean", "USE_MOCK_SERVER", "true")
        }
        getByName("release") {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"https://api.openf1.org/v1\"")
            buildConfigField("boolean", "USE_MOCK_SERVER", "false")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    implementation(project(":server"))
}

