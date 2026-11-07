package com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerDeliveryScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProducerDeliveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dispatch = uiState.currentDispatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recojo en Fundo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (dispatch == null) {
                Text("No hay unidades de recojo asignadas para hoy.")
                return@Scaffold
            }

            // Tarjeta de Estado & ETA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isDispatched) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dispatch.orderCode,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (uiState.isDispatched) Color(0xFF2E7D32) else Color(0xFF1565C0),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (uiState.isDispatched) "DESPACHADO" else "EN RUTA AL FUNDO",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (!uiState.isDispatched) {
                        Text(
                            text = "Llegada estimada en: ${dispatch.etaMinutes} minutos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Carga entregada y en tránsito al centro de distribución",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Datos del Chofer y Unidad
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transporte Asignado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Distribuidor: ${dispatch.distributorName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Conductor: ${dispatch.driverName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Vehículo: ${dispatch.vehiclePlate}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Teléfono de contacto: ${dispatch.driverPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Datos de la Carga / Fruta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Detalle del Lote", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Producto: ${dispatch.assignedFruit}", style = MaterialTheme.typography.bodyMedium)
                    Text("Peso Total Estimado: ${dispatch.totalWeightKg} Kg", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón interactivo de despacho
            Button(
                onClick = { viewModel.onConfirmPickup() },
                enabled = !uiState.isDispatched,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (uiState.isDispatched) "Despacho Confirmado" else "Confirmar Carga y Salida del Camión",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}