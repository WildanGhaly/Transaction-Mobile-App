package com.example.if3210_2024_android_ppl
import android.app.AlertDialog
import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.api.LoginRequest
import com.example.if3210_2024_android_ppl.api.LoginResponse
import com.example.if3210_2024_android_ppl.api.RetrofitInstance
import com.example.if3210_2024_android_ppl.database.user.User
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding // Binding untuk layout XML login
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Log.d("LoginActivity", "Email or password is empty")
            } else {
                showLoadingDialog()
                Log.d("LoginActivity", "Email: $email, Password: $password")

                RetrofitInstance.api.login(LoginRequest(email, password))
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            hideLoadingDialog()
                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                // Handle successful login response
                                if (loginResponse != null) {
                                    Log.d("LoginActivity", "Login successful, Token: ${loginResponse.token}")
                                    val user1 = User(null, email, loginResponse.token)
                                    mUserViewModel.addUser(user1)
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // Handle unsuccessful login response
                                showLoginFailedDialog()
                                Log.d("LoginActivity", "Login failed")
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            // Handle failure
                            Log.e("LoginActivity", "Login failed", t)
                            showLoginFailedDialog()
                        }
                    })
            }
        }
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_login, null))
        builder.setCancelable(false) // Make it not cancellable

        loadingDialog = builder.create()
        loadingDialog.show()
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun showLoginFailedDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login_fail, null)
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }
}
