# Swift Flow Android

Android mobile application for the Swift Flow logistics and distribution tracking system.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Network**: Retrofit + OkHttp
- **Local Storage**: Room Database + DataStore
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose

## Requirements

- Android Studio (version: Hedgehog or later recommended)
- JDK 17
- Android SDK with API 34
- Minimum Android version: 7.0 (API 24)

## Project Structure

```
app/src/main/java/com/swiftflow/
├── data/                      # Data layer
│   ├── local/                 # Local database (Room)
│   ├── remote/                # API services
│   └── repository/            # Repository implementations
├── domain/                    # Domain layer
│   ├── model/                 # Domain models
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Use cases (business logic)
├── presentation/              # Presentation layer (UI)
│   ├── auth/                  # Login/Authentication screens
│   ├── delivery/              # Delivery screens
│   ├── product/               # Product screens
│   ├── common/                # Shared UI components
│   │   ├── components/        # Reusable Composables
│   │   └── theme/             # Theme, colors, typography
│   └── navigation/            # Navigation setup
├── di/                        # Dependency Injection modules
└── utils/                     # Utility classes

MainActivity.kt                 # Main entry point
SwiftFlowApp.kt                # Application class (Hilt setup)
```

## Setup Instructions

### 1. Backend Setup

Ensure the Swift Flow backend is running:

```bash
cd ../swift-flow-be
cargo run
```

The backend should be available at `http://localhost:8080`

### 2. Configure API URL

The API base URL is set in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/api/\"")
```

**Note**: `10.0.2.2` is the special IP for Android Emulator to access localhost.

For physical devices, change to your computer's IP:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://192.168.x.x:8080/api/\"")
```

### 3. Build and Run

#### Using Android Studio:
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click **Run** (or press Shift+F10)

#### Using Command Line:
```bash
# Build debug APK
./gradlew assembleDebug

# Install and run on connected device
./gradlew installDebug
```

## Features

### Sales Representative Features
- ✅ Login with username and password
- 🚧 View product catalog
- 🚧 Create delivery logs with:
  - Location details (name, street, district, city, region)
  - GPS coordinates
  - Product items and quantities
  - Photo attachments
  - Additional notes
- 🚧 View delivery history

### Supervisor Features
- ✅ Login with supervisor credentials
- 🚧 View all deliveries
- 🚧 Filter deliveries by:
  - Region/City/District/Street
  - Status
  - Date range
- 🚧 Analytics dashboard

## API Endpoints

The app connects to these backend endpoints:

- `POST /auth/login` - User authentication
- `GET /products` - Fetch product catalog
- `POST /deliveries` - Create new delivery
- `GET /deliveries` - List deliveries (with filters)
- `GET /deliveries/:id` - Get delivery details
- `POST /deliveries/:id/photos` - Upload delivery photo

## Permissions

The app requires the following permissions:
- **INTERNET** - API communication
- **ACCESS_FINE_LOCATION** - GPS coordinates for deliveries
- **CAMERA** - Taking delivery photos
- **READ_EXTERNAL_STORAGE** - Reading images (Android ≤ 12)

## Development Status

- ✅ Project structure initialized
- ✅ Gradle configuration complete
- ✅ MVVM architecture setup
- ✅ Hilt dependency injection configured
- ✅ Network layer (Retrofit) implemented
- ✅ Repository pattern implemented
- ✅ ViewModels created
- ✅ Login screen implemented
- 🚧 Delivery screens (in progress)
- 🚧 Product screens (planned)
- 🚧 Camera integration (planned)
- 🚧 GPS integration (planned)

## Next Steps

1. Implement delivery list screen
2. Implement create delivery form
3. Add camera capture functionality
4. Integrate GPS location services
5. Add offline support with Room database
6. Implement image upload functionality
7. Add supervisor analytics screens

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Build Variants

- **debug**: Development build with logging enabled
- **release**: Production build with ProGuard optimization

## Contributing

1. Follow Kotlin coding conventions
2. Use Jetpack Compose for all UI components
3. Maintain MVVM architecture
4. Write unit tests for ViewModels and repositories
5. Keep UI components small and reusable

## Troubleshooting

### Can't connect to backend
- Ensure backend is running on port 8080
- For emulator: Use `10.0.2.2`
- For physical device: Use your computer's local IP
- Check firewall settings

### Gradle sync failed
- Check Java version (requires JDK 17)
- Clear Gradle cache: `./gradlew clean`
- Invalidate caches in Android Studio

### Build errors
- Update Android Studio to latest version
- Sync project with Gradle files
- Clean and rebuild: `./gradlew clean build`
