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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

private const val GEOFENCE_CHANNEL_ID = "geofence"
private const val GEOFENCE_NOTIF_ID = 4010

/**
 * Modal Bottom Sheet for Geofence Alerts.
 * Pixel-perfect implementation following the mockup with 28dp top corners, ETA card, Canvas radar, compact tiles, payload summary, and EXACTLY two action buttons.
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

    // Fire native local notification when sheet appears
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = darkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF3B4D40)) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // a. Overline "GEOFENCE ALERT" with small pulsing dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(limeColor)
                )
                Text(
                    text = stringResource(id = R.string.geofence_sheet_overline),
                    style = FruitLogixTheme.typography.labelSmall.copy(
                        color = limeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            // b. Title "FL-102 entered the warehouse geofence" (Poppins 22sp, white, max 2 lines)
            Text(
                text = stringResource(id = R.string.geofence_sheet_title, arrival.unitId),
                style = FruitLogixTheme.typography.headlineSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // c. ETA Card: Timer icon in lime circle, ETA title & big value on left, distance & speed on right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(innerCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timer icon in lime circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(limeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logistics_clock),
                                contentDescription = null,
                                tint = Color(0xFF1B2E1E),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = stringResource(id = R.string.geofence_sheet_eta_overline),
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )

                            // Computed ETA string (never 0 min, show "< 1 MIN" if < 1)
                            val computedEtaMin = arrival.etaMinutes ?: (if (arrival.distanceKm <= 0.2f) 1 else ((arrival.distanceKm / 18f) * 60).toInt())
                            val etaText = if (computedEtaMin < 1 || arrival.distanceKm <= 0.1f) {
                                stringResource(id = R.string.geofence_sheet_eta_less_than_min)
                            } else {
                                stringResource(id = R.string.geofence_sheet_eta_min, computedEtaMin)
                            }

                            Text(
                                text = etaText,
                                style = FruitLogixTheme.typography.headlineLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = limeColor,
                                    fontSize = 28.sp,
                                    lineHeight = 32.sp
                                )
                            )
                        }
                    }

                    // Right side position & speed info
                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.geofence_sheet_distance_speed,
                                arrival.distanceKm
                            ),
                            style = FruitLogixTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                id = R.string.geofence_sheet_cruising_speed,
                                arrival.speedKmh.ifEmpty { "18 km/h" }.uppercase()
                            ),
                            style = FruitLogixTheme.typography.labelSmall.copy(
                                color = textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // d. RADAR BAND: Full width ~160dp tall card drawn with Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(innerCardBg)
            ) {
                RadarBandCanvas(unitId = arrival.unitId, assignedDock = arrival.assignedDock)
            }

            // e. Two Compact Tiles Side by Side: REEFER CLIMATE & ASSIGNED DOCK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tile 1: REEFER CLIMATE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
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
                                text = stringResource(id = R.string.geofence_sheet_reefer_overline),
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 10.sp,
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
                                    text = stringResource(id = R.string.geofence_sheet_nominal),
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
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LimeProgressBar(progressFraction = 0.85f, height = 4.dp)
                    }
                }

                // Tile 2: ASSIGNED DOCK
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(innerCardBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.geofence_sheet_dock_overline),
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logistics_truck),
                                contentDescription = null,
                                tint = limeColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = arrival.assignedDock,
                            style = FruitLogixTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = stringResource(id = R.string.geofence_sheet_dock_caption),
                            style = FruitLogixTheme.typography.bodySmall.copy(
                                color = textMuted,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // f. Payload Card: Leaf icon, fruit, pallets at right, second row driver & plate chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(innerCardBg)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334438)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_logistics_snowflake),
                                    contentDescription = null,
                                    tint = limeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = arrival.cargoDescription.split("•").firstOrNull()?.trim() ?: arrival.cargoDescription,
                                style = FruitLogixTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "${arrival.palletsCount} PALLETS",
                            style = FruitLogixTheme.typography.labelSmall.copy(
                                color = limeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shield_check),
                                contentDescription = null,
                                tint = textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = arrival.driverName,
                                style = FruitLogixTheme.typography.bodySmall.copy(
                                    color = textMuted,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF334438))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = arrival.licensePlate,
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

            Spacer(modifier = Modifier.height(6.dp))

            // g. Buttons: Lime pill "Start reception →" (56dp) & Ghost pill "Dismiss" below it
            Button(
                onClick = {
                    onDismiss()
                    onStartReception(arrival.unitId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = limeColor,
                    contentColor = Color(0xFF1B2E1E)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logistics_arrive),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.geofence_sheet_btn_start_arrow),
                    style = FruitLogixTheme.typography.labelLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = innerCardBg,
                    contentColor = textMuted
                )
            ) {
                Text(
                    text = stringResource(id = R.string.geofence_sheet_btn_dismiss),
                    style = FruitLogixTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

/**
 * Radar Band Canvas component (~160dp tall full width card visualizer).
 */
@Composable
private fun RadarBandCanvas(
    unitId: String,
    assignedDock: String
) {
    val limeColor = Color(0xFFC2E85A)

    val infiniteTransition = rememberInfiniteTransition(label = "RadarBandSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.height * 0.42f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            // Outer ring (dashed perimeter)
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

            // Truck position approaching from lower left
            val truckAngleRad = Math.toRadians(215.0 + (sweepAngle * 0.04))
            val truckX = center.x + (maxRadius * 0.7f * kotlin.math.cos(truckAngleRad)).toFloat()
            val truckY = center.y + (maxRadius * 0.7f * kotlin.math.sin(truckAngleRad)).toFloat()
            val truckPos = Offset(truckX, truckY)

            // Radar line
            drawLine(
                color = limeColor,
                start = truckPos,
                end = center,
                strokeWidth = 2f
            )

            // Truck dot glow & center dot
            drawCircle(
                color = limeColor.copy(alpha = 0.35f),
                radius = 12f,
                center = truckPos
            )
            drawCircle(
                color = limeColor,
                radius = 6f,
                center = truckPos
            )
        }

        // Top Right Pill inside card: "Dock B assigned"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF334438))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logistics_truck),
                    contentDescription = null,
                    tint = limeColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.geofence_sheet_pill_dock),
                    style = FruitLogixTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Bottom Left Pill inside card: "FL-102 crossing perimeter"
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF334438))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logistics_truck),
                    contentDescription = null,
                    tint = limeColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.geofence_sheet_pill_crossing, unitId),
                    style = FruitLogixTheme.typography.labelSmall.copy(
                        color = limeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
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
