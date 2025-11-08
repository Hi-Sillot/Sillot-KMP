package sc.hwd.sofill.interfaces.app

import android.service.notification.StatusBarNotification

interface INotificationAccessService {

    // region 生命周期回调
    fun onListenerConnected()
    fun onListenerDisconnected()
    // endregion

    // region 通知事件处理
    fun onNotificationPosted(sbn: StatusBarNotification?)
    fun onNotificationRemoved(sbn: StatusBarNotification?)
    // endregion

    // region 通知处理逻辑
    fun handleNotificationPosted(sbn: StatusBarNotification)
    fun handleNotificationRemoved(sbn: StatusBarNotification)
    fun logNotification(sbn: StatusBarNotification, action: String)
    // endregion

    // region 工具方法
    fun extractNotificationInfo(sbn: StatusBarNotification): NotificationInfo
    fun shouldProcessNotification(packageName: String): Boolean
    // endregion
}

data class NotificationInfo(
    val packageName: String,
    val id: Int,
    val tag: String?,
    val title: String?,
    val text: String?,
    val timestamp: Long
)