package com.example.waterplants.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model

class HomeViewModel: ViewModel() {

    val isConnected: LiveData<Boolean> get() {return Model.getInstance(null)?.bluetooth?.isConnected ?: MutableLiveData(false) }
    var firstTime = true

    fun connect() {
        Model.getInstance(null)?.bluetooth?.scanAndConnect()
    }

    fun disconnect() {
        Model.getInstance(null)?.bluetooth?.disconnect()
    }
}