/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/25 上午12:33
 * updated: 2024/8/25 上午12:33
 */

package sc.hwd.sofill.Us

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.tencent.mmkv.MMKV
import sc.hwd.sofill.android.BiometricCallback
import sc.hwd.sofill.android.BiometricPromptHelper
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun checkBiometric(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    onError: (CharSequence) -> Unit
) {
    val TAG = "checkBiometric"
    Log.d(TAG, "showBiometricPrompt() invoked")
    val mainHandler = Handler(Looper.getMainLooper())
    // 在主线程中执行
    mainHandler.post {
        // 在 MainActivity 中调用 showBiometricPrompt 方法
        try {
            BiometricPromptHelper(
                activity, callback =
                object : BiometricCallback {
                    override fun onAuthenticationSuccess() {
                        Log.d(TAG, "认证成功")
                        onSuccess.invoke()
                    }

                    override fun onAuthenticationFailed() {
                        Log.d(TAG, "认证失败")
                        onFailure.invoke()
                    }

                    override fun onAuthenticationError(errString: CharSequence) {
                        Log.w(TAG, "认证错误: $errString")
                        // 认证错误的处理逻辑（一般是用户点击了取消）
                        onError.invoke(errString)
                    }
                }).simpleBiometricPrompt()
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            throw RuntimeException(e)
        }
    }
}



fun displayTokenEndLimiter(inputStr: String, endLength: Int): String {
    val length = inputStr.length
    return if (length >= endLength) {
        "*".repeat(length - endLength) + inputStr.substring(length - endLength)
    } else {
        inputStr
    }
}

fun displayTokenLimiter(inputStr: String, startLength: Int, endLength: Int): String {
    val length = inputStr.length
    return if (length <= startLength) {
        inputStr
    } else {
        val starsCount = length - startLength - endLength
        if (starsCount > 0) {
            inputStr.substring(0, startLength) + "*".repeat(starsCount) + inputStr.substring(
                length - endLength
            )
        } else {
            inputStr.substring(
                0,
                startLength + starsCount
            ) + inputStr.substring(length - endLength)
        }
    }
}

/**
 * 生成AES密钥
 *
 */
fun generateAesKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(128) // 选择密钥大小，这里为128位
    return keyGenerator.generateKey()
}


/**
 * AES加密
 *
 */
@SuppressLint("GetInstance")
fun encryptAes(data: String, key: SecretKey): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
}


/**
 * AES解密
 *
 */
@SuppressLint("GetInstance")
fun decryptAes(encryptedData: String, key: SecretKey): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, key)
    val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
    return String(decryptedBytes, Charsets.UTF_8)
}

/**
 * 读取MMKV中存储的加密Token并进行解密
 *
 */
fun getDecryptedToken(mmkv: MMKV, MMKV_KEY: String, AES_KEY: String): String? {
    // 从MMKV中读取存储的AES密钥
    val encodedKey = mmkv.decodeString(AES_KEY, null) ?: return null
    val keyBytes = Base64.decode(encodedKey, Base64.DEFAULT)
    val aesKey = SecretKeySpec(keyBytes, "AES")

    // 从MMKV中读取存储的加密Token
    val encryptedToken = mmkv.decodeString(MMKV_KEY, null) ?: return null

    // 解密Token
    return decryptAes(encryptedToken, aesKey)
}