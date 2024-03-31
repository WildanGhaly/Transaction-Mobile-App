package com.example.if3210_2024_android_ppl.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTransactionFragment : Fragment() {

    private val db by lazy { TransactionDatabase(requireContext()) }
    private var transactionId: Int = 0
    private val listItem = arrayOf(
        "Pembelian", "Pemasukan"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)

        val adapterItems = ArrayAdapter(requireContext(), R.layout.list_dropdown, listItem)
        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.addCategory)
        autoCompleteTextView.setAdapter(adapterItems)
        autoCompleteTextView.setOnItemClickListener { adapterView, _, i, _ ->
            val item = adapterView.getItemAtPosition(i).toString()
            Toast.makeText(requireContext(), "Item: $item", Toast.LENGTH_SHORT).show()
        }

        setupListener(view)


        // Check if transactionId is provided via arguments for editing
        val transactionId = arguments?.getInt("transactionId", 0)
        if (transactionId != null && transactionId != 0) {
            // If transactionId is not null and not 0, it means it's for editing
            populateTransactionDetails(transactionId)
        }

        return view
    }
    private fun setupListener(view: View) {
        val saveButton = view.findViewById<Button>(R.id.buttonSave)

        saveButton.setOnClickListener {
            saveTransaction(view)
        }
    }

    private fun populateTransactionDetails(transactionId: Int) {
        // Coroutine for fetching transaction details from the database
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = db.transactionDao().getTransactions(transactionId)
            if (transactions.isNotEmpty()) {
                val transaction = transactions[0] // Get the first transaction
                requireActivity().runOnUiThread {
                    // Populate UI fields with transaction details
                    view?.findViewById<EditText>(R.id.addTextTitle)?.setText(transaction.name)
                    view?.findViewById<EditText>(R.id.addPrice)?.setText(transaction.price.toString())
                    view?.findViewById<EditText>(R.id.addLocation)?.setText(transaction.location)
                    view?.findViewById<AutoCompleteTextView>(R.id.addCategory)?.setText(transaction.category)
                }
            }
        }
    }


    private fun saveTransaction(view: View) {
        val titleText = view.findViewById<EditText>(R.id.addTextTitle).text.toString()
        val priceText = view.findViewById<EditText>(R.id.addPrice).text.toString().toIntOrNull() ?: 0
        val locationText = view.findViewById<EditText>(R.id.addLocation).text.toString()
        val categoryText = view.findViewById<AutoCompleteTextView>(R.id.addCategory).text.toString()
        val currentDate = LocalDate.now().toString()

        if (titleText.isNotEmpty()) {
            // Check if transactionId is provided via arguments for editing
            val transactionId = arguments?.getInt("transactionId", 0)
            if (transactionId != null && transactionId != 0) {
                // If transactionId is provided and not 0, update the existing transaction
                CoroutineScope(Dispatchers.IO).launch {
                    db.transactionDao().updateTransaction(
                        Transaction(
                            transactionId,
                            0, // You may need to pass the user ID or any other relevant ID here
                            titleText,
                            priceText,
                            locationText,
                            currentDate,
                            categoryText
                        )
                    )
                    requireActivity().runOnUiThread {
                        findNavController().navigateUp()
                    }
                }
            } else {
                // If transactionId is not provided or 0, it means it's for adding a new transaction
                CoroutineScope(Dispatchers.IO).launch {
                    db.transactionDao().addTransaction(
                        Transaction(0, 0, titleText, priceText, locationText, currentDate, categoryText)
                    )
                    requireActivity().runOnUiThread {
                        findNavController().navigateUp()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please enter title", Toast.LENGTH_SHORT).show()
        }
    }
}

