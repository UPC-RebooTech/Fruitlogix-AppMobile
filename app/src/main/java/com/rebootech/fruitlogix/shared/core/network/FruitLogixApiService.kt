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
import retrofit2.http.Query

// ─── 1. Autenticación (TS02) ────────────────────────────────────────────────
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

/** UpdateOrderResource: the server only changes these fields (not client, items or quantity). */
data class UpdateOrderRequestDto(
    val deliveryDueDate: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val notes: String? = null
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
    val cancellationReason: String? = null,
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
/** CreateDeliveryResource. The server requires driverName, vehiclePlate, routeOrigin, routeDestination and routeDistanceKm > 0. */
data class CreateDeliveryRequestDto(
    val orderId: Int,
    val driverName: String,
    val driverPhone: String,
    val vehiclePlate: String,
    val vehicleType: String,
    val routeOrigin: String,
    val routeDestination: String,
    val routeDistanceKm: Double,
    /** ISO date-time, e.g. 2026-11-13T15:30:00 */
    val estimatedTimeOfArrival: String
)

/** ReportDelayResource: the server only stores the reason and sets the status to Delayed. */
data class ReportDelayRequestDto(
    val reason: String
)

/** DeliveryResource. currentStatus: PendingDispatch, InTransit, Delayed, Delivered or Failed. */
data class DeliveryResponseDto(
    val id: Int,
    val orderId: Int? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val vehiclePlate: String? = null,
    val vehicleType: String? = null,
    val routeOrigin: String? = null,
    val routeDestination: String? = null,
    val routeDistanceKm: Double? = null,
    val estimatedTimeOfArrival: String? = null,
    val currentStatus: String? = null
)

/** TrackingLogResource: GPS position and reefer readings of a delivery. */
data class TrackingLogResponseDto(
    val id: Int? = null,
    val deliveryId: Int? = null,
    val timestamp: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val temperature: Double? = null,
    val humidity: Double? = null
)

/** AlertResource. type: Temperature, Humidity, Location or Delay; severity: Info, Warning or Critical. */
data class LogisticsAlertResponseDto(
    val id: Int? = null,
    val deliveryId: Int? = null,
    val type: String? = null,
    val severity: String? = null,
    val message: String? = null,
    val isResolved: Boolean? = null,
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
    val orderId: Int?,
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

    @POST("deliveries/{id}/start-dispatch")
    suspend fun startDelivery(@Path("id") id: Int): Response<DeliveryResponseDto>

    // The server only reads "reason" from the body
    @POST("deliveries/{id}/report-delay")
    suspend fun reportDeliveryDelay(
        @Path("id") id: Int,
        @Body request: ReportDelayRequestDto
    ): Response<DeliveryResponseDto>

    @GET("tracking-logs")
    suspend fun getTrackingLogs(
        @Query("deliveryId") deliveryId: Int
    ): Response<List<TrackingLogResponseDto>>

    @GET("alerts")
    suspend fun getLogisticsAlerts(): Response<List<LogisticsAlertResponseDto>>

    // E. Infrastructure IoT
    @GET("io-t-devices")
    suspend fun getIotDevices(): Response<List<IotDeviceResponseDto>>

    // The server ignores the deviceId query and returns all readings (per device: sensor-readings/device/{deviceId})
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

}