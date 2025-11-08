/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/1 上午2:00
 * updated: 2024/9/1 上午2:00
 */

package sc.hwd.sofill.android.services

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.SpringBackDraggable
import com.kongzue.dialogx.dialogs.PopNotification
import com.tencent.bugly.crashreport.BuglyLog
import sc.hwd.sofill.R
import sc.hwd.sofill.Ss.S_Notification
import sc.hwd.sofill.U
import sc.hwd.sofill.Us.U_Permission.hasPermission_FOREGROUND_SERVICE_DATA_SYNC
import sc.hwd.sofill.Us.U_Thread.runOnUiThread
import sc.hwd.sofill.android.SillotApplication
import sc.hwd.sofill.android.net.networkRequestBluetooth
import sc.hwd.sofill.android.net.networkRequestCellular
import sc.hwd.sofill.android.net.networkRequestEthernet
import sc.hwd.sofill.android.net.networkRequestUSB
import sc.hwd.sofill.android.net.networkRequestVPN
import sc.hwd.sofill.android.net.networkRequestWifi
import sc.hwd.sofill.android.net.networkRequestWifiAware
import sc.hwd.sofill.android.permission.PermissionInterceptor
import sc.hwd.sofill.interfaces.sofill.view.IFloatingWindowService
import sc.hwd.sofill.interfaces.sofill.view.IFloatingWindowServiceManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 由于涉及到悬浮窗显示，因此必须作为前台服务运行
 * - 错误调用：只调用 `startForegroundService`
 * - 正确调用：无论是 `startService` 还是 `startForegroundService` 都需要  `startForeground` 来启动前台服务（当然还需要权限先验）
 * 不调用 `startForeground` 必崩溃，fuck google
 */
class FloatingWindowService() : Service(),IFloatingWindowService {
    lateinit var floatingWindow: EasyWindow<*>
    lateinit var floatingBallWindow: EasyWindow<*>
    private lateinit var connectivityManager: ConnectivityManager


    companion object {
        private val TAG = "services/FloatingWindowService.kt"

        @JvmField
        val ACTION_TOGGLE_WINDOW = "ACTION_TOGGLE_WINDOW"

        @JvmField
        val ACTION_SHOW_WINDOW = "ACTION_SHOW_WINDOW"

        @JvmField
        val ACTION_HIDE_WINDOW = "ACTION_HIDE_WINDOW"



    }

    override fun onBind(intent: Intent): IBinder? {
        BuglyLog.i(TAG, "onBind called")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        BuglyLog.i(TAG, "onCreate called")
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        init()
        works()
    }

    // 启动服务
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        BuglyLog.i(TAG, "onStartCommand() -> intent: $intent, flags: $flags, startId: $startId")
        if (intent.action.isNullOrEmpty()) {
            works()
        }
        if (intent.action == ACTION_TOGGLE_WINDOW) {
            if (floatingWindow.isShowing) {
                floatingWindow.cancel()
                floatingBallWindow.show()
            } else if (floatingBallWindow.isShowing) {
                floatingBallWindow.cancel()
                floatingWindow.show()
            } else {
                floatingBallWindow.show()
            }
        }
        if (intent.action == ACTION_SHOW_WINDOW) {
            runOnUiThread {
                if (!floatingWindow.isShowing) floatingBallWindow.show()
            }
        }
        if (intent.action == ACTION_HIDE_WINDOW) {
            runOnUiThread {
                floatingBallWindow.cancel()
                floatingWindow.cancel()
            }
        }
        super.onStartCommand(intent, flags, startId)
        return START_REDELIVER_INTENT // 如果 Service 被杀死，系统会尝试重新创建 Service，并且会重新传递最后一个 Intent 给 Service 的 onStartCommand() 方法。
    }

    override fun onDestroy() {
        BuglyLog.i(TAG, "onDestroy called")
        clean()
        super.onDestroy()
    }

    override fun onCreateService() {
        TODO("Not yet implemented")
    }


    override fun onDestroyService() {
        TODO("Not yet implemented")
    }

    /**
     * startForeground 创建的通知，只能通过 startForeground 才能更新（调用本函数即可）
     * TODO: 添加权限验证
     */
    override fun showNotification(notification: Notification?) {
        if (hasPermission_FOREGROUND_SERVICE_DATA_SYNC(applicationContext)) {
            BuglyLog.d(TAG, "-> 启动/更新前台服务通知")
            // 必须首先始终调用 startService(Intent) 来告诉系统应该让服务持续运行，然后使用此方法告诉它要更努力地保持运行。
            startForeground(
                S_Notification.SILLOT_WIFI_notificationId,
                notification ?:  SillotApplication.getInstance().foregroundPushManager.builderWIFI.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun works() {
        BuglyLog.i(TAG, "works called")
        showNotification()
    }

    private fun init() {
        initWindows(application)
        registerNetworkCallback()
    }

    private fun clean() {
        BuglyLog.i(TAG, "stopService called")
        // ... 清理逻辑 ...
        runOnUiThread {
            floatingBallWindow.cancel()
            floatingWindow.cancel()
        }
        unregisterNetworkCallback()
    }

    override fun initWindows(application: Application) {
        BuglyLog.d(TAG, "-> 初始化悬浮窗")
        val springBackDraggable = SpringBackDraggable(SpringBackDraggable.ORIENTATION_HORIZONTAL)
        springBackDraggable.isAllowMoveToScreenNotch = false
        floatingBallWindow = EasyWindow.with(application)
            .setContentView(R.layout.floating_ball_layout)
            .setGravity(Gravity.START or Gravity.TOP)
            .setYOffset(200)
            .setDraggable(springBackDraggable)
            .setOnClickListener(R.id.wifi_floating_ball) { easyWindow, view ->
                BuglyLog.d(TAG, "点击了悬浮球")
                runOnUiThread {
                    easyWindow.cancel()
                    floatingWindow.show()
                }
            }
        floatingWindow = EasyWindow.with(application)
            .setContentView(R.layout.floating_window_layout)
            .setGravity(Gravity.START or Gravity.TOP)
            .setYOffset(200)
            .setDraggable(springBackDraggable)
            .setOnClickListener(R.id.close_button) { easyWindow, view ->
                BuglyLog.d(TAG, "点击了关闭按钮")
                runOnUiThread {
                    easyWindow.cancel()
                }
            }
            .setOnClickListener(R.id.hide_button) { easyWindow, view ->
                BuglyLog.d(TAG, "点击了折叠按钮")
                runOnUiThread {
                    easyWindow.cancel()
                    floatingBallWindow.show()
                }
            }
            .setOnWindowLifecycle(object : EasyWindow.OnWindowLifecycle {
                override fun onWindowShow(easyWindow: EasyWindow<*>?) {
                    super.onWindowShow(easyWindow)
                    updateWifiInfo("showInfoGlobalWindow")
                }
            })
    }

    override fun showFloatingWindow() {
        TODO("Not yet implemented")
    }

    override fun hideFloatingWindow() {
        TODO("Not yet implemented")
    }

    override fun toggleFloatingWindow() {
        TODO("Not yet implemented")
    }


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onUnavailable() {
            super.onUnavailable()
            updateWifiInfo("onUnavailable")
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            updateWifiInfo("onAvailable")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            updateWifiInfo("onLost")
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateWifiInfo("onCapabilitiesChanged")
        }
    }

    private var isNetworkCallbackRegistered = false

    /**
     * `addCapability` 将给定的能力要求添加到此构建器中。这些代表所请求网络的必需能力。
     * 请注意，在搜索满足请求的网络时，所请求的所有能力都必须得到满足。
     */
    override fun registerNetworkCallback() {
        if (isNetworkCallbackRegistered) {
            // NetworkCallback 已经注册，无需再次注册
            return
        }

        /**
         * 开关 VPN 无法触发
         */
        val networkRequests = listOf(
            networkRequestCellular,
            networkRequestWifi,
            networkRequestBluetooth,
            networkRequestEthernet,
            networkRequestVPN,
            networkRequestWifiAware,
            networkRequestUSB
        )

        networkRequests.forEach { request ->
            connectivityManager.registerNetworkCallback(
                request.build(),
                networkCallback
            )
        }


        isNetworkCallbackRegistered = true
    }

    // 在适当的生命周期函数中注销NetworkCallback，例如在Activity的onDestroy中
    override fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        isNetworkCallbackRegistered = false
    }

    /**
     * 不在 activity 中无法进行权限授予，因此不检查
     * 可手动外部调用
     * 调用链：updateWifiInfo -> performWifiScan -> updateIpAddress
     */
    override fun updateWifiInfo(reason: String) {
        BuglyLog.i(TAG, "updateWifiInfo called -> reason: $reason")
        performWifiScan()
    }


    /**
     *  执行WiFi扫描操作
     *  WifiManager 已过时， 使用 ConnectivityManager 不需要 registerWifiReceiver
     *  TODO: 原来通过 SSID 获取 WIFI 名称的方法失效了
     */
    @SuppressLint("SetTextI18n")
    override fun performWifiScan() {
        BuglyLog.i(TAG, "performWifiScan called")
        val networkInfo = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(networkInfo)
        val linkProperties = connectivityManager.getLinkProperties(networkInfo)
//        BuglyLog.d(TAG, "Link properties: $linkProperties \n\n Link capabilities: $capabilities")
        linkProperties?.let {
            BuglyLog.d(
                TAG, "dnsServers: ${it.dnsServers}, " +
                        "httpProxy: ${it.httpProxy}, isPrivateDnsActive: ${it.isPrivateDnsActive}, " +
                        "linkAddresses: ${it.linkAddresses}"
            )

            capabilities?.let { it1 ->
                // hasTransport 应该是正在使用的传输类型，不代表是否支持该传输类型或者是否开启
                val wifi = it1.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val vpn = it1.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                val lte = it1.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) // 移动网络
                val lan = it1.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) // 以太网
                val usb = it1.hasTransport(NetworkCapabilities.TRANSPORT_USB) // USB网络
                BuglyLog.d(
                    TAG,
                    "Transports: Wifi: $wifi, VPN: $vpn, LTE: $lte, LAN: $lan, USB: $usb"
                )
                BuglyLog.d(
                    TAG, "${it1.transportInfo} ${U.getWifiSignalStrengthLevel(it1.signalStrength)} " +
                            "${it1.linkDownstreamBandwidthKbps} ${it1.linkUpstreamBandwidthKbps} "
                )
                // 显示当前连接的WiFi信息以及信号强度
                val wifiDetails = StringBuilder()
                wifiDetails
                    .append("\nDNS服务: ").append(it.dnsServers.toString())
                    .append("\n")

                if (wifi) {
                    wifiDetails
                        .append("\n\n信号强度: ").append(U.getWifiSignalStrengthLevel(it1.signalStrength))
                        .append("\n当前连接类型：WIFI ")
                        .append("\n")
                }
                if (lte) {
                    wifiDetails
                        .append("\n当前连接类型：蜂窝数据 ")
                        .append("\n")
                }
                if (lan) {
                    wifiDetails
                        .append("\n当前连接类型：以太网 ")
                        .append("\n")
                }
                if (usb) {
                    wifiDetails
                        .append("\n当前连接类型：USB共享网络 ")
                        .append("\n")
                }
                if (vpn) {
                    wifiDetails
                        .append("\n正在使用 VPN")
                        .append("\n")
                }

                Handler(Looper.getMainLooper()).post {
                    floatingWindow.setText(R.id.wifi_status_textview, wifiDetails.toString())
                    updateIpAddress("点击通知，显示浮窗")
                }

            }
        } ?: {
            Handler(Looper.getMainLooper()).post {
                floatingWindow.setText(R.id.wifi_status_textview, "网络不可用")
                updateIpAddress("💔 失去连接")
            }
        }()
    }


    @SuppressLint("SetTextI18n")
    override fun updateIpAddress(notificationText: String?) {
        BuglyLog.i(TAG, "updateIpAddress called -> notificationText: $notificationText")
        val executorService: ExecutorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            try {
                var ipAddress = "0.0.0.0"
                var hostname = "Unknown"
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in Collections.list(networkInterfaces)) {
                    if (!networkInterface.name.equals("wlan0", ignoreCase = true)) continue

                    val inetAddresses = networkInterface.inetAddresses
                    for (inetAddress in Collections.list(inetAddresses)) {
                        if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
                            ipAddress = inetAddress.hostAddress as String
                            hostname = inetAddress.hostName as String
                            break
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    floatingWindow.setText(R.id.lan_ip_textview, "IP: $ipAddress \n $hostname")
                    // 没有找到替换启动前台服务的通知的解决方法，会导致通知栏显示两个通知，暂时禁用
                    // IP地址更新后，更新通知内容
                    val newNotification =  SillotApplication.getInstance().foregroundPushManager.builderWIFI
                        .setContentTitle("$hostname IP: $ipAddress")
                        .setContentText(notificationText)
                        .build()

                    // startForeground 创建的通知，只能通过 startForeground 才能更新
                    showNotification(newNotification)

                    // notify 无法更新 startForeground 创建的通知，会导致通知栏显示两个通知
//                    appIns.foregroundPushManager.notify(
//                        S_Notification.SILLOT_WIFI_notificationId,
//                        newNotification
//                    )
                }
            } catch (e: Exception) {
                PopNotification.show(TAG, "Error getting LAN IP address : $e")
            }
        }
    }

    override fun cleanResources() {
        TODO("Not yet implemented")
    }
}

/**
 *  java 请通过 `Objects.requireNonNull(getFloatingWindowServiceManager().getValue())` 访问。
 */
val floatingWindowServiceManager = lazy { FloatingWindowServiceManager() }

class FloatingWindowServiceManager(): IFloatingWindowServiceManager {
    private val TAG = "services/FloatingWindowServiceManager.kt"
    @Volatile
    override var startFloatingWindowServiceLock = false
    @Volatile
    override var isStartedActivityLock = false
    /**
     * 推荐在 activity 的 onBackground() 和 onForeground() 方法中调用
     */
    @SuppressLint("WrongConstant")
    override fun startFloatingWindowService(activity: Activity, show: Boolean) {
        isStartedActivityLock = SillotApplication.getInstance().isStartedActivity(activity)
        BuglyLog.d(TAG, "startFloatingWindowService() invoked -> ${ SillotApplication.getInstance().isStartedActivity(activity)} " +
                "activity: ${activity.javaClass.name}, show: $show")
        if (startFloatingWindowServiceLock) {
            return
        }
        if (activity.isFinishing || activity.isDestroyed) return
        startFloatingWindowServiceLock = true
        // 如果需要授权，可安全等待回到活动，如果已经授权可以直接启动服务
        XXPermissions.with(activity)
            .permission(Permission.ACCESS_FINE_LOCATION)
            .permission(Permission.POST_NOTIFICATIONS)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        Log.d(TAG, "获取部分权限成功，但部分权限未正常授予")
                        return
                    }
                    var xx = XXPermissions.with(activity).permission(Permission.SYSTEM_ALERT_WINDOW)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//                        xx = xx.permission(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
//                    }
                    xx.interceptor(PermissionInterceptor())
                        .request(object : OnPermissionCallback {
                            override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                                if (!allGranted) {
                                    Log.d(TAG, "获取部分权限成功，但部分权限未正常授予")
                                    return
                                }

                                startFloatingWindowServiceLock = false
//                                BuglyLog.d(
//                                    TAG,
//                                    "Permissions granted. ${isStartedActivityLock}"
//                                )
                                if (show && isStartedActivityLock) return
                                // 启动悬浮窗服务
                                Intent(activity, FloatingWindowService::class.java).let {
                                    it.action = if (show) FloatingWindowService.ACTION_SHOW_WINDOW
                                    else FloatingWindowService.ACTION_HIDE_WINDOW
                                    activity.startForegroundService(it)
                                }
                            }

                            override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                                startFloatingWindowServiceLock = false
                                if (doNotAskAgain) {
                                    Log.d(TAG, "被永久拒绝授权，请手动授予权限")
                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.startPermissionActivity(activity, permissions)
                                } else {
                                    Log.d(TAG, "获取权限失败")
                                }
                            }
                        })
                }
                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    startFloatingWindowServiceLock = false
                    if (never) {
                        Log.d(TAG, "被永久拒绝授权，请手动授予权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(activity, permissions)
                    } else {
                        Log.d(TAG, "获取权限失败")
                    }
                }
            })



    }

    override fun isStartedActivity(activity: Activity): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPermissionsGranted(
        permissions: MutableList<String>,
        allGranted: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(
        permissions: MutableList<String>,
        doNotAskAgain: Boolean
    ) {
        TODO("Not yet implemented")
    }

}