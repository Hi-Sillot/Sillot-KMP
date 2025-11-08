/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/9/2 上午7:01
 * updated: 2024/9/2 上午7:01
 */

package sc.hwd.sofill.interfaces.gibbet

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import mobile.CreateDocWithMdResponse
import mobile.NotebookListResponse

data class IResponse<T>(val code: Int,
                        val data: T,
                        val msg: String)

data class INbList(val notebooks: List<INotebook>)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class INotebook(
    val closed: Boolean,
    val dueFlashcardCount: Int,
    val flashcardCount: Int,
    val icon: String,
    val id: String,
    val name: String,
    val newFlashcardCount: Int,
    val sort: Int,
    val sortMode: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class IPayload(val markdown: String, val notebook: String, val path: String)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ICreateDocWithMdRequest(
    val Markdown: String, val Notebook: String, val Path: String,
    val ID: String? = null,
    val ParentID: String? = null,
    val WithMath: Boolean = false
    )

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class IAppendBlockRequest(
    val Data: String,
    val DataType: String,
    val ParentID: String,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class IInsertBlockNextRequest(
    val Data: String,
    val DataType: String,
    val PreviousID: String,
)

/**
 * filelock.go
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ISiyuanFilelockWalk(val dir: String)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ISiyuanFilelockWalkRes(
    val code: Int,
    val msg: String,
    val data: ISiyuanFilelockWalkResFiles?
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ISiyuanFilelockWalkResFiles(
    val files: MutableList<ISiyuanFilelockWalkResFilesItem>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ISiyuanFilelockWalkResFilesItem(
    val path: String,
    val name: String,
    val size: Long,
    val updated: Long,
    val isDir: Boolean
)

@Keep
@Parcelize
data class INotebookListResponse(
    val value: @RawValue NotebookListResponse,
) : Parcelable {
    override fun toString(): String {
        return "$value"
    }
}

@Keep
@Parcelize
data class ICreateDocWithMdResponse(
    val value: @RawValue CreateDocWithMdResponse,
) : Parcelable {
    override fun toString(): String {
        return "$value"
    }
}