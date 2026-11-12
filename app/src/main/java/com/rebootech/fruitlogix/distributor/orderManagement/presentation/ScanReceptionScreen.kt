package com.rebootech.fruitlogix.distributor.orderManagement.presentation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** The truck arrives: read the lot label, check it against the order and confirm the reception. */
@Composable
fun ScanReceptionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanReceptionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var hasCamera by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }
    // Ask once when the screen opens: the user came here to scan
    LaunchedEffect(Unit) {
        if (!hasCamera) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        BackHeader(
            title = stringResource(R.string.dist_scan_title),
            subtitle = stringResource(R.string.dist_scan_subtitle),
            onBack = onBackClick
        )

        val received = state.receivedOrderId
        if (received != null) {
            ReceivedContent(orderId = received, onScanAnother = viewModel::reset)
            return@Column
        }

        if (hasCamera) {
            CameraPreview()
            Text(
                text = stringResource(R.string.dist_scan_camera_hint),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            PermissionCard(onAllow = { permissionLauncher.launch(Manifest.permission.CAMERA) })
        }

        state.result?.let { result ->
            ResultCard(
                result = result,
                isConfirming = state.isConfirming,
                onConfirm = viewModel::confirmReception,
                onScanAgain = viewModel::reset
            )
        }

        if (state.showManualEntry) {
            LabeledTextField(
                label = stringResource(R.string.dist_scan_code_label),
                value = state.manualCode,
                onValueChange = viewModel::onManualCodeChange,
                placeholder = stringResource(R.string.dist_scan_code_placeholder),
                leadingIconRes = R.drawable.ic_receipt,
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    viewModel.checkManualCode()
                }
            )
            SecondaryButton(
                text = stringResource(R.string.dist_scan_check_code),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.checkManualCode()
                },
                enabled = state.manualCode.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TextActionButton(
                text = stringResource(R.string.dist_scan_type_code),
                onClick = viewModel::showManualEntry,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        DemoSection(onValid = viewModel::simulateValidScan, onDamaged = viewModel::simulateDamagedScan)
    }
}

@Composable
private fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }
    val frameColor = MaterialTheme.colorScheme.primaryContainer

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(FruitLogixTheme.shapes.HeroCard)
            .background(MaterialTheme.colorScheme.inverseSurface)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = controller
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Frame where the label has to fit
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 250.dp, height = 140.dp)
                .border(3.dp, frameColor, FruitLogixTheme.shapes.Card)
        )
    }
}

@Composable
private fun PermissionCard(onAllow: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_camera, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(stringResource(R.string.dist_scan_permission_title), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(stringResource(R.string.dist_scan_permission_message), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        PrimaryButton(text = stringResource(R.string.dist_scan_permission_allow), onClick = onAllow, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ResultCard(
    result: LotScanResult,
    isConfirming: Boolean,
    onConfirm: () -> Unit,
    onScanAgain: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val (container, content, icon) = when (result) {
        is LotScanResult.Matched -> Triple(colors.successContainer, colors.onSuccessContainer, R.drawable.ic_verified)
        is LotScanResult.NotOnRoute -> Triple(colors.attentionContainer, colors.onAttentionContainer, R.drawable.ic_warning)
        is LotScanResult.NotRecognized -> Triple(scheme.errorContainer, scheme.onErrorContainer, R.drawable.ic_alert)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(container)
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = icon, contentDescription = null, tint = content, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(
                text = when (result) {
                    is LotScanResult.Matched -> stringResource(R.string.dist_scan_matched_title, result.order.id)
                    is LotScanResult.NotOnRoute -> stringResource(R.string.dist_scan_not_on_route_title, result.order.id)
                    is LotScanResult.NotRecognized -> stringResource(R.string.dist_scan_unknown_title)
                },
                style = FruitLogixTheme.typography.titleMedium,
                color = content
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        when (result) {
            is LotScanResult.Matched -> {
                val order = result.order
                Text(stringResource(R.string.dist_scan_for_client, order.clientName), style = FruitLogixTheme.typography.bodyLarge, color = content)
                Text(
                    text = "${order.productName} · ${formatQuantity(order.quantityTons, order.pallets)}",
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = content
                )
                Text(result.code, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = content)
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
                PrimaryButton(
                    text = stringResource(R.string.dist_scan_confirm),
                    onClick = onConfirm,
                    enabled = !isConfirming,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is LotScanResult.NotOnRoute -> Text(
                text = stringResource(R.string.dist_scan_not_on_route_message, stringResource(orderStatusLabel(result.order.status)).lowercase()),
                style = FruitLogixTheme.typography.bodyMedium,
                color = content
            )
            is LotScanResult.NotRecognized -> Text(
                text = stringResource(R.string.dist_scan_unknown_message),
                style = FruitLogixTheme.typography.bodyMedium,
                color = content
            )
        }
        TextActionButton(text = stringResource(R.string.dist_scan_again), onClick = onScanAgain, enabled = !isConfirming)
    }
}

@Composable
private fun ReceivedContent(orderId: String, onScanAnother: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FruitLogixTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = colors.onSuccessContainer, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Text(stringResource(R.string.dist_scan_received_title), style = FruitLogixTheme.typography.headlineSmall, color = scheme.onSurface)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(
            text = stringResource(R.string.dist_scan_received_message, orderId),
            style = FruitLogixTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.md))
        PrimaryButton(text = stringResource(R.string.dist_scan_another), onClick = onScanAnother, modifier = Modifier.fillMaxWidth())
    }
}

/** Kept visible but secondary: lets the team test the flow until the camera decodes labels. */
@Composable
private fun DemoSection(onValid: () -> Unit, onDamaged: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(Color.Transparent)
            .border(1.dp, scheme.outlineVariant, FruitLogixTheme.shapes.Button)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Text(stringResource(R.string.dist_scan_demo_title), style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        TextActionButton(text = stringResource(R.string.dist_scan_demo_valid), onClick = onValid)
        TextActionButton(text = stringResource(R.string.dist_scan_demo_invalid), onClick = onDamaged)
    }
}
