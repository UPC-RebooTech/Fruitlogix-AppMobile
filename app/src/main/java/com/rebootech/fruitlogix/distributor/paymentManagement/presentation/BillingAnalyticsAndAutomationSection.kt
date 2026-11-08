package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.shared.ui.theme.*
import java.util.Locale

data class ProposedCreditNote(
    val id: String,
    val invoiceId: String,
    val clientName: String,
    val reason: String,
    val suggestedAmountPen: Double,
    val isApproved: Boolean = false
)

data class PricePointHistory(
    val monthLabel: String,
    val pricePerKgPen: Float
)

data class ClientPurchaseSummary(
    val clientName: String,
    val periodMode: String, // "Semanal" o "Mensual"
    val totalKg: Int,
    val totalAmountPen: Double,
    val ordersCount: Int
)

@Composable
fun BillingAnalyticsAndAutomationSection(
    onAutoInvoiceCreated: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    var feedbackBanner by remember { mutableStateOf<String?>(null) }
    var errorBanner by remember { mutableStateOf<String?>(null) }

    // Modal US66: Factura automática al recibir (Kilos × Precio pactado)
    var showAutoInvoiceDialog by remember { mutableStateOf(false) }

    // Lista de Notas de Crédito Propuestas (US67)
    val creditNotes = remember {
        mutableStateListOf(
            ProposedCreditNote(
                id = "NC-2026-01",
                invoiceId = "INV-1042",
                clientName = "Supermercados Lima Norte",
                reason = "Excursión térmica en ruta (9.4°C por 25 min) y 45 kg de arándano rechazado",
                suggestedAmountPen = 405.00
            ),
            ProposedCreditNote(
                id = "NC-2026-02",
                invoiceId = "INV-1034",
                clientName = "Distribuidora Central",
                reason = "Merma por daño mecánico en recepción (62 kg de Palta Hass)",
                suggestedAmountPen = 558.00
            )
        )
    }

    var creditNoteToAdjust by remember { mutableStateOf<ProposedCreditNote?>(null) }

    // Estado US68: Evolución de precios por producto y cliente
    var selectedProduct by remember { mutableStateOf("Palta Hass") }
    var selectedClientForPrice by remember { mutableStateOf("Supermercados Lima Norte") }

    val priceHistoryMap = remember {
        mapOf(
            "Palta Hass|Supermercados Lima Norte" to listOf(
                PricePointHistory("Jun", 8.2f),
                PricePointHistory("Jul", 8.5f),
                PricePointHistory("Ago", 9.0f),
                PricePointHistory("Set", 8.8f),
                PricePointHistory("Oct", 9.4f)
            ),
            "Arándano Biloxi|Supermercados Lima Norte" to listOf(
                PricePointHistory("Jun", 16.5f),
                PricePointHistory("Jul", 17.2f),
                PricePointHistory("Ago", 18.0f),
                PricePointHistory("Set", 17.8f),
                PricePointHistory("Oct", 18.5f)
            ),
            "Mango Kent|Mercados Lima Sur" to listOf(
                PricePointHistory("Jun", 6.0f),
                PricePointHistory("Jul", 6.4f),
                PricePointHistory("Ago", 6.8f),
                PricePointHistory("Set", 7.1f),
                PricePointHistory("Oct", 7.5f)
            )
        )
    }

    // Estado US69: Resumen de compras por cliente (Semanal / Mensual)
    var selectedGrouping by remember { mutableStateOf("Mensual") }
    var selectedClientSummary by remember { mutableStateOf("Supermercados Lima Norte") }

    val purchaseSummaries = remember {
        listOf(
            ClientPurchaseSummary("Supermercados Lima Norte", "Semanal", 1850, 17390.00, 3),
            ClientPurchaseSummary("Supermercados Lima Norte", "Mensual", 7420, 69748.00, 12),
            ClientPurchaseSummary("Mercados Lima Sur", "Semanal", 920, 6900.00, 2),
            ClientPurchaseSummary("Mercados Lima Sur", "Mensual", 3800, 28500.00, 8)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FruitLogixTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        feedbackBanner?.let { msg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorSurfaceDark)
                    .border(1.dp, ColorPrimary, RoundedCornerShape(12.dp))
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorDanger)
                    .border(1.dp, ColorDangerStrong, RoundedCornerShape(12.dp))
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
                    Text("OK", color = ColorTextOnDark, fontSize = 12.sp)
                }
            }
        }

        // 1. US66: Emisión Automática de Factura al Confirmar Recepción (Kilos × Precio Pactado)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FACTURACIÓN AUTOMÁTICA (US66)",
                            color = ColorTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cálculo automático: Kilos recibidos × Precio pactado",
                            color = ColorTextOnDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { showAutoInvoiceDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Emitir (US66)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 2. US67: Notas de Crédito Propuestas por Fruta Rechazada o Excursión Térmica
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "NOTAS DE CRÉDITO PROPUESTAS (US67)",
                    color = ColorWarning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sugerencias automáticas por fruta rechazada o excursión de temperatura",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )

                if (creditNotes.isEmpty()) {
                    // US67 Escenario 2: Pedido sin observaciones
                    Text(
                        text = "✓ Todos los pedidos recientes fueron recibidos sin rechazos ni excursiones térmicas. No hay notas de crédito pendientes.",
                        color = ColorSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    creditNotes.forEach { note ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorAppbar)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${note.id} • Factura ${note.invoiceId}",
                                    color = ColorPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "S/ ${String.format(Locale.US, "%.2f", note.suggestedAmountPen)}",
                                    color = ColorWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Cliente: ${note.clientName}",
                                color = ColorTextOnDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Motivo: ${note.reason}",
                                color = ColorTextMuted,
                                fontSize = 11.sp
                            )

                            if (note.isApproved) {
                                Text(
                                    text = "✓ Nota de crédito aprobada y descontada de la factura ${note.invoiceId}",
                                    color = ColorSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { creditNoteToAdjust = note },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTextOnDark)
                                    ) {
                                        Text("Ajustar Monto", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            val idx = creditNotes.indexOf(note)
                                            if (idx >= 0) {
                                                creditNotes[idx] = note.copy(isApproved = true)
                                                feedbackBanner = "Nota de crédito ${note.id} por S/ ${String.format(Locale.US, "%.2f", note.suggestedAmountPen)} aprobada."
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ColorPrimary,
                                            contentColor = ColorOnPrimary
                                        )
                                    ) {
                                        Text("Aprobar NC", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. US69: Resumen de Compras por Cliente (Agrupado por Semana o Mes)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "RESUMEN DE COMPRAS POR CLIENTE (US69)",
                    color = ColorTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                // Selector de Cliente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Supermercados Lima Norte",
                        "Mercados Lima Sur",
                        "Cliente Nuevo (Sin compras)"
                    ).forEach { client ->
                        val isSelected = selectedClientSummary == client
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ColorPrimary else ColorAppbar)
                                .clickable { selectedClientSummary = client }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = client.substringBefore(" "),
                                color = if (isSelected) ColorOnPrimary else ColorTextOnDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Selector Semanal / Mensual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semanal", "Mensual").forEach { mode ->
                        val isSelected = selectedGrouping == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ColorPrimary.copy(alpha = 0.25f) else ColorAppbar)
                                .border(
                                    1.dp,
                                    if (isSelected) ColorPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedGrouping = mode }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Agrupación $mode",
                                color = if (isSelected) ColorPrimary else ColorTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val match = purchaseSummaries.find {
                    it.clientName == selectedClientSummary && it.periodMode == selectedGrouping
                }

                if (match == null) {
                    // US69 Escenario 2: Cliente sin compras en el periodo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorAppbar)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Total Comprado: 0 kg • Monto: S/ 0.00",
                            color = ColorTextOnDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Este cliente no registra compras finalizadas en el periodo $selectedGrouping seleccionado.",
                            color = ColorTextMuted,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorAppbar)
                                .padding(12.dp)
                        ) {
                            Text("VOLUMEN ($selectedGrouping)", color = ColorTextMuted, fontSize = 10.sp)
                            Text("${match.totalKg} kg", color = ColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("${match.ordersCount} pedidos cerrados", color = ColorTextOnDark, fontSize = 11.sp)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorAppbar)
                                .padding(12.dp)
                        ) {
                            Text("FACTURADO ($selectedGrouping)", color = ColorTextMuted, fontSize = 10.sp)
                            Text("S/ ${String.format(Locale.US, "%.2f", match.totalAmountPen)}", color = ColorSuccess, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(match.clientName, color = ColorTextOnDark, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 4. US68: Evolución de Precios por Kilo (Producto + Cliente)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "EVOLUCIÓN DE PRECIOS POR KILO (US68)",
                    color = ColorTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("Filtrar por Producto:", color = ColorTextOnDark, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Palta Hass", "Arándano Biloxi", "Mango Kent").forEach { prod ->
                        val isSelected = selectedProduct == prod
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ColorPrimary else ColorAppbar)
                                .clickable { selectedProduct = prod }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prod.substringBefore(" "),
                                color = if (isSelected) ColorOnPrimary else ColorTextOnDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text("Filtrar por Cliente:", color = ColorTextOnDark, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Supermercados Lima Norte", "Mercados Lima Sur").forEach { cli ->
                        val isSelected = selectedClientForPrice == cli
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ColorPrimary.copy(alpha = 0.25f) else ColorAppbar)
                                .border(
                                    1.dp,
                                    if (isSelected) ColorPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedClientForPrice = cli }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cli.replace("Supermercados ", "").replace("Mercados ", ""),
                                color = if (isSelected) ColorPrimary else ColorTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val key = "$selectedProduct|$selectedClientForPrice"
                val points = priceHistoryMap[key] ?: emptyList()

                if (points.isEmpty()) {
                    // US68 Escenario 2: Sin historial de precios para la combinación
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorAppbar)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay datos históricos de precios para '$selectedProduct' con '$selectedClientForPrice'.",
                            color = ColorTextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    PriceEvolutionChart(points = points)
                }
            }
        }
    }

    // Modal US66: Calcular y emitir factura automática
    if (showAutoInvoiceDialog) {
        AutoInvoiceDialog(
            onDismiss = { showAutoInvoiceDialog = false },
            onInvoiceGenerated = { newInvoice ->
                showAutoInvoiceDialog = false
                errorBanner = null
                feedbackBanner = "Factura ${newInvoice.id} generada automáticamente por S/ ${String.format(Locale.US, "%.2f", newInvoice.amount)} y registrada en Cuentas por Cobrar."
                onAutoInvoiceCreated(newInvoice)
            },
            onPendingPricingAlert = { msg ->
                showAutoInvoiceDialog = false
                feedbackBanner = null
                errorBanner = msg
            }
        )
    }

    // Modal US67: Ajustar monto de Nota de Crédito
    creditNoteToAdjust?.let { target ->
        var amountInput by remember(target) { mutableStateOf(target.suggestedAmountPen.toString()) }
        AlertDialog(
            onDismissRequest = { creditNoteToAdjust = null },
            containerColor = ColorSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Ajustar Nota de Crédito (${target.id})", color = ColorTextOnDark, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Monto de compensación (S/)", color = ColorTextMuted) },
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = amountInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            val idx = creditNotes.indexOfFirst { it.id == target.id }
                            if (idx >= 0) {
                                creditNotes[idx] = target.copy(
                                    suggestedAmountPen = parsed,
                                    isApproved = true
                                )
                            }
                            feedbackBanner = "Nota de crédito ${target.id} ajustada a S/ ${String.format(Locale.US, "%.2f", parsed)} y aprobada."
                            creditNoteToAdjust = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar y Aprobar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { creditNoteToAdjust = null }) {
                    Text("Cancelar", color = ColorTextMuted)
                }
            }
        )
    }
}

@Composable
private fun PriceEvolutionChart(points: List<PricePointHistory>) {
    val minPrice = (points.minOfOrNull { it.pricePerKgPen } ?: 0f) - 0.5f
    val maxPrice = (points.maxOfOrNull { it.pricePerKgPen } ?: 10f) + 0.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorAppbar)
            .padding(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            if (points.size < 2) return@Canvas
            val stepX = size.width / (points.size - 1)
            val rangeY = (maxPrice - minPrice).coerceAtLeast(1f)

            val path = Path()
            points.forEachIndexed { index, pt ->
                val x = index * stepX
                val y = size.height - ((pt.pricePerKgPen - minPrice) / rangeY) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = ColorPrimary,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            points.forEachIndexed { index, pt ->
                val x = index * stepX
                val y = size.height - ((pt.pricePerKgPen - minPrice) / rangeY) * size.height
                drawCircle(
                    color = ColorPrimary,
                    radius = 8f,
                    center = Offset(x, y)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "S/ ${pt.pricePerKgPen}",
                        color = ColorPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pt.monthLabel,
                        color = ColorTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoInvoiceDialog(
    onDismiss: () -> Unit,
    onInvoiceGenerated: (Invoice) -> Unit,
    onPendingPricingAlert: (String) -> Unit
) {
    var clientInput by remember { mutableStateOf("Supermercados Lima Norte") }
    var kilosInput by remember { mutableStateOf("1200") }
    var pricePerKgInput by remember { mutableStateOf("8.50") }

    val kilos = kilosInput.toDoubleOrNull() ?: 0.0
    val price = pricePerKgInput.toDoubleOrNull()
    val totalCalculated = if (price != null && price > 0) kilos * price else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Factura Automática al Recibir (US66)",
                color = ColorTextOnDark,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Al confirmarse la recepción, el sistema multiplica los kilos recibidos por el precio pactado. Si dejas el precio vacío, se marcará como 'Facturación pendiente' (Escenario 2).",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = clientInput,
                    onValueChange = { clientInput = it },
                    label = { Text("Cliente Comercial *", color = ColorTextMuted) },
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
                    value = kilosInput,
                    onValueChange = { kilosInput = it },
                    label = { Text("Kilos Efectivamente Recibidos (kg) *", color = ColorTextMuted) },
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
                    value = pricePerKgInput,
                    onValueChange = { pricePerKgInput = it },
                    label = { Text("Precio Pactado por Kilo (S/)", color = ColorTextMuted) },
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

                Text(
                    text = if (price != null && price > 0) {
                        "Total calculado: S/ ${String.format(Locale.US, "%.2f", totalCalculated)}"
                    } else {
                        "⚠ Precio pactado no disponible: Se detendrá la emisión (US66 Escenario 2)."
                    },
                    color = if (price != null && price > 0) ColorPrimary else ColorWarning,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (price == null || price <= 0.0) {
                        // US66 Escenario 2: Precio pactado no disponible
                        onPendingPricingAlert(
                            "Emisión detenida (US66 Escenario 2): El pedido de '$clientInput' no tiene precio pactado. Marcado como 'Facturación pendiente'."
                        )
                    } else if (kilos > 0) {
                        val generated = Invoice(
                            id = "INV-${(1050..1099).random()}",
                            counterpartyName = clientInput.ifBlank { "Cliente Comercial" },
                            amount = totalCalculated,
                            dateLabel = "Hoy • Auto",
                            type = InvoiceType.RECEIVABLE,
                            status = InvoiceStatus.PENDING
                        )
                        onInvoiceGenerated(generated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Procesar Recepción", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextMuted)
            }
        }
    )
}