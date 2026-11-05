package com.babyygurll.store

import android.app.Application
import com.babyygurll.store.data.diagnostics.AppCrashHandler
import com.razorpay.Checkout

class BabyyGurllStoreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Install global fatal crash handler
        AppCrashHandler.install(this)
        // Preload Razorpay checkout framework
        Checkout.preload(applicationContext)
    }
}
