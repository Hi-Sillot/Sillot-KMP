/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2020-2024.
 *
 * lastModified: 2024/8/24 下午11:09
 * updated: 2024/8/24 下午11:09
 */
package org.b3log.siyuan

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.text.TextUtils
import com.google.common.base.Strings
import com.tencent.bugly.crashreport.BuglyLog
import mobile.Mobile
import org.apache.commons.io.FileUtils
import sc.hwd.sofill.Us.formatFromMillis
import java.io.BufferedInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 工具类.
 *
 * @author [Liang Ding](https://88250.b3log.org)
 * @author [Jane Haring](https://github.com/wwxiaoqi)
 * @version 1.1.0.7, Mar 20, 2024
 * @since 1.0.0
 */
object Utils {

    fun isFirstLaunch(activity: Activity): Boolean {
        val sharedPreferences = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply()
        }
        return isFirstLaunch
    }

    fun unzipAsset(assetManager: AssetManager, zipName: String, targetDirectory: String) {
        var zis: ZipInputStream? = null
        try {
            val zipFile = assetManager.open(zipName)
            zis = ZipInputStream(BufferedInputStream(zipFile))
            var ze: ZipEntry?
            var count: Int
            val buffer = ByteArray(1024 * 512)
            while ((zis.getNextEntry().also { ze = it }) != null) {
                val file = File(targetDirectory, ze!!.getName())
                try {
                    ensureZipPathSafety(file, targetDirectory)
                } catch (se: Exception) {
                    throw se
                }

                val dir = if (ze.isDirectory()) file else file.getParentFile()
                if (dir != null && !dir.isDirectory() && !dir.mkdirs()) throw FileNotFoundException(
                    "Failed to ensure directory: " + dir.getAbsolutePath()
                )
                if (ze.isDirectory()) continue
                FileOutputStream(file).use { fout ->
                    while ((zis.read(buffer).also { count = it }) != -1) fout.write(
                        buffer,
                        0,
                        count
                    )
                }             /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } catch (e: Exception) {
            LogError(
                "boot",
                "unzip asset [from=" + zipName + ", to=" + targetDirectory + "] failed",
                e
            )
        } finally {
            if (null != zis) {
                try {
                    zis.close()
                } catch (e: Exception) {
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun ensureZipPathSafety(outputFile: File, destDirectory: String) {
        val destDirCanonicalPath = (File(destDirectory)).getCanonicalPath()
        val outputFileCanonicalPath = outputFile.getCanonicalPath()
        if (!outputFileCanonicalPath.startsWith(destDirCanonicalPath)) {
            throw Exception(
                String.format(
                    "Found Zip Path Traversal Vulnerability with %s",
                    outputFileCanonicalPath
                )
            )
        }
    }

    val IPAddressList: String
        get() {
            val list: MutableList<String?> =
                ArrayList<String?>()
            try {
                val enNetI =
                    NetworkInterface.getNetworkInterfaces()
                while (enNetI.hasMoreElements()) {
                    val netI = enNetI.nextElement()
                    val enumIpAddr =
                        netI.getInetAddresses()
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress()) {
                            list.add(inetAddress.getHostAddress())
                        }
                    }
                }
            } catch (e: Exception) {
                LogError(
                    "network",
                    "get IP list failed, returns 127.0.0.1",
                    e
                )
            }
            list.add("127.0.0.1")
            return TextUtils.join(",", list)
        }

    @JvmStatic
    fun LogError(tag: String?, msg: String?, e: Throwable?) {
        synchronized(Utils::class.java) {
            if (null != e) {
                BuglyLog.e(tag, msg, e)
            } else {
                BuglyLog.e(tag, msg)
            }
            try {
                val workspacePath = Mobile.getCurrentWorkspacePath()
                if (Strings.isNullOrEmpty(workspacePath)) {
                    return
                }

                val mobileLogPath = workspacePath + "/temp/mobile.log"
                val logFile = File(mobileLogPath)
                if (logFile.exists() && 1024 * 1024 * 8 < logFile.length()) {
                    FileUtils.deleteQuietly(logFile)
                }

                val writer = FileWriter(logFile, true)
                val time = formatFromMillis(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")
                writer.write("E " + time + " " + tag + " " + msg + "\n")
                if (null != e) {
                    writer.write(e.toString() + "\n")
                }
                writer.flush()
                writer.close()
            } catch (ex: Exception) {
                BuglyLog.e("logging", "Write mobile log failed", ex)
            }
        }
    }

    fun LogInfo(tag: String?, msg: String?) {
        synchronized(Utils::class.java) {
            BuglyLog.i(tag, msg)
            try {
                val workspacePath = Mobile.getCurrentWorkspacePath()
                if (Strings.isNullOrEmpty(workspacePath)) {
                    return
                }

                val mobileLogPath = workspacePath + "/temp/mobile.log"
                val logFile = File(mobileLogPath)
                if (logFile.exists() && 1024 * 1024 * 8 < logFile.length()) {
                    FileUtils.deleteQuietly(logFile)
                }

                val writer = FileWriter(logFile, true)
                val time = formatFromMillis(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")
                writer.write("I " + time + " " + tag + " " + msg + "\n")
                writer.flush()
                writer.close()
            } catch (ex: Exception) {
                BuglyLog.e("logging", "Write mobile log failed", ex)
            }
        }
    }

    /**
     * Checks if the current package name contains ".debug" and if debug mode is enabled.
     *
     * @param context The Android context used to retrieve the package information.
     * @return true if the package name contains ".debug" and debug mode is enabled, false otherwise.
     */
    fun isDebugPackageAndMode(context: Context): Boolean {
        val packageManager = context.getPackageManager()
        var appInfo: ApplicationInfo? = null
        try {
            appInfo = packageManager.getApplicationInfo(context.getPackageName(), 0)
        } catch (e: PackageManager.NameNotFoundException) {
            LogError("isDebugPackageAndMode", e.getLocalizedMessage(), e)
        }

        // Check if the package name contains ".debug"
        val isDebugPackage =
            context.getPackageName() != null && context.getPackageName().contains(".debug")
        val isDebugMode =
            appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return isDebugPackage && isDebugMode
    }
}
