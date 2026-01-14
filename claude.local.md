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
- **CameraX** 1.3.1 - Camera functionality (not yet used)
- **ExifInterface** 1.3.7 - Extract GPS data from photos
- **Android Geocoder** - Built-in reverse geocoding (no API key needed)

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
│   │   ├── DeliveryViewModel.kt
│   │   ├── DeliveryDetailViewModel.kt
│   │   ├── DeliveryListScreen.kt
│   │   ├── DeliveryDetailScreen.kt
│   │   ├── DashboardScreen.kt
│   │   └── wizard/           # 3-step delivery creation wizard
│   │       ├── CreateDeliveryWizardViewModel.kt
│   │       ├── CreateDeliveryWizardScreen.kt
│   │       ├── PhotoSelectionStep.kt
│   │       ├── LocationReviewStep.kt
│   │       └── ProductSelectionStep.kt
│   ├── product/
│   │   ├── ProductViewModel.kt
│   │   ├── ProductListScreen.kt
│   │   └── ProductFormScreen.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   ├── common/
│   │   ├── MainScreen.kt     # Bottom navigation container
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

## Implementation Status

### Completed Features ✅

#### Authentication & Authorization
- Login screen with JWT token authentication
- Automatic token injection via `AuthInterceptor`
- Token persistence using DataStore
- Role-based navigation (SALES vs SUPERVISOR)

#### Delivery Management
- **Dashboard**: Summary cards showing delivery counts by status
- **Delivery List**: Auto-refreshing list with role-based filtering
  - Layout glitch fix: Uses `isRefreshing` state for background updates
  - Auto-refresh on screen resume via `LifecycleEventObserver`
- **Delivery Detail**: Clickable cards navigate to detail screen showing:
  - Photos in horizontal carousel
  - Location details (name, street, district, city, region, coordinates)
  - Products list with quantities
  - Notes section
  - Created date and creator's initial
- **Create Delivery Wizard** (3 steps):
  - **Step 1**: Photo selection (max 10) with EXIF GPS extraction
    - Uses legacy file picker to preserve EXIF data
    - Green location indicator on photos with GPS data
  - **Step 2**: Location review with Android Geocoder reverse geocoding
    - All fields required and editable
  - **Step 3**: Product selection with quantities and notes

#### Product Management
- Product list screen (SUPERVISOR only)
- Create/edit product forms
- Delete products

#### User Interface
- Bottom navigation with role-based items
- Settings/Profile screen with logout
- Material 3 design system
- Dark/Light theme support

### Backend Integration
- All API endpoints integrated
- Photo upload via multipart/form-data
- Automatic role-based filtering (backend enforced)
- Image loading from backend `/uploads/` directory

### Known Issues / Limitations
- Emulator keyboard visibility issues (workaround: use host keyboard)
- No offline support yet (Room database not implemented)
- No camera integration (uses photo picker instead)

## Technical Details

### Delivery Creation Wizard Flow

The wizard creates deliveries in multiple steps with progressive enhancement:

1. **Step 1: Photo Selection**
   - User selects up to 10 photos
   - EXIF GPS data extracted using `ExifInterface`
   - Photos with GPS show green location indicator
   - Requires `ACCESS_MEDIA_LOCATION` permission

2. **Step 2: Location Review**
   - Creates delivery with `NEED_TO_CONFIRM` status and empty products
   - Extracts GPS from first photo with location data
   - Calls Android Geocoder for reverse geocoding
   - Uploads photos sequentially via multipart/form-data
   - Updates delivery with geocoded location data
   - All location fields are editable and required

3. **Step 3: Product Selection**
   - Select products from dropdown
   - Specify quantities for each product
   - Add optional notes
   - Updates delivery with final products and notes

**Why this approach:**
- Progressive creation ensures data is saved even if user exits
- Photos uploaded early to prevent data loss
- Status `NEED_TO_CONFIRM` indicates incomplete delivery
- Final step changes status to `READY`

### Photo Picker vs Camera
- Uses `ActivityResultContracts.GetMultipleContents` (legacy picker)
- **NOT** using `PickMultipleVisualMedia` because it strips EXIF data for privacy
- Requires `MediaStore.setRequireOriginal()` on Android 10+

### Geocoding Strategy
- Uses Android's built-in `Geocoder` class
- No API key required (unlike Google Maps Geocoding API)
- Requires network connection
- Returns Indonesian locale results (`Locale("id", "ID")`)

### Data Refresh Strategy
- Two loading states: `isLoading` and `isRefreshing`
- `isLoading`: Only true on initial load (empty list)
- `isRefreshing`: True on background refresh (list already populated)
- Prevents UI "glitch" where list disappears during refresh
- Auto-refresh triggered by `LifecycleEventObserver` on `ON_RESUME`

### Role-Based Filtering
- Backend enforces filtering based on JWT claims
- SALES users automatically filtered by `created_by`
- SUPERVISOR users see all deliveries
- Frontend simplified - no manual filtering logic needed

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
