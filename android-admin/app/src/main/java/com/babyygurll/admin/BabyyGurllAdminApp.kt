package com.babyygurll.admin

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.babyygurll.admin.data.diagnostics.AppCrashHandler
import com.babyygurll.admin.data.diagnostics.ErrorLogManager
import com.babyygurll.admin.util.DataUriFetcher

class BabyyGurllAdminApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Global Error Detection & Crash Handler
        AppCrashHandler.install(this)
        ErrorLogManager.logInfo("APPLICATION", "BabyyGurll Admin App initialized with DataUriFetcher")
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory())
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: BabyyGurllAdminApp
            private set
    }
}
