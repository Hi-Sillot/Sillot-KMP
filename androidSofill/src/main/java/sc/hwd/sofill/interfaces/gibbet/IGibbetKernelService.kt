package sc.hwd.sofill.interfaces.gibbet

import android.app.Notification
import android.os.Binder
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer

interface IGibbetKernelService {
    var builderGibbetKernel: NotificationCompat.Builder?
    val checkHttpServerWorkerName: String
    var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>?
    var serverPort: Int
    val localIPs: String
    var webView: WebView?
    var webViewVer: String?
    var userAgent: String?
    var kernelStarted: Boolean
    var stopKernelOnDestroy: Boolean
    fun showNotification(notification: Notification? = null)
    fun isHttpServerRunning(): Boolean

    /************* GoMobile 接口::开始 *************/
    fun goUpdateAssets()
    fun goReindexAssetContentOnce()
    fun goIncSyncOnce()
    fun goInsertBlockNext(paramsJSON: String)
    fun goIsHttpServing(): Boolean
    fun goGetNotebooks(flashcard: Boolean): INotebookListResponse
    fun goCreateDocWithMd(paramsJSON: String): ICreateDocWithMdResponse
    /************* GoMobile 接口::结束 *************/

    abstract class LocalBinder: Binder() {
        abstract fun getService(): IGibbetKernelService
    }

}