package com.rebootech.fruitlogix.logisticsMonitoring.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ArrivalStatus
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Card component for displaying an inbound arrival unit in the fleet control center.
 */
@Composable
fun ArrivalCard(
    arrival: Arrival,
    onStartReceptionClick: (String) -> Unit,
    onViewRouteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkCardBg = Color(0xFF1F2D23)
    val limeColor = Color(0xFFC2E85A)
    val amberColor = Color(0xFFFFB300)
    val textMuted = Color(0xFFA0B2A6)
    val innerCardBg = Color(0xFF28382D)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = darkCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Header: Truck ID + Plate badge + Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Unit ${arrival.unitId}",
                        style = FruitLogixTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334438))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = arrival.licensePlate,
                            style = FruitLogixTheme.typography.bodySmall.copy(
                                color = textMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Status pill
                val isAtGate = arrival.status == ArrivalStatus.AT_GATE
                val statusBg = if (isAtGate) Color(0xFF3B4D2C) else Color(0xFF4D432C)
                val statusText = if (isAtGate) "● AT GATE" else "● APPROACHING"
                val statusColor = if (isAtGate) limeColor else amberColor

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = FruitLogixTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            // Driver & Transport Line
            Text(
                text = "${arrival.driverName} • Trans-Agro Express",
                style = FruitLogixTheme.typography.bodySmall.copy(
                    color = textMuted,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Cargo & Dock Spec Inner Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(innerCardBg)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = arrival.cargoDescription,
                                style = FruitLogixTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Order #${arrival.orderId}",
                                style = FruitLogixTheme.typography.bodySmall.copy(
                                    color = textMuted,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ASSIGNED DOCK",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = arrival.assignedDock,
                                style = FruitLogixTheme.typography.bodyMedium.copy(
                                    color = limeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Telemetry Tiles (Reefer Temp + Speed/E-Seal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reefer Temp Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(innerCardBg)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF334438)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logistics_snowflake),
                                contentDescription = null,
                                tint = limeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "REEFER TEMP",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = arrival.reeferTemp,
                                    style = FruitLogixTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "●",
                                    style = TextStyle(color = limeColor, fontSize = 8.sp)
                                )
                            }
                        }
                    }
                }

                // Speed / E-Seal Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(innerCardBg)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF334438)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logistics_truck),
                                contentDescription = null,
                                tint = limeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (arrival.status == ArrivalStatus.AT_GATE) "E-SEAL STATUS" else "CURRENT SPEED",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (arrival.status == ArrivalStatus.AT_GATE) arrival.eSealStatus else arrival.speedKmh,
                                style = FruitLogixTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Distance / Proximity Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (arrival.status == ArrivalStatus.AT_GATE) "PROXIMITY (GEOFENCE RING 1)" else "DISTANCE TO GATE",
                        style = FruitLogixTheme.typography.labelSmall.copy(
                            color = textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = arrival.distanceLabel,
                        style = FruitLogixTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val progress = when {
                    arrival.distanceKm <= 0.2f -> 0.95f
                    arrival.distanceKm <= 2.0f -> 0.65f
                    else -> 0.35f
                }

                LimeProgressBar(progressFraction = progress)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Action Button
            if (arrival.status == ArrivalStatus.AT_GATE) {
                Button(
                    onClick = { onStartReceptionClick(arrival.unitId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = limeColor,
                        contentColor = Color(0xFF1B2E1E)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logistics_arrive),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.geofence_sheet_btn_start),
                        style = FruitLogixTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onViewRouteClick(arrival.unitId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = innerCardBg,
                        contentColor = Color.White
                    ),
                    border = null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logistics_route),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View route",
                        style = FruitLogixTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}
