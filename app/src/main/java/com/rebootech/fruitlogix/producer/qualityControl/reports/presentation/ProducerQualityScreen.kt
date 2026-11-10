package com.rebootech.fruitlogix.producer.qualityControl.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.reports.data.QualityReportRepository
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun ProducerQualityScreen(
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val allReports = QualityReportRepository.reports

    val filteredReports = allReports.filter { report ->
        val matchesTab = when (selectedTab) {
            "Approved" -> report.status == QualityStatus.APPROVED
            "Pending" -> report.status == QualityStatus.PENDING
            "Rejected" -> report.status == QualityStatus.REJECTED
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                report.orderNumber.contains(searchQuery, ignoreCase = true) ||
                report.productName.contains(searchQuery, ignoreCase = true) ||
                report.clientName.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(16.dp)
    ) {
        // Título Principal y Subtítulo
        Text(
            text = "Quality Reports",
            style = FruitLogixTheme.typography.displayLarge,
            color = FruitLogixTheme.colors.textOnLight
        )
        Text(
            text = "INSPECTION HISTORY PER ORDER • FUNDO SANTA ROSA",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Tarjeta de Métricas Superiores (Pass Rate & Sampled Today)
        FruitLogixCard(
            variant = CardVariant.Dark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield_check),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inspection Pass Rate", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("92.8%", style = FruitLogixTheme.typography.displayLarge, color = FruitLogixTheme.colors.white)
                    Text("📈 +1.4% this week", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.primary)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(55.dp)
                        .background(FruitLogixTheme.colors.surfaceTint)
                )

                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("SAMPLED TODAY", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("1,470 kg", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.white)
                    Text("42 total crates inspected", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search order #, product, client...", color = FruitLogixTheme.colors.textMuted) },
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = null, tint = FruitLogixTheme.colors.textMuted)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FruitLogixTheme.colors.surfaceLight,
                unfocusedContainerColor = FruitLogixTheme.colors.surfaceLight,
                focusedBorderColor = FruitLogixTheme.colors.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Pestañas de Filtro (All, Approved, Pending, Rejected)
        val tabs = listOf("All", "Approved", "Pending", "Rejected")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                val count = when (tab) {
                    "Approved" -> allReports.count { it.status == QualityStatus.APPROVED }
                    "Pending" -> allReports.count { it.status == QualityStatus.PENDING }
                    "Rejected" -> allReports.count { it.status == QualityStatus.REJECTED }
                    else -> allReports.size
                }
                Surface(
                    color = if (isSelected) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceLight,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { selectedTab = tab }
                ) {
                    Text(
                        text = "$tab ($count)",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textOnLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Lista de Reportes de Calidad
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredReports) { report ->
                QualityReportCard(
                    report = report,
                    onClick = { onNavigateToDetail(report.id) }
                )
            }
        }
    }
}

@Composable
fun QualityReportCard(report: QualityReportItem, onClick: () -> Unit) {
    val statusBg = when (report.status) {
        QualityStatus.APPROVED -> FruitLogixTheme.colors.success
        QualityStatus.PENDING -> FruitLogixTheme.colors.warning
        QualityStatus.REJECTED -> FruitLogixTheme.colors.dangerStrong
    }

    val statusText = when (report.status) {
        QualityStatus.APPROVED -> "Approved"
        QualityStatus.PENDING -> "Pending Quality"
        QualityStatus.REJECTED -> "Rejected"
    }

    // Usamos el contenedor con clickable limpio
    FruitLogixCard(
        variant = CardVariant.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // <--- Aquí toda la tarjeta reacciona al toque
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header de la Orden y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${report.orderNumber} • ${report.clientName}",
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.textOnLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${report.timestamp} • ${report.locationInfo}",
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
                Surface(
                    color = statusBg.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = statusBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtarjeta de Producto y Lote
            Surface(
                color = FruitLogixTheme.colors.surfaceLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_leaf),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${report.productName} • ${report.weightKg} kg",
                            style = FruitLogixTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FruitLogixTheme.colors.textOnLight
                        )
                    }
                    Surface(
                        color = FruitLogixTheme.colors.surfaceTint,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = report.lotCode,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textOnLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Alerta si es rechazado
            if (!report.alertMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_alert),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.dangerStrong,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = report.alertMessage,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.dangerStrong,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid de Métricas de Inspección
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricBox("Visual", "${report.visualScore} / 5.0", modifier = Modifier.weight(1f))
                MetricBox("Defects", "${report.defectsPercentage}%", modifier = Modifier.weight(1f), isAlert = report.defectsPercentage > 3.0)
                MetricBox("Core Temp", "${report.coreTemp} °C", modifier = Modifier.weight(1f))
                MetricBox("Humidity", "${report.humidityPercentage}%", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Inspector y Botón de Acción inferior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Inspector: ${report.inspectorName}",
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }

                // Superficie interactiva que también dispara el onClick general
                Surface(
                    color = FruitLogixTheme.colors.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when(report.status) {
                                QualityStatus.APPROVED -> "Certificate >"
                                QualityStatus.PENDING -> "Review >"
                                QualityStatus.REJECTED -> "Audit Details >"
                            },
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MetricBox(label: String, value: String, modifier: Modifier = Modifier, isAlert: Boolean = false) {
    Surface(
        color = if (isAlert) FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.1f) else FruitLogixTheme.colors.surfaceLight,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = FruitLogixTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textOnLight
            )
        }
    }
}