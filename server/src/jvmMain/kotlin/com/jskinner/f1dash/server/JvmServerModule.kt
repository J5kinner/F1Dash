package com.jskinner.f1dash.server

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level

actual fun Application.configurePlatformFeatures() {
    install(CallLogging) {
        level = Level.INFO
    }
}