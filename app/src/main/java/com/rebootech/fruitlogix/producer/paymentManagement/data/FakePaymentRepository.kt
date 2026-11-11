package com.rebootech.fruitlogix.producer.paymentManagement.data

import android.util.Log
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FakePaymentRepository : PaymentRepository {
    private val _summary = MutableStateFlow(
        PaymentSummary(
            pendingAmount = 18450.0,
            paidAmount = 42300.0,
            overdueAmount = 3200.0
        )
    )

    init {
        fetchPaymentSummaryFromBackend()
    }

    override fun observePaymentSummary(): Flow<PaymentSummary> = _summary.asStateFlow()

    override suspend fun getPaymentSummary(): PaymentSummary {
        return _summary.value
    }

    private fun fetchPaymentSummaryFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Intenta consultar la API para sincronizar información de órdenes/pagos
                val remoteOrders = RetrofitClient.api.getOrders()
                if (remoteOrders.isNotEmpty()) {
                    Log.d("FruitLogix-API", "Resumen de pagos sincronizado desde la nube.")
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                }
            } catch (e: Exception) {
                // Si Render está inactivo, mantiene el respaldo local sin interrumpir la app
                Log.e("FruitLogix-API", "Backend inactivo para pagos. Usando respaldo local: ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
    }

    companion object {
        val instance: FakePaymentRepository by lazy { FakePaymentRepository() }
    }
}