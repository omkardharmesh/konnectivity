plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    androidLibrary {
        namespace = "com.plusmobileapps.konnectivity"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "konnectivity"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.startup.runtime)
        }
    }
}

// GitHub Packages publishing configuration
// to publish: ./gradlew :konnectivity:publishAllPublicationsToGithubPackagesRepository
// credentials sourced from env (ORG_GRADLE_PROJECT_githubPackagesUsername/Password)
// or ~/.gradle/gradle.properties (user-level, NOT project)
publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/omkardharmesh/konnectivity")
            credentials(PasswordCredentials::class)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.omkardharmesh",
        artifactId = "konnectivity",
        version = "0.0.1"
    )

    pom {
        name.set("Konnectivity")
        description.set("Kotlin Multiplatform library for checking the current network connectivity status on Android and iOS")
        url.set("https://github.com/omkardharmesh/konnectivity")
        inceptionYear.set("2026")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("omkardharmesh")
                name.set("omkardharmesh")
                email.set("31363769+omkardharmesh@users.noreply.github.com")
            }
        }

        scm {
            connection.set("scm:git:https://github.com/omkardharmesh/konnectivity.git")
            developerConnection.set("scm:git:https://github.com/omkardharmesh/konnectivity.git")
            url.set("https://github.com/omkardharmesh/konnectivity")
        }
    }
}
