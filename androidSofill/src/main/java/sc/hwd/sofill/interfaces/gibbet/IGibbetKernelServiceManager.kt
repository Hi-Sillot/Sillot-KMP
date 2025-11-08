package sc.hwd.sofill.interfaces.gibbet

import android.app.Activity

interface IGibbetKernelServiceManager {
    val service: IGibbetKernelService?
    fun bindService(action: IGibbetKernelServiceAction, webViewKey: String, activity: Activity)
    fun stopServiceByKillKernel()
}