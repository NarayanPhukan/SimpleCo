package com.babyygurll.admin.data.api

import com.babyygurll.admin.BabyyGurllAdminApp
import com.babyygurll.admin.data.diagnostics.ErrorLogManager
import com.babyygurll.admin.data.repository.PreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkModule {

    private var currentBaseUrl: String? = null
    private var cachedApiService: AdminApiService? = null

    private val preferencesManager by lazy {
        PreferencesManager(BabyyGurllAdminApp.instance)
    }

    private val errorDetectionInterceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            val response = chain.proceed(request)
            if (!response.isSuccessful) {
                ErrorLogManager.logWarning(
                    tag = "HTTP_${response.code}",
                    message = "Server returned status ${response.code} for ${request.method} ${request.url.encodedPath}"
                )
            }
            response
        } catch (e: IOException) {
            val endpoint = request.url.toString()
            val hint = if (endpoint.contains("10.0.2.2")) {
                "Make sure your Node/Express backend is running on port 5000."
            } else {
                "Check server reachability or Wi-Fi connection."
            }
            ErrorLogManager.logError(
                tag = "NETWORK_ERROR",
                message = "Failed to connect to $endpoint. $hint Reason: ${e.message}",
                throwable = e
            )
            throw e
        }
    }

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(errorDetectionInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun getApiService(): AdminApiService {
        val targetUrl = preferencesManager.serverUrl

        if (cachedApiService == null || currentBaseUrl != targetUrl) {
            currentBaseUrl = targetUrl
            val retrofit = Retrofit.Builder()
                .baseUrl(targetUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            cachedApiService = retrofit.create(AdminApiService::class.java)
        }

        return cachedApiService!!
    }

    fun invalidateClient() {
        cachedApiService = null
        currentBaseUrl = null
    }
}
