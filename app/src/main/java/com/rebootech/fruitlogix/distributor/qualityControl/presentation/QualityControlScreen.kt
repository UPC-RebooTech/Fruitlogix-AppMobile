package com.rebootech.fruitlogix.distributor.qualityControl.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

data class ShrinkageRecord(
    val id: String,
    val period: String,
    val stage: String,
    val cause: String,
    val product: String,
    val lostKg: Int,
    val estimatedCostPen: Double
)

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

    val shrinkageList = remember {
        listOf(
            ShrinkageRecord("m1", "Oct 2026", "Transporte", "Excursión térmica (>8°C)", "Arándano Biloxi", 45, 810.0),
            ShrinkageRecord("m2", "Oct 2026", "Recepción", "Daño mecánico en cajas", "Palta Hass", 62, 558.0),
            ShrinkageRecord("m3", "Oct 2026", "Cosecha", "Calibre menor al estándar", "Mango Kent", 30, 210.0),
            ShrinkageRecord("m4", "Sep 2026", "Transporte", "Demora en control de ruta", "Uva Red Globe", 28, 336.0)
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
            Text(
                text = "Control de Calidad",
                color = ColorTextOnLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Validación de lotes, inspección offline, voz y reporte de mermas",
                color = ColorAppbar,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                    listOf("Lotes", "Campo", "Voz", "Mermas").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (selectedTab == index) ColorPrimary else ColorTextMuted
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(reportsList) { report ->
                            QualityReportCard(
                                report = report,
                                onApprove = {
                                    val idx = reportsList.indexOf(report)
                                    if (idx >= 0) {
                                        reportsList[idx] = report.copy(status = QualityStatus.APPROVED)
                                    }
                                },
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
                3 -> {
                    ShrinkageReportSection(records = shrinkageList)
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
                            "Ingresa el motivo obligatorio del rechazo previo a descartar el lote (US10).",
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
                                val target = selectedReportForRejection
                                val idx = reportsList.indexOfFirst { it.id == target?.id }
                                if (idx >= 0 && target != null) {
                                    reportsList[idx] = target.copy(
                                        status = QualityStatus.REJECTED,
                                        rejectionReason = rejectionInputText
                                    )
                                }
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
private fun ShrinkageReportSection(records: List<ShrinkageRecord>) {
    var selectedPeriod by remember { mutableStateOf("Oct 2026") }
    val periods = listOf("Oct 2026", "Sep 2026", "Ago 2026 (Sin mermas)")

    val filtered = records.filter { it.period == selectedPeriod }
    val totalLostKg = filtered.sumOf { it.lostKg }
    val totalCost = filtered.sumOf { it.estimatedCostPen }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Filtrar periodo de análisis (US73):",
                color = ColorTextOnLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periods.forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ColorPrimary else ColorSurfaceDark)
                            .clickable { selectedPeriod = period }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = period,
                            color = if (isSelected) ColorOnPrimary else ColorTextOnDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            // US73 Escenario 2: Sin mermas en el periodo
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSurfaceDark)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay mermas registradas en este periodo",
                        color = ColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Todas las entregas del rango seleccionado cumplieron con el 100% de integridad.",
                        color = ColorTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Resumen de pérdidas del periodo
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ColorSurfaceDark)
                            .padding(14.dp)
                    ) {
                        Text("MERMA TOTAL", color = ColorTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$totalLostKg kg", color = ColorDangerStrong, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ColorSurfaceDark)
                            .padding(14.dp)
                    ) {
                        Text("IMPACTO ECONÓMICO", color = ColorTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("S/ ${String.format("%.2f", totalCost)}", color = ColorWarning, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(filtered, key = { it.id }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, ColorAppbar, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ColorAppbar)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Etapa: ${item.stage}", color = ColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("-${item.lostKg} kg (S/ ${String.format("%.2f", item.estimatedCostPen)})", color = ColorDangerStrong, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.product, color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Causa: ${item.cause}", color = ColorTextMuted, fontSize = 12.sp)
                }
            }
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
                        Text("Pendiente de sincronización", color = ColorWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
            Text("Inspección en Campo (US53)", color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

        Spacer(modifier = Modifier.height(4.dp))

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
            Text("Guardar Inspección Offline", fontWeight = FontWeight.Bold)
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
    var audioErrorMessage by remember { mutableStateOf<String?>(null) }

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
        Text("Reportar Incidencia con Voz (US57)", color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Graba una nota de voz de al menos 1 segundo para adjuntarla al lote.", color = ColorTextMuted, fontSize = 12.sp)

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
                else if (recordedAudioDuration != null) "Audio grabado válidamente: $recordedAudioDuration"
                else "Presiona para grabar audio",
                color = if (isRecording) ColorDangerStrong else ColorTextOnDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            audioErrorMessage?.let { err ->
                Text(text = err, color = ColorDangerStrong, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    if (!isRecording) {
                        audioErrorMessage = null
                        isRecording = true
                    } else {
                        isRecording = false
                        // US57 Escenario 2: Grabación menor a 1 segundo se descarta y muestra error
                        if (recordingSeconds < 1) {
                            recordedAudioDuration = null
                            audioErrorMessage = "Grabación interrumpida o menor a 1 segundo. Audio descartado, intenta nuevamente."
                        } else {
                            audioErrorMessage = null
                            recordedAudioDuration = String.format("%02d:%02d seg", recordingSeconds / 60, recordingSeconds % 60)
                        }
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
                if (lotInput.isNotBlank() && descInput.isNotBlank() && recordedAudioDuration != null) {
                    onSaveIncident(
                        lotInput,
                        descInput,
                        recordedAudioDuration!!
                    )
                }
            },
            enabled = lotInput.isNotBlank() && descInput.isNotBlank() && recordedAudioDuration != null,
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Registrar Incidencia con Audio", fontWeight = FontWeight.Bold)
        }
    }
}