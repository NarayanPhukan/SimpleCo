package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Order
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import com.babyygurll.admin.util.AdminProductImage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: AdminUiState,
    onRefresh: () -> Unit,
    onNavigateOrders: (String?) -> Unit,
    onNavigateProducts: () -> Unit,
    onNavigateNewProduct: () -> Unit,
    onNavigateCoupons: () -> Unit,
    onSelectOrder: (Order) -> Unit,
    onSelectPeriod: (String) -> Unit,
    onOpenNotifications: () -> Unit = {}
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Dynamic greeting & date
    val calendar = remember { Calendar.getInstance() }
    val formattedDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(Date()).uppercase()
    }
    val greeting = remember {
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            else -> "Good evening!"
        }
    }

    // Production flow stage counts
    val pendingPaymentCount = remember(state.orders) {
        state.orders.count { it.paymentStatus == "pending" || it.statusCode == "pending" }
    }
    val preparingCount = remember(state.orders) {
        state.orders.count { it.statusCode in listOf("received", "preparing", "confirmed") }
    }
    val beingHandmadeCount = remember(state.orders) {
        state.orders.count { it.statusCode in listOf("crafted", "handmade") }
    }
    val readyToShipCount = remember(state.orders) {
        state.orders.count { it.statusCode in listOf("packed", "ready") }
    }
    val shippedCount = remember(state.orders) {
        state.orders.count { it.statusCode == "shipped" }
    }
    val lowStockCount = remember(state.products) {
        state.products.count { it.inStock && it.stockCount in 1..3 }
    }
    val delayedCount = remember(state.orders) {
        state.orders.count { it.statusCode in listOf("received", "pending") }
    }
    val failedPayCount = remember(state.orders) {
        state.orders.count { it.paymentStatus == "failed" }
    }
    val customRequestsCount = remember(state.orders) {
        state.orders.count { it.items.any { item -> !item.customNote.isNullOrBlank() } }
    }
    val totalAttention = lowStockCount + delayedCount + failedPayCount + customRequestsCount

    Scaffold(
        containerColor = SoftIvory
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Header Row: Date, Greeting, Bell & Avatar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Here's what's happening with your store.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalGray.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bell Icon in rounded square with notification dot
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                .clickable { onOpenNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MidnightNavy,
                                modifier = Modifier.size(22.dp)
                            )
                            // Notification dot if unread alerts exist
                            if (state.unreadNotificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(7.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(RoyalBlue)
                                )
                            }
                        }

                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MidnightNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // 2x2 Metric Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: Today's Revenue (Navy) & Total Orders (White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Today's Revenue (Dark Navy)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MidnightNavy)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Today's Revenue",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = currencyFormat.format(state.metrics.todayRevenue),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${state.metrics.todayOrdersCount} today",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF60A5FA)
                                    )
                                }
                            }
                        }

                        // Total Orders (White Card)
                        MetricWhiteCard(
                            title = "Total Orders",
                            icon = Icons.Outlined.ReceiptLong,
                            value = "${state.metrics.totalOrders}",
                            subtext = "${state.metrics.todayOrdersCount} today",
                            subtextColor = RoyalBlue,
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp),
                            onClick = { onNavigateOrders(null) }
                        )
                    }

                    // Row 2: Pending Orders & Total Customers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricWhiteCard(
                            title = "Pending Orders",
                            icon = Icons.Outlined.Schedule,
                            value = "${state.metrics.pendingFulfillmentCount}",
                            subtext = "Needs action",
                            subtextColor = CharcoalGray,
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp),
                            onClick = { onNavigateOrders("pending") }
                        )

                        MetricWhiteCard(
                            title = "Total Customers",
                            icon = Icons.Outlined.Person,
                            value = NumberFormat.getNumberInstance(Locale.US).format(state.metrics.totalCustomersCount),
                            subtext = "${state.metrics.totalCustomersCount} registered",
                            subtextColor = RoyalBlue,
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp),
                            onClick = { /* Navigate or filter */ }
                        )
                    }
                }
            }

            // Revenue Analytics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REVENUE ANALYTICS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currencyFormat.format(state.metrics.periodRevenue),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${state.metrics.revenueGrowthPercent}% vs previous period",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoyalBlue
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEEF4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.BarChart,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Segmented Period Filter: 7 Days | 30 Days | 12 Months
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("7 Days", "30 Days", "12 Months").forEach { period ->
                                    val isSelected = state.selectedPeriod == period
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectPeriod(period) }
                                    ) {
                                        Text(
                                            text = period,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MidnightNavy else CharcoalGray,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        var selectedChartIndex by remember(state.metrics.chartDays) {
                            val nonZeroIdx = state.metrics.chartDays.indexOfLast { it.second > 0 }
                            mutableIntStateOf(if (nonZeroIdx >= 0) nonZeroIdx else (state.metrics.chartDays.size - 1).coerceAtLeast(0))
                        }
                        val activePoint = state.metrics.chartDays.getOrNull(selectedChartIndex)

                        // Smooth Curved Bezier Chart with Live Dynamic Tooltip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                        ) {
                            RevenueCurveChart(
                                days = state.metrics.chartDays,
                                selectedIndex = selectedChartIndex,
                                onSelectIndex = { selectedChartIndex = it },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Dynamic Live Tooltip for Selected / Active Day
                            if (activePoint != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MidnightNavy)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${activePoint.first}  ${currencyFormat.format(activePoint.second)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Add Product",
                        onClick = onNavigateNewProduct
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.ReceiptLong,
                        label = "Manage Orders",
                        onClick = { onNavigateOrders(null) }
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.LocalOffer,
                        label = "Add Coupon",
                        onClick = onNavigateCoupons
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Inventory2,
                        label = "View Inventory",
                        onClick = onNavigateProducts
                    )
                }
            }

            // Production Flow (ORDER OVERVIEW)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ORDER OVERVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Production flow",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy
                                )
                            }
                            TextButton(onClick = { onNavigateOrders(null) }) {
                                Text(
                                    text = "View all >",
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProductionFlowItem(
                            label = "Pending payment",
                            subtext = "orders",
                            count = pendingPaymentCount,
                            color = Color(0xFF93C5FD),
                            onClick = { onNavigateOrders("pending") }
                        )
                        HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f))
                        ProductionFlowItem(
                            label = "Preparing",
                            subtext = "orders",
                            count = preparingCount,
                            color = RoyalBlue,
                            onClick = { onNavigateOrders("preparing") }
                        )
                        HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f))
                        ProductionFlowItem(
                            label = "Being handmade",
                            subtext = "orders",
                            count = beingHandmadeCount,
                            color = MidnightNavy,
                            onClick = { onNavigateOrders("handmade") }
                        )
                        HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f))
                        ProductionFlowItem(
                            label = "Ready to ship",
                            subtext = "orders",
                            count = readyToShipCount,
                            color = RoyalBlue,
                            onClick = { onNavigateOrders("ready") }
                        )
                        HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f))
                        ProductionFlowItem(
                            label = "Shipped",
                            subtext = "orders",
                            count = shippedCount,
                            color = MidnightNavy,
                            onClick = { onNavigateOrders("shipped") }
                        )
                    }
                }
            }

            // Attention Required Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Attention required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEEF4FF)
                        ) {
                            Text(
                                text = "$totalAttention",
                                color = RoyalBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Refresh",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.clickable { onRefresh() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AttentionCard(
                            icon = Icons.Outlined.Archive,
                            title = "Low-stock products",
                            desc = "$lowStockCount products below threshold",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateProducts
                        )
                        AttentionCard(
                            icon = Icons.Outlined.Schedule,
                            title = "Orders awaiting action",
                            desc = "$delayedCount orders need action",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateOrders("pending") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AttentionCard(
                            icon = Icons.Outlined.CreditCard,
                            title = "Pending payments",
                            desc = "$failedPayCount payments to review",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateOrders("pending") }
                        )
                        AttentionCard(
                            icon = Icons.Outlined.AutoAwesome,
                            title = "Custom requests",
                            desc = "$customRequestsCount custom inquiries",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateOrders("handmade") }
                        )
                    }
                }
            }

            // Recent Orders Header & Preview List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                    TextButton(onClick = { onNavigateOrders(null) }) {
                        Text(
                            text = "View all >",
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Recent Order Cards
            val recentList = if (state.orders.isNotEmpty()) state.orders.take(4) else emptyList()
            if (recentList.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No orders placed yet",
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Live orders from your storefront will appear here automatically.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(recentList) { order ->
                    RecentOrderRowCard(
                        order = order,
                        serverUrl = state.serverUrl,
                        currencyFormat = currencyFormat,
                        onClick = { onSelectOrder(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricWhiteCard(
    title: String,
    icon: ImageVector,
    value: String,
    subtext: String,
    subtextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalGray
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEF4FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subtextColor
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .height(95.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MidnightNavy,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProductionFlowItem(
    label: String,
    subtext: String,
    count: Int,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MidnightNavy
            )
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Text(
            text = "$count",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MidnightNavy
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AttentionCard(
    icon: ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RevenueCurveChart(
    days: List<Pair<String, Double>>,
    selectedIndex: Int = -1,
    onSelectIndex: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    val maxVal = remember(days) {
        val highest = days.maxOfOrNull { it.second } ?: 0.0
        if (highest > 0.0) highest else 1000.0
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(days) {
                    detectTapGestures { offset ->
                        val n = days.size
                        if (n > 0) {
                            val segmentWidth = size.width / n
                            val tappedIdx = (offset.x / segmentWidth).toInt().coerceIn(0, n - 1)
                            onSelectIndex(tappedIdx)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val n = days.size

            // Y-axis guide lines at 15%, 55%, 85%
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(0f, height * 0.15f),
                end = Offset(width, height * 0.15f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(0f, height * 0.55f),
                end = Offset(width, height * 0.55f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFFF8FAFC),
                start = Offset(0f, height * 0.85f),
                end = Offset(width, height * 0.85f),
                strokeWidth = 1.dp.toPx()
            )

            val xPad = 24.dp.toPx()
            val availableWidth = width - (xPad * 2)

            val points = days.mapIndexed { idx, pair ->
                val x = if (n <= 1) width / 2f else xPad + (idx.toFloat() / (n - 1)) * availableWidth
                val ratio = (pair.second / maxVal).coerceIn(0.0, 1.0).toFloat()
                val y = height * (0.85f - (ratio * 0.67f))
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX = (p0.x + p1.x) / 2
                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }
                }

                // Fill gradient under curve
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height * 0.90f)
                    lineTo(points.first().x, height * 0.90f)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RoyalBlue.copy(alpha = 0.22f),
                            RoyalBlue.copy(alpha = 0.02f)
                        )
                    )
                )

                // Stroke line
                drawPath(
                    path = path,
                    color = RoyalBlue,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points / dots
                points.forEachIndexed { i, pt ->
                    val isSelected = (i == selectedIndex)
                    if (isSelected) {
                        // Outer glowing halo
                        drawCircle(
                            color = RoyalBlue.copy(alpha = 0.25f),
                            radius = 9.dp.toPx(),
                            center = pt
                        )
                        // Solid navy center
                        drawCircle(
                            color = MidnightNavy,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        // White inner dot
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    } else if (days[i].second > 0) {
                        drawCircle(
                            color = RoyalBlue,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { i, (day, _) ->
                val isSelected = (i == selectedIndex)
                Text(
                    text = day,
                    fontSize = 11.sp,
                    color = if (isSelected) MidnightNavy else TextMuted,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.clickable { onSelectIndex(i) }
                )
            }
        }
    }
}

@Composable
fun RecentOrderRowCard(
    order: Order,
    serverUrl: String,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val firstItem = order.items.firstOrNull()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                AdminProductImage(
                    model = firstItem?.image,
                    contentDescription = firstItem?.name,
                    serverUrl = serverUrl,
                    fallbackEmoji = "🧶",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderId}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEEF4FF)
                    ) {
                        Text(
                            text = order.statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = order.shippingAddress?.recipientName ?: "Boutique Customer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Text(
                    text = order.displayDate,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Text(
                text = currencyFormat.format(order.total),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MidnightNavy
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


