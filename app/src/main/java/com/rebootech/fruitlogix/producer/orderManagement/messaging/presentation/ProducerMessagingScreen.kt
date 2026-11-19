package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatConversation
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Messages inbox: one conversation per order with its distributor.
 * Card rule: conversations are light list cards; unread ones get a border and a count.
 */
@Composable
fun ProducerMessagingScreen(
    onOpenChat: (String) -> Unit = {},
    viewModel: ProducerMessagingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.inner)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.messages_title),
                    style = FruitLogixTheme.typography.headlineMedium,
                    color = scheme.onBackground
                )
                Text(
                    text = stringResource(R.string.messages_subtitle),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        when {
            uiState.isLoading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(spacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = scheme.primary)
                }
            }
            uiState.conversations.isEmpty() -> item {
                EmptyState(
                    title = stringResource(R.string.messages_empty_title),
                    message = stringResource(R.string.messages_empty_message)
                )
            }
            else -> items(uiState.conversations, key = { it.orderId }) { conversation ->
                ConversationCard(
                    conversation = conversation,
                    onClick = {
                        viewModel.onOpenConversation(conversation.orderId)
                        onOpenChat(conversation.orderId)
                    }
                )
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val hasUnread = conversation.unreadCount > 0
    val last = conversation.lastMessage

    FruitLogixCard(
        variant = CardVariant.White,
        onClick = onClick,
        border = if (hasUnread) BorderStroke(2.dp, scheme.primary) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
                .padding(spacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            // Client avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(conversation.clientName),
                    style = FruitLogixTheme.typography.titleSmall,
                    color = scheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(spacing.inner))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.clientName,
                        style = FruitLogixTheme.typography.titleSmall,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    last?.let {
                        Spacer(modifier = Modifier.width(spacing.xs))
                        Text(
                            text = formatTimeAgo(it.sentAt),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = if (hasUnread) scheme.primary else scheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${stringResource(R.string.messages_order, conversation.orderId)} · ${conversation.productSummary}",
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(spacing.xxs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            last == null -> stringResource(R.string.messages_no_messages)
                            last.sender == MessageSender.PRODUCER -> stringResource(R.string.messages_you, last.message)
                            else -> last.message
                        },
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = if (hasUnread) scheme.onSurface else scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasUnread) {
                        Spacer(modifier = Modifier.width(spacing.xs))
                        val unreadDescription = pluralStringResource(
                            R.plurals.messages_unread,
                            conversation.unreadCount,
                            conversation.unreadCount
                        )
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
                                .clip(CircleShape)
                                .background(scheme.primaryContainer)
                                .semantics { contentDescription = unreadDescription }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                style = FruitLogixTheme.typography.labelMedium,
                                color = scheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
