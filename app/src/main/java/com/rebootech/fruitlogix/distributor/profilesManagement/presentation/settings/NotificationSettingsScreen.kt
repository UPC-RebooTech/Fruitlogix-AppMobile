package com.rebootech.fruitlogix.distributor.profilesManagement.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.SettingsDivider
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.components.SettingsSwitchRow
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Distributor alerts on this phone: cold chain, arrivals, delays, quality, orders and invoices. */
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { DistributorDependencies.settingsRepository(context) }
    val settings by repository.notifications.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(FruitLogixTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.md)
    ) {
        BackHeader(title = stringResource(R.string.settings_notifications), onBack = onNavigateBack)
        Text(
            text = stringResource(R.string.settings_notifications_intro),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        // Logistics
        SettingsGroup(title = null) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_thermostat,
                title = stringResource(R.string.dset_notif_temperature),
                description = stringResource(R.string.dset_notif_temperature_desc),
                checked = settings.temperatureAlerts,
                onCheckedChange = { repository.updateNotifications(settings.copy(temperatureAlerts = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_logistics_arrive,
                title = stringResource(R.string.dset_notif_arrivals),
                description = stringResource(R.string.dset_notif_arrivals_desc),
                checked = settings.arrivals,
                onCheckedChange = { repository.updateNotifications(settings.copy(arrivals = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_logistics_clock,
                title = stringResource(R.string.dset_notif_delays),
                description = stringResource(R.string.dset_notif_delays_desc),
                checked = settings.delays,
                onCheckedChange = { repository.updateNotifications(settings.copy(delays = it)) }
            )
        }

        // Business
        SettingsGroup(title = null) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_shield_check,
                title = stringResource(R.string.dset_notif_quality),
                description = stringResource(R.string.dset_notif_quality_desc),
                checked = settings.qualityLots,
                onCheckedChange = { repository.updateNotifications(settings.copy(qualityLots = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_orders,
                title = stringResource(R.string.dset_notif_orders),
                description = stringResource(R.string.dset_notif_orders_desc),
                checked = settings.orderUpdates,
                onCheckedChange = { repository.updateNotifications(settings.copy(orderUpdates = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_invoices,
                title = stringResource(R.string.dset_notif_invoices),
                description = stringResource(R.string.dset_notif_invoices_desc),
                checked = settings.invoices,
                onCheckedChange = { repository.updateNotifications(settings.copy(invoices = it)) }
            )
        }

        SettingsGroup(title = null) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_fleet_moon,
                title = stringResource(R.string.settings_notif_quiet),
                description = stringResource(R.string.dset_notif_quiet_desc),
                checked = settings.quietHours,
                onCheckedChange = { repository.updateNotifications(settings.copy(quietHours = it)) }
            )
        }
    }
}
