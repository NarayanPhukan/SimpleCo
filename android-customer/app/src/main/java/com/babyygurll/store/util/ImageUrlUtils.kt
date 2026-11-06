package com.babyygurll.store.util

import com.babyygurll.store.data.api.ApiClient

object ImageUrlUtils {

    /**
     * Resolves a raw image string into an absolute network URL or valid Data URI.
     *
     * Handles:
     * 1. Null or blank strings -> ""
     * 2. Data URIs (data:image/...) -> returned as-is
     * 3. Absolute URLs (http://, https://, content://, file://) -> returned as-is
     * 4. Relative paths (/assets/images/..., assets/images/...) -> prepends backend origin
     * 5. Plain filenames (e.g. "strawberry-cow.jpg") -> prepends backend origin + /assets/images/
     */
    fun resolveImageUrl(rawUrl: String?, baseUrl: String = ApiClient.DEFAULT_BASE_URL): String {
        if (rawUrl.isNullOrBlank()) return ""
        val trimmed = rawUrl.trim()

        // 1. Protocol URLs
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("content://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)
        ) {
            return trimmed
        }

        // 2. Data URIs
        if (trimmed.startsWith("data:", ignoreCase = true)) {
            return trimmed
        }

        // 2b. Raw Base64 string without data: prefix
        if (trimmed.startsWith("/9j/")) {
            return "data:image/jpeg;base64,$trimmed"
        }
        if (trimmed.startsWith("iVBORw0KGgo")) {
            return "data:image/png;base64,$trimmed"
        }

        // 3. Resolve against server origin (strip trailing /api or /api/)
        val serverHost = baseUrl.substringBefore("/api").trimEnd('/')
        val cleanPath = trimmed.trimStart('/')

        return when {
            cleanPath.startsWith("assets/", ignoreCase = true) -> "$serverHost/$cleanPath"
            cleanPath.contains("/") -> "$serverHost/$cleanPath"
            else -> "$serverHost/assets/images/$cleanPath"
        }
    }
}

