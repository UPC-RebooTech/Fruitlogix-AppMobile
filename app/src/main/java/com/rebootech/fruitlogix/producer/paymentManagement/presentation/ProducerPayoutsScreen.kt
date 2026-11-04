package com.rebootech.fruitlogix.producer.paymentManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ProducerPayoutsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        Text(
            text = "Liquidaciones y Pagos",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            style = FruitLogixTheme.typography.displayLarge,
            color = FruitLogixTheme.colors.textOnLight
        )
        Text(
            text = "ESTADOS DE CUENTA Y TRANSFERENCIAS BANCARIAS",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        FruitLogixCard(
            variant = CardVariant.Dark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StatusBadge(text = "Depositado", type = StatusBadgeType.SUCCESS)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Liquidación Lote #842 • US$ 14,250.00",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.textOnDark
                )
                Text(
                    text = "Abono BBVA • Cuenta Fundo Los Laureles",
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        }
    }
}

