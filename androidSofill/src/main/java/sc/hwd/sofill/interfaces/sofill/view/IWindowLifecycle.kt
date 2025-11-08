package sc.hwd.sofill.interfaces.sofill.view

/**
 * 窗口生命周期回调接口
 * 定义悬浮窗口的生命周期事件
 */
interface IWindowLifecycle {

    /**
     * 窗口显示时调用
     * @param window 窗口对象
     */
    fun onWindowShow(window: Any?)

    /**
     * 窗口隐藏时调用
     * @param window 窗口对象
     */
    fun onWindowHide(window: Any?)

    /**
     * 窗口关闭时调用
     * @param window 窗口对象
     */
    fun onWindowCancel(window: Any?)
}