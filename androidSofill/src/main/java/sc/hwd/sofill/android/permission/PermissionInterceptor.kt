/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/24 下午1:10
 * updated: 2024/8/24 下午1:10
 */

package sc.hwd.sofill.android.permission

import android.app.Activity
import android.app.AlertDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionInterceptor
import com.hjq.permissions.XXPermissions

/**
 * 适用于需要跳转或者OEM魔改的权限，普通权限不需要此拦截器
 */
class PermissionInterceptor @JvmOverloads constructor(
    private var mPermissionDescription: String? = null
) : OnPermissionInterceptor {

    override fun launchPermissionRequest(
        activity: Activity,
        allPermissions: List<String>,
        callback: OnPermissionCallback?
    ) {
        if (XXPermissions.isGranted(activity, allPermissions)) {
            callback?.onGranted(allPermissions, true)
        } else {
            // 显示一个对话框
            AlertDialog.Builder(activity)
                .setTitle("需要授权才能继续")
                .setMessage(
                    "需授予下列权限：\n${allPermissions.joinToString(", \n")}"
                )
                .setPositiveButton("前往授权") { _, _ ->
                    super.launchPermissionRequest(activity, allPermissions, callback)
                }
                .setNegativeButton("任性拒绝") { _, _ ->
                    callback?.onDenied(allPermissions, false)
                }.show()
        }
    }

    override fun grantedPermissionRequest(
        activity: Activity,
        allPermissions: List<String>,
        grantedPermissions: List<String>,
        allGranted: Boolean,
        callback: OnPermissionCallback?
    ) {
        callback?.onGranted(grantedPermissions, allGranted)
        super.grantedPermissionRequest(activity, allPermissions, grantedPermissions, allGranted, callback)
    }

    override fun deniedPermissionRequest(
        activity: Activity,
        allPermissions: List<String>,
        deniedPermissions: List<String>,
        doNotAskAgain: Boolean,
        callback: OnPermissionCallback?
    ) {
        callback?.onDenied(deniedPermissions, doNotAskAgain)
        super.deniedPermissionRequest(activity, allPermissions, deniedPermissions, doNotAskAgain, callback)
    }

    override fun finishPermissionRequest(
        activity: Activity,
        allPermissions: List<String>,
        skipRequest: Boolean,
        callback: OnPermissionCallback?
    ) {
        super.finishPermissionRequest(activity, allPermissions, skipRequest, callback)
    }

}

