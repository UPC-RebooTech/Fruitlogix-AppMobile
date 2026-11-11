package com.rebootech.fruitlogix.producer.profilesManagement.presentation

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.BuildConfig
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.producer.profilesManagement.data.PrefsSettingsRepository
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.NotificationSettings
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsDivider
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsGroup
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsNavRow
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Number of alert types in [NotificationSettings] (quiet hours is not an alert). */
private fun NotificationSettings.enabledAlerts(): Int =
    listOf(newOrders, qualityResults, coldChainAlerts, payments, messages).count { it }

private const val TOTAL_ALERTS = 5

/**
 * My account (producer).
 * - Highlighted (dark) card: the person using the app — name, @username, role and farm —
 *   with "Edit profile". Personal, farm and legal details live in Edit profile.
 * - Settings list (light): notifications, language, data and sync, help. Then log out.
 */
@Composable
fun ProducerProfileScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenSync: () -> Unit = {},
    onOpenHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    val session by remember { DataStoreSessionRepository.getInstance(context) }.session.collectAsState()
    val profile by FakeProfileRepository.instance.profile.collectAsState()
    val settingsRepo = remember { PrefsSettingsRepository.getInstance(context) }
    val notifications by settingsRepo.notifications.collectAsState()
    val lastSync by settingsRepo.lastSyncAt.collectAsState()
    val language = remember { AppLocale.current(context) }

    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        Text(
            text = stringResource(R.string.profile_account_title),
            style = FruitLogixTheme.typography.headlineMedium,
            color = scheme.onBackground
        )

        // Account card (highlighted)
        FruitLogixCard(
            variant = CardVariant.Dark,
            shape = FruitLogixTheme.shapes.HeroCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(scheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialsOf(profile.fullName),
                            style = FruitLogixTheme.typography.titleLarge,
                            color = scheme.onPrimaryContainer
                        )
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
                        session?.username?.let { username ->
                            Text(
                                text = stringResource(R.string.profile_username, username),
                                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                                color = FruitLogixTheme.colors.onInverseSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.inner))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = stringResource(R.string.profile_role_producer),
                        type = StatusBadgeType.HIGHLIGHT,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(spacing.xs))
                    AppIcon(
                        id = R.drawable.ic_leaf,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.onInverseSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.xxs))
                    Text(
                        text = profile.farmName,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.onInverseSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextActionButton(
                        text = stringResource(R.string.profile_edit),
                        onClick = onEditProfile,
                        leadingIcon = {
                            AppIcon(
                                id = R.drawable.ic_edit,
                                contentDescription = null,
                                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                            )
                        }
                    )
                }
            }
        }

        // Settings
        SettingsGroup(title = stringResource(R.string.profile_section_settings)) {
            SettingsNavRow(
                iconRes = R.drawable.ic_notifications,
                title = stringResource(R.string.settings_notifications),
                summary = pluralStringResource(
                    R.plurals.settings_notifications_summary,
                    notifications.enabledAlerts(),
                    notifications.enabledAlerts(),
                    TOTAL_ALERTS
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
                summary = lastSync?.let {
                    stringResource(R.string.settings_sync_summary, formatRelativeDateTime(it))
                } ?: stringResource(R.string.settings_sync_never),
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
                onClick = { showSignOutDialog = true },
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

    if (showSignOutDialog) {
        ConfirmDialog(
            title = stringResource(R.string.auth_logout_dialog_title),
            message = stringResource(R.string.auth_logout_dialog_message),
            confirmText = stringResource(R.string.auth_logout_dialog_confirm),
            dismissText = stringResource(R.string.auth_logout_dialog_cancel),
            destructive = true,
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }
}
