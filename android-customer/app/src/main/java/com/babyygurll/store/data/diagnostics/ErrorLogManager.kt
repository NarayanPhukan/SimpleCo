package com.babyygurll.store.data.diagnostics

import android.content.Context
import android.os.Build
import com.babyygurll.store.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

data class ClientLogEntry(
    val id: String,
    val timestamp: String,
    val severity: String,
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)

object ErrorLogManager {

    private const val MAX_LOGS = 60
    private const val PREFS_NAME = "babyygurll_diagnostics_prefs"
    private const val KEY_LAST_CRASH = "last_unhandled_crash"
    private const val KEY_PENDING_REPORT = "has_pending_user_report"

    private val logsDeque = ConcurrentLinkedDeque<ClientLogEntry>()
    private val _logsFlow = MutableStateFlow<List<ClientLogEntry>>(emptyList())
    val logsFlow: StateFlow<List<ClientLogEntry>> = _logsFlow.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        val stackTrace = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }

        val entry = ClientLogEntry(
            id = "err_${System.currentTimeMillis()}",
            timestamp = dateFormat.format(Date()),
            severity = "ERROR",
            tag = tag,
            message = message,
            stackTrace = stackTrace
        )
        addEntry(entry)
    }

    fun logWarning(tag: String, message: String) {
        val entry = ClientLogEntry(
            id = "warn_${System.currentTimeMillis()}",
            timestamp = dateFormat.format(Date()),
            severity = "WARNING",
            tag = tag,
            message = message
        )
        addEntry(entry)
    }

    fun logInfo(tag: String, message: String) {
        val entry = ClientLogEntry(
            id = "info_${System.currentTimeMillis()}",
            timestamp = dateFormat.format(Date()),
            severity = "INFO",
            tag = tag,
            message = message
        )
        addEntry(entry)
    }

    private fun addEntry(entry: ClientLogEntry) {
        logsDeque.addFirst(entry)
        while (logsDeque.size > MAX_LOGS) {
            logsDeque.removeLast()
        }
        _logsFlow.update { logsDeque.toList() }
    }

    fun recordFatalCrash(context: Context, threadName: String, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val report = buildString {
            appendLine("=== BABYYGURLL BOUTIQUE AUTO CRASH REPORT ===")
            appendLine("Timestamp: ${dateFormat.format(Date())}")
            appendLine("App Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Thread: $threadName")
            appendLine("Error: ${throwable.message ?: throwable.javaClass.simpleName}")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(stackTrace.take(1500))
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_CRASH, report)
            .putBoolean(KEY_PENDING_REPORT, true)
            .commit()
    }

    fun getPendingCrashReport(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasPending = prefs.getBoolean(KEY_PENDING_REPORT, false)
        return if (hasPending) prefs.getString(KEY_LAST_CRASH, null) else null
    }

    fun dismissPendingCrashReport(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PENDING_REPORT, false).apply()
    }

    fun generateDiagnosticsReport(): String {
        return buildString {
            appendLine("=== BABYYGURLL CUSTOMER APP DIAGNOSTICS ===")
            appendLine("Generated: ${dateFormat.format(Date())}")
            appendLine("App Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
            appendLine()
            appendLine("Recent Event Logs (${logsDeque.size}):")
            if (logsDeque.isEmpty()) {
                appendLine("No errors recorded. All systems operating smoothly.")
            } else {
                logsDeque.take(15).forEach { log ->
                    appendLine("[${log.timestamp}] [${log.severity}] ${log.tag}: ${log.message}")
                    if (!log.stackTrace.isNullOrBlank()) {
                        appendLine("Trace: ${log.stackTrace.lines().take(4).joinToString("\n  ")}")
                    }
                }
            }
        }
    }
}

