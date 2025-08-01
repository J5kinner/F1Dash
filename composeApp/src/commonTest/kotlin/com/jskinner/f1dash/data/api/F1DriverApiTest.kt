package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F1DriverApiTest {

    private val mockDriversResponse = """
        [
            {
                "meeting_key": 1234,
                "session_key": 5678,
                "driver_number": 1,
                "broadcast_name": "M VERSTAPPEN",
                "full_name": "Max Verstappen",
                "name_acronym": "VER",
                "team_name": "Red Bull Racing",
                "team_colour": "3671c6",
                "first_name": "Max",
                "last_name": "Verstappen",
                "headshot_url": "https://example.com/headshot.jpg",
                "country_code": "NED"
            },
            {
                "meeting_key": 1234,
                "session_key": 5678,
                "driver_number": 44,
                "broadcast_name": "L HAMILTON",
                "full_name": "Lewis Hamilton",
                "name_acronym": "HAM",
                "team_name": "Mercedes",
                "team_colour": "6cd3bf",
                "first_name": "Lewis",
                "last_name": "Hamilton",
                "headshot_url": "https://example.com/headshot2.jpg",
                "country_code": "GBR"
            }
        ]
    """.trimIndent()

    private val singleDriverResponse = """
        [
            {
                "meeting_key": 1234,
                "session_key": 5678,
                "driver_number": 1,
                "broadcast_name": "M VERSTAPPEN",
                "full_name": "Max Verstappen",
                "name_acronym": "VER",
                "team_name": "Red Bull Racing",
                "team_colour": "3671c6",
                "first_name": "Max",
                "last_name": "Verstappen",
                "headshot_url": "https://example.com/headshot.jpg",
                "country_code": "NED"
            }
        ]
    """.trimIndent()

    private fun createMockHttpClient(response: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = response,
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Test
    fun `getDrivers returns success when API call succeeds`() = runTest {
        // Given
        val mockHttpClient = createMockHttpClient(mockDriversResponse)
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDrivers()

        // Then
        assertTrue(result is ApiResult.Success)
        val drivers = result.data
        assertEquals(2, drivers.size)

        val firstDriver = drivers[0]
        assertEquals(1234, firstDriver.meetingKey)
        assertEquals(5678, firstDriver.sessionKey)
        assertEquals(1, firstDriver.driverNumber)
        assertEquals("M VERSTAPPEN", firstDriver.broadcastName)
        assertEquals("Max Verstappen", firstDriver.fullName)
        assertEquals("VER", firstDriver.nameAcronym)
        assertEquals("Red Bull Racing", firstDriver.teamName)
        assertEquals("3671c6", firstDriver.teamColour)
        assertEquals("Max", firstDriver.firstName)
        assertEquals("Verstappen", firstDriver.lastName)
        assertEquals("https://example.com/headshot.jpg", firstDriver.headshotUrl)
        assertEquals("NED", firstDriver.countryCode)
    }

    @Test
    fun `getDrivers returns error when API call fails`() = runTest {
        // Given
        val mockHttpClient = createMockHttpClient("", HttpStatusCode.InternalServerError)
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDrivers()

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue(result.message.isNotEmpty())
    }

    @Test
    fun `getDriversForSession returns success with session parameter`() = runTest {
        // Given
        val mockHttpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    // Verify session_key parameter is included
                    val sessionKey = request.url.parameters["session_key"]
                    assertEquals("5678", sessionKey)

                    respond(
                        content = mockDriversResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDriversForSession(5678)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals(2, result.data.size)
    }

    @Test
    fun `getDriversForSession returns error when API call fails`() = runTest {
        // Given
        val mockHttpClient = createMockHttpClient("", HttpStatusCode.BadRequest)
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDriversForSession(5678)

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue(result.message.isNotEmpty())
    }

    @Test
    fun `getDriverByNumber returns success when driver exists`() = runTest {
        // Given
        val mockHttpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    // Verify driver_number parameter is included
                    val driverNumber = request.url.parameters["driver_number"]
                    assertEquals("1", driverNumber)

                    respond(
                        content = singleDriverResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDriverByNumber(1)

        // Then
        assertTrue(result is ApiResult.Success)
        val driver = result.data
        assertEquals(1, driver.driverNumber)
        assertEquals("Max Verstappen", driver.fullName)
    }

    @Test
    fun `getDriverByNumber with sessionKey includes both parameters`() = runTest {
        // Given
        val mockHttpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    // Verify both parameters are included
                    val driverNumber = request.url.parameters["driver_number"]
                    val sessionKey = request.url.parameters["session_key"]
                    assertEquals("1", driverNumber)
                    assertEquals("5678", sessionKey)

                    respond(
                        content = singleDriverResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDriverByNumber(1, 5678)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals(1, result.data.driverNumber)
    }

    @Test
    fun `getDriverByNumber returns error when driver not found`() = runTest {
        // Given
        val mockHttpClient = createMockHttpClient("[]") // Empty array
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDriverByNumber(999)

        // Then
        assertTrue(result is ApiResult.Error)
        assertEquals("Driver not found", result.message)
    }

    @Test
    fun `getDriverByNumber returns error when API call fails`() = runTest {
        // Given
        val mockHttpClient = createMockHttpClient("", HttpStatusCode.NotFound)
        val apiClient = F1DriverApi(mockHttpClient)

        // When
        val result = apiClient.getDriverByNumber(1)

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue(result.message.isNotEmpty())
    }
}