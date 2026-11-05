package com.babyygurll.admin.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.diagnostics.ErrorLogManager
import com.babyygurll.admin.data.model.StoreSettings
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showServerDialog by remember { mutableStateOf(false) }
    var showLogDetails by remember { mutableStateOf(false) }

    // Local form state
    var storeName by remember { mutableStateOf(uiState.settings.storeName) }
    var tagline by remember { mutableStateOf(uiState.settings.tagline) }
    var announcementText by remember { mutableStateOf(uiState.settings.announcementText) }
    var announcementBadge by remember { mutableStateOf(uiState.settings.announcementBadge) }
    var announcementActive by remember { mutableStateOf(uiState.settings.announcementActive) }
    var freeShippingThreshold by remember { mutableStateOf(uiState.settings.freeShippingThreshold.toString()) }
    var standardShippingFee by remember { mutableStateOf(uiState.settings.standardShippingFee.toString()) }
    var expressShippingFee by remember { mutableStateOf(uiState.settings.expressShippingFee.toString()) }
    var supportEmail by remember { mutableStateOf(uiState.settings.supportEmail) }
    var supportPhone by remember { mutableStateOf(uiState.settings.supportPhone) }
    var studioAddress by remember { mutableStateOf(uiState.settings.studioAddress) }
    var adminRazorpayAccountId by remember { mutableStateOf(uiState.settings.adminRazorpayAccountId) }

    // Test pincode state
    var testPincode by remember { mutableStateOf("") }

    // Synchronize local form when backend state is loaded
    LaunchedEffect(uiState.settings) {
        storeName = uiState.settings.storeName
        tagline = uiState.settings.tagline
        announcementText = uiState.settings.announcementText
        announcementBadge = uiState.settings.announcementBadge
        announcementActive = uiState.settings.announcementActive
        freeShippingThreshold = uiState.settings.freeShippingThreshold.toInt().toString()
        standardShippingFee = uiState.settings.standardShippingFee.toInt().toString()
        expressShippingFee = uiState.settings.expressShippingFee.toInt().toString()
        supportEmail = uiState.settings.supportEmail
        supportPhone = uiState.settings.supportPhone
        studioAddress = uiState.settings.studioAddress
        adminRazorpayAccountId = uiState.settings.adminRazorpayAccountId
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftIvory,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Studio Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Text(
                            text = "Boutique operations, diagnostics & updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload settings & diagnostics",
                            tint = MidnightNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Auto-Update Detection & Versioning
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.SystemUpdate, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "App Version & Auto-Update",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.isUpdateAvailable) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = if (uiState.isUpdateAvailable) "Update Available" else "Latest Release",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isUpdateAvailable) CrimsonAccent else Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Current Installed Version: v${com.babyygurll.admin.BuildConfig.VERSION_NAME} (Build ${com.babyygurll.admin.BuildConfig.VERSION_CODE}). The app automatically scans the server release manifest for updates on launch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray.copy(alpha = 0.7f)
                    )

                    Button(
                        onClick = { viewModel.checkForAppUpdates(silent = false) },
                        enabled = !uiState.isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isCheckingUpdate) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking Release Manifest...", color = Color.White)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Updates Now", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Section 2: Error Detection & Live System Diagnostics
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.MonitorHeart, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "System Diagnostics & Error Monitor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        IconButton(onClick = { viewModel.fetchDiagnostics() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh diagnostics", tint = RoyalBlue)
                        }
                    }

                    Text(
                        text = "Real-time health monitor covering database connectivity, third-party payment & courier APIs, and recorded exception logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray.copy(alpha = 0.7f)
                    )

                    // Diagnostics Badges Grid
                    val diag = uiState.diagnostics
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Database Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Storage, contentDescription = null, modifier = Modifier.size(16.dp), tint = CharcoalGray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MongoDB Store", style = MaterialTheme.typography.bodyMedium, color = MidnightNavy)
                            }
                            val dbOk = diag?.database?.status == "connected"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (dbOk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = if (dbOk) "Connected (${diag?.database?.latencyMs ?: 0}ms)" else (diag?.database?.status ?: "Checking..."),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dbOk) Color(0xFF2E7D32) else CrimsonAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Razorpay Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Payment, contentDescription = null, modifier = Modifier.size(16.dp), tint = CharcoalGray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Razorpay Payments", style = MaterialTheme.typography.bodyMedium, color = MidnightNavy)
                            }
                            val rzpConfigured = diag?.services?.razorpay?.configured == true
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (rzpConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = if (rzpConfigured) "Configured (${diag?.services?.razorpay?.mode?.uppercase()})" else "Not configured",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rzpConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Shiprocket Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp), tint = CharcoalGray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shiprocket Logistics", style = MaterialTheme.typography.bodyMedium, color = MidnightNavy)
                            }
                            val srConfigured = diag?.services?.shiprocket?.configured == true
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (srConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = if (srConfigured) "Connected" else "Not configured",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (srConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Firebase Auth Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp), tint = CharcoalGray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Firebase Identity", style = MaterialTheme.typography.bodyMedium, color = MidnightNavy)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "babygurl-crochet-dev",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFFF1F3F9))

                    // Error Logs Summary & Action Row
                    val totalErrors = uiState.clientErrors.size + (diag?.recentErrors?.size ?: 0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (totalErrors == 0) "No errors detected ✓" else "$totalErrors event logs captured",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (totalErrors == 0) Color(0xFF2E7D32) else CrimsonAccent
                        )

                        TextButton(onClick = { showLogDetails = !showLogDetails }) {
                            Text(if (showLogDetails) "Hide Logs" else "View Logs")
                        }
                    }

                    // Expandable Logs Viewer
                    if (showLogDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FD), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.clientErrors.isEmpty() && (diag?.recentErrors?.isEmpty() == true)) {
                                Text(
                                    text = "All systems functioning with zero errors.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalGray
                                )
                            } else {
                                uiState.clientErrors.take(10).forEach { err ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "[${err.severity}] ${err.tag}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (err.severity == "ERROR") CrimsonAccent else RoyalBlue
                                            )
                                            Text(
                                                text = err.timestamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CharcoalGray.copy(alpha = 0.5f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = err.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MidnightNavy,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Diagnostics Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val report = ErrorLogManager.generateFullDiagnosticsReport(
                                    serverUrl = uiState.serverUrl,
                                    isOnline = diag?.serverStatus == "healthy"
                                )
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("BabyyGurll Diagnostics", report)
                                clipboard.setPrimaryClip(clip)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Report", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearAllLogs() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Logs", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Section 3: Store Announcement Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Campaign, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Announcement Bar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        Switch(
                            checked = announcementActive,
                            onCheckedChange = { announcementActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RoyalBlue
                            )
                        )
                    }

                    Text(
                        text = "Displayed at the very top of the customer website. Used for site-wide discount codes and free delivery announcements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = { announcementText = it },
                        label = { Text("Announcement Headline") },
                        placeholder = { Text("e.g. Free delivery across India on orders over ₹999") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = announcementBadge,
                        onValueChange = { announcementBadge = it },
                        label = { Text("Call-To-Action Badge") },
                        placeholder = { Text("e.g. CODE: BABYYGURLL FOR 15% OFF") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 4: Store Identity
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Brand Identity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline / Subtitle") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 5: Shipping & Thresholds
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shipping Fees & Free Delivery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }

                    OutlinedTextField(
                        value = freeShippingThreshold,
                        onValueChange = { freeShippingThreshold = it },
                        label = { Text("Free Delivery Qualifying Total (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = standardShippingFee,
                            onValueChange = { standardShippingFee = it },
                            label = { Text("Standard Fee (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expressShippingFee,
                            onValueChange = { expressShippingFee = it },
                            label = { Text("Express Fee (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 6: Live Pincode Serviceability Tester
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PinDrop, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Postal Code Serviceability Tool",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }

                    Text(
                        text = "Verify whether Shiprocket / courier partners can deliver to a destination postal code in India.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testPincode,
                            onValueChange = { if (it.length <= 6) testPincode = it },
                            label = { Text("6-Digit PIN") },
                            placeholder = { Text("e.g. 560001") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (testPincode.isNotBlank()) {
                                    viewModel.checkPincode(testPincode.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Check", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    uiState.pinServiceabilityResult?.let { res ->
                        val isOk = res.serviceable == true
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isOk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isOk) Color(0xFF2E7D32) else CrimsonAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res.message ?: (if (isOk) "PIN ${res.deliveryPostcode} is serviceable" else "Unserviceable location"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isOk) Color(0xFF2E7D32) else CrimsonAccent
                                )
                            }
                        }
                    }
                }
            }

            // Section 7: Support & Contact Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ContactSupport, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customer Support",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }

                    OutlinedTextField(
                        value = supportEmail,
                        onValueChange = { supportEmail = it },
                        label = { Text("Support Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supportPhone,
                        onValueChange = { supportPhone = it },
                        label = { Text("Phone / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = studioAddress,
                        onValueChange = { studioAddress = it },
                        label = { Text("Studio Physical Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 8: Automated Admin Payouts (Razorpay Route)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Automated Admin Payouts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (adminRazorpayAccountId.trim().startsWith("acc_")) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = if (adminRazorpayAccountId.trim().startsWith("acc_")) "✓ Auto-Route Active" else "Not Linked",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (adminRazorpayAccountId.trim().startsWith("acc_")) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "With Razorpay Route, 100% of customer order money automatically routes directly to the Admin's bank account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = adminRazorpayAccountId,
                        onValueChange = { adminRazorpayAccountId = it },
                        label = { Text("Admin Linked Account ID (acc_...)") },
                        placeholder = { Text("e.g. acc_N1234567890abc") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Setup Guide: Open Razorpay Dashboard → Route → Linked Accounts → Add Account with the Admin's Bank Account Number & IFSC. Copy the generated 'acc_...' ID and paste it here.",
                                fontSize = 11.sp,
                                color = MidnightNavy,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    val updated = StoreSettings(
                        storeName = storeName.trim(),
                        tagline = tagline.trim(),
                        announcementText = announcementText.trim(),
                        announcementBadge = announcementBadge.trim(),
                        announcementActive = announcementActive,
                        freeShippingThreshold = freeShippingThreshold.toDoubleOrNull() ?: 999.0,
                        standardShippingFee = standardShippingFee.toDoubleOrNull() ?: 79.0,
                        expressShippingFee = expressShippingFee.toDoubleOrNull() ?: 149.0,
                        supportEmail = supportEmail.trim(),
                        supportPhone = supportPhone.trim(),
                        studioAddress = studioAddress.trim(),
                        adminRazorpayAccountId = adminRazorpayAccountId.trim()
                    )
                    viewModel.saveSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save All Changes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Backend Connection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Server Connection & Endpoint",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )

                    Text(
                        text = "Access Mode: Direct Admin Access (No Login Required)",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalBlue,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Active Endpoint: ${uiState.serverUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalGray.copy(alpha = 0.7f)
                    )

                    Button(
                        onClick = { showServerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch Backend Server URL", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showServerDialog) {
        ServerConfigDialog(
            currentUrl = uiState.serverUrl,
            onDismiss = { showServerDialog = false },
            onSave = { newUrl ->
                viewModel.updateServerUrl(newUrl)
                showServerDialog = false
            }
        )
    }
}
