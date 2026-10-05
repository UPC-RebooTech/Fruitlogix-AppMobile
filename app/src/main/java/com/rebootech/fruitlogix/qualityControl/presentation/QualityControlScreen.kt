package com.rebootech.fruitlogix.qualityControl.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.*
import kotlinx.coroutines.delay

// Modelo para Reportes de Calidad de Lote (US10 + US53 + US57)
data class QualityReport(
    val id: String,
    val lotCode: String,
    val productName: String,
    val producerName: String,
    val temp: String,
    val humidity: String,
    var status: QualityStatus = QualityStatus.PENDING,
    var rejectionReason: String = "",
    val isPendingSync: Boolean = false,
    var audioNoteDuration: String? = null
)

enum class QualityStatus { PENDING, APPROVED, REJECTED }

@Composable
fun QualityControlScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedReportForRejection by remember { mutableStateOf<QualityReport?>(null) }
    var rejectionInputText by remember { mutableStateOf("") }

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
            .background(ColorBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Control de Calidad",
                color = ColorTextOnLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gestión de lotes e incidencias con nota de voz",
                color = ColorAppbar,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // TabRow adaptado al tema
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ColorSurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ColorSurfaceDark,
                    contentColor = ColorPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Lotes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedTab == 0) ColorPrimary else ColorTextMuted) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Campo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedTab == 1) ColorPrimary else ColorTextMuted) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Incidencias", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedTab == 2) ColorPrimary else ColorTextMuted) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(reportsList) { report ->
                            QualityReportCard(
                                report = report,
                                onApprove = { report.status = QualityStatus.APPROVED },
                                onReject = {
                                    selectedReportForRejection = report
                                    showRejectDialog = true
                                }
                            )
                        }
                    }
                }
                1 -> {
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
                                    isPendingSync = true,
                                    status = QualityStatus.PENDING
                                )
                            )
                            selectedTab = 0
                        }
                    )
                }
                2 -> {
                    VoiceIncidentReportForm(
                        onSaveIncident = { lot, description, audioDuration ->
                            reportsList.add(
                                0,
                                QualityReport(
                                    id = (reportsList.size + 1).toString(),
                                    lotCode = lot,
                                    productName = "Incidencia: $description",
                                    producerName = "Reporte por Voz",
                                    temp = "N/A",
                                    humidity = "N/A",
                                    status = QualityStatus.PENDING,
                                    audioNoteDuration = audioDuration
                                )
                            )
                            selectedTab = 0
                        }
                    )
                }
            }
        }

        if (showRejectDialog && selectedReportForRejection != null) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = ColorSurfaceDark,
                title = {
                    Text("Rechazar Lote de Producto", color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Ingresa el motivo obligatorio del rechazo previo a descartar el lote.",
                            color = ColorTextMuted,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = rejectionInputText,
                            onValueChange = { rejectionInputText = it },
                            label = { Text("Motivo de rechazo *", color = ColorTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorTextOnDark,
                                unfocusedTextColor = ColorTextOnDark,
                                focusedBorderColor = ColorDangerStrong,
                                unfocusedBorderColor = ColorAppbar,
                                focusedContainerColor = ColorAppbar,
                                unfocusedContainerColor = ColorAppbar
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
                        colors = ButtonDefaults.buttonColors(containerColor = ColorDangerStrong, contentColor = ColorTextOnDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirmar Rechazo", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancelar", color = ColorTextMuted)
                    }
                }
            )
        }
    }
}

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
            .background(ColorSurfaceDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.lotCode, color = ColorPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (report.isPendingSync) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ColorWarning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Pendiente sync", color = ColorWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val (badgeBg, badgeText, statusLabel) = when (report.status) {
                QualityStatus.PENDING -> Triple(ColorWarning.copy(alpha = 0.2f), ColorWarning, "Pendiente")
                QualityStatus.APPROVED -> Triple(ColorSuccess.copy(alpha = 0.2f), ColorSuccess, "Aprobado")
                QualityStatus.REJECTED -> Triple(ColorDangerStrong.copy(alpha = 0.2f), ColorDangerStrong, "Rechazado")
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
        Text(report.productName, color = ColorTextOnDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Origen: ${report.producerName}", color = ColorTextMuted, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Temp: ${report.temp}", color = ColorTextMuted, fontSize = 12.sp)
            Text("Humedad: ${report.humidity}", color = ColorTextMuted, fontSize = 12.sp)
        }

        if (report.audioNoteDuration != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorAppbar)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    contentDescription = "Nota de voz",
                    tint = ColorPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nota de voz adjunta (${report.audioNoteDuration})",
                    color = ColorPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (report.status == QualityStatus.REJECTED && report.rejectionReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Motivo de rechazo: ${report.rejectionReason}", color = ColorDangerStrong, fontSize = 12.sp)
        }

        if (report.status == QualityStatus.PENDING) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorDangerStrong),
                    border = BorderStroke(1.dp, ColorDangerStrong),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rechazar")
                }
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Aprobar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
            .background(ColorSurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Inspección en Campo", color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorSuccess.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Modo Offline", color = ColorSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = lotInput,
            onValueChange = { lotInput = it },
            label = { Text("Código de Lote *", color = ColorTextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorTextOnDark, unfocusedTextColor = ColorTextOnDark,
                focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorAppbar,
                focusedContainerColor = ColorAppbar, unfocusedContainerColor = ColorAppbar
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = productInput,
            onValueChange = { productInput = it },
            label = { Text("Producto (ej. Palta Hass)", color = ColorTextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorTextOnDark, unfocusedTextColor = ColorTextOnDark,
                focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorAppbar,
                focusedContainerColor = ColorAppbar, unfocusedContainerColor = ColorAppbar
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tempInput,
            onValueChange = {
                tempInput = it
                val tempVal = it.toDoubleOrNull()
                tempError = tempVal != null && (tempVal < 0 || tempVal > 40)
            },
            label = { Text("Temperatura (°C)", color = ColorTextMuted) },
            isError = tempError,
            supportingText = {
                if (tempError) {
                    Text("Valor fuera de rango permitido (0°C a 40°C)", color = ColorDangerStrong)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorTextOnDark, unfocusedTextColor = ColorTextOnDark,
                focusedBorderColor = if (tempError) ColorDangerStrong else ColorPrimary,
                unfocusedBorderColor = if (tempError) ColorDangerStrong else ColorAppbar,
                focusedContainerColor = ColorAppbar, unfocusedContainerColor = ColorAppbar
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = humInput,
            onValueChange = { humInput = it },
            label = { Text("Humedad (%)", color = ColorTextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorTextOnDark, unfocusedTextColor = ColorTextOnDark,
                focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorAppbar,
                focusedContainerColor = ColorAppbar, unfocusedContainerColor = ColorAppbar
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
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Guardar Inspección", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VoiceIncidentReportForm(
    onSaveIncident: (lot: String, description: String, audioDuration: String) -> Unit
) {
    var lotInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var recordedAudioDuration by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordingSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reportar Incidencia", color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Adjunta notas de voz para agilizar el registro de observaciones.", color = ColorTextMuted, fontSize = 12.sp)

        OutlinedTextField(
            value = lotInput,
            onValueChange = { lotInput = it },
            label = { Text("Código de Lote *", color = ColorTextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorTextOnDark, unfocusedTextColor = ColorTextOnDark,
                focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorAppbar,
                focusedContainerColor = ColorAppbar, unfocusedContainerColor = ColorAppbar
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descInput,
            onValueChange = { descInput = it },
            label = { Text("Descripción del Problema *", color = ColorTextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorTextOnDark, unfocusedTextColor = ColorTextOnDark,
                focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorAppbar,
                focusedContainerColor = ColorAppbar, unfocusedContainerColor = ColorAppbar
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ColorAppbar)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val formattedCurrentTime = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)

            Text(
                text = if (isRecording) "Grabando nota de voz ($formattedCurrentTime seg)..."
                else if (recordedAudioDuration != null) "Audio grabado: $recordedAudioDuration"
                else "Presiona para grabar audio",
                color = if (isRecording) ColorDangerStrong else ColorTextOnDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    if (!isRecording) {
                        isRecording = true
                    } else {
                        isRecording = false
                        recordedAudioDuration = String.format("%02d:%02d seg", recordingSeconds / 60, recordingSeconds % 60)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) ColorDangerStrong else ColorSurfaceDark,
                    contentColor = ColorTextOnDark
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = null,
                        tint = ColorTextOnDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "Detener Grabación" else "Iniciar Grabación")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (lotInput.isNotBlank() && descInput.isNotBlank()) {
                    val finalDuration = recordedAudioDuration ?: "00:05 seg"
                    onSaveIncident(
                        lotInput,
                        descInput,
                        finalDuration
                    )
                }
            },
            enabled = lotInput.isNotBlank() && descInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Registrar Incidencia", fontWeight = FontWeight.Bold)
        }
    }
}