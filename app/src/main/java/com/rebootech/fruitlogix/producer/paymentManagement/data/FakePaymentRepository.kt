package com.rebootech.fruitlogix.producer.paymentManagement.data

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.Payout
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutStatus
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutType
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** In-memory payments until the payments backend exists. */
class FakePaymentRepository : PaymentRepository {

    private val today = LocalDate.now()

    private val _summary = MutableStateFlow(
        PaymentSummary(
            pendingAmount = 18450.0,
            paidAmount = 42300.0,
            overdueAmount = 3200.0
        )
    )

    private val _payouts = MutableStateFlow(
        listOf(
            Payout(
                id = "INV-104",
                type = PayoutType.HARVEST_ADVANCE,
                productName = "Mandarina W. Murcott",
                lotCode = null,
                distributorName = "Cencosud Retail",
                issuedOn = today.minusDays(6),
                dueOn = today.plusDays(4),
                amount = 1850.00,
                status = PayoutStatus.PENDING
            ),
            Payout(
                id = "INV-103",
                type = PayoutType.LOT_SETTLEMENT,
                productName = "Kent Mango",
                lotCode = "12",
                distributorName = "Mercado Central S.A.",
                issuedOn = today.minusDays(9),
                dueOn = null,
                amount = 3120.00,
                status = PayoutStatus.PAID
            ),
            Payout(
                id = "INV-102",
                type = PayoutType.LOT_SETTLEMENT,
                productName = "Hass Avocado",
                lotCode = "44",
                distributorName = "Supermercados Wong",
                issuedOn = today.minusDays(18),
                dueOn = today.minusDays(3),
                amount = 8900.50,
                status = PayoutStatus.OVERDUE
            ),
            Payout(
                id = "INV-101",
                type = PayoutType.LOT_SETTLEMENT,
                productName = "Arándano Ventura",
                lotCode = "07",
                distributorName = "Supermercados Wong",
                issuedOn = today.minusDays(30),
                dueOn = null,
                amount = 4250.00,
                status = PayoutStatus.PAID
            )
        )
    )

    override fun observePaymentSummary(): Flow<PaymentSummary> = _summary.asStateFlow()

    override suspend fun getPaymentSummary(): PaymentSummary = _summary.value

    override fun observePayouts(): Flow<List<Payout>> = _payouts.asStateFlow()

    companion object {
        val instance: FakePaymentRepository by lazy { FakePaymentRepository() }
    }
}
