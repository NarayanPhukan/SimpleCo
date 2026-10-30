package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.model.AppVersionInfo
import com.babyygurll.admin.data.update.ApkUpdateDownloader
import com.babyygurll.admin.data.update.UpdateDownloadState
import com.babyygurll.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppUpdateDialog(
    updateInfo: AppVersionInfo,
    isCritical: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val downloadState by ApkUpdateDownloader.downloadState.collectAsState()

    AlertDialog(
        onDismissRequest = {
            if (!isCritical && downloadState !is UpdateDownloadState.Downloading) {
                ApkUpdateDownloader.resetState()
                onDismiss()
            }
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCritical) CrimsonAccent.copy(alpha = 0.12f) else RoyalBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = if (isCritical) CrimsonAccent else RoyalBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isCritical) "Critical Update Required" else "Update Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Text(
                    text = "v1.0.0 → v${updateInfo.latestVersionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RoyalBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isCritical) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "This update contains essential boutique fixes. Updating is required to continue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonAccent,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    text = "What's New in v${updateInfo.latestVersionName}:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF6F8FC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = updateInfo.releaseNotes.ifBlank { "• Performance optimizations and cloud sync improvements." },
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                when (val state = downloadState) {
                    is UpdateDownloadState.Downloading -> {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Downloading APK package...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${state.progressPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (state.progressPercent > 0) {
                                LinearProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = RoyalBlue
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = RoyalBlue
                                )
                            }
                        }
                    }
                    is UpdateDownloadState.ReadyToInstall -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ APK Download complete. System installer launching...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    is UpdateDownloadState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Download failed: ${state.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrimsonAccent,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    else -> {}
                }

                if (updateInfo.releaseDate.isNotBlank()) {
                    Text(
                        text = "Released: ${updateInfo.releaseDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalGray.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            when (val state = downloadState) {
                is UpdateDownloadState.Downloading -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Downloading (${state.progressPercent}%)...")
                    }
                }
                is UpdateDownloadState.ReadyToInstall -> {
                    Button(
                        onClick = {
                            ApkUpdateDownloader.installApk(context, state.apkFile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Install Update", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                ApkUpdateDownloader.startDownload(
                                    context = context,
                                    rawUrl = updateInfo.downloadUrl,
                                    versionName = updateInfo.latestVersionName
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCritical) CrimsonAccent else RoyalBlue
                        )
                    ) {
                        Text(
                            text = if (isCritical) "Download & Install" else "Update Now",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (!isCritical && downloadState !is UpdateDownloadState.Downloading) {
                TextButton(
                    onClick = {
                        ApkUpdateDownloader.resetState()
                        onDismiss()
                    }
                ) {
                    Text("Later", color = CharcoalGray)
                }
            }
        }
    )
}
