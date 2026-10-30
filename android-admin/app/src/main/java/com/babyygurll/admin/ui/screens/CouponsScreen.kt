package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.model.Coupon
import com.babyygurll.admin.data.model.CouponCreateRequest
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var couponToDelete by remember { mutableStateOf<Coupon?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftIvory,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Coupons & Offers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Text(
                            text = "${uiState.coupons.size} active studio discounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchCoupons() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh coupons",
                            tint = MidnightNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add coupon") },
                text = { Text("New Coupon", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.coupons.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = RoyalBlue.copy(alpha = 0.08f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalOffer,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Discount Codes Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create promotional codes like percentage discounts or free delivery to reward shoppers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create First Coupon")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.coupons, key = { it.id ?: it.code }) { coupon ->
                        CouponCard(
                            coupon = coupon,
                            onDelete = { couponToDelete = coupon }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = RoyalBlue
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateCouponDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { req ->
                viewModel.createCoupon(req) {
                    showCreateDialog = false
                }
            }
        )
    }

    couponToDelete?.let { coupon ->
        AlertDialog(
            onDismissRequest = { couponToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = CrimsonAccent) },
            title = { Text("Delete Coupon?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to deactivate and remove coupon '${coupon.code}'? Shoppers will no longer be able to apply it.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coupon.id?.let { viewModel.deleteCoupon(it) }
                        couponToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent)
                ) {
                    Text("Delete Coupon", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { couponToDelete = null }) {
                    Text("Cancel", color = CharcoalGray)
                }
            }
        )
    }
}

@Composable
fun CouponCard(
    coupon: Coupon,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ticket Code Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MidnightNavy.copy(alpha = 0.06f),
                    modifier = Modifier.border(
                        width = 1.5.dp,
                        color = MidnightNavy.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = MidnightNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = coupon.code,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            color = MidnightNavy
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete coupon",
                        tint = CrimsonAccent.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Value summary & Description
            val discountText = when (coupon.discountType) {
                "percentage" -> "${coupon.discountValue.toInt()}% OFF"
                "flat" -> "₹${coupon.discountValue.toInt()} OFF"
                "free_shipping" -> "FREE SHIPPING"
                else -> "${coupon.discountValue} OFF"
            }

            Text(
                text = discountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue
            )

            if (coupon.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = coupon.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0xFFF1F3F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val minText = if (coupon.minOrderValue > 0) "Min ₹${coupon.minOrderValue.toInt()}" else "No min"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF4F6FB)
                    ) {
                        Text(
                            text = minText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MidnightNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (coupon.isPublic) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StageReceivedBg
                        ) {
                            Text(
                                text = "Public",
                                style = MaterialTheme.typography.labelSmall,
                                color = StageReceivedText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFECEFF1)
                        ) {
                            Text(
                                text = "Secret / VIP",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF455A64),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Redeemed: ${coupon.usageCount}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalGray.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCouponDialog(
    onDismiss: () -> Unit,
    onConfirm: (CouponCreateRequest) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("percentage") } // percentage, flat, free_shipping
    var discountValue by remember { mutableStateOf("15") }
    var minOrderValue by remember { mutableStateOf("0") }
    var isPublic by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Promo Coupon",
                fontWeight = FontWeight.Bold,
                color = MidnightNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code *") },
                    placeholder = { Text("e.g. CROCHET15") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. 15% off boutique opening") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Discount Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MidnightNavy
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = discountType == "percentage",
                        onClick = { discountType = "percentage" },
                        label = { Text("% Off") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = discountType == "flat",
                        onClick = { discountType = "flat" },
                        label = { Text("₹ Flat") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = discountType == "free_shipping",
                        onClick = { discountType = "free_shipping" },
                        label = { Text("Free Ship") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                if (discountType != "free_shipping") {
                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text(if (discountType == "percentage") "Percentage Value (%) *" else "Discount Value (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = minOrderValue,
                    onValueChange = { minOrderValue = it },
                    label = { Text("Min Order Value (₹)") },
                    placeholder = { Text("0 for no minimum") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = CheckboxDefaults.colors(checkedColor = RoyalBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Display on website announcement / banner",
                        style = MaterialTheme.typography.bodySmall,
                        color = MidnightNavy
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isBlank()) {
                        errorMessage = "Please enter a coupon code"
                        return@Button
                    }
                    val dVal = if (discountType == "free_shipping") 0.0 else (discountValue.toDoubleOrNull() ?: 0.0)
                    val mVal = minOrderValue.toDoubleOrNull() ?: 0.0

                    onConfirm(
                        CouponCreateRequest(
                            code = code.trim().uppercase(),
                            description = description.trim(),
                            discountType = discountType,
                            discountValue = dVal,
                            minOrderValue = mVal,
                            isPublic = isPublic
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Create Code", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalGray)
            }
        }
    )
}

