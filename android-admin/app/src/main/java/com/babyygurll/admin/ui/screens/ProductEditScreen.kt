package com.babyygurll.admin.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Product
import com.babyygurll.admin.data.model.ProductCreateRequest
import com.babyygurll.admin.ui.theme.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    product: Product?,
    serverUrl: String,
    onBack: () -> Unit,
    onSave: (ProductCreateRequest, String?) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "plushies") }
    var priceText by remember { mutableStateOf(product?.price?.toInt()?.toString() ?: "") }
    var origPriceText by remember { mutableStateOf(product?.originalPrice?.toInt()?.toString() ?: "") }
    var badge by remember { mutableStateOf(product?.badge ?: "Handcrafted") }
    var imageUrl by remember { mutableStateOf(product?.image ?: "/assets/images/strawberry-cow.jpg") }
    var leadTime by remember { mutableStateOf(product?.leadTime ?: "Ready to ship (1-2 days)") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var inStock by remember { mutableStateOf(product?.inStock ?: true) }

    // Active mode: 0 = Edit Details Form, 1 = Customer Point of View Preview
    var selectedTab by remember { mutableStateOf(0) }

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

    // Local Device Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val maxDim = 800
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scale = if (width > maxDim || height > maxDim) {
                        min(maxDim.toFloat() / width, maxDim.toFloat() / height)
                    } else 1.0f

                    val scaledBitmap = if (scale < 1.0f) {
                        Bitmap.createScaledBitmap(
                            originalBitmap,
                            (width * scale).toInt(),
                            (height * scale).toInt(),
                            true
                        )
                    } else originalBitmap

                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                    imageUrl = "data:image/jpeg;base64,$base64String"
                }
            } catch (e: Exception) {
                // Keep existing image on error
            }
        }
    }

    val resolvedImageUrl = remember(imageUrl, serverUrl) {
        when {
            imageUrl.startsWith("data:") -> imageUrl
            imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> imageUrl
            else -> "${serverUrl.trimEnd('/')}/${imageUrl.trimStart('/')}"
        }
    }

    val priceVal = priceText.toDoubleOrNull() ?: 0.0
    val origPriceVal = origPriceText.toDoubleOrNull()
    val discountPercent = if (origPriceVal != null && origPriceVal > priceVal && priceVal > 0) {
        (((origPriceVal - priceVal) / origPriceVal) * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (product != null) "Edit Piece" else "Add New Piece",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Tab Switcher: [ Edit Details ] | [ Customer View Preview ]
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = RoyalBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Details", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Customer Preview", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // EDIT DETAILS FORM
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Product Photography & Device Selection
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
                                    text = "Product Photography",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Image Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BrandPrimaryLight)
                                            .border(1.dp, BrandPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = resolvedImageUrl,
                                            contentDescription = "Selected photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Button(
                                            onClick = { photoPickerLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = RoyalBlue,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Choose from Device",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }

                                        Text(
                                            text = if (imageUrl.startsWith("data:")) "✓ Custom device photo selected" else "Select from gallery or pick a preset",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (imageUrl.startsWith("data:")) Color(0xFF2E7D32) else CharcoalGray.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                        )
                                    }
                                }

                                Text("Or Choose Studio Preset:", style = MaterialTheme.typography.labelSmall)

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
                                            label = { Text(title) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = RoyalBlue,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color.White,
                                                labelColor = CharcoalGray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Product Details Card
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
                                            label = { Text(catLabel) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = RoyalBlue,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color.White,
                                                labelColor = CharcoalGray
                                            )
                                        )
                                    }
                                }

                                // Price and Compare Price
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
                                    placeholder = { Text("Bestseller, Top Gift, Handcrafted...") },
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
                                    Column {
                                        Text("Inventory In Stock", fontWeight = FontWeight.Medium)
                                        Text(
                                            text = if (inStock) "Customers can order this item" else "Marked as sold out",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CharcoalGray.copy(alpha = 0.6f)
                                        )
                                    }
                                    Switch(checked = inStock, onCheckedChange = { inStock = it })
                                }
                            }
                        }
                    }

                    // Button to Preview from Customer Point of View
                    item {
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preview Customer Point of View 👁️", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Save Button
                    item {
                        Button(
                            onClick = {
                                val catObj = categories.find { it.first == category }
                                val req = ProductCreateRequest(
                                    name = name.trim(),
                                    category = category,
                                    categoryName = catObj?.second,
                                    price = priceVal,
                                    originalPrice = origPriceVal,
                                    image = imageUrl.trim(),
                                    badge = badge.trim().ifBlank { null },
                                    leadTime = leadTime.trim().ifBlank { null },
                                    description = description.trim(),
                                    inStock = inStock
                                )
                                onSave(req, product?.id)
                            },
                            enabled = name.isNotBlank() && priceVal > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Piece to Boutique", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // CUSTOMER POINT OF VIEW PREVIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Customer View Explanation Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandPrimaryLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Customer Point of View Preview",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BrandSecondary
                                    )
                                    Text(
                                        text = "This is exactly how buyers see your item in the online boutique.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalGray
                                    )
                                }
                            }
                        }
                    }

                    // Realistic Customer Boutique Product Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Product Image with Badge & Wishlist
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                        .background(Color(0xFFF9F7F5))
                                ) {
                                    AsyncImage(
                                        model = resolvedImageUrl,
                                        contentDescription = name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Badge in Top-Left
                                    if (badge.isNotBlank()) {
                                        Surface(
                                            color = BrandPrimary,
                                            shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 20.dp),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = badge,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    // Heart / Wishlist in Top-Right
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.9f),
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Save",
                                                tint = CrimsonAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // Out of Stock Overlay
                                    if (!inStock) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.65f),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "SOLD OUT • PRE-ORDER ONLY",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = CrimsonAccent,
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Product Content
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Rating & Category
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = categories.find { it.first == category }?.second ?: category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoyalBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "★★★★★",
                                                color = Color(0xFFFFA000),
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "5.0 (New)",
                                                fontSize = 11.sp,
                                                color = CharcoalGray.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Product Title
                                    Text(
                                        text = if (name.isNotBlank()) name else "Untitled Handcrafted Creation",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MidnightNavy
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Pricing & Discount
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "₹${priceVal.toInt()}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary
                                        )

                                        if (origPriceVal != null && origPriceVal > priceVal) {
                                            Text(
                                                text = "₹${origPriceVal.toInt()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = TextDecoration.LineThrough
                                                ),
                                                color = CharcoalGray.copy(alpha = 0.5f)
                                            )

                                            Surface(
                                                color = Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "$discountPercent% OFF",
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Lead Time & Shipping Pills
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF6F8FC)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(13.dp), tint = RoyalBlue)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = leadTime, fontSize = 11.sp, color = RoyalBlue, fontWeight = FontWeight.Medium)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF2E7D32))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Free Delivery", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Description snippet
                                    Text(
                                        text = if (description.isNotBlank()) description else "Each bespoke piece is handcrafted individually with 100% milk cotton yarn.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalGray,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Customer CTA Simulation
                                    Button(
                                        onClick = {},
                                        enabled = inStock,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BrandPrimary,
                                            contentColor = Color.White,
                                            disabledContainerColor = Color.LightGray,
                                            disabledContentColor = Color.DarkGray
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (inStock) "Add to Bag • ₹${priceVal.toInt()}" else "Currently Sold Out",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Publish Action from Preview
                    item {
                        Button(
                            onClick = {
                                val catObj = categories.find { it.first == category }
                                val req = ProductCreateRequest(
                                    name = name.trim(),
                                    category = category,
                                    categoryName = catObj?.second,
                                    price = priceVal,
                                    originalPrice = origPriceVal,
                                    image = imageUrl.trim(),
                                    badge = badge.trim().ifBlank { null },
                                    leadTime = leadTime.trim().ifBlank { null },
                                    description = description.trim(),
                                    inStock = inStock
                                )
                                onSave(req, product?.id)
                            },
                            enabled = name.isNotBlank() && priceVal > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandSecondary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Everything Looks Great — Publish Now", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
