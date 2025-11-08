plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "sc.hwd.spiller"
    compileSdk = 35

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
//        dataBinding = true // 数据绑定
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":androidSofill"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.window.window6)
    implementation(libs.androidx.window.window.java6)
    implementation(libs.androidx.window.window.rxjava36)
    implementation(libs.androidx.biometric.biometric.ktx)
    annotationProcessor(libs.androidx.room.compiler)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // UI Tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.runtime.rxjava3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.fragment.fragment)
    implementation(libs.androidx.fragment.fragment.ktx2)
    implementation(libs.androidx.fragment.fragment.compose2)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work.work.runtime.ktx3)
    implementation(libs.androidx.work.work.multiprocess3)
    implementation(libs.androidx.webkit)

    val media3_version = "1.3.1"
//    implementation("androidx.media3:media3-exoplayer:$media3_version") // For media playback using ExoPlayer
//    implementation("androidx.media3:media3-exoplayer-dash:$media3_version") // For DASH playback support with ExoPlayer
//    implementation("androidx.media3:media3-exoplayer-hls:$media3_version") // For HLS playback support with ExoPlayer
//    implementation("androidx.media3:media3-exoplayer-smoothstreaming:$media3_version") // For SmoothStreaming playback support with ExoPlayer
//    implementation("androidx.media3:media3-exoplayer-rtsp:$media3_version") // For RTSP playback support with ExoPlayer
//    implementation("androidx.media3:media3-exoplayer-midi:$media3_version")  // For MIDI playback support with ExoPlayer (see additional dependency requirements in
//    // https://github.com/androidx/media/blob/release/libraries/decoder_midi/README.md)
//    implementation("androidx.media3:media3-exoplayer-ima:$media3_version") // For ad insertion using the Interactive Media Ads SDK with ExoPlayer
//    implementation("androidx.media3:media3-datasource-cronet:$media3_version") // For loading data using the Cronet network stack
//    implementation("androidx.media3:media3-datasource-okhttp:$media3_version") // For loading data using the OkHttp network stack
//    implementation("androidx.media3:media3-datasource-rtmp:$media3_version") // For loading data using librtmp
//    implementation("androidx.media3:media3-ui:$media3_version") // For building media playback UIs
//    implementation("androidx.media3:media3-ui-leanback:$media3_version") // For building media playback UIs for Android TV using the Jetpack Leanback library
//    implementation("androidx.media3:media3-session:$media3_version") // For exposing and controlling media sessions
//    implementation("androidx.media3:media3-extractor:$media3_version") // For extracting data from media containers
//    implementation("androidx.media3:media3-cast:$media3_version") // For integrating with Cast
//    implementation("androidx.media3:media3-exoplayer-workmanager:$media3_version") // For scheduling background operations using Jetpack Work's WorkManager with ExoPlayer
//    implementation("androidx.media3:media3-transformer:$media3_version") // For transforming media files
//    implementation("androidx.media3:media3-effect:$media3_version") // For applying effects on video frames
//    implementation("androidx.media3:media3-muxer:$media3_version") // For muxing media files
//    implementation("androidx.media3:media3-test-utils:$media3_version") // Utilities for testing media components (including ExoPlayer components)
//    implementation("androidx.media3:media3-test-utils-robolectric:$media3_version") // Utilities for testing media components (including ExoPlayer components) via Robolectric
//    implementation("androidx.media3:media3-container:$media3_version") // Common functionality for reading and writing media containers
//    implementation("androidx.media3:media3-database:$media3_version") // Common functionality for media database components
//    implementation("androidx.media3:media3-decoder:$media3_version") // Common functionality for media decoders
//    implementation("androidx.media3:media3-datasource:$media3_version") // Common functionality for loading data
    implementation("androidx.media3:media3-common:$media3_version") // MimeTypes 需要

    implementation(libs.commons.io)
    // https://github.com/Hi-Windom/Sillot/issues/1103
    // implementation libs.zackratos.ultimatebarx

    // 此库已停止维护，汐洛完全移除了对其的依赖
    // implementation 'com.blankj:utilcodex:1.31.1'

    implementation(libs.io.ktor.ktor.server.cio4) // Netty的本地传输不支持Android。因此使用CIO作为Ktor引擎
    implementation(libs.io.ktor.ktor.server.core4)
    implementation(libs.io.ktor.ktor.server.content.negotiation4)
    implementation(libs.io.ktor.ktor.client.core4)
    implementation(libs.io.ktor.ktor.client.cio4)
    implementation(libs.io.ktor.ktor.client.serialization4)
    implementation(libs.io.ktor.ktor.serialization.kotlinx.json4)

    implementation(libs.com.squareup.okhttp3.okhttp3)
    implementation(libs.com.squareup.retrofit2.retrofit3)
    // Gson converter for Retrofit
    implementation(libs.com.squareup.retrofit2.converter.gson3)

//    极光推送
    implementation(libs.cn.jiguang.sdk.jcore3)
    implementation(libs.cn.jiguang.sdk.jpush3)

    implementation(libs.com.tencent.mmkv3)

    implementation(libs.rxandroid)
    // Because RxAndroid releases are few and far between, it is recommended you also
    // explicitly depend on RxJava's latest version for bug fixes and new features.
    // (see https://github.com/ReactiveX/RxJava/releases for latest 3.x.x version)
    implementation(libs.rxjava)
    implementation(libs.rxkotlin)

//    annotationProcessor 'com.android.databinding:compiler:3.1.4' // 数据绑定

    //使用以下依赖的话需要在创建Moshi的时候添加KotlinJsonAdapterFactory()
    implementation(libs.moshi)
//Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    implementation(libs.moshi.kotlin)
    // 如果你需要使用Moshi来处理JSON adapters，也添加这个依赖
//            ksp(libs.moshi.kotlin.codegen)

    implementation(libs.com.tencent.bugly.crashreport2)
    implementation(libs.io.coil.kt.coil2)
    implementation(libs.io.coil.kt.coil.compose2)

    implementation(libs.realm.kotlin.library.base)
//            implementation(libs.realm.kotlin.library.sync) // If using Device Sync，已弃用
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.serialization.json)

    implementation(libs.com.louiscad.splitties.splitties.systemservices2)
    implementation(libs.eventbus)
    implementation(libs.cascade)
    implementation(libs.cascade.compose)
    implementation(libs.org.jsoup.jsoup)
    implementation(libs.commonmark)

    implementation(libs.getactivity.xxpermissions)
    implementation(libs.easywindow)
    implementation(libs.getactivity.toaster)
    debugImplementation(libs.logcat)

    //完整版引入
    implementation(libs.com.github.carguo.gsyvideoplayer.gsyvideoplayer)

    implementation(libs.dialogx)
    implementation(libs.dialogxmiuistyle)
    implementation(libs.zxing.lite) // 二维码/条形码（一维码） 扫描与生成
    implementation(libs.fileselector)
    implementation(libs.ketch) // file downloader library
    implementation(libs.colorpicker.compose)

    implementation(libs.colormath)
    // android.graphics.Color
    implementation(libs.colormath.ext.android.color)
    // androidx.annotation.ColorInt
    implementation(libs.colormath.ext.android.colorint)
    // androidx.compose.ui.graphics.Color
    implementation(libs.colormath.ext.jetpack.compose)
}