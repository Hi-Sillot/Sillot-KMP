/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/22 下午9:54
 * updated: 2024/8/22 下午9:54
 */

package sc.hwd.sofill.android.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.window.embedding.ActivityFilter
import androidx.window.embedding.ActivityRule
import androidx.window.embedding.EmbeddingAspectRatio
import androidx.window.embedding.RuleController
import androidx.window.embedding.SplitAttributes
import androidx.window.embedding.SplitPairFilter
import androidx.window.embedding.SplitPairRule
import androidx.window.embedding.SplitPlaceholderRule
import androidx.window.embedding.SplitRule
import sc.hwd.sofill.interfaces.sofill.view.SplitConfigProvider

/**
 * [ref](https://developer.android.google.cn/codelabs/large-screens/activity-embedding?hl=zh-cn#5)
 * [ref2](https://developer.android.google.cn/guide/topics/large-screens/activity-embedding?hl=zh-cn)
 * [ref3](https://developer.android.google.cn/reference/kotlin/androidx/window/embedding/package-summary)
 *
 * 使用 activity 嵌入时，您无法使用 WindowManager 方法 `getCurrentWindowMetrics()` 和 `getMaximumWindowMetrics()` 来确定屏幕宽度，
 * 因为这些方法返回的窗口指标描述了包含调用方法的嵌入 activity 的显示窗格。如需获取 activity 嵌入应用的准确尺寸，
 * 请使用[分屏属性计算器](https://developer.android.google.cn/reference/kotlin/androidx/window/embedding/SplitController?hl=zh-cn#setSplitAttributesCalculator(kotlin.Function1))
 * 和 [SplitAttributesCalculatorParams](https://developer.android.google.cn/reference/kotlin/androidx/window/embedding/SplitAttributesCalculatorParams?hl=zh-cn)。
 */
//class SplitManager_old {
//
//    companion object {
//
//        /**
//         * 创建分屏规则
//         */
//        fun createSplit(context: Context) {
//            // 创建一个分屏对过滤器，标识共享分屏的 activity
//            // 该过滤器可以包含用于启动辅助 activity 的 intent 操作（第三个参数）。如果您添加了 intent 操作，过滤器会检查该操作以及 activity 名称。
//            // 对于您自己应用中的 activity，您可能不需要过滤 intent 操作，因此该参数可以为 null。
//            val splitPairFilter = SplitPairFilter(
//                ComponentName(context, MainActivity::class.java),
//                ComponentName(context, WebViewActivity::class.java),
//                null
//            )
////            val splitPairFilter = SplitPairFilter(
////                ComponentName(context, "*"),
////                ComponentName(context, "*"),
////                null
////            )
//            // 将过滤条件添加到过滤条件集
//            val filterSet = setOf(splitPairFilter)
//            // 为分屏创建布局属性
//            val splitAttributes: SplitAttributes = SplitAttributes.Builder()
//                // 定义将可用显示区域分配给每个 activity 容器的方式。宽高比分屏类型指定主要容器占据的显示区域比例；辅助容器则会占据剩余的显示区域。
//                .setSplitType(SplitAttributes.SplitType.ratio(0.58f))
//                // 指定 activity 容器相对于另一种容器的布局方式，主要容器优先。
//                .setLayoutDirection(SplitAttributes.LayoutDirection.LEFT_TO_RIGHT)
//                .build()
//            // 构建分屏对规则
//            val splitPairRule = SplitPairRule.Builder(filterSet)
//                // 将布局属性应用于规则
//                .setDefaultSplitAttributes(splitAttributes)
//                // 设置允许分屏的最小显示宽度（以密度无关像素 dp 为单位）。
//                .setMinWidthDp(840)
//                // 设置允许分屏的最小值（以 dp 为单位），无论设备显示方向如何，都必须确保两个显示屏尺寸中较小的尺寸不低于该值才允许分屏。
//                .setMinSmallestWidthDp(600)
//                // 设置结束辅助容器中的所有 activity 会对主要容器中的 activity 有何影响。
//                // NEVER 表示在辅助容器中的所有 activity 均结束时，系统不应结束主要 activity。
//                .setFinishPrimaryWithSecondary(SplitRule.FinishBehavior.NEVER)
//                // 设置结束主要容器中的所有 activity 会对辅助容器中的 activity 有何影响。
//                // ALWAYS 表示当主要容器中的所有 activity 均结束时，系统应始终结束辅助容器中的 activity。
//                .setFinishSecondaryWithPrimary(SplitRule.FinishBehavior.ALWAYS)
//                // 指定在辅助容器中启动新 activity 时，该容器中的所有 activity 是否都已结束。
//                // false 表示新 activity 会堆叠在辅助容器中已有的 activity 之上。
//                .setClearTop(false)
//                // 设置在使用拆分时，竖屏状态下父窗口边界的宽高比的最大值。
//                // 当窗口宽高比大于此处请求的值时，次要容器中的活动将堆叠在主要容器中的活动之上，完全覆盖它们。
//                // 此值仅在父窗口为竖屏（高度 >= 宽度）时使用。
//                .setMaxAspectRatioInPortrait(EmbeddingAspectRatio.ratio(1.5f))
//                .build()
//            // 获取 WindowManager RuleController 的单例实例并添加规则
//            val ruleController = RuleController.getInstance(context)
//            ruleController.addRule(splitPairRule)
//
//        }
//
//        /**
//         * 创建占位符规则
//         */
//        fun createPlaceholder(context: Context) {
//            // 创建 ActivityFilter
//            val placeholderActivityFilter = ActivityFilter(
//                ComponentName(context, HomeActivity::class.java),
//                null
//            )
//            // 为分屏创建布局属性
//            val splitAttributes: SplitAttributes = SplitAttributes.Builder()
//                // 定义将可用显示区域分配给每个 activity 容器的方式。宽高比分屏类型指定主要容器占据的显示区域比例；辅助容器则会占据剩余的显示区域。
//                .setSplitType(SplitAttributes.SplitType.ratio(0.33f))
//                // 指定 activity 容器相对于另一种容器的布局方式，主要容器优先。
//                .setLayoutDirection(SplitAttributes.LayoutDirection.LEFT_TO_RIGHT)
//                .build()
//            // 将过滤条件添加到过滤条件集
//            val placeholderActivityFilterSet = setOf(placeholderActivityFilter)
//            // 创建 SplitPlaceholderRule
//            val splitPlaceholderRule = SplitPlaceholderRule.Builder(
//                // 包含 activity 过滤条件，通过确定与占位符 activity 相关联的 activity 来确定何时应用规则。
//                placeholderActivityFilterSet,
//                // 指定占位符 activity 的启动状态。
//                Intent(context, WebViewActivity::class.java)
//            )
//                // 将布局属性应用于规则。
//                .setDefaultSplitAttributes(splitAttributes)
//                // 设置允许分屏的最小显示宽度（以密度无关像素 dp 为单位）。
//                .setMinWidthDp(840)
//                // 设置允许分屏的最小值（以 dp 为单位），无论设备显示方向如何，都必须确保两个显示屏尺寸中较小的尺寸不低于该值才允许分屏。
//                .setMinSmallestWidthDp(600)
//                // 置结束占位符 activity 会对主要容器中的 activity 有何影响。
//                // ALWAYS 表示当占位符 activity 结束时，系统应始终结束主要容器中的 activity。
//                .setFinishPrimaryWithPlaceholder(SplitRule.FinishBehavior.ALWAYS)
//                .build()
//            // 向 WindowManager RuleController 添加规则
//            val ruleController = RuleController.getInstance(context)
//            ruleController.addRule(splitPlaceholderRule)
//
//        }
//
//        /**
//         * 创建拒绝分屏 activity 规则
//         */
//        fun createActivity(context: Context) {
//            // 创建 ActivityFilter
//            val summaryActivityFilter = ActivityFilter(
//                ComponentName(context, "*"),
//                null
//            )
//            // 将过滤条件添加到过滤条件集
//            val summaryActivityFilterSet = setOf(summaryActivityFilter)
//            // 创建 ActivityRule
//            val activityRule = ActivityRule.Builder(
//                // 包含 activity 过滤条件，通过确定您希望从分屏中排除的 activity，以确定何时应用规则。
//                summaryActivityFilterSet
//            )
//                // 指定 activity 是否应展开以填充所有可用的显示空间。
//                .setAlwaysExpand(true)
//                .build()
//            // 向 WindowManager RuleController 添加规则
//            val ruleController = RuleController.getInstance(context)
//            ruleController.addRule(activityRule)
//
//        }
//    }
//}


/**
 * 分屏管理器 - 重构版本
 * 通过配置接口解耦具体Activity依赖
 */
class SplitManager private constructor(
    private val configProvider: SplitConfigProvider
) {

    companion object {
        private var instance: SplitManager? = null

        /**
         * 初始化分屏管理器
         */
        @JvmStatic
        fun initialize(configProvider: SplitConfigProvider = DefaultSplitConfigProvider()) {
            if (instance == null) {
                instance = SplitManager(configProvider)
            }
        }

        /**
         * 获取分屏管理器实例
         */
        @JvmStatic
        fun getInstance(): SplitManager {
            return instance ?: throw IllegalStateException("SplitManager not initialized. Call initialize() first.")
        }

        /**
         * 快速创建分屏规则（兼容旧版本）
         */
        @JvmStatic
        fun createSplit(context: Context, configProvider: SplitConfigProvider = DefaultSplitConfigProvider()) {
            initialize(configProvider)
            getInstance().setupSplitRules(context)
        }
    }

    /**
     * 设置所有分屏规则
     */
    fun setupSplitRules(context: Context) {
        createSplitPairRule(context)
        createPlaceholderRule(context)
        createActivityRules(context)
    }

    /**
     * 创建分屏对规则
     */
    private fun createSplitPairRule(context: Context) {
        val primaryComponent = configProvider.getPrimaryActivityComponent(context)
        val secondaryComponent = configProvider.getSecondaryActivityComponent(context)

        if (primaryComponent == null || secondaryComponent == null) {
            // 如果无法获取ComponentName，跳过创建规则
            return
        }

        val splitPairFilter = SplitPairFilter(
            primaryComponent,
            secondaryComponent,
            null
        )

        val filterSet = setOf(splitPairFilter)

        val splitAttributes = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(configProvider.getSplitRatio()))
            .setLayoutDirection(SplitAttributes.LayoutDirection.LEFT_TO_RIGHT)
            .build()

        val splitPairRule = SplitPairRule.Builder(filterSet)
            .setDefaultSplitAttributes(splitAttributes)
            .setMinWidthDp(configProvider.getMinSplitWidthDp())
            .setMinSmallestWidthDp(configProvider.getMinSmallestWidthDp())
            .setFinishPrimaryWithSecondary(SplitRule.FinishBehavior.NEVER)
            .setFinishSecondaryWithPrimary(SplitRule.FinishBehavior.ALWAYS)
            .setClearTop(false)
            .setMaxAspectRatioInPortrait(EmbeddingAspectRatio.ratio(1.5f))
            .build()

        RuleController.getInstance(context).addRule(splitPairRule)
    }

    /**
     * 创建占位符规则
     */
    private fun createPlaceholderRule(context: Context) {
        val placeholderComponent = configProvider.getPlaceholderActivityComponent(context) ?: return

        val placeholderActivityFilter = ActivityFilter(
            configProvider.getPrimaryActivityComponent(context) ?: return,
            null
        )

        val splitAttributes = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.33f))
            .setLayoutDirection(SplitAttributes.LayoutDirection.LEFT_TO_RIGHT)
            .build()

        val placeholderActivityFilterSet = setOf(placeholderActivityFilter)

        val splitPlaceholderRule = SplitPlaceholderRule.Builder(
            placeholderActivityFilterSet,
            Intent().setComponent(placeholderComponent)
        )
            .setDefaultSplitAttributes(splitAttributes)
            .setMinWidthDp(configProvider.getMinSplitWidthDp())
            .setMinSmallestWidthDp(configProvider.getMinSmallestWidthDp())
            .setFinishPrimaryWithPlaceholder(SplitRule.FinishBehavior.ALWAYS)
            .build()

        RuleController.getInstance(context).addRule(splitPlaceholderRule)
    }

    /**
     * 创建Activity规则
     */
    private fun createActivityRules(context: Context) {
        val fullScreenActivities = configProvider.getFullScreenActivities(context)

        fullScreenActivities.forEach { componentName ->
            val activityFilter = ActivityFilter(componentName, null)
            val activityFilterSet = setOf(activityFilter)

            val activityRule = ActivityRule.Builder(activityFilterSet)
                .setAlwaysExpand(true)
                .build()

            RuleController.getInstance(context).addRule(activityRule)
        }
    }

    /**
     * 清除所有规则
     */
    fun clearRules(context: Context) {
        RuleController.getInstance(context).clearRules()
    }

    /**
     * 更新配置
     */
    fun updateConfig(newConfigProvider: SplitConfigProvider) {
        // 这里可以添加逻辑来动态更新配置
        // 注意：需要重新设置规则才能生效
    }
}


/**
 * 默认分屏配置实现
 */
class DefaultSplitConfigProvider : SplitConfigProvider {

    override fun getSplitRatio(): Float = 0.58f

    override fun getMinSplitWidthDp(): Int = 840

    override fun getMinSmallestWidthDp(): Int = 600

    override fun getPrimaryActivityComponent(context: Context): ComponentName? {
        // 通过反射或其他方式动态获取，避免直接依赖
        return try {
            val className = "org.b3log.siyuan.MainActivity" // 配置化
            ComponentName(context, className)
        } catch (e: Exception) {
            null
        }
    }

    override fun getSecondaryActivityComponent(context: Context): ComponentName? {
        return try {
            val className = "sc.hwd.sofill.compose.WebViewActivity" // 配置化
            ComponentName(context, className)
        } catch (e: Exception) {
            null
        }
    }

    override fun getPlaceholderActivityComponent(context: Context): ComponentName? {
        return try {
            val className = "org.b3log.ld246.HomeActivity" // 配置化
            ComponentName(context, className)
        } catch (e: Exception) {
            null
        }
    }

    override fun getFullScreenActivities(context: Context): List<ComponentName> {
        // 返回需要全屏显示的Activity列表
        return emptyList()
    }
}