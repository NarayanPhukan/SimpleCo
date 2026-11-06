package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.babyygurll.admin.data.model.ShipmentItem
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingScreen(
    state: AdminUiState,
    onBack: () -> Unit,
    onNewShipment: () -> Unit = {},
    onViewOrder: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Ready", "In transit", "Delivered"
    var selectedShipmentForDetail by remember { mutableStateOf<ShipmentItem?>(null) }
    var showDispatchDialog by remember { mutableStateOf(false) }

    // Real shipment manifests extracted directly from live orders
    val realShipments = remember(state.orders) {
        val nonCancelledOrders = state.orders.filter { it.statusCode != "cancelled" }
        nonCancelledOrders.map { ord ->
            val city = ord.shippingAddress?.city?.ifBlank { null }
                ?: ord.shippingAddress?.state?.ifBlank { null }
                ?: "India"
            val carrierText = if (!ord.trackingNumber.isNullOrBlank()) {
                "${ord.carrier ?: "Courier"} · ${ord.trackingNumber}"
            } else {
                "${ord.carrier ?: "Express"} · Est ${ord.estimatedDelivery ?: "2-3 days"}"
            }
            val status = when (ord.statusCode.lowercase()) {
                "delivered" -> "Delivered"
                "shipped" -> "In transit"
                else -> "Ready to ship"
            }
            ShipmentItem(
                id = ord.orderId,
                orderRef = "#${ord.orderId}",
                destinationCity = city,
                carrierDetail = carrierText,
                status = status,
                recipientName = ord.shippingAddress?.recipientName?.ifBlank { null }
                    ?: (if (ord.userEmail.contains("@")) ord.userEmail.substringBefore("@") else "Valued Customer"),
                trackingUrl = null
            )
        }
    }

    val filteredList = remember(realShipments, searchQuery, selectedFilter) {
        realShipments.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.orderRef.contains(searchQuery, ignoreCase = true) ||
                item.destinationCity.contains(searchQuery, ignoreCase = true) ||
                item.carrierDetail.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Ready" -> item.status.contains("Ready", ignoreCase = true)
                "In transit" -> item.status.contains("transit", ignoreCase = true)
                "Delivered" -> item.status.equals("Delivered", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onBack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MidnightNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Shipping",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Shipments in progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Add button (+)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoyalBlue,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { showDispatchDialog = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New shipment",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Dark Midnight Navy KPI Banner Card (12 Shipments in progress ↗ 8.2%)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2C69),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalShipping,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "${realShipments.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Shipments in progress",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Dynamic Shipment Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val inTransit = realShipments.count { it.status == "In transit" }
                        if (realShipments.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$inTransit in transit",
                                color = Color(0xFF22C55E),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "No shipments",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar: Search shipping with Tune icon
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search shipping",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filter",
                            tint = MidnightNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills: All | Ready | In transit | Delivered
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Ready", "In transit", "Delivered").forEach { pill ->
                    val isSelected = selectedFilter.equals(pill, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) RoyalBlue else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = pill }
                    ) {
                        Text(
                            text = pill,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Header: Overview & Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    fontSize = 16.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        selectedFilter = if (selectedFilter == "Ready") "In transit" else "Ready"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter",
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Filter",
                        color = RoyalBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grouped Card List of Shipments
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (realShipments.isEmpty()) "No shipments in progress" else "No matching shipments",
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (realShipments.isEmpty()) "When orders are packed and dispatched, shipment manifests will appear here." else "Try adjusting your search query or filter.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        filteredList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedShipmentForDetail = item }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocalShipping,
                                            contentDescription = null,
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "${item.orderRef} · ${item.destinationCity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MidnightNavy,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.carrierDetail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val (bgCol, txtCol) = when (item.status) {
                                    "Ready to ship" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                    "Delivered" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                    else -> Color(0xFFF1F5F9) to Color(0xFF475569)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = bgCol
                                ) {
                                    Text(
                                        text = item.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = txtCol,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = "Details",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (index < filteredList.size - 1) {
                            HorizontalDivider(
                                color = BrandBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Informational Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0F7FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDBEAFE),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Everything is up to date",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "We'll notify you when something needs your attention.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Detail Dialog
    selectedShipmentForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedShipmentForDetail = null },
            title = {
                Text(
                    text = "${item.orderRef} • ${item.destinationCity}",
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recipient: ${item.recipientName}", color = MidnightNavy, fontWeight = FontWeight.SemiBold)
                    Text("Carrier: ${item.carrierDetail}", color = RoyalBlue)
                    Text("Status: ${item.status}", color = TextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanId = item.orderRef.replace("#", "")
                        onViewOrder(cleanId)
                        selectedShipmentForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("View Order Details", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedShipmentForDetail = null }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }

    if (showDispatchDialog) {
        var awbNumber by remember { mutableStateOf("") }
        var courier by remember { mutableStateOf("Shiprocket") }

        AlertDialog(
            onDismissRequest = { showDispatchDialog = false },
            title = {
                Text("Generate Shipping Label", fontWeight = FontWeight.Bold, color = MidnightNavy)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = courier,
                        onValueChange = { courier = it },
                        label = { Text("Logistics Partner") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = awbNumber,
                        onValueChange = { awbNumber = it },
                        label = { Text("AWB / Tracking Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDispatchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Dispatch Parcel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDispatchDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

