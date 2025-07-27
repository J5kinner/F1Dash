package com.jskinner.f1dash.server.routes

import com.jskinner.f1dash.server.data.MockDataGenerator
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureF1Routes() {
    routing {
        route("/v1") {

            get("/drivers") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull()
                val driverNumber = call.parameters["driver_number"]?.toIntOrNull()

                val drivers = MockDataGenerator.getDrivers(sessionKey, driverNumber)
                call.respond(drivers)
            }

            get("/sessions") {
                val year = call.parameters["year"]?.toIntOrNull()
                val sessionType = call.parameters["session_type"]
                val sessionKey = call.parameters["session_key"]?.toIntOrNull()

                val sessions = MockDataGenerator.getSessions(year, sessionType, sessionKey)
                call.respond(sessions)
            }

            get("/position") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull() ?: 9250
                val driverNumber = call.parameters["driver_number"]?.toIntOrNull()
                val lapNumber = call.parameters["lap_number"]?.toIntOrNull()

                val positions = MockDataGenerator.generatePositions(sessionKey, driverNumber, lapNumber)
                call.respond(positions)
            }

            get("/laps") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull() ?: 9250
                val driverNumber = call.parameters["driver_number"]?.toIntOrNull()
                val lapNumber = call.parameters["lap_number"]?.toIntOrNull()

                val laps = MockDataGenerator.generateLaps(sessionKey, driverNumber, lapNumber)
                call.respond(laps)
            }

            get("/intervals") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull() ?: 9250
                val driverNumber = call.parameters["driver_number"]?.toIntOrNull()

                val intervals = MockDataGenerator.generateIntervals(sessionKey, driverNumber)
                call.respond(intervals)
            }

            get("/stints") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull() ?: 9250
                val driverNumber = call.parameters["driver_number"]?.toIntOrNull()

                val stints = MockDataGenerator.generateStints(sessionKey, driverNumber)
                call.respond(stints)
            }

            get("/pit") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull() ?: 9250
                val driverNumber = call.parameters["driver_number"]?.toIntOrNull()

                val pitStops = MockDataGenerator.generatePitStops(sessionKey, driverNumber)
                call.respond(pitStops)
            }

            get("/session_result") {
                val sessionKey = call.parameters["session_key"]?.toIntOrNull() ?: 9250

                val results = MockDataGenerator.generateSessionResults(sessionKey)
                call.respond(results)
            }
        }
    }
}