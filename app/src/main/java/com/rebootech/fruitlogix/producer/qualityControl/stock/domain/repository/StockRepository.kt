package com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun observeStock(): Flow<List<StockItem>>
    suspend fun getStockItem(productId: String): StockItem?
}

