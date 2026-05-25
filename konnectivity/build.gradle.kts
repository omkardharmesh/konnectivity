import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

// Load Maven Central + signing credentials from root local.properties (gitignored).
// Vanniktech's maven-publish plugin resolves them via project.findProperty().
val localProps = rootProject.file("local.properties").takeIf { it.exists() }?.let { file ->
    Properties().apply { file.inputStream().use { load(it) } }
}
localProps?.forEach { (name, value) ->
    val key = name.toString()
    if (key == "sdk.dir") return@forEach
    if (!project.hasProperty(key)) {
        project.extensions.extraProperties[key] = value
    }
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

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "io.github.omkardharmesh",
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
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("omkardharmesh")
                name.set("omkardharmesh")
                email.set("31363769+omkardharmesh@users.noreply.github.com")
                url.set("https://github.com/omkardharmesh")
            }
        }

        scm {
            connection.set("scm:git:https://github.com/omkardharmesh/konnectivity.git")
            developerConnection.set("scm:git:https://github.com/omkardharmesh/konnectivity.git")
            url.set("https://github.com/omkardharmesh/konnectivity")
        }
    }
}
