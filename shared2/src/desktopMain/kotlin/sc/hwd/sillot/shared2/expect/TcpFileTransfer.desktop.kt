package sc.hwd.sillot.shared2.expect

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream

actual object TcpFileTransfer {
    private const val BUFFER_SIZE = 8192

    @Serializable
    private data class FileHead(val name: String, val size: Long, val mime: String = "bin")

    // Windows 端直接使用文件路径
    private var saveDirectory: File? = null

    // ✅ 设置保存目录
    actual fun setSaveDirectory(dir: String) {
        if (dir.isBlank()) return

        val directory = File(dir)
        if (!directory.exists()) {
            directory.mkdirs() // 创建目录（包括父目录）
        }

        if (directory.exists() && directory.isDirectory) {
            saveDirectory = directory
            println("✅ Windows 保存目录设置为: ${directory.absolutePath}")
        } else {
            println("❌ 无法设置保存目录: $dir")
        }
    }

    actual suspend fun saveFile(name: String, data: ByteArray): PlatformFile? =
        withContext(Dispatchers.IO) {
            val directory = saveDirectory ?: return@withContext null

            try {
                // 确保文件名安全（移除非法字符）
                val safeFileName = sanitizeFileName(name)
                val outputFile = File(directory, safeFileName)

                // 如果文件已存在，添加序号避免覆盖
                val finalFile = getUniqueFile(outputFile)

                // 写入文件
                FileOutputStream(finalFile).use { output ->
                    output.write(data)
                }

                println("✅ 文件保存成功: ${finalFile.absolutePath} (${data.size} 字节)")

                // Windows 端返回文件路径
                PlatformFile(finalFile.absolutePath)
            } catch (e: Exception) {
                println("❌ Windows 保存文件失败: ${e.message}")
                e.printStackTrace()
                null
            }
        }

    // ✅ 清理文件名中的非法字符
    private fun sanitizeFileName(fileName: String): String {
        // Windows 文件名中不允许的字符
        val illegalChars = arrayOf("<", ">", ":", "\"", "/", "\\", "|", "?", "*")
        var safeName = fileName
        illegalChars.forEach { char ->
            safeName = safeName.replace(char, "_")
        }
        return safeName
    }

    // ✅ 获取唯一的文件名（避免覆盖）
    private fun getUniqueFile(file: File): File {
        if (!file.exists()) {
            return file
        }

        val nameWithoutExt = file.nameWithoutExtension
        val extension = file.extension
        var counter = 1

        while (true) {
            val newName = if (extension.isBlank()) {
                "$nameWithoutExt($counter)"
            } else {
                "$nameWithoutExt($counter).$extension"
            }

            val newFile = File(file.parent, newName)
            if (!newFile.exists()) {
                return newFile
            }
            counter++
        }
    }

    // Windows 端不需要媒体库相关方法，但为了接口一致性可以留空或简单实现

    // ✅ 获取文件 MIME 类型（可选实现）
    private fun getMimeTypeFromFileName(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", ignoreCase = true) ||
                    fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
            fileName.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
            fileName.endsWith(".avi", ignoreCase = true) -> "video/x-msvideo"
            fileName.endsWith(".mkv", ignoreCase = true) -> "video/x-matroska"
            fileName.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
            fileName.endsWith(".wav", ignoreCase = true) -> "audio/wav"
            fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
            fileName.endsWith(".doc", ignoreCase = true) -> "application/msword"
            fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            fileName.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
            fileName.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            fileName.endsWith(".zip", ignoreCase = true) -> "application/zip"
            else -> "application/octet-stream"
        }
    }
}