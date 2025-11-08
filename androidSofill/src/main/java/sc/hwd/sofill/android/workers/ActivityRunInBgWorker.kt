/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/26 下午12:46
 * updated: 2024/8/26 下午12:47
 */

package sc.hwd.sofill.android.workers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hjq.toast.Toaster
import sc.hwd.sofill.S.Matrix_Gibbet
import sc.hwd.sofill.Us.isInSpecialMode
import sc.hwd.sofill.Us.isTop
import sc.hwd.sofill.android.SillotApplication
import java.util.concurrent.TimeUnit
import kotlin.collections.get

/**
 * # 后台运行 Activity 的状态处理 Worker
 *
 * 原生的 `Toast.makeText.show()` 方法实测三个活动切换时存在丢失 Toast 的情况，因此使用轮子哥的 [Toaster] 库来显示 Toast。
 */
class ActivityRunInBgWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/ActivityRunInBgWorker.kt"
    @SuppressLint("ServiceCast")
    override fun doWork(): Result {
        val activityClassName = inputData.getString("activity")
        val matrixModel = inputData.getString("matrixModel")
        SillotApplication.getInstance().activityPool[activityClassName]?.let {
            if (it.isTop() && !SillotApplication.getInstance().isStartedActivity(it)) {
                // 使用Handler将Toast显示操作发送到主线程
                Handler(Looper.getMainLooper()).post {
                        if (it.isInSpecialMode() == true) {
                            Toaster.show("${matrixModel}进入特殊模式后台运行")
                        } else {
                            Toaster.show("${matrixModel}进入后台运行")
                        }
                }
                if (matrixModel == Matrix_Gibbet) {
                    SillotApplication.getInstance().foregroundPushManager.showGibbetNotification()
                }
                return Result.success()
            }
        }
        return Result.retry()
    }

    companion object {
        fun doOneTimeWork(workManager: WorkManager, inputData: Data,  uniqueWorkName: String) {
            var oneTimeWorkRequest: OneTimeWorkRequest? = null
            val constraints = Constraints.Builder()
                .build()
            // 将任务加入到WorkManager中，并设置一个UniqueWork名称
            oneTimeWorkRequest = OneTimeWorkRequest.Builder(ActivityRunInBgWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .setInitialDelay(2, TimeUnit.SECONDS) // 在频繁切换活动时至少要2秒延时才能来得及取消
                .build()
            workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE, // 测试没发现不同策略行为不同，暂用APPEND_OR_REPLACE
                oneTimeWorkRequest
            )
        }
    }
}
