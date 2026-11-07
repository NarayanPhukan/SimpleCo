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
        const val DEFAULT_SERVER_URL = "https://simpleco.onrender.com/" // 24/7 Render Cloud Backend
    }

    var serverUrl: String
        get() {
            val saved = prefs.getString(KEY_SERVER_URL, null)
            // If unset, empty, or pointing to local emulator / localhost from earlier builds, force Render cloud URL
            if (saved.isNullOrBlank() || saved.contains("10.0.2.2") || saved.contains("localhost")) {
                prefs.edit().putString(KEY_SERVER_URL, DEFAULT_SERVER_URL).apply()
                return DEFAULT_SERVER_URL
            }
            var url = saved.trim()
            if (!url.endsWith("/")) url += "/"
            return url
        }
        set(value) {
            var formatted = value.trim()
            if (formatted.isBlank() || formatted.contains("10.0.2.2") || formatted.contains("localhost")) {
                formatted = DEFAULT_SERVER_URL
            } else {
                if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                    formatted = "https://$formatted"
                }
                if (!formatted.endsWith("/")) formatted += "/"
            }
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

    var dismissedUpdateVersion: Int
        get() = prefs.getInt("key_dismissed_update_version", 0)
        set(value) = prefs.edit().putInt("key_dismissed_update_version", value).apply()

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_ADMIN_PIN)
            .apply()
    }
}
