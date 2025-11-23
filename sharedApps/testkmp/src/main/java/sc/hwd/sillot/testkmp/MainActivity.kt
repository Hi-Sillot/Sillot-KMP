package sc.hwd.sillot.testkmp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import sc.hwd.sillot.shared2.App
import sc.hwd.sillot.shared2.DiscoveryViewModel
import sc.hwd.sillot.shared2.actual.AndroidContextProvider
import sc.hwd.sillot.shared2.expect.TcpFileTransfer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContextProvider.init(application)
        setContent {
            App("Android", onSelectDirectory = {
                dirPickerLauncher.launch(null)  // ✅ 启动系统目录选择器
            })
        }
        // 申请附近设备权限
        requestPermissions(arrayOf(android.Manifest.permission.NEARBY_WIFI_DEVICES), 0)
    }

    // ✅ 使用 OpenDocumentTree 获取持久化权限
    private val dirPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { treeUri ->
            // ✅ 关键：立即获取持久化权限
            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // ✅ 设置给 DiscoveryViewModel
            DiscoveryViewModel().setSaveDirectory(treeUri.toString())
        }
    }
}

