package sc.hwd.sillot.shared2.expect

import sc.hwd.sillot.shared2.interfaces.ServiceInfo


expect object Mdns {
    fun getDeviceName(): String
    suspend fun discover(type: String): List<ServiceInfo>
    fun register(type: String, name: String, port: Int)
    fun unregister()
    fun unregisterAllServices()
}