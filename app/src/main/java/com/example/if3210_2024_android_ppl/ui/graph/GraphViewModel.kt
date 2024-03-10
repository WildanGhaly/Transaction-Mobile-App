package com.example.if3210_2024_android_ppl.ui.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GraphViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is graph Fragment"
    }
    val text: LiveData<String> = _text
}