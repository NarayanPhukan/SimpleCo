package com.babyygurll.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
    onFilterChange: (String) -> Unit = {},
    onRefresh: () -> Unit,
    onNavigateOrders: (String) -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val filterPills = listOf("All", "New", "Returning", "High-value")
    var selectedCustomerForDialog by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: Title ("Customers"), Subtitle ("1,284 customer profiles")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Customers",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${state.customers.size} ${if (state.customers.size == 1) "customer profile" else "customer profiles"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            // Search Bar: Search name, phone or email
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
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
                        value = state.customerSearchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "Search name, phone or email",
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
                    if (state.customerSearchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
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

            // Navy KPI Metrics Banner (1,284 Total customers | 38% Returning | ₹4,820 Avg. spend)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Customers
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${state.metrics.totalCustomersCount}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Total customers",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Returning %
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${state.metrics.returningCustomersPercentage}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Returning",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Avg. Spend
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currencyFormat.format(state.metrics.averageCustomerSpend),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Avg. spend",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Pills Row (All, New, Returning, High-value)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterPills.forEach { pill ->
                    val isSelected = state.selectedCustomerFilter.equals(pill, true)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) RoyalBlue else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onFilterChange(pill) }
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

            Spacer(modifier = Modifier.height(14.dp))

            // Customer Profiles Grouped Card List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                val list = state.filteredCustomers
                if (list.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = TextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No matching customer profiles",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(list, key = { index, cust -> cust.email.ifBlank { "${cust.name}_$index" } }) { idx, customer ->
                            CustomerListItem(
                                customer = customer,
                                currencyFormat = currencyFormat,
                                onClick = { selectedCustomerForDialog = customer }
                            )

                            if (idx < list.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = BrandBorder.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Detail & Quick Contact Modal Dialog
    selectedCustomerForDialog?.let { cust ->
        AlertDialog(
            onDismissRequest = { selectedCustomerForDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cust.initials,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MidnightNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = cust.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MidnightNavy)
                        Text(text = cust.customerTag, fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (cust.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = cust.phone, fontSize = 13.sp, color = MidnightNavy)
                        }
                    }
                    if (cust.email.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = cust.email, fontSize = 13.sp, color = MidnightNavy)
                        }
                    }
                    if (cust.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${cust.address}${if (cust.city.isNotBlank()) ", ${cust.city}" else ""}", fontSize = 13.sp, color = MidnightNavy)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Orders", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${cust.ordersCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MidnightNavy)
                        }
                        Column {
                            Text(text = "Total Spend", fontSize = 11.sp, color = TextMuted)
                            Text(text = currencyFormat.format(cust.totalSpent), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MidnightNavy)
                        }
                        Column {
                            Text(text = "Last Order", fontSize = 11.sp, color = TextMuted)
                            Text(text = cust.displayLastOrder, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MidnightNavy)
                        }
                    }

                    // Contact Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (cust.phone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val cleanPhone = cust.phone.replace("[^0-9+]".toRegex(), "")
                                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone"))
                                    context.startActivity(waIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                            }
                        }

                        Button(
                            onClick = {
                                onNavigateOrders(cust.name.ifBlank { cust.email })
                                selectedCustomerForDialog = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Orders", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerForDialog = null }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun CustomerListItem(
    customer: Customer,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Initials Avatar Circle (e.g. "AS", "MN", "RK", "DM")
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFDBEAFE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = customer.initials,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MidnightNavy
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Details Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = customer.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MidnightNavy,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${customer.ordersCount} orders · Last order ${customer.displayLastOrder}",
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currencyFormat.format(customer.totalSpent),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "total spend",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status Tag Pill (e.g. "High-value", "Returning", "New")
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Text(
                text = customer.customerTag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}
