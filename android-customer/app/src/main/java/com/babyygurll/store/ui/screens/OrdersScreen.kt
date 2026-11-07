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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.Order
import com.babyygurll.store.ui.components.BoutiqueImage
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun OrdersScreen(
    viewModel: StoreViewModel,
    onOrderClick: (Order) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.customerOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Processing", "Shipped", "Delivered", "Cancelled")

    LaunchedEffect(currentUser) {
        currentUser?.email?.let { viewModel.fetchCustomerOrders(it) }
    }

    val filteredOrders = remember(orders, selectedTab) {
        when (selectedTab) {
            "Processing" -> orders.filter { !it.isCancelled && (it.status.equals("processing", ignoreCase = true) || it.status.equals("placed", ignoreCase = true) || it.statusStage < 2) }
            "Shipped" -> orders.filter { !it.isCancelled && it.status.equals("shipped", ignoreCase = true) }
            "Delivered" -> orders.filter { !it.isCancelled && it.status.equals("delivered", ignoreCase = true) }
            "Cancelled" -> orders.filter { it.isCancelled }
            else -> orders
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { onExploreClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = BrandDarkNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "My Orders",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = BrandDarkNavy
                        )
                    }

                    // Segmented Tabs Row: All | Processing | Shipped | Delivered (Screenshot 2)
                    TabRow(
                        selectedTabIndex = tabs.indexOf(selectedTab),
                        containerColor = Color.White,
                        contentColor = BrandPrimary,
                        divider = { HorizontalDivider(color = SurfaceBorder, thickness = 1.dp) }
                    ) {
                        tabs.forEach { tabName ->
                            val isSelected = selectedTab == tabName
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = tabName },
                                text = {
                                    Text(
                                        text = tabName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) BrandPrimary else TextSecondary
                                    )
                                }
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${filteredOrders.size} ${if (filteredOrders.size == 1) "order" else "orders"} in the last 6 months",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            if (isLoading && orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPrimary)
                }
            } else if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No orders found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = BrandDarkNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When you order our handcrafted pieces, track them right here!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onExploreClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Text("Start Shopping", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredOrders, key = { it.orderId.ifBlank { it.id } }) { order ->
                        OrderHistoryCard(
                            order = order,
                            onClick = { onOrderClick(order) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Order Card matching Screenshot 2
 */
@Composable
fun OrderHistoryCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: ORDER #... | Date
            val orderRef = order.orderId.ifBlank { "BG" + order.id.takeLast(8) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDER #$orderRef",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = order.displayDate,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            // Item Details Row (Screenshot 2)
            val firstItem = order.items.firstOrNull()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.size(64.dp)
                ) {
                    BoutiqueImage(
                        imageUrl = firstItem?.image,
                        contentDescription = firstItem?.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Status Badge (Screenshot 2: HANDCRAFTED / SHIPPED / DELIVERED)
                    val statusUpper = order.status.uppercase()
                    val badgeColor = when {
                        order.isCancelled || statusUpper.contains("CANCEL") -> StatusError
                        statusUpper.contains("DELIVER") -> StatusSuccess
                        statusUpper.contains("SHIP") -> BrandPrimary
                        else -> Color(0xFF2563EB)
                    }
                    val badgeBg = when {
                        order.isCancelled || statusUpper.contains("CANCEL") -> StatusError.copy(alpha = 0.12f)
                        statusUpper.contains("DELIVER") -> Color(0xFFDCFCE7)
                        statusUpper.contains("SHIP") -> Color(0xFFEFF6FF)
                        else -> Color(0xFFEFF6FF)
                    }
                    val badgeText = when {
                        order.isCancelled || statusUpper.contains("CANCEL") -> "CANCELLED"
                        statusUpper.contains("DELIVER") -> "DELIVERED"
                        statusUpper.contains("SHIP") -> "SHIPPED"
                        else -> "HANDCRAFTED"
                    }

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = firstItem?.name ?: "Handcrafted Crochet Piece",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkNavy,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val statusMsg = when {
                        statusUpper.contains("DELIVER") -> "Delivered to your doorstep"
                        statusUpper.contains("SHIP") -> "In transit with Shiprocket"
                        else -> "Your order is being made with care"
                    }
                    Text(
                        text = statusMsg,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹${order.total.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkNavy
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            // Footer: View order details › (Screenshot 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View order details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
