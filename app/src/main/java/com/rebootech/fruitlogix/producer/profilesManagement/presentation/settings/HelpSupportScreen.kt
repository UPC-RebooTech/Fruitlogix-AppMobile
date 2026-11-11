package com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsDivider
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private data class Faq(@StringRes val question: Int, @StringRes val answer: Int)

private val faqs = listOf(
    Faq(R.string.help_faq_offline_q, R.string.help_faq_offline_a),
    Faq(R.string.help_faq_reject_q, R.string.help_faq_reject_a),
    Faq(R.string.help_faq_payments_q, R.string.help_faq_payments_a),
    Faq(R.string.help_faq_language_q, R.string.help_faq_language_a)
)

/** Frequent questions (expandable) and a contact card. */
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit,
    onOpenMessages: () -> Unit,
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
                FaqItem(faq)
            }
        }

        // Contact (light card)
        FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(spacing.sm)) {
                Text(
                    text = stringResource(R.string.help_contact_title),
                    style = FruitLogixTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(
                    text = stringResource(R.string.help_contact_desc),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                SecondaryButton(
                    text = stringResource(R.string.help_contact_messages),
                    onClick = onOpenMessages,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        AppIcon(
                            id = R.drawable.ic_chat,
                            contentDescription = null,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FaqItem(faq: Faq) {
    val scheme = MaterialTheme.colorScheme
    var expanded by rememberSaveable { mutableStateOf(false) }
    val stateText = stringResource(if (expanded) R.string.help_cd_collapse else R.string.help_cd_expand)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { expanded = !expanded }
            .semantics { stateDescription = stateText }
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(faq.question),
                style = FruitLogixTheme.typography.titleSmall,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            AppIcon(
                id = R.drawable.ic_chevron_right,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconSm)
                    .rotate(if (expanded) 90f else 0f)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = stringResource(faq.answer),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = FruitLogixTheme.spacing.xs)
            )
        }
    }
}
