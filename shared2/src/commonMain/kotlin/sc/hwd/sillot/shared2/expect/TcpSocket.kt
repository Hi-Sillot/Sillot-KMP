package sc.hwd.sillot.shared2.expect

expect object TcpSocket {
    // 启动后台服务器，收到字符串时回调
    suspend fun listen(port: Int, onMessage: (String) -> Unit)
    // 发送单行字符串（带换行符）
    suspend fun send(host: String, port: Int, text: String)
}