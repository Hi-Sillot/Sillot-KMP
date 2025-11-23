package sc.hwd.sillot.shared2.actual

import android.app.Application
import android.content.Context

object AndroidContextProvider {
    private lateinit var application: Application

    fun init(app: Application) {
        application = app
    }

    fun getContext(): Context = application
}
