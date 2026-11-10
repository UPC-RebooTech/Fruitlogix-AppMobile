package com.rebootech.fruitlogix.producer.infrastructureIot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

enum class NotificationSeverity {
    CRITICAL, WARNING, INFO
}

data class IotNotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val sensorId: String,
    val location: String,
    val timestamp: String,
    val severity: NotificationSeverity,
    val relatedOrderId: String? = null,
    val isRead: Boolean = false
)

@Composable
fun ProducerNotificationsScreen(
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todas") }

    // Lista mutable reactiva para poder descartar (borrar) o marcar como leídas
    var notificationsList by remember {
        mutableStateOf(
            listOf(
                IotNotificationItem(
                    id = "1",
                    title = "Sensor Temperatura — Cámara #1",
                    description = "Temperatura crítica en cámara de frío: 9.4°C detectados (Límite max: 8.0°C). Riesgo de descalibrado en palets.",
                    sensorId = "TEMP-BAY-01",
                    location = "Pre-Cool Tunnel B",
                    timestamp = "Hace 5 min",
                    severity = NotificationSeverity.CRITICAL,
                    relatedOrderId = "1044",
                    isRead = false
                ),
                IotNotificationItem(
                    id = "2",
                    title = "Sensor Humedad — Almacén Palets Hass",
                    description = "Humedad relativa cayó al 65.2% en zona de empaque. Recomendado activar nebulizadores.",
                    sensorId = "HUM-ZONE-04",
                    location = "Packhouse Bay 3",
                    timestamp = "Hace 22 min",
                    severity = NotificationSeverity.WARNING,
                    relatedOrderId = "1048",
                    isRead = false
                ),
                IotNotificationItem(
                    id = "3",
                    title = "Telemetría de Tránsito — Camión #3",
                    description = "Registro de vibración excesiva durante carga en muelle 2. Sensor G-Force activado.",
                    sensorId = "IOT-TRK-09",
                    location = "En Tránsito a Muelle",
                    timestamp = "Hace 1h 10m",
                    severity = NotificationSeverity.WARNING,
                    relatedOrderId = "1048",
                    isRead = true
                ),
                IotNotificationItem(
                    id = "4",
                    title = "Sincronización Ledgers IoT",
                    description = "300 kg de Hass Avocado registrados con hash de trazabilidad SENASA/HACCP verificado.",
                    sensorId = "GATEWAY-CORE-01",
                    location = "Fundo Santa Rosa",
                    timestamp = "Hace 2h",
                    severity = NotificationSeverity.INFO,
                    isRead = true
                )
            )
        )
    }

    val filteredNotifications = when (selectedFilter) {
        "Críticas" -> notificationsList.filter { it.severity == NotificationSeverity.CRITICAL }
        "Cámara Fría" -> notificationsList.filter { it.sensorId.contains("TEMP") || it.location.contains("Cool") }
        else -> notificationsList
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(horizontal = FruitLogixTheme.spacing.sm)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notificaciones IoT",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.displayLarge,
                        color = FruitLogixTheme.colors.textOnLight
                    )
                    Text(
                        text = "ALERTAS DE SENSORES Y EVENTOS DE CAMPO",
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (notificationsList.any { !it.isRead }) {
                    TextButton(
                        onClick = {
                            notificationsList = notificationsList.map { it.copy(isRead = true) }
                        }
                    ) {
                        Text(
                            "Marcar leídas",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Filtros rápidos (Chips)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("Todas", "Críticas", "Cámara Fría")
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                filter,
                                color = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textOnLight,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = FruitLogixTheme.colors.surfaceLight,
                            selectedContainerColor = FruitLogixTheme.colors.primary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Estado Vacío si no hay notificaciones
        if (filteredNotifications.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(30.dp))
                EmptyState(
                    title = "Sin notificaciones",
                    message = "No tienes alertas activas en esta categoría por el momento."
                )
            }
        } else {
            // Lista de Notificaciones
            items(filteredNotifications, key = { it.id }) { item ->
                IotNotificationCard(
                    notification = item,
                    onCardClick = {
                        // Marca como leída al tocar la tarjeta
                        notificationsList = notificationsList.map {
                            if (it.id == item.id) it.copy(isRead = true) else it
                        }
                    },
                    onDismiss = {
                        // Elimina/descarta la notificación
                        notificationsList = notificationsList.filter { it.id != item.id }
                    },
                    onOrderClick = { orderId ->
                        // Redirige al detalle del pedido dinámicamente
                        onNavigate(ProducerOrderManagementRoutes.orderDetail(orderId))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.lg + 80.dp))
        }
    }
}

@Composable
private fun IotNotificationCard(
    notification: IotNotificationItem,
    onCardClick: () -> Unit,
    onDismiss: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val (badgeBg, badgeText, badgeColor) = when (notification.severity) {
        NotificationSeverity.CRITICAL -> Triple(FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.15f), "CRÍTICO", FruitLogixTheme.colors.dangerStrong)
        NotificationSeverity.WARNING -> Triple(Color(0xFFE88A1A).copy(alpha = 0.15f), "ADVERTENCIA", Color(0xFFE88A1A))
        NotificationSeverity.INFO -> Triple(FruitLogixTheme.colors.primary.copy(alpha = 0.15f), "INFO", FruitLogixTheme.colors.primary)
    }

    FruitLogixCard(
        variant = CardVariant.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FruitLogixTheme.colors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = FruitLogixTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.timestamp,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón X para borrar/descartar notificación
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Descartar",
                            tint = FruitLogixTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = FruitLogixTheme.colors.textOnLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.description,
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = FruitLogixTheme.colors.bg.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sensor: ${notification.sensorId} • ${notification.location}",
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted,
                        fontSize = 11.sp
                    )

                    if (notification.relatedOrderId != null) {
                        Text(
                            text = "Ver pedido #${notification.relatedOrderId} →",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOrderClick(notification.relatedOrderId) }
                        )
                    }
                }
            }
        }
    }
}