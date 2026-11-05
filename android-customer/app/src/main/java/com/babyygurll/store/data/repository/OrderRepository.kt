package com.babyygurll.store.data.repository

import android.content.Context
import com.babyygurll.store.data.api.ApiClient
import com.babyygurll.store.data.model.Order
import com.babyygurll.store.data.model.OrderCreateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(private val context: Context) {

    private val api get() = ApiClient.getService(context)

    suspend fun createOrder(req: OrderCreateRequest): Result<Order> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createOrder(req)
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to place order"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCustomerOrders(email: String): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getCustomerOrders(email.trim().lowercase())
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to load orders"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getOrderById(orderId: String): Result<Order> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getOrderById(orderId)
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Order not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createRazorpayOrder(amountInRupees: Double): Result<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            try {
                val amountInPaise = (amountInRupees * 100).toLong().coerceAtLeast(100L)
                val response = api.createRazorpayOrder(mapOf(
                    "amount" to amountInPaise,
                    "currency" to "INR",
                    "receipt" to "rcpt_${System.currentTimeMillis()}"
                ))
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to initialize payment order"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getRazorpayKey(): String = withContext(Dispatchers.IO) {
        try {
            val response = api.getRazorpayKey()
            if (response.isSuccessful) {
                val key = response.body()?.get("key_id")?.toString()
                    ?: response.body()?.get("key")?.toString()
                if (!key.isNullOrBlank()) return@withContext key
            }
        } catch (_: Exception) {}
        "rzp_live_TmiLSHkOVIFPyZ"
    }
}

