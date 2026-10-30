package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.ui.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean,
    serverUrl: String,
    errorMessage: String?,
    onFirebaseLogin: (email: String, pass: String) -> Unit,
    onFirebaseRegister: (email: String, pass: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onServerPinLogin: (pin: String) -> Unit,
    onUpdateServerUrl: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Firebase Auth, 1 = Server Master PIN
    var isRegisterMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var serverPin by remember { mutableStateOf("") }
    var serverPinVisible by remember { mutableStateOf(false) }

    var showServerDialog by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MidnightNavy,
                        Color(0xFF010C35)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Studio Avatar Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(RoyalBlue.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🧶", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "BabyyGurll Studio",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Artisanal Crochet • Admin Portal",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.75f)),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Auth Mode Selector Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFFF1F4FA),
                        contentColor = RoyalBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                localError = null
                            },
                            text = {
                                Text(
                                    "Firebase Auth",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = { Icon(Icons.Outlined.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                localError = null
                            },
                            text = {
                                Text(
                                    "Server PIN",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = { Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (selectedTab == 0) {
                        // FIREBASE AUTH MODE
                        Text(
                            text = if (isRegisterMode) "Create Admin Account" else "Independent Admin Login",
                            style = MaterialTheme.typography.titleMedium,
                            color = MidnightNavy,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (isRegisterMode)
                                "Register an administrator email with Firebase to access boutique management."
                            else
                                "Sign in securely with your Firebase administrator credentials.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                localError = null
                            },
                            label = { Text("Admin Email") },
                            placeholder = { Text("admin@babyygurll.in") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = RoyalBlue)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                localError = null
                            },
                            label = { Text("Password") },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = CharcoalGray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        onFirebaseLogin(email.trim(), password)
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isRegisterMode) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    localError = null
                                },
                                label = { Text("Confirm Password") },
                                placeholder = { Text("••••••••") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.LockReset, contentDescription = null, tint = RoyalBlue)
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (password != confirmPassword) {
                                            localError = "Passwords do not match"
                                        } else if (email.isNotBlank() && password.length >= 6) {
                                            onFirebaseRegister(email.trim(), password)
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Forgot password text button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { showForgotDialog = true },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "Forgot password?",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Display Errors
                        val activeError = localError ?: errorMessage
                        if (activeError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = CrimsonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrimsonAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    localError = "Please enter both email and password"
                                    return@Button
                                }
                                if (isRegisterMode) {
                                    if (password.length < 6) {
                                        localError = "Password must be at least 6 characters"
                                        return@Button
                                    }
                                    if (password != confirmPassword) {
                                        localError = "Passwords do not match"
                                        return@Button
                                    }
                                    onFirebaseRegister(email.trim(), password)
                                } else {
                                    onFirebaseLogin(email.trim(), password)
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isRegisterMode) "Register Administrator" else "Sign In with Firebase",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                localError = null
                            }
                        ) {
                            Text(
                                text = if (isRegisterMode) "Already have an account? Sign In" else "Need a new admin login? Register here",
                                style = MaterialTheme.typography.bodySmall,
                                color = MidnightNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }

                    } else {
                        // SERVER MASTER PIN MODE
                        Text(
                            text = "Backend Server PIN",
                            style = MaterialTheme.typography.titleMedium,
                            color = MidnightNavy,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Authenticate directly against your local or hosted Express backend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = serverPin,
                            onValueChange = {
                                serverPin = it
                                localError = null
                            },
                            label = { Text("Studio Master Password") },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = RoyalBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { serverPinVisible = !serverPinVisible }) {
                                    Icon(
                                        imageVector = if (serverPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = CharcoalGray
                                    )
                                }
                            },
                            visualTransformation = if (serverPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (serverPin.isNotBlank()) {
                                        onServerPinLogin(serverPin.trim())
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val activeError = localError ?: errorMessage
                        if (activeError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = CrimsonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrimsonAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (serverPin.isBlank()) {
                                    localError = "Please enter the studio password"
                                    return@Button
                                }
                                onServerPinLogin(serverPin.trim())
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Unlock Studio Portal",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Backend Server Settings Link
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showServerDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Server Endpoint",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "API: $serverUrl",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showServerDialog) {
        ServerConfigDialog(
            currentUrl = serverUrl,
            onDismiss = { showServerDialog = false },
            onSave = { newUrl ->
                onUpdateServerUrl(newUrl)
                showServerDialog = false
            }
        )
    }

    if (showForgotDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            icon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = RoyalBlue) },
            title = { Text("Reset Admin Password", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your administrator email. Firebase will send a secure password reset link to your inbox.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                            resetError = null
                        },
                        label = { Text("Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    resetError?.let {
                        Text(it, color = CrimsonAccent, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                            resetError = "Please enter a valid email"
                            return@Button
                        }
                        onForgotPassword(resetEmail.trim())
                        showForgotDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Send Reset Link", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel", color = CharcoalGray)
                }
            }
        )
    }
}
