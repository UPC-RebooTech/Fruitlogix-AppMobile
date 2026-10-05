package com.rebootech.fruitlogix.orderManagement.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.orderManagement.domain.model.LotScanStatus
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun ScanReceptionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanReceptionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        TextButton(
            onClick = onBackClick
        ) {
            Text(
                text = stringResource(R.string.scan_reception_back),
                color = FruitLogixTheme.colors.textOnLight
            )
        }

        Text(
            text = stringResource(R.string.scan_reception_title),
            style = FruitLogixTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.textOnLight
        )

        Text(
            text = stringResource(R.string.scan_reception_subtitle),
            style = FruitLogixTheme.typography.bodyMedium,
            color = FruitLogixTheme.colors.textOnLight.copy(
                alpha = 0.7f
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (hasCameraPermission) {
            CameraPreview()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.scan_reception_camera_hint),
                color = FruitLogixTheme.colors.textOnLight.copy(
                    alpha = 0.7f
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = stringResource(R.string.scan_reception_demo_valid),
                onClick = viewModel::simulateSuccessfulScan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = viewModel::simulateInvalidScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        R.string.scan_reception_demo_invalid
                    ),
                    color = FruitLogixTheme.colors.textOnLight
                )
            }
        } else {
            CameraPermissionCard(
                onRequestPermission = {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                },
                onManualEntry = viewModel::showManualEntry
            )
        }

        state.scanResult?.let { result ->
            Spacer(modifier = Modifier.height(20.dp))

            when (result.status) {
                LotScanStatus.MATCHED -> {
                    MatchConfirmedCard(
                        orderId = result.orderId.orEmpty(),
                        productName = result.productName.orEmpty(),
                        quantityLabel = result.quantityLabel.orEmpty(),
                        onScanAgain = viewModel::resetScan
                    )
                }

                LotScanStatus.NOT_RECOGNIZED -> {
                    ScanErrorCard(
                        onManualEntry = viewModel::showManualEntry
                    )
                }
            }
        }

        if (state.showManualEntry) {
            Spacer(modifier = Modifier.height(20.dp))

            ManualCodeEntry(
                value = state.manualCode,
                onValueChange = viewModel::onManualCodeChange,
                onValidate = viewModel::validateManualCode
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context)
    }

    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)

        onDispose {
            cameraController.unbind()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(FruitLogixTheme.colors.surfaceDark)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController

                    scaleType =
                        PreviewView.ScaleType.FILL_CENTER

                    implementationMode =
                        PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(
                    width = 250.dp,
                    height = 150.dp
                )
                .border(
                    width = 3.dp,
                    color = FruitLogixTheme.colors.primary,
                    shape = RoundedCornerShape(20.dp)
                )
        )
    }
}

@Composable
private fun CameraPermissionCard(
    onRequestPermission: () -> Unit,
    onManualEntry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier.padding(
                FruitLogixTheme.spacing.sm
            )
        ) {
            Text(
                text = stringResource(
                    R.string.scan_reception_permission_title
                ),
                color = FruitLogixTheme.colors.textOnDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.scan_reception_permission_message
                ),
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = stringResource(
                    R.string.scan_reception_permission_button
                ),
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = onManualEntry,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(
                        R.string.scan_reception_manual_option
                    ),
                    color = FruitLogixTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun MatchConfirmedCard(
    orderId: String,
    productName: String,
    quantityLabel: String,
    onScanAgain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier.padding(
                FruitLogixTheme.spacing.sm
            )
        ) {
            Text(
                text = stringResource(
                    R.string.scan_reception_match_title
                ),
                color = FruitLogixTheme.colors.success,
                style = FruitLogixTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    R.string.scan_reception_order,
                    orderId
                ),
                color = FruitLogixTheme.colors.textOnDark
            )

            Text(
                text = stringResource(
                    R.string.scan_reception_product,
                    productName
                ),
                color = FruitLogixTheme.colors.textMuted
            )

            Text(
                text = stringResource(
                    R.string.scan_reception_quantity,
                    quantityLabel
                ),
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = stringResource(
                    R.string.scan_reception_scan_again
                ),
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScanErrorCard(
    onManualEntry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.danger
        )
    ) {
        Column(
            modifier = Modifier.padding(
                FruitLogixTheme.spacing.sm
            )
        ) {
            Text(
                text = stringResource(
                    R.string.scan_reception_error_title
                ),
                color = FruitLogixTheme.colors.textOnDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.scan_reception_error_message
                ),
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onManualEntry
            ) {
                Text(
                    text = stringResource(
                        R.string.scan_reception_manual_option
                    ),
                    color = FruitLogixTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun ManualCodeEntry(
    value: String,
    onValueChange: (String) -> Unit,
    onValidate: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FruitLogixTheme.colors.textOnLight,
        unfocusedTextColor = FruitLogixTheme.colors.textOnLight,
        focusedLabelColor = FruitLogixTheme.colors.primary,
        unfocusedLabelColor =
            FruitLogixTheme.colors.textOnLight.copy(alpha = 0.65f),
        focusedBorderColor = FruitLogixTheme.colors.primary,
        cursorColor = FruitLogixTheme.colors.primary
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(
                R.string.scan_reception_manual_title
            ),
            color = FruitLogixTheme.colors.textOnLight,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(
                        R.string.scan_reception_manual_label
                    )
                )
            },
            placeholder = {
                Text("LOT-FX-1040-FRESA")
            },
            colors = fieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryButton(
            text = stringResource(
                R.string.scan_reception_validate
            ),
            onClick = onValidate,
            modifier = Modifier.fillMaxWidth()
        )
    }
}