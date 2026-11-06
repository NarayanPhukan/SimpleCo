package com.babyygurll.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.babyygurll.admin.ui.navigation.Screen
import com.babyygurll.admin.ui.screens.*
import com.babyygurll.admin.ui.theme.BabyyGurllAdminTheme
import com.babyygurll.admin.ui.theme.MidnightNavy
import com.babyygurll.admin.ui.theme.RoyalBlue
import com.babyygurll.admin.ui.theme.TextMuted
import com.babyygurll.admin.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Filled.GridView, Icons.Outlined.GridView)
    object Orders : BottomNavItem(Screen.Orders.route, "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Products : BottomNavItem(Screen.Products.route, "Products", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)
    object Customers : BottomNavItem(Screen.Customers.route, "Customers", Icons.Filled.Person, Icons.Outlined.Person)
    object More : BottomNavItem(Screen.More.route, "More", Icons.Filled.Menu, Icons.Outlined.Menu)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AdminViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BabyyGurllAdminTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                var showServerConfigDialog by remember { mutableStateOf(false) }

                // Collect transient toast / snackbar messages from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.eventMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val bottomNavItems = remember {
                        listOf(
                            BottomNavItem.Dashboard,
                            BottomNavItem.Orders,
                            BottomNavItem.Products,
                            BottomNavItem.Customers,
                            BottomNavItem.More
                        )
                    }

                    val operationsSubRoutes = remember {
                        setOf(
                            Screen.Inventory.route,
                            Screen.ProductionQueue.route,
                            Screen.CustomRequests.route,
                            Screen.Shipping.route,
                            Screen.Refunds.route
                        )
                    }

                    // Show bottom bar on primary tabs and store operations screens (matching design)
                    val showBottomBar = currentRoute in bottomNavItems.map { it.route } || currentRoute in operationsSubRoutes

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (showBottomBar) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White,
                                    shadowElevation = 8.dp,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        bottomNavItems.forEach { item ->
                                            val isSelected = currentRoute == item.route || (item == BottomNavItem.More && currentRoute in operationsSubRoutes)

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        navController.navigate(item.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                    .padding(vertical = 6.dp)
                                            ) {
                                                // Active Indicator Pill Bar on Top of Item
                                                Box(
                                                    modifier = Modifier
                                                        .width(28.dp)
                                                        .height(3.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(if (isSelected) RoyalBlue else Color.Transparent)
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                BadgedBox(
                                                    badge = {
                                                        if (item == BottomNavItem.Orders && uiState.metrics.pendingFulfillmentCount > 0) {
                                                            Badge(
                                                                containerColor = RoyalBlue,
                                                                contentColor = Color.White
                                                            ) {
                                                                Text(
                                                                    text = uiState.metrics.pendingFulfillmentCount.toString(),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                        contentDescription = item.title,
                                                        tint = if (isSelected) RoyalBlue else TextMuted,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = item.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) RoyalBlue else TextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Dashboard.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // Dashboard Screen (Image 1 & 2)
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(
                                    state = uiState,
                                    onRefresh = { viewModel.refreshAll() },
                                    onNavigateOrders = { filter ->
                                        if (filter != null) {
                                            viewModel.setOrderFilter(filter)
                                        }
                                        navController.navigate(Screen.Orders.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateProducts = {
                                        navController.navigate(Screen.Products.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateNewProduct = {
                                        navController.navigate(Screen.ProductEdit.createRoute(null))
                                    },
                                    onNavigateCoupons = {
                                        navController.navigate(Screen.Coupons.route)
                                    },
                                    onSelectOrder = { order ->
                                        viewModel.selectOrder(order)
                                        navController.navigate(Screen.OrderDetail.createRoute(order.orderId))
                                    },
                                    onSelectPeriod = { period ->
                                        viewModel.setSelectedPeriod(period)
                                    }
                                )
                            }

                            // Orders Fulfillment Screen (Image 3)
                            composable(Screen.Orders.route) {
                                OrdersScreen(
                                    state = uiState,
                                    onSearchChange = { viewModel.setOrderSearch(it) },
                                    onFilterChange = { viewModel.setOrderFilter(it) },
                                    onRefresh = { viewModel.fetchDashboardAndOrders() },
                                    onSelectOrder = { order ->
                                        viewModel.selectOrder(order)
                                        navController.navigate(Screen.OrderDetail.createRoute(order.orderId))
                                    },
                                    onCreateManualOrder = {
                                        navController.navigate(Screen.CreateManualOrder.route)
                                    },
                                    onOpenShippingChecker = {
                                        navController.navigate(Screen.Settings.route)
                                    },
                                    onOpenMoreMenu = {
                                        navController.navigate(Screen.More.route)
                                    }
                                )
                            }

                            // Order Details Screen (Image 4 & 5)
                            composable(
                                route = Screen.OrderDetail.route,
                                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                                val order = uiState.orders.find { it.orderId == orderId } ?: uiState.selectedOrder

                                OrderDetailScreen(
                                    order = order,
                                    serverUrl = uiState.serverUrl,
                                    onBack = { navController.popBackStack() },
                                    onAdvanceStage = { id -> viewModel.advanceOrder(id) },
                                    onUpdateTracking = { id, carrier, tracking, estDate ->
                                        viewModel.updateTracking(id, carrier, tracking, estDate)
                                    },
                                    internalNotes = uiState.orderNotesMap[orderId] ?: emptyList(),
                                    onAddInternalNote = { id, note ->
                                        viewModel.addOrderInternalNote(id, note)
                                    },
                                    checklist = uiState.orderChecklistMap[orderId] ?: emptyMap(),
                                    onToggleChecklistItem = { id, task, isDone ->
                                        viewModel.toggleOrderChecklistItem(id, task, isDone)
                                    }
                                )
                            }

                            // Products Catalog Screen
                            composable(Screen.Products.route) {
                                ProductsScreen(
                                    state = uiState,
                                    serverUrl = uiState.serverUrl,
                                    onSearchChange = { viewModel.setProductSearch(it) },
                                    onFilterChange = { viewModel.setProductFilter(it) },
                                    onRefresh = { viewModel.fetchProducts() },
                                    onSelectProduct = { product ->
                                        viewModel.selectProduct(product)
                                        navController.navigate(Screen.ProductDetail.createRoute(product.id.ifBlank { product.mongoId ?: "" }))
                                    },
                                    onEditProduct = { product ->
                                        navController.navigate(Screen.ProductEdit.createRoute(product.id))
                                    },
                                    onToggleStock = { product -> viewModel.toggleProductStock(product) },
                                    onDeleteProduct = { id -> viewModel.deleteProduct(id) },
                                    onNewProduct = {
                                        navController.navigate(Screen.ProductEdit.createRoute(null))
                                    }
                                )
                            }

                            // Product Details Screen (Images 1 & 2)
                            composable(
                                route = Screen.ProductDetail.route,
                                arguments = listOf(navArgument("productId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                                val product = uiState.products.find { it.id == productId || it.mongoId == productId }
                                    ?: uiState.selectedProduct

                                ProductDetailScreen(
                                    product = product,
                                    serverUrl = uiState.serverUrl,
                                    orders = uiState.orders,
                                    onBack = { navController.popBackStack() },
                                    onEdit = {
                                        product?.let {
                                            navController.navigate(Screen.ProductEdit.createRoute(it.id))
                                        }
                                    },
                                    onToggleStock = {
                                        product?.let { viewModel.toggleProductStock(it) }
                                    },
                                    onDelete = {
                                        product?.let {
                                            viewModel.deleteProduct(it.mongoId ?: it.id)
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            }

                            // Customers Screen (Tab 4)
                            composable(Screen.Customers.route) {
                                CustomersScreen(
                                    state = uiState,
                                    onSearchChange = { viewModel.setCustomerSearch(it) },
                                    onFilterChange = { viewModel.setCustomerFilter(it) },
                                    onRefresh = { viewModel.fetchDashboardAndOrders() },
                                    onNavigateOrders = { custQuery ->
                                        viewModel.setOrderSearch(custQuery)
                                        navController.navigate(Screen.Orders.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            // More Options Screen (Tab 5)
                            composable(Screen.More.route) {
                                MoreScreen(
                                    state = uiState,
                                    onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                                    onNavigateCoupons = { navController.navigate(Screen.Coupons.route) },
                                    onNavigateProducts = {
                                        navController.navigate(Screen.Products.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateOrders = { filter ->
                                        if (filter != null) {
                                            viewModel.setOrderFilter(filter)
                                        }
                                        navController.navigate(Screen.Orders.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateInventory = { navController.navigate(Screen.Inventory.route) },
                                    onNavigateCustomRequests = { navController.navigate(Screen.CustomRequests.route) },
                                    onNavigateProductionQueue = { navController.navigate(Screen.ProductionQueue.route) },
                                    onNavigateShipping = { navController.navigate(Screen.Shipping.route) },
                                    onNavigateRefunds = { navController.navigate(Screen.Refunds.route) },
                                    onCheckUpdates = { viewModel.checkForAppUpdates(silent = false) },
                                    onOpenServerConfig = { showServerConfigDialog = true },
                                    onLogout = { viewModel.logout() }
                                )
                            }

                            // Operations: Inventory Screen (Screenshot 1)
                            composable(Screen.Inventory.route) {
                                InventoryScreen(
                                    state = uiState,
                                    onBack = { navController.popBackStack() },
                                    onNewProduct = { navController.navigate(Screen.ProductEdit.createRoute(null)) },
                                    onSelectProduct = { prodId -> navController.navigate(Screen.ProductDetail.createRoute(prodId)) }
                                )
                            }

                            // Operations: Production Queue Screen (Screenshot 2)
                            composable(Screen.ProductionQueue.route) {
                                ProductionQueueScreen(
                                    state = uiState,
                                    onBack = { navController.popBackStack() },
                                    onAdvanceStage = { id -> viewModel.advanceOrder(id) }
                                )
                            }

                            // Operations: Custom Requests Screen (Screenshot 3)
                            composable(Screen.CustomRequests.route) {
                                CustomRequestsScreen(
                                    state = uiState,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Operations: Shipping Screen (Screenshot 4)
                            composable(Screen.Shipping.route) {
                                ShippingScreen(
                                    state = uiState,
                                    onBack = { navController.popBackStack() },
                                    onViewOrder = { orderId ->
                                        val order = uiState.orders.find { it.orderId == orderId }
                                        if (order != null) viewModel.selectOrder(order)
                                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                                    }
                                )
                            }

                            // Razorpay Payments & Refunds Screen (Screenshot 1)
                            composable(Screen.Refunds.route) {
                                RefundsScreen(
                                    state = uiState,
                                    onBack = { navController.popBackStack() },
                                    onViewOrder = { orderId ->
                                        val order = uiState.orders.find { it.orderId == orderId }
                                        if (order != null) viewModel.selectOrder(order)
                                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                                    },
                                    onIssueRefund = { orderId, amount, reason ->
                                        viewModel.issueRefund(orderId, amount, reason)
                                    },
                                    onRefresh = { viewModel.fetchRefunds() }
                                )
                            }

                            // Product Editor Screen (Add / Edit)
                            composable(
                                route = Screen.ProductEdit.route,
                                arguments = listOf(
                                    navArgument("id") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val productId = backStackEntry.arguments?.getString("id")
                                val existingProduct = remember(productId, uiState.products) {
                                    if (!productId.isNullOrBlank()) {
                                        uiState.products.find { it.id == productId || it.mongoId == productId }
                                    } else null
                                }

                                ProductEditScreen(
                                    product = existingProduct,
                                    serverUrl = uiState.serverUrl,
                                    onBack = { navController.popBackStack() },
                                    onSave = { req, id ->
                                        val cleanId = id?.trim()?.ifBlank { null }
                                            ?: existingProduct?.id?.trim()?.ifBlank { null }
                                            ?: existingProduct?.mongoId?.trim()?.ifBlank { null }
                                        viewModel.saveProduct(req, cleanId) {
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            }

                            // Coupons & Promos Screen
                            composable(Screen.Coupons.route) {
                                CouponsScreen(viewModel = viewModel)
                            }

                            // Settings Screen
                            composable(Screen.Settings.route) {
                                SettingsScreen(viewModel = viewModel)
                            }

                            // Create Manual Order Screen
                            composable(Screen.CreateManualOrder.route) {
                                CreateManualOrderScreen(
                                    state = uiState,
                                    onBack = { navController.popBackStack() },
                                    onCreateOrder = { req, onDone ->
                                        viewModel.createManualOrder(req) { created ->
                                            onDone(created)
                                        }
                                    },
                                    onOrderCreatedSuccess = { orderId ->
                                        navController.navigate(Screen.OrderCreatedSuccess.createRoute(orderId)) {
                                            popUpTo(Screen.Orders.route)
                                        }
                                    }
                                )
                            }

                            // Order Created Confirmation Screen
                            composable(
                                route = Screen.OrderCreatedSuccess.route,
                                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                                OrderCreatedSuccessScreen(
                                    orderId = orderId,
                                    onViewOrder = { id ->
                                        val found = uiState.orders.find { it.orderId == id }
                                        if (found != null) {
                                            viewModel.selectOrder(found)
                                        }
                                        navController.navigate(Screen.OrderDetail.createRoute(id)) {
                                            popUpTo(Screen.Orders.route)
                                        }
                                    },
                                    onBackToOrders = {
                                        navController.navigate(Screen.Orders.route) {
                                            popUpTo(Screen.Orders.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Server Config Dialog
                    if (showServerConfigDialog) {
                        ServerConfigDialog(
                            currentUrl = uiState.serverUrl,
                            onSave = { newUrl ->
                                viewModel.updateServerUrl(newUrl)
                                showServerConfigDialog = false
                            },
                            onDismiss = { showServerConfigDialog = false }
                        )
                    }

                    // Global Auto-Update Detection Dialog
                    if (uiState.isUpdateAvailable && uiState.appUpdateInfo != null) {
                        AppUpdateDialog(
                            updateInfo = uiState.appUpdateInfo!!,
                            isCritical = uiState.isCriticalUpdate,
                            onDismiss = { viewModel.dismissUpdateDialog() }
                        )
                    }
                }
            }
        }
    }
}
