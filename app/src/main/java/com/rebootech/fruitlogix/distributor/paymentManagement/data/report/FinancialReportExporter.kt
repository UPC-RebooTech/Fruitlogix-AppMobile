package com.rebootech.fruitlogix.distributor.paymentManagement.data.report

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.InvoiceRules
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Builds an A4 PDF with the invoices on screen and opens the share sheet.
 * Texts come from string resources, so the PDF is in the app's language.
 */
object FinancialReportExporter {

    fun exportAndShare(context: Context, invoices: List<Invoice>, statusFilter: InvoiceStatus?) {
        val file = File(File(context.cacheDir, "reports").apply { mkdirs() }, "fruitlogix_financial_report.pdf")
        createPdf(context, file, invoices, statusFilter)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.inv_pdf_subject))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(share, context.getString(R.string.inv_pdf_share)))
    }

    private fun statusLabel(context: Context, status: InvoiceStatus?): String = context.getString(
        when (status) {
            null -> R.string.inv_filter_all
            InvoiceStatus.PENDING -> R.string.inv_status_pending
            InvoiceStatus.OVERDUE -> R.string.inv_status_overdue
            InvoiceStatus.PAID -> R.string.inv_status_paid
        }
    )

    private fun createPdf(context: Context, file: File, invoices: List<Invoice>, statusFilter: InvoiceStatus?) {
        val locale = context.resources.configuration.locales[0]
        val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        val money = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        fun pen(amount: Double) = context.getString(R.string.common_amount_pen, money.format(amount))

        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var y = 50f

        fun text(value: String, size: Float = 12f, bold: Boolean = false, after: Float = 20f) {
            if (y > pageHeight - 60f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                y = 50f
            }
            paint.textSize = size
            paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            page.canvas.drawText(value, margin, y, paint)
            y += after
        }

        text("FRUITLOGIX", 22f, bold = true, after = 28f)
        text(context.getString(R.string.inv_pdf_title), 18f, bold = true, after = 24f)
        text(context.getString(R.string.inv_pdf_generated, LocalDate.now().format(date)))
        text(context.getString(R.string.inv_pdf_filter, statusLabel(context, statusFilter)), after = 30f)

        listOf(
            InvoiceType.RECEIVABLE to R.string.inv_to_collect,
            InvoiceType.PAYABLE to R.string.inv_to_pay
        ).forEach { (type, title) ->
            val group = invoices.filter { it.type == type }
            if (group.isEmpty()) return@forEach
            text(context.getString(title).uppercase(locale), 15f, bold = true, after = 24f)
            group.forEach { invoice ->
                text("${invoice.id} · ${invoice.counterpartyName}", 13f, bold = true, after = 18f)
                text(
                    "${context.getString(R.string.inv_issued, invoice.issuedOn.format(date))} · " +
                        context.getString(R.string.inv_due, invoice.dueOn.format(date)),
                    11f, after = 18f
                )
                text("${pen(invoice.amount)} · ${statusLabel(context, InvoiceRules.effectiveStatus(invoice))}", 12f, after = 26f)
            }
            text(context.getString(R.string.inv_pdf_total, pen(group.sumOf { it.amount })), 13f, bold = true, after = 34f)
        }

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
    }
}
