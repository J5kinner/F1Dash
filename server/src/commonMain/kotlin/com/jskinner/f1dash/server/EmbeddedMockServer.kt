package com.jskinner.f1dash.server

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.runBlocking

class EmbeddedMockServer {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private var serverPort: Int = 0

    fun start(): String {
        if (server != null && serverPort > 0) {
            return "http://localhost:$serverPort/v1"
        }

        // Try multiple ports to find an available one
        val portRange = 8080..8090
        for (port in portRange) {
            try {
                server = embeddedServer(CIO, port = port, host = "127.0.0.1") {
                    module()
                }

                runBlocking {
                    server?.start(wait = false)
                    serverPort = port
                }

                return "http://localhost:$serverPort/v1"
            } catch (e: Exception) {
                // Port is in use, try next one
                server?.stop(0, 0)
                server = null
                continue
            }
        }

        // If all ports failed, throw exception
        throw RuntimeException("Could not start embedded server: all ports $portRange are in use")
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        serverPort = 0
    }

    fun isRunning(): Boolean = server != null && serverPort > 0
}