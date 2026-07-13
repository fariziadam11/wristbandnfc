# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a native Android application written in Kotlin for wristband NFC functionality. The project is a minimal shell with no NFC implementation yet.

**Package**: `com.gbs.wristbandnfc`
**Min SDK**: 29 (Android 10)
**Target SDK**: 36 (Android 15)

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests "com.gbs.wristbandnfc.ExampleUnitTest"

# Clean build
./gradlew clean

# Build with verbose output
./gradlew assembleDebug --info
```

The APK outputs to `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture

The project follows standard Android Gradle structure:
- `app/src/main/` - Main source code and resources
- `app/src/test/` - Unit tests (run on JVM)
- `app/src/androidTest/` - Instrumented tests (run on device/emulator)
- `gradle/libs.versions.toml` - Centralized dependency version management

## Dependencies

Versions are managed via Gradle's version catalog (`gradle/libs.versions.toml`):
- AndroidX AppCompat
- Material Components
- Kotlin core extensions
- JUnit 4 for unit testing
- AndroidX Test (Espresso, JUnit) for instrumented tests

## Configuration

- `local.properties` - SDK location (auto-configured by Android Studio)
- `gradle.properties` - Gradle settings including JVM args and configuration cache
- `settings.gradle.kts` - Project includes and repository configuration
