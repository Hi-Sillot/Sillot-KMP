package sc.hwd.sillot.shared2.expect

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType

expect object TcpFileTransfer {
    /**
     * 保存目录字符串（Android: content:// URI, Windows: 普通路径）
     */
    fun setSaveDirectory(dir: String)

    // ✅ 保存文件（由平台实现）
    suspend fun saveFile(name: String, data: ByteArray): PlatformFile?
}