package com.example.if3210_2024_android_ppl.database.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.if3210_2024_android_ppl.R

class TransactionAdapter (private val transactions: ArrayList<Transaction>, private val listener: OnAdapterListener) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return TransactionViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.view.findViewById<TextView>(R.id.text_title).text = transaction.name
        holder.view.findViewById<TextView>(R.id.text_price).text = transaction.price.toString()
        holder.view.findViewById<TextView>(R.id.text_location).text = transaction.location
        holder.view.findViewById<TextView>(R.id.text_category).text = transaction.category
        holder.view.findViewById<LinearLayout>(R.id.transaction_container).setOnClickListener{
            listener.onClick(transaction)
        }
    }

    fun setData(transactionList: List<Transaction>) {
        transactions.clear()
        transactions.addAll(transactionList)
        notifyDataSetChanged()
    }

    interface OnAdapterListener {
        fun onClick(transaction: Transaction)
    }
}