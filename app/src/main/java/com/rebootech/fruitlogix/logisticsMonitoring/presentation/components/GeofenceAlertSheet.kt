package com.rebootech.fruitlogix.logisticsMonitoring.presentation.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private const val GEOFENCE_CHANNEL_ID = "geofence"
private const val GEOFENCE_NOTIF_ID = 4010

/**
 * Modal Bottom Sheet triggered when a shipment enters the warehouse geofence.
 * Displays radar animation, ETA, climate, assigned dock, payload info, and EXACTLY two action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceAlertSheet(
    arrival: Arrival,
    onDismiss: () -> Unit,
    onStartReception: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val darkSurface = Color(0xFF1F2D23)
    val innerCardBg = Color(0xFF28382D)
    val limeColor = Color(0xFFC2E85A)
    val textMuted = Color(0xFFA0B2A6)

    // Permission launcher for Android 13+ POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendGeofenceNotification(context, arrival)
        }
    }

    // Fire notification when sheet appears
    LaunchedEffect(arrival.unitId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                sendGeofenceNotification(context, arrival)
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            sendGeofenceNotification(context, arrival)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = darkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3B4D40))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 1. Header Trigger Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF334435))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(limeColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GEOFENCE TRIGGER • WAREHOUSE PERIMETER (RADIUS 2.5 KM)",
                            style = FruitLogixTheme.typography.labelSmall.copy(
                                color = limeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "LIVE GPS",
                        style = FruitLogixTheme.typography.labelSmall.copy(
                            color = textMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle & Main Title
            Text(
                text = "📍 OUTER PERIMETER BREACH DETECTED",
                style = FruitLogixTheme.typography.labelSmall.copy(
                    color = limeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.geofence_sheet_title, arrival.unitId),
                style = FruitLogixTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. ETA Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(innerCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B4D2C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logistics_clock),
                                contentDescription = null,
                                tint = limeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DOCK APPROACH ETA",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.geofence_sheet_eta,
                                    arrival.etaMinutes ?: 6
                                ),
                                style = FruitLogixTheme.typography.titleMedium.copy(
                                    color = limeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${arrival.distanceKm} km to Gate 2",
                            style = FruitLogixTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "CRUISING ${arrival.speedKmh.ifEmpty { "34 KM/H" }}",
                            style = FruitLogixTheme.typography.labelSmall.copy(
                                color = textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Radar Canvas Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(innerCardBg),
                contentAlignment = Alignment.Center
            ) {
                RadarCanvasVisualizer(unitId = arrival.unitId, assignedDock = arrival.assignedDock)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Reefer Climate & Pre-assigned Dock Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reefer Climate Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(innerCardBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REEFER CLIMATE",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3B4D2C))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NOMINAL",
                                    style = FruitLogixTheme.typography.labelSmall.copy(
                                        color = limeColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${arrival.reeferTemp} / RH ${arrival.humidity}",
                            style = FruitLogixTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                // Pre-assigned Dock Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(innerCardBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "PRE-ASSIGNED DOCK",
                            style = FruitLogixTheme.typography.labelSmall.copy(
                                color = textMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = arrival.assignedDock,
                            style = FruitLogixTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Cold Chamber 02 • Pre-Cooled",
                            style = FruitLogixTheme.typography.bodySmall.copy(
                                color = textMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Payload Spec Box
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PAYLOAD SPEC",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = arrival.cargoDescription,
                                style = FruitLogixTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${arrival.palletsCount} PALLETS",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = limeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 ${arrival.driverName}",
                            style = FruitLogixTheme.typography.bodySmall.copy(
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF334438))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PLATE ${arrival.licensePlate}",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. EXACTLY TWO BUTTONS: Lime Filled "Start reception" and Outlined "Dismiss"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B4D40))
                ) {
                    Text(
                        text = stringResource(id = R.string.geofence_sheet_btn_dismiss),
                        style = FruitLogixTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onStartReception(arrival.unitId)
                    },
                    modifier = Modifier
                        .weight(1.5f)
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.geofence_sheet_btn_start),
                        style = FruitLogixTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Radar Canvas visualizer rendering concentric dashed rings, center warehouse dot, radar line, and truck dot.
 */
@Composable
private fun RadarCanvasVisualizer(
    unitId: String,
    assignedDock: String
) {
    val limeColor = Color(0xFFC2E85A)
    val textMuted = Color(0xFFA0B2A6)

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.height * 0.42f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            // Outer ring
            drawCircle(
                color = Color(0xFF3B4D40),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 1.5f, pathEffect = dashEffect)
            )

            // Inner ring
            drawCircle(
                color = Color(0xFF3B4D40),
                radius = maxRadius * 0.5f,
                center = center,
                style = Stroke(width = 1.5f, pathEffect = dashEffect)
            )

            // Center warehouse dot
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = center
            )

            // Truck position on radar (45 deg angle on outer ring)
            val truckAngleRad = Math.toRadians(210.0 + (sweepAngle * 0.05))
            val truckX = center.x + (maxRadius * 0.7f * kotlin.math.cos(truckAngleRad)).toFloat()
            val truckY = center.y + (maxRadius * 0.7f * kotlin.math.sin(truckAngleRad)).toFloat()
            val truckPos = Offset(truckX, truckY)

            // Radar line from truck to warehouse
            drawLine(
                color = limeColor,
                start = truckPos,
                end = center,
                strokeWidth = 2f
            )

            // Truck dot glow & dot
            drawCircle(
                color = limeColor.copy(alpha = 0.3f),
                radius = 12f,
                center = truckPos
            )
            drawCircle(
                color = limeColor,
                radius = 6f,
                center = truckPos
            )
        }

        // Overlay text badges
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF334438))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🏷️ $assignedDock ASSIGNED",
                style = FruitLogixTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF334438))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🚛 $unitId (CROSSING SECTOR B)",
                style = FruitLogixTheme.typography.labelSmall.copy(
                    color = limeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Fires native NotificationCompat notification when geofence entry occurs.
 */
private fun sendGeofenceNotification(context: Context, arrival: Arrival) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            GEOFENCE_CHANNEL_ID,
            context.getString(R.string.geofence_notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.geofence_notif_channel_desc)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Launch intent back to app
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    } ?: Intent()

    val pendingIntent = PendingIntent.getActivity(
        context,
        GEOFENCE_NOTIF_ID,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, GEOFENCE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_logistics_arrive)
        .setContentTitle(context.getString(R.string.geofence_sheet_title, arrival.unitId))
        .setContentText("Dock approach ETA ${arrival.etaMinutes ?: 6} min • Callao Cold Hub")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(GEOFENCE_NOTIF_ID, notification)
    }
}
