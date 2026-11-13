package com.rebootech.fruitlogix.shared.core.network

import okhttp3.ResponseBody
import retrofit2.Response
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

data class SignUpRequestDto(
    val username: String,
    val password: String
)

data class AuthenticatedUserDto(
    val id: Int,
    val username: String?,
    val token: String?
)

// ─── Producers (/api/v1/producers) ─── same field names as the server ─────
/** ProducerResource returned by GET, POST and PUT. */
data class ProducerResourceDto(
    val id: Int,
    val producerType: String? = null, // "Individual" or "Company"
    val fullName: String? = null,
    val taxId: String? = null,
    val legalName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val address: String? = null,
    val crop: String? = null,
    val cultivatedHectares: Double? = null,
    val monthlyProduction: String? = null,
    val operationsStartDate: String? = null,
    val rating: Double? = null,
    val certifications: String? = null,
    val photo: String? = null
)

/** Body of POST and PUT. The server requires producerType, fullName, an 11-digit taxId, email, phone and crop. */
data class SaveProducerRequestDto(
    val producerType: String,
    val fullName: String,
    val taxId: String,
    val legalName: String,
    val email: String,
    val phone: String,
    val country: String,
    val region: String,
    val city: String,
    val address: String,
    val crop: String,
    val cultivatedHectares: Double = 0.0,
    val monthlyProduction: String = "",
    val operationsStartDate: String? = null,
    val certifications: String = "",
    val photo: String = ""
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

    /**
     * 200 when created, 409 when the username is taken. Only the status code is read: the body's
     * "message" comes as an object (untranslated server text), which a String field cannot parse.
     */
    @POST("authentication/sign-up")
    suspend fun signUp(@Body request: SignUpRequestDto): Response<ResponseBody>

    // Producers: 409 when the RUC is taken, 400 when a field is invalid, 404 when the id does not exist
    @GET("producers")
    suspend fun getProducers(): List<ProducerResourceDto>

    @POST("producers")
    suspend fun createProducer(@Body producer: SaveProducerRequestDto): ProducerResourceDto

    @PUT("producers/{id}")
    suspend fun updateProducer(@Path("id") id: Int, @Body producer: SaveProducerRequestDto): ProducerResourceDto

    @DELETE("producers/{id}")
    suspend fun deleteProducer(@Path("id") id: Int): Response<Unit>

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