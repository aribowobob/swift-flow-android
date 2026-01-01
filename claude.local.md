# Swift Flow Android - Project Knowledge Base

## Project Overview
Swift Flow Android is the mobile frontend for the Swift Flow logistics and distribution tracking system. This app enables sales representatives to log merchandise deliveries and supervisors to monitor distribution across regions.

## Technology Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Build System**: Gradle 8.2 with Kotlin DSL

### Architecture & Patterns
- **Architecture**: Clean Architecture with MVVM
- **Layers**:
  - **Presentation**: UI (Jetpack Compose) + ViewModels
  - **Domain**: Business logic, models, repository interfaces
  - **Data**: Repository implementations, API clients, local database

### Key Libraries

#### Dependency Injection
- **Hilt** 2.50 - Compile-time dependency injection

#### Networking
- **Retrofit** 2.9.0 - REST API client
- **OkHttp** 4.12.0 - HTTP client with interceptors
- **Gson** - JSON serialization/deserialization

#### Local Storage
- **Room** 2.6.1 - SQLite database abstraction
- **DataStore** 1.0.0 - Key-value storage (used for auth tokens)

#### UI Components
- **Jetpack Compose BOM** 2024.02.00
- **Material 3** - Material Design components
- **Navigation Compose** 2.7.7 - Navigation between screens
- **Coil** 2.5.0 - Image loading and caching

#### Camera & Location
- **CameraX** 1.3.1 - Camera functionality
- **Play Services Location** 21.1.0 - GPS coordinates

#### Lifecycle
- **Lifecycle ViewModel Compose** 2.7.0
- **Lifecycle Runtime Compose** 2.7.0

## Project Structure

```
app/src/main/java/com/swiftflow/
├── data/
│   ├── local/
│   │   ├── dao/              # Room DAOs (future)
│   │   └── entity/           # Room entities (future)
│   ├── remote/
│   │   ├── api/              # Retrofit API interfaces
│   │   │   ├── AuthApi.kt
│   │   │   ├── ProductApi.kt
│   │   │   └── DeliveryApi.kt
│   │   └── dto/              # Data Transfer Objects (future)
│   └── repository/           # Repository implementations
│       ├── AuthRepositoryImpl.kt
│       ├── ProductRepositoryImpl.kt
│       └── DeliveryRepositoryImpl.kt
├── domain/
│   ├── model/                # Domain models
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   └── Delivery.kt
│   ├── repository/           # Repository interfaces
│   │   ├── AuthRepository.kt
│   │   ├── ProductRepository.kt
│   │   └── DeliveryRepository.kt
│   └── usecase/              # Business logic use cases (future)
├── presentation/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   └── LoginScreen.kt
│   ├── delivery/
│   │   └── DeliveryViewModel.kt
│   ├── product/
│   │   └── ProductViewModel.kt
│   ├── common/
│   │   ├── components/       # Reusable composables
│   │   └── theme/            # App theme configuration
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   └── navigation/
│       └── NavGraph.kt       # Navigation routes
├── di/                       # Hilt modules
│   ├── NetworkModule.kt      # Network dependencies
│   └── RepositoryModule.kt   # Repository bindings
├── utils/                    # Utility classes
│   ├── AuthInterceptor.kt    # JWT token injection
│   ├── TokenManager.kt       # Token storage/retrieval
│   └── Resource.kt           # API response wrapper
├── MainActivity.kt           # Main entry point
└── SwiftFlowApp.kt          # Application class (Hilt)
```

## Backend Integration

### API Configuration
- **Base URL**: `http://10.0.2.2:8080/api/` (for emulator)
  - `10.0.2.2` is Android emulator's alias for `localhost`
  - For physical devices, use your computer's IP: `http://192.168.x.x:8080/api/`
- **Authentication**: JWT Bearer tokens
- **API Endpoints**:
  - `POST /auth/login` - User login
  - `POST /auth/register` - User registration
  - `GET /products` - List products
  - `GET /deliveries` - List deliveries (with filters)
  - `POST /deliveries` - Create delivery
  - `GET /deliveries/:id` - Get delivery details
  - `POST /deliveries/:id/photos` - Upload photo

### Authentication Flow
1. User logs in via `LoginScreen`
2. `AuthViewModel` calls `AuthRepository.login()`
3. Backend returns JWT token + user info
4. `TokenManager` stores token in DataStore
5. `AuthInterceptor` automatically adds token to all subsequent API requests
6. Navigation redirects to delivery list screen

### Data Models

#### User
```kotlin
data class User(
    val id: Int,
    val username: String,
    val role: UserRole,        // SALES or SUPERVISOR
    val initial: String,        // 3-character identifier
    val isActive: Boolean,
    val createdAt: String
)
```

#### Product
```kotlin
data class Product(
    val id: Int,
    val sku: String,
    val name: String,
    val unit: String
)
```

#### Delivery
```kotlin
data class Delivery(
    val id: Int,
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val lat: String?,
    val lon: String?,
    val notes: String?,
    val status: DeliveryStatus,  // READY, DONE, BROKEN, NEED_TO_CONFIRM
    val createdAt: String,
    val createdBy: Int,
    val updatedAt: String,
    val updatedBy: Int
)
```

## Key Features

### Implemented ✅
- User authentication (login)
- JWT token management with auto-injection
- MVVM architecture setup
- Navigation framework
- Repository pattern for data access
- Network error handling with Resource wrapper

### In Progress 🚧
- Delivery list screen
- Create delivery form
- Product selection UI

### Planned 📋
- Camera integration for delivery photos
- GPS location capture
- Offline support with Room database
- Image upload to backend
- Supervisor analytics dashboard
- Delivery filtering by region/status

## Development Guidelines

### State Management
- Use `StateFlow` in ViewModels for UI state
- Collect state in Composables using `collectAsState()`
- Emit state updates using `_state.update { ... }`

### API Calls
- All API calls return `Flow<Resource<T>>`
- `Resource` has three states: `Loading`, `Success`, `Error`
- ViewModels collect and update UI state accordingly

### Navigation
- Use `NavController` for screen navigation
- Define routes in `Screen` sealed class
- Pass parameters via navigation arguments

### Dependency Injection
- Use `@HiltViewModel` for ViewModels
- Use `@Inject` constructor for dependencies
- Register modules with `@InstallIn(SingletonComponent::class)`

### Composable Best Practices
- Keep composables small and focused
- Extract reusable components to `presentation/common/components/`
- Use `Modifier` parameter for customization
- Preview composables with `@Preview`

## Build Configuration

### Variants
- **debug**: Development build with logging
- **release**: Production build with ProGuard

### ProGuard Rules
- Keep Retrofit/OkHttp classes
- Keep Gson serialization
- Keep Room entities
- Keep data models

## Testing Strategy

### Unit Tests
- Test ViewModels with mocked repositories
- Test repositories with mocked API services
- Located in `app/src/test/`

### Instrumented Tests
- Test Composable UI components
- Test navigation flows
- Located in `app/src/androidTest/`

## Permissions

### Required Permissions
- `INTERNET` - API communication
- `ACCESS_NETWORK_STATE` - Check network availability
- `ACCESS_FINE_LOCATION` - GPS coordinates for deliveries
- `ACCESS_COARSE_LOCATION` - Approximate location
- `CAMERA` - Take delivery photos
- `READ_EXTERNAL_STORAGE` - Read images (API ≤ 32)
- `WRITE_EXTERNAL_STORAGE` - Save images (API ≤ 28)

## Common Tasks

### Add New Screen
1. Create screen composable in `presentation/<feature>/`
2. Add route to `Screen` sealed class in `NavGraph.kt`
3. Add composable to `NavHost` in `NavGraph.kt`
4. Navigate using `navController.navigate(Screen.YourScreen.route)`

### Add New API Endpoint
1. Add function to appropriate API interface in `data/remote/api/`
2. Update repository interface in `domain/repository/`
3. Implement in repository in `data/repository/`
4. Call from ViewModel

### Add New Model
1. Create data class in `domain/model/`
2. Ensure fields match backend schema
3. Add to Gson/Room if needed

## Troubleshooting

### Can't connect to backend
- Ensure backend is running: `cd ../swift-flow-be && cargo run`
- Check API_BASE_URL in `app/build.gradle.kts`
- For emulator: use `10.0.2.2`
- For physical device: use your computer's local IP
- Enable cleartext traffic in AndroidManifest (already configured)

### Gradle sync issues
- File → Invalidate Caches / Restart
- Delete `.gradle` and `build` directories
- Sync project with Gradle files

### Hilt errors
- Ensure `@HiltAndroidApp` on Application class
- Ensure `@AndroidEntryPoint` on Activity
- Ensure `@HiltViewModel` on ViewModels
- Clean and rebuild project

## Resources

### Backend Documentation
See `../swift-flow-be/README.md` for backend API documentation

### Android Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Retrofit](https://square.github.io/retrofit/)
- [Room](https://developer.android.com/training/data-storage/room)
