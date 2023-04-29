package com.example.waterplants.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model
import java.util.Collections

class ScheduleViewModel : ViewModel() {
    //var selectedHoseInt = 1
    val selectedHose = MutableLiveData<Int>().apply { value = 0 }

    fun select(position: Int) {
        Model.getInstance(null)?.select(selectedHose.value!!, position)
    }
}