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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babyygurll.admin.data.model.*
import com.babyygurll.admin.ui.theme.*
import com.babyygurll.admin.util.AdminProductImage
import com.babyygurll.admin.util.TagAutomation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    product: Product?,
    serverUrl: String,
    existingProducts: List<Product> = emptyList(),
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
    var showSavedToast by remember { mutableStateOf(false) }

    val stepTitles = listOf(
        "Basic info",
        "Images",
        "Pricing",
        "Inventory",
        "Variants",
        "Handmade",
        "Shipping",
        "Visibility",
        "Review"
    )

    // ==========================================
    // FORM STATE
    // ==========================================

    // Step 1: Basic info
    var productName by remember(product?.id) { mutableStateOf(product?.name ?: "") }
    var shortDescription by remember(product?.id) { mutableStateOf(product?.description?.take(120) ?: "") }
    
    val allBoutiqueCategories = BoutiqueCategoryData.categories
    val initialCat = remember(product?.id) {
        val catName = product?.categoryName ?: ""
        val catKey = product?.category ?: ""
        allBoutiqueCategories.firstOrNull { 
            it.name.equals(catName, ignoreCase = true) || it.id.equals(catKey, ignoreCase = true)
        } ?: allBoutiqueCategories[1] // Bags & Purses default
    }
    var selectedBoutiqueCategory by remember(product?.id) { mutableStateOf(initialCat) }
    var selectedBoutiqueSubcategory by remember(product?.id, selectedBoutiqueCategory) {
        val subId = product?.subcategory ?: ""
        val subName = product?.subcategoryName ?: ""
        val found = selectedBoutiqueCategory.subcategories.firstOrNull {
            it.id.equals(subId, ignoreCase = true) || it.name.equals(subName, ignoreCase = true)
        } ?: selectedBoutiqueCategory.subcategories.first()
        mutableStateOf(found)
    }

    val category = selectedBoutiqueCategory.name
    val subcategory = selectedBoutiqueSubcategory.name
    var sku by remember(product?.id) { mutableStateOf(product?.sku ?: "") }
    var isSkuLocked by remember(product?.id) {
        mutableStateOf(product != null || (product?.sku?.isNotBlank() == true))
    }

    val existingSkus = remember(existingProducts, product?.id) {
        existingProducts
            .filter { it.id != product?.id && it.mongoId != product?.mongoId && it.id != product?.mongoId }
            .mapNotNull { it.sku.trim().uppercase().ifBlank { null } }
            .toSet()
    }

    val isSkuDuplicate = remember(sku, existingSkus) {
        val trimmed = sku.trim().uppercase()
        trimmed.isNotBlank() && existingSkus.contains(trimmed)
    }

    val generateUniqueSku: (String) -> String = { cat ->
        val prefix = when {
            cat.contains("Bag", true) -> "BGB-OCB"
            cat.contains("Plush", true) -> "BGB-PLS"
            cat.contains("Floral", true) -> "BGB-FLR"
            cat.contains("Apparel", true) -> "BGB-CKC"
            else -> "BGB-PWS"
        }
        var candidate = "$prefix-0${(10..99).random()}"
        var tries = 0
        while (existingSkus.contains(candidate.uppercase()) && tries < 200) {
            candidate = "$prefix-0${(10..99).random()}"
            tries++
        }
        if (existingSkus.contains(candidate.uppercase())) {
            tries = 0
            while (existingSkus.contains(candidate.uppercase()) && tries < 500) {
                candidate = "$prefix-${(1000..9999).random()}"
                tries++
            }
        }
        candidate
    }

    var hasManuallyEditedTags by remember(product?.id) { mutableStateOf(false) }

    var productTags by remember(product?.id) {
        val existing = product?.tags?.joinToString(", ") ?: ""
        if (existing.isNotBlank()) {
            mutableStateOf(existing)
        } else if (product != null) {
            val auto = TagAutomation.generateTags(
                category = product.category,
                categoryName = product.categoryName,
                subcategory = product.subcategory ?: "",
                subcategoryName = product.subcategoryName ?: "",
                title = product.name,
                description = product.description
            )
            mutableStateOf(auto.joinToString(", "))
        } else {
            mutableStateOf("")
        }
    }

    val autoGenerateTags = {
        val generated = TagAutomation.generateTags(
            category = selectedBoutiqueCategory.id,
            categoryName = selectedBoutiqueCategory.name,
            subcategory = selectedBoutiqueSubcategory.id,
            subcategoryName = selectedBoutiqueSubcategory.name,
            title = productName.trim(),
            description = shortDescription.trim()
        )
        if (generated.isNotEmpty()) {
            productTags = generated.joinToString(", ")
        }
    }

    LaunchedEffect(productName, selectedBoutiqueCategory, selectedBoutiqueSubcategory) {
        if (!hasManuallyEditedTags && productName.isNotBlank()) {
            autoGenerateTags()
        }
    }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSubcategoryMenu by remember { mutableStateOf(false) }

    // Step 2: Images
    val initialImages = remember(product?.id, product?.images, product?.image) {
        when {
            product != null && product.allImages.isNotEmpty() -> product.allImages
            product != null && product.resolvedPrimaryImage.isNotBlank() -> listOf(product.resolvedPrimaryImage)
            else -> emptyList()
        }
    }
    var imagesList by remember(product?.id) { mutableStateOf<List<String>>(initialImages) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
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
                                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
                            } else originalBitmap

                            val outputStream = ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                            val byteArray = outputStream.toByteArray()
                            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                            newImages.add("data:image/jpeg;base64,$base64String")
                        }
                    } catch (e: Exception) {
                        // Skip unreadable
                    }
                }
                withContext(Dispatchers.Main) {
                    imagesList = imagesList + newImages
                }
            }
        }
    }

    // Step 3: Pricing
    var sellingPriceText by remember(product?.id) {
        mutableStateOf(product?.price?.takeIf { it > 0 }?.toInt()?.toString() ?: "")
    }
    var originalPriceText by remember(product?.id) {
        mutableStateOf(product?.originalPrice?.takeIf { it > 0 }?.toInt()?.toString() ?: "")
    }
    var costPriceText by remember(product?.id) { mutableStateOf("") }

    val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
    val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
    val estimatedProfit = (sellingPrice - costPrice).coerceAtLeast(0.0)
    val profitMargin = if (sellingPrice > 0) ((estimatedProfit / sellingPrice) * 100) else 0.0

    // Step 4: Inventory
    var trackInventory by remember { mutableStateOf(true) }
    var availableQuantityText by remember(product?.id) {
        mutableStateOf(product?.stockCount?.toString() ?: "")
    }
    var lowStockThresholdText by remember { mutableStateOf("") }
    var allowBackorders by remember { mutableStateOf(false) }

    // Step 5: Variants
    var colorChips by remember {
        mutableStateOf<List<String>>(
            if (!product?.colors.isNullOrEmpty()) product!!.colors.map { it.name }
            else if (!product?.variants.isNullOrEmpty()) product!!.variants.mapNotNull { it.color }.distinct()
            else emptyList()
        )
    }
    var sizeChips by remember {
        mutableStateOf<List<String>>(
            if (!product?.sizes.isNullOrEmpty()) product!!.sizes
            else if (!product?.variants.isNullOrEmpty()) product!!.variants.mapNotNull { it.size }.distinct()
            else emptyList()
        )
    }
    var dynamicVariants by remember {
        mutableStateOf<List<ProductVariant>>(
            product?.variants ?: emptyList()
        )
    }
    var showAddColorDialog by remember { mutableStateOf(false) }
    var newColorInput by remember { mutableStateOf("") }
    var showAddSizeDialog by remember { mutableStateOf(false) }
    var newSizeInput by remember { mutableStateOf("") }

    // Direct Custom Variant Dialog state
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var newVariantName by remember { mutableStateOf("") }
    var newVariantColor by remember { mutableStateOf("") }
    var newVariantSize by remember { mutableStateOf("") }
    var newVariantPrice by remember { mutableStateOf("") }
    var newVariantStock by remember { mutableStateOf("") }
    var newVariantSku by remember { mutableStateOf("") }

    val regenerateVariants: (List<String>, List<String>, List<ProductVariant>) -> List<ProductVariant> = { colors, sizes, existing ->
        if (colors.isEmpty() && sizes.isEmpty()) {
            emptyList()
        } else {
            val basePrice = sellingPriceText.toDoubleOrNull() ?: 0.0
            val baseStock = availableQuantityText.toIntOrNull() ?: 0
            val baseOrigPrice = originalPriceText.toDoubleOrNull()

            val activeColors = if (colors.isNotEmpty()) colors else listOf("")
            val activeSizes = if (sizes.isNotEmpty()) sizes else listOf("")

            val result = mutableListOf<ProductVariant>()
            for (c in activeColors) {
                for (s in activeSizes) {
                    val name = when {
                        c.isNotBlank() && s.isNotBlank() -> "$s / $c"
                        c.isNotBlank() -> c
                        s.isNotBlank() -> s
                        else -> ""
                    }
                    if (name.isBlank()) continue

                    val prev = existing.firstOrNull {
                        (it.color.orEmpty().equals(c, ignoreCase = true) || it.name.contains(c, ignoreCase = true)) &&
                        (it.size.orEmpty().equals(s, ignoreCase = true) || it.name.contains(s, ignoreCase = true))
                    }
                    result.add(
                        prev ?: ProductVariant(
                            id = "${c.lowercase().replace(" ", "-")}-${s.lowercase().replace(" ", "-")}".trim('-').ifBlank { "var-${(100..999).random()}" },
                            name = name,
                            size = s.ifBlank { null },
                            color = c.ifBlank { null },
                            colorHex = "#255DCE",
                            price = basePrice,
                            originalPrice = baseOrigPrice,
                            stock = baseStock,
                            sku = if (c.isNotBlank()) "$sku-${c.take(3).uppercase()}" else sku
                        )
                    )
                }
            }
            result
        }
    }

    LaunchedEffect(Unit) {
        if (dynamicVariants.isEmpty() && (colorChips.isNotEmpty() || sizeChips.isNotEmpty())) {
            dynamicVariants = regenerateVariants(colorChips, sizeChips, emptyList())
        }
    }

    // Step 6: Handmade details
    var materialsUsed by remember { mutableStateOf("") }
    var yarnType by remember { mutableStateOf("") }
    var yarnColour by remember { mutableStateOf(product?.colors?.firstOrNull()?.name ?: "") }
    var productionType by remember { mutableStateOf("Made to order") }
    var showProductionTypeMenu by remember { mutableStateOf(false) }
    val productionTypeOptions = listOf("Made to order", "Ready to ship", "Made in small batches")
    var estimatedProductionTime by remember { mutableStateOf("") }
    var customisationAvailable by remember { mutableStateOf(true) }
    var careInstructions by remember { mutableStateOf("") }

    // Step 7: Shipping information
    var weightText by remember { mutableStateOf("") }
    var packageType by remember { mutableStateOf("Box") }
    var showPackageTypeMenu by remember { mutableStateOf(false) }
    val packageTypeOptions = listOf("Box", "Pouch", "Envelope", "Tube")
    var parcelLength by remember { mutableStateOf("") }
    var parcelWidth by remember { mutableStateOf("") }
    var parcelHeight by remember { mutableStateOf("") }
    var processingTime by remember { mutableStateOf("") }
    var shippingNote by remember { mutableStateOf("") }

    // Step 8: Storefront visibility
    var publishImmediately by remember { mutableStateOf(true) }
    var featuredProduct by remember { mutableStateOf(true) }
    var showOnHomepage by remember { mutableStateOf(false) }
    var displayStockStatus by remember { mutableStateOf(true) }
    var allowCustomerCustomisation by remember { mutableStateOf(true) }

    // Helper to submit/save product
    val submitProduct: (Boolean) -> Unit = { isDraft ->
        val finalPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
        val finalOrigPrice = originalPriceText.toDoubleOrNull()
        val primaryImg = imagesList.firstOrNull() ?: ""
        val finalVariants = if (dynamicVariants.isNotEmpty()) {
            dynamicVariants
        } else if (colorChips.isNotEmpty() || sizeChips.isNotEmpty()) {
            regenerateVariants(colorChips, sizeChips, emptyList())
        } else {
            emptyList()
        }
        val effectiveColors = (colorChips + dynamicVariants.mapNotNull { it.color }).distinct().filter { it.isNotBlank() }
        val effectiveSizes = (sizeChips + dynamicVariants.mapNotNull { it.size }).distinct().filter { it.isNotBlank() }

        val parsedTags = productTags.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
        val finalTags = if (parsedTags.isNotEmpty()) {
            parsedTags
        } else {
            TagAutomation.generateTags(
                category = selectedBoutiqueCategory.id,
                categoryName = selectedBoutiqueCategory.name,
                subcategory = selectedBoutiqueSubcategory.id,
                subcategoryName = selectedBoutiqueSubcategory.name,
                title = productName.trim(),
                description = shortDescription.trim()
            )
        }

        if (!isSkuDuplicate) {
            val finalSku = if (sku.isNotBlank()) sku.trim().uppercase() else generateUniqueSku(selectedBoutiqueCategory.name)

            val req = ProductCreateRequest(
                name = productName.trim(),
                sku = finalSku,
                category = selectedBoutiqueCategory.id,
                categoryName = selectedBoutiqueCategory.name,
                subcategory = selectedBoutiqueSubcategory.id,
                subcategoryName = selectedBoutiqueSubcategory.name,
                price = finalPrice,
                originalPrice = finalOrigPrice,
                image = primaryImg,
                images = imagesList,
                badge = if (isDraft) "Draft" else "Published",
                leadTime = estimatedProductionTime,
                description = shortDescription.trim(),
                colors = effectiveColors.map { ProductColor(name = it, hex = "#255DCE") },
                sizes = effectiveSizes,
                variants = finalVariants,
                tags = finalTags,
                inStock = !isDraft && (availableQuantityText.toIntOrNull() ?: 0) > 0
            )
            onSave(req, product?.id ?: product?.mongoId)
        }
    }

    val triggerSaveNotification = {
        showSavedToast = true
        coroutineScope.launch {
            delay(3000)
            showSavedToast = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

                            // Top Right Saved Indicator (clickable to trigger toast)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { triggerSaveNotification() }
                                    .padding(4.dp)
                            ) {
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

                        // Linear Progress Bar
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
                            onClick = {
                                triggerSaveNotification()
                                submitProduct(true)
                            },
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
                                if (currentStep == 9) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
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

                                FieldHeader("Product name", isRequired = true)
                                OutlinedTextField(
                                    value = productName,
                                    onValueChange = { productName = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                FieldHeader("Short description", isRequired = false)
                                OutlinedTextField(
                                    value = shortDescription,
                                    onValueChange = { shortDescription = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

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
                                            Text(text = "${selectedBoutiqueCategory.icon} ${selectedBoutiqueCategory.name}", fontSize = 14.sp, color = MidnightNavy)
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showCategoryMenu,
                                        onDismissRequest = { showCategoryMenu = false }
                                    ) {
                                        allBoutiqueCategories.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text("${opt.icon} ${opt.name}") },
                                                onClick = {
                                                    selectedBoutiqueCategory = opt
                                                    selectedBoutiqueSubcategory = opt.subcategories.first()
                                                    showCategoryMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

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
                                            Text(text = selectedBoutiqueSubcategory.name, fontSize = 14.sp, color = MidnightNavy)
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showSubcategoryMenu,
                                        onDismissRequest = { showSubcategoryMenu = false }
                                    ) {
                                        selectedBoutiqueCategory.subcategories.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt.name) },
                                                onClick = {
                                                    selectedBoutiqueSubcategory = opt
                                                    showSubcategoryMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FieldHeader("Product Code (SKU)", isRequired = true)
                                    if (isSkuDuplicate) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF2F2),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Duplicate",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Duplicate Code",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC2626)
                                                )
                                            }
                                        }
                                    } else if (isSkuLocked) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Unique & Fixed",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }
                                }
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSkuDuplicate) Color(0xFFDC2626) else if (isSkuLocked) Color(0xFFE2E8F0) else BrandBorder
                                    ),
                                    color = if (isSkuLocked) Color(0xFFF8FAFC) else Color.White
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
                                            onValueChange = {
                                                if (!isSkuLocked) {
                                                    sku = it
                                                }
                                            },
                                            readOnly = isSkuLocked,
                                            singleLine = true,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                disabledContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent,
                                                focusedTextColor = if (isSkuDuplicate) Color(0xFFDC2626) else if (isSkuLocked) Color(0xFF334155) else MidnightNavy,
                                                unfocusedTextColor = if (isSkuDuplicate) Color(0xFFDC2626) else if (isSkuLocked) Color(0xFF334155) else MidnightNavy
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (!isSkuLocked) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFEFF6FF),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        sku = generateUniqueSku(category)
                                                        isSkuLocked = true
                                                    }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = RoyalBlue,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Generate",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = RoyalBlue
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF1F5F9),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = Color(0xFF64748B),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Fixed",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF475569)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (isSkuDuplicate) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Product code '$sku' is already used by another product. Product codes must be strictly unique.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                } else if (isSkuLocked) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Product code is unique, permanently fixed, and can never be changed.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Click Generate to assign a unique, permanent product code. Each code must be strictly unique.",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FieldHeader("Product tags", isRequired = false)

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                hasManuallyEditedTags = false
                                                autoGenerateTags()
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = RoyalBlue,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Auto-analyze",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalBlue
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = productTags,
                                    onValueChange = {
                                        productTags = it
                                        hasManuallyEditedTags = true
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                val activeTagList = remember(productTags) {
                                    productTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                }
                                if (activeTagList.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        activeTagList.forEach { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color(0xFFEFF6FF),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "#$tag",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = RoyalBlue
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove tag",
                                                        tint = RoyalBlue.copy(alpha = 0.7f),
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clickable {
                                                                val remaining = activeTagList.filter { it != tag }
                                                                productTags = remaining.joinToString(", ")
                                                                hasManuallyEditedTags = true
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "Automated by analyzing category, subcategory, title & description. Comma-separated.",
                                    fontSize = 11.sp,
                                    color = TextMuted
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
                                    text = "Add up to 8 high-quality images. Swipe/drag cards or tap ◀ ▶ to reorder. First photo is Primary.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )

                                val primaryImg = imagesList.firstOrNull() ?: ""

                                if (primaryImg.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        AdminProductImage(
                                            model = primaryImg,
                                            contentDescription = "Primary Image",
                                            serverUrl = serverUrl,
                                            fallbackIcon = Icons.Outlined.PhotoCamera,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = RoyalBlue,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "PRIMARY PREVIEW",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

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
                                                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Add more", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MidnightNavy)
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { photoPickerLauncher.launch("image/*") }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Outlined.PhotoCamera,
                                                contentDescription = null,
                                                tint = RoyalBlue,
                                                modifier = Modifier.size(42.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Upload Product Photos",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MidnightNavy
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tap to choose images from gallery (JPG, PNG)",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                // Interactive Reorderable Photo Gallery Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    imagesList.forEachIndexed { idx, img ->
                                        var dragDelta by remember(idx, img) { mutableFloatStateOf(0f) }
                                        val isPrimary = (idx == 0)

                                        Box(
                                            modifier = Modifier
                                                .offset { IntOffset(x = dragDelta.roundToInt(), y = 0) }
                                                .size(width = 104.dp, height = 114.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .border(
                                                    width = if (isPrimary) 2.dp else 1.dp,
                                                    color = if (isPrimary) RoyalBlue else BrandBorder,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .pointerInput(idx, imagesList.size) {
                                                    detectHorizontalDragGestures(
                                                        onDragStart = { dragDelta = 0f },
                                                        onDragEnd = {
                                                            val threshold = 50f
                                                            if (dragDelta < -threshold && idx < imagesList.size - 1) {
                                                                // Dragged Left -> move forward in list
                                                                val list = imagesList.toMutableList()
                                                                val item = list.removeAt(idx)
                                                                list.add(idx + 1, item)
                                                                imagesList = list
                                                            } else if (dragDelta > threshold && idx > 0) {
                                                                // Dragged Right -> move backward in list
                                                                val list = imagesList.toMutableList()
                                                                val item = list.removeAt(idx)
                                                                list.add(idx - 1, item)
                                                                imagesList = list
                                                            }
                                                            dragDelta = 0f
                                                        },
                                                        onDragCancel = { dragDelta = 0f },
                                                        onHorizontalDrag = { _, dragAmount ->
                                                            dragDelta += dragAmount
                                                        }
                                                    )
                                                }
                                        ) {
                                            AdminProductImage(
                                                model = img,
                                                contentDescription = "Photo ${idx + 1}",
                                                serverUrl = serverUrl,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Bottom Action Pill: ★ PRIMARY or "Set Primary"
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(if (isPrimary) RoyalBlue else Color(0xCC0F172A))
                                                    .clickable {
                                                        if (!isPrimary) {
                                                            val list = imagesList.toMutableList()
                                                            val selected = list.removeAt(idx)
                                                            list.add(0, selected)
                                                            imagesList = list
                                                        }
                                                    }
                                                    .padding(vertical = 3.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (isPrimary) "★ PRIMARY" else "Set Primary",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Top Reorder Arrows & Delete Button Bar
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Reorder Left Button (◀)
                                                if (idx > 0) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color.White.copy(alpha = 0.92f),
                                                        shadowElevation = 1.dp,
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clickable {
                                                                val list = imagesList.toMutableList()
                                                                val item = list.removeAt(idx)
                                                                list.add(idx - 1, item)
                                                                imagesList = list
                                                            }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.ChevronLeft,
                                                                contentDescription = "Move left",
                                                                tint = MidnightNavy,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(20.dp))
                                                }

                                                // Reorder Right Button (▶)
                                                if (idx < imagesList.size - 1) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color.White.copy(alpha = 0.92f),
                                                        shadowElevation = 1.dp,
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clickable {
                                                                val list = imagesList.toMutableList()
                                                                val item = list.removeAt(idx)
                                                                list.add(idx + 1, item)
                                                                imagesList = list
                                                            }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.ChevronRight,
                                                                contentDescription = "Move right",
                                                                tint = MidnightNavy,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(20.dp))
                                                }

                                                // Delete Photo Button (✕)
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White.copy(alpha = 0.92f),
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable {
                                                            val list = imagesList.toMutableList()
                                                            list.removeAt(idx)
                                                            imagesList = list
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove photo",
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Add Image Slot Button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                        modifier = Modifier
                                            .size(width = 104.dp, height = 114.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { photoPickerLauncher.launch("image/*") }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Add image", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                        }
                                    }
                                }

                                Text(
                                    text = "Use high-resolution photos. First photo will be displayed on customer catalog cards.",
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

                                // Colour Box
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
                                            Text(
                                                text = "+ Add colour",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = RoyalBlue,
                                                modifier = Modifier
                                                    .clickable { showAddColorDialog = true }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            colorChips.forEach { col ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFEFF6FF)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = col,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MidnightNavy
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            tint = TextMuted,
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .clickable {
                                                                    val updated = colorChips.filter { it != col }
                                                                    colorChips = updated
                                                                    dynamicVariants = regenerateVariants(updated, sizeChips, dynamicVariants)
                                                                }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Size Box
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
                                            Text(
                                                text = "+ Add size",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = RoyalBlue,
                                                modifier = Modifier
                                                    .clickable { showAddSizeDialog = true }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            sizeChips.forEach { sz ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFEFF6FF)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = sz,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MidnightNavy
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            tint = TextMuted,
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .clickable {
                                                                    val updated = sizeChips.filter { it != sz }
                                                                    sizeChips = updated
                                                                    dynamicVariants = regenerateVariants(colorChips, updated, dynamicVariants)
                                                                }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Action Buttons Row: Add Variant, Add Colour, Add Size
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            newVariantPrice = sellingPriceText
                                            newVariantStock = availableQuantityText
                                            showAddVariantDialog = true
                                        },
                                        modifier = Modifier.weight(1.4f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Add Variant", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { showAddColorDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MidnightNavy),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                                    ) {
                                        Text("+ Colour", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = { showAddSizeDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MidnightNavy),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                                    ) {
                                        Text("+ Size", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                val comboCount = dynamicVariants.size
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "$comboCount product variants", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MidnightNavy)
                                    if (colorChips.isNotEmpty() || sizeChips.isNotEmpty()) {
                                        Text(
                                            text = "Regenerate all",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RoyalBlue,
                                            modifier = Modifier.clickable {
                                                dynamicVariants = regenerateVariants(colorChips, sizeChips, emptyList())
                                            }
                                        )
                                    }
                                }

                                // Interactive combinations list with individual price, stock and delete
                                if (dynamicVariants.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No variants added yet. Tap '+ Add Variant' above to create custom options.",
                                                fontSize = 13.sp,
                                                color = TextMuted,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        dynamicVariants.forEachIndexed { vIdx, v ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text(
                                                                text = v.name,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MidnightNavy
                                                            )
                                                            if (!v.color.isNullOrBlank()) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = Color(0xFFEFF6FF)
                                                                ) {
                                                                    Text(
                                                                        text = v.color!!,
                                                                        fontSize = 10.sp,
                                                                        color = RoyalBlue,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                            if (!v.size.isNullOrBlank()) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = Color(0xFFF1F5F9)
                                                                ) {
                                                                    Text(
                                                                        text = v.size!!,
                                                                        fontSize = 10.sp,
                                                                        color = TextMuted,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "SKU: ${v.sku ?: "Auto"}",
                                                                fontSize = 11.sp,
                                                                color = TextMuted
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            // Delete Variant Button
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFFEE2E2))
                                                                    .clickable {
                                                                        val list = dynamicVariants.toMutableList()
                                                                        list.removeAt(vIdx)
                                                                        dynamicVariants = list
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Delete variant",
                                                                    tint = Color(0xFFEF4444),
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        // Variant Price
                                                        OutlinedTextField(
                                                            value = if (v.price > 0) v.price.toInt().toString() else "",
                                                            onValueChange = { newPriceStr ->
                                                                val p = newPriceStr.toDoubleOrNull() ?: 0.0
                                                                dynamicVariants = dynamicVariants.toMutableList().apply {
                                                                    this[vIdx] = v.copy(price = p)
                                                                }
                                                            },
                                                            label = { Text("Price (₹)", fontSize = 11.sp) },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = RoyalBlue,
                                                                unfocusedBorderColor = BrandBorder,
                                                                focusedContainerColor = Color.White,
                                                                unfocusedContainerColor = Color.White
                                                            )
                                                        )

                                                        // Variant Stock
                                                        OutlinedTextField(
                                                            value = v.stock.toString(),
                                                            onValueChange = { newStockStr ->
                                                                val s = newStockStr.toIntOrNull() ?: 0
                                                                dynamicVariants = dynamicVariants.toMutableList().apply {
                                                                    this[vIdx] = v.copy(stock = s)
                                                                }
                                                            },
                                                            label = { Text("Stock units", fontSize = 11.sp) },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = RoyalBlue,
                                                                unfocusedBorderColor = BrandBorder,
                                                                focusedContainerColor = Color.White,
                                                                unfocusedContainerColor = Color.White
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 6: Handmade Details (Image 1)
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
                                    text = "Handmade details",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "Set accurate expectations for your one-of-a-kind product.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )

                                FieldHeader("Materials used", isRequired = true)
                                OutlinedTextField(
                                    value = materialsUsed,
                                    onValueChange = { materialsUsed = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                FieldHeader("Yarn type", isRequired = false)
                                OutlinedTextField(
                                    value = yarnType,
                                    onValueChange = { yarnType = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                FieldHeader("Yarn colour", isRequired = false)
                                OutlinedTextField(
                                    value = yarnColour,
                                    onValueChange = { yarnColour = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                FieldHeader("Production type", isRequired = false)
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { showProductionTypeMenu = true },
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
                                            Text(text = productionType, fontSize = 14.sp, color = MidnightNavy)
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showProductionTypeMenu,
                                        onDismissRequest = { showProductionTypeMenu = false }
                                    ) {
                                        productionTypeOptions.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = {
                                                    productionType = opt
                                                    showProductionTypeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                FieldHeader("Estimated production time", isRequired = false)
                                OutlinedTextField(
                                    value = estimatedProductionTime,
                                    onValueChange = { estimatedProductionTime = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Customisation available", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Text(text = "Customers can submit personalised instructions", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = customisationAvailable,
                                        onCheckedChange = { customisationAvailable = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                    )
                                }

                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                FieldHeader("Care instructions", isRequired = false)
                                OutlinedTextField(
                                    value = careInstructions,
                                    onValueChange = { careInstructions = it },
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
                    // STEP 7: Shipping Information (Image 2)
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
                                    text = "Shipping information",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "Used to calculate accurate courier rates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FieldHeader("Weight", isRequired = false)
                                        OutlinedTextField(
                                            value = weightText,
                                            onValueChange = { weightText = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = RoyalBlue,
                                                unfocusedBorderColor = BrandBorder
                                            )
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        FieldHeader("Package type", isRequired = false)
                                        Box {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { showPackageTypeMenu = true },
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
                                                    Text(text = packageType, fontSize = 14.sp, color = MidnightNavy)
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = showPackageTypeMenu,
                                                onDismissRequest = { showPackageTypeMenu = false }
                                            ) {
                                                packageTypeOptions.forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt) },
                                                        onClick = {
                                                            packageType = opt
                                                            showPackageTypeMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "Packed parcel dimensions",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Length", fontSize = 12.sp, color = TextMuted)
                                        OutlinedTextField(
                                            value = parcelLength,
                                            onValueChange = { parcelLength = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Width", fontSize = 12.sp, color = TextMuted)
                                        OutlinedTextField(
                                            value = parcelWidth,
                                            onValueChange = { parcelWidth = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Height", fontSize = 12.sp, color = TextMuted)
                                        OutlinedTextField(
                                            value = parcelHeight,
                                            onValueChange = { parcelHeight = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                FieldHeader("Processing time", isRequired = false)
                                OutlinedTextField(
                                    value = processingTime,
                                    onValueChange = { processingTime = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalBlue,
                                        unfocusedBorderColor = BrandBorder
                                    )
                                )

                                FieldHeader("Shipping note", isRequired = false)
                                OutlinedTextField(
                                    value = shippingNote,
                                    onValueChange = { shippingNote = it },
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
                    // STEP 8: Storefront Visibility (Image 3)
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
                                    text = "Storefront visibility",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "Choose how and where this product appears.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )

                                // Toggle 1: Publish immediately
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Publish immediately", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Text(text = "Make visible after product is saved", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = publishImmediately,
                                        onCheckedChange = { publishImmediately = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                    )
                                }

                                // Toggle 2: Featured product
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Featured product", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Text(text = "Highlight in featured collections", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = featuredProduct,
                                        onCheckedChange = { featuredProduct = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                    )
                                }

                                // Toggle 3: Show on homepage
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Show on homepage", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Text(text = "Include in the homepage product rail", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = showOnHomepage,
                                        onCheckedChange = { showOnHomepage = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                    )
                                }

                                // Toggle 4: Display stock status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Display stock status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Text(text = "Show low-stock messaging to customers", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = displayStockStatus,
                                        onCheckedChange = { displayStockStatus = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                    )
                                }

                                // Toggle 5: Allow customer customisation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Allow customer customisation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MidnightNavy)
                                        Text(text = "Show a customisation note field", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = allowCustomerCustomisation,
                                        onCheckedChange = { allowCustomerCustomisation = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Storefront Preview Box
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF0F7FF),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFE2E8F0))
                                        ) {
                                            val u = imagesList.firstOrNull() ?: ""
                                            AdminProductImage(
                                                model = u,
                                                contentDescription = null,
                                                serverUrl = serverUrl,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "STOREFRONT PREVIEW",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalBlue,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = productName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MidnightNavy
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "₹$sellingPriceText",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MidnightNavy
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Only ${availableQuantityText} left",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD97706)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 9: Ready to Publish (Image 4)
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Ready to publish",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "Review your product details before it goes live.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )

                                // Product Top Summary Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE2E8F0))
                                    ) {
                                        val u = imagesList.firstOrNull() ?: ""
                                        AdminProductImage(
                                            model = u,
                                            contentDescription = null,
                                            serverUrl = serverUrl,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = productName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MidnightNavy)
                                        Text(text = "$category · $productionType", fontSize = 12.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "₹$sellingPriceText", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MidnightNavy)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "Ready",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                // Review Checklist Rows
                                val comboCount = colorChips.size * sizeChips.size

                                ReviewChecklistRow(title = "Basic information", onEdit = { currentStep = 1 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "${imagesList.size} product images", onEdit = { currentStep = 2 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "Pricing & margin", onEdit = { currentStep = 3 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "$availableQuantityText units in stock", onEdit = { currentStep = 4 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "$comboCount variant combinations", onEdit = { currentStep = 5 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "Handmade details", onEdit = { currentStep = 6 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "Shipping information", onEdit = { currentStep = 7 })
                                HorizontalDivider(color = BrandBorder.copy(alpha = 0.6f))

                                ReviewChecklistRow(title = "Storefront visibility", onEdit = { currentStep = 8 })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ==========================================
        // FLOATING TOAST NOTIFICATION (Image 5)
        // ==========================================
        AnimatedVisibility(
            visible = showSavedToast,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Product saved",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your changes have been saved successfully.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    IconButton(
                        onClick = { showSavedToast = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (showAddColorDialog) {
            AlertDialog(
                onDismissRequest = { showAddColorDialog = false },
                title = {
                    Text(
                        text = "Add Colour Option",
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter the name of the new colour option for this product:",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newColorInput,
                            onValueChange = { newColorInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = BrandBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newColorInput.isNotBlank()) {
                                val updated = (colorChips + newColorInput.trim()).distinct()
                                colorChips = updated
                                dynamicVariants = regenerateVariants(updated, sizeChips, dynamicVariants)
                                newColorInput = ""
                                showAddColorDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Add Colour", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddColorDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        if (showAddSizeDialog) {
            AlertDialog(
                onDismissRequest = { showAddSizeDialog = false },
                title = {
                    Text(
                        text = "Add Size Option",
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter the name of the new size option for this product:",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newSizeInput,
                            onValueChange = { newSizeInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = BrandBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSizeInput.isNotBlank()) {
                                val updated = (sizeChips + newSizeInput.trim()).distinct()
                                sizeChips = updated
                                dynamicVariants = regenerateVariants(colorChips, updated, dynamicVariants)
                                newSizeInput = ""
                                showAddSizeDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Add Size", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSizeDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // Direct Custom Variant Dialog
        if (showAddVariantDialog) {
            AlertDialog(
                onDismissRequest = { showAddVariantDialog = false },
                title = {
                    Text(
                        text = "Add Custom Variant",
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add an individual option with custom pricing and stock:",
                            fontSize = 13.sp,
                            color = TextMuted
                        )

                        // Variant Name
                        OutlinedTextField(
                            value = newVariantName,
                            onValueChange = { newVariantName = it },
                            label = { Text("Variant Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = BrandBorder
                            )
                        )

                        // Color & Size Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newVariantColor,
                                onValueChange = { newVariantColor = it },
                                label = { Text("Colour") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )
                            OutlinedTextField(
                                value = newVariantSize,
                                onValueChange = { newVariantSize = it },
                                label = { Text("Size") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )
                        }

                        // Price & Stock Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newVariantPrice,
                                onValueChange = { newVariantPrice = it },
                                label = { Text("Price (₹) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )
                            OutlinedTextField(
                                value = newVariantStock,
                                onValueChange = { newVariantStock = it },
                                label = { Text("Stock Units") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = BrandBorder
                                )
                            )
                        }

                        // SKU
                        OutlinedTextField(
                            value = newVariantSku,
                            onValueChange = { newVariantSku = it },
                            label = { Text("Custom SKU (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = BrandBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = newVariantName.trim().ifBlank {
                                val col = newVariantColor.trim()
                                val sz = newVariantSize.trim()
                                when {
                                    col.isNotBlank() && sz.isNotBlank() -> "$col / $sz"
                                    col.isNotBlank() -> col
                                    sz.isNotBlank() -> sz
                                    else -> "Custom Edition"
                                }
                            }
                            val p = newVariantPrice.toDoubleOrNull() ?: sellingPriceText.toDoubleOrNull() ?: 0.0
                            val s = newVariantStock.toIntOrNull() ?: availableQuantityText.toIntOrNull() ?: 0
                            val autoSku = newVariantSku.trim().ifBlank {
                                val code = name.filter { it.isLetter() }.take(3).uppercase()
                                "$sku-$code"
                            }
                            val varId = "var-${System.currentTimeMillis() % 100000}"

                            val customVariant = ProductVariant(
                                id = varId,
                                name = name,
                                color = newVariantColor.trim().ifBlank { null },
                                size = newVariantSize.trim().ifBlank { null },
                                colorHex = "#255DCE",
                                price = p,
                                originalPrice = originalPriceText.toDoubleOrNull(),
                                stock = s,
                                sku = autoSku
                            )

                            dynamicVariants = dynamicVariants + customVariant

                            if (newVariantColor.isNotBlank() && !colorChips.contains(newVariantColor.trim())) {
                                colorChips = colorChips + newVariantColor.trim()
                            }
                            if (newVariantSize.isNotBlank() && !sizeChips.contains(newVariantSize.trim())) {
                                sizeChips = sizeChips + newVariantSize.trim()
                            }

                            newVariantName = ""
                            newVariantColor = ""
                            newVariantSize = ""
                            newVariantPrice = ""
                            newVariantStock = ""
                            newVariantSku = ""
                            showAddVariantDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Add Variant", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVariantDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun ReviewChecklistRow(
    title: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MidnightNavy
            )
        }

        Text(
            text = "Edit",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = RoyalBlue,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onEdit)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
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
