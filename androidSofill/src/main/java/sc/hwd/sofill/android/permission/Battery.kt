/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/25 上午1:04
 * updated: 2024/8/25 上午1:04
 */

package sc.hwd.sofill.android.permission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import sc.hwd.sofill.Ss.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT
import sc.hwd.sofill.Ss.S_Events
import sc.hwd.sofill.Us.Toast
import androidx.core.net.toUri

/**
 * 定义回调接口
 */
interface BatteryOptimizationCallback {
    fun onBatteryOptimizationResult(isGranted: Boolean, requestCode: Int)
}

class Battery : AppCompatActivity() {
    companion object {
        const val EXTRA_REQUEST_CODE = "extra_request_code"

        // 静态回调实例
        private var callback: BatteryOptimizationCallback? = null

        fun setCallback(callback: BatteryOptimizationCallback) {
            this.callback = callback
        }
    }

//    在应用清单声明对应 activity
    private lateinit var requestIgnoreBatteryOptimizationsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT)
        val appContext: Context = applicationContext

        requestIgnoreBatteryOptimizationsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val isGranted = result.resultCode == RESULT_OK || isIgnoringBatteryOptimizations()

            if (isGranted) {
                Toast.Show(appContext, "已加入电池优化的白名单")
            } else {
                Toast.Show(appContext, "未加入电池优化的白名单")
            }

            // 通过回调通知结果
            callback?.onBatteryOptimizationResult(isGranted, requestCode)

            finish() // 用户已经做出选择，现在可以结束 Activity
        }

        if (!isIgnoringBatteryOptimizations()) {
            // 未加入电池优化的白名单，则弹出系统弹窗供用户选择
            requestIgnoreBatteryOptimizations()
        } else {
            callback?.onBatteryOptimizationResult(true, requestCode)
            // 已加入电池优化的白名单，直接结束 Activity
            finish()
        }
    }


    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) // REF https://developer.android.com/reference/android/provider/Settings#ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = "package:$packageName".toUri()
            requestIgnoreBatteryOptimizationsLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.onBatteryOptimizationResult(false,
                intent.getIntExtra(EXTRA_REQUEST_CODE, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT)
            )
            finish()
        }
    }

}
