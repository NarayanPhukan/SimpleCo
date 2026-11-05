package com.babyygurll.store.data.repository

import android.content.Context
import com.babyygurll.store.data.api.ApiClient
import com.babyygurll.store.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository(private val context: Context) {

    private val api get() = ApiClient.getService(context)

    suspend fun getProducts(category: String? = null, search: String? = null, sort: String? = null): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            try {
                val catQuery = if (category.isNullOrBlank() || category == "all") null else category
                val searchQuery = search?.trim()?.ifBlank { null }
                val response = api.getProducts(catQuery, searchQuery, sort)
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to load products"))
                }
            } catch (e: Exception) {
                Result.failure(e)
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
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception("Failed to get customer app version"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
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
}

