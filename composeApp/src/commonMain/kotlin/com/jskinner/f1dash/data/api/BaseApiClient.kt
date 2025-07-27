package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

abstract class BaseApiClient(val httpClient: HttpClient) {

    companion object {
        private val apiConfig = getApiConfig()
        val BASE_URL = apiConfig.baseUrl
        const val REQUEST_DELAY = 100L
    }

    suspend inline fun <reified T> makeRequest(
        endpoint: String,
        parameters: Map<String, Any> = emptyMap()
    ): ApiResult<T> {
        return try {
            delay(REQUEST_DELAY)
            val url = "$BASE_URL/$endpoint"
            println("🌐 Making API request to: $url")
            if (parameters.isNotEmpty()) {
                println("📋 With parameters: $parameters")
            }

            val response = httpClient.get(url) {
                parameters.forEach { (key, value) ->
                    parameter(key, value.toString())
                }
            }
            println("✅ API request successful: ${response.status}")
            val result = response.body<T>()
            ApiResult.Success(result)
        } catch (e: Exception) {
            println("❌ API request failed: ${e.message}")
            println("🔍 Exception type: ${e::class.simpleName}")
            ApiResult.Error("Failed to connect to $BASE_URL: ${e.message}")
        }
    }

    suspend inline fun <reified T> makeRequest(
        endpoint: String,
        parameters: Map<String, Any> = emptyMap(),
        body: suspend () -> T
    ): ApiResult<T> {
        return try {
            delay(REQUEST_DELAY)
            val url = "$BASE_URL/$endpoint"
            println("🌐 Making API request to: $url")
            if (parameters.isNotEmpty()) {
                println("📋 With parameters: $parameters")
            }

            val response = httpClient.get(url) {
                parameters.forEach { (key, value) ->
                    parameter(key, value.toString())
                }
            }
            println("✅ API request successful: ${response.status}")
            val result = body()
            ApiResult.Success(result)
        } catch (e: Exception) {
            println("❌ API request failed: ${e.message}")
            println("🔍 Exception type: ${e::class.simpleName}")
            ApiResult.Error("Failed to connect to $BASE_URL: ${e.message}")
        }
    }
} 