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
// project引用时只需要 api(project(":androidSillotGibbet")) 而无需受路径影响
include(":androidSillotGibbet")
project(":androidSillotGibbet").projectDir = file("androidModules/Gibbet")
include(":androidPotter")
project(":androidPotter").projectDir = file("androidModules/Potter")
include(":androidSiow")
project(":androidSiow").projectDir = file("androidModules/Siow")
include(":androidSpiller")
project(":androidSpiller").projectDir = file("androidModules/Spiller")
include(":androidLoftus")
project(":androidLoftus").projectDir = file("androidModules/Loftus")
include(":androidCyns")
project(":androidCyns").projectDir = file("androidModules/Cyns")
include(":androidHellise")
project(":androidHellise").projectDir = file("androidModules/Hellise")
include(":androidHime")
project(":androidHime").projectDir = file("androidModules/Hime")
include(":androidSofly")
project(":androidSofly").projectDir = file("androidModules/Sofly")
include(":androidSolist")
project(":androidSolist").projectDir = file("androidModules/Solist")
include(":androidSberrow")
project(":androidSberrow").projectDir = file("androidModules/Sberrow")