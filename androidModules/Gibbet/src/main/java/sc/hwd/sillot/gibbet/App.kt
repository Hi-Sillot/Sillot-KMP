package sc.hwd.sillot.gibbet

import android.util.Log
import sc.hwd.sofill.android.SillotApplication

class GibbetModuleApp : SillotApplication() {
    private val TAG = "GibbetModuleApp"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }

}