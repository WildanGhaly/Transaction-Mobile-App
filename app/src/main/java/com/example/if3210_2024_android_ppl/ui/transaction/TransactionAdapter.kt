package com.example.if3210_2024_android_ppl.ui.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.database.transaction.Transaction

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
        holder.view.findViewById<TextView>(R.id.text_date).text = transaction.date
        holder.view.findViewById<TextView>(R.id.text_title).text = transaction.name
        holder.view.findViewById<TextView>(R.id.text_price).text = transaction.price.toString()
        holder.view.findViewById<TextView>(R.id.text_location).text = transaction.location
        holder.view.findViewById<TextView>(R.id.text_category).text = transaction.category
        holder.view.findViewById<LinearLayout>(R.id.transaction_container).setOnClickListener{
            listener.onClick(transaction)
        }
        holder.view.findViewById<ImageButton>(R.id.icon_delete).setOnClickListener {
            listener.onDelete(transaction)
        }
        holder.view.findViewById<Button>(R.id.buttonShowLocation).setOnClickListener {
            listener.showLocation(transaction)
        }
    }



    fun setData(transactionList: List<Transaction>) {
        transactions.clear()
        transactions.addAll(transactionList)
        notifyDataSetChanged()
    }

    interface OnAdapterListener {
        fun onClick(transaction: Transaction)
        fun onDelete(transaction: Transaction)
        fun showLocation(transaction: Transaction)
    }
}