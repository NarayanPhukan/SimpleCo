package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.BoutiqueCategoryData
import com.babyygurll.store.data.model.Product
import com.babyygurll.store.ui.components.BoutiqueHeader
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun CategoriesScreen(
    viewModel: StoreViewModel,
    onProductClick: (Product) -> Unit,
    onNavigateToBag: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()
    val cartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    val activeCategoryObj = remember(selectedCategory) {
        if (selectedCategory != "all") BoutiqueCategoryData.getCategoryById(selectedCategory) else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        // Top Header
        BoutiqueHeader(
            cartBadgeCount = cartCount,
            notificationBadgeCount = unreadNotifications,
            onBagClick = onNavigateToBag,
            onNotificationClick = onNavigateToNotifications
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header: THE COLLECTION / Find your piece
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BOUTIQUE DEPARTMENTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (activeCategoryObj != null) "${activeCategoryObj.icon} ${activeCategoryObj.name}" else "Explore Collections",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = BrandDarkNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (activeCategoryObj != null) "${activeCategoryObj.subcategories.size} artisan craft subcategories" else "8 handcrafted departments · Thoughtfully made",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "Search across all categories...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }
            }

            // Primary Category Chips Row (All 8 official boutique categories)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val isAllSelected = selectedCategory == "all"
                        Surface(
                            color = if (isAllSelected) BrandDarkNavy else Color.White,
                            shape = RoundedCornerShape(20.dp),
                            border = if (isAllSelected) null else BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.clickable { viewModel.selectCategory("all") }
                        ) {
                            Text(
                                text = "✨ All Departments",
                                color = if (isAllSelected) Color.White else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    items(BoutiqueCategoryData.categories) { cat ->
                        val isSelected = selectedCategory == cat.id
                        Surface(
                            color = if (isSelected) BrandDarkNavy else Color.White,
                            shape = RoundedCornerShape(20.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.clickable { viewModel.selectCategory(cat.id) }
                        ) {
                            Text(
                                text = "${cat.icon} ${cat.name}",
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Dynamic Subcategory Chips Row (When department is active)
            if (activeCategoryObj != null && activeCategoryObj.subcategories.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "SUBCATEGORIES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                val isAllSub = selectedSubcategory == "all"
                                Surface(
                                    color = if (isAllSub) BrandPrimary else Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isAllSub) null else BorderStroke(1.dp, SurfaceBorder),
                                    modifier = Modifier.clickable { viewModel.selectSubcategory("all") }
                                ) {
                                    Text(
                                        text = "All ${activeCategoryObj.name}",
                                        color = if (isAllSub) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isAllSub) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            items(activeCategoryObj.subcategories) { sub ->
                                val isSubSelected = selectedSubcategory == sub.id
                                Surface(
                                    color = if (isSubSelected) BrandPrimary else Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isSubSelected) null else BorderStroke(1.dp, SurfaceBorder),
                                    modifier = Modifier.clickable { viewModel.selectSubcategory(sub.id) }
                                ) {
                                    Text(
                                        text = sub.name,
                                        color = if (isSubSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Department Explorer Grid (Shown when "all" is selected so users can browse all 8 departments visually)
            if (selectedCategory == "all" && searchQuery.isBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "QUICK DEPARTMENT DIRECTORY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                val categoryPairs = BoutiqueCategoryData.categories.chunked(2)
                items(categoryPairs) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (cat in pair) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectCategory(cat.id) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = cat.icon, fontSize = 24.sp)
                                        Text(
                                            text = "${cat.subcategories.size} types",
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDarkNavy,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cat.subcategories.take(2).joinToString(", ") { it.name } + "...",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Meta Row: X handmade pieces · Sort / Active Filter tag
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${products.size} handmade pieces",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        if (selectedCategory != "all" || selectedSubcategory != "all") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = BrandPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    viewModel.selectCategory("all")
                                    viewModel.selectSubcategory("all")
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Filtered", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Sort: Featured ›",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkNavy
                    )
                }
            }

            // 2-Column Product Grid
            if (isLoading && products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                }
            } else if (products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌸", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No items in this collection yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandDarkNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting another department or explore all creations.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.selectCategory("all")
                                    viewModel.selectSubcategory("all")
                                    viewModel.setSearchQuery("")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandDarkNavy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View All Products", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                val chunked = products.chunked(2)
                items(chunked) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (product in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductGridCard(
                                    product = product,
                                    isWishlisted = wishlistIds.contains(product.effectiveId),
                                    onProductClick = { onProductClick(product) },
                                    onWishlistClick = { viewModel.toggleWishlist(product) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
