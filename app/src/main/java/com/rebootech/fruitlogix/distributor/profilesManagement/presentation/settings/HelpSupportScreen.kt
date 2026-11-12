package com.rebootech.fruitlogix.distributor.profilesManagement.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FaqRow
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.SettingsDivider
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private data class Faq(@StringRes val question: Int, @StringRes val answer: Int)

private val faqs = listOf(
    Faq(R.string.dset_faq_reception_q, R.string.dset_faq_reception_a),
    Faq(R.string.dset_faq_temperature_q, R.string.dset_faq_temperature_a),
    Faq(R.string.dset_faq_quality_q, R.string.dset_faq_quality_a),
    Faq(R.string.dset_faq_offline_q, R.string.dset_faq_offline_a),
    Faq(R.string.help_faq_language_q, R.string.help_faq_language_a)
)

/** Distributor frequent questions (expandable) and a contact card. */
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit,
    onOpenProducers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
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
            title = stringResource(R.string.settings_help),
            onBack = onNavigateBack,
            subtitle = stringResource(R.string.help_intro)
        )

        SettingsGroup(title = null) {
            faqs.forEachIndexed { index, faq ->
                if (index > 0) SettingsDivider()
                FaqRow(question = stringResource(faq.question), answer = stringResource(faq.answer))
            }
        }

        // Contact (light card)
        FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(spacing.sm)) {
                Text(stringResource(R.string.help_contact_title), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(stringResource(R.string.dset_contact_desc), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(spacing.sm))
                SecondaryButton(
                    text = stringResource(R.string.dset_contact_producers),
                    onClick = onOpenProducers,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        AppIcon(id = R.drawable.ic_producers, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                    }
                )
            }
        }
    }
}
