package com.babyygurll.store.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch

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

    val variants = remember(product) { product.effectiveVariants }
    var selectedVariantIndex by remember(product.effectiveId) { mutableIntStateOf(0) }
    val activeVariant = variants.getOrNull(selectedVariantIndex) ?: variants.firstOrNull()

    val imageList = remember(product, activeVariant) {
        val list = mutableListOf<String>()
        if (!activeVariant?.image.isNullOrBlank()) {
            list.add(activeVariant!!.image!!)
        }
        list.addAll(product.allImages.filter { it.isNotBlank() })
        if (list.isNotEmpty()) list.distinct() else listOf(product.resolvedPrimaryImage)
    }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { imageList.size.coerceAtLeast(1) })
    var careExpanded by remember { mutableStateOf(false) }

    val activeStock = activeVariant?.stock ?: (if (product.inStock) 5 else 0)
    val isAvailable = activeStock > 0 && product.inStock

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
                            viewModel.addToCart(
                                product = product,
                                quantity = 1,
                                selectedVariant = activeVariant,
                                selectedColor = activeVariant?.color
                            )
                        },
                        enabled = isAvailable,
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
                            viewModel.addToCart(
                                product = product,
                                quantity = 1,
                                selectedVariant = activeVariant,
                                selectedColor = activeVariant?.color
                            )
                            onBuyNow()
                        },
                        enabled = isAvailable,
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
            // Hero Image Carousel Area with HorizontalPager, Controls & Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color(0xFFF8FAFC))
            ) {
                // Interactive Swipeable HorizontalPager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageImage = imageList.getOrNull(page) ?: product.resolvedPrimaryImage
                    BoutiqueImage(
                        imageUrl = pageImage,
                        contentDescription = "${product.name} - Slide ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Previous Image Chevron Button
                if (imageList.size > 1 && pagerState.currentPage > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.88f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(38.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous photo",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Next Image Chevron Button
                if (imageList.size > 1 && pagerState.currentPage < imageList.size - 1) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.88f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next photo",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Top Floating Action Buttons (Back & Wishlist)
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
                            val isSelected = index == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(width = if (isSelected) 18.dp else 5.dp, height = 5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }

                    // Page Pill: "1/4"
                    Surface(
                        color = Color(0xCC0A1940),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${imageList.size.coerceAtLeast(1)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Thumbnail Row Strip (when multiple photos exist)
            if (imageList.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    imageList.forEachIndexed { index, imgUrl ->
                        val isSelected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BrandPrimary else SurfaceBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        ) {
                            BoutiqueImage(
                                imageUrl = imgUrl,
                                contentDescription = "Photo thumbnail ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Body Content
            Column(modifier = Modifier.padding(20.dp)) {
                // Availability Pill Badge
                Surface(
                    color = if (isAvailable) Color(0xFFEBF3FC) else Color(0xFFFEE2E2),
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
                                .background(if (isAvailable) BrandPrimary else Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAvailable) "AVAILABLE • $activeStock IN STOCK" else "OUT OF STOCK",
                            color = if (isAvailable) BrandPrimary else Color(0xFFDC2626),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val currentPrice = activeVariant?.price ?: product.price
                        Text(
                            text = "₹${currentPrice.toInt()}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = BrandDarkNavy
                        )
                        val origPrice = activeVariant?.originalPrice ?: product.originalPrice
                        if (origPrice != null && origPrice > currentPrice) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${origPrice.toInt()}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = TextMuted
                            )
                        }
                    }

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

                // Dynamic Variant Selector Chips
                if (variants.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SELECT OPTION / VARIANT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkNavy,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(variants.size) { idx ->
                            val v = variants[idx]
                            val isSelected = idx == selectedVariantIndex
                            val isItemAvailable = v.stock > 0

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) BrandPrimary else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = isItemAvailable) {
                                        selectedVariantIndex = idx
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!v.colorHex.isNullOrBlank()) {
                                        val parsedColor = try {
                                            Color(android.graphics.Color.parseColor(v.colorHex))
                                        } catch (e: Exception) {
                                            BrandPrimary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(parsedColor)
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column {
                                        Text(
                                            text = v.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isItemAvailable) (if (isSelected) BrandPrimary else BrandDarkNavy) else TextMuted
                                        )
                                        Text(
                                            text = if (isItemAvailable) "₹${v.price.toInt()} · ${v.stock} left" else "Out of stock",
                                            fontSize = 11.sp,
                                            color = if (isSelected) BrandPrimary.copy(alpha = 0.85f) else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
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
