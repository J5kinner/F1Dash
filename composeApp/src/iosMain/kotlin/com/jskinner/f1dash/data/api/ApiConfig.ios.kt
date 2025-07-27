package com.jskinner.f1dash.data.api

class IosApiConfig : ApiConfig {
    override val isDebugMode: Boolean = run {
        // For iOS, we'll use a simple check - in production this would be more sophisticated
        // For now, default to debug mode for development
        true  // TODO: Implement proper debug/release detection for iOS
    }

    override val baseUrl: String = if (isDebugMode) {
        "http://localhost:3000/v1"  // Mock server for iOS simulator
    } else {
        "https://api.openf1.org/v1"  // Real OpenF1 API for release
    }
}

actual fun getApiConfig(): ApiConfig = IosApiConfig()