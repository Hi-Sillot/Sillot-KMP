import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}


extra.apply {
    set("siyuanVersionCode", 239)
    set("siyuanVersionName", "3.1.2")
    set("versionSillot", "") // 初始化 Sillot 版本同步
    set("vC", 14) // Sillot-android 主要版本递增
}

android {
    namespace = "SillotMatrix.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "sc.hwd.sillot.kmp"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // tinker 不支持，但是 rfix 支持
    splits {
        // Configures multiple APKs based on ABI.
        abi {
            // Enables building multiple APKs per ABI.
            isEnable = true

            // By default all ABIs are included, so use reset() and include to specify
            // Resets the list of ABIs that Gradle should create APKs for to none.
            reset()

            // Specifies a list of ABIs that Gradle should create APKs for.
            include("x86_64", "arm64-v8a")

            // Specifies that we do not want to also generate a universal APK that includes all ABIs.
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // https://developer.android.google.cn/build/dependencies?hl=zh-cn#dependency-info-play
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    /**
     * 文档 https://developer.android.google.cn/reference/tools/gradle-api/7.2/com/android/build/api/dsl/BuildFeatures
     *
     * buildFeatures 最好不要移动到别的文件里，否则可能不生效
     */
    buildFeatures {
//        dataBinding = true // 数据绑定
        compose = true
        buildConfig = true
        aidl =
            true // Android Interface Definition Language，即Android接口定义语言；用于让某个Service与多个应用程序组件之间进行跨进程通信
    }

    defaultConfig {
        // 多渠道设置不同 applicationId 无法正常切换生效，只能用 applicationIdSuffix 区分（效果是一样的）
        applicationId = "sc.hwd.sillot" // applicationId 是应用程序在设备上的唯一标识符，决定了应用程序的安装位置和包名。
        versionCode = 14
        versionName = "" // Sillot 版本同步

        addManifestPlaceholders(
            mapOf<String, Any>(
                "JPUSH_PKGNAME" to "sc.hwd.sillot",
                //JPush 上注册的包名对应的 Appkey.
                "JPUSH_APPKEY" to "737a606890201619d17999c7",
                //暂时填写默认值即可.
                "JPUSH_CHANNEL" to "developer-default"
            )
        )

        /**
         * 定义的先后会影响构建类型，不能随意更改先后顺序
         *
         * 注意：android studio 只支持当前激活的变体（辅助功能，不影响实际构建），即每个 dimension 只能有一个激活的 variant 。
         * 因此，修改非当前激活的变体的 src/flavor 前最好先在 Build Variants 里切换 Active Build Variant 到其他变体。
         *
         *
         * tinker 不支持 abi split ，但是 rfix 支持
         */
        flavorDimensions += "CHANNEL" // 渠道，允许共存安装（通过applicationIdSuffix和provider_authorities）
        flavorDimensions += "PRO" // 产品矩阵，无法共存安装
    }


    /**
     * 多渠道打包配置
     *
     * 渠道里可以使用 android {} 里的配置，比如 defaultConfig {}
     *
     * [2025年11月7日] 不再执着于服务应用的拆分，而是改为拆分子产品。由于无法实现两个风味的清单文件合并（只能风味向main合并），因此不再
     *
     * 所有风味均提供的产品：
     * 【KMP】汐洛分享助手（Sofly）、汐洛同步助手（Cyns）、汐洛播放器（Spiller）
     * 【仅安卓】汐洛浏览器（Sberrow）
     */
    productFlavors {
        // rfix 支持 abi split ， 所以不再定义 ABI 风味

        /**
         * 洛可可（完全体，主推风味），主要通过 sourceSets 实现
         */
        create("Rococo")
        {
            dimension = "PRO"
            isDefault = true
            buildConfigField("String", "PRO", "\"Rococo\"")
            resValue("string", "app_name", "汐洛·洛可可")
        }
        /**
         * 救赎风味，包含：
         * 【KMP】汐洛绞架（Gibbet，思源笔记定制版）、汐洛清单（Solist）
         * 【仅安卓】链滴流云（Loftus，链滴社区客户端）
         */
        create("Salvation")
        {
            dimension = "PRO"
            resValue("string", "app_name", "汐洛·救赎")
            buildConfigField("String", "PRO", "\"Salvation\"")
        }
        /**
         * 神罚风味，包含：
         * 【KMP】汐洛赫礼斯（Hellise）、汐洛司华（Siow）、汐洛叵特（Potter）
         * 【仅安卓】汐洛输入法（Hime，同文输入法定制版，能否集成尚未验证）
         */
        create("Damnation")
        {
            dimension = "PRO"
            resValue("string", "app_name", "汐洛·神罚")
            buildConfigField("String", "PRO", "\"Damnation\"")
        }

        // -------- 维度分割线 -------- //

        /**
         * 彖乄渠道，默认
         */
        create("T")
        {
            dimension = "CHANNEL"
            isDefault = true
            buildConfigField("String", "CHANNEL", "\"T\"")
            applicationIdSuffix = ".T"
            versionNameSuffix = ".T.${project.ext.get("vC")}"
            manifestPlaceholders["provider_authorities"] = "sc.hwd.sillot.provider.T"
            buildConfigField(
                "String",
                "PROVIDER_AUTHORITIES",
                "\"sc.hwd.sillot.provider.T\""
            )
        }
        /**
         * 金丝雀，面向尝鲜体验渠道
         */
        create("Canary")
        {
            dimension = "CHANNEL"
            buildConfigField("String", "CHANNEL", "\"Canary\"")
            applicationIdSuffix = ".C"
            versionNameSuffix = ".C.${project.ext.get("vC")}${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"))
            }" // 避免覆盖安装失败
            manifestPlaceholders["provider_authorities"] = "sc.hwd.sillot.provider.C"
            buildConfigField(
                "String",
                "PROVIDER_AUTHORITIES",
                "\"sc.hwd.sillot.provider.C\""
            )
        }
        /**
         * 先锋，小范围测试渠道特供
         */
        create("Pioneer")
        {
            dimension = "CHANNEL"
            buildConfigField("String", "CHANNEL", "\"Pioneer\"")
            applicationIdSuffix = ".P"
            versionNameSuffix = ".P.${project.ext.get("vC")}"
            manifestPlaceholders["provider_authorities"] = "sc.hwd.sillot.provider.P"
            buildConfigField(
                "String",
                "PROVIDER_AUTHORITIES",
                "\"sc.hwd.sillot.provider.P\""
            )
        }


    }

    // 打包改名
    applicationVariants.all {
        outputs.all {
            val ver = defaultConfig.versionName
            val abi = filters.find { it.filterType == "ABI" }?.identifier ?: flavorName
            println("Variant type: $buildType Flavor: $flavorName ABI: $abi")
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "${project.name}-$ver-${abi}.apk";
        }
    }


    /**
     * AIDL 文档 https://source.android.google.cn/docs/core/architecture/aidl/aidl-language?hl=zh-cn
     *
     * 中文互联网关于 AIDL 的文章全是垃圾，因此我不打算具体实现任何 AIDL
     *
     * 或许可以换个思路，通过跨进程的Activity来启动服务，并通过 Intent 传递数据调用服务的方法，这样就不用 AIDL 了
     * 试了不太行，还是老实用 AIDL 吧，参考 https://github.com/ibaozi-cn/Multi-Process-Audio-Recorder
     */
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("jnilibs") // TODO
            aidl.setSrcDirs(listOf("src/main/aidl"))
        }

        /**
         * 司马安卓 sourceSets 就清单文件不支持多个来源，因此 src/Rococo/AndroidManifest.xml 需要手动维护
         */
        getByName("Rococo") {
            java.srcDirs("src/Salvation/java", "src/Damnation/java")
            aidl.setSrcDirs(listOf("src/main/aidl"))
        }
        getByName("Salvation") {
            java.srcDirs("src/Salvation/java")
            aidl.setSrcDirs(listOf("src/main/aidl"))
//            manifest.srcFile("src/Salvation/AndroidManifest.xml")
//            resources.srcDirs("src/Salvation/res")
//            res.srcDirs("src/Salvation/res/values")
//            assets.srcDirs("src/Salvation/assets")
        }
        getByName("Damnation") {
            java.srcDirs("src/Damnation/java")
            aidl.setSrcDirs(listOf("src/main/aidl"))
        }


        /**
         * 输出 flavor 信息
         */
        all { set ->
            if (!set.name.lowercase().contains("test")) {
                println("${set.name} -> ${set.java.srcDirs()}")
            }
            true // 返回 true 表示继续处理后续的 source sets
        }
    }

    buildTypes {
        // https://developer.android.com/build/shrink-code?hl=zh-cn
        getByName("debug") {
            matchingFallbacks.add("release") // 第三方库处理
        }
        getByName("release") {
            isJniDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            matchingFallbacks.add("release") // 第三方库处理
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            ) // 混淆规则
        }
    }
}

// TODO 临时禁用，请勿删除
//androidComponents {
//    beforeVariants { variantBuilder ->
//        // 获取风味组合变体
//        val flavorNames = mutableListOf<String>()
//        variantBuilder.productFlavors.forEach { (dimension, flavor) ->
//            flavorNames.add(flavor)
//        }
//        val buildTypeName = variantBuilder.buildType
//        println("flavorNames: $flavorNames buildTypeName: $buildTypeName")
//
//        // 过滤不需要的风味组合变体
//        if (flavorNames.contains("Canary") && !buildTypeName.equals("debug", ignoreCase = true)) {
//            variantBuilder.enable = false // 禁用 Canary 非 debug 版本
//        }
//        if (flavorNames.contains("Pioneer") && buildTypeName.equals("debug", ignoreCase = true)) {
//            variantBuilder.enable = false // 禁用 Pioneer debug 版本
//        }
//        if (flavorNames.contains("T") && flavorNames.contains("Rococo")) {
//            variantBuilder.enable = false // 禁用 T+Rococo 组合
//        }
//        if (flavorNames.contains("Salvation") || flavorNames.contains("Damnation")) {
//            variantBuilder.enable = false // 禁用 Salvation/Damnation 组合
//        }
//    }
//}


// 渠道依赖 TODO: https://juejin.cn/post/6844903844556587015
dependencies {
    implementation(project(":shared"))

    "RococoImplementation"(project(":androidSofill"))
    // 加载所有模块
    "RococoImplementation"(project(":androidCyns"))
    "RococoImplementation"(project(":androidSillotGibbet"))
    "RococoImplementation"(project(":androidHellise"))
    "RococoImplementation"(project(":androidHime"))
    "RococoImplementation"(project(":androidLoftus"))
    "RococoImplementation"(project(":androidPotter"))
    "RococoImplementation"(project(":androidSberrow"))
    "RococoImplementation"(project(":androidSiow"))
    "RococoImplementation"(project(":androidSofly"))
    "RococoImplementation"(project(":androidSolist"))
    "RococoImplementation"(project(":androidSpiller"))

//    "DamnationCompileOnly"(group = "", name = "kernel", version = "", ext = "aar")  // 编译时仅需要其API
//    "SalvationImplementation"(group = "", name = "kernel", version = "", ext = "aar")
//    "RococoImplementation"(group = "", name = "kernel", version = "", ext = "aar")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

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

    // https://shiply.tds.qq.com/docs/doc?id=4008331373
    implementation(libs.shiply.upgrade)
    implementation(libs.shiply.upgrade.ui) // 弹框ui相关，业务方如果自己自定义弹框，可以不依赖这个库
    implementation(libs.shiply.upgrade.diff.pkg.patch) // 用于差量APK合并，如果业务方不使用差量能力，可以不依赖这个包
}