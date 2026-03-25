# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Set JAVA_HOME (required if system Java is not installed)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run a single unit test class
./gradlew testDebugUnitTest --tests "com.example.counterapp.ExampleUnitTest"
```

## Architecture

**MVVM + Repository pattern** with Hilt DI, Room database, and Jetpack Compose UI.

```
data/local/        → Room entities (Category, Counter), DAOs, Database
data/repository/   → CounterAppRepository (singleton, single source of truth for all data ops)
di/                → Hilt DatabaseModule (provides DB, DAOs)
navigation/        → Sealed Routes class + NavGraph (3 destinations: Home, CategoryDetail, CounterDetail)
ui/home/           → Category list with stats
ui/category/       → Counter list within a category, stats banner
ui/counter/        → Counter detail: increment buttons, live timer, pause/resume, edit dialog
ui/components/     → Shared composables (ConfirmDeleteDialog, StatItem)
ui/theme/          → Material 3 theme with dynamic color support (API 31+)
util/              → TimeFormatter
```

**Data flow:** Room DAOs return `Flow<T>` → Repository exposes them → ViewModels convert to `StateFlow` via `stateIn(WhileSubscribed(5000))` → Compose collects via `collectAsState()`.

**Timer system:** CounterDetailViewModel tracks active sessions using `SystemClock.elapsedRealtime()`. Sessions start on `Lifecycle.ON_START`, flush on `ON_STOP`. A coroutine ticker updates `liveElapsedMs` every second. Pause/resume is separate from the edit flow.

**Database seeding:** On first create, the Hilt DatabaseModule's Room callback inserts a "General" category and "My Counter" counter. Deleting all categories auto-recreates "General".

## Key Conventions

- Package: `com.example.counterapp`
- Min SDK 28, Target/Compile SDK 35, Java 11
- AGP 9.1.0, Kotlin 2.2.10, KSP for Room/Hilt code generation
- Navigation uses sealed class `Routes` with `createRoute()` helpers and `LongType` arguments
- All ViewModels use `@HiltViewModel` with `SavedStateHandle` for navigation args
- Counters cascade-delete when their parent category is deleted (Room ForeignKey)
- Counter increments are coerced to >= 0 in the repository
