package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.BuildConfig
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorNotificationSettings
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ServerStatus
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SettingsDivider
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.components.SettingsNavRow
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * My account (distributor), same layout as the producer's.
 * - Highlighted (dark) card: who is signed in — name, @username, role and hub — with "Edit profile".
 *   Contact and company data (email, phone, RUC) live in Edit profile.
 * - Settings list (light): notifications, language, data and sync, help. Then log out.
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenSync: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val language = remember { AppLocale.current(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        BackHeader(title = stringResource(R.string.profile_account_title), onBack = onBack)

        state.profile?.let { AccountCard(it, state.username, onEditProfile) }

        SettingsGroup(title = stringResource(R.string.profile_section_settings)) {
            SettingsNavRow(
                iconRes = R.drawable.ic_notifications,
                title = stringResource(R.string.settings_notifications),
                summary = pluralStringResource(
                    R.plurals.settings_notifications_summary,
                    state.enabledAlerts,
                    state.enabledAlerts,
                    DistributorNotificationSettings.TOTAL_ALERTS
                ),
                onClick = onOpenNotifications
            )
            SettingsDivider()
            SettingsNavRow(
                iconRes = R.drawable.ic_language,
                title = stringResource(R.string.settings_language),
                summary = stringResource(
                    if (language == AppLanguage.ES) R.string.settings_language_es else R.string.settings_language_en
                ),
                onClick = onOpenLanguage
            )
            SettingsDivider()
            SettingsNavRow(
                iconRes = R.drawable.ic_refresh,
                title = stringResource(R.string.settings_sync),
                summary = syncSummary(state.serverStatus, state.lastSyncAt),
                onClick = onOpenSync
            )
        }

        SettingsGroup(title = stringResource(R.string.profile_section_support)) {
            SettingsNavRow(
                iconRes = R.drawable.ic_info,
                title = stringResource(R.string.settings_help),
                summary = stringResource(R.string.settings_help_summary),
                onClick = onOpenHelp
            )
            SettingsDivider()
            SettingsNavRow(
                iconRes = R.drawable.ic_logout,
                title = stringResource(R.string.auth_logout_button),
                summary = null,
                onClick = viewModel::askSignOut,
                tint = scheme.error
            )
        }

        Text(
            text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
            style = FruitLogixTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (state.confirmSignOut) {
        ConfirmDialog(
            title = stringResource(R.string.auth_logout_dialog_title),
            message = stringResource(R.string.auth_logout_dialog_message),
            confirmText = stringResource(R.string.auth_logout_dialog_confirm),
            dismissText = stringResource(R.string.auth_logout_dialog_cancel),
            destructive = true,
            onConfirm = {
                viewModel.cancelSignOut()
                onSignOut()
            },
            onDismiss = viewModel::cancelSignOut
        )
    }
}

/** Offline matters more than the last sync time, so it is shown first. */
@Composable
private fun syncSummary(status: ServerStatus, lastSync: java.time.LocalDateTime?): String = when {
    status == ServerStatus.OFFLINE -> stringResource(R.string.dset_server_offline_desc)
    lastSync != null -> stringResource(R.string.settings_sync_summary, formatRelativeDateTime(lastSync))
    else -> stringResource(R.string.settings_sync_never)
}

/** Highlighted (dark) account block. */
@Composable
private fun AccountCard(profile: DistributorProfile, username: String, onEditProfile: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val spacing = FruitLogixTheme.spacing

    FruitLogixCard(variant = CardVariant.Dark, shape = FruitLogixTheme.shapes.HeroCard, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(scheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initialsOf(profile.fullName), style = FruitLogixTheme.typography.titleLarge, color = scheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(spacing.inner))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName,
                        style = FruitLogixTheme.typography.titleLarge,
                        color = scheme.inverseOnSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (username.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.profile_username, username),
                            style = FruitLogixTheme.extraTypography.dataMonoSmall,
                            color = colors.onInverseSurfaceVariant
                        )
                    }
                    if (profile.roleTitle.isNotBlank()) {
                        Text(
                            text = profile.roleTitle,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = colors.onInverseSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.inner))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                    text = stringResource(R.string.dprof_role),
                    type = StatusBadgeType.HIGHLIGHT,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(spacing.xs))
                AppIcon(
                    id = R.drawable.ic_warehouse,
                    contentDescription = null,
                    tint = colors.onInverseSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                Text(
                    text = profile.companyHub,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = colors.onInverseSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextActionButton(
                    text = stringResource(R.string.profile_edit),
                    onClick = onEditProfile,
                    leadingIcon = {
                        AppIcon(id = R.drawable.ic_edit, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                    }
                )
            }
        }
    }
}
