package com.example.if3210_2024_android_ppl.ui.bill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.api.BillItem

class BillAdapter(private val items: List<BillItem>) : RecyclerView.Adapter<BillAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val quantityTextView: TextView = itemView.findViewById(R.id.itemQuantityTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.itemPriceTextView)

        fun bind(item: BillItem) {
            nameTextView.text = item.name
            quantityTextView.text = "Qty: ${item.qty}"
            priceTextView.text = "Price: ${item.price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_bill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
