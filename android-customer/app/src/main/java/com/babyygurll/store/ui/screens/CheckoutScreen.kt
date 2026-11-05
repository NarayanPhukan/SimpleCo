package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.ShippingAddress
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

val INDIAN_STATES = listOf(
    "Delhi", "Maharashtra", "Karnataka", "Tamil Nadu", "Assam", "West Bengal",
    "Gujarat", "Uttar Pradesh", "Rajasthan", "Kerala", "Telangana", "Haryana",
    "Punjab", "Bihar", "Odisha", "Madhya Pradesh", "Andhra Pradesh", "Goa"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    onStartPayment: (address: ShippingAddress, totalAmount: Double, paymentMethod: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialAddr = remember { viewModel.savedAddress }
    val grandTotal by viewModel.cartGrandTotal.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var name by remember { mutableStateOf(initialAddr.recipientName.ifBlank { currentUser?.name ?: "" }) }
    var phone by remember { mutableStateOf(initialAddr.phone.ifBlank { currentUser?.phone ?: "" }) }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }

    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf(initialAddr.street) }
    var city by remember { mutableStateOf(initialAddr.city) }
    var state by remember { mutableStateOf(initialAddr.state.ifBlank { "Delhi" }) }
    var zip by remember { mutableStateOf(initialAddr.zip) }
    var addressType by remember { mutableStateOf("Home") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }

    var stateExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Circular Back Button
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onBack() }
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

                    // Center: "Checkout"
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = BrandDarkNavy
                    )

                    // Right: Secure Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Secure",
                            color = BrandPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Final payable",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${grandTotal.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDarkNavy
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
                                val cleanZip = zip.replace("[^0-9]".toRegex(), "")
                                if (name.isBlank() || cleanPhone.length != 10 || city.isBlank() || cleanZip.length != 6) {
                                    formError = "Please complete full name, 10-digit mobile number, city and 6-digit PIN code."
                                    return@Button
                                }
                                formError = null
                                val fullStreet = if (houseNo.isNotBlank()) "$houseNo, $street" else street
                                val address = ShippingAddress(
                                    recipientName = name.trim(),
                                    phone = cleanPhone,
                                    street = fullStreet.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    zip = cleanZip
                                )
                                onStartPayment(address, grandTotal, selectedPaymentMethod)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pay Securely",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stepped Progress Indicator: (✓ Bag) ----- (② Address) ----- (③ Payment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Step 1: Bag (Completed)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Bag", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Medium)
                }

                // Dashed line
                HorizontalDivider(
                    color = BrandPrimary,
                    thickness = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Step 2: Address (Active)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, BrandPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", color = BrandPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Address", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                }

                // Dashed line
                HorizontalDivider(
                    color = SurfaceBorder,
                    thickness = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Step 3: Payment (Upcoming)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, TextMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", color = TextMuted, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Payment", fontSize = 11.sp, color = TextMuted)
                }
            }

            if (formError != null) {
                Surface(
                    color = StatusError.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formError!!,
                        color = StatusError,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Card 1: STEP 1 - Contact Information (Screenshot 5)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("STEP 1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 0.8.sp)
                            Text("Contact Information", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Full name", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; formError = null },
                        placeholder = { Text("Aanya Sharma", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SurfaceBorder,
                            focusedBorderColor = BrandPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Mobile number", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .height(52.dp)
                                .width(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+91", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; formError = null },
                            placeholder = { Text("98765 43210", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SurfaceBorder,
                                focusedBorderColor = BrandPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Email address", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("aanya@example.com", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SurfaceBorder,
                            focusedBorderColor = BrandPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Card 2: STEP 2 - Delivery Address (Screenshot 5)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("STEP 2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 0.8.sp)
                            Text("Delivery Address", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PIN Code & City Row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PIN code", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = zip,
                                onValueChange = { zip = it; formError = null },
                                placeholder = { Text("110001", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("City", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it; formError = null },
                                placeholder = { Text("New Delhi", color = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("House no. & building", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = houseNo,
                        onValueChange = { houseNo = it },
                        placeholder = { Text("House / Flat / Building", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SurfaceBorder,
                            focusedBorderColor = BrandPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Street, area or locality", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it; formError = null },
                        placeholder = { Text("Area / Road / Landmark", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SurfaceBorder,
                            focusedBorderColor = BrandPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("State", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = stateExpanded,
                        onExpandedChange = { stateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SurfaceBorder,
                                focusedBorderColor = BrandPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false }
                        ) {
                            INDIAN_STATES.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        state = option
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Address Type Chips: [Home] [Work] [Other]
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Home", "Work", "Other").forEach { type ->
                            val isSelected = addressType == type
                            Surface(
                                color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) BrandPrimary else SurfaceBorder),
                                modifier = Modifier.clickable { addressType = type }
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BrandPrimary else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: STEP 3 - Payment Method (Screenshot 1)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CreditCard,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("STEP 3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 0.8.sp)
                            Text("Payment Method", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Razorpay Secure Banner
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Razorpay Secure",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = "100% safe & encrypted payments",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment Method Radio Options
                    val paymentOptions = listOf(
                        "UPI" to "UPI",
                        "Card" to "Credit / Debit Card",
                        "Netbanking" to "Netbanking",
                        "Wallets" to "Wallets"
                    )

                    paymentOptions.forEachIndexed { index, (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = key }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, SurfaceBorder),
                                    modifier = Modifier.size(width = 42.dp, height = 30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        when (key) {
                                            "UPI" -> Text("UPI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                                            "Card" -> Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            "Netbanking" -> Text("₹", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            else -> Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandDarkNavy
                                )
                            }

                            RadioButton(
                                selected = selectedPaymentMethod == key,
                                onClick = { selectedPaymentMethod = key },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = BrandPrimary,
                                    unselectedColor = TextMuted
                                )
                            )
                        }

                        if (index < paymentOptions.size - 1) {
                            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        }
                    }
                }
            }

            // Order Summary Preview Card (Screenshot 1)
            val cartItems by viewModel.cartItems.collectAsState()
            var showSummaryModal by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSummaryModal = !showSummaryModal }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mini thumbnails
                        Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                            cartItems.take(2).forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    com.babyygurll.store.ui.components.BoutiqueImage(
                                        imageUrl = item.product.resolvedPrimaryImage,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            val count = cartItems.sumOf { it.quantity }
                            Text(
                                text = "$count handmade ${if (count == 1) "piece" else "pieces"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )
                            Text(
                                text = "View order summary",
                                fontSize = 11.sp,
                                color = BrandPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showSummaryModal) {
                    HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                    Column(modifier = Modifier.padding(14.dp)) {
                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}× ${item.product.name}",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹${(item.product.price * item.quantity).toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
