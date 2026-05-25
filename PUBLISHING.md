# Publishing Guide

End-to-end workflow for cutting a new Konnectivity release to Maven Central via Sonatype Central Portal. Read this once on first publish; bookmark the **Release** section after that.

## What you are publishing

A single Kotlin Multiplatform library that produces four Maven artifacts under the auto-verified `io.github.omkardharmesh` namespace:

| Artifact | Coordinate |
|---|---|
| KMP root metadata | `io.github.omkardharmesh:konnectivity:<version>` |
| Android AAR | `io.github.omkardharmesh:konnectivity-android:<version>` |
| iOS arm64 klib | `io.github.omkardharmesh:konnectivity-iosarm64:<version>` |
| iOS simulator arm64 klib | `io.github.omkardharmesh:konnectivity-iossimulatorarm64:<version>` |

Consumers add `mavenCentral()` and the root coordinate. Gradle metadata resolves the per-target variants automatically. No PAT, no auth.

## Tooling baseline

- macOS host (Kotlin/Native iOS targets cannot cross-compile from Linux)
- JDK 17 (Homebrew `openjdk@17` works)
- Kotlin 2.2.x, AGP 8.13, Gradle 8.13 — all pinned in `gradle/libs.versions.toml` and `gradle/wrapper/gradle-wrapper.properties`
- `com.vanniktech.maven.publish` 0.36.0 — pinned in `libs.versions.toml`
- `gpg` (GnuPG 2.x; macOS: `brew install gnupg`)
- `gh` CLI logged into the `omkardharmesh` account (`gh auth status` must show it as active)

## One-time setup

You do this once per maintainer machine. None of these steps touch the repo's tracked files — credentials live in `~/.gnupg/` and the gitignored `local.properties`.

### 1. Sonatype Central Portal account

1. Open https://central.sonatype.com.
2. Click **Sign In** → **Continue with GitHub** using the `omkardharmesh` account (NOT `omkar-elo`).
3. The namespace `io.github.omkardharmesh` is auto-verified because GitHub owns `github.io/omkardharmesh`. You will see it under **Namespaces** with status **Verified**.

If the auto-verification did not happen (very rare), email `central-support@sonatype.com` with your GitHub username — they will provision it manually.

### 2. Generate Central Portal User Token

The Central Portal upload API does not accept your login password. It requires a **User Token** — a username/password pair generated in the portal.

1. https://central.sonatype.com/account → **Generate User Token**.
2. Save both values immediately; the password is shown once.
3. Drop them into `local.properties` at the repo root (NOT `~/.gradle/gradle.properties` — we keep everything at repo level):

   ```properties
   mavenCentralUsername=<token-name>
   mavenCentralPassword=<token-password>
   ```

### 3. Generate GPG signing key

Central Portal requires all artifacts to be signed with a published GPG public key.

```bash
gpg --gen-key
# name:        omkardharmesh
# email:       31363769+omkardharmesh@users.noreply.github.com
# passphrase:  pick a strong one (e.g. openssl rand -base64 32)

gpg --list-secret-keys --keyid-format=long
# copy the 16-char hex after "sec   rsa4096/" — that's the full key id.
```

Stash the passphrase locally (not in the repo, not in the conversation):

```bash
echo '<passphrase>' > ~/.gnupg/central-signing-passphrase.txt
chmod 600 ~/.gnupg/central-signing-passphrase.txt
```

Publish the public key so Central can verify your signatures:

```bash
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
```

### 4. Export the secret key (ASCII-armored)

Vanniktech 0.36 uses an in-memory signing path that takes the ASCII-armored secret key as a Gradle property. We use this instead of the legacy `signing.secretKeyRingFile` because BouncyCastle (the library vanniktech bundles) cannot parse keyrings exported by GnuPG 2.4+ — it throws `PGPException: checksum mismatch at in checksum of 20 bytes` even when the passphrase is correct and `gpg --detach-sign` succeeds with the same inputs.

```bash
PASS=$(cat ~/.gnupg/central-signing-passphrase.txt)
echo "$PASS" | gpg --batch --pinentry-mode loopback --passphrase-fd 0 \
    --export-secret-keys --armor <KEY_ID> > ~/.gnupg/secring.asc
```

You should now have `~/.gnupg/secring.asc` starting with `-----BEGIN PGP PRIVATE KEY BLOCK-----`.

### 5. Wire credentials into `local.properties`

Final shape of `local.properties` (already gitignored — see `.gitignore`):

```properties
sdk.dir=/Users/<you>/Library/Android/sdk

# Sonatype Central Portal user token
mavenCentralUsername=<token-name>
mavenCentralPassword=<token-password>

# GPG signing — last 8 chars of KEY_ID, the passphrase, and the path to the
# ASCII-armored secret key. signing.secretKeyAsciiFile defaults to
# ~/.gnupg/secring.asc if omitted.
signing.keyId=<last-8-chars-of-KEY_ID>
signing.password=<gpg-key-passphrase>
signing.secretKeyAsciiFile=/Users/<you>/.gnupg/secring.asc
```

`publish.sh` reads this file; nothing else in the build does.

## Release workflow

Maven Central versions are **immutable** — once `io.github.omkardharmesh:konnectivity:X.Y.Z` is live, you cannot overwrite it. Always bump the version before publishing.

### 1. Bump the version

```kotlin
// konnectivity/build.gradle.kts
mavenPublishing {
    coordinates(
        groupId = "io.github.omkardharmesh",
        artifactId = "konnectivity",
        version = "0.0.3"  // ← bump
    )
}
```

Also update the version in any consumer snippet in `README.md` (search-replace the old coordinate).

### 2. Commit + tag

```bash
git add konnectivity/build.gradle.kts README.md
git commit -m "chore(release): bump to 0.0.3"
git push

git tag -a v0.0.3 -m "v0.0.3 - Maven Central release"
git push origin v0.0.3
```

Tags are for traceability only — the publish itself reads the version from `build.gradle.kts`, not the tag.

### 3. Publish

```bash
./publish.sh
```

`publish.sh` reads `local.properties`, loads `~/.gnupg/secring.asc` into memory, and runs `./gradlew :konnectivity:publishAndReleaseToMavenCentral --no-configuration-cache` with five `-P` arguments:

- `mavenCentralUsername`
- `mavenCentralPassword`
- `signingInMemoryKeyId`
- `signingInMemoryKeyPassword`
- `signingInMemoryKey` (the full ASCII-armored key block)

Expected tail of the run:

```
> Task :konnectivity:publishAndReleaseToMavenCentral
Validating deployment <uuid>...
Deployment is being validated
Deployment is being published to Maven Central

BUILD SUCCESSFUL
```

### 4. Wait for propagation

Artifacts appear on `repo1.maven.org` in 3–15 min after `BUILD SUCCESSFUL`. Poll:

```bash
curl -sI -o /dev/null -w "%{http_code}\n" \
  https://repo1.maven.org/maven2/io/github/omkardharmesh/konnectivity/0.0.3/konnectivity-0.0.3.module
```

`200` means the KMP root is live. The four artifacts land within the same minute of each other.

Browse the release at https://central.sonatype.com/artifact/io.github.omkardharmesh/konnectivity once propagation completes.

### 5. Tell consumers

```kotlin
implementation("io.github.omkardharmesh:konnectivity:0.0.3")
```

That is the only change. No repository entries beyond `mavenCentral()`.

## Troubleshooting

### `mavenCentralUsername not found`

The credentials never reached Gradle. `local.properties` is not auto-loaded as Gradle properties — only `gradle.properties` files are. `publish.sh` exists exactly to bridge that gap with `-P` flags. If you run `./gradlew :konnectivity:publishAndReleaseToMavenCentral` directly, it will fail with this error. Use `./publish.sh`.

### `PGPException: checksum mismatch at in checksum of 20 bytes`

You are using the legacy file-based signing path (`signing.secretKeyRingFile`). BouncyCastle bundled with vanniktech 0.36 cannot read GnuPG 2.4+ keyrings. Switch to in-memory signing by exporting the ASCII-armored secret key to `~/.gnupg/secring.asc` (step 4 of one-time setup) and pointing `local.properties` at it via `signing.secretKeyAsciiFile`. `publish.sh` then translates this into `signingInMemoryKey*` `-P` flags.

### `401 Unauthorized` from Maven Central

The User Token in `local.properties` is wrong, expired, or copied from a different account. Regenerate at https://central.sonatype.com/account.

### Deployment stuck in "validating" forever

Central Portal sometimes parks deployments that fail validation silently. Open https://central.sonatype.com/publishing/deployments — every deployment ID printed by `publish.sh` shows up here with a state (`VALIDATED`, `PUBLISHING`, `PUBLISHED`, `FAILED`) and error log. If `FAILED`, the validator usually complains about a missing POM field (license/scm/developer); fix it in `mavenPublishing.pom { ... }` and re-publish with the next version bump.

### iOS klibs missing from a release

You ran `./publish.sh` on Linux. Kotlin/Native iOS targets cannot cross-compile from Linux. Re-run on a macOS host with the same `local.properties`.

## Security

- `local.properties`, `~/.gnupg/secring.asc`, and `~/.gnupg/central-signing-passphrase.txt` never enter Git. `.gitignore` already covers `local.properties`. Confirm with `git check-ignore -v local.properties` before any commit.
- Rotate the Central Portal User Token in the portal UI if it ever leaves your machine (transcript, screenshot, log file).
- The GPG private key is protected by your passphrase. If the passphrase or `secring.asc` leaks, revoke the key (`gpg --gen-revoke <KEY_ID>` then `gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>`) and generate a new keypair.
- The `gh` CLI's active account must be `omkardharmesh`, not `omkar-elo`. `gh auth status` should list `omkardharmesh` with `Active account: true`. The work account is unrelated to this release pipeline; never use its credentials.

## Versioning convention

- `0.0.x` — pre-1.0, breaking changes allowed within the minor line. Use while the public API is in flux.
- `0.x.0` — once the API is stable enough that consumers should not expect surprises, but before the 1.0 stability commitment.
- `1.0.0` — first stable release. After this, follow [SemVer](https://semver.org/) strictly: patch for fixes, minor for backwards-compatible additions, major for breaking changes.

Git tags mirror the artifact version one-to-one: `v0.0.3` → `io.github.omkardharmesh:konnectivity:0.0.3`.

## What is NOT in this pipeline

- **No CI publish.** Releases run from a local Mac. To move to GitHub Actions later, store the five `-P` values as repository secrets and replace the `local.properties` reads in `publish.sh` with `${{ secrets.* }}` references; the rest of the script stays the same. The `macos-latest` runner satisfies the iOS host requirement.
- **No JitPack.** JitPack's Linux build servers cannot compile iOS klibs; the resulting module advertises iOS variants that 404. Maven Central is the only channel we support.
- **No GitHub Packages.** GitHub Support has confirmed Maven artifacts on `maven.pkg.github.com` will never support anonymous reads — every consumer would need a PAT. Maven Central avoids that entirely.
