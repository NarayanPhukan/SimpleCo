package com.babyygurll.store.data.model

import com.babyygurll.store.util.ImageUrlUtils
import com.google.gson.annotations.SerializedName

data class ProductColor(
    @SerializedName("name") val name: String = "",
    @SerializedName("hex") val hex: String = ""
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
    @SerializedName("colors") val colors: List<ProductColor> = emptyList(),
    @SerializedName("sizes") val sizes: List<String> = emptyList(),
    @SerializedName("variants") val variants: List<ProductVariant> = emptyList(),
    @SerializedName("inStock") val inStock: Boolean = true
) {
    val effectiveId: String
        get() = id.ifBlank { mongoId ?: "" }

    val resolvedPrimaryImage: String
        get() {
            val candidate = if (image.startsWith("/assets/images/") && images.any { !it.startsWith("/assets/images/") }) {
                images.firstOrNull { !it.startsWith("/assets/images/") } ?: image
            } else {
                image.ifBlank { images.firstOrNull() ?: "" }
            }
            return ImageUrlUtils.resolveImageUrl(candidate)
        }

    val allImages: List<String>
        get() {
            val list = if (images.isNotEmpty()) {
                val real = images.filter { !it.startsWith("/assets/images/") }
                if (real.isNotEmpty()) real else images
            } else if (image.isNotBlank()) {
                listOf(image)
            } else emptyList()
            return list.map { ImageUrlUtils.resolveImageUrl(it) }
        }

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
                        stock = if (inStock) 5 else 0
                    )
                }
            }
            return listOf(
                ProductVariant(
                    id = "std-1",
                    name = "Standard Edition",
                    price = price,
                    originalPrice = originalPrice,
                    stock = if (inStock) 5 else 0
                )
            )
        }
}

data class AppVersionInfo(
    @SerializedName("latestVersionCode") val latestVersionCode: Int = 3,
    @SerializedName("latestVersionName") val latestVersionName: String = "1.0.2",
    @SerializedName("minRequiredVersionCode") val minRequiredVersionCode: Int = 1,
    @SerializedName("releaseNotes") val releaseNotes: String = "• Fixed account registration and login authentication\n• Fixed product images loading across boutique\n• Live Shiprocket tracking and Razorpay secure checkout",
    @SerializedName("downloadUrl") val downloadUrl: String = "https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk",
    @SerializedName("critical") val critical: Boolean = false
)

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
    @SerializedName("supportPhone") val supportPhone: String = "+91 98200 12345",
    @SerializedName("customerAppVersion") val customerAppVersion: AppVersionInfo? = null,
    @SerializedName("appVersion") val appVersion: AppVersionInfo? = null
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
    val customNote: String = "",
    val selectedVariant: ProductVariant? = null,
    val selectedColor: String? = null
) {
    val unitPrice: Double
        get() = selectedVariant?.price ?: product.price

    val cartKey: String
        get() = "${product.effectiveId}_${selectedVariant?.id ?: selectedVariant?.name ?: selectedColor ?: "default"}"
}

data class ShippingAddress(
    @SerializedName("recipientName") val recipientName: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("state") val state: String = "Maharashtra",
    @SerializedName("zip") val zip: String = "",
    @SerializedName("country") val country: String = "India"
) {
    val name: String get() = recipientName
    val addressLine: String get() = street
    val pincode: String get() = zip

    companion object {
        fun from(
            name: String = "",
            phone: String = "",
            addressLine: String = "",
            city: String = "",
            state: String = "Maharashtra",
            pincode: String = "",
            country: String = "India"
        ) = ShippingAddress(
            recipientName = name,
            phone = phone,
            street = addressLine,
            city = city,
            state = state,
            zip = pincode,
            country = country
        )
    }
}

data class OrderTotals(
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("shipping") val shipping: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0
)

data class OrderItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("color") val color: String = "Default",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("image") val image: String = "",
    @SerializedName("customNote") val customNote: String = ""
) {
    val resolvedImage: String
        get() = ImageUrlUtils.resolveImageUrl(image)
}

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
    @SerializedName("shiprocketStatus") val shiprocketStatus: String? = null,
    @SerializedName("cancelReason") val cancelReason: String? = null,
    @SerializedName("cancelledAt") val cancelledAt: String? = null,
    @SerializedName("razorpayRefundId") val razorpayRefundId: String? = null,
    @SerializedName("refundStatus") val refundStatus: String? = null,
    @SerializedName("refundAmount") val refundAmount: Double? = null
) {
    val id: String
        get() = orderId

    val status: String
        get() = statusCode.ifBlank { statusLabel }

    val isCancelled: Boolean
        get() = statusCode.equals("cancelled", ignoreCase = true)

    val canCancel: Boolean
        get() = !isCancelled && !statusCode.equals("delivered", ignoreCase = true)
}

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
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("memberTier") val memberTier: String? = "VIP Collector Club",
    @SerializedName("memberSince") val memberSince: String? = null
) {
    val effectiveId: String
        get() = id ?: mongoId ?: email
}

data class AuthResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("data") val data: User? = null,
    @SerializedName("message") val message: String? = null
) {
    val effectiveUser: User?
        get() = user ?: data
}

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("count") val count: Int? = null
)

