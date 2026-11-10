package com.rebootech.fruitlogix.producer.qualityControl.harvests.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

data class CreateHarvestBatchRequest(
    @SerializedName("producerId") val producerId: Long = 1L,
    @SerializedName("fruitType") val fruitType: String,
    @SerializedName("quantityKg") val quantityKg: Double,
    @SerializedName("harvestDate") val harvestDate: String
)

data class HarvestBatchResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("producerId") val producerId: Long? = null,
    @SerializedName("fruitType") val fruitType: String? = null,
    @SerializedName("quantityKg") val quantityKg: Double? = null,
    @SerializedName("harvestDate") val harvestDate: String? = null,
    @SerializedName("status") val status: String? = null
)

interface HarvestApiService {

    @Headers("Accept: application/json")
    @GET("api/v1/harvest-batches")
    suspend fun getHarvestBatches(): Response<List<HarvestBatchResponse>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("api/v1/harvest-batches")
    suspend fun createHarvestBatch(
        @Body request: CreateHarvestBatchRequest
    ): Response<HarvestBatchResponse>
}