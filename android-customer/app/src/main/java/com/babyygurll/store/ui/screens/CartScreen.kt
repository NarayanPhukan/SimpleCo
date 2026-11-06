package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalShipping
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
import com.babyygurll.store.data.model.CartItem
import com.babyygurll.store.ui.components.BoutiqueImage
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun CartScreen(
    viewModel: StoreViewModel,
    onCheckoutClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val discount by viewModel.cartDiscount.collectAsState()
    val shipping by viewModel.cartShipping.collectAsState()
    val grandTotal by viewModel.cartGrandTotal.collectAsState()

    var couponInput by remember { mutableStateOf("") }
    val totalCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Circular Back Button
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onExploreClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandDarkNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center: "My Shopping Bag"
                    Text(
                        text = "My Shopping Bag",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = BrandDarkNavy
                    )

                    // Right: "2 items"
                    Text(
                        text = if (totalCount > 0) "$totalCount items" else "",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCheckoutClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Proceed to Checkout · ₹${grandTotal.toInt()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            // Empty State matching Screenshot 5
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Soft light blue squircle with blue bag icon
                    Surface(
                        color = Color(0xFFEBF5FF),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your bag is waiting",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = BrandDarkNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Discover something handmade to make your\neveryday a little more beautiful.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onExploreClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 8.dp)
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
            // Shopping Bag with Items matching Screenshot 3
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(BrandBackground)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                // 1. Free Delivery Banner (Screenshot 3)
                item {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalShipping,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "You've unlocked free delivery",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = "Handmade happiness, delivered free",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 2. Cart Items Container Card (Screenshot 3)
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            cartItems.forEachIndexed { index, item ->
                                CartItemRow(
                                    item = item,
                                    onIncrease = { viewModel.updateCartQuantity(item.cartKey, item.quantity + 1) },
                                    onDecrease = { viewModel.updateCartQuantity(item.cartKey, item.quantity - 1) },
                                    onRemove = { viewModel.removeFromCart(item.cartKey) }
                                )

                                if (index < cartItems.size - 1) {
                                    HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }

                // 3. Offers & Coupons Card (Screenshot 3)
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = BrandDarkNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Offers & Coupons",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Coupon Code Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it.uppercase() },
                                    placeholder = {
                                        Text(
                                            text = if (appliedCoupon != null) appliedCoupon!!.code else "Enter coupon code",
                                            fontSize = 13.sp,
                                            color = TextMuted
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedBorderColor = BrandPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clickable {
                                            if (couponInput.isNotBlank()) {
                                                viewModel.applyCoupon(couponInput)
                                                couponInput = ""
                                            }
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = "Apply",
                                            color = BrandPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Available Coupon Dashed Pill Box: HANDMADE200 / Save ₹200
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.applyCoupon("HANDMADE200") }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "HANDMADE200",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary
                                        )
                                        Text(
                                            text = "Save ₹200 on orders above ₹1,999",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Text(
                                        text = "APPLY",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Price Details Card (Screenshot 3)
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Price Details",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            PriceRow(label = "Subtotal", value = "₹${subtotal.toInt()}")
                            Spacer(modifier = Modifier.height(8.dp))
                            PriceRow(
                                label = "Coupon discount",
                                value = if (discount > 0) "- ₹${discount.toInt()}" else "- ₹0",
                                valueColor = if (discount > 0) StatusSuccess else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PriceRow(
                                label = "Shipping",
                                value = "FREE",
                                valueColor = BrandPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order Total",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = "₹${grandTotal.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cart Item Row matching Screenshot 3
 */
@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            modifier = Modifier.size(72.dp)
        ) {
            val img = item.selectedVariant?.image?.ifBlank { null } ?: item.product.resolvedPrimaryImage
            BoutiqueImage(
                imageUrl = img,
                contentDescription = item.product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDarkNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val variantLabel = listOfNotNull(
                item.selectedVariant?.name?.ifBlank { null },
                item.selectedColor?.ifBlank { null }
            ).firstOrNull() ?: item.customNote.ifBlank { null } ?: "Standard edition"
            Text(
                text = variantLabel,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "₹${item.unitPrice.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDarkNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stepper Pill: [ — 1 + ]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Remove Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onRemove() }
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remove",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Remove",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun PriceRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
