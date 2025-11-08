/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/25 上午1:03
 * updated: 2024/8/25 上午1:03
 */

package sc.hwd.sofill.Us

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.kongzue.dialogx.dialogs.PopTip
import sc.hwd.sofill.Ss.S_Intent

object U_FuckOtherApp {

    /**
     * @param packageManager
     * @param recipient 收件人
     * @param subject 邮件主题
     * @param body 邮件正文
     */
    fun Context.sendEmail(
        packageManager: PackageManager,
        recipient: String,
        subject: String?,
        body: String?
    ) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto:")) // only email apps should handle this

        // 设置收件人
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        // 设置邮件主题
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        // 设置邮件正文
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            PopTip.show("No email client found")
        }
    }

    /**
     * @param context 能够获取到 applicationContext 的都可以，传入 applicationContext 也没问题
     * @param qqNumber QQ号
     * @param tip 提示
     */
    fun Context.launchQQAndCopyToClipboard(qqNumber: CharSequence, tip: CharSequence) {
        // 将QQ号复制到剪贴板
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", qqNumber)
        clipboard.setPrimaryClip(clip)

        val intent: Intent? = packageManager.getLaunchIntentForPackage(S_Intent.QQ)

        if (intent != null) {
            startActivity(intent)
            Toast.makeText(applicationContext, tip, Toast.LENGTH_SHORT).show() // 启动后弹出，否则会被吞掉。因为跳转了，使用 applicationContext 才能显示
        } else {
            PopTip.show("QQ 未安装")
        }
    }

    /**
     * @param context 能够获取到 applicationContext 的都可以，传入 applicationContext 也没问题
     * @param TTA 抖音号
     * @param tip 提示
     */
    fun Context.launchTikTopAndCopyToClipboard(TTA: CharSequence, tip: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", TTA)
        clipboard.setPrimaryClip(clip)

        val intent: Intent? =
            packageManager.getLaunchIntentForPackage(S_Intent.抖音)

        if (intent != null) {
            startActivity(intent)
            Toast.makeText(applicationContext, tip, Toast.LENGTH_SHORT).show() // 启动后弹出，否则会被吞掉。因为跳转了，使用 applicationContext 才能显示
        } else {
            PopTip.show("抖音未安装")
        }
    }

    /**
     * @param context 能够获取到 applicationContext 的都可以，传入 applicationContext 也没问题
     * @param blockURL: 格式为 siyuan://blocks/xxx
     * @param tip 提示
     */
    fun Context.launchSiyuan(blockURL: String, tip: CharSequence? = null) {
        val intent: Intent? =
            packageManager.getLaunchIntentForPackage(S_Intent.SiYuan)

        if (intent != null) {
            intent.putExtra("blockURL", blockURL)
            startActivity(intent)
            tip?.let{ Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show() } // 启动后弹出，否则会被吞掉。因为跳转了，使用 applicationContext 才能显示
        } else {
            PopTip.show("思源笔记未安装")
        }
    }
}