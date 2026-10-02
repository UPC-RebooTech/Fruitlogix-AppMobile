package com.rebootech.fruitlogix.ui.components

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
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.ui.theme.PoppinsFontFamily

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
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadNotifications: Boolean = true,
    userInitials: String = "FL"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FruitLogixTheme.colors.appbar)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.xs),
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
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(FruitLogixTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_sensor_waves,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Column {
                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.textOnDark
                )
                Text(
                    text = subtitle.uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Controls: Language Toggle Pill + Notification Bell + Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            LanguageTogglePill(
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
                AppIcon(
                    id = R.drawable.ic_notifications,
                    contentDescription = "Notifications",
                    tint = FruitLogixTheme.colors.textOnDark,
                    modifier = Modifier.size(24.dp)
                )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(FruitLogixTheme.colors.dangerStrong)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FruitLogixTheme.colors.surfaceDark)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LanguageTogglePill(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(FruitLogixTheme.shapes.Pill)
            .background(FruitLogixTheme.colors.surfaceDark)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageOptionItem(
            languageText = "ES",
            isSelected = selectedLanguage == AppLanguage.ES,
            onClick = { onLanguageSelected(AppLanguage.ES) }
        )
        LanguageOptionItem(
            languageText = "EN",
            isSelected = selectedLanguage == AppLanguage.EN,
            onClick = { onLanguageSelected(AppLanguage.EN) }
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
            .clip(FruitLogixTheme.shapes.Pill)
            .background(if (isSelected) FruitLogixTheme.colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = languageText,
            style = FruitLogixTheme.typography.labelSmall,
            color = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun FruitLogixTopBarPreview() {
    FruitLogixTheme {
        FruitLogixTopBar(
            title = "FruitLogix",
            subtitle = "DISTRIBUTOR COMMAND",
            selectedLanguage = AppLanguage.ES,
            onLanguageSelected = {},
            onNotificationClick = {},
            onProfileClick = {}
        )
    }
}
