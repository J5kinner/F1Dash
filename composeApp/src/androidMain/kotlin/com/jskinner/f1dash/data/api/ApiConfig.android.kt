package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.BuildConfig

class AndroidApiConfig : ApiConfig {
    override val isDebugMode: Boolean = BuildConfig.USE_MOCK_SERVER

    override val baseUrl: String = if (isDebugMode) {
        // Try multiple possible addresses for Android emulator
        // 10.0.2.2 is the standard, but some configurations use different addresses
        getLocalServerUrl()
    } else {
        BuildConfig.API_BASE_URL
    }

    private fun getLocalServerUrl(): String {
        // You can manually change this if 10.0.2.2 doesn't work in your setup
        val possibleHosts = listOf(
            "10.0.2.2:3000",    // Standard Android emulator
            "10.0.3.2:3000",    // Some Android emulator configurations  
            "192.168.1.2:3000", // If running on physical device and server on same network
            "localhost:3000"     // If somehow localhost works
        )

        // For now, use the standard one - you can modify this for testing
        return "http://10.0.2.2:3000/v1"
    }
}

actual fun getApiConfig(): ApiConfig = AndroidApiConfig()