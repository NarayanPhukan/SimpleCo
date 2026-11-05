package com.babyygurll.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.babyygurll.admin.ui.navigation.Screen
import com.babyygurll.admin.ui.screens.*
import com.babyygurll.admin.ui.theme.BabyyGurllAdminTheme
import com.babyygurll.admin.ui.theme.MidnightNavy
import com.babyygurll.admin.ui.theme.RoyalBlue
import com.babyygurll.admin.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(Screen.Dashboard.route, "Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Orders : BottomNavItem(Screen.Orders.route, "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Products : BottomNavItem(Screen.Products.route, "Catalog", Icons.Filled.Inventory2, Icons.Outlined.Inventory2)
    object Coupons : BottomNavItem(Screen.Coupons.route, "Coupons", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer)
    object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Tune, Icons.Outlined.Tune)
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
                            BottomNavItem.Coupons,
                            BottomNavItem.Settings
                        )
                    }

                    // Hide bottom navigation on full-screen detail editors
                    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                if (showBottomBar) {
                                    NavigationBar(
                                        containerColor = Color.White,
                                        tonalElevation = 8.dp
                                    ) {
                                        bottomNavItems.forEach { item ->
                                            val isSelected = currentRoute == item.route

                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = {
                                                    navController.navigate(item.route) {
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
                                                            if (item == BottomNavItem.Orders && uiState.metrics.pendingFulfillmentCount > 0) {
                                                                Badge(
                                                                    containerColor = MaterialTheme.colorScheme.error,
                                                                    contentColor = Color.White
                                                                ) {
                                                                    Text(
                                                                        text = uiState.metrics.pendingFulfillmentCount.toString(),
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                            contentDescription = item.title
                                                        )
                                                    }
                                                },
                                                label = {
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = RoyalBlue,
                                                    selectedTextColor = RoyalBlue,
                                                    indicatorColor = RoyalBlue.copy(alpha = 0.12f),
                                                    unselectedIconColor = MidnightNavy.copy(alpha = 0.5f),
                                                    unselectedTextColor = MidnightNavy.copy(alpha = 0.6f)
                                                )
                                            )
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
                                // Dashboard Screen
                                composable(Screen.Dashboard.route) {
                                    DashboardScreen(
                                        state = uiState,
                                        onRefresh = { viewModel.refreshAll() },
                                        onNavigateOrders = {
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
                                        onSelectOrder = { order ->
                                            viewModel.selectOrder(order)
                                            navController.navigate(Screen.OrderDetail.createRoute(order.orderId))
                                        }
                                    )
                                }

                                // Orders Fulfillment Screen
                                composable(Screen.Orders.route) {
                                    OrdersScreen(
                                        state = uiState,
                                        onSearchChange = { viewModel.setOrderSearch(it) },
                                        onFilterChange = { viewModel.setOrderFilter(it) },
                                        onRefresh = { viewModel.fetchDashboardAndOrders() },
                                        onSelectOrder = { order ->
                                            viewModel.selectOrder(order)
                                            navController.navigate(Screen.OrderDetail.createRoute(order.orderId))
                                        }
                                    )
                                }

                                // Order Details Screen
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
                                        }
                                    )
                                }

                                // Products Catalog Screen
                                composable(Screen.Products.route) {
                                    ProductsScreen(
                                        state = uiState,
                                        serverUrl = uiState.serverUrl,
                                        onSearchChange = { viewModel.setProductSearch(it) },
                                        onCategoryChange = { viewModel.setProductCategoryFilter(it) },
                                        onRefresh = { viewModel.fetchProducts() },
                                        onToggleStock = { product -> viewModel.toggleProductStock(product) },
                                        onEditProduct = { product ->
                                            navController.navigate(Screen.ProductEdit.createRoute(product.id))
                                        },
                                        onDeleteProduct = { id -> viewModel.deleteProduct(id) },
                                        onNewProduct = {
                                            navController.navigate(Screen.ProductEdit.createRoute(null))
                                        }
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
                                            viewModel.saveProduct(req, id) {
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
                            }
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

