package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
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
import com.babyygurll.store.data.model.ShippingAddress
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun SavedAddressesScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSaved = viewModel.savedAddress
    var addressList by remember {
        mutableStateOf(
            listOf(
                ShippingAddress.from(
                    name = if (currentSaved.name.isNotBlank()) currentSaved.name else "Ananya Sharma",
                    phone = if (currentSaved.phone.isNotBlank()) currentSaved.phone else "+91 98765 43210",
                    addressLine = if (currentSaved.addressLine.isNotBlank()) currentSaved.addressLine else "42, Bluebell Apartments, Lake View Road",
                    city = if (currentSaved.city.isNotBlank()) currentSaved.city else "Bengaluru",
                    state = if (currentSaved.state.isNotBlank()) currentSaved.state else "Karnataka",
                    pincode = if (currentSaved.pincode.isNotBlank()) currentSaved.pincode else "560001"
                )
            )
        )
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    // Dialog state
    var dlgName by remember { mutableStateOf("") }
    var dlgPhone by remember { mutableStateOf("") }
    var dlgLine by remember { mutableStateOf("") }
    var dlgLandmark by remember { mutableStateOf("") }
    var dlgCity by remember { mutableStateOf("") }
    var dlgState by remember { mutableStateOf("") }
    var dlgPin by remember { mutableStateOf("") }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = ShippingAddress.from(
                            name = dlgName.ifBlank { "Ananya Sharma" },
                            phone = dlgPhone.ifBlank { "+91 98765 43210" },
                            addressLine = dlgLine.ifBlank { "42, Bluebell Apartments, Lake View Road" },
                            city = dlgCity.ifBlank { "Bengaluru" },
                            state = dlgState.ifBlank { "Karnataka" },
                            pincode = dlgPin.ifBlank { "560001" }
                        )
                        viewModel.saveShippingAddress(updated)
                        if (editingIndex != null && editingIndex!! < addressList.size) {
                            addressList = addressList.toMutableList().also { it[editingIndex!!] = updated }
                        } else {
                            addressList = addressList + updated
                        }
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Address", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text(
                    text = if (editingIndex != null) "Edit Address" else "Add New Address",
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dlgName,
                        onValueChange = { dlgName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dlgPhone,
                        onValueChange = { dlgPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dlgLine,
                        onValueChange = { dlgLine = it },
                        label = { Text("House / Flat / Road") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dlgLandmark,
                        onValueChange = { dlgLandmark = it },
                        label = { Text("Landmark (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dlgCity,
                            onValueChange = { dlgCity = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dlgPin,
                            onValueChange = { dlgPin = it },
                            label = { Text("PIN") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = dlgState,
                        onValueChange = { dlgState = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "Saved Addresses",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = BrandDarkNavy
                            )
                            Text(
                                text = "Manage your delivery locations.",
                                fontSize = 12.sp,
                                color = TextSecondary
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Blue CTA Card: Add New Address
            Surface(
                color = BrandPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editingIndex = null
                        dlgName = ""
                        dlgPhone = ""
                        dlgLine = ""
                        dlgLandmark = ""
                        dlgCity = ""
                        dlgState = ""
                        dlgPin = ""
                        showAddressDialog = true
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // White plus inside rounded container
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add New Address",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Save a new delivery location",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Section: Your addresses & 1 SAVED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your addresses",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy
                )
                Text(
                    text = "${addressList.size} SAVED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // Address Cards matching screenshot
            addressList.forEachIndexed { index, addr ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFF3B82F6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top row: HOME badge + DEFAULT
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Home,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "HOME",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                if (index == 0) {
                                    Text(
                                        text = "DEFAULT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = addr.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = addr.phone,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = addr.addressLine,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = "Near Central Library",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${addr.city}, ${addr.state} — ${addr.pincode}",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Edit & Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.clickable {
                                        editingIndex = index
                                        dlgName = addr.name
                                        dlgPhone = addr.phone
                                        dlgLine = addr.addressLine
                                        dlgLandmark = "Near Central Library"
                                        dlgCity = addr.city
                                        dlgState = addr.state
                                        dlgPin = addr.pincode
                                        showAddressDialog = true
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandDarkNavy
                                    )
                                }

                                Row(
                                    modifier = Modifier.clickable {
                                        if (addressList.size > 1) {
                                            addressList = addressList.filterIndexed { i, _ -> i != index }
                                        }
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Delete",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Bottom Highlight Strip: Selected for checkout
                        Surface(
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Selected for checkout",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary
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

