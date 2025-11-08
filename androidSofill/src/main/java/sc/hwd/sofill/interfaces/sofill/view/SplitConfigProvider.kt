package sc.hwd.sofill.interfaces.sofill.view

import android.content.ComponentName
import android.content.Context

interface SplitConfigProvider {

    /**
     * 获取主要Activity的ComponentName
     */
    fun getPrimaryActivityComponent(context: Context): ComponentName?

    /**
     * 获取次要Activity的ComponentName
     */
    fun getSecondaryActivityComponent(context: Context): ComponentName?

    /**
     * 获取占位符Activity的ComponentName
     */
    fun getPlaceholderActivityComponent(context: Context): ComponentName?

    /**
     * 获取需要全屏显示的Activity的ComponentName列表
     */
    fun getFullScreenActivities(context: Context): List<ComponentName>

    /**
     * 获取分屏比例
     */
    fun getSplitRatio(): Float

    /**
     * 获取最小分屏宽度(dp)
     */
    fun getMinSplitWidthDp(): Int

    /**
     * 获取最小分屏最小宽度(dp)
     */
    fun getMinSmallestWidthDp(): Int
}