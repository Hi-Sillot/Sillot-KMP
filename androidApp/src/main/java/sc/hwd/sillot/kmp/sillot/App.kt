/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午10:04
 * updated: 2024/9/2 上午10:04
 */

package sc.hwd.sillot.kmp.sillot

import SillotMatrix.android.BuildConfig
import SillotMatrix.android.R
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import android.view.Gravity
import cn.jpush.android.api.JPushInterface
import com.hjq.toast.Toaster
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.kongzue.dialogx.util.views.ActivityScreenShotImageView
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.b3log.siyuan.MainActivity
import sc.hwd.sofill.S
import sc.hwd.sofill.Us.U_DEBUG
import sc.hwd.sofill.Us.U_Phone
import sc.hwd.sofill.Us.addFlagsForMatrixModel
import sc.hwd.sofill.android.SillotApplication
import sc.hwd.sofill.base.shiply_uprade


/**
 * 在Kotlin中，`by lazy` 是一种委托属性，它确保了属性的值只在首次访问时计算一次，并在后续访问时返回相同的值。而直接访问 companion object 中的属性则是直接访问那个属性。
 *
 * java 请通过 `Objects.requireNonNull(getApp())` 访问。
 */
val app by lazy { App.application }

/**
 * 汐洛 APP
 */
class App() : SillotApplication() {
    val TAG = "Sillot-App"
    var CHANNEL: String? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var application: App
            private set

    }

    fun startTargetActivity() {
        if (currentMainIntentRef == null) {
            Intent(this, MainActivity::class.java).apply {
                addFlagsForMatrixModel()
            }.also {
                Log.d(TAG, "startTargetActivity @ new $it")
                startActivity(it)
            }
        } else {
            currentMainIntentRef?.let {
                Log.d(TAG, "startTargetActivity @ $it")
                startActivity(it)
            }
        }
    }

    /**
     * 当应用程序结束时调用，但这个方法不保证一定会被调用，因为当系统杀死应用程序进程时，它不会调用onTerminate()。
     */
    override fun onTerminate() {
        super.onTerminate()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        application = this

        CoroutineScope(Dispatchers.IO).launch { // 注意非阻塞后的空指针引用
            withContext(Dispatchers.Main) {
                initSDKs(this@App)
            }
        }

    }


    /**
     * 在onCreate方法之前。这个方法的目的是将应用程序的上下文与它的基类上下文关联起来。
     *
     * Process: sc.hwd.sillot.T, PID: 7021
     *     java.lang.NoClassDefFoundError: Failed resolution of: Lcom/appjoint2/core/AppJoint2;
     *     at sc.hwd.sillot.kmp.sillot.App.attachBaseContext(App.kt:158)
     *     at android.app.Application.attach(Application.java:387)
     *     at android.app.Instrumentation.newApplication(Instrumentation.java:1353)
     */
    override fun attachBaseContext(base: Context?) {
        Log.w(TAG, "attachBaseContext -> new app base on $base")
        super.attachBaseContext(base)
        earlyInit(this)
    }

    fun reportException(throwable: Throwable?) {
        // Ensure throwable is not null before reporting
        throwable?.let {
            // 主动上传到 bugly
            CrashReport.postCatchedException(it)
        }
    }

    private fun earlyInit(context: Application) {
        CHANNEL = BuildConfig.CHANNEL
        MMKV.initialize(context)

    }

    private fun initSDKs(context: Application) {
        Log.w(TAG, "initSDKs -> channel = $CHANNEL")
        val strategy = UserStrategy(context)
        val sb = StringBuilder()
        sb.append(Build.BRAND).append("-").append(Build.MODEL).append(" (")
            .append(Build.MANUFACTURER).append(")")
        strategy.deviceModel = sb.toString()
        // 设置anr时是否获取系统trace文件，默认为false 。抓取堆栈的系统接口 Thread.getStackTrace 可能造成crash，建议只对少量用户开启
        strategy.isEnableCatchAnrTrace = true

        // 获取当前包名
        val packageName: String = context.packageName
        // 获取当前进程名
        val processName = U_DEBUG.getProcessName(Process.myPid())
        // 设置是否为上报进程
        strategy.isUploadProcess = processName == null || processName == packageName
        strategy.crashHandleCallback = object : CrashReport.CrashHandleCallback() {
            override fun onCrashHandleStart(
                crashType: Int, errorType: String,
                errorMessage: String, errorStack: String
            ): Map<String, String> {
                val map = LinkedHashMap<String, String>()
                map["Key"] = "Value"
                return map
            }

            override fun onCrashHandleStart2GetExtraDatas(
                crashType: Int, errorType: String,
                errorMessage: String, errorStack: String
            ): ByteArray {
                return try {
                    "Extra data.".toByteArray(charset("UTF-8"))
                } catch (e: Exception) {
                    e.toString().toByteArray(charset("UTF-8"))
                }
            }
        }
        CrashReport.initCrashReport(context, S.initCrashReportID, true, strategy) // 初始化 bugly
        CrashReport.setAppChannel(context, CHANNEL)
        BuglyLog.setCache(3 * 1024) // 大于阈值会持久化至文件

        JPushInterface.setDebugMode(true)
        JPushInterface.init(context)
        Toaster.init(context).also {
            // 全局配置
            Toaster.setView(R.layout.toast_custom_view)
            Toaster.setGravity(Gravity.TOP)
        }
        DialogX.globalTheme = DialogX.THEME.AUTO
        DialogX.autoRunOnUIThread = true // 自动在主线程执行
        if (U_Phone.isMIUI(applicationContext) || U_Phone.isLargeScreenMachine(context)) {
            DialogX.globalStyle = MIUIStyle()
        } else {
            // 其他主题感觉都不好看，暂时默认，以后可能自己弄个
        }
        ActivityScreenShotImageView.hideContentView =
            true; // https://github.com/kongzue/DialogX/wiki/%E5%85%A8%E5%B1%8F%E5%AF%B9%E8%AF%9D%E6%A1%86-FullScreenDialog

        shiply_uprade(CHANNEL, applicationContext)
    }

}

