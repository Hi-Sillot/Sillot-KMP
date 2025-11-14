pluginManagement {
    repositories {
        maven { url=uri ("https://jitpack.io") }
        maven { url=uri ("https://maven.aliyun.com/repository/releases") }
        maven { url=uri ("https://maven.aliyun.com/repository/google") }
        maven { url=uri ("https://maven.aliyun.com/repository/central") }
        maven { url=uri ("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url=uri ("https://maven.aliyun.com/repository/public") }
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
    }
    plugins {
        kotlin("jvm") version "2.2.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        flatDir {
            // local
            dirs("libs")
        }
        // 阿里云Maven中央仓库下载源
        // https://developer.aliyun.com/mvn/guide?spm=a2c6h.13651104.0.0.435836a4Lghjtb
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        // 不在GitHub Actions中使用的仓库配置
        if (System.getenv("CI") == null) {
            maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        }
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
        maven { url = uri("https://maven.cnb.cool/tencent-tds/shiply-public/-/packages/") }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "Sillot-KMP"
include(":androidApp")
include(":shared")
//include(":h5App")
//include(":miniApp")

include(":androidSofill")
// 包含androidModules下的所有模块
file("androidModules").listFiles()?.forEach { moduleDir ->
    if (moduleDir.isDirectory) {
        include(":android${moduleDir.name}")
        // project引用时只需要 include(project(":android${moduleDir.name}")) 而无需受路径影响
        project(":android${moduleDir.name}").projectDir = file("androidModules/${moduleDir.name}")
        println("include android${moduleDir.name} dir ${file("androidModules/${moduleDir.name}")}")
    }
}
