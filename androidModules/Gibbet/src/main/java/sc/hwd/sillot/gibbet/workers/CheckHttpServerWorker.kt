/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午8:40
 * updated: 2024/9/2 上午8:40
 */

package sc.hwd.sillot.gibbet.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tencent.bugly.crashreport.BuglyLog
import sc.hwd.sofill.Us.formatFromLocalDateTime
import sc.hwd.sofill.android.SillotApplication
import java.time.LocalDateTime

class CheckHttpServerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/CheckHttpServerWorker.kt"
    override fun doWork(): Result {
        SillotApplication.getInstance().gibbetKernelServiceManager?.let { manager ->
            manager.service?.let { service ->
                BuglyLog.i(TAG, "check: Mobile.isHttpServing(): ${service.goIsHttpServing()}")
                val t = formatFromLocalDateTime(
                    LocalDateTime.now(),
                    "HH:mm:ss"
                )
                service.showNotification(
                    service.builderGibbetKernel?.setContentText("点击通知，唤醒内核 ($t)")
                        ?.build()
                )
                return Result.success()
            }
        }
        return Result.failure()
    }
}
