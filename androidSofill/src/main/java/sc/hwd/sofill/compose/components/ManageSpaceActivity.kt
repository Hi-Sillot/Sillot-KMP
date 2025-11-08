/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/25 上午12:58
 * updated: 2024/8/25 上午12:58
 */

package sc.hwd.sofill.compose.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import sc.hwd.sofill.R
import sc.hwd.sofill.Ss.S_Compose
import sc.hwd.sofill.Us.U_FileUtils
import sc.hwd.sofill.Us.U_FileUtils.getSizeRecursively
import sc.hwd.sofill.Us.checkBiometric
import sc.hwd.sofill.Us.thisSourceFilePath
import sc.hwd.sofill.android.toString_BiometricManagerCanAuthenticateState_BIOMETRIC_STRONG
import sc.hwd.sofill.android.toString_BiometricManagerCanAuthenticateState_DEVICE_CREDENTIAL
import sc.hwd.sofill.annotations.SillotActivity
import sc.hwd.sofill.annotations.SillotActivityType
import sc.hwd.sofill.compose.components.CommonTopAppBar
import sc.hwd.sofill.compose.components.WaitUI
import sc.hwd.sofill.compose.theme.CascadeMaterialTheme
import java.io.File

// TODO: 添加更多可清理内容
@SillotActivity(SillotActivityType.UseVisible)
class ManageSpaceActivity : FragmentActivity() {
    private val TAG = "ManageSpaceActivity.kt"
    private val srcPath = thisSourceFilePath(TAG)
    private lateinit var thisActivity: FragmentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thisActivity = this
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        setContent {
            CascadeMaterialTheme {
                WaitUI()
            }
        }
        checkBiometric(
            thisActivity,
            ::navigateToMainScreen,
            ::showRetryDialog,
            ::showBiometricErrorDialog
        )

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //设置竖屏锁定
    }

    private fun navigateToMainScreen() {
        // 认证成功，导航到主屏幕
        setContent {
            CascadeMaterialTheme {
                UI(intent, srcPath)
            }
        }
    }

    private fun showRetryDialog() {
        // 显示重试对话框
        setContent {
            CascadeMaterialTheme {
                WaitUI()
            }
        }
        checkBiometric(
            thisActivity,
            ::navigateToMainScreen,
            ::showRetryDialog,
            ::showBiometricErrorDialog
        )
    }

    private fun showBiometricErrorDialog(errString: CharSequence) {
        // 生物识别错误
        setContent {
            CascadeMaterialTheme {
                ErrorUI(errString)
            }
        }

    }


    @Composable
    private fun ErrorUI(message: CharSequence) {
        val isMenuVisible = rememberSaveable { mutableStateOf(false) }
        val biometricManager =
            BiometricManager.from(thisActivity)
        val state1 = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
        val state2 = biometricManager.canAuthenticate(DEVICE_CREDENTIAL)
        Scaffold(
            topBar = {
                CommonTopAppBar("当前需要认证", TAG, null, isMenuVisible) {
                    thisActivity.finish()
                }
            },
            bottomBar = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        setContent {
                            CascadeMaterialTheme {
                                WaitUI()
                            }
                        }
                        checkBiometric(
                            thisActivity,
                            ::navigateToMainScreen,
                            ::showRetryDialog,
                            ::showBiometricErrorDialog
                        )
                    },
                ) {
                    Text("重试认证")
                }
            }) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    ErrorText(state1.toString_BiometricManagerCanAuthenticateState_BIOMETRIC_STRONG())
                    ErrorText(state2.toString_BiometricManagerCanAuthenticateState_DEVICE_CREDENTIAL())
                    if (state1 == state2 &&
                        (state1 == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                                state1 == BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED)
                        ) {
                        Text(
                            text = "由于${message}，您当前可以跳过认证。",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = S_Compose.text_fontSize_xxxx.current,
                            lineHeight = S_Compose.text_lineHeight_xxxx.current,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis, // 如果文本仍然太长，则显示省略号
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navigateToMainScreen()
                                return@Button
                            },
                        ) {
                            Text("跳过认证")
                        }
                    } else {
                        Text(
                            text = message.toString(),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = S_Compose.text_fontSize_xxxx.current,
                            lineHeight = S_Compose.text_lineHeight_xxxx.current,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis, // 如果文本仍然太长，则显示省略号
                        )
                        ErrorText("基于用户数据安全考虑，您必须认证成功才能继续。")
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorText(text: String) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            fontSize = S_Compose.text_fontSize_xxxx.current,
            lineHeight = S_Compose.text_lineHeight_xxxx.current,
        )
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
private fun UI(intent: Intent?, TAG: String) {
    val uri = intent?.data
    val Lcc = LocalContext.current
     // val activityName = Lcc.getString(Lcc.resources.getIdentifier("", "string", Lcc.packageName)) // 在 Compose 中，使用 LocalContext.current.getString 代替 resources.getString() 方法来获取字符串资源
    val activityName = stringResource(R.string.activity_name_ManageSpaceActivity)
    val isMenuVisible = rememberSaveable { mutableStateOf(false) }
    val filesList = rememberSaveable { mutableStateOf<List<File>>(emptyList()) }
    val selectedFiles = rememberSaveable { mutableSetOf<String>() }
    val selectedFilesCount = rememberSaveable { mutableIntStateOf(0) } // 跟踪选中的文件数量
    var isCleaning by remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(true) }
    var refreshFilesList by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val mutex = Mutex()

    LaunchedEffect(key1 = isCleaning, key2 = refreshFilesList) {
        mutex.withLock {
            isLoading.value = true
            // 获取所有缓存目录的文件列表
            val cacheDirs = listOf(
                // 应用的外部文件存储目录，用于存储持久性文件，如音乐、图片等。这些文件对用户是可见的，并且会随着应用的卸载而被删除。
                // /storage/emulated/$userId/Android/data/$packageName/files
                Lcc.getExternalFilesDir(null),
                Lcc.filesDir, // 应用的内部文件存储目录，用于存储持久性文件，如应用下载的文件或应用生成的文件。 /data/user/$userId/$packageName/files
                Lcc.cacheDir, // 应用的内部缓存目录，用于存储临时缓存文件。系统可能会在设备存储空间不足时自动清理这个目录的内容。/data/data/$packageName/cache
                File(Lcc.filesDir.parent, "app_webview"), // 存储与WebView相关的缓存和数据 /data/data/$packageName/app_webview
                Lcc.noBackupFilesDir, // 不可备份文件目录 /data/data/$packageName/no_backup
            )

            val _filesList = cacheDirs.flatMap { dir ->
                dir?.listFiles()?.filterNotNull()?.filter {
                    it.exists() && it.getSizeRecursively() > 0
                } ?: emptyList()
            }.sortedByDescending { it.getSizeRecursively() } // 按大小降序排序
            filesList.value = _filesList
            isLoading.value = false
            refreshFilesList = false
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(activityName, TAG, uri, isMenuVisible) {
                // 将Context对象安全地转换为Activity
                if (Lcc is Activity) {
                    Lcc.finish() // 结束活动
                }
            }
        },
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                onClick = {
                    isCleaning = true
                    coroutineScope.launch {
                        launchCleaningProcess(selectedFiles) {
                            isCleaning = false
                            selectedFiles.clear()
                            selectedFilesCount.intValue = 0 // 清理后重置计数器
                        }
                    }
                },
                enabled = selectedFilesCount.intValue > 0 && !isCleaning && !refreshFilesList // selectedFiles.isNotEmpty() 判断根本行不通，见鬼
            ) {
                Text("清理选中项目")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    refreshFilesList = !refreshFilesList
                }) {
                Icon(Icons.Filled.Refresh, contentDescription = "刷新")
            }
        }
    ) {
        if (filesList.value.isEmpty() && !isLoading.value && !refreshFilesList && !isCleaning) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("已经很干净啦~")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(filesList.value) { file ->
                    val isExternalFilesDir =
                        file.path.startsWith(Lcc.getExternalFilesDir(null)?.path ?: "")
                    val isChecked = rememberSaveable(file.absolutePath) {
                        mutableStateOf(selectedFiles.contains(file.absolutePath))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked.value,
                            onCheckedChange = { checked ->
                                isChecked.value = checked
                                if (checked) {
                                    selectedFiles.add(file.absolutePath)
                                    selectedFilesCount.intValue++
                                } else {
                                    selectedFiles.remove(file.absolutePath)
                                    selectedFilesCount.intValue--
                                }
                            }
                        )
                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(0.dp)) {
                            Row {
                                Icon(
                                    imageVector = if (file.isDirectory) Icons.Filled.Folder else if (file.isFile) Icons.Filled.FileOpen else Icons.Filled.Archive,
                                    contentDescription = "item type",
                                    Modifier.size(16.dp).align(Alignment.CenterVertically)
                                )
                                Text(
                                    text = file.name,
                                    fontSize = 14.sp,
                                    color = if (isExternalFilesDir) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 4.dp).align(Alignment.CenterVertically)
                                )
                            }
                            Text(
                                text = file.absolutePath,
                                color = Color.Gray,
                                fontSize = 8.sp,
                                lineHeight = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis, // 如果文本仍然太长，则显示省略号
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            text = U_FileUtils.getFileOrFolderSize(file),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 14.dp).align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}

suspend fun launchCleaningProcess(selectedFiles: Set<String>, onComplete: () -> Unit) {
    withContext(Dispatchers.IO) {
        selectedFiles.forEach { filePath ->
            val file = File(filePath)
            file.deleteRecursively()
        }
        withContext(Dispatchers.Main) {
            onComplete()
        }
    }
}
