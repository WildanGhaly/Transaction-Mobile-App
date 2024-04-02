package com.example.if3210_2024_android_ppl.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TransactionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is transaction Fragment"
    }
    val text: LiveData<String> = _text
}