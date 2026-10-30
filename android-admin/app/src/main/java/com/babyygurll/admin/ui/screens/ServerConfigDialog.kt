package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babyygurll.admin.ui.theme.MidnightNavy
import com.babyygurll.admin.ui.theme.RoyalBlue

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var urlText by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Backend Server URL",
                style = MaterialTheme.typography.titleLarge,
                color = MidnightNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configure the API address used by the app to connect to your 24/7 cloud backend (Render / Vercel) or local development server.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Server Base URL") },
                    placeholder = { Text("https://your-app.onrender.com/") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoyalBlue
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { urlText = "https://simpleco.onrender.com/" },
                        label = { Text("SimpleCo Live", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = { urlText = "http://10.0.2.2:5000/" },
                        label = { Text("Emulator", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = { urlText = "http://localhost:5000/" },
                        label = { Text("Localhost", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (urlText.isNotBlank()) {
                        onSave(urlText)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
