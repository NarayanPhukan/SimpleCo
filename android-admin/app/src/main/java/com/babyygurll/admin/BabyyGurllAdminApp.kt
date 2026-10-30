package com.babyygurll.admin

import android.app.Application
import com.babyygurll.admin.data.diagnostics.AppCrashHandler
import com.babyygurll.admin.data.diagnostics.ErrorLogManager

class BabyyGurllAdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Global Error Detection & Crash Handler
        AppCrashHandler.install(this)
        ErrorLogManager.logInfo("APPLICATION", "BabyyGurll Admin App v1.0.0 initialized")
    }

    companion object {
        lateinit var instance: BabyyGurllAdminApp
            private set
    }
}
