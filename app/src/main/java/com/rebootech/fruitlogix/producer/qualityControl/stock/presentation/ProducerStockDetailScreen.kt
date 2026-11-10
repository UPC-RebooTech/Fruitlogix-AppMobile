package com.rebootech.fruitlogix.producer.qualityControl.stock.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerStockDetailScreen(
    lotId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Buscamos el ítem actual en el repositorio global basado en el ID recibido por parámetro
    val stockItem = StockRepository.stockItems.find { it.id == lotId }

    var currentTonnage by remember { mutableIntStateOf(stockItem?.availableKg ?: 85) }
    var safetyLimit by remember { mutableIntStateOf(100) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Overview • Fundo Santa Rosa (Block 3B)", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                        Text(stockItem?.productName ?: "Red Globe Grapes", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, style = FruitLogixTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = FruitLogixTheme.colors.textOnLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FruitLogixTheme.colors.bg)
            )
        },
        containerColor = FruitLogixTheme.colors.bg
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Banner de sincronización exitosa superior
            Surface(
                color = FruitLogixTheme.colors.success.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.success,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stock updated successfully. Ledger synchronized.",
                        style = FruitLogixTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = FruitLogixTheme.colors.textOnLight
                    )
                }
            }

            // 2. Tarjeta Principal de Volumen y Cámara Frigorífica
            FruitLogixCard(
                variant = CardVariant.Dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "CRITICAL STOCK",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = FruitLogixTheme.typography.labelSmall,
                                color = FruitLogixTheme.colors.dangerStrong,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("3.8°C Vault", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("LIVE LOT VOLUME", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentTonnage",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = FruitLogixTheme.typography.displayLarge,
                            color = FruitLogixTheme.colors.primary,
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("kg", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.white, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Text("Safety deficit: -15 kg below threshold", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.dangerStrong)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Reserved Orders", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Text("0 kg", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.white)
                        }
                        Column {
                            Text("Free to Commit", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Text("$currentTonnage kg", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.primary)
                        }
                    }
                }
            }

            // 3. Quick Adjustment
            FruitLogixCard(
                variant = CardVariant.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Quick Adjustment", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                        Text("Auto-sync", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.success, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("TARGET ON-HAND TONNAGE", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text("$currentTonnage kg", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdjustButton("-10 kg", modifier = Modifier.weight(1f)) { currentTonnage = (currentTonnage - 10).coerceAtLeast(0) }
                        AdjustButton("-1 kg", modifier = Modifier.weight(1f)) { currentTonnage = (currentTonnage - 1).coerceAtLeast(0) }
                        AdjustButton("+1 kg", modifier = Modifier.weight(1f)) { currentTonnage += 1 }
                        AdjustButton("+10 kg", modifier = Modifier.weight(1f)) { currentTonnage += 10 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Actualizamos los kilos en el repositorio global compartido
                            StockRepository.updateStockKg(
                                id = lotId,
                                newKg = currentTonnage
                            )
                            Toast.makeText(context, "Stock actualizado correctamente", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Save Stock Changes", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.onPrimary)
                    }
                }
            }

            // 4. Safety Alert Threshold
            FruitLogixCard(
                variant = CardVariant.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Safety Alert Threshold", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                        Surface(color = FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("Active Trigger", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.dangerStrong, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Minimum tonnage warning line", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$safetyLimit kg", style = FruitLogixTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { safetyLimit = (safetyLimit - 5).coerceAtLeast(0) }, modifier = Modifier.background(FruitLogixTheme.colors.surfaceTint, RoundedCornerShape(6.dp)).size(36.dp)) {
                                Text("-", fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { safetyLimit += 5 }, modifier = Modifier.background(FruitLogixTheme.colors.surfaceTint, RoundedCornerShape(6.dp)).size(36.dp)) {
                                Text("+", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. Audit Ledger
            FruitLogixCard(
                variant = CardVariant.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Audit Ledger", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                    Spacer(modifier = Modifier.height(12.dp))
                    AuditLedgerRow("Harvest Intake • Crew Alpha", "+120 kg", "Verified")
                    Spacer(modifier = Modifier.height(8.dp))
                    AuditLedgerRow("Order #1036 • Dispatch", "-150 kg", "Dispatched")
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = FruitLogixTheme.colors.surfaceDark,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shield_check),
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SHA-256 Verified Ledger (0x9f82...4c12)",
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
}

@Composable
fun AdjustButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.surfaceTint),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AuditLedgerRow(title: String, amount: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
            Text(status, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
        }
        Text(amount, style = FruitLogixTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (amount.startsWith("+")) FruitLogixTheme.colors.success else FruitLogixTheme.colors.dangerStrong)
    }
}