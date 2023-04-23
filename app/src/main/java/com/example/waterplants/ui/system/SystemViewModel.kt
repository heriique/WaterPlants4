package com.example.waterplants.ui.system

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model

class SystemViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is system Fragment"
    }
    val text: LiveData<String> = _text

    val systemWaterLevel: LiveData<String> = Model.getInstance(null)?.systemWaterLevel!!

    fun askForSystemStatus() {
        Model.getInstance(null)?.askForSystemStatus()
    }

    fun processMessage(msg: String) {
        Model.getInstance(null)?.processMessage(msg)
    }
}