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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.withContext
import sc.hwd.sillot.shared2.expect.TcpFileTransfer
import java.io.File

class DiscoveryViewModel : ViewModel() {
    private val TAG = "DiscoveryViewModel"
    val discovered = MutableStateFlow<List<ServiceInfo>>(emptyList())
    val inputText = MutableStateFlow("")  // 纯文本消息
    val receivedFiles = MutableStateFlow<List<String>>(emptyList())  // ✅ 只存路径

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var fileReceiverJob: Job? = null  // ✅ 显式持有 Job

    private val myPort = Random.nextInt(10_000, 20_000)

    private val serviceType = "_sync._tcp.local." // Android 和 Windows

    // ✅ 保存目录（为空时提示用户选择）
    val saveDir = MutableStateFlow<String?>(null)

    // ✅ 文件分片重组缓存
    private val fileChunks = mutableMapOf<String, MutableList<ByteArray>>()

    fun setSaveDirectory(dir: String) {
        saveDir.value = dir
        TcpFileTransfer.setSaveDirectory(dir)  // ✅ 传递字符串
        println("📁 用户设置保存目录: $dir")
    }

    init {
        scope.launch {
            println("🚀 [KMP] 启动发现 - 端口: $myPort, 服务类型: $serviceType")
            // ✅ 启动统一消息服务器
            launch {
                TcpSocket.listen(myPort) { rawData ->
                    scope.launch {
                        handleIncomingMessage(rawData)
                    }
                }
            }

            // 注册 mDNS 服务（IO 线程）
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) { Mdns.unregister() }
                Mdns.register(serviceType, Mdns.getDeviceName(), myPort)
                println("✅ [KMP] 已注册服务: ${Mdns.getDeviceName()}:$myPort")
            }


            // 3. 定期发现服务（IO 线程）
            while (true) {
                try {
                    val list = withContext(Dispatchers.IO) {
                        Mdns.discover(serviceType)
                    }
                    // ✅ 获取本机 IP 地址
                    val myHost = getMyHostAddress()
                    discovered.value = list.filter { it.host != myHost} // 过滤掉自己
                    println("🔍 [KMP] 当前: $myHost:$myPort, 发现 ${discovered.value.size} 个服务: ${discovered.value}")
                } catch (e: Exception) {
                    println("❌ [KMP] 发现失败: ${e.message}")
                    e.printStackTrace()
                }
                delay(2_000) // 根据 mDNS 规范，取消注册的服务不会立即从服务列表中消失，而是会发送 "goodbye" 包，并在大约 1 秒后从缓存中清除
            }
        }

        // 监听纯文本输入
        scope.launch {
            inputText.drop(1).collectLatest { text ->
                broadcastMessage(Message.text(text))
            }
        }


    }

    override fun onCleared() {
        super.onCleared()
        println("🚪 [KMP] 关闭发现")
        scope.launch {
            withContext(Dispatchers.IO) { Mdns.unregister() }
        }
        scope.cancel()
    }

    /**
     * compose 重组需要调用此函数注销之前的注册
     */
    fun unregister() {
        this.onCleared()
    }


    // ✅ 检查是否有保存目录
    fun hasSaveDirectory(): Boolean = saveDir.value != null

    // ✅ 获取本机 IP 地址（跨平台）
    private fun getMyHostAddress(): String {
        return try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress("8.8.8.8", 80), 1000)
                socket.localAddress.hostAddress
            }
        } catch (e: Exception) {
            try {
                // 回退方法：遍历网络接口
                java.net.NetworkInterface.getNetworkInterfaces().toList()
                    .filter { it.isUp && !it.isLoopback }
                    .flatMap { it.inetAddresses.toList() }
                    .filterIsInstance<java.net.Inet4Address>()
                    .firstOrNull { it.isSiteLocalAddress }
                    ?.hostAddress
                    ?: java.net.InetAddress.getLocalHost().hostAddress
                    ?: "127.0.0.1"
            } catch (ex: Exception) {
                "127.0.0.1"
            }
        }
    }

    // ✅ 统一消息分发
    private suspend fun handleIncomingMessage(rawData: String) {
        try {
            val message = Message.deserialize(rawData)
            println("🔍 解析消息类型: ${message.type}")

            when (message.type) {
                "text" -> {
                    inputText.value = message.payload
                    println("📨 收到文本: ${message.payload}")
                }
                "file_chunk" -> {
                    // ✅ 关键：在内部捕获异常，不影响其他分支
                    try {
                        handleFileChunk(message)
                    } catch (e: Exception) {
                        println("❌ 处理文件分片失败: ${e.message}")
                        e.printStackTrace()
                    }
                }
                else -> {
                    // ✅ 未知类型，视为纯文本
                    inputText.value = rawData
                    println("⚠️ 未知消息类型: ${message.type}")
                }
            }
        } catch (e: Exception) {
            // JSON 解析失败，视为旧版纯文本
            inputText.value = rawData
            println("📨 收到旧版文本: $rawData")
        }
    }

    // ✅ 处理文件分片
    private suspend fun handleFileChunk(message: Message) {
        println("📥 开始处理文件分片: index=${message.chunkIndex}, total=${message.totalChunks}")

        message.chunkIndex?.let { index ->
            message.totalChunks?.let { total ->
                val key = "${message.fileName}_$total"
                val chunks = fileChunks.getOrPut(key) { mutableListOf() }

                // ✅ 检查分片完整性
                if (index >= total) {
                    println("❌ 非法分片索引: $index >= $total")
                    return
                }

                // ✅ 解码分片数据
                val chunkData = try {
                    Message.decodeFromBase64(message.payload)
                } catch (e: Exception) {
                    println("❌ 解码 Base64 失败: ${e.message}")
                    return
                }

                // ✅ 添加分片
                if (index < chunks.size) {
                    chunks[index] = chunkData  // 替换已存在的
                } else if (index == chunks.size) {
                    chunks.add(chunkData)      // 添加新分片
                } else {
                    println("❌ 分片索引不连续: $index, 当前大小: ${chunks.size}")
                    return
                }

                println("📥 收到分片 ${index + 1}/$total (大小: ${chunkData.size} bytes)")

                // ✅ 检查是否收齐
                if (chunks.size == total) {
                    saveCompleteFile(message.fileName!!, chunks, message.fileSize!!)
                    fileChunks.remove(key)
                }
            } ?: run {
                println("❌ 消息缺少 totalChunks")
            }
        } ?: run {
            println("❌ 消息缺少 chunkIndex")
        }
    }

    // ✅ 重组并保存文件
    private suspend fun saveCompleteFile(
        name: String,
        chunks: List<ByteArray>,
        expectedSize: Long
    ) {
        val data = chunks.fold(byteArrayOf()) { acc, chunk -> acc + chunk }

        if (data.size.toLong() != expectedSize) {
            println("❌ 文件大小不匹配: 预期 $expectedSize, 实际 ${data.size}")
            return
        } else {
            println("✅ 文件大小匹配: $expectedSize , 开始重组并保存文件")
        }
        // ✅ 调用平台实现
        val savedFile = TcpFileTransfer.saveFile(name, data)
        savedFile?.let {
            receivedFiles.value += it.absolutePath()  // ✅ 添加路径
            println("✅ 文件接收完成: ${it.name}")
        }

    }


    // ✅ 广播消息给所有在线设备
    private suspend fun broadcastMessage(msg: Message) {
        val json = Message.serialize(msg)
        discovered.value.forEach { peer ->
            try {
                TcpSocket.send(peer.host, peer.port, json)
            } catch (e: Exception) {
                println("⚠️ 发送失败 ${peer.name}: ${e.message}")
            }
        }
    }

    // ✅ 发送文件（核心函数）
    fun sendFileToAll(file: PlatformFile) {
        scope.launch {
            println("📤 正在发送文件: ${file.name}")
            val data = file.readBytes()
            val chunks = Message.fileChunks(file.name, "application/octet-stream", data)

            chunks.forEachIndexed { index, chunk ->
                broadcastMessage(chunk)
                println("📤 发送分片 ${index + 1}/${chunks.size}")
                delay(20)  // ✅ 每片间隔 20ms，避免网络拥堵
            }
        }
    }


}