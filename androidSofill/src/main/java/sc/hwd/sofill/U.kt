/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/27 下午3:26
 * updated: 2024/8/27 下午3:26
 */

package sc.hwd.sofill

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.kongzue.dialogx.dialogs.PopNotification
import sc.hwd.sofill.Ss.REQUEST_CODE_INSTALL_PERMISSION
import sc.hwd.sofill.Us.Toast
import sc.hwd.sofill.android.media.vedio.videoPlayer.SimplePlayer
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder


object U {
    private val TAG = "sc.hwd.sofill.U"

    fun Context.getMetaData(key: String): String? {
        val applicationInfo: ApplicationInfo? = try {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
        return applicationInfo?.metaData?.getString(key)
    }

    /**
     * 判断颜色是否为亮色
     */
    fun Int.isLightColor(): Boolean {
        val darkness = 1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
        return darkness < 0.5
    }

    fun genSpannableColorfulString(text: CharSequence, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        val foregroundColorSpan = ForegroundColorSpan(color)
        // 设置颜色范围为整个字符串，从起始位置0到字符串长度
        spannableString.setSpan(
            foregroundColorSpan,
            0,
            spannableString.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    fun genSpannableColorfulRangeString(
        text: CharSequence,
        color: Int,
        start: Int,
        end: Int
    ): SpannableString {
        val spannableString = SpannableString(text)
        val foregroundColorSpan = ForegroundColorSpan(color)
        // 设置颜色范围为整个字符串，从起始位置0到字符串长度
        spannableString.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannableString
    }


    fun isSystemDarkMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun getWifiSignalStrengthLevel(signalStrength: Int): String {
        return when {
            signalStrength >= -50 -> "极好"
            signalStrength >= -60 -> "很好"
            signalStrength >= -70 -> "正常"
            signalStrength >= -80 -> "一般"
            signalStrength >= -90 -> "较弱"
            else -> "极弱"
        }
    }

    /**
     * TODO: 没找到可行方法
     */
    fun getMobileDataSignalStrengthLevel(signalStrength: Int) {
    }


    /**
     * 比较两个版本字符串的大小。
     *
     * @param version1 要比较的第一个版本字符串。
     * @param version2 要比较的第二个版本字符串。
     * @return 如果第一个版本字符串小于、等于或大于第二个版本字符串，则分别返回小于零、零或大于零的整数。
     *
     * 该函数根据点分隔符将版本字符串分割成各个部分，然后逐部分进行数值比较。如果数值部分相等，
     * 则继续比较下一部分。如果一个版本字符串比另一个拥有更多的部分，且所有之前的部分都相等，
     * 则拥有更多部分的版本字符串被认为是更大的。如果某部分无法转换为整数，则默认为零。
     */
    @JvmStatic
    fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val parts2 = version2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in 0 until Math.min(parts1.size, parts2.size)) {
            val num1 = parts1[i].toIntOrNull() ?: 0
            val num2 = parts2[i].toIntOrNull() ?: 0
            if (num1 < num2) return -1
            if (num1 > num2) return 1
        }

        return when {
            parts1.size < parts2.size -> -1
            parts1.size > parts2.size -> 1
            else -> 0
        }
    }

    fun replaceScheme_deepDecode(url: String, old: String, new: String): String {
        // 解码URL
        var decodedUrl = URLDecoder.decode(url, "UTF-8")
        // 替换scheme
        decodedUrl = decodedUrl.replace(old, new)
        var previousUrl: String
        do {
            previousUrl = decodedUrl
            // 再次解码URL
            decodedUrl = URLDecoder.decode(decodedUrl, "UTF-8")
        } while (decodedUrl != previousUrl)

        return decodedUrl
    }

    fun replaceEncodeScheme(url: String, old: String, new: String): String {
        return url.replace(URLEncoder.encode(old), URLEncoder.encode(new))
    }

    fun parseAndDecodeUrl(url: String, regex: Regex): String {
        val decodedUrls = regex.findAll(url).map { matchResult ->
            val encodedUrl = matchResult.groupValues[1]
            URLDecoder.decode(encodedUrl, "UTF-8")
        }.joinToString(separator = " ", prefix = "\"", postfix = "\"")

        // 使用解码后的 URL 替换原始 URL 中的匹配部分
        return regex.replace(url, decodedUrls)
    }


    fun isStorageSpaceAvailable(contentResolver: ContentResolver, uri: Uri): Boolean {
        contentResolver.openFileDescriptor(uri, "r").use { pfd ->
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val fileSize = pfd?.statSize ?: 0
                val buffer = ByteArray(8 * 1024) // 缓冲区大小为 8 KB
                var bytesRead: Int
                var totalBytesRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    // 假设存储空间不足的临界值为文件大小的三倍
                    if (totalBytesRead > fileSize * 3) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun handleVideo(context: Context, uri: Uri) {
        val videoPath = if (uri.scheme == "file") {
            // 本地文件
            uri.path ?: ""
        } else {
            // URL
            uri.toString()
        }

        val intent = Intent(context, SimplePlayer::class.java)
        intent.putExtra("videoPath", videoPath)
        context.startActivity(intent)
    }

    /**
     * 安装 apk 文件，需要申请权限。不申请权限安装请使用 installApk2
     */
    fun installApk(activity: Activity, apkUri: Uri) {
        try {
            val installIntent: Intent

            // 检查是否已有安装未知来源应用的权限
            val packageManager = activity.packageManager
            val hasInstallPermission = packageManager.canRequestPackageInstalls()
            if (!hasInstallPermission) {
                Toast.Show(activity, "请先授予汐洛安装未知应用权限")
                // 启动授权 activity
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                activity.startActivityForResult(
                    intent,
                    REQUEST_CODE_INSTALL_PERMISSION
                )
                return
            }
            // Android N及以上版本需要额外权限
            installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE) // 忽略已弃用，神金搞那么复杂
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                installIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // 安装应用不需要写权限，如果是另一个应用的私有文件会导致无法安装

            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            activity.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("Us.installApk", e.toString())
            PopNotification.show("任务失败", e.toString()).noAutoDismiss()
        }
    }

    /**
     * 安装 apk 文件，无需申请权限，但是需要有对应处理软件（一般系统都自带）。申请权限安装请使用 installApk
     */
    fun installApk2(activity: Activity, apkUri: Uri) {
        try {
            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // 如果是第三方软件提供的安装包，确保继承了读取权限
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // 创建一个选择器对话框，让用户选择使用哪个应用来打开APK文件
            val chooserIntent = Intent.createChooser(installIntent, "选择安装应用的方式")

            // 启动系统提供的对话框，让用户选择处理意图的应用
            activity.startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // 如果没有找到可以处理的应用，提示用户
            PopNotification.show(
                "任务失败",
                "没有找到可以安装APK的应用，请尝试使用文件管理器或其他第三方应用打开APK文件。"
            )
        } catch (e: Exception) {
            Log.e("Us.installApk", e.toString())
            PopNotification.show("任务失败", e.toString()).noAutoDismiss()
        }
    }

    /**
     * 使用第三方应用打开视频文件。
     *
     * @param activity 上下文Activity
     * @param videoUri 视频文件的Uri
     */
    fun openVideoWithThirdPartyApp(activity: Activity, videoUri: Uri) {
        try {
            val videoIntent = Intent(Intent.ACTION_VIEW)
            videoIntent.setDataAndType(videoUri, "video/*")
            videoIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // 确保第三方应用有读取该URI的权限
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooserIntent = Intent.createChooser(videoIntent, "选择处理此视频的应用")

            // 如果有可以处理该意图的应用，则启动选择器对话框
            if (videoIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(chooserIntent)
            } else {
                // 如果没有找到可以处理的应用，提示用户
                PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
            }
        } catch (e: ActivityNotFoundException) {
            // 如果没有找到可以处理的应用，提示用户
            PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
        } catch (e: Exception) {
            // 其他异常处理
            PopNotification.show("任务失败", "打开视频时出错: ${e.message}")
        }
    }

    /**
     * 使用第三方应用打开视频文件。
     *
     * @param activity 上下文Activity
     * @param audioUri 音频文件的Uri
     */
    fun openAudioWithThirdPartyApp(activity: Activity, audioUri: Uri) {
        try {
            val videoIntent = Intent(Intent.ACTION_VIEW)
            videoIntent.setDataAndType(audioUri, "audio/*")
            videoIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // 确保第三方应用有读取该URI的权限
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooserIntent = Intent.createChooser(videoIntent, "选择处理此音频的应用")

            // 如果有可以处理该意图的应用，则启动选择器对话框
            if (videoIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(chooserIntent)
            } else {
                // 如果没有找到可以处理的应用，提示用户
                PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
            }
        } catch (e: ActivityNotFoundException) {
            // 如果没有找到可以处理的应用，提示用户
            PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
        } catch (e: Exception) {
            // 其他异常处理
            PopNotification.show("任务失败", "打开音频时出错: ${e.message}")
        }
    }

    fun deleteFileByUri(context: Context, uri: Uri): Boolean {
        // 获取ContentResolver实例
        val contentResolver = context.contentResolver

        // 尝试从内容提供者中删除文件
        try {
            // 删除文件，这个调用会同时从文件系统和内容提供者的数据库中删除文件
            val deletedRows = contentResolver.delete(uri, null, null)

            // 如果删除的行数大于0，则表示文件删除成功
            if (deletedRows > 0) {
                return true
            }
        } catch (e: Exception) {
            // 处理可能出现的异常，例如权限问题或文件不存在
            Log.e("FileDelete", "Error deleting file", e)
        }

        // 删除失败
        return false
    }

    fun notifyGallery(context: Context, imageUri: Uri) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(
                context, arrayOf(imageUri.toString()), null
            ) { path: String, uri: Uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } else {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.setData(imageUri)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    fun notifyGallery(context: Context, imageFile: File) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(
                context, arrayOf(imageFile.toString()), null
            ) { path: String, uri: Uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } else {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(imageFile)
            mediaScanIntent.setData(contentUri)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    fun notifyGallery(activity: Activity, imageFile: File) {
        notifyGallery(activity as Context, imageFile)
    }

}