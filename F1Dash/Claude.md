# KMP Architecture Guide - OrbitMVI + Koin + Ktor + Compose

## Dependencies Setup

### shared/build.gradle.kts
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            
            // Ktor - Networking
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            implementation("io.ktor:ktor-client-logging:2.3.7")
            
            // Koin - Dependency Injection
            implementation("io.insert-koin:koin-core:3.5.0")
            implementation("io.insert-koin:koin-compose:1.1.0")
            
            // OrbitMVI - State Management
            implementation("org.orbit-mvi:orbit-viewmodel:6.1.0")
            implementation("org.orbit-mvi:orbit-compose:6.1.0")
            
            // Navigation
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
            
            // Image Loading
            implementation("io.coil-kt:coil-compose:2.5.0")
            
            // Material3 (includes pull-to-refresh)
            implementation("androidx.compose.material3:material3:1.1.2")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            
            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            
            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
        }
        
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-android:2.3.7")
            implementation("androidx.activity:activity-compose:1.8.2")
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.ktor:ktor-client-mock:2.3.7")
            implementation("org.orbit-mvi:orbit-test:6.1.0")
            implementation("androidx.compose.ui:ui-test-junit4:1.5.11")
        }
    }
}
```

## Project Structure

```
shared/
├── commonMain/kotlin/
│   ├── data/
│   │   ├── api/
│   │   ├── repository/
│   │   └── models/
│   ├── domain/
│   │   ├── models/
│   │   └── repository/
│   ├── presentation/
│   │   └── viewmodels/
│   ├── ui/
│   │   ├── screens/
│   │   ├── components/
│   │   └── theme/
│   └── di/
│       └── modules/
└── androidMain/kotlin/
```

## Data Layer

### API Models
```kotlin
// shared/src/commonMain/kotlin/data/models/ApiModels.kt
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiResponse<T>(
    val data: T,
    val status: String,
    val message: String? = null
)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}
```

### Ktor Client Setup
```kotlin
// shared/src/commonMain/kotlin/data/api/ApiClient.kt
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class ApiClient(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://api.example.com"
        private const val REQUEST_DELAY = 100L
    }
    
    suspend fun getUsers(): ApiResult<List<User>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/users")
            val users = response.body<List<User>>()
            ApiResult.Success(users)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getUser(id: Int): ApiResult<User> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/users/$id")
            val user = response.body<User>()
            ApiResult.Success(user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}
```

### Repository Implementation
```kotlin
// shared/src/commonMain/kotlin/data/repository/UserRepositoryImpl.kt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface UserRepository {
    suspend fun getUsers(): Flow<ApiResult<List<User>>>
    suspend fun getUser(id: Int): Flow<ApiResult<User>>
}

class UserRepositoryImpl(
    private val apiClient: ApiClient
) : UserRepository {
    
    override suspend fun getUsers(): Flow<ApiResult<List<User>>> = flow {
        emit(ApiResult.Loading)
        val result = apiClient.getUsers()
        emit(result)
    }
    
    override suspend fun getUser(id: Int): Flow<ApiResult<User>> = flow {
        emit(ApiResult.Loading)
        val result = apiClient.getUser(id)
        emit(result)
    }
}
```

## Domain Layer

### Domain Models
```kotlin
// shared/src/commonMain/kotlin/domain/models/DomainModels.kt
data class UserProfile(
    val id: Int,
    val displayName: String,
    val email: String,
    val isActive: Boolean
)

// Extension function to map API to Domain
fun User.toDomainModel() = UserProfile(
    id = id,
    displayName = name,
    email = email,
    isActive = true
)
```

## Presentation Layer - OrbitMVI

### ViewModel with OrbitMVI
```kotlin
// shared/src/commonMain/kotlin/presentation/UserListViewModel.kt
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class UserListState(
    val users: List<UserProfile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UserListSideEffect {
    data class ShowToast(val message: String) : UserListSideEffect()
    data class NavigateToDetail(val userId: Int) : UserListSideEffect()
}

class UserListViewModel(
    private val userRepository: UserRepository
) : ViewModel(), ContainerHost<UserListState, UserListSideEffect> {

    override val container: Container<UserListState, UserListSideEffect> =
        container(UserListState())

    fun loadUsers() = intent {
        userRepository.getUsers().collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    reduce { state.copy(isLoading = true, error = null) }
                }
                is ApiResult.Success -> {
                    reduce { 
                        state.copy(
                            users = result.data.map { it.toDomainModel() },
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    reduce { 
                        state.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    postSideEffect(UserListSideEffect.ShowToast(result.message))
                }
            }
        }
    }

    fun onUserClick(userId: Int) = intent {
        postSideEffect(UserListSideEffect.NavigateToDetail(userId))
    }

    fun onRefresh() = intent {
        loadUsers()
    }
}
```

## Dependency Injection - Koin

### Koin Modules
```kotlin
// shared/src/commonMain/kotlin/di/NetworkModule.kt
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
    
    single<ApiClient> { ApiClient(get()) }
}

// shared/src/commonMain/kotlin/di/RepositoryModule.kt
val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(get()) }
}

// shared/src/commonMain/kotlin/di/ViewModelModule.kt
val viewModelModule = module {
    factory { UserListViewModel(get()) }
}

// shared/src/commonMain/kotlin/di/AppModule.kt
val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule
)
```

### Koin Initialization
```kotlin
// shared/src/commonMain/kotlin/di/KoinInitializer.kt
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModules)
}

// Platform-specific initialization
// shared/src/commonMain/kotlin/di/KoinInitializer.kt
fun initKoin() = initKoin {}
```

## Navigation

### Basic Navigation Setup
```kotlin
// shared/src/commonMain/kotlin/ui/navigation/AppNavigation.kt
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            UserListScreen(
                onNavigateToDetail = { userId ->
                    navController.navigate("detail/$userId")
                }
            )
        }
        
        composable("detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            UserDetailScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Navigation routes
object Routes {
    const val DASHBOARD = "dashboard"
    const val DETAIL = "detail"
    
    fun detail(userId: Int) = "detail/$userId"
}
```

### Bottom Navigation
```kotlin
// shared/src/commonMain/kotlin/ui/navigation/BottomNavigation.kt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", Icons.Default.Home, "Dashboard"),
    BottomNavItem("settings", Icons.Default.Settings, "Settings")
)
```

## Image Loading with Coil

### Basic Image Loading
```kotlin
// shared/src/commonMain/kotlin/ui/components/ImageComponents.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage

@Composable
fun UserAvatar(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            // Fallback UI for failed loads
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default avatar"
                )
            }
        }
    )
}

@Composable
fun NetworkImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
```

### Image with Placeholder
```kotlin
@Composable
fun ImageWithPlaceholder(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = { 
        CircularProgressIndicator() 
    }
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        loading = { placeholder() },
        error = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error loading image"
            )
        }
    )
}
```

## Pull-to-Refresh

### Basic Pull-to-Refresh
```kotlin
// shared/src/commonMain/kotlin/ui/components/PullRefreshComponents.kt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = Modifier
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        content()
        
        if (pullRefreshState.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

### Advanced Pull-to-Refresh with Custom Indicator
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = modifier.nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        content()
        
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState,
        )
    }
}
```

## Complete Screen Example with All Features

### Enhanced User List Screen
```kotlin
// shared/src/commonMain/kotlin/ui/screens/EnhancedUserListScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedUserListScreen(
    viewModel: UserListViewModel = koinInject(),
    onNavigateToDetail: (Int) -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    val state by viewModel.collectAsState()
    
    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is UserListSideEffect.ShowToast -> onShowToast(sideEffect.message)
            is UserListSideEffect.NavigateToDetail -> onNavigateToDetail(sideEffect.userId)
        }
    }
    
    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
    
    PullRefreshContent(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.onRefresh() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Users",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when {
                state.isLoading && state.users.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                state.error != null && state.users.isEmpty() -> {
                    Column {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.onRefresh() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                else -> {
                    LazyColumn {
                        items(state.users) { user ->
                            EnhancedUserItem(
                                user = user,
                                onClick = { viewModel.onUserClick(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedUserItem(
    user: UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                imageUrl = user.avatarUrl,
                contentDescription = "${user.displayName} avatar",
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (user.isActive) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

### App with Navigation
```kotlin
// shared/src/commonMain/kotlin/ui/App.kt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        val navController = rememberNavController()
        
        AppTheme {
            Scaffold(
                bottomBar = { 
                    AppBottomNavigation(navController) 
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    AppNavigation()
                }
            }
        }
    }
}
```

## UI Layer - Compose

### Screen with OrbitMVI Integration
```kotlin
// shared/src/commonMain/kotlin/ui/screens/UserListScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun UserListScreen(
    viewModel: UserListViewModel = koinInject(),
    onNavigateToDetail: (Int) -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    val state by viewModel.collectAsState()
    
    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is UserListSideEffect.ShowToast -> onShowToast(sideEffect.message)
            is UserListSideEffect.NavigateToDetail -> onNavigateToDetail(sideEffect.userId)
        }
    }
    
    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Users",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            state.error != null -> {
                Column {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.onRefresh() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
            
            else -> {
                LazyColumn {
                    items(state.users) { user ->
                        UserItem(
                            user = user,
                            onClick = { viewModel.onUserClick(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

### App Entry Point
```kotlin
// shared/src/commonMain/kotlin/ui/App.kt
import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        AppTheme {
            UserListScreen()
        }
    }
}
```

## Testing

### ViewModel Testing with OrbitMVI
```kotlin
// shared/src/commonTest/kotlin/presentation/UserListViewModelTest.kt
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserListViewModelTest {
    
    private val mockRepository = MockUserRepository()
    private val viewModel = UserListViewModel(mockRepository)
    
    @Test
    fun `loadUsers should update state correctly`() = runTest {
        // Given
        val expectedUsers = listOf(
            User(1, "John", "john@email.com"),
            User(2, "Jane", "jane@email.com")
        )
        mockRepository.setUsers(expectedUsers)
        
        // When & Then
        viewModel.test(this) {
            expectInitialState()
            
            containerHost.loadUsers()
            
            expectState { state ->
                assertTrue(state.isLoading)
            }
            
            expectState { state ->
                assertEquals(expectedUsers.size, state.users.size)
                assertEquals(false, state.isLoading)
                assertEquals(null, state.error)
            }
        }
    }
}
```

### UI Testing
```kotlin
// shared/src/commonTest/kotlin/ui/UserListScreenTest.kt
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class UserListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userListScreen_displaysUsers() {
        composeTestRule.setContent {
            UserListScreen()
        }
        
        composeTestRule
            .onNodeWithText("Users")
            .assertIsDisplayed()
    }
}
```

## Android Integration

### MainActivity.kt
```kotlin
// androidApp/src/main/kotlin/MainActivity.kt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initKoin()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}
```

## Build Commands

```bash
# Clean build
./gradlew clean

# Build shared module
./gradlew :shared:build

# Run tests
./gradlew :shared:testDebugUnitTest

# Install on device
./gradlew :androidApp:installDebug

# Generate release
./gradlew :androidApp:bundleRelease
```

## Key Patterns

### 1. State Management with OrbitMVI
- Use `intent` for actions
- Use `reduce` for state updates
- Use `postSideEffect` for navigation/one-time events

### 2. Dependency Injection with Koin
- Organize modules by layer (network, repository, viewmodel)
- Use `koinInject()` in Compose
- Initialize Koin in main activity

### 3. Network with Ktor
- Configure client once, reuse everywhere
- Handle errors gracefully
- Add logging for debugging

### 4. Error Handling
- Use sealed classes for results
- Show loading states
- Provide retry mechanisms

This architecture provides clean separation of concerns, testability, and maintainability for KMP projects.