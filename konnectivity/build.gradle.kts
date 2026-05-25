plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
    id("convention.publication")
}

group = "com.github.omkardharmesh"
version = Deps.LIBRARY_VERSION

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "A kotlin multiplatform mobile network connectivity checker"
        homepage = "https://github.com/omkardharmesh/konnectivity"
        version = Deps.LIBRARY_VERSION
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "konnectivity"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Deps.Jetbrains.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Jetbrains.coroutinesTesting)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Deps.Android.startUp)
            }
        }
        val androidTest by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.plusmobileapps.konnectivity"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileSdk = Deps.Android.compileSDK
    defaultConfig {
        minSdk = Deps.Android.minSDK
        targetSdk = Deps.Android.targetSDK
    }
}
