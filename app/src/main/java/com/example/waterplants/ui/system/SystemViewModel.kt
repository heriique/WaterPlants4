package com.example.waterplants.ui.system

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model
import com.example.waterplants.model.Plant

class SystemViewModel : ViewModel() {
    val systemWaterLevel: LiveData<Int?> = Model.getInstance(null)?.systemWaterLevel!!
    val systemPlants: LiveData<List<Plant>> = Model.getInstance(null)?.systemPlants!!

    fun askForSystemStatus() {
        Model.getInstance(null)?.askForSystemStatus()
    }

    fun processMessage(msg: String) {
        Model.getInstance(null)?.processMessage(msg)
    }
}