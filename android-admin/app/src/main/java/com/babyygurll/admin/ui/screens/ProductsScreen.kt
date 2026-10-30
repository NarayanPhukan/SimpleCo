package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Product
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.ui.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    state: AdminUiState,
    serverUrl: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleStock: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onNewProduct: () -> Unit
) {
    val categories = listOf(
        "all" to "All (${state.products.size})",
        "plushies" to "Plushies",
        "floral" to "Floral",
        "bags" to "Bags",
        "wearables" to "Wearables",
        "accessories" to "Accessories"
    )

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Boutique Catalog",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSecondary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewProduct,
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Crochet Piece")
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = state.productSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by name, category, yarn...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (state.productSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BrandSurface,
                    unfocusedContainerColor = BrandSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (key, label) ->
                    val isSelected = state.selectedProductCategory == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Products List
            if (state.filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🧶", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.productSearchQuery.isNotEmpty())
                                "No items match '${state.productSearchQuery}'"
                            else
                                "No products in this category",
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
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(state.filteredProducts) { product ->
                        ProductItemCard(
                            product = product,
                            serverUrl = serverUrl,
                            onToggleStock = { onToggleStock(product) },
                            onEdit = { onEditProduct(product) },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = {
                Text("Are you sure you want to remove '${productToDelete?.name}' from the boutique catalog? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { onDeleteProduct(it.id) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandAccentRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    serverUrl: String,
    onToggleStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = if (product.image.startsWith("http")) product.image
            else "${serverUrl.trimEnd('/')}/${product.image.trimStart('/')}"

            AsyncImage(
                model = imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandPrimaryLight)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (!product.badge.isNullOrBlank()) {
                    Surface(
                        color = BrandPrimaryLight,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = product.badge,
                            color = BrandPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "₹${product.price.toInt()} • ${product.categoryName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Switch(
                        checked = product.inStock,
                        onCheckedChange = { onToggleStock() },
                        modifier = Modifier.scale(0.75f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (product.inStock) "In Stock" else "Sold Out",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.inStock) StatusDelivered else BrandAccentRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = BrandAccentRed)
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(0.dp)
)

