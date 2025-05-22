# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application called "Personal Accountant" built with Kotlin and Jetpack Compose following SOLID principles and clean architecture patterns.

## Key Details

- **Package Name**: `ir.act.personalAccountant`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Build System**: Gradle with Kotlin DSL
- **UI Framework**: Jetpack Compose with Material 3
- **Language**: Kotlin with JVM target 11
- **Architecture**: MVVM + UseCase pattern with Clean Architecture

## Common Commands

### Build Commands
```bash
# Clean and build the project
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device/emulator
./gradlew installDebug
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "ir.act.personalAccountant.ExampleUnitTest"

# Run instrumented test class
./gradlew connectedAndroidTest --tests "ir.act.personalAccountant.ExampleInstrumentedTest"
```

### Development Commands
```bash
# Check for lint issues
./gradlew lint

# Generate lint report
./gradlew lintDebug

# Run dependency updates check
./gradlew dependencyUpdates
```

## Project Structure

```
app/src/
├── main/
│   ├── kotlin/ir/act/personalAccountant/
│   │   ├── Application.kt
│   │   ├── core/
│   │   │   └── ui/theme/
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── entity/
│   │   │   │   └── Cache.kt
│   │   │   ├── remote/
│   │   │   │   ├── model/
│   │   │   │   └── ApiService.kt
│   │   │   └── repository/
│   │   │       ├── Repository.kt
│   │   │       └── RepositoryImpl.kt
│   │   ├── di/
│   │   │   └── ApplicationModule.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   └── usecase/
│   │   │       ├── UseCase.kt
│   │   │       └── UseCaseImpl.kt
│   │   └── presentation/
│   │       ├── MainActivity.kt
│   │       ├── MainScreen.kt
│   │       └── feature/
│   │           ├── Contract.kt
│   │           ├── Screen.kt
│   │           └── ViewModel.kt
│   └── res/
├── test/
│   └── java/
│       ├── CoroutineTestRule.kt
│       ├── ir/act/personalAccountant/
│       │   ├── data/
│       │   ├── domain/
│       │   └── presentation/
│       └── stub/
│           ├── FakeRepository.kt
│           └── TestData.kt
└── androidTest/
    └── kotlin/ir/act/personalAccountant/
        └── presentation/
```

## Architecture Overview

This app follows **MVVM + UseCase architecture** with **Jetpack Compose** and **SOLID principles**.

### Layer Structure
- **Domain Layer** (`domain/`): Pure business logic with no Android dependencies
  - `model/`: Domain entities
  - `usecase/`: Business logic interfaces and implementations
  
- **Data Layer** (`data/`): External data handling
  - `remote/`: Retrofit API service and DTOs for API integration
  - `local/`: Database and In-memory cache
  - `repository/`: Repository pattern implementation bridging domain and data

- **Presentation Layer** (`presentation/`): UI with Jetpack Compose
  - Uses **Contract pattern** for UiState, Events, and UiInteractions

- **DI Layer** (`di/`): Hilt dependency injection setup
  - ApplicationModule provides Retrofit setup for API endpoint

### Key Patterns & SOLID Principles
- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Classes open for extension, closed for modification
- **Liskov Substitution**: Interfaces can be substituted with implementations
- **Interface Segregation**: No client depends on methods it doesn't use
- **Dependency Inversion**: Depend on abstractions, not concretions

- **Contract Pattern**: Each screen has a Contract object defining UiState, Events, and UiInteractions
- **Repository Pattern**: Repository interface with RepositoryImpl
- **UseCase Pattern**: UseCase handles business logic between repository and ViewModels
- **Hilt DI**: All dependencies injected via @Singleton bindings
- **TDD Approach**: Tests written first using fakes for all dependencies

### Testing Structure
- **Unit Tests** (`test/`): ViewModels, Repository, UseCase, and Cache with coroutines
- **UI Tests** (`androidTest/`): Compose screen tests with test data
- **Test Doubles** (`stub/`): Fake implementations for all major dependencies
- **Test Framework**: JUnit + MockK + Turbine for Flow testing + Coroutines Test

### Tech Stack
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM + UseCase pattern
- **DI**: Hilt
- **Networking**: Retrofit + Gson + OkHttp logging
- **Navigation**: Navigation Compose
- **Serialization**: Kotlinx Serialization
- **Build System**: Gradle with Kotlin DSL (KTS)
- **Testing**: JUnit, MockK, Turbine, Espresso, Compose Testing

## Development Notes

- Follow Clean Architecture principles with clear separation of concerns
- Use dependency injection for all major dependencies
- Write tests first (TDD approach) with proper fakes
- Maintain single responsibility for each class and function
- Use interfaces for abstraction and easier testing
- Apply SOLID principles throughout the codebase