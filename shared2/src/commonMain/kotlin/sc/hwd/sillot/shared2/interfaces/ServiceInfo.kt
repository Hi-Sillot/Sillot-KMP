package sc.hwd.sillot.shared2.interfaces

data class ServiceInfo(
    val name: String,
    val host: String,
    val port: Int,
    val filePort: Int? = null  // ✅ 文件端口
)