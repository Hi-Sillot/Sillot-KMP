/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/23 下午12:41
 * updated: 2024/8/23 下午12:41
 */

package sc.hwd.sofill.android.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import sc.hwd.sofill.Ss.REQUEST_OVERLAY
import sc.hwd.sofill.Us.Toast
import androidx.core.net.toUri


/**
 * 定义回调接口
 */
interface OverlayOptimizationCallback {
    fun onOverlayOptimizationResult(isGranted: Boolean, requestCode: Int)
}

class Overlay : AppCompatActivity() {
    companion object {
        const val EXTRA_REQUEST_CODE = "extra_request_code"

        // 静态回调实例
        private var callback: OverlayOptimizationCallback? = null

        fun setCallback(callback: OverlayOptimizationCallback) {
            this.callback = callback
        }
    }
    private lateinit var requestOverlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestCode = intent.getIntExtra(Battery.Companion.EXTRA_REQUEST_CODE, REQUEST_OVERLAY)
        val appContext: Context = applicationContext
        Toast.Show(appContext, "找到汐洛并允许显示悬浮窗")

        requestOverlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val isGranted = Settings.canDrawOverlays(appContext)

            if (isGranted) {
                Toast.Show(appContext, "已获取显示悬浮窗权限")
            } else {
                Toast.Show(appContext, "未获取显示悬浮窗权限")
            }

            // 通过回调通知结果
            callback?.onOverlayOptimizationResult(isGranted, requestCode)

            finish() // 用户已经做出选择，现在可以结束 Activity
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        ) {
            Toast.Show(appContext, "请允许显示悬浮窗权限以实现某些功能")
            finish()
        } else {
            if (!Settings.canDrawOverlays(appContext)) {
                requestOverlayPermission()
            } else {
                finish()
            }
        }


    }

    private fun requestOverlayPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:$packageName".toUri()
            requestOverlayPermissionLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
