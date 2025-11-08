/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/24 下午10:52
 * updated: 2024/8/24 下午10:52
 */

package sc.hwd.sofill.android

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

abstract class MatrixModel: FragmentActivity(),LifecycleEventObserver {
    /**
     * 基于此类的活动需要重写，例如汐洛绞架
     * 抽象方法。这样，任何继承自MatrixModel的类都必须实现这个方法，否则它们将无法被实例化。
     */
    abstract fun getMatrixModel(): String

    /**
     * ## 当活动进入前台时调用的方法，活动启动时不会调用
     *
     * 代替 `com.blankj.utilcode.util.Utils.OnAppStatusChangedListener` 的同名方法。
     *
     * #### 基于此类的活动重写此方法默认不会生效，除非注册了 `LifecycleEventObserver` 监听器（一般在 `onCreate` 方法中注册），不需要手动移除监听器，活动销毁时会自动移除监听器。
     * ```java
     * // java
     * getLifecycle().addObserver(this);
     * ```
     * ```kotlin
     * // kotlin
     * lifecycle.addObserver(this)
     * ```
     */
    // 默认实现，子类可以选择重写
    open fun onForeground(activity: Activity) {}

    /**
     * ## 当活动进入后台时调用的方法，活动销毁时不会调用
     *
     * > 销毁判断通过简单的延时，因此不保证准确性，特别是活动生命周期复杂的情况下。
     *
     * 代替 `com.blankj.utilcode.util.Utils.OnAppStatusChangedListener` 的同名方法。
     *
     * #### 基于此类的活动重写此方法默认不会生效，除非注册了 `LifecycleEventObserver` 监听器（一般在 `onCreate` 方法中注册），不需要手动移除监听器，活动销毁时会自动移除监听器。
     * ```java
     * // java
     * getLifecycle().addObserver(this);
     * ```
     * ```kotlin
     * // kotlin
     * lifecycle.addObserver(this)
     * ```
     */
    // 默认实现，子类可以选择重写
    open fun onBackground(activity: Activity) {}

    private var mIsFirstResume = true
    private var mIsForeground = false


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d("MatrixModel", "onStateChanged: $event")
        when (event) {
            Lifecycle.Event.ON_RESUME -> _onResume()
            Lifecycle.Event.ON_STOP -> _onStop()
            Lifecycle.Event.ON_DESTROY -> {
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    private fun _onResume() {
        Log.d("MatrixModel", "onResume")
        if (mIsFirstResume) {
            mIsFirstResume = false
            return
        }
        mIsForeground = true
        onForeground(this)
    }

    private fun _onStop() {
        Log.d("MatrixModel", "onStop")
        mIsForeground = false
        Handler(Looper.getMainLooper()).postDelayed({
            if (!this.isDestroyed && !mIsForeground) {
                onBackground(this)
            }
        }, 100)
    }


    /**
     * 基于此类的活动可选是否重写
     * TODO
     */
    open fun undefined(): String = ""
}

