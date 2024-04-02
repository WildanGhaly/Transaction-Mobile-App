package com.example.if3210_2024_android_ppl.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExcelFileCreator(private val context: Context) {
    fun createExcelFile(transactions: List<Transaction>, useXlsxFormat: Boolean): Uri {
        val workbook = if (useXlsxFormat) {
            XSSFWorkbook()
        } else {
            HSSFWorkbook()
        }
        val sheet = workbook.createSheet("Transactions")

        // Define header
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Title")
        headerRow.createCell(1).setCellValue("Price")
        headerRow.createCell(2).setCellValue("Category")
        headerRow.createCell(3).setCellValue("Location")
        headerRow.createCell(4).setCellValue("Date")

        // Fill data
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(transaction.name ?: "")
            row.createCell(1).setCellValue(transaction.price?.toDouble() ?: 0.0)
            row.createCell(2).setCellValue(transaction.category ?: "")
            row.createCell(3).setCellValue(transaction.location ?: "")
            row.createCell(4).setCellValue(transaction.date ?: "")
        }

        // Save to a temp file
        val fileExtension = if (useXlsxFormat) ".xlsx" else ".xls"
        val file = File.createTempFile("Transactions", fileExtension, context.cacheDir)
        FileOutputStream(file).use { fos ->
            workbook.write(fos)
        }
        workbook.close()

        // Return a URI for this file
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}
