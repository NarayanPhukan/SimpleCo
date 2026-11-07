package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    state: AdminUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onSelectOrder: (Order) -> Unit,
    onCreateManualOrder: () -> Unit = {},
    onOpenShippingChecker: () -> Unit = {},
    onOpenMoreMenu: () -> Unit = {}
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Filter pills matching Image 3
    val filterOptions = remember(state.orders) {
        listOf(
            "all" to "All ${state.orders.size}",
            "new" to "New",
            "confirmed" to "Confirmed",
            "preparing" to "Preparing",
            "handmade" to "Being Handmade",
            "ready" to "Ready to Ship",
            "shipped" to "Shipped",
            "delivered" to "Delivered",
            "cancelled" to "Cancelled"
        )
    }

    var isAscendingSort by remember { mutableStateOf(false) }
    var showNewOrderDialog by remember { mutableStateOf(false) }

    // Sort filtered orders
    val sortedOrders = remember(state.filteredOrders, isAscendingSort) {
        if (isAscendingSort) {
            state.filteredOrders.sortedBy { it.createdAt ?: "" }
        } else {
            state.filteredOrders.sortedByDescending { it.createdAt ?: "" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Orders",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                },
                actions = {
                    // Delivery truck icon button in rounded border
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable { onOpenShippingChecker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocalShipping,
                            contentDescription = "Shipping",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Menu icon button in rounded border
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable { onOpenMoreMenu() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIvory)
            )
        },
        containerColor = SoftIvory
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = state.orderSearchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Order ID, customer or phone",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.orderSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BrandBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Horizontal Filter Pills (All 18, New, Confirmed, Preparing, Being Handmade...)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { (key, label) ->
                        val isSelected = state.selectedOrderFilter == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MidnightNavy else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onFilterChange(key) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else CharcoalGray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Subheader: "5 orders" on left, "Newest first >" on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sortedOrders.size} orders",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isAscendingSort = !isAscendingSort }
                    ) {
                        Text(
                            text = if (isAscendingSort) "Oldest first >" else "Newest first >",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalBlue
                        )
                    }
                }

                // Orders List
                if (sortedOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📦", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (state.orderSearchQuery.isNotEmpty())
                                    "No orders match '${state.orderSearchQuery}'"
                                else
                                    "No orders in this stage",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
                    ) {
                        items(sortedOrders) { order ->
                            OrderCardItem(
                                order = order,
                                serverUrl = state.serverUrl,
                                currencyFormat = currencyFormat,
                                onClick = { onSelectOrder(order) }
                            )
                        }
                    }
                }
            }

            // Bottom Floating Action Buttons: [✨ Production] and [+ New order]
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Production Button (White pill with border)
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .clickable { onFilterChange("handmade") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Production",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }
                }

                // New Order Button (Navy pill)
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = MidnightNavy,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .clickable { onCreateManualOrder() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New order",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showNewOrderDialog) {
        AlertDialog(
            onDismissRequest = { showNewOrderDialog = false },
            title = { Text("Studio Custom Order", fontWeight = FontWeight.Bold) },
            text = {
                Text("Custom direct orders can be created here. Enter client name, item, custom crochet notes, and dispatch details.")
            },
            confirmButton = {
                Button(
                    onClick = { showNewOrderDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("Got it", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun OrderCardItem(
    order: Order,
    serverUrl: String,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stacked Thumbnail Images
            Box(
                modifier = Modifier
                    .size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                val itemsList = order.items
                if (itemsList.size > 1) {
                    // Background secondary image stacked offset
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .offset(x = 10.dp, y = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        val secondImg = itemsList.getOrNull(1)?.image
                        AdminProductImage(
                            model = secondImg,
                            contentDescription = null,
                            serverUrl = serverUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Front primary image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    val firstImg = itemsList.firstOrNull()?.image
                    AdminProductImage(
                        model = firstImg,
                        contentDescription = null,
                        serverUrl = serverUrl,
                        fallbackEmoji = "🧶",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Top Row: Order ID & Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${order.orderId}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )

                    Text(
                        text = currencyFormat.format(order.total),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Customer Name + items count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val customerName = order.shippingAddress?.recipientName ?: "Boutique Customer"
                    val itemCount = order.items.sumOf { it.quantity }.let { if (it > 0) it else 1 }
                    Text(
                        text = "$customerName  $itemCount ${if (itemCount == 1) "item" else "items"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MidnightNavy
                    )

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Row: Date & Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.displayDate,
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Payment Badge (Paid / COD)
                        val isPaid = order.paymentStatus == "paid" || (order.paymentMethod?.contains("Razorpay", true) == true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPaid) Color(0xFFEEF4FF) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (isPaid) "Paid" else "COD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) RoyalBlue else Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        // Stage Pill Badge
                        val isHandmade = order.statusCode in listOf("crafted", "handmade")
                        val badgeBg = when {
                            isHandmade -> MidnightNavy
                            order.statusCode == "cancelled" -> CrimsonAccent
                            else -> Color(0xFFF1F5F9)
                        }
                        val badgeText = when {
                            isHandmade -> Color.White
                            order.statusCode == "cancelled" -> Color.White
                            else -> RoyalBlue
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = when (order.statusCode) {
                                    "received", "new" -> "New"
                                    "confirmed" -> "Confirmed"
                                    "crafted", "handmade" -> "Being Handmade"
                                    "packed", "ready" -> "Ready to Ship"
                                    "shipped" -> "Shipped"
                                    "delivered" -> "Delivered"
                                    "cancelled" -> "Cancelled"
                                    else -> order.statusLabel
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
