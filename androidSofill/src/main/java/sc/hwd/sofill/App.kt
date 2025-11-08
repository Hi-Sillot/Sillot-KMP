package sc.hwd.sofill

import android.util.Log
import sc.hwd.sofill.android.SillotApplication

class SofillModuleApp : SillotApplication() {
    private val TAG = "SofillModuleApp"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }
}

object LibraryConfig {
    var versionName: String = ""
    var versionCode: Int = 0
    var BuildConfig_PROVIDER_AUTHORITIES = ""

    fun setup(versionName: String, versionCode: Int, BuildConfig_PROVIDER_AUTHORITIES: String) {
        this.versionName = versionName
        this.versionCode = versionCode
        this.BuildConfig_PROVIDER_AUTHORITIES = BuildConfig_PROVIDER_AUTHORITIES
    }
}