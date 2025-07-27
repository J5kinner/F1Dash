package com.jskinner.f1dash.data.api

object ApiLogger {
    fun logApiConfiguration() {
        val config = getApiConfig()
        val mode = if (config.isDebugMode) "MOCK SERVER" else "PRODUCTION API"
        println("🏎️ F1Dash API Configuration: Using $mode at ${config.baseUrl}")
    }
}