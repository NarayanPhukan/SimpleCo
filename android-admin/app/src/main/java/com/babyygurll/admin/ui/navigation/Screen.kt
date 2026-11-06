package com.babyygurll.admin.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Authentication")
    object Dashboard : Screen("dashboard", "Overview")
    object Orders : Screen("orders", "Orders")
    object OrderDetail : Screen("order_detail/{orderId}", "Order Details") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object Products : Screen("products", "Catalog")
    object ProductEdit : Screen("product_edit?id={id}", "Edit Piece") {
        fun createRoute(id: String? = null) = if (id != null) "product_edit?id=$id" else "product_edit"
    }
    object Coupons : Screen("coupons", "Discounts")
    object Settings : Screen("settings", "Studio Settings")
    object Customers : Screen("customers", "Customers")
    object More : Screen("more", "More")
    object CreateManualOrder : Screen("create_manual_order", "Create manual order")
    object OrderCreatedSuccess : Screen("order_created_success/{orderId}", "Order Created") {
        fun createRoute(orderId: String) = "order_created_success/$orderId"
    }
}

