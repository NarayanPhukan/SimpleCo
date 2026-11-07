package com.babyygurll.admin.data.repository

import com.babyygurll.admin.data.api.NetworkModule
import com.babyygurll.admin.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminRepository(
    private val prefs: PreferencesManager
) {
    private val api get() = NetworkModule.getApiService()

    suspend fun verifyPassword(password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyAdmin(VerifyAdminRequest(password))
            if (response.isSuccessful && response.body()?.success == true) {
                prefs.isLoggedIn = true
                prefs.savedAdminPin = password
                Result.success(true)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Invalid administrator credentials"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Cannot reach server at ${prefs.serverUrl}. ${e.localizedMessage ?: "Check connection."}"))
        }
    }

    suspend fun fetchSettings(): Result<StoreSettings> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSettings()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to fetch settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSettings(settings: StoreSettings): Result<StoreSettings> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateSettings(settings)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to update settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProducts(category: String? = null, search: String? = null): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts(category = category, search = search)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to load products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(req: ProductCreateRequest): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val response = api.createProduct(req)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to create product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: String, fields: Map<String, Any?>): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProduct(id, fields)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = try {
                    response.errorBody()?.string()?.let { errJson ->
                        val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        regex.find(errJson)?.groupValues?.get(1) ?: errJson
                    }
                } catch (e: Exception) { null }
                Result.failure(Exception(errorMsg ?: body?.message ?: "Failed to update product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProduct(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = try {
                    response.errorBody()?.string()?.let { errJson ->
                        val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        regex.find(errJson)?.groupValues?.get(1) ?: errJson
                    }
                } catch (e: Exception) { null }
                Result.failure(Exception(errorMsg ?: "Failed to delete product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllOrders()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to load orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(req: ManualOrderCreateRequest): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = api.createOrder(req)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to create manual order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun advanceOrderStage(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = api.advanceOrderStage(orderId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to advance stage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderTracking(orderId: String, carrier: String, trackingNumber: String, estimatedDelivery: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val req = OrderTrackingUpdateRequest(carrier, trackingNumber, estimatedDelivery)
            val response = api.updateOrderTracking(orderId, req)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to update tracking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCoupons(): Result<List<Coupon>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCoupons()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to load coupons"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCoupon(req: CouponCreateRequest): Result<Coupon> = withContext(Dispatchers.IO) {
        try {
            val response = api.createCoupon(req)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to create coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCoupon(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteCoupon(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkPincode(pincode: String): Result<PincodeCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkPincode(PincodeCheckRequest(pincode))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(body?.message ?: "Serviceability check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Auto Update Detection
    suspend fun fetchAppVersion(): Result<AppVersionInfo> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAppVersion()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                return@withContext Result.success(body.data)
            }
        } catch (e: Exception) {
            // Render cold start fallback
        }

        try {
            val rawUrl = "https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/client/public/android-admin-version.json"
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

        Result.failure(Exception("Failed to check for updates"))
    }

    // Diagnostics & System Health
    suspend fun fetchDiagnostics(): Result<DiagnosticsData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDiagnostics()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Diagnostics check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearServerErrorLogs(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.clearErrorLogs()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to clear error logs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Razorpay Refunds
    suspend fun fetchRefunds(): Result<List<RefundRecord>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRefunds()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.refunds)
            } else {
                Result.failure(Exception("Failed to fetch refunds from server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun issueRefund(orderId: String, amount: Double? = null, reason: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.issueRefund(IssueRefundRequest(orderId = orderId, amount = amount, reason = reason))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to issue refund"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val defaultAdminNotifications = listOf(
        AdminNotification(
            id = "notif_admin_sys",
            recipient = "admin",
            role = "admin",
            title = "Studio System Operational ⚡",
            message = "Boutique engine connected to Render Cloud & MongoDB Atlas. Auto-updates active.",
            type = "system",
            isRead = false,
            createdAt = java.time.Instant.now().toString()
        ),
        AdminNotification(
            id = "notif_admin_dept",
            recipient = "admin",
            role = "admin",
            title = "8 Boutique Departments Active 🧶",
            message = "Hair Accessories, Bags & Purses, Flowers, Plushies, and Apparel are live for shoppers.",
            type = "general",
            isRead = false,
            createdAt = java.time.Instant.now().minusSeconds(1800).toString()
        ),
        AdminNotification(
            id = "notif_admin_ready",
            recipient = "admin",
            role = "admin",
            title = "Dispatch & Payments Online 📦",
            message = "Razorpay live merchant gateway connected. Ready for real-time customer orders.",
            type = "order",
            isRead = false,
            createdAt = java.time.Instant.now().minusSeconds(3600).toString()
        )
    )

    // Notification Center & Broadcast
    suspend fun fetchNotifications(): Result<Pair<List<AdminNotification>, Int>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getNotifications(role = "admin")
            val body = response.body()
            if (response.isSuccessful && body != null && body.data.isNotEmpty()) {
                Result.success(Pair(body.data, body.unreadCount))
            } else {
                Result.success(Pair(defaultAdminNotifications, defaultAdminNotifications.count { !it.isRead }))
            }
        } catch (e: Exception) {
            Result.success(Pair(defaultAdminNotifications, defaultAdminNotifications.count { !it.isRead }))
        }
    }

    suspend fun markNotificationRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.markNotificationRead(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to mark as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.markAllNotificationsRead(mapOf("role" to "admin"))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to mark all as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteNotification(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete notification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendBroadcastNotification(title: String, message: String, type: String): Result<AdminNotification> = withContext(Dispatchers.IO) {
        try {
            val req = CreateNotificationRequest(
                title = title,
                message = message,
                type = type,
                role = "customer",
                recipient = "all"
            )
            val response = api.createNotification(req)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to send broadcast"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

