package com.babyygurll.admin.data.repository

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "babyygurll_admin_prefs"
        private const val KEY_SERVER_URL = "key_server_url"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_ADMIN_PIN = "key_admin_pin"
        private const val KEY_ADMIN_EMAIL = "key_admin_email"
        private const val KEY_AUTH_PROVIDER = "key_auth_provider"
        private const val DEFAULT_SERVER_URL = "http://10.0.2.2:5000/" // Android Emulator localhost
    }

    var serverUrl: String
        get() {
            var url = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
            if (!url.endsWith("/")) url += "/"
            return url
        }
        set(value) {
            var formatted = value.trim()
            if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                formatted = "http://$formatted"
            }
            if (!formatted.endsWith("/")) formatted += "/"
            prefs.edit().putString(KEY_SERVER_URL, formatted).apply()
        }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var savedAdminPin: String
        get() = prefs.getString(KEY_ADMIN_PIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ADMIN_PIN, value).apply()

    var adminEmail: String
        get() = prefs.getString(KEY_ADMIN_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ADMIN_EMAIL, value).apply()

    var authProvider: String
        get() = prefs.getString(KEY_AUTH_PROVIDER, "firebase") ?: "firebase"
        set(value) = prefs.edit().putString(KEY_AUTH_PROVIDER, value).apply()

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_ADMIN_PIN)
            .apply()
    }
}
