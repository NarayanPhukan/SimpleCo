package com.babyygurll.store

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
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
import com.babyygurll.store.BuildConfig
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
    object Cart : Screen("cart", "Cart", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)
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

                val availableUpdate by viewModel.availableUpdate.collectAsState()
                val updateDownloadState by viewModel.updateDownloadState.collectAsState()
                val pendingErrorReport by viewModel.pendingErrorReport.collectAsState()

                var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
                var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

                // Auto-Update Dialog
                if (availableUpdate != null) {
                    AppUpdateDialog(
                        updateInfo = availableUpdate!!,
                        currentVersion = BuildConfig.VERSION_NAME,
                        downloadState = updateDownloadState,
                        onDownloadClick = { viewModel.startAppUpdate(this@MainActivity) },
                        onDismiss = { viewModel.dismissUpdateDialog() }
                    )
                }

                // Auto-Detected Error Report Dialog
                if (!pendingErrorReport.isNullOrBlank()) {
                    ErrorReportDialog(
                        reportText = pendingErrorReport!!,
                        onDismiss = { viewModel.dismissErrorReport() }
                    )
                }

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
                            Surface(
                                color = Color.White,
                                border = BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.height(68.dp)
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
                                                Box(
                                                    modifier = if (isSelected) {
                                                        Modifier
                                                            .border(1.5.dp, BrandDarkNavy, RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                                    } else {
                                                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    BadgedBox(
                                                        badge = {
                                                            if (screen == Screen.Cart && cartBadgeCount > 0) {
                                                                Badge(
                                                                    containerColor = BrandPrimary,
                                                                    contentColor = Color.White,
                                                                    modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
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
                                                            imageVector = screen.inactiveIcon,
                                                            contentDescription = screen.title,
                                                            tint = if (isSelected) BrandDarkNavy else TextMuted,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = if (isSelected) BrandDarkNavy else TextMuted
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = Color.Transparent,
                                                selectedIconColor = BrandDarkNavy,
                                                selectedTextColor = BrandDarkNavy,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            )
                                        )
                                    }
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
                                },
                                onNavigateToShop = {
                                    navController.navigate(Screen.Categories.route)
                                },
                                onNavigateToBag = {
                                    navController.navigate(Screen.Cart.route)
                                }
                            )
                        }

                        composable(Screen.Categories.route) {
                            CategoriesScreen(
                                viewModel = viewModel,
                                onProductClick = { product ->
                                    selectedProductForDetail = product
                                    navController.navigate(Screen.ProductDetail.route)
                                },
                                onNavigateToBag = {
                                    navController.navigate(Screen.Cart.route)
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
                                },
                                onNavigateToBag = {
                                    navController.navigate(Screen.Cart.route)
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
                                },
                                onNavigateToWishlist = {
                                    navController.navigate(Screen.Wishlist.route)
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

