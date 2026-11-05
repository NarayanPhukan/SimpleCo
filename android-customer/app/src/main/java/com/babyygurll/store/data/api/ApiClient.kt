package com.babyygurll.store.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PREFS_NAME = "babyygurll_customer_prefs"
    private const val KEY_SERVER_URL = "server_url"
    const val DEFAULT_BASE_URL = "https://simpleco.onrender.com/api/"

    private var currentUrl: String = DEFAULT_BASE_URL
    private var apiService: StoreApiService? = null

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setServerUrl(context: Context, newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SERVER_URL, formatted)
            .apply()
        currentUrl = formatted
        apiService = null
    }

    fun getService(context: Context): StoreApiService {
        val url = getServerUrl(context)
        if (apiService == null || currentUrl != url) {
            currentUrl = url

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(StoreApiService::class.java)
        }
        return apiService!!
    }
}

