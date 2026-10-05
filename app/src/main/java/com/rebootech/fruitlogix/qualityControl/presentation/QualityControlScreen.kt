package com.rebootech.fruitlogix.qualityControl.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de colores de FruitLogix
private val BackgroundDark = Color(0xFF141A14)
private val CardBackground = Color(0xFF1E261D)
private val CardBorder = Color(0xFF2E382C)
private val AccentLime = Color(0xFFD2F852)
private val TextWhite = Color(0xFFF5F5F5)
private val TextMuted = Color(0xFF9EABA0)
private val ErrorRed = Color(0xFFE57373)
private val SuccessGreen = Color(0xFF81C784)

// Modelo para Reportes de Calidad de Lote (US10 + US53)
data class QualityReport(
    val id: String,
    val lotCode: String,
    val productName: String,
    val producerName: String,
    val temp: String,
    val humidity: String,
    var status: QualityStatus = QualityStatus.PENDING,
    var rejectionReason: String = "",
    val isPendingSync: Boolean = false
)

enum class QualityStatus { PENDING, APPROVED, REJECTED }

@Composable
fun QualityControlScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Lotes, 1: Inspección Campo
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedReportForRejection by remember { mutableStateOf<QualityReport?>(null) }
    var rejectionInputText by remember { mutableStateOf("") }

    // Lista interactiva de reportes de lotes
    val reportsList = remember {
        mutableStateListOf(
            QualityReport("1", "LOT-2026-089", "Palta Hass Exportación", "Agropecuaria El Valle", "6 °C", "85%"),
            QualityReport("2", "LOT-2026-092", "Mango Kent Grado A", "Agrícola Olmos S.A.C.", "11 °C", "90%"),
            QualityReport("3", "LOT-2026-095", "Arándanos Biloxi", "Campos del Sur", "2 °C", "88%"),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Control de Calidad",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Inspección de lotes y validación de calidad",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Pestañas sin advertencias (TabRow dentro de Surface)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBackground,
                    contentColor = AccentLime
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Validar Lotes", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Formulario Campo", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // US10: Lista de validación de lotes
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reportsList) { report ->
                        QualityReportCard(
                            report = report,
                            onApprove = {
                                report.status = QualityStatus.APPROVED
                            },
                            onReject = {
                                selectedReportForRejection = report
                                showRejectDialog = true
                            }
                        )
                    }
                }
            } else {
                // US53: Formulario de inspección de campo offline
                FieldInspectionForm(
                    onSaveInspection = { newLot, product, temp, hum ->
                        reportsList.add(
                            0,
                            QualityReport(
                                id = (reportsList.size + 1).toString(),
                                lotCode = newLot,
                                productName = product,
                                producerName = "Inspección Campo (Offline)",
                                temp = "$temp °C",
                                humidity = "$hum%",
                                isPendingSync = true
                            )
                        )
                        selectedTab = 0
                    }
                )
            }
        }

        // Diálogo para Requerir Motivo Obligatorio de Rechazo (US10)
        if (showRejectDialog && selectedReportForRejection != null) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = CardBackground,
                title = {
                    Text("Rechazar Lote de Producto", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Ingresa el motivo obligatorio del rechazo previo a descartar el lote.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = rejectionInputText,
                            onValueChange = { rejectionInputText = it },
                            label = { Text("Motivo de rechazo *", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ErrorRed,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = BackgroundDark,
                                unfocusedContainerColor = BackgroundDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionInputText.isNotBlank()) {
                                selectedReportForRejection?.status = QualityStatus.REJECTED
                                selectedReportForRejection?.rejectionReason = rejectionInputText
                                rejectionInputText = ""
                                showRejectDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = TextWhite),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirmar Rechazo", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancelar", color = TextMuted)
                    }
                }
            )
        }
    }
}

// Tarjeta para Validar Lote (US10 + US53)
@Composable
private fun QualityReportCard(
    report: QualityReport,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.lotCode, color = AccentLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (report.isPendingSync) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF332B14))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Pendiente de sincronización", color = Color(0xFFFFC107), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val (badgeBg, badgeText, statusLabel) = when (report.status) {
                QualityStatus.PENDING -> Triple(Color(0xFF332B14), Color(0xFFFFC107), "Pendiente")
                QualityStatus.APPROVED -> Triple(Color(0xFF1B331E), SuccessGreen, "Aprobado")
                QualityStatus.REJECTED -> Triple(Color(0xFF3B1C1C), ErrorRed, "Rechazado")
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(statusLabel, color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(report.productName, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Origen: ${report.producerName}", color = TextMuted, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Temp: ${report.temp}", color = TextMuted, fontSize = 12.sp)
            Text("Humedad: ${report.humidity}", color = TextMuted, fontSize = 12.sp)
        }

        if (report.status == QualityStatus.REJECTED && report.rejectionReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Motivo de rechazo: ${report.rejectionReason}", color = ErrorRed, fontSize = 12.sp)
        }

        if (report.status == QualityStatus.PENDING) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = BorderStroke(1.dp, ErrorRed),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rechazar")
                }
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BackgroundDark),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Aprobar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Formulario de inspección de campo offline (US53)
@Composable
private fun FieldInspectionForm(
    onSaveInspection: (lot: String, product: String, temp: String, hum: String) -> Unit
) {
    var lotInput by remember { mutableStateOf("") }
    var productInput by remember { mutableStateOf("") }
    var tempInput by remember { mutableStateOf("") }
    var humInput by remember { mutableStateOf("") }

    var tempError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Inspección en Campo", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B331E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Modo Offline", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = lotInput,
            onValueChange = { lotInput = it },
            label = { Text("Código de Lote *", color = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                focusedBorderColor = AccentLime, unfocusedBorderColor = CardBorder,
                focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = productInput,
            onValueChange = { productInput = it },
            label = { Text("Producto (ej. Palta Hass)", color = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                focusedBorderColor = AccentLime, unfocusedBorderColor = CardBorder,
                focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Validación de rango de temperatura (US53 - Escenario 2)
        OutlinedTextField(
            value = tempInput,
            onValueChange = {
                tempInput = it
                val tempVal = it.toDoubleOrNull()
                tempError = tempVal != null && (tempVal < 0 || tempVal > 40)
            },
            label = { Text("Temperatura (°C)", color = TextMuted) },
            isError = tempError,
            supportingText = {
                if (tempError) {
                    Text("Valor fuera de rango permitido (0°C a 40°C)", color = ErrorRed)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                focusedBorderColor = if (tempError) ErrorRed else AccentLime,
                unfocusedBorderColor = if (tempError) ErrorRed else CardBorder,
                focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = humInput,
            onValueChange = { humInput = it },
            label = { Text("Humedad (%)", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                focusedBorderColor = AccentLime, unfocusedBorderColor = CardBorder,
                focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (lotInput.isNotBlank() && !tempError) {
                    onSaveInspection(
                        lotInput,
                        productInput.ifBlank { "Producto General" },
                        tempInput.ifBlank { "5" },
                        humInput.ifBlank { "80" }
                    )
                }
            },
            enabled = !tempError && lotInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BackgroundDark),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Guardar", fontWeight = FontWeight.Bold)
        }
    }
}