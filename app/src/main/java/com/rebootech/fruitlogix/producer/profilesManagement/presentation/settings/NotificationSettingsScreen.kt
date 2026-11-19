package com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings

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
import com.rebootech.fruitlogix.producer.profilesManagement.data.PrefsSettingsRepository
import com.rebootech.fruitlogix.shared.ui.components.SettingsDivider
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.components.SettingsSwitchRow
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Notification preferences, saved on the device. */
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { PrefsSettingsRepository.getInstance(context) }
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
        BackHeader(
            title = stringResource(R.string.settings_notifications),
            onBack = onNavigateBack
        )
        Text(
            text = stringResource(R.string.settings_notifications_intro),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        SettingsGroup(title = null) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_orders,
                title = stringResource(R.string.settings_notif_new_orders),
                description = stringResource(R.string.settings_notif_new_orders_desc),
                checked = settings.newOrders,
                onCheckedChange = { repository.updateNotifications(settings.copy(newOrders = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_shield_check,
                title = stringResource(R.string.settings_notif_quality),
                description = stringResource(R.string.settings_notif_quality_desc),
                checked = settings.qualityResults,
                onCheckedChange = { repository.updateNotifications(settings.copy(qualityResults = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_thermostat,
                title = stringResource(R.string.settings_notif_cold_chain),
                description = stringResource(R.string.settings_notif_cold_chain_desc),
                checked = settings.coldChainAlerts,
                onCheckedChange = { repository.updateNotifications(settings.copy(coldChainAlerts = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_invoices,
                title = stringResource(R.string.settings_notif_payments),
                description = stringResource(R.string.settings_notif_payments_desc),
                checked = settings.payments,
                onCheckedChange = { repository.updateNotifications(settings.copy(payments = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_chat,
                title = stringResource(R.string.settings_notif_messages),
                description = stringResource(R.string.settings_notif_messages_desc),
                checked = settings.messages,
                onCheckedChange = { repository.updateNotifications(settings.copy(messages = it)) }
            )
        }

        SettingsGroup(title = null) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_fleet_moon,
                title = stringResource(R.string.settings_notif_quiet),
                description = stringResource(R.string.settings_notif_quiet_desc),
                checked = settings.quietHours,
                onCheckedChange = { repository.updateNotifications(settings.copy(quietHours = it)) }
            )
        }
    }
}
