package com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsDivider
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsGroup
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsRadioRow
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Language choice. Uses the same mechanism as the top bar toggle ([AppLocale]); the activity
 * is recreated and the whole app switches language. Language names are shown in their own language.
 */
@Composable
fun LanguageSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val current = remember { AppLocale.current(context) }
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(FruitLogixTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.md)
    ) {
        BackHeader(
            title = stringResource(R.string.settings_language),
            onBack = onNavigateBack
        )
        Text(
            text = stringResource(R.string.settings_language_intro),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        SettingsGroup(title = null) {
            SettingsRadioRow(
                title = stringResource(R.string.settings_language_en),
                selected = current == AppLanguage.EN,
                onClick = { activity?.let { AppLocale.apply(it, AppLanguage.EN) } }
            )
            SettingsDivider()
            SettingsRadioRow(
                title = stringResource(R.string.settings_language_es),
                selected = current == AppLanguage.ES,
                onClick = { activity?.let { AppLocale.apply(it, AppLanguage.ES) } }
            )
        }
    }
}
