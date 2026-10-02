package com.rebootech.fruitlogix.ui.components

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
import com.rebootech.fruitlogix.ui.theme.FruitLogixExtraTypography
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme

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
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(
            modifier = Modifier.padding(FruitLogixTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caption.uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FruitLogixTheme.colors.appbar),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = FruitLogixExtraTypography.kpiNumber,
                    color = FruitLogixTheme.colors.textOnDark,
                    fontWeight = FontWeight.Bold
                )
                deltaPill?.let {
                    it()
                }
            }

            progress?.let {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                LimeProgressBar(progress = it)
            }

            footer?.let {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                it()
            }
        }
    }
}

@Preview
@Composable
private fun KpiCardPreview() {
    FruitLogixTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KpiCard(
                caption = "Active Shipments",
                value = "142",
                icon = {
                    AppIcon(
                        id = R.drawable.ic_fleet,
                        contentDescription = "Fleet",
                        tint = FruitLogixTheme.colors.primary
                    )
                },
                deltaPill = {
                    StatusBadge(text = "+12%", type = StatusBadgeType.SUCCESS)
                },
                progress = 0.75f
            )
        }
    }
}
