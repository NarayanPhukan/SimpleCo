package com.babyygurll.store.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babyygurll.store.BuildConfig
import com.babyygurll.store.data.diagnostics.ErrorLogManager
import com.babyygurll.store.data.model.*
import com.babyygurll.store.data.repository.*
import com.babyygurll.store.data.update.CustomerUpdateDownloader
import com.babyygurll.store.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val storeRepo = StoreRepository(application)
    private val cartRepo = CartRepository(application)
    private val orderRepo = OrderRepository(application)
    private val authRepo = AuthRepository(application)

    // Catalog State
    private val _rawProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow("all")
    val selectedSubcategory = _selectedSubcategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow("featured") // "featured", "price-low", "price-high", "rating"
    val sortOption = _sortOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _storeSetting = MutableStateFlow(StoreSetting())
    val storeSetting = _storeSetting.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons = _coupons.asStateFlow()

    // Auto-Update State
    private val _availableUpdate = MutableStateFlow<AppVersionInfo?>(null)
    val availableUpdate = _availableUpdate.asStateFlow()
    val updateDownloadState = CustomerUpdateDownloader.downloadState

    // Auto-Detected Error Report State
    private val _pendingErrorReport = MutableStateFlow<String?>(null)
    val pendingErrorReport = _pendingErrorReport.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow<List<StoreNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount = _unreadNotificationCount.asStateFlow()

    private val _isLoadingNotifications = MutableStateFlow(false)
    val isLoadingNotifications = _isLoadingNotifications.asStateFlow()

    // Derived filtered and sorted products
    val products: StateFlow<List<Product>> = combine(
        _rawProducts,
        _selectedCategory,
        _selectedSubcategory,
        _searchQuery,
        _sortOption
    ) { raw, cat, subcat, query, sort ->
        var list = raw
        if (cat != "all") {
            list = list.filter { it.matchesCategory(cat) }
        }
        if (subcat != "all") {
            list = list.filter { it.matchesSubcategory(subcat) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.categoryName.lowercase().contains(q) ||
                (it.subcategoryName ?: "").lowercase().contains(q)
            }
        }
        when (sort) {
            "price-low" -> list.sortedBy { it.price }
            "price-high" -> list.sortedByDescending { it.price }
            "rating" -> list.sortedByDescending { it.rating }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart & Wishlist State (delegated to repo)
    val cartItems = cartRepo.cartItems
    val appliedCoupon = cartRepo.appliedCoupon
    val wishlistIds = cartRepo.wishlistIds

    // Cart Totals
    val cartSubtotal: StateFlow<Double> = cartItems.map { list ->
        list.sumOf { it.unitPrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDiscount: StateFlow<Double> = combine(cartSubtotal, appliedCoupon) { subtotal, coupon ->
        if (coupon == null || subtotal <= 0.0) return@combine 0.0
        if (coupon.minOrderValue != null && subtotal < coupon.minOrderValue) return@combine 0.0
        var disc = 0.0
        if (coupon.discountPercent != null && coupon.discountPercent > 0) {
            disc += (subtotal * coupon.discountPercent / 100.0)
        }
        if (coupon.discountFlat != null && coupon.discountFlat > 0) {
            disc += coupon.discountFlat
        }
        minOf(disc, subtotal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartShipping: StateFlow<Double> = MutableStateFlow(0.0).asStateFlow() // Free delivery across India

    val cartGrandTotal: StateFlow<Double> = combine(cartSubtotal, cartDiscount, cartShipping) { sub, disc, ship ->
        maxOf(0.0, sub - disc + ship)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User & Auth State
    val currentUser = authRepo.currentUser
    val savedAddress: ShippingAddress get() = authRepo.getSavedAddress()

    // Orders State
    private val _customerOrders = MutableStateFlow<List<Order>>(emptyList())
    val customerOrders = _customerOrders.asStateFlow()

    private val _activeOrderDetail = MutableStateFlow<Order?>(null)
    val activeOrderDetail = _activeOrderDetail.asStateFlow()

    // Single Toast/Event flow
    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage = _eventMessage.asSharedFlow()

    init {
        // Hydrate instantly from local cache so UI is never blank while cold-booting
        val cached = storeRepo.getCachedProducts()
        if (cached.isNotEmpty()) {
            _rawProducts.value = cached
        }
        loadInitialData()
        // Re-fetch notifications whenever user auth changes
        viewModelScope.launch {
            currentUser.collectLatest {
                fetchNotifications(silent = true)
            }
        }
        // Periodic background notification check
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                fetchNotifications(silent = true)
            }
        }
    }

    fun loadInitialData() {
        fetchProducts()
        fetchSettings()
        fetchCoupons()
        fetchNotifications()
        checkPendingCrashReport()
    }

    fun fetchProducts(retryCount: Int = 0) {
        viewModelScope.launch {
            if (retryCount == 0 && _rawProducts.value.isEmpty()) {
                _isLoading.value = true
            }
            val res = storeRepo.getProducts()
            res.onSuccess { list ->
                if (list.isNotEmpty()) {
                    _rawProducts.value = list
                    _isLoading.value = false
                } else if (retryCount < 3) {
                    // Retry in 2.5s in case backend database was still establishing connections
                    kotlinx.coroutines.delay(2500)
                    fetchProducts(retryCount + 1)
                } else {
                    _rawProducts.value = list
                    _isLoading.value = false
                }
            }.onFailure { err ->
                ErrorLogManager.logWarning("CATALOG_FETCH", "Failed to fetch products: ${err.message}")
                if (retryCount < 3) {
                    // Render cloud cold-boot auto-retry! Wait 3.5s and retry
                    kotlinx.coroutines.delay(3500)
                    fetchProducts(retryCount + 1)
                } else {
                    if (_rawProducts.value.isEmpty()) {
                        _eventMessage.emit(err.message ?: "Could not load products")
                    }
                    _isLoading.value = false
                }
            }
        }
    }

    fun fetchSettings() {
        viewModelScope.launch {
            val res = storeRepo.getStoreSettings()
            res.onSuccess {
                _storeSetting.value = it
                checkForUpdates(it)
            }
            // Also query dedicated customer app version endpoint
            storeRepo.getCustomerAppVersion().onSuccess { versionInfo ->
                if (versionInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
                    _availableUpdate.value = versionInfo
                }
            }
        }
    }

    private fun checkForUpdates(setting: StoreSetting) {
        val versionInfo = setting.customerAppVersion
            ?: (if (setting.appVersion?.downloadUrl?.contains("android-customer") == true) setting.appVersion else null)
            ?: return
        if (versionInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
            _availableUpdate.value = versionInfo
        }
    }

    fun checkForUpdatesManual() {
        viewModelScope.launch {
            _isLoading.value = true
            var foundUpdate: AppVersionInfo? = null

            val directRes = storeRepo.getCustomerAppVersion()
            directRes.onSuccess { versionInfo ->
                if (versionInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
                    foundUpdate = versionInfo
                }
            }

            if (foundUpdate == null) {
                storeRepo.getStoreSettings().onSuccess { setting ->
                    _storeSetting.value = setting
                    val versionInfo = setting.customerAppVersion
                        ?: (if (setting.appVersion?.downloadUrl?.contains("android-customer") == true) setting.appVersion else null)
                    if (versionInfo != null && versionInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
                        foundUpdate = versionInfo
                    }
                }
            }

            if (foundUpdate != null) {
                _availableUpdate.value = foundUpdate
            } else {
                _eventMessage.emit("You're already on the latest version (v${BuildConfig.VERSION_NAME})! 🌸")
            }
            _isLoading.value = false
        }
    }

    fun startAppUpdate(context: Context) {
        val update = _availableUpdate.value ?: return
        viewModelScope.launch {
            CustomerUpdateDownloader.startDownload(
                context = context,
                rawUrl = update.downloadUrl,
                versionName = update.latestVersionName
            )
        }
    }

    fun dismissUpdateDialog() {
        _availableUpdate.value = null
        CustomerUpdateDownloader.resetState()
    }

    private fun checkPendingCrashReport() {
        val report = ErrorLogManager.getPendingCrashReport(getApplication())
        if (!report.isNullOrBlank()) {
            _pendingErrorReport.value = report
        }
    }

    fun dismissErrorReport() {
        _pendingErrorReport.value = null
        ErrorLogManager.dismissPendingCrashReport(getApplication())
    }

    fun triggerManualErrorReport() {
        _pendingErrorReport.value = ErrorLogManager.generateDiagnosticsReport()
    }

    fun fetchCoupons() {
        viewModelScope.launch {
            val res = storeRepo.getCoupons()
            res.onSuccess { _coupons.value = it }
        }
    }

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
        _selectedSubcategory.value = "all"
    }

    fun selectSubcategory(subcat: String) {
        _selectedSubcategory.value = subcat
    }

    fun clearFilters() {
        _selectedCategory.value = "all"
        _selectedSubcategory.value = "all"
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: String) {
        _sortOption.value = sort
    }

    // Cart Actions
    fun addToCart(
        product: Product,
        quantity: Int = 1,
        customNote: String = "",
        selectedVariant: com.babyygurll.store.data.model.ProductVariant? = null,
        selectedColor: String? = null
    ) {
        cartRepo.addToCart(product, quantity, customNote, selectedVariant, selectedColor)
        viewModelScope.launch {
            val variantLabel = selectedVariant?.name ?: selectedColor
            val msg = if (variantLabel != null) "Added ${product.name} ($variantLabel) to cart ✓" else "Added ${product.name} to cart ✓"
            _eventMessage.emit(msg)
        }
    }

    fun updateCartQuantity(cartKey: String, quantity: Int) {
        cartRepo.updateQuantity(cartKey, quantity)
    }

    fun removeFromCart(cartKey: String) {
        cartRepo.removeFromCart(cartKey)
    }

    fun clearCart() {
        cartRepo.clearCart()
    }

    fun applyCoupon(code: String) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) {
            viewModelScope.launch { _eventMessage.emit("Please enter a coupon code") }
            return
        }
        viewModelScope.launch {
            val res = storeRepo.validateCoupon(trimmed, cartSubtotal.value)
            res.onSuccess { coupon ->
                cartRepo.applyCoupon(coupon)
                _eventMessage.emit("Coupon ${coupon.code} applied! ✓")
            }.onFailure { err ->
                // Local fallback for default store coupons if offline
                when (trimmed) {
                    "BABYYGURLL" -> {
                        val c = Coupon(code = "BABYYGURLL", discountPercent = 15.0, description = "15% Off Handcrafted Pieces")
                        cartRepo.applyCoupon(c)
                        _eventMessage.emit("Coupon BABYYGURLL applied (15% OFF) ✓")
                    }
                    "CUTIE100" -> {
                        val c = Coupon(code = "CUTIE100", discountFlat = 100.0, minOrderValue = 599.0, description = "₹100 Off orders over ₹599")
                        cartRepo.applyCoupon(c)
                        _eventMessage.emit("Coupon CUTIE100 applied (₹100 OFF) ✓")
                    }
                    "FREESHIP" -> {
                        val c = Coupon(code = "FREESHIP", freeShipping = true, description = "Free Express Delivery")
                        cartRepo.applyCoupon(c)
                        _eventMessage.emit("Coupon FREESHIP applied ✓")
                    }
                    else -> _eventMessage.emit(err.message ?: "Invalid coupon code")
                }
            }
        }
    }

    fun removeCoupon() {
        cartRepo.removeCoupon()
        viewModelScope.launch {
            _eventMessage.emit("Coupon removed")
        }
    }

    // Wishlist Actions
    fun toggleWishlist(product: Product) {
        val added = cartRepo.toggleWishlist(product.effectiveId)
        viewModelScope.launch {
            if (added) {
                _eventMessage.emit("Saved to wishlist ♥")
            } else {
                _eventMessage.emit("Removed from wishlist")
            }
        }
    }

    // User / Auth Actions
    fun login(email: String, pass: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepo.login(email, pass)
            _isLoading.value = false
            res.onSuccess { user ->
                _eventMessage.emit("Welcome back, ${user.name}! 🌸")
                onSuccess()
            }.onFailure { err ->
                val msg = err.message ?: "Login failed"
                _eventMessage.emit(msg)
                onError(msg)
            }
        }
    }

    fun register(name: String, email: String, pass: String, phone: String?, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepo.register(name, email, pass, phone)
            _isLoading.value = false
            res.onSuccess { user ->
                _eventMessage.emit("Account created for ${user.name}! 🌸")
                onSuccess()
            }.onFailure { err ->
                val msg = err.message ?: "Registration failed"
                _eventMessage.emit(msg)
                onError(msg)
            }
        }
    }

    fun getLastGeneratedOtp(): String? = authRepo.getLastGeneratedOtp()

    fun sendOtp(email: String, onSuccess: (String) -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepo.sendOtp(email)
            _isLoading.value = false
            res.onSuccess { msg ->
                _eventMessage.emit(msg)
                onSuccess(msg)
            }.onFailure { err ->
                val msg = err.message ?: "Failed to send code"
                _eventMessage.emit(msg)
                onError(msg)
            }
        }
    }

    fun verifyOtp(email: String, otp: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepo.verifyOtp(email, otp)
            _isLoading.value = false
            res.onSuccess {
                _eventMessage.emit("Code verified successfully! 🌸")
                onSuccess()
            }.onFailure { err ->
                val msg = err.message ?: "Invalid code"
                _eventMessage.emit(msg)
                onError(msg)
            }
        }
    }

    fun resetPasswordWithOtp(email: String, otp: String, newPass: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepo.resetPassword(email, otp, newPass)
            _isLoading.value = false
            res.onSuccess { user ->
                _eventMessage.emit("Password reset successfully! Welcome, ${user.name} 🌸")
                onSuccess()
            }.onFailure { err ->
                val msg = err.message ?: "Failed to reset password"
                _eventMessage.emit(msg)
                onError(msg)
            }
        }
    }

    fun logout() {
        authRepo.logout()
        viewModelScope.launch {
            _eventMessage.emit("Signed out")
        }
    }

    fun saveShippingAddress(addr: ShippingAddress) {
        authRepo.saveAddress(addr)
    }

    // Orders Actions
    fun placeOrder(
        shippingAddress: ShippingAddress,
        paymentId: String = "",
        razorpayOrderId: String = "",
        onSuccess: (order: Order) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val userEmail = currentUser.value?.email ?: "guest@babyygurll.in"
            val items = cartItems.value.map {
                val variantDesc = it.selectedVariant?.name ?: it.selectedColor
                OrderItem(
                    id = it.selectedVariant?.id ?: it.product.effectiveId,
                    name = if (variantDesc != null) "${it.product.name} ($variantDesc)" else it.product.name,
                    color = it.selectedColor ?: it.selectedVariant?.color ?: "Default",
                    price = it.unitPrice,
                    quantity = it.quantity,
                    image = it.selectedVariant?.image?.ifBlank { null } ?: it.product.image,
                    customNote = it.customNote
                )
            }
            val totals = OrderTotals(
                subtotal = cartSubtotal.value,
                shipping = cartShipping.value,
                discount = cartDiscount.value,
                total = cartGrandTotal.value
            )

            val req = OrderCreateRequest(
                userEmail = userEmail,
                shippingAddress = shippingAddress,
                paymentMethod = if (paymentId.isNotBlank()) "Razorpay Online ($paymentId)" else "Razorpay Online",
                paymentStatus = "paid",
                razorpayPaymentId = paymentId,
                razorpayOrderId = razorpayOrderId,
                totals = totals,
                items = items
            )

            val res = orderRepo.createOrder(req)
            _isLoading.value = false
            res.onSuccess { order ->
                clearCart()
                saveShippingAddress(shippingAddress)
                _eventMessage.emit("Order #${order.orderId} confirmed! 🌸")
                onSuccess(order)
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to place order")
            }
        }
    }

    suspend fun createRazorpayOrder(amountInRupees: Double): Result<Map<String, Any>> {
        return orderRepo.createRazorpayOrder(amountInRupees)
    }

    suspend fun getRazorpayKey(): String {
        return orderRepo.getRazorpayKey()
    }

    fun fetchCustomerOrders(email: String? = null) {
        val targetEmail = email ?: currentUser.value?.email ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = orderRepo.getCustomerOrders(targetEmail)
            _isLoading.value = false
            res.onSuccess {
                _customerOrders.value = it
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Failed to load orders")
            }
        }
    }

    fun cancelOrder(orderId: String, reason: String = "Changed mind", onResult: (Order?) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = orderRepo.cancelOrder(orderId, reason)
            _isLoading.value = false
            res.onSuccess { updatedOrder ->
                _customerOrders.value = _customerOrders.value.map {
                    if (it.orderId == updatedOrder.orderId) updatedOrder else it
                }
                _eventMessage.emit("Order #${updatedOrder.orderId} has been cancelled ✓")
                onResult(updatedOrder)
            }.onFailure { err ->
                _eventMessage.emit(err.message ?: "Could not cancel order")
                onResult(null)
            }
        }
    }

    fun fetchOrderDetail(orderId: String) {
        viewModelScope.launch {
            val res = orderRepo.getOrderById(orderId)
            res.onSuccess {
                _activeOrderDetail.value = it
            }
        }
    }

    // Customer Notification Actions
    fun fetchNotifications(silent: Boolean = false) {
        viewModelScope.launch {
            val email = currentUser.value?.email ?: "guest@babyygurll.in"
            if (!silent) _isLoadingNotifications.value = true
            val prevIds = _notifications.value.map { it.id }.toSet()
            val result = storeRepo.getNotifications(email)
            result.onSuccess { (list, unread) ->
                val newUnread = list.filter { !it.isRead && it.id !in prevIds }
                if (newUnread.isNotEmpty() && prevIds.isNotEmpty()) {
                    val latest = newUnread.first()
                    NotificationHelper.showNotification(
                        getApplication(),
                        latest.id.hashCode(),
                        latest.title,
                        latest.message
                    )
                }
                _notifications.value = list
                _unreadNotificationCount.value = unread
                _isLoadingNotifications.value = false
            }.onFailure {
                _isLoadingNotifications.value = false
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            val result = storeRepo.markNotificationRead(id)
            if (result.isSuccess) {
                val updated = _notifications.value.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
                _notifications.value = updated
                _unreadNotificationCount.value = updated.count { !it.isRead }
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            val email = currentUser.value?.email ?: "guest@babyygurll.in"
            val result = storeRepo.markAllNotificationsRead(email)
            if (result.isSuccess) {
                val updated = _notifications.value.map { it.copy(isRead = true) }
                _notifications.value = updated
                _unreadNotificationCount.value = 0
                _eventMessage.emit("All notifications marked as read")
            }
        }
    }
}

