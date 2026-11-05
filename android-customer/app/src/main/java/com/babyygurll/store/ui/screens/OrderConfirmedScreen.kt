package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.data.model.Order
import com.babyygurll.store.ui.components.BoutiqueImage
import com.babyygurll.store.ui.theme.*

@Composable
fun OrderConfirmedScreen(
    order: Order,
    onTrackOrder: () -> Unit,
    onContinueShopping: () -> Unit,
    onCancelOrder: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order confirmed",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = BrandDarkNavy
                    )

                    IconButton(
                        onClick = onContinueShopping,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BrandDarkNavy
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTrackOrder,
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
                            text = "Track My Order",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onContinueShopping,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandDarkNavy),
                        border = BorderStroke(1.5.dp, BrandDarkNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Continue Shopping",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onCancelOrder != null) {
                        TextButton(
                            onClick = onCancelOrder,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cancel this order",
                                color = StatusError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Animated Success Checkmark Ring matching Screenshot 4
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE))
                )
                // Inner blue circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirmed",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // Order Number & Headline
            val orderRef = order.orderId.ifBlank { "BG" + order.id.takeLast(6) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ORDER #$orderRef",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your order is confirmed!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = BrandDarkNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Thank you for choosing handmade. Your little bundle of happiness is getting ready.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Card 1: Order Meta Card (Screenshot 4)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetaCell(
                            label = "ORDER DATE",
                            value = order.dateFormatted.ifBlank { "Today" },
                            modifier = Modifier.weight(1f)
                        )
                        MetaCell(
                            label = "PAYMENT",
                            value = "${order.paymentMethod} • Paid",
                            valueColor = BrandPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetaCell(
                            label = "EST. DELIVERY",
                            value = "3–5 Business Days",
                            modifier = Modifier.weight(1f)
                        )
                        MetaCell(
                            label = "TOTAL",
                            value = "₹${order.total.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Card 2: YOUR PIECES Card (Screenshot 4)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "YOUR PIECES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    order.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.size(52.dp)
                            ) {
                                BoutiqueImage(
                                    imageUrl = item.image,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = "Qty ${item.quantity} • Handmade",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = "₹${(item.price * item.quantity).toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )
                        }

                        if (index < order.items.size - 1) {
                            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                        }
                    }
                }
            }

            // Card 3: DELIVERING TO Card (Screenshot 4)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DELIVERING TO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            val addr = order.shippingAddress
                            Text(
                                text = "${addr?.recipientName ?: "Customer"} • ${addr?.phone ?: ""}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${addr?.street ?: ""}, ${addr?.city ?: ""}, ${addr?.state ?: ""} — ${addr?.zip ?: ""}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MetaCell(label: String, value: String, valueColor: Color = BrandDarkNavy, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
