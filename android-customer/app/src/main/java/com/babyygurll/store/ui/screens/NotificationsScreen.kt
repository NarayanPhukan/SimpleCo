package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.StoreNotification
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun NotificationsScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    val isLoading by viewModel.isLoadingNotifications.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Orders" -> notifications.filter { it.type == "order" || it.type == "shipping" }
            "News" -> notifications.filter { it.type == "promotion" || it.type == "broadcast" || it.type == "general" }
            else -> notifications
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, SurfaceBorder)
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Boutique Alerts",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = BrandPrimary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$unreadCount new",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Order progress, dispatches & atelier updates",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.triggerTestNotification() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = "Test Notification",
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllNotificationsRead() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = "Mark all read",
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = BrandBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Orders", "News").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        "Orders" -> notifications.count { it.type == "order" || it.type == "shipping" }
                        "News" -> notifications.count { it.type == "promotion" || it.type == "broadcast" || it.type == "general" }
                        else -> notifications.size
                    }

                    Surface(
                        color = if (isSelected) BrandDarkNavy else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) BrandDarkNavy else SurfaceBorder
                        ),
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = "$filter ($count)",
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsNone,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Text(
                            text = "No alerts yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDarkNavy
                        )
                        Text(
                            text = "When you place orders or when our artisans announce limited-edition drops, you'll see updates right here.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.fetchNotifications() },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Text("Refresh Alerts", color = BrandDarkNavy, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { viewModel.triggerTestNotification() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandDarkNavy)
                            ) {
                                Text("Test Notification", color = Color.White, fontSize = 13.sp)
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
                        CustomerNotificationCard(
                            notification = notif,
                            onCardClick = {
                                if (!notif.isRead) viewModel.markNotificationRead(notif.id)
                                val orderId = notif.orderId
                                if (!orderId.isNullOrBlank()) {
                                    onNavigateToOrder(orderId)
                                }
                            },
                            onNavigateToOrder = { id -> onNavigateToOrder(id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerNotificationCard(
    notification: StoreNotification,
    onCardClick: () -> Unit,
    onNavigateToOrder: (String) -> Unit
) {
    val (typeBg, typeColor, typeIcon, typeTag) = when (notification.type) {
        "order" -> Quad(Color(0xFFF0FDF4), Color(0xFF16A34A), Icons.Outlined.ShoppingBag, "Order Update")
        "shipping" -> Quad(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Outlined.LocalShipping, "Dispatch")
        "promotion", "broadcast" -> Quad(Color(0xFFFDF2F8), Color(0xFFDB2777), Icons.Outlined.AutoAwesome, "Atelier News")
        else -> Quad(Color(0xFFF8FAFC), Color(0xFF475569), Icons.Outlined.Notifications, "Boutique Alert")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (notification.isRead) Color.White else Color(0xFFF8FAFC)),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) SurfaceBorder else BrandPrimary.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 1.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Pill
            Surface(
                color = typeBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
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
                    Text(
                        text = typeTag.uppercase(),
                        color = typeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.relativeTime,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy
                )

                Spacer(modifier = Modifier.height(3.dp))

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
                        modifier = Modifier.clickable { onNavigateToOrder(orderId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Track Order #$orderId",
                                color = BrandPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

