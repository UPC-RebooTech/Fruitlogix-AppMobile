package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

enum class AppLanguage {
    ES,
    EN
}

/**
 * Top App Bar component for FruitLogix Distributor app.
 *
 * @param title Top bar title string
 * @param subtitle Top bar subtitle string
 * @param selectedLanguage Currently selected language (ES | EN)
 * @param onLanguageSelected Callback when user toggles language
 * @param onNotificationClick Callback when user taps notification bell
 * @param onProfileClick Callback when user taps user avatar
 * @param modifier Modifier to apply
 * @param hasUnreadNotifications Show red dot on notification bell if true
 * @param userInitials Initials to display inside avatar circle
 */
@Composable
fun FruitLogixTopBar(
    title: String,
    subtitle: String,
    selectedLanguage: com.rebootech.fruitlogix.shared.ui.components.AppLanguage,
    onLanguageSelected: (com.rebootech.fruitlogix.shared.ui.components.AppLanguage) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadNotifications: Boolean = true,
    userInitials: String = "FL"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar)
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title & Subtitle Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_sensor_waves,
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
            Column {
                Text(
                    text = title,
                    fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
                )
                Text(
                    text = subtitle.uppercase(),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Controls: Language Toggle Pill + Notification Bell + Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs)
        ) {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LanguageTogglePill(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNotificationClick),
                contentAlignment = Alignment.Center
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_notifications,
                    contentDescription = "Notifications",
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                    modifier = Modifier.size(24.dp)
                )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LanguageTogglePill(
    selectedLanguage: com.rebootech.fruitlogix.shared.ui.components.AppLanguage,
    onLanguageSelected: (com.rebootech.fruitlogix.shared.ui.components.AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LanguageOptionItem(
            languageText = "ES",
            isSelected = selectedLanguage == _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppLanguage.ES,
            onClick = { onLanguageSelected(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppLanguage.ES) }
        )
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LanguageOptionItem(
            languageText = "EN",
            isSelected = selectedLanguage == _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppLanguage.EN,
            onClick = { onLanguageSelected(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppLanguage.EN) }
        )
    }
}

@Composable
private fun LanguageOptionItem(
    languageText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(if (isSelected) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = languageText,
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
            color = if (isSelected) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun FruitLogixTopBarPreview() {
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar(
            title = "FruitLogix",
            subtitle = "DISTRIBUTOR COMMAND",
            selectedLanguage = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppLanguage.ES,
            onLanguageSelected = {},
            onNotificationClick = {},
            onProfileClick = {}
        )
    }
}
