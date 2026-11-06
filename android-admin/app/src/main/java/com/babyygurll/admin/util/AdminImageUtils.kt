package com.babyygurll.admin.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.babyygurll.admin.data.repository.PreferencesManager

object AdminImageUtils {

    /**
     * Resolves a raw image string into an absolute network URL or valid Data URI.
     * Prevents prepending serverUrl to Data URIs (data:image/...) which caused images to fail.
     */
    fun resolveImageUrl(rawUrl: String?, serverUrl: String = PreferencesManager.DEFAULT_SERVER_URL): String {
        if (rawUrl.isNullOrBlank()) return ""
        val trimmed = rawUrl.trim()

        // 1. Data URIs - must NEVER have serverUrl prepended
        if (trimmed.startsWith("data:", ignoreCase = true)) {
            return trimmed
        }

        // 2. Raw Base64 string without data: prefix
        if (trimmed.startsWith("/9j/") || trimmed.startsWith("iVBORw0KGgo")) {
            return "data:image/jpeg;base64,$trimmed"
        }

        // 3. Absolute network or local URIs
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("content://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)
        ) {
            return trimmed
        }

        // 4. Relative paths -> resolve against server base URL
        val base = serverUrl.trimEnd('/')
        val path = trimmed.trimStart('/')
        return if (path.startsWith("assets/", ignoreCase = true) || path.contains("/")) {
            "$base/$path"
        } else {
            "$base/assets/images/$path"
        }
    }

    /**
     * Directly decodes Base64 into Android Bitmap in-memory with zero latency.
     */
    fun decodeBase64Bitmap(dataUriOrRaw: String): Bitmap? {
        return try {
            val base64Data = when {
                dataUriOrRaw.contains(";base64,", ignoreCase = true) -> dataUriOrRaw.substringAfter(";base64,").trim()
                dataUriOrRaw.startsWith("data:", ignoreCase = true) && dataUriOrRaw.contains(",") -> dataUriOrRaw.substringAfter(",").trim()
                else -> dataUriOrRaw.trim()
            }
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Universal Image Component for BabyyGurll Admin.
 * Flawlessly renders:
 * 1. Base64 Data URIs (data:image/jpeg;base64,...) via instant zero-latency Bitmap decode
 * 2. Raw Base64 strings (/9j/...)
 * 3. Absolute HTTP/HTTPS URLs via Coil crossfade
 * 4. Relative paths (/uploads/...) resolved against serverUrl
 * 5. Fallback icons or emoji when image is missing or loading fails
 */
@Composable
fun AdminProductImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    serverUrl: String = PreferencesManager.DEFAULT_SERVER_URL,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: ImageVector = Icons.Outlined.ShoppingBag,
    fallbackEmoji: String? = null
) {
    val context = LocalContext.current
    val resolved = remember(model, serverUrl) {
        AdminImageUtils.resolveImageUrl(model, serverUrl)
    }

    val isBase64 = remember(resolved) {
        resolved.startsWith("data:", ignoreCase = true) ||
            resolved.startsWith("/9j/") ||
            resolved.startsWith("iVBORw0KGgo")
    }

    val base64Bitmap: Bitmap? = remember(resolved) {
        if (isBase64) AdminImageUtils.decodeBase64Bitmap(resolved) else null
    }

    Box(
        modifier = modifier.background(Color(0xFFF1F5F9)),
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
            resolved.isNotBlank() && !isBase64 -> {
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
                if (fallbackEmoji != null) {
                    Text(text = fallbackEmoji, fontSize = 24.sp)
                } else {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

