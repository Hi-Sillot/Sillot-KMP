package sc.hwd.sillot.testkmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import sc.hwd.sillot.shared2.Greeting

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Greeting("Android")
        }
        // 申请附近设备权限
        requestPermissions(arrayOf(android.Manifest.permission.NEARBY_WIFI_DEVICES), 0)
    }
}

