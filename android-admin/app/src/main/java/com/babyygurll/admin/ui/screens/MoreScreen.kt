package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.BuildConfig
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    state: AdminUiState,
    onNavigateSettings: () -> Unit,
    onNavigateCoupons: () -> Unit,
    onCheckUpdates: () -> Unit,
    onOpenServerConfig: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "More Options",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIvory)
            )
        },
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.settings.storeName.ifBlank { "BabyyGurll Studio" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = state.adminEmail ?: "Studio Administrator",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Storefront Connected",
                                fontSize = 11.sp,
                                color = Color(0xFF86EFAC)
                            )
                        }
                    }
                }
            }

            // Operations Group
            Text(
                text = "STORE MANAGEMENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Outlined.Tune,
                        title = "Studio Settings",
                        subtitle = "Boutique details, shipping rates & announcement banner",
                        onClick = onNavigateSettings
                    )
                    HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))
                    MoreMenuItem(
                        icon = Icons.Outlined.LocalOffer,
                        title = "Coupons & Discounts",
                        subtitle = "Manage promotional codes and special offers",
                        badge = "${state.coupons.size}",
                        onClick = onNavigateCoupons
                    )
                    HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))
                    MoreMenuItem(
                        icon = Icons.Outlined.Dns,
                        title = "Server Configuration",
                        subtitle = state.serverUrl.replace("https://", "").replace("http://", "").trimEnd('/'),
                        onClick = onOpenServerConfig
                    )
                }
            }

            // System & Updates Group
            Text(
                text = "SYSTEM & UPDATES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Outlined.SystemUpdate,
                        title = "Check for Updates",
                        subtitle = "Installed: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        badge = if (state.isUpdateAvailable) "NEW" else null,
                        badgeColor = RoyalBlue,
                        onClick = onCheckUpdates
                    )
                    HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))
                    MoreMenuItem(
                        icon = Icons.Outlined.HealthAndSafety,
                        title = "System Diagnostics",
                        subtitle = "MongoDB Atlas latency, Razorpay, Shiprocket & Firebase",
                        onClick = onNavigateSettings
                    )
                }
            }

            // Sign out button
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Sign Out of Studio Portal", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = Color(0xFFEEF4FF),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEEF4FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MidnightNavy
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalGray.copy(alpha = 0.7f),
                maxLines = 1
            )
        }

        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (badgeColor == RoyalBlue) RoyalBlue else Color(0xFFEEF4FF)
            ) {
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badgeColor == RoyalBlue) Color.White else RoyalBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
