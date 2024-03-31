package com.example.if3210_2024_android_ppl.ui.setting

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.FragmentSettingBinding

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

    private fun setupButtonListeners() {
        binding.buttonSendTransaction.setOnClickListener {
            // TODO: Handle send transaction button click
            userViewModel.getActiveUserEmail { email ->
                if (email != null) {
                    Log.d("SettingFragment", "Active user's email: $email")

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "message/rfc822"
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Tes judul")
                    intent.putExtra(Intent.EXTRA_TEXT, "Tes body")
                    intent.setPackage("com.google.android.gm")

                    try {
                        startActivity(intent)
                    } catch (ex: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), "Aplikasi Gmail tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.e("SettingFragment", "Failed to retrieve active user's email")
                }
            }
        }

        binding.buttonSaveTransaction.setOnClickListener {
            // TODO: Handle save transaction button click
        }

        binding.buttonBelow.setOnClickListener {
            // TODO: Handle logout button click
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}