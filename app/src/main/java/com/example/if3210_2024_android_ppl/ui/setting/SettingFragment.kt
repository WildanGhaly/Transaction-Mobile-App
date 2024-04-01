package com.example.if3210_2024_android_ppl.ui.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.if3210_2024_android_ppl.LoginActivity
import com.example.if3210_2024_android_ppl.TokenCheckService
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.FragmentSettingBinding
import com.example.if3210_2024_android_ppl.util.EmailSender
import com.example.if3210_2024_android_ppl.util.ExcelFileCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private lateinit var userViewModel: UserViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        setupButtonListeners()

        return root
    }

    private fun sendReport(transactions: List<Transaction>, useXlsxFormat: Boolean) {
        context?.let { ctx ->
            val excelFileCreator = ExcelFileCreator(ctx)
            val emailSender = EmailSender(ctx)

            val fileUri = excelFileCreator.createExcelFile(transactions, useXlsxFormat)
            val mimeType = if (useXlsxFormat) {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            } else {
                "application/vnd.ms-excel"
            }
            emailSender.sendEmailWithAttachment(
                "dummy@example.com",
                "Transaction Report",
                "Here is your transaction report.",
                fileUri,
                mimeType
            )
        }
    }


    private fun setupButtonListeners() {
        binding.buttonSendTransaction.setOnClickListener {
            // TODO: Handle send transaction button click
            userViewModel.getActiveUserEmail { email ->
                if (email != null) {
                    Log.d("SettingFragment", "Active user's email: $email")

                    // TODO: getTransactions()
                    val transaction = listOf<Transaction>()
                    val isXlsxFormat = binding.switchXlsXlsx.isChecked // xlsx true, else false
                    sendReport(transaction, isXlsxFormat)

                } else {
                    Log.e("SettingFragment", "Failed to retrieve active user's email")
                }
            }

            Log.d("SettingFragment", "on click")
        }

        binding.buttonSaveTransaction.setOnClickListener {
            // TODO: Handle save transaction button click
            val transactions = listOf<Transaction>()
            val isXlsxFormat = binding.switchXlsXlsx.isChecked // xlsx true, else false
            context?.let { ctx ->
                val excelFileCreator = ExcelFileCreator(ctx)
                val fileUri = excelFileCreator.createExcelFile(transactions, isXlsxFormat)
                val mimeType = if (isXlsxFormat) {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            } else {
                "application/vnd.ms-excel"
            }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Excel File"))
            }
        }

        binding.buttonBelow.setOnClickListener {
            // TODO: Handle logout button click
            activity?.stopService(Intent(context, TokenCheckService::class.java))
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    userViewModel.logout()
                }
                withContext(Dispatchers.Main) {
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}