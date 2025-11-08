/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午10:41
 * updated: 2024/9/2 上午10:41
 */

package sc.hwd.sofill.android.events

import sc.hwd.sofill.R
import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import sc.hwd.sofill.Ss.S_Notification
import sc.hwd.sofill.android.ForegroundPush
import sc.hwd.sofill.android.ForegroundPush.applyCommonBuilderSettings
import sc.hwd.sofill.android.ForegroundPush.setActivityNotificationBuilder
import sc.hwd.sofill.android.ForegroundPush.setNotificationPendingIntent
import sc.hwd.sofill.android.ForegroundPush.setServiceNotificationBuilder
import sc.hwd.sofill.android.services.FloatingWindowService
import sc.hwd.sofill.interfaces.sofill.events.IForegroundPushManager


/**
 * description: 前台通知管理类
 * @author: <a href="https://github.com/Soltus">Soltus</a>, AI
 */
class ForegroundPushManager(private val context: Context) : IForegroundPushManager {

    override val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    override var builderGibbet = NotificationCompat.Builder(context, S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID)
    override var builderWIFI = NotificationCompat.Builder(context, S_Notification.SILLOT_WIFI_NOTIFICATION_CHANNEL_ID)

    val chanGibbet = NotificationChannel(S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID,
        S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANEL_NAME,
        NotificationManager.IMPORTANCE_HIGH)
    val chanGibbetKernel = NotificationChannel(S_Notification.SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANNEL_ID,
        S_Notification.SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANEL_NAME,
        NotificationManager.IMPORTANCE_HIGH)
    val chanWifi = NotificationChannel(S_Notification.SILLOT_WIFI_NOTIFICATION_CHANNEL_ID,
        S_Notification.SILLOT_WIFI_NOTIFICATION_CHANEL_NAME,
        NotificationManager.IMPORTANCE_HIGH)

    init {
        initChannels()
        initBuilders()
    }

    override fun initChannels() {
        Log.d(TAG, "initChannels")
        clearAllNotificationChannels()

        chanGibbet.description = S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANEL_DESC
        chanGibbet.applyCommonNotificationChannelSettings()

        chanGibbetKernel.description = S_Notification.SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANEL_DESC
        chanGibbetKernel.applyCommonNotificationChannelSettings()

        chanWifi.description = S_Notification.SILLOT_WIFI_NOTIFICATION_CHANEL_DESC
        chanWifi.applyCommonNotificationChannelSettings()

        notificationManager.createNotificationChannels(listOf(chanGibbet, chanGibbetKernel, chanWifi))
    }

    override fun initBuilders() {
        Log.d(TAG, "initBuilders")
        builderGibbet = initGibbetNotificationBuilder()
        builderWIFI = initWifiNotificationBuilder()
    }

    /**
     * 显示汐洛绞架固定通知，这就使用默认构造器，不适用于更新通知
     */
    override fun showGibbetNotification(){
        Log.d(TAG, "showGibbetNotification")
        notify(S_Notification.SILLOT_GIBBET_notificationId,
            builderGibbet.setChannelId(S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID).build())
    }


    override fun showWifiNotification(){
        Log.d(TAG, "showWifiNotification")
        notify(S_Notification.SILLOT_WIFI_notificationId,
            builderWIFI.setChannelId(S_Notification.SILLOT_WIFI_NOTIFICATION_CHANNEL_ID).build())
    }

    /**
     * 执行通知，也适用于更新通知
     */
    override fun notify(id: Int, notification: Notification) {
        Log.d(TAG, "notify -> id: $id, channelId: ${notification.channelId}")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.cancel(id)
            notificationManager.notify(TAG, id, notification)
        }
    }

    /**
     * 停止汐洛绞架固定通知
     */
    override fun stopGibbetNotification(){
        notificationManager.cancel(S_Notification.SILLOT_GIBBET_notificationId)
    }

    /**
     * 汐洛绞架服务通知
     */
//    private fun initGibbetNotificationBuilder(): NotificationCompat.Builder {
//        Log.d(TAG, "initGibbetNotificationBuilder")
//        builderGibbet.setSmallIcon(R.drawable.icon) //通知小图标
//            .setContentTitle("❤️ 来自汐洛绞架") //通知标题
//            .setContentText("点击通知，返回活动") //通知内容
//            .applyCommonBuilderSettings().let {
//                return setActivityNotificationBuilder(it, context,
//                    S_Notification.SILLOT_NOTIFICATION_REQUEST_CODE_ACTIVITY,
//                    MainActivity::class.java)
//            }
//    }
    /**
     * 汐洛绞架服务通知
     * @param targetActivityClassName 目标 Activity 类名，例如："com.example.MainActivity"
     * @param targetAction 目标 action，例如："android.intent.action.MAIN"
     */
    override fun initGibbetNotificationBuilder(
        smallIconResId: Int,
        contentTitle: String,
        contentText: String,
        targetActivityClassName: String?,
        targetAction: String?
    ): NotificationCompat.Builder {

        val intent = if (targetActivityClassName != null) {
            // 通过类名创建 Intent
            Intent().setClassName(context.packageName, targetActivityClassName)
        } else if (targetAction != null) {
            // 通过 action 创建 Intent
            Intent(targetAction)
        } else {
            // 默认创建包的主 Activity
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(context.packageName)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            launchIntent ?: Intent() // 回退方案
        }

        val pendingIntent = ForegroundPush.createActivityPendingIntent(
            context = context,
            requestCode = S_Notification.SILLOT_NOTIFICATION_REQUEST_CODE_ACTIVITY,
            activityIntent = intent
        )

        return NotificationCompat.Builder(context, S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(smallIconResId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .applyCommonBuilderSettings()
            .setNotificationPendingIntent(pendingIntent)
    }


    /**
     * WIFI状态服务通知
     */
    override fun initWifiNotificationBuilder(): NotificationCompat.Builder {
        Log.d(TAG, "initWifiNotificationBuilder")
        builderWIFI.setSmallIcon(R.drawable.icon)
            .setContentTitle("🟢 FloatingWindowService")
            .setContentText("点击通知，显示浮窗")
            .applyCommonBuilderSettings(false).let {
                return setServiceNotificationBuilder(it, context,
                    S_Notification.SILLOT_NOTIFICATION_REQUEST_CODE_SERVICE,
                    FloatingWindowService::class.java,
                    FloatingWindowService.ACTION_SHOW_WINDOW)
            }
    }


    override fun NotificationChannel.applyCommonNotificationChannelSettings(): NotificationChannel {
        this.apply {
            enableLights(false) // 呼吸灯
            setSound(null, null) // 提示音
            enableVibration(true) // 震动
        }
        return this
    }

    /**
     * 获取所有当前存在的通知渠道ID
     */
    override fun getExistingNotificationChannelIds(): Set<String> {
        return notificationManager.notificationChannels.map { it.id }.toSet()
    }

    /**
     * 清除所有通知渠道，跳过正在使用或被占用的通知渠道
     */
    override fun clearAllNotificationChannels() {
        getExistingNotificationChannelIds().forEach { channelId ->
            try {
                notificationManager.deleteNotificationChannel(channelId)
            } catch (e: Exception) {
                // 捕获异常，可能是由于通知渠道被占用导致的
                Log.w(TAG, "无法删除通知渠道 $channelId，可能正在使用或被占用", e)
            }
        }
    }

    /**
     * 清除不再需要的通知渠道
     */
    @Deprecated("暂时不用，因为有些通知渠道是动态创建的，无法预先知道。直接清理所有通知渠道即可")
    override fun clearObsoleteNotificationChannels() {
        val existingChannelIds = getExistingNotificationChannelIds()
        val channelIdsToKeep = setOf(
            S_Notification.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID,
            S_Notification.SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANNEL_ID,
            S_Notification.SILLOT_WIFI_NOTIFICATION_CHANNEL_ID
        )

        // 找出应该删除的渠道ID
        val channelIdsToDelete = existingChannelIds - channelIdsToKeep

        // 删除不再需要的渠道
        channelIdsToDelete.forEach { channelId ->
            notificationManager.deleteNotificationChannel(channelId)
        }
    }

    companion object {
        private val TAG = "ForegroundPushManager"
    }

}