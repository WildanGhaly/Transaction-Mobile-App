package com.example.if3210_2024_android_ppl.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is camera Fragment"
    }
    val text: LiveData<String> = _text
}