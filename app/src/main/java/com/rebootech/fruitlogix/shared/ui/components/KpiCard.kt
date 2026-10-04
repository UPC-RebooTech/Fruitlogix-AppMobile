package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * KPI Card component: dark surface, overline caption, large Poppins number, small icon in a rounded square.
 *
 * @param caption Overline caption text
 * @param value Large KPI number string
 * @param icon Icon composable inside rounded square container
 * @param modifier Modifier to apply
 * @param deltaPill Optional delta status badge composable
 * @param progress Optional progress value (0.0f..1.0f)
 * @param footer Optional footer composable content
 */
@Composable
fun KpiCard(
    caption: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    deltaPill: (@Composable () -> Unit)? = null,
    progress: Float? = null,
    footer: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(
            modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caption.uppercase(),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography.kpiNumber,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                    fontWeight = FontWeight.Bold
                )
                deltaPill?.let {
                    it()
                }
            }

            progress?.let {
                Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar(
                    progress = it
                )
            }

            footer?.let {
                Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
                it()
            }
        }
    }
}

@Preview
@Composable
private fun KpiCardPreview() {
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.KpiCard(
                caption = "Active Shipments",
                value = "142",
                icon = {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_fleet,
                        contentDescription = "Fleet",
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
                    )
                },
                deltaPill = {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                        text = "+12%",
                        type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS
                    )
                },
                progress = 0.75f
            )
        }
    }
}
