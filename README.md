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
            implementation("io.github.omkardharmesh:konnectivity:0.0.1")
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

This library is published to Maven Central via Sonatype Central Portal under the auto-verified `io.github.omkardharmesh` namespace.

### One-time setup

1. Create a Central Portal account at [central.sonatype.com](https://central.sonatype.com) by signing in with GitHub. This auto-verifies the `io.github.omkardharmesh` namespace.
2. Generate a User Token at `https://central.sonatype.com/account` → "Generate User Token". This produces a username + password pair distinct from your login.
3. Generate a GPG key and export an ASCII-armored secret key (the in-memory format vanniktech consumes, which avoids BouncyCastle's brittle keyring parser):
   ```bash
   gpg --gen-key
   gpg --list-secret-keys --keyid-format=long
   gpg --export-secret-keys --armor <KEY_ID> > ~/.gnupg/secring.asc
   gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
   ```
4. Add credentials to the gitignored `local.properties` at the repo root:
   ```properties
   sdk.dir=/Users/<you>/Library/Android/sdk

   # Sonatype Central Portal user token (NOT your login password)
   mavenCentralUsername=<token-name>
   mavenCentralPassword=<token-password>

   # GPG signing — last 8 chars of the key ID, the passphrase, and where the
   # ASCII-armored secret key lives. Defaults to ~/.gnupg/secring.asc if
   # signing.secretKeyAsciiFile is omitted.
   signing.keyId=<last-8-chars-of-key-id>
   signing.password=<gpg-key-passphrase>
   signing.secretKeyAsciiFile=/Users/<you>/.gnupg/secring.asc
   ```

### Release

iOS klibs require a macOS host.

```bash
git tag v<X.Y.Z>
git push origin v<X.Y.Z>
./publish.sh
```

`./publish.sh` reads `local.properties`, loads the ASCII-armored key into memory, and invokes `./gradlew :konnectivity:publishAndReleaseToMavenCentral` with the credentials as `-P` arguments. The artifact appears at [central.sonatype.com/artifact/io.github.omkardharmesh/konnectivity](https://central.sonatype.com/artifact/io.github.omkardharmesh/konnectivity) within ~10 minutes.

## Credits

Forked from [Plus-Mobile-Apps/konnectivity](https://github.com/Plus-Mobile-Apps/konnectivity) (MIT) by Andrew Steinmetz.
