package com.babyygurll.store.data.repository

import android.content.Context
import com.babyygurll.store.data.api.ApiClient
import com.babyygurll.store.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository(private val context: Context) {

    private val api get() = ApiClient.getService(context)
    private val prefs = context.getSharedPreferences("babyygurll_catalog_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCachedProducts(): List<Product> {
        return try {
            val json = prefs.getString("cached_products_json", null) ?: return emptyList()
            val type = object : TypeToken<List<Product>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCachedProducts(products: List<Product>) {
        try {
            val json = gson.toJson(products)
            prefs.edit().putString("cached_products_json", json).apply()
        } catch (e: Exception) {
            // Ignore cache error
        }
    }

    suspend fun getProducts(category: String? = null, search: String? = null, sort: String? = null): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            try {
                val catQuery = if (category.isNullOrBlank() || category == "all") null else category
                val searchQuery = search?.trim()?.ifBlank { null }
                val response = api.getProducts(catQuery, searchQuery, sort)
                if (response.isSuccessful && response.body()?.data != null) {
                    val list = response.body()!!.data!!
                    if (list.isNotEmpty() && catQuery == null && searchQuery == null) {
                        saveCachedProducts(list)
                    }
                    Result.success(list)
                } else {
                    val cached = getCachedProducts()
                    if (cached.isNotEmpty()) {
                        Result.success(cached)
                    } else {
                        Result.failure(Exception(response.body()?.message ?: "Failed to load products"))
                    }
                }
            } catch (e: Exception) {
                val cached = getCachedProducts()
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(e)
                }
            }
        }

    suspend fun getProductById(id: String): Result<Product> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getProductById(id)
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Product not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getStoreSettings(): Result<StoreSetting> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getStoreSettings()
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.success(StoreSetting()) // Graceful fallback
                }
            } catch (e: Exception) {
                Result.success(StoreSetting()) // Offline fallback
            }
        }

    suspend fun getCustomerAppVersion(): Result<AppVersionInfo> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getCustomerAppVersion()
                if (response.isSuccessful && response.body()?.data != null) {
                    return@withContext Result.success(response.body()!!.data!!)
                }
            } catch (e: Exception) {
                // Render cold start fallback
            }

            try {
                val rawUrl = "https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/client/public/android-customer-version.json"
                val connection = java.net.URL(rawUrl).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.requestMethod = "GET"
                if (connection.responseCode == 200) {
                    val stream = connection.inputStream.bufferedReader().use { it.readText() }
                    val info = com.google.gson.Gson().fromJson(stream, AppVersionInfo::class.java)
                    if (info != null) {
                        return@withContext Result.success(info)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }

            Result.failure(Exception("Failed to get customer app version"))
        }

    suspend fun getCoupons(): Result<List<Coupon>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getCoupons()
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.success(emptyList())
                }
            } catch (e: Exception) {
                Result.success(emptyList())
            }
        }

    suspend fun validateCoupon(code: String, cartSubtotal: Double): Result<Coupon> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.validateCoupon(mapOf("code" to code, "cartSubtotal" to cartSubtotal))
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Invalid coupon code"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun checkPincode(pincode: String): Result<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.checkPincode(pincode.trim())
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "PIN code not serviceable"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Default boutique alerts when backend is starting or offline
    private val defaultBoutiqueNotifications = listOf(
        StoreNotification(
            id = "notif_welcome_cust",
            recipient = "all",
            role = "customer",
            title = "Welcome to BabyyGurll Boutique 🌸",
            message = "Discover our handmade crochet scrunchies, plushies, bags & accessories crafted with love.",
            type = "promotion",
            isRead = false,
            createdAt = java.time.Instant.now().toString()
        ),
        StoreNotification(
            id = "notif_dept_cust",
            recipient = "all",
            role = "customer",
            title = "8 Handcrafted Boutique Departments 🧵",
            message = "Explore Hair Accessories, Bags & Purses, Flowers & Bouquets, Plushies, and more!",
            type = "promotion",
            isRead = false,
            createdAt = java.time.Instant.now().minusSeconds(1800).toString()
        ),
        StoreNotification(
            id = "notif_ship_cust",
            recipient = "all",
            role = "customer",
            title = "Complimentary Shipping Across India ✨",
            message = "Enjoy free tracked shipping on all orders over ₹999 across all pin codes.",
            type = "promotion",
            isRead = false,
            createdAt = java.time.Instant.now().minusSeconds(3600).toString()
        )
    )

    // Notifications
    suspend fun getNotifications(userEmail: String): Result<Pair<List<StoreNotification>, Int>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getNotifications(userEmail = userEmail, role = "customer")
                val body = response.body()
                if (response.isSuccessful && body != null && body.data.isNotEmpty()) {
                    Result.success(Pair(body.data, body.unreadCount))
                } else {
                    // Graceful fallback to initial boutique alerts
                    Result.success(Pair(defaultBoutiqueNotifications, defaultBoutiqueNotifications.count { !it.isRead }))
                }
            } catch (e: Exception) {
                // Graceful fallback on network error/timeout/backend waking up
                Result.success(Pair(defaultBoutiqueNotifications, defaultBoutiqueNotifications.count { !it.isRead }))
            }
        }

    suspend fun markNotificationRead(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.markNotificationRead(id)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to mark as read"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun markAllNotificationsRead(userEmail: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.markAllNotificationsRead(mapOf("userEmail" to userEmail, "role" to "customer"))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to mark all as read"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

