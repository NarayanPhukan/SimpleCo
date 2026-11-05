package com.babyygurll.store

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.babyygurll.store.data.diagnostics.AppCrashHandler
import com.babyygurll.store.data.diagnostics.ErrorLogManager
import com.babyygurll.store.util.DataUriFetcher
import com.razorpay.Checkout

class BabyyGurllStoreApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        instance = this

        // Install global crash & error diagnostics
        AppCrashHandler.install(this)
        ErrorLogManager.logInfo("APPLICATION", "BabyyGurll Customer App initialized with Coil DataUriFetcher")

        // Preload Razorpay Checkout
        Checkout.preload(applicationContext)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: BabyyGurllStoreApp
            private set
    }
}
