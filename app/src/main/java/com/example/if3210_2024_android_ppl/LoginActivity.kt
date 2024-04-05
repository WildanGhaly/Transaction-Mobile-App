package com.example.if3210_2024_android_ppl
import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.api.KeystoreHelper
import com.example.if3210_2024_android_ppl.api.LoginRequest
import com.example.if3210_2024_android_ppl.api.LoginResponse
import com.example.if3210_2024_android_ppl.api.RetrofitInstance
import com.example.if3210_2024_android_ppl.database.user.User
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.ActivityLoginBinding
import com.example.if3210_2024_android_ppl.util.DialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mUserViewModel: UserViewModel
    private var loadingDialog: AlertDialog? = null
    private var isLoginInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        binding.buttonLogin.setOnClickListener {
            if (!isLoginInProgress) {
                isLoginInProgress = true
                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()
                if (email.isEmpty() || password.isEmpty()) {
                    Log.d("LoginActivity", "Email or password is empty")
                } else {
                    if (isNetworkAvailable(this)) {
                        loadingDialog = DialogUtils.showLoadingDialog(this)
                        Log.d("LoginActivity", "Email: $email, Password: $password")

                        RetrofitInstance.api.login(LoginRequest(email, password))
                            .enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(
                                    call: Call<LoginResponse>,
                                    response: Response<LoginResponse>
                                ) {
                                    isLoginInProgress = false
                                    hideLoadingDialog()
                                    if (response.isSuccessful) {
                                        val loginResponse = response.body()
                                        if (loginResponse != null) {
                                            Log.d("LoginActivity", "Login successful, Token: ${loginResponse.token}")
                                            val keystoreHelper = KeystoreHelper(this@LoginActivity)
                                            keystoreHelper.saveToken(loginResponse.token)
                                            val user1 = User(null, email)
                                            mUserViewModel.addUser(user1)
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    } else {
                                        DialogUtils.showLoginFailedDialog(this@LoginActivity)
                                        Log.d("LoginActivity", "Login failed")
                                    }
                                }

                                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                    isLoginInProgress = false
                                    hideLoadingDialog()
                                    if (t is SocketTimeoutException) {
                                        Log.e("LoginActivity", "Request timed out")
                                        DialogUtils.showTimeoutDialog(this@LoginActivity)
                                    } else {
                                        Log.e("LoginActivity", "Login failed", t)
                                        DialogUtils.showLoginFailedDialog(this@LoginActivity)
                                    }
                                }
                            })
                    } else {
                        DialogUtils.showNoInternetDialog(this)
                        isLoginInProgress = false
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

}
