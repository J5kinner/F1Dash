# F1Dash Mock Server

A Kotlin Multiplatform mock server that replicates the OpenF1 API for local development and testing of the F1Dash app.

## Features

- **Full OpenF1 API Compatibility**: All endpoints match the real OpenF1 API structure
- **Shared Data Models**: Uses the same Kotlin data models as the F1Dash app
- **Realistic Mock Data**: Includes realistic F1 driver, session, and race data
- **CORS Enabled**: Ready for local development
- **Query Parameter Support**: Supports all filtering parameters used by the app

## Available Endpoints

All endpoints are available at `http://localhost:3000/v1/`

### Drivers

```
GET /v1/drivers
GET /v1/drivers?session_key=9158
GET /v1/drivers?driver_number=1
GET /v1/drivers?session_key=9158&driver_number=1
```

### Sessions

```
GET /v1/sessions
GET /v1/sessions?year=2024
GET /v1/sessions?session_type=Race
GET /v1/sessions?session_key=9158
```

### Position Data

```
GET /v1/position?session_key=9158
GET /v1/position?session_key=9158&driver_number=1
GET /v1/position?session_key=9158&lap_number=10
```

### Lap Data

```
GET /v1/laps?session_key=9158
GET /v1/laps?session_key=9158&driver_number=1
GET /v1/laps?session_key=9158&lap_number=10
```

### Intervals

```
GET /v1/intervals?session_key=9158
GET /v1/intervals?session_key=9158&driver_number=1
```

### Tire Stints

```
GET /v1/stints?session_key=9158
GET /v1/stints?session_key=9158&driver_number=1
```

### Pit Stops

```
GET /v1/pit?session_key=9158
GET /v1/pit?session_key=9158&driver_number=1
```

### Session Results

```
GET /v1/session_result?session_key=9158
```

## Quick Start

### 1. Start the Server (Debug Mode Only)

```bash
./gradlew :server:jvmRun
```

Or run the shell script:

```bash
cd server
./run.sh
```

The server will start on `http://localhost:3000`

### 2. Automatic Configuration ✨

**No manual configuration needed!** The app automatically switches between:

- **Debug builds**: Uses mock server at `http://localhost:3000` (Android) or `http://10.0.2.2:3000` (Android Emulator)
- **Release builds**: Uses real OpenF1 API at `https://api.openf1.org/v1`

You'll see a log message at app startup indicating which API is being used:

```
🏎️ F1Dash API Configuration: Using MOCK SERVER at http://localhost:3000/v1
```

or

```
🏎️ F1Dash API Configuration: Using PRODUCTION API at https://api.openf1.org/v1
```

### 3. Test the Server

Visit `http://localhost:3000` in your browser to see the server status and available endpoints.

## Development Workflow

### Debug Development

1. Start the mock server: `./gradlew :server:jvmRun`
2. Run your app in debug mode
3. All API calls will go to the local mock server
4. Fast development without internet dependency

### Release Testing

1. Stop the mock server
2. Build your app in release mode
3. All API calls will go to the real OpenF1 API
4. Test with live F1 data

## Mock Data

The server includes realistic mock data for:

- **10 F1 Drivers**: Max Verstappen, Oscar Piastri, Lando Norris, etc.
- **Multiple Sessions**: Race, Qualifying, Sprint sessions
- **Race Data**: 50 laps of position, lap time, and telemetry data
- **Tire Strategies**: Realistic tire compounds and pit stop timings
- **Session Results**: Complete race results with positions and times

## Development

### Project Structure

```
server/
├── src/
│   ├── commonMain/kotlin/
│   │   └── com/jskinner/f1dash/server/
│   │       └── data/
│   │           └── MockDataGenerator.kt
│   └── jvmMain/kotlin/
│       └── com/jskinner/f1dash/server/
│           ├── Server.kt
│           └── routes/
│               └── F1Routes.kt
├── build.gradle.kts
└── README.md
```

### Adding New Mock Data

To add new drivers, sessions, or modify existing data, edit the `MockDataGenerator.kt` file:

```kotlin
// Add new drivers to the drivers list
private val drivers = listOf(
    F1DriverApiResponse(
        driverNumber = 99,
        fullName = "New Driver",
        // ... other properties
    ),
    // ... existing drivers
)
```

### Customizing Response Delays

The server includes no artificial delays by default, but you can add them by modifying the route handlers:

```kotlin
get("/drivers") {
    delay(500) // Add 500ms delay
    val drivers = MockDataGenerator.getDrivers(sessionKey, driverNumber)
    call.respond(drivers)
}
```

## Production Notes

- This server is intended for development and testing only
- All data is generated in-memory and resets on server restart
- No authentication or rate limiting is implemented
- CORS is configured to allow all origins for development ease

## Troubleshooting

### Port Already in Use

If port 3000 is already in use, you can change it in `Server.kt`:

```kotlin
embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
```

### Build Issues

Make sure you're running the server from the project root:

```bash
./gradlew :server:jvmRun
```

### Connection Issues from Android Emulator

Use `10.0.2.2:3000` instead of `localhost:3000` when running on Android emulator:

```kotlin
const val BASE_URL = "http://10.0.2.2:3000/v1"
```