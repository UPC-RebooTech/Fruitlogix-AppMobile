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

/** role: CommercialClient, Producer or Distributor (this app sends Producer or Distributor). */
data class SignUpRequestDto(
    val username: String,
    val password: String,
    val role: String? = null
)

data class AuthenticatedUserDto(
    val id: Int,
    val username: String?,
    val token: String?,
    /** CommercialClient, Producer, Distributor or Admin. Not inside the JWT: keep it with the token. */
    val role: String? = null
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
    val items: List<OrderItemResponseDto>? = null,
    val updatedAt: String? = null,
    val deliveredAt: String? = null,
    val receivedAt: String? = null,
    val reception: OrderReceptionDto? = null,
    val batchId: Int? = null,
    val producerName: String? = null
)

data class AssignProducerRequestDto(
    val producerId: Int
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

/** DeliveryResource. currentStatus: PendingDispatch, InTransit, Delayed, Arrived, Delivered or Failed. */
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
    val currentStatus: String? = null,
    val dispatchedAt: String? = null,
    val arrivedAt: String? = null,
    val deliveredAt: String? = null
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


    // ── Orders: producer decision, harvest lot, cancel and reception ──
    /** action "accept" (Pending with producer → InPreparation) or "reject" (needs reason). */
    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(@Path("id") id: Int, @Body body: UpdateOrderStatusRequestDto): Response<OrderResponseDto>

    @PATCH("orders/{id}/batch")
    suspend fun assignOrderBatch(@Path("id") id: Int, @Body body: AssignOrderBatchRequestDto): Response<OrderResponseDto>

    /** Use it when DELETE answers 409 (the order already has a delivery, invoice or payment). */
    @PATCH("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: Int, @Body body: CancelOrderRequestDto): Response<OrderResponseDto>

    /** Only for InTransit orders (409 otherwise). Moves the order to Delivered and its delivery too. */
    @POST("orders/{id}/reception")
    suspend fun confirmOrderReception(@Path("id") id: Int, @Body body: ConfirmReceptionRequestDto): Response<OrderResponseDto>

    // ── Deliveries: detail, events, arrival, checkpoints, reassignment, tracking ──
    @GET("deliveries/{id}")
    suspend fun getDelivery(@Path("id") id: Int): Response<DeliveryResponseDto>

    @GET("deliveries/order/{orderId}")
    suspend fun getDeliveriesByOrder(@Path("orderId") orderId: Int): Response<List<DeliveryResponseDto>>

    @GET("deliveries/{id}/events")
    suspend fun getDeliveryEvents(@Path("id") id: Int): Response<List<DeliveryEventDto>>

    /** Only from InTransit or Delayed (409 otherwise). */
    @POST("deliveries/{id}/arrive")
    suspend fun arriveDelivery(@Path("id") id: Int, @Body body: ArriveDeliveryRequestDto): Response<DeliveryResponseDto>

    /** Before the arrival (409 after Arrived, Delivered or Failed). */
    @PATCH("deliveries/{id}/assignment")
    suspend fun updateDeliveryAssignment(@Path("id") id: Int, @Body body: UpdateDeliveryAssignmentRequestDto): Response<DeliveryResponseDto>

    /** Only while InTransit or Delayed. */
    @POST("deliveries/{id}/checkpoints")
    suspend fun addDeliveryCheckpoint(@Path("id") id: Int, @Body body: AddCheckpointRequestDto): Response<DeliveryEventDto>

    /** Inside the client's warehouse radius an InTransit/Delayed delivery becomes Arrived (geofence). */
    @POST("tracking-logs")
    suspend fun createTrackingLog(@Body body: CreateTrackingLogRequestDto): Response<TrackingLogResponseDto>

    // ── Products (catalog) ──
    @GET("products")
    suspend fun getProducts(
        @Query("producerId") producerId: Int? = null,
        @Query("fruitType") fruitType: String? = null,
        @Query("available") available: Boolean? = null
    ): Response<List<ProductDto>>

    @POST("products")
    suspend fun createProduct(@Body body: SaveProductRequestDto): Response<ProductDto>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body body: SaveProductRequestDto): Response<ProductDto>

    /** Deactivates the product (it is not erased). Reactivate with PUT isActive = true. */
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    // ── Producer profile of the signed-in Producer (404 = not created yet) ──
    @GET("producers/me")
    suspend fun getMyProducerProfile(): Response<ProducerResourceDto>

    /** Same call as [createProducer], but keeps the error body (409: the account already has a profile). */
    @POST("producers")
    suspend fun createMyProducerProfile(@Body producer: SaveProducerRequestDto): Response<ProducerResourceDto>

    @PUT("producers/{id}")
    suspend fun updateMyProducerProfile(@Path("id") id: Int, @Body producer: SaveProducerRequestDto): Response<ProducerResourceDto>

    // ── Harvest batches and quality inspections ──
    /** Not filtered by the server: filter by producerId in the app. */
    @GET("harvest-batches")
    suspend fun getHarvestBatches(): Response<List<HarvestBatchDto>>

    @POST("harvest-batches")
    suspend fun createHarvestBatch(@Body body: CreateHarvestBatchRequestDto): Response<HarvestBatchDto>

    @PATCH("harvest-batches/{id}/status")
    suspend fun updateHarvestBatchStatus(@Path("id") id: Int, @Body body: UpdateHarvestBatchStatusRequestDto): Response<HarvestBatchDto>

    @POST("quality-inspections")
    suspend fun createQualityInspection(@Body body: QualityInspectionRequestDto): Response<QualityInspectionDto>

    @GET("quality-inspections/batch/{batchId}")
    suspend fun getQualityInspectionsByBatch(@Path("batchId") batchId: Int): Response<List<QualityInspectionDto>>

    // ── Incidents and users ──
    @GET("incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @POST("incidents")
    suspend fun createIncident(@Body body: CreateIncidentRequestDto): Response<IncidentDto>

    /** A Distributor sees every account (id and username), used to pick the client of an order. */
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>
}
