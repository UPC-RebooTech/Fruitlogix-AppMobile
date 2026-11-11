package com.rebootech.fruitlogix.producer.dashboard.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.domain.model.DashboardSummary
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.HeroActionCard
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.PipelineBar
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.QuickActionButton
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.StockHealthSection
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.TaskRow
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.UpcomingHarvestCard
import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.infrastructureIot.ProducerInfrastructureIotRoutes
import com.rebootech.fruitlogix.producer.navigation.ProducerRoute
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.ErrorState
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.OfflineBanner
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Producer Home.
 *
 * Card rule: highlighted next action and KPIs use the Dark variant (inverseSurface);
 * tasks, order flow, inventory and harvest are light list cards (surfaceContainer*).
 * All text comes from string resources (values = English, values-es = Spanish).
 */
@Composable
fun ProducerHomeScreen(
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProducerHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.onSnackbarDismissed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = scheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage?.asString().orEmpty(),
                    onRetry = { viewModel.loadDashboard() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> HomeContent(
                uiState = uiState,
                onNavigate = onNavigate,
                onAcceptOrder = { viewModel.onAcceptOrderClick(it) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = FruitLogixTheme.spacing.md)
        )
    }

    // "New inspection" picker is currently disabled; the quick action goes to Orders.
    // OrderPickerSheet is ready (localized) for when it is re-enabled.

    val pendingOrder = uiState.pendingAcceptOrder
    if (uiState.showAcceptConfirmDialog && pendingOrder != null) {
        ConfirmDialog(
            title = stringResource(R.string.producer_home_accept_title, pendingOrder.id),
            message = stringResource(R.string.producer_home_accept_message, pendingOrder.clientName),
            confirmText = stringResource(R.string.producer_home_accept_confirm),
            onConfirm = { viewModel.onConfirmAcceptOrder() },
            onDismiss = { viewModel.onDismissAcceptDialog() }
        )
    }
}

@Composable
private fun HomeContent(
    uiState: ProducerHomeUiState,
    onNavigate: (String) -> Unit,
    onAcceptOrder: (com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order) -> Unit
) {
    val spacing = FruitLogixTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.sm,
            end = spacing.sm,
            top = spacing.sm,
            bottom = spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        if (uiState.isOffline) {
            item { OfflineBanner(isOffline = true, modifier = Modifier.clip(FruitLogixTheme.shapes.Button)) }
        }

        // 1. Greeting, farm and date
        item {
            GreetingHeader(
                dayPart = uiState.dayPart,
                userName = uiState.userName,
                farmName = uiState.farmName,
                today = uiState.today
            )
        }

        // 2. Highlighted next action (dark)
        item {
            HeroActionCard(
                order = uiState.heroOrder,
                shortage = uiState.heroStockShortage,
                onAcceptClick = onAcceptOrder,
                onReviewDetailsClick = { order ->
                    onNavigate(ProducerOrderManagementRoutes.orderDetail(order.id))
                }
            )
        }

        // 3. Quick actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                QuickActionButton(
                    iconRes = R.drawable.ic_radar,
                    label = stringResource(R.string.producer_home_qa_scan_lot),
                    onClick = { onNavigate(ProducerQualityControlRoutes.ScanLotAny) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    iconRes = R.drawable.ic_task_check,
                    label = stringResource(R.string.producer_home_qa_new_inspection),
                    onClick = { onNavigate(ProducerRoute.Orders.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    iconRes = R.drawable.ic_inventory,
                    label = stringResource(R.string.producer_home_qa_update_stock),
                    onClick = { onNavigate(ProducerRoute.Stock.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    iconRes = R.drawable.ic_leaf,
                    label = stringResource(R.string.producer_home_qa_log_harvest),
                    onClick = { onNavigate(ProducerQualityControlRoutes.RegisterHarvest) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Today's tasks (light list)
        item {
            TasksCard(
                tasks = uiState.tasks,
                onTaskClick = { onNavigate(it.destinationRoute) },
                onViewAll = { onNavigate(ProducerInfrastructureIotRoutes.Notifications) }
            )
        }

        // 5. KPIs 2x2 (dark)
        item { KpiGrid(summary = uiState.summary) }

        // 6. Order flow (light)
        item {
            PipelineBar(
                pipeline = uiState.pipeline,
                onSegmentClick = { onNavigate(ProducerRoute.Orders.route) },
                onNavigateOrdersClick = { onNavigate(ProducerRoute.Orders.route) }
            )
        }

        // 7. Inventory health (light)
        item {
            StockHealthSection(
                stockItems = uiState.stockItems,
                onItemClick = { item ->
                    onNavigate(ProducerQualityControlRoutes.stockDetail(item.productId))
                },
                onNavigateStockClick = { onNavigate(ProducerRoute.Stock.route) }
            )
        }

        // 8. Next harvest (light)
        item {
            UpcomingHarvestCard(
                harvest = uiState.upcomingHarvest,
                onClick = { onNavigate(ProducerQualityControlRoutes.Harvests) }
            )
        }
    }
}

@Composable
private fun GreetingHeader(
    dayPart: DayPart,
    userName: String,
    farmName: String,
    today: LocalDate
) {
    val scheme = MaterialTheme.colorScheme
    val locale = currentLocale()
    val greetingRes = when (dayPart) {
        DayPart.MORNING -> R.string.producer_home_greeting_morning
        DayPart.AFTERNOON -> R.string.producer_home_greeting_afternoon
        DayPart.EVENING -> R.string.producer_home_greeting_evening
    }
    val datePattern = stringResource(R.string.producer_home_date_pattern)
    val dateText = remember(today, locale, datePattern) {
        today.format(DateTimeFormatter.ofPattern(datePattern, locale))
            .replaceFirstChar { it.titlecase(locale) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(greetingRes, userName),
            style = FruitLogixTheme.typography.headlineMedium,
            color = scheme.onBackground
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                id = R.drawable.ic_leaf,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
            )
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
            Text(
                text = farmName,
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Row(
                modifier = Modifier
                    .clip(FruitLogixTheme.shapes.Tag)
                    .background(scheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_schedule,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
                Text(
                    text = dateText,
                    style = FruitLogixTheme.typography.labelMedium,
                    color = scheme.onSecondaryContainer,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TasksCard(
    tasks: List<ProducerTask>,
    onTaskClick: (ProducerTask) -> Unit,
    onViewAll: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val visibleTasks = tasks.take(5)

    FruitLogixCard(
        variant = CardVariant.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.producer_home_tasks_title),
                    style = FruitLogixTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                StatusBadge(
                    text = tasks.size.toString(),
                    type = StatusBadgeType.HIGHLIGHT,
                    showDot = false
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            if (visibleTasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.producer_home_tasks_empty),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                    visibleTasks.forEach { task ->
                        TaskRow(task = task, onClick = { onTaskClick(task) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

            TextActionButton(
                text = stringResource(R.string.producer_home_tasks_view_all),
                onClick = onViewAll,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    AppIcon(
                        id = R.drawable.ic_arrow_forward,
                        contentDescription = null,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                }
            )
        }
    }
}

@Composable
private fun KpiGrid(summary: DashboardSummary) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val locale = currentLocale()
    val amountFormat = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 0 }
    }
    val spacing = FruitLogixTheme.spacing

    fun kpiIcon(id: Int): @Composable () -> Unit = {
        AppIcon(
            id = id,
            contentDescription = null,
            tint = scheme.inversePrimary,
            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            KpiCard(
                caption = stringResource(R.string.producer_home_kpi_pending_orders),
                value = summary.pendingOrdersCount.toString(),
                icon = kpiIcon(R.drawable.ic_orders),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                caption = stringResource(R.string.producer_home_kpi_lots_dispatched),
                value = summary.lotsDispatchedThisMonth.toString(),
                icon = kpiIcon(R.drawable.ic_dispatch_truck),
                footer = {
                    Text(
                        text = stringResource(R.string.producer_home_kpi_this_month),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = colors.onInverseSurfaceVariant
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            KpiCard(
                caption = stringResource(R.string.producer_home_kpi_quality_approval),
                value = "${summary.qualityApprovalPct}%",
                icon = kpiIcon(R.drawable.ic_shield_check),
                deltaPill = {
                    StatusBadge(
                        text = stringResource(R.string.producer_home_kpi_optimal),
                        type = StatusBadgeType.SUCCESS,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                caption = stringResource(R.string.producer_home_kpi_pending_payments),
                value = stringResource(
                    R.string.common_amount_pen,
                    amountFormat.format(summary.pendingPaymentsAmount)
                ),
                icon = kpiIcon(R.drawable.ic_invoices),
                footer = {
                    Text(
                        text = stringResource(
                            R.string.producer_home_kpi_collected,
                            stringResource(
                                R.string.common_amount_pen,
                                amountFormat.format(summary.paidPaymentsAmount)
                            )
                        ),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = colors.onInverseSurfaceVariant
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales.get(0)
