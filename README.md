# Konnectivity

A Kotlin Multiplatform library for checking the network connectivity status of a mobile device.

This fork is maintained at [omkardharmesh/konnectivity](https://github.com/omkardharmesh/konnectivity) and adds:

- **NPE-safe `Konnectivity()` factory** — returns a no-op `KonnectivityImpl(NONE)` when called before `androidx.startup` has populated the application context, instead of throwing.
- **Modern KMP build** — Kotlin 2.2.x, AGP 8.13+, Gradle 9.x, `com.android.kotlin.multiplatform.library` DSL, `gradle/libs.versions.toml` version catalog, `com.vanniktech.maven.publish` plugin.
- **Published via JitPack** for anonymous public consumption (no auth required).

[![JitPack](https://jitpack.io/v/omkardharmesh/konnectivity.svg)](https://jitpack.io/#omkardharmesh/konnectivity)

## Supported targets

- Android (`minSdk = 21`, `compileSdk = 36`)
- iOS (`iosArm64`, `iosSimulatorArm64`) — physical device required for `WIFI` / `CELLULAR` reporting on iOS

## Setup

Add the JitPack repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your KMP `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.omkardharmesh:konnectivity:0.0.1")
        }
    }
}
```

## Usage

Create a single instance of `Konnectivity` and inject it:

```kotlin
val konnectivity: Konnectivity = Konnectivity()
```

Read the current status:

```kotlin
val isConnected: Boolean = konnectivity.isConnected

when (konnectivity.currentNetworkConnection) {
    NetworkConnection.NONE -> "Not connected to the internet"
    NetworkConnection.WIFI -> "Connected to wifi"
    NetworkConnection.CELLULAR -> "Connected to cellular"
}
```

Observe changes via `StateFlow`:

```kotlin
scope.launch {
    konnectivity.isConnectedState.collect { isConnected ->
        // react
    }
}

scope.launch {
    konnectivity.currentNetworkConnectionState.collect { connection ->
        when (connection) {
            NetworkConnection.NONE -> "Not connected to the internet"
            NetworkConnection.WIFI -> "Connected to wifi"
            NetworkConnection.CELLULAR -> "Connected to cellular"
        }
    }
}
```

## Publishing (maintainer notes)

Push a git tag `vX.Y.Z` to `main`; JitPack will build and publish the artifact on demand from that tag.

```bash
git tag v0.0.1
git push origin v0.0.1
```

The artifact will be available at `https://jitpack.io/com/github/omkardharmesh/konnectivity/<version>` once the build succeeds.

## Credits

Forked from [Plus-Mobile-Apps/konnectivity](https://github.com/Plus-Mobile-Apps/konnectivity) (MIT) by Andrew Steinmetz.
