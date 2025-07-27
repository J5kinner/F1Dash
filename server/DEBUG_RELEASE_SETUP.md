# 🏎️ Debug/Release API Configuration

Your F1Dash app now automatically switches between the mock server and real OpenF1 API based on build type!

## ✅ **How It Works**

### **🔧 Debug Builds (Development)**

- **Android**: Uses mock server at `http://10.0.2.2:3000/v1` (Android Emulator compatible)
- **iOS**: Uses mock server at `http://localhost:3000/v1` (iOS Simulator compatible)
- **Fast development** with realistic mock data
- **No internet dependency** required

### **🚀 Release Builds (Production)**

- **All platforms**: Uses real OpenF1 API at `https://api.openf1.org/v1`
- **Live F1 data** from official sources
- **Production-ready** API endpoints

## 🎯 **Development Workflow**

### Debug Development

1. **Start mock server**: `./gradlew :server:jvmRun`
2. **Run app in debug mode**: Android Studio → Run (green play button)
3. **See log**: `🏎️ F1Dash API Configuration: Using MOCK SERVER`
4. All API calls go to your local mock server

### Release Testing

1. **Stop mock server** (if running)
2. **Build release**: Android Studio → Build → Generate Signed Bundle/APK
3. **See log**: `🏎️ F1Dash API Configuration: Using PRODUCTION API`
4. All API calls go to real OpenF1 API

## 🔧 **Technical Implementation**

### Files Modified:

- `composeApp/src/commonMain/kotlin/com/jskinner/f1dash/data/api/ApiConfig.kt` - Interface
- `composeApp/src/androidMain/kotlin/com/jskinner/f1dash/data/api/ApiConfig.android.kt` - Android impl
- `composeApp/src/iosMain/kotlin/com/jskinner/f1dash/data/api/ApiConfig.ios.kt` - iOS impl
- `composeApp/src/commonMain/kotlin/com/jskinner/f1dash/data/api/BaseApiClient.kt` - Updated to use config
- `composeApp/build.gradle.kts` - Added BuildConfig fields
- `composeApp/src/debug/AndroidManifest.xml` - Debug-only cleartext traffic permission
- `composeApp/src/debug/res/xml/network_security_config.xml` - HTTP security config

### Build Configuration:

```kotlin
buildTypes {
    getByName("debug") {
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/v1\"")
        buildConfigField("boolean", "USE_MOCK_SERVER", "true")
    }
    getByName("release") {
        buildConfigField("String", "API_BASE_URL", "\"https://api.openf1.org/v1\"")
        buildConfigField("boolean", "USE_MOCK_SERVER", "false")
    }
}
```

## 🐛 **Troubleshooting**

### ❌ "Cleartext HTTP traffic not permitted"

**Fixed!** This error occurs because Android blocks HTTP traffic by default. Solution implemented:

- ✅ Added network security config for debug builds only
- ✅ Allows HTTP traffic to `10.0.2.2`, `localhost`, and `127.0.0.1` in debug
- ✅ Release builds remain secure (HTTPS only)

### Mock Server Not Working?

1. Check server is running: `curl http://localhost:3000/health`
2. For Android Emulator, server must be at `10.0.2.2:3000`
3. For iOS Simulator, server must be at `localhost:3000`
4. **Clean and rebuild** after adding network config: `./gradlew clean assembleDebug`

### Wrong API Being Used?

1. Check the log message at app startup
2. Verify you're running debug vs release build
3. Clean and rebuild: `./gradlew clean build`

### Network Issues in Release?

1. Ensure internet connection is available
2. Check OpenF1 API status: `curl https://api.openf1.org/v1/drivers`
3. Verify no firewall blocking external requests

## 📱 **Platform Notes**

### Android

- ✅ Fully automatic debug/release detection
- ✅ Uses `BuildConfig.DEBUG` flag
- ✅ Emulator-compatible URLs

### iOS

- ⚠️ Currently defaults to debug mode
- 🔄 TODO: Implement proper Xcode build configuration detection
- ✅ Simulator-compatible URLs

## 🎉 **Benefits**

- **Zero manual configuration** - just run debug or release builds
- **Fast development** with mock data
- **Easy testing** with real API data
- **No code changes** needed between debug/release
- **Visual feedback** via log messages
- **Platform optimized** URLs for emulators/simulators