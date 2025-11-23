package sc.hwd.sillot.shared2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sc.hwd.sillot.shared2.expect.TcpFileTransfer
import sc.hwd.sillot.shared2.theme.WindowsThemeManager
import java.awt.EventQueue
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileSystemView

@Composable
fun MainWindow(onCloseRequest: () -> Unit) {
    setupConsoleEncoding()
    val state = rememberWindowState(
        size = DpSize(1200.dp, 800.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )

    // 用于触发目录选择
    var triggerDirectorySelection by remember { mutableStateOf(false) }

    // 当需要选择目录时触发
    LaunchedEffect(triggerDirectorySelection) {
        if (triggerDirectorySelection) {
            triggerDirectorySelection = false
            val selectedDir = selectDirectoryModern()
            selectedDir?.let { dir ->
                // 设置保存目录
                TcpFileTransfer.setSaveDirectory(dir)
                // 这里可以通知 ViewModel 或其他组件
                println("✅ Windows 保存目录设置为: $dir")
            }
        }
    }

    Window(
        state = state,
        resizable = true,
        title = "汐洛",
        onCloseRequest = onCloseRequest
    ) {
        var isDarkTheme by remember { mutableStateOf(WindowsThemeManager.isWindowsDarkTheme()) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                WindowsThemeManager.listenWindowsThemeChanges { newSystemThemeIsDark ->
                    if (isDarkTheme != newSystemThemeIsDark) isDarkTheme = newSystemThemeIsDark
                }
            }
        }

        LaunchedEffect(isDarkTheme, window) {
            SwingUtilities.invokeLater {
                WindowsThemeManager.setWindowsTitleBarTheme(window, isDarkTheme)
            }
        }

        App(
             "Sillot Desktop",
            onSelectDirectory = {
                // 触发目录选择
                triggerDirectorySelection = true
            }
        )
    }
}


// 更好的目录选择实现（使用系统目录选择器）
private fun selectDirectoryImproved(): String? {
    return try {
        // 使用 Swing 的 JFileChooser 专门选择目录
        val fileChooser = javax.swing.JFileChooser()
        fileChooser.fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "选择文件保存目录"

        val result = fileChooser.showOpenDialog(null)
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else {
            null
        }
    } catch (e: Exception) {
        println("❌ 目录选择失败: ${e.message}")
        null
    }
}

// 现代化的目录选择器
private fun selectDirectoryModern(): String? {
    return try {
        var selectedPath: String? = null

        // 在 EDT（事件分发线程）中执行 Swing 操作
        if (EventQueue.isDispatchThread()) {
            selectedPath = showDirectoryChooser()
        } else {
            EventQueue.invokeAndWait {
                selectedPath = showDirectoryChooser()
            }
        }

        selectedPath
    } catch (e: Exception) {
        println("❌ 目录选择失败: ${e.message}")
        selectDirectoryImproved() // 回退到改进版本
    }
}

private fun showDirectoryChooser(): String? {
    return try {
        // 设置系统外观
        UIManager.setLookAndFeel(UIManager.getLookAndFeel())

        val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory).apply {
            dialogTitle = "选择文件保存目录"
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
            // 设置默认目录（可选）
            currentDirectory = FileSystemView.getFileSystemView().homeDirectory
        }

        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            // 确保目录存在，如果不存在则创建
            if (!selectedFile.exists()) {
                selectedFile.mkdirs()
            }
            selectedFile.absolutePath
        } else {
            null
        }
    } catch (e: Exception) {
        println("❌ 目录选择器初始化失败: ${e.message}")
        null
    }
}

private fun setupConsoleEncoding() {
    try {
        // 设置系统属性
        System.setProperty("file.encoding", "UTF-8")

        // 强制设置标准输出的编码
        val out = PrintStream(System.out, true, StandardCharsets.UTF_8)
        val err = PrintStream(System.err, true, StandardCharsets.UTF_8)
        System.setOut(out)
        System.setErr(err)

        println("[SUCCESS] Console encoding set to UTF-8")
    } catch (e: Exception) {
        println("[ERROR] Failed to set console encoding: ${e.message}")
    }
}

fun main() = application {
    MainWindow(onCloseRequest = ::exitApplication
    )
}