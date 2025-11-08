// 暂不适配鸿蒙

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    kotlin("android").version("2.0.21-KBA-010").apply(false)
    kotlin("multiplatform").version("2.0.21-KBA-010").apply(false)
    id("com.google.devtools.ksp").version("2.0.21-1.0.27").apply(false)
    id("org.jetbrains.compose").version("1.7.3").apply(false)
    kotlin("plugin.compose").version("2.0.21-KBA-010").apply(false)
}