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
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.FragmentSettingBinding
import com.example.if3210_2024_android_ppl.util.DialogUtils
import com.example.if3210_2024_android_ppl.util.EmailSender
import com.example.if3210_2024_android_ppl.util.ExcelFileCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeoutOrNull

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private lateinit var userViewModel: UserViewModel
    private val db by lazy { TransactionDatabase(requireContext()) }
    private var isOperationInProgress = false
    private var isSaveOperationInProgress = false
    private var isLogoutOperationInProgress = false

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

            userViewModel.getActiveUserEmail { email ->
                emailSender.sendEmailWithAttachment(
                    email?: "13521015@std.stei.itb.ac.id",
                    "Transaction Report",
                    "Here is your transaction report.",
                    fileUri,
                    mimeType
                )
            }
        }
    }

    private fun setupButtonListeners() {
        binding.buttonSendTransaction.setOnClickListener {
            if (!isOperationInProgress) {
                isOperationInProgress = true
                val dialog = DialogUtils.showLoadingDialog(requireContext())
                userViewModel.getActiveUserEmail { email ->
                    if (email != null) {
                        lifecycleScope.launch {
                            try {
                                val job = withTimeoutOrNull(10000) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val transactions = db.transactionDao().getTransactions()
                                        val isXlsxFormat = binding.switchXlsXlsx.isChecked
                                        sendReport(transactions, isXlsxFormat)
                                    }
                                }
                                if (job == null) {
                                    DialogUtils.showTimeoutDialog(requireContext())
                                }
                            } catch (e: TimeoutCancellationException) {
                                DialogUtils.showTimeoutDialog(requireContext())
                            } finally {
                                dialog.dismiss()
                                isOperationInProgress = false
                            }
                        }
                    } else {
                        Log.e("SettingFragment", "Failed to retrieve active user's email")
                        dialog.dismiss()
                        isOperationInProgress = false
                    }
                }
            }
        }

        binding.buttonSaveTransaction.setOnClickListener {
            if (!isSaveOperationInProgress) {
                isSaveOperationInProgress = true
                val dialog = DialogUtils.showLoadingDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        val job = withTimeoutOrNull(10000) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val transactions = db.transactionDao().getTransactions()
                                val isXlsxFormat = binding.switchXlsXlsx.isChecked
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
                                    withContext(Dispatchers.Main) {
                                        startActivity(Intent.createChooser(shareIntent, "Share Excel File"))
                                    }
                                }
                            }
                        }
                        if (job == null) {
                            DialogUtils.showTimeoutDialog(requireContext())
                        }
                    } catch (e: TimeoutCancellationException) {
                        DialogUtils.showTimeoutDialog(requireContext())
                    } finally {
                        dialog.dismiss()
                        isSaveOperationInProgress = false
                    }
                }
            }
        }


        binding.buttonBelow.setOnClickListener {
            if (!isLogoutOperationInProgress) {
                isLogoutOperationInProgress = true
                val dialog = DialogUtils.showLoadingDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        activity?.stopService(Intent(context, TokenCheckService::class.java))
                        withContext(Dispatchers.IO) {
                            userViewModel.logout()
                        }
                        withContext(Dispatchers.Main) {
                            val intent = Intent(activity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        }
                    } finally {
                        dialog.dismiss()
                        isLogoutOperationInProgress = false
                    }
                }
            }
        }


        binding.buttonRandomizeTransaction.setOnClickListener {
            // TODO: RANDOMIZE TRANSACTION HERE
            // TODO: RANDOMIZE TRANSACTION HERE
            // TODO: RANDOMIZE TRANSACTION HERE

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}