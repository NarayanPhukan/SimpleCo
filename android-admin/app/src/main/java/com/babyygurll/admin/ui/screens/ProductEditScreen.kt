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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.Product
import com.babyygurll.admin.data.model.ProductCreateRequest
import com.babyygurll.admin.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.NumberFormat
import java.util.Locale
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
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Step state (1 to 9)
    var currentStep by remember { mutableIntStateOf(1) }

    val stepTitles = listOf(
        "Basic info",
        "Images",
        "Pricing",
        "Inventory",
        "Variants",
        "Shipping",
        "Materials",
        "SEO",
        "Review"
    )

    // Step 1: Basic info
    var productName by remember(product?.id) { mutableStateOf(product?.name ?: "Ocean Bloom Crochet Bag") }
    var shortDescription by remember(product?.id) { mutableStateOf(product?.description?.take(100) ?: "A structured hand-crocheted shoulder bag") }
    var category by remember(product?.id) { mutableStateOf(product?.categoryName ?: "Handbags") }
    var subcategory by remember(product?.id) { mutableStateOf("Shoulder bags") }
    var sku by remember(product?.id) { mutableStateOf(product?.sku ?: "BGB-OCB-042") }
    var productTags by remember(product?.id) { mutableStateOf("crochet, handmade, blue, shoulder bag") }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSubcategoryMenu by remember { mutableStateOf(false) }

    val categoryOptions = listOf("Handbags", "Plushies", "Floral & Home", "Apparel", "Accessories")
    val subcategoryOptions = listOf("Shoulder bags", "Totes & Baskets", "Mini Pouches", "Plush Amigurumi", "Sweaters & Tops", "Keychains")

    // Step 2: Images
    val initialImages = remember(product?.id, product?.images, product?.image) {
        when {
            product != null && product.images.isNotEmpty() -> product.images
            product != null && product.image.isNotBlank() -> listOf(product.image)
            else -> listOf("/assets/images/granny-bag.jpg")
        }
    }
    var imagesList by remember(product?.id) { mutableStateOf<List<String>>(initialImages) }
    var isProcessingImages by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessingImages = true
                val newImages = mutableListOf<String>()
                for (uri in uris) {
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
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
                            newImages.add("data:image/jpeg;base64,$base64String")
                        }
                    } catch (e: Exception) {
                        // Skip unreadable files
                    }
                }
                withContext(Dispatchers.Main) {
                    imagesList = imagesList + newImages
                    isProcessingImages = false
                }
            }
        }
    }

    // Step 3: Pricing
    var sellingPriceText by remember(product?.id) {
        mutableStateOf(product?.price?.takeIf { it > 0 }?.toInt()?.toString() ?: "2499")
    }
    var originalPriceText by remember(product?.id) {
        mutableStateOf(product?.originalPrice?.takeIf { it > 0 }?.toInt()?.toString() ?: "2899")
    }
    var costPriceText by remember(product?.id) { mutableStateOf("1120") }

    val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 2499.0
    val costPrice = costPriceText.toDoubleOrNull() ?: 1120.0
    val estimatedProfit = (sellingPrice - costPrice).coerceAtLeast(0.0)
    val profitMargin = if (sellingPrice > 0) ((estimatedProfit / sellingPrice) * 100) else 0.0

    // Step 4: Inventory
    var trackInventory by remember { mutableStateOf(true) }
    var availableQuantityText by remember(product?.id) {
        mutableStateOf(product?.stockCount?.toString() ?: "8")
    }
    var lowStockThresholdText by remember { mutableStateOf("4") }
    var allowBackorders by remember { mutableStateOf(false) }

    // Step 5: Variants
    var colorChips by remember { mutableStateOf(listOf("Ocean Blue", "Cloud White", "Soft Grey")) }
    var sizeChips by remember { mutableStateOf(listOf("Small", "Medium", "Large")) }
    var newColorInput by remember { mutableStateOf("") }
    var newSizeInput by remember { mutableStateOf("") }

    // Step 6: Shipping
    var leadTime by remember(product?.id) {
        mutableStateOf(product?.leadTime ?: "3–5 days made to order")
    }
    var weightGrams by remember { mutableStateOf("350") }
    var packageDimensions by remember { mutableStateOf("25 × 18 × 8") }
    var shippingClass by remember { mutableStateOf("Standard Courier") }

    // Step 7: Materials & Care
    var materialsText by remember { mutableStateOf("Milk cotton yarn, cotton lining") }
    var hardwareText by remember { mutableStateOf("Cotton canvas lining, magnetic brass closure") }
    var careInstructions by remember {
        mutableStateOf("Hand wash gently in cold water with mild detergent. Lay flat to dry in shade.")
    }

    // Step 8: SEO & Visibility
    var storefrontStatus by remember(product?.id) {
        mutableStateOf(if (product?.inStock == false) "Draft" else "Published")
    }
    var seoTitle by remember(productName) {
        mutableStateOf("$productName | BabyyGurll Boutique")
    }
    var seoDescription by remember(shortDescription) {
        mutableStateOf("Hand-crocheted piece crafted from soft milk cotton yarn. Bespoke artisanal creation.")
    }

    // Helper to submit/save product
    val submitProduct: (Boolean) -> Unit = { isDraft ->
        val finalPrice = sellingPriceText.toDoubleOrNull() ?: 2499.0
        val finalOrigPrice = originalPriceText.toDoubleOrNull()
        val primaryImg = imagesList.firstOrNull() ?: "/assets/images/granny-bag.jpg"

        val req = ProductCreateRequest(
            name = productName.trim().ifBlank { "Ocean Bloom Crochet Bag" },
            category = when {
                category.contains("Handbag", true) || category.contains("Bag", true) -> "bags"
                category.contains("Plush", true) -> "plushies"
                category.contains("Floral", true) -> "floral"
                category.contains("Apparel", true) -> "wearables"
                else -> "accessories"
            },
            categoryName = category,
            price = finalPrice,
            originalPrice = finalOrigPrice,
            image = primaryImg,
            images = imagesList,
            badge = if (isDraft) "Draft" else "Published",
            leadTime = leadTime,
            description = shortDescription.trim().ifBlank {
                "A structured hand-crocheted shoulder bag made with premium milk cotton yarn. Finished with a lined interior, magnetic closure and adjustable strap."
            },
            inStock = !isDraft && (availableQuantityText.toIntOrNull() ?: 1) > 0
        )
        onSave(req, product?.id ?: product?.mongoId)
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftIvory
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (currentStep > 1) {
                                            currentStep--
                                        } else {
                                            onBack()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MidnightNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (product != null) "Edit product" else "Add product",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "Step $currentStep of 9 · ${stepTitles[currentStep - 1]}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Top Right Saved Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Saved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { currentStep / 9f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = RoyalBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step Indicator Pills Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..9) {
                            val title = stepTitles[i - 1]
                            when {
                                i < currentStep -> {
                                    // Completed Step (Checkmark Circle)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDBEAFE))
                                            .clickable { currentStep = i },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                i == currentStep -> {
                                    // Active Step Pill
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = RoyalBlue,
                                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                    ) {
                                        Text(
                                            text = "$i  $title",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                else -> {
                                    // Inactive Future Step Circle
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE2E8F0))
                                            .clickable { currentStep = i },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$i",
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { submitProduct(true) },
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Save draft",
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (currentStep < 9) {
                                currentStep++
                            } else {
                                submitProduct(false)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentStep < 9) "Continue" else "Publish product",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            if (currentStep < 9) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            when (currentStep) {
                // ==========================================
                // STEP 1: Basic Information
                // ==========================================
                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Basic information",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Tell customers what makes this handmade piece special.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            // Product name
                            FieldHeader("Product name", isRequired = true)
                            OutlinedTextField(
                                value = productName,
                                onValueChange = { productName = it },
                                placeholder = { Text("e.g. Ocean Bloom Crochet Bag") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )

                            // Short description
                            FieldHeader("Short description", isRequired = false)
                            OutlinedTextField(
                                value = shortDescription,
                                onValueChange = { shortDescription = it },
                                placeholder = { Text("A structured hand-crocheted shoulder bag") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )

                            // Category
                            FieldHeader("Category", isRequired = true)
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showCategoryMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = category, fontSize = 14.sp, color = MidnightNavy)
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                                    }
                                }

                                DropdownMenu(
                                    expanded = showCategoryMenu,
                                    onDismissRequest = { showCategoryMenu = false }
                                ) {
                                    categoryOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                category = opt
                                                showCategoryMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Subcategory
                            FieldHeader("Subcategory", isRequired = false)
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showSubcategoryMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = subcategory, fontSize = 14.sp, color = MidnightNavy)
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                                    }
                                }

                                DropdownMenu(
                                    expanded = showSubcategoryMenu,
                                    onDismissRequest = { showSubcategoryMenu = false }
                                ) {
                                    subcategoryOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                subcategory = opt
                                                showSubcategoryMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // SKU with Generate button
                            FieldHeader("SKU", isRequired = true)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = sku,
                                        onValueChange = { sku = it },
                                        placeholder = { Text("e.g. BGB-OCB-042") },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val prefix = when {
                                                    category.contains("Bag", true) -> "BGB-OCB"
                                                    category.contains("Plush", true) -> "BGB-PLS"
                                                    category.contains("Floral", true) -> "BGB-FLR"
                                                    category.contains("Apparel", true) -> "BGB-CKC"
                                                    else -> "BGB-PWS"
                                                }
                                                sku = "$prefix-0${(10..99).random()}"
                                            }
                                    ) {
                                        Text(
                                            text = "Generate",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalBlue,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Unique stock keeping unit for this product.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            // Product tags
                            FieldHeader("Product tags", isRequired = false)
                            OutlinedTextField(
                                value = productTags,
                                onValueChange = { productTags = it },
                                placeholder = { Text("crochet, handmade, blue, shoulder bag") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 2: Images
                // ==========================================
                2 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Product images",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Add up to 8 high-quality images. Drag to reorder.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            // Large Primary Image Preview
                            val primaryImg = imagesList.firstOrNull() ?: ""
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                val fullUrl = if (primaryImg.startsWith("http") || primaryImg.startsWith("data:")) {
                                    primaryImg
                                } else if (primaryImg.isNotBlank()) {
                                    "${serverUrl.trimEnd('/')}${if (primaryImg.startsWith("/")) "" else "/"}$primaryImg"
                                } else ""

                                if (fullUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = fullUrl,
                                        contentDescription = "Primary Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                    }
                                }

                                // PRIMARY Badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = RoyalBlue,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "PRIMARY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Change Button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Change", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MidnightNavy)
                                    }
                                }
                            }

                            // Secondary Thumbnails Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                imagesList.drop(1).take(2).forEachIndexed { idx, img ->
                                    val realIdx = idx + 1
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(86.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        val u = if (img.startsWith("http") || img.startsWith("data:")) img else "${serverUrl.trimEnd('/')}$img"
                                        AsyncImage(model = u, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

                                        // Close/Delete Badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.9f))
                                                .clickable {
                                                    imagesList = imagesList.toMutableList().apply { removeAt(realIdx) }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = MidnightNavy, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }

                                // Add Image Slot
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(86.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Add image", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                    }
                                }
                            }

                            Text(
                                text = "Use square images at least 1200 × 1200 px. JPG, PNG up to 10 MB.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 3: Pricing
                // ==========================================
                3 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Pricing",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Internal cost and profit details are never shown to customers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            // Selling price
                            FieldHeader("Selling price", isRequired = true)
                            OutlinedTextField(
                                value = sellingPriceText,
                                onValueChange = { sellingPriceText = it.filter { ch -> ch.isDigit() } },
                                prefix = { Text("₹ ", fontWeight = FontWeight.SemiBold, color = MidnightNavy) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )

                            // Original / MRP price
                            FieldHeader("Original / MRP price", isRequired = false)
                            OutlinedTextField(
                                value = originalPriceText,
                                onValueChange = { originalPriceText = it.filter { ch -> ch.isDigit() } },
                                prefix = { Text("₹ ", fontWeight = FontWeight.SemiBold, color = MidnightNavy) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )

                            // Cost price
                            FieldHeader("Cost price", isRequired = false)
                            OutlinedTextField(
                                value = costPriceText,
                                onValueChange = { costPriceText = it.filter { ch -> ch.isDigit() } },
                                prefix = { Text("₹ ", fontWeight = FontWeight.SemiBold, color = MidnightNavy) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )
                            Text(
                                text = "Visible only to authorized admins.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            // Dark Navy Profit Analytics Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MidnightNavy)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Estimated profit", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = currencyFormat.format(estimatedProfit),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.15f)))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 16.dp)
                                    ) {
                                        Text(text = "Profit margin", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = String.format(Locale.ENGLISH, "%.1f%%", profitMargin),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 4: Inventory
                // ==========================================
                4 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Inventory",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Track stock and get alerts before you run out.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            // Track Inventory Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Track inventory", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                    Text(text = "Automatically update stock when orders are confirmed", fontSize = 12.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = trackInventory,
                                    onCheckedChange = { trackInventory = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                )
                            }

                            // Available Quantity
                            FieldHeader("Available quantity", isRequired = true)
                            OutlinedTextField(
                                value = availableQuantityText,
                                onValueChange = { availableQuantityText = it.filter { ch -> ch.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )

                            // Low-Stock Threshold
                            FieldHeader("Low-stock threshold", isRequired = false)
                            OutlinedTextField(
                                value = lowStockThresholdText,
                                onValueChange = { lowStockThresholdText = it.filter { ch -> ch.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )

                            // Allow Backorders Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Allow backorders", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                    Text(text = "Continue selling when this product reaches zero", fontSize = 12.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = allowBackorders,
                                    onCheckedChange = { allowBackorders = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                )
                            }

                            // In-Stock Status Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "In stock", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "We'll notify you when stock reaches ${lowStockThresholdText.ifBlank { "4" }} units.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 5: Variants
                // ==========================================
                5 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Product variants",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Offer options such as colour, size, length or style.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            // Option Box 1: Colour
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Colour", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        colorChips.forEach { col ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFEFF6FF)
                                            ) {
                                                Text(
                                                    text = col,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MidnightNavy,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Option Box 2: Size
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Size", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        sizeChips.forEach { sz ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFEFF6FF)
                                            ) {
                                                Text(
                                                    text = sz,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MidnightNavy,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Add another option box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Add another option", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                }
                            }

                            // Combinations and Bulk Edit
                            val comboCount = colorChips.size * sizeChips.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "$comboCount variant combinations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MidnightNavy)
                                Text(
                                    text = "Bulk edit stock and pricing >",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 6: Shipping & Dispatch
                // ==========================================
                6 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Production & Shipping",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Specify handcrafting duration and dispatch dimensions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            FieldHeader("Lead time / Crafting duration", isRequired = true)
                            OutlinedTextField(
                                value = leadTime,
                                onValueChange = { leadTime = it },
                                placeholder = { Text("e.g. 3–5 days made to order") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            FieldHeader("Weight (grams)", isRequired = false)
                            OutlinedTextField(
                                value = weightGrams,
                                onValueChange = { weightGrams = it },
                                placeholder = { Text("e.g. 350") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            FieldHeader("Package dimensions (L × W × H cm)", isRequired = false)
                            OutlinedTextField(
                                value = packageDimensions,
                                onValueChange = { packageDimensions = it },
                                placeholder = { Text("e.g. 25 × 18 × 8") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 7: Materials & Artisan Care
                // ==========================================
                7 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Materials & Artisan Care",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Help customers care for their handmade crochet creations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            FieldHeader("Yarn material", isRequired = true)
                            OutlinedTextField(
                                value = materialsText,
                                onValueChange = { materialsText = it },
                                placeholder = { Text("e.g. Milk cotton yarn, cotton lining") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            FieldHeader("Lining / Hardware", isRequired = false)
                            OutlinedTextField(
                                value = hardwareText,
                                onValueChange = { hardwareText = it },
                                placeholder = { Text("e.g. Cotton lining, magnetic closure") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            FieldHeader("Care instructions", isRequired = false)
                            OutlinedTextField(
                                value = careInstructions,
                                onValueChange = { careInstructions = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 8: Storefront & SEO
                // ==========================================
                8 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Storefront & SEO",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Customize how this piece appears in search results and social links.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            FieldHeader("Storefront status", isRequired = true)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val isPub = storefrontStatus == "Published"
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isPub) RoyalBlue else Color.White,
                                    border = if (isPub) null else androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { storefrontStatus = "Published" }
                                ) {
                                    Text(
                                        text = "Published · Live on store",
                                        color = if (isPub) Color.White else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                val isDraft = storefrontStatus == "Draft"
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDraft) RoyalBlue else Color.White,
                                    border = if (isDraft) null else androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { storefrontStatus = "Draft" }
                                ) {
                                    Text(
                                        text = "Draft · Private",
                                        color = if (isDraft) Color.White else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            FieldHeader("Page title", isRequired = false)
                            OutlinedTextField(
                                value = seoTitle,
                                onValueChange = { seoTitle = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            FieldHeader("Meta description", isRequired = false)
                            OutlinedTextField(
                                value = seoDescription,
                                onValueChange = { seoDescription = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 9: Review & Publish
                // ==========================================
                9 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Review & Publish",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Check all details before making this piece live for patrons.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            // Summary Preview Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFD),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE2E8F0))
                                    ) {
                                        val u = imagesList.firstOrNull() ?: ""
                                        if (u.isNotBlank()) {
                                            AsyncImage(model = u, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = productName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MidnightNavy)
                                        Text(text = "$category · $sku", fontSize = 12.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "₹ $sellingPriceText", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoyalBlue)
                                    }
                                }
                            }

                            // Green Complete Status Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "Ready to publish", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                        Text(text = "All required fields completed. Ready for boutique catalogue.", fontSize = 11.sp, color = Color(0xFF16A34A))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FieldHeader(label: String, isRequired: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MidnightNavy
        )
        if (isRequired) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Required",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFDC2626)
            )
        }
    }
}
