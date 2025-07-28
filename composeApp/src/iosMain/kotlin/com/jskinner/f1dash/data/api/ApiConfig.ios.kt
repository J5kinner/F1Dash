package com.jskinner.f1dash.data.api

class IosApiConfig : ApiConfig {
    override val isDebugMode: Boolean = run {
        // For iOS, we'll use a simple check - in production this would be more sophisticated
        // For now, default to debug mode for development
        true  // TODO: Implement proper debug/release detection for iOS
    }

    override val baseUrl: String by lazy {
        if (isDebugMode) {
            try {
                IosEmbeddedServerManager.getBaseUrl()
            } catch (e: Exception) {
                println("Failed to start embedded server: ${e.message}")
                "http://localhost:3000/v1"  // Fallback to external server
            }
        } else {
            "https://api.openf1.org/v1"  // Real OpenF1 API for release
        }
    }
}

object IosEmbeddedServerManager {
    private val server by lazy { com.jskinner.f1dash.server.EmbeddedMockServer() }

    fun getBaseUrl(): String = server.start()
}

actual fun getApiConfig(): ApiConfig = IosApiConfig()