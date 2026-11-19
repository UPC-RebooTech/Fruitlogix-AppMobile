package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatMessage
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatTime
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Item of the chat list: a day separator or a message. */
private sealed class ChatRow(val key: String) {
    class Day(val date: LocalDate) : ChatRow("day-$date")
    class Message(val message: ChatMessage) : ChatRow(message.id)
}

/**
 * Chat with the distributor for one order.
 * - Header on the light surface: back, client avatar + name (dark text), order and products,
 *   and a shortcut to the order.
 * - Incoming bubbles are light with a border; the producer's bubbles are Lima.
 * - Quick replies for common field answers, and a 56dp send button.
 */
@Composable
fun ProducerChatScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onOpenOrder: (String) -> Unit = {},
    viewModel: ProducerChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(orderId) { viewModel.initChat(orderId) }

    val rows = buildList {
        var lastDate: LocalDate? = null
        uiState.messages.forEach { message ->
            val date = message.sentAt.toLocalDate()
            if (date != lastDate) {
                add(ChatRow.Day(date))
                lastDate = date
            }
            add(ChatRow.Message(message))
        }
    }

    LaunchedEffect(rows.size, uiState.isDistributorTyping) {
        if (rows.isNotEmpty()) listState.animateScrollToItem(rows.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
    ) {
        ChatHeader(
            clientName = uiState.clientName,
            orderId = uiState.orderId,
            productSummary = uiState.productSummary,
            onBack = onNavigateBack,
            onOpenOrder = { onOpenOrder(uiState.orderId) }
        )

        if (rows.isEmpty()) {
            ChatEmptyState(orderId = uiState.orderId, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = FruitLogixTheme.spacing.sm,
                    vertical = FruitLogixTheme.spacing.inner
                ),
                verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
            ) {
                items(rows, key = { it.key }) { row ->
                    when (row) {
                        is ChatRow.Day -> DaySeparator(row.date)
                        is ChatRow.Message -> MessageBubble(row.message)
                    }
                }
                if (uiState.isDistributorTyping) {
                    item(key = "typing") {
                        Text(
                            text = stringResource(R.string.chat_typing, uiState.clientName),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = FruitLogixTheme.spacing.xxs)
                        )
                    }
                }
            }
        }

        QuickReplies(onReply = { viewModel.sendMessage(quickReply = it) })

        ChatInputBar(
            text = uiState.currentInput,
            onTextChange = viewModel::onInputChange,
            onSend = { viewModel.sendMessage() }
        )
    }
}

@Composable
private fun ChatHeader(
    clientName: String,
    orderId: String,
    productSummary: String,
    onBack: () -> Unit,
    onOpenOrder: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.background(scheme.surfaceContainerLowest)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FruitLogixTheme.spacing.xxs, vertical = FruitLogixTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIconButton(
                iconRes = R.drawable.ic_arrow_back,
                description = stringResource(R.string.common_back),
                onClick = onBack
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(clientName),
                    style = FruitLogixTheme.typography.titleSmall,
                    color = scheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clientName,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.chat_subtitle, orderId, productSummary),
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HeaderIconButton(
                iconRes = R.drawable.ic_orders,
                description = stringResource(R.string.chat_cd_view_order),
                onClick = onOpenOrder,
                tint = scheme.primary
            )
        }
        HorizontalDivider(color = scheme.outlineVariant)
    }
}

@Composable
private fun HeaderIconButton(
    iconRes: Int,
    description: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .size(FruitLogixTheme.sizing.minTouch)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            id = iconRes,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(FruitLogixTheme.sizing.icon)
        )
    }
}

@Composable
private fun DaySeparator(date: LocalDate) {
    val scheme = MaterialTheme.colorScheme
    val locale = appLocale()
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.chat_today)
        today.minusDays(1) -> stringResource(R.string.chat_yesterday)
        else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { it.titlecase(locale) }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = FruitLogixTheme.spacing.xs), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier
                .clip(FruitLogixTheme.shapes.Pill)
                .background(scheme.surfaceContainerHigh)
                .padding(horizontal = FruitLogixTheme.spacing.inner, vertical = FruitLogixTheme.spacing.xxs)
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val scheme = MaterialTheme.colorScheme
    val isMine = message.sender == MessageSender.PRODUCER

    val shape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)
    }
    val container = if (isMine) scheme.primaryContainer else scheme.surfaceContainerLowest
    val content = if (isMine) scheme.onPrimaryContainer else scheme.onSurface
    val meta = if (isMine) scheme.onPrimaryContainer.copy(alpha = 0.75f) else scheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(container)
                .then(if (isMine) Modifier else Modifier.border(BorderStroke(1.dp, scheme.outlineVariant), shape))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                style = FruitLogixTheme.typography.bodyLarge,
                color = content
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatTime(message.sentAt),
                style = FruitLogixTheme.typography.labelMedium,
                color = meta,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun ChatEmptyState(orderId: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FruitLogixTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = R.drawable.ic_chat,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(FruitLogixTheme.sizing.icon)
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
        Text(
            text = stringResource(R.string.chat_empty_title),
            style = FruitLogixTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Text(
            text = stringResource(R.string.chat_empty_message, orderId),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun QuickReplies(onReply: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val replies = listOf(
        stringResource(R.string.chat_quick_ready),
        stringResource(R.string.chat_quick_confirm),
        stringResource(R.string.chat_quick_delay),
        stringResource(R.string.chat_quick_call)
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = FruitLogixTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs),
        modifier = Modifier.padding(top = FruitLogixTheme.spacing.xs)
    ) {
        items(replies) { reply ->
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch)
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(scheme.secondaryContainer)
                    .clickable(role = Role.Button) { onReply(reply) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reply,
                    style = FruitLogixTheme.typography.labelLarge,
                    color = scheme.onSecondaryContainer,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val canSend = text.isNotBlank()
    val sendDescription = stringResource(R.string.chat_cd_send)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.Bottom
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
            textStyle = FruitLogixTheme.typography.bodyLarge,
            maxLines = 4,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = scheme.surfaceContainerLowest,
                unfocusedContainerColor = scheme.surfaceContainerLowest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedPlaceholderColor = scheme.onSurfaceVariant,
                unfocusedPlaceholderColor = scheme.onSurfaceVariant
            ),
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
                .border(BorderStroke(1.dp, scheme.outlineVariant), RoundedCornerShape(28.dp))
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.fieldTouch)
                .clip(CircleShape)
                .background(if (canSend) scheme.primaryContainer else scheme.surfaceContainerHigh)
                .clickable(enabled = canSend, role = Role.Button, onClick = onSend)
                .semantics { contentDescription = sendDescription },
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = R.drawable.ic_send,
                contentDescription = null,
                tint = if (canSend) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                modifier = Modifier.size(FruitLogixTheme.sizing.icon)
            )
        }
    }
}
