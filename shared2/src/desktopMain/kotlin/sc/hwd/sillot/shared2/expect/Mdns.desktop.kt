package sc.hwd.sillot.shared2.expect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sc.hwd.sillot.shared2.interfaces.ServiceInfo
import java.net.InetAddress
import java.net.NetworkInterface
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo as JmDnsServiceInfo

actual object Mdns {
    private var jmdns: JmDNS? = null
    private var serviceInfo: JmDnsServiceInfo? = null  // ③ 这里用别名

    // ✅ 新增：获取真实局域网 IP
    private fun getLocalIp(): InetAddress {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .first { addr ->
                    val host = addr.hostAddress
                    host?.matches(Regex("^(192\\.168|10\\.|172\\.)\\..*")) ?: false
                }
        } catch (e: Exception) {
            // fallback to localhost
            InetAddress.getLocalHost()
        }
    }

    actual fun getDeviceName(): String = "Device"  // TODO： 获取电脑名称

    actual suspend fun discover(type: String): List<sc.hwd.sillot.shared2.interfaces.ServiceInfo> = withContext(Dispatchers.IO) {
//        val j = JmDNS.create(InetAddress.getLocalHost())
        val j = JmDNS.create(getLocalIp())  // ✅ 关键：绑定到局域网 IP
        val services = j.list(type, 1_000)
        j.close()
        services.map { jmDnsInfo ->  // ④ 明确变量名
            ServiceInfo(
                name = jmDnsInfo.name,
                host = jmDnsInfo.inetAddresses.firstOrNull()?.hostAddress ?: "",
                port = jmDnsInfo.port
            )
        }
    }

    actual fun register(type: String, name: String, port: Int) {
//        val j = JmDNS.create(InetAddress.getLocalHost())
        val j = JmDNS.create(getLocalIp())  // ✅ 关键：绑定到局域网 IP
        jmdns = j
        serviceInfo = JmDnsServiceInfo.create(type, name, port, 0, 0, "path=index")
        j.registerService(serviceInfo)
    }

    actual fun unregister() {
        serviceInfo?.let {
            jmdns?.unregisterService(it)
            println("Unregister service: $it")
        }
        jmdns?.close()
    }

    actual fun unregisterAllServices() {
        println("Unregister all services")
        jmdns?.unregisterAllServices();
        jmdns?.close()
    }
}