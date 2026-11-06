package com.babyygurll.store.data.api

import com.babyygurll.store.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface StoreApiService {

    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): Response<ApiResponse<List<Product>>>

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: String
    ): Response<ApiResponse<Product>>

    @GET("settings")
    suspend fun getStoreSettings(): Response<ApiResponse<StoreSetting>>

    @GET("settings/app-version?app=customer")
    suspend fun getCustomerAppVersion(): Response<ApiResponse<AppVersionInfo>>

    @GET("coupons")
    suspend fun getCoupons(): Response<ApiResponse<List<Coupon>>>

    @POST("coupons/validate")
    suspend fun validateCoupon(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<Coupon>>

    @POST("orders")
    suspend fun createOrder(
        @Body req: OrderCreateRequest
    ): Response<ApiResponse<Order>>

    @GET("orders")
    suspend fun getCustomerOrders(
        @Query("email") email: String
    ): Response<ApiResponse<List<Order>>>

    @GET("orders/{orderId}")
    suspend fun getOrderById(
        @Path("orderId") orderId: String
    ): Response<ApiResponse<Order>>

    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<ApiResponse<Order>>

    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(
        @Query("email") email: String
    ): Response<AuthResponse>

    @GET("shipping/serviceability")
    suspend fun checkPincode(
        @Query("delivery_postcode") deliveryPostcode: String
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("payment/create-order")
    suspend fun createRazorpayOrder(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, Any>>

    @GET("payment/razorpay-key")
    suspend fun getRazorpayKey(): Response<Map<String, Any>>

    // Customer Notifications
    @GET("notifications")
    suspend fun getNotifications(
        @Query("userEmail") userEmail: String,
        @Query("role") role: String = "customer"
    ): Response<CustomerNotificationsResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>
}

