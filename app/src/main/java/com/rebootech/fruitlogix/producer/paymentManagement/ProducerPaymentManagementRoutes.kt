package com.rebootech.fruitlogix.producer.paymentManagement

object ProducerPaymentManagementRoutes {
    const val Payments = "payments"
    const val SettlementDetail = "payments_settlement/{settlementId}"
    const val UpgradePro = "payments_upgrade_pro"
    fun settlementDetail(id: String) = "payments_settlement/$id"
}

