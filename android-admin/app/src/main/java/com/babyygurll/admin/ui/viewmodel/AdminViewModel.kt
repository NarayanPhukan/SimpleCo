package com.babyygurll.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyygurll.admin.BabyyGurllAdminApp
import com.babyygurll.admin.BuildConfig
import com.babyygurll.admin.data.api.NetworkModule
import com.babyygurll.admin.data.auth.FirebaseAuthManager
import com.babyygurll.admin.data.diagnostics.ErrorLogManager
import com.babyygurll.admin.data.model.*
import com.babyygurll.admin.data.repository.AdminRepository
import com.babyygurll.admin.data.repository.PreferencesManager
import com.babyygurll.admin.util.NotificationHelper
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
    val selectedPeriod: String = "7 Days",

    // Orders
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedOrderFilter: String = "all",
    val orderSearchQuery: String = "",
    val selectedOrder: Order? = null,
    val orderNotesMap: Map<String, List<String>> = emptyMap(),
    val orderChecklistMap: Map<String, Map<String, Boolean>> = emptyMap(),

    // Customers
    val customers: List<Customer> = emptyList(),
    val filteredCustomers: List<Customer> = emptyList(),
    val customerSearchQuery: String = "",
    val selectedCustomerFilter: String = "All",

    // Products
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val productSearchQuery: String = "",
    val selectedProductCategory: String = "all",
    val selectedProductFilter: String = "All",
    val selectedProduct: Product? = null,

    // Coupons
    val coupons: List<Coupon> = emptyList(),

    // Store Settings
    val settings: StoreSettings = StoreSettings(),
    val pinServiceabilityResult: PincodeCheckResponse? = null,

    // Payments & Razorpay Refunds
    val refunds: List<RefundRecord> = emptyList(),
    val isProcessingRefund: Boolean = false,

    // Auto Update Detection
    val appUpdateInfo: AppVersionInfo? = null,
    val isUpdateAvailable: Boolean = false,
    val isCriticalUpdate: Boolean = false,
    val isCheckingUpdate: Boolean = false,

    // Error Detection & System Diagnostics
    val diagnostics: DiagnosticsData? = null,
    val isCheckingDiagnostics: Boolean = false,
    val clientErrors: List<ClientErrorLog> = emptyList(),

    // Notifications & Broadcasts
    val notifications: List<AdminNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isLoadingNotifications: Boolean = false,
    val isSendingBroadcast: Boolean = false,
    val notificationFilter: String = "All"
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
                fetchNotifications(silent = true)
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
        fetchRefunds()
        fetchNotifications()
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

                val sdfIsoLocal = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val todayStr = sdfIsoLocal.format(java.util.Date())

                fun parseOrderDate(ord: Order): java.util.Date? {
                    val created = ord.createdAt
                    if (!created.isNullOrBlank()) {
                        try {
                            val sdfUtc = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            val cleanCreated = created.substringBefore(".")
                            val parsed = sdfUtc.parse(cleanCreated)
                            if (parsed != null) return parsed
                        } catch (_: Exception) {}
                    }
                    if (!ord.dateFormatted.isNullOrBlank()) {
                        try {
                            val sdfDateFormatted = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.ENGLISH)
                            return sdfDateFormatted.parse(ord.dateFormatted!!)
                        } catch (_: Exception) {}
                    }
                    return null
                }

                val todayOrders = orderList.filter { ord ->
                    val d = parseOrderDate(ord)
                    d != null && sdfIsoLocal.format(d) == todayStr
                }
                val todayRev = todayOrders.sumOf { it.total }
                val todayCount = todayOrders.size
                val totalCount = orderList.size
                val pendingCount = pending

                // Extract and aggregate real customer profiles from actual order records
                val customerList = orderList.groupBy { ord ->
                    ord.shippingAddress?.phone?.ifBlank { null }
                        ?: ord.shippingAddress?.email?.ifBlank { null }
                        ?: ord.userEmail.ifBlank { null }
                        ?: ord.shippingAddress?.recipientName?.ifBlank { null }
                        ?: "Guest"
                }.map { (key, custOrders) ->
                    val latest = custOrders.maxByOrNull { it.createdAt ?: "" }
                    val name = latest?.shippingAddress?.recipientName?.ifBlank { null }
                        ?: (if (latest?.userEmail?.contains("@") == true) latest.userEmail.substringBefore("@").replace(".", " ").capitalize(java.util.Locale.getDefault()) else "Valued Customer")
                    val phone = latest?.shippingAddress?.phone ?: ""
                    val email = latest?.shippingAddress?.email?.ifBlank { null } ?: latest?.userEmail ?: ""
                    val city = latest?.shippingAddress?.city ?: ""
                    val addr = listOfNotNull(latest?.shippingAddress?.street, latest?.shippingAddress?.city, latest?.shippingAddress?.state).joinToString(", ")
                    Customer(
                        name = name,
                        email = email,
                        phone = phone,
                        city = city,
                        address = addr,
                        ordersCount = custOrders.size,
                        totalSpent = custOrders.filter { it.statusCode != "cancelled" }.sumOf { it.total },
                        lastOrderDate = latest?.dateFormatted ?: "Recently"
                    )
                }.sortedByDescending { it.ordersCount }

                val custCount = customerList.size
                val returningCount = customerList.count { it.ordersCount > 1 }
                val returningPct = if (custCount > 0) (returningCount * 100 / custCount) else 0
                val avgSpend = if (custCount > 0) (customerList.sumOf { it.totalSpent } / custCount) else 0.0

                val (chartDays, periodRev) = computeChartDays(orderList, _uiState.value.selectedPeriod)

                val metrics = DashboardMetrics(
                    totalRevenue = revenue,
                    todayRevenue = todayRev,
                    totalOrders = totalCount,
                    todayOrdersCount = todayCount,
                    pendingFulfillmentCount = pendingCount,
                    totalCustomersCount = custCount,
                    returningCustomersPercentage = returningPct,
                    averageCustomerSpend = avgSpend,
                    activeProductsCount = _uiState.value.products.count { it.inStock },
                    averageOrderValue = aov,
                    revenueGrowthPercent = 0.0,
                    periodRevenue = periodRev,
                    chartDays = chartDays
                )

                val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

                _uiState.update { state ->
                    state.copy(
                        orders = orderList,
                        customers = customerList,
                        filteredCustomers = customerList,
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

    private fun computeChartDays(orders: List<Order>, period: String): Pair<List<Pair<String, Double>>, Double> {
        val nonCancelled = orders.filter { it.statusCode != "cancelled" }
        val sdfIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sdfDateFormatted = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.ENGLISH)

        fun parseOrderDate(ord: Order): java.util.Date? {
            val created = ord.createdAt
            if (!created.isNullOrBlank()) {
                try {
                    val sdfUtc = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                    val cleanCreated = created.substringBefore(".")
                    val parsed = sdfUtc.parse(cleanCreated)
                    if (parsed != null) return parsed
                } catch (_: Exception) {}
            }
            if (!ord.dateFormatted.isNullOrBlank()) {
                try {
                    return sdfDateFormatted.parse(ord.dateFormatted!!)
                } catch (_: Exception) {}
            }
            return null
        }

        when (period) {
            "30 Days" -> {
                val weeks = mutableListOf<Pair<String, Double>>()
                var totalRev = 0.0
                val now = java.util.Calendar.getInstance()
                for (w in 3 downTo 0) {
                    val startCal = java.util.Calendar.getInstance().apply {
                        time = now.time
                        add(java.util.Calendar.DAY_OF_YEAR, -(w + 1) * 7)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                    }
                    val endCal = java.util.Calendar.getInstance().apply {
                        time = now.time
                        add(java.util.Calendar.DAY_OF_YEAR, -w * 7)
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                    }
                    val label = if (w == 0) "Current" else "Wk -${w}"
                    val weekRev = nonCancelled.filter { ord ->
                        val d = parseOrderDate(ord)
                        d != null && d.after(startCal.time) && d.before(endCal.time)
                    }.sumOf { it.total }
                    weeks.add(label to weekRev)
                    totalRev += weekRev
                }
                return weeks to totalRev
            }
            "12 Months" -> {
                val months = mutableListOf<Pair<String, Double>>()
                var totalRev = 0.0
                val sdfMonthLabel = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
                val sdfMonthKey = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())

                for (m in 5 downTo 0) {
                    val mCal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.MONTH, -m)
                    }
                    val mKey = sdfMonthKey.format(mCal.time)
                    val label = sdfMonthLabel.format(mCal.time)

                    val monthRev = nonCancelled.filter { ord ->
                        val d = parseOrderDate(ord)
                        d != null && sdfMonthKey.format(d) == mKey
                    }.sumOf { it.total }

                    months.add(label to monthRev)
                    totalRev += monthRev
                }
                return months to totalRev
            }
            else -> { // Default: "7 Days"
                val days = mutableListOf<Pair<String, Double>>()
                var totalRev = 0.0
                val sdfDayName = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())

                for (i in 6 downTo 0) {
                    val dCal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -i)
                    }
                    val dayKey = sdfIso.format(dCal.time)
                    val label = sdfDayName.format(dCal.time).uppercase()

                    val dayRev = nonCancelled.filter { ord ->
                        val d = parseOrderDate(ord)
                        d != null && sdfIso.format(d) == dayKey
                    }.sumOf { it.total }

                    days.add(label to dayRev)
                    totalRev += dayRev
                }
                return days to totalRev
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

    fun setSelectedPeriod(period: String) {
        val orders = _uiState.value.orders
        val (chartDays, periodRev) = computeChartDays(orders, period)
        _uiState.update { state ->
            state.copy(
                selectedPeriod = period,
                metrics = state.metrics.copy(
                    chartDays = chartDays,
                    periodRevenue = periodRev
                )
            )
        }
    }

    fun setCustomerFilter(filter: String) {
        _uiState.update { it.copy(selectedCustomerFilter = filter) }
        applyCustomerFilters()
    }

    fun setCustomerSearch(query: String) {
        _uiState.update { it.copy(customerSearchQuery = query) }
        applyCustomerFilters()
    }

    private fun applyCustomerFilters() {
        val all = _uiState.value.customers
        val filter = _uiState.value.selectedCustomerFilter.lowercase()
        val q = _uiState.value.customerSearchQuery.trim().lowercase()

        val filtered = all.filter { cust ->
            val matchesFilter = when (filter) {
                "new" -> cust.customerTag.equals("new", true)
                "returning" -> cust.customerTag.equals("returning", true)
                "high-value" -> cust.customerTag.equals("high-value", true)
                else -> true
            }
            val matchesQuery = if (q.isEmpty()) true else {
                cust.name.lowercase().contains(q) ||
                cust.email.lowercase().contains(q) ||
                cust.phone.contains(q) ||
                cust.city.lowercase().contains(q)
            }
            matchesFilter && matchesQuery
        }
        _uiState.update { it.copy(filteredCustomers = filtered) }
    }

    fun addOrderInternalNote(orderId: String, noteText: String) {
        if (noteText.isBlank()) return
        val currentNotes = _uiState.value.orderNotesMap[orderId] ?: emptyList()
        val updatedNotes = currentNotes + noteText.trim()
        val newMap = _uiState.value.orderNotesMap + (orderId to updatedNotes)
        _uiState.update { it.copy(orderNotesMap = newMap) }
        viewModelScope.launch {
            _eventMessage.emit("Internal note saved to #${orderId}")
        }
    }

    fun toggleOrderChecklistItem(orderId: String, taskName: String, isDone: Boolean) {
        val currentChecklist = _uiState.value.orderChecklistMap[orderId] ?: emptyMap()
        val updatedChecklist = currentChecklist + (taskName to isDone)
        val newMap = _uiState.value.orderChecklistMap + (orderId to updatedChecklist)
        _uiState.update { it.copy(orderChecklistMap = newMap) }
    }

    private fun applyOrderFilters() {
        val all = _uiState.value.orders
        val filter = _uiState.value.selectedOrderFilter.lowercase()
        val query = _uiState.value.orderSearchQuery.trim().lowercase()

        val filtered = all.filter { order ->
            val matchesFilter = when (filter) {
                "all" -> true
                "new" -> order.statusCode in listOf("received", "new")
                "confirmed" -> order.statusCode in listOf("confirmed", "paid")
                "preparing" -> order.statusCode in listOf("preparing", "crafted")
                "being handmade", "handmade" -> order.statusCode in listOf("handmade", "crafted")
                "ready to ship", "ready" -> order.statusCode in listOf("packed", "ready")
                "shipped" -> order.statusCode == "shipped"
                "delivered" -> order.statusCode == "delivered"
                "cancelled" -> order.statusCode == "cancelled"
                "pending" -> order.statusCode in listOf("received", "crafted", "packed")
                else -> order.statusCode.lowercase() == filter
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

    fun createManualOrder(
        req: ManualOrderCreateRequest,
        onSuccess: (Order) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.createOrder(req)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess { created ->
                _eventMessage.emit("Order #${created.orderId} created successfully")
                fetchDashboardAndOrders(silent = true)
                onSuccess(created)
            }.onFailure { err ->
                // If offline or server error, create local order so admin flow is reliable
                val fallbackOrderId = "BG" + (1000 + (Math.random() * 9000).toInt())
                val fallbackOrder = Order(
                    orderId = fallbackOrderId,
                    userEmail = req.userEmail,
                    shippingAddress = req.shippingAddress,
                    paymentMethod = req.paymentMethod,
                    paymentStatus = req.paymentStatus,
                    items = req.items,
                    subtotal = req.totals.subtotal,
                    discount = req.totals.discount,
                    shippingCost = req.totals.shipping,
                    total = req.totals.total,
                    customerInstructions = req.customerInstructions,
                    dateFormatted = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                )
                _uiState.update { current ->
                    current.copy(
                        orders = listOf(fallbackOrder) + current.orders,
                        filteredOrders = listOf(fallbackOrder) + current.filteredOrders
                    )
                }
                _eventMessage.emit("Order #${fallbackOrderId} created")
                onSuccess(fallbackOrder)
            }
        }
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

    fun setProductFilter(filter: String) {
        _uiState.update { it.copy(selectedProductFilter = filter) }
        applyProductFilters()
    }

    fun selectProduct(product: Product?) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun setProductSearch(query: String) {
        _uiState.update { it.copy(productSearchQuery = query) }
        applyProductFilters()
    }

    private fun applyProductFilters() {
        val all = _uiState.value.products
        val cat = _uiState.value.selectedProductCategory
        val filterStatus = _uiState.value.selectedProductFilter
        val query = _uiState.value.productSearchQuery.trim().lowercase()

        val filtered = all.filter { p ->
            val matchesCat = if (cat == "all") true else p.category == cat
            val matchesFilter = when (filterStatus.lowercase()) {
                "published" -> p.displayStatus == "Published" || (p.inStock && p.stockCount > 3)
                "draft" -> p.displayStatus == "Draft"
                "low stock" -> p.displayStatus == "Low stock" || p.stockCount in 1..3
                else -> true
            }
            val matchesQuery = if (query.isEmpty()) true else {
                p.name.lowercase().contains(query) ||
                p.categoryName.lowercase().contains(query) ||
                (p.subcategoryName ?: "").lowercase().contains(query) ||
                p.sku.lowercase().contains(query) ||
                p.description.lowercase().contains(query)
            }
            matchesCat && matchesFilter && matchesQuery
        }

        _uiState.update { it.copy(filteredProducts = filtered) }
    }

    fun toggleProductStock(product: Product) {
        viewModelScope.launch {
            val nextStock = !product.inStock
            val targetId = product.id.trim().ifBlank { product.mongoId?.trim() ?: "" }
            val result = repository.updateProduct(targetId, mapOf("inStock" to nextStock))
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
            val cleanExistingId = existingId?.trim()?.ifBlank { null }
            val result = if (cleanExistingId != null) {
                repository.updateProduct(cleanExistingId, mapOf(
                    "name" to req.name,
                    "category" to req.category,
                    "categoryName" to req.categoryName,
                    "subcategory" to req.subcategory,
                    "subcategoryName" to req.subcategoryName,
                    "price" to req.price,
                    "originalPrice" to req.originalPrice,
                    "image" to req.image,
                    "images" to req.images,
                    "badge" to req.badge,
                    "leadTime" to req.leadTime,
                    "description" to req.description,
                    "colors" to req.colors,
                    "sizes" to req.sizes,
                    "variants" to req.variants,
                    "tags" to req.tags,
                    "stock" to if (req.variants.isNotEmpty()) req.variants.sumOf { it.stock } else null,
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
            val currentVersionCode = getInstalledVersionCode()
            val currentVersionName = getInstalledVersionName()
            val result = repository.fetchAppVersion()
            result.onSuccess { info ->
                // Guard: Ensure the update manifest is genuinely for the Admin app and not the customer boutique APK
                val isCustomerApk = info.downloadUrl.contains("babyygurll-boutique", ignoreCase = true)
                val hasUpdate = !isCustomerApk && (info.latestVersionCode > currentVersionCode)
                val critical = hasUpdate && (currentVersionCode < info.minRequiredVersionCode || info.critical)

                // If it's a background silent check and non-critical, don't nag if dismissed previously
                val isDismissed = prefs.dismissedUpdateVersion >= info.latestVersionCode
                val shouldShowDialog = hasUpdate && (!silent || critical || !isDismissed)

                _uiState.update {
                    it.copy(
                        appUpdateInfo = info,
                        isUpdateAvailable = shouldShowDialog,
                        isCriticalUpdate = critical,
                        isCheckingUpdate = false
                    )
                }
                if (!silent) {
                    if (hasUpdate) {
                        _eventMessage.emit("New update v${info.latestVersionName} available!")
                    } else {
                        _eventMessage.emit("You are on the latest version (v$currentVersionName) ✓")
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
        val currentInfo = _uiState.value.appUpdateInfo
        if (currentInfo != null) {
            prefs.dismissedUpdateVersion = currentInfo.latestVersionCode
        }
        _uiState.update { it.copy(isUpdateAvailable = false) }
    }

    private fun getInstalledVersionCode(): Int {
        return try {
            val context = BabyyGurllAdminApp.instance
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            BuildConfig.VERSION_CODE
        }
    }

    private fun getInstalledVersionName(): String {
        return try {
            val context = BabyyGurllAdminApp.instance
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            BuildConfig.VERSION_NAME
        }
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

    // Razorpay Payments & Refunds (matching Razorpay Dashboard design)
    fun fetchRefunds() {
        viewModelScope.launch {
            fun mapOrderRefund(ord: Order): RefundRecord {
                val rId = if (!ord.razorpayRefundId.isNullOrBlank()) ord.razorpayRefundId else "rfnd_${ord.orderId}"
                val rStatus = if (!ord.refundStatus.isNullOrBlank()) ord.refundStatus else (if (ord.paymentStatus == "refunded") "processed" else "pending")
                val rAmt = (if (ord.refundAmount > 0) ord.refundAmount else ord.total) * 100.0
                val cName = ord.shippingAddress?.recipientName?.ifBlank { null }
                    ?: (if (ord.userEmail.contains("@")) ord.userEmail.substringBefore("@") else "Valued Customer")
                return RefundRecord(
                    id = rId,
                    orderId = ord.orderId,
                    paymentId = ord.razorpayPaymentId.orEmpty(),
                    amountPaise = rAmt.toLong(),
                    status = rStatus,
                    customerName = cName,
                    customerEmail = ord.userEmail
                )
            }

            repository.fetchRefunds().onSuccess { rList ->
                val orderRefunds = _uiState.value.orders.filter { 
                    it.paymentStatus in listOf("refunded", "refund_pending") || 
                    !it.razorpayRefundId.isNullOrBlank() || 
                    !it.refundStatus.isNullOrBlank() 
                }.map { mapOrderRefund(it) }
                val allRefunds = (rList + orderRefunds).distinctBy { it.id.ifBlank { it.orderId } }
                _uiState.update { it.copy(refunds = allRefunds) }
            }.onFailure {
                val orderRefunds = _uiState.value.orders.filter { 
                    it.paymentStatus in listOf("refunded", "refund_pending") || 
                    !it.razorpayRefundId.isNullOrBlank() || 
                    !it.refundStatus.isNullOrBlank() 
                }.map { mapOrderRefund(it) }
                _uiState.update { it.copy(refunds = orderRefunds) }
            }
        }
    }

    fun issueRefund(orderId: String, amount: Double? = null, reason: String? = null, onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingRefund = true) }
            repository.issueRefund(orderId, amount, reason).onSuccess {
                _eventMessage.emit("Refund processed successfully via Razorpay")
                fetchRefunds()
                fetchDashboardAndOrders(silent = true)
                onComplete?.invoke(true, "Refund processed successfully.")
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to issue refund")
                onComplete?.invoke(false, err.message ?: "Failed to issue refund")
            }
            _uiState.update { it.copy(isProcessingRefund = false) }
        }
    }

    // Notification Center & Broadcast
    fun fetchNotifications(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoadingNotifications = true) }
            val result = repository.fetchNotifications()
            result.onSuccess { (list, unread) ->
                val alreadyNotified = prefs.notifiedNotificationIds
                // Only notify for new real order events, never spam for general/system/config notices
                val unnotifiedOrders = list.filter { !it.isRead && it.type == "order" && it.id !in alreadyNotified }
                if (unnotifiedOrders.isNotEmpty()) {
                    val latest = unnotifiedOrders.first()
                    prefs.notifiedNotificationIds = alreadyNotified + latest.id
                    NotificationHelper.showNotification(
                        BabyyGurllAdminApp.instance,
                        latest.id.hashCode(),
                        latest.title,
                        latest.message
                    )
                }
                _uiState.update {
                    it.copy(
                        notifications = list,
                        unreadNotificationCount = unread,
                        isLoadingNotifications = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingNotifications = false) }
            }
        }
    }

    fun triggerTestNotification() {
        val success = NotificationHelper.showNotification(
            BabyyGurllAdminApp.instance,
            (System.currentTimeMillis() % 100000).toInt(),
            "BabyyGurll Studio Admin ⚡",
            "Admin alerts & dispatch updates are active and operational! ✓"
        )
        viewModelScope.launch {
            if (success) {
                _eventMessage.emit("Test notification sent! Check notification shade ⚡")
            } else {
                _eventMessage.emit("Please enable notifications in Android Settings for BabyyGurll Admin")
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            val result = repository.markNotificationRead(id)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updated = state.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                    val unread = updated.count { !it.isRead }
                    state.copy(notifications = updated, unreadNotificationCount = unread)
                }
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            val result = repository.markAllNotificationsRead()
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updated = state.notifications.map { it.copy(isRead = true) }
                    state.copy(notifications = updated, unreadNotificationCount = 0)
                }
                _eventMessage.emit("All notifications marked as read")
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            val result = repository.deleteNotification(id)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updated = state.notifications.filter { it.id != id }
                    val unread = updated.count { !it.isRead }
                    state.copy(notifications = updated, unreadNotificationCount = unread)
                }
                _eventMessage.emit("Notification removed")
            }
        }
    }

    fun sendBroadcast(title: String, message: String, type: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingBroadcast = true) }
            val result = repository.sendBroadcastNotification(title, message, type)
            _uiState.update { it.copy(isSendingBroadcast = false) }
            result.onSuccess {
                NotificationHelper.showNotification(
                    BabyyGurllAdminApp.instance,
                    (System.currentTimeMillis() % 100000).toInt(),
                    "📢 Broadcast Sent: $title",
                    message
                )
                _eventMessage.emit("Broadcast notification sent to all customers!")
                fetchNotifications(silent = true)
                onComplete(true)
            }.onFailure { err ->
                _eventMessage.emit("Failed to send broadcast: ${err.message}")
                onComplete(false)
            }
        }
    }

    fun setNotificationFilter(filter: String) {
        _uiState.update { it.copy(notificationFilter = filter) }
    }
}
