package sc.hwd.sillot.shared2

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarMode
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Back
import top.yukonga.miuix.kmp.icon.icons.useful.NavigatorSwitch
import top.yukonga.miuix.kmp.icon.icons.useful.Personal
import top.yukonga.miuix.kmp.icon.icons.useful.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Composable
fun Greeting(name: String, isDarkTheme: Boolean = isSystemInDarkTheme()) {
 App(name)
}

@Composable
fun App(name: String) {
    val isDarkTheme = isSystemInDarkTheme()
    val colors = if (isDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }
    var checked by remember { mutableStateOf(false) }
    var useSmallTopBar by remember { mutableStateOf(false) }
    val items = listOf(
        NavigationItem("首页", MiuixIcons.Useful.NavigatorSwitch),
        NavigationItem("我的", MiuixIcons.Useful.Personal),
        NavigationItem("设置", MiuixIcons.Useful.Settings)
    )
    var selectedIndex by remember { mutableStateOf(0) }
    MiuixTheme(colors = colors) {
        Scaffold(
            topBar = {
                Box {
                    if (useSmallTopBar) {
                        SmallTopAppBar(
                            title = "精简模式",
                            navigationIcon = {
                                IconButton(onClick = { useSmallTopBar = false }) {
                                    Icon(
                                        imageVector = MiuixIcons.Useful.Back,
                                        contentDescription = "切换到大标题",
                                        tint = MiuixTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        )
                    } else {
                        TopAppBar(
                            title = "标题",
                            largeTitle = "展开模式",
                            navigationIcon = {
                                IconButton(onClick = { useSmallTopBar = true }) {
                                    Icon(
                                        imageVector = MiuixIcons.Useful.Back,
                                        contentDescription = "切换到小标题",
                                        tint = MiuixTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        )
                    }
                }
            },
            bottomBar = {
                FloatingNavigationBar(
                    items = items,
                    selected = selectedIndex,
                    onClick = { selectedIndex = it },
                    mode = FloatingNavigationBarMode.IconOnly // 仅显示图标
                )
            },
            floatingActionButton = {
                // FloatingActionButton
            }
        ) {
                paddingValues ->
            // 内容区域需要考虑 padding
            Box(
                modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding(), start = 26.dp)
                    .fillMaxSize()
            ) {
                Column {
                    Text(text = "Hello $name! love from shared2")

                    Switch(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )

                    Button(
                        onClick = { /* 处理点击事件 */ }
                    ) {
                        Text("按钮")
                    }
                }
            }
        }
    }
}