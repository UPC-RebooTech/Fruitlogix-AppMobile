package com.rebootech.fruitlogix.distributor

import android.content.Context
import com.rebootech.fruitlogix.distributor.fleetManagement.data.FakeDriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.infrastructureIot.data.FakeIotRepository
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.paymentManagement.data.FakeBillingRepository
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository.BillingRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.data.FakeProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.data.PrefsDistributorSettingsRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.DistributorSettingsRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import com.rebootech.fruitlogix.distributor.qualityControl.data.FakeQualityRepository
import com.rebootech.fruitlogix.distributor.qualityControl.domain.repository.QualityRepository

object DistributorDependencies {
    val orderRepository: OrderRepository by lazy { FakeOrderRepository.instance }
    val iotRepository: IotRepository by lazy { FakeIotRepository() }
    val fleetRepository: FleetRepository by lazy { FakeFleetRepository(orderRepository) }
    val billingRepository: BillingRepository by lazy { FakeBillingRepository.instance }
    val qualityRepository: QualityRepository by lazy { FakeQualityRepository() }
    val profilesRepository: ProfilesRepository by lazy { FakeProfilesRepository.instance }
    val driverRepository: DriverRepository by lazy { FakeDriverRepository(orderRepository) }

    @Volatile
    private var settings: DistributorSettingsRepository? = null

    fun settingsRepository(context: Context): DistributorSettingsRepository =
        settings ?: synchronized(this) {
            settings ?: PrefsDistributorSettingsRepository(
                context.applicationContext,
                refreshers = listOf(
                    { orderRepository.refresh() },
                    { billingRepository.refresh() },
                    { profilesRepository.refresh() },
                    { qualityRepository.refresh() },
                    { fleetRepository.refresh() },
                    { iotRepository.refresh() }
                )
            ).also { settings = it }
        }
}