package com.babyygurll.admin.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.babyygurll.admin.data.diagnostics.ErrorLogManager
import com.babyygurll.admin.data.repository.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

object ApkUpdateDownloader {

    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    suspend fun startDownload(context: Context, rawUrl: String, versionName: String) {
        withContext(Dispatchers.IO) {
            try {
                _downloadState.value = UpdateDownloadState.Downloading(0, 0, 0)

                val prefs = PreferencesManager(context)
                var resolvedUrl = rawUrl.trim()
                if (resolvedUrl.isBlank() || resolvedUrl.contains("github.com/babyygurll")) {
                    resolvedUrl = "${prefs.serverUrl.trimEnd('/')}/api/settings/download-apk"
                }

                val request = Request.Builder()
                    .url(resolvedUrl)
                    .build()

                var response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    // Fallback to direct raw APK binary CDN
                    val fallbackUrl = "https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-admin/babyygurll-admin.apk"
                    response = httpClient.newCall(Request.Builder().url(fallbackUrl).build()).execute()
                }

                if (!response.isSuccessful) {
                    throw Exception("Server returned HTTP ${response.code} when fetching APK")
                }

                val body = response.body ?: throw Exception("Empty APK response from server")
                val totalLength = body.contentLength()

                val updatesDir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
                val apkFile = File(updatesDir, "BabyyGurll-Admin-v$versionName.apk")
                if (apkFile.exists()) apkFile.delete()

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(8 * 1024)
                var bytesCopied: Long = 0
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesCopied += bytesRead
                    if (totalLength > 0) {
                        val progress = ((bytesCopied * 100) / totalLength).toInt()
                        _downloadState.value = UpdateDownloadState.Downloading(progress, bytesCopied, totalLength)
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                _downloadState.value = UpdateDownloadState.ReadyToInstall(apkFile)

                // Prompt package installer on main thread
                withContext(Dispatchers.Main) {
                    installApk(context, apkFile)
                }

            } catch (e: Exception) {
                ErrorLogManager.logError("APK_DOWNLOAD", "Failed to download update: ${e.message}", e)
                _downloadState.value = UpdateDownloadState.Error(e.localizedMessage ?: "Failed to download update")
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            ErrorLogManager.logError("APK_INSTALL", "Failed to launch package installer: ${e.message}", e)
        }
    }

    fun resetState() {
        _downloadState.value = UpdateDownloadState.Idle
    }
}
