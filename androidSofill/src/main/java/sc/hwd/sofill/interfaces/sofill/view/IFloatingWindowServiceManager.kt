package sc.hwd.sofill.interfaces.sofill.view

import android.app.Activity

/**
 * 悬浮窗服务管理器接口
 * 定义悬浮窗服务的启动和管理操作
 *
 * @author: <a href="https://github.com/Soltus">Soltus</a>, AI
 */
interface IFloatingWindowServiceManager {

    // region 属性
    /**
     * 服务启动锁，防止重复启动
     */
    val startFloatingWindowServiceLock: Boolean

    /**
     * 活动启动状态锁
     */
    val isStartedActivityLock: Boolean
    // endregion

    // region 服务管理
    /**
     * 启动悬浮窗服务
     * @param activity 活动上下文
     * @param show 是否显示悬浮窗
     */
    fun startFloatingWindowService(activity: Activity, show: Boolean)

    /**
     * 检查活动是否已启动
     * @param activity 要检查的活动
     * @return 是否已启动
     */
    fun isStartedActivity(activity: Activity): Boolean
    // endregion

    // region 权限回调
    /**
     * 权限授予回调
     * @param permissions 授予的权限列表
     * @param allGranted 是否全部授予
     */
    fun onPermissionsGranted(permissions: MutableList<String>, allGranted: Boolean)

    /**
     * 权限拒绝回调
     * @param permissions 拒绝的权限列表
     * @param doNotAskAgain 是否永久拒绝
     */
    fun onPermissionsDenied(permissions: MutableList<String>, doNotAskAgain: Boolean)
    // endregion

    companion object {
        const val TAG = "IFloatingWindowServiceManager"
    }
}