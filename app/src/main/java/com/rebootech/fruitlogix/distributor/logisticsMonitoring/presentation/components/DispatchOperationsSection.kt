package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DispatchDetail
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.isReeferWithinThreshold
import com.rebootech.fruitlogix.shared.ui.theme.*
import java.security.MessageDigest

enum class DeliveryLifecycleState(val label: String) {
    PENDING("Pendiente de salida"),
    IN_ROUTE("En Camino"),
    DELIVERED("Finalizado con evidencia"),
    INCIDENT("Incidencia en entrega")
}

data class LotStageEvent(
    val stageName: String,
    val timestamp: String,
    val responsible: String,
    val isCompleted: Boolean
)

@Composable
fun DispatchOperationsSection(
    detail: DispatchDetail,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Estado del pedido (US14)
    var currentDeliveryState by remember(detail.unitId) {
        mutableStateOf(DeliveryLifecycleState.IN_ROUTE)
    }
    var currentEta by remember(detail.unitId) { mutableStateOf(detail.dockEta) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }
    var errorBanner by remember { mutableStateOf<String?>(null) }

    // Estados de modales
    var showReeferDialog by remember { mutableStateOf(false) }
    var showDelayDialog by remember { mutableStateOf(false) }
    var showFinalDeliveryDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }

    val numericCurrentTemp = detail.currentTemp
        .replace("°C", "")
        .replace("C", "")
        .trim()
        .toDoubleOrNull() ?: 3.2

    // US72: Línea de tiempo del lote (Cosechado -> Inspeccionado -> Cargado -> Recibido -> Facturado)
    val isDelivered = currentDeliveryState == DeliveryLifecycleState.DELIVERED
    val lotTimeline = remember(currentDeliveryState) {
        listOf(
            LotStageEvent("1. Cosechado", "01 Oct • 05:30 AM", "Fundo Origen (${detail.originName})", true),
            LotStageEvent("2. Inspeccionado", "01 Oct • 07:15 AM", "Ing. Calidad • Lote Aprobado", true),
            LotStageEvent("3. Cargado en Reefer", "01 Oct • 08:40 AM", "${detail.driverName} (${detail.licensePlate})", true),
            LotStageEvent(
                "4. Recibido en Destino",
                if (isDelivered) "01 Oct • Confirmado con Firma/Foto/GPS" else "Por realizar",
                detail.destinationName,
                isDelivered
            ),
            LotStageEvent(
                "5. Facturado",
                if (isDelivered) "01 Oct • Factura emitida automáticamente" else "Por realizar",
                "Sistema Billing FruitLogix",
                isDelivered
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mensajes de retroalimentación o bloqueo
        feedbackBanner?.let { msg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorSurfaceDark)
                    .border(1.dp, ColorPrimary, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = msg,
                    color = ColorPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { feedbackBanner = null }) {
                    Text("OK", color = ColorTextOnDark, fontSize = 12.sp)
                }
            }
        }

        errorBanner?.let { err ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorDanger)
                    .border(1.dp, ColorDangerStrong, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = err,
                    color = ColorTextOnDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { errorBanner = null }) {
                    Text("Cerrar", color = ColorTextOnDark, fontSize = 12.sp)
                }
            }
        }

        // Tarjeta 1: Control Operativo de Entrega, Reefer y Retrasos (US14, US17, US64, US71)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OPERACIONES DE DESPACHO",
                            color = ColorTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estado: ${currentDeliveryState.label} • ETA: $currentEta",
                            color = ColorPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Selector manual de estado con validación de transición inválida (US14)
                Text(
                    text = "Actualizar Estado Manualmente (US14):",
                    color = ColorTextOnDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        DeliveryLifecycleState.PENDING to "Pendiente",
                        DeliveryLifecycleState.IN_ROUTE to "En Camino",
                        DeliveryLifecycleState.DELIVERED to "Entregado"
                    ).forEach { (targetState, label) ->
                        val isSelected = currentDeliveryState == targetState
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ColorPrimary else ColorAppbar)
                                .clickable {
                                    // US14 Escenario 2: Bloquear transición directa de Pendiente a Entregado
                                    if (currentDeliveryState == DeliveryLifecycleState.PENDING &&
                                        targetState == DeliveryLifecycleState.DELIVERED
                                    ) {
                                        errorBanner = "Transición inválida (US14): No puedes pasar de 'Pendiente' a 'Entregado' sin estar 'En Camino'."
                                        feedbackBanner = null
                                    } else if (targetState == DeliveryLifecycleState.DELIVERED) {
                                        showFinalDeliveryDialog = true
                                    } else {
                                        currentDeliveryState = targetState
                                        errorBanner = null
                                        feedbackBanner = "Estado actualizado a '${targetState.label}'. Cliente notificado."
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ColorOnPrimary else ColorTextOnDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = ColorAppbar, thickness = 0.7.dp)

                // Botones de acciones operativas: US64, US17, US16/52/56, US71
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showReeferDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorPrimary)
                    ) {
                        Text("Verificar Reefer (US64)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showDelayDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorWarning)
                    ) {
                        Text("Notificar Retraso (US17)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showFinalDeliveryDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary
                        )
                    ) {
                        Text("Entrega Final (US16/52)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showCertificateDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorAppbar,
                            contentColor = ColorTextOnDark
                        )
                    ) {
                        Text("Certificado Frío (US71)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tarjeta 2: Línea de Tiempo del Lote (US72)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LÍNEA DE TIEMPO DEL LOTE (US72)",
                    color = ColorTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Trazabilidad de extremo a extremo • Orden #${detail.orderId}",
                    color = ColorTextOnDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                lotTimeline.forEach { stage ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (stage.isCompleted) ColorSuccess else ColorWarning)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stage.stageName,
                                color = if (stage.isCompleted) ColorTextOnDark else ColorTextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${stage.timestamp} • ${stage.responsible}",
                                color = if (stage.isCompleted) ColorPrimary else ColorWarning,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal US64: Verificar Reefer antes de despachar
    if (showReeferDialog) {
        ReeferPreCheckDialog(
            unitId = detail.unitId,
            currentTemp = numericCurrentTemp,
            minAllowed = detail.minTempThreshold.toDouble(),
            maxAllowed = detail.maxTempThreshold.toDouble(),
            onDismiss = { showReeferDialog = false },
            onApproved = {
                showReeferDialog = false
                errorBanner = null
                feedbackBanner = "Verificación de Reefer aprobada (${numericCurrentTemp}°C dentro del umbral). Despacho autorizado."
            }
        )
    }

    // Modal US17: Notificar retraso en entrega con validación de nueva ETA
    if (showDelayDialog) {
        NotifyDelayDialog(
            currentEta = currentEta,
            onDismiss = { showDelayDialog = false },
            onSendPush = { newEta, reason ->
                currentEta = "$newEta (Retraso)"
                showDelayDialog = false
                errorBanner = null
                feedbackBanner = "Alerta push enviada al cliente comercial: Nuevo ETA $newEta • Motivo: $reason"
            }
        )
    }

    // Modal US16 + US52 + US56: Registrar entrega final con Firma Digital, Foto obligatoria y GPS
    if (showFinalDeliveryDialog) {
        FinalDeliveryProofDialog(
            orderId = detail.orderId,
            destinationName = detail.destinationName,
            onDismiss = { showFinalDeliveryDialog = false },
            onCompleteSuccess = { gpsLabel ->
                currentDeliveryState = DeliveryLifecycleState.DELIVERED
                showFinalDeliveryDialog = false
                errorBanner = null
                feedbackBanner = "Pedido #${detail.orderId} Finalizado con evidencia (Firma + Foto sellada + GPS $gpsLabel). Factura generada."
            },
            onRejectDelivery = { reason ->
                currentDeliveryState = DeliveryLifecycleState.INCIDENT
                showFinalDeliveryDialog = false
                feedbackBanner = null
                errorBanner = "Entrega rechazada en destino (#${detail.orderId}). Motivo registrado: $reason"
            }
        )
    }

    // Modal US71: Certificado de cadena de frío con Hash SHA-256 y Share Sheet
    if (showCertificateDialog) {
        ColdChainCertificateDialog(
            detail = detail,
            onDismiss = { showCertificateDialog = false },
            onShareCertificate = { shareBody ->
                showCertificateDialog = false
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Certificado de Cadena de Frío - ${detail.orderId}")
                    putExtra(Intent.EXTRA_TEXT, shareBody)
                }
                context.startActivity(
                    Intent.createChooser(sendIntent, "Compartir Certificado de Cadena de Frío")
                )
            }
        )
    }
}

@Composable
private fun ReeferPreCheckDialog(
    unitId: String,
    currentTemp: Double,
    minAllowed: Double,
    maxAllowed: Double,
    onDismiss: () -> Unit,
    onApproved: () -> Unit
) {
    var simulatedTemp by remember { mutableStateOf(currentTemp.toString()) }
    val parsedTemp = simulatedTemp.toDoubleOrNull() ?: currentTemp
    val isWithinBounds = parsedTemp >= minAllowed && isReeferWithinThreshold(parsedTemp, maxAllowed)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Verificar Reefer Pre-Despacho (US64)",
                color = ColorTextOnDark,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Unidad: $unitId • Rango requerido: ${minAllowed}°C a ${maxAllowed}°C",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = simulatedTemp,
                    onValueChange = { simulatedTemp = it },
                    label = { Text("Lectura actual del Reefer (°C)", color = ColorTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorTextOnDark,
                        unfocusedTextColor = ColorTextOnDark,
                        focusedBorderColor = if (isWithinBounds) ColorPrimary else ColorDangerStrong,
                        unfocusedBorderColor = ColorAppbar,
                        focusedContainerColor = ColorAppbar,
                        unfocusedContainerColor = ColorAppbar
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isWithinBounds) {
                    Text(
                        text = "✓ Conformidad confirmada: El contenedor refrigerado está en el rango óptimo.",
                        color = ColorSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "✗ Despacho bloqueado: La temperatura actual (${parsedTemp}°C) está fuera del rango permitido (${minAllowed}°C a ${maxAllowed}°C).",
                        color = ColorDangerStrong,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApproved,
                enabled = isWithinBounds,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Autorizar Salida", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun NotifyDelayDialog(
    currentEta: String,
    onDismiss: () -> Unit,
    onSendPush: (newEta: String, reason: String) -> Unit
) {
    var newEtaInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("Congestión vehicular en control de peaje") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Notificar Retraso en Entrega (US17)",
                color = ColorTextOnDark,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ETA original: $currentEta. Ingresa la nueva fecha y hora estimada para enviar una alerta push al Cliente Comercial.",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )

                validationError?.let { err ->
                    Text(
                        text = err,
                        color = ColorDangerStrong,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = newEtaInput,
                    onValueChange = {
                        newEtaInput = it
                        validationError = null
                    },
                    label = { Text("Nueva Fecha/Hora Estimada (ej. Hoy 16:15 PET) *", color = ColorTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorTextOnDark,
                        unfocusedTextColor = ColorTextOnDark,
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorAppbar,
                        focusedContainerColor = ColorAppbar,
                        unfocusedContainerColor = ColorAppbar
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Motivo del retraso *", color = ColorTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorTextOnDark,
                        unfocusedTextColor = ColorTextOnDark,
                        focusedBorderColor = ColorPrimary,
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
                    if (newEtaInput.isBlank()) {
                        validationError = "Debes ingresar una nueva fecha y hora estimada de llegada para notificar al cliente."
                    } else {
                        onSendPush(newEtaInput.trim(), reasonInput.ifBlank { "Demora en ruta" })
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorWarning,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Enviar Push", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun FinalDeliveryProofDialog(
    orderId: String,
    destinationName: String,
    onDismiss: () -> Unit,
    onCompleteSuccess: (gpsCoordinates: String) -> Unit,
    onRejectDelivery: (reason: String) -> Unit
) {
    val strokePoints = remember { mutableStateListOf<Offset>() }
    var hasCapturedPhoto by remember { mutableStateOf(false) }
    var simulateOutsideRadius by remember { mutableStateOf(false) }
    var manualGpsConfirmed by remember { mutableStateOf(false) }
    var isRejectMode by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gpsString = if (simulateOutsideRadius) {
        "-12.0895, -77.0412 (A 1.8 km del punto de entrega)"
    } else {
        "-12.0464, -77.0428 (En radio de $destinationName)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isRejectMode) "Rechazar Entrega (US16)" else "Confirmar Entrega Final (US16/52/56)",
                color = ColorTextOnDark,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = ColorDangerStrong,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isRejectMode) {
                    Text(
                        text = "Selecciona o escribe el motivo obligatorio por el cual el cliente rechaza la mercadería:",
                        color = ColorTextMuted,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Motivo de rechazo en destino *", color = ColorTextMuted) },
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
                } else {
                    // 1. Firma Digital Táctil (US16)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. Firma Digital del Cliente (US16):", color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { strokePoints.clear() }) {
                            Text("Limpiar", color = ColorTextMuted, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorAppbar)
                            .border(1.dp, ColorPrimary, RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    strokePoints.add(change.position)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (strokePoints.isEmpty()) {
                            Text("Dibuja la firma táctil aquí o toca para firmar", color = ColorTextMuted, fontSize = 11.sp)
                        }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (i in 1 until strokePoints.size) {
                                drawLine(
                                    color = ColorPrimary,
                                    start = strokePoints[i - 1],
                                    end = strokePoints[i],
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    if (strokePoints.isEmpty()) {
                        TextButton(
                            onClick = {
                                strokePoints.addAll(
                                    listOf(Offset(40f, 50f), Offset(120f, 35f), Offset(210f, 65f), Offset(300f, 40f))
                                )
                            }
                        ) {
                            Text("+ Simular trazo de firma o lectura QR", color = ColorPrimary, fontSize = 11.sp)
                        }
                    }

                    // 2. Evidencia Fotográfica Obligatoria (US52)
                    Text("2. Evidencia Fotográfica del Pallet (US52):", color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            hasCapturedPhoto = true
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasCapturedPhoto) ColorSuccess.copy(alpha = 0.25f) else ColorAppbar,
                            contentColor = if (hasCapturedPhoto) ColorSuccess else ColorTextOnDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (hasCapturedPhoto) "✓ Foto capturada con Sello de Agua (Fecha/Hora/GPS)" else "📷 Tomar Fotografía del Paquete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Geolocalización Automática (US56)
                    Text("3. Ubicación GPS Capturada (US56):", color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = gpsString, color = ColorTextOnDark, fontSize = 11.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = simulateOutsideRadius,
                            onCheckedChange = {
                                simulateOutsideRadius = it
                                manualGpsConfirmed = false
                            }
                        )
                        Text("Simular ubicación fuera del radio esperado", color = ColorTextMuted, fontSize = 11.sp)
                    }

                    if (simulateOutsideRadius) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorDanger)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Alerta de discrepancia GPS (US56 Escenario 2): Estás lejos de la dirección registrada.",
                                color = ColorDangerStrong,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = manualGpsConfirmed,
                                    onCheckedChange = { manualGpsConfirmed = it }
                                )
                                Text("Confirmar manualmente ubicación alternativa", color = ColorTextOnDark, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isRejectMode) {
                Button(
                    onClick = {
                        if (rejectionReason.isBlank()) {
                            errorMessage = "El motivo de rechazo es obligatorio."
                        } else {
                            onRejectDelivery(rejectionReason.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorDangerStrong, contentColor = ColorTextOnDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Rechazo", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        when {
                            strokePoints.isEmpty() -> {
                                errorMessage = "Debes capturar la firma digital del cliente o validar el código QR (US16)."
                            }
                            !hasCapturedPhoto -> {
                                errorMessage = "La evidencia fotográfica del paquete es obligatoria para completar la entrega (US52 Escenario 2)."
                            }
                            simulateOutsideRadius && !manualGpsConfirmed -> {
                                errorMessage = "Debes confirmar manualmente la discrepancia de ubicación GPS (US56 Escenario 2)."
                            }
                            else -> {
                                onCompleteSuccess(gpsString)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Finalizar Entrega", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            Row {
                if (!isRejectMode) {
                    TextButton(onClick = { isRejectMode = true; errorMessage = null }) {
                        Text("Rechazar", color = ColorDangerStrong)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = ColorTextMuted)
                }
            }
        }
    )
}

@Composable
private fun ColdChainCertificateDialog(
    detail: DispatchDetail,
    onDismiss: () -> Unit,
    onShareCertificate: (String) -> Unit
) {
    val readings = detail.detailedTemperaturePoints
    val excursions = readings.filter { it.isExcursion }
    val hashInput = "${detail.orderId}-${detail.unitId}-${detail.driverLicense}-${readings.size}"
    val sha256Hash = remember(hashInput) {
        MessageDigest.getInstance("SHA-256")
            .digest(hashInput.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(24)
            .uppercase()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Certificado de Cadena de Frío (US71)",
                color = ColorTextOnDark,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            if (readings.isEmpty()) {
                Text(
                    text = "No hay información suficiente de lecturas térmicas para emitir el certificado.",
                    color = ColorDangerStrong,
                    fontSize = 13.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Orden: #${detail.orderId} • Unidad: ${detail.unitId} (${detail.licensePlate})", color = ColorPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Carga: ${detail.cargoDescription}", color = ColorTextOnDark, fontSize = 12.sp)
                    Text("Ruta: ${detail.routeLabel}", color = ColorTextMuted, fontSize = 12.sp)
                    Text("Rango configurado: ${detail.minTempThreshold}°C a ${detail.maxTempThreshold}°C", color = ColorTextOnDark, fontSize = 12.sp)
                    Text("Total lecturas auditadas: ${readings.size} puntos", color = ColorTextOnDark, fontSize = 12.sp)
                    Text(
                        text = if (excursions.isEmpty()) "Excursiones detectadas: 0 (100% Conforme)"
                        else "Excursiones registradas: ${excursions.size} (${excursions.first().excursionTooltip ?: "Superó umbral"})",
                        color = if (excursions.isEmpty()) ColorSuccess else ColorWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    HorizontalDivider(color = ColorAppbar, modifier = Modifier.padding(vertical = 4.dp))
                    Text("HASH DE VERIFICACIÓN SHA-256:", color = ColorTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(sha256Hash, color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            if (readings.isNotEmpty()) {
                Button(
                    onClick = {
                        val certText = """
                            CERTIFICADO OFICIAL DE CADENA DE FRÍO - FRUITLOGIX
                            Orden: #${detail.orderId} | Unidad: ${detail.unitId} (${detail.licensePlate})
                            Producto: ${detail.cargoDescription}
                            Ruta: ${detail.routeLabel}
                            Umbral requerido: ${detail.minTempThreshold}°C - ${detail.maxTempThreshold}°C
                            Lecturas registradas: ${readings.size} | Excursiones: ${excursions.size}
                            Hash de Integridad: $sha256Hash
                        """.trimIndent()
                        onShareCertificate(certText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Compartir Certificado", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextMuted)
            }
        }
    )
}