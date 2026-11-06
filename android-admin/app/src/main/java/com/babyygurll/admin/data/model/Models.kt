package com.babyygurll.admin.data.model

import com.google.gson.annotations.SerializedName

// Generic API response wrappers
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("data") val data: T? = null
)

// Product Models
data class ProductColor(
    @SerializedName("name") val name: String,
    @SerializedName("hex") val hex: String
)

data class Product(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("category") val category: String = "plushies",
    @SerializedName("categoryName") val categoryName: String = "Amigurumi Plushies",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("originalPrice") val originalPrice: Double? = null,
    @SerializedName("image") val image: String = "",
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("badge") val badge: String? = null,
    @SerializedName("rating") val rating: Double = 5.0,
    @SerializedName("reviewsCount") val reviewsCount: Int = 0,
    @SerializedName("leadTime") val leadTime: String = "Ready to ship (1-2 days)",
    @SerializedName("description") val description: String = "",
    @SerializedName("inStock") val inStock: Boolean = true,
    @SerializedName("colors") val colors: List<ProductColor> = emptyList()
)

data class ProductCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("categoryName") val categoryName: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("originalPrice") val originalPrice: Double? = null,
    @SerializedName("image") val image: String,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("badge") val badge: String? = null,
    @SerializedName("leadTime") val leadTime: String? = null,
    @SerializedName("description") val description: String,
    @SerializedName("inStock") val inStock: Boolean = true
)

// Order Models
data class OrderItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("productId") val productId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("color") val color: String? = null,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("image") val image: String? = null,
    @SerializedName("customNote") val customNote: String? = null
)

data class ShippingAddress(
    @SerializedName("recipientName") val recipientName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("zip") val zip: String? = null,
    @SerializedName("country") val country: String? = "India"
)

data class TimelineEvent(
    @SerializedName("stage") val stage: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("completed") val completed: Boolean = false,
    @SerializedName("current") val current: Boolean = false,
    @SerializedName("desc") val desc: String = ""
)

data class Order(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("userEmail") val userEmail: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("dateFormatted") val dateFormatted: String? = null,
    @SerializedName("statusStage") val statusStage: Int = 0,
    @SerializedName("statusLabel") val statusLabel: String = "Order Received",
    @SerializedName("statusCode") val statusCode: String = "received",
    @SerializedName("carrier") val carrier: String? = "Express Delivery",
    @SerializedName("trackingNumber") val trackingNumber: String? = null,
    @SerializedName("estimatedDelivery") val estimatedDelivery: String? = null,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddress? = null,
    @SerializedName("paymentMethod") val paymentMethod: String? = "Razorpay Online",
    @SerializedName("paymentStatus") val paymentStatus: String? = "paid",
    @SerializedName("razorpayPaymentId") val razorpayPaymentId: String? = null,
    @SerializedName("razorpayOrderId") val razorpayOrderId: String? = null,
    @SerializedName("shiprocketOrderId") val shiprocketOrderId: String? = null,
    @SerializedName("shiprocketShipmentId") val shiprocketShipmentId: String? = null,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("shippingCost") val shippingCost: Double = 0.0,
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("timeline") val timeline: List<TimelineEvent> = emptyList(),
    @SerializedName("customerInstructions") val customerInstructions: String? = null,
    @SerializedName("internalNotes") val internalNotes: List<String> = emptyList(),
    @SerializedName("productionChecklist") val productionChecklist: Map<String, Boolean> = emptyMap()
)

data class OrderTrackingUpdateRequest(
    @SerializedName("carrier") val carrier: String,
    @SerializedName("trackingNumber") val trackingNumber: String,
    @SerializedName("estimatedDelivery") val estimatedDelivery: String
)

data class ManualOrderTotals(
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("shipping") val shipping: Double = 80.0,
    @SerializedName("tax") val tax: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0
)

data class ManualOrderCreateRequest(
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddress,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("items") val items: List<OrderItem>,
    @SerializedName("totals") val totals: ManualOrderTotals,
    @SerializedName("isManualOrder") val isManualOrder: Boolean = true,
    @SerializedName("source") val source: String = "WhatsApp",
    @SerializedName("customerInstructions") val customerInstructions: String? = null
)

// Coupon Models
data class Coupon(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("code") val code: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("discountType") val discountType: String = "percentage", // percentage, flat, free_shipping
    @SerializedName("discountValue") val discountValue: Double = 0.0,
    @SerializedName("minOrderValue") val minOrderValue: Double = 0.0,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("isPublic") val isPublic: Boolean = true,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("usageCount") val usageCount: Int = 0
)

data class CouponCreateRequest(
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String,
    @SerializedName("discountType") val discountType: String,
    @SerializedName("discountValue") val discountValue: Double,
    @SerializedName("minOrderValue") val minOrderValue: Double = 0.0,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("isPublic") val isPublic: Boolean = true
)

// Store Settings Models
data class StoreSettings(
    @SerializedName("key") val key: String = "main_store_config",
    @SerializedName("storeName") val storeName: String = "BabyyGurll Crochet Studio",
    @SerializedName("tagline") val tagline: String = "Bespoke Handmade Crochet Creations",
    @SerializedName("announcementText") val announcementText: String = "",
    @SerializedName("announcementBadge") val announcementBadge: String = "",
    @SerializedName("announcementActive") val announcementActive: Boolean = true,
    @SerializedName("freeShippingThreshold") val freeShippingThreshold: Double = 999.0,
    @SerializedName("standardShippingFee") val standardShippingFee: Double = 79.0,
    @SerializedName("expressShippingFee") val expressShippingFee: Double = 149.0,
    @SerializedName("supportEmail") val supportEmail: String = "hello@babyygurll.in",
    @SerializedName("supportPhone") val supportPhone: String = "",
    @SerializedName("studioAddress") val studioAddress: String = "",
    @SerializedName("adminRazorpayAccountId") val adminRazorpayAccountId: String = ""
)

data class VerifyAdminRequest(
    @SerializedName("password") val password: String
)

data class PincodeCheckRequest(
    @SerializedName("deliveryPostcode") val deliveryPostcode: String
)

data class PincodeCheckResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("connected") val connected: Boolean? = null,
    @SerializedName("serviceable") val serviceable: Boolean? = null,
    @SerializedName("deliveryPostcode") val deliveryPostcode: String? = null,
    @SerializedName("message") val message: String? = null
)

// Computed Dashboard Metrics
data class DashboardMetrics(
    val totalRevenue: Double = 0.0,
    val todayRevenue: Double = 12450.0,
    val totalOrders: Int = 18,
    val todayOrdersCount: Int = 4,
    val pendingFulfillmentCount: Int = 5,
    val totalCustomersCount: Int = 1284,
    val activeProductsCount: Int = 0,
    val averageOrderValue: Double = 0.0,
    val revenueGrowthPercent: Double = 14.2,
    val periodRevenue: Double = 86240.0,
    val chartDays: List<Pair<String, Double>> = listOf(
        "Mon" to 12000.0,
        "Tue" to 14500.0,
        "Wed" to 11800.0,
        "Thu" to 15400.0,
        "Fri" to 16820.0,
        "Sat" to 14200.0,
        "Sun" to 15800.0
    )
)

data class Customer(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val address: String = "",
    val ordersCount: Int = 0,
    val totalSpent: Double = 0.0,
    val lastOrderDate: String = ""
)



// Auto Update Models
data class AppVersionInfo(
    @SerializedName("latestVersionCode") val latestVersionCode: Int = 1,
    @SerializedName("latestVersionName") val latestVersionName: String = "1.0.0",
    @SerializedName("minRequiredVersionCode") val minRequiredVersionCode: Int = 1,
    @SerializedName("releaseNotes") val releaseNotes: String = "",
    @SerializedName("downloadUrl") val downloadUrl: String = "",
    @SerializedName("releaseDate") val releaseDate: String = "",
    @SerializedName("critical") val critical: Boolean = false
)

data class AppUpdateCheckResult(
    val isUpdateAvailable: Boolean = false,
    val isCritical: Boolean = false,
    val currentVersionCode: Int = 1,
    val currentVersionName: String = "1.0.0",
    val updateInfo: AppVersionInfo? = null
)

// Diagnostics & Error Detection Models
data class ServerErrorLog(
    @SerializedName("id") val id: String = "",
    @SerializedName("timestamp") val timestamp: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("details") val details: String? = null
)

data class DatabaseDiagnostics(
    @SerializedName("status") val status: String = "unknown",
    @SerializedName("readyState") val readyState: Int = 0,
    @SerializedName("latencyMs") val latencyMs: Long? = null,
    @SerializedName("host") val host: String = ""
)

data class ServiceInfo(
    @SerializedName("configured") val configured: Boolean = false,
    @SerializedName("mode") val mode: String? = null,
    @SerializedName("account") val account: String? = null,
    @SerializedName("projectId") val projectId: String? = null
)

data class ServicesDiagnostics(
    @SerializedName("razorpay") val razorpay: ServiceInfo? = null,
    @SerializedName("shiprocket") val shiprocket: ServiceInfo? = null,
    @SerializedName("firebase") val firebase: ServiceInfo? = null
)

data class EnvironmentDiagnostics(
    @SerializedName("nodeVersion") val nodeVersion: String = "",
    @SerializedName("platform") val platform: String = "",
    @SerializedName("memoryUsageMb") val memoryUsageMb: Int = 0,
    @SerializedName("totalHeapMb") val totalHeapMb: Int = 0
)

data class DiagnosticsData(
    @SerializedName("timestamp") val timestamp: String = "",
    @SerializedName("uptimeSeconds") val uptimeSeconds: Long = 0,
    @SerializedName("serverStatus") val serverStatus: String = "unknown",
    @SerializedName("database") val database: DatabaseDiagnostics = DatabaseDiagnostics(),
    @SerializedName("services") val services: ServicesDiagnostics = ServicesDiagnostics(),
    @SerializedName("environment") val environment: EnvironmentDiagnostics = EnvironmentDiagnostics(),
    @SerializedName("recentErrors") val recentErrors: List<ServerErrorLog> = emptyList()
)

data class ClientErrorLog(
    val id: String,
    val timestamp: String,
    val severity: String = "ERROR", // ERROR, WARNING, INFO
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)
