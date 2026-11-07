package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatMessage
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerChatScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProducerChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(orderId) {
        viewModel.initChat(orderId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.distributorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Pedido #${uiState.orderId} • En línea",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC5E1A5)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                text = uiState.currentInput,
                onTextChange = viewModel::onInputChange,
                onSendMessage = viewModel::sendMessage
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                ChatMessageItem(message = message)
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val isProducer = message.sender == MessageSender.PRODUCER

    val bubbleColor = if (isProducer) Color(0xFFD0E768) else Color(0xFF233527)
    val textColor = if (isProducer) Color(0xFF1B2A1E) else Color(0xFFE9F5EB)
    val timeColor = if (isProducer) Color(0xFF4A5A4A) else Color(0xFF9AB19D)

    val bubbleShape = if (isProducer) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isProducer) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(color = bubbleColor, shape = bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = timeColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Escribe un mensaje...", color = Color(0xFF6B7280), fontSize = 14.sp) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B2A1E),
                unfocusedTextColor = Color(0xFF1B2A1E),
                cursorColor = Color(0xFF1B2A1E),
                focusedBorderColor = Color(0xFFD0E768),
                unfocusedBorderColor = Color(0xFFB0C5A4)
            )
        )

        IconButton(
            onClick = onSendMessage,
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFFD0E768), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = "Enviar mensaje",
                tint = Color(0xFF1B2A1E),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}