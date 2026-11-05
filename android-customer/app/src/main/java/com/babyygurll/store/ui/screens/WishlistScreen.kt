package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.Product
import com.babyygurll.store.ui.components.BoutiqueImage
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun WishlistScreen(
    viewModel: StoreViewModel,
    onProductClick: (Product) -> Unit,
    onExploreClick: () -> Unit,
    onNavigateToBag: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.products.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    val wishlistedProducts = remember(allProducts, wishlistIds) {
        allProducts.filter { wishlistIds.contains(it.effectiveId) }
    }

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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "My Wishlist",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = BrandDarkNavy
                        )
                        Text(
                            text = "${wishlistedProducts.size} saved pieces",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onNavigateToBag,
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = BrandPrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = "$cartCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = "Shopping Bag",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(24.dp)
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
                .padding(horizontal = 16.dp)
        ) {
            // Eyebrow Row: YOUR EDIT / Handpicked with love (Screenshot 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR EDIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Handpicked with love",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (wishlistedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Your wishlist is empty",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = BrandDarkNavy
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap the heart on any piece you love to save it here for later.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onExploreClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Explore the collection",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                // 2-Column Wishlist Grid (Screenshot 3)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(wishlistedProducts, key = { it.effectiveId }) { product ->
                        WishlistProductCard(
                            product = product,
                            onProductClick = { onProductClick(product) },
                            onRemoveFromWishlist = { viewModel.toggleWishlist(product) },
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Product Card matching Wishlist in Screenshot 3:
 * Card with circular filled blue heart, stock indicator dot, price,
 * and dark navy outlined button: [ 👜 Add to Cart ]
 */
@Composable
fun WishlistProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onRemoveFromWishlist: () -> Unit,
    onAddToCart: () -> Unit,
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
            // Image Box with Filled Royal Blue Heart
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

                // Filled Blue Heart Button
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
                        onClick = onRemoveFromWishlist,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Remove",
                            tint = BrandPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Info & Add to Cart button
            Column(modifier = Modifier.padding(12.dp)) {
                // Stock Dot row
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "₹${product.price.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Outlined [ 👜 Add to Cart ] Button (Screenshot 3)
                OutlinedButton(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandDarkNavy),
                    border = BorderStroke(1.dp, BrandDarkNavy),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = BrandDarkNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add to Cart",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkNavy
                    )
                }
            }
        }
    }
}
