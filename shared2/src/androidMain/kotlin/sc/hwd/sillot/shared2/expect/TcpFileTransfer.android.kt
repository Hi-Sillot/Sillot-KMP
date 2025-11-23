package sc.hwd.sillot.shared2.expect

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toUri
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import sc.hwd.sillot.shared2.actual.AndroidContextProvider

actual object TcpFileTransfer {
    private const val BUFFER_SIZE = 8192

    @Serializable
    private data class FileHead(val name: String, val size: Long, val mime: String = "bin")

    // ✅ 缓存 ContentResolver 和基础 URI
    private var contentResolver: ContentResolver? = null
    private var baseUri: Uri? = null


// ✅ 接收字符串并转换为 Uri
    actual fun setSaveDirectory(dir: String) {
        if (dir.isBlank()) return
        val context = AndroidContextProvider.getContext()  // 获取 Context
        contentResolver = context.contentResolver
        baseUri = dir.toUri()
    // ✅ 移除 takePersistableUriPermission，在 MainActivity 中获取
    }

    actual suspend fun saveFile(name: String, data: ByteArray): PlatformFile? =
        withContext(Dispatchers.IO) {
            val resolver = contentResolver ?: return@withContext null
            try {
                val docUri = DocumentsContract.buildDocumentUriUsingTree(baseUri!!,
                    DocumentsContract.getTreeDocumentId(baseUri!!))
                val newFileUri = DocumentsContract.createDocument(resolver, docUri,
                    "application/octet-stream", name) ?: return@withContext null

                resolver.openOutputStream(newFileUri)?.use { output ->
                    output.write(data)
                }
                val context = AndroidContextProvider.getContext()  // 获取 Context
                // ✅ 通知系统媒体库更新
                notifyMediaStoreUpdate(context, newFileUri, name, data.size.toLong())
                PlatformFile(newFileUri.toString())
            } catch (e: Exception) {
                println("❌ 保存文件失败: ${e.message}")
                null
            }
        }

    // ✅ 通知媒体库更新
    private fun notifyMediaStoreUpdate(context: Context, fileUri: Uri, fileName: String, fileSize: Long) {
        try {
            // 方法1: 发送广播通知媒体扫描
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri)
            context.sendBroadcast(mediaScanIntent)

            // 方法2: 使用 MediaScannerConnection 扫描文件（更可靠）
            val mimeType = getMimeTypeFromFileName(fileName)
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(fileUri.toString()),
                arrayOf(mimeType)
            ) { path, uri ->
                println("✅ 媒体库扫描完成: $fileName -> $uri")
            }

            // 方法3: 对于特定类型的文件，使用更具体的更新方式
            when {
                fileName.endsWith(".jpg", ignoreCase = true) ||
                        fileName.endsWith(".jpeg", ignoreCase = true) ||
                        fileName.endsWith(".png", ignoreCase = true) ||
                        fileName.endsWith(".gif", ignoreCase = true) -> {
                    // 图片文件：更新图库
                    updateMediaStoreForImage(context, fileUri, fileName, fileSize)
                }
                fileName.endsWith(".mp4", ignoreCase = true) ||
                        fileName.endsWith(".avi", ignoreCase = true) ||
                        fileName.endsWith(".mkv", ignoreCase = true) -> {
                    // 视频文件：更新视频库
                    updateMediaStoreForVideo(context, fileUri, fileName, fileSize)
                }
                fileName.endsWith(".mp3", ignoreCase = true) ||
                        fileName.endsWith(".wav", ignoreCase = true) ||
                        fileName.endsWith(".flac", ignoreCase = true) -> {
                    // 音频文件：更新音乐库
                    updateMediaStoreForAudio(context, fileUri, fileName, fileSize)
                }
            }

            println("✅ 已通知系统更新媒体库: $fileName")

        } catch (e: Exception) {
            println("⚠️ 媒体库更新通知失败: ${e.message}")
        }
    }

    // ✅ 根据文件名获取 MIME 类型
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
            else -> "application/octet-stream"
        }
    }

    // ✅ 专门处理图片文件的媒体库更新
    private fun updateMediaStoreForImage(context: android.content.Context, uri: Uri, fileName: String, size: Long) {
        try {
            val values = android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.SIZE, size)
                put(MediaStore.Images.Media.MIME_TYPE, getMimeTypeFromFileName(fileName))
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            }

            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let {
                println("✅ 图片已添加到媒体库: $fileName")
            }
        } catch (e: Exception) {
            println("⚠️ 图片媒体库更新失败: ${e.message}")
        }
    }

    // ✅ 专门处理视频文件的媒体库更新
    private fun updateMediaStoreForVideo(context: android.content.Context, uri: Uri, fileName: String, size: Long) {
        try {
            val values = android.content.ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.SIZE, size)
                put(MediaStore.Video.Media.MIME_TYPE, getMimeTypeFromFileName(fileName))
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            }

            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            println("⚠️ 视频媒体库更新失败: ${e.message}")
        }
    }

    // ✅ 专门处理音频文件的媒体库更新
    private fun updateMediaStoreForAudio(context: android.content.Context, uri: Uri, fileName: String, size: Long) {
        try {
            val values = android.content.ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.SIZE, size)
                put(MediaStore.Audio.Media.MIME_TYPE, getMimeTypeFromFileName(fileName))
                put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            }

            context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            println("⚠️ 音频媒体库更新失败: ${e.message}")
        }
    }

}