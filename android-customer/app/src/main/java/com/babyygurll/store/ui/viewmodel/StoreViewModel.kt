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

    // Derived filtered and sorted products
    val products: StateFlow<List<Product>> = combine(
        _rawProducts,
        _selectedCategory,
        _searchQuery,
        _sortOption
    ) { raw, cat, query, sort ->
        var list = raw
        if (cat != "all") {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
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
        list.sumOf { it.product.price * it.quantity }
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
        loadInitialData()
    }

    fun loadInitialData() {
        fetchProducts()
        fetchSettings()
        fetchCoupons()
        checkPendingCrashReport()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = storeRepo.getProducts()
            res.onSuccess { list ->
                _rawProducts.value = list
            }.onFailure { err ->
                ErrorLogManager.logWarning("CATALOG_FETCH", "Failed to fetch products: ${err.message}")
                _eventMessage.emit(err.message ?: "Could not load products")
            }
            _isLoading.value = false
        }
    }

    fun fetchSettings() {
        viewModelScope.launch {
            val res = storeRepo.getStoreSettings()
            res.onSuccess {
                _storeSetting.value = it
                checkForUpdates(it)
            }
        }
    }

    private fun checkForUpdates(setting: StoreSetting) {
        val versionInfo = setting.customerAppVersion ?: setting.appVersion ?: return
        val isCustomerApk = versionInfo.downloadUrl.contains("android-customer") || setting.customerAppVersion != null
        if (isCustomerApk && versionInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
            _availableUpdate.value = versionInfo
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
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: String) {
        _sortOption.value = sort
    }

    // Cart Actions
    fun addToCart(product: Product, quantity: Int = 1, customNote: String = "") {
        cartRepo.addToCart(product, quantity, customNote)
        viewModelScope.launch {
            _eventMessage.emit("Added ${product.name} to cart ✓")
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        cartRepo.updateQuantity(productId, quantity)
    }

    fun removeFromCart(productId: String) {
        cartRepo.removeFromCart(productId)
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
                OrderItem(
                    id = it.product.effectiveId,
                    name = it.product.name,
                    price = it.product.price,
                    quantity = it.quantity,
                    image = it.product.image,
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

    fun fetchOrderDetail(orderId: String) {
        viewModelScope.launch {
            val res = orderRepo.getOrderById(orderId)
            res.onSuccess {
                _activeOrderDetail.value = it
            }
        }
    }
}

