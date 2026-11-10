package com.rebootech.fruitlogix.shared.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// ─── DTOs de Autenticación (TS02) ──────────────────────────
data class SignInRequestDto(
    val username: String,
    val password: String
)

data class AuthenticatedUserDto(
    val id: Int,
    val username: String,
    val token: String
)

// ─── DTOs de Productores (US05, US06, US39, US40) ──────────
data class ProducerDto(
    val id: Int? = null,
    val name: String? = null,
    val rucOrDni: String? = null,
    val type: String? = null, // "Individual" o "Company"
    val location: String? = null,
    val specialty: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val rating: Double? = null
)

// ─── DTOs de Pedidos (US01, US02, US03, US04) ──────────────
data class OrderDto(
    val id: Int? = null,
    val orderNumber: String? = null,
    val clientId: String? = null,
    val clientName: String? = null,
    val producerId: String? = null,
    val productName: String? = null,
    val quantityKg: Double? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val deliveryDate: String? = null,
    val createdAt: String? = null
)

data class AssignProducerRequestDto(
    val producerId: String
)

// ─── DTOs de Facturación (US47, US66) ──────────────────────
data class InvoiceDto(
    val id: Int? = null,
    val invoiceNumber: String? = null,
    val clientName: String? = null,
    val amount: Double? = null,
    val issueDate: String? = null,
    val dueDate: String? = null,
    val type: String? = null,
    val status: String? = null
)

// ─── DTOs de Control de Calidad (US10, US12, US53) ─────────
data class QualityInspectionDto(
    val id: Int? = null,
    val batchId: String? = null,
    val productName: String? = null,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val status: String? = null,
    val comments: String? = null
)

interface FruitLogixApiService {

    @POST("authentication/sign-in")
    suspend fun signIn(@Body request: SignInRequestDto): AuthenticatedUserDto

    // Productores
    @GET("producers")
    suspend fun getProducers(): List<ProducerDto>

    @POST("producers")
    suspend fun createProducer(@Body producer: ProducerDto): ProducerDto

    @PUT("producers/{id}")
    suspend fun updateProducer(@Path("id") id: String, @Body producer: ProducerDto): ProducerDto

    @DELETE("producers/{id}")
    suspend fun deleteProducer(@Path("id") id: String)

    // Pedidos
    @GET("orders")
    suspend fun getOrders(): List<OrderDto>

    @POST("orders")
    suspend fun createOrder(@Body order: OrderDto): OrderDto

    @PUT("orders/{id}")
    suspend fun updateOrder(@Path("id") id: String, @Body order: OrderDto): OrderDto

    @PATCH("orders/{id}/assign-producer")
    suspend fun assignProducer(
        @Path("id") id: String,
        @Body body: AssignProducerRequestDto
    ): OrderDto

    @DELETE("orders/{id}")
    suspend fun deleteOrder(@Path("id") id: String)

    // Facturas
    @GET("invoices")
    suspend fun getInvoices(): List<InvoiceDto>

    @POST("invoices")
    suspend fun createInvoice(@Body invoice: InvoiceDto): InvoiceDto

    // Inspecciones de Calidad
    @GET("quality-inspections")
    suspend fun getQualityInspections(): List<QualityInspectionDto>

    @POST("quality-inspections")
    suspend fun createQualityInspection(@Body inspection: QualityInspectionDto): QualityInspectionDto
}