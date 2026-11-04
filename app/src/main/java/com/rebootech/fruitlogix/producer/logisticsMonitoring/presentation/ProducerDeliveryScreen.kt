package com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation

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
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ProducerDeliveryScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        Text(
            text = "Entrega al Camión",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            style = FruitLogixTheme.typography.displayLarge,
            color = FruitLogixTheme.colors.textOnLight
        )
        Text(
            text = "LOGÍSTICA DE DESPACHO Y SEGUIMIENTO",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        FruitLogixCard(
            variant = CardVariant.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Pantalla en construcción",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.textOnLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aquí irá el flujo de entrega y confirmación de despacho al camión distribuidor.",
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        }
    }
}

