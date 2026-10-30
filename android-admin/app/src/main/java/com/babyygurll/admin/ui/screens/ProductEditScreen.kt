package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Product
import com.babyygurll.admin.data.model.ProductCreateRequest
import com.babyygurll.admin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    product: Product?,
    serverUrl: String,
    onBack: () -> Unit,
    onSave: (ProductCreateRequest, String?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "plushies") }
    var priceText by remember { mutableStateOf(product?.price?.toInt()?.toString() ?: "") }
    var origPriceText by remember { mutableStateOf(product?.originalPrice?.toInt()?.toString() ?: "") }
    var badge by remember { mutableStateOf(product?.badge ?: "Handcrafted") }
    var imageUrl by remember { mutableStateOf(product?.image ?: "/assets/images/strawberry-cow.jpg") }
    var leadTime by remember { mutableStateOf(product?.leadTime ?: "Ready to ship (1-2 days)") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var inStock by remember { mutableStateOf(product?.inStock ?: true) }

    val categories = listOf(
        "plushies" to "Amigurumi Plushies",
        "floral" to "Floral & Home",
        "bags" to "Bags & Totes",
        "wearables" to "Wearables",
        "accessories" to "Accessories"
    )

    val photoPresets = listOf(
        "Strawberry Cow" to "/assets/images/strawberry-cow.jpg",
        "Tulip Bouquet" to "/assets/images/tulip-bouquet.jpg",
        "Granny Bag" to "/assets/images/granny-bag.jpg",
        "Cardigan" to "/assets/images/crochet-cardigan.jpg",
        "Lavender Bunny" to "/assets/images/lavender-bunny.jpg",
        "Coasters Set" to "/assets/images/crochet-coasters.jpg",
        "Bucket Hat" to "/assets/images/bucket-hat.jpg",
        "Berry Keychain" to "/assets/images/strawberry-keychain.jpg"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (product != null) "Edit Crochet Piece" else "Add New Crochet Piece",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSecondary)
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl
                        else "${serverUrl.trimEnd('/')}/${imageUrl.trimStart('/')}"

                        AsyncImage(
                            model = fullImageUrl,
                            contentDescription = name,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandPrimaryLight)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = if (name.isNotBlank()) name else "Untitled Crochet Piece",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "₹${priceText.ifBlank { "0" }} • $category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (inStock) "✓ Active in Store" else "✕ Marked as Sold Out",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (inStock) StatusDelivered else BrandAccentRed
                            )
                        }
                    }
                }
            }

            // Piece Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Piece Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Title *") },
                            placeholder = { Text("e.g. Pastel Blossom Coaster Set") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Chips
                        Text("Category", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { (catId, catLabel) ->
                                FilterChip(
                                    selected = category == catId,
                                    onClick = { category = catId },
                                    label = { Text(catLabel) }
                                )
                            }
                        }

                        // Price and Original Price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Price (₹) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = origPriceText,
                                onValueChange = { origPriceText = it },
                                label = { Text("Compare Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = badge,
                            onValueChange = { badge = it },
                            label = { Text("Badge Label") },
                            placeholder = { Text("Bestseller, Top Gift, Artisan Heirloom...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = leadTime,
                            onValueChange = { leadTime = it },
                            label = { Text("Lead Time") },
                            placeholder = { Text("Ready to ship (1-2 days)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Artisan Story") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Inventory In Stock", fontWeight = FontWeight.Medium)
                            Switch(checked = inStock, onCheckedChange = { inStock = it })
                        }
                    }
                }
            }

            // Photography Presets
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Product Photography",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text("Select Curated Studio Preset:", style = MaterialTheme.typography.labelSmall)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            photoPresets.forEach { (title, url) ->
                                FilterChip(
                                    selected = imageUrl == url,
                                    onClick = { imageUrl = url },
                                    label = { Text(title) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Image URL or Path") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
                        val parsedOrigPrice = origPriceText.toDoubleOrNull()
                        val catObj = categories.find { it.first == category }
                        val req = ProductCreateRequest(
                            name = name.trim(),
                            category = category,
                            categoryName = catObj?.second,
                            price = parsedPrice,
                            originalPrice = parsedOrigPrice,
                            image = imageUrl.trim(),
                            badge = badge.trim().ifBlank { null },
                            leadTime = leadTime.trim().ifBlank { null },
                            description = description.trim(),
                            inStock = inStock
                        )
                        onSave(req, product?.id)
                    },
                    enabled = name.isNotBlank() && priceText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Piece to Boutique", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

