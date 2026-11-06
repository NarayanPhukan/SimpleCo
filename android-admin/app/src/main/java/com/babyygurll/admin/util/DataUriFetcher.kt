package com.babyygurll.admin.util

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options

class DataUriFetcher(
    private val dataUri: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val base64Prefix = ";base64,"
        val base64Data = when {
            dataUri.contains(base64Prefix, ignoreCase = true) -> dataUri.substringAfter(base64Prefix).trim()
            dataUri.startsWith("data:", ignoreCase = true) && dataUri.contains(",") -> dataUri.substringAfter(",").trim()
            else -> return null
        }

        return try {
            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null

            DrawableResult(
                drawable = BitmapDrawable(options.context.resources, bitmap),
                isSampled = false,
                dataSource = DataSource.MEMORY
            )
        } catch (e: Exception) {
            null
        }
    }

    class Factory : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.startsWith("data:", ignoreCase = true)) {
                return DataUriFetcher(data, options)
            }
            return null
        }
    }
}

