# Konnectivity

A Kotlin Multiplatform library for checking the network connectivity status of a mobile device.

This fork is maintained at [omkardharmesh/konnectivity](https://github.com/omkardharmesh/konnectivity) and adds:

- **NPE-safe `Konnectivity()` factory** — returns a no-op `KonnectivityImpl(NONE)` when called before `androidx.startup` has populated the application context, instead of throwing.
- **Modern KMP build** — Kotlin 2.2.x, AGP 8.13, Gradle 8.13, `com.android.kotlin.multiplatform.library` DSL, `gradle/libs.versions.toml` version catalog, `com.vanniktech.maven.publish` plugin.
- **Published to Maven Central** — anonymous public consumption, no authentication required.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.omkardharmesh/konnectivity?color=blue)](https://central.sonatype.com/artifact/io.github.omkardharmesh/konnectivity)

## Supported targets

- Android (`minSdk = 21`, `compileSdk = 36`)
- iOS (`iosArm64`, `iosSimulatorArm64`) — physical device required for `WIFI` / `CELLULAR` reporting on iOS

## Consuming the artifact

`mavenCentral()` is the only repository required. No credentials needed.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

Add the dependency to your KMP `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.omkardharmesh:konnectivity:0.0.2")
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

## Publishing

Maintainer workflow — Central Portal account, GPG keypair, version bump, release command, troubleshooting — lives in [PUBLISHING.md](PUBLISHING.md).

Quick release (after one-time setup):

```bash
# 1. bump version in konnectivity/build.gradle.kts + README
# 2. commit + tag + push
./publish.sh
```

iOS klibs require a macOS host. Artifact appears at [central.sonatype.com/artifact/io.github.omkardharmesh/konnectivity](https://central.sonatype.com/artifact/io.github.omkardharmesh/konnectivity) within ~10 minutes.

## Credits

Forked from [Plus-Mobile-Apps/konnectivity](https://github.com/Plus-Mobile-Apps/konnectivity) (MIT) by Andrew Steinmetz.
