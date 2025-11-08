/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/22 13:12
 * updated: 2024/8/22 13:12
 */

package sc.hwd.sofill.android

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.kongzue.dialogx.dialogs.PopNotification
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * 国内定制ROM可能因为安全问题，仅支持指纹
 */
class BiometricPromptHelper(
    private val activity: FragmentActivity,
    private val callback: BiometricCallback
) {
    /**
     * 使用强生物认证时支持回退到锁屏凭据认证
     */
    var strongCanBackToDeviceCredential = true

    /**
     * 允许无法进行生物认证时使用锁屏凭据认证，此项为 `true` （默认值）时将忽略 [strongCanBackToDeviceCredential] 配置（视为 `true`）
     */
    var allowDeviceCredential = false
    private val semaphore = Semaphore(0)
    private val biometricManager = BiometricManager.from(activity)
    private var promptInfo: BiometricPrompt.PromptInfo? = null
    private var prompt: BiometricPrompt? = null
    private val executorBiometric = ContextCompat.getMainExecutor(activity)
    private val cb = object : BiometricPrompt.AuthenticationCallback() {
        /**
         * 在遇到不可恢复的错误且身份验证已停止时调用。
         * 认证错误一般是：
         *  [13] 取消 - 点击 `NegativeButton`
         *  [10] 用户取消了指纹操作。 - 点击认证界面关闭按钮
         */
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            callback.onAuthenticationError("[$errorCode] $errString")
            semaphore.release() // 出现错误时释放锁
        }

        /**
         * 当识别到生物识别信息（例如指纹、面部等）时调用，表示用户已成功进行身份验证。
         */
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // 认证成功的逻辑处理可以在这里进行
            try {
                callback.onAuthenticationSuccess()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            semaphore.release() // 认证成功时释放锁
        }

        /**
         * 当生物识别信息（例如指纹、面部等）出现但未被识别为属于用户时调用。
         */
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            callback.onAuthenticationFailed()
            semaphore.release() // 认证失败时释放锁
        }
    }

    /**
     * TODO
     */
    fun simpleBiometricPrompt() {
        val TAG = "newBiometricPrompt"
        (promptInfo ?: newPromptInfo())?.let {
            // 等待认证结果
            try {
                (prompt ?: newBiometricPrompt(activity))?.authenticate(it)
                semaphore.tryAcquire(1, TimeUnit.SECONDS)
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
            }
        } ?: {
            val state =
                if (strongCanBackToDeviceCredential || allowDeviceCredential) "要求提供生物识别或锁屏凭据验证，但均不可用"
                else "要求提供生物识别验证，但暂不可用"
            PopNotification.show(state)
            callback.onAuthenticationError(state)
        }()

    }

    private fun newBiometricPrompt(
        activity: FragmentActivity
    ): BiometricPrompt? {
        prompt = BiometricPrompt(activity, executorBiometric, cb)
        return prompt
    }

    private fun newPromptInfo(): BiometricPrompt.PromptInfo? {
        val TAG = "getPromptInfo"
        val _BIOMETRIC_STRONG =
            biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        val _DEVICE_CREDENTIAL =
            biometricManager.canAuthenticate(DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        Log.d(
            TAG,
            "生物识别是否可用：$_BIOMETRIC_STRONG | 是否支持回退到锁屏凭据认证：$strongCanBackToDeviceCredential"
        )
        Log.d(
            TAG,
            "屏凭据验证是否可用：$_DEVICE_CREDENTIAL | 是否允许无法进行生物认证时使用锁屏凭据认证：$allowDeviceCredential"
        )
        if (allowDeviceCredential) {
            promptInfo =
                if (_BIOMETRIC_STRONG) genPromptInfo() else genPromptInfo_DEVICE_CREDENTIAL()
        } else if (strongCanBackToDeviceCredential) {
            promptInfo =
                if (_BIOMETRIC_STRONG) genPromptInfo() else genPromptInfo_DEVICE_CREDENTIAL()
        } else {
            promptInfo = if (_BIOMETRIC_STRONG) genPromptInfo_BIOMETRIC_STRONG() else null
        }

        return promptInfo
    }

    /**
     * 请注意，`setNegativeButtonText` 与设备凭据身份验证不兼容，如果通过 `setAllowedAuthenticators`
     * 或 `setDeviceCredentialAllowed` 启用了设备凭据身份验证，则不得设置此选项。
     */
    private fun genPromptInfo(): BiometricPrompt.PromptInfo? {
        val biometricStr = biometricManager.getStrings(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("机主身份验证")
            .setSubtitle(biometricStr?.settingName.toString())
            .setDescription(biometricStr?.promptMessage.toString())
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) // 支持回退到锁屏凭据
            .build()
    }

    /**
     * 一般情况不推荐，应当使用 [genPromptInfo]
     */
    private fun genPromptInfo_BIOMETRIC_STRONG(): BiometricPrompt.PromptInfo? {
        val biometricStr = biometricManager.getStrings(BIOMETRIC_STRONG)
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物识别验证")
            .setSubtitle(biometricStr?.settingName.toString())
            .setDescription(biometricStr?.promptMessage.toString())
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
    }

    private fun genPromptInfo_DEVICE_CREDENTIAL(): BiometricPrompt.PromptInfo? {
        val biometricStr = biometricManager.getStrings(DEVICE_CREDENTIAL)
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("锁屏凭据验证")
            .setSubtitle(biometricStr?.settingName.toString())
            .setDescription(biometricStr?.promptMessage.toString())
            .setAllowedAuthenticators(DEVICE_CREDENTIAL)
            .build()
    }
}

/**
 * 定义BiometricCallback接口
 */
interface BiometricCallback {
    fun onAuthenticationSuccess()
    fun onAuthenticationFailed()
    fun onAuthenticationError(errString: CharSequence)
}

fun Int.toString_BiometricManagerCanAuthenticateState_BIOMETRIC_STRONG(): String {
    return when (this) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            "应用可以进行生物识别技术进行身份验证。"
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            "该设备上没有搭载可用的生物特征功能。"
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            "生物识别功能当前不可用。"
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            "用户没有录入生物识别数据。"
        }

        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            "设备需要安全更新才能使用生物识别功能。"
        }

        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            "设备不支持生物识别功能。"
        }

        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            "生物识别功能的当前状态未知。"
        }

        else -> {
            "未知原因"
        }
    }
}

fun Int.toString_BiometricManagerCanAuthenticateState_DEVICE_CREDENTIAL(): String {
    return when (this) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            "应用可以进行锁屏凭据进行身份验证。"
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            "该设备上没有搭载可用的锁屏凭据功能。"
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            "锁屏凭据功能当前不可用。"
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            "用户没有录入锁屏凭据数据。"
        }

        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            "设备需要安全更新才能使用锁屏凭据功能。"
        }

        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            "设备不支持锁屏凭据功能。"
        }

        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            "锁屏凭据功能的当前状态未知。"
        }

        else -> {
            "未知原因"
        }
    }
}