package com.example.if3210_2024_android_ppl.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BillItem(
    val name: String,
    val qty: Int,
    val price: Double
) : Parcelable