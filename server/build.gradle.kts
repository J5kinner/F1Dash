@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.jskinner.f1dash.server"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

kotlin {
    jvm {
        mainRun {
            mainClass.set("com.jskinner.f1dash.server.ServerKt")
        }
    }

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "F1Server"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.serialization.json)
            implementation(libs.coroutines.core)
            implementation(libs.datetime)
            implementation("io.ktor:ktor-server-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-server-cio:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-server-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-server-cors:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-server-call-logging:${libs.versions.ktor.get()}")
        }

        jvmMain.dependencies {
            implementation("io.ktor:ktor-server-netty:${libs.versions.ktor.get()}")
            implementation("ch.qos.logback:logback-classic:1.4.14")
        }
    }
}