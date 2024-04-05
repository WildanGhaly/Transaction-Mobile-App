package com.example.if3210_2024_android_ppl

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import com.example.if3210_2024_android_ppl.api.KeystoreHelper
import com.example.if3210_2024_android_ppl.api.RetrofitInstance
import com.example.if3210_2024_android_ppl.api.TokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenCheckService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        checkTokenExpiration()
        return START_STICKY
    }

    private fun checkTokenExpiration() {
        runnable = Runnable {
            isTokenExpired()
            handler.postDelayed(runnable, 60000)
        }
        handler.post(runnable)
    }

    private fun isTokenExpired() {
        val keystoreHelper = KeystoreHelper(applicationContext)
        val token = keystoreHelper.getToken()
        val authorizationHeaderValue = "Bearer $token"

        val call: Call<TokenResponse> = RetrofitInstance.api.token(authorizationHeaderValue)
        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { tokenResponse ->
                        Log.d("TokenCheckService", "Token valid, NIM: ${tokenResponse.nim}")
                    }
                } else {
                    Log.d("TokenCheckService", "Token might be invalid or expired.")
                    handleExpiredToken()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.d("TokenCheckService", "Failed to validate token: ${t.message}")
                handleExpiredToken()
            }
        })
    }

    private fun handleExpiredToken() {
        val intent = Intent("com.example.ACTION_SESSION_EXPIRED")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        if(::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        super.onDestroy()
    }

}
