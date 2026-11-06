package com.babyygurll.admin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.*
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateManualOrderScreen(
    state: AdminUiState,
    onBack: () -> Unit,
    onCreateOrder: (ManualOrderCreateRequest, onDone: (Order) -> Unit) -> Unit,
    onOrderCreatedSuccess: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Order Source State (WhatsApp, Instagram, Offline)
    val sources = listOf("WhatsApp", "Instagram", "Offline")
    var selectedSource by remember { mutableStateOf("WhatsApp") }

    // Customer State
    var showCustomerMenu by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember(state.customers) {
        mutableStateOf<Customer?>(state.customers.firstOrNull())
    }

    // Product Selection State
    var showProductPicker by remember { mutableStateOf(false) }
    var selectedProduct by remember(state.products) {
        mutableStateOf<Product?>(
            state.products.find { it.price == 80.0 }
                ?: state.products.firstOrNull()
        )
    }
    var quantity by remember { mutableIntStateOf(2) }
    var customizationInstructions by remember {
        mutableStateOf("Color, size or personalization notes...")
    }

    // Discount State
    var couponCode by remember { mutableStateOf("") }
    var approvedDiscount by remember { mutableDoubleStateOf(0.0) }
    var showManualDiscountDialog by remember { mutableStateOf(false) }
    var manualDiscountInput by remember { mutableStateOf("") }

    // Address State
    var fullAddress by remember {
        mutableStateOf("42, Lake View Road, Indiranagar, Bengaluru, Karnataka 560038")
    }

    // When customer changes, update address if available
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let { cust ->
            if (cust.address.isNotBlank()) {
                fullAddress = "${cust.address}, ${cust.city}"
            }
        }
    }

    // Payment State
    var paymentMethod by remember { mutableStateOf("UPI") }
    var paymentStatus by remember { mutableStateOf("Paid") }
    var showPaymentMethodMenu by remember { mutableStateOf(false) }
    var showPaymentStatusMenu by remember { mutableStateOf(false) }

    val paymentMethods = listOf("UPI", "COD", "Card", "Cash", "Net Banking")
    val paymentStatuses = listOf("Paid", "Pending")

    // Price Calculations
    val unitPrice = selectedProduct?.price ?: 1890.0
    val itemsSubtotal = unitPrice * quantity
    val shippingFee = if (itemsSubtotal >= 999.0 && state.settings.freeShippingThreshold > 0) 80.0 else 80.0
    val finalTotal = (itemsSubtotal - approvedDiscount + shippingFee).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftIvory
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Create manual order",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "BabyyGurll Boutique",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            // Save as draft
                            val request = ManualOrderCreateRequest(
                                userEmail = selectedCustomer?.email?.ifBlank { "manual-order@babyygurll.in" }
                                    ?: "manual-order@babyygurll.in",
                                shippingAddress = ShippingAddress(
                                    recipientName = selectedCustomer?.name ?: "Customer",
                                    phone = selectedCustomer?.phone ?: "+91 99876 21402",
                                    street = fullAddress,
                                    city = "Bengaluru",
                                    state = "Karnataka",
                                    zip = "560038"
                                ),
                                paymentMethod = paymentMethod,
                                paymentStatus = "Pending",
                                items = listOf(
                                    OrderItem(
                                        productId = selectedProduct?.id ?: "manual_item",
                                        name = selectedProduct?.name ?: "Daisy Crochet Sling",
                                        color = "Sky blue · Medium",
                                        quantity = quantity,
                                        price = unitPrice,
                                        image = selectedProduct?.image ?: "",
                                        customNote = customizationInstructions
                                    )
                                ),
                                totals = ManualOrderTotals(
                                    subtotal = itemsSubtotal,
                                    discount = approvedDiscount,
                                    shipping = shippingFee,
                                    tax = 0.0,
                                    total = finalTotal
                                ),
                                isManualOrder = true,
                                source = selectedSource,
                                customerInstructions = customizationInstructions
                            )
                            onCreateOrder(request) { created ->
                                onOrderCreatedSuccess(created.orderId)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MidnightNavy),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Save draft",
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = {
                            val request = ManualOrderCreateRequest(
                                userEmail = selectedCustomer?.email?.ifBlank { "manual-order@babyygurll.in" }
                                    ?: "manual-order@babyygurll.in",
                                shippingAddress = ShippingAddress(
                                    recipientName = selectedCustomer?.name ?: "Customer",
                                    phone = selectedCustomer?.phone ?: "+91 99876 21402",
                                    street = fullAddress,
                                    city = "Bengaluru",
                                    state = "Karnataka",
                                    zip = "560038"
                                ),
                                paymentMethod = paymentMethod,
                                paymentStatus = paymentStatus.lowercase(),
                                items = listOf(
                                    OrderItem(
                                        productId = selectedProduct?.id ?: "manual_item",
                                        name = selectedProduct?.name ?: "Daisy Crochet Sling",
                                        color = "Sky blue · Medium",
                                        quantity = quantity,
                                        price = unitPrice,
                                        image = selectedProduct?.image ?: "",
                                        customNote = customizationInstructions
                                    )
                                ),
                                totals = ManualOrderTotals(
                                    subtotal = itemsSubtotal,
                                    discount = approvedDiscount,
                                    shipping = shippingFee,
                                    tax = 0.0,
                                    total = finalTotal
                                ),
                                isManualOrder = true,
                                source = selectedSource,
                                customerInstructions = customizationInstructions
                            )
                            onCreateOrder(request) { created ->
                                onOrderCreatedSuccess(created.orderId)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Create order",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ORDER SOURCE Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDER SOURCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sources.forEach { source ->
                        val isSelected = selectedSource == source
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MidnightNavy else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedSource = source }
                        ) {
                            Text(
                                text = source,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextMuted,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Customer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Customer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )

                    Text(
                        text = "Select customer",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    // Customer Selector Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showCustomerMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val label = selectedCustomer?.let { "${it.name} · ${it.phone}" }
                                    ?: "Aarohi Mehta · +91 99876 21402"
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MidnightNavy
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCustomerMenu,
                            onDismissRequest = { showCustomerMenu = false }
                        ) {
                            if (state.customers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Aarohi Mehta · +91 99876 21402") },
                                    onClick = {
                                        selectedCustomer = Customer(
                                            name = "Aarohi Mehta",
                                            phone = "+91 99876 21402",
                                            email = "aarohi@email.com",
                                            address = "42, Lake View Road, Indiranagar",
                                            city = "Bengaluru"
                                        )
                                        showCustomerMenu = false
                                    }
                                )
                            } else {
                                state.customers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { Text("${customer.name} · ${customer.phone}") },
                                        onClick = {
                                            selectedCustomer = customer
                                            showCustomerMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Create new customer action
                    Row(
                        modifier = Modifier
                            .clickable { showNewCustomerDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Create new customer",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalBlue
                        )
                    }
                }
            }

            // Products Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Products",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showProductPicker = true }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add product",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalBlue
                            )
                        }
                    }

                    // Product Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Thumbnail
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                        ) {
                            val imgUrl = selectedProduct?.image?.let {
                                if (it.startsWith("http")) it else "${state.serverUrl.trimEnd('/')}$it"
                            } ?: ""
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showProductPicker = true }
                        ) {
                            Text(
                                text = selectedProduct?.name?.ifBlank { "Daisy Crochet Sling" }
                                    ?: "Daisy Crochet Sling",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sky blue · Medium",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Quantity Stepper
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = MidnightNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = quantity.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(
                                    onClick = { quantity++ },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = MidnightNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Customization instructions text field
                    Text(
                        text = "Customization instructions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = customizationInstructions,
                        onValueChange = { customizationInstructions = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = BrandBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        placeholder = {
                            Text("Color, size or personalization notes...", color = TextMuted, fontSize = 13.sp)
                        }
                    )
                }
            }

            // Discount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Discount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { couponCode = it },
                            placeholder = { Text("Coupon code", color = TextMuted, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = BrandBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                if (couponCode.isNotBlank()) {
                                    val matched = state.coupons.find { it.code.equals(couponCode.trim(), true) }
                                    if (matched != null) {
                                        approvedDiscount = if (matched.discountType == "percentage") {
                                            (itemsSubtotal * (matched.discountValue / 100.0))
                                        } else {
                                            matched.discountValue
                                        }
                                    } else {
                                        approvedDiscount = 100.0 // Default demo coupon discount
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("Apply", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Approved manual discount link
                    Row(
                        modifier = Modifier
                            .clickable { showManualDiscountDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ConfirmationNumber,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (approvedDiscount > 0) "Discount applied: ₹${approvedDiscount.toInt()} (Tap to change)" else "Add approved manual discount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalBlue
                        )
                    }
                }
            }

            // Shipping Address Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Shipping address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )

                    Text(
                        text = "Full address",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = BrandBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            // Payment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Method Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Method",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showPaymentMethodMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = paymentMethod,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MidnightNavy
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showPaymentMethodMenu,
                                    onDismissRequest = { showPaymentMethodMenu = false }
                                ) {
                                    paymentMethods.forEach { method ->
                                        DropdownMenuItem(
                                            text = { Text(method) },
                                            onClick = {
                                                paymentMethod = method
                                                showPaymentMethodMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Status Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showPaymentStatusMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = paymentStatus,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MidnightNavy
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showPaymentStatusMenu,
                                    onDismissRequest = { showPaymentStatusMenu = false }
                                ) {
                                    paymentStatuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                paymentStatus = status
                                                showPaymentStatusMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Price Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                        text = "Price breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Items ($quantity)", fontSize = 13.sp, color = TextMuted)
                        Text(
                            text = currencyFormat.format(itemsSubtotal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightNavy
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Approved discount", fontSize = 13.sp, color = TextMuted)
                        Text(
                            text = "-${currencyFormat.format(approvedDiscount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightNavy
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Shipping", fontSize = 13.sp, color = TextMuted)
                        Text(
                            text = currencyFormat.format(shippingFee),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightNavy
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = BrandBorder.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Text(
                            text = currencyFormat.format(finalTotal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // New Customer Dialog
    if (showNewCustomerDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newCity by remember { mutableStateOf("Bengaluru") }
        var newStreet by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewCustomerDialog = false },
            title = { Text("Create New Customer", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStreet,
                        onValueChange = { newStreet = it },
                        label = { Text("Street Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val createdCust = Customer(
                                name = newName.trim(),
                                phone = newPhone.trim(),
                                email = newEmail.trim().ifBlank { "${newName.lowercase().replace(" ", "")}@client.in" },
                                city = newCity.trim(),
                                address = newStreet.trim()
                            )
                            selectedCustomer = createdCust
                            if (newStreet.isNotBlank()) {
                                fullAddress = "${newStreet.trim()}, ${newCity.trim()}"
                            }
                            showNewCustomerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("Add Customer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCustomerDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Product Picker Dialog
    if (showProductPicker) {
        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            title = { Text("Select Boutique Product", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.products.forEach { prod ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedProduct = prod
                                    showProductPicker = false
                                },
                            color = if (selectedProduct?.id == prod.id) Color(0xFFEFF6FF) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MidnightNavy
                                    )
                                    Text(
                                        text = "${currencyFormat.format(prod.price)} · ${prod.categoryName}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductPicker = false }) {
                    Text("Close", color = RoyalBlue)
                }
            }
        )
    }

    // Manual Discount Dialog
    if (showManualDiscountDialog) {
        AlertDialog(
            onDismissRequest = { showManualDiscountDialog = false },
            title = { Text("Manual Discount (₹)", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                OutlinedTextField(
                    value = manualDiscountInput,
                    onValueChange = { manualDiscountInput = it },
                    placeholder = { Text("e.g. 150") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = manualDiscountInput.toDoubleOrNull() ?: 0.0
                        approvedDiscount = amount
                        showManualDiscountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("Apply Discount", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDiscountDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
