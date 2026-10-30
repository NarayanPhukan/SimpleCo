package com.babyygurll.admin.data.diagnostics

import android.os.Build
import com.babyygurll.admin.data.model.ClientErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

object ErrorLogManager {

    private const val MAX_LOGS = 80
    private val logsDeque = ConcurrentLinkedDeque<ClientErrorLog>()

    private val _logsFlow = MutableStateFlow<List<ClientErrorLog>>(emptyList())
    val logsFlow: StateFlow<List<ClientErrorLog>> = _logsFlow.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        val stackTrace = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }

        val entry = ClientErrorLog(
            id = "err_${System.currentTimeMillis()}_${(100..999).random()}",
            timestamp = dateFormat.format(Date()),
            severity = "ERROR",
            tag = tag,
            message = message,
            stackTrace = stackTrace
        )

        addEntry(entry)
    }

    fun logWarning(tag: String, message: String) {
        val entry = ClientErrorLog(
            id = "warn_${System.currentTimeMillis()}_${(100..999).random()}",
            timestamp = dateFormat.format(Date()),
            severity = "WARNING",
            tag = tag,
            message = message
        )
        addEntry(entry)
    }

    fun logInfo(tag: String, message: String) {
        val entry = ClientErrorLog(
            id = "info_${System.currentTimeMillis()}_${(100..999).random()}",
            timestamp = dateFormat.format(Date()),
            severity = "INFO",
            tag = tag,
            message = message
        )
        addEntry(entry)
    }

    private fun addEntry(entry: ClientErrorLog) {
        logsDeque.addFirst(entry)
        while (logsDeque.size > MAX_LOGS) {
            logsDeque.removeLast()
        }
        _logsFlow.update { logsDeque.toList() }
    }

    fun clearLogs() {
        logsDeque.clear()
        _logsFlow.update { emptyList() }
    }

    fun generateFullDiagnosticsReport(serverUrl: String, isOnline: Boolean): String {
        val sb = StringBuilder()
        sb.appendLine("============================================")
        sb.appendLine("🧶 BABYYGURLL ADMIN - SYSTEM DIAGNOSTICS REPORT")
        sb.appendLine("Generated At: ${dateFormat.format(Date())}")
        sb.appendLine("============================================")
        sb.appendLine()
        sb.appendLine("--- DEVICE & APP TELEMETRY ---")
        sb.appendLine("App Version: 1.0.0 (Build 1)")
        sb.appendLine("Android OS: ${Build.VERSION.RELEASE} (API Level ${Build.VERSION.SDK_INT})")
        sb.appendLine("Device Model: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
        sb.appendLine("Target Backend: $serverUrl")
        sb.appendLine("Network Reachable: $isOnline")
        sb.appendLine()
        sb.appendLine("--- LOGGED EVENTS & ERRORS (${logsDeque.size}) ---")
        if (logsDeque.isEmpty()) {
            sb.appendLine("No errors detected. System operating normally.")
        } else {
            logsDeque.forEachIndexed { idx, log ->
                sb.appendLine("[#${idx + 1}] [${log.severity}] ${log.timestamp} | [${log.tag}] ${log.message}")
                if (!log.stackTrace.isNullOrBlank()) {
                    sb.appendLine("Stack Trace:")
                    sb.appendLine(log.stackTrace.lines().take(8).joinToString("\n") { "    $it" })
                }
                sb.appendLine("--------------------------------------------")
            }
        }
        sb.appendLine("============================================")
        return sb.toString()
    }
}

