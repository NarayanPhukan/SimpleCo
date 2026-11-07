package com.babyygurll.store.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.babyygurll.store.data.diagnostics.ErrorLogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

object CustomerUpdateDownloader {

    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun startDownload(context: Context, rawUrl: String, versionName: String) {
        withContext(Dispatchers.IO) {
            try {
                _downloadState.value = UpdateDownloadState.Downloading(0, 0, 0)

                // High-speed direct repository endpoint (contains latest build with new logo)
                val targetUrl = if (rawUrl.isNotBlank() && !rawUrl.contains("onrender.com")) {
                    rawUrl.trim()
                } else {
                    "https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk"
                }

                val req = Request.Builder()
                    .url(targetUrl)
                    .header("Accept-Encoding", "identity")
                    .header("User-Agent", "BabyyGurllCustomerApp/v$versionName")
                    .build()

                val response = httpClient.newCall(req).execute()
                if (!response.isSuccessful) {
                    throw Exception("Update server returned HTTP ${response.code}")
                }

                val body = response.body ?: throw Exception("Empty response when downloading update")
                val totalLength = body.contentLength()

                val updatesDir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
                val apkFile = File(updatesDir, "BabyyGurll-Boutique-v$versionName.apk")
                if (apkFile.exists()) apkFile.delete()

                val inputStream = BufferedInputStream(body.byteStream(), 64 * 1024)
                val outputStream = BufferedOutputStream(FileOutputStream(apkFile), 64 * 1024)

                val buffer = ByteArray(64 * 1024) // 64 KB high-speed buffer
                var bytesCopied: Long = 0
                var bytesRead: Int
                var lastReportedPercent = -1
                var lastReportedTime = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesCopied += bytesRead
                    if (totalLength > 0) {
                        val progress = ((bytesCopied * 100) / totalLength).toInt()
                        val now = System.currentTimeMillis()
                        if (progress != lastReportedPercent && (now - lastReportedTime > 150 || progress == 100)) {
                            lastReportedPercent = progress
                            lastReportedTime = now
                            _downloadState.value = UpdateDownloadState.Downloading(progress, bytesCopied, totalLength)
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                response.close()

                _downloadState.value = UpdateDownloadState.ReadyToInstall(apkFile)

                withContext(Dispatchers.Main) {
                    installApk(context, apkFile)
                }
            } catch (e: Exception) {
                try {
                    val updatesDir = File(context.cacheDir, "apk_updates")
                    val apkFile = File(updatesDir, "BabyyGurll-Boutique-v$versionName.apk")
                    if (apkFile.exists()) apkFile.delete()
                } catch (_: Exception) {}
                ErrorLogManager.logError("AUTO_UPDATE", "Failed to download update", e)
                _downloadState.value = UpdateDownloadState.Error(e.message ?: "Failed to download update")
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                _downloadState.value = UpdateDownloadState.Error("APK file not found on device")
                return
            }

            // Verify package integrity before launching installer to prevent "Invalid package" error
            val packageInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (packageInfo == null) {
                apkFile.delete()
                _downloadState.value = UpdateDownloadState.Error("Downloaded package is incomplete or invalid. Please download again.")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
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
            ErrorLogManager.logError("APK_INSTALL", "Failed to launch package installer", e)
            _downloadState.value = UpdateDownloadState.Error("Could not launch installer: ${e.message}")
        }
    }

    fun resetState() {
        _downloadState.value = UpdateDownloadState.Idle
    }
}

