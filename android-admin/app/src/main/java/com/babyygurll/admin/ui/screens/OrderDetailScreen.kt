package com.babyygurll.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Order
import com.babyygurll.admin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order?,
    serverUrl: String,
    onBack: () -> Unit,
    onAdvanceStage: (String) -> Unit,
    onUpdateTracking: (orderId: String, carrier: String, trackingNumber: String, estDate: String) -> Unit
) {
    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order not found")
        }
        return
    }

    val context = LocalContext.current
    var showTrackingDialog by remember { mutableStateOf(false) }
    var carrierInput by remember { mutableStateOf(order.carrier ?: "Express Air") }
    var trackingInput by remember { mutableStateOf(order.trackingNumber ?: "") }
    var estDateInput by remember { mutableStateOf(order.estimatedDelivery ?: "") }

    val statusColor = when (order.statusCode) {
        "received" -> StatusReceived
        "crafted" -> StatusCrafted
        "packed" -> StatusPacked
        "shipped" -> StatusShipped
        "delivered" -> StatusDelivered
        else -> TextSecondary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSecondary)
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stage Banner Card
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
                            Column {
                                Text(
                                    text = "Current Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = order.statusLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Stage ${order.statusStage + 1}/5",
                                    color = statusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.statusStage < 4 && order.statusCode != "cancelled") {
                                Button(
                                    onClick = { onAdvanceStage(order.orderId) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Advance Stage", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            OutlinedButton(
                                onClick = { showTrackingDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Waybill")
                            }
                        }
                    }
                }
            }

            // Customer & Delivery Address Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer & Shipping Destination",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val addr = order.shippingAddress
                        Text(
                            text = addr?.recipientName ?: order.userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        if (!addr?.street.isNullOrBlank()) {
                            Text(text = addr?.street ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!addr?.city.isNullOrBlank()) {
                            Text(
                                text = "${addr?.city}, ${addr?.state ?: ""} - ${addr?.zip ?: ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (!addr?.phone.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${addr?.phone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call: ${addr?.phone}")
                                }
                            }
                        }
                    }
                }
            }

            // Ordered Items
            item {
                Text(
                    text = "Pieces in Order (${order.items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(order.items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imageUrl = if (item.image?.startsWith("http") == true) item.image
                        else "${serverUrl.trimEnd('/')}/${item.image?.trimStart('/')}"

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandPrimaryLight)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (!item.color.isNullOrBlank()) {
                                Text(
                                    text = "Yarn Color: ${item.color}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            if (!item.customNote.isNullOrBlank()) {
                                Text(
                                    text = "Note: \"${item.customNote}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${item.price.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Qty: ${item.quantity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Order Financial Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", color = TextSecondary)
                            Text("₹${order.subtotal.toInt()}")
                        }
                        if (order.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", color = StatusDelivered)
                                Text("-₹${order.discount.toInt()}", color = StatusDelivered)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Shipping", color = TextSecondary)
                            Text(if (order.shippingCost == 0.0) "FREE" else "₹${order.shippingCost.toInt()}")
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BrandBorder)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("₹${order.total.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Payment: ${order.paymentMethod ?: "Online"} (${order.paymentStatus?.uppercase() ?: "PAID"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Tracking Waybill Edit Dialog
    if (showTrackingDialog) {
        AlertDialog(
            onDismissRequest = { showTrackingDialog = false },
            title = { Text("Update Shipment Tracking") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = carrierInput,
                        onValueChange = { carrierInput = it },
                        label = { Text("Courier / Carrier Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = { trackingInput = it },
                        label = { Text("AWB / Tracking Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estDateInput,
                        onValueChange = { estDateInput = it },
                        label = { Text("Estimated Delivery Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTracking(order.orderId, carrierInput, trackingInput, estDateInput)
                        showTrackingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Tracking", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrackingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

