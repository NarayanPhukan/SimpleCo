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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.BuildConfig
import com.babyygurll.store.ui.components.YarnBoutiqueBranding
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun AccountScreen(
    viewModel: StoreViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSavedAddresses: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(true) }

    var formError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            if (currentUser != null) {
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
            } else {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
                // Logged In: Dark Navy Profile Card
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
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onNavigateToEditProfile() }
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

                // YOUR BOUTIQUE section
                Text(
                    text = "YOUR BOUTIQUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BrandPrimary
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AccountMenuRow(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "My Orders",
                            subtitle = "Track orders & past invoices",
                            onClick = onNavigateToOrders
                        )
                        HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.6f), thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.FavoriteBorder,
                            title = "My Wishlist",
                            subtitle = "Pieces saved for later",
                            trailingBadge = if (wishlistIds.isNotEmpty()) "${wishlistIds.size}" else null,
                            onClick = onNavigateToWishlist
                        )
                        HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.6f), thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.LocationOn,
                            title = "Saved Addresses",
                            subtitle = "Manage delivery locations",
                            onClick = onNavigateToSavedAddresses
                        )
                        HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.6f), thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications & Alerts",
                            subtitle = "Order updates & artisan announcements",
                            trailingBadge = if (unreadNotifications > 0) "$unreadNotifications" else null,
                            onClick = onNavigateToNotifications
                        )
                        HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.6f), thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.Person,
                            title = "Edit Profile",
                            subtitle = "Personal details & security",
                            onClick = onNavigateToEditProfile
                        )
                    }
                }

                // HELP & SUPPORT
                Text(
                    text = "HELP & SUPPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BrandPrimary
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AccountMenuRow(
                            icon = Icons.Outlined.Chat,
                            title = "WhatsApp Concierge",
                            subtitle = "Live artisan support & queries",
                            onClick = {
                                val url = "https://wa.me/919999999999?text=${Uri.encode("Hello BabyyGurll team, I have a query about my order.")}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        )
                        HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.6f), thickness = 1.dp)
                        AccountMenuRow(
                            icon = Icons.Outlined.HelpOutline,
                            title = "Shipping & Returns Policy",
                            subtitle = "Artisanal pieces take 3-5 business days",
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://babyygurll.com/shipping-policy")))
                            }
                        )
                    }
                }

                // APP INFO & UPDATES CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                color = BrandLightBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Official APK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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

                // Log out Button
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
            } else {
                // NOT LOGGED IN: Exact match to Screenshot 21a42c9e (Login) and 4e6b2bd8 (Create Account)
                Spacer(modifier = Modifier.height(10.dp))

                // Yarn Ball Branding Header
                YarnBoutiqueBranding()

                Spacer(modifier = Modifier.height(16.dp))

                // Main Auth Card with white background
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Segmented Toggle Pill Container matching screenshot
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                // Login Tab
                                Surface(
                                    color = if (!isRegisterMode) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (!isRegisterMode) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                                    shadowElevation = if (!isRegisterMode) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isRegisterMode = false; formError = null }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Login",
                                            fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (!isRegisterMode) BrandPrimary else TextSecondary
                                        )
                                    }
                                }

                                // Create Account Tab
                                Surface(
                                    color = if (isRegisterMode) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (isRegisterMode) BorderStroke(1.5.dp, BrandDarkNavy) else null,
                                    shadowElevation = if (isRegisterMode) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isRegisterMode = true; formError = null }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Create Account",
                                            fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (isRegisterMode) BrandDarkNavy else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

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
                            // LOGIN FORM matching Screenshot
                            Text(
                                text = "Welcome back",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Sign in to continue your handmade journey.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Email or Mobile Number",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandDarkNavy
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it; formError = null },
                                placeholder = { Text("you@example.com", color = TextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Email, contentDescription = null, tint = TextMuted)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Password",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandDarkNavy
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it; formError = null },
                                placeholder = { Text("Minimum 8 characters", color = TextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextMuted)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Forgot Password Link right-aligned
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = onNavigateToForgotPassword,
                                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = BrandPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sign In Button
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
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                } else {
                                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // OR divider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                                Text(
                                    text = "  OR  ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Continue with OTP Outlined Button
                            OutlinedButton(
                                onClick = onNavigateToForgotPassword,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, BrandDarkNavy),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandDarkNavy),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Continue with OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Continue as Guest link
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = onNavigateToHome) {
                                    Text(
                                        text = "Continue as Guest ›",
                                        color = BrandPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            // CREATE ACCOUNT FORM matching Screenshot
                            Text(
                                text = "Join our little world",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Create an account for a more personal experience.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it; formError = null },
                                placeholder = { Text("Enter your full name", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it; formError = null },
                                placeholder = { Text("+91 00000 00000", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Email Address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it; formError = null },
                                placeholder = { Text("name@example.com", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it; formError = null },
                                placeholder = { Text("Minimum 8 characters", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextMuted) },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Confirm Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it; formError = null },
                                placeholder = { Text("Enter password again", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextMuted) },
                                trailingIcon = {
                                    IconButton(onClick = { regConfirmVisible = !regConfirmVisible }) {
                                        Icon(
                                            imageVector = if (regConfirmVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (regConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Terms Checkbox
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreeToTerms,
                                    onCheckedChange = { agreeToTerms = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "I agree to the ",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Terms & Conditions",
                                    fontSize = 12.sp,
                                    color = BrandPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://babyygurll.com/terms")))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                        formError = "Please fill in all required fields"
                                        return@Button
                                    }
                                    if (regPassword.length < 8) {
                                        formError = "Password must be at least 8 characters"
                                        return@Button
                                    }
                                    if (regPassword != regConfirmPassword) {
                                        formError = "Passwords do not match"
                                        return@Button
                                    }
                                    if (!agreeToTerms) {
                                        formError = "Please accept Terms & Conditions"
                                        return@Button
                                    }
                                    formError = null
                                    viewModel.register(
                                        name = regName,
                                        email = regEmail,
                                        pass = regPassword,
                                        phone = regPhone,
                                        onError = { formError = it }
                                    )
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                } else {
                                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: Privacy Policy • Terms of Service
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://babyygurll.com/privacy")))
                        }
                    )
                    Text("  •  ", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "Terms of Service",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://babyygurll.com/terms")))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
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
