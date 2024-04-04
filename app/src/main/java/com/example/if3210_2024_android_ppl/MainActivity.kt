package com.example.if3210_2024_android_ppl

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.content.BroadcastReceiver
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.if3210_2024_android_ppl.api.KeystoreHelper
import com.example.if3210_2024_android_ppl.api.RetrofitInstance
import com.example.if3210_2024_android_ppl.api.TokenResponse
import com.example.if3210_2024_android_ppl.database.user.User
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.ActivityMainBinding
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.view.ViewGroup
import androidx.navigation.NavController
import com.google.android.material.navigationrail.NavigationRailView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: BroadcastReceiver
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter("com.example.ACTION_SESSION_EXPIRED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(sessionExpiredReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(sessionExpiredReceiver, filter)
        }
        startService(Intent(this, TokenCheckService::class.java))


        val networkManager = NetworkManager(this)
        networkManager.observe(this){
            if(it){
                Log.d("Network", "Connected")
            }else{
                Log.d("Network", "Disconnected")
                showNetworkErrorDialog()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.navView as NavigationRailView
        } else {
            binding.navView as BottomNavigationView
        }

//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transaction, R.id.navigation_scan, R.id.navigation_graph, R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView?.setupWithNavController(navController)

        // Check network status and show dialog if network is not available
        // Register BroadcastReceiver for network changes
        networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (!isNetworkAvailable()) {
                    showNetworkErrorDialog()
                }
            }
        }
        ContextCompat.registerReceiver(this,networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    private val sessionExpiredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showSessionExpireDialog()
        }
    }

    private fun showSessionExpireDialog() {
        stopService(Intent(this, TokenCheckService::class.java))
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_session_expire, null)
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(loginIntent)

            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showNetworkErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Network Error")
            .setMessage("No internet connection available. Please check your network settings.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
