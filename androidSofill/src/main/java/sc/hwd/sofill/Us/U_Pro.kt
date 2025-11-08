/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/25 上午12:49
 * updated: 2024/8/25 上午12:49
 */

package sc.hwd.sofill.Us

import android.app.Activity
import android.content.Intent
import android.view.DragEvent
import android.view.View
import android.webkit.WebView
import java.io.File

object U_Pro {

    /**
     * 转发处理
     */
    @JvmStatic
    fun View.onDragSend2Producer(event: DragEvent, targetActivityClass: Class<out Activity>) {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val clipData = event.clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    val intent = Intent(this.context, targetActivityClass)
                    when {
                        item.uri != null -> {
                            intent.data = item.uri
                            intent.action = event.action.toString()
                        }

                        item.text != null -> {
                            val text = item.text.toString()
                            intent.putExtra("text_data", text)
                            intent.action = Intent.ACTION_SEND
                            intent.type = "text/plain"
                        }
                    }
                    this.context.startActivity(intent)
                }
            }
        }
    }
    /**
     * 转发处理 - 使用字符串类名参数
     */
    @JvmStatic
    fun View.onDragSend2Producer(event: DragEvent, targetActivityClassName: String) {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val clipData = event.clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    val intent = Intent()
                    intent.setClassName(this.context, targetActivityClassName)

                    when {
                        item.uri != null -> {
                            intent.data = item.uri
                            intent.action = event.action.toString()
                        }

                        item.text != null -> {
                            val text = item.text.toString()
                            intent.putExtra("text_data", text)
                            intent.action = Intent.ACTION_SEND
                            intent.type = "text/plain"
                        }
                    }
                    this.context.startActivity(intent)
                }
            }
        }
    }


    @JvmStatic
    fun WebView.onDragWebView(event: DragEvent, callback: ContentInsertCallback) {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val clipData = event.clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    when {
                        item.uri != null -> {
                            val file = U_FileUtils.getFileFromUri(context, item.uri)
                            if (file != null) {
                                callback.onFileContentInsert(file, this)
                            }
                        }
                        item.text != null -> {
                            callback.onStringContentInsert(item.text.toString(), this)
                        }
                        else -> {
                            callback.onDefault(event, this)
                        }
                    }
                }
            }
        }
    }

    interface ContentInsertCallback {
        fun onStringContentInsert(content: String, webView: WebView)
        fun onFileContentInsert(file: File, webView: WebView)
        fun onDefault(event: DragEvent, webView: WebView)
    }

}
