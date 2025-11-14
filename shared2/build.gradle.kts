import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("com.google.devtools.ksp")
    kotlin("plugin.compose")
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

                implementation("top.yukonga.miuix.kmp:miuix:0.6.1")
            }
        }

//        val androidMain by getting {
//            dependencies {
//                implementation(libs.androidx.activity.compose)
//                implementation(libs.androidx.appcompat)
//            }
//        }

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
    compileSdk = 35
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