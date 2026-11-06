package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Product
import com.babyygurll.admin.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product?,
    serverUrl: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleStock: () -> Unit,
    onDelete: () -> Unit
) {
    if (product == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftIvory),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Piece details not found", color = TextMuted, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)) {
                    Text("Return to Catalog", color = Color.White)
                }
            }
        }
        return
    }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showVariantsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftIvory
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MidnightNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Title & SKU
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 17.sp
                        )
                        Text(
                            text = product.sku,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    // Three Dots Menu Button
                    Box {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                .clickable { showMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Menu",
                                tint = MidnightNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
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
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = SoftIvory
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Image Card with Counter Badge (1/4)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                val imageUrl = if (product.image.startsWith("http")) {
                    product.image
                } else if (product.image.isNotBlank()) {
                    "${serverUrl.trimEnd('/')}${if (product.image.startsWith("/")) "" else "/"}${product.image}"
                } else {
                    ""
                }

                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Image Counter Badge (Bottom Right: "1 / 4")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC0B1953),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "1 / 4",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Primary Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Status Badge Pill
                        val status = product.displayStatus
                        val (bgColor, textColor) = when (status) {
                            "Published" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                            "Low stock" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                            "Out of stock" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                            else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor
                        ) {
                            Text(
                                text = status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${product.categoryName} · Made to order",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currencyFormat.format(product.price),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )

                        val origPrice = product.displayOriginalPrice
                        if (origPrice > product.price) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currencyFormat.format(origPrice),
                                fontSize = 14.sp,
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }

                        product.discountPercentage?.let { pct ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "$pct% off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Navy KPI Stats Banner (Available stock | Production time | Units sold)
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
                    // Available Stock
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Available stock",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${product.stockCount} units",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Production Time
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Production time",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = product.productionDays,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Units Sold
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Units sold",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${product.unitsSold}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Product Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Product information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.description.ifBlank {
                            "A structured hand–crocheted shoulder bag made with premium milk cotton yarn. Finished with a lined interior, magnetic closure and adjustable strap."
                        },
                        fontSize = 13.sp,
                        color = MidnightNavy.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2x2 Grid of Metadata
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Materials", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Milk cotton yarn, cotton lining",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Yarn colour", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = product.colors.firstOrNull()?.name ?: "Midnight blue",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Customisation", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Available",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Last updated", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "18 Jun 2025",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy
                            )
                        }
                    }
                }
            }

            // Variants Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Variants",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "View all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalBlue,
                            modifier = Modifier.clickable { showVariantsDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val colorName = product.colors.firstOrNull()?.name ?: "Ocean Blue"

                    // Variant 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Small / $colorName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MidnightNavy
                        )
                        Text(
                            text = "${currencyFormat.format(product.price)} · 3 left",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightNavy
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = BrandBorder.copy(alpha = 0.6f)
                    )

                    // Variant 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Medium / $colorName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MidnightNavy
                        )
                        Text(
                            text = "${currencyFormat.format(product.price + 300)} · 5 left",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Piece", fontWeight = FontWeight.Bold, color = CrimsonAccent) },
            text = { Text("Are you sure you want to remove \"${product.name}\" from your boutique catalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Variants Dialog
    if (showVariantsDialog) {
        val colorName = product.colors.firstOrNull()?.name ?: "Ocean Blue"
        AlertDialog(
            onDismissRequest = { showVariantsDialog = false },
            title = { Text("All SKU Variants", fontWeight = FontWeight.Bold, color = MidnightNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Small / $colorName: ${currencyFormat.format(product.price)} (3 units remaining)")
                    Text("• Medium / $colorName: ${currencyFormat.format(product.price + 300)} (5 units remaining)")
                    Text("• Large / $colorName: ${currencyFormat.format(product.price + 600)} (Pre-order / 0 units)")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVariantsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }
}
