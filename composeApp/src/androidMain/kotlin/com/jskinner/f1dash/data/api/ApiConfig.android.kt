package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.BuildConfig

class AndroidApiConfig : ApiConfig {
    override val isDebugMode: Boolean = BuildConfig.USE_MOCK_SERVER

    override val baseUrl: String by lazy {
        if (BuildConfig.USE_MOCK_SERVER) {
            try {
                EmbeddedServerManager.getBaseUrl()
            } catch (e: Exception) {
                println("Failed to start embedded server: ${e.message}")
                BuildConfig.API_BASE_URL
            }
        } else {
            BuildConfig.API_BASE_URL
        }
    }
}

object EmbeddedServerManager {
    private val server by lazy { com.jskinner.f1dash.server.EmbeddedMockServer() }

    fun getBaseUrl(): String = server.start()
}

actual fun getApiConfig(): ApiConfig = AndroidApiConfig()