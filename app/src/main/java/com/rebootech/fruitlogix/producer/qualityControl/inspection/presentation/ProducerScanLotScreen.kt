package com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.rebootech.fruitlogix.producer.qualityControl.inspection.data.InspectionOrderRepository
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

sealed class ScannerState {
    object Scanning : ScannerState()
    object MatchConfirmed : ScannerState()
    object ManualEntry : ScannerState()
    object PermissionDenied : ScannerState()
    object CodeNotRecognized : ScannerState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerScanLotScreen(
    orderId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToInspection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentState by remember { mutableStateOf<ScannerState>(ScannerState.Scanning) }
    var manualCodeInput by remember { mutableStateOf("LOT-HA-09") }
    var isTorchOn by remember { mutableStateOf(false) }

    val activeOrderId = orderId?.takeIf { it.isNotBlank() }?.replace("#", "") ?: "1048"
    val orderData = remember(activeOrderId) { InspectionOrderRepository.getOrderById(activeOrderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan Lot Qr / Barcode",
                        style = FruitLogixTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FruitLogixTheme.colors.textOnLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back", tint = FruitLogixTheme.colors.textOnLight)
                    }
                },
                actions = {
                    IconButton(onClick = { isTorchOn = !isTorchOn }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_flash),
                            contentDescription = "Torch",
                            tint = if (isTorchOn) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.textOnLight
                        )
                    }
                    IconButton(onClick = { currentState = ScannerState.PermissionDenied }) {
                        Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = "Profile", tint = FruitLogixTheme.colors.textOnLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FruitLogixTheme.colors.bg)
            )
        },
        containerColor = FruitLogixTheme.colors.bg
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fondo de cámara simulado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(
                            2.dp,
                            if (currentState is ScannerState.CodeNotRecognized) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentState) {
                        is ScannerState.Scanning -> {
                            Text("LOOKING FOR LOT QR OR GS1", color = FruitLogixTheme.colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(8.dp))
                        }
                        is ScannerState.MatchConfirmed -> {
                            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(FruitLogixTheme.colors.primary), contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary)
                            }
                        }
                        is ScannerState.CodeNotRecognized -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painter = painterResource(id = R.drawable.ic_alert), contentDescription = null, tint = FruitLogixTheme.colors.dangerStrong, modifier = Modifier.size(36.dp))
                                Text("UNREADABLE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        else -> {
                            Text("SCANNER PAUSED", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Panel Inferior con fondo verde oscuro (surfaceDark) para excelente contraste
            Surface(
                color = FruitLogixTheme.colors.surfaceDark,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f)).align(Alignment.CenterHorizontally))

                    when (currentState) {
                        is ScannerState.Scanning -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(FruitLogixTheme.colors.primary))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera sensor active · Auto-detect", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                                }
                                Text("🔊 Beep on scan", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.primary)
                            }

                            // Tarjeta interna con contraste optimizado sobre el fondo verde oscuro
                            Surface(color = FruitLogixTheme.colors.bg.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("ACTIVE LOGISTICS ORDER", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                                        Text("Slot 04", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Order #${orderData.orderId}", style = FruitLogixTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${orderData.clientName} · ${orderData.productName} ${orderData.netWeight}", style = FruitLogixTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Expected Pallets\n3 of 8 Scanned", style = FruitLogixTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("Packhouse Bay\nChamber C-North", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = { currentState = ScannerState.ManualEntry },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Enter code manually", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { currentState = ScannerState.MatchConfirmed }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                                    Text("Simulate Match", fontSize = 12.sp)
                                }
                                OutlinedButton(onClick = { currentState = ScannerState.CodeNotRecognized }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                                    Text("Simulate Error", fontSize = 12.sp)
                                }
                            }
                        }

                        is ScannerState.MatchConfirmed -> {
                            Surface(color = FruitLogixTheme.colors.primary, shape = RoundedCornerShape(10.dp)) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Match Confirmed! 100% ID", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                                        Text("Tag telemetry perfectly aligned with packing manifest", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.onPrimary)
                                    }
                                }
                            }

                            Surface(color = FruitLogixTheme.colors.bg.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("CONSIGNMENT", style = FruitLogixTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("Order #${orderData.orderId} · ${orderData.clientName}", fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${orderData.productName} • ${orderData.netWeight} (${orderData.cratesInfo})", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.primary)
                                }
                            }

                            Button(
                                onClick = { onNavigateToInspection(activeOrderId) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Continue to inspection →", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                            }

                            TextButton(onClick = { currentState = ScannerState.Scanning }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text("Scan another crate / box", color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        is ScannerState.ManualEntry -> {
                            Text("Enter lot code manually", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            OutlinedTextField(
                                value = manualCodeInput,
                                onValueChange = { manualCodeInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = FruitLogixTheme.colors.primary,
                                    unfocusedBorderColor = FruitLogixTheme.colors.surfaceTint
                                )
                            )
                            Button(
                                onClick = { currentState = ScannerState.MatchConfirmed },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Validate code", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                            }
                            TextButton(onClick = { currentState = ScannerState.Scanning }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text("Return to camera scanner", color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        is ScannerState.PermissionDenied -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Camera permission required", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("FruitLogix Producer needs camera access to scan lot QR tags.", style = FruitLogixTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            Button(
                                onClick = { currentState = ScannerState.Scanning },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Open settings", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                            }
                        }

                        is ScannerState.CodeNotRecognized -> {
                            Surface(color = FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.3f), shape = RoundedCornerShape(10.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Code not recognized", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.dangerStrong)
                                    Text("Scanned code is unreadable or not linked to any open order.", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                                }
                            }
                            Button(
                                onClick = { currentState = ScannerState.Scanning },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Try Again", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                            }
                            OutlinedButton(
                                onClick = { currentState = ScannerState.ManualEntry },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Enter code manually", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}