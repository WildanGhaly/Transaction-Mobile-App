package com.example.if3210_2024_android_ppl.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class EmailSender(private val context: Context) {
    fun sendEmailWithAttachment(email: String, subject: String, body: String, fileUri: Uri, mimeType: String) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
            type = mimeType
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.google.android.gm")
        }

        try {
            if (emailIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(emailIntent)
            } else {
                throw android.content.ActivityNotFoundException()
            }
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "Gmail is not installed.", Toast.LENGTH_SHORT).show()
        }
    }
}

