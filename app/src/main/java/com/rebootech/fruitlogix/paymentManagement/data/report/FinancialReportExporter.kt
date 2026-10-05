package com.rebootech.fruitlogix.paymentManagement.data.report

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceType
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object FinancialReportExporter {

    fun exportAndShare(
        context: Context,
        invoices: List<Invoice>,
        dateFilter: String,
        statusFilter: InvoiceStatus?
    ) {
        val reportsDirectory = File(
            context.cacheDir,
            "reports"
        ).apply {
            mkdirs()
        }

        val reportFile = File(
            reportsDirectory,
            "fruitlogix_financial_report.pdf"
        )

        createPdf(
            file = reportFile,
            invoices = invoices,
            dateFilter = dateFilter,
            statusFilter = statusFilter
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            reportFile
        )

        val shareIntent = Intent(
            Intent.ACTION_SEND
        ).apply {
            type = "application/pdf"

            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            putExtra(
                Intent.EXTRA_SUBJECT,
                "FruitLogix Financial Report"
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        context.startActivity(
            Intent.createChooser(
                shareIntent,
                "Share financial report"
            )
        )
    }

    private fun createPdf(
        file: File,
        invoices: List<Invoice>,
        dateFilter: String,
        statusFilter: InvoiceStatus?
    ) {
        val document = PdfDocument()

        val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f

        var pageNumber = 1

        var page = document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber
            ).create()
        )

        var canvas = page.canvas
        var y = 50f

        fun newPage() {
            document.finishPage(page)

            pageNumber++

            page = document.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )

            canvas = page.canvas
            y = 50f
        }

        fun drawText(
            value: String,
            size: Float = 12f,
            bold: Boolean = false,
            spacingAfter: Float = 22f
        ) {
            if (y > pageHeight - 60f) {
                newPage()
            }

            paint.textSize = size

            paint.typeface = if (bold) {
                Typeface.DEFAULT_BOLD
            } else {
                Typeface.DEFAULT
            }

            canvas.drawText(
                value,
                margin,
                y,
                paint
            )

            y += spacingAfter
        }

        drawText(
            value = "FRUITLOGIX",
            size = 22f,
            bold = true,
            spacingAfter = 28f
        )

        drawText(
            value = "Financial Report",
            size = 18f,
            bold = true,
            spacingAfter = 30f
        )

        drawText(
            value = "Applied filters",
            size = 14f,
            bold = true
        )

        drawText(
            value = "Date: ${
                dateFilter.ifBlank { "All" }
            }"
        )

        drawText(
            value = "Status: ${
                statusFilter?.name ?: "All"
            }",
            spacingAfter = 30f
        )

        val receivables = invoices.filter {
            it.type == InvoiceType.RECEIVABLE
        }

        val payables = invoices.filter {
            it.type == InvoiceType.PAYABLE
        }

        if (receivables.isNotEmpty()) {
            drawText(
                value = "RECEIVABLES",
                size = 15f,
                bold = true,
                spacingAfter = 26f
            )

            receivables.forEach { invoice ->
                drawInvoice(
                    invoice = invoice,
                    drawText = ::drawText
                )

                y += 8f
            }
        }

        if (payables.isNotEmpty()) {
            drawText(
                value = "PAYABLES",
                size = 15f,
                bold = true,
                spacingAfter = 26f
            )

            payables.forEach { invoice ->
                drawInvoice(
                    invoice = invoice,
                    drawText = ::drawText
                )

                y += 8f
            }
        }

        document.finishPage(page)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }

        document.close()
    }

    private fun drawInvoice(
        invoice: Invoice,
        drawText: (
            String,
            Float,
            Boolean,
            Float
        ) -> Unit
    ) {
        drawText(
            invoice.id,
            13f,
            true,
            20f
        )

        drawText(
            invoice.counterpartyName,
            12f,
            false,
            18f
        )

        drawText(
            invoice.dateLabel,
            11f,
            false,
            18f
        )

        drawText(
            formatAmount(invoice.amount),
            12f,
            true,
            18f
        )

        drawText(
            "Status: ${invoice.status.name}",
            11f,
            false,
            20f
        )
    }

    private fun formatAmount(
        amount: Double
    ): String {
        val formatter =
            NumberFormat.getNumberInstance(
                Locale.US
            )

        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2

        return "S/ ${formatter.format(amount)}"
    }
}