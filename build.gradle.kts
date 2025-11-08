plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    id("com.google.devtools.ksp").version("2.3.1").apply(false)   // 使用 ksp 而不是 kapt ； 需要适配 kotlin 版本
    id("org.jetbrains.compose").version("1.7.3").apply(false)
    kotlin("plugin.compose").version("2.1.21").apply(false)  // https://developer.android.com/develop/ui/compose/compiler?hl=zh-cn
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.0" apply false
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath(BuildPlugin.kuikly)
        classpath(libs.gradle) // 8.0+ 需要 JDK 17+
        classpath(libs.kotlin.gradle.plugin)
        classpath("io.github.xilinjia.krdb:gradle-plugin:3.2.9")
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))

}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(8)
}
