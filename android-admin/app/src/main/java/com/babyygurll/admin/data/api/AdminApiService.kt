package com.babyygurll.admin.data.api

import com.babyygurll.admin.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {

    // Admin Auth & Settings
    @POST("api/settings/verify-admin")
    suspend fun verifyAdmin(
        @Body request: VerifyAdminRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/settings")
    suspend fun getSettings(): Response<ApiResponse<StoreSettings>>

    @PUT("api/settings")
    suspend fun updateSettings(
        @Body settings: StoreSettings
    ): Response<ApiResponse<StoreSettings>>

    // Products Management
    @GET("api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): Response<ApiResponse<List<Product>>>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: String
    ): Response<ApiResponse<Product>>

    @POST("api/products")
    suspend fun createProduct(
        @Body product: ProductCreateRequest
    ): Response<ApiResponse<Product>>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body product: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<Product>>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    // Orders Management
    @GET("api/orders")
    suspend fun getAllOrders(
        @Query("all") all: String = "true"
    ): Response<ApiResponse<List<Order>>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): Response<ApiResponse<Order>>

    @POST("api/orders/{id}/advance")
    suspend fun advanceOrderStage(
        @Path("id") id: String
    ): Response<ApiResponse<Order>>

    @POST("api/orders")
    suspend fun createOrder(
        @Body request: ManualOrderCreateRequest
    ): Response<ApiResponse<Order>>

    @PUT("api/orders/{id}/tracking")
    suspend fun updateOrderTracking(
        @Path("id") id: String,
        @Body request: OrderTrackingUpdateRequest
    ): Response<ApiResponse<Order>>

    // Coupons Management
    @GET("api/coupons")
    suspend fun getCoupons(): Response<ApiResponse<List<Coupon>>>

    @POST("api/coupons")
    suspend fun createCoupon(
        @Body request: CouponCreateRequest
    ): Response<ApiResponse<Coupon>>

    @DELETE("api/coupons/{id}")
    suspend fun deleteCoupon(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @POST("api/coupons/reset-defaults")
    suspend fun resetDefaultCoupons(): Response<ApiResponse<List<Coupon>>>

    // Shiprocket Pincode Check
    @POST("api/shipping/serviceability")
    suspend fun checkPincode(
        @Body request: PincodeCheckRequest
    ): Response<PincodeCheckResponse>

    // Auto Update Detection
    @GET("api/settings/app-version?app=admin")
    suspend fun getAppVersion(): Response<ApiResponse<AppVersionInfo>>

    // Error Detection & System Diagnostics
    @GET("api/settings/diagnostics")
    suspend fun getDiagnostics(): Response<ApiResponse<DiagnosticsData>>

    @POST("api/settings/report-error")
    suspend fun reportError(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<ServerErrorLog>>

    @POST("api/settings/clear-error-logs")
    suspend fun clearErrorLogs(): Response<ApiResponse<Unit>>

    // Payment & Razorpay Refunds
    @GET("api/payment/refunds")
    suspend fun getRefunds(): Response<RefundsResponse>

    @POST("api/payment/refund")
    suspend fun issueRefund(
        @Body request: IssueRefundRequest
    ): Response<ApiResponse<Unit>>
}

