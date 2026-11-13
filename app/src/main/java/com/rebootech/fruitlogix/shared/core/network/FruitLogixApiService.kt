package com.rebootech.fruitlogix.shared.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ─── 1. Autenticación (TS02) ────────────────────────────────────────────────
data class SignInRequestDto(
    val username: String,
    val password: String
)

data class AuthenticatedUserDto(
    val id: Int,
    val username: String,
    val token: String
)

// ─── 2. Order Management (Contratos reales de Swagger) ──────────────────────
data class OrderItemRequestDto(
    val productId: Int,
    val productName: String,
    val quantityKg: Double,
    val unitPrice: Double,
    val subtotal: Double
)

data class CreateOrderRequestDto(
    val commercialClientId: Int,
    val deliveryDueDate: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val notes: String? = null,
    val items: List<OrderItemRequestDto>
)

data class UpdateOrderRequestDto(
    val commercialClientId: Int,
    val deliveryDueDate: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val notes: String? = null,
    val items: List<OrderItemRequestDto>
)

data class OrderItemResponseDto(
    val id: Int? = null,
    val productId: Int? = null,
    val productName: String? = null,
    val quantityKg: Double? = null,
    val unitPrice: Double? = null,
    val subtotal: Double? = null
)

data class OrderResponseDto(
    val id: Int? = null,
    val orderNumber: String? = null,
    val commercialClientId: Int? = null,
    val clientName: String? = null,
    val producerId: Int? = null,
    val deliveryDueDate: String? = null,
    val deliveryAddress: String? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val items: List<OrderItemResponseDto>? = null
)

data class AssignProducerRequestDto(
    val producerId: Int
)

// ─── 3. Quality Control (QualityInspections en Swagger) ─────────────────────
data class TechnicalParametersDto(
    val temperatureCelsius: Double,
    val humidityPercent: Double,
    val brixDegrees: Double = 12.0
)

data class QualityChecklistDto(
    val packagingIntact: Boolean = true,
    val labelingCorrect: Boolean = true,
    val pestFree: Boolean = true,
    val coldChainMaintained: Boolean = true
)

data class CreateQualityInspectionRequestDto(
    val batchId: Int,
    val notes: String,
    val visualInspection: String, // "APPROVED" | "REJECTED" | "OBSERVED"
    val technicalParameters: TechnicalParametersDto,
    val checklist: QualityChecklistDto
)

data class QualityInspectionResponseDto(
    val id: Int? = null,
    val batchId: Int? = null,
    val notes: String? = null,
    val visualInspection: String? = null,
    val status: String? = null,
    val temperatureCelsius: Double? = null,
    val humidityPercent: Double? = null,
    val technicalParameters: TechnicalParametersDto? = null,
    val checklist: QualityChecklistDto? = null,
    val createdAt: String? = null
)

// ─── 4. Logistics & Deliveries ──────────────────────────────────────────────
data class CreateDeliveryRequestDto(
    val orderId: Int,
    val originAddress: String,
    val destinationAddress: String,
    val estimatedArrival: String,
    val notes: String? = null
)

data class ReportDelayRequestDto(
    val newEstimatedArrival: String,
    val delayMinutes: Int,
    val reason: String
)

data class DeliveryResponseDto(
    val id: Int? = null,
    val orderId: Int? = null,
    val unitCode: String? = null,
    val vehiclePlate: String? = null,
    val driverName: String? = null,
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val status: String? = null,
    val estimatedArrival: String? = null,
    val delayMinutes: Int? = null,
    val delayReason: String? = null
)

data class TrackingLogResponseDto(
    val id: Int? = null,
    val deliveryId: Int? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: String? = null,
    val notes: String? = null
)

data class LogisticsAlertResponseDto(
    val id: Int? = null,
    val deliveryId: Int? = null,
    val deviceId: String? = null,
    val alertType: String? = null,
    val severity: String? = null,
    val message: String? = null,
    val createdAt: String? = null
)

// ─── 5. Infrastructure IoT (IoTDevices, SensorReadings, AlertRules) ─────────
data class IotDeviceResponseDto(
    val id: String? = null,
    val deviceCode: String? = null,
    val unitId: String? = null,
    val location: String? = null,
    val status: String? = null,
    val batteryLevel: Int? = null
)

data class SensorReadingResponseDto(
    val id: Int? = null,
    val deviceId: String? = null,
    val value: Double? = null,
    val unit: String? = null, // "C", "°C", "%", "RH"
    val timestamp: String? = null
)

data class CreateSensorReadingRequestDto(
    val deviceId: String,
    val value: Double,
    val unit: String,
    val timestamp: String
)

data class AlertRuleResponseDto(
    val id: Int? = null,
    val fruitName: String? = null,
    val minTemperature: Double? = null,
    val maxTemperature: Double? = null,
    val minHumidity: Double? = null,
    val maxHumidity: Double? = null
)

// ─── 6. Invoices & Billing ──────────────────────────────────────────────────
data class CreateInvoiceRequestDto(
    val clientId: Int,
    val orderId: Int,
    val amount: Double,
    val currency: String = "PEN",
    val dueDate: String,
    val invoiceType: String
)

data class InvoiceResponseDto(
    val id: Int? = null,
    val invoiceNumber: String? = null,
    val clientId: Int? = null,
    val clientName: String? = null,
    val orderId: Int? = null,
    val totalAmount: Double? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val dueDate: String? = null,
    val invoiceType: String? = null,
    val issuedAt: String? = null,
    val status: String? = null
)

// ─── 7. Producer Network del Distribuidor ───────────────────────────────────
data class ProducerRequestDto(
    val producerType: String,
    val fullName: String,
    val taxId: String,
    val legalName: String,
    val country: String = "Perú",
    val region: String,
    val city: String,
    val address: String,
    val crop: String,
    val email: String? = null,
    val phone: String? = null
)

data class ProducerResponseDto(
    val id: Int? = null,
    val producerType: String? = null,
    val fullName: String? = null,
    val taxId: String? = null,
    val legalName: String? = null,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val address: String? = null,
    val crop: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val rating: Double? = null
)

interface FruitLogixApiService {

    @POST("authentication/sign-in")
    suspend fun signIn(@Body request: SignInRequestDto): Response<AuthenticatedUserDto>

    // A. Order Management
    @GET("orders")
    suspend fun getOrders(): Response<List<OrderResponseDto>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Response<OrderResponseDto>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequestDto): Response<OrderResponseDto>

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Path("id") id: Int,
        @Body request: UpdateOrderRequestDto
    ): Response<OrderResponseDto>

    @DELETE("orders/{id}")
    suspend fun deleteOrder(@Path("id") id: Int): Response<Unit>

    @PATCH("orders/{id}/assign-producer")
    suspend fun assignProducer(
        @Path("id") id: Int,
        @Body body: AssignProducerRequestDto
    ): Response<OrderResponseDto>

    // B. Quality Control
    @POST("quality-inspections")
    suspend fun createQualityInspection(
        @Body request: CreateQualityInspectionRequestDto
    ): Response<QualityInspectionResponseDto>

    @GET("quality-inspections/batch/{batchId}")
    suspend fun getQualityInspectionsByBatch(
        @Path("batchId") batchId: Int
    ): Response<List<QualityInspectionResponseDto>>

    // D. Logistics & Deliveries
    @GET("deliveries")
    suspend fun getDeliveries(): Response<List<DeliveryResponseDto>>

    @POST("deliveries")
    suspend fun createDelivery(
        @Body request: CreateDeliveryRequestDto
    ): Response<DeliveryResponseDto>

    @POST("deliveries/{id}/start")
    suspend fun startDelivery(@Path("id") id: Int): Response<DeliveryResponseDto>

    @POST("deliveries/{id}/delay")
    suspend fun reportDeliveryDelay(
        @Path("id") id: Int,
        @Body request: ReportDelayRequestDto
    ): Response<DeliveryResponseDto>

    @POST("deliveries/{id}/complete")
    suspend fun completeDelivery(@Path("id") id: Int): Response<DeliveryResponseDto>

    @GET("trackinglogs")
    suspend fun getTrackingLogs(
        @Query("deliveryId") deliveryId: Int
    ): Response<List<TrackingLogResponseDto>>

    @GET("alerts")
    suspend fun getLogisticsAlerts(): Response<List<LogisticsAlertResponseDto>>

    // E. Infrastructure IoT
    @GET("devices")
    suspend fun getIotDevices(): Response<List<IotDeviceResponseDto>>

    @GET("sensor-readings")
    suspend fun getSensorReadings(
        @Query("deviceId") deviceId: String? = null
    ): Response<List<SensorReadingResponseDto>>

    @POST("sensor-readings")
    suspend fun createSensorReading(
        @Body request: CreateSensorReadingRequestDto
    ): Response<SensorReadingResponseDto>

    @GET("alert-rules")
    suspend fun getAlertRules(): Response<List<AlertRuleResponseDto>>

    // F. Invoices & Billing
    @GET("invoices")
    suspend fun getInvoices(): Response<List<InvoiceResponseDto>>

    @GET("invoices/{id}")
    suspend fun getInvoiceById(@Path("id") id: Int): Response<InvoiceResponseDto>

    @POST("invoices")
    suspend fun createInvoice(
        @Body request: CreateInvoiceRequestDto
    ): Response<InvoiceResponseDto>

    // G. Producer Network
    @GET("producers")
    suspend fun getProducers(): Response<List<ProducerResponseDto>>

    @GET("producers/{id}")
    suspend fun getProducerById(@Path("id") id: Int): Response<ProducerResponseDto>

    @POST("producers")
    suspend fun createProducer(
        @Body request: ProducerRequestDto
    ): Response<ProducerResponseDto>

    @PUT("producers/{id}")
    suspend fun updateProducer(
        @Path("id") id: Int,
        @Body request: ProducerRequestDto
    ): Response<ProducerResponseDto>

    @DELETE("producers/{id}")
    suspend fun deleteProducer(@Path("id") id: Int): Response<Unit>
}