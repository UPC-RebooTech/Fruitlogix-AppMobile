package com.rebootech.fruitlogix.producer.infrastructureIot.domain.repository

import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.IotAlert
import kotlinx.coroutines.flow.StateFlow

interface IotAlertRepository {
    val alerts: StateFlow<List<IotAlert>>
    fun markRead(id: String)
    fun markAllRead()
    fun dismiss(id: String)
}
