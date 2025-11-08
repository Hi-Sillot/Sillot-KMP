package sc.hwd.sofill.interfaces.sofill.events

import android.app.Notification
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import sc.hwd.sofill.R

/**
 * 前台通知管理接口
 * 定义前台通知管理的基本操作和功能
 *
 * @author: <a href="https://github.com/Soltus">Soltus</a>, AI
 */
interface IForegroundPushManager {

    // region 属性
    val notificationManager: NotificationManagerCompat
    val builderGibbet: NotificationCompat.Builder
    val builderWIFI: NotificationCompat.Builder
    // endregion

    // region 通知渠道管理
    /**
     * 初始化通知渠道
     */
    fun initChannels()

    /**
     * 初始化通知构建器
     */
    fun initBuilders()

    /**
     * 应用通用的通知渠道设置
     */
    fun NotificationChannel.applyCommonNotificationChannelSettings(): NotificationChannel

    /**
     * 获取所有当前存在的通知渠道ID
     */
    fun getExistingNotificationChannelIds(): Set<String>

    /**
     * 清除所有通知渠道，跳过正在使用或被占用的通知渠道
     */
    fun clearAllNotificationChannels()

    /**
     * 清除不再需要的通知渠道
     */
    @Deprecated("暂时不用，因为有些通知渠道是动态创建的，无法预先知道。直接清理所有通知渠道即可")
    fun clearObsoleteNotificationChannels()
    // endregion

    // region 通知显示管理
    /**
     * 显示汐洛绞架固定通知
     */
    fun showGibbetNotification()

    /**
     * 显示WIFI状态服务通知
     */
    fun showWifiNotification()

    /**
     * 执行通知，也适用于更新通知
     * @param id 通知ID
     * @param notification 通知对象
     */
    fun notify(id: Int, notification: Notification)

    /**
     * 停止汐洛绞架固定通知
     */
    fun stopGibbetNotification()
    // endregion

    // region 构建器初始化
    /**
     * 初始化汐洛绞架服务通知构建器
     * @param smallIconResId 小图标资源ID
     * @param contentTitle 通知标题
     * @param contentText 通知内容
     * @param targetActivityClassName 目标Activity类名
     * @param targetAction 目标action
     * @return 配置好的通知构建器
     */
    fun initGibbetNotificationBuilder(
        smallIconResId: Int = R.drawable.icon,
        contentTitle: String = "❤️ 来自汐洛绞架",
        contentText: String = "点击通知，返回活动",
        targetActivityClassName: String? = null,
        targetAction: String? = null
    ): NotificationCompat.Builder

    /**
     * 初始化WIFI状态服务通知构建器
     * @return 配置好的通知构建器
     */
    fun initWifiNotificationBuilder(): NotificationCompat.Builder
    // endregion

    companion object {
        const val TAG = "IForegroundPushManager"
    }
}