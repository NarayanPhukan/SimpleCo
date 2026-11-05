package com.babyygurll.store.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.BuildConfig
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun AccountScreen(
    viewModel: StoreViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToWishlist: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf<String?>(null) }
    var showForgotPassword by remember { mutableStateOf(false) }

    if (showForgotPassword) {
        ForgotPasswordDialog(
            viewModel = viewModel,
            initialEmail = loginEmail,
            onDismiss = { showForgotPassword = false },
            onPasswordResetSuccess = {
                showForgotPassword = false
                formError = "Password reset successfully! Please sign in with your new password."
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = BrandDarkNavy
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BrandBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentUser != null) {
                // Logged In: Dark Navy Profile Card (Screenshot 4)
                val user = currentUser!!
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandDarkNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Blue Avatar with Initials
                        Surface(
                            color = BrandPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = user.name.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                    .ifBlank { "AS" }
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.email,
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MEMBER SINCE 2024",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFF93C5FD)
                            )
                        }

                        // Edit icon button
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Not Logged In: Sign In / Create Account Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isRegisterMode = false; formError = null },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isRegisterMode) BrandDarkNavy else Color(0xFFF1F5F9),
                                    contentColor = if (!isRegisterMode) Color.White else TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sign In", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { isRegisterMode = true; formError = null },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRegisterMode) BrandDarkNavy else Color(0xFFF1F5F9),
                                    contentColor = if (isRegisterMode) Color.White else TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Create Account", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (formError != null) {
                            Surface(
                                color = StatusError.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = formError!!,
                                    color = StatusError,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (!isRegisterMode) {
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it; formError = null },
                                label = { Text("Email Address") },
                                placeholder = { Text("your.email@example.com") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it; formError = null },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { showForgotPassword = true },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = BrandPrimary,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                        formError = "Please enter both email and password"
                                        return@Button
                                    }
                                    formError = null
                                    viewModel.login(loginEmail, loginPassword, onError = { formError = it })
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it; formError = null },
                                label = { Text("Full Name *") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it; formError = null },
                                label = { Text("Email Address *") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it; formError = null },
                                label = { Text("Create Password *") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Mobile Number (Optional)") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                        formError = "Please enter name, email and password"
                                        return@Button
                                    }
                                    formError = null
                                    viewModel.register(regName, regEmail, regPassword, regPhone, onError = { formError = it })
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: "YOUR BOUTIQUE" (Screenshot 4)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "YOUR BOUTIQUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AccountMenuRow(
                            icon = Icons.Outlined.Inventory2,
                            title = "My Orders",
                            subtitle = "Track, return or buy again",
                            onClick = onNavigateToOrders
                        )
                        HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.LocationOn,
                            title = "Saved Addresses",
                            subtitle = "Doorstep delivery addresses",
                            onClick = {}
                        )
                        HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.FavoriteBorder,
                            title = "Wishlist",
                            subtitle = "${wishlistIds.size} pieces saved for later",
                            onClick = onNavigateToWishlist
                        )
                        HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.ConfirmationNumber,
                            title = "Coupons",
                            subtitle = "3 offers waiting for you",
                            onClick = {}
                        )
                    }
                }
            }

            // Section 2: "HELP & SUPPORT" (Screenshot 4)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HELP & SUPPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AccountMenuRow(
                            icon = Icons.Outlined.HelpOutline,
                            title = "Customer Support",
                            subtitle = "We're happy to help",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:hello@babyygurll.in")
                                    putExtra(Intent.EXTRA_SUBJECT, "Customer Inquiry - BabyyGurll")
                                }
                                context.startActivity(intent)
                            }
                        )
                        HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.Chat,
                            title = "WhatsApp Contact",
                            subtitle = "Chat directly with our crafter",
                            trailingBadge = "ONLINE",
                            onClick = {
                                val uri = Uri.parse("https://wa.me/916002839514?text=Hi%20BabyyGurll!%20🌸%20I%20have%20a%20question%20about%20an%20order%20or%20piece.")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        )
                        HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.Security,
                            title = "Privacy Policy",
                            subtitle = "Your privacy matters to us",
                            onClick = {}
                        )
                    }
                }
            }

            // App Updates & Diagnostics Section
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BabyyGurll Boutique",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Official APK",
                                color = BrandPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.checkForUpdatesManual() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check Updates", fontSize = 12.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.triggerManualErrorReport() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.BugReport, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Diagnostics", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Log out Button (Screenshot 4)
            if (currentUser != null) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logout() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Log out",
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log out",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDarkNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AccountMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingBadge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDarkNavy
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (trailingBadge != null) {
            Surface(
                color = Color(0xFFE0EDFF),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = trailingBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
