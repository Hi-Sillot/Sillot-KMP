package sc.hwd.sofill.android.lifecycle.services

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 服务等待管理器
 */
object ServiceWaiterManager {

    private var isInitialized = AtomicBoolean(false)
    private lateinit var application: Application
    private val serviceWaiters = mutableMapOf<String, ServiceWaiter>()

    /**
     * 初始化方法，由 ContentProvider 调用
     */
    @Synchronized
    fun init(context: Context) {
        if (isInitialized.getAndSet(true)) {
            return
        }

        application = context.applicationContext as Application
        // 可以在这里进行其他初始化工作
    }

    /**
     * 注册服务等待器
     */
    fun registerServiceWaiter(key: String, serviceProvider: () -> Any?): ServiceWaiter {
        val waiter = ServiceWaiter(serviceProvider, application)
        serviceWaiters[key] = waiter
        return waiter
    }

    /**
     * 获取服务等待器
     */
    fun getServiceWaiter(key: String): ServiceWaiter {
        return serviceWaiters[key] ?: throw IllegalArgumentException("ServiceWaiter with key '$key' not found")
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return isInitialized.get()
    }
}

/**
 * 服务等待器类
 */
class ServiceWaiter(
    private val serviceProvider: () -> Any?,
    private val application: Application
) {

    /**
     * 等待服务就绪（简化版）
     */
    fun waitForService(
        onReady: () -> Unit,
        onError: (Exception) -> Unit = { it.printStackTrace() }
    ) {
        val config = ServiceWaitConfig.Builder()
            .setServiceProvider(serviceProvider)
            .build()
        waitForServiceWithConfig(config, onReady, onError)
    }

    /**
     * 等待服务就绪（带配置）
     */
    fun waitForServiceWithConfig(
        config: ServiceWaitConfig,
        onReady: () -> Unit,
        onError: (Exception) -> Unit = { it.printStackTrace() }
    ) {
        // 这里需要确保在 Activity 上下文中调用
        // 实际使用时，调用方需要确保在 Activity 中调用此方法
        executeWait(config, onReady, onError)
    }

    private fun executeWait(
        config: ServiceWaitConfig,
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // 由于我们无法直接获取当前 Activity，这里需要调用方确保在 Activity 中调用
        // 在实际项目中，可以通过 Application 注册 Activity 生命周期回调来获取当前 Activity
        GlobalScope.launch(Dispatchers.Main) {
            try {
                waitForWithTimeout(
                    objProvider = config.serviceProvider,
                    checkInterval = config.checkInterval,
                    timeout = config.timeout
                )
                onReady()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private suspend fun <T> waitForWithTimeout(
        objProvider: () -> T?,
        checkInterval: Long,
        timeout: Long
    ): T {
        return withContext(Dispatchers.Main) {
            suspendCoroutine<T> { continuation ->
                val handler = Handler(Looper.getMainLooper())
                var elapsedTime = 0L
                val startTime = System.currentTimeMillis()

                val checkRunnable = object : Runnable {
                    override fun run() {
                        val obj = objProvider()
                        if (obj != null) {
                            continuation.resume(obj)
                        } else {
                            elapsedTime = System.currentTimeMillis() - startTime
                            if (elapsedTime >= timeout) {
                                continuation.resumeWithException(
                                    TimeoutException("Wait for service timeout after $timeout ms")
                                )
                            } else {
                                handler.postDelayed(this, checkInterval)
                            }
                        }
                    }
                }
                handler.post(checkRunnable)

                continuation.context[Job]?.invokeOnCompletion {
                    handler.removeCallbacks(checkRunnable)
                }
            }
        }
    }
}

/**
 * 服务等待配置
 */
data class ServiceWaitConfig(
    val serviceProvider: () -> Any?,
    val checkInterval: Long = 100L,
    val timeout: Long = 30000L
) {
    class Builder {
        private var serviceProvider: (() -> Any?)? = null
        private var checkInterval: Long = 100L
        private var timeout: Long = 30000L

        fun setServiceProvider(provider: () -> Any?) = apply {
            this.serviceProvider = provider
        }

        fun setCheckInterval(interval: Long) = apply {
            this.checkInterval = interval
        }

        fun setTimeout(timeout: Long) = apply {
            this.timeout = timeout
        }

        fun build(): ServiceWaitConfig {
            return ServiceWaitConfig(
                serviceProvider = serviceProvider ?: throw IllegalArgumentException("Service provider must be set"),
                checkInterval = checkInterval,
                timeout = timeout
            )
        }
    }
}