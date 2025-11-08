/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午11:29
 * updated: 2024/9/2 上午11:29
 */

package sc.hwd.sillot.gibbet.services

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import sc.hwd.sofill.Ss.S_Intent
import sc.hwd.sofill.interfaces.gibbet.IGibbetKernelService
import sc.hwd.sofill.interfaces.gibbet.IGibbetKernelServiceAction
import sc.hwd.sofill.interfaces.gibbet.IGibbetKernelServiceManager
import kotlin.coroutines.resumeWithException

// 服务状态枚举
enum class GibbetServiceState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

class GibbetKernelServiceManager(private val context: Context) : IGibbetKernelServiceManager {
    private val TAG = "GibbetKernelServiceManager"
    private var s: IGibbetKernelService? = null
    private var isServiceBound = false
    private var action: IGibbetKernelServiceAction? = null

    // 服务状态管理
    private var serviceState = GibbetServiceState.DISCONNECTED
    private val serviceStateListeners = mutableListOf<(GibbetServiceState) -> Unit>()

    // 等待服务连接的挂起函数支持
    private var serviceConnectionContinuation: (() -> Unit)? = null
    private var serviceErrorContinuation: ((Exception) -> Unit)? = null

    override val service: IGibbetKernelService?
        get() = s

    // 获取当前服务状态
    fun getServiceState(): GibbetServiceState = serviceState

    // 注册服务状态监听器
    fun addServiceStateListener(listener: (GibbetServiceState) -> Unit) {
        serviceStateListeners.add(listener)
    }

    fun removeServiceStateListener(listener: (GibbetServiceState) -> Unit) {
        serviceStateListeners.remove(listener)
    }

    private fun updateServiceState(newState: GibbetServiceState) {
        if (serviceState != newState) {
            serviceState = newState
            serviceStateListeners.forEach { it(newState) }
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected() invoked -> $className")
            val binder = service as IGibbetKernelService.LocalBinder
            s = binder.getService()
            isServiceBound = true

            s?.showNotification()

            // 服务绑定后，执行依赖于bootService的代码
            action?.performActionWithService()

            // 通知等待的协程
            serviceConnectionContinuation?.invoke()
            serviceConnectionContinuation = null
        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            Log.d(TAG, "onNullBinding() invoked -> $name")
            updateServiceState(GibbetServiceState.ERROR)
            notifyServiceError(Exception("Service binding returned null"))
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            Log.d(TAG, "onBindingDied() invoked -> $name")
            updateServiceState(GibbetServiceState.ERROR)
            notifyServiceError(Exception("Service binding died"))
        }

        /**
         * unbindService 不会触发
         */
        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "onServiceDisconnected() invoked")
            updateServiceState(GibbetServiceState.DISCONNECTED)
            releaseService()
        }
    }

    private fun notifyServiceError(exception: Exception) {
        serviceErrorContinuation?.invoke(exception)
        serviceErrorContinuation = null
    }

    override fun bindService(action: IGibbetKernelServiceAction, webViewKey: String, activity: Activity) {
        Log.d(TAG, "bindService() invoked")
        if (isServiceBound) {
            s?.stopKernelOnDestroy = false // 重复绑定时，防止杀死内核
            releaseService()
        }
        this.action = action
        updateServiceState(GibbetServiceState.CONNECTING)


        val pkgName = context.applicationContext.packageName
        val serviceName = "sc.hwd.sillot.gibbet.services.GibbetKernelService"

        Log.d(
            TAG,
            "bindService() -> ${ComponentName(pkgName, serviceName)}"
        )


        val intent = Intent()
        intent.component = ComponentName(pkgName, serviceName)
        intent.putExtra(S_Intent.EXTRA_WEB_VIEW_KEY, webViewKey)
        val bindResult = context.bindService(intent, serviceConnection,
            Context.BIND_AUTO_CREATE
                    //                or Context.BIND_EXTERNAL_SERVICE
                    or Context.BIND_IMPORTANT)

        Log.d(TAG, "bindService() result -> $bindResult")

        if (!bindResult) {
            updateServiceState(GibbetServiceState.ERROR)
            notifyServiceError(Exception("Failed to bind service"))
        }

    }

    override fun stopServiceByKillKernel() {
        Log.d(TAG, "stopServiceByKillKernel() invoked")
        s?.stopKernelOnDestroy = true
        releaseService()
    }

    private fun releaseService() {
        Log.d(TAG, "releaseBootService() invoked")
        if (isServiceBound) {
//            bootService?.let {
//                it.kernelStarted = false
//                it.stopSelf()
//                bootService = null
//            }
            context.unbindService(serviceConnection)
            isServiceBound = false
            s = null
            action = null
            updateServiceState(GibbetServiceState.DISCONNECTED)
        }
    }

    /**
     * 等待服务连接（供 ServiceWaiterManager 使用）
     */
    suspend fun waitForServiceConnection(): Boolean {
        return if (serviceState == GibbetServiceState.CONNECTED) {
            true
        } else {
            try {
                // 使用挂起函数等待服务连接
                suspendCancellableCoroutine { continuation ->
                    serviceConnectionContinuation = {
                        continuation.resume(true, null)
                    }
                    serviceErrorContinuation = { exception ->
                        continuation.resumeWithException(exception)
                    }

                    // 设置超时
                    continuation.invokeOnCancellation {
                        serviceConnectionContinuation = null
                        serviceErrorContinuation = null
                    }
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * 检查服务是否可用
     */
    fun isServiceAvailable(): Boolean {
        return serviceState == GibbetServiceState.CONNECTED && s != null
    }

    private val serviceReadyCallbacks = mutableListOf<(IGibbetKernelService) -> Unit>()
    private val serviceErrorCallbacks = mutableListOf<(Exception) -> Unit>()

    /**
     * 添加服务就绪回调
     */
    fun onServiceReady(callback: (IGibbetKernelService) -> Unit) {
        if (isServiceBound && service != null) {
            callback(service!!)
        } else {
            serviceReadyCallbacks.add(callback)
        }
    }

    /**
     * 添加服务错误回调
     */
    fun onServiceError(callback: (Exception) -> Unit) {
        serviceErrorCallbacks.add(callback)
    }

}
