package com.babyygurll.admin.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.model.Order
import com.babyygurll.admin.data.model.RefundRecord
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundsScreen(
    state: AdminUiState,
    onBack: () -> Unit,
    onViewOrder: (String) -> Unit = {},
    onIssueRefund: (orderId: String, amount: Double?, reason: String?) -> Unit = { _, _, _ -> },
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTimePeriod by remember { mutableStateOf("Today") } // "Today", "Yesterday", "7 Days", "30 Days", "All"
    var showPeriodDropdown by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Refunds, 1: Batch Refunds
    var showRefundGuideDialog by remember { mutableStateOf(false) }
    var showNewRefundDialog by remember { mutableStateOf(false) }
    var selectedRefundForDetail by remember { mutableStateOf<RefundRecord?>(null) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Filter refunds by time period
    val filteredRefunds = remember(state.refunds, selectedTimePeriod) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sevenDaysMs = 7 * oneDayMs
        val thirtyDaysMs = 30 * oneDayMs

        when (selectedTimePeriod) {
            "Today" -> state.refunds.filter {
                val age = now - (it.createdAtTimestamp * 1000L)
                age in 0..oneDayMs || it.createdAtTimestamp == 0L
            }
            "Yesterday" -> state.refunds.filter {
                val age = now - (it.createdAtTimestamp * 1000L)
                age in oneDayMs..(2 * oneDayMs)
            }
            "7 Days" -> state.refunds.filter {
                val age = now - (it.createdAtTimestamp * 1000L)
                age in 0..sevenDaysMs || it.createdAtTimestamp == 0L
            }
            "30 Days" -> state.refunds.filter {
                val age = now - (it.createdAtTimestamp * 1000L)
                age in 0..thirtyDaysMs || it.createdAtTimestamp == 0L
            }
            else -> state.refunds
        }
    }

    // Dynamic metrics calculated strictly from real refund records
    val processedRefunds = remember(filteredRefunds) {
        filteredRefunds.filter { it.status.lowercase() in listOf("processed", "refunded") }
    }
    val processingRefunds = remember(filteredRefunds) {
        filteredRefunds.filter { it.status.lowercase() in listOf("pending", "processing", "manual_review", "refund_pending") }
    }
    val failedRefunds = remember(filteredRefunds) {
        filteredRefunds.filter { it.status.lowercase().startsWith("failed") }
    }

    val totalRefundedAmount = remember(processedRefunds) { processedRefunds.sumOf { it.amountRupees } }
    val totalProcessingAmount = remember(processingRefunds) { processingRefunds.sumOf { it.amountRupees } }
    val totalFailedAmount = remember(failedRefunds) { failedRefunds.sumOf { it.amountRupees } }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewRefundDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Issue Refund", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        bottomBar = {
            // Footer bar matching screenshot: Test Mode + Help & Support
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Test Mode Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Science,
                            contentDescription = "Mode",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Test Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MidnightNavy
                        )
                        Switch(
                            checked = false,
                            onCheckedChange = {
                                Toast.makeText(context, "Production Live Mode active for Razorpay", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    // Help & Support
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.clickable { showSupportDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Headphones,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Help & Support",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Navigation: "< Go Back"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBack)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Go Back",
                        color = RoyalBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            // 2. Title Row: "Refunds" + "Today ∨" Dropdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refunds",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy,
                        fontSize = 24.sp
                    )

                    // Period Dropdown Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.clickable { showPeriodDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = selectedTimePeriod,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MidnightNavy
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showPeriodDropdown,
                            onDismissRequest = { showPeriodDropdown = false }
                        ) {
                            listOf("Today", "Yesterday", "7 Days", "30 Days", "All").forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period, fontSize = 13.sp) },
                                    onClick = {
                                        selectedTimePeriod = period
                                        showPeriodDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Three Metric Cards: Refunded, Processing, Failed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 1: Refunded
                    RefundMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Refunded",
                        amount = "₹${String.format(Locale.ENGLISH, "%,.2f", totalRefundedAmount)}",
                        countSubtitle = "from ${processedRefunds.size} processed refunds",
                        onInfoClick = {
                            Toast.makeText(context, "Completed refunds settled back to customer bank/UPI", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Card 2: Processing
                    RefundMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Processing",
                        amount = "₹${String.format(Locale.ENGLISH, "%,.2f", totalProcessingAmount)}",
                        countSubtitle = "from ${processingRefunds.size} refunds",
                        onInfoClick = {
                            Toast.makeText(context, "Refunds currently queued with Razorpay banking network", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Card 3: Failed
                    RefundMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Failed",
                        amount = "₹${String.format(Locale.ENGLISH, "%,.2f", totalFailedAmount)}",
                        countSubtitle = "from ${failedRefunds.size} refunds",
                        onInfoClick = {
                            Toast.makeText(context, "Refunds that require manual account reconciliation", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // 4. Tabs: "Refunds" | "Batch Refunds"
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Tab 0: Refunds
                        Column(
                            modifier = Modifier
                                .clickable { selectedTab = 0 }
                                .padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "Refunds",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) RoyalBlue else TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedTab == 0) {
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(48.dp)
                                        .background(RoyalBlue, RoundedCornerShape(1.dp))
                                )
                            }
                        }

                        // Tab 1: Batch Refunds
                        Column(
                            modifier = Modifier
                                .clickable { selectedTab = 1 }
                                .padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "Batch Refunds",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) RoyalBlue else TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedTab == 1) {
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(80.dp)
                                        .background(RoyalBlue, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = BrandBorder, thickness = 1.dp)
                }
            }

            // 5. Main Card Content (Empty State or List of Refunds)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    if (selectedTab == 1) {
                        // Batch Refunds Tab placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Batch refunds upload",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MidnightNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Upload a CSV file containing multiple order IDs to process instant batch refunds.",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (filteredRefunds.isEmpty()) {
                        // Empty State matching Screenshot 1 exactly
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Illustration Graphic
                                RefundGraphicIllustration()

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Issue full, partial, or instant refunds",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MidnightNavy,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Refunds to customers get deducted from your current balance and appear here",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Refunds guide →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showRefundGuideDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else {
                        // Real Refunds List
                        Column(modifier = Modifier.fillMaxWidth()) {
                            filteredRefunds.forEachIndexed { index, refund ->
                                RefundItemRow(
                                    refund = refund,
                                    onClick = { selectedRefundForDetail = refund }
                                )

                                if (index < filteredRefunds.size - 1) {
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
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Detail Dialog for clicked refund
    selectedRefundForDetail?.let { refund ->
        AlertDialog(
            onDismissRequest = { selectedRefundForDetail = null },
            title = {
                Text(
                    text = "Refund #${refund.id.takeLast(8)}",
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Order: #${refund.orderId}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MidnightNavy)
                    Text(text = "Customer: ${refund.customerName} (${refund.customerEmail})", fontSize = 12.sp, color = TextMuted)
                    Text(text = "Amount: ₹${String.format(Locale.ENGLISH, "%,.2f", refund.amountRupees)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Text(text = "Status: ${refund.displayStatus}", fontSize = 12.sp, color = if (refund.displayStatus == "Processed") Color(0xFF16A34A) else TextMuted)
                    if (refund.paymentId.isNotBlank()) {
                        Text(text = "Payment ID: ${refund.paymentId}", fontSize = 11.sp, color = TextMuted)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ordId = refund.orderId
                        selectedRefundForDetail = null
                        if (ordId.isNotBlank()) onViewOrder(ordId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("View Order", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRefundForDetail = null }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }

    // New Refund Dialog
    if (showNewRefundDialog) {
        var orderIdInput by remember { mutableStateOf("") }
        var refundAmountInput by remember { mutableStateOf("") }
        var refundReasonInput by remember { mutableStateOf("") }
        var selectedOrderForRefund by remember { mutableStateOf<Order?>(null) }

        AlertDialog(
            onDismissRequest = { showNewRefundDialog = false },
            title = { Text("Issue Razorpay Refund", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter order ID or select a prepaid order to process a 100% full or partial refund back to source account / UPI.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = orderIdInput,
                        onValueChange = { input ->
                            orderIdInput = input
                            selectedOrderForRefund = state.orders.find { it.orderId.equals(input.trim(), ignoreCase = true) }
                            if (selectedOrderForRefund != null && refundAmountInput.isBlank()) {
                                refundAmountInput = selectedOrderForRefund!!.total.toString()
                            }
                        },
                        label = { Text("Order ID") },
                        placeholder = { Text("e.g. BG1048") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    selectedOrderForRefund?.let { ord ->
                        Text(
                            text = "Customer: ${ord.shippingAddress?.recipientName ?: ord.userEmail} · Order Total: ₹${ord.total}",
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedTextField(
                        value = refundAmountInput,
                        onValueChange = { refundAmountInput = it },
                        label = { Text("Refund Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = refundReasonInput,
                        onValueChange = { refundReasonInput = it },
                        label = { Text("Reason for Refund") },
                        placeholder = { Text("e.g. Customer cancellation, stock unavailable") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanId = orderIdInput.trim().removePrefix("#")
                        if (cleanId.isNotBlank()) {
                            val amt = refundAmountInput.toDoubleOrNull()
                            onIssueRefund(cleanId, amt, refundReasonInput.ifBlank { "Admin manual refund" })
                            showNewRefundDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    enabled = orderIdInput.isNotBlank() && !state.isProcessingRefund
                ) {
                    if (state.isProcessingRefund) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Process Refund", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewRefundDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Refunds Guide Dialog
    if (showRefundGuideDialog) {
        AlertDialog(
            onDismissRequest = { showRefundGuideDialog = false },
            title = { Text("Razorpay Refunds Guide", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Instant Refunds: Processed within minutes via Razorpay Optimum speed directly to customer's source UPI / debit card.",
                        fontSize = 12.sp,
                        color = MidnightNavy
                    )
                    Text(
                        text = "• Normal Refunds: Standard banking settlement takes 2 to 3 business days depending on customer's issuing bank.",
                        fontSize = 12.sp,
                        color = MidnightNavy
                    )
                    Text(
                        text = "• Balance Deduction: Refunded amounts are automatically deducted from your current Razorpay settlement account balance.",
                        fontSize = 12.sp,
                        color = MidnightNavy
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRefundGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Got It", color = Color.White)
                }
            }
        )
    }

    // Help & Support Dialog
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Razorpay Settlement & Refund Operations", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MidnightNavy)
                    Text("Support Email: care@babyygurll.in", fontSize = 12.sp, color = TextMuted)
                    Text("Merchant Support: 1800-123-7700 (9 AM - 7 PM Mon-Sat)", fontSize = 12.sp, color = TextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun RefundMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    countSubtitle: String,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier
                        .size(13.dp)
                        .clickable(onClick = onInfoClick)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MidnightNavy,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = countSubtitle,
                fontSize = 9.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RefundItemRow(
    refund: RefundRecord,
    onClick: () -> Unit
) {
    val (statusBg, statusTxt) = when (refund.displayStatus.lowercase()) {
        "processed" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "processing" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = refund.id.ifBlank { "rfnd_${refund.orderId}" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MidnightNavy
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = refund.displayStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTxt,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Order #${refund.orderId} · ${refund.customerName.ifBlank { "Customer" }}",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${String.format(Locale.ENGLISH, "%,.2f", refund.amountRupees)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MidnightNavy
            )
            Text(
                text = refund.speed.ifBlank { "optimum" },
                fontSize = 10.sp,
                color = RoyalBlue
            )
        }
    }
}

@Composable
fun RefundGraphicIllustration() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEFF6FF))
            .border(1.5.dp, Color(0xFFBFDBFE), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CurrencyExchange,
            contentDescription = null,
            tint = RoyalBlue,
            modifier = Modifier.size(36.dp)
        )
    }
}
