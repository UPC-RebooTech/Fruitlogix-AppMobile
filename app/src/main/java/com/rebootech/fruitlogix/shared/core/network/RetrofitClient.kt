package com.rebootech.fruitlogix.shared.core.network

import android.util.Log
import com.rebootech.fruitlogix.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

enum class BackendSyncStatus {
    CONNECTING,
    ONLINE,
    OFFLINE_FALLBACK
}

object RetrofitClient {

    /** FruitLogix platform on Render (MySQL on filess.io). */
    const val BASE_URL = "https://rebootech-fruitlogix-api.onrender.com/api/v1/"

    /**
     * JWT of the signed-in user, sent on every request. The session repository sets it
     * when a session is restored, saved or closed.
     */
    @Volatile
    var jwtToken: String? = null

    private val _syncStatus = MutableStateFlow(BackendSyncStatus.CONNECTING)
    val syncStatus: StateFlow<BackendSyncStatus> = _syncStatus.asStateFlow()

    fun updateSyncStatus(status: BackendSyncStatus) {
        _syncStatus.value = status
    }

    private val authInterceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")
        jwtToken?.let { builder.header("Authorization", "Bearer $it") }
        chain.proceed(builder.build())
    }

    // Only the request line and status in debug builds: bodies would print passwords and tokens
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("FruitLogix-API", message)
    }.apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
    }

    // Render's free plan sleeps after a while without traffic and takes up to a minute to wake up
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
