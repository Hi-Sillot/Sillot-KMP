package sc.hwd.sofill.interfaces.sofill.view

import android.app.Application
import android.app.Notification
import android.content.Intent

/**
 * 悬浮窗服务接口
 * 定义悬浮窗服务的基本操作和功能
 *
 * @author: <a href="https://github.com/Soltus">Soltus</a>, AI
 */
interface IFloatingWindowService {

    // region 伴生对象常量
    companion object {
        const val ACTION_TOGGLE_WINDOW = "ACTION_TOGGLE_WINDOW"
        const val ACTION_SHOW_WINDOW = "ACTION_SHOW_WINDOW"
        const val ACTION_HIDE_WINDOW = "ACTION_HIDE_WINDOW"
        const val TAG = "IFloatingWindowService"
    }
    // endregion

    // region 服务生命周期
    /**
     * 服务创建时的初始化操作
     */
    fun onCreateService()

    /**
     * 服务启动命令处理
     * @param intent 启动意图
     * @param flags 启动标志
     * @param startId 启动ID
     * @return 启动模式
     */
    fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int

    /**
     * 服务销毁时的清理操作
     */
    fun onDestroyService()
    // endregion

    // region 通知管理
    /**
     * 显示或更新前台服务通知
     * @param notification 通知对象，为null时使用默认通知
     */
    fun showNotification(notification: Notification? = null)
    // endregion

    // region 悬浮窗管理
    /**
     * 初始化悬浮窗
     * @param application 应用上下文
     */
    fun initWindows(application: Application)

    /**
     * 显示悬浮窗
     */
    fun showFloatingWindow()

    /**
     * 隐藏悬浮窗
     */
    fun hideFloatingWindow()

    /**
     * 切换悬浮窗显示状态
     */
    fun toggleFloatingWindow()


    // region 网络监控
    /**
     * 注册网络状态回调
     */
    fun registerNetworkCallback()

    /**
     * 注销网络状态回调
     */
    fun unregisterNetworkCallback()

    /**
     * 更新WiFi信息
     * @param reason 更新原因
     */
    fun updateWifiInfo(reason: String)

    /**
     * 执行WiFi扫描操作
     */
    fun performWifiScan()

    /**
     * 更新IP地址显示
     * @param notificationText 通知文本
     */
    fun updateIpAddress(notificationText: String? = "Wifi : 没有诶")
    // endregion

    // region 清理操作
    /**
     * 清理资源
     */
    fun cleanResources()
    // endregion
}