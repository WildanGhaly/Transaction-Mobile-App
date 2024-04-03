package com.example.if3210_2024_android_ppl.api

data class BillResponse(
    val items: BillItems
)

data class BillItems(
    val items: List<BillItem>
)

data class BillItem(
    val name: String,
    val qty: Int,
    val price: Double
)
