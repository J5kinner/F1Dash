package com.jskinner.f1dash.data.api

interface ApiConfig {
    val baseUrl: String
    val isDebugMode: Boolean
}

expect fun getApiConfig(): ApiConfig