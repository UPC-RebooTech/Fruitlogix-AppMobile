package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerStatus
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.components.InviteProducerSheet
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.components.ProducerFormSheet
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatKg
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat

@Composable
fun ProducersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProducersViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val shareTitle = stringResource(R.string.prod_invite_share_title)
    val anyCrop = stringResource(R.string.prod_invite_any_crop)

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it.asString(context))
            viewModel.onMessageShown()
        }
    }
    // US41: hand the invitation to WhatsApp, email or SMS
    val inviteText = state.invite?.let { stringResource(R.string.prod_invite_text, it.name, it.inviterName, it.crop.ifBlank { anyCrop }, it.link) }
    LaunchedEffect(state.invite) {
        inviteText?.let { text ->
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }, shareTitle))
            viewModel.onInviteShared()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item { BackHeader(title = stringResource(R.string.prod_title), subtitle = stringResource(R.string.prod_subtitle), onBack = onBack) }
            item { Kpis(state) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    PrimaryButton(text = stringResource(R.string.prod_register), onClick = { viewModel.openSheet(ProducerSheet.Register) }, modifier = Modifier.weight(1.4f))
                    SecondaryButton(
                        text = stringResource(R.string.prod_invite),
                        onClick = { viewModel.openSheet(ProducerSheet.Invite) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { AppIcon(id = R.drawable.ic_share, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                    )
                }
            }
            item {
                LabeledTextField(
                    label = stringResource(R.string.prod_search),
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    leadingIconRes = R.drawable.ic_search,
                    imeAction = ImeAction.Search
                )
            }
            if (state.visible.isEmpty()) {
                item { EmptyState(title = stringResource(R.string.prod_empty_title), message = stringResource(R.string.prod_empty_message)) }
            } else {
                items(state.visible, key = { it.id }) { producer ->
                    ProducerCard(
                        producer = producer,
                        onCall = {
                            val number = producer.phone.filter { it.isDigit() || it == '+' }
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                        },
                        onEdit = { viewModel.openSheet(ProducerSheet.Edit(producer)) },
                        onDelete = { viewModel.askDelete(producer) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }

    state.sheet?.let { ProducerSheetHost(it, state, viewModel) }
    state.deleting?.let { producer ->
        ConfirmDialog(
            title = stringResource(R.string.prod_delete_title, producer.name),
            message = stringResource(R.string.prod_delete_message),
            confirmText = stringResource(R.string.prod_delete_confirm),
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
            destructive = true
        )
    }
}

/** US42: network summary. */
@Composable
private fun Kpis(state: ProducersUiState) {
    val scheme = MaterialTheme.colorScheme
    val kpis = state.kpis
    val spacing = FruitLogixTheme.spacing

    @Composable
    fun icon(id: Int): @Composable () -> Unit = {
        AppIcon(id = id, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            KpiCard(caption = stringResource(R.string.prod_kpi_total), value = kpis.total.toString(), icon = icon(R.drawable.ic_producers), modifier = Modifier.weight(1f))
            KpiCard(
                caption = stringResource(R.string.prod_kpi_quality),
                value = stringResource(R.string.prod_kpi_quality_value, formatDecimal(kpis.averageQuality)),
                icon = icon(R.drawable.ic_star),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            KpiCard(
                caption = stringResource(R.string.prod_kpi_tons),
                value = formatKg(kpis.weeklyTons * 1000),
                icon = icon(R.drawable.ic_inventory),
                modifier = Modifier.weight(1f)
            )
            KpiCard(caption = stringResource(R.string.prod_kpi_incidents), value = kpis.incidents30d.toString(), icon = icon(R.drawable.ic_alert), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProducerCard(producer: Producer, onCall: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val invited = producer.status == ProducerStatus.INVITED
    val number = NumberFormat.getIntegerInstance(appLocale())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconBox)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(initialsOf(producer.name), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Column(modifier = Modifier.weight(1f)) {
                Text(producer.name, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = stringResource(if (producer.entityType == ProducerEntityType.COMPANY) R.string.prod_company else R.string.prod_individual),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            if (invited) StatusBadge(text = stringResource(R.string.prod_invited), type = StatusBadgeType.INFO, maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        val place = listOf(producer.farmName, producer.location).filter { it.isNotBlank() }.joinToString(" · ")
        if (place.isNotBlank()) Text(place, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurface)
        if (invited) {
            Text(stringResource(R.string.prod_waiting), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        } else {
            Text(
                text = stringResource(R.string.prod_crop_rating, producer.cropType, formatDecimal(producer.qualityRating)),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.prod_stock_orders, number.format(producer.stockKg), producer.activeOrders),
                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                color = scheme.onSurfaceVariant
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (producer.phone.isNotBlank()) TextActionButton(text = stringResource(R.string.prod_call), onClick = onCall)
            TextActionButton(text = stringResource(R.string.prod_edit), onClick = onEdit)
            TextActionButton(text = stringResource(R.string.prod_delete), onClick = onDelete)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProducerSheetHost(sheet: ProducerSheet, state: ProducersUiState, viewModel: ProducersViewModel) {
    val scheme = MaterialTheme.colorScheme
    val title = when (sheet) {
        ProducerSheet.Register -> stringResource(R.string.prod_form_register_title)
        is ProducerSheet.Edit -> stringResource(R.string.prod_form_edit_title, sheet.producer.name)
        ProducerSheet.Invite -> stringResource(R.string.prod_invite_title)
    }
    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg)
        ) {
            Text(title, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
            when (sheet) {
                ProducerSheet.Register -> ProducerFormSheet(producer = null, errors = state.formErrors, onSave = viewModel::save, serverError = state.sheetError?.let { stringResource(it) }, saving = state.saving)
                is ProducerSheet.Edit -> ProducerFormSheet(producer = sheet.producer, errors = state.formErrors, onSave = viewModel::save, serverError = state.sheetError?.let { stringResource(it) }, saving = state.saving)
                ProducerSheet.Invite -> InviteProducerSheet(error = state.inviteError?.let { stringResource(it) }, onInvite = viewModel::invite)
            }
        }
    }
}
