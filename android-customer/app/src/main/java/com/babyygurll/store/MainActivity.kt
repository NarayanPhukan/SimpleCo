package com.babyygurll.store

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.babyygurll.store.data.model.Order
import com.babyygurll.store.data.model.Product
import com.babyygurll.store.data.model.ShippingAddress
import com.babyygurll.store.ui.screens.*
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

sealed class Screen(val route: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Categories : Screen("categories", "Categories", Icons.Filled.GridView, Icons.Outlined.GridView)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Cart : Screen("cart", "Bag", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)
    object Account : Screen("account", "Account", Icons.Filled.Person, Icons.Outlined.Person)

    object ProductDetail : Screen("product_detail", "Product Detail", Icons.Filled.Info, Icons.Outlined.Info)
    object Checkout : Screen("checkout", "Checkout", Icons.Filled.Payment, Icons.Outlined.Payment)
    object Orders : Screen("orders", "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object OrderDetail : Screen("order_detail", "Order Detail", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping)
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Home,
    Screen.Categories,
    Screen.Wishlist,
    Screen.Cart,
    Screen.Account
)

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private val viewModel: StoreViewModel by viewModels()
    private var pendingShippingAddress: ShippingAddress? = null
    private var onPaymentSuccessCallback: ((order: Order) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)

        setContent {
            BabyyGurllCustomerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val cartItems by viewModel.cartItems.collectAsState()
                val cartBadgeCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

                var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
                var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

                // Observe Toast Events
                LaunchedEffect(Unit) {
                    viewModel.eventMessage.collect { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                val showBottomBar = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 8.dp
                            ) {
                                BOTTOM_NAV_ITEMS.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (screen == Screen.Cart && cartBadgeCount > 0) {
                                                        Badge(
                                                            containerColor = BrandPrimary,
                                                            contentColor = Color.White
                                                        ) {
                                                            Text(
                                                                text = if (cartBadgeCount > 99) "99+" else "$cartBadgeCount",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                                            )
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                                    contentDescription = screen.title,
                                                    tint = if (isSelected) BrandPrimary else TextMuted
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = if (isSelected) BrandPrimary else TextMuted
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = BrandLightPink
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onProductClick = { product ->
                                    selectedProductForDetail = product
                                    navController.navigate(Screen.ProductDetail.route)
                                }
                            )
                        }

                        composable(Screen.Categories.route) {
                            CategoriesScreen(
                                viewModel = viewModel,
                                onCategorySelected = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                }
                            )
                        }

                        composable(Screen.Wishlist.route) {
                            WishlistScreen(
                                viewModel = viewModel,
                                onProductClick = { product ->
                                    selectedProductForDetail = product
                                    navController.navigate(Screen.ProductDetail.route)
                                },
                                onExploreClick = {
                                    navController.navigate(Screen.Home.route)
                                }
                            )
                        }

                        composable(Screen.Cart.route) {
                            CartScreen(
                                viewModel = viewModel,
                                onCheckoutClick = {
                                    navController.navigate(Screen.Checkout.route)
                                },
                                onExploreClick = {
                                    navController.navigate(Screen.Home.route)
                                }
                            )
                        }

                        composable(Screen.Account.route) {
                            AccountScreen(
                                viewModel = viewModel,
                                onNavigateToOrders = {
                                    navController.navigate(Screen.Orders.route)
                                }
                            )
                        }

                        composable(Screen.ProductDetail.route) {
                            val prod = selectedProductForDetail
                            if (prod != null) {
                                ProductDetailScreen(
                                    product = prod,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToCart = {
                                        navController.navigate(Screen.Cart.route)
                                    },
                                    onBuyNow = {
                                        navController.navigate(Screen.Checkout.route)
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        }

                        composable(Screen.Checkout.route) {
                            CheckoutScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onStartPayment = { address, amount ->
                                    pendingShippingAddress = address
                                    onPaymentSuccessCallback = { createdOrder ->
                                        selectedOrderForDetail = createdOrder
                                        navController.navigate(Screen.OrderDetail.route) {
                                            popUpTo(Screen.Home.route) { inclusive = false }
                                        }
                                    }
                                    startRazorpayCheckout(address, amount)
                                }
                            )
                        }

                        composable(Screen.Orders.route) {
                            OrdersScreen(
                                viewModel = viewModel,
                                onOrderClick = { order ->
                                    selectedOrderForDetail = order
                                    navController.navigate(Screen.OrderDetail.route)
                                },
                                onExploreClick = {
                                    navController.navigate(Screen.Home.route)
                                }
                            )
                        }

                        composable(Screen.OrderDetail.route) {
                            val ord = selectedOrderForDetail
                            if (ord != null) {
                                OrderDetailScreen(
                                    order = ord,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            } else {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startRazorpayCheckout(address: ShippingAddress, amountInRupees: Double) {
        val checkout = Checkout()
        // Default standard store key
        checkout.setKeyID("rzp_test_YourTestKeyIdHere")

        try {
            val options = JSONObject().apply {
                put("name", "BabyyGurll Crochet Studio")
                put("description", "Handcrafted Bespoke Crochet Pieces")
                put("currency", "INR")
                put("amount", (amountInRupees * 100).toLong())
                put("send_sms_hash", true)

                val prefill = JSONObject().apply {
                    put("contact", address.phone)
                    put("email", viewModel.currentUser.value?.email ?: "guest@babyygurll.in")
                }
                put("prefill", prefill)

                val theme = JSONObject().apply {
                    put("color", "#E85D75")
                }
                put("theme", theme)
            }

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Payment error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val addr = pendingShippingAddress ?: return
        val rzpId = razorpayPaymentId ?: paymentData?.paymentId ?: "pay_simulated_${System.currentTimeMillis()}"
        val orderId = paymentData?.orderId ?: ""

        viewModel.placeOrder(
            shippingAddress = addr,
            paymentId = rzpId,
            razorpayOrderId = orderId
        ) { order ->
            onPaymentSuccessCallback?.invoke(order)
            pendingShippingAddress = null
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment cancelled or declined", Toast.LENGTH_SHORT).show()
    }
}

