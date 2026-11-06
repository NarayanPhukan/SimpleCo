package com.babyygurll.store.data.repository

import android.content.Context
import com.babyygurll.store.data.model.CartItem
import com.babyygurll.store.data.model.Coupon
import com.babyygurll.store.data.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("babyygurll_cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val KEY_CART = "cart_items_json"
    private val KEY_COUPON = "applied_coupon_json"
    private val KEY_WISHLIST = "wishlist_ids_json"

    private val _cartItems = MutableStateFlow<List<CartItem>>(loadCart())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<Coupon?>(loadCoupon())
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(loadWishlist())
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private fun loadCart(): List<CartItem> {
        val json = prefs.getString(KEY_CART, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCart(items: List<CartItem>) {
        _cartItems.value = items
        prefs.edit().putString(KEY_CART, gson.toJson(items)).apply()
    }

    private fun loadCoupon(): Coupon? {
        val json = prefs.getString(KEY_COUPON, null) ?: return null
        return try {
            gson.fromJson(json, Coupon::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadWishlist(): Set<String> {
        val json = prefs.getString(KEY_WISHLIST, null) ?: return emptySet()
        return try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(json, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun addToCart(
        product: Product,
        quantity: Int = 1,
        customNote: String = "",
        selectedVariant: com.babyygurll.store.data.model.ProductVariant? = null,
        selectedColor: String? = null
    ) {
        val current = _cartItems.value.toMutableList()
        val key = "${product.effectiveId}_${selectedVariant?.id ?: selectedVariant?.name ?: selectedColor ?: "default"}"
        val existingIndex = current.indexOfFirst { it.cartKey == key }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            val updatedQty = existing.quantity + quantity
            val updatedNote = if (customNote.isNotBlank()) customNote else existing.customNote
            current[existingIndex] = existing.copy(quantity = updatedQty, customNote = updatedNote)
        } else {
            current.add(
                CartItem(
                    product = product,
                    quantity = quantity,
                    customNote = customNote,
                    selectedVariant = selectedVariant,
                    selectedColor = selectedColor ?: selectedVariant?.color
                )
            )
        }
        saveCart(current)
    }

    fun updateQuantity(cartKey: String, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.cartKey == cartKey || it.product.effectiveId == cartKey }
        if (index >= 0) {
            if (quantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = quantity)
            }
            saveCart(current)
        }
    }

    fun removeFromCart(cartKey: String) {
        val current = _cartItems.value.filter { it.cartKey != cartKey && it.product.effectiveId != cartKey }
        saveCart(current)
    }

    fun clearCart() {
        saveCart(emptyList())
        removeCoupon()
    }

    fun applyCoupon(coupon: Coupon) {
        _appliedCoupon.value = coupon
        prefs.edit().putString(KEY_COUPON, gson.toJson(coupon)).apply()
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        prefs.edit().remove(KEY_COUPON).apply()
    }

    fun toggleWishlist(productId: String): Boolean {
        val current = _wishlistIds.value.toMutableSet()
        val added = if (current.contains(productId)) {
            current.remove(productId)
            false
        } else {
            current.add(productId)
            true
        }
        _wishlistIds.value = current
        prefs.edit().putString(KEY_WISHLIST, gson.toJson(current)).apply()
        return added
    }

    fun isInWishlist(productId: String): Boolean {
        return _wishlistIds.value.contains(productId)
    }
}

