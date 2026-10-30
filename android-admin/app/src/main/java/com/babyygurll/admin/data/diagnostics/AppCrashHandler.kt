package com.babyygurll.admin.data.diagnostics

import android.content.Context
import android.util.Log

class AppCrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e("AppCrashHandler", "CRASH DETECTED in thread: ${thread.name}", throwable)
            ErrorLogManager.logError(
                tag = "FATAL_CRASH",
                message = "Uncaught exception in thread '${thread.name}': ${throwable.message ?: throwable.javaClass.simpleName}",
                throwable = throwable
            )
        } catch (t: Throwable) {
            Log.e("AppCrashHandler", "Failed to log crash", t)
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        fun install(context: Context) {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            if (current !is AppCrashHandler) {
                Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler(context, current))
            }
        }
    }
}

