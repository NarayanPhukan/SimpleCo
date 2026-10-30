package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.babyygurll.admin.data.model.Order
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: AdminUiState,
    onRefresh: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateProducts: () -> Unit,
    onNavigateNewProduct: () -> Unit,
    onSelectOrder: (Order) -> Unit
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Dynamic stage counts calculated from live orders
    val receivedCount = remember(state.orders) { state.orders.count { it.statusCode == "received" } }
    val craftedCount = remember(state.orders) { state.orders.count { it.statusCode == "crafted" } }
    val packedCount = remember(state.orders) { state.orders.count { it.statusCode == "packed" } }
    val shippedCount = remember(state.orders) { state.orders.count { it.statusCode == "shipped" } }
    val deliveredCount = remember(state.orders) { state.orders.count { it.statusCode == "delivered" } }
    val outOfStockCount = remember(state.products) { state.products.count { !it.inStock } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Studio Overview",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (state.isSyncing) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Text(
                            text = "${state.settings.storeName} • Dynamic Cloud",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandSecondary
                )
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Cloud Connection Status Banner
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32))
                            )
                            Text(
                                text = "SimpleCo Cloud (Render)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandSecondary
                            )
                        }

                        Text(
                            text = if (state.isSyncing) "Syncing..." else "Live • ${state.lastSyncedTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isSyncing) RoyalBlue else CharcoalGray.copy(alpha = 0.6f),
                            fontWeight = if (state.isSyncing) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = RoyalBlue
                    )
                }
            }

            // Out of stock dynamic alert
            if (outOfStockCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateProducts() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CrimsonAccent.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$outOfStockCount item${if (outOfStockCount > 1) "s" else ""} sold out in catalog",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonAccent
                                )
                                Text(
                                    text = "Tap to review stock levels and replenish items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CharcoalGray
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CrimsonAccent)
                        }
                    }
                }
            }

            // Dynamic KPI Grid Row 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Revenue",
                        value = currencyFormat.format(state.metrics.totalRevenue),
                        icon = Icons.Default.CurrencyRupee,
                        iconColor = BrandPrimary,
                        bgColor = BrandPrimaryLight,
                        subtitle = "Gross boutique sales"
                    )
                    StatMetricCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateOrders() },
                        title = "Total Orders",
                        value = "${state.metrics.totalOrders}",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = BrandSecondary,
                        bgColor = BrandPrimaryLight,
                        subtitle = "Tap to view list"
                    )
                }
            }

            // Dynamic KPI Grid Row 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateOrders() },
                        title = "Pending Dispatch",
                        value = "${state.metrics.pendingFulfillmentCount}",
                        icon = Icons.Default.LocalShipping,
                        iconColor = if (state.metrics.pendingFulfillmentCount > 0) CrimsonAccent else Color(0xFF2E7D32),
                        bgColor = if (state.metrics.pendingFulfillmentCount > 0) CrimsonAccent.copy(alpha = 0.12f) else BrandPrimaryLight,
                        subtitle = if (state.metrics.pendingFulfillmentCount > 0) "Needs fulfillment" else "All orders dispatched"
                    )
                    StatMetricCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateProducts() },
                        title = "Active Pieces",
                        value = "${state.metrics.activeProductsCount} / ${state.products.size}",
                        icon = Icons.Default.Inventory2,
                        iconColor = BrandPrimary,
                        bgColor = BrandPrimaryLight,
                        subtitle = "In stock in catalog"
                    )
                }
            }

            // Dynamic Fulfillment Pipeline Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Fulfillment Pipeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            TextButton(onClick = onNavigateOrders) {
                                Text("Orders >", style = MaterialTheme.typography.labelSmall, color = RoyalBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PipelineStageBadge(
                                emoji = "📥",
                                label = "Received",
                                count = receivedCount,
                                isPending = receivedCount > 0,
                                onClick = onNavigateOrders
                            )
                            PipelineStageBadge(
                                emoji = "🪡",
                                label = "Crafting",
                                count = craftedCount,
                                isPending = craftedCount > 0,
                                onClick = onNavigateOrders
                            )
                            PipelineStageBadge(
                                emoji = "🎀",
                                label = "Packed",
                                count = packedCount,
                                isPending = packedCount > 0,
                                onClick = onNavigateOrders
                            )
                            PipelineStageBadge(
                                emoji = "🚚",
                                label = "Shipped",
                                count = shippedCount,
                                isPending = false,
                                onClick = onNavigateOrders
                            )
                            PipelineStageBadge(
                                emoji = "🎁",
                                label = "Delivered",
                                count = deliveredCount,
                                isPending = false,
                                onClick = onNavigateOrders
                            )
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Studio Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            QuickActionItem(
                                icon = Icons.Default.AddCircle,
                                label = "Add Piece",
                                onClick = onNavigateNewProduct
                            )
                            QuickActionItem(
                                icon = Icons.Default.Inventory2,
                                label = "Catalog",
                                onClick = onNavigateProducts
                            )
                            QuickActionItem(
                                icon = Icons.Default.LocalShipping,
                                label = "Fulfillment",
                                onClick = onNavigateOrders
                            )
                        }
                    }
                }
            }

            // Recent Orders Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Studio Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    TextButton(onClick = onNavigateOrders) {
                        Text("View All (${state.orders.size})")
                    }
                }
            }

            if (state.orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No orders recorded yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "As customer orders are placed in the boutique, they sync here in real time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            } else {
                items(state.orders.take(5)) { order ->
                    RecentOrderRow(
                        order = order,
                        onClick = { onSelectOrder(order) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PipelineStageBadge(
    emoji: String,
    label: String,
    count: Int,
    isPending: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isPending) CrimsonAccent.copy(alpha = 0.12f) else BrandPrimaryLight,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isPending) CrimsonAccent else MidnightNavy
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = CharcoalGray.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    subtitle: String = ""
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = CharcoalGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(BrandPrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = BrandPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
fun RecentOrderRow(
    order: Order,
    onClick: () -> Unit
) {
    val statusColor = when (order.statusCode) {
        "received" -> StatusReceived
        "crafted" -> StatusCrafted
        "packed" -> StatusPacked
        "shipped" -> StatusShipped
        "delivered" -> StatusDelivered
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.statusLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${order.shippingAddress?.recipientName ?: order.userEmail} • ${order.items.size} pcs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${order.total.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
                Text(
                    text = order.dateFormatted ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
