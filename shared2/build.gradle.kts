import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("com.google.devtools.ksp")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

kotlin {
    jvmToolchain(17)

    jvm("desktop") {

    }

    androidTarget {
        // migrate to the compilerOptions DSL. More details are here: https://kotl.in/u1r8ln
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }
    }

    sourceSets {
        val commonMain by getting {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation(libs.miuix)
                implementation(libs.jmdns)
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.6")
                implementation("io.github.vinceglb:filekit-dialogs-compose:0.12.0")
                implementation(libs.jetbrains.kotlinx.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                project(":androidSofill")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.jna)
                implementation(libs.jna.platform)
            }
        }

    }
}

android {
    compileSdk = 36
    namespace = "sc.hwd.sillot.shared2"

    defaultConfig {
        minSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
