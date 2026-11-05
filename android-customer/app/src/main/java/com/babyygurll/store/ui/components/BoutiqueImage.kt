package com.babyygurll.store.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.babyygurll.store.ui.theme.BrandCream
import com.babyygurll.store.util.ImageUrlUtils

/**
 * Robust boutique image renderer for BabyyGurll e-commerce pieces.
 * 
 * Instantly renders:
 * 1. Base64 JPEG/PNG Data URIs (bypassing Coil mapper, zero latency bitmap decode).
 * 2. Absolute HTTP/HTTPS URLs (resolved via ImageUrlUtils with Coil crossfade & disk caching).
 * 3. Fallback artisanal crochet emoji placeholder.
 */
@Composable
fun BoutiqueImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderText: String = "🧶"
) {
    val context = LocalContext.current
    val resolved = remember(imageUrl) {
        ImageUrlUtils.resolveImageUrl(imageUrl)
    }

    // Direct in-memory Base64 decoding
    val base64Bitmap: Bitmap? = remember(resolved) {
        when {
            resolved.startsWith("data:", ignoreCase = true) && resolved.contains(";base64,", ignoreCase = true) -> {
                try {
                    val base64Index = resolved.indexOf(";base64,", ignoreCase = true)
                    val data = resolved.substring(base64Index + 8).trim()
                    val bytes = Base64.decode(data, Base64.DEFAULT)
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                } catch (e: Exception) {
                    null
                }
            }
            resolved.startsWith("/9j/") || resolved.startsWith("iVBORw0KGgo") -> {
                try {
                    val bytes = Base64.decode(resolved.trim(), Base64.DEFAULT)
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    Box(
        modifier = modifier.background(BrandCream),
        contentAlignment = Alignment.Center
    ) {
        when {
            base64Bitmap != null -> {
                Image(
                    bitmap = base64Bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            resolved.isNotBlank() -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(resolved)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            else -> {
                Text(placeholderText, fontSize = 36.sp)
            }
        }
    }
}
