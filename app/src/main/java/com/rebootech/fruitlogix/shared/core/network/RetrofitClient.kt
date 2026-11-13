package com.rebootech.fruitlogix.shared.core.network

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

enum class BackendSyncStatus(val label: String) {
    CONNECTING("Sincronizando con Render API..."),
    ONLINE("Conectado a Render API (En vivo)"),
    OFFLINE_FALLBACK("Modo Local Activo (Render en reposo / Offline)")
}

object RetrofitClient {

    const val BASE_URL = "https://rebootech-fruitlogix-api.onrender.com/api/v1/"

    @Volatile
    var jwtToken: String? = null

    private val _syncStatus = MutableStateFlow(BackendSyncStatus.CONNECTING)
    val syncStatus: StateFlow<BackendSyncStatus> = _syncStatus.asStateFlow()

    fun updateSyncStatus(status: BackendSyncStatus) {
        _syncStatus.value = status
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        jwtToken?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("FruitLogix-API", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: FruitLogixApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FruitLogixApiService::class.java)
    }
}