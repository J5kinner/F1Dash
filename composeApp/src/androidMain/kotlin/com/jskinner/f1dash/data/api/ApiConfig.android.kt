package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.BuildConfig

class AndroidApiConfig : ApiConfig {
    override val isDebugMode: Boolean = BuildConfig.USE_MOCK_SERVER

    private fun logBuildVariant() {
        println("🤖 Android BuildVariant Detection:")
        println("   Build Type: ${BuildVariant.buildType}")
        println("   Is Debug Build: ${BuildVariant.isDebugBuild}")
        println("   USE_MOCK_SERVER: ${BuildConfig.USE_MOCK_SERVER}")

        if (BuildVariant.isDebugBuild != BuildConfig.DEBUG) {
            println("⚠️  WARNING: BuildVariant.isDebugBuild (${BuildVariant.isDebugBuild}) != BuildConfig.DEBUG (${BuildConfig.DEBUG})")
        }
    }

    override val baseUrl: String by lazy {
        logBuildVariant()
        println("🤖 Android Build Configuration:")
        println("   USE_MOCK_SERVER: ${BuildConfig.USE_MOCK_SERVER}")
        println("   API_BASE_URL: ${BuildConfig.API_BASE_URL}")
        println("   Build Type: ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")
        
        if (BuildConfig.USE_MOCK_SERVER) {
            try {
                val serverUrl = EmbeddedServerManager.getBaseUrl()
                println("   Using Embedded Server: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                println("❌ Failed to start embedded server: ${e.message}")
                println("   Falling back to: ${BuildConfig.API_BASE_URL}")
                BuildConfig.API_BASE_URL
            }
        } else {
            println("   Using Real API: ${BuildConfig.API_BASE_URL}")
            BuildConfig.API_BASE_URL
        }
    }
}

object EmbeddedServerManager {
    private val server by lazy { com.jskinner.f1dash.server.EmbeddedMockServer() }

    fun getBaseUrl(): String = server.start()
}

actual fun getApiConfig(): ApiConfig = AndroidApiConfig()