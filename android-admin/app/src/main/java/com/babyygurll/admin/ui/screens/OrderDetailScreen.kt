package com.babyygurll.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Order
import com.babyygurll.admin.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order?,
    serverUrl: String,
    onBack: () -> Unit,
    onAdvanceStage: (String) -> Unit,
    onUpdateTracking: (orderId: String, carrier: String, trackingNumber: String, estDate: String) -> Unit,
    internalNotes: List<String> = emptyList(),
    onAddInternalNote: (orderId: String, note: String) -> Unit = { _, _ -> },
    checklist: Map<String, Boolean> = emptyMap(),
    onToggleChecklistItem: (orderId: String, item: String, isDone: Boolean) -> Unit = { _, _, _ -> }
) {
    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order not found", color = TextMuted)
        }
        return
    }

    val context = LocalContext.current
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    var showStatusDialog by remember { mutableStateOf(false) }
    var showTrackingDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showDocumentsDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }

    var carrierInput by remember(order.carrier) { mutableStateOf(order.carrier ?: "Delhivery Express") }
    var trackingInput by remember(order.trackingNumber) { mutableStateOf(order.trackingNumber ?: "") }
    var estDateInput by remember(order.estimatedDelivery) { mutableStateOf(order.estimatedDelivery ?: "") }

    val defaultChecklist = listOf(
        "Materials collected",
        "Crochet started",
        "Crochet completed",
        "Quality check passed",
        "Packaged & tagged"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = {
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "#${order.orderId}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Text(
                            text = "BabyyGurll Boutique",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable { showTrackingDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIvory)
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar: [Documents] and [Update status]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Documents button (white with border)
                    OutlinedButton(
                        onClick = { showDocumentsDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = MidnightNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Documents",
                            color = MidnightNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Update status button (Dark Navy)
                    Button(
                        onClick = { showStatusDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Update status",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        containerColor = SoftIvory
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // 1. ORDER PLACED Banner (Navy Card)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MidnightNavy)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ORDER PLACED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = order.dateFormatted ?: "Recently",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Status pill button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showStatusDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.statusLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Customer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )

                            // WhatsApp Action Link
                            val phone = order.shippingAddress?.phone ?: ""
                            Text(
                                text = "WhatsApp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                modifier = Modifier.clickable {
                                    val clean = phone.replace("[^0-9]".toRegex(), "")
                                    val formatted = if (clean.length == 10) "91$clean" else clean
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formatted&text=Hello%20${Uri.encode(order.shippingAddress?.recipientName ?: "")},%20this%20is%20BabyyGurll%20Studio%20regarding%20order%20%23${order.orderId}!%20🌸")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val initials = remember(order.shippingAddress?.recipientName) {
                                val n = order.shippingAddress?.recipientName ?: "AM"
                                n.split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "AM" }
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = order.shippingAddress?.recipientName ?: "Aarohi Mehta",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy
                                )
                                val contactLine = listOfNotNull(
                                    order.shippingAddress?.phone?.takeIf { it.isNotBlank() },
                                    order.shippingAddress?.email?.takeIf { it.isNotBlank() } ?: order.userEmail.takeIf { it.isNotBlank() }
                                ).joinToString(" · ")
                                Text(
                                    text = contactLine.ifBlank { "+91 99876 21402 · customer@email.com" },
                                    fontSize = 12.sp,
                                    color = CharcoalGray.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val fullAddress = listOfNotNull(
                            order.shippingAddress?.street?.takeIf { it.isNotBlank() },
                            order.shippingAddress?.city?.takeIf { it.isNotBlank() },
                            order.shippingAddress?.state?.takeIf { it.isNotBlank() },
                            order.shippingAddress?.zip?.takeIf { it.isNotBlank() }
                        ).joinToString(", ")

                        Text(
                            text = fullAddress.ifBlank { "42, Lake View Road, Indiranagar, Bengaluru, Karnataka 560038" },
                            fontSize = 13.sp,
                            color = CharcoalGray.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 3. Ordered Products Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ordered products",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )

                            Text(
                                text = "Edit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                modifier = Modifier.clickable { /* Edit piece options */ }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val displayItems = if (order.items.isNotEmpty()) order.items else emptyList()
                        if (displayItems.isEmpty()) {
                            SampleOrderItemRow(
                                title = "Daisy Crochet Sling",
                                variant = "Sky blue · Medium",
                                customNote = "Add two white daisies on the flap.",
                                qty = 1,
                                price = "₹1,890"
                            )
                        } else {
                            displayItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        if (!item.image.isNullOrBlank()) {
                                            AsyncImage(
                                                model = item.image,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(text = "🌸", fontSize = 24.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MidnightNavy
                                            )
                                            Text(
                                                text = currencyFormat.format(item.price * item.quantity),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MidnightNavy
                                            )
                                        }

                                        if (!item.color.isNullOrBlank()) {
                                            Text(
                                                text = item.color,
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }

                                        // Custom note callout if present
                                        if (!item.customNote.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFEEF4FF)
                                            ) {
                                                Text(
                                                    text = "Custom note: ${item.customNote}",
                                                    fontSize = 11.sp,
                                                    color = RoyalBlue,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Qty ${item.quantity} × ${currencyFormat.format(item.price)}",
                                            fontSize = 12.sp,
                                            color = CharcoalGray
                                        )
                                    }
                                }

                                if (index < displayItems.size - 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 4. Payment Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Payment summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        PaymentSummaryRow(
                            label = "Item subtotal",
                            value = currencyFormat.format(if (order.subtotal > 0) order.subtotal else order.total)
                        )

                        if (order.discount > 0) {
                            PaymentSummaryRow(
                                label = "Discount · BABYYGURLL",
                                value = "-${currencyFormat.format(order.discount)}",
                                valueColor = Color(0xFF16A34A)
                            )
                        }

                        PaymentSummaryRow(
                            label = "Shipping",
                            value = if (order.shippingCost > 0) currencyFormat.format(order.shippingCost) else "FREE"
                        )

                        PaymentSummaryRow(
                            label = "Tax",
                            value = "Included"
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = BrandBorder)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Final total",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                            Text(
                                text = currencyFormat.format(order.total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Payment Method Pill / Banner
                        val isPaid = order.paymentStatus == "paid" || (order.paymentMethod?.contains("Razorpay", true) == true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPaid) Color(0xFFEEF4FF) else Color(0xFFFEF3C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.CreditCard,
                                    contentDescription = null,
                                    tint = if (isPaid) RoyalBlue else Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isPaid) "UPI · Paid & verified" else "Cash on Delivery (Pending)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaid) RoyalBlue else Color(0xFFD97706)
                                    )
                                    val txId = order.razorpayPaymentId ?: order.razorpayOrderId ?: "pay_Ns82KX2391"
                                    Text(
                                        text = "Transaction ID · $txId",
                                        fontSize = 11.sp,
                                        color = CharcoalGray.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Order Notes Card (Image 5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )

                            Text(
                                text = "+ Add note",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                modifier = Modifier.clickable { showAddNoteDialog = true }
                            )
                        }

                        // Customer instructions note
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFD),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Customer instructions",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = order.customerInstructions
                                        ?: "Please gift wrap and include a handwritten birthday note.",
                                    fontSize = 12.sp,
                                    color = CharcoalGray
                                )
                            }
                        }

                        // Internal notes
                        val notes = if (internalNotes.isNotEmpty()) internalNotes else listOf("Blue yarn shade approved with customer on WhatsApp.")
                        notes.forEach { note ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFD),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Internal note · AK",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note,
                                        fontSize = 12.sp,
                                        color = CharcoalGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Order Timeline Card (Image 5 - 8 Stages)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Order timeline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val currentStage = when (order.statusCode) {
                            "received", "new" -> 1
                            "confirmed" -> 2
                            "preparing" -> 3
                            "crafted", "handmade" -> 4
                            "packed", "ready" -> 6
                            "shipped" -> 7
                            "delivered" -> 8
                            else -> 4
                        }

                        val timelineSteps = listOf(
                            Triple(1, "Order placed", "18 Feb · 10:20 AM"),
                            Triple(2, "Payment confirmed", "18 Feb · 11:21 AM"),
                            Triple(3, "Preparing", "18 Feb · 12:22 AM"),
                            Triple(4, "Being Handmade", "Current status"),
                            Triple(5, "Quality Check", "Pending"),
                            Triple(6, "Ready to Ship", "Pending"),
                            Triple(7, "Shipped", "Pending"),
                            Triple(8, "Delivered", "Pending")
                        )

                        timelineSteps.forEachIndexed { idx, (stepNum, title, time) ->
                            val isCompleted = stepNum < currentStage
                            val isCurrent = stepNum == currentStage
                            val isLast = idx == timelineSteps.size - 1

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Step Circle Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCompleted || isCurrent -> RoyalBlue
                                                    else -> Color.White
                                                }
                                            )
                                            .then(
                                                if (!isCompleted && !isCurrent) {
                                                    Modifier.border(1.5.dp, BrandBorder, CircleShape)
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$stepNum",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) Color.White else TextMuted
                                            )
                                        }
                                    }

                                    // Vertical connecting line
                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(26.dp)
                                                .background(if (isCompleted) RoyalBlue else BrandBorder)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isCurrent || isCompleted) MidnightNavy else TextMuted
                                    )
                                    Text(
                                        text = time,
                                        fontSize = 11.sp,
                                        color = if (isCurrent) RoyalBlue else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Production Checklist Card (Image 5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Production checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )

                            Text(
                                text = "Open queue",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                modifier = Modifier.clickable { /* View queue */ }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        defaultChecklist.forEach { task ->
                            val isChecked = checklist[task] ?: (task in listOf("Materials collected", "Crochet started"))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onToggleChecklistItem(order.orderId, task, !isChecked)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        onToggleChecklistItem(order.orderId, task, checked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = RoyalBlue,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task,
                                    fontSize = 13.sp,
                                    fontWeight = if (isChecked) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isChecked) MidnightNavy else CharcoalGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Update Status Dialog
    if (showStatusDialog) {
        val stages = listOf(
            "received" to "Order Received",
            "confirmed" to "Payment Confirmed",
            "preparing" to "Preparing Materials",
            "crafted" to "Being Handmade",
            "packed" to "Ready to Ship",
            "shipped" to "Shipped via Courier",
            "delivered" to "Delivered to Customer"
        )

        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Update Order Stage", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stages.forEach { (code, label) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (order.statusCode == code) RoyalBlue else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAdvanceStage(order.orderId)
                                    showStatusDialog = false
                                }
                        ) {
                            Text(
                                text = label,
                                color = if (order.statusCode == code) Color.White else MidnightNavy,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Internal Studio Note", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    placeholder = { Text("e.g. Blue yarn approved on WhatsApp...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteText.isNotBlank()) {
                            onAddInternalNote(order.orderId, newNoteText)
                            newNoteText = ""
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("Save Note", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Tracking / Courier Dialog
    if (showTrackingDialog) {
        AlertDialog(
            onDismissRequest = { showTrackingDialog = false },
            title = { Text("Shipping & Courier Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = carrierInput,
                        onValueChange = { carrierInput = it },
                        label = { Text("Courier Service") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = { trackingInput = it },
                        label = { Text("AWB / Tracking Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = estDateInput,
                        onValueChange = { estDateInput = it },
                        label = { Text("Estimated Delivery Date") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTracking(order.orderId, carrierInput, trackingInput, estDateInput)
                        showTrackingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("Save Tracking", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrackingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Documents / Packing Slip Dialog
    if (showDocumentsDialog) {
        AlertDialog(
            onDismissRequest = { showDocumentsDialog = false },
            title = { Text("Order Documents", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Launch browser or print receipt
                                val url = "$serverUrl/api/orders/${order.orderId}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                showDocumentsDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Print, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Print Boutique Packing Slip", fontWeight = FontWeight.SemiBold, color = MidnightNavy)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDocumentsDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Receipt, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Customer Tax Invoice (PDF)", fontWeight = FontWeight.SemiBold, color = MidnightNavy)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDocumentsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PaymentSummaryRow(
    label: String,
    value: String,
    valueColor: Color = MidnightNavy
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = CharcoalGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun SampleOrderItemRow(
    title: String,
    variant: String,
    customNote: String,
    qty: Int,
    price: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFCE7F3)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌸", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Text(
                    text = price,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
            }

            Text(
                text = variant,
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEEF4FF)
            ) {
                Text(
                    text = "Custom note: $customNote",
                    fontSize = 11.sp,
                    color = RoyalBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Qty $qty × $price",
                fontSize = 12.sp,
                color = CharcoalGray
            )
        }
    }
}
