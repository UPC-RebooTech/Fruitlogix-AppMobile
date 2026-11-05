package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.ColorDangerStrong
import com.rebootech.fruitlogix.shared.ui.theme.ColorInfo
import com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary
import com.rebootech.fruitlogix.shared.ui.theme.ColorSuccess
import com.rebootech.fruitlogix.shared.ui.theme.ColorWarning

enum class SyncState {
    SYNCED,
    PENDING_SYNC,
    SYNCING,
    CONFLICT
}

/**
 * Status chip displaying synchronization state.
 *
 * @param state Synchronization state (SYNCED, PENDING_SYNC, SYNCING, CONFLICT)
 * @param modifier Optional modifier
 * @param onClick Optional click handler (e.g. to open Sync Center)
 */
@Composable
fun SyncChip(
    state: SyncState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (label, badgeType) = when (state) {
        SyncState.SYNCED -> Pair("Synced", StatusBadgeType.SUCCESS)
        SyncState.PENDING_SYNC -> Pair("Pending Sync", StatusBadgeType.WARNING)
        SyncState.SYNCING -> Pair("Syncing...", StatusBadgeType.INFO)
        SyncState.CONFLICT -> Pair("Sync Conflict", StatusBadgeType.DANGER)
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    StatusBadge(
        text = label,
        type = badgeType,
        modifier = clickableModifier,
        icon = {
            val dotColor = when (badgeType) {
                StatusBadgeType.SUCCESS -> ColorSuccess
                StatusBadgeType.WARNING -> ColorWarning
                StatusBadgeType.INFO -> ColorInfo
                StatusBadgeType.DANGER -> ColorDangerStrong
                else -> ColorPrimary
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    )
}
