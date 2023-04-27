package com.example.waterplants.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model

class HomeViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to PlanteVanner 11000!"
    }
    val text: LiveData<String> = _text

    fun connect() {
        Model.getInstance(null)?.bluetooth?.connect()
    }
}