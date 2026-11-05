package com.babyygurll.store.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.Product
import com.babyygurll.store.ui.components.BoutiqueImage
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun ProductDetailScreen(
    product: Product,
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onBuyNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isWishlisted = wishlistIds.contains(product.effectiveId)

    val imageList = remember(product) {
        val list = product.allImages.filter { it.isNotBlank() }
        if (list.isNotEmpty()) list else listOf(product.resolvedPrimaryImage)
    }
    var currentImageIndex by remember { mutableIntStateOf(0) }
    var careExpanded by remember { mutableStateOf(false) }

    Scaffold(
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Add to Cart (Royal Blue)
                    Button(
                        onClick = {
                            viewModel.addToCart(product, 1)
                        },
                        enabled = product.inStock,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Add to Cart",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Buy Now (Dark Navy)
                    Button(
                        onClick = {
                            viewModel.addToCart(product, 1)
                            onBuyNow()
                        },
                        enabled = product.inStock,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandDarkNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Buy Now",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Area with Floating Back & Wishlist Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color(0xFFF8FAFC))
            ) {
                // Product Image
                val currentImage = imageList.getOrNull(currentImageIndex) ?: product.resolvedPrimaryImage
                BoutiqueImage(
                    imageUrl = currentImage,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Floating Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular White Back Button
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onBack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Circular White Wishlist Button
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { viewModel.toggleWishlist(product) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) BrandPrimary else BrandDarkNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Bottom Carousel Indicators & Page Number Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Center Dots: [ — • • • ]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        val count = imageList.size.coerceAtLeast(1)
                        repeat(count) { index ->
                            val isSelected = index == currentImageIndex
                            Box(
                                modifier = Modifier
                                    .size(width = if (isSelected) 16.dp else 4.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
                                    .clickable { currentImageIndex = index }
                            )
                        }
                    }

                    // Page Pill: "1/4"
                    Surface(
                        color = Color(0xCC0A1940),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${currentImageIndex + 1}/${imageList.size.coerceAtLeast(1)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Body Content
            Column(modifier = Modifier.padding(20.dp)) {
                // Availability Pill Badge
                Surface(
                    color = Color(0xFFEBF3FC),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (product.inStock) "AVAILABLE • READY TO SHIP" else "MADE TO ORDER",
                            color = BrandPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Title
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = BrandDarkNavy
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price & Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = BrandDarkNavy
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = BrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating} (${product.reviewsCount.coerceAtLeast(24)} reviews)",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = product.description.ifBlank {
                        "A cloud-soft companion, carefully crocheted by hand with the gentlest cotton yarn. Each piece has its own tiny personality."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // 2x2 Specs Grid matching Screenshot 2
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecCell(
                            label = "YARN MATERIAL",
                            value = "100% Cotton",
                            modifier = Modifier.weight(1f)
                        )
                        SpecCell(
                            label = "DIMENSIONS",
                            value = "22 × 14 cm",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SpecCell(
                            label = "COLOUR",
                            value = product.categoryName.ifBlank { "Cloud White & Blue" },
                            modifier = Modifier.weight(1f)
                        )
                        SpecCell(
                            label = "PREPARATION",
                            value = product.leadTime ?: "2–3 business days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Care Instructions Accordion
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { careExpanded = !careExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Care instructions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDarkNavy
                        )
                        Icon(
                            imageVector = if (careExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = careExpanded) {
                        Text(
                            text = "Gently hand wash in cool water. Reshape and lay flat to dry. Do not bleach.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WhatsApp Crafter Concierge
                OutlinedButton(
                    onClick = {
                        val msg = "Hi BabyyGurll! 🌸 I'm interested in the handmade \"${product.name}\" (₹${product.price.toInt()}) and wanted to ask about custom yarn colors!"
                        val uri = Uri.parse("https://wa.me/916002839514?text=${Uri.encode(msg)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                    border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💬 Chat with Crafter on WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun SpecCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BrandDarkNavy
        )
    }
}
