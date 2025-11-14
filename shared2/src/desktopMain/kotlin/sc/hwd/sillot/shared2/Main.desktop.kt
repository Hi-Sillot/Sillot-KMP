package sc.hwd.sillot.shared2

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
import sc.hwd.sillot.shared2.theme.WindowsThemeManager
import javax.swing.SwingUtilities

fun main() = application {
    val state = rememberWindowState(
        size = DpSize(1200.dp, 800.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )


    Window(
        state = state,
        resizable = true,
        title = "汐洛",
        onCloseRequest = ::exitApplication
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
        Greeting("Sillot Desktop")
    }
}
