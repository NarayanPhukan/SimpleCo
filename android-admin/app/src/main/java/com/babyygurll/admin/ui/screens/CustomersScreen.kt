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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.data.model.Customer
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    state: AdminUiState,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateOrders: (String) -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Customers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                        Text(
                            text = "${state.customers.size} boutique patrons",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MidnightNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIvory)
            )
        },
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.customerSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by name, phone, email, city...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RoyalBlue)
                },
                trailingIcon = {
                    if (state.customerSearchQuery.isNotEmpty()) {
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "👥", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.customerSearchQuery.isNotEmpty())
                                "No customers match '${state.customerSearchQuery}'"
                            else
                                "No customer records yet",
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
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
                ) {
                    items(state.filteredCustomers) { customer ->
                        CustomerCard(
                            customer = customer,
                            currencyFormat = currencyFormat,
                            onWhatsAppClick = {
                                val cleanPhone = customer.phone.replace("[^0-9]".toRegex(), "")
                                val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=Hello%20${Uri.encode(customer.name)},%20greeting%20from%20BabyyGurll%20Crochet%20Studio!%20🌸")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            onCallClick = {
                                val uri = Uri.parse("tel:${customer.phone}")
                                context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                            },
                            onEmailClick = {
                                if (customer.email.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${customer.email}")
                                        putExtra(Intent.EXTRA_SUBJECT, "Update regarding your BabyyGurll order")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    currencyFormat: NumberFormat,
    onWhatsAppClick: () -> Unit,
    onCallClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Avatar
                val initials = remember(customer.name) {
                    customer.name.split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .joinToString("")
                        .ifBlank { "C" }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                    if (customer.city.isNotBlank()) {
                        Text(
                            text = customer.city,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray.copy(alpha = 0.7f)
                        )
                    }
                }

                // Total Orders Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEEF4FF)
                ) {
                    Text(
                        text = "${customer.ordersCount} ${if (customer.ordersCount == 1) "order" else "orders"}",
                        color = RoyalBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (customer.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray
                        )
                    }
                }
                if (customer.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = customer.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BrandBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = currencyFormat.format(customer.totalSpent),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (customer.phone.isNotBlank()) {
                        FilledTonalIconButton(
                            onClick = onWhatsAppClick,
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFDCFCE7),
                                contentColor = Color(0xFF16A34A)
                            )
                        ) {
                            Text("💬", fontSize = 16.sp)
                        }

                        FilledTonalIconButton(
                            onClick = onCallClick,
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFEEF4FF),
                                contentColor = RoyalBlue
                            )
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        }
                    }

                    if (customer.email.isNotBlank()) {
                        FilledTonalIconButton(
                            onClick = onEmailClick,
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = CharcoalGray
                            )
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = "Email", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

