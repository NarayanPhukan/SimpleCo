package com.babyygurll.admin.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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

    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "plushies") }
    var priceText by remember { mutableStateOf(product?.price?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var origPriceText by remember { mutableStateOf(product?.originalPrice?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var badge by remember { mutableStateOf(product?.badge ?: "Handcrafted") }
    var leadTime by remember { mutableStateOf(product?.leadTime ?: "Ready to ship (1-2 days)") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var inStock by remember { mutableStateOf(product?.inStock ?: true) }

    // Multi-photo gallery state
    val initialImages = remember(product) {
        if (product != null) {
            if (product.images.isNotEmpty()) {
                product.images
            } else if (product.image.isNotBlank()) {
                listOf(product.image)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    var imagesList by remember { mutableStateOf<List<String>>(initialImages) }
    var localBitmapsList by remember { mutableStateOf<List<Bitmap?>>(List(initialImages.size) { null }) }
    var activePreviewIndex by remember { mutableStateOf(0) }
    var isProcessingImages by remember { mutableStateOf(false) }

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

    // Local Device Multi-Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessingImages = true
                val newImages = mutableListOf<String>()
                val newBitmaps = mutableListOf<Bitmap>()

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

                            newBitmaps.add(scaledBitmap)
                            newImages.add("data:image/jpeg;base64,$base64String")
                        }
                    } catch (e: Exception) {
                        // Skip unreadable files
                    }
                }

                withContext(Dispatchers.Main) {
                    imagesList = imagesList + newImages
                    localBitmapsList = localBitmapsList + newBitmaps
                    isProcessingImages = false
                }
            }
        }
    }

    // Photo Management Callbacks
    val removeImageAt: (Int) -> Unit = { index ->
        if (index in imagesList.indices) {
            val updatedImages = imagesList.toMutableList().apply { removeAt(index) }
            val updatedBitmaps = localBitmapsList.toMutableList().apply {
                if (index < size) removeAt(index)
            }
            imagesList = updatedImages
            localBitmapsList = updatedBitmaps
            if (activePreviewIndex >= updatedImages.size) {
                activePreviewIndex = (updatedImages.size - 1).coerceAtLeast(0)
            }
        }
    }

    val makeCover: (Int) -> Unit = { index ->
        if (index in 1 until imagesList.size) {
            val targetImg = imagesList[index]
            val targetBitmap = localBitmapsList.getOrNull(index)

            val updatedImages = imagesList.toMutableList().apply {
                removeAt(index)
                add(0, targetImg)
            }
            val updatedBitmaps = localBitmapsList.toMutableList().apply {
                if (index < size) {
                    removeAt(index)
                    add(0, targetBitmap)
                }
            }
            imagesList = updatedImages
            localBitmapsList = updatedBitmaps
            activePreviewIndex = 0
        }
    }

    val addPresetPhoto: (String) -> Unit = { presetUrl ->
        imagesList = imagesList + presetUrl
        localBitmapsList = localBitmapsList + listOf<Bitmap?>(null)
    }

    val addAllSamplePresets: () -> Unit = {
        val sampleSet = listOf(
            "/assets/images/strawberry-cow.jpg",
            "/assets/images/tulip-bouquet.jpg",
            "/assets/images/granny-bag.jpg",
            "/assets/images/crochet-cardigan.jpg"
        )
        imagesList = imagesList + sampleSet
        localBitmapsList = localBitmapsList + List(sampleSet.size) { null }
    }

    // Active image resolver for Customer POV preview
    val activeImgUrl = imagesList.getOrNull(activePreviewIndex) ?: ""
    val activeLocalBitmap = localBitmapsList.getOrNull(activePreviewIndex)

    val parsedActiveBase64Bitmap: Bitmap? = remember(activeImgUrl) {
        if (activeLocalBitmap == null && activeImgUrl.startsWith("data:image/") && activeImgUrl.contains(";base64,")) {
            try {
                val base64Data = activeImgUrl.substringAfter(";base64,")
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val activeDisplayBitmap = activeLocalBitmap ?: parsedActiveBase64Bitmap

    val resolvedActiveUrl = remember(activeImgUrl, serverUrl) {
        when {
            activeImgUrl.isBlank() -> ""
            activeImgUrl.startsWith("data:") -> activeImgUrl
            activeImgUrl.startsWith("http://") || activeImgUrl.startsWith("https://") -> activeImgUrl
            else -> "${serverUrl.trimEnd('/')}/${activeImgUrl.trimStart('/')}"
        }
    }

    val priceVal = priceText.toDoubleOrNull() ?: 0.0
    val origPriceVal = origPriceText.toDoubleOrNull()
    val discountPercent = if (origPriceVal != null && origPriceVal > priceVal && priceVal > 0) {
        (((origPriceVal - priceVal) / origPriceVal) * 100).toInt()
    } else 0

    // Validation: Minimum 4 photos required for new creations; existing pieces can be updated with current photos
    val hasMinPhotos = if (product != null) imagesList.isNotEmpty() else imagesList.size >= 4
    val isPublishEnabled = name.isNotBlank() && priceVal > 0 && hasMinPhotos && !isProcessingImages

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
            // Segmented Tab Switcher: [ Edit Details ] | [ Customer Preview ]
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
                                Text(
                                    text = "Edit Details",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
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
                                Text(
                                    text = "Customer Preview",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
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

                    // Product Photography Card (Multi-Photo, Minimum 4)
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
                                // Header with photo count requirement badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Product Photography",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )

                                    Surface(
                                        color = if (hasMinPhotos) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (hasMinPhotos) Color(0xFF81C784) else Color(0xFFFFB74D)
                                        )
                                    ) {
                                        Text(
                                            text = if (hasMinPhotos) "✓ ${imagesList.size} Photos (Ready)" else "${imagesList.size}/4 Required",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasMinPhotos) Color(0xFF2E7D32) else Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Minimum 4 Warning / Success Banner
                                if (!hasMinPhotos) {
                                    Surface(
                                        color = Color(0xFFFFF8E1),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFF57F17),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Minimum 4 Product Photos Required",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFBF360C)
                                                )
                                                Text(
                                                    text = "Please add ${4 - imagesList.size} more photo${if (4 - imagesList.size > 1) "s" else ""} from different angles to showcase your handcrafted piece.",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF4E342E)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "✓ Ready: ${imagesList.size} photos selected. First photo will be your boutique cover.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1B5E20)
                                            )
                                        }
                                    }
                                }

                                // Primary Button: Pick from Local Device
                                Button(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (imagesList.isEmpty()) "Choose Photos from Device (Min 4)" else "+ Add More Photos from Device",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }

                                if (isProcessingImages) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Optimizing and encoding photos...",
                                            fontSize = 11.sp,
                                            color = CharcoalGray.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                // Photo Management Strip / Gallery
                                if (imagesList.isNotEmpty()) {
                                    Text(
                                        text = "Selected Photos (${imagesList.size}) — Tap to preview, #1 is boutique Cover:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CharcoalGray
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        imagesList.forEachIndexed { index, imgUrl ->
                                            val isCover = index == 0
                                            val isSelected = activePreviewIndex == index
                                            val itemBitmap = localBitmapsList.getOrNull(index)
                                            val resolvedThumb = remember(imgUrl, serverUrl) {
                                                when {
                                                    imgUrl.isBlank() -> ""
                                                    imgUrl.startsWith("data:") -> imgUrl
                                                    imgUrl.startsWith("http://") || imgUrl.startsWith("https://") -> imgUrl
                                                    else -> "${serverUrl.trimEnd('/')}/${imgUrl.trimStart('/')}"
                                                }
                                            }

                                            Card(
                                                modifier = Modifier
                                                    .width(105.dp)
                                                    .height(135.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) RoyalBlue else Color(0xFFE2E8F0)
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                                            ) {
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(98.dp)
                                                            .clickable { activePreviewIndex = index }
                                                    ) {
                                                        if (itemBitmap != null) {
                                                            Image(
                                                                bitmap = itemBitmap.asImageBitmap(),
                                                                contentDescription = "Photo ${index + 1}",
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        } else {
                                                            AsyncImage(
                                                                model = resolvedThumb,
                                                                contentDescription = "Photo ${index + 1}",
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }

                                                        // Tag: Cover vs Index
                                                        Surface(
                                                            color = if (isCover) BrandPrimary else Color.Black.copy(alpha = 0.65f),
                                                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                                                            modifier = Modifier.align(Alignment.TopStart)
                                                        ) {
                                                            Text(
                                                                text = if (isCover) "★ COVER" else "#${index + 1}",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }

                                                        // Delete Button
                                                        IconButton(
                                                            onClick = { removeImageAt(index) },
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .align(Alignment.TopEnd)
                                                                .padding(2.dp)
                                                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Remove photo",
                                                                tint = CrimsonAccent,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }

                                                    // Bottom Action Slot
                                                    if (isCover) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color(0xFFE8F5E9)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "Main Cover",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF2E7D32)
                                                            )
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(BrandPrimaryLight.copy(alpha = 0.4f))
                                                                .clickable { makeCover(index) },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "Make Cover",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = RoyalBlue
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Dashed "+ Add More" card slot
                                        Box(
                                            modifier = Modifier
                                                .width(105.dp)
                                                .height(135.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BrandPrimaryLight.copy(alpha = 0.25f))
                                                .border(
                                                    1.5.dp,
                                                    RoyalBlue.copy(alpha = 0.4f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { photoPickerLauncher.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddPhotoAlternate,
                                                    contentDescription = "Add More",
                                                    tint = RoyalBlue,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "+ Add More",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoyalBlue
                                                )
                                                Text(
                                                    text = "from device",
                                                    fontSize = 9.sp,
                                                    color = CharcoalGray.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Empty state helper box
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFAFAFA),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Collections,
                                                contentDescription = null,
                                                tint = CharcoalGray.copy(alpha = 0.4f),
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No photos added yet",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = CharcoalGray
                                            )
                                            Text(
                                                text = "Tap 'Choose Photos from Device' above or select studio presets below. Minimum 4 required to publish.",
                                                fontSize = 11.sp,
                                                color = CharcoalGray.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                }

                                // Quick Studio Presets
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Or Add Studio Presets (+):", style = MaterialTheme.typography.labelSmall)
                                    TextButton(
                                        onClick = { addAllSamplePresets() },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("+ Add 4 Sample Photos", fontSize = 11.sp, color = RoyalBlue)
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    photoPresets.forEach { (title, url) ->
                                        FilterChip(
                                            selected = false,
                                            onClick = { addPresetPhoto(url) },
                                            label = { Text("+ $title") },
                                            colors = FilterChipDefaults.filterChipColors(
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
                            Text("Preview Customer Point of View (${imagesList.size} Photos) 👁️", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Save / Publish Button
                    item {
                        Button(
                            onClick = {
                                val catObj = categories.find { it.first == category }
                                val primaryImage = imagesList.firstOrNull() ?: "/assets/images/strawberry-cow.jpg"
                                val req = ProductCreateRequest(
                                    name = name.trim(),
                                    category = category,
                                    categoryName = catObj?.second,
                                    price = priceVal,
                                    originalPrice = origPriceVal,
                                    image = primaryImage,
                                    images = imagesList,
                                    badge = badge.trim().ifBlank { null },
                                    leadTime = leadTime.trim().ifBlank { null },
                                    description = description.trim(),
                                    inStock = inStock
                                )
                                onSave(req, product?.id)
                            },
                            enabled = isPublishEnabled,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFCCCCCC),
                                disabledContentColor = Color(0xFF666666)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasMinPhotos) {
                                    if (product != null) "Save Changes ✓" else "Publish Piece to Boutique (${imagesList.size} Photos) ✓"
                                } else {
                                    "Add ${4 - imagesList.size} More Photo${if (4 - imagesList.size > 1) "s" else ""} to Launch"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (!hasMinPhotos && product == null) {
                            Text(
                                text = "⚠️ A minimum of 4 photos is required to launch a new product in the boutique.",
                                fontSize = 11.sp,
                                color = CrimsonAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
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
                                        text = "Experience your multi-photo gallery exactly as buyers see it in the online store.",
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
                                // Product Hero Image Showcase
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                        .background(Color(0xFFF9F7F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        activeDisplayBitmap != null -> {
                                            Image(
                                                bitmap = activeDisplayBitmap.asImageBitmap(),
                                                contentDescription = name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        resolvedActiveUrl.isNotBlank() -> {
                                            AsyncImage(
                                                model = resolvedActiveUrl,
                                                contentDescription = name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        else -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(24.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(CircleShape)
                                                        .background(BrandPrimaryLight),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AddPhotoAlternate,
                                                        contentDescription = null,
                                                        tint = BrandPrimary,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Piece Showcase Preview",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MidnightNavy
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Select at least 4 photos from device to preview customer angles.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = CharcoalGray.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                )
                                            }
                                        }
                                    }

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

                                    // Multi-photo count indicator in Top-Right
                                    if (imagesList.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(bottomStart = 10.dp),
                                            color = Color.Black.copy(alpha = 0.65f),
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.PhotoCamera,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${activePreviewIndex + 1} / ${imagesList.size}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Wishlist icon (Customer UI imitation)
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.9f),
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
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

                                // Interactive Customer Multi-Photo Thumbnail Strip
                                if (imagesList.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9))
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = "Customer Gallery Views (tap thumbnail to switch angle):",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CharcoalGray.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            imagesList.forEachIndexed { idx, url ->
                                                val isSelectedAngle = activePreviewIndex == idx
                                                val thumbBm = localBitmapsList.getOrNull(idx)
                                                val resUrl = remember(url, serverUrl) {
                                                    when {
                                                        url.isBlank() -> ""
                                                        url.startsWith("data:") -> url
                                                        url.startsWith("http://") || url.startsWith("https://") -> url
                                                        else -> "${serverUrl.trimEnd('/')}/${url.trimStart('/')}"
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(
                                                        width = if (isSelectedAngle) 2.5.dp else 1.dp,
                                                        color = if (isSelectedAngle) RoyalBlue else Color(0xFFCBD5E1)
                                                    ),
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clickable { activePreviewIndex = idx }
                                                ) {
                                                    if (thumbBm != null) {
                                                        Image(
                                                            bitmap = thumbBm.asImageBitmap(),
                                                            contentDescription = "Angle ${idx + 1}",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        AsyncImage(
                                                            model = resUrl,
                                                            contentDescription = "Angle ${idx + 1}",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
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
                                val primaryImage = imagesList.firstOrNull() ?: "/assets/images/strawberry-cow.jpg"
                                val req = ProductCreateRequest(
                                    name = name.trim(),
                                    category = category,
                                    categoryName = catObj?.second,
                                    price = priceVal,
                                    originalPrice = origPriceVal,
                                    image = primaryImage,
                                    images = imagesList,
                                    badge = badge.trim().ifBlank { null },
                                    leadTime = leadTime.trim().ifBlank { null },
                                    description = description.trim(),
                                    inStock = inStock
                                )
                                onSave(req, product?.id)
                            },
                            enabled = isPublishEnabled,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandSecondary,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFCCCCCC),
                                disabledContentColor = Color(0xFF666666)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasMinPhotos) {
                                    if (product != null) "Everything Looks Great — Save Changes ✓" else "Everything Looks Great — Publish Now (${imagesList.size} Photos) ✓"
                                } else {
                                    "Need ${4 - imagesList.size} More Photo${if (4 - imagesList.size > 1) "s" else ""} to Publish"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (!hasMinPhotos && product == null) {
                            Text(
                                text = "⚠️ Minimum 4 product photos required to launch. Switch to 'Edit Details' tab to add more photos.",
                                fontSize = 11.sp,
                                color = CrimsonAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
