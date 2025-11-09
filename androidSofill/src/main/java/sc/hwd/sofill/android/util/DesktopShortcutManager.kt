package sc.hwd.sofill.android.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 桌面快捷方式管理工具类
 * 支持 Android 8.0+ 的固定快捷方式功能
 * 用法：
 * 1. 在 Application 中初始化
 * ```kotlin
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         DesktopShortcutManager.getInstance().init(this)
 *     }
 * }
 * ```
 *
 * 2. 创建快捷方式
 * ```kotlin
 * // 创建单个快捷方式
 * val shortcutConfig = DesktopShortcutManager.ShortcutConfig(
 *     id = "navigation_shortcut",
 *     shortLabel = "导航",
 *     longLabel = "快速导航",
 *     iconResId = R.drawable.ic_navigation,
 *     intent = Intent(this, MainActivity::class.java).apply {
 *         action = "com.example.NAVIGATION"
 *     },
 *     disabledMessage = "该功能暂不可用"
 * )
 *
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 *     val success = DesktopShortcutManager.getInstance().createPinShortcut(shortcutConfig)
 *     if (success) {
 *         Toast.makeText(this, "快捷方式创建请求已发送", Toast.LENGTH_SHORT).show()
 *     }
 * }
 *
 * // 批量创建快捷方式
 * val configs = listOf(
 *     DesktopShortcutManager.ShortcutConfig(
 *         id = "nav1",
 *         shortLabel = "首页",
 *         iconResId = R.drawable.ic_home,
 *         intent = Intent(this, MainActivity::class.java)
 *     ),
 *     DesktopShortcutManager.ShortcutConfig(
 *         id = "nav2",
 *         shortLabel = "设置",
 *         iconResId = R.drawable.ic_settings,
 *         intent = Intent(this, SettingsActivity::class.java)
 *     )
 * )
 *
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 *     DesktopShortcutManager.getInstance().createMultiplePinShortcuts(configs)
 * }
 * ```
 *
 * 3. 注册结果监听
 * ```kotlin
 * class MainActivity : AppCompatActivity() {
 *     private val pinnedReceiver = object : PinnedShortcutReceiver() {
 *         override fun onShortcutPinned() {
 *             runOnUiThread {
 *                 Toast.makeText(this@MainActivity, "快捷方式创建成功", Toast.LENGTH_SHORT).show()
 *             }
 *         }
 *     }
 *
 *     override fun onResume() {
 *         super.onResume()
 *         DesktopShortcutManager.getInstance().registerPinnedReceiver(pinnedReceiver)
 *     }
 *
 *     override fun onPause() {
 *         super.onPause()
 *         DesktopShortcutManager.getInstance().unregisterPinnedReceiver(pinnedReceiver)
 *     }
 * }
 * ```
 */
class DesktopShortcutManager private constructor() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: DesktopShortcutManager? = null

        /**
         * 获取单例实例
         */
        fun getInstance(): DesktopShortcutManager {
            return instance ?: synchronized(this) {
                instance ?: DesktopShortcutManager().also { instance = it }
            }
        }

        /**
         * 默认的快捷方式创建成功广播 Action
         */
        const val DEFAULT_PINNED_ACTION = "com.example.desktopshortcut.PINNED_BROADCAST"
    }

    private var applicationContext: Context? = null

    /**
     * 初始化工具类（必须在 Application 中调用）
     */
    fun init(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }
    }

    /**
     * 检查是否支持创建固定快捷方式
     */
    fun isPinShortcutSupported(): Boolean {
        val context = getValidContext() ?: return false
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        return shortcutManager.isRequestPinShortcutSupported
    }

    /**
     * 创建桌面快捷方式
     * @param shortcutConfig 快捷方式配置
     * @param broadcastAction 创建成功后的广播 Action（可选）
     * @param pendingIntentFlags PendingIntent 的 flags
     */
    fun createPinShortcut(
        shortcutConfig: ShortcutConfig,
        broadcastAction: String = DEFAULT_PINNED_ACTION,
        pendingIntentFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    ): Boolean {
        val context = getValidContext() ?: return false

        return try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            if (!shortcutManager.isRequestPinShortcutSupported) {
                return false
            }

            val shortcutInfo = buildShortcutInfo(context, shortcutConfig)
            val successCallback = createSuccessCallback(context, broadcastAction, pendingIntentFlags)

            shortcutManager.requestPinShortcut(shortcutInfo, successCallback?.intentSender)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 创建多个快捷方式（批量）
     */
    fun createMultiplePinShortcuts(
        shortcutConfigs: List<ShortcutConfig>,
        broadcastAction: String = DEFAULT_PINNED_ACTION,
        pendingIntentFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    ): Boolean {
        val context = getValidContext() ?: return false

        return try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            if (!shortcutManager.isRequestPinShortcutSupported) {
                return false
            }

            val successCallback = createSuccessCallback(context, broadcastAction, pendingIntentFlags)

            shortcutConfigs.forEach { config ->
                val shortcutInfo = buildShortcutInfo(context, config)
                shortcutManager.requestPinShortcut(shortcutInfo, successCallback?.intentSender)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取当前应用的动态快捷方式列表
     */
    fun getDynamicShortcuts(): List<ShortcutInfo> {
        val context = getValidContext() ?: return emptyList()
        return try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.dynamicShortcuts
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 移除指定的快捷方式
     */
    fun removeShortcut(shortcutId: String) {
        val context = getValidContext() ?: return
        try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.removeDynamicShortcuts(listOf(shortcutId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 移除所有动态快捷方式
     */
    fun removeAllDynamicShortcuts() {
        val context = getValidContext() ?: return
        try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.removeAllDynamicShortcuts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 禁用指定的快捷方式
     */
    fun disableShortcut(shortcutId: String, disableMessage: String? = null) {
        val context = getValidContext() ?: return
        try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.disableShortcuts(listOf(shortcutId), disableMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 添加快捷方式创建成功的广播接收器
     */
    fun registerPinnedReceiver(receiver: BroadcastReceiver, action: String = DEFAULT_PINNED_ACTION) {
        val context = getValidContext() ?: return
        try {
            val filter = IntentFilter(action)
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消注册广播接收器
     */
    fun unregisterPinnedReceiver(receiver: BroadcastReceiver) {
        val context = getValidContext() ?: return
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getValidContext(): Context? {
        if (applicationContext == null) {
            throw IllegalStateException("DesktopShortcutManager 未初始化，请在 Application 中调用 init() 方法")
        }
        return applicationContext
    }

    private fun buildShortcutInfo(context: Context, config: ShortcutConfig): ShortcutInfo {
        return ShortcutInfo.Builder(context, config.id)
            .setShortLabel(config.shortLabel)
            .setLongLabel(config.longLabel ?: config.shortLabel)
            .setIcon(Icon.createWithResource(context, config.iconResId))
            .setIntent(config.intent)
            .apply {
                config.disabledMessage?.let { setDisabledMessage(it) }
            }
            .build()
    }

    private fun createSuccessCallback(
        context: Context,
        broadcastAction: String,
        flags: Int
    ): PendingIntent? {
        return try {
            val callbackIntent = Intent(broadcastAction)
            PendingIntent.getBroadcast(context, 0, callbackIntent, flags)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 快捷方式配置数据类
     */
    data class ShortcutConfig(
        val id: String,                    // 快捷方式唯一ID
        val shortLabel: String,            // 短标签
        val longLabel: String? = null,     // 长标签（可选）
        val iconResId: Int,                // 图标资源ID
        val intent: Intent,                // 点击快捷方式后要启动的Intent
        val disabledMessage: String? = null // 禁用时显示的消息（可选）
    )
}

/**
 * 快捷方式创建结果接收器的基类
 */
abstract class PinnedShortcutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DesktopShortcutManager.DEFAULT_PINNED_ACTION) {
            onShortcutPinned()
        } else {
            onShortcutPinnedWithAction(intent.action ?: "")
        }
    }

    /**
     * 快捷方式创建成功（使用默认 Action）
     */
    abstract fun onShortcutPinned()

    /**
     * 快捷方式创建成功（使用自定义 Action）
     */
    open fun onShortcutPinnedWithAction(action: String) {
        onShortcutPinned()
    }
}