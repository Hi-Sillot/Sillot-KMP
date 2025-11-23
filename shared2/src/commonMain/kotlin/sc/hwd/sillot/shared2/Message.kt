package sc.hwd.sillot.shared2

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class Message(
    val type: String,  // "text", "file_chunk", "file_complete"
    val payload: String,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val chunkIndex: Int? = null,
    val totalChunks: Int? = null,
    val fileMime: String? = null
) {
    companion object {
        const val CHUNK_SIZE = 1024 * 512  // ✅ 每片 512KB（安全大小）

        fun text(content: String) = Message(type = "text", payload = content)

        // ✅ 生成文件分片
        fun fileChunks(name: String, mime: String, data: ByteArray): List<Message> {
            val total = (data.size + CHUNK_SIZE - 1) / CHUNK_SIZE
            return List(total) { index ->
                val start = index * CHUNK_SIZE
                val end = minOf(start + CHUNK_SIZE, data.size)
                val chunkData = data.sliceArray(start until end)
                Message(
                    type = "file_chunk",
                    payload = encodeToBase64(chunkData),
                    fileName = name,
                    fileSize = data.size.toLong(),
                    chunkIndex = index,
                    totalChunks = total,
                    fileMime = mime
                )
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun encodeToBase64(bytes: ByteArray): String = Base64.encode(bytes)

        @OptIn(ExperimentalEncodingApi::class)
        fun decodeFromBase64(str: String): ByteArray = Base64.decode(str)

        // ✅ JSON 序列化
        fun serialize(msg: Message): String = Json.encodeToString(msg)

        // ✅ JSON 反序列化
        fun deserialize(json: String): Message = Json.decodeFromString(json)

    }
}