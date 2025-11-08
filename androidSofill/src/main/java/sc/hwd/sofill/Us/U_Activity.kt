/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/25 上午12:32
 * updated: 2024/8/25 上午12:32
 */

package sc.hwd.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.WindowManager


fun Activity.isInSpecialMode(): Boolean {
    return this.isInMultiWindowMode || this.isInFreeformMode() || this.isInPictureInPictureMode
}


@SuppressLint("ObsoleteSdkInt")
fun Activity.isInFreeformMode(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // 从Android M开始，可以通过判断窗口属性来检测是否为Freeform模式
        window.attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    } else {
        false
    }
}


/**
 * 禁止截屏
 */
fun Activity.disableScreenshot() {
    this.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

/**
 * 允许截屏
 */
fun Activity.enableScreenshot() {
    this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

/**
 * ### Activity 处于栈顶, 当频繁切换 Activity 时需要此判断
 *
 * 判断当前 Activity 是否位于栈顶，如果没有切换活动，那么当前 Activity 就是栈顶，即使它位于后台。
 * 结果仅供参考，不保证准确性，仅限 app 内使用。
 */
fun Activity.isTop(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    // 这个方法对第三方应用程序不再可用：以文档为中心的最近任务列表的引入意味着它可能会将个人信息泄露给调用者。
    // 为了向后兼容，它仍然会返回其数据的一个小子集：至少包括调用者自己的任务，可能还包括一些已知不敏感的其他任务，如主屏幕任务。
    // 在汐洛分包前，此方法仍然有效。`task.topActivity` 实测三个活动切换依旧有效
    val runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE)
    for (task in runningTasks) {
        task.topActivity?.let {
            Log.d("isTop", "topActivity: ${it.className}, currentActivity: ${this.javaClass.name}")
            if (
                this.javaClass.name == it.className
            ) {
                return true
            }
        }
    }
    return false
}


/**
 * ### 启动 Gibbet Activity 并传递 blockURL
 *
 * @param activityClass: 目标Activity的Class，如 `MainActivity::class.java`
 * @param blockURL: 格式为 `siyuan://blocks/xxx`
 * @param shouldAddSpecialFlags: 是否添加特殊的启动模式，调用时判断 Activity 是否已启动
 */
inline fun <reified T : Activity> Activity.startGibbetActivityWithBlock(
    activityClass: Class<T>,
    blockURL: String,
    shouldAddSpecialFlags: Boolean = false
) {
    Intent(applicationContext, activityClass).apply {
        if (shouldAddSpecialFlags) {
            addFlagsForMatrixModel()
        }
        putExtra("blockURL", blockURL)
    }.also {
        startActivity(it)
    }
}