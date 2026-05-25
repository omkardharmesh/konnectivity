# Konnectivity

A Kotlin Multiplatform library for checking the network connectivity status of a mobile device.

This fork is maintained at [omkardharmesh/konnectivity](https://github.com/omkardharmesh/konnectivity) and adds:

- **NPE-safe `Konnectivity()` factory** — returns a no-op `KonnectivityImpl(NONE)` when called before `androidx.startup` has populated the application context, instead of throwing.
- **Modern KMP build** — Kotlin 2.2.x, AGP 8.13, Gradle 8.13, `com.android.kotlin.multiplatform.library` DSL, `gradle/libs.versions.toml` version catalog, `com.vanniktech.maven.publish` plugin.
- **Published to GitHub Packages** from macOS — full KMP distribution (Android AAR + `iosArm64` + `iosSimulatorArm64` klibs).

## Supported targets

- Android (`minSdk = 21`, `compileSdk = 36`)
- iOS (`iosArm64`, `iosSimulatorArm64`) — physical device required for `WIFI` / `CELLULAR` reporting on iOS

## Consuming the artifact

GitHub Packages requires authentication even for public packages. Add credentials to `~/.gradle/gradle.properties` (NOT the project-level `gradle.properties`):

```properties
githubPackagesUsername=<your-github-username>
githubPackagesPassword=<personal-access-token-with-read:packages-scope>
```

Add the GitHub Packages repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/omkardharmesh/konnectivity")
            credentials(PasswordCredentials::class)
        }
    }
}
```

Add the dependency to your KMP `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.omkardharmesh:konnectivity:0.0.1")
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

GitHub Packages must be published from macOS to include iOS klibs. JitPack will not work — its Linux build runners cannot cross-compile Kotlin/Native iOS targets.

```bash
ORG_GRADLE_PROJECT_githubPackagesUsername=omkardharmesh \
ORG_GRADLE_PROJECT_githubPackagesPassword=$(gh auth token) \
./gradlew :konnectivity:publishAllPublicationsToGithubPackagesRepository
```

This uses the active `gh` CLI account's token (no PAT stored on disk). Replace with `read:packages` + `write:packages` scoped PAT in `~/.gradle/gradle.properties` for repeatable publishes.

Tag the release for traceability:

```bash
git tag v0.0.1
git push origin v0.0.1
```

## Credits

Forked from [Plus-Mobile-Apps/konnectivity](https://github.com/Plus-Mobile-Apps/konnectivity) (MIT) by Andrew Steinmetz.
