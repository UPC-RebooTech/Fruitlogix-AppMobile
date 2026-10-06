package com.rebootech.fruitlogix.paymentManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoiceDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
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
                text = stringResource(
                    R.string.invoice_detail_back
                ),
                color = FruitLogixTheme.colors.textOnLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                R.string.invoice_detail_title
            ),
            style = FruitLogixTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.textOnLight
        )

        Text(
            text = stringResource(
                R.string.invoice_detail_subtitle,
                invoiceId
            ),
            style = FruitLogixTheme.typography.bodyMedium,
            color = FruitLogixTheme.colors.textOnLight.copy(
                alpha = 0.7f
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    ),
                    color = FruitLogixTheme.colors.primary
                )
            }

            state.invoice != null -> {
                val invoice = state.invoice!!

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FruitLogixTheme.shapes.Card,
                    colors = CardDefaults.cardColors(
                        containerColor =
                            FruitLogixTheme.colors.surfaceDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = invoice.id,
                            style =
                                FruitLogixTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color =
                                FruitLogixTheme.colors.textOnDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = invoice.counterpartyName,
                            style =
                                FruitLogixTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                FruitLogixTheme.colors.textOnDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = invoice.dateLabel,
                            color =
                                FruitLogixTheme.colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = formatDetailAmount(
                                invoice.amount
                            ),
                            style =
                                FruitLogixTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color =
                                FruitLogixTheme.colors.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = invoice.type.name,
                            color =
                                FruitLogixTheme.colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = invoice.status.name,
                            color =
                                FruitLogixTheme.colors.textOnDark
                        )
                    }
                }
            }

            state.isNotFound -> {
                Text(
                    text = stringResource(
                        R.string.invoice_detail_not_found
                    ),
                    color = FruitLogixTheme.colors.textOnLight
                )
            }
        }
    }
}

private fun formatDetailAmount(
    amount: Double
): String {
    val formatter =
        NumberFormat.getNumberInstance(Locale.US)

    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2

    return "S/ ${formatter.format(amount)}"
}