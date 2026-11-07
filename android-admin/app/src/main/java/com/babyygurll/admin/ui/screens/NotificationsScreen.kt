package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.model.AdminNotification
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: AdminUiState,
    onBack: () -> Unit,
    onViewOrder: (String) -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onSendBroadcast: (title: String, message: String, type: String) -> Unit,
    onRefresh: () -> Unit,
    onTestNotification: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    val filteredList = remember(state.notifications, selectedFilter) {
        when (selectedFilter) {
            "Orders" -> state.notifications.filter { it.type == "order" || it.type == "shipping" }
            "Low Stock" -> state.notifications.filter { it.type == "stock" }
            "Custom" -> state.notifications.filter { it.type == "custom_request" }
            "Broadcasts" -> state.notifications.filter { it.type == "broadcast" || it.type == "promotion" }
            else -> state.notifications
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MidnightNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notifications & Alerts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                            if (state.unreadNotificationCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = RoyalBlue,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${state.unreadNotificationCount} new",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Live boutique activity & customer dispatches",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // Mark all read button
                    if (state.unreadNotificationCount > 0) {
                        IconButton(
                            onClick = onMarkAllRead,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Outlined.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = RoyalBlue
                            )
                        }
                    }

                    // Test notification button
                    IconButton(
                        onClick = onTestNotification,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            contentDescription = "Test Notification",
                            tint = RoyalBlue
                        )
                    }

                    // Broadcast button
                    IconButton(
                        onClick = { showBroadcastDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Campaign,
                            contentDescription = "Send broadcast",
                            tint = RoyalBlue
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips Row
            val filters = listOf("All", "Orders", "Low Stock", "Custom", "Broadcasts")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        "Orders" -> state.notifications.count { it.type == "order" || it.type == "shipping" }
                        "Low Stock" -> state.notifications.count { it.type == "stock" }
                        "Custom" -> state.notifications.count { it.type == "custom_request" }
                        "Broadcasts" -> state.notifications.count { it.type == "broadcast" || it.type == "promotion" }
                        else -> state.notifications.size
                    }

                    Surface(
                        color = if (isSelected) MidnightNavy else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MidnightNavy else BrandBorder
                        ),
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = "$filter ($count)",
                            color = if (isSelected) Color.White else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Notification List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFFEEF2FF),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.NotificationsNone,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = "No notifications yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Text(
                            text = "All boutique orders, low-stock warnings, and announcements will appear here.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRefresh,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refresh Alerts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onTestNotification,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Alert", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id.ifBlank { it.hashCode().toString() } }) { notif ->
                        NotificationCard(
                            notification = notif,
                            onCardClick = {
                                if (!notif.isRead) onMarkRead(notif.id)
                                val orderId = notif.orderId
                                if (!orderId.isNullOrBlank()) {
                                    onViewOrder(orderId)
                                }
                            },
                            onDelete = { onDeleteNotification(notif.id) },
                            onViewOrder = { orderId -> onViewOrder(orderId) }
                        )
                    }
                }
            }
        }
    }

    // Broadcast Announcement Dialog
    if (showBroadcastDialog) {
        var broadcastTitle by remember { mutableStateOf("") }
        var broadcastMessage by remember { mutableStateOf("") }
        var broadcastType by remember { mutableStateOf("promotion") }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Campaign, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Broadcast Alert",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Send an in-app announcement to all customer apps.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. 🌸 Festive Crochet Drop Now Live!") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = broadcastMessage,
                        onValueChange = { broadcastMessage = it },
                        label = { Text("Message") },
                        placeholder = { Text("Use code FESTIVE15 for 15% off milk cotton bouquets & bags.") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Notification Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MidnightNavy
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("promotion" to "Promo", "broadcast" to "Update", "system" to "Alert").forEach { (typeKey, label) ->
                            val isSel = broadcastType == typeKey
                            Surface(
                                color = if (isSel) RoyalBlue else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { broadcastType = typeKey }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else MidnightNavy,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank()) {
                            onSendBroadcast(broadcastTitle.trim(), broadcastMessage.trim(), broadcastType)
                            showBroadcastDialog = false
                        }
                    },
                    enabled = broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank() && !state.isSendingBroadcast,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    if (state.isSendingBroadcast) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Broadcast", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun NotificationCard(
    notification: AdminNotification,
    onCardClick: () -> Unit,
    onDelete: () -> Unit,
    onViewOrder: (String) -> Unit
) {
    val (typeBg, typeColor, typeIcon, typeLabel) = when (notification.type) {
        "order" -> Quad(Color(0xFFECFDF5), Color(0xFF059669), Icons.Outlined.ShoppingBag, "Order")
        "shipping" -> Quad(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Outlined.LocalShipping, "Dispatch")
        "stock" -> Quad(Color(0xFFFFFBEB), Color(0xFFD97706), Icons.Outlined.Warning, "Stock Alert")
        "custom_request" -> Quad(Color(0xFFFAF5FF), Color(0xFF9333EA), Icons.Outlined.Brush, "Custom Design")
        "broadcast", "promotion" -> Quad(Color(0xFFFDF2F8), Color(0xFFDB2777), Icons.Outlined.Campaign, "Broadcast")
        else -> Quad(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Outlined.Notifications, "System")
    }

    Surface(
        color = if (notification.isRead) Color.White else Color(0xFFF0FDF4),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (notification.isRead) BrandBorder else Color(0xFF86EFAC)
        ),
        shadowElevation = if (notification.isRead) 1.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon emblem
            Surface(
                color = typeBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = typeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = typeLabel.uppercase(),
                            color = typeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.relativeTime,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(RoyalBlue)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )

                val orderId = notification.orderId
                if (!orderId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onViewOrder(orderId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "View Order #$orderId",
                                color = RoyalBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Delete",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

