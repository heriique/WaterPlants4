package com.example.waterplants.ui.system

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model
import com.example.waterplants.model.Plant

class SystemViewModel : ViewModel() {
    val systemWaterLevel: LiveData<Int?> = Model.getInstance(null)?.systemWaterLevel!!
    val systemPlants: LiveData<List<Plant>> = Model.getInstance(null)?.systemPlants!!
    val isConnected: LiveData<Boolean> get() {return Model.getInstance(null)?.bluetooth?.isConnected ?: MutableLiveData(false) }

    fun askForSystemStatus() {
        Model.getInstance(null)?.askForSystemStatus()
    }
}