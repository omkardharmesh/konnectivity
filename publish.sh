#!/usr/bin/env bash
# Publish konnectivity to Maven Central.
#
# Reads credentials from gitignored local.properties (Sonatype Central Portal
# user token, signing key id/passphrase) and the ASCII-armored secret key from
# the path in signing.secretKeyAsciiFile, then passes them as -P project
# properties so vanniktech-maven-publish can resolve them via
# providers.gradleProperty(...).
#
# Uses in-memory signing (signingInMemoryKey*) instead of file-based
# (signing.secretKeyRingFile*) because BouncyCastle versions bundled with
# recent vanniktech plugins fail to parse keyring files exported by GnuPG 2.4+
# ("PGPException: checksum mismatch at in checksum of 20 bytes").
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [[ ! -f local.properties ]]; then
    echo "local.properties not found at $SCRIPT_DIR" >&2
    exit 1
fi

read_prop() {
    # Grep no-match returns 1; suppress so callers can default missing keys.
    grep "^${1}=" local.properties 2>/dev/null | head -n1 | cut -d= -f2- || true
}

MAVEN_USER=$(read_prop mavenCentralUsername)
MAVEN_PASS=$(read_prop mavenCentralPassword)
SIGN_KEY_ID=$(read_prop signing.keyId)
SIGN_PASS=$(read_prop signing.password)
ASCII_KEY_FILE=$(read_prop signing.secretKeyAsciiFile)
ASCII_KEY_FILE="${ASCII_KEY_FILE:-$HOME/.gnupg/secring.asc}"
ASCII_KEY_FILE="${ASCII_KEY_FILE/#\~/$HOME}"

for label in MAVEN_USER MAVEN_PASS SIGN_KEY_ID SIGN_PASS; do
    if [[ -z "${!label}" ]]; then
        echo "Missing $label in local.properties" >&2
        exit 1
    fi
done

if [[ ! -f "$ASCII_KEY_FILE" ]]; then
    echo "ASCII-armored key not found at $ASCII_KEY_FILE" >&2
    echo "Export it with: gpg --export-secret-keys --armor $SIGN_KEY_ID > $ASCII_KEY_FILE" >&2
    exit 1
fi

KEY_CONTENT=$(<"$ASCII_KEY_FILE")

exec ./gradlew \
    :konnectivity:publishAndReleaseToMavenCentral \
    --no-configuration-cache \
    -PmavenCentralUsername="$MAVEN_USER" \
    -PmavenCentralPassword="$MAVEN_PASS" \
    -PsigningInMemoryKeyId="$SIGN_KEY_ID" \
    -PsigningInMemoryKeyPassword="$SIGN_PASS" \
    -PsigningInMemoryKey="$KEY_CONTENT" \
    "$@"
