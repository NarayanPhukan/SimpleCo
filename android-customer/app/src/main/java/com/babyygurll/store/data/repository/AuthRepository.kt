package com.babyygurll.store.data.repository

import android.content.Context
import com.babyygurll.store.data.api.ApiClient
import com.babyygurll.store.data.model.AuthResponse
import com.babyygurll.store.data.model.ShippingAddress
import com.babyygurll.store.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("babyygurll_user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val api get() = ApiClient.getService(context)

    private val KEY_USER = "current_user_json"
    private val KEY_TOKEN = "auth_jwt_token"
    private val KEY_SAVED_ADDRESS = "saved_shipping_address"

    private val _currentUser = MutableStateFlow<User?>(loadUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private fun loadUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractErrorMessage(response: retrofit2.Response<AuthResponse>, fallback: String): String {
        val bodyMsg = response.body()?.message
        if (!bodyMsg.isNullOrBlank()) return bodyMsg

        val errString = try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            null
        }

        if (!errString.isNullOrBlank()) {
            try {
                val parsed = gson.fromJson(errString, AuthResponse::class.java)
                if (!parsed.message.isNullOrBlank()) {
                    return parsed.message
                }
            } catch (e: Exception) {
                try {
                    val map = gson.fromJson(errString, Map::class.java)
                    val msg = map["message"]?.toString()
                    if (!msg.isNullOrBlank()) return msg
                } catch (e2: Exception) {
                    return errString.take(120)
                }
            }
        }
        return fallback
    }

    suspend fun login(email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(mapOf("email" to email.trim(), "password" to pass))
            val body = response.body()
            val user = body?.effectiveUser
            if (response.isSuccessful && user != null) {
                saveUserSession(user, body?.token ?: "")
                Result.success(user)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Login failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, pass: String, phone: String?): Result<User> = withContext(Dispatchers.IO) {
        try {
            val map = mutableMapOf(
                "name" to name.trim(),
                "email" to email.trim(),
                "password" to pass
            )
            phone?.let { if (it.isNotBlank()) map["phone"] = it.trim() }

            val response = api.register(map)
            val body = response.body()
            val user = body?.effectiveUser
            if (response.isSuccessful && user != null) {
                saveUserSession(user, body?.token ?: "")
                Result.success(user)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Registration failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUserSession(user: User, token: String) {
        _currentUser.value = user
        prefs.edit()
            .putString(KEY_USER, gson.toJson(user))
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().remove(KEY_USER).remove(KEY_TOKEN).apply()
    }

    fun getSavedAddress(): ShippingAddress {
        val json = prefs.getString(KEY_SAVED_ADDRESS, null) ?: return ShippingAddress()
        return try {
            gson.fromJson(json, ShippingAddress::class.java) ?: ShippingAddress()
        } catch (e: Exception) {
            ShippingAddress()
        }
    }

    fun saveAddress(addr: ShippingAddress) {
        prefs.edit().putString(KEY_SAVED_ADDRESS, gson.toJson(addr)).apply()
    }
}

