plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

kotlin {
    jvmToolchain(17)
    jvm("desktop") {

    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared2"))
                implementation(compose.desktop.currentOs)
                // 添加其他桌面端依赖
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "sc.hwd.sillot.shared2.Main_desktopKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Sillot-Desktop"
            packageVersion = "1.0.0"
        }
    }
}
