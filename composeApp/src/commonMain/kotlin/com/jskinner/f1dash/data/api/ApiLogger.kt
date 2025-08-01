package com.jskinner.f1dash.data.api

object ApiLogger {
    fun logApiConfiguration() {
        val config = getApiConfig()
        val mode = if (config.isDebugMode) "MOCK SERVER" else "PRODUCTION API"

        println("🏎️ F1Dash API Configuration:")
        println("   Mode: $mode")
        println("   Base URL: ${config.baseUrl}")
        println("   Is Debug Mode: ${config.isDebugMode}")
        println("   Config Type: ${config::class.simpleName}")

        println("   Build Variant: ${BuildVariant.buildType}")
        println("   Is Debug Build: ${BuildVariant.isDebugBuild}")

        if (config.isDebugMode != BuildVariant.isDebugBuild) {
            println("⚠️  WARNING: Config debug mode (${config.isDebugMode}) != Build variant debug (${BuildVariant.isDebugBuild})")
        }

        if (config.isDebugMode && !config.baseUrl.contains("localhost") && !config.baseUrl.contains("10.0.2.2")) {
            println("⚠️  WARNING: Debug mode but not using local server URL!")
        }
        if (!config.isDebugMode && !config.baseUrl.contains("openf1.org")) {
            println("⚠️  WARNING: Release mode but not using OpenF1 API URL!")
        }

        println("==================================================")
    }
}