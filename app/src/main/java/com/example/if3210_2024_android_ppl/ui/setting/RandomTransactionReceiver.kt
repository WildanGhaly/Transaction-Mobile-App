package com.example.if3210_2024_android_ppl.ui.setting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RandomTransactionReceiver : BroadcastReceiver() {

    companion object {
        var title: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        title = intent?.getStringExtra("title")
        Log.d("Main Activity", "dbResponse : $title")
    }
}