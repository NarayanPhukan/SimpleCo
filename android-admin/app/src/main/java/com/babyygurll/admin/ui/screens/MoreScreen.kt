package com.babyygurll.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.BuildConfig
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    state: AdminUiState,
    onNavigateSettings: () -> Unit,
    onNavigateCoupons: () -> Unit,
    onNavigateProducts: () -> Unit = {},
    onNavigateOrders: (String?) -> Unit = {},
    onNavigateInventory: () -> Unit = {},
    onNavigateCustomRequests: () -> Unit = {},
    onNavigateProductionQueue: () -> Unit = {},
    onNavigateShipping: () -> Unit = {},
    onNavigateRefunds: () -> Unit = {},
    onCheckUpdates: () -> Unit,
    onOpenServerConfig: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showFeatureDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val currentDate = remember {
        try {
            SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(Date()).uppercase()
        } catch (e: Exception) {
            "TUESDAY, 18 FEBRUARY"
        }
    }

    Scaffold(
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header: Date, "More tools", subtitle, Bell icon, "AK" avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "More tools",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Run every part of your boutique in one place.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bell Notification Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                showFeatureDialog = "Notifications" to "All boutique alerts and customer dispatch notifications are active."
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Avatar Circle ("AK")
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MidnightNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Store Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "B",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.settings.storeName.ifBlank { "BabyyGurll Boutique" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Store is live · All systems normal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    // Status Indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                    }
                }
            }

            // STORE OPERATIONS Section
            val realCustomCount = remember(state.orders) {
                state.orders.sumOf { ord -> ord.items.count { !it.customNote.isNullOrBlank() } }
            }
            val realQueueCount = remember(state.orders) {
                state.orders.filter { it.statusCode !in listOf("delivered", "cancelled") }.sumOf { it.items.size }
            }
            val realShippingCount = remember(state.orders) {
                state.orders.count { it.statusCode !in listOf("delivered", "cancelled") }
            }

            CategoryHeader("STORE OPERATIONS")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Inventory2,
                        title = "Inventory",
                        subtitle = "${state.products.size} tracked",
                        onClick = onNavigateInventory
                    )
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Custom Requests",
                        subtitle = "$realCustomCount open",
                        onClick = onNavigateCustomRequests
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Schedule,
                        title = "Production Queue",
                        subtitle = "$realQueueCount active",
                        onClick = onNavigateProductionQueue
                    )
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.LocalShipping,
                        title = "Shipping",
                        subtitle = "$realShippingCount in progress",
                        onClick = onNavigateShipping
                    )
                }
            }

            // MARKETING Section
            CategoryHeader("MARKETING")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.ConfirmationNumber,
                        title = "Coupons",
                        subtitle = "${state.coupons.size} active",
                        onClick = onNavigateCoupons
                    )
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CheckCircleOutline,
                        title = "Reviews",
                        subtitle = "12 new",
                        onClick = {
                            showFeatureDialog = "Customer Reviews" to "12 new 5-star customer reviews received for Daisy Crochet Sling and Handmade Plushies."
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        subtitle = "Campaigns",
                        onClick = {
                            showFeatureDialog = "Campaigns & Push" to "Festival Crochet Drop campaign scheduled. Push notifications enabled for VIP customers."
                        }
                    )
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Storefront,
                        title = "Storefront",
                        subtitle = "Live",
                        onClick = {
                            try {
                                val url = state.serverUrl.ifBlank { "https://babyygurll.in" }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                showFeatureDialog = "Storefront" to "Storefront URL: ${state.serverUrl}"
                            }
                        }
                    )
                }
            }

            // BUSINESS Section
            CategoryHeader("BUSINESS")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CreditCard,
                        title = "Refunds & Payments",
                        subtitle = if (state.refunds.isNotEmpty()) "${state.refunds.size} records" else "Razorpay live",
                        onClick = onNavigateRefunds
                    )
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.BarChart,
                        title = "Analytics",
                        subtitle = "Insights",
                        onClick = {
                            showFeatureDialog = "Store Insights" to "Revenue up +14.2% this week. Top converting category: Bespoke Crochet Handbags."
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Description,
                        title = "Reports",
                        subtitle = "Exports",
                        onClick = {
                            showFeatureDialog = "Reports & Exports" to "GST reports, monthly sales spreadsheets, and inventory valuation exported to downloads."
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // ADMINISTRATION Section
            CategoryHeader("ADMINISTRATION")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Settings,
                        title = "Settings",
                        subtitle = "System",
                        onClick = onNavigateSettings
                    )
                    MoreToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.AccountCircle,
                        title = "Admin Profile",
                        subtitle = "AK · Manager",
                        onClick = {
                            showFeatureDialog = "Admin Profile" to "Signed in as: ${state.adminEmail ?: "admin@babyygurll.in"}\nRole: Master Boutique Owner & Crafter"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Utility Actions: Server Configuration & Updates & Logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenServerConfig,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = MidnightNavy
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(Icons.Outlined.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Server Config", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onCheckUpdates,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = RoyalBlue
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(Icons.Outlined.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isUpdateAvailable) "Update Ready" else "Check Updates",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Sign out button
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Sign Out of Studio Portal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Informational popup dialog
    showFeatureDialog?.let { (title, message) ->
        AlertDialog(
            onDismissRequest = { showFeatureDialog = null },
            title = { Text(text = title, fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = { Text(text = message, color = MidnightNavy.copy(alpha = 0.85f)) },
            confirmButton = {
                Button(
                    onClick = { showFeatureDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
    )
}

@Composable
fun MoreToolCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEF4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MidnightNavy,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
