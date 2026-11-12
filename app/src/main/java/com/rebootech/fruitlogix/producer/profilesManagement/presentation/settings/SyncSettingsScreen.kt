package com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.profilesManagement.data.PrefsSettingsRepository
import com.rebootech.fruitlogix.shared.ui.components.SettingsDivider
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.components.SettingsSwitchRow
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import kotlinx.coroutines.launch

/** Offline work and synchronization: status, sync now, and sync preferences. */
@Composable
fun SyncSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { PrefsSettingsRepository.getInstance(context) }
    val sync by repository.sync.collectAsState()
    val lastSync by repository.lastSyncAt.collectAsState()
    val pending by repository.pendingChanges.collectAsState()
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }

    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val spacing = FruitLogixTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        BackHeader(
            title = stringResource(R.string.settings_sync),
            onBack = onNavigateBack
        )
        Text(
            text = stringResource(R.string.settings_sync_intro),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        // Status card (light) with sync now
        FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(spacing.sm)) {
                Text(
                    text = stringResource(R.string.settings_sync_status).uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val upToDate = pending == 0
                    Box(
                        modifier = Modifier
                            .size(FruitLogixTheme.sizing.iconBox)
                            .clip(FruitLogixTheme.shapes.IconBox)
                            .background(if (upToDate) colors.successContainer else colors.attentionContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = if (upToDate) R.drawable.ic_check else R.drawable.ic_warning,
                            contentDescription = null,
                            tint = if (upToDate) colors.onSuccessContainer else colors.onAttentionContainer,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    }
                    Spacer(modifier = Modifier.width(spacing.inner))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (upToDate) {
                                stringResource(R.string.settings_sync_up_to_date)
                            } else {
                                pluralStringResource(R.plurals.settings_sync_pending, pending, pending)
                            },
                            style = FruitLogixTheme.typography.titleSmall,
                            color = scheme.onSurface
                        )
                        Text(
                            text = lastSync?.let {
                                stringResource(R.string.settings_sync_summary, formatRelativeDateTime(it))
                            } ?: stringResource(R.string.settings_sync_never),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.sm))
                SecondaryButton(
                    text = stringResource(if (syncing) R.string.settings_sync_in_progress else R.string.settings_sync_now),
                    onClick = {
                        if (!syncing) {
                            syncing = true
                            scope.launch {
                                repository.syncNow()
                                syncing = false
                            }
                        }
                    },
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        if (syncing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                            )
                        } else {
                            AppIcon(
                                id = R.drawable.ic_refresh,
                                contentDescription = null,
                                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                            )
                        }
                    }
                )
            }
        }

        SettingsGroup(title = null) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_refresh,
                title = stringResource(R.string.settings_sync_auto),
                description = stringResource(R.string.settings_sync_auto_desc),
                checked = sync.autoSync,
                onCheckedChange = { repository.updateSync(sync.copy(autoSync = it)) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                iconRes = R.drawable.ic_logistics_signal,
                title = stringResource(R.string.settings_sync_wifi),
                description = stringResource(R.string.settings_sync_wifi_desc),
                checked = sync.wifiOnly,
                onCheckedChange = { repository.updateSync(sync.copy(wifiOnly = it)) }
            )
        }
    }
}
