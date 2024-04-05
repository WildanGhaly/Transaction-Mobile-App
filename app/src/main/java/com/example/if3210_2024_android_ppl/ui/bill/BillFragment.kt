package com.example.if3210_2024_android_ppl.ui.bill

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.api.BillItem
import com.example.if3210_2024_android_ppl.api.MultiBill
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.FragmentBillBinding
import com.example.if3210_2024_android_ppl.databinding.FragmentTransactionBinding
import com.example.if3210_2024_android_ppl.util.DialogUtils
import com.example.if3210_2024_android_ppl.util.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillFragment : Fragment() {

    private var items: List<BillItem>? = null
    private val db by lazy { TransactionDatabase(requireContext()) }
    private lateinit var userViewModel: UserViewModel
    private lateinit var locationHelper: LocationHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_bill, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationHelper = LocationHelper(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val multiBill = arguments?.getParcelable<MultiBill>("arrBil")
        val billItems = multiBill?.items ?: listOf()

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

        setupRecyclerView(billItems)

        // Setting up the ImageButton click listener
        val saveButton = view.findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            val loadingDialog = DialogUtils.showLoadingDialog(requireContext())
            fetchAndSaveLocation(billItems, loadingDialog)
        }

    }

    private fun fetchAndSaveLocation(billItems: List<BillItem>, loadingDialog: AlertDialog) {
        val locationNameTemp = null ?: "ITB"
        val latitudeTemp = null ?: -6.9274065413170725
        val longitudeTemp = null ?: 107.76996019357847

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationHelper.getLocationDetails { locationName, latitude, longitude ->
                saveTransactions(billItems, locationName?:locationNameTemp, latitude?:latitudeTemp, longitude?:longitudeTemp, loadingDialog)
            }
        } else {
            saveTransactions(billItems, locationNameTemp, latitudeTemp, longitudeTemp, loadingDialog)
        }
    }

    private fun saveTransactions(
        billItems: List<BillItem>,
        locationName: String?,
        latitude: Double,
        longitude: Double,
        loadingDialog: AlertDialog
    ) {
        userViewModel.getActiveUserEmail { email ->
            val transactions = billItems.map { item ->
                Transaction(
                    id = 0,
                    idUser = email,
                    name = item.name,
                    price = item.price,
                    quantity = item.qty,
                    location = locationName,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    category = "Pemasukan",
                    latitude = latitude,
                    longitude = longitude
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.transactionDao().addMultiTransaction(transactions)
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        DialogUtils.showTransactionSavedDialog(requireContext())
                        findNavController().navigateUp()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        DialogUtils.showSomethingWentWrongDialog(requireContext())
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigate(R.id.action_navigation_bill_to_navigation_scan)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView(items: List<BillItem>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.itemsRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = BillAdapter(items)
    }

    companion object {
        const val PERMISSIONS_REQUEST_LOCATION = 101
    }

}
