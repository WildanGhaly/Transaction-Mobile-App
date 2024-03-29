package com.example.if3210_2024_android_ppl.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null

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

        setupButtonListeners()

        return root
    }

    private fun setupButtonListeners() {
        binding.buttonSendTransaction.setOnClickListener {
            // TODO: Handle send transaction button click
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