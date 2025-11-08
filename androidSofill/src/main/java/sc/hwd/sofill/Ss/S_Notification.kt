/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/24 上午11:06
 * updated: 2024/8/24 上午11:06
 */

package sc.hwd.sofill.Ss

object S_Notification {

    // 通知ID，用于重复更新的通知
    const val SILLOT_GIBBET_notificationId = 58666
    const val SILLOT_GIBBET_KERNEL_notificationId = 58131
    const val USB_AUDIO_EXCLUSIVE_notificationId = 7654321
    const val SILLOT_WIFI_notificationId = 90001

    // 用户可见的通道名称
    const val SILLOT_GIBBET_NOTIFICATION_CHANEL_NAME = "🦢 汐洛绞架通知服务"
    const val SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANEL_NAME = "🦢 汐洛绞架内核通知服务"
    const val SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANEL_NAME = "🦢 汐洛音乐播放服务"
    const val SILLOT_WIFI_NOTIFICATION_CHANEL_NAME = "🦢 汐洛WIFI通知服务"

    // 用户可见的通道描述
    const val SILLOT_GIBBET_NOTIFICATION_CHANEL_DESC = "🤍 汐洛绞架通知服务描述"
    const val SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANEL_DESC = "🤍 汐洛绞架内核通知服务描述"
    const val SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANEL_DESC = "🤍 汐洛音乐播放服务描述"
    const val SILLOT_WIFI_NOTIFICATION_CHANEL_DESC = "🤍 汐洛WIFI通知服务描述"

    // 通知频道ID
    const val channel_id_prefix = "sillot_notification_channel_id_"
    const val SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID = "$channel_id_prefix$SILLOT_GIBBET_notificationId"
    const val SILLOT_GIBBET_KERNEL_NOTIFICATION_CHANNEL_ID = "$channel_id_prefix$SILLOT_GIBBET_KERNEL_notificationId"
    const val SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID = "$channel_id_prefix$USB_AUDIO_EXCLUSIVE_notificationId"
    const val SILLOT_WIFI_NOTIFICATION_CHANNEL_ID = "$channel_id_prefix$SILLOT_WIFI_notificationId"

    const val SILLOT_NOTIFICATION_REQUEST_CODE_ACTIVITY = 1
    const val SILLOT_NOTIFICATION_REQUEST_CODE_SERVICE = 2

}