package com.babyygurll.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyygurll.admin.BabyyGurllAdminApp
import com.babyygurll.admin.data.api.NetworkModule
import com.babyygurll.admin.data.auth.FirebaseAuthManager
import com.babyygurll.admin.data.diagnostics.ErrorLogManager
import com.babyygurll.admin.data.model.*
import com.babyygurll.admin.data.repository.AdminRepository
import com.babyygurll.admin.data.repository.PreferencesManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val isAuthenticated: Boolean = true,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncedTime: String = "Just now",
    val serverUrl: String = PreferencesManager.DEFAULT_SERVER_URL,
    val errorMessage: String? = null,
    val adminEmail: String? = "Studio Administrator",
    val authProvider: String = "direct",
    
    // Dashboard
    val metrics: DashboardMetrics = DashboardMetrics(),

    // Orders
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedOrderFilter: String = "all",
    val orderSearchQuery: String = "",
    val selectedOrder: Order? = null,

    // Products
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val productSearchQuery: String = "",
    val selectedProductCategory: String = "all",

    // Coupons
    val coupons: List<Coupon> = emptyList(),

    // Store Settings
    val settings: StoreSettings = StoreSettings(),
    val pinServiceabilityResult: PincodeCheckResponse? = null,

    // Auto Update Detection
    val appUpdateInfo: AppVersionInfo? = null,
    val isUpdateAvailable: Boolean = false,
    val isCriticalUpdate: Boolean = false,
    val isCheckingUpdate: Boolean = false,

    // Error Detection & System Diagnostics
    val diagnostics: DiagnosticsData? = null,
    val isCheckingDiagnostics: Boolean = false,
    val clientErrors: List<ClientErrorLog> = emptyList()
)

class AdminViewModel : ViewModel() {

    private val prefs = PreferencesManager(BabyyGurllAdminApp.instance)
    private val repository = AdminRepository(prefs)

    private val _uiState = MutableStateFlow(
        AdminUiState(
            isAuthenticated = true,
            serverUrl = prefs.serverUrl,
            adminEmail = "Studio Administrator",
            authProvider = "direct"
        )
    )
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage: SharedFlow<String> = _eventMessage.asSharedFlow()

    init {
        // Collect client error logs reactively
        viewModelScope.launch {
            ErrorLogManager.logsFlow.collect { logs ->
                _uiState.update { it.copy(clientErrors = logs) }
            }
        }

        // Direct access: immediately fetch store data without login
        refreshAll()

        // Real-time periodic dynamic sync loop (polls every 12 seconds in background)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(12_000)
                fetchDashboardAndOrders(silent = true)
                fetchProducts(silent = true)
            }
        }

        // Silent auto-update check on boot
        checkForAppUpdates(silent = true)
    }

    fun updateServerUrl(newUrl: String) {
        prefs.serverUrl = newUrl
        NetworkModule.invalidateClient()
        _uiState.update { it.copy(serverUrl = prefs.serverUrl) }
        viewModelScope.launch {
            _eventMessage.emit("Server URL updated: ${prefs.serverUrl}")
        }
        refreshAll()
    }

    // Independent Firebase Authentication
    fun loginWithFirebase(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = FirebaseAuthManager.signIn(email, pass)
            result.onSuccess { user ->
                prefs.isLoggedIn = true
                prefs.adminEmail = user.email ?: email
                prefs.authProvider = "firebase"
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        adminEmail = user.email ?: email,
                        authProvider = "firebase",
                        errorMessage = null
                    )
                }
                _eventMessage.emit("Welcome, ${user.email ?: "Admin"} 🧶")
                refreshAll()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        isLoading = false,
                        errorMessage = err.message ?: "Authentication failed"
                    )
                }
                _eventMessage.emit(err.message ?: "Firebase authentication failed")
            }
        }
    }

    fun registerWithFirebase(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = FirebaseAuthManager.createAccount(email, pass)
            result.onSuccess { user ->
                prefs.isLoggedIn = true
                prefs.adminEmail = user.email ?: email
                prefs.authProvider = "firebase"
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        adminEmail = user.email ?: email,
                        authProvider = "firebase",
                        errorMessage = null
                    )
                }
                _eventMessage.emit("Admin account registered for ${user.email} 🧶")
                refreshAll()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Registration failed"
                    )
                }
                _eventMessage.emit(err.message ?: "Account registration failed")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = FirebaseAuthManager.sendPasswordReset(email)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _eventMessage.emit("Password reset email sent to $email")
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false) }
                _eventMessage.emit(err.message ?: "Failed to send reset link")
            }
        }
    }

    // Backend PIN / Password Login
    fun login(password: String) {
        loginWithServerPassword(password)
    }

    fun loginWithServerPassword(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.verifyPassword(password)
            result.onSuccess {
                prefs.isLoggedIn = true
                prefs.authProvider = "server_pin"
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        authProvider = "server_pin",
                        errorMessage = null
                    )
                }
                _eventMessage.emit("Welcome to Studio Portal 🧶")
                refreshAll()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        isLoading = false,
                        errorMessage = err.message
                    )
                }
                _eventMessage.emit(err.message ?: "Authentication failed")
            }
        }
    }

    fun logout() {
        FirebaseAuthManager.signOut()
        prefs.clearSession()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                adminEmail = null
            )
        }
        viewModelScope.launch {
            _eventMessage.emit("Signed out of Studio Portal")
        }
    }

    fun refreshAll() {
        fetchDashboardAndOrders()
        fetchProducts()
        fetchCoupons()
        fetchSettings()
        fetchDiagnostics()
        checkForAppUpdates(silent = true)
    }

    // Orders & Dashboard
    fun fetchDashboardAndOrders(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true) } else _uiState.update { it.copy(isSyncing = true) }
            val result = repository.fetchAllOrders()
            result.onSuccess { orderList ->
                val revenue = orderList.filter { it.statusCode != "cancelled" }.sumOf { it.total }
                val pending = orderList.count { it.statusCode in listOf("received", "crafted", "packed") }
                val aov = if (orderList.isNotEmpty()) revenue / orderList.size else 0.0

                val metrics = DashboardMetrics(
                    totalRevenue = revenue,
                    totalOrders = orderList.size,
                    pendingFulfillmentCount = pending,
                    activeProductsCount = _uiState.value.products.count { it.inStock },
                    averageOrderValue = aov
                )

                val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

                _uiState.update { state ->
                    state.copy(
                        orders = orderList,
                        metrics = metrics,
                        isLoading = false,
                        isSyncing = false,
                        lastSyncedTime = timeStr
                    )
                }
                applyOrderFilters()
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, isSyncing = false) }
                if (!silent) {
                    _eventMessage.emit("Orders sync: ${err.message}")
                }
            }
        }
    }

    fun setOrderFilter(filter: String) {
        _uiState.update { it.copy(selectedOrderFilter = filter) }
        applyOrderFilters()
    }

    fun setOrderSearch(query: String) {
        _uiState.update { it.copy(orderSearchQuery = query) }
        applyOrderFilters()
    }

    private fun applyOrderFilters() {
        val all = _uiState.value.orders
        val filter = _uiState.value.selectedOrderFilter
        val query = _uiState.value.orderSearchQuery.trim().lowercase()

        val filtered = all.filter { order ->
            val matchesFilter = when (filter) {
                "all" -> true
                "pending" -> order.statusCode in listOf("received", "crafted", "packed")
                else -> order.statusCode == filter
            }

            val matchesQuery = if (query.isEmpty()) true else {
                order.orderId.lowercase().contains(query) ||
                (order.shippingAddress?.recipientName?.lowercase()?.contains(query) == true) ||
                order.userEmail.lowercase().contains(query) ||
                (order.shippingAddress?.phone?.contains(query) == true) ||
                (order.trackingNumber?.lowercase()?.contains(query) == true)
            }

            matchesFilter && matchesQuery
        }

        _uiState.update { it.copy(filteredOrders = filtered) }
    }

    fun selectOrder(order: Order?) {
        _uiState.update { it.copy(selectedOrder = order) }
    }

    fun advanceOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.advanceOrderStage(orderId)
            result.onSuccess { updated ->
                _eventMessage.emit("Order #${updated.orderId} advanced to: ${updated.statusLabel}")
                fetchDashboardAndOrders()
                if (_uiState.value.selectedOrder?.orderId == orderId) {
                    _uiState.update { it.copy(selectedOrder = updated) }
                }
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to advance stage")
            }
        }
    }

    fun updateTracking(orderId: String, carrier: String, trackingNumber: String, estDate: String) {
        viewModelScope.launch {
            val result = repository.updateOrderTracking(orderId, carrier, trackingNumber, estDate)
            result.onSuccess { updated ->
                _eventMessage.emit("Tracking updated for #${updated.orderId}")
                fetchDashboardAndOrders()
                if (_uiState.value.selectedOrder?.orderId == orderId) {
                    _uiState.update { it.copy(selectedOrder = updated) }
                }
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to update tracking")
            }
        }
    }

    // Products
    fun fetchProducts(silent: Boolean = false) {
        viewModelScope.launch {
            val result = repository.fetchProducts()
            result.onSuccess { list ->
                _uiState.update { state ->
                    val activeCount = list.count { it.inStock }
                    state.copy(
                        products = list,
                        metrics = state.metrics.copy(activeProductsCount = activeCount)
                    )
                }
                applyProductFilters()
            }.onFailure { err ->
                if (!silent) {
                    _eventMessage.emit("Catalog sync: ${err.message}")
                }
            }
        }
    }

    fun setProductCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedProductCategory = category) }
        applyProductFilters()
    }

    fun setProductSearch(query: String) {
        _uiState.update { it.copy(productSearchQuery = query) }
        applyProductFilters()
    }

    private fun applyProductFilters() {
        val all = _uiState.value.products
        val cat = _uiState.value.selectedProductCategory
        val query = _uiState.value.productSearchQuery.trim().lowercase()

        val filtered = all.filter { p ->
            val matchesCat = if (cat == "all") true else p.category == cat
            val matchesQuery = if (query.isEmpty()) true else {
                p.name.lowercase().contains(query) ||
                p.categoryName.lowercase().contains(query) ||
                p.description.lowercase().contains(query)
            }
            matchesCat && matchesQuery
        }

        _uiState.update { it.copy(filteredProducts = filtered) }
    }

    fun toggleProductStock(product: Product) {
        viewModelScope.launch {
            val nextStock = !product.inStock
            val result = repository.updateProduct(product.id, mapOf("inStock" to nextStock))
            result.onSuccess {
                _eventMessage.emit("${product.name} marked as ${if (nextStock) "In Stock" else "Sold Out"}")
                fetchProducts()
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to update stock status")
            }
        }
    }

    fun saveProduct(req: ProductCreateRequest, existingId: String? = null, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = if (existingId != null) {
                repository.updateProduct(existingId, mapOf(
                    "name" to req.name,
                    "price" to req.price,
                    "originalPrice" to req.originalPrice,
                    "category" to req.category,
                    "image" to req.image,
                    "badge" to req.badge,
                    "leadTime" to req.leadTime,
                    "description" to req.description,
                    "inStock" to req.inStock
                ))
            } else {
                repository.createProduct(req)
            }

            result.onSuccess {
                _eventMessage.emit("Product saved successfully ✓")
                fetchProducts()
                onDone()
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Could not save product")
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            val result = repository.deleteProduct(id)
            result.onSuccess {
                _eventMessage.emit("Product removed from boutique")
                fetchProducts()
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to delete product")
            }
        }
    }

    // Coupons
    fun fetchCoupons() {
        viewModelScope.launch {
            val result = repository.fetchCoupons()
            result.onSuccess { list ->
                _uiState.update { it.copy(coupons = list) }
            }.onFailure { err ->
                _eventMessage.emit("Coupons sync: ${err.message}")
            }
        }
    }

    fun createCoupon(req: CouponCreateRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = repository.createCoupon(req)
            result.onSuccess {
                _eventMessage.emit("Coupon code ${it.code} activated!")
                fetchCoupons()
                onDone()
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to create coupon")
            }
        }
    }

    fun deleteCoupon(id: String) {
        viewModelScope.launch {
            val result = repository.deleteCoupon(id)
            result.onSuccess {
                _eventMessage.emit("Coupon deleted")
                fetchCoupons()
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to delete coupon")
            }
        }
    }

    // Store Settings
    fun fetchSettings() {
        viewModelScope.launch {
            val result = repository.fetchSettings()
            result.onSuccess { s ->
                _uiState.update { it.copy(settings = s) }
            }.onFailure { err ->
                _eventMessage.emit("Settings sync: ${err.message}")
            }
        }
    }

    fun saveSettings(settings: StoreSettings) {
        viewModelScope.launch {
            val result = repository.saveSettings(settings)
            result.onSuccess {
                _uiState.update { it.copy(settings = settings) }
                _eventMessage.emit("Studio settings saved successfully ✓")
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to save settings")
            }
        }
    }

    fun checkPincode(pincode: String) {
        viewModelScope.launch {
            val result = repository.checkPincode(pincode)
            result.onSuccess { res ->
                _uiState.update { it.copy(pinServiceabilityResult = res) }
                _eventMessage.emit(res.message ?: if (res.serviceable == true) "PIN $pincode is Serviceable" else "PIN not serviceable")
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Pincode lookup error")
            }
        }
    }

    // Auto-Update Detection
    fun checkForAppUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isCheckingUpdate = true) }
            val currentVersionCode = 2 // Matches BuildConfig.VERSION_CODE
            val result = repository.fetchAppVersion()
            result.onSuccess { info ->
                val hasUpdate = info.latestVersionCode > currentVersionCode
                val critical = hasUpdate && (currentVersionCode < info.minRequiredVersionCode || info.critical)
                _uiState.update {
                    it.copy(
                        appUpdateInfo = info,
                        isUpdateAvailable = hasUpdate,
                        isCriticalUpdate = critical,
                        isCheckingUpdate = false
                    )
                }
                if (!silent) {
                    if (hasUpdate) {
                        _eventMessage.emit("New update v${info.latestVersionName} available!")
                    } else {
                        _eventMessage.emit("You are on the latest version (v1.1.0) ✓")
                    }
                }
            }.onFailure { err ->
                if (!silent) {
                    _uiState.update { it.copy(isCheckingUpdate = false) }
                    _eventMessage.emit("Update check failed: ${err.message}")
                }
                ErrorLogManager.logWarning("UPDATE_CHECK", "Could not check app version: ${err.message}")
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(isUpdateAvailable = false) }
    }

    // Error Detection & System Diagnostics
    fun fetchDiagnostics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingDiagnostics = true) }
            val result = repository.fetchDiagnostics()
            result.onSuccess { data ->
                _uiState.update { it.copy(diagnostics = data, isCheckingDiagnostics = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(isCheckingDiagnostics = false) }
                ErrorLogManager.logError("DIAGNOSTICS", "Diagnostics fetch failed: ${err.message}", err)
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            ErrorLogManager.clearLogs()
            repository.clearServerErrorLogs()
            _eventMessage.emit("Diagnostics & error logs cleared ✓")
            fetchDiagnostics()
        }
    }
}
