/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午11:00
 * updated: 2024/9/2 上午11:00
 */

package sc.hwd.sillot.gibbet.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.tencent.bugly.crashreport.BuglyLog
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mobile.Mobile
import org.apache.commons.io.FileUtils
import org.b3log.siyuan.Utils
import sc.hwd.sillot.gibbet.R
import sc.hwd.sillot.gibbet.workers.CheckHttpServerWorker
import sc.hwd.sillot.gibbet.workers.SyncDataWorker
import sc.hwd.sofill.LibraryConfig
import sc.hwd.sofill.S
import sc.hwd.sofill.Ss.S_Color
import sc.hwd.sofill.Ss.S_Intent
import sc.hwd.sofill.Ss.S_Notification
import sc.hwd.sofill.Us.U_Permission.hasPermission_FOREGROUND_SERVICE_DATA_SYNC
import sc.hwd.sofill.Us.getWebViewVer
import sc.hwd.sofill.android.ForegroundPush.applyCommonBuilderSettings
import sc.hwd.sofill.android.ForegroundPush.setServiceNotificationBuilder
import sc.hwd.sofill.interfaces.gibbet.IGibbetKernelService
import sc.hwd.sofill.android.webview.WebPoolsPro
import sc.hwd.sofill.interfaces.gibbet.ICreateDocWithMdResponse
import sc.hwd.sofill.interfaces.gibbet.INotebookListResponse
import sc.hwd.sofill.interfaces.gibbet.ISiyuanFilelockWalk
import sc.hwd.sofill.interfaces.gibbet.ISiyuanFilelockWalkRes
import sc.hwd.sofill.interfaces.gibbet.ISiyuanFilelockWalkResFiles
import sc.hwd.sofill.interfaces.gibbet.ISiyuanFilelockWalkResFilesItem
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * TODO: 双开共存时内核固定端口冲突
 */
class GibbetKernelService : Service(), IGibbetKernelService {

    companion object {
        private val TAG = "services/GibbetKernelService.kt"
    }

    override val checkHttpServerWorkerName = "CheckHttpServerWork"

    override var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    override var serverPort = S.AndroidServerPort
    override val localIPs = Utils.IPAddressList
    override var webView: WebView? = null
    override var webViewVer: String? = null
    override var userAgent: String? = null
    override var kernelStarted = false

    /**
     * 目前依赖此配置强制停止内核
     */
    override var stopKernelOnDestroy = true
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandler: Handler
    private var webViewKey: String? = null
    private lateinit var dataDir: String
    private lateinit var appDir: String
    override var builderGibbetKernel: NotificationCompat.Builder? = null


    override fun onCreate() {
        super.onCreate()
        BuglyLog.i(TAG, "onCreate() invoked")
        dataDir = filesDir.absolutePath
        appDir = "$dataDir/app"
        mHandlerThread = HandlerThread("MyHandlerThread")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        builderGibbetKernel = initGibbetKernelNotificationBuilder()
        works()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        BuglyLog.i(TAG, "onStartCommand() -> intent: $intent, flags: $flags, startId: $startId")
        works()
        return START_REDELIVER_INTENT // 如果 Service 被杀死，系统会尝试重新创建 Service，并且会重新传递最后一个 Intent 给 Service 的 onStartCommand() 方法。
    }

    override fun onDestroy() {
        super.onDestroy()
        BuglyLog.i(TAG, "onDestroy() invoked")
        webView?.let { webViewKey?.let { it1 -> WebPoolsPro.instance?.recycle(it, it1) } }
        server?.stop()
        if (stopKernelOnDestroy) Mobile.stopKernel() else server?.stop()
    }

    private val binder = object : IGibbetKernelService.LocalBinder() {
        override fun getService(): IGibbetKernelService = this@GibbetKernelService
    }

    override fun onBind(intent: Intent): IBinder? {
        BuglyLog.i(TAG, "onBind() invoked, intent: $intent")
        webViewKey = intent.getStringExtra(S_Intent.EXTRA_WEB_VIEW_KEY)
        return binder
    }

//    internal inner class LocalBinder : Binder() {
//        fun getService(): GibbetKernelService = this@GibbetKernelService
//    }

    /**
     * startForeground 创建的通知，只能通过 startForeground 才能更新（调用本函数即可）
     * TODO: 添加权限验证
     */
    override fun showNotification(notification: Notification?) {
        if (hasPermission_FOREGROUND_SERVICE_DATA_SYNC(applicationContext)) {
            BuglyLog.d(TAG, "-> 启动/更新前台服务通知")
            // 必须首先始终调用 startService(Intent) 来告诉系统应该让服务持续运行，然后使用此方法告诉它要更努力地保持运行。
            startForeground(
                S_Notification.SILLOT_GIBBET_KERNEL_notificationId,
                notification ?: initGibbetKernelNotificationBuilder().build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
    }

    /**
     * 汐洛绞架内核服务通知
     */
    private fun initGibbetKernelNotificationBuilder(): NotificationCompat.Builder {
        Log.d(TAG, "initGibbetKernelNotificationBuilder")
        val serviceName = "sc.hwd.sillot.gibbet.services.GibbetKernelService"
        val serviceClass: Class<Service> = Class.forName(serviceName) as Class<Service>
        builderGibbetKernel = NotificationCompat.Builder(this, S_Notification.SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANNEL_ID)
        builderGibbetKernel!!.setSmallIcon(R.drawable.icon)
            .setContentTitle("🟢 GibbetKernelService")
            .setContentText("点击通知，唤醒内核")
            .applyCommonBuilderSettings().let {
                return it
                // Isolated process not allowed to call getIntentSender by createServicePendingIntent
                return setServiceNotificationBuilder(it, this.applicationContext,
                    S_Notification.SILLOT_NOTIFICATION_REQUEST_CODE_SERVICE,
                    serviceClass)
            }
    }


    private fun works() {

        BuglyLog.d(TAG, "-> 初始化 UI 元素")
        init_webView()

        BuglyLog.d(TAG, "-> 拉起内核")
        startKernel()

//        BuglyLog.d(TAG, "-> 周期同步数据")
//        scheduleSyncDataWork()

        BuglyLog.d(TAG, "-> 内核心跳检测")
        CheckHttpServerWork()
    }

    private fun init_webView() {
        // 不使用 activity 的上下文会导致 https://github.com/Hi-Windom/Sillot/issues/814 这里改为获取在 MainActivity 初始化好的 webView
        webView =
            webViewKey?.let { WebPoolsPro.instance?.acquireWebView(it) }
        webView?.setBackgroundColor(Color.parseColor(S_Color.Hex_bgColor_light))
        val ws = webView?.settings
        userAgent = ws?.userAgentString
        webViewVer = this.getWebViewVer()
    }

    private val bootHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val cmd = msg.getData().getString("cmd")
            if ("startKernel" == cmd) {
                bootKernel()
            } else {
                BuglyLog.w(TAG, cmd.toString())
            }
        }
    }

    override fun isHttpServerRunning(): Boolean {
        return server != null
    }

    private fun startHttpServer() {
        if (isHttpServerRunning()) {
            server?.stop()
            BuglyLog.w(TAG, "startHttpServer() stop exist server")
        }
        try {
            // 解决乱码问题 https://github.com/koush/AndroidAsync/issues/656#issuecomment-523325452
            val charsetClass = Charsets::class.java
            val usAscii = charsetClass.getDeclaredField("US_ASCII")
            usAscii.isAccessible = true
            usAscii[Charsets::class.java] = Charsets.UTF_8
        } catch (e: Exception) {
            Utils.LogError(TAG, "init charset failed", e)
        }
        /**
         * localIPs 多于一个则绑定所有网卡以便通过局域网IP访问；
         * localIPs 只有一个则绑定回环地址 可能是 127.0.0.1 也可能是 [::1]
         */
        val inetAddress: String =
            if (localIPs.split(',').size > 1) "0.0.0.0" else InetAddress.getLoopbackAddress().hostAddress
        val _server = embeddedServer(CIO, port = getAvailablePort(), host = inetAddress) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            routing {
                post("/api/walkDir") {
                    withContext(Dispatchers.IO) { // 使用 IO 协程上下文处理文件系统操作
                        val start = System.currentTimeMillis()
                        try {
//                        BuglyLog.w(TAG, "${call.request.contentLength()} ${call.request.contentType()}")
                            val requestJSON = call.receive<ISiyuanFilelockWalk>()
                            val dir = requestJSON.dir
                            val directory = File(dir)
                            val filesList =
                                ISiyuanFilelockWalkResFiles(files = mutableListOf<ISiyuanFilelockWalkResFilesItem>())
                            directory.walkTopDown().filter { it.isDirectory || it.isFile }
                                .forEach { file ->
                                    filesList.files.add(
                                        ISiyuanFilelockWalkResFilesItem(
                                            path = file.absolutePath,
                                            name = file.name,
                                            size = file.length(),
                                            updated = file.lastModified(),
                                            isDir = file.isDirectory
                                        )
                                    )
                                }

                            call.respond(
                                ISiyuanFilelockWalkRes(
                                    code = 0,
                                    msg = "",
                                    data = filesList
                                )
                            )
                            Utils.LogInfo(
                                TAG,
                                "walk dir [$dir] in [${System.currentTimeMillis() - start}] ms"
                            )
                        } catch (e: Exception) {
                            Utils.LogError(TAG, "walk dir failed: ${e.message}", e)
                            call.respond(
                                ISiyuanFilelockWalkRes(
                                    code = 0,
                                    msg = e.stackTraceToString(),
                                    data = null
                                )
                            )
                        }
                    }
                }
            }
        }
        server = _server
        server?.let {
            it.start(wait = false) // 不等待阻塞
                // TODO: 新版 environment.connectors 不存在
//            val actualPort = it.environment.connectors.first().port
//            val actualHost = it.environment.connectors.first().host
//            Utils.LogInfo(TAG, "HTTP server is listening on ${actualHost}, port [${actualPort}]")
        }
    }

    private fun getAvailablePort(): Int {
        var ret = serverPort
        try {
            ServerSocket(serverPort).use { socket ->
                ret = socket.localPort
            }
        } catch (e: Exception) {
            Utils.LogError(
                TAG,
                "$serverPort not available: ${e.message} \n will try to use a automatically port",
                e
            )
            try {
                ServerSocket(0).use { socket ->
                    ret = socket.localPort
                }
            } catch (e: Exception) {
                BuglyLog.e(TAG, "get available port failed ${e.message}")
                Utils.LogError(TAG, "get available port failed", e)
            }
        }
        return ret
    }

    private fun startKernel() {
        BuglyLog.w(TAG, "startKernel() invoked")
        synchronized(this) {
            if (kernelStarted) {
                return
            }
            kernelStarted = true
            val b = Bundle()
            b.putString("cmd", "startKernel")
            val msg = Message()
            msg.data = b
            bootHandler.sendMessage(msg)
        }
    }

    private fun bootKernel() {
        Mobile.setHttpServerPort(serverPort.toLong())
        if (Mobile.isHttpServing()) {
            Utils.LogInfo(TAG, "kernel HTTP server is running")
            return
        }
        initAppAssets()
        startHttpServer()
        val appDir = filesDir.absolutePath + "/app"
        // As of API 24 (Nougat) and later 获取用户的设备首选语言
        val locales = resources.configuration.getLocales()
        val locale = locales[0]
        val workspaceBaseDir = getExternalFilesDir(null)?.absolutePath
        val timezone = TimeZone.getDefault().id
        mHandler.post {
            try {
                val lang = determineLanguage(locale)
                BuglyLog.d(
                    TAG,
                    "Mobile.startKernel() -> [${localIPs}] workspaceBaseDir -> $workspaceBaseDir"
                )
                Mobile.startKernel(
                    "android", appDir, workspaceBaseDir, timezone, localIPs, lang,
                    Build.VERSION.RELEASE +
                            "/SDK " + Build.VERSION.SDK_INT +
                            "/WebView " + webViewVer +
                            "/Manufacturer " + Build.MANUFACTURER +
                            "/Brand " + Build.BRAND +
                            "/UA " + userAgent
                )
                BuglyLog.d(TAG, "Mobile.startKernel() ok")
            } catch (e: Exception) {
                // 处理异常
                BuglyLog.e(TAG, "Error in background thread", e)
            }
        }
        val b = Bundle()
        b.putString("cmd", "bootIndex")
        val msg = Message()
        msg.data = b
        bootHandler.sendMessage(msg)
    }

    private fun determineLanguage(locale: Locale): String {
        val lang = locale.language + "_" + locale.country
        return when {
            lang.lowercase(Locale.getDefault()).contains("cn") -> "zh_CN"
            lang.lowercase(Locale.getDefault()).contains("es") -> "es_ES"
            lang.lowercase(Locale.getDefault()).contains("fr") -> "fr_FR"
            else -> "en_US"
        }
    }

    private fun needUnzipAssets(): Boolean {
        BuglyLog.i(TAG, "needUnzipAssets() invoked")
        val appDirFile = File(appDir)
        appDirFile.mkdirs()
        var ret = true
        if (Utils.isDebugPackageAndMode(this)) {
            BuglyLog.i("boot", "always unzip assets in debug mode")
            return ret
        }
        val appVerFile = File(appDir, "VERSION")
        if (appVerFile.exists()) {
            try {
                val ver = FileUtils.readFileToString(appVerFile, StandardCharsets.UTF_8)
                ret = ver != LibraryConfig.versionName
            } catch (e: java.lang.Exception) {
                Utils.LogError("boot", "check version failed", e)
            }
        }
        return ret
    }

    private fun initAppAssets() {
        if (needUnzipAssets()) {
            val appVerFile = File(appDir, "VERSION")
            BuglyLog.i(TAG, "Clearing appearance... 20%")
            try {
                FileUtils.deleteDirectory(File(appDir))
            } catch (e: java.lang.Exception) {
                Utils.LogError(
                    "boot",
                    "delete dir [$appDir] failed, exit application", e
                )
                stopSelf()
                return
            }
            BuglyLog.i(TAG, "Initializing appearance... 60%")
            Utils.unzipAsset(assets, "app.zip", "$appDir/app")
            try {
                FileUtils.writeStringToFile(appVerFile, LibraryConfig.versionName, StandardCharsets.UTF_8)
            } catch (e: java.lang.Exception) {
                Utils.LogError("boot", "write version failed", e)
            }
            BuglyLog.i(TAG, "Booting kernel... 80%")
        }
    }

    /**
     * 目前看似乎没有必要，同步感知可以及时同步
     */
    private fun scheduleSyncDataWork() {
        val workManager = WorkManager.getInstance(this.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 确保在网络连接时运行
            .setRequiresBatteryNotLow(true) // 低电量时不运行
            .build()
        // 可以定义的最短重复间隔是 15 分钟
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(SyncDataWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(10, TimeUnit.MINUTES) // 在加入队列后至少经过 10 分钟后再运行
                .build()

        workManager.enqueueUniquePeriodicWork(
            "scheduleSyncDataWork",
            ExistingPeriodicWorkPolicy.KEEP, // 如果已经存在，则保持
            periodicWorkRequest
        )
    }

    /**
     * 这种方法并不是官方推荐的，因为它可能会导致任务之间的延迟，并且在高频率下可能会对系统资源造成压力。
     */
    private fun CheckHttpServerWork() {
        val workManager = WorkManager.getInstance(this.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 创建一个OneTimeWorkRequest
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CheckHttpServerWorker::class.java)
            .setConstraints(constraints)
            .build()

        // 将任务加入到WorkManager中，并设置一个UniqueWork名称
        workManager.enqueueUniqueWork(
            checkHttpServerWorkerName,
            ExistingWorkPolicy.REPLACE, // 每次都替换之前的任务
            oneTimeWorkRequest
        )

        // 任务完成后，延迟一段时间再次启动同一个任务
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observeForever { workInfo ->
                if (workInfo != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        CheckHttpServerWork()
                    }, 60000)
                }
            }
    }

    override fun goUpdateAssets() {
        Mobile.updateAssets()
    }

    override fun goReindexAssetContentOnce() {
        Mobile.reindexAssetContentOnce()
    }

    override fun goIncSyncOnce() {
        Mobile.incSyncOnce()
    }

    override fun goInsertBlockNext(paramsJSON: String) {
        Mobile.insertBlockNext(paramsJSON)
    }

    override fun goIsHttpServing(): Boolean {
        return Mobile.isHttpServing()
    }

    override fun goGetNotebooks(flashcard: Boolean): INotebookListResponse {
        return INotebookListResponse(value = Mobile.getNotebooks(flashcard))
//        return Mobile.getNotebooks(flashcard)
    }

    override fun goCreateDocWithMd(paramsJSON: String): ICreateDocWithMdResponse {
        return ICreateDocWithMdResponse(value = Mobile.createDocWithMd(paramsJSON))
//        return Mobile.createDocWithMd(paramsJSON)
    }
}

/**
 * 使用协程等待内核 HTTP 服务启动。
 */
@Deprecated("不需要了")
fun waitForKernelHttpServingWithCoroutines() = runBlocking {
    while (!Mobile.isHttpServing()) {
        delay(20)
    }
}

