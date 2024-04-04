package com.example.if3210_2024_android_ppl.ui.transaction

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import com.example.if3210_2024_android_ppl.database.transaction.TransactionViewModel
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.ui.setting.RandomTransactionReceiver
import com.example.if3210_2024_android_ppl.util.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTransactionFragment : Fragment() {

    private val db by lazy { TransactionDatabase(requireContext()) }
    private lateinit var userViewModel: UserViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private var transactionId: Int = 0
    private val listItem = arrayOf(
        "Pembelian", "Pemasukan"
    )

    private lateinit var locationHelper: LocationHelper
    private val randomTransactionReceiver = RandomTransactionReceiver()

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 1002
    }

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

        val permissionFineLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCoarseLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED || permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            // Request both fine and coarse location permissions
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            // Permissions are already granted, proceed with the location fetching
        }

        locationHelper = LocationHelper(requireContext())
        view.findViewById<EditText>(R.id.addLocation).setOnClickListener {
            locationHelper.getLocationDetails { locationName, latitude, longitude ->
                Toast.makeText(requireContext(), "Getting Your Current Location", Toast.LENGTH_SHORT).show()
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    view.findViewById<EditText>(R.id.addLocation).setText("$locationName ($latitude, $longitude)")
                } else {
                    Toast.makeText(requireContext(), "Input Your Location Manually", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // Check if transactionId is provided via arguments for editing
        val transactionId = arguments?.getInt("transactionId", 0)
        if (transactionId != null && transactionId != 0) {
            // If transactionId is not null and not 0, it means it's for editing
            populateTransactionDetails(transactionId)
        }

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        val title = RandomTransactionReceiver.title
        val quantity = RandomTransactionReceiver.quantity
        val price = RandomTransactionReceiver.price
        val category = RandomTransactionReceiver.category
        view.findViewById<EditText>(R.id.addTextTitle).setText(title)
        view.findViewById<EditText>(R.id.addQuantity).setText(quantity.toString())
        view.findViewById<EditText>(R.id.addPrice).setText(price.toString())
        view.findViewById<EditText>(R.id.addCategory).setText(category)

        return view
    }
    private fun setupListener(view: View) {
        val saveButton = view.findViewById<Button>(R.id.buttonSave)

        saveButton.setOnClickListener {
            saveTransaction(view)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("Main Activity","dbResponse: masuk")
        val filter = IntentFilter("com.example.if3210_2024_android_ppl.RANDOM_TRANSACTION")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(randomTransactionReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        Log.d("Main Activity","dbResponse: keluar")
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomTransactionReceiver)
    }

    fun resetTransactionData() {
        RandomTransactionReceiver.title = null
        RandomTransactionReceiver.quantity = 0
        RandomTransactionReceiver.price = 0.0
        RandomTransactionReceiver.category = null
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
                    view?.findViewById<EditText>(R.id.addQuantity)?.setText(transaction.quantity.toString())
                    view?.findViewById<EditText>(R.id.addPrice)?.setText(transaction.price.toString())
                    view?.findViewById<EditText>(R.id.addLocation)?.setText(transaction.location)
                    view?.findViewById<AutoCompleteTextView>(R.id.addCategory)?.setText(transaction.category)
                }
            }
        }
    }


    private fun saveTransaction(view: View) {
        val titleText = view.findViewById<EditText>(R.id.addTextTitle).text.toString()
        val quantityText = view.findViewById<EditText>(R.id.addQuantity).text.toString().toIntOrNull() ?: 0
        val priceText = view.findViewById<EditText>(R.id.addPrice).text.toString().toDoubleOrNull() ?: 0.0
        val locationText = view.findViewById<EditText>(R.id.addLocation).text.toString()
        val categoryText = view.findViewById<AutoCompleteTextView>(R.id.addCategory).text.toString()
        val currentDate = LocalDate.now().toString()

        var locationName: String
        var latitude: Double = 0.0
        var longitude: Double = 0.0

        val parts = locationText.split("(")
        if (parts.size >= 2) {
            // Extract location name
            locationName = parts[0].trim()

            // Extract latitude and longitude from the second part
            val coordinates = parts[1].replace(")", "").split(",")
            if (coordinates.size >= 2) {
                latitude = coordinates[0].trim().toDoubleOrNull() ?: 0.0
                longitude = coordinates[1].trim().toDoubleOrNull() ?: 0.0
            }
        } else {
            locationName = locationText
            latitude = -6.9274065413170725
            longitude = 107.76996019357847
        }

        userViewModel.getActiveUserEmail { email ->
            if (titleText.isNotEmpty()) {
                // Check if transactionId is provided via arguments for editing
                val transactionId = arguments?.getInt("transactionId", 0)
                if (transactionId != null && transactionId != 0) {
                    // If transactionId is provided and not 0, update the existing transaction
                    transactionViewModel.updateTransaction(
                        Transaction(
                            transactionId,
                            email, // You may need to pass the user ID or any other relevant ID here
                            titleText,
                            quantityText,
                            priceText,
                            locationName,
                            currentDate,
                            categoryText,
                            latitude,
                            longitude
                        )
                    )
                    requireActivity().runOnUiThread {
                        findNavController().navigateUp()
                    }
                } else {
                    // If transactionId is not provided or 0, it means it's for adding a new transaction
                    transactionViewModel.addTransaction(
                        Transaction(0, email, titleText, quantityText, priceText, locationName, currentDate, categoryText, latitude, longitude)
                    )
                    requireActivity().runOnUiThread {
                        findNavController().navigateUp()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter title", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetTransactionData()
    }
}

