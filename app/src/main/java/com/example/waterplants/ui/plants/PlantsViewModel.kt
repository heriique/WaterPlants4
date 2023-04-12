package com.example.waterplants.ui.plants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlantsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is plants Fragment"
    }
    val text: LiveData<String> = _text
}