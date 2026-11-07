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

data class ProductVariant(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("size") val size: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("colorHex") val colorHex: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("originalPrice") val originalPrice: Double? = null,
    @SerializedName("stock") val stock: Int = 5,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("image") val image: String? = null
)

data class Product(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val id: String = "",
    @SerializedName("sku") val rawSku: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("category") val category: String = "plushies",
    @SerializedName("categoryName") val categoryName: String = "Amigurumi Plushies",
    @SerializedName("subcategory") val subcategory: String? = null,
    @SerializedName("subcategoryName") val subcategoryName: String? = null,
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
    @SerializedName("colors") val colors: List<ProductColor> = emptyList(),
    @SerializedName("sizes") val sizes: List<String> = emptyList(),
    @SerializedName("variants") val variants: List<ProductVariant> = emptyList(),
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("stock") val stock: Int? = null
) {
    val resolvedPrimaryImage: String
        get() {
            if (image.startsWith("/assets/images/") && images.any { !it.startsWith("/assets/images/") }) {
                return images.firstOrNull { !it.startsWith("/assets/images/") } ?: image
            }
            return image.ifBlank { images.firstOrNull() ?: "" }
        }

    val allImages: List<String>
        get() {
            val list = if (images.isNotEmpty()) {
                val real = images.filter { !it.startsWith("/assets/images/") }
                if (real.isNotEmpty()) real else images
            } else if (image.isNotBlank()) {
                listOf(image)
            } else emptyList()
            return list
        }

    val sku: String
        get() {
            if (!rawSku.isNullOrBlank()) return rawSku
            val effective = (if (id.isNotBlank()) id else mongoId.orEmpty())
            val clean = effective.filter { it.isLetterOrDigit() }.takeLast(6).uppercase()
            return if (clean.isNotBlank()) "BGC-$clean" else "BGC-ITEM"
        }

    val stockCount: Int
        get() {
            if (!inStock) return 0
            if (stock != null) return stock
            if (variants.isNotEmpty()) {
                val variantTotal = variants.sumOf { it.stock }
                if (variantTotal > 0) return variantTotal
            }
            return 10
        }

    val unitsSold: Int
        get() = 0

    val productionDays: String
        get() = if (leadTime.contains("days", true)) leadTime.substringBefore("(") else "3–5 days"

    val displayStatus: String
        get() = when {
            !inStock || stockCount <= 0 -> "Out of stock"
            stockCount in 1..3 -> "Low stock"
            badge?.contains("Draft", true) == true -> "Draft"
            else -> "Published"
        }

    val discountPercentage: Int?
        get() {
            val orig = originalPrice
            if (orig != null && orig > price) {
                return (((orig - price) / orig) * 100).toInt()
            }
            return null
        }

    val displayOriginalPrice: Double
        get() = originalPrice ?: price

    val effectiveVariants: List<ProductVariant>
        get() {
            if (variants.isNotEmpty()) return variants
            if (colors.isNotEmpty()) {
                return colors.mapIndexed { idx, c ->
                    ProductVariant(
                        id = "col-$idx",
                        name = c.name,
                        color = c.name,
                        colorHex = c.hex,
                        price = price,
                        originalPrice = originalPrice,
                        stock = stockCount
                    )
                }
            }
            return listOf(
                ProductVariant(
                    id = "std-1",
                    name = "Standard edition",
                    price = price,
                    originalPrice = originalPrice,
                    stock = stockCount
                )
            )
        }
}

data class ProductCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String,
    @SerializedName("categoryName") val categoryName: String? = null,
    @SerializedName("subcategory") val subcategory: String? = null,
    @SerializedName("subcategoryName") val subcategoryName: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("originalPrice") val originalPrice: Double? = null,
    @SerializedName("image") val image: String,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("badge") val badge: String? = null,
    @SerializedName("leadTime") val leadTime: String? = null,
    @SerializedName("description") val description: String,
    @SerializedName("colors") val colors: List<ProductColor> = emptyList(),
    @SerializedName("sizes") val sizes: List<String> = emptyList(),
    @SerializedName("variants") val variants: List<ProductVariant> = emptyList(),
    @SerializedName("tags") val tags: List<String> = emptyList(),
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
    @SerializedName("productionChecklist") val productionChecklist: Map<String, Boolean> = emptyMap(),
    @SerializedName("razorpayRefundId") val razorpayRefundId: String? = null,
    @SerializedName("refundStatus") val refundStatus: String? = null,
    @SerializedName("refundAmount") val refundAmount: Double = 0.0,
    @SerializedName("cancelReason") val cancelReason: String? = null,
    @SerializedName("cancelledAt") val cancelledAt: String? = null
) {
    val displayDate: String
        get() {
            if (!createdAt.isNullOrBlank()) {
                try {
                    val sdfUtc = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                    val cleanCreated = createdAt.substringBefore(".")
                    val parsed = sdfUtc.parse(cleanCreated)
                    if (parsed != null) {
                        val sdfOut = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.ENGLISH)
                        return sdfOut.format(parsed)
                    }
                } catch (_: Exception) {}
            }
            return dateFormatted ?: "Recently"
        }
}

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
    val todayRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val todayOrdersCount: Int = 0,
    val pendingFulfillmentCount: Int = 0,
    val totalCustomersCount: Int = 0,
    val returningCustomersPercentage: Int = 0,
    val averageCustomerSpend: Double = 0.0,
    val activeProductsCount: Int = 0,
    val averageOrderValue: Double = 0.0,
    val revenueGrowthPercent: Double = 0.0,
    val periodRevenue: Double = 0.0,
    val chartDays: List<Pair<String, Double>> = emptyList()
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
) {
    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            return when {
                parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
                parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
                else -> "CU"
            }
        }

    val customerTag: String
        get() = when {
            totalSpent >= 12000 || ordersCount >= 5 -> "High-value"
            ordersCount > 1 -> "Returning"
            else -> "New"
        }

    val displayLastOrder: String
        get() = if (lastOrderDate.isNotBlank() && lastOrderDate != "Recently") lastOrderDate else "Recently"
}



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

// Operations Department Models
data class ProductionQueueItem(
    val id: String,
    val orderRef: String,
    val itemName: String,
    val dueDate: String,
    val stage: String, // "Being crocheted", "Quality check", "Waiting"
    val artisan: String = "Master Crafter",
    val progressPercent: Int = 50
)

data class CustomRequestItem(
    val id: String,
    val title: String,
    val customerName: String,
    val customerPhone: String = "+91 98201 12345",
    val customerEmail: String = "client@example.com",
    val status: String, // "New", "Reviewing", "Quoted"
    val budget: String = "₹2,500 - ₹3,500",
    val notes: String = "Bespoke color palette and personalized crochet monogram.",
    val requestedDate: String = "Recently"
)

data class ShipmentItem(
    val id: String,
    val orderRef: String,
    val destinationCity: String,
    val carrierDetail: String, // "Shiprocket · 0.8 kg", "AWB 1439826402", "Delivered 18 Jun"
    val status: String, // "Ready to ship", "In transit", "Delivered"
    val recipientName: String = "Valued Customer",
    val trackingUrl: String? = null
)

// Razorpay Refunds & Payments Models (matching Razorpay Dashboard design)
data class RefundRecord(
    @SerializedName("id") val id: String = "",
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("paymentId") val paymentId: String = "",
    @SerializedName("amount") val amountPaise: Long = 0L,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("status") val status: String = "processed", // "processed", "pending", "failed"
    @SerializedName("speed_processed") val speed: String = "optimum",
    @SerializedName("created_at") val createdAtTimestamp: Long = 0L,
    @SerializedName("customerName") val customerName: String = "",
    @SerializedName("customerEmail") val customerEmail: String = "",
    @SerializedName("notes") val notes: Map<String, String>? = null
) {
    val amountRupees: Double get() = if (amountPaise > 0) amountPaise / 100.0 else 0.0
    val displayStatus: String
        get() = when (status.lowercase()) {
            "processed", "refunded" -> "Processed"
            "pending", "refund_pending", "manual_review" -> "Processing"
            else -> if (status.startsWith("failed")) "Failed" else status.replaceFirstChar { it.uppercase() }
        }
}

data class RefundsResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("refunds") val refunds: List<RefundRecord> = emptyList()
)

data class IssueRefundRequest(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("reason") val reason: String? = null
)

// Notification Models
data class AdminNotification(
    @SerializedName("_id") val id: String = "",
    @SerializedName("recipient") val recipient: String = "admin",
    @SerializedName("role") val role: String = "admin",
    @SerializedName("title") val title: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("type") val type: String = "general", // order, shipping, stock, custom_request, broadcast, system
    @SerializedName("data") val data: Map<String, Any?> = emptyMap(),
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    val orderId: String?
        get() = (data["orderId"] as? String) ?: (data["orderNumber"] as? String)

    val relativeTime: String
        get() {
            if (createdAt.isNullOrBlank()) return "Recent"
            return try {
                val clean = createdAt.replace("Z", "+00:00")
                val parsed = java.time.Instant.parse(if (clean.contains("+") || clean.endsWith("Z")) clean else "${clean}Z")
                val now = java.time.Instant.now()
                val diffSeconds = java.time.Duration.between(parsed, now).seconds
                when {
                    diffSeconds < 60 -> "Just now"
                    diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
                    diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
                    diffSeconds < 172800 -> "Yesterday"
                    else -> "${diffSeconds / 86400}d ago"
                }
            } catch (e: Exception) {
                "Recent"
            }
        }
}

data class NotificationsResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("unreadCount") val unreadCount: Int = 0,
    @SerializedName("data") val data: List<AdminNotification> = emptyList()
)

data class CreateNotificationRequest(
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String = "broadcast",
    @SerializedName("role") val role: String = "customer",
    @SerializedName("recipient") val recipient: String = "all",
    @SerializedName("data") val data: Map<String, Any?> = emptyMap()
)


