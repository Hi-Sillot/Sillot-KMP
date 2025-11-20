package sc.hwd.sillot.shared2

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarMode
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TextField
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
    val colors = if (isDarkTheme) darkColorScheme() else lightColorScheme()

    var checked by remember { mutableStateOf(false) }
    var useSmallTopBar by remember { mutableStateOf(false) }
    val items = listOf(
        NavigationItem("首页", MiuixIcons.Useful.NavigatorSwitch),
        NavigationItem("我的", MiuixIcons.Useful.Personal),
        NavigationItem("设置", MiuixIcons.Useful.Settings)
    )
    var selectedIndex by remember { mutableStateOf(0) }

    // ✅ 关键：实例化 ViewModel 并收集状态
    val vm = remember { DiscoveryViewModel() }
    val discovered by vm.discovered.collectAsState(emptyList())
    val syncText by vm.inputText.collectAsState()

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
                    mode = FloatingNavigationBarMode.IconOnly
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxSize()
            ) {
                Text("Hello $name! love from shared2")
                Spacer(Modifier.height(12.dp))
                Switch(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
                Spacer(Modifier.height(16.dp))

                // ✅ 服务发现列表
                Text("发现的服务 (${discovered.size})")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MiuixTheme.colorScheme.background
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (discovered.isEmpty()) {
                            item {
                                Text("暂无设备在线")
                            }
                        }
                        items(discovered, key = { it.name }) { item ->
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Text(item.name)
                                Text("@ ${item.host}:${item.port}")
                            }
                            Divider(Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ✅ 同步输入框
                Text("输入框内容实时同步")
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = syncText,
                    onValueChange = { vm.inputText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("在这里输入文字，其他设备会同步显示") },
                    singleLine = true
                )
            }
        }
    }
}