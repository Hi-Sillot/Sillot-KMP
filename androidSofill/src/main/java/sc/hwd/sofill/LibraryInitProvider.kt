package sc.hwd.sofill

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import sc.hwd.sofill.android.lifecycle.services.ServiceWaiterManager

/**
 * VERSION_NAME 和 VERSION_CODE 都是在 androidApp 的 build.gradle.kts 中配置的
 */
class LibraryInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.applicationContext?.let { context ->
            try {
                val buildConfigClass = Class.forName("${context.packageName}.BuildConfig")
                val versionName = buildConfigClass.getField("VERSION_NAME").get(null) as String
                val versionCode = buildConfigClass.getField("VERSION_CODE").get(null) as Int
                val BuildConfig_PROVIDER_AUTHORITIES = buildConfigClass.getField("PROVIDER_AUTHORITIES").get(null) as String
                LibraryConfig.setup(versionName, versionCode, BuildConfig_PROVIDER_AUTHORITIES)

                ServiceWaiterManager.init(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    // 其他方法可以空实现
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?) = null
    override fun getType(uri: Uri) = null
    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?) = 0
}