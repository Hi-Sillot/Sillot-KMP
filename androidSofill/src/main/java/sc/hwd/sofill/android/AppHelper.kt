/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午7:39
 * updated: 2024/9/2 上午7:39
 */

package sc.hwd.sofill.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.work.Data
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sc.hwd.sofill.S
import sc.hwd.sofill.Us.U_Phone.setPreferredDisplayMode
import sc.hwd.sofill.android.events.ForegroundPushManager
import sc.hwd.sofill.android.lifecycle.AppMonitor
import sc.hwd.sofill.android.view.SplitManager
import sc.hwd.sofill.android.workers.ActivityRunInBgWorker
import sc.hwd.sofill.annotations.SillotActivity
import sc.hwd.sofill.annotations.SillotActivityType
import sc.hwd.sofill.interfaces.gibbet.IGibbetKernelServiceManager
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "Sofill-AppHelper"

class AppHelper {

}


open class SillotApplication : Application() {
    var versionName: String = ""
    var versionCode: Int = 0
    var BuildConfig_PROVIDER_AUTHORITIES = ""


    companion object {
        private lateinit var instance: SillotApplication
        @JvmStatic
        fun getInstance(): SillotApplication {
            return instance
        }
    }

    lateinit var foregroundPushManager: ForegroundPushManager


    @JvmField
    @Volatile
    var currentMainIntentRef: Intent? = null
    fun isStartedActivity(activity: Activity): Boolean {
        return activity.javaClass.name == currentStartedActivityRef?.javaClass?.name
    }


    @JvmField
    @Volatile
    var currentStartedActivityRef: Activity? = null
    @JvmField
    @Volatile
    var activityPool: ConcurrentHashMap<String, Activity> = ConcurrentHashMap()

    val gibbetKernelServiceManager: IGibbetKernelServiceManager? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        SplitManager.Companion.createSplit(this)
        foregroundPushManager = ForegroundPushManager(this)

        CoroutineScope(Dispatchers.IO).launch { // 注意非阻塞后的空指针引用
            registerActivityLifecycleCB()
            initAppMonitor()
        }

    }


    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(
            TAG,
            "onLowMemory() invoked"
        )
    }

    private fun rmActivity(activity: Activity) {
        activityPool.remove(activity.javaClass.name)
    }
    private fun setActivity(activity: Activity) {
        activityPool[activity.javaClass.name] = activity
    }

    private fun initAppMonitor() {
        val funTAG = "$TAG AppMonitor"
        //初始化
        AppMonitor.initialize(this, true)
        //注册监听 App 状态变化（前台，后台）
        AppMonitor.registerAppStatusCallback(object : AppMonitor.OnAppStatusCallback {
            override fun onAppForeground(activity: Activity) {
                //App 切换到前台
                Log.d(funTAG, "onAppForeground(Activity = $activity)")
            }

            override fun onAppBackground(activity: Activity) {
                //App 切换到后台
                Log.d(funTAG, "onAppBackground(Activity = $activity)")
            }

        })
        //注册监听 Activity 状态变化
        AppMonitor.registerActivityStatusCallback(object : AppMonitor.OnActivityStatusCallback {
            override fun onAliveStatusChanged(
                activity: Activity,
                isAliveState: Boolean,
                aliveActivityCount: Int
            ) {
                //Activity 的存活状态或数量发生变化
                Log.d(
                    funTAG,
                    "onAliveStatusChanged(Activity = $activity, isAliveState = $isAliveState, aliveActivityCount = $aliveActivityCount)"
                )
            }

            override fun onActiveStatusChanged(
                activity: Activity,
                isActiveState: Boolean,
                activeActivityCount: Int
            ) {
                //Activity 的活跃状态或数量发生变化
                Log.d(
                    funTAG,
                    "onActiveStatusChanged(Activity = $activity, isActiveState = $isActiveState, activeActivityCount = $activeActivityCount)"
                )
            }

        })

        //注册监听屏幕状态变化（开屏、关屏、解锁）
        AppMonitor.registerScreenStatusCallback(object : AppMonitor.OnScreenStatusCallback {
            override fun onScreenStatusChanged(isScreenOn: Boolean) {
                //屏幕状态发生变化（开屏或关屏）
                Log.d(funTAG, "onScreenStatusChanged(isScreenOn = $isScreenOn)")
            }

            override fun onUserPresent() {
                //解锁：当设备唤醒后，用户在（解锁键盘消失）时回调
                Log.d(funTAG, "onUserPresent()")
            }

        })
    }

    private fun registerActivityLifecycleCB() {

        var refCount = 0
        val workManager = WorkManager.getInstance(this)


        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                if (isStartedActivity(activity)) currentStartedActivityRef = null
            }

            override fun onActivityStarted(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                refCount++
                currentStartedActivityRef = activity

                val annotations =
                    activity.javaClass.getAnnotationsByType(SillotActivity::class.java)

                // 遍历注解并处理每个注解
                annotations.forEach { annotation ->
                    Log.d(
                        TAG,
                        "onActivityStarted() invoked -> the activity's annotation.TYPE ${annotation.TYPE}"
                    )
                    if (annotation.TYPE == SillotActivityType.Main) {
                        currentMainIntentRef = activity.intent
                    }
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.w(
                    TAG,
                    "[$refCount] onActivityDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                rmActivity(activity)
                if (refCount == 0) {
                    // 所有Activity都销毁了，执行清理操作
                    currentStartedActivityRef = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(
                    TAG,
                    "onActivitySaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            @SuppressLint("RestrictedApi")
            override fun onActivityStopped(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                refCount--
                // 检查活动是否是MatrixModel的实例
                if (activity is MatrixModel) {
                    val matrixModel = activity.getMatrixModel()
                    Log.w("App", "Matrix_model: $matrixModel")
                    val data = Data.Builder()
                        .putString("activity", activity.javaClass.name)
                        .putString("matrixModel", matrixModel)
                        .build()
                    ActivityRunInBgWorker.doOneTimeWork(workManager, data, "${activity.javaClass.name}${S.activityRunNote1}")
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(
                    TAG,
                    "onActivityCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                setActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                if (activity is MatrixModel) {
                    val matrixModel = activity.getMatrixModel()
                    Log.w("App", "Matrix_model: $matrixModel")
                    if (matrixModel == S.Matrix_Gibbet) {
                        foregroundPushManager.stopGibbetNotification()
                    }
                }
            }

            override fun onActivityPreDestroyed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreDestroyed(activity)
            }

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(
                    TAG,
                    "onActivityPreCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                activity.setPreferredDisplayMode() // 全局高刷新率
                super.onActivityPreCreated(activity, savedInstanceState)
            }

            override fun onActivityPreStarted(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                workManager.cancelUniqueWork("${activity.javaClass.name}${S.activityRunNote1}")
                super.onActivityPreStarted(activity)
            }

            override fun onActivityPreStopped(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreStopped(activity)
            }

            override fun onActivityPrePaused(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPrePaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPrePaused(activity)
            }

            override fun onActivityPreResumed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreResumed(activity)
            }

            override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(
                    TAG,
                    "onActivityPostCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostCreated(activity, savedInstanceState)
            }

            override fun onActivityPostDestroyed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostDestroyed(activity)
            }

            override fun onActivityPostPaused(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostPaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostPaused(activity)
            }

            override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(
                    TAG,
                    "onActivityPostSaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostSaveInstanceState(activity, outState)
            }

            override fun onActivityPostResumed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostResumed(activity)
            }

            override fun onActivityPostStarted(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostStarted(activity)
            }

            override fun onActivityPostStopped(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostStopped(activity)
            }

            override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(
                    TAG,
                    "onActivityPreSaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreSaveInstanceState(activity, outState)
            }

        })
    }


}
