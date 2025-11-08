// 暂不适配鸿蒙
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
}

dependencyResolutionManagement {
    repositories {
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
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Sillot-KMP"

val buildFileName = "build.ohos.gradle.kts"
rootProject.buildFileName = buildFileName

include(":androidApp")
include(":shared")
project(":shared").buildFileName = buildFileName