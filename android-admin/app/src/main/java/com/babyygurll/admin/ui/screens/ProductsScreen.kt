package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Product
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState
import com.babyygurll.admin.util.AdminProductImage
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    state: AdminUiState,
    serverUrl: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onSelectProduct: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onToggleStock: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onNewProduct: () -> Unit
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val filterPills = listOf("All", "Published", "Draft", "Low stock")
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var sortAscending by remember { mutableStateOf(false) }

    val displayedProducts = remember(state.filteredProducts, sortAscending) {
        if (sortAscending) {
            state.filteredProducts.sortedBy { it.price }
        } else {
            state.filteredProducts
        }
    }

    Scaffold(
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: Title ("Products"), Subtitle ("46 products in your catalogue"), "+" blue button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Products",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${state.products.size} products in your catalogue",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                // Add New Product Button (+ Blue Box)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoyalBlue,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNewProduct)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Product",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Search Bar: Search by name, SKU or category
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
                        value = state.productSearchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "Search by name, SKU or category",
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
                    if (state.productSearchQuery.isNotEmpty()) {
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

            // Filter Pills Row (All, Published, Draft, Low stock)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterPills.forEach { pill ->
                    val isSelected = state.selectedProductFilter.equals(pill, true)
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

            // Meta Info Line: "46 products" | "Recently added"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayedProducts.size} products",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { sortAscending = !sortAscending }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sort,
                        contentDescription = "Sort",
                        tint = MidnightNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (sortAscending) "Price: Low to High" else "Recently added",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MidnightNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products Catalog List
            if (displayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pieces found in catalogue",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedProducts, key = { it.id.ifBlank { it.mongoId ?: it.name } }) { product ->
                        ProductCatalogueCard(
                            product = product,
                            serverUrl = serverUrl,
                            currencyFormat = currencyFormat,
                            onClick = { onSelectProduct(product) },
                            onEdit = { onEditProduct(product) },
                            onToggleStock = { onToggleStock(product) },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Piece", fontWeight = FontWeight.Bold, color = CrimsonAccent) },
            text = { Text("Are you sure you want to permanently remove \"${prod.name}\" from the studio catalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = prod.mongoId ?: prod.id
                        onDeleteProduct(id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun ProductCatalogueCard(
    product: Product,
    serverUrl: String,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleStock: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Thumbnail
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                AdminProductImage(
                    model = product.image,
                    contentDescription = product.name,
                    serverUrl = serverUrl,
                    fallbackEmoji = "🧶",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${product.categoryName} · ${product.sku}",
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currencyFormat.format(product.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${product.stockCount} in stock",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Status Badge Pill
                val status = product.displayStatus
                val (bgColor, textColor) = when (status) {
                    "Published" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                    "Low stock" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                    "Out of stock" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                    else -> Color(0xFFF1F5F9) to Color(0xFF64748B) // Draft
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bgColor
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Three-Dots Menu Button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = TextMuted
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Details") },
                        leadingIcon = { Icon(Icons.Outlined.Visibility, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Piece") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (product.inStock) "Mark Out of Stock" else "Mark In Stock") },
                        leadingIcon = { Icon(Icons.Outlined.Inventory, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onToggleStock()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete Piece", color = CrimsonAccent) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = CrimsonAccent) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
