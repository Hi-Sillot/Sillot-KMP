package sc.hwd.sillot.shared2.expect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket

actual object TcpSocket {
    actual suspend fun listen(port: Int, onMessage: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            ServerSocket(port).use { server ->
                while (true) {
                    val socket = server.accept()
                    socket.use {
                        it.getInputStream().bufferedReader().lineSequence().forEach { line ->
                            onMessage(line)
                        }
                    }
                }
            }
        }
    }

    actual suspend fun send(host: String, port: Int, text: String) {
        withContext(Dispatchers.IO) {
            Socket(host, port).use { socket ->
                socket.getOutputStream().writer().apply {
                    write(text + "\n")
                    flush()
                }
            }
        }
    }
}