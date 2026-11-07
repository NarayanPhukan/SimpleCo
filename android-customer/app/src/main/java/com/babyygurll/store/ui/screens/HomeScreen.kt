package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.R
import com.babyygurll.store.data.model.Product
import com.babyygurll.store.ui.components.BoutiqueHeader
import com.babyygurll.store.ui.components.BoutiqueImage
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

val BOUTIQUE_CATEGORIES = listOf(
    "all" to "All",
    "plushies" to "Plushies",
    "flowers" to "Flowers",
    "scrunchies" to "Scrunchies",
    "bags" to "Bags",
    "wearables" to "Wearables"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StoreViewModel,
    onProductClick: (Product) -> Unit,
    onNavigateToShop: () -> Unit = {},
    onNavigateToBag: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        // 1. Top Boutique Header: (BG) BabyyGurll BOUTIQUE [Bell & Bag icons]
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
            // 2. Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { onNavigateToSearch() }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        readOnly = true,
                        enabled = false,
                        placeholder = {
                            Text(
                                text = "Search handmade treasures",
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
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledBorderColor = SurfaceBorder,
                            disabledTextColor = BrandDarkNavy,
                            disabledPlaceholderColor = TextMuted,
                            disabledLeadingIconColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onNavigateToSearch() }
                    )
                }
            }

            // 3. Featured Hero Card: Split Navy + Craft Piece with "Shop Now ›" & dots
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandDarkNavy),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left section (Navy text & CTA)
                            Column(
                                modifier = Modifier
                                    .weight(1.15f)
                                    .fillMaxHeight()
                                    .padding(start = 18.dp, end = 8.dp, top = 16.dp, bottom = 14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "HANDMADE • JUST FOR YOU",
                                        color = Color(0xFF93C5FD),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Handcrafted\nwith Love.",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 23.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Little things, made beautifully.",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Shop Now Pill Button
                                    Surface(
                                        color = BrandPrimary,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.clickable { onNavigateToShop() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Shop Now",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    // Carousel Dots: [ — • • ]
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 14.dp, height = 4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color.White)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.4f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }

                            // Right section (Fixed Boutique Craft Hero Image)
                            Box(
                                modifier = Modifier
                                    .weight(0.95f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.hero_banner),
                                    contentDescription = "Handcrafted Piece",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // 4. Category Filter Chips: [All] + 8 Official Boutique Categories
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = if (selectedCategory == "all") 12.dp else 4.dp),
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
                                text = "✨ All",
                                color = if (isAllSelected) Color.White else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    items(com.babyygurll.store.data.model.BoutiqueCategoryData.categories) { cat ->
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

            // 4b. Dynamic Subcategory Row if a category is active
            if (selectedCategory != "all") {
                val currentCategory = com.babyygurll.store.data.model.BoutiqueCategoryData.getCategoryById(selectedCategory)
                if (currentCategory != null && currentCategory.subcategories.isNotEmpty()) {
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp, top = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                val isAllSubSelected = selectedSubcategory == "all"
                                Surface(
                                    color = if (isAllSubSelected) BrandPrimary else Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, if (isAllSubSelected) BrandPrimary else SurfaceBorder),
                                    modifier = Modifier.clickable { viewModel.selectSubcategory("all") }
                                ) {
                                    Text(
                                        text = "All ${currentCategory.name}",
                                        color = if (isAllSubSelected) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isAllSubSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            items(currentCategory.subcategories) { subcat ->
                                val isSubSelected = selectedSubcategory == subcat.id
                                Surface(
                                    color = if (isSubSelected) BrandPrimary else Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, if (isSubSelected) BrandPrimary else SurfaceBorder),
                                    modifier = Modifier.clickable { viewModel.selectSubcategory(subcat.id) }
                                ) {
                                    Text(
                                        text = subcat.name,
                                        color = if (isSubSelected) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section Header: CURATED FOR YOU / Featured Products [View all]
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CURATED FOR YOU",
                        color = BrandPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Featured Products",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = BrandDarkNavy
                        )
                        Text(
                            text = "View all",
                            color = BrandPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToShop() }
                        )
                    }
                }
            }

            // 6. 2-Column Product Grid in LazyColumn (chunked by 2)
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
                                text = "No treasures found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandDarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Waking up studio atelier...",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.fetchProducts() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandDarkNavy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Refresh Catalog", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                val chunkedProducts = products.chunked(2)
                items(chunkedProducts) { rowItems ->
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

/**
 * Product Card matching Screenshot 1:
 * Rounded card, square image with white circular heart button at top right,
 * title, price in bold navy, and "● In stock" blue tag.
 */
@Composable
fun ProductGridCard(
    product: Product,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onWishlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
    ) {
        Column {
            // Product Image Box with Floating Circular Heart Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF8FAFC))
            ) {
                BoutiqueImage(
                    imageUrl = product.resolvedPrimaryImage,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Circular Floating Wishlist Button
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                ) {
                    IconButton(
                        onClick = onWishlistClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) BrandPrimary else BrandDarkNavy,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Product Details
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = BrandDarkNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "₹${product.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = BrandDarkNavy
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stock indicator row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (product.inStock) BrandPrimary else StatusError)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (product.inStock) "In stock" else "Sold out",
                        fontSize = 11.sp,
                        color = if (product.inStock) BrandPrimary else StatusError,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
