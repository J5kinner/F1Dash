package com.jskinner.f1dash.server

import com.jskinner.f1dash.server.routes.configureF1Routes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

expect fun Application.configurePlatformFeatures()

fun Application.module() {
    configureKtorFeatures()
    configurePlatformFeatures()
    configureF1Routes()

    routing {
        get("/") {
            call.respondText(
                "F1Dash Mock Server is running!\n\nAvailable endpoints:\n" +
                        "- GET /v1/drivers\n" +
                        "- GET /v1/sessions\n" +
                        "- GET /v1/position\n" +
                        "- GET /v1/laps\n" +
                        "- GET /v1/intervals\n" +
                        "- GET /v1/stints\n" +
                        "- GET /v1/pit\n" +
                        "- GET /v1/session_result\n\n" +
                        "Server running on embedded mock server",
                ContentType.Text.Plain
            )
        }

        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }
    }
}

fun Application.configureKtorFeatures() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        anyHost()
    }
}