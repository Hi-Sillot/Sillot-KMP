package sc.hwd.sofill.android

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat

object ForegroundPush {
    private val TAG = "ForegroundPush"

    /**
     * 通用设置
     */
    fun NotificationCompat.Builder.applyCommonBuilderSettings(autoCancel: Boolean = true): NotificationCompat.Builder {
        this.setAutoCancel(autoCancel) //点击通知栏关闭通知
            .setOngoing(true) //不能清除通知
            .setPriority(NotificationManager.IMPORTANCE_HIGH) // 通知类别，适用“勿扰模式”
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 屏幕可见性，适用“锁屏状态”
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setSilent(true) // 静默通知 https://github.com/Hi-Windom/Sillot-android/issues/80
        return this
    }

    /**
     * 设置通知点击的PendingIntent（通用版本）
     */
    fun NotificationCompat.Builder.setNotificationPendingIntent(
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        this.setContentIntent(pendingIntent)
        return this
    }

    /**
     * 启动Service的PendingIntent
     *
     * Isolated process not allowed to call getIntentSender by createServicePendingIntent
     */
    private fun createServicePendingIntent(
        context: Context, requestCode: Int,
        serviceClass: Class<out Service>, action: String? = null): PendingIntent {
        Log.d(TAG, "createServicePendingIntent")
        val serviceIntent = Intent(context, serviceClass)
        action?.let { serviceIntent.action = it }
        return PendingIntent.getService(context, requestCode, serviceIntent, PendingIntent.FLAG_IMMUTABLE)
    }
    /**
     * 创建启动服务的PendingIntent（通用版本）
     */
    fun createServicePendingIntent(
        context: Context,
        requestCode: Int,
        serviceIntent: Intent,
        flags: Int = PendingIntent.FLAG_IMMUTABLE
    ): PendingIntent {
        return PendingIntent.getService(context, requestCode, serviceIntent, flags)
    }

    /**
     * 启动Activity的PendingIntent
     */
    private fun createActivityPendingIntent(
        context: Context, requestCode: Int,
        activityClass: Class<out Activity>, action: String? = null): PendingIntent {
        Log.d(TAG, "createActivityPendingIntent")
        val activityIntent = Intent(context, activityClass)
        action?.let { activityIntent.action = it }
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 确保可以启动Activity
        return PendingIntent.getActivity(context, requestCode, activityIntent, PendingIntent.FLAG_IMMUTABLE)
    }
    /**
     * 创建启动Activity的PendingIntent（通用版本）
     */
    fun createActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityIntent: Intent,
        flags: Int = PendingIntent.FLAG_IMMUTABLE
    ): PendingIntent {
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context, requestCode, activityIntent, flags)
    }

    /**
     * 创建广播的PendingIntent（通用版本）
     */
    fun createBroadcastPendingIntent(
        context: Context,
        requestCode: Int,
        broadcastIntent: Intent,
        flags: Int = PendingIntent.FLAG_IMMUTABLE
    ): PendingIntent {
        return PendingIntent.getBroadcast(context, requestCode, broadcastIntent, flags)
    }

    /**
     * 用于启动Service的通知构建器（兼容版本）
     */
    @Deprecated("使用setNotificationPendingIntent和createServicePendingIntent替代", ReplaceWith("setNotificationPendingIntent(createServicePendingIntent(context, requestCode, Intent(context, serviceClass).apply { action?.let { this.action = it } }))"))
    fun setServiceNotificationBuilder(
        builder: NotificationCompat.Builder, context: Context, requestCode: Int,
        serviceClass: Class<out Service>, action: String? = null): NotificationCompat.Builder {
        val intent = Intent(context, serviceClass)
        action?.let { intent.action = it }
        return builder.setNotificationPendingIntent(createServicePendingIntent(context, requestCode, intent))
    }

    /**
     * 用于启动Activity的通知构建器（兼容版本）
     */
    @Deprecated("使用setNotificationPendingIntent和createActivityPendingIntent替代", ReplaceWith("setNotificationPendingIntent(createActivityPendingIntent(context, requestCode, Intent(context, activityClass).apply { action?.let { this.action = it } }))"))
    fun setActivityNotificationBuilder(
        builder: NotificationCompat.Builder, context: Context, requestCode: Int,
        activityClass: Class<out Activity>, action: String? = null): NotificationCompat.Builder {
        val intent = Intent(context, activityClass)
        action?.let { intent.action = it }
        return builder.setNotificationPendingIntent(createActivityPendingIntent(context, requestCode, intent))
    }
}