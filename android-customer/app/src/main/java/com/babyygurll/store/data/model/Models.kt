package com.babyygurll.store.data.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("leadTime") val leadTime: String? = "Ready to ship (1-2 days)",
    @SerializedName("description") val description: String = "",
    @SerializedName("inStock") val inStock: Boolean = true
) {
    val effectiveId: String
        get() = id.ifBlank { mongoId ?: "" }

    val allImages: List<String>
        get() = if (images.isNotEmpty()) images else if (image.isNotBlank()) listOf(image) else emptyList()
}

data class StoreSetting(
    @SerializedName("storeName") val storeName: String = "BabyyGurll Crochet Studio",
    @SerializedName("tagline") val tagline: String = "Bespoke Handmade Crochet Creations",
    @SerializedName("announcementText") val announcementText: String = "Free delivery across India on all orders • 100% milk cotton yarn",
    @SerializedName("announcementBadge") val announcementBadge: String = "Code: BABYYGURLL for 15% OFF",
    @SerializedName("announcementActive") val announcementActive: Boolean = true,
    @SerializedName("freeShippingThreshold") val freeShippingThreshold: Double = 0.0,
    @SerializedName("standardShippingFee") val standardShippingFee: Double = 0.0,
    @SerializedName("expressShippingFee") val expressShippingFee: Double = 0.0,
    @SerializedName("supportEmail") val supportEmail: String = "hello@babyygurll.in",
    @SerializedName("supportPhone") val supportPhone: String = "+91 98200 12345"
)

data class Coupon(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("code") val code: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("discountPercent") val discountPercent: Double? = null,
    @SerializedName("discountFlat") val discountFlat: Double? = null,
    @SerializedName("minOrderValue") val minOrderValue: Double? = null,
    @SerializedName("freeShipping") val freeShipping: Boolean = false,
    @SerializedName("active") val active: Boolean = true
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val customNote: String = ""
)

data class ShippingAddress(
    @SerializedName("recipientName") val recipientName: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("state") val state: String = "Maharashtra",
    @SerializedName("zip") val zip: String = "",
    @SerializedName("country") val country: String = "India"
)

data class OrderTotals(
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("shipping") val shipping: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0
)

data class OrderItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("image") val image: String = "",
    @SerializedName("customNote") val customNote: String = ""
)

data class TimelineEvent(
    @SerializedName("title") val title: String = "",
    @SerializedName("detail") val detail: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("completed") val completed: Boolean = false
)

data class Order(
    @SerializedName("orderId") val orderId: String = "",
    @SerializedName("userEmail") val userEmail: String = "",
    @SerializedName("dateFormatted") val dateFormatted: String = "",
    @SerializedName("statusStage") val statusStage: Int = 0,
    @SerializedName("statusLabel") val statusLabel: String = "Order Placed & Yarn Selected",
    @SerializedName("statusCode") val statusCode: String = "received",
    @SerializedName("carrier") val carrier: String = "Express Delivery",
    @SerializedName("trackingNumber") val trackingNumber: String = "",
    @SerializedName("estimatedDelivery") val estimatedDelivery: String = "",
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddress? = null,
    @SerializedName("paymentMethod") val paymentMethod: String = "Razorpay Online",
    @SerializedName("paymentStatus") val paymentStatus: String = "paid",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("shippingCost") val shippingCost: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("timeline") val timeline: List<TimelineEvent> = emptyList(),
    @SerializedName("shiprocketOrderId") val shiprocketOrderId: String? = null,
    @SerializedName("shiprocketShipmentId") val shiprocketShipmentId: String? = null,
    @SerializedName("shiprocketStatus") val shiprocketStatus: String? = null
)

data class OrderCreateRequest(
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddress,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("razorpayPaymentId") val razorpayPaymentId: String = "",
    @SerializedName("razorpayOrderId") val razorpayOrderId: String = "",
    @SerializedName("totals") val totals: OrderTotals,
    @SerializedName("items") val items: List<OrderItem>
)

data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String? = null
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("message") val message: String? = null
)

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("count") val count: Int? = null
)

