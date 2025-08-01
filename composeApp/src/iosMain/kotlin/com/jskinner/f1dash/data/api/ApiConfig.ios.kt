package com.jskinner.f1dash.data.api

class IosApiConfig : ApiConfig {
    override val isDebugMode: Boolean = BuildVariant.isDebugBuild

    override val baseUrl: String = if (isDebugMode) {
        "http://localhost:3000/v1"
    } else {
        "https://api.openf1.org/v1"
    }
}

actual fun getApiConfig(): ApiConfig = IosApiConfig()