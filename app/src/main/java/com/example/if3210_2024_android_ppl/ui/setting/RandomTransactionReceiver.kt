package com.example.if3210_2024_android_ppl.ui.setting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RandomTransactionReceiver : BroadcastReceiver() {

    companion object {
        var title: String? = null
        var quantity: Int = 0
        var price: Double? = 0.0
        var category: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        title = intent?.getStringExtra("title")
        quantity = intent?.getIntExtra("quantity", 0) ?: 0 // Use the Elvis operator to handle null case
        price = intent?.getDoubleExtra("price", 0.0)
        category = intent?.getStringExtra("category")
    }
}