/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/28 上午1:25
 * updated: 2024/8/28 上午1:25
 */

package sc.hwd.sofill.Us

import android.os.Build

enum class Channels(val channel: Int) {
    /**
     * # 金丝雀渠道
     */
    Canary(1),
    /**
     * # 先锋渠道
     */
    Pioneer(2),
    /**
     * # 洛可可渠道
     */
    Rococo(3);

    companion object {
        fun fromLevel(level: Int) = entries.firstOrNull { it.channel == level }
        fun fromName(name: String) = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

        /**
         * 不指定参数二时判断是否合法，否则判断是否大于等于指定参数二的优先级
         */
        fun allowChannel(channelName: String?, compareLevel: Channels? = null): Boolean {
            if (channelName.isNullOrBlank()) return false
            if (compareLevel == null) return Channels.fromName(channelName) != null
            val channelPriority = Channels.fromName(channelName)?.channel ?: return false
            return channelPriority >= compareLevel.channel
        }
    }
}


/**
 * Build.SUPPORTED_ABIS 返回一个字符串数组，包含了设备支持的所有的ABI。
 * 这里只是简单地返回第一个支持的ABI，通常是优先级最高的
 */
fun getAbi(): String {
    return Build.SUPPORTED_ABIS[0].toString()
}