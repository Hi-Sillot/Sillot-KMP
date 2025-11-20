package sc.hwd.sillot.shared2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sc.hwd.sillot.shared2.expect.Mdns
import sc.hwd.sillot.shared2.expect.TcpSocket
import sc.hwd.sillot.shared2.interfaces.ServiceInfo
import kotlin.random.Random
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.withContext

class DiscoveryViewModel : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val discovered = MutableStateFlow<List<ServiceInfo>>(emptyList())
    val inputText = MutableStateFlow("")

    private val myPort = Random.nextInt(10_000, 20_000)
    private val serviceType = "_sync._tcp.local." // Android 和 Windows 必须一字不差

    init {
        scope.launch {
            println("🚀 [KMP] 启动发现 - 端口: $myPort, 服务类型: $serviceType")
            // 1. 启动 TCP 服务器（IO 线程）
            launch {
                TcpSocket.listen(myPort) { remoteText ->
                    println("📨 [KMP] 收到文本: $remoteText")
                    inputText.value = remoteText
                }
            }

            // 2. 注册 mDNS 服务（IO 线程）
            withContext(Dispatchers.IO) {
                Mdns.register(serviceType, "KMP-${Mdns.getDeviceName()}", myPort)
                println("✅ [KMP] 已注册服务: KMP-${Mdns.getDeviceName()}:$myPort")
            }

            // 3. 定期发现服务（IO 线程）
            while (true) {
                try {
                    val list = withContext(Dispatchers.IO) {
                        Mdns.discover(serviceType)
                    }
                    println("🔍 [KMP] 发现 ${list.size} 个服务: $list")
                    discovered.value = list.filter { it.port != myPort }
                } catch (e: Exception) {
                    println("❌ [KMP] 发现失败: ${e.message}")
                    e.printStackTrace()
                }
                delay(3_000)
            }
        }

        // 4. 监听输入变化并发送
        scope.launch {
            inputText.drop(1).collectLatest { text ->
                println("📝 [KMP] 发送给 ${discovered.value.size} 个设备: $text")
                discovered.value.forEach { peer ->
                    try {
                        TcpSocket.send(peer.host, peer.port, text)
                    } catch (e: Exception) {
                        // 忽略离线设备
                        println("⚠️ [KMP] 发送失败 ${peer.host}:${peer.port}")
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.launch {
            withContext(Dispatchers.IO) { Mdns.unregister() }
        }
        scope.cancel()
    }
}