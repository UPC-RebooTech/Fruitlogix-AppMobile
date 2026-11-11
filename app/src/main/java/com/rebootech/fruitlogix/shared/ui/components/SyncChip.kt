package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

enum class SyncState {
    SYNCED,
    PENDING_SYNC,
    SYNCING,
    CONFLICT,
    OFFLINE
}

/**
 * Status chip displaying synchronization state.
 */
@Composable
fun SyncChip(
    state: SyncState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (label, badgeType) = when (state) {
        SyncState.SYNCED -> stringResource(R.string.sync_synced) to StatusBadgeType.SUCCESS
        SyncState.PENDING_SYNC -> pluralStringResource(R.plurals.sync_pending_changes, 1, 1) to StatusBadgeType.WARNING
        SyncState.SYNCING -> stringResource(R.string.sync_syncing) to StatusBadgeType.INFO
        SyncState.CONFLICT -> stringResource(R.string.sync_conflict) to StatusBadgeType.DANGER
        SyncState.OFFLINE -> stringResource(R.string.sync_offline) to StatusBadgeType.WARNING
    }

    StatusBadge(
        text = label,
        type = badgeType,
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    )
}

/**
 * Discreet sync strip shown under the top bar only when something needs the user's attention:
 * offline, changes waiting to sync, syncing or a conflict. Renders nothing when [SyncState.SYNCED].
 *
 * @param pendingChanges Number of local changes not yet sent (used for PENDING_SYNC)
 */
@Composable
fun SyncStatusStrip(
    state: SyncState,
    modifier: Modifier = Modifier,
    pendingChanges: Int = 0,
    onClick: (() -> Unit)? = null
) {
    if (state == SyncState.SYNCED) return

    val colors = FruitLogixTheme.colors
    val scheme = MaterialTheme.colorScheme
    data class StripStyle(val bg: Color, val fg: Color, val icon: Int)

    val style = when (state) {
        SyncState.CONFLICT -> StripStyle(scheme.errorContainer, scheme.onErrorContainer, R.drawable.ic_warning)
        SyncState.SYNCING -> StripStyle(colors.coldContainer, colors.onColdContainer, R.drawable.ic_refresh)
        else -> StripStyle(colors.attentionContainer, colors.onAttentionContainer, R.drawable.ic_warning)
    }
    val text = when (state) {
        SyncState.OFFLINE -> stringResource(R.string.sync_offline)
        SyncState.PENDING_SYNC -> pluralStringResource(
            R.plurals.sync_pending_changes,
            pendingChanges.coerceAtLeast(1),
            pendingChanges.coerceAtLeast(1)
        )
        SyncState.SYNCING -> stringResource(R.string.sync_syncing)
        SyncState.CONFLICT -> stringResource(R.string.sync_conflict)
        SyncState.SYNCED -> return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(style.bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            id = style.icon,
            contentDescription = null,
            tint = style.fg,
            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(
            text = text,
            style = FruitLogixTheme.typography.labelMedium,
            color = style.fg
        )
    }
}
