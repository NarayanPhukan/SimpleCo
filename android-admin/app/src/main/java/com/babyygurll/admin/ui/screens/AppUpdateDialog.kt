package com.babyygurll.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.model.AppVersionInfo
import com.babyygurll.admin.ui.theme.*

@Composable
fun AppUpdateDialog(
    updateInfo: AppVersionInfo,
    isCritical: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            if (!isCritical) onDismiss()
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
                            text = "This update contains essential security and order fulfillment fixes. Updating is mandatory to continue managing the boutique.",
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
                        text = updateInfo.releaseNotes.ifBlank { "• Performance optimizations and stability improvements." },
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray,
                        modifier = Modifier.padding(12.dp)
                    )
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
            Button(
                onClick = {
                    val url = updateInfo.downloadUrl.ifBlank { "https://github.com/babyygurll/admin-releases/releases/latest" }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                    if (!isCritical) onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCritical) CrimsonAccent else RoyalBlue
                )
            ) {
                Text(
                    text = if (isCritical) "Update Required" else "Update Now",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            if (!isCritical) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = CharcoalGray)
                }
            }
        }
    )
}

