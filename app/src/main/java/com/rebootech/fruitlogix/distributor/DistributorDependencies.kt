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

/**
 * Composition root of the distributor (there is no DI framework). This is the only place that
 * knows the fake implementations: contexts depend on each other only through domain interfaces.
 * Swapping a fake for the Render backend later means changing one line here.
 */
object DistributorDependencies {
    val orderRepository: OrderRepository by lazy { FakeOrderRepository.instance }
    val iotRepository: IotRepository by lazy { FakeIotRepository() }
    val fleetRepository: FleetRepository by lazy { FakeFleetRepository(orderRepository) }
    val billingRepository: BillingRepository by lazy { FakeBillingRepository.instance }
    val qualityRepository: QualityRepository by lazy { FakeQualityRepository() }
    val profilesRepository: ProfilesRepository by lazy { FakeProfilesRepository.instance }
    val driverRepository: DriverRepository by lazy { FakeDriverRepository(orderRepository) }

    @Volatile private var settings: DistributorSettingsRepository? = null

    /** Settings live in SharedPreferences, so they need a Context the first time. */
    fun settingsRepository(context: Context): DistributorSettingsRepository =
        settings ?: synchronized(this) {
            settings ?: PrefsDistributorSettingsRepository(
                context.applicationContext,
                // Everything that syncs with Render today
                refreshers = listOf({ billingRepository.refresh() }, { profilesRepository.refresh() })
            ).also { settings = it }
        }
}
