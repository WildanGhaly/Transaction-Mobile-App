package com.example.if3210_2024_android_ppl.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.transaction.TransactionAdapter
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import com.example.if3210_2024_android_ppl.databinding.FragmentTransactionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionFragment : Fragment() {

    val db by lazy { TransactionDatabase(requireContext()) }
    lateinit var transactionAdapter: TransactionAdapter

    private var _binding: FragmentTransactionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val transactionViewModel =
            ViewModelProvider(this).get(TransactionViewModel::class.java)

        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val navController = findNavController()

        val addButton: ImageButton = binding.addButton
        addButton.setOnClickListener {
            navController.navigate(R.id.navigation_addTransaction)
        }
        setupRecyclerView()
        return root
    }

    private fun setupRecyclerView() {
        val navController = findNavController()
        transactionAdapter = TransactionAdapter(arrayListOf(), object: TransactionAdapter.OnAdapterListener{
            override fun onClick(transaction: Transaction) {

//                Toast.makeText(requireContext(), transaction.name,
//                    Toast.LENGTH_SHORT).show()
                val bundle = bundleOf("transactionId" to transaction.id)
                navController.navigate(R.id.navigation_editTransaction, bundle)

            }

        })
        val list_transaction: RecyclerView = binding.listTransaction
        list_transaction.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter

        }
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch {
            val transactionList = db.transactionDao().getTransactions()
            Log.d("MainActivity","dbResponse: $transactionList")
            withContext(Dispatchers.Main) {
                transactionAdapter.setData(transactionList)
            }
        }
    }

    fun intentEdit() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}